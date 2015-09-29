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
import models.yh.arap.TransferAccountsOrder;

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
	 @RequiresPermissions(value = {PermissionConstant.PERMSSION_TA_LIST})
	public void index() {
		render("/yh/arap/TransferAccountsOrder/TransferAccountsOrderList.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_TA_CREATE, PermissionConstant.PERMSSION_TA_UPDATE}, logical=Logical.OR)
		public void create() {
			List<Record> itemList  = Db.find("select * from fin_item where type='报销费用' and parent_id !=0 ");
	        setAttr("itemList", itemList);
	        List<Record> parentItemList  = Db.find("select * from fin_item where type='报销费用' and parent_id =0 ");
	        setAttr("parentItemList", parentItemList);
			render("/yh/arap/TransferAccountsOrder/TransferAccountsOrderEdit.html");
		}
	public void findUser(){
		String userId = getPara("userId");
		UserLogin approval = UserLogin.dao
				.findFirst("select * from user_login where id='" + userId + "'");
		renderJson(approval);
	}
	@RequiresPermissions(value = { PermissionConstant.PERMSSION_TA_LIST })
	public void list(){
		String orderNo =getPara("orderNo");
		String transfer_method =getPara("transfer_method");
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = " SELECT count(1) total"
        			+ " FROM"
        			+ " transfer_accounts_order tao"
        			+ " LEFT JOIN fin_account fain ON fain.id = tao.bank_in"
        			+ " LEFT JOIN fin_account faout ON faout.id = tao.bank_out"
        			+ " LEFT JOIN user_login ul ON ul.id = tao.create_id";
        String sql=" SELECT tao.id id,tao.order_no order_no,tao.transfer_stamp transfer_stamp,tao.`STATUS` transfer_status,"
        			+ " tao.transfer_method transfer_method,fain.account_no account_in,"
        			+ " faout.account_no account_out,tao.amount,ul.c_name c_name,tao.remark remark"
        			+ " FROM"
        			+ " transfer_accounts_order tao"
        			+ " LEFT JOIN fin_account fain ON fain.id = tao.bank_in"
        			+ " LEFT JOIN fin_account faout ON faout.id = tao.bank_out"
        			+ " LEFT JOIN user_login ul ON ul.id = tao.create_id";
        String condition = "";
        
        if(orderNo != null || transfer_method != null){
        	condition =  " where ifnull(order_no,'') like '%" + orderNo + "%' "
        			+ " and ifnull(transfer_method,'') like '%" + transfer_method + "%' ";
        }
        Record rec = Db.findFirst(sqlTotal+condition);
        logger.debug("total records:" + rec.getLong("total"));
        
        long sTime = Calendar.getInstance().getTimeInMillis();
        List<Record> BillingOrders = Db.find(sql+condition+sLimit);
        long eTime = Calendar.getInstance().getTimeInMillis();
        logger.debug("time cost:" + (eTime-sTime));
        
        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
        BillingOrderListMap.put("aaData", BillingOrders);
        renderJson(BillingOrderListMap);
       
	}
	@RequiresPermissions(value = { PermissionConstant.PERMSSION_TA_UPDATE })
	public void edit(){
			String id = getPara("id");
			TransferAccountsOrder transferaccounts = TransferAccountsOrder.dao.findById(id);
			setAttr("transferaccounts", transferaccounts);
			Account in_acc=Account.dao.findById(transferaccounts.get("bank_in"));
			Account out_acc=Account.dao.findById(transferaccounts.get("bank_out"));
			setAttr("in_acc",in_acc);
			setAttr("out_acc",out_acc);
			//创建人
			if(!"".equals(transferaccounts.get("create_id")) &&transferaccounts.get("create_id")!=null){
			UserLogin create1 = UserLogin.dao
					.findFirst("select * from user_login where id='" + transferaccounts.get("create_id") + "'");
			setAttr("createName", create1.get("c_name"));
		   }
			if(!"".equals(transferaccounts.get("confirm_id")) &&transferaccounts.get("confirm_id")!=null){
				UserLogin create1 = UserLogin.dao
						.findFirst("select * from user_login where id='" + transferaccounts.get("confirm_id") + "'");
				setAttr("confirmName", create1.get("c_name"));
				setAttr("confirmStamp", transferaccounts.get("confirm_stamp"));
			}
			
		render("/yh/arap/TransferAccountsOrder/TransferAccountsOrderEdit.html");
	}
		@RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_CREATE, PermissionConstant.PERMSSION_COSTREIMBURSEMENT_UPDATE}, logical=Logical.OR)
		public void getFinAccount() {
			List<Record> locationList= Db.find("SELECT * from fin_account");
			renderJson(locationList);
		}
		@RequiresPermissions(value = {PermissionConstant.PERMSSION_TA_CREATE, PermissionConstant.PERMSSION_TA_UPDATE}, logical=Logical.OR)
		public void confirem() {
			String transferOrderId =getPara("transferOrderId");
			String in_filter =getPara("in_filter");
			String out_filter =getPara("out_filter");
			String amount =getPara("amount");
			String remark =getPara("remark");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name + "'");
			TransferAccountsOrder transferaccounts =TransferAccountsOrder.dao.findById(transferOrderId);
			if(!"".equals(transferOrderId) && transferOrderId != null){
			    transferaccounts =TransferAccountsOrder.dao.findById(transferOrderId);
			    transferaccounts.set("STATUS","已确认");
				transferaccounts.set("confirm_id",users.get(0).get("id"));
				transferaccounts.set("confirm_stamp",new Date());
				transferaccounts.update();
			}
			
			//更新账户金额
			if(!"".equals(in_filter) && in_filter != null){
				Account account= Account.dao.findById(in_filter);
				double account_aounmt=0.0;
				if(account.getDouble("amount")!=null){
					 account_aounmt =account.getDouble("amount");
				}
				else{
					account_aounmt=0;
				}
				
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
				arapaccountauditlog.set("transferOrder_id", transferOrderId);
				arapaccountauditlog.save();
			}
			if(!"".equals(out_filter) && in_filter != null){
				ArapAccountAuditLog arapaccountauditlog = new ArapAccountAuditLog();
				Account account= Account.dao.findById(out_filter);
				double account_aounmt=0.0;
				if(account.getDouble("amount")!=null){
					 account_aounmt =account.getDouble("amount");
				}
				else{
					account_aounmt=0;
				}
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
				arapaccountauditlog.set("transferOrder_id", transferOrderId);
				arapaccountauditlog.save();
			}
			renderJson(transferaccounts);
		}
		@RequiresPermissions(value = {PermissionConstant.PERMSSION_TA_CREATE, PermissionConstant.PERMSSION_TA_UPDATE}, logical=Logical.OR)
		public void save() {
			TransferAccountsOrder transferaccounts =null;
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
			    transferaccounts =TransferAccountsOrder.dao.findById(transferOrderId);
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
			    transferaccounts= new TransferAccountsOrder();
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
