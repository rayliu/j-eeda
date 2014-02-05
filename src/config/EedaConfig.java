package config;import models.Case;import models.Leads;import models.Order;import models.OrderItem;import models.UserLogin;import org.bee.tl.ext.jfinal.BeetlRenderFactory;import com.jfinal.config.Constants;import com.jfinal.config.Handlers;import com.jfinal.config.Interceptors;import com.jfinal.config.JFinalConfig;import com.jfinal.config.Plugins;import com.jfinal.config.Routes;import com.jfinal.ext.plugin.shiro.ShiroPlugin;import com.jfinal.plugin.activerecord.ActiveRecordPlugin;import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;import com.jfinal.plugin.activerecord.dialect.MysqlDialect;import com.jfinal.plugin.c3p0.C3p0Plugin;import controllers.AppController;import controllers.CaseController;import controllers.SalesOrderController;import controllers.UserProfileController;public class EedaConfig extends JFinalConfig {	private static String H2 = "H2";	private static String Mysql = "Mysql";	private static String ProdMysql = "ProdMysql";	/**	 * 	 * 供Shiro插件使用。	 */	Routes routes;	C3p0Plugin cp;	ActiveRecordPlugin arp;	public void configConstant(Constants me) {		me.setDevMode(true);		BeetlRenderFactory templateFactory = new BeetlRenderFactory();		me.setMainRenderFactory(templateFactory);		templateFactory.groupTemplate.setCharset("utf-8");// 没有这句，html上的汉字会乱码		// 注册后，可以使beetl html中使用shiro tag		templateFactory.groupTemplate.registerFunctionPackage("shiro", new ShiroExt());		me.setErrorView(401, "/login.html");		me.setErrorView(403, "/login.html");		me.setError404View("/login.html");		me.setError500View("/login.html");		me.setErrorView(503, "/login.html");	}	public void configRoute(Routes me) {		this.routes = me;		me.add("/", AppController.class);		me.add("/case", CaseController.class);		me.add("/user", UserProfileController.class);		me.add("/salesOrder", SalesOrderController.class);		// me.add("/au", AdminUserController.class);	}	public void configPlugin(Plugins me) {		// 加载Shiro插件, for backend notation, not for UI		me.add(new ShiroPlugin(routes));		loadPropertyFile("app_config.txt");		// H2 or mysql		initDBconnector();		me.add(cp);		arp = new ActiveRecordPlugin(cp);		arp.setShowSql(true);// ShowSql		me.add(arp);		arp.setDialect(new MysqlDialect());		// 配置属性名(字段名)大小写不敏感容器工厂		arp.setContainerFactory(new CaseInsensitiveContainerFactory());		arp.addMapping("leads", Leads.class);		arp.addMapping("support_case", Case.class);		arp.addMapping("user_login", UserLogin.class);		arp.addMapping("order_header", Order.class);		arp.addMapping("order_item", OrderItem.class);	}	private void initDBconnector() {		String dbType = getProperty("dbType");		String url = getProperty("dbUrl");		String username = getProperty("username");		String pwd = getProperty("pwd");		if (H2.equals(dbType)) {			connectH2();		} else {			cp = new C3p0Plugin(url, username, pwd);		}	}	private void connectH2() {		cp = new C3p0Plugin("jdbc:h2:mem:eeda;", "sa", "");		cp.setDriverClass("org.h2.Driver");		DataInitUtil.init(cp);	}	public void configInterceptor(Interceptors me) {		// me.add(new ShiroInterceptor());	}	public void configHandler(Handlers me) {		// c3p0本来就有的一个bug，解决方法是将maxStatement设为0		// ((ComboPooledDataSource) DbKit.getDataSource()).setMaxStatements(0);	}}