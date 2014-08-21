package controllers.yh.delivery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderFinItem;
import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.DepartOrder;
import models.DepartOrderFinItem;
import models.Fin_item;
import models.InventoryItem;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.contract.Contract;
import models.yh.delivery.DeliveryOrder;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

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
        // 生成应付
        setPay(transferOrder, users, sqlDate);

    }

    // 配送单 发车应付计费  先获取有效期内的合同，条目越具体优先级越高
    // 级别1：计费类别 + 货  品 + 始发地 + 目的地
    // 级别2：计费类别 + 货  品 + 目的地
    // 级别3：计费类别 + 始发地 + 目的地
    // 级别4：计费类别 + 目的地
    private void setPay(DeliveryOrder deliverOrder, List<UserLogin> users, java.sql.Timestamp sqlDate) {
        // 生成应付
        Long spId=deliverOrder.getLong("sp_id");
        if ( spId== null)
            return;
        
        //先获取有效期内的配送sp合同, 如有多个，默认取第一个
        Contract spContract= Contract.dao.findFirst("select * from contract where type='DELIVERY_SERVICE_PROVIDER' " +
                "and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="+spId);
        if(spContract==null)
            return;
        
        String chargeType = deliverOrder.get("charge_type");
        if(chargeType==null)//TODO 计费方式，页面上没有保存
            chargeType="perUnit";
        Long deliverOrderId = deliverOrder.getLong("id");
        if (spId != null) {
            
            List<Record> deliveryOrderItemList = Db
                    .find("select toi.product_id, d_o.route_from, d_o.route_to from delivery_order_item doi left join transfer_order_item toi on doi.transfer_item_id = toi.id left join delivery_order d_o on doi.delivery_id = d_o.id " 
                    +"where doi.delivery_id =" + deliverOrderId);
            for (Record dOrderItemRecord : deliveryOrderItemList) {
                Record contractFinItem = Db
                        .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                + "and product_id ="+dOrderItemRecord.get("product_id")
                                +" and from_id = '"+ dOrderItemRecord.get("route_from")
                                +"' and to_id = '"+ dOrderItemRecord.get("route_to")
                                + "' and priceType='"+chargeType+"'");
                
                if (contractFinItem != null) {
                    genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem);
                }else{
                    contractFinItem = Db
                            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                    + "and product_id ="+dOrderItemRecord.get("product_id")
                                    +" and to_id = '"+ dOrderItemRecord.get("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    
                    if (contractFinItem != null) {
                        genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem);
                    }else{
                        contractFinItem = Db
                                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                        +" and from_id = '"+ dOrderItemRecord.get("route_from")
                                        +"' and to_id = '"+ dOrderItemRecord.get("route_to")
                                        + "' and priceType='"+chargeType+"'");
                        
                        if (contractFinItem != null) {
                            genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem);
                        }else{
                            contractFinItem = Db
                                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                            //+" and to_id = '"+ dOrderItemRecord.get("route_to")
                                            +" and to_id = '110101' and priceType='"+chargeType+"'");
                            
                            if (contractFinItem != null) {
                                genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem);
                            }
                        }
                    }
                }
            }
            
        }
    }

    private void genFinItem(Long departOrderId, Record tOrderItemRecord,
            Record contractFinItem) {
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
        DeliveryOrderFinItem deliveryFinItem = new DeliveryOrderFinItem();
        deliveryFinItem.set("fin_item_id", contractFinItem.get("fin_item_id"));
        //ATM数量是1， 普通货品应该有数量 * tOrderItemRecord.getDouble("amount")
        deliveryFinItem.set("amount",contractFinItem.getDouble("amount") );
        deliveryFinItem.set("order_id", departOrderId);
        deliveryFinItem.set("status", "未完成");
        deliveryFinItem.set("creator", LoginUserController.getLoginUserId(this));
        deliveryFinItem.set("create_date", now);
        deliveryFinItem.save();
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
                    if (inventList.size() > 0) {

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
        DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(delivery_id);
        deliveryOrder.set("status", "已签收");
        deliveryOrder.update();
        String transferId = deliveryOrder.get("transfer_order_id");
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

        Date createDate = Calendar.getInstance().getTime();
        String orderNo = creatOrderNo();
        List<Record> transferlist = Db
                .find("select * from delivery_order_item where delivery_id='" + delivery_id + "'");
        for (int i = 0; i < transferlist.size(); i++) {
            if (transferlist.get(i).get("transfer_item_id") == null) {
                TransferOrder transferOrder = TransferOrder.dao.findById(transferlist.get(i).get("transfer_order_id"));
                if (transferOrder != null) {
                    ReturnOrder returnOrder = new ReturnOrder();
                    returnOrder.set("order_no", orderNo);
                    returnOrder.set("transfer_order_id", transferlist.get(i).get("transfer_order_id"));
                    returnOrder.set("delivery_order_id", delivery_id);
                    returnOrder.set("customer_id", transferOrder.get("customer_id"));
                    returnOrder.set("notity_party_id", transferOrder.get("notity_party_id"));
                    returnOrder.set("order_type", "应收");
                    returnOrder.set("transaction_status", "新建");
                    returnOrder.set("creator", users.get(0).get("id"));
                    returnOrder.set("create_date", createDate);
                    returnOrder.save();
                }

            } else {
                // ATM回单
                TransferOrder transferOrder = TransferOrder.dao.findById(transferlist.get(i).get("transfer_order_id"));
                if (transferOrder != null) {
                    ReturnOrder returnOrder = new ReturnOrder();
                    returnOrder.set("order_no", orderNo);
                    returnOrder.set("transfer_order_id", transferlist.get(i).get("transfer_order_id"));
                    returnOrder.set("delivery_order_id", delivery_id);
                    returnOrder.set("customer_id", transferOrder.get("customer_id"));
                    returnOrder.set("notity_party_id", transferOrder.get("notity_party_id"));
                    returnOrder.set("order_type", "应收");
                    returnOrder.set("transaction_status", "新建");
                    returnOrder.set("creator", users.get(0).get("id"));
                    returnOrder.set("create_date", createDate);
                    returnOrder.save();
                }
            }
        }
        // 生成回单

        // 生成应收
        TransferOrderFinItem tFinItem = new TransferOrderFinItem();
        List<Record> trasferList = Db
                .find("select * from delivery_order_item where delivery_id ='" + delivery_id + "'");
        // 普通货品
        for (int i = 0; i < trasferList.size(); i++) {
            if (trasferList.get(0).get("trasfer_item_id") == null) {
                TransferOrder tOrder = TransferOrder.dao.findById(trasferList.get(i).get("transfer_order_id"));
                if (deliveryOrder.get("customer_id") != null) {
                    List<Record> contractList = Db
                            .find("select amount from contract_item where contract_id in(select id from contract c where c.party_id ='"
                                    + tOrder.get("customer_id")
                                    + "') and from_id = '"
                                    + tOrder.get("route_from")
                                    + "' and to_id ='"
                                    + tOrder.get("route_to")
                                    + "' and priceType='"
                                    + tOrder.get("charge_type") + "'");
                    if (contractList.size() > 0) {
                        tFinItem.set("order_id", tOrder.get("id"));
                        tFinItem.set("fin_item_id", "4");
                        tFinItem.set("amount", contractList.get(0).get("amount"));
                        tFinItem.set("status", "未完成");
                        tFinItem.set("creator", users.get(0).get("id"));
                        tFinItem.set("create_date", sqlDate);
                        tFinItem.save();
                    }
                }
            } else {
                TransferOrder tOrder = TransferOrder.dao.findById(trasferList.get(i).get("transfer_order_id"));
                int size = trasferList.size();
                if (deliveryOrder.get("customer_id") != null) {
                    List<Record> contractList = Db
                            .find("select amount from contract_item where contract_id in(select id from contract c where c.party_id ='"
                                    + tOrder.get("customer_id")
                                    + "') and from_id = '"
                                    + tOrder.get("route_from")
                                    + "' and to_id ='"
                                    + tOrder.get("route_to")
                                    + "' and priceType='"
                                    + tOrder.get("charge_type") + "'");
                    if (contractList.size() > 0) {
                        tFinItem.set("order_id", tOrder.get("id"));
                        tFinItem.set("fin_item_id", "4");
                        tFinItem.set("amount", Double.parseDouble(contractList.get(0).get("amount").toString()) * size);
                        tFinItem.set("status", "未完成");
                        tFinItem.set("creator", users.get(0).get("id"));
                        tFinItem.set("create_date", sqlDate);
                        tFinItem.save();
                    }
                }
            }
        }
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
        String transferId = "";
        List<DeliveryOrderItem> dItem = DeliveryOrderItem.dao
                .find("select * from delivery_order_item where delivery_id = '" + id + "'");
        if (dItem.size() > 0) {
            for (DeliveryOrderItem dItem2 : dItem) {
                transferId += dItem2.get("transfer_order_id") + ",";
            }
            transferId = transferId.substring(0, transferId.length() - 1);
        }

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from transfer_order_fin_item where order_id in(" + transferId + ")";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select d.*,f.name,f.remark,t.order_no as transferOrderNo from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join transfer_order t on t.id = d.order_id where d.order_id in("
                        + transferId + ") and f.type='应收'");

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
        String sql = "select count(1) total from transfer_order_fin_item where delivery_id ='" + id + "' ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select d.*,f.name,f.remark,t.order_no as transferOrderNo from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join transfer_order t on t.id = d.order_id where d.delivery_id='"
                        + id + "' and f.type='应付'");

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
        String deliveryId = getPara();
        Fin_item fItem = new Fin_item();
        TransferOrderFinItem dFinItem = new TransferOrderFinItem();
        fItem.set("type", "应付");
        fItem.save();
        dFinItem.set("fin_item_id", fItem.get("id")).set("status", "新建").set("delivery_id", deliveryId);
        dFinItem.save();
        renderJson("{\"success\":true}");
    }

    // 添加应付
    public void paymentSave() {
        String returnValue = "";
        String id = getPara("id");
        String finItemId = getPara("finItemId");
        TransferOrderFinItem dFinItem = TransferOrderFinItem.dao.findById(id);

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
            }
        }
        renderText(returnValue);
    }

    public void fin_item() {
        // String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        locationList = Db.find("select * from fin_item where type='应付'");
        renderJson(locationList);
    }

    public void fin_item2() {
        // String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        locationList = Db.find("select * from fin_item where type='应收'");
        renderJson(locationList);
    }
}
