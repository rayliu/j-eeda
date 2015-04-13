package controllers.yh.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.Office;
import models.UserOffice;
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

import controllers.yh.util.PermissionConstant;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class OfficeConfigController extends Controller{

    private Logger logger = Logger.getLogger(OfficeConfigController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    String userName = currentUser.getPrincipal().toString();
    UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
    Office parentOffice = Office.dao.findFirst("select * from office where id = ?",currentoffice.get("office_id"));
    Long parentID = parentOffice.get("belong_office");
    
    
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_LIST})
	public void index() {
		render("/yh/profile/officeConfig/edit.html");
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

}
