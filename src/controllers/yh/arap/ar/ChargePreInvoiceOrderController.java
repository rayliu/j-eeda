package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapChargeInvoiceApplication;
import models.ArapChargeOrder;
import models.ChargeApplicationOrderRel;
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
import com.jfinal.plugin.activerecord.tx.Tx;

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
				+ " c1.abbr sp "
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
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	public void create() {
		String ids = getPara("ids");

		setAttr("chargeCheckOrderIds", ids);
		if(ids != null && !"".equals(ids)){
			String[] idArray = ids.split(",");
			logger.debug(String.valueOf(idArray.length));
			Double totalAmount = 0.0;
			Double totalReceive = 0.0;
			for(int i=0;i<idArray.length;i++){
				ArapChargeOrder rOrder = ArapChargeOrder.dao.findById(idArray[i]);
				Double chargeTotalAmount = rOrder.getDouble("charge_amount")==null?0.0:rOrder.getDouble("charge_amount");
				totalAmount = totalAmount + chargeTotalAmount;
				ChargeApplicationOrderRel c = ChargeApplicationOrderRel.dao.findFirst("select sum(receive_amount) totalReceive from charge_application_order_rel where charge_order_id = ?",idArray[i]);
				Double chargeTotalReceive = c.getDouble("totalReceive")==null?0.0:c.getDouble("totalReceive");
				totalReceive = totalReceive + chargeTotalReceive;
			}
			setAttr("total_amount", totalAmount);
			setAttr("total_receive", totalReceive);
			setAttr("total_noreceive",totalAmount-totalReceive);
			
			ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(idArray[0]);
			Long customerId = arapChargeOrder.get("payee_id");
			
			Long spId = arapChargeOrder.get("sp_id");
			setAttr("spId", spId);
			if (!"".equals(customerId) && customerId != null) {
				Party party = Party.dao.findById(customerId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get("contact_id"));
				setAttr("customer", contact);
				setAttr("type", "CUSTOMER");
				setAttr("classify", "");
			}
		}

		setAttr("saveOK", false);
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));
		
		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		setAttr("status", "new");
			render("/yh/arap/ChargePreInvoiceOrder/ChargePreInvoiceOrderEdit.html");
	}
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE,PermissionConstant.PERMSSION_CPIO_UPDATE},logical=Logical.OR)
    @Before(Tx.class)
	public void save() {
		ArapChargeInvoiceApplication arapAuditInvoiceApplication = null;
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		String paymentMethod = getPara("paymentMethod");
		String spId = getPara("spId");
		if (!"".equals(chargePreInvoiceOrderId) && chargePreInvoiceOrderId != null) {
			arapAuditInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId);
            //arapAuditInvoiceApplication.set("status", "new");
			arapAuditInvoiceApplication.set("create_by", getPara("create_by"));
			arapAuditInvoiceApplication.set("create_stamp", new Date());
			arapAuditInvoiceApplication.set("remark", getPara("remark"));
			arapAuditInvoiceApplication.set("last_modified_by", getPara("create_by"));
			arapAuditInvoiceApplication.set("last_modified_stamp", new Date());
			arapAuditInvoiceApplication.set("payment_method", paymentMethod);
			if("transfers".equals(paymentMethod)){
				if(getPara("accountTypeSelect") != null && !"".equals(getPara("accountTypeSelect"))){
					arapAuditInvoiceApplication.set("account_id", getPara("accountTypeSelect"));
				}
			}else{
				arapAuditInvoiceApplication.set("account_id", null);				
			}
			arapAuditInvoiceApplication.update();
		} else {
			arapAuditInvoiceApplication = new ArapChargeInvoiceApplication();
			arapAuditInvoiceApplication.set("order_no", OrderNoGenerator.getNextOrderNo("YSSQ"));
			arapAuditInvoiceApplication.set("status", "新建");
			if(!getPara("customer_id").equals("") && getPara("customer_id")!= null){
				arapAuditInvoiceApplication.set("payee_id", getPara("customer_id"));
			}
			if(!spId.equals("") && spId!= null){
				arapAuditInvoiceApplication.set("sp_id", spId);
			}
			arapAuditInvoiceApplication.set("create_by", getPara("create_by"));
			arapAuditInvoiceApplication.set("create_stamp", new Date());
			arapAuditInvoiceApplication.set("remark", getPara("remark"));
			arapAuditInvoiceApplication.set("payment_method", getPara("paymentMethod"));
			if("transfers".equals(paymentMethod)){
				if(getPara("accountTypeSelect") != null && !"".equals(getPara("accountTypeSelect"))){
					arapAuditInvoiceApplication.set("account_id", getPara("accountTypeSelect"));
				}
			}
			arapAuditInvoiceApplication.save();

//			String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
//			String[] chargeCheckOrderIdsArr = chargeCheckOrderIds.split(",");
//			for (int i = 0; i < chargeCheckOrderIdsArr.length; i++) {
//				ArapChargeInvoiceApplicationItem arapAuditInvoiceApplicationItem = new ArapChargeInvoiceApplicationItem();
//				arapAuditInvoiceApplicationItem.set("invoice_application_id", arapAuditInvoiceApplication.get("id"));
//				arapAuditInvoiceApplicationItem.set("charge_order_id", chargeCheckOrderIdsArr[i]);
//				arapAuditInvoiceApplicationItem.save();
//				
//				ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(chargeCheckOrderIdsArr[i]);
//				arapAuditOrder.set("status", "开票申请中");
//				arapAuditOrder.update();
//			}
			String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
			String[] chargeCheckOrderIdsArr = chargeCheckOrderIds.split(",");
			for (int i = 0; i < chargeCheckOrderIdsArr.length; i++) {
				//更新中间表
				ChargeApplicationOrderRel chargeApplicationOrderRel = new ChargeApplicationOrderRel();
				chargeApplicationOrderRel.set("application_order_id", arapAuditInvoiceApplication.getLong("id"));
				chargeApplicationOrderRel.set("charge_order_id", chargeCheckOrderIdsArr[i]);
				chargeApplicationOrderRel.save();
				//更新对账单表
				ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(chargeCheckOrderIdsArr[i]);
				arapAuditOrder.set("status", "收款申请中");
				arapAuditOrder.update();
			}
		}

		renderJson(arapAuditInvoiceApplication);;
	}
	
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
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_UPDATE})
	public void edit() throws ParseException {
		String id = getPara("id");
		ArapChargeInvoiceApplication arapAuditInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(id);
		Long customerId = arapAuditInvoiceApplication.getLong("payee_id");
		if (!"".equals(customerId) && customerId != null) {
			Party party = Party.dao.findById(customerId);
			setAttr("party", party);
			Contact contact = Contact.dao.findById(party.get("contact_id")
					.toString());
			setAttr("customer", contact);
		}
		String chargeCheckOrderIds = "";
		List<ChargeApplicationOrderRel> list = ChargeApplicationOrderRel.dao.find("select * from charge_application_order_rel where application_order_id = ?", id);
		for(ChargeApplicationOrderRel chargeApplicationOrderRel : list){
			chargeCheckOrderIds += chargeApplicationOrderRel.get("charge_order_id") + ",";
		}
		chargeCheckOrderIds = chargeCheckOrderIds.substring(0, chargeCheckOrderIds.length() - 1);
		setAttr("chargeCheckOrderIds", chargeCheckOrderIds);
		
		//计算应收总金额
		String[] idArray = chargeCheckOrderIds.split(",");
		Double totalAmount = 0.0;
		Double totalReceive = 0.0;
		for(int i=0;i<idArray.length;i++){
			ArapChargeOrder rOrder = ArapChargeOrder.dao.findById(idArray[i]);
			Double chargeTotalAmount = rOrder.getDouble("charge_amount")==null?0.0:rOrder.getDouble("charge_amount");
			totalAmount = totalAmount + chargeTotalAmount;
			ChargeApplicationOrderRel c = ChargeApplicationOrderRel.dao.findFirst("select sum(receive_amount) totalReceive from charge_application_order_rel where charge_order_id = ?",idArray[i]);
			Double chargeTotalReceive = c.getDouble("totalReceive")==null?0.0:c.getDouble("totalReceive");
			totalReceive = totalReceive + chargeTotalReceive;
		}
		ChargeApplicationOrderRel chargeApplicationOrderRel1 = ChargeApplicationOrderRel.dao.findFirst("SELECT sum(caor.receive_amount) this_receive FROM charge_application_order_rel caor  WHERE caor.application_order_id=?",id);
	    Double receive_amount = chargeApplicationOrderRel1.getDouble("this_receive");
		Double total_noreceive =  totalAmount - totalReceive;
		setAttr("total_amount", totalAmount);
	    setAttr("total_receive", totalReceive);
		setAttr("total_noreceive", total_noreceive);
		setAttr("receive_amount", receive_amount);
	    
		UserLogin userLogin = UserLogin.dao.findById(arapAuditInvoiceApplication.get("create_by"));
		setAttr("userLogin", userLogin);
		if(!"".equals(arapAuditInvoiceApplication.get("audit_by")) && arapAuditInvoiceApplication.get("audit_by") != null){
			UserLogin auditName = UserLogin.dao.findById(arapAuditInvoiceApplication.get("audit_by"));
			setAttr("auditName", auditName);
			setAttr("auditData", arapAuditInvoiceApplication.get("audit_stamp"));
		}
		if(!"".equals(arapAuditInvoiceApplication.get("approver_by")) && arapAuditInvoiceApplication.get("approver_by") != null){
			UserLogin approvalName = UserLogin.dao.findById(arapAuditInvoiceApplication.get("approver_by"));
			setAttr("approvalName", approvalName);
			setAttr("approvalData", arapAuditInvoiceApplication.get("approval_stamp"));
		}
		
		setAttr("arapAuditInvoiceApplication", arapAuditInvoiceApplication);
		render("/yh/arap/ChargePreInvoiceOrder/ChargePreInvoiceOrderEdit.html");
	}
    
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
					+ " WHERE caor.charge_order_id = aao.id ) IS NOT NULL ))";
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
		String sql = "select * from charge_application_order_rel where application_order_id = '"+chargePreInvoiceOrderId+"' and charge_order_id = '"+chargeOrderId+"'";
		ChargeApplicationOrderRel chargeApplicationOrderRel = ChargeApplicationOrderRel.dao.findFirst(sql);
		String name = getPara("name");
		String value = getPara("value");
		if ("receive_amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		chargeApplicationOrderRel.set(name, value);
		chargeApplicationOrderRel.update();
		
		
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
		renderJson(map);
	}
	
}
