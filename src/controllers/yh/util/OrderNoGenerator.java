package controllers.yh.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import models.yh.profile.CustomizeField;

import org.apache.commons.lang.StringUtils;

import com.jfinal.log.Logger;

/**
 * generate Order number
 * 
 * All use one counter
 */
public class OrderNoGenerator {
    private static Logger logger = Logger.getLogger(OrderNoGenerator.class);
	
	private static volatile String count = "00000";
	private static String dateValue = "20110101";
	
	public final static Byte[] locks = new Byte[0];  
	//如果服务器重启了，当前的序列号就从数据库找到最后的号码，然后接着计数
	//TODO：如果需要按每张单的前缀来生成序列号，可以多加一个Map来记录
	public static String getNextOrderNo(String orderPrefix) {
		if("00000".equals(count)){
			initCountFromDB();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String nowdate = sdf.format(new Date());
	    //日期不同, 则从新计数
        if(!nowdate.equals(dateValue)){
            dateValue=nowdate;
            count = "00000";
        }
        String orderNo="";
        synchronized (locks){	
            orderNo = orderPrefix +nowdate+ getNo(count);
            
            CustomizeField cf = CustomizeField.dao.findFirst("update customize_field set field_code = ? where order_type='latestOrderNo'", orderNo);
//            if(cf!=null){
//		        logger.debug("orderNo:"+orderNo);
//		        cf.set("field_code", orderNo).update();
//	        }
		}
		return orderNo;
	}

	public synchronized static void initCountFromDB() {
		String previousNo="";
		CustomizeField cf = CustomizeField.dao.findFirst("select * from customize_field where order_type='latestOrderNo'");
		if(cf!=null){
			previousNo = cf.get("field_code");
			//不管前缀长度，后面的数字长度是 13， 2011010100001
			String ymd = StringUtils.right(previousNo, 13).substring(0, 8); // 获取年月日字符串
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String nowdate = sdf.format(new Date());
			dateValue = nowdate;
			if(ymd.equals(nowdate)){//如果年月日 =今天， 获取流水号
				count = StringUtils.right(previousNo, 5); // 获取流水号
			}
		}
	}

	/**
	 * 返回当天的订单数+1
	 */
	private static synchronized String getNo(String s) {
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
