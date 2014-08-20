package controllers.yh.departOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartOrderFinItem;
import models.DepartTransferOrder;
import models.Fin_item;
import models.InventoryItem;
import models.Location;
import models.Party;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.contract.Contract;
import models.yh.profile.Carinfo;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class DepartOrderController extends Controller {

    private Logger logger = Logger.getLogger(DepartOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
        if (LoginUserController.isAuthenticated(this))
            render("departOrder/departOrderList.html");
    }

    public void onTrip() {
        if (LoginUserController.isAuthenticated(this))
            render("departOrder/departOrderOnTripList.html");
    }

    public void list() {
        String orderNo = getPara("orderNo");
        String departNo = getPara("departNo");
        String status = getPara("status");
        String sp = getPara("sp");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        String sql = "";
        String sqlTotal = "";
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        if (orderNo == null && departNo == null && status == null && sp == null && beginTime == null && endTime == null) {
            sqlTotal = "select count(1) total from depart_order deo "
                    + "left join carinfo  car on deo.driver_id=car.id" + " where combine_type = '"
                    + DepartOrder.COMBINE_TYPE_DEPART + "'";

            sql = "select deo.id,deo.depart_no ,deo.create_stamp ,deo.status as depart_status,ct.contact_person,ct.phone,c.car_no,c.cartype,c.length, (select group_concat(tr.order_no separator '\r\n') from transfer_order tr where tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id ))  as transfer_order_no  from depart_order deo "
                    + " left join carinfo c on deo.carinfo_id = c.id "
                    + " left join party p on deo.driver_id = p.id "
                    + " left join contact ct on p.contact_id = ct.id  where  ifnull(deo.status,'') != 'aa'  and combine_type = '"
                    + DepartOrder.COMBINE_TYPE_DEPART + "' order by deo.create_stamp desc" + sLimit;
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }
            sqlTotal = "select count(1) total from depart_order deo "
                    + " left join party p on deo.driver_id = p.id and p.party_type = 'DRIVER' "
                    + "left join carinfo  car on deo.driver_id=car.id"
                    + " left join contact c on p.contact_id = c.id "
                    + " left join transfer_order tr  on tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id )"
                    + "  where deo.combine_type = 'DEPART' and " + "ifnull(deo.status,'') like '%" + status + "%' and "
                    + "ifnull(deo.depart_no,'') like '%" + departNo + "%' and " + "ifnull(c.company_name,'')  like '%"
                    + sp + "%' and " + "ifnull(tr.order_no,'') like '%" + orderNo + "%'"
                    + " and deo.create_stamp between '" + beginTime + "' " + "and '" + endTime
                    + "'group by deo.id order by deo.create_stamp desc ";

            sql = "select deo.id,deo.depart_no ,deo.create_stamp ,deo.status as depart_status,ct.contact_person,ct.phone,c.car_no,c.cartype,c.length, group_concat(tr.order_no separator ' ') as transfer_order_no "
                    + " from depart_order deo"
                    + " left join carinfo c on deo.carinfo_id = c.id "
                    + " left join party p on deo.driver_id = p.id "
                    + " left join contact ct on p.contact_id = ct.id "
                    + " left join transfer_order tr  on tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id )"
                    + "  where deo.combine_type = 'DEPART' and ifnull(deo.status,'') like '%"
                    + status
                    + "%' and ifnull(deo.depart_no,'') like '%"
                    + departNo
                    + "%' and ifnull(tr.order_no,'') like '%"
                    + orderNo
                    + "%'"
                    + " and deo.create_stamp between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "'group by deo.id order by deo.create_stamp desc " + sLimit;
        }
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> departOrders = Db.find(sql);

        Map map = new HashMap();
        map.put("sEcho", pageIndex);
        map.put("iTotalRecords", rec.getLong("total"));
        map.put("iTotalDisplayRecords", rec.getLong("total"));
        map.put("aaData", departOrders);
        renderJson(map);
    }

    public void add() {
        if (LoginUserController.isAuthenticated(this))
            render("/yh/departOrder/allTransferOrderList.html");
    }

    // 修改发车单页面
    public void edit() {
        String sql = "select do.*,co.contact_person,co.phone,u.user_name,(select group_concat(dt.order_id  separator',')  from depart_transfer  dt "
                + "where dt.depart_id =do.id)as order_id from depart_order  do "
                + "left join contact co on co.id in( select p.contact_id  from party p where p.id=do.driver_id ) "
                + "left join user_login  u on u.id=do.create_by where do.combine_type ='"
                + DepartOrder.COMBINE_TYPE_DEPART + "' and do.id in(" + getPara("id") + ")";
        DepartOrder departOrder = DepartOrder.dao.findFirst(sql);
        setAttr("departOrder", departOrder);

        DepartTransferOrder departTransferOrder2 = DepartTransferOrder.dao.findFirst(
                "select * from depart_transfer where depart_id = ? order by id desc", departOrder.get("id"));

        TransferOrder transferOrderAttr = TransferOrder.dao.findById(departTransferOrder2.get("order_id"));
        setAttr("transferOrderAttr", transferOrderAttr);

        Long sp_id = departOrder.get("sp_id");
        if (sp_id != null) {
            Party sp = Party.dao.findById(sp_id);
            Contact spContact = Contact.dao.findById(sp.get("contact_id"));
            setAttr("spContact", spContact);
        }
        Long driverId = departOrder.get("driver_id");
        if (driverId != null) {
            Party driver = Party.dao.findById(driverId);
            Contact driverContact = Contact.dao.findById(driver.get("contact_id"));
            setAttr("driverContact", driverContact);
        }
        Long carinfoId = departOrder.get("carinfo_id");
        if (carinfoId != null) {
            Carinfo carinfo = Carinfo.dao.findById(carinfoId);
            setAttr("carinfo", carinfo);
        }
        UserLogin userLogin = UserLogin.dao.findById(departOrder.get("create_by"));
        setAttr("userLogin2", userLogin);
        setAttr("depart_id", getPara());
        String orderId = "";
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                "select * from depart_transfer where depart_id = ?", departOrder.get("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            orderId += departTransferOrder.get("order_id") + ",";
        }
        orderId = orderId.substring(0, orderId.length() - 1);
        setAttr("localArr", orderId);

        String routeFrom = departOrder.get("route_from");
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

        String routeTo = departOrder.get("route_to");
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

        TransferOrderMilestone transferOrderMilestone = TransferOrderMilestone.dao.findFirst(
                "select * from transfer_order_milestone where pickup_id = ? order by create_stamp desc",
                departOrder.get("id"));
        setAttr("transferOrderMilestone", transferOrderMilestone);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/departOrder/editDepartOrder.html");
    }

    // 弹窗
    public void boxedit() {
        if (!"".equals(getPara("edit_depart_id"))) {
            getIintedit(Integer.parseInt(getPara("edit_depart_id").toString()));
        }
    }

    public void getIintedit(int depart_id) {
        int edit_depart_id = depart_id;
        String sql = "select deo.*,co.contact_person,co.phone,u.user_name,(select group_concat(dt.order_id  separator',') from depart_transfer  dt "
                + "where dt.depart_id =deo.id)as order_id from depart_order  deo "
                + "left join contact co on co.id in( select p.contact_id  from party p where p.id=deo.driver_id ) "
                + "left join user_login  u on u.id=deo.create_by where deo.combine_type ='DEPART' and deo.id in("
                + edit_depart_id + ")";
        DepartOrder depar = DepartOrder.dao.findFirst(sql);
        String routeFrom = depar.get("route_from");
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

        String routeTo = depar.get("route_to");
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
        String sql_order_id = "select * from depart_transfer  where depart_id=" + depart_id;
        List<Record> order_id = Db.find(sql_order_id);
        String tr_order_id = "";
        for (int i = 0; i < order_id.size(); i++) {
            String id = order_id.get(i).get("order_id").toString();
            tr_order_id += id + ",";
        }
        tr_order_id = tr_order_id.substring(0, tr_order_id.length() - 1);
        String order_check_sql = "select depart_id , order_id from depart_transfer  where depart_id=" + depart_id + "";
        List<Record> order_check = Db.find(order_check_sql);
        if (order_check.size() == 1) {
            int id = Integer.parseInt(order_check.get(0).get("order_id").toString());
            TransferOrder tr = TransferOrder.dao.findById(id);
            if (!"delivery".equals(tr.get("arrival_mode"))) {
                setAttr("check_sh", false);
                setAttr("notifi_check", false);
            } else {
                setAttr("notifi_check", true);
            }
        } else {
            for (int i = 0; i < order_check.size(); i++) {
                int id = Integer.parseInt(order_check.get(0).get("order_id").toString());
                TransferOrder tr = TransferOrder.dao.findById(id);
                if (!"delivery".equals(tr.get("arrival_mode"))) {
                    setAttr("check_sh", false);
                    break;
                }
            }
        }
        Long driverId = depar.get("driver_id");
        if (driverId != null) {
            Party driver = Party.dao.findById(driverId);
            Contact driverContact = Contact.dao.findById(driver.get("contact_id"));
            setAttr("driverContact", driverContact);
        }
        Long carinfoId = depar.get("carinfo_id");
        if (carinfoId != null) {
            Carinfo carinfo = Carinfo.dao.findById(carinfoId);
            setAttr("carinfo", carinfo);
        }
        setAttr("type", "many");
        setAttr("depart_id", getPara());
        setAttr("localArr", tr_order_id);
        setAttr("depart", depar);
        setAttr("depart_id", depart_id);
        if (LoginUserController.isAuthenticated(this))
            render("departOrder/editTransferOrder.html");

    }

    public void createTransferOrderList() {
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String address = getPara("address");
        String customer = getPara("customer");
        String routeFrom = getPara("routeFrom");
        String routeTo = getPara("routeTo");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        Record rec = null;
        String sLimit = "";
        String sql = "";
        String sqlTotal = "";
        String pageIndex = getPara("sEcho");
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
                    + " where (tor.status = '已入货场'or tor.status like '%部分%') and ifnull(tor.depart_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'";
            rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));
            sql = "select distinct tor.id,tor.order_no,tor.cargo_nature, tor.arrival_mode ,"
                    + " (select sum(toi.weight) from transfer_order_item toi where toi.order_id = tor.id) as total_weight,"
                    + " (select sum(toi.volume) from transfer_order_item toi where toi.order_id = tor.id) as total_volumn,"
                    + " (select sum(toi.amount) from transfer_order_item toi where toi.order_id = tor.id) as total_amount,"
                    + " ifnull(dor.address, '') doaddress, ifnull(tor.pickup_mode, '') pickup_mode,tor.status,c.company_name cname,"
                    + " l1.name route_from,l2.name route_to,tor.create_stamp ,cont.company_name as spname,cont.id as spid from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id left join contact cont on  cont.id=p2.contact_id "
                    + " left join depart_transfer dt on tor.id = dt.order_id left join depart_order dor on dor.id = dt.depart_id "
                    + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code"
                    + " where (tor.status = '已入货场' or tor.status like '%部分%')"
                    + "  and ifnull(tor.depart_assign_status, '') !='" + TransferOrder.ASSIGN_STATUS_ALL
                    + "' order by tor.create_stamp desc";
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
                    + " where (tor.status = '已入货场' or tor.status like '%部分%') and ifnull(tor.depart_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'" + " and tor.order_no like '%" + orderNo
                    + "%' and tor.status like '%" + status + "%' and tor.address like '%" + address
                    + "%' and c.company_name like '%" + customer + "%' and create_stamp between '" + beginTime
                    + "' and '" + endTime + "'";

            sql = "select distinct tor.id,tor.order_no,tor.cargo_nature, tor.arrival_mode ,"
                    + " (select sum(tori.weight) from transfer_order_item tori where tori.order_id = tor.id) as total_weight,"
                    + " (select sum(tori.volume) from transfer_order_item tori where tori.order_id = tor.id) as total_volumn,"
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " dor.address doaddress,tor.pickup_mode,tor.status,c.company_name cname,"
                    + " (select name from location where code = tor.route_from) route_from,(select name from location where code = tor.route_to) route_to,tor.create_stamp,tor.depart_assign_status,c2.company_name spname from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id  left join contact c2 on p2.contact_id = c2.id "
                    + " left join depart_transfer dt on tor.id = dt.order_id left join depart_order dor on dor.id = dt.depart_id "
                    + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " where (tor.status ='已入货场' or tor.status like '%部分%') and ifnull(tor.depart_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'" + " and tor.order_no like '%" + orderNo
                    + "%' and tor.status like '%" + status + "%' and tor.address like '%" + address
                    + "%' and c.company_name like '%" + customer + "%' and tor.create_stamp between '" + beginTime
                    + "' and '" + endTime + "'" + " order by tor.create_stamp desc";
        }
        rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> transferOrders = Db.find(sql);

        Map transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);
        renderJson(transferOrderListMap);
    }

    public void createDepartOrder() {
        String list = this.getPara("localArr");
        setAttr("localArr", list);

        String[] orderIds = list.split(",");
        for (int i = 0; i < orderIds.length; i++) {
            TransferOrder transferOrder = TransferOrder.dao.findById(orderIds[i]);
            if (transferOrder.get("sp_id") != null) {
                Party sp = Party.dao.findById(transferOrder.get("sp_id"));
                setAttr("partySp", sp);
                Contact spContact = Contact.dao.findById(sp.get("contact_id"));
                setAttr("spContact", spContact);
                break;
            }
        }

        logger.debug("localArr" + list);
        String order_no = null;
        setAttr("saveOK", false);
        TransferOrder transferOrder = new TransferOrder();
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));

        DepartOrder order = DepartOrder.dao.findFirst("select * from depart_order where combine_type='"
                + DepartOrder.COMBINE_TYPE_DEPART + "' order by depart_no desc limit 0,1");
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
            setAttr("order_no", "FC" + order_no);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            order_no = format + "00001";
            setAttr("order_no", "FC" + order_no);
        }

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);

        setAttr("status", "新建");
        setAttr("saveOK", false);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/departOrder/editDepartOrder.html");
    }

    // editTransferOrder 初始数据
    public void getInitDepartOrderItems() {
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

    // 判断是否单据继续创建
    public boolean CheckDepartOrder(String[] order_id) {
        int detail_total = 0;// 运输单货品/单品数
        int depart_detail = 0;// 发车单单品个数
        int last = 0;// 未处理单品个数
        boolean check = false;
        for (int i = 0; i < order_id.length; i++) {
            /* 运输单查询货品、单品个数 */

            String total_tr_item = "select * from transfer_order_item  where order_id=" + Integer.parseInt(order_id[i]);// 货品id
            List<Record> tr_itemlist = Db.find(total_tr_item);
            for (int j = 0; j < tr_itemlist.size(); j++) {
                String total_tr_detail = "select * from transfer_order_item_detail  where item_id="
                        + Integer.parseInt(tr_itemlist.get(j).get("id").toString());// 单品id
                List<Record> tr_detaillist = Db.find(total_tr_detail);
                if (tr_detaillist.size() > 0) {
                    detail_total = detail_total + tr_detaillist.size();
                } else {
                    detail_total++;
                }
            }
            /* 发车单查询货品，单品个数 */
            String total_depart_detail = "select * from depart_transfer_itemdetail  where order_id="
                    + Integer.parseInt(order_id[i]);// 发车单单品个数
            List<Record> depart_detaillist = Db.find(total_depart_detail);
            depart_detail = depart_detail + depart_detaillist.size();
        }
        last = detail_total - depart_detail;
        if (last > 0) {
            check = true;
        }
        return check;
    }

    // 保存发车单
    public void saveDepartOrder() {
        String depart_id = getPara("depart_id");// 发车单id
        String charge_type = getPara("chargeType");// 供应商计费类型
        String sp_id = getPara("sp_id");// 供应商id
        // 查找创建人id
        String name = (String) currentUser.getPrincipal();
        UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
        String creat_id = users.get("id").toString();// 创建人id
        String driver_id = getPara("driver_id");// 司机id
        String carinfoId = getPara("carinfoId");// 司机id
        String car_follow_name = getPara("car_follow_name");// 跟车人
        String car_follow_phone = getPara("car_follow_phone");// 跟车人电话
        Date createDate = Calendar.getInstance().getTime();
        String checkedDetail = getPara("checkedDetail");
        String uncheckedDetailIds = getPara("uncheckedDetail");
        DepartOrder dp = null;
        if ("".equals(depart_id)) {
            dp = new DepartOrder();
            dp.set("charge_type", charge_type).set("create_by", getPara("create_by")).set("create_stamp", createDate)
                    .set("combine_type", DepartOrder.COMBINE_TYPE_DEPART).set("depart_no", getPara("order_no"))
                    .set("remark", getPara("remark")).set("car_follow_name", getPara("car_follow_name"))
                    .set("car_follow_phone", getPara("car_follow_phone")).set("route_from", getPara("route_from"))
                    .set("route_to", getPara("route_to")).set("status", getPara("status"));
            if (!"".equals(driver_id) && driver_id != null) {
                dp.set("driver_id", driver_id);
            }
            if (!"".equals(carinfoId) && carinfoId != null) {
                dp.set("carinfo_id", carinfoId);
            }
            String partySpId = getPara("partySpId");
            if ("".equals(sp_id)) {
                if (!"".equals(partySpId)) {
                    dp.set("sp_id", partySpId);
                }
            } else {
                dp.set("sp_id", sp_id);
            }
            if (!"".equals(car_follow_name)) {
                dp.set("car_follow_name", car_follow_name);
            }
            if (!"".equals(car_follow_phone)) {
                dp.set("car_follow_phone", car_follow_phone);
            }
            dp.save();
            saveDepartTransfer(dp, getPara("orderid"), checkedDetail, uncheckedDetailIds);
            saveDepartOrderMilestone(dp);
            if (!"".equals(partySpId)) {
                updateTransferOrderSp(dp);
            }
        } else {
            dp = DepartOrder.dao.findById(Integer.parseInt(depart_id));
            dp.set("charge_type", charge_type).set("create_by", getPara("create_by")).set("create_stamp", createDate)
                    .set("combine_type", DepartOrder.COMBINE_TYPE_DEPART).set("depart_no", getPara("order_no"))
                    .set("remark", getPara("remark")).set("car_follow_name", getPara("car_follow_name"))
                    .set("car_follow_phone", getPara("car_follow_phone")).set("route_from", getPara("route_from"))
                    .set("route_to", getPara("route_to")).set("status", getPara("status"));
            if (!"".equals(driver_id) && driver_id != null) {
                dp.set("driver_id", driver_id);
            }
            if (!"".equals(carinfoId) && carinfoId != null) {
                dp.set("carinfo_id", carinfoId);
            }
            if (!"".equals(sp_id)) {
                dp.set("sp_id", Integer.parseInt(sp_id));
            }
            if (!"".equals(car_follow_name)) {
                dp.set("car_follow_name", car_follow_name);
            }
            if (!"".equals(car_follow_phone)) {
                dp.set("car_follow_phone", car_follow_phone);
            }
            dp.update();
            updateDepartTransfer(dp, getPara("orderid"), checkedDetail, uncheckedDetailIds);
            if (!"".equals(sp_id)) {
                updateTransferOrderSp(dp);
            }
        }
        renderJson(dp);
    }

    // 更新运输单的供应商
    private void updateTransferOrderSp(DepartOrder dp) {
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                "select * from depart_transfer where depart_id = ?", dp.get("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
            transferOrder.set("sp_id", dp.get("sp_id"));
            transferOrder.update();
        }
    }

    // 保存发车里程碑
    private void saveDepartOrderMilestone(DepartOrder pickupOrder) {
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        transferOrderMilestone.set("status", "新建");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
        transferOrderMilestone.set("depart_id", pickupOrder.get("id"));
        transferOrderMilestone.save();
    }

    // 更新中间表
    private void updateDepartTransfer(DepartOrder pickupOrder, String orderId, String checkedDetail,
            String uncheckedDetailId) {
        if (checkedDetail != null && !"".equals(checkedDetail)) {
            String[] checkedDetailIds = checkedDetail.split(",");
            TransferOrderItemDetail transferOrderItemDetail = null;
            for (int j = 0; j < checkedDetailIds.length && checkedDetailIds.length > 0; j++) {
                transferOrderItemDetail = TransferOrderItemDetail.dao.findById(checkedDetailIds[j]);
                transferOrderItemDetail.set("depart_id", pickupOrder.get("id"));
                transferOrderItemDetail.update();
            }
            TransferOrder transferOrder = TransferOrder.dao.findById(transferOrderItemDetail.get("order_id"));
            transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
            transferOrder.update();
        }
        String[] uncheckedDetailIds = uncheckedDetailId.split(",");
        if (uncheckedDetailId != null && !"".equals(uncheckedDetailId)) {
            TransferOrderItemDetail transferOrderItemDetail = null;
            for (int j = 0; j < uncheckedDetailIds.length && uncheckedDetailIds.length > 0; j++) {
                transferOrderItemDetail = TransferOrderItemDetail.dao.findById(uncheckedDetailIds[j]);
                transferOrderItemDetail.set("depart_id", null);
                transferOrderItemDetail.update();
            }
        }
        if (uncheckedDetailIds.length == 0 || "".equals(uncheckedDetailIds[0])) {
            List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
                    .find("select * from transfer_order_item_detail where order_id in(" + orderId + ")");
            String str = "";
            for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
                Long departId = transferOrderItemDetail.get("depart_id");
                if (departId == null || "".equals(departId)) {
                    str += departId;
                }
            }
            if ("".equals(str)) {
                List<TransferOrder> transferOrders = TransferOrder.dao
                        .find("select * from transfer_order where id in (" + orderId + ")");
                for (TransferOrder transferOrder : transferOrders) {
                    transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
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
                transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
                transferOrder.update();
                departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
                departTransferOrder.save();

                List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find(
                        "select * from transfer_order_item_detail where order_id = ?", params[i]);
                for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
                    if (transferOrderItemDetail.get("depart_id") == null) {
                        transferOrderItemDetail.set("depart_id", pickupOrder.get("id"));
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
                transferOrder.update();
                departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
                departTransferOrder.save();
            }
            String[] checkedDetailIds = checkedDetail.split(",");
            for (int j = 0; j < checkedDetailIds.length; j++) {
                TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
                        .findById(checkedDetailIds[j]);
                transferOrderItemDetail.set("depart_id", pickupOrder.get("id"));
                transferOrderItemDetail.update();
            }

            String[] uncheckedDetailIds = uncheckedDetailId.split(",");
            for (int j = 0; j < uncheckedDetailIds.length; j++) {
                TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
                        .findById(uncheckedDetailIds[j]);
                transferOrderItemDetail.set("depart_id", "");
                transferOrderItemDetail.update();
            }
        }
    }

    // 修改发车单状态
    public void updatestate() {
        String depart_id = getPara("depart_id");// 发车单id
        String order_state = getPara("order_state");// 状态
        int nummber = 0;// 没入库的货品数量

        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");

        DepartOrder departOrder = DepartOrder.dao.findById(Integer.parseInt(depart_id));
        departOrder.set("status", order_state).update();
        if ("已入库".equals(order_state)) {
            productInWarehouse(depart_id);// 产品入库
        }
        if ("已发车".equals(order_state)) {
            List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                    "select * from depart_transfer where depart_id = ?", depart_id);
            for (DepartTransferOrder departTransferOrder : departTransferOrders) {
                TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
                transferOrder.set("status", "已发车");
                transferOrder.update();

                TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
                transferOrderMilestone.set("status", "已发车");
                transferOrderMilestone.set("create_by", users.get(0).get("id"));
                transferOrderMilestone.set("location", "");
                java.util.Date utilDate2 = new java.util.Date();
                java.sql.Timestamp sqlDate2 = new java.sql.Timestamp(utilDate2.getTime());
                transferOrderMilestone.set("create_stamp", sqlDate2);
                transferOrderMilestone.set("order_id", transferOrder.get("id"));
                transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
                transferOrderMilestone.save();
            }
            
            // 生成应付, （如果已经有了应付，就要清除掉旧数据重新算）
            // 计件/整车/零担 生成发车单中供应商的应付，要算 item 的数量 * 合同中定义的价格
            // Depart_Order_fin_item 提货单/发车单应付明细表
            // 第一步：看发车单调度选择的计费方式是哪种：计件，整车，零担
            // 第二步：循环所选运输单中的 item, 到合同中（循环）比对去算钱。
            

            List<DepartTransferOrder> dItem = DepartTransferOrder.dao
                    .find("select order_id from depart_transfer where depart_id =" + depart_id + "");
            
            String transferIds = "";
            for (DepartTransferOrder dItem2 : dItem) {
                transferIds += dItem2.get("order_id") + ",";
            }
            if(transferIds.length()>0)
                transferIds = transferIds.substring(0, transferIds.length() - 1);

            List<Record> transferOrderItemList = Db
                    .find("select toi.*, t_o.route_from, t_o.route_to from transfer_order_item toi left join transfer_order t_o on toi.order_id = t_o.id where toi.order_id in("
                            + transferIds + ") order by pickup_seq desc");
            // 生成应付
            calcCost(users, departOrder, transferOrderItemList);

        }
        
        if ("已签收".equals(order_state)) {
            // 生成回单
            Date createDate = Calendar.getInstance().getTime();
            String orderNo = creatOrderNo();
            ReturnOrder returnOrder = new ReturnOrder();
            returnOrder.set("order_no", orderNo);
            returnOrder.set("depart_order_id", depart_id);
            // returnOrder.set("customer_id", deliveryOrder.get("customer_id"));
            // returnOrder.set("notity_party_id",
            // deliveryOrder.get("notity_party_id"));
            returnOrder.set("order_type", "应收");
            returnOrder.set("transaction_status", "新建");
            returnOrder.set("creator", users.get(0).get("id"));
            returnOrder.set("create_date", createDate);
            returnOrder.save();

        }

        savePickupOrderMilestone(departOrder, order_state);
        Map Map = new HashMap();
        Map.put("amount", nummber);
        Map.put("depart", departOrder);
        renderJson(Map);
    }

    // TODO 发车单应付计费  先获取有效期内的合同，条目越具体优先级越高
    // 级别1：计费类别 + 货  品 + 始发地 + 目的地
    // 级别2：计费类别 + 货  品 + 目的地
    // 级别3：计费类别 + 始发地 + 目的地
    // 级别4：计费类别 + 目的地
    
    // priceType 不分大小写在mysql会有问题
    private void calcCost(List<UserLogin> users, DepartOrder departOrder, List<Record> transferOrderItemList) {
        Long spId=departOrder.getLong("sp_id");
        if ( spId== null)
            return;
        
        //先获取有效期内的sp合同, 如有多个，默认取第一个
        Contract spContract= Contract.dao.findFirst("select * from contract where type='SERVICE_PROVIDER' " +
        		"and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="+spId);
        if(spContract==null)
            return;
        
        String chargeType = departOrder.get("charge_type");
        if("perUnit".equals(chargeType)){
            chargeType="计件";
        }else if("perCar".equals(chargeType)){
            chargeType="整车";
        }else if("perCargo".equals(chargeType)){
            chargeType="零担";
        }
        
        if ( spId!= null) {
            for (Record tOrderItemRecord : transferOrderItemList) {
                
                Record contractFinItem = Db
                        .findFirst("select amount from contract_item where contract_id ="+spContract.getLong("id")
                                +" and from_id = '"
                                + tOrderItemRecord.get("route_from")
                                + "' and priceType='"+chargeType+"'");
                if (contractFinItem != null) {//级别3
                    genFinItem(users, departOrder, tOrderItemRecord, contractFinItem);
                }else{//级别4
                    contractFinItem = Db
                            .findFirst("select amount from contract_item where contract_id ="+spContract.getLong("id")
                                    +" and to_id = '"+ tOrderItemRecord.get("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    if (contractFinItem != null) 
                        genFinItem(users, departOrder, tOrderItemRecord, contractFinItem);
                }
            }
        }
    }

    private void genFinItem( List<UserLogin> users, DepartOrder departOrder, Record tOrderItemRecord,
            Record contractFinItem) {
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
        DepartOrderFinItem pickupFinItem = new DepartOrderFinItem();
        pickupFinItem.set("fin_item_id", "1");
        pickupFinItem.set("amount",
                contractFinItem.getDouble("amount") * tOrderItemRecord.getDouble("amount"));
        pickupFinItem.set("depart_order_id", departOrder.getLong("id"));
        pickupFinItem.set("status", "未完成");
        pickupFinItem.set("creator", users.get(0).get("id"));
        pickupFinItem.set("create_date", now);
        pickupFinItem.save();
    }

    // 构造单号
    public static String creatOrderNo() {
        String order_no = null;
        String the_order_no = null;
        ReturnOrder order = ReturnOrder.dao.findFirst("select * from return_order " + " order by id desc limit 0,1");
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
                order_no = String.valueOf((oldTime + 1));
            } else {
                order_no = String.valueOf(newTime);
            }
            the_order_no = "HD" + order_no;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            order_no = format + "00001";
            the_order_no = "HD" + order_no;
        }
        return the_order_no;
    }

    // 点击货品table的查看 ，显示对应货品的单品
    public void itemDetailList() {
        String item_id = getPara("item_id");
        String depart_id = getPara("depart_id");// 发车单id
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from transfer_order_item_detail  where item_id =" + item_id + "";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> itemdetail = null;
        if ("".equals(depart_id)) {
            String sql_tr_itemdetail_id = "select id as itemdetail_id  from transfer_order_item_detail where item_id="
                    + item_id;
            String sql_dp_itemdetail_id = "select itemdetail_id  from depart_transfer_itemdetail  where item_id="
                    + item_id;
            List<Record> sql_tr_itemdetail_idlist = Db.find(sql_tr_itemdetail_id);
            List<Record> sql_dp_itemdetail_idlist = Db.find(sql_dp_itemdetail_id);
            /* List<Record> depart_itemdetail=Db.find(sql_item); */
            sql_tr_itemdetail_idlist.removeAll(sql_dp_itemdetail_idlist);
            String detail_id = "0";
            if (sql_tr_itemdetail_idlist.size() > 0) {
                for (int i = 0; i < sql_tr_itemdetail_idlist.size(); i++) {
                    detail_id += sql_tr_itemdetail_idlist.get(i).get("itemdetail_id") + ",";
                }
                detail_id = detail_id.substring(0, detail_id.length() - 1);
            }
            String sql = "select * from transfer_order_item_detail  where ID in(" + detail_id + ")";
            itemdetail = Db.find(sql);
        } else {
            String sql = "select * from transfer_order_item_detail  where item_id in(" + item_id + ")";
            itemdetail = Db.find(sql);
        }

        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", itemdetail);
        renderJson(Map);
    }

    // 发车单删除
    public void cancel() {
        String id = getPara();
        String sql = "select * from depart_transfer  where depart_id =" + id + "";
        List<DepartTransferOrder> re_tr = DepartTransferOrder.dao.find(sql);
        for (int i = 0; i < re_tr.size(); i++) {
            DepartTransferOrder dep_tr = DepartTransferOrder.dao.findFirst(sql);
            dep_tr.set("depart_id", null).update();
            dep_tr.delete();
        }
        DepartOrder re = DepartOrder.dao.findById(id);
        re.set("driver_id", null).update();
        re.delete();
        render("departOrder/departOrderList.html");
    }

    // 保存发车里程碑
    private TransferOrderMilestone savePickupOrderMilestone(DepartOrder departOrder, String status) {
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        if (status == null) {
            transferOrderMilestone.set("status", "新建");
        } else {
            transferOrderMilestone.set("status", status);
            transferOrderstatus(departOrder.get("id").toString(), status, null);
        }
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
        transferOrderMilestone.set("depart_id", departOrder.get("id"));
        transferOrderMilestone.save();
        return transferOrderMilestone;
    }

    // 单击tab里程碑
    public void transferOrderMilestoneList() {
        Map<String, List> map = new HashMap<String, List>();
        List<String> usernames = new ArrayList<String>();
        String departOrderId = getPara("departOrderId");
        if (departOrderId == "" || departOrderId == null) {
            departOrderId = "-1";
        }
        if (!"-1".equals(departOrderId)) {
            List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                    .find("select * from transfer_order_milestone where type = '"
                            + TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE + "'and depart_id=" + departOrderId);
            for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = userLogin.get("user_name");
                usernames.add(username);
            }
            map.put("transferOrderMilestones", transferOrderMilestones);
            map.put("usernames", usernames);
        }
        renderJson(map);
    }

    // 编辑里程碑
    public void saveTransferOrderMilestone() {
        String milestoneDepartId = getPara("milestoneDepartId");
        Map<String, Object> map = new HashMap<String, Object>();
        if (milestoneDepartId != null && !"".equals(milestoneDepartId)) {
            DepartOrder departOrder = DepartOrder.dao.findById(milestoneDepartId);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            String status = getPara("status");
            String location = getPara("location");
            transferOrderstatus(milestoneDepartId, status, location);
            if (!status.isEmpty()) {
                transferOrderMilestone.set("status", status);
                departOrder.set("status", status);
            } else {
                transferOrderMilestone.set("status", "在途");
                departOrder.set("status", "在途");
            }
            departOrder.update();
            if (!location.isEmpty()) {
                transferOrderMilestone.set("location", location);
            } else {
                transferOrderMilestone.set("location", "");
            }
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");

            transferOrderMilestone.set("create_by", users.get(0).get("id"));

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("depart_id", milestoneDepartId);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
            transferOrderMilestone.save();

            map.put("transferOrderMilestone", transferOrderMilestone);
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("user_name");
            map.put("username", username);
        }
        renderJson(map);
    }

    // 同步运输单状态里程碑
    public void transferOrderstatus(String de_or, String status, String location) {
        int depart_id = Integer.parseInt(de_or);
        List<DepartTransferOrder> dep = DepartTransferOrder.dao
                .find("select * from depart_transfer  where depart_id in(" + depart_id + ")");
        for (int i = 0; i < dep.size(); i++) {
            int order_id = Integer.parseInt(dep.get(i).get("order_id").toString());
            TransferOrder tr = TransferOrder.dao.findById(order_id);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            if (!"".equals(status) && status != null) {
                if (tr.get("depart_assign_status") == TransferOrder.ASSIGN_STATUS_PARTIAL) {
                    transferOrderMilestone.set("status", "部分" + status);
                    tr.set("status", "部分" + status);
                } else {
                    transferOrderMilestone.set("status", status);
                    tr.set("status", status);
                }
            } else {
                if (tr.get("depart_assign_status") == TransferOrder.ASSIGN_STATUS_PARTIAL) {
                    transferOrderMilestone.set("status", "部分在途");
                    tr.set("status", "部分在途");
                } else {
                    transferOrderMilestone.set("status", "在途");
                    tr.set("status", "在途");
                }
            }
            tr.update();
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
            transferOrderMilestone.set("create_by", users.get(0).get("id"));
            if (location == null || location.isEmpty()) {
                transferOrderMilestone.set("location", "");
            } else {
                transferOrderMilestone.set("location", location);
            }

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.set("order_id", order_id);
            transferOrderMilestone.save();

        }

    }

    // 在途运输单管理
    public void transferMilestoneindex() {
        if (LoginUserController.isAuthenticated(this))
            render("departOrder/TransferOrderStatus.html");
    }

    public void transferMilestone() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from transfer_order_milestone trom "
                + "left join transfer_order tor on tor.id=trom.order_id "
                + "left join user_login  us on us.id=trom.create_by " + "where trom.status='在途' and trom. type='"
                + TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE + "'";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));
        List<TransferOrderMilestone> transferOrderMilestone = TransferOrderMilestone.dao
                .find("select trom.*,tor.order_no as order_no,us.user_name as usernames from transfer_order_milestone trom "
                        + "left join transfer_order tor on tor.id=trom.order_id "
                        + "left join user_login  us on us.id=trom.create_by "
                        + "where trom.status='在途' and trom. type='"
                        + TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE + "'" + sLimit);

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", transferOrderMilestone);

        renderJson(orderMap);
    }

    // 回显供应商
    public void getIntsp() {
        Contact co = Contact.dao
                .findFirst("select * from contact  where id in(select contact_id  from party p where p.id="
                        + Integer.parseInt(getPara("sp_id").toString()) + " )");
        renderJson(co);
    }

    // 修改运输单仓库
    public void updateWarehouse(String depart_id, String house_id) {
        int de_id = Integer.parseInt(depart_id);
    }

    // 外包运输单更新
    public void TransferonTrip() {
        if (LoginUserController.isAuthenticated(this))
            render("departOrder/transferOrderOnTripList.html");
    }

    public void transferonTriplist() {
        Map transferOrderListMap = null;
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String address = getPara("address");
        String customer = getPara("customer");
        String sp = getPara("sp");
        String officeName = getPara("officeName");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");

        // String strWhere = DataTablesUtils.buildSingleFilter(this);
        if (orderNo == null && status == null && address == null && customer == null && sp == null && beginTime == null
                && endTime == null) {
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }

            String sqlTotal = "select count(1) total from transfer_order t where t.status!='取消' and operation_type ='out_source'";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select t.*,c1.abbr cname,c2.abbr spname,t.create_stamp,o.office_name oname from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join office o on t.office_id = o.id where t.status!='取消' and operation_type ='out_source'  order by create_stamp desc"
                    + sLimit;

            List<Record> transferOrders = Db.find(sql);

            transferOrderListMap = new HashMap();
            transferOrderListMap.put("sEcho", pageIndex);
            transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
            transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

            transferOrderListMap.put("aaData", transferOrders);
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }

            String sqlTotal = "select count(1) total from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id"
                    + " left join office o on t.office_id = o.id where t.status!='取消' and operation_type ='out_source' and t.order_no like '%"
                    + orderNo + "%' and t.status like '%" + status + "%' and t.address like '%" + address
                    + "%' and c1.abbr like '%" + customer + "%' and ifnull(c2.abbr,'') like '%" + sp
                    + "%' and o.office_name  like '%" + officeName + "%' and create_stamp between '" + beginTime
                    + "' and '" + endTime + "'";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select t.*,c1.abbr cname,c2.abbr spname,o.office_name oname from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id"
                    + " left join office o on t.office_id = o.id where t.status!='取消' and operation_type ='out_source' and t.order_no like '%"
                    + orderNo + "%' and t.status like '%" + status + "%' and t.address like '%" + address
                    + "%' and c1.abbr like '%" + customer + "%' and ifnull(c2.abbr,'') like '%" + sp
                    + "%' and o.office_name  like '%" + officeName + "%' and create_stamp between '" + beginTime
                    + "' and '" + endTime + "' order by create_stamp desc" + sLimit;

            List<Record> transferOrders = Db.find(sql);

            transferOrderListMap = new HashMap();
            transferOrderListMap.put("sEcho", pageIndex);
            transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
            transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

            transferOrderListMap.put("aaData", transferOrders);
        }
        renderJson(transferOrderListMap);
    }

    // 外包运输单里程碑
    public void transferMilestoneList() {
        Map<String, List> map = new HashMap<String, List>();
        List<String> usernames = new ArrayList<String>();
        String order_id = getPara("order_id");
        if (order_id == "" || order_id == null) {
            order_id = "-1";
        }
        String pickupOrderId = getPara("pickupOrderId");
        if (pickupOrderId == "" || pickupOrderId == null) {
            pickupOrderId = "-1";
        }
        if (order_id != "-1" && pickupOrderId == "-1") {
            List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                    .find("select * from transfer_order_milestone where type = '"
                            + TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE + "' and order_id=" + order_id);
            for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = userLogin.get("user_name");
                usernames.add(username);
            }
            map.put("transferOrderMilestones", transferOrderMilestones);
            map.put("usernames", usernames);
        } else {
            List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                    .find("select * from transfer_order_milestone where type = '"
                            + TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE + "' and pickup_id=" + pickupOrderId);
            for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = userLogin.get("user_name");
                usernames.add(username);
            }
            map.put("transferOrderMilestones", transferOrderMilestones);
            map.put("usernames", usernames);
        }
        renderJson(map);
    }

    // 保存外包运输单里程碑
    public void saveTransferMilestone() {
        String milestoneDepartId = getPara("milestoneDepartId");
        Map<String, Object> map = new HashMap<String, Object>();
        if (milestoneDepartId != null && !"".equals(milestoneDepartId)) {
            TransferOrder tr = TransferOrder.dao.findById(milestoneDepartId);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            String status = getPara("status");
            String location = getPara("location");
            transferOrderstatus(milestoneDepartId, status, location);
            if (!status.isEmpty()) {
                transferOrderMilestone.set("status", status);
                tr.set("status", status);
            } else {
                transferOrderMilestone.set("status", "在途");
                tr.set("status", "在途");
            }
            tr.update();
            if (!location.isEmpty()) {
                transferOrderMilestone.set("location", location);
            } else {
                transferOrderMilestone.set("location", "");
            }
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");

            transferOrderMilestone.set("create_by", users.get(0).get("id"));

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("order_id", milestoneDepartId);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.save();

            map.put("transferOrderMilestone", transferOrderMilestone);
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("user_name");
            map.put("username", username);
        }
        renderJson(map);
    }

    // 初始化收货人信息
    public void ginNotifyPerson() {
        int id = 0;
        String[] order_id = getPara("order_id").split(",");
        if (order_id.length == 1) {
            id = Integer.parseInt(getPara("order_id").toString());
            String Sql = "select co.* from contact  co " + "left join transfer_order tro  on tro.id= " + id
                    + "left join party p on p.id=tro.notify_party_id " + "where co.id=p.contact_id";
            Contact co = Contact.dao.findFirst(Sql);
            renderJson(co);
        }

    }

    // 回显司机信息
    public void ginDriver() {
        String id = getPara("depart_id");
        if (id != null && id != "") {
            DepartOrder de = DepartOrder.dao.findById(Integer.parseInt(id));
            Long d_id = de.get("driver_id");
            Carinfo car = Carinfo.dao.findById(d_id);
            renderJson(car);
        }
    }

    // 从发车单货品计算货品数量
    public double productAmount(String depart_id, String item_id) {
        int de_id = Integer.parseInt(depart_id);
        int it_id = Integer.parseInt(item_id);
        double amount = 0;
        String sql = "select * from depart_transfer_itemdetail where depart_id =" + de_id + " and item_id=" + it_id
                + "";
        List<Record> de_item_amount = Db.find(sql);
        if (de_item_amount.size() == 1) {
            if (de_item_amount.get(0).get("itemdetail_id") == null) {
                TransferOrderItem tr_item = TransferOrderItem.dao.findById(it_id);
                amount = Double.parseDouble(tr_item.get("amount").toString());
            } else {
                amount = de_item_amount.size();
            }
        } else if (de_item_amount.size() > 1) {
            amount = de_item_amount.size();
        }
        return amount;

    }

    // 产品入库
    public void productInWarehouse(String departId) {
        if (!"".equals(departId) && departId != null) {
            String orderIds = "";
            List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                    "select * from depart_transfer where depart_id = ?", departId);
            for (DepartTransferOrder departTransferOrder : departTransferOrders) {
                orderIds += departTransferOrder.get("order_id") + ",";
            }
            orderIds = orderIds.substring(0, orderIds.length() - 1);
            List<TransferOrder> transferOrders = TransferOrder.dao.find("select * from transfer_order where id in("
                    + orderIds + ")");
            for (TransferOrder transferOrder : transferOrders) {
                InventoryItem inventoryItem = null;
                List<TransferOrderItem> transferOrderItems = TransferOrderItem.dao.find(
                        "select * from transfer_order_item where order_id = ?", transferOrder.get("id"));
                for (TransferOrderItem transferOrderItem : transferOrderItems) {
                    if (transferOrderItem != null) {
                        if (transferOrderItem.get("product_id") != null) {
                            String inventoryItemSql = "select * from inventory_item where product_id = "
                                    + transferOrderItem.get("product_id") + " and warehouse_id = "
                                    + transferOrder.get("warehouse_id");
                            inventoryItem = InventoryItem.dao.findFirst(inventoryItemSql);
                            String sqlTotal = "select count(1) total from transfer_order_item_detail where depart_id = "
                                    + departId + " and order_id = " + transferOrder.get("id");
                            Record rec = Db.findFirst(sqlTotal);
                            Long amount = rec.getLong("total");
                            if (inventoryItem == null) {
                                inventoryItem = new InventoryItem();
                                inventoryItem.set("party_id", transferOrder.get("customer_id"));
                                inventoryItem.set("warehouse_id", transferOrder.get("warehouse_id"));
                                inventoryItem.set("product_id", transferOrderItem.get("product_id"));
                                inventoryItem.set("total_quantity", amount);
                                inventoryItem.save();
                            } else {
                                inventoryItem.set("total_quantity",
                                        Double.parseDouble(inventoryItem.get("total_quantity").toString()) + amount);
                                inventoryItem.update();
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * public void CreatReturnOrder() { boolean check =
     * CreatReturnOrder.CreatOrder(ReturnOrder.Depart_Order,
     * getPara("depart_id").toString()); renderJson(check); }
     */

    // 应付list
    public void accountPayable() {
        String id = getPara();
        if (id == null || id.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from transfer_order_fin_item where depart_id = '" + id + "'";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select d.*,f.name,f.remark from depart_order_fin_item d "
                + "left join fin_item f on d.fin_item_id = f.id " + "where d.depart_order_id ='" + id
                + "' and f.type='应付'");

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

    public void addNewRow() {
        String pickupOrderId = getPara();
        Fin_item fItem = new Fin_item();
        DepartOrderFinItem dFinItem = new DepartOrderFinItem();
        fItem.set("type", "应付");
        fItem.save();
        dFinItem.set("fin_item_id", fItem.get("id")).set("status", "新建").set("depart_order_id", pickupOrderId);
        dFinItem.save();
        renderJson("{\"success\":true}");
    }

    // 添加应付
    public void paymentSave() {
        String returnValue = "";
        String id = getPara("id");
        String finItemId = getPara("finItemId");
        DepartOrderFinItem dFinItem = DepartOrderFinItem.dao.findById(id);

        Fin_item fItem = Fin_item.dao.findById(dFinItem.get("fin_item_id"));

        String amount = getPara("amount");

        String username = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + username + "'");
        Date createDate = Calendar.getInstance().getTime();

        if (!"".equals(finItemId) && finItemId != null) {
            dFinItem.set("fin_item_id", finItemId).update();
            returnValue = finItemId;
        } else if (!"".equals(amount) && amount != null) {
            dFinItem.set("amount", amount).update();
            returnValue = amount;
        }
        List<Record> list = Db.find("select * from fin_item");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get("name") == null) {
                Fin_item.dao.deleteById(list.get(i).get("id"));
                List<Record> list2 = Db.find("select * from depart_order_fin_item where fin_item_id ='"
                        + list.get(i).get("id") + "'");
                List<Record> list3 = Db.find("select * from fin_item where id ='" + list2.get(0).get("fin_item_id")
                        + "'");
                if (list3.size() == 0) {
                    TransferOrderFinItem.dao.deleteById(list2.get(0).get("id"));
                }
            }
        }
        renderText(returnValue);
    }

    public void getPaymentList() {
        // String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        locationList = Db.find("select * from fin_item where type='应付'");
        renderJson(locationList);
    }    

    // 点击货品table的查看 ，显示货品对应的单品
    public void findAllItemDetail() {
        String itemId = getPara("item_id");
        String departId = getPara("departId");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String category = getPara("category");
        String sqlTotal = "select count(1) total from transfer_order_item_detail tod where tod.item_id = " + itemId
                + " and tod.depart_id = " + departId;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from transfer_order_item_detail tod where tod.item_id = " + itemId
                + " and tod.depart_id = " + departId;

        List<Record> details = Db.find(sql);

        Map productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        productListMap.put("aaData", details);
        renderJson(productListMap);
    }

}
