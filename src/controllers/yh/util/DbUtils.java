package controllers.yh.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Model;

import controllers.yh.damageOrder.DamageOrderController;
import models.Location;

public class DbUtils {
	private static Logger logger = Logger.getLogger(DbUtils.class);
	
	public static String buildConditions(Map<String, String[]> paraMap) {
		String condition = "";
		Map<String, Map<String, String>> dateFieldMap = new HashMap<String, Map<String, String>>();

        for (Entry<String, String[]> entry : paraMap.entrySet()) {
            String key = entry.getKey();
            String filterValue = entry.getValue()[0];
            
            if(StringUtils.isNotEmpty(filterValue) && !"undefined".equals(filterValue)){
            	logger.debug(key + ":" + filterValue);
            	if(key.endsWith("_no") || key.endsWith("_name")){
            		condition += " and " + key + " like '%" + filterValue + "%' ";
            		continue;
            	}else if(key.endsWith("_id") || key.endsWith("_status")){
            		condition += " and " + key + " = '" + filterValue + "' ";
            		continue;
            	}else if(key.endsWith("_begin_time")){
            		key = key.replaceAll("_begin_time", "");
            		Map<String, String> valueMap = dateFieldMap.get(key)==null?new HashMap<String, String>():dateFieldMap.get(key);
            		valueMap.put("_begin_time", filterValue);
            		dateFieldMap.put(key, valueMap);
            		continue;
            	}else if(key.endsWith("_end_time")){
            		key = key.replaceAll("_end_time", "");
            		Map<String, String> valueMap = dateFieldMap.get(key)==null?new HashMap<String, String>():dateFieldMap.get(key);
            		valueMap.put("_end_time", filterValue);
            		dateFieldMap.put(key, valueMap);
            		continue;
            	}
            }	
        }
        
        //处理日期
        for (Entry<String, Map<String, String>> entry : dateFieldMap.entrySet()) {
        	String beginTime = "1970-1-1";
        	String endTime = "2037-12-31";
        	
        	String key = entry.getKey();
        	
        	Map<String, String> valueMap = entry.getValue();
        	for (Entry<String,String> valueEntry : valueMap.entrySet()) {
        		String subKey = valueEntry.getKey();
        		if(subKey.equals("_begin_time")){
        			beginTime = valueEntry.getValue();
            		continue;
            	}else if(subKey.equals("_end_time")){
            		endTime = valueEntry.getValue();
            		continue;
            	}
			}
        	condition += " and " + key + " between '" + beginTime + "' and '" + endTime+ " 23:59:59' ";
        }
        return condition;
	}
	
	public static void handleList(List<Map<String, String>> itemList,
	        String master_order_id, Class<?> clazz,
	        String master_col_name) 
			throws InstantiationException, IllegalAccessException {
    	for (Map<String, String> rowMap : itemList) {//获取每一行
    		Model<?> model = (Model<?>) clazz.newInstance();
    		
    		String rowId = rowMap.get("id");
    		String action = rowMap.get("action");
    		if(StringUtils.isEmpty(rowId)){//创建
    			if(rowMap.get("status")==null){                        //货损明细
        			if(rowMap.get("product_no")!=null 
        					|| rowMap.get("serial_no")!=null 
        					|| rowMap.get("amount")!=null  
        					|| rowMap.get("remark")!=null
        			){
        				if(!rowMap.get("product_no").equals("") 
    	    					|| !rowMap.get("serial_no").equals("") 
    	    					|| !rowMap.get("amount").equals("")  
    	    					|| !rowMap.get("remark").equals("") 
    	    			){
        					setModelValues(rowMap, model);
                			model.set(master_col_name, master_order_id);
                			model.save();
        				}
        			}
    			}else{                                                //赔偿赔款明细
        			if(!rowMap.get("status").equals("")){
        				if(!rowMap.get("fin_item").equals("") 
    	    					|| !rowMap.get("amount").equals("") 
    	    					|| !rowMap.get("party_name").equals("")  
    	    					|| !rowMap.get("remark").equals("") 
    	    			){
        					setModelValues(rowMap, model);
                			model.set(master_col_name, master_order_id);
                			model.save();
        				}
        			}
        		}
    			
    		}else if("DELETE".equals(action)){//delete
    			Model<?> deleteModel = model.findById(rowId);
    			deleteModel.delete();
    		}else{//UPDATE
    			Model<?> updateModel = model.findById(rowId);
    			setModelValues(rowMap, updateModel);
    			updateModel.update();
    		}
		}
	}
	
	
	public static void handleList(List<Map<String, String>> itemList,
            Class<?> clazz,
            Map<String, String> master_ref_col) 
            throws InstantiationException, IllegalAccessException {
        for (Map<String, String> rowMap : itemList) {//获取每一行
            Model<?> model = (Model<?>) clazz.newInstance();
            
            String rowId = rowMap.get("id");
            String action = rowMap.get("action");
            if(StringUtils.isEmpty(rowId)){//创建
                model.save();//先创建，获取id以方便创建从表时获取上级 id
                setModelValues(rowMap, model);
                for (String col_name : master_ref_col.keySet()) {
                    model.set(col_name, master_ref_col.get(col_name));
                }
                
                model.update();
            }else if("DELETE".equals(action)){//delete
                Model<?> deleteModel = model.findById(rowId);
                deleteModel.delete();
            }else{//UPDATE
                Model<?> updateModel = model.findById(rowId);
                setModelValues(rowMap, updateModel);
                updateModel.update();
            }
        }
    }

    //遇到 _list 是从表Map, 递归处理， model = ?_list
	public static void setModelValues(Map<String, ?> dto, Model<?> model) {
	    String modelName = model.getClass().getSimpleName();
		logger.debug("----Model:"+modelName);
		for (Entry<String, ?> entry : dto.entrySet()) { 
			String key = entry.getKey();
			if(!key.endsWith("_list")){
            	String value = String.valueOf(entry.getValue()) ;
            	//忽略  action 字段
            	if(StringUtils.isNotEmpty(value) && !"action".equals(key)){
            		logger.debug(key+":"+value);
            		try {
                        model.set(key, value);
                        if("Field".equals(modelName) && "field_display_name".equals(key)){//对field 的特殊处理
                            model.set("field_name", PingYinUtil.getFirstSpell(value).toUpperCase());
                            //需判断当前表是否有相同字段名
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
            	}
            }else{
                String modelClassName = key.substring(0, key.indexOf("_list"));
                try {
                    List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get(key);
                    Class c = Class.forName("models.yh.structure."+StringUtils.capitalize(modelClassName));
                   
                    Map<String, String> master_ref= new HashMap<String, String>();
                    master_ref.put("structure_id", model.get("id").toString());
                    handleList(itemList, c, master_ref);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
		}
	}
}
