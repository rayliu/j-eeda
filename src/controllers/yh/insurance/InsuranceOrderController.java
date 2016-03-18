package controllers.yh.insurance;

import interceptor.SetAttrLoginUserInterceptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.FinItem;
import models.InsuranceFinItem;
import models.InsuranceOrder;
import models.Party;
import models.Product;
import models.TransferOrder;
import models.TransferOrderItem;
import models.UserLogin;
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
import controllers.yh.util.getCustomFile;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class InsuranceOrderController extends Controller {
    private Logger logger = Logger.getLogger(InsuranceOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_IO_LIST})
    public void index() {
    	    render("/yh/insuranceOrder/insuranceOrderList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_IO_CREATE})
    public void add() {
    	Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
    	setAttr("customizeField", customizeField);
    	render("/yh/insuranceOrder/insuranceOrderSearchTransfer.html");
    }

    public void create() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        String customerId = getPara("customerId");
        Party party = Party.dao.findById(customerId);

        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
        setAttr("customer", contact);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
    		render("/yh/insuranceOrder/insuranceOrderEdit.html");
    }

    // 保险单选取运输单
    public void createList() {
    	
        Map transferOrderListMap = null;
        String orderNo = getPara("orderNo");

        String customer = getPara("customer");
        String routeFrom = getPara("routeFrom");
        String routeTo = getPara("routeTo");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        String sql = "";
        String sqlTotal = "";
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String fromSql= " from transfer_order tor "
                + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                + " left join party p2 on tor.sp_id = p2.id " + " left join contact c2 on p2.contact_id = c2.id "
                + " left join user_login ul on ul.id = tor.create_by "  
                + " left join office o on o.id = tor .office_id";
        
        
        if (orderNo == null &&  customer == null && routeFrom == null && routeTo == null
                && beginTime == null && endTime == null) {
            sqlTotal = "select count(1) total "
            		+ fromSql
                    + " where tor.insurance_id is null and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.insurance_id is null and tor. STATUS != '手动删除' AND tor.order_type != 'cargoReturnOrder' and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
            sql = "select tor.id,tor.order_no,tor.planning_time,tor.operation_type,tor.cargo_nature,tor.order_type,"
            		+ "	(SELECT l.NAME FROM location l where l.code = tor.route_from) route_from,"
            		+ " (SELECT	l.NAME FROM location l where l.code = tor.route_to) route_to,"
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ifnull(nullif(ul.c_name,''),ul.user_name) create_by,tor.customer_order_no,"
                    + " tor.create_stamp,tor.pickup_assign_status,"
                    + " tor.create_stamp start_create_stamp "
                    + fromSql
                    + " where tor.insurance_id is null "    
                    + " and tor. STATUS != '手动删除' AND tor.order_type != 'cargoReturnOrder' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
                    + " order by tor.planning_time asc" + sLimit;
        } else{
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }
            sqlTotal = "select count(1) total "
            		+ fromSql
                    + " where tor.insurance_id is null and tor. STATUS != '手动删除' AND tor.order_type != 'cargoReturnOrder' and ifnull((SELECT l. NAME FROM location l WHERE l. CODE = tor.route_from), '') like '%"
                    + routeFrom
                    + "%' and ifnull((SELECT l. NAME FROM location l WHERE l. CODE = tor.route_to), '') like '%"
                    + routeTo
                    + "%'"
                    + "and ifnull(tor.order_no,'') like '%"
                    + orderNo
                    + "%' and ifnull(c.abbr,'') like '%"
                    + customer
                    + "%' and (tor.create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "')  and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
            sql = "select tor.id,tor.order_no,tor.planning_time,tor.operation_type,tor.cargo_nature,tor.order_type,"
            		+ "	(SELECT l.NAME FROM location l where l.code = tor.route_from) route_from,"
            		+ " (SELECT	l.NAME FROM location l where l.code = tor.route_to) route_to,"
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ifnull(nullif(ul.c_name,''),ul.user_name) create_by,tor.customer_order_no,"
                    + " tor.create_stamp,tor.pickup_assign_status,"
                    + " tor.create_stamp start_create_stamp "
                    + fromSql
                    + " where tor.insurance_id is null and tor. STATUS != '手动删除' AND tor.order_type != 'cargoReturnOrder'"
                    + " and  ifnull((SELECT l. NAME FROM location l WHERE l. CODE = tor.route_from), '') like '%"
                    + routeFrom
                    + "%' and ifnull((SELECT	l. NAME	FROM location l WHERE l. CODE = tor.route_to), '') like '%"
                    + routeTo
                    + "%' and ifnull(tor.order_no,'') like '%"
                    + orderNo
                    + "%' and ifnull(c.abbr,'') like '%"
                    + customer
                    + "%' and tor.planning_time between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "'  and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + "  and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
                    + " order by tor.planning_time asc" + sLimit;
        } 
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> transferOrders = Db.find(sql);

        transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);
        renderJson(transferOrderListMap);
    }

    // billing order 列表
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_IO_LIST})
    public void list() {
    	String orderNo = getPara("orderNo");
    	String departNo = getPara("departNo");
    	String beginTime = getPara("beginTime");
    	String endTime = getPara("endTime");
    	String planningBeginTime = getPara("planningBeginTime");
    	String planningEndTime = getPara("planningEndTime");
    	String customer = getPara("customer");
    	
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal="";
        String sql = "";
        
        sqlTotal = "select count(distinct ior.id) total from insurance_order ior "
        		+ " left join transfer_order tor on tor.insurance_id = ior.id "
        		+ " left join office o on o.id = tor .office_id where  o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
        		+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
        sql = "select distinct ior.*, con.company_name, (select c.abbr from party p left join contact c on p.contact_id = c.id where p.id=tor.customer_id) as customer,(select group_concat(tor.order_no separator '\r\n') from transfer_order tor where tor.insurance_id = ior.id) transfer_order_no,"
        		+ " (SELECT group_concat(cast(tor.planning_time AS CHAR) SEPARATOR '\r\n')  FROM transfer_order tor WHERE tor.insurance_id = ior.id) planning_time,"
        		+ " (select group_concat(cast(deo.departure_time as char) separator '\r\n') from transfer_order tor left join depart_transfer dt on dt.order_id = tor.id left join depart_order deo on deo.id = dt.depart_id where	tor.insurance_id = ior.id ) departure_time,"
        		+ " (SELECT ROUND(SUM(insurance_amount),2) from insurance_fin_item ifi where ifi.insurance_order_id =ior.id and IFNULL(ifi.cost_source,'') != '对账调整金额') insurance_amount,"
        		+ " (SELECT ROUND(SUM(insurance_amount),2) from insurance_fin_item ifi where ifi.insurance_order_id =ior.id ) change_amount"
//        		+ " (SELECT group_concat( insfi.insurance_no SEPARATOR '<br>' ) FROM insurance_order ior"
//        		+ " left JOIN insurance_fin_item insfi on insfi.insurance_order_id=ior.id"
//        		+ " WHERE tor.insurance_id = ior.id) insurance_no"
        		+ " from insurance_order ior "
        		+ " left join transfer_order tor on tor.insurance_id = ior.id "
        		+ " left join office o on o.id = tor .office_id "
        		+ " left join party p on p.id = ior.insurance_id "
        		+ " left join contact con on con.id = p.contact_id"
        		+ " where  o.id in (select office_id from user_office where user_name='"+ currentUser.getPrincipal()+"') "
        		+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
        
        String orderBysql = " order by ior.create_stamp desc ";
        if(orderNo==null&&departNo==null&&beginTime==null&&endTime==null){
        	sql = sql+orderBysql;
        }else{
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			if (planningBeginTime == null || "".equals(planningBeginTime)) {
        		planningBeginTime = "1-1-1";
			}
			if (planningEndTime == null || "".equals(planningEndTime)) {
				planningEndTime = "9999-12-31";
			}
        	String condition=" and ifnull(ior.order_no,'') like '%"
		    				+departNo
		    				+"%' and ifnull(tor.order_no,'') like '%"
		    				+orderNo
		    				+"%' and (SELECT c.abbr FROM party p LEFT JOIN contact c ON p.contact_id = c.id WHERE p.id = tor.customer_id) like '%"
		    				+customer
		    				+"%' and ior.create_stamp between '"
		    				+beginTime
		    				+"' and '"+endTime+"'"
		    				+" and tor.planning_time between '"
		    				+ planningBeginTime
		    				+"' and '"+planningEndTime+"'";;
        	sqlTotal = sqlTotal + condition;
	        sql = sql + condition + orderBysql;
        }
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
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_IO_CREATE})
    public void createInsuranceOrder() {
        String list = this.getPara("localArr");
        setAttr("localArr", list);
        String[] transferOrderIds = list.split(",");

        TransferOrder transferOrderAttr = TransferOrder.dao.findById(transferOrderIds[0]);
        setAttr("transferOrderAttr", transferOrderAttr);
        Long customerId = transferOrderAttr.get("customer_id");
        if (customerId != null && !"".equals(customerId)) {
            Party costomerParty = Party.dao.findById(customerId);
            Contact customerContact = Contact.dao.findById(costomerParty.get("contact_id"));
            setAttr("customerContact", customerContact);
            setAttr("hid_customer_id", customerId);
        }

        logger.debug("localArr" + list);
        String order_no = null;
        setAttr("saveOK", false);
        String[] orderIds = list.split(",");
        for (int i = 0; i < orderIds.length; i++) {
            TransferOrder transferOrder = TransferOrder.dao.findById(orderIds[i]);
            transferOrder.set("pickup_seq", i + 1);
            transferOrder.update();
        }
        TransferOrder transferOrder = new TransferOrder();
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));

        setAttr("order_no", OrderNoGenerator.getNextOrderNo("BX"));

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);

        setAttr("status", "新建");
        setAttr("saveOK", false);
        List<Record> paymentItemList = Collections.EMPTY_LIST;
        paymentItemList = Db.find("select * from fin_item where type='应付'");
        setAttr("paymentItemList", paymentItemList);
            render("/yh/insuranceOrder/insuranceOrderEdit.html");
    }

    // 初始化货品数据
    public void getInitPickupOrderItems() {
        String order_id = getPara("orderid");// 运输单id
        String tr_item = getPara("tr_item");// 货品id
        String item_detail = getPara("item_detail");// 单品id
        String insuranceOrderId = getPara("insuranceOrderId");// 单品id

        String sLimit = "";
        String sqlTotal ="";
        String sql ="";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        if(insuranceOrderId!=null&&!"".equals(insuranceOrderId)){
            sqlTotal = "select count(1) total from insurance_fin_item where insurance_order_id = " + insuranceOrderId;
            sql = "select ifi.id ,tor.order_no,c.abbr customer,toi.amount,tor.remark,ifi.amount fin_amount,ifi.rate,ifi.insurance_no,"
                    + " round(ifi.amount * toi.amount,2) total_amount,ifi.insurance_amount,ifnull(toi.item_no, pd.item_no) item_no,"
            		+ " ifnull(toi.item_name, pd.item_name) item_name,ifnull(toi.volume, pd.volume) * toi.amount volume,"
            		+ " (select group_concat(cast(tom.create_stamp AS CHAR) SEPARATOR '\r\n')  from transfer_order_milestone tom where tom.order_id = tor.id and tom.status = '已发车') start_create_stamp,"
            		+ " ifnull((select name from location l where l.code = tor.route_from),'') route_from,ifnull((select name from location l where l.code = tor.route_to),'') route_to "
                    + " from insurance_fin_item  ifi "
                    + " left join transfer_order_item toi on toi.id = ifi.transfer_order_item_id"
            		+ " left join transfer_order tor on tor.id = toi.order_id"
            		+ " left join party p on p.id = tor.customer_id"
            		+ " left join contact c on c.id = p.contact_id"
            		+ " left join product pd ON pd.id = toi.product_id"
                    + " where ifi.insurance_order_id = '" + insuranceOrderId + "'"
                    + " order by ifi.create_stamp desc " + sLimit;
        }else{
        	sqlTotal = "select count(1) total from transfer_order_item toi where  toi.order_id in ("+order_id+")";
            sql = "select toi.id,tor.order_no,c.abbr customer, toi.amount, tor.remark, pd.insurance_amount fin_amount, pit.insurance_rate rate,round(pd.insurance_amount * toi.amount, 2) total_amount,"
            		+ " round(pd.insurance_amount * toi.amount * pit.insurance_rate, 2) insurance_amount, ifnull(toi.item_no, pd.item_no) item_no,"
            		+ " ifnull(toi.item_name, pd.item_name) item_name,ifnull(toi.volume, pd.volume) * toi.amount volume,"
            		+ " (select group_concat(cast(tom.create_stamp AS CHAR) SEPARATOR '\r\n')  from transfer_order_milestone tom where tom.order_id = tor.id and tom.status = '已发车') start_create_stamp,"
            		+ " ifnull((select name from location l where l.code = tor.route_from),'') route_from,ifnull((select name from location l where l.code = tor.route_to),'') route_to "
            		+ " from transfer_order_item toi "
            		+ " left join transfer_order tor on tor.id = toi.order_id"
            		+ " left join party p on p.id = tor.customer_id"
            		+ " left join contact c on c.id = p.contact_id"
            		+ " left join product pd ON pd.id = toi.product_id"
            		+ " left join party_insurance_item pit on pit.customer_id = tor.customer_id"
            		+ " where toi.order_id in ("+order_id+")"+ sLimit;
        }
        logger.debug("sql :" + sqlTotal);
        
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> departOrderitem = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", departOrderitem);
        renderJson(Map);
    }  
    
    public void updateInsuranceOrderFinItem(){
    	String itemId = getPara("itemId");
    	String name = getPara("name");
    	String value = getPara("value");
    	String insuranceAmount = getPara("insuranceAmount");
    	InsuranceFinItem insuranceFinItem = InsuranceFinItem.dao.findById(itemId);
    	if(insuranceFinItem != null && !"".equals(value)){
    		if(insuranceAmount != null && !"".equals(insuranceAmount)){
    			insuranceFinItem.set("insurance_amount", insuranceAmount);
    		}
    		insuranceFinItem.set(name, value).update();
    	}
    	renderJson("{\"success\":true}");
    }
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_IO_CREATE, PermissionConstant.PERMSSION_IO_UPDATE}, logical=Logical.OR)
    @Before(Tx.class)
    public void save(){
    	InsuranceOrder insuranceOrder = null;
    	String insuranceOrderId = getPara("insuranceId");
    	String officeSelect = getPara("officeSelect");
    	String insuranceSelect = getPara("insuranceSelect");
    	String insurance_no = getPara("insurance_no");
    	if(insuranceOrderId != null && !"".equals(insuranceOrderId)){
    		insuranceOrder = InsuranceOrder.dao.findById(insuranceOrderId);
    		if(officeSelect != null && !"".equals(officeSelect)){
    			insuranceOrder.set("office_id", officeSelect);    			
    		}
    		if(insuranceSelect != null && !"".equals(insuranceSelect)){
    			insuranceOrder.set("insurance_id", insuranceSelect);    			
    		}
    		if(insurance_no != null && !"".equals(insurance_no)){
    			insuranceOrder.set("insurance_no", insurance_no);    			
    		}
    		insuranceOrder.set("remark", getPara("remark")).update();
    	}else{
    		Party insurance = null;
    		insuranceOrder = new InsuranceOrder();
    		insuranceOrder.set("order_no", getPara("order_no"));
    		insuranceOrder.set("create_stamp", new Date());
    		insuranceOrder.set("status", getPara("status"));
    		insuranceOrder.set("create_by", getPara("create_by"));
    		insuranceOrder.set("remark", getPara("remark"));
    		if(officeSelect != null && !"".equals(officeSelect)){
    			insuranceOrder.set("office_id", officeSelect);  
    		}
    		//保险公司
    		if(insuranceSelect != null && !"".equals(insuranceSelect)){
    			insuranceOrder.set("insurance_id", insuranceSelect);  
    		}
			insuranceOrder.set("audit_status", "新建");
			insuranceOrder.set("sign_status", "未回单");
			insuranceOrder.set("insurance_no", insurance_no);
    		insuranceOrder.save();
    		
    		String orderId = getPara("orderid");
    		String[] orderIds = orderId.split(",");
    		FinItem finItem = FinItem.dao.findFirst("select id from fin_item where type = ? and name = ?", "应付", "保险费");
    		for(int i = 0; i < orderIds.length; i++){
    			TransferOrder transferOrder = TransferOrder.dao.findById(orderIds[i]);
    			transferOrder.set("insurance_id", insuranceOrder.get("id"));
    			transferOrder.update();
    			
    			//保险公司费率
    			if(insuranceSelect != null && !"".equals(insuranceSelect)){
    				insurance = Party.dao.findFirst("select pit.insurance_rate from party p "
    						+ " left join contact c on c.id = p.contact_id "
    						+ " left join party_insurance_item pit on pit.party_id = p.id"
    						+ " where (pit.is_stop != 1 or pit.is_stop is null)"
    						+ " and current_date > pit.beginTime"
    						+ " and current_date < pit.endTime"
    						+ " and p.party_type = '"+ Party.PARTY_TYPE_INSURANCE_PARTY + "'"
    						+ " and pit.customer_id = '" + transferOrder.get("customer_id") + "'"
    						+ " and p.id = '" + insuranceSelect + "'");
    			}
    			//保险单从表--按单据货品买保险
    			Party party = Party.dao.findById(transferOrder.get("customer_id"));
    			List<TransferOrderItem> itemList = TransferOrderItem.dao.find("select id,product_id,amount from transfer_order_item where order_id = ?",orderIds[i]);
    			for (TransferOrderItem transferOrderItem : itemList) {
    				InsuranceFinItem insuranceFinItem = new InsuranceFinItem();
    				if(transferOrderItem!= null){
    					Product product = Product.dao.findById(transferOrderItem.get("product_id"));
    					if(product ==null){
    					    double prodoctInsuranceAmount = 0;
                            insuranceFinItem.set("amount", prodoctInsuranceAmount);
                            if(party!= null){
                                insuranceFinItem.set("income_rate", party.getDouble("insurance_rates"));
                                if(insurance.getDouble("insurance_rate") != null){
                                    double insuranceRates = insurance.getDouble("insurance_rate");
                                    double productAmount = transferOrderItem.getDouble("amount");
                                    BigDecimal b = new BigDecimal(prodoctInsuranceAmount * productAmount * insuranceRates);
                                    double InsuranceInsuranceAmount = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                    insuranceFinItem.set("rate", insuranceRates).set("insurance_amount", InsuranceInsuranceAmount);
                                }
                            } 
    					}else{
                            if(product.getDouble("insurance_amount")!= null){
                                double prodoctInsuranceAmount = product.getDouble("insurance_amount");
                                insuranceFinItem.set("amount", prodoctInsuranceAmount);
                                if(party!= null){
                                    insuranceFinItem.set("income_rate", party.getDouble("insurance_rates"));
                                    if(insurance.getDouble("insurance_rate") != null){
                                        double insuranceRates = insurance.getDouble("insurance_rate");
                                        double productAmount = transferOrderItem.getDouble("amount");
                                        BigDecimal b = new BigDecimal(prodoctInsuranceAmount * productAmount * insuranceRates);
                                        double InsuranceInsuranceAmount = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                        insuranceFinItem.set("rate", insuranceRates).set("insurance_amount", InsuranceInsuranceAmount);
                                    }
                                }
                            }
    					}
    				}
    				insuranceFinItem.set("transfer_order_item_id", transferOrderItem.get("id"))
        			.set("insurance_order_id", insuranceOrder.get("id"))
        			.set("fin_item_id", finItem.get("id"))
        			.save();
				}
    		}
    	}
    	
    	renderJson(insuranceOrder);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_IO_UPDATE})
    public void edit(){
    	String id = getPara("id");
    	List paymentItemList = new ArrayList();
    	InsuranceOrder insuranceOrder = null;
    	if(id != null && !"".equals(id)){
    		insuranceOrder = InsuranceOrder.dao.findById(id);
    		setAttr("insurance_no", insuranceOrder.getStr("insurance_no"));
    	}
    	setAttr("insuranceOrder", insuranceOrder);
    	setAttr("paymentItemList", paymentItemList);
    	UserLogin userLogin = UserLogin.dao.findById(insuranceOrder.get("create_by"));
        setAttr("userLogin2", userLogin);String orderId = "";
        List<TransferOrder> transferOrders = TransferOrder.dao.find("select * from transfer_order where insurance_id = ?", insuranceOrder.get("id"));
        for (TransferOrder transferOrder : transferOrders) {
            orderId += transferOrder.get("id") + ",";
        }
        orderId = orderId.substring(0, orderId.length() - 1);
        setAttr("localArr", orderId);
        String[] transferOrderIds = orderId.split(",");

        TransferOrder transferOrderAttr = TransferOrder.dao.findById(transferOrderIds[0]);
        setAttr("transferOrderAttr", transferOrderAttr);
        Long customerId = transferOrderAttr.get("customer_id");
        if (customerId != null && !"".equals(customerId)) {
            Party costomerParty = Party.dao.findById(customerId);
            Contact customerContact = Contact.dao.findById(costomerParty.get("contact_id"));
            setAttr("customerContact", customerContact);
            setAttr("hid_customer_id", customerId);
        }
        
        render("/yh/insuranceOrder/insuranceOrderEdit.html");
    }
    
    // 应付
    public void accountPayable(){
    	Map orderMap = new HashMap();
    	String insuranceOrderId = getPara("insuranceOrderId");
    	if(insuranceOrderId != null && !"".equals(insuranceOrderId)){	    		
	        String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	        // 获取总条数
	        /*String totalWhere = "";
	        String sql = "select 1 total from insurance_fin_item";
	        Record rec = Db.findFirst(sql + totalWhere);
	        logger.debug("total records:" + rec.getLong("total"));*/

	        // 获取当前页的数据
	        List<Record> orders = Db.find("select round(sum(insurance_amount),2) sum_amount from insurance_fin_item where insurance_order_id = "+insuranceOrderId);
	
	        orderMap.put("sEcho", pageIndex);
	        orderMap.put("iTotalRecords", 1);
	        orderMap.put("iTotalDisplayRecords", 1);
	        orderMap.put("aaData", orders);
    	}
        renderJson(orderMap);
    }
    
    // 应收
    public void incomePayable(){
    	Map orderMap = new HashMap();
    	String insuranceOrderId = getPara("insuranceOrderId");
    	if(insuranceOrderId == null || "".equals(insuranceOrderId)){	
    		insuranceOrderId = "-1";
    	}
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}

        String sqlTotal = "select count(1) total from transfer_order tor where tor.insurance_id = "+insuranceOrderId;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		String sql = "select c.abbr cname,tor.id order_id,tor.order_no transfer_order_no,round(sum(ifi.amount*ifi.rate),2) sum_amount,ifi.income_rate,"
						+ " sum(ifi.amount * toi.amount) sum_insurance,round(sum(ifi.amount  * toi.amount) * income_rate,2) income_insurance_amount from transfer_order tor"
						+ " left join insurance_order ior on ior.id = tor.insurance_id"
						+ " left join transfer_order_item toi on toi.order_id = tor.id"
						+ " left join insurance_fin_item ifi on ifi.transfer_order_item_id = toi.id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " where tor.insurance_id = "+insuranceOrderId+" group by tor.id";
		
		List<Record> orders = Db.find(sql);
		
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);
    	renderJson(orderMap);
    }
    
    // 应收条目
    public void incomeFinItem(){
    	String orderId = getPara("orderId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if("income_rate".equals(name) && "".equals(value)){
    		value = "0";
    	}
		List<TransferOrderItem> transferOrderItems = TransferOrderItem.dao.find("select * from transfer_order_item toi where toi.order_id =?", orderId);
		for(TransferOrderItem transferOrderItem : transferOrderItems){
			InsuranceFinItem insuranceFinItem = InsuranceFinItem.dao.findFirst("select * from insurance_fin_item ifi where ifi.transfer_order_item_id=?", transferOrderItem.get("id"));
			insuranceFinItem.set(name, value);
			insuranceFinItem.update();
		}    
    	renderJson("{\"success\":true}");
    }
    
    //所有的保险公司
    public void findAllInsurance(){
    	List<Party> party = Party.dao.find("select p.id,c.company_name,c.abbr from party p left join contact c on c.id = p.contact_id where p.party_type = '"+ Party.PARTY_TYPE_INSURANCE_PARTY + "'");
		renderJson(party);
    }
    
    //从新计算保险费用
    public void resetInsurance(){
    	String insuranceOrderId = getPara("insuranceId");
    	String insuranceSelect = getPara("insuranceSelect");
    	String customerId = getPara("customer_id");
    	//保险公司费率
    	Party insurance = null;
		if(insuranceSelect != null && !"".equals(insuranceSelect)){
			insurance = Party.dao.findFirst("select pit.insurance_rate from party p "
				+ " left join contact c on c.id = p.contact_id "
				+ " left join party_insurance_item pit on pit.party_id = p.id"
				+ " where (pit.is_stop != 1 or pit.is_stop is null)"
				+ " and current_date > pit.beginTime"
				+ " and current_date < pit.endTime"
				+ " and p.party_type = '"+ Party.PARTY_TYPE_INSURANCE_PARTY + "'"
				+ " and pit.customer_id = '" + customerId + "'"
				+ " and p.id = '" + insuranceSelect + "'");
		}
		
		//保险单从表--按单据货品买保险
		List<InsuranceFinItem> itemList = InsuranceFinItem.dao.find("select * from insurance_fin_item where insurance_order_id = ?",insuranceOrderId);
		for (InsuranceFinItem insuranceFinItem : itemList) {
			TransferOrderItem transferOrderItem = TransferOrderItem.dao.findById(insuranceFinItem.get("transfer_order_item_id"));
			if(transferOrderItem.get("amount") != null && !"".equals(transferOrderItem.get("amount"))){
		    	if(insurance.get("insurance_rate") != null && !"".equals(insurance.get("insurance_rate"))){
					if(insuranceFinItem.get("amount") != null && !"".equals(insuranceFinItem.get("amount"))){
						double insuranceRates = insurance.getDouble("insurance_rate");//应付费率
						double productAmount = transferOrderItem.getDouble("amount");//货品数量
						double prodoctInsuranceAmount = insuranceFinItem.getDouble("amount");//货品保额
						BigDecimal b = new BigDecimal(prodoctInsuranceAmount * productAmount * insuranceRates);
				    	double InsuranceInsuranceAmount = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				    	insuranceFinItem.set("rate", insuranceRates).set("insurance_amount", InsuranceInsuranceAmount).update();
					}
				}
			}
		}
		renderJson("{\"success\":true}");
    }
    public void showCustomerAounmt(){
    	Map orderMap = new HashMap();
    	String customer=getPara("customer");
    	String planningEndTime = getPara("planningEndTime");
    	String planningBeginTime = getPara("planningBeginTime");
    	String condition="";
    	if (planningBeginTime == null || "".equals(planningBeginTime)) {
    		planningBeginTime = "1-1-1";
		}
		if (planningEndTime == null || "".equals(planningEndTime)) {
			planningEndTime = "9999-12-31";
		}
		if(customer!=null&&!"".equals(customer)){
			condition=" where customer like '%" +customer+ "%'";
		}
    	String sql = "SELECT ROUND(sum(sum_amount),2) sum_amount from (SELECT DISTINCT ior.create_stamp,"
    			+ " (SELECT c.abbr FROM party p LEFT JOIN contact c ON p.contact_id = c.id WHERE p.id = tor.customer_id) AS customer,"
    			+ " (SELECT group_concat(cast(tor.planning_time AS CHAR) SEPARATOR '\r\n')  FROM transfer_order tor WHERE tor.insurance_id = ior.id) planning_time,"
    			+ " (SELECT sum(ifi.insurance_amount) FROM insurance_fin_item ifi WHERE ifi.insurance_order_id = ior.id ) sum_amount FROM insurance_order ior"
    			+ " LEFT JOIN transfer_order tor ON tor.insurance_id = ior.id"
    			+ " LEFT JOIN office o ON o.id = tor.office_id"
    			+ " LEFT JOIN party p ON p.id = ior.insurance_id"
    			+ " LEFT JOIN contact con ON con.id = p.contact_id"
    			+ " WHERE o.id IN (SELECT office_id FROM user_office WHERE user_name = 'admin@eeda123.com' )"
    			+ " AND tor.customer_id IN ( SELECT customer_id FROM user_customer WHERE user_name = 'admin@eeda123.com')"
    			+ "	and tor.planning_time between '"+planningBeginTime+"' and '"+planningEndTime+"'"
    			+ ") a";
        List<Record> orders = Db.find(sql+condition);
   		orderMap.put("orders", orders);
       	renderJson(orderMap);
    }
    
}
