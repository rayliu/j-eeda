package controllers.yh.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartTransferOrder;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.departOrder.DepartOrderController;
import controllers.yh.returnOrder.ReturnOrderController;
import controllers.yh.util.OrderNoUtil;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
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
                String username = "";
                if(userLogin.get("c_name")!=null&&!"".equals(userLogin.get("c_name"))){
                	username=userLogin.get("c_name");
                }else{
                	username=userLogin.get("user_name");
                }
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
                String username = "";
                if(userLogin.get("c_name")!=null&&!"".equals(userLogin.get("c_name"))){
                	username=userLogin.get("c_name");
                }else{
                	username=userLogin.get("user_name");
                }
                usernames.add(username);
            }
            map.put("transferOrderMilestones", transferOrderMilestones);
            map.put("usernames", usernames);
        }
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
            String username = userLogin.get("c_name");
            if(username==null||"".equals(username)){
            	username=userLogin.get("user_name");
            }
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
            String username = userLogin.get("c_name");
            if(username==null||"".equals(username)){
            	username=userLogin.get("user_name");
            }
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
            String username = userLogin.get("c_name");
            if(username==null||"".equals(username)){
            	username=userLogin.get("user_name");
            }
            map.put("username", username);
        }
        renderJson(map);
    }

    // 收货确认
    public void receipt() {
        Long order_id = Long.parseLong(getPara("orderId"));
        Long departOrderId = Long.parseLong(getPara("departOrderId"));
        TransferOrder transferOrder = TransferOrder.dao.findById(order_id);
        DepartOrder departOrder = DepartOrder.dao.findById(departOrderId);
        List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
                .find("select toid.pickup_id from transfer_order_item_detail toid where order_id = ? group by toid.pickup_id",
                        transferOrder.get("id"));
        if (transferOrderItemDetails.size() > 1) {
            transferOrder.set("status", "部分已收货");
            transferOrder.update();
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "部分已收货");
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
            
            departOrder.set("status", "部分已收货");
            departOrder.update();
            transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "部分已收货");
            transferOrderMilestone.set("create_by", users.get(0).get("id"));
            transferOrderMilestone.set("location", "");
            utilDate = new java.util.Date();
            sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("depart_id", departOrderId);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
            transferOrderMilestone.save();
        } else {
            transferOrder.set("status", "已收货");
            transferOrder.update();
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "已收货");
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

            departOrder.set("status", "已收货");
            departOrder.update();
            transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "已收货");
            transferOrderMilestone.set("create_by", users.get(0).get("id"));
            transferOrderMilestone.set("location", "");
            utilDate = new java.util.Date();
            sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("depart_id", departOrderId);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
            transferOrderMilestone.save();
            
            String sql = "select * from return_order order by id desc limit 0,1";
            String orderNo = OrderNoUtil.getOrderNo(sql, "HD");
            ReturnOrder returnOrder = new ReturnOrder();
            returnOrder.set("order_no", orderNo);
            returnOrder.set("transaction_status", "新建");
            returnOrder.set("creator", users.get(0).get("id"));
            returnOrder.set("create_date", sqlDate);
            returnOrder.set("transfer_order_id", order_id);
            returnOrder.set("customer_id", transferOrder.get("customer_id"));
            returnOrder.save();

            /*TransferOrderFinItem tFinItem = new TransferOrderFinItem();
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
            }*/
            
            // 生成应收
	        ReturnOrderController roController= new ReturnOrderController();
	        roController.calculateChargeByCustomer(transferOrder, returnOrder.getLong("id"), users);
        }
        renderJson("{\"success\":true}");
    }

    // 发车确认
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_COMPLETED})
    public void departureConfirmation() {
        String departOrderId = getPara("departOrderId");
        int num = 1;
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find("select * from depart_transfer where depart_id = ?", departOrderId);
        for(DepartTransferOrder departTransferOrder : departTransferOrders){
        	String sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + departTransferOrder.get("order_id");
        	Record rec = Db.findFirst(sqlTotal);
        	Long total = rec.getLong("total");
        	
        	sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + departTransferOrder.get("order_id") + " and depart_id = " + departOrderId;
        	rec = Db.findFirst(sqlTotal);
        	Long departTotal1 = rec.getLong("total");
        	sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + departTransferOrder.get("order_id") + " and (status = '已发车' or status = '已入库')";
        	rec = Db.findFirst(sqlTotal);
        	Long departTotal2 = rec.getLong("total");
        	Long departTotal = departTotal1 + departTotal2;
        	if(total == departTotal){
		        List<TransferOrder> transferOrders = TransferOrder.dao.find("select * from transfer_order where id in (" + departTransferOrder.get("order_id") + ")");
		        for(TransferOrder transferOrder : transferOrders){
		        	transferOrder.set("status", "已发车");
		        	transferOrder.update();
		        	
		        	TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
			        transferOrderMilestone = milestoneMessages(transferOrderMilestone);
			        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
			        transferOrderMilestone.set("order_id", transferOrder.get("id"));
			        transferOrderMilestone.set("status", "已发车");
			        transferOrderMilestone.save();
		        }   
        	}else{
		        List<TransferOrder> transferOrders = TransferOrder.dao.find("select * from transfer_order where id in (" + departTransferOrder.get("order_id") + ")");
		        for(TransferOrder transferOrder : transferOrders){
		        	transferOrder.set("status", "部分已发车");
		        	transferOrder.update();
		        	
		        	TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
			        transferOrderMilestone = milestoneMessages(transferOrderMilestone);
			        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
			        transferOrderMilestone.set("order_id", transferOrder.get("id"));
			        transferOrderMilestone.set("status", "部分已发车");
			        transferOrderMilestone.save();
		        }
        	}
	        DepartOrder departOrder = DepartOrder.dao.findById(departOrderId);
	        departOrder.set("status", "已发车");
	        departOrder.update();
	        if(num == 1){
	        	TransferOrderMilestone departOrderMilestone = new TransferOrderMilestone();
		        departOrderMilestone = milestoneMessages(departOrderMilestone);
		        departOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
		        departOrderMilestone.set("status", "已发车");
		        departOrderMilestone.set("depart_id", departOrder.get("id"));
		        departOrderMilestone.save();
		        num = 2;
	        }
	        
	        
	        List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where order_id = " + departTransferOrder.get("order_id") + " and depart_id = " + departOrderId);
	        for(TransferOrderItemDetail detail : transferOrderItemDetails){
	        	detail.set("status", "已发车");
	        	detail.update();
	        }	
        }
        renderJson("{\"success\":true}");        
    }
    
    // 入库确认
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_OT_UPDATE})
    public void warehousingConfirm() {
    	String departOrderId = getPara("departOrderId");
    	List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find("select * from depart_transfer where depart_id = ?", departOrderId);
    	for(DepartTransferOrder departTransferOrder : departTransferOrders){
    		TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
    		if("ATM".equals(transferOrder.get("cargo_nature"))){
    			//这里只能算单品的总数,缺失判断不同货品的情况
        		//运输单中所有单品
        		String sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + departTransferOrder.get("order_id");
        		Record rec = Db.findFirst(sqlTotal);
        		Long detailTotal = rec.getLong("total");
        		//运输单中此次发车单品数量
        		sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + departTransferOrder.get("order_id") + " and depart_id = " + departOrderId;
        		rec = Db.findFirst(sqlTotal);
        		Long departTotal1 = rec.getLong("total");
        		//运输单中已入库的数量
        		sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + departTransferOrder.get("order_id") + " and status = '已入库'";
        		rec = Db.findFirst(sqlTotal);
        		Long departTotal2 = rec.getLong("total");
        		Long departTotal = departTotal1 + departTotal2;
        		if(detailTotal == departTotal){
    				transferOrder.set("status", "已入库");
    				transferOrder.update();
    				
    				TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
    				transferOrderMilestone = milestoneMessages(transferOrderMilestone);
    				transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
    				transferOrderMilestone.set("order_id", transferOrder.get("id"));
    				transferOrderMilestone.set("status", "已入库");
    				transferOrderMilestone.save();
        		}else{
    				transferOrder.set("status", "部分已入库");
    				transferOrder.update();
    				
    				TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
    				transferOrderMilestone = milestoneMessages(transferOrderMilestone);
    				transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
    				transferOrderMilestone.set("order_id", transferOrder.get("id"));
    				transferOrderMilestone.set("status", "部分已入库");
    				transferOrderMilestone.save();
        		}
        		
        		List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where order_id = " + departTransferOrder.get("order_id") + " and depart_id = " + departOrderId);
        		for(TransferOrderItemDetail detail : transferOrderItemDetails){
        			detail.set("status", "已入库");
        			detail.update();
        		}	
	        		
    		}else{
    			transferOrder.set("status", "已入库");
				transferOrder.update();
				
				TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
				transferOrderMilestone = milestoneMessages(transferOrderMilestone);
				transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
				transferOrderMilestone.set("order_id", transferOrder.get("id"));
				transferOrderMilestone.set("status", "已入库");
				transferOrderMilestone.save();
    		}
    		
    		DepartOrder departOrder = DepartOrder.dao.findById(departOrderId);
    		departOrder.set("status", "已入库");
    		departOrder.update();
    		TransferOrderMilestone departOrderMilestone = new TransferOrderMilestone();
    		departOrderMilestone = milestoneMessages(departOrderMilestone);
    		departOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
    		departOrderMilestone.set("status", "已入库");
    		departOrderMilestone.set("depart_id", departOrder.get("id"));
    		departOrderMilestone.save();
    	}
    	DepartOrderController.productInWarehouse(departOrderId);
    	renderJson("{\"success\":true}");        
    }
    
    // 里程碑信息
    private TransferOrderMilestone milestoneMessages(TransferOrderMilestone transferOrderMilestone){
    	String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("order_id", getPara("order_id"));
    	return transferOrderMilestone;
    }
}
