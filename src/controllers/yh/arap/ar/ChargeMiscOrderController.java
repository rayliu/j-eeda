package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapChargeInvoiceApplication;
import models.ArapMiscChargeOrder;
import models.ArapMiscChargeOrderItem;
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

import controllers.yh.util.OrderNoUtil;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeMiscOrderController extends Controller {
    private Logger logger = Logger.getLogger(ChargeMiscOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_LIST})
    public void index() {
    	   render("/yh/arap/ChargeMiscOrder/ChargeMiscOrderList.html");
    }


    public void confirm() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        String customerId = getPara("customerId");
        Party party = Party.dao.findById(customerId);

        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
        setAttr("customer", contact);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
        render("/yh/arap/ChargeAcceptOrder/ChargeCheckOrderEdit.html");
    }

    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_LIST})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_misc_charge_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select amco.*,aco.order_no charge_order_no from arap_misc_charge_order amco"
					+ " left join arap_charge_order aco on aco.id = amco.charge_order_id order by amco.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	public void create() {
		String ids = getPara("ids");
		String[] idArray = ids.split(",");
		logger.debug(String.valueOf(idArray.length));

		setAttr("chargeCheckOrderIds", ids);
		String beginTime = getPara("beginTime");
		if (beginTime != null && !"".equals(beginTime)) {
			setAttr("beginTime", beginTime);
		}
		String endTime = getPara("endTime");
		if (endTime != null && !"".equals(endTime)) {
			setAttr("endTime", endTime);
		}
		String customerId = getPara("customerId");
		if (!"".equals(customerId) && customerId != null) {
			Party party = Party.dao.findById(customerId);
			setAttr("party", party);
			Contact contact = Contact.dao.findById(party.get("contact_id")
					.toString());
			setAttr("customer", contact);
			setAttr("type", "CUSTOMER");
			setAttr("classify", "");
		}

		setAttr("saveOK", false);
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));

		String sql = "select * from arap_charge_invoice_application_order order by id desc limit 0,1";
		setAttr("order_no", OrderNoUtil.getOrderNo(sql,"SGSK"));
		
		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);
		setAttr("status", "new");
			render("/yh/arap/ChargeMiscOrder/ChargeMiscOrderEdit.html");
	}
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE,PermissionConstant.PERMSSION_CPIO_UPDATE},logical=Logical.OR)
	public void save() {
		ArapMiscChargeOrder arapMiscChargeOrder = null;
		String chargeMiscOrderId = getPara("chargeMiscOrderId");
		String paymentMethod = getPara("paymentMethod");
		if (!"".equals(chargeMiscOrderId) && chargeMiscOrderId != null) {
			arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(chargeMiscOrderId);
			arapMiscChargeOrder.set("type", getPara("type"));
			arapMiscChargeOrder.set("remark", getPara("remark"));
			arapMiscChargeOrder.set("payment_method", getPara("paymentMethod"));
			if("transfers".equals(paymentMethod)){
				if(getPara("accountTypeSelect") != null && !"".equals(getPara("accountTypeSelect"))){
					arapMiscChargeOrder.set("account_id", getPara("accountTypeSelect"));
				}
			}else{
				arapMiscChargeOrder.set("account_id", null);				
			}
			arapMiscChargeOrder.update();
		} else {
			arapMiscChargeOrder = new ArapMiscChargeOrder();
			arapMiscChargeOrder.set("order_no", getPara("order_no"));
			arapMiscChargeOrder.set("status", "新建");
			arapMiscChargeOrder.set("type", getPara("type"));
			arapMiscChargeOrder.set("create_by", getPara("create_by"));
			arapMiscChargeOrder.set("create_stamp", new Date());
			arapMiscChargeOrder.set("remark", getPara("remark"));
			arapMiscChargeOrder.set("charge_order_id", getPara("chargeCheckOrderIds"));
			arapMiscChargeOrder.set("payment_method", getPara("paymentMethod"));
			if("transfers".equals(paymentMethod)){
				if(getPara("accountTypeSelect") != null && !"".equals(getPara("accountTypeSelect"))){
					arapMiscChargeOrder.set("account_id", getPara("accountTypeSelect"));
				}
			}else{
				arapMiscChargeOrder.set("account_id", null);				
			}
			arapMiscChargeOrder.save();
		}
		renderJson(arapMiscChargeOrder);;
	}
	
	// 审核
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_APPROVAL})
	public void auditChargePreInvoiceOrder(){
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		if(chargePreInvoiceOrderId != null && !"".equals(chargePreInvoiceOrderId)){
			ArapChargeInvoiceApplication arapAuditOrder = ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId);
			arapAuditOrder.set("status", "已审核");
            String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
			arapAuditOrder.set("audit_by", users.get(0).get("id"));
			arapAuditOrder.set("audit_stamp", new Date());
			arapAuditOrder.update();
		}
        renderJson("{\"success\":true}");
	}
	
	// 审批
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CONFIRMATION})
	public void approvalChargePreInvoiceOrder(){
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		if(chargePreInvoiceOrderId != null && !"".equals(chargePreInvoiceOrderId)){
			ArapChargeInvoiceApplication arapAuditOrder = ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId);
			arapAuditOrder.set("status", "已审批");
            String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
			arapAuditOrder.set("approver_by", users.get(0).get("id"));
			arapAuditOrder.set("approval_stamp", new Date());
			arapAuditOrder.update();
		}
		renderJson("{\"success\":true}");
	}
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_UPDATE})
	public void edit() throws ParseException {
		String id = getPara("id");
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);
		
		ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(id);
		setAttr("arapMiscChargeOrder", arapMiscChargeOrder);
			render("/yh/arap/ChargeMiscOrder/ChargeMiscOrderEdit.html");
	}
    
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	public void chargeMiscOrderItemList() {
		String chargeMiscOrderId = getPara("chargeMiscOrderId");
		if(chargeMiscOrderId == null || "".equals(chargeMiscOrderId)){
			chargeMiscOrderId = "-1";
		}
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map orderMap = new HashMap();
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from arap_misc_charge_order_item amcoi "
					+ " left join arap_misc_charge_order amco on amco.id = amcoi.misc_order_id where amco.id = "+chargeMiscOrderId;
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select amcoi.*,amco.order_no charge_order_no,c.abbr cname,fi.name name from arap_misc_charge_order_item amcoi"
					+ " left join arap_misc_charge_order amco on amco.id = amcoi.misc_order_id"
					+ " left join arap_charge_order aco on aco.id = amco.charge_order_id"
					+ " left join party p on p.id = aco.payee_id"
					+ " left join contact c on c.id = p.contact_id"
					+ " left join fin_item fi on amcoi.fin_item_id = fi.id"
					+ " where amco.id = "+ chargeMiscOrderId +" " + sLimit);

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
	
	public void searchAllAccount(){
		List<Account> accounts = Account.dao.find("select * from fin_account where type != 'PAY'");
		renderJson(accounts);
	}
	
	public void addNewFee(){
		ArapMiscChargeOrderItem arapMiscChargeOrderItem = new ArapMiscChargeOrderItem();
		arapMiscChargeOrderItem.set("status", "新建");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
		arapMiscChargeOrderItem.set("creator", users.get(0).get("id"));
		arapMiscChargeOrderItem.set("create_date", new Date());
		arapMiscChargeOrderItem.set("misc_order_id", getPara("chargeMiscOrderId"));
		arapMiscChargeOrderItem.save();
		renderJson(arapMiscChargeOrderItem);
	}
	
	public void updateChargeMiscOrderItem(){
		String paymentId = getPara("paymentId");
		String name = getPara("name");
		String value = getPara("value");
		if ("amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		if (paymentId != null && !"".equals(paymentId)) {
			ArapMiscChargeOrderItem arapMiscChargeOrderItem = ArapMiscChargeOrderItem.dao.findById(paymentId);
			arapMiscChargeOrderItem.set(name, value);
			arapMiscChargeOrderItem.update();
		}
		renderJson("{\"success\":true}");
	}
	
	public void finItemdel(){
		ArapMiscChargeOrderItem.dao.deleteById(getPara());
		renderJson("{\"success\":true}");
	}
	
	public void chargeCheckList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
		if(chargeCheckOrderIds == null || "".equals(chargeCheckOrderIds)){
			chargeCheckOrderIds = "-1";
		}
		String sqlTotal = "select count(1) total from arap_charge_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select distinct aao.*, usl.user_name as creator_name,c.abbr cname"
				+ " from arap_charge_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login usl on usl.id=aao.create_by"
				+ " where aao.id in ("+chargeCheckOrderIds+") order by aao.create_stamp desc " + sLimit;

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
