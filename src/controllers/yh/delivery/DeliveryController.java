package controllers.yh.delivery;

import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.DepartTransferOrder;
import models.Location;
import models.Office;
import models.ParentOfficeModel;
import models.Party;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.Warehouse;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;
import controllers.yh.util.ReaderXLS;
import controllers.yh.util.ReaderXlSX;
import controllers.yh.util.getCustomFile;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class DeliveryController extends Controller {
	private Logger logger = Logger.getLogger(DeliveryController.class);
	// in config route已经将路径默认设置为/yh
	// me.add("/yh", controllers.yh.AppController.class, "/yh");
	Subject currentUser = SecurityUtils.getSubject();
	
	//@RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_LIST})
	public void index() {
		render("/yh/delivery/deliveryOrderList.html");
	}
	
	//TODO: 这个有问题，暂时屏蔽
	//@RequiresPermissions(value = {PermissionConstant.PERMSSION_DOM_LIST})
	public void deliverOnTrip(){
		render("/yh/delivery/deliveryOrderStatus.html");
	}

	// 配送单list
	//@RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_LIST})
	public void deliveryList() {
		String orderNo_filter = getPara("orderNo_filter");
		String transfer_filter = getPara("transfer_filter");
		String status_filter = getPara("status_filter");
		String customer_filter = getPara("customer_filter");
		String sp_filter = getPara("sp_filter");
		String beginTime_filter = getPara("beginTime_filter");
		String endTime_filter = getPara("endTime_filter");
		String plan_beginTime_filter = getPara("plan_beginTime_filter");
		String plan_endTime_filter = getPara("plan_endTime_filter");
		String office_filter = getPara("office_filter");
		String serial_no = getPara("serial_no");
		String delivery_no = getPara("delivery_no");
		String address_filter = getPara("address_filter");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		
		String sortColIndex = getPara("iSortCol_0");
	    String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		
		Map transferOrderListMap = new HashMap();
			String condition = " where 1=1 ";
			if(!"".equals(orderNo_filter)&&orderNo_filter != null){
				condition += " and ifnull(tor.customer_order_no,'') like '" + orderNo_filter + "' ";
			}
			if(status_filter!=null&&!"".equals(status_filter)){
				condition += " and ifnull(d.status,'') like '%"+ status_filter+ "%' ";
			}
			if(customer_filter!=null&&!"".equals(customer_filter)){
				condition += " and ifnull(c.abbr,'') like '%"+ customer_filter+ "%'";
			}
			if(delivery_no!=null&&!"".equals(delivery_no)){
				condition += " and ifnull(d.order_no,'') like '%"+ delivery_no+ "%' ";
			}
			if(transfer_filter!=null&&!"".equals(transfer_filter)){
				condition +=" and ifnull(dt2.transfer_no,'') like '%"+ transfer_filter+ "%' ";
			}
			if(sp_filter!=null&&!"".equals(sp_filter)){
				condition +=" and ifnull(c3.abbr,'') like'%"+ sp_filter+ "%' ";
			}
			if(serial_no!=null&&!"".equals(serial_no)){
				condition +=" and ifnull(trid.serial_no,'') like'%"+ serial_no+ "%'";
			}
			if(address_filter!=null&&!"".equals(address_filter)){
				condition +=" and IFNULL(trid.notify_party_company,IFNULL(d.receivingunit,'')) like '%"+ address_filter+ "%'";
			}
			if(office_filter!=null&&!"".equals(office_filter)){
				condition +=" AND ifnull(w.warehouse_name,'') like'%"+ office_filter+ "%'";;
			}
			if ((beginTime_filter != null && !"".equals(beginTime_filter))||(endTime_filter != null && !"".equals(endTime_filter))) {
        		if (beginTime_filter == null || "".equals(beginTime_filter)) {
        			beginTime_filter = "1970-01-01";
    			}
        		
    			if (endTime_filter == null || "".equals(endTime_filter)) {
    				endTime_filter = "2037-12-31";
    			}
    			condition += " and d.create_stamp between '"+ beginTime_filter+ "' and '" + endTime_filter + "' ";
			}
			if ((plan_beginTime_filter != null && !"".equals(beginTime_filter))||(plan_endTime_filter != null && !"".equals(plan_endTime_filter))) {
        		if (plan_beginTime_filter == null || "".equals(plan_beginTime_filter)) {
        			plan_beginTime_filter = "1970-01-01";
    			}
        		
    			if (plan_endTime_filter == null || "".equals(plan_endTime_filter)) {
    				endTime_filter = "2037-12-31";
    			}
    			condition += " and tor.planning_time between '"+ plan_beginTime_filter+ "' and '" + plan_endTime_filter + "' ";
			}
			condition += " and d.create_stamp BETWEEN '1970-01-01' AND '2037-12-31'"
					+ " AND !(unix_timestamp(tor.planning_time) < unix_timestamp('2015-07-01') AND ifnull(c.abbr, '') = '江苏国光')"
					+ " AND d.customer_id IN ( SELECT customer_id FROM user_customer WHERE user_name = '"+currentUser.getPrincipal()+"' ) "
					+ " AND w.office_id IN (SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"')"
					+ " GROUP BY d.id ) A ";
			String sqlTotal = "SELECT count(1) total from (select count(1) total"
					+ " FROM delivery_order d"
					+ " LEFT JOIN party p ON d.customer_id = p.id"
					+ " LEFT JOIN contact c ON p.contact_id = c.id"
					+ " LEFT JOIN party p2 ON d.notify_party_id = p2.id"
					+ " LEFT JOIN contact c2 ON p2.contact_id = c2.id"
					+ " LEFT JOIN party p3 ON d.sp_id = p3.id"
					+ " LEFT JOIN contact c3 ON p3.contact_id = c3.id"
					+ " LEFT JOIN delivery_order_item dt2 ON dt2.delivery_id = d.id"
					+ " LEFT JOIN transfer_order_item_detail trid ON trid.id = dt2.transfer_item_detail_id"
					+ " LEFT JOIN warehouse w ON d.from_warehouse_id = w.id"
					+ " LEFT JOIN delivery_order_item doi ON doi.delivery_id = d.id"
					+ " LEFT JOIN transfer_order tor ON tor.id = doi.transfer_order_id"
					+ " LEFT JOIN office o ON o.id = tor.office_id"
					+ " LEFT JOIN transfer_order_item toi ON toi.order_id = tor.id";
			String sql = "select * from(SELECT toi.item_no item_no,trid.id tid,IFNULL(c2.contact_person, IFNULL(trid.notify_party_name, '')) driver,IFNULL(c2.phone,IFNULL(trid.notify_party_phone, '')) phone,pickup_mode,IFNULL(c2.address,IFNULL(trid.notify_party_company, '')) company,o.office_name,tor.customer_order_no,tor.STATUS statu,w.warehouse_name, "
					+ " (SELECT CASE"
					+ " 		WHEN d.cargo_nature ='ATM' THEN ("
					+ " 				select count(1) from delivery_order_item doi"
					+ " 		where doi.delivery_id = d.id"
					+ " 	 )"
					+ "  WHEN d.cargo_nature ='cargo' THEN ("
					+ " 	select sum(doi.amount) from delivery_order_item doi"
					+ " 	where doi.delivery_id = d.id"
					+ "  )"
					+ " 	 END ) amount,"
					+ " (SELECT CASE"
					+ " 		WHEN d.cargo_nature ='ATM' THEN ("
					+ " 				select sum(toid.pieces) from delivery_order_item doi, transfer_order_item_detail toid"
					+ " 		where doi.transfer_item_detail_id=toid.id and doi.delivery_id = d.id"
					+ " 	 )"
					+ "  WHEN d.cargo_nature ='cargo' THEN ("
					+ " 	select sum(doi.amount) from delivery_order_item doi where doi.delivery_id = d.id"
					+ "  )"
					+ " 	 END ) pcs_amount,"
					+ " tor.planning_time plan_time, d.*, c.abbr AS customer, c2.company_name AS c2,"
					+ "( SELECT group_concat( DISTINCT doi.transfer_no SEPARATOR ' ' ) FROM delivery_order_item doi WHERE delivery_id = d.id ) AS transfer_order_no,"
					+ " ( SELECT group_concat( trid.serial_no SEPARATOR ' ' )"
					+ " FROM "
					+ " delivery_order_item doi LEFT JOIN transfer_order_item_detail trid ON trid.id = doi.transfer_item_detail_id"
					+ " WHERE doi.delivery_id = d.id ) AS serial_no "
					+ " FROM delivery_order d"
					+ " LEFT JOIN party p ON d.customer_id = p.id"
					+ " LEFT JOIN contact c ON p.contact_id = c.id"
					+ " LEFT JOIN party p2 ON d.notify_party_id = p2.id"
					+ " LEFT JOIN contact c2 ON p2.contact_id = c2.id"
					+ " LEFT JOIN party p3 ON d.sp_id = p3.id"
					+ " LEFT JOIN contact c3 ON p3.contact_id = c3.id"
					+ " LEFT JOIN delivery_order_item dt2 ON dt2.delivery_id = d.id"
					+ " LEFT JOIN transfer_order_item_detail trid ON trid.id = dt2.transfer_item_detail_id"
					+ " LEFT JOIN warehouse w ON d.from_warehouse_id = w.id"
					+ " LEFT JOIN delivery_order_item doi ON doi.delivery_id = d.id"
					+ " LEFT JOIN transfer_order tor ON tor.id = doi.transfer_order_id"
					+ " LEFT JOIN office o ON o.id = tor.office_id"
					+ " LEFT JOIN transfer_order_item toi ON toi.order_id = tor.id";

			
			String orderByStr = " order by A.create_stamp desc ";
	        if(colName.length()>0){
	        	orderByStr = " order by A."+colName+" "+sortBy;
	        }
	        Record rec = Db.findFirst(sqlTotal+condition);
			logger.debug("total records:" + rec.getLong("total"));
			List<Record> transferOrders = Db.find(sql + condition + orderByStr + sLimit);

			transferOrderListMap.put("sEcho", pageIndex);
			transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
			transferOrderListMap.put("iTotalDisplayRecords",
					rec.getLong("total"));
			transferOrderListMap.put("aaData", transferOrders);
			renderJson(transferOrderListMap);
		}
	// 在途配送单list

	@RequiresPermissions(value = {PermissionConstant.PERMSSION_DOM_LIST})
	public void deliveryMilestone() {

		String transferorderNo = getPara("transferorderNo");
		String deliveryNo = getPara("deliveryNo");
		String customer = getPara("customer");
		String sp = getPara("sp");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String status = getPara("status");
		if(status != null && status != ""){
			if(status.equals("ok")){
				status = "('已送达', '已签收')";
			}else{
				status = "('已发车')";
			}
		}else{
			status = "('已发车','已送达', '已签收')";
		}
		

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "";
		

		String sql = " select DISTINCT d.*,(SELECT group_concat(DISTINCT cast(tor.planning_time as char) SEPARATOR '\r\n') from transfer_order tor LEFT JOIN delivery_order_item dt2 ON dt2.transfer_order_id = tor.id where dt2.delivery_id = d.id) planning_time," 
				+ " ("
				+ " select group_concat(DISTINCT toid.item_no SEPARATOR ' ') "
				+ " from delivery_order_item doi "
				+ " LEFT JOIN transfer_order_item_detail toid ON toid.id = doi.transfer_item_detail_id"
				+ " WHERE doi.delivery_id = d.id"
				+ " ) item_no,"
				+ " ("
				+ "  select sum(toid.pieces) from delivery_order_item doi "
				+ " 	LEFT JOIN transfer_order_item_detail toid ON toid.id = doi.transfer_item_detail_id"
				+ " WHERE doi.delivery_id = d.id"
				+ " ) pieces,"
				+ " c.abbr as customer,"
				+ " c2.company_name as c2,"
				+ " ("
				+ "  select group_concat("
				+ " 			DISTINCT toid.serial_no SEPARATOR ' '"
				+ " 	)  from delivery_order_item doi "
				+ " LEFT JOIN transfer_order_item_detail toid ON toid.id = doi.transfer_item_detail_id"
				+ " WHERE doi.delivery_id = d.id"
				+ " ) serial_no,"
				+ " (select group_concat(distinct doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no, "
				+ " (select location from delivery_order_milestone dom where delivery_id = d.id order by id desc limit 0,1) location "
				+ " from delivery_order d "
				+ " left join party p on d.customer_id = p.id "
				+ " left join contact c on p.contact_id = c.id "
				+ " left join party p2 on d.sp_id = p2.id "			
				+ " left join contact c2 on p2.contact_id = c2.id "
				+ " LEFT JOIN delivery_order_item dt2 ON dt2.delivery_id = d.id"
				+ " left join warehouse w on d.from_warehouse_id = w.id "
				+ " LEFT JOIN transfer_order_item_detail trid ON trid.id = dt2.transfer_item_detail_id"
				+ " LEFT JOIN transfer_order tor ON tor.id = dt2.transfer_order_id"
				+ " where !(unix_timestamp(tor.planning_time) < unix_timestamp('2015-07-01')AND ifnull(c.abbr, '') = '江苏国光') AND ifnull(d.create_stamp, '') BETWEEN '1-1-1'AND '9999-12-31' and ifnull(d. STATUS, '') IN "+status+""
				+ " and d.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
				+ " order by d.create_stamp desc" + sLimit;

		List<Record> depart = null;
		if (transferorderNo == null && deliveryNo == null && customer == null
				&& sp == null && beginTime == null && endTime == null) {
			sqlTotal ="select count(*) total from (select distinct d.*,"
					+ "c.abbr as customer,"
					+ "c2.company_name as c2,"
					+ ""
					+ "(select group_concat(doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no, "
					+ "(select location from delivery_order_milestone dom where delivery_id = d.id order by id desc limit 0,1) location "
					+ "from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "			
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " LEFT JOIN delivery_order_item dt2 ON dt2.delivery_id = d.id"
					+ " left join warehouse w on d.from_warehouse_id = w.id "
					+ " LEFT JOIN transfer_order_item_detail trid ON trid.id = dt2.transfer_item_detail_id"
					+ " LEFT JOIN transfer_order tor ON tor.id = dt2.transfer_order_id"
					+ " where !(unix_timestamp(tor.planning_time) < unix_timestamp('2015-07-01')AND ifnull(c.abbr, '') = '江苏国光') and ifnull(d.status,'') in "
					+ status
					+" AND ifnull(d.create_stamp,'') BETWEEN '1-1-1'AND '9999-12-31'"
					+ " and d.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')) as delivery_view ";
			depart = Db.find(sql);
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			sqlTotal ="select count(*) total from (select distinct d.*,"
					+ "c.abbr as customer,"
					+ "c2.company_name as c2,"
					+ ""
					+ "(select group_concat(doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no, "
					+ "(select location from delivery_order_milestone dom where delivery_id = d.id order by id desc limit 0,1) location "
					+ "from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "			
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " LEFT JOIN delivery_order_item dt2 ON dt2.delivery_id = d.id"
					+ " left join warehouse w on d.from_warehouse_id = w.id "
					+ " LEFT JOIN transfer_order_item_detail trid ON trid.id = dt2.transfer_item_detail_id"
					+ " LEFT JOIN transfer_order tor ON tor.id = dt2.transfer_order_id"
					+ " where !(unix_timestamp(tor.planning_time) < unix_timestamp('2015-07-01')AND ifnull(c.abbr, '') = '江苏国光') and ifnull(d.order_no,'') like '%"
					+ deliveryNo
					+ "%' and ifnull(c.abbr,'') like '%"
					+ customer
					+ "%' and ifnull(d.status,'') in "
					+ status
					+ " and ifnull(dt2.transfer_no,'') like '%"
					+ transferorderNo
					+ "%' and ifnull(c2.abbr,'') like'%"
					+ sp
					+ "%' and d.create_stamp between '"
					+ beginTime
					+ "' and '"
					+ endTime + "' "
					+ " and d.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')) as delivery_view ";
			String sql_seach = "select distinct d.*,(SELECT group_concat(DISTINCT cast(tor.planning_time as char) SEPARATOR '\r\n') from transfer_order tor LEFT JOIN delivery_order_item dt2 ON dt2.transfer_order_id = tor.id where dt2.delivery_id = d.id) planning_time,"
					+ " ("
					+ " select group_concat(DISTINCT toid.item_no SEPARATOR ' ') "
					+ " from delivery_order_item doi "
					+ " LEFT JOIN transfer_order_item_detail toid ON toid.id = doi.transfer_item_detail_id"
					+ " WHERE doi.delivery_id = d.id"
					+ " ) item_no,"
					+ " ("
					+ "  select sum(toid.pieces) from delivery_order_item doi "
					+ " 	LEFT JOIN transfer_order_item_detail toid ON toid.id = doi.transfer_item_detail_id"
					+ " WHERE doi.delivery_id = d.id"
					+ " ) pieces,"
					+ " c.abbr as customer,"
					+ " c2.company_name as c2,"
					+ " ("
					+ "  select group_concat("
					+ " 			DISTINCT toid.serial_no SEPARATOR ' '"
					+ " 	)  from delivery_order_item doi "
					+ " LEFT JOIN transfer_order_item_detail toid ON toid.id = doi.transfer_item_detail_id"
					+ " WHERE doi.delivery_id = d.id"
					+ " ) serial_no,"
					+ " (select group_concat(doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no, "
					+ " (select location from delivery_order_milestone dom where delivery_id = d.id order by id desc limit 0,1) location "
					+ " from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "			
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " LEFT JOIN delivery_order_item dt2 ON dt2.delivery_id = d.id"
					+ " left join warehouse w on d.from_warehouse_id = w.id "
					+ " LEFT JOIN transfer_order_item_detail trid ON trid.id = dt2.transfer_item_detail_id"
					+ " LEFT JOIN transfer_order tor ON tor.id = dt2.transfer_order_id"
					+ " where  !(unix_timestamp(tor.planning_time) < unix_timestamp('2015-07-01')AND ifnull(c.abbr, '') = '江苏国光') AND ifnull(d.create_stamp, '') BETWEEN '1-1-1'AND '9999-12-31' and ifnull(d.order_no,'') like '%"
					+ deliveryNo
					+ "%' and ifnull(c.abbr,'') like '%"
					+ customer
					+ "%' and ifnull(d.status,'') in "
					+ status
					+ " and ifnull(dt2.transfer_no,'') like '%"
					+ transferorderNo
					+ "%' and ifnull(c2.abbr,'') like'%"
					+ sp
					+ "%' and d.create_stamp between '"
					+ beginTime
					+ "' and '"
					+ endTime + "'"
					+ " and d.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')order by d.create_stamp desc" + sLimit;
			depart = Db.find(sql_seach);
		}
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		
		Map map = new HashMap();
		map.put("sEcho", pageIndex);
		map.put("iTotalRecords", rec.getLong("total"));
		map.put("iTotalDisplayRecords", rec.getLong("total"));
		map.put("aaData", depart);

		renderJson(map);
	}

	@RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_CREATE})
	public void add() {
		setAttr("saveOK", false);
		
		Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
		setAttr("customizeField", customizeField);
		render("/yh/delivery/deliveryOrderSearchTransfer.html");
	}
	//@RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_UPDATE})
	public void edit() {
		String id = getPara("id");
		//System.out.println(id);
		DeliveryOrder tOrder = DeliveryOrder.dao.findById(id);
		setAttr("cargoNature", tOrder.get("cargo_nature"));
		setAttr("deliveryOrder", tOrder);
		List<Record> serIdList = Db
				.find("select * from delivery_order_item where delivery_id ="
						+ id);

		if("cargo".equals(tOrder.get("cargo_nature"))){
			Record rec = Db.findFirst("select transfer_no ,concat(group_concat(cast(product_id as char) separator ',')) productIds,"
					+ " concat(group_concat(cast(product_number as char) separator ',')) productNumbers,"
					+ " concat(group_concat(cast(transfer_item_id as char) separator ',')) transferItemIds"
					+ " from delivery_order_item where delivery_id = "+ id);
			
			setAttr("productIds", rec.get("productIds"));
			setAttr("shippingNumbers", rec.get("productNumbers"));
			setAttr("transferItemIds", rec.get("transferItemIds"));
			setAttr("transferOrderNo", rec.get("transfer_no"));
		}else{
			// 运输单信息
			if (serIdList.size() > 0) {
				if (serIdList.get(0).get("transfer_item_detail_id") == null) {
					TransferOrder transferOrder = TransferOrder.dao
							.findById(serIdList.get(0).get("transfer_order_id"));
					setAttr("transferOrder", transferOrder);
				}
			}
			if (serIdList.size() != 0) {
				// 序列号id
				String idStr = "";
				for (Record record : serIdList) {
					idStr += record.get("transfer_item_detail_id") + ",";
				}// 4,5,6
				idStr = idStr.substring(0, idStr.length() - 1);
				setAttr("localArr2", idStr);
				// 序列号运输单id数组
				List<Record> transferIdList = Db
						.find("select transfer_order_id from delivery_order_item where delivery_id ="
								+ id);
				String idStr2 = "";
				for (Record record : transferIdList) {
					idStr2 += record.get("transfer_order_id") + ",";
				}// 4,5,6
				idStr2 = idStr2.substring(0, idStr2.length() - 1);
				setAttr("localArr", idStr2);
			} else {
				// 运输单id
				DeliveryOrderItem deliveryOrderItem = DeliveryOrderItem.dao
						.findFirst("select transfer_order_id from delivery_order_item where delivery_id ="
								+ id);
				// List<Record> transferId =
				// Db.find("select transfer_order_id from delivery_order_item where delivery_id ="+
				// id);
				// TODO: 如果transferId==null, 下面这句报错！！！
				// String transferId2 =
				// transferId.get(0).get("transfer_order_id").toString();
				if (deliveryOrderItem != null) {
					setAttr("transferId", deliveryOrderItem.get("id"));
				} else {
					setAttr("transferId", null);
				}

			}
			
		}
		String sql = "select p.id as cid,c.contact_person,c.company_name,c.address,c.mobile from party p left join contact c on c.id = p.contact_id where p.id = "+tOrder.get("customer_id");
		//Record customerContact = Db.findFirst(sql);
		Party customerContact = Party.dao.findFirst(sql);

		// 供应商信息
		Party spContact = Party.dao
				.findFirst("select *,p.id as spid from party p,contact c where p.id ='"
						+ tOrder.get("sp_id") + "'and p.contact_id = c.id");

		// 收货人信息
		Contact notifyPartyContact = null;
		if (tOrder.get("notify_party_id") != null) {
			notifyPartyContact = (Contact) Contact.dao
					.findFirst(
							"select c.abbr as company,c.phone,c.contact_person,"
							+ "c.address,c.mobile,c.company_name, "
							+ "p.id as pid,c.id as contactId from party p, contact c where p.contact_id=c.id and p.id =?",
							tOrder.get("notify_party_id"));
			setAttr("notifyParty", notifyPartyContact);
		}
		else{
			TransferOrderItemDetail transferorderitemdetail =TransferOrderItemDetail.dao.findFirst("SELECT d.* FROM transfer_order_item_detail d LEFT JOIN delivery_order_item doi on doi.transfer_item_detail_id=d.id WHERE doi.delivery_id=?",id);
			setAttr("notifytransferorder", transferorderitemdetail);
 		}
		
		//RDC
		Warehouse warehouse = Warehouse.dao
				.findById(tOrder.get("from_warehouse_id"));
		Office office =  Office.dao.findById(warehouse.get("office_id"));
		
		setAttr("warehouse", warehouse);
		setAttr("office", office);

		setAttr("deliveryId", tOrder);
		setAttr("customer", customerContact);
		setAttr("spContact", spContact);
		
		String routeFrom = tOrder.get("route_from");
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
		
		
		String routeTo = tOrder.get("route_to");
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
			List<Record> paymentItemList = Collections.EMPTY_LIST;
			paymentItemList = Db.find("select * from fin_item where type='应付'");
			setAttr("paymentItemList", paymentItemList);
		}
		
		
		Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
		
		setAttr("customizeField", customizeField);
		render("/yh/delivery/deliveryOrderEdit.html");

	}

	// 配送单客户
	public void selectCustomer() {
		List<Contact> customer = Contact.dao
				.find("select * from contact where id in(select contact_id from party where id in(select customer_id from transfer_order group by customer_id)) and id='1'");
		renderJson(customer);
	}

	/*
	 * // 供应商列表,列出最近使用的5个客户 public void selectSp() { List<Contact> contactjson =
	 * Contact.dao .find(
	 * "select * from contact c  where id in (select contact_id from party where party_type='SERVICE_PROVIDER' order by last_update_date desc limit 0,5)"
	 * ); renderJson(contactjson); }
	 */
	public void creat2() {

		String customerId = getPara("customerId");
		String warehouseId = getPara("warehouseId");
		//String transferOrderNo = getPara("transferOrderNo1");
		String transferItemIds = getPara("transferItemIds");
		String productIds = getPara("productIds");
		String shippingNumbers = getPara("shippingNumbers");
		String cargoNature = getPara("cargoNature");
//		//客户ID
//		Record record = Db.findFirst("SELECT tor.customer_id FROM transfer_order tor LEFT JOIN transfer_order_item toi on toi.order_id = tor.id where toi.id in (?)",transferItemIds);
//		Long customerId = record.getLong("customer_id");
		//客户
		if (customerId != null) {
			
			Party party = Party.dao
					.findFirst("select p.id as cid,c.contact_person,c.company_name,c.address,c.mobile from party p left join contact c on c.id = p.contact_id where p.id = '"+ customerId + "'");
			setAttr("customer", party);
		}
		
		Warehouse warehouse = Warehouse.dao.findById(warehouseId);
		//rdc
		if(warehouse != null){
			Office office =  Office.dao.findById(warehouse.get("office_id"));
			setAttr("office", office);
			
			//初始地
			String routeFrom = warehouse.get("location");
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
		}
		TransferOrderItem transferItem = TransferOrderItem.dao.findById(transferItemIds);
		TransferOrder transferOrder = TransferOrder.dao.findById(transferItem.get("order_id"));
		setAttr("transferItemIds", transferItemIds);
		setAttr("productIds", productIds);
		setAttr("shippingNumbers", shippingNumbers);
		setAttr("transferOrderNo", transferOrder.get("order_no"));
		setAttr("transferOrdercompany_name", transferOrder.get("receiving_unit"));
		setAttr("transferOrderaddress", transferOrder.get("receiving_address"));
		setAttr("transferOrderPerson", transferOrder.get("receiving_name"));
		setAttr("transferOrderPhone", transferOrder.get("receiving_phone"));
		setAttr("warehouse", warehouse);
		setAttr("cargoNature", cargoNature);//货品属性
		//setAttr("cargoNaturName", "普通货品");
		List<Record> paymentItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("paymentItemList", paymentItemList);
		
		render("/yh/delivery/deliveryOrderEdit.html");
	}
	

	// 创建配送单普通货品
	public void creat() {
		// customer, sp, notify_party
		String id = getPara();
			// render("/yh/delivery/deliveryOrderEdit.html");
			renderJson(id);
		// renderJson("{\"success\":true}");
	}

	// 创建运输单ATM机
	public void creat3() {
		String id = getPara();
		String list = this.getPara("localArr");
		String list2 = this.getPara("localArr2");// 序列号id
		String list3 = this.getPara("localArr3");
		String cusId = getPara("cusId");
		String rdc = getPara("hiddenRdc");
		String cargoNature = getPara("cargoNature");
		Party party = Party.dao
				.findFirst("select p.id as cid,c.contact_person,c.company_name,c.address,c.mobile from party p left join contact c on c.id = p.contact_id where p.id = '"
						+ cusId + "'");
		setAttr("localArr", list);
		setAttr("localArr2", list2);
		setAttr("localArr3", list3);
		setAttr("customer", party);
		
		//rdc
		Office office =  Office.dao.findById(rdc);
		setAttr("office", office);
		

		String sql="select t.receiving_unit as company,c1.* ,td.notify_party_phone as phone,"
				+ " td.notify_party_name as contact_person,td.notify_party_company as address,t.route_from "
				+ " from transfer_order_item_detail td "
				+ " left join transfer_order t on t.id =td.order_id "
				+ " left join party p on p.id = td.notify_party_id "
				+ " left join contact c1 on p.contact_id =c1.id  where td.id in(" + list2 + ")";
		TransferOrderItemDetail notify = TransferOrderItemDetail.dao
				.findFirst(sql);
		// 选取的配送货品的仓库必须一致
		TransferOrder tOrder = TransferOrder.dao
				.findFirst("select warehouse_id from transfer_order where id in("
						+ list + ")");
		
		Warehouse warehouse = Warehouse.dao
				.findById(tOrder.get("warehouse_id"));
		
		String routeFrom = office.get("location");
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
		
		setAttr("notifyParty", notify);
		setAttr("warehouse", warehouse);
		setAttr("cargoNature", cargoNature);
		List<Record> paymentItemList = Collections.EMPTY_LIST;
		paymentItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("paymentItemList", paymentItemList);
			render("/yh/delivery/deliveryOrderEdit.html");
	}

	// 获取运输单普通货品list
	public void searchTransfer() {
		String deliveryOrderNo = getPara("deliveryOrderNo1");
		String customerName = getPara("customerName1");
		String orderStatue = getPara("orderStatue1");
		String warehouse = getPara("warehouse1");
		
		Map transferOrderListMap = new HashMap();
		if(warehouse==null&&customerName==null){

			transferOrderListMap.put("sEcho", 0);
			transferOrderListMap.put("iTotalRecords", 0);
			transferOrderListMap.put("iTotalDisplayRecords",null);
			transferOrderListMap.put("aaData", 0);
			
			renderJson(transferOrderListMap);
			return;
		}

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		};
		
		String sqlTotal = "select count(1) total from inventory_item ii "
                +" left join product pro on ii.product_id = pro.id "
                +" left join warehouse w on ii.warehouse_id = w.id "
                +" left join party p on ii.party_id = p.id "
                +" left join contact c  on p.contact_id = c.id ";
		
		String sql = "select ii.id inventoryId,ii.total_quantity, ii.product_id, ii.party_id, ii.available_quantity, pro.*, w.warehouse_name, w.id as warehouse_id, c.abbr, ii.party_id as customer_id from inventory_item ii "
                +" left join product pro on ii.product_id = pro.id "
                +" left join warehouse w on ii.warehouse_id = w.id "
                +" left join party p on ii.party_id = p.id "
                +" left join contact c  on p.contact_id = c.id ";
		
		if (deliveryOrderNo == null && customerName == null
				&& orderStatue == null && warehouse == null) {
			
			Record rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));
			
			List<Record> transferOrders = Db.find(sql);

			transferOrderListMap.put("sEcho", pageIndex);
			transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
			transferOrderListMap.put("iTotalDisplayRecords",
					rec.getLong("total"));
			transferOrderListMap.put("aaData", transferOrders);
		} else {
			String sqlFilter="";
			if(warehouse!=null&&warehouse!=""&&customerName!=null&&customerName!=""){
				sqlFilter= "where ifnull(w.warehouse_name,'') like '%" + warehouse + "%'"
                    + "and ifnull(c.abbr,'') like '%" + customerName + "%'";
			}
			Record rec = Db.findFirst(sqlTotal+sqlFilter);
			logger.debug("total records:" + rec.getLong("total"));

			List<Record> transferOrders = Db.find(sql+sqlFilter);

			transferOrderListMap.put("sEcho", pageIndex);
			transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
			transferOrderListMap.put("iTotalDisplayRecords",
					rec.getLong("total"));
			transferOrderListMap.put("aaData", transferOrders);
		}
		renderJson(transferOrderListMap);
	}

	// 获取运输单ATM序列号list
	public void searchTransferByATM() {
		String deliveryOrderNo = getPara("deliveryOrderNo");
		String customerName = getPara("customerName");
		String orderStatue = getPara("orderStatue");
		String warehouse = getPara("warehouse");
		String code =getPara("code");
		String customer_order_number=getPara("customer_order_number");
		String singleid = getPara("singleid");
		String inputStrrdc =getPara("inputStrrdc");
		String inputStr = getPara("warehouse");
    	String rdc = getPara("rdc");
    	String wnsql ="";	
    	if(inputStr != null && rdc != null && !"".equals(inputStr) && !"".equals(rdc)){
    		wnsql = "select w.warehouse_name from warehouse w left join office o on o.id = w.office_id where w.warehouse_name LIKE '%"+inputStr+"%' and o.id = "+rdc;
    	}else if(inputStr != null && !"".equals(inputStr)){
    		wnsql = "select w.warehouse_name from warehouse where warehouse_name like '%"+inputStr+"%'";
    	}else if(rdc != null && !"".equals(rdc)){
    		wnsql = "select w.warehouse_name from warehouse w left join office o on o.id = w.office_id where o.id = "+rdc;
    	}else{
    		wnsql= "select w.warehouse_name from warehouse";
    	}
		Map transferOrderListMap = new HashMap();
		if(customerName==null&&inputStrrdc==null){
			transferOrderListMap.put("sEcho", 0);
			transferOrderListMap.put("iTotalRecords", 0);
			transferOrderListMap.put("iTotalDisplayRecords",null);
			transferOrderListMap.put("aaData", 0);
			renderJson(transferOrderListMap);
			return;
		}
		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sqlTotal="select count(1) total from transfer_order_item_detail t1 "
					+ " left join transfer_order t2 on t1.order_id=t2.id "
					+ " LEFT JOIN depart_order doi on doi.id=t1.depart_id"
					+ " LEFT JOIN transfer_order_milestone tom on tom.depart_id=doi.id"
					+ " left join warehouse w on t2.warehouse_id = w.id "
					+ " left join party p on t2.customer_id = p.id "
					+ " left join party p2 on t1.notify_party_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join office o on o.id = t2 .office_id "
					+ " where delivery_id is null "
					+ " and t2.cargo_nature = 'ATM' "
					+ " and tom.`STATUS`='已入库' "
					+ " and (t1.is_delivered is null or t1.is_delivered = false)  "
					+ " and t1.depart_id is not null"
					+ " and t2.`STATUS` != '手动删除' ";
		
		String sql="";
		if (deliveryOrderNo == null && customerName == null && orderStatue == null && warehouse == null&&code==null) {
			sql= "select t1.id id,t1.serial_no,t1.item_no,t1.id as tid,t1.notify_party_company as company,t2.*,w.warehouse_name,c.abbr,c2.address as Naddress "
					+ " from transfer_order_item_detail t1 "
					+ " left join transfer_order t2 on t1.order_id=t2.id "
					+ " LEFT JOIN depart_order doi on doi.id=t1.depart_id"
					+ " LEFT JOIN transfer_order_milestone tom on tom.depart_id=doi.id"
					+ " left join warehouse w on t2.warehouse_id = w.id "
					+ " left join party p on t2.customer_id = p.id "
					+ " left join party p2 on t1.notify_party_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join office o on o.id = t2 .office_id "
					+ " left join party p3 on t2.sp_id = p3.id "
					+ " left join contact c3 on p3.contact_id = c3.id "
					/*+ " where (t2.status='已入库' or t2.status ='部分已入库') "*/
					+ " where delivery_id is null "
					+ " and t2.cargo_nature = 'ATM' "
					+ " and tom.`STATUS`='已入库' "
					+ " and (t1.is_delivered is null or t1.is_delivered = false)  "
					+ " and t1.depart_id is not null"
					+ " and t2.`STATUS` != '手动删除' "
					+ " order by t1.serial_no asc " + sLimit;
			
		} else {
			 sql ="select  t1.serial_no,t1.item_no,t1.pieces,t2.arrival_time,c3.contact_person driver,c3.phone,o.office_name, t1.id as tid,t1.notify_party_company as company, t2.*,w.warehouse_name,c.abbr "
			 		+ " from transfer_order_item_detail t1 "
					+ " left join transfer_order t2 on t1.order_id=t2.id "
					+ " LEFT JOIN depart_order doi on doi.id=t1.depart_id"
					+ " LEFT JOIN transfer_order_milestone tom on tom.depart_id=doi.id"
					+ " left join warehouse w on t2.warehouse_id = w.id "
					+ " left join party p on t2.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on t1.notify_party_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join office o on o.id = t2 .office_id "
					+ " left join party p3 on t2.sp_id = p3.id "
					+ " left join contact c3 on p3.contact_id = c3.id "
					/*+ " where (t2.status='已入库' or t2.status ='部分已入库') "*/
					+ " where delivery_id is null "
					+ " and t2.cargo_nature = 'ATM' "
					+ " and tom.`STATUS`='已入库' "
					+ " and (t1.is_delivered is null or t1.is_delivered = false)  "
					+ " and t1.depart_id is not null"
					+ " and t2.`STATUS` != '手动删除' ";
			if(code!=""&&code!=null){
				sqlTotal =sqlTotal+" and serial_no like '%"+code+"%'";
				sql =sql +" and serial_no like '%"+code+"%'";
			}
			
			if(deliveryOrderNo!=""&&deliveryOrderNo!=null){
				sqlTotal =sqlTotal+" and ifnull(t2.order_no,'') like '%"+ deliveryOrderNo+ "%'";
				sql =sql+" and ifnull(t2.order_no,'') like '%"+ deliveryOrderNo+ "%'";
			}
			if(singleid!=""&&singleid!=null){
				sqlTotal =sqlTotal+" and ifnull(t1.id,'') like '%"+ singleid+ "%'";
				sql =sql+" and ifnull(t1.id,'') like '%"+ singleid+ "%'";
			}
			if(customer_order_number!=""&&customer_order_number!=null){
				sqlTotal =sqlTotal+" and ifnull(t2.customer_order_no,'') like '%"+ customer_order_number+ "%'";
				sql =sql+" and ifnull(t2.customer_order_no,'') like '%"+ customer_order_number+ "%'";
			}
			
			if(inputStrrdc!=""&&customerName!=""&&inputStrrdc!=null&&customerName!=null){
				sqlTotal += " and w.warehouse_name like '%" + warehouse + "%' "
						+ " and w.warehouse_name in("+wnsql+")"
					+ " and c.abbr like '%" + customerName + "%' order by t1.serial_no asc";
				sql += " and w.warehouse_name like '%" + warehouse + "%' "
						+ " and w.warehouse_name in("+wnsql+")"
					+ " and c.abbr like '%" + customerName + "%' order by t1.serial_no asc";
			}
			
			if(orderStatue!=""&&orderStatue!=null){
				sqlTotal += "and ifnull(t2.status,'') like '%" + orderStatue + "%'";
				sql += "and ifnull(t2.status,'') like '%" + orderStatue + "%' order by t1.serial_no asc";
			}
		}
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		List<Record> transferOrders = Db.find(sql + sLimit);

		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords",rec.getLong("total"));
		transferOrderListMap.put("aaData", transferOrders);
		renderJson(transferOrderListMap);
	}

	public void cancel() {
		String id = getPara();
		System.out.println(id);
		DeliveryOrder.dao.findById(id).set("Status", "取消").update();
		renderJson("{\"success\":true}");
	}

	// 查找供应商
	public void searchSp() {
		String input = getPara("input");
		
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();
		
		List<Record> locationList = Collections.EMPTY_LIST;
		String sql = "";
		if(input!=null&&input!=""){
			sql= "select p.id pid,p.*, c.*,c.id cid from party p left join contact c on c.id = p.contact_id where sp_type = 'delivery' and c.abbr like '%"+input+"%' and p.office_id = "+parentID;
		}else{
			sql= "select p.id pid,p.*, c.*,c.id cid from party p left join contact c on c.id = p.contact_id where sp_type = 'delivery' and p.office_id = "+parentID;
		}
		
		locationList = Db.find(sql);
		renderJson(locationList);
	}
	public void searchPartSp() {
		String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		 ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();
		String sql = "";
		if(input!=null&&input!=""){
			sql= "select p.id pid,p.*, c.*,c.id cid from party p left join contact c on c.id = p.contact_id left join office o on o.id = p.office_id where sp_type = 'delivery' and (p.is_stop is null or p.is_stop = 0) and c.abbr like '%"+input+"%'  and (o.id = "+parentID + " or o.belong_office = "+parentID + ")";
		}else{
			sql= "select p.id pid,p.*, c.*,c.id cid from party p left join contact c on c.id = p.contact_id left join office o on o.id = p.office_id where sp_type = 'delivery' and (p.is_stop is null or p.is_stop = 0) and (o.id = "+parentID + " or o.belong_office = "+parentID + ")";
		}
		
		locationList = Db.find(sql);
		renderJson(locationList);
	}
	// 配送单货品list
	public void orderList() {
		String idlist = getPara("localArr");
		// String idlist2 = getPara("localArr2");
		String aa = getPara("aa");
		if (aa == null || aa.equals("")) {
			Map Map = new HashMap();
			Map.put("sEcho", 0);
			Map.put("iTotalRecords", 0);
			Map.put("iTotalDisplayRecords", 0);
			Map.put("aaData", null);
			renderJson(Map);
			return;
		}
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from transfer_order_item tof"
				+ " left join transfer_order t_o on tof.order_id =t_o.id "
				+ " left join contact c on c.id in (select contact_id from party p where t_o.customer_id=p.id)"
				+ " where tof.order_id in(" + idlist + ")";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		String sql = "";
		List<Record> departOrderitem = null;

		sql = "select tof.* ,t_o.order_no as order_no,c.abbr as customer from transfer_order_item tof "
				+ " left join transfer_order t_o on tof.order_id =t_o.id "
				+ "left join contact c on c.id in (select contact_id from party p where t_o.customer_id=p.id) "
				+ " where tof.order_id in("
				+ idlist
				+ ")  order by c.id"
				+ sLimit;
		departOrderitem = Db.find(sql);
		for (int i = 0; i < departOrderitem.size(); i++) {
			String itemname = departOrderitem.get(i).get("item_name");
			if ("ATM".equals(itemname)) {
				Long itemid = departOrderitem.get(i).get("id");
				String sql2 = "select serial_no from transfer_order_item_detail  where item_id ="
						+ itemid;
				List<Record> itemserial_no = Db.find(sql2);
				String itemno = itemserial_no.get(0).get("SERIAL_NO");
				departOrderitem.get(i).set("serial_no", itemno);
			} else {
				departOrderitem.get(i).set("serial_no", "无");
			}
		}
		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));

		Map.put("aaData", departOrderitem);

		renderJson(Map);
	}
	
	//更新货品明细列表
	public void updateTansterOrderItemDetail(){
		String detailId = getPara("detailId");
		String name = getPara("name");
		String value = getPara("value");
		
		TransferOrderItemDetail t =TransferOrderItemDetail.dao.findById(detailId);
		t.set(name, value).update();

		renderJson(t);
	}
	
	
	
	
	// 配送单货品ATMlist
	public void orderList2() {
		String idlist = getPara("localArr");
		String idlist2 = getPara("localArr2");
		if (idlist2 == null || idlist2.equals("")) {
			Map Map = new HashMap();
			Map.put("sEcho", 0);
			Map.put("iTotalRecords", 0);
			Map.put("iTotalDisplayRecords", 0);
			Map.put("aaData", null);
			renderJson(Map);
			return;
		}
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from transfer_order_item_detail t "
				+ "left join transfer_order_item t1 on t.item_id =t1.id "
				+ "left join transfer_order t3 on t3.id =t.order_id "
				+ "left join contact c on c.id in (select contact_id from party p where t3.customer_id=p.id) "
				+ "where t.id in(" + idlist2 + ")";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		String sql = "";
		List<Record> departOrderitem = null;
		sql = "select t.*,t3.order_no,c.abbr as customer from transfer_order_item_detail t "
				+ "left join transfer_order_item t1 on t.item_id =t1.id "
				+ "left join transfer_order t3 on t3.id =t.order_id "
				+ "left join contact c on c.id in (select contact_id from party p where t3.customer_id=p.id) "
				+ "where t.id in(" + idlist2 + ") order by t.id desc " + sLimit;
		/*
		 * sql =
		 * "select tof.* ,t_o.order_no as order_no,c.abbr as customer,toid.serial_no as serial_no from transfer_order_item tof "
		 * + " left join transfer_order  t_o  on tof.order_id =t_o.id " +
		 * "left join contact c on c.id in (select contact_id from party p where t_o.customer_id=p.id) "
		 * +
		 * "left join transfer_order_item_detail toid on toid.item_id =tof.id "
		 * + " where toid.id in(" + idlist2 + ")" + sLimit;
		 */
		departOrderitem = Db.find(sql);

		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));

		Map.put("aaData", departOrderitem);

		renderJson(Map);
	}

	// 配送单保存
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_CREATE,PermissionConstant.PERMSSION_DYO_UPDATE},logical=Logical.OR)
	@Before(Tx.class)
	public void deliverySave() {
        String car_type = getPara("car_type");// 供应商计费类型, 如果是整车，需要知道整车类型
        String ltlPriceType = getPara("ltlUnitType");//如果是零担，需要知道零担计费类型：按体积，按重量
		String orderNo = OrderNoGenerator.getNextOrderNo("PS");
		String deliveryid = getPara("delivery_id");
		DeliveryOrder deliveryOrder = null;
		String notifyId = getPara("notify_id");
		String sign_document_no = getPara("sign_document_no");//签收单据号
		String spId = getPara("sp_id");
		String cargoNature = getPara("cargoNature");
		String idlist3 = getPara("localArr");
		String idlist5 = getPara("localArr3");
		String warehouseId = getPara("warehouse_id");
		String customerId = getPara("customer_id");
		String businessStamp = getPara("business_stamp");
		String clientOrderStamp = getPara("client_order_stamp");
		String orderDeliveryStamp  = getPara("order_delivery_stamp");
		String transferOrderNo  = getPara("transferOrderNo").trim();
		String receivingunit =getPara("receivingunit");
		String[] idlist = getPara("localArr").split(",");
		String[] idlist2 = getPara("localArr2").split(",");
		String[] idlist4 = getPara("localArr3").split(",");
		String[] transferItemId =  getPara("transferItemIds").split(",");
		String[] productId =  getPara("productIds").split(",");
		String[] shippingNumber =  getPara("shippingNumbers").split(",");

		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");

		Date createDate = Calendar.getInstance().getTime();

		Party party = new Party();
		Contact contact = new Contact();
		deliveryOrder = new DeliveryOrder();
		if (notifyId == null || notifyId.equals("")) {
			contact.set("company_name", getPara("notify_company_name"))
					.set("contact_person", getPara("notify_contact_person"))
					.set("address", getPara("notify_address"))
					.set("phone", getPara("notify_phone"))
					.set("mobile", getPara("notify_mobile"));
			contact.save();
			party.set("contact_id", contact.get("id")) 
					.set("party_type", "NOTIFY_PARTY")
					.set("create_date", createDate)
					.set("creator", users.get(0).get("id"));
			party.save();
		} else {
			contact.set("id", getPara("contact_id"))
					.set("company_name", getPara("notify_company_name"))
					.set("contact_person", getPara("notify_contact_person"))
					.set("address", getPara("notify_address"))
					.set("mobile", getPara("notify_mobile"))
					.set("phone", getPara("notify_phone")).update();
		}

		if (deliveryid == null || "".equals(deliveryid)) {
			deliveryOrder.set("order_no", orderNo)
					.set("customer_id", customerId)
					.set("sp_id", spId)
					.set("notify_party_id", party.get("id"))
					.set("create_stamp", createDate).set("status", "新建")
					.set("route_to", getPara("route_to"))
					.set("route_from", getPara("route_from"))
					.set("pricetype", getPara("chargeType"))
					.set("from_warehouse_id", warehouseId)
					.set("cargo_nature", cargoNature)
					.set("receivingunit", receivingunit)
					.set("ref_no", sign_document_no)
					.set("client_requirement", getPara("client_requirement"))
					.set("ltl_price_type", ltlPriceType).set("car_type", car_type)
					.set("customer_delivery_no",getPara("customerDelveryNo"));

			if (notifyId == null || notifyId.equals("")) {
				deliveryOrder.set("notify_party_id", party.get("id"));
			} else {
				deliveryOrder.set("notify_party_id", notifyId);
			}
			
			if(!"".equals(businessStamp) && businessStamp != null)
				deliveryOrder.set("business_stamp", businessStamp);
			if(!"".equals(clientOrderStamp) && clientOrderStamp != null)
				deliveryOrder.set("client_order_stamp", clientOrderStamp);
			if(!"".equals(orderDeliveryStamp) && orderDeliveryStamp != null)
				deliveryOrder.set("order_delivery_stamp", orderDeliveryStamp);

			deliveryOrder.set("audit_status", "新建");
			deliveryOrder.set("sign_status", "未回单");
			if("cargo".equals(cargoNature)){
				deliveryOrder.set("delivery_plan_type", "untreated");
			}
			deliveryOrder.save();
			if("cargo".equals(cargoNature)){
				TransferOrder order = TransferOrder.dao.findFirst("select * from transfer_order where order_no = '"+ transferOrderNo + "';");
				for (int i = 0; i < productId.length; i++) {
					//修改可用库存
					//InventoryItem item = InventoryItem.dao.findFirst("select * from inventory_item ii where ii.warehouse_id = '" + warehouseId + "' and ii.product_id = '" + productId[i] + "' and ii.party_id = '" + customerId + "';");
					//item.set("available_quantity", item.getDouble("total_quantity") - Double.parseDouble(shippingNumber[i])).update();
	
					//更新transferOrderIten表
					TransferOrderItem transferOrderItem = TransferOrderItem.dao.findById(transferItemId[i]);
					Double total_amount = transferOrderItem.getDouble("amount");
					Double Tcomplete_amount = transferOrderItem.getDouble("complete_amount");
					if(Tcomplete_amount == null){
						Tcomplete_amount = 0.00;
					}
//					Double complete_amount = Tcomplete_amount + Double.valueOf(shippingNumber[i]) ;
					Double this_amount = Double.valueOf(shippingNumber[i]);
//					transferOrderItem.set("complete_amount", complete_amount).update();
					//新增配送单从表
					DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
					deliveryOrderItem.set("delivery_id", deliveryOrder.get("id"))
					.set("transfer_item_id", transferItemId[i])
					.set("transfer_no", transferOrderNo)
					.set("transfer_order_id",order.get("id"))
					.set("amount", this_amount)
					.save();
					
				
				} 
			}else{
				String string = getPara("tranferid");
				// 改变运输单状态
				if (!string.equals("")) {
					TransferOrder tOrder = TransferOrder.dao
							.findById(getPara("tranferid"));
					tOrder.set("status", "配送中");
					tOrder.update();
				}

				if (!idlist3.equals("")) {
					for (int i = 0; i < idlist.length; i++) {
						DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
						deliveryOrderItem.set("delivery_id",deliveryOrder.get("id"))
						.set("transfer_order_id",idlist[i]);
						deliveryOrderItem.set("transfer_item_detail_id", idlist2[i]);
						deliveryOrderItem.set("transfer_no", idlist4[i]);
						deliveryOrderItem.set("amount", 1);
						deliveryOrderItem.save();
						
						/*TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao.findById(idlist2[i]);
						transferOrderItemDetail.set("delivery_id", deliveryOrder.get("id"));
						transferOrderItemDetail.update();*/
					}
				} else {
					DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
					deliveryOrderItem.set("delivery_id", deliveryOrder.get("id"))
							.set("transfer_order_id", getPara("tranferid"))
							.set("transfer_no", idlist5);
					deliveryOrderItem.save();
				}

				// 在单品中设置delivery_id
				String detailIds = getPara("localArr2");
				String[] detailIdArr = detailIds.split(",");
				for (int i = 0; i < detailIdArr.length; i++) {
					TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
							.findById(detailIdArr[i]);
					transferOrderItemDetail.set("delivery_id",
							deliveryOrder.get("id"));
					transferOrderItemDetail.set("is_delivered", true);
					transferOrderItemDetail.update();
				}
			}
			saveDeliveryOrderMilestone(deliveryOrder);
		} else {

			deliveryOrder.set("sp_id", spId)
					.set("Customer_id", customerId)
					.set("id", deliveryid).set("route_to", getPara("route_to"))
					.set("route_from", getPara("route_from"))
					.set("priceType", getPara("chargeType"))
					.set("receivingunit", receivingunit)
					.set("client_requirement", getPara("client_requirement"))
					.set("ltl_price_type", ltlPriceType).set("car_type", car_type)
					.set("customer_delivery_no", getPara("customerDelveryNo"))
					.set("ref_no", sign_document_no);
			if("".equals(getPara("notify_id")) || getPara("notify_id") == null)
				deliveryOrder.set("notify_party_id", party.get("id"));
            else{
            	deliveryOrder.set("notify_party_id", getPara("notify_id"));
			}
			if(!"".equals(businessStamp) && businessStamp != null)
				deliveryOrder.set("business_stamp", businessStamp);
			if(!"".equals(clientOrderStamp) && clientOrderStamp != null)
				deliveryOrder.set("client_order_stamp", clientOrderStamp);
			if(!"".equals(orderDeliveryStamp) && orderDeliveryStamp != null)
				deliveryOrder.set("order_delivery_stamp", orderDeliveryStamp);
			
			deliveryOrder.update();
		}
		renderJson(deliveryOrder);
	}
	
	

	// 保存运输里程碑
	private void saveDeliveryOrderMilestone(DeliveryOrder deliveryOrder) {
		DeliveryOrderMilestone deliveryOrderMilestone = new DeliveryOrderMilestone();
		deliveryOrderMilestone.set("status", "新建");
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		deliveryOrderMilestone.set("create_by", users.get(0).get("id"));
		deliveryOrderMilestone.set("location", "");
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		deliveryOrderMilestone.set("create_stamp", sqlDate);
		deliveryOrderMilestone.set("delivery_id", deliveryOrder.get("id"));
		deliveryOrderMilestone.save();
	}

	/*
	 * // 运输单ATM序列号 public void serialNo() { String id = getPara("id");
	 * System.out.println(id); List<Record> transferOrders = Db .find(
	 * "SELECT serial_no  FROM transfer_order_item_detail where item_name = 'ATM' and ORDER_ID  = "
	 * + id); renderJson(transferOrders); }
	 */

	// 发车确认
	public void departureConfirmation() {
		/*
		String warehouseId = getPara("warehouseId");
		String customerId = getPara("customerId");
		String cargoNature = getPara("cargoNature");
		String[] transferItemId =  getPara("transferItemIds").split(",");
		String[] productId =  getPara("productIds").split(",");
		String[] shippingNumber =  getPara("shippingNumbers").split(",");
		*/
		Long delivery_id = Long.parseLong(getPara("deliveryid"));
		System.out.println(delivery_id);
		DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(delivery_id);
		deliveryOrder.set("status", "配送在途");
		deliveryOrder.update();
		
		/*//货品属性：一站式普通货品，还有普通货品配送没做
		if("cargo".equals(cargoNature)){
			for (int i = 0; i < productId.length; i++) {
				//修改实际库存
				InventoryItem item = InventoryItem.dao.findFirst("select * from inventory_item ii where ii.warehouse_id = '" + warehouseId + "' and ii.product_id = '" + productId[i] + "' and ii.party_id = '" + customerId + "';");
				item.set("total_quantity", item.getDouble("total_quantity") - Double.parseDouble(shippingNumber[i])).update();
				//修改运输单已完成数量
				TransferOrderItem transferOrderItem = TransferOrderItem.dao.findById(transferItemId[i]);
				double outCompleteAmount = transferOrderItem.getDouble("complete_amount")==null?0:transferOrderItem.getDouble("complete_amount");
				double newCompleteAmount = transferOrderItem.getDouble("amount") - (outCompleteAmount + Double.parseDouble(shippingNumber[i]));
				transferOrderItem.set("complete_amount", newCompleteAmount).save();
			}
		}*/
		
		Map<String, Object> map = new HashMap<String, Object>();
		DeliveryOrderMilestone deliveryOrderMilestone = new DeliveryOrderMilestone();
		deliveryOrderMilestone.set("status", "已发车");
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		deliveryOrderMilestone.set("create_by", users.get(0).get("id"));
		deliveryOrderMilestone.set("location", "");
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		deliveryOrderMilestone.set("create_stamp", sqlDate);
		deliveryOrderMilestone.set("order_id", getPara("order_id"));
		deliveryOrderMilestone.save();
		map.put("transferOrderMilestone", deliveryOrderMilestone);
		UserLogin userLogin = UserLogin.dao.findById(deliveryOrderMilestone
				.get("create_by"));
		String username = userLogin.get("user_name");
		map.put("username", username);
		renderJson(map);

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
			List<DeliveryOrderMilestone> transferOrderMilestones = DeliveryOrderMilestone.dao
					.find("select * from delivery_order_milestone where delivery_id="
							+ departOrderId);
			for (DeliveryOrderMilestone transferOrderMilestone : transferOrderMilestones) {
				UserLogin userLogin = UserLogin.dao
						.findById(transferOrderMilestone.get("create_by"));
				String username = userLogin.get("c_name");
				if(username==null||"".equals(username)){
	            	username=userLogin.get("user_name");
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
			DeliveryOrder deliveryOrder = DeliveryOrder.dao
					.findById(milestoneDepartId);
			DeliveryOrderMilestone deliveryOrderMilestone = new DeliveryOrderMilestone();
			/*取值*/
			String status = getPara("status");
			String location = getPara("location");
			/*String arrivalTime =;*/
			String deliveryException =getPara("deliveryException");
			String completeType =getPara("complete");//完成情况
			String completeRemark =getPara("completeRemark");
			String sealType =getPara("seal");//是否盖章
			String sealRemark =getPara("remark");
			/*取值*/
			
			
			if(getPara("arrival_filter")!=null&&!"".equalsIgnoreCase(getPara("arrival_filter"))){
				deliveryOrderMilestone.set("arrival_time",getPara("arrival_filter"));
			}else{
				Date date = new Date();
			
				deliveryOrderMilestone.set("arrival_time",date);
			}
			
			deliveryOrderMilestone.set("unusual_handle", deliveryException);
			if(completeType.equalsIgnoreCase("yes")){
				deliveryOrderMilestone.set("performance", completeRemark);
				deliveryOrderMilestone.set("unfinished_reason", "");
			}else{
				deliveryOrderMilestone.set("performance", "");
				deliveryOrderMilestone.set("unfinished_reason", completeRemark);
			}
			if(sealType.equalsIgnoreCase("yes")){
				deliveryOrderMilestone.set("finished_seral", "是");
				deliveryOrderMilestone.set("unseral_reason", "");
			}else{
				deliveryOrderMilestone.set("finished_seral", "");
				deliveryOrderMilestone.set("unseral_reason", sealRemark);
			}
			
						
			if (!status.isEmpty()) {
				deliveryOrderMilestone.set("status", status);
				deliveryOrder.set("status", status);
			} else {
				deliveryOrderMilestone.set("status", "在途");
				deliveryOrder.set("status", "在途");
			}
			deliveryOrder.update();
			if (!location.isEmpty()) {
				deliveryOrderMilestone.set("location", location);
			} else {
				deliveryOrderMilestone.set("location", "");
			}
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");

			deliveryOrderMilestone.set("create_by", users.get(0).get("id"));

			java.util.Date utilDate = new java.util.Date();
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(
					utilDate.getTime());
			deliveryOrderMilestone.set("create_stamp", sqlDate);
			deliveryOrderMilestone.set("delivery_id", milestoneDepartId);
			deliveryOrderMilestone.save();

			map.put("transferOrderMilestone", deliveryOrderMilestone);
			UserLogin userLogin = UserLogin.dao.findById(deliveryOrderMilestone
					.get("create_by"));
			String username = userLogin.get("c_name");
			if(username==null||"".equals(username)){
            	username=userLogin.get("user_name");
            }
			map.put("username", username);
		}
		renderJson(map);
	}

	// 同步运输单状态里程碑
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
			if (!status.isEmpty()) {
				transferOrderMilestone.set("status", status);
				tr.set("status", status);
			} else {
				transferOrderMilestone.set("status", "在途");
				tr.set("status", "在途");
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
	
	//配送单状态
    public void findDeliveryOrderType(){
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        String orderNo = getPara("pointInTime");
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");  
	    Calendar pastDay = Calendar.getInstance(); 
	    if("pastOneDay".equals(orderNo))
	    	pastDay.add(Calendar.DAY_OF_WEEK, -1);
	    else if("pastSevenDay".equals(orderNo))
	    	pastDay.add(Calendar.DAY_OF_WEEK, -7);
	    else
	    	pastDay.add(Calendar.DAY_OF_WEEK, -30);
	    String beginTime = df.format(pastDay.getTime());
	    String endTime = simpleDate.format(Calendar.getInstance().getTime());
	    
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(0) total from delivery_order d left join warehouse w on w.id = d.from_warehouse_id where w.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
				+ " and d.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
				+ " and d.create_stamp between '" + beginTime + "' and '" + endTime + "'";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select d.id, d.order_no,"
        		+ " ifnull((select name from location where code = d.route_from ), '' ) route_from,"
        		+ " ifnull((select name from location where code = d.route_to ), '' ) route_to,"
        		+ " (select status from delivery_order_milestone where delivery_id = d.id order by id desc limit 0,1) status,"
        		+ " (select create_stamp from delivery_order_milestone where delivery_id = d.id order by id desc limit 0,1) create_stamp"
        		+ " from delivery_order d left join warehouse w on w.id = d.from_warehouse_id  where w.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
				+ " and d.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
				+ " and d.create_stamp between '" + beginTime + "' and '" + endTime + "' order by d.id desc " + sLimit;
        List<Record> deliveryOrderItems = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", deliveryOrderItems);
        renderJson(Map); 
    }
    // 查找RDC	
    public void searchAllRDC() {
    	String inputStr = getPara("rdc");
    	String sql ="";
    	 ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    	Long parentID = pom.getParentOfficeId();
    	if(inputStr!=null){
    		sql = "select * from office where  office_name like '%"+inputStr+"%' and (id = " + parentID + " or belong_office = " + parentID +")";
    	}else{
    		sql= "select * from office where (id = " + parentID + " or belong_office = " + parentID +")";
    	}
        List<Office> office = Office.dao.find(sql);
        renderJson(office);
    }
    public void searchPartRDC() {
    	String inputStr = getPara("rdc");
    	String sql ="";
    	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    	Long parentID = pom.getParentOfficeId();
    	/*"select o.id,o.office_name,o.is_stop from office o where o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')"*/
    	if(inputStr!=null){
    		sql = "select * from office where  office_name like '%"+inputStr+"%' and id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')";
    	}else{
    		sql= "select * from office where id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')";
    	}
        List<Office> office = Office.dao.find(sql);
        renderJson(office);
    }
    
    // 查找序列号
    public void searchAllOrderStatue() {
    	String inputStr = getPara("orderStatue");
    	String warehouse = getPara("warehouse");
    	String customerName = getPara("customerName");
    	
    	String sql ="";
    	if(inputStr != null && !"".equals(inputStr)){
    		sql= " SELECT t1.serial_no, t1.item_no, t1.id AS tid, t1.notify_party_company AS company, t2.*, w.warehouse_name, c.abbr"
        			+ " FROM transfer_order_item_detail t1"
        			+ " LEFT JOIN transfer_order t2 ON t1.order_id = t2.id"
        			+ " LEFT JOIN warehouse w ON t2.warehouse_id = w.id"
        			+ " LEFT JOIN party p ON t2.customer_id = p.id"
        			+ " LEFT JOIN contact c ON p.contact_id = c.id"
        			+ " LEFT JOIN party p2 ON t1.notify_party_id = p2.id"
        			+ " LEFT JOIN contact c2 ON p2.contact_id = c2.id"
        			+ " LEFT JOIN office o ON o.id = t2.office_id"
        			+ " WHERE delivery_id IS NULL"
        			+ " AND t2.cargo_nature = 'ATM'"
        			+ " AND ( t1.is_delivered IS NULL OR t1.is_delivered = FALSE )"
        			+ " AND t1.depart_id IS NOT NULL"
        			+ " AND w.warehouse_name LIKE '%"+warehouse+"%'"
        			+ " AND c.abbr LIKE '%"+customerName+"%'"
        			+ " and t1.serial_no LIKE '%"+inputStr+"%'";
    	}else{
    		sql= " SELECT t1.serial_no, t1.item_no, t1.id AS tid, t1.notify_party_company AS company, t2.*, w.warehouse_name, c.abbr"
    			+ " FROM transfer_order_item_detail t1"
    			+ " LEFT JOIN transfer_order t2 ON t1.order_id = t2.id"
    			+ " LEFT JOIN warehouse w ON t2.warehouse_id = w.id"
    			+ " LEFT JOIN party p ON t2.customer_id = p.id"
    			+ " LEFT JOIN contact c ON p.contact_id = c.id"
    			+ " LEFT JOIN party p2 ON t1.notify_party_id = p2.id"
    			+ " LEFT JOIN contact c2 ON p2.contact_id = c2.id"
    			+ " LEFT JOIN office o ON o.id = t2.office_id"
    			+ " WHERE delivery_id IS NULL"
    			+ " AND t2.cargo_nature = 'ATM'"
    			+ " AND ( t1.is_delivered IS NULL OR t1.is_delivered = FALSE )"
    			+ " AND t1.depart_id IS NOT NULL"
    			+ " AND w.warehouse_name LIKE '%"+warehouse+"%'"
    			+ " AND c.abbr LIKE '%"+customerName+"%'";
    	}
        List<TransferOrderItemDetail> TOIDetail = TransferOrderItemDetail.dao.find(sql);
        renderJson(TOIDetail);
    }
    
    // 查找仓库
    public void searchAllwarehouse() {
    	String inputStr = getPara("warehouseName");
    	String rdc = getPara("rdc");
    	String sql ="";
    	/*if(inputStr!=null){
    		sql = "select * from warehouse where warehouse_name like '%"+inputStr+"%'";
    	}else{
    		sql= "select * from warehouse";
    	}*/
    	
    	if(inputStr != null && rdc != null && !"".equals(inputStr) && !"".equals(rdc)){
    		sql = "select * from warehouse w left join office o on o.id = w.office_id where w.warehouse_name LIKE '%"+inputStr+"%' and o.id = "+rdc;
    	}else if(inputStr != null && !"".equals(inputStr)){
    		sql = "select * from warehouse where warehouse_name like '%"+inputStr+"%'";
    	}else if(rdc != null && !"".equals(rdc)){
    		sql = "select * from warehouse w left join office o on o.id = w.office_id where o.id = "+rdc;
    	}else{
    		sql= "select * from warehouse";
    	}
    	
    	
        List<Warehouse> warehouses = Warehouse.dao.find(sql);
        renderJson(warehouses);
    }
    public void searchPartWarehouse() {
    	String inputStr = getPara("warehouseName");
    	String rdc = getPara("rdc");
    	String sql ="";
    	if(inputStr!=null){
    		sql = "select w.* from warehouse w left join office o on w.office_id = o.id where w.warehouse_name like '%"+inputStr+"%' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')";
    	}else{
    		sql= "select w.* from warehouse w left join office o on w.office_id = o.id where o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')";
    	}
    	
    	
        List<Warehouse> warehouses = Warehouse.dao.find(sql);
        renderJson(warehouses);
    }
    
    public void orderListCargo(){
    	
    	//String warehouseId = getPara("warehouseId");
    	String transferItemIds = getPara("transferItemIds");
    	//String productIds = getPara("productIds");
    	//String customerId = getPara("customerId");
    	String pageIndex = getPara("sEcho");
    	
    	//String[] productId =  productIds.split(",");
    	String sqlTotal = "select count(0) total from transfer_order_item where id in (" + transferItemIds + ");";
        /*String sql = "select pro.id pid, pro.item_name, pro.item_no, pro.volume, pro.weight, c.abbr from inventory_item ii"
        		+ " left join product pro on ii.product_id = pro.id"
        		+ " left join warehouse w on ii.warehouse_id = w.id"
        		+ " left join party p on ii.party_id = p.id"
        		+ " left join contact c on p.contact_id = c.id"
        		+ " where w.id = '" + warehouseId + "' and p.id = '" + customerId + "' and pro.id in (" + productIds + ");";*/
        String sql = "select toi.*,c.abbr,tor.order_no from transfer_order_item toi "
        		+ " left join transfer_order tor on tor.id = toi.order_id"
        		+ " left join product pro on pro.id = toi.product_id"
        		+ " left join party p on p.id = tor.customer_id"
        		+ " left join contact c on c.id = p.contact_id"
        		+ " where toi.id in (" + transferItemIds + ");";
        Record rec = Db.findFirst(sqlTotal);
        List<Record> products = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", products);
        renderJson(Map);
    	
    }
    
    //获取一站式货品
    public void findTransferOrderItems(){
    	String warehouse1 = getPara("warehouse1");
    	String transferOrderNo = getPara("transferOrderNo");
    	String customerName1 = getPara("customerName1");
    	
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
    	String sqlTotal = "select count(0) total from transfer_order_item t1"
        		+ " left join transfer_order t2 on t1.order_id = t2.id"
        		+ " left join product pro on pro.id = t1.product_id"
        		+ " left join warehouse w on t2.warehouse_id = w.id"
        		+ " left join party p on t2.customer_id = p.id"
        		+ " left join contact c on p.contact_id = c.id"
        		+ " where t2.status in ('已入库','部分配送中') and t2.cargo_nature = 'cargo'"
        		+ " and w.warehouse_name LIKE '%" + warehouse1 + "%'"
        		+ " and c.abbr LIKE '%" + customerName1 + "%'"
        		+ " and t2.order_no like '%" + transferOrderNo + "%'"
        		+ " and (t1.amount != t1.complete_amount or t1.complete_amount is null) "
        		+ " order by t1.id desc";
    	
        String sql = "select t1.id as tid,w.id as wid,p.id as pid,pro.id as productId,ifnull(pro.item_no,t1.item_no) as item_no,ifnull(pro.item_name,t1.item_name) as item_name,t1.amount,t1.complete_amount,t2.order_no,t2.customer_order_no,t2.status,t2.cargo_nature,w.warehouse_name,c.abbr,"
        		+ " (select sum(product_number) from delivery_order_item  toi left join delivery_order dor on dor.id = toi.delivery_id "
        		+ " where toi.transfer_no like '%" + transferOrderNo + "%' and toi.product_id = t1.product_id and dor.status = '新建') quantity "
        		+ " from transfer_order_item t1"
        		+ " left join transfer_order t2 on t1.order_id = t2.id"
        		+ " left join product pro on pro.id = t1.product_id"
        		+ " left join warehouse w on t2.warehouse_id = w.id"
        		+ " left join party p on t2.customer_id = p.id"
        		+ " left join contact c on p.contact_id = c.id"
        		+ " where t2.status in ('已入库','部分配送中') and t2.cargo_nature = 'cargo'"
        		+ " and w.warehouse_name LIKE '%" + warehouse1 + "%'"
        		+ " and c.abbr LIKE '%" + customerName1 + "%'"
        		+ " and t2.order_no like '%" + transferOrderNo + "%'"
        		+ " and (t1.amount != t1.complete_amount or t1.complete_amount is null)"
        		+ " order by t1.id desc " + sLimit;
        
        Record rec = Db.findFirst(sqlTotal);
        List<Record> transferOrderItems = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", transferOrderItems);
        renderJson(Map); 
    }
    
    // 导入配送单
 	public void importDeliveryOrder() {
 		UploadFile uploadFile = getFile();
 		File file = uploadFile.getFile();
 		String fileName = file.getName();
 		logger.debug("文件名:" + file.getName() +",路径："+file.getPath());
 		Map<String,String> resultMap = new HashMap<String,String>();
  		try {
  			String[] title = null;
  			List<Map<String,String>> content = new ArrayList<Map<String,String>>();
  			if(fileName.endsWith(".xls")){
  				title = ReaderXLS.getXlsTitle(file);
  				content = ReaderXLS.getXlsContent(file);
  			}else if(fileName.endsWith(".xlsx")){
  				title = ReaderXlSX.getXlsTitle(file);
  				content = ReaderXlSX.getXlsContent(file);
  			}else{
  				resultMap.put("result", "false");
 				resultMap.put("cause", "导入失败，请选择正确的execl文件");
  			}
  			if(title != null && content.size() > 0){
 				DeliveryOrderExeclHandeln handeln = new DeliveryOrderExeclHandeln();
 				if(handeln.checkoutExeclTitle(title,"deliveryOrder")){
 					resultMap = handeln.importDeliveryOrder(content);
 				}else{
 					resultMap.put("result", "false");
 					resultMap.put("cause", "导入失败，execl标题列与系统默认execl标题列不一致");
 				}
  			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			resultMap.put("result", "false");
 			resultMap.put("cause", "导入失败，请选择正确的execl文件<br/>（建议使用Microsoft Office Execl软件操作数据）");
 			renderJson(resultMap);
 		}
  		logger.debug("result:" + resultMap.get("result") +",cause:"+resultMap.get("cause"));
  		
 		renderJson(resultMap);
 	}
 // 更新配送单
  	public void reviseDeliveryOrder() {
  		UploadFile uploadFile = getFile();
  		File file = uploadFile.getFile();
  		String fileName = file.getName();
  		logger.debug("文件名:" + file.getName() +",路径："+file.getPath());
  		Map<String,String> resultMap = new HashMap<String,String>();
   		try {
   			String[] title = null;
   			List<Map<String,String>> content = new ArrayList<Map<String,String>>();
   			if(fileName.endsWith(".xls")){
   				title = ReaderXLS.getXlsTitle(file);
   				content = ReaderXLS.getXlsContent(file);
   			}else if(fileName.endsWith(".xlsx")){
   				title = ReaderXlSX.getXlsTitle(file);
   				content = ReaderXlSX.getXlsContent(file);
   			}else{
   				resultMap.put("result", "false");
  				resultMap.put("cause", "导入失败，请选择正确的execl文件");
   			}
   			if(title != null && content.size() > 0){
  				DeliveryOrderExeclHandeln handeln = new DeliveryOrderExeclHandeln();
  				if(handeln.reviseExeclTitle(title,"reviseDeliveryOrder")){
  					resultMap = handeln.reviseDeliveryOrder(content);
  				}else{
  					resultMap.put("result", "false");
  					resultMap.put("cause", "导入失败，execl标题列与系统默认execl标题列不一致");
  				}
   			}
  		} catch (Exception e) {
  			e.printStackTrace();
  			resultMap.put("result", "false");
  			resultMap.put("cause", "导入失败，请选择正确的execl文件<br/>（建议使用Microsoft Office Execl软件操作数据）");
  			renderJson(resultMap);
  		}
   		logger.debug("result:" + resultMap.get("result") +",cause:"+resultMap.get("cause"));
   		
  		renderJson(resultMap);
  	}
 	
    //下载配送单导入模板
	public void downloadDeliveryOrderTemplate(){
		File file = new File(PathKit.getWebRootPath()+"/download/配送单导入模板.xlsx");
		renderFile(file);
	}
	//下载配送单更新模板
	public void reviseDownloadDeliveryOrderTemplate(){
		File file = new File(PathKit.getWebRootPath()+"/download/配送单更新模板.xlsx");
		renderFile(file);
	}
	
	
	

    // 列出客户公司名称
    public void searchCustomer() {
        String locationName = getPara("locationName");
        
        List<Record> locationList = Collections.EMPTY_LIST;
        if (locationName.trim().length() > 0) {
            locationList = Db
                    .find("select * from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER' and c.abbr like '%"+locationName+"%' and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')");

        } else {
            locationList = Db.find("select * from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER'  and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')");

        }
        renderJson(locationList);
    }

}
