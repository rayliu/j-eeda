package controllers.yh.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

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
        String order_id = dto.get("order_id").toString();
        
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

    private static Record buildFieldRec(String order_id, Map<String, ?> structure) {
        int structureId = ((Double)structure.get("ID")).intValue();
        Record orderRec;
       
        if(structure.get("PARENT_ID") == null){
            String tableName = "T_" + structureId;
            orderRec = Db.findById(tableName, order_id);
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
            String tableName = tableMap.get("id");
            String colSet = "";
            for (Entry<String, String> entry: tableMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if("id".equals(key)){
                    continue;
                }
                colSet += ","+key + "='"+value+"'";
            }
            
            String sql = "update "+tableName+" set " + colSet.substring(1) + " where id=" + orderId;
            logger.debug(sql);
            Db.update(sql);
        }
        
        List<Map<String, ?>> table_list = (ArrayList<Map<String, ?>>)dto.get("table_list");
        for (Map<String, ?> tableMap : table_list) {//获取每一个从表
            String tableName = tableMap.get("id").toString();
            
            String colSet = "";
            List<Map> rowFieldsList = (ArrayList<Map>)tableMap.get("row_list");
            for (Map<String, String> rowMap : rowFieldsList) {//表中每一行
                String rowId = rowMap.get("id").toString();
                for (Entry<String, String> entry: rowMap.entrySet()) {//行的每个字段
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if("id".equals(key)){
                        continue;
                    }
                    colSet += ","+key + "='"+value+"'";
                }
                String sql = "update " + tableName + colSet + " where id=" +rowId;
                logger.debug(sql);
                Db.update(sql);
            }
        }
    }
    
    public static String commonInsert(Map<String, ?> dto) {
        BigInteger order_id = new BigInteger("0");
        List<Map<String, String>> fields_list = (ArrayList<Map<String, String>>)dto.get("fields_list");
        for (Map<String, String> tableMap : fields_list) {//获取每一行
            String tableName = tableMap.get("id");
            
            String colName = "parent_id";
            String colValue = "null";
            for (Entry<String, String> entry: tableMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if("id".equals(key)){
                    continue;
                }
                colName += ","+key;
                colValue+= ",'"+value+"'";
            }
            
            String sql = "insert into "+tableName+"("+colName+") values("+colValue+")";
            logger.debug(sql);
            Db.update(sql);
            Record idRec = Db.findFirst("select LAST_INSERT_ID() id");
            order_id = idRec.getBigInteger("id");
            logger.debug(tableName+" last insert order_id = "+order_id);
        }
        List<Map<String, ?>> table_list = (ArrayList<Map<String, ?>>)dto.get("table_list");
        for (Map<String, ?> tableMap : table_list) {//获取每一行
            String tableName = tableMap.get("id").toString();
            
            String colName = "parent_id";
            String colValue = order_id.toString();
            List<Map> rowFieldsList = (ArrayList<Map>)tableMap.get("row_list");
            for (Map<String, String> rowMap : rowFieldsList) {
                for (Entry<String, String> entry: rowMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if("id".equals(key)){
                        continue;
                    }
                    colName += ","+key;
                    colValue+= ",'"+value+"'";
                }
            }
            String sql = "insert into "+tableName+"("+colName+") values("+colValue+")";
            logger.debug(sql);
            Db.update(sql);
        }
        return order_id.toString();
    }
}
