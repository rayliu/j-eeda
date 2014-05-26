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

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class ServiceProviderController extends Controller {

    private Logger logger = Logger.getLogger(ServiceProviderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
        if (LoginUserController.isAuthenticated(this))
            render("profile/serviceProvider/serviceProviderList.html");
    }

    public void list() {
        /*
         * Paging
         */
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from party where party_type='SERVICE_PROVIDER'";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select *,p.id as pid from party p, contact c where p.party_type='SERVICE_PROVIDER' and p.contact_id=c.id order by p.create_date desc ";
        List<Record> customers = Db.find(sql);

        String code = "";
        for (int i = 0; i < customers.size(); i++) {

            code = customers.get(i).get("location");
            String sql2 = "SELECT trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code='"
                    + code + "'";
            List<Record> customers2 = Db.find(sql2);
            String id = "";
            try {
                id = customers2.get(0).get("dname");
            } catch (Exception e) {
                // TODO: handle exception
                customers.get(i).set("dname", null);
            }

            customers.get(i).set("dname", id);

        }

        Map customerListMap = new HashMap();
        customerListMap.put("sEcho", pageIndex);
        customerListMap.put("iTotalRecords", rec.getLong("total"));
        customerListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        customerListMap.put("aaData", customers);

        renderJson(customerListMap);
    }

    public void add() {
        setAttr("saveOK", false);
        if (LoginUserController.isAuthenticated(this))
            render("profile/serviceProvider/serviceProviderEdit.html");
    }

    public void edit() {
        long id = getParaToLong();

        Party party = Party.dao.findById(id);
        Contact locationCode = Contact.dao.findById(party.get("contact_id"),
                "location");
        String code = locationCode.get("location");

        Location location = Location.dao
                .findFirst("SELECT l.name as DISTRICT, l1.name as CITY,l2.name as PROVINCE,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                        + code + "'");
        System.out.println(location);
        setAttr("location", location);

        setAttr("party", party);

        Contact contact = Contact.dao
                .findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id="
                        + id);
        setAttr("contact", contact);
        if (LoginUserController.isAuthenticated(this))
            render("profile/serviceProvider/serviceProviderEdit.html");
    }

    public void delete() {
        // long id = getParaToLong();
        String id = getPara();
        // Db.deleteById("contract", id);
        Party party = Party.dao.findById(id);

        List<TransferOrder> transferOrders = TransferOrder.dao
                .find("select * from transfer_order where sp_id="
                        + party.get("id"));
        for (TransferOrder transferOrder : transferOrders) {
            transferOrder.set("sp_id", null);
            transferOrder.set("customer_id", null);
            transferOrder.update();
        }
        List<DeliveryOrder> deliveryOrders = DeliveryOrder.dao
                .find("select * from delivery_order where sp_id="
                        + party.get("id"));
        for (DeliveryOrder deliveryOrder : deliveryOrders) {
            deliveryOrder.set("sp_id", null);
            deliveryOrder.set("customer_id", null);
            deliveryOrder.update();
        }

        Contact contact = Contact.dao
                .findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id="
                        + id);

        contact.delete();
        party.delete();
        if (LoginUserController.isAuthenticated(this))
            redirect("/yh/serviceProvider");
    }

    public void save() {

        String id = getPara("party_id");
        Party party = null;
        Contact contact = null;
        Date createDate = Calendar.getInstance().getTime();
        if (id != null && !id.equals("")) {
            party = Party.dao.findById(id);
            party.set("last_update_date", createDate);
            party.set("remark", getPara("remark"));
            party.update();

            contact = Contact.dao
                    .findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id="
                            + id);
            ;
            setContact(contact);
            contact.update();
        } else {
            contact = new Contact();
            setContact(contact);
            contact.save();
            party = new Party();
            party.set("party_type", Party.PARTY_TYPE_SERVICE_PROVIDER);
            party.set("contact_id", contact.getLong("id"));
            party.set("creator", currentUser.getPrincipal());
            party.set("create_date", createDate);
            party.set("remark", getPara("remark"));
            party.save();

        }

        setAttr("saveOK", true);
        if (LoginUserController.isAuthenticated(this))
        	redirect("/yh/serviceProvider");
    }

    private void setContact(Contact contact) {
        contact.set("company_name", getPara("company_name"));
        contact.set("contact_person", getPara("contact_person"));
        contact.set("location", getPara("location"));
        contact.set("email", getPara("email"));
        contact.set("abbr", getPara("abbr"));
        contact.set("mobile", getPara("mobile"));
        contact.set("phone", getPara("phone"));
        contact.set("address", getPara("address"));
        contact.set("introduction", getPara("introduction"));
        contact.set("city", getPara("city"));
        contact.set("postal_code", getPara("postal_code"));
    }

    public void province() {
        List<Record> locationList = Db
                .find("select * from location where pcode ='1'");
        renderJson(locationList);
    }

    public void city() {
        String cityId = getPara("id");
        System.out.println(cityId);
        List<Record> locationList = Db
                .find("select * from location where pcode ='" + cityId + "'");
        renderJson(locationList);
    }

    public void area() {
        String areaId = getPara("id");
        System.out.println(areaId);
        List<Record> locationList = Db
                .find("select * from location where pcode ='" + areaId + "'");
        renderJson(locationList);
    }

    public void searchAllCity() {
        String province = getPara("province");
        List<Location> locations = Location.dao
                .find("select * from location where name in (select name from location where pcode=(select code from location where name = '"
                        + province + "'))");
        renderJson(locations);
    }

    public void searchAllDistrict() {
        String city = getPara("city");
        List<Location> locations = Location.dao
                .find("select * from location where pcode=(select code from location where name = '"
                        + city + "')");
        renderJson(locations);
    }
}
