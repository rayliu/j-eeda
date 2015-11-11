package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapAccountAuditLog;
import models.ArapChargeInvoice;
import models.ArapChargeInvoiceApplication;
import models.ArapChargeOrder;
import models.ArapCostOrder;
import models.ChargeApplicationOrderRel;
import models.Party;
import models.UserLogin;
import models.yh.arap.ArapAccountAuditSummary;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.arap.inoutorder.ArapInOutMiscOrder;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargePreInvoiceOrderController extends Controller {
    private Logger logger = Logger.getLogger(ChargePreInvoiceOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_LIST})
    public void index() {
    	   render("/yh/arap/ChargePreInvoiceOrder/ChargePreInvoiceOrderList.html");
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

    // 应付条目列表
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_LIST})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String customer = getPara("customer");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String status = getPara("status");
        String orderNo = getPara("orderNo");
        
        String sqlTotal = "";
        String sql = "select aaia.*,"
        		+ " c.abbr cname,"
        		+ " ifnull(ul.c_name, ul.user_name) as create_by,"
        		+ " ifnull(ul2.c_name, ul2.user_name) audit_by,"
        		+ " ifnull(ul3.c_name, ul3.user_name) approval_by ,"
        		+ " (select case "
        		+ "	when aci.status = '已收款确认' then aci.status "
				+ "	when aci.status != '已收款确认' and aci.status != '' then '开票记录中'"
				+ "	else aciao.status"
				+ "	end "
				+ "	from arap_charge_invoice_application_order aciao"
				+ "	left join arap_charge_invoice aci on aciao.invoice_order_id = aci.id"
				+ "	where aciao.id = aaia.id) as order_status,"
				+ " c1.abbr sp ,"
				+ " (select GROUP_CONCAT(aco.order_no SEPARATOR '<br/>')"
				+ " from arap_charge_order aco "
				+ " LEFT JOIN charge_application_order_rel cao on cao.charge_order_id = aco.id "
				+ " where cao.application_order_id = aaia.id"
				+ " ) dzd_order_no"
        		+ " from arap_charge_invoice_application_order aaia "
				+ " left join party p on p.id = aaia.payee_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " left join contact c1 on c1.id = aaia.sp_id"
				+ " left join user_login ul on ul.id = aaia.create_by"
				+ " left join user_login ul2 on ul2.id = aaia.audit_by"
				+ " left join user_login ul3 on ul3.id = aaia.approver_by ";
         String condition = "";
        if(customer == null && beginTime == null && endTime == null 
        		&& status == null && orderNo ==null ){
        	condition = " ";
        }else{
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			
			condition = " where ifnull(c.abbr,'') like '%" + customer + "%' "
					+ " and ifnull(aaia.order_no,'') like '%" + orderNo + "%' "
					+ " and aaia.create_stamp between '" + beginTime + "' and '" + endTime + "' ";
			if(status != null && !"".equals(status)){
				condition = condition + " and aaia.status = '" + status +"' ";
			}
        }
        
       sqlTotal = "select count(1) total from arap_charge_invoice_application_order aaia "
				+ " left join party p on p.id = aaia.payee_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login ul on ul.id = aaia.create_by"
				+ " left join user_login ul2 on ul2.id = aaia.audit_by"
				+ " left join user_login ul3 on ul3.id = aaia.approver_by ";
        
       
        

        Record rec = Db.findFirst(sqlTotal + condition);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql + condition + " order by aaia.create_stamp desc "+ sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
//	public void create() {
//		String ids = getPara("ids");
//
//		setAttr("chargeCheckOrderIds", ids);
//		if(ids != null && !"".equals(ids)){
//			String[] idArray = ids.split(",");
//			logger.debug(String.valueOf(idArray.length));
//			Double totalAmount = 0.0;
//			Double totalReceive = 0.0;
//			for(int i=0;i<idArray.length;i++){
//				ArapChargeOrder rOrder = ArapChargeOrder.dao.findById(idArray[i]);
//				Double chargeTotalAmount = rOrder.getDouble("charge_amount")==null?0.0:rOrder.getDouble("charge_amount");
//				totalAmount = totalAmount + chargeTotalAmount;
//				ChargeApplicationOrderRel c = ChargeApplicationOrderRel.dao.findFirst("select sum(receive_amount) totalReceive from charge_application_order_rel where charge_order_id = ?",idArray[i]);
//				Double chargeTotalReceive = c.getDouble("totalReceive")==null?0.0:c.getDouble("totalReceive");
//				totalReceive = totalReceive + chargeTotalReceive;
//			}
//			setAttr("total_amount", totalAmount);
//			setAttr("total_receive", totalReceive);
//			setAttr("total_noreceive",totalAmount-totalReceive);
//			
//			ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(idArray[0]);
//			Long customerId = arapChargeOrder.get("payee_id");
//			
//			Long spId = arapChargeOrder.get("sp_id");
//			setAttr("spId", spId);
//			if (!"".equals(customerId) && customerId != null) {
//				Party party = Party.dao.findById(customerId);
//				setAttr("party", party);
//				Contact contact = Contact.dao.findById(party.get("contact_id"));
//				setAttr("customer", contact);
//				setAttr("type", "CUSTOMER");
//				setAttr("classify", "");
//			}
//		}
//
//		setAttr("saveOK", false);
//		String name = (String) currentUser.getPrincipal();
//		List<UserLogin> users = UserLogin.dao
//				.find("select * from user_login where user_name='" + name + "'");
//		setAttr("create_by", users.get(0).get("id"));
//		
//		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
//		setAttr("userLogin", userLogin);
//
//		setAttr("status", "new");
//			render("/yh/arap/ChargePreInvoiceOrder/ChargePreInvoiceOrderEdit.html");
//	}
    
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE,PermissionConstant.PERMSSION_CPIO_UPDATE},logical=Logical.OR)
//    @Before(Tx.class)
//	public void save() {
//		ArapChargeInvoiceApplication arapAuditInvoiceApplication = null;
//		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
//		String paymentMethod = getPara("paymentMethod");
//		String haveInvoice = getPara("invoiceType");
//		String spId = getPara("spId");
//		if (!"".equals(chargePreInvoiceOrderId) && chargePreInvoiceOrderId != null) {
//			arapAuditInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId);
//            //arapAuditInvoiceApplication.set("status", "new");
//			arapAuditInvoiceApplication.set("create_by", getPara("create_by"));
//			arapAuditInvoiceApplication.set("create_stamp", new Date());
//			arapAuditInvoiceApplication.set("remark", getPara("remark"));
//			arapAuditInvoiceApplication.set("last_modified_by", getPara("create_by"));
//			arapAuditInvoiceApplication.set("last_modified_stamp", new Date());
//			arapAuditInvoiceApplication.set("payment_method", paymentMethod);
//			//arapAuditInvoiceApplication.set("have_invoice", haveInvoice);
//			if("transfers".equals(paymentMethod)){
//				if(getPara("accountTypeSelect") != null && !"".equals(getPara("accountTypeSelect"))){
//					arapAuditInvoiceApplication.set("account_id", getPara("accountTypeSelect"));
//				}
//			}else{
//				arapAuditInvoiceApplication.set("account_id", null);				
//			}
//			arapAuditInvoiceApplication.update();
//		} else {
//			arapAuditInvoiceApplication = new ArapChargeInvoiceApplication();
//			arapAuditInvoiceApplication.set("order_no", OrderNoGenerator.getNextOrderNo("YSSQ"));
//			arapAuditInvoiceApplication.set("status", "新建");
//			//arapAuditInvoiceApplication.set("have_invoice", haveInvoice);
//			if(!getPara("customer_id").equals("") && getPara("customer_id")!= null){
//				arapAuditInvoiceApplication.set("payee_id", getPara("customer_id"));
//			}
//			if(!spId.equals("") && spId!= null){
//				arapAuditInvoiceApplication.set("sp_id", spId);
//			}
//			arapAuditInvoiceApplication.set("create_by", getPara("create_by"));
//			arapAuditInvoiceApplication.set("create_stamp", new Date());
//			arapAuditInvoiceApplication.set("remark", getPara("remark"));
//			arapAuditInvoiceApplication.set("payment_method", getPara("paymentMethod"));
//			if("transfers".equals(paymentMethod)){
//				if(getPara("accountTypeSelect") != null && !"".equals(getPara("accountTypeSelect"))){
//					arapAuditInvoiceApplication.set("account_id", getPara("accountTypeSelect"));
//				}
//			}
//			arapAuditInvoiceApplication.save();
//
////			String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
////			String[] chargeCheckOrderIdsArr = chargeCheckOrderIds.split(",");
////			for (int i = 0; i < chargeCheckOrderIdsArr.length; i++) {
////				ArapChargeInvoiceApplicationItem arapAuditInvoiceApplicationItem = new ArapChargeInvoiceApplicationItem();
////				arapAuditInvoiceApplicationItem.set("invoice_application_id", arapAuditInvoiceApplication.get("id"));
////				arapAuditInvoiceApplicationItem.set("charge_order_id", chargeCheckOrderIdsArr[i]);
////				arapAuditInvoiceApplicationItem.save();
////				
////				ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(chargeCheckOrderIdsArr[i]);
////				arapAuditOrder.set("status", "开票申请中");
////				arapAuditOrder.update();
////			}
//			String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
//			String[] chargeCheckOrderIdsArr = chargeCheckOrderIds.split(",");
//			for (int i = 0; i < chargeCheckOrderIdsArr.length; i++) {
//				//更新中间表
//				ChargeApplicationOrderRel chargeApplicationOrderRel = new ChargeApplicationOrderRel();
//				chargeApplicationOrderRel.set("application_order_id", arapAuditInvoiceApplication.getLong("id"));
//				chargeApplicationOrderRel.set("charge_order_id", chargeCheckOrderIdsArr[i]);
//				chargeApplicationOrderRel.save();
//				//更新对账单表
//				ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(chargeCheckOrderIdsArr[i]);
//				arapAuditOrder.set("status", "收款申请中");
//				arapAuditOrder.update();
//			}
//		}
//
//		renderJson(arapAuditInvoiceApplication);;
//	}
	
	// 审核
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_APPROVAL})
	public void auditChargePreInvoiceOrder(){
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		ArapChargeInvoiceApplication arapAuditOrder = null;
		Map map =new HashMap();
		if(chargePreInvoiceOrderId != null && !"".equals(chargePreInvoiceOrderId)){
			arapAuditOrder = ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId);
			arapAuditOrder.set("status", "已审核");
            String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
			arapAuditOrder.set("audit_by", users.get(0).get("id"));
			arapAuditOrder.set("audit_stamp", new Date());
			arapAuditOrder.update();
			map.put("arapAuditOrder", arapAuditOrder);
			map.put("user", users.get(0));
		}
		
        renderJson(map);
	}
	
	// 审批
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CONFIRMATION})
	public void approvalChargePreInvoiceOrder(){
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		Map map =new HashMap();
		if(chargePreInvoiceOrderId != null && !"".equals(chargePreInvoiceOrderId)){
			ArapChargeInvoiceApplication arapAuditOrder = ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId);
			arapAuditOrder.set("status", "已审批");
            String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
			arapAuditOrder.set("approver_by", users.get(0).get("id"));
			arapAuditOrder.set("approval_stamp", new Date());
			arapAuditOrder.update();
			map.put("arapAuditOrder", arapAuditOrder);
			map.put("user", users.get(0));
		}
		renderJson(map);
	}
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_UPDATE})
//	public void edit() throws ParseException {
//		String id = getPara("id");
//		ArapChargeInvoiceApplication arapAuditInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(id);
//		Long customerId = arapAuditInvoiceApplication.getLong("payee_id");
//		if (!"".equals(customerId) && customerId != null) {
//			Party party = Party.dao.findById(customerId);
//			setAttr("party", party);
//			Contact contact = Contact.dao.findById(party.get("contact_id")
//					.toString());
//			setAttr("customer", contact);
//		}
//		String chargeCheckOrderIds = "";
//		List<ChargeApplicationOrderRel> list = ChargeApplicationOrderRel.dao.find("select * from charge_application_order_rel where application_order_id = ?", id);
//		for(ChargeApplicationOrderRel chargeApplicationOrderRel : list){
//			chargeCheckOrderIds += chargeApplicationOrderRel.get("charge_order_id") + ",";
//		}
//		chargeCheckOrderIds = chargeCheckOrderIds.substring(0, chargeCheckOrderIds.length() - 1);
//		setAttr("chargeCheckOrderIds", chargeCheckOrderIds);
//		
//		//计算应收总金额
//		String[] idArray = chargeCheckOrderIds.split(",");
//		Double totalAmount = 0.0;
//		Double totalReceive = 0.0;
//		for(int i=0;i<idArray.length;i++){
//			ArapChargeOrder rOrder = ArapChargeOrder.dao.findById(idArray[i]);
//			Double chargeTotalAmount = rOrder.getDouble("charge_amount")==null?0.0:rOrder.getDouble("charge_amount");
//			totalAmount = totalAmount + chargeTotalAmount;
//			ChargeApplicationOrderRel c = ChargeApplicationOrderRel.dao.findFirst("select sum(receive_amount) totalReceive from charge_application_order_rel where charge_order_id = ?",idArray[i]);
//			Double chargeTotalReceive = c.getDouble("totalReceive")==null?0.0:c.getDouble("totalReceive");
//			totalReceive = totalReceive + chargeTotalReceive;
//		}
//		ChargeApplicationOrderRel chargeApplicationOrderRel1 = ChargeApplicationOrderRel.dao.findFirst("SELECT sum(caor.receive_amount) this_receive FROM charge_application_order_rel caor  WHERE caor.application_order_id=?",id);
//	    Double receive_amount = chargeApplicationOrderRel1.getDouble("this_receive");
//		Double total_noreceive =  totalAmount - totalReceive;
//		setAttr("total_amount", totalAmount);
//	    setAttr("total_receive", totalReceive);
//		setAttr("total_noreceive", total_noreceive);
//		setAttr("receive_amount", receive_amount);
//	    
//		UserLogin userLogin = UserLogin.dao.findById(arapAuditInvoiceApplication.get("create_by"));
//		setAttr("userLogin", userLogin);
//		if(!"".equals(arapAuditInvoiceApplication.get("audit_by")) && arapAuditInvoiceApplication.get("audit_by") != null){
//			UserLogin auditName = UserLogin.dao.findById(arapAuditInvoiceApplication.get("audit_by"));
//			setAttr("auditName", auditName);
//			setAttr("auditData", arapAuditInvoiceApplication.get("audit_stamp"));
//		}
//		if(!"".equals(arapAuditInvoiceApplication.get("approver_by")) && arapAuditInvoiceApplication.get("approver_by") != null){
//			UserLogin approvalName = UserLogin.dao.findById(arapAuditInvoiceApplication.get("approver_by"));
//			setAttr("approvalName", approvalName);
//			setAttr("approvalData", arapAuditInvoiceApplication.get("approval_stamp"));
//		}
//		
//		setAttr("arapAuditInvoiceApplication", arapAuditInvoiceApplication);
//		render("/yh/arap/ChargePreInvoiceOrder/ChargePreInvoiceOrderEdit.html");
//	}
    
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	public void chargeCheckOrderList() {
		String returnOrderIds = getPara("returnOrderIds");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map orderMap = new HashMap();
		
		
		String customer = getPara("customer");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String office = getPara("office");
		String status = getPara("status");
		String orderNo = getPara("orderNo");
		String sql ="select distinct aao.*,  ( SELECT ifnull(sum(caor.receive_amount), 0) total_receive"
				    + " FROM charge_application_order_rel caor"
				    + " WHERE caor.charge_order_id = aao.id ) total_receive,"
				    + " aao.charge_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) total_receive"
				    + " FROM charge_application_order_rel caor"
				    + " WHERE caor.charge_order_id = aao.id ) total_noreceive,"
				    + " ifnull(usl.c_name, usl.user_name) as creator_name,o.office_name oname,c.abbr cname,MONTH(aao.create_stamp)as c_stamp,"
				    + " c1.abbr sp "
					+ " from arap_charge_order aao "
					+ " left join party p on p.id = aao.payee_id "
					+ " left join contact c on c.id = p.contact_id"
					+ " left join contact c1 on c1.id = aao.sp_id"
					+ " left join office o on o.id = p.office_id"
					+ " left join user_login usl on usl.id=aao.create_by"
					+ " where (aao.status = '已确认' "
					+ " OR (( SELECT ifnull(sum(caor.receive_amount), '') total_receive "
					+ " FROM charge_application_order_rel caor WHERE caor.charge_order_id = aao.id ) < aao.charge_amount "
					+ " AND ( SELECT sum(caor.receive_amount) total_receive FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aao.id ) IS NOT NULL ))  and aao.have_invoice is null";
		String sqlTotal ="";
		String condition = "";
		//TODO 网点与对账单状态未做
		
		sqlTotal = "select count(1) total from arap_charge_order where (status = '已确认'"
				+ " OR ( aao. STATUS = '收款申请中' AND ( SELECT ifnull(sum(caor.receive_amount), '') total_receive "
				+ " FROM charge_application_order_rel caor WHERE caor.charge_order_id = aao.id ) < aao.charge_amount "
				+ " AND ( SELECT sum(caor.receive_amount) total_receive FROM charge_application_order_rel caor "
				+ " WHERE caor.charge_order_id = aao.id ) IS NOT NULL ))";
		if(customer == null && beginTime == null && endTime == null 
				&& office == null && status == null && orderNo == null ){
			condition = " order by aao.create_stamp desc ";
		}else{
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			
			condition = " and ifnull(c.abbr,'') like '%" + customer + "%' "
					+ " and ifnull(aao.order_no,'') like '%" + orderNo + "%' "
					+ " and ifnull(o.office_name,'') like '%" + office + "%' "
					+ " and ifnull(aao.STATUS,'') like '%" + status + "%' "
					+ " and aao.create_stamp between '" + beginTime + "' and '" + endTime +"' "
					+ " order by aao.create_stamp desc ";
			
		}

		sqlTotal = "select count(1) total from (" + sql + condition + ") as A";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		List<Record> orders = Db.find(sql + condition + sLimit);
		
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
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	public void chargeOrderListByIds() {
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
		String sLimit = "";
		if(chargeCheckOrderIds == null || "".equals(chargeCheckOrderIds)){
			chargeCheckOrderIds = "-1";
		}
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map orderMap = new HashMap();
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from arap_charge_order where id in("+chargeCheckOrderIds+")";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));
		
		// 获取当前页的数据
		sql = "SELECT aco.*, c.abbr cname, ifnull(ul.c_name, ul.user_name) creator_name,  "
				+ " ( SELECT ifnull(sum(caor.receive_amount), 0) total_receive FROM charge_application_order_rel caor"
				+ " WHERE caor.charge_order_id = aco.id ) total_receive,"
				+ " ( SELECT caor.receive_amount FROM charge_application_order_rel caor"
				+ " WHERE caor.charge_order_id = aco.id AND caor.application_order_id = appl_order.id ) receive_amount,"
				+ " ( aco.charge_amount - ( 	SELECT 	ifnull(sum(caor.receive_amount), 0) "
				+ " FROM charge_application_order_rel caor WHERE caor.charge_order_id = aco.id ) ) noreceive_amount"
				+ " FROM"
				+ " arap_charge_invoice_application_order appl_order"
				+ " LEFT JOIN charge_application_order_rel caor ON caor.application_order_id = appl_order.id"
				+ " LEFT JOIN arap_charge_order aco ON aco.id = caor.charge_order_id"
				+ " LEFT JOIN party p ON p.id = aco.payee_id"
				+ " LEFT JOIN contact c ON c.id = p.contact_id"
				+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
				+ " WHERE appl_order.id = '"+chargePreInvoiceOrderId+"'";
				List<Record> orders = Db.find(sql);
		
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
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	public void chargeCheckOrderList2() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		List<Record> BillingOrders = null;

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}
	
	public void searchAllAccount(){
		List<Account> accounts = Account.dao.find("select * from fin_account where type != 'PAY'");
		renderJson(accounts);
	}
	
	
	
	// 更新ArapChargeOrder信息
	public void updateArapChargeOrder() {
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		String chargeOrderId = getPara("chargeOrderId");
		String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
		
		String[] idArray = chargeCheckOrderIds.split(",");
		Double totalAmount = 0.0;
		Double totalReceive = 0.0;
		String tips = "";
		String sql = "select * from charge_application_order_rel where application_order_id = '"+chargePreInvoiceOrderId+"' and charge_order_id = '"+chargeOrderId+"'";
		ChargeApplicationOrderRel chargeApplicationOrderRel = ChargeApplicationOrderRel.dao.findFirst(sql);
		String name = getPara("name");
		String value = getPara("value");
		Double oldAmount = chargeApplicationOrderRel.getDouble("receive_amount");
		if ("receive_amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		
		if(oldAmount!=null){
			if(!oldAmount.equals(Double.parseDouble(value))){
				chargeApplicationOrderRel.set(name, value).update();
				tips = "success";
			}else{
				tips = "noChange";
			}
		}else{
			chargeApplicationOrderRel.set(name, value).update();
			tips = "success";
		}

		
		
		for(int i=0;i<idArray.length;i++){
			ArapChargeOrder rOrder = ArapChargeOrder.dao.findById(idArray[i]);
			Double chargeTotalAmount = rOrder.getDouble("charge_amount")==null?0.0:rOrder.getDouble("charge_amount");
			totalAmount = totalAmount + chargeTotalAmount;
			
			ChargeApplicationOrderRel c = ChargeApplicationOrderRel.dao.findFirst("select sum(receive_amount) totalReceive from charge_application_order_rel where charge_order_id = ?",idArray[i]);
			Double chargeTotalReceive = c.getDouble("totalReceive")==null?0.0:c.getDouble("totalReceive");
			totalReceive = totalReceive + chargeTotalReceive;
		}
		
		//这张单收入的总金额
		ChargeApplicationOrderRel chargeApplicationOrderRel1 = ChargeApplicationOrderRel.dao.findFirst("SELECT sum(caor.receive_amount) receive_amount FROM charge_application_order_rel caor  WHERE caor.application_order_id=?",chargePreInvoiceOrderId);
		Double receive_amount = chargeApplicationOrderRel1.getDouble("receive_amount");
		
		//更新主表金额数据
		ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId).set("total_amount", receive_amount).update();
		//Double total_amount = ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId).getDouble("total_amount");
		Double total_noreceive = totalAmount - totalReceive;
		Map map = new HashMap();
		map.put("total_receive", totalReceive);
		map.put("receive_amount", receive_amount);
		map.put("total_noreceive", total_noreceive);
		map.put("chargeApplicationOrderRel", chargeApplicationOrderRel);
		map.put("tips", tips);
		renderJson(map);
	}
	
	
	
	//新模块
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	public void create() {
		String ids = getPara("sids");
		setAttr("ids", ids);
		
		String payee_id = "";
		String payee_name = "";
		String deposit_bank = "";
		String bank_no = "";
		String account_name = "";
		String[] orderArrId=ids.split(",");
		for (int i=0;i<orderArrId.length;) {
			String[] one=orderArrId[i].split(":");
			String id = one[0];
			String orderType = one[1];
			if("应收对账单".equals(orderType)){
				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
				payee_id = arapCostOrder.getLong("payee_id").toString();
			}else if("开票记录单".equals(orderType)){
				ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(id);
				payee_id = arapChargeInvoice.getLong("payee_id").toString();
			}else if("手工收入单".equals(orderType)){
				ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(id);
				String type = arapMiscChargeOrder.getStr("charge_from_type");
				payee_name = arapMiscChargeOrder.getStr("others_name");
				if(type.equals("sp")){
					payee_id = arapMiscChargeOrder.getLong("sp_id").toString();
				}else if(type.equals("customer")){
					payee_id = arapMiscChargeOrder.getLong("customer_id").toString();
				}
			}else if("往来票据单".equals(orderType)){
				ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
				payee_name = arapInOutMiscOrder.getStr("charge_person");
			}
			break;
		}
		
		if(!payee_id.equals("")){
			Contact contact = Contact.dao.findById(payee_id);
			deposit_bank = contact.getStr("bank_name");
			bank_no = contact.getStr("bank_no");
			account_name = contact.getStr("receiver");
			setAttr("deposit_bank", deposit_bank);
			setAttr("bank_no", bank_no);
			setAttr("account_name", account_name);
		}
		
		setAttr("payee_id", payee_id);
		setAttr("payee_name", payee_name);
		
			
		List<Record> Account = null;
		Account = Db.find("select * from fin_account where bank_name != '现金'");
		setAttr("accountList", Account);
		
		setAttr("submit_name", LoginUserController.getLoginUserName(this));
		setAttr("status", "new");
		render("/yh/arap/ChargeAcceptOrder/chargeEdit.html");
	}
	
	
	//新模块
	//新逻辑
	public void chargeOrderList() {
        String ids = getPara("ids");
        String application_id = getPara("application_id");
        String dz_id ="" ;//对账单
        String kpjl_id = "";//开票记录单
        String sr_id = "";//收入单
        String wl_id = "";//往来票据单
        String sql = "";
        
        
        if(application_id.equals("")){
        	if(!application_id.equals(ids)){
        		String[] orderArrId=ids.split(",");
 				for (int i=0;i<orderArrId.length;i++) {
 					String[] one=orderArrId[i].split(":");
 					String id = one[0];
 					String orderType = one[1];
 					if("应收对账单".equals(orderType)){
 						dz_id += id+",";
 					}else if("开票记录单".equals(orderType)){
 						kpjl_id += id+",";
 					}else if("手工收入单".equals(orderType)){
 						sr_id += id+",";
 					}else if("往来票据单".equals(orderType)){
 						wl_id += id+",";
 					}
 				}
 				if(!dz_id.equals(""))
 					dz_id = dz_id.substring(0, dz_id.length()-1);
 				else
 					dz_id = "''";
 				if(!kpjl_id.equals(""))
 					kpjl_id = kpjl_id.substring(0, kpjl_id.length()-1);
 				else
 					kpjl_id = "''";
 				if(!sr_id.equals(""))
 					sr_id = sr_id.substring(0, sr_id.length()-1);
 				else
 					sr_id = "''";
 				if(!wl_id.equals(""))
 					wl_id = wl_id.substring(0, wl_id.length()-1);
 				else
 					wl_id = "''";
        	}
	       
			
		
			sql = " SELECT aco.id,aco.payee_id,payee payee_name,aco.order_no, '应收对账单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
					+ " c.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, aco.charge_amount,"
					+ " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '应收对账单' "
					+ " ) receive_amount,"
					+ " (aco.charge_amount - (SELECT ifnull(sum(caor.receive_amount), 0) "
					+ " FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '应收对账单'"
					+ " )) noreceive_amount"
					+ " FROM arap_charge_order aco "
					+ " LEFT JOIN party p ON p.id = aco.payee_id"
					+ " LEFT JOIN contact c ON c.id = p.contact_id"
					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
					+ " WHERE "
					+ " aco.id in(" + dz_id +")"
					+ " union "
					+ " SELECT aci.id, aci.payee_id payee_id, NULL payee_name, aci.order_no, '开票记录单' order_type, aci. STATUS, aci.remark, "
					+ " aci.create_stamp create_stamp, c.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, aci.total_amount cost_amount, "
					+ " ( SELECT ifnull(sum(caor.receive_amount), 0)"
					+ "  FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '开票记录单' ) receive_amount, "
					+ " ( aci.total_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) FROM "
					+ " charge_application_order_rel caor"
					+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '开票记录单' ) ) noreceive_amount "
					+ " FROM arap_charge_invoice aci "
					+ " LEFT OUTER JOIN party p ON aci.payee_id = p.id "
					+ " LEFT OUTER JOIN contact c ON c.id = p.contact_id "
					+ " LEFT OUTER JOIN user_login ul ON aci.create_by = ul.id"
					+ " LEFT OUTER JOIN office o ON ul.office_id = o.id "
					+ " WHERE aci.id in(" + kpjl_id +")"
				    + " union"
				    + " SELECT aco.id,"
				    + " (case when aco.charge_from_type = 'sp' then aco.sp_id"
					+ " when aco.charge_from_type = 'customer' then aco.customer_id end) payee_id,aco.others_name payee_name,"
				    + " aco.order_no, '手工收入单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
					+ " (case when aco.charge_from_type = 'sp' then (select c.company_name from contact c where c.id = aco.sp_id)"
					+ " when aco.charge_from_type = 'customer' then (select c.company_name from contact c where c.id = aco.customer_id) end) cname,"
					+ "  ifnull(ul.c_name, ul.user_name) creator_name, aco.total_amount charge_amount,"
					+ " ( SELECT ifnull(sum(caor.receive_amount), 0)"
					+ "  FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '手工收入单' ) receive_amount, "
					+ " ( aco.total_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) FROM "
					+ " charge_application_order_rel caor"
					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '手工收入单' ) ) noreceive_amount "
					+ " FROM arap_misc_charge_order aco "
					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
					+ " WHERE "
					+ " aco.id in(" + sr_id +")"
				    + " union"
				    + " SELECT aio.id, null payee_id,aio.charge_person payee_name, aio.order_no, '往来票据单' order_type,"
				    + " aio.pay_status STATUS, aio.remark, aio.create_date create_stamp, aio.charge_unit cname,"
				    + " ifnull(ul.c_name, ul.user_name) creator_name,"
				    + " aio.charge_amount charge_amount,"
				    + " ( SELECT ifnull(sum(caor.receive_amount), 0)"
					+ "  FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aio.id AND caor.order_type = '往来票据单' ) receive_amount, "
					+ " ( aio.charge_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) FROM "
					+ " charge_application_order_rel caor"
					+ " WHERE caor.charge_order_id = aio.id AND caor.order_type = '往来票据单' ) ) noreceive_amount "
				    + " FROM arap_in_out_misc_order aio"
				    + " LEFT JOIN user_login ul ON ul.id = aio.creator_id"
				    + " WHERE aio.id in(" + wl_id +")";
		}else{
			sql = " select *  from (SELECT aco.id,aco.payee_id,payee payee_name,aco.order_no, '应收对账单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
					+ " c.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, aco.charge_amount,"
					+ " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aco.id and caor.application_order_id = aciao.id AND caor.order_type = '应收对账单' ) receive_amount,"
					+ " (aco.charge_amount - (SELECT ifnull(sum(caor.receive_amount), 0) "
					+ " FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '应收对账单'  )) noreceive_amount, aciao.id app_id "
					+ " FROM arap_charge_order aco "
					+ " LEFT JOIN charge_application_order_rel caor on caor.charge_order_id = aco.id"
					+ " LEFT JOIN arap_charge_invoice_application_order aciao on aciao.id = caor.application_order_id"
					+ " LEFT JOIN party p ON p.id = aco.payee_id"
					+ " LEFT JOIN contact c ON c.id = p.contact_id"
					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
					+ " where caor.order_type = '应收对账单'"
					+ " union "
					+ " SELECT aci.id, aci.payee_id payee_id, NULL payee_name, aci.order_no, '开票记录单' order_type, aci. STATUS, aci.remark, "
					+ " aci.create_stamp create_stamp, c.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, aci.total_amount cost_amount, "
					+ " ( SELECT ifnull(sum(caor.receive_amount), 0)"
					+ "  FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aci.id and caor.application_order_id = aciao.id AND caor.order_type = '开票记录单' ) receive_amount, "
					+ " ( aci.total_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) FROM "
					+ " charge_application_order_rel caor"
					+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '开票记录单' ) ) noreceive_amount, aciao.id app_id  "
					+ " FROM arap_charge_invoice aci "
					+ " LEFT JOIN charge_application_order_rel caor on caor.charge_order_id = aci.id"
					+ " LEFT JOIN arap_charge_invoice_application_order aciao on aciao.id = caor.application_order_id"
					+ " LEFT OUTER JOIN party p ON aci.payee_id = p.id "
					+ " LEFT OUTER JOIN contact c ON c.id = p.contact_id "
					+ " LEFT OUTER JOIN user_login ul ON aci.create_by = ul.id"
					+ " LEFT OUTER JOIN office o ON ul.office_id = o.id "
					+ " where caor.order_type = '开票记录单'"
				    + " union"
				    + " SELECT aco.id,"
				    + " (case when aco.charge_from_type = 'sp' then aco.sp_id"
					+ " when aco.charge_from_type = 'customer' then aco.customer_id end) payee_id,aco.others_name payee_name,"
				    + " aco.order_no, '手工收入单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
					+ " (case when aco.charge_from_type = 'sp' then (select c.company_name from contact c where c.id = aco.sp_id)"
					+ " when aco.charge_from_type = 'customer' then (select c.company_name from contact c where c.id = aco.customer_id) end) cname,"
					+ "  ifnull(ul.c_name, ul.user_name) creator_name, aco.total_amount charge_amount,"
					+ " ( SELECT ifnull(sum(caor.receive_amount), 0)"
					+ "  FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aco.id and caor.application_order_id = aciao.id AND caor.order_type = '手工收入单' ) receive_amount, "
					+ " ( aco.total_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) FROM "
					+ " charge_application_order_rel caor"
					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '手工收入单' ) ) noreceive_amount , aciao.id app_id  "
					+ " FROM arap_misc_charge_order aco "
					+ " LEFT JOIN charge_application_order_rel caor on caor.charge_order_id = aco.id"
					+ " LEFT JOIN arap_charge_invoice_application_order aciao on aciao.id = caor.application_order_id"
					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
					+ " where caor.order_type = '手工收入单'"
				    + " union"
				    + " SELECT aio.id, null payee_id,aio.charge_person payee_name, aio.order_no, '往来票据单' order_type,"
				    + " aio.pay_status STATUS, aio.remark, aio.create_date create_stamp, aio.charge_unit cname,"
				    + " ifnull(ul.c_name, ul.user_name) creator_name,"
				    + " aio.charge_amount charge_amount,"
				    + " ( SELECT ifnull(sum(caor.receive_amount), 0)"
					+ "  FROM charge_application_order_rel caor "
					+ " WHERE caor.charge_order_id = aio.id and caor.application_order_id = aciao.id AND caor.order_type = '往来票据单' ) receive_amount, "
					+ " ( aio.charge_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) FROM "
					+ " charge_application_order_rel caor"
					+ " WHERE caor.charge_order_id = aio.id AND caor.order_type = '往来票据单' ) ) noreceive_amount , aciao.id app_id  "
				    + " FROM arap_in_out_misc_order aio"
				    + " LEFT JOIN charge_application_order_rel caor on caor.charge_order_id = aio.id"
					+ " LEFT JOIN arap_charge_invoice_application_order aciao on aciao.id = caor.application_order_id"
				    + " LEFT JOIN user_login ul ON ul.id = aio.creator_id"
				    + " where caor.order_type = '往来票据单'"
				    + " ) A where app_id ="+application_id;
		}
		
		Map BillingOrderListMap = new HashMap();
		List<Record> recordList= Db.find(sql);
        BillingOrderListMap.put("iTotalRecords", recordList.size());
        BillingOrderListMap.put("iTotalDisplayRecords", recordList.size());
        BillingOrderListMap.put("aaData", recordList);

        renderJson(BillingOrderListMap);
	}
	
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_UPDATE})
	public void edit() throws ParseException {
		String id = getPara("id");
		setAttr("application_id", id);
		
		ArapChargeInvoiceApplication arapAuditInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(id);
		setAttr("invoiceApplication", arapAuditInvoiceApplication);
		
		Contact con  = Contact.dao.findById(arapAuditInvoiceApplication.get("payee_id"));
		if(con != null){
			String payee_filter = con.get("company_name");
			setAttr("payee_filter", payee_filter);
		}
		UserLogin userLogin = null;
		userLogin = UserLogin.dao .findById(arapAuditInvoiceApplication.get("create_by"));
		String submit_name = userLogin.get("c_name");
		setAttr("submit_name", submit_name);
		
		Long check_by = arapAuditInvoiceApplication.getLong("check_by");
		if( check_by != null){
			userLogin = UserLogin.dao .findById(check_by);
			String check_name = userLogin.get("c_name");
			setAttr("check_name", check_name);
		}
		
		List<Record> Account = Db.find("select * from fin_account where bank_name != '现金'");
		setAttr("accountList", Account);
		
		render("/yh/arap/ChargeAcceptOrder/chargeEdit.html");
	}
	
	
	@Before(Tx.class)
	public void save() {
		ArapChargeInvoiceApplication arapAuditInvoiceApplication = null;
		String application_id = getPara("application_id");
		String paymentMethod = getPara("payment_method");//收款方式
		String bank_no = getPara("bank_no");          //收款账号
		String payee_name = getPara("payee_name");    //收款人
		String numname = getPara("account_name");   //账户名
		String payee_unit = getPara("payee_unit");      //收款单位
		String payee_id = getPara("payee_id")==""?null:getPara("payee_id");         //付款给
		String billing_unit = getPara("billing_unit"); //收款单位
		String billtype = getPara("invoice_type");   //开票类型
		String bank_name = getPara("deposit_bank");   //开户行
		String total_amount = getPara("total_amount")==""?"0.00":getPara("total_amount");   //申请总金额

		
		if (!"".equals(application_id) && application_id != null) {
			arapAuditInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(application_id);
			arapAuditInvoiceApplication.set("create_stamp", new Date());
			arapAuditInvoiceApplication.set("last_modified_by",LoginUserController.getLoginUserId(this));
			arapAuditInvoiceApplication.set("last_modified_stamp", new Date());
			arapAuditInvoiceApplication.set("payee_name", payee_name);
			arapAuditInvoiceApplication.set("payment_method", paymentMethod);
			arapAuditInvoiceApplication.set("payee_unit", payee_unit);
			arapAuditInvoiceApplication.set("billing_unit", billing_unit);
			arapAuditInvoiceApplication.set("bill_type", billtype);
			arapAuditInvoiceApplication.set("bank_no", bank_no);
			arapAuditInvoiceApplication.set("bank_name", bank_name);
			arapAuditInvoiceApplication.set("num_name", numname);
			arapAuditInvoiceApplication.set("payee_id", payee_id);
			if (total_amount != null && !"".equals(total_amount)) {
				arapAuditInvoiceApplication.set("total_amount",total_amount);
			}
			arapAuditInvoiceApplication.update();
			
			String strJson = getPara("detailJson");
			Gson gson = new Gson();
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String id = (String)map.get("id");
				String order_type = (String)map.get("order_type");
				String value = (String)map.get("value");

				ChargeApplicationOrderRel chargeApplicationOrderRel = ChargeApplicationOrderRel.dao.findFirst("select * from charge_application_order_rel where charge_order_id =? and application_order_id = ?",id,application_id);
				chargeApplicationOrderRel.set("application_order_id", arapAuditInvoiceApplication.getLong("id"));
				chargeApplicationOrderRel.set("charge_order_id", id);
				chargeApplicationOrderRel.set("order_type", order_type);
				chargeApplicationOrderRel.set("receive_amount", value);
				chargeApplicationOrderRel.update();
				
				
//				if(order_type.equals("应收对账单")){
//					ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(id);
//					arapChargeOrder.set("status", "收款申请中").update();
//				}else if(order_type.equals("手工收入单")){
//					ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(id);
//					arapMiscChargeOrder.set("status", "收款申请中").update();
//				}else if(order_type.equals("开票记录单")){
//					ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(id);
//					arapChargeInvoice.set("status", "收款申请中").update();
//				}else if(order_type.equals("往来票据单")){
//					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
//					arapInOutMiscOrder.set("charge_status", "收款申请中").update();
//				}
			}
		} else {
			arapAuditInvoiceApplication = new ArapChargeInvoiceApplication();
			arapAuditInvoiceApplication.set("order_no",
					OrderNoGenerator.getNextOrderNo("YFSQ"));
			arapAuditInvoiceApplication.set("status", "新建");
			arapAuditInvoiceApplication.set("create_by", LoginUserController.getLoginUserId(this));
			arapAuditInvoiceApplication.set("create_stamp", new Date());
			arapAuditInvoiceApplication.set("payee_name", payee_name);
			arapAuditInvoiceApplication.set("payment_method", paymentMethod);
			arapAuditInvoiceApplication.set("payee_unit", payee_unit);
			arapAuditInvoiceApplication.set("billing_unit", billing_unit);
			arapAuditInvoiceApplication.set("bill_type", billtype);
			arapAuditInvoiceApplication.set("bank_no", bank_no);
			arapAuditInvoiceApplication.set("bank_name", bank_name);
			arapAuditInvoiceApplication.set("num_name", numname);
			arapAuditInvoiceApplication.set("payee_id", payee_id);
			
			if (total_amount != null && !"".equals(total_amount)) {
				arapAuditInvoiceApplication.set("total_amount",total_amount);
			}
			arapAuditInvoiceApplication.save();
			
			String strJson = getPara("detailJson");
			Gson gson = new Gson();
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String id = (String)map.get("id");
				String order_type = (String)map.get("order_type");
				String value = (String)map.get("value");

				ChargeApplicationOrderRel chargeApplicationOrderRel = new ChargeApplicationOrderRel();
				chargeApplicationOrderRel.set("application_order_id", arapAuditInvoiceApplication.getLong("id"));
				chargeApplicationOrderRel.set("charge_order_id", id);
				chargeApplicationOrderRel.set("order_type", order_type);
				chargeApplicationOrderRel.set("receive_amount", value);
				chargeApplicationOrderRel.save();
				
                if(order_type.equals("应收对账单")){
					ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(id);
					arapChargeOrder.set("status", "收款申请中").update();
				}else if(order_type.equals("手工收入单")){
					ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(id);
					arapMiscChargeOrder.set("status", "收款申请中").update();
				}else if(order_type.equals("开票记录单")){
					ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(id);
					arapChargeInvoice.set("status", "收款申请中").update();
				}else if(order_type.equals("往来票据单")){
					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
					arapInOutMiscOrder.set("charge_status", "收款申请中").update();
				}
			}
		}
		renderJson(arapAuditInvoiceApplication);
	}
	
	
	
	
	
	//复核
	@Before(Tx.class)
    public void checkStatus(){
        String application_id=getPara("application_id");
        
        ArapChargeInvoiceApplication arapChargeInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(application_id);
        arapChargeInvoiceApplication.set("status", "已复核");
        arapChargeInvoiceApplication.set("check_by", LoginUserController.getLoginUserId(this));
        arapChargeInvoiceApplication.set("check_stamp", new Date()).update();
        renderJson(arapChargeInvoiceApplication);
        
        //更改原始单据状态
        String strJson = getPara("detailJson");
		Gson gson = new Gson();
		List<Map> idList = new Gson().fromJson(strJson, 
				new TypeToken<List<Map>>(){}.getType());
		for (Map map : idList) {
			String id = (String)map.get("id");
			String order_type = (String)map.get("order_type");

			
			if(order_type.equals("应收对账单")){
				ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(id);
				Double total_amount = arapChargeOrder.getDouble("charge_amount");
				Record re = Db.findFirst("select sum(receive_amount) total from charge_application_order_rel where charge_order_id =? and order_type = '应收对账单'",id);
				Double receive_amount = re.getDouble("total");
				if(!total_amount.equals(receive_amount)){
					arapChargeOrder.set("status", "部分已复核").update();
				}else
					arapChargeOrder.set("status", "已复核").update();
			}else if(order_type.equals("手工收入单")){
				ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(id);
				
				Double total_amount = arapMiscChargeOrder.getDouble("total_amount");
				Record re = Db.findFirst("select sum(receive_amount) total from charge_application_order_rel where charge_order_id =? and order_type = '手工收入单'",id);
				Double receive_amount = re.getDouble("total");
				if(!total_amount.equals(receive_amount)){
					arapMiscChargeOrder.set("status", "部分已复核").update();
				}else
					arapMiscChargeOrder.set("status", "已复核").update();
									
			}else if(order_type.equals("开票记录单")){
				ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(id);
				
				Double total_amount = arapChargeInvoice.getDouble("total_amount");
				Record re = Db.findFirst("select sum(receive_amount) total from charge_application_order_rel where charge_order_id =? and order_type = '开票记录单'",id);
				Double receive_amount = re.getDouble("total");
				if(!total_amount.equals(receive_amount)){
					arapChargeInvoice.set("status", "部分已复核").update();
				}else
					arapChargeInvoice.set("status", "已复核").update();
				
			}else if(order_type.equals("往来票据单")){
				ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
				
				Double total_amount = arapInOutMiscOrder.getDouble("charge_amount");
				Record re = Db.findFirst("select sum(receive_amount) total from charge_application_order_rel where charge_order_id =? and order_type = '往来票据单'",id);
				Double receive_amount = re.getDouble("total");
				if(!total_amount.equals(receive_amount)){
					arapInOutMiscOrder.set("charge_status", "部分已复核").update();
				}else
					arapInOutMiscOrder.set("charge_status", "已复核").update();
			}
		}
        
    }
	
	//退回
	@Before(Tx.class)
    public void returnOrder(){
        String application_id=getPara("application_id");
        
        ArapChargeInvoiceApplication arapChargeInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(application_id);
        arapChargeInvoiceApplication.set("status", "新建");
        arapChargeInvoiceApplication.set("return_by", LoginUserController.getLoginUserId(this));
        arapChargeInvoiceApplication.set("return_stamp", new Date()).update();
        renderJson("{\"success\":true}");
        
        //更改原始单据状态
        String strJson = getPara("detailJson");
		Gson gson = new Gson();
		List<Map> idList = new Gson().fromJson(strJson, 
				new TypeToken<List<Map>>(){}.getType());
		for (Map map : idList) {
			String id = (String)map.get("id");
			String order_type = (String)map.get("order_type");

			
			if(order_type.equals("应收对账单")){
				ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(id);
				arapChargeOrder.set("status", "收款申请中").update();
			}else if(order_type.equals("手工收入单")){
				ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(id);
				arapMiscChargeOrder.set("status", "收款申请中").update();
			}else if(order_type.equals("开票记录单")){
				ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(id);
				arapChargeInvoice.set("status", "收款申请中").update();
			}else if(order_type.equals("往来票据单")){
				ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
				arapInOutMiscOrder.set("charge_status", "收款申请中").update();
			}
		}
        
    }

    
	//付款确认
	@Before(Tx.class)
    public void confirmOrder(){
        String application_id=getPara("application_id");
        String receive_type = getPara("receive_type");
        String receive_bank_id = getPara("receive_bank");
        String receive_time = getPara("receive_time");
        
        if( receive_time==null||receive_time.equals("")){
        	receive_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
   		}
        
        ArapChargeInvoiceApplication arapChargeInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(application_id);
        String pay_amount = arapChargeInvoiceApplication.getDouble("total_amount").toString();
        arapChargeInvoiceApplication.set("status", "已收款");
        arapChargeInvoiceApplication.set("receive_type", receive_type);
        if(receive_bank_id != null && !receive_bank_id.equals(""))
        	arapChargeInvoiceApplication.set("receive_bank_id", receive_bank_id);
        arapChargeInvoiceApplication.set("receive_time", receive_time);
        arapChargeInvoiceApplication.set("confirm_by", LoginUserController.getLoginUserId(this));
        arapChargeInvoiceApplication.set("confirm_stamp", new Date());
        arapChargeInvoiceApplication.update();
        
        //更改原始单据状态
        String strJson = getPara("detailJson");
		Gson gson = new Gson();
		List<Map> idList = new Gson().fromJson(strJson, 
				new TypeToken<List<Map>>(){}.getType());
		for (Map map : idList) {
			String id = (String)map.get("id");
			String order_type = (String)map.get("order_type");

			if(order_type.equals("应收对账单")){
				ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(id);
				Double total_amount = arapChargeOrder.getDouble("charge_amount");
				Record re = Db.findFirst("select sum(receive_amount) total from charge_application_order_rel where charge_order_id =? and order_type = '应收对账单'",id);
				Double receive_amount = re.getDouble("total");
				if(!total_amount.equals(receive_amount)){
					arapChargeOrder.set("status", "部分已收款").update();
				}else
					arapChargeOrder.set("status", "已收款").update();
			}else if(order_type.equals("手工收入单")){
				ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(id);
				
				Double total_amount = arapMiscChargeOrder.getDouble("total_amount");
				Record re = Db.findFirst("select sum(receive_amount) total from charge_application_order_rel where charge_order_id =? and order_type = '手工收入单'",id);
				Double receive_amount = re.getDouble("total");
				if(!total_amount.equals(receive_amount)){
					arapMiscChargeOrder.set("status", "部分已收款").update();
				}else
					arapMiscChargeOrder.set("status", "已收款").update();
									
			}else if(order_type.equals("开票记录单")){
				ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(id);
				
				Double total_amount = arapChargeInvoice.getDouble("total_amount");
				Record re = Db.findFirst("select sum(receive_amount) total from charge_application_order_rel where charge_order_id =? and order_type = '开票记录单'",id);
				Double receive_amount = re.getDouble("total");
				if(!total_amount.equals(receive_amount)){
					arapChargeInvoice.set("status", "部分已收款").update();
				}else
					arapChargeInvoice.set("status", "已收款").update();
				
			}else if(order_type.equals("往来票据单")){
				ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
				
				Double total_amount = arapInOutMiscOrder.getDouble("charge_amount");
				Record re = Db.findFirst("select sum(receive_amount) total from charge_application_order_rel where charge_order_id =? and order_type = '往来票据单'",id);
				Double receive_amount = re.getDouble("total");
				if(!total_amount.equals(receive_amount)){
					arapInOutMiscOrder.set("charge_status", "部分已收款").update();
				}else
					arapInOutMiscOrder.set("charge_status", "已收款").update();
			}
		}
        
        
        
      //新建日记账表数据
		 ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
        auditLog.set("payment_method", receive_type);
        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_CHARGE);
        auditLog.set("amount", pay_amount);
        auditLog.set("creator", LoginUserController.getLoginUserId(this));
        auditLog.set("create_date", receive_time);
        if(receive_bank_id!=null  &&  !receive_bank_id.equals(""))
        	auditLog.set("account_id", receive_bank_id);
        else
        	auditLog.set("account_id", 4);
        
        auditLog.set("source_order", "应收开票申请单");
        auditLog.set("invoice_order_id", application_id);
        auditLog.save();
        
        if("transfers".equals(receive_type)){
        	updateAccountSummary(pay_amount, Long.parseLong(receive_bank_id) ,receive_time);
        }else{
        	Account cashAccount = Account.dao.findFirst("select * from fin_account where bank_name ='现金'");
        	updateAccountSummary(pay_amount,cashAccount.getLong("id"),receive_time);
        }	        
        renderJson("{\"success\":true}");  
    }
	
	
	
	//更新日记账的账户期初结余
	//本期结余 = 期初结余 + 本期总收入 - 本a期总支出
	private void updateAccountSummary(String receive_amount, Long acountId,String receive_time) {
		Calendar cal = Calendar.getInstance();  
		int this_year = cal.get(Calendar.YEAR);  
		int this_month = cal.get(Calendar.MONTH)+1;  
		String year = receive_time.substring(0, 4);
		String month = receive_time.substring(5, 7);
		
		if(String.valueOf(this_year).equals(year) && String.valueOf(this_month).equals(month)){
			ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
					"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
					, acountId, year, month);
			if(aaas!=null){
				aaas.set("total_charge", (aaas.getDouble("total_charge")==null?0.0:aaas.getDouble("total_charge")) + Double.parseDouble(receive_amount));
				aaas.set("balance_amount", (aaas.getDouble("init_amount")-aaas.getDouble("total_cost")+ aaas.getDouble("total_charge")));
				aaas.update();
			}else{//add a new
				//1.该账户没有记录
				//2.该月份没有，从上月拷贝一条，考虑：上月也没有（跨1-N月）， 跨年
				ArapAccountAuditSummary newSummary = new ArapAccountAuditSummary();
			}
		}else{
			ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
					"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
					, acountId, year, month);

			if(aaas!=null){
				aaas.set("total_charge", (aaas.getDouble("total_charge")==null?0.0:aaas.getDouble("total_charge")) + Double.parseDouble(receive_amount));
				aaas.set("balance_amount", (aaas.getDouble("init_amount")+aaas.getDouble("total_charge")- aaas.getDouble("total_cost")));
				aaas.update();
			
				for(int i = 1 ;i<=(this_month - Integer.parseInt(month)); i++){
					ArapAccountAuditSummary this_aaas = ArapAccountAuditSummary.dao.findFirst(
							"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
							, acountId, this_year, Integer.parseInt(month)+i);
					
					this_aaas.set("init_amount", this_aaas.getDouble("init_amount")==null?0.0:(this_aaas.getDouble("init_amount") + Double.parseDouble(receive_amount)));
					this_aaas.set("balance_amount", this_aaas.getDouble("balance_amount")==null?0.0:(this_aaas.getDouble("balance_amount") + Double.parseDouble(receive_amount)));
					this_aaas.update();	
				}
			}
		}
	}
}
