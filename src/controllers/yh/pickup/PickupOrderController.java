package controllers.yh.pickup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartTransferOrder;
import models.InventoryItem;
import models.Party;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.profile.Carinfo;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class PickupOrderController extends Controller {
    private Logger logger = Logger.getLogger(PickupOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    private boolean isAuthenticated() {
        if (!currentUser.isAuthenticated()) {
            redirect("/yh/login");
            return false;
        }
        setAttr("userId", currentUser.getPrincipal());
        return true;
    }

    public void index() {
        if (!isAuthenticated())
            return;
        render("/yh/pickup/pickupOrderList.html");
    }

    public void add() {
        if (LoginUserController.isAuthenticated(this))
            render("/yh/pickup/pickupOrderSearchTransfer.html");
    }

    public void createPickupOrder() {
        String list = this.getPara("localArr");
        setAttr("localArr", list);
        String[] transferOrderIds = list.split(",");

        TransferOrder transferOrderAttr = TransferOrder.dao.findById(transferOrderIds[0]);
        setAttr("transferOrderAttr", transferOrderAttr);

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

        DepartOrder order = DepartOrder.dao.findFirst("select * from depart_order where combine_type='"
                + DepartOrder.COMBINE_TYPE_PICKUP + "' order by depart_no desc limit 0,1");
        if (order != null) {
            String num = order.get("depart_no");
            String str = num.substring(2, num.length());
            System.out.println(str);
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
            setAttr("order_no", "PC" + order_no);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            order_no = format + "00001";
            setAttr("order_no", "PC" + order_no);
        }

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);

        setAttr("status", "新建");
        setAttr("saveOK", false);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/pickup/editPickupOrder.html");
    }

    // 拼车单列表显示
    public void pickuplist() {
        String orderNo = getPara("orderNo");
        String departNo = getPara("departNo");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sql = "";
        String sqlTotal = "";
        if (orderNo == null && departNo == null && beginTime == null && endTime == null) {
            sqlTotal = "select count(1) total from depart_order do " + ""
                    + " left join carinfo c on do.driver_id = c.id " + " where do.status!='取消' and combine_type = '"
                    + DepartOrder.COMBINE_TYPE_PICKUP + "'";

            sql = "select do.*,ct.contact_person,ct.phone,c.car_no,c.cartype,c.status cstatus,c.length, (select group_concat(dt.transfer_order_no separator '\r\n')  from depart_transfer dt where depart_id = do.id)  as transfer_order_no  from depart_order do "
                    + " left join carinfo c on do.carinfo_id = c.id "
                    + " left join party p on do.driver_id = p.id "
                    + " left join contact ct on p.contact_id = ct.id "
                    + " where do.status!='取消' and combine_type = '"
                    + DepartOrder.COMBINE_TYPE_PICKUP + "' order by do.create_stamp desc" + sLimit;
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }
            sqlTotal = "select count(distinct do.id) total from depart_order do "
                    + " left join carinfo c on do.driver_id = c.id "
                    + " left join depart_transfer dt2 on dt2.depart_id = do.id"
                    + " where do.status!='取消' and combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP
                    + "' and depart_no like '%" + departNo + "%' and dt2.transfer_order_no like '%" + orderNo
                    + "%' and do.create_stamp between '" + beginTime + "' and '" + endTime + "'";

            sql = "select distinct do.*,ct.contact_person,ct.phone,c.car_no,c.cartype,c.status cstatus,c.length, (select group_concat(dt.transfer_order_no separator '\r\n')  from depart_transfer dt where depart_id = do.id)  as transfer_order_no  from depart_order do "
                    + " left join carinfo c on do.carinfo_id = c.id "
                    + " left join party p on do.driver_id = p.id "
                    + " left join contact ct on p.contact_id = ct.id "
                    + " left join depart_transfer dt2 on dt2.depart_id = do.id"
                    + " where do.status!='取消' and combine_type = '"
                    + DepartOrder.COMBINE_TYPE_PICKUP
                    + "' and depart_no like '%"
                    + departNo
                    + "%' and dt2.transfer_order_no like '%"
                    + orderNo
                    + "%' and do.create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' order by do.create_stamp desc" + sLimit;
        }
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> warehouses = Db.find(sql);

        Map map = new HashMap();
        map.put("sEcho", pageIndex);
        map.put("iTotalRecords", rec.getLong("total"));
        map.put("iTotalDisplayRecords", rec.getLong("total"));

        map.put("aaData", warehouses);
        renderJson(map);
    }

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
        if (orderNo == null && status == null && address == null && customer == null && routeFrom == null
                && routeTo == null && beginTime == null && endTime == null) {
            sqlTotal = "select count(1) total  from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code"
                    + " where tor.status not in ('已入库','已签收') and tor.operation_type = 'own' and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'";
            sql = "select tor.id,tor.order_no,tor.cargo_nature,tor.order_type,"
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*tori.amount from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount)  from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,"
                    + " l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code"
                    + " where tor.status not in ('已入库','已签收') and tor.operation_type = 'own' and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'" + " order by tor.create_stamp desc" + sLimit;
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
                    + " where tor.status not in ('已入库','已签收') and tor.operation_type = 'own' and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'" + " "
                    + " and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%'"
                    + "and tor.order_no like '%" + orderNo
                    + "%' and tor.status like '%" + status + "%' and tor.address like '%" + address
                    + "%' and c.company_name like '%" + customer + "%' and create_stamp between '" + beginTime
                    + "' and '" + endTime + "' and tor.order_type like '%" + orderType + "%'";
            sql = "select tor.id,tor.order_no,tor.cargo_nature,tor.order_type,"
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*tori.amount from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount)  from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,"
                    + " l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " where tor.status not in ('已入库','已签收') and tor.operation_type = 'own' and ifnull(tor.pickup_assign_status, '') !='"
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
                    + "%' and c.company_name like '%"
                    + customer
                    + "%' and create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' and tor.order_type like '%"
                    + orderType
                    + "%' order by tor.CREATE_STAMP desc" + sLimit;
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
                    + " where tor.status not in ('已入库','已签收') and tor.operation_type = 'own' and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'" + " "
                    + " and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%'"
                    + "and tor.order_no like '%" + orderNo
                    + "%' and tor.status like '%" + status + "%' and tor.address like '%" + address
                    + "%' and c.company_name like '%" + customer + "%' and create_stamp between '" + beginTime
                    + "' and '" + endTime + "' and tor.order_type like '%" + orderType + "%'";

            sql = "select tor.id,tor.order_no,tor.cargo_nature,tor.order_type,"
                    + " case (select sum(tori.weight)*sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) when 0 then (select sum(pd.weight)*tori.amount from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.weight)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_weight, "
                    + " case ifnull((select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id),0) when 0 then (select sum(pd.volume)*sum(tori.amount)  from transfer_order_item tori left join product pd on pd.id  = tori.product_id where tor.id = tori.order_id)  else (select sum(tori.volume)*sum(tori.amount)  from transfer_order_item tori where tori.order_id = tor.id) end as total_volume, "
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,"
                    + " (select name from location where code = tor.route_from) route_from,(select name from location where code = tor.route_to) route_to,tor.create_stamp,tor.pickup_assign_status from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " where tor.status not in ('已入库','已签收') and tor.operation_type = 'own' and ifnull(tor.pickup_assign_status, '') !='"
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
                    + "%' and c.company_name like '%"
                    + customer
                    + "%' and create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' and tor.order_type like '%"
                    + orderType
                    + "%' order by tor.create_stamp desc" + sLimit;

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

    // 选取额外运输单
    public void externTransferOrderList() {
        String orderIds = "";
        String pickupOrderId = getPara("pickupOrderId");
        if (pickupOrderId == null || "".equals(pickupOrderId)) {
            pickupOrderId = "-1";
        }
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                "select * from depart_transfer where depart_id = ?", pickupOrderId);
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            orderIds += departTransferOrder.get("order_id") + ",";
        }
        if (!"".equals(orderIds) && orderIds != null) {
            orderIds = orderIds.substring(0, orderIds.length() - 1);
        } else {
            orderIds = "-1";
        }
        String pageIndex = getPara("sEcho");
        String sLimit = "";
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(1) total  from transfer_order tor "
                + " left join party p on tor.customer_id = p.id "
                + " left join contact c on p.contact_id = c.id "
                + " left join location l1 on tor.route_from = l1.code "
                + " left join location l2 on tor.route_to = l2.code"
                + " where tor.status not in ('已入库','已签收') and tor.operation_type = 'own' and ifnull(tor.pickup_assign_status, '') !='"
                + TransferOrder.ASSIGN_STATUS_ALL + "' and tor.id not in(" + orderIds + ")";
        String sql = "select tor.id,tor.order_no,tor.cargo_nature,tor.order_type,"
                + " (select sum(tori.weight) from transfer_order_item tori where tori.order_id = tor.id) as total_weight,"
                + " (select sum(tori.volume) from transfer_order_item tori where tori.order_id = tor.id) as total_volumn,"
                + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,"
                + " l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status from transfer_order tor "
                + " left join party p on tor.customer_id = p.id "
                + " left join contact c on p.contact_id = c.id "
                + " left join location l1 on tor.route_from = l1.code "
                + " left join location l2 on tor.route_to = l2.code"
                + " where tor.status not in ('已入库','已签收') and tor.operation_type = 'own' and ifnull(tor.pickup_assign_status, '') !='"
                + TransferOrder.ASSIGN_STATUS_ALL + "'" + " and tor.id not in(" + orderIds
                + ") order by tor.create_stamp desc" + sLimit;

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
        String sqlTotal = "select count(1) total from transfer_order_item tof"
                + " left join transfer_order  t_o  on tof.order_id =t_o.id "
                + " left join contact c on c.id in (select contact_id from party p where t_o.customer_id=p.id)"
                + " where tof.order_id in(" + order_id + ")";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select tof.* , t_o.order_no as order_no, t_o.id as tr_order_id, c.company_name as customer  from transfer_order_item tof"
                + " left join transfer_order  t_o  on tof.order_id = t_o.id "
                + "left join contact c on c.id in (select contact_id from party p where t_o.customer_id=p.id)"
                + " where tof.order_id in(" + order_id + ")  order by c.id" + sLimit;
        List<Record> departOrderitem = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", departOrderitem);
        renderJson(Map);
    }

    // 保存拼车单
    public void savePickupOrder() {
        DepartOrder pickupOrder = null;
        String pickId = getPara("pickupId");
        String driverId = getPara("driver_id");
        String carinfoId = getPara("carinfoId");
        Carinfo carinfo = null;
        String checkedDetail = getPara("checkedDetail");
        String uncheckedDetailIds = getPara("uncheckedDetail");
        if (pickId == null || "".equals(pickId)) {
            pickupOrder = new DepartOrder();
            pickupOrder.set("depart_no", getPara("order_no"));
            pickupOrder.set("status", getPara("status"));
            pickupOrder.set("create_by", getPara("create_by"));
            pickupOrder.set("car_no", getPara("car_no"));
            pickupOrder.set("car_type", getPara("car_type"));
            pickupOrder.set("car_size", getPara("car_size"));
            pickupOrder.set("car_follow_name", getPara("car_follow_name"));
            pickupOrder.set("car_follow_phone", getPara("car_follow_phone"));
            pickupOrder.set("remark", getPara("remark"));
            pickupOrder.set("combine_type", DepartOrder.COMBINE_TYPE_PICKUP);
            pickupOrder.set("pickup_mode", getPara("pickupMode"));
            pickupOrder.set("address", getPara("address"));
            pickupOrder.set("kilometres", getPara("kilometres").equals("") ? 0 : getPara("kilometres"));
            pickupOrder.set("road_bridge", getPara("roadBridge").equals("") ? 0 : getPara("roadBridge"));
            pickupOrder.set("income", getPara("income").equals("") ? 0 : getPara("income"));
            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            pickupOrder.set("create_stamp", sqlDate);
            if ("own".equals(getPara("pickupMode"))) {
                if (driverId != null && !"".equals(driverId)) {
                    pickupOrder.set("driver_id", driverId);
                }
                if (carinfoId != null && !"".equals(carinfoId)) {
                    pickupOrder.set("carinfo_id", carinfoId);
                }
            } else {
                if (!getPara("sp_id").equals("")) {
                    pickupOrder.set("sp_id", getPara("sp_id"));
                }
            }
            String[] values = getParaValues("checkbox");
            if (values != null) {
                if (values.length == 1) {
                    for (int i = 0; i < values.length; i++) {
                        if ("yandCheckbox".equals(values[i])) {
                            pickupOrder.set("address", getPara("address"));
                        }
                        if ("warehouseCheckbox".equals(values[i])) {
                            pickupOrder.set("warehouse_id", getPara("gateInSelect"));
                        } 
                    }
                } else {
                    for (int i = 0; i < values.length; i++) {
                        if ("yandCheckbox".equals(values[i])) {
                            pickupOrder.set("address", getPara("address"));
                        }
                        if ("warehouseCheckbox".equals(values[i])) {
                            pickupOrder.set("warehouse_id", getPara("gateInSelect"));
                        }
                    }
                }
            } else {
                pickupOrder.set("address", null);
                pickupOrder.set("warehouse_id", null);
            }
            pickupOrder.save();
            saveDepartTransfer(pickupOrder, getPara("orderid"), checkedDetail, uncheckedDetailIds);
            savePickupOrderMilestone(pickupOrder);
        } else {
            pickupOrder = DepartOrder.dao.findById(pickId);
            pickupOrder.set("depart_no", getPara("order_no"));
            pickupOrder.set("status", getPara("status"));
            pickupOrder.set("create_by", getPara("create_by"));
            pickupOrder.set("car_no", getPara("car_no"));
            pickupOrder.set("car_type", getPara("car_type"));
            pickupOrder.set("car_size", getPara("car_size"));
            pickupOrder.set("car_follow_name", getPara("car_follow_name"));
            pickupOrder.set("car_follow_phone", getPara("car_follow_phone"));
            pickupOrder.set("remark", getPara("remark"));
            pickupOrder.set("combine_type", DepartOrder.COMBINE_TYPE_PICKUP);
            pickupOrder.set("pickup_mode", getPara("pickupMode"));
            pickupOrder.set("address", getPara("address"));
            pickupOrder.set("kilometres", getPara("kilometres").equals("") ? 0 : getPara("kilometres"));
            pickupOrder.set("road_bridge", getPara("roadBridge").equals("") ? 0 : getPara("roadBridge"));
            pickupOrder.set("income", getPara("income").equals("") ? 0 : getPara("income"));
            updateTransferOrderPickupMode(pickupOrder);
            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            pickupOrder.set("create_stamp", sqlDate);
            if ("own".equals(getPara("pickupMode"))) {
                if (driverId != null && !"".equals(driverId)) {
                    pickupOrder.set("driver_id", driverId);
                }
                if (carinfoId != null && !"".equals(carinfoId)) {
                    pickupOrder.set("carinfo_id", carinfoId);
                }
            } else {
                if (!getPara("sp_id").equals("")) {
                    pickupOrder.set("sp_id", getPara("sp_id"));
                }
            }
            String[] values = getParaValues("checkbox");
            if (values != null) {
                if (values.length == 1) {
                    for (int i = 0; i < values.length; i++) {
                        if ("yandCheckbox".equals(values[i])) {
                            pickupOrder.set("warehouse_id", null);
                        }
                        if ("warehouseCheckbox".equals(values[i])) {
                            pickupOrder.set("address", null);
                        }
                    }
                } else {
                    for (int i = 0; i < values.length; i++) {
                        if ("yandCheckbox".equals(values[i])) {
                            pickupOrder.set("address", getPara("address"));
                        }
                        if ("warehouseCheckbox".equals(values[i])) {
                            pickupOrder.set("warehouse_id", getPara("gateInSelect"));
                        }
                    }
                }
            } else {
                pickupOrder.set("address", null);
                pickupOrder.set("warehouse_id", null);
            }
            pickupOrder.update();
            updateDepartTransfer(pickupOrder, getPara("orderid"), checkedDetail, uncheckedDetailIds);
        }
        renderJson(pickupOrder);
    }

    // 更新运输单的提货方式
    private void updateTransferOrderPickupMode(DepartOrder pickupOrder) {
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                "select * from depart_transfer where depart_id = ?", pickupOrder.get("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
            if (transferOrder != null) {
                transferOrder.set("pickup_mode", pickupOrder.get("pickup_mode"));
                transferOrder.update();
            }
        }
    }

    // 更新中间表
    private void updateDepartTransfer(DepartOrder pickupOrder, String orderId, String checkedDetail,
            String uncheckedDetailId) {
        if (checkedDetail != null && !"".equals(checkedDetail)) {
            String[] checkedDetailIds = checkedDetail.split(",");
            TransferOrderItemDetail transferOrderItemDetail = null;
            for (int j = 0; j < checkedDetailIds.length && checkedDetailIds.length > 0; j++) {
                transferOrderItemDetail = TransferOrderItemDetail.dao.findById(checkedDetailIds[j]);
                transferOrderItemDetail.set("pickup_id", pickupOrder.get("id"));
                transferOrderItemDetail.update();
            }
            TransferOrder transferOrder = TransferOrder.dao.findById(transferOrderItemDetail.get("order_id"));
            transferOrder.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
            transferOrder.update();
        }
        String[] uncheckedDetailIds = uncheckedDetailId.split(",");
        if (uncheckedDetailId != null && !"".equals(uncheckedDetailId)) {
            TransferOrderItemDetail transferOrderItemDetail = null;
            for (int j = 0; j < uncheckedDetailIds.length && uncheckedDetailIds.length > 0; j++) {
                transferOrderItemDetail = TransferOrderItemDetail.dao.findById(uncheckedDetailIds[j]);
                transferOrderItemDetail.set("pickup_id", null);
                transferOrderItemDetail.update();
            }
        }
        if (uncheckedDetailIds.length == 0 || "".equals(uncheckedDetailIds[0])) {
            List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
                    .find("select * from transfer_order_item_detail where order_id in(" + orderId + ")");
            String str = "";
            for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
                Long departId = transferOrderItemDetail.get("pickup_id");
                if (departId == null || "".equals(departId)) {
                    str += departId;
                }
            }
            if ("".equals(str)) {
                List<TransferOrder> transferOrders = TransferOrder.dao
                        .find("select * from transfer_order where id in (" + orderId + ")");
                for (TransferOrder transferOrder : transferOrders) {
                    transferOrder.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
                    transferOrder.update();
                }
            }
        }
    }

    // 将数据保存进中间表
    private void saveDepartTransfer(DepartOrder pickupOrder, String param, String checkedDetail,
            String uncheckedDetailId) {
        DepartTransferOrder departTransferOrder = null;
        String[] params = param.split(",");
        if (checkedDetail == null || "".equals(checkedDetail)) {
            for (int i = 0; i < params.length; i++) {
                departTransferOrder = new DepartTransferOrder();
                departTransferOrder.set("depart_id", pickupOrder.get("id"));
                departTransferOrder.set("order_id", params[i]);
                TransferOrder transferOrder = TransferOrder.dao.findById(params[i]);
                transferOrder.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
                transferOrder.set("pickup_mode", pickupOrder.get("pickup_mode"));
                transferOrder.update();
                departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
                departTransferOrder.save();

                List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find(
                        "select * from transfer_order_item_detail where order_id = ?", params[i]);
                for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
                    if (transferOrderItemDetail.get("pickup_id") == null) {
                        transferOrderItemDetail.set("pickup_id", pickupOrder.get("id"));
                        transferOrderItemDetail.update();
                    }
                }
            }
        } else {
            for (int i = 0; i < params.length; i++) {
                departTransferOrder = new DepartTransferOrder();
                departTransferOrder.set("depart_id", pickupOrder.get("id"));
                departTransferOrder.set("order_id", params[i]);
                TransferOrder transferOrder = TransferOrder.dao.findById(params[i]);
                transferOrder.set("pickup_mode", pickupOrder.get("pickup_mode"));
                transferOrder.update();
                departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
                departTransferOrder.save();
            }
            String[] checkedDetailIds = checkedDetail.split(",");
            for (int j = 0; j < checkedDetailIds.length; j++) {
                TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
                        .findById(checkedDetailIds[j]);
                transferOrderItemDetail.set("pickup_id", pickupOrder.get("id"));
                transferOrderItemDetail.update();
            }

            String[] uncheckedDetailIds = uncheckedDetailId.split(",");
            for (int j = 0; j < uncheckedDetailIds.length; j++) {
                TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
                        .findById(uncheckedDetailIds[j]);
                transferOrderItemDetail.set("pickup_id", "");
                transferOrderItemDetail.update();
            }
        }
    }

    // 修改拼车单页面
    public void edit() {
        String sql = "select do.*,co.contact_person,co.phone,u.user_name,(select group_concat(dt.order_id  separator',')  from depart_transfer  dt "
                + "where dt.depart_id =do.id)as order_id from depart_order  do "
                + "left join contact co on co.id in( select p.contact_id  from party p where p.id=do.driver_id ) "
                + "left join user_login  u on u.id=do.create_by where do.combine_type ='"
                + DepartOrder.COMBINE_TYPE_PICKUP + "' and do.id in(" + getPara("id") + ")";
        DepartOrder pickupOrder = DepartOrder.dao.findFirst(sql);
        setAttr("pickupOrder", pickupOrder);

        Long sp_id = pickupOrder.get("sp_id");
        if (sp_id != null) {
            Party sp = Party.dao.findById(sp_id);
            Contact spContact = Contact.dao.findById(sp.get("contact_id"));
            setAttr("spContact", spContact);
        }
        Long driverId = pickupOrder.get("driver_id");
        if (driverId != null) {
            Party driver = Party.dao.findById(driverId);
            Contact driverContact = Contact.dao.findById(driver.get("contact_id"));
            setAttr("driverContact", driverContact);
        }
        Long carinfoId = pickupOrder.get("carinfo_id");
        if (carinfoId != null) {
            Carinfo carinfo = Carinfo.dao.findById(carinfoId);
            setAttr("carinfo", carinfo);
        }
        UserLogin userLogin = UserLogin.dao.findById(pickupOrder.get("create_by"));
        setAttr("userLogin2", userLogin);
        setAttr("depart_id", getPara());
        String orderId = "";
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                "select * from depart_transfer where depart_id = ?", pickupOrder.get("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            orderId += departTransferOrder.get("order_id") + ",";
        }
        orderId = orderId.substring(0, orderId.length() - 1);
        setAttr("localArr", orderId);

        TransferOrderMilestone transferOrderMilestone = TransferOrderMilestone.dao.findFirst(
                "select * from transfer_order_milestone where pickup_id = ? order by create_stamp desc",
                pickupOrder.get("id"));
        setAttr("transferOrderMilestone", transferOrderMilestone);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/pickup/editPickupOrder.html");
    }

    // 保存拼车里程碑
    private void savePickupOrderMilestone(DepartOrder pickupOrder) {
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        transferOrderMilestone.set("status", "新建");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE);
        transferOrderMilestone.set("pickup_id", pickupOrder.get("id"));
        transferOrderMilestone.save();
    }

    // 在途提货拼车单
    public void pickupOrderMilestone() {
        render("/yh/pickup/pickupOrderMilestone.html");
    }

    // 列出所有的在途提货拼车单
    public void pickupOrderMilestoneList() {
        Map<String, List> map = new HashMap<String, List>();
        List<String> usernames = new ArrayList<String>();
        List<TransferOrderMilestone> milestones = new ArrayList<TransferOrderMilestone>();
        List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                .find("select pickup_id from transfer_order_milestone where type = '"
                        + TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE + "' group by pickup_id");
        for (TransferOrderMilestone pm : transferOrderMilestones) {
            if (pm.get("pickup_id") != null) {
                TransferOrderMilestone transferOrderMilestone = TransferOrderMilestone.dao
                        .findFirst("select tom.*,dto.depart_no from transfer_order_milestone tom "
                                + " left join depart_order dto on dto.id = " + pm.get("pickup_id")
                                + " where tom.type = '" + TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE
                                + "' and tom.pickup_id=" + pm.get("pickup_id") + " and dto.combine_type = '"
                                + DepartOrder.COMBINE_TYPE_PICKUP + "' order by tom.create_stamp desc");
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = userLogin.get("user_name");
                milestones.add(transferOrderMilestone);
                usernames.add(username);
            }
        }
        map.put("milestones", milestones);
        map.put("usernames", usernames);
        renderJson(map);
    }

    // 完成
    public void finishPickupOrder() {
        DepartOrder pickupOrder = DepartOrder.dao.findById(getPara("pickupOrderId"));
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                "select * from depart_transfer where depart_id = ?", pickupOrder.get("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
            if ("新建".equals(transferOrder.get("status"))) {
                transferOrder.set("status", "已入货场");
                transferOrder.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
                transferOrder.update();
                TransferOrderMilestone milestone = new TransferOrderMilestone();
                milestone.set("status", "已入货场");
                milestone.set("location", "");
                milestone.set("order_id", transferOrder.get("id"));
                String name = (String) currentUser.getPrincipal();
                List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
                milestone.set("create_by", users.get(0).get("id"));
                java.util.Date utilDate = new java.util.Date();
                java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
                milestone.set("create_stamp", sqlDate);
                milestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
                milestone.save();
            }
        }
        pickupOrder.set("status", "已入货场");
        pickupOrder.update();
        TransferOrderMilestone pickupMilestone = new TransferOrderMilestone();
        pickupMilestone.set("status", "已入货场");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        pickupMilestone.set("create_by", users.get(0).get("id"));
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        pickupMilestone.set("create_stamp", sqlDate);
        pickupMilestone.set("pickup_id", pickupOrder.get("id"));
        pickupMilestone.set("type", TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE);
        pickupMilestone.save();

        // 生成应付
        TransferOrderFinItem tFinItem = new TransferOrderFinItem();
        List<Record> departList = Db.find("select order_id from depart_transfer where depart_id ='"
                + getPara("pickupOrderId") + "'");
        for (int i = 0; i < departList.size(); i++) {
            TransferOrder tOrder = TransferOrder.dao.findById(departList.get(i).get("order_id"));
            if (pickupOrder.get("sp_id") != null) {
                List<Record> contractList = Db
                        .find("select * from contract_item where contract_id in(select id from contract c where c.party_id ='"
                                + pickupOrder.get("sp_id")
                                + "') and from_id = '"
                                + tOrder.get("route_from")
                                + "' and to_id ='" + tOrder.get("route_to") + "' ");
            }

        }
        renderJson("{\"success\":true}");
    }

    // 查出所有运输单的提货地点
    public void findAllAddress() {
        List<TransferOrder> transferOrders = new ArrayList<TransferOrder>();
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao
                .find("select dt.*,tor.pickup_seq  from depart_transfer dt left join transfer_order tor on tor.id = dt.order_id where depart_id = ? order by tor.pickup_seq desc",
                        getPara("pickupOrderId"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            TransferOrder transferOrder = TransferOrder.dao
                    .findFirst(
                            "select tor.*,c.abbr cname from transfer_order tor left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id where tor.id = ?",
                            departTransferOrder.get("order_id"));
            transferOrders.add(transferOrder);
        }
        renderJson(transferOrders);
    }

    // 查出所有运输单的提货地点
    public void swapPickupSeq() {
        TransferOrder transferOrder = TransferOrder.dao.findById(getPara("currentId"));
        transferOrder.set("pickup_seq", getPara("currentVal"));
        transferOrder.update();
        TransferOrder transferOrderPrev = TransferOrder.dao.findById(getPara("targetId"));
        transferOrderPrev.set("pickup_seq", getPara("targetVal"));
        transferOrderPrev.update();
        renderJson("{\"success\":true}");
    }

    // 取消
    public void cancel() {
        String id = getPara();
        DepartOrder.dao.findById(id).set("Status", "取消").update();
        renderJson("{\"success\":true}");
    }

    // 筛选掉入库的运输单
    public void getTransferOrderDestination() {
        String pickupOrderId = getPara("pickupOrderId");
        Map<String, String[]> map = getParaMap();
        String orderId = "";
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            if ("pickupOrderId".equals(entry.getKey())) {
                continue;
            }
            String[] values = entry.getValue();
            String value = values[0];

            if (value.indexOf("warehouse") >= 0) {
                String transOrderId = value.substring(9);
                List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                        "select * from depart_transfer where depart_id = ?", pickupOrderId);
                for (DepartTransferOrder departTransferOrder : departTransferOrders) {
                    if (transOrderId.equals(departTransferOrder.get("order_id") + "")) {
                        // 去掉入库的单据
                        TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
                        orderId += transferOrder.get("id") + ",";
                        transferOrder.set("status", "已入库");
                        transferOrder.update();
                        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
                        transferOrderMilestone.set("status", "已入库");
                        String name = (String) currentUser.getPrincipal();
                        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name
                                + "'");
                        transferOrderMilestone.set("create_by", users.get(0).get("id"));
                        java.util.Date utilDate = new java.util.Date();
                        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
                        transferOrderMilestone.set("create_stamp", sqlDate);
                        transferOrderMilestone.set("order_id", transferOrder.get("id"));
                        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);

                        savegateIn(departTransferOrder);
                    }
                }
            }
        }
        renderJson("{\"success\":true}");
    }

    public void savegateIn(DepartTransferOrder departTransferOrder) {
        // product_id不为空时入库
        List<Record> transferOrderItem = Db.find("select * from transfer_order_item where order_id='"
                + departTransferOrder.get("order_id") + "'");
        TransferOrder tOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));

        InventoryItem item = null;
        if (transferOrderItem.size() > 0) {
            for (int i = 0; i < transferOrderItem.size(); i++) {
                if (transferOrderItem.get(i).get("product_id") != null) {
                    item = new InventoryItem();
                    String in_item_check_sql = "select * from inventory_item where product_id="
                            + Integer.parseInt(transferOrderItem.get(i).get("product_id").toString()) + ""
                            + " and warehouse_id=" + Integer.parseInt(tOrder.get("warehouse_id").toString()) + "";
                    InventoryItem inventoryItem = InventoryItem.dao.findFirst(in_item_check_sql);
                    if (inventoryItem == null) {
                        item.set("party_id", tOrder.get("customer_id"));
                        item.set("warehouse_id", tOrder.get("warehouse_id"));
                        item.set("product_id", transferOrderItem.get(i).get("product_id"));
                        item.set("total_quantity", transferOrderItem.get(i).get("amount"));
                        item.save();
                    } else {
                        item = InventoryItem.dao.findById(inventoryItem.get("id"));
                        item.set(
                                "total_quantity",
                                Double.parseDouble(item.get("total_quantity").toString())
                                        + Double.parseDouble(transferOrderItem.get(i).get("amount").toString()));
                        item.update();
                    }
                }
            }
        }
    }

    // 添加额的外运输单
    public void addExternalTransferOrder() {
        String transferOrderId = getPara("transferOrderIds");
        String pickupOrderId = getPara("pickupOrderId");
        String[] transferOrderIds = transferOrderId.split(",");
        DepartTransferOrder departTransferOrder = null;
        for (int i = 0; i < transferOrderIds.length; i++) {
            departTransferOrder = new DepartTransferOrder();
            departTransferOrder.set("depart_id", pickupOrderId);
            departTransferOrder.set("order_id", transferOrderIds[i]);
            TransferOrder transferOrder = TransferOrder.dao.findById(transferOrderIds[i]);
            departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
            departTransferOrder.save();
        }
        renderJson("{\"success\":true}");
    }

    // 点击货品table的查看 ，显示货品对应的单品
    public void findAllItemDetail() {
        String itemId = getPara("item_id");
        String pickupId = getPara("pickupId");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String category = getPara("category");
        String sqlTotal = "select count(1) total from transfer_order_item_detail tod where tod.item_id = " + itemId
                + " and tod.depart_id = " + pickupId;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from transfer_order_item_detail tod where tod.item_id = " + itemId
                + " and tod.depart_id = " + pickupId;

        List<Record> details = Db.find(sql);

        Map productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        productListMap.put("aaData", details);
        renderJson(productListMap);
    }
}
