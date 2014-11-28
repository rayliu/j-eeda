package controllers.yh.returnOrder;

import java.util.Calendar;
import java.util.Date;

import models.DepartTransferOrder;
import models.ReturnOrder;
import models.TransferOrder;
import models.UserLogin;
import models.yh.delivery.DeliveryOrder;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import controllers.yh.util.OrderNoUtil;


public class CreatReturnOrder{
	static Subject currentUser = SecurityUtils.getSubject();
	//生成回单方法
	public static boolean CreatOrder(String type,String order_id){
		int id=Integer.parseInt(order_id);//单据id
		String name = (String) currentUser.getPrincipal();//当前用户
	    UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
	    Date createDate = Calendar.getInstance().getTime();//当前时间
	    String orderNo = OrderNoUtil.getOrderNo("return_order", null);
	    ReturnOrder re=new ReturnOrder();
		re.set("order_no", "HD" + orderNo);
		re.set("create_date", createDate);
		re.set("transaction_status", "new");
		re.set("creator",Integer.parseInt(users.get("id").toString()));
	    if(ReturnOrder.Depart_Order.equals(type)){
	    	DepartTransferOrder det=DepartTransferOrder.dao.findFirst("select * from depart_transfer where depart_id="+id+"");
			TransferOrder trf=TransferOrder.dao.findFirst("select * from transfer_order where id="+Integer.parseInt(det.get("order_id").toString())+"");
	    	re.set("depart_order_id", id);
			re.set("notity_party_id",Integer.parseInt(trf.get("notify_party_id").toString()));
			re.set("customer_id",Integer.parseInt(trf.get("customer_id").toString()));
		}else if(ReturnOrder.Delivery_Order.equals(type)){
			DeliveryOrder de=DeliveryOrder.dao.findById(id);
			re.set("delivery_order_id", id);
			re.set("notity_party_id",Integer.parseInt(de.get("notify_party_id").toString()));
			re.set("customer_id",Integer.parseInt(de.get("customer_id").toString()));
		}
	   boolean check =re.save();
		return check;
	}
}
