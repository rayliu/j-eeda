package controllers.yh.inventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.Party;
import models.Warehouse;
import models.WarehouseOrder;

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
                render("inventory/gateIn.html");
        }
        if (url.equals("/yh/gateOut")) {
            if (LoginUserController.isAuthenticated(this))
                render("inventory/gateOut.html");
        }
    }

    public void gateIn_add() {
        render("/yh/inventory/gateInEdit.html");
    }

    public void gateOut_add() {
        render("/yh/inventory/gateOutEdit.html");
    }

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
        WarehouseOrder warehouseOrder = new WarehouseOrder();
        String gateInId = getPara("gateInId");
        if (gateInId != "") {

        } else {

        }
        renderJson(0);
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

    public void searchAllwarehouse() {
        List<Warehouse> warehouses = Warehouse.dao
                .find("select * from warehouse");
        renderJson(warehouses);
    }
}
