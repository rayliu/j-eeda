package controllers.yh.order;

import java.util.Date;
import java.util.List;

import models.TransferOrderMilestone;
import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;

public class TransferOrderMilestoneController extends Controller {

	private Logger logger = Logger.getLogger(TransferOrderMilestoneController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void transferOrderMilestoneList(){
		String order_id = getPara("order_id");
		List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao.find("select * from transfer_order_milestone where order_id="+order_id+" order by create_stamp asc");
		renderJson(transferOrderMilestones);
	}
	
	// 确认发车
	public void departureConfirmation(){
		TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
		transferOrderMilestone.set("status", "已发车");
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
		transferOrderMilestone.set("create_by", users.get(0).get("id"));
		transferOrderMilestone.set("create_stamp", new Date());
		transferOrderMilestone.set("location", "");
		transferOrderMilestone.set("order_id", getPara("order_id"));
		transferOrderMilestone.save();
		renderJson(transferOrderMilestone);
	}
}
