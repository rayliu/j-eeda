package controllers.yh.delivery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.Location;
import models.Party;
import models.TransferOrder;
import models.TransferOrderItemDetail;
import models.UserLogin;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.util.OrderNoGenerator;
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
    		System.out.println();
    		for (Record record : titleList) {
    			System.out.println("系统："+record.get("execl_title")+",execl:"+title[num]);
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
			
			deliveryOrder.set("order_no", orderNo)
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
		return importResult;
	}
}
