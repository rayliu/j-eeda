package controllers.yh.pickup;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartOrderFinItem;
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
import models.yh.contract.Contract;
import models.yh.pickup.PickupDriverAssistant;
import models.yh.profile.Carinfo;
import models.yh.profile.Contact;
import models.yh.profile.DriverAssistant;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.departOrder.DepartOrderController;
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
    public void createPickupOrder() {
        String list = this.getPara("localArr");
        setAttr("localArr", list);
        String[] transferOrderIds = list.split(",");
        String detailIds = getPara("detailIds");
        String cargoIds = getPara("cargoIds");
        String cargoNumbers = getPara("cargoNumbers");
        String cargoItemIds = getPara("cargoItemIds");
        String flag = getPara("flag");

        if (transferOrderIds.length == 1) {
            TransferOrder transferOrderAttr = TransferOrder.dao.findById(transferOrderIds[0]);
            setAttr("transferOrderAttr", transferOrderAttr);
            Long spId = transferOrderAttr.get("sp_id");
            if (spId != null && !"".equals(spId)) {
                Party spParty = Party.dao.findById(spId);
                setAttr("spParty", spParty);
                Contact spContact = Contact.dao.findById(spParty.get("contact_id"));
                setAttr("spContact", spContact);
            }
        }

        logger.debug("localArr" + list);
        setAttr("saveOK", false);
        String[] orderIds = list.split(",");
        for (int i = 0; i < orderIds.length; i++) {
            TransferOrder transferOrder = TransferOrder.dao.findById(orderIds[i]);
            transferOrder.set("pickup_seq", i + 1);
            transferOrder.update();
        }
        String sql = "SELECT tra.receiving_address FROM transfer_order tra "
                + " where tra.id in("+list+")";
        Record tro = Db.findFirst(sql);
        setAttr("transferOrder",tro);
        setAttr("t_id", list);
        
        TransferOrder transferOrder = new TransferOrder();
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);

        setAttr("status", "新建");
        setAttr("saveOK", false);
        setAttr("detailIds", detailIds);
        setAttr("cargoIds", cargoIds);
        setAttr("cargoNumbers", cargoNumbers);
        setAttr("cargoItemIds", cargoItemIds);
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
        String orderNo = getPara("orderNo");
        String departNo = getPara("departNo");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String take = getPara("take");
        String status = getPara("status");
        String office = getPara("office");
        String customerId = getPara("customer_filter");
        String sp_filter = getPara("sp_filter");
        String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sql = "";
        String sqlTotal = "";
        if ((orderNo == null || "".equals(orderNo)) && (departNo == null || "".equals(departNo)) && (beginTime == null || "".equals(beginTime))
        		&& (endTime == null || "".equals(endTime)) &&  (take == null || "".equals(take)) && 
				 (status == null || "".equals(status)) && (office == null || "".equals(office)) && (sp_filter == null || "".equals(sp_filter))&&  customerId == null ) {
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
            		+ " c2.abbr sp_name "
            		+ " from depart_order dor "
                    + " left join carinfo c on dor.carinfo_id = c.id "
                    + " left join party p on dor.driver_id = p.id "
                    + " left join contact ct on p.contact_id = ct.id "
                    + " left join user_login u on u.id = dor.create_by "
                    + " left join depart_transfer dtf on dtf.pickup_id = dor.id "
                    + " left join transfer_order t_o on t_o.id = dtf.order_id "
                    + " left join office o on o.id = t_o.office_id "
                    + " LEFT JOIN party p2 on p2.id = dor.sp_id "
                    + " LEFT JOIN contact c2 on c2.id = p2.id "
                    + " where dor.status!='取消' and combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "' "
            		+ " and dor.status!='手动删除' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and t_o.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
                    + " group by dor.id ";
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
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
                    + " LEFT JOIN contact c2 on c2.id = p2.id "
                    + " where dor.status!='取消' and combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "' "
            		+ " and ifnull(depart_no,'') like '%"+ departNo + "%' "
            		+ " and ifnull(c2.abbr,'') like '%"+ sp_filter + "%' "
            		+ " and ifnull(dtf.transfer_order_no,'') like '%"+ orderNo + "%' "
    				+ " and dor.turnout_time between '" + beginTime + "' and '" + endTime+ "' "
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
            		+ " c2.abbr sp_name"
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
                    + " LEFT JOIN contact c2 on c2.id = p2.id "
                    + " where dor.status!='取消' and combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "' "
            		+ " and ifnull(depart_no,'') like '%"+ departNo + "%' "
            		+ " and ifnull(c2.abbr,'') like '%"+ sp_filter + "%' "
            		+ " and ifnull(dtf.transfer_order_no,'') like '%"+ orderNo + "%' "
    				+ " and dor.turnout_time between '" + beginTime + "' and '" + endTime+ "' "
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
        if (orderNo == null && status == null && address == null && customer == null && routeFrom == null && routeTo == null
                && beginTime == null && endTime == null) {
            sqlTotal = "select count(1) total  from transfer_order tor "
            		+ " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " 
            		+ " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code"
                    + " left join office o on o.id = tor.office_id "
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收', '已收货','已发车') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL 
                    + "' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.status!='手动删除' and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
            sql = "select tor.id,tor.order_no,tor.operation_type,tor.cargo_nature,tor.order_type,tor.planning_time,tor.cargo_nature_detail,"
            		+ " round((select sum((ifnull(toi.amount, 0) - ifnull(toi.pickup_number,0)) * ifnull(p.volume, 0)) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id = tor.id),2) total_volume,"
                    + " round((select sum((ifnull(toi.amount, 0) - ifnull(toi.pickup_number,0)) * ifnull(p.weight, 0)) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id = tor.id),2) total_weight,"
                    + " (select sum(tori.amount) - sum(ifnull(tori.pickup_number,0)) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " (select count(0) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null) atmamount,"
                    + " (select round(sum(volume),2) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null) atmvolume,"
                    + " (select round(sum(weight),2) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null) atmweight,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status,o.office_name office_name"
                    + " from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " 
                    + " left join contact c on p.contact_id = c.id "
                    + " left join location l1 on tor.route_from = l1.code " 
                    + " left join location l2 on tor.route_to = l2.code "
                    + " left join office o on o.id = tor.office_id "
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收', '已收货','已发车') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'" 
                    + " and tor.status!='手动删除'  and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
                    + sql1
                    + " order by tor.planning_time desc" + sLimit;
        } else if ("".equals(routeFrom) && "".equals(routeTo)) {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }
            sqlTotal = "select count(1) total from transfer_order tor " 
            		+ " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " 
            		+ " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code "
                    + " left join office o on tor.office_id = o.id "
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收', '已收货','已发车') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL+ "'"
                    + " and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%'"
                    + "and tor.order_no like '%"
                    + orderNo
                    + "%' and tor.status like '%"
                    + status
                    + "%' and ifnull(tor.address, '') like '%"
                    + address
                    + "%' and c.abbr like '%"
                    + customer
                    + "%' and tor.planning_time between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' and tor.order_type like '%"
                    + orderType + "%' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + "and tor.status!='手动删除'  and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
            sql = "select tor.id,tor.order_no,tor.operation_type,tor.cargo_nature,tor.order_type,tor.planning_time,tor.cargo_nature_detail,"
            		+ " round((select sum((ifnull(toi.amount, 0) - ifnull(toi.pickup_number,0)) * ifnull(p.volume, 0)) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id = tor.id),2) total_volume,"
                    + " round((select sum((ifnull(toi.amount, 0) - ifnull(toi.pickup_number,0)) * ifnull(p.weight, 0)) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id = tor.id),2) total_weight,"
                    + " (select sum(tori.amount) - sum(ifnull(tori.pickup_number,0)) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " (select count(0) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null) atmamount,"
                    + " (select round(sum(volume),2) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null) atmvolume,"
                    + " (select round(sum(weight),2) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null) atmweight,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status,o.office_name office_name"
                    + " from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + " left join location l1 on tor.route_from = l1.code " 
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " left join office o on o.id= tor.office_id"
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收', '已收货','已发车') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL
                    + "'"
                    + " and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%' and tor.order_no like '%"
                    + orderNo
                    + "%' and tor.status like '%"
                    + status
                    + "%' and ifnull(tor.address, '') like '%"
                    + address
                    + "%' and c.abbr like '%"
                    + customer
                    + "%' and tor.planning_time between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' and tor.order_type like '%"
                    + orderType + "%' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.status!='手动删除'  and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
                    + " order by tor.planning_time desc" + sLimit;
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }

            sqlTotal = "select count(1) total from transfer_order tor " + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收', '已收货','已发车','已投保') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL
                    + "'"
                    + " "
                    + " and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%'"
                    + "and tor.order_no like '%"
                    + orderNo
                    + "%' and tor.status like '%"
                    + status
                    + "%' and ifnull(tor.address, '') like '%"
                    + address
                    + "%' and c.abbr like '%"
                    + customer
                    + "%' and tor.planning_time between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' and tor.order_type like '%"
                    + orderType + "%' and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.status!='手动删除'  and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";

            sql = "select tor.id,tor.order_no,tor.operation_type,tor.cargo_nature,tor.order_type,tor.planning_time,tor.cargo_nature_detail,"
            		+ " round((select sum((ifnull(toi.amount, 0) - ifnull(toi.pickup_number,0)) * ifnull(p.volume, 0)) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id = tor.id),2) total_volume,"
                    + " round((select sum((ifnull(toi.amount, 0) - ifnull(toi.pickup_number,0)) * ifnull(p.weight, 0)) from transfer_order_item toi left join product p ON p.id = toi.product_id where toi.order_id = tor.id),2) total_weight,"
                    + " (select sum(tori.amount) - sum(ifnull(tori.pickup_number,0)) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " (select count(0) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null) atmamount,"
                    + " (select round(sum(volume),2) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null) atmvolume,"
                    + " (select round(sum(weight),2) total from transfer_order_item_detail where order_id = tor.id  and pickup_id is null) atmweight,"
                    + " tor.address,tor.pickup_mode,tor.arrival_mode,tor.status,c.abbr cname,l1.name route_from,l2.name route_to,tor.create_stamp,tor.pickup_assign_status,o.office_name office_name"
                    + " from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " 
                    + " left join contact c on p.contact_id = c.id "
                    + " left join location l1 on tor.route_from = l1.code " 
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " left join office o on o.id= tor.office_id"
                    + " where tor.operation_type =  'own' and tor.status not in ('已入库','已签收', '已收货','已发车','已投保') and ifnull(tor.pickup_assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL
                    + "'"
                    + " and ifnull(l1.name, '') like '%"
                    + routeFrom
                    + "%' and ifnull(l2.name, '') like '%"
                    + routeTo
                    + "%' and tor.order_no like '%"
                    + orderNo
                    + "%' and tor.status like '%"
                    + status
                    + "%' and ifnull(tor.address, '') like '%"
                    + address
                    + "%' and c.abbr like '%"
                    + customer
                    + "%' and tor.planning_time between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' and tor.order_type like '%"
                    + orderType + "%' and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.status!='手动删除'  and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
                    + " order by tor.planning_time desc" + sLimit;

            
        }
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> transferOrders = Db.find(sql);

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
            orderIds += departTransferOrder.get("order_id") + ",";
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
        String trItem = getPara("tr_item");// 货品id
        String itemDetail = getPara("item_detail");// 单品id
        String pickId = getPara("pickupId");
        String departOrderId = getPara("departOrderId");
        
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "SELECT COUNT(0) total FROM(SELECT(SELECT count(0) total FROM	transfer_order_item_detail WHERE order_id = tor.id	AND item_id = toi.id AND depart_id = 1632) atmamount, ifnull(toi.amount, 0) cargoamount FROM transfer_order_item toi LEFT JOIN transfer_order tor ON tor.id = toi.order_id WHERE toi.order_id IN (" + orderId + ")) a where (atmamount > 0 or cargoamount>0)";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        String sql = "";
        if(!"".equals(pickId) && pickId != null){
        	 sql = " SELECT * FROM(select toi.id,ifnull(toi.item_name, pd.item_name) item_name,tor.planning_time,ifnull(toi.item_no, pd.item_no) item_no,"
             		 + " round(ifnull(pd.volume, 0),2) volume,round(ifnull(pd.weight, 0),2) weight,tor.cargo_nature,c.abbr customer,tor.order_no,toi.remark,"
             		 + " ifnull((select count(0) total from transfer_order_item_detail where order_id = tor.id and item_id = toi.id and pickup_id = '"+pickId+"'), 0) atmamount,"
                     + " ifnull((select ifnull(dt.amount, 0)  from depart_transfer dt where dt.order_item_id = toi.id and dt.pickup_id = '"+pickId+"'), 0) cargoamount,"
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
        }else{
        	 sql = "SELECT * FROM(select toi.id,ifnull(toi.item_name, pd.item_name) item_name,tor.planning_time,ifnull(toi.item_no, pd.item_no) item_no,"
             		+ " round(ifnull(pd.volume, 0),2) volume,round(ifnull(pd.weight, 0),2) weight,tor.cargo_nature,"
             		+ " (select count(0) total from transfer_order_item_detail where order_id = tor.id and item_id = toi.id and depart_id = "+departOrderId+") atmamount,"
                     + " ifnull(toi.amount, 0) cargoamount,ifnull(toi.volume, 0) cargovolume,ifnull(toi.sum_weight, 0) cargoweight,c.abbr customer,tor.order_no,toi.remark  from transfer_order_item toi "
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
    public void savePickupOrder() {
        DepartOrder pickupOrder = null;
        String pickId = getPara("pickupId");
        String driverId = getPara("driver_id");
        String carinfoId = getPara("carinfoId");
        Carinfo carinfo = null;
        String checkedDetail = getPara("checkedDetail");
        String uncheckedDetailIds = getPara("uncheckedDetail");
        String replenishmentOrderId = getPara("replenishmentOrderId");
        String gateInSelect = getPara("gateInSelect");
        String returnTime = getPara("return_time");
        String datailIdsStr = getPara("detailIds");
        String con_address = getPara("con_address");
        String[] detailIds = getPara("detailIds").split(",");
        String[] orderids = getPara("orderid").split(",");
        String[] cargoIds = getPara("cargoIds").split(",");
        String[] cargoNumbers = getPara("cargoNumbers").split("&");
        String[] cargoItemIds = getPara("cargoItemIds").split("&");
        String[] driverAssistantIds = getPara("driverAssistantIds").split(",");
        String[] driverAssistantNames = getPara("driverAssistantNames").split(",");
        String[] driverAssistantPhones = getPara("driverAssistantPhones").split(",");
        
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
            /*if(getPara("payment") != null && !"".equals(getPara("payment"))){
            	pickupOrder.set("payment", getPara("payment"));
            }*/
            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            pickupOrder.set("create_stamp", sqlDate);
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
            String[] values = getParaValues("checkbox");
            if (values != null) {
                if (values.length == 1) {
                    for (int i = 0; i < values.length; i++) {
                        if ("yandCheckbox".equals(values[i])) {
                            pickupOrder.set("address", getPara("address"));
                            pickupOrder.set("warehouse_id", null);
                        }
                        if ("warehouseCheckbox".equals(values[i])) {
                        	
                        	if(gateInSelect == "" || gateInSelect == null){
                        		pickupOrder.set("warehouse_id", replenishmentOrderId);
                        	}else{
                        		pickupOrder.set("warehouse_id", gateInSelect);
                        	}
                            pickupOrder.set("address", null);
                        }
                        if ("receiverCheckbox".equals(values[i])) {
                        	pickupOrder.set("is_direct_deliver", true);
                        	/*String sql = "SELECT *,con.id con_id FROM transfer_order tra "
                                    + " LEFT JOIN party de on de.id = tra.notify_party_id"
                                    + " LEFT JOIN contact con on con.id = de.contact_id"
                                    + " where tra.id in("+orderids[0]+")";
                        	Record record =Db.findFirst(sql);
                        	record.get("con_id");
                        	Contact contact = Contact.dao.findById(record.get("con_id"));
                        	contact.set("address", con_address).update();*/
                        	TransferOrder transferOrder = TransferOrder.dao.findById(orderids[0]);
                        	transferOrder.set("receiving_address", con_address).update();
                        }else{
                        	pickupOrder.set("is_direct_deliver", false);
                        }
                    }
                } else {
                    for (int i = 0; i < values.length; i++) {
                        if ("yandCheckbox".equals(values[i])) {
                            pickupOrder.set("address", getPara("address"));
                        }
                        if ("warehouseCheckbox".equals(values[i])) {
                        	if(gateInSelect == "" || gateInSelect == null){
                        		pickupOrder.set("warehouse_id", replenishmentOrderId);
                        	}else{
                        		pickupOrder.set("warehouse_id", gateInSelect);
                        	}
                        }
                    }
                }
            } else {
                pickupOrder.set("address", null);
                pickupOrder.set("warehouse_id", null);
                pickupOrder.set("is_direct_deliver", false);
            }
            pickupOrder.set("car_summary_type", "untreated");
            pickupOrder.save();
            //saveDepartTransfer(pickupOrder, getPara("orderid"), checkedDetail, uncheckedDetailIds);
            savePickupOrderMilestone(pickupOrder);
            
             //ATM单品
            if(detailIds[0].trim() != ""){
	            for (int i = 0; i < detailIds.length; i++) {
					TransferOrderItemDetail detail = TransferOrderItemDetail.dao.findById(detailIds[i]);
					detail.set("pickup_id",pickupOrder.get("id")).update();
				}
	           
	            String findOrderIdSql = "select group_concat(distinct cast(order_id as char) separator ',') id from transfer_order_item_detail where id in ("+datailIdsStr+");";
	            Record rec = Db.findFirst(findOrderIdSql);
	            String[] TransferIds = rec.getStr("id").split(",");
	            for (int i = 0; i < TransferIds.length; i++) {
	            	//运输单单品总数
	            	Record totalTransferOrderAmount = Db.findFirst("select count(0) total from transfer_order_item_detail where order_id = " + TransferIds[i]);
	            	//总提货数量（之前+现在）
	            	Record totalPickAmount = Db.findFirst("select count(0) total from transfer_order_item_detail where pickup_id is not null and  order_id = " + TransferIds[i]);
	            	//运输单
					TransferOrder transferOrderATM = TransferOrder.dao.findById(TransferIds[i]);
					if(totalPickAmount.getLong("total") == totalTransferOrderAmount.getLong("total")){
						transferOrderATM.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
					}else{
						transferOrderATM.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
					}
					transferOrderATM.set("pickup_mode", pickupOrder.get("pickup_mode"));
					transferOrderATM.update();
					//从表
					DepartTransferOrder departTransferOrder = new DepartTransferOrder();
		            departTransferOrder.set("pickup_id", pickupOrder.get("id"));
		            departTransferOrder.set("order_id", TransferIds[i]);
		            departTransferOrder.set("amount", detailIds.length);
		            departTransferOrder.set("transfer_order_no", transferOrderATM.get("order_no"));
		            departTransferOrder.save();
	            }
            }
            //普货 - 多次提货
            if(cargoIds[0].trim() != ""){
	            for (int i = 0; i < cargoIds.length; i++) {
	            	TransferOrder transferOrderCargo = TransferOrder.dao.findById(cargoIds[i]);
	            	/*这里是一次提货
	            	transferOrderCargo.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
	            	transferOrderCargo.set("pickup_mode", pickupOrder.get("pickup_mode"));
					transferOrderCargo.update();
					//从表
					DepartTransferOrder departTransferOrder = new DepartTransferOrder();
		            departTransferOrder.set("pickup_id", pickupOrder.get("id"));
		            departTransferOrder.set("order_id", cargoIds[i]);
		            departTransferOrder.set("transfer_order_no", transferOrderCargo.get("order_no"));
		            departTransferOrder.save();*/
	            	//总提货数量（之前+现在）
	            	double sumPickAmount = 0;
	            	//运输单货品总数（sum）
	            	double sumTransferOrderItemAmount = 0;
	            	String[] cargoNumber = cargoNumbers[i].split(",");
	            	//去除空字符串
	            	
	            	String findItemSql = "select * from transfer_order_item where order_id = " + cargoIds[i];
					List<TransferOrderItem> itemlList = TransferOrderItem.dao.find(findItemSql);
					for (int j = 0; j < itemlList.size(); j++) {
						if(!"".equals(cargoNumber[j].trim()) && Double.parseDouble(cargoNumber[j]) != 0){
							double pickupAmount = 0;
							if(itemlList.get(j).get("pickup_number") != null && !"".equals(itemlList.get(j).get("pickup_number"))){
								pickupAmount = itemlList.get(j).getDouble("pickup_number");
							}
							
							itemlList.get(j).set("pickup_number",pickupAmount + Double.parseDouble(cargoNumber[j])).update();
							//从表
							DepartTransferOrder departTransferOrder = new DepartTransferOrder();
				            departTransferOrder.set("pickup_id", pickupOrder.get("id"));
				            departTransferOrder.set("order_id", cargoIds[i]);
				            departTransferOrder.set("amount", cargoNumber[j]);
				            departTransferOrder.set("order_item_id", itemlList.get(j).get("id"));
				            departTransferOrder.set("transfer_order_no", transferOrderCargo.get("order_no"));
				            departTransferOrder.save();
				            
							sumPickAmount+=(pickupAmount + Double.parseDouble(cargoNumber[j]));
							if(itemlList.get(j).get("amount") != null && !"".equals(itemlList.get(j).get("amount"))){
								sumTransferOrderItemAmount += itemlList.get(j).getDouble("amount");
							}
						}
					}
					//运输单
					if(sumPickAmount == sumTransferOrderItemAmount){
						transferOrderCargo.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
					}else{
						transferOrderCargo.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
					}
					transferOrderCargo.set("pickup_mode", pickupOrder.get("pickup_mode")).update();
				}
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
            /*if(getPara("payment") != null && !"".equals(getPara("payment"))){
            	pickupOrder.set("payment", getPara("payment"));
            }*/
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
            String[] values = getParaValues("checkbox");
            if (values != null) {
                if (values.length == 1) {
                    for (int i = 0; i < values.length; i++) {
                        if ("yandCheckbox".equals(values[i])) {
                            pickupOrder.set("address", getPara("address"));
                            pickupOrder.set("warehouse_id", null);
                        }
                        if ("warehouseCheckbox".equals(values[i])) {
                        	if(gateInSelect == "" || gateInSelect == null){
                        		pickupOrder.set("warehouse_id", replenishmentOrderId);
                        	}else{
                        		pickupOrder.set("warehouse_id", gateInSelect);
                        	}
                            pickupOrder.set("address", null);
                        }
                    }
                } else {
                    for (int i = 0; i < values.length; i++) {
                        if ("yandCheckbox".equals(values[i])) {
                            pickupOrder.set("address", getPara("address"));
                        }
                        if ("warehouseCheckbox".equals(values[i])) {
                        	if(gateInSelect == "" || gateInSelect == null){
                        		pickupOrder.set("warehouse_id", replenishmentOrderId);
                        	}else{
                        		pickupOrder.set("warehouse_id", gateInSelect);
                        	}
                        }
                    }
                }
            } else {
                pickupOrder.set("address", null);
                pickupOrder.set("warehouse_id", null);
            }
            pickupOrder.update();
            //updateDepartTransfer(pickupOrder, getPara("orderid"), checkedDetail, uncheckedDetailIds);
        }
        
        //跟车人员
        List<PickupDriverAssistant> assistantList = PickupDriverAssistant.dao.find("select id from pickup_driver_assistant where pickup_id = ?",pickupOrder.get("id"));
        for (PickupDriverAssistant pickupDriverAssistant : assistantList) {
        	PickupDriverAssistant.dao.deleteById(pickupDriverAssistant.get("id"));
		}
        for (int i = 0; i < driverAssistantIds.length; i++) {
        	if(!"".equals(driverAssistantIds[i])){
        		PickupDriverAssistant assistant = new PickupDriverAssistant();
            	assistant.set("pickup_id",pickupOrder.get("id"))
            	.set("driver_assistant_id",driverAssistantIds[i])
            	.set("name",driverAssistantNames[i])
            	.set("phone",driverAssistantPhones[i])
            	.save();
        	}
		}
        renderJson(pickupOrder);
    }
    
    
 //撤销调车单
 	public void cancelPickupOder() {
 		String pickupId = getPara("pickupId");
 		String sql="SELECT * FROM depart_order d where d. STATUS IN ('已发车', '已收货', '已入货场' , '已入库' ) and d.id = '"+pickupId+ "'";
 		List<Record> nextOrders = Db.find(sql);
 		if(nextOrders.size()==0){
 			//取消里程碑
 			Record r1 = Db.findFirst("SELECT * FROM transfer_order_milestone where pickup_id = '" + pickupId+ "'");
 			TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
 			String name = (String) currentUser.getPrincipal();
 	        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
 	        transferOrderMilestone.set("create_by", users.get(0).get("id"));
 	        transferOrderMilestone.set("status", "取消");
 	        java.util.Date utilDate = new java.util.Date();
 	        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
 	        transferOrderMilestone.set("create_stamp", sqlDate);
 	        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE);
 	        transferOrderMilestone.set("pickup_id", pickupId);
 	        transferOrderMilestone.save();
 			
 			List<Record> r = Db.find("select * from depart_transfer dt where dt.pickup_id = '"+pickupId + "'");
 			for(int i = 0; i < r.size(); i++){
 				//运输单单品总数
            	Record totalTransferOrderAmount = Db.findFirst("select count(0) total from transfer_order_item_detail where order_id = " + r.get(i).getLong("order_id"));
            	//总提货数量（之前+现在）
            	Record totalPickAmount = Db.findFirst("select count(0) total from transfer_order_item_detail where pickup_id is not null and  order_id = '" +  r.get(i).getLong("order_id")+"'");
            	//总提货数量（现在）
            	Record currentPickAmount = Db.findFirst("select count(0) total from transfer_order_item_detail where pickup_id is not null and  order_id = '" +  r.get(i).getLong("order_id")+"' and pickup_id = '"+pickupId+"'");
            	//回滚运输单状态
            	if(currentPickAmount.getLong("total") == totalTransferOrderAmount.getLong("total")){
            		TransferOrder.dao.findById(r.get(i).getLong("order_id")).set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_NEW).set("pickup_mode", null).set("pickup_seq",null).update();
				}else{
					if(currentPickAmount.getLong("total") == totalPickAmount.getLong("total")){
						TransferOrder.dao.findById(r.get(i).getLong("order_id")).set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_NEW).set("pickup_mode", null).set("pickup_seq",null).update();
					}else{
						TransferOrder.dao.findById(r.get(i).getLong("order_id")).set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL).update();
					}
				}
            	//删除DepartTransferOrder表数据
     			DepartTransferOrder.dao.findById(r.get(i).getLong("id")).delete();
 			}
 			//回滚单品数量
 			List<Record> detail = Db.find("SELECT * FROM transfer_order_item_detail where pickup_id = '" + pickupId+ "'");
 			for(int i = 0; i < detail.size(); i++){
 				TransferOrderItemDetail.dao.findById(detail.get(i).getLong("id")).set("pickup_id", null).update();
 			}
 			
 			//更新调车单表运输状态为取消
 			DepartOrder.dao.findById(pickupId).set("Status", "取消").update();
 			renderJson("{\"success\":true}");
 		}else{
 			renderJson("{\"success\":false}");
 		}
 		
 	}
    
    
    
    // 保存发车记录单
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
                pickupOrder.get("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
            if (transferOrder != null) {
                transferOrder.set("pickup_mode", pickupOrder.get("pickup_mode"));
                transferOrder.update();
            }
        }
    }

    // 更新中间表
    private void updateDepartTransfer(DepartOrder pickupOrder, String orderId, String checkedDetail, String uncheckedDetailId) {
        if (checkedDetail != null && !"".equals(checkedDetail)) {
            String[] checkedDetailIds = checkedDetail.split(",");
            TransferOrderItemDetail transferOrderItemDetail = null;
            for (int j = 0; j < checkedDetailIds.length && checkedDetailIds.length > 0; j++) {
                transferOrderItemDetail = TransferOrderItemDetail.dao.findById(checkedDetailIds[j]);
                transferOrderItemDetail.set("pickup_id", pickupOrder.get("id"));
                transferOrderItemDetail.update();
            }
            TransferOrder transferOrder = TransferOrder.dao.findById(transferOrderItemDetail.get("order_id"));
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
                Long departId = transferOrderItemDetail.get("pickup_id");
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
    private void pickupOrderEdit(){
    	String sql = "select dor.*,co.contact_person,co.phone,u.user_name,(select group_concat(cast(dt.order_id as char)  separator',')  from depart_transfer  dt "
                + "where dt.pickup_id =dor.id)as order_id from depart_order  dor "
                + "left join contact co on co.id in( select p.contact_id  from party p where p.id=dor.driver_id ) "
                + "left join user_login  u on u.id=dor.create_by where dor.combine_type ='"
                + DepartOrder.COMBINE_TYPE_PICKUP
                + "' and dor.id in(" + getPara("id") + ")";
        DepartOrder pickupOrder = DepartOrder.dao.findFirst(sql);
        setAttr("pickupOrder", pickupOrder);
        
        String list = pickupOrder.getStr("order_id");
        String[] transferOrderIds = list.split(",");
        TransferOrder transferOrderAttr = TransferOrder.dao.findById(transferOrderIds[0]);
        setAttr("transferOrderAttr", transferOrderAttr);
        
        String sq = "SELECT tra.receiving_address FROM transfer_order tra "
                + " where tra.id in("+transferOrderIds[0]+")";
        Record tro = Db.findFirst(sq);
        setAttr("transferOrder",tro);
        setAttr("t_id",transferOrderIds[0]);
        
        

        Long sp_id = pickupOrder.get("sp_id");
        if (sp_id != null) {
            Party sp = Party.dao.findById(sp_id);
            Contact spContact = Contact.dao.findById(sp.get("contact_id"));
            setAttr("spContact", spContact);
        }
        Long driverId = pickupOrder.get("driver_id");
        if (driverId != null) {
            Party driver = Party.dao.findById(driverId);
            Contact driverContact = Contact.dao.findById(driver.get("contact_id"));
            setAttr("driverContact", driverContact);
        }
        Long carinfoId = pickupOrder.get("carinfo_id");
        if (carinfoId != null) {
            Carinfo carinfo = Carinfo.dao.findById(carinfoId);
            setAttr("carinfo", carinfo);
        }
        UserLogin userLogin = UserLogin.dao.findById(pickupOrder.get("create_by"));
        setAttr("userLogin2", userLogin);
        setAttr("pickup_id", getPara());
        String orderId = "";
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find("select * from depart_transfer where pickup_id = ?",
                pickupOrder.get("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            orderId += departTransferOrder.get("order_id") + ",";
        }
        orderId = orderId.substring(0, orderId.length() - 1);
        setAttr("localArr", orderId);

        TransferOrderMilestone transferOrderMilestone = TransferOrderMilestone.dao.findFirst(
                "select * from transfer_order_milestone where pickup_id = ? order by create_stamp desc", pickupOrder.get("id"));
        setAttr("transferOrderMilestone", transferOrderMilestone);
        
        List<Record> paymentItemList = Collections.EMPTY_LIST;
        paymentItemList = Db.find("select * from fin_item where type='应付'");
        setAttr("paymentItemList", paymentItemList);
        List<Record> incomeItemList = Collections.EMPTY_LIST;
        incomeItemList = Db.find("select * from fin_item where type='应收'");
        setAttr("incomeItemList", incomeItemList);
        
        String finItemIds = "";
        List<PickupOrderFinItem> PickupOrderFinItems = PickupOrderFinItem.dao.find("select * from pickup_order_fin_item where pickup_order_id = ?", pickupOrder.get("id"));
        for(PickupOrderFinItem pickupOrderFinItem : PickupOrderFinItems){
        	finItemIds += pickupOrderFinItem.get("fin_item_id") + ",";
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
    private void savePickupOrderMilestone(DepartOrder pickupOrder) {
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        transferOrderMilestone.set("status", "新建");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE);
        transferOrderMilestone.set("pickup_id", pickupOrder.get("id"));
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
            if (pm.get("pickup_id") != null) {
                TransferOrderMilestone transferOrderMilestone = TransferOrderMilestone.dao
                        .findFirst("select tom.*,dto.depart_no from transfer_order_milestone tom "
                                + " left join depart_order dto on dto.id = " + pm.get("pickup_id") + " where tom.type = '"
                                + TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE + "' and tom.pickup_id=" + pm.get("pickup_id")
                                + " and dto.combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "' order by tom.create_stamp desc");
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = userLogin.get("user_name");
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
    public void finishPickupOrder() {
        String pickupOrderId = getPara("pickupOrderId");
        DepartOrder pickupOrder = DepartOrder.dao.findById(pickupOrderId);
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find("select * from depart_transfer where pickup_id = ?",
                pickupOrder.get("id"));
        //TransferOrder tro = TransferOrder.dao.findById(departTransferOrders.get(0).get("order_id"));
        //boolean direct = false;
        boolean direct=false;
        if(pickupOrder.getBoolean("is_direct_deliver")!=null){
         direct = pickupOrder.getBoolean("is_direct_deliver");
        }
        if(!direct){
	        //相关运输单业务处理:提货发车之后，运输单中除了补货订单状态为已入库外，其他都是默认为正在处理状态
	        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
	            TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
	            TransferOrderMilestone milestone = new TransferOrderMilestone();
	            if ("新建".equals(transferOrder.get("status")) || "部分已入货场".equals(transferOrder.get("status")) || "部分已入库".equals(transferOrder.get("status"))) {
	                if ("movesOrder".equals(transferOrder.get("order_type")) ||"salesOrder".equals(transferOrder.get("order_type")) || "arrangementOrder".equals(transferOrder.get("order_type")) || "cargoReturnOrder".equals(transferOrder.get("order_type"))) {//销售订单
	                    if (transferOrder.get("pickup_assign_status").equals(TransferOrder.ASSIGN_STATUS_PARTIAL)) {
	                        transferOrder.set("status", "部分已入货场");
	                        milestone.set("status", "部分已入货场");
	                        transferOrder.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
	                    } else {
	                        transferOrder.set("status", "已入货场");
	                        milestone.set("status", "已入货场");
	                        transferOrder.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
	                    }
	                    if("arrangementOrder".equals(transferOrder.get("order_type")) || "cargoReturnOrder".equals(transferOrder.get("order_type"))){
	                    	SubtractInventory(pickupOrder,transferOrder);
	                    }
	                    
	                } else if ("replenishmentOrder".equals(transferOrder.get("order_type"))) {
	                        transferOrder.set("status", "已入库");
	                        milestone.set("status", "已入库");
	                        transferOrder.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
	                }
	            }
	            transferOrder.update();
	            milestone.set("location", "");
	            milestone.set("order_id", transferOrder.get("id"));
	            String name = (String) currentUser.getPrincipal();
	            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
	            milestone.set("create_by", users.get(0).get("id"));
	            java.util.Date utilDate = new java.util.Date();
	            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
	            milestone.set("create_stamp", sqlDate);
	            milestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
	            milestone.save();
	        }
        }
        //调车单业务处理
        TransferOrder transferOrderType = TransferOrder.dao.findById(departTransferOrders.get(0).get("order_id"));
        TransferOrderMilestone pickupMilestone = new TransferOrderMilestone();
        if(!direct){
	        if (transferOrderType.get("order_type").equals("salesOrder") || transferOrderType.get("order_type").equals("movesOrder") || transferOrderType.get("order_type").equals("arrangementOrder") || transferOrderType.get("order_type").equals("cargoReturnOrder")) {
	            pickupOrder.set("status", "已入货场");
	            pickupMilestone.set("status", "已入货场");
	        } else if (transferOrderType.get("order_type").equals("replenishmentOrder")) {
	            pickupOrder.set("status", "已入库");
	            pickupMilestone.set("status", "已入库");
	            if (!"".equals(pickupOrderId) && pickupOrderId != null) {
	                productInWarehouse(pickupOrderId);
	            }
	        }
	        pickupOrder.update();
	        String name = (String) currentUser.getPrincipal();
	        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
	        pickupMilestone.set("create_by", users.get(0).get("id"));
	        java.util.Date utilDate = new java.util.Date();
	        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
	        pickupMilestone.set("create_stamp", sqlDate);
	        pickupMilestone.set("pickup_id", pickupOrder.get("id"));
	        pickupMilestone.set("type", TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE);
	        pickupMilestone.save();
        }else{
        	//获取运输单中的应付
        	String sql1 = "SELECT count(1) total FROM transfer_order_fin_item tor "
        			+ " LEFT JOIN depart_transfer dt on tor.order_id = dt.order_id "
        			+ " where dt.pickup_id = "+ pickupOrderId
        			+ " and tor.fin_item_id in (1,2,3,18,32,33,34,35)";
        	String sql = "SELECT tor.* FROM transfer_order_fin_item tor "
        			+ " LEFT JOIN depart_transfer dt on tor.order_id = dt.order_id "
        			+ " where dt.pickup_id = "+ pickupOrderId
        			+ " and tor.fin_item_id in (1,2,3,18,32,33,34,35)";
        	Record rc1 = Db.findFirst(sql1);
        	Record rc = Db.findFirst(sql);
        	//System.out.println(rc.hashCode());
        	if(rc1.getLong("total")>0){
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
        String chargeType = pickupOrder.get("charge_type");

        List<DepartTransferOrder> dItem = DepartTransferOrder.dao.find("select order_id from depart_transfer where pickup_id ='"
                + getPara("pickupOrderId") + "'");
        String transferId = "";
        for (DepartTransferOrder dItem2 : dItem) {
            transferId += dItem2.get("order_id") + ",";
        }
        transferId = transferId.substring(0, transferId.length() - 1);

        List<Record> transferOrderItemList = Db
                .find("select toi.*, t_o.route_from, t_o.route_to from transfer_order_item toi left join transfer_order t_o on toi.order_id = t_o.id where toi.order_id in("
                        + transferId + ") order by pickup_seq desc");
        String name = (String) currentUser.getPrincipal();
    	List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        pickupMilestone.set("create_by", users.get(0).get("id"));
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
        if (pickupOrder.get("income") != null) {
            TransferOrderFinItem tFinItem2 = new TransferOrderFinItem();
            int size = dItem.size();
            for (int i = 0; i < dItem.size(); i++) {
                tFinItem2.set("fin_item_id", "4");// TODO 死代码
                tFinItem2.set("amount", Double.parseDouble(pickupOrder.get("income").toString()) / size);
                // tFinItem2.set("order_id", dItem.get(i).get("order_id"));
                tFinItem2.set("status", "未完成");
                tFinItem2.set("creator", users.get(0).get("id"));
                tFinItem2.set("create_date", sqlDate);
                tFinItem2.save();
            }
        }
        renderJson("{\"success\":true}");
    }

    private void genFinPerUnit(DepartOrder pickupOrder, List<UserLogin> users, java.sql.Timestamp sqlDate, Contract spContract,
            String chargeType, List<Record> transferOrderItemList) {
        for (Record tOrderItemRecord : transferOrderItemList) {
            // TODO 这个是递归，可以整理成一个递归函数
            Record contractFinItem = Db.findFirst("select amount, fin_item_id from contract_item where contract_id ="
                    + spContract.getLong("id") + " and product_id =" + tOrderItemRecord.get("product_id") + " and from_id = '"
                    + tOrderItemRecord.get("route_from") + "' and to_id = '" + tOrderItemRecord.get("route_to") + "' and priceType='"
                    + chargeType + "'");

            if (contractFinItem != null) {
                genFinItem(pickupOrder, users, sqlDate, tOrderItemRecord, contractFinItem);
            } else {
                contractFinItem = Db.findFirst("select amount, fin_item_id from contract_item where contract_id ="
                        + spContract.getLong("id") + " and product_id =" + tOrderItemRecord.get("product_id") + " and from_id = '"
                        + tOrderItemRecord.get("route_from") + "' and priceType='" + chargeType + "'");

                if (contractFinItem != null) {
                    genFinItem(pickupOrder, users, sqlDate, tOrderItemRecord, contractFinItem);
                } else {
                    contractFinItem = Db.findFirst("select amount, fin_item_id from contract_item where contract_id ="
                            + spContract.getLong("id") + " and from_id = '" + tOrderItemRecord.get("route_from") + "' and to_id = '"
                            + tOrderItemRecord.get("route_to") + "' and priceType='" + chargeType + "'");

                    if (contractFinItem != null) {
                        genFinItem(pickupOrder, users, sqlDate, tOrderItemRecord, contractFinItem);
                    } else {
                        contractFinItem = Db.findFirst("select amount, fin_item_id from contract_item where contract_id ="
                                + spContract.getLong("id") + " and from_id = '" + tOrderItemRecord.get("route_from") + "' and priceType='"
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
        pickupFinItem.set("fin_item_id", contractFinItem.get("fin_item_id"));
        pickupFinItem.set("amount", contractFinItem.getDouble("amount") * tOrderItemRecord.getDouble("amount"));
        pickupFinItem.set("depart_order_id", pickupOrder.getLong("id"));
        pickupFinItem.set("status", "未完成");
        pickupFinItem.set("creator", users.get(0).get("id"));
        pickupFinItem.set("create_date", sqlDate);
        pickupFinItem.save();
    }

    // 产品入库
    public void productInWarehouse(String departId) {
        if (!"".equals(departId) && departId != null) {
            String orderIds = "";
            List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                    "select * from depart_transfer where pickup_id = ?", departId);
            for (DepartTransferOrder departTransferOrder : departTransferOrders) {
                orderIds += departTransferOrder.get("order_id") + ",";
            }
            orderIds = orderIds.substring(0, orderIds.length() - 1);
            List<TransferOrder> transferOrders = TransferOrder.dao.find("select * from transfer_order where id in(" + orderIds + ")");
            for (TransferOrder transferOrder : transferOrders) {
                InventoryItem inventoryItem = null;
                List<TransferOrderItem> transferOrderItems = TransferOrderItem.dao.find(
                        "select * from transfer_order_item where order_id = ?", transferOrder.get("id"));
                for (TransferOrderItem transferOrderItem : transferOrderItems) {
                    if (transferOrderItem != null) {
                        if (transferOrderItem.get("product_id") != null) {
                            String inventoryItemSql = "select * from inventory_item where product_id = "
                                    + transferOrderItem.get("product_id") + " and warehouse_id = " + transferOrder.get("warehouse_id");
                            inventoryItem = InventoryItem.dao.findFirst(inventoryItemSql);
                            String sqlTotal = "select count(1) total from transfer_order_item_detail where pickup_id = " + departId
                                    + " and order_id = " + transferOrder.get("id");
                            Record rec = Db.findFirst(sqlTotal);
                            Long amount = rec.getLong("total");
                            if(amount == 0){
                            	amount = Math.round(transferOrderItem.getDouble("amount"));
                            }
                            if (inventoryItem == null) {
                                inventoryItem = new InventoryItem();
                                inventoryItem.set("party_id", transferOrder.get("customer_id"));
                                inventoryItem.set("warehouse_id", transferOrder.get("warehouse_id"));
                                inventoryItem.set("product_id", transferOrderItem.get("product_id"));
                                inventoryItem.set("total_quantity", amount);
                                inventoryItem.save();
                            } else {
                                inventoryItem.set("total_quantity", Double.parseDouble(inventoryItem.get("total_quantity").toString())
                                        + amount);
                                inventoryItem.update();
                            }
                        }
                    }
                }
            }
        }
    }

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
                            departTransferOrder.get("order_id"));
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
        
        String sql = "select distinct tor.*,c.abbr cname ,deo.is_direct_deliver from transfer_order tor"
        		+ " left join party p on tor.customer_id = p.id left join contact c on c.id = p.contact_id "
        		+ " left join depart_transfer dept on dept.order_id = tor.id "
        		+ " left join depart_order deo on deo.id = dept.pickup_id"
        		+ " where tor.id in("+orderIds+") order by tor.pickup_seq desc " + sLimit;

        //logger.debug("sql:" + sql);
        List<Record> transferOrders = Db.find(sql);

        Map transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);

        renderJson(transferOrderListMap);    	
    }
    
    public void swapPickupSeq() {
        TransferOrder transferOrder = TransferOrder.dao.findById(getPara("currentId"));
        transferOrder.set("pickup_seq", getPara("currentVal"));
        transferOrder.update();
        TransferOrder transferOrderPrev = TransferOrder.dao.findById(getPara("targetId"));
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
                    if (transOrderId.equals(departTransferOrder.get("order_id") + "")) {
                        // 去掉入库的单据
                        TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
                        orderId += transferOrder.get("id") + ",";
                        transferOrder.set("status", "已入库");
                        transferOrder.update();
                        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
                        transferOrderMilestone.set("status", "已入库");
                        String name = (String) currentUser.getPrincipal();
                        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
                        transferOrderMilestone.set("create_by", users.get(0).get("id"));
                        java.util.Date utilDate = new java.util.Date();
                        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
                        transferOrderMilestone.set("create_stamp", sqlDate);
                        transferOrderMilestone.set("order_id", transferOrder.get("id"));
                        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);

                        // 产品入库
                        productInWarehouseOnTransferOrderId(transOrderId);
                    }
                }
            }
        }
        renderJson("{\"success\":true}");
    }
     
    //直送调车完成后生成回单
    public void receipt(String receiverId) {
        String order_id = receiverId;
        DepartTransferOrder depart = DepartTransferOrder.dao.findFirst("select * from depart_transfer where order_id = "+order_id);
        Object departOrderId = depart.get("pickup_id");
        depart.set("depart_id", departOrderId).update();
        
        //TransferOrder transferOrder = TransferOrder.dao.findById(order_id);
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        //判断当前直送的模式，是入中转仓还是直接发送给客户工厂，如果是入中转仓，增加库存
        DepartOrderController.productInWarehouse(getPara("departOrderId"));
        //修改发车单信息
        DepartOrder departOrder = DepartOrder.dao.findById(departOrderId);
        departOrder.set("status", "已收货")
        .set("sign_status", "已回单").update();
        //直接生成回单，在把合同等费用带到回单中
        String orderNo = OrderNoGenerator.getNextOrderNo("HD");
        ReturnOrder returnOrder = new ReturnOrder();
        returnOrder.set("order_no", orderNo)
        .set("transaction_status", "新建")
        .set("creator", users.get(0).get("id"))
        .set("create_date", sqlDate)
        //回单中"transfer_order_id"字段修改为“depart_id”
        //.set("depart_id", departOrderId)
        .set("transfer_order_id", depart.get("order_id")).save();
        
        
        
        //修改运输单信息
        List<Record> departOrderIds = Db.find("select order_id from depart_transfer where pickup_id = ? ;",departOrderId);
        for (Record record : departOrderIds) {
        	long transerOrderId = record.getLong("order_id");
        	TransferOrder transferOrder = TransferOrder.dao.findById(transerOrderId);
			transferOrder.set("status", "已收货").update();
			//设置回单客户信息，必须是同一个客户
            returnOrder.set("customer_id", transferOrder.get("customer_id")).update();
            
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "已收货")
            .set("create_by", users.get(0).get("id"))
            .set("location", "")
            .set("create_stamp", sqlDate)
            .set("order_id", depart.get("order_id"))
            .set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE)
            .save();

            transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "已收货")
            .set("create_by", users.get(0).get("id"))
            .set("location", "")
            .set("create_stamp", sqlDate)
            .set("depart_id", departOrderId)
            .set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE)
            .save();
            
            ReturnOrderController roController= new ReturnOrderController(); 
            //把运输单的应收带到回单中
            roController.tansferIncomeFinItemToReturnFinItem(returnOrder, Long.parseLong("0"), transerOrderId);
            //直送时把保险单费用带到回单
            roController.addInsuranceFin(transferOrder, departOrder, returnOrder);
            //TODO:根据合同生成费用
            roController.calculateChargeByCustomer(transferOrder, returnOrder.getLong("id"), users);
		}
    }
    
    // 筛选掉入库的运输单
    public void getTransferOrderDestination() {
    	String warehouseIds = getPara("warehouseIds");
    	String receiverId = getPara("receiverId");
    	if(receiverId != null && !"".equals(receiverId)){
    		receipt(receiverId);
    	}else{
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
				transferOrderMilestone.set("create_by", users.get(0).get("id"));
				java.util.Date utilDate = new java.util.Date();
				java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
				transferOrderMilestone.set("create_stamp", sqlDate);
				transferOrderMilestone.set("order_id", transferOrder.get("id"));
				transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
				
				// 产品入库
				productInWarehouseOnTransferOrderId(warehouseIdArr[i]);
			}
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
                    transferOrder.get("id"));
            for (TransferOrderItem transferOrderItem : transferOrderItems) {
                if (transferOrderItem != null) {
                    if (transferOrderItem.get("product_id") != null) {
                        String inventoryItemSql = "select * from inventory_item where product_id = " + transferOrderItem.get("product_id")
                                + " and warehouse_id = " + transferOrder.get("warehouse_id");
                        inventoryItem = InventoryItem.dao.findFirst(inventoryItemSql);
                        String sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + transOrderId;
                        Record rec = Db.findFirst(sqlTotal);
                        Long amount = rec.getLong("total");
                        if (inventoryItem == null) {
                            inventoryItem = new InventoryItem();
                            inventoryItem.set("party_id", transferOrder.get("customer_id"));
                            inventoryItem.set("warehouse_id", transferOrder.get("warehouse_id"));
                            inventoryItem.set("product_id", transferOrderItem.get("product_id"));
                            inventoryItem.set("total_quantity", amount);
                            inventoryItem.save();
                        } else {
                            inventoryItem
                                    .set("total_quantity", Double.parseDouble(inventoryItem.get("total_quantity").toString()) + amount);
                            inventoryItem.update();
                        }
                    }
                }
            }
        }
    }

    public void savegateIn(DepartTransferOrder departTransferOrder) {
        // product_id不为空时入库
        List<Record> transferOrderItem = Db.find("select * from transfer_order_item where order_id='" + departTransferOrder.get("order_id")
                + "'");
        TransferOrder tOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));

        InventoryItem item = null;
        if (transferOrderItem.size() > 0) {
            for (int i = 0; i < transferOrderItem.size(); i++) {
                if (transferOrderItem.get(i).get("product_id") != null) {
                    item = new InventoryItem();
                    String in_item_check_sql = "select * from inventory_item where product_id="
                            + Integer.parseInt(transferOrderItem.get(i).get("product_id").toString()) + "" + " and warehouse_id="
                            + Integer.parseInt(tOrder.get("warehouse_id").toString()) + "";
                    InventoryItem inventoryItem = InventoryItem.dao.findFirst(in_item_check_sql);
                    if (inventoryItem == null) {
                        item.set("party_id", tOrder.get("customer_id"));
                        item.set("warehouse_id", tOrder.get("warehouse_id"));
                        item.set("product_id", transferOrderItem.get(i).get("product_id"));
                        item.set("total_quantity", transferOrderItem.get(i).get("amount"));
                        item.save();
                    } else {
                        item = InventoryItem.dao.findById(inventoryItem.get("id"));
                        item.set(
                                "total_quantity",
                                Double.parseDouble(item.get("total_quantity").toString())
                                        + Double.parseDouble(transferOrderItem.get(i).get("amount").toString()));
                        item.update();
                    }
                }
            }
        }
    }

    // 添加额的外运输单
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
            departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
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
    public void addNewRow() {
    	List<FinItem> items = new ArrayList<FinItem>();
        String pickupOrderId = getPara();
        FinItem item = FinItem.dao.findFirst("select * from fin_item where type = '应付' order by id asc");
        if(item != null){
	        PickupOrderFinItem finItem = new PickupOrderFinItem();
	        finItem.set("status", "新建").set("pickup_order_id", pickupOrderId).set("fin_item_id", item.get("id"));
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
    		pFinItem.set("status", "新建").set("pickup_order_id", pickupOrderId).set("fin_item_id", item.get("id"));
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
        		transferOrderFinTtem.set("order_id", list.get(i).get("order_id"));
        		transferOrderFinTtem.set("fin_item_id", item.get("id"));
        		transferOrderFinTtem.set("depart_order_fin_item_id", departOrderFinItemids.get(0).get("id"));
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

        FinItem fItem = FinItem.dao.findById(dFinItem.get("fin_item_id"));

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
        	departOrderFinItem.set("fin_item_id", finItem.get("id"));
        	departOrderFinItem.set("amount", avg);
        	departOrderFinItem.set("create_date", new Date());
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
	    	departOrderFinItem.set("creator", users.get(0).get("id"));
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
         	DepartOrderFinItem departOrderFinItem = DepartOrderFinItem.dao.findById(transferOrderFinItem.get("depart_order_fin_item_id"));
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
            		+ " (select count(0) total from transfer_order_item_detail where item_id = toi.id  and pickup_id is null) atmamount,toi.pickup_number,toi.amount,"
    				+ " ifnull(p.unit, toi.unit) unit, round(toi.volume ,2) sum_volume, round(toi.sum_weight ,2) sum_weight, toi.remark from transfer_order_item toi"
            		+ " left join transfer_order tor on tor.id = toi.order_id"
            		+ " left join product p on p.id = toi.product_id"
            		+ " where toi.order_id = " +transferOrderId + " order by toi.id ";
            
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
    	String transferOrderItemId = getPara("item_id");
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
    		String totalWhere = "select count(0) total from transfer_order_item_detail where item_id = '" + transferOrderItemId + "'  and pickup_id is null";
            String sql = "select id,serial_no,pieces from transfer_order_item_detail where item_id = '" + transferOrderItemId + "'  and pickup_id is null";
            
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
    	Record serialNoList = null;
    	if(orderId != ""){
    		String sql = "select group_concat(cast(id as char) separator ',') id,group_concat(serial_no separator ' ') serial_no from transfer_order_item_detail where pickup_id is null and order_id = '" + orderId + "';";
    		serialNoList = Db.findFirst(sql);
    		logger.debug("serialNoList:" + serialNoList);
    	}
    	renderJson(serialNoList);
    }
    //按运输单查找所有普货货品数量
    public void findNumberByOrderId(){
    	String orderId = getPara("order_id");
    	Record serialNoList = null;
    	if(orderId != ""){
    		String sql = "select group_concat(cast(id as char) separator ',') ids,group_concat(cast(amount as char) separator ',') amounts,group_concat(cast(ifnull(pickup_number,0) as char) separator ',') pickup_numbers from transfer_order_item where order_id = '" + orderId + "';";
    		serialNoList = Db.findFirst(sql);
    		logger.debug("serialNoList:" + serialNoList);
    	}
    	renderJson(serialNoList);
    }
    //计算单品体积、重量
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
			if(item.get("sum_weight") != null && !"".equals(item.get("sum_weight")) && !"".equals(itemNumbers[j]) && item.get("amount") != null && !"".equals(item.get("amount"))){
				sumWeight += item.getDouble("sum_weight") * (Double.parseDouble(itemNumbers[j]) / item.getDouble("amount"));
			}
			if(item.get("volume") != null && !"".equals(item.get("volume")) && !"".equals(itemNumbers[j]) && item.get("amount") != null && !"".equals(item.get("amount"))){
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
    	if("arrangementOrder".equals(transferOrder.get("order_type"))){
    		is_subtract = true;
    	}else{
    		if("deliveryToFachtoryFromWarehouse".equals(transferOrder.get("arrival_mode"))){
    			is_subtract = true;
    		}
    	}
		if(is_subtract){
			 List<TransferOrderItem> list =  TransferOrderItem.dao.find("select * from transfer_order_item where order_id = ? ",transferOrder.get("id"));
			for (TransferOrderItem transferOrderItem : list) {
				if(transferOrderItem.getLong("product_id") != null && transferOrderItem.getLong("product_id") != 0 ){
					InventoryItem ii = InventoryItem.dao.findFirst("select * from inventory_item where party_id =? and warehouse_id = ? and product_id = ?",transferOrder.get("customer_id"),transferOrder.get("from_warehouse_id"),transferOrderItem.get("product_id"));
					TransferOrderItemDetail toid = TransferOrderItemDetail.dao.findFirst("select count(*) as amount from transfer_order_item_detail where item_id = ? and pickup_id = ? ",transferOrderItem.get("id"),pickupOrder.get("id"));
					Double total_quantity = ii.getDouble("total_quantity") ;
					if(total_quantity - toid.getLong("amount") >= 0 ){
							ii.set("total_quantity", total_quantity - toid.getLong("amount"));
			    				ii.update();
			    	}
				}
			}
		}
    }
    
}
