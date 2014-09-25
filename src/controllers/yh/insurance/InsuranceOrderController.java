package controllers.yh.insurance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.InsuranceFinItem;
import models.InsuranceOrder;
import models.Party;
import models.TransferOrder;
import models.UserLogin;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class InsuranceOrderController extends Controller {
    private Logger logger = Logger.getLogger(InsuranceOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
    	if(LoginUserController.isAuthenticated(this))
    	    render("/yh/insuranceOrder/insuranceOrderList.html");
    }

    public void add() {
    	if(LoginUserController.isAuthenticated(this))
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
    	if(LoginUserController.isAuthenticated(this))
    		render("/yh/insuranceOrder/insuranceOrderEdit.html");
    }

    // 保险单选取运输单
    public void createList() {
        Map transferOrderListMap = null;
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String address = getPara("address");
        String customer = getPara("customer");
        String routeFrom = getPara("routeFrom");
        String routeTo = getPara("routeTo");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String orderType = getPara("orderType") == null ? "" : getPara("orderType");

        if (!"".equals(orderType)) {
            if ("销售订单".contains(orderType)) {
                orderType = "salesOrder";
            } else if ("补货订单".contains(orderType)) {
                orderType = "replenishmentOrder";
            } else if ("调拨订单".contains(orderType)) {
                orderType = "arrangementOrder";
            } else if ("退货订单".contains(orderType)) {
                orderType = "cargoReturnOrder";
            } else if ("质量退单".contains(orderType)) {
                orderType = "damageReturnOrder";
            } else if ("出库运输单".contains(orderType)) {
                orderType = "gateOutTransferOrder";
            }
        }

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        String sql = "";
        String sqlTotal = "";
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        if (orderNo == null && status == null && address == null && customer == null && routeFrom == null && routeTo == null
                && beginTime == null && endTime == null) {
            sqlTotal = "select count(1) total  from transfer_order tor " + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code where tor.status = '已发车'";
            sql = "select tor.id,tor.order_no,tor.operation_type,tor.cargo_nature,tor.order_type,"
            		+ "	(select name from location l where l.code = dor.route_from) route_from,(select name from location l where l.code = dor.route_to) route_to, "
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ul.user_name create_by,tor.customer_order_no,"
                    + " tor.create_stamp,tor.pickup_assign_status,"
                    + " (select tom.create_stamp  from transfer_order_milestone tom where tom.order_id = tor.id and tom.status = '已发车' limit 0,1) start_create_stamp "
                    + " from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id " + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join user_login ul on ul.id = tor.create_by "  
                    + " left join depart_transfer dt on dt.order_id = tor.id "              
                    + " left join depart_order dor on dor.id = dt.depart_id where tor.status = '已发车' and dor.combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"' order by tor.create_stamp desc" + sLimit;
        } else if ("".equals(routeFrom) && "".equals(routeTo)) {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }
            sqlTotal = "select count(1) total from transfer_order tor " + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code "
                    + "  where tor.status = '已发车' and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%'"
                    + "and tor.order_no like '%"
                    + orderNo
                    + "%' and tor.status like '%"
                    + status
                    + "%' and tor.address like '%"
                    + address
                    + "%' and ifnull(c.abbr,'') like '%"
                    + customer
                    + "%' and create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' and tor.order_type like '%"
                    + orderType + "%'";
            sql = "select tor.id,tor.order_no,tor.operation_type,tor.cargo_nature,tor.order_type,"
                	+ "	(select name from location l where l.code = dor.route_from) route_from,(select name from location l where l.code = dor.route_to) route_to, "
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ul.user_name create_by,tor.customer_order_no,"
                    + " tor.create_stamp,tor.pickup_assign_status,"
                    + " (select tom.create_stamp  from transfer_order_milestone tom where tom.order_id = tor.id and tom.status = '已发车' limit 0,1) start_create_stamp "
                    + " from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id " + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join user_login ul on ul.id = tor.create_by "  
                    + " left join depart_transfer dt on dt.order_id = tor.id "              
                    + " left join depart_order dor on dor.id = dt.depart_id where tor.status = '已发车' and dor.combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"' and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%' and tor.order_no like '%"
                    + orderNo
                    + "%' and tor.status like '%"
                    + status
                    + "%' and tor.address like '%"
                    + address
                    + "%' and ifnull(c.abbr,'') like '%"
                    + customer
                    + "%' and create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' and tor.order_type like '%"
                    + orderType + "%' order by tor.CREATE_STAMP desc" + sLimit;
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }

            sqlTotal = "select count(1) total from transfer_order tor " + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " where tor.status = '已发车' and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%'"
                    + "and tor.order_no like '%"
                    + orderNo
                    + "%' and tor.status like '%"
                    + status
                    + "%' and tor.address like '%"
                    + address
                    + "%' and ifnull(c.abbr,'') like '%"
                    + customer
                    + "%' and create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' and tor.order_type like '%"
                    + orderType + "%'";

            sql = "select tor.id,tor.order_no,tor.operation_type,tor.cargo_nature,tor.order_type,"
                	+ "	(select name from location l where l.code = dor.route_from) route_from,(select name from location l where l.code = dor.route_to) route_to, "
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ul.user_name create_by,tor.customer_order_no,"
                    + " tor.create_stamp,tor.pickup_assign_status,"
                    + " (select tom.create_stamp  from transfer_order_milestone tom where tom.order_id = tor.id and tom.status = '已发车' limit 0,1) start_create_stamp "
                    + " from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id " + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join user_login ul on ul.id = tor.create_by "  
                    + " left join depart_transfer dt on dt.order_id = tor.id "              
                    + " left join depart_order dor on dor.id = dt.depart_id where tor.status = '已发车' and dor.combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"' and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%' and tor.order_no like '%"
                    + orderNo
                    + "%' and tor.status like '%"
                    + status
                    + "%' and tor.address like '%"
                    + address
                    + "%' and ifnull(c.abbr,'') like '%"
                    + customer
                    + "%' and create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' and tor.order_type like '%"
                    + orderType + "%' order by tor.create_stamp desc" + sLimit;

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
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from insurance_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select ior.*,'YS2014092200001' transfer_order_no from insurance_order ior order by ior.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }

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

        InsuranceOrder order = InsuranceOrder.dao.findFirst("select * from insurance_order order by order_no desc limit 0,1");
        if (order != null) {
            String num = order.get("order_no");
            String str = num.substring(2, num.length());
            Long oldTime = Long.parseLong(str);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            String time = format + "00001";
            Long newTime = Long.parseLong(time);
            if (oldTime >= newTime) {
                order_no = String.valueOf((oldTime + 1));
            } else {
                order_no = String.valueOf(newTime);
            }
            setAttr("order_no", "BX" + order_no);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            order_no = format + "00001";
            setAttr("order_no", "BX" + order_no);
        }

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);

        setAttr("status", "新建");
        setAttr("saveOK", false);
        List<Record> paymentItemList = Collections.EMPTY_LIST;
        paymentItemList = Db.find("select * from fin_item where type='应付'");
        setAttr("paymentItemList", paymentItemList);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/insuranceOrder/insuranceOrderEdit.html");
    }

    // 初始化货品数据
    public void getInitPickupOrderItems() {
        String order_id = getPara("localArr");// 运输单id
        String tr_item = getPara("tr_item");// 货品id
        String item_detail = getPara("item_detail");// 单品id

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(1) total from transfer_order_item tof" + " where tof.order_id in(" + order_id + ")";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select toi.id item_id,ifi.id fin_id,ifi.amount fin_amount,ifi.*,ifnull(toi.item_name, pd.item_name) item_name,ifnull(toi.item_no, pd.item_no) item_no,ifnull(toi.volume, pd.volume)*toi.amount volume, "
                + " ifnull(case toi.weight when 0.0 then null else toi.weight end, pd.weight)*toi.amount weight"
                + " ,c.abbr customer,tor.order_no,toi.amount,toi.remark,"
                + " (select tom.create_stamp  from transfer_order_milestone tom where tom.order_id = tor.id and tom.status = '已发车') start_create_stamp,"
                + "	(select name from location l where l.code = dor.route_from) route_from,(select name from location l where l.code = dor.route_to) route_to "
				+ "  from transfer_order_item toi "
                + " left join insurance_fin_item ifi on toi.id = ifi.transfer_order_item_id "
                + " left join transfer_order tor on tor.id = toi.order_id"
                + " left join party p on p.id = tor.customer_id"
                + " left join contact c on c.id = p.contact_id"
                + " left join product pd on pd.id = toi.product_id"
                + " left join depart_transfer dt on dt.order_id = tor.id"
                + " left join depart_order dor on dor.id = dt.depart_id"
                + " where toi.order_id in(" + order_id + ") and dor.combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"' order by c.id" + sLimit;
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
    	String insuranceOrderId = getPara("insuranceOrderId");
    	String name = getPara("name");
    	String value = getPara("value");
    	String insuranceAmount = getPara("insuranceAmount");
    	InsuranceFinItem insuranceFinItem = InsuranceFinItem.dao.findFirst("select * from insurance_fin_item where transfer_order_item_id = ?", itemId);
    	if("amount".equals(name) && "".equals(value)){
    		value = "0";
    	}
    	if("".equals(insuranceAmount) || insuranceAmount == null){
    		insuranceAmount = "0";
    	}
    	if(insuranceFinItem != null){
    		insuranceFinItem.set(name, value);
    		insuranceFinItem.set("insurance_amount", insuranceAmount);
    		insuranceFinItem.update();
    	}else{
    		insuranceFinItem = new InsuranceFinItem();
    		insuranceFinItem.set(name, value);
    		insuranceFinItem.set("insurance_amount", insuranceAmount);
    		insuranceFinItem.set("transfer_order_item_id", itemId);
    		insuranceFinItem.set("insurance_order_id", insuranceOrderId);
    		insuranceFinItem.save();
    	}
    	renderJson("{\"success\":true}");
    }
    
    public void save(){
    	InsuranceOrder insuranceOrder = null;
    	String insuranceOrderId = getPara("insuranceOrderId");
    	if(insuranceOrderId != null && !"".equals(insuranceOrderId)){
    		insuranceOrder = InsuranceOrder.dao.findById(insuranceOrderId);
    		insuranceOrder.set("remark", getPara("remark"));
    		insuranceOrder.update();
    	}else{
    		insuranceOrder = new InsuranceOrder();
    		insuranceOrder.set("order_no", getPara("order_no"));
    		insuranceOrder.set("create_stamp", new Date());
    		insuranceOrder.set("status", getPara("status"));
    		insuranceOrder.set("create_by", getPara("create_by"));
    		insuranceOrder.set("remark", getPara("remark"));
    		insuranceOrder.save();
    		
    		String orderId = getPara("orderid");
    		String[] orderIds = orderId.split(",");
    		for(int i = 0; i < orderIds.length; i++){
    			TransferOrder transferOrder = TransferOrder.dao.findById(orderIds[i]);
    			transferOrder.set("insurance_id", insuranceOrder.get("id"));
    			transferOrder.update();
    		}
    	}
    	renderJson(insuranceOrder);
    }
    
    public void edit(){
    	String id = getPara("id");
    	List paymentItemList = new ArrayList();
    	InsuranceOrder insuranceOrder = null;
    	if(id != null && !"".equals(id)){
    		insuranceOrder = InsuranceOrder.dao.findById(id);
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
        }
    	if(LoginUserController.isAuthenticated(this))
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
	        List<Record> orders = Db.find("select sum(insurance_amount) sum_amount from insurance_fin_item where insurance_order_id = "+insuranceOrderId);
	
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
    	if(insuranceOrderId != null && !"".equals(insuranceOrderId)){	    		
    		String sLimit = "";
    		String pageIndex = getPara("sEcho");
    		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    		}
    		
    		// 获取当前页的数据
    		String sql = "select ior.id,sum(ifi.amount) sum_amount,(group_concat(distinct tor.order_no separator '\r\n')) as transfer_order_no"
					+ " ,c.abbr cname,ifi.income_rate income_rate,(income_rate*sum(ifi.amount)) income_insurance_amount from insurance_order ior"
					+ " left join insurance_fin_item ifi on ior.id = ifi.insurance_order_id"
					+ " left join transfer_order_item toi on toi.id = ifi.transfer_order_item_id"
					+ " left join transfer_order tor on tor.id = toi.order_id"
					+ " left join party p on p.id = tor.customer_id"
					+ " left join contact c on c.id = p.contact_id"
					+ " where ior.id = " + insuranceOrderId;
    		List<Record> orders = Db.find(sql);
    		
    		orderMap.put("sEcho", pageIndex);
    		orderMap.put("iTotalRecords", 1);
    		orderMap.put("iTotalDisplayRecords", 1);
    		orderMap.put("aaData", orders);
    	}
    	renderJson(orderMap);
    }
    
    // 应收条目
    public void incomeFinItem(){
    	String id = getPara("id");
    	String name = getPara("name");
    	String value = getPara("value");
    	if(id != null && !"".equals(id)){
    		InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(id);
	    	List<InsuranceFinItem> insuranceFinItems = InsuranceFinItem.dao.find("select * from insurance_fin_item where insurance_order_id = ?", insuranceOrder.get("id"));
	    	if("income_rate".equals(name) && "".equals(value)){
	    		value = "0";
	    	}
	    	for(InsuranceFinItem insuranceFinItem : insuranceFinItems){
	    		insuranceFinItem.set(name, value);
	    		insuranceFinItem.update();
	    	}
    	}
    	renderJson("{\"success\":true}");
    }
}
