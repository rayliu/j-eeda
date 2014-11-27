package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeOrder;

import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeAcceptOrderController extends Controller {
    private Logger logger = Logger.getLogger(ChargeAcceptOrderController.class);

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

        String sqlTotal = "select count(1) total from arap_charge_invoice aci";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aci.*,group_concat(invoice_item.invoice_no separator '\r\n') invoice_no"
					  + " from arap_charge_invoice aci"
					  + " left join arap_charge_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id order by aci.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
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
}
