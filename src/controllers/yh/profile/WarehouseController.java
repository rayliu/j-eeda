package controllers.yh.profile;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.TransferOrder;
import models.Warehouse;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import controllers.yh.LoginUserController;

public class WarehouseController extends Controller{

    private Logger logger = Logger.getLogger(WarehouseController.class);
    
	public void index() {
		if(LoginUserController.isAuthenticated(this))
		render("profile/warehouse/warehouseList.html");
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

		String sqlTotal = "select count(1) total from warehouse";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select w.*,c.contact_person,c.phone,(SELECT trim(concat(l2.name, ' ', l1.name,' ',l.name)) FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code=c.location) dname,lc.name from warehouse w"
				+ " left join contact c on w.contact_id = c.id"
				+ " left join location lc on c.location = lc.code";

		List<Record> warehouses = Db.find(sql);

		Map warehouseListMap = new HashMap();
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

	public void add() {
		setAttr("saveOK", false);
		if(LoginUserController.isAuthenticated(this))
		render("profile/warehouse/warehouseEdit.html");
	}

	public void edit() {
		long id = getParaToLong();

		Warehouse warehouse = Warehouse.dao.findById(id);
		setAttr("warehouse", warehouse);
		
		Contact locationCode = Contact.dao.findById(warehouse.get("contact_id"),
                "location");
        String code = locationCode.get("location");

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
        setAttr("location", location);
		
		Contact contact = Contact.dao.findFirst("select * from contact where id = (select contact_id from warehouse where id="+warehouse.get("id")+")");
		setAttr("contact", contact);	
		if(LoginUserController.isAuthenticated(this))
		render("profile/warehouse/warehouseEdit.html");
	}

	public void delete() {
		long id = getParaToLong();
		List<TransferOrder> orders = TransferOrder.dao.find("select * from transfer_order where warehouse_id = "+id);
    	for(TransferOrder order : orders){
    		order.set("warehouse_id", null);
    		order.update();
    	}
		Warehouse warehouse = Warehouse.dao.findById(id);
		warehouse.delete();
		if(LoginUserController.isAuthenticated(this))
		redirect("/yh/warehouse");
	}

	@SuppressWarnings("unused")
	public void save() {
		UploadFile uploadFile = getFile("fileupload");
			
		Warehouse warehouse = null;
		String id = getPara("warehouse_id");	
		
		Contact contact = null;
		Date createDate = Calendar.getInstance().getTime();
		if (id != null && !id.equals("")) {
			warehouse = Warehouse.dao.findById(id);
			warehouse.set("warehouse_name", getPara("warehouse_name"));
			warehouse.set("warehouse_address", getPara("warehouse_address"));
			warehouse.set("warehouse_desc", getPara("warehouse_desc"));
			if(uploadFile != null){
				warehouse.set("path", uploadFile.getFileName());
			}

			contact = Contact.dao.findFirst("select * from contact where id=?",
					warehouse.getLong("contact_id"));
			setContact(contact);
			contact.update();
			warehouse.update();
		} else {
			contact = new Contact();
			setContact(contact);
			contact.save();
			warehouse = new Warehouse();
			warehouse.set("warehouse_name", getPara("warehouse_name"))
					 .set("warehouse_address", getPara("warehouse_address"))
					 .set("warehouse_desc", getPara("warehouse_desc"))
					 .set("warehouse_area", getPara("warehouse_area"));					 
			if(uploadFile != null){
				warehouse.set("path", uploadFile.getFileName());
			}
			warehouse.set("contact_id", contact.get("id"));
			warehouse.save();
		}
		setAttr("saveOK", true);
		if(LoginUserController.isAuthenticated(this))
		redirect("/yh/warehouse");
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
		contact.set("location", getPara("location"));
	}
}
