package controllers.yh.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.TransferOrder;
import models.TransferOrderMilestone;
import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;

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
        if(order_id != "-1" && pickupOrderId == "-1"){
	        List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
	                .find("select * from transfer_order_milestone where type = '"+TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE+"' and order_id=" + order_id);
	        for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
	            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
	            String username = userLogin.get("user_name");
	            usernames.add(username);
	        }
	        map.put("transferOrderMilestones", transferOrderMilestones);
	        map.put("usernames", usernames);
        }else{
        	List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
	                .find("select * from transfer_order_milestone where type = '"+TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE+"' and pickup_id=" + pickupOrderId);
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
    	if(order_id != null && !"".equals(order_id)){
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
    	}else if(milestonePickupId != null && !"".equals(milestonePickupId)){
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
	
	        map.put("transferOrderMilestone", transferOrderMilestone);
	        UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
	        String username = userLogin.get("user_name");
	        map.put("username", username);
    	}else if(milestoneDepartId!=null&&!"".equals(milestoneDepartId)){
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
    	Long order_id = Long.parseLong(getPara("order_id"));
    	TransferOrder transferOrder = TransferOrder.dao.findById(order_id);
    	transferOrder.set("status", "已签收");
    	transferOrder.update();
        Map<String, Object> map = new HashMap<String, Object>();
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        transferOrderMilestone.set("status", "已签收");
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
