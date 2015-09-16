package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;
import models.DepartOrder;
import models.Location;
import models.ParentOfficeModel;
import models.Party;
import models.UserLogin;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ArapMiscCostOrderItem;
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

import controllers.yh.LoginUserController;
import controllers.yh.util.LocationUtil;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
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
    	String sp = getPara("sp");
        String customer = getPara("companyName");
        String spName = getPara("spName");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_misc_cost_order amco"
        		+ " left join arap_cost_order aco on aco.id = amco.cost_order_id"
        		+ " left join party p1 on amco.customer_id = p1.id"
				+ " left join party p2 on amco.sp_id = p2.id"
				+ " left join contact c1 on p1.contact_id = c1.id"
				+ " left join contact c2 on p2.contact_id = c2.id";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        String sql = "select amco.*,aco.order_no cost_order_no from arap_misc_cost_order amco"
					+ " left join arap_cost_order aco on aco.id = amco.cost_order_id "
					+ " left join party p1 on amco.customer_id = p1.id"
					+ " left join party p2 on amco.sp_id = p2.id"
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id";
        logger.debug("sql:" + sql);
        String condition = "";
        //TODO 始发地和目的地 客户没有做
        if(sp != null || customer != null || spName != null
        		|| status != null || beginTime != null || endTime != null|| orderNo != null){
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
			if (status!=null && "业务收款".equals(status)) {
				status = "biz";
			}
			if (status!=null && "非业务收款".equals(status)) {
				status = "non_biz";
			}
			condition = " where "
					+ " ifnull(c2.abbr,'') like '%" + spName + "%' "
					+ " and ifnull(c1.abbr,'') like '%" + customer + "%' "
					+ " and amco.order_no like '%" + orderNo + "%' "
					+ " and amco.create_stamp between '" + beginTime + "' and '" + endTime+ " 23:59:59' ";
			if(status!=null && status.length()>0){
				condition  += " and amco.type = '" + status + "' ";
			}
			
        }
        List<Record> BillingOrders = Db.find(sql+ condition + " order by amco.create_stamp desc " +sLimit);

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
	public void save() throws Exception {		
		String jsonStr=getPara("params");
    	logger.debug(jsonStr);
    	 Gson gson = new Gson();  
         Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
         
        ArapMiscCostOrder arapMiscCostOrder = new ArapMiscCostOrder();
		String costMiscOrderId = (String) dto.get("costMiscOrderId");
		String biz_type = (String) dto.get("biz_type");
		String customerId = (String) dto.get("customer_id");
		String spId = (String) dto.get("sp_id");
		String routeFrom = (String) dto.get("route_from");
		String routeTo = (String) dto.get("route_to");
		String others_name = (String) dto.get("others_name");
		String ref_no = (String) dto.get("ref_no");
		String remark = (String) dto.get("remark");
		Double amount = (Double) dto.get("amount");
		String cost_to_type = (String) dto.get("cost_to_type");
		UserLogin user = LoginUserController.getLoginUser(this);
		
		String old_biz_type = "";
		if (!"".equals(costMiscOrderId) && costMiscOrderId != null) {
			//TODO: 如果已经应付确认过，就不能修改了
			arapMiscCostOrder = ArapMiscCostOrder.dao.findById(costMiscOrderId);
			old_biz_type = arapMiscCostOrder.getStr("type");
			
			if (!"".equals(customerId) && customerId != null) {
				arapMiscCostOrder.set("customer_id",customerId);
			}
			if (spId != null && !"".equals(spId)) {
				arapMiscCostOrder.set("sp_id",spId);
			}
			arapMiscCostOrder.set("others_name",others_name);
			arapMiscCostOrder.set("ref_no",ref_no);
			arapMiscCostOrder.set("type", biz_type);
			arapMiscCostOrder.set("cost_to_type", cost_to_type);
			arapMiscCostOrder.set("route_from", routeFrom);
			arapMiscCostOrder.set("route_to", routeTo);
			arapMiscCostOrder.set("remark", remark);
			if (amount != null && !"".equals(amount)) {
				arapMiscCostOrder.set("total_amount", amount);
			}
			
			arapMiscCostOrder.update();
		} else {
			arapMiscCostOrder = new ArapMiscCostOrder();
			arapMiscCostOrder.set("status", "新建");
			if (!"".equals(customerId) && customerId != null) {
				arapMiscCostOrder.set("customer_id",customerId);
			}
			if (spId != null && !"".equals(spId)) {
				arapMiscCostOrder.set("sp_id",spId);
			}
			arapMiscCostOrder.set("others_name",others_name);
			arapMiscCostOrder.set("ref_no",ref_no);
			arapMiscCostOrder.set("type", biz_type);
			arapMiscCostOrder.set("cost_to_type", cost_to_type);
			arapMiscCostOrder.set("route_from", routeFrom);
			arapMiscCostOrder.set("route_to", routeTo);
			arapMiscCostOrder.set("remark", remark);
			arapMiscCostOrder.set("audit_status", "新建");
			arapMiscCostOrder.set("office_id", user.getLong("office_id"));
			arapMiscCostOrder.set("create_by", user.getLong("id"));
			arapMiscCostOrder.set("create_stamp", new Date());
			arapMiscCostOrder.set("order_no", OrderNoGenerator.getNextOrderNo("SGFK"));
			
			if (amount != null && !"".equals(amount)) {
				arapMiscCostOrder.set("total_amount", amount);
			}	
			arapMiscCostOrder.save();
		}
		
		Db.update(
				"delete from arap_misc_cost_order_item where misc_order_id = ?",
				costMiscOrderId);
		
		List<Map> items =  (List<Map>) dto.get("items");
		for(Map item : items){
			ArapMiscCostOrderItem arapMiscCostOrderItem = new ArapMiscCostOrderItem();
			arapMiscCostOrderItem.set("status", "新建");
			arapMiscCostOrderItem.set("fin_item_id", item.get("NAME"));
			arapMiscCostOrderItem.set("amount", item.get("AMOUNT"));
			arapMiscCostOrderItem.set("creator", user.getLong("id"));
			arapMiscCostOrderItem.set("create_date", new Date());
			arapMiscCostOrderItem.set("customer_order_no", item.get("CUSTOMER_ORDER_NO"));
			arapMiscCostOrderItem.set("item_desc", item.get("ITEM_DESC"));
			arapMiscCostOrderItem.set("misc_order_id", arapMiscCostOrder.getLong("id"));
			arapMiscCostOrderItem.save();
		}
		
		ArapMiscCostOrder destOrder = null;
		
		if (!"".equals(costMiscOrderId) && costMiscOrderId != null) {// update
			// 1. 是从biz->non_biz, 新生成对应往来单
			if ("biz".equals(old_biz_type) && "non_biz".equals(biz_type)) {
				destOrder = buildNewPrePayOrder(arapMiscCostOrder, user);
			} else if ("non_biz".equals(old_biz_type)
					&& "non_biz".equals(biz_type)) {
				// non_biz 不变，update 对应的信息，判断对应往来单状态是否是“新建”，
				// 是就删除整张单，不是则提示应为往来单已复核，不能改变
				deleteRefOrder(costMiscOrderId);
				destOrder = buildNewPrePayOrder(arapMiscCostOrder, user);
			} else if ("non_biz".equals(old_biz_type) && "biz".equals(biz_type)) {
				// non_biz -> biz 删除整张对应的单，判断对应往来单状态是否是“新建”，
				// 是就删除整张单，不是则提示应为往来单已复核，不能改变
				deleteRefOrder(costMiscOrderId);
			}
		} else {// new
			if("non_biz".equals(biz_type))
				destOrder = buildNewPrePayOrder(arapMiscCostOrder, user);
		}

		if(destOrder!=null){
			arapMiscCostOrder.set("ref_order_no", destOrder.getStr("order_no"));
			arapMiscCostOrder.set("ref_order_id", destOrder.getLong("id"));
			arapMiscCostOrder.update();
		}
		renderJson(arapMiscCostOrder);
	}
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PrePayOrder_UPDATE})
	public void edit() {
		String id = getPara("id");
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("receivableItemList", receivableItemList);
		
		ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(id);
		Long spId = arapMiscCostOrder.get("sp_id");
		if (!"".equals(spId) && spId != null) {
			Party party = Party.dao.findById(spId);
			setAttr("spParty", party);
			Contact contact = Contact.dao.findById(party.get("contact_id").toString());
			setAttr("spContact", contact); 
		}
		
		Long customerId = arapMiscCostOrder.get("customer_id");
		if (!"".equals(customerId) && customerId != null) {
			Party party = Party.dao.findById(customerId);
			setAttr("customerparty", party);
			Contact contact = Contact.dao.findById(party.get("contact_id").toString());
			setAttr("customerContact", contact); 
		}
		
		String routeFrom = arapMiscCostOrder.get("route_from");
		Location locationFrom = LocationUtil.getLocation(routeFrom);
		setAttr("locationFrom", locationFrom);
		
		String routeTo = arapMiscCostOrder.get("route_to");
		Location locationTo = LocationUtil.getLocation(routeTo);
		setAttr("locationTo", locationTo);
				
		UserLogin userLogin = UserLogin.dao.findById(arapMiscCostOrder.get("create_by"));
		setAttr("userLogin", userLogin);
		setAttr("arapMiscCostOrder", arapMiscCostOrder);
		
		
		// 获取当前页的数据
		List<Record> itemList = Db
				.find("select amcoi.*,"
						+ "fi.name name "
					+ " from arap_misc_cost_order_item amcoi"
					+ " left join fin_item fi on amcoi.fin_item_id = fi.id"
					+ " where amcoi.misc_order_id = '"+ id +"' ");
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
