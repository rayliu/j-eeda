 package controllers.yh.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.ParentOfficeModel;
import models.Party;
import models.UserCustomer;
import models.UserRole;
import models.yh.profile.Contact;
import models.yh.profile.CustomerRoute;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomerController extends Controller {

    private Logger logger = Logger.getLogger(CustomerController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    
    // in config route已经将路径默认设置为/yh
    // me.add("/yh", controllers.yh.AppController.class, "/yh");
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_C_LIST})
    public void index() {
            render("/yh/profile/customer/CustomerList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_C_LIST})
    public void list() {
        String company_name = getPara("COMPANY_NAME");
        String contact_person = getPara("CONTACT_PERSON");
        String receipt = getPara("RECEIPT");
        String abbr = getPara("ABBR");
        String address = getPara("ADDRESS");
        String location = getPara("LOCATION");
        
        Long parentID = pom.getParentOfficeId();
        
        if (company_name == null && contact_person == null && receipt == null && abbr == null && address == null
                && location == null) {
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }

            String sqlTotal = "select count(1) total from party p left join office o on p.office_id = o.id where p.party_type='CUSTOMER'and (o.id = " + parentID + " or o.belong_office = "+ parentID +")";
            

            String sql = "select p.*,c.*,p.id as pid,l.name,trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname from party p "
                    + "left join contact c on p.contact_id=c.id "
                    + "left join location l on l.code=c.location "
                    + "left join location  l1 on l.pcode =l1.code "
                    + "left join location l2 on l1.pcode = l2.code "
                    + " left join office o on o.id = p.office_id  "
                    + "where p.party_type='CUSTOMER' and (o.id = " + parentID + " or o.belong_office = " + parentID + ") order by p.create_date desc " + sLimit;
            
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));
            
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

            String sqlTotal = "select count(1) total from party p left join office o on o.id = p.office_id where p.party_type='CUSTOMER' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select p.*,c.*,p.id as pid,l.name,trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname from party p "
                    + "left join contact c on p.contact_id=c.id "
                    + "left join location l on l.code=c.location "
                    + "left join location  l1 on l.pcode =l1.code "
                    + "left join location l2 on l1.pcode = l2.code "
                    + "left join office o on o.id = p.office_id "
                    + "where p.party_type='CUSTOMER' "
                    + "and ifnull(c.company_name,'') like '%"
                    + company_name
                    + "%' and ifnull(c.contact_person,'') like '%"
                    + contact_person
                    + "%' and ifnull(p.receipt,'') like '%"
                    + receipt
                    + "%' and ifnull(c.address,'') like '%"
                    + address
                    + "%' and ifnull(c.abbr,'') like '%" + abbr + "%'  and (o.id = " + parentID + " or o.belong_office = " + parentID + ") order by p.create_date desc " + sLimit;

            List<Record> customers = Db.find(sql);

            Map customerListMap = new HashMap();
            customerListMap.put("sEcho", pageIndex);
            customerListMap.put("iTotalRecords", rec.getLong("total"));
            customerListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            customerListMap.put("aaData", customers);
            renderJson(customerListMap);
        }
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_C_CREATE})
    public void add() {
        setAttr("saveOK", false);
            render("/yh/profile/customer/CustomerEdit.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_C_UPDATE})
    public void edit() {
        String id = getPara();

        Party party = Party.dao.findById(id);
        Contact locationCode = Contact.dao.findById(party.get("contact_id"));
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
        
        Record re = Db.findFirst("select get_loc_full_name('"+party.getStr("default_loc_from")+"') as locFrom");
        setAttr("default_loc_from", re.getStr("locFrom"));
        
        setAttr("party", party);
        if(party.getInt("is_inventory_control")>0){
        	setAttr("is_inventory_control", "Y");
        }else{
        	setAttr("is_inventory_control", "N");
        }
        Contact contact = Contact.dao.findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id="
                + id);
        setAttr("contact", contact);

        render("/yh/profile/customer/CustomerEdit.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_C_DELETE})
    public void delete() {
        long id = getParaToLong();
        Party party = Party.dao.findById(id);
        Object obj = party.get("is_stop");
   
        if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
        	party.set("is_stop", true);
        }else{
        	party.set("is_stop", false);
        }
        party.update();
        redirect("/customer");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_C_CREATE, PermissionConstant.PERMSSION_C_UPDATE}, logical=Logical.OR)
    @Before(Tx.class)
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
            party.set("is_auto_ps", getPara("isAutoPS"));
            party.set("default_loc_from", getPara("default_loc_from"));
            if("Y".equals(getPara("isInventoryControl"))){
            	party.set("is_inventory_control", true);
            }else{
            	party.set("is_inventory_control", false);
            }
            if(getPara("insurance_rates") != ""){
            	party.set("insurance_rates", getPara("insurance_rates"));
            }
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
            party.set("office_id", pom.getCurrentOfficeId());
            party.set("is_auto_ps", getPara("isAutoPS"));
            party.set("default_loc_from", getPara("default_loc_from"));
            if("Y".equals(getPara("isInventoryControl"))){
            	party.set("is_inventory_control", true);
            }else{
            	party.set("is_inventory_control", false);
            }
            if(getPara("insurance_rates") != ""){
            	party.set("insurance_rates", getPara("insurance_rates"));
            }
            party.save();
            
            Long parentID = pom.getParentOfficeId();
            //判断当前是否是系统管理员，是的话将当前的客户默认给
            List<UserRole> urList = UserRole.dao.find("select * from user_role ur left join user_login ul on ur.user_name = ul.user_name left join office o on o.id = ul.office_id  where role_code = 'admin' and (o.id = ? or o.belong_office = ?)",parentID,parentID);
            if(urList.size()>0){
            	for (UserRole userRole : urList) {
                	UserCustomer uc = new UserCustomer();
                	uc.set("user_name", userRole.get("user_name"));
                	uc.set("customer_id",party.get("id"));
                	uc.save();
    			}
            }
            
        }

        setAttr("saveOK", true);
            redirect("/customer");
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
    public void checkCustomerNameExist(){
 		String company_name= getPara("company_name");
 		boolean checkObjectExist;
 		Long parentID = pom.getParentOfficeId();
 		Contact contact = Contact.dao.findFirst("select c.*,p.*,c.id as cid,p.id as pid from contact c left join party p on c.id = p.contact_id where c.company_name =? and p.party_type='CUSTOMER' and p.office_id = ?",company_name,parentID);
 		
 		if(contact == null){
 			checkObjectExist=true;
 		}else{
 			checkObjectExist=false;
 		}
 		renderJson(checkObjectExist);
 	}
    public void checkCustomerAbbrExist(){
    	String abbr= getPara("abbr");
 		boolean checkObjectExist;
 		Long parentID = pom.getParentOfficeId();
 		Contact contact = Contact.dao.findFirst("select c.*,p.*,c.id as cid,p.id as pid from contact c left join party p on c.id = p.contact_id where c.abbr =? and p.party_type='CUSTOMER' and p.office_id = ?",abbr,parentID);
 		if(contact == null){
 			checkObjectExist=true;
 		}else{
 			checkObjectExist=false;
 		}
 		renderJson(checkObjectExist);
 	}
    public void searchPartCustomer() {
		String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select *,p.id as pid,p.payment from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_CUSTOMER
							+ "' and (company_name like '%"
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
							+ "%') and (p.is_stop is null or p.is_stop = 0) and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') limit 0,10");
		} else {
			locationList = Db
					.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_CUSTOMER + "' and (p.is_stop is null or p.is_stop = 0) and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')");
		}
		renderJson(locationList);
	}
    
    @Before(Tx.class)
    public void saveCustomerRoute(){
        String id = getPara("route_id");
        String customer_id = getPara("customer_id");
        String location_from = getPara("location_from");
        String location_to = getPara("location_to");
        String sp_id = getPara("sp_id");
        String charge_type = getPara("charge_type");
        String car_type = getPara("car_type");
        String ltl_price_type = getPara("ltl_price_type");
        String limitation = getPara("limitationFile");
        CustomerRoute route = null;
 
        try{
            if(StringUtils.isEmpty(id)){
            	//路线重复校验
                String sql = "SELECT * FROM `customer_route_provider` crp where crp.customer_id ="+ customer_id
                		+ " and  crp.location_from ="+location_from + " and crp.location_to = "+location_to;
                CustomerRoute cRoute = CustomerRoute.dao.findFirst(sql);
                route = new CustomerRoute();
                if(cRoute==null){	 
                     route.set("customer_id", customer_id);
                     route.set("location_from", location_from);
                     route.set("location_to", location_to);
                     route.set("sp_id", sp_id);
                     route.set("charge_type", charge_type);
                     route.set("car_type", car_type);
                     route.set("ltl_price_type", ltl_price_type);
                     route.set("limitation", limitation);
                     route.save();
                }else{
                	 route.set("id", -1);
                }
            }else{
            	String sql = "SELECT * FROM `customer_route_provider` crp where crp.customer_id ="+ customer_id
                		+ " and  crp.location_from ="+location_from + " and crp.location_to = "+location_to+" and crp.id != "+id;
                CustomerRoute cRoute = CustomerRoute.dao.findFirst(sql);
                if(cRoute==null){
                	route = CustomerRoute.dao.findById(id);
                    route.set("customer_id", customer_id);
                    route.set("location_from", location_from);
                    route.set("location_to", location_to);
                    route.set("sp_id", sp_id);
                    route.set("charge_type", charge_type);
                    route.set("car_type", car_type);
                    route.set("ltl_price_type", ltl_price_type);
                    route.set("limitation", limitation);
                    route.update();
                }else{
                	route = new CustomerRoute();
                	route.set("id", -1);
                }
            }
        }catch(Exception e){
            String errMsg = e.getMessage();
            logger.debug(errMsg);
            if(errMsg.indexOf("index_route")>0){
                route.set("id", -1);
            }
        }
        renderJson(route);
    }
    
    public void deleteCustomerRoute(){
        String id = getPara("route_id");
        CustomerRoute route = CustomerRoute.dao.findById(id);
        route.delete();
        renderText("ok");
    }
    
    public void getCustomerRoute(){
        String id = getPara("route_id");
        String sql = "select rp.*, get_loc_full_name(rp.location_from) location_from_name, get_loc_full_name(rp.location_to) location_to_name, sp.abbr sp_name from customer_route_provider rp"
                +" left join contact sp on rp.sp_id = sp.id"
                +" where rp.id = ?";
        Record route = Db.findFirst(sql, id);
        renderJson(route);
    }
    
    public void routeList(){
        String draw = getPara("draw");
        String start = getPara("start");
        String length = getPara("length");
        
        String customer_id = getPara("customer_id");
        
        String sql = "select rp.*, l1.name location_from_name, l2.name location_to_name, sp.abbr sp_name from customer_route_provider rp"
                +" left join location l1 on rp.location_from = l1.code"
                +" left join location l2 on rp.location_to = l2.code"
                +" left join contact sp on rp.sp_id = sp.id"
                +" where customer_id = ?";
        
        String totalSql = "select count(1) total from (" + sql + ") A";
        
        Record rec = Db.findFirst(totalSql, customer_id);
        String sLimit = " limit " + start + ", " +length; 
        
        List<Record> locationList = Db.find(sql +" order by id desc " + sLimit, customer_id);
        if(locationList == null)
            locationList = Collections.EMPTY_LIST;
        
        Map orderListMap = new HashMap();
        orderListMap.put("draw", draw);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", locationList);
        renderJson(orderListMap);
    }
}
