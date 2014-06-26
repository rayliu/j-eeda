package controllers.yh.delivery;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.Party;
import models.TransferOrder;
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

public class DeliveryController extends Controller {
    private Logger logger = Logger.getLogger(DeliveryController.class);
    // in config route已经将路径默认设置为/yh
    // me.add("/yh", controllers.yh.AppController.class, "/yh");
    Subject currentUser = SecurityUtils.getSubject();

    private boolean isAuthenticated() {
        if (!currentUser.isAuthenticated()) {
            if (LoginUserController.isAuthenticated(this))
                redirect("/yh/login");
            return false;
        }
        setAttr("userId", currentUser.getPrincipal());
        return true;
    }

    public void index() {
        if (!isAuthenticated())
            return;
        if (LoginUserController.isAuthenticated(this))
            render("/yh/delivery/deliveryOrderList.html");
    }

    public void deliveryList() {
        String orderNo_filter = getPara("orderNo_filter");
        String transfer_filter = getPara("transfer_filter");
        String status_filter = getPara("status_filter");
        String customer_filter = getPara("customer_filter");
        String sp_filter = getPara("sp_filter");
        String beginTime_filter = getPara("beginTime_filter");
        String endTime_filter = getPara("endTime_filter");
        String warehouse = getPara("warehouse");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map transferOrderListMap = new HashMap();
        if (orderNo_filter == null && transfer_filter == null && status_filter == null && customer_filter == null && sp_filter == null
                && beginTime_filter == null && endTime_filter == null) {
            String sqlTotal = "select count(1) total from delivery_order";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select d.*,c.company_name as customer,c2.company_name as c2,w.warehouse_name ,(select group_concat(doi.transfer_no separator '\r\n') FROM delivery_order_item doi where delivery_id = d.id) as TRANSFER_ORDER_NO  FROM delivery_order d "
                    + "left join party p on d.customer_id = p.id left join contact c on p.contact_id = c.id "
                    + "left join party p2 on d.sp_id = p2.id join contact c2 on p2.contact_id = c2.id "
                    + "left join transfer_order t on d.transfer_order_id = t.id "
                    + "left join warehouse w on t.warehouse_id = w.id order by d.CREATE_STAMP desc";
            List<Record> transferOrders = Db.find(sql);

            transferOrderListMap.put("sEcho", pageIndex);
            transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
            transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            transferOrderListMap.put("aaData", transferOrders);
        } else {
            if (beginTime_filter == null || "".equals(beginTime_filter)) {
                beginTime_filter = "1-1-1";
            }
            if (endTime_filter == null || "".equals(endTime_filter)) {
                endTime_filter = "9999-12-31";
            }

            String sqlTotal = "select count(1) total from delivery_order";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select d.*,c.company_name as customer,c2.company_name as c2,t.order_no as transfer_order_no,w.warehouse_name from delivery_order d "
                    + "left join party p on d.customer_id = p.id "
                    + "left join contact c on p.contact_id = c.id "
                    + "left join party p2 on d.sp_id = p2.id "
                    + "left join contact c2 on p2.contact_id = c2.id "
                    + "left join transfer_order t on d.transfer_order_id = t.id "
                    + "left join warehouse w on t.warehouse_id = w.id "
                    + "where d.ORDER_NO like '%"
                    + orderNo_filter
                    + "%' and  t.order_no like '%"
                    + transfer_filter
                    + "%' and d.STATUS like '%"
                    + status_filter
                    + "%' and c.company_name like '%"
                    + customer_filter
                    + "%' and w.warehouse_name like '%"
                    + warehouse
                    + "%' and c2.company_name like'%"
                    + sp_filter
                    + "%' and d.CREATE_STAMP between '" + beginTime_filter + "' and '" + endTime_filter + "' ";

            List<Record> transferOrders = Db.find(sql);

            transferOrderListMap.put("sEcho", pageIndex);
            transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
            transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            transferOrderListMap.put("aaData", transferOrders);
        }
        // 获取总条数

        renderJson(transferOrderListMap);
    }

    // 构造单号
    public String creat_order_no() {
        String order_no = null;
        String the_order_no = null;
        DeliveryOrder order = DeliveryOrder.dao.findFirst("select * from delivery_order order by order_no desc limit 0,1");
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
            the_order_no = "PS" + order_no;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            order_no = format + "00001";
            the_order_no = "PS" + order_no;
        }
        return the_order_no;
    }

    public void add() {
        setAttr("saveOK", false);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/delivery/deliveryOrderSearchTransfer.html");
    }

    public void edit() {
        String id = getPara();

        DeliveryOrder tOrder = DeliveryOrder.dao.findById(id);
        List<Record> serIdList = Db.find("select transfer_order_id from delivery_order_item where delivery_id =" + id);
        try {
            serIdList.get(0).get("transfer_order_id");
        } catch (Exception e) {
            System.out.println(e);
        }
        if (serIdList.size() != 0) {
            // 序列号id
            String idStr = "";
            for (Record record : serIdList) {
                idStr += record.get("transfer_order_id") + ",";
            }// 4,5,6
            idStr = idStr.substring(0, idStr.length() - 1);
            setAttr("localArr2", idStr);
            // 序列号运输单id数组
            List<Record> transferIdList = Db.find("select transfer_order_id from delivery_order_item where delivery_id =" + id);
            String idStr2 = "";
            for (Record record : transferIdList) {
                idStr2 += record.get("transfer_order_id") + ",";
            }// 4,5,6
            idStr2 = idStr2.substring(0, idStr2.length() - 1);
            setAttr("localArr", idStr2);
        } else {
            // 运输单id
            List<Record> transferId = Db.find("select transfer_order_id from delivery_order_item where delivery_id =" + id);
            // TODO: 如果transferId==null, 下面这句报错！！！
            String transferId2 = transferId.get(0).get("transfer_order_id").toString();
            setAttr("transferId", transferId2);
        }
        // 运输单信息
        TransferOrder transferOrder = TransferOrder.dao.findById(tOrder.get("transfer_order_id"));
        // 客户信息
        // Party customerContact = Party.dao
        // .findFirst("select *,p.id as customerId from party p,contact c where p.id ='"
        // + tOrder.get("customer_id")
        // + "'and p.contact_id = c.id");
        // 供应商信息
        Party spContact = Party.dao.findFirst("select *,p.id as spId from party p,contact c where p.id ='" + tOrder.get("sp_id")
                + "'and p.contact_id = c.id");

        // List<Record> deliveryIdList = Db
        // .find("select TRANSFER_ORDER_ID from DELIVERY_ORDER where id ="
        // + id);
        // String dd =
        // deliveryIdList.get(0).get("TRANSFER_ORDER_ID").toString();

        // 收货人信息
        Contact notifyPartyContact = null;
        if (tOrder.get("notify_party_id") != null) {
            notifyPartyContact = (Contact) Contact.dao.findFirst(
                    "select *,p.id as pid,c.id as contactId from party p, contact c where p.contact_id=c.id and p.id =?",
                    tOrder.get("notify_party_id"));
        }
        setAttr("deliveryId", tOrder);
        // setAttr("customer", customerContact);
        setAttr("deliveryOrder", transferOrder);
        setAttr("notifyParty", notifyPartyContact);
        setAttr("spContact", spContact);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/delivery/deliveryOrderEdit.html");

    }

    // 配送单客户
    public void selectCustomer() {
        List<Contact> customer = Contact.dao
                .find("select * from contact where id in(select contact_id from party where id in(SELECT customer_id  FROM transfer_order group by customer_id)) and id='1'");
        renderJson(customer);
    }

    /*
     * // 供应商列表,列出最近使用的5个客户 public void selectSp() { List<Contact> contactjson =
     * Contact.dao .find(
     * "select * from contact c  where id in (select contact_id from party where party_type='SERVICE_PROVIDER' order by last_update_date desc limit 0,5)"
     * ); renderJson(contactjson); }
     */
    public void creat2() {
        String id = getPara();

        // String ser = getPara("ser");
        /*
         * String[] ser; ser = getParaValues("ser");
         */

        /*
         * if (!"".equals(ser)) { List<DeliveryOrderItem> itemsId =
         * DeliveryOrderItem.dao
         * .find("select id from delivery_ORDER_ITEM where SERIAL_NO='" + ser +
         * "'"); DeliveryOrderItem itemsIds = itemsId.get(0);
         * setAttr("itemsIds", itemsIds); }
         */
        /*
         * if (id != null) { List<Contact> customers = Contact.dao .find(
         * "select *,p.id as customerId from contact c,party p,TRANSFER_ORDER t where p.contact_id=c.id and t.customer_id = p.id and t.id ="
         * + id + ""); Contact customer = customers.get(0); setAttr("customer",
         * customer); }
         */
        TransferOrder tOrder = TransferOrder.dao.findById(id);
        // setAttr("ser", ser);

        setAttr("transferId", id);
        setAttr("deliveryOrder", tOrder);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/delivery/deliveryOrderEdit.html");
    }

    // 创建配送单普通货品
    public void creat() {
        // customer, sp, notify_party
        String id = getPara();
        if (LoginUserController.isAuthenticated(this))
            // render("/yh/delivery/deliveryOrderEdit.html");
            renderJson(id);
        // renderJson("{\"success\":true}");
    }

    // 创建运输单ATM机
    public void creat3() {
        String list = this.getPara("localArr");
        String list2 = this.getPara("localArr2");
        String list3 = this.getPara("localArr3");
        System.out.println(list);
        setAttr("localArr", list);
        setAttr("localArr2", list2);
        setAttr("localArr3", list3);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/delivery/deliveryOrderEdit.html");
    }

    // 创建 结构 行为
    public void deliveryOrderList() {

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from user_login ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));
        // 获取当前页的数据
        List<Record> orders = Db.find("select * from user_login");
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // 获取运输单普通货品
    public void SearchTransfer() {
        String deliveryOrderNo = getPara("deliveryOrderNo1");
        String customerName = getPara("customerName1");
        String orderStatue = getPara("orderStatue1");
        String warehouse = getPara("warehouse1");

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map transferOrderListMap = new HashMap();
        if (deliveryOrderNo == null && customerName == null && orderStatue == null && warehouse == null) {
            String sqlTotal = "select count(1) total from transfer_order t " + "left join warehouse w on t.warehouse_id = w.id "
                    + "where t.STATUS='已入库' and t.CARGO_NATURE='cargo'";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select t.*,w.warehouse_name,c.company_name from transfer_order t "
                    + "left join warehouse w on t.warehouse_id = w.id " + "left join party p on t.customer_id = p.id "
                    + "left join contact c  on p.contact_id = c.id " + "where t.STATUS='已入库' and t.CARGO_NATURE='cargo'";
            List<Record> transferOrders = Db.find(sql);

            transferOrderListMap.put("sEcho", pageIndex);
            transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
            transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            transferOrderListMap.put("aaData", transferOrders);
        } else {
            String sqlTotal = "select count(1) total from transfer_order t " + "left join warehouse w on t.warehouse_id = w.id "
                    + "where t.STATUS='已入库' and t.CARGO_NATURE='cargo'";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select t.*,w.warehouse_name,c.company_name from transfer_order t "
                    + "left join warehouse w on t.warehouse_id = w.id " + "left join party p on t.customer_id = p.id "
                    + "left join contact c  on p.contact_id = c.id " + "where t.STATUS='已入库' and t.CARGO_NATURE='cargo' "
                    + "and t.order_no like '%" + deliveryOrderNo + "%'" + "and w.warehouse_name like '%" + warehouse + "%'"
                    + "and c.company_name like '%" + customerName + "%'" + "and t.STATUS like '%" + orderStatue + "%'";
            List<Record> transferOrders = Db.find(sql);

            transferOrderListMap.put("sEcho", pageIndex);
            transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
            transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            transferOrderListMap.put("aaData", transferOrders);
        }
        renderJson(transferOrderListMap);
    }

    // 获取运输单ATM序列号
    public void SearchTransfer2() {
        String deliveryOrderNo = getPara("deliveryOrderNo");
        String customerName = getPara("customerName");
        String orderStatue = getPara("orderStatue");
        String warehouse = getPara("warehouse");

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map transferOrderListMap = new HashMap();
        if (deliveryOrderNo == null && customerName == null && orderStatue == null && warehouse == null) {
            String sqlTotal = "select count(1) total FROM transfer_order_item_detail t1 "
                    + "left join transfer_order t2 on t1.order_id=t2.id " + "left join warehouse w on t2.warehouse_id = w.id "
                    + "where t2.STATUS='已入库' and t2.CARGO_NATURE='ATM'";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "SELECT  t1.seRIAL_NO,t1.id as tid,t2.*,w.wAREHOUSE_NAME,c.company_name FROM transfer_order_item_detail t1 "
                    + "left join transfer_order t2 on t1.order_id=t2.id " + "left join warehouse w on t2.warehouse_id = w.id "
                    + "left join party p on t2.customer_id = p.id " + "left join contact c on p.contact_id = c.id "
                    + "where t2.STATUS='已入库' and t2.CARGO_NATURE='ATM'";
            List<Record> transferOrders = Db.find(sql);

            transferOrderListMap.put("sEcho", pageIndex);
            transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
            transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            transferOrderListMap.put("aaData", transferOrders);
        } else {
            String sqlTotal = "select count(1) total FROM transfer_order_item_detail t1 "
                    + "left join transfer_order t2 on t1.order_id=t2.id " + "left join warehouse w on t2.warehouse_id = w.id "
                    + "where t2.STATUS='已入库' and t2.CARGO_NATURE='ATM'";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "SELECT  t1.seRIAL_NO ,t2.*,w.wAREHOUSE_NAME,c.company_name FROM transfer_order_item_detail t1 "
                    + "left join transfer_order t2 on t1.order_id=t2.id " + "left join warehouse w on t2.warehouse_id = w.id "
                    + "left join party p on t2.customer_id = p.id " + "left join contact c on p.contact_id = c.id "
                    + "where t2.STATUS='已入库' and t2.CARGO_NATURE='ATM'" + "and t2.order_no like '%" + deliveryOrderNo + "%'"
                    + "and w.warehouse_name like '%" + warehouse + "%'" + "and c.company_name like '%" + customerName + "%'"
                    + "and t2.STATUS like '%" + orderStatue + "%'";

            List<Record> transferOrders = Db.find(sql);

            transferOrderListMap.put("sEcho", pageIndex);
            transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
            transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            transferOrderListMap.put("aaData", transferOrders);
        }

        renderJson(transferOrderListMap);
    }

    public void cancel() {
        String id = getPara();
        System.out.println(id);
        DeliveryOrder.dao.findById(id).set("Status", "取消").update();
        renderJson("{\"success\":true}");
    }

    // 查找供应商
    public void searchSp() {
        String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {

            locationList = Db
                    .find("select *,p.id as pid from contact c,party p where p.contact_id= c.id and p.party_type ='SERVICE_PROVIDER' and (c.company_name like '%"
                            + input
                            + "%' or c.contact_person like '%"
                            + input
                            + "%' or c.email like '%"
                            + input
                            + "%' or c.mobile like '%"
                            + input
                            + "%' or c.phone like '%"
                            + input
                            + "%' or c.address like '%"
                            + input
                            + "%' or c.postal_code like '%"
                            + input + "%') limit 0,10");
        }
        renderJson(locationList);
    }

    public void orderList() {
        String idlist = getPara("localArr");
        String idlist2 = getPara("localArr2");
        System.out.println(idlist2);
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from transfer_order_item tof" + " left join transfer_order t_o on tof.ORDER_ID =t_o.id "
                + " left join CONTACT c on c.id in (select contact_id from party p where t_o.customer_id=p.id)" + " where tof.ORDER_ID in("
                + idlist + ")";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        String sql = "";
        List<Record> departOrderitem = null;
        if (idlist2 == null || idlist2.equals("null")) {
            sql = "SELECT tof.* ,t_o.ORDER_NO as order_no,c.COMPANY_NAME as customer  FROM transfer_order_item tof "
                    + " left join transfer_order t_o on tof.ORDER_ID =t_o.id "
                    + "left join CONTACT c on c.id in (select contact_id from party p where t_o.customer_id=p.id) "
                    + " where tof.ORDER_ID in(" + idlist + ")  order by c.id" + sLimit;
            departOrderitem = Db.find(sql);
            for (int i = 0; i < departOrderitem.size(); i++) {
                String itemname = departOrderitem.get(i).get("item_name");
                if ("ATM".equals(itemname)) {
                    Long itemid = departOrderitem.get(i).get("id");
                    String sql2 = "SELECT SERIAL_NO  FROM transfer_order_item_detail  where ITEM_ID =" + itemid;
                    List<Record> itemserial_no = Db.find(sql2);
                    String itemno = itemserial_no.get(0).get("SERIAL_NO");
                    departOrderitem.get(i).set("serial_no", itemno);
                } else {
                    departOrderitem.get(i).set("serial_no", "无");
                }
            }
        } else {
            sql = "SELECT tof.* ,t_o.ORDER_NO as order_no,c.COMPANY_NAME as customer,toid.serial_no as serial_no FROM transfer_order_item tof "
                    + " left join transfer_order  t_o  on tof.ORDER_ID =t_o.id "
                    + "left join CONTACT c on c.id in (select contact_id from party p where t_o.customer_id=p.id) "
                    + "left join transfer_order_item_detail toid on toid.ITEM_ID =tof.id " + " where toid.id in(" + idlist2 + ")" + sLimit;
            departOrderitem = Db.find(sql);
        }

        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));

        Map.put("aaData", departOrderitem);

        renderJson(Map);
    }

    public void deliverySave() {
        String orderNo = creat_order_no();// 构造发车单号
        String deliveryid = getPara("delivery_id");
        DeliveryOrder deliveryOrder = null;
        String notifyId = getPara("notify_id");
        // String itemId = getPara("item_id");
        String idlist3 = getPara("localArr");
        System.out.println(idlist3);
        String[] idlist = getPara("localArr").split(",");
        String[] idlist2 = getPara("localArr2").split(",");
        String[] idlist4 = getPara("localArr3").split(",");

        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");

        Date createDate = Calendar.getInstance().getTime();

        Party party = new Party();
        Contact contact = new Contact();
        deliveryOrder = new DeliveryOrder();

        if (notifyId == null || notifyId.equals("")) {
            contact.set("company_name", getPara("notify_company_name")).set("contact_person", getPara("notify_contact_person"))
                    .set("address", getPara("notify_address")).set("phone", getPara("notify_phone"));
            contact.save();
            party.set("contact_id", contact.get("id")).set("party_type", "NOTIFY_PARTY").set("create_date", createDate)
                    .set("creator", users.get(0).get("id"));
            party.save();
        } else {
            contact.set("id", getPara("contact_id")).set("company_name", getPara("notify_company_name"))
                    .set("contact_person", getPara("notify_contact_person")).set("address", getPara("notify_address"))
                    .set("phone", getPara("notify_phone")).update();
        }

        if (deliveryid == null || "".equals(deliveryid)) {

            deliveryOrder
                    .set("Order_no", orderNo)
                    // .set("Transfer_order_id", getPara("tranferid"))
                    // .set("Customer_id", getPara("customer_id"))
                    .set("Sp_id", getPara("cid")).set("Notify_party_id", party.get("id")).set("CREATE_STAMP", createDate)
                    .set("Status", "新建");
            deliveryOrder.save();

            if (!idlist3.equals("")) {
                for (int i = 0; i < idlist.length; i++) {
                    DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
                    deliveryOrderItem.set("DELIVERY_ID", deliveryOrder.get("id")).set("transfer_order_id", idlist[i]);
                    deliveryOrderItem.set("TRANSFER_ITEM_ID", idlist2[i]);
                    deliveryOrderItem.set("transfer_no", idlist4[i]);
                    deliveryOrderItem.save();
                }
            } else {
                DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
                deliveryOrderItem.set("DELIVERY_ID", deliveryOrder.get("id")).set("transfer_order_id", getPara("tranferid"));
                deliveryOrderItem.save();
            }
            saveDeliveryOrderMilestone(deliveryOrder);
        } else {

            deliveryOrder.set("Sp_id", getPara("cid")).set("Notify_party_id", getPara("notify_id"))
                    .set("Customer_id", getPara("customer_id")).set("id", deliveryid);
            deliveryOrder.update();
        }
        renderJson(deliveryOrder);
    }

    // 保存运输里程碑
    private void saveDeliveryOrderMilestone(DeliveryOrder transferOrder) {
        DeliveryOrderMilestone deliveryOrderMilestone = new DeliveryOrderMilestone();
        deliveryOrderMilestone.set("status", "新建");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        deliveryOrderMilestone.set("create_by", users.get(0).get("id"));
        deliveryOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        deliveryOrderMilestone.set("create_stamp", sqlDate);
        deliveryOrderMilestone.set("delivery_id", transferOrder.get("id"));
        deliveryOrderMilestone.save();
    }

    /*
     * // 运输单ATM序列号 public void serialNo() { String id = getPara("id");
     * System.out.println(id); List<Record> transferOrders = Db .find(
     * "SELECT serial_no  FROM transfer_order_item_detail where item_name = 'ATM' and ORDER_ID  = "
     * + id); renderJson(transferOrders); }
     */

    // 发车确认
    public void departureConfirmation() {
        Long delivery_id = Long.parseLong(getPara("deliveryid"));
        System.out.println(delivery_id);
        DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(delivery_id);
        deliveryOrder.set("status", "配送在途");
        deliveryOrder.update();

        Map<String, Object> map = new HashMap<String, Object>();
        DeliveryOrderMilestone deliveryOrderMilestone = new DeliveryOrderMilestone();
        deliveryOrderMilestone.set("status", "已发车");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        deliveryOrderMilestone.set("create_by", users.get(0).get("id"));
        deliveryOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        deliveryOrderMilestone.set("create_stamp", sqlDate);
        deliveryOrderMilestone.set("order_id", getPara("order_id"));
        deliveryOrderMilestone.save();
        map.put("transferOrderMilestone", deliveryOrderMilestone);
        UserLogin userLogin = UserLogin.dao.findById(deliveryOrderMilestone.get("create_by"));
        String username = userLogin.get("user_name");
        map.put("username", username);
        renderJson(map);
    }
}
