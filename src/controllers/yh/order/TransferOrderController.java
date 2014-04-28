package controllers.yh.order;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.Party;
import models.TransferOrder;
import models.TransferOrderItem;
import models.UserLogin;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class TransferOrderController extends Controller {

    private Logger logger = Logger.getLogger(TransferOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
        render("transferOrder/transferOrderList.html");
    }

    public void list() {
        /*
         * Paging
         */
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from transfer_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from transfer_order";

        List<Record> transferOrders = Db.find(sql);

        Map transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);

        renderJson(transferOrderListMap);
    }

    public void add() {
        setAttr("saveOK", false);
        render("transferOrder/editTransferOrder.html");
        // render("transferOrder/transferOrderEdit.html");
    }

    public void edit1() {
        long id = getParaToLong();

        Party party = Party.dao.findById(id);
        setAttr("party", party);

        Contact contact = Contact.dao.findFirst("select * from contact where id=?", party.getLong("contact_id"));
        setAttr("contact", contact);

        render("transferOrder/transferOrderEdit.html");
    }

    public void edit() {
        render("transferOrder/editTransferOrder.html");
    }

    public void delete() {
        long id = getParaToLong();

        Party party = Party.dao.findById(id);
        party.delete();

        Contact contact = Contact.dao.findFirst("select * from contact where id=?", party.getLong("contact_id"));
        contact.delete();

        redirect("/yh/transferOrder");
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
            setContact(contact);
            contact.update();
        } else {
            contact = new Contact();
            setContact(contact);
            contact.save();
            party = new Party();
            party.set("party_type", Party.PARTY_TYPE_SERVICE_PROVIDER);
            party.set("contact_id", contact.getLong("id"));
            party.set("creator", "test");
            party.set("create_date", createDate);
            party.save();

        }

        setAttr("saveOK", true);
        render("transferOrder/transferOrderList.html");
    }

    private void setContact(Contact contact) {
        contact.set("company_name", getPara("company_name"));
        contact.set("contact_person", getPara("contact_person"));
        contact.set("email", getPara("email"));
        contact.set("mobile", getPara("mobile"));
        contact.set("phone", getPara("phone"));
        contact.set("address", getPara("address"));
        contact.set("city", getPara("city"));
        contact.set("postal_code", getPara("postal_code"));
    }

    // 客户列表,列出最近使用的5个客户
    public void selectCustomer() {
        List<Contact> contactjson = Contact.dao
                .find("select * from contact c  where id in (select contact_id from party where party_type='CUSTOMER' order by last_update_date desc limit 0,5)");
        renderJson(contactjson);
    }

    // 客户列表,列出最近使用的5个供应商
    public void selectServiceProvider() {
        List<Contact> contactjson = Contact.dao
                .find("select * from contact c  where id in (select contact_id from party where party_type='SERVICE_PROVIDER' order by last_update_date desc limit 0,5)");
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
            setContact(contact);
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
            setContact(contact);
            contact.save();
            party.set("contact_id", contact.getLong("id"));
            party.set("create_date", new Date());
            party.set("creator", currentUser.getPrincipal());
            party.save();
        }
        renderJson(party.get("id"));
    }

    // 保存联系人
    public void saveContact() {
        String notify_party_id = getPara("notify_party_id");
        Party party = null;
        if (notify_party_id != null && !notify_party_id.equals("")) {
            party = Party.dao.findById(notify_party_id);
        } else {
            party = new Party();
            Contact contact = new Contact();
            setContact(contact);
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

    // TODO saveItem的同时对整个表单进行保存?如果是的话,那么运输单中需维护customer_id,sp_id,contact_id
    // 这些外键需在页面中传递,传的方式以及获取的方式未知?
    // 在新建页面不能直接显示,需重新进入才能显示刚添加的.
    // 保存货品属性的信息
    public void saveItem() {
        TransferOrder transferOrder = new TransferOrder();
        transferOrder.set("customer_id", getPara("customer_id"));
        transferOrder.set("sp_id", getPara("sp_id"));
        transferOrder.set("notify_party_id", getPara("notify_party_id"));
        transferOrder.set("cargo_nature", getPara("cargo_nature"));
        transferOrder.set("pickup_mode", getPara("pickup_mode"));
        transferOrder.set("arrival_mode", getPara("arrival_mode"));
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrder.set("create_by", users.get(0).get("id"));
        transferOrder.set("create_stamp", new Date());
        transferOrder.set("order_no", UUID.randomUUID().toString());
        transferOrder.set("status", "订单已生成");
        saveOrderItem(transferOrder);
        transferOrder.save();
        render("transferOrder/transferOrderList.html");
    }

    // 保存订单项
    public void saveOrderItem(TransferOrder transferOrder) {
        TransferOrderItem orderItem = new TransferOrderItem();
        orderItem.set("item_name", getPara("item_name"));
        orderItem.set("item_desc", getPara("item_desc"));
        orderItem.set("amount", getPara("amount"));
        orderItem.set("unit", getPara("unit"));
        orderItem.set("volume", getPara("volume"));
        orderItem.set("weight", getPara("weight"));
        orderItem.set("remark", getPara("remark"));
        orderItem.set("order_id", transferOrder.get("id"));
        orderItem.save();
    }
}
