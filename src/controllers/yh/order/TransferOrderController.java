package controllers.yh.order;

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
import models.Fin_item;
import models.Location;
import models.Office;
import models.Party;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItem;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.Warehouse;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import validator.AuthenticatedValidator;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;
import controllers.yh.util.PoiUtils;

//@Before(AuthenticatedValidator.class)

@RequiresAuthentication
public class TransferOrderController extends Controller {

	private Logger logger = Logger.getLogger(TransferOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render("/yh/transferOrder/transferOrderList.html");
	}

	public void list() {
		Map transferOrderListMap = null;
		String orderNo = getPara("orderNo");
		String status = getPara("status");
		String address = getPara("address");
		String customer = getPara("customer");
		String sp = getPara("sp");
		String officeName = getPara("officeName");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		
		String order_type=getPara("order_type");
	
		String plantime=getPara("plantime");
		String arrivarltime=getPara("arrivarltime");
		String customer_order_no=getPara("customer_order_no");

		if (orderNo == null && status == null && address == null
				&& customer == null && sp == null && beginTime == null
				&& endTime == null&& order_type == null&& plantime == null
				&& arrivarltime == null&& customer_order_no == null) {
			String sLimit = "";
			String pageIndex = getPara("sEcho");
			if (getPara("iDisplayStart") != null
					&& getPara("iDisplayLength") != null) {
				sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
						+ getPara("iDisplayLength");
			}

			String sqlTotal = "select count(1) total from transfer_order t where t.status!='取消'";
			Record rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));

			String sql ="select t.*,"
					+ "c1.abbr cname,"
					+ "c2.abbr spname,"
					+ "o.office_name oname, "
                    + " (select sum(toi.amount) from transfer_order_item toi where toi.order_id=t.id) amount,"
                    + " (select sum(ifnull(toi.volume,p.volume)*toi.amount) from transfer_order_item toi left join product p on p.id=toi.product_id where toi.order_id=t.id) volume,"
                    + " (select sum(ifnull(nullif(toi.weight, 0), p.weight)*toi.amount) from transfer_order_item toi left join product p on p.id=toi.product_id where toi.order_id=t.id) weight, "
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
					+ " where t.status !='取消' order by create_stamp desc"
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
					+ " where t.status !='取消' "
					+ " and t.order_no like '%"+ orderNo 
					+ "%' and t.status like '%" + status
					+ "%' and t.address like '%" + address
					+ "%' and c1.abbr like '%" + customer
					+ "%' and ifnull(c2.abbr,'') like '%" + sp
					+ "%' and ifnull(o.office_name,'')  like '%" + officeName
					+ "%' and ifnull(t.order_type,'') like '%" + order_type
					+ "%' and ifnull(t.customer_order_no,'') like '%" + customer_order_no
					+ "%' and ifnull(t.planning_time,'') like '%" + plantime
					+ "%' and ifnull(t.arrival_time,'') like '%" + arrivarltime
					+ "%' and create_stamp between '" + beginTime + "' and '" + endTime + "'";
			Record rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));

			String sql = "select t.*,"
					+ "c1.abbr cname,"
					+ "c2.abbr spname,"
					+ "o.office_name oname, "
                    + " (select sum(toi.amount) from transfer_order_item toi where toi.order_id=t.id) amount,"
                    + " (select sum(ifnull(toi.volume,p.volume)*toi.amount) from transfer_order_item toi left join product p on p.id=toi.product_id where toi.order_id=t.id) volume,"
                    + " (select sum(ifnull(nullif(toi.weight, 0), p.weight)*toi.amount) from transfer_order_item toi left join product p on p.id=toi.product_id where toi.order_id=t.id) weight, "
                    + " (select sum(toid.pieces) from transfer_order_item_detail toid where toid.order_id=t.id) pieces, "
					+ " ifnull((select name from location where code = t.route_from),'') route_from,"
					+ " ifnull((select name from location where code = t.route_to),'') route_to, "
					+ " ul.user_name user_name"
					+ " from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join office o on t.office_id = o.id "
					+ " left join user_login ul on ul.id=t.create_by "
					+ " where t.status !='取消'"
					+ " and t.order_no like '%" + orderNo
					+ "%' and t.status like '%" + status
					+ "%' and t.address like '%" + address
					+ "%' and c1.abbr like '%" + customer
					+ "%' and ifnull(c2.abbr,'') like '%" + sp
					+ "%' and ifnull(o.office_name,'')  like '%" + officeName
					+ "%' and ifnull(t.order_type,'') like '%" + order_type
					+ "%' and ifnull(t.customer_order_no,'') like '%" + customer_order_no
					+ "%' and ifnull(t.planning_time,'') like '%" + plantime
					+ "%' and ifnull(t.arrival_time,'') like '%" + arrivarltime
					+ "%' and create_stamp between '" + beginTime
					+ "' and '" + endTime + "' order by create_stamp desc" + sLimit;

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

		TransferOrder order = TransferOrder.dao
				.findFirst("select * from transfer_order order by order_no desc limit 0,1");
		if (order != null) {
			String num = order.get("order_no");
			String str = num.substring(2, num.length());
			System.out.println(str);
			Long oldTime = Long.parseLong(str);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String format = sdf.format(new Date());
			String time = format + "00001";
			Long newTime = Long.parseLong(time);
			if (oldTime >= newTime) {
				order_no = String.valueOf((oldTime + 1));
			} else {
				order_no = String.valueOf(newTime);
			}
			setAttr("order_no", "YS" + order_no);
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String format = sdf.format(new Date());
			order_no = format + "00001";
			setAttr("order_no", "YS" + order_no);
		}

		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);
		List<Record> paymentItemList = Collections.EMPTY_LIST;
		paymentItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("paymentItemList", paymentItemList);
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);
		setAttr("status", "新建");
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
		Long notify_party_id = transferOrder.get("notify_party_id");
		if (notify_party_id != null) {
			Party notify = Party.dao.findById(notify_party_id);
			Contact contact = Contact.dao.findById(notify.get("contact_id"));
			setAttr("contact", contact);
		}

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
		}

		UserLogin userLogin = UserLogin.dao.findById(transferOrder
				.get("create_by"));
		setAttr("userLogin2", userLogin);
		List<Record> paymentItemList = Collections.EMPTY_LIST;
		paymentItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("paymentItemList", paymentItemList);
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);
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
	public void saveTransferOrder() {
		String order_id = getPara("id");
		String warehouseId = getPara("gateInSelect");
		String officeId = getPara("officeSelect");
		String customerId = getPara("customer_id");
		String spId = getPara("sp_id");
		TransferOrder transferOrder = null;
		String cargoNature = getPara("cargoNature");
		if (order_id == null || "".equals(order_id)) {
			transferOrder = new TransferOrder();
			if (!"".equals(spId) && spId != null) {
				transferOrder.set("sp_id", spId);
			}
			transferOrder.set("customer_id", customerId);
			transferOrder.set("status", getPara("status"));
			transferOrder.set("order_no", getPara("order_no"));
			transferOrder.set("create_by", getPara("create_by"));
			if ("cargo".equals(cargoNature)) {
				transferOrder.set("cargo_nature_detail",
						getPara("cargoNatureDetail"));
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
			transferOrder.set("customer_province", getPara("customerProvince"));
			transferOrder.set("pickup_assign_status",
					TransferOrder.ASSIGN_STATUS_NEW);
			transferOrder.set("depart_assign_status",
					TransferOrder.ASSIGN_STATUS_NEW);
			transferOrder.set("payment", getPara("payment"));
			transferOrder.set("charge_type", getPara("chargeType"));
			transferOrder.set("charge_type2", getPara("chargeType2"));
			transferOrder.set("customer_order_no", getPara("customerOrderNo")); 
			transferOrder.set("receiving_unit", getPara("receiving_unit")); 
			if(getParaToDate("planning_time") != null){
				transferOrder.set("planning_time", getPara("planning_time")); 
			}
			if(getParaToDate("arrival_time") != null){
				transferOrder.set("arrival_time", getPara("arrival_time")); 
			}
			Party party = null;
			String notifyPartyId = getPara("notify_party_id");
			if (getPara("arrivalMode") != null
					&& getPara("arrivalMode").equals("delivery")) {
				if (notifyPartyId == null || "".equals(notifyPartyId)) {
					party = saveContact();
				} else {
					party = updateContact(notifyPartyId);
				}
				transferOrder.set("notify_party_id", party.get("id"));
			} else {
				if (warehouseId != null && !"".equals(warehouseId)) {
					transferOrder.set("warehouse_id", warehouseId);
				}
				if (notifyPartyId == null || "".equals(notifyPartyId)) {
					party = saveContact();
				} else {
					party = updateContact(notifyPartyId);
				}
				transferOrder.set("notify_party_id", party.get("id"));
			}
			if (officeId != null && !"".equals(officeId)) {
				transferOrder.set("office_id", officeId);
			}
			transferOrder.save();
			/*
			 * // 如果是货品直送,则需生成一张发车单 if
			 * (transferOrder.get("arrival_mode").equals("delivery")) {
			 * createDepartOrder(transferOrder); }
			 */
			saveTransferOrderMilestone(transferOrder);
		} else {
			transferOrder = TransferOrder.dao.findById(order_id);
			if (!"".equals(spId) && spId != null) {
				transferOrder.set("sp_id", spId);
			}
			transferOrder.set("customer_id", customerId);
			transferOrder.set("order_no", getPara("order_no"));
			transferOrder.set("create_by", getPara("create_by"));
			if ("cargo".equals(cargoNature)) {
				transferOrder.set("cargo_nature_detail",
						getPara("cargoNatureDetail"));
			}
			transferOrder.set("cargo_nature", getPara("cargoNature"));
			transferOrder.set("operation_type", getPara("operationType"));
			transferOrder.set("arrival_mode", getPara("arrivalMode"));
			transferOrder.set("address", getPara("address"));
			transferOrder.set("create_stamp", new Date());
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
			transferOrder.set("charge_type2", getPara("chargeType2"));
			transferOrder.set("customer_order_no", getPara("customerOrderNo"));
			transferOrder.set("receiving_unit", getPara("receiving_unit")); 
			if(getParaToDate("planning_time") != null){
				transferOrder.set("planning_time", getPara("planning_time")); 
			}
			if(getParaToDate("arrival_time") != null){
				transferOrder.set("arrival_time", getPara("arrival_time")); 
			}
			Party party = null;
			String notifyPartyId = getPara("notify_party_id");
			if (getPara("arrivalMode") != null
					&& getPara("arrivalMode").equals("delivery")) {
				if (notifyPartyId == null || "".equals(notifyPartyId)) {
					party = saveContact();
				} else {
					party = updateContact(notifyPartyId);
				}
				transferOrder.set("notify_party_id", party.get("id"));
			} else {
				if (warehouseId != null && !"".equals(warehouseId)) {
					transferOrder.set("warehouse_id", warehouseId);
				}
				if (notifyPartyId == null || "".equals(notifyPartyId)) {
					party = saveContact();
				} else {
					party = updateContact(notifyPartyId);
				}
				transferOrder.set("notify_party_id", party.get("id"));
			}
			if (officeId != null && !"".equals(officeId)) {
				transferOrder.set("office_id", officeId);
			}
			transferOrder.update();
			/*
			 * // 如果是货品直送,则需判断是否新建一张发车单 if
			 * (transferOrder.get("arrival_mode").equals("delivery")) {
			 * DepartTransferOrder departTransferOrder =
			 * DepartTransferOrder.dao.findFirst(
			 * "select * from depart_transfer where order_id = ?",
			 * transferOrder.get("id")); if (departTransferOrder == null) {
			 * createDepartOrder(transferOrder); } else {
			 * updateDepartOrder(transferOrder, departTransferOrder); } } else {
			 * deleteDepartOrder(transferOrder); }
			 */
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

	// 创建发车单
	private void createDepartOrder(TransferOrder transferOrder) {
		String depart_no = creatDepartNo();
		String name = (String) currentUser.getPrincipal();
		UserLogin users = UserLogin.dao
				.findFirst("select * from user_login where user_name='" + name
						+ "'");
		String creat_id = users.get("id").toString();
		Date createDate = Calendar.getInstance().getTime();
		DepartOrder departOrder = new DepartOrder();
		departOrder.set("create_by", Integer.parseInt(creat_id))
				.set("create_stamp", createDate).set("combine_type", "DEPART")
				.set("depart_no", depart_no)
				.set("car_no", transferOrder.get("car_no"))
				.set("car_type", transferOrder.get("car_type"))
				.set("car_size", transferOrder.get("car_size"));
		departOrder.set("driver_id", transferOrder.get("driver_id"));
		departOrder.save();

		DepartTransferOrder departTransferOrder = new DepartTransferOrder();
		departTransferOrder.set("depart_id", departOrder.get("id"));
		departTransferOrder.set("order_id", transferOrder.get("id"));
		departTransferOrder.set("transfer_order_no",
				transferOrder.get("order_no"));
		departTransferOrder.save();
	}

	// 更新发车单
	private void updateDepartOrder(TransferOrder transferOrder,
			DepartTransferOrder departTransferOrder) {
		String depart_no = creatDepartNo();
		String name = (String) currentUser.getPrincipal();
		UserLogin users = UserLogin.dao
				.findFirst("select * from user_login where user_name='" + name
						+ "'");
		String creat_id = users.get("id").toString();
		Date createDate = Calendar.getInstance().getTime();
		DepartOrder departOrder = DepartOrder.dao.findById(departTransferOrder
				.get("depart_id"));
		departOrder.set("create_by", Integer.parseInt(creat_id))
				.set("create_stamp", createDate).set("combine_type", "DEPART")
				.set("depart_no", depart_no)
				.set("car_no", transferOrder.get("car_no"))
				.set("car_type", transferOrder.get("car_type"))
				.set("car_size", transferOrder.get("car_size"));
		departOrder.set("driver_id", transferOrder.get("driver_id"));
		departOrder.update();

		departTransferOrder.set("depart_id", departOrder.get("id"));
		departTransferOrder.set("order_id", transferOrder.get("id"));
		departTransferOrder.set("transfer_order_no",
				transferOrder.get("order_no"));
		departTransferOrder.update();
	}

	// 创建发车单序列号
	private String creatDepartNo() {
		String order_no = null;
		String the_order_no = null;
		DepartOrder order = DepartOrder.dao
				.findFirst("select * from depart_order where  combine_type= '"
						+ DepartOrder.COMBINE_TYPE_DEPART
						+ "' order by depart_no desc limit 0,1");
		if (order != null) {
			String num = order.get("DEPART_no");
			String str = num.substring(2, num.length());
			System.out.println(str);
			Long oldTime = Long.parseLong(str);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String format = sdf.format(new Date());
			String time = format + "00001";
			Long newTime = Long.parseLong(time);
			if (oldTime >= newTime) {
				order_no = String.valueOf((oldTime + 1));
			} else {
				order_no = String.valueOf(newTime);
			}
			the_order_no = "FC" + order_no;
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String format = sdf.format(new Date());
			order_no = format + "00001";
			the_order_no = "FC" + order_no;
		}
		return the_order_no;
	}

	// 保存运输里程碑
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
	public Party saveContact() {
		Party party = new Party();
		Contact contact = setContact();
		party.set("contact_id", contact.getLong("id"));
		party.set("create_date", new Date());
		party.set("creator", currentUser.getPrincipal());
		party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
		party.save();
		return party;
	}

	// 更新收货人party
	public Party updateContact(String notifyPartyId) {
		Party party = Party.dao.findById(notifyPartyId);
		Contact contact = editContact(party);
		party.set("create_date", new Date());
		party.set("creator", currentUser.getPrincipal());
		party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
		party.update();
		return party;
	}

	// 保存联系人
	private Contact setContact() {
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
							+ "' and (company_name like '%"
							+ input
							+ "%' or contact_person like '%"
							+ input
							+ "%' or email like '%"
							+ input
							+ "%' or mobile like '%"
							+ input
							+ "%' or phone like '%"
							+ input
							+ "%' or address like '%"
							+ input
							+ "%' or postal_code like '%"
							+ input
							+ "%') limit 0,10");
		} else {
			locationList = Db
					.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_CUSTOMER + "'");
		}
		renderJson(locationList);
	}

	// 查找供应商
	public void searchSp() {
		String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select *,p.id as pid, p.payment from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_SERVICE_PROVIDER
							+ "' and (company_name like '%"
							+ input
							+ "%' or contact_person like '%"
							+ input
							+ "%' or email like '%"
							+ input
							+ "%' or mobile like '%"
							+ input
							+ "%' or phone like '%"
							+ input
							+ "%' or address like '%"
							+ input
							+ "%' or postal_code like '%"
							+ input
							+ "%') limit 0,10");
		} else {
			locationList = Db
					.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_SERVICE_PROVIDER + "'");
		}
		renderJson(locationList);
	}

	// 查找序列号
	public void searchItemNo() {
		String input = getPara("input");
		String customerId = getPara("customerId");
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			input = input.toUpperCase();
			locationList = Db
					.find("select * from product where category_id in (select id from category where customer_id = "
							+ customerId
							+ ") and ( upper(item_no) like '%"
							+ input
							+ "%' or upper(item_name) like '%"
							+ input
							+ "%') limit 0,10");
		} else {
			locationList = Db
					.find("select * from product where category_id in (select id from category where customer_id = "
							+ customerId + ")");
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

	// 取消
	public void cancel() {
		String id = getPara();
		TransferOrder.dao.findById(id).set("Status", "取消").update();
		renderJson("{\"success\":true}");
	}

	// 导入运输单
	public void importTransferOrder() {
		// UploadFile uploadFile = getFile();
		// logger.debug("上传的文件名:" + uploadFile.getFileName());
		Map<String, List<Object>> map = PoiUtils.readExcel("c:/c.xlsx");

		renderJson("{\"success\":true}");
	}

	// 根据客户查出location
	public void searchLocationFrom() {
		String code = getPara("locationFrom");
		List<Location> provinces = Location.dao
				.find("select * from location where pcode ='1'");
		Location l = Location.dao.findFirst("select * from location where CODE = '" + code + "'");
		Location l2 = Location.dao.findFirst("select * from location where code = (select pcode from location where CODE = '" + code + "')");
		Location location = null;
		if (provinces.contains(l)) {
			location = Location.dao.findFirst("select l.name as province,l.code from location l left join location where l.code = '" + code + "'");
		} else if (provinces.contains(l2)) {
			location = Location.dao
					.findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
							+ code + "'");
		}else {
			location = Location.dao
					.findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
							+ code + "'");
		}
		renderJson(location);
	}

	// 查出所有的warehouse
	public void searchAllWarehouse() {
		List<Warehouse> warehouses = Warehouse.dao
				.find("select * from warehouse");
		renderJson(warehouses);
	}

	// 查出所有的office
	public void searchAllOffice() {
		List<Office> offices = Office.dao.find("select * from office");
		renderJson(offices);
	}

	// 查出所有的driver
	public void searchAllDriver() {
		String input = getPara("input");
		String party_type =getPara("partyType");
		List<Record> locationList = Collections.EMPTY_LIST;
		String sql= "";
		if (input.trim().length() > 0) {
			if(party_type!=null){
				sql = "select p.id pid,c.* from party p left join contact c on c.id = p.contact_id where c.contact_person like '%"
						+ input
						+ "%' or c.phone like '%"
						+ input
						+ "%' and p.party_type = '"
						+ party_type
						+ "'";
				
			}else{
				sql = "select p.id pid,c.* from party p left join contact c on c.id = p.contact_id where c.contact_person like '%"
						+ input
						+ "%' or c.phone like '%"
						+ input
						+ "%' and p.party_type = 'SP_DRIVER'";
			}
			
		} else {
			/*locationList = Db
					.find("select p.id pid,c.* from party p left join contact c on c.id = p.contact_id where p.party_type = '"
						+ Party.PARTY_TYPE_DRIVER + "'");*/
			if(party_type!=null){
				sql= "select p.id pid,c.* from party p left join contact c on c.id = p.contact_id where p.party_type = '"
						+ party_type + "'";
			}else{
				sql= "select p.id pid,c.* from party p left join contact c on c.id = p.contact_id where p.party_type = 'SP_DRIVER'";
			}
			
		}
		locationList = Db.find(sql);
		renderJson(locationList);
	}

	// 查出所有的carinfo
	public void searchAllCarInfo() {
		String type = getPara("type");
		String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		String sql="";
		
		if (input.trim().length() > 0) {
			if(type!=null){
				/*sql="select * from carinfo where "
						+ "car_no like '%" + input + "%' or phone like '%"
						+ input + "%'and type = '"+ type+"'";*/
				sql = "select * from ("
						+ "select * from carinfo where type ='"+type+"') "
						+ "where car_no like '%"+input+"%' or phone like '%"+input+"%'";
						
			}else{
				sql="select * from carinfo where car_no like '%" + input + "%' or phone like '%"
							+ input + "%' and type = 'SP'";
			}
				
		} else {
			if(type!=null){
				sql = "select * from carinfo where type = '" + type + "'";
			}else{
				sql ="select * from carinfo where type = 'SP'";
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
				+ id + "' "; //and f.type='应收' TODO： 有问题
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		/*List<Record> orders = Db
				.find("select d.*,f.name,t.order_no as transferOrderNo from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join transfer_order t on t.id = d.order_id where d.order_id ='"
						+ id + "' and f.type='应收'");*/
		List<Record> orders =Db.find("SELECT d.*, f. NAME, t.order_no AS transferOrderNo FROM "
				+ "transfer_order_fin_item d "
				+ "LEFT JOIN fin_item f ON d.fin_item_id = f.id "
				//+ "LEFT JOIN return_order r ON d.order_id = r.id "
				+ "LEFT JOIN transfer_order t ON t.id = d.order_id "
				+ "WHERE d.order_id = "+id); //+" AND f.type = '应收'"
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}

	// 应付list
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

		List<Record> list = Db.find("select * from fin_item");
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).get("name") == null) {
				Fin_item.dao.deleteById(list.get(i).get("id"));
			}
		}

		renderJson(orderMap);
	}
	
    // 应收
    public void addNewRow2() {
        List<Fin_item> items = new ArrayList<Fin_item>();
        String orderId = getPara();
        Fin_item item = Fin_item.dao
                .findFirst("select * from fin_item where type = '应收' order by id asc");
        if (item != null) {
            TransferOrderFinItem dFinItem = new TransferOrderFinItem();
            dFinItem.set("status", "新建").set("fin_item_id", item.get("id"))
                    .set("order_id", orderId);
            dFinItem.save();
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

		Fin_item fItem = Fin_item.dao.findById(dFinItem.get("fin_item_id"));

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
				Fin_item.dao.deleteById(list.get(i).get("id"));
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
				Fin_item fin_item = Fin_item.dao.findById(list2.get(i).get(
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
		if (contact != null) {
			String code = contact.get("location");
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
}
