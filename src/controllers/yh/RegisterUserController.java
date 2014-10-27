package controllers.yh;

import com.jfinal.core.Controller;

public class RegisterUserController  extends Controller{
	public void index(){
		render("/yh/register.html");
	}
	public void saveRegistrant(){
		String userName = getPara("username");
		String email=getPara("email");
		String officeName=getPara("officeName");
		String phone_name=getPara("contact_phone_name");
		String phone=getPara("contact_phone");
		String againPassword=getPara("againPassword");
		String office_register=getPara("office_register");
		/*
		 * 第一步判断公司名是否注册
		 * 第二步判断输入数据增加到数据库中
		 * */
		if(office_register.equals("register")){
			
		}
		render("/yh/index.html");
	}
}
