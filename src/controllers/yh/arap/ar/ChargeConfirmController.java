package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapAccountAuditLog;
import models.ArapChargeInvoice;
import models.ArapChargeInvoiceApplication;
import models.ArapChargeReceiveConfirmOrder;
import models.ArapChargeReceiveConfirmOrderDtail;
import models.ArapChargeReceiveConfirmOrderDtailLog;
import models.ArapChargeReceiveConfirmOrderLog;
import models.CostApplicationOrderRel;
import models.DepartOrder;
import models.InsuranceOrder;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
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
public class ChargeConfirmController extends Controller {
    private Logger logger = Logger.getLogger(ChargeConfirmController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CONFIRMATION})
    public void index() {
    	render("/yh/arap/ChargeConfirm/ChargeConfirmList.html");
    }
    
   	public void edit() {
   		String id = getPara("id");
   		String order_type = "";
   		setAttr("confirmId", id);
   		
   		List<ArapChargeReceiveConfirmOrderDtail> list = ArapChargeReceiveConfirmOrderDtail.dao.find("select * from arap_charge_receive_confirm_order_detail where order_id in(?)",id);
   		for(ArapChargeReceiveConfirmOrderDtail a: list){
   			if(a.getLong("invoice_order_id")!=null){
   				order_type = "开票记录单";
   				setAttr("order_type", "开票记录单");
   			}else if(a.getLong("misc_cost_order_id")!=null){
   				order_type = "成本单";
   				setAttr("order_type", "成本单");
   			}
   		}
   		//setAttr("order_type", order_type);
   		ArapChargeReceiveConfirmOrder arapChargeReceiveConfirmOrder = ArapChargeReceiveConfirmOrder.dao.findById(id);
   		setAttr("arapChargeReceiveConfirmOrder", arapChargeReceiveConfirmOrder);
   		
   		if(arapChargeReceiveConfirmOrder.getLong("sp_id")!= null){
	   		String sql1 = "select * from  party p LEFT JOIN office o ON o.id = p.office_id where p.id = '"+arapChargeReceiveConfirmOrder.getLong("sp_id")+"'";
	   		Record re1 = Db.findFirst(sql1);
	   		setAttr("abbr", re1.getStr("abbr"));
   		}
   		String sql = "";
   		if(order_type.equals("成本单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(misc_cost_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_cost_pay_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_cost_pay_confirm_order acp WHERE acp.id = '"+id+"'";
   		}else if(order_type.equals("开票记录单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(invoice_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_charge_receive_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_charge_receive_confirm_order acp WHERE acp.id = '"+id+"'";
   		}
   		
		Record re = Db.findFirst(sql);
		setAttr("orderIds", re.get("ids"));
		
		
		//获取已付款金额
   		String sql2 = "select sum(acp.amount) total from arap_charge_receive_confirm_order_log acp "
				+ "  where acp.order_id = '"+id+"'";
		Record re2 = Db.findFirst(sql2);
		setAttr("total_receive", re2.get("total"));

   		render("/yh/arap/ChargeConfirm/ChargeConfirmEdit.html");
   	}
   	
   	
	@SuppressWarnings("null")
	@Before(Tx.class)
	public void save() {
   		String receive_type = getPara("receive_type");
   		String receive_bank = getPara("receive_bank");
   		String receive_account_no = getPara("receive_account_no");
   		String receive_amount = getPara("receive_amount");
   		String noreceive_amount = getPara("noreceive_amount");
		
   		ArapChargeReceiveConfirmOrder arapChargeReceiveConfirmOrder = null;
		Record re = null;
   		String confirmId = getPara("confirmId");
   		String orderIds = getPara("orderIds");
   		
   		String sp_filter = getPara("sp_filter");       //供应商
   		String payee_unit = getPara("payee_unit");    //收款单位
   		String account_name = getPara("account_name");//账户人名
   		String invoice_type = getPara("invoice_type");//开票类型
   		String payee_name = getPara("payee_name");    //收款人
   		String deposit_bank = getPara("deposit_bank");//开户行
   		String billing_unit = getPara("billing_unit"); //开票单位
   		String bank_no = getPara("bank_no");   
   		String order_type = getPara("order_type"); //单据类型
   		
   		if(confirmId != ""&& confirmId != null){
			//更新主表
   			arapChargeReceiveConfirmOrder = ArapChargeReceiveConfirmOrder.dao.findById(confirmId);
   			arapChargeReceiveConfirmOrder.set("last_updator", LoginUserController.getLoginUserId(this));
   			arapChargeReceiveConfirmOrder.set("last_update_date", new Date()).update();				
		} else {
			Contact contact = Contact.dao.findFirst("select * from contact where abbr = '"+sp_filter+"'");
			String name = (String) currentUser.getPrincipal();
	        Long userId =  LoginUserController.getLoginUserId(this);
	      //创建主表
	        arapChargeReceiveConfirmOrder = new ArapChargeReceiveConfirmOrder();
	        arapChargeReceiveConfirmOrder.set("order_no", OrderNoGenerator.getNextOrderNo("YSQR"));
			if(sp_filter!=null&&!sp_filter.equals("")){
				arapChargeReceiveConfirmOrder.set("sp_id",contact.getLong("id"));
			}
			arapChargeReceiveConfirmOrder.set("status", "新建");
//			arapChargeReceiveConfirmOrder.set("invoice_type",invoice_type);
//			arapChargeReceiveConfirmOrder.set("invoice_company", billing_unit);
//			arapChargeReceiveConfirmOrder.set("receive_company", payee_unit);
//			arapChargeReceiveConfirmOrder.set("receive_person", payee_name);
//			arapChargeReceiveConfirmOrder.set("receive_bank", deposit_bank);
//			arapChargeReceiveConfirmOrder.set("receive_bank_person_name", account_name);//账户人名
//			arapChargeReceiveConfirmOrder.set("receive_account_no",bank_no);
			arapChargeReceiveConfirmOrder.set("creator",userId);
			arapChargeReceiveConfirmOrder.set("create_date",new Date());
			arapChargeReceiveConfirmOrder.save();		
			
			//新建字表
			String[] idArray = orderIds.split(",");
			for (int i = 0; i < idArray.length; i++) {
				ArapChargeReceiveConfirmOrderDtail arapChargeReceiveConfirmOrderDtail =new ArapChargeReceiveConfirmOrderDtail();
				arapChargeReceiveConfirmOrderDtail.set("order_id", arapChargeReceiveConfirmOrder.getLong("id"));
				if(order_type.equals("成本单")){
					arapChargeReceiveConfirmOrderDtail.set("misc_cost_order_id", idArray[i]);
				}else if(order_type.equals("开票记录单")){
					arapChargeReceiveConfirmOrderDtail.set("invoice_order_id", idArray[i]);
				}
				arapChargeReceiveConfirmOrderDtail.save();
				
				//更新开票记录单状态
				if(order_type.equals("成本单")){
					ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(idArray[i]);
					arapMiscCostOrder.set("status", "收款确认中").update();
				}else if(order_type.equals("开票记录单")){
					ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(idArray[i]);
					arapChargeInvoice.set("status", "收款确认中").update();
					List<ArapChargeInvoiceApplication> chargeOrderList = ArapChargeInvoiceApplication.dao.find("select * from arap_charge_invoice_application_order where invoice_order_id = ?",idArray[i]);
					for(ArapChargeInvoiceApplication arapChargeInvoiceApplication : chargeOrderList){
						arapChargeInvoiceApplication.set("status", "收款确认中").update();
			    	}
				}
			}
		}
   		renderJson(arapChargeReceiveConfirmOrder);
   	}
	
	@Before(Tx.class)
	public void saveConfirmLog(){
		String confirmId = getPara("confirmId");
		String receive_type = getPara("receive_type");
   		String receive_bank = getPara("receive_bank");
   		String receive_account_no = getPara("receive_account_no");
   		String receive_amount = getPara("receive_amount");
   		String noreceive_amount = getPara("noreceive_amount");
   		String total_amount = getPara("total_amount");
   		String orderIds = getPara("orderIds");
   		String order_type = getPara("order_type"); //单据类型
   		String detailJson = getPara("detailJson");
   		String applicationId = "";
   		String value = "";
   		String sql = "";
   		Gson gson = new Gson();
		List<LinkedTreeMap> list = gson.fromJson(detailJson, new TypeToken<List<LinkedTreeMap>>(){}.getType());
		for (Map obj: list) {
			logger.debug(obj.get("id").toString());
			logger.debug(obj.get("value").toString());
			applicationId = obj.get("id").toString();
			value = obj.get("value").toString();
			if(order_type.equals("成本单")){
				sql = "select * from arap_charge_receive_confirm_order_detail where misc_cost_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}else if(order_type.equals("开票记录单")){
				sql = "select * from arap_charge_receive_confirm_order_detail where invoice_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}
			
			ArapChargeReceiveConfirmOrderDtail detail = ArapChargeReceiveConfirmOrderDtail.dao.findFirst(sql);
			ArapChargeReceiveConfirmOrderDtailLog arapChargeReceiveConfirmOrderDtailLog = new ArapChargeReceiveConfirmOrderDtailLog();
			arapChargeReceiveConfirmOrderDtailLog.set("order_id", confirmId);
			arapChargeReceiveConfirmOrderDtailLog.set("detail_id", detail.getLong("id"));
			arapChargeReceiveConfirmOrderDtailLog.set("receive_amount", value);
			arapChargeReceiveConfirmOrderDtailLog.set("create_date", new Date());
			arapChargeReceiveConfirmOrderDtailLog.save();
		}
		
		ArapChargeReceiveConfirmOrderLog arapChargeReceiveConfirmOrderLog = null;
   		Account account = Account.dao.findFirst("select * from fin_account where account_no = '"+receive_account_no+"'");
		//创建付款LOG记录表
   		arapChargeReceiveConfirmOrderLog = new ArapChargeReceiveConfirmOrderLog();
		if(account!=null)
			arapChargeReceiveConfirmOrderLog.set("pay_in_bank_id", account.getLong("id"));
		arapChargeReceiveConfirmOrderLog.set("receive_type", receive_type);
		arapChargeReceiveConfirmOrderLog.set("receive_in_bank_name", receive_bank);
		arapChargeReceiveConfirmOrderLog.set("receive_in_account_no", receive_account_no);
		arapChargeReceiveConfirmOrderLog.set("amount", receive_amount);
		arapChargeReceiveConfirmOrderLog.set("create_date", new Date());
		arapChargeReceiveConfirmOrderLog.set("creator",LoginUserController.getLoginUserId(this));
		arapChargeReceiveConfirmOrderLog.set("order_id",confirmId);
		arapChargeReceiveConfirmOrderLog.save();
		
		//获取已收款金额
   		sql = "select sum(acp.amount) total from arap_charge_receive_confirm_order_log acp "
				+ "  where acp.order_id = '"+confirmId+"'";
		Record re = Db.findFirst(sql);
		
		
		ArapChargeReceiveConfirmOrder arapChargeReceiveConfirmOrder = ArapChargeReceiveConfirmOrder.dao.findById(confirmId);
		
		String[] idArray = orderIds.split(",");
		if(order_type.equals("成本单")){
			ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(applicationId);
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				arapChargeReceiveConfirmOrder.set("status", "已收款").update();
				arapMiscCostOrder.set("status", "已收款").update();
			}else{
				arapChargeReceiveConfirmOrder.set("status", "部分已收款").update();
				arapMiscCostOrder.set("status", "部分已收款").update();
			}
		}else if(order_type.equals("开票记录单")){
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				//更新确认表状态
				arapChargeReceiveConfirmOrder.set("status", "已收款").update();
				
				//更新申请单状态
				for (int i = 0; i < idArray.length; i++) {
					ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(idArray[i]);
					arapChargeInvoice.set("status", "已收款确认").update();
					//List<Record> list= Db.find("SELECT STATUS from arap_cost_order where application_order_id =?",idArray[i]);
					List<ArapChargeInvoiceApplication> arapChargeInvoiceApplicationList = ArapChargeInvoiceApplication.dao.find("select * from arap_charge_invoice_application_order where invoice_order_id = ?",idArray[i]);
					for(ArapChargeInvoiceApplication arapChargeInvoiceApplication : arapChargeInvoiceApplicationList){
						arapChargeInvoiceApplication.set("status", "已收款确认").update();
			    	}
				}
			}else{
				arapChargeReceiveConfirmOrder.set("status", "部分已收款").update();
			}
		}
		
		
		//新建日记账表数据
		 ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
	        auditLog.set("payment_method", receive_type);
	        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_CHARGE);
	        auditLog.set("amount", receive_amount);
	        auditLog.set("creator", LoginUserController.getLoginUserId(this));
	        auditLog.set("create_date", new Date());
	        if(account!=null)
	        	auditLog.set("account_id", account.get("id"));
	        else
	        	auditLog.set("account_id", 4);
	        if(order_type.equals("成本单")){
	        	auditLog.set("misc_order_id", confirmId);
	        	auditLog.set("source_order", "成本单");
	        }
	        else if(order_type.equals("开票记录单")){
	        	auditLog.set("invoice_order_id", confirmId);
	        	auditLog.set("source_order", "应付开票记录单");
	        }
	        auditLog.save();
	        
	        if("cash".equals(receive_type)){
	        	Account cash = Account.dao.findFirst("select * from fin_account where bank_name ='现金'");
	        	cash.set("amount", (cash.getDouble("amount")==null?0.0:cash.getDouble("amount")) - Double.parseDouble(receive_amount)).update();
	        }else{
	        	account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) - Double.parseDouble(receive_amount)).update();
	        }
		
		Map BillingOrderListMap = new HashMap();
   		BillingOrderListMap.put("arapChargeReceiveConfirmOrder", arapChargeReceiveConfirmOrder);
   		BillingOrderListMap.put("re", re);
   		renderJson(BillingOrderListMap);
	}
	
   	
	public void logList() {
        String pageIndex = getPara("sEcho");
		String confirmId = getPara("confirmId");
		String sql = "select acp.*,ul.c_name from arap_charge_receive_confirm_order_log acp "
				+ " left join user_login ul on ul.id = acp.creator "
				+ "  where acp.order_id = '"+confirmId+"'";
		List<Record> re = Db.find(sql);
		
		Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", re.size());
        BillingOrderListMap.put("iTotalDisplayRecords", re.size());

        BillingOrderListMap.put("aaData", re);

		renderJson(BillingOrderListMap);
	}
	
	
    
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	
	public void create() {
		
		String all=getPara("chargeIds2");
		setAttr("all", all);
        String[] alls=all.split(",");
        String orderIds = "";
        String order_type = "";

        for(int i=0;i< alls.length;i++){
        	String[] one = alls[i].split(":");
        	if(orderIds == ""){
        		orderIds=one[0];
        	}else{
        		orderIds+=","+one[0];
        	}
        	order_type =one[1];
        }
        setAttr("orderIds", orderIds);
        setAttr("order_type", order_type);
          
        String sql = "";
        for(int i=0;i< alls.length;i++){
        	String[] one = alls[i].split(":");
			String id = one[0];
			if(order_type.equals("成本单")){
				sql = " SELECT c.abbr FROM arap_misc_charge_order amco "
						+ " LEFT JOIN party p ON p.id = amco.sp_id "
						+ " LEFT JOIN office o ON o.id = p.office_id "
						+ " LEFT JOIN contact c ON c.id = p.contact_id "
						+ " WHERE amco.id = '"+id+"'";
			}else if(order_type.equals("报销单")){
				sql = "SELECT c.abbr,aci.bill_type,aci.billing_unit,aci.payee_unit,aci.payee_name,aci.bank_no,aci.bank_name FROM `arap_cost_invoice_application_order` aci"
						+ " LEFT JOIN cost_application_order_rel cao on cao.application_order_id = aci.id"
						+ " LEFT JOIN arap_cost_order aco on aco.id = cao.cost_order_id "
						+ " LEFT JOIN party p ON p.id = aci.payee_id "
						+ " LEFT JOIN office o ON o.id = p.office_id "
						+ " LEFT JOIN contact c ON c.id = p.contact_id"
						+ " where aci.id = '"+id+"'";
			}else if(order_type.equals("开票记录单")){
//				sql = "SELECT c.abbr,aci.bill_type,aci.billing_unit,aci.payee_unit,aci.payee_name,aci.bank_no,aci.bank_name FROM `arap_cost_invoice_application_order` aci"
//						+ " LEFT JOIN cost_application_order_rel cao on cao.application_order_id = aci.id"
//						+ " LEFT JOIN arap_cost_order aco on aco.id = cao.cost_order_id "
//						+ " LEFT JOIN party p ON p.id = aci.payee_id "
//						+ " LEFT JOIN office o ON o.id = p.office_id "
//						+ " LEFT JOIN contact c ON c.id = p.contact_id"
//						+ " where aci.id = '"+id+"'";
				sql = "select * from arap_charge_invoice where id = ?"+id;
			}
        }
		//Record record = Db.findFirst(sql);
		setAttr("invoiceApplicationOrder", null);
		setAttr("userName", LoginUserController.getLoginUserName(this));
		render("/yh/arap/ChargeConfirm/ChargeConfirmEdit.html");

	}
	
	
	public void applicationList() {
        
        String pageIndex = getPara("sEcho");
        String orderIds = getPara("orderIds");
        String order_type = getPara("order_type");
        
        List<Record> record = null;
        String sql = "";
        Record re = null;
        if (orderIds != null && !"".equals(orderIds)) {
			String[] idArray = orderIds.split(",");
			if(order_type.equals("成本单")){
				sql = " SELECT amco.id,amco.order_no,amco.create_stamp cost_stamp,amco.total_amount pay_amount,"
						+ " ifnull(amco.total_amount-( select sum(acpcodl.pay_amount) from arap_cost_pay_confirm_order_detail_log acpcodl  "
						+ " LEFT JOIN arap_cost_pay_confirm_order_detail acpcod on acpcod.id = acpcodl.detail_id "
						+ " where acpcod.misc_cost_order_id = amco.id ),amco.total_amount) nopay_amount "
						+ " FROM arap_misc_cost_order amco "
						+ " WHERE amco.id in(" + orderIds + ")";
				
			}else if(order_type.equals("报销单")){
				sql = "";
			}else{
				sql = "SELECT aci.*,  "
					+ "IFNULL( aci.total_amount - ( SELECT sum(acrcodl.receive_amount) FROM arap_charge_receive_confirm_order_detail_log acrcodl "
					+ "LEFT JOIN arap_charge_receive_confirm_order_detail acrcod on acrcod.id = acrcodl.detail_id "
					+ "WHERE  acrcod.invoice_order_id = aci.id ), "
					+ "aci.total_amount ) noreceive_amount FROM arap_charge_invoice aci WHERE aci.id IN("+orderIds+")";
			}
			record = Db.find(sql);			
		}

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", record.size());
        BillingOrderListMap.put("iTotalDisplayRecords", record.size());

        BillingOrderListMap.put("aaData", record);

        renderJson(BillingOrderListMap);
    }
	
	
	
	

	// 更新支付金额信息
	public void updateMoney() {
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		String costOrderId = getPara("costOrderId");
		String sql = "select * from cost_application_order_rel where application_order_id = '"+costPreInvoiceOrderId+"' and cost_order_id = '"+costOrderId+"'";
		CostApplicationOrderRel costApplicationOrderRel = CostApplicationOrderRel.dao.findFirst(sql);
		String name = getPara("name");
		String value = getPara("value");
		
		if ("pay_amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		costApplicationOrderRel.set(name, value);
		costApplicationOrderRel.update();
		
		renderJson(costApplicationOrderRel);
	}

	
	
	
	public void searchAllAccount(){
		List<Account> accounts = Account.dao.find("select * from fin_account where type != 'REC' and bank_name != '现金'");
		renderJson(accounts);
	}

    // 付款确认单列表
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void list() {
    	String orderNo = getPara("orderNo")==null?null:getPara("orderNo").trim();
    	String appOrderNo = getPara("applicationOrderNo")==null?null:getPara("applicationOrderNo").trim();
		String status = getPara("status")==null?null:getPara("status").trim();
		String spName = getPara("sp_name")==null?null:getPara("sp_name").trim();
		String receiverName = getPara("receiverName")==null?null:getPara("receiverName").trim();
		String beginTime = getPara("beginTime")==null?null:getPara("beginTime").trim();
		String endTime = getPara("endTime")==null?null:getPara("endTime").trim();
		
    	String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
               
        String fromSql = " from arap_cost_pay_confirm_order cpco "
        			+ " left join party p1 on cpco.sp_id = p1.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join user_login ul on ul.id=cpco.creator";
        
        String totalSql = "select count(1) total" + fromSql;
        
        String columsSql = "select cpco.*, "
        		+ " (select group_concat( DISTINCT cao.order_no SEPARATOR '<br/>' ) "
        		+ " FROM arap_cost_pay_confirm_order_detail co, arap_cost_invoice_application_order cao "
				+ " where co.application_order_id = cao.id and co.order_id = cpco.id) fksq_no,"
				+ " (SELECT group_concat( DISTINCT amco.order_no SEPARATOR '<br/>' )"
				+ " FROM arap_cost_pay_confirm_order_detail co, arap_misc_cost_order amco"
				+ " WHERE co.misc_cost_order_id = amco.id AND co.order_id = cpco.id ) misc_no,"
				+ " ( SELECT ifnull(sum(caor.pay_amount), "
				+ " (SELECT  amco.total_amount FROM arap_cost_pay_confirm_order_detail co,"
				+ "  arap_misc_cost_order amco WHERE co.misc_cost_order_id = amco.id AND co.order_id = cpco.id )) "
				+ " FROM cost_application_order_rel caor WHERE caor.application_order_id "
				+ " IN (SELECT acpcod.application_order_id FROM arap_cost_pay_confirm_order cpco1 LEFT JOIN arap_cost_pay_confirm_order_detail acpcod "
				+ " ON acpcod.order_id = cpco1.id WHERE cpco1.id = cpco.id)) pay_amount,"
//				+ " (select sum(cao.total_amount) "
//        		+ " FROM arap_cost_pay_confirm_order_detail co, arap_cost_invoice_application_order cao "
//				+ " where co.application_order_id = cao.id and co.order_id = cpco.id) pay_amount,"
				+ " (SELECT	ifnull(sum(log.amount), 0) FROM arap_cost_pay_confirm_order_log log "
				+ " where log.order_id = cpco.id) already_pay, "
        		+ " c1.abbr sp_name,"
        		+ "ifnull(nullif(ul.c_name,''), ul.user_name) user_name "
        		+ fromSql;
        String orderBy= " order by cpco.create_date desc ";
        
        String conditions=" where 1=1 ";
        if (StringUtils.isNotEmpty(orderNo)){
        	conditions+=" and UPPER(cpco.order_no) like '%"+orderNo.toUpperCase()+"%'";
        }
//        if (appOrderNo != null){
//        	conditions+=" fksq_no like'%"+appOrderNo+"%'";
//        }
        if (StringUtils.isNotEmpty(status)){
        	conditions+=" and cpco.status = '"+status+"'";
        }
        if (StringUtils.isNotEmpty(spName)){
        	conditions+=" and c1.abbr like '%"+spName+"%'";
        }
        if (StringUtils.isNotEmpty(receiverName)){
        	conditions+=" and cpco.receive_person like '%"+receiverName+"%'";
        }
        
        if (StringUtils.isNotEmpty(beginTime)){
        	beginTime = " and cpco.create_date between'"+beginTime+"'";
        }else{
        	beginTime =" and cpco.create_date between '1970-1-1'";
        }
        
        if (StringUtils.isNotEmpty(endTime)){
        	endTime =" and '"+endTime+"'";
        }else{
        	endTime =" and '3000-1-1'";
        }
        conditions+=beginTime+endTime;
        
        
        totalSql+=conditions;
        Record recTotal = Db.findFirst(totalSql);
        Long total = recTotal.getLong("total");
        logger.debug("total records:" + total);
        
        columsSql+=conditions + orderBy + sLimit;
        List<Record> costPayConfirmOrders = Db.find(columsSql);

        Map orderListMap = new HashMap();
        orderListMap.put("sEcho", pageIndex);
        orderListMap.put("iTotalRecords", total);
        orderListMap.put("iTotalDisplayRecords", costPayConfirmOrders.size());

        orderListMap.put("aaData", costPayConfirmOrders);

        renderJson(orderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void chargeConfiremReturnOrder(){
    	String ids = getPara("ids");
    	String orderNos = getPara("orderNos");
    	String[] idArr = ids.split(",");
    	String[] orderNoArr = orderNos.split(",");
    	for(int i=0 ; i<idArr.length ; i++){
    		if("提货".equals(orderNoArr[i])){
    			DepartOrder pickupOrder = DepartOrder.dao.findById(idArr[i]);
    			pickupOrder.set("audit_status", "已确认");
    			pickupOrder.update();
    		}else if("零担".equals(orderNoArr[i]) || "整车".equals(orderNoArr[i])){
    			DepartOrder departOrder = DepartOrder.dao.findById(idArr[i]);
    			departOrder.set("audit_status", "已确认");
    			departOrder.update();
    		}else if("配送".equals(orderNoArr[i])){
    			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(idArr[i]);
    			deliveryOrder.set("audit_status", "已确认");
    			deliveryOrder.update();
    		}else if("成本单".equals(orderNoArr[i])){
    			ArapMiscCostOrder arapmisccostOrder = ArapMiscCostOrder.dao.findById(idArr[i]);
    			arapmisccostOrder.set("audit_status", "已确认");
    			arapmisccostOrder.update();
    		}else{
    			InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(idArr[i]);
    			insuranceOrder.set("audit_status", "已确认");
    			insuranceOrder.update();
    		}
    	}
        renderJson("{\"success\":true}");
    }
}
