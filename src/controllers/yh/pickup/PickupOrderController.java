package controllers.yh.pickup;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderItem;
import models.DepartOrder;
import models.DepartOrderFinItem;
import models.DepartPickupOrder;
import models.DepartTransferOrder;
import models.FinItem;
import models.InventoryItem;
import models.ParentOfficeModel;
import models.Party;
import models.PickupOrderFinItem;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.Warehouse;
import models.yh.contract.Contract;
import models.yh.delivery.DeliveryOrder;
import models.yh.pickup.PickupDriverAssistant;
import models.yh.profile.Carinfo;
import models.yh.profile.Contact;
import models.yh.profile.DriverAssistant;
import models.yh.returnOrder.ReturnOrderFinItem;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.protocol.RequestExpectContinue;
//import org.apache.hadoop.hive.ql.parse.HiveParser.booleanValue_return;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.OfficeController;
import controllers.yh.returnOrder.ReturnOrderController;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;
import controllers.yh.util.getCustomFile;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PickupOrderController extends Controller {
    private Logger logger = Logger.getLogger(PickupOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_LIST})
    public void index() {
    	Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
    	setAttr("customizeField", customizeField);
    	
    	List<Record> re = Db.find("SELECT o.id,o.office_name FROM transfer_order tor "
    			+ " LEFT JOIN office o on o.id = tor.office_id"
    			+ " where tor.office_id in (select office_id from user_office where user_name='"
				+ currentUser.getPrincipal() + "')  GROUP BY o.id ;");
    	setAttr("officeList", re);

        render("/yh/pickup/pickupOrderList.html");
    }

    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_CREATE})
    public void add() {
    	String flag = getPara("flag");
    	Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
    	customizeField.put("flag", flag);
    	setAttr("customizeField", customizeField);
        render("/yh/pickup/pickupOrderSearchTransfer.html");
    }
    
    
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_CREATE})
    @Before(Tx.class)
    public void createPickupOrder() {
        String ids = getPara("ids");   //总ID
        String[] transferOrderIds = ids.split(",");   //总ID
        String flag = getPara("flag");
        String twice_pickup_type = getPara("twice_pickup_type");
        
        String strJson = getPara("detailJson");
		List<Map> idList = new Gson().fromJson(strJson, 
				new TypeToken<List<Map>>(){}.getType());

        if (transferOrderIds.length == 1) {
            TransferOrder transferOrderAttr = TransferOrder.dao.findById(transferOrderIds[0]);
            setAttr("transferOrderAttr", transferOrderAttr);
            Long spId = transferOrderAttr.getLong("sp_id");
            if (spId != null && !"".equals(spId)) {
                Party spParty = Party.dao.findById(spId);
                setAttr("spParty", spParty);
                Contact spContact = Contact.dao.findById(spParty.getLong("contact_id"));
                setAttr("spContact", spContact);
            }
        }
        setAttr("twice_pickup_type",twice_pickup_type);
        setAttr("saveOK", false);
    
        //多地点提货顺序
        String cargo_nature = "";
        for (int i = 0; i < transferOrderIds.length; i++) {
            TransferOrder transferOrder = TransferOrder.dao.findById(transferOrderIds[i]);
            transferOrder.set("pickup_seq", i + 1);
            transferOrder.update();
            
            if("cargo".equals(transferOrder.getStr("cargo_nature"))){
            	cargo_nature = "cargo";
            }
        }
        setAttr("cargo_nature", cargo_nature);
        
        //直送收货人地址（应该是）
        String sql = "SELECT tra.receiving_address FROM transfer_order tra "
                + " where tra.id = '"+transferOrderIds[0]+"'";
        Record tro = Db.findFirst(sql);
        setAttr("transferOrder",tro);
        setAttr("transferIds", ids);  
        setAttr("create_by",LoginUserController.getLoginUserId(this));
        UserLogin userLogin = UserLogin.dao.findById(LoginUserController.getLoginUserId(this));
        setAttr("userLogin", userLogin);
        setAttr("status", "新建");
        setAttr("saveOK", false);
        setAttr("strJson",strJson.replaceAll("\"", "'"));
        setAttr("flag", flag);
        
        List<Record> paymentItemList = Collections.EMPTY_LIST;
        paymentItemList = Db.find("select * from fin_item where type='应付'");
        setAttr("paymentItemList", paymentItemList);
        List<Record> incomeItemList = Collections.EMPTY_LIST;
        incomeItemList = Db.find("select * from fin_item where type='应收'");
        setAttr("incomeItemList", incomeItemList);
        Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
        setAttr("customizeField", customizeField);
        render("/yh/pickup/editPickupOrder.html");
    }

    // 拼车单列表
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_LIST})
    public void pickuplist() {
        String orderNo = getPara("orderNo").trim();
        String driver = getPara("carNo").trim();
        String departNo = getPara("departNo").trim();
        String beginTime = getPara("beginTime").trim();
        String endTime = getPara("endTime").trim();
        String planningBeginTime = getPara("planningBeginTime").trim();
        String planningEndTime = getPara("planningEndTime").trim();
        String take = getPara("take").trim();
        String status = getPara("status").trim();
        String office = getPara("office").trim();
        String customerId = getPara("customer_filter").trim();
        String sp_filter = getPara("sp_filter").trim();
        String sortColIndex = getPara("iSortCol_0").trim();
		String sortBy = getPara("sSortDir_0").trim();
		String colName = getPara("mDataProp_"+sortColIndex).trim();
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sql = "";
        String sqlTotal = "";
        if (StringUtils.isEmpty(planningBeginTime)&& StringUtils.isEmpty(planningEndTime) && (orderNo == null || "".equals(orderNo)) && (departNo == null || "".equals(departNo)) && (beginTime == null || "".equals(beginTime))
        		&& (endTime == null || "".equals(endTime)) &&  (take == null || "".equals(take)) && 
				 (status == null || "".equals(status)) && (office == null || "".equals(office))&& StringUtils.isNotEmpty(driver) && (sp_filter == null || "".equals(sp_filter))&&  customerId == null ) {
            sqlTotal = "select count(distinct dor.id) total "
            		+ " from depart_order dor "
                    + " left join carinfo c on dor.carinfo_id = c.id "
                    + " left join party p on dor.driver_id = p.id "
                    + " left join contact ct on p.contact_id = ct.id "
                    + " left join user_login u on u.id = dor.create_by "
                    + " left join depart_transfer dtf on dtf.pickup_id = dor.id "
                    + " left join transfer_order t_o on t_o.id = dtf.order_id "
                    + " left join office o on o.id = t_o.office_id "
                    + " left join transfer_order_item toi on toi.order_id = t_o.id "
                    + " left join product pd on pd.id = toi.product_id "
                    + " where dor.status!='取消' and combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "' "
            		+ " and dor.status!='手动删除' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
            		+ " and t_o.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";

            sql = "select dor.id,dor.depart_no,dor.status,dor.pickup_mode,dor.car_no,dor.driver contact_person,dor.phone, dor.turnout_time,dor.remark,"
            		+ " ifnull(dor.driver,c.driver) contact_person,ifnull(dor.phone,c.phone) phone,ifnull(dor.car_type,c.cartype) cartype,c.status cstatus,"
            		+ " ifnull(nullif(u.c_name,''),u.user_name) user_name,o.office_name office_name,"
            		+ " round((select sum(ifnull(p.volume,0)*ifnull(dt.amount,0)) from transfer_order_item toi left join depart_transfer dt on dt.order_item_id = toi.id left join product p on p.id = toi.product_id where dt.pickup_id = dor.id),2) cargovolume,"
            		+ " round((select sum(ifnull(p.weight,0)*ifnull(dt.amount,0)) from transfer_order_item toi left join depart_transfer dt on dt.order_item_id = toi.id left join product p on p.id = toi.product_id where dt.pickup_id = dor.id),2) cargoweight,"
            		+ " round((select sum(ifnull(volume,0)) from transfer_order_item_detail where pickup_id = dor.id),2) atmvolume,"
            		+ " round((select sum(ifnull(weight,0)) from transfer_order_item_detail where pickup_id = dor.id),2) atmweight,"
            		+ " (select group_concat( distinct dt.transfer_order_no separator '<br/>')  from depart_transfer dt where pickup_id = dor.id)  as transfer_order_no,"
            		+ " (select group_concat(distinct c1.abbr separator '<br/>') from depart_transfer dt left join transfer_order t_o on t_o.id = dt.order_id left join party p1 on t_o.customer_id = p1.id left join contact c1 on p1.contact_id = c1.id where dt.pickup_id = dor.id) customernames,  "
            		+ " group_concat( cast(t_o.planning_time as char) SEPARATOR '<br/>' ) planning_time, "
            		+ " c2.abbr sp_name, "
            		+ " (select sum(amount) from pickup_order_fin_item where pickup_order_id=dor.id and status is not null) cost_amount"
            		+ " from depart_order dor "
                    + " left join carinfo c on dor.carinfo_id = c.id "
                    + " left join party p on dor.driver_id = p.id "
                    + " left join contact ct on p.contact_id = ct.id "
                    + " left join user_login u on u.id = dor.create_by "
                    + " left join depart_transfer dtf on dtf.pickup_id = dor.id "
                    + " left join transfer_order t_o on t_o.id = dtf.order_id "
                    + " left join office o on o.id = t_o.office_id "
                    + " LEFT JOIN party p2 on p2.id = dor.sp_id "
                    + " LEFT JOIN contact c2 on c2.id = p2.contact_id "
                    + " where dor.status!='取消' and combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "' "
            		+ " and dor.status!='手动删除' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and t_o.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
                    + " group by dor.id ";
        } else {
            if (StringUtils.isEmpty(beginTime)) {
                beginTime = "2000-1-1";
            }
            if (StringUtils.isEmpty(endTime)) {
                endTime = "2037-12-31";
            }else{
            	endTime += " 23:59:59";
            }
            if (StringUtils.isEmpty(planningBeginTime)) {
            	planningBeginTime = "2000-1-1";
            }
            if (StringUtils.isEmpty(planningEndTime)) {
            	planningEndTime = "2037-12-31";
            }else{
            	planningEndTime += " 23:59:59";
            }
            sqlTotal = "select count(distinct dor.id) total "
            		+ " from depart_order dor "
                    + " left join carinfo c on dor.carinfo_id = c.id "
                    + " left join party p on dor.driver_id = p.id "
                    + " left join contact ct on p.contact_id = ct.id "
                    + " left join user_login u on u.id = dor.create_by "
                    + " left join depart_transfer dtf on dtf.pickup_id = dor.id "
                    + " left join transfer_order t_o on t_o.id = dtf.order_id "
                    + " left join office o on o.id = t_o.office_id "
                    + " left join party p1 on t_o.customer_id = p1.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " LEFT JOIN party p2 on p2.id = dor.sp_id "
                    + " LEFT JOIN contact c2 on c2.id = p2.contact_id"
                    + " where dor.status!='取消' and combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "' "
            		+ " and ifnull(depart_no,'') like '%"+ departNo + "%' "
            		+ " and ifnull(dor.driver,'') like '%"+ driver + "%' "
            		+ " and ifnull(c2.abbr,'') like '%"+ sp_filter + "%' "
            		+ " and ifnull(dtf.transfer_order_no,'') like '%"+ orderNo + "%' "
    				+ " and dor.turnout_time between '" + beginTime + "' and '" + endTime+ "' "
    				+ " and ifnull(t_o.planning_time,'') between '" + planningBeginTime + "' and '" + planningEndTime+ "' "
					+ " and ifnull(dor.status,'') like '%"+status+ "%' "
					+ " and ifnull(o.office_name,'') like '%"+office+ "%' "
					+ " and ifnull(dor.pickup_mode,'') like '%"+take+ "%' "
					+ " and c1.abbr like '%" + customerId + "%'"
                    + " and dor.status!='手动删除' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and t_o.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";

            sql = "select dor.id,dor.depart_no,dor.status,dor.pickup_mode,dor.car_no,dor.driver contact_person,dor.phone, dor.turnout_time,dor.remark,"
            		+ " ifnull(dor.driver,c.driver) contact_person,ifnull(dor.phone,c.phone) phone,ifnull(dor.car_type,c.cartype) cartype,c.status cstatus,"
            		+ " ifnull(nullif(u.c_name,''),u.user_name) user_name,o.office_name office_name,"
            		+ " round((select sum(ifnull(p.volume,0)*ifnull(dt.amount,0)) from transfer_order_item toi left join depart_transfer dt on dt.order_item_id = toi.id left join product p on p.id = toi.product_id where dt.pickup_id = dor.id),2) cargovolume,"
            		+ " round((select sum(ifnull(p.weight,0)*ifnull(dt.amount,0)) from transfer_order_item toi left join depart_transfer dt on dt.order_item_id = toi.id left join product p on p.id = toi.product_id where dt.pickup_id = dor.id),2) cargoweight,"
            		+ " round((select sum(ifnull(volume,0)) from transfer_order_item_detail where pickup_id = dor.id),2) atmvolume,"
            		+ " round((select sum(ifnull(weight,0)) from transfer_order_item_detail where pickup_id = dor.id),2) atmweight,"
            		+ " (select group_concat( distinct dt.transfer_order_no separator '<br/>')  from depart_transfer dt where pickup_id = dor.id)  as transfer_order_no,  "
            		+ " (select group_concat(distinct c1.abbr separator '<br/>') from depart_transfer dt left join transfer_order t_o on t_o.id = dt.order_id left join party p1 on t_o.customer_id = p1.id left join contact c1 on p1.contact_id = c1.id where dt.pickup_id = dor.id) customernames , "
            		+ " group_concat( cast(t_o.planning_time as char) SEPARATOR '<br/>' ) planning_time, "
            		+ " c2.abbr sp_name, "
            		+ " (select sum(amount) from pickup_order_fin_item where pickup_order_id=dor.id and status is not null) cost_amount"
            		+ " from depart_order dor "
                    + " left join carinfo c on dor.carinfo_id = c.id "
                    + " left join party p on dor.driver_id = p.id "
                    + " left join contact ct on p.contact_id = ct.id "
                    + " left join user_login u on u.id = dor.create_by "
                    + " left join depart_transfer dtf on dtf.pickup_id = dor.id "
                    + " left join transfer_order t_o on t_o.id = dtf.order_id "
                    + " left join office o on o.id = t_o.office_id "
                    + " left join party p1 on t_o.customer_id = p1.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " LEFT JOIN party p2 on p2.id = dor.sp_id "
                    + " LEFT JOIN contact c2 on c2.id = p2.contact_id "
                    + " where dor.status!='取消' and combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "' "
            		+ " and ifnull(depart_no,'') like '%"+ departNo + "%' "
            		+ " and ifnull(dor.driver,'') like '%"+ driver + "%' "
            		+ " and ifnull(c2.abbr,'') like '%"+ sp_filter + "%' "
            		+ " and ifnull(dtf.transfer_order_no,'') like '%"+ orderNo + "%' "
    				+ " and dor.turnout_time between '" + beginTime + "' and '" + endTime+ "' "
    				+ " and ifnull(t_o.planning_time,'') between '" + planningBeginTime + "' and '" + planningEndTime+ "' "
					+ " and ifnull(dor.status,'') like '%"+status+ "%' "
					+ " and ifnull(o.office_name,'') like '%"+office+ "%' "
					+ " and ifnull(dor.pickup_mode,'') like '%"+take+ "%' "
					+ " and c1.abbr like '%" + customerId + "%'"
					+ " and dor.status!='手动删除' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
            		+ " and t_o.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
            		+ " group by dor.id ";
    
        }
        String orderByStr = " order by A.depart_time desc ";
        if(colName.length()>0){
        	orderByStr = " order by "+colName+" "+sortBy;
        }
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> warehouses = Db.find(sql + orderByStr + sLimit);

        Map map = new HashMap();
        map.put("sEcho", pageIndex);
        map.put("iTotalRecords", rec.getLong("total"));
        map.put("iTotalDisplayRecords", rec.getLong("total"));

        map.put("aaData", warehouses);
        renderJson(map);
    }
    
    
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_CREATE})
    public void createList() {
        Map transferOrderListMap = null;
        String flag = getPara("flag");
        String orderNo = getPara("orderNo");
        String item_no = getPara("item_no");
        String status = getPara("status");
        String address = getPara("address");
        String customer = getPara("customer");
        String routeFrom = getPara("routeFrom");
        String routeTo = getPara("routeTo");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String orderType = getPara("orderType") == null ? "" : getPara("orderType");
        
        String sql1 = "";
        if(flag=="" || flag==null){
        	sql1 = "";
        }else if(flag.equals("derect")){
        	sql1 = "and tor.arrival_mode='delivery'";
        }
        if (!"".equals(orderType)) {
            if ("销售订单".contains(orderType)) {
                orderType = "salesOrder";
            } else if ("补货订单".contains(orderType)) {
                orderType = "replenishmentOrder";
            } else if ("调拨订单".contains(orderType)) {
                orderType = "arrangementOrder";
            } else if ("退货订单".contains(orderType)) {
                orderType = "cargoReturnOrder";
            } else if ("质量退单".contains(orderType)) {
                orderType = "damageReturnOrder";
            } else if ("出库运输单".contains(orderType)) {
                orderType = "gateOutTransferOrder";
            }
        }

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        String sql = "";
        String sqlTotal = "";
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String conditions=" where 1=1 ";
		if (StringUtils.isNotEmpty(orderNo)){
        	conditions+=" and order_no like '%"+orderNo+"%'";
        }
        if (StringUtils.isNotEmpty(customer)){
        	conditions+=" and cname like '%"+customer+"%'";
        }
        if (StringUtils.isNotEmpty(routeFrom)){
        	conditions+=" and route_from like '%"+routeFrom+"%'";
        }
        if (StringUtils.isNotEmpty(routeTo)){
        	conditions+=" and route_to like '%"+routeTo+"%'";
        }
        if (StringUtils.isNotEmpty(address)){
        	conditions+=" and address like '%"+address+"%'";
        }
        if (StringUtils.isNotEmpty(item_no)){
        	conditions+=" and item_no like '%"+item_no+"%'";
        }
        if (StringUtils.isNotEmpty(orderType)){
        	conditions+=" and order_type like '%"+orderType+"%'";
        }
        if (StringUtils.isNotEmpty(beginTime)){
			beginTime = " and create_stamp between'"+beginTime+"'";
        }else{
        	beginTime =" and create_stamp between '2000-1-1'";
        }
        if (StringUtils.isNotEmpty(endTime)){
        	endTime =" and '"+endTime+"'";
        }else{
        	endTime =" and '3000-1-1'";
        }
        conditions += beginTime + endTime;
        conditions += " and !( cargo_nature = 'ATM' AND atmamount = 0 ) and !( cargo_nature = 'cargo' AND total_amount <= 0 )  and office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"; 

        sql = "select tor.id,tor.receiving_address,tor.office_id , tor.customer_id,tor.order_no,"
        		+ " (select GROUP_CONCAT(ifnull(p.item_no,ifnull(toi.item_no,toi.item_name)) SEPARATOR '<br/>') "
        		+ " from transfer_order_item toi"
        		+ " LEFT JOIN product p on p.id = toi.product_id "
        		+ " where order_id = tor.id) item_no,"
        		+ " tor.operation_type,tor.cargo_nature,tor.order_type,tor.planning_time,tor.cargo_nature_detail,"
        		+ " round((select sum((ifnull(toi.amount, 0) - ifnull(toi.pickup_number,0)) * ifnull(p.volume, 0)) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id = tor.id),2) total_volume,"
                + " round((select sum((ifnull(toi.amount, 0) - ifnull(toi.pickup_number,0)) * ifnull(p.weight, 0)) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id = tor.id),2) total_weight,"
                + " (select sum(tori.amount - ifnull(tori.pickup_number,0)- tori.have_twice_pickup) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                + " (select count(0) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null and twice_pickup_id is null) atmamount,"
                + " (select round(sum(volume),2) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null and twice_pickup_id is null) atmvolume,"
                + " (select round(sum(weight),2) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null and twice_pickup_id is null) atmweight,"
                + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status,o.office_name office_name"
                + " from transfer_order tor "
                + " left join party p on tor.customer_id = p.id " 
                + " left join contact c on p.contact_id = c.id "
                + " left join location l1 on tor.route_from = l1.code "
                + " left join location l2 on tor.route_to = l2.code  "
                + " left join office o on o.id= tor.office_id"
                + " where tor. STATUS != '已完成' and tor.pickup_assign_status!= 'ALL' "
                + " and (tor.operation_type != 'out_source' or (tor.operation_type = 'out_source' and tor.order_type ='replenishmentOrder'))"
                + " and tor.status not in('手动删除','取消')"
                + " union"
                + " select tor.id,tor.receiving_address,tor.office_id , tor.customer_id,CONCAT(tor.order_no,'<br/>','(一次提货)') order_no,"
                + " (select GROUP_CONCAT(ifnull(p.item_no,ifnull(toi.item_no,toi.item_name)) SEPARATOR '<br/>') "
        		+ " from transfer_order_item toi"
        		+ " LEFT JOIN product p on p.id = toi.product_id "
        		+ " where order_id = tor.id) item_no,"
                + " tor.operation_type,tor.cargo_nature,tor.order_type,tor.planning_time,tor.cargo_nature_detail,"
        		+ " round((select sum((ifnull(toi.amount, 0) - ifnull(toi.pickup_number,0)) * ifnull(p.volume, 0)) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id = tor.id),2) total_volume,"
                + " round((select sum((ifnull(toi.amount, 0) - ifnull(toi.pickup_number,0)) * ifnull(p.weight, 0)) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id = tor.id),2) total_weight,"
                + " (select sum(ifnull(tori.twice_pickup_number,0)-ifnull(tori.pickup_number, 0)) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                + " (select count(0) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null and twice_pickup_id is not null) atmamount,"
                + " (select round(sum(volume),2) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null and twice_pickup_id is not null) atmvolume,"
                + " (select round(sum(weight),2) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null and twice_pickup_id is not null) atmweight,"
                + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status,o.office_name office_name"
                + " from transfer_order tor "
                + " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
                + " LEFT JOIN depart_order dor on dor.id = dt.pickup_id "
                + " left join party p on tor.customer_id = p.id " 
                + " left join contact c on p.contact_id = c.id "
                + " left join location l1 on tor.route_from = l1.code " 
                + " left join location l2 on tor.route_to = l2.code  "
                + " left join office o on o.id= tor.office_id"
                + " where tor. STATUS != '已完成' and tor.pickup_assign_status!= 'ALL' "
                + " and (tor.operation_type != 'out_source' or (tor.operation_type = 'out_source' and tor.order_type ='replenishmentOrder'))"
                + " and tor.status not in('手动删除','取消')"
                + " and dor.`STATUS` = '已二次提货'"
                + " group by tor.id ";

            
        Record rec = Db.findFirst("select count(1) total from (select * from ("+sql+ ") A "+ conditions + ") B");
        logger.debug("total records:" + rec.getLong("total"));
      
        List<Record> transferOrders = Db.find("select * from ("+sql+ ") A "+ conditions + " order by planning_time desc " + sLimit);
        transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);
        renderJson(transferOrderListMap);
    }

    // 选取额外运输单
    public void externTransferOrderList() {
        String orderIds = "";
        String pickupOrderId = getPara("pickupOrderId");
        if (pickupOrderId == null || "".equals(pickupOrderId)) {
            pickupOrderId = "-1";
        }
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find("select * from depart_transfer where pickup_id = ?",
                pickupOrderId);
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            orderIds += departTransferOrder.getLong("order_id") + ",";
        }
        if (!"".equals(orderIds) && orderIds != null) {
            orderIds = orderIds.substring(0, orderIds.length() - 1);
        } else {
            orderIds = "-1";
        }
        String pageIndex = getPara("sEcho");
        String sLimit = "";
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(1) total  from transfer_order tor " + " left join party p on tor.customer_id = p.id "
                + " left join contact c on p.contact_id = c.id " + " left join location l1 on tor.route_from = l1.code "
                + " left join location l2 on tor.route_to = l2.code"
                + " where tor.status not in ('已入库','已签收') and tor.operation_type = 'own' and ifnull(tor.pickup_assign_status, '') !='"
                + TransferOrder.ASSIGN_STATUS_ALL + "' and tor.id not in(" + orderIds + ")";
        String sql = "select tor.id,tor.order_no,tor.cargo_nature,tor.order_type,"
        		+ " round((select sum(ifnull(toi.volume,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_volume, "
                + " round((select sum(ifnull(toi.sum_weight,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_weight, "
                + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,"
                + " l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status from transfer_order tor "
                + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                + " left join location l1 on tor.route_from = l1.code " + " left join location l2 on tor.route_to = l2.code"
                + " where tor.status not in ('已入库','已签收') and tor.operation_type = 'own' and ifnull(tor.pickup_assign_status, '') !='"
                + TransferOrder.ASSIGN_STATUS_ALL + "'" + " and tor.id not in(" + orderIds + ") order by tor.create_stamp desc" + sLimit;

        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> transferOrders = Db.find(sql);

        Map transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);
        renderJson(transferOrderListMap);
    }

    // 初始化货品数据
    public void getInitPickupOrderItems() {
        String orderId = getPara("localArr");// 运输单id
        String pickId = getPara("pickupId");
        String pickup_type = getPara("pickup_type");
    	if("twice_pickup".equals(pickup_type)){
    		pickup_type = "  twice_pickup_id ";
    	}else{
    		pickup_type = "  pickup_id";
    	}
    	
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "SELECT COUNT(0) total FROM(SELECT(SELECT count(0) total FROM	transfer_order_item_detail WHERE order_id = tor.id	AND item_id = toi.id and"+ pickup_type +" = '"+pickId+"') atmamount, ifnull(toi.amount, 0) cargoamount FROM transfer_order_item toi LEFT JOIN transfer_order tor ON tor.id = toi.order_id WHERE toi.order_id IN (" + orderId + ")) a where (atmamount > 0 or cargoamount>0)";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        String sql = "";
        if(!"".equals(pickId) && pickId != null){
        	 sql = " SELECT * FROM(select toi.id,ifnull(toi.item_name, pd.item_name) item_name,tor.planning_time,ifnull(toi.item_no, pd.item_no) item_no,"
             		 + " round(ifnull(pd.volume, 0),2) volume,round(ifnull(pd.weight, 0),2) weight,tor.cargo_nature,c.abbr customer,tor.order_no,toi.remark,"
             		 + " ifnull((select count(0) total from transfer_order_item_detail where order_id = tor.id and item_id = toi.id and"+ pickup_type +" = '"+pickId+"'), 0) atmamount,"
                     + " ifnull((select ifnull(dt.amount, 0)  from depart_transfer dt where dt.order_id = tor.id and dt.pickup_id = '"+pickId+"'), 0) cargoamount,"
                     + " round(ifnull((select (ifnull(pdd.volume, 0) * ifnull(dt.amount, 0))  from depart_transfer dt left join transfer_order_item toii on toii.id = dt.order_item_id "
                     + " left join product pdd ON pdd.id = toii.product_id where dt.order_item_id = toi.id and dt.pickup_id = '"+pickId+"'), 0),2) cargovolume,"
                     + " round(ifnull((select (ifnull(pdd.weight, 0) * ifnull(dt.amount, 0))  from depart_transfer dt left join transfer_order_item toii on toii.id = dt.order_item_id"
                     + " left join product pdd ON pdd.id = toii.product_id where dt.order_item_id = toi.id and dt.pickup_id = '"+pickId+"'), 0),2) cargoweight"
                     + " from transfer_order_item toi "
                     + " left join transfer_order tor on tor.id = toi.order_id"
                     + " left join party p on p.id = tor.customer_id" 
                     + " left join contact c on c.id = p.contact_id"
                     + " left join product pd on pd.id = toi.product_id"
                     + " where toi.order_id in(" + orderId + ")  order by c.id ) a where (atmamount > 0 or cargoamount>0)" + sLimit;
        }
      
        List<Record> departOrderitem = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", departOrderitem);
        renderJson(Map);
    }

    // 保存/更新调车单
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_CREATE,PermissionConstant.PERMISSION_PO_UPDATE},logical=Logical.OR)
    @Before(Tx.class)
    public void savePickupOrder() {
        DepartOrder pickupOrder = null;
        String pickId = getPara("pickupId");
        String driverId = getPara("driver_id");
        String carinfoId = getPara("carinfoId");
        String replenishmentOrderId = getPara("replenishmentOrderId");
        String gateInSelect = getPara("gateInSelect");
        String returnTime = getPara("return_time");
        String con_address = getPara("con_address");
        String[] orderids = getPara("orderid").split(",");
        String[] driverAssistantIds = getPara("driverAssistantIds").split(",");
        String[] driverAssistantNames = getPara("driverAssistantNames").split(",");
        String[] driverAssistantPhones = getPara("driverAssistantPhones").split(",");
        String strJson = getPara("strJson");

        if (pickId == null || "".equals(pickId)) {
            pickupOrder = new DepartOrder();
            pickupOrder.set("depart_no", OrderNoGenerator.getNextOrderNo("DC"));
            pickupOrder.set("status", getPara("status"));
            pickupOrder.set("charge_type", getPara("chargeType"));
            pickupOrder.set("create_by", getPara("create_by"));
            pickupOrder.set("car_no", getPara("car_no"));
            pickupOrder.set("car_type", getPara("car_type"));
            pickupOrder.set("car_size", getPara("car_size"));
            pickupOrder.set("driver", getPara("driver_name"));
            pickupOrder.set("phone", getPara("driver_phone"));
            pickupOrder.set("remark", getPara("remark"));
            pickupOrder.set("combine_type", DepartOrder.COMBINE_TYPE_PICKUP);
            pickupOrder.set("pickup_mode", getPara("pickupMode"));
            pickupOrder.set("address", getPara("address"));
            pickupOrder.set("turnout_time", getPara("turnout_time"));
            pickupOrder.set("audit_status", "新建");
            pickupOrder.set("sign_status", "未回单");
            if(returnTime != null && !"".equals(returnTime)){
            	pickupOrder.set("return_time", returnTime);
            }
           
            pickupOrder.set("create_stamp", new Date());
            if (!"own".equals(getPara("pickupMode"))) {
                if (!getPara("sp_id").equals("")) {
                    pickupOrder.set("sp_id", getPara("sp_id"));
                }
            }
            if (driverId != null && !"".equals(driverId)) {
                pickupOrder.set("driver_id", driverId);
            }else{            	
            	pickupOrder.set("driver_id", null);
            }
            if (carinfoId != null && !"".equals(carinfoId)) {
                pickupOrder.set("carinfo_id", carinfoId);
            }
            
            String address_type = getPara("address_type");
            if("yard".equals(address_type)){
            	pickupOrder.set("is_direct_deliver", false);
            	pickupOrder.set("address", getPara("address"));
            	pickupOrder.set("pickup_type", "yard");
            }else if("warehouse".equals(address_type)){
            	if(gateInSelect == "" || gateInSelect == null){
            		pickupOrder.set("warehouse_id", replenishmentOrderId);
            	}else{
            		pickupOrder.set("warehouse_id", gateInSelect);
            	}
            	pickupOrder.set("pickup_type", "warehouse");
            	pickupOrder.set("is_direct_deliver", false);
            }else if("twice_pickup".equals(address_type)){
            	pickupOrder.set("pickup_type", "twice_pickup");
            	pickupOrder.set("is_direct_deliver", false);
            }else{
            	pickupOrder.set("pickup_type", "direct");
            	pickupOrder.set("is_direct_deliver", true);
            	
            	for(int i = 0; i < orderids.length ; i++){
            		TransferOrder transferOrder = TransferOrder.dao.findById(orderids[i]);
                	transferOrder.set("receiving_address", con_address).update();
            	}
            	
            }
            
            pickupOrder.set("car_summary_type", "untreated");
            pickupOrder.save();
            
            savePickupOrderMilestone(pickupOrder);
            
            
            
            List<Map> idList = new Gson().fromJson(strJson, 
    				new TypeToken<List<Map>>(){}.getType());
    		for (Map map : idList) {
    			String orderId = (String)map.get("id");         //运输单号
    			String order_type = (String)map.get("order_type");//单据类型
    			String numbers = (String)map.get("number");          //选择的数量 
    			String cargoItemId = (String)map.get("cargoItemId");  //itemId
    			String detail_ids = (String)map.get("detail_ids");
    			
    			if(pickupOrder.getLong("office_id")==null){
    				TransferOrder tor = TransferOrder.dao.findById(orderId);
        			pickupOrder.set("office_id", tor.getLong("office_id")).update();
    			}
    			
    			
    			if(StringUtils.isNotEmpty(getPara("flag"))){
    				if("derect".equals(getPara("flag"))){
        				List<Record> res = Db.find("select id from transfer_order_item_detail where order_id = ?",orderId);
        				for(Record re :res){
        					long detailid = re.getLong("id");
        					TransferOrderItemDetail detail = TransferOrderItemDetail.dao.findById(detailid);
        					detail.set("pickup_id",pickupOrder.getLong("id")).update();
        				}
        			}
    			}else{
    				//更新ATM detail表pickup_ID
        			String[] detailIds = detail_ids.split(",");
                    if(!detail_ids.equals("") && detail_ids != null){
        	            for (int i = 0; i < detailIds.length; i++) {
        					TransferOrderItemDetail detail = TransferOrderItemDetail.dao.findById(detailIds[i]);
        					
        					if("twice_pickup".equals(address_type)){
        						if(detail.getLong("twice_pickup_id") == null){
        							detail.set("twice_pickup_id",pickupOrder.getLong("id")).update();
        						}
                        	}else{
                        		if(detail.getLong("pickup_id") == null){
                        			detail.set("pickup_id",pickupOrder.getLong("id")).update();
                        		}
                        	}
        	            }
                    }
    			}
    			
    			
    			
    			//ATM单品
                if(order_type.equals("ATM")){
	            	//运输单单品总数
	            	Record totalTransferOrderAmount = Db.findFirst("select count(0) total from transfer_order_item_detail where order_id = ?",orderId);
	            	Long total = totalTransferOrderAmount.getLong("total");
	            	//总提货数量（之前+现在）
	            	String pickup_type ="";
	            	if("twice_pickup".equals(address_type)){
	            		pickup_type = "twice_pickup_id";
	            	}else{
	            		pickup_type = "pickup_id";
	            	}
	            	Record totalPickAmount = Db.findFirst("select count(0) total from transfer_order_item_detail where "+ pickup_type +" is not null and order_id = ?",orderId);
	            	Long before_number = totalPickAmount.getLong("total");
	            	
	            	//更新运输单
					TransferOrder transferOrderATM = TransferOrder.dao.findById(orderId);
					if(total == before_number){
						transferOrderATM.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
					}else{
						transferOrderATM.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
					}
					transferOrderATM.set("pickup_mode", pickupOrder.getStr("pickup_mode"));
					transferOrderATM.set("status", "处理中");
					transferOrderATM.update();
					//从表
					DepartTransferOrder departTransferOrder = new DepartTransferOrder();
		            departTransferOrder.set("pickup_id", pickupOrder.getLong("id"));
		            departTransferOrder.set("order_id", orderId);
		            departTransferOrder.set("amount", numbers);
		            departTransferOrder.set("transfer_order_no", transferOrderATM.getStr("order_no"));
		            if("twice_pickup".equals(address_type)){
		            	departTransferOrder.set("twice_pickup_flag", "Y");
		            }
		            departTransferOrder.save();
                }
                
                
                //普货 - 多次提货
                if(order_type.equals("普通货品")){
	            	//总提货数量（之前+现在）
	            	double sumPickAmount = 0;
	            	//运输单货品总数（sum）
	            	double sumTransferOrderItemAmount = 0;
	            	
                	TransferOrder transferOrderCargo = TransferOrder.dao.findById(orderId);
	            	String[] number = numbers.split(",");
	            	String[] ItemId = cargoItemId.split(",");
					for (int j = 0; j < number.length ; j++) {
						TransferOrderItem transferOrderItem = TransferOrderItem.dao.findById(ItemId[j]);
						if(!"".equals(number[j].trim())){
							//更新字表调车数量
							double pickupAmount = 0;
							
							if("twice_pickup".equals(address_type)){  //二次提货
								if(transferOrderItem.getDouble("twice_pickup_number") != null && !"".equals(transferOrderItem.getDouble("twice_pickup_number"))){
									pickupAmount = transferOrderItem.getDouble("twice_pickup_number");
								}
								transferOrderItem.set("twice_pickup_number",pickupAmount + Double.parseDouble(number[j])).update();
								transferOrderItem.set("have_twice_pickup",transferOrderItem.getDouble("have_twice_pickup") + Double.parseDouble(number[j])).update();
							}else{
								if(transferOrderItem.getDouble("pickup_number") != null && !"".equals(transferOrderItem.getDouble("pickup_number"))){
									pickupAmount = transferOrderItem.getDouble("pickup_number");
								}
								
								//作用？ 为了创建列表算正常非一次提货后的数量all - pickup_num - have_twice_pickup
								transferOrderItem.set("pickup_number",pickupAmount + Double.parseDouble(number[j])).update();
								if(transferOrderItem.getDouble("have_twice_pickup")>0){
									transferOrderItem.set("have_twice_pickup",transferOrderItem.getDouble("have_twice_pickup")-Double.parseDouble(number[j])).update();
								}
								
							}
							
							
							//从表
							DepartTransferOrder departTransferOrder = new DepartTransferOrder();
				            departTransferOrder.set("pickup_id", pickupOrder.getLong("id"));
				            departTransferOrder.set("order_id", orderId);
				            if("twice_pickup".equals(address_type)){  //二次提货
				            	departTransferOrder.set("twice_pickup_flag","Y");
				            	sumPickAmount += transferOrderItem.getDouble("twice_pickup_number");
								sumTransferOrderItemAmount += transferOrderItem.getDouble("amount");
				            }
				            departTransferOrder.set("amount", number[j]);
				            departTransferOrder.set("order_item_id", ItemId[j]);
				            departTransferOrder.set("transfer_order_no", transferOrderCargo.getStr("order_no"));
				            departTransferOrder.save();
				            
							sumPickAmount += transferOrderItem.getDouble("pickup_number");
							sumTransferOrderItemAmount += transferOrderItem.getDouble("amount");
						}
					}
					
					//更新运输单
					if(sumPickAmount == sumTransferOrderItemAmount){
						if("twice_pickup".equals(address_type)){  //二次提货
							transferOrderCargo.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_NEW);
						}else{
							transferOrderCargo.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
						}
					}else{
						transferOrderCargo.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
					}
					transferOrderCargo.set("status", "处理中");
					transferOrderCargo.set("pickup_mode", pickupOrder.getStr("pickup_mode")).update(); 
                }
                
                
              //更新运输单里程碑
				TransferOrderMilestone milestone = new TransferOrderMilestone();
				milestone.set("status", "提货中");
		        milestone.set("create_by", LoginUserController.getLoginUserId(this));
		        milestone.set("location", "");
		        milestone.set("create_stamp", new Date());
		        milestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
		        milestone.set("order_id",orderId);
		        milestone.save();
    		}    
        } else {
            pickupOrder = DepartOrder.dao.findById(pickId);
            pickupOrder.set("charge_type", getPara("chargeType"));
            pickupOrder.set("status", getPara("status"));
            pickupOrder.set("create_by", getPara("create_by"));
            pickupOrder.set("car_no", getPara("car_no"));
            pickupOrder.set("car_type", getPara("car_type"));
            pickupOrder.set("car_size", getPara("car_size"));
            pickupOrder.set("driver", getPara("driver_name"));
            pickupOrder.set("phone", getPara("driver_phone"));
            pickupOrder.set("remark", getPara("remark"));
            pickupOrder.set("combine_type", DepartOrder.COMBINE_TYPE_PICKUP);
            pickupOrder.set("pickup_mode", getPara("pickupMode"));
            pickupOrder.set("address", getPara("address"));
            pickupOrder.set("turnout_time", getPara("turnout_time"));
            if(returnTime != null && !"".equals(returnTime)){
            	pickupOrder.set("return_time", returnTime);
            }
            updateTransferOrderPickupMode(pickupOrder);
            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            pickupOrder.set("create_stamp", sqlDate);
            if (!"own".equals(getPara("pickupMode"))) {
                if (!getPara("sp_id").equals("")) {
                    pickupOrder.set("sp_id", getPara("sp_id"));
                }
            } else {
                pickupOrder.set("sp_id", null);
            }
            if (driverId != null && !"".equals(driverId)) {
                pickupOrder.set("driver_id", driverId);
            }else{            	
            	pickupOrder.set("driver_id", null);
            }
            if (carinfoId != null && !"".equals(carinfoId)) {
                pickupOrder.set("carinfo_id", carinfoId);
            }
            
            pickupOrder.set("create_stamp", new Date());
            if (!"own".equals(getPara("pickupMode"))) {
                if (!getPara("sp_id").equals("")) {
                    pickupOrder.set("sp_id", getPara("sp_id"));
                }
            }
            if (driverId != null && !"".equals(driverId)) {
                pickupOrder.set("driver_id", driverId);
            }else{            	
            	pickupOrder.set("driver_id", null);
            }
            if (carinfoId != null && !"".equals(carinfoId)) {
                pickupOrder.set("carinfo_id", carinfoId);
            }
            
            String address_type = getPara("address_type");
            if("yard".equals(address_type)){
            	pickupOrder.set("address", getPara("address"));
            	pickupOrder.set("pickup_type", "yard");
            }else if("warehouse".equals(address_type)){
            	if(gateInSelect == "" || gateInSelect == null){
            		pickupOrder.set("warehouse_id", replenishmentOrderId);
            	}else{
            		pickupOrder.set("warehouse_id", gateInSelect);
            	}
            	pickupOrder.set("pickup_type", "warehouse");
            }else if("twice_pickup".equals(address_type)){
            	pickupOrder.set("pickup_type", "twice_pickup");
            }else{
            	pickupOrder.set("pickup_type", "direct");
            	TransferOrder transferOrder = TransferOrder.dao.findById(orderids[0]);
            	transferOrder.set("receiving_address", con_address).update();
            }

            pickupOrder.update();
        }
        
        //跟车人员
        List<PickupDriverAssistant> assistantList = PickupDriverAssistant.dao.find("select id from pickup_driver_assistant where pickup_id = ?",pickupOrder.getLong("id"));
        for (PickupDriverAssistant pickupDriverAssistant : assistantList) {
        	PickupDriverAssistant.dao.deleteById(pickupDriverAssistant.getLong("id"));
		}
        for (int i = 0; i < driverAssistantIds.length; i++) {
        	if(!"".equals(driverAssistantIds[i])){
        		PickupDriverAssistant assistant = new PickupDriverAssistant();
            	assistant.set("pickup_id",pickupOrder.getLong("id"))
            	.set("driver_assistant_id",driverAssistantIds[i])
            	.set("name",driverAssistantNames[i])
            	.set("phone",driverAssistantPhones[i])
            	.save();
        	}
		}
        renderJson(pickupOrder);
    }
    
    
    //撤销调车单
    @Before(Tx.class)
 	public void cancelPickupOder() {
 		String pickupId = getPara("pickupId");
 		Record return_rec = new Record();
 		return_rec.set("success", false);
 		//不存在单号，返回
 		if(pickupId==null || ("").equals(pickupId)){
 			return_rec.set("msg", "单号为空");
 			renderJson(return_rec);
 			return ;
 		}
 		
 		//校验是否存在下级单据	
 		//1.判断此单据是否为直送单据
 		DepartOrder departOrder = DepartOrder.dao.findById(pickupId);
 		//检查调车单是否存在下级财务单据（应付）
 		String audit_status = departOrder.getStr("audit_status");
 		if(!"新建".equals(audit_status) && !"已确认".equals(audit_status)){
 			return_rec.set("msg", "此单据已做应付财务单据");
 			renderJson(return_rec);
 			return ;
 		}
 		
 		String car_summary_type = departOrder.getStr("car_summary_type");
		if("processed".equals(car_summary_type)){
			return_rec.set("msg", "此单据已做行车单，无法更改了哦");
 			renderJson(return_rec);
 			return ;
		}
 		
 		boolean is_direct_deliver = departOrder.getBoolean("is_direct_deliver");
 		//若为直送
 		if(is_direct_deliver){
 			//删除相对应回单
 			if(!deleteReturnOrder(pickupId)){
 				return_rec.set("msg", "此单据的回单已做应收财务单据");
 	 			renderJson(return_rec);
 	 			return ;
 			}
 		}
 		
 		//2.校验调车单是否存在下级单据
 		DepartPickupOrder dpOrder = DepartPickupOrder.dao.findFirst("select * from depart_pickup where pickup_id = ?",pickupId);
 		if(dpOrder == null){
 			//ATM正常提货（非直送）撤销
 			deleteOrder(pickupId);
 			
 			//删除主单据
 			departOrder.delete();
 			
 			return_rec.set("success", true);
 			renderJson(return_rec);
 		}else{
 			renderJson(return_rec);
 		}
 		
 	}
    
    //二次提货撤销
    @Before(Tx.class)
    public void cancelTwicePickupOrder(){
    	String order_id = getPara("pickupId");
    	Record order = new Record();
    	String Message = "";
    	Boolean result = false;
    	
    	
    	
    	if(StrKit.notBlank(order_id)){
    		order = Db.findById("depart_order", order_id);
    		if("untreated".equals(order.getStr("car_summary_type"))){
    			
    			List<Record> depart_transfer_list = Db.find("SELECT dt.* FROM depart_transfer dt"
    					+ " LEFT JOIN depart_order deo ON deo.id = dt.pickup_id"
    					+ " WHERE deo.id =? GROUP BY dt.order_id ",order_id);
    			if(depart_transfer_list.size()>0){
    				boolean return_flag = false;
    				for (Record record : depart_transfer_list) {
    					Long trans_id = record.getLong("order_id");
    					Record trans_rec = Db.findById("transfer_order", trans_id);
    					String cargo_nature = trans_rec.getStr("cargo_nature");
    					String cargo_nature_detail = trans_rec.getStr("cargo_nature_detail");
    					if(trans_id != null){
    						if("ATM".equals(cargo_nature)){
    							//当前提货单所勾选的明细
    							List<Record> pickup_detail = Db.find("SELECT * FROM transfer_order_item_detail "
        								+ " WHERE order_id = ? AND twice_pickup_id = ? and ifnull(pickup_id, '') != ''", trans_id, order_id);
    							//判断当前明细是否做了下级单据
    							if(pickup_detail.size() > 0){
    								result = false;
    								Message = "此单据已做下级单据，无法撤销";
    								return_flag = true;
    								
    							}
    						} else {
    							Double this_amount = record.getDouble("amount");
    							Long toi_id = record.getLong("order_item_id");
    							Record toi_rec = Db.findById("transfer_order_item", toi_id);
    							Double have_twice_pickup = toi_rec.getDouble("have_twice_pickup");
    							if(this_amount > have_twice_pickup){
    								result = false;
    								Message = "此单据已做下级单据，无法撤销";
    								return_flag = true;
    							}
    						}
    					}
    				}
    				if(return_flag){
    					order.set("result", result);
				    	order.set("Message", Message);
				    	renderJson(order);
						return;
    				}
    				
    				
    				
    				for (Record record : depart_transfer_list) {
						Long transfer_order_id = record.getLong("order_id");
						if(transfer_order_id!=null){
							Record transfer_order = Db.findById("transfer_order", transfer_order_id);
							if("ATM".equals(transfer_order.getStr("cargo_nature")) || "cargoNatureDetailYes".equals(transfer_order.getStr("cargo_nature_detail"))){
								//当前提货单所勾选的明细
								List<Record> pickup_detail = Db.find("SELECT * FROM transfer_order_item_detail "
	    								+ " WHERE order_id = ? AND twice_pickup_id = ?", transfer_order_id, order_id);
								
								//撤回该提货单在明细的记录
								for(Record item : pickup_detail){
									item.set("twice_pickup_id", null);
									Db.update("transfer_order_item_detail",item);
								}
								
								//该运输单的货品总明细
								List<Record> pickup_total_detail = Db.find("SELECT * FROM transfer_order_item_detail "
	    								+ " WHERE order_id = ? and (ifnull(pickup_id,'') != '' || ifnull(twice_pickup_id,'') != '')", transfer_order_id);
								
    							if(pickup_total_detail.size()>0){
//    								transfer_order.set("pickup_assign_status", "PARTIAL");
    								transfer_order.set("STATUS", "处理中");
    							}else{
    								transfer_order.set("pickup_assign_status", "NEW");
    								transfer_order.set("STATUS", "新建");
    							}
    							
    							
    							if("cargo".equals(transfer_order.getStr("cargo_nature"))&&"cargoNatureDetailYes".equals(transfer_order.getStr("cargo_nature_detail"))){
    								List<Record> transfer_order_item = Db.find("SELECT toi.*,dt.amount depart_amount FROM depart_transfer dt"
    										+ " LEFT JOIN transfer_order_item toi ON toi.`id` = dt.`order_item_id` WHERE dt.`order_id` = ? AND dt.`pickup_id` = ? ",transfer_order_id,order_id);
    								for (Record record2 : transfer_order_item) {
    									record2.set("twice_pickup_number", (record2.getDouble("twice_pickup_number")-record2.getDouble("depart_amount")));
    									record2.set("have_twice_pickup", (record2.getDouble("have_twice_pickup")-record2.getDouble("depart_amount")));
    									record2.remove("depart_amount");
    									Db.update("transfer_order_item",record2);
									}
    							}
							}else if("cargo".equals(transfer_order.getStr("cargo_nature"))){

								List<Record> transfer_order_item  =Db.find("SELECT * FROM depart_transfer WHERE pickup_id = ? AND order_id = ? AND twice_pickup_flag ='Y'   ",order_id,transfer_order_id);
								int  check_result = 0;
								for (Record record_item : transfer_order_item) {
									Record transfer_item  =Db.findById("transfer_order_item", record_item.getLong("order_item_id"));
									if(record_item.getDouble("amount")==transfer_item.getDouble("amount")){
										transfer_item.set("have_twice_pickup", 0);
										transfer_item.set("twice_pickup_number", 0);
										Db.update("transfer_order_item",transfer_item);
									}else{
										transfer_item.set("have_twice_pickup",(transfer_item.getDouble("have_twice_pickup")-record_item.getDouble("amount")));
										transfer_item.set("twice_pickup_number", (transfer_item.getDouble("twice_pickup_number")-record_item.getDouble("amount")));
										Db.update("transfer_order_item",transfer_item);
									}
									if(transfer_item.getDouble("have_twice_pickup")!=0&&transfer_item.getDouble("twice_pickup_number")!=0){
										check_result++;
									}
									List <Record> twice_item_check =Db.find("SELECT * FROM transfer_order_item WHERE order_id =?  AND twice_pickup_number !=0 AND id != ?",transfer_order_id,transfer_item.getLong("id"));
									if(twice_item_check.size()>0){
										check_result ++ ;
									}
									
								}
								if(check_result>0){
									transfer_order.set("pickup_assign_status", "PARTIAL");
								}else{
									transfer_order.set("pickup_assign_status", "NEW");
    								transfer_order.set("STATUS", "新建");
								}
							}
							result=Db.update("transfer_order",transfer_order);
						}else{
							Message = "数据异常，撤销失败";
						}
					}
    			}else{
    				Message = "数据异常，撤销失败";
    			}
    			
    			List<Record> remove_depart_transfer_list = Db.find("SELECT dt.* FROM depart_transfer dt  LEFT JOIN depart_order deo ON deo.id = dt.pickup_id WHERE deo.id =? ",order_id);
    			for (Record record : remove_depart_transfer_list) {
					Db.delete("depart_transfer", record);
				}
    			Db.delete("depart_order", order);
    			Message="撤销成功,一秒后自动返回调车单列表。。。";
    		}else{
    			Message = "该单据已做行车单,请先撤销相应行车单再进行此操作";
    		}
    	}else{
    		Message = "单据不存在，请检查数据或重试";
    	}
    	order.set("result", result);
    	order.set("Message", Message);
    	renderJson(order);
    }
    
    //撤销回单
    @Before(Tx.class)
    public boolean deleteReturnOrder(String pickupId){
    	List<DepartTransferOrder> dts = DepartTransferOrder.dao.find("select * from depart_transfer where pickup_id = ?",pickupId);
    	for(DepartTransferOrder dt : dts){
    		long transfer_id = dt.getLong("order_id");
    		
    		ReturnOrder returnOrder = ReturnOrder.dao.findFirst("select * from return_order where transfer_order_id =?",transfer_id);
    		long return_id = 0;
    		if(returnOrder != null)
    			return_id = returnOrder.getLong("id");
    		else
    			return true;
    		
    		//校验是否有下级财务单据（应收）
    		String transaction_status = returnOrder.getStr("transaction_status");
    		if(!"新建".equals(transaction_status) && !"已签收".equals(transaction_status)){
    			return false;
    		}
    		
    			
    		List<ReturnOrderFinItem> rofis = ReturnOrderFinItem.dao.find("select * from return_order_fin_item where return_order_id = ?",return_id);
    		for(ReturnOrderFinItem rofi:rofis){
    			rofi.delete();
    		}
    		returnOrder.delete();
    	}
    	
    	return true;
    }
    
    //正常提货）撤销
    public void deleteOrder(String pickupId){
    	//清除提货单里程碑
    	List<TransferOrderMilestone> tomilestones = TransferOrderMilestone.dao.find("SELECT * FROM transfer_order_milestone where pickup_id = '" + pickupId+ "'");
    	for(TransferOrderMilestone tomilestone : tomilestones){
    		tomilestone.delete();
    	}
    	
    	//更新单品信息表信息
    	List<TransferOrderItemDetail> toids = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where pickup_id = ? or twice_pickup_id = ?",pickupId,pickupId);
    	for(TransferOrderItemDetail toid : toids){
    		toid.set("pickup_id", null).update();
    	}
    	
    	//清除中间表数据
    	List<DepartTransferOrder> dts = DepartTransferOrder.dao.find("select * from depart_transfer where pickup_id = ?",pickupId);
    	for(DepartTransferOrder dt : dts){
    		long transfer_id = dt.getLong("order_id");
    		
    		//更新对应运输单信息
    		TransferOrder tor = TransferOrder.dao.findById(transfer_id);
    		String order_type = tor.getStr("cargo_nature");
    		TransferOrderItemDetail toid = TransferOrderItemDetail.dao.findFirst("select * from transfer_order_item_detail where (ifnull(pickup_id,'') != '' or ifnull(twice_pickup_id,'') != '') and order_id= ? ",transfer_id);
    		if(toid==null){
    			if("ATM".equals(order_type) || ("cargo".equals(order_type) && "cargoNatureDetailYes".equals(tor.getStr("cargo_nature_detail")))){
    				tor.set("pickup_assign_status", "NEW");
        			tor.set("pickup_seq", null);
        			tor.set("status", "新建");
    			}
    		}else{
    			TransferOrderItemDetail t = TransferOrderItemDetail.dao.findFirst("select * from transfer_order_item_detail "
    					+ "where ifnull(pickup_id,'') != '' and order_id= ? ",transfer_id);
    			if (t == null) {
    				tor.set("pickup_assign_status", "NEW");
    			} else {
    				tor.set("pickup_assign_status", "PARTIAL");
    			}
    			
    			tor.set("status", "处理中");
    		}
    		
    		
    		//如果是普货的话，更新单品表数量
        	if("cargo".equals(order_type)){
        		long item_id = dt.getLong("order_item_id");
        		Double amount = dt.getDouble("amount");
        		List<TransferOrderItem> tois = TransferOrderItem.dao.find("select * from transfer_order_item where id = ?",item_id);
        		for(TransferOrderItem toi: tois){
        			Double now = toi.getDouble("pickup_number");
        			Double now_twice = toi.getDouble("have_twice_pickup");
        			Double twice_pickup_number = toi.getDouble("twice_pickup_number");
        			toi.set("pickup_number", now-amount);
        			
        			if(twice_pickup_number >= (now_twice + amount)){
        				toi.set("have_twice_pickup", now_twice + amount);
        			}
        			toi.update();
        		}
        		
        		//计算普货提货的数量，从而来更新运输单的pickup_assign_status状态
        		Record record = Db.findFirst("select ifnull(sum(pickup_number),0) pickup_number from transfer_order_item where order_id = ? ",transfer_id);
        		Double pickup_number = record.getDouble("pickup_number");
        		if(pickup_number>0){
        			tor.set("pickup_assign_status", "PARTIAL");
        			tor.set("status", "处理中");
        		} else {
        			
        			//二次提货数量判断
        			Record r = Db.findFirst("select ifnull(sum(twice_pickup_number),0) twice_pickup_number from transfer_order_item where order_id = ? ",transfer_id);
        			Double twice_pickup_number = r.getDouble("twice_pickup_number");
        			if(twice_pickup_number > 0){
            			tor.set("status", "处理中");
        			}else{
        				tor.set("status", "新建");
        			}
        			
        			tor.set("pickup_assign_status", "NEW");
        			
        		}
        		
        		
        		
        	}
        	
        	tor.update();
    		
    		//情况运输单的里程碑信息
    		List<TransferOrderMilestone> transferMilies = TransferOrderMilestone.dao.find("SELECT * FROM transfer_order_milestone where status !='新建 ' and order_id = '" + transfer_id+ "'");
        	for(TransferOrderMilestone transferMilie : transferMilies){
        		transferMilie.delete();
        	}
        	
        	//删除中间表
    		dt.delete();
    	}
    }
    
 
    // 保存发车记录单
    @Before(Tx.class)
    public void saveCarManagePickupOrder() {
    	DepartOrder pickupOrder = null;
    	String pickId = getPara("pickupId");
    	if (pickId != null && !"".equals(pickId)) {    		
    		pickupOrder = DepartOrder.dao.findById(pickId);
    		pickupOrder.set("car_follow_name", getPara("car_follow_name"));
            pickupOrder.set("car_follow_phone", getPara("car_follow_phone"));
    		pickupOrder.set("kilometres", getPara("kilometres").equals("") ? 0 : getPara("kilometres"));
            pickupOrder.set("road_bridge", getPara("roadBridge").equals("") ? 0 : getPara("roadBridge"));
            pickupOrder.set("income", getPara("income").equals("") ? 0 : getPara("income"));    		
    		pickupOrder.update();
    	}
    	renderJson(pickupOrder);
    }

    // 更新运输单的提货方式
    private void updateTransferOrderPickupMode(DepartOrder pickupOrder) {
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find("select * from depart_transfer where pickup_id = ?",
                pickupOrder.getLong("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.getLong("order_id"));
            if (transferOrder != null) {
                transferOrder.set("pickup_mode", pickupOrder.getStr("pickup_mode"));
                transferOrder.update();
            }
        }
    }

    // 更新中间表
    @Before(Tx.class)
    private void updateDepartTransfer(DepartOrder pickupOrder, String orderId, String checkedDetail, String uncheckedDetailId) {
        if (checkedDetail != null && !"".equals(checkedDetail)) {
            String[] checkedDetailIds = checkedDetail.split(",");
            TransferOrderItemDetail transferOrderItemDetail = null;
            for (int j = 0; j < checkedDetailIds.length && checkedDetailIds.length > 0; j++) {
                transferOrderItemDetail = TransferOrderItemDetail.dao.findById(checkedDetailIds[j]);
                transferOrderItemDetail.set("pickup_id", pickupOrder.getLong("id"));
                transferOrderItemDetail.update();
            }
            TransferOrder transferOrder = TransferOrder.dao.findById(transferOrderItemDetail.getLong("order_id"));
            transferOrder.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
            transferOrder.update();
        }
        String[] uncheckedDetailIds = uncheckedDetailId.split(",");
        if (uncheckedDetailId != null && !"".equals(uncheckedDetailId)) {
            TransferOrderItemDetail transferOrderItemDetail = null;
            for (int j = 0; j < uncheckedDetailIds.length && uncheckedDetailIds.length > 0; j++) {
                transferOrderItemDetail = TransferOrderItemDetail.dao.findById(uncheckedDetailIds[j]);
                transferOrderItemDetail.set("pickup_id", null);
                transferOrderItemDetail.update();
            }
        }
        if (uncheckedDetailIds.length == 0 || "".equals(uncheckedDetailIds[0])) {
            List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
                    .find("select * from transfer_order_item_detail where order_id in(" + orderId + ")");
            String str = "";
            for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
                Long departId = transferOrderItemDetail.getLong("pickup_id");
                if (departId == null || "".equals(departId)) {
                    str += departId;
                }
            }
            if ("".equals(str)) {
                List<TransferOrder> transferOrders = TransferOrder.dao.find("select * from transfer_order where id in (" + orderId + ")");
                for (TransferOrder transferOrder : transferOrders) {
                    transferOrder.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
                    transferOrder.update();
                }
            }
        }
    }


    //问题解决:    select group_concat(  cast(id as char)   ) funIds from fun
    //使用时取：   pickupOrder.getStr("funIds")
    @Before(Tx.class)
    private void pickupOrderEdit(){
    	String sql = "select dor.*,co.contact_person,co.phone,u.user_name,(select group_concat(cast(dt.order_id as char)  separator',')  from depart_transfer  dt "
                + "where dt.pickup_id =dor.id)as order_id from depart_order  dor "
                + "left join contact co on co.id in( select p.contact_id  from party p where p.id=dor.driver_id ) "
                + "left join user_login  u on u.id=dor.create_by where dor.combine_type ='"
                + DepartOrder.COMBINE_TYPE_PICKUP
                + "' and dor.id in(" + getPara("id") + ")";
        DepartOrder pickupOrder = DepartOrder.dao.findFirst(sql);
        setAttr("pickupOrder", pickupOrder);
        if(pickupOrder.getBoolean("is_direct_deliver")){
        	setAttr("flag", "derect");
        }else{
        	setAttr("flag", "underect");
        }
        
        String list = pickupOrder.getStr("order_id");
        String[] transferOrderIds = list.split(",");
        TransferOrder transferOrderAttr = TransferOrder.dao.findById(transferOrderIds[0]);
        setAttr("transferOrderAttr", transferOrderAttr);
        
        String sq = "SELECT tra.receiving_address FROM transfer_order tra "
                + " where tra.id in("+transferOrderIds[0]+")";
        Record tro = Db.findFirst(sq);
        setAttr("transferOrder",tro);
        setAttr("t_id",transferOrderIds[0]);
        
        

        Long sp_id = pickupOrder.getLong("sp_id");
        if (sp_id != null) {
            Party sp = Party.dao.findById(sp_id);
            Contact spContact = Contact.dao.findById(sp.getLong("contact_id"));
            setAttr("spContact", spContact);
        }
        Long driverId = pickupOrder.getLong("driver_id");
        if (driverId != null) {
            Party driver = Party.dao.findById(driverId);
            Contact driverContact = Contact.dao.findById(driver.getLong("contact_id"));
            setAttr("driverContact", driverContact);
        }
        Long carinfoId = pickupOrder.getLong("carinfo_id");
        if (carinfoId != null) {
            Carinfo carinfo = Carinfo.dao.findById(carinfoId);
            setAttr("carinfo", carinfo);
        }
        UserLogin userLogin = UserLogin.dao.findById(pickupOrder.getLong("create_by"));
        setAttr("userLogin2", userLogin);
        setAttr("pickup_id", getPara());
        String orderId = "";
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find("select * from depart_transfer where pickup_id = ?",
                pickupOrder.getLong("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            orderId += departTransferOrder.getLong("order_id") + ",";
        }
        orderId = orderId.substring(0, orderId.length() - 1);
        setAttr("transferIds", orderId);

        TransferOrderMilestone transferOrderMilestone = TransferOrderMilestone.dao.findFirst(
                "select * from transfer_order_milestone where pickup_id = ? order by create_stamp desc", pickupOrder.getLong("id"));
        setAttr("transferOrderMilestone", transferOrderMilestone);
        
        List<Record> paymentItemList = Collections.EMPTY_LIST;
        paymentItemList = Db.find("select * from fin_item where type='应付'");
        setAttr("paymentItemList", paymentItemList);
        List<Record> incomeItemList = Collections.EMPTY_LIST;
        incomeItemList = Db.find("select * from fin_item where type='应收'");
        setAttr("incomeItemList", incomeItemList);
        
        String finItemIds = "";
        List<PickupOrderFinItem> PickupOrderFinItems = PickupOrderFinItem.dao.find("select * from pickup_order_fin_item where pickup_order_id = ?", pickupOrder.getLong("id"));
        for(PickupOrderFinItem pickupOrderFinItem : PickupOrderFinItems){
        	finItemIds += pickupOrderFinItem.getLong("fin_item_id") + ",";
        }
        if(finItemIds != null && !"".equals(finItemIds)){
        	finItemIds = finItemIds.substring(0, finItemIds.length() - 1);
        }
        setAttr("finItemIds", finItemIds);        
    }
    
    // 修改拼车单页面
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_UPDATE})
    public void edit() {
    	pickupOrderEdit();
    	Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
    	setAttr("customizeField", customizeField);
        render("/yh/pickup/editPickupOrder.html");
    }
    
    // 修改拼车单页面
    public void carManageEdit() {
    	pickupOrderEdit();
    	Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
    	setAttr("customizeField", customizeField);
    	render("/yh/pickup/carManageEditPickupOrder.html");
    }

    // 保存拼车里程碑
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_CREATE})
    @Before(Tx.class)
    private void savePickupOrderMilestone(DepartOrder pickupOrder) {
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        transferOrderMilestone.set("status", "新建");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).getLong("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE);
        transferOrderMilestone.set("pickup_id", pickupOrder.getLong("id"));
        transferOrderMilestone.save();
    }
    

    // 在途提货拼车单
    public void pickupOrderMilestone() {
        render("/yh/pickup/pickupOrderMilestone.html");
    }

    // 列出所有的在途提货拼车单
    public void pickupOrderMilestoneList() {
        Map<String, List> map = new HashMap<String, List>();
        List<String> usernames = new ArrayList<String>();
        List<TransferOrderMilestone> milestones = new ArrayList<TransferOrderMilestone>();
        List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                .find("select pickup_id from transfer_order_milestone where type = '" + TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE
                        + "' group by pickup_id");
        for (TransferOrderMilestone pm : transferOrderMilestones) {
            if (pm.getLong("pickup_id") != null) {
                TransferOrderMilestone transferOrderMilestone = TransferOrderMilestone.dao
                        .findFirst("select tom.*,dto.depart_no from transfer_order_milestone tom "
                                + " left join depart_order dto on dto.id = " + pm.getLong("pickup_id") + " where tom.type = '"
                                + TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE + "' and tom.pickup_id=" + pm.getLong("pickup_id")
                                + " and dto.combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "' order by tom.create_stamp desc");
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.getLong("create_by"));
                String username = userLogin.getStr("user_name");
                milestones.add(transferOrderMilestone);
                usernames.add(username);
            }
        }
        map.put("milestones", milestones);
        map.put("usernames", usernames);
        renderJson(map);
    }

    // 完成
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_COMPLETED})
    @Before(Tx.class)
    public void finishPickupOrder() {
        String pickupOrderId = getPara("pickupOrderId");
        DepartOrder pickupOrder = DepartOrder.dao.findById(pickupOrderId);
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find("select * from depart_transfer where pickup_id = ?",
                pickupOrder.getLong("id"));

        boolean direct=false;
        if(pickupOrder.getBoolean("is_direct_deliver")!=null){
        	direct = pickupOrder.getBoolean("is_direct_deliver");
        }
        String pickup_type = pickupOrder.getStr("pickup_type");

        //非直送调车
        if(!direct){
        	String order_type = null;
	        //相关运输单业务处理:提货发车之后，运输单中除了补货订单状态为已入库外，其他都是默认为正在处理状态
	        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
	            TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.getLong("order_id"));
	            TransferOrderMilestone milestone = new TransferOrderMilestone();
	            order_type = transferOrder.getStr("order_type");
	            String pickup_status  = transferOrder.getStr("pickup_assign_status");
	            if("twice_pickup".equals(pickup_type)){
	            	if(pickup_status.equals("PARTIAL")){
	            		milestone.set("status", "部分已二次提货");
	            	}else{
	            		milestone.set("status", "已二次提货");
	            		transferOrder.set("pickup_assign_status", "NEW").update();
	            	}
	            }else{
	            	if ("movesOrder".equals(order_type) ||"salesOrder".equals(order_type) || "arrangementOrder".equals(order_type) || "cargoReturnOrder".equals(order_type)) {//销售订单
		            	if(!"warehouse".equals(pickup_type)){
		            		if(pickup_status.equals("PARTIAL")){
			            		milestone.set("status", "部分已入货场");	
			            	}else{
			            		milestone.set("status", "已入货场");
			            	}
		                    if("arrangementOrder".equals(order_type) || "cargoReturnOrder".equals(order_type)){
		                    	SubtractInventory(pickupOrder,transferOrder);
		                    } 
		            	}else{
		            		if(pickup_status.equals("PARTIAL")){
			            		milestone.set("status", "部分已入库");
			            	}else{
			            		milestone.set("status", "已入库");
			            		transferOrder.set("status", "已完成").update();
			            	}
		            		
		            		createDeliveryOrder(transferOrder,pickupOrderId);
		            	}
	            		
		            } else if ("replenishmentOrder".equals(order_type)) {  //补货
		            	if(pickup_status.equals("PARTIAL")){
		            		milestone.set("status", "部分已入库");
		            	}else{
		            		milestone.set("status", "已入库");
		            	}
		            }
	            }
	            
	            
	            //新建里程碑
	            milestone.set("order_id", transferOrder.getLong("id"));
	            milestone.set("create_by",  LoginUserController.getLoginUserId(this));
	            milestone.set("create_stamp", new Date());
	            milestone.set("location", "");
	            milestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
	            milestone.save();
	        }
	        
	        TransferOrderMilestone pickupMilestone = new TransferOrderMilestone();
	        if("twice_pickup".equals(pickup_type)){
	        	pickupMilestone.set("status", "已二次提货");
	        	//更新调车单状态
                pickupOrder.set("status", "已二次提货");
            }else{
            	if ("movesOrder".equals(order_type) ||"salesOrder".equals(order_type) || "arrangementOrder".equals(order_type) || "cargoReturnOrder".equals(order_type)) {//销售订单
            		if(!"warehouse".equals(pickup_type)){
            			pickupMilestone.set("status", "已入货场");
        	        	//更新调车单状态
                        pickupOrder.set("status", "已入货场");
            		}else{ 
            			pickupMilestone.set("status", "已入库");
        	        	//更新调车单状态
                        pickupOrder.set("status", "已入库");
            		}
            		
                } else if ("replenishmentOrder".equals(order_type)) {  //补货
                	pickupMilestone.set("status", "已入库");
                	//更新调车单状态
                	pickupOrder.set("status", "已入库");
                }
            }
	        
	        pickupMilestone.set("create_by", LoginUserController.getLoginUserId(this));
	        pickupMilestone.set("create_stamp", new Date());
	        pickupMilestone.set("pickup_id", pickupOrder.getLong("id"));
	        pickupMilestone.set("type", TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE);
	        pickupMilestone.save();  
	        pickupOrder.update();
        }else{
        	 //自动生成相对于的回单（一运输单----一回单）
        	 receipt(pickupOrderId);
        	 
             //修改掉车单信息
             pickupOrder.set("status", "已收货")
             .set("sign_status", "已回单").update();

        	//获取运输单中的应付
        	String sql = "SELECT tor.* FROM transfer_order_fin_item tor "
        			+ " left join fin_item fi on fi.id = tor.fin_item_id "
        			+ " LEFT JOIN depart_transfer dt on tor.order_id = dt.order_id "
        			+ " where dt.pickup_id = "+ pickupOrderId
        			+ " and fi.type = '应付'";
        	Record rc = Db.findFirst(sql);
        	if(rc != null){
        		PickupOrderFinItem finItem = new PickupOrderFinItem();
                finItem.set("status", "新建");
                finItem.set("pickup_order_id", pickupOrderId);
                finItem.set("fin_item_id", rc.getLong("fin_item_id"));
                finItem.set("amount",  rc.getDouble("amount"));
                finItem.set("create_date",  rc.getDate("create_date"));
                finItem.set("remark",  rc.getStr("remark"));
                finItem.save();
        	}
        }

        // 生成应付, （如果已经有了应付，就要清除掉旧数据重新算）
        // 先获取有效期内的合同，条目越具体优先级越高
        // 级别1：计费类别 + 货 品 + 始发地 + 目的地
        // 级别2：计费类别 + 货 品 + 目的地
        // 级别3：计费类别 + 始发地 + 目的地
        // 级别4：计费类别 + 始发地

        // 按件生成提货单中供应商的应付，要算 item 的数量 * 合同中定义的价格
        // Depart_Order_fin_item 提货单/发车单应付明细表
        // 第一步：看提货单调度选择的计费方式是哪种：计件，整车，零担
        // 第二步：循环所选运输单中的 item, 到合同中（循环）比对去算钱。
        Long spId = pickupOrder.getLong("sp_id");
        if (spId == null) {
            logger.debug("供应商id=null");
            renderJson("{\"success\": false,\"reason\": \"供应商id=null\"}");
        }

        // 先获取有效期内的sp合同, 如有多个，默认取第一个
        Contract spContract = Contract.dao.findFirst("select * from contract where type='DELIVERY_SERVICE_PROVIDER' "
                + "and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id=" + spId);
        if (spContract == null) {
            logger.debug("没有效期内的sp合同~!");
            renderJson("{\"success\": false,\"reason\": \"没有效期内的sp合同\"}");
        }
        String chargeType = pickupOrder.getStr("charge_type");

        List<DepartTransferOrder> dItem = DepartTransferOrder.dao.find("select order_id from depart_transfer where pickup_id ='"
                + getPara("pickupOrderId") + "'");
        String transferId = "";
        for (DepartTransferOrder dItem2 : dItem) {
            transferId += dItem2.getLong("order_id") + ",";
        }
        transferId = transferId.substring(0, transferId.length() - 1);

        List<Record> transferOrderItemList = Db
                .find("select toi.*, t_o.route_from, t_o.route_to from transfer_order_item toi left join transfer_order t_o on toi.order_id = t_o.id where toi.order_id in("
                        + transferId + ") order by pickup_seq desc");
        String name = (String) currentUser.getPrincipal();
    	List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        //pickupMilestone.set("create_by", users.get(0).get("id"));
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        if (spId != null) {
            if ("perUnit".equals(chargeType)) {
                genFinPerUnit(pickupOrder, users, sqlDate, spContract, chargeType, transferOrderItemList);
            } else if ("perCar".equals(chargeType)) {
                // 拼车单没有始发地，目的地，无法计算整车的合同价
            } else if ("perCargo".equals(chargeType)) {

            }
        }

        // 生成客户支付中转费
        if (pickupOrder.getDouble("income") != null) {
            TransferOrderFinItem tFinItem2 = new TransferOrderFinItem();
            int size = dItem.size();
            for (int i = 0; i < dItem.size(); i++) {
                tFinItem2.set("fin_item_id", "4");// TODO 死代码
                tFinItem2.set("amount", Double.parseDouble(pickupOrder.getDouble("income").toString()) / size);
                // tFinItem2.set("order_id", dItem.get(i).get("order_id"));
                tFinItem2.set("status", "未完成");
                tFinItem2.set("creator", users.get(0).getLong("id"));
                tFinItem2.set("create_date", sqlDate);
                tFinItem2.save();
            }
        }
        renderJson(pickupOrder);
    }
    
    
    
    @Before(Tx.class)
    public void createDeliveryOrder(TransferOrder transferOrder,String pickup_id){
    	Party party=Party.dao.findById(transferOrder.getLong("customer_id"));
    	DepartOrder departOrder1= DepartOrder.dao.findById(pickup_id);
    	if("Y".equals(party.getStr("is_auto_ps"))){
    		List<TransferOrderItemDetail> transferorderitemdetail =TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where order_id = "+transferOrder.getLong("id")+" and pickup_id ="+ pickup_id);
    		for (TransferOrderItemDetail transferdetail:transferorderitemdetail) {
    			if(transferdetail.getLong("delivery_id")==null){
    				DeliveryOrder deliveryOrder = null;
            		String orderNo = OrderNoGenerator.getNextOrderNo("PS");
            		Date createDate = Calendar.getInstance().getTime();
            		String name = (String) currentUser.getPrincipal();
            		List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
            		Warehouse warehouse = Warehouse.dao.findFirst("SELECT * from warehouse where id=?",transferOrder.getLong("warehouse_id")); 
            		deliveryOrder = new DeliveryOrder();
            		deliveryOrder.set("order_no", orderNo)
    				.set("customer_id", transferOrder.getLong("customer_id"))
    				.set("notify_party_id", transferdetail.getLong("notify_party_id"))
    				.set("create_stamp", createDate).set("create_by", users.get(0).getLong("id")).set("status", "新建")
    				.set("route_from",transferOrder.getStr("route_to"))
    				.set("route_to",transferOrder.getStr("route_to"))
    				.set("pricetype", getPara("chargeType"))
    				.set("office_id", warehouse.getLong("office_id"))
    				.set("from_warehouse_id", transferOrder.getLong("warehouse_id"))
    				.set("cargo_nature", transferOrder.getStr("cargo_nature"))
    				.set("priceType", departOrder1.getStr("charge_type"))
    				.set("deliveryMode", "out_source")
    				.set("ref_no",transferOrder.getStr("sign_in_no"))
    				.set("warehouse_nature", "warehouseNatureNo")
    				.set("ltl_price_type", departOrder1.getStr("ltl_price_type")).set("car_type", departOrder1.getStr("car_type"))
    				.set("audit_status", "新建").set("sign_status", "未回单");
            		if(warehouse!=null){
            			deliveryOrder.set("sp_id", warehouse.getLong("sp_id"));
            		}
            		deliveryOrder.save();
    				DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
    				deliveryOrderItem.set("delivery_id",deliveryOrder.getLong("id"))
    				.set("transfer_order_id",transferOrder.getLong("id"))
    				.set("transfer_no",transferOrder.getStr("order_no"))
    				.set("transfer_item_detail_id",transferdetail.getLong("id"))
    				.set("amount", 1);
    				deliveryOrderItem.save();
    				//在单品中设置delivery_id
    				TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
    						.findById(transferdetail.getLong("id"));
    				transferOrderItemDetail.set("delivery_id",deliveryOrder.getLong("id"));
    				transferOrderItemDetail.set("is_delivered", true);
    				transferOrderItemDetail.update();
    			}
    		}
    	}
    }
    
    
    
    
    //直送调车完成后生成回单
    @Before(Tx.class)
    public void receipt(String pickupOrderId) {	
        String pickupId = pickupOrderId;         //调车单号
       
        //修改运输单信息
        List<DepartTransferOrder> departTransfers= DepartTransferOrder.dao.find("select * from depart_transfer where pickup_id = ? ",pickupId);
        for (DepartTransferOrder departTransferOrder : departTransfers) {
        	Long order_id = departTransferOrder.getLong("order_id");   //运单号

        	
            //更新运输单信息
        	TransferOrder transferOrder = TransferOrder.dao.findById(order_id);
			transferOrder.set("status", "已完成").update();
			//运输单日记账
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "已收货")
            .set("create_by", LoginUserController.getLoginUserId(this))
            .set("location", "")
            .set("create_stamp", new Date())
            .set("order_id", order_id)
            .set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE)
            .save();
            //调车单日记账
            transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "已收货")
            .set("create_by", LoginUserController.getLoginUserId(this))
            .set("location", "")
            .set("create_stamp", new Date())
            .set("depart_id", order_id)
            .set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE)
            .save();
            
          //校验是否有存在回单
        	Record rRec = Db.findFirst("select * from return_order ror where transfer_order_id = ?",order_id);
        	if(rRec != null){
        		return;
        	}
 
            //直接生成回单，在把合同等费用带到回单中
            String orderNo = OrderNoGenerator.getNextOrderNo("HD");
            ReturnOrder returnOrder = new ReturnOrder();
            returnOrder.set("order_no", orderNo)
            .set("transaction_status", "新建")
            .set("creator", LoginUserController.getLoginUserId(this))
            .set("create_date", new Date())
            .set("transfer_order_id", order_id)
            .set("office_id", transferOrder.getLong("office_id"))
            .set("customer_id", transferOrder.getLong("customer_id"))
            .save();

            ReturnOrderController roController= new ReturnOrderController(); 
            //把运输单的应收带到回单中
            roController.tansferIncomeFinItemToReturnFinItem(returnOrder, Long.parseLong("0"), order_id);
            //直送时把保险单费用带到回单
            roController.addInsuranceFin(transferOrder, returnOrder);
            //TODO:根据合同生成费用
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
            roController.calculateChargeByCustomer(transferOrder, returnOrder.getLong("id"), users);
		}
    }
    
    


    private void genFinPerUnit(DepartOrder pickupOrder, List<UserLogin> users, java.sql.Timestamp sqlDate, Contract spContract,
            String chargeType, List<Record> transferOrderItemList) {
        for (Record tOrderItemRecord : transferOrderItemList) {
            // TODO 这个是递归，可以整理成一个递归函数
            Record contractFinItem = Db.findFirst("select amount, fin_item_id from contract_item where contract_id ="
                    + spContract.getLong("id") + " and product_id =" + tOrderItemRecord.getLong("product_id") + " and from_id = '"
                    + tOrderItemRecord.getStr("route_from") + "' and to_id = '" + tOrderItemRecord.getStr("route_to") + "' and priceType='"
                    + chargeType + "'");

            if (contractFinItem != null) {
                genFinItem(pickupOrder, users, sqlDate, tOrderItemRecord, contractFinItem);
            } else {
                contractFinItem = Db.findFirst("select amount, fin_item_id from contract_item where contract_id ="
                        + spContract.getLong("id") + " and product_id =" + tOrderItemRecord.getLong("product_id") + " and from_id = '"
                        + tOrderItemRecord.getStr("route_from") + "' and priceType='" + chargeType + "'");

                if (contractFinItem != null) {
                    genFinItem(pickupOrder, users, sqlDate, tOrderItemRecord, contractFinItem);
                } else {
                    contractFinItem = Db.findFirst("select amount, fin_item_id from contract_item where contract_id ="
                            + spContract.getLong("id") + " and from_id = '" + tOrderItemRecord.getStr("route_from") + "' and to_id = '"
                            + tOrderItemRecord.getStr("route_to") + "' and priceType='" + chargeType + "'");

                    if (contractFinItem != null) {
                        genFinItem(pickupOrder, users, sqlDate, tOrderItemRecord, contractFinItem);
                    } else {
                        contractFinItem = Db.findFirst("select amount, fin_item_id from contract_item where contract_id ="
                                + spContract.getLong("id") + " and from_id = '" + tOrderItemRecord.getStr("route_from") + "' and priceType='"
                                + chargeType + "'");

                        if (contractFinItem != null) {
                            genFinItem(pickupOrder, users, sqlDate, tOrderItemRecord, contractFinItem);
                        }
                    }
                }
            }
        }// end of for
    }

    private void genFinItem(DepartOrder pickupOrder, List<UserLogin> users, java.sql.Timestamp sqlDate, Record tOrderItemRecord,
            Record contractFinItem) {
        DepartOrderFinItem pickupFinItem = new DepartOrderFinItem();
        pickupFinItem.set("fin_item_id", contractFinItem.getLong("fin_item_id"));
        pickupFinItem.set("amount", contractFinItem.getDouble("amount") * tOrderItemRecord.getDouble("amount"));
        pickupFinItem.set("depart_order_id", pickupOrder.getLong("id"));
        pickupFinItem.set("status", "未完成");
        pickupFinItem.set("creator", users.get(0).getLong("id"));
        pickupFinItem.set("create_date", sqlDate);
        pickupFinItem.save();
    }

//    // 产品入库
//    public void productInWarehouse(String departId) {
//        if (!"".equals(departId) && departId != null) {
//            String orderIds = "";
//            List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
//                    "select * from depart_transfer where pickup_id = ?", departId);
//            for (DepartTransferOrder departTransferOrder : departTransferOrders) {
//                orderIds += departTransferOrder.get("order_id") + ",";
//            }
//            orderIds = orderIds.substring(0, orderIds.length() - 1);
//            List<TransferOrder> transferOrders = TransferOrder.dao.find("select * from transfer_order where id in(" + orderIds + ")");
//            for (TransferOrder transferOrder : transferOrders) {
//                InventoryItem inventoryItem = null;
//                List<TransferOrderItem> transferOrderItems = TransferOrderItem.dao.find(
//                        "select * from transfer_order_item where order_id = ?", transferOrder.get("id"));
//                for (TransferOrderItem transferOrderItem : transferOrderItems) {
//                    if (transferOrderItem != null) {
//                        if (transferOrderItem.get("product_id") != null) {
//                            String inventoryItemSql = "select * from inventory_item where product_id = "
//                                    + transferOrderItem.get("product_id") + " and warehouse_id = " + transferOrder.get("warehouse_id");
//                            inventoryItem = InventoryItem.dao.findFirst(inventoryItemSql);
//                            String sqlTotal = "select count(1) total from transfer_order_item_detail where pickup_id = " + departId
//                                    + " and order_id = " + transferOrder.get("id");
//                            Record rec = Db.findFirst(sqlTotal);
//                            Long amount = rec.getLong("total");
//                            if(amount == 0){
//                            	amount = Math.round(transferOrderItem.getDouble("amount"));
//                            }
//                            if (inventoryItem == null) {
//                                inventoryItem = new InventoryItem();
//                                inventoryItem.set("party_id", transferOrder.get("customer_id"));
//                                inventoryItem.set("warehouse_id", transferOrder.get("warehouse_id"));
//                                inventoryItem.set("product_id", transferOrderItem.get("product_id"));
//                                inventoryItem.set("total_quantity", amount);
//                                inventoryItem.save();
//                            } else {
//                                inventoryItem.set("total_quantity", Double.parseDouble(inventoryItem.get("total_quantity").toString())
//                                        + amount);
//                                inventoryItem.update();
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    // 查出所有运输单的提货地点
    public void findAllAddress() {
        List<TransferOrder> transferOrders = new ArrayList<TransferOrder>();
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao
                .find("select dt.*,tor.pickup_seq  from depart_transfer dt left join transfer_order tor on tor.id = dt.order_id where pickup_id = ? order by tor.pickup_seq desc",
                        getPara("pickupOrderId"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            TransferOrder transferOrder = TransferOrder.dao
                    .findFirst(
                            "select tor.*,c.abbr cname,sum(f.amount) amount,f.rate from transfer_order tor left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id left join transfer_order_fin_item f on f.order_id = tor.id where tor.id = ?",
                            departTransferOrder.getLong("order_id"));
            transferOrders.add(transferOrder);
        }
        renderJson(transferOrders);
    }

    // 获取所有的路线
    public void findAllRoute(){
    	String orderIds = getPara("orderIds");
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from transfer_order tor where tor.id in("+orderIds+")";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

//        String sql = "select tor.*,c.abbr cname from transfer_order tor "
//        		+ " left join party p on tor.customer_id = p.id left join contact c on c.id = p.contact_id"
//        		+ " where tor.id in("+orderIds+") order by tor.pickup_seq desc " + sLimit;
        
        String sql = "select tor.*,c.abbr cname ,deo.is_direct_deliver,deo.pickup_type from transfer_order tor"
        		+ " left join party p on tor.customer_id = p.id left join contact c on c.id = p.contact_id "
        		+ " left join depart_transfer dept on dept.order_id = tor.id "
        		+ " left join depart_order deo on deo.id = dept.pickup_id"
        		+ " where tor.id in("+orderIds+") group by tor.id order by tor.pickup_seq desc " + sLimit;

        //logger.debug("sql:" + sql);
        List<Record> transferOrders = Db.find(sql);

        Map transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);

        renderJson(transferOrderListMap);    	
    }
    
    @Before(Tx.class)
    public void swapPickupSeq() {
        TransferOrder transferOrder = TransferOrder.dao.findById(getParaToLong("currentId"));
        transferOrder.set("pickup_seq", getPara("currentVal"));
        transferOrder.update();
        TransferOrder transferOrderPrev = TransferOrder.dao.findById(getParaToLong("targetId"));
        transferOrderPrev.set("pickup_seq", getPara("targetVal"));
        transferOrderPrev.update();
        renderJson("{\"success\":true}");
    }

    // 取消
    public void cancel() {
        String id = getPara();
        DepartOrder.dao.findById(id).set("Status", "取消").update();
        renderJson("{\"success\":true}");
    }

    // 筛选掉入库的运输单
    public void getTransferOrderDestinationBak() {
        String pickupOrderId = getPara("pickupOrderId");
        Map<String, String[]> map = getParaMap();
        String orderId = "";
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            if ("pickupOrderId".equals(entry.getKey())) {
                continue;
            }
            String[] values = entry.getValue();
            String value = values[0];

            if (value.indexOf("warehouse") >= 0) {
                String transOrderId = value.substring(9);
                List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                        "select * from depart_transfer where pickup_id = ?", pickupOrderId);
                for (DepartTransferOrder departTransferOrder : departTransferOrders) {
                    if (transOrderId.equals(departTransferOrder.getLong("order_id") + "")) {
                        // 去掉入库的单据
                        TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.getLong("order_id"));
                        orderId += transferOrder.getLong("id") + ",";
                        transferOrder.set("status", "已入库");
                        transferOrder.update();
                        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
                        transferOrderMilestone.set("status", "已入库");
                        String name = (String) currentUser.getPrincipal();
                        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
                        transferOrderMilestone.set("create_by", users.get(0).getLong("id"));
                        java.util.Date utilDate = new java.util.Date();
                        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
                        transferOrderMilestone.set("create_stamp", sqlDate);
                        transferOrderMilestone.set("order_id", transferOrder.getLong("id"));
                        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);

                        // 产品入库
                        productInWarehouseOnTransferOrderId(transOrderId);
                    }
                }
            }
        }
        renderJson("{\"success\":true}");
    }
     
  
    
    // 筛选掉入库的运输单
    public void getTransferOrderDestination() {
    	String warehouseIds = getPara("warehouseIds");
    	
    	if(warehouseIds != null && !"".equals(warehouseIds)){
    		String[] warehouseIdArr = warehouseIds.split(",");
			for (int i=0;i<warehouseIdArr.length;i++) {
				// 去掉入库的单据
				TransferOrder transferOrder = TransferOrder.dao.findById(warehouseIdArr[i]);
				transferOrder.set("status", "已入库");
				transferOrder.update();
				TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
				transferOrderMilestone.set("status", "已入库");
				String name = (String) currentUser.getPrincipal();
				List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
				transferOrderMilestone.set("create_by", users.get(0).getLong("id"));
				java.util.Date utilDate = new java.util.Date();
				java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
				transferOrderMilestone.set("create_stamp", sqlDate);
				transferOrderMilestone.set("order_id", transferOrder.getLong("id"));
				transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
				
				// 产品入库
				productInWarehouseOnTransferOrderId(warehouseIdArr[i]);
			}
	    }
    	renderJson("{\"success\":true}");
    }

    

    
    // 产品入库
    public void productInWarehouseOnTransferOrderId(String transOrderId) {
        if (!"".equals(transOrderId) && transOrderId != null) {
            TransferOrder transferOrder = TransferOrder.dao.findById(transOrderId);
            InventoryItem inventoryItem = null;
            List<TransferOrderItem> transferOrderItems = TransferOrderItem.dao.find("select * from transfer_order_item where order_id = ?",
                    transferOrder.getLong("id"));
            for (TransferOrderItem transferOrderItem : transferOrderItems) {
                if (transferOrderItem != null) {
                    if (transferOrderItem.getLong("product_id") != null) {
                        String inventoryItemSql = "select * from inventory_item where product_id = " + transferOrderItem.getLong("product_id")
                                + " and warehouse_id = " + transferOrder.getLong("warehouse_id");
                        inventoryItem = InventoryItem.dao.findFirst(inventoryItemSql);
                        String sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + transOrderId;
                        Record rec = Db.findFirst(sqlTotal);
                        Long amount = rec.getLong("total");
                        if (inventoryItem == null) {
                            inventoryItem = new InventoryItem();
                            inventoryItem.set("party_id", transferOrder.getLong("customer_id"));
                            inventoryItem.set("warehouse_id", transferOrder.getLong("warehouse_id"));
                            inventoryItem.set("product_id", transferOrderItem.getLong("product_id"));
                            inventoryItem.set("total_quantity", amount);
                            inventoryItem.save();
                        } else {
                            inventoryItem
                                    .set("total_quantity", Double.parseDouble(inventoryItem.getDouble("total_quantity").toString()) + amount);
                            inventoryItem.update();
                        }
                    }
                }
            }
        }
    }

    @Before(Tx.class)
    public void savegateIn(DepartTransferOrder departTransferOrder) {
        // product_id不为空时入库
        List<Record> transferOrderItem = Db.find("select * from transfer_order_item where order_id='" + departTransferOrder.getLong("order_id")
                + "'");
        TransferOrder tOrder = TransferOrder.dao.findById(departTransferOrder.getLong("order_id"));

        InventoryItem item = null;
        if (transferOrderItem.size() > 0) {
            for (int i = 0; i < transferOrderItem.size(); i++) {
                if (transferOrderItem.get(i).getLong("product_id") != null) {
                    item = new InventoryItem();
                    String in_item_check_sql = "select * from inventory_item where product_id="
                            + Integer.parseInt(transferOrderItem.get(i).getLong("product_id").toString()) + "" + " and warehouse_id="
                            + Integer.parseInt(tOrder.getLong("warehouse_id").toString()) + "";
                    InventoryItem inventoryItem = InventoryItem.dao.findFirst(in_item_check_sql);
                    if (inventoryItem == null) {
                        item.set("party_id", tOrder.getLong("customer_id"));
                        item.set("warehouse_id", tOrder.getLong("warehouse_id"));
                        item.set("product_id", transferOrderItem.get(i).getLong("product_id"));
                        item.set("total_quantity", transferOrderItem.get(i).getDouble("amount"));
                        item.save();
                    } else {
                        item = InventoryItem.dao.findById(inventoryItem.getLong("id"));
                        item.set(
                                "total_quantity",
                                Double.parseDouble(item.getDouble("total_quantity").toString())
                                        + Double.parseDouble(transferOrderItem.get(i).getDouble("amount").toString()));
                        item.update();
                    }
                }
            }
        }
    }

    // 添加额的外运输单
    @Before(Tx.class)
    public void addExternalTransferOrder() {
        String transferOrderId = getPara("transferOrderIds");
        String pickupOrderId = getPara("pickupOrderId");
        String[] transferOrderIds = transferOrderId.split(",");
        DepartTransferOrder departTransferOrder = null;
        for (int i = 0; i < transferOrderIds.length; i++) {
            departTransferOrder = new DepartTransferOrder();
            departTransferOrder.set("pickup_id", pickupOrderId);
            departTransferOrder.set("order_id", transferOrderIds[i]);
            TransferOrder transferOrder = TransferOrder.dao.findById(transferOrderIds[i]);
            departTransferOrder.set("transfer_order_no", transferOrder.getStr("order_no"));
            departTransferOrder.save();
        }
        renderJson("{\"success\":true}");
    }

    // 点击货品table的查看 ，显示货品对应的单品
    public void findAllItemDetail() {
        String itemId = getPara("item_id");
        String pickupId = getPara("pickupId");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String category = getPara("category");
        String sqlTotal = "select count(1) total from transfer_order_item_detail tod where tod.item_id = " + itemId
                + " and tod.pickup_id = " + pickupId;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from transfer_order_item_detail tod where tod.item_id = " + itemId + " and tod.pickup_id = " + pickupId;

        List<Record> details = Db.find(sql);

        Map productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        productListMap.put("aaData", details);
        renderJson(productListMap);
    }

    // 应付list
    public void accountPayable() {
        String id = getPara("pickupOrderId");
        if (id == null || id.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }
        
        DepartOrder dp = DepartOrder.dao.findById(id);
        /*if(dp.get("is_direct_deliver")){
        	String sql1 = "SELECT count(1) total FROM transfer_order_fin_item tor "
        			+ " LEFT JOIN depart_transfer dt on tor.order_id = dt.order_id "
        			+ " where dt.pickup_id = "+ id
        			+ " and tor.fin_item_id in (1,2,3,18,32,33,34,35)";
        	String sql = "SELECT tor.* FROM transfer_order_fin_item tor "
        			+ " LEFT JOIN depart_transfer dt on tor.order_id = dt.order_id "
        			+ " where dt.pickup_id = "+ id
        			+ " and tor.fin_item_id in (1,2,3,18,32,33,34,35)";
        	Record rc1 = Db.findFirst(sql1);
        	Record rc = Db.findFirst(sql);
        	//System.out.println(rc.hashCode());
        	if(rc1.getLong("total")>0){
        		PickupOrderFinItem finItem = new PickupOrderFinItem();
                finItem.set("status", "新建");
                finItem.set("pickup_order_id", id);
                finItem.set("fin_item_id", rc.getLong("fin_item_id"));
                finItem.set("amount",  rc.getDouble("amount"));
                finItem.set("create_date",  rc.getDate("create_date"));
                finItem.set("remark",  rc.getStr("remark"));
                finItem.save();
        	}else{
        		System.out.println("空");
        	}
        }*/
        /*PickupOrderFinItem finItem = new PickupOrderFinItem();
        finItem.set("status", "新建").set("pickup_order_id", pickupOrderId).set("fin_item_id", item.get("id"));
        finItem.save();*/
        
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from pickup_order_fin_item d "
                + "left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id =" + id + " and f.type = '应付'";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select d.*,f.name from pickup_order_fin_item d "
                + "left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id =" + id + " and f.type = '应付'");

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        // 没有名字就删掉？不能删，要让人知道这里有错(不是，只是删除临时添加的条目而已，不删除添加的记录)

        // TODO: 8-19 这里为什么要删 “应收应付条目定义”中的数据，基础数据 不能在业务单据中的操作进行删除。
        // List<Record> list = Db.find("select * from fin_item");
        // for (int i = 0; i < list.size(); i++) {
        // if (list.get(i).get("name") == null) {
        // Fin_item.dao.deleteById(list.get(i).get("id"));
        // }
        // }
        renderJson(orderMap);
    }
    
    // 自营车辆应付list
    public void ownCarAccountPayable() {
    	String id = getPara("pickupOrderId");
    	String sLimit = "";
    	String pageIndex = getPara("sEcho");
    	if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    		sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    	}    	
    	// 获取总条数
    	String totalWhere = "";
    	String sql = "select count(1) total from fin_item f where f.type = '自营应付' and f.driver_type = '"+Carinfo.CARINFO_TYPE_OWN+"'";
    	Record rec = Db.findFirst(sql + totalWhere);
    	logger.debug("total records:" + rec.getLong("total"));
    	
    	// 获取当前页的数据
    	List<Record> orders = Db.find("select f.* from fin_item f where f.type = '自营应付' and f.driver_type = '"+Carinfo.CARINFO_TYPE_OWN+"'");
    	
    	Map orderMap = new HashMap();
    	orderMap.put("sEcho", pageIndex);
    	orderMap.put("iTotalRecords", rec.getLong("total"));
    	orderMap.put("iTotalDisplayRecords", rec.getLong("total"));    	
    	orderMap.put("aaData", orders);
    	renderJson(orderMap);
    }

    // 添加应付
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_ADD_COST})
    @Before(Tx.class)
    public void addNewRow() {
    	List<FinItem> items = new ArrayList<FinItem>();
        String pickupOrderId = getPara();
        FinItem item = FinItem.dao.findFirst("select * from fin_item where type = '应付' order by id asc");
        if(item != null){
	        PickupOrderFinItem finItem = new PickupOrderFinItem();
	        finItem.set("status", "新建").set("pickup_order_id", pickupOrderId).set("fin_item_id", item.getLong("id"));
	        finItem.save();
        }
        items.add(item);
        renderJson(items);
    }
    
    // 添加应收
    public void addIncomeRow() {
    	List<FinItem> items = new ArrayList<FinItem>();
    	String pickupOrderId = getPara();
    	FinItem item = FinItem.dao.findFirst("select * from fin_item where type = '应收' order by id asc");
    	if(item != null){
    		//DepartOrderFinItem dFinItem = new DepartOrderFinItem();
    		PickupOrderFinItem pFinItem = new PickupOrderFinItem();
    		pFinItem.set("status", "新建").set("pickup_order_id", pickupOrderId).set("fin_item_id", item.getLong("id"));
    		pFinItem.save();
    		//找出新建应收的ID
    		List<Record> departOrderFinItemids = Db.find("select * from pickup_order_fin_item order by id desc limit 0,1");
    		//此处按拼车单中运输单数量添加tranfer_order_fin_item表中数据
    		List<Record> list = Db.find("select order_id from depart_transfer where pickup_id = ?",pickupOrderId);
    		int num = list.size();
    		if(num == 0){
    			num=1;
    		}
    		for (int i = 0; i < num; i++) {
    			TransferOrderFinItem transferOrderFinTtem = new TransferOrderFinItem();
        		transferOrderFinTtem.set("order_id", list.get(i).getLong("order_id"));
        		transferOrderFinTtem.set("fin_item_id", item.getLong("id"));
        		transferOrderFinTtem.set("depart_order_fin_item_id", departOrderFinItemids.get(0).getLong("id"));
        		transferOrderFinTtem.set("status", "未完成");
        		transferOrderFinTtem.set("amount", 0);
        		transferOrderFinTtem.set("rate", 1.00/num);
        		transferOrderFinTtem.save();
			}
    	}
    	items.add(item);
    	renderJson(items);
    }

    // 添加应付
    public void paymentSave() {
        String returnValue = "";
        String id = getPara("id");
        String finItemId = getPara("finItemId");
        DepartOrderFinItem dFinItem = DepartOrderFinItem.dao.findById(id);

        FinItem fItem = FinItem.dao.findById(dFinItem.getLong("fin_item_id"));

        String amount = getPara("amount");

        String username = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + username + "'");
        Date createDate = Calendar.getInstance().getTime();

        if (!"".equals(finItemId) && finItemId != null) {
            dFinItem.set("fin_item_id", finItemId).update();
            returnValue = finItemId;
        } else if (!"".equals(amount) && amount != null) {
            dFinItem.set("amount", amount).update();
            returnValue = amount;
        }

       /* List<Record> list = Db.find("select * from fin_item");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get("name") == null) {
                Fin_item.dao.deleteById(list.get(i).get("id"));
                List<Record> list2 = Db.find("select * from depart_order_fin_item where fin_item_id ='" + list.get(i).get("id") + "'");
                List<Record> list3 = Db.find("select * from fin_item where id ='" + list2.get(0).get("fin_item_id") + "'");
                if (list3.size() == 0) {
                    TransferOrderFinItem.dao.deleteById(list2.get(0).get("id"));
                }
            }
        }*/
        renderText(returnValue);
    }

    public void getPaymentList() {
        // String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        locationList = Db.find("select * from fin_item where type='应付'");
        renderJson(locationList);
    }

    // 费用删除
    public void finItemdel() {
        String id = getPara();
        PickupOrderFinItem.dao.deleteById(id);
        renderJson("{\"success\":true}");
    }
    
    //删除应收
    public void delReceivable() {
        String orderId = getPara();
        TransferOrderFinItem transferOrderFinItem = TransferOrderFinItem.dao.findFirst("select * from transfer_order_fin_item where order_id = ?", orderId);
        transferOrderFinItem.delete();
        /*//同时删除transfer_order_fin_item表中对应的费用信息
        List<TransferOrderFinItem> transferOrderFinItems = TransferOrderFinItem.dao.
        		find("select * from transfer_order_fin_item where depart_order_fin_item_id = ?",id);
        for (TransferOrderFinItem transferOrderFinItem : transferOrderFinItems) {
			transferOrderFinItem.delete();
		}*/
        renderJson("{\"success\":true}");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_TO_CREATE})
    public void pickupOrderPaymentList(){
    	String pickupOrderId = getPara("pickupOrderId");
    	if(pickupOrderId == null || "".equals(pickupOrderId)){
    		pickupOrderId = "-1";
    	}
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(distinct tor.id) total from pickup_order_fin_item dofi"
						+ " left join depart_order dor on dofi.pickup_order_id = dor.id"
						+ " left join depart_transfer dt on dt.pickup_id = dor.id"
						+ " left join transfer_order tor on tor.id = dt.order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join fin_item fi on fi.id = dofi.fin_item_id"
						+ " where dor.combine_type='"+DepartOrder.COMBINE_TYPE_PICKUP+"' and dor.id ="+pickupOrderId+" and fi.type = '应收' and fi.name = '分摊费用'";
        String sql = "select distinct tor.order_no transferno,c.abbr cname,dofi.amount amount,tor.create_stamp from pickup_order_fin_item dofi"
						+ " left join depart_order dor on dofi.pickup_order_id = dor.id"
						+ " left join depart_transfer dt on dt.pickup_id = dor.id"
						+ " left join transfer_order tor on tor.id = dt.order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join fin_item fi on fi.id = dofi.fin_item_id"
						+ " where dor.combine_type='"+DepartOrder.COMBINE_TYPE_PICKUP+"' and dor.id ="+pickupOrderId+" and fi.type = '应收' and fi.name = '分摊费用' order by tor.create_stamp desc" + sLimit;
        
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> transferOrders = Db.find(sql);

        Map transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);
        renderJson(transferOrderListMap);
    }
    
    public void wentDutch(){
    	String pickupOrderId = getPara("pickupOrderId");
    	String customerCount = "select count(c.abbr) customerCount from depart_order dor"
								+ " left join depart_transfer dt on dt.pickup_id = dor.id"
								+ " left join transfer_order tor on tor.id = dt.order_id"
								+ " left join party p on p.id = tor.customer_id"
								+ " left join contact c on c.id = p.contact_id"
								+ " where dor.combine_type='"+DepartOrder.COMBINE_TYPE_PICKUP+"' and dor.id = " + pickupOrderId;
    	String amountSql = "select sum(amount) amount from ("
								+ " select distinct dofi.id id,dofi.amount amount from pickup_order_fin_item dofi"
								+ " left join depart_order dor on dofi.pickup_order_id = dor.id"
								+ " left join depart_transfer dt on dt.pickup_id = dor.id"
								+ " left join transfer_order tor on tor.id = dt.order_id"
								+ " left join party p on p.id = tor.customer_id"
								+ " left join contact c on c.id = p.contact_id"
								+ " left join fin_item fi on fi.id = dofi.fin_item_id"
								+ " where dor.combine_type='"+DepartOrder.COMBINE_TYPE_PICKUP+"' and dor.id = "+pickupOrderId+" and fi.type = '应付') dofi_item";
        Record rec = Db.findFirst(customerCount);
        Long customer = rec.getLong("customerCount");
        rec = Db.findFirst(amountSql);
        Double amount = rec.getDouble("amount");
        Double avg = amount / customer;
        FinItem finItem = FinItem.dao.findFirst("select * from fin_item where name = '分摊费用' and type = '应收'");
        for(int i=0;i<customer;i++){
        	PickupOrderFinItem departOrderFinItem = new PickupOrderFinItem();
        	departOrderFinItem.set("pickup_order_id", pickupOrderId);
        	departOrderFinItem.set("fin_item_id", finItem.getLong("id"));
        	departOrderFinItem.set("amount", avg);
        	departOrderFinItem.set("create_date", new Date());
        	departOrderFinItem.set("cost_source", "分摊费用");
        	departOrderFinItem.save();
        }
        renderJson("{\"success\":true}");
    }
    
    // 付费条目
    public void searchAllPayItem(){
    	List<FinItem> items = FinItem.dao.find("select * from fin_item where type = '应付'");
    	renderJson(items);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_PO_ADD_COST})
    @Before(Tx.class)
    public void updatePickupOrderFinItem(){
    	// 由于应收中只存在一条总金额,所以简化处理
    	String paymentId = getPara("paymentId");
    	String name = getPara("name");
    	String value = getPara("value");
    	PickupOrderFinItem pickupOrderFinItem = PickupOrderFinItem.dao.findById(paymentId);
    	if("amount".equals(name) && "".equals(value)){
    		value = "0";
    	}
    	if(pickupOrderFinItem != null){
    		pickupOrderFinItem.set(name, value);
    		pickupOrderFinItem.update();
    	}
    	/*if(paymentId != null && !"".equals(paymentId)){
    		PickupOrderFinItem departOrderFinItem = PickupOrderFinItem.dao.findById(paymentId);
    		departOrderFinItem.set(name, value);
    		departOrderFinItem.update();
    		//TransferOrderFinItem transferOrderFinItem = 
    		if("fin_item_id".equals(name)){
    			List<TransferOrderFinItem> transferOrderFinTtems = TransferOrderFinItem.dao
    	                .find("select * from transfer_order_fin_item where depart_order_fin_item_id = ?",paymentId);
    			for (TransferOrderFinItem transferOrderFinItem : transferOrderFinTtems) {
    				transferOrderFinItem.set(name, value);
    				transferOrderFinItem.update();
    			}
    		}
    		if("amount".equals(name)){
    			List<TransferOrderFinItem> transferOrderFinTtems = TransferOrderFinItem.dao
    	                .find("select * from transfer_order_fin_item where depart_order_fin_item_id = ?",paymentId);
    			double num = transferOrderFinTtems.size();
        		if(num == 0){
        			num=1;
        		}
        		double result = Double.parseDouble(value)*(1/num);
    			for (TransferOrderFinItem transferOrderFinItem : transferOrderFinTtems) {
    				transferOrderFinItem.set(name, result);
    				transferOrderFinItem.update();
    			}
    		}
    	}*/
    	
        renderJson("{\"success\":true}");
    }
    
    // 保存拼车单自营车辆应付
    public void saveOwnCarFinItem(){
    	DepartOrderFinItem departOrderFinItem = DepartOrderFinItem.dao.findFirst("select * from depart_order_fin_item where pickup_order_id = ? and fin_item_id = ?", getPara("pickupOrderId"), getPara("finItemId"));
    	if(departOrderFinItem != null){
    		departOrderFinItem.set("amount", getPara("amount"));
	    	departOrderFinItem.set("last_update_date", new Date());
	    	departOrderFinItem.update();
    	}else{
	    	departOrderFinItem = new DepartOrderFinItem();    	
	    	departOrderFinItem.set("pickup_order_id", getPara("pickupOrderId"));
	    	departOrderFinItem.set("fin_item_id", getPara("finItemId"));
	    	departOrderFinItem.set("amount", getPara("amount"));
	    	departOrderFinItem.set("create_date", new Date());
	        String name = (String) currentUser.getPrincipal();
	    	List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
	    	departOrderFinItem.set("creator", users.get(0).getLong("id"));
	    	departOrderFinItem.set("status", "new");
	    	departOrderFinItem.save();
    	}
    	renderJson(departOrderFinItem);
    }
    
    // 查找拼车单自营车辆应付
    public void searchOwnCarFinItem(){
    	DepartOrderFinItem departOrderFinItem = DepartOrderFinItem.dao.findFirst("select * from depart_order_fin_item where pickup_order_id = ? and fin_item_id = ?", getPara("pickupOrderId"), getPara("finItemId"));    	
    	renderJson(departOrderFinItem);
    }
    
    // 删除拼车单自营车辆应付
    public void deletePickupOrderFinItem(){
    	DepartOrderFinItem departOrderFinItem = DepartOrderFinItem.dao.findFirst("select * from depart_order_fin_item where pickup_order_id = ? and fin_item_id = ?", getPara("pickupOrderId"), getPara("finItemId"));    	
    	departOrderFinItem.delete();
        renderJson("{\"success\":true}");
    }   

    // 应收
    public void incomePayable(){
    	Map orderMap = new HashMap();
    	String pickupOrderId = getPara("pickupOrderId");
    	if(pickupOrderId != null && !"".equals(pickupOrderId)){	    		
    		String sLimit = "";
    		String pageIndex = getPara("sEcho");
    		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    		}
    		String totalWhere = "";
            String sql = "select count(tor.id) total from depart_order dor "
				+ " left join depart_transfer dt on dt.pickup_id = dor.id"
				+ " left join transfer_order tor on dt.order_id = tor.id"
				+ " left join party p on p.id = tor.customer_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " left join transfer_order_fin_item tofi on tofi.order_id = tor.id"
				+ " left join fin_item fi on fi.id  = tofi.fin_item_id"
				+ " where dor.combine_type = '"+DepartOrder.COMBINE_TYPE_PICKUP+"' and dor.id = "+pickupOrderId;
            Record rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db.find("select tor.id order_id,tor.order_no order_no,c.company_name cname,fi.name name,tofi.id id,tofi.amount amount,tofi.remark,tofi.status status from depart_order dor "
				+ " left join depart_transfer dt on dt.pickup_id = dor.id"
				+ " left join transfer_order tor on dt.order_id = tor.id"
				+ " left join party p on p.id = tor.customer_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " left join transfer_order_fin_item tofi on tofi.order_id = tor.id"
				+ " left join fin_item fi on fi.id  = tofi.fin_item_id"
				+ " where dor.combine_type = '"+DepartOrder.COMBINE_TYPE_PICKUP+"' and dor.id = "+pickupOrderId);
    		
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
    		orderMap.put("aaData", orders);
    	}
    	renderJson(orderMap);
    }
    //修改分摊比例
    public void updateTransferOrderFinItem(){
    	String id = getPara("id");
    	double rate = Double.parseDouble(getPara("value"));
    	List<TransferOrderFinItem> transferOrderFinItems = TransferOrderFinItem.dao.
         	find("select * from transfer_order_fin_item where order_id = ?",id);
        for (TransferOrderFinItem transferOrderFinItem : transferOrderFinItems) {
         	DepartOrderFinItem departOrderFinItem = DepartOrderFinItem.dao.findById(transferOrderFinItem.getLong("depart_order_fin_item_id"));
         	transferOrderFinItem.set("rate", rate / 100);
         	transferOrderFinItem.set("amount", rate / 100 * departOrderFinItem.getDouble("amount"));
 			transferOrderFinItem.update();
 		} 
        renderJson("{\"success\":true}");
    }
    /*public void updateTransferOrderFinItem(){
    	String ids = getPara("id");
    	String values = getPara("value");
	    String id[] = ids.split(",");
	    String value[] = values.split(",");
	    for (int i = 0; i < id.length; i++) {
	    	double rate = Double.parseDouble(value[i]);
	    	 List<TransferOrderFinItem> transferOrderFinItems = TransferOrderFinItem.dao.
	         		find("select * from transfer_order_fin_item where order_id = ?",id[i]);
	         for (TransferOrderFinItem transferOrderFinItem : transferOrderFinItems) {
	         	DepartOrderFinItem departOrderFinItem = DepartOrderFinItem.dao.findById(transferOrderFinItem.get("depart_order_fin_item_id"));
	         	transferOrderFinItem.set("rate", rate / 100);
	         	transferOrderFinItem.set("amount", rate / 100 * departOrderFinItem.getDouble("amount"));
	 			transferOrderFinItem.update();
	 		} 
		}
        renderJson("{\"success\":true}");
    }*/
    //查找货品
    public void findTransferOrderItem(){
    	Map orderMap = new HashMap();
    	String transferOrderId = getPara("order_id");
    	String pickup_type = getPara("pickup_type");
    	String pickup_numer = "";
    	if("twice_pickup".equals(pickup_type)){
    		pickup_type = " and twice_pickup_id is not null";
    		pickup_numer = "toi.twice_pickup_number pickup_number";
    		
    	}else{
    		pickup_type = " and twice_pickup_id is null";
    		pickup_numer = "(ifnull(toi.pickup_number,0)+ifnull(toi.twice_pickup_number,0)) pickup_number";
    	}
    	
    	if (transferOrderId == null || transferOrderId.equals("")) {
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
        }else{
    		String sLimit = "";
    		String pageIndex = getPara("sEcho");
    		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    		}
    		String totalWhere = "select distinct count(0) total from transfer_order_item toi"
            		+ " left join transfer_order tor on tor.id = toi.order_id"
            		+ " left join product p on p.id = toi.product_id"
            		+ " where toi.order_id = " + transferOrderId;
            
            String sql = "select distinct toi.id, toi.product_id prod_id, tor.order_no, ifnull(p.item_no, toi.item_no) item_no, ifnull(p.item_name, toi.item_name) item_name,"
            		+ " (select count(0) total from transfer_order_item_detail where item_id = toi.id  and pickup_id is null "+ pickup_type +") atmamount,"+pickup_numer+",toi.amount,"
    				+ " ifnull(p.unit, toi.unit) unit, round(toi.volume ,2) sum_volume, round(toi.sum_weight ,2) sum_weight, toi.remark from transfer_order_item toi"
            		+ " left join transfer_order tor on tor.id = toi.order_id"
            		+ " left join product p on p.id = toi.product_id"
            		+ " where toi.order_id = " +transferOrderId + " order by toi.id";
            
            Record rec = Db.findFirst(totalWhere);
            logger.debug("total records:" + rec.getLong("total"));
    		
            List<Record> orders = Db.find(sql);
            
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
    		orderMap.put("aaData", orders);
    	}
    	renderJson(orderMap);
    }
    
    //查找单品
    public void findTransferOrderItemDetail(){
    	Map orderMap = new HashMap();
    	String pickup_type = getPara("pickup_type");
    	String transferOrderItemId = getPara("item_id");
    	if("twice_pickup".equals(pickup_type)){
    		pickup_type = " and twice_pickup_id is not null";
    	}else{
    		pickup_type = " and twice_pickup_id is null";
    	}
    	
    	
    	if (transferOrderItemId == null || transferOrderItemId.equals("")) {
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
        }else{
    		String sLimit = "";
    		String pageIndex = getPara("sEcho");
    		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    		}
    		String totalWhere = "select count(0) total from transfer_order_item_detail where item_id = '" + transferOrderItemId + "'  and pickup_id is null" +pickup_type;
            String sql = "select id,serial_no,pieces from transfer_order_item_detail where item_id = '" + transferOrderItemId + "'  and pickup_id is null" +pickup_type;
            
            Record rec = Db.findFirst(totalWhere);
            logger.debug("total records:" + rec.getLong("total"));
    		
            List<Record> orders = Db.find(sql);
            
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
    		orderMap.put("aaData", orders);
    	}
    	renderJson(orderMap);
    }
    //按运输单查找所有单品序列号
    public void findSerialNoByOrderId(){
    	String orderId = getPara("order_id");
    	String pickup_type = getPara("pickup_type");
    	Record serialNoList = null;
    	if("twice_pickup".equals(pickup_type)){
    		pickup_type = " and twice_pickup_id is not null";
    	}else{
    		pickup_type = " and twice_pickup_id is null";
    	}
    	if(orderId != ""){
    		String sql = "select group_concat(cast(id as char) separator ',') id,group_concat(serial_no separator ' ') serial_no from transfer_order_item_detail where pickup_id is null "+pickup_type+" and order_id = '" + orderId + "';";
    		serialNoList = Db.findFirst(sql);
    		logger.debug("serialNoList:" + serialNoList);
    	}
    	renderJson(serialNoList);
    }
    //按运输单查找所有普货货品数量
    public void findNumberByOrderId(){
    	String orderId = getPara("order_id");
    	String twice = getPara("twice");
    	String totalAmount = " group_concat( cast(toi.amount-ifnull(toi.pickup_number,0)-ifnull(toi.have_twice_pickup,0) AS CHAR) SEPARATOR ',' ) total_amounts";
    	if("二次提货".equals(twice)){
    		totalAmount = " group_concat( cast(ifnull(toi.twice_pickup_number,0)-ifnull(toi.pickup_number,0) AS CHAR) SEPARATOR ',' ) total_amounts";
    	}
    	
    	Record serialNoList = null;
    	if(orderId != ""){
    		String sql = "SELECT group_concat( cast(toi.id AS CHAR) SEPARATOR ',' ) ids, "
    				+ " ( SELECT group_concat( cast(toid.id AS CHAR) SEPARATOR ',' ) FROM transfer_order_item_detail toid "
    				+ " WHERE toid.order_id = toi.order_id  and toid.pickup_id is null ) detail_ids, "
    				+ " ( SELECT group_concat( toid.serial_no SEPARATOR ' ' ) FROM transfer_order_item_detail toid "
    				+ " WHERE toid.order_id = toi.order_id  and toid.pickup_id is null ) serial_nos, "
    				+ " group_concat( cast(toi.amount AS CHAR) SEPARATOR ',' ) amounts,"
    				+ totalAmount+","
    				+ " group_concat( cast( ifnull(toi.pickup_number, 0) AS CHAR "
    				+ " ) SEPARATOR ',' ) pickup_numbers FROM transfer_order_item toi "
    				+ " where order_id = '" + orderId + "';";
    		serialNoList = Db.findFirst(sql);
    		logger.debug("serialNoList:" + serialNoList);
    	}
    	renderJson(serialNoList);
    }
    //计算单品体积、重量
    @Before(Tx.class)
    public void detailItemsCalculate(){
    	String detailIds = getPara("detailIds");
    	logger.debug("detailIds:" + detailIds);
    	Record serialNoList = null;
    	if(detailIds != ""){
    		String sql = "select round(sum(ifnull(volume,0)),2) volume,round(sum(ifnull(weight,0)),2) weight from transfer_order_item_detail where id in(" + detailIds.toString() + ");";
    		serialNoList = Db.findFirst(sql);
    		logger.debug("serialNoList:" + serialNoList);
    	}
    	renderJson(serialNoList);
    }
    //计算货品体积、重量
    public void productItemsCalculate(){
    	String item_ids[] = getPara("item_id").split(",");
    	String itemNumbers[] = getPara("itemNumbers").split(",");
    	List<Double> itemList = new ArrayList<Double>();
    	double sumWeight = 0;
    	double sumVolume = 0;
    	for (int j = 0; j < item_ids.length; j++) {
			TransferOrderItem item = TransferOrderItem.dao.findById(item_ids[j]);
			if(item.getDouble("sum_weight") != null && !"".equals(item.getDouble("sum_weight")) && !"".equals(itemNumbers[j]) && item.getDouble("amount") != null && !"".equals(item.getDouble("amount"))){
				sumWeight += item.getDouble("sum_weight") * (Double.parseDouble(itemNumbers[j]) / item.getDouble("amount"));
			}
			if(item.getDouble("volume") != null && !"".equals(item.getDouble("volume")) && !"".equals(itemNumbers[j]) && item.getDouble("amount") != null && !"".equals(item.getDouble("amount"))){
				sumVolume += item.getDouble("volume") * (Double.parseDouble(itemNumbers[j]) / item.getDouble("amount"));
			}
		}
    	renderJson("{\"sumWeight\":"+sumWeight+",\"sumVolume\":"+sumVolume+"}");
    }
    
    //TODO:查询所有跟车人员
    public void findDriverAssistant() {
		String input = getPara("input");
		List<DriverAssistant> driverAssistantList = Collections.EMPTY_LIST;
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();
		if (input.trim().length() > 0) {
			driverAssistantList = DriverAssistant.dao.find("select pda.id,pda.name,pda.phone from driver_assistant pda left join office o on pda.office_id = o.id where (pda.is_stop is null or pda.is_stop = 0) and pda.name like '%" + input + "%'  and (o.id = " + parentID + " or o.belong_office = " + parentID + ")");
		} else {
			driverAssistantList = DriverAssistant.dao.find("select pda.id,pda.name,pda.phone from driver_assistant pda left join office o on pda.office_id = o.id where (pda.is_stop is null or pda.is_stop = 0)  and (o.id = " + parentID + " or o.belong_office = " + parentID + ")");
		}
		renderJson(driverAssistantList);
	}
    
    //查询调车单跟车人员
    public void findPickupDriverAssistant() {
    	String pickId = getPara("pickupId");
    	List<PickupDriverAssistant> assistantList = new ArrayList<PickupDriverAssistant>();
    	if(pickId != null && !"".equals(pickId))
    			assistantList = PickupDriverAssistant.dao.find("select pda.id,pda.name,pda.phone from pickup_driver_assistant pda where pda.pickup_id = ?",pickId);
		renderJson(assistantList);
	}
    
    //查询调车单跟车人员
    @Before(Tx.class)
    public void deletePickupDriverAssistant() {
    	String id = getPara("id");
    	if(!"".equals(id) && id != null)
    		PickupDriverAssistant.dao.deleteById(id);
    	renderJson("{\"success\":true}");
	}
    /**
     * 当订单类型是调拨单或者退货单从中转仓出货时，减少库存
     */
    public void  SubtractInventory(DepartOrder pickupOrder, TransferOrder transferOrder){
    	boolean is_subtract = false;
    	if("arrangementOrder".equals(transferOrder.getStr("order_type"))){
    		is_subtract = true;
    	}else{
    		if("deliveryToFachtoryFromWarehouse".equals(transferOrder.getStr("arrival_mode"))){
    			is_subtract = true;
    		}
    	}
		if(is_subtract){
			 List<TransferOrderItem> list =  TransferOrderItem.dao.find("select * from transfer_order_item where order_id = ? ",transferOrder.getLong("id"));
			for (TransferOrderItem transferOrderItem : list) {
				if(transferOrderItem.getLong("product_id") != null && transferOrderItem.getLong("product_id") != 0 ){
					InventoryItem ii = InventoryItem.dao.findFirst("select * from inventory_item where party_id =? and warehouse_id = ? and product_id = ?",transferOrder.getLong("customer_id"),transferOrder.getLong("from_warehouse_id"),transferOrderItem.getLong("product_id"));
					TransferOrderItemDetail toid = TransferOrderItemDetail.dao.findFirst("select count(*) as amount from transfer_order_item_detail where item_id = ? and pickup_id = ? ",transferOrderItem.getLong("id"),pickupOrder.getLong("id"));
					Double total_quantity = ii.getDouble("total_quantity") ;
					if(total_quantity - toid.getLong("amount") >= 0 ){
							ii.set("total_quantity", total_quantity - toid.getLong("amount"));
			    				ii.update();
			    	}
				}
			}
		}
    }
    
    @Before(Tx.class)
    public void update_pickupMode(){
    	boolean result = false;
    	String error_msg = "";
    	String pickupId = getPara("pickupId");
    	String pickupMode = getPara("pickupMode");
    	String sp_id = getPara("sp_id");
    	String driver_id = getPara("driver_id");
    	
    	String car_no = getPara("car_no");
    	String driver = getPara("driver");
    	String phone = getPara("phone");
    	
    	DepartOrder dp = DepartOrder.dao.findById(pickupId);
    	if(dp != null){
    		String car_summary_type = dp.getStr("car_summary_type");
    		String audit_status = dp.getStr("audit_status");
    		if(!"untreated".equals(car_summary_type)){
    			error_msg = "已做行车单，无法更改了哦";
    		} else if(!"新建".equals(audit_status)){
    			error_msg = "已做财务单据，无法更改了哦";
    		} else {
    			dp.set("pickup_mode", pickupMode);
    			
    			if(StrKit.notBlank(driver_id)){
    				dp.set("carinfo_id", driver_id);
    			}else{
    				dp.set("carinfo_id", null);
    			}
    			dp.set("car_no", pickupMode);
    			dp.set("driver", pickupMode);
    			dp.set("phone", pickupMode);
    			
    			if(StrKit.notBlank(sp_id)){
    				dp.set("sp_id", sp_id);
    			}
    			dp.update();	
    			result = true;
    		}
    	}
    	
    	Record order = new Record();
    	order.set("result", result);
    	order.set("error_msg", error_msg);
    	renderJson(order);
    }
    
}
