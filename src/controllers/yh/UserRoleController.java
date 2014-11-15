package controllers.yh;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Role;
import models.UserRole;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.CompareStrList;
import controllers.yh.util.PermissionConstant;
//@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class UserRoleController extends Controller {
	private Logger logger = Logger.getLogger(PrivilegeController.class);
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_UR_LIST})
	public void index(){
		render("/yh/profile/userRole/userRolelist.html");
	}
	
	/*查询用户角色*/
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_UR_LIST})
	public void list(){
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}
		
		
		// 获取总条数
		String totalWhere ="select count(1) total from user_role";
		
		String sql = "select ur.user_name,group_concat(r.name separator '<br>') name,ur.remark from user_role ur left join role r on r.code=ur.role_code group by ur.user_name" + sLimit;

		Record rec = Db.findFirst(totalWhere);	
		logger.debug("total records:" + rec.getLong("total"));
		// 获取当前页的数据
		List<Record> orders = Db.find(sql + sLimit);

		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}
	/*编辑*/
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_UR_UPDATE})
	public void edit(){
		String user_name = getPara("username");
		setAttr("user_name", user_name);		
		render("/yh/profile/userRole/assigning_roles.html");
	}
	
	/*给新用户分配角色*/
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_UR_CREATE})
	public void add(){
		render("/yh/profile/userRole/addRole.html");
	}
	/*列出没有角色的用户*/
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_UR_CREATE})
	public void userList(){
        List<Record> orders = Db.find("select u.*, ur.role_code from user_login u left join user_role ur on u.user_name = ur.user_name where ur.role_code is null");
        renderJson(orders);
	}
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_UR_CREATE})
	public void saveUserRole(){
		String name = getPara("name");
		String r = getPara("roles");
		String[] roles = r.split(",");
		for (String id : roles) {
			UserRole ur = new UserRole();
			ur.set("user_name", name);
			/*根据id找到Role*/
			Role role = Role.dao.findFirst("select * from role where id=?",id);
			ur.set("role_code", role.get("code"));
			ur.save();
		}
		renderJson();
	}
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_UR_UPDATE})
	public void updateRole(){
		String name = getPara("name");
		String r = getPara("roles");
		
		String[] roles = r.split(",");
		//先删除所有的数据，在保存
		//旧 [1，2，3，4，5]，  新的[3,5,6]
		//比较两个数组，delet [1,2,4], add [6]
		//旧数据
		List<UserRole> list = UserRole.dao.find("select id from user_role where user_name=?",name);
		
		List<Object> ids = new ArrayList<Object>();
		for (UserRole ur : list) {
			ids.add(ur.get("id"));
		}
		
		CompareStrList compare = new CompareStrList();
		
		List returnList = compare.compare(ids, roles);
		
		ids = (List<Object>) returnList.get(0);
		List<String> saveList = (List<String>) returnList.get(1);
		
		for (Object id : ids) {
			UserRole.dao.findFirst("select * from user_role where id=?", id).delete();
		}
		
		for (Object object : saveList) {
			UserRole ur = new UserRole();
			ur.set("user_name", name);
			/*根据id找到Role*/
			Role role = Role.dao.findFirst("select * from role where id=?",object);
			ur.set("role_code", role.get("code"));
			ur.save();
		}
		
		/*for (UserRole userRole : list) {
			userRole.delete();
		}
		if(!r.equals("")){
			for (String id : roles) {
				UserRole ur = new UserRole();
				ur.set("user_name", name);
				根据id找到Role
				Role role = Role.dao.findFirst("select * from role where id=?",id);
				ur.set("role_code", role.get("code"));
				ur.save();
			}
		}*/
		
		renderJson();
	}
	public void roleList() {
		//获取选中的用户
		String username = getPara("username");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		// 获取总条数

		Record rec = Db.findFirst("select count(1) total from role r left join  (select u.user_name,u.role_code from user_role u where u.user_name =?) ur on ur.role_code = r.code",username);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db.find("select r.id , r.code,r.name,ur.user_name ,ur.role_code from role r left join  (select u.user_name,u.role_code from user_role u where u.user_name =?) ur on ur.role_code = r.code",username);
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);

	}
	public void userPermissionRender(){
		String username = getPara("username");
		setAttr("username", username);
		render("/yh/profile/userRole/userPermission.html");
	}
	//查询用户的权限
	public void permissionList(){
		/*获取到用户的名称*/
		String username = getPara("username");
		
		//根据用户的名称查到用户的所有角色，然后根据角色查到权限
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		// 获取总条数
		String totalWhere = "";
		String sql_m ="";
		Record rec=null;
		List<Record> orders=null;
		
		
		totalWhere ="select distinct count(*) total from permission p left join (select rp.* from user_role  ur left join role_permission  rp on rp.role_code = ur.role_code where ur.user_name =?) r on r.permission_code = p.code ";
		
		rec = Db.findFirst(totalWhere,username);
		
		// 获取当前页的数据
		orders = Db.find("select distinct p.id, p.code, p.name,p.module_name ,r.permission_code from permission p left join (select rp.* from user_role  ur left join role_permission  rp on rp.role_code = ur.role_code where ur.user_name =?) r on r.permission_code = p.code order by p.id" + sLimit,username);

		logger.debug("total records:" + rec.getLong("total"));
		
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}
	
}
