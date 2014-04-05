package config;

import models.Party;
import models.PartyAttribute;
import models.UserLogin;
import models.eeda.Case;
import models.eeda.Leads;
import models.eeda.Order;
import models.eeda.OrderItem;

import org.bee.tl.ext.jfinal.BeetlRenderFactory;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.plugin.shiro.ShiroPlugin;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.c3p0.C3p0Plugin;

import controllers.eeda.AppController;
import controllers.eeda.CaseController;
import controllers.eeda.LoanController;
import controllers.eeda.PropertyClientController;
import controllers.eeda.SalesOrderController;
import controllers.eeda.UserProfileController;

public class EedaConfig extends JFinalConfig {
	private static String H2 = "H2";
	private static String Mysql = "Mysql";
	private static String ProdMysql = "ProdMysql";
	/**
	 * 
	 * 渚汼hiro鎻掍欢浣跨敤銆	 */
	Routes routes;

	C3p0Plugin cp;
	ActiveRecordPlugin arp;

	public void configConstant(Constants me) {
		me.setDevMode(true);

		BeetlRenderFactory templateFactory = new BeetlRenderFactory();
		me.setMainRenderFactory(templateFactory);

		templateFactory.groupTemplate.setCharset("utf-8");// 娌℃湁杩欏彞锛宧tml涓婄殑姹夊瓧浼氫贡鐮
		// 娉ㄥ唽鍚庯紝鍙互浣縝eetl html涓娇鐢╯hiro tag
		templateFactory.groupTemplate.registerFunctionPackage("shiro", new ShiroExt());

		// me.setErrorView(401, "/login.html");
		// me.setErrorView(403, "/login.html");
		// me.setError404View("/login.html");
		// me.setError500View("/login.html");
		// me.setErrorView(503, "/login.html");

	}

	public void configRoute(Routes me) {
		this.routes = me;

		me.add("/", AppController.class);
		me.add("/yh", controllers.yh.AppController.class, "/yh");
		me.add("/yh/loginUser", controllers.yh.LoginUserController.class, "/yh");
		me.add("/yh/role", controllers.yh.RoleController.class, "/yh");
		me.add("/case", CaseController.class);
		me.add("/user", UserProfileController.class);
		me.add("/salesOrder", SalesOrderController.class);
		me.add("/loan", LoanController.class);
		me.add("/propertyClient", PropertyClientController.class);

		// me.add("/au", AdminUserController.class);

	}

	public void configPlugin(Plugins me) {
		// 鍔犺浇Shiro鎻掍欢, for backend notation, not for UI
		me.add(new ShiroPlugin(routes));

		loadPropertyFile("app_config.txt");

		// H2 or mysql
		initDBconnector();

		me.add(cp);

		arp = new ActiveRecordPlugin(cp);
		arp.setShowSql(true);// ShowSql
		me.add(arp);

		arp.setDialect(new MysqlDialect());
		// 閰嶇疆灞炴€у悕(瀛楁鍚澶у皬鍐欎笉鏁忔劅瀹瑰櫒宸ュ巶
		arp.setContainerFactory(new CaseInsensitiveContainerFactory());

		arp.addMapping("leads", Leads.class);
		arp.addMapping("support_case", Case.class);
		arp.addMapping("user_login", UserLogin.class);
		arp.addMapping("order_header", Order.class);
		arp.addMapping("order_item", OrderItem.class);
		arp.addMapping("party", Party.class);
		arp.addMapping("party_attribute", PartyAttribute.class);

	}

	private void initDBconnector() {
		String dbType = getProperty("dbType");
		String url = getProperty("dbUrl");
		String username = getProperty("username");
		String pwd = getProperty("pwd");

		if (H2.equals(dbType)) {
			connectH2();
		} else {
			cp = new C3p0Plugin(url, username, pwd);
		}

	}

	private void connectH2() {
		cp = new C3p0Plugin("jdbc:h2:mem:eeda;", "sa", "");
		cp.setDriverClass("org.h2.Driver");
		DataInitUtil.initH2Tables(cp);
	}

	public void configInterceptor(Interceptors me) {
		// me.add(new ShiroInterceptor());

	}

	public void configHandler(Handlers me) {
		if (H2.equals(getProperty("dbType"))) {
			DataInitUtil.initData(cp);
		}
	}
}
