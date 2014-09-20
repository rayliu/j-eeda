package config;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;

import models.Account;
import models.ArapAuditInvoice;
import models.ArapAuditItem;
import models.ArapAuditOrder;
import models.ArapAuditOrderInvoice;
import models.Category;
import models.DeliveryOrderFinItem;
import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.DepartOrder;
import models.DepartOrderFinItem;
import models.DepartTransferOrder;
import models.Fin_item;
import models.InsuranceOrder;
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
//        me.add("/", AppController.class);
        me.add("/case", CaseController.class);
        me.add("/user", UserProfileController.class);
//        me.add("/salesOrder", SalesOrderController.class);
//        me.add("/loan", LoanController.class);
//        me.add("/propertyClient", PropertyClientController.class);
//        me.add("/sp", controllers.eeda.ServiceProviderController.class);
        // me.add("/au", AdminUserController.class);

        String path="/";
        // me.add("/fileUpload", HelloController.class);
        me.add("/debug", controllers.yh.LogController.class, "/");
        // yh project controller
        me.add("/", controllers.yh.AppController.class, "/");
        me.add("/loginUser", controllers.yh.LoginUserController.class, "/");
        me.add("/role", controllers.yh.RoleController.class, "/");
        me.add("/toll", controllers.yh.TollController.class, "/");
        me.add("/privilege", controllers.yh.PrivilegeController.class, "/");
        me.add("/pay", controllers.yh.PayController.class, "/");
        me.add("/ownCarPay", controllers.yh.PayController.class, "/");
        me.add("/customer", controllers.yh.profile.CustomerController.class, "/");
        me.add("/serviceProvider", controllers.yh.profile.ServiceProviderController.class, "/");
        me.add("/location", controllers.yh.LocationController.class, "/");
        me.add("/customerContract", controllers.yh.contract.ContractController.class, "/");
        me.add("/spContract", controllers.yh.contract.ContractController.class, "/");
        me.add("/route", controllers.yh.RouteController.class, "/");
        me.add("/office", controllers.yh.OfficeController.class, "/");
        me.add("/product", controllers.yh.profile.ProductController.class, "/");
        me.add("/warehouse", controllers.yh.profile.WarehouseController.class, "/");
        me.add("/orderStatus", controllers.yh.profile.OrderStatusController.class, "/");
        me.add("/account", controllers.yh.AccountController.class, "/");
        me.add("/transferOrder", controllers.yh.order.TransferOrderController.class, "/");
        me.add("/transferOrderItem", controllers.yh.order.TransferOrderItemController.class, "/");
        me.add("/transferOrderItemDetail", controllers.yh.order.TransferOrderItemDetailController.class, "/");
        me.add("/transferOrderMilestone", controllers.yh.order.TransferOrderMilestoneController.class, "/");
        me.add("/returnOrder", controllers.yh.returnOrder.ReturnOrderController.class, "/");
        me.add("/delivery", controllers.yh.delivery.DeliveryController.class, "/");

        me.add("/deliverySpContract", controllers.yh.contract.ContractController.class, "/");

        me.add("/deliveryOrderMilestone", controllers.yh.delivery.DeliveryOrderMilestoneController.class, "/");
        me.add("/pickupOrder", controllers.yh.pickup.PickupOrderController.class, "/");

        me.add("/paymentCheckOrder", controllers.yh.arap.PaymentCheckOrderController.class, "/");

        me.add("/copeCheckOrder", controllers.yh.arap.CopeCheckOrderController.class, "/");
        me.add("/departOrder", controllers.yh.departOrder.DepartOrderController.class, "/");
        me.add("/gateIn", controllers.yh.inventory.InventoryController.class, "/");
        me.add("/gateOut", controllers.yh.inventory.InventoryController.class, "/");
        me.add("/stock", controllers.yh.inventory.InventoryController.class, "/");
        me.add("/carinfo", controllers.yh.profile.CarinfoController.class, "/");
        me.add("/carmanage", controllers.yh.profile.CarinfoController.class, "/");
        me.add("/driverinfo", controllers.yh.profile.CarinfoController.class, "/");
        me.add("/spdriverinfo", controllers.yh.profile.CarinfoController.class, "/");
        me.add("/spcarinfo", controllers.yh.profile.CarinfoController.class, "/");
        me.add("/deliveryMilestone", controllers.yh.delivery.DeliveryController.class, "/");
        //ar
        me.add("/chargeCheckOrder", controllers.yh.arap.ar.ChargeCheckOrderController.class, "/");
        me.add("/chargeInvoiceOrder", controllers.yh.arap.ar.ChargeInvoiceOrderController.class, "/");
        me.add("/chargeAcceptOrder", controllers.yh.arap.ar.ChargeAcceptOrderController.class, "/");
        me.add("/chargeAdjustOrder", controllers.yh.arap.ar.ChargeAdjustOrderController.class, "/");
        //ap
        me.add("/costCheckOrder", controllers.yh.arap.ap.CostCheckOrderController.class, "/");
        me.add("/costAcceptOrder", controllers.yh.arap.ap.CostAcceptOrderController.class, "/");
        me.add("/costAdjustOrder", controllers.yh.arap.ap.CostAdjustOrderController.class, "/");
        //audit log
        me.add("/accountAuditLog", controllers.yh.arap.AccountAuditLogController.class, "/");
        //insuranceOrder
        me.add("/insuranceOrder", controllers.yh.insurance.InsuranceOrderController.class, "/");
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


        arp.addMapping("support_case", Case.class);
        arp.addMapping("user_login", UserLogin.class);

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
        arp.addMapping("depart_order_fin_item", DepartOrderFinItem.class);//提货拼车单、发车单的应付表
        arp.addMapping("arap_audit_order", ArapAuditOrder.class);
        arp.addMapping("arap_audit_item", ArapAuditItem.class);
        arp.addMapping("arap_audit_invoice", ArapAuditInvoice.class);
        arp.addMapping("arap_audit_order_invoice", ArapAuditOrderInvoice.class);
        arp.addMapping("insurance_order", InsuranceOrder.class);
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
            //DataInitUtil.initH2Tables(cp);

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
        //DataInitUtil.initData(cp);
    }
}
