package controllers.yh.inventory;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.InventoryItem;
import models.Office;
import models.Party;
import models.Product;
import models.TransferOrder;
import models.TransferOrderItem;
import models.UserLogin;
import models.Warehouse;
import models.WarehouseOrder;
import models.WarehouseOrderItem;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.OrderNoUtil;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class InventoryController extends Controller {

    private Logger logger = Logger.getLogger(InventoryController.class);
    Subject currentUser = SecurityUtils.getSubject();    
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INLIST})
    public void index() {
        setAttr("inventory", "gateIn");
        render("/yh/inventory/inventoryList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_II_LIST})
    public void stockIndex(){
        render("/yh/inventory/stock.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTLIST})
    public void outIndex(){
    	 setAttr("inventory", "gateOut");
         
         render("/yh/inventory/inventoryList.html");
    }

    // 入库单list
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INLIST})
    public void gateInlist() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from warehouse_order";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                + "left join party p on p.id =w_o.party_id " + "left join contact c on p.contact_id =c.id "
                + "left join warehouse w on w.id = w_o.warehouse_id where order_type='入库'");
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // 出库单list
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTLIST})
    public void gateOutlist() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from warehouse_order";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                + "left join party p on p.id =w_o.party_id " + "left join contact c on p.contact_id =c.id "
                + "left join warehouse w on w.id = w_o.warehouse_id where order_type='出库'");
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // 库存list
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_II_LIST})
    public void stocklist() {
        String customerId = getPara("customerId");
        String warehouseId = getPara("warehouseId");
        String officeId = getPara("offeceId");
        logger.debug("customerId:"+customerId+",warehouseId:"+warehouseId);
        if ((customerId == null && warehouseId == null && officeId == null) || ( "".equals(customerId) && "".equals(warehouseId) && "".equals(officeId))) {
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
            sLimit = " limit " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String sql = "select sum(total_quantity) total_quantity, company_name, item_name, item_no, unit, warehouse_name, office_name  from ("
        		+ "select i_t.total_quantity, c.company_name, p.item_name, p.item_no, p.unit,  "
        		+ "(select warehouse_name from warehouse where id = i_t.warehouse_id) warehouse_name, "
        		+ "(select office_name from office o left join warehouse w on o.id = w.office_id where w.id = i_t.warehouse_id) office_name "
        		+ " from inventory_item i_t  left join product p on  p.id =i_t.product_id  left join party p2 on i_t.party_id =p2.id  "
        		+ "left join contact c on p2.contact_id = c.id  left join warehouse w on w.id = i_t.warehouse_id "
        		+ "left join office o on o.id = w.office_id  where 1=1 ";
        
        if((customerId != null) && !"".equals(customerId)){
        	sql = sql + " and p2.id =" + customerId ;
        }
        
        if(warehouseId != null && !"".equals(warehouseId)){
        	sql = sql + " and w.id =" + warehouseId ;
        }
        
        if((officeId != null) && !"".equals(officeId)){
        	sql = sql + " and o.id =" + officeId ;
        }
        
        String groupSql = ") as A group by company_name, item_name, item_no, unit, warehouse_name, office_name ";
        
        String sqlTotal = "select count(1) total from (" + sql + groupSql + ") as B";// 获取总条数
        
        sql = sql + groupSql + sLimit;
        Record rec = Db.findFirst(sqlTotal);
        // 获取当前页的数据
        List<Record> orders = Db.find(sql);
        Map orderMap = new HashMap();
        if(rec == null ){
        	orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
        }else{
        	orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        }
        orderMap.put("sEcho", pageIndex);
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    // 入库单添加
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INCREATE})
    public void gateIn_add() {
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
        WarehouseOrderItem orders = WarehouseOrderItem.dao.findFirst("select * from warehouse_order_item where id='"
                + id + "'");
        renderJson(orders);
    }

    // 出库单添加
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTCREATE})
    public void gateOut_add() {
            render("/yh/inventory/gateOutEdit.html");
    }

    // 入库单修改edit
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INUPDATE})
    public void gateInEdit() {
        String id = getPara();
        System.out.println(id);
        WarehouseOrder orders = WarehouseOrder.dao
                .findFirst("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                        + "left join party p on p.id =w_o.party_id " + "left join contact c on p.contact_id =c.id "
                        + "left join warehouse w on w.id = w_o.warehouse_id where w_o.id='" + id + "'");
        setAttr("warehouseOrder", orders);
            render("/yh/inventory/gateInEdit.html");
    }

    // 出库单修改edit
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTUPDATE})
    public void gateOutEdit() {
        String id = getPara();
        System.out.println(id);
        WarehouseOrder orders = WarehouseOrder.dao
                .findFirst("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                        + "left join party p on p.id =w_o.party_id " + "left join contact c on p.contact_id =c.id "
                        + "left join warehouse w on w.id = w_o.warehouse_id where w_o.id='" + id + "'");
        setAttr("warehouseOrder", orders);
            render("/yh/inventory/gateOutEdit.html");
    }

    // 查找客户
    public void searchCustomer() {
        String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER' and (p.is_stop is null or p.is_stop = 0) and (company_name like '%"
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
                            + input + "%' or postal_code like '%" + input + "%')  limit 0,10");
        } else {
            locationList = Db
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
                            + Party.PARTY_TYPE_CUSTOMER + "' and (p.is_stop is null or p.is_stop = 0)");
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
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String category = getPara("category");
        String sqlTotal = "select count(1) total from warehouse_order_item where warehouse_order_id = "
                + warehouseorderid;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from warehouse_order_item where warehouse_order_id = " + warehouseorderid;
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
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String category = getPara("category");
        String sqlTotal = "select count(1) total from warehouse_order_item where warehouse_order_id = "
                + warehouseorderid;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from warehouse_order_item where warehouse_order_id = " + warehouseorderid;
        List<Record> products = Db.find(sql);
        productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
        productListMap.put("aaData", products);

        renderJson(productListMap);
    }

    // 保存入库单
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INCREATE,PermissionConstant.PERMISSION_WO_INUPDATE},logical=Logical.OR)
    public void gateInSave() {
        
    	String sql = "select * from warehouse_order where order_type ='入库' order by id desc limit 0,1";
        String orderNo = OrderNoUtil.getOrderNo(sql, "RK");
        
        WarehouseOrder warehouseOrder = new WarehouseOrder();
        String gateInId = getPara("warehouseorderId");
        System.out.println();
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        warehouseOrder.set("party_id", getPara("party_id")).set("warehouse_id", getPara("warehouseId"))
                .set("order_type", "入库").set("status", "新建").set("qualifier", getPara("qualifier"))
                .set("remark", getPara("remark"));

        if (gateInId != "") {
            warehouseOrder.set("id", gateInId).set("last_updater", users.get(0).get("id"))
                    .set("last_update_date", createDate);
            warehouseOrder.update();
        } else {
            warehouseOrder.set("creator", users.get(0).get("id")).set("create_date", createDate)
                    .set("order_no", orderNo);
            warehouseOrder.save();
        }
        renderJson(warehouseOrder.get("id"));
    }

    // 保存出库单
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTCREATE,PermissionConstant.PERMISSION_WO_OUTUPDATE},logical=Logical.OR)
    public void gateOutSave() {
    	String sql = "select * from warehouse_order where order_type ='出库' order by id desc limit 0,1";
        String orderNo = OrderNoUtil.getOrderNo(sql, "CK");
        
        WarehouseOrder warehouseOrder = new WarehouseOrder();
        String gateOutId = getPara("warehouseorderId");
        System.out.println();
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        warehouseOrder.set("party_id", getPara("party_id")).set("warehouse_id", getPara("warehouseId"))
                .set("order_no", orderNo).set("order_type", "出库").set("status", "新建")
                .set("qualifier", getPara("qualifier")).set("remark", getPara("remark"));

        if (gateOutId != "") {
            warehouseOrder.set("id", gateOutId).set("last_updater", users.get(0).get("id"))
                    .set("last_update_date", createDate);
            warehouseOrder.update();
        } else {
            warehouseOrder.set("creator", users.get(0).get("id")).set("create_date", createDate);
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
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        if (warehouseOrderItemId != "") {
            warehouseOrderItem = WarehouseOrderItem.dao.findById(warehouseOrderItemId);
            setwarehouseItem(warehouseOrderItem);
            warehouseOrderItem.set("warehouse_order_id", warehouseorderid);
            warehouseOrderItem.set("last_updater", users.get(0).get("id")).set("last_update_date", createDate);
            warehouseOrderItem.update();
        } else {
            warehouseOrderItem = new WarehouseOrderItem();
            setwarehouseItem(warehouseOrderItem);
            warehouseOrderItem.set("warehouse_order_id", warehouseorderid);
            warehouseOrderItem.set("creator", users.get(0).get("id")).set("create_date", createDate);
            warehouseOrderItem.save();
        }
        renderJson(warehouseOrderItem.get("id"));
    }

    public void setwarehouseItem(WarehouseOrderItem warehouseOrderItem) {
        warehouseOrderItem.set("product_id", getPara("productId"))
                .set("item_name", getPara("item_name"))
                .set("item_no", getPara("itemNoMessage"))
                // .set("expire_date", getPara("expire_date"))
                .set("lot_no", getPara("lot_no")).set("uom", getPara("uom")).set("caton_no", getPara("caton_no"))
                .set("total_quantity", getPara("total_quantity")).set("unit_price", getPara("unit_price"))
                .set("unit_cost", getPara("unit_cost")).set("item_desc", getPara("item_desc"));
    }

    // 查找序列号
    public void searchItemNo() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select * from product where category_id in (select id from category where customer_id = "
                            + customerId + ") and item_no like '%" + input + "%' limit 0,10");
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
                            + customerId + ") and item_name like '%" + input + "%' limit 0,10");
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
        String warehouseId = getPara("warehouseId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db.find("select * from inventory_item i " + "left join product p on p.id = i.product_id "
                    + "where i.party_id='" + customerId + "' and i.warehouse_id ='" + warehouseId
                    + "' p.item_name like '%" + input + "%' limit 0,10");
        } else {
            locationList = Db.find("select * from inventory_item i " + "left join product p on p.id = i.product_id "
                    + "where i.party_id='" + customerId + "' and i.warehouse_id ='" + warehouseId + "' ");
        }
        renderJson(locationList);
    }

    // gateOut查找产品名2
    public void searchName2() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        String warehouseId = getPara("warehouseId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db.find("select * from inventory_item i " + "left join product p on p.id = i.product_id "
                    + "where i.party_id='" + customerId + "' and i.warehouse_id ='" + warehouseId
                    + "' p.item_name like '%" + input + "%' limit 0,10");
        } else {
            locationList = Db.find("select * from inventory_item i " + "left join product p on p.id = i.product_id "
                    + "where i.party_id='" + customerId + "' and i.warehouse_id ='" + warehouseId + "' ");
        }
        renderJson(locationList);
    }

    // 查找仓库
    public void searchAllwarehouse() {
    	String inputStr = getPara("warehouseName");
    	String sql ="";
    	if(inputStr!=null){
    		sql = "select * from warehouse where warehouse_name like '%"+inputStr+"%'";
    	}else{
    		sql= "select * from warehouse";
    	}
        List<Warehouse> warehouses = Warehouse.dao.find(sql);
        renderJson(warehouses);
    }

    // 查找客户
    public void searchgateOutCustomer() {
        String input = getPara("input");
        String warehouseId = getPara("warehouseId");
        if (warehouseId.equals("")) {
        	renderJson();
            //return;
        }
        List<Record> locationList = Collections.EMPTY_LIST;
        locationList = Db.find("SELECT w_o.party_id as pid,c.company_name FROM `warehouse_order` w_o "
                + "left join party p on p.id = w_o.party_id "
                + "left join contact c on p.contact_id = c.id where w_o.warehouse_id ='" + warehouseId
                + "' and w_o.order_type='入库' and c.company_name like '%" + input + "%' group by c.company_name");
        renderJson(locationList);
    }

    // 入仓确认
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INCOMPLETED})
    public void gateInConfirm() {
        String id = getPara();

        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        WarehouseOrder warehouseOrder = WarehouseOrder.dao.findById(id);

        List<Record> list = Db.find("select * from warehouse_order_item where warehouse_order_id = '" + id + "'");
        // 获取已入库的库存
        List<Record> inverntory = Db
                .find("select * from inventory_item where product_id in(select product_id from warehouse_order_item where warehouse_order_id = '"
                        + id + "' and warehouse_id ='" + warehouseOrder.get("warehouse_id") + "')");

        // 入库库存添加
        for (int i = 0; i < inverntory.size(); i++) {
            if (inverntory.size() > 0) {
                if (list.get(i).get("product_id").equals(inverntory.get(i).get("product_id"))
                        && inverntory.get(i).get("warehouse_id").equals(warehouseOrder.get("warehouse_id"))) {
                    InventoryItem inventoryItem = InventoryItem.dao.findById(inverntory.get(i).get("id"));
                    inventoryItem.set(
                            "total_quantity",
                            Double.parseDouble(inverntory.get(i).get("total_quantity").toString())
                                    + Double.parseDouble(list.get(i).get("total_quantity").toString()));
                    inventoryItem.update();
                }
            }
        }
        // 判断过滤重复的货品，已存在不添加只加数量
        List<Record> list2 = Db.find("select warehouse_id,product_id from warehouse_order_item w "
                + "left join warehouse_order w2 on w.warehouse_order_id = w2.id " + "where w.warehouse_order_id ='"
                + id + "'");
        List<Record> inverntory2 = Db
                .find("select warehouse_id,product_id from inventory_item where product_id in(select product_id from warehouse_order_item w left join warehouse_order w2 on w.warehouse_order_id = w2.id where w.warehouse_order_id ='"
                        + id + "')");
        list2.removeAll(inverntory2);

        for (int i = 0; i < list2.size(); i++) {
            List<Record> list3 = Db.find("select w2.warehouse_id,w.* from warehouse_order_item w "
                    + "left join warehouse_order w2 on w.warehouse_order_id = w2.id " + "where w.warehouse_order_id ='"
                    + id + "' and product_id ='" + list2.get(i).get("product_id") + "'");

            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem
                    .set("party_id", warehouseOrder.get("party_id"))
                    .set("warehouse_id", warehouseOrder.get("warehouse_id"))
                    .set("product_id", list3.get(0).get("product_id"))
                    // .set("item_name", list3.get(0).get("item_name"))
                    // .set("item_no", list3.get(0).get("item_no"))
                    .set("uom", list3.get(0).get("uom")).set("lot_no", list3.get(0).get("lot_no"))
                    .set("total_quantity", list3.get(0).get("total_quantity"))
                    .set("unit_price", list3.get(0).get("unit_price")).set("unit_cost", list3.get(0).get("unit_cost"))
                    .set("caton_no", list3.get(0).get("caton_no")).set("creator", users.get(0).get("id"))
                    .set("create_date", createDate);
            inventoryItem.save();

        }
        warehouseOrder.set("status", "已入库");
        warehouseOrder.update();
        renderJson("{\"success\":true}");
    }

    // 出仓确认
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTCOMPLETED})
    public void gateOutConfirm() {
        String id = getPara();

        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        // 获取从表的货品数据
        List<Record> warehouseItem = Db.find("select * from warehouse_order_item where warehouse_order_id = '" + id
                + "'");

        // 获取已入库的库存
        List<Record> inventory = Db
                .find("select * from inventory_item where product_id in(select product_id from warehouse_order_item where warehouse_order_id = '"
                        + id + "')");
        // 出库后更新数据
        for (int i = 0; i < warehouseItem.size(); i++) {
            InventoryItem inventoryItem = InventoryItem.dao.findById(inventory.get(i).get("id"));
            inventoryItem.set("total_quantity", Double.parseDouble(inventory.get(i).get("total_quantity").toString())
                    - Double.parseDouble(warehouseItem.get(i).get("total_quantity").toString()));
            inventoryItem.update();
        }
        // 删除库存为0的数据
        List<Record> list = Db
                .find("select id,total_quantity from inventory_item where product_id in(select product_id from warehouse_order_item where warehouse_order_id = '"
                        + id + "')");
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (Double.parseDouble(list.get(i).get("total_quantity").toString()) <= 0) {
                    InventoryItem.dao.deleteById(list.get(i).get("id"));
                }
            }
        }

        // 出库单完成出库
        WarehouseOrder warehouseOrder = WarehouseOrder.dao.findById(id);
        warehouseOrder.set("status", "已出库");
        warehouseOrder.update();

        // 生成运输单
        String orderType = getPara("orderType");
        if (orderType.equals("gateOutTransferOrder")) {
            creatTransferOrder(id, users, createDate, warehouseItem, inventory);
        } else {
            renderJson("{\"success\":true}");
        }

    }

    // 生成运输单
    public void creatTransferOrder(String id, List<UserLogin> users, Date createDate, List<Record> warehouseItem,
            List<Record> inventory) {
        if (inventory.size() > 0) {
        	String sql = "select * from transfer_order order by id desc limit 0,1";
            String orderNo = OrderNoUtil.getOrderNo(sql, "YS");
            
            TransferOrder transferOrder = new TransferOrder();
            Party party = Party.dao
                    .findFirst(" select c.location from party p,contact c where p.contact_id =c.id and p.id='"
                            + inventory.get(0).get("party_id") + "'");

            transferOrder.set("order_no", orderNo);
            transferOrder.set("customer_id", inventory.get(0).get("party_id"));
            transferOrder.set("status", "新建");
            transferOrder.set("warehouse_id", inventory.get(0).get("warehouse_id"));
            transferOrder.set("route_from", party.get("location"));
            transferOrder.set("order_type", "gateOutTransferOrder");
            transferOrder.set("create_stamp", createDate);
            transferOrder.set("create_by", users.get(0).get("id"));
            transferOrder.save();

            for (int i = 0; i < inventory.size(); i++) {
                Product product = Product.dao.findById(inventory.get(i).get("product_id"));
                if (product != null) {
                    TransferOrderItem tItem = new TransferOrderItem();
                    tItem.set("item_no", product.get("item_no"));
                    tItem.set("item_name", product.get("item_name"));
                    tItem.set("size", product.get("size"));
                    tItem.set("width", product.get("width"));
                    tItem.set("height", product.get("height"));
                    tItem.set("volume", product.get("volume"));
                    tItem.set("weight", product.get("weight"));
                    tItem.set("amount", warehouseItem.get(i).get("total_quantity"));
                    tItem.set("unit", product.get("unit"));
                    tItem.set("product_id", inventory.get(i).get("product_id"));
                    tItem.set("order_id", transferOrder.get("id"));
                    tItem.save();
                }
            }
            renderJson(transferOrder.get("id"));
        }
    }

    // 生成运输单

    // 选中客户验证是否有产品
    public void confirmproduct() {
        String id = getPara();
        List<Record> list = Db
                .find("select  * from product p left join category c on p.category_id =c.id where c.customer_id ='"
                        + id + "'");
        if (list.size() > 0) {
            renderJson("{\"success\":true}");
        } else {
            renderJson("{\"success\":false}");
        }
    }
    
    // 查找所有网点
    public void searchAllOffice() {
    	String officeName = getPara("officeName");
    	String sql ="";
    	if(officeName != null && !"".equals(officeName)){
    		sql = "select * from office where office_name like '%"+officeName+"%'";
    	}else{
    		sql = "select * from office";
    	}
        List<Office> office = Office.dao.find(sql);
        renderJson(office);
    }
    // 按网点查找仓库
    public void findWarehouseById() {
    	String warehouseName = getPara("warehouseName");
    	String officeId = getPara("officeId");
    	String sql ="";
    	if(officeId != null && !"".equals(officeId)){
    		sql = "select * from warehouse where office_id = " + officeId;
    	}else if(warehouseName != null && !"".equals(warehouseName)){
    		sql = "select * from warehouse where warehouse_name like '%"+warehouseName+"%'";
    	}else{
    		sql = "select * from warehouse";
    	}
        List<Warehouse> warehouses = Warehouse.dao.find(sql);
        renderJson(warehouses);
    }

}
