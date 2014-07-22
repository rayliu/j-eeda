package controllers.yh.returnorder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import models.DepartTransferOrder;
import models.ReturnOrder;
import models.TransferOrder;
import models.UserLogin;
import models.yh.delivery.DeliveryOrder;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;


public class CreatReturnOrder{
	static Subject currentUser = SecurityUtils.getSubject();
	// 构造单号
	public static String creatOrderNo() {
	    String order_no = null;
	    String the_order_no = null;
	    ReturnOrder order = ReturnOrder.dao.findFirst("select * from return_order "
	            + " order by id desc limit 0,1");
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
//生成回单方法
	public static boolean CreatOrder(String type,String order_id){
				int id=Integer.parseInt(order_id);//单据id
				String name = (String) currentUser.getPrincipal();//当前用户
			    UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
			    Date createDate = Calendar.getInstance().getTime();//当前时间
			    String order_no=creatOrderNo();
			    ReturnOrder re=new ReturnOrder();
				re.set("order_no", order_no);
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
