package controllers.yh.delivery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderFinItem;
import models.DeliveryOrderMilestone;
import models.Fin_item;
import models.InventoryItem;
import models.TransferOrder;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.delivery.DeliveryOrder;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class DeliveryOrderMilestoneController extends Controller {

    private Logger logger = Logger.getLogger(DeliveryOrderMilestoneController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void transferOrderMilestoneList() {
        Map<String, List> map = new HashMap<String, List>();
        List<String> usernames = new ArrayList<String>();
        String delivery_id = getPara("delivery_id");
        if (delivery_id == "") {
            delivery_id = "-1";
        }
        List<DeliveryOrderMilestone> transferOrderMilestones = DeliveryOrderMilestone.dao
                .find("select * from delivery_order_milestone where delivery_id=" + delivery_id);
        for (DeliveryOrderMilestone transferOrderMilestone : transferOrderMilestones) {
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("user_name");
            usernames.add(username);
        }
        map.put("transferOrderMilestones", transferOrderMilestones);
        map.put("usernames", usernames);
        renderJson(map);
    }

    // 发车确认
    public void departureConfirmation() {
        Long delivery_id = Long.parseLong(getPara("delivery_id"));
        DeliveryOrder transferOrder = DeliveryOrder.dao.findById(delivery_id);
        transferOrder.set("status", "已发车");
        transferOrder.update();
        // 扣库存
        gateOutProduct(delivery_id);
        // 生成应付

        Map<String, Object> map = new HashMap<String, Object>();
        DeliveryOrderMilestone transferOrderMilestone = new DeliveryOrderMilestone();
        transferOrderMilestone.set("status", "已发车");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("delivery_id", getPara("delivery_id"));
        transferOrderMilestone.save();
        map.put("transferOrderMilestone", transferOrderMilestone);
        UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
        String username = userLogin.get("user_name");
        map.put("username", username);
        renderJson(map);
    }

    // 扣库存
    private void gateOutProduct(Long delivery_id) {
        // 获取运输单的id
        List<Record> list = Db.find("select transfer_order_id from delivery_order_item where delivery_id ='"
                + delivery_id + "'");
        // 获取运输单信息
        TransferOrder tOrder = TransferOrder.dao.findById(list.get(0).get("transfer_order_id"));

        // 单个id（普通货品的）
        if (list.size() == 1 || list.get(0).get("transfer_item_id") == null) {
            // 运输单 货品
            List<Record> transferItem = Db.find("select * from transfer_order_item where order_id ='"
                    + list.get(0).get("transfer_order_id") + "'");
            // 出库扣库存
            for (int i = 0; i < transferItem.size(); i++) {
                if (transferItem.get(i).get("product_id") != null) {
                    // 运输单之前加入的库存
                    List<Record> inventList = Db.find("select * from inventory_item where party_id='"
                            + tOrder.get("customer_id") + "' and warehouse_id ='" + tOrder.get("warehouse_id")
                            + "' and product_id ='" + transferItem.get(i).get("product_id") + "'");

                    InventoryItem inventoryItem = InventoryItem.dao.findById(inventList.get(i).get("id"));
                    inventoryItem.set(
                            "total_quantity",
                            Double.parseDouble(inventList.get(0).get("total_quantity").toString())
                                    - Double.parseDouble(transferItem.get(i).get("amount").toString()));
                    inventoryItem.update();
                    // 删除库存为0的数据
                    if (inventList.size() > 0) {
                        if (Double.parseDouble(inventList.get(i).get("total_quantity").toString()) <= 0) {
                            InventoryItem.dao.deleteById(inventList.get(i).get("id"));
                        }
                    }
                }
            }
        } else {
            // ATM单品配送扣库存
            // 运输单 货品
            List<Record> transferItem = Db.find("select * from transfer_order_item where order_id ='"
                    + list.get(0).get("transfer_order_item") + "'");

            // 出库扣库存
            for (int i = 0; i < transferItem.size(); i++) {
                List<Record> transferItemDetail = Db.find("select * from delivery_order_item where transfer_item_id ='"
                        + transferItem.get(i).get("id") + "'");
                int size = transferItemDetail.size();
                if (transferItem.get(i).get("product_id") != null) {
                    // 运输单之前加入的库存
                    List<Record> inventList = Db.find("select * from inventory_item where party_id='"
                            + tOrder.get("customer_id") + "' and warehouse_id ='" + tOrder.get("warehouse_id")
                            + "' and product_id ='" + transferItem.get(i).get("product_id") + "'");

                    InventoryItem inventoryItem = InventoryItem.dao.findById(inventList.get(i).get("id"));
                    inventoryItem.set("total_quantity",
                            Double.parseDouble(inventList.get(0).get("total_quantity").toString()) - size);
                    inventoryItem.update();
                    // 删除库存为0的数据
                    if (inventList.size() > 0) {
                        if (Double.parseDouble(inventList.get(i).get("total_quantity").toString()) <= 0) {
                            InventoryItem.dao.deleteById(inventList.get(i).get("id"));
                        }
                    }
                }
            }
        }
    }

    // 保存里程碑
    public void saveTransferOrderMilestone() {
        Long delivery_id = Long.parseLong(getPara("delivery_id"));
        DeliveryOrder transferOrder = DeliveryOrder.dao.findById(delivery_id);
        Map<String, Object> map = new HashMap<String, Object>();
        DeliveryOrderMilestone transferOrderMilestone = new DeliveryOrderMilestone();
        String status = getPara("status");
        String location = getPara("location");
        if (!status.isEmpty()) {
            transferOrderMilestone.set("status", status);
            transferOrder.set("status", status);
        } else {
            transferOrderMilestone.set("status", "在途");
            transferOrder.set("status", "在途");
        }
        transferOrder.update();
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
        transferOrderMilestone.set("delivery_id", getPara("delivery_id"));
        transferOrderMilestone.save();

        map.put("transferOrderMilestone", transferOrderMilestone);
        UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
        String username = userLogin.get("user_name");
        map.put("username", username);
        renderJson(map);
    }

    // 回单签收
    public void receipt() {
        Long delivery_id = Long.parseLong(getPara("delivery_id"));
        DeliveryOrder transferOrder = DeliveryOrder.dao.findById(delivery_id);
        transferOrder.set("status", "已签收");
        transferOrder.update();
        String transferId = transferOrder.get("transfer_order_id");
        Map<String, Object> map = new HashMap<String, Object>();
        DeliveryOrderMilestone transferOrderMilestone = new DeliveryOrderMilestone();
        transferOrderMilestone.set("status", "已签收");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("delivery_id", getPara("delivery_id"));
        transferOrderMilestone.save();
        map.put("transferOrderMilestone", transferOrderMilestone);
        UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
        String username = userLogin.get("user_name");
        map.put("username", username);
        renderJson(map);
    }

    // 入库确认
    public void warehousingConfirm() {
        Long order_id = Long.parseLong(getPara("order_id"));
        TransferOrder transferOrder = TransferOrder.dao.findById(order_id);
        transferOrder.set("status", "已入库");
        transferOrder.update();
        Map<String, Object> map = new HashMap<String, Object>();
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        transferOrderMilestone.set("status", "已入库");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("delivery_id", getPara("delivery_id"));
        transferOrderMilestone.save();
        map.put("transferOrderMilestone", transferOrderMilestone);
        UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
        String username = userLogin.get("user_name");
        map.put("username", username);
        renderJson(map);
    }

    // 应收list
    public void accountReceivable() {
        String id = getPara();
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from delivery_order_fin_item ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select * from delivery_order_fin_item d left join fin_item f on d.fin_item_id = f.id where f.type='应收' and d.order_id ='"
                        + id + "'");

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // 应付list
    public void accountPayable() {
        String id = getPara();
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from delivery_order_fin_item ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select * from delivery_order_fin_item d left join fin_item f on d.fin_item_id = f.id where f.type='应付' and d.order_id='"
                        + id + "'");

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // 添加应收
    public void receiptSave() {
        String id = getPara("delivery_item_id");

        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        Fin_item fItem = new Fin_item();
        DeliveryOrderFinItem dFinItem = new DeliveryOrderFinItem();
        if (!id.equals("")) {

        } else {
            fItem.set("name", getPara("item_name")).set("Remark", getPara("item_remark")).set("type", "应收");
            fItem.save();
            dFinItem.set("amount", getPara("item_amount")).set("fin_item_id", fItem.get("id")).set("status", "新建")
                    .set("order_id", getPara());
            dFinItem.save();
        }
        renderJson("{\"success\":true}");
    }

    // 添加应付
    public void paymentSave() {
        String id = getPara("delivery_item_id2");

        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        Fin_item fItem = new Fin_item();
        DeliveryOrderFinItem dFinItem = new DeliveryOrderFinItem();
        if (!id.equals("")) {

        } else {
            fItem.set("name", getPara("item_name2")).set("Remark", getPara("item_remark2")).set("type", "应付");
            fItem.save();
            dFinItem.set("amount", getPara("item_amount2")).set("fin_item_id", fItem.get("id")).set("status", "新建")
                    .set("order_id", getPara());
            dFinItem.save();
        }
        renderJson("{\"success\":true}");
    }
}
