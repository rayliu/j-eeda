package controllers.yh;


import java.text.SimpleDateFormat;
import java.util.Date;

import models.UserLogin;
import models.UserOffice;
import models.UserRole;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import config.DataInitUtil;
import config.EedaConfig;

public class RegisterUserController  extends Controller{
	//这个是记录操作日志的类
	private Logger logger = Logger.getLogger(LoginUserController.class);
	
	public void index(){
		render("/yh/profile/registerUser/register.html");
	}
	@Before(Tx.class)
	public void saveRegistrant(){
		
		String userName = getPara("username");
		String email=getPara("email");
		String officeName=getPara("officeName");
		String phone_name=getPara("contact_name");
		String phone=getPara("contact_phone");
		String againPassword=getPara("password");
		//判断登陆名字步能为空
		
		//第一步，将公司的信息填入到office表中，判断公司名不为空和公司名不能重复
        	//保存网点（公司）
        	Record office = new Record();
        	office.set("OFFICE_NAME", officeName);
        	office.set("office_person",phone_name);
        	office.set("phone", phone);
        	office.set("email", email);
        	office.set("type", "总公司");
        	Db.save("office", office);
        	//将当前公司名称注册
        	Record office_config = new Record();
        	office_config.set("office_id", office.get("id"));
        	office_config.set("system_title", officeName);
        	Db.save("office_config", office_config);
        	
        	//将新注册公司的ID设值到注册用户中
        	Record user = new Record();
            user.set("user_name", userName);
            user.set("password", againPassword);
        	user.set("office_id", office.get("id"));
        	Db.save("user_login", user);
        	//初始化数据
        	DataInitUtil.initBaseData(office,user);   
        	
        	//给当前默认系统管理员系统管理员角色
        	UserRole userRole = new UserRole();
        	userRole.set("user_name", userName);
        	userRole.set("role_code", "admin");
        	userRole.save();
        	
        	UserOffice uo = new  UserOffice();
        	uo.set("user_name",userName);
        	uo.set("office_id", office.get("id"));
        	uo.set("is_main", true);
        	uo.save();
        	
        	
        	
        	//注册成功
        	 Email emailTo = new SimpleEmail();
        	 emailTo.setHostName("smtp.exmail.qq.com");
        	 emailTo.setSmtpPort(465);
             
             /*输入公司的邮箱和密码*/
             /*EedaConfig.mailUser, EedaConfig.mailPwd*/
        	 emailTo.setAuthenticator(new DefaultAuthenticator(EedaConfig.mailUser, EedaConfig.mailPwd));        
        	 emailTo.setSSLOnConnect(true);
             
        	 try{
        		/*EedaConfig.mailUser*/
	        	emailTo.setFrom(EedaConfig.mailUser);//设置发信人
	        	emailTo.setSubject("新公司注册");
	            Date date = new Date();
	            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	            String newDate = sf.format(date);
	            String basePath =newDate + "  " + userName + "用户在平台上注册" + officeName + "的一个总公司";
	
	            emailTo.setMsg(basePath);
	            /*添加邮件收件人*/
	            emailTo.addTo("ray_liu@eeda123.com");//设置收件人
	            emailTo.addTo("kate.lin@eeda123.com");
            	emailTo.send();
             	
             }catch(Exception e){
             	e.printStackTrace();
             	
             }finally{
            	//保存成功后登录
             	forwardAction("/login");
             }
		
	}
	
	public void checkUserNameExist(){
		String userName= getPara("username");
		boolean checkObjectExist;

		UserLogin user = UserLogin.dao.findFirst(
                "select * from user_login where user_name=?", userName);
		if(user == null){
			checkObjectExist=true;
		}else{
			checkObjectExist=false;
		}
		renderJson(checkObjectExist);
	}
	
	public void checkOfficeNameExist(){
		String officeName = getPara("officeName");
		Record office = Db.findFirst("select * from office where office_name=?",officeName);
		boolean checkObjectExist;
		if(office == null){
			checkObjectExist=true;
		}else{
			checkObjectExist=false;
		}
		renderJson(checkObjectExist);
	}
}
