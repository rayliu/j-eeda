package controllers.yh.inventory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.InventoryItem;
import models.Party;
import models.UserLogin;
import models.Warehouse;
import models.WarehouseOrder;
import models.WarehouseOrderItem;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class InventoryController extends Controller {

    private Logger logger = Logger.getLogger(InventoryController.class);
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
        HttpServletRequest re = getRequest();
        String url = re.getRequestURI();
        logger.debug("URI:" + url);
        if (url.equals("/yh/gateIn")) {

            setAttr("inventory", "gateIn");
            if (LoginUserController.isAuthenticated(this))
                render("inventory/inventoryList.html");
        }
        if (url.equals("/yh/gateOut")) {

            setAttr("inventory", "gateOut");
            if (LoginUserController.isAuthenticated(this))
                render("inventory/inventoryList.html");
        }
        if (url.equals("/yh/stock")) {
            if (LoginUserController.isAuthenticated(this))
                render("inventory/stock.html");
        }
    }

    // 入库单list
    public void gateInlist() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from warehouse_order";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                        + "left join party p on p.id =w_o.party_id "
                        + "left join contact c on p.contact_id =c.id "
                        + "left join warehouse w on w.id = w_o.warehouse_id where order_type='入库'");
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // 出库单list
    public void gateOutlist() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from warehouse_order";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                        + "left join party p on p.id =w_o.party_id "
                        + "left join contact c on p.contact_id =c.id "
                        + "left join warehouse w on w.id = w_o.warehouse_id where order_type='出库'");
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // 库存list
    public void stocklist() {
        String id = getPara();
        if (id == null) {
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
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from warehouse_order_item";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));
        // 获取当前页的数据
        List<Record> orders = Db
                .find("select * from inventory_item i_t where warehouse_id ="
                        + id);
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    // 入库单添加
    public void gateIn_add() {
        if (LoginUserController.isAuthenticated(this))
            render("/yh/inventory/gateInEdit.html");
    }

    // 入库单产品删除
    public void gateInProductDelect() {
        String id = getPara();
        if (id != null) {
            WarehouseOrderItem.dao.deleteById(id);
        }
        renderJson("{\"success\":true}");
    }

    // 入库单产品编辑
    public void gateInProductEdit() {
        String id = getPara();
        System.out.println(id);
        WarehouseOrderItem orders = WarehouseOrderItem.dao
                .findFirst("select * from warehouse_order_item where id='" + id
                        + "'");
        renderJson(orders);
    }

    // 出库单添加
    public void gateOut_add() {
        if (LoginUserController.isAuthenticated(this))
            render("/yh/inventory/gateOutEdit.html");
    }

    // 入库单修改edit
    public void gateInEdit() {
        String id = getPara();
        System.out.println(id);
        WarehouseOrder orders = WarehouseOrder.dao
                .findFirst("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                        + "left join party p on p.id =w_o.party_id "
                        + "left join contact c on p.contact_id =c.id "
                        + "left join warehouse w on w.id = w_o.warehouse_id where w_o.id='"
                        + id + "'");
        setAttr("warehouseOrder", orders);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/inventory/gateInEdit.html");
    }

    // 出库单修改edit
    public void gateOutEdit() {
        String id = getPara();
        System.out.println(id);
        WarehouseOrder orders = WarehouseOrder.dao
                .findFirst("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                        + "left join party p on p.id =w_o.party_id "
                        + "left join contact c on p.contact_id =c.id "
                        + "left join warehouse w on w.id = w_o.warehouse_id where w_o.id='"
                        + id + "'");
        setAttr("warehouseOrder", orders);
        if (LoginUserController.isAuthenticated(this))
            render("/yh/inventory/gateOutEdit.html");
    }

    // 查找客户
    public void searchCustomer() {
        String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER' and (company_name like '%"
                            + input
                            + "%' or contact_person like '%"
                            + input
                            + "%' or email like '%"
                            + input
                            + "%' or mobile like '%"
                            + input
                            + "%' or phone like '%"
                            + input
                            + "%' or address like '%"
                            + input
                            + "%' or postal_code like '%"
                            + input
                            + "%') limit 0,10");
        } else {
            locationList = Db
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
                            + Party.PARTY_TYPE_CUSTOMER + "'");
        }
        renderJson(locationList);
    }

    // 入库产品list
    public void gateInProductlist() {
        String warehouseorderid = getPara();
        System.out.println(warehouseorderid);
        if (warehouseorderid == null) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        Map productListMap = null;
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        String category = getPara("category");
        String sqlTotal = "select count(1) total from warehouse_order_item where warehouse_order_id = "
                + warehouseorderid;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from warehouse_order_item where warehouse_order_id = "
                + warehouseorderid;
        List<Record> products = Db.find(sql);
        productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
        productListMap.put("aaData", products);

        renderJson(productListMap);
    }

    // 出库产品list
    public void gateInProductlist2() {
        String warehouseorderid = getPara();
        System.out.println(warehouseorderid);
        if (warehouseorderid == null) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        Map productListMap = null;
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        String category = getPara("category");
        String sqlTotal = "select count(1) total from warehouse_order_item where warehouse_order_id = "
                + warehouseorderid;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from warehouse_order_item where warehouse_order_id = "
                + warehouseorderid;
        List<Record> products = Db.find(sql);
        productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
        productListMap.put("aaData", products);

        renderJson(productListMap);
    }

    // 保存入库单
    public void gateInSave() {
        String orderNo = creat_order_no();// 构造发车单号
        WarehouseOrder warehouseOrder = new WarehouseOrder();
        String gateInId = getPara("warehouseorderId");
        System.out.println();
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao
                .find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        warehouseOrder.set("party_id", getPara("party_id"))
                .set("warehouse_id", getPara("warehouseId"))
                .set("order_type", "入库").set("status", "新建")
                .set("qualifier", getPara("qualifier"))
                .set("remark", getPara("remark"));

        if (gateInId != "") {
            warehouseOrder.set("id", gateInId)
                    .set("last_updater", users.get(0).get("id"))
                    .set("last_update_date", createDate);
            warehouseOrder.update();
        } else {
            warehouseOrder.set("creator", users.get(0).get("id"))
                    .set("create_date", createDate).set("order_no", orderNo);
            warehouseOrder.save();
        }
        renderJson(warehouseOrder.get("id"));
    }

    // 保存入库单
    public void gateOutSave() {
        String orderNo = creat_order_no2();// 构造发车单号
        WarehouseOrder warehouseOrder = new WarehouseOrder();
        String gateOutId = getPara("warehouseorderId");
        System.out.println();
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao
                .find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        warehouseOrder.set("party_id", getPara("party_id"))
                .set("warehouse_id", getPara("warehouseId"))
                .set("order_no", orderNo).set("order_type", "出库")
                .set("status", "新建").set("qualifier", getPara("qualifier"))
                .set("remark", getPara("remark"));

        if (gateOutId != "") {
            warehouseOrder.set("id", gateOutId)
                    .set("last_updater", users.get(0).get("id"))
                    .set("last_update_date", createDate);
            warehouseOrder.update();
        } else {
            warehouseOrder.set("creator", users.get(0).get("id")).set(
                    "create_date", createDate);
            warehouseOrder.save();
        }
        renderJson(warehouseOrder.get("id"));
    }

    // 保存入库单货品
    public void savewareOrderItem() {
        String warehouseorderid = getPara();
        WarehouseOrderItem warehouseOrderItem = null;
        String productId = getPara("productId");
        String warehouseOrderItemId = getPara("warehouseOderItemId");
        System.out.println(productId);
        /*
         * if (productId.equals("")) { renderJson(0); return; }
         */
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao
                .find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        if (warehouseOrderItemId != "") {
            warehouseOrderItem = WarehouseOrderItem.dao
                    .findById(warehouseOrderItemId);
            setwarehouseItem(warehouseOrderItem);
            warehouseOrderItem.set("warehouse_order_id", warehouseorderid);
            warehouseOrderItem.set("last_updater", users.get(0).get("id")).set(
                    "last_update_date", createDate);
            warehouseOrderItem.update();
        } else {
            warehouseOrderItem = new WarehouseOrderItem();
            setwarehouseItem(warehouseOrderItem);
            warehouseOrderItem.set("warehouse_order_id", warehouseorderid);
            warehouseOrderItem.set("creator", users.get(0).get("id")).set(
                    "create_date", createDate);
            warehouseOrderItem.save();
        }
        renderJson(warehouseOrderItem.get("id"));
    }

    public void setwarehouseItem(WarehouseOrderItem warehouseOrderItem) {
        warehouseOrderItem
                .set("product_id", getPara("productId"))
                .set("item_name", getPara("item_name"))
                .set("item_no", getPara("itemNoMessage"))
                // .set("expire_date", getPara("expire_date"))
                .set("lot_no", getPara("lot_no")).set("uom", getPara("uom"))
                .set("caton_no", getPara("caton_no"))
                .set("total_quantity", getPara("total_quantity"))
                .set("unit_price", getPara("unit_price"))
                .set("unit_cost", getPara("unit_cost"))
                .set("item_desc", getPara("item_desc"));
    }

    // 构造入库单号
    public String creat_order_no() {
        String order_no = null;
        String the_order_no = null;
        WarehouseOrder order = WarehouseOrder.dao
                .findFirst("select * from warehouse_order where order_type ='入库' order by order_no desc limit 0,1");
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
            the_order_no = "RK" + order_no;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            order_no = format + "00001";
            the_order_no = "RK" + order_no;
        }
        return the_order_no;
    }

    // 构造出库单号
    public String creat_order_no2() {
        String order_no = null;
        String the_order_no = null;
        WarehouseOrder order = WarehouseOrder.dao
                .findFirst("select * from warehouse_order where order_type ='出库' order by order_no desc limit 0,1");
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
            the_order_no = "CK" + order_no;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            order_no = format + "00001";
            the_order_no = "CK" + order_no;
        }
        return the_order_no;
    }

    // 查找序列号
    public void searchItemNo() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select * from product where category_id in (select id from category where customer_id = "
                            + customerId
                            + ") and item_no like '%"
                            + input
                            + "%' limit 0,10");
        } else {
            locationList = Db
                    .find("select * from product where category_id in (select id from category where customer_id = "
                            + customerId + ")");
        }
        renderJson(locationList);
    }

    // 查找产品名
    public void searchItemName() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select * from product where category_id in (select id from category where customer_id = "
                            + customerId
                            + ") and item_name like '%"
                            + input
                            + "%' limit 0,10");
        } else {
            locationList = Db
                    .find("select * from product where category_id in (select id from category where customer_id = "
                            + customerId + ")");
        }
        renderJson(locationList);
    }

    // gateOut查找序列号2
    public void searchNo2() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select * from inventory_item where party_id='"
                            + customerId + "' and item_no like '%" + input
                            + "%' limit 0,10");
        } else {
            locationList = Db
                    .find("select * from inventory_item where party_id='"
                            + customerId + "'");
        }
        renderJson(locationList);
    }

    // gateOut查找产品名2
    public void searchName2() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select * from inventory_item where party_id='"
                            + customerId + "' and item_name like '%" + input
                            + "%' limit 0,10");
        } else {
            locationList = Db
                    .find("select * from inventory_item where party_id='"
                            + customerId + "'");
        }
        renderJson(locationList);
    }

    // 查找仓库
    public void searchAllwarehouse() {
        String input = getPara("input");
        List<Warehouse> warehouses = Warehouse.dao
                .find("select * from warehouse");
        renderJson(warehouses);
    }

    // 查找客户
    public void searchgateOutCustomer() {
        String input = getPara("input");
        String warehouseId = getPara("warehouseId");
        if (warehouseId.equals("")) {
            return;
        }
        List<Record> locationList = Collections.EMPTY_LIST;
        locationList = Db
                .find("SELECT w_o.party_id as pid,c.company_name FROM `warehouse_order` w_o "
                        + "left join party p on p.id = w_o.party_id "
                        + "left join contact c on p.contact_id = c.id where w_o.warehouse_id ='"
                        + warehouseId
                        + "' and w_o.order_type='入库' and c.company_name like '%"
                        + input + "%' group by c.company_name");
        renderJson(locationList);
    }

    // 入仓确认
    public void gateInConfirm() {
        String id = getPara();

        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao
                .find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        WarehouseOrder warehouseOrder = WarehouseOrder.dao.findById(id);
        warehouseOrder.set("status", "已入库");
        // warehouseOrder.update();
        List<Record> list = Db
                .find("select * from warehouse_order_item where warehouse_order_id = '"
                        + id + "'");

        // 获取已入库的库存
        List<Record> inverntory = Db
                .find("select * from inventory_item where item_no in(select item_no from warehouse_order_item where warehouse_order_id = '"
                        + id + "')");

        // 入库库存添加
        for (int i = 0; i < inverntory.size(); i++) {
            if (inverntory.size() > 0) {
                if (list.get(i).get("item_no")
                        .equals(inverntory.get(i).get("item_no"))) {
                    InventoryItem inventoryItem = InventoryItem.dao
                            .findById(inverntory.get(i).get("id"));
                    inventoryItem.set(
                            "total_quantity",
                            Double.parseDouble(inverntory.get(i)
                                    .get("total_quantity").toString())
                                    + Double.parseDouble(list.get(i)
                                            .get("total_quantity").toString()));
                    inventoryItem.update();
                }
            }
        }
        List<Record> list2 = Db
                .find("select item_no from warehouse_order_item where warehouse_order_id = '"
                        + id + "'");
        List<Record> inverntory2 = Db
                .find("select item_no from inventory_item where item_no in(select item_no from warehouse_order_item where warehouse_order_id = '"
                        + id + "')");
        list2.removeAll(inverntory2);

        for (int i = 0; i < list2.size(); i++) {
            List<Record> list3 = Db
                    .find("select * from warehouse_order_item where warehouse_order_id = '"
                            + id
                            + "' and item_no ='"
                            + list2.get(i).getStr("item_no") + "'");

            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem.set("party_id", warehouseOrder.get("party_id"))
                    .set("warehouse_id", warehouseOrder.get("warehouse_id"))
                    .set("product_id", list3.get(i).get("product_id"))
                    .set("item_name", list3.get(i).get("item_name"))
                    .set("item_no", list3.get(i).get("item_no"))
                    .set("uom", list3.get(i).get("uom"))
                    .set("lot_no", list3.get(i).get("lot_no"))
                    .set("total_quantity", list3.get(i).get("total_quantity"))
                    .set("unit_price", list3.get(i).get("unit_price"))
                    .set("unit_cost", list3.get(i).get("unit_cost"))
                    .set("caton_no", list3.get(i).get("caton_no"))
                    .set("creator", users.get(0).get("id"))
                    .set("create_date", createDate);
            inventoryItem.save();

        }
        renderJson("{\"success\":true}");
    }

    // 出仓确认
    public void gateOutConfirm() {
        String id = getPara();
        // 获取从表的货品数据
        List<Record> warehouseItem = Db
                .find("select * from warehouse_order_item where warehouse_order_id = '"
                        + id + "'");
        // 出库单完成出库
        WarehouseOrder warehouseOrder = WarehouseOrder.dao.findById(id);
        warehouseOrder.set("status", "已出库");
        warehouseOrder.update();

        // 获取已入库的库存
        List<Record> inverntory = Db
                .find("select * from inventory_item where item_no in(select item_no from warehouse_order_item where warehouse_order_id = '"
                        + id + "')");
        // 出库后更新数据
        for (int i = 0; i < warehouseItem.size(); i++) {
            InventoryItem inventoryItem = InventoryItem.dao.findById(inverntory
                    .get(i).get("id"));
            inventoryItem.set(
                    "total_quantity",
                    Double.parseDouble(inverntory.get(i).get("total_quantity")
                            .toString())
                            - Double.parseDouble(warehouseItem.get(i)
                                    .get("total_quantity").toString()));
            inventoryItem.update();
        }
        // 删除库存为0的数据
        List<Record> list = Db
                .find("select id,total_quantity from inventory_item where item_no in(select item_no from warehouse_order_item where warehouse_order_id = '"
                        + id + "')");
        for (int i = 0; i < list.size(); i++) {
            if (Double
                    .parseDouble(list.get(i).get("total_quantity").toString()) <= 0.0) {
                InventoryItem.dao.deleteById(list.get(i).get("id"));
            }
        }
        renderJson("{\"success\":true}");
    }

}
