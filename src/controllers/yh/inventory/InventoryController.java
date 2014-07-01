package controllers.yh.inventory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
            if (LoginUserController.isAuthenticated(this))
                setAttr("inventory", "gateIn");
            render("inventory/inventoryList.html");
        }
        if (url.equals("/yh/gateOut")) {
            if (LoginUserController.isAuthenticated(this))
                setAttr("inventory", "gateOut");
            render("inventory/inventoryList.html");
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

    // 入库单添加
    public void gateIn_add() {
        if (LoginUserController.isAuthenticated(this))
            render("/yh/inventory/gateInEdit.html");
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
        List<Record> orders = Db
                .find("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                        + "left join party p on p.id =w_o.party_id "
                        + "left join contact c on p.contact_id =c.id "
                        + "left join warehouse w on w.id = w_o.warehouse_id where w_o.id ='"
                        + id + "'");
        setAttr("warehouseOrder", orders);
        WarehouseOrder warehouseOrder = WarehouseOrder.dao.findById(id);
        render("/yh/inventory/gateInEdit.html");
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
        String sLimit = "";
        Map productListMap = null;
        String categoryId = getPara("categoryId");
        if (categoryId == null || "".equals(categoryId)) {
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null
                    && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                        + getPara("iDisplayLength");
            }

            String category = getPara("category");
            String sqlTotal = "select count(1) total from product";
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select * from product";

            List<Record> products = Db.find(sql);

            productListMap = new HashMap();
            productListMap.put("sEcho", pageIndex);
            productListMap.put("iTotalRecords", rec.getLong("total"));
            productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            productListMap.put("aaData", products);
        } else {
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null
                    && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                        + getPara("iDisplayLength");
            }

            String category = getPara("category");
            String sqlTotal = "select count(1) total from product where category_id = "
                    + categoryId;
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select * from product where category_id = "
                    + categoryId;
            List<Record> products = Db.find(sql);
            productListMap = new HashMap();
            productListMap.put("sEcho", pageIndex);
            productListMap.put("iTotalRecords", rec.getLong("total"));
            productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
            productListMap.put("aaData", products);
        }
        renderJson(productListMap);
    }

    // 保存入库单
    public void gateInSave() {
        String orderNo = creat_order_no();// 构造发车单号
        WarehouseOrder warehouseOrder = new WarehouseOrder();
        String gateInId = getPara("gateInId");

        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao
                .find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        warehouseOrder.set("party_id", getPara("party_id"))
                .set("warehouse_id", getPara("warehouseId"))
                .set("order_no", orderNo).set("order_type", "入库")
                .set("status", "新建").set("qualifier", getPara("qualifier"))
                .set("remark", getPara("remark"));

        if (gateInId != "") {

            warehouseOrder.set("last_updater", users.get(0).get("id"))
                    .set("last_update_date", createDate)
                    .set("id", getAttr("gateInId"));
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
        WarehouseOrderItem warehouseOrderItem = new WarehouseOrderItem();
        String warehouseOderItemId = getPara("warehouseOderItemId");
        if (warehouseOderItemId != "") {
            warehouseOrderItem.set("", getPara());
        } else {

        }
    }

    // 构造单号
    public String creat_order_no() {
        String order_no = null;
        String the_order_no = null;
        WarehouseOrder order = WarehouseOrder.dao
                .findFirst("select * from warehouse_order order by order_no desc limit 0,1");
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

    // 查找仓库
    public void searchAllwarehouse() {
        List<Warehouse> warehouses = Warehouse.dao
                .find("select * from warehouse");
        renderJson(warehouses);
    }
}
