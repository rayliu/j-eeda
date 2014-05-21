package controllers.yh.order;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class TransferOrderController extends Controller {

    private Logger logger = Logger.getLogger(TransferOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
    	if(LoginUserController.isAuthenticated(this))
        render("transferOrder/transferOrderList.html");
    }

	public void list() {
    	Map transferOrderListMap = null;
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String address = getPara("address");
        String customer = getPara("customer");
        String sp = getPara("sp");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        if(orderNo == null && status == null && address == null && customer == null && sp == null && beginTime == null && endTime == null){
	        String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	
	        String sqlTotal = "select count(1) total from transfer_order";
	        Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        String sql = "select to.*,c1.company_name cname,c2.company_name spname,to.create_stamp from transfer_order to "
							+" left join party p1 on to.customer_id = p1.id "
							+" left join party p2 on to.sp_id = p2.id "
							+" left join contact c1 on p1.contact_id = c1.id"
							+" left join contact c2 on p2.contact_id = c2.id";
	
	        List<Record> transferOrders = Db.find(sql);
	        
	        transferOrderListMap = new HashMap();
	        transferOrderListMap.put("sEcho", pageIndex);
	        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
	        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
	
	        transferOrderListMap.put("aaData", transferOrders);
        }else{
            if(beginTime == null || "".equals(beginTime)){
            	beginTime = "1-1-1";
            }
            if(endTime == null || "".equals(endTime)){
            	endTime = "9999-12-31";
            }
        	String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	
	        String sqlTotal = "select count(1) total from transfer_order to "
							+" left join party p1 on to.customer_id = p1.id "
							+" left join party p2 on to.sp_id = p2.id "
							+" left join contact c1 on p1.contact_id = c1.id"
							+" left join contact c2 on p2.contact_id = c2.id where to.order_no like '%"+orderNo+"%' and to.status like '%"+status+"%' and to.address like '%"+address+"%' and c1.COMPANY_NAME like '%"+customer+"%' and c2.COMPANY_NAME  like '%"+sp+"%' and create_stamp between '"+beginTime+"' and '"+endTime+"'";
	        Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        String sql = "select to.*,c1.company_name cname,c2.company_name spname from transfer_order to "
							+" left join party p1 on to.customer_id = p1.id "
							+" left join party p2 on to.sp_id = p2.id "
							+" left join contact c1 on p1.contact_id = c1.id"
							+" left join contact c2 on p2.contact_id = c2.id where to.order_no like '%"+orderNo+"%' and to.status like '%"+status+"%' and to.address like '%"+address+"%' and c1.COMPANY_NAME like '%"+customer+"%' and c2.COMPANY_NAME  like '%"+sp+"%' and create_stamp between '"+beginTime+"' and '"+endTime+"'";
	
	        List<Record> transferOrders = Db.find(sql);
	        
	        transferOrderListMap = new HashMap();
	        transferOrderListMap.put("sEcho", pageIndex);
	        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
	        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
	
	        transferOrderListMap.put("aaData", transferOrders);
        }

        renderJson(transferOrderListMap);
    }

    public void add() {
        setAttr("saveOK", false);
        TransferOrder transferOrder = new TransferOrder();
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));

        TransferOrder order = TransferOrder.dao.findFirst("select * from transfer_order order by order_no desc limit 0,1");
        if (order != null) {
            String num = order.get("order_no");
            String order_no = String.valueOf((Long.parseLong(num) + 1));
            setAttr("order_no", order_no);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            String order_no = format + "00001";
            setAttr("order_no", order_no);
        }

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);

        setAttr("status", "新建");
        if(LoginUserController.isAuthenticated(this))
        render("transferOrder/editTransferOrder.html");
        // render("transferOrder/transferOrderEdit.html");
    }

    public void edit1() {
        long id = getParaToLong();

        Party party = Party.dao.findById(id);
        setAttr("party", party);

        Contact contact = Contact.dao.findFirst("select * from contact where id=?", party.getLong("contact_id"));
        setAttr("contact", contact);
        if(LoginUserController.isAuthenticated(this))
        render("transferOrder/transferOrderEdit.html");
    }

    public void edit() {
        long id = getParaToLong();
        TransferOrder transferOrder = TransferOrder.dao.findById(id);
        setAttr("transferOrder", transferOrder);

        Party customer = Party.dao.findById(transferOrder.get("customer_id"));
        Contact customerContact = Contact.dao.findById(customer.get("contact_id"));
        setAttr("customerContact", customerContact);
        Party sp = Party.dao.findById(transferOrder.get("sp_id"));
        Contact spContact = Contact.dao.findById(sp.get("contact_id"));
        setAttr("spContact", spContact);
        Long notify_party_id = transferOrder.get("notify_party_id");
        if (notify_party_id != null) {
            Party notify = Party.dao.findById(notify_party_id);
            Contact contact = Contact.dao.findById(notify.get("contact_id"));
            setAttr("contact", contact);
        } else {
            setAttr("contact", null);
        }

        UserLogin userLogin = UserLogin.dao.findById(transferOrder.get("create_by"));
        setAttr("userLogin", userLogin);
        if(LoginUserController.isAuthenticated(this))
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
        if(LoginUserController.isAuthenticated(this))
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
    	if(LoginUserController.isAuthenticated(this))
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
        TransferOrder transferOrder = null;
        if (order_id.isEmpty()) {
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

            if (getPara("arrivalMode") != null && getPara("arrivalMode").equals("货品直送")) {
                Party party = saveContact();
                transferOrder.set("notify_party_id", party.get("id"));
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

            if (getPara("arrivalMode") != null && getPara("arrivalMode").equals("delivery")) {
                Party party = saveContact();
                transferOrder.set("notify_party_id", party.get("id"));
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

    // 保存联系人
    private Contact setContact() {
        Contact contact = new Contact();
        contact.set("company_name", getPara("notify_company_name"));
        contact.set("contact_person", getPara("notify_contact_person"));
        contact.set("phone", getPara("notify_phone"));
        contact.set("address", getPara("notify_address"));
        contact.save();
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
        if(LoginUserController.isAuthenticated(this))
        redirect("/yh/transferOrder");
    }
    
    // 取消
    public void cancel() {
        String id = getPara();
        TransferOrder.dao.findById(id).set("Status", "取消").update();
        renderJson("{\"success\":true}");
    }
}
