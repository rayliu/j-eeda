package controllers.yh.returnTransfer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.SetAttrLoginUserInterceptor;
import models.Location;
import models.Office;
import models.Party;
import models.TransferOrder;
import models.UserLogin;
import models.UserOffice;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.order.TransferOrderController;
import controllers.yh.util.PermissionConstant;
import controllers.yh.util.getCustomFile;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ReturnTransferController extends Controller{
	private Logger logger = Logger.getLogger(TransferOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	public void index() {
		Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
		setAttr("customizeField", customizeField);
		render("/yh/returnTransfer/returnTransferOrderList.html");
	}
	
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_TO_LIST})
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
		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sqlTotal = "";
		String sql = "";
		if (orderNo == null && status == null && address == null
				&& customer == null && sp == null && beginTime == null
				&& endTime == null&& order_type == null&& plantime == null
				&& arrivarltime == null&& customer_order_no == null) {
			

			sqlTotal = "select count(1) total from transfer_order t "
					+ " where t.status!='取消' and t.office_id in(select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
					+ " and t.order_type = 'cargoReturnOrder' and t.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
			

			sql = "select t.id,t.order_no,t.customer_order_no,t.status,t.cargo_nature,t.operation_type,t.arrival_mode,t.pickup_mode,t.create_stamp,"
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
					+ " where t.status !='取消' and t.order_type = 'cargoReturnOrder' and (t.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')) "
					+ " and t.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
					+ " order by t.status !='新建',t.status !='已发车',t.status !='在途',t.status !='已入货场',t.status !='已入库',t.status !='已签收',t.planning_time desc"
					+ sLimit;

			
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			
			sqlTotal = "select count(*) total "
					+ " from transfer_order t "
					+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on t.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join office o on t.office_id = o.id "
					+ " left join user_login ul on ul.id=t.create_by "
					+ " where t.status !='取消' and t.order_type = 'cargoReturnOrder' "
					+ " and t.order_no like '%"+ orderNo.trim() 
					+ "%' and t.status like '%" + status.trim()
					+ "%' and t.address like '%" + address.trim()
					+ "%' and c1.abbr like '%" + customer.trim()
					+ "%' and ifnull(c2.abbr,'') like '%" + sp.trim()
					+ "%' and ifnull(o.office_name,'')  like '%" + officeName.trim()
					+ "%' and ifnull(t.customer_order_no,'') like '%" + customer_order_no.trim()
					+ "%' and ifnull(t.planning_time,'') like '%" + plantime
					+ "%' and ifnull(t.arrival_time,'') like '%" + arrivarltime
					+ "%' and t.create_stamp between '" + beginTime + "' and '" + endTime + "'  "
					+ " and t.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
					+ " and t.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
			

			sql = "select t.id,t.order_no,t.customer_order_no,t.status,t.cargo_nature,t.operation_type,t.arrival_mode,t.pickup_mode,t.create_stamp,"
					+ " t.planning_time,t.arrival_time, t.address,t.order_type, c1.abbr cname,c2.abbr spname,o.office_name oname,t.remark,"
                    + " (select sum(toi.amount) from transfer_order_item toi where toi.order_id=t.id) amount,"
                    + " round((select sum(ifnull(toi.volume,0)) from transfer_order_item toi where toi.order_id = t.id),2) volume, "
                    + " round((select sum(ifnull(toi.sum_weight,0)) from transfer_order_item toi where toi.order_id = t.id),2) weight, "
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
					+ " where t.status !='取消' and t.order_type = 'cargoReturnOrder'"
					+ " and t.order_no like '%" + orderNo.trim()
					+ "%' and t.status like '%" + status.trim()
					+ "%' and t.address like '%" + address.trim()
					+ "%' and c1.abbr like '%" + customer.trim()
					+ "%' and ifnull(c2.abbr,'') like '%" + sp.trim()
					+ "%' and ifnull(o.office_name,'')  like '%" + officeName.trim()
					+ "%' and ifnull(t.customer_order_no,'') like '%" + customer_order_no.trim()
					+ "%' and ifnull(t.planning_time,'') like '%" + plantime
					+ "%' and ifnull(t.arrival_time,'') like '%" + arrivarltime
					+ "%' and t.create_stamp between '" + beginTime
					+ "' and '" + endTime + "' "
					+ " and t.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
					+ " and t.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') order by t.status !='新建',t.status !='已发车',t.status !='在途',t.status !='已入货场',t.status !='已入库',t.status !='已签收', t.planning_time desc" + sLimit;

			
		}
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		
		List<Record> transferOrders = Db.find(sql);

		transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords",rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);
		renderJson(transferOrderListMap);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_TO_CREATE})
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
		//根据用户的默认网点确定默认的运作网点
		UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = 1",userLogin.get("user_name"));
		if(uo != null){
			transferOrder.set("office_id", uo.get("office_id"));
		}
		
		
		setAttr("transferOrder",transferOrder);
		
		Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
		
		setAttr("customizeField", customizeField);
		
		render("/yh/returnTransfer/updateReturnTransferOrder.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_TO_UPDATE})
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
		Office office = Office.dao.findFirst("select o.* from office o left join warehouse w on w.office_id = o.id where w.id = ?", transferOrder.get("warehouse_id"));
		setAttr("office", office);	
		
		Office outOffice = Office.dao.findFirst("select o.* from office o left join warehouse w on w.office_id = o.id where w.id = ?", transferOrder.get("from_warehouse_id"));
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
		
		Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
		
		
		setAttr("customizeField", customizeField);
		
		render("/yh/returnTransfer/updateReturnTransferOrder.html");
	}
	
}
