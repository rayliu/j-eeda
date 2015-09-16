package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapCostOrder;
import models.Location;
import models.Party;
import models.UserLogin;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ArapMiscCostOrderItem;
import models.yh.arap.prePayOrder.ArapPrePayOrder;
import models.yh.arap.prePayOrder.ArapPrePayOrderItem;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.util.LocationUtil;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PrePayOrderController extends Controller {
    private Logger logger = Logger.getLogger(PrePayOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_PrePayOrder_LIST})
    public void index() {
    	   render("/yh/arap/PrePayOrder/PrePayOrderList.html");
    }

    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PrePayOrder_LIST})
    public void list() {
        String spName = getPara("spName");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String orderNo = getPara("orderNo");
//        String status = getPara("status");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String sql = "select appo.*, c.abbr as sp_name, ifnull(ul.c_name, ul.user_name) creator_name from arap_pre_pay_order appo"
        		+ " left join user_login ul on appo.creator = ul.id"
				+ " left join party p on appo.sp_id = p.id"
				+ " left join contact c on p.contact_id = c.id";
        logger.debug("sql:" + sql);
        String condition = "";
        if(spName != null || beginTime != null || endTime != null|| orderNo != null){
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
			
			condition = " where "
					+ " ifnull(c.abbr,'') like '%" + spName + "%' "
					+ " and appo.order_no like '%" + orderNo + "%' "
					+ " and appo.create_date between '" + beginTime + "' and '" + endTime+ " 23:59:59' ";
        }

        String sqlTotal = "select count(1) total from ("+sql+ condition+") A";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql+ condition + " order by appo.create_date desc " +sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PrePayOrder_CREATE})
	public void create() {
		String ids = getPara("ids");
		if(ids != null && !"".equals(ids)){
			String[] idArray = ids.split(",");
			logger.debug(String.valueOf(idArray.length));
	
			setAttr("costCheckOrderIds", ids); 
			ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(idArray[0]);
			Long spId = arapCostOrder.get("payee_id");
			if (!"".equals(spId) && spId != null) {
				Party party = Party.dao.findById(spId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get("contact_id").toString());
				setAttr("sp", contact); 
			}
		}

		setAttr("saveOK", false);
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));
		
		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("receivableItemList", receivableItemList);
		setAttr("status", "new");
		
		List<Record> itemList = Collections.emptyList();
		setAttr("itemList", itemList);
		
			render("/yh/arap/PrePayOrder/PrePayOrderEdit.html");
	}
    
    private void deleteRefOrder(String originOrderId) throws Exception {
		ArapMiscCostOrder refOrder = ArapMiscCostOrder.dao.findFirst(
				"select * from arap_misc_cost_order where ref_order_id=?",
				originOrderId);
		if (refOrder == null)
			return;
		if (!"新建".equals(refOrder.getStr("status"))) {
			throw new Exception("对应的手工成本单已不是“新建”，不能修改本张单据。");
		}
		long refOrderId = refOrder.getLong("id");
		Db.update(
				"delete from arap_misc_cost_order_item where misc_order_id = ?",
				refOrderId);
		refOrder.delete();
	} 
    
    private ArapMiscCostOrder buildNewPrePayOrder(ArapMiscCostOrder originOrder,
			UserLogin user) throws IllegalAccessException,
			InvocationTargetException {
		long originId = originOrder.getLong("id");
		
        ArapMiscCostOrder orderDest = new ArapMiscCostOrder();
		orderDest.set("status", "新建");
		if (!"".equals(originOrder.get("customer_id")) && originOrder.get("customer_id") != null) {
			orderDest.set("customer_id",originOrder.get("customer_id"));
		}
		if (originOrder.getStr("sp_id") != null && !"".equals(originOrder.getStr("sp_id"))) {
			orderDest.set("sp_id",originOrder.getStr("sp_id"));
		}
		orderDest.set("others_name",originOrder.getStr("others_name"));
		orderDest.set("ref_no",originOrder.getStr("ref_no"));
		orderDest.set("type", originOrder.getStr("type"));
		orderDest.set("cost_to_type", originOrder.getStr("cost_to_type"));
		orderDest.set("route_from", originOrder.getStr("route_from"));
		orderDest.set("route_to", originOrder.getStr("route_to"));
		orderDest.set("remark", originOrder.getStr("remark"));
		orderDest.set("audit_status", "新建");
		orderDest.set("create_by", user.getLong("id"));
		orderDest.set("create_stamp", new Date());
		orderDest.set("order_no", OrderNoGenerator.getNextOrderNo("SGFK"));
		if (originOrder.getDouble("total_amount") != null && !"".equals(originOrder.getDouble("total_amount"))) {
			orderDest.set("total_amount", 0-originOrder.getDouble("total_amount"));
		}
		orderDest.set("ref_order_no", originOrder.getStr("order_no"));
		orderDest.set("ref_order_id", originOrder.getLong("id"));
		orderDest.save();

		List<ArapMiscCostOrderItem> originItems = ArapMiscCostOrderItem.dao
				.find("select * from arap_misc_cost_order_item where misc_order_id = ?",
						originId);

		for (ArapMiscCostOrderItem originItem : originItems) {
			ArapMiscCostOrderItem newItem = new ArapMiscCostOrderItem();
		    newItem.set("status", "新建");
			newItem.set("fin_item_id", originItem.get("fin_item_id"));
			if(originItem.getDouble("AMOUNT")!=null)
			newItem.set("amount", 0-originItem.getDouble("AMOUNT"));
			newItem.set("creator", user.getLong("id"));
			newItem.set("create_date", new Date());
			newItem.set("customer_order_no", originItem.get("CUSTOMER_ORDER_NO"));
			newItem.set("item_desc", originItem.get("ITEM_DESC"));
			newItem.set("misc_order_id", orderDest.getLong("id"));
			newItem.save();
		}
		return orderDest;
	}
    
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PrePayOrder_CREATE,PermissionConstant.PERMSSION_PrePayOrder_UPDATE},logical=Logical.OR)
    @Before(Tx.class)
	public void save() throws Exception {		
		String jsonStr=getPara("params");
    	logger.debug(jsonStr);
    	 Gson gson = new Gson();  
         Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
         
        ArapPrePayOrder arapPrePayOrder = new ArapPrePayOrder();
		String orderId = (String) dto.get("orderId");
		String spId = (String) dto.get("sp_id");
		String ref_no = (String) dto.get("ref_no");
		String remark = (String) dto.get("remark");
		Double amount = (Double) dto.get("amount");
		UserLogin user = LoginUserController.getLoginUser(this);
		
		
		if (!"".equals(orderId) && orderId != null) {
			arapPrePayOrder = ArapPrePayOrder.dao.findById(orderId);
			
			if (spId != null && !"".equals(spId)) {
				arapPrePayOrder.set("sp_id",spId);
			}
			arapPrePayOrder.set("ref_no",ref_no);
			arapPrePayOrder.set("remark", remark);
			if (amount != null && !"".equals(amount)) {
				arapPrePayOrder.set("total_amount", amount);
			}
			
			arapPrePayOrder.update();
		} else {
			arapPrePayOrder = new ArapPrePayOrder();
			arapPrePayOrder.set("status", "新建");
			if (spId != null && !"".equals(spId)) {
				arapPrePayOrder.set("sp_id",spId);
			}
			
			arapPrePayOrder.set("ref_no",ref_no);
			arapPrePayOrder.set("remark", remark);
			arapPrePayOrder.set("office_id", user.getLong("office_id"));
			arapPrePayOrder.set("creator", user.getLong("id"));
			arapPrePayOrder.set("create_date", new Date());
			arapPrePayOrder.set("order_no", OrderNoGenerator.getNextOrderNo("YF"));
			
			if (amount != null && !"".equals(amount)) {
				arapPrePayOrder.set("total_amount", amount);
			}	
			arapPrePayOrder.save();
		}
		
		List<Map> items =  (List<Map>) dto.get("items");
		for(Map item : items){
			String itemId = (String) item.get("ID");
			String action = (String) item.get("ACTION");
			if("".equals(itemId)){
				ArapPrePayOrderItem arapPrePayOrderItem = new ArapPrePayOrderItem();
				arapPrePayOrderItem.set("order_id", arapPrePayOrder.getLong("id"));
				arapPrePayOrderItem.set("status", "新建");
				arapPrePayOrderItem.set("fin_item_id", item.get("FIN_ITEM_ID"));
				arapPrePayOrderItem.set("amount", item.get("AMOUNT"));
				arapPrePayOrderItem.set("item_desc", item.get("ITEM_DESC"));
				arapPrePayOrderItem.save();
			}else{
				ArapPrePayOrderItem arapPrePayOrderItem = ArapPrePayOrderItem.dao.findById(itemId);
				if("DELETE".equals(action)){
					arapPrePayOrderItem.delete();
				}else{
					arapPrePayOrderItem.set("fin_item_id", item.get("FIN_ITEM_ID"));
					arapPrePayOrderItem.set("amount", item.get("AMOUNT"));
					arapPrePayOrderItem.set("item_desc", item.get("ITEM_DESC"));
					arapPrePayOrderItem.update();
				}
			}
		}
		
		ArapPrePayOrder destOrder = null;
		
		if (!"".equals(orderId) && orderId != null) {// update
			// 是就删除整张单，不是则提示应为往来单已复核，不能改变
//			deleteRefOrder(costMiscOrderId);
//			destOrder = buildNewPrePayOrder(arapPrePayOrder, user);
		} else {// new
//			destOrder = buildNewPrePayOrder(arapPrePayOrder, user);
		}

		if(destOrder!=null){
			arapPrePayOrder.set("ref_order_no", destOrder.getStr("order_no"));
			arapPrePayOrder.set("ref_order_id", destOrder.getLong("id"));
			arapPrePayOrder.update();
		}
		renderJson(arapPrePayOrder);
	}
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PrePayOrder_UPDATE})
	public void edit() {
		String id = getPara("id");
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("receivableItemList", receivableItemList);
		
		ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao.findById(id);
		Long spId = arapPrePayOrder.get("sp_id");
		if (!"".equals(spId) && spId != null) {
			Record rec = Db.findFirst("select c.company_name from party p, contact c where"
					+ " p.contact_id = c.id and p.id=? ", spId); 
			
			setAttr("sp_name", rec.getStr("company_name")); 
		}
		
			
		UserLogin userLogin = UserLogin.dao.findById(arapPrePayOrder.get("creator"));
		setAttr("creator_name", userLogin.getStr("c_name"));
		
		setAttr("arapPrePayOrder", arapPrePayOrder);
		
		
		// 获取当前页的数据
		List<Record> itemList = Db.find("select appoi.*, fi.name name "
					+ " from arap_pre_pay_order_item appoi"
					+ " left join fin_item fi on appoi.fin_item_id = fi.id"
					+ " where appoi.order_id = '"+ id +"' ");
		setAttr("itemList", itemList);
		render("/yh/arap/PrePayOrder/PrePayOrderEdit.html");
	}
    
	public void searchAllAccount(){
		List<Account> accounts = Account.dao.find("select * from fin_account where type != 'REC' and bank_name != '现金'");
		renderJson(accounts);
	}
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_PrePayOrder_CANCEL})
	public void cancel(){
		
	}
}
