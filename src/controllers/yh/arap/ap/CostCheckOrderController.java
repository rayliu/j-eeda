package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostItem;
import models.ArapCostOrder;
import models.DepartOrder;
import models.UserLogin;

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

import controllers.yh.util.OrderNoUtil;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostCheckOrderController extends Controller {
    private Logger logger = Logger.getLogger(CostCheckOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CCOI_LIST})
    public void index() {
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "");
        render("/yh/arap/CostCheckOrder/CostCheckOrderList.html");
    }

    public void add() {
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "");
        render("/yh/arap/CostCheckOrder/CostCheckOrderCreateSearchList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CCOI_CREATE})
    public void create() {
        String ids = getPara("ids");
        String orderNos = getPara("orderNos");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        setAttr("orderIds", ids);	 
        setAttr("orderNos", orderNos);	 
        String beginTime = getPara("beginTime");
        if(beginTime != null && !"".equals(beginTime)){
        	setAttr("beginTime", beginTime);
        }
        String endTime = getPara("endTime");
        if(endTime != null && !"".equals(endTime)){
        	setAttr("endTime", endTime);	
        }
        /*String customerId = getPara("customerId");
        if(!"".equals(customerId) && customerId != null){
	        Party party = Party.dao.findById(customerId);
	        setAttr("party", party);	        
	        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
	        setAttr("customer", contact);
	        setAttr("type", "CUSTOMER");
	    	setAttr("classify", "");
        }*/
        
        setAttr("saveOK", false);
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));

        String orderNo = OrderNoUtil.getOrderNo("arap_cost_order", null);
        setAttr("order_no", "YFDZ" + orderNo);


        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);

        setAttr("status", "new");
    		render("/yh/arap/CostCheckOrder/CostCheckOrderEdit.html");
    }

    // 创建应收对帐单时，先选取合适的回单，条件：客户，时间段
    public void createList() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 根据company_id 过滤
        String colsLength = getPara("iColumns");
        String fieldsWhere = "AND (";
        for (int i = 0; i < Integer.parseInt(colsLength); i++) {
            String mDataProp = getPara("mDataProp_" + i);
            String searchValue = getPara("sSearch_" + i);
            logger.debug(mDataProp + "[" + searchValue + "]");
            if (searchValue != null && !"".equals(searchValue)) {
                if (mDataProp.equals("COMPANY_ID")) {
                    fieldsWhere += "p.id" + " = " + searchValue + " AND ";
                } else {
                    fieldsWhere += mDataProp + " like '%" + searchValue + "%' AND ";
                }
            }
        }
        logger.debug("2nd filter:" + fieldsWhere);
        if (fieldsWhere.length() > 8) {
            fieldsWhere = fieldsWhere.substring(0, fieldsWhere.length() - 4);
            fieldsWhere += ')';
        } else {
            fieldsWhere = "";
        }
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total FROM RETURN_ORDER ro left join party p on ro.customer_id = p.id "
                + "where ro.TRANSACTION_STATUS = 'confirmed' ";
        Record rec = Db.findFirst(sql + fieldsWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("SELECT ro.*, to.order_no as transfer_order_no, do.order_no as delivery_order_no, p.id as company_id, c.company_name FROM RETURN_ORDER ro "
                        + "left join transfer_order to on ro.transfer_order_id = to.id "
                        + "left join delivery_order do on ro.delivery_order_id = do.id "
                        + "left join party p on ro.customer_id = p.id "
                        + "left join contact c on p.contact_id = c.id where ro.TRANSACTION_STATUS = 'confirmed' " + fieldsWhere);
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    // billing order 列表
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_TO_CREATE})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_cost_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aco.*,group_concat(acoo.invoice_no separator ',') invoice_no  from arap_cost_order aco left join arap_cost_order_invoice_no acoo on acoo.cost_order_id = aco.id group by aco.id";

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CCOI_CREATE, PermissionConstant.PERMSSION_CCOI_UPDATE}, logical=Logical.OR)
    public void save(){
    	ArapCostOrder arapAuditOrder = null;
    	String costCheckOrderId = getPara("costCheckOrderId");
    	if(!"".equals(costCheckOrderId) && costCheckOrderId != null){
    		arapAuditOrder = ArapCostOrder.dao.findById(costCheckOrderId);
	    	arapAuditOrder.set("order_no", getPara("order_no"));
	    	//arapAuditOrder.set("order_type", );
	    	arapAuditOrder.set("status", "new");
	    	arapAuditOrder.set("payee_id", getPara("customer_id"));
	    	arapAuditOrder.set("create_by", getPara("create_by"));
	    	arapAuditOrder.set("create_stamp", new Date());
	    	arapAuditOrder.set("remark", getPara("remark"));
	    	arapAuditOrder.update();
	    	
	    	/*List<ArapChargeItem> arapAuditItems = ArapChargeItem.dao.find("select * from arap_audit_item where audit_order_id = ?", arapAuditOrder.get("id"));
	    	for(ArapChargeItem arapAuditItem : arapAuditItems){
		    	//arapAuditItem.set("ref_order_type", );
		    	//arapAuditItem.set("item_status", "");
		    	arapAuditItem.set("create_by", getPara("create_by"));
		    	arapAuditItem.set("create_stamp", new Date());
		    	arapAuditItem.update();
	    	}*/
    	}else{
	    	arapAuditOrder = new ArapCostOrder();
	    	arapAuditOrder.set("order_no", getPara("order_no"));
	    	//arapAuditOrder.set("order_type", );
	    	arapAuditOrder.set("status", "new");
	    	//arapAuditOrder.set("payee_id", getPara("customer_id"));
	    	arapAuditOrder.set("create_by", getPara("create_by"));
	    	arapAuditOrder.set("create_stamp", new Date());
	    	//arapAuditOrder.set("begin_time", getPara("beginTime"));
	    	//arapAuditOrder.set("end_time", getPara("endTime"));
	    	arapAuditOrder.set("remark", getPara("remark"));
	    	arapAuditOrder.save();
	    	
	    	String orderIds = getPara("orderIds");
	    	String orderNos = getPara("orderNos");
	    	String[] orderIdsArr = orderIds.split(",");
	    	String[] orderNosArr = orderNos.split(",");
	    	for(int i=0;i<orderIdsArr.length;i++){
		    	ArapCostItem arapAuditItem = new ArapCostItem();
		    	//arapAuditItem.set("ref_order_type", );
		    	arapAuditItem.set("ref_order_id", orderIdsArr[i]);
		    	arapAuditItem.set("ref_order_no", orderNosArr[i]);
		    	arapAuditItem.set("cost_order_id", arapAuditOrder.get("id"));
		    	//arapAuditItem.set("item_status", "");
		    	arapAuditItem.set("create_by", getPara("create_by"));
		    	arapAuditItem.set("create_stamp", new Date());
		    	arapAuditItem.save();
	    	}
    	}
        renderJson(arapAuditOrder);;
    }

@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCOI_UPDATE})
    public void edit(){
    	ArapCostOrder arapAuditOrder = ArapCostOrder.dao.findById(getPara("id"));
    	/*String customerId = arapAuditOrder.get("payee_id");
    	if(!"".equals(customerId) && customerId != null){
	    	Party party = Party.dao.findById(customerId);
	        setAttr("party", party);	        
	        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
	        setAttr("customer", contact);
    	} */   	
    	UserLogin userLogin = UserLogin.dao.findById(arapAuditOrder.get("create_by"));
    	setAttr("userLogin", userLogin);
    	setAttr("arapAuditOrder", arapAuditOrder);
    	String orderIds = "";
    	String orderNos = "";
    	List<ArapCostItem> arapCostItems = ArapCostItem.dao.find("select * from arap_cost_item where cost_order_id = ?", arapAuditOrder.get("id"));
    	for(ArapCostItem arapCostItem : arapCostItems){
    		orderIds += arapCostItem.get("ref_order_id") + ",";
    		orderNos += arapCostItem.get("ref_order_no") + ",";
    	}
    	orderIds = orderIds.substring(0, orderIds.length() - 1);
    	orderNos = orderNos.substring(0, orderNos.length() - 1);
    	setAttr("orderIds", orderIds);
    	setAttr("orderNos", orderNos);
    		render("/yh/arap/CostCheckOrder/CostCheckOrderEdit.html");
    }

	// 审核

@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCOI_AFFIRM})
	public void auditCostCheckOrder(){
		String costCheckOrderId = getPara("costCheckOrderId");
		if(costCheckOrderId != null && !"".equals(costCheckOrderId)){
			ArapCostOrder arapAuditOrder = ArapCostOrder.dao.findById(costCheckOrderId);
			arapAuditOrder.set("status", "已确认");
	        String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
	        arapAuditOrder.set("confirm_by", users.get(0).get("id"));
	        arapAuditOrder.set("confirm_stamp", new Date());
			arapAuditOrder.update();
			
			//updateReturnOrderStatus(arapAuditOrder, "对账已确认");
		}
        renderJson("{\"success\":true}");
	}
	
	public void costConfirmList(){
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String orderNos = getPara("orderNos");
        String orderIds = getPara("orderIds");
        String[] orderNoArr = orderNos.split(",");
        String[] orderIdArr = orderIds.split(",");
        String pickupOrderIds = "";
        String departOrderIds = "";
        String deliveryOrderIds = "";
        String insuranceOrderIds = "";
        String pickupOrderSql = "";
        String departOrderSql = "";
        String deliveryOrderSql = "";
        String insuranceOrderSql = "";
        for(int i=0;i<orderNoArr.length;i++){
        	String preOrderNo = orderNoArr[i].substring(0,2);
    		if("PC".equals(preOrderNo)){
    			pickupOrderIds += orderIdArr[i] + ",";
    		}else if("FC".equals(preOrderNo)){
    			departOrderIds += orderIdArr[i] + ",";
    		}else if("PS".equals(preOrderNo)){
    			deliveryOrderIds += orderIdArr[i] + ",";
    		}else{
    			insuranceOrderIds += orderIdArr[i] + ",";
    		}
        }
        if(!"".equals(pickupOrderIds)){
        	pickupOrderIds = pickupOrderIds.substring(0, pickupOrderIds.length() - 1);
        	pickupOrderSql = " union select distinct dpr.id,dpr.depart_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'提货' business_type, "
    			+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
    			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
    			+ " transfer_order_no,'没回单' return_order_collection,dpr.remark,oe.office_name office_name"
    			+ " from return_order ror "
    			+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
    			+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
    			+ " left join depart_transfer dtr on dtr.order_id = doi.transfer_order_id"
    			+ " left join depart_order dpr on dpr.id = dtr.pickup_id"
    			+ " left join transfer_order tor on tor.id = dtr.order_id "
    			+ " left join transfer_order_item_detail toid on toid.order_id = tor.id "
    			+ " left join transfer_order_item toi on toi.id = toid.item_id "
    			+ " left join product prod on toi.product_id = prod.id "
    			+ " left join user_login ul on ul.id = dpr.create_by "
    			+ " left join party p on p.id = dpr.sp_id "
    			+ " left join contact c on c.id = p.contact_id"
    			+ " left join office oe on oe.id = tor.office_id"
    			+ " where dor.id = ror.delivery_order_id and dpr.id in("+pickupOrderIds+") and (ifnull(dpr.id, 0) > 0)"
    			+ " group by dpr.id ";
        }
        if(!"".equals(departOrderIds)){
        	departOrderIds = departOrderIds.substring(0, departOrderIds.length() - 1);
        	departOrderSql = " union select distinct dpr.id,dpr.depart_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'零担' business_type, "
				+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
				+ " transfer_order_no,'没回单' return_order_collection,dpr.remark,oe.office_name office_name"
				+ " from return_order ror "
				+ " left join delivery_order dor on dor.id = ror.delivery_order_id  "
				+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
				+ " left join depart_transfer dtr on dtr.order_id = doi.transfer_order_id"
				+ " left join depart_order dpr on dpr.id = dtr.depart_id"
				+ " left join transfer_order tor on tor.id = dtr.order_id "
				+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id"
				+ " left join product prod on toi.product_id = prod.id "
				+ " left join user_login ul on ul.id = dpr.create_by "
				+ " left join party p on p.id = dpr.sp_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join office oe on oe.id = tor.office_id"
				+ " where dor.id = ror.delivery_order_id and dpr.id in("+departOrderIds+") and (ifnull(dpr.id, 0) > 0)"
				+ " group by dpr.id ";
        }
        if(!"".equals(deliveryOrderIds)){
        	deliveryOrderIds = deliveryOrderIds.substring(0, deliveryOrderIds.length() - 1);
        	deliveryOrderSql = "select distinct dor.id,dor.order_no,dor.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator,'配送' business_type, "
				+ " (select sum(amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.type = '应付') pay_amount, "
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '\r\n')"
				+ " transfer_order_no,'有回单' return_order_collection,dor.remark,oe.office_name office_name"
				+ " from return_order ror "
				+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
				+ " left join party p on p.id = dor.sp_id "
				+ " left join contact c on c.id = p.contact_id "
				+ " left join delivery_order_item doi on doi.delivery_id = dor.id "
				+ " left join transfer_order_item_detail toid on toid.id = doi.transfer_item_detail_id "
				+ " left join transfer_order_item toi on toi.id = toid.item_id "
				+ " left join product prod on toi.product_id = prod.id "
				+ " left join user_login ul on ul.id = dor.create_by "
				+ " left join warehouse w on w.id = dor.from_warehouse_id "
				+ " left join office oe on oe.id = w.office_id"
				+ " where dor.id = ror.delivery_order_id and dor.id in("+deliveryOrderIds+") group by dor.id ";
        }
        if(!"".equals(insuranceOrderIds)){
        	insuranceOrderIds = insuranceOrderIds.substring(0, insuranceOrderIds.length() - 1);
        	insuranceOrderSql = "";
        }
        String sqlTotal = "select count(1) total from (" + deliveryOrderSql + " " + departOrderSql + " " + pickupOrderSql +") a";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = deliveryOrderSql + " " + departOrderSql + " " + pickupOrderSql + sLimit;

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
