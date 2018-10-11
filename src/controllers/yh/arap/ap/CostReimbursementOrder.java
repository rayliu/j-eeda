package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.DeliveryOrderMilestone;
import models.FinItem;
import models.Office;
import models.UserLogin;
import models.yh.arap.ReimbursementOrder;
import models.yh.arap.ReimbursementOrderFinItem;

import org.apache.commons.lang.StringUtils;
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
import controllers.yh.OfficeController;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostReimbursementOrder extends Controller {

	private Logger logger = Logger.getLogger(CostCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	 @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_LIST})
	public void index() {
		 setAttr("userId", LoginUserController.getLoginUserId(this));
		 render("/yh/arap/CostReimbursement/CostReimbursementList.html");
	}
	 @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_CREATE, PermissionConstant.PERMSSION_COSTREIMBURSEMENT_UPDATE}, logical=Logical.OR)
	public void create() {
		//Long office_id = OfficeController.getOfficeId(currentUser.getPrincipal().toString());
		 List<Record> offices = Db
					.find("select o.id,o.office_name,o.is_stop from office o where o.id in (select office_id from user_office where user_name='"
							+ currentUser.getPrincipal() + "')");
		setAttr("userOffices", offices);
		List<Record> itemList  = Db.find("select * from fin_item where type='报销费用' and parent_id !=0 ");
        setAttr("itemList", itemList);
        List<Record> parentItemList  = Db.find("select * from fin_item where type='报销费用' and parent_id =0 ");
        setAttr("parentItemList", parentItemList);
		render("/yh/arap/CostReimbursement/CostReimbursementEdit.html");
	}
	public void getFinAccount() {
			List<Record> locationList= Db.find("SELECT * from fin_account where type='ALL' or type='PAY'");
			renderJson(locationList);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_CREATE, PermissionConstant.PERMSSION_COSTREIMBURSEMENT_UPDATE}, logical=Logical.OR)
	public void saveReimbursementOrder() {
		String id = getPara("reimbursementId");
		String accId = getPara("accId");
		//String status = getPara("status");
		String accountName = getPara("account_name");
		String accountNo = getPara("account_no");
		String account_bank = getPara("account_bank");

		String remark = getPara("remark");
		String invoicePayment = getPara("invoice_payment");
		String payment_type = getPara("payment_type");
		String office_id = getPara("office_id");
		String orderNo = null;
		ReimbursementOrder rei = null;
		if (id == null || "".equals(id)) {
			// 单号
			orderNo = OrderNoGenerator.getNextOrderNo("YFBX");
			
			String name = (String) currentUser.getPrincipal();
			UserLogin users = UserLogin.dao
					.findFirst("select * from user_login where user_name='" + name + "'");
			Long userId = LoginUserController.getLoginUserId(this);
			rei = new ReimbursementOrder();
			String a =accId.replaceAll(" ", "");
			if(accId!=null){
				if(accId==""){
					accId = null;
				}
				rei.set("fin_account_id", accId);
			}
			if(StringUtils.isNotBlank(office_id)){
				rei.set("office_id", office_id);
			}
			
			rei.set("order_no", orderNo).set("status", "新建")
					.set("account_name", accountName).set("account_no", accountNo)
					.set("create_id", userId).set("account_bank", account_bank)
					.set("create_stamp", new Date()).set("remark", remark)
					.set("invoice_payment", invoicePayment).set("payment_type", payment_type)
					.save();
			
			DeliveryOrderMilestone milestone = new DeliveryOrderMilestone();
			milestone.set("status", "新建").set("create_by", userId)
					.set("create_stamp", new Date()).set("reimbursement_id", rei.getLong("id"))
					.save();
			
		} else {

			rei = ReimbursementOrder.dao.findById(id);
			if(accId!=null){
				if(accId==""){
					accId=null;
				}
				rei.set("fin_account_id", accId);
			}
			if(StringUtils.isNotBlank(office_id)){
				rei.set("office_id", office_id);
			}
			rei.set("account_name", accountName).set("account_no", accountNo).set("account_bank", account_bank)
				.set("invoice_payment", invoicePayment).set("payment_type", payment_type)
				.set("remark", remark).update();
		}
		
		renderJson(rei);
	}
	 @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_LIST})
	public void reimbursementList(){
		String orderNo = getPara("orderNo")==null?"":getPara("orderNo").trim();
		String status = getPara("status")==null?"":getPara("status").trim();
		String accountName = getPara("accountName")==null?"":getPara("accountName").trim();
		String office_name = getPara("office_name");
		String begin_time = getPara("begin_time");
		String end_time = getPara("end_time");
		String creator_name = getPara("creator_name");
		String sortColIndex = getPara("iSortCol_0");
	    String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        if(StringUtils.isNotBlank(office_name)){
        	office_name = " and off.office_name like '%"+office_name+"%'";
        }else{
        	office_name = "";
        }
        if(StringUtils.isEmpty(begin_time)){
        	begin_time = "2000-01-01";
        }
        if(StringUtils.isNotEmpty(end_time)){
        	end_time += " 23:59:59"; 
        }else{
        	end_time = "2037-01-01";
        }
        String sqlTotal = "";
        String sql = "";

        	sqlTotal = "select count(1) total from reimbursement_order ro "
        			+ " left join reimbursement_order_fin_item rofi on rofi.order_id = ro.id "
        			+ " left join user_login us on us.id  = ro.create_id "
        			+ " left join office off on off.id = ro.office_id"
	        		+ " where ro.order_no like 'YFBX%' and ro.order_no like '%" + orderNo + "%'"
	        		+ " and ro.status like '%" + status + "%' "+ office_name
	        		+ " and us.c_name like '%" + creator_name + "%'"
	        		+ " and (ro.create_stamp between '" + begin_time + "' and '" + end_time + "')"
        			+ " and ifnull(ro.account_name,'') like '%" + accountName + "%'"
        			+ " and ro.office_id IN ( SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"')";
	        sql = "select ro.*,off.office_name,fi.name f_name,(select sum(revocation_amount) from reimbursement_order_fin_item where order_id = ro.id) amount,"
	        		+ " (select ifnull(c_name, user_name) from user_login where id = ro.create_id) createName,"
	        		+ " (select ifnull(c_name, user_name) from user_login where id = ro.audit_id) auditName,"
	        		+ " (select ifnull(c_name, user_name) from user_login where id = ro.approval_id)  approvalName"
	        		+ " from reimbursement_order ro left join reimbursement_order_fin_item rofi on rofi.order_id = ro.id "
	        		+ " left join user_login u on u.id  = ro.audit_id "
	        		+ " left join user_login us on us.id  = ro.create_id "
	        		+ " LEFT JOIN fin_item fi ON fi.id = rofi.fin_item_id"
	        		+ " left join office off on off.id = ro.office_id"
	        		+ " where ro.order_no like 'YFBX%' and ro.order_no like '%" + orderNo + "%'"
	        		+ " and ro.status like '%" + status + "%'"
	        		+ " and us.c_name like '%" + creator_name + "%'"
	        		+ " and (ro.create_stamp between '" + begin_time + "' and '" + end_time + "')"
	        		+ " and ifnull(ro.account_name,'') like '%" + accountName + "%'"
	        		+ office_name
	        		+ " and ro.office_id IN ( SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"')"
	        		+ " group by ro.id ";
        
        
		Record rec = Db.findFirst(sqlTotal);
		String orderByStr = " order by ro.create_stamp desc ";
	    if(colName.length()>0){
	        orderByStr = " order by "+colName+" "+sortBy;
	    }
		List<Record> orders = Db.find(sql+ orderByStr + sLimit);
        HashMap orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
	}
	
	public void edit(){
		String id = getPara("id");
		ReimbursementOrder rei = ReimbursementOrder.dao.findById(id);
		Long office_id = rei.getLong("office_id");
		if(office_id != null){
			Office off = Office.dao.findById(office_id);
			if(off != null){
				setAttr("re_office_name", off.getStr("office_name"));
			}
		}
		
		setAttr("rei", rei);
		Account acc=Account.dao.findById(rei.getLong("fin_account_id"));
		setAttr("acc",acc);
		//创建人
		UserLogin create = UserLogin.dao
				.findFirst("select * from user_login where id='" + rei.getLong("create_id") + "'");
		setAttr("createName", create.getStr("c_name"));
		List<Record> itemList  = Db.find("select * from fin_item where type='报销费用' and parent_id != 0");
        setAttr("itemList", itemList);
        
        List<Record> parentItemList  = Db.find("select * from fin_item where type='报销费用' and parent_id = 0");
        setAttr("parentItemList", parentItemList);
		
		render("/yh/arap/CostReimbursement/CostReimbursementEdit.html");
	}
	
	public void findUser(){
		String userId = getPara("userId");
		UserLogin approval = UserLogin.dao
				.findFirst("select * from user_login where id='" + userId + "'");
		renderJson(approval);
	}
	 @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTREIMBURSEMENT_CONFIRM})
	public void updateReimbursement(){
		String reimbursementId = getPara("reimbursementId");
		String btntTxt = getPara("btntTxt");
		String name = (String) currentUser.getPrincipal();
		UserLogin users = UserLogin.dao
				.findFirst("select * from user_login where user_name='" + name + "'");
		ReimbursementOrder rei = ReimbursementOrder.dao.findById(reimbursementId);
		DeliveryOrderMilestone milestone = new DeliveryOrderMilestone();
		milestone.set("create_by", users.getLong("id")).set("create_stamp", new Date()).set("reimbursement_id", rei.getLong("id"));
		if("审核".equals(btntTxt)){
			rei.set("status", "已审核").set("audit_id", users.getLong("id"))
			.set("audit_stamp", new Date()).update();
			milestone.set("status", "已审核").save();
		}else if("审批".equals(btntTxt)){
			rei.set("status", "已审批").set("approval_id", users.getLong("id"))
			.set("approval_stamp", new Date()).update();
			milestone.set("status", "已审批").save();
		}else if("取消审核".equals(btntTxt)){
			rei.set("status", "取消审核").set("audit_id", null)
			.set("audit_stamp", null).update();
			milestone.set("status", "取消审核").save();
		}else if("取消审批".equals(btntTxt)){
			rei.set("status", "取消审批").set("approval_id", null)
			.set("approval_stamp", null).update();
			milestone.set("status", "取消审批").save();
		}
		renderJson(rei);
	} 
	
	// 应付list
    public void accountPayable() {
        String id = getPara();
        if (id == null || id.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(0) total from reimbursement_order_fin_item ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select d.*, f1.name item,f2.name parent_item, f2.id parent_item_id from reimbursement_order_fin_item d "
        		+ " left join fin_item f1 on d.fin_item_id = f1.id"
        		+ " left join fin_item f2 on f2.id = f1.parent_id"
                + " where d.order_id =" + id);

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }
	//新增应付
    public void addNewRow() {
        List<FinItem> items = new ArrayList<FinItem>();
        String orderId = getPara();
       FinItem item = FinItem.dao.findFirst("select * from fin_item where type = '应付' order by id asc");
       if(item != null){
        	ReimbursementOrderFinItem dFinItem = new ReimbursementOrderFinItem();
	        //dFinItem.set("fin_item_id", item.get("id"))
	        //.set("fin_attribution_id", null)
	        dFinItem.set("order_id", orderId)
	        .save();
        }
        items.add(item);
        renderJson(items);
    }
    //修改应付
    public void updateReimbursementOrderFinItem(){
    	String paymentId = getPara("paymentId");
    	String name = getPara("name");
    	String value = getPara("value");
    	ReimbursementOrder rei = null;
    	if("revocation_amount".equals(name) && "".equals(value)){
    		value = "0";
    	}
    	if("invoice_amount ".equals(name) && "".equals(value)){
    		value = "0";
    	}
    	if(paymentId != null && !"".equals(paymentId)){
    		ReimbursementOrderFinItem reimbursementOrderFinItem = ReimbursementOrderFinItem.dao.findById(paymentId);
    		reimbursementOrderFinItem.set(name, value);
    		reimbursementOrderFinItem.update();
    		rei = ReimbursementOrder.dao.findById(reimbursementOrderFinItem.getStr("order_id"));
    		if("revocation_amount".equals(name) && !"0".equals(value)){
    			Record rec = Db.findFirst("select sum(revocation_amount) amount from reimbursement_order_fin_item where order_id = "+ reimbursementOrderFinItem.getStr("order_id"));
    			rei.set("amount", rec.getDouble("amount")).update();
    		} 
    	}
        renderJson(rei);
    }
    // 删除应付 
    public void finItemdel() {
        String id = getPara();
        ReimbursementOrderFinItem.dao.deleteById(id);
        renderJson("{\"success\":true}");
    }
    
    // 应付list
    public void findAllMilestone() {
        String id = getPara();
        if (id == null || id.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(0) total from delivery_order_milestone where reimbursement_id = " + id;
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select d.*,u.user_name,u.c_name from delivery_order_milestone d left join user_login u on u.id = d.create_by where d.reimbursement_id = " + id);

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
        
    }
    
    public void findItem(){
    	String name = getPara("name");
    	String value = getPara("value");
    	
    	List<Record> parentItemList  = Db.find("select * from fin_item where type='报销费用' and parent_id !=0 and parent_id = '"+value+"'");
    	renderJson(parentItemList);
    }
    
    @Before(Tx.class)
    public void delete(){
    	String id = getPara("id");
    	
    	Db.update("delete from reimbursement_order_fin_item where order_id = ?",id);
    	Db.update("delete from delivery_order_milestone where reimbursement_id = ?",id);
    	Db.deleteById("reimbursement_order", id);
    	
    	renderJson("{\"success\":true}");
    }
    
    @Before(Tx.class)
    public void lock(){
    	String id = getPara("id");
    	Db.update("update reimbursement_order set lock_flag = 'Y' where id = ?",id);
    	renderJson("{\"success\":true}");
    }
    
}
