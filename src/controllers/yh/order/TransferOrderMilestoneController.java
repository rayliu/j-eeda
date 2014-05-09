package controllers.yh.order;

import java.util.List;

import models.TransferOrderMilestone;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;

public class TransferOrderMilestoneController extends Controller {

	private Logger logger = Logger.getLogger(TransferOrderMilestoneController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void transferOrderMilestoneList(){
		String order_id = getPara("order_id");
		List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao.find("select * from transfer_order_milestone where order_id="+order_id);
		renderJson(transferOrderMilestones);
	}
}
