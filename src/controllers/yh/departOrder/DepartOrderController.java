package controllers.yh.departOrder;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class DepartOrderController extends Controller {

    private Logger logger = Logger.getLogger(DepartOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
        if (LoginUserController.isAuthenticated(this))
            render("departOrder/departOrderList.html");
    }

    public void list() {
    	String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from depart_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select * from depart_order";

		List<Record> warehouses = Db.find(sql);

		Map map = new HashMap();
		map.put("sEcho", pageIndex);
		map.put("iTotalRecords", rec.getLong("total"));
		map.put("iTotalDisplayRecords", rec.getLong("total"));

		map.put("aaData", warehouses);

		renderJson(map);
    }

    public void add() {
    	setAttr("saveOK", false);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/departOrder/allTransferOrderList.html");
    }

    public void createTransferOrderList() {
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

        String sql = "select * from transfer_order order by create_stamp desc";

        List<Record> transferOrders = Db.find(sql);

        Map transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);

        renderJson(transferOrderListMap);
    }
    
    public void edit1() {
        long id = getParaToLong();

        Party party = Party.dao.findById(id);
        setAttr("party", party);

        Contact contact = Contact.dao.findFirst("select * from contact where id=?", party.getLong("contact_id"));
        setAttr("contact", contact);
        if (LoginUserController.isAuthenticated(this))
            render("transferOrder/transferOrderEdit.html");
    }

    public void edit() {
        long id = getParaToLong();
        TransferOrder transferOrder = TransferOrder.dao.findById(id);
        setAttr("transferOrder", transferOrder);

        Long customer_id = transferOrder.get("customer_id");
        Long sp_id = transferOrder.get("sp_id");
        if(customer_id != null){
	        Party customer = Party.dao.findById(customer_id);
	        Contact customerContact = Contact.dao.findById(customer.get("contact_id"));
	        setAttr("customerContact", customerContact);
        }
        if(sp_id != null){
	        Party sp = Party.dao.findById(sp_id);
	        Contact spContact = Contact.dao.findById(sp.get("contact_id"));
	        setAttr("spContact", spContact);
        }
        Long notify_party_id = transferOrder.get("notify_party_id");
        if (notify_party_id != null) {
            Party notify = Party.dao.findById(notify_party_id);
            Contact contact = Contact.dao.findById(notify.get("contact_id"));
            setAttr("contact", contact);
            Contact locationCode = Contact.dao.findById(notify.get("contact_id"));
            String code = locationCode.get("location");

            List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
            Location l = Location.dao.findFirst("SELECT * FROM LOCATION where code = (select pcode from location where CODE = '" + code
                    + "')");
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
        if(routeFrom != null || !"".equals(routeFrom)){
            List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
            Location l = Location.dao.findFirst("SELECT * FROM LOCATION where code = (select pcode from location where CODE = '" + routeFrom
                    + "')");
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
        if(routeTo != null || !"".equals(routeTo)){
        	List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
        	Location l = Location.dao.findFirst("SELECT * FROM LOCATION where code = (select pcode from location where CODE = '" + routeTo
        			+ "')");
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

        UserLogin userLogin = UserLogin.dao.findById(transferOrder.get("create_by"));
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

            contact = Contact.dao.findFirst("select * from contact where id=?", party.getLong("contact_id"));
            // setContact(contact);
            contact.update();
        } else {
            contact = new Contact();
            // setContact(contact);
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
        // saveOrderItem(transferOrder);
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

            if (getPara("arrivalMode") != null && getPara("arrivalMode").equals("delivery")) {
            	// 到达方式为货品直送时把warehouseId置为null
            	warehouseId = null;
            	Party party = null;
            	String notifyPartyId = getPara("notify_party_id");
            	if(notifyPartyId == null || "".equals("notifyPartyId")){
            		party = saveContact();
            	}else{
            		party = updateContact(notifyPartyId);
            	}
                transferOrder.set("notify_party_id", party.get("id"));
            }
            if(warehouseId != null && !"".equals(warehouseId)){
            	transferOrder.set("warehouse_id", warehouseId);
            }
            if(officeId != null && !"".equals(officeId)){
            	transferOrder.set("office_id", officeId);
            }
            transferOrder.save();
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

            if (getPara("arrivalMode") != null && getPara("arrivalMode").equals("delivery")) {
            	// 到达方式为货品直送时把warehouseId置为null
            	warehouseId = null;
            	Party party = null;
            	String notifyPartyId = getPara("notify_party_id");
            	if(notifyPartyId == null || "".equals("notifyPartyId")){
            		party = saveContact();
            	}else{
            		party = updateContact(notifyPartyId);
            	}
                transferOrder.set("notify_party_id", party.get("id"));
            }
            if(warehouseId != null && !"".equals(warehouseId)){
            	transferOrder.set("warehouse_id", warehouseId);
            }
            if(officeId != null && !"".equals(officeId)){
            	transferOrder.set("office_id", officeId);
            }
            transferOrder.update();
        }
        renderJson(transferOrder);
    }

    // 保存运输里程碑
    private void saveTransferOrderMilestone(TransferOrder transferOrder) {
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        transferOrderMilestone.set("status", "新建");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
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
    
    // 更新收货人
    public Party updateContact(String notifyPartyId) {
    	Party party = Party.dao.findById(notifyPartyId);
    	Contact contact = editContact(party);
    	//party.set("contact_id", contact.getLong("id"));
    	party.set("create_date", new Date());
    	party.set("creator", currentUser.getPrincipal());
    	party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
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
        //UploadFile uploadFile = getFile();
        //logger.debug("上传的文件名:" + uploadFile.getFileName());
    	Map<String, List<String>> map = PoiUtils.readExcel("c:/c.xlsx");
    	for(Map.Entry<String, List<String>> entry : map.entrySet()){
    		/*for(String param : entry.getValue()){
    			System.out.println(param);
    		}*/
    		logger.debug(entry.getKey() +"   "+entry.getValue());
    		List<String> list = entry.getValue();
    	}
    	renderJson("{\"success\":true}");
    }
    
    // 根据客户查出location
    public void searchLocationFrom(){
    	String code = getPara("locationFrom");
    	List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
        Location l = Location.dao.findFirst("SELECT * FROM LOCATION where code = (select pcode from location where CODE = '"+code+"')");
        Location location = null;
        if(provinces.contains(l)){
        	location = Location.dao
	                .findFirst("SELECT l.name as CITY,l1.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
	                        + code + "'");
        }else{
        	location = Location.dao
	                .findFirst("SELECT l.name as DISTRICT, l1.name as CITY,l2.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
	                        + code + "'");
        }
       renderJson(location);
    }
    
    // 查出所有的warehouse
    public void searchAllWarehouse(){
    	List<Warehouse> warehouses = Warehouse.dao.find("select * from warehouse");
    	renderJson(warehouses);
    }
    
    // 查出所有的office
    public void searchAllOffice(){
    	List<Office> offices = Office.dao.find("select * from office");
    	renderJson(offices);
    }
}
