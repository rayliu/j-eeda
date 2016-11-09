package controllers.yh.order;

import interceptor.SetAttrLoginUserInterceptor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartTransferOrder;
import models.FinItem;
import models.Location;
import models.Office;
import models.ParentOfficeModel;
import models.Party;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.UserOffice;
import models.Warehouse;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.yh.LoginUserController;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;
import controllers.yh.util.ReaderXLS;
import controllers.yh.util.ReaderXlSX;
import controllers.yh.util.getCustomFile;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TransferOrderController extends Controller {

	private Logger logger = Logger.getLogger(TransferOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {

		Map<String, String> customizeField = getCustomFile.getInstance()
				.getCustomizeFile(this);
		setAttr("customizeField", customizeField);

		List<Record> offices = Db
				.find("select o.id,o.office_name,o.is_stop from office o where o.id in (select office_id from user_office where user_name='"
						+ currentUser.getPrincipal() + "')");
		setAttr("userOffices", offices);

		render("/yh/transferOrder/transferOrderList.html");
	}

	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void list() {
		Map transferOrderListMap = null;
		String orderNo = getPara("orderNo").trim() ;
		String status = getPara("status").trim();
		String address = getPara("address").trim();
		String customer = getPara("customer").trim();
		String sp = getPara("sp").trim();
		String officeName = getPara("officeName").trim();
		String beginTime = getPara("beginTime").trim();
		String endTime = getPara("endTime").trim();

		String order_type = getPara("order_type").trim();
		String operation_type = getPara("operation_type").trim();
		String plantime = getPara("plantime").trim();
		String arrivarltime = getPara("arrivarltime").trim();
		String customer_order_no = getPara("customer_order_no").trim();
		String to_route = getPara("to_route").trim();

		if (orderNo == null && status == null && address == null
				&& customer == null && sp == null && beginTime == null
				&& endTime == null && order_type == null && plantime == null
				&& arrivarltime == null && customer_order_no == null
				&& operation_type == null && to_route == null) {
			String sLimit = "";
			String pageIndex = getPara("sEcho");
			if (getPara("iDisplayStart") != null
					&& getPara("iDisplayLength") != null) {
				sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
						+ getPara("iDisplayLength");
			}

			String sqlTotal = "select count(1) total from transfer_order t "
					+ " where t.status!='手动删除' and t.order_type != 'cargoReturnOrder' and t.office_id in(select office_id from user_office where user_name='"
					+ currentUser.getPrincipal()
					+ "') "
					+ " and t.customer_id in (select customer_id from user_customer where user_name='"
					+ currentUser.getPrincipal() + "')";
			Record rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));

			String sql = "select t.id,t.order_no,t.customer_order_no,t.status,t.cargo_nature,t.operation_type,t.arrival_mode,t.pickup_mode,t.create_stamp,"
					+ " t.planning_time,t.arrival_time, t.address,t.order_type, c1.abbr cname,c2.abbr spname,o.office_name oname,t.remark,"
					+ " (select sum(toi.amount) from transfer_order_item toi where toi.order_id=t.id) amount,"
					+ " round((select sum(ifnull(toi.volume,0)) from transfer_order_item toi where toi.order_id = t.id),2) volume, "
					+ " round((select sum(ifnull(toi.sum_weight,0)) from transfer_order_item toi where toi.order_id = t.id),2) weight, "
					+ " (select sum(toid.pieces) from transfer_order_item_detail toid where toid.order_id=t.id) pieces, "
					+ " ifnull((select name from location where code = t.route_from),'') route_from,"
					+ " ifnull((select name from location where code = t.route_to),'') route_to,"
					+ " ul.user_name user_name "
					+ " from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join office o on t.office_id = o.id "
					+ " left join user_login ul on ul.id=t.create_by "
					+ " where t.status!='手动删除' and t.order_type != 'cargoReturnOrder' and (t.office_id in (select office_id from user_office where user_name='"
					+ currentUser.getPrincipal()
					+ "')) "
					+ " and t.customer_id in (select customer_id from user_customer where user_name='"
					+ currentUser.getPrincipal()
					+ "')"
					+ " order by t.status !='新建',t.status !='已发车',t.status !='在途',t.status !='已入货场',t.status !='已入库',t.status !='已签收' desc,t.planning_time desc"
					+ sLimit;

			List<Record> transferOrders = Db.find(sql);

			transferOrderListMap = new HashMap();
			transferOrderListMap.put("sEcho", pageIndex);
			transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
			transferOrderListMap.put("iTotalDisplayRecords",
					rec.getLong("total"));

			transferOrderListMap.put("aaData", transferOrders);
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}else{
				endTime = endTime+" 23:59:59";
			}

			if (plantime == null || "".equals(plantime)) {
				plantime = "1-1-1";
			}
			if (arrivarltime == null || "".equals(arrivarltime)) {
				arrivarltime = "9999-12-31";
			}else{
				arrivarltime = arrivarltime+" 23:59:59";
			}

			String sLimit = "";
			String pageIndex = getPara("sEcho");
			if (getPara("iDisplayStart") != null
					&& getPara("iDisplayLength") != null) {
				sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
						+ getPara("iDisplayLength");
			}

			String sqlTotal = "select count(*) total "
					+ " from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join office o on t.office_id = o.id "
					+ " left join user_login ul on ul.id=t.create_by "
					+ " left join location l on l.CODE = t.route_to"
					+ " where t.status!='手动删除' and t.order_type != 'cargoReturnOrder' "
					+ " and t.order_no like '%"
					+ orderNo
					+ "%' and t.status like '%"
					+ status
					+ "%' and l.name like '%"
					+ to_route
					+ "%' and t.address like '%"
					+ address
					+ "%' and c1.abbr like '%"
					+ customer
					+ "%' and ifnull(c2.abbr,'') like '%"
					+ sp
					+ "%' and ifnull(o.office_name,'')  like '%"
					+ officeName
					+ "%' and ifnull(t.order_type,'') like '%"
					+ order_type
					+ "%' and ifnull(t.customer_order_no,'') like '%"
					+ customer_order_no
					+ "%' and ifnull(t.planning_time,'') between '"
					+ plantime
					+ "' and '"
					+ arrivarltime
					+ "' "
					// + "%' and ifnull(t.planning_time,'') like '%" + plantime
					// + "%' and ifnull(t.arrival_time,'') like '%" +
					// arrivarltime
					+ " and t.create_stamp between '"
					+ beginTime
					+ "' and '"
					+ endTime
					+ "' "
					+ " and t.operation_type like '%"
					+ operation_type
					+ "%'"
					+ " and t.office_id in (select office_id from user_office where user_name='"
					+ currentUser.getPrincipal()
					+ "') "
					+ " and t.customer_id in (select customer_id from user_customer where user_name='"
					+ currentUser.getPrincipal() + "')";
			Record rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));

			String sql = "select t.id,t.order_no,t.customer_order_no,t.status,t.cargo_nature,t.operation_type,t.arrival_mode,t.pickup_mode,t.create_stamp,"
					+ " t.planning_time,t.arrival_time, t.address,t.order_type, c1.abbr cname,c2.abbr spname,o.office_name oname,t.remark,"
					+ " (select sum(toi.amount) from transfer_order_item toi where toi.order_id=t.id) amount,"
					+ " round((select sum(ifnull(toi.volume,0)) from transfer_order_item toi where toi.order_id = t.id),2) volume, "
					+ " round((select sum(ifnull(toi.sum_weight,0)) from transfer_order_item toi where toi.order_id = t.id),2) weight, "
					+ " (select sum(toid.pieces) from transfer_order_item_detail toid where toid.order_id=t.id) pieces, "
					+ " ifnull(l_f.name,'') route_from,"
					+ " ifnull(l_t.name,'') route_to, "
					+ " ul.user_name user_name"
					+ " from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join office o on t.office_id = o.id "
					+ " left join user_login ul on ul.id=t.create_by "
					+ " left join location l_f on l_f.CODE = t.route_from "
					+ " left join location l_t on l_t.CODE = t.route_to "
					+ " where t.status!='手动删除' and t.order_type != 'cargoReturnOrder' "
					+ " and t.order_no like '%"
					+ orderNo
					+ "%' and t.status like '%"
					+ status
					+ "%' and ifnull(l_t.name,'') like '%"
					+ to_route
					+ "%' and t.address like '%"
					+ address
					+ "%' and c1.abbr like '%"
					+ customer
					+ "%' and ifnull(c2.abbr,'') like '%"
					+ sp
					+ "%' and ifnull(o.office_name,'')  like '%"
					+ officeName
					+ "%' and ifnull(t.order_type,'') like '%"
					+ order_type
					+ "%' and ifnull(t.customer_order_no,'') like '%"
					+ customer_order_no
					+ "%' and ifnull(t.planning_time,'') between '"
					+ plantime
					+ "' and '"
					+ arrivarltime
					+ "' "
					// + "%' and ifnull(t.planning_time,'') like '%" + plantime
					// + "%' and ifnull(t.arrival_time,'') like '%" +
					// arrivarltime
					+ " and t.create_stamp between '"
					+ beginTime
					+ "' and '"
					+ endTime
					+ "' "
					+ " and t.operation_type like '%"
					+ operation_type
					+ "%'"
					+ " and t.office_id in (select office_id from user_office where user_name='"
					+ currentUser.getPrincipal()
					+ "') "
					+ " and t.customer_id in (select customer_id from user_customer where user_name='"
					+ currentUser.getPrincipal()
					+ "') order by t.status !='新建',t.status !='已发车',t.status !='在途',t.status !='已入货场',t.status !='已入库',t.status !='已签收' desc, t.planning_time desc"
					+ sLimit;

			List<Record> transferOrders = Db.find(sql);

			transferOrderListMap = new HashMap();
			transferOrderListMap.put("sEcho", pageIndex);
			transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
			transferOrderListMap.put("iTotalDisplayRecords",
					rec.getLong("total"));

			transferOrderListMap.put("aaData", transferOrders);
		}

		renderJson(transferOrderListMap);
	}

	public void add() {
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
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);
		setAttr("status", "新建");
		// 根据用户的默认网点确定默认的运作网点
		UserOffice uo = UserOffice.dao
				.findFirst(
						"select * from user_office where user_name = ? and is_main = 1",
						userLogin.get("user_name"));
		if (uo != null) {
			transferOrder.set("office_id", uo.get("office_id"));
		}/*
		 * else{ transferOrder.set("office_id", null); }
		 */

		setAttr("transferOrder", transferOrder);

		Map<String, String> customizeField = getCustomFile.getInstance()
				.getCustomizeFile(this);

		setAttr("customizeField", customizeField);

		render("/yh/transferOrder/updateTransferOrder.html");
	}

	public void edit() {
		long id = getParaToLong("id");
		TransferOrder transferOrder = TransferOrder.dao.findById(id);
		setAttr("transferOrder", transferOrder);

		Long customer_id = transferOrder.get("customer_id");
		Long sp_id = transferOrder.get("sp_id");
		Long driver_id = transferOrder.get("driver_id");

		if (customer_id != null) {
			Party customer = Party.dao.findById(customer_id);
			Contact customerContact = Contact.dao.findById(customer
					.get("contact_id"));
			setAttr("customerContact", customerContact);
		}
		if (sp_id != null) {
			Party sp = Party.dao.findById(sp_id);
			Contact spContact = Contact.dao.findById(sp.get("contact_id"));
			setAttr("spContact", spContact);
		}
		if (driver_id != null) {
			Party driver = Party.dao.findById(driver_id);
			Contact driverContact = Contact.dao.findById(driver
					.get("contact_id"));
			setAttr("driverContact", driverContact);
		}
		/*
		 * Long notify_party_id = transferOrder.get("notify_party_id"); if
		 * (notify_party_id != null) { Party notify =
		 * Party.dao.findById(notify_party_id); Contact contact =
		 * Contact.dao.findById(notify.get("contact_id")); setAttr("contact",
		 * contact); }
		 */

		String routeFrom = transferOrder.get("route_from");
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
			
			Record re = Db.findFirst("select get_loc_full_name('"+routeFrom+"') as fromRoute");
			setAttr("fromRoute", re.getStr("fromRoute"));
		}

		String routeTo = transferOrder.get("route_to");
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
			
			Record re = Db.findFirst("select get_loc_full_name('"+routeTo+"') as toRoute");
			setAttr("toRoute", re.getStr("toRoute"));
		}
		Office office = Office.dao
				.findFirst(
						"select o.* from office o left join warehouse w on w.office_id = o.id where w.id = ?",
						transferOrder.get("warehouse_id"));
		setAttr("office", office);

		Office outOffice = Office.dao
				.findFirst(
						"select o.* from office o left join warehouse w on w.office_id = o.id where w.id = ?",
						transferOrder.get("from_warehouse_id"));
		setAttr("outOffice", outOffice);

		UserLogin userLogin = UserLogin.dao.findById(transferOrder
				.get("create_by"));
		setAttr("userLogin2", userLogin);
		List<Record> paymentItemList = Collections.EMPTY_LIST;
		paymentItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("paymentItemList", paymentItemList);
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);

		Map<String, String> customizeField = getCustomFile.getInstance()
				.getCustomizeFile(this);

		setAttr("customizeField", customizeField);

		render("/yh/transferOrder/updateTransferOrder.html");
	}

	public void save() {
		String id = getPara("party_id");
		Party party = null;
		Contact contact = null;
		Date createDate = Calendar.getInstance().getTime();
		if (id != null && !id.equals("")) {
			party = Party.dao.findById(id);
			party.set("last_update_date", createDate).update();

			contact = Contact.dao.findFirst("select * from contact where id=?",
					party.getLong("contact_id"));
			contact.update();
		} else {
			contact = new Contact();
			contact.save();
			party = new Party();
			party.set("party_type", Party.PARTY_TYPE_SERVICE_PROVIDER);
			party.set("contact_id", contact.getLong("id"));
			party.set("creator", "test");
			party.set("create_date", createDate);
			party.save();
		}
		setAttr("saveOK", true);
		render("/yh/transferOrder/transferOrderList.html");
	}

	// 保存客户
	public void saveCustomer() {
		String customer_id = getPara("customer_id");
		Party party = null;
		if (customer_id != null && !customer_id.equals("")) {
			party = Party.dao.findById(customer_id);
		} else {
			party = new Party();
			party.set("party_type", Party.PARTY_TYPE_CUSTOMER);
			Contact contact = new Contact();
			contact.save();
			party.set("contact_id", contact.getLong("id"));
			party.set("create_date", new Date());
			party.set("creator", currentUser.getPrincipal());
			party.save();
		}
		renderJson(party.get("id"));
	}

	// 保存供应商
	public void saveServiceProvider() {
		String sp_id = getPara("sp_id");
		Party party = null;
		if (sp_id != null && !sp_id.equals("")) {
			party = Party.dao.findById(sp_id);
		} else {
			party = new Party();
			party.set("party_type", Party.PARTY_TYPE_SERVICE_PROVIDER);
			Contact contact = new Contact();
			contact.save();
			party.set("contact_id", contact.getLong("id"));
			party.set("create_date", new Date());
			party.set("creator", currentUser.getPrincipal());
			party.save();
		}
		renderJson(party.get("id"));
	}

	// 收货人列表
	public void selectContact() {
		List<Contact> contacts = Contact.dao.find("select * from contact");
		renderJson(contacts);
	}

	public void saveItem() {
		render("/yh/transferOrder/transferOrderList.html");
	}

	// 保存订单项
	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_CREATE,
			PermissionConstant.PERMISSION_TO_UPDATE }, logical = Logical.OR)
	public void saveOrderItem() {
		TransferOrderItem orderItem = new TransferOrderItem();
		orderItem.set("item_name", getPara("item_name"));
		orderItem.set("item_desc", getPara("item_desc"));
		orderItem.set("amount", getPara("amount"));
		orderItem.set("unit", getPara("unit"));
		orderItem.set("volume", getPara("volume"));
		orderItem.set("weight", getPara("weight"));
		orderItem.set("remark", getPara("remark"));
		orderItem.set("order_id", getPara("order_id"));
		orderItem.save();
		// 当不需要返回值时
		renderJson("{\"success\":true}");
	}

	// 保存运输单
	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_CREATE,
			PermissionConstant.PERMISSION_TO_UPDATE }, logical = Logical.OR)
	public void saveTransferOrder() {
		String order_id = getPara("id");
		String warehouseId = getPara("gateInSelect");
		String officeId = getPara("officeSelect");
		String customerId = getPara("customer_id");
		String spId = getPara("sp_id");
		String from_warehouse_id = getPara("gateOutSelect");
		String sign_in_no = getPara("sign_in_no");
		boolean costCheckBox = "on".equals(getPara("costCheckBox"));
		boolean revenueCheckBox = "on".equals(getPara("revenueCheckBox"));

		TransferOrder transferOrder = null;
		String cargoNature = getPara("cargoNature");
		if (order_id == null || "".equals(order_id)) {
			transferOrder = new TransferOrder();
			if (!"".equals(spId) && spId != null) {
				transferOrder.set("sp_id", spId);
			}
			if(!"".equals(sign_in_no) && sign_in_no !=null){
				transferOrder.set("sign_in_no", sign_in_no);
			}
			transferOrder.set("customer_id", customerId);
			transferOrder.set("status", getPara("status"));
			if ("cargoReturnOrder".equals(getPara("orderType"))) {
				transferOrder.set("order_no",
						OrderNoGenerator.getNextOrderNo("TH"));
			} else {
				transferOrder.set("order_no",
						OrderNoGenerator.getNextOrderNo("YS"));
			}
			transferOrder.set("create_by", getPara("create_by"));
			if ("cargo".equals(cargoNature)) {
				transferOrder.set("cargo_nature_detail",
						getPara("cargoNatureDetail"));
			}else{
				transferOrder.set("cargo_nature_detail",
						"cargoNatureDetailYes");
			}
			transferOrder.set("cargo_nature", cargoNature);
			transferOrder.set("operation_type", getPara("operationType"));
			transferOrder.set("arrival_mode", getPara("arrivalMode"));
			transferOrder.set("address", getPara("address"));
			transferOrder.set("create_stamp", new Date());
			transferOrder.set("remark", getPara("remark"));
			transferOrder.set("route_from", getPara("route_from"));
			transferOrder.set("route_to", getPara("route_to"));
			transferOrder.set("order_type", getPara("orderType"));
			transferOrder.set("from_warehouse_id", from_warehouse_id);
			transferOrder.set("customer_province", getPara("customerProvince"));
			transferOrder.set("pickup_assign_status",
					TransferOrder.ASSIGN_STATUS_NEW);
			transferOrder.set("depart_assign_status",
					TransferOrder.ASSIGN_STATUS_NEW);
			transferOrder.set("payment", getPara("payment"));
			transferOrder.set("charge_type", getPara("chargeType"));
			transferOrder.set("car_type", getPara("car_type"));
			transferOrder.set("ltl_unit_type", getPara("ltlUnitType"));
			transferOrder.set("charge_type2", getPara("chargeType2"));
			transferOrder.set("customer_order_no", getPara("customerOrderNo"));
			transferOrder.set("receiving_unit", getPara("receiving_unit"));
			transferOrder.set("receiving_name",
					getPara("notify_contact_person"));
			transferOrder.set("receiving_address", getPara("notify_address"));
			transferOrder.set("receiving_phone", getPara("notify_phone"));
			transferOrder.set("no_contract_revenue", revenueCheckBox);
			transferOrder.set("no_contract_cost", costCheckBox);

			if (getParaToDate("planning_time") != null) {
				transferOrder.set("planning_time", getPara("planning_time"));
			}
			if (getParaToDate("arrival_time") != null) {
				transferOrder.set("arrival_time", getPara("arrival_time"));
			}
			if (getPara("arrivalMode") != null
					&& getPara("arrivalMode").equals("delivery")
					|| getPara("arrivalMode").equals("deliveryToFactory")) {

			} else {
				if (warehouseId != null && !"".equals(warehouseId)) {
					transferOrder.set("warehouse_id", warehouseId);
				}

			}
			if (officeId != null && !"".equals(officeId)) {
				transferOrder.set("office_id", officeId);
			}
			transferOrder.save();

			saveTransferOrderMilestone(transferOrder);
		} else {
			transferOrder = TransferOrder.dao.findById(order_id);
			if (!"".equals(spId) && spId != null) {
				transferOrder.set("sp_id", spId);
			}
			transferOrder.set("from_warehouse_id", from_warehouse_id);

			transferOrder.set("customer_id", customerId);
			if ("cargo".equals(cargoNature)) {
				transferOrder.set("cargo_nature_detail",
						getPara("cargoNatureDetail"));
			}else{
				transferOrder.set("cargo_nature_detail",
						"cargoNatureDetailYes");
			}
			transferOrder.set("cargo_nature", getPara("cargoNature"));
			transferOrder.set("operation_type", getPara("operationType"));
			transferOrder.set("arrival_mode", getPara("arrivalMode"));
			transferOrder.set("address", getPara("address"));
			transferOrder.set("remark", getPara("remark"));
			transferOrder.set("route_from", getPara("route_from"));
			transferOrder.set("route_to", getPara("route_to"));
			transferOrder.set("order_type", getPara("orderType"));
			transferOrder.set("customer_province", getPara("customerProvince"));
			transferOrder.set("pickup_assign_status",
					TransferOrder.ASSIGN_STATUS_NEW);
			transferOrder.set("depart_assign_status",
					TransferOrder.ASSIGN_STATUS_NEW);
			transferOrder.set("payment", getPara("payment"));
			transferOrder.set("charge_type", getPara("chargeType"));
			transferOrder.set("car_type", getPara("car_type"));
			transferOrder.set("ltl_unit_type", getPara("ltlUnitType"));
			transferOrder.set("charge_type2", getPara("chargeType2"));
			transferOrder.set("customer_order_no", getPara("customerOrderNo"));
			transferOrder.set("receiving_unit", getPara("receiving_unit"));
			transferOrder.set("receiving_name",
					getPara("notify_contact_person"));
			transferOrder.set("receiving_address", getPara("notify_address"));
			transferOrder.set("receiving_phone", getPara("notify_phone"));
			transferOrder.set("no_contract_revenue", revenueCheckBox);
			transferOrder.set("no_contract_cost", costCheckBox);
			transferOrder.set("sign_in_no", sign_in_no);
	
			if (getParaToDate("planning_time") != null) {
				transferOrder.set("planning_time", getPara("planning_time"));
			}
			if (getParaToDate("arrival_time") != null) {
				transferOrder.set("arrival_time", getPara("arrival_time"));
			}

			if (getPara("arrivalMode") != null
					&& getPara("arrivalMode").equals("delivery")
					|| getPara("arrivalMode").equals("deliveryToFactory")) {

				transferOrder.set("warehouse_id", null);
			} else {
				if (warehouseId != null && !"".equals(warehouseId)) {
					transferOrder.set("warehouse_id", warehouseId);
				}

			}
			if (officeId != null && !"".equals(officeId)) {
				transferOrder.set("office_id", officeId);
			}
			transferOrder.update();

		}
		renderJson(transferOrder);
	}

	// 删除发车单
	private void deleteDepartOrder(TransferOrder transferOrder) {
		DepartTransferOrder departTransferOrder = DepartTransferOrder.dao
				.findFirst("select * from depart_transfer where order_id = ?",
						transferOrder.get("id"));
		if (departTransferOrder != null) {
			Long departId = departTransferOrder.get("depart_id");
			departTransferOrder.set("order_id", null);
			departTransferOrder.update();
			departTransferOrder.delete();
			DepartOrder.dao.deleteById(departId);
		}
	}

	// 保存运输里程碑
	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_CREATE,
			PermissionConstant.PERMISSION_TO_UPDATE }, logical = Logical.OR)
	private void saveTransferOrderMilestone(TransferOrder transferOrder) {
		TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
		transferOrderMilestone.set("status", "新建");
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		transferOrderMilestone.set("create_by", users.get(0).get("id"));
		transferOrderMilestone.set("location", "");
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		transferOrderMilestone.set("create_stamp", sqlDate);
		transferOrderMilestone.set("type",
				TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
		transferOrderMilestone.set("order_id", transferOrder.get("id"));
		transferOrderMilestone.save();
	}

	// 保存收货人
	/*
	 * public Party saveContact() { Party party = new Party(); Contact contact =
	 * setContact(); party.set("contact_id", contact.getLong("id"));
	 * party.set("create_date", new Date()); party.set("creator",
	 * currentUser.getPrincipal()); party.set("party_type",
	 * Party.PARTY_TYPE_NOTIFY_PARTY); party.save(); return party; }
	 */

	// 更新收货人party
	/*
	 * public Party updateContact(String notifyPartyId) { Party party =
	 * Party.dao.findById(notifyPartyId); Contact contact = editContact(party);
	 * party.set("create_date", new Date()); party.set("creator",
	 * currentUser.getPrincipal()); party.set("party_type",
	 * Party.PARTY_TYPE_NOTIFY_PARTY); party.update(); return party; }
	 */

	// 保存联系人
	public Contact setContact() {
		Contact contact = new Contact();
		contact.set("contact_person", getPara("notify_contact_person"));
		contact.set("phone", getPara("notify_phone"));
		contact.set("address", getPara("notify_address"));
		contact.save();
		return contact;
	}

	// 更新联系人
	private Contact editContact(Party party) {
		Contact contact = Contact.dao.findById(party.get("contact_id"));
		contact.set("contact_person", getPara("notify_contact_person"));
		contact.set("phone", getPara("notify_phone"));
		contact.set("address", getPara("notify_address"));
		contact.update();
		return contact;
	}

	// 更新司机
	private Contact editDriver(Party party) {
		Contact contact = Contact.dao.findById(party.get("contact_id"));
		contact.set("contact_person", getPara("driver_name"));
		contact.set("phone", getPara("driver_phone"));
		contact.update();
		return contact;
	}

	// 查找客户
	public void searchCustomer() {
		String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select *,p.id as pid,p.payment from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_CUSTOMER
							+ "' and (c.company_name like '%"
							+ input
							+ "%' or c.contact_person like '%"
							+ input
							+ "%' or c.email like '%"
							+ input
							+ "%' or c.mobile like '%"
							+ input
							+ "%' or c.phone like '%"
							+ input
							+ "%' or c.address like '%"
							+ input
							+ "%' or c.postal_code like '%"
							+ input
							+ "%') and p.id in (select customer_id from user_customer where user_name='"
							+ currentUser.getPrincipal() + "') limit 0,10");
		} else {
			locationList = Db
					.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_CUSTOMER
							+ "' and p.id in (select customer_id from user_customer where user_name='"
							+ currentUser.getPrincipal() + "')");
		}
		renderJson(locationList);
	}

	public void searchPartCustomer() {
		String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select *,p.id as pid,p.payment from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_CUSTOMER
							+ "' and (c.company_name like '%"
							+ input
							+ "%' or c.contact_person like '%"
							+ input
							+ "%' or c.email like '%"
							+ input
							+ "%' or c.mobile like '%"
							+ input
							+ "%' or c.phone like '%"
							+ input
							+ "%' or c.address like '%"
							+ input
							+ "%' or c.postal_code like '%"
							+ input
							+ "%') and (p.is_stop is null or p.is_stop = 0) and p.id in (select customer_id from user_customer where user_name='"
							+ currentUser.getPrincipal() + "') limit 0,10");
		} else {
			locationList = Db
					.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_CUSTOMER
							+ "' and (p.is_stop is null or p.is_stop = 0) and p.id in (select customer_id from user_customer where user_name='"
							+ currentUser.getPrincipal() + "')");
		}
		renderJson(locationList);
	}

	// 查找供应商
	public void searchSp() {
		String input = getPara("input");
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();
		// Long officeID = pom.getCurrentOfficeId();
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select p.*,c.*,p.id as pid, p.payment from party p,contact c,office o where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_SERVICE_PROVIDER
							+ "' and (c.company_name like '%"
							+ input
							+ "%' or c.abbr like '%"
							+ input
							+ "%' or c.contact_person like '%"
							+ input
							+ "%' or c.email like '%"
							+ input
							+ "%' or c.mobile like '%"
							+ input
							+ "%' or c.phone like '%"
							+ input
							+ "%' or c.address like '%"
							+ input
							+ "%' or c.postal_code like '%"
							+ input
							+ "%') and o.id = p.office_id and (o.id = ? or o.belong_office = ?)",
							parentID, parentID);
		} else {
			locationList = Db
					.find("select p.*,c.*,p.id as pid from party p,contact c,office o where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_SERVICE_PROVIDER
							+ "' and o.id = p.office_id and (o.id = ? or o.belong_office = ?)",
							parentID, parentID);
		}
		renderJson(locationList);
	}

	// 查找序列号
	public void searchItemNo() {
		String input = getPara("input");
		String customerId = getPara("customerId");
		String warehouseId = getPara("warehouseId");
		String orderType = getPara("orderType");
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			input = input.toUpperCase();
			if ("arrangementOrder".equalsIgnoreCase(orderType)) {
				locationList = Db
						.find("select ii.total_quantity,p.* from inventory_item ii "
								+ "left join product p on p.id = ii.product_id where warehouse_id = "
								+ warehouseId
								+ " and party_id = "
								+ customerId
								+ "  and ( upper(p.item_no) like '%"
								+ input
								+ "%' or upper(p.item_name) like '%"
								+ input
								+ "%') limit 0,10");
			} else {
				locationList = Db
						.find("select * from product where category_id in (select id from category where customer_id = "
								+ customerId
								+ ") and ( upper(item_no) like '%"
								+ input
								+ "%' or upper(item_name) like '%"
								+ input
								+ "%') and (is_stop is null or is_stop =0) limit 0,10");
			}

		} else {
			if ("arrangementOrder".equalsIgnoreCase(orderType)) {
				locationList = Db
						.find("select ii.total_quantity,p.* from inventory_item ii left join product p on p.id = ii.product_id where warehouse_id = ? and party_id = ?",
								warehouseId, customerId);
			} else {
				locationList = Db
						.find("select * from product where category_id in (select id from category where customer_id = "
								+ customerId
								+ ") and (is_stop is null or is_stop =0)");
			}

		}
		renderJson(locationList);
	}

	// 查找产品名
	public void searchItemName() {
		String input = getPara("input");
		String customerId = getPara("customerId");
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select * from product where category_id in (select id from category where customer_id = "
							+ customerId
							+ ") and item_name like '%"
							+ input
							+ "%' limit 0,10");
		} else {
			locationList = Db
					.find("select * from product where category_id in (select id from category where customer_id = "
							+ customerId + ")");
		}
		renderJson(locationList);
	}

	// 删除订单
	// @RequiresPermissions(value = {PermissionConstant.PERMISSION_TO_DELETE})
	public void delete() {
		long id = getParaToLong();

		// 删除主表
		TransferOrder transferOrder = TransferOrder.dao.findById(id);
		transferOrder.set("notify_party_id", null);
		transferOrder.set("customer_id", null);
		transferOrder.set("sp_id", null);

		transferOrder.delete();
		redirect("/transferOrder");
	}

	//撤销单据
	@Before(Tx.class)
	public void cancel() {
		String id = getPara("orderId");
		String sql = "select dt.transfer_order_no, dor.depart_no, dor.STATUS as pickup_status from depart_transfer dt "
				+ "left outer join depart_order dor on dt.pickup_id = dor.id "
				+ "where dor.STATUS not in('新建','取消') and dt.order_id=" + id;
		List<Record> nextOrders = Db.find(sql);
		if (nextOrders.size() == 0) {
			//先删除从表
			//删除主表
			//1.删除里程碑数据
			List<TransferOrderMilestone> tms = TransferOrderMilestone.dao.find("select * from transfer_order_milestone where order_id =?",id);
			for(TransferOrderMilestone tm :tms){
				tm.delete();
			}
			
			//2.删除单品明细表
			List<TransferOrderItemDetail> toids = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where order_id =?",id);
			for(TransferOrderItemDetail toid :toids){
				toid.delete();
			}
			
			//3.删除单品表
			List<TransferOrderItem> tois = TransferOrderItem.dao.find("select * from transfer_order_item where order_id =?",id);
			for(TransferOrderItem toi :tois){
				toi.delete();
			}
			
			//4.删除主表
			TransferOrder order = TransferOrder.dao.findById(id);
			order.delete();
			renderJson("{\"success\":true}");
		} else {
			renderJson("{\"success\":false}");
		}

	}

	// 导入运输单
	public void importTransferOrder() {
		String strFileConfirm = getPara("str");
		String errCustomerNo = getPara("errCustomerNo");
		UploadFile uploadFile = null;
		File file = null;
		String fileName = null;
		String strFile = null;
		if (strFileConfirm == null) {
			uploadFile = getFile();
			file = uploadFile.getFile();
			fileName = file.getName();
			strFile = file.getPath();
		}
		Map<String, String> resultMap = new HashMap<String, String>();
		try {
			String[] title = null;
			List<Map<String, String>> content = new ArrayList<Map<String, String>>();
			if (strFileConfirm == null) {
				if (fileName.endsWith(".xls")) {
					title = ReaderXLS.getXlsTitle(file);
					content = ReaderXLS.getXlsContent(file);
				} else if (fileName.endsWith(".xlsx")) {
					title = ReaderXlSX.getXlsTitle(file);
					content = ReaderXlSX.getXlsContent(file);
				} else {
					resultMap.put("result", "false");
					resultMap.put("cause", "导入失败，请选择正确的excel文件");
				}
			} else {
				if (strFileConfirm.endsWith(".xls")) {
					title = ReaderXLS.getXlsTitle(new File(strFileConfirm));
					content = ReaderXLS.getXlsContent(new File(strFileConfirm));
				} else if (strFileConfirm.endsWith(".xlsx")) {
					title = ReaderXlSX.getXlsTitle(new File(strFileConfirm));
					content = ReaderXlSX.getXlsContent(new File(strFileConfirm));
				} else {
					resultMap.put("result", "false");
					resultMap.put("cause", "导入失败，请选择正确的excel文件");
				}
			}
			if (title != null && content.size() > 0) {
				TransferOrderExeclHandeln handeln = new TransferOrderExeclHandeln();
				if (handeln.checkoutExeclTitle(title, "transferOrder")) {
					resultMap = handeln.importTransferOrder(content,strFileConfirm,errCustomerNo);
					resultMap.put("strFile", strFile);
				} else {
					resultMap.put("result", "false");
					resultMap.put("cause", "导入失败，excel标题列与系统默认excel标题列不一致");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", "false");
			resultMap
					.put("cause",
							"导入失败，请选择正确的excel文件<br/>（建议使用Microsoft Office Excel软件操作数据）");
			// renderJson(resultMap);
		}
		logger.debug("result:" + resultMap.get("result") + ",cause:"
				+ resultMap.get("cause"));

		renderJson(resultMap);
	}
	
	
	//查询地址（省份-城市）
	public void searchLocation() {
		String code = getPara("location");
		if("".equals(code) || code==null)
			return;
		Record re = Db.findFirst("select get_loc_full_name('"+code+"') as toRoute");
		renderJson(re);
	}
	

	// 根据客户查出location
	public void searchLocationFrom() {
		String code = getPara("locationFrom");
		List<Location> provinces = Location.dao
				.find("select * from location where pcode ='1'");
		Location l = Location.dao
				.findFirst("select * from location where CODE = '" + code + "'");
		Location l2 = Location.dao
				.findFirst("select * from location where code = (select pcode from location where CODE = '"
						+ code + "')");
		Location location = null;
		if (provinces.contains(l)) {
			location = Location.dao
					.findFirst("select l.name as province,l.code from location l left join location where l.code = '"
							+ code + "'");
		} else if (provinces.contains(l2)) {
			location = Location.dao
					.findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
							+ code + "'");
		} else {
			location = Location.dao
					.findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
							+ code + "'");
		}
		renderJson(location);
	}

	// 查出所有的warehouse
	public void searchAllWarehouse() {
		String officeId = getPara("officeId");

		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();

		List<Warehouse> warehouses = null;
		if (officeId != null && !"".equals(officeId)) {
			warehouses = Warehouse.dao
					.find("select * from warehouse where office_id in ("
							+ getPara("officeId") + ")");
		} else {
			warehouses = Warehouse.dao
					.find("select * from warehouse w left join office o on o.id = w.office_id where (o.id = "
							+ parentID
							+ " or o.belong_office = "
							+ parentID
							+ ")");
		}
		renderJson(warehouses);
	}

	// 查出所有的office
	public void searchAllOffice() {
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();

		List<Record> offices = Db
				.find("select o.id,o.office_name,o.is_stop from office o where o.id = "
						+ parentID + " or o.belong_office = " + parentID);
		renderJson(offices);
	}

	// 根据用户是否拥有这个网点查询
	public void searchPartOffice() {
		List<Record> offices = Db
				.find("select o.id,o.office_name,o.is_stop from office o where o.id in (select office_id from user_office where user_name='"
						+ currentUser.getPrincipal() + "')");
		renderJson(offices);
	}

	// 查出所有的driver
	public void searchAllDriver() {
		String input = getPara("input");
		String party_type = getPara("partyType");

		// 获取当前用户的总公司
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();

		List<Record> locationList = Collections.EMPTY_LIST;
		String sql = "";
		if (input.trim().length() > 0) {
			if (party_type != null) {
				sql = "select p.id pid,c.*,p.is_stop from party p left join contact c on c.id = p.contact_id left join office o on p.office_id = o.id where (c.contact_person like '%"
						+ input
						+ "%' or c.phone like '%"
						+ input
						+ "%') and p.party_type = '"
						+ party_type
						+ "' and (p.is_stop is null or p.is_stop = 0) and (o.id = "
						+ parentID + " or o.belong_office = " + parentID + ")";

			} else {
				sql = "select p.id pid,c.*,p.is_stop from party p left join contact c on c.id = p.contact_id left join office o on p.office_id = o.id where (c.contact_person like '%"
						+ input
						+ "%' or c.phone like '%"
						+ input
						+ "%') and p.party_type = 'SP_DRIVER' and (p.is_stop is null or p.is_stop = 0) and (o.id = "
						+ parentID + " or o.belong_office = " + parentID + ")";
			}

		} else {

			if (party_type != null) {
				sql = "select p.id pid,c.*,p.is_stop from party p left join contact c on c.id = p.contact_id left join office o on p.office_id = o.id where p.party_type = '"
						+ party_type
						+ "'  and (p.is_stop is null or p.is_stop = 0) and (o.id = "
						+ parentID + " or o.belong_office = " + parentID + ")";
			} else {
				sql = "select p.id pid,c.*,p.is_stop from party p left join contact c on c.id = p.contact_id left join office o on p.office_id = o.id where p.party_type = 'SP_DRIVER'  and (p.is_stop is null or p.is_stop = 0) and (o.id = "
						+ parentID + " or o.belong_office = " + parentID + ")";
			}

		}
		locationList = Db.find(sql);
		renderJson(locationList);
	}

	// TODO: 查出所有的carinfo
	public void searchAllCarInfo() {
		String type = getPara("type");
		String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;

		// 获取当前用户的总公司
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();

		String sql = "";

		if (input.trim().length() > 0) {
			if (type != null) {
				sql = "SELECT * FROM carinfo c LEFT JOIN office o ON o.id = c.office_id WHERE c.type = '"+type+"' AND (c.is_stop IS NULL OR c.is_stop = 0)"
						+ " AND (o.id = "+parentID+" OR o.belong_office ="+parentID+")AND (c.driver LIKE '%"+input+"%' or c.car_no LIKE '%"+input+"%')";

			} else {
				sql = "SELECT * FROM carinfo c LEFT JOIN office o ON o.id = c.office_id WHERE (c.is_stop IS NULL OR c.is_stop = 0) "
						+ "AND (o.id = "+parentID+" OR o.belong_office ="+parentID+")AND (c.driver LIKE '%"+input+"%' or c.car_no LIKE '%"+input+"%')";
			}

		} else {
			if (type != null) {
				sql = "select c.* from carinfo  c left join office o on o.id = c.office_id where c.type = '"
						+ type
						+ "'  and (c.is_stop is null or c.is_stop = 0) and (o.id = "
						+ parentID + " or o.belong_office = " + parentID + ")";
			} else {
				sql = "select c.* from carinfo c left join office o on c.office_id = o.id  where c.type = 'SP'  and (c.is_stop is null or c.is_stop = 0)  and (o.id = "
						+ parentID + " or o.belong_office = " + parentID + ")";
			}

		}
		locationList = Db.find(sql);
		renderJson(locationList);
	}

	// 应收应付
	// 应收list
	public void accountReceivable() {
		String id = getPara();

		String sLimit = "";
		if (id == null || id.equals("")) {
			Map orderMap = new HashMap();
			orderMap.put("sEcho", 0);
			orderMap.put("iTotalRecords", 0);
			orderMap.put("iTotalDisplayRecords", 0);
			orderMap.put("aaData", null);
			renderJson(orderMap);
			return;
		}
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		// 获取总条数
		String totalWhere = "";
		String sql = "select count(0) total from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join transfer_order t on t.id = d.order_id where d.order_id ='"
				+ id + "' and f.type='应收'"; // and f.type='应收' TODO： 有问题
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select d.*,f.name,dor.order_no as deliveryOrderNO,t.order_no as transferOrderNo "
						+ " from transfer_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id "
						+ " left join transfer_order t on t.id = d.order_id "
						+ " left join delivery_order_item doi on doi.transfer_order_id = d.order_id"
						+ " left join delivery_order dor on dor.id = doi.delivery_id"
						+ " where d.order_id = " + id + " and f.type = '应收'");
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}

	// TODO 应付list
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
		String sql = "select count(0) total from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join transfer_order t on t.id = d.order_id where d.order_id='"
				+ id + "' and f.type='应付' ";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select d.*,f.name,t.order_no as transferOrderNo from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join transfer_order t on t.id = d.order_id where d.order_id='"
						+ id + "' and f.type='应付' ");

		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}

	// 应收
	public void addNewRow2() {
		List<FinItem> items = new ArrayList<FinItem>();
		String orderId = getPara();
		FinItem item = FinItem.dao
				.findFirst("select * from fin_item where type = '应收' order by id asc");
		if (item != null) {
			TransferOrderFinItem dFinItem = new TransferOrderFinItem();
			dFinItem.set("status", "新建").set("fin_item_id", item.get("id"))
					.set("order_id", orderId)
					.set("create_name", dFinItem.CREATE_NAME_USER).save();
		}
		items.add(item);
		renderJson(items);
	}

	public void addNewRow() {
		List<FinItem> items = new ArrayList<FinItem>();
		String orderId = getPara();
		FinItem item = FinItem.dao
				.findFirst("select * from fin_item where type = '应付' order by id asc");
		if (item != null) {
			TransferOrderFinItem dFinItem = new TransferOrderFinItem();
			dFinItem.set("status", "新建").set("fin_item_id", item.get("id"))
					.set("order_id", orderId)
					.set("create_name", dFinItem.CREATE_NAME_USER).save();
		}
		items.add(item);
		renderJson(items);
	}

	// 保存应收应付
	public void paymentSave() {
		String returnValue = "";
		String id = getPara("id");
		String finItemId = getPara("finItemId");
		TransferOrderFinItem dFinItem = TransferOrderFinItem.dao.findById(id);

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
		// 清除不要的条目
		List<Record> list = Db.find("select * from fin_item");
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).get("name") == null) {
				FinItem.dao.deleteById(list.get(i).get("id"));
				List<Record> list2 = Db
						.find("select * from transfer_order_fin_item where fin_item_id ='"
								+ list.get(i).get("id") + "'");
				List<Record> list3 = Db
						.find("select * from fin_item where id ='"
								+ list2.get(0).get("fin_item_id") + "'");
				if (list3.size() == 0) {
					TransferOrderFinItem.dao.deleteById(list2.get(0).get("id"));
				}
			}
		}
		// 删除没添加成功的记录
		List<Record> list2 = Db
				.find("select * from transfer_order_fin_item where order_id ='"
						+ dFinItem.get("order_id") + "'");
		if (list2.size() > 0) {
			for (int i = 0; i < list2.size(); i++) {
				FinItem fin_item = FinItem.dao.findById(list2.get(i).get(
						"fin_item_id"));
				if (fin_item == null) {
					TransferOrderFinItem.dao.deleteById(list2.get(i).get("id"));
				}

			}
		}
		renderText(returnValue);
	}

	public void getPaymentList() {// getPaymentList
		// String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		locationList = Db.find("select * from fin_item where type='应付'");
		renderJson(locationList);
	}

	public void getChargeList() {// getChargeList
		// String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		locationList = Db.find("select * from fin_item where type='应收'");
		renderJson(locationList);
	}

	// 费用删除
	public void finItemdel() {
		String id = getPara();
		TransferOrderFinItem.dao.deleteById(id);
		renderJson("{\"success\":true}");
	}

	// 选中仓库后需查出相应的信息
	public void selectWarehouse() {
		String warehouseId = getPara("warehouseId");
		Warehouse warehouse = Warehouse.dao.findById(warehouseId);
		Contact contact = Contact.dao
				.findById(warehouse.get("notify_party_id"));
		Location location = new Location();
		if (warehouse != null) {
			String code = warehouse.get("location");
			if (code != null || !"".equals(code)) {
				List<Location> provinces = Location.dao
						.find("select * from location where pcode ='1'");
				Location l = Location.dao
						.findFirst("select * from location where code = (select pcode from location where code = '"
								+ code + "')");
				if (provinces.contains(l)) {
					location = Location.dao
							.findFirst("select l1.id,l.name as city,l.code cityCode,l1.name as province,l1.code provinceCode from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
									+ code + "'");
				} else {
					location = Location.dao
							.findFirst("select l2.id,l.name as district,l.code districtCode, l1.name as city,l1.code cityCode,l2.name as province,l2.code provinceCode from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
									+ code + "'");
				}
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("location", location);
		map.put("warehouse", warehouse);
		renderJson(map);
	}

	// 修改应收
	public void updateTransferOrderFinItem() {
		String paymentId = getPara("paymentId");
		String name = getPara("name");
		String value = getPara("value");
		String orderId = getPara("orderId");
		if ("amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		TransferOrderFinItem transferOrderFinItem = null;
		if (paymentId != null && !"".equals(paymentId)) {
			transferOrderFinItem = TransferOrderFinItem.dao.findById(paymentId);
			transferOrderFinItem.set(name, value);
			transferOrderFinItem.update();
		} else {
			// 假定金额都是第一个填,如果不进行判断会创建多条记录
			if ("amount".equals(name) && !"".equals(value)) {
				transferOrderFinItem = new TransferOrderFinItem();
				transferOrderFinItem.set(name, value);
				transferOrderFinItem.set("order_id", orderId);
				transferOrderFinItem.set("status", "未结算");
				// transferOrderFinItem.set("create",
				// TransferOrderFinItem.CREATE_NAME_PICKUP);
				transferOrderFinItem.save();
			}
		}
		renderJson(transferOrderFinItem);
	}

	// 运输单状态
	public void findTransferOrderType() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		String orderNo = getPara("pointInTime");
		SimpleDateFormat simpleDate = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Calendar pastDay = Calendar.getInstance();
		if ("pastOneDay".equals(orderNo))
			pastDay.add(Calendar.DAY_OF_WEEK, -1);
		else if ("pastSevenDay".equals(orderNo))
			pastDay.add(Calendar.DAY_OF_WEEK, -7);
		else
			pastDay.add(Calendar.DAY_OF_WEEK, -30);
		String beginTime = df.format(pastDay.getTime());
		String endTime = simpleDate.format(Calendar.getInstance().getTime());

		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sqlTotal = "select count(0) total from transfer_order t where t.status != '取消' and (t.office_id in (select office_id from user_office where user_name='"
				+ currentUser.getPrincipal()
				+ "')) "
				+ " and t.customer_id in (select customer_id from user_customer where user_name='"
				+ currentUser.getPrincipal()
				+ "')"
				+ " and create_stamp between '"
				+ beginTime
				+ "' and '"
				+ endTime + "'";
		logger.debug("sql :" + sqlTotal);
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select t.id, t.order_no,"
				+ " ifnull((select name from location where code = t.route_from ), '' ) route_from,"
				+ " ifnull((select name from location where code = t.route_to ), '' ) route_to,"
				+ " (select status from transfer_order_milestone where order_id = t.id order by id desc limit 0,1) status,"
				+ " (select create_stamp from transfer_order_milestone where order_id = t.id order by id desc limit 0,1) create_stamp"
				+ " from transfer_order t where t.status != '取消' and (t.office_id in (select office_id from user_office where user_name='"
				+ currentUser.getPrincipal()
				+ "')) "
				+ " and t.customer_id in (select customer_id from user_customer where user_name='"
				+ currentUser.getPrincipal() + "') "
				+ " and create_stamp between '" + beginTime + "' and '"
				+ endTime + "' group by t.id order by t.create_stamp desc "
				+ sLimit;
		List<Record> transferOrderItems = Db.find(sql);
		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));
		Map.put("aaData", transferOrderItems);
		renderJson(Map);
	}

	// 运输单状态
	public void updateTransferOrderRevenue() {
		String orderId = getPara("order_id");
		String name = getPara("name");
		boolean revenue = getParaToBoolean("revenue");
		TransferOrder trans = TransferOrder.dao.findById(orderId);
		trans.set(name, revenue).update();
		renderJson("{\"success\":true}");
	}

	/**
	 * 文件下载
	 */
	public void downloadTransferOrderTemplate() {
		File file = new File(PathKit.getWebRootPath() + "/download/运输单导入模板.xls");
		renderFile(file);
	}

	public void searchAllUnit() {
		List<Record> offices = Db.find("select * from unit");
		renderJson(offices);
	}
	
	
	//通过路线获取供应商信息
	public void findSpByRoute(){
		String route_from = getPara("route_from");
		String route_to = getPara("route_to");
		String customer_id = getPara("customer_id");
		Record c_r_p  = Db.findFirst("SELECT cr.*,c.company_name,c.abbr,c.phone,c.address,c.contact_person FROM `customer_route_provider` cr"
				+ " LEFT JOIN contact c on c.id = cr.sp_id "
				+ " where cr.customer_id = "+customer_id+" and cr.location_from ='"+route_from+"' and cr.location_to = '"+ route_to + "'");
		if(c_r_p!=null)
		    renderJson(c_r_p);
		else
			renderJson("{\"success\":false}");
	}

}
