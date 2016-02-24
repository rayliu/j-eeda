package controllers.yh.contract;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.FinItem;
import models.Location;
import models.ParentOfficeModel;
import models.Party;
import models.yh.contract.Contract;
import models.yh.contract.ContractItem;
import models.yh.profile.Contact;

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

import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ContractController extends Controller {

    private Logger logger = Logger.getLogger(ContractController.class);
    Subject currentUser = SecurityUtils.getSubject();
    // in config route已经将路径默认设置为/yh
    // me.add("/yh", controllers.yh.AppController.class, "/yh");
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CC_LIST})
    public void index() {
        HttpServletRequest re = getRequest();
        String url = re.getRequestURI();
        logger.debug("URI:" + url);
        setAttr("contractType", "CUSTOMER");    	
        render("/yh/contract/ContractList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CD_LIST})
    public void spIndex(){
    	setAttr("contractType", "SERVICE_PROVIDER");
        render("/yh/contract/ContractList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CD_LIST})
    public void deliverySpIndex(){
    	 setAttr("contractType", "DELIVERY_SERVICE_PROVIDER");
         render("/yh/contract/ContractList.html");
    }

    public void companyNameList() {
        String input = getPara("input");
        String type = getPara("type");
    	if("DELIVERY_SERVICE_PROVIDER".equals(type)){
    		type = "SERVICE_PROVIDER";
    	}
    	
    	//查询当前用户的父类公司的id
    	 ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    	Long parentID = pom.getParentOfficeId();
    	
        List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select *,p.id as pid, p.payment from party p,contact c,office o where o.id = p.office_id and p.contact_id = c.id and p.party_type = '"
							+ type
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
							+ "%') and (o.id = " + pom.getCurrentOfficeId() + " or  o.belong_office = " + parentID + ")");
		} else {
			locationList = Db
					.find("select *,p.id as pid from party p,contact c,office o where p.office_id = o.id and p.contact_id = c.id and p.party_type = '"
							+ type + "' and (o.id = " + pom.getCurrentOfficeId() + " or  o.belong_office = " + parentID + ")");
		}
		renderJson(locationList);
    }

    // 客户合同列表
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CC_LIST})
    public void customerList() {
        String contractName_filter = getPara("contractName_filter");
        String contactPerson_filter = getPara("contactPerson_filter");
        String periodFrom_filter = getPara("periodFrom_filter");
        String companyName_filter = getPara("companyName_filter");
        String phone_filter = getPara("phone_filter");
        String periodTo_filter = getPara("periodTo_filter");
      
        //查询当前用户的父类公司的id
        ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    	Long parentID = pom.getParentOfficeId();
    	
        Map orderMap = new HashMap();
        if (contractName_filter == null && contactPerson_filter == null && periodFrom_filter == null && companyName_filter == null
                && phone_filter == null && periodTo_filter == null) {
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1,office o where c.party_id= p.id and p.contact_id = c1.id and c.type='CUSTOMER' and o.id = p.office_id and (o.id = " + pom.getCurrentOfficeId() + " or  o.belong_office = " + parentID + ")";
            //System.out.println(sql);
            Record rec = Db.findFirst(sql + totalWhere);
            long total = rec.getLong("total");
            logger.debug("total records:" + total);

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid,c.is_stop as c_is_Stop from contract c,party p,contact c1,office o where c.party_id= p.id and p.contact_id = c1.id and c.type='CUSTOMER' and o.id = p.office_id and (o.id = " + pom.getCurrentOfficeId() + " or  o.belong_office = " + parentID + ")"
                            + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", total);
            orderMap.put("iTotalDisplayRecords", total);
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        } else {
            /*
             * if (periodFrom_filter == null || "".equals(periodFrom_filter)) {
             * periodFrom_filter = "1-1-1"; } if (periodTo_filter == null ||
             * "".equals(periodTo_filter)) { periodTo_filter = "9999-12-31"; }
             */
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1,office o where o.id = p.office_id and c.party_id= p.id and p.contact_id = c1.id and c.type='CUSTOMER' and (o.id = " + pom.getCurrentOfficeId() + " or  o.belong_office = " + parentID + ")"
            		 + "and c.name like '%"
                     + contractName_filter
                     + "%' and ifnull(c1.contact_person,'') like '%"
                     + contactPerson_filter
                     + "%' and ifnull(c1.company_name,'') like '%"
                     + companyName_filter
                     + "%' and (ifnull(c1.mobile,'') like '%"
                     + phone_filter
                     + "%' or ifnull(c1.phone,'') like '%"
                     + phone_filter
                     + "%' ) and c.period_from like '%"
                     + periodFrom_filter
                     + "%' and c.period_to like '%" + periodTo_filter + "%'";
            
            Record rec = Db.findFirst(sql + totalWhere);
            long total = rec.getLong("total");
            logger.debug("total records:" + total);
            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid from contract c,party p,contact c1,office o  where o.id = p.office_id and c.party_id= p.id and p.contact_id = c1.id and c.type='CUSTOMER' and (o.id = " + pom.getCurrentOfficeId() + " or  o.belong_office = " + parentID + ")"
                            + "and c.name like '%"
                            + contractName_filter
                            + "%' and ifnull(c1.contact_person,'') like '%"
                            + contactPerson_filter
                            + "%' and ifnull(c1.company_name,'') like '%"
                            + companyName_filter
                            + "%' and (ifnull(c1.mobile,'') like '%"
                            + phone_filter
                            + "%' or ifnull(c1.phone,'') like '%"
                            + phone_filter
                            + "%') and c.period_from like '%"
                            + periodFrom_filter
                            + "%' and c.period_to like '%" + periodTo_filter + "%'" + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", total);
            orderMap.put("iTotalDisplayRecords", total);
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        }
    }

    // 配送供应商合同列表
    public void deliveryspList() {
    	
        String contractName_filter = getPara("contractName_filter");
        String contactPerson_filter = getPara("contactPerson_filter");
        String periodFrom_filter = getPara("periodFrom_filter");
        String companyName_filter = getPara("companyName_filter");
        String phone_filter = getPara("phone_filter");
        String periodTo_filter = getPara("periodTo_filter");
        //查询当前用户所属网点
        
        //获取当前用户的总公司的ID
        
        Long parentId = pom.getParentOfficeId();
        Long currentId = pom.getCurrentOfficeId();
        
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        if (contractName_filter == null && contactPerson_filter == null && periodFrom_filter == null && companyName_filter == null
                && phone_filter == null && periodTo_filter == null) {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1,office o where o.id = p.office_id and c.party_id= p.id and p.contact_id = c1.id and c.type='DELIVERY_SERVICE_PROVIDER' "
            		+ " and (o.id = " + currentId + " or o.belong_office = " + parentId + ")";
            
            Record rec = Db.findFirst(sql + totalWhere);
            long total = rec.getLong("total");
            logger.debug("total records:" + total);

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid,c.is_stop as c_is_stop from contract c,party p,contact c1,office o where c.party_id= p.id and p.contact_id = c1.id and c.type='DELIVERY_SERVICE_PROVIDER' and o.id = p.office_id and (o.id = " + currentId + " or o.belong_office = " + parentId + ")"
                            + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", total);
            orderMap.put("iTotalDisplayRecords", total);
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        } else {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(*) total from contract c,party p,contact c1,office o where c.party_id= p.id and p.contact_id = c1.id and c.type='DELIVERY_SERVICE_PROVIDER' and o.id = p.office_id and (o.id = " + currentId + " or o.belong_office = " + parentId + ")"
                            + "and c.name like '%"
                            + contractName_filter
                            + "%' and ifnull(c1.contact_person,'') like '%"
                            + contactPerson_filter
                            + "%' and ifnull(c1.company_name,'') like '%"
                            + companyName_filter
                            + "%' and ifnull(c1.mobile,'') like '%"
                            + phone_filter
                            + "%' and c.period_from like '%"
                            + periodFrom_filter
                            + "%' and c.period_to like '%" + periodTo_filter + "%'";
            System.out.println(sql);
            Record rec = Db.findFirst(sql + totalWhere);
            long total = rec.getLong("total");
            logger.debug("total records:" + total);

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid from contract c,party p,contact c1,office o where c.party_id= p.id and p.contact_id = c1.id and c.type='DELIVERY_SERVICE_PROVIDER' and o.id = p.office_id and (o.id = " + currentId + " or o.belong_office = " + parentId + ")"
                            + "and c.name like '%"
                            + contractName_filter
                            + "%' and ifnull(c1.contact_person,'') like '%"
                            + contactPerson_filter
                            + "%' and ifnull(c1.company_name,'') like '%"
                            + companyName_filter
                            + "%' and ifnull(c1.mobile,'') like '%"
                            + phone_filter
                            + "%' and c.period_from like '%"
                            + periodFrom_filter
                            + "%' and c.period_to like '%" + periodTo_filter + "%'" + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", total);
            orderMap.put("iTotalDisplayRecords", total);
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        }

    }

    // 干线供应商合同列表
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CP_LIST})
    public void spList() {
        String contractName_filter = getPara("contractName_filter");
        String contactPerson_filter = getPara("contactPerson_filter");
        String periodFrom_filter = getPara("periodFrom_filter");
        String companyName_filter = getPara("companyName_filter");
        String phone_filter = getPara("phone_filter");
        String periodTo_filter = getPara("periodTo_filter");

     
        Long parentID = pom.getParentOfficeId();
        Long currentId= pom.getCurrentOfficeId();
        
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        if (contractName_filter == null && contactPerson_filter == null && periodFrom_filter == null && companyName_filter == null
                && phone_filter == null && periodTo_filter == null) {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1,office o where c.party_id= p.id and p.contact_id = c1.id and c.type='SERVICE_PROVIDER' and o.id = p.office_id and (o.id = " + currentId + " or o.belong_office = " + parentID + ")";
            
            Record rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid,c.is_stop as c_is_stop from contract c,party p,contact c1,office o where c.party_id= p.id and p.contact_id = c1.id and c.type='SERVICE_PROVIDER' and o.id = p.office_id and (o.id = " + currentId + " or o.belong_office = " + parentID + ")"
                            + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        } else {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1,office o where o.id = p.office_id and c.party_id= p.id and p.contact_id = c1.id and c.type='SERVICE_PROVIDER' and (o.id = " + currentId + " or o.belong_office = " + parentID + ")"
            		+ "and c.name like '%"
                    + contractName_filter
                    + "%' and ifnull(c1.contact_person,'') like '%"
                    + contactPerson_filter
                    + "%' and ifnull(c1.company_name,'') like '%"
                    + companyName_filter
                    + "%' and ifnull(c1.mobile,'') like '%"
                    + phone_filter
                    + "%' and c.period_from like '%"
                    + periodFrom_filter
                    + "%' and c.period_to like '%" + periodTo_filter + "%' ";
            
            Record rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid from contract c,party p,contact c1,office o where o.id = p.office_id and  c.party_id= p.id and p.contact_id = c1.id and c.type='SERVICE_PROVIDER' and (o.id = " + currentId + " or o.belong_office = " + parentID + ")"
                            + "and c.name like '%"
                            + contractName_filter
                            + "%' and ifnull(c1.contact_person,'') like '%"
                            + contactPerson_filter
                            + "%' and ifnull(c1.company_name,'') like '%"
                            + companyName_filter
                            + "%' and ifnull(c1.mobile,'') like '%"
                            + phone_filter
                            + "%' and c.period_from like '%"
                            + periodFrom_filter
                            + "%' and c.period_to like '%" + periodTo_filter + "%'" + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        }

    }
    
    //
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_IO_LIST})
    public void searchInsurance() {
        String insuranceName = getPara("insuranceName");
        
        List<Record> insuranceList = Collections.EMPTY_LIST;
        
        String sql="select c.id, c.abbr from party p,contact c where p.contact_id = c.id and p.party_type = 'INSURANCE_PARTY' ";
        		
        if (insuranceName.trim().length() > 0) {
        	sql +=" and (c.abbr like '%" + insuranceName + "%' or c.quick_search_code like '%" + insuranceName.toUpperCase() + "%') ";
        } 
        
        insuranceList = Db.find(sql);  
        renderJson(insuranceList);
    }
    
    
    
    
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CC_CREATE,PermissionConstant.PERMSSION_CP_CREATE,PermissionConstant.PERMSSION_CD_CREATE},logical=Logical.OR)
    public void add() {
        HttpServletRequest re = getRequest();
        String url = re.getRequestURI();
        logger.debug("URI:" + url);
        if (url.equals("/customerContract/add")) {
            setAttr("contractType", "CUSTOMER");
            setAttr("saveOK", false);
            List<FinItem> finItemList = FinItem.dao.find("select * from fin_item where type='应收'");
            setAttr("finItemList", finItemList);
                render("/yh/contract/ContractEdit.html");
        }
        if (url.equals("/deliverySpContract/add")) {
            setAttr("contractType", "DELIVERY_SERVICE_PROVIDER");
            setAttr("saveOK", false);
            List<FinItem> finItemList = FinItem.dao.find("select * from fin_item where type='应付'");
            setAttr("finItemList", finItemList);
                render("/yh/contract/ContractEdit.html");
        }
        if (url.equals("/spContract/add")) {
            setAttr("contractType", "SERVICE_PROVIDER");
            setAttr("saveOK", false);
            List<FinItem> finItemList = FinItem.dao.find("select * from fin_item where type='应付'");
            setAttr("finItemList", finItemList);
                render("/yh/contract/ContractEdit.html");
        }
        setAttr("saveOK", false);

    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CC_UPDAET,PermissionConstant.PERMSSION_CP_UPDATE,PermissionConstant.PERMSSION_CD_UPDATE},logical=Logical.OR)
    public void edit() {
        String id = getPara();
        if (id != null) {
            Contract contract = Contract.dao.findById(id);
            Contact contact = Contact.dao.findFirst("select * from party p left join contact c on p.contact_id =c.id where p.id ='"
                    + contract.get("party_id") + "'");
            
            setAttr("c", contact);
            setAttr("ul", contract);
            String contract_type = contract.get("type");
            List<FinItem> finItemList = null;
            String fin_type="应付";
            if("CUSTOMER".equals(contract_type)){
            	fin_type = "应收";
            }
            finItemList = FinItem.dao.find("select * from fin_item where type='" + fin_type + "'");
            setAttr("finItemList", finItemList);
        }
            render("/yh/contract/ContractEdit.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CC_CREATE, PermissionConstant.PERMSSION_CC_UPDAET,PermissionConstant.PERMSSION_CD_CREATE,PermissionConstant.PERMSSION_CD_UPDATE,PermissionConstant.PERMSSION_CP_CREATE,PermissionConstant.PERMSSION_CP_UPDATE}, logical=Logical.OR)
    public void save() {
        String id = getPara("contractId");
        Date createDate = Calendar.getInstance().getTime();
        Contract c;
        if (id != "") {
            c = Contract.dao.findById(id);
        }else{
        	c = new Contract();
        }
        c.set("name", getPara("contract_name"));
        c.set("party_id", getParaToInt("partyid"));
        c.set("period_from", getPara("period_from"));
        c.set("period_to", getPara("period_to"));
        c.set("remark", getPara("remark"));
        if (id != "") { 
            c.update();
        } else { 
        	c.set("type", getPara("type2"));
            c.save();
           
        }
       
        renderJson(c.get("id"));
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CC_DELETE})
    public void delete() {
        String id = getPara();
        Contract contract=Contract.dao.findById(id);
        Object obj = contract.get("is_stop");
        if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
        	contract.set("is_stop", true);
        }else{
        	contract.set("is_stop", false);
        }
        contract.update();
        /*if (id != null) {
            Db.deleteById("contract", id);
        }*/
            redirect("/customerContract");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CP_DELETE})
    public void delete2() {
        String id = getPara();
        Contract contract=Contract.dao.findById(id);
        Object obj = contract.get("is_stop");
        if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
        	contract.set("is_stop", true);
        }else{
        	contract.set("is_stop", false);
        }
        contract.update();
       /* if (id != null) {
            Db.deleteById("contract", id);
        }*/
            redirect("/spContract/spIndex");
    }
    public void delete3() {
    	String id = getPara();
        Contract contract=Contract.dao.findById(id);
        Object obj = contract.get("is_stop");
        if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
        	contract.set("is_stop", true);
        }else{
        	contract.set("is_stop", false);
        }
        contract.update();
            redirect("/deliverySpContract/deliverySpIndex");
    }

    // 列出客户公司名称
    public void search() {
        String customerName = getPara("locationName");
        if(StringUtils.isEmpty(customerName)){
        	customerName = getPara("customerName");
        }
        
        if(StringUtils.isEmpty(customerName)){
        	customerName = "";
        }
        
        List<Record> locationList = Collections.EMPTY_LIST;
        String sql = "select c.id, c.abbr from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER' "
        		+ " and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
        			
        if (customerName.trim().length() > 0) {
        	sql +=" and (c.abbr like '%" + customerName + "%' or c.quick_search_code like '%" + customerName.toUpperCase() + "%') ";
        }
        locationList = Db.find(sql);

        renderJson(locationList);
    }
    
    public void searcCustomer() {
    	
        String locationName = getPara("locationName");
        
        List<Record> locationList = Collections.EMPTY_LIST;
        if (locationName.trim().length() > 0) {
            locationList = Db
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER' and (p.is_stop is null or p.is_stop = 0) and c.company_name like '%"+locationName+"%' and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')");

        } else {
            locationList = Db.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER' and (p.is_stop is null or p.is_stop = 0)  and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')");

        }
        renderJson(locationList);
       
    }


    // 列出供应商公司名称, 包括：干线，配送
    public void searchSp() {
    	String input = getPara("spName");
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();
		// Long officeID = pom.getCurrentOfficeId();
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select p.*,c.*,p.id as pid, p.payment from party p,contact c,office o where p.contact_id = c.id and p.party_type = '"
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
							+ "%') and o.id = p.office_id and (o.id = ? or o.belong_office = ?)",
							parentID, parentID);
		} else {
			locationList = Db
					.find("select p.id, c.abbr,c.postal_code,c.contact_person,c.email,c.phone,c.address,c.company_name, p.id AS pid from party p "
							+ " LEFT JOIN contact c on p.contact_id = c.id "
							+ " LEFT JOIN office o on o.id = p.office_id"
							+ " where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_SERVICE_PROVIDER
							+ "' and o.id = p.office_id and (o.id = ? or o.belong_office = ?)",
							parentID, parentID);
		}
		renderJson(locationList);
    }
    
    public void searchPart() {
        String locationName = getPara("locationName");
        
        //查询当前用户的父类公司的id
      
        Long parentID = pom.getParentOfficeId();
        Long currentId = pom.getCurrentOfficeId();
        
        // 不能查所有
        if (locationName.trim().length() > 0) {
            List<Record> locationList = Db
                    .find("select *,p.id as pid from party p,contact c,office o where p.contact_id = c.id and o.id = p.office_id  and (p.is_stop is null or p.is_stop = 0) and p.party_type = 'SERVICE_PROVIDER' and c.company_name like ?",
                            "%" + locationName + "% and (o.id = " + currentId + " or o.belong_office  = " + parentID + ")");
            renderJson(locationList);
        } else {
            List<Record> locationList = Db
                    .find("select *,p.id as pid from party p,contact c,office o where p.contact_id = c.id and o.id = p.office_id and p.party_type = 'SERVICE_PROVIDER'  and (p.is_stop is null or p.is_stop = 0) and (o.id = " + currentId + " or o.belong_office  = " + parentID + ")");
            renderJson(locationList);
        }
    }
    // 合同运价（计件）
    public void routeEdit() {
        String contractId = getPara("routId");
        if (contractId.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", 0);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        String sql = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        if (contractId != null && contractId.length() > 0) {
            sql = "select count(1) total from contract_item c where c.contract_id = " + contractId + " and PRICETYPE ='perUnit'";
        }

        System.out.println(sql);
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));        
        // 获取当前页的数据
        List<Record> orders = null;
        if (contractId != null && contractId.length() > 0) {
            orders = Db.find("select c.*, fi.name as fin_item_name , p.item_name,"                            
            		+ " location_from,"
                    + " location_to "
            		+ " from  contract_item c left join product p on c.product_id = p.id left join fin_item fi on c.fin_item_id = fi.id where c.contract_id = "
                            + contractId + " and PRICETYPE ='perUnit' order by id desc" + sLimit);
        }
        
        for (Record record : orders) {
        	replaceLocationName(record);
		}
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }
    
    private void replaceLocationName(Record record){
    	concatLocationName("from", record);
    	concatLocationName("to", record);
    }

	private void concatLocationName(String type, Record record) {
		String colName="from_id";
		String replaceColName="location_from";
		if("to".equals(type)){
			colName="to_id";
			replaceColName="location_to";
		}
		
		String locCode=record.getStr(colName);
		if(locCode != null && !"".equals(locCode)){
			String last2=locCode.substring(4);
			Integer in=Integer.parseInt(last2);
			if(in>0){
				Location loc = Location.dao.findFirst("select p_loc.name p_name, loc.name from location loc left join location p_loc on p_loc.code=loc.pcode where loc.code='"+locCode+"'");
				record.set(replaceColName, loc.getStr("p_name")+loc.getStr("name"));
			}else{
				Location loc = Location.dao.findFirst("select loc.name from location loc where loc.code='"+locCode+"'");
				record.set(replaceColName, loc.getStr("name"));
			}
		}
	}
    
    // 获取始发地,目的地路线
    private String getRouteAddress(String code){
    	List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
		Location l = Location.dao.findFirst("select * from location where code = '" + code + "'");
		Location l2 = Location.dao.findFirst("select * from location where code = (select pcode from location where code = '" + code + "')");
		String str = "";
		if (provinces.contains(l)) {
			str = "select l.name as location from location l left join location where l.code = '" + code + "'";
		} else if (provinces.contains(l2)) {
			str = "select trim(concat(l1.name, ' ', l.name)) location from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
							+ code + "'";
		}else {
			str = "select trim(concat(l2.name, ' ', l1.name, ' ', l.name)) location from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
							+ code + "'";
		}
		Record rec = Db.findFirst(str);
		return rec.getStr("location");
    }

    // 合同运价（整车）
    public void routeEdit2() {
        String contractId = getPara("routId");
        if (contractId.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", 0);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        String sql = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        if (contractId != null && contractId.length() > 0) {
            sql = "select count(1) total from contract_item c where c.contract_id = " + contractId + " and PRICETYPE ='perCar'";
        }

        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = null;
        if (contractId != null && contractId.length() > 0) {
            orders = Db
                    .find("select c.*, fi.name as fin_item_name, p.item_name,"                            
                    		+ " location_from,"
                            + " location_to "
                    		+ " from  contract_item c left join product p on c.product_id = p.id left join fin_item fi on c.fin_item_id = fi.id where c.contract_id = "
                            + contractId + " and PRICETYPE ='perCar'  order by id desc" + sLimit);
        }
        for (Record record : orders) {
        	replaceLocationName(record);
		}
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    // 合同运价（零担）
    public void routeEdit3() {
        String contractId = getPara("routId");
        if (contractId.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", 0);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        String sql = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        if (contractId != null && contractId.length() > 0) {
            sql = "select count(1) total from contract_item c where c.contract_id = " + contractId + " and pricetype ='perCargo'";
        }

        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = null;
        if (contractId != null && contractId.length() > 0) {
            orders = Db
                    .find("select c.*, fi.name as fin_item_name, p.item_name,"                            
                    		+ " location_from,"
                            + " location_to "
                    		+ " from  contract_item c left join product p on c.product_id = p.id left join fin_item fi on c.fin_item_id = fi.id where c.contract_id = "
                            + contractId + " and pricetype ='perCargo'  order by id desc" + sLimit);
        }
        
        for (Record record : orders) {
        	replaceLocationName(record);
		}
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    public void routeAdd() {
        ContractItem item = new ContractItem();
        String contractId = getPara("routeContractId");
        String routeItemId = getPara("routeItemId");
        String priceType = getPara("priceType");
        
        //初始地城市文本
        StringBuffer fromName = new StringBuffer(); 
        fromName.append(getPara("hideProvinceFrom")+" ");
        fromName.append(getPara("hideCityFrom")+" ");
        fromName.append(getPara("hideDistrictFrom"));
        //目的地城市文本
        StringBuffer toName = new StringBuffer(); 
        toName.append(getPara("hideProvinceTo")+" ");
        toName.append(getPara("hideCityTo")+" ");
        toName.append(getPara("hideDistrictTo"));
        //初始地城市id
        StringBuffer fromNameId = new StringBuffer(); 
        fromNameId.append(getPara("mbProvinceFrom") +" ");
        fromNameId.append(getPara("cmbCityFrom")+" ");
        fromNameId.append(getPara("cmbAreaFrom"));
        //目的地城市id
        StringBuffer toNameId = new StringBuffer(); 
        toNameId.append(getPara("mbProvinceTo")+" ");
        toNameId.append(getPara("cmbCityTo")+" ");
        toNameId.append(getPara("cmbAreaTo"));
        String locationFrom = "";
        String locationTo = "";
        if(getPara("route_from") != null && !"".equals(getPara("route_from")))
        	locationFrom = getRouteAddress(getPara("route_from"));
        if(getPara("route_to") != null && !"".equals(getPara("route_to")))
        	locationTo = getRouteAddress(getPara("route_to"));
        
        // 判断合同干线是否存在
        item.set("contract_id", contractId).set("fin_item_id", getPara("fin_item")).set("pricetype", getPara("priceType"))
                .set("from_id", getPara("route_from")).set("to_id", getPara("route_to")).set("location_from", locationFrom).set("location_to", locationTo)
                .set("amount", getPara("price")).set("dayfrom", getPara("day"))
                .set("dayto", getPara("day2"));
        
        if (getPara("productId").equals("")) {
            item.set("product_id", null);
        } else {
            item.set("product_id", getPara("productId"));
        }
        if (routeItemId != "") {

            if (priceType.equals("perUnit")) {
                item.set("id", getPara("routeItemId")).set("cartype", null).set("fin_item_id", getPara("fin_item"))
                        .set("ltlUnitType", null);
                item.set("unit", getPara("unit2"));

                item.update();
                renderJson("{\"success\":true}");
            }
            if (priceType.equals("perCar")) {
                item.set("id", getPara("routeItemId"));
                item.set("cartype", getPara("carType2")).set("fin_item_id", getPara("fin_item"))
                        .set("ltlUnitType", null);

                item.update();
                renderJson("{\"success\":true}");
            }
            if (priceType.equals("perCargo")) {
                item.set("id", getPara("routeItemId")).set("fin_item_id", getPara("fin_item"));
                if(!"".equals(getPara("amountFrom")) && getPara("amountFrom") != null)
                	item.set("amountFrom", getPara("amountFrom"));
                if(!"".equals(getPara("amountTo")) && getPara("amountTo") != null)
                	item.set("amountTo", getPara("amountTo"));
                
                item.set("ltlUnitType", getPara("ltlUnitType")).set("cartype", null);
                item.update();
                renderJson("{\"success\":true}");
            }
        } else {
            if (priceType.equals("perUnit")) {
                item.set("unit", getPara("unit2"));
                item.save();
                renderJson("{\"success\":true}");
            }
            if (priceType.equals("perCar")) {
                item.set("cartype", getPara("carType2"));
                item.save();
                renderJson("{\"success\":true}");
            }
            if (priceType.equals("perCargo")) {
                item.set("ltlUnitType", getPara("ltlUnitType"));
                if(!"".equals(getPara("amountFrom")) && getPara("amountFrom") != null)
                	item.set("amountFrom", getPara("amountFrom"));
                if(!"".equals(getPara("amountTo")) && getPara("amountTo") != null)
                	item.set("amountTo", getPara("amountTo"));
                item.save();
                renderJson("{\"success\":true}");
            }
        }
    }

    /*
     * // 通过输入起点和终点判断干线id public void searchRoute() { String fromName =
     * getPara("fromName"); String toName = getPara("toName");
     * System.out.println(fromName); System.out.println(toName); List<Route>
     * routeId = Route.dao
     * .find("select id as rId from CONTRACT_ITEM where location_from like '%" +
     * fromName + "%' and location_to like '%" + toName + "%'");
     * System.out.println(routeId); renderJson(routeId); }
     */

    public void contractRouteEdit() {
        String id = getPara();

        String contractId = getPara("contractId");
        System.out.println(id);
        // Route route = Route.dao.findById(id);
        Contract contract = Contract.dao.findById(contractId);
        List<Record> list = null;
        if (contract.get("party_id") != null) {
            list = Db
                    .find("select c.*,p.id as pid,p.item_name from contract_item c left join product p on p.id =c.product_id where c.contract_id ='"
                            + contractId + "' and c.id = '" + id + "'");
        } else {
            list = Db.find("select c.*,'null' as item_name from contract_item c where c.contract_id ='" + contractId + "' and c.id = '"
                    + id + "'");

        }
        renderJson(list);
    }

    public void routeDelete() {
        String id = getPara();
        if (id != null) {
            Db.deleteById("contract_item", id);
        }
        renderJson("{\"success\":true}");
    }

    // 查找产品名
    public void searchItemName() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db.find("select * from product where category_id in (select id from category where customer_id = " + customerId
                    + ") and item_name like '%" + input + "%' limit 0,10");
        } else {
            locationList = Db.find("select * from product where category_id in (select id from category where customer_id = " + customerId
                    + ")");
        }
        renderJson(locationList);
    }

    // input控件列出城市列表
    public void searchlocation() {
        String locationName = getPara("locationName");

        // 不能查所有
        if (locationName.trim().length() > 0) {

            locationName = locationName.replaceAll("'", "''");// 转义，地点查询中经常出现单引号
            List<Record> locationList = Db.find("select * from location where name like '%" + locationName + "%' or code like '%"
                    + locationName + "%' or pcode like '%" + locationName + "%' order by id limit 10");
            renderJson(locationList);
        } else {
            renderJson("");
        }
    }
    //保存校验
    public void checkedRepetition(){
    	String contractId = getPara("contractId");
    	String priceType = getPara("priceType");
    	String fromId = getPara("fromId");
    	String toId = getPara("toId");
    	String productId = getPara("productId");
    	String carType2 = getPara("carType2");
    	//String carLength2 = getPara("carLength2");
    	String ltlUnitType = getPara("ltlUnitType");
    	String amountFrom = getPara("amountFrom");
    	String amountTo = getPara("amountTo");
    	String finItemId = getPara("finItemId");
    	
    	String sql = "select count(0) total from contract_item  where contract_id  = '"+contractId+"' and pricetype = '"+priceType+"'and from_id = " + fromId + " and  to_id = '"+toId+"' ";
    	if(!"".equals(productId) && productId != null)
    		sql = sql + " and product_id = '"+productId+"' ";
    	else
    		sql = sql + " and product_id is null";
    	if(!"".equals(finItemId) && finItemId != null)
    		sql = sql + " and fin_item_id = '"+finItemId+"' ";
    	
    	if(!"".equals(carType2) && carType2 != null)
    		sql = sql + " and cartype = '"+carType2+"' ";
    	/*if(!"".equals(carLength2) && carLength2 != null)
    		sql = sql + " and carlength = '"+carLength2+"' ";*/
    	if(!"".equals(ltlUnitType) && ltlUnitType != null)
    		sql = sql + " and ltlunittype = '"+ltlUnitType+"' ";
    	if(!"".equals(amountFrom) && amountFrom != null)
    		sql = sql + " and amountFrom is not null ";
    	else
    		sql = sql + " and amountFrom is null ";
    	if(!"".equals(amountTo) && amountTo != null)
    		sql = sql + " and amountTo is not null ";
    	else
    		sql = sql + " and amountTo is null ";
    	
    	Record rec = Db.findFirst(sql);
    	if(rec.getLong("total") > 0)
    		renderJson("{\"success\":true}");
    	else
    		renderJson("{\"success\":false}");
    }
    
    
    public void checkContractNameExist(){
    	boolean checkObjectExist;
		String name= getPara("contract_name");
		String[] str =name.split(",");
		String contract_name = str[0];
		String type ="";
		if(str.length>=2){
			type =str[1];
			Contract contract = Contract.dao.findFirst("select * from contract where name =? and type=?",contract_name,type);
			if(contract == null){
				checkObjectExist=true;
			}else{
				checkObjectExist=false;
			}
		}else{
			checkObjectExist=true;
		}

		renderJson(checkObjectExist);
	}
    
    
}
