package controllers.yh.order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.profile.Contact;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.OrderNoUtil;
public class TransferOrderExeclHandeln extends TransferOrderController{
	
	//校验execl标题是否与默认标题一致
    public boolean checkoutExeclTitle(String[] title,String execlType){
    	int num = 0;
    	List<Record> titleList = Db.find("select execl_title from execl_title where execl_type = '"+ execlType +"';");
    	if(titleList != null){
    		System.out.println();
    		for (Record record : titleList) {
				if(record.get("execl_title").equals(title[num])){
					num++;
				}
			}
    	}
    	if(num >= titleList.size()){
    		return true;
    	}else{
    		return false;
    	}
    }
    //导入数据
    public Map<String,String> importTransferOrder(List<Map<String,String>> content){
    	Map<String, String> importResult = new HashMap<String, String>();
    	try{
        	String name = (String) currentUser.getPrincipal();
    		UserLogin user = UserLogin.dao
    				.findFirst("select * from user_login where user_name='" + name + "'");
        	
    		SimpleDateFormat dbDataFormat = new SimpleDateFormat("yyyy-MM-dd");
    		
    		int resultNum = 0;
    		int causeRow = 0;
    		String title = "";
    		String because = "数据不能为空";
    		List<Integer> executeNum = new ArrayList<Integer>();
    		for (int j = 0; j < content.size(); j++) {
    			Record tansferOrder = Db.findFirst("select * from transfer_order where customer_order_no = '" + content.get(j).get("运输单号").trim() + "';");
    			if(tansferOrder == null){
    				executeNum.add(j);
    			}
    		}
        	for (int j = 0; j < content.size(); j++) {
        		for (int i = 0; i < executeNum.size(); i++) {
					if(j == i){
						String customerOrderNo = content.get(j).get("运输单号").trim();
		        		causeRow = j+2;
		        		if("".equals(customerOrderNo)){
		        			title = "运输单号";
		        			break;
		        		}
		        		String customerName = content.get(j).get("客户名称(简称)").trim();
		        		if("".equals(customerName)){
		        			title = "客户名称";
		        			break;
		        		}
		        		String planning = content.get(j).get("计划日期");
		        		if("".equals(planning)){
		        			title = "计划日期";
		        			break;
		        		}
		        		String arrivl = content.get(j).get("预计到货日期");
		        		if("".equals(arrivl)){
		        			title = "预计到货日期";
		        			break;
		        		}
		        		String operationType = content.get(j).get("运营方式");
		        		if("".equals(operationType)){
		        			title = "运营方式";
		        			break;
		        		}
		        		String arrivalMode = content.get(j).get("到达方式");
		        		if("".equals(arrivalMode)){
		        			title = "到达方式";
		        			break;
		        		}
		        		because = "数据有误";
		        		//货品属性
		        		Record product = null;
		        		if(!"".equals(content.get(j).get("货品型号"))){
			        		product = Db.findFirst("select * from product where item_no = '"+content.get(j).get("货品型号")+"';");
			    			if(product == null){
			    				title = "货品型号";
			    				break;
			    			}
		        		}
		    			//仓库
		        		Record warehouse = null;
		        		if(!"".equals(content.get(j).get("中转仓"))){
			    			warehouse = Db.findFirst("select id from warehouse where warehouse_name = '" + content.get(j).get("中转仓") + "';");
			    			if(warehouse == null){
			    				title = "中转仓";
			    				break;
			    			}
		        		}
		    			//始发城市
		        		Record location1 = null;
		        		if(!"".equals(content.get(j).get("始发城市"))){
			    			location1 = Db.findFirst("select code from location where name = '" +content.get(j).get("始发城市") +"';");
			    			if(location1 == null){
			    				title = "始发城市";
			    				break;
			    			}
		        		}
		    			//目的地城市
		        		Record location2 = null;
		        		if(!"".equals(content.get(j).get("到达城市"))){
			    			location2 = Db.findFirst("select code from location where name = '" +content.get(j).get("到达城市") +"';");
			    			if(location2 == null){
			    				title = "到达城市";
			    				break;
			    			}
		        		}
		    			//客户名称
		        		Record customer = null;
		        		if(!"".equals(content.get(j).get("客户名称(简称)"))){
			    			String sql = "select p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type ='" + Party.PARTY_TYPE_CUSTOMER+ "' and c.abbr ='" + content.get(j).get("客户名称(简称)") + "';";
			    			customer = Db.findFirst(sql);
			    			if(customer == null){
			    				title = "客户名称(简称)";
			    				break;
			    			}
		        		}
		    			//供应商名称
		        		Record provider = null;
		        		if(!"".equals(content.get(j).get("供应商名称(简称)"))){
			    			provider = Db.findFirst("select p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type ='" + Party.PARTY_TYPE_SERVICE_PROVIDER+ "' and c.abbr ='" + content.get(j).get("供应商名称(简称)") + "';");
			    			if(provider == null){
			    				title = "供应商名称(简称)";
			    				break;
			    			}
		        		}
		    			//网点
		        		Record office = null;
		        		if(!"".equals(content.get(j).get("网点"))){
			    			office = Db.findFirst("select id from office where office_name = '" + content.get(j).get("网点") + "';");
			    			if(office == null){
			    				title = "网点";
			    				break;
			    			}
		        		}
		        		TransferOrder tansferOrder = TransferOrder.dao.findFirst("select * from transfer_order where customer_order_no = '" + customerOrderNo + "';");
		    			if(tansferOrder != null){
		    				TransferOrderItem tansferOrderItem= TransferOrderItem.dao.findFirst("select * from transfer_order_item where order_id = '" + tansferOrder.get("id") +"' and item_no = '" + product.get("item_no") + "';");
		    				if(tansferOrderItem != null){
		    					//本来这里是要修改货品明细表中amount（数量），
		    					//由于execl中“发货数量”列是货品总数，所以不用修改
		    					
		    					TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao.findFirst("select * from transfer_order_item_detail where order_id = '" + tansferOrder.get("id") +"' and item_id = '" + tansferOrderItem.get("id") +"' and serial_no = '" + content.get(j).get("单品序列号") + "';");
		    					if(transferOrderItemDetail != null){
		    						int num = transferOrderItemDetail.getInt("pieces") + Integer.parseInt(content.get(j).get("单品件数"));
		    						transferOrderItemDetail.set("pieces", num).update();
		    					}else{
		    						//创建保存单品货品明细
		    						TransferOrderItemDetail itemDatail = new TransferOrderItemDetail();
		    						itemDatail.set("order_id", tansferOrder.get("id"))
		    						.set("item_id", tansferOrderItem.get("id"))
		    						.set("item_no", product.get("item_no"))
		    						.set("serial_no", content.get(j).get("单品序列号"))
		    						.set("item_name", product.get("item_name"))
		    						.set("volume", product.get("volume"))
		    						.set("weight", product.get("weight"))
		    						.set("pieces", content.get(j).get("单品件数")) 
		    						.set("notify_party_company", content.get(j).get("单品收货地址"))//收货地址
			    					.set("notify_party_name", content.get(j).get("单品收货人"))//收货人
			    					.set("notify_party_phone", content.get(j).get("单品收货人联系电话"))//收货人电话
			    					.set("sales_order_no", content.get(j).get("单品销售单号"))//销售单号
			    					.set("responsible_person", content.get(j).get("责任人"))//责任人
			    					.set("business_manager", content.get(j).get("业务经理"))//业务经理
			    					.set("station_name", content.get(j).get("服务站名称"))//服务站名称
			    					.set("service_telephone", content.get(j).get("服务站电话"))//服务站电话
		    						.save();
		    					}
		    				}else{
		    					//创建保存货品明细
		    					TransferOrderItem item =new TransferOrderItem();
		    					//体积相加
		    					double size = product.getDouble("size")/1000;
			    				double width = product.getDouble("width")/1000;
			    				double height = product.getDouble("height")/1000;
			    				double sumVolume = size * width * height * Double.parseDouble(content.get(j).get("发货数量"));
		    					double num = product.getDouble("weight") * Double.parseDouble(content.get(j).get("发货数量"));
		    					item.set("order_id", tansferOrder.get("id"))
		    					.set("amount", content.get(j).get("发货数量"))
		    					.set("item_no", product.get("item_no"))
		    					.set("item_name", product.get("item_name"))
		    					.set("size", product.get("size"))
		    					.set("unit", product.get("unit"))
		    					.set("width", product.get("width"))
		    					.set("height", product.get("height"))
		    					.set("volume", sumVolume)
		    					.set("weight", product.getDouble("weight"))
		    					.set("sum_weight", num)
		    					.set("product_id", product.get("id"))
		    					.save();
		    					//创建保存单品货品明细
		    					TransferOrderItemDetail itemDatail = new TransferOrderItemDetail();
		    					itemDatail.set("order_id", tansferOrder.get("id"))
		    					.set("item_id", item.get("id"))
		    					.set("item_no", product.get("item_no"))
		    					.set("serial_no", content.get(j).get("单品序列号"))
		    					.set("item_name", product.get("item_name"))
		    					.set("volume", product.get("volume"))
		    					.set("weight", product.get("weight"))
		    					.set("pieces", content.get(j).get("单品件数")) 
		    					.set("notify_party_company", content.get(j).get("单品收货地址"))//收货地址
		    					.set("notify_party_name", content.get(j).get("单品收货人"))//收货人
		    					.set("notify_party_phone", content.get(j).get("单品收货人联系电话"))//收货人电话
		    					.set("sales_order_no", content.get(j).get("单品销售单号"))//销售单号
		    					.set("responsible_person", content.get(j).get("责任人"))//责任人
		    					.set("business_manager", content.get(j).get("业务经理"))//业务经理
		    					.set("station_name", content.get(j).get("服务站名称"))//服务站名称
		    					.set("service_telephone", content.get(j).get("服务站电话"))//服务站电话
		    					.save();
		    				}
		    			}else{
		    				String sql = "select * from transfer_order order by id desc limit 0,1";
		    	    		String orderNo = OrderNoUtil.getOrderNo(sql, "YS");
		    				//生成运输单数量
		    				++resultNum;
		    				//创建保存运输单
		    				Date planningTime = dbDataFormat.parse(planning);
		    				Date arrivalTime = dbDataFormat.parse(arrivl);
		    				TransferOrder transferOrder = new TransferOrder();
		    				transferOrder.set("order_no", orderNo)
		    				.set("order_type", "salesOrder")//订单类型：默认为销售订单
		    				.set("charge_type", "perUnit")//客户计费方式：默认计件
		    				.set("charge_type2", "perUnit")//供应商计费方式：默认计件
		    				.set("pickup_assign_status",TransferOrder.ASSIGN_STATUS_NEW)
		    				.set("depart_assign_status",TransferOrder.ASSIGN_STATUS_NEW)
		    				.set("status", "新建")
		    				.set("create_by", user.get("id"))//创建人id
		    				.set("create_stamp", new Date())//创建时间
		    				.set("planning_time", planningTime)//计划时间
		    				.set("arrival_time", arrivalTime)//预计到货时间
		    				.set("customer_order_no", customerOrderNo);//客户订单号
		    				
		    				//运营方式
		    				if("外包".equals(content.get(j).get("运营方式"))){
		    					transferOrder.set("operation_type", "out_source");
		    				}else{
		    					transferOrder.set("operation_type", "own");
		    				}
		    				//取货地址、收货单位
		    				transferOrder.set("address", content.get(j).get("始发城市"))
	    					.set("receiving_unit", content.get(j).get("收货单位"));
		    				
		    				//到达方式
		    				if("入中转仓".equals(content.get(j).get("到达方式"))){
		    					//入中转仓
		    					transferOrder.set("arrival_mode", "gateIn")
		    					.set("warehouse_id", warehouse.get("id"));
		    				}else{
		    					//货品直送
		    					transferOrder.set("arrival_mode", "delivery");
		    					// 保存联系人
		    					Contact contact = new Contact();
		    					contact.set("address", content.get(j).get("单品收货地址"));//收货地址
		    					contact.set("contact_person", content.get(j).get("单品收货人"));//收货人
		    					contact.set("phone", content.get(j).get("单品收货人联系电话"));//收货人电话
		    					contact.save();
		    					// 保存收货人
		    					Party party = new Party();
		    					party.set("contact_id", contact.getLong("id"));
		    					party.set("create_date", new Date());
		    					party.set("creator", currentUser.getPrincipal());
		    					party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
		    					party.save();
		    					//收货人
			    				transferOrder.set("notify_party_id", party.get("id"));
		    				}
		    				//始发城市
		    				transferOrder.set("route_from",location1.get("code"));
		    				//目的地城市
		    				transferOrder.set("route_to",location2.get("code"));
		    				//客户名称
		    				transferOrder.set("customer_id",customer.get("pid"));
		    				//供应商名称
		    				transferOrder.set("sp_id", provider.get("pid"));
		    				//网点
		    				transferOrder.set("office_id", office.get("id"));
		    				//货品属性
							if(content.get(j).get("货品属性").equals("ATM"))
								transferOrder.set("cargo_nature","ATM");
							else
								transferOrder.set("cargo_nature","cargo");
		    				//是否有单品
		    				if(!"".equals(content.get(j).get("单品序列号"))){
		    					transferOrder.set("cargo_nature_detail","cargoNatureDetailYes");
		    				}else{
		    					transferOrder.set("cargo_nature_detail","cargoNatureDetailNo");
		    				}
		    				transferOrder.save();
		    				//保存运输里程碑
		    				TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
		    				transferOrderMilestone.set("status", "新建");
		    				transferOrderMilestone.set("create_by", user.get("id"));
		    				transferOrderMilestone.set("location", "");
		    				java.util.Date utilDate = new java.util.Date();
		    				java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		    				transferOrderMilestone.set("create_stamp", sqlDate);
		    				transferOrderMilestone.set("type",
		    						TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
		    				transferOrderMilestone.set("order_id", transferOrder.get("id"));
		    				transferOrderMilestone.save();
		    				//创建保存货品明细
		    				TransferOrderItem item =new TransferOrderItem();
		    				double num = product.getDouble("weight") * Double.parseDouble(content.get(j).get("发货数量"));
		    				double size = product.getDouble("size")/1000;
		    				double width = product.getDouble("width")/1000;
		    				double height = product.getDouble("height")/1000;
		    				double sumVolume = size * width * height * Double.parseDouble(content.get(j).get("发货数量"));
		    				item.set("order_id", transferOrder.get("id"))
		    				.set("amount", content.get(j).get("发货数量"))
		    				.set("item_no", product.get("item_no"))
		    				.set("item_name", product.get("item_name"))
		    				.set("size", product.get("size"))
		    				.set("unit", product.get("unit"))
		    				.set("width", product.get("width"))
		    				.set("height", product.get("height"))
		    				.set("volume", sumVolume)
		    				.set("weight", product.getDouble("weight"))
		    				.set("sum_weight", num)
		    				.set("product_id", product.get("id"))
		    				.save();
		    				//创建保存单品货品明细
		    				if(!"".equals(content.get(j).get("单品序列号"))){
		    					TransferOrderItemDetail itemDatail = new TransferOrderItemDetail();
		    					itemDatail.set("order_id", transferOrder.get("id"))
		    					.set("item_id", item.get("id"))
		    					.set("item_no", product.get("item_no"))
		    					.set("serial_no", content.get(j).get("单品序列号"))
		    					.set("item_name", product.get("item_name"))
		    					.set("volume", product.get("volume"))
		    					.set("weight", product.get("weight"))
		    					//.set("notify_party_id", customer.get("id"))
		    					.set("pieces", content.get(j).get("单品件数"))
		    					.set("notify_party_company", content.get(j).get("单品收货地址"))//收货地址
		    					.set("notify_party_name", content.get(j).get("单品收货人"))//收货人
		    					.set("notify_party_phone", content.get(j).get("单品收货人联系电话"))//收货人电话
		    					.set("sales_order_no", content.get(j).get("单品销售单号"))//销售单号
		    					.set("responsible_person", content.get(j).get("责任人"))//责任人
		    					.set("business_manager", content.get(j).get("业务经理"))//业务经理
		    					.set("station_name", content.get(j).get("服务站名称"))//服务站名称
		    					.set("service_telephone", content.get(j).get("服务站电话"))//服务站电话
		    					.save();
		    				}
		    			}
		        	}
				}
        		if(!"".equals(title)){
        			break;
        		}
			}
        	if("".equals(title)){
        		importResult.put("result","true");
        		if(resultNum == 0){
        			importResult.put("cause", "不能导入重复运输单");
        		}else{
        			importResult.put("cause", "成功导入" + resultNum + "张运输单");
        		}
        	}else{ 
        		importResult.put("result","false");
        		importResult.put("cause", "成功导入至第" + (causeRow-1) + "行,因第【" + causeRow + "】行【" + title + "】列" + because);
        	}
        	
    	} catch (ParseException e) {
			e.printStackTrace();
		} 
    	return importResult;  
    }
	
}
