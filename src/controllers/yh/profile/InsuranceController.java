package controllers.yh.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.Party;
import models.UserLogin;
import models.UserOffice;
import models.yh.profile.Contact;
import models.yh.profile.PartyInsuranceItem;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.ParentOffice;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class InsuranceController extends Controller{

	private Logger logger = Logger.getLogger(InsuranceController.class);	
	Subject currentUser = SecurityUtils.getSubject();
	
	public void index() {	
	    render("/yh/profile/insurance/insuranceList.html"); 
    }
	
	public void add(){
		render("/yh/profile/insurance/insuranceEdit.html"); 
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
        
        ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
        Long parentID = pom.getParentOfficeId();
        
        String sqlTotal = "select count(*) as total from party p left join contact c on p.contact_id = c.id left join office o on o.id = p.office_id where (o.id =" + parentID + " or o.belong_office = " + parentID + ") and p.party_type = 'INSURANCE_PARTY' ";
        
        String sql = "select c.id,p.id as pid,c.company_name,ifnull(c.mobile,c.phone) contact_phone,c.contact_person,c.address,p.is_stop  from party p  left join contact c on p.contact_id = c.id left join office o on o.id = p.office_id where (o.id =" + parentID + " or o.belong_office = " + parentID + ") and p.party_type = 'INSURANCE_PARTY' ";
        
        String condition = "";
        
        if(name !=null && !"".equals(name) || person !=null && !"".equals(person)
        		|| address !=null && !"".equals(address)){
        	condition = " and c.company_name like '%" + name + "%' and c.contact_person like '%" + person + "%' and c.address like '%" + address + "%'";
        }
        
        
        Record rec = Db.findFirst(sqlTotal + condition);
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
	
    public void saveInsurance(){
    	String insuranceId = getPara("insuranceId");
    	String insuranceName = getPara("insuranceName");
    	String companyName = getPara("company_name");
    	String mobilePhone = getPara("mobilePhone");
    	String abbr = getPara("abbr");
    	String phone = getPara("phone");
    	String address = getPara("address");
    	String post = getPara("post");
    	String remark = getPara("remark");
    	String payment = getPara("payment");//付款方式
    	String receipt = getPara("receipt");//付款单位
    	String receiver = getPara("receiver");//收款人
    	String bank_no = getPara("bank_no");//银行账户
    	String bank_name = getPara("bank_name");//开户行
    	
        Party party = null;
        if (insuranceId == null || "".equals(insuranceId)) {
        	String name = (String) currentUser.getPrincipal();
    		List<UserLogin> users = UserLogin.dao
    				.find("select * from user_login where user_name='" + name + "'");
    		String userName = currentUser.getPrincipal().toString();
    		UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
    		Contact contact = new Contact();
        	contact.set("company_name", insuranceName)
			.set("contact_person", companyName)
			.set("address", address)
			.set("phone", phone)
			.set("mobile", mobilePhone)
			.set("introduction", post)
			.set("receiver", receiver)
			.set("bank_name", bank_name)
			.set("bank_no", bank_no)
        	.set("abbr", abbr).save();
			party = new Party();
			party.set("contact_id", contact.get("id"))
			.set("party_type", "INSURANCE_PARTY")
			.set("payment",payment)
			.set("receipt", receipt)
			.set("create_date", new Date())
			.set("creator", users.get(0).get("id"))
			.set("office_id", currentoffice.get("office_id"))
			.set("remark", remark).save();
        } else {
        	party = Party.dao.findById(insuranceId);
        	Contact contact = Contact.dao.findById(party.get("contact_id"));
        	contact.set("company_name", insuranceName)
			.set("contact_person", companyName)
			.set("address", address)
			.set("phone", phone)
			.set("mobile", mobilePhone)
			.set("introduction", post)
			.set("bank_name", bank_name)
			.set("bank_no", bank_no)
        	.set("receiver", receiver)
        	.set("abbr", abbr).update();
        	party.set("remark", remark).set("payment",payment)
			.set("receipt", receipt).update();
        }
        renderJson(party); 
    }
    
    public void edit(){
    	String insuranceId = getPara();
    	Party party = Party.dao.findById(insuranceId);
    	Contact contact = Contact.dao.findById(party.get("contact_id"));
    	setAttr("party", party);
    	setAttr("contact", contact);
    	render("/yh/profile/insurance/insuranceEdit.html"); 
    }
    
    public void findAllInsuranceItem(){
		String inseruanceId = getPara("insuranceId");
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        Map orderMap = new HashMap();
        if(inseruanceId != null && !"".equals(inseruanceId)){
        	 String sqlTotal = "select count(*) total from party_insurance_item where party_id = '" + inseruanceId + "'";
             String sql = "select pit.id,c.abbr,pit.insurance_rate,pit.beginTime,pit.endTime,pit.remark,pit.is_stop from party_insurance_item pit "
             		+ " left join party p on p.id = pit.customer_id"
             		+ " left join contact c on c.id = p.contact_id"
             		+ " where pit.party_id = '" + inseruanceId + "'";
             Record rec = Db.findFirst(sqlTotal + sLimit);
             List<Record> insurances = Db.find(sql + sLimit);
             orderMap.put("sEcho", pageIndex);
             orderMap.put("iTotalRecords", rec.getLong("total"));
             orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
             orderMap.put("aaData", insurances);
        }else{
        	orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
        }
        renderJson(orderMap);
    }
    
    public void saveInsuranceItem(){
    	String insuranceId = getPara("rateInsuranceId");
    	String rateItemId = getPara("rateItemId");
    	String customerId = getPara("customer_id");
    	String insuranceRate = getPara("insurance_rate");
    	String beginTime = getPara("beginTime");
    	String endTime = getPara("endTime");
    	String remark = getPara("remark1");
    	PartyInsuranceItem partyInsuranceItem = null;
        if (rateItemId == null || "".equals(rateItemId)) {
        	partyInsuranceItem = new PartyInsuranceItem();
        	partyInsuranceItem.set("party_id", insuranceId)
			.set("customer_id", customerId)
			.set("insurance_rate", insuranceRate)
			.set("beginTime", beginTime)
			.set("endTime", endTime)
			.set("remark", remark).save();
        } else {
        	partyInsuranceItem = PartyInsuranceItem.dao.findById(rateItemId);
        	partyInsuranceItem.set("customer_id", customerId)
			.set("insurance_rate", insuranceRate)
			.set("beginTime", beginTime)
			.set("endTime", endTime)
			.set("remark", remark).update();
        }
        renderJson(partyInsuranceItem); 
    }
    
    public void rateEdit(){
    	String rateItemId = getPara();
    	Record partyInsuranceItem = Db.findFirst("select * from party_insurance_item where id = ?",rateItemId);
    	Party party = Party.dao.findFirst("select c.company_name,c.abbr from party p left join contact c on c.id = p.contact_id where p.id = '"+ partyInsuranceItem.get("customer_id") + "'");
    	partyInsuranceItem.set("company_name", party.get("company_name"));
    	renderJson(partyInsuranceItem); 
    }
    
    public void rateDel(){
    	String rateItemId = getPara();
    	PartyInsuranceItem partyInsuranceItem = PartyInsuranceItem.dao.findById(rateItemId);
        Object obj = partyInsuranceItem.get("is_stop");
        if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
        	partyInsuranceItem.set("is_stop", true);
        }else{
        	partyInsuranceItem.set("is_stop", false);
        }
        partyInsuranceItem.update();
        renderJson("{\"success\":true}");
	}
}
