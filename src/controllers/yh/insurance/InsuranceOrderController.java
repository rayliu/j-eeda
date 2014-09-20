package controllers.yh.insurance;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    + " left join location l2 on tor.route_to = l2.code"
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'";
            sql = "select tor.id,tor.order_no,tor.operation_type,tor.cargo_nature,tor.order_type,"
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ul.user_name create_by,tor.customer_order_no,"
                    + " l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id " + " left join contact c2 on p2.contact_id = c2.id "
                    + "  left join user_login ul on ul.id = tor.create_by "
                    + " left join location l1 on tor.route_from = l1.code " + " left join location l2 on tor.route_to = l2.code"
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'" + " order by tor.create_stamp desc" + sLimit;
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
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL
                    + "'"
                    + " "
                    + " and ifnull(l1.name, '') like '%"
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
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ul.user_name create_by,tor.customer_order_no,"
                    + " l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id " + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join user_login ul on ul.id = tor.create_by "
                    + " left join location l1 on tor.route_from = l1.code " + " left join location l2 on tor.route_to = l2.code  "
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL
                    + "'"
                    + " and ifnull(l1.name, '') like '%"
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
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL
                    + "'"
                    + " "
                    + " and ifnull(l1.name, '') like '%"
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
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount) from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,c2.abbr spname,ul.user_name create_by,tor.customer_order_no,"
                    + " (select name from location where code = tor.route_from) route_from,(select name from location where code = tor.route_to) route_to,tor.create_stamp,tor.pickup_assign_status from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id " + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join user_login ul on ul.id = tor.create_by "
                    + " left join location l1 on tor.route_from = l1.code " + " left join location l2 on tor.route_to = l2.code  "
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL
                    + "'"
                    + " and ifnull(l1.name, '') like '%"
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

        String sql = "select * from insurance_order create_stamp desc " + sLimit;

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

        if (transferOrderIds.length == 1) {
            TransferOrder transferOrderAttr = TransferOrder.dao.findById(transferOrderIds[0]);
            setAttr("transferOrderAttr", transferOrderAttr);
            Long spId = transferOrderAttr.get("sp_id");
            if (spId != null && !"".equals(spId)) {
                Party spParty = Party.dao.findById(spId);
                setAttr("spParty", spParty);
                Contact spContact = Contact.dao.findById(spParty.get("contact_id"));
                setAttr("spContact", spContact);
            }
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
}
