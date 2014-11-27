package controllers.yh;

import models.Office;
import models.UserLogin;
import models.UserRole;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class RegisterUserController  extends Controller{
	//这个是记录操作日志的类
	private Logger logger = Logger.getLogger(LoginUserController.class);
	
	public void index(){
		render("/yh/profile/registerUser/register.html");
	}
	
	public void saveRegistrant(){
		
		String userName = getPara("username");
		String email=getPara("email");
		String officeName=getPara("officeName");
		String phone_name=getPara("contact_name");
		String phone=getPara("contact_phone");
		String againPassword=getPara("password");
		String office_register=getPara("officeRegister");
		//判断登陆名字步能为空
		Record user = new Record();
        user.set("user_name", userName);
        user.set("password", againPassword);
  
		//第一步，将公司的信息填入到office表中，判断公司名不为空和公司名不能重复
        if(office_register.equals("unregister")){
        	//保存注册公司
        	Record office = new Record();
        	office.set("OFFICE_NAME", officeName);
        	office.set("contact_phone_name",phone_name);
        	office.set("contact_phone", phone);
        	office.set("email", email);
        	office.set("type","总公司");
        	Db.save("office", office);
        	//根据名称查询到刚才保存的注册公司
        	Office user_office = Office.dao.findFirst("select * from office where office_name=?",officeName);
        	//将新注册公司的ID设值到注册用户中
        	user.set("office_id", user_office.get("id"));
        	
        	Db.save("user_login", user);
        	//给新公司第一个注册的人加入系统用户的角色
        	UserRole ur = new UserRole();
        	ur.set("user_name", userName);
        	ur.set("role_code", "admin");
        	ur.save();
        	//查询新注册用户
        	//UserLogin newUser = UserLogin.dao.findFirst("select * from user_login where user_name=?",userName);
        	setAttr("userId", userName);
        	
        	render("/yh/index.html");
        }else{
        	Db.save("user_login", user);
        	render("/yh/profile/registerUser/registerSuccess.html");
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
