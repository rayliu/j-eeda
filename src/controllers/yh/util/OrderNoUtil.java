package controllers.yh.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class OrderNoUtil {
	
	public static String getOrderNo(String sql,String head){
		String orderNo = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Record order = Db.findFirst(sql);
		if (order != null) {
			String num = "";
			if(order.get("order_no") != null)
				num = order.get("order_no");
			else
				num = order.get("depart_no");
			String str = num.substring(2, num.length());
			if(!str.matches("\\d*"))
				str = num.substring(4, num.length());
			Long oldTime = Long.parseLong(str);
			String format = sdf.format(new Date());
			String time = format + "00001";
			Long newTime = Long.parseLong(time);
			if (oldTime >= newTime) {
				orderNo = String.valueOf((oldTime + 1));
			} else {
				orderNo = String.valueOf(newTime);
			}
		} else {
			String format = sdf.format(new Date());
			orderNo = format + "00001";
		}
		return head+orderNo;
	}
}
