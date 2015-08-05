package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

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

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.LocationUtil;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostMiscOrderController extends Controller {
    private Logger logger = Logger.getLogger(CostMiscOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_LIST})
    public void index() {
    	   render("/yh/arap/CostMiscOrder/CostMiscOrderList.html");
    }


    public void confirm() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        String customerId = getPara("customerId");
        Party party = Party.dao.findById(customerId);

        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
        setAttr("customer", contact);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
        render("/yh/arap/CostAcceptOrder/CostCheckOrderEdit.html");
    }

    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_LIST})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_misc_cost_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select amco.*,aco.order_no cost_order_no from arap_misc_cost_order amco"
					+ " left join arap_cost_order aco on aco.id = amco.cost_order_id order by amco.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
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
			render("/yh/arap/CostMiscOrder/CostMiscOrderEdit.html");
	}
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE,PermissionConstant.PERMSSION_CPIO_UPDATE},logical=Logical.OR)
	public void save() {
		ArapMiscCostOrder arapMiscCostOrder = null;
		String costMiscOrderId = getPara("costMiscOrderId");
		//String paymentMethod = getPara("paymentMethod");
		String customerId = getPara("customer_id").equals("")?null:getPara("customer_id");
		String spId=getPara("sp_id").equals("")?null:getPara("sp_id");
		String routeFrom=getPara("route_from");
		String routeTo=getPara("route_to");
		
		if (!"".equals(costMiscOrderId) && costMiscOrderId != null) {
			//TODO: 如果已经应付确认过，就不能修改了
			arapMiscCostOrder = ArapMiscCostOrder.dao.findById(costMiscOrderId);
			arapMiscCostOrder.set("customer_id",customerId);
			arapMiscCostOrder.set("sp_id",spId);
			//arapMiscCostOrder.set("type", getPara("type"));普通收款
			arapMiscCostOrder.set("route_from", routeFrom);
			arapMiscCostOrder.set("route_to", routeTo);
			arapMiscCostOrder.set("remark", getPara("remark"));
			
			arapMiscCostOrder.update();
		} else {
			arapMiscCostOrder = new ArapMiscCostOrder();
			arapMiscCostOrder.set("status", "新建");
			arapMiscCostOrder.set("customer_id",customerId);
			arapMiscCostOrder.set("sp_id",spId);
			arapMiscCostOrder.set("route_from", routeFrom);
			arapMiscCostOrder.set("route_to", routeTo);
			arapMiscCostOrder.set("audit_status", "新建");
			arapMiscCostOrder.set("create_by", getPara("create_by"));
			arapMiscCostOrder.set("create_stamp", new Date());
			arapMiscCostOrder.set("remark", getPara("remark"));
			
			arapMiscCostOrder.set("order_no", OrderNoGenerator.getNextOrderNo("SGFK"));
						
			arapMiscCostOrder.save();
		}
		renderJson(arapMiscCostOrder);;
	}
	
	// 审核
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_APPROVAL})
	public void auditCostPreInvoiceOrder(){
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		if(costPreInvoiceOrderId != null && !"".equals(costPreInvoiceOrderId)){
			ArapCostInvoiceApplication arapAuditOrder = ArapCostInvoiceApplication.dao.findById(costPreInvoiceOrderId);
			arapAuditOrder.set("status", "已审核");
            String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
			arapAuditOrder.set("audit_by", users.get(0).get("id"));
			arapAuditOrder.set("audit_stamp", new Date());
			arapAuditOrder.update();
		}
        renderJson("{\"success\":true}");
	}
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_UPDATE})
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
			render("/yh/arap/CostMiscOrder/CostMiscOrderEdit.html");
	}
    
	//@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	public void costMiscOrderItemList() {
		String costMiscOrderId = getPara("costMiscOrderId");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map orderMap = new HashMap();
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from arap_misc_cost_order_item amcoi "
					+ " left join arap_misc_cost_order amco on amco.id = amcoi.misc_order_id where amco.id = "+costMiscOrderId;
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select amcoi.id, amcoi.create_date, amcoi.status, amcoi.item_desc, amcoi.customer_order_no, amcoi.amount,amcoi.order_type,amcoi.order_no,amcoi.order_stamp,amco.order_no cost_order_no,c.abbr cname,fi.name name "
					+ " from arap_misc_cost_order_item amcoi"
					+ " left join arap_misc_cost_order amco on amco.id = amcoi.misc_order_id"
					+ " left join arap_cost_order aco on aco.id = amco.cost_order_id"
					+ " left join party p on p.id = aco.payee_id"
					+ " left join contact c on c.id = p.contact_id"
					+ " left join fin_item fi on amcoi.fin_item_id = fi.id"
					+ " where amco.id = "+ costMiscOrderId +" " + sLimit);

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);
		renderJson(orderMap);
	}
	
	public void searchAllAccount(){
		List<Account> accounts = Account.dao.find("select * from fin_account where type != 'REC' and bank_name != '现金'");
		renderJson(accounts);
	}
	
	public void addNewFee(){
		ArapMiscCostOrderItem arapMiscCostOrderItem = new ArapMiscCostOrderItem();
		arapMiscCostOrderItem.set("status", "新建");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
		arapMiscCostOrderItem.set("creator", users.get(0).get("id"));
		arapMiscCostOrderItem.set("create_date", new Date());
		arapMiscCostOrderItem.set("fin_item_id", "1");
		arapMiscCostOrderItem.set("misc_order_id", getPara("costMiscOrderId"));
		arapMiscCostOrderItem.save();
		renderJson(arapMiscCostOrderItem);
	}
	
	public void updateCostMiscOrderItem(){
		String paymentId = getPara("paymentId");
		String costMiscOrderId = getPara("costMiscOrderId");
		String name = getPara("name");
		String value = getPara("value");
		if ("amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		if (paymentId != null && !"".equals(paymentId)) {
			ArapMiscCostOrderItem arapMiscCostOrderItem = ArapMiscCostOrderItem.dao.findById(paymentId);
			arapMiscCostOrderItem.set(name, value);
			arapMiscCostOrderItem.update();
		}
		ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(costMiscOrderId);
		Record record = Db.findFirst("select sum(amount) sum_amount from arap_misc_cost_order_item where misc_order_id = ?", costMiscOrderId);
		arapMiscCostOrder.set("total_amount", record.get("sum_amount"));
		arapMiscCostOrder.update();
		
		
		renderJson(arapMiscCostOrder);
	}
	
	public void finItemdel(){
		ArapMiscCostOrderItem.dao.deleteById(getPara());
		renderJson("{\"success\":true}");
	}
	
	public void costCheckList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String costCheckOrderIds = getPara("costCheckOrderIds");
		if(costCheckOrderIds == null || "".equals(costCheckOrderIds)){
			costCheckOrderIds = "-1";
		}
		String sqlTotal = "select count(1) total from arap_cost_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select distinct aao.*, usl.user_name as creator_name,c.abbr cname"
				+ " from arap_cost_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login usl on usl.id=aao.create_by"
				+ " where aao.id in ("+costCheckOrderIds+") order by aao.create_stamp desc " + sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}
	
	public void costCheckorderListById(){
		String costCheckOrderIds = getPara("costCheckOrderIds");
		if(costCheckOrderIds == null || "".equals(costCheckOrderIds)){
			costCheckOrderIds = "-1";
		}
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_cost_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aco.*,group_concat(acoo.invoice_no separator ',') invoice_no,c.abbr cname,ul.user_name creator_name from arap_cost_order aco"
        		+ " left join party p on p.id = aco.payee_id left join contact c on c.id = p.contact_id"
        		+ " left join user_login ul on ul.id = aco.create_by"
        		+ " left join arap_cost_order_invoice_no acoo on acoo.cost_order_id = aco.id where aco.id = "+costCheckOrderIds+" group by aco.id";

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
	}
	
	
	public void saveMiscPartyInfo(){
		String miscId = getPara("miscId");
		String partyId = getPara("partyId");
		String partyType = getPara("partyType");
		ArapMiscCostOrder misc = ArapMiscCostOrder.dao.findById(miscId);
		if(Party.PARTY_TYPE_SERVICE_PROVIDER.equals(partyType))
			misc.set("payee_id", partyId).update();
		else
			misc.set("customer_id", partyId).update();
		renderJson("{\"success\":true}");
	}
	
	//根据单据类型按日期条件查找相应单号
	public void findOrderNoByOrderType(){
		String input = getPara("input");
		String orderType = getPara("orderType");
		String orderStamp = getPara("orderStamp");
		List<Record> recordList = new ArrayList<Record>();
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();
		if("transferOrder".equals(orderType))
			recordList = Db.find("select tor.id,tor.order_no from transfer_order tor "
					+ " left join office o on o.id = tor.office_id "
					+ " where to_days(create_stamp) = to_days('" + orderStamp + "') "
					+ " and tor.order_no like '%" + input + "%' "
					+ " and (o.id = " + parentID + " or o.belong_office = " + parentID + ");");
		else if("pickupOrder".equals(orderType))
			recordList = Db.find("select dor.id,dor.depart_no  as order_no from depart_order dor"
					+ " left join user_login u on u.id = dor.create_by"
					+ " left join office o on o.id = u.office_id"
					+ " where dor.combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "'"
					+ " and	to_days(create_stamp) = to_days('" + orderStamp + "')"
					+ " and dor.depart_no like '%" + input + "%'"
					+ " and (o.id = " + parentID + " or o.belong_office = " + parentID + ");");
		else if("departOrder".equals(orderType))
			recordList = Db.find("select dor.id,dor.depart_no  as order_no from depart_order dor"
					+ " left join user_login u on u.id = dor.create_by"
					+ " left join office o on o.id = u.office_id"
					+ " where dor.combine_type = '" + DepartOrder.COMBINE_TYPE_DEPART + "'"
					+ " and	to_days(create_stamp) = to_days('" + orderStamp + "')"
					+ " and dor.depart_no like '%" + input + "%'"
					+ " and (o.id = " + parentID + " or o.belong_office = " + parentID + ");");
		else if("deliveryOrder".equals(orderType))
			recordList = Db.find("select dor.id,dor.order_no from delivery_order dor"
					+ " left join user_login u on u.id = dor.create_by"
					+ " left join office o on o.id = u.office_id"
					+ " where to_days(create_stamp) = to_days('" + orderStamp + "')"
					+ " and dor.order_no like '%" + input + "%'"
					+ " and (o.id = " + parentID + " or o.belong_office = " + parentID + ");");	
		renderJson(recordList);
	}
	
}
