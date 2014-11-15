package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeApplicationInvoiceNo;
import models.ArapChargeInvoice;
import models.ArapChargeInvoiceItemInvoiceNo;
import models.Party;
import models.UserLogin;
import models.yh.profile.Contact;

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

import controllers.yh.util.PermissionConstant;

//@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeInvoiceOrderController extends Controller {
    private Logger logger = Logger.getLogger(ChargeInvoiceOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CIO_LIST})
    public void index() {
    	    render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderList.html");
    }

    public void add() {
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "");
        render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderCreateSearchList.html");
    }
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CIO_CREATE})
    public void create() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        setAttr("chargeCheckOrderIds", ids);	 
        String customerId = getPara("customerId");
        if(!"".equals(customerId) && customerId != null){
	        Party party = Party.dao.findById(customerId);
	        setAttr("party", party);	        
	        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
	        setAttr("customer", contact);
	        setAttr("type", "CUSTOMER");
	    	setAttr("classify", "");
        }
        
        String order_no = null;
        setAttr("saveOK", false);
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));

        ArapChargeInvoice order = ArapChargeInvoice.dao.findFirst("select * from arap_charge_invoice order by order_no desc limit 0,1");
        if (order != null) {
            String num = order.get("order_no");
            String str = num.substring(4, num.length());
            Long oldTime = Long.parseLong(str);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            String time = format + "00001";
            Long newTime = Long.parseLong(time);
            if (oldTime >= newTime) {
                order_no = String.valueOf((oldTime + 1));
            } else {
                order_no = String.valueOf(newTime);
            }
            setAttr("order_no", "YSFP" + order_no);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            order_no = format + "00001";
            setAttr("order_no", "YSFP" + order_no);
        }

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);

        setAttr("status", "新建");
    		render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderEdit.html");
    }

    // 创建应收对帐单时，先选取合适的回单，条件：客户，时间段
    public void createList() {
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_charge_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aao.*,c.abbr cname,ror.order_no return_order_no,tor.order_no transfer_order_no,dor.order_no delivery_order_no,ul.user_name creator_name from arap_charge_order aao "
						+" left join party p on p.id = aao.payee_id "
						+" left join contact c on c.id = p.contact_id "
						+" left join arap_charge_item aai on aai.charge_order_id= aao.id "
						+" left join return_order ror on ror.id = aai.ref_order_id "
						+" left join transfer_order tor on tor.id = ror.transfer_order_id "
						+" left join delivery_order dor on dor.id = ror.delivery_order_id "
				        +" left join user_login ul on ul.id = aao.create_by where aao.status = 'checking' order by aao.create_stamp desc";

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }

    // billing order 列表
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CIO_LIST})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_charge_invoice";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aci.*,group_concat(acai.invoice_no separator '\r\n') invoice_item_no,ul.user_name creator_name from arap_charge_invoice aci"
				+ " left join arap_charge_invoice_item_invoice_no acio on acio.invoice_id = aci.id "
				+ " left join arap_charge_application_invoice_no acai on acai.invoice_no = acio.invoice_no"
				+ " left join arap_charge_invoice_application_order acao on acao.id = acai.application_order_id"
				+ " left join user_login ul on ul.id = aci.create_by "
				+ " group by aci.id order by aci.create_stamp desc";

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CIO_CREATE, PermissionConstant.PERMSSION_CIO_UPDATE}, logical=Logical.OR)
    public void save(){
    	ArapChargeInvoice arapAuditInvoice = null;
    	String chargeInvoiceOrderId = getPara("chargeInvoiceOrderId");
    	if(!"".equals(chargeInvoiceOrderId) && chargeInvoiceOrderId != null){
    		arapAuditInvoice = ArapChargeInvoice.dao.findById(chargeInvoiceOrderId);
	    	arapAuditInvoice.set("create_stamp", new Date());
	    	arapAuditInvoice.set("remark", getPara("remark"));
	    	arapAuditInvoice.set("last_modified_by", getPara("create_by"));
	    	arapAuditInvoice.set("last_modified_stamp", new Date());
	    	arapAuditInvoice.update();
    	}else{
    		arapAuditInvoice = new ArapChargeInvoice();
    		arapAuditInvoice.set("order_no", getPara("order_no"));
    		arapAuditInvoice.set("create_by", getPara("create_by"));
    		arapAuditInvoice.set("status", getPara("status"));
	    	arapAuditInvoice.set("create_stamp", new Date());
	    	arapAuditInvoice.set("remark", getPara("remark"));
	    	arapAuditInvoice.save();
    	}
        renderJson(arapAuditInvoice);;
    }
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CIO_UPDATE})
    public void edit() throws ParseException{
    	ArapChargeInvoice arapAuditInvoice = ArapChargeInvoice.dao.findById(getPara("id"));  	
    	UserLogin userLogin = UserLogin.dao.findById(arapAuditInvoice.get("create_by"));
    	setAttr("userLogin", userLogin);
    	setAttr("ArapAuditInvoice", arapAuditInvoice);
    	
    		render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderEdit.html");
    }
    
    public void chargeInvoiceOrderList() {
    	String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from arap_charge_order aao where aao.id in ("+chargeCheckOrderIds+")";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select sum(tofi.amount) amount,aao.*,ror.order_no return_order_no, usl.user_name as creator_name,dor.order_no as delivery_order_no, ifnull(c.contact_person,c2.contact_person) cname, "
                		+ " ifnull(tor.order_no,(select group_concat(tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no from arap_charge_order aao "
						+ "	left join arap_charge_item aai on aai.charge_order_id = aao.id"
						+ "	left join return_order ror on ror.id = aai.ref_order_id"
						+ "	left join transfer_order tor on tor.id = ror.transfer_order_id  left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id"
						+ "	left join delivery_order dor on ror.delivery_order_id = dor.id left join delivery_order_item doi on doi.delivery_id = dor.id"
						+ "	left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=ror.creator "                     
						+ "	left join transfer_order_fin_item tofi on tofi.order_id = ifnull(tor.id,tor2.id)"             
						+ "	left join fin_item fi on fi.id = tofi.fin_item_id" 
						+ "	where aao.id in ("+chargeCheckOrderIds+") group by ror.id order by ror.create_date desc " + sLimit);

        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);

        renderJson(orderMap);
    }
    
    // 添加发票
    public void addInvoiceItem(){
    	String chargeInvoiceOrderId = getPara("chargeInvoiceOrderId");
    	if(chargeInvoiceOrderId != null && !"".equals(chargeInvoiceOrderId)){
    		ArapChargeInvoiceItemInvoiceNo arapChargeInvoiceItemInvoiceNo = new ArapChargeInvoiceItemInvoiceNo();
    		arapChargeInvoiceItemInvoiceNo.set("invoice_id", chargeInvoiceOrderId);
    		arapChargeInvoiceItemInvoiceNo.save();
    	}
        renderJson("{\"success\":true}");
    }
    
    // 发票号列表
    public void chargeInvoiceItemList(){
    	String chargeInvoiceOrderId = getPara("chargeInvoiceOrderId");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from arap_charge_invoice_item_invoice_no where invoice_id=" + chargeInvoiceOrderId;
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select group_concat(acao.order_no separator '\r\n') pre_order_no,acio.*"
        		+ " from arap_charge_invoice_item_invoice_no acio "
				+ " left join arap_charge_application_invoice_no acai on acai.invoice_no = acio.invoice_no"
				+ " left join arap_charge_invoice_application_order acao on acao.id = acai.application_order_id"
				+ " where acio.invoice_id=" + chargeInvoiceOrderId + " group by acio.id " + sLimit);

        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);

        renderJson(orderMap);
    }
    
    // 更新InvoiceItem信息
    public void updateInvoiceItem(){
    	List<ArapChargeInvoiceItemInvoiceNo> arapChargeInvoiceItemInvoiceNos = new ArrayList<ArapChargeInvoiceItemInvoiceNo>();
    	ArapChargeInvoiceItemInvoiceNo itemInvoiceNo = ArapChargeInvoiceItemInvoiceNo.dao.findById(getPara("invoiceItemId"));
    	String name = getPara("name");
    	String value = getPara("value");
    	if("amount".equals(name) && "".equals(value)){
    		value = "0";
    	}
    	itemInvoiceNo.set(name, value);
    	itemInvoiceNo.update();
    	if("invoice_no".equals(name) && !"".equals(value)){
    		arapChargeInvoiceItemInvoiceNos = ArapChargeInvoiceItemInvoiceNo.dao.find("select * from arap_charge_invoice_item_invoice_no where invoice_id = ?", getPara("chargeInvoiceOrderId"));    		
    	}
        renderJson(arapChargeInvoiceItemInvoiceNos);
    }
    
    // 获取所有的发票号
    public void findAllInvoiceItemNo(){
    	List<ArapChargeInvoiceItemInvoiceNo> arapChargeInvoiceItemInvoiceNos = ArapChargeInvoiceItemInvoiceNo.dao.find("select * from arap_charge_invoice_item_invoice_no where invoice_id = ?", getPara("chargeInvoiceOrderId"));
        renderJson(arapChargeInvoiceItemInvoiceNos);
    }
    
    // 更新开票申请单
    public void updatePreInvoice(){
    	String name = getPara("name");
    	String value = getPara("value");
    	
    	String[] values = null;
    	Map<String,String[]> map = getParaMap();
    	for(Map.Entry<String,String[]> entry : map.entrySet()){
    		if("value[]".equals(entry.getKey())){
    			values = entry.getValue();
    		}
    	}
    	String preInvoiceId = getPara("preInvoiceId");
    	List<ArapChargeApplicationInvoiceNo> arapChargeApplicationInvoiceNos = ArapChargeApplicationInvoiceNo.dao.find("select * from arap_charge_application_invoice_no where application_order_id = ?", preInvoiceId);
    	for(ArapChargeApplicationInvoiceNo chargeApplicationInvoiceNo : arapChargeApplicationInvoiceNos){
    		chargeApplicationInvoiceNo.delete();
    	}
    	for(int i=0;i<values.length;i++){
	    	ArapChargeApplicationInvoiceNo arapChargeApplicationInvoiceNo = ArapChargeApplicationInvoiceNo.dao.findFirst("select * from arap_charge_application_invoice_no where application_order_id = ? and invoice_no = ?", preInvoiceId, values[i]);
	    	if(arapChargeApplicationInvoiceNo == null){
	    		arapChargeApplicationInvoiceNo = new ArapChargeApplicationInvoiceNo();
	    		arapChargeApplicationInvoiceNo.set(name, values[i]);
		    	arapChargeApplicationInvoiceNo.set("application_order_id", preInvoiceId);
		    	arapChargeApplicationInvoiceNo.save();
	    	}
    	}
        renderJson("{\"success\":true}");
    }
    
    public void chargePreInvoiceOrderList(){
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_charge_invoice_application_order where status = '已审批' ";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aaia.*,ul.user_name create_by,ul2.user_name audit_by,ul3.user_name approval_by,"
				+ " (select group_concat(acai.invoice_no) from arap_charge_invoice_application_order aaia"
				+ " left join arap_charge_application_invoice_no acai on acai.application_order_id = aaia.id) invoice_no"
        		+ " from arap_charge_invoice_application_order aaia "
				+ " left join user_login ul on ul.id = aaia.create_by"
				+ " left join user_login ul2 on ul2.id = aaia.audit_by"
				+ " left join user_login ul3 on ul3.id = aaia.approver_by where aaia.status = '已审批' order by aaia.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
}
