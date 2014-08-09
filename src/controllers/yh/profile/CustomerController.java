package controllers.yh.profile;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.Party;
import models.TransferOrder;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class CustomerController extends Controller {

    private Logger logger = Logger.getLogger(CustomerController.class);
    Subject currentUser = SecurityUtils.getSubject();

    // in config route已经将路径默认设置为/yh
    // me.add("/yh", controllers.yh.AppController.class, "/yh");
    public void index() {
        if (LoginUserController.isAuthenticated(this))
            render("profile/customer/CustomerList.html");
    }

    public void list() {
        String company_name = getPara("COMPANY_NAME");
        String contact_person = getPara("CONTACT_PERSON");
        String receipt = getPara("RECEIPT");
        String abbr = getPara("ABBR");
        String address = getPara("ADDRESS");
        String location = getPara("LOCATION");
        if (company_name == null && contact_person == null && receipt == null && abbr == null && address == null
                && location == null) {
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }

            String sqlTotal = "select count(1) total from party where party_type='CUSTOMER'";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select p.*,c.*,p.id as pid,l.name,trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname from party p "
                    + "left join contact c on p.contact_id=c.id "
                    + "left join location l on l.code=c.location "
                    + "left join location  l1 on l.pcode =l1.code "
                    + "left join location l2 on l1.pcode = l2.code "
                    + "where p.party_type='CUSTOMER' order by p.create_date desc " + sLimit;
            List<Record> customers = Db.find(sql);

            Map customerListMap = new HashMap();
            customerListMap.put("sEcho", pageIndex);
            customerListMap.put("iTotalRecords", rec.getLong("total"));
            customerListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            customerListMap.put("aaData", customers);
            renderJson(customerListMap);
        } else {

            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }

            String sqlTotal = "select count(1) total from party where party_type='CUSTOMER'";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select p.*,c.*,p.id as pid,l.name,trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname from party p "
                    + "left join contact c on p.contact_id=c.id "
                    + "left join location l on l.code=c.location "
                    + "left join location  l1 on l.pcode =l1.code "
                    + "left join location l2 on l1.pcode = l2.code "
                    + "where p.party_type='CUSTOMER' "
                    + "and ifnull(c.company_name,'') like '%"
                    + company_name
                    + "%' and ifnull(c.contact_person,'') like '%"
                    + contact_person
                    + "%' and ifnull(p.receipt,'') like '%"
                    + receipt
                    + "%' and ifnull(c.address,'') like '%"
                    + address
                    + "%' and ifnull(c.abbr,'') like '%" + abbr + "%' order by p.create_date desc " + sLimit;

            List<Record> customers = Db.find(sql);

            Map customerListMap = new HashMap();
            customerListMap.put("sEcho", pageIndex);
            customerListMap.put("iTotalRecords", rec.getLong("total"));
            customerListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            customerListMap.put("aaData", customers);
            renderJson(customerListMap);
        }
    }

    public void add() {
        setAttr("saveOK", false);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/profile/customer/CustomerEdit.html");
    }

    public void edit() {
        if (!LoginUserController.isAuthenticated(this))
            return;

        String id = getPara();

        Party party = Party.dao.findById(id);
        Contact locationCode = Contact.dao.findById(party.get("contact_id"), "location");
        String code = locationCode.get("location");

        List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
        Location l = Location.dao
                .findFirst("select * from location where code = (select pcode from location where code = '" + code
                        + "')");
        Location location = null;
        if (provinces.contains(l)) {
            location = Location.dao
                    .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                            + code + "'");
        } else {
            location = Location.dao
                    .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                            + code + "'");
        }
        setAttr("location", location);

        setAttr("party", party);

        Contact contact = Contact.dao.findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id="
                + id);
        setAttr("contact", contact);

        render("profile/customer/CustomerEdit.html");
    }

    public void delete() {
        long id = getParaToLong();

        Party party = Party.dao.findById(id);
        List<TransferOrder> transferOrders = TransferOrder.dao.find("select * from transfer_order where sp_id="
                + party.get("id"));
        for (TransferOrder transferOrder : transferOrders) {
            transferOrder.set("sp_id", null);
            transferOrder.update();
        }
        List<DeliveryOrder> deliveryOrders = DeliveryOrder.dao.find("select * from delivery_order where sp_id="
                + party.get("id"));
        for (DeliveryOrder deliveryOrder : deliveryOrders) {
            deliveryOrder.set("sp_id", null);
            deliveryOrder.update();
        }

        Contact contact = Contact.dao.findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id="
                + id);
        ;
        contact.delete();

        party.delete();
        if (LoginUserController.isAuthenticated(this))
            redirect("/yh/customer");
    }

    public void save() {

        String id = getPara("party_id");
        Party party = null;
        Contact contact = null;
        Date createDate = Calendar.getInstance().getTime();
        if (!"".equals(id) && id != null) {
            party = Party.dao.findById(id);
            party.set("last_update_date", createDate);
            party.set("remark", getPara("remark"));
            party.set("payment", getPara("payment"));
            party.set("receipt", getPara("receipt"));
            party.set("charge_type", getPara("chargeType"));
            party.update();

            contact = Contact.dao.findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id=" + id);
            setContact(contact);
            contact.update();
        } else {
            contact = new Contact();
            setContact(contact);
            contact.save();

            party = new Party();
            party.set("party_type", Party.PARTY_TYPE_CUSTOMER);
            party.set("contact_id", contact.getLong("id"));
            party.set("creator", currentUser.getPrincipal());
            party.set("create_date", createDate);
            party.set("remark", getPara("remark"));
            party.set("receipt", getPara("receipt"));
            party.set("payment", getPara("payment"));
            party.set("charge_type", getPara("chargeType"));
            party.save();

        }

        setAttr("saveOK", true);
        if (LoginUserController.isAuthenticated(this))
            redirect("/yh/customer");
    }

    private void setContact(Contact contact) {
        contact.set("company_name", getPara("company_name"));
        contact.set("contact_person", getPara("contact_person"));
        contact.set("email", getPara("email"));
        contact.set("abbr", getPara("abbr"));
        contact.set("location", getPara("location"));
        contact.set("introduction", getPara("introduction"));
        contact.set("mobile", getPara("mobile"));
        contact.set("phone", getPara("phone"));
        contact.set("address", getPara("address"));
        contact.set("city", getPara("city"));
        contact.set("postal_code", getPara("postal_code"));
    }

    /*
     * public void location() { String id = getPara(); Contact contact =
     * Contact.dao.findById(id); String code = contact.get("location"); if (code
     * != null) { List<Record> transferOrders = Db .find(
     * "SELECT trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code="
     * + code); renderJson(transferOrders); } else { renderJson(0); }
     * 
     * }
     */
}
