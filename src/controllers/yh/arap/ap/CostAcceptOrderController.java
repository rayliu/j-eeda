package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;
import models.yh.arap.ArapMiscCostOrder;
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
    	String page = getPara("page");
    	setAttr("page", page);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
    	render("/yh/arap/CostAcceptOrder/CostAcceptOrderList.html");
    }
    
    
    
    public void edit2() {
    	
    	String id = getPara("id");
    	String attribute = getPara("attribute");
    	String sql = "SELECT ul.c_name,c.abbr cname,aciao.* FROM arap_cost_invoice_application_order aciao "
    			+ " LEFT JOIN party p ON p.id = aciao.payee_id "
    			+ " LEFT JOIN contact c ON c.id = p.contact_id "
    			+ " LEFT JOIN user_login ul on ul.id = aciao.create_by "
    			+ " where aciao.id = '"+id+"'";
    	Record re = Db.findFirst(sql);
    	setAttr("invoiceApplication", re);
    	setAttr("attribute", attribute);
    	
    	render("/yh/arap/CostAcceptOrder/invoiceEdit.html");
    }
    
    
    public void costOrderList() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        String id = getPara("id");
        String sqlTotal = "";
        String sql = "";
        List<Record> record = null;
        Record re = null;
        if (id != null && !"".equals(id)) {
			//String[] idArray = id.split(",");
			sql = "SELECT '对账单' type,caor.pay_amount,(aco.cost_amount - (select sum(caor1.pay_amount) from cost_application_order_rel caor1 where caor1.cost_order_id = aco.id)) daifu,aco.* FROM arap_cost_invoice_application_order aciao "
					+ " LEFT JOIN cost_application_order_rel caor on caor.application_order_id = aciao.id "
					+ " LEFT JOIN arap_cost_order aco on aco.id = caor.cost_order_id "
					+ " where aciao.id ='"+id+"'";
			record = Db.find(sql);			
		}

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", record.size());
        BillingOrderListMap.put("iTotalDisplayRecords", record.size());

        BillingOrderListMap.put("aaData", record);

        renderJson(BillingOrderListMap);
    }

    public void unlist() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        String spName = getPara("sp");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String orderNo = getPara("orderNo");
        String status1 = getPara("status");
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
        String condition = "";
        if(spName != null || status1 != null || beginTime != null || endTime != null|| orderNo != null)
        {
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
		condition = " where "
					+ " ifnull(cname,'') like '%" + spName + "%' "
					+ " and create_time between '" + beginTime + "' and '" + endTime+ " 23:59:59' "
				    + " and ifnull(order_no,'') like '%" + orderNo + "%' "
				    + " and ifnull(status,'') like '%" + status1 + "%' ";
        }
        String sqlTotal = "select count(*) total from(select aci.id, aci.order_no, aci.payment_method, aci.payee_name, aci.account_id, aci.status,'对账单' attribute, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,"
        		+ " aci.total_amount total_amount, "
        		+ " ( select sum(cao.pay_amount) from cost_application_order_rel cao where cao.application_order_id = aci.id ) application_amount, "
        		+ " c.abbr cname "
        		+ " from arap_cost_invoice_application_order aci "
        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id "
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status='" + status + "' group by aci.id "
        		+ " UNION"
        		+ " SELECT ro.id, ro.order_no,null as payment_method,ro.account_name as payee_name,null as account_id,"
        		+ " ro.STATUS,'报销单' attribute,null as invoice_no,ro.create_stamp create_time,ro.remark,"
        		+ " ro.amount actual_cost,"
        		+ " null as application ,"
        		+ " null as cname"
        		+ " FROM reimbursement_order ro"
        		+ " where ro.status in ('audit', '新建') and ro.order_no not like 'XCBX%'"
        		+ " UNION SELECT amco.id, amco.order_no, NULL AS payment_method, "
        		+ " NULL AS payee_name, "
        		+ " NULL AS account_id, amco. STATUS, "
        		+ " '成本单' attribute, NULL AS invoice_no, amco.create_stamp create_time, amco.remark, amco.total_amount total_amount, "
        		+ " NULL AS application_amount, "
        		+ " (case amco.cost_to_type"
        		+ " when 'sp' then "
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.sp_id)"
                + " when 'customer' then"
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.customer_id)"
                + " end"
                + " ) AS cname "
        		+ " FROM arap_misc_cost_order amco WHERE amco.STATUS= '新建' and amco.type = 'non_biz'"
        		+ " ) A";
        
        String sql = "select * from(select aci.id, aci.order_no, aci.payment_method, aci.payee_name, aci.account_id, aci.status,'对账单' attribute, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,"
        		+ " aci.total_amount total_amount, "
        		+ " ( select sum(cao.pay_amount) from cost_application_order_rel cao where cao.application_order_id = aci.id ) application_amount, "
        		+ " c.abbr cname "
        		+ " from arap_cost_invoice_application_order aci "
        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id "
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status='" + status + "' group by aci.id "
        		+ " UNION"
        		+ " SELECT ro.id, ro.order_no,null as payment_method,ro.account_name as payee_name,null as account_id,"
        		+ " ro.STATUS,'报销单' attribute,null as invoice_no,ro.create_stamp create_time,ro.remark,"
        		+ " ro.amount actual_cost,"
        		+ " null as application ,"
        		+ " null as cname"
        		+ " FROM reimbursement_order ro"
        		+ " where ro.status in ('audit', '新建') and ro.order_no not like 'XCBX%'"
        		+ " UNION SELECT amco.id, amco.order_no, NULL AS payment_method, "
        		+ " NULL AS payee_name, "
        		+ " NULL AS account_id, amco. STATUS, "
        		+ " '成本单' attribute, NULL AS invoice_no, amco.create_stamp create_time, amco.remark, amco.total_amount total_amount, "
        		+ " NULL AS application_amount, "
        		+ " (case amco.cost_to_type"
        		+ " when 'sp' then "
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.sp_id)"
                + " when 'customer' then"
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.customer_id)"
                + " end"
                + " ) AS cname "
        		+ " FROM arap_misc_cost_order amco WHERE amco.STATUS= '新建' and amco.type = 'non_biz'"
        		+ " ) A";
        		
        
        
        Record rec = Db.findFirst(sqlTotal+condition);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> BillingOrders = Db.find(sql+condition+ " order by A.create_time desc " + sLimit);

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
        //String customer = getPara("companyName");
        String spName = getPara("sp");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
      
        String statusStr = " ('已复核','已付款确认')";
        if(status!=null && status.equals("payed")){
            statusStr = " ('付款确认中','已付款确认')";
        };
        String condition = "";
        if(spName != null || status != null || beginTime != null || endTime != null|| orderNo != null)
        {
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
		condition = " where "
					+ " ifnull(cname,'') like '%" + spName + "%' "
					+ " and create_time between '" + beginTime + "' and '" + endTime+ " 23:59:59' "
				    + " and ifnull(order_no,'') like '%" + orderNo + "%' "
				    + " and ifnull(status,'') like '%" + status + "%' ";
        }
        String sqlTotal = "select count(*) total from(select aci.id, aci.order_no,'申请单' as order_type, aci.payment_method, aci.payee_name, aci.account_id, aci.status, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,"
        		+ " aci.total_amount total_amount, "
        		+ " ( select sum(cao.pay_amount) from cost_application_order_rel cao where cao.application_order_id = aci.id ) application_amount, "
        		+ " c.abbr cname "
        		+ " from arap_cost_invoice_application_order aci "
        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id "
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status in "+statusStr+" group by aci.id "
        		+ " UNION"
        		+ " SELECT ro.id, ro.order_no,'报销单' as order_type,null as payment_method,null as payee_name,null as account_id,"
        		+ " ro.STATUS,null as invoice_no,ro.create_stamp create_time,ro.remark,"
        		+ " (SELECT round(sum(cso.next_start_car_amount + cso.month_refuel_amount) - sum(cso.deduct_apportion_amount),2)"
        		+ " FROM car_summary_order cso WHERE cso.id IN (SELECT id FROM car_summary_order cso WHERE cso.reimbursement_order_id = ro.id)) actual_cost,"
        		+ " null as application ,"
        		+ " null as cname"
        		+ " FROM reimbursement_order ro"
        		+ " LEFT JOIN car_summary_order cso on cso.id in(ro.car_summary_order_ids)"
        		+ " where ro.STATUS='已复核'"
        		+ " UNION SELECT amco.id, amco.order_no,'成本单' as order_type, NULL AS payment_method, null AS payee_name, NULL AS account_id, amco. STATUS, "
        		+ "  NULL AS invoice_no, amco.create_stamp create_time, amco.remark, amco.total_amount total_amount, "
        		+ " NULL AS application_amount, "
        		+ " (case amco.cost_to_type"
        		+ " when 'sp' then "
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.sp_id)"
                + " when 'customer' then"
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.customer_id)"
                + " end"
                + " ) AS cname FROM arap_misc_cost_order amco WHERE amco.STATUS= '已复核' and amco.type = 'non_biz'"
        		+ " ) A";
        
        String sql = "select * from(select aci.id, aci.order_no,'申请单' as order_type, aci.payment_method, aci.payee_name, aci.account_id, aci.status, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,"
        		+ " aci.total_amount total_amount, "
        		+ " ( select sum(cao.pay_amount) from cost_application_order_rel cao where cao.application_order_id = aci.id ) application_amount, "
        		+ " c.abbr cname "
        		+ " from arap_cost_invoice_application_order aci "
        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id "
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status in "+statusStr+" group by aci.id "
        		+ " UNION"
        		+ " SELECT ro.id, ro.order_no,'报销单' as order_type,null as payment_method,null as payee_name,null as account_id,"
        		+ " ro.STATUS,null as invoice_no,ro.create_stamp create_time,ro.remark,"
        		+ " (SELECT round(sum(cso.next_start_car_amount + cso.month_refuel_amount) - sum(cso.deduct_apportion_amount),2)"
        		+ " FROM car_summary_order cso WHERE cso.id IN (SELECT id FROM car_summary_order cso WHERE cso.reimbursement_order_id = ro.id)) actual_cost,"
        		+ " null as application ,"
        		+ " null as cname"
        		+ " FROM reimbursement_order ro"
        		+ " LEFT JOIN car_summary_order cso on cso.id in(ro.car_summary_order_ids)"
        		+ " where ro.STATUS='已复核'"
        		+ " UNION SELECT amco.id, amco.order_no,'成本单' as order_type, NULL AS payment_method, null AS payee_name, NULL AS account_id, amco. STATUS, "
        		+ "  NULL AS invoice_no, amco.create_stamp create_time, amco.remark, amco.total_amount total_amount, "
        		+ " NULL AS application_amount, "
        		+ " (case amco.cost_to_type"
        		+ " when 'sp' then "
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.sp_id)"
                + " when 'customer' then"
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.customer_id)"
                + " end"
                + " ) AS cname FROM arap_misc_cost_order amco WHERE amco.STATUS= '已复核' and amco.type = 'non_biz'"
        		+ ") A";
        
        
        Record rec = Db.findFirst(sqlTotal+condition);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> BillingOrders = Db.find(sql+ condition + " order by A.create_time desc " +sLimit);

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
        	}else if(orderArr[i].equals("成本单")){
        		ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(orderArrId[i]);
        		arapMiscCostOrder.set("status", "已复核");
        		arapMiscCostOrder.update();
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
