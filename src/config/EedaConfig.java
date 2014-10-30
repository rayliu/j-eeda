package config;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;

import models.Account;
import models.ArapChargeApplicationInvoiceNo;
import models.ArapChargeInvoice;
import models.ArapChargeInvoiceApplication;
import models.ArapChargeInvoiceApplicationItem;
import models.ArapChargeInvoiceItemInvoiceNo;
import models.ArapChargeItem;
import models.ArapChargeOrder;
import models.ArapCostInvoice;
import models.ArapCostInvoiceApplication;
import models.ArapCostInvoiceItemInvoiceNo;
import models.ArapCostItem;
import models.ArapCostOrder;
import models.ArapCostOrderInvoiceNo;
import models.CarSummaryDetail;
import models.CarSummaryDetailOilFee;
import models.CarSummaryDetailOtherFee;
import models.CarSummaryDetailRouteFee;
import models.CarSummaryDetailSalary;
import models.CarSummaryOrder;
import models.Category;
import models.DeliveryOrderFinItem;
import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.DepartOrder;
import models.DepartOrderFinItem;
import models.DepartTransferOrder;
import models.Fin_item;
import models.InsuranceFinItem;
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
import models.yh.profile.Carinfo;
import models.yh.profile.Contact;
import models.yh.profile.Route;
import models.yh.returnOrder.ReturnOrderFinItem;

import org.apache.log4j.Logger;
import org.bee.tl.ext.jfinal.BeetlRenderFactory;
import org.h2.tools.Server;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.plugin.shiro.ShiroInterceptor;
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
import controllers.yh.RegisterUserController;

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

        //没有权限时跳转到login
        me.setErrorView(401, "/yh/login.html");
        me.setErrorView(403, "/yh/login.html");
        
        //内部出错跳转到login,这个只是临时解决方案。
        me.setError404View("/yh/login.html");
        me.setError500View("/yh/login.html");
        
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

        //TODO: 为之后去掉 yh做准备
        String contentPath="/yh";
        
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
        me.add(contentPath+"/debug", controllers.yh.LogController.class, contentPath);
        // yh project controller
        me.add(contentPath, controllers.yh.AppController.class, contentPath);
        me.add(contentPath+"/loginUser", controllers.yh.LoginUserController.class, contentPath);
        //register loginUser
        me.add(contentPath+"/register",RegisterUserController.class,contentPath);
        
        me.add(contentPath+"/role", controllers.yh.RoleController.class, contentPath);
        me.add(contentPath+"/toll", controllers.yh.TollController.class, contentPath);
        me.add(contentPath+"/privilege", controllers.yh.PrivilegeController.class, contentPath);
        me.add(contentPath+"/pay", controllers.yh.PayController.class, contentPath);
        me.add(contentPath+"/ownCarPay", controllers.yh.PayController.class, contentPath);
        me.add(contentPath+"/customer", controllers.yh.profile.CustomerController.class, contentPath);
        me.add(contentPath+"/serviceProvider", controllers.yh.profile.ServiceProviderController.class, contentPath);
        me.add(contentPath+"/location", controllers.yh.LocationController.class, contentPath);
        me.add(contentPath+"/customerContract", controllers.yh.contract.ContractController.class, contentPath);
        me.add(contentPath+"/spContract", controllers.yh.contract.ContractController.class, contentPath);
        me.add(contentPath+"/route", controllers.yh.RouteController.class, contentPath);
        me.add(contentPath+"/office", controllers.yh.OfficeController.class, contentPath);
        me.add(contentPath+"/product", controllers.yh.profile.ProductController.class, contentPath);
        me.add(contentPath+"/warehouse", controllers.yh.profile.WarehouseController.class, contentPath);
        me.add(contentPath+"/orderStatus", controllers.yh.profile.OrderStatusController.class, contentPath);
        me.add(contentPath+"/account", controllers.yh.AccountController.class, contentPath);
        me.add(contentPath+"/transferOrder", controllers.yh.order.TransferOrderController.class, contentPath);
        me.add(contentPath+"/transferOrderItem", controllers.yh.order.TransferOrderItemController.class, contentPath);
        me.add(contentPath+"/transferOrderItemDetail", controllers.yh.order.TransferOrderItemDetailController.class, contentPath);
        me.add(contentPath+"/transferOrderMilestone", controllers.yh.order.TransferOrderMilestoneController.class, contentPath);
        me.add(contentPath+"/returnOrder", controllers.yh.returnOrder.ReturnOrderController.class, contentPath);
        me.add(contentPath+"/delivery", controllers.yh.delivery.DeliveryController.class, contentPath);

        me.add(contentPath+"/deliverySpContract", controllers.yh.contract.ContractController.class, contentPath);

        me.add(contentPath+"/deliveryOrderMilestone", controllers.yh.delivery.DeliveryOrderMilestoneController.class, contentPath);
        me.add(contentPath+"/pickupOrder", controllers.yh.pickup.PickupOrderController.class, contentPath);

        me.add(contentPath+"/paymentCheckOrder", controllers.yh.arap.PaymentCheckOrderController.class, contentPath);

        me.add(contentPath+"/copeCheckOrder", controllers.yh.arap.CopeCheckOrderController.class, contentPath);
        me.add(contentPath+"/departOrder", controllers.yh.departOrder.DepartOrderController.class, contentPath);
        me.add(contentPath+"/gateIn", controllers.yh.inventory.InventoryController.class, contentPath);
        me.add(contentPath+"/gateOut", controllers.yh.inventory.InventoryController.class, contentPath);
        me.add(contentPath+"/stock", controllers.yh.inventory.InventoryController.class, contentPath);
        me.add(contentPath+"/carinfo", controllers.yh.profile.CarinfoController.class, contentPath);
        me.add(contentPath+"/carmanage", controllers.yh.profile.CarinfoController.class, contentPath);
        me.add(contentPath+"/carsummary", controllers.yh.profile.CarinfoControllerTest.class, contentPath);
        me.add(contentPath+"/driverinfo", controllers.yh.profile.CarinfoController.class, contentPath);
        me.add(contentPath+"/spdriverinfo", controllers.yh.profile.CarinfoController.class, contentPath);
        me.add(contentPath+"/spcarinfo", controllers.yh.profile.CarinfoController.class, contentPath);
        me.add(contentPath+"/deliveryMilestone", controllers.yh.delivery.DeliveryController.class, contentPath);
        
        //ar= account revenue  应收条目处理
        me.add(contentPath+"/chargeConfiremList", controllers.yh.arap.ar.ChargeItemConfirmController.class, contentPath);
        me.add(contentPath+"/chargeCheckOrder", controllers.yh.arap.ar.ChargeCheckOrderController.class, contentPath);
        me.add(contentPath+"/chargePreInvoiceOrder", controllers.yh.arap.ar.ChargePreInvoiceOrderController.class, contentPath);
        me.add(contentPath+"/chargeInvoiceOrder", controllers.yh.arap.ar.ChargeInvoiceOrderController.class, contentPath);
        me.add(contentPath+"/chargeAcceptOrder", controllers.yh.arap.ar.ChargeAcceptOrderController.class, contentPath);
        me.add(contentPath+"/chargeAdjustOrder", controllers.yh.arap.ar.ChargeAdjustOrderController.class, contentPath);
        //ap 应付条目处理
        me.add(contentPath+"/costConfirmList", controllers.yh.arap.ap.CostItemConfirmController.class, contentPath);
        me.add(contentPath+"/costCheckOrder", controllers.yh.arap.ap.CostCheckOrderController.class, contentPath);
        me.add(contentPath+"/costPreInvoiceOrder", controllers.yh.arap.ap.CostPreInvoiceOrderController.class, contentPath);
        me.add(contentPath+"/costAdjustOrder", controllers.yh.arap.ap.CostAdjustOrderController.class, contentPath);
        //audit log
        me.add(contentPath+"/accountAuditLog", controllers.yh.arap.AccountAuditLogController.class, contentPath);
        //insuranceOrder
        me.add(contentPath+"/insuranceOrder", controllers.yh.insurance.InsuranceOrderController.class, contentPath);
        
        
        
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
        arp.addMapping("return_order_fin_item", ReturnOrderFinItem.class);
        arp.addMapping("delivery_order", DeliveryOrder.class);
        arp.addMapping("transfer_order_milestone", TransferOrderMilestone.class);
        arp.addMapping("billing_order", BillingOrder.class);
        arp.addMapping("billing_order_item", BillingOrderItem.class);
        arp.addMapping("delivery_order_milestone", DeliveryOrderMilestone.class);
        arp.addMapping("location", Location.class);
        //arp.addMapping("fin_account_item", AccountItem.class);
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
        arp.addMapping("arap_charge_order", ArapChargeOrder.class);
        arp.addMapping("arap_charge_item", ArapChargeItem.class);
        arp.addMapping("arap_charge_invoice", ArapChargeInvoice.class);
        arp.addMapping("insurance_order", InsuranceOrder.class);
        arp.addMapping("insurance_fin_item", InsuranceFinItem.class);
        arp.addMapping("arap_charge_invoice_application_order", ArapChargeInvoiceApplication.class);
        arp.addMapping("arap_charge_invoice_application_item", ArapChargeInvoiceApplicationItem.class);
        arp.addMapping("arap_charge_invoice_item_invoice_no", ArapChargeInvoiceItemInvoiceNo.class);
        arp.addMapping("arap_charge_application_invoice_no", ArapChargeApplicationInvoiceNo.class);
        // 应付对账单
        arp.addMapping("arap_cost_order", ArapCostOrder.class);
        arp.addMapping("arap_cost_item", ArapCostItem.class);
        arp.addMapping("arap_cost_invoice_application_order", ArapCostInvoiceApplication.class);
        arp.addMapping("arap_cost_invoice", ArapCostInvoice.class);
        arp.addMapping("arap_cost_invoice_item_invoice_no", ArapCostInvoiceItemInvoiceNo.class);
        arp.addMapping("arap_cost_order_invoice_no", ArapCostOrderInvoiceNo.class);
        // yh mapping
        //行车单
        arp.addMapping("car_summary_order", CarSummaryOrder.class);
        arp.addMapping("car_summary_detail", CarSummaryDetail.class);
        arp.addMapping("car_summary_detail_route_fee", CarSummaryDetailRouteFee.class);
        arp.addMapping("car_summary_detail_oil_fee", CarSummaryDetailOilFee.class);
        arp.addMapping("car_summary_detail_salary", CarSummaryDetailSalary.class);
        arp.addMapping("car_summary_detail_other_fee", CarSummaryDetailOtherFee.class);

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
        me.add(new ShiroInterceptor());

    }

    public void configHandler(Handlers me) {
        if (H2.equals(getProperty("dbType"))) {
            DataInitUtil.initData(cp);
        }
        // DataInitUtil.initData(cp);
    }
}
