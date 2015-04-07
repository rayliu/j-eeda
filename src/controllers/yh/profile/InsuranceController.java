package controllers.yh.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.SetAttrLoginUserInterceptor;
import models.Office;
import models.Party;
import models.UserOffice;

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
public class InsuranceController extends Controller{
	Subject currentUser = SecurityUtils.getSubject();
	private Logger logger = Logger.getLogger(InsuranceController.class);
	
	public void index() {	
	    render("/yh/profile/insurance/insuranceList.html"); 
    }
	
	public void list(){
		String name = getPara("customerName");
		String person = getPara("person");
		String address = getPara("address");
		
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String userName = currentUser.getPrincipal().toString();
        UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
        Office parentOffice = Office.dao.findFirst("select * from office where id = ?",currentoffice.get("office_id"));
        Long parentID = parentOffice.get("belong_office");
        if(parentID == null || "".equals(parentID)){
        	parentID = parentOffice.getLong("id");
        }
        
        String sqlTotal = "select count(*) as total from party p left join contact c on p.contact_id = c.id left join office o on o.id = p.office_id left join location l on c.location = l.code where (o.id =" + parentID + " or o.belong_office = " + parentID + ") and p.party_type = 'INSURANCE_PARTY' ";
        
        String sql = "select c.id,p.id as pid,c.company_name,ifnull(c.mobile,c.phone) contact_phone,c.contact_person,l.name as address,p.is_stop  from party p  left join contact c on p.contact_id = c.id left join office o on o.id = p.office_id left join location l on c.location = l.code where (o.id =" + parentID + " or o.belong_office = " + parentID + ") and p.party_type = 'INSURANCE_PARTY' ";
        
        String condition = "";
        
        if(name !=null && !"".equals(name) || person !=null && !"".equals(person)
        		|| address !=null && !"".equals(address)){
        	condition = " and c.company_name like '% " + name + " %' and c.contact_person like '% " + person + " % ' and l.name like '% " + address + " %'";
        }
        
        
        Record rec = Db.findFirst(sqlTotal + condition + sLimit);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> insurances = Db.find(sql + condition + sLimit);

        Map customerListMap = new HashMap();
        customerListMap.put("sEcho", pageIndex);
        customerListMap.put("iTotalRecords", rec.getLong("total"));
        customerListMap.put("iTotalDisplayRecords", rec.getLong("total"));
        customerListMap.put("aaData", insurances);
        renderJson(customerListMap);
	}
	public void del(){
		long id = getParaToLong();
        Party party = Party.dao.findById(id);
        Object obj = party.get("is_stop");
   
        if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
        	party.set("is_stop", true);
        }else{
        	party.set("is_stop", false);
        }
        party.update();
        redirect("/insurance");
	}
}
