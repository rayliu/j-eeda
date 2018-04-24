package controllers.yh.departOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderFinItem;
import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.DepartOrder;
import models.DepartOrderFinItem;
import models.DepartPickupOrder;
import models.DepartTransferOrder;
import models.FinItem;
import models.InventoryItem;
import models.Location;
import models.Party;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.contract.Contract;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Carinfo;
import models.yh.profile.Contact;
import models.yh.returnOrder.ReturnOrderFinItem;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.OfficeController;
import controllers.yh.util.LocationUtil;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;
import controllers.yh.util.getCustomFile;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class DepartOrderController extends Controller {

	private Logger logger = Logger.getLogger(DepartOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	private static final String DEPART_ORDER_TOKEN="depart_order_token";
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_LIST})
	public void index() {
		
		List<Record> re = Db.find("SELECT o.id,o.office_name FROM transfer_order tor "
    			+ " LEFT JOIN office o on o.id = tor.office_id"
    			+ " where tor.office_id in (select office_id from user_office where user_name='"
				+ currentUser.getPrincipal() + "')  GROUP BY o.id ;");
    	setAttr("officeList", re);
		
		render("/yh/departOrder/departOrderList.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_OT_LIST})
	public void onTrip() {
		List<Record> re = Db.find("SELECT o.id,o.office_name FROM transfer_order tor "
    			+ " LEFT JOIN office o on o.id = tor.office_id  GROUP BY o.id ;");
		List<Record> re2 = Db.find("SELECT o.id,o.office_name FROM transfer_order tor "
    			+ " LEFT JOIN office o on o.id = tor.office_id"
    			+ " where tor.office_id in (select office_id from user_office where user_name='"
				+ currentUser.getPrincipal() + "')  GROUP BY o.id ;");
    	setAttr("officeList", re);
    	setAttr("ListOperationOffice",re2);
		render("/yh/departOrder/departOrderOnTripList.html");
	}
	// 发车单在途供应商
	public void companyNameList() {
		String inputStr = getPara("input");
		String sql = "select distinct  c.abbr as company  from contact c where c.abbr  is not null "
				+ "and c.abbr like '%" + inputStr + "%'";
		List<Record> companyNameList = Db.find(sql);
		renderJson(companyNameList);
	}
	// 运输单外包供应商
	public void cpnameList() {
		String sp_filter = getPara("sp_filter");
		String sql = "select distinct c.abbr company from contact c where c.abbr is not null and ifnull(c.abbr,'') like '%"
				+ sp_filter + "%'";
		List<Record> companyNameList = Db.find(sql);
		renderJson(companyNameList);
	}
	// 运输单外包客户(有问题)
	public void customerList() {
		String customer_filter = getPara("customer_filter");
		String sql = "select distinct c1.abbr customer from transfer_order t "
				+ " left join party p1 on t.customer_id = p1.id "
				+ " left join party p2 on t.sp_id = p2.id "
				+ " left join contact c1 on p1.contact_id = c1.id"
				+ " left join contact c2 on p2.contact_id = c2.id"
				+ " left join office o on t.office_id = o.id "
				+ " where c1.abbr is not null "
				+ " and operation_type ='out_source' " + " and c1.abbr like '%"
				+ customer_filter + "%'";

		List<Record> companyNameList = Db.find(sql);
		renderJson(companyNameList);
	}
	// 运输单外包网点
	public void officenameList() {
		String officeName_filter = getPara("officeName_filter");
		String sql = "select distinct o.office_name officeName  from office o "
				+ "where o.office_name is not null "
				+ "and o.office_name like '%" + officeName_filter + "%'";
		List<Record> companyNameList = Db.find(sql);
		renderJson(companyNameList);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_LIST})
	public void list() {
		String orderNo = getPara("orderNo")==null?"":getPara("orderNo").trim();
		String departNo = getPara("departNo")==null?"":getPara("departNo").trim();
		String status = getPara("status")==null?"":getPara("status").trim();
		String sp = getPara("sp")==null?"":getPara("sp").trim();
		String beginTime = getPara("beginTime")==null?"":getPara("beginTime").trim();
		String endTime = getPara("endTime")==null?"":getPara("endTime").trim();
		String planBeginTime = getPara("planBeginTime")==null?"":getPara("planBeginTime").trim();
		String planEndTime = getPara("planEndTime")==null?"":getPara("planEndTime").trim();
		String office = getPara("office")==null?"":getPara("office").trim();
		String start = getPara("start")==null?"":getPara("start").trim();
		String destination = getPara("destination")==null?"":getPara("destination").trim();
		String customer = getPara("customer")==null?"":getPara("customer").trim();
		String booking_note_number = getPara("booking_note_number")==null?"":getPara("booking_note_number").trim();
		String costchebox = getPara("costchebox")==null?"":getPara("costchebox").trim();
		String transfer_type = getPara("transfer_type")==null?"":getPara("transfer_type").trim();

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		String sql = "";
		String totalSql = "";
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String conditions=" where 1=1 ";
		if (StringUtils.isNotEmpty(orderNo)){
        	conditions+=" and transfer_order_no like '%"+orderNo+"%'";
        }
        if (StringUtils.isNotEmpty(status)){
        	conditions+=" and depart_status like '%"+status+"%'";
        }
        if (StringUtils.isNotEmpty(departNo)){
        	conditions+=" and depart_no like '%"+departNo+"%'";
        }
        if (StringUtils.isNotEmpty(booking_note_number)){
        	conditions+=" and booking_note_number like '%"+booking_note_number+"%'";
        }
        if (StringUtils.isNotEmpty(sp)){
        	conditions+=" and abbr like '%"+sp+"%'";
        }
        if (StringUtils.isNotEmpty(office)){
        	conditions+=" and office_name like '%"+office+"%'";
        }
        
        String condition = "";
        if (StringUtils.isNotEmpty(customer)){
        	condition = " and ("
        			+ " SELECT GROUP_CONCAT( DISTINCT c.abbr SEPARATOR '' ) "
        			+ " FROM transfer_order tor LEFT JOIN depart_transfer dt ON dt.order_id = tor.id "
        			+ " LEFT JOIN party p ON p.id = tor.customer_id LEFT JOIN contact c ON c.id = p.contact_id "
        			+ " WHERE dt.depart_id = deo.id ) like '%"+customer+"%'";
        }
        if (StringUtils.isNotEmpty(transfer_type)){
        	conditions+=" and trip_type like '%"+transfer_type+"%'";
        }
        if (StringUtils.isNotEmpty(start)){
        	conditions+=" and route_from like '%"+start+"%'";
        }
        if (StringUtils.isNotEmpty(destination)){
        	conditions+=" and route_to like '%"+destination+"%'";
        }
        if (StringUtils.isNotEmpty(costchebox)){
        	if(costchebox.equals("0"))
        		conditions+=" and total_cost = "+costchebox;
        }
        if (StringUtils.isNotEmpty(beginTime)){
			beginTime = " and create_stamp between'"+beginTime+"'";
        }else{
        	beginTime =" and create_stamp between '2000-1-1'";
        }
        if (StringUtils.isNotEmpty(endTime)){
        	endTime =" and '"+endTime+" 23:59:59'";
        }else{
        	endTime =" and '3000-1-1'";
        }
        conditions += beginTime + endTime;
        if (StringUtils.isNotEmpty(planBeginTime)){
        	planBeginTime = " and planning_time between '"+planBeginTime+"'";
        }else{
        	planBeginTime =" and planning_time between '2000-1-1'";
        }
        if (StringUtils.isNotEmpty(planEndTime)){
        	planEndTime =" and '"+planEndTime+" 23:59:59'";
        }else{
        	planEndTime =" and '3000-1-1'";
        }
        conditions += planBeginTime + planEndTime;
        conditions += "and office_id in (select office_id from user_office where user_name='"
				+ currentUser.getPrincipal()
				+ "') and customer_id in (select customer_id from user_customer where user_name='"
				+ currentUser.getPrincipal() + "')  ";

        totalSql = "select deo.id,deo.booking_note_number,deo.depart_no,deo.create_stamp,deo. status as depart_status,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT tor.office_id SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor"
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " WHERE dt.depart_id = deo.id ) AS office_id,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT o.office_name SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor"
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " left join office o on o.id = tor.office_id "
				+ " WHERE dt.depart_id = deo.id ) office_name,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT tor.customer_id SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor "
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " WHERE dt.depart_id = deo.id ) customer_id ,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT c.abbr SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor "
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " LEFT JOIN party p on p.id = tor.customer_id "
				+ " LEFT JOIN contact c on c.id = p.contact_id "
				+ " WHERE dt.depart_id = deo.id ) customer,"
				+ " ct.abbr abbr,"
				+ " l1.NAME route_from,"
				+ " l2.NAME route_to,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT tor.order_no SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor"
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " WHERE dt.depart_id = deo.id ) AS transfer_order_no,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT cast(tor.planning_time as char) SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor"
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " WHERE dt.depart_id = deo.id ) AS planning_time,"
				+ " (select ifnull(sum(dofi.amount), 0) from depart_order_fin_item dofi LEFT JOIN fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = deo.id and fi.type= '应付' ) total_cost,"
				+ " deo.transfer_type as trip_type"
				+ " from depart_order deo"
				+ " left join party p on deo.sp_id = p.id"
				+ " left join contact ct on p.contact_id = ct.id"
				+ " left join location l1 on l1.code = deo.route_from"
				+ " left join location l2 on l2.code = deo.route_to "
				+ " where "
				+ " deo.status!='手动删除' and deo.combine_type='DEPART' "
				+ condition;
        
		sql = "select deo.id,deo.booking_note_number,deo.depart_no,deo.create_stamp,deo. status as depart_status,deo.arrival_time arrival_time,deo.remark remark,ifnull(deo.driver, c.driver) contact_person,ifnull(deo.phone, c.phone) phone,c.car_no,c.cartype,c.length,ifnull(u.c_name,u.user_name) user_name,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT tor.office_id SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor"
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " WHERE dt.depart_id = deo.id ) AS office_id,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT o.office_name SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor"
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " left join office o on o.id = tor.office_id "
				+ " WHERE dt.depart_id = deo.id )  office_name,"
				+ " deo.departure_time departure_time ,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT tor.customer_id SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor "
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " WHERE dt.depart_id = deo.id ) customer_id ,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT c.abbr SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor "
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " LEFT JOIN party p on p.id = tor.customer_id "
				+ " LEFT JOIN contact c on c.id = p.contact_id "
				+ " WHERE dt.depart_id = deo.id ) customer,"
				+ " ct.abbr abbr,"
				+ " l1.NAME route_from,"
				+ " l2.NAME route_to,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT tor.order_no SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor"
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " WHERE dt.depart_id = deo.id ) AS transfer_order_no,"
				+ " ( SELECT GROUP_CONCAT( DISTINCT cast(tor.planning_time as char) SEPARATOR '\r\n' ) "
				+ " FROM transfer_order tor"
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " WHERE dt.depart_id = deo.id ) AS planning_time,"
				+ " (select ifnull(sum(dofi.amount), 0) from depart_order_fin_item dofi LEFT JOIN fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = deo.id and fi.type= '应付' ) total_cost,"
				+ " deo.transfer_type as trip_type,"
				+ " ifnull((SELECT count(toid.id) FROM "
				+ " transfer_order_item_detail toid"
				+ " LEFT JOIN transfer_order tor ON tor.id = toid.order_id"
				+ " LEFT JOIN depart_order deo1 ON deo1.id = toid.depart_id"
				+ " WHERE"
				+ " deo1.id = deo.id AND ( tor.cargo_nature = 'ATM' OR ( tor.cargo_nature = 'cargo'"
				+ " AND tor.cargo_nature_detail = 'cargoNatureDetailYes' )"
				+ " )),"
				+ " (SELECT count(toi.id) FROM transfer_order_item toi"
				+ " LEFT JOIN transfer_order tor ON tor.id = toi.order_id"
				+ " LEFT JOIN depart_transfer dt ON dt.order_item_id = toi.id"
				+ " LEFT JOIN depart_pickup dp ON dp.pickup_id = dt.pickup_id AND dp.order_id = toi.order_id"
				+ " WHERE (dp.depart_id = deo.id or (dt.depart_id = deo.id AND tor.operation_type = 'out_source' ))"
				+ " and tor.cargo_nature = 'cargo' and tor.cargo_nature_detail = 'cargoNatureDetailNo' )"
				+ " ) amount"
				+ " from depart_order deo"
				+ " left join carinfo c on deo.carinfo_id = c.id"
				+ " left join party p on deo.sp_id = p.id"
				+ " left join contact ct on p.contact_id = ct.id"
				+ " left join user_login u on u.id = deo.create_by"
				+ " left join location l1 on l1.code = deo.route_from"
				+ " left join location l2 on l2.code = deo.route_to "
				+ " where "
				+ " deo.status!='手动删除' and deo.combine_type='DEPART' "
				+ condition;
				

		Record rec = Db.findFirst("select count(1) total from (select * from (" + totalSql+ ") A " + conditions +" ) B");
		List<Record> departOrders = Db.find("select * from (" + sql+ ") A " + conditions + " order by create_stamp desc " + sLimit);
		Map map = new HashMap();
		map.put("sEcho", pageIndex);
		map.put("iTotalRecords", rec.getLong("total"));
		map.put("iTotalDisplayRecords", rec.getLong("total"));
		map.put("aaData", departOrders);
		renderJson(map);
	}

	// 发车单在途列表
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_OT_LIST})
	public void onTripList() {
		String orderNo = getPara("orderNo")==null?"":getPara("orderNo").trim();
		String departNo = getPara("departNo")==null?"":getPara("departNo").trim();
		String status = getPara("status")==null?"":getPara("status").trim();
		String sp = getPara("sp")==null?"":getPara("sp").trim();
		String beginTime = getPara("beginTime")==null?"":getPara("beginTime").trim();
		String booking_note_number = getPara("booking_note_number")==null?"":getPara("booking_note_number").trim();
		String cus_order_no = getPara("cus_order_no")==null?"":getPara("cus_order_no").trim();
		String endTime = getPara("endTime")==null?"":getPara("endTime").trim();
		String sLimit = "";
		String pageIndex = getPara("sEcho")==null?"":getPara("sEcho");
		String sql = "";
		String sqlTotal = "";

		String office = getPara("office")==null?"":getPara("office").trim();
		String start = getPara("start")==null?"":getPara("start").trim();
		String end = getPara("end")==null?"":getPara("end").trim();
		String customer = getPara("customer")==null?"":getPara("customer").trim();
		String planBeginTime = getPara("planBeginTime")==null?"":getPara("planBeginTime").trim();
		String planEndTime = getPara("planEndTime")==null?"":getPara("planEndTime").trim();
		String office_id = getPara("office_id")==null?"":getPara("office_id").trim();

		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		
		String conditions = " where 1 = 1 ";
		if(StringUtils.isNotEmpty(orderNo)){
			conditions += " and ifnull(tr.order_no,'') like '%" + orderNo + "%'"; 
		}
		if(StringUtils.isNotEmpty(cus_order_no)){
			conditions += " and ifnull(tr.customer_order_no,'') like '%" + cus_order_no + "%'"; 
		}
		if(StringUtils.isNotEmpty(booking_note_number)){
			conditions += " and ifnull(deo.booking_note_number,'') like '%" + booking_note_number + "%'"; 
		}
		if(StringUtils.isNotEmpty(departNo)){
			conditions += " and ifnull(deo.depart_no,'') like '%" + departNo + "%'"; 
		}
		if(StringUtils.isNotEmpty(status)){
			conditions += " and ifnull(deo.status,'') like '%" + status + "%'"; 
		}
		if(StringUtils.isNotEmpty(sp)){
			conditions += " and ifnull(c2.abbr,'') like '%" + sp + "%'"; 
		}
		if(StringUtils.isNotEmpty(office)){
			conditions += " and ifnull(wo.id,'') = '" + office + "'"; 
		}
		if(StringUtils.isNotEmpty(office_id)){
			conditions += " and ifnull(t.office_id,'') = '" + office_id + "'"; 
		}
		if(StringUtils.isNotEmpty(start)){
			conditions += " and ifnull(l1.name,'') like '%" + start + "%'"; 
		}
		if(StringUtils.isNotEmpty(end)){
			conditions += " and ifnull(l2.name,'') like '%" + end + "%'"; 
		}
		if(StringUtils.isNotEmpty(customer)){
			conditions += " and ifnull(c1.abbr,'') like '%" + customer + "%'"; 
		}
		
		if(StringUtils.isEmpty(beginTime)){
			beginTime = "2000-01-01";
		}
		if(StringUtils.isEmpty(endTime)){
			endTime = "2037-12-31";
		}else{
			endTime += " 23:59:59";
		}
		conditions += " and deo.departure_time between '" + beginTime + "' and '" + endTime + "'"; 
		
		if(StringUtils.isEmpty(planBeginTime)){
			planBeginTime = "2000-01-01";
		}
		if(StringUtils.isEmpty(planEndTime)){
			planEndTime = "2037-12-31";
		}else{
			planEndTime += " 23:59:59";
		}
		conditions += " and (SELECT	group_concat(CAST(tr.planning_time AS char) SEPARATOR '\r\n')"
				+ " FROM transfer_order tr, depart_transfer dt where dt.depart_id = deo.id and tr.id = dt.order_id)"
				+ " between '" + planBeginTime + "' and '" + planEndTime + "'"; 

	
		sqlTotal = "select count(distinct deo.id) total"
				+ " from depart_order deo "
				+ " left join carinfo c on deo.carinfo_id = c.id "
				+ " left join depart_transfer dt on dt.depart_id = deo.id "
				+ " left join transfer_order t on t.id = dt.order_id "
				+ " left join office o on o.id = t.office_id "
				+ " left join party p1 on t.customer_id = p1.id "
				+ " left join party p2 on deo.sp_id = p2.id "
				+ " left join contact c1 on p1.contact_id = c1.id "
				+ " left join contact c2 on p2.contact_id = c2.id "
				+ " left join location l1 on deo.route_from = l1.code "
				+ " left join location l2 on deo.route_to =l2.code "
				+ " LEFT JOIN warehouse w ON w.location = deo.route_to"
				+ " LEFT JOIN office wo ON wo.id = w.office_id"
				+ " left join transfer_order tr  on tr.id = dt.order_id"
				+ conditions
				+ " and ifnull(deo.pickup_flag,'') !='Y' and deo.combine_type = 'DEPART'  and ifnull(deo.status,'') != '新建' and "
				+ " ( t.office_id IN (SELECT office_id FROM user_office WHERE user_name = '"+ currentUser.getPrincipal()+"') "//o.id
                + " or"
                + " w.office_id IN (SELECT office_id FROM user_office WHERE user_name = '"+ currentUser.getPrincipal()+"')"
                + " )"
				+ " and deo.status!='手动删除' and tr.customer_id in (select customer_id from user_customer where user_name='"
				+ currentUser.getPrincipal() + "')";

		sql = "select deo.id,deo.booking_note_number,deo.depart_no ,deo.departure_time,deo.charge_type,deo.create_stamp ,deo.status as depart_status,c2.contact_person driver,c2.phone,"
				+ " c1.abbr cname,c2.abbr spname,o.office_name office_name, l1.name route_from,l2.name route_to, t.arrival_mode arrival_mode,"
				+ " deo.arrival_time plan_time, t.arrival_time arrival_time, deo.remark, "
				+ " (SELECT	group_concat(CAST(tr.planning_time AS char) SEPARATOR '\r\n') FROM transfer_order tr, depart_transfer dt where dt.depart_id = deo.id and tr.id = dt.order_id) AS planning_time,"
				+ " ((case when t.operation_type != 'out_source'"
				+ " then (SELECT ifnull(sum(dt.amount),0) FROM depart_transfer dt LEFT JOIN transfer_order tor on tor.id = dt.order_id LEFT JOIN depart_pickup dp on dp.pickup_id = dt.pickup_id "
				+ " WHERE dp.depart_id = deo.id and tor.cargo_nature = 'cargo' and tor.id = t.id)"
				+ " else"
				+ " (select sum(amount) from transfer_order_item toi where toi.order_id = t.id)"
				+ " end) "
				+ "+ (SELECT count(0) FROM transfer_order_item_detail toid, depart_transfer dt, transfer_order tor WHERE dt.depart_id = deo.id and dt.order_id = tor.id and toid.order_id = tor.id and toid.depart_id = deo.id and tor.cargo_nature = 'ATM')) amount, "
				+ " (SELECT	tr.arrival_mode	FROM transfer_order tr, depart_transfer dt where dt.depart_id = deo.id and tr.id = dt.order_id  LIMIT 0,1) arrival_mode, "
				+ " (SELECT	group_concat(tr.order_no SEPARATOR '\r\n') FROM transfer_order tr, depart_transfer dt where dt.depart_id = deo.id and tr.id = dt.order_id) AS transfer_order_no, "
				+ " (SELECT	group_concat(	tr.customer_order_no SEPARATOR '\r\n') FROM transfer_order tr, depart_transfer dt where dt.depart_id = deo.id and tr.id = dt.order_id) AS customer_order_no,"
				+ " (SELECT	ifnull(location, '')	FROM	transfer_order_milestone tom, depart_order deo where tom.depart_id = deo.id ORDER BY	tom.id DESC LIMIT 0,1) location, "
				+ " (SELECT ifnull(exception_record, '') FROM transfer_order_milestone tom LEFT JOIN depart_order deo on tom.depart_id = deo.id ORDER BY tom.id DESC LIMIT 0,1) exception_record, "
				+ " (select dt.order_id from depart_transfer dt where dt.depart_id = deo.id limit 0,1) order_id,  "
				+ " deo.transfer_type as trip_type,"
				+ " w.office_id route_to_office_id, "
                + " wo.office_name route_to_office_name,"
                + " if((w.office_id in ( SELECT office_id FROM user_office WHERE user_name = '"+ currentUser.getPrincipal()+"')) && w.office_id != t.office_id ,'Y','N') wai_flag"
				+ " from depart_order deo "
				+ " left join carinfo c on deo.carinfo_id = c.id "
				+ " left join depart_transfer dt on dt.depart_id = deo.id "
				+ " left join transfer_order t on t.id = dt.order_id "
				+ " left join office o on o.id = t.office_id "
				+ " left join party p1 on t.customer_id = p1.id "
				+ " left join party p2 on deo.sp_id = p2.id "
				+ " left join contact c1 on p1.contact_id = c1.id "
				+ " left join contact c2 on p2.contact_id = c2.id "
				+ " left join location l1 on deo.route_from = l1.code "
				+ " left join location l2 on deo.route_to =l2.code "
				+ " LEFT JOIN warehouse w ON w.location = deo.route_to"
				+ " LEFT JOIN office wo ON wo.id = w.office_id"
				+ " left join transfer_order tr  on tr.id = dt.order_id"
				+ conditions
				+ " and ifnull(deo.pickup_flag,'') !='Y' and deo.combine_type = 'DEPART'  and ifnull(deo.status,'') != '新建' and "
				+ " ( t.office_id IN (SELECT office_id FROM user_office WHERE user_name = '"+ currentUser.getPrincipal()+"') "//o.id
                + " or"
                + " w.office_id IN (SELECT office_id FROM user_office WHERE user_name = '"+ currentUser.getPrincipal()+"')"
                + " )"
				+ " and deo.status!='手动删除' and tr.customer_id in (select customer_id from user_customer where user_name='"
				+ currentUser.getPrincipal() + "')"
				+ " group by deo.id order by deo.id desc "
				+ sLimit;
		
        String flag = getPara("flag");
        if("new".equals(flag)){
            Record depart = new Record();
            Map map = new HashMap();
            map.put("sEcho", pageIndex);
            map.put("iTotalRecords", 0);
            map.put("iTotalDisplayRecords", 0);
            map.put("aaData", depart);
            renderJson(map);
        }else{
        	Record rec = Db.findFirst(sqlTotal);
    		long startMi = Calendar.getInstance().getTimeInMillis();
    		List<Record> departOrders = Db.find(sql);
    		long endMi = Calendar.getInstance().getTimeInMillis();
    		logger.debug("get result cost:" + (endMi - startMi));
    		Map map = new HashMap();
    		map.put("sEcho", pageIndex);
    		map.put("iTotalRecords", rec.getLong("total"));
    		map.put("iTotalDisplayRecords", rec.getLong("total"));
    		map.put("aaData", departOrders);
    		renderJson(map);
        }
	}
	
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_CREATE})
	public void add() {

		Map<String, String> customizeField = getCustomFile.getInstance()
				.getCustomizeFile(this);
		setAttr("customizeField", customizeField);
		render("/yh/departOrder/allTransferOrderList.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_CREATE})
	public void addForRouteSp() {

		Map<String, String> customizeField = getCustomFile.getInstance()
				.getCustomizeFile(this);
		setAttr("customizeField", customizeField);
		render("/yh/departOrder/allTransferOrderListForRouteSp.html");
	}

	// 修改发车单页面
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_UPDATE})
	@Before(Tx.class)
	public void edit() {
        createToken(DEPART_ORDER_TOKEN);
        
		String sql = "select do.*,co.contact_person,co.phone,u.user_name,(select group_concat(dt.order_id  separator',')  from depart_transfer  dt "
				+ "where dt.depart_id =do.id)as order_id from depart_order  do "
				+ "left join contact co on co.id in( select p.contact_id  from party p where p.id=do.driver_id ) "
				+ "left join user_login  u on u.id=do.create_by where do.combine_type ='"
				+ DepartOrder.COMBINE_TYPE_DEPART
				+ "' and do.id in("
				+ getPara("id") + ")";
		DepartOrder departOrder = DepartOrder.dao.findFirst(sql);
		setAttr("departOrder", departOrder);

		DepartTransferOrder departTransferOrder2 = DepartTransferOrder.dao
				.findFirst(
						"select * from depart_transfer where depart_id = ? order by id desc",
						departOrder.get("id"));

		TransferOrder transferOrderAttr = TransferOrder.dao
				.findById(departTransferOrder2.get("order_id"));
		setAttr("transferOrderAttr", transferOrderAttr);

		Long sp_id = departOrder.get("sp_id");
		if (sp_id != null) {
			Party sp = Party.dao.findById(sp_id);
			Contact spContact = Contact.dao.findById(sp.get("contact_id"));
			setAttr("spContact", spContact);
		}
		Long driverId = departOrder.get("driver_id");
		if (driverId != null) {
			Party driver = Party.dao.findById(driverId);
			Contact driverContact = Contact.dao.findById(driver
					.get("contact_id"));
			setAttr("driverContact", driverContact);
		}
		Long carinfoId = departOrder.get("carinfo_id");
		if (carinfoId != null) {
			Carinfo carinfo = Carinfo.dao.findById(carinfoId);
			setAttr("carinfo", carinfo);
		}
		UserLogin userLogin = UserLogin.dao.findById(departOrder
				.get("create_by"));
		setAttr("userLogin2", userLogin);
		setAttr("depart_id", getPara());
		String orderId = "";
		List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao
				.find("select * from depart_transfer where depart_id = ?",
						departOrder.get("id"));
		for (DepartTransferOrder departTransferOrder : departTransferOrders) {
			orderId += departTransferOrder.get("order_id") + ",";
		}
		orderId = orderId.substring(0, orderId.length() - 1);
		setAttr("localArr", orderId);

		String routeFrom = departOrder.get("route_from");
		Location locationFrom = null;
		if (routeFrom != null || !"".equals(routeFrom)) {
			List<Location> provinces = Location.dao
					.find("select * from location where pcode ='1'");
			Location l = Location.dao
					.findFirst("select * from location where code = (select pcode from location where code = '"
							+ routeFrom + "')");
			if (provinces.contains(l)) {
				locationFrom = Location.dao
						.findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
								+ routeFrom + "'");
			} else {
				locationFrom = Location.dao
						.findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
								+ routeFrom + "'");
			}
			setAttr("locationFrom", locationFrom);
		}

		String routeTo = departOrder.get("route_to");
		Location locationTo = null;
		if (routeTo != null || !"".equals(routeTo)) {
			List<Location> provinces = Location.dao
					.find("select * from location where pcode ='1'");
			Location l = Location.dao
					.findFirst("select * from location where code = (select pcode from location where code = '"
							+ routeTo + "')");
			if (provinces.contains(l)) {
				locationTo = Location.dao
						.findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
								+ routeTo + "'");
			} else {
				locationTo = Location.dao
						.findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
								+ routeTo + "'");
			}
			setAttr("locationTo", locationTo);
		}

		TransferOrderMilestone transferOrderMilestone = TransferOrderMilestone.dao
				.findFirst(
						"select * from transfer_order_milestone where pickup_id = ? order by create_stamp desc",
						departOrder.get("id"));
		setAttr("transferOrderMilestone", transferOrderMilestone);
		List<Record> paymentItemList = Collections.EMPTY_LIST;
		paymentItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("paymentItemList", paymentItemList);
		
		createToken("eedaToken");
		render("/yh/departOrder/editDepartOrder.html");
	}

	// 创建发车单的运输单列表
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_CREATE})
	public void createTransferOrderList() {
		String orderNo = getPara("orderNo");
		String status = getPara("status");
		String address = getPara("address");
		String customer = getPara("customer");
		String routeFrom = getPara("routeFrom");
		String routeTo = getPara("routeTo");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		Record rec = null;
		String sLimit = "";
		String sql = "";
		String sqlTotal = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		
//		String fromSql=" from v_create_depart vcd where "
//					+ " vcd.office_id in (select office_id from user_office where user_name='"+ currentUser.getPrincipal()	+ "') "
//					+ " and vcd.status not in('手动删除', '已入库') "
//					+ " and (vcd.is_direct_deliver != 1 or vcd.is_direct_deliver is null)"
//					+ " and vcd.customer_id in (select customer_id from user_customer where user_name='"	+ currentUser.getPrincipal() + "')"
//					+ " and (pickup_id is not null or operation_type='out_source')"
//					+ " and (cargo_nature='cargo' or total_amount !=0)";
		 sql= " SELECT tor.id AS id,tor.`STATUS`, tor.order_no AS order_no, '' AS pickup_id, '' AS pickup_no,"
				+ " tor.customer_order_no AS customer_order_no, tor.planning_time AS planning_time, l1. NAME AS route_from,"
				+ " l2. NAME AS route_to, c.abbr AS cname, tor.operation_type AS operation_type, tor.cargo_nature AS cargo_nature,"
				+ " ( CASE WHEN ( tor.cargo_nature = 'ATM' AND tor.operation_type = 'out_source' ) "
				+ " THEN ( SELECT count(*) FROM transfer_order_item_detail WHERE order_id = tor.id )"
				+ " WHEN ( tor.cargo_nature = 'cargo' AND tor.operation_type = 'out_source' ) "
				+ " THEN ( SELECT sum(amount) FROM transfer_order_item WHERE order_id = tor.id )"
				+ " END ) total_amount,"
				+ " '' AS doaddress, tor.arrival_mode AS arrival_mode, ifnull(tor.pickup_mode, '') AS pickup_mode, c2.abbr AS spname,"
				+ " tor.charge_type AS charge_type, o.office_name AS office_name, tor.create_stamp AS create_stamp ,tor.office_id,tor.customer_id "
				+ " FROM transfer_order tor "
				+ " LEFT JOIN location l1 ON l1. CODE = tor.route_from"
				+ " LEFT JOIN location l2 ON l2. CODE = tor.route_to"
				+ " LEFT JOIN party p ON p.id = tor.customer_id"
				+ " LEFT JOIN contact c ON c.id = p.contact_id"
				+ " LEFT JOIN party p2 ON p2.id = tor.sp_id"
				+ " LEFT JOIN contact c2 ON c2.id = p2.contact_id"
				+ " LEFT JOIN office o ON o.id = tor.office_id"
				+ " WHERE"
				+ " tor.operation_type = 'out_source'"
				+ " AND ( ifnull( tor.depart_assign_status, '' ) <> 'ALL' )"
				+ " AND tor. STATUS not in ('手动删除','取消') "
				+ " and tor.`STATUS` = '新建' "
				+ " AND tor.`order_type` <> 'replenishmentOrder'"
				+ " UNION"
				+ " SELECT DISTINCT"
				+ " tor.id AS id,tor.`STATUS`,  tor.order_no AS order_no,  cast(dor.id as char) AS pickup_id,  dor.depart_no AS pickup_no,"
				+ " tor.customer_order_no AS customer_order_no,  tor.planning_time AS planning_time,"
				+ " l1. NAME AS route_from,  l2. NAME AS route_to,  c.abbr AS cname, tor.operation_type AS operation_type,  tor.cargo_nature AS cargo_nature,"
				+ " ( CASE WHEN ( tor.cargo_nature = 'ATM' ) "
				+ " THEN ( SELECT count(*) FROM transfer_order_item_detail WHERE order_id = tor.id and pickup_id = dor.id )"
				+ " WHEN ( tor.cargo_nature = 'cargo' ) "
				+ " THEN ( select sum(amount) from depart_transfer where order_id = tor.id and pickup_id = dor.id )"
				+ " END ) total_amount,"
				+ " '' AS doaddress, tor.arrival_mode AS arrival_mode, ifnull(tor.pickup_mode, '') AS pickup_mode,"
				+ " c2.abbr AS spname, tor.charge_type AS charge_type, o.office_name AS office_name, tor.create_stamp AS create_stamp,tor.office_id,tor.customer_id "
				+ " FROM depart_order dor"
				+ " LEFT JOIN depart_transfer dt on dt.pickup_id = dor.id"
				+ " LEFT JOIN transfer_order tor ON dt.order_id = tor.id "
				+ " LEFT JOIN location l1 ON l1. CODE = tor.route_from"
				+ " LEFT JOIN location l2 ON l2. CODE = tor.route_to"
				
				+ " LEFT JOIN party p ON p.id = tor.customer_id"
				+ " LEFT JOIN contact c ON c.id = p.contact_id"
				+ " LEFT JOIN party p2 ON p2.id = tor.sp_id"
				+ " LEFT JOIN contact c2 ON c2.id = p2.contact_id"

				+ " LEFT JOIN office o ON o.id = tor.office_id"
				+ " WHERE "
				+ " dor.`STATUS` = '已入货场' "
				+ " AND ( dor.is_direct_deliver != 1 OR dor.is_direct_deliver IS NULL )"
				+ " AND tor. STATUS not in ('手动删除','取消') "
				+ " and (select GROUP_CONCAT(id) from depart_pickup where pickup_id = dor.id and order_id = tor.id) is null";
		
		String conditions=" where 1=1 and total_amount >0";
		if (StringUtils.isNotEmpty(orderNo)){
        	conditions+=" and UPPER(order_no) like '%"+orderNo+"%'";
        }
        if (StringUtils.isNotEmpty(status)){
        	conditions+=" and UPPER(status) like '%"+status+"%'";
        }   
        if (StringUtils.isNotEmpty(routeFrom)){
        	conditions+=" and UPPER(route_from) like '%"+routeFrom+"%'";
        }
        if (StringUtils.isNotEmpty(routeTo)){
        	conditions+=" and UPPER(route_to) like '%"+routeTo+"%'";
        }
        if (StringUtils.isNotEmpty(customer)){
        	conditions+=" and UPPER(cname) like '%"+customer+"%'";
        }
        if (StringUtils.isNotEmpty(address)){
        	conditions+=" and UPPER(doaddress) like '%"+address+"%'";
        }
        if (StringUtils.isNotEmpty(beginTime)){
        	beginTime = " and planning_time between'"+beginTime+"'";
        }else{
        	beginTime =" and planning_time between '1970-1-1'";
        }
        if (StringUtils.isNotEmpty(endTime)){
        	endTime =" and '"+endTime+"'";
        }else{
        	endTime =" and '3000-1-1'";
        }
        conditions+=beginTime+endTime;
        
        conditions+= " and office_id in (select office_id from user_office where user_name='"+ currentUser.getPrincipal()+ "') "
        		+ " and customer_id in (select customer_id from user_customer where user_name='" + currentUser.getPrincipal() + "')";

		
//		if (orderNo == null && status == null && address == null
//				&& customer == null && routeFrom == null && routeTo == null
//				&& beginTime == null && endTime == null) {			
//			sqlTotal = "select count(1) total "+ fromSql;
//			sql = "select * " + fromSql+"";
//		} else {
//			if (beginTime == null || "".equals(beginTime)) {
//				beginTime = "1-1-1";
//			}
//			if (endTime == null || "".equals(endTime)) {
//				endTime = "9999-12-31";
//			}
//			sqlTotal = "select count(1) total " + fromSql
//					+ " and vcd.order_no like '%"+ orderNo+ "%' "
//					+ " and vcd.status like '%"+ status+ "%' "
//					+ " and vcd.doaddress like '%"+ address+ "%' "
//					+ " and vcd.cname like '%"+ customer+ "%' "
//					+ " and vcd.route_from like '%"+ routeFrom+ "%' "
//					+ " and vcd.route_to like '%"+ routeTo+ "%' "
//					+ " and vcd.planning_time between '"+ beginTime+ "' and '"+ endTime+ "'"
//					+ " and vcd.status!='手动删除' "
//					+ " and vcd.office_id in (select office_id from user_office where user_name='"+ currentUser.getPrincipal()+ "') "
//					+ " and vcd.customer_id in (select customer_id from user_customer where user_name='" + currentUser.getPrincipal() + "')";
//
//			sql = "select * "  + fromSql
//					+ " and vcd.order_no like '%"+ orderNo+ "%' "
//					+ " and vcd.status like '%"+ status+ "%' "
//					+ " and vcd.doaddress like '%"+ address+ "%' "
//					+ " and vcd.cname like '%"+ customer+ "%' "
//					+ " and vcd.route_from like '%"+ routeFrom+ "%' "
//					+ " and vcd.route_to like '%"+ routeTo+ "%' "
//					+ " and vcd.planning_time between '"+ beginTime+ "' and '"+ endTime+ "'"
//					+ " and vcd.status!='手动删除' "
//					+ " and vcd.office_id in (select office_id from user_office where user_name='"+ currentUser.getPrincipal()+ "') "
//					+ " and vcd.customer_id in (select customer_id from user_customer where user_name='" + currentUser.getPrincipal() + "')";
//		}
		String orderByStr = " order by planning_time desc ";
        if(colName.length()>0){
        	orderByStr = " order by "+colName+" "+sortBy;
        }
			rec = Db.findFirst("select count(*) total from (select * from ("+sql+") A "+conditions+ ") B" );
			logger.debug("total records:" + rec.getLong("total"));
			List<Record> transferOrders = Db.find("select * from ("+sql+") A "+conditions+  orderByStr + sLimit);
			Map transferOrderListMap = new HashMap();
			transferOrderListMap.put("sEcho", pageIndex);
			transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
			transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
			transferOrderListMap.put("aaData", transferOrders);
			renderJson(transferOrderListMap);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_CREATE})
	// 创建发车单的运输单列表(干线供应商+整车)
	public void createTransferOrderListForRouteSp() {
		String orderNo = getPara("orderNo");
		String status = getPara("status");
		String address = getPara("address");
		String customer = getPara("customer");
		String routeFrom = getPara("routeFrom");
		String routeTo = getPara("routeTo");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		Record rec = null;
		String sLimit = "";
		String sql = "";
		String sqlTotal = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		if (orderNo == null && status == null && address == null
				&& customer == null && routeFrom == null && routeTo == null
				&& beginTime == null && endTime == null) {
			sqlTotal = "select count(1) total  from transfer_order tor "
					+ " left join party p on tor.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join location l1 on tor.route_from = l1.code "
					+ " left join location l2 on tor.route_to = l2.code"
					+ " left join office o on tor.office_id = o.id "
					+ " where tor.status = '新建' "
					+ " and ifnull(tor.pickup_assign_status, '') = '"
					+ TransferOrder.ASSIGN_STATUS_NEW
					+ "' "
					+ " and ifnull(tor.depart_assign_status, '') = '"
					+ TransferOrder.ASSIGN_STATUS_NEW
					+ "' "
					+ " and o.id in (select office_id from user_office where user_name='"
					+ currentUser.getPrincipal()
					+ "') "
					+ " and tor.customer_id in (select customer_id from user_customer where user_name='"
					+ currentUser.getPrincipal() + "')";
			rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));
			sql = "select distinct tor.id,tor.order_no,tor.customer_order_no,tor.planning_time,tor.operation_type,tor.cargo_nature, tor.arrival_mode ,"
					+ " round((select sum(ifnull(toi.volume,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_volume, "
					+ " round((select sum(ifnull(toi.sum_weight,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_weight, "
					+ " (select sum(toi.amount) from transfer_order_item toi where toi.order_id = tor.id) as total_amount,"
					+ " ifnull(dor.address, '') doaddress, ifnull(tor.pickup_mode, '') pickup_mode,tor.status,c.abbr cname,"
					+ " l1.name route_from,l2.name route_to,tor.create_stamp ,cont.abbr as spname,cont.id as spid, "
					+ " o.office_name office_name from transfer_order tor "
					+ " left join party p on tor.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on tor.sp_id = p2.id left join contact cont on  cont.id=p2.contact_id "
					+ " left join depart_transfer dt on tor.id = dt.order_id left join depart_order dor on dor.id = dt.depart_id "
					+ " left join location l1 on tor.route_from = l1.code "
					+ " left join location l2 on tor.route_to = l2.code "
					+ " left join office o on o.id = tor.office_id "
					+ " where tor.status = '新建' "
					+ " and ifnull(tor.pickup_assign_status, '') = '"
					+ TransferOrder.ASSIGN_STATUS_NEW
					+ "' "
					+ " and ifnull(tor.depart_assign_status, '') = '"
					+ TransferOrder.ASSIGN_STATUS_NEW
					+ "' "
					+ " and o.id in (select office_id from user_office where user_name='"
					+ currentUser.getPrincipal()
					+ "') "
					+ " and tor.customer_id in (select customer_id from user_customer where user_name='"
					+ currentUser.getPrincipal()
					+ "')"
					+ " order by tor.planning_time desc " + sLimit;
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			sqlTotal = "select count(1) total from transfer_order tor "
					+ " left join party p on tor.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join location l1 on tor.route_from = l1.code "
					+ " left join location l2 on tor.route_to = l2.code  "
					+ " left join office o on o.id = tor.office_id "
					+ " where tor.status = '新建' "
					+ " and ifnull(tor.pickup_assign_status, '') = '"
					+ TransferOrder.ASSIGN_STATUS_NEW
					+ "' "
					+ " and ifnull(tor.depart_assign_status, '') = '"
					+ TransferOrder.ASSIGN_STATUS_NEW
					+ "' "
					+ " and o.id in (select office_id from user_office where user_name='"
					+ currentUser.getPrincipal()
					+ "') "
					+ " and tor.customer_id in (select customer_id from user_customer where user_name='"
					+ currentUser.getPrincipal()
					+ "')"
					+ " and tor.order_no like '%"
					+ orderNo
					+ "%' "
					+ " and tor.status like '%"
					+ status
					+ "%' "
					+ " and tor.address like '%"
					+ address
					+ "%' "
					+ " and c.abbr like '%"
					+ customer
					+ "%' "
					+ " and l1.name like '%"
					+ routeFrom
					+ "%' "
					+ " and l2.name like '%"
					+ routeTo
					+ "%' "
					+ " and tor.planning_time between '"
					+ beginTime
					+ "' "
					+ " and '" + endTime + "' ";

			sql = "select distinct tor.id,tor.order_no,tor.customer_order_no,tor.planning_time,tor.operation_type,tor.cargo_nature, tor.arrival_mode ,"
					+ " round((select sum(ifnull(toi.volume,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_volume, "
					+ " round((select sum(ifnull(toi.sum_weight,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_weight, "
					+ " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
					+ " dor.address doaddress,tor.pickup_mode,tor.status,c.abbr cname,"
					+ " (select name from location where code = tor.route_from) route_from,(select name from location where code = tor.route_to) route_to,tor.create_stamp,tor.depart_assign_status,c2.abbr spname, "
					+ " o.office_name office_name from transfer_order tor "
					+ " left join party p on tor.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on tor.sp_id = p2.id  left join contact c2 on p2.contact_id = c2.id "
					+ " left join depart_transfer dt on tor.id = dt.order_id left join depart_order dor on dor.id = dt.depart_id "
					+ " left join location l1 on tor.route_from = l1.code "
					+ " left join location l2 on tor.route_to = l2.code "
					+ " left join office o on o.id = tor.office_id "
					+ " where tor.status = '新建' "
					+ " and ifnull(tor.pickup_assign_status, '') = '"
					+ TransferOrder.ASSIGN_STATUS_NEW
					+ "' "
					+ " and ifnull(tor.depart_assign_status, '') = '"
					+ TransferOrder.ASSIGN_STATUS_NEW
					+ "' "
					+ " and o.id in (select office_id from user_office where user_name='"
					+ currentUser.getPrincipal()
					+ "') "
					+ " and tor.customer_id in (select customer_id from user_customer where user_name='"
					+ currentUser.getPrincipal()
					+ "')"
					+ " and tor.order_no like '%"
					+ orderNo
					+ "%' "
					+ " and tor.status like '%"
					+ status
					+ "%' "
					+ " and tor.address like '%"
					+ address
					+ "%' "
					+ " and c.abbr like '%"
					+ customer
					+ "%' "
					+ " and l1.name like '%"
					+ routeFrom
					+ "%' "
					+ " and l2.name like '%"
					+ routeTo
					+ "%' "
					+ " and tor.planning_time between '"
					+ beginTime
					+ "' "
					+ " and '"
					+ endTime
					+ "' "
					+ " order by tor.planning_time desc " + sLimit;
		}
		rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		List<Record> transferOrders = Db.find(sql);

		Map transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);
		renderJson(transferOrderListMap);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_CREATE})
	@Before(Tx.class)
	public void createDepartOrder() {
	    createToken(this.DEPART_ORDER_TOKEN);
		String list = this.getPara("localArr");
		setAttr("localArr", list);
		setAttr("routeSp", getPara("routeSp"));
		setAttr("transfer_type", getPara("transfer_type"));
		setAttr("pickupIds", getPara("pickupIds"));
		String[] orderIds = list.split(",");
		int numone = 0;
		for (int i = 0; i < orderIds.length; i++) {
			String[] array = orderIds[i].split(":");
			String orderId = array[0];
			TransferOrder transferOrder = TransferOrder.dao
					.findById(orderId);
			if (numone == 0) {
				setAttr("transferOrder", transferOrder);
				setAttr("chargeType", transferOrder.get("charge_type"));

				numone = 1;
			}
			String routeFrom = transferOrder.get("route_from");
			Location locationFrom = LocationUtil.getLocation(routeFrom);
			if (locationFrom != null) {
				setAttr("locationFrom", locationFrom);
			}

			String routeTo = transferOrder.get("route_to");
			Location locationTo = LocationUtil.getLocation(routeTo);;
			if (routeTo != null || !"".equals(routeTo)) {
				setAttr("locationTo", locationTo);
			}
			if (transferOrder.get("sp_id") != null) {
				Party sp = Party.dao.findById(transferOrder.get("sp_id"));
				setAttr("partySp", sp);
				Contact spContact = Contact.dao.findById(sp.get("contact_id"));
				setAttr("spContact", spContact);
				break;
			}
		}

		logger.debug("localArr" + list);
		String order_no = null;
		setAttr("saveOK", false);
		TransferOrder transferOrder = new TransferOrder();
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));

		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);
		List<Record> paymentItemList = Collections.EMPTY_LIST;
		paymentItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("paymentItemList", paymentItemList);
		setAttr("audit_status", "新建");
		setAttr("status", "新建");
		setAttr("saveOK", false);
		render("/yh/departOrder/editDepartOrder.html");
	}

	// editTransferOrder 初始数据
	public void getInitDepartOrderItems() {
		String order_id = getPara("localArr");// 运输单id
		String tr_item = getPara("tr_item");// 货品id
		String item_detail = getPara("item_detail");// 单品id

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sqlTotal = "select count(1) total from transfer_order_item tof"
				+ " left join transfer_order  t_o  on tof.order_id =t_o.id "
				+ " left join contact c on c.id in (select contact_id from party p where t_o.customer_id=p.id)"
				+ " where tof.order_id in(" + order_id + ")";
		logger.debug("sql :" + sqlTotal);
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select tof.* , t_o.order_no as order_no, t_o.id as tr_order_id, c.abbr as customer  from transfer_order_item tof"
				+ " left join transfer_order  t_o  on tof.order_id = t_o.id "
				+ "left join contact c on c.id in (select contact_id from party p where t_o.customer_id=p.id)"
				+ " where tof.order_id in("
				+ order_id
				+ ")  order by c.id"
				+ sLimit;
		List<Record> departOrderitem = Db.find(sql);
		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));
		Map.put("aaData", departOrderitem);
		renderJson(Map);
	}

	// 保存发车单
	@Before(Tx.class)
	public void saveDepartOrder() {
//	    if(!validateToken(DEPART_ORDER_TOKEN)){
//	        renderJson(new DepartOrder());
//	        return;
//	    }
		String depart_id = getPara("depart_id");// 发车单id
		String charge_type = getPara("chargeType");// 供应商计费类型
		String car_type = getPara("car_type");// 供应商计费类型, 如果是整车，需要知道整车类型
		String ltlPriceType = getPara("ltlUnitType");// 如果是零担，需要知道零担计费类型：按体积，按重量
		String sp_id = getPara("sp_id");// 供应商id
		String volume=getPara("volume");//获取体积
		String weight=getPara("weight");//获取体重
		// 查找创建人id
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		String driver_id = getPara("driver_id");//
		String carinfoId = getPara("carinfoId");// 司机id
		String car_follow_name = getPara("car_follow_name");// 跟车人
		String car_follow_phone = getPara("car_follow_phone");// 跟车人电话
		Date createDate = Calendar.getInstance().getTime();
		String checkedDetail = getPara("checkedDetail");
		String uncheckedDetailIds = getPara("uncheckedDetail");
		String bookingNoteNumber = getPara("booking_note_number");
		String transfer_type = getPara("transfer_type");// 运输方式
		String[] list = getPara("orderid").split(",");
		String partySpId = getPara("partySpId");

		DepartOrder dp = null;
		if ("".equals(depart_id)) {
			dp = new DepartOrder();
			dp.set("charge_type", charge_type)
					.set("create_by", users.get(0).get("id"))
					.set("create_stamp", createDate)
					.set("combine_type", DepartOrder.COMBINE_TYPE_DEPART)
					.set("depart_no", OrderNoGenerator.getNextOrderNo("FC"))
					.set("remark", getPara("remark"))
					.set("car_follow_name", getPara("car_follow_name"))
					.set("car_follow_phone", getPara("car_follow_phone"))
					.set("route_from", getPara("route_from"))
					.set("route_to", getPara("route_to"))
					.set("status", getPara("status"))
					.set("ltl_price_type", ltlPriceType)
					.set("car_type", car_type)
					.set("driver", getPara("driver_name"))
					.set("phone", getPara("driver_phone"))
					.set("car_no", getPara("car_no"))
					.set("transfer_type", transfer_type)
					.set("booking_note_number", bookingNoteNumber);
			if (!"".equals(driver_id) && driver_id != null) {
				dp.set("driver_id", driver_id);
			} else {
				dp.set("driver_id", null);
			}
			if (!"".equals(carinfoId) && carinfoId != null) {
				dp.set("carinfo_id", carinfoId);
			}
			
			if ("".equals(sp_id)) {
				if (!"".equals(partySpId)) {
					dp.set("sp_id", partySpId);
				}
			} else {
				dp.set("sp_id", sp_id);
			}
			if (!"".equals(car_follow_name)) {
				dp.set("car_follow_name", car_follow_name);
			}
			if (!"".equals(car_follow_phone)) {
				dp.set("car_follow_phone", car_follow_phone);
			}
			if(!"".equals(volume)){
				dp.set("volume", volume);
			}
			if(!"".equals(weight)){
				dp.set("weight", weight);
			}
			if (getParaToDate("arrival_time") != null) {
				dp.set("arrival_time", getPara("arrival_time"));
			}
			if (getParaToDate("departure_time") != null) {
				dp.set("departure_time", getPara("departure_time"));
			}
			dp.set("audit_status", "新建");
			dp.set("sign_status", "未回单");
			
			dp.save();
			
			saveDepartOrderMilestone(dp);
			if (!"".equals(partySpId)) {
				updateTransferOrderSp(dp);
			}

			// 如果是整车发车单需要更新运输单的信息
			String routeSp = getPara("routeSp");
			if (routeSp != null && !"".equals(routeSp)) {
				transferOrderForRouteSp(dp);
			}
	
			// 更新运输单发车状态，新建发车单从表
			for (int i = 0; i < list.length; i++) {
				String[] array = list[i].split(":");
				String orderId = array[0];
				String pickupId = null;
				if(array.length>1)
					pickupId = array[1];
				if("wu".equals(pickupId)){
					pickupId = null;
				}
				
				//
				if(dp.get("office_id")==null){
    				TransferOrder tor = TransferOrder.dao.findById(orderId);
    				dp.set("office_id", tor.get("office_id")).update();
    			}

				TransferOrder transferOrder = TransferOrder.dao.findById(orderId);
				DepartTransferOrder departTransferOrder = new DepartTransferOrder();
				departTransferOrder.set("depart_id", dp.get("id"));
				departTransferOrder.set("order_id",orderId);
				departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
				departTransferOrder.save();
				// 记录调车单中单品的发车单ID，//发车单从表记录所选的调车单
				if ("整车".equals(transfer_type) || pickupId == "" || pickupId == null ) {
					List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
							.find("select * from transfer_order_item_detail where order_id = '"
									+ orderId + "';");
					for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
						transferOrderItemDetail.set("depart_id", dp.get("id"));
						transferOrderItemDetail.update();
					}

					DepartPickupOrder departPickup = new DepartPickupOrder();
					departPickup.set("depart_id", dp.get("id"))
							.set("order_id", orderId).save();

					//更新对应的运输单
					transferOrder.set("depart_assign_status",
							TransferOrder.ASSIGN_STATUS_ALL);
					transferOrder.set("status", "处理中");
					transferOrder.update();
				} else {
					//更新depart_pickup表数据
					if (!"".equals(pickupId)){
					List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
							.find("select * from transfer_order_item_detail where order_id = '"
									+ orderId
									+ "' and pickup_id = ?",
									pickupId);
					for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
						transferOrderItemDetail.set("depart_id",
								dp.get("id"));
						transferOrderItemDetail.update();
					}

					DepartPickupOrder departPickup = new DepartPickupOrder();
					departPickup.set("depart_id", dp.get("id"))
							.set("pickup_id", pickupId)
							.set("order_id", orderId).save();
					}

					// 验证是否已全部发车完成，调车单全部提货完成的情况下进行判断
					if (TransferOrder.ASSIGN_STATUS_ALL.equals(transferOrder
							.get("pickup_assign_status"))) {
						if (transferOrder.get("cargo_nature").equals("ATM")) {
							// 运输单单品总数
							Record totalTransferOrderAmount = Db
									.findFirst("select count(0) total from transfer_order_item_detail where order_id = "
											+ orderId);
							// 总提货数量（之前+现在）
							Record totalPickAmount = Db
									.findFirst("select count(0) total from transfer_order_item_detail where depart_id is not null and order_id = "
											+ orderId);
							// 运输单
							if (totalPickAmount.getLong("total") == totalTransferOrderAmount
									.getLong("total")) {
								transferOrder.set("depart_assign_status",
										TransferOrder.ASSIGN_STATUS_ALL);
							} else {
								transferOrder.set("depart_assign_status",
										TransferOrder.ASSIGN_STATUS_PARTIAL);
							}
						} else {
							//普货已发车总数量
							Record re = Db.findFirst("select sum(amount) total from transfer_order_item where order_id = ?", orderId);
						    double total = re.getDouble("total");
							
						    
							if(!transferOrder.getStr("operation_type").equals("out_source")){
								 re = Db.findFirst("select sum(amount) yifa from (select dt.* from depart_transfer dt"
												+ " LEFT JOIN depart_pickup dp on dt.pickup_id = dp.pickup_id"
												+ " where dp.depart_id is not null "
												+ " and dt.order_id = ? group by dt.id ) a", orderId);
								double amount = re.getDouble("yifa");//已做发车单的数量（之前+现在）
								
								if(total == amount){
									transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
								}else{
									transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
								}
							}else{
								transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
							}
						}
					} else {
						transferOrder.set("depart_assign_status",
								TransferOrder.ASSIGN_STATUS_PARTIAL);
					}
					transferOrder.update();
				}
			}
		} else {// TODO update不需要更改create_by, create_date
			dp = DepartOrder.dao.findById(Integer.parseInt(depart_id));
			dp.set("charge_type", charge_type)
					.set("combine_type", DepartOrder.COMBINE_TYPE_DEPART)
					.set("remark", getPara("remark"))
					.set("car_follow_name", getPara("car_follow_name"))
					.set("car_follow_phone", getPara("car_follow_phone"))
					.set("route_from", getPara("route_from"))
					.set("route_to", getPara("route_to"))
					.set("ltl_price_type", ltlPriceType)
					.set("car_type", car_type)
					.set("driver", getPara("driver_name"))
					.set("phone", getPara("driver_phone"))
					.set("car_no", getPara("car_no"))
					.set("booking_note_number", bookingNoteNumber);
			if (!"".equals(driver_id) && driver_id != null) {
				dp.set("driver_id", driver_id);
			} else {
				dp.set("driver_id", null);
			}
			if (!"".equals(carinfoId) && carinfoId != null) {
				dp.set("carinfo_id", carinfoId);
			} else {
				dp.set("carinfo_id", null);
			}
			if (!"".equals(sp_id)) {
				dp.set("sp_id", Integer.parseInt(sp_id));
			}
			if (!"".equals(car_follow_name)) {
				dp.set("car_follow_name", car_follow_name);
			}
			if (!"".equals(car_follow_phone)) {
				dp.set("car_follow_phone", car_follow_phone);
			}
			if (getParaToDate("arrival_time") != null) {
				dp.set("arrival_time", getPara("arrival_time"));
			}
			if (getParaToDate("departure_time") != null) {
				dp.set("departure_time", getPara("departure_time"));
			}
			dp.update();
//			updateDepartTransfer(dp, getPara("orderid"), checkedDetail,
//					uncheckedDetailIds);
			if (!"".equals(sp_id)) {
				updateTransferOrderSp(dp);
			}
		}
		
		//更新运输单供应商（同步发车单）
		for (int i = 0; i < list.length; i++) {
			String[] array = list[i].split(":");
			String orderId = array[0];
			TransferOrder transferOrder = TransferOrder.dao.findById(orderId);
			if ("".equals(sp_id)) {
				if (!"".equals(partySpId)) {
					transferOrder.set("sp_id", partySpId).update();
				}
			} else {
				transferOrder.set("sp_id", sp_id).update();
			}
		}
//		createToken(DEPART_ORDER_TOKEN);
//		String serverTokenId = getSessionAttr(DEPART_ORDER_TOKEN);
//		logger.debug("DEPART_ORDER_TOKEN:"+serverTokenId);
//		dp.put(DEPART_ORDER_TOKEN, serverTokenId);
		renderJson(dp);
	}

	// 如果是整车发车单需要更新运输单的信息
	@Before(Tx.class)
	private void transferOrderForRouteSp(DepartOrder dp) {
		List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao
				.find("select * from depart_transfer where depart_id = ?",
						dp.get("id"));
		for (DepartTransferOrder departTransferOrder : departTransferOrders) {
			TransferOrder transferOrder = TransferOrder.dao
					.findById(departTransferOrder.get("order_id"));
			transferOrder.set("pickup_mode", "routeSP");
			transferOrder.set("charge_type", "perCar");
			transferOrder.update();
		}
	}

	// 更新运输单的供应商
	@Before(Tx.class)
	private void updateTransferOrderSp(DepartOrder dp) {
		List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao
				.find("select * from depart_transfer where depart_id = ?",
						dp.get("id"));
		for (DepartTransferOrder departTransferOrder : departTransferOrders) {
			TransferOrder transferOrder = TransferOrder.dao
					.findById(departTransferOrder.get("order_id"));
			transferOrder.set("sp_id", dp.get("sp_id"));
			transferOrder.update();
		}
	}

	// 保存发车里程碑
	@Before(Tx.class)
	private void saveDepartOrderMilestone(DepartOrder pickupOrder) {
		TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
		transferOrderMilestone.set("status", "新建");
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		transferOrderMilestone.set("create_by", users.get(0).get("id"));
		transferOrderMilestone.set("location", "");
		transferOrderMilestone.set("exception_record", "");
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		transferOrderMilestone.set("create_stamp", sqlDate);
		transferOrderMilestone.set("type",
				TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
		transferOrderMilestone.set("depart_id", pickupOrder.get("id"));
		transferOrderMilestone.save();
	}

	// 更新中间表
	@Before(Tx.class)
	private void updateDepartTransfer(DepartOrder pickupOrder, String orderId,
			String checkedDetail, String uncheckedDetailId) {
		if (checkedDetail != null && !"".equals(checkedDetail)) {
			String[] checkedDetailIds = checkedDetail.split(",");
			TransferOrderItemDetail transferOrderItemDetail = null;
			for (int j = 0; j < checkedDetailIds.length
					&& checkedDetailIds.length > 0; j++) {
				transferOrderItemDetail = TransferOrderItemDetail.dao
						.findById(checkedDetailIds[j]);
				transferOrderItemDetail.set("depart_id", pickupOrder.get("id"));
				transferOrderItemDetail.update();
			}
			TransferOrder transferOrder = TransferOrder.dao
					.findById(transferOrderItemDetail.get("order_id"));
			transferOrder.set("depart_assign_status",
					TransferOrder.ASSIGN_STATUS_PARTIAL);
			transferOrder.update();
		}
		String[] uncheckedDetailIds = uncheckedDetailId.split(",");
		if (uncheckedDetailId != null && !"".equals(uncheckedDetailId)) {
			TransferOrderItemDetail transferOrderItemDetail = null;
			for (int j = 0; j < uncheckedDetailIds.length
					&& uncheckedDetailIds.length > 0; j++) {
				transferOrderItemDetail = TransferOrderItemDetail.dao
						.findById(uncheckedDetailIds[j]);
				transferOrderItemDetail.set("depart_id", null);
				transferOrderItemDetail.update();
			}
		}
		if (uncheckedDetailIds.length == 0 || "".equals(uncheckedDetailIds[0])) {
			List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
					.find("select * from transfer_order_item_detail where order_id in("
							+ orderId + ")");
			String str = "";
			for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
				Long departId = transferOrderItemDetail.get("depart_id");
				if (departId == null || "".equals(departId)) {
					str += departId;
				}
			}
			if ("".equals(str)) {
				List<TransferOrder> transferOrders = TransferOrder.dao
						.find("select * from transfer_order where id in ("
								+ orderId + ")");
				for (TransferOrder transferOrder : transferOrders) {
					transferOrder.set("depart_assign_status",
							TransferOrder.ASSIGN_STATUS_ALL);
					transferOrder.update();
				}
			}
		}
	}

	@Before(Tx.class)
	public void saveupdatestate() {
		String depart_id = getPara("depart_id");// 发车单id
		String order_state = getPara("order_state");// 状态

		int nummber = 0;// 没入库的货品数量
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		String name = (String) currentUser.getPrincipal();
		UserLogin users = UserLogin.dao
				.findFirst("select * from user_login where user_name='" + name
						+ "'");
		DepartOrder departOrder = DepartOrder.dao.findById(Integer.parseInt(depart_id));
		
		// 生成应付, （如果已经有了应付，就要清除掉旧数据重新算）
		// 计件/整车/零担 生成发车单中供应商的应付，要算 item 的数量 * 合同中定义的价格
		// Depart_Order_fin_item 提货单/发车单应付明细表
		// 第一步：看发车单调度选择的计费方式是哪种：计件，整车，零担
		// 第二步：循环所选运输单中的 item, 到合同中（循环）比对去算钱。

		List<DepartTransferOrder> dItem = DepartTransferOrder.dao
				.find("select order_id from depart_transfer where depart_id ="
						+ depart_id + "");
		List<DepartOrderFinItem> dofi = DepartOrderFinItem.dao
				.find("select depart_order_id from depart_order_fin_item where depart_order_id ="
						+ depart_id + "");

		String transferIds = "";
		for (DepartTransferOrder dItem2 : dItem) {
			transferIds += dItem2.get("order_id") + ",";
		}

		if (transferIds.length() > 0)
			transferIds = transferIds.substring(0, transferIds.length() - 1);
		
		String finItemOrderIds1 = "";
		for (DepartOrderFinItem dofi1 : dofi) {
			finItemOrderIds1 += dofi1.get("depart_order_id") + ",";
		}
		List<Record> transferOrderItemList = Db
				.find("select toi.*, t_o.route_from, t_o.route_to,t_o.cargo_nature from transfer_order_item toi left join transfer_order t_o on toi.order_id = t_o.id where toi.order_id in("
						+ transferIds + ") order by pickup_seq desc");
		
		if (finItemOrderIds1.length() > 0) {
			// 如果已生成应付，就不再生成应付
		} else {
			calcCost(departOrder, transferOrderItemList);
		}
		if ("已签收".equals(order_state)) {
			// 生成回单
			Date createDate = Calendar.getInstance().getTime();
			String orderNo = OrderNoGenerator.getNextOrderNo("HD");

			ReturnOrder returnOrder = new ReturnOrder();
			returnOrder.set("order_no", orderNo);
			returnOrder.set("depart_order_id", depart_id);
			returnOrder.set("order_type", "应收");
			returnOrder.set("transaction_status", "新建");
			returnOrder.set("creator", users.get("id"));
			returnOrder.set("create_date", createDate);
			returnOrder.save();

		}

		Map Map = new HashMap();
		Map.put("amount", nummber);
		Map.put("depart", departOrder);
		renderJson(Map);
	}
	// TODO 发车单应付计费 先获取有效期内的合同，条目越具体优先级越高
	// 级别1：计费类别 + 货 品 + 始发地 + 目的地
	// 级别2：计费类别 + 货 品 + 目的地
	// 级别3：计费类别 + 始发地 + 目的地
	// 级别4：计费类别 + 目的地

	// priceType 不分大小写在mysql会有问题
	private void calcCost(DepartOrder departOrder,
			List<Record> transferOrderItemList) {
		Long spId = departOrder.getLong("sp_id");
		if (spId == null)
			return;
		getFinNoContractCost(departOrder);
		String orderCreateDate = departOrder.getDate("create_stamp").toString();
		// 先获取有效期内的sp合同, 如有多个，默认取第一个
		Contract spContract = Contract.dao
				.findFirst("select c.* from contract c left join contract_item ci on ci.contract_id = c.id where c.type='SERVICE_PROVIDER' "
						+ "and ('"
						+ departOrder.getDate("create_stamp")
						+ "' between c.period_from and c.period_to) and c.party_id = "
						+ spId
						+ " and ci.from_id = '"
						+ departOrder.get("route_from")
						+ "' and ci.to_id = '"
						+ departOrder.get("route_to") + "'");
		if (spContract == null)
			return;

		String chargeType = departOrder.get("charge_type");

		if (spId != null) {
			if ("perUnit".equals(chargeType)) {
				genFinPerUnit(departOrder, transferOrderItemList, spContract,
						chargeType);
			} else if ("perCar".equals(chargeType)) {
				genFinPerCar(departOrder, transferOrderItemList, spContract,
						chargeType);
			} else if ("perCargo".equals(chargeType)) {
				// 每次都新生成一个helper来处理计算，防止并发问题。
				DepartOrderPaymentHelper.getInstance().genFinPerCargo(
						departOrder, transferOrderItemList, spContract,
						chargeType);
			}
		}
	}

	private void genFinPerCar(DepartOrder departOrder,
			List<Record> transferOrderItemList, Contract spContract,
			String chargeType) {
		// 根据发车单整车的车型，始发地，目的地，计算合同价
		boolean isFinContract = true;
		for (Record record : transferOrderItemList) {
			TransferOrder transferOrder = TransferOrder.dao.findFirst(
					"select * from transfer_order where id = ?",
					record.get("order_id"));
			Boolean isTrue = transferOrder.get("no_contract_cost");
			if (isTrue) {
				isFinContract = false;
			}

		}
		// TransferOrder transfer=
		// TransferOrder.dao.findFirst("select * from transfer_order where id = ?",transferOrderItemList.get(0).get("order_id"));
		/* getFinNoContractCost(departOrder); */
		if (isFinContract) {
			Record contractFinItem = Db
					.findFirst("select amount, fin_item_id from contract_item where contract_id ="
							+ spContract.getLong("id")
							+ " and carType = '"
							+ departOrder.get("car_type")// 对应发车单的 car_type
							+ "' and from_id = '"
							+ departOrder.get("route_from")
							+ "' and to_id = '"
							+ departOrder.get("route_to")
							+ "' and priceType='"
							+ chargeType + "'");
			if (contractFinItem != null) {
				genFinItem(departOrder, null, contractFinItem, chargeType);
			} else {
				contractFinItem = Db
						.findFirst("select amount, fin_item_id from contract_item where contract_id ="
								+ spContract.getLong("id")
								+ " and carType = '"
								+ departOrder.get("car_type")// 对应发车单的 car_type
								+ "' and from_id = '"
								+ departOrder.get("route_from")
								+ "' and to_id = '"
								+ departOrder.get("route_to")
								+ "' and priceType='" + chargeType + "'");
				if (contractFinItem != null) {
					genFinItem(departOrder, null, contractFinItem, chargeType);
				}/*
				 * else{ contractFinItem = Db .findFirst(
				 * "select amount, fin_item_id from contract_item where contract_id ="
				 * +spContract.getLong("id") +" and from_id = '" +
				 * departOrder.get("route_from") +"' and to_id = '" +
				 * departOrder.get("route_to") +
				 * "' and priceType='"+chargeType+"'"); if (contractFinItem !=
				 * null) { genFinItem(users, departOrder, null, contractFinItem,
				 * chargeType); }
				 */
				else {
					contractFinItem = Db
							.findFirst("select amount, fin_item_id from contract_item where contract_id ="
									+ spContract.getLong("id")
									+ " and carType = '"
									+ departOrder.get("car_type")// 对应发车单的
																	// car_type
									+ "' and to_id = '"
									+ departOrder.get("route_to")
									+ "' and priceType='" + chargeType + "'");
					if (contractFinItem != null) {
						genFinItem(departOrder, null, contractFinItem,
								chargeType);
					}
				}
			}
		}
	}

	private void genFinPerUnit(DepartOrder departOrder,
			List<Record> transferOrderItemList, Contract spContract,
			String chargeType) {

		// TransferOrder transfer =
		// TransferOrder.dao.findFirst("select * from transfer_order where id = ?",transferOrderItemList.get(0).get("order_id"));

		for (Record tOrderItemRecord : transferOrderItemList) {
			// TODO:获取到运输单，并且判断是否要计算合同
			TransferOrder transferOrder = TransferOrder.dao.findFirst(
					"select * from transfer_order where id = ?",
					tOrderItemRecord.get("order_id"));
			Boolean isTrue = transferOrder.get("no_contract_cost");
			if (!isTrue) {
				Record contractFinItem = Db
						.findFirst("select amount, fin_item_id from contract_item where contract_id ="
								+ spContract.getLong("id")
								+ " and product_id = "
								+ tOrderItemRecord.get("product_id")
								+ " and from_id = '"
								+ departOrder.get("route_from")
								+ "' and to_id = '"
								+ departOrder.get("route_to")
								+ "' and priceType='" + chargeType + "'");
				if (contractFinItem != null) {
					genFinItem(departOrder, tOrderItemRecord, contractFinItem,
							chargeType);
				} else {
					contractFinItem = Db
							.findFirst("select amount, fin_item_id from contract_item where contract_id ="
									+ spContract.getLong("id")
									+ " and product_id = "
									+ tOrderItemRecord.get("product_id")
									+ " and to_id = '"
									+ departOrder.get("route_to")
									+ "' and priceType='" + chargeType + "'");
					if (contractFinItem != null) {
						genFinItem(departOrder, tOrderItemRecord,
								contractFinItem, chargeType);
					} else {
						contractFinItem = Db
								.findFirst("select amount, fin_item_id from contract_item where contract_id ="
										+ spContract.getLong("id")
										+ " and from_id = '"
										+ departOrder.get("route_from")
										+ "' and to_id = '"
										+ departOrder.get("route_to")
										+ "' and priceType='"
										+ chargeType
										+ "'");
						if (contractFinItem != null) {
							genFinItem(departOrder, tOrderItemRecord,
									contractFinItem, chargeType);
						} else {
							contractFinItem = Db
									.findFirst("select amount, fin_item_id from contract_item where contract_id ="
											+ spContract.getLong("id")
											+ " and to_id = '"
											+ departOrder.get("route_to")
											+ "' and priceType='"
											+ chargeType
											+ "'");
							if (contractFinItem != null) {
								genFinItem(departOrder, tOrderItemRecord,
										contractFinItem, chargeType);
							}
						}
					}
				}
			}
		}
	}

	private void genFinItem(DepartOrder departOrder, Record tOrderItemRecord,
			Record contractFinItem, String chargeType) {
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
		DepartOrderFinItem departOrderFinItem = new DepartOrderFinItem();

		departOrderFinItem.set("fin_item_id",
				contractFinItem.get("fin_item_id"));
		String name = (String) currentUser.getPrincipal();
		UserLogin users = UserLogin.dao
				.findFirst("select * from user_login where user_name='" + name
						+ "'");
		if ("perCar".equals(chargeType)) {
			// 整车生成应付条目
			genFinItemPerCar(departOrder, contractFinItem, now,
					departOrderFinItem, users);
		} else {
			if (tOrderItemRecord != null) {
				String cargo_nature = tOrderItemRecord.get("cargo_nature");
				double money = 0;
				if (cargo_nature.equals("cargo")) {// 普货计件
					genFinItemNormalCargo(departOrder, tOrderItemRecord,
							contractFinItem, now, departOrderFinItem, users);
				} else {// ATM 计件
					genFinItemPerATM(departOrder, tOrderItemRecord,
							contractFinItem, now, departOrderFinItem, users);
				}
			}
		}
	}

	private void genFinItemPerATM(DepartOrder departOrder,
			Record tOrderItemRecord, Record contractFinItem,
			java.sql.Timestamp now, DepartOrderFinItem departOrderFinItem,
			UserLogin users) {
		double money;
		Record record = Db
				.findFirst("select count(toid.id) as amount from transfer_order_item_detail toid where item_id = "
						+ tOrderItemRecord.get("id")
						+ " and depart_id = "
						+ departOrder.get("id"));
		if (record.getLong("amount") != 0) {
			money = contractFinItem.getDouble("amount")
					* Double.parseDouble(record.get("amount").toString());
			BigDecimal bg = new BigDecimal(money);
			double amountDouble = bg.setScale(2, BigDecimal.ROUND_HALF_UP)
					.doubleValue();
			departOrderFinItem.set("amount", amountDouble);
			departOrderFinItem.set("transfer_order_id",
					tOrderItemRecord.get("order_id"));
			departOrderFinItem.set("transfer_order_item_id",
					tOrderItemRecord.get("id"));

			saveFinItem(departOrder, now, departOrderFinItem, users);
		}
	}
	private void genFinItemNormalCargo(DepartOrder departOrder,
			Record tOrderItemRecord, Record contractFinItem,
			java.sql.Timestamp now, DepartOrderFinItem departOrderFinItem,
			UserLogin users) {
		double money;
		money = contractFinItem.getDouble("amount")
				* Double.parseDouble(tOrderItemRecord.get("amount").toString());
		BigDecimal bg = new BigDecimal(money);
		double amountDouble = bg.setScale(2, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
		departOrderFinItem.set("amount", amountDouble);
		departOrderFinItem.set("transfer_order_id",
				tOrderItemRecord.get("order_id"));
		departOrderFinItem.set("transfer_order_item_id",
				tOrderItemRecord.get("id"));

		saveFinItem(departOrder, now, departOrderFinItem, users);
	}
	private void genFinItemPerCar(DepartOrder departOrder,
			Record contractFinItem, java.sql.Timestamp now,
			DepartOrderFinItem departOrderFinItem, UserLogin users) {
		double money = contractFinItem.getDouble("amount");
		BigDecimal bg = new BigDecimal(money);
		double amountDouble = bg.setScale(2, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
		departOrderFinItem.set("amount", amountDouble);

		saveFinItem(departOrder, now, departOrderFinItem, users);
	}
	
	@Before(Tx.class)
	private void saveFinItem(DepartOrder departOrder, java.sql.Timestamp now,
			DepartOrderFinItem departOrderFinItem, UserLogin users) {
		departOrderFinItem.set("depart_order_id", departOrder.getLong("id"));
		departOrderFinItem.set("status", "未完成");
		departOrderFinItem.set("creator", users.get("id"));
		departOrderFinItem.set("create_date", now);
		departOrderFinItem.set("create_name",
				departOrderFinItem.CREATE_NAME_SYSTEM);
		departOrderFinItem.set("cost_source", "合同费用");
		departOrderFinItem.save();
	}

	// 点击货品table的查看 ，显示对应货品的单品
	public void itemDetailList() {
		String item_id = getPara("item_id");
		String depart_id = getPara("depart_id");// 发车单id
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from transfer_order_item_detail  where item_id ="
				+ item_id + "";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		List<Record> itemdetail = null;
		if ("".equals(depart_id)) {
			String sql_tr_itemdetail_id = "select id as itemdetail_id  from transfer_order_item_detail where item_id="
					+ item_id;
			String sql_dp_itemdetail_id = "select itemdetail_id  from depart_transfer_itemdetail  where item_id="
					+ item_id;
			List<Record> sql_tr_itemdetail_idlist = Db
					.find(sql_tr_itemdetail_id);
			List<Record> sql_dp_itemdetail_idlist = Db
					.find(sql_dp_itemdetail_id);
			/* List<Record> depart_itemdetail=Db.find(sql_item); */
			sql_tr_itemdetail_idlist.removeAll(sql_dp_itemdetail_idlist);
			String detail_id = "0";
			if (sql_tr_itemdetail_idlist.size() > 0) {
				for (int i = 0; i < sql_tr_itemdetail_idlist.size(); i++) {
					detail_id += sql_tr_itemdetail_idlist.get(i).get(
							"itemdetail_id")
							+ ",";
				}
				detail_id = detail_id.substring(0, detail_id.length() - 1);
			}
			String sql = "select * from transfer_order_item_detail  where ID in("
					+ detail_id + ")";
			itemdetail = Db.find(sql);
		} else {
			String sql = "select * from transfer_order_item_detail  where item_id in("
					+ item_id + ")";
			itemdetail = Db.find(sql);
		}

		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));
		Map.put("aaData", itemdetail);
		renderJson(Map);
	}

	// 发车单删除
	@Before(Tx.class)
	public void cancel() {
		String id = getPara();
		String sql = "select * from depart_transfer  where depart_id =" + id
				+ "";
		List<DepartTransferOrder> re_tr = DepartTransferOrder.dao.find(sql);
		for (int i = 0; i < re_tr.size(); i++) {
			DepartTransferOrder dep_tr = DepartTransferOrder.dao.findFirst(sql);
			dep_tr.set("depart_id", null).update();
			dep_tr.delete();
		}
		DepartOrder re = DepartOrder.dao.findById(id);
		re.set("driver_id", null).update();
		re.delete();
		render("/yh/departOrder/departOrderList.html");
	}

	// 单击tab里程碑
	public void transferOrderMilestoneList() {
		Map<String, List> map = new HashMap<String, List>();
		List<String> usernames = new ArrayList<String>();
		String departOrderId = getPara("departOrderId");
		if (departOrderId == "" || departOrderId == null) {
			departOrderId = "-1";
		}
		if (!"-1".equals(departOrderId)) {
			List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
					.find("select * from transfer_order_milestone where type = '"
							+ TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE
							+ "'and depart_id=" + departOrderId);
			for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
				UserLogin userLogin = UserLogin.dao
						.findById(transferOrderMilestone.get("create_by"));
				String username = userLogin.get("c_name");
				if (username == null || "".equals(username)) {
					username = userLogin.get("user_name");
				}
				usernames.add(username);
			}
			map.put("transferOrderMilestones", transferOrderMilestones);
			map.put("usernames", usernames);
		}
		renderJson(map);
	}

	// 编辑里程碑
	public void saveTransferOrderMilestone() {
		String milestoneDepartId = getPara("milestoneDepartId");
		Map<String, Object> map = new HashMap<String, Object>();
		if (milestoneDepartId != null && !"".equals(milestoneDepartId)) {
			DepartOrder departOrder = DepartOrder.dao
					.findById(milestoneDepartId);
			TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
			String status = getPara("status");
			String location = getPara("location");
			String exception_record = getPara("exception_record");
			transferOrderstatus(milestoneDepartId, status, location);
			if (!status.isEmpty()) {
				transferOrderMilestone.set("status", status);
				departOrder.set("status", status);
			} else {
				transferOrderMilestone.set("status", "在途");
				departOrder.set("status", "在途");
			}
			departOrder.update();
			if (!location.isEmpty()) {
				transferOrderMilestone.set("location", location);
			} else {
				transferOrderMilestone.set("location", "");
			}
			if (exception_record != null && "".equals(exception_record)) {
				transferOrderMilestone
						.set("exception_record", exception_record);
			} else {
				transferOrderMilestone.set("exception_record", "");
			}
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");

			transferOrderMilestone.set("create_by", users.get(0).get("id"));

			java.util.Date utilDate = new java.util.Date();
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(
					utilDate.getTime());

			transferOrderMilestone.set("create_stamp", sqlDate);
			transferOrderMilestone.set("depart_id", milestoneDepartId);
			transferOrderMilestone.set("type",
					TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
			transferOrderMilestone.save();

			map.put("transferOrderMilestone", transferOrderMilestone);
			UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone
					.get("create_by"));
			String username = userLogin.get("c_name");
			if (username == null || "".equals(username)) {
				username = userLogin.get("user_name");
			}
			map.put("username", username);
		}
		renderJson(map);
	}

	// 同步运输单状态里程碑
	@Before(Tx.class)
	public void transferOrderstatus(String de_or, String status, String location) {
		int depart_id = Integer.parseInt(de_or);
		List<DepartTransferOrder> dep = DepartTransferOrder.dao
				.find("select * from depart_transfer  where depart_id in("
						+ depart_id + ")");
		for (int i = 0; i < dep.size(); i++) {
			int order_id = Integer.parseInt(dep.get(i).get("order_id")
					.toString());
			TransferOrder tr = TransferOrder.dao.findById(order_id);
			TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
			if (!"".equals(status) && status != null) {
				if (tr.get("depart_assign_status") == TransferOrder.ASSIGN_STATUS_PARTIAL) {
					transferOrderMilestone.set("status", "部分" + status);
					tr.set("status", "部分" + status);
				} else {
					transferOrderMilestone.set("status", status);
					tr.set("status", status);
				}
			} else {
				if (tr.get("depart_assign_status") == TransferOrder.ASSIGN_STATUS_PARTIAL) {
					transferOrderMilestone.set("status", "部分在途");
					tr.set("status", "部分在途");
				} else {
					transferOrderMilestone.set("status", "在途");
					tr.set("status", "在途");
				}
			}
			tr.update();
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");
			transferOrderMilestone.set("create_by", users.get(0).get("id"));
			if (location == null || location.isEmpty()) {
				transferOrderMilestone.set("location", "");
			} else {
				transferOrderMilestone.set("location", location);
			}

			java.util.Date utilDate = new java.util.Date();
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(
					utilDate.getTime());
			transferOrderMilestone.set("create_stamp", sqlDate);
			transferOrderMilestone.set("type",
					TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
			transferOrderMilestone.set("order_id", order_id);
			transferOrderMilestone.save();

		}

	}

	// 在途运输单管理
	public void transferMilestoneIndex() {
		Map<String, String> customizeField = getCustomFile.getInstance()
				.getCustomizeFile(this);
		setAttr("customizeField", customizeField);
		render("/yh/departOrder/TransferOrderStatus.html");
	}

	public void ownTransferMilestone() {
		Map transferOrderListMap = null;
		String orderNo = getPara("orderNo");
		String status = getPara("status");
		String address = getPara("address");
		String customer = getPara("customer");
		String sp = getPara("sp");
		String officeName = getPara("officeName");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sqlTotal = "";
		String sql = "";
		Record rec = null;
		// String strWhere = DataTablesUtils.buildSingleFilter(this);
		if (orderNo == null && status == null && address == null
				&& customer == null && sp == null && beginTime == null
				&& endTime == null) {
			sqlTotal = "select count(1) total from transfer_order t where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='own'";
			sql = "select t.*,c1.abbr cname,c2.abbr spname,t.create_stamp,o.office_name oname from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='own'  order by create_stamp desc"
					+ sLimit;
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}

			sqlTotal = "select count(1) total from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id"
					+ " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='own' and t.order_no like '%"
					+ orderNo + "%' and t.status like '%" + status
					+ "%' and t.address like '%" + address
					+ "%' and c1.abbr like '%" + customer
					+ "%' and ifnull(c2.abbr,'') like '%" + sp
					+ "%' and o.office_name  like '%" + officeName
					+ "%' and create_stamp between '" + beginTime + "' and '"
					+ endTime + "'";

			sql = "select t.*,c1.abbr cname,c2.abbr spname,o.office_name oname from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id"
					+ " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='own' and t.order_no like '%"
					+ orderNo
					+ "%' and t.status like '%"
					+ status
					+ "%' and t.address like '%"
					+ address
					+ "%' and c1.abbr like '%"
					+ customer
					+ "%' and ifnull(c2.abbr,'') like '%"
					+ sp
					+ "%' and o.office_name  like '%"
					+ officeName
					+ "%' and create_stamp between '"
					+ beginTime
					+ "' and '"
					+ endTime + "' order by create_stamp desc" + sLimit;
		}
		rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		List<Record> transferOrders = Db.find(sql);

		transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);
		renderJson(transferOrderListMap);
	}

	// 回显供应商
	public void getIntsp() {
		Contact co = Contact.dao
				.findFirst("select * from contact  where id in(select contact_id  from party p where p.id="
						+ Integer.parseInt(getPara("sp_id").toString()) + " )");
		renderJson(co);
	}

	// 修改运输单仓库
	@Before(Tx.class)
	public void updateWarehouse(String depart_id, String house_id) {
		int de_id = Integer.parseInt(depart_id);
	}

	// 外包运输单更新
	@Before(Tx.class)
	public void transferonTrip() {
		Map<String, String> customizeField = getCustomFile.getInstance()
				.getCustomizeFile(this);
		setAttr("customizeField", customizeField);
		render("/yh/departOrder/transferOrderOnTripList.html");
	}

	public void transferonTriplist() {
		Map transferOrderListMap = null;
		String orderNo = getPara("orderNo");
		String status = getPara("status");
		String address = getPara("address");
		String customer = getPara("customer");
		String sp = getPara("sp");
		String officeName = getPara("officeName");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sqlTotal = "";
		String sql = "";
		Record rec = null;
		// String strWhere = DataTablesUtils.buildSingleFilter(this);
		if (orderNo == null && status == null && address == null
				&& customer == null && sp == null && beginTime == null
				&& endTime == null) {
			sqlTotal = "select count(1) total from transfer_order t where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='out_source'";
			sql = "select t.*,c1.abbr cname,c2.abbr spname,t.create_stamp,o.office_name oname from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='out_source'  order by create_stamp desc"
					+ sLimit;
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}

			sqlTotal = "select count(1) total from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id"
					+ " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='out_source' and t.order_no like '%"
					+ orderNo + "%' and t.status like '%" + status
					+ "%' and t.address like '%" + address
					+ "%' and c1.abbr like '%" + customer
					+ "%' and ifnull(c2.abbr,'') like '%" + sp
					+ "%' and o.office_name  like '%" + officeName
					+ "%' and create_stamp between '" + beginTime + "' and '"
					+ endTime + "'";

			sql = "select t.*,c1.abbr cname,c2.abbr spname,o.office_name oname from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id"
					+ " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='out_source' and t.order_no like '%"
					+ orderNo
					+ "%' and t.status like '%"
					+ status
					+ "%' and t.address like '%"
					+ address
					+ "%' and c1.abbr like '%"
					+ customer
					+ "%' and ifnull(c2.abbr,'') like '%"
					+ sp
					+ "%' and o.office_name  like '%"
					+ officeName
					+ "%' and create_stamp between '"
					+ beginTime
					+ "' and '"
					+ endTime + "' order by create_stamp desc" + sLimit;
		}
		rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		List<Record> transferOrders = Db.find(sql);

		transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);
		renderJson(transferOrderListMap);
	}

	// 外包运输单里程碑
	public void transferMilestoneList() {
		Map<String, List> map = new HashMap<String, List>();
		List<String> usernames = new ArrayList<String>();
		String order_id = getPara("order_id");
		if (order_id == "" || order_id == null) {
			order_id = "-1";
		}
		String pickupOrderId = getPara("pickupOrderId");
		if (pickupOrderId == "" || pickupOrderId == null) {
			pickupOrderId = "-1";
		}
		if (order_id != "-1" && pickupOrderId == "-1") {
			List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
					.find("select * from transfer_order_milestone where type = '"
							+ TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE
							+ "' and order_id=" + order_id);
			for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
				UserLogin userLogin = UserLogin.dao
						.findById(transferOrderMilestone.get("create_by"));
				String username = userLogin.get("c_name");
				if (username == null || "".equals(username)) {
					username = userLogin.get("user_name");
				}
				usernames.add(username);
			}
			map.put("transferOrderMilestones", transferOrderMilestones);
			map.put("usernames", usernames);
		} else {
			List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
					.find("select * from transfer_order_milestone where type = '"
							+ TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE
							+ "' and pickup_id=" + pickupOrderId);
			for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
				UserLogin userLogin = UserLogin.dao
						.findById(transferOrderMilestone.get("create_by"));
				String username = userLogin.get("c_name");
				if (username == null || "".equals(username)) {
					username = userLogin.get("user_name");
				}
				usernames.add(username);
			}
			map.put("transferOrderMilestones", transferOrderMilestones);
			map.put("usernames", usernames);
		}
		renderJson(map);
	}

	// 保存外包运输单里程碑
	public void saveTransferMilestone() {
		String milestoneDepartId = getPara("milestoneDepartId");
		Map<String, Object> map = new HashMap<String, Object>();
		if (milestoneDepartId != null && !"".equals(milestoneDepartId)) {
			TransferOrder tr = TransferOrder.dao.findById(milestoneDepartId);
			TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
			String status = getPara("status");
			String location = getPara("location");
			transferOrderstatus(milestoneDepartId, status, location);
			if (!status.isEmpty()) {
				transferOrderMilestone.set("status", status);
				tr.set("status", status);
			} else {
				transferOrderMilestone.set("status", "在途");
				tr.set("status", "在途");
			}
			tr.update();
			if (!location.isEmpty()) {
				transferOrderMilestone.set("location", location);
			} else {
				transferOrderMilestone.set("location", "");
			}
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");

			transferOrderMilestone.set("create_by", users.get(0).get("id"));

			java.util.Date utilDate = new java.util.Date();
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(
					utilDate.getTime());
			transferOrderMilestone.set("create_stamp", sqlDate);
			transferOrderMilestone.set("order_id", milestoneDepartId);
			transferOrderMilestone.set("type",
					TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
			transferOrderMilestone.save();

			map.put("transferOrderMilestone", transferOrderMilestone);
			UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone
					.get("create_by"));
			String username = userLogin.get("user_name");
			map.put("username", username);
		}
		renderJson(map);
	}

	

	// 回显司机信息
	public void ginDriver() {
		String id = getPara("depart_id");
		if (id != null && id != "") {
			DepartOrder de = DepartOrder.dao.findById(Integer.parseInt(id));
			Long d_id = de.get("driver_id");
			Carinfo car = Carinfo.dao.findById(d_id);
			renderJson(car);
		}
	}

	// 从发车单货品计算货品数量
	@Before(Tx.class)
	public double productAmount(String depart_id, String item_id) {
		int de_id = Integer.parseInt(depart_id);
		int it_id = Integer.parseInt(item_id);
		double amount = 0;
		String sql = "select * from depart_transfer_itemdetail where depart_id ="
				+ de_id + " and item_id=" + it_id + "";
		List<Record> de_item_amount = Db.find(sql);
		if (de_item_amount.size() == 1) {
			if (de_item_amount.get(0).get("itemdetail_id") == null) {
				TransferOrderItem tr_item = TransferOrderItem.dao
						.findById(it_id);
				amount = Double.parseDouble(tr_item.get("amount").toString());
			} else {
				amount = de_item_amount.size();
			}
		} else if (de_item_amount.size() > 1) {
			amount = de_item_amount.size();
		}
		return amount;

	}

	// 产品入库
	@Before(Tx.class)
	public static void productInWarehouse(String departId) {
		if (!"".equals(departId) && departId != null) {
			String orderIds = "";
			List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao
					.find("select * from depart_transfer where depart_id = ?",
							departId);
			for (DepartTransferOrder departTransferOrder : departTransferOrders) {
				orderIds += departTransferOrder.get("order_id") + ",";
			}
			orderIds = orderIds.substring(0, orderIds.length() - 1);
			List<TransferOrder> transferOrders = TransferOrder.dao
					.find("select * from transfer_order where id in("
							+ orderIds + ")");
			for (TransferOrder transferOrder : transferOrders) {
				if ("gateIn".equals(transferOrder.get("arrival_mode"))
						|| "deliveryToWarehouse".equals(transferOrder
								.get("arrival_mode"))) {
					InventoryItem inventoryItem = null;
					List<TransferOrderItem> transferOrderItems = TransferOrderItem.dao
							.find("select * from transfer_order_item where order_id = ?",
									transferOrder.get("id"));
					for (TransferOrderItem transferOrderItem : transferOrderItems) {
						if (transferOrderItem != null) {
							if (transferOrderItem.get("product_id") != null) {
								// 判断是否有库存
								String inventoryItemSql = "select * from inventory_item where product_id = "
										+ transferOrderItem.get("product_id")
										+ " and warehouse_id = "
										+ transferOrder.get("warehouse_id");
								inventoryItem = InventoryItem.dao
										.findFirst(inventoryItemSql);
								// 判断发车单中的运输单是否有单品,
								String sqlTotal = "select count(1) total from transfer_order_item_detail where depart_id = "
										+ departId
										+ " and order_id = "
										+ transferOrder.get("id")
										+ " and item_id = "
										+ transferOrderItem.get("id");
								Record rec = Db.findFirst(sqlTotal);
								Long amount = rec.getLong("total");
								if (amount == 0) {
									// 当运输单没有单品时取货品信息中的数量
									amount = Math.round(transferOrderItem
											.getDouble("amount"));
								}
								if (inventoryItem == null) {
									inventoryItem = new InventoryItem();
									inventoryItem.set("party_id",
											transferOrder.get("customer_id"));
									inventoryItem.set("warehouse_id",
											transferOrder.get("warehouse_id"));
									inventoryItem
											.set("product_id",
													transferOrderItem
															.get("product_id"));
									inventoryItem.set("total_quantity", amount);
									inventoryItem.set("available_quantity",
											amount);
									inventoryItem.save();
								} else {
									inventoryItem.set(
											"total_quantity",
											Double.parseDouble(inventoryItem
													.get("total_quantity")
													.toString())
													+ amount);
									if (inventoryItem.get("available_quantity") == null
											|| "".equals(inventoryItem
													.get("total_quantity"))) {
										inventoryItem.set("available_quantity",
												amount);
									} else {
										inventoryItem
												.set("available_quantity",
														Double.parseDouble(inventoryItem
																.get("available_quantity")
																.toString())
																+ amount);
									}

									inventoryItem.update();
								}
							}
						}
					}
				}

			}
		}
	}

	/*
	 * public void CreatReturnOrder() { boolean check =
	 * CreatReturnOrder.CreatOrder(ReturnOrder.Depart_Order,
	 * getPara("depart_id").toString()); renderJson(check); }
	 */

	// TODO:应付list,查询条件（一张运输单，三张运输单，多种产品）
	public void accountPayable() {
		String id = getPara();
		if (id == null || id.equals("")) {
			Map orderMap = new HashMap();
			orderMap.put("sEcho", 0);
			orderMap.put("iTotalRecords", 0);
			orderMap.put("iTotalDisplayRecords", 0);
			orderMap.put("aaData", null);
			renderJson(orderMap);
			return;
		}
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from depart_order_fin_item d "
				+ "left join fin_item f on d.fin_item_id = f.id "
				+ "where d.depart_order_id ='" + id + "' and f.type='应付'";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		DepartOrder depart = DepartOrder.dao.findById(id);
		String charge_type = depart.get("charge_type");
		List<Record> record = Db
				.find("select tor.cargo_nature from transfer_order tor left join depart_transfer dt on tor.id = dt.order_id where dt.depart_id =?",
						id);
		String cargo_nature = record.get(0).get("cargo_nature");
		String conditionSql = "";
		if ("ATM".equals(cargo_nature)) {
			if ("perUnit".equals(charge_type)) {
				conditionSql = " select d.*,"
						+ " f.name,"
						+ "  tor.order_no transfer_order_no,"
						+ " (select count(id) as item_amount from transfer_order_item_detail where depart_id =d.depart_order_id and item_id = d.transfer_order_item_id) item_amount,"
						+ " (select count(id) * p.volume as volume from transfer_order_item_detail where depart_id =d.depart_order_id and item_id = d.transfer_order_item_id) volume,"
						+ " (select count(id) * p.weight as weight from transfer_order_item_detail where depart_id =d.depart_order_id and item_id = d.transfer_order_item_id) weight,"
						+ " (select ci.amount from depart_order dor left join contract c on c.party_id = dor.sp_id left join contract_item ci on ci.contract_id = c.id where dor.id = d.depart_order_id and dor.charge_type = ci.pricetype and c.type ='SERVICE_PROVIDER' and dor.route_from = ci.from_id and dor.route_to = ci.to_id) price,"
						+ " (select charge_type from depart_order where id = d.depart_order_id) fin_charge_type,"
						+ " (select l.name from depart_order dor left join location l on dor.route_from = l.code where dor.id = d.depart_order_id) route_from,"
						+ " (select lt.name from depart_order dor left join location lt on dor.route_to = lt.code where dor.id = d.depart_order_id) route_to"
						+ " from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id "
						+ " left join transfer_order tor on tor.id = d.transfer_order_id"
						+ " left join transfer_order_item tori on tori.id = d.transfer_order_item_id"
						+ " left join product p on p.id = tori.product_id"
						+ " where d.depart_order_id =" + id
						+ " and f.type='应付' and cost_source ='合同费用'";
			} else {
				// 整车和零担情况
				conditionSql = " select d.*,"
						+ " f.name,"
						+ " (select group_concat(tor.order_no separator '<br/>') from transfer_order tor left join depart_transfer dt on dt.order_id = tor.id where dt.depart_id =d.depart_order_id ) transfer_order_no,"
						+ " (select count(id) as item_amount from transfer_order_item_detail where depart_id =d.depart_order_id) item_amount,"
						+ " round((select sum(volume) from transfer_order_item_detail toid where toid.depart_id=d.depart_order_id),2)volume,"
						+ " round((select sum(weight) from transfer_order_item_detail toid where toid.depart_id=d.depart_order_id),2) weight,"
						+ " (select ci.amount from depart_order dor left join contract c on c.party_id = dor.sp_id left join contract_item ci on ci.contract_id = c.id where dor.id = d.depart_order_id and dor.charge_type = ci.pricetype and c.type ='SERVICE_PROVIDER' and dor.route_from = ci.from_id and dor.route_to = ci.to_id) price,"
						+ " (select charge_type from depart_order where id = d.depart_order_id) fin_charge_type,"
						+ " (select l.name from depart_order dor left join location l on dor.route_from = l.code where dor.id = d.depart_order_id) route_from,"
						+ " (select lt.name from depart_order dor left join location lt on dor.route_to = lt.code where dor.id = d.depart_order_id) route_to"
						+ " from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id "
						+ " left join transfer_order tor on tor.id = d.transfer_order_id"
						+ " left join transfer_order_item tori on tori.id = d.transfer_order_item_id"
						+ " left join product p on p.id = tori.product_id"
						+ " where d.depart_order_id =" + id
						+ " and f.type='应付' and cost_source ='合同费用'";
			}
		} else {
			conditionSql = " select d.*,"
					+ " f.name,"
					+ " (select group_concat(tor.order_no separator '<br/>') from transfer_order tor left join depart_transfer dt on dt.order_id = tor.id where dt.depart_id =d.depart_order_id ) transfer_order_no,"
					+ " (select sum(toi.amount) from transfer_order_item toi left join depart_transfer dt on dt.order_id = toi.order_id where dt.depart_id = d.depart_order_id) item_amount,"
					+ " ifnull(nullif(tori.volume,0),p.volume) volume,"
					+ " ifnull(nullif(tori.weight,0),p.weight) weight,"
					+ " (select ci.amount from depart_order dor left join contract c on c.party_id = dor.sp_id left join contract_item ci on ci.contract_id = c.id where dor.id = d.depart_order_id and dor.charge_type = ci.pricetype and c.type ='SERVICE_PROVIDER' and dor.route_from = ci.from_id and dor.route_to = ci.to_id) price,"
					+ " (select charge_type from depart_order where id = d.depart_order_id) fin_charge_type,"
					+ " (select l.name from depart_order dor left join location l on dor.route_from = l.code where dor.id = d.depart_order_id) route_from,"
					+ " (select lt.name from depart_order dor left join location lt on dor.route_to = lt.code where dor.id = d.depart_order_id) route_to"
					+ " from depart_order_fin_item d "
					+ " left join fin_item f on d.fin_item_id = f.id "
					+ " left join transfer_order tor on tor.id = d.transfer_order_id"
					+ " left join transfer_order_item tori on tori.id = d.transfer_order_item_id"
					+ " left join product p on p.id = tori.product_id"
					+ " where d.depart_order_id =" + id
					+ " and f.type='应付' and cost_source ='合同费用'";
		}

		String querySql = "select d.*,"
				+ " f.name,"
				+ " tor.order_no transfer_order_no,"
				+ " null item_amount,"
				+ " null volume,"
				+ " null weight,"
				+ " null price,"
				+ " null fin_charge_type,"
				+ " null route_from,"
				+ " null route_to"
				+ " from depart_order_fin_item d "
				+ " left join fin_item f on d.fin_item_id = f.id "
				+ " left join transfer_order tor on tor.id = d.transfer_order_id"
				+ " where d.depart_order_id =" + id
				+ " and f.type='应付' and cost_source ='运输单应付费用'" + " union"
				+ conditionSql + " union" + " select d.*," + " f.name,"
				+ " null transfer_order_no," + " null item_amount,"
				+ " null volume," + " null weight," + " null price,"
				+ " null fin_charge_type," + " null route_from,"
				+ " null route_to" + " from depart_order_fin_item d "
				+ " left join fin_item f on d.fin_item_id = f.id "
				+ " where d.depart_order_id =" + id
				+ " and f.type='应付'";

		/*
		 * String querySql = "select d.*," + " f.name," +
		 * " tor.order_no transfer_order_no," +
		 * " ifnull(tori.item_name, p.item_name) item_name," +
		 * " null item_amount," + " null volume," + " null weight" +
		 * " from depart_order_fin_item d " +
		 * " left join fin_item f on d.fin_item_id = f.id " +
		 * " left join transfer_order tor on tor.id = d.transfer_order_id" +
		 * " left join transfer_order_item tori on tori.id = d.transfer_order_item_id"
		 * + " left join product p on p.id = tori.product_id" +
		 * " where d.depart_order_id =" + id + " and f.type='应付'";
		 */
		// 获取当前页的数据
		List<Record> orders = Db.find(querySql);

		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		/*
		 * List<Record> list = Db.find("select * from fin_item"); for (int i =
		 * 0; i < list.size(); i++) { if (list.get(i).get("name") == null) {
		 * Fin_item.dao.deleteById(list.get(i).get("id")); } }
		 */
		renderJson(orderMap);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_ADD_COST})
	public void addNewRow() {

		List<FinItem> items = new ArrayList<FinItem>();
		String orderId = getPara();
		FinItem item = FinItem.dao
				.findFirst("select * from fin_item where type = '应付' order by id asc");
		if (item != null) {
			DepartOrderFinItem dFinItem = new DepartOrderFinItem();
			dFinItem.set("status", "新建").set("fin_item_id", item.get("id"))
					.set("depart_order_id", orderId)
					.set("create_name", dFinItem.CREATE_NAME_USER).save();
		}
		items.add(item);
		renderJson(items);
	}

	// 添加应付
	public void paymentSave() {
		String returnValue = "";
		String id = getPara("id");
		String finItemId = getPara("finItemId");
		DepartOrderFinItem dFinItem = DepartOrderFinItem.dao.findById(id);

		FinItem fItem = FinItem.dao.findById(dFinItem.get("fin_item_id"));

		String amount = getPara("amount");

		String username = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + username
						+ "'");
		Date createDate = Calendar.getInstance().getTime();

		if (!"".equals(finItemId) && finItemId != null) {
			dFinItem.set("fin_item_id", finItemId).update();
			returnValue = finItemId;
		} else if (!"".equals(amount) && amount != null) {
			dFinItem.set("amount", amount).update();
			returnValue = amount;
		}
		List<Record> list = Db.find("select * from fin_item");
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).get("name") == null) {
				FinItem.dao.deleteById(list.get(i).get("id"));
				List<Record> list2 = Db
						.find("select * from depart_order_fin_item where fin_item_id ='"
								+ list.get(i).get("id") + "'");
				List<Record> list3 = Db
						.find("select * from fin_item where id ='"
								+ list2.get(0).get("fin_item_id") + "'");
				if (list3.size() == 0) {
					TransferOrderFinItem.dao.deleteById(list2.get(0).get("id"));
				}
			}
		}
		renderText(returnValue);
	}

	public void getPaymentList() {
		// String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		locationList = Db.find("select * from fin_item where type='应付'");
		renderJson(locationList);
	}

	// 点击货品table的查看 ，显示货品对应的单品
	public void findAllItemDetail() {
		String itemId = getPara("item_id");
		String departId = getPara("departId");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String category = getPara("category");
		String sqlTotal = "select count(1) total from transfer_order_item_detail tod where tod.item_id = "
				+ itemId + " and tod.depart_id = " + departId;
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select * from transfer_order_item_detail tod where tod.item_id = "
				+ itemId + " and tod.depart_id = " + departId;

		List<Record> details = Db.find(sql);

		Map productListMap = new HashMap();
		productListMap.put("sEcho", pageIndex);
		productListMap.put("iTotalRecords", rec.getLong("total"));
		productListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		productListMap.put("aaData", details);
		renderJson(productListMap);
	}
	// 修改应付
	public void updateDepartOrderFinItem() {
		String paymentId = getPara("paymentId");
		String name = getPara("name");
		String value = getPara("value");
		if ("amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		if (paymentId != null && !"".equals(paymentId)) {
			DepartOrderFinItem departOrderFinItem = DepartOrderFinItem.dao
					.findById(paymentId);
			departOrderFinItem.set(name, value);

			departOrderFinItem.update();
		}
		renderJson("{\"success\":true}");
	}
	// 删除应付
	@Before(Tx.class)
	public void finItemdel() {
		String id = getPara();
		DepartOrderFinItem.dao.deleteById(id);
		renderJson("{\"success\":true}");
	}
	// TODO：运输单带过来的费用
	public void getFinNoContractCost(DepartOrder departOrder) {
		List<TransferOrder> tran = TransferOrder.dao
				.find("select tor.* from transfer_order tor left join depart_transfer dt on dt.order_id = tor.id  where dt.depart_id = ?",
						departOrder.get("id"));

		List<TransferOrderFinItem> tofiList;
		if (tran.size() > 0) {
			for (TransferOrder transferOrder : tran) {
				tofiList = TransferOrderFinItem.dao
						.find("select sum(amount) as total_cost,fin_item_id from transfer_order_fin_item tofi left join fin_item fi on fi.id = tofi.fin_item_id where order_id = ? and fi.type='应付' group by tofi.fin_item_id",
								transferOrder.get("id"));
				String name = (String) currentUser.getPrincipal();
				UserLogin users = UserLogin.dao
						.findFirst("select * from user_login where user_name='"
								+ name + "'");
				java.util.Date utilDate = new java.util.Date();
				java.sql.Timestamp now = new java.sql.Timestamp(
						utilDate.getTime());
				if (tofiList.size() > 0) {
					for (TransferOrderFinItem transferOrderFinItem : tofiList) {
						DepartOrderFinItem departOrderFinItem = new DepartOrderFinItem();
						departOrderFinItem.set("fin_item_id",
								transferOrderFinItem.get("fin_item_id"));
						departOrderFinItem.set("amount",
								transferOrderFinItem.get("total_cost"));
						departOrderFinItem.set("depart_order_id",
								departOrder.getLong("id"));
						departOrderFinItem.set("status", "未完成");
						departOrderFinItem.set("creator", users.get("id"));
						departOrderFinItem.set("create_date", now);
						departOrderFinItem.set("create_name",
								departOrderFinItem.CREATE_NAME_SYSTEM);
						departOrderFinItem.set("transfer_order_id",
								transferOrder.get("id"));
						// departOrderFinItem.set("transfer_order_item_id",
						// toi.get("id"));
						departOrderFinItem.set("cost_source", "运输单应付费用");
						departOrderFinItem.save();
					}
				}

			}
		}

	}

	/**
	 * 整车发车时减少库存
	 * 
	 * @param departOrderId
	 */
	public void SubtractInventory(DepartTransferOrder departTransferOrder,
			String departOrderId) {
		TransferOrder transferOrder = TransferOrder.dao
				.findById(departTransferOrder.get("order_id"));

		if ("arrangementOrder".equals(transferOrder.get("order_type"))
				|| ("cargoReturnOrder".equals(transferOrder.get("order_type")) && "deliveryToFachtoryFromWarehouse"
						.equals(transferOrder.get("arrival_mode")))) {
			List<TransferOrderItem> list = TransferOrderItem.dao.find(
					"select * from transfer_order_item where order_id = ? ",
					transferOrder.get("id"));
			for (TransferOrderItem transferOrderItem : list) {
				if (transferOrderItem.getLong("product_id") != null
						&& transferOrderItem.getLong("product_id") != 0) {
					InventoryItem ii = InventoryItem.dao
							.findFirst(
									"select * from inventory_item where party_id =? and warehouse_id = ? and product_id = ?",
									transferOrder.get("customer_id"),
									transferOrder.get("from_warehouse_id"),
									transferOrderItem.get("product_id"));
					TransferOrderItemDetail toid = TransferOrderItemDetail.dao
							.findFirst(
									"select count(*) as amount from transfer_order_item_detail where item_id = ? and depart_id = ? and pickup_id is null ",
									transferOrderItem.get("id"), departOrderId);
					Double total_quantity = ii.getDouble("total_quantity");
					if (total_quantity - toid.getLong("amount") >= 0) {
						ii.set("total_quantity",
								total_quantity - toid.getLong("amount"));
						ii.update();
					}
				}

			}
		}
	}
	
	
	 // 发车确认
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_COMPLETED})
    @Before(Tx.class)
    public void departureConfirmation() {
        String departOrderId = getPara("departOrderId"); 
        int num = 1;
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find("select * from depart_transfer where depart_id = ?", departOrderId);
        //更新发车单状态
        DepartOrder departOrder = DepartOrder.dao.findById(departOrderId);
        departOrder.set("status", "运输在途");
        departOrder.update();
        
        TransferOrderMilestone departOrderMilestone = new TransferOrderMilestone();
    	departOrderMilestone.set("create_by", LoginUserController.getLoginUserId(this));
    	departOrderMilestone.set("create_stamp", new Date());
        departOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
        departOrderMilestone.set("status", "已发车");
        departOrderMilestone.set("location", "");
        departOrderMilestone.set("depart_id", departOrderId);
        departOrderMilestone.save();
        
        for(DepartTransferOrder departTransferOrder : departTransferOrders){
        	Long transferId = departTransferOrder.get("order_id");
        	TransferOrder transfer = TransferOrder.dao.findById(transferId);
        	String cargo_nature = transfer.getStr("cargo_nature");

        	TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        	if(cargo_nature.equals("ATM")){
        		String sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + transferId;
            	Record rec = Db.findFirst(sqlTotal);
            	Long total = rec.getLong("total");  //总数量

            	sqlTotal = "select count(1) total from transfer_order_item_detail toid"
            			+ " left join depart_order dor on dor.id = toid.depart_id "
            			+ " where dor.status is not null and dor.status !='新建'"
            			+ " and toid.order_id = " + transferId ;
            	rec = Db.findFirst(sqlTotal);
            	Long departTotal = rec.getLong("total");   //已发车数量（包括这次）
            	if(total==departTotal){
            		transferOrderMilestone.set("status", "已发车");
            	}else{
            		transferOrderMilestone.set("status", "部分已发车");
            	}
        	}else{
        		//普货货品总数量
    			Record re = Db.findFirst("select sum(toi.amount) total from transfer_order_item toi where toi.order_id = ?",transferId);
    			double totalAmount = re.getDouble("total") ;    //总货品数量
    			double departAmount = 0.0;  //已入库的数量（包括这次）
    			if(!transfer.getStr("operation_type").equals("out_source")){
    				re = Db.findFirst("select sum(ifnull(dt.amount,0)) yishou from depart_pickup dp"
    						+ " LEFT JOIN depart_order dor on dor.id = dp.depart_id"
    						+ " LEFT JOIN depart_transfer dt on dt.pickup_id = dp.pickup_id"
    						+ " where dor.status is not null and dor.status !='新建' and dt.order_id = ?",transferId);
    				departAmount = re.getDouble("yishou");
    			}else{
    				departAmount = totalAmount; //外包（因无法多次发车，所以数量和总数量相同）
    			}
    			if(totalAmount == departAmount){
            		transferOrderMilestone.set("status", "已发车");
            	}else{
            		transferOrderMilestone.set("status", "部分已发车");
            	}
        	}
	        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
	        transferOrderMilestone.set("order_id", transferId);
	        transferOrderMilestone.set("create_by", LoginUserController.getLoginUserId(this));
	        transferOrderMilestone.set("create_stamp", new Date());
	        transferOrderMilestone.set("location", "");
	        transferOrderMilestone.save();
	        

	        List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where order_id = " + departTransferOrder.get("order_id") + " and depart_id = " + departOrderId);
	        for(TransferOrderItemDetail detail : transferOrderItemDetails){
	        	detail.set("status", "已发车");
	        	detail.update();
	        }	
        }
        
        renderJson("{\"success\":true}");        
    }
    
    
    // 初始化货品数据
    public void getDepartOrderItem() {
        String orderId = getPara("localArr");// 运输单id
        String departOrderId = getPara("departOrderId");
        String[] list = orderId.split(",");
        
        String id = "";
        for (int i = 0; i < list.length; i++) {
			String[] array = list[i].split(":");
			if(i == list.length-1)
				id += array[0];
			else{
				id += array[0]+",";
			}
		}
        
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "SELECT COUNT(0) total FROM(SELECT(SELECT count(0) total FROM	transfer_order_item_detail WHERE order_id = tor.id	AND item_id = toi.id AND depart_id = 1632) atmamount, ifnull(toi.amount, 0) cargoamount FROM transfer_order_item toi LEFT JOIN transfer_order tor ON tor.id = toi.order_id WHERE toi.order_id IN (" + id + ")) a where (atmamount > 0 or cargoamount>0)";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        String sql = "SELECT * FROM(select toi.id,ifnull(toi.item_name, pd.item_name) item_name,tor.planning_time,ifnull(toi.item_no, pd.item_no) item_no,"
             		+ " round(ifnull(pd.volume, 0),2) volume,round(ifnull(pd.weight, 0),2) weight,tor.cargo_nature,"
                    + " (select count(0) total from transfer_order_item_detail where order_id = tor.id and item_id = toi.id and depart_id = "+departOrderId+") atmamount,"
                     + " (case "
                     + " when tor.operation_type != 'out_source'"
                     + " then ( "
                     + " select dt.amount from depart_order deo "
                     + " LEFT JOIN depart_pickup dpi on dpi.depart_id = deo.id"
                     + " LEFT JOIN depart_transfer dt on dt.pickup_id = dpi.pickup_id"
                     + " where deo.id = "+departOrderId
                     + " and tor.id = dt.order_id"
                     + "  GROUP BY toi.id "
                     //+ " SELECT ifnull(sum(dt.amount), 0) FROM depart_transfer dt LEFT JOIN transfer_order tor1 ON tor1.id = dt.order_id"
                     //+ " LEFT JOIN depart_pickup dp ON dp.pickup_id = dt.pickup_id WHERE dp.depart_id =  "+departOrderId
                     //+ " AND  tor1.cargo_nature = 'cargo' and dt.order_item_id = toi.id AND tor1.id = tor.id"
                     + " )"
                     + " else"
                     + " toi.amount"
                     + " end"
                     + " ) cargoamount,ifnull(toi.volume, 0) cargovolume,ifnull(toi.sum_weight, 0) cargoweight,c.abbr customer,tor.order_no,toi.remark  from transfer_order_item toi "
                     + " left join transfer_order tor on tor.id = toi.order_id"
                     + " left join party p on p.id = tor.customer_id"
                     + " left join contact c on c.id = p.contact_id"
                     + " left join product pd on pd.id = toi.product_id"
                     + " where toi.order_id in(" + id + ")  order by c.id ) a where if(a.cargo_nature='ATM',a.atmamount,a.cargoamount) > 0 " + sLimit;

        List<Record> departOrderitem = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", departOrderitem);
        renderJson(Map);
    }
    
    @Before(Tx.class)
    public void cancelOrder(){
    	String id = getPara("orderId");
    	List transferOrderIds = new ArrayList();
    	
    	if(id==null || "".equals(id))
    		return;
    	//再次校验是否存在下级单据
    	Record re = Db.findFirst("select * from transfer_order_item_detail where depart_id = ? and delivery_id is not null",id);
    	if(re!=null){
    		renderJson("{\"success\":false}");
    	}else{
    		//1.清除单品明细表发车单数据
    		List<TransferOrderItemDetail> toids = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where depart_id = ?",id);
    		for(TransferOrderItemDetail toid :toids){
				toid.set("depart_id",null).update();
			}
    		
    		//2.清除中间关联表数据
    		List<DepartTransferOrder> dtos = DepartTransferOrder.dao.find("select * from depart_transfer where depart_id = ?",id);
    		for(DepartTransferOrder dto :dtos){
    			Long tranId = dto.getLong("order_id");
    			transferOrderIds.add(tranId);
    			//保存运输单id
    			dto.delete();
			}
    		
    		//3.清除depart_pickup表数据
    		List<DepartPickupOrder> dpos = DepartPickupOrder.dao.find("select * from depart_pickup where depart_id = ?",id);
    		for(DepartPickupOrder dpo : dpos){
    			//保存运输单id
    			dpo.delete();
			}
    		
    		//4.删除主单据
    		DepartOrder dep  = DepartOrder.dao.findById(id);
    		dep.delete();
    		
    		//5.删除相对于的里程碑数据
    		List<TransferOrderMilestone> toms = TransferOrderMilestone.dao.find("select * from transfer_order_milestone where depart_id = ?",id);
    		for(TransferOrderMilestone tom :toms){
    			tom.delete();
			}
    		
    		//6.更新相对的运输单
    		for(int i=0;i<transferOrderIds.size();i++){
    			//查看此单据为多次发车还是一次发车
    			Record dt = Db.findFirst("select * from depart_transfer where order_id =? and depart_id is not null",transferOrderIds.get(i));
    			TransferOrder to = TransferOrder.dao.findById(transferOrderIds.get(i));
    			if(dt!=null){//多次发车
        			to.set("depart_assign_status", "PARTIAL");
    			}else{
        			to.set("depart_assign_status", "NEW");
        			//判断是否外包
        			String operation_type = to.getStr("operation_type");
        			if("out_source".equals(operation_type)){
        				to.set("status", "新建");
        			}
    			}
    			
    			to.update();
			}
    		
    		renderJson("{\"success\":true}");
    	}
    }

    
  //撤销入库
  	@Before(Tx.class)
  	public void deleteInWarehouse() {
  		String depart_id = getPara("order_id");
  		
  		if("".equals(depart_id)||depart_id==null)
  			return;
  		
  		DepartOrder departOrder = DepartOrder.dao.findById(depart_id);
        //判断是否存在下级财务单据（应付未对账）
        String audit_status = departOrder.getStr("audit_status");
        if(!"新建".equals(audit_status) && !"已确认".equals(audit_status)){
        	renderJson("{\"success\":false}");
			return;
        }
        
        List<DepartTransferOrder> dts = DepartTransferOrder.dao.find("select order_id from depart_transfer where depart_id = ? ",depart_id);
        //先通过状态判断是否存在下级单据（ps:不可在校验状态的同时更新单据信息（这样可以避免多张运输单同时入库的情况））
        for (DepartTransferOrder dt : dts) {
        	long transfer_id = dt.getLong("order_id");
        	TransferOrder tor = TransferOrder.dao.findById(transfer_id);
        	
        	List<TransferOrderItemDetail> toids = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail"
        			+ " where depart_id = ? and order_id = ?",depart_id,transfer_id);
        	for(TransferOrderItemDetail toid : toids){
        		long delivery_id = 0;
        		if(toid.getLong("delivery_id") != null){
        			delivery_id = toid.getLong("delivery_id");
        			
        			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(delivery_id);
        			String delievry_status = deliveryOrder.getStr("status");
      				if(!"新建".equals(delievry_status)){
      					//有下级单据，不可撤销
	  					renderJson("{\"success\":false}");
	  					return;
      				}
        		}	
        	}
        	
        	String cargo_nature = tor.getStr("cargo_nature");
        	if("cargo".equals(cargo_nature)){
        		DeliveryOrderItem doi = DeliveryOrderItem.dao.findFirst("select * from delivery_order_item where transfer_order_id = ?",transfer_id);
        		if(doi != null){
        			//有下级单据，不可撤销
  					renderJson("{\"success\":false}");
  					return;
        		}
        	}
        }
        
        //校验完没有下级单据后，开始撤销相对应的单据信息
        for (DepartTransferOrder dt : dts) {
        	long transfer_id = dt.getLong("order_id");
        	TransferOrder tor = TransferOrder.dao.findById(transfer_id);
        	
        	List<TransferOrderItemDetail> toids = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail"
        			+ " where depart_id = ? and order_id = ?",depart_id,transfer_id);
        	for(TransferOrderItemDetail toid : toids){
        		long delivery_id = 0;
        		if(toid.getLong("delivery_id") != null){
        			delivery_id = toid.getLong("delivery_id");
        			
        			//更新单品明细表
    	  			toid.set("delivery_id", null);
    	  			toid.set("status", "已发车");
    	  			toid.set("is_delivered", 0);
    	  			toid.update();
        			
        			//删除对应的配送单
  	  			    deleteATM(delivery_id);
        		}	
        	}
        	
        	//更新运输单状态
  			tor.set("status", "处理中").update();
        }
  		
  		//更新发车单状态
        departOrder.set("status", "运输在途");
        departOrder.set("audit_status", "新建").update();	
		
  		renderJson("{\"success\":true}");	
  	}
  	
  	
  	
  	//ATM撤销
  	@Before(Tx.class)
  	public void deleteATM(long delivery_id){

  		//删除里程碑相关数据
  		List<DeliveryOrderMilestone> doms = DeliveryOrderMilestone.dao.find("select * from delivery_order_milestone where delivery_id = ?",delivery_id);
  		for (DeliveryOrderMilestone dom:doms) {
  			dom.delete();
  		}
  		
  		//删除配送子表(费用明细)
  		List<DeliveryOrderFinItem> dofis = DeliveryOrderFinItem.dao.find("select * from delivery_order_fin_item where order_id = ?",delivery_id);
  		for (DeliveryOrderFinItem dofi:dofis) {
  			dofi.delete();
  		}	
  		
  		//删除配送字表
  		List<DeliveryOrderItem> dois = DeliveryOrderItem.dao.find("select * from delivery_order_item where delivery_id = ?",delivery_id);
  		for (DeliveryOrderItem doi:dois) {
  			doi.delete();
  		}
  		
  	    //2.删除主表
  		DeliveryOrder dor = DeliveryOrder.dao.findById(delivery_id);
		dor.delete();	
  	}
  	
  	
  	
  	
    //撤销收货
    @Before(Tx.class)
    public void deleteReceipt(){
    	String order_id = getPara("order_id");

        DepartOrder departOrder = DepartOrder.dao.findById(order_id);
        //判断是否存在下级财务单据（应付未对账）
        String audit_status = departOrder.getStr("audit_status");
        if(!"新建".equals(audit_status) && !"已确认".equals(audit_status)){
        	renderJson("{\"success\":false}");
			return;
        }

        List<DepartTransferOrder> dts = DepartTransferOrder.dao.find("select order_id from depart_transfer where depart_id = ? ",order_id);
        //先通过状态校验是否存在下级单据
        for (DepartTransferOrder dt : dts) {
        	long transfer_id = dt.getLong("order_id");
        	TransferOrder tor = TransferOrder.dao.findById(transfer_id);
        	
        	//判断此单据是否为退货单
        	//是？（撤销对应的配送单）：（撤销对应的回单）
        	String order_type = tor.getStr("order_type");
        	if("cargoReturnOrder".equals(order_type)){
        		List<TransferOrderItemDetail> toids = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail "
            			+ "where order_id = ? and depart_id = ?",transfer_id,order_id);
            	long delivery_id = 0;
            	//更新单品明细表
          		for (TransferOrderItemDetail toid:toids) {
          			//配送单ID
          			if(toid.getLong("delivery_id") != null){
          				delivery_id = toid.getLong("delivery_id");
          				
          				//校验配送单是否存在下级单据
          	  			DeliveryOrder dor = DeliveryOrder.dao.findById(delivery_id);
          				String status = dor.getStr("status");
          				if(!"新建".equals(status)){
          					renderJson("{\"success\":false}");
             	 			return ;
          				}
          			}
          		}
        	} else {
        		ReturnOrder returnOrder = ReturnOrder.dao.findFirst("select * from return_order where transfer_order_id =?",transfer_id);
        		//校验是否有下级财务单据（应收）
        		String transaction_status = returnOrder.getStr("transaction_status");
        		if(!"新建".equals(transaction_status) && !"已签收".equals(transaction_status) && !"已确认".equals(transaction_status)){
        			renderJson("{\"success\":false}");
     	 			return ;
        		}
        	}
        }
        
        
        for (DepartTransferOrder dt : dts) {
        	long transfer_id = dt.getLong("order_id");
        	TransferOrder tor = TransferOrder.dao.findById(transfer_id);
        	
        	//判断此单据是否为退货单
        	//是？（撤销对应的配送单）：（撤销对应的回单）
        	String order_type = tor.getStr("order_type");
        	if("cargoReturnOrder".equals(order_type)){
        		if(!deleteDeliveryOrder(transfer_id,order_id)){
        			renderJson("{\"success\":false}");
     	 			return ;
        		}
        	}else{
        		if(!deleteReturnOrder(transfer_id)){
        			renderJson("{\"success\":false}");
     	 			return ;
        		}
        	}
        	
        	//更新运输单状态信息
        	tor.set("status", "处理中").update(); 	
        }
        
        //更新发车单信息
        departOrder.set("status", "运输在途");
        departOrder.set("sign_status", "未回单");
        departOrder.set("audit_status", "新建");
        departOrder.update();
        
        //更新单品详细表
        List<TransferOrderItemDetail> toids = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where depart_id = ?",order_id);
        for(TransferOrderItemDetail toid:toids){
        	toid.set("status", "已发车").update();
        }
        
        renderJson("{\"success\":true}");
    }
    
    
    //撤销回单
    @Before(Tx.class)
    public boolean deleteReturnOrder(long transfer_id){
		ReturnOrder returnOrder = ReturnOrder.dao.findFirst("select * from return_order where transfer_order_id =?",transfer_id);
		if(returnOrder == null)
			return true;
		
		long return_id = returnOrder.getLong("id");

		//删除相关回单应收应付数据
		List<ReturnOrderFinItem> rofis = ReturnOrderFinItem.dao.find("select * from return_order_fin_item where return_order_id = ?",return_id);
		for(ReturnOrderFinItem rofi:rofis){
			rofi.delete();
		}
		
		//删除主单据（回单）
		returnOrder.delete();
    	return true;
    }
    
    //撤销配送单(退货单)一张配送单
    @Before(Tx.class)
    public boolean deleteDeliveryOrder(long transfer_id,String depart_id){
    	List<TransferOrderItemDetail> toids = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail "
    			+ "where order_id = ? and depart_id = ?",transfer_id,depart_id);
    	long delivery_id = 0;
    	//更新单品明细表
  		for (TransferOrderItemDetail toid:toids) {
  			//配送单ID
  			if(toid.getLong("delivery_id") != null){
  				delivery_id = toid.getLong("delivery_id");
  			}
  			
  			toid.set("delivery_id", null);
  			toid.set("status", "已发车");
  			toid.set("is_delivered", 0);
  			toid.update();
  		}
  		
  		if(delivery_id>0){
			DeliveryOrder dor = DeliveryOrder.dao.findById(delivery_id);
			
		    //删除里程碑相关数据
	  		List<DeliveryOrderMilestone> doms = DeliveryOrderMilestone.dao.find("select * from delivery_order_milestone where delivery_id = ?",delivery_id);
	  		for (DeliveryOrderMilestone dom:doms) {
	  			dom.delete();
	  		}
	  		
	  		//删除配送子表(费用明细)
	  		List<DeliveryOrderFinItem> dofis = DeliveryOrderFinItem.dao.find("select * from delivery_order_fin_item where order_id = ?",delivery_id);
	  		for (DeliveryOrderFinItem dofi:dofis) {
	  			dofi.delete();
	  		}	
	  		
	  		//删除配送子表
	  		List<DeliveryOrderItem> dois = DeliveryOrderItem.dao.find("select * from delivery_order_item where delivery_id = ?",delivery_id);
	  		for (DeliveryOrderItem doi:dois) {
	  			doi.delete();
	  		}
	  		
	  		//删除主单据（配送单）
	  		dor.delete();
		}
  		return true;
    }

    
  //普货撤销
  	@Before(Tx.class)
  	public void deleteCargo(String id){
  		List<TransferOrderItemDetail> toids = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where delivery_id = ?",id);
  		for (TransferOrderItemDetail toid:toids) {
  			toid.set("delivery_id", null);
  			toid.set("status", "已发车");
  			toid.update();
  		}
  		
  		//删除配送子表
  		List<DeliveryOrderItem> dois = DeliveryOrderItem.dao.find("select * from delivery_order_item where order_id = ?",id);
  		for (DeliveryOrderItem doi:dois) {
  			//清除单品表信息
  			long transfer_item_id = doi.getLong("transfer_item_id");
  			int amount = Integer.parseInt(doi.getStr("amount"));
  			TransferOrderItem toi =TransferOrderItem.dao.findById(transfer_item_id);
  			int complete_amount = Integer.parseInt(toi.getStr("complete_amount"));
  			int totalAmount = Integer.parseInt(toi.getStr("amount"));
  			
  			if(amount==totalAmount){
  				toi.set("complete_amount",null);
  			}else{
  				toi.set("complete_amount",complete_amount-amount);
  			}
  			toi.update();
  			
  			doi.delete();
  		}
  		
  		List<TransferOrderItem> tois = TransferOrderItem.dao.find("select * from transfer_order_item where delivery_id = ?",id);
  		for (TransferOrderItem toi:tois) {
  			toi.set("delivery_id", null);
  			toi.set("complete_amount", "已入库");
  			toi.update();
  		}
  		
  		//删除里程碑相关数据
  		List<DeliveryOrderMilestone> doms = DeliveryOrderMilestone.dao.find("select * from delivery_order_milestone where delivery_id = ?",id);
  		for (DeliveryOrderMilestone dom:doms) {
  			dom.delete();
  		}
  		
  		//删除配送字表(费用明细)
  		List<DeliveryOrderFinItem> dofis = DeliveryOrderFinItem.dao.find("select * from delivery_order_fin_item where order_id = ?",id);
  		for (DeliveryOrderFinItem dofi:dofis) {
  			dofi.delete();
  		}	
  	}
    
  	
  	public void getItemDetail(){
  		String depart_id = getPara("depart_id");
  		String sLimit = "";
  		String pageIndex = getPara("sEcho");
  		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
  		
  		String sql = " "//有单品
  				+ " select tor.order_no,toid.item_no,toid.serial_no, 1 amount from transfer_order_item_detail toid "
  				+ " LEFT JOIN transfer_order tor on tor.id = toid.order_id"
  				+ " LEFT JOIN depart_order deo on deo.id = toid.depart_id where deo.id = "+depart_id
  				+ " and (tor.cargo_nature = 'ATM' "
  				+ " or (tor.cargo_nature = 'cargo' and tor.cargo_nature_detail='cargoNatureDetailYes'))"
  				+ " union all" //普货(无单品)
  				+ " select tor.order_no,toi.item_no,null serial_no,dt.amount from transfer_order_item toi"
  				+ " LEFT JOIN transfer_order tor on tor.id = toi.order_id"
  				+ " LEFT JOIN depart_transfer dt on dt.order_item_id = toi.id"
  				+ " LEFT JOIN depart_pickup dp on dp.pickup_id = dt.pickup_id and dp.order_id = toi.order_id "
  				+ " WHERE"
  				+ " dp.depart_id = "+depart_id
  				+ " and (tor.cargo_nature = 'cargo' and tor.cargo_nature_detail='cargoNatureDetailNo')"
  				+ " GROUP BY toi.id "
  				+ " union all "   //外包
  				+ " select tor.order_no,toi.item_no,null serial_no,toi.amount from transfer_order_item toi"
  				+ " LEFT JOIN depart_transfer dt on dt.order_id = toi.order_id"
  				+ " LEFT JOIN transfer_order tor on tor.id = dt.order_id"
  				+ " WHERE"
  				+ " dt.depart_id= "+depart_id
  				+ " and tor.cargo_nature = 'cargo' and tor.operation_type = 'out_source' "
  				+ " GROUP BY toi.id" ;
  		Record total = Db.findFirst("select count(1) total from ("+sql+") A");
  		List<Record> re = Db.find(sql+sLimit);
  		Map Map = new HashMap();
  		Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", total.get("total"));
        Map.put("iTotalDisplayRecords", total.get("total"));
        Map.put("aaData", re);
        renderJson(Map);
  	}
}
