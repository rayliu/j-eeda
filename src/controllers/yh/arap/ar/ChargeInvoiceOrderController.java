package controllers.yh.arap.ar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAuditInvoice;
import models.ArapAuditOrder;
import models.ArapAuditOrderInvoice;
import models.Party;
import models.UserLogin;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class ChargeInvoiceOrderController extends Controller {
    private Logger logger = Logger.getLogger(ChargeInvoiceOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
    	if(LoginUserController.isAuthenticated(this))
    	    render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderList.html");
    }

    public void add() {
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "");
    	if(LoginUserController.isAuthenticated(this))
        render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderCreateSearchList.html");
    }

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

        ArapAuditInvoice order = ArapAuditInvoice.dao.findFirst("select * from arap_audit_invoice order by order_no desc limit 0,1");
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

        setAttr("status", "new");
    	if(LoginUserController.isAuthenticated(this))
    		render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderEdit.html");
    }

    // 创建应收对帐单时，先选取合适的回单，条件：客户，时间段
    public void createList() {
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_audit_order where status = 'confirmed'";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aao.*,c.abbr cname,ror.order_no return_order_no,tor.order_no transfer_order_no,dor.order_no delivery_order_no,ul.user_name creator_name from arap_audit_order aao "
						+" left join party p on p.id = aao.payee_id "
						+" left join contact c on c.id = p.contact_id "
						+" left join arap_audit_item aai on aai.audit_order_id= aao.id "
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
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_audit_invoice";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aaie.* ,aao.order_no check_order_no,c.abbr cname,ror.order_no return_order_no,tor.order_no transfer_order_no,dor.order_no delivery_order_no,ul.user_name creator_name from arap_audit_invoice aaie"
						+" left join arap_audit_order_invoice aaoi on aaie.id = aaoi.audit_invoice_id"
						+" left join arap_audit_order aao on aao.id = aaoi.audit_order_id  "
						+" left join party p on p.id = aao.payee_id "
						+" left join contact c on c.id = p.contact_id "
						+" left join arap_audit_item aai on aai.audit_order_id= aao.id "
						+" left join return_order ror on ror.id = aai.ref_order_id "
						+" left join transfer_order tor on tor.id = ror.transfer_order_id "
						+" left join delivery_order dor on dor.id = ror.delivery_order_id "
				        +" left join user_login ul on ul.id = aao.create_by order by aao.create_stamp desc";

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    public void save(){
    	ArapAuditInvoice arapAuditInvoice = null;
    	String chargeInvoiceOrderId = getPara("chargeInvoiceOrderId");
    	if("".equals(chargeInvoiceOrderId) || chargeInvoiceOrderId == null){
    		arapAuditInvoice = new ArapAuditInvoice();
    		arapAuditInvoice.set("order_no", getPara("order_no"));
			// TODO 由于未处理审核按钮,暂且使用该方式流转
    		arapAuditInvoice.set("status", "confirmed");
    		arapAuditInvoice.set("create_by", getPara("create_by"));
	    	arapAuditInvoice.set("create_stamp", new Date());
	    	arapAuditInvoice.set("remark", getPara("remark"));
	    	arapAuditInvoice.save();
	    	
	    	String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
	    	String[] chargeCheckOrderIdsArr = chargeCheckOrderIds.split(",");
	    	for(int i=0;i<chargeCheckOrderIdsArr.length;i++){
		    	ArapAuditOrderInvoice arapAuditOrderInvoice = new ArapAuditOrderInvoice();
		    	arapAuditOrderInvoice.set("audit_order_id", chargeCheckOrderIdsArr[i]);
		    	arapAuditOrderInvoice.set("audit_invoice_id", arapAuditInvoice.get("id"));
		    	arapAuditOrderInvoice.save();
	    	}
    	}else{
    		arapAuditInvoice = ArapAuditInvoice.dao.findById(chargeInvoiceOrderId);
	    	arapAuditInvoice.set("create_stamp", new Date());
	    	arapAuditInvoice.set("remark", getPara("remark"));
	    	arapAuditInvoice.set("last_modified_by", getPara("create_by"));
	    	arapAuditInvoice.set("last_modified_stamp", new Date());
	    	arapAuditInvoice.update();
    	}
        renderJson(arapAuditInvoice);;
    }
    
    public void edit() throws ParseException{
    	ArapAuditInvoice arapAuditInvoice = ArapAuditInvoice.dao.findById(getPara("id"));  	
    	UserLogin userLogin = UserLogin.dao.findById(arapAuditInvoice.get("create_by"));
    	setAttr("userLogin", userLogin);
    	setAttr("ArapAuditInvoice", arapAuditInvoice);
    	
    	String chargeCheckOrderIds = "";
    	List<ArapAuditOrderInvoice> arapAuditOrderInvoices = ArapAuditOrderInvoice.dao.find("select * from arap_audit_order_invoice where audit_invoice_id = ?", arapAuditInvoice.get("id"));
    	for(ArapAuditOrderInvoice arapAuditOrderInvoice : arapAuditOrderInvoices){
    		chargeCheckOrderIds += arapAuditOrderInvoice.get("audit_order_id") + ",";

    		// TODO 此处循环了多次需重新处理
    		ArapAuditOrder arapAuditOrder = ArapAuditOrder.dao.findById(arapAuditOrderInvoice.get("audit_order_id"));
        	String customerId = arapAuditOrder.get("payee_id");
        	if(!"".equals(customerId) && customerId != null){
    	    	Party party = Party.dao.findById(customerId);
    	        setAttr("party", party);	        
    	        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
    	        setAttr("customer", contact);
        	}  
    	}
    	chargeCheckOrderIds = chargeCheckOrderIds.substring(0, chargeCheckOrderIds.length() - 1);
    	setAttr("chargeCheckOrderIds", chargeCheckOrderIds);
     	if(LoginUserController.isAuthenticated(this))
    		render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderEdit.html");
    }
    
    public void returnOrderList() {
    	String chargeInvoiceOrderId = getPara("chargeInvoiceOrderId");
    	if(chargeInvoiceOrderId == null || "".equals(chargeInvoiceOrderId)){
    		chargeInvoiceOrderId = "-1";
    	}
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from return_order ro ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select sum(tofi.amount) amount,ror.*, usl.user_name as creator_name,dor.order_no as delivery_order_no, ifnull(c.contact_person,c2.contact_person) cname, "
                		+ " ifnull(tor.order_no,(select group_concat(tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no from arap_audit_order aao "
						+ "	left join arap_audit_item aai on aai.audit_order_id = aao.id"
						+ "	left join return_order ror on ror.id = aai.ref_order_id"
						+ "	left join transfer_order tor on tor.id = ror.transfer_order_id  left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id"
						+ "	left join delivery_order dor on ror.delivery_order_id = dor.id left join delivery_order_item doi on doi.delivery_id = dor.id"
						+ "	left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=ror.creator "                     
						+ "	left join transfer_order_fin_item tofi on tofi.order_id = ifnull(tor.id,tor2.id)"             
						+ "	left join fin_item fi on fi.id = tofi.fin_item_id" 
						+ "	where fi.type = '应收' and aai.ref_order_id = ror.id and aao.id = "+chargeInvoiceOrderId+" order by ror.create_date desc " + sLimit);

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
        String sql = "select count(1) total from arap_audit_order aao where aao.id in ("+chargeCheckOrderIds+")";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select sum(tofi.amount) amount,aao.*,ror.order_no return_order_no, usl.user_name as creator_name,dor.order_no as delivery_order_no, ifnull(c.contact_person,c2.contact_person) cname, "
                		+ " ifnull(tor.order_no,(select group_concat(tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no from arap_audit_order aao "
						+ "	left join arap_audit_item aai on aai.audit_order_id = aao.id"
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
}
