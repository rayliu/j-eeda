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
import models.TransferOrderItemDetail;
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
import controllers.yh.returnOrder.ReturnOrderController;

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
        DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(delivery_id);
        deliveryOrder.set("status", "已发车");
        deliveryOrder.update();
        
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
        // 扣库存
        gateOutProduct(deliveryOrder);
        // 生成应付
        setPay(deliveryOrder, users, sqlDate);

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
        
        String chargeType = deliverOrder.get("pricetype");
        
        Long deliverOrderId = deliverOrder.getLong("id");
        if (spId != null) {
            if ("perUnit".equals(chargeType)) {
                genFinPerUnit(spContract, chargeType, deliverOrderId);
            } else if ("perCar".equals(chargeType)) {
                genFinPerCar(spContract, chargeType, deliverOrder);
            } else if ("perCargo".equals(chargeType)) {

            }
            
            
        }
    }

    private void genFinPerCar(Contract spContract, String chargeType, DeliveryOrder deliverOrder) {
        Long deliverOrderId = deliverOrder.getLong("id");
    
        Record contractFinItem = Db
                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                        +" and cartype = " + deliverOrder.get("car_type")
                        +" and carlength = " + deliverOrder.get("car_size")
                        +" and from_id = '"+ deliverOrder.get("route_from")
                        +"' and to_id = '"+ deliverOrder.get("route_to")
                        + "' and priceType='"+chargeType+"'");
        
        if (contractFinItem != null) {
            genFinItem(deliverOrderId, null, contractFinItem);
        }else{
            contractFinItem = Db
                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                            +" and cartype = " + deliverOrder.get("car_type")
                            +" and from_id = '"+ deliverOrder.get("route_from")
                            +"' and to_id = '"+ deliverOrder.get("route_to")
                            + "' and priceType='"+chargeType+"'");
            
            if (contractFinItem != null) {
                genFinItem(deliverOrderId, null, contractFinItem);
            }else{
                contractFinItem = Db
                        .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                +" and from_id = '"+ deliverOrder.get("route_from")
                                +"' and to_id = '"+ deliverOrder.get("route_to")
                                + "' and priceType='"+chargeType+"'");
                
                if (contractFinItem != null) {
                    genFinItem(deliverOrderId, null, contractFinItem);
                }else{
                    contractFinItem = Db
                            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                    +" and to_id = '"+ deliverOrder.get("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    
                    if (contractFinItem != null) {
                        genFinItem(deliverOrderId, null, contractFinItem);
                    }
                }
            }
        }
        
    }
    private void genFinPerUnit(Contract spContract, String chargeType, Long deliverOrderId) {
        List<Record> deliveryOrderItemList = Db
                .find("SELECT count(1) amount, toi.product_id, d_o.route_from, d_o.route_to FROM "+
					    "delivery_order_item doi LEFT JOIN transfer_order_item_detail toid ON doi.transfer_item_detail_id = toid.id "+
					        "LEFT JOIN transfer_order_item toi ON toid.item_id = toi.id "+
					        "LEFT JOIN delivery_order d_o ON doi.delivery_id = d_o.id "+
                		"WHERE  doi.delivery_id = "+ deliverOrderId+" group by toi.product_id, d_o.route_from, d_o.route_to" );
        for (Record dOrderItemRecord : deliveryOrderItemList) {
            Record contractFinItem = Db
                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                            + " and product_id ="+dOrderItemRecord.get("product_id")
                            +" and from_id = '"+ dOrderItemRecord.get("route_from")
                            +"' and to_id = '"+ dOrderItemRecord.get("route_to")
                            + "' and priceType='"+chargeType+"'");
            
            if (contractFinItem != null) {
                genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem);
            }else{
                contractFinItem = Db
                        .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                + " and product_id ="+dOrderItemRecord.get("product_id")
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
                                        +" and to_id = '"+ dOrderItemRecord.get("route_to")
                                        +"' and priceType='"+chargeType+"'");
                        
                        if (contractFinItem != null) {
                            genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem);
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
        
        if(tOrderItemRecord==null){
            deliveryFinItem.set("amount", contractFinItem.getDouble("amount") );
        }else{            
            Long itemAmount=tOrderItemRecord.getLong("amount");
            deliveryFinItem.set("amount", contractFinItem.getDouble("amount") * itemAmount);
        }
        
        deliveryFinItem.set("order_id", departOrderId);
        deliveryFinItem.set("status", "未完成");
        deliveryFinItem.set("creator", LoginUserController.getLoginUserId(this));
        deliveryFinItem.set("create_date", now);
        deliveryFinItem.save();
    }
    // 扣库存
    private void gateOutProduct(DeliveryOrder deliveryOrder) {
        Long deliveryOrderId=deliveryOrder.getLong("id");
        Long warehouseId=deliveryOrder.get("from_warehouse_id");
        // 获取配送单的item list TODO 只针对ATM， 普通货品需要再验证
        List<Record> itemList = Db.find("select di.*, ti.product_id, c.name as c_name from delivery_order_item di "
                                        +"left join transfer_order_item_detail toid on di.transfer_item_detail_id = toid.id  "
                                        +"left join transfer_order_item ti on toid.item_id = ti.id "
                                        +"left join product p on ti.product_id = p.id  "
                                        +"left join category c on p.category_id = c.id  "
                                        +"where di.delivery_id ="+ deliveryOrderId);
        for (Record dOrderItemRecord : itemList) {
            //如果没有product_id, 则不用计算库存
            if(dOrderItemRecord.get("product_id")==null)
                continue;
            
            Long productId=dOrderItemRecord.getLong("product_id");
            if("ATM".equals(dOrderItemRecord.get("c_name"))){
                InventoryItem item = InventoryItem.dao.findFirst("select * from inventory_item where product_id=? and warehouse_id=?", productId, warehouseId);
                if(item!=null){
                    //TODO 如果库存不够，应该报错提示，并且回滚，不能发车
                    item.set("total_quantity", item.getDouble("total_quantity")-1).update();
                }
                //在运输单中把单品设置为已出库（配送）
                TransferOrderItemDetail tDetail= TransferOrderItemDetail.dao.findById(dOrderItemRecord.getLong("transfer_item_detail_id"));
                if(item!=null){
                    tDetail.set("is_delivered", true).update();
                }
            }else{
                InventoryItem item = InventoryItem.dao.findFirst("select * from inventory_item where product_id=? and warehouse_id=?", productId, warehouseId);
                if(item!=null){
                    //TODO 如果是普通货品，还是-1？？
                    item.set("total_quantity", item.getDouble("total_quantity")-dOrderItemRecord.getDouble("amount")).update();
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

    // 配送单  到达确认
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
        
        //如果是配送单生成回单：一张配送单只生成一张回单
        ReturnOrder returnOrder = new ReturnOrder();
        returnOrder.set("order_no", orderNo);
        returnOrder.set("delivery_order_id", delivery_id);
        returnOrder.set("customer_id", deliveryOrder.get("customer_id"));
        returnOrder.set("notity_party_id", deliveryOrder.get("notity_party_id"));
        returnOrder.set("order_type", "应收");
        returnOrder.set("transaction_status", "新建");
        returnOrder.set("creator", users.get(0).get("id"));
        returnOrder.set("create_date", createDate);
        returnOrder.save();

        // 生成应收
        //ATM
        if("ATM".equals(deliveryOrder.get("cargo_nature"))){
	        ReturnOrderController roController= new ReturnOrderController();
	        roController.calculateCharge(deliveryOrder);
        }
        //TODO:  普通货品的先不算
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

    /*// 应收list
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
    }*/

    // 应付list
    public void accountPayable() {
        //String id = getPara()==null?"-1":getPara();
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
        String sql = "SELECT count(1) "+
                	  "FROM  delivery_order_fin_item d LEFT JOIN  fin_item f ON d.fin_item_id = f.id "+
                	  "WHERE d.order_id = "+id+"  and f.type='应付' ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("SELECT d.*, f.name AS fin_item_name, "+
                	  "(SELECT group_concat(distinct transfer_no separator ' ') FROM delivery_order_item where delivery_id=d.order_id) AS transferorderno "+
                	  "FROM  delivery_order_fin_item d LEFT JOIN  fin_item f ON d.fin_item_id = f.id "+
                	  "WHERE d.order_id = "+id+"  and f.type='应付'");

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
        List<Fin_item> items = new ArrayList<Fin_item>();
        String orderId = getPara();
        Fin_item item = Fin_item.dao.findFirst("select * from fin_item where type = '应付' order by id asc");
        if(item != null){
        	DeliveryOrderFinItem dFinItem = new DeliveryOrderFinItem();
	        dFinItem.set("status", "新建").set("fin_item_id", item.get("id")).set("order_id", orderId);
	        dFinItem.save();
        }
        items.add(item);
        renderJson(items);
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
    
    //修改应付
    public void updateDeliveryOrderFinItem(){
    	String paymentId = getPara("paymentId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if("amount".equals(name) && "".equals(value)){
    		value = "0";
    	}
    	if(paymentId != null && !"".equals(paymentId)){
    		DeliveryOrderFinItem deliveryOrderFinItem = DeliveryOrderFinItem.dao.findById(paymentId);
    		deliveryOrderFinItem.set(name, value);
    		deliveryOrderFinItem.update();
    	}
        renderJson("{\"success\":true}");
    }
}
