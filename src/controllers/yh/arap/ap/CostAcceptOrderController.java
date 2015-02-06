package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapAccountAuditLog;
import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;
import models.yh.arap.ArapMiscCostOrder;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostAcceptOrderController extends Controller {
    private Logger logger = Logger.getLogger(CostAcceptOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTCONFIRM_LIST})
    public void index() {
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
    	    render("/yh/arap/CostAcceptOrder/CostAcceptOrderList.html");
    }

    // billing order 列表
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String status ="已审批";
        String fk_status ="新建";
        String select_status = getPara("status");
        if(select_status != null && !"".equals(select_status)){
        	status = select_status;
        	fk_status = select_status;
        }
       /* String sqlTotal = "select count(1) total from (select aci.id, aci.order_no, aci.status, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,aci.total_amount total_amount,c.abbr cname "
        		+ " from arap_cost_invoice_application_order aci "
        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id"
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status='" + status + "'  group by aci.id "
				+ " union all "
				+ " select amco.id, amco.order_no, amco.status, '' invoice_no, amco.create_stamp create_time, amco.remark, amco.total_amount,c.abbr cname "
				+ " from arap_misc_cost_order amco"
				+ " left join party p on p.id = amco.payee_id left join contact c on c.id = p.contact_id"
				+ " where amco.status='" + fk_status + "') tab";
        
        String sql = "select aci.id, aci.order_no, aci.status, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,aci.total_amount total_amount,c.abbr cname "
        		+ " from arap_cost_invoice_application_order aci "
        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id"
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status='" + status + "' group by aci.id "
				+ " union all "
				+ " select amco.id, amco.order_no, amco.status, '' invoice_no, amco.create_stamp create_time, amco.remark, amco.total_amount,c.abbr cname "
				+ " from arap_misc_cost_order amco"
				+ " left join party p on p.id = amco.payee_id left join contact c on c.id = p.contact_id"
				+ " where amco.status='" + fk_status + "' "
				+ " order by create_time desc " + sLimit;*/
        String sqlTotal = "select count(1) total"
		        		+ " from arap_cost_invoice_application_order aci "
		        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id"
		        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status='" + status + "'";
        
        String sql = "select aci.id, aci.order_no, aci.status, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,aci.total_amount total_amount,c.abbr cname "
        		+ " from arap_cost_invoice_application_order aci "
        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id"
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status='" + status + "' group by aci.id order by aci.create_stamp desc " + sLimit;;
        
        
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    // 收款
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTCONFIRM_CONFIRM})
    @Before(Tx.class)
    public void costAccept(){
    	ArapCostOrder arapAuditOrder = ArapCostOrder.dao.findById(getPara("costCheckOrderId"));
    	arapAuditOrder.set("status", "completed");
    	arapAuditOrder.update();
        renderJson("{\"success\":true}");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTCONFIRM_CONFIRM})
    public void save(){
    	String costIds = getPara("costIds");
    	String paymentMethod = getPara("paymentMethod");
    	String accountId = getPara("accountTypeSelect");
    	String[] costIdArr = null; 
    	if(costIds != null && !"".equals(costIds)){
    		costIdArr = costIds.split(",");
    	}
    	for(int i=0;i<costIdArr.length;i++){
    		String[] arr = costIdArr[i].split(":");
    		String orderId = arr[0];
    		String orderNo = arr[1];
            if(orderNo.startsWith("SGFK")){
				ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(orderId);
				arapMiscCostOrder.set("status", "已付款确认");
				arapMiscCostOrder.update();
            }else{
                ArapCostInvoiceApplication arapcostInvoice = ArapCostInvoiceApplication.dao.findById(orderId);
                arapcostInvoice.set("status", "已付款确认");
                arapcostInvoice.update();
                //应收对账单的状态改变
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
                    
                }
                
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
						rec = Db.findFirst("select aci.total_amount total from arap_cost_invoice_application_order aci where aci.order_no='"+orderNo+"'");
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
						rec = Db.findFirst("select aci.total_amount total from arap_cost_invoice_application_order aci where aci.order_no='"+orderNo+"'");
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
    	redirect("/costAcceptOrder");
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
