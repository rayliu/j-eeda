package controllers.yh.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.util.CollectionUtils;

import com.google.gson.Gson;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.eeda.ModuleController;

public class EedaCommonHandler {
    private static Logger logger = Logger.getLogger(EedaCommonHandler.class);
    
    /**
     * editOrder页面: save传回来的结构, 查询order传出去的结构按以下格式构造
     * orderDto{
     *    id:1，                  //单据的id，
     *    module_id: 13，         //对应的模块id，
     *    action: "保存",         //对应的按钮动作
     *    fields_list:[{         //类型为“字段”的数据，只有一行
     *          id:1,            //对应表名T_3.id
     *          structure_id:3,  //对应表名T_3
     *          F1_DH: "YS1001", //对应表中字段T_3.F1_DH
     *          F2_KH: "1",      //对应表中字段T_3.F2_KH
     *          F3_GYS: "8"      //对应表中字段T_3.F3_GYS
     *      }，{}],
     *    table_list:[           //类型为“列表”的数据，相当于从表
     *      {
     *         structure_id: 5,         //对应表名T_5
     *         structure_parent_id: 3,  //此从表对应主表为structure_id:3,
     *         row_list:[               //对应表名T_5中的多行
     *              {
     *                  id: 1,          //对应表名T_5.id
     *                  parent_id:1,    //对应表名T_3.id=1
     *                  F13_XH: "ewqe", //对应表中字段T_5.F13_XH
     *                  F14_SL: ""
     *                  F15_TJ: ""
     *                  F16_ZL: ""
     *                  F17_DZ: ""
     *              }
     *         ] 
     *      },{}
     *    ],  
     * }
     */
    public static Record getOrderDto(String jsonStr){
        Record orderRec = new Record();
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        String order_id = dto.get("id").toString();
        
        orderRec.set("id", order_id);
        orderRec.set("module_id", dto.get("MODULE_ID"));
        
        List fieldsList = new ArrayList();
        List tableList  = new ArrayList();
        orderRec.set("fields_list", fieldsList);
        orderRec.set("table_list", tableList);
        
        List<Map<String, ?>> strctureList = (ArrayList<Map<String, ?>>)dto.get("STRUCTURE_LIST");
        for (Map<String, ?> structure : strctureList) {
            
            if("字段".equals(structure.get("STRUCTURE_TYPE"))){
                Record fieldRec = buildFieldRec(order_id, structure);
                fieldsList.add(fieldRec);
            }else{
                Record tableRec = new Record();
                int structureId = ((Double)structure.get("ID")).intValue();
                int parentStructureId = ((Double)structure.get("PARENT_ID")).intValue();
                tableRec.set("structure_id", structureId);
                tableRec.set("structure_parent_id", parentStructureId);
                
                if(structure.get("PARENT_ID") != null){
                    String tableName = "T_" + structureId;
                    List<Record> rowList = Db.find("select * from "+tableName+" where parent_id="+order_id);
                    tableRec.set("row_list", rowList);
                    tableList.add(tableRec);
                }
                
            }
        }
        logger.debug(orderRec.toJson());
        return orderRec;
    }
    
    public static Map searchOrder(Enumeration<String>  paraNames, HttpServletRequest request){
        Map orderListMap = new HashMap();
        String structur_id = "";
        
        String draw = "";
        String start = "";
        String length = "";
        
        String colCondition = "";
        String subCol = "";
        
        //获取field定义
        List<String> fieldIdList = new ArrayList<String>();
        List<String> fieldNameList = new ArrayList<String>();
        for (Enumeration<String> e = paraNames; paraNames.hasMoreElements();){
            String paraName = e.nextElement();
            String paraValue = request.getParameter(paraName);
            System.out.println(paraName + "=" + paraValue);
            
            if("structure_id".equals(paraName)){
                structur_id = paraValue;
            }
            if("draw".equals(paraName)){
                draw = paraValue;
            }
            if("start".equals(paraName)){
                start = paraValue;
            }
            if("length".equals(paraName)){
                length = paraValue;
            }
            if(paraName.startsWith("F") && !paraName.endsWith("_INPUT")){//获取field定义
                String id = paraName.split("_")[0].replace("F", "");
                fieldIdList.add(id);
                fieldNameList.add(paraName);
            }
            
        }

        List<Record> resultList = null;
        //获取field定义, 判断是否需要针对特殊列表转换ID -> 名字
        String fieldSql = "select * from field where id in(" + StringUtils.join(fieldIdList, ", ")+")";
        List<Record> fieldDefineList = Db.find(fieldSql);
        
        Map<String, Map> dateMap = new HashMap<String, Map>();
        for (String fieldName : fieldNameList) {
                String key = fieldName;
                String value = request.getParameter(key);
                
                logger.debug("key="+key);
                if(key.endsWith("_begin_time")){
                    String dateFieldKey = key.substring(0, key.indexOf("_begin_time"));
                    Map dateValueMap = new HashMap();
                    dateValueMap = dateMap.get(dateFieldKey);
                    if(dateValueMap==null){
                        dateValueMap = new HashMap();
                    }
                    dateValueMap.put("begin_time", value);
                }else if(key.endsWith("_end_time")){
                    String dateFieldKey = key.substring(0, key.indexOf("_end_time"));
                    Map dateValueMap = new HashMap();
                    dateValueMap = dateMap.get(dateFieldKey);
                    if(dateValueMap==null){
                        dateValueMap = new HashMap();
                    }
                    dateValueMap.put("end_time", value);
                }else{
                    Record fieldDefine = getFieldDefine(key, fieldDefineList);
                    if("下拉列表".equals(fieldDefine.get("field_type")) 
                           && ("客户列表".equals(fieldDefine.get("field_type_ext_type"))
                               || "供应商列表".equals(fieldDefine.get("field_type_ext_type"))
                              )
                      ){
                        subCol += ", (select abbr from contact where id="+key+") "+ key +"_INPUT";
                        if(StringUtils.isNotEmpty(value)){
                            colCondition += " and " + key + " = '" + value + "'";
                        }
                    }else{
                        if(StringUtils.isNotEmpty(value))
                            colCondition += " and " + key + " like '%" + value + "%'";
                    }
                }
        }
        
        //处理日期
        for (Entry<String, Map> entry: dateMap.entrySet()) {
            String key = entry.getKey();
            Map valueMap = entry.getValue();
            colCondition += "and " + key + " between '" + valueMap.get("begin_time") + " 00:00:00' and " + valueMap.get("end_time") +" 23:59:59";
        }
        
        String sql = "select t.* "+ subCol +" from T_" + structur_id +" t where 1=1 " + colCondition;
        
        String totalSql = "select count(1) total from (" + sql + ") A";
        
        Record rec = Db.findFirst(totalSql);
        String sLimit = " limit " + start + ", " +length; 
        resultList = Db.find(sql +" order by id desc " + sLimit);
        if(resultList == null)
            resultList = Collections.EMPTY_LIST;
        
        
        orderListMap.put("draw", draw);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", resultList);

        return orderListMap;
    }

    private static Record getFieldDefine(String fieldName, List<Record> fieldDefineList){
        Record rec = null;
        for (Record record : fieldDefineList) {
            String recFieldName = "F" + record.get("id") + "_" + record.get("field_name");
            if(fieldName.equals(recFieldName)){
                rec = record;
                break;
            }
        }
        return rec;
    }
    
    private static Record buildFieldRec(String order_id, Map<String, ?> structure) {
        int structureId = ((Double)structure.get("ID")).intValue();
        Record orderRec;
       
        if(structure.get("PARENT_ID") == null){
            String tableName = "T_" + structureId;
            
            List<Map> fieldList = (ArrayList<Map>)structure.get("FIELDS_LIST");
            orderRec = Db.findById(tableName, order_id);
            String[] colNames = orderRec.getColumnNames();
            for (int i = 0; i < colNames.length; i++) {
                String colName = colNames[i];
                if(colName.endsWith("_KH")){
                    Record rec = Db.findFirst("select * from contact where id=?", orderRec.get(colName));
                    if(rec != null){
                        orderRec.set(colName+"_input", rec.get("abbr"));
                    }
                }else if(colName.endsWith("_GYS")){
                    Record rec = Db.findFirst("select * from contact where id=?", orderRec.get(colName));
                    if(rec != null){
                        orderRec.set(colName+"_input", rec.get("abbr"));
                    }
                }
            }
            orderRec.set("structure_id", structureId);
            logger.debug(orderRec.toJson());
            
        }else{
            int parentId = ((Double)structure.get("PARENT_ID")).intValue();
            String tableName = "T_" + structureId;
            orderRec = Db.findById(tableName, "parent_id", order_id);
            if(orderRec != null)
                orderRec.set("structure_id", structureId);
            logger.debug(orderRec.toJson());
        }
        return orderRec;
    }
    
    public static void commonUpdate(Map<String, ?> dto) {
        String orderId = dto.get("id").toString();
        List<Map<String, String>> fields_list = (ArrayList<Map<String, String>>)dto.get("fields_list");
        for (Map<String, String> tableMap : fields_list) {//获取每一个主表+主表从属表
            String structure_id = tableMap.get("structure_id");
            String colSet = "";
            for (Entry<String, String> entry: tableMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if("id".equals(key) || "structure_id".equals(key)){
                    continue;
                }
                colSet += ","+key + "='"+value+"'";
            }
            
            String sql = "update T_"+structure_id+" set " + colSet.substring(1) + " where id=" + orderId;
            logger.debug(sql);
            Db.update(sql);
        }
        
        List<Map<String, ?>> table_list = (ArrayList<Map<String, ?>>)dto.get("table_list");
        for (Map<String, ?> tableMap : table_list) {//获取每一个从表
            String structure_id = tableMap.get("structure_id").toString();
            
            List<Map> rowFieldsList = (ArrayList<Map>)tableMap.get("row_list");
            
            //先处理删除
            tableRowDelete(orderId, structure_id, rowFieldsList);
           
            for (Map<String, String> rowMap : rowFieldsList) {//表中每一行, update or insert
                String rowId = rowMap.get("id").toString();
                if(StringUtils.isEmpty(rowId)){
                    tableRowInsert(orderId, structure_id, rowMap);
                }else{
                    tableRowUpdate(structure_id, rowMap, rowId);
                }
            }
            
            
        }
    }

    private static void tableRowDelete(String orderId, String structure_id,
            List<Map> rowFieldsList) {
        List<String> idList = new ArrayList<String>();
        for (Map<String, String> rowMap : rowFieldsList) {
            String rowId = rowMap.get("id").toString();
            if(StringUtils.isNotEmpty(rowId)){
                idList.add(rowId);
            }
        }
        
        String deleteSql = "delete from T_" + structure_id + " where parent_id=" + orderId + " and id not in (" + StringUtils.join(idList, ", ") + ")";
        logger.debug(deleteSql);
        Db.update(deleteSql);
    }

    private static void tableRowUpdate(String structure_id,
            Map<String, String> rowMap, String rowId) {
        String colSet = "";
        for (Entry<String, String> entry: rowMap.entrySet()) {//行的每个字段
            String key = entry.getKey();
            String value = entry.getValue();
            if("id".equals(key)){
                continue;
            }
            colSet += ","+key + "='"+value+"'";
        }
        String sql = "update T_" + structure_id + " set " + colSet.substring(1) + " where id=" +rowId;
        logger.debug(sql);
        Db.update(sql);
    }

    private static void tableRowInsert(String orderId, String structure_id,
            Map<String, String> rowMap) {
        String colName = "parent_id";
        String colValue = orderId;
        
        for (Entry<String, String> entry: rowMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if("id".equals(key)){
                continue;
            }
            colName += ","+key;
            colValue+= ",'"+value+"'";
        }
        
        String sql = "insert into T_"+structure_id+"("+colName+") values("+colValue+")";
        logger.debug(sql);
        Db.update(sql);
    }
    
    public static String commonInsert(Map<String, ?> dto) {
        BigInteger order_id = new BigInteger("0");
        List<Map<String, String>> fields_list = (ArrayList<Map<String, String>>)dto.get("fields_list");
        for (Map<String, String> tableMap : fields_list) {//获取每一行
            String tableName = tableMap.get("structure_id");
            
            String colName = "parent_id";
            String colValue = "null";
            for (Entry<String, String> entry: tableMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if("id".equals(key) || key.endsWith("_INPUT") || "structure_id".equals(key)){
                    continue;
                }
                colName += ","+key;
                colValue+= ",'"+value+"'";
            }
            
            String sql = "insert into T_"+tableName+"("+colName+") values("+colValue+")";
            logger.debug(sql);
            Db.update(sql);
            Record idRec = Db.findFirst("select LAST_INSERT_ID() id");
            order_id = idRec.getBigInteger("id");
            logger.debug(tableName+" last insert order_id = "+order_id);
        }
        List<Map<String, ?>> table_list = (ArrayList<Map<String, ?>>)dto.get("table_list");
        for (Map<String, ?> tableMap : table_list) {//获取每一行
            String tableName = tableMap.get("structure_id").toString();
            
            List<Map> rowFieldsList = (ArrayList<Map>)tableMap.get("row_list");
            for (Map<String, String> rowMap : rowFieldsList) {
                String colName = "parent_id";
                String colValue = order_id.toString();//TODO:  get structure_parent_id from ??
                for (Entry<String, String> entry: rowMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if("id".equals(key) || key.endsWith("_INPUT") || "structure_id".equals(key)){
                        continue;
                    }
                    colName += ","+key;
                    colValue+= ",'"+value+"'";
                }
                String sql = "insert into T_"+tableName+"("+colName+") values("+colValue+")";
                logger.debug(sql);
                Db.update(sql);
            }
            
        }
        return order_id.toString();
    }
}
