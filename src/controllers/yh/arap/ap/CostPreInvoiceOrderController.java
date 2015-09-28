package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapAccountAuditLog;
import models.ArapCostInvoiceApplication;
import models.ArapCostInvoiceItemInvoiceNo;
import models.ArapCostOrder;
import models.ArapCostOrderInvoiceNo;
import models.CostApplicationOrderRel;
import models.Party;
import models.UserLogin;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.prePayOrder.ArapPrePayOrder;
import models.yh.profile.Contact;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
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
public class CostPreInvoiceOrderController extends Controller {
	private Logger logger = Logger
			.getLogger(CostPreInvoiceOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_LIST})
	public void index() {
		render("/yh/arap/CostPreInvoiceOrder/CostPreInvoiceOrderList.html");
	}

	public void confirm() {
		String ids = getPara("ids");
		String[] idArray = ids.split(",");
		logger.debug(String.valueOf(idArray.length));

		String customerId = getPara("customerId");
		Party party = Party.dao.findById(customerId);

		Contact contact = Contact.dao.findById(party.get("contact_id")
				.toString());
		setAttr("customer", contact);
		setAttr("type", "CUSTOMER");
		setAttr("classify", "receivable");
		render("/yh/arap/ChargeAcceptOrder/ChargeCheckOrderEdit.html");
	}
	public void allaccount() {
		List<Record> locationList = Collections.EMPTY_LIST;
		locationList = Db
				.find("select * from fin_account f where bank_name<>'现金'");
		renderJson(locationList);
	}
	//供应商下拉列表查询
	public void sp_filter_list(){
		String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		locationList = Db
				.find("select * from contact c where company_name<>''and (c.company_name like '%"
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
							+ "%')");
		renderJson(locationList);
	}
	// 应付条目列表
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_LIST})
	public void list() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sp = getPara("sp");
		String customer = getPara("customer");
		String orderNo = getPara("orderNo");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String status = getPara("status");
		String selectOrderNO = getPara("selectOrderNO");

		String sqlTotal = "";
		String sql = "select DISTINCT "
				+ " concat("
				+ "  ifnull((SELECT "
				+ " 	GROUP_CONCAT(concat(aco.order_no, '<br/>') SEPARATOR '')"
				+ " FROM"
				+ " 	cost_application_order_rel caor"
				+ "    left join arap_cost_order aco on caor.cost_order_id = aco.id "
				+ " 	where caor.application_order_id = aaia.id and caor.order_type='对账单'), '')"
				+ " ,"
				+ "  ifnull((SELECT "
				+ " 		GROUP_CONCAT( appo.order_no SEPARATOR '<br/>')"
				+ " 	FROM"
				+ " 		cost_application_order_rel caor "
		        + "         left join arap_pre_pay_order appo on caor.cost_order_id = appo.id "
				+ " 		where caor.application_order_id = aaia.id and caor.order_type='预付单'),'')"
				+ " ) cost_order_no,"
				+ " (SELECT sum(caor.pay_amount) from cost_application_order_rel caor WHERE caor.application_order_id = aaia.id ) pay_amount,"
				+ " aaia.*, MONTH (aaia.create_stamp) AS c_stamp,c.abbr cname,c.company_name as company_name,o.office_name oname,ifnull(ul.c_name,ul.user_name) create_b,ul2.user_name audit_by,ul3.user_name approval_by from arap_cost_invoice_application_order aaia "
				+ " left join user_login ul on ul.id = aaia.create_by"
				+ " left join user_login ul2 on ul2.id = aaia.audit_by"
				+ " left join user_login ul3 on ul3.id = aaia.approver_by"
				+ " left join party p on p.id = aaia.payee_id "
				+ " left join office o ON o.id = p.office_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " left join cost_application_order_rel caor on caor.application_order_id = aaia.id"
				+ " LEFT JOIN arap_cost_order aco on aco.id = caor.cost_order_id ";
		String condition = "";
		// TODO 客户和供应商没做
		if (sp != null || customer != null || orderNo != null
				|| beginTime != null || endTime != null) {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			condition = " where ifnull(aco.order_no ,'') like '%" + orderNo
					+ "%' " + " and aaia.create_stamp between '" + beginTime
					+ "' and '" + endTime + "' "
					+ "and ifnull(c.company_name, '') like '%"+customer+"%'"
					+ "and ifnull(c.abbr, '') like '%"+sp+"%'"
					+ "and ifnull(aaia.status, '') like '%"+status+"%'"
			        + "and ifnull(aaia.order_no, '') LIKE '%"+selectOrderNO+"%'" ;

		}

		sqlTotal = "select count(1) total from arap_cost_invoice_application_order aaia "
				+ " left join user_login ul on ul.id = aaia.create_by"
				+ " left join user_login ul2 on ul2.id = aaia.audit_by"
				+ " left join user_login ul3 on ul3.id = aaia.approver_by"
				+ " left join party p on p.id = aaia.payee_id "
				+ " left join office o ON o.id = p.office_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " left join cost_application_order_rel caor on caor.application_order_id = aaia.id"
				+ " LEFT JOIN arap_cost_order aco on aco.id = caor.cost_order_id ";

		//sql = sql + condition + " order by aaia.create_stamp desc " + sLimit;
		
		

		Record rec = Db.findFirst(sqlTotal  + condition);
		logger.debug("total records:" + rec.getLong("total"));
		
		List<Record>   BillingOrders = Db.find(sql + condition + " order by aaia.create_stamp desc " + sLimit);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CREATE})
	public void create() {
		String strJson = getPara("ids");
		setAttr("costCheckOrderIds", strJson.replace("\"", "'"));
		Double totalAmount = 0.0;
		
		Gson gson = new Gson();
		
		List<Map> idList = new Gson().fromJson(strJson, 
				new TypeToken<List<Map>>(){}.getType());
		for (Map map : idList) {
			String orderType = (String)map.get("order_type");
			List<Integer> ids = (List<Integer>)map.get("ids");
			if("对账单".equals(orderType)){
				totalAmount += getDzTotal(ids);
			}else{
				totalAmount += getYfTotal(ids);
			}
		}
		
		setAttr("saveOK", false);
		setAttr("totalAmount", totalAmount);
		
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));

		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		setAttr("status", "新建");
		render("/yh/arap/CostPreInvoiceOrder/CostPreInvoiceOrderEdit.html");
	}

	private Double getYfTotal(List<Integer> idList) {
		Double totalAmount = 0.0;
		String ids = StringUtils.join(idList, ",");
		if (ids != null && !"".equals(ids)) {
			String[] idArray = ids.split(",");
			ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao
					.findById(idArray[0]);
			Long spId = arapPrePayOrder.getLong("sp_id");
			if (!"".equals(spId) && spId != null) {
				Party party = Party.dao.findById(spId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get("contact_id")
						.toString());
				setAttr("customer", contact);
			}
			
			Record rec = Db.findFirst("SELECT sum(total_amount) total_amount from arap_pre_pay_order appo where appo.id in("+ids+")");
			totalAmount=rec.getDouble("total_amount");
			//setAttr("paidAmount", totalAmount);
			
			Double paidAmount=(Double) (getAttr("paidAmount")==null?0.0:getAttr("paidAmount"));
			for (int i = 0; i < idArray.length; i++) {
				Record r = Db.findFirst("SELECT ifnull(sum(caor.pay_amount), 0) total_pay FROM cost_application_order_rel caor"
						+ " WHERE caor.order_type='预付单' and caor.cost_order_id = ?", idArray[i]);
				paidAmount=r.getDouble("total_pay")+paidAmount;
			}
			setAttr("paidAmount", paidAmount);
		}
		return totalAmount;
	}
	
	private Double getDzTotal(List<Integer> idList) {
		Double totalAmount = 0.0;
		String ids = StringUtils.join(idList, ",");
		if (ids != null && !"".equals(ids)) {
			String[] idArray = ids.split(",");
			ArapCostOrder arapCostOrder = ArapCostOrder.dao
					.findById(idArray[0]);
			Long spId = arapCostOrder.getLong("payee_id");
			if (!"".equals(spId) && spId != null) {
				Party party = Party.dao.findById(spId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get("contact_id")
						.toString());
				setAttr("customer", contact);
			}
			Double paidAmount=0.0;
			for (int i = 0; i < idArray.length; i++) {
				Record rec = Db.findFirst("SELECT ifnull(sum(caor.pay_amount), 0) total_pay FROM cost_application_order_rel caor"
						+ " WHERE caor.order_type='对账单' and caor.cost_order_id = ?", idArray[i]);
				paidAmount=rec.getDouble("total_pay")+paidAmount;
				
				arapCostOrder = ArapCostOrder.dao.findById(idArray[i]);
				Double costCheckAmount = arapCostOrder.getDouble("cost_amount") == null
						? 0.0
						: arapCostOrder.getDouble("cost_amount");
				totalAmount = totalAmount + costCheckAmount;
				
			}
			setAttr("paidAmount", paidAmount);
		}
		return totalAmount;
	}
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CREATE,
			PermissionConstant.PERMSSION_CPO_UPDATE}, logical = Logical.OR)
	@Before(Tx.class)
	public void save() {
		ArapCostInvoiceApplication arapAuditInvoiceApplication = null;
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		String paymentMethod = getPara("paymentMethod");
		String account_id = getPara("account");
		String bank_no = getPara("bank_no");
		String bank_name = getPara("bank_name");
		String payee_unit = getPara("payee_unit");
		String sp_Id = getPara("sp_id");
		String billing = getPara("billing");
		String billtype = getPara("billtype");
		String numname = getPara("num_name");
		if (!"".equals(costPreInvoiceOrderId) && costPreInvoiceOrderId != null) {
			arapAuditInvoiceApplication = ArapCostInvoiceApplication.dao
					.findById(costPreInvoiceOrderId);
			
			arapAuditInvoiceApplication.set("create_by", getPara("create_by"));
			arapAuditInvoiceApplication.set("create_stamp", new Date());
			arapAuditInvoiceApplication.set("remark", getPara("remark"));
			arapAuditInvoiceApplication.set("last_modified_by",
					getPara("create_by"));
			arapAuditInvoiceApplication.set("last_modified_stamp", new Date());
			arapAuditInvoiceApplication.set("payee_name", getPara("payeename"));
			arapAuditInvoiceApplication.set("payment_method", paymentMethod);
			arapAuditInvoiceApplication.set("payee_unit", payee_unit);
			arapAuditInvoiceApplication.set("billing_unit", billing);
			arapAuditInvoiceApplication.set("bill_type", billtype);
			arapAuditInvoiceApplication.set("bank_no", bank_no);
			arapAuditInvoiceApplication.set("bank_name", bank_name);
			arapAuditInvoiceApplication.set("num_name", numname);
			String noInvoice = getPara("noInvoice");
			if ("on".equals(noInvoice)) {
				noInvoice = "true";
			} else {
				noInvoice = "false";
			}
			arapAuditInvoiceApplication.set("noInvoice", noInvoice);
			if ("transfers".equals(paymentMethod)) {
				arapAuditInvoiceApplication.set("account_id", account_id);
			} else {
				arapAuditInvoiceApplication.set("account_id", null);
			}
			arapAuditInvoiceApplication.update();
		} else {
			String payee_id = getPara("customer_id");
			if (sp_Id != null && !"".equals(sp_Id)) {
				payee_id = sp_Id;
			}
			else{}
			arapAuditInvoiceApplication = new ArapCostInvoiceApplication();

			String sql = "select * from arap_cost_invoice_application_order order by id desc limit 0,1";
			if ("transfers".equals(paymentMethod)) {

				arapAuditInvoiceApplication.set("account_id", account_id);
			} else {
				arapAuditInvoiceApplication.set("account_id", null);
			}
			arapAuditInvoiceApplication.set("order_no",
					OrderNoGenerator.getNextOrderNo("YFSQ"));
			arapAuditInvoiceApplication.set("status", "新建");
			arapAuditInvoiceApplication.set("payee_id", payee_id);
			arapAuditInvoiceApplication.set("create_by", getPara("create_by"));
			arapAuditInvoiceApplication.set("create_stamp", new Date());
			arapAuditInvoiceApplication.set("remark", getPara("remark"));
			arapAuditInvoiceApplication.set("payee_name", getPara("payeename"));
			arapAuditInvoiceApplication.set("payee_unit", payee_unit);
			arapAuditInvoiceApplication.set("billing_unit", billing);
			arapAuditInvoiceApplication.set("bill_type", billtype);
			arapAuditInvoiceApplication.set("payment_method",
					getPara("paymentMethod"));
			arapAuditInvoiceApplication.set("bank_no", bank_no);
			arapAuditInvoiceApplication.set("bank_name", bank_name);
			if(numname != null){
				arapAuditInvoiceApplication.set("num_name", numname);
			}
			if (getPara("total_amount") != null
					&& !"".equals(getPara("total_amount"))) {
				arapAuditInvoiceApplication.set("total_amount",
						getPara("total_amount"));
			}
			arapAuditInvoiceApplication.save();
			
			
			
			String strJson = getPara("costCheckOrderIds");
			Gson gson = new Gson();
			
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String orderType = (String)map.get("order_type");
				List<String> ids = (List<String>)map.get("ids");
				
				for (String id : ids) {
					//更新中间表
					CostApplicationOrderRel costApplicationOrderRel = new CostApplicationOrderRel();
					costApplicationOrderRel.set("application_order_id", arapAuditInvoiceApplication.getLong("id"));
					costApplicationOrderRel.set("cost_order_id", id);
					costApplicationOrderRel.set("order_type", orderType);
					costApplicationOrderRel.save();
					if("对账单".equals(orderType)){
						//更新对账单表
						updateDzOrder(arapAuditInvoiceApplication, id);
					}else{
						//更新预付单表
						updateYfOrder(arapAuditInvoiceApplication, id);
					}
				}
				
			}

		}
		renderJson(arapAuditInvoiceApplication);
	}
	
	private void updateYfOrder(
			ArapCostInvoiceApplication arapAuditInvoiceApplication, String id) {
		ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao.findById(id);
		//判断是否已全部付款
		String sql5 = "select ifnull(sum(caor.pay_amount), 0) total_pay from cost_application_order_rel caor"
				+ " where caor.order_type='预付单' and caor.cost_order_id = '"+id+"'";
				//+ "LEFT JOIN arap_pre_pay_order appo on appo.id = caor.application_order_id where aco.id = '"+id+"'";
		Record r = Db.findFirst(sql5);
		if( arapPrePayOrder.getDouble("total_amount") > r.getDouble("total_pay")){
			arapPrePayOrder.set("status", "部分付款申请中");
		}else{
			arapPrePayOrder.set("status", "付款申请中");
		}
		arapPrePayOrder.update();
	}

	private void updateDzOrder(
			ArapCostInvoiceApplication arapAuditInvoiceApplication, String id) {
		ArapCostOrder arapAuditOrder = ArapCostOrder.dao.findById(id);
		arapAuditOrder.set("application_order_id", arapAuditInvoiceApplication.get("id"));
		//判断是否已全部付款
		String sql5 = "select ifnull(sum(caor.pay_amount),0) total_pay from arap_cost_order aco  "
				+ "LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = aco.id and caor.order_type='对账单'"
				+ "LEFT JOIN arap_cost_invoice_application_order aciao on aciao.id = caor.application_order_id where aco.id = '"+id+"'";
		Record r = Db.findFirst(sql5);
		if( arapAuditOrder.getDouble("cost_amount") > r.getDouble("total_pay")){
			arapAuditOrder.set("status", "部分付款申请中");
		}else{
			arapAuditOrder.set("status", "付款申请中");
		}
		arapAuditOrder.update();
	}

	// 审核
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_APPROVAL})
	public void auditCostPreInvoiceOrder() {
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		ArapCostInvoiceApplication arapAuditOrder = null;
		if (costPreInvoiceOrderId != null && !"".equals(costPreInvoiceOrderId)) {
			arapAuditOrder = ArapCostInvoiceApplication.dao.findById(costPreInvoiceOrderId);
			arapAuditOrder.set("status", "已审核");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");
			arapAuditOrder.set("audit_by", users.get(0).get("id"));
			arapAuditOrder.set("audit_stamp", new Date());
			arapAuditOrder.update();
		}
		Map BillingOrderListMap = new HashMap();
		UserLogin ul = UserLogin.dao.findById(arapAuditOrder.get("audit_by"));
		BillingOrderListMap.put("arapAuditOrder", arapAuditOrder);
		BillingOrderListMap.put("ul", ul);
		renderJson(BillingOrderListMap);
	}

	// 审批
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CONFIRMATION})
	public void approvalCostPreInvoiceOrder() {
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		ArapCostInvoiceApplication arapAuditOrder = null;
		if (costPreInvoiceOrderId != null && !"".equals(costPreInvoiceOrderId)) {
			arapAuditOrder = ArapCostInvoiceApplication.dao
					.findById(costPreInvoiceOrderId);
			arapAuditOrder.set("status", "已审批");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");
			arapAuditOrder.set("approver_by", users.get(0).get("id"));
			arapAuditOrder.set("approval_stamp", new Date());
			arapAuditOrder.update();
		}
		Map BillingOrderListMap = new HashMap();
		UserLogin ul = UserLogin.dao
				.findById(arapAuditOrder.get("approver_by"));
		BillingOrderListMap.put("arapAuditOrder", arapAuditOrder);
		BillingOrderListMap.put("ul", ul);
		renderJson(BillingOrderListMap);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_UPDATE})
	public void edit() throws ParseException {
		String id = getPara("id");
		ArapCostInvoiceApplication arapAuditInvoiceApplication = ArapCostInvoiceApplication.dao
				.findById(id);
		Contact con = Contact.dao.findById(arapAuditInvoiceApplication.get("payee_id")
				.toString());
		
		//已付总金额
//		String sql = "select sum(pay_amount) totalPay from arap_cost_order aco where aco.application_order_id = '" + id +"'";
//		Record totalPay = Db.findFirst(sql);
//		setAttr("totalPay", totalPay.get("totalPay"));
		
		String company_name =con.get("company_name");
		Long customerId = arapAuditInvoiceApplication.get("payee_id");
		setAttr("payee_unit",arapAuditInvoiceApplication.get("payee_unit"));
		setAttr("billing_unit",arapAuditInvoiceApplication.get("billing_unit"));
		setAttr("create_stamp", arapAuditInvoiceApplication.get("create_stamp"));
		setAttr("audit_stamp", arapAuditInvoiceApplication.get("audit_stamp"));
		setAttr("bill_type",arapAuditInvoiceApplication.get("bill_type"));
		setAttr("approval_stamp",
				arapAuditInvoiceApplication.get("approval_stamp"));
		setAttr("noInvoice", arapAuditInvoiceApplication.get("noInvoice"));
		setAttr("company_name",company_name);
		setAttr("payee_name",arapAuditInvoiceApplication.get("payee_name"));
		setAttr("bank_name",arapAuditInvoiceApplication.get("bank_name"));
		setAttr("bank_no",arapAuditInvoiceApplication.get("bank_no"));
		setAttr("num_name",arapAuditInvoiceApplication.get("num_name"));
		UserLogin userLogin = UserLogin.dao
				.findById(arapAuditInvoiceApplication.get("create_by"));
		setAttr("userLogin", userLogin);
		setAttr("arapAuditInvoiceApplication", arapAuditInvoiceApplication);
		
		//需付款金额
		Record rec = Db.findFirst("SELECT sum(caor.pay_amount) pay_amount_a FROM arap_cost_order aco LEFT JOIN cost_application_order_rel caor ON caor.cost_order_id = aco.id WHERE caor.application_order_id=?",id);
		
		setAttr("paidAmount", 0);
		
		//已付总金额
		Record rec1 = Db.findFirst("SELECT sum(ifnull(caor.pay_amount,0)) total_pay FROM cost_application_order_rel caor"
				+ " WHERE caor.application_order_id=?",id);
		
		setAttr("tpayment", rec1.getDouble("total_pay"));//本次支付金额
		String costCheckOrderIds = "";
		List<ArapCostOrder> arapCostOrders = ArapCostOrder.dao.find(
				"SELECT aco.* FROM `arap_cost_order` aco "
				+ " LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = aco.id "
				+ " where caor.application_order_id = '"+id+"'"
				);
		if(arapCostOrders.size()== 0){
			arapCostOrders = ArapCostOrder.dao.find(
	                "select * from arap_cost_order where application_order_id = '"+ id+"'");
		}
		for (ArapCostOrder arapCostOrder : arapCostOrders) {
			costCheckOrderIds += arapCostOrder.get("id") + ",";
		}
		costCheckOrderIds = costCheckOrderIds.substring(0,
				costCheckOrderIds.length() - 1);
		setAttr("costCheckOrderIds", costCheckOrderIds);
		
		//已付总金额
		Record re = Db.findFirst("select sum(aco.pay_amount) paid_amount from cost_application_order_rel aco where aco.cost_order_id in("+costCheckOrderIds+")");
		Double paidAmount = re.getDouble("paid_amount");
		setAttr("paidAmount", paidAmount);
		
		
		userLogin = UserLogin.dao.findById(arapAuditInvoiceApplication
				.get("approver_by"));
		if(userLogin!=null){
			setAttr("approver_name", userLogin.get("c_name"));
			
			userLogin = UserLogin.dao.findById(arapAuditInvoiceApplication
					.get("audit_by"));
			setAttr("audit_name", userLogin.get("c_name"));
		}
		render("/yh/arap/CostPreInvoiceOrder/CostPreInvoiceOrderEdit.html");
	}

	// 添加发票
	public void addInvoiceItem() {
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		ArapCostInvoiceApplication application = ArapCostInvoiceApplication.dao
				.findById(costPreInvoiceOrderId);
		if (costPreInvoiceOrderId != null && !"".equals(costPreInvoiceOrderId)) {
			ArapCostInvoiceItemInvoiceNo arapCostInvoiceItemInvoiceNo = new ArapCostInvoiceItemInvoiceNo();
			arapCostInvoiceItemInvoiceNo.set("invoice_id",
					costPreInvoiceOrderId);
			if (application.get("payee_id") != null
					&& !"".equals(application.get("payee_id"))) {
				arapCostInvoiceItemInvoiceNo.set("payee_id",
						application.get("payee_id"));
			}
			arapCostInvoiceItemInvoiceNo.save();
		}
		renderJson("{\"success\":true}");
	}

	// 删除发票
	public void deleteInvoiceItem() {
		String id = getPara();
		ArapCostInvoiceItemInvoiceNo.dao.deleteById(id);
		renderJson("{\"success\":true}");
	}

	// 发票号列表
	public void costInvoiceItemList() {
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
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
		String sql = "select count(1) total from arap_cost_invoice_item_invoice_no acio"
				+ " left join arap_cost_order_invoice_no  acoi on acoi.invoice_no = acio.invoice_no"
				+ " where acio.invoice_id = '" + costPreInvoiceOrderId +"'";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select distinct acio.*,c.abbr cname,(select group_concat(aco.order_no separator '\r\n') from arap_cost_order aco left join arap_cost_order_invoice_no app_no on app_no.cost_order_id = aco.id where app_no.invoice_no = acio.invoice_no) cost_order_no "
						+ " from arap_cost_invoice_item_invoice_no acio"
						+ " left join arap_cost_order_invoice_no  acoi on acoi.invoice_no = acio.invoice_no"
						+ " left join party p on p.id = acio.payee_id left join contact c on c.id = p.contact_id"
						+ " where acio.invoice_id = '"
						+ costPreInvoiceOrderId
						+ "' " + sLimit);

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);
		renderJson(orderMap);
	}

	// 更新InvoiceItem信息
	public void updateInvoiceItem() {
		List<ArapCostInvoiceItemInvoiceNo> arapCostInvoiceItemInvoiceNos = new ArrayList<ArapCostInvoiceItemInvoiceNo>();
		ArapCostInvoiceItemInvoiceNo itemInvoiceNo = ArapCostInvoiceItemInvoiceNo.dao
				.findById(getPara("invoiceItemId"));
		String name = getPara("name");
		String value = getPara("value");
		if ("amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		itemInvoiceNo.set(name, value);
		itemInvoiceNo.update();
		if ("invoice_no".equals(name) && !"".equals(value)) {
			arapCostInvoiceItemInvoiceNos = ArapCostInvoiceItemInvoiceNo.dao
					.find("select * from arap_cost_invoice_item_invoice_no where invoice_id = ?",
							getPara("costPreInvoiceOrderId"));
		}
		renderJson(arapCostInvoiceItemInvoiceNos);
	}

	// 获取所有的发票号
	public void findAllInvoiceItemNo() {
		List<ArapCostInvoiceItemInvoiceNo> arapCostInvoiceItemInvoiceNos = ArapCostInvoiceItemInvoiceNo.dao
				.find("select * from arap_cost_invoice_item_invoice_no where invoice_id = ?",
						getPara("costPreInvoiceOrderId"));
		renderJson(arapCostInvoiceItemInvoiceNos);
	}
	
	
	// 更新ArapCostOrder信息
	public void updateArapCostOrder() {
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		String costOrderId = getPara("costOrderId");
		
//		ArapCostInvoiceApplication arapCostInvoiceApplication = ArapCostInvoiceApplication.dao
//				.findById(costPreInvoiceOrderId);
//		String name = getPara("name");
//		String value = getPara("value");
		
//		CostApplicationOrderRel CostApplicationOrderRel = CostApplicationOrderRel.dao
//				.findById(costOrderId);
		String sql = "select * from cost_application_order_rel where application_order_id = '"+costPreInvoiceOrderId+"' and cost_order_id = '"+costOrderId+"'";
		CostApplicationOrderRel costApplicationOrderRel = CostApplicationOrderRel.dao.findFirst(sql);
		//SELECT sum(caor.pay_amount) FROM arap_cost_order acom 	LEFT JOIN cost_application_order_rel caor ON caor.cost_order_id = aco.id WHERE caor.application_order_id = aaia.id
		String name = getPara("name");
		String value = getPara("value");
		
		if ("pay_amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		costApplicationOrderRel.set(name, value);
		costApplicationOrderRel.update();
		CostApplicationOrderRel costApplicationOrderRel1 = CostApplicationOrderRel.dao.findFirst(
				"SELECT sum(caor.pay_amount) pay_amount_a FROM cost_application_order_rel caor"
				+ " WHERE caor.application_order_id=?",costPreInvoiceOrderId);
		Double  pay_amount_a = costApplicationOrderRel1.getDouble("pay_amount_a");
		Map map = new HashMap();
		map.put("pay_amount_a", pay_amount_a);
		map.put("costApplicationOrderRel", costApplicationOrderRel);
		renderJson(map);
	}

		
	// 更新开票申请单
	public void updatePreInvoice() {
		String name = getPara("name");

		String[] values = null;
		Map<String, String[]> map = getParaMap();
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			if ("value[]".equals(entry.getKey())) {
				values = entry.getValue();
			}
		}
		String costCheckOrderId = getPara("costCheckOrderId");
		List<ArapCostOrderInvoiceNo> arapCostOrderInvoiceNos = ArapCostOrderInvoiceNo.dao
				.find("select * from arap_cost_order_invoice_no where cost_order_id = ?",
						costCheckOrderId);
		for (ArapCostOrderInvoiceNo arapCostOrderInvoiceNo : arapCostOrderInvoiceNos) {
			arapCostOrderInvoiceNo.delete();
		}
		if (values != null) {
			for (int i = 0; i < values.length && values.length > 0; i++) {
				ArapCostOrderInvoiceNo arapCostOrderInvoiceNo = ArapCostOrderInvoiceNo.dao
						.findFirst(
								"select * from arap_cost_order_invoice_no where cost_order_id = ? and invoice_no = ?",
								costCheckOrderId, values[i]);
				if (arapCostOrderInvoiceNo == null) {
					arapCostOrderInvoiceNo = new ArapCostOrderInvoiceNo();
					arapCostOrderInvoiceNo.set(name, values[i]);
					arapCostOrderInvoiceNo.set("cost_order_id",
							costCheckOrderId);
					arapCostOrderInvoiceNo.save();
				}
			}
		}
		renderJson("{\"success\":true}");
	}

	public void costCheckOrderList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sp = getPara("sp");
		String customer = getPara("customer");
		String orderNo = getPara("orderNo");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");

		String sqlTotal = "";
		String sql = "select * from(select "
				+ " ( SELECT sum(caor.pay_amount) total_pay FROM cost_application_order_rel caor"
				+ " WHERE caor.cost_order_id = aco.id and caor.order_type='对账单'"
				+ " ) total_pay,"
				+ " '对账单' order_type,"
				+ " aco.id,"
				+ " aco.order_no,"
			    + " aco.status,"
			    + " aco.total_amount,"
			    + " aco.debit_amount,"
			    + " aco.cost_amount,"
			    + " aco.remark,"
			    + " aco.create_stamp, MONTH (aco.create_stamp) AS c_stamp,c.company_name as company_name,"
			    + " '' invoice_no,"
			    + " c.abbr sp_name,"
			    + " ifnull(ul.c_name, ul.user_name) creator_name,o.office_name oname from arap_cost_order aco "
				+ " left join party p on p.id = aco.payee_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login ul on ul.id = aco.create_by"
				+ " left join office o on o.id=p.office_id"
				+ " where aco.status = '已确认' "
				+ " or "
				+ " (aco.status in ( '付款申请中','部分付款申请中')  and  "
				+ " ( SELECT ifnull(sum(caor.pay_amount), '') total_pay"
				+ " FROM cost_application_order_rel caor"
				+ " WHERE caor.cost_order_id = aco.id ) < aco.total_amount "
				+ " and  ( SELECT sum(caor.pay_amount) total_pay FROM cost_application_order_rel caor"
				+ " WHERE caor.cost_order_id = aco.id "
				+ " ) is not null ) "
				+ " union "
				+ " select "
			    + " ( SELECT sum(caor.pay_amount) total_pay FROM cost_application_order_rel caor"
				+ " WHERE caor.cost_order_id = ppo.id and caor.order_type='预付单'"
				+ " ) total_pay,"
				+ " '预付单' order_type,"
				+ " ppo.id,"
				+ " ppo.order_no,"
			    + " ppo.status,"
			    + " ppo.total_amount,"
			    + " 0 debit_amount,"
			    + " 0 cost_amount,"
			    + " ppo.remark,"
			    + " ppo.create_date create_stamp,"
			    + " MONTH(ppo.create_date) AS c_stamp,"
			    + " c.company_name AS company_name,"
			    + " '' invoice_no,"
			    + " c.abbr sp_name,"
			    + " ifnull(ul.c_name, ul.user_name) creator_name,"
			    + " o.office_name oname "
			    + " from arap_pre_pay_order ppo "
				+ " left outer join party p on ppo.sp_id = p.id"
			    + " left outer join contact c on c.id = p.contact_id"
			    + " LEFT outer JOIN user_login ul ON ppo.creator=ul.id"
			    + " LEFT outer JOIN office o ON ppo.office_id=o.id"
			    + " where ppo.status in ('新建', '部分付款申请中', '付款申请中')"
			    + " and case "
			    + "   when ppo.total_amount>0 then "
			    + "     (ppo.total_amount > (SELECT  IFNULL(SUM(caor.pay_amount), 0) total_pay"
		        + "                            FROM cost_application_order_rel caor"
		        + "                          WHERE caor.cost_order_id = ppo.id AND caor.order_type = '预付单')"
				+ "     )"
		        + " when ppo.total_amount<0 then "
		        + "     (ppo.total_amount < (SELECT  IFNULL(SUM(caor.pay_amount), 0) total_pay"
		        + "                           FROM  cost_application_order_rel caor"
		        + "                            WHERE caor.cost_order_id = ppo.id AND caor.order_type = '预付单')"
			    + "	    )"
		        + "  end"
				+ ") A";
		String condition = "";
		// TODO 客户条件过滤没有做
		if (sp != null || customer != null || orderNo != null
				|| beginTime != null || endTime != null) {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-01-01";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
			condition = " where ifnull(A.order_no,'') like '%" + orderNo
					+ "%' " + " and ifnull(A.sp_name,'') like '%" + sp + "%' "
					+ " and A.create_stamp between '" + beginTime + "' and '"
					+ endTime + " 23:59:59' ";

		}

		
		sql = sql + condition
				+ " order by A.create_stamp desc " ;

		sqlTotal = "select count(1) total from ("+sql+") B";
		
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		List<Record> BillingOrders = Db.find(sql+ sLimit);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	
	public void costCheckOrderListById() {
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		if (costPreInvoiceOrderId == null || "".equals(costPreInvoiceOrderId)) {
			costPreInvoiceOrderId = "-1";
		}
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from arap_cost_invoice_application_order appl_order"
				+ " LEFT JOIN cost_application_order_rel caor on caor.application_order_id = appl_order.id "
//				+ " LEFT JOIN arap_cost_order aco on aco.id = caor.cost_order_id"
//				+ " left join party p on p.id = aco.payee_id left join contact c on c.id = p.contact_id"
//				+ " left join user_login ul on ul.id = aco.create_by"
				+ " where appl_order.id = "
				+ costPreInvoiceOrderId;
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select '对账单' order_type,"
				+ " aco.id,"
			    + " aco.order_no,"
			    + " aco.status,"
			    + " aco.remark,"
			    + " aco.cost_amount,"
			    + " aco.total_amount,"
			    + " aco.debit_amount,"
			    + " aco.create_stamp,"
				+ " c.abbr cname, "
				+ " ifnull(ul.c_name,ul.user_name) creator_name,"
				+ " (select group_concat(acai.invoice_no) from arap_cost_order aaia left join arap_cost_order_invoice_no acai on acai.cost_order_id = aaia.id where aaia.id = aco.id) invoice_no,"
				+ " (select group_concat(cost_invoice_no.invoice_no separator ',') from arap_cost_invoice_item_invoice_no cost_invoice_no where cost_invoice_no.invoice_id = appl_order.id) all_invoice_no,"				
				+ " ( SELECT ifnull(sum(caor.pay_amount), 0) total_pay FROM cost_application_order_rel caor"
				+ " WHERE caor.cost_order_id = aco.id ) total_pay ,"
				+ " ( SELECT caor.pay_amount this_pay FROM cost_application_order_rel caor"
				+ " WHERE caor.cost_order_id = aco.id and caor.order_type='对账单' and caor.application_order_id = appl_order.id ) pay_amount ,"
				+ " (aco.cost_amount - (SELECT ifnull(sum(caor.pay_amount), 0) total_pay FROM cost_application_order_rel caor WHERE caor.cost_order_id = aco.id and caor.order_type='对账单')) yufu_amount "
				+ " from arap_cost_invoice_application_order appl_order, cost_application_order_rel caor"
				+ " LEFT JOIN arap_cost_order aco on aco.id = caor.cost_order_id"
				+ " left join party p on p.id = aco.payee_id left join contact c on c.id = p.contact_id"
				+ " left join user_login ul on ul.id = aco.create_by"
				+ " where caor.application_order_id = appl_order.id and caor.order_type = '对账单' and appl_order.id = "
				+ costPreInvoiceOrderId
				+ " union"
				+ " select"
				+ " '预付单' order_type,"
				+ " ppo.id,"
				+ " ppo.order_no,"
				+ " ppo.status,"
				+ " ppo.remark,"
				+ " ppo.total_amount cost_amount,"//应付金额
				+ " ppo.total_amount,"//对账金额
				+ " 0 debit_amount,"
				+ " ppo.create_date create_stamp,"
				+ " c.abbr cname,"
				+ " ifnull(ul.c_name, ul.user_name) creator_name,"
				+ " '' invoice_no,"
				+ " '' all_invoice_no,"
				+ " 0 total_pay,"
				+ "  ( SELECT caor.pay_amount this_pay FROM cost_application_order_rel caor"
				+ " WHERE caor.cost_order_id = ppo.id and caor.order_type='预付单' and caor.application_order_id = appl_order.id ) pay_amount,"
				+ " (ppo.total_amount - (SELECT ifnull(sum(caor.pay_amount), 0) total_pay FROM cost_application_order_rel caor WHERE caor.cost_order_id = ppo.id and caor.order_type='预付单')) yufu_amount "
				+ " from  arap_cost_invoice_application_order appl_order, cost_application_order_rel caor"
				+ " LEFT JOIN  arap_pre_pay_order ppo on caor.cost_order_id = ppo.id"
				+ " left outer join party p on ppo.sp_id = p.id"
				+ " left outer join contact c on c.id = p.contact_id"
				+ " LEFT outer JOIN user_login ul ON ppo.creator=ul.id"
				+ " LEFT outer JOIN office o ON ppo.office_id=o.id"
				+ " where caor.application_order_id = appl_order.id AND caor.order_type = '预付单' and appl_order.id = "+ costPreInvoiceOrderId
				+ sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);
		//以前都逻辑
//		if(BillingOrders.size()!=0&&BillingOrders.get(0).getLong("id")== null){
//			 sql = "select aco.*,c.abbr cname, (select group_concat(acai.invoice_no) from arap_cost_order aaia left join arap_cost_order_invoice_no acai on acai.cost_order_id = aaia.id where aaia.id = aco.id) invoice_no,"
//					+ " (select group_concat(cost_invoice_no.invoice_no separator ',') from arap_cost_invoice_item_invoice_no cost_invoice_no where cost_invoice_no.invoice_id = appl_order.id) all_invoice_no,ul.user_name creator_name,"
//					+ " ( SELECT ifnull(sum(caor.pay_amount), 0) total_pay FROM cost_application_order_rel caor"
//					+ " WHERE caor.cost_order_id = aco.id ) total_pay ,"
//					+ " ( SELECT caor.pay_amount this_pay FROM cost_application_order_rel caor"
//					+ " WHERE caor.cost_order_id = aco.id and caor.application_order_id = appl_order.id ) pay_amount ,"
//					+ " (aco.cost_amount - (SELECT ifnull(sum(caor.pay_amount), 0) total_pay FROM cost_application_order_rel caor WHERE caor.cost_order_id = aco.id )) yufu_amount "
//					+ " from arap_cost_invoice_application_order appl_order"
//	                + " left join arap_cost_order aco on aco.application_order_id = appl_order.id"
//					+ " left join party p on p.id = aco.payee_id left join contact c on c.id = p.contact_id"
//					+ " left join user_login ul on ul.id = aco.create_by"
//					+ " where appl_order.id = "
//					+ costPreInvoiceOrderId
//					+ " order by aco.create_stamp desc " + sLimit;
//			BillingOrders = Db.find(sql);
//		}

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}
	
	//收款确认
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTCONFIRM_CONFIRM})
    public void payConfirm(){
    	String costIds = getPara("costIds");
    	String paymentMethod = getPara("paymentMethod");
    	String accountId = getPara("accountTypeSelect");
    	String[] costIdArr = null; 
    	if(costIds != null && !"".equals(costIds)){
    		costIdArr = costIds.split(",");
    	}
    	
    	String id = "";
    	for(int i=0;i<costIdArr.length;i++){
    		String[] arr = costIdArr[i].split(":");
    		String orderId = arr[0];
    		id = orderId;
    		String orderNo = arr[1];
            if(orderNo.startsWith("SGFK")){
				ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(orderId);
				arapMiscCostOrder.set("status", "已付款确认");
				arapMiscCostOrder.update();
            }else{
                ArapCostInvoiceApplication arapcostInvoice = ArapCostInvoiceApplication.dao.findById(orderId);
                arapcostInvoice.set("status", "已付款确认");
                arapcostInvoice.update();
               /* //应收对账单的状态改变
                ArapCostOrder arapAuditOrder = ArapCostOrder.dao.findFirst("select * from arap_cost_order where application_order_id = ?",orderId);
                arapAuditOrder.set("status", "已付款确认");
                arapAuditOrder.update();
                //手工付款单的状态改变：注意有的对账单没有手工付款单
                Long arapMiscId = arapAuditOrder.get("id");
                if(arapMiscId != null && !"".equals(arapMiscId)){
                	List<ArapMiscCostOrder> list = ArapMiscCostOrder.dao.find("select * from arap_misc_cost_order where cost_order_id = ?",arapMiscId);
                	if(list.size()>0){
                		for (ArapMiscCostOrder model : list) {
                			model.set("status", "对账已完成");
                			model.update();
						}
                	}
                    
                }*/
                
            	}
			
				//现金 或 银行  金额处理
				if("cash".equals(paymentMethod)){
					Account account = Account.dao.findFirst("select * from fin_account where bank_name ='现金'");
					if(account!=null){
						Record rec = null;
						if(orderNo.startsWith("SGFK")){
							rec = Db.findFirst("select sum(amcoi.amount) total from arap_misc_cost_order amco, arap_misc_cost_order_item amcoi "
									+ "where amco.id = amcoi.misc_order_id and amco.order_no='"+orderNo+"'");
		                    if(rec!=null){
		                    	double total = rec.getDouble("total")==null?0.0:rec.getDouble("total");
		                        //银行账户 金额处理
		                        account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) - total).update();
		                        //日记账
		                        createAuditLog(orderId, account, total, paymentMethod, "手工付款单");
		                    }
						}else{
							//rec = Db.findFirst("select aci.total_amount total from arap_cost_invoice_application_order aci where aci.order_no='"+orderNo+"'");
							String sql = "select sum(caor.pay_amount) total from arap_cost_invoice_application_order aci "
									+ " LEFT JOIN cost_application_order_rel caor on caor.application_order_id = aci.id"
									+ " where aci.id = '"+id+"'";
							rec = Db.findFirst(sql);
							if(rec.getDouble("total") == null){
	                            rec = Db.findFirst("select aci.total_amount total from arap_cost_invoice_application_order aci where aci.order_no='"+orderNo+"'");
							}
							if(rec!=null){
		                    	double total = rec.getDouble("total")==null?0.0:rec.getDouble("total");
		                        //银行账户 金额处理
		                        account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) - total).update();
		                        //日记账
		                        createAuditLog(orderId, account, total, paymentMethod, "应付开票申请单");
		                    }
						}
					}
				}else{//银行账户  金额处理
				    Account account = Account.dao.findFirst("select * from fin_account where id ="+accountId);
	                if(account!=null){
	                	Record rec = null;
						if(orderNo.startsWith("SGFK")){
							rec = Db.findFirst("select sum(amcoi.amount) total from arap_misc_cost_order amco, arap_misc_cost_order_item amcoi "
									+ "where amco.id = amcoi.misc_order_id and amco.order_no='"+orderNo+"'");
		                    if(rec!=null){
		                    	double total = rec.getDouble("total")==null?0.0:rec.getDouble("total");
		                        //银行账户 金额处理
		                        account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) - total).update();
		                        //日记账
		                        createAuditLog(orderId, account, total, paymentMethod, "手工付款单");
		                    }
						}else{
							//rec = Db.findFirst("select aci.total_amount total from arap_cost_invoice_application_order aci where aci.order_no='"+orderNo+"'");
							String sql = "select sum(caor.pay_amount) total from arap_cost_invoice_application_order aci "
									+ " LEFT JOIN cost_application_order_rel caor on caor.application_order_id = aci.id"
									+ " where aci.id = '"+id+"'";
							rec = Db.findFirst(sql);
							if(rec.getDouble("total") == null){
	                            rec = Db.findFirst("select aci.total_amount total from arap_cost_invoice_application_order aci where aci.order_no='"+orderNo+"'");
							}
		                    if(rec!=null){
		                    	double total = rec.getDouble("total")==null?0.0:rec.getDouble("total");
		                        //银行账户 金额处理
		                        account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) - total).update();
		                        //日记账
		                        createAuditLog(orderId, account, total, paymentMethod, "应付开票申请单");
		                    }
						}
	                }
				}
    		}
    		redirect("/costPreInvoiceOrder/edit?id="+id);
    	}

	
		private void createAuditLog(String orderId, Account account, double total, String paymentMethod, String sourceOrder) {
	        ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
	        auditLog.set("payment_method", paymentMethod);
	        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_COST);
	        auditLog.set("amount", total);
	        auditLog.set("creator", LoginUserController.getLoginUserId(this));
	        auditLog.set("create_date", new Date());
	        auditLog.set("misc_order_id", orderId);
	        auditLog.set("invoice_order_id", null);
	        auditLog.set("account_id", account.get("id"));
	        auditLog.set("source_order", sourceOrder);
	        auditLog.save();
	    }	
	
	
}
