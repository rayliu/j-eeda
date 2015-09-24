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
    	String ids = getPara("ids");
    	String beginTime = getPara("beginTime");
    	String endTime = getPara("endTime");
    	if(ids == null || "".equals(ids)){
    		ids = "-1";
    	}
    	if(beginTime == null || "".equals(beginTime)){
    		beginTime = "1970-01-01";
    	}
    	if(endTime == null || "".equals(endTime)){
    		endTime = "2037-12-31";
    	}
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "";
        String sql = "";
        if(!"-1".equals(ids)){
        	sqlTotal = "select count(1) total from arap_account_audit_log aaal where aaal.account_id in("+ids+") and aaal.create_date between  '" + beginTime + "' and '" + endTime + "'";
        	sql = "select aaal.*,aci.order_no invoice_order_no,ul.user_name user_name from arap_account_audit_log aaal"
        			+ " left join user_login ul on ul.id = aaal.creator"
        			+ " left join arap_charge_invoice aci on aci.id = aaal.invoice_order_id and aaal.source_order='应付开票申请单'"
        			+ " left join transfer_accounts_order tao ON tao.id = aaal.invoice_order_id and aaal.source_order='转账单' "
        			+ " where aaal.account_id in("+ids+") and aaal.create_date between  '" + beginTime + "' and '" + endTime + " 23:59:59' order by aaal.create_date desc " + sLimit;        	
        }else{
        	sqlTotal = "select count(1) total from arap_account_audit_log aaal where aaal.create_date between  '" + beginTime + "' and '" + endTime + " 23:59:59'";
        	sql = "select aaal.*,aci.order_no invoice_order_no,ifnull(ul.c_name, ul.user_name) user_name, fa.bank_name,"
        			+ " if(aaal.payment_type='CHARGE', "
        			+ "	   (select order_no from arap_charge_invoice where id = aaal.misc_order_id),"
        			+ " ifnull((select group_concat( DISTINCT order_no SEPARATOR '<br/>' ) from arap_cost_invoice_application_order where id in("
        			+ "  select d.application_order_id "
				    + "  from arap_cost_pay_confirm_order_detail d, arap_cost_pay_confirm_order_detail_log dl"
				    + "  where d.id = dl.detail_id and dl.order_id=aaal.invoice_order_id )) "
					+ " ,(select group_concat( DISTINCT order_no SEPARATOR '<br/>' ) from car_summary_order where id in("
        			+ "  select d.car_summary_order_id "
				    + "  from arap_cost_pay_confirm_order_detail d, arap_cost_pay_confirm_order_detail_log dl"
				    + "  where d.id = dl.detail_id and dl.order_id=aaal.car_summary_order_id )))) order_no from arap_account_audit_log aaal"
        			+ " left join user_login ul on ul.id = aaal.creator"
        			+ " left join arap_charge_invoice aci on aci.id = aaal.invoice_order_id and aaal.source_order='应付开票申请单'"
        			+ " left join transfer_accounts_order tao ON tao.id = aaal.invoice_order_id and aaal.source_order='转账单' "
        			+ " left join fin_account fa on aaal.account_id = fa.id "
        			+ " where aaal.create_date between  '" + beginTime + "' and '" + endTime + " 23:59:59' order by aaal.create_date desc " + sLimit;  
        }
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    //出纳日记帐：所有账户的按月期初期末总计
    public void accountList() {
    	String sLimit = "";
    	String pageIndex = getPara("sEcho");
    	if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    		sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    	}
    	
    	String sqlTotal = "select count(1) total from fin_account";
    	Record rec = Db.findFirst(sqlTotal);
    	logger.debug("total records:" + rec.getLong("total"));
    	
    	String sql = "select fa.*, aas.* from fin_account fa "
    			+ "left join arap_account_audit_summary aas on fa.id = aas.account_id order by fa.id desc " + sLimit;
    	
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
