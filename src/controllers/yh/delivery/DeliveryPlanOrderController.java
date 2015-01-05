package controllers.yh.delivery;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.yh.delivery.DeliveryOrder;
import models.yh.delivery.DeliveryPlanOrder;
import models.yh.delivery.DeliveryPlanOrderDetail;
import models.yh.delivery.DeliveryPlanOrderMilestone;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.profile.CarinfoController;
import controllers.yh.util.OrderNoUtil;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class DeliveryPlanOrderController extends Controller {
	
	private Logger logger = Logger.getLogger(CarinfoController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	public void index() {
		render("/yh/delivery/deliveryPlanOrderList.html");
    }
	
	public void add(){
		render("/yh/delivery/deliveryPlanOrderSearchDelivery.html");
	}
	//可选配送单
	public void deliveryList(){
		
		String orderNoFilter = getPara("orderNo_filter");
		String transferFilter = getPara("transfer_filter");
		String statusFilter = getPara("status_filter");
		String customerFilter = getPara("customer_filter");
		String spFilter = getPara("sp_filter");
		String serialNo = getPara("serial_no");
		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map deliveryOderListMap = new HashMap();
		String sqlTotal = "";
		String sql  = "";
		if (orderNoFilter == null && transferFilter == null
				&& statusFilter == null && customerFilter == null
				&& spFilter == null && serialNo == null) {
			sqlTotal = "select count(1) total from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id"
					+ " where d.status = '新建' and ifnull(d.delivery_plan_type,'') != 'processed'";
			
			sql = "select d.*,c.abbr as customer,c2.company_name as c2,(select group_concat( distinct doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no,"
					+ " (select group_concat(trid.serial_no separator '\r\n') from delivery_order_item doi left join transfer_order_item_detail trid on trid.id = doi.transfer_item_detail_id where doi.delivery_id = d.id) as serial_no"
					+ " from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id"
					+ " where d.status = '新建' and ifnull(d.delivery_plan_type,'') != 'processed' order by d.create_stamp desc "
					+ sLimit;

		} else {

			sqlTotal = "select count(1) total from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join delivery_order_item dt2 on dt2.delivery_id = d.id "
					+ " left join transfer_order_item_detail trid on trid.id = dt2.transfer_item_detail_id "
					+ " where d.status = '新建' and ifnull(d.delivery_plan_type,'') != 'processed'"
					+ " and ifnull(d.order_no,'') like '%" + orderNoFilter + "%' "
					+ " and ifnull(d.status,'') like '%" + statusFilter + "%' "
					+ " and ifnull(c.abbr,'') like '%" + customerFilter + "%' "
					+ " and ifnull(dt2.transfer_no,'') like '%" + transferFilter + "%' "
					+ " and ifnull(c2.company_name,'') like'%" + spFilter + "%' "
					+ " and ifnull(trid.serial_no,'') like'%" + serialNo + "%'";

			sql = "select d.*,c.abbr as customer,c2.company_name as c2,(select group_concat( distinct doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no,"
					+ " (select group_concat(trid.serial_no separator '\r\n') from delivery_order_item doi left join transfer_order_item_detail trid on trid.id = doi.transfer_item_detail_id where doi.delivery_id = d.id) as serial_no"
					+ " from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join delivery_order_item dt2 on dt2.delivery_id = d.id "
					+ " left join transfer_order_item_detail trid on trid.id = dt2.transfer_item_detail_id "
					+ " where d.status = '新建' and ifnull(d.delivery_plan_type,'') != 'processed'"
					+ " and ifnull(d.order_no,'') like '%" + orderNoFilter + "%' "
					+ " and ifnull(d.status,'') like '%" + statusFilter + "%' "
					+ " and ifnull(c.abbr,'') like '%" + customerFilter + "%' "
					+ " and ifnull(dt2.transfer_no,'') like '%" + transferFilter + "%' "
					+ " and ifnull(c2.company_name,'') like'%" + spFilter + "%' "
					+ " and ifnull(trid.serial_no,'') like'%" + serialNo + "%' "
					+ " group by d.id order by d.create_stamp desc" + sLimit;
		}
		Record rec = Db.findFirst(sqlTotal);
		List<Record> deliveryOrders = Db.find(sql);

		deliveryOderListMap.put("sEcho", pageIndex);
		deliveryOderListMap.put("iTotalRecords", rec.getLong("total"));
		deliveryOderListMap.put("iTotalDisplayRecords",
				rec.getLong("total"));
		deliveryOderListMap.put("aaData", deliveryOrders);

		renderJson(deliveryOderListMap);
	}
	
	public void findAllDeliveryPlanOrder(){
		
		String orderNo = getPara("order_no");
		String deliveryNo = getPara("delivery_no");
		String officeId = getPara("office_id");
		String carNo = getPara("car_no");
		String turnoutTime = getPara("turnout_time");
		String returnTime = getPara("return_time");
		 
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String sqlTotal = "";
        String sql = "";
		if (orderNo == null && deliveryNo == null && officeId == null
				&& carNo == null && returnTime == null && turnoutTime == null) {
			sqlTotal = "select count(0) total from delivery_plan_order ";
			sql = "select d.*,u.user_name, o.office_name,"
					+ " (select group_concat(de.order_no separator '\r\n') from delivery_plan_order_detail dd"
					+ " left join delivery_order de on de.id = dd.delivery_id where dd.order_id = d.id) deliver_no,"
					+ " (select sum( ifnull(toi.volume, p.volume) * toi.amount ) from transfer_order_item toi"
					+ " left join product p on p.id = toi.product_id where exists "
					+ " (select 1 from delivery_order_item doi where toi.order_id = doi.transfer_order_id and exists  "
					+ " (select 1 from delivery_plan_order_detail dpod where dpod.delivery_id=doi.delivery_id and order_id = 34) ) ) volume, "
					+ " (select sum(ifnull(nullif(toi.weight, 0),p.weight) * toi.amount) from transfer_order_item toi "
					+ " left join product p on p.id = toi.product_id where exists  (select 1 from delivery_order_item doi "
					+ " where doi.transfer_order_id=toi.order_id and exists "
					+ " (select 1 from delivery_plan_order_detail dpod where doi.delivery_id=dpod.delivery_id and order_id = d.id) )) weight "
					+ " from delivery_plan_order d"
					+ " left join office o on o.id = d.office_id"
					+ " left join delivery_plan_order_detail de on de.order_id = d.id"
					+ " left join user_login u on u.id = d.create_id group by d.id order by d.id desc " + sLimit;
	        
		}else{
			
			sqlTotal = "select count(0) total from delivery_plan_order d"
					+ " left join office o on o.id = d.office_id"
					+ " left join delivery_plan_order_detail de on de.order_id = d.id"
					+ " left join delivery_order dor on dor.id = de.delivery_id"
					+ " left join user_login u on u.id = d.create_id "
					+ " where ifnull(d.order_no, '') like '%" + orderNo + "%'"
					+ " and ifnull(d.office_id, '') like '%" + officeId + "%'"
					+ " and ifnull(d.turnout_time, '') like '%" + turnoutTime + "%'"
					+ " and ifnull(d.return_time, '') like '%" + returnTime + "%'"
					+ " and ifnull(d.car_no, '') like '%" + carNo + "%'"
					+ " and ifnull(dor.order_no, '') like '%" + deliveryNo + "%'";
			
			sql = "select d.*,u.user_name, o.office_name,"
					+ " (select group_concat(de.order_no separator '\r\n') from delivery_plan_order_detail dd"
					+ " left join delivery_order de on de.id = dd.delivery_id where dd.order_id = d.id) deliver_no,"
					+ " (select sum( ifnull(toi.volume, p.volume) * toi.amount ) from transfer_order_item toi"
					+ " left join product p on p.id = toi.product_id where exists "
					+ " (select 1 from delivery_order_item doi where toi.order_id = doi.transfer_order_id and exists  "
					+ " (select 1 from delivery_plan_order_detail dpod where dpod.delivery_id=doi.delivery_id and order_id = 34) ) ) volume, "
					+ " (select sum(ifnull(nullif(toi.weight, 0),p.weight) * toi.amount) from transfer_order_item toi "
					+ " left join product p on p.id = toi.product_id where exists  (select 1 from delivery_order_item doi "
					+ " where doi.transfer_order_id=toi.order_id and exists "
					+ " (select 1 from delivery_plan_order_detail dpod where doi.delivery_id=dpod.delivery_id and order_id = d.id) )) weight "
					+ " from delivery_plan_order d"
					+ " left join office o on o.id = d.office_id"
					+ " left join delivery_plan_order_detail de on de.order_id = d.id"
					+ " left join delivery_order dor on dor.id = de.delivery_id"
					+ " left join user_login u on u.id = d.create_id "
					+ " where ifnull(d.order_no, '') like '%" + orderNo + "%'"
					+ " and ifnull(d.office_id, '') like '%" + officeId + "%'"
					+ " and ifnull(d.turnout_time, '') like '%" + turnoutTime + "%'"
					+ " and ifnull(d.return_time, '') like '%" + returnTime + "%'"
					+ " and ifnull(d.car_no, '') like '%" + carNo + "%'"
					+ " and ifnull(dor.order_no, '') like '%" + deliveryNo + "%'"
					+ "group by d.id order by d.id desc " + sLimit;
		}
		Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orders = Db.find(sql);
		
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
	}
	
	public void edit(){
		String id = getPara("id");
		DeliveryPlanOrder deliveryPlanOrder = DeliveryPlanOrder.dao.findById(id);
		long spId = deliveryPlanOrder.getLong("sp_id");
		String sql="select p.id,c.company_name from	party p left join contact c on c.id = p.contact_id where p.id = "+spId;
		Record rec = Db.findFirst(sql);
		setAttr("spName", rec.get("company_name"));
		setAttr("spId", spId);
		setAttr("deliveryPlanOrder", deliveryPlanOrder);
		UserLogin user = UserLogin.dao.findFirst("select * from user_login where id='" + deliveryPlanOrder.getLong("create_id") + "'");
		setAttr("user", user);
		//获取配送单id并拼接成字符串
        String sql2="select concat(group_concat(cast(delivery_id as char) separator ',')) deliveryOrders from delivery_plan_order_detail where order_id = " + id;
        Record rec2 = Db.findFirst(sql2);
        setAttr("deliveryOrderIds", rec2.get("deliveryOrders").toString());

		render("/yh/delivery/deliveryPlanOrderEdit.html");
	}
	
	public void createDeliveryPlanOrder(){
		String deliveryOrderIds = getPara("deliveryOrderIds");
		String spId = getPara("spId");
		String sql="select p.id,c.company_name from	party p left join contact c on c.id = p.contact_id where p.id = "+spId;
		Record rec = Db.findFirst(sql);
		setAttr("spId", spId);
		setAttr("spName", rec.get("company_name"));
		setAttr("deliveryOrderIds", deliveryOrderIds);
		render("/yh/delivery/deliveryPlanOrderEdit.html");
	}
	
	//保存
	public void saveDeliveryPlanOrder(){
		
		String deliveryPlanOrderId = getPara("deliveryPlanOrderId");
		String spId = getPara("spId");
		String carInfoId = getPara("carInfoId");
		String carNo = getPara("car_no");
		String driver = getPara("driver");
		String phone = getPara("phone");
		String turnoutIime = getPara("turnout_time");
		String returnTime = getPara("return_time");
		String remark = getPara("remark");
		
		String deliveryOrderIds = getPara("deliveryOrderIds");
		String deliveryOrderId[] = deliveryOrderIds.split(",");
		DeliveryPlanOrder deliveryPlanOrder = null;
		if (deliveryPlanOrderId == null || "".equals(deliveryPlanOrderId)) {
			String sql = "select * from delivery_plan_order order by id desc limit 0,1";
			String name = (String) currentUser.getPrincipal();
	        UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
	        System.out.println(",.,.,.,.,.,.,.,.,.,.,..,.,,.,.,.,.:"+users.get("office_id"));
			deliveryPlanOrder = new DeliveryPlanOrder();
            deliveryPlanOrder.set("order_no", OrderNoUtil.getOrderNo(sql, "PSPC"))
            .set("status", "新建").set("create_id", users.get("id")).set("create_stamp", new Date())
            .set("sp_id", spId).set("turnout_time", turnoutIime).set("car_no", carNo)
            .set("driver", driver).set("phone", phone).set("carinfo_id", carInfoId)
            .set("office_id", users.get("office_id"));
            if(!"".equals(returnTime) && returnTime != null){
            	deliveryPlanOrder.set("return_time", returnTime);
            }
            deliveryPlanOrder.save();
            
            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            
            DeliveryPlanOrderMilestone milestone = new DeliveryPlanOrderMilestone();
            milestone.set("create_id", users.get("id")).set("status", "新建")
            .set("create_stamp", sqlDate).set("order_id", deliveryPlanOrder.get("id"))
            .save();
            
            //记录所选配送单
            for (int i = 0; i < deliveryOrderId.length; i++) {
    			//插入从表数据
            	DeliveryPlanOrderDetail departTransfer = new DeliveryPlanOrderDetail();
            	departTransfer.set("order_id", deliveryPlanOrder.get("id"))
            	.set("delivery_id", deliveryOrderId[i]).save();
            	
            	DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(deliveryOrderId[i]);
            	deliveryOrder.set("delivery_plan_type", "processed").update();
			}
		} else {
			deliveryPlanOrder = DeliveryPlanOrder.dao.findById(deliveryPlanOrderId);
			deliveryPlanOrder.set("turnout_time", turnoutIime)
            .set("car_no", carNo).set("driver", driver).set("phone", phone)
            .set("carinfo_id", carInfoId).set("remark", remark);
			 if(!"".equals(returnTime) && returnTime != null){
	            	deliveryPlanOrder.set("return_time", returnTime);
	         }
			 deliveryPlanOrder.update();
		}
		renderJson(deliveryPlanOrder);
	}
	
	// 查出所有的carinfo
	public void searchAllCarInfo() {
		String type = getPara("type");
		String name = getPara("name");
		String input = getPara("input");
		String sql="";
		
		if(input == null || "".equals(input)){
			sql = "select * from carinfo where type = '" + type + "'";
		}else{
			sql = "select * from carinfo where type = '" + type + "' and " + name + " like '" + input + "';";
		}
		List<Record> locationList = Db.find(sql);
		renderJson(locationList);
	}
	
	//线路
	public void findDeliveryPlanRoute(){
		String deliveryOrderIds = getPara("deliveryOrderIds");
		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map deliveryOderListMap = new HashMap();
		String sqlTotal = "select count(0) total from delivery_order where id in(" + deliveryOrderIds + ")";
		String sql  = "select d.*, c.abbr as customer from delivery_order d left join party p on d.customer_id = p.id"
				+ " left join contact c on p.contact_id = c.id where d.id in(" + deliveryOrderIds + ") order by d.create_stamp desc "+sLimit;
		
		Record rec = Db.findFirst(sqlTotal);
		List<Record> deliveryOrders = Db.find(sql);

		deliveryOderListMap.put("sEcho", pageIndex);
		deliveryOderListMap.put("iTotalRecords", rec.getLong("total"));
		deliveryOderListMap.put("iTotalDisplayRecords",
				rec.getLong("total"));
		deliveryOderListMap.put("aaData", deliveryOrders);

		renderJson(deliveryOderListMap);
	}
	
	//货品明细
	public void findDeliveryOrderItems() {
		String deliveryOrderIds = getPara("deliveryOrderIds");
		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from transfer_order_item_detail t "
				+ " left join transfer_order_item t1 on t.item_id =t1.id "
				+ " left join transfer_order t3 on t3.id =t.order_id "
				+ " left join contact c on c.id in (select contact_id from party p where t3.customer_id=p.id) "
				+ "	left join delivery_order_item dot on dot.transfer_order_id = t.order_id"
				+ " where dot.delivery_id in(" + deliveryOrderIds + ")";
		
		String  sql = "select t.*,t3.order_no,c.abbr as customer from transfer_order_item_detail t "
				+ " left join transfer_order_item t1 on t.item_id =t1.id "
				+ " left join transfer_order t3 on t3.id =t.order_id "
				+ " left join contact c on c.id in (select contact_id from party p where t3.customer_id=p.id) "
				+ " left join delivery_order_item dot on dot.transfer_order_id = t.order_id"
				+ " where dot.delivery_id in(" + deliveryOrderIds + ") order by t.id desc " + sLimit;
		
		Record rec = Db.findFirst(sqlTotal);
		List<Record> departOrderitem = Db.find(sql);

		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));
		Map.put("aaData", departOrderitem);

		renderJson(Map);
	}
	
	public void findDeliveryOrderMilestone(){
		String deliveryPlanOrderId = getPara("deliveryPlanOrderId");
		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(0) total from delivery_plan_order_milestone where order_id = " + deliveryPlanOrderId;
		
        String sql = "select d.*,u.user_name,u.c_name from delivery_plan_order_milestone d left join user_login u on u.id = d.create_id "
                + " where d.order_id = " + deliveryPlanOrderId + " order by d.id asc " + sLimit;

		Record rec = Db.findFirst(sqlTotal);
		List<Record> departOrderitem = Db.find(sql);

		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));
		Map.put("aaData", departOrderitem);

		renderJson(Map);
	}
	
	//更新运输里程碑
	public void updateDeliveryPlanOrderMilestone(){
		String status = getPara("status");
		String location = getPara("location");
		String deliveryPlanOrderId = getPara("deliveryPlanOrderId");
		
		String name = (String) currentUser.getPrincipal();
        UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		
		DeliveryPlanOrderMilestone milestone = new DeliveryPlanOrderMilestone();
		milestone.set("address", location).set("create_id", users.get("id"))
		.set("create_stamp", sqlDate).set("order_id", deliveryPlanOrderId);
		if(status == null || "".equals(status)){
			milestone.set("status", "在途");
		}else{
			milestone.set("status", status);
		}
		milestone.save();
		renderJson("{\"success\":true}");
	}
	
}
