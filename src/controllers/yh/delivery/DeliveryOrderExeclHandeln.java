package controllers.yh.delivery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.Location;
import models.Party;
import models.TransferOrder;
import models.TransferOrderItemDetail;
import models.UserLogin;
import models.Warehouse;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
@Before(Tx.class)
public class DeliveryOrderExeclHandeln extends DeliveryController {
	
	String name = (String) currentUser.getPrincipal();
	UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
	
	/**
     * 校验execl标题是否与默认标题一致
     * @param title,execlType
     * @return 
     */
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
	
	/**
     * 检验数据：同一张单的数据是否有隔单（中间还存在其他单据数据）现象
     * @param content
     * @return 
     */
	private Map<String, String> validatingOrderNo(List<Map<String,String>> content){
		Map<String, String> importResult = new HashMap<String, String>();
		try {
			importResult.put("result","true");
			importResult.put("cause", "验证数据成功");
			List<String> orderNoList = new ArrayList<String>();
			Set<String> orderNoSet = new HashSet<String>();
			for (int j = 0; j < content.size(); j++) {
				orderNoList.add(content.get(j).get("配送单号"));
				orderNoSet.add(content.get(j).get("配送单号"));
			}
			Iterator orderNos = orderNoSet.iterator();//先迭代出来  
	        while(orderNos.hasNext()){//遍历  
	        	boolean flag = false; 
	        	String orderNo = orderNos.next().toString();
	        	int firstIndex = orderNoList.indexOf(orderNo);
	        	int lastIndex = orderNoList.lastIndexOf(orderNo);
	        	System.out.println("单号："+orderNo+",第一次出现位置："+firstIndex+",最后一次出现位置："+lastIndex);
	        	for (int i = firstIndex; i <= lastIndex; i++) {
					if(!orderNoList.get(i).equals(orderNo)){
						importResult.put("result","false");
						importResult.put("cause", "验证数据失败，同一张单号数据中存在其他单号数据现象，在第"+(i+2)+"行【配送单号】列");
						flag = true; 
						break;
					}
				}
	        	if(flag){
	        		break;
        		} 
	        }  
		} catch (Exception e) {
			importResult.put("result","false");
			importResult.put("cause", "验证数据出错，请重新整理文件数据后在导入");
		}
		return importResult;
	}
	
	/**
     * 检验数据：此方法已处理同一文件多次导入的可能
     * 	1.必填列
     * 	2.execl数据与系统数据需一致的列
     * 	3.execl内容有格式要求列
     * @param content
     * @return 
     */
	private Map<String,String> validatingData(List<Map<String,String>> content){
    	Map<String, String> importResult = new HashMap<String, String>();
		int causeRow = 0;
    	String title = "";
		String because = "数据不能为空";
		SimpleDateFormat dbDataFormat = new SimpleDateFormat("yyyy-MM-dd");
    	for (int j = 0; j < content.size(); j++) {
    		causeRow = j+2;
    		System.out.println("数据验证至第【"+causeRow+"】行");
    		if("".equals(content.get(j).get("配送单号").trim())){
    			title = "客户订单号";
    			break;
    		}else if("".equals(content.get(j).get("收货单位").trim())){
    			title = "收货单位";
    			break;
    		}else if("".equals(content.get(j).get("货品数量").trim())){
    			title = "货品数量";
    			break;
    		}else if("".equals(content.get(j).get("客户名称(简称)").trim())){
    			title = "客户名称(简称)";
    			break;
    		}else if("".equals(content.get(j).get("货品型号").trim())){
    			title = "货品型号";
    			break;
    		}else if("".equals(content.get(j).get("单品序列号").trim())){
    			title = "单品序列号";
    			break;
    		}else if("".equals(content.get(j).get("供应商名称(简称)").trim())){
    			title = "供应商名称(简称)";
    			break;
    		}else if("".equals(content.get(j).get("配送仓库").trim())){
    			title = "配送仓库";
    			break;
    		}else if("".equals(content.get(j).get("始发地城市").trim())){
    			title = "始发地城市";
    			break;
    		}else if("".equals(content.get(j).get("目的地城市").trim())){
    			title = "目的地城市";
    			break;
    		}
    		
    		try {
    			if("".equals(content.get(j).get("预约送货时间"))){
        			title = "预约送货时间";
        			break;
        		}else{
        			dbDataFormat.parse(content.get(j).get("预约送货时间"));
        		}
			} catch (ParseException e) {
				title = "预约送货时间";
				because = "数据有误";
				break;
			}
    		
    		try {
        		if("".equals(content.get(j).get("向客户预约时间"))){
        			title = "向客户预约时间";
        			break;
        		}else{
        			dbDataFormat.parse(content.get(j).get("向客户预约时间"));
        		}
			} catch (ParseException e) {
				title = "向客户预约时间";
				because = "数据有误";
				break;
			}
    		
    		try {
        		if("".equals(content.get(j).get("业务要求配送时间"))){
        			title = "业务要求配送时间";
        			break;
        		}else{
        			dbDataFormat.parse(content.get(j).get("业务要求配送时间"));
        		}
			} catch (ParseException e) {
				title = "业务要求配送时间";
				because = "数据有误";
				break;
			}
    		
    		because = "数据有误";
    		//始发地城市
    		Location location1 = Location.dao.findFirst("select code from location where name = '" +content.get(j).get("始发地城市") +"';");
			if(location1 == null){
				title = "始发地城市";
				break;
    		}
			
			//目的地城市
			Location location2 = Location.dao.findFirst("select code from location where name = '" +content.get(j).get("目的地城市") +"';");
			if(location2 == null){
				title = "目的地城市";
				break;
    		}
			
			//供应商名称
			Party provider = Party.dao.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_SERVICE_PROVIDER+ "' and c.abbr ='" + content.get(j).get("供应商名称(简称)") + "';");
			if(provider == null){
				title = "供应商名称(简称)";
				break;
    		}
			
    		//配送仓库
			Warehouse warehouse = Warehouse.dao.findFirst("select id from warehouse where warehouse_name = '" + content.get(j).get("配送仓库") + "';");
			if(warehouse == null){
				title = "配送仓库";
				break;
    		}
    		
    		//客户名称
			Party customer = Party.dao.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_CUSTOMER+ "' and c.abbr ='" + content.get(j).get("客户名称(简称)") + "';");
			if(customer == null){
				title = "客户名称(简称)";
				break;
    		}else{
    			//单品序列号
    			TransferOrderItemDetail transferOrderItemDetail1 = TransferOrderItemDetail.dao.findFirst("select toid.id from transfer_order_item_detail toid left join transfer_order tor on tor.id = toid.order_id where toid.delivery_id is null and toid.status = '已入库' and toid.serial_no = '"+content.get(j).get("单品序列号")+"';");
    			if(transferOrderItemDetail1 != null){
    				TransferOrderItemDetail transferOrderItemDetail2 = TransferOrderItemDetail.dao.findFirst("select toid.id from transfer_order_item_detail toid left join transfer_order tor on tor.id = toid.order_id where toid.delivery_id is null and toid.status = '已入库' and toid.serial_no = '"+content.get(j).get("单品序列号")+"' and tor.customer_id = '" + customer.get("pid") + "';");
        			if(transferOrderItemDetail2 != null){
        				TransferOrderItemDetail transferOrderItemDetail3 = TransferOrderItemDetail.dao.findFirst("select toid.id from transfer_order_item_detail toid left join transfer_order tor on tor.id = toid.order_id where toid.delivery_id is null and toid.status = '已入库' and toid.item_no = '"+content.get(j).get("货品型号")+"' and toid.serial_no = '"+content.get(j).get("单品序列号")+"' and tor.customer_id = '" + customer.get("pid") + "';");
            			if(transferOrderItemDetail3 == null){
            				title = "单品序列号";
            				because = "，该货品型号下没有此单品【"+content.get(j).get("单品序列号")+"】";
                			break;
                		}
            		}else{
            			title = "单品序列号";
        				because = "，该客户没有此单品【"+content.get(j).get("单品序列号")+"】";
            			break;
            		}
    			}else{
    				title = "单品序列号";
    				because = "，此单品已配送或没有此单品【"+content.get(j).get("单品序列号")+"】";
        			break;
    			}
    		}
    	}
    	if("".equals(title)){
    		importResult.put("result","true");
			importResult.put("cause", "验证数据成功");
    	}else{ 
    		importResult.put("result","false");
    		if("客户订单号".equals(title)){
    			importResult.put("cause", "验证数据至第" + (causeRow-1) + "行,因第" + causeRow + "行【" + title + "】列,请为同一张运输单的客户订单号做上标识（如：运输单001，运输单002等）！");
        	}else{
        		importResult.put("cause", "验证数据至第" + (causeRow-1) + "行,因第" + causeRow + "行【" + title + "】列" + because);
        	}
    	}
    	return importResult;
    }
	
	//设置配送单信息
	private DeliveryOrder saveDeliveryOrder(Map<String, String> content,Party party, TransferOrder order) {
		DeliveryOrder deliveryOrder = new DeliveryOrder();
		try {
			Party provider = Party.dao.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_SERVICE_PROVIDER+ "' and c.abbr = ?;",content.get("供应商名称(简称)"));
			Location location1 = Location.dao.findFirst("select code from location where name = ?;",content.get("始发地城市"));
			Location location2 = Location.dao.findFirst("select code from location where name = ?;",content.get("目的地城市"));
			
			SimpleDateFormat dbDataFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date orderDeliveryStamp = dbDataFormat.parse(content.get("预约送货时间"));
			Date clientOrderStamp = dbDataFormat.parse(content.get("向客户预约时间"));
			Date businessStamp = dbDataFormat.parse(content.get("业务要求配送时间"));
			//String orderNo = OrderNoGenerator.getNextOrderNo("PS");
			String orderNo = content.get("配送单号");
			
			deliveryOrder.set("order_no", "PS-"+orderNo)
			.set("customer_id", order.get("customer_id"))
			.set("from_warehouse_id", order.get("warehouse_id"))
			.set("cargo_nature", order.get("cargo_nature"))
			.set("sp_id", provider.get("pid"))
			.set("notify_party_id", party.get("id"))
			.set("route_to", location2.get("code"))
			.set("route_from", location1.get("code"))
			.set("create_stamp", Calendar.getInstance().getTime())
			.set("status", "新建")
			.set("audit_status", "新建")
			.set("sign_status", "未回单")
			.set("pricetype", "perUnit")
			.set("ltl_price_type", "perCBM")
			.set("client_requirement", content.get("需求确认"))
			.set("customer_delivery_no",content.get("客户配送单号"))
			.set("business_stamp", businessStamp)
			.set("client_order_stamp", clientOrderStamp)
			.set("order_delivery_stamp", orderDeliveryStamp)
			.save();
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return deliveryOrder;
	}
	
	// 保存运输里程碑
	private void saveDeliveryOrderMilestone(DeliveryOrder deliveryOrder) {
		DeliveryOrderMilestone deliveryOrderMilestone = new DeliveryOrderMilestone();
		deliveryOrderMilestone.set("status", "新建")
		.set("create_by", user.get("id"))
		.set("create_stamp", Calendar.getInstance().getTime())
		.set("delivery_id", deliveryOrder.get("id"))
		.save();
	}
	
	//更新运输单信息
	private void updateTransferOrder(TransferOrder order,TransferOrderItemDetail detail, DeliveryOrder deliveryOrder) {
		order.set("status", "配送中").update();
		detail.set("delivery_id",deliveryOrder.get("id")).set("is_delivered", true).update();
	}
	
	//设置从表信息
	private void saveDeliveryItem(TransferOrder order,TransferOrderItemDetail detail, DeliveryOrder deliveryOrder) {
		DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
		deliveryOrderItem.set("delivery_id",deliveryOrder.get("id"))
		.set("transfer_order_id",order.get("id"))
		.set("transfer_item_detail_id", detail.get("id"))
		.set("transfer_no", order.get("order_no"))
		.set("amount", 1)
		.save();
	}
	
	//设置收货人信息
	private Party saveNotify(Map<String,String> content) {
		Party party = new Party();
		Contact contact = new Contact();
		
		contact.set("company_name", content.get("收货单位"))
		.set("contact_person", content.get("联系人"))
		.set("address", content.get("收货地址"))
		.set("phone", content.get("联系电话"))
		.save();
		party.set("contact_id", contact.get("id"))
		.set("party_type", "NOTIFY_PARTY")
		.set("create_date", Calendar.getInstance().getTime())
		.set("creator", user.get("id"))
		.save();
		
		return party;
	}
	
	/**
     * 导入数据
     * @param content
     * @return 导入结果
     */
	public Map<String,String> importDeliveryOrder(List<Map<String,String>> content){
		Map<String, String> importResult = new HashMap<String, String>();
		importResult = validatingOrderNo(content);
    	if("true".equals(importResult.get("result"))){
			importResult = validatingData(content);
			if("true".equals(importResult.get("result"))){
				int resultNum = 0;
		    	int causeRow = 0;
				try {
					String deliverOrderNo = "";
					String customer = "";
					String companyName = "";
					String destinationCity = "";
					String warehouse = "";
					
					for (int j = 0; j < content.size(); j++) {
						causeRow = j+2;
						System.out.println("导入至第【"+causeRow+"】行");
						TransferOrder order = TransferOrder.dao.findFirst("select tor.* from transfer_order_item_detail toid left join transfer_order tor on tor.id = toid.order_id where toid.serial_no = ?;",content.get(j).get("单品序列号"));
						TransferOrderItemDetail detail = TransferOrderItemDetail.dao.findFirst("select * from transfer_order_item_detail where serial_no = ?;",content.get(j).get("单品序列号"));
						if(order != null && detail != null){
							//判断是否为同一张单
							if(deliverOrderNo.equals(content.get(j).get("配送单号")) && customer.equals(content.get(j).get("客户名称(简称)")) &&
									companyName.equals(content.get(j).get("收货单位")) && destinationCity.equals(content.get(j).get("目的地城市")) &&
									warehouse.equals(content.get(j).get("配送仓库"))){
								DeliveryOrder deliveryOrder = DeliveryOrder.dao.findFirst("select * from delivery_order where order_no = ? ;",content.get(j).get("配送单号"));
								//设置从表信息
								saveDeliveryItem(order, detail, deliveryOrder);
								//更新运输单信息
								updateTransferOrder(order, detail, deliveryOrder);
							}else{
								deliverOrderNo = content.get(j).get("配送单号");
								customer = content.get(j).get("客户名称(简称)");
								companyName = content.get(j).get("收货单位");
								destinationCity = content.get(j).get("目的地城市");
								warehouse = content.get(j).get("配送仓库");
								//生成配送单数量
			    				++resultNum;
								//设置收货人信息
								Party party = saveNotify(content.get(j));
								//设置配送单信息
								DeliveryOrder deliveryOrder = saveDeliveryOrder(content.get(j), party, order);
								//设置从表信息
								saveDeliveryItem(order, detail, deliveryOrder);
								//设置里程碑信息
								saveDeliveryOrderMilestone(deliveryOrder);
								//更新运输单信息
								updateTransferOrder(order, detail, deliveryOrder);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("未知错误！");
					importResult.put("result","true");
					importResult.put("cause", "未知错误，已成功导入至第" + (causeRow-1) + "行！");
					return importResult;
				}
				importResult.put("result","true");
		    	importResult.put("cause", "成功导入" + resultNum + "张配送单");
			}
    	}
		return importResult;
	}
}
