package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import models.yh.arap.ArapAccountAuditSummary;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ReimbursementOrder;
import models.yh.arap.inoutorder.ArapInOutMiscOrder;
import models.yh.arap.prePayOrder.ArapPrePayOrder;
import models.yh.carmanage.CarSummaryOrder;
import models.yh.damageOrder.DamageOrder;
import models.yh.damageOrder.DamageOrderFinItem;
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

		Contact contact = Contact.dao.findFirst("select c.company_name from contact c left join party p on c.id = p.contact_id where p.id =?",party.get("contact_id")
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
		String ids = getPara("ids");
		ArapCostInvoiceApplication arapAuditInvoiceApplication = null;
		String application_id = getPara("application_id");
		String paymentMethod = getPara("payment_method");//付款方式
		String bank_no = getPara("bank_no");          //收款账号
		String payee_name = getPara("payee_name");    //收款人
		String numname = getPara("account_name");   //账户名
		String payee_unit = getPara("payee_unit");      //收款单位
		String payee_id = getPara("payee_id")==""?null:getPara("payee_id");         //付款给
		String billing_unit = getPara("billing_unit"); //收款单位
		String billtype = getPara("invoice_type");   //开票类型
		String bank_name= getPara("deposit_bank");   //开户行
		String total_amount = getPara("total_amount")==""?"0.00":getPara("total_amount");   //申请总金额

		
		if (!"".equals(application_id) && application_id != null) {
			arapAuditInvoiceApplication = ArapCostInvoiceApplication.dao.findById(application_id);
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

				CostApplicationOrderRel costApplicationOrderRel = CostApplicationOrderRel.dao.findFirst("select * from cost_application_order_rel where cost_order_id =? and application_order_id = ?",id,application_id);
				costApplicationOrderRel.set("application_order_id", arapAuditInvoiceApplication.getLong("id"));
				costApplicationOrderRel.set("cost_order_id", id);
				costApplicationOrderRel.set("order_type", order_type);
				costApplicationOrderRel.set("pay_amount", value);
				costApplicationOrderRel.update();
			}
		} else {
			arapAuditInvoiceApplication = new ArapCostInvoiceApplication();
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
				String cname = (String)map.get("payee_unit");

				CostApplicationOrderRel costApplicationOrderRel = new CostApplicationOrderRel();
				costApplicationOrderRel.set("application_order_id", arapAuditInvoiceApplication.getLong("id"));
				costApplicationOrderRel.set("cost_order_id", id);
				costApplicationOrderRel.set("order_type", order_type);
				costApplicationOrderRel.set("pay_amount", value);
				
				if(cname!=null)
					costApplicationOrderRel.set("payee_unit", cname);
				costApplicationOrderRel.save();
				
                if(order_type.equals("对账单")){
					ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
					arapCostOrder.set("status", "付款申请中").update();
				}else if(order_type.equals("成本单")){
					ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(id);
					arapMiscCostOrder.set("audit_status", "付款申请中").update();
				}else if(order_type.equals("行车单")){
					CarSummaryOrder carSummaryOrder = CarSummaryOrder.dao.findById(id);
					carSummaryOrder.set("status", "付款申请中").update();
				}else if(order_type.equals("预付单")){
					ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao.findById(id);
					arapPrePayOrder.set("status", "付款申请中").update();
				}else if(order_type.equals("报销单")){
					ReimbursementOrder reimbursementOrder = ReimbursementOrder.dao.findById(id);
					reimbursementOrder.set("status", "付款申请中").update();
				}else if(order_type.equals("往来票据单")){
					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
					arapInOutMiscOrder.set("pay_status", "付款申请中").update();
				}else if(order_type.equals("货损单")){
					DamageOrder damageOrder = DamageOrder.dao.findById(id);
					if(!damageOrder.getStr("status").equals("已结案"))
						damageOrder.set("status", "单据处理中").update();
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

		String sql = "select * from cost_application_order_rel where application_order_id = '"+costPreInvoiceOrderId+"' and cost_order_id = '"+costOrderId+"'";
		CostApplicationOrderRel costApplicationOrderRel = CostApplicationOrderRel.dao.findFirst(sql);
		//SELECT sum(caor.pay_amount) FROM arap_cost_order acom 	LEFT JOIN cost_application_order_rel caor ON caor.cost_order_id = aco.id WHERE caor.application_order_id = aaia.id
		String name = getPara("name");
		String value = getPara("value");
		Double oldAmount = costApplicationOrderRel.getDouble("pay_amount");
		String tips = "";
		if ("pay_amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		
		if(oldAmount!=null){
			if(!oldAmount.equals(Double.parseDouble(value))){
				costApplicationOrderRel.set(name, value);
				tips = "success";
			}else{
				tips = "noChange";
			}
		}else{
			costApplicationOrderRel.set(name, value);
			tips = "success";
		}
		
		costApplicationOrderRel.set(name, value);
		costApplicationOrderRel.update();
		CostApplicationOrderRel costApplicationOrderRel1 = CostApplicationOrderRel.dao.findFirst(
				"SELECT sum(caor.pay_amount) pay_amount_a FROM cost_application_order_rel caor"
				+ " WHERE caor.application_order_id=?",costPreInvoiceOrderId);
		Double  pay_amount_a = costApplicationOrderRel1.getDouble("pay_amount_a");
		Map map = new HashMap();
		map.put("pay_amount_a", pay_amount_a);
		map.put("tips", tips);
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
		
		
		
		
		@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CREATE})
		public void create() {
			String ids = getPara("sids");
			setAttr("ids", ids);
			
			String payee_id = "";
			String payee_filter = "";
			String payee_name = "";
			String deposit_bank = "";
			String bank_no = "";
			String account_name = "";
			String[] orderArrId=ids.split(",");
			for (int i=0;i<orderArrId.length;) {
					String[] one=orderArrId[i].split(":");
					String id = one[0];
					String orderType = one[1];
					
					if("应付对账单".equals(orderType)){
 						ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
 						payee_id = arapCostOrder.getLong("payee_id").toString();
 					}else if("预付单".equals(orderType)){
 						ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao.findById(id);
 						payee_id = arapPrePayOrder.getLong("sp_id").toString();
 					}else if("成本单".equals(orderType)){
 						ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(id);
 						String type = arapMiscCostOrder.getStr("cost_to_type");
 						payee_name = arapMiscCostOrder.getStr("others_name");
 						if(type.equals("sp")){
 							payee_id = arapMiscCostOrder.getLong("sp_id").toString();
 						}else if(type.equals("customer")){
 							payee_id = arapMiscCostOrder.getLong("customer_id").toString();
 						}else if(type.equals("insurance")){
 							payee_id = arapMiscCostOrder.getLong("insurance_id").toString();
 						}
 					}else if("行车单".equals(orderType)){
 						CarSummaryOrder carSummaryOrder = CarSummaryOrder.dao.findById(id);
 						payee_name = carSummaryOrder.getStr("main_driver_name");
 					}else if("报销单".equals(orderType)){
 						ReimbursementOrder reimbursementOrder = ReimbursementOrder.dao.findById(id);
 						payee_name = reimbursementOrder.getStr("account_name");
 						deposit_bank = reimbursementOrder.getStr("account_bank");
 						bank_no = reimbursementOrder.getStr("account_no");
 						account_name = reimbursementOrder.getStr("account_name");
 					}else if("往来票据单".equals(orderType)){
 						ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
 						payee_name = arapInOutMiscOrder.getStr("charge_person");
 					}else if("货损单".equals(orderType)){
 						String cname = one[2];
 						Record record = Db.findFirst("select id from contact c where c.abbr = '"+cname+"' ");
 						if(record != null)
 							payee_id = record.getLong("id").toString();
 					}
					break;
			}
			
			if(!payee_id.equals("")){
				Contact contact = Contact.dao.findFirst("select * from contact c left join party p on c.id = p.contact_id where p.id = ?",payee_id);
				payee_filter = contact.getStr("company_name");
				deposit_bank = contact.getStr("bank_name");
				bank_no = contact.getStr("bank_no");
				account_name = contact.getStr("receiver");
			}
			setAttr("payee_filter", payee_filter);
			setAttr("deposit_bank", deposit_bank);
			setAttr("bank_no", bank_no);
			setAttr("account_name", account_name);
			
			setAttr("payee_id", payee_id);
			setAttr("payee_name", payee_name);
			
				
			List<Record> Account = null;
			Account = Db.find("select * from fin_account where bank_name != '现金'");
			setAttr("accountList", Account);
			
			setAttr("submit_name", LoginUserController.getLoginUserName(this));
			setAttr("saveOK", false);
			setAttr("status", "new");
			render("/yh/arap/CostAcceptOrder/payEdit.html");
		}
		
		
		//新模块
		//性逻辑
		public void costOrderList() {
	        String ids = getPara("ids");
	        String application_id = getPara("application_id");
	        String dz_id ="" ;//对账单
	        String yf_id = "";//预付单
	        String cb_id = "";//成本单
	        String xc_id = "";//行车单
	        String bx_id = "";//报销单
	        String wl_id = "";//往来票据单
	        String hs_id = "";//货损单
	        String sql = "";
	        String cname = "";
	        String payee_unit = "";
	        if(application_id.equals("")){
	        	if(!application_id.equals(ids)){
	        		String[] orderArrId=ids.split(",");
	        		
	 				for (int i=0;i<orderArrId.length;i++) {
	 					String[] one=orderArrId[i].split(":");
	 					String id = one[0];
	 					String orderType = one[1];
	 					
	 					
	 					if("应付对账单".equals(orderType)){
	 						dz_id += id+",";
	 					}else if("预付单".equals(orderType)){
	 						yf_id += id+",";
	 					}else if("成本单".equals(orderType)){
	 						cb_id += id+",";
	 					}else if("行车单".equals(orderType)){
	 						xc_id += id+",";
	 					}else if("报销单".equals(orderType)){
	 						bx_id += id+",";
	 					}else if("往来票据单".equals(orderType)){
	 						wl_id += id+",";
	 					}else if("货损单".equals(orderType)){
	 						cname =" and dofi.party_name = '"+ one[2] +"'";
	 						payee_unit = one[2];
	 						hs_id += id+",";
	 					}
	 				}
	 				if(!dz_id.equals(""))
	 					dz_id = dz_id.substring(0, dz_id.length()-1);
	 				else
	 					dz_id = "''";
	 				if(!yf_id.equals(""))
	 					yf_id = yf_id.substring(0, yf_id.length()-1);
	 				else
	 					yf_id = "''";
	 				if(!cb_id.equals(""))
	 					cb_id = cb_id.substring(0, cb_id.length()-1);
	 				else
	 					cb_id = "''";
	 				if(!xc_id.equals(""))
	 					xc_id = xc_id.substring(0, xc_id.length()-1);
	 				else
	 					xc_id = "''";
	 				if(!bx_id.equals(""))
	 					bx_id = bx_id.substring(0, bx_id.length()-1);
	 				else
	 					bx_id = "''";
	 				if(!wl_id.equals(""))
	 					wl_id = wl_id.substring(0, wl_id.length()-1);
	 				else
	 					wl_id = "''";
	 				if(!hs_id.equals(""))
	 					hs_id = hs_id.substring(0, hs_id.length()-1);
	 				else
	 					hs_id = "''";
	        	}
		       
				
			
				sql = " SELECT aco.id,aco.payee_id,null payee_name,aco.order_no, '对账单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
						+ " c.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, aco.cost_amount,"
						+ " ( SELECT ifnull(sum(caor.pay_amount),0) FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = aco.id AND caor.order_type = '对账单' "
						+ " ) pay_amount,"
						+ " (aco.cost_amount - (SELECT ifnull(sum(caor.pay_amount), 0) "
						+ " FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = aco.id AND caor.order_type = '对账单'"
						+ " )) yufu_amount,null item_ids,null payee_unit "
						+ " FROM arap_cost_order aco "
						+ " LEFT JOIN party p ON p.id = aco.payee_id"
						+ " LEFT JOIN contact c ON c.id = p.contact_id"
						+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
						+ " WHERE "
						+ " aco.id in(" + dz_id +")"
						+ " union "
						+ " SELECT ppo.id,ppo.sp_id payee_id,null payee_name, ppo.order_no, '预付单' order_type, ppo. STATUS, ppo.remark, "
						+ " ppo.create_date create_stamp, c.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, "
						+ " ppo.total_amount cost_amount,"
						+ " ( SELECT ifnull(sum(caor.pay_amount),0 ) "
						+ " FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = ppo.id"
						+ " AND caor.order_type = '预付单'"
						+ " ) pay_amount,"
						+ " ( ppo.total_amount - ( SELECT ifnull(sum(caor.pay_amount), 0) total_pay"
						+ " FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = ppo.id "
						+ " AND caor.order_type = '预付单' ) ) yufu_amount,null item_ids,null payee_unit "
						+ " FROM arap_pre_pay_order ppo"
						+ " LEFT OUTER JOIN party p ON ppo.sp_id = p.id"
						+ " LEFT OUTER JOIN contact c ON c.id = p.contact_id"
						+ " LEFT OUTER JOIN user_login ul ON ppo.creator = ul.id"
						+ " LEFT OUTER JOIN office o ON ppo.office_id = o.id"
						+ " WHERE "
						+ " ppo.id in(" + yf_id +")"
					    + " union"
					    + " SELECT aco.id,"
					    + " (case when aco.cost_to_type = 'sp' then aco.sp_id"
						+ " when aco.cost_to_type = 'customer' then aco.customer_id"
						+ " when aco.cost_to_type = 'insurance' then aco.insurance_id end) payee_id,aco.others_name payee_name,"
					    + " aco.order_no, '成本单' order_type, aco.audit_STATUS, aco.remark, aco.create_stamp,"
						+ " (case when aco.cost_to_type = 'sp' then (select c.company_name from contact c left join party p on c.id = p.contact_id where p.id = aco.sp_id)"
						+ " when aco.cost_to_type = 'customer' then (select c.company_name from contact c left join party p on c.id = p.contact_id where p.id = aco.customer_id)"
						+ " when aco.cost_to_type = 'insurance' then (select c.company_name from contact c left join party p on c.id = p.contact_id where p.id = aco.insurance_id) end) cname,"
						+ "  ifnull(ul.c_name, ul.user_name) creator_name, aco.total_amount cost_amount,"
						+ " ( SELECT ifnull(sum(caor.pay_amount),0) FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = aco.id AND caor.order_type = '成本单' "
						+ " ) pay_amount,"
						+ " (aco.total_amount - (SELECT ifnull(sum(caor.pay_amount), 0) "
						+ " FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = aco.id AND caor.order_type = '成本单'"
						+ " )) yufu_amount,null item_ids,null payee_unit "
						+ " FROM arap_misc_cost_order aco "
						+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
						+ " WHERE "
						+ " aco.id in(" + cb_id +")"
						 + " union"
					    + " SELECT aco.id,null payee_id,aco.main_driver_name payee_name, aco.order_no, '行车单' order_type, aco.STATUS, '' remark, aco.create_data create_stamp,"
						+ " '' cname,"
						+ " '' creator_name, aco.actual_payment_amount cost_amount,"
						+ " ( SELECT ifnull(sum(caor.pay_amount),0) FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = aco.id AND caor.order_type = '行车单' "
						+ " ) pay_amount,"
						+ " (aco.actual_payment_amount - (SELECT ifnull(sum(caor.pay_amount), 0) "
						+ " FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = aco.id AND caor.order_type = '行车单'"
						+ " )) yufu_amount,null item_ids,null payee_unit "
						+ " FROM car_summary_order aco "
						+ " WHERE "
						+ " aco.id in(" + xc_id +")"
					    + " union "
					    + " SELECT ror.id, null payee_id,ror.account_name payee_name, ror.order_no, '报销单' order_type,"
					    + " ror. STATUS, ror.remark, ror.create_stamp, null cname,"
					    + " ifnull(ul.c_name, ul.user_name) creator_name,"
					    + " ror.amount cost_amount,"
					    + " ( SELECT ifnull(sum(caor.pay_amount), 0)"
					    + " FROM cost_application_order_rel caor"
					    + " WHERE caor.cost_order_id = ror.id"
					    + " AND caor.order_type = '报销单'"
					    + " ) pay_amount,"
					    + " ( ror.amount - ( SELECT ifnull(sum(caor.pay_amount), 0) "
					    + " FROM cost_application_order_rel caor"
					    + " WHERE caor.cost_order_id = ror.id"
					    + " AND caor.order_type = '报销单' ) ) yufu_amount,null item_ids,null payee_unit "
					    + " FROM reimbursement_order ror"
					    + " LEFT JOIN user_login ul ON ul.id = ror.create_id"
					    + " WHERE ror.id in(" + bx_id +")"
					    + " union"
					    + " SELECT aio.id, null payee_id,aio.charge_person payee_name, aio.order_no, '往来票据单' order_type,"
					    + " aio.pay_status STATUS, aio.remark, aio.create_date create_stamp, aio.charge_unit cname,"
					    + " ifnull(ul.c_name, ul.user_name) creator_name,"
					    + " aio.pay_amount cost_amount,"
					    + " ( SELECT ifnull(sum(caor.pay_amount), 0)"
					    + " FROM cost_application_order_rel caor"
					    + " WHERE caor.cost_order_id = aio.id"
					    + " AND caor.order_type = '往来票据单'"
					    + " ) pay_amount,"
					    + " ( aio.pay_amount - ( SELECT ifnull(sum(caor.pay_amount), 0) "
					    + " FROM cost_application_order_rel caor"
					    + " WHERE caor.cost_order_id = aio.id"
					    + " AND caor.order_type = '往来票据单' ) ) yufu_amount,null item_ids,null payee_unit "
					    + " FROM arap_in_out_misc_order aio"
					    + " LEFT JOIN user_login ul ON ul.id = aio.creator_id"
					    + " WHERE aio.id in(" + wl_id +")"
					    + " union"
					    + " SELECT dor.id, dor.customer_id payee_id, '' payee_name, dor.order_no, '货损单' order_type,"
					    + " dofi.status STATUS, dofi.remark, dor.create_date create_stamp,"
					    + " (case when dofi.party_type ='客户' "
					    + " then c.abbr "
        	            + " else"
        	            + " dofi.party_name"
        	            + " end) cname," //收款单位
					    + " ifnull(ul.c_name, ul.user_name) creator_name,"
					    + " sum(ifnull(dofi.amount,0)) cost_amount,"
					    + " ( SELECT ifnull(sum(caor.pay_amount), 0)"
					    + " FROM cost_application_order_rel caor"
					    + " WHERE caor.cost_order_id = dor.id"
					    + " AND caor.order_type = '货损单'"
					    + " and caor.payee_unit = dofi.party_name "
					    + " ) pay_amount,"
					    + " (sum(ifnull(dofi.amount,0)) - ( SELECT ifnull(sum(caor.pay_amount), 0) "
					    + " FROM cost_application_order_rel caor"
					    + " WHERE caor.cost_order_id = dor.id"
					    + " AND caor.order_type = '货损单'  and caor.payee_unit = dofi.party_name ) ) yufu_amount,GROUP_CONCAT(cast(dofi.id as char)) item_ids,'"+payee_unit+"' payee_unit "
					    + " FROM damage_order dor"
					    + " LEFT JOIN damage_order_fin_item dofi on dofi.order_id = dor.id and dofi.type = 'cost' and dofi.status='已确认'"
					    + " LEFT JOIN user_login ul ON ul.id = dor.creator"
					    + "	left join party p on p.id = dor.customer_id "
					    + " left join contact c on c.id = p.contact_id "
					    + " WHERE dor.id in(" + hs_id +")"
					    +   cname
					    + " group by dofi.party_name ";
			}else{
				//Record re = Db.findFirst("select ");
				
				sql = "select * from( SELECT aco.id,aco.payee_id,null payee_name,aco.order_no, '对账单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
						+ " c.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, aco.cost_amount,"
						+ " ( SELECT ifnull(sum(caor.pay_amount),0) FROM cost_application_order_rel caor "
						+ " WHERE "
						+ " caor.cost_order_id = aco.id and caor.application_order_id = aciao.id "
						+ " AND caor.order_type = '对账单' "
						+ " ) pay_amount,"
						+ " (aco.cost_amount - (SELECT ifnull(sum(caor.pay_amount), 0) "
						+ " FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = aco.id"
						+ " AND caor.order_type = '对账单'"
						+ " )) yufu_amount, aciao.id app_id "
						+ " FROM arap_cost_order aco "
						+ " LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = aco.id"
						+ " LEFT JOIN arap_cost_invoice_application_order aciao on aciao.id = caor.application_order_id"
						+ " LEFT JOIN party p ON p.id = aco.payee_id"
						+ " LEFT JOIN contact c ON c.id = p.contact_id"
						+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
						+ " where caor.order_type = '对账单'"
						
						+ " union "
						+ " SELECT ppo.id,ppo.sp_id payee_id,null payee_name,  ppo.order_no, '预付单' order_type, ppo. STATUS, ppo.remark, "
						+ " ppo.create_date create_stamp, c.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, "
						+ " ppo.total_amount cost_amount,"
						+ " ( SELECT ifnull(sum(caor.pay_amount),0 ) "
						+ " FROM cost_application_order_rel caor "
						+ " WHERE "
						+ " caor.application_order_id = aciao.id"
						+ " and caor.cost_order_id = ppo.id"
						+ " AND caor.order_type = '预付单'"
						+ " ) pay_amount,"
						+ " ( ppo.total_amount - ( SELECT ifnull(sum(caor.pay_amount), 0) total_pay"
						+ " FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = ppo.id "
						+ " AND caor.order_type = '预付单' ) ) yufu_amount, aciao.id app_id "
						+ " FROM arap_pre_pay_order ppo"
						+ " LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = ppo.id"
						+ " LEFT JOIN arap_cost_invoice_application_order aciao on aciao.id = caor.application_order_id"
						+ " LEFT OUTER JOIN party p ON ppo.sp_id = p.id"
						+ " LEFT OUTER JOIN contact c ON c.id = p.contact_id"
						+ " LEFT OUTER JOIN user_login ul ON ppo.creator = ul.id"
						+ " LEFT OUTER JOIN office o ON ppo.office_id = o.id"
						+ " where caor.order_type = '预付单'"
					    + " union"
					    + " SELECT aco.id,"
					    + " (case when aco.cost_to_type = 'sp' then aco.sp_id"
						+ " when aco.cost_to_type = 'customer' then aco.customer_id end) payee_id,aco.others_name payee_name, "
					    + " aco.order_no, '成本单' order_type, aco.audit_STATUS, aco.remark, aco.create_stamp,"
						+ " (case when aco.cost_to_type = 'sp' then (select c.company_name from contact c left join party p on c.id = p.contact_id where p.id = aco.sp_id)"
						+ " when aco.cost_to_type = 'customer' then (select c.company_name from contact c left join party p on c.id = p.contact_id where p.id = aco.customer_id) end) cname,"
						+ "  ifnull(ul.c_name, ul.user_name) creator_name, aco.total_amount cost_amount,"
						+ " ( SELECT ifnull(sum(caor.pay_amount),0) FROM cost_application_order_rel caor "
						+ " WHERE "
						+ " caor.application_order_id = aciao.id"
						+ " and caor.cost_order_id = aco.id AND caor.order_type = '成本单' "
						+ " ) pay_amount,"
						+ " (aco.total_amount - (SELECT ifnull(sum(caor.pay_amount), 0) "
						+ " FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = aco.id AND caor.order_type = '成本单'"
						+ " )) yufu_amount, aciao.id app_id "
						+ " FROM arap_misc_cost_order aco "
						+ " LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = aco.id"
						+ " LEFT JOIN arap_cost_invoice_application_order aciao on aciao.id = caor.application_order_id"
						+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
						+ " where caor.order_type = '成本单'"
						 + " union"
					    + " SELECT aco.id,null payee_id,aco.main_driver_name payee_name, aco.order_no, '行车单' order_type, aco.STATUS, '' remark, aco.create_data create_stamp,"
						+ " '' cname,"
						+ " '' creator_name, aco.actual_payment_amount cost_amount,"
						+ " ( SELECT ifnull(sum(caor.pay_amount),0) FROM cost_application_order_rel caor "
						+ " WHERE  "
						+ " caor.application_order_id = aciao.id "
						+ " and caor.cost_order_id = aco.id AND caor.order_type = '行车单' "
						+ " ) pay_amount,"
						+ " (aco.actual_payment_amount - (SELECT ifnull(sum(caor.pay_amount), 0) "
						+ " FROM cost_application_order_rel caor "
						+ " WHERE caor.cost_order_id = aco.id AND caor.order_type = '行车单'"
						+ " )) yufu_amount, aciao.id app_id "
						+ " FROM car_summary_order aco"
						+ " LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = aco.id"
						+ " LEFT JOIN arap_cost_invoice_application_order aciao on aciao.id = caor.application_order_id"
						+ " where caor.order_type = '行车单'"
						+ " union "
					    + " SELECT ror.id, null payee_id,ror.account_name payee_name, ror.order_no, '报销单' order_type,"
					    + " ror. STATUS, ror.remark, ror.create_stamp, null cname,"
					    + " ifnull(ul.c_name, ul.user_name) creator_name,"
					    + " ror.amount cost_amount,"
					    + " ( SELECT ifnull(sum(caor.pay_amount), 0)  FROM cost_application_order_rel caor"
					    + " WHERE caor.cost_order_id = ror.id and "
					    + " caor.application_order_id = aciao.id  AND caor.order_type = '报销单'"
					    + " ) pay_amount,"
					    + " ( ror.amount - ( SELECT ifnull(sum(caor.pay_amount), 0) "
					    + " FROM cost_application_order_rel caor"
					    + " WHERE caor.cost_order_id = ror.id"
					    + " AND caor.order_type = '报销单' ) ) yufu_amount, aciao.id app_id"
					    + " FROM reimbursement_order ror"
					    + " LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = ror.id"
						+ " LEFT JOIN arap_cost_invoice_application_order aciao on aciao.id = caor.application_order_id"
					    + " LEFT JOIN user_login ul ON ul.id = ror.create_id"
					    + " where caor.order_type = '报销单'"
					    + " union"
					    + " SELECT aio.id, null payee_id,aio.charge_person payee_name, aio.order_no, '往来票据单' order_type,"
					    + " aio.pay_status STATUS, aio.remark, aio.create_date create_stamp, aio.charge_unit cname,"
					    + " ifnull(ul.c_name, ul.user_name) creator_name,"
					    + " aio.pay_amount cost_amount,"
					    + " ( SELECT ifnull(sum(caor.pay_amount), 0)"
					    + " FROM cost_application_order_rel caor"
					    + " WHERE "
					    + " caor.cost_order_id = aio.id and caor.application_order_id = aciao.id"
					    + " AND caor.order_type = '往来票据单'"
					    + " ) pay_amount,"
					    + " ( aio.pay_amount - ( SELECT ifnull(sum(caor.pay_amount), 0) "
					    + " FROM cost_application_order_rel caor"
					    + " WHERE caor.cost_order_id = aio.id"
					    + " AND caor.order_type = '往来票据单' ) ) yufu_amount, aciao.id app_id"
					    + " FROM arap_in_out_misc_order aio"
					    + " LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = aio.id"
						+ " LEFT JOIN arap_cost_invoice_application_order aciao on aciao.id = caor.application_order_id"
					    + " LEFT JOIN user_login ul ON ul.id = aio.creator_id"
					    + " where caor.order_type = '往来票据单'"
						+ " union"
					    + " SELECT dor.id, dor.customer_id payee_id,'' payee_name, dor.order_no, '货损单' order_type,"
					    + " dofi.status STATUS, dofi.remark, dor.create_date create_stamp, "
					    + " (case when dofi.party_type ='客户' "
					    + " then c.abbr "
			            + " else"
			            + " dofi.party_name"
			            + " end) cname,"
					    + " ifnull(ul.c_name, ul.user_name) creator_name,"
					    + " sum(ifnull(dofi.amount,0)) cost_amount,"
					    + " ( SELECT ifnull(sum(caor.pay_amount), 0)"
					    + " FROM cost_application_order_rel caor"
					    + " WHERE "
					    + " caor.cost_order_id = dor.id and caor.application_order_id = aciao.id"
					    + " AND caor.order_type = '货损单'"
					    + " ) pay_amount,"
					    + " (sum(ifnull(dofi.amount,0)) - ( SELECT ifnull(sum(caor.pay_amount), 0) "
					    + " FROM cost_application_order_rel caor"
					    + " WHERE caor.cost_order_id = dor.id"
					    + " AND caor.order_type = '货损单' AND dofi.party_name = caor.payee_unit)) yufu_amount, aciao.id app_id"
					    + " FROM damage_order dor"
					    + " LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = dor.id"
					    + " LEFT JOIN damage_order_fin_item dofi on dofi.order_id = dor.id "
					    + " and dofi.party_name = caor.payee_unit and dofi.status = '已确认' and dofi.type = 'cost'"
					    //+ " and dofi.id in(caor.item_ids)"
						+ " LEFT JOIN arap_cost_invoice_application_order aciao on aciao.id = caor.application_order_id"
					    + " LEFT JOIN user_login ul ON ul.id = dor.creator"
					    + " left join party p on p.id = dor.customer_id "
					    + " left join contact c on c.id = p.contact_id "
					    + " where caor.order_type = '货损单'"
					    + " GROUP BY caor.application_order_id"
						+ " ) A where app_id ="+application_id ;
			}
			
			Map BillingOrderListMap = new HashMap();
			List<Record> recordList= Db.find(sql);
	        BillingOrderListMap.put("iTotalRecords", recordList.size());
	        BillingOrderListMap.put("iTotalDisplayRecords", recordList.size());
	        BillingOrderListMap.put("aaData", recordList);

	        renderJson(BillingOrderListMap);
		}
		
		
		
		@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_UPDATE})
		public void edit() throws ParseException {
			String id = getPara("id");
			setAttr("application_id", id);
			
			ArapCostInvoiceApplication arapAuditInvoiceApplication = ArapCostInvoiceApplication.dao.findById(id);
			setAttr("invoiceApplication", arapAuditInvoiceApplication);
			
			Contact con  = Contact.dao.findFirst("select * from contact c left join party p on c.id = p.contact_id where p.id =?",arapAuditInvoiceApplication.get("payee_id"));
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
			
			render("/yh/arap/CostAcceptOrder/payEdit.html");
		}
		
		
		//复核
		@Before(Tx.class)
	    public void checkStatus(){
	        String application_id=getPara("application_id");
	        
	        ArapCostInvoiceApplication arapCostInvoiceApplication = ArapCostInvoiceApplication.dao.findById(application_id);
	        arapCostInvoiceApplication.set("status", "已复核");
	        arapCostInvoiceApplication.set("check_by", LoginUserController.getLoginUserId(this));
	        arapCostInvoiceApplication.set("check_stamp", new Date()).update();
	        
	        
	        //更改原始单据状态
	        String strJson = getPara("detailJson");
			Gson gson = new Gson();
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String id = (String)map.get("id");
				String order_type = (String)map.get("order_type");

				
				if(order_type.equals("对账单")){
					ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
					Double total_amount = arapCostOrder.getDouble("cost_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '对账单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapCostOrder.set("status", "部分已复核").update();
					}else
						arapCostOrder.set("status", "已复核").update();
				}else if(order_type.equals("成本单")){
					ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(id);
					
					Double total_amount = arapMiscCostOrder.getDouble("total_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '成本单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapMiscCostOrder.set("audit_status", "部分已复核").update();
					}else
						arapMiscCostOrder.set("audit_status", "已复核").update();
										
				}else if(order_type.equals("行车单")){
					CarSummaryOrder carSummaryOrder = CarSummaryOrder.dao.findById(id);
					
					Double total_amount = carSummaryOrder.getDouble("actual_payment_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '行车单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						carSummaryOrder.set("status", "部分已复核").update();
					}else
						carSummaryOrder.set("status", "已复核").update();
					
				}else if(order_type.equals("预付单")){
					ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao.findById(id);
					
					Double total_amount = arapPrePayOrder.getDouble("total_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '预付单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapPrePayOrder.set("status", "部分已复核").update();
					}else
						arapPrePayOrder.set("status", "已复核").update();
					
				}else if(order_type.equals("报销单")){
					ReimbursementOrder reimbursementOrder = ReimbursementOrder.dao.findById(id);
					Double total_amount = reimbursementOrder.getDouble("amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '报销单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						reimbursementOrder.set("status", "部分已复核").update();
					}else
						reimbursementOrder.set("status", "已复核").update();
					
				}else if(order_type.equals("往来票据单")){
					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
					Double total_amount = arapInOutMiscOrder.getDouble("pay_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '往来票据单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapInOutMiscOrder.set("pay_status", "部分已复核").update();
					}else
						arapInOutMiscOrder.set("pay_status", "已复核").update();
				}			
			}
			
			Record r = arapCostInvoiceApplication.toRecord();
			String check_name = LoginUserController.getUserNameById(arapCostInvoiceApplication.getLong("check_by").toString());
			r.set("check_name", check_name);
			renderJson(r);
	    }
		
		//退回
		@Before(Tx.class)
	    public void returnOrder(){
	        String application_id=getPara("application_id");
	        
	        ArapCostInvoiceApplication arapCostInvoiceApplication = ArapCostInvoiceApplication.dao.findById(application_id);
	        arapCostInvoiceApplication.set("status", "新建");
	        arapCostInvoiceApplication.set("return_by", LoginUserController.getLoginUserId(this));
	        arapCostInvoiceApplication.set("return_stamp", new Date()).update();
	        
	        //更改原始单据状态
	        String strJson = getPara("detailJson");
			Gson gson = new Gson();
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String id = (String)map.get("id");
				String order_type = (String)map.get("order_type");

				
				if(order_type.equals("对账单")){
					ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
					arapCostOrder.set("status", "付款申请中").update();
				}else if(order_type.equals("成本单")){
					ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(id);
					arapMiscCostOrder.set("audit_status", "付款申请中").update();
				}else if(order_type.equals("行车单")){
					CarSummaryOrder carSummaryOrder = CarSummaryOrder.dao.findById(id);
					carSummaryOrder.set("status", "付款申请中").update();
				}else if(order_type.equals("预付单")){
					ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao.findById(id);
					arapPrePayOrder.set("status", "付款申请中").update();
				}else if(order_type.equals("报销单")){
					ReimbursementOrder reimbursementOrder = ReimbursementOrder.dao.findById(id);
					reimbursementOrder.set("status", "付款申请中").update();
				}else if(order_type.equals("往来票据单")){
					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
					arapInOutMiscOrder.set("pay_status", "付款申请中").update();
				}
			}
			renderJson("{\"success\":true}");
	    }
		
		
		
		//撤销申请单据
		@Before(Tx.class)
	    public void deleteOrder(){
			//先更改对应的单据状态
			//删除从表数据
			//删除主单据数据
			
	        String application_id=getPara("application_id");
	        //删除从表数据
	        String sql = "select * from cost_application_order_rel "
					+ " where application_order_id = '"+application_id+"'";
			List<CostApplicationOrderRel> rel = CostApplicationOrderRel.dao.find(sql);
			for(CostApplicationOrderRel crel:rel){
				long id = crel.getLong("cost_order_id");
				String order_type = crel.getStr("order_type");
				
				//修改相关单据状态
				if(order_type.equals("对账单")){
					ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
					arapCostOrder.set("status", "已确认").update();
				}else if(order_type.equals("成本单")){
					ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(id);
					arapMiscCostOrder.set("audit_status", "新建").update();
				}else if(order_type.equals("行车单")){
					CarSummaryOrder carSummaryOrder = CarSummaryOrder.dao.findById(id);
					carSummaryOrder.set("status", "已审核").update();
				}else if(order_type.equals("预付单")){
					ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao.findById(id);
					arapPrePayOrder.set("status", "新建").update();
				}else if(order_type.equals("报销单")){
					ReimbursementOrder reimbursementOrder = ReimbursementOrder.dao.findById(id);
					reimbursementOrder.set("status", "新建").update();
				}else if(order_type.equals("往来票据单")){
					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
					arapInOutMiscOrder.set("pay_status", "未付").update();
				}
				
				//删除从表数据
				crel.delete();
			}
			
			//删除主表数据
			ArapCostInvoiceApplication arapCostInvoiceApplication = ArapCostInvoiceApplication.dao.findById(application_id);
	        arapCostInvoiceApplication.delete();
		        
			
			renderJson("{\"success\":true}");
	    }
		
		
	    
		//付款确认
		@Before(Tx.class)
	    public void confirmOrder(){
	        String application_id=getPara("application_id");
	        String pay_type = getPara("pay_type");
	        String pay_bank_id = getPara("pay_bank");
	        String pay_time = getPara("pay_time");
	        
	        if( pay_time==null||pay_time.equals("")){
	   			pay_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	   		}
	        
	        ArapCostInvoiceApplication arapCostInvoiceApplication = ArapCostInvoiceApplication.dao.findById(application_id);
	        String pay_amount = arapCostInvoiceApplication.getDouble("total_amount").toString();
	        arapCostInvoiceApplication.set("status", "已付款");
	        arapCostInvoiceApplication.set("pay_type", pay_type);
	        if(pay_bank_id != null && !pay_bank_id.equals(""))
	        	arapCostInvoiceApplication.set("confirm_bank_id", pay_bank_id);
	        else{
	        	arapCostInvoiceApplication.set("confirm_bank_id", 4);
	        }
	        if(pay_time==null || pay_time.equals(""))
	        	arapCostInvoiceApplication.set("pay_time", new Date());
	        else
	        	arapCostInvoiceApplication.set("pay_time", pay_time);
	        arapCostInvoiceApplication.set("confirm_by", LoginUserController.getLoginUserId(this));
	        arapCostInvoiceApplication.set("confirm_stamp", new Date());
	        arapCostInvoiceApplication.set("confirm_by", LoginUserController.getLoginUserId(this));
	        arapCostInvoiceApplication.set("confirm_stamp", new Date());
	        arapCostInvoiceApplication.update();
	        
	        //更改原始单据状态
	        String strJson = getPara("detailJson");
			Gson gson = new Gson();
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String id = (String)map.get("id");
				String order_type = (String)map.get("order_type");

				if(order_type.equals("对账单")){
					ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
					Double total_amount = arapCostOrder.getDouble("cost_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '对账单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapCostOrder.set("status", "部分已付款").update();
					}else
						arapCostOrder.set("status", "已付款").update();
				}else if(order_type.equals("成本单")){
					ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(id);
					
					Double total_amount = arapMiscCostOrder.getDouble("total_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '成本单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapMiscCostOrder.set("audit_status", "部分已付款").update();
					}else
						arapMiscCostOrder.set("audit_status", "已付款").update();
										
				}else if(order_type.equals("行车单")){
					CarSummaryOrder carSummaryOrder = CarSummaryOrder.dao.findById(id);
					
					Double total_amount = carSummaryOrder.getDouble("actual_payment_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '行车单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						carSummaryOrder.set("status", "部分已付款").update();
					}else
						carSummaryOrder.set("status", "已付款").update();
					
				}else if(order_type.equals("预付单")){
					ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao.findById(id);
					
					Double total_amount = arapPrePayOrder.getDouble("total_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '预付单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapPrePayOrder.set("status", "部分已付款").update();
					}else
						arapPrePayOrder.set("status", "已付款").update();
					
				}else if(order_type.equals("报销单")){
					ReimbursementOrder reimbursementOrder = ReimbursementOrder.dao.findById(id);
					Double total_amount = reimbursementOrder.getDouble("amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '报销单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						reimbursementOrder.set("status", "部分已付款").update();
					}else
						reimbursementOrder.set("status", "已付款").update();
					
				}else if(order_type.equals("往来票据单")){
					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
					
					Double total_amount = arapInOutMiscOrder.getDouble("pay_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '往来票据单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapInOutMiscOrder.set("pay_status", "部分已付款").update();
					}else
						arapInOutMiscOrder.set("pay_status", "已付款").update();
				} else if(order_type.equals("货损单")){
					DamageOrder damageOrder = DamageOrder.dao.findById(id);
					Record rec = Db.findFirst("select sum(ifnull(amount,0)) total_amount from damage_order_fin_item dof where dof.order_id = ?",id);
					Double total_amount = rec.getDouble("total_amount");
					Record re = Db.findFirst("select ifnull(sum(pay_amount),0) total from cost_application_order_rel cao"
							+ "  LEFT JOIN arap_cost_invoice_application_order acia on acia.id = cao.application_order_id"
							+ "  where cost_order_id =? and order_type = '货损单' and acia.`STATUS`='已付款'",id);
					Record re2 = Db.findFirst("select ifnull(sum(receive_amount),0) total2 from charge_application_order_rel cao"
							+ " LEFT JOIN arap_charge_invoice_application_order acia on acia.id = cao.application_order_id "
							+ " where charge_order_id =? and order_type = '货损单' and acia.`STATUS`='已收款'",id);
					Double paid_amount = re.getDouble("total");
					Double receive_amount = re2.getDouble("total2");
					Double total = paid_amount + receive_amount;
					if(total_amount.equals(total)){
						if(!damageOrder.getStr("status").equals("已结案"))
							damageOrder.set("status", "已完成").update();
					}
				}
			}
	        
	        
	        
	      //新建日记账表数据
			 ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
	        auditLog.set("payment_method", pay_type);
	        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_COST);
	        auditLog.set("amount", pay_amount);
	        auditLog.set("creator", LoginUserController.getLoginUserId(this));
	        auditLog.set("create_date", pay_time);
	        if(pay_bank_id!=null && !pay_bank_id.equals("") )
	        	auditLog.set("account_id", pay_bank_id);
	        else
	        	auditLog.set("account_id", 4);
	        auditLog.set("source_order", "应付开票申请单");
	        auditLog.set("invoice_order_id", application_id);
	        auditLog.save();
	                
	        renderJson("{\"success\":true}");  
	    }
		
		
		
		//付款确认退回
		//同时更新日记账里面的数据（退回金额）
		@Before(Tx.class)
	    public void returnConfirmOrder(){
	        String application_id=getPara("application_id");
	        
	        ArapCostInvoiceApplication arapCostInvoiceApplication = ArapCostInvoiceApplication.dao.findById(application_id);
	        arapCostInvoiceApplication.set("status", "已复核");
	        arapCostInvoiceApplication.set("return_confirm_by", LoginUserController.getLoginUserId(this));
	        arapCostInvoiceApplication.set("return_confirm_stamp", new Date());
	        arapCostInvoiceApplication.update();
	       
	        //更改原始单据状态
	        String strJson = getPara("detailJson");
			Gson gson = new Gson();
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String id = (String)map.get("id");
				String order_type = (String)map.get("order_type");

				if(order_type.equals("对账单")){
					ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
					Double total_amount = arapCostOrder.getDouble("cost_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '对账单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapCostOrder.set("status", "部分已复核").update();
					}else
						arapCostOrder.set("status", "已复核").update();
				}else if(order_type.equals("成本单")){
					ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(id);
					
					Double total_amount = arapMiscCostOrder.getDouble("total_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '成本单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapMiscCostOrder.set("audit_status", "部分已复核").update();
					}else
						arapMiscCostOrder.set("audit_status", "已复核").update();
										
				}else if(order_type.equals("行车单")){
					CarSummaryOrder carSummaryOrder = CarSummaryOrder.dao.findById(id);
					
					Double total_amount = carSummaryOrder.getDouble("actual_payment_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '行车单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						carSummaryOrder.set("status", "部分已复核").update();
					}else
						carSummaryOrder.set("status", "已复核").update();
					
				}else if(order_type.equals("预付单")){
					ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao.findById(id);
					
					Double total_amount = arapPrePayOrder.getDouble("total_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '预付单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapPrePayOrder.set("status", "部分已复核").update();
					}else
						arapPrePayOrder.set("status", "已复核").update();
					
				}else if(order_type.equals("报销单")){
					ReimbursementOrder reimbursementOrder = ReimbursementOrder.dao.findById(id);
					Double total_amount = reimbursementOrder.getDouble("amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '报销单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						reimbursementOrder.set("status", "部分已复核").update();
					}else
						reimbursementOrder.set("status", "已复核").update();
					
				}else if(order_type.equals("往来票据单")){
					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
					
					Double total_amount = arapInOutMiscOrder.getDouble("pay_amount");
					Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '往来票据单'",id);
					Double paid_amount = re.getDouble("total");
					if(!total_amount.equals(paid_amount)){
						arapInOutMiscOrder.set("pay_status", "部分已复核").update();
					}else
						arapInOutMiscOrder.set("pay_status", "已复核").update();
					
				}else if(order_type.equals("货损单")){
					DamageOrder damageOrder = DamageOrder.dao.findById(id);
					if(!damageOrder.getStr("status").equals("已结案")) 
						damageOrder.set("status", "单据处理中").update();
				}
			}
			
			
			//撤销对应日记账信息
			ArapAccountAuditLog arapAccountAuditLog = ArapAccountAuditLog
					.dao.findFirst("select * from arap_account_audit_log where source_order = '应付开票申请单' and payment_type = 'COST' and invoice_order_id = ? ",application_id);
			arapAccountAuditLog.delete();
  
			renderJson("{\"success\":true}");
	        
	    }
	
}
