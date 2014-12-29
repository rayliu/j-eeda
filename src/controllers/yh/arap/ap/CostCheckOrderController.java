package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostItem;
import models.ArapCostOrder;
import models.DepartOrder;
import models.InsuranceOrder;
import models.Party;
import models.UserLogin;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.delivery.DeliveryOrder;
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
        String beginTime = getPara("beginTime");
        if(beginTime != null && !"".equals(beginTime)){
        	setAttr("beginTime", beginTime);
        }
        String endTime = getPara("endTime");
        if(endTime != null && !"".equals(endTime)){
        	setAttr("endTime", endTime);	
        }

    	String orderIds = getPara("ids");
    	String orderNos = getPara("orderNos");
    	String[] orderIdsArr = orderIds.split(",");
    	String[] orderNoArr = orderNos.split(",");
    	Double totalAmount = 0.0;
    	Long spId = null;
    	for(int i=0;i<orderIdsArr.length;i++){
            Record rec = null;
            if("提货".equals(orderNoArr[i])){
            	rec = Db.findFirst("select sum(amount) sum_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	totalAmount = totalAmount + rec.getDouble("sum_amount");
            	DepartOrder departOrder = DepartOrder.dao.findById(orderIdsArr[i]);
            	spId = departOrder.getLong("sp_id");
            }else if("零担".equals(orderNoArr[i])){
            	rec = Db.findFirst("select sum(amount) sum_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	totalAmount = totalAmount + rec.getDouble("sum_amount");
            	DepartOrder departOrder = DepartOrder.dao.findById(orderIdsArr[i]);
            	spId = departOrder.getLong("sp_id");
            }else if("配送".equals(orderNoArr[i])){
            	rec = Db.findFirst("select sum(amount) sum_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	totalAmount = totalAmount + rec.getDouble("sum_amount");
            	DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(orderIdsArr[i]);
            	spId = deliveryOrder.getLong("sp_id");
            }else{
            	//rec = Db.findFirst("", orderIdsArr[i]);
            }
    	}
    	if(!"".equals(spId) && spId != null){
    		Party party = Party.dao.findById(spId);
    		setAttr("party", party);	        
    		Contact contact = Contact.dao.findById(party.get("contact_id").toString());
    		setAttr("sp", contact);
    	}
    	setAttr("orderIds", orderIds);	 
    	setAttr("orderNos", orderNos);	 
    	setAttr("totalAmount", totalAmount);	 
        
        setAttr("saveOK", false);
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));
        
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

        String sp = getPara("sp");
        String shifadi = getPara("shifadi");
        String customer = getPara("customer");
        String mudidi = getPara("mudidi");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        
        String sqlTotal = "";
        String sql = "select aco.*,group_concat(acoo.invoice_no separator ',') invoice_no,c.abbr cname,ul.user_name creator_name from arap_cost_order aco "
        		+ " left join party p on p.id = aco.payee_id"
        		+ " left join contact c on c.id = p.contact_id"
        		+ " left join user_login ul on ul.id = aco.create_by"
        		+ " left join arap_cost_order_invoice_no acoo on acoo.cost_order_id = aco.id ";
        String condition = "";
        
        if(sp != null || shifadi != null || customer != null
        		|| mudidi != null || beginTime != null || endTime != null){
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			condition = " where ifnull(c.abbr,'') like '%" + sp + "%' "
						+ " and aco.create_stamp between '" + beginTime + "' and '" + endTime+ "' ";
			
			
        }
        
        sqlTotal = "select count(1) total from arap_cost_order aco "
        		+ " left join party p on p.id = aco.payee_id"
        		+ " left join contact c on c.id = p.contact_id"
        		+ " left join user_login ul on ul.id = aco.create_by"
        		+ " left join arap_cost_order_invoice_no acoo on acoo.cost_order_id = aco.id ";
       

       
        Record rec = Db.findFirst(sqlTotal + condition );
        logger.debug("total records:" + rec.getLong("total"));
        
        
        List<Record> BillingOrders = Db.find(sql + condition + " group by aco.id order by aco.create_stamp desc "+sLimit);

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
		String total_amount = getPara("total_amount");
		String debit_amount = getPara("debit_amount")==""?"0":getPara("debit_amount");
    	if(!"".equals(costCheckOrderId) && costCheckOrderId != null){
    		arapAuditOrder = ArapCostOrder.dao.findById(costCheckOrderId);
	    	//arapAuditOrder.set("order_type", );
	    	arapAuditOrder.set("status", "new");
	    	arapAuditOrder.set("create_by", getPara("create_by"));
	    	arapAuditOrder.set("create_stamp", new Date());
	    	arapAuditOrder.set("remark", getPara("remark"));
			if(getParaToDate("begin_time") != null){
				arapAuditOrder.set("begin_time", getPara("begin_time")); 
			}
            if(getParaToDate("end_time") != null){
            	arapAuditOrder.set("end_time", getPara("end_time")); 
            }
            arapAuditOrder.set("total_amount", total_amount);
            if(total_amount != null && !"".equals(total_amount) && debit_amount != null && !"".equals(debit_amount)){
            	arapAuditOrder.set("cost_amount", Double.parseDouble(total_amount) - Double.parseDouble(debit_amount));
            }
	    	arapAuditOrder.update();
    	}else{
	    	arapAuditOrder = new ArapCostOrder();
	        String sql = "select * from arap_cost_order order by id desc limit 0,1";
	    	arapAuditOrder.set("order_no", OrderNoUtil.getOrderNo(sql, "YFDZ"));
	    	//arapAuditOrder.set("order_type", );
	    	arapAuditOrder.set("status", "new");
	    	arapAuditOrder.set("payee_id", getPara("sp_id"));
	    	arapAuditOrder.set("create_by", getPara("create_by"));
	    	arapAuditOrder.set("create_stamp", new Date());
	    	arapAuditOrder.set("remark", getPara("remark"));
	    	if(getParaToDate("begin_time") != null){
				arapAuditOrder.set("begin_time", getPara("begin_time")); 
			}
            if(getParaToDate("end_time") != null){
            	arapAuditOrder.set("end_time", getPara("end_time")); 
            }
            arapAuditOrder.set("total_amount", total_amount);
            if(total_amount != null && !"".equals(total_amount) && debit_amount != null && !"".equals(debit_amount)){
            	arapAuditOrder.set("cost_amount", Double.parseDouble(total_amount) - Double.parseDouble(debit_amount));
            }
	    	arapAuditOrder.save();
	    	
	    	String orderIds = getPara("orderIds");
	    	String orderNos = getPara("orderNos");
	    	String[] orderIdsArr = orderIds.split(",");
	    	String[] orderNoArr = orderNos.split(",");
	    	for(int i=0;i<orderIdsArr.length;i++){
		    	ArapCostItem arapAuditItem = new ArapCostItem();
		    	//arapAuditItem.set("ref_order_type", );
		    	arapAuditItem.set("ref_order_id", orderIdsArr[i]);
		    	arapAuditItem.set("ref_order_no", orderNoArr[i]);
		    	arapAuditItem.set("cost_order_id", arapAuditOrder.get("id"));
		    	//arapAuditItem.set("item_status", "");
		    	arapAuditItem.set("create_by", getPara("create_by"));
		    	arapAuditItem.set("create_stamp", new Date());
		    	arapAuditItem.save();
	    	}
	    	for(int i=0;i<orderIdsArr.length;i++){
	            if("提货".equals(orderNoArr[i])){
	            	DepartOrder departOrder = DepartOrder.dao.findById(orderIdsArr[i]);
	            	departOrder.set("audit_status", "对账中");
	            	departOrder.update();
	            }else if("零担".equals(orderNoArr[i])){
	            	DepartOrder departOrder = DepartOrder.dao.findById(orderIdsArr[i]);
	            	departOrder.set("audit_status", "对账中");
	            	departOrder.update();
	            }else if("配送".equals(orderNoArr[i])){
	            	DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(orderIdsArr[i]);
	            	deliveryOrder.set("audit_status", "对账中");
	            	deliveryOrder.update();
	            }else{
	            	InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(orderIdsArr[i]);
	            	insuranceOrder.set("audit_status", "对账中");
	            	insuranceOrder.update();
	            }
	    	}
    	}
        renderJson(arapAuditOrder);;
    }

@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCOI_UPDATE})
    public void edit(){
    	ArapCostOrder arapAuditOrder = ArapCostOrder.dao.findById(getPara("id"));
    	Long spId = arapAuditOrder.get("payee_id");
    	if(!"".equals(spId) && spId != null){
	    	Party party = Party.dao.findById(spId);
	        setAttr("party", party);	        
	        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
	        setAttr("sp", contact);
    	}    	
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
    			+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
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
				+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
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
				+ " transfer_order_no,dor.sign_status return_order_collection,dor.remark,oe.office_name office_name"
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
	
	public void costConfirmOrderList() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String orderNo = getPara("orderNo");
    	String sp = getPara("sp");
    	String no = getPara("no");
    	String beginTime = getPara("beginTime");
    	String endTime = getPara("endTime");
    	String type = getPara("type");
    	String status = getPara("status");
    	
    	
    	String sqlTotal = "";
    	String sql = " select * from (select distinct dor.id,dor.order_no order_no,dor.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator,'配送' business_type, "
    			+ " (select sum(amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.type = '应付') pay_amount, "
    			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '\r\n')"
    			+ " transfer_order_no,dor.sign_status return_order_collection,dor.remark,oe.office_name office_name"
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
    			+ " where dor.id = ror.delivery_order_id and dor.audit_status='已确认' group by dor.id"
    			+ " union"
    			+ " select distinct dpr.id,dpr.depart_no order_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'零担' business_type, "
    			+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
    			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
    			+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
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
    			+ " where dor.id = ror.delivery_order_id and (ifnull(dpr.id, 0) > 0) and dpr.audit_status='已确认'"
    			+ " group by dpr.id"
    			+ " union"
    			+ " select distinct dpr.id,dpr.depart_no order_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'零担' business_type, "
    			+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
    			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
    			+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
    			+ " from return_order ror "
    			+ " left join depart_transfer dtr on dtr.order_id = ror.transfer_order_id"
    			+ " left join depart_order dpr on dpr.id = dtr.depart_id"
    			+ " left join transfer_order tor on tor.id = dtr.order_id "
    			+ " left join transfer_order_item toi on toi.order_id = tor.id "
    			+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id"
    			+ " left join product prod on toi.product_id = prod.id "
    			+ " left join user_login ul on ul.id = dpr.create_by "
    			+ " left join party p on p.id = dpr.sp_id "
    			+ " left join contact c on c.id = p.contact_id"
    			+ " left join office oe on oe.id = tor.office_id"
    			+ " where (ifnull(dpr.id, 0) > 0) and dpr.audit_status='已确认'"
    			+ " group by dpr.id"
    			+ " union"
    			+ " select distinct dpr.id,dpr.depart_no order_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'提货' business_type, "
    			+ " (select sum(amount) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付') pay_amount, "
    			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
    			+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
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
    			+ " where dor.id = ror.delivery_order_id and (ifnull(dpr.id, 0) > 0) and dpr.audit_status='已确认'"
    			+ " group by dpr.id "
    			+ " union"
    			+ " select distinct dpr.id,dpr.depart_no order_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'提货' business_type, "
    			+ " (select sum(amount) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付') pay_amount, "
    			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
    			+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
    			+ " from return_order ror "
    			+ " left join depart_transfer dtr on dtr.order_id = ror.transfer_order_id"
    			+ " left join depart_order dpr on dpr.id = dtr.pickup_id"
    			+ " left join transfer_order tor on tor.id = dtr.order_id "
    			+ " left join transfer_order_item toi on toi.order_id = tor.id "
    			+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id"
    			+ " left join product prod on toi.product_id = prod.id "
    			+ " left join user_login ul on ul.id = dpr.create_by "
    			+ " left join party p on p.id = dpr.sp_id "
    			+ " left join contact c on c.id = p.contact_id"
    			+ " left join office oe on oe.id = tor.office_id"
    			+ " where (ifnull(dpr.id, 0) > 0) and dpr.audit_status='已确认'"
    			+ " group by dpr.id ) as newView" ;
    	String condition = "";
    	
    	
    	if(orderNo != null || sp != null || no != null || beginTime != null
    			|| endTime != null || type != null || status != null){
    		if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
    		
    		condition = " where ifnull(transfer_order_no,'') like '%" + orderNo + "%' "
    					+ " and order_no like '%" + no + "%' "
    					+ " and business_type like '%" + type + "%' "
    					+ " and status like '%" + status + "%' "
    					+ " and spname like '%" + sp + "%' "
    					+ " and create_stamp between '" + beginTime + "' and '" + endTime + "' ";
    	}
    	
        sqlTotal = "select count(1) from (" + sql + condition + ") as B";

       
        
        
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql + sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
	
	public void costConfirmListById() {
		String orderIds = getPara("orderIds");
    	String orderNos = getPara("orderNos");
    	String pickupId = "";
    	String departId = "";
    	String deliveryId = "";
    	if(orderIds == null || orderIds == ""){
    		pickupId = "-1";
        	departId = "-1";
        	deliveryId = "-1";
    	}else{
	    	String[] orderIdsArr = orderIds.split(",");
	    	String[] orderNoArr = orderNos.split(",");
	    	for(int i=0;i<orderIdsArr.length;i++){
	            Record rec = null;
	            if("提货".equals(orderNoArr[i])){
	            	pickupId += orderIdsArr[i] + ",";
	            }else if("零担".equals(orderNoArr[i])){
	            	departId += orderIdsArr[i] + ",";
	            }else if("配送".equals(orderNoArr[i])){
	            	deliveryId += orderIdsArr[i] + ",";
	            }else{
	            	//rec = Db.findFirst("", orderIdsArr[i]);
	            }
	    	}
	    	if(pickupId != null && !"".equals(pickupId)){
	    		pickupId = pickupId.substring(0, pickupId.length() - 1);
	    	} else{
	    		pickupId = "-1";
	    	}
	    	if(departId != null && !"".equals(departId)){
	    		departId = departId.substring(0, departId.length() - 1);
	    	} else{
	    		departId = "-1";
	    	}
	    	if(deliveryId != null && !"".equals(deliveryId)){
	    		deliveryId = deliveryId.substring(0, deliveryId.length() - 1);
			} else{
				deliveryId = "-1";
			}
    	}

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}		
    	String searchSql = "select distinct dor.id,dor.order_no order_no,dor.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator,'配送' business_type, "
				+ " (select sum(amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.type = '应付') pay_amount, "
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '\r\n')"
				+ " transfer_order_no,dor.sign_status return_order_collection,dor.remark,oe.office_name office_name"
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
				+ " where dor.id = ror.delivery_order_id and dor.id in("+deliveryId+") group by dor.id"
				+ " union"
				+ " select distinct dpr.id,dpr.depart_no order_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'零担' business_type, "
				+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
				+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
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
				+ " where dor.id = ror.delivery_order_id and (ifnull(dpr.id, 0) > 0) and dpr.id in("+departId+")"
				+ " group by dpr.id"
				+ " union"
				+ " select distinct dpr.id,dpr.depart_no order_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'零担' business_type, "
				+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
				+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
				+ " from return_order ror "
				+ " left join depart_transfer dtr on dtr.order_id = ror.transfer_order_id"
				+ " left join depart_order dpr on dpr.id = dtr.depart_id"
				+ " left join transfer_order tor on tor.id = dtr.order_id "
				+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id"
				+ " left join product prod on toi.product_id = prod.id "
				+ " left join user_login ul on ul.id = dpr.create_by "
				+ " left join party p on p.id = dpr.sp_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join office oe on oe.id = tor.office_id"
				+ " where (ifnull(dpr.id, 0) > 0) and dpr.id in("+departId+")"
				+ " group by dpr.id"
				+ " union"
				+ " select distinct dpr.id,dpr.depart_no order_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'提货' business_type, "
				+ " (select sum(amount) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付') pay_amount, "
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
				+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
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
				+ " where dor.id = ror.delivery_order_id and (ifnull(dpr.id, 0) > 0) and dpr.id in("+pickupId+")"
				+ " group by dpr.id "
				+ " union"
				+ " select distinct dpr.id,dpr.depart_no order_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'提货' business_type, "
				+ " (select sum(amount) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付') pay_amount, "
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
				+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
				+ " from return_order ror "
				+ " left join depart_transfer dtr on dtr.order_id = ror.transfer_order_id"
				+ " left join depart_order dpr on dpr.id = dtr.pickup_id"
				+ " left join transfer_order tor on tor.id = dtr.order_id "
				+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id"
				+ " left join product prod on toi.product_id = prod.id "
				+ " left join user_login ul on ul.id = dpr.create_by "
				+ " left join party p on p.id = dpr.sp_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join office oe on oe.id = tor.office_id"
				+ " where (ifnull(dpr.id, 0) > 0) and dpr.id in("+pickupId+")"
				+ " group by dpr.id ";
    	
		String sqlTotal = "select count(1) total from ("+searchSql+") a";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		
		String sql = searchSql + sLimit;
		
		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);
		
		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
		
		BillingOrderListMap.put("aaData", BillingOrders);
		
		renderJson(BillingOrderListMap);
	}

	public void checkCostMiscList(){
		String costCheckOrderId = getPara("costCheckOrderId");
		if(costCheckOrderId == null || "".equals(costCheckOrderId)){
			costCheckOrderId = "-1";
		}
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
		String sql = "select count(amco.id) total from arap_misc_cost_order amco"
					+ " left join arap_cost_order aco on amco.cost_order_id = aco.id"
					+ " left join arap_misc_cost_order_item amcoi on amcoi.misc_order_id = amco.id where aco.id = "+costCheckOrderId;
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select amcoi.*,amco.order_no misc_order_no,c.abbr cname,fi.name name from arap_misc_cost_order amco"
					+ " left join arap_cost_order aco on aco.id = amco.cost_order_id "
					+ " left join arap_misc_cost_order_item amcoi on amcoi.misc_order_id = amco.id "
					+ " left join party p on p.id = aco.payee_id left join contact c on c.id = p.contact_id "
					+ " left join fin_item fi on amcoi.fin_item_id = fi.id where aco.id =  "+ costCheckOrderId +" " + sLimit);

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}

	public void externalMiscOrderList(){
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_misc_cost_order where ifnull(cost_order_id, 0) = 0";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select amco.*,aco.order_no cost_order_no from arap_misc_cost_order amco"
					+ " left join arap_cost_order aco on aco.id = amco.cost_order_id"
					+ " where ifnull(cost_order_id, 0) = 0 order by amco.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
	}
	
	public void updateCostMiscOrder(){
		String micsOrderIds = getPara("micsOrderIds");
		String costCheckOrderId = getPara("costCheckOrderId");
		ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(costCheckOrderId);
		if(micsOrderIds != null && !"".equals(micsOrderIds)){
			String[] micsOrderIdArr = micsOrderIds.split(",");
			for(int i=0;i<micsOrderIdArr.length;i++){
				ArapMiscCostOrder arapMisccostOrder = ArapMiscCostOrder.dao.findById(micsOrderIdArr[i]);
				arapMisccostOrder.set("cost_order_id", costCheckOrderId);
				arapMisccostOrder.update();
			}
			
			Record record = Db.findFirst("select sum(amount) sum_amount from arap_misc_cost_order mco"
								+ " left join arap_misc_cost_order_item mcoi on mcoi.misc_order_id = mco.id where mco.cost_order_id = ?", costCheckOrderId);
			Double total_amount = arapCostOrder.getDouble("total_amount");
			Double debit_amount = record.getDouble("sum_amount");
			arapCostOrder.set("debit_amount", debit_amount);
			arapCostOrder.set("cost_amount", total_amount - debit_amount);
			arapCostOrder.update();
		}
        renderJson(arapCostOrder);
	}
}
