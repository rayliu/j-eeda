package controllers.yh;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import models.Permission;
import models.Role;
import models.RolePermission;
import models.UserLogin;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.PermissionConstant;

//@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PrivilegeController extends Controller {
	private Logger logger = Logger.getLogger(PrivilegeController.class);
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_LIST})
	public void index() {
		render("/yh/profile/privilege/PrivilegeList.html");
	}
	// //@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_LIST})
	//编辑和分配都是用这个list
	public void list() {
		
		String rolename = getPara("rolename");
		String code = null;
		if(rolename!=null){
			Role role = Role.dao.findFirst("select * from role where name=?",rolename);
			code = role.get("code");
		}
		
		
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
		
		
		totalWhere ="select count(*) total from permission p left join  (select * from role_permission rp where rp.role_code =?) r on r.permission_code = p.code";
		sql_m ="select p.code, p.name,p.module_name ,r.permission_code from permission p left join  (select * from role_permission rp where rp.role_code =?) r on r.permission_code = p.code";
		
		rec = Db.findFirst(totalWhere,code);
		
		// 获取当前页的数据
		orders = Db.find(sql_m  + sLimit,code);
		

		logger.debug("total records:" + rec.getLong("total"));
		
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);

	}
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_LIST})
	public void roleList() {
		String sql_m = "select distinct r.name,r.code from role_permission  rp left join role r on r.code =rp.role_code group by r.name,r.code";

		// 获取当前页的数据
		List<Record> orders = Db.find(sql_m);
		renderJson(orders);
	}
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_CREATE})
	public void add(){
		render("/yh/profile/privilege/RolePrivilege.html");
	}
	/*查找没有权限的角色*/
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_CREATE})
	public void seachNewRole(){
		String sql_m = "select r.code,r.name from role r left join role_permission rp on r.code = rp.role_code where rp.role_code is null";
		List<Record> orders = Db.find(sql_m);
		renderJson(orders);
	}
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_CREATE})
	public void save(){
		String rolename = getPara("name");
		String permissions = getPara("permissions");
		String[] ps = permissions.split(",");
		//根据角色名称找到角色代码
		Role role = Role.dao.findFirst("select * from role where name=?",rolename);
		for (String str : ps) {
			RolePermission r = new RolePermission();
			r.set("role_code", role.get("code"));
			r.set("permission_code", str);
			r.save();
		}
		renderJson();
	}
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_UPDATE})
	public void update(){
		String rolename = getPara("name");
		String permissions = getPara("permissions");
		String[] ps = permissions.split(",");
		//根据角色名称找到角色代码
		Role role = Role.dao.findFirst("select * from role where name=?",rolename);
		List<RolePermission> rp = RolePermission.dao.find("select * from role_permission where role_code =?",role.get("code"));
		for (RolePermission rolePermission : rp) {
			rolePermission.delete();
		}
		/*重新保存数据*/
		if(!permissions.equals("")){
			for (String str : ps) {
				RolePermission r = new RolePermission();
				r.set("role_code", role.get("code"));
				r.set("permission_code", str);
				r.save();
			}
		}
		renderJson();
	}
	 //@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_UPDATE})
	public void edit(){
		String rolename = getPara("rolename");
		setAttr("rolename", rolename);
		render("/yh/profile/privilege/RolePrivilegeEdit.html");
	}
}
