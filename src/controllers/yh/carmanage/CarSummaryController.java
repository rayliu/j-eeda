package controllers.yh.carmanage;

import interceptor.SetAttrLoginUserInterceptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.ParentOfficeModel;
import models.TransferOrder;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.carmanage.CarSummaryDetail;
import models.yh.carmanage.CarSummaryDetailOilFee;
import models.yh.carmanage.CarSummaryDetailOtherFee;
import models.yh.carmanage.CarSummaryDetailRouteFee;
import models.yh.carmanage.CarSummaryDetailSalary;
import models.yh.carmanage.CarSummaryOrder;
import models.yh.delivery.DeliveryOrder;
import models.yh.pickup.PickupDriverAssistant;
import models.yh.profile.DriverAssistant;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.profile.CarinfoController;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CarSummaryController extends Controller {
	private Logger logger = Logger.getLogger(CarinfoController.class);
	Subject currentUser = SecurityUtils.getSubject();
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CS_LIST, PermissionConstant.PERMSSION_CS_CREATE,PermissionConstant.PERMSSION_CS_UPDATE}, logical=Logical.OR)
	public void index() {
       render("/yh/carmanage/carSummaryList.html");
    }
	
	//未处理调车单
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CS_CREATE})
	public void untreatedCarManageList(){
		
		Map orderMap = null;
		String status = getPara("status")==null?"":getPara("status").trim();
		String driver = getPara("driver")==null?"":getPara("driver").trim();
		String car_no = getPara("car_no")==null?"":getPara("car_no").trim();
		String transferOrderNo = getPara("transferOrderNo")==null?"":getPara("transferOrderNo").trim();
		String turnout_time = getPara("create_stamp")==null?"":getPara("create_stamp").trim();
		
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sql = "";
        String sqlTotal = "";
        String condition = " ";
        // 获取总条数
	    sqlTotal =" SELECT count(1) total from (SELECT dor.id, o.id oid,t_o.customer_id, dor.depart_no,ifnull(u.c_name, u.user_name) user_name,dor.remark,(SELECT group_concat(dt.transfer_order_no SEPARATOR '<br>') FROM depart_transfer dt WHERE pickup_id = dor.id) AS transfer_order_no,"
		        + " dor. STATUS,dor.car_no,dor.driver contact_person,dor.phone phone,dor.car_type cartype,dor.turnout_time,o.office_name office_name,(SELECT round(sum(ifnull(toi.volume, 0) * (dtf.amount / toi.amount)),2) FROM transfer_order_item toi LEFT JOIN depart_transfer dtf ON dtf.order_item_id = toi.id LEFT JOIN transfer_order t ON t.id = toi.order_id WHERE dtf.pickup_id = dor.id AND t.cargo_nature = 'cargo') cargovolume,"
		        + " (SELECT round(sum(ifnull(toi.sum_weight, 0) * (dtf.amount / toi.amount)),2) FROM transfer_order_item toi LEFT JOIN depart_transfer dtf ON dtf.order_item_id = toi.id LEFT JOIN transfer_order t ON t.id = toi.order_id WHERE dtf.pickup_id = dor.id AND t.cargo_nature = 'cargo') cargoweight,"
		        + " round((SELECT sum(ifnull(volume, 0)) FROM transfer_order_item_detail WHERE pickup_id = dor.id),2) atmvolume,round((SELECT sum(ifnull(weight, 0)) FROM transfer_order_item_detail WHERE pickup_id = dor.id),2) atmweight"
		        + " FROM depart_order dor"
		        + " LEFT JOIN party p ON dor.driver_id = p.id"
		        + " LEFT JOIN contact ct ON p.contact_id = ct.id"
		        + " LEFT JOIN user_login u ON u.id = dor.create_by"
		        + " LEFT JOIN depart_transfer dtf ON dtf.pickup_id = dor.id"
		        + " LEFT JOIN transfer_order t_o ON t_o.id = dtf.order_id"
		        + " LEFT JOIN office o ON o.id = t_o.office_id"
		        + " WHERE dor. STATUS != '取消' AND dor.car_summary_type = 'untreated' AND combine_type = 'PICKUP'"
		        + " AND ( dor. STATUS = '已入货场' OR dor. STATUS = '已入库'OR dor. STATUS = '已收货')AND dor.pickup_mode = 'own'"
		        + " GROUP BY dor.id, dor.car_no "
		        + " UNION"
		        + " SELECT dor.id, o.id oid, t_o.customer_id,dor.order_no, ifnull(ul.c_name, ul.user_name) user_name,dor.remark,(SELECT group_concat(tor.order_no SEPARATOR '<br>') FROM delivery_order_item doi LEFT JOIN transfer_order tor on tor.id=doi.transfer_order_id WHERE doi.delivery_id = dor.id) AS transfer_order_no,"
		        + " dor.`STATUS`,cf.car_no,cf.driver,cf.phone,'' cartype,dor.business_stamp,o.office_name, ROUND((SELECT ifnull(toi.volume, 0) * ifnull(sum(doi.amount),0) FROM delivery_order_item doi LEFT JOIN transfer_order_item toi on toi.id=doi.transfer_item_id WHERE doi.delivery_id = dor.id),2) AS cargovolume,"
		        + " ROUND((SELECT ifnull(toi.weight, 0) * ifnull(sum(doi.amount),0) FROM delivery_order_item doi LEFT JOIN transfer_order_item toi on toi.id=doi.transfer_item_id WHERE doi.delivery_id = dor.id),2) AS cargoweight,0 atmvolume,0 atmweight"
		        + " FROM delivery_order dor"
		        + " LEFT JOIN delivery_order_item doi on doi.delivery_id=dor.id"
		        + " LEFT JOIN transfer_order t_o on t_o.id=doi.transfer_order_id"
		        + " LEFT JOIN carinfo cf on cf.id=dor.car_id"
		        + " LEFT JOIN office o ON o.id = dor.office_id"
		        + " LEFT JOIN user_login ul ON ul.id = dor.create_by"
		        + " where dor.deliveryMode='own' AND ifnull(dor.car_summary_type,'untreated') != 'processed' ) a "
		        + " where oid IN (SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"')"
		        + " AND customer_id IN (SELECT customer_id FROM user_customer WHERE user_name = '"+currentUser.getPrincipal()+"')";
	       // 获取当前页的数据
	     sql = " SELECT * from (SELECT dor.id, o.id oid,'提货' order_type,t_o.customer_id, dor.depart_no,ifnull(u.c_name, u.user_name) user_name,dor.remark,(SELECT group_concat(dt.transfer_order_no SEPARATOR '<br>') FROM depart_transfer dt WHERE pickup_id = dor.id) AS transfer_order_no,"
	        	+ " dor. STATUS,dor.car_no,dor.driver contact_person,dor.phone phone,dor.car_type cartype,dor.turnout_time,o.office_name office_name,(SELECT round(sum(ifnull(toi.volume, 0) * (dtf.amount / toi.amount)),2) FROM transfer_order_item toi LEFT JOIN depart_transfer dtf ON dtf.order_item_id = toi.id LEFT JOIN transfer_order t ON t.id = toi.order_id WHERE dtf.pickup_id = dor.id AND t.cargo_nature = 'cargo') cargovolume,"
	        	+ " (SELECT round(sum(ifnull(toi.sum_weight, 0) * (dtf.amount / toi.amount)),2) FROM transfer_order_item toi LEFT JOIN depart_transfer dtf ON dtf.order_item_id = toi.id LEFT JOIN transfer_order t ON t.id = toi.order_id WHERE dtf.pickup_id = dor.id AND t.cargo_nature = 'cargo') cargoweight,"
	        	+ " round((SELECT sum(ifnull(volume, 0)) FROM transfer_order_item_detail WHERE pickup_id = dor.id),2) atmvolume,round((SELECT sum(ifnull(weight, 0)) FROM transfer_order_item_detail WHERE pickup_id = dor.id),2) atmweight"
	        	+ " FROM depart_order dor"
	        	+ " LEFT JOIN party p ON dor.driver_id = p.id"
	        	+ " LEFT JOIN contact ct ON p.contact_id = ct.id"
	        	+ " LEFT JOIN user_login u ON u.id = dor.create_by"
	        	+ " LEFT JOIN depart_transfer dtf ON dtf.pickup_id = dor.id"
	        	+ " LEFT JOIN transfer_order t_o ON t_o.id = dtf.order_id"
	        	+ " LEFT JOIN office o ON o.id = t_o.office_id"
	        	+ " WHERE dor. STATUS != '取消' AND dor.car_summary_type = 'untreated' AND combine_type = 'PICKUP'"
	        	+ " AND ( dor. STATUS = '已入货场' OR dor. STATUS = '已入库'OR dor. STATUS = '已收货')AND dor.pickup_mode = 'own'"
	        	+ " GROUP BY dor.id, dor.car_no "
	        	+ " UNION"
	        	+ " SELECT dor.id, o.id oid,'配送' order_type, t_o.customer_id,dor.order_no, ifnull(ul.c_name, ul.user_name) user_name,dor.remark,(SELECT group_concat(tor.order_no SEPARATOR '<br>') FROM delivery_order_item doi LEFT JOIN transfer_order tor on tor.id=doi.transfer_order_id WHERE doi.delivery_id = dor.id) AS transfer_order_no,"
	        	+ " dor.`STATUS`,cf.car_no,cf.driver contact_person,cf.phone,'' cartype,dor.business_stamp,o.office_name, ROUND((SELECT ifnull(toi.volume, 0) * ifnull(sum(doi.amount),0) FROM delivery_order_item doi LEFT JOIN transfer_order_item toi on toi.id=doi.transfer_item_id WHERE doi.delivery_id = dor.id),2) AS cargovolume,"
	        	+ " ROUND((SELECT ifnull(toi.weight, 0) * ifnull(sum(doi.amount),0) FROM delivery_order_item doi LEFT JOIN transfer_order_item toi on toi.id=doi.transfer_item_id WHERE doi.delivery_id = dor.id),2) AS cargoweight,0 atmvolume,0 atmweight"
	        	+ " FROM delivery_order dor"
	        	+ " LEFT JOIN delivery_order_item doi on doi.delivery_id=dor.id"
	        	+ " LEFT JOIN transfer_order t_o on t_o.id=doi.transfer_order_id"
	        	+ " LEFT JOIN carinfo cf on cf.id=dor.car_id"
	        	+ " LEFT JOIN office o ON o.id = dor.office_id"
	        	+ " LEFT JOIN user_login ul ON ul.id = dor.create_by"
	        	+ " where dor.deliveryMode='own' AND ifnull(dor.car_summary_type,'untreated') != 'processed') a "
	        	+ " where oid IN (SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"')"
	        	+ " AND customer_id IN (SELECT customer_id FROM user_customer WHERE user_name = '"+currentUser.getPrincipal()+"')";
	     if (driver != null &&!"".equals(driver)) {	
	    	 condition +=" AND ifnull(contact_person,'') like'%"+ driver.trim()+ "%'";
	     }
	     if (status != null &&!"".equals(status)) {	
	    	 condition +=" AND ifnull(status,'') like'%"+ status.trim()+ "%'";
	     }
	     if (car_no != null &&!"".equals(car_no)) {	
	    	 condition +=" AND ifnull(car_no,'') like'%"+ car_no.trim()+ "%'";
	     }
	     if (transferOrderNo != null &&!"".equals(transferOrderNo)) {	
	    	 condition +=" AND ifnull(transfer_order_no,'') like'%"+ transferOrderNo.trim()+ "%'";
	     }
	     if (turnout_time != null &&!"".equals(turnout_time)) {	
	    	 condition +=" AND ifnull(turnout_time,'') like'%"+ turnout_time+ "%'";
	     }
        List<Record> orders = Db.find(sql+condition + sLimit);
        Record rec = Db.findFirst(sqlTotal+condition);
	 	orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
		
	}
	
	//行车单查询
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CS_LIST})
	public void carSummaryOrderList(){
		
		Map orderMap = null;
		String status = getPara("status");
		String driver = getPara("driver");
		String car_no = getPara("car_no");
		String transferOrderNo = getPara("transferOrderNo");
		String order_no = getPara("order_no");
		String start_data = getPara("start_data");
		 
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String sqlTotal = "";
        String sql = "";
        String condition = " where 1=1 ";
        sqlTotal = "select count(1) total from (select distinct cso.id,cso.order_no ,cso.status ,cso.car_no,cso.main_driver_name ,"
				+ "cso.month_refuel_amount,cso.deduct_apportion_amount,cso.actual_payment_amount,"
				+ "	round((cso.next_start_car_amount + cso.month_refuel_amount),2) AS total_cost,"
				+ " round((cso.finish_car_mileage - cso.start_car_mileage),2) AS carsummarymileage,"
				+ " (select group_concat(pickup_order_no separator '<br>' ) from car_summary_detail where car_summary_id = cso.id) as pickup_no,"
				+ " (SELECT GROUP_CONCAT(DISTINCT ifnull(dt.transfer_order_no,doi.transfer_no) SEPARATOR '<br>') FROM car_summary_detail csd LEFT JOIN depart_transfer dt ON dt.pickup_id = csd.pickup_order_id and csd.pickup_type='提货' LEFT JOIN delivery_order_item doi on doi.delivery_id=csd.pickup_order_id and csd.pickup_type='配送'  WHERE csd.car_summary_id = cso.id) AS transfer_order_no,"
				+ " (select turnout_time from depart_order where id = ( select min(pickup_order_id) from car_summary_detail where car_summary_id = cso.id and pickup_type = '提货')) as turnout_time,"
				+ " (select return_time from depart_order where id = ( select max(pickup_order_id) from car_summary_detail where car_summary_id = cso.id and pickup_type = '提货')) as return_time,"
				+ " ROUND(((SELECT(SELECT ifnull(count(toid.id)*toid.volume,0) FROM transfer_order_item_detail toid LEFT JOIN car_summary_detail csd ON toid.pickup_id = csd.pickup_order_id WHERE csd.car_summary_id = cso.id  AND csd.pickup_type = '提货'))+"
				+ " ((SELECT(SELECT ifnull(count(toid.id)*toid.volume,0) FROM transfer_order_item_detail toid LEFT JOIN car_summary_detail csd ON toid.delivery_id = csd.pickup_order_id WHERE csd.car_summary_id = cso.id  AND csd.pickup_type = '配送')))),2) volume,"
				+ " ROUND(((SELECT(SELECT ifnull(count(toid.id)*toid.weight,0) FROM transfer_order_item_detail toid LEFT JOIN car_summary_detail csd ON toid.pickup_id = csd.pickup_order_id WHERE csd.car_summary_id = cso.id  AND csd.pickup_type = '提货'))+"
				+ " ((SELECT(SELECT ifnull(count(toid.id)*toid.weight,0) FROM transfer_order_item_detail toid LEFT JOIN car_summary_detail csd ON toid.delivery_id = csd.pickup_order_id WHERE csd.car_summary_id = cso.id  AND csd.pickup_type = '配送')))),2) weight,"
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
				+ " from car_summary_order cso "
				+ " order by cso.create_data desc ) a " ;
		sql = " select * from (select distinct cso.id,cso.order_no ,cso.status ,cso.car_no,cso.main_driver_name ,"
				+ "cso.month_refuel_amount,cso.deduct_apportion_amount,cso.actual_payment_amount,"
				+ "	round((cso.next_start_car_amount + cso.month_refuel_amount),2) AS total_cost,"
				+ " round((cso.finish_car_mileage - cso.start_car_mileage),2) AS carsummarymileage,"
				+ " (select group_concat(pickup_order_no separator '<br>' ) from car_summary_detail where car_summary_id = cso.id) as pickup_no,"
				+ " (SELECT GROUP_CONCAT(DISTINCT ifnull(dt.transfer_order_no,doi.transfer_no) SEPARATOR '<br>') FROM car_summary_detail csd LEFT JOIN depart_transfer dt ON dt.pickup_id = csd.pickup_order_id and csd.pickup_type='提货' LEFT JOIN delivery_order_item doi on doi.delivery_id=csd.pickup_order_id and csd.pickup_type='配送'  WHERE csd.car_summary_id = cso.id) AS transfer_order_no,"
				+ " (select turnout_time from depart_order where id = ( select min(pickup_order_id) from car_summary_detail where car_summary_id = cso.id and pickup_type = '提货')) as turnout_time,"
				+ " (select return_time from depart_order where id = ( select max(pickup_order_id) from car_summary_detail where car_summary_id = cso.id and pickup_type = '提货')) as return_time,"
				+ " ROUND(((SELECT(SELECT ifnull(count(toid.id)*toid.volume,0) FROM transfer_order_item_detail toid LEFT JOIN car_summary_detail csd ON toid.pickup_id = csd.pickup_order_id WHERE csd.car_summary_id = cso.id  AND csd.pickup_type = '提货'))+"
				+ " ((SELECT(SELECT ifnull(count(toid.id)*toid.volume,0) FROM transfer_order_item_detail toid LEFT JOIN car_summary_detail csd ON toid.delivery_id = csd.pickup_order_id WHERE csd.car_summary_id = cso.id  AND csd.pickup_type = '配送')))),2) volume,"
				+ " ROUND(((SELECT(SELECT ifnull(count(toid.id)*toid.weight,0) FROM transfer_order_item_detail toid LEFT JOIN car_summary_detail csd ON toid.pickup_id = csd.pickup_order_id WHERE csd.car_summary_id = cso.id  AND csd.pickup_type = '提货'))+"
				+ " ((SELECT(SELECT ifnull(count(toid.id)*toid.weight,0) FROM transfer_order_item_detail toid LEFT JOIN car_summary_detail csd ON toid.delivery_id = csd.pickup_order_id WHERE csd.car_summary_id = cso.id  AND csd.pickup_type = '配送')))),2) weight,"
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
				+ " from car_summary_order cso "
				+ " order by cso.create_data desc ) a " ;
		if (driver != null ) {
			condition +=" AND ifnull(main_driver_name,'') like'%"+ driver.trim()+ "%'";
		}
		if (status != null ) {
			condition +=" AND ifnull(status,'') like'%"+ status.trim()+ "%'";
		}
		if (car_no != null  ) {
			condition +=" AND ifnull(car_no,'') like'%"+ car_no.trim()+ "%'";
		}
		if (transferOrderNo != null) {
			condition +=" AND ifnull(transfer_order_no,'') like'%"+ transferOrderNo.trim()+ "%'";
		}
		if (order_no != null) {
			condition +=" AND ifnull(order_no,'') like'%"+ order_no.trim()+ "%'";
		}
		if (start_data != null ) {
			condition +=" AND ifnull(turnout_time,'') like'%"+ start_data.trim()+ "%'";
		}
		Record rec = Db.findFirst(sqlTotal+condition);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orders = Db.find(sql+condition+sLimit);
		
	 	orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
		
		
	}
	
	//创建行车单
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CS_CREATE})
	public void createCarSummary(){
		String pickupIdsArray = getPara("pickupIds");
		String orderTypesArray = getPara("orderTypes");
		setAttr("pickupIds", pickupIdsArray);
		setAttr("orderTypes", orderTypesArray);
		if(!"".equals(pickupIdsArray) && pickupIdsArray != null){
			try {
				String pickupIds[] = pickupIdsArray.split(",");
				String orderTypes[] = orderTypesArray.split(",");
				int num = 0;
				for (int i = 0; i < pickupIds.length; i++) {
					DeliveryOrder deliveryOrder= DeliveryOrder.dao.findFirst("SELECT c.car_no,c.driver from delivery_order d LEFT JOIN carinfo c on c.id=d.car_id where d.id=?",pickupIds[i]);
					if("配送".equals(orderTypes[i])){
						if(num == 0){
							//车牌号
							setAttr("car_no", deliveryOrder.get("car_no"));
							//主司机姓名
							setAttr("driver", deliveryOrder.get("driver"));
						}
					}else{
						DepartOrder departOrder = DepartOrder.dao.findById(pickupIds[i]);
						if(num == 0){
							//车牌号
							setAttr("car_no", departOrder.get("car_no"));
							//主司机姓名
							setAttr("driver", departOrder.get("driver"));
						}
					}
					num++;
				}
				//出车次
				setAttr("carNumber", pickupIds.length);
				//是否审核 isAudit
				setAttr("isAudit", "no");
				
			} catch (Exception e) {
				
			}
		}
		
		render("/yh/carmanage/carSummaryEdit.html");
	}
	
	//保存行车单
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CS_CREATE, PermissionConstant.PERMSSION_CS_UPDATE}, logical=Logical.OR)
	public void saveCarSummary(){
		//拼车单id
		String pickupIdArray = getPara("pickupIds");
		String pickupIds[] = pickupIdArray.split(",");
		//拼车单类型
		String orderTypeArray = getPara("orderTypes");
		String orderTypes[] = orderTypeArray.split(",");
		//行车单信息
        String carSummaryId = getPara("car_summary_id");
        String carNo = getPara("car_no");
        String mainDriverName = getPara("main_driver_name");
        String mainDriverAmount = getPara("main_driver_amount").equals("") ?"0":getPara("main_driver_amount");
        String minorDriverName = getPara("minor_driver_name");
        String minorDriverAmount = getPara("minor_driver_amount").equals("") ?"0":getPara("minor_driver_amount");
        String startCarMileage = getPara("start_car_mileage").equals("") ?"0":getPara("start_car_mileage");
        String finishCarMileage = getPara("finish_car_mileage").equals("") ?"0":getPara("finish_car_mileage");
        String monthStartCarNext = getPara("month_start_car_next").equals("") ?"0":getPara("month_start_car_next");
        String monthCarRunMileage = getPara("month_car_run_mileage").equals("") ?"0":getPara("month_car_run_mileage");
        String monthRefuelAmount = getPara("month_refuel_amount").equals("") ?"0":getPara("month_refuel_amount");
        String nextStartCarAmount = getPara("next_start_car_amount").equals("") ?"0":getPara("next_start_car_amount");
        String deductApportionAmount = getPara("deduct_apportion_amount").equals("") ?"0":getPara("deduct_apportion_amount");
        String actualPaymentAmount = getPara("actual_payment_amount").equals("") ?"0":getPara("actual_payment_amount");
        String orderNo = null;
        CarSummaryOrder carSummaryOrder = null;
        if(carSummaryId.equals("") || carSummaryId == null){ //新建时
        	carSummaryOrder = new CarSummaryOrder();
        	java.util.Date utilDate = new java.util.Date();
        	java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        	//调车单号
        	orderNo = OrderNoGenerator.getNextOrderNo("XC");
        	
        	carSummaryOrder.set("order_no", orderNo).set("car_no", carNo).set("main_driver_name", mainDriverName)
            		.set("main_driver_amount", mainDriverAmount).set("minor_driver_name", minorDriverName)
            		.set("minor_driver_amount", minorDriverAmount).set("start_car_mileage", startCarMileage)
            		.set("finish_car_mileage", finishCarMileage).set("month_start_car_next", monthStartCarNext)
            		.set("month_car_run_mileage", monthCarRunMileage).set("month_refuel_amount", monthRefuelAmount)
            		.set("next_start_car_amount", nextStartCarAmount).set("deduct_apportion_amount", deductApportionAmount)
            		.set("actual_payment_amount", actualPaymentAmount).set("create_data", sqlDate)
            		.set("status", "新建").save();
        	
    		long id = carSummaryOrder.getLong("id");
    		List<Long> orderIds = new ArrayList<Long>();
    		
    		for (int i = 0; i < pickupIds.length; i++) {
    			//修改调车单状态为：已处理
    			if("配送".equals(orderTypes[i])){
    				DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(pickupIds[i]);
    				deliveryOrder.set("car_summary_type", "processed");
    				deliveryOrder.update();
    				//插入从表数据
	    			CarSummaryDetail carSummaryDetail = new CarSummaryDetail();
	    			carSummaryDetail.set("car_summary_id", id);
	    			carSummaryDetail.set("pickup_order_id", deliveryOrder.get("id"));
	    			carSummaryDetail.set("pickup_order_no", deliveryOrder.get("order_no"));
	    			carSummaryDetail.set("pickup_type", "配送");
	    			carSummaryDetail.save();
	    			//记录运输单id
	    			List<Record> recList = Db.find("SELECT doi.transfer_order_id FROM delivery_order d LEFT JOIN delivery_order_item doi ON doi.delivery_id = d.id WHERE d.id =?",deliveryOrder.get("id"));
	    			for (Record record : recList) {
	    				orderIds.add(record.getLong("transfer_order_id"));
					}
    			}else{
	    			DepartOrder departOrder = DepartOrder.dao.findById(pickupIds[i]);
	    			departOrder.set("car_summary_type", "processed");
	    			departOrder.update();
	    			//插入从表数据
	    			CarSummaryDetail carSummaryDetail = new CarSummaryDetail();
	    			carSummaryDetail.set("car_summary_id", id);
	    			carSummaryDetail.set("pickup_order_id", departOrder.get("id"));
	    			carSummaryDetail.set("pickup_order_no", departOrder.get("depart_no"));
	    			carSummaryDetail.set("pickup_type", "提货");
	    			carSummaryDetail.save();
	    			//记录运输单id
	    			List<Record> recList = Db.find("select distinct order_id from depart_transfer where pickup_id = "+departOrder.get("id"));
	    			for (Record record : recList) {
	    				orderIds.add(record.getLong("order_id"));
					}
    			}
    			//送货员工资明细
    			List<PickupDriverAssistant> assistantList = PickupDriverAssistant.dao.find("select * from pickup_driver_assistant where pickup_id = ?",pickupIds[i]);
    			for (PickupDriverAssistant pickupDriverAssistant : assistantList) {
    				if(!"".equals(pickupDriverAssistant.get("driver_assistant_id")) && pickupDriverAssistant.get("driver_assistant_id") != null){
        				DriverAssistant driver = DriverAssistant.dao.findById(pickupDriverAssistant.get("driver_assistant_id"));
        				CarSummaryDetailSalary salary = new CarSummaryDetailSalary();
        				salary.set("car_summary_id", id)
        				.set("username", pickupDriverAssistant.get("name"))
						.set("work_type", "跟车人员")
						.set("create_data", new Date());
        				if(driver != null){
							salary.set("deserved_amount", driver.get("daily_wage"));
						}
        				salary.save();
    				}else{
    					CarSummaryDetailSalary salary = new CarSummaryDetailSalary();
        				salary.set("car_summary_id", id)
        				.set("username", pickupDriverAssistant.get("name"))
						.set("work_type", "跟车人员")
						.set("create_data", new Date())
						.save();
    				}
				}
			}
    		//创建费用合计表初始数据
    		initCarSummaryDetailOtherFeeData(id);
    		//创建行车里程碑
    		saveCarSummaryOrderMilestone(id,carSummaryOrder.CAR_SUMMARY_SYSTEM_NEW);
    		//设置默认运输单分摊比例
    		double number = 1.0D/orderIds.size();
    		BigDecimal b = new BigDecimal(number); 
    		double rate = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();  
    		for (Long orderId : orderIds) {
    			TransferOrder transferOrder = TransferOrder.dao.findById(orderId);
				transferOrder.set("car_summary_order_share_ratio", rate);
				transferOrder.update();
			}
        }else{//修改时
        	carSummaryOrder = CarSummaryOrder.dao.findById(carSummaryId);
        	if(carSummaryOrder != null){
        		carSummaryOrder.set("main_driver_name", mainDriverName)
	    		.set("main_driver_amount", mainDriverAmount).set("minor_driver_name", minorDriverName)
	    		.set("minor_driver_amount", minorDriverAmount).set("start_car_mileage", startCarMileage)
	    		.set("finish_car_mileage", finishCarMileage).set("month_start_car_next", monthStartCarNext)
	    		.set("month_car_run_mileage", monthCarRunMileage).set("month_refuel_amount", monthRefuelAmount)
	    		.set("next_start_car_amount", nextStartCarAmount).set("deduct_apportion_amount", deductApportionAmount)
	    		.set("actual_payment_amount", actualPaymentAmount).update();
        	}
        }
        carSummaryId = carSummaryOrder.getLong("id").toString();
        //修改费用合计中的司机工资
        if(!"0".equals(mainDriverAmount) || !"0".equals(minorDriverAmount) ){
	        double amount = Double.parseDouble(mainDriverAmount)+Double.parseDouble(minorDriverAmount);
			String sql="update car_summary_detail_other_fee set amount = "+amount+" where amount_item  = '司机工资' and car_summary_id = "+carSummaryId;
			Db.update(sql);
        }
        //计算本次油耗
		if(!"0".equals(monthCarRunMileage)){
			updateNextFuelStandard(carSummaryId);
		}
        renderJson(carSummaryOrder);
	}
	
	//查询所有自营副司机
	public void searchAllDriver(){
		List<Record> carinfoList = Db.find("select c.* from carinfo c left join office o on c.office_id = o.id where c.type = 'OWN' and (o.id = " + pom.getParentOfficeId() + " or o.belong_office = " + pom.getParentOfficeId() + ")");
		renderJson(carinfoList);
	}
	
	//线路查询
	public void findAllAddress(){
		String pickupIdsArr = getPara("pickupIds");
		String pickupIds[] = pickupIdsArr.split(",");
		String orderTypesArr = getPara("orderTypes");
		String orderTypes[] = orderTypesArr.split(",");
		String pickupId="";
		String deliveryId="";
		for(int i=0; i<pickupIds.length;i++){
			if("配送".equals(orderTypes[i])){
				deliveryId+=pickupIds[i];
				deliveryId+=",";
			}else{
				pickupId+=pickupIds[i];
				pickupId+=",";
			}
		}
		if(!"".equals(pickupId)){
			pickupId = pickupId.substring(0,pickupId.length()-1);
		}else{
			pickupId="-1";
		}
		if(!"".equals(deliveryId)){
			deliveryId = deliveryId.substring(0,deliveryId.length()-1);
		}else{
			deliveryId="-1";
		}
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "SELECT COUNT(1) total from (SELECT dor.id FROM depart_order dor WHERE dor.id IN ("+pickupId+") UNION SELECT dor.id FROM delivery_order dor WHERE dor.id IN ("+deliveryId+")) a";
    	 
        String sql = "SELECT DISTINCT dor.depart_no, dor.create_stamp,tr.order_no,c.abbr,tr.address transferaddress,dor.address pickupaddress,"
        		+ " (SELECT warehouse_name FROM warehouse WHERE id = tr.warehouse_id) warehousename"
        		+ " FROM depart_order dor"
        		+ " LEFT JOIN depart_transfer dt ON dt.pickup_id = dor.id"
        		+ " LEFT JOIN transfer_order tr ON tr.id = dt.order_id"
        		+ " LEFT JOIN party p ON p.id = tr.customer_id"
        		+ " LEFT JOIN contact c ON c.id = p.contact_id"
        		+ " WHERE dor.id IN ("+pickupId+")"
        		+ " UNION"
        		+ " SELECT DISTINCT dor.order_no depart_no,dor.create_stamp,tr.order_no,c.abbr,tr.address transferaddress,l.`name` pickupaddress,"
        		+ " (SELECT warehouse_name FROM warehouse WHERE id = tr.warehouse_id) warehousename"
        		+ " FROM delivery_order dor"
        		+ " LEFT JOIN delivery_order_item doi on doi.delivery_id=dor.id"
        		+ " LEFT JOIN transfer_order tr on tr.id=doi.transfer_order_id"
        		+ " LEFT JOIN location l on l.`code`=dor.route_to"
        		+ " LEFT JOIN party p ON p.id = dor.customer_id"
        		+ " LEFT JOIN contact c ON c.id = p.contact_id"
        		+ " WHERE dor.id IN ("+deliveryId+")";
         
		Record rec = Db.findFirst(sqlTotal);
		List<Record> orders = Db.find(sql);
        
        HashMap orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
		
	}
	// 货品信息
    public void findPickupOrderItems() {
    	String pickupIdsArr = getPara("pickupIds");
		String pickupIds[] = pickupIdsArr.split(",");
		String orderTypesArr = getPara("orderTypes");
		String orderTypes[] = orderTypesArr.split(",");
		String pickupId="";
		String deliveryId="";
		for(int i=0; i<pickupIds.length;i++){
			if("配送".equals(orderTypes[i])){
				deliveryId+=pickupIds[i];
				deliveryId+=",";
			}else{
				pickupId+=pickupIds[i];
				pickupId+=",";
			}
		}
		if(!"".equals(pickupId)){
			pickupId = pickupId.substring(0,pickupId.length()-1);
		}else{
			pickupId="-1";
		}
		if(!"".equals(deliveryId)){
			deliveryId = deliveryId.substring(0,deliveryId.length()-1);
		}else{
			deliveryId="-1";
		}
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(distinct toi.id) total from transfer_order_item toi"
        		+ " left join depart_transfer dt on dt.order_id = toi.order_id"
        		+ " left join depart_order dor on dor.id = dt.pickup_id"
        		+ " left join transfer_order tor on tor.id = dt.order_id "
        		+ " where dt.pickup_id in(" + pickupId + ")";
        
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        //当运输单多个产品的时候会有问题
        String sql = "select distinct toi.id,dor.depart_no,tor.order_no,c.abbr customer,ifnull(toi.item_no, pd.item_no) item_no,toi.item_name,toi.remark,tor.cargo_nature, "
        		+ " (select count(0) total from transfer_order_item_detail where order_id = tor.id and item_id = toi.id and pickup_id = dt.pickup_id) atmamount,"
        		+ " (select round(ifnull(pd.volume * count(0), 0), 2) from transfer_order_item_detail where order_id = tor.id and item_id = toi.id and pickup_id = dt.pickup_id) atmvolume,"
        		+ " (select round(ifnull(pd.weight * count(0), 0), 2) from transfer_order_item_detail where order_id = tor.id and item_id = toi.id and pickup_id = dt.pickup_id) atmweight,"
        		+ " (select ifnull(d.amount,0) from transfer_order_item t left join depart_transfer d on d.order_item_id = t.id where t.id = toi.id and d.pickup_id = dt.pickup_id) cargoamount,"
        		+ " (select round(ifnull(pd.volume,0)*ifnull(d.amount,0),2) from transfer_order_item t left join depart_transfer d on d.order_item_id = t.id where t.id = toi.id and d.pickup_id = dt.pickup_id) cargovolume,"
        		+ " (select round(ifnull(pd.weight,0)*ifnull(d.amount,0),2) from transfer_order_item t left join depart_transfer d on d.order_item_id = t.id where t.id = toi.id and d.pickup_id = dt.pickup_id) cargoweight"
        		+ " from transfer_order_item toi"
        		+ " left join depart_transfer dt on dt.order_id = toi.order_id"
        		+ " left join depart_order dor on dor.id = dt.pickup_id"
        		+ " left join transfer_order tor on tor.id = dt.order_id "
        		+ " left join party p on p.id = tor.customer_id "
        		+ " left join contact c on c.id = p.contact_id "
        		+ " left join product pd on pd.id = toi.product_id "
        		+ " where dt.pickup_id in(" + pickupId + ")"
        		+ " UNION"
        		+ " SELECT DISTINCT  toi.id, dor.order_no depart_no,tor.order_no, c.abbr customer, ifnull(toi.item_no, pd.item_no) item_no,toi.item_name,toi.remark,tor.cargo_nature,"
        		+ " ROUND((SELECT ifnull(sum(doi.amount), 0) FROM delivery_order_item doi WHERE doi.delivery_id = dor.id),2) AS atmvolume,"
        		+ " ROUND((SELECT ifnull(toi.volume, 0) * ifnull(sum(doi.amount), 0) FROM delivery_order_item doi LEFT JOIN transfer_order_item toi ON toi.id = doi.transfer_item_id WHERE doi.delivery_id = dor.id),2) AS atmvolume,"
        		+ " ROUND((SELECT ifnull(toi.weight, 0) * ifnull(sum(doi.amount), 0) FROM delivery_order_item doi LEFT JOIN transfer_order_item toi ON toi.id = doi.transfer_item_id WHERE doi.delivery_id = dor.id),2) AS atmweight,"
        		+ " 0 cargoamount,0 cargovolume,0 cargoweight"
        		+ " FROM delivery_order dor"
        		+" LEFT JOIN delivery_order_item doi ON dor.id = doi.delivery_id"
        		+" LEFT JOIN transfer_order tor on tor.id=doi.transfer_order_id"
        		+" LEFT JOIN transfer_order_item toi ON toi.order_id=tor.id"
        		+" LEFT JOIN party p ON p.id = tor.customer_id"
        		+" LEFT JOIN contact c ON c.id = p.contact_id"
        		+" LEFT JOIN product pd ON pd.id = toi.product_id"
        		+" WHERE dor.id IN ("+deliveryId+")" + sLimit;
        List<Record> departOrderitem = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", departOrderitem);
        renderJson(Map);
    }
    // 里程碑
    public void transferOrderMilestoneList() {
    	String carSummaryId = getPara("car_summary_id");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(0) total from transfer_order_milestone tom"
        		+ " left join user_login u on u.id = tom.create_by"
        		+ " where tom.type = '"
                            + TransferOrderMilestone.TYPE_CAR_SUMMARY_MILESTONE + "' and tom.car_summary_id = "+ carSummaryId;
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select tom.*,u.user_name,u.c_name from transfer_order_milestone tom"
        		+ " left join user_login u on u.id = tom.create_by"
        		+ " where tom.type = '"
                            + TransferOrderMilestone.TYPE_CAR_SUMMARY_MILESTONE + "' and tom.car_summary_id = "+ carSummaryId;
        List<Record> departOrderitem = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", departOrderitem);
        renderJson(Map);
    }
    
    //查询路桥费明细
    public void findCarSummaryRouteFee(){
    	String car_summary_id = getPara("car_summary_id");// 调车单id
    	if(car_summary_id != ""){
	    	String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	        String sqlTotal = "select count(0) total from car_summary_detail_route_fee where car_summary_id ="+car_summary_id+";";
	        logger.debug("sql :" + sqlTotal);
	        Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        String sql = "select * from car_summary_detail_route_fee where car_summary_id ="+car_summary_id+";";
	        List<Record> departOrderitem = Db.find(sql);
	        Map Map = new HashMap();
	        Map.put("sEcho", pageIndex);
	        Map.put("iTotalRecords", rec.getLong("total"));
	        Map.put("iTotalDisplayRecords", rec.getLong("total"));
	        Map.put("aaData", departOrderitem);
	        renderJson(Map); 
    	}
    }
    // 添加路桥费明细
    public void addCarSummaryRouteFee() {
        String carSummaryId = getPara();
        if(carSummaryId != ""){
        	java.util.Date utilDate = new java.util.Date();
        	java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            
        	String sql = "select * from car_summary_detail_route_fee where  car_summary_id ='"+carSummaryId+"' order by item  desc limit 0,1;";
        	List<CarSummaryDetailRouteFee> routeFeeList = CarSummaryDetailRouteFee.dao.find(sql);
        	long item = 1;
        	if(routeFeeList.size() > 0){
        		item += routeFeeList.get(0).getLong("item");
        	}
        	CarSummaryDetailRouteFee routeFee = new CarSummaryDetailRouteFee();
            routeFee.set("item", item).set("car_summary_id", carSummaryId).set("charge_data", sqlDate).save();
        }
        renderJson("{\"success\":true}");
    }
    //删除路桥费明细
    public void delCarSummaryRouteFee(){
    	String carSummaryRouteFeeId = getPara();
    	CarSummaryDetailRouteFee.dao.deleteById(carSummaryRouteFeeId);
    	renderJson("{\"success\":true}");
    }
    //修改路桥费明细
    public void updateCarSummaryDetailRouteFee(){
    	String carSummaryId = getPara("car_summary_id");
    	String routeFeeId = getPara("routeFeeId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if(!"".equals(routeFeeId) && routeFeeId != null){
    		if("travel_amount".equals(name) && "".equals(value)){
    			value = "0";
    		}
    		CarSummaryDetailRouteFee carSummaryDetailrouteFee = CarSummaryDetailRouteFee.dao.findById(routeFeeId);
    		carSummaryDetailrouteFee.set(name, value);
    		carSummaryDetailrouteFee.update();
    		//修改费用合计中的路桥费
    		if("travel_amount".equals(name) && !"0".equals(value)){
    			Record rec = Db.findFirst("select sum(travel_amount) amount from car_summary_detail_route_fee where car_summary_id ="+carSummaryId);
    			String sql="update car_summary_detail_other_fee set amount = "+rec.getDouble("amount")+" where amount_item  = '路桥费' and car_summary_id = "+carSummaryId;
    			Db.update(sql);
    		}
    	}
    	renderJson("{\"success\":true}");
    }
    
    //查询加油记录
    public void findCarSummaryDetailOilFee(){
    	String car_summary_id = getPara("car_summary_id");// 调车单id
    	if(car_summary_id != ""){
	    	String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	        String sqlTotal = "select count(0) total from car_summary_detail_oil_fee where car_summary_id ="+car_summary_id+";";
	        logger.debug("sql :" + sqlTotal);
	        Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        String sql = "select * from car_summary_detail_oil_fee where car_summary_id ="+car_summary_id+";";
	        List<Record> departOrderitem = Db.find(sql);
	        Map Map = new HashMap();
	        Map.put("sEcho", pageIndex);
	        Map.put("iTotalRecords", rec.getLong("total"));
	        Map.put("iTotalDisplayRecords", rec.getLong("total"));
	        Map.put("aaData", departOrderitem);
	        renderJson(Map); 
    	}
    }
    // 添加加油记录
    public void addCarSummaryDetailOilFee() {
        String carSummaryId = getPara();
        if(carSummaryId != ""){
        	java.util.Date utilDate = new java.util.Date();
        	java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        	
        	String sql = "select * from car_summary_detail_oil_fee where  car_summary_id ='"+carSummaryId+"' order by item  desc limit 0,1;";
        	List<CarSummaryDetailOilFee> oilFeeList = CarSummaryDetailOilFee.dao.find(sql);
        	long item = 1;
        	if(oilFeeList.size() > 0){
        		item += oilFeeList.get(0).getLong("item");
        	}
        	CarSummaryDetailOilFee oilFee = new CarSummaryDetailOilFee();
        	oilFee.set("item", item).set("car_summary_id", carSummaryId).set("refuel_type", "0#柴油")
        	.set("refuel_data", sqlDate).set("payment_type", "油卡").save();
        }
        renderJson("{\"success\":true}");
    }
    //删除加油记录
    public void delCarSummaryDetailOilFee(){
    	String carSummaryDetailOilFeeId = getPara();
    	CarSummaryDetailOilFee.dao.deleteById(carSummaryDetailOilFeeId);
    	renderJson("{\"success\":true}");
    }
    //修改加油记录
    public void updateCarSummaryDetailOilFee(){
    	String carSummaryId = getPara("car_summary_id");
    	String routeFeeId = getPara("routeFeeId");
    	String name = getPara("name");
    	String value = getPara("value");
    	String refuel_number = getPara("refuel_number");
    	String avg_econ = getPara("avg_econ");
    	if(!"".equals(routeFeeId) && routeFeeId != null){
    		if("odometer_mileage".equals(name) || "refuel_unit_cost".equals(name)
    			||  "refuel_amount".equals(name) || "load_amount".equals(name)){
    			if("".equals(value)){
    				value = "0";
    			}
    		}
    		CarSummaryDetailOilFee carSummaryDetailOilFee = CarSummaryDetailOilFee.dao.findById(routeFeeId);
    		carSummaryDetailOilFee.set(name, value);
    		if(refuel_number != null && !"".equals(refuel_number)){
    			carSummaryDetailOilFee.set("refuel_number", refuel_number);
    			carSummaryDetailOilFee.update();
    			//修改费用合计中的本次加油
    			Record rec = Db.findFirst("select ifnull(sum(refuel_amount),0) amount from car_summary_detail_oil_fee where car_summary_id ="+carSummaryId);
				String sql="update car_summary_detail_other_fee set amount = "+rec.get("amount")+"  where amount_item  = '本次加油' and car_summary_id = "+carSummaryId;
    			Db.update(sql);
    			//修改费用合计中的本次油耗
    			updateNextFuelStandard(carSummaryId);
    		}
    		if(avg_econ != null && !"".equals(avg_econ)){
    			carSummaryDetailOilFee.set("avg_econ", avg_econ);
    		}
    		carSummaryDetailOilFee.update();
    		/*
    		//修改费用合计中的本次加油
    		if("payment_type".equals(name) || "refuel_amount".equals(name) && !"0".equals(value)){
    			try {
    				Record rec = Db.findFirst("select ifnull(sum(refuel_amount),0) amount from car_summary_detail_oil_fee where payment_type = '现金' and car_summary_id ="+carSummaryId);
    				String sql="update car_summary_detail_other_fee set amount = "+rec.get("amount")+"  where amount_item  = '本次加油' and car_summary_id = "+carSummaryId;
        			Db.update(sql);
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    		//修改费用合计中的本次油耗
    		if("refuel_unit_cost".equals(name) && !"0".equals(value)){
    			updateNextFuelStandard(carSummaryId);
    		}*/
    	}
    	renderJson("{\"success\":true}");
    }
    
    //查询送货员工资明细
    public void findCarSummaryDetailSalary(){
    	String carSummaryId = getPara("car_summary_id");// 调车单id
    	if(carSummaryId != ""){
	    	String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	        String sqlTotal = "select count(0) total from car_summary_detail_salary where car_summary_id ="+carSummaryId+";";
	        logger.debug("sql :" + sqlTotal);
	        Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        String sql = "select * from car_summary_detail_salary where car_summary_id ="+carSummaryId+";";
	        List<Record> departOrderitem = Db.find(sql);
	        Map Map = new HashMap();
	        Map.put("sEcho", pageIndex);
	        Map.put("iTotalRecords", rec.getLong("total"));
	        Map.put("iTotalDisplayRecords", rec.getLong("total"));
	        Map.put("aaData", departOrderitem);
	        renderJson(Map); 
    	}
    }
    // 添加送货员工资明细
    public void addCarSummaryDetailSalary() {
        String carSummaryId = getPara();
        if(carSummaryId != ""){
        	java.util.Date utilDate = new java.util.Date();
        	java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        	String sql = "select * from car_summary_detail_salary where  car_summary_id ='"+carSummaryId+"' order by item  desc limit 0,1;";
        	List<CarSummaryDetailSalary> salaryList = CarSummaryDetailSalary.dao.find(sql);
        	long item = 1;
        	if(salaryList.size() > 0){
        		item += salaryList.get(0).getLong("item");
        	}
        	CarSummaryDetailSalary salary = new CarSummaryDetailSalary();
        	salary.set("item", item).set("car_summary_id", carSummaryId)
        	.set("create_data", sqlDate).save();
        }
        renderJson("{\"success\":true}");
    }
    //删除送货员工资明细
    public void delCarSummaryDatailSalary(){
    	String carSummaryDetailSalaryId = getPara();
    	CarSummaryDetailSalary.dao.deleteById(carSummaryDetailSalaryId);
    	renderJson("{\"success\":true}");
    }
    //修改送货员工资明细
    public void updateCarSummaryDetailSalary(){
    	String carSummaryId = getPara("car_summary_id");
    	String routeFeeId = getPara("routeFeeId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if(!"".equals(routeFeeId) && routeFeeId != null){
    		if("deserved_amount".equals(name) && "".equals(value)){
    			value = "0";
    		}
    		CarSummaryDetailSalary salary = CarSummaryDetailSalary.dao.findById(routeFeeId);
    		salary.set(name, value);
    		salary.update();
    		//修改费用合计中的送货员工资
    		if("deserved_amount".equals(name) && !"0".equals(value)){
    			Record rec = Db.findFirst("select sum(deserved_amount) amount from car_summary_detail_salary where car_summary_id ="+carSummaryId);
    			String sql="update car_summary_detail_other_fee set amount="+rec.getDouble("amount")+" where amount_item  = '送货员工资' and car_summary_id = "+carSummaryId;
    			Db.update(sql);
    		}
    	}
    	renderJson("{\"success\":true}");
    }
    //查询费用合计
    public void findCarSummaryDetailOtherFee(){
    	String car_summary_id = getPara("car_summary_id");// 调车单id
    	if(car_summary_id != ""){
	    	String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	        String sqlTotal = "select count(0) total from car_summary_detail_other_fee where car_summary_id ="+car_summary_id+";";
	        logger.debug("sql :" + sqlTotal);
	        Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        String sql = "select * from car_summary_detail_other_fee where car_summary_id ="+car_summary_id+";";
	        List<Record> departOrderitem = Db.find(sql);
	        Map Map = new HashMap();
	        Map.put("sEcho", pageIndex);
	        Map.put("iTotalRecords", rec.getLong("total"));
	        Map.put("iTotalDisplayRecords", rec.getLong("total"));
	        Map.put("aaData", departOrderitem);
	        renderJson(Map); 
    	}
    }
    //修改费用合计(复杂的费用计算业务)
    public void updateCarSummaryDetailOtherFee(){
    	String routeFeeId = getPara("routeFeeId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if(!"".equals(routeFeeId) && routeFeeId != null){
    		if("amount".equals(name) && "".equals(value)){
    			value = "0";
    		}
    		CarSummaryDetailOtherFee otherFee = CarSummaryDetailOtherFee.dao.findById(routeFeeId);
    		otherFee.set(name, value);
    		otherFee.update();
    	}
    	renderJson("{\"success\":true}");
    }
    //审核-撤销审核
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CS_APPROVAL})
    public void updateCarSummaryOrderStatus(){
    	String carSummaryId = getPara("carSummaryId");
    	String value = getPara("value").trim();
    	if(!"".equals(carSummaryId) && carSummaryId != null){
    		CarSummaryOrder order = CarSummaryOrder.dao.findById(carSummaryId);
    		if("审核".equals(value)){
    			value = "已审批";
    		}else if("撤销审核".equals(value)){
    			value = order.CAR_SUMMARY_SYSTEM_REVOCATION;
    		}else if("报销".equals(value)){
    			value = order.CAR_SUMMARY_SYSTEM_REIMBURSEMENT;
    		}else{
    			value = "";
    		}
    		order.set("status", value);
    		order.update();
    		//修改行车里程碑状态
    		saveCarSummaryOrderMilestone(Long.parseLong(carSummaryId),value);
    	}
    	renderJson("{\"success\":true}");
    }
    //每次新增调车单时要添加的数据
    private void initCarSummaryDetailOtherFeeData(long carSummaryOrderId){
    	String name[] = {"car_summary_id","item","amount_item","is_delete"};
    	String items[] = {"本次加油","本次油耗","出车补贴","司机工资","路桥费","装卸费","罚款","送货员工资","停车费","住宿费","过磅费","其他费用"};
    	String isdeletes[] = {"是","否"};
    	for (int i = 0; i < items.length; i++) {
    		CarSummaryDetailOtherFee c = new CarSummaryDetailOtherFee();
    		if(i == 1 || i == 3 || i == 7)
    			c.set(name[0], carSummaryOrderId).set(name[1], i+1).set(name[2], items[i]).set(name[3], isdeletes[0]).set("amount", 0).save();
    		else
    			c.set(name[0], carSummaryOrderId).set(name[1], i+1).set(name[2], items[i]).set(name[3], isdeletes[1]).set("amount", 0).save();
		}
    }
    // 保存行车里程碑
 	private void saveCarSummaryOrderMilestone(long carSummaryOrderId,String status) {
 		TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
 		transferOrderMilestone.set("status", status);
 		String name = (String) currentUser.getPrincipal();
 		List<UserLogin> users = UserLogin.dao
 				.find("select * from user_login where user_name='" + name + "'");
 		transferOrderMilestone.set("create_by", users.get(0).get("id"));
 		transferOrderMilestone.set("location", "");
 		java.util.Date utilDate = new java.util.Date();
 		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
 		transferOrderMilestone.set("create_stamp", sqlDate);
 		transferOrderMilestone.set("type",
 				TransferOrderMilestone.TYPE_CAR_SUMMARY_MILESTONE);
 		transferOrderMilestone.set("car_summary_id", carSummaryOrderId);
 		transferOrderMilestone.save();
 	}
 	//编辑行车单
 	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CS_CREATE})
    public void edit(){
    	String carSummaryId = getPara("carSummaryId");
    	if(carSummaryId != "" && carSummaryId != null){
    		CarSummaryOrder carSummaryOrder = CarSummaryOrder.dao.findById(carSummaryId);
    		//车牌号
			setAttr("car_no", carSummaryOrder.get("car_no"));
			//主司机姓名
			setAttr("driver", carSummaryOrder.get("main_driver_name"));
			//出车次
			setAttr("carNumber", carSummaryOrder.get("month_start_car_next"));
			//是否审核 isAudit
			String status = carSummaryOrder.get("status");
			if("新建".equals(status) ||carSummaryOrder.CAR_SUMMARY_SYSTEM_REVOCATION.equals(status) )
				setAttr("isAudit", "no");
			else if("已审批".equals(status) || carSummaryOrder.CAR_SUMMARY_SYSTEM_REIMBURSEMENT.equals(status))
				setAttr("isAudit", "yes");
			else
				setAttr("isAudit", "no");
			
			setAttr("carSummaryOrder", carSummaryOrder);
			
			Record rec = Db.findFirst("select group_concat(cast(csd.pickup_order_id as char) separator ',') pickupids,group_concat(cast(csd.pickup_type as char) separator ',') pickup_type  from car_summary_detail csd where csd.car_summary_id in("+carSummaryId+") ");
			//拼车单号
			setAttr("pickupIds", rec.get("pickupids"));
			setAttr("orderTypes", rec.get("pickup_type"));
    	}
    	render("/yh/carmanage/carSummaryEdit.html");
    }
    
    //查询运输单
    public void findTransferOrder(){
    	String pickupIdsArr = getPara("pickupIds");
		String pickupIds[] = pickupIdsArr.split(",");
		String orderTypesArr = getPara("orderTypes");
		String orderTypes[] = orderTypesArr.split(",");
		String pickupId="";
		String deliveryId="";
		for(int i=0; i<pickupIds.length;i++){
			if("配送".equals(orderTypes[i])){
				deliveryId+=pickupIds[i];
				deliveryId+=",";
			}else{
				pickupId+=pickupIds[i];
				pickupId+=",";
			}
		}
		if(!"".equals(pickupId)){
			pickupId = pickupId.substring(0,pickupId.length()-1);
		}else{
			pickupId="-1";
		}
		if(!"".equals(deliveryId)){
			deliveryId = deliveryId.substring(0,deliveryId.length()-1);
		}else{
			deliveryId="-1";
		}
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "SELECT COUNT(1) total from (select  id from depart_order where id in("+pickupId+") union SELECT id from delivery_order where id in ("+deliveryId+"))a";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select distinct tr.id,tr.order_no,c.abbr,tr.car_summary_order_share_ratio,tr.remark,tr.cargo_nature,"
        		+ " (select count(0) total from transfer_order_item_detail where order_id = tr.id  and pickup_id in(" + pickupId+")) atmamount,"
        		+ " round((select sum(ifnull(volume, 0)) from transfer_order_item_detail where pickup_id in(" + pickupId+")), 2) atmvolume,"
        		+ " round((select sum(ifnull(weight, 0)) from transfer_order_item_detail where pickup_id in(" + pickupId+")), 2) atmweight,"
        		+ " (select SUM(amount) from depart_transfer where pickup_id = dt.pickup_id) cargoamount,"
        		+ " (select round(sum(ifnull(toi.volume,0)*(dtf.amount/toi.amount)),2) from transfer_order_item toi left join depart_transfer dtf on dtf.order_item_id = toi.id "
        		+ " left join transfer_order t on t.id = toi.order_id	where dtf.pickup_id = dt.pickup_id and t.cargo_nature = 'cargo') cargovolume,"
        		+ " (select round(sum(ifnull(toi.sum_weight,0)*(dtf.amount/toi.amount)),2) from transfer_order_item toi left join depart_transfer dtf on dtf.order_item_id = toi.id "
        		+ " left join transfer_order t on t.id = toi.order_id	where dtf.pickup_id = dt.pickup_id and t.cargo_nature = 'cargo') cargoweight"
        		+ " from depart_transfer dt"
        		+ " left join transfer_order tr on tr.id = dt.order_id"
        		+ " left join transfer_order_item toi on toi.order_id = tr.id"
        		+ " left join party p on p.id = tr.customer_id"
        		+ " left join contact c on c.id = p.contact_id"
        		+ " left join product pd on pd.id = toi.product_id"
        		+ " where dt.pickup_id in(" + pickupId+") "
        		+ " UNION"
        		+ " SELECT t_o.id,t_o.order_no ,c.abbr,t_o.car_summary_order_share_ratio,t_o.remark,t_o.cargo_nature,"
        		+ " ROUND((SELECT ifnull(sum(doi.amount), 0) FROM delivery_order_item doi LEFT JOIN transfer_order_item toi ON toi.id = doi.transfer_item_id WHERE doi.delivery_id = dor.id),2) atmamount,"
        		+ " ROUND((SELECT ifnull(toi.volume, 0) * ifnull(sum(doi.amount), 0) FROM delivery_order_item doi LEFT JOIN transfer_order_item toi ON toi.id = doi.transfer_item_id WHERE doi.delivery_id = dor.id),2) AS cargovolume,"
        		+ " ROUND((SELECT ifnull(toi.weight, 0) * ifnull(sum(doi.amount), 0)FROM delivery_order_item doi LEFT JOIN transfer_order_item toi ON toi.id = doi.transfer_item_id WHERE doi.delivery_id = dor.id),2) AS cargoweight,"
        		+ " 0 cargoamount,0 cargovolume,0 cargoweight"
        		+ " FROM delivery_order dor"
        		+ " LEFT JOIN delivery_order_item doi ON doi.delivery_id = dor.id"
        		+ " LEFT JOIN transfer_order t_o ON t_o.id = doi.transfer_order_id"
        		+ " LEFT JOIN party p ON p.id = t_o.customer_id"
        		+ " LEFT JOIN contact c ON c.id = p.contact_id"
        		+ " where dor.id in("+deliveryId+")"+ sLimit;
        List<Record> departOrderitem = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", departOrderitem);
        renderJson(Map); 
    	
    }
    //修改运输单比例
    public void updateTransferOrderShareRatio(){
    	String[] orderIds  =  getPara("orderIds").split(",");
    	String[] rates  =  getPara("rates").split(",");
    	
    	if(orderIds.length > 0 && rates.length > 0){
	    	for (int i = 0; i < orderIds.length; i++) {
	    		double rate = Double.parseDouble(rates[i])/100;
	    		TransferOrder transferOrder = TransferOrder.dao.findById(orderIds[i]);
	    		transferOrder.set("car_summary_order_share_ratio", rate);
	    		transferOrder.update();
			}
    	}
    	renderJson("{\"success\":true}");
    }
    
    
    /**
     * 费用计算
     * return:本单总加油、本单出车成本、应扣分摊费用、实际应付费用
     */
    public void calculateCost(){
    	String carSummaryId = getPara("carSummaryId");
    	Map<String,Double > costMap = new HashMap<String, Double>();
    	CarSummaryOrder order =CarSummaryOrder.dao.findById(carSummaryId);
    	//出车成本
    	Record rec1 = Db.findFirst("select sum(amount) amount from car_summary_detail_other_fee where item != 1 and car_summary_id = "+carSummaryId+";");
    	if(rec1.get("amount") != null && rec1.get("amount") != ""){
    		costMap.put("next_start_car_amount", rec1.getDouble("amount"));
    		order.set("next_start_car_amount", rec1.getDouble("amount"));
    	}
    	//总加油费（现金）
    	Record rec2 = Db.findFirst("select sum(refuel_amount) amount from car_summary_detail_oil_fee where payment_type = '现金' and car_summary_id = "+carSummaryId+";");
    	if(rec2.get("amount") != null && rec2.get("amount") != ""){
    		costMap.put("month_refuel_amount ", rec2.getDouble("amount"));
    		order.set("month_refuel_amount", rec2.getDouble("amount"));
    	}
    	//应扣分摊费用
    	Record rec4 = Db.findFirst("select sum(amount) amount  from car_summary_detail_other_fee where is_delete = '是' and car_summary_id = "+carSummaryId+";");
    	if(rec4.get("amount") != null && rec4.get("amount") != ""){
    		BigDecimal b = new BigDecimal(rec4.getDouble("amount")); 
    		double money = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();  
    		costMap.put("deduct_apportion_amount ", money);
    		order.set("deduct_apportion_amount", money);
    	}
    	//实际应付费用
    	Record rec3 = Db.findFirst("select sum(amount) amount  from car_summary_detail_other_fee where is_delete = '否' and car_summary_id = "+carSummaryId+" and item!=1;");
    	Record rec5 = Db.findFirst("select sum(refuel_amount) amount from car_summary_detail_oil_fee where payment_type = '现金' and car_summary_id = "+carSummaryId+";");
    	if(rec3.get("amount") != null && rec3.get("amount") != ""){
    		BigDecimal b = new BigDecimal(rec3.getDouble("amount")); 
    		double money3 = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    		if(rec5.get("amount") != null && rec5.get("amount") != ""){
    			BigDecimal c = new BigDecimal(rec5.getDouble("amount")); 
        		double money5 = c.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    			costMap.put("actual_payment_amount ", money3 + money5);
        		order.set("actual_payment_amount", money3 + money5);
    		}else{
    			costMap.put("actual_payment_amount ", money3);
        		order.set("actual_payment_amount", money3);
    		}
    	}
    	order.update();
    	renderJson(costMap); 
    }
    //本次油耗
    public void updateNextFuelStandard(String carSummaryId){
    	CarSummaryOrder order = CarSummaryOrder.dao.findById(carSummaryId);
    	String carNo = order.getStr("car_no");
    	double mileage = 0;
    	double refuelCost = 0;
    	double hundredFuelStandard = 0;
    	if(order.get("month_car_run_mileage") != null && !"".equals(order.getDouble("month_car_run_mileage"))){
    		mileage = order.getDouble("month_car_run_mileage");//行驶里程
    	}
    	Record oilFee1 = Db.findFirst("select avg(refuel_unit_cost) refuel_cost from car_summary_detail_oil_fee where car_summary_id='"+carSummaryId+"'");
    	if(oilFee1.get("refuel_cost") != null && !"".equals(oilFee1.get("refuel_cost"))){
    		refuelCost = oilFee1.getDouble("refuel_cost");//平均油价
    	}
    	if(oilFee1.get("refuel_cost")==null||"".equals(oilFee1.get("refuel_cost"))){
    	Record oilFee2 = Db.findFirst("select refuel_unit_cost refuel_cost from car_summary_detail_oil_fee order by id desc limit 1");
    	if(oilFee2.get("refuel_cost") != null && !"".equals(oilFee2.get("refuel_cost"))){
    		refuelCost = oilFee2.getDouble("refuel_cost");//平均油价
    	}
    	else{
    		refuelCost=5.77;
    	}
    	}
    	Record carinfo = Db.findFirst("select hundred_fuel_standard from carinfo where car_no = '"+carNo+"';");
    	if(carinfo.get("hundred_fuel_standard") != null && !"".equals(carinfo.get("hundred_fuel_standard"))){
    		hundredFuelStandard = carinfo.getDouble("hundred_fuel_standard");//百公里油耗
    	}
    	if(mileage > 0 && refuelCost > 0 && hundredFuelStandard > 0){
    		double number = mileage * refuelCost * hundredFuelStandard / 100;
    		BigDecimal b = new BigDecimal(number); 
    		double money = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();  
    		String sql = "update car_summary_detail_other_fee set amount = ? where car_summary_id = '"+carSummaryId+"' and item = 2 ;";
    		Db.update(sql, money);
    	}
    }
    
    
    
    
}
