package controllers.yh.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.SetAttrLoginUserInterceptor;
import models.yh.profile.Carinfo;
import models.yh.profile.DriverAssistant;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class DriverAssistantController extends Controller {

	private Logger logger = Logger.getLogger(CarinfoController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	public void index() {	
	    render("/yh/profile/driverAssistant/driverAssistantList.html"); 
    }
	
	
    public void addEscortMan(){
    	render("/yh/profile/driverAssistant/driverAssistantEdit.html");
    }
	
    public void list(){
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from driver_assistant";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select id,name,phone,date_of_entry,identity_number,is_stop from driver_assistant " + sLimit);
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }
    
    public void savaDriverAssistant(){
    	String driver_assistant_id = getPara("driver_assistant_id");
        DriverAssistant driverAssistant = null;
        if (driver_assistant_id != null && !"".equals(driver_assistant_id)) {
            driverAssistant = DriverAssistant.dao.findById(driver_assistant_id);
            driverAssistant.set("name", getPara("name"))
            .set("identity_number", getPara("identity_number"))
            .set("date_of_entry", getPara("date_of_entry"))
            .set("phone", getPara("phone"))
            .set("academic_qualifications", getPara("academic_qualifications"))
            .set("date_of_entry", getPara("date_of_entry"))
            .set("daily_wage", getPara("daily_wage") == "" ? 0 : getPara("daily_wage"))
            .set("office_id", getPara("officeSelect"));
            if(getParaToDate("beging_stamp") != null){
            	driverAssistant.set("beging_stamp", getPara("beging_stamp")); 
			}
            if(getParaToDate("end_stamp") != null){
            	driverAssistant.set("end_stamp", getPara("end_stamp")); 
            }
            driverAssistant.update();
        } else {
            driverAssistant = new DriverAssistant();
            driverAssistant.set("name", getPara("name"))
            .set("identity_number", getPara("identity_number"))
            .set("date_of_entry", getPara("date_of_entry"))
            .set("phone", getPara("phone"))
            .set("academic_qualifications", getPara("academic_qualifications"))
            .set("date_of_entry", getPara("date_of_entry"))
            .set("daily_wage", getPara("daily_wage") == "" ? 0 : getPara("daily_wage"))
            .set("office_id", getPara("officeSelect"));
            if(getParaToDate("beging_stamp") != null){
            	driverAssistant.set("beging_stamp", getPara("beging_stamp")); 
			}
            if(getParaToDate("end_stamp") != null){
            	driverAssistant.set("end_stamp", getPara("end_stamp")); 
            }
            driverAssistant.save();
        }
    	render("/yh/profile/driverAssistant/driverAssistantList.html"); 
    }
	
    public void delect() {
        String id = getPara();
		DriverAssistant driverAssistant = DriverAssistant.dao.findById(id);
		Object obj = driverAssistant.get("is_stop");
		if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
			driverAssistant.set("is_stop", true);
		}else{
			driverAssistant.set("is_stop", false);
		}
		driverAssistant.update();
        renderJson("{\"success\":true}");
    }
    
    public void edit() {
        String id = getPara();
        DriverAssistant driverAssistant = DriverAssistant.dao.findById(id);
        setAttr("driverAssistant", driverAssistant);
        render("/yh/profile/driverAssistant/driverAssistantEdit.html");
    }
}
