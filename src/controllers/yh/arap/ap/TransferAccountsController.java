package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapAccountAuditLog;
import models.UserLogin;
import models.yh.arap.ArapAccountAuditSummary;
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
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
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
		@Before(Tx.class)
		public void confirem() {
			String transferOrderId =getPara("transferOrderId");
			String in_filter =getPara("in_filter");
			String out_filter =getPara("out_filter");
			String amount =getPara("amount");
			String remark =getPara("remark");
			String transfer_time =getPara("transfer_time");
			
			if( transfer_time==null||transfer_time.equals("")){
				transfer_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	   		}
			
			
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
				arapaccountauditlog.set("create_date",transfer_time);
				arapaccountauditlog.set("remark", remark);
				arapaccountauditlog.set("source_order", "转账单");
				arapaccountauditlog.set("account_id", in_filter);
				arapaccountauditlog.set("invoice_order_id", transferOrderId);
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
				arapaccountauditlog.set("create_date",transfer_time);
				arapaccountauditlog.set("remark", remark);
				arapaccountauditlog.set("source_order", "转账单");
				arapaccountauditlog.set("account_id", out_filter);
				arapaccountauditlog.set("invoice_order_id", transferOrderId);
				arapaccountauditlog.save();
			}
			
			//日记账金额更新
			updateAccountSummary(amount, Long.parseLong(out_filter),Long.parseLong(in_filter) ,transfer_time);
			
			renderJson(transferaccounts);
		}
		
		
		//更新日记账的账户期初结余
		//本期结余 = 期初结余 + 本期总收入 - 本期总支出
		@Before(Tx.class)
		private void updateAccountSummary(String amount, Long out_filter, Long in_filter,  String pay_time) {
			Calendar cal = Calendar.getInstance();  
			int this_year = cal.get(Calendar.YEAR);  
			int this_month = cal.get(Calendar.MONTH)+1;  
			String year = pay_time.substring(0, 4);
			String month = pay_time.substring(5, 7);
			DecimalFormat df = new DecimalFormat("#.00");
			if(String.valueOf(this_year).equals(year) && String.valueOf(this_month).equals(month)){
				
				//支出
				ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
						"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
						, out_filter, year, month);
				if(aaas!=null){
					Double total_cost = aaas.getDouble("total_cost") + Double.parseDouble(amount);
					Double balance_amount = aaas.getDouble("balance_amount") - Double.parseDouble(amount);
					aaas.set("total_cost",df.format(total_cost));
					aaas.set("balance_amount",df.format(balance_amount));
					aaas.update();
				}else{
					ArapAccountAuditSummary newSummary = new ArapAccountAuditSummary();
				}
				
				//收入
				ArapAccountAuditSummary aaas2 = ArapAccountAuditSummary.dao.findFirst(
						"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
						, in_filter, year, month);
				if(aaas2!=null){
					Double total_charge = aaas2.getDouble("total_charge") + Double.parseDouble(amount);
					Double balance_amount = aaas2.getDouble("balance_amount") + Double.parseDouble(amount);
					aaas2.set("total_charge",df.format(total_charge) );
					aaas2.set("balance_amount",df.format(balance_amount));
					aaas2.update();
				}else{
					ArapAccountAuditSummary newSummary = new ArapAccountAuditSummary();
				}
			}else{
				
				//支出
				ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
						"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
						, out_filter, year, month);
				
				if(aaas!=null){
					Double total_cost = aaas.getDouble("total_cost") + Double.parseDouble(amount);
					Double balance_amount = aaas.getDouble("balance_amount") - Double.parseDouble(amount);
					aaas.set("total_cost",df.format(total_cost));
					aaas.set("balance_amount",df.format(balance_amount));
					aaas.update();
					
					for(int i = 1 ;i<=(this_month - Integer.parseInt(month)); i++){
						ArapAccountAuditSummary this_aaas = ArapAccountAuditSummary.dao.findFirst(
								"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
								, out_filter, this_year, Integer.parseInt(month)+i);
						Double init_amount = this_aaas.getDouble("init_amount") - Double.parseDouble(amount);
						Double balance_amount2 =  this_aaas.getDouble("balance_amount") - Double.parseDouble(amount);
						this_aaas.set("init_amount", df.format(init_amount));
						this_aaas.set("balance_amount",df.format(balance_amount2));
						this_aaas.update();
					}
				}
				
				//收入
				ArapAccountAuditSummary aaas1 = ArapAccountAuditSummary.dao.findFirst(
						"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
						, in_filter, year, month);
				
				if(aaas1!=null){
					Double total_charge = aaas1.getDouble("total_charge") + Double.parseDouble(amount);
					Double balance_amount = aaas1.getDouble("balance_amount") + Double.parseDouble(amount);;
					aaas1.set("total_charge",df.format(total_charge) );
					aaas1.set("balance_amount",df.format(balance_amount));
					aaas1.update();
				
					
					for(int i = 1 ;i<=(this_month - Integer.parseInt(month)); i++){
						ArapAccountAuditSummary this_aaas = ArapAccountAuditSummary.dao.findFirst(
								"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
								, in_filter, this_year, Integer.parseInt(month)+i);
						
						Double init_amount = this_aaas.getDouble("init_amount") + Double.parseDouble(amount);
						Double balance_amount2 = this_aaas.getDouble("balance_amount") + Double.parseDouble(amount);
						this_aaas.set("init_amount",df.format(init_amount));
						this_aaas.set("balance_amount", df.format(balance_amount2));
						this_aaas.update();	
					}
					
				}
				
				
			}
		}
		
		
		
		//付款确认退回
		//同时更新日记账里面的数据（退回金额）
		@Before(Tx.class)
	    public void returnConfirmOrder(){
	        String orderId=getPara("id");
	        TransferAccountsOrder transferAccountsOrder = TransferAccountsOrder.dao.findById(orderId);
	        transferAccountsOrder.set("status", "新建");
	        transferAccountsOrder.set("return_confirm_by", LoginUserController.getLoginUserId(this));
	        transferAccountsOrder.set("return_confirm_stamp", new Date());
	        transferAccountsOrder.update();
	       
			
			//撤销对应日记账信息
			Double amount = transferAccountsOrder.getDouble("amount");
			String transfer_time = transferAccountsOrder.getDate("transfer_stamp").toString();
			Long bank_out = transferAccountsOrder.getLong("bank_out");
			Long bank_in = transferAccountsOrder.getLong("bank_in");
			
			ArapAccountAuditLog arapAccountAuditLog = ArapAccountAuditLog.dao.findFirst("select * from arap_account_audit_log where source_order = '转账单' and payment_type = 'COST' and invoice_order_id = ? ",orderId);
			Double out = arapAccountAuditLog.getDouble("amount");
			arapAccountAuditLog.delete();
			ArapAccountAuditLog arapAccountAuditLog2 = ArapAccountAuditLog.dao.findFirst("select * from arap_account_audit_log where source_order = '转账单' and payment_type = 'CHARGE' and invoice_order_id = ? ",orderId);
			Double in = arapAccountAuditLog2.getDouble("amount");
			arapAccountAuditLog2.delete();
			
			////撤销功能是更新对应金额
			deleteAccountSummaryIn(amount.toString(), bank_in ,transfer_time);    //转入撤销
			deleteAccountSummaryOut(amount.toString(), bank_out ,transfer_time);   //转出撤销
			renderJson("{\"success\":true}");
	        
	    }
		
		//撤销功能是更新对应金额
		//更新对应金额
		//更新日记账的账户期初结余
		//本期结余 = 期初结余 + 本期总收入 - 本期总支出
		@Before(Tx.class)
		private void deleteAccountSummaryOut(String pay_amount, Long acountId, String pay_time) {
			Calendar cal = Calendar.getInstance();  
			int this_year = cal.get(Calendar.YEAR);  
			int this_month = cal.get(Calendar.MONTH)+1;  
			String year = pay_time.substring(0, 4);
			String month = pay_time.substring(5, 7);
			DecimalFormat df = new DecimalFormat("#.00");
			if(String.valueOf(this_year).equals(year) && String.valueOf(this_month).equals(month)){
				ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
						"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
						, acountId, year, month);
				if(aaas!=null){
					Double total_cost = aaas.getDouble("total_cost") - Double.parseDouble(pay_amount);
					aaas.set("total_cost",df.format(total_cost));
					
					Double balance_amount = aaas.getDouble("balance_amount") + Double.parseDouble(pay_amount);
					aaas.set("balance_amount", df.format(balance_amount));
					aaas.update();
				}
			}else{
				ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
						"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
						, acountId, year, month);
				
				if(aaas!=null){
					Double total_cost = aaas.getDouble("total_cost") - Double.parseDouble(pay_amount);
					aaas.set("total_cost", df.format(total_cost));
					
					Double balance_amount = aaas.getDouble("balance_amount") + Double.parseDouble(pay_amount);
					aaas.set("balance_amount", df.format(balance_amount));
					aaas.update();
					
					for(int i = 1 ;i<=(this_month - Integer.parseInt(month)); i++){
						ArapAccountAuditSummary this_aaas = ArapAccountAuditSummary.dao.findFirst(
								"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
								, acountId, this_year, Integer.parseInt(month)+i);
						Double init_amount = this_aaas.getDouble("init_amount") + Double.parseDouble(pay_amount);
						Double balance_amount2 = this_aaas.getDouble("balance_amount") + Double.parseDouble(pay_amount);
						this_aaas.set("init_amount", df.format(init_amount));
						this_aaas.set("balance_amount",df.format(balance_amount2));
						this_aaas.update();
					}
				}
			}
		}
		
		
		//撤销功能是更新对应金额
		//更新对应金额
		//更新日记账的账户期初结余
		//本期结余 = 期初结余 + 本期总收入 - 本期总支出
		@Before(Tx.class)
		private void deleteAccountSummaryIn(String receive_amount, Long acountId, String receive_time) {
			Calendar cal = Calendar.getInstance();  
			int this_year = cal.get(Calendar.YEAR);  
			int this_month = cal.get(Calendar.MONTH)+1;  
			String year = receive_time.substring(0, 4);
			String month = receive_time.substring(5, 7);
			DecimalFormat df = new DecimalFormat("#.00");
			if(String.valueOf(this_year).equals(year) && String.valueOf(this_month).equals(month)){
				ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
						"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
						, acountId, year, month);
				if(aaas!=null){
					Double total_charge = aaas.getDouble("total_charge") - Double.parseDouble(receive_amount);
					aaas.set("total_charge",df.format(total_charge));
					
					Double balance_amount = aaas.getDouble("balance_amount") - Double.parseDouble(receive_amount);
					aaas.set("balance_amount", df.format(balance_amount));
					aaas.update();
				}
			}else{
				ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
						"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
						, acountId, year, month);
				
				if(aaas!=null){
					Double total_charge = aaas.getDouble("total_charge") - Double.parseDouble(receive_amount);
					aaas.set("total_charge", df.format(total_charge));
					
					Double balance_amount = aaas.getDouble("balance_amount") - Double.parseDouble(receive_amount);
					aaas.set("balance_amount", df.format(balance_amount));
					aaas.update();
					
					for(int i = 1 ;i<=(this_month - Integer.parseInt(month)); i++){
						ArapAccountAuditSummary this_aaas = ArapAccountAuditSummary.dao.findFirst(
								"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
								, acountId, this_year, Integer.parseInt(month)+i);
						Double init_amount = this_aaas.getDouble("init_amount") - Double.parseDouble(receive_amount);
						Double balance_amount2 = this_aaas.getDouble("balance_amount") - Double.parseDouble(receive_amount);
						this_aaas.set("init_amount", df.format(init_amount));
						this_aaas.set("balance_amount",df.format(balance_amount2));
						this_aaas.update();
					}
				}
			}
		}
		

		@RequiresPermissions(value = {PermissionConstant.PERMSSION_TA_CREATE, PermissionConstant.PERMSSION_TA_UPDATE}, logical=Logical.OR)
		public void save() {
			TransferAccountsOrder transferaccounts =null;
			String method =getPara("transfer_filter");
			String in_filter =getPara("in_filter");
			String out_filter =getPara("out_filter");
			String amount =getPara("amount");
			String transfer_time = getPara("transfer_time");
			String remark =getPara("remark");
			String transferOrderId =getPara("transferOrderId");
			
			if( transfer_time==null||transfer_time.equals("")){
				transfer_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	   		}
			
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
