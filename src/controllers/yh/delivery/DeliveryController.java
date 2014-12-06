package controllers.yh.delivery;

import interceptor.SetAttrLoginUserInterceptor;

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
import models.Party;
import models.TransferOrder;
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
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.OrderNoUtil;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class DeliveryController extends Controller {
	private Logger logger = Logger.getLogger(DeliveryController.class);
	// in config route已经将路径默认设置为/yh
	// me.add("/yh", controllers.yh.AppController.class, "/yh");
	Subject currentUser = SecurityUtils.getSubject();
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_LIST})
	public void index() {
		render("/yh/delivery/deliveryOrderList.html");
		
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_DOM_LIST})
	public void deliverOnTrip(){
		
		render("/yh/delivery/deliveryOrderStatus.html");
	}

	// 配送单list
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_LIST})
	public void deliveryList() {
		String orderNo_filter = getPara("orderNo_filter");
		String transfer_filter = getPara("transfer_filter");
		String status_filter = getPara("status_filter");
		String customer_filter = getPara("customer_filter");
		String sp_filter = getPara("sp_filter");
		String beginTime_filter = getPara("beginTime_filter");
		String endTime_filter = getPara("endTime_filter");
		String warehouse = getPara("warehouse");
		String serial_no = getPara("serial_no");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map transferOrderListMap = new HashMap();
		if (orderNo_filter == null && transfer_filter == null
				&& status_filter == null && customer_filter == null
				&& sp_filter == null && beginTime_filter == null
				&& endTime_filter == null) {
			String sqlTotal = "select count(1) total from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id";
			Record rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));

			String sql = "select d.*,c.abbr as customer,c2.company_name as c2,(select group_concat( distinct doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no,"
					+ " (select group_concat(trid.serial_no separator '\r\n') from delivery_order_item doi left join transfer_order_item_detail trid on trid.id = doi.transfer_item_detail_id where doi.delivery_id = d.id) as serial_no"
					+ " from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id order by d.create_stamp desc "
					+ sLimit;
			List<Record> transferOrders = Db.find(sql);

			transferOrderListMap.put("sEcho", pageIndex);
			transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
			transferOrderListMap.put("iTotalDisplayRecords",
					rec.getLong("total"));
			transferOrderListMap.put("aaData", transferOrders);
		} else {
			if (beginTime_filter == null || "".equals(beginTime_filter)) {
				beginTime_filter = "1-1-1";
			}
			if (endTime_filter == null || "".equals(endTime_filter)) {
				endTime_filter = "9999-12-31";
			}

			String sqlTotal = "select count(1) total from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join delivery_order_item dt2 on dt2.delivery_id = d.id "
					+ " left join transfer_order_item_detail trid on trid.id = dt2.transfer_item_detail_id "
					+ " where ifnull(d.order_no,'') like '%"
					+ orderNo_filter
					+ "%' and ifnull(d.status,'') like '%"
					+ status_filter
					+ "%' and ifnull(c.abbr,'') like '%"
					+ customer_filter
					+ "%' and ifnull(dt2.transfer_no,'') like '%"
					+ transfer_filter
					+ "%' and ifnull(c2.company_name,'') like'%"
					+ sp_filter
					+ "%' and ifnull(trid.serial_no,'') like'%"
					+ serial_no
					+ "%' and d.create_stamp between '"
					+ beginTime_filter
					+ "' and '" + endTime_filter + "'" ;
			Record rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));

			String sql = "select d.*,c.abbr as customer,c2.company_name as c2,(select group_concat( distinct doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no,"
					+ " (select group_concat(trid.serial_no separator '\r\n') from delivery_order_item doi left join transfer_order_item_detail trid on trid.id = doi.transfer_item_detail_id where doi.delivery_id = d.id) as serial_no"
					+ " from delivery_order d "
					+ " left join party p on d.customer_id = p.id "
					+ " left join contact c on p.contact_id = c.id "
					+ " left join party p2 on d.sp_id = p2.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join delivery_order_item dt2 on dt2.delivery_id = d.id "
					+ " left join transfer_order_item_detail trid on trid.id = dt2.transfer_item_detail_id "
					+ " where ifnull(d.order_no,'') like '%"
					+ orderNo_filter
					+ "%' and ifnull(d.status,'') like '%"
					+ status_filter
					+ "%' and ifnull(c.abbr,'') like '%"
					+ customer_filter
					+ "%' and ifnull(dt2.transfer_no,'') like '%"
					+ transfer_filter
					+ "%' and ifnull(c2.company_name,'') like'%"
					+ sp_filter
					+ "%' and ifnull(trid.serial_no,'') like'%"
					+ serial_no
					+ "%' and d.create_stamp between '"
					+ beginTime_filter
					+ "' and '" + endTime_filter + "' group by d.id " + sLimit;

			List<Record> transferOrders = Db.find(sql);

			transferOrderListMap.put("sEcho", pageIndex);
			transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
			transferOrderListMap.put("iTotalDisplayRecords",
					rec.getLong("total"));
			transferOrderListMap.put("aaData", transferOrders);
		}
		// 获取总条数

		renderJson(transferOrderListMap);
	}

	// 在途配送单list

	@RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_ADD_COST})
	public void deliveryMilestone() {

		String transferorderNo = getPara("transferorderNo");
		String deliveryNo = getPara("deliveryNo");
		String customer = getPara("customer");
		String sp = getPara("sp");
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
		

		String sql = "select d.*,"
				+ "c.abbr as customer,"
				+ "c2.company_name as c2,"
				+ "(select group_concat(distinct doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no, "
				+ "(select location from delivery_order_milestone dom where delivery_id = d.id order by id desc limit 0,1) location "
				+ " from delivery_order d "
				+ "left join party p on d.customer_id = p.id "
				+ "left join contact c on p.contact_id = c.id "
				+ "left join party p2 on d.sp_id = p2.id "
				+ "left join contact c2 on p2.contact_id = c2.id "
				+ "order by d.create_stamp desc" + sLimit;

		List<Record> depart = null;
		if (transferorderNo == null && deliveryNo == null && customer == null
				&& sp == null && beginTime == null && endTime == null) {
			sqlTotal ="select count(1) total from delivery_order";
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
					+ "(select group_concat(doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no, "
					+ "(select location from delivery_order_milestone dom where delivery_id = d.id order by id desc limit 0,1) location "
					+ "from delivery_order d "
					+ "left join party p on d.customer_id = p.id "
					+ "left join contact c on p.contact_id = c.id "
					+ "left join party p2 on d.sp_id = p2.id "
					+ "left join contact c2 on p2.contact_id = c2.id "
					+ "left join delivery_order_item dt2 on dt2.delivery_id = d.id "
					+ "where ifnull(d.order_no,'') like '%"
					+ deliveryNo
					+ "%' and ifnull(c.abbr,'') like '%"
					+ customer
					+ "%' and ifnull(dt2.transfer_no,'') like '%"
					+ transferorderNo
					+ "%' and ifnull(c2.company_name,'') like'%"
					+ sp
					+ "%' and d.create_stamp between '"
					+ beginTime
					+ "' and '"
					+ endTime + "') as delivery_view";
			String sql_seach = "select distinct d.*,"
					+ "c.abbr as customer,"
					+ "c2.company_name as c2,"
					+ "(select group_concat(doi.transfer_no separator '\r\n') from delivery_order_item doi where delivery_id = d.id) as transfer_order_no, "
					+ "(select location from delivery_order_milestone dom where delivery_id = d.id order by id desc limit 0,1) location "
					+ "from delivery_order d "
					+ "left join party p on d.customer_id = p.id "
					+ "left join contact c on p.contact_id = c.id "
					+ "left join party p2 on d.sp_id = p2.id "
					+ "left join contact c2 on p2.contact_id = c2.id "
					+ "left join delivery_order_item dt2 on dt2.delivery_id = d.id "
					+ "where ifnull(d.order_no,'') like '%"
					+ deliveryNo
					+ "%' and ifnull(c.abbr,'') like '%"
					+ customer
					+ "%' and ifnull(dt2.transfer_no,'') like '%"
					+ transferorderNo
					+ "%' and ifnull(c2.company_name,'') like'%"
					+ sp
					+ "%' and d.create_stamp between '"
					+ beginTime
					+ "' and '"
					+ endTime + "' " + sLimit;
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
			render("/yh/delivery/deliveryOrderSearchTransfer.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_UPDATE})
	public void edit() {
		String id = getPara("id");
		//System.out.println(id);
		DeliveryOrder tOrder = DeliveryOrder.dao.findById(id);
		setAttr("cargoNature", tOrder.get("cargo_nature"));
		setAttr("deliveryOrder", tOrder);
		List<Record> serIdList = Db
				.find("select * from delivery_order_item where delivery_id ="
						+ id);

		// 运输单信息
		if (serIdList.size() > 0) {
			if (serIdList.get(0).get("transfer_item_detail_id") == null) {
				TransferOrder transferOrder = TransferOrder.dao
						.findById(serIdList.get(0).get("transfer_order_id"));
				setAttr("transferOrder", transferOrder);
			}
		}

		/*
		 * try { serIdList.get(0).get("transfer_order_id"); } catch (Exception
		 * e) { System.out.println(e); }
		 */
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

		// 客户信息
		Party customerContact = Party.dao
				.findFirst("select *,p.id as customerId from party p,contact c where p.id ='"
						+ tOrder.get("customer_id")
						+ "'and p.contact_id = c.id");

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
		}

		setAttr("deliveryId", tOrder);
		setAttr("customer", customerContact);

		setAttr("notifyParty", notifyPartyContact);
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
		String id = getPara("id");
		String list = this.getPara("localArr");
		System.out.println(list);
		// String ser = getPara("ser");
		/*
		 * String[] ser; ser = getParaValues("ser");
		 */

		/*
		 * if (!"".equals(ser)) { List<DeliveryOrderItem> itemsId =
		 * DeliveryOrderItem.dao
		 * .find("select id from delivery_ORDER_ITEM where SERIAL_NO='" + ser +
		 * "'"); DeliveryOrderItem itemsIds = itemsId.get(0);
		 * setAttr("itemsIds", itemsIds); }
		 */

		if (id != null) {
			List<Contact> customers = Contact.dao
					.find("select *,p.id as customerId from contact c,party p,transfer_order t where p.contact_id=c.id and t.customer_id = p.id and t.id ="
							+ id + "");
			Contact customer = customers.get(0);
			setAttr("customer", customer);
		}

		TransferOrder tOrder = TransferOrder.dao.findById(id);
		// setAttr("ser", ser);

		TransferOrder notity = TransferOrder.dao
				.findFirst("select c.*,p.id as pid,c.id as contactId from transfer_order t "
						+ "left join party p on p.id =t.notify_party_id "
						+ "left join contact c on p.contact_id =c.id "
						+ "where t.id='" + id + "'");

		// 仓库code
		Warehouse warehouse = Warehouse.dao
				.findById(tOrder.get("warehouse_id"));
		System.out.println(warehouse);
		setAttr("warehouse", warehouse);
		setAttr("transferId", id);
		setAttr("deliveryOrder", tOrder);
		setAttr("localArr3", list);
		setAttr("notifyParty", notity);
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
		String cargoNature = getPara("cargoNature");
		Party party = Party.dao
				.findFirst("select *,p.id as customerId from party p left join contact c on p.contact_id=c.id where p.id ='"
						+ cusId + "'");
		setAttr("localArr", list);
		setAttr("localArr2", list2);
		setAttr("localArr3", list3);
		setAttr("customer", party);

		String sql="select t.receiving_unit as company,c1.* ,td.notify_party_phone as phone,"
				+ " td.notify_party_name as contact_person,td.notify_party_company as address,t.route_from "
				+ " from transfer_order_item_detail td "
				+ " left join transfer_order t on t.id =td.order_id "
				+ " left join contact c on t.notify_party_id= c.id "
				+ " left join party p on p.id = td.notify_party_id "
				+ " left join contact c1 on p.contact_id =c1.id  where td.id in(" + list2 + ")";
		TransferOrderItemDetail notify = TransferOrderItemDetail.dao
				.findFirst(sql);
		// 选取的配送货品的仓库必须一致
		TransferOrder tOrder = TransferOrder.dao
				.findFirst("select warehouse_id from transfer_order where id in("
						+ list + ")");
		
		String routeFrom = notify.get("route_from");
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
		
		Warehouse warehouse = Warehouse.dao
				.findById(tOrder.get("warehouse_id"));
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
		
		String sql = "select ii.total_quantity, ii.product_id, ii.party_id , pro.*, w.warehouse_name, w.id as warehouse_id, c.abbr, ii.party_id as customer_id from inventory_item ii "
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
		Map transferOrderListMap = new HashMap();
		if(customerName==null&&warehouse==null){
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
		/*String sqlTotal = "select count(1) total from transfer_order_item_detail t1 "
				+ "left join transfer_order t2 on t1.order_id=t2.id "
				+ "where t2.status='已入库' and t2.cargo_nature='ATM' and (t1.is_delivered is null or t1.is_delivered=FALSE)";*/
		String sqlTotal="select  count(1) total from transfer_order_item_detail t1 "
					+ "left join transfer_order t2 on t1.order_id=t2.id "
					+ "left join warehouse w on t2.warehouse_id = w.id "
					+ "left join party p on t2.customer_id = p.id "
					+ "left join party p2 on t1.notify_party_id = p2.id "
					+ "left join contact c2 on p2.contact_id = c2.id "
					+ "left join contact c on p.contact_id = c.id "
					+ "where t2.status='已入库' and t2.cargo_nature='ATM' and (t1.is_delivered is null or t1.is_delivered=FALSE) and t1.delivery_id is null";
		
		String sql="";
		if (deliveryOrderNo == null && customerName == null
				&& orderStatue == null && warehouse == null&&code==null) {
			sql= "select  t1.serial_no,t1.item_no,t1.id as tid,t1.notify_party_company as company,t2.*,w.warehouse_name,c.abbr,c2.address as Naddress from transfer_order_item_detail t1 "
					+ "left join transfer_order t2 on t1.order_id=t2.id "
					+ "left join warehouse w on t2.warehouse_id = w.id "
					+ "left join party p on t2.customer_id = p.id "
					+ "left join party p2 on t1.notify_party_id = p2.id "
					+ "left join contact c2 on p2.contact_id = c2.id "
					+ "left join contact c on p.contact_id = c.id "
					+ "where t2.status='已入库' and t2.cargo_nature='ATM' and (t1.is_delivered is null or t1.is_delivered=false) and t1.delivery_id is null order by t1.id desc "
					+ sLimit;
			
		} else {
			 sql ="select  t1.serial_no,t1.item_no, t1.id as tid,t1.notify_party_company as company, t2.*,w.warehouse_name,c.abbr from transfer_order_item_detail t1 "
					+ "left join transfer_order t2 on t1.order_id=t2.id "
					+ "left join warehouse w on t2.warehouse_id = w.id "
					+ "left join party p on t2.customer_id = p.id "
					+ "left join contact c on p.contact_id = c.id "
					+ "left join party p2 on t1.notify_party_id = p2.id "
					+ "left join contact c2 on p2.contact_id = c2.id "
					+ "where t2.status='已入库' and t2.cargo_nature='ATM' and (t1.is_delivered is null or t1.is_delivered=false) and t1.delivery_id is null ";
			if(code!=""&&code!=null){
				sqlTotal =sqlTotal+" and serial_no like '%"+code+"%'";
				sql =sql +" and serial_no like '%"+code+"%'";
			}
			if(deliveryOrderNo!=""&&deliveryOrderNo!=null){
				sqlTotal =sqlTotal+" and ifnull(t2.order_no,'') like '%"+ deliveryOrderNo+ "%'";
				sql =sql+" and ifnull(t2.order_no,'') like '%"+ deliveryOrderNo+ "%'";
			}
			
			if(warehouse!=""&&customerName!=""&&warehouse!=null&&customerName!=null){
				sqlTotal = sqlTotal +" and w.warehouse_name like '%"
						+ warehouse
						+ "%' and c.abbr like '%"
						+ customerName
						+ "%'";
				sql= sql+" and w.warehouse_name like '%"
					+ warehouse
					+ "%' and c.abbr like '%"
					+ customerName
					+ "%' order by t1.id desc";
			}
			if(orderStatue!=""&&orderStatue!=null){
				sqlTotal =sqlTotal+"and ifnull(t2.status,'') like '%"
						+ orderStatue
						+ "%'";
				sql = sql+"and ifnull(t2.status,'') like '%"
						+ orderStatue
						+ "%' order by t1.id desc";
			}
			sql = sql + sLimit;
		
		}
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		List<Record> transferOrders = Db.find(sql);

		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords",
				rec.getLong("total"));
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
		List<Record> locationList = Collections.EMPTY_LIST;
		String sql = "";
		if(input!=null&&input!=""){
			sql= "select p.id pid,p.*, c.*,c.id cid from	party p left join contact c on c.id = p.contact_id where sp_type = 'delivery' and c.abbr like '%"+input+"%'";
		}else{
			sql= "select p.id pid,p.*, c.*,c.id cid from	party p left join contact c on c.id = p.contact_id where sp_type = 'delivery'";
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

	// 配送单货品ATMlist
	public void orderList2() {
		String idlist = getPara("localArr");
		String idlist2 = getPara("localArr2");
		if (idlist == null || idlist.equals("")) {
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

	public void deliverySave() {
        String car_type = getPara("car_type");// 供应商计费类型, 如果是整车，需要知道整车类型
        String ltlPriceType = getPara("ltlUnitType");//如果是零担，需要知道零担计费类型：按体积，按重量
		String sql = "select * from delivery_order order by id desc limit 0,1";
		String orderNo = OrderNoUtil.getOrderNo(sql,"PS");
		String deliveryid = getPara("delivery_id");
		DeliveryOrder deliveryOrder = null;
		String notifyId = getPara("notify_id");
		String spId = getPara("sp_id");
		String cargoNature = getPara("cargoNature");
		String idlist3 = getPara("localArr");
		String idlist5 = getPara("localArr3");
		String businessStamp = getPara("business_stamp");
		String clientOrderStamp = getPara("client_order_stamp");
		String orderDeliveryStamp  = getPara("order_delivery_stamp");

		String[] idlist = getPara("localArr").split(",");
		String[] idlist2 = getPara("localArr2").split(",");
		String[] idlist4 = getPara("localArr3").split(",");

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
					.set("mobile", getPara("notify_phone")).update();
		}

		if (deliveryid == null || "".equals(deliveryid)) {

			deliveryOrder.set("order_no", orderNo)
					.set("customer_id", getPara("customer_id"))
					.set("sp_id", getPara("sp_id"))
					.set("notify_party_id", party.get("id"))
					.set("create_stamp", createDate).set("status", "新建")
					.set("route_to", getPara("route_to"))
					.set("route_from", getPara("route_from"))
					.set("pricetype", getPara("chargeType"))
					.set("from_warehouse_id", getPara("warehouse_id"))
					.set("cargo_nature", cargoNature)
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
			
			deliveryOrder.save();

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
					deliveryOrderItem.set("delivery_id",
							deliveryOrder.get("id")).set("transfer_order_id",
							idlist[i]);
					deliveryOrderItem
							.set("transfer_item_detail_id", idlist2[i]);
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
				//transferOrderItemDetail.set("is_delivered", "已创建");
				transferOrderItemDetail.update();
			}
			saveDeliveryOrderMilestone(deliveryOrder);
		} else {

			deliveryOrder.set("sp_id", getPara("cid"))
					.set("notify_party_id", getPara("notify_id"))
					.set("Customer_id", getPara("customer_id"))
					.set("id", deliveryid).set("route_to", getPara("route_to"))
					.set("route_from", getPara("route_from"))
					.set("priceType", getPara("chargeType"))
					.set("client_requirement", getPara("client_requirement"))
					.set("ltl_price_type", ltlPriceType).set("car_type", car_type)
					.set("customer_delivery_no", getPara("customerDelveryNo"));
			
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
	private void saveDeliveryOrderMilestone(DeliveryOrder transferOrder) {
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
		deliveryOrderMilestone.set("delivery_id", transferOrder.get("id"));
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
		Long delivery_id = Long.parseLong(getPara("deliveryid"));
		System.out.println(delivery_id);
		DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(delivery_id);
		deliveryOrder.set("status", "配送在途");
		deliveryOrder.update();

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
				String username = userLogin.get("user_name");
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
			String username = userLogin.get("user_name");
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
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(0) total from delivery_order";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select d.order_no,"
        		+ " ifnull((select name from location where code = d.route_from ), '' ) route_from,"
        		+ " ifnull((select name from location where code = d.route_to ), '' ) route_to,"
        		+ " (select status from delivery_order_milestone where delivery_id = d.id order by id desc limit 0,1) status,"
        		+ " (select create_stamp from delivery_order_milestone where delivery_id = d.id order by id desc limit 0,1) create_stamp"
        		+ " from delivery_order d order by d.id desc " + sLimit;
        List<Record> deliveryOrderItems = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", deliveryOrderItems);
        renderJson(Map); 
    }
	
	
}
