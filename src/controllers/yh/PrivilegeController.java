package controllers.yh;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Privilege;
import models.Role;
import models.UserLogin;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class PrivilegeController extends Controller {
	private Logger logger = Logger.getLogger(PrivilegeController.class);

	public void index() {
		if (LoginUserController.isAuthenticated(this))
			render("profile/privilege/PrivilegeList.html");
	}

	public void privilegelist() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total FROM MODULES ";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		String sql_m = "SELECT * FROM MODULES  " + sLimit;

		// 获取当前页的数据
		List<Record> orders = Db.find(sql_m);
		for (int i = 0; i < orders.size(); i++) {

			String sql_mp = "SELECT m.* ,p.* FROM MODULES_PRIVILEGE   mp left join MODULES  m on m.id=mp.MODULE_ID left join PRIVILEGES  p on p.id=mp.PRIVILEGE_ID where mp.module_id='"
			        + orders.get(i).get("id") + "' ";
			// 获取模块对应的权限
			List<Record> module_p = Db.find(sql_mp);
			orders.get(i).set("module_p", module_p);

			// for (int j = 0; j < module_p.size(); j++) {
			// String m_pr = module_p.get(j).get("privilege");
			// orders.get(i).set("m_p" + j + " ", m_pr);
			// }
		}

		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);

	}

	public void editModule_pri() {
		String[] m_p_list = getParaValues("localArr");
		System.out.print(m_p_list);
	}

	public void userrole() {
		if (LoginUserController.isAuthenticated(this))
			render("profile/privilege/UserRole.html");
	}

	public void roleprivilege() {
		if (LoginUserController.isAuthenticated(this))
			render("profile/privilege/RolePrivilege.html");
	}

	public void SelectUser() {
		String select = getPara("select");

		if (select.equals("1")) {
			List<UserLogin> userjson = UserLogin.dao
			        .find("select * from user_login");
			renderJson(userjson);
		}

		if (select.equals("2")) {
			List<Role> rolejson = Role.dao.find("select * from role");
			renderJson(rolejson);
		}
		if (select.equals("3")) {
			List<Privilege> rolejson = Privilege.dao
			        .find("select * from privilege_table");
			renderJson(rolejson);
		}

	}
}
