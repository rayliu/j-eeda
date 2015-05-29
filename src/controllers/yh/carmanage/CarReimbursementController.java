package controllers.yh.carmanage;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.yh.arap.ReimbursementOrder;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;
import controllers.yh.profile.CarinfoController;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.OrderNoUtil;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CarReimbursementController extends Controller {
	private Logger logger = Logger.getLogger(CarinfoController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	public void index() {
        HttpServletRequest re = getRequest();
        String url = re.getRequestURI();
        if (url.equals("/carreimbursement")) {
            render("/yh/carmanage/carReimbursementList.html");
        }
    }
	
	public void list(){
		Map orderMap = null;
		String carReimbursementNo = getPara("carReimbursementNo");
		String carNo = getPara("carNo");
		String departOrderNo = getPara("departOrderNo");
		String turnoutTime = getPara("turnoutTime");
		String carReimbursementStatus = getPara("carReimbursementStatus");
		String driver = getPara("driver");
		String acditorName = getPara("acditorName");
		
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String sqlTotal = "";
        String sql = "";
        if(carReimbursementNo == null && carReimbursementStatus == null && acditorName == null){
        	sqlTotal = "select count(0) total from reimbursement_order ro "
        			+ " left join user_login l1 on l1.id = ro.create_id"
        			+ " left join office o on o.id = l1.office_id"
        			+ " where o.id in (select office_id from user_office where user_name = '"+currentUser.getPrincipal()+"')";
        	sql = "select ro.id, ro.order_no,ro.status,ro.create_stamp,ro.audit_stamp,l1.c_name creator, l2.c_name auditor, "
        			+ " (select group_concat(order_no separator '<br>' ) from car_summary_order cso where cso.reimbursement_order_id = ro.id) cso_order_no,"
        			+ " (select sum(cso.next_start_car_amount + cso.month_refuel_amount) from car_summary_order cso where cso.id in "
        			+ " (select id from	car_summary_order cso where	cso.reimbursement_order_id = ro.id) ) total_cost,     "
        			+ " (select sum(cso.deduct_apportion_amount) from car_summary_order cso where cso.id in "
        			+ " (select id from	car_summary_order cso where	cso.reimbursement_order_id = ro.id) ) deduct_cost,    "
        			+ " (select sum(cso.next_start_car_amount + cso.month_refuel_amount)- sum(cso.deduct_apportion_amount) from car_summary_order cso   where cso.id in "
        			+ " (select id from	car_summary_order cso where	cso.reimbursement_order_id = ro.id)) actual_cost "
        			+ " from reimbursement_order ro "
        			+ " left join user_login l1 on l1.id = ro.create_id"
        			+ " left join user_login l2 on l2.id = ro.audit_id"
        			+ " left join office o on o.id = l1.office_id"
        			+ " where o.id in (select office_id from user_office where user_name = '"+currentUser.getPrincipal()+"')"
        			+ " order by ro.create_stamp desc " + sLimit;
        }else{
        	sqlTotal = "select count(0) total from reimbursement_order ro"
        			+ " left join user_login l1 on l1.id = ro.create_id"
        			+ " left join user_login l2 on l2.id = ro.audit_id"
        			+ " where ifnull(ro.order_no, '') like '%" + carReimbursementNo + "%'"
        			+ " and ifnull(ro.status, '') like '%" + carReimbursementStatus + "%'"
        			+ " and ifnull(l2.c_name, '') like '%" + acditorName + "%'";
        	sql = "select ro.id, ro.order_no,ro.status,ro.create_stamp,ro.audit_stamp,l1.c_name creator, l2.c_name auditor, "
        			+ " (select group_concat(order_no separator '<br>' ) from car_summary_order cso where cso.reimbursement_order_id = ro.id) cso_order_no,"
        			+ " (select sum(cso.next_start_car_amount + cso.month_refuel_amount) from car_summary_order cso where cso.id in "
        			+ " (select id from	car_summary_order cso where	cso.reimbursement_order_id = ro.id) ) total_cost,     "
        			+ " (select sum(cso.deduct_apportion_amount) from car_summary_order cso where cso.id in "
        			+ " (select id from	car_summary_order cso where	cso.reimbursement_order_id = ro.id) ) deduct_cost,    "
        			+ " (select sum(cso.next_start_car_amount + cso.month_refuel_amount)- sum(cso.deduct_apportion_amount) from car_summary_order cso   where cso.id in "
        			+ " (select id from	car_summary_order cso where	cso.reimbursement_order_id = ro.id)) actual_cost "
        			+ " from reimbursement_order ro "
        			+ " left join user_login l1 on l1.id = ro.create_id"
        			+ " left join user_login l2 on l2.id = ro.audit_id"
        			+ " left join office o on o.id = l1.office_id"
        			+ " where ifnull(ro.order_no, '') like '%" + carReimbursementNo + "%'"
        			+ " and ifnull(ro.status, '') like '%" + carReimbursementStatus + "%'"
        			+ " and ifnull(l2.c_name, '') like '%" + acditorName + "%'"
        			+ " and o.id in (select office_id from user_office where user_name = '"+currentUser.getPrincipal()+"')"
        			+ " order by ro.create_stamp desc " + sLimit;
        }
        
		Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
		List<Record> orders = Db.find(sql);
		
	 	orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
	}
	
	public void orderDetailList(){
		Map orderMap = null;
		String carSummaryOrderIds = getPara("car_summary_order_ids");
		
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        if(carSummaryOrderIds != null && !"".equals(carSummaryOrderIds)){
			String sqlTotal = "select count(distinct cso.id) total from car_summary_order cso"
					+ " left join car_summary_detail csd on csd.car_summary_id = cso.id"
					+ " left join depart_order dod on dod.id = csd.pickup_order_id "
					+ "	where cso.id in ("+carSummaryOrderIds+")";
			
			String sql = "select distinct cso.id,cso.order_no ,cso.status ,cso.car_no,cso.main_driver_name ,"
					+ " cso.month_refuel_amount,cso.deduct_apportion_amount,cso.actual_payment_amount,"
					+ "	(cso.next_start_car_amount + cso.month_refuel_amount) as total_cost ,"
					+ " (cso.finish_car_mileage - cso.start_car_mileage ) as carsummarymileage,"
					+ " (select group_concat(pickup_order_no SEPARATOR '\r\n' ) from car_summary_detail where car_summary_id = cso.id) as pickup_no,"
					+ " (select group_concat( dt.transfer_order_no SEPARATOR '\r\n' ) from depart_transfer dt"
					+ " where dt.pickup_id in(select pickup_order_id from car_summary_detail where car_summary_id = cso.id )) as transfer_order_no,"
					+ " (select turnout_time from depart_order where id = ( select min(pickup_order_id) from car_summary_detail where car_summary_id = cso.id)) as turnout_time,"
					+ " (select return_time from depart_order where id = ( select max(pickup_order_id) from car_summary_detail where car_summary_id = cso.id)) as return_time,"
					+ " (select sum(ifnull(toi.volume, p.volume) * toi.amount ) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id IN "
					+ " (select order_id from depart_transfer where pickup_id IN ( select pickup_order_id from car_summary_detail where car_summary_id = cso.id ))) as volume,"
					+ " (select sum( ifnull(nullif(toi.weight, 0),p.weight) * toi.amount) from transfer_order_item toi left join product p on p.id = toi.product_id where toi.order_id IN "
					+ " (select order_id from depart_transfer where pickup_id IN ( select pickup_order_id from car_summary_detail where car_summary_id = cso.id ))) as weight,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 2) refuel_consume,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 3) subsidy,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 4) driver_salary,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 5) toll_charge,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 6) handling_charges,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 7) fine,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 8) deliveryman_salary,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 9) parking_charge,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 10) quarterage,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 11) weighing_charge,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 12) other_charges"
					+ " from car_summary_order cso"
					+ " left join car_summary_detail csd on csd.car_summary_id = cso.id"
					+ " left join depart_order dod on dod.id = csd.pickup_order_id "
					+ "	where cso.id in ("+carSummaryOrderIds+") "
					//+ " and ifnull(dor.start_data, '') like '%"+transferOrderNo+"%'"
					+ " order by cso.create_data desc " + sLimit;
			Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
			List<Record> orders = Db.find(sql);
			orderMap = new HashMap();
	        orderMap.put("sEcho", pageIndex);
	        orderMap.put("iTotalRecords", rec.getLong("total"));
	        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
	        orderMap.put("aaData", orders);
        }else{
        	orderMap = new HashMap();
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
        }
        renderJson(orderMap);
	}
	
	//行车单查询
	public void carSummaryOrderList(){
		Map orderMap = null;
		String driver = getPara("driver");
		String carNo = getPara("car_no");
		String transferOrderNo = getPara("transferOrderNo");
		String orderNo = getPara("carSummaryOrderNo");
		String departOrderNo = getPara("departOrderNo");
		String turnoutTime = getPara("turnout_time");
		
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String sqlTotal = "";
        String sql = "";
		if (driver == null && carNo == null && transferOrderNo == null && orderNo == null && turnoutTime == null && departOrderNo == null) {
			sqlTotal = "select count(0) total from car_summary_order where status='checked' and reimbursement_order_id is null";
			sql = "select cso.id,cso.order_no ,cso.status ,cso.car_no,cso.main_driver_name ,"
					+ " cso.month_refuel_amount, cso.deduct_apportion_amount, cso.actual_payment_amount,"
					+ "	(cso.next_start_car_amount + cso.month_refuel_amount) as total_cost ,"
					+ " (cso.finish_car_mileage - cso.start_car_mileage ) as carsummarymileage,"
					+ " (select group_concat(pickup_order_no separator '<br>' ) from car_summary_detail where car_summary_id = cso.id) as pickup_no,"
					+ " (select group_concat( dt.transfer_order_no separator '<br>' ) from depart_transfer dt"
					+ " where dt.pickup_id in(select pickup_order_id from car_summary_detail where car_summary_id = cso.id )) as transfer_order_no,"
					+ " (select turnout_time from depart_order where id = ( select min(pickup_order_id) from car_summary_detail where car_summary_id = cso.id)) as turnout_time,"
					+ " (select return_time from depart_order where id = ( select max(pickup_order_id) from car_summary_detail where car_summary_id = cso.id)) as return_time,"
					+ " (select sum(ifnull(toi.volume, p.volume) * toi.amount ) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id IN "
					+ " (select order_id from depart_transfer where pickup_id IN ( select pickup_order_id from car_summary_detail where car_summary_id = cso.id ))) as volume,"
					+ " (select sum( ifnull(nullif(toi.weight, 0),p.weight) * toi.amount) from transfer_order_item toi left join product p on p.id = toi.product_id where toi.order_id IN "
					+ " (select order_id from depart_transfer where pickup_id IN ( select pickup_order_id from car_summary_detail where car_summary_id = cso.id ))) as weight,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 2) refuel_consume,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 3) subsidy,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 4) driver_salary,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 5) toll_charge,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 6) handling_charges,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 7) fine,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 8) deliveryman_salary,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 9) parking_charge,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 10) quarterage,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 11) weighing_charge,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 12) other_charges"
					+ " from car_summary_order cso where cso.status='checked' and cso.reimbursement_order_id is null"
					+ " order by cso.create_data desc " + sLimit;
	        
		}else{
			sqlTotal = "select distinct count(0) total from car_summary_order cso"
					+ " left join car_summary_detail csd on csd.car_summary_id = cso.id"
					+ " left join depart_order dod on dod.id = csd.pickup_order_id "
					+ " left join depart_transfer dt on dt.pickup_id = dod.id"
					+ " left join transfer_order tor on tor.id = dt.order_id"
					+ "	where cso.status='checked' and cso.reimbursement_order_id is null"
					+ " and ifnull(cso.car_no, '') like '%"+carNo+"%'"
					+ " and ifnull(cso.main_driver_name, '') like '%"+driver+"%'"
					+ " and ifnull(cso.order_no, '') like '%"+orderNo+"%'"
					+ " and ifnull(tor.order_no, '') like '%"+transferOrderNo+"%'"
					+ " and ifnull(dod.turnout_time, '') like '%"+turnoutTime+"%'"
					+ " and ifnull(dod.depart_no, '') like '%"+departOrderNo+"%'";
			
			sql = "select distinct cso.id,cso.order_no ,cso.status ,cso.car_no,cso.main_driver_name ,"
					+ "cso.month_refuel_amount,cso.deduct_apportion_amount,cso.actual_payment_amount,"
					+ "	(cso.next_start_car_amount + cso.month_refuel_amount) as total_cost ,"
					+ " (cso.finish_car_mileage - cso.start_car_mileage ) as carsummarymileage,"
					+ " (select group_concat(pickup_order_no separator '<br>' ) from car_summary_detail where car_summary_id = cso.id) as pickup_no,"
					+ " (select group_concat( dt.transfer_order_no separator '<br>' ) from depart_transfer dt"
					+ " where dt.pickup_id in(select pickup_order_id from car_summary_detail where car_summary_id = cso.id )) as transfer_order_no,"
					+ " (select turnout_time from depart_order where id = ( select min(pickup_order_id) from car_summary_detail where car_summary_id = cso.id)) as turnout_time,"
					+ " (select return_time from depart_order where id = ( select max(pickup_order_id) from car_summary_detail where car_summary_id = cso.id)) as return_time,"
					+ " (select sum(ifnull(toi.volume, p.volume) * toi.amount ) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id IN "
					+ " (select order_id from depart_transfer where pickup_id IN ( select pickup_order_id from car_summary_detail where car_summary_id = cso.id ))) as volume,"
					+ " (select sum( ifnull(nullif(toi.weight, 0),p.weight) * toi.amount) from transfer_order_item toi left join product p on p.id = toi.product_id where toi.order_id IN "
					+ " (select order_id from depart_transfer where pickup_id IN ( select pickup_order_id from car_summary_detail where car_summary_id = cso.id ))) as weight,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 2) refuel_consume,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 3) subsidy,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 4) driver_salary,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 5) toll_charge,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 6) handling_charges,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 7) fine,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 8) deliveryman_salary,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 9) parking_charge,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 10) quarterage,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 11) weighing_charge,"
					+ " (select amount from car_summary_detail_other_fee where car_summary_id = cso.id and item = 12) other_charges"
					+ " from car_summary_order cso"
					+ " left join car_summary_detail csd on csd.car_summary_id = cso.id"
					+ " left join depart_order dod on dod.id = csd.pickup_order_id "
					+ " left join depart_transfer dt on dt.pickup_id = dod.id"
					+ " left join transfer_order tor on tor.id = dt.order_id"
					+ "	where cso.status='checked' and cso.reimbursement_order_id is null"
					+ " and ifnull(cso.car_no, '') like '%"+carNo+"%'"
					+ " and ifnull(cso.main_driver_name, '') like '%"+driver+"%'"
					+ " and ifnull(cso.order_no, '') like '%"+orderNo+"%'"
					+ " and ifnull(tor.order_no, '') like '%"+transferOrderNo+"%'"
					+ " and ifnull(dod.turnout_time, '') like '%"+turnoutTime+"%'"
					+ " and ifnull(dod.depart_no, '') like '%"+departOrderNo+"%'"
					+ " order by cso.create_data desc " + sLimit;
		}
		Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orders = Db.find(sql);
		
	 	orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
	}
	
	//编辑行车报销单
	public void edit(){
		String orderId = getPara("orderId");
		ReimbursementOrder order = ReimbursementOrder.dao.findById(orderId);
				
        setAttr("create_by", LoginUserController.getUserNameById(order.get("create_id").toString()));
        setAttr("audit_name", LoginUserController.getUserNameById(order.get("create_id").toString()));
		setAttr("order", order);
		setAttr("car_summary_order_ids", order.get("car_summary_order_ids") == null?"":order.get("car_summary_order_ids"));
		render("/yh/carmanage/carReimbursementEdit.html");
	}
	
	//行车报销单-审核
	public void audit(){
		String orderId = getPara("orderId");
		ReimbursementOrder order = ReimbursementOrder.dao.findById(orderId);
		
		order.set("audit_id", LoginUserController.getLoginUserId(this));
		order.set("status", ReimbursementOrder.ORDER_STATUS_AUDIT);
		order.set("audit_stamp", new Date());
		order.update();
		
		renderJson("{\"audit_name\":\""+LoginUserController.getLoginUserName(this)+"\", \"status\":\"已审核\"}");
	}
	
	//创建行车报销单
	public void createCarReimbursement(){
		String car_summary_order_ids = getPara("carSummeryIds");
		String[] ids=car_summary_order_ids.split(",");
		
		setAttr("car_summary_order_ids", car_summary_order_ids);
		render("/yh/carmanage/carReimbursementEdit.html");
	}
	
	//保存行车报销单
	public void saveCarReimbursement(){
		ReimbursementOrder order = null;
		try {
			String orderId = getPara("orderId");
			String car_summary_order_ids = getPara("car_summary_order_ids");
			
			if(orderId==null || "".equals(orderId)){
				order = new ReimbursementOrder();
				//行车报销单号
				String orderNo = OrderNoGenerator.getNextOrderNo("XCBX");
				order.set("order_no", orderNo);
				order.set("car_summary_order_ids", car_summary_order_ids);				
				order.set("create_id", LoginUserController.getLoginUserId(this));
				order.set("status", ReimbursementOrder.ORDER_STATUS_NEW);
				order.set("create_stamp", new Date());
				order.set("remark", getPara("remark"));
				order.save();
				
				//更新行车单
				Db.update("update car_summary_order set reimbursement_order_id="+order.getLong("id")+"  where id in("+car_summary_order_ids+")");
			}else{
				order = ReimbursementOrder.dao.findById(orderId);
				order.set("remark", getPara("remark"));
				order.update();
			}
					
			//order.set("creator", "test");
			renderJson(order);
		} catch (Throwable e) {
			//TODO: com.jfinal.plugin.activerecord.ActiveRecordException 在这里扑捉不到
			order = new ReimbursementOrder();
			e.printStackTrace();
			renderJson(order);
		}
	}
}
