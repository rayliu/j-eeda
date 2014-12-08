package controllers.yh.insurance;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.Fin_item;
import models.InsuranceFinItem;
import models.InsuranceOrder;
import models.Party;
import models.TransferOrder;
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

import controllers.yh.util.OrderNoUtil;
import controllers.yh.util.PermissionConstant;

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
        if (orderNo == null &&  customer == null && routeFrom == null && routeTo == null
                && beginTime == null && endTime == null) {
            sqlTotal = "select count(1) total  from transfer_order tor " + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code where tor.status = '已发车'";
            sql = "select tor.id,tor.order_no,tor.operation_type,tor.cargo_nature,tor.order_type,"
            		+ "	(select name from location l where l.code = dor.route_from) route_from,(select name from location l where l.code = dor.route_to) route_to, "
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ul.c_name create_by,tor.customer_order_no,"
                    + " tor.create_stamp,tor.pickup_assign_status,"
                    + " dor.departure_time start_create_stamp "
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
            sqlTotal = "select count(1) total from transfer_order tor " 
            		+ " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " 
            		+ " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code "
                    + "  where tor.status = '已发车' and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
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
                    + "') ";
            sql = "select tor.id,tor.order_no,tor.operation_type,tor.cargo_nature,tor.order_type,"
                	+ "	(select name from location l where l.code = dor.route_from) route_from,(select name from location l where l.code = dor.route_to) route_to, "
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ul.c_name create_by,tor.customer_order_no,"
                    + " tor.create_stamp,tor.pickup_assign_status,"
                    + " dor.departure_time start_create_stamp "
                    + " from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id " + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join user_login ul on ul.id = tor.create_by "  
                    + " left join depart_transfer dt on dt.order_id = tor.id "              
                    + " left join depart_order dor on dor.id = dt.depart_id where tor.status = '已发车' and dor.combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"' and ifnull((select name from location l where l.code = dor.route_from), '') like '%"
                    + routeFrom
                    + "%' and ifnull((select name from location l where l.code = dor.route_to), '') like '%"
                    + routeTo
                    + "%' and ifnull(tor.order_no,'') like '%"
                    + orderNo
                    + "%' and ifnull(c.abbr,'') like '%"
                    + customer
                    + "%' and tor.create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' order by tor.CREATE_STAMP desc" + sLimit;
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }

            sqlTotal = "select count(1) total from transfer_order tor "
            		+ " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " 
                    + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " where tor.status = '已发车' and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%'"
                    + "and tor.order_no like '%"
                    + orderNo
                    + "%'  and ifnull(c.abbr,'') like '%"
                    + customer
                    + "%' and tor.create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' ";

            sql = "select tor.id,tor.order_no,tor.operation_type,tor.cargo_nature,tor.order_type,"
                	+ "	(select name from location l where l.code = dor.route_from) route_from,(select name from location l where l.code = dor.route_to) route_to, "
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ul.c_name create_by,tor.customer_order_no,"
                    + " tor.create_stamp,tor.pickup_assign_status,"
                    + " dor.departure_time start_create_stamp "
                    + " from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id " + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join user_login ul on ul.id = tor.create_by "  
                    + " left join depart_transfer dt on dt.order_id = tor.id "              
                    + " left join depart_order dor on dor.id = dt.depart_id where tor.status = '已发车' and dor.combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"' and ifnull((select name from location l where l.code = dor.route_from), '') like '%"
                    + routeFrom
                    + "%' and ifnull((select name from location l where l.code = dor.route_to), '') like '%"
                    + routeTo
                    + "%' and tor.order_no like '%"
                    + orderNo
                    + "%'  and ifnull(c.abbr,'') like '%"
                    + customer
                    + "%' and tor.create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "'  order by tor.create_stamp desc" + sLimit;

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
    	
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal="";
        String sql = "";
        
        sqlTotal = "select count(1) total from insurance_order ior ";
        sql = "select ior.*,(select group_concat(tor.order_no separator '\r\n') from transfer_order tor where tor.insurance_id = ior.id) transfer_order_no from insurance_order ior ";
        
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
        	String condition=" where ifnull(order_no,'') like '%"
		    				+departNo
		    				+"%' and ifnull((select group_concat(tor.order_no separator '\r\n') from transfer_order tor where tor.insurance_id = ior.id),'') like '%"
		    				+orderNo
		    				+"%' and create_stamp between '"
		    				+beginTime
		    				+"' and '"+endTime+"'";
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

        String sql = "select * from insurance_order order by id desc limit 0,1";
        setAttr("order_no", OrderNoUtil.getOrderNo(sql, "BX"));

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
        String order_id = getPara("localArr");// 运输单id
        String tr_item = getPara("tr_item");// 货品id
        String item_detail = getPara("item_detail");// 单品id
        String insuranceOrderId = getPara("insuranceOrderId");// 单品id

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(1) total from transfer_order_item tof" + " where tof.order_id in(" + order_id + ")";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "";
        if(insuranceOrderId != null && !"".equals(insuranceOrderId)){
	        sql = "select toi.id item_id,ifi.id fin_id,ifi.amount fin_amount,ifi.*,ifnull(toi.item_name, pd.item_name) item_name,ifnull(toi.item_no, pd.item_no) item_no,ifnull(toi.volume, pd.volume)*toi.amount volume, "
	                + " ifnull(case toi.weight when 0.0 then null else toi.weight end, pd.weight)*toi.amount weight"
	                + " ,c.abbr customer,tor.order_no,toi.amount,toi.remark,"
	                + " (select tom.create_stamp  from transfer_order_milestone tom where tom.order_id = tor.id and tom.status = '已发车') start_create_stamp,"
	                + "	(select name from location l where l.code = dor.route_from) route_from,(select name from location l where l.code = dor.route_to) route_to "
					+ "  from transfer_order_item toi "
	                + " left join insurance_fin_item ifi on toi.id = ifi.transfer_order_item_id "
	                + " left join fin_item fi on fi.id = ifi.fin_item_id"
	                + " left join transfer_order tor on tor.id = toi.order_id"
	                + " left join party p on p.id = tor.customer_id"
	                + " left join contact c on c.id = p.contact_id"
	                + " left join product pd on pd.id = toi.product_id"
	                + " left join depart_transfer dt on dt.order_id = tor.id"
	                + " left join depart_order dor on dor.id = dt.depart_id"
	                + " where toi.order_id in(" + order_id + ") and dor.combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"' and fi.name = '保险费' and fi.type = '应付' order by c.id" + sLimit;
        }else{
        	sql = "select toi.id item_id,ifi.id fin_id,ifi.amount fin_amount,ifi.*,ifnull(toi.item_name, pd.item_name) item_name,ifnull(toi.item_no, pd.item_no) item_no,ifnull(toi.volume, pd.volume)*toi.amount volume, "
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
        }
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
    	Fin_item finItem = Fin_item.dao.findFirst("select * from fin_item where type = ? and name = ?", "应付", "保险费");
    	Fin_item finItem2 = Fin_item.dao.findFirst("select * from fin_item where type = ? and name = ?", "应收", "保险费");
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
    		
    		if("amount".equals(name) && !"".equals(value)){
	    		Long transferItemId = insuranceFinItem.get("transfer_order_item_id");
	    		insuranceFinItem = InsuranceFinItem.dao.findFirst("select ifi.* from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.transfer_order_item_id = ? and type = ? and name = ?", transferItemId, "应收", "保险费");
	
	    		insuranceFinItem.set(name, value);
	    		insuranceFinItem.update();
    		}
    	}else{
    		insuranceFinItem = new InsuranceFinItem();
    		insuranceFinItem.set(name, value);
    		insuranceFinItem.set("insurance_amount", insuranceAmount);
    		insuranceFinItem.set("transfer_order_item_id", itemId);
    		insuranceFinItem.set("insurance_order_id", insuranceOrderId);
    		insuranceFinItem.set("fin_item_id", finItem.get("id"));
    		insuranceFinItem.save();
    		
    		insuranceFinItem = new InsuranceFinItem();
    		insuranceFinItem.set(name, value);
    		insuranceFinItem.set("transfer_order_item_id", itemId);
    		insuranceFinItem.set("insurance_order_id", insuranceOrderId);
    		insuranceFinItem.set("fin_item_id", finItem2.get("id"));
    		insuranceFinItem.save();
    	}
    	renderJson("{\"success\":true}");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_IO_CREATE, PermissionConstant.PERMSSION_IO_UPDATE}, logical=Logical.OR)
    public void save(){
    	InsuranceOrder insuranceOrder = null;
    	String insuranceOrderId = getPara("insuranceId");
    	String officeSelect = getPara("officeSelect");
    	if(insuranceOrderId != null && !"".equals(insuranceOrderId)){
    		insuranceOrder = InsuranceOrder.dao.findById(insuranceOrderId);
    		insuranceOrder.set("remark", getPara("remark"));
    		if(officeSelect != null && !"".equals(officeSelect)){
    			insuranceOrder.set("office_id", officeSelect);    			
    		}
    		insuranceOrder.update();
    	}else{
    		insuranceOrder = new InsuranceOrder();
    		insuranceOrder.set("order_no", getPara("order_no"));
    		insuranceOrder.set("create_stamp", new Date());
    		insuranceOrder.set("status", getPara("status"));
    		insuranceOrder.set("create_by", getPara("create_by"));
    		insuranceOrder.set("remark", getPara("remark"));
    		if(officeSelect != null && !"".equals(officeSelect)){
    			insuranceOrder.set("office_id", officeSelect);    			
    		}
    		insuranceOrder.save();
    		
    		String orderId = getPara("orderid");
    		String[] orderIds = orderId.split(",");
    		for(int i = 0; i < orderIds.length; i++){
    			TransferOrder transferOrder = TransferOrder.dao.findById(orderIds[i]);
    			transferOrder.set("insurance_id", insuranceOrder.get("id"));
    			transferOrder.set("status", "已投保");
    			transferOrder.update();
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
    		String sql = "select ior.id,(select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where fi.type = '应付' and fi.name = '保险费' and ior.id = ifi.insurance_order_id) sum_amount,(group_concat(distinct tor.order_no separator '\r\n')) as transfer_order_no ,c.abbr cname,(select income_rate from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where fi.type = '应收' and fi.name = '保险费' and ior.id = ifi.insurance_order_id order by fi.id limit 0,1) income_rate,"
    				+ "((select income_rate from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where fi.type = '应收' and fi.name = '保险费' and ior.id = ifi.insurance_order_id order by fi.id limit 0,1)*(select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where fi.type = '应付' and fi.name = '保险费' and ior.id = ifi.insurance_order_id)) income_insurance_amount from insurance_fin_item ifi "
					+ " left join insurance_order ior  on ior.id = ifi.insurance_order_id"
					+ " left join fin_item fi on fi.id = ifi.fin_item_id"
					+ " left join transfer_order_item toi on toi.id = ifi.transfer_order_item_id left join transfer_order tor on tor.id = toi.order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
					+ " where fi.type = '应收' and fi.name = '保险费' and ior.id = " + insuranceOrderId;
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
    	String insuranceOrderId = getPara("insuranceOrderId");
    	String name = getPara("name");
    	String value = getPara("value");
    	Fin_item finItem = Fin_item.dao.findFirst("select * from fin_item where type = ? and name = ?", "应收", "保险费");
    	if("income_rate".equals(name) && "".equals(value)){
    		value = "0";
    	}
		List<InsuranceFinItem> insuranceFinItems = InsuranceFinItem.dao.find("select ifi.* from insurance_order ior left join insurance_fin_item ifi on ior.id = ifi.insurance_order_id left join fin_item fi on fi.id = ifi.fin_item_id where fi.name = '保险费' and fi.type = '应收' and ior.id = "+insuranceOrderId);
		for(InsuranceFinItem insuranceFinItem : insuranceFinItems){
			insuranceFinItem.set("fin_item_id", finItem.get("id"));
			insuranceFinItem.set(name, value);
			insuranceFinItem.update();
		}    
    	renderJson("{\"success\":true}");
    }
}
