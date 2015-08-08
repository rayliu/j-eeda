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
import controllers.yh.util.OrderNoGenerator;
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

    //TODO:  直送运输单-收货确认：不可以多次提货，整单发车
    /**
     * 直送时要判断是什么单据，如果是退货单，要明确是入仓还是直送到客户工厂，其中入仓的需要增加库存
     */
    
    public void receipt() {
        //Long order_id = Long.parseLong(getPara("orderId"));
        Long departOrderId = Long.parseLong(getPara("departOrderId"));
        //TransferOrder transferOrder = TransferOrder.dao.findById(order_id);
        //通过发车单获取运输单ID
        List<Record> transferOrderIds = Db.find("select order_id from depart_transfer where depart_id = ? ;",departOrderId);
        
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        //判断当前直送的模式，是入中转仓还是直接发送给客户工厂，如果是入中转仓，增加库存
        DepartOrderController.productInWarehouse(getPara("departOrderId"));
        //修改发车单信息
        DepartOrder departOrder = DepartOrder.dao.findById(departOrderId);
        departOrder.set("status", "已收货").update();
       
        for (Record record : transferOrderIds) {
        	//获取运输单ID
        	long transerOrderId = record.getLong("order_id");
        	 //直接生成回单，在把合同等费用带到回单中
            String orderNo = OrderNoGenerator.getNextOrderNo("HD");
            ReturnOrder returnOrder = new ReturnOrder();
            returnOrder.set("order_no", orderNo)
            .set("transaction_status", "新建")
            .set("creator", users.get(0).get("id"))
            .set("create_date", sqlDate)
            //回单中"transfer_order_id"字段修改为“depart_id”
            //.set("depart_id", departOrderId)
            .set("transfer_order_id", transerOrderId).save();
        	
            //修改运输单信息
        	TransferOrder transferOrder = TransferOrder.dao.findById(transerOrderId);
			transferOrder.set("status", "已收货").update();
			//设置回单客户信息，必须是同一个客户
            returnOrder.set("customer_id", transferOrder.get("customer_id")).update();
            
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "已收货")
            .set("create_by", users.get(0).get("id"))
            .set("location", "")
            .set("create_stamp", sqlDate)
            .set("order_id", transerOrderId)
            .set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE)
            .save();

            transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "已收货")
            .set("create_by", users.get(0).get("id"))
            .set("location", "")
            .set("create_stamp", sqlDate)
            .set("depart_id", departOrderId)
            .set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE)
            .save();
          
            
            
            ReturnOrderController roController= new ReturnOrderController(); 
            //把运输单的应收带到回单中
            roController.tansferIncomeFinItemToReturnFinItem(returnOrder, Long.parseLong("0"), transerOrderId);
            //直送时把保险单费用带到回单
            roController.addInsuranceFin(transferOrder, departOrder, returnOrder);
            //TODO:根据合同生成费用
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
	       
	        /*if("arrangementOrder".equals(transferOrder.get("order_type"))){*/
	        
	        DepartOrderController dor = new DepartOrderController();
	        dor.SubtractInventory(departTransferOrder,departOrderId);
	    	/*}*/
	       
        }
        
        renderJson("{\"success\":true}");        
    }
    
    //TODO:  入库确认
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
