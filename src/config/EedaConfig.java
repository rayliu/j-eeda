package config;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;

import models.Account;
import models.ArapAuditItem;
import models.ArapAuditOrder;
import models.Category;
import models.DeliveryOrderFinItem;
import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.DepartOrder;
import models.DepartTransferOrder;
import models.Fin_item;
import models.InventoryItem;
import models.Location;
import models.Office;
import models.OrderStatus;
import models.Party;
import models.PartyAttribute;
import models.Privilege;
import models.Product;
import models.ReturnOrder;
import models.Role;
import models.Toll;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.Warehouse;
import models.WarehouseOrder;
import models.WarehouseOrderItem;
import models.eeda.Case;
import models.eeda.Leads;
import models.eeda.Order;
import models.eeda.OrderItem;
import models.eeda.ServiceProvider;
import models.yh.arap.BillingOrder;
import models.yh.arap.BillingOrderItem;
import models.yh.contract.Contract;
import models.yh.contract.ContractItem;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.AccountItem;
import models.yh.profile.Carinfo;
import models.yh.profile.Contact;
import models.yh.profile.Route;

import org.apache.log4j.Logger;
import org.bee.tl.ext.jfinal.BeetlRenderFactory;
import org.h2.tools.Server;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.plugin.shiro.ShiroPlugin;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.SqlReporter;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.c3p0.C3p0Plugin;

import controllers.eeda.AppController;
import controllers.eeda.CaseController;
import controllers.eeda.LoanController;
import controllers.eeda.PropertyClientController;
import controllers.eeda.SalesOrderController;
import controllers.eeda.UserProfileController;

public class EedaConfig extends JFinalConfig {
    private Logger logger = Logger.getLogger(EedaConfig.class);

    private static String H2 = "H2";
    private static String Mysql = "Mysql";
    private static String ProdMysql = "ProdMysql";
    /**
     * 
     * 供Shiro插件使用 。
     */
    Routes routes;

    C3p0Plugin cp;
    ActiveRecordPlugin arp;

    public void configConstant(Constants me) {

        me.setDevMode(true);

        BeetlRenderFactory templateFactory = new BeetlRenderFactory();
        me.setMainRenderFactory(templateFactory);

        templateFactory.groupTemplate.setCharset("utf-8");// 没有这句，html上的汉字会乱码

        // 注册后，可以使beetl html中使用shiro tag
        templateFactory.groupTemplate.registerFunctionPackage("shiro", new ShiroExt());

        // me.setErrorView(401, "/login.html");
        // me.setErrorView(403, "/login.html");
        me.setError404View("/404.html");
        // me.setError500View("/login.html");
        // me.setErrorView(503, "/login.html");
        // get name representing the running Java virtual machine.
        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name);
        // get pid
        String pid = name.split("@")[0];
        logger.info("Pid is: " + pid);
    }

    public void configRoute(Routes me) {
        this.routes = me;

        // eeda project controller
        me.add("/", AppController.class);
        me.add("/case", CaseController.class);
        me.add("/user", UserProfileController.class);
        me.add("/salesOrder", SalesOrderController.class);
        me.add("/loan", LoanController.class);
        me.add("/propertyClient", PropertyClientController.class);
        me.add("/sp", controllers.eeda.ServiceProviderController.class);
        // me.add("/au", AdminUserController.class);

        // me.add("/fileUpload", HelloController.class);
        me.add("/yh/debug", controllers.yh.LogController.class, "/yh");
        // yh project controller
        me.add("/yh", controllers.yh.AppController.class, "/yh");
        me.add("/yh/loginUser", controllers.yh.LoginUserController.class, "/yh");
        me.add("/yh/role", controllers.yh.RoleController.class, "/yh");
        me.add("/yh/toll", controllers.yh.TollController.class, "/yh");
        me.add("/yh/privilege", controllers.yh.PrivilegeController.class, "/yh");
        me.add("/yh/pay", controllers.yh.PayController.class, "/yh");
        me.add("/yh/customer", controllers.yh.profile.CustomerController.class, "/yh");
        me.add("/yh/serviceProvider", controllers.yh.profile.ServiceProviderController.class, "/yh");
        me.add("/yh/location", controllers.yh.LocationController.class, "/yh");
        me.add("/yh/customerContract", controllers.yh.contract.ContractController.class, "/yh");
        me.add("/yh/spContract", controllers.yh.contract.ContractController.class, "/yh");
        me.add("/yh/route", controllers.yh.RouteController.class, "/yh");
        me.add("/yh/office", controllers.yh.OfficeController.class, "/yh");
        me.add("/yh/product", controllers.yh.profile.ProductController.class, "/yh");
        me.add("/yh/warehouse", controllers.yh.profile.WarehouseController.class, "/yh");
        me.add("/yh/orderStatus", controllers.yh.profile.OrderStatusController.class, "/yh");
        me.add("/yh/account", controllers.yh.AccountController.class, "/yh");
        me.add("/yh/transferOrder", controllers.yh.order.TransferOrderController.class, "/yh");
        me.add("/yh/transferOrderItem", controllers.yh.order.TransferOrderItemController.class, "/yh");
        me.add("/yh/transferOrderItemDetail", controllers.yh.order.TransferOrderItemDetailController.class, "/yh");
        me.add("/yh/transferOrderMilestone", controllers.yh.order.TransferOrderMilestoneController.class, "/yh");
        me.add("/yh/returnOrder", controllers.yh.returnOrder.ReturnOrderController.class, "/yh");
        me.add("/yh/delivery", controllers.yh.delivery.DeliveryController.class, "/yh");

        me.add("/yh/deliverySpContract", controllers.yh.contract.ContractController.class, "/yh");

        me.add("/yh/deliveryOrderMilestone", controllers.yh.delivery.DeliveryOrderMilestoneController.class, "/yh");
        me.add("/yh/pickupOrder", controllers.yh.pickup.PickupOrderController.class, "/yh");
        me.add("/yh/chargeCheckOrder", controllers.yh.arap.ChargeCheckOrderController.class, "/yh");
        me.add("/yh/paymentCheckOrder", controllers.yh.arap.PaymentCheckOrderController.class, "/yh");
        me.add("/yh/receivableCheckOrder", controllers.yh.arap.ReceivableCheckOrderController.class, "/yh");
        me.add("/yh/copeCheckOrder", controllers.yh.arap.CopeCheckOrderController.class, "/yh");
        me.add("/yh/departOrder", controllers.yh.departOrder.DepartOrderController.class, "/yh");
        me.add("/yh/gateIn", controllers.yh.inventory.InventoryController.class, "/yh");
        me.add("/yh/gateOut", controllers.yh.inventory.InventoryController.class, "/yh");
        me.add("/yh/stock", controllers.yh.inventory.InventoryController.class, "/yh");
        me.add("/yh/carinfo", controllers.yh.profile.CarinfoController.class, "/yh");
        me.add("/yh/carmanage", controllers.yh.profile.CarinfoController.class, "/yh");
        me.add("/yh/driverinfo", controllers.yh.profile.CarinfoController.class, "/yh");
        me.add("/yh/deliveryMilestone", controllers.yh.delivery.DeliveryController.class, "/yh");
    }

    public void configPlugin(Plugins me) {
        // 加载Shiro插件, for backend notation, not for UI
        me.add(new ShiroPlugin(routes));

        loadPropertyFile("app_config.txt");

        // H2 or mysql
        initDBconnector();

        me.add(cp);

        arp = new ActiveRecordPlugin(cp);
        arp.setShowSql(true);// 控制台打印Sql
        SqlReporter.setLogger(true);// log4j 打印Sql
        me.add(arp);

        arp.setDialect(new MysqlDialect());
        // 配置属性名(字段名)大小写不敏感容器工厂
        arp.setContainerFactory(new CaseInsensitiveContainerFactory());

        arp.addMapping("leads", Leads.class);
        arp.addMapping("support_case", Case.class);
        arp.addMapping("user_login", UserLogin.class);
        arp.addMapping("order_header", Order.class);
        arp.addMapping("order_item", OrderItem.class);
        arp.addMapping("party", Party.class);
        arp.addMapping("party_attribute", PartyAttribute.class);
        arp.addMapping("dp_prof_provider_info", ServiceProvider.class);

        arp.addMapping("contact", Contact.class);
        arp.addMapping("office", Office.class);
        arp.addMapping("fin_account", Account.class);
        arp.addMapping("role", Role.class);
        arp.addMapping("fin_item", Toll.class);
        arp.addMapping("privileges", Privilege.class);
        arp.addMapping("route", Route.class);
        arp.addMapping("product", Product.class);
        arp.addMapping("category", Category.class);
        arp.addMapping("warehouse", Warehouse.class);
        arp.addMapping("contract_item", ContractItem.class);
        arp.addMapping("order_status", OrderStatus.class);
        arp.addMapping("contract", Contract.class);
        arp.addMapping("transfer_order", TransferOrder.class);
        arp.addMapping("transfer_order_item", TransferOrderItem.class);
        arp.addMapping("transfer_order_item_detail", TransferOrderItemDetail.class);
        arp.addMapping("return_order", ReturnOrder.class);
        arp.addMapping("delivery_order", DeliveryOrder.class);
        arp.addMapping("transfer_order_milestone", TransferOrderMilestone.class);
        arp.addMapping("billing_order", BillingOrder.class);
        arp.addMapping("billing_order_item", BillingOrderItem.class);
        arp.addMapping("delivery_order_milestone", DeliveryOrderMilestone.class);
        arp.addMapping("location", Location.class);
        arp.addMapping("fin_account_item", AccountItem.class);
        arp.addMapping("delivery_order_item", DeliveryOrderItem.class);
        arp.addMapping("depart_order", DepartOrder.class);
        arp.addMapping("depart_transfer", DepartTransferOrder.class);
        arp.addMapping("warehouse_order", WarehouseOrder.class);
        arp.addMapping("warehouse_order_item", WarehouseOrderItem.class);
        arp.addMapping("inventory_item", InventoryItem.class);
        arp.addMapping("carinfo", Carinfo.class);
        arp.addMapping("fin_item", Fin_item.class);
        arp.addMapping("delivery_order_fin_item", DeliveryOrderFinItem.class);
        arp.addMapping("transfer_order_fin_item", TransferOrderFinItem.class);
        arp.addMapping("arap_audit_order", ArapAuditOrder.class);
        arp.addMapping("arap_audit_item", ArapAuditItem.class);
        // yh mapping

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
            // DataInitUtil.initH2Tables(cp);

        }

    }

    private void connectH2() {
        // 这个启动web console以方便通过localhost:8082访问数据库
        try {
            Server.createWebServer().start();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cp = new C3p0Plugin("jdbc:h2:mem:eeda;", "sa", "");
        // cp = new C3p0Plugin("jdbc:h2:data/sample;IFEXISTS=TRUE;", "sa", "");
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
        // DataInitUtil.initData(cp);
    }
}
