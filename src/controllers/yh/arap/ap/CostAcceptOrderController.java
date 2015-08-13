package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;





import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;





import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;




import models.yh.arap.ReimbursementOrder;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;





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

    public void unlist() {
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
      
        String sqlTotal = "SELECT COUNT(1) total from (select aci.order_no"
		        		+ " from arap_cost_invoice_application_order aci "
		        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id"
		        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status='" + status + "'"
		        		+ " UNION"
		        		+ " SELECT  ro.order_no FROM reimbursement_order ro"
		        		+ " LEFT JOIN car_summary_order cso on cso.id in(ro.car_summary_order_ids)"
		        		+ " where ro.STATUS='audit') as a";
        
        String sql = "select aci.id, aci.order_no, aci.payment_method, aci.payee_name, aci.account_id, aci.status,'对账单' attribute, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,aci.total_amount total_amount,c.abbr cname "
        		+ " from arap_cost_invoice_application_order aci "
        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id"
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status='" + status + "' group by aci.id "
        		+ " UNION"
        		+ " SELECT ro.id, ro.order_no,null as payment_method,null as payee_name,null as account_id,"
        		+ " ro.STATUS,'报销单' attribute,null as invoice_no,ro.create_stamp create_time,ro.remark,"
        		+ " (SELECT round(sum(cso.next_start_car_amount + cso.month_refuel_amount) - sum(cso.deduct_apportion_amount),2)"
        		+ " FROM car_summary_order cso WHERE cso.id IN (SELECT id FROM car_summary_order cso WHERE cso.reimbursement_order_id = ro.id)) actual_cost,"
        		+ " null as cname"
        		+ " FROM reimbursement_order ro"
        		+ " LEFT JOIN car_summary_order cso on cso.id in(ro.car_summary_order_ids)"
        		+ " where ro.STATUS='audit'" + sLimit;;
        
        
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
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
      
        String sqlTotal = "select count(1) total"
		        		+ " from arap_cost_invoice_application_order aci "
		        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id"
		        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status in ('已复核','已付款确认')";
        
        String sql = "select aci.id, aci.order_no, aci.payment_method, aci.payee_name, aci.account_id, aci.status, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,aci.total_amount total_amount,c.abbr cname "
        		+ " from arap_cost_invoice_application_order aci "
        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id"
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status in ('已复核','已付款确认') group by aci.id "
        		+ " UNION"
        		+ " SELECT ro.id, ro.order_no,null as payment_method,null as payee_name,null as account_id,"
        		+ " ro.STATUS,null as invoice_no,ro.create_stamp create_time,ro.remark,"
        		+ " (SELECT round(sum(cso.next_start_car_amount + cso.month_refuel_amount) - sum(cso.deduct_apportion_amount),2)"
        		+ " FROM car_summary_order cso WHERE cso.id IN (SELECT id FROM car_summary_order cso WHERE cso.reimbursement_order_id = ro.id)) actual_cost,"
        		+ " null as cname"
        		+ " FROM reimbursement_order ro"
        		+ " LEFT JOIN car_summary_order cso on cso.id in(ro.car_summary_order_ids)"
        		+ " where ro.STATUS='已复核'"  + sLimit;
        
        
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
    public void checkStatus(){
        String orderId=getPara("ids");
        String order=getPara("order");
        String[] orderArrId=orderId.split(",");
        String[] orderArr=order.split(",");
        List<Record> recordList= new ArrayList<Record>();
        for(int i=0;i<orderArrId.length;i++){
        	if(orderArr[i].equals("对账单")){
            ArapCostInvoiceApplication arapcostinvoiceapplication= ArapCostInvoiceApplication.dao.findById(orderArrId[i]);
            arapcostinvoiceapplication.set("status","已复核");
            arapcostinvoiceapplication.update();
        	}else if(orderArr[i].equals("报销单")){
        		ReimbursementOrder reimbursementorder =ReimbursementOrder.dao.findById(orderArrId[i]);
        		reimbursementorder.set("status", "已复核");
        		reimbursementorder.update();
        	}
            renderJson("{\"success\":true}");
        }
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
    
    
}
