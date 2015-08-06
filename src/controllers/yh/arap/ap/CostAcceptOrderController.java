package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;




import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;




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
      
        String sqlTotal = "select count(1) total"
		        		+ " from arap_cost_invoice_application_order aci "
		        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id"
		        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status='" + status + "'";
        
        String sql = "select aci.id, aci.order_no, aci.payment_method, aci.payee_name, aci.account_id, aci.status, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,aci.total_amount total_amount,c.abbr cname "
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
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status in ('已复核','已付款确认') group by aci.id order by aci.create_stamp desc " + sLimit;;
        
        
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
        String[] orderArrId=orderId.split(",");
        List<Record> recordList= new ArrayList<Record>();
        for(int i=0;i<orderArrId.length;i++){
            ArapCostInvoiceApplication arapcostinvoiceapplication= ArapCostInvoiceApplication.dao.findById(orderArrId[i]);
            arapcostinvoiceapplication.set("status","已复核");
            arapcostinvoiceapplication.update();
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
