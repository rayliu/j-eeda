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

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
public class TransferOrderExeclHandeln extends TransferOrderController{
	
	
	//校验execl标题是否与默认标题一致
    public boolean checkoutExeclTitle(String[] title,String execlType){
    	int num = 0;
    	List<Record> titleList = Db.find("select execl_title from execl_title where execl_type = '"+ execlType +"';");
    	if(titleList != null){
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
    		List<UserLogin> users = UserLogin.dao
    				.find("select * from user_login where user_name='" + name + "'");
        	
        	//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    		SimpleDateFormat dbDataFormat = new SimpleDateFormat("yyyy-MM-dd");
    		/*String format = sdf.format(new Date());
    		String time = format + "00001";
    		Long newTime = Long.parseLong(time);*/
    		
    		int resultNum = 0;
    		int causeRow = 0;
    		String title = "";
    		String because = "数据不能为空";
    		List<Integer> executeNum = new ArrayList<Integer>();
    		for (int j = 0; j < content.size(); j++) {
    			Record tansferOrder = Db.findFirst("select * from transfer_order where customer_order_no = '" + content.get(j).get("运输单").trim() + "';");
    			if(tansferOrder == null){
    				executeNum.add(j);
    			}
    		}
        	for (int j = 0; j < content.size(); j++) {
        		for (int exeNum : executeNum) {
					if(j == exeNum){
						String customerOrderNo = content.get(j).get("运输单").trim();
		        		causeRow = j+2;
		        		if("".equals(customerOrderNo)){
		        			title = "运输单";
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
		        		because = "数据有误";
		        		//货品属性
		        		Record product = null;
		        		if(!"".equals(content.get(j).get("产品型号"))){
			        		product = Db.findFirst("select * from product where item_no = '"+content.get(j).get("产品型号")+"';");
			    			if(product == null){
			    				title = "产品型号";
			    				break;
			    			}
		        		}
		    			//仓库
		        		Record warehouse = null;
		        		if(!"".equals(content.get(j).get("仓储地点"))){
			    			warehouse = Db.findFirst("select id from warehouse where warehouse_name = '" + content.get(j).get("仓储地点") + "';");
			    			if(warehouse == null){
			    				title = "仓储地点";
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
			    				title = "客户名称";
			    				break;
			    			}
		        		}
		    			//供应商名称
		        		Record provider = null;
		        		if(!"".equals(content.get(j).get("供应商名称(简称)"))){
			    			provider = Db.findFirst("select p.id from party p,contact c where p.contact_id = c.id and p.party_type ='" + Party.PARTY_TYPE_SERVICE_PROVIDER+ "' and c.abbr ='" + content.get(j).get("供应商名称(简称)") + "';");
			    			if(provider == null){
			    				title = "供应商名称";
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
		    				TransferOrderItem tansferOrderItem= TransferOrderItem.dao.findFirst("select * from transfer_order_item where order_id = '" + tansferOrder.get("id") +"' and item_no = '" + content.get(j).get("产品型号") + "';");
		    				if(tansferOrderItem != null){
		    					//本来这里是要修改货品明细表中amount（数量），
		    					//由于execl中“发货数量”列是货品总数，所以不用修改
		    					//体积相加
		    					double sumVolume = tansferOrderItem.getDouble("volume") + (product.getDouble("volume")*Double.parseDouble(content.get(j).get("件数")));
		    					tansferOrderItem.set("volume", sumVolume).update();
		    					
		    					TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao.findFirst("select * from transfer_order_item_detail where order_id = '" + tansferOrder.get("id") +"' and item_id = '" + tansferOrderItem.get("id") +"' and serial_no = '" + content.get(j).get("序列号") + "';");
		    					if(transferOrderItemDetail != null){
		    						int num = transferOrderItemDetail.getInt("pieces") + Integer.parseInt(content.get(j).get("件数"));
		    						transferOrderItemDetail.set("pieces", num).update();
		    					}else{
		    						
		    						//创建保存单品货品明细
		    						TransferOrderItemDetail itemDatail = new TransferOrderItemDetail();
		    						itemDatail.set("order_id", tansferOrder.get("id"))
		    						.set("item_id", tansferOrderItem.get("id"))
		    						.set("item_no", product.get("item_no"))
		    						.set("serial_no", content.get(j).get("序列号"))
		    						.set("item_name", product.get("item_name"))
		    						.set("volume", product.get("volume"))
		    						.set("weight", product.get("weight"))
		    						//.set("notify_party_id", customer.get("id"))
		    						.set("pieces", content.get(j).get("件数")) 
		    						.set("notify_party_company", content.get(j).get("收货单位"))//收货单位
			    					.set("notify_party_name", content.get(j).get("收货人"))//收货人
			    					.set("notify_party_phone", content.get(j).get("收货人联系电话"))//收货人电话
		    						.save();
		    					}
		    				}else{
		    					//创建保存货品明细
		    					TransferOrderItem item =new TransferOrderItem();
		    					//体积相加
		    					double sumVolume = product.getDouble("volume")*Double.parseDouble(content.get(j).get("件数"));
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
		    					.set("serial_no", content.get(j).get("序列号"))
		    					.set("item_name", product.get("item_name"))
		    					.set("volume", product.get("volume"))
		    					.set("weight", product.get("weight"))
		    					//.set("notify_party_id", customer.get("id"))
		    					.set("pieces", content.get(j).get("件数")) 
		    					.set("notify_party_company", content.get(j).get("收货单位"))//收货单位
		    					.set("notify_party_name", content.get(j).get("收货人"))//收货人
		    					.set("notify_party_phone", content.get(j).get("收货人联系电话"))//收货人电话
		    					.save();
		    				}
		    			}else{
		    				String order_no = "";
		    	    		TransferOrder order = TransferOrder.dao
		    	    				.findFirst("select * from transfer_order order by order_no desc limit 0,1");
		    	    		if (order != null) {
		    	    			String num = order.get("order_no");
		    	    			String str = num.substring(2, num.length());
		    	    			System.out.println(str);
		    	    			Long oldTime = Long.parseLong(str);
		    	    			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		    	    			String format = sdf.format(new Date());
		    	    			String time = format + "00001";
		    	    			Long newTime = Long.parseLong(time);
		    	    			if (oldTime >= newTime) {
		    	    				order_no = String.valueOf((oldTime + 1));
		    	    			} else {
		    	    				order_no = String.valueOf(newTime);
		    	    			}
		    	    		} else {
		    	    			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		    	    			String format = sdf.format(new Date());
		    	    			order_no = format + "00001";
		    	    		}
		    				
		    				//生成运输单数量
		    				++resultNum;
		    				//创建保存运输单
		    				Date planningTime = dbDataFormat.parse(planning);
		    				Date arrivalTime = dbDataFormat.parse(arrivl);
		    				TransferOrder transferOrder = new TransferOrder();
		    				transferOrder.set("order_no", "YS" + order_no)
		    				.set("order_type", "salesOrder")//订单类型：默认为销售订单
		    				.set("operation_type", "own")//运营方式：默认自营
		    				.set("charge_type", "perUnit")//客户计费方式：默认计件
		    				.set("charge_type2", "perUnit")//供应商计费方式：默认计件
		    				.set("pickup_assign_status",TransferOrder.ASSIGN_STATUS_NEW)
		    				.set("depart_assign_status",TransferOrder.ASSIGN_STATUS_NEW)
		    				.set("status", "新建")
		    				.set("create_by", users.get(0).get("id"))//创建人id
		    				.set("create_stamp", new Date())//创建时间
		    				.set("planning_time", planningTime)//计划时间
		    				.set("arrival_time", arrivalTime)//预计到货时间
		    				.set("customer_order_no", customerOrderNo);//客户订单号
		    				
		    				//到达方式
		    				if(!"".equals(content.get(j).get("仓储地点"))){
		    					transferOrder.set("arrival_mode", "gateIn")
		    					.set("warehouse_id", warehouse.get("id"));//仓库
		    				}else{
		    					transferOrder.set("arrival_mode", "delivery")
		    					.set("receiving_unit", content.get(j).get("收货单位"));//收货单位
		    				}
		    				//始发城市
		    				transferOrder.set("route_from",location1.get("code"));
		    				//目的地城市
		    				transferOrder.set("route_to",location2.get("code"));
		    				//客户名称
		    				transferOrder.set("customer_id",customer.get("pid"));
		    				//供应商名称
		    				transferOrder.set("sp_id", provider.get("id"));
		    				//网点
		    				transferOrder.set("office_id", office.get("id"));
		    				//货品属性
							if(product.get("item_name").equals("ATM"))
								transferOrder.set("cargo_nature","ATM");
							else
								transferOrder.set("cargo_nature","cargo");
		    				//是否有单品
		    				if(!"".equals(content.get(j).get("序列号"))){
		    					transferOrder.set("cargo_nature_detail","cargoNatureDetailYes");
		    				}else{
		    					transferOrder.set("cargo_nature_detail","cargoNatureDetailNo");
		    				}
		    				//收货人
		    				transferOrder.set("notify_party_id", customer.get("id"));
		    				transferOrder.save();
		    				//保存运输里程碑
		    				TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
		    				transferOrderMilestone.set("status", "新建");
		    				transferOrderMilestone.set("create_by", users.get(0).get("id"));
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
		    				item.set("order_id", transferOrder.get("id"))
		    				.set("amount", content.get(j).get("发货数量"))
		    				.set("item_no", product.get("item_no"))
		    				.set("item_name", product.get("item_name"))
		    				.set("size", product.get("size"))
		    				.set("unit", product.get("unit"))
		    				.set("width", product.get("width"))
		    				.set("height", product.get("height"))
		    				.set("volume", product.getDouble("volume")*Double.parseDouble(content.get(j).get("件数")))
		    				.set("weight", product.getDouble("weight"))
		    				.set("sum_weight", num)
		    				.set("product_id", product.get("id"))
		    				.save();
		    				//创建保存单品货品明细
		    				if(!"".equals(content.get(j).get("序列号"))){
		    					TransferOrderItemDetail itemDatail = new TransferOrderItemDetail();
		    					itemDatail.set("order_id", transferOrder.get("id"))
		    					.set("item_id", item.get("id"))
		    					.set("item_no", product.get("item_no"))
		    					.set("serial_no", content.get(j).get("序列号"))
		    					.set("item_name", product.get("item_name"))
		    					.set("volume", product.get("volume"))
		    					.set("weight", product.get("weight"))
		    					//.set("notify_party_id", customer.get("id"))
		    					.set("pieces", content.get(j).get("件数"))
		    					.set("notify_party_company", content.get(j).get("收货单位"))//收货单位
		    					.set("notify_party_name", content.get(j).get("收货人"))//收货人
		    					.set("notify_party_phone", content.get(j).get("收货人联系电话"))//收货人电话
		    					.save();
		    				}
		    			}
		        	}
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
        		importResult.put("cause", "成功导入至第" + (causeRow-1) + "行,因【" + title + "】列第【" + causeRow + "】行" + because);
        	}
        	
    	} catch (ParseException e) {
			e.printStackTrace();
		} 
    	return importResult;  
    }
	
}
