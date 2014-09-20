package controllers.yh.profile;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.Party;
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
		render("/yh/profile/warehouse/warehouseList.html");
	}

	public void list() {
		Map warehouseListMap = null;
		String warehouseName = getPara("warehouseName");
		String warehouseAddress = getPara("warehouseAddress");
		if(warehouseName == null && warehouseAddress == null){
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
	
			String sql = "select w.*,c.contact_person,c.phone,(select trim(concat(l2.name, ' ', l1.name,' ',l.name)) from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code=c.location) dname,lc.name from warehouse w"
							+ " left join party p on w.notify_party_id = p.id"
							+ " left join contact c on p.contact_id = c.id"
							+ " left join location lc on c.location = lc.code order by w.id desc "
							+ sLimit;
	
			List<Record> warehouses = Db.find(sql);
	
			warehouseListMap = new HashMap();
			warehouseListMap.put("sEcho", pageIndex);
			warehouseListMap.put("iTotalRecords", rec.getLong("total"));
			warehouseListMap.put("iTotalDisplayRecords", rec.getLong("total"));
	
			warehouseListMap.put("aaData", warehouses);
		}else{
			String sLimit = "";
			String pageIndex = getPara("sEcho");
			if (getPara("iDisplayStart") != null
					&& getPara("iDisplayLength") != null) {
				sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
						+ getPara("iDisplayLength");
			}
			String sqlTotal = "select count(1) total from warehouse where warehouse_name like '%"+warehouseName+"%' and warehouse_address like '%"+warehouseAddress+"%'";
			Record rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));
	
			String sql = "select w.*,c.contact_person,c.phone,(select trim(concat(l2.name, ' ', l1.name,' ',l.name)) from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code=c.location) dname,lc.name from warehouse w"
							+ " left join party p on w.notify_party_id = p.id"
							+ " left join contact c on p.contact_id = c.id"
							+ " left join location lc on c.location = lc.code"
							+ "  where warehouse_name like '%"+warehouseName+"%' and warehouse_address like '%"+warehouseAddress+"%' order by w.id desc "
							+ sLimit;
	
			List<Record> warehouses = Db.find(sql);
	
			warehouseListMap = new HashMap();
			warehouseListMap.put("sEcho", pageIndex);
			warehouseListMap.put("iTotalRecords", rec.getLong("total"));
			warehouseListMap.put("iTotalDisplayRecords", rec.getLong("total"));
	
			warehouseListMap.put("aaData", warehouses);
		}
		renderJson(warehouseListMap);
	}
	
	public void listContact(){
		List<Contact> contactjson = Contact.dao.find("select * from contact");			
        renderJson(contactjson);
	}

	public void add() {
		setAttr("saveOK", false);
		if(LoginUserController.isAuthenticated(this))
		render("/yh/profile/warehouse/warehouseEdit.html");
	}

	public void edit() {
		long id = getParaToLong();

		Warehouse warehouse = Warehouse.dao.findById(id);
		setAttr("warehouse", warehouse);
		Party party = Party.dao.findById(warehouse.get("notify_party_id"));
		if(party != null){
			Contact locationCode = Contact.dao.findById(party.get("contact_id"), "location");
	        String code = locationCode.get("location");
	
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
		Contact contact = Contact.dao.findFirst("select * from contact where id = (select contact_id from party where id="+warehouse.get("notify_party_id")+")");
		setAttr("contact", contact);
		Contact sp = Contact.dao.findFirst("select * from contact where id = (select contact_id from party where id="+warehouse.get("sp_id")+")");
		setAttr("sp", sp);
		if(LoginUserController.isAuthenticated(this))
		render("/yh/profile/warehouse/warehouseEdit.html");
	}

	public void delete() {
		long id = getParaToLong();
		List<TransferOrder> orders = TransferOrder.dao.find("select * from transfer_order where warehouse_id = "+id);
    	for(TransferOrder order : orders){
    		order.set("warehouse_id", null);
    		order.update();
    	}
		Warehouse warehouse = Warehouse.dao.findById(id);
		warehouse.set("office_id", null);
		warehouse.set("sp_id", null);
		warehouse.update();
		warehouse.delete();
		if(LoginUserController.isAuthenticated(this))
		redirect("/warehouse");
	}

	@SuppressWarnings("unused")
	public void save() {
		UploadFile uploadFile = getFile("fileupload");
        String spId = getPara("sp_id");
        String officeId = getPara("officeSelect");	
		Warehouse warehouse = null;
		String id = getPara("warehouse_id");	
		
		Contact contact = null;
		Party party = null;
		Date createDate = Calendar.getInstance().getTime();
		if (id != null && !id.equals("")) {
			party = Party.dao.findFirst("select * from party where id = ?", getPara("notifyPartyId"));
			contact = Contact.dao.findFirst("select * from contact where id = ?", party.get("contact_id"));
			setContact(contact);
			contact.update();
			warehouse = Warehouse.dao.findById(id);
			warehouse.set("warehouse_name", getPara("warehouse_name"));
			warehouse.set("warehouse_address", getPara("warehouse_address"));
			warehouse.set("status", getPara("warehouseStatus"));
			warehouse.set("warehouse_desc", getPara("warehouse_desc"));
			if(uploadFile != null){
				warehouse.set("path", uploadFile.getFileName());
			}
			warehouse.set("notify_party_id", party.get("id"));
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
			warehouse.update();
		} else {
			party = new Party();
			contact = new Contact();
			setContact(contact);
			contact.save();
			party.save();
			party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
			party.set("contact_id", contact.get("id"));
			party.update();
			warehouse = new Warehouse();
			warehouse.set("warehouse_name", getPara("warehouse_name"))
					 .set("warehouse_address", getPara("warehouse_address"))
					 .set("warehouse_desc", getPara("warehouse_desc"))
					 .set("status", getPara("warehouseStatus"))
					 .set("warehouse_area", getPara("warehouse_area"));					 
			if(uploadFile != null){
				warehouse.set("path", uploadFile.getFileName());
			}
			warehouse.set("notify_party_id", party.get("id"));
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
			warehouse.save();
		}
		setAttr("saveOK", true);
		if(LoginUserController.isAuthenticated(this))
		redirect("/warehouse");
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
