package controllers.yh;

import com.jfinal.core.Controller;

public class RoleController extends Controller {
public  void  index(){
	render("profile/RoleList.html");
}
public void editRole(){
	render("profile/RoleEdit.html");
}

public void cancelRole(){
	render("index.html");
}
}
