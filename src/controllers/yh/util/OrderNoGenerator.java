package controllers.yh.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * generate Order number
 * 
 */
public class OrderNoGenerator {
	
	
	private static String count = "00000";
	private static String dateValue = "20110101";
	//TODO：如果需要按每张单的前缀来生成序列号，可以多加一个Map来记录
	
	public synchronized static String getNextOrderNo(String orderPrefix) {
		long No = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String nowdate = sdf.format(new Date());
		No = Long.parseLong(nowdate);
		if (!(String.valueOf(No)).equals(dateValue)) {
			count = "00000";
			dateValue = String.valueOf(No);
		}
		String num = String.valueOf(No);
		num += getNo(count);
		num = orderPrefix + num;
		return num;
	}


	/**
	 * 返回当天的订单数+1
	 */
	private static String getNo(String s) {
		String rs = s;
		int i = Integer.parseInt(rs);
		i += 1;
		rs = "" + i;
		int seqLength = 5;//序列号长度00001
		for (int j = rs.length(); j < seqLength; j++) {
			rs = "0" + rs;
		}
		count = rs;
		return rs;
	}

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			System.out.println(getNextOrderNo("YS"));
		}
		for (int i = 0; i < 10; i++) {
			System.out.println(getNextOrderNo("CB"));
		}
	}

}
