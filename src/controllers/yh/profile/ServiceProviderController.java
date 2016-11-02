package controllers.yh.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.ParentOfficeModel;
import models.Party;
import models.Product;
import models.yh.profile.Contact;
import models.yh.profile.ProviderChargeType;

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

import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ServiceProviderController extends Controller {

    private Logger logger = Logger.getLogger(ServiceProviderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_P_LIST})
    public void index() {
        render("/yh/profile/serviceProvider/serviceProviderList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_P_LIST})
    public void list() {
        String company_name = getPara("COMPANY_NAME")==null?"":getPara("COMPANY_NAME").trim();
        String contact_person = getPara("CONTACT_PERSON")==null?"":getPara("CONTACT_PERSON").trim();
        String receipt = getPara("RECEIPT")==null?"":getPara("RECEIPT").trim();
        String abbr = getPara("ABBR")==null?"":getPara("ABBR").trim();
        String address = getPara("ADDRESS")==null?"":getPara("ADDRESS").trim();
        String location = getPara("LOCATION")==null?"":getPara("LOCATION").trim();
        
        
        Long parentID = pom.getParentOfficeId();
        
        if (company_name == null && contact_person == null && receipt == null && abbr == null && address == null
                && location == null) {
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }
            String sqlTotal = "select count(1) total from party p left join office o on p.office_id = o.id where p.party_type='SERVICE_PROVIDER' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")";
            Record rec = Db.findFirst(sqlTotal); 
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select p.*,c.*,p.id as pid,l.name,trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname from party p "
                    + "left join contact c on p.contact_id=c.id "
                    + "left join location l on l.code=c.location "
                    + "left join location  l1 on l.pcode =l1.code "
                    + "left join location l2 on l1.pcode = l2.code "
                    + "left join office o on o.id = p.office_id "
                    + "where p.party_type='SERVICE_PROVIDER' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")  " + sLimit;
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
                    + "left join office o on o.id = p.office_id "
                    + "where p.party_type='SERVICE_PROVIDER' "
                    + "and ifnull(c.company_name,'') like '%"
                    + company_name
                    + "%' and ifnull(c.contact_person,'') like '%"
                    + contact_person
                    + "%' and ifnull(p.receipt,'') like '%"
                    + receipt
                    + "%' and ifnull(c.address,'') like '%"
                    + address
                    + "%' and ifnull(c.abbr,'') like '%" + abbr + "%' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")" ;
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select p.*,c.*,p.id as pid,l.name,trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname from party p "
                    + "left join contact c on p.contact_id=c.id "
                    + "left join location l on l.code=c.location "
                    + "left join location  l1 on l.pcode =l1.code "
                    + "left join location l2 on l1.pcode = l2.code "
                    + "left join office o on o.id = p.office_id "
                    + "where p.party_type='SERVICE_PROVIDER' "
                    + "and ifnull(c.company_name,'') like '%"
                    + company_name
                    + "%' and ifnull(c.contact_person,'') like '%"
                    + contact_person
                    + "%' and ifnull(p.receipt,'') like '%"
                    + receipt
                    + "%' and ifnull(c.address,'') like '%"
                    + address
                    + "%' and ifnull(c.abbr,'') like '%" + abbr + "%' and (o.id = " + parentID + " or o.belong_office = " + parentID + ") " + sLimit;
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
        String id = getPara("id");
        Party party = Party.dao.findById(id);
        Contact locationCode = Contact.dao.findById(party.getLong("contact_id"));
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
        Contact contact1 = null;
        Contact contact2 = null;
        Date createDate = Calendar.getInstance().getTime();
        if (id != null && !id.equals("")) {
            party = Party.dao.findById(id);
            party.set("last_update_date", createDate);
            party.set("remark", getPara("remark"));
            party.set("receipt", getPara("receipt"));
            party.set("payment", getPara("payment"));
            party.update();

            contact = Contact.dao.findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id=" + id);
            contact.set("receiver", getPara("receiver"));
            contact.set("bank_no", getPara("bank_no"));
            contact.set("bank_name", getPara("bank_name"));
            setContact(contact);
            contact.update();
        } else {
            //判断供应商简称
            contact1 = Contact.dao.findFirst("select * from contact where abbr=?",getPara("abbr"));
            if(contact1!=null){
            	renderText("abbrError");
            	return ;
            }
          //判断供应商全称
            contact2 = Contact.dao.findFirst("select * from contact where company_name=?",getPara("company_name")); 
            if(contact2!=null){
            	renderText("companyError");
            	return ;
            }
        	/*Long parentID = parentOffice.get("belong_office");
        	if(parentID == null || "".equals(parentID)){
        		parentID = parentOffice.getLong("id");
        	}*/
            contact = new Contact();
            contact.set("receiver", getPara("receiver"));
            contact.set("bank_no", getPara("bank_no"));
            contact.set("bank_name", getPara("bank_name"));
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
            party.set("office_id", pom.getCurrentOfficeId());
            party.save();
            
            

        }
     
        setAttr("saveOK", true);
        //redirect("/serviceProvider");
        renderJson(party);
    }

    private void setContact(Contact contact) {
        contact.set("company_name", getPara("company_name"));
        contact.set("contact_person", getPara("contact_person"));
        contact.set("location", getPara("location"));
        contact.set("email", getPara("email"));
        contact.set("abbr", getPara("abbr"));
        String sp_type = (getPara("sp_type_line")==null?"":getPara("sp_type_line") +";")
                + (getPara("sp_type_delivery")==null?"":getPara("sp_type_delivery") +";")
                + (getPara("sp_type_pickup")==null?"":getPara("sp_type_pickup") +";")
                + (getPara("sp_type_personal")==null?"":getPara("sp_type_personal"));
        contact.set("sp_type", sp_type);
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
		String sp_type = getPara("sp_type")==null?"":getPara("sp_type");
		String[] spArr = sp_type.split(";");
		String spCon = "";
		if(spArr.length>0){
		    for (String spType : spArr) {
		        spCon += " or c.sp_type like '%"+spType+"%'";
            }
		    spCon = spCon.substring(4);
		}
		Long parentID = pom.getParentOfficeId();
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find(" select p.*,c.*,p.id as pid, p.payment from party p,contact c,office o where o.id = p.office_id and p.contact_id = c.id and"
					        + " ("+spCon+") and p.party_type = '"
							+ Party.PARTY_TYPE_SERVICE_PROVIDER
							+ "' and (c.company_name like '%"
							+ input
							+ "%' or c.abbr like '%"
							+ input
							+ "%' or c.contact_person like '%"
							+ input
							+ "%' or c.email like '%"
							+ input
							+ "%' or c.mobile like '%"
							+ input
							+ "%' or c.phone like '%"
							+ input
							+ "%' or c.address like '%"
							+ input
							+ "%' or c.postal_code like '%"
							+ input
							+ "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) limit 0,10",parentID,parentID);
		} else {
			locationList = Db
					.find("select p.*,c.*,p.id as pid from party p,contact c,office o where o.id = p.office_id and p.contact_id = c.id and"
					        + " ("+spCon+") and p.party_type = '"
							+ Party.PARTY_TYPE_SERVICE_PROVIDER + "'  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office =?)",parentID,parentID);
		}
		renderJson(locationList);
	}
    public void searchInsurance() {
		String input = getPara("input");
		Long parentID = pom.getParentOfficeId();
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find(" select p.*,c.*,p.id as pid, p.payment from party p,contact c,office o where o.id = p.office_id and p.contact_id = c.id and"
					        + " p.party_type = '"
							+ Party.PARTY_TYPE_INSURANCE_PARTY
							+ "' and (c.company_name like '%"
							+ input
							+ "%' or c.abbr like '%"
							+ input
							+ "%' or c.contact_person like '%"
							+ input
							+ "%' or c.email like '%"
							+ input
							+ "%' or c.mobile like '%"
							+ input
							+ "%' or c.phone like '%"
							+ input
							+ "%' or c.address like '%"
							+ input
							+ "%' or c.postal_code like '%"
							+ input
							+ "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) limit 0,10",parentID,parentID);
		} else {
			locationList = Db
					.find("select p.*,c.*,p.id as pid from party p,contact c,office o where o.id = p.office_id and p.contact_id = c.id and"
					        + " p.party_type = '"
							+ Party.PARTY_TYPE_INSURANCE_PARTY + "'  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office =?)",parentID,parentID);
		}
		renderJson(locationList);
	}
    public void chargeTypeList(){
    	String id = getPara("typeId");
    	
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map chargeTypeMap = new HashMap();
    	if(id == null || "".equals(id)){
    		chargeTypeMap.put("sEcho", 0);
    		chargeTypeMap.put("iTotalRecords", 0);
    		chargeTypeMap.put("iTotalDisplayRecords", 0);
    		chargeTypeMap.put("aaData", null);
    	}else{
    		String totalSql = "select count(*) as total from charge_type ct left join party p on p.id = ct.customer_id left join contact c on c.id = p.contact_id where ct.sp_id = " + id;
    		Record rec = Db.findFirst(totalSql);
    		
    		List<ProviderChargeType> list = ProviderChargeType.dao.find("select ct.id,ct.charge_type,ifnull(c.abbr,c.company_name) as customer_name,ct.remark from charge_type ct left join party p on p.id = ct.customer_id left join contact c on c.id = p.contact_id where ct.sp_id = ? " + sLimit,id);
    		chargeTypeMap.put("sEcho", pageIndex);
    		chargeTypeMap.put("iTotalRecords", rec.get("total"));
    		chargeTypeMap.put("iTotalDisplayRecords", rec.get("total"));
    		chargeTypeMap.put("aaData", list);
    	}
    	renderJson(chargeTypeMap);
    	
    }
    public void saveChargeType(){
    	String sp_id = getPara("sp_id");
    	String item_id = getPara("chargeTypeItemId");
    	String customer_id = getPara("customer_id");
    	String type = getPara("c_type");
    	String remark = getPara("chargeTypeRemark");
    	if(sp_id == null || "".equals(sp_id)){
    		renderJson();
    	}
    	ProviderChargeType p = ProviderChargeType.dao.findFirst("select * from charge_type where sp_id = ? and customer_id = ? ",sp_id,customer_id);
    	if(p != null){
    		p.set("remark", remark);
    		p.set("charge_type", type);
    		p.update();
    		renderJson(p);
    	}else{
    		ProviderChargeType pct = null;
        	if(item_id == null || "".equals(item_id)){
        		//保存数据
        		pct = new ProviderChargeType();
        		
        		pct.set("sp_id", sp_id);
        		pct.set("customer_id",customer_id);
        		pct.set("remark", remark);
        		pct.set("charge_type",type);
        		pct.save();
        	}else{
        		//更新数据
        		pct = ProviderChargeType.dao.findById(item_id);
        		//pct.set("customer_id", customer_id);
        		pct.set("remark", remark);
        		pct.set("charge_type", type);
        		pct.update();
        	}
        	renderJson(pct);
    	}
    	
    }
    public void delChargeType(){
    	String id = getPara("id");
    	if(id != null && !"".equals(id)){
    		ProviderChargeType.dao.deleteById(id);
    		renderJson("{\"success\":true}");
    	}else{
    		renderJson("{\"success\":false}");
    	}
    	
    }
    public void editChargeType(){
    	String id = getPara("id");
    	ProviderChargeType pct = ProviderChargeType.dao.findFirst("select ct.*,ifnull(c.abbr,c.company_name) as customer_name from charge_type ct left join party p on p.id = ct.customer_id left join contact c on c.id = p.contact_id where ct.id = ?",id);
    	renderJson(pct);
    }
    public void seachChargeType(){
    	String sp_id = getPara("sp_id");
    	String customer_id = getPara("customer_id");
    	ProviderChargeType pct = null;
    	if(sp_id != null && !"".equals(sp_id) && customer_id != null && !"".equals(customer_id)){
    		pct = ProviderChargeType.dao.findFirst("select charge_type from charge_type where sp_id = ? and customer_id = ?",sp_id,customer_id);
    		if(pct != null){
    			renderJson(pct);
    		}else{
    			Party p = Party.dao.findById(sp_id);
    			
    			renderJson("{\"charge_type\":" + p.getStr("charge_typ") + "}");
    		}
    	}else{
    		renderJson("{\"charge_type\":error}");
    	}
    	
    }
    
    
}
