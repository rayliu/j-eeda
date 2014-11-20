package controllers.yh.order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
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
    public Map<String,String> importTransferOrder(List<Map<String,String>> content,Party party) throws ParseException{
    	Map<String, String> importResult = new HashMap<String, String>();
    	String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat dbDataFormat = new SimpleDateFormat("yyyy-MM-dd");
		String format = sdf.format(new Date());
		String time = format + "00001";
		Long newTime = Long.parseLong(time);
		int resultNum = 0;
		String result = "";
    	for (int j = 0; j < content.size(); j++) {
    		String customerOrderNo = content.get(j).get("运输单").trim();
    		if(!"".equals(customerOrderNo)){
				Record tansferOrder = Db.findFirst("select * from transfer_order where customer_order_no = '" + customerOrderNo + "';");
				if(tansferOrder != null){
					Record product = Db.findFirst("select * from product where item_no = '"+content.get(j).get("产品型号")+"';");
					Record tansferOrderItem= Db.findFirst("select * from transfer_order_item where order_id = '" + tansferOrder.get("id") +"' and item_no = '" + content.get(j).get("产品型号") + "';");
					if(tansferOrderItem != null){
						//本来这里是要修改货品明细表中amount（数量），
						//由于execl中“发货数量”列是货品总数，所以不用修改
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
							.set("notify_party_id", party.get("id"))
							.set("pieces", content.get(j).get("件数")) 
							.save();
						}
					}else{
						//创建保存货品明细
						TransferOrderItem item =new TransferOrderItem();
						double num = product.getDouble("weight") * Double.parseDouble(content.get(j).get("发货数量"));
						item.set("order_id", tansferOrder.get("id"))
						.set("amount", content.get(j).get("发货数量"))
						.set("item_no", product.get("item_no"))
						.set("item_name", product.get("item_name"))
						.set("size", product.get("size"))
						.set("unit", product.get("unit"))
						.set("width", product.get("width"))
						.set("height", product.get("height"))
						.set("volume", product.get("volume"))
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
						.set("notify_party_id", party.get("id"))
						.set("pieces", content.get(j).get("件数")) 
						.save();
					}
				}else{
					if(!"".equals(customerOrderNo) && customerOrderNo != null){
						//创建保存运输单
						String planning = content.get(j).get("计划日期");
						String arrivl = content.get(j).get("预计到货日期");
						Date planningTime = dbDataFormat.parse(planning);
						Date arrivalTime = dbDataFormat.parse(planning);
						TransferOrder transferOrder = new TransferOrder();
						transferOrder.set("order_no", "YS" + newTime++)
						.set("order_type", "salesOrder")//订单类型：默认为销售订单
						.set("operation_type", "own")//运营方式：默认自营
						.set("charge_type", "perUnit")//客户计费方式：默认计件
						.set("charge_type2", "perUnit")//供应商计费方式：默认计件
						.set("pickup_assign_status",TransferOrder.ASSIGN_STATUS_NEW)
						.set("depart_assign_status",TransferOrder.ASSIGN_STATUS_NEW)
						.set("status", "新建")
						.set("create_by", users.get(0).get("id"))//创建人id
						.set("create_stamp", new Date())//创建时间
						.set("receiving_unit", content.get(j).get("收货单位"))//收货单位
						.set("planning_time", planningTime)//计划时间
						.set("arrival_time", arrivalTime)//预计到货时间
						.set("customer_order_no", customerOrderNo);//客户订单号
						
						//到达方式
						if(!"".equals(content.get(j).get("仓储地点"))){
							transferOrder.set("arrival_mode", "gateIn");
						}else{
							transferOrder.set("arrival_mode", "delivery");
						}
						//始发城市
						Record rec1 = Db.findFirst("select code from location where name = '" +content.get(j).get("始发城市") +"';");
						if(rec1 != null){
							transferOrder.set("route_from",rec1.get("code"));
						}
						//目的地城市
						Record rec2 = Db.findFirst("select code from location where name = '" +content.get(j).get("到达城市") +"';");
						if(rec2 != null){
							transferOrder.set("route_to",rec2.get("code"));
						}
						//客户名称
						String sql = "select p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type ='" + Party.PARTY_TYPE_CUSTOMER+ "' and company_name ='" + content.get(j).get("客户名称") + "';";
						Record rec3 = Db.findFirst(sql);
						if(rec3 != null){
							transferOrder.set("customer_id",rec3.get("pid"));
						}else{
							result = "客户名称不能为空";
						}
						//供应商名称
						Record rec4 = Db.findFirst("select p.id from party p,contact c where p.contact_id = c.id and p.party_type ='" + Party.PARTY_TYPE_SERVICE_PROVIDER+ "' and company_name ='" + content.get(j).get("供应商名称") + "';");
						if(rec4 != null){
							transferOrder.set("sp_id", rec4.get("id"));
						}
						//网点
						Record rec5 = Db.findFirst("select id from office where office_name = '" + content.get(j).get("网点") + "';");
						if(rec5 != null){
							transferOrder.set("office_id", rec5.get("id"));
						}
						//仓库
						Record rec6 = Db.findFirst("select id from warehouse where warehouse_name = '" + content.get(j).get("仓储地点") + "';");
						if(rec6!= null){
							transferOrder.set("warehouse_id", rec6.get("id"));
						}
						//货品属性
						Record product = Db.findFirst("select * from product where item_no = '"+content.get(j).get("产品型号")+"';");
						if(product != null){
							if(product.get("item_name").equals("ATM"))
								transferOrder.set("cargo_nature","ATM");
							else
								transferOrder.set("cargo_nature","cargo");
						}
						//收货人
						transferOrder.set("notify_party_id", party.get("id"));
						transferOrder.save();
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
						.set("volume", product.get("volume"))
						.set("weight", product.getDouble("weight"))
						.set("sum_weight", num)
						.set("product_id", product.get("id"))
						.save();
						//创建保存单品货品明细
						TransferOrderItemDetail itemDatail = new TransferOrderItemDetail();
						itemDatail.set("order_id", transferOrder.get("id"))
						.set("item_id", item.get("id"))
						.set("item_no", product.get("item_no"))
						.set("serial_no", content.get(j).get("序列号"))
						.set("item_name", product.get("item_name"))
						.set("volume", product.get("volume"))
						.set("weight", product.get("weight"))
						.set("notify_party_id", party.get("id"))
						.set("pieces", content.get(j).get("件数")) 
						.save();
					}
				}
				++resultNum;
    		}
    	}
    	if(!"".equals(result))
    		importResult.put("result",result);
    	else
    		importResult.put("result", "成功插入" + resultNum + "条数据");
    	return importResult;  
    }
    
    
	
}
