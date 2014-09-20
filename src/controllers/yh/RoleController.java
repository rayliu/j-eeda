package controllers.yh;import java.util.Calendar;import java.util.Date;import java.util.HashMap;import java.util.List;import java.util.Map;import models.Role;import com.jfinal.core.Controller;import com.jfinal.log.Logger;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;public class RoleController extends Controller {	private Logger logger = Logger.getLogger(RoleController.class);	public void index() {		if (LoginUserController.isAuthenticated(this))			render("/yh/profile/RoleList.html");	}	public void list() {		String sLimit = "";		String pageIndex = getPara("sEcho");		if (getPara("iDisplayStart") != null		        && getPara("iDisplayLength") != null) {			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "			        + getPara("iDisplayLength");		}		// 获取总条数		String totalWhere = "";		String sql = "select count(1) total from role ";		Record rec = Db.findFirst(sql + totalWhere);		logger.debug("total records:" + rec.getLong("total"));		// 获取当前页的数据		List<Record> orders = Db.find("select * from role");		Map orderMap = new HashMap();		orderMap.put("sEcho", pageIndex);		orderMap.put("iTotalRecords", rec.getLong("total"));		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));		orderMap.put("aaData", orders);		renderJson(orderMap);	}	// 点击创建用户角色	public void addRole() {		if (LoginUserController.isAuthenticated(this))			render("/yh/profile/RoleAdd.html");	}	// 点击编辑	public void ClickRole() {		String id = getPara();		if (id != null) {			Role h = Role.dao.findById(id);			setAttr("hh", h);			if (LoginUserController.isAuthenticated(this))				render("/yh/profile/RoleEdit.html");		}	}	// 点击创建角色保存	public void SaveRole() {		Role r = new Role();		String name = getPara("rolename");		Date time = Calendar.getInstance().getTime();		String people = getPara("rolepeople");		boolean b = r.set("role_name", name).set("role_time", time)		        .set("role_people", people).save();		if (b == true) {			if (LoginUserController.isAuthenticated(this))				render("/yh/profile/RoleList.html");		}	}	// 点击编辑保存	public void editRole() {		String id = getPara("id");		String rolename = getPara("rolename");		String uppeople = getPara("roleuppeople");		Date uptime = Calendar.getInstance().getTime();		Role role = Role.dao.findById(id);		boolean b = role.set("role_name", rolename)		        .set("role_lasttime", uptime).set("role_lastpeople", uppeople)		        .update();		if (LoginUserController.isAuthenticated(this))			render("/yh/profile/RoleList.html");	}	// 删除	public void deleteRole() {		String id = getPara();		if (id != null) {			Role l = Role.dao.findById(id);			l.delete();		}		if (LoginUserController.isAuthenticated(this))			redirect("/role");	}}