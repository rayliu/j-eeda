package controllers.yh;
import javax.servlet.http.HttpServletRequest;

import models.UserLogin;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.eclipse.jetty.server.Request;

import com.jfinal.core.Controller;

public class ResetPassWordController extends Controller{
	public void index(){
		String input = getPara("input");
		if(input==null){
			render("/yh/profile/resetPassWord/resetPass.html");
		}else{

			render("/yh/profile/resetPassWord/inputPass.html");
		}
		
	}
	  // 使用common-email, javamail
    public void sendMail() throws Exception {
    	String userEmail = getPara("userEmail");
    	
    	  
        Email email = new SimpleEmail();
        /*smtp.exmail.qq.com*/
        email.setHostName("smtp.exmail.qq.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("red.luo@eeda123.com", "luo0330"));
        email.setSSLOnConnect(true);
        
        email.setFrom(userEmail);//设置发信人
        email.setSubject("忘记密码");
        
       HttpServletRequest req = this.getRequest();
       String basePath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+"/reset/index?input=1";

        email.setMsg("请点击下面超连接，完成重置密码工作  \t "+ basePath);
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
    	UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=?", getPara("email"));
    	user.set("password", getPara("pass"));
    	user.update();
    	render("/yh/profile/resetPassWord/resetSuccess.html");
    }
}
