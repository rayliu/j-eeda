package controllers.yh.arap.ap;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.SetAttrLoginUserInterceptor;
import models.Account;
import models.ArapAccountAuditLog;
import models.ArapCostInvoiceApplication;
import models.UserLogin;
import models.yh.arap.ReimbursementOrder;
import models.yh.arap.TransferAccounts;

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

import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TransferAccountsController extends Controller {
	private Logger logger = Logger.getLogger(CostCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	 @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_LIST})
	public void index() {
		render("/yh/arap/TransferAccounts/TransferAccountsList.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_CREATE, PermissionConstant.PERMSSION_COSTREIMBURSEMENT_UPDATE}, logical=Logical.OR)
		public void create() {
			List<Record> itemList  = Db.find("select * from fin_item where type='报销费用' and parent_id !=0 ");
	        setAttr("itemList", itemList);
	        List<Record> parentItemList  = Db.find("select * from fin_item where type='报销费用' and parent_id =0 ");
	        setAttr("parentItemList", parentItemList);
			render("/yh/arap/TransferAccounts/TransferAccountsEdit.html");
		}
	public void findUser(){
		String userId = getPara("userId");
		UserLogin approval = UserLogin.dao
				.findFirst("select * from user_login where id='" + userId + "'");
		renderJson(approval);
	}
	public void list(){
		String method =getPara("transfer_filter");
		String in_filter =getPara("in_filter");
		String out_filter =getPara("out_filter");
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = " SELECT count(1) total"
        			+ " FROM"
        			+ " transferaccounts tfa"
        			+ " LEFT JOIN fin_account fain ON fain.id = tfa.bank_in"
        			+ " LEFT JOIN fin_account faout ON faout.id = tfa.bank_out"
        			+ " LEFT JOIN user_login ul ON ul.id = tfa.create_id";
        String sql=" SELECT tfa.id id,tfa.order_no order_no,tfa.transfer_stamp transfer_stamp,tfa.`STATUS` transfer_status,"
        			+ " tfa.transfer_method transfer_method,fain.account_no account_in,"
        			+ " faout.account_no account_out,tfa.amount,ul.c_name c_name,tfa.remark remark"
        			+ " FROM"
        			+ " transferaccounts tfa"
        			+ " LEFT JOIN fin_account fain ON fain.id = tfa.bank_in"
        			+ " LEFT JOIN fin_account faout ON faout.id = tfa.bank_out"
        			+ " LEFT JOIN user_login ul ON ul.id = tfa.create_id";
        String condition = "";
        
        /*if(orderNo != null || sp != null || no != null || beginTime != null
        	|| endTime !=null || status != null || type != null){
			if (plantime == null || "".equals(plantime)) {
				plantime = "1-1-1";
			}
			if (arrivaltime == null || "".equals(arrivaltime)) {
				arrivaltime = "9999-12-31";
			}*/
			 
        	/*condition =  " where ifnull(order_no,'') like '%" + no + "%' "
        			+ " and ifnull(transfer_order_no,'') like '%" + orderNo + "%' "
        			+ " and ifnull(status,'') like '%" + status + "%' "
        			+ " and ifnull(spname,'') like '%" + sp + "%' "
        			+ " and ifnull(depart_time, '1-1-1') between '" + beginTime + "' and '" + endTime + "' "
        			+ " and ifnull(business_type,'') like '%" + type + "%'"
        			+ " and ifnull(route_from,'') like '%" + route_from + "%'"
        			+ " and ifnull(route_to,'') like '%" + route_to + "%'"
        			+ " and ifnull(booking_note_number,'') like '%" + booking_note_number + "%'"
        			+ " and ifnull(planning_time, '1-1-1') between '" + plantime + "' and '" + arrivaltime + "' "
        	        + " and ifnull(cname,'') like '%" + customer_name + "%'"
        	        + " and ifnull(status, '') != '手动删除'";
        }
        if (condition == "" ){
        	condition=" where ifnull(status, '') != '手动删除' ";
        }*/
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        long sTime = Calendar.getInstance().getTimeInMillis();
        List<Record> BillingOrders = Db.find(sql+sLimit);
        long eTime = Calendar.getInstance().getTimeInMillis();
        logger.debug("time cost:" + (eTime-sTime));
        
        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
        BillingOrderListMap.put("aaData", BillingOrders);
        renderJson(BillingOrderListMap);
       
	}
	public void edit(){
			String id = getPara("id");
			TransferAccounts transferaccounts = TransferAccounts.dao.findById(id);
			setAttr("transferaccounts", transferaccounts);
			Account in_acc=Account.dao.findById(transferaccounts.get("bank_in"));
			Account out_acc=Account.dao.findById(transferaccounts.get("bank_out"));
			setAttr("in_acc",in_acc);
			setAttr("out_acc",out_acc);
			//创建人
			UserLogin create = UserLogin.dao
					.findFirst("select * from user_login where id='" + transferaccounts.get("create_id") + "'");
			setAttr("createName", create.get("c_name"));
		render("/yh/arap/TransferAccounts/TransferAccountsEdit.html");
	}
		@RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_CREATE, PermissionConstant.PERMSSION_COSTREIMBURSEMENT_UPDATE}, logical=Logical.OR)
		public void getFinAccount() {
			List<Record> locationList= Db.find("SELECT * from fin_account");
			renderJson(locationList);
		}
		@RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_CREATE, PermissionConstant.PERMSSION_COSTREIMBURSEMENT_UPDATE}, logical=Logical.OR)
		public void confirem() {
			String transferOrderId =getPara("transferOrderId");
			String in_filter =getPara("in_filter");
			String out_filter =getPara("out_filter");
			String amount =getPara("amount");
			String remark =getPara("remark");
			TransferAccounts transferaccounts =TransferAccounts.dao.findById(transferOrderId);
			transferaccounts.set("STATUS","已确认");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name + "'");
			//更新账户金额
			if(!"".equals(in_filter) && in_filter != null){
				Account account= Account.dao.findById(in_filter);
				double account_aounmt =account.getDouble("amount");
				double change_aounmt=account_aounmt+Double.parseDouble(amount);
				account.set("amount", change_aounmt);
				account.update();
				ArapAccountAuditLog arapaccountauditlog = new ArapAccountAuditLog();
				arapaccountauditlog.set("payment_method", "transfers");
				arapaccountauditlog.set("payment_type", "CHARGE");
				arapaccountauditlog.set("amount", amount);
				arapaccountauditlog.set("creator",users.get(0).get("id"));
				arapaccountauditlog.set("create_date",new Date());
				arapaccountauditlog.set("remark", remark);
				arapaccountauditlog.set("source_order", "转账单");
				arapaccountauditlog.set("account_id", in_filter);
				arapaccountauditlog.save();
			}
			if(!"".equals(out_filter) && in_filter != null){
				ArapAccountAuditLog arapaccountauditlog = new ArapAccountAuditLog();
				Account account= Account.dao.findById(out_filter);
				double account_aounmt =account.getDouble("amount");
				double change_aounmt=account_aounmt-Double.parseDouble(amount);
				account.set("amount", change_aounmt);
				account.update();
				arapaccountauditlog.set("payment_method", "transfers");
				arapaccountauditlog.set("payment_type", "COST");
				arapaccountauditlog.set("amount", amount);
				arapaccountauditlog.set("creator",users.get(0).get("id"));
				arapaccountauditlog.set("create_date",new Date());
				arapaccountauditlog.set("remark", remark);
				arapaccountauditlog.set("source_order", "转账单");
				arapaccountauditlog.set("account_id", out_filter);
				arapaccountauditlog.save();
			}
			renderJson(transferaccounts);
		}
		@RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_CREATE, PermissionConstant.PERMSSION_COSTREIMBURSEMENT_UPDATE}, logical=Logical.OR)
		public void save() {
			TransferAccounts transferaccounts =null;
			String method =getPara("transfer_filter");
			String in_filter =getPara("in_filter");
			String out_filter =getPara("out_filter");
			String amount =getPara("amount");
			String transfer_time =getPara("transfer_time");
			String remark =getPara("remark");
			String transferOrderId =getPara("transferOrderId");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name + "'");
			if(!"".equals(transferOrderId) && transferOrderId != null){
			    transferaccounts =TransferAccounts.dao.findById(transferOrderId);
				transferaccounts.set("create_id",users.get(0).get("id"));
				transferaccounts.set("create_stamp",new Date());
				transferaccounts.set("transfer_method",method);
				transferaccounts.set("bank_in",in_filter);
				transferaccounts.set("bank_out",out_filter);
				transferaccounts.set("amount",amount);
				transferaccounts.set("transfer_stamp",transfer_time);
				transferaccounts.set("remark",remark);
				transferaccounts.update();
			}
			else{
			    transferaccounts= new TransferAccounts();
				transferaccounts.set("order_no", OrderNoGenerator.getNextOrderNo("ZZSQ"));
				transferaccounts.set("STATUS","新建");
				transferaccounts.set("create_id",users.get(0).get("id"));
				transferaccounts.set("create_stamp",new Date());
				transferaccounts.set("transfer_method",method);
				transferaccounts.set("bank_in",in_filter);
				transferaccounts.set("bank_out",out_filter);
				transferaccounts.set("amount",amount);
				transferaccounts.set("transfer_stamp",transfer_time);
				transferaccounts.set("remark",remark);
				transferaccounts.save();
			}
			renderJson(transferaccounts);
		}
}
