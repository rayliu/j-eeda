package controllers.yh.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class OrderNoUtil {
	
	public static String getOrderNo(String tableName,String type){
		String orderNo = "";
		Record order = null;
		String num = "";
		if(type != null){
			if("depart_order".equals(tableName)){
				order = Db.findFirst("select * from " + tableName + " and combine_type = '" + type + "' order by id desc limit 0,1");
				if (order != null) 
					num = order.get("depart_no");
			}else if("warehouse_order".equals(tableName)){
				order = Db.findFirst("select * from warehouse_order where order_type ='" + type +"' order by id desc limit 0,1");
				if (order != null) 
					num = order.get("order_no");
			}
		}else{
			order = Db.findFirst("select * from " + tableName + " order by id desc limit 0,1");
			if (order != null) 
				num = order.get("order_no");
		}
		if (order != null) {
			String str = num.substring(2, num.length());
			System.out.println(str);
			Long oldTime = Long.parseLong(str);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String format = sdf.format(new Date());
			String time = format + "00001";
			Long newTime = Long.parseLong(time);
			if (oldTime >= newTime) {
				orderNo = String.valueOf((oldTime + 1));
			} else {
				orderNo = String.valueOf(newTime);
			}
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String format = sdf.format(new Date());
			orderNo = format + "00001";
		}
		return orderNo;
	}
}
