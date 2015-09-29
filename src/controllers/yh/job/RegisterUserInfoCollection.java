package controllers.yh.job;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import config.EedaConfig;

/**
 * @author Administrator 收集一个礼拜内新用户注册的信息,每周五下午5:30分发送邮件
 */
public class RegisterUserInfoCollection implements Runnable {
	private Logger logger = Logger.getLogger(RegisterUserInfoCollection.class);
	@Override
	public void run()  {

		SimpleDateFormat simpleDate = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dayDate = new SimpleDateFormat("yyyy-MM-dd");
		Calendar pastDay = Calendar.getInstance();
		pastDay.add(Calendar.DAY_OF_WEEK, -7);
		String beginTime = dayDate.format(pastDay.getTime());
		String endTime = simpleDate.format(Calendar.getInstance().getTime());
		try {
			// 查询数据
			List<Record> recList = Db.find("select o.id ,u.user_name ,u.password , o.office_name ,o.office_person ,o.phone ,u.last_login from office o "
					+ "left join user_login u on u.office_id = o.id where o.type = '总公司' and o.create_stamp between '"+ beginTime + "' and '" + endTime + "'");
			// 生成execl文件保存至/WebRoot/upload
			createExecl(recList,beginTime,endTime);
			// 注册成功
			MultiPartEmail email = new MultiPartEmail();
		   	//Email email = new SimpleEmail();
		   	email.setHostName("smtp.exmail.qq.com");
		   	email.setSmtpPort(465);
	        
	        // 输入公司的邮箱和密码
		   	email.setAuthenticator(new DefaultAuthenticator(EedaConfig.mailUser, EedaConfig.mailPwd));        
		   	email.setSSLOnConnect(true);
	        
	   		// 设置发信人
	   		email.setFrom(EedaConfig.mailUser);
	   		// 设置主题
	   		email.setSubject(beginTime+"至"+endTime+"易达物流系统注册信息");
	   		// 设置正文内容
	   		email.setMsg("易达物流系统注册信息，从"+beginTime+"至"+endTime+"注册易达系统的用户及公司信息见附件！");
	   		// 要发送的附件    
	        EmailAttachment attachment = new EmailAttachment();    
	        File file = new File(System.getProperty("user.dir")+"\\WebRoot\\upload\\eeda_register_info.xls");    
	        attachment.setPath(file.getPath());    
	        attachment.setName(file.getName());    
	        // 设置附件描述    
	        attachment.setDescription("易达物流"+beginTime+"至今的注册用户信息");    
	        // 设置附件类型    
	        attachment.setDisposition(EmailAttachment.ATTACHMENT);  
	        // 添加邮件附件   
	        email.attach(attachment);
	   		// 添加邮件收件人
	   		email.addTo("ray_liu@eeda123.com");//设置收件人
	   		email.addTo("kate.lin@eeda123.com");
       		email.send();
       		logger.debug("邮件已发送!");
		} catch (Exception e) {
			logger.debug("发送邮件出错了!");
			e.printStackTrace();
		}
	}
	
	/*
	 * 把数据库中的字段导入到Excel ，并生成Excel文档
	 */
	public void createExecl(List<Record> dataList,String beginTime,String endTime) throws Exception {
		// 第一步，创建一个webbook，对应一个Excel文件
		HSSFWorkbook wb = new HSSFWorkbook();
		// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
		HSSFSheet sheet = wb.createSheet("易达物流注册信息");
		// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
		HSSFRow row = sheet.createRow((int) 0);
		// 第四步，创建单元格，并设置值表头 设置表头居中
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式

		HSSFCell cell = row.createCell((short) 0);
		cell.setCellValue("注册邮箱");
		cell.setCellStyle(style);
		cell = row.createCell((short) 1);
		cell.setCellValue("密码");
		cell.setCellStyle(style);
		cell = row.createCell((short) 2);
		cell.setCellValue("公司名称");
		cell.setCellStyle(style);
		cell = row.createCell((short) 3);
		cell.setCellValue("联系人");
		cell.setCellStyle(style);
		cell = row.createCell((short) 4);
		cell.setCellValue("联系电话");
		cell.setCellStyle(style);
		cell = row.createCell((short) 5);
		cell.setCellValue("最后一次登录时间");
		cell.setCellStyle(style);

		// 第五步，写入实体数据 实际应用中这些数据从数据库得到，
		for (int i = 0; i < dataList.size(); i++) {
			row = sheet.createRow((int) i + 1);
			// 第四步，创建单元格，并设置值
			row.createCell((short) 0).setCellValue(
					dataList.get(i).get("user_name") == null ? "" : dataList
							.get(i).get("user_name").toString());
			row.createCell((short) 1).setCellValue(
					dataList.get(i).get("password") == null ? "" : dataList
							.get(i).get("password").toString());
			row.createCell((short) 2).setCellValue(
					dataList.get(i).get("office_name") == null ? "" : dataList
							.get(i).get("office_name").toString());
			row.createCell((short) 3).setCellValue(
					dataList.get(i).get("office_person") == null ? ""
							: dataList.get(i).get("office_person").toString());
			row.createCell((short) 4).setCellValue(
					dataList.get(i).get("phone") == null ? "" : dataList.get(i)
							.get("phone").toString());
			row.createCell((short) 5).setCellValue(
					dataList.get(i).get("last_login") == null ? "" : dataList
							.get(i).get("last_login").toString());
		}
		// 第六步，将文件存到指定位置
		try {
			logger.debug("路径:"+this.getClass().getResource("/").getPath());
			logger.debug("项目路径:"+System.getProperty("user.dir"));
			
			//FileOutputStream fout = new FileOutputStream("E:/易达注册信息.xls");
			FileOutputStream fout = new FileOutputStream(System.getProperty("user.dir")+"\\WebRoot\\upload\\eeda_register_info.xls");
			wb.write(fout);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
