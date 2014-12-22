package controllers.yh.arap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
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
public class AccountAuditLogController extends Controller {
    private Logger logger = Logger.getLogger(AccountAuditLogController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PCO_LIST})
    public void index() {
    	 render("/yh/arap/AccountAuditLog/AccountAuditLogList.html");
    }

    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_account_audit_log aaal";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aaal.*,aci.order_no invoice_order_no,ul.user_name user_name from arap_account_audit_log aaal"
        				+ " left join user_login ul on ul.id = aaal.creator"
        				+ " left join arap_charge_invoice aci on aci.id = aaal.invoice_order_id order by aaal.create_date desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    public void accountList() {
    	String sLimit = "";
    	String pageIndex = getPara("sEcho");
    	if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    		sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    	}
    	
    	String sqlTotal = "select count(1) total from fin_account";
    	Record rec = Db.findFirst(sqlTotal);
    	logger.debug("total records:" + rec.getLong("total"));
    	
    	String sql = "select * from fin_account order by id desc " + sLimit;
    	
    	logger.debug("sql:" + sql);
    	List<Record> BillingOrders = Db.find(sql);
    	
    	Map BillingOrderListMap = new HashMap();
    	BillingOrderListMap.put("sEcho", pageIndex);
    	BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
    	BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
    	
    	BillingOrderListMap.put("aaData", BillingOrders);
    	
    	renderJson(BillingOrderListMap);
    }
}
