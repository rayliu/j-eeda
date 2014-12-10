package controllers.yh.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.UserLogin;
import models.UserOffice;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class UserOfficeController extends Controller{
	public void index(){
		render("/yh/profile/office/userOfficeList.html");
	}
	public void list(){
		//查询某个用户属于那些网点
		String username = getPara("username");
		if(username==null||"".equals(username)){
			Map orderMap = new HashMap();
			orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
	        return;
		}
		List<Record> rlist = Db.find("select o.office_code,o.office_name,o.address from user_office uo "
				+ "left join office o on o.id=uo.office_id "
				+ "left join user_login ul on ul.user_name = uo.user_name "
				+ "where user_name=? or office.id=ul.office_id",username);
		
		Map orderMap = new HashMap();
		orderMap.put("aaData", rlist);
		renderJson(orderMap);
	}
	public void selectUser(){
		String input = getPara("locationName");
		String sql ="select * from user_login where office_id is not null";
		if(input!=null&&input!=""){
			sql=sql +" and user_name like '%"+input+"%'";
		}
		
		List<UserLogin> ulist = UserLogin.dao.find(sql);
		renderJson(ulist);
	}
	
	public void addNetwork(){
		render("/yh/profile/office/addUserOffice.html");
	}
	public void seachUser(){
		String input = getPara("locationName");
		String sql ="select user_name from user_login where office_id is null";
		if(input!=null&&input!=""){
			sql=sql +" and user_name like '%"+input+"%'";
		}
		
		List<UserLogin> ulist = UserLogin.dao.find(sql);
		renderJson(ulist);
	}
	public void seachOffice(){
		List<Office> offices = Office.dao.find("select * from office");
		renderJson(offices);
	}
	public void queryOffice(){
		/*分页*/
		String officeIds = getPara("offices");
		//String office[] = officeIds.split(",");
		if(officeIds==null || "".equals(officeIds)){
			Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
		}
		
		List<Record> offices = Db.find("select * from office where id in("+officeIds+")");
		
		Map orderMap = new HashMap();
		orderMap.put("aaData", offices);
		
		renderJson(orderMap);
	}
	public void save(){
		String username = getPara("username");
		String officeIds = getPara("officeIds");
		String ids[] = officeIds.split(",");
		boolean isSuccess=false;
		if(username!=null&&officeIds!=null){
			for (int i = 0; i < ids.length; i++) {
				UserOffice uo = new UserOffice();
				uo.set("user_name", username);
				uo.set("office_id", ids[i]);
				uo.save();
			}
			isSuccess=true;
		}
		renderJson(isSuccess);
	}
	public void checkUserNameExist(){
		String userName= getPara("user_filter");
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
}
