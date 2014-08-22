package controllers.yh.returnOrder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.Fin_item;
import models.Location;
import models.Party;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class ReturnOrderController extends Controller {
    private Logger logger = Logger.getLogger(ReturnOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
        if (LoginUserController.isAuthenticated(this))
            render("returnOrder/returnOrderList.html");
    }

    public static String createReturnOrderNo() {
        String orderNo = null;
        ReturnOrder order = ReturnOrder.dao.findFirst("select * from return_order order by order_no desc limit 0,1");
        if (order != null) {
            String num = order.get("order_no");
            String str = num.substring(2, num.length());
            System.out.println(str);
            Long oldTime = Long.parseLong(str);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            String time = format + "00001";
            Long newTime = Long.parseLong(time);
            if (oldTime >= newTime) {
                orderNo = String.valueOf((oldTime + 1));
            } else {
                orderNo = String.valueOf(newTime);
            }
            orderNo = "HD" + orderNo;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            orderNo = format + "00001";
            orderNo = "HD" + orderNo;
        }
        return orderNo;
    }

    public void list() {
        String order_no = getPara("order_no");
        String tr_order_no = getPara("tr_order_no");
        String de_order_no = getPara("de_order_no");
        String stator = getPara("stator");
        String status = getPara("status");
        String time_one = getPara("time_one");
        String time_two = getPara("time_two");

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        if (order_no == null && tr_order_no == null && de_order_no == null && stator == null && status == null
                && time_one == null && time_two == null) {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from return_order ro ";
            Record rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select distinct r_o.*, usl.user_name as creator_name, ifnull(tor.order_no, tor2.order_no) transfer_order_no, d_o.order_no as delivery_order_no, ifnull(c.contact_person,c2.contact_person) cname from return_order r_o " 
							+" left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id  "
							+" left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join delivery_order_item doi on doi.delivery_id = d_o.id "
							+" left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=r_o.creator order by r_o.create_date desc " + sLimit);

            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);

        } else {
            if (time_one == null || "".equals(time_one)) {
                time_one = "1-1-1";
            }
            if (time_two == null || "".equals(time_two)) {
                time_two = "9999-12-31";
            }

            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from return_order r_o " 
							+" left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
							+" left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join transfer_order tor2 on tor2.id = d_o.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
							+" left join user_login  usl on usl.id=r_o.creator where ifnull(r_o.order_no,'')  like'%"
		                    + order_no + "%' and  " + "ifnull(tor.order_no,tor2.order_no)  like'%" + tr_order_no + "%'  and  "
		                    + "ifnull(d_o.order_no,'')  like'%" + de_order_no + "%'  and "
		                    + "ifnull(r_o.transaction_status ,'')  like'%" + status + "%' and "
		                    + "ifnull(usl.user_name ,'')  like'%" + stator + "%'  and " + "r_o.create_date between '"
		                    + time_one + "' and '" + time_two + "'";
            Record rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select distinct r_o.*, usl.user_name as creator_name, ifnull(tor.order_no,tor2.order_no) transfer_order_no, d_o.order_no as delivery_order_no, ifnull(c.contact_person,c2.contact_person) cname from return_order r_o " 
							+" left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
							+" left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join transfer_order tor2 on tor2.id = d_o.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
							+" left join user_login  usl on usl.id=r_o.creator "
                            + "where ifnull(r_o.order_no,'')  like'%"
                            + order_no
                            + "%' and  "
                            + "ifnull(tor.order_no,tor2.order_no)  like'%"
                            + tr_order_no
                            + "%'  and  "
                            + "ifnull(d_o.order_no,'')  like'%"
                            + de_order_no
                            + "%'  and "
                            + "ifnull(r_o.transaction_status ,'')  like'%"
                            + status
                            + "%' and "
                            + "ifnull(usl.user_name ,'')  like'%"
                            + stator
                            + "%'  and "
                            + "r_o.create_date between '"
                            + time_one + "' and '" + time_two + "' order by r_o.create_date desc "+sLimit);

            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);
        }

        renderJson(orderMap);
    }

    // 点击查看
    public void edit() {
        ReturnOrder returnOrder = ReturnOrder.dao.findById(getPara("id"));
        TransferOrder transferOrder = null;
        Long deliveryId = returnOrder.get("delivery_order_id");
        Long transferOrderId = returnOrder.get("transfer_order_id");
        Long notify_party_id;
        String code = "";
        if (deliveryId == null) {
            transferOrder = TransferOrder.dao.findById(transferOrderId);
            notify_party_id = transferOrder.get("notify_party_id");
        }else{
        	DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(deliveryId);
        	// TODO 一张配送单对应多张运输单时回单怎样取出信息
        	List<DeliveryOrderItem> deliveryOrderItems = DeliveryOrderItem.dao.find("select * from delivery_order_item where delivery_id = ?", deliveryId);
        	for(DeliveryOrderItem deliveryOrderItem : deliveryOrderItems){
            	transferOrder = TransferOrder.dao.findById(deliveryOrderItem.get("transfer_order_id"));
        	}
            notify_party_id = deliveryOrder.get("notify_party_id");
        }
        setAttr("transferOrder", transferOrder);
        
        Long customer_id = transferOrder.get("customer_id");
        if (customer_id != null) {
            Party customer = Party.dao.findById(customer_id);
            Contact customerContact = Contact.dao.findById(customer.get("contact_id"));
            setAttr("customerContact", customerContact);
        }           
        if (notify_party_id != null) {
            Party notify = Party.dao.findById(notify_party_id);
            Contact contact = Contact.dao.findById(notify.get("contact_id"));
            setAttr("contact", contact);
            Contact locationCode = Contact.dao.findById(notify.get("contact_id"));
            code = locationCode.get("location");
        } 

        List<Location> provinces2 = Location.dao.find("select * from location where pcode ='1'");
        Location l2 = Location.dao
                .findFirst("SELECT * FROM location where code = (select pcode from location where CODE = '"
                        + code + "')");
        Location location = null;
        if (provinces2.contains(l2)) {
            location = Location.dao
                    .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                            + code + "'");
        } else {
            location = Location.dao
                    .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                            + code + "'");
        }
        setAttr("location", location);
        
        String routeFrom = transferOrder.get("route_from");
        Location locationFrom = null;
        if (routeFrom != null || !"".equals(routeFrom)) {
            List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
            Location l = Location.dao
                    .findFirst("select * from location where code = (select pcode from location where code = '"
                            + routeFrom + "')");
            if (provinces.contains(l)) {
                locationFrom = Location.dao
                        .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                + routeFrom + "'");
            } else {
                locationFrom = Location.dao
                        .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                + routeFrom + "'");
            }
            setAttr("locationFrom", locationFrom);
        }

        String routeTo = transferOrder.get("route_to");
        Location locationTo = null;
        if (routeTo != null || !"".equals(routeTo)) {
            List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
            Location l = Location.dao
                    .findFirst("select * from location where code = (select pcode from location where code = '"
                            + routeTo + "')");
            if (provinces.contains(l)) {
                locationTo = Location.dao
                        .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                + routeTo + "'");
            } else {
                locationTo = Location.dao
                        .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                + routeTo + "'");
            }
            setAttr("locationTo", locationTo);
        }

        setAttr("returnOrder", returnOrder);
        UserLogin userLogin = UserLogin.dao.findById(returnOrder.get("creator"));
        setAttr("userLogin", userLogin);
        UserLogin userLoginTo = UserLogin.dao.findById(transferOrder.get("create_by"));
        setAttr("userLoginTo", userLoginTo);
        if (LoginUserController.isAuthenticated(this))
            render("returnOrder/returnOrder.html");
    }

    public void save() {
        ReturnOrder returnOrder = ReturnOrder.dao.findById(getPara("id"));
        Long deliveryId = returnOrder.get("delivery_order_id");
        String routeTo = getPara("route_to");
        Long notifyPartyId;
        if(deliveryId == null){
        	// 直送
        	TransferOrder transferOrder = TransferOrder.dao.findById(returnOrder.get("transfer_order_id"));
        	if(!"".equals(routeTo) && routeTo != null){
        		transferOrder.set("route_to", routeTo);
        	}
        	transferOrder.update();
        	notifyPartyId = transferOrder.get("notify_party_id");
        	if(notifyPartyId != null){
        		updateContact(notifyPartyId);
        	}
        }else{
        	// 非直送
        	DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(deliveryId);
        	if(!"".equals(routeTo) && routeTo != null){
        		deliveryOrder.set("route_to", routeTo);
        	}
        	deliveryOrder.update();
        	notifyPartyId = deliveryOrder.get("notify_party_id");
        	if(notifyPartyId != null){
        		updateContact(notifyPartyId);
        	}
        }
        returnOrder.set("remark", getPara("remark"));
        returnOrder.update();
        if (LoginUserController.isAuthenticated(this))
            renderJson(returnOrder);

    }

    // 更新收货人信息
    private void updateContact(Long notifyPartyId) {
		Party party = Party.dao.findById(notifyPartyId);
		Contact contact = Contact.dao.findById(party.get("contact_id"));
		contact.set("company_name", getPara("company_name"));
		contact.set("address", getPara("address"));
		contact.set("contact_person", getPara("contact_person"));
		contact.set("phone", getPara("phone"));
		contact.update();
	}

	// 回单签收
    public void returnOrderReceipt() {
        String id = getPara();
        ReturnOrder returnOrder = ReturnOrder.dao.findById(id);
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        returnOrder.set("transaction_status", "已签收").set("receipt_date", sqlDate).update();
        Long deliveryId = returnOrder.get("delivery_order_id");
        if(deliveryId != null && !"".equals(deliveryId)){
        	DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(returnOrder.get("delivery_order_id"));
        	deliveryOrder.set("status", "已签收");
        	deliveryOrder.update();
        	
        	DeliveryOrderMilestone transferOrderMilestone = new DeliveryOrderMilestone();
        	transferOrderMilestone.set("status", "已签收");
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
            transferOrderMilestone.set("create_by", users.get(0).get("id"));
            transferOrderMilestone.set("location", "");
            utilDate = new java.util.Date();
            sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("delivery_id", deliveryOrder.get("id"));
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.save();
        }else{
        	TransferOrder transferOrder = TransferOrder.dao.findById(returnOrder.get("transfer_order_id"));
        	transferOrder.set("status", "已签收");
        	transferOrder.update();
        	
        	TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        	transferOrderMilestone.set("status", "已签收");
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
            transferOrderMilestone.set("create_by", users.get(0).get("id"));
            transferOrderMilestone.set("location", "");
            utilDate = new java.util.Date();
            sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("order_id", transferOrder.get("id"));
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.save();
        }
        renderJson("{\"success\":true}");
    }
    
    // 取消
    public void cancel() {
    	String id = getPara();
    	ReturnOrder re = ReturnOrder.dao.findById(id);
    	re.set("TRANSACTION_STATUS", "cancel").update();
    	renderJson("{\"success\":true}");
    }

    // 应收list
    public void accountReceivable() {
        String id = getPara();
        ReturnOrder returnOrder = ReturnOrder.dao.findById(id);

        String sLimit = "";
        if (id == null || id.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from transfer_order_fin_item where order_id ='"
                + returnOrder.get("transfer_order_id") + "' ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select d.*,f.name,f.remark,t.order_no as transferOrderNo from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join transfer_order t on t.id = d.order_id where d.order_id ='"
                        + returnOrder.get("transfer_order_id") + "' and f.type='应收'");

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        List<Record> list = Db.find("select * from fin_item");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get("name") == null) {
                Fin_item.dao.deleteById(list.get(i).get("id"));
            }
        }
        renderJson(orderMap);
    }
    
    public void transferOrderDetailList() {
        String deliveryOrderId = getPara("deliveryOrderId");
        String orderId = getPara("orderId");
        if (deliveryOrderId == null || "".equals(deliveryOrderId)) {
        	deliveryOrderId = "-1";
        }
        if (orderId == null || "".equals(orderId)) {
        	orderId = "-1";
        }
        logger.debug(deliveryOrderId);

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sql = "";
        String sqlTotal = "";
        if(deliveryOrderId != "-1"){
	        sqlTotal = "select count(1) total from transfer_order_item_detail where delivery_id =" + deliveryOrderId;
	
	        sql = "select d.*,c.contact_person,c.phone,c.address from transfer_order_item_detail d"
					+ " left join party p on d.notify_party_id = p.id"
					+ " left join contact c on p.contact_id = c.id"
					+ " where d.delivery_id ="+deliveryOrderId + sLimit;	
        }else{
        	sqlTotal = "select count(1) total from transfer_order_item_detail where order_id="+orderId;
	
	        sql = "select d.*,c.contact_person,c.phone,c.address from transfer_order_item_detail d"
					+ " left join party p on d.notify_party_id = p.id"
					+ " left join contact c on p.contact_id = c.id"
	                + " where order_id = "+orderId + sLimit;	
        }

        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> transferOrders = Db.find(sql);
        Map transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);

        renderJson(transferOrderListMap);
    }
}
