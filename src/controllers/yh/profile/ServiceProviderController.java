package controllers.yh.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.Office;
import models.Party;
import models.UserOffice;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ServiceProviderController extends Controller {

    private Logger logger = Logger.getLogger(ServiceProviderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    String userName = currentUser.getPrincipal().toString();
    UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
    Office parentOffice = Office.dao.findFirst("select * from office where id = ?",currentoffice.get("office_id"));
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_P_LIST})
    public void index() {
        render("/yh/profile/serviceProvider/serviceProviderList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_P_LIST})
    public void list() {
        String company_name = getPara("COMPANY_NAME");
        String contact_person = getPara("CONTACT_PERSON");
        String receipt = getPara("RECEIPT");
        String abbr = getPara("ABBR");
        String address = getPara("ADDRESS");
        String location = getPara("LOCATION");
        
        
        Long parentID = parentOffice.get("belong_office");
        if(parentID == null || "".equals(parentID)){
        	parentID = parentOffice.getLong("id");
        }
        
        if (company_name == null && contact_person == null && receipt == null && abbr == null && address == null
                && location == null) {
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }
            String sqlTotal = "select count(1) total from party where party_type='SERVICE_PROVIDER' and office_id = " + parentID;
            Record rec = Db.findFirst(sqlTotal); 
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select p.*,c.*,p.id as pid,l.name,trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname from party p "
                    + "left join contact c on p.contact_id=c.id "
                    + "left join location l on l.code=c.location "
                    + "left join location  l1 on l.pcode =l1.code "
                    + "left join location l2 on l1.pcode = l2.code "
                    + "where p.party_type='SERVICE_PROVIDER' and p.office_id = " + parentID + " order by p.create_date desc " + sLimit;
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
            String sqlTotal = "select count(*) total from party p "
                    + "left join contact c on p.contact_id=c.id "
                    + "left join location l on l.code=c.location "
                    + "left join location  l1 on l.pcode =l1.code "
                    + "left join location l2 on l1.pcode = l2.code "
                    + "where p.party_type='SERVICE_PROVIDER' "
                    + "and ifnull(c.company_name,'') like '%"
                    + company_name
                    + "%' and ifnull(c.contact_person,'') like '%"
                    + contact_person
                    + "%' and ifnull(p.receipt,'') like '%"
                    + receipt
                    + "%' and ifnull(c.address,'') like '%"
                    + address
                    + "%' and ifnull(c.abbr,'') like '%" + abbr + "%' and p.office_id = " + parentID ;
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select p.*,c.*,p.id as pid,l.name,trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname from party p "
                    + "left join contact c on p.contact_id=c.id "
                    + "left join location l on l.code=c.location "
                    + "left join location  l1 on l.pcode =l1.code "
                    + "left join location l2 on l1.pcode = l2.code "
                    + "where p.party_type='SERVICE_PROVIDER' "
                    + "and ifnull(c.company_name,'') like '%"
                    + company_name
                    + "%' and ifnull(c.contact_person,'') like '%"
                    + contact_person
                    + "%' and ifnull(p.receipt,'') like '%"
                    + receipt
                    + "%' and ifnull(c.address,'') like '%"
                    + address
                    + "%' and ifnull(c.abbr,'') like '%" + abbr + "%' and p.office_id = " + parentID + " order by p.create_date desc " + sLimit;
            List<Record> customers = Db.find(sql);

            Map customerListMap = new HashMap();
            customerListMap.put("sEcho", pageIndex);
            customerListMap.put("iTotalRecords", rec.getLong("total"));
            customerListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            customerListMap.put("aaData", customers);
            renderJson(customerListMap);
        }
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_P_CREATE})
    public void add() {
        setAttr("saveOK", false);
            render("/yh/profile/serviceProvider/serviceProviderEdit.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_P_UPDATE})
    public void edit() {
        long id = getParaToLong();
        
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
            render("/yh/profile/serviceProvider/serviceProviderEdit.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_P_DELETE})
    public void delete() {
       
        String id = getPara();
        
        Party party = Party.dao.findById(id);
        
        Object obj = party.get("is_stop");
        if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
        	party.set("is_stop", true);
        }else{
        	party.set("is_stop", false);
        }
        party.update();
        redirect("/serviceProvider");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_P_CREATE, PermissionConstant.PERMSSION_P_UPDATE}, logical=Logical.OR)
    public void save() {

        String id = getPara("party_id");
        Party party = null;
        Contact contact = null;
        Date createDate = Calendar.getInstance().getTime();
        if (id != null && !id.equals("")) {
            party = Party.dao.findById(id);
            party.set("last_update_date", createDate);
            party.set("remark", getPara("remark"));
            party.set("receipt", getPara("receipt"));
            party.set("payment", getPara("payment"));
            party.update();

            contact = Contact.dao.findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id=" + id);
            ;
            setContact(contact);
            contact.update();
        } else {
        	Long parentID = parentOffice.get("belong_office");
        	if(parentID == null || "".equals(parentID)){
        		parentID = parentOffice.getLong("id");
        	}
            contact = new Contact();
            setContact(contact);
            contact.save();
            party = new Party();
            party.set("party_type", Party.PARTY_TYPE_SERVICE_PROVIDER);
            party.set("contact_id", contact.getLong("id"));
            party.set("creator", currentUser.getPrincipal());
            party.set("create_date", createDate);
            party.set("receipt", getPara("receipt"));
            party.set("remark", getPara("remark"));
            party.set("payment", getPara("payment"));
            party.set("charge_type", getPara("chargeType"));
            party.set("office_id", parentID);
            party.save();

        }

        setAttr("saveOK", true);
            redirect("/serviceProvider");
    }

    private void setContact(Contact contact) {
        contact.set("company_name", getPara("company_name"));
        contact.set("contact_person", getPara("contact_person"));
        contact.set("location", getPara("location"));
        contact.set("email", getPara("email"));
        contact.set("abbr", getPara("abbr"));
        contact.set("sp_type", getPara("sp_type"));
        contact.set("mobile", getPara("mobile"));
        contact.set("phone", getPara("phone"));
        contact.set("address", getPara("address"));
        contact.set("introduction", getPara("introduction"));
        contact.set("city", getPara("city"));
        contact.set("postal_code", getPara("postal_code"));
    }

    public void province() {
        List<Record> locationList = Db.find("select * from location where pcode ='1'");
        renderJson(locationList);
    }

    public void city() {
        String cityId = getPara("id");
        System.out.println(cityId);
        List<Record> locationList = Db.find("select * from location where pcode ='" + cityId + "'");
        renderJson(locationList);
    }

    public void area() {
        String areaId = getPara("id");
        System.out.println(areaId);
        List<Record> locationList = Db.find("select * from location where pcode ='" + areaId + "'");
        renderJson(locationList);
    }

    public void searchAllCity() {
        String province = getPara("province");
        List<Location> locations = Location.dao
                .find("select * from location where id in (select id from location where pcode=(select code from location where name = '"
                        + province + "'))");
        renderJson(locations);
    }

    public void searchAllDistrict() {
        String city = getPara("city");
        List<Location> locations = Location.dao
                .find("select * from location where pcode=(select code from location where name = '" + city + "')");
        renderJson(locations);
    }
    
    // 一次查出省份,城市,区
    public void searchAllLocation() {    	
    	List<Location> provinceLocations = Location.dao.find("select * from location where pcode ='1'");
    	
        String province = getPara("province");
        List<Location> cityLocations = Location.dao
                .find("select * from location where id in (select id from location where pcode=(select code from location where name = '"
                        + province + "'))");
        
        String city = getPara("city");
        List<Location> districtLocations = Location.dao
                .find("select * from location where pcode=(select code from location where name = '" + city + "')");
        Map<String, List<Location>> map = new HashMap<String, List<Location>>();
        map.put("provinceLocations", provinceLocations);
        map.put("cityLocations", cityLocations);
        map.put("districtLocations", districtLocations);
    	renderJson(map);
    }
    public void searchSp() {
    	
		String input = getPara("input");
		
		Long parentID = parentOffice.get("belong_office");
		if(parentID == null || "".equals(parentID)){
			parentID = parentOffice.getLong("id");
		}
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select *,p.id as pid, p.payment from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_SERVICE_PROVIDER
							+ "' and (company_name like '%"
							+ input
							+ "%' or abbr like '%"
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
							+ "%')  and (p.is_stop is null or p.is_stop = 0) and p.office_id = ? limit 0,10",parentID);
		} else {
			locationList = Db
					.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_SERVICE_PROVIDER + "'  and (p.is_stop is null or p.is_stop = 0) and p.office_id = ?",parentID);
		}
		renderJson(locationList);
	}
}
