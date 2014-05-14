package controllers.yh;

import java.util.List;

import models.Privilege;
import models.Role;
import models.UserLogin;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;


public class PrivilegeController extends Controller {
	public void index(){
		if(LoginUserController.isAuthenticated(this))
		render("profile/privilege/PrivilegeList.html");
	}
	
	public void userrole(){
		if(LoginUserController.isAuthenticated(this))
		render("profile/privilege/UserRole.html");
	}
	public void roleprivilege(){
		if(LoginUserController.isAuthenticated(this))
		render("profile/privilege/RolePrivilege.html");
	}
	public void SelectUser(){
		String select=getPara("select");
		
		if(select.equals("1")){
			List<UserLogin> userjson = UserLogin.dao.find("select * from user_login");			
	        renderJson(userjson);
		}
		
		if(select.equals("2")){
			List<Role> rolejson = Role.dao.find("select * from role_table");			
	        renderJson(rolejson);
		}
		if(select.equals("3")){
			List<Privilege> rolejson = Privilege.dao.find("select * from privilege_table");			
	        renderJson(rolejson);
		}
		
	}
}
