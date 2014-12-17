package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.ArapChargeInvoice;
import models.ArapChargeOrder;
import models.ArapMiscChargeOrder;
import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeAcceptOrderController extends Controller {
    private Logger logger = Logger.getLogger(ChargeAcceptOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
    	    render("/yh/arap/ChargeAcceptOrder/ChargeAcceptOrderList.html");
    }

    // billing order 列表
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from (select aci.id, aci.order_no, aci.status, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark "
        		+ "from arap_charge_invoice aci left join arap_charge_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id group by aci.id "
				+ "union all "
				+ "select id, order_no, status, '' invoice_no, create_stamp create_time, remark  from arap_misc_charge_order where status='新建') tab";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aci.id, aci.order_no, aci.status, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark "
        		+ "from arap_charge_invoice aci left join arap_charge_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id group by aci.id "
				+ "union all "
				+ "select id, order_no, status, '' invoice_no, create_stamp create_time, remark  from arap_misc_charge_order where status='新建' "
				+ "order by create_time desc " + sLimit;

        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    // 收款
    public void chargeAccept(){
    	ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(getPara("chargeCheckOrderId"));
    	arapAuditOrder.set("status", "completed");
    	arapAuditOrder.update();
        renderJson("{\"success\":true}");
    }
    
    public void save(){
    	String chargeIds = getPara("chargeIds");
    	String paymentMethod = getPara("paymentMethod");
    	String[] chargeIdArr = null; 
    	if(chargeIds != null && !"".equals(chargeIds)){
    		chargeIdArr = chargeIds.split(",");
    	}
    	for(int i=0;i<chargeIdArr.length;i++){
    		String[] arr = chargeIdArr[i].split(":");
    		String orderNo = arr[1];
    		if(orderNo.startsWith("SGSK")){
    			ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(arr[0]);
    			arapMiscChargeOrder.set("status", "已收款确认");
    			arapMiscChargeOrder.update();
    			
    			//TODO: 现金 或 银行  金额处理
    			
    			//TODO: 日记账
    		}else{
	    		ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(chargeIdArr[i]);
	    		arapChargeInvoice.set("status", "已收款确认");
	    		arapChargeInvoice.update();
	    		
	    		ArapAccountAuditLog accountAuditLog = new ArapAccountAuditLog();
	    		accountAuditLog.set("account_id", getPara("accountTypeSelect"));
	    		accountAuditLog.set("invoice_order_id", chargeIdArr[i]);
	    		accountAuditLog.set("payment_method", paymentMethod);
	    		//accountAuditLog.set("amount", );
	    		String name = (String) currentUser.getPrincipal();            
	    		List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
	            java.util.Date utilDate = new java.util.Date();
	            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
	    		accountAuditLog.set("creator", users.get(0).get("id"));
	    		accountAuditLog.set("create_date", sqlDate);
	    		accountAuditLog.save();
    		}
    	}
    	redirect("/chargeAcceptOrder");
    }
}
