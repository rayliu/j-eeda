package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapChargeInvoiceApplication;
import models.ArapCostInvoiceApplication;
import models.ArapCostInvoiceItemInvoiceNo;
import models.ArapCostOrder;
import models.ArapCostOrderInvoiceNo;
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

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostPreInvoiceOrderController extends Controller {
    private Logger logger = Logger.getLogger(CostPreInvoiceOrderController.class);
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

        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
        setAttr("customer", contact);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
        render("/yh/arap/ChargeAcceptOrder/ChargeCheckOrderEdit.html");
    }

    // 应付条目列表
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_LIST})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_charge_invoice_application_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aaia.*,ul.user_name create_by,ul2.user_name audit_by,ul3.user_name approval_by from arap_cost_invoice_application_order aaia "
				+ " left join user_login ul on ul.id = aaia.create_by"
				+ " left join user_login ul2 on ul2.id = aaia.audit_by"
				+ " left join user_login ul3 on ul3.id = aaia.approver_by order by aaia.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CREATE})
	public void create() {
		String ids = getPara("ids");
		String[] idArray = ids.split(",");
		logger.debug(String.valueOf(idArray.length));

		setAttr("costCheckOrderIds", ids);
		/*String beginTime = getPara("beginTime");
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
		}*/

		String order_no = null;
		setAttr("saveOK", false);
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));

		ArapChargeInvoiceApplication order = ArapChargeInvoiceApplication.dao
				.findFirst("select * from arap_charge_invoice_application_order order by order_no desc limit 0,1");
		if (order != null) {
			String num = order.get("order_no");
            // TODO num.substring(2, num.length()); 该方法不通用
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
			setAttr("order_no", "YFSQ" + order_no);
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String format = sdf.format(new Date());
			order_no = format + "00001";
			setAttr("order_no", "YFSQ" + order_no);
		}

		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		setAttr("status", "new");
			render("/yh/arap/CostPreInvoiceOrder/CostPreInvoiceOrderEdit.html");
	}
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CREATE, PermissionConstant.PERMSSION_CPO_UPDATE}, logical=Logical.OR)
	public void save() {
		ArapCostInvoiceApplication arapAuditInvoiceApplication = null;
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		String paymentMethod = getPara("paymentMethod");
		if (!"".equals(costPreInvoiceOrderId) && costPreInvoiceOrderId != null) {
			arapAuditInvoiceApplication = ArapCostInvoiceApplication.dao.findById(costPreInvoiceOrderId);
			arapAuditInvoiceApplication.set("order_no", getPara("order_no"));
			// arapAuditOrder.set("order_type", );
			arapAuditInvoiceApplication.set("status", "new");
			//arapAuditInvoiceApplication.set("payee_id", getPara("customer_id"));
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
			arapAuditInvoiceApplication = new ArapCostInvoiceApplication();
			arapAuditInvoiceApplication.set("order_no", getPara("order_no"));
			arapAuditInvoiceApplication.set("status", "新建");
			//arapAuditInvoiceApplication.set("payee_id", getPara("customer_id"));
			arapAuditInvoiceApplication.set("create_by", getPara("create_by"));
			arapAuditInvoiceApplication.set("create_stamp", new Date());
			arapAuditInvoiceApplication.set("remark", getPara("remark"));
			arapAuditInvoiceApplication.set("payment_method", getPara("paymentMethod"));
			if("transfers".equals(paymentMethod)){
				if(getPara("accountTypeSelect") != null && !"".equals(getPara("accountTypeSelect"))){
					arapAuditInvoiceApplication.set("account_id", getPara("accountTypeSelect"));
				}
			}else{
				arapAuditInvoiceApplication.set("account_id", null);				
			}
			arapAuditInvoiceApplication.save();

			String costCheckOrderIds = getPara("costCheckOrderIds");
			String[] costCheckOrderIdsArr = costCheckOrderIds.split(",");
			for (int i = 0; i < costCheckOrderIdsArr.length; i++) {
				ArapCostOrder arapAuditOrder = ArapCostOrder.dao.findById(costCheckOrderIdsArr[i]);
				arapAuditOrder.set("application_order_id", arapAuditInvoiceApplication.get("id"));
				arapAuditOrder.update();
			}
		}
		renderJson(arapAuditInvoiceApplication);;
	}
	
	// 审核

@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_APPROVAL})
	public void auditCostPreInvoiceOrder(){
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		if(costPreInvoiceOrderId != null && !"".equals(costPreInvoiceOrderId)){
			ArapCostInvoiceApplication arapAuditOrder = ArapCostInvoiceApplication.dao.findById(costPreInvoiceOrderId);
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
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CONFIRMATION})
	public void approvalCostPreInvoiceOrder(){
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		if(costPreInvoiceOrderId != null && !"".equals(costPreInvoiceOrderId)){
			ArapCostInvoiceApplication arapAuditOrder = ArapCostInvoiceApplication.dao.findById(costPreInvoiceOrderId);
			arapAuditOrder.set("status", "已审批");
            String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
			arapAuditOrder.set("approver_by", users.get(0).get("id"));
			arapAuditOrder.set("approval_stamp", new Date());
			arapAuditOrder.update();
		}
		renderJson("{\"success\":true}");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_UPDATE})
	public void edit() throws ParseException {
		String id = getPara("id");
		ArapCostInvoiceApplication arapAuditInvoiceApplication = ArapCostInvoiceApplication.dao.findById(id);
		/*String customerId = arapAuditInvoiceApplication.get("payee_id");
		if (!"".equals(customerId) && customerId != null) {
			Party party = Party.dao.findById(customerId);
			setAttr("party", party);
			Contact contact = Contact.dao.findById(party.get("contact_id")
					.toString());
			setAttr("customer", contact);
		}*/
		UserLogin userLogin = UserLogin.dao.findById(arapAuditInvoiceApplication.get("create_by"));
		setAttr("userLogin", userLogin);
		setAttr("arapAuditInvoiceApplication", arapAuditInvoiceApplication);

		String costCheckOrderIds = "";
		List<ArapCostOrder> arapCostOrders = ArapCostOrder.dao.find("select * from arap_cost_order where application_order_id = ?", id);
		for(ArapCostOrder arapCostOrder : arapCostOrders){
			costCheckOrderIds += arapCostOrder.get("id") + ",";
		}
		costCheckOrderIds = costCheckOrderIds.substring(0, costCheckOrderIds.length() - 1);
		setAttr("costCheckOrderIds", costCheckOrderIds);
			render("/yh/arap/CostPreInvoiceOrder/CostPreInvoiceOrderEdit.html");
	}
	
    // 添加发票
    public void addInvoiceItem(){
    	String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
    	if(costPreInvoiceOrderId != null && !"".equals(costPreInvoiceOrderId)){
    		ArapCostInvoiceItemInvoiceNo arapCostInvoiceItemInvoiceNo = new ArapCostInvoiceItemInvoiceNo();
    		arapCostInvoiceItemInvoiceNo.set("invoice_id", costPreInvoiceOrderId);
    		arapCostInvoiceItemInvoiceNo.save();
    	}
        renderJson("{\"success\":true}");
    }
    
    // 发票号列表
    public void costInvoiceItemList(){
    	String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from arap_cost_invoice_item_invoice_no acio"
				+ " left join arap_cost_order_invoice_no  acoi on acoi.invoice_no = acio.invoice_no"
				+ " where acio.invoice_id = " + costPreInvoiceOrderId;
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select acio.*,(select group_concat(aco.order_no separator '\r\n') from arap_cost_order aco where aco.id = acoi.cost_order_id) cost_order_no "
        		+ " from arap_cost_invoice_item_invoice_no acio"
				+ " left join arap_cost_order_invoice_no  acoi on acoi.invoice_no = acio.invoice_no"
				+ " where acio.invoice_id = " + costPreInvoiceOrderId + " " + sLimit);

        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }
    
    // 更新InvoiceItem信息
    public void updateInvoiceItem(){
    	List<ArapCostInvoiceItemInvoiceNo> arapCostInvoiceItemInvoiceNos = new ArrayList<ArapCostInvoiceItemInvoiceNo>();
    	ArapCostInvoiceItemInvoiceNo itemInvoiceNo = ArapCostInvoiceItemInvoiceNo.dao.findById(getPara("invoiceItemId"));
    	String name = getPara("name");
    	String value = getPara("value");
    	if("amount".equals(name) && "".equals(value)){
    		value = "0";
    	}
    	itemInvoiceNo.set(name, value);
    	itemInvoiceNo.update();
    	if("invoice_no".equals(name) && !"".equals(value)){
    		arapCostInvoiceItemInvoiceNos = ArapCostInvoiceItemInvoiceNo.dao.find("select * from arap_cost_invoice_item_invoice_no where invoice_id = ?", getPara("costPreInvoiceOrderId"));    		
    	}
        renderJson(arapCostInvoiceItemInvoiceNos);
    }
    
    // 获取所有的发票号
    public void findAllInvoiceItemNo(){
    	List<ArapCostInvoiceItemInvoiceNo> arapCostInvoiceItemInvoiceNos = ArapCostInvoiceItemInvoiceNo.dao.find("select * from arap_cost_invoice_item_invoice_no where invoice_id = ?", getPara("costPreInvoiceOrderId"));
        renderJson(arapCostInvoiceItemInvoiceNos);
    }
    
    // 更新开票申请单
    public void updatePreInvoice(){
    	String name = getPara("name");
    	
    	String[] values = null;
    	Map<String,String[]> map = getParaMap();
    	for(Map.Entry<String,String[]> entry : map.entrySet()){
    		if("value[]".equals(entry.getKey())){
    			values = entry.getValue();
    		}
    	}
    	String costCheckOrderId = getPara("costCheckOrderId");
    	List<ArapCostOrderInvoiceNo> arapCostOrderInvoiceNos = ArapCostOrderInvoiceNo.dao.find("select * from arap_cost_order_invoice_no where cost_order_id = ?", costCheckOrderId);
    	for(ArapCostOrderInvoiceNo arapCostOrderInvoiceNo : arapCostOrderInvoiceNos){
    		arapCostOrderInvoiceNo.delete();
    	}
    	for(int i=0;i<values.length;i++){
    		ArapCostOrderInvoiceNo arapCostOrderInvoiceNo = ArapCostOrderInvoiceNo.dao.findFirst("select * from arap_cost_order_invoice_no where cost_order_id = ? and invoice_no = ?", costCheckOrderId, values[i]);
	    	if(arapCostOrderInvoiceNo == null){
	    		arapCostOrderInvoiceNo = new ArapCostOrderInvoiceNo();
	    		arapCostOrderInvoiceNo.set(name, values[i]);
	    		arapCostOrderInvoiceNo.set("cost_order_id", costCheckOrderId);
	    		arapCostOrderInvoiceNo.save();
	    	}
    	}
        renderJson("{\"success\":true}");
    }
    
    public void costCheckOrderList(){
    	String costCheckOrderIds = getPara("costCheckOrderIds");
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_cost_order where id in("+costCheckOrderIds+")";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aco.*,(select group_concat(acai.invoice_no) from arap_cost_order aaia"
				+ " left join arap_cost_order_invoice_no acai on acai.cost_order_id = aaia.id) invoice_no from arap_cost_order aco where aco.id in("+costCheckOrderIds+") order by aco.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }

	public void searchAllAccount(){
		List<Account> accounts = Account.dao.find("select * from fin_account where type != 'REC'");
		renderJson(accounts);
	}
}
