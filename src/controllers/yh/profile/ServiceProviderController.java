package controllers.yh.profile;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ServiceProviderController extends Controller {

    private Logger logger = Logger.getLogger(ServiceProviderController.class);

    public void index() {
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

        String sql = "select p.id, p.creator, p.create_date, c.company_name, c.contact_person from party p, contact c where p.party_type='SERVICE_PROVIDER' and p.contact_id=c.id order by p.create_date desc ";

        List<Record> customers = Db.find(sql);

        Map customerListMap = new HashMap();
        customerListMap.put("sEcho", pageIndex);
        customerListMap.put("iTotalRecords", rec.getLong("total"));
        customerListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        customerListMap.put("aaData", customers);

        renderJson(customerListMap);
    }

    public void add() {
        setAttr("saveOK", false);
        render("profile/serviceProvider/serviceProviderEdit.html");
    }

    public void edit() {
        long id = getParaToLong();

        Party party = Party.dao.findById(id);
        setAttr("party", party);

        Contact contact = Contact.dao
                .findFirst("select * from contact where id=?",
                        party.getLong("contact_id"));
        setAttr("contact", contact);

        render("profile/serviceProvider/serviceProviderEdit.html");
    }

    public void delete() {
        long id = getParaToLong();

        Party party = Party.dao.findById(id);
        party.delete();

        Contact contact = Contact.dao
                .findFirst("select * from contact where id=?",
                        party.getLong("contact_id"));
        contact.delete();

        redirect("/yh/serviceProvider");
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
        render("profile/serviceProvider/serviceProviderList.html");
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

}
