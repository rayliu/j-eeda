package controllers.yh.order;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartTransferOrder;
import models.Location;
import models.Office;
import models.Party;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.Warehouse;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;
import controllers.yh.util.PoiUtils;

public class TransferOrderController extends Controller {

    private Logger logger = Logger.getLogger(TransferOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
        if (LoginUserController.isAuthenticated(this))
            render("transferOrder/transferOrderList.html");
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

        // String strWhere = DataTablesUtils.buildSingleFilter(this);
        if (orderNo == null && status == null && address == null
                && customer == null && sp == null && beginTime == null
                && endTime == null) {
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null
                    && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                        + getPara("iDisplayLength");
            }

            String sqlTotal = "select count(1) total from transfer_order";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select t.*,c1.abbr cname,c2.abbr spname,t.create_stamp,o.office_name oname from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join office o on t.office_id = o.id order by create_stamp desc";

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

            String sqlTotal = "select count(1) total from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id"
                    + " left join office o on t.office_id = o.id where t.order_no like '%"
                    + orderNo + "%' and t.status like '%" + status
                    + "%' and t.address like '%" + address
                    + "%' and c1.abbr like '%" + customer
                    + "%' and c2.abbr like '%" + sp
                    + "%' and o.office_name  like '%" + officeName
                    + "%' and create_stamp between '" + beginTime + "' and '"
                    + endTime + "'";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select t.*,c1.abbr cname,c2.abbr spname,o.office_name oname from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id"
                    + " left join office o on t.office_id = o.id where t.order_no like '%"
                    + orderNo
                    + "%' and t.status like '%"
                    + status
                    + "%' and t.address like '%"
                    + address
                    + "%' and c1.abbr like '%"
                    + customer
                    + "%' and c2.abbr  like '%"
                    + sp
                    + "%' and o.office_name  like '%"
                    + officeName
                    + "%' and create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime + "' order by create_stamp desc";

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

        setAttr("status", "新建");
        if (LoginUserController.isAuthenticated(this))
            render("transferOrder/editTransferOrder.html");
        // render("transferOrder/transferOrderEdit.html");
    }

    public void edit() {
        // long id = getParaToLong();
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
        	Contact driverContact = Contact.dao.findById(driver.get("contact_id"));
        	setAttr("driverContact", driverContact);
        }
        Long notify_party_id = transferOrder.get("notify_party_id");
        if (notify_party_id != null) {
            Party notify = Party.dao.findById(notify_party_id);
            Contact contact = Contact.dao.findById(notify.get("contact_id"));
            setAttr("contact", contact);
            Contact locationCode = Contact.dao.findById(notify
                    .get("contact_id"));
            String code = locationCode.get("location");

            List<Location> provinces = Location.dao
                    .find("select * from location where pcode ='1'");
            Location l = Location.dao
                    .findFirst("SELECT * FROM LOCATION where code = (select pcode from location where CODE = '"
                            + code + "')");
            Location location = null;
            if (provinces.contains(l)) {
                location = Location.dao
                        .findFirst("SELECT l.name as CITY,l1.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                + code + "'");
            } else {
                location = Location.dao
                        .findFirst("SELECT l.name as DISTRICT, l1.name as CITY,l2.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                + code + "'");
            }
            setAttr("location", location);
        } else {
            setAttr("contact", null);
        }

        String routeFrom = transferOrder.get("route_from");
        Location locationFrom = null;
        if (routeFrom != null || !"".equals(routeFrom)) {
            List<Location> provinces = Location.dao
                    .find("select * from location where pcode ='1'");
            Location l = Location.dao
                    .findFirst("SELECT * FROM LOCATION where code = (select pcode from location where CODE = '"
                            + routeFrom + "')");
            if (provinces.contains(l)) {
                locationFrom = Location.dao
                        .findFirst("SELECT l.name as CITY,l1.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                + routeFrom + "'");
            } else {
                locationFrom = Location.dao
                        .findFirst("SELECT l.name as DISTRICT, l1.name as CITY,l2.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
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
                    .findFirst("SELECT * FROM LOCATION where code = (select pcode from location where CODE = '"
                            + routeTo + "')");
            if (provinces.contains(l)) {
                locationTo = Location.dao
                        .findFirst("SELECT l.name as CITY,l1.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                + routeTo + "'");
            } else {
                locationTo = Location.dao
                        .findFirst("SELECT l.name as DISTRICT, l1.name as CITY,l2.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                + routeTo + "'");
            }
            setAttr("locationTo", locationTo);
        }

        UserLogin userLogin = UserLogin.dao.findById(transferOrder
                .get("create_by"));
        setAttr("userLogin", userLogin);
        if (LoginUserController.isAuthenticated(this))
            render("transferOrder/updateTransferOrder.html");
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
        if (LoginUserController.isAuthenticated(this))
            render("transferOrder/transferOrderList.html");
    }

    // 客户列表,列出最近使用的5个客户
    public void selectCustomer() {
        List<Contact> contactjson = Contact.dao
                .find("SELECT * FROM CONTACT WHERE ID IN(SELECT CONTACT_ID FROM PARTY WHERE ID IN(SELECT CUSTOMER_ID FROM TRANSFER_ORDER ORDER BY CREATE_STAMP DESC) LIMIT 0,5)");
        renderJson(contactjson);
    }

    // 客户列表,列出最近使用的5个供应商
    public void selectServiceProvider() {
        List<Contact> contactjson = Contact.dao
                .find("SELECT * FROM CONTACT WHERE ID IN(SELECT CONTACT_ID FROM PARTY WHERE ID IN(SELECT SP_ID FROM TRANSFER_ORDER ORDER BY CREATE_STAMP DESC) LIMIT 0,5)");
        renderJson(contactjson);
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
            // setContact(contact);
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
            // setContact(contact);
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
        if (LoginUserController.isAuthenticated(this))
            render("transferOrder/transferOrderList.html");
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
        TransferOrder transferOrder = null;
        if (order_id == null || "".equals(order_id)) {
            transferOrder = new TransferOrder();
            Party customer = Party.dao.findById(getPara("customer_id"));
            transferOrder.set("customer_id", customer.get("id"));
            Party sp = Party.dao.findById(getPara("sp_id"));
            transferOrder.set("sp_id", sp.get("id"));
            transferOrder.set("status", getPara("status"));
            transferOrder.set("order_no", getPara("order_no"));
            transferOrder.set("create_by", getPara("create_by"));
            transferOrder.set("cargo_nature", getPara("cargoNature"));
            transferOrder.set("pickup_mode", getPara("pickupMode"));
            transferOrder.set("arrival_mode", getPara("arrivalMode"));
            transferOrder.set("address", getPara("address"));
            transferOrder.set("create_stamp", new Date());
            transferOrder.set("remark", getPara("remark"));
            transferOrder.set("route_from", getPara("route_from"));
            transferOrder.set("route_to", getPara("route_to"));
            transferOrder.set("order_type", getPara("orderType"));
            transferOrder.set("customer_province", getPara("customerProvince"));
            transferOrder.set("car_size", getPara("car_size"));
            transferOrder.set("car_no", getPara("car_no"));
            transferOrder.set("car_type", getPara("car_type"));
            transferOrder.set("assign_status", TransferOrder.ASSIGN_STATUS_NEW);

            if (getPara("arrivalMode") != null
                    && getPara("arrivalMode").equals("delivery")) {
                // 到达方式为货品直送时把warehouseId置为null
                warehouseId = null;
                Party party = null;
                Party dirver = null;
                String notifyPartyId = getPara("notify_party_id");
                String driverId = getPara("driver_id");
                if (notifyPartyId == null || "".equals(notifyPartyId)) {
                    party = saveContact();
                } else {
                    party = updateContact(notifyPartyId);
                }
                if (driverId == null || "".equals(driverId)) {
                	dirver = saveDriver();
                } else {
                	dirver = updateDriver(driverId);
                }
                transferOrder.set("notify_party_id", party.get("id"));
                transferOrder.set("driver_id", dirver.get("id"));
            }
            if (warehouseId != null && !"".equals(warehouseId)) {
                transferOrder.set("warehouse_id", warehouseId);
            }
            if (officeId != null && !"".equals(officeId)) {
                transferOrder.set("office_id", officeId);
            }
            transferOrder.save();
            // 如果是货品直送,则需生成一张发车单
            if(transferOrder.get("arrival_mode").equals("delivery")){
            	createDepartOrder(transferOrder);
            }
            saveTransferOrderMilestone(transferOrder);
        } else {
            transferOrder = TransferOrder.dao.findById(order_id);
            transferOrder.set("customer_id", getPara("customer_id"));
            transferOrder.set("sp_id", getPara("sp_id"));
            transferOrder.set("order_no", getPara("order_no"));
            transferOrder.set("create_by", getPara("create_by"));
            transferOrder.set("cargo_nature", getPara("cargoNature"));
            transferOrder.set("pickup_mode", getPara("pickupMode"));
            transferOrder.set("arrival_mode", getPara("arrivalMode"));
            transferOrder.set("address", getPara("address"));
            transferOrder.set("create_stamp", new Date());
            transferOrder.set("remark", getPara("remark"));
            transferOrder.set("route_from", getPara("route_from"));
            transferOrder.set("route_to", getPara("route_to"));
            transferOrder.set("order_type", getPara("orderType"));
            transferOrder.set("customer_province", getPara("customerProvince"));
            transferOrder.set("car_size", getPara("car_size"));
            transferOrder.set("car_no", getPara("car_no"));
            transferOrder.set("car_type", getPara("car_type"));
            transferOrder.set("assign_status", TransferOrder.ASSIGN_STATUS_NEW);

            if (getPara("arrivalMode") != null
                    && getPara("arrivalMode").equals("delivery")) {
                // 到达方式为货品直送时把warehouseId置为null
                warehouseId = null;
                Party party = null;
                Party dirver = null;
                String notifyPartyId = getPara("notify_party_id");
                String driverId = getPara("driver_id");
                if (notifyPartyId == null || "".equals(notifyPartyId)) {
                    party = saveContact();
                } else {
                    party = updateContact(notifyPartyId);
                }
                if (driverId == null || "".equals(driverId)) {
                	dirver = saveDriver();
                } else {
                	dirver = updateDriver(driverId);
                }
                transferOrder.set("notify_party_id", party.get("id"));
                transferOrder.set("driver_id", dirver.get("id"));
            }else{
            	transferOrder.set("notify_party_id", null);            	
            	transferOrder.set("driver_id", null);            	
            }
            if (warehouseId != null && !"".equals(warehouseId)) {
                transferOrder.set("warehouse_id", warehouseId);
            }
            if (officeId != null && !"".equals(officeId)) {
                transferOrder.set("office_id", officeId);
            }
            transferOrder.update();
            // 如果是货品直送,则需判断是否新建一张发车单
            if(transferOrder.get("arrival_mode").equals("delivery")){
            	DepartTransferOrder departTransferOrder = DepartTransferOrder.dao.findFirst("select * from depart_transfer where order_id = ?", transferOrder.get("id"));
            	if(departTransferOrder == null){
            		createDepartOrder(transferOrder);
            	}else{
            		updateDepartOrder(transferOrder, departTransferOrder);
            	}
            }else{
            	deleteDepartOrder(transferOrder);
            }
        }
        renderJson(transferOrder);
    }

    // 删除发车单
    private void deleteDepartOrder(TransferOrder transferOrder) {
    	DepartTransferOrder departTransferOrder = DepartTransferOrder.dao.findFirst("select * from depart_transfer where order_id = ?", transferOrder.get("id"));
    	if(departTransferOrder != null){
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
		UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
		String creat_id = users.get("id").toString();
		Date createDate = Calendar.getInstance().getTime();
		DepartOrder departOrder = new DepartOrder();			
		departOrder.set("create_by", Integer.parseInt(creat_id)).set("create_stamp", createDate)
			.set("combine_type", "DEPART").set("depart_no", depart_no)
			.set("car_no", transferOrder.get("car_no")).set("car_type", transferOrder.get("car_type")).set("car_size", transferOrder.get("car_size"));
		departOrder.set("driver_id", transferOrder.get("driver_id"));
		departOrder.save();
		
		DepartTransferOrder departTransferOrder = new DepartTransferOrder();
		departTransferOrder.set("depart_id", departOrder.get("id"));
		departTransferOrder.set("order_id", transferOrder.get("id"));
		departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
		departTransferOrder.save();
	}
    
    // 更新发车单
    private void updateDepartOrder(TransferOrder transferOrder, DepartTransferOrder departTransferOrder) {
    	String depart_no = creatDepartNo();
    	String name = (String) currentUser.getPrincipal();
    	UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
    	String creat_id = users.get("id").toString();
    	Date createDate = Calendar.getInstance().getTime();
    	DepartOrder departOrder = DepartOrder.dao.findById(departTransferOrder.get("depart_id"));
    	departOrder.set("create_by", Integer.parseInt(creat_id)).set("create_stamp", createDate)
			.set("combine_type", "DEPART").set("depart_no", depart_no)
			.set("car_no", transferOrder.get("car_no")).set("car_type", transferOrder.get("car_type")).set("car_size", transferOrder.get("car_size"));
		departOrder.set("driver_id", transferOrder.get("driver_id"));
		departOrder.update();
		
		departTransferOrder.set("depart_id", departOrder.get("id"));
		departTransferOrder.set("order_id", transferOrder.get("id"));
		departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
		departTransferOrder.update();
    }

    // 创建发车单序列号
	private String creatDepartNo() {
		String order_no = null;
		String the_order_no = null;
		DepartOrder order = DepartOrder.dao.findFirst("select * from DEPART_ORDER where  COMBINE_TYPE= '"
				+ DepartOrder.COMBINE_TYPE_DEPART + "' order by DEPART_no desc limit 0,1");
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
        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
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
    
    // 保存司机
    public Party saveDriver() {
    	Party party = new Party();
    	Contact contact = setDriver();
    	party.set("contact_id", contact.getLong("id"));
    	party.set("create_date", new Date());
    	party.set("creator", currentUser.getPrincipal());
    	party.set("party_type", Party.PARTY_TYPE_DRIVER);
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
    
    // 更新司机party
    public Party updateDriver(String driverId) {
    	Party party = Party.dao.findById(driverId);
    	Contact contact = editDriver(party);
    	party.set("create_date", new Date());
    	party.set("creator", currentUser.getPrincipal());
    	party.set("party_type", Party.PARTY_TYPE_DRIVER);
    	party.update();
    	return party;
    }

    // 保存联系人
    private Contact setContact() {
        Contact contact = new Contact();
        contact.set("company_name", getPara("notify_company_name"));
        contact.set("contact_person", getPara("notify_contact_person"));
        contact.set("phone", getPara("notify_phone"));
        contact.set("address", getPara("notify_address"));
        contact.set("location", getPara("notify_location"));
        contact.save();
        return contact;
    }
    
    // 保存司机
    private Contact setDriver() {
    	Contact contact = new Contact();
    	contact.set("contact_person", getPara("driver_name"));
    	contact.set("phone", getPara("driver_phone"));
    	contact.save();
    	return contact;
    }

    // 更新联系人
    private Contact editContact(Party party) {
        Contact contact = Contact.dao.findById(party.get("contact_id"));
        contact.set("company_name", getPara("notify_company_name"));
        contact.set("contact_person", getPara("notify_contact_person"));
        contact.set("phone", getPara("notify_phone"));
        contact.set("address", getPara("notify_address"));
        contact.set("location", getPara("notify_location"));
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
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER' and (company_name like '%"
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
        }
        renderJson(locationList);
    }

    // 查找供应商
    public void searchSp() {
        String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'SERVICE_PROVIDER' and (company_name like '%"
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
        }
        renderJson(locationList);
    }
    
    // 查找序列号
    public void searchItemNo() {
    	String input = getPara("input");
    	List<Record> locationList = Collections.EMPTY_LIST;
    	if (input.trim().length() > 0) {
    		locationList = Db.find("select * from product where item_no like '%"+ input+ "%' limit 0,10");
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
        if (LoginUserController.isAuthenticated(this))
            redirect("/yh/transferOrder");
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
        Location l = Location.dao
                .findFirst("SELECT * FROM LOCATION where code = (select pcode from location where CODE = '"
                        + code + "')");
        Location location = null;
        if (provinces.contains(l)) {
            location = Location.dao
                    .findFirst("SELECT l.name as CITY,l1.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                            + code + "'");
        } else {
            location = Location.dao
                    .findFirst("SELECT l.name as DISTRICT, l1.name as CITY,l2.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
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
         List<Record> locationList = Collections.EMPTY_LIST;
         if (input.trim().length() > 0) {
             locationList = Db
                     .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"+Party.PARTY_TYPE_DRIVER+"' and (contact_person like '%"+ input +"%' or phone like '%"+ input + "%') limit 0,10");
         }
         renderJson(locationList);
    }
}
