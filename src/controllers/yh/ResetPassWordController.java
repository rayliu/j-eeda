package controllers.yh;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import models.UserLogin;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import com.jfinal.core.Controller;

import config.EedaConfig;

public class ResetPassWordController extends Controller{
	
	public void index(){
		render("/yh/profile/resetPassWord/resetPass.html");
	}
	public void input(){
		render("/yh/profile/resetPassWord/inputPass.html");
	}
	  // 使用common-email, javamail
    public void sendMail() throws Exception {
    	String userEmail = getPara("userEmail");
    	
    	  
        Email email = new SimpleEmail();
        /*smtp.exmail.qq.com*/
        email.setHostName("smtp.exmail.qq.com");
        email.setSmtpPort(465);
        
        /*输入公司的邮箱和密码*/
        /*EedaConfig.mailUser, EedaConfig.mailPwd*/
        email.setAuthenticator(new DefaultAuthenticator(EedaConfig.mailUser, EedaConfig.mailPwd));        
        email.setSSLOnConnect(true);
        
        /*EedaConfig.mailUser*/
        email.setFrom(EedaConfig.mailUser);//设置发信人
        email.setSubject("重置密码");
        
        
        int max=5;
        int min=10;
        Random random = new Random();

        int s = random.nextInt(max)%(max-min+1) + min;
        //随机数保存到数据库
        UserLogin userLogin = UserLogin.dao.findFirst("select * from user_login where user_name=?",userEmail);
        
        userLogin.set("token", s);
        userLogin.update();
        
        
       HttpServletRequest req = this.getRequest();
       String basePath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+"/reset/input?token="+s;

        email.setMsg("请点击下面超链接，完成重置密码工作  \t "+ basePath);
        email.addTo(userEmail);//设置收件人
      
        try{
        	email.send();
        	render("/yh/profile/resetPassWord/sendSuccess.html");
        }catch(Exception e){
        	e.printStackTrace();
        	render("/yh/profile/resetPassWord/sendError.html");
        }
        
       
    }
    public void checkEmailExist(){
		String userName= getPara("email");
		boolean checkObjectExist;
		
		UserLogin user = UserLogin.dao.findFirst(
                "select * from user_login where user_name=?", userName);
		if(user == null){
			checkObjectExist=false;
		}else{
			checkObjectExist=true;
		}
		renderJson(checkObjectExist);
	}
    public void resetUserPass(){
    	UserLogin user = UserLogin.dao.findFirst("select * from user_login where token=?", getPara("token"));
		user.set("password", getPara("pass"));
    	user.set("token", "");
    	user.update();
    	render("/yh/profile/resetPassWord/resetSuccess.html");
    	
    }
}
