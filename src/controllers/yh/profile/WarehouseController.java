package controllers.yh.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.Office;
import models.ParentOfficeModel;
import models.Warehouse;
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
import com.jfinal.upload.UploadFile;
import org.apache.commons.lang.StringUtils;

import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class WarehouseController extends Controller{

    private Logger logger = Logger.getLogger(WarehouseController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    Long parentID = pom.getParentOfficeId();
    
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_W_LIST})
	public void index() {
		render("/yh/profile/warehouse/warehouseList.html");
	}
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_W_LIST})
	public void list() {
		Map warehouseListMap = null;
		String warehouseName = getPara("warehouseName");
		String warehouseAddress = getPara("warehouseAddress");
		String spId = getPara("spId");
		
		String conditions = " where 1 = 1";
		if(warehouseName != null && !"".equals(warehouseName)){
			conditions += " and w.warehouse_name like '%"+warehouseName+"%'";
		}
		
		if(warehouseAddress != null && !"".equals(warehouseAddress)){
			conditions += " and w.warehouse_address like '%"+warehouseAddress+"%'";	
		}
		
		if(spId != null && !"".equals(spId)){
			conditions += " and w.sp_id = '"+spId+"'";
		}
		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sqlTotal = "select count(1) total from warehouse w left join office o on o.id = w.office_id LEFT JOIN  party p ON w.sp_id = p.id "
							+" LEFT JOIN contact c ON p.contact_id = c.id "
							+ conditions
							+ " and (o.id = " + parentID + " or o.belong_office = " + parentID +")";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select w.*,(select trim(concat(IFNULL(l2. NAME,''),' ',IFNULL(l1. NAME,''),' ',IFNULL(l. NAME,''))) from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code=w.location) dname,lc.name ,company_name from warehouse w"
						+ "	LEFT JOIN  party p ON w.sp_id = p.id "
						+ "	LEFT JOIN contact c ON p.contact_id = c.id "
						+ " left join location lc on w.location = lc.code"
						+ "  left join office o on o.id = w.office_id"
						+ conditions
						+ "  and (o.id = " + parentID + " or o.belong_office = " + parentID +")"
						+ " order by w.id desc "
						+ sLimit;
	
		List<Record> warehouses = Db.find(sql);

		warehouseListMap = new HashMap();
		warehouseListMap.put("sEcho", pageIndex);
		warehouseListMap.put("iTotalRecords", rec.getLong("total"));
		warehouseListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		warehouseListMap.put("aaData", warehouses);
		renderJson(warehouseListMap);
	}
	
	public void listContact(){
		List<Contact> contactjson = Contact.dao.find("select * from contact");			
        renderJson(contactjson);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_CREATE})
	public void add() {
		setAttr("saveOK", false);
		render("/yh/profile/warehouse/warehouseEdit.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_UPDATE})
	public void edit() {
		long id = getParaToLong();

		Warehouse warehouse = Warehouse.dao.findById(id);
		setAttr("warehouse", warehouse);
		//Party party = Party.dao.findById(warehouse.get("notify_party_id"));
		//if(party != null){
			//Contact locationCode = Contact.dao.findById(party.get("contact_id"), "location");
		if(warehouse.get("location") != null && !"".equals(warehouse.get("location"))){
	        String code = warehouse.get("location");
	
	        List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
	        Location l = Location.dao.findFirst("select * from location where code = (select pcode from location where code = '"+code+"')");
	        Location location = null;
	        if(provinces.contains(l)){
	        	location = Location.dao
		                .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
		                        + code + "'");
	        }else{
	        	location = Location.dao
		                .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
		                        + code + "'");
	        }
	        setAttr("location", location);
		}
		//Contact contact = Contact.dao.findFirst("select * from contact where id = (select contact_id from party where id="+warehouse.get("notify_party_id")+")");
		//setAttr("contact", contact);
		Contact sp = Contact.dao.findFirst("select * from contact where id = (select contact_id from party where id="+warehouse.get("sp_id")+")");
		setAttr("sp", sp);
		render("/yh/profile/warehouse/warehouseEdit.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_DELETE})
	public void delete() {
		
		String id = getPara();
		Warehouse warehouse = Warehouse.dao.findById(id);
		/*warehouse.set("office_id", null);
		warehouse.set("sp_id", null);*/
		if(!"inactive".equals(warehouse.get("status"))){
			warehouse.set("status", "inactive");
		}else{
			warehouse.set("status", "active");
		}
		warehouse.update();
		//warehouse.delete();
		redirect("/warehouse");
	}

	@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_CREATE, PermissionConstant.PERMSSION_W_UPDATE}, logical=Logical.OR)
	public void save() {
		UploadFile uploadFile = getFile("fileupload");
        String spId = getPara("sp_id");
        String officeId = getPara("officeSelect");	
		Warehouse warehouse = null;
		String id = getPara("warehouse_id");	
		
		//Contact contact = null;
		//Party party = null;
		Date createDate = Calendar.getInstance().getTime();
		if (id != null && !id.equals("")) {
			//party = Party.dao.findFirst("select * from party where id = ?", getPara("notifyPartyId"));
			//contact = Contact.dao.findFirst("select * from contact where id = ?", party.get("contact_id"));
			//setContact(contact);
			//contact.update();
			warehouse = Warehouse.dao.findById(id);
			warehouse.set("warehouse_name", getPara("warehouse_name"));
			warehouse.set("warehouse_address", getPara("warehouse_address"));
			warehouse.set("status", getPara("warehouseStatus"));
			warehouse.set("warehouse_desc", getPara("warehouse_desc"));
			warehouse.set("notify_name", getPara("notify_name"));
			warehouse.set("notify_mobile", getPara("notify_mobile"));
			warehouse.set("location", getPara("location"));
			if(uploadFile != null){
				warehouse.set("path", uploadFile.getFileName());
			}
            if(spId != null && !"".equals(spId)){
            	warehouse.set("sp_id", spId);
            }
            if(officeId != null && !"".equals(officeId)){
            	warehouse.set("office_id", officeId);
            }
			warehouse.set("warehouse_type", getPara("warehouseType"));
			warehouse.update();
		} else {
			//party = new Party();
			//contact = new Contact();
			//setContact(contact);
			//contact.save();
			//party.save();
			//party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
			//party.set("contact_id", contact.get("id"));
			//party.update();
			warehouse = new Warehouse();
			warehouse.set("warehouse_name", getPara("warehouse_name"))
					 .set("warehouse_address", getPara("warehouse_address"))
					 .set("warehouse_desc", getPara("warehouse_desc"))
					 .set("status", getPara("warehouseStatus"))
					 .set("warehouse_area", getPara("warehouse_area"));	
			warehouse.set("notify_name", getPara("notify_name"));
			warehouse.set("notify_mobile", getPara("notify_mobile"));
			warehouse.set("location", getPara("location"));				 
			if(uploadFile != null){
				warehouse.set("path", uploadFile.getFileName());
			}
			//warehouse.set("notify_party_id", party.get("id"));
			if (getPara("warehouseType") != null && getPara("warehouseType").equals("ownWarehouse")) {
            	spId = null;
            }else{
            	officeId = null;
            }
            if(spId != null && !"".equals(spId)){
            	warehouse.set("sp_id", spId);
            }
            if(officeId != null && !"".equals(officeId)){
            	warehouse.set("office_id", officeId);
            }
			warehouse.set("warehouse_type", getPara("warehouseType"));
			//所属网点
			/*String name = (String) currentUser.getPrincipal();
	 		List<UserLogin> users = UserLogin.dao
	 				.find("select * from user_login where user_name='" + name + "'");
	 		warehouse.set("office_id", users.get(0).get("office_id"));*/
			
			warehouse.save();
		}
		setAttr("saveOK", true);
		redirect("/warehouse");
	}

	/*private void setContact(Contact contact) {
		contact.set("company_name", getPara("company_name"));
		contact.set("contact_person", getPara("contact_person"));
		contact.set("email", getPara("email"));
		contact.set("mobile", getPara("mobile"));
		contact.set("phone", getPara("phone"));
		contact.set("address", getPara("address"));
		contact.set("city", getPara("city"));
		contact.set("postal_code", getPara("postal_code"));
		contact.set("location", getPara("location"));
	}*/
	
	public void findDocaltion(){
		String officeId = getPara("officeId");
		Office office = Office.dao.findById(officeId);
		String code = null;
		if(office.get("location") != null && !"".equals(office.get("location"))){
			code = office.get("location");
		}
		logger.debug("所在地："+code);
        renderJson(code);
	}
	
	
}
