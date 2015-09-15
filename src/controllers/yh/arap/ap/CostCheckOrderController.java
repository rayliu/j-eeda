package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostItem;
import models.ArapCostOrder;
import models.DeliveryOrderFinItem;
import models.DepartOrder;
import models.DepartOrderFinItem;
import models.InsuranceFinItem;
import models.InsuranceOrder;
import models.Party;
import models.PickupOrderFinItem;
import models.UserLogin;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ArapMiscCostOrderItem;
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
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.util.OrderNoGenerator;
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
    	Double changeAmount = 0.0;
    	Long spId = null;
    	for(int i=0;i<orderIdsArr.length;i++){
            Record rec = null;
            Record rec1 = null;
            if("提货".equals(orderNoArr[i])){
            	rec = Db.findFirst("select sum(amount) sum_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	if(rec.getDouble("sum_amount")!=null){
            		totalAmount = totalAmount + rec.getDouble("sum_amount");
            	}
            	rec1 = Db.findFirst("select sum(change_amount) change_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ?", orderIdsArr[i]);
            	if(rec1.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec1.getDouble("change_amount");
            	}else{
            		if(rec.getDouble("sum_amount")!=null){
                		changeAmount=rec.getDouble("sum_amount")+changeAmount;
                		}
            	}
            	DepartOrder departOrder = DepartOrder.dao.findById(orderIdsArr[i]);
            	spId = departOrder.getLong("sp_id");
            }else if("零担".equals(orderNoArr[i]) || "整车".equals(orderNoArr[i])){
            	rec = Db.findFirst("select sum(amount) sum_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	if(rec.getDouble("sum_amount")!=null){
            		totalAmount = totalAmount + rec.getDouble("sum_amount");
            	}
            	rec1 = Db.findFirst("select sum(change_amount) change_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ?", orderIdsArr[i]);
            	if(rec1.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec1.getDouble("change_amount");
            	}
            	else{
            		if(rec.getDouble("sum_amount")!=null){
            		changeAmount=rec.getDouble("sum_amount")+changeAmount;
            		}
            	}
            	DepartOrder departOrder = DepartOrder.dao.findById(orderIdsArr[i]);
            	spId = departOrder.getLong("sp_id");
            }else if("配送".equals(orderNoArr[i])){
            	//DeliveryOrderFinItem deliveryorderfinitem =DeliveryOrderFinItem.dao.findById(paymentId);
            	rec = Db.findFirst("select sum(amount) sum_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	if(rec.getDouble("sum_amount")!=null){
            		totalAmount = totalAmount + rec.getDouble("sum_amount");
            	}
            	rec1 = Db.findFirst("select sum(change_amount) change_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ?", orderIdsArr[i]);
            	if(rec1.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec1.getDouble("change_amount");
            	}else{
            		if(rec.getDouble("sum_amount")!=null){
                		changeAmount=rec.getDouble("sum_amount")+changeAmount;
                }
            	}
            	DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(orderIdsArr[i]);
            	spId = deliveryOrder.getLong("sp_id");
            }else if("成本单".equals(orderNoArr[i])){
            	//这是成本单的应付
            	rec = Db.findFirst("select sum(amount) sum_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ? and fi.type ='应付'",orderIdsArr[i]);
            	if(rec.getDouble("sum_amount")!=null){
            		totalAmount = totalAmount + rec.getDouble("sum_amount");
            	}
            	rec1 = Db.findFirst("select sum(change_amount) change_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ?",orderIdsArr[i]);
            	if(rec1.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec1.getDouble("change_amount");
            	}else{
            		if(rec.getDouble("sum_amount")!=null){
                		changeAmount=rec.getDouble("sum_amount")+changeAmount;
                		}
            	}
            	
            	ArapMiscCostOrder arapmisc = ArapMiscCostOrder.dao.findById(orderIdsArr[i]);
            	spId = arapmisc.getLong("sp_id");
            	/*InsuranceOrder insuraceOrder = InsuranceOrder.dao.findById(orderIdsArr[i]);
            	spId = insuraceOrder.get("sp_id");*/
            }
            else{
            	//这是保险单的应付
            	rec = Db.findFirst("select sum(insurance_amount) sum_amount from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id  where ifi.insurance_order_id = ? and fi.type ='应付'",orderIdsArr[i]);
            	if(rec.getDouble("sum_amount")!=null){
            		totalAmount = totalAmount + rec.getDouble("sum_amount");
            	}
            	rec1 = Db.findFirst("select sum(change_amount) change_amount from insurance_fin_item ifi where ifi.insurance_order_id = ?",orderIdsArr[i]);
            	if(rec1.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec1.getDouble("change_amount");
            	}else{
            		if(rec.getDouble("sum_amount")!=null){
                		changeAmount=rec.getDouble("sum_amount")+changeAmount;
                		}
            	}
            	InsuranceOrder insuraceOrder = InsuranceOrder.dao.findById(orderIdsArr[i]);
            	spId = insuraceOrder.get("sp_id");
            }
    	}
    	if(!"".equals(spId) && spId != null){
    		Party party = Party.dao.findById(spId);
    		setAttr("party", party);	        
    		Contact contact = Contact.dao.findById(party.get("contact_id").toString());
    		setAttr("sp", contact);
    	}else{
    		//由于没有保险公司的维护，暂时实例化一个虚拟的
    		Contact contact = new Contact();
    		contact.set("company_name", "保险公司");
    		setAttr("sp", contact);
    	}
    	Double actualAmount=totalAmount-changeAmount;
    	setAttr("orderIds", orderIds);	 
    	setAttr("orderNos", orderNos);	 
    	setAttr("changeAmount", changeAmount);
        setAttr("saveOK", false);
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));
        UserLogin ul = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("create_name", ul.get("c_name"));
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
        String sql = "select aco.*,MONTH(aco.begin_time) as c_stamp,o.office_name oname,c.company_name as company_name,"
        		+ " group_concat(acoo.invoice_no separator ',') invoice_no,"
        		+ " c.abbr cname,"
        		+ " ifnull(ul.c_name,ul.user_name) creator_name,"
        		+ " (select case "
        		+ " when aciao. status = '已付款确认' then aciao.status "
        		+ " when aciao.status != '已付款确认' and aciao.status !='' then '付款申请中' "
        		+ " else acor.status end as status "
        		+ " from arap_cost_order acor left join arap_cost_invoice_application_order aciao on acor.application_order_id = aciao.id where acor.id = aco.id) as order_status"
        		+ " from arap_cost_order aco "
        		+ " left join party p on p.id = aco.payee_id"
        		+ " left join contact c on c.id = p.contact_id"
        		+ " left join office o ON o.id=p.office_id"
        		+ " left join user_login ul on ul.id = aco.create_by"
        		+ " left join arap_cost_order_invoice_no acoo on acoo.cost_order_id = aco.id ";
        String condition = "";
        //TODO 始发地和目的地 客户没有做
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
	    	arapAuditOrder.set("status", "新建");
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
	    	arapAuditOrder.set("order_no", OrderNoGenerator.getNextOrderNo("YFDZ"));
	    	//arapAuditOrder.set("order_type", );
	    	String sp_id = getPara("sp_id");
	    	if(sp_id == null || "".equals(sp_id)){
	    		sp_id = null;
	    	}
	    	arapAuditOrder.set("status", "新建");
	    	arapAuditOrder.set("payee_id", sp_id);
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
	            	double amount=0.0;
	            	List<Record> BillingOrders = Db.find("select id,amount,change_amount from pickup_order_fin_item where fin_item_id!=7 and pickup_order_id= ?",orderIdsArr[i]);
	            	for(int j=0;j<BillingOrders.size();j++){
	            		Record b=BillingOrders.get(j);
	            		if(b.getDouble("CHANGE_AMOUNT")==null){
	            		 amount = b.getDouble("AMOUNT");
	            		}
	            		else{
	            			amount=b.getDouble("CHANGE_AMOUNT");
	            		}
	            		Long id= b.getLong("ID");
	            		DecimalFormat df = new DecimalFormat("0.00");
	            		String num = df.format(amount);
	            		PickupOrderFinItem pickuporderfinitem =PickupOrderFinItem.dao.findById(id);
	            		pickuporderfinitem.set("change_amount", num);
	            		pickuporderfinitem.update();
	            	}
	            }else if("零担".equals(orderNoArr[i])){
	            	DepartOrder departOrder = DepartOrder.dao.findById(orderIdsArr[i]);
	            	departOrder.set("audit_status", "对账中");
	            	departOrder.update();
	            	double amount=0.0;
	            	List<Record> BillingOrders = Db.find("select id,amount,change_amount from depart_order_fin_item where depart_order_id=?",orderIdsArr[i]);
	            	for(int j=0;j<BillingOrders.size();j++){
	            		Record b=BillingOrders.get(j);
	            		if(b.getDouble("CHANGE_AMOUNT")==null){
	            		 amount = b.getDouble("AMOUNT");
	            		}
	            		else{
	            			amount=b.getDouble("CHANGE_AMOUNT");
	            		}
	            		Long id= b.getLong("ID");
	            		DecimalFormat df = new DecimalFormat("0.00");
	            		String num = df.format(amount);
	            		DepartOrderFinItem departorfinitem =DepartOrderFinItem.dao.findById(id);
	            		departorfinitem.set("change_amount", num);
	            		departorfinitem.update();
	            	}
	            }else if("配送".equals(orderNoArr[i])){
	            	DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(orderIdsArr[i]);
	            	deliveryOrder.set("audit_status", "对账中");
	            	deliveryOrder.update();
	            	double amount=0.0;
	            	List<Record> BillingOrders = Db.find("select id,amount,change_amount from delivery_order_fin_item where order_id=?",orderIdsArr[i]);
	            	for(int j=0;j<BillingOrders.size();j++){
	            		Record b=BillingOrders.get(j);
	            		if(b.getDouble("CHANGE_AMOUNT")==null){
	            		 amount = b.getDouble("AMOUNT");
	            		}
	            		else{
	            			amount=b.getDouble("CHANGE_AMOUNT");
	            		}
	            		Long id= b.getLong("ID");
	            		DecimalFormat df = new DecimalFormat("0.00");
	            		String num = df.format(amount);
	            		DeliveryOrderFinItem deliveryfinitem =DeliveryOrderFinItem.dao.findById(id);
	            		deliveryfinitem.set("change_amount", num);
	            		deliveryfinitem.update();
	            	}
	            }else if("成本单".equals(orderNoArr[i])){
	            	ArapMiscCostOrder arapmisc = ArapMiscCostOrder.dao.findById(orderIdsArr[i]);
	            	arapmisc.set("audit_status", "对账中");
	            	arapmisc.update();
	            	double amount=0.0;
	            	List<Record> BillingOrders = Db.find("select id,amount,change_amount from arap_misc_cost_order_item amcoi where misc_order_id=?",orderIdsArr[i]);
	            	for(int j=0;j<BillingOrders.size();j++){
	            		Record b=BillingOrders.get(j);
	            		if(b.getDouble("CHANGE_AMOUNT")==null){
	            		 amount = b.getDouble("AMOUNT");
	            		}
	            		else{
	            			amount=b.getDouble("CHANGE_AMOUNT");
	            		}
	            		Long id= b.getLong("ID");
	            		DecimalFormat df = new DecimalFormat("0.00");
	            		String num = df.format(amount);
	            		ArapMiscCostOrderItem arapmiscorderitem =ArapMiscCostOrderItem.dao.findById(id);
	            		arapmiscorderitem.set("change_amount", num);
	            		arapmiscorderitem.update();
	            	}
	            	
	            }else{
	            	InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(orderIdsArr[i]);
	            	insuranceOrder.set("audit_status", "对账中");
	            	insuranceOrder.update();
	            	double amount=0.0;
	            	List<Record> BillingOrders = Db.find("select id,insurance_amount,change_amount from insurance_fin_item ifi where ifi.insurance_order_id=?",orderIdsArr[i]);
	            	for(int j=0;j<BillingOrders.size();j++){
	            		Record b=BillingOrders.get(j);
	            		if(b.getDouble("CHANGE_AMOUNT")==null){
	            		 amount = b.getDouble("INSURANCE_AMOUNT");
	            		}
	            		else{
	            			amount=b.getDouble("CHANGE_AMOUNT");
	            		}
	            		Long id= b.getLong("ID");
	            		DecimalFormat df = new DecimalFormat("0.00");
	            		String num = df.format(amount);
	            		InsuranceFinItem insurancefinitem =InsuranceFinItem.dao.findById(id);
	            		insurancefinitem.set("change_amount", num);
	            		insurancefinitem.update();
	            	}
	            	
	            }
	    	}
    	}
        renderJson(arapAuditOrder);
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
    	
    	UserLogin create_user = UserLogin.dao.findById(arapAuditOrder.get("create_by"));
    	setAttr("create_user", create_user);
    	UserLogin confirm_user = UserLogin.dao.findById(arapAuditOrder.get("confirm_by"));
    	setAttr("confirm_user", confirm_user);
    	
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
    	//调整金额
    	String[] orderIdsArr = orderIds.split(",");
    	String[] orderNoArr = orderNos.split(",");
    	Record rec1 = null;
    	Record rec = null;
    	Double totalAmount = 0.00;
    	Double changeAmount = 0.00;
    	for(int i=0;i<orderIdsArr.length;i++){
            if("提货".equals(orderNoArr[i])){
            	rec1 = Db.findFirst("select sum(amount) sum_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	totalAmount = totalAmount + rec1.getDouble("sum_amount");
            	rec = Db.findFirst("select sum(change_amount) change_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ?", orderIdsArr[i]);
            	if(rec.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec.getDouble("change_amount");
            	}
            }else if("零担".equals(orderNoArr[i])){
            	rec1 = Db.findFirst("select sum(amount) sum_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	totalAmount = totalAmount + rec1.getDouble("sum_amount");
            	rec = Db.findFirst("select sum(change_amount) change_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ?", orderIdsArr[i]);
            	if(rec.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec.getDouble("change_amount");
            	}
            }else if("配送".equals(orderNoArr[i])){
            	rec1 = Db.findFirst("select sum(amount) sum_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	totalAmount = totalAmount + rec1.getDouble("sum_amount");
            	rec = Db.findFirst("select sum(change_amount) change_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ?", orderIdsArr[i]);
            	if(rec.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec.getDouble("change_amount");
            	}
            }else if("成本单".equals(orderNoArr[i])){
            	rec1 = Db.findFirst("select sum(amount) sum_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ? and fi.type ='应付'",orderIdsArr[i]);
            	totalAmount = totalAmount + rec1.getDouble("sum_amount");
            	rec = Db.findFirst("select sum(change_amount) change_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ?",orderIdsArr[i]);
            	if(rec.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec.getDouble("change_amount");
            	}
            }else{
            	rec1 = Db.findFirst("select sum(insurance_amount) sum_amount from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id  where ifi.insurance_order_id = ? and fi.type ='应付'",orderIdsArr[i]);
            	totalAmount = totalAmount + rec1.getDouble("sum_amount");
            	rec = Db.findFirst("select sum(change_amount) change_amount from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id  where ifi.insurance_order_id = ?",orderIdsArr[i]);
            	if(rec.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec.getDouble("change_amount");
            	}
            }
            Double actualAmount=totalAmount-changeAmount;
            setAttr("totalAmount", totalAmount);
            setAttr("changeAmount", changeAmount);
            setAttr("actualAmount", actualAmount);
    	}
    	
    	
    		render("/yh/arap/CostCheckOrder/CostCheckOrderEdit.html");
    }

	// 审核

	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCOI_AFFIRM})
	@Before(Tx.class)
	public void auditCostCheckOrder(){
		String costCheckOrderId = getPara("costCheckOrderId");
		ArapCostOrder arapAuditOrder = null;
		if(costCheckOrderId != null && !"".equals(costCheckOrderId)){
			arapAuditOrder = ArapCostOrder.dao.findById(costCheckOrderId);
			arapAuditOrder.set("status", "已确认");
	        String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
	        arapAuditOrder.set("confirm_by", users.get(0).get("id"));
	        arapAuditOrder.set("confirm_stamp", new Date());
			arapAuditOrder.update();
			List<ArapMiscCostOrder> list = ArapMiscCostOrder.dao.find("select * from arap_misc_cost_order where cost_order_id = ?",arapAuditOrder.get("id"));
			if(list.size()>0){
				for (ArapMiscCostOrder arapMiscCostOrder : list) {
					arapMiscCostOrder.set("status", "对账已确认");
					arapMiscCostOrder.update();
				}
			}
			
			
			//updateReturnOrderStatus(arapAuditOrder, "对账已确认");
		}
		Map BillingOrderListMap = new HashMap();
		UserLogin ul = UserLogin.dao.findById(arapAuditOrder.get("confirm_by"));
		BillingOrderListMap.put("arapAuditOrder", arapAuditOrder);
		BillingOrderListMap.put("ul", ul);
        renderJson(BillingOrderListMap);
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
        String booking_id= getPara("booking_id");;
        String orderNo = getPara("orderNo");
    	String sp = getPara("sp");
    	String no = getPara("no");
    	String beginTime = getPara("beginTime");
    	String endTime = getPara("endTime");
    	String type = getPara("type");
    	String status = getPara("status");
    	
    	String sqlTotal = "";
    	String sql = " select * from (select distinct dor.id, dpr.route_from ,lo.name from_name ,dpr.route_to ,lo2.name to_name, tor.planning_time ,dor.order_no order_no,dor.status,c.abbr spname,"
    						+ " (SELECT sum(doi1.amount) FROM delivery_order_item doi1 WHERE doi1.delivery_id = dor.id ) amount, "
    						+ " ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator,'配送' business_type, (select sum(amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.type = '应付') pay_amount,"
    			            + " ((select sum(dofi.amount) from delivery_order_fin_item dofi where dofi.order_id = dor.id)-(select sum(tofi.amount) from	transfer_order tor"
        		            + " left join transfer_order_fin_item tofi on tofi.order_id = tor.id"
        		            + " left join fin_item fi on fi.id = tofi.fin_item_id"
        		            + " where fi.type = '应付' and tor.id = doi.transfer_order_id)) alance,"
    			            + " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '\r\n') transfer_order_no,dor.sign_status return_order_collection,dor.remark,dpr.booking_note_number,oe.office_name office_name, "
    			            + " toid.serial_no, dor.ref_no"
							+ " from delivery_order dor"
							+ " left join party p on p.id = dor.sp_id "
							+ " left join depart_order dpr on dpr.sp_id=p.id"
							+ " left join contact c on c.id = p.contact_id "
							+ " left join delivery_order_item doi on doi.delivery_id = dor.id "
							+ " left join transfer_order_item_detail toid on toid.id = doi.transfer_item_detail_id "
							+ " left join transfer_order_item toi on toi.id = toid.item_id "
							+ " left join transfer_order tor on tor.id = toi.order_id "
							+ " left join product prod on toi.product_id = prod.id "
							+ " left join user_login ul on ul.id = dor.create_by "
							+ " left join warehouse w on w.id = dor.from_warehouse_id "
							+ " left join location lo on lo.code = dpr.route_from "
							+ " left join location lo2 on lo2.code = dpr.route_to "
							+ " left join office oe on oe.id = w.office_id where dor.audit_status='已确认' group by dor.id "
							+ " union"
							+ " select distinct dpr.id, dpr.route_from ,lo.name from_name ,dpr.route_to ,lo2.name to_name ,tor.planning_time ,dpr.depart_no order_no,dpr.status,c.abbr spname,"
							+ " ( SELECT CASE WHEN tor.cargo_nature = 'ATM' THEN ( SELECT count(toid.id) FROM transfer_order_item_detail toid  "
							+ " WHERE toid.depart_id = dpr.id ) WHEN tor.cargo_nature = 'cargo' THEN ( SELECT sum(toi.amount) FROM "
							+ " depart_order dpr2 LEFT JOIN depart_transfer dt ON dt.depart_id = dpr2.id "
							+ " LEFT JOIN transfer_order_item toi ON toi.order_id = dt.order_id  "
							+ " WHERE dpr2.id = dpr.id ) END ) amount, "
							+ " ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'零担' business_type, (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount,"
							+ " ((select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id  and fi.type = '应付')-(select sum(tofi.amount)"
							+ " from transfer_order tor"
							+ " left join transfer_order_fin_item tofi on tofi.order_id = tor.id"
							+ " left join fin_item fi on fi.id = tofi.fin_item_id"
							+ " where fi.type = '应付' and tor.id = dtr.order_id)) alance,"
							+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n') transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,dpr.booking_note_number,oe.office_name office_name, "
							+ " '' serial_no, '' ref_no"
							+ " from depart_order dpr "
							+ " left join depart_transfer dtr on dtr.depart_id = dpr.id"
							+ " left join transfer_order tor on tor.id = dtr.order_id "
							+ " left join transfer_order_item toi on toi.order_id = tor.id "
							+ " left join product prod on toi.product_id = prod.id "
							+ " left join user_login ul on ul.id = dpr.create_by left join party p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
							+ " left join location lo on lo.code = dpr.route_from "
							+ " left join location lo2 on lo2.code = dpr.route_to "
							+ " left join office oe on oe.id = tor.office_id where  (ifnull(dtr.depart_id, 0) > 0) and dpr.audit_status='已确认' AND dpr.combine_type = 'DEPART' group by dpr.id"
							+ " union "
							+ " select distinct dpr.id, dpr.route_from ,lo.name from_name ,dpr.route_to ,lo2.name to_name ,tor.planning_time ,dpr.depart_no order_no,dpr.status,c.abbr spname,"
							+ " ( SELECT CASE WHEN tor.cargo_nature = 'ATM' THEN ( "
							+ " SELECT count(toid.id) FROM transfer_order_item_detail toid "
							+ " WHERE toid.pickup_id = dpr.id ) "
							+ " WHEN tor.cargo_nature = 'cargo' THEN "
							+ " ( SELECT sum(toi.amount) FROM depart_order dpr2 "
							+ " LEFT JOIN depart_transfer dt ON dt.pickup_id = dpr2.id "
							+ " LEFT JOIN transfer_order_item toi ON toi.order_id = dt.order_id "
							+ " WHERE dpr2.id = dpr.id ) END ) amount,"
							+ " ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'提货' business_type, (select sum(amount) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付') pay_amount, "
							+ " ((select sum(pofi.amount) from pickup_order_fin_item pofi  where pofi.pickup_order_id = dpr.id )-(select sum(tofi.amount)"
							+ " from transfer_order tor"
							+ " left join transfer_order_fin_item tofi on tofi.order_id = tor.id"
							+ " left join fin_item fi on fi.id = tofi.fin_item_id"
							+ " where fi.type = '应付' and tor.id = dtr.order_id)) alance,"
							+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n') transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,dpr.booking_note_number,oe.office_name office_name, "
							+ " '' serial_no, '' ref_no"
							+ " from depart_order dpr "
							+ " left join depart_transfer dtr on dtr.pickup_id = dpr.id"
							+ " left join transfer_order tor on tor.id = dtr.order_id "
							+ " left join transfer_order_item toi on toi.order_id = tor.id "
							+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id "
							+ " left join product prod on toi.product_id = prod.id "
							+ " left join user_login ul on ul.id = dpr.create_by left join party p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
							+ " left join location lo on lo.code = dpr.route_from "
							+ " left join location lo2 on lo2.code = dpr.route_to "
							+ " left join office oe on oe.id = tor.office_id where (ifnull(dtr.pickup_id, 0) > 0) and dpr.audit_status='已确认' AND dpr.combine_type = 'PICKUP' group by dpr.id"
							+ " union "
							+ " select distinct ior.id, dpr.route_from ,lo.name from_name ,dpr.route_to ,lo2.name to_name ,tor.planning_time ,ior.order_no order_no,ior.status,'保险公司' spname,sum(toi.amount) amount,round(sum(ifnull(prod.volume,toi.volume)),2) volume,round(sum(ifnull(prod.weight,toi.weight)),2) weight,ior.create_stamp create_stamp,ul.user_name creator,'保险' business_type, round((select sum(insurance_amount) from insurance_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.insurance_order_id = ior.id and fi.type = '应付'),2) pay_amount, "
							+ " ((select sum(ifi.insurance_amount) from insurance_fin_item ifi  where ifi.insurance_order_id = ior.id )-(select sum(tofi.amount)"
							+ " from transfer_order tor"
							+ " left join transfer_order_fin_item tofi on tofi.order_id = tor.id"
							+ " left join fin_item fi on fi.id = tofi.fin_item_id"
							+ " where fi.type = '应付' and fi.name='保险费' and ior.id = tor.insurance_id)) alance,"
							+ " group_concat(distinct tor.order_no separator '\r\n') transfer_order_no,ior.sign_status return_order_collection,ior.remark,dpr.booking_note_number,oe.office_name office_name, "
							+ " '' serial_no, '' ref_no"
							+ " from insurance_order ior "
							+ " left join transfer_order tor on ior.id = tor.insurance_id "
							+ " left join transfer_order_item toi on toi.order_id = tor.id "
							+ " left join party p ON p.id=tor.sp_id"
                            + " left join depart_order dpr ON dpr.sp_id=p.id"
							+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id "
							+ " left join product prod on toi.product_id = prod.id "
							+ " left join user_login ul on ul.id = ior.create_by"
							+ " left join office oe on oe.id = tor.office_id "
							+ " left join location lo on lo.code = dpr.route_from "
							+ " left join location lo2 on lo2.code = dpr.route_to "
							+ " where ior.audit_status='已确认' group by ior.id"
							+ " union "
							+ " SELECT DISTINCT amco.id,amco.route_from,l. NAME route_name,"
							+ " amco.route_to,l1. NAME to_name,NULL AS planning_time,"
							+ " amco.order_no,amco. STATUS,c.abbr spname,NULL AS amount,"
							+ " NULL AS volume,NULL AS weight,amco.create_stamp,"
							+ " ul.user_name creator,'成本单' business_type,"
							+ " amco.total_amount pay_amount, null as alance,NULL AS transfer_order_no,"
							+ " NULL AS return_order_collection,amco.remark remark,"
							+ " NULL AS booking_note_number,NULL AS office_name,"
							+ " '' serial_no, '' ref_no "
							+ " FROM arap_misc_cost_order amco"
							+ " LEFT JOIN user_login ul ON ul.id = amco.create_by"
							+ " LEFT JOIN party p1 ON amco.customer_id = p1.id"
							+ " LEFT JOIN contact c1 ON p1.contact_id = c1.id"
							+ " LEFT JOIN party p ON amco.sp_id = p.id"
							+ " LEFT JOIN contact c ON p.contact_id = c.id"
							+ " LEFT JOIN location l ON amco.route_from = l. CODE"
							+ " LEFT JOIN location l1 ON amco.route_to = l1. CODE"
							+ " WHERE	amco.audit_status = '已确认'"
							+ " GROUP BY amco.id) as newView " ;
    	String condition = "";
    	
    	
    	if(orderNo != null || sp != null || no != null || beginTime != null
    			|| endTime != null || type != null || status != null){
    		String time ="";
			if ((beginTime == null || "".equals(beginTime))&&(endTime == null || "".equals(endTime))) {
				time = "1970-01-01";
			}
    		if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-01-01";
			}
    		
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
    		condition = " where ifnull(transfer_order_no,'') like '%" + orderNo + "%' "
    					+ " and order_no like '%" + no + "%' "
    					+ " and business_type like '%" + type + "%' "
    					+ " and status like '%" + status + "%' "
    					+ " and spname like '%" + sp + "%' "
    					+ " and ifnull(booking_note_number,'')  like '%"+booking_id+"%'"
    					+ " and ifnull(planning_time,'"+time+"') between '" + beginTime + "' and '" + endTime + " 23:59:59' ";
    		
    	}
    	
        sqlTotal = "select count(1) total from (" + sql + condition + ") as B";  
        
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql + condition + "order by planning_time"+sLimit);

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
    	String insuranceId = "";
    	String arapmiscId = "";
    	if(orderIds == null || orderIds == ""){
    		pickupId = "-1";
        	departId = "-1";
        	deliveryId = "-1";
        	insuranceId = "-1";
        	arapmiscId ="-1";
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
	            }else if("成本单".equals(orderNoArr[i])){
	            	arapmiscId += orderIdsArr[i] + ",";
	            }else{
	            	insuranceId += orderIdsArr[i] + ",";
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
	    	if(arapmiscId != null && !"".equals(arapmiscId)){
	    		arapmiscId = arapmiscId.substring(0, arapmiscId.length() - 1);
			} else{
				arapmiscId = "-1";
			}
	    	if(insuranceId != null && !"".equals(insuranceId)){
	    		insuranceId = insuranceId.substring(0, insuranceId.length() - 1);
	    	} else{
	    		insuranceId = "-1";
	    	}
    	}

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}		
    	String searchSql = "select distinct dor.id,dofi.id did,dor.order_no order_no,dor.status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator,'配送' business_type, (select sum(amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.type = '应付') pay_amount,(SELECT ifnull(sum(change_amount),0) FROM delivery_order_fin_item dofi WHERE dofi.order_id = dor.id) change_amount, group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '\r\n') transfer_order_no,dor.sign_status return_order_collection,dor.remark,oe.office_name office_name "
							+ " from delivery_order dor"
							+ " left join party p on p.id = dor.sp_id "
							+ " LEFT JOIN delivery_order_fin_item dofi on dofi.order_id=dor.id"
							+ " left join contact c on c.id = p.contact_id "
							+ " left join delivery_order_item doi on doi.delivery_id = dor.id "
							+ " left join transfer_order_item_detail toid on toid.id = doi.transfer_item_detail_id "
							+ " left join transfer_order_item toi on toi.id = toid.item_id "
							+ " left join product prod on toi.product_id = prod.id "
							+ " left join user_login ul on ul.id = dor.create_by "
							+ " left join warehouse w on w.id = dor.from_warehouse_id "
							+ " left join office oe on oe.id = w.office_id where dor.id in("+deliveryId+") group by dor.id "
							+ " union"
							+ " select distinct dpr.id,dofi.id did,dpr.depart_no order_no,dpr.status,c.abbr spname,(SELECT COUNT(0) FROM transfer_order_item_detail WHERE depart_id = dpr.id) amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'零担' business_type, (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount,(SELECT ifnull(sum(change_amount),0) FROM depart_order_fin_item dofi WHERE dofi.depart_order_id = dpr.id) change_amount, group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n') transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name "
							+ " from depart_order dpr "
							+ " left join depart_transfer dtr on dtr.depart_id = dpr.id"
							+ " LEFT JOIN depart_order_fin_item dofi on dofi.depart_order_id=dpr.id"
							+ " left join transfer_order tor on tor.id = dtr.order_id "
							+ " left join transfer_order_item toi on toi.order_id = tor.id "
							+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id "
							+ " left join product prod on toi.product_id = prod.id "
							+ " left join user_login ul on ul.id = dpr.create_by left join party p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
							+ " left join office oe on oe.id = tor.office_id where (ifnull(dtr.depart_id, 0) > 0) and dpr.id in("+departId+") group by dpr.id"
							+ " union "
							+ " select distinct dpr.id,dofi.id did,dpr.depart_no order_no,dpr.status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'提货' business_type, (select sum(amount) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付') pay_amount,(SELECT ifnull(sum(change_amount),0) FROM pickup_order_fin_item dofi WHERE dofi.pickup_order_id = dpr.id) change_amount, group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n') transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name "
							+ " from depart_order dpr "
							+ " left join depart_transfer dtr on dtr.pickup_id = dpr.id"
							+ " LEFT JOIN pickup_order_fin_item dofi on dofi.pickup_order_id=dpr.id"
							+ " left join transfer_order tor on tor.id = dtr.order_id "
							+ " left join transfer_order_item toi on toi.order_id = tor.id "
							+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id "
							+ " left join product prod on toi.product_id = prod.id "
							+ " left join user_login ul on ul.id = dpr.create_by left join party p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
							+ " left join office oe on oe.id = tor.office_id where (ifnull(dtr.pickup_id, 0) > 0) and dpr.id in("+pickupId+") group by dpr.id"
							+ " union "
							+ " select distinct ior.id,ifi.id did,ior.order_no order_no,ior.status,'保险公司' spname,sum(toi.amount) amount,sum(ifnull(prod.volume,toi.volume)) volume,sum(ifnull(prod.weight,toi.weight)) weight,ior.create_stamp create_stamp,ul.user_name creator,'保险' business_type, (select sum(insurance_amount) from insurance_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.insurance_order_id = ior.id and fi.type = '应付') pay_amount,(SELECT ifnull(sum(change_amount),0) FROM insurance_fin_item dofi WHERE dofi.insurance_order_id = ior.id) change_amount, group_concat(distinct tor.order_no separator '\r\n') transfer_order_no,ior.sign_status return_order_collection,ior.remark,oe.office_name office_name "
							+ " from insurance_order ior "
							+ " left join transfer_order tor on ior.id = tor.insurance_id "
							+ " LEFT JOIN insurance_fin_item ifi ON ifi.insurance_order_id=ior.id"
							+ " left join transfer_order_item toi on toi.order_id = tor.id "
							+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id "
							+ " left join product prod on toi.product_id = prod.id "
							+ " left join user_login ul on ul.id = ior.create_by "
							+ " left join office oe on oe.id = tor.office_id where ior.id in("+insuranceId+") group by ior.id "
							+ " union "
							+ " SELECT DISTINCT amco.id,amcoi.id did,amco.order_no,amco. STATUS,c.abbr spname,amco.total_amount,NULL AS volume,NULL AS weight,amco.create_stamp,ul.user_name creator,'成本单' business_type,(SELECT sum(amount) FROM arap_misc_cost_order_item amcoi LEFT JOIN fin_item fi ON fi.id = amcoi.fin_item_id WHERE amcoi.misc_order_id= amco.id ) pay_amount,(SELECT ifnull(sum(change_amount),0) FROM arap_misc_cost_order_item amcoi LEFT JOIN fin_item fi ON fi.id = amcoi.fin_item_id WHERE amcoi.misc_order_id= amco.id ) AS change_amount,NULL AS transfer_order_no,NULL AS return_order_collection,amco.remark remark,NULL AS office_name "
							+ " FROM arap_misc_cost_order amco "
							+ " LEFT JOIN arap_misc_cost_order_item amcoi ON amcoi.misc_order_id = amco.id "
							+ " LEFT JOIN user_login ul ON ul.id = amco.create_by "
							+ " LEFT JOIN party p ON amco.sp_id = p.id "
							+ " LEFT JOIN contact c ON p.contact_id = c.id WHERE amco.id in ("+arapmiscId+") GROUP BY amco.id";
    	
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
	public void updateDepartOrderFinItem() {
		String paymentId = getPara("paymentId");
		String tId = getPara("departId");
		String name = getPara("name");
		String value = getPara("value");
		String orderNos = getPara("orderNos");
		String orderIds= getPara("ids");
		String type=getPara("ty");
		String[] orderIdsArr = orderIds.split(",");
    	String[] orderNoArr = orderNos.split(",");
    	Record rec = null;
    	Record rec1 = null;
    	Record rec2 = null;
    	Record rec3 = null;
    	double totalAmount = 0.0;
    	double changeAmount=0.0;
    	double changeAmount1=0.0;
 		for(int i=0;i<orderIdsArr.length;i++){
            if("提货".equals(orderNoArr[i])){
            	rec1 = Db.findFirst("select sum(amount) sum_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	totalAmount = totalAmount + rec1.getDouble("sum_amount");	
            	List<Record> BillingOrders = Db.find("select id,amount from pickup_order_fin_item where fin_item_id!=7 and pickup_order_id=?",tId);
            	if("提货".equals(type)){
            		rec2 = Db.findFirst("select sum(amount) sum_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ? and fi.type = '应付'", tId);
                	changeAmount1 = rec2.getDouble("sum_amount");
            		for(int j=0;j<BillingOrders.size();j++){
            		Record b=BillingOrders.get(j);
            		double amount = b.getDouble("AMOUNT");
            		Long id= b.getLong("ID");
            		double dd = Double.valueOf(value);
            		double amount1= amount/changeAmount1*dd;
            		DecimalFormat df = new DecimalFormat("0.00");
            		String num = df.format(amount1);
            		PickupOrderFinItem pickuporderfinitem =PickupOrderFinItem.dao.findById(id);
            		pickuporderfinitem.set("change_amount", num);
            		pickuporderfinitem.update();
            	}
            	}
            	rec = Db.findFirst("select sum(amount) amount,sum(change_amount) change_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where fin_item_id!=7 and  dofi.pickup_order_id = ?", orderIdsArr[i]);
            	if(rec.getDouble("change_amount")!=null){
            		changeAmount = changeAmount + rec.getDouble("change_amount");
            	}
            	else{
            		changeAmount = changeAmount + rec.getDouble("amount");
            	}
            }else if("零担".equals(orderNoArr[i])){
            	rec3 = Db.findFirst("select * from depart_order_fin_item  where depart_order_id =?", orderIdsArr[i]);
            	if(rec3==null){
            		DepartOrderFinItem dofi = new DepartOrderFinItem();
            		dofi.set("depart_order_id", orderIdsArr[i]);
            		dofi.set("fin_item_id", "1");
            		dofi.set("amount", "0");
            		dofi.set("change_amount", value);
            		dofi.set("STATUS", "新建");
            		dofi.set("create_date", new Date());
            		dofi.set("create_name", "user");
            		dofi.set("cost_source", "对账确认金额为0增加明细");
            		dofi.save();
            	}
            	rec1 = Db.findFirst("select sum(amount) sum_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	totalAmount = totalAmount + rec1.getDouble("sum_amount");
            	List<Record> BillingOrders = Db.find("select id,amount from depart_order_fin_item where depart_order_id=?",tId);
            	if("零担".equals(type)){
            		rec2 = Db.findFirst("select sum(amount) sum_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ? and fi.type = '应付'", tId);
                	changeAmount1 = rec2.getDouble("sum_amount");
            		for(int j=0;j<BillingOrders.size();j++){
            		Record b=BillingOrders.get(j);
            		double amount = b.getDouble("AMOUNT");
            		Long id= b.getLong("ID");
            		double dd = Double.valueOf(value);
            		double amount1= amount/changeAmount1*dd;
            		DecimalFormat df = new DecimalFormat("0.00");
            		String num = df.format(amount1);
            		DepartOrderFinItem departorfinitem =DepartOrderFinItem.dao.findById(id);
            		if(changeAmount1==0.0){
            			departorfinitem.set("change_amount", value);
                		departorfinitem.update();
            		}
            		else{
            		departorfinitem.set("change_amount", num);
            		departorfinitem.update();
            		}
            	}
            	}
            		
                	rec = Db.findFirst("select sum(amount) amount, sum(change_amount) change_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ?", orderIdsArr[i]);
                	if(rec.getDouble("change_amount")!=null){
                		changeAmount = changeAmount + rec.getDouble("change_amount");
                	}
                	else{
                		changeAmount = changeAmount + rec.getDouble("amount");
                	}
                	
            	
            	rec2 =Db.findFirst("select amount from depart_order_fin_item where depart_order_id=?",tId);
            	//DepartOrderFinItem DepartOrderFinItem =DepartOrderFinItem.dao.findById();
            	//departorder.set("change_amount", value);
            	//departorder.update();
            	
            }else if("配送".equals(orderNoArr[i])){
            	rec3 = Db.findFirst("select * from delivery_order_fin_item  where order_id =?", orderIdsArr[i]);
            	if(rec3==null){
            		DeliveryOrderFinItem dofi = new DeliveryOrderFinItem();
            		dofi.set("order_id", orderIdsArr[i]);
            		dofi.set("fin_item_id", "1");
            		dofi.set("amount", "0");
            		dofi.set("change_amount", value);
            		dofi.set("STATUS", "新建");
            		dofi.set("create_date", new Date());
            		dofi.set("create_name", "user");
            		dofi.save();
            	}
            	rec1 = Db.findFirst("select sum(amount) sum_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ? and fi.type = '应付'", orderIdsArr[i]);
            	totalAmount = totalAmount + rec1.getDouble("sum_amount");
            	List<Record> BillingOrders = Db.find("select id,amount from delivery_order_fin_item where order_id=?",tId);
            	if("配送".equals(type)){for(int j=0;j<BillingOrders.size();j++){
            		rec2 = Db.findFirst("select sum(amount) sum_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ? and fi.type = '应付'", tId);
                	changeAmount1 = rec2.getDouble("sum_amount");
            		Record b=BillingOrders.get(j);
            		double amount = b.getDouble("AMOUNT");
            		Long id= b.getLong("ID");
            		double dd = Double.valueOf(value);
            		double amount1= amount/changeAmount1*dd;
            		DecimalFormat df = new DecimalFormat("0.00");
            		String num = df.format(amount1);
            		DeliveryOrderFinItem deliveryfinitem =DeliveryOrderFinItem.dao.findById(id);
            		if(changeAmount1==0.0){
            			deliveryfinitem.set("change_amount", value);
            			deliveryfinitem.update();
            		}else{
            		deliveryfinitem.set("change_amount", num);
            		deliveryfinitem.update();
            		}
            	}
            	}
            	rec = Db.findFirst("select sum(amount) amount, sum(change_amount) change_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ?", orderIdsArr[i]);
            	if(rec.getDouble("change_amount")!=null){
            		changeAmount = changeAmount + rec.getDouble("change_amount");
            	}
            	else{
            		changeAmount = changeAmount + rec.getDouble("amount");
            	}
            }else if("成本单".equals(orderNoArr[i])){
            	ArapMiscCostOrderItem misccostorderitem =ArapMiscCostOrderItem.dao.findById(paymentId);
            	rec1 = Db.findFirst("select sum(amount) sum_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ? and fi.type ='应付'",orderIdsArr[i]);
            	totalAmount = totalAmount + rec1.getDouble("sum_amount");
            	List<Record> BillingOrders = Db.find("select id,amount from arap_misc_cost_order_item amcoi where misc_order_id=?",tId);
            	if("成本单".equals(type)){for(int j=0;j<BillingOrders.size();j++){
            		rec2 = Db.findFirst("select sum(amount) sum_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ? and fi.type ='应付'?", tId);
                	changeAmount1 = rec2.getDouble("sum_amount");
            		Record b=BillingOrders.get(j);
            		double amount = b.getDouble("AMOUNT");
            		Long id= b.getLong("ID");
            		double dd = Double.valueOf(value);
            		double amount1= amount/changeAmount1*dd;
            		DecimalFormat df = new DecimalFormat("0.00");
            		String num = df.format(amount1);
            		ArapMiscCostOrderItem ArapMiscOrderItem =ArapMiscCostOrderItem.dao.findById(id);
            		ArapMiscOrderItem.set("change_amount", num);
            		ArapMiscOrderItem.update();
            	}
            	}
            	
            	rec = Db.findFirst("select sum(change_amount) change_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ?",orderIdsArr[i]);
            	if(rec.getDouble("change_amount")!=null){
            	changeAmount = changeAmount + rec.getDouble("change_amount");
            	}
            }else if("保险".equals(orderNoArr[i])){
            	InsuranceFinItem insurancefinitem =InsuranceFinItem.dao.findById(paymentId);
            	if(value.equals("")){
            		value=null;
            	}
            	insurancefinitem.set(name, value);
            	insurancefinitem.update();
            	rec1 = Db.findFirst("select sum(insurance_amount) sum_amount from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id  where ifi.insurance_order_id = ? and fi.type ='应付'",orderIdsArr[i]);
            	totalAmount = totalAmount + rec1.getDouble("sum_amount");
            	rec = Db.findFirst("select sum(insurance_amount) amount,sum(change_amount) change_amount from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id  where ifi.insurance_order_id = ?",orderIdsArr[i]);
            	if(rec.getDouble("change_amount")!=null){
            		changeAmount = changeAmount + rec.getDouble("change_amount");
            	}
            	else{
            		changeAmount = changeAmount + rec.getDouble("amount");
            	}
            }
    	}
		if ("change_amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		/*if (paymentId != null && !"".equals(paymentId)) {
			DepartOrderFinItem departOrderFinItem = DepartOrderFinItem.dao
					.findById(paymentId);
			departOrderFinItem.set(name, value);
			departOrderFinItem.update();
		}*/
		Map map = new HashMap();
		map.put("changeAmount", changeAmount);
		renderJson(map);
		//renderJson("{\"success\":true}");
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
	//保存后，手工单状态转为“已入对账单”。
	public void updateCostMiscOrder(){
		String micsOrderIds = getPara("micsOrderIds");
		String costCheckOrderId = getPara("costCheckOrderId");
		ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(costCheckOrderId);
		if(micsOrderIds != null && !"".equals(micsOrderIds)){
			String[] micsOrderIdArr = micsOrderIds.split(",");
			for(int i=0;i<micsOrderIdArr.length;i++){
				ArapMiscCostOrder arapMisccostOrder = ArapMiscCostOrder.dao.findById(micsOrderIdArr[i]);
				arapMisccostOrder.set("cost_order_id", costCheckOrderId);
				arapMisccostOrder.set("status", "已入对账单");
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
