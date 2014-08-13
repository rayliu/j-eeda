package controllers.yh.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartTransferOrder;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.returnOrder.ReturnOrderController;

public class TransferOrderMilestoneController extends Controller {

    private Logger logger = Logger.getLogger(TransferOrderMilestoneController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void transferOrderMilestoneList() {
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

    // 发车确认
    public void departureConfirmation() {
        Long order_id = Long.parseLong(getPara("order_id"));
        TransferOrder transferOrder = TransferOrder.dao.findById(order_id);
        transferOrder.set("status", "已发车");
        transferOrder.update();
        Map<String, Object> map = new HashMap<String, Object>();
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        transferOrderMilestone.set("status", "已发车");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("order_id", getPara("order_id"));
        transferOrderMilestone.save();
        map.put("transferOrderMilestone", transferOrderMilestone);
        UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
        String username = userLogin.get("user_name");
        map.put("username", username);
        renderJson(map);
    }

    // 保存里程碑
    public void saveTransferOrderMilestone() {
        String order_id = getPara("order_id");
        String milestonePickupId = getPara("milestonePickupId");
        String milestoneDepartId = getPara("milestoneDepartId");
        Map<String, Object> map = new HashMap<String, Object>();
        if (order_id != null && !"".equals(order_id)) {
            TransferOrder transferOrder = TransferOrder.dao.findById(order_id);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
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
            transferOrderMilestone.set("order_id", getPara("order_id"));
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.save();

            map.put("transferOrderMilestone", transferOrderMilestone);
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("user_name");
            map.put("username", username);
        } else if (milestonePickupId != null && !"".equals(milestonePickupId)) {
            TransferOrder transferOrder = null;
            DepartOrder departOrder = DepartOrder.dao.findById(milestonePickupId);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            String status = getPara("status");
            String location = getPara("location");
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
            transferOrderMilestone.set("pickup_id", milestonePickupId);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE);
            transferOrderMilestone.save();

            // 更新运输单状态
            List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                    "select * from depart_transfer where depart_id = ?", departOrder.get("id"));
            for (DepartTransferOrder departTransferOrder : departTransferOrders) {
                transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
                TransferOrderMilestone tom = new TransferOrderMilestone();
                String status2 = getPara("status");
                String location2 = getPara("location");
                if (!status.isEmpty()) {
                    tom.set("status", status2);
                    transferOrder.set("status", status2);
                } else {
                    tom.set("status", "提货在途");
                    transferOrder.set("status", "提货在途");
                }
                departOrder.update();
                if (!location.isEmpty()) {
                    tom.set("location", location2);
                } else {
                    tom.set("location", "");
                }
                String name2 = (String) currentUser.getPrincipal();
                List<UserLogin> users2 = UserLogin.dao.find("select * from user_login where user_name='" + name2 + "'");

                tom.set("create_by", users.get(0).get("id"));

                java.util.Date utilDate2 = new java.util.Date();
                java.sql.Timestamp sqlDate2 = new java.sql.Timestamp(utilDate2.getTime());
                tom.set("create_stamp", sqlDate2);
                tom.set("order_id", departTransferOrder.get("order_id"));
                tom.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
                tom.save();
                transferOrder.update();
            }

            map.put("transferOrderMilestone", transferOrderMilestone);
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("user_name");
            map.put("username", username);
        } else if (milestoneDepartId != null && !"".equals(milestoneDepartId)) {
            DepartOrder departOrder = DepartOrder.dao.findById(milestoneDepartId);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            String status = getPara("status");
            String location = getPara("location");
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

    // 回单签收
    public void receipt() {
        Long order_id = Long.parseLong(getPara("orderId"));
        TransferOrder transferOrder = TransferOrder.dao.findById(order_id);
        List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
                .find("select toid.pickup_id from transfer_order_item_detail toid where order_id = ? group by toid.pickup_id",
                        transferOrder.get("id"));
        if (transferOrderItemDetails.size() > 1) {
            transferOrder.set("status", "部分已签收");
            transferOrder.update();
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "部分已签收");
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
            transferOrderMilestone.set("create_by", users.get(0).get("id"));
            transferOrderMilestone.set("location", "");
            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("order_id", order_id);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.save();
        } else {
            transferOrder.set("status", "已签收");
            transferOrder.update();
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "已签收");
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
            transferOrderMilestone.set("create_by", users.get(0).get("id"));
            transferOrderMilestone.set("location", "");
            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("order_id", order_id);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.save();

            ReturnOrder returnOrder = new ReturnOrder();
            returnOrder.set("order_no", ReturnOrderController.createReturnOrderNo());
            returnOrder.set("transaction_status", "新建");
            returnOrder.set("creator", users.get(0).get("id"));
            returnOrder.set("create_date", sqlDate);
            returnOrder.set("transfer_order_id", order_id);
            returnOrder.save();

            // 生成应收

            TransferOrderFinItem tFinItem = new TransferOrderFinItem();

            if (transferOrder.get("coustomer_id") != null) {
                // ATM计件算
                List<Record> list = Db.find("select * from transfer_order_item where order_id ='"
                        + transferOrder.get("id") + "'");
                int size = list.size();
                if (transferOrder.get("cargo_nature").equals("ATM")) {
                    List<Record> contractList = Db
                            .find("select amount from contract_item where contract_id in(select id from contract c where c.party_id ='"
                                    + transferOrder.get("coustomer_id")
                                    + "') and from_id = '"
                                    + transferOrder.get("route_from")
                                    + "' and to_id ='"
                                    + transferOrder.get("routa e_to")
                                    + "' and priceType='"
                                    + transferOrder.get("charge_type") + "'");
                    if (contractList.size() > 0) {
                        tFinItem.set("order_id", transferOrder.get("id"));
                        tFinItem.set("fin_item_id", "4");
                        tFinItem.set("amount", Double.parseDouble(contractList.get(0).get("amount").toString()) * size);
                        tFinItem.set("status", "未完成");
                        tFinItem.set("creator", users.get(0).get("id"));
                        tFinItem.set("create_date", sqlDate);
                        tFinItem.save();
                    }
                } else {
                    // 普通货品
                    List<Record> contractList = Db
                            .find("select amount from contract_item where contract_id in(select id from contract c where c.party_id ='"
                                    + transferOrder.get("coustomer_id")
                                    + "') and from_id = '"
                                    + transferOrder.get("route_from")
                                    + "' and to_id ='"
                                    + transferOrder.get("routa e_to")
                                    + "' and priceType='"
                                    + transferOrder.get("charge_type") + "'");
                    if (contractList.size() > 0) {
                        tFinItem.set("order_id", transferOrder.get("id"));
                        tFinItem.set("fin_item_id", "4");
                        tFinItem.set("amount", contractList.get(0).get("amount"));
                        tFinItem.set("status", "未完成");
                        tFinItem.set("creator", users.get(0).get("id"));
                        tFinItem.set("create_date", sqlDate);
                        tFinItem.save();
                    }
                }
            }
        }
        renderJson("{\"success\":true}");
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
        transferOrderMilestone.set("order_id", getPara("order_id"));
        transferOrderMilestone.save();
        map.put("transferOrderMilestone", transferOrderMilestone);
        UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
        String username = userLogin.get("user_name");
        map.put("username", username);
        renderJson(map);
    }
}
