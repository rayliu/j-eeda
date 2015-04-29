package controllers.yh.departOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartOrderFinItem;
import models.DepartPickupOrder;
import models.DepartTransferOrder;
import models.FinItem;
import models.InventoryItem;
import models.Location;
import models.Party;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.contract.Contract;
import models.yh.profile.Carinfo;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;
import controllers.yh.util.getCustomFile;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class DepartOrderController extends Controller {

    private Logger logger = Logger.getLogger(DepartOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_LIST})
    public void index() {
            render("/yh/departOrder/departOrderList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_OT_LIST})
    public void onTrip() {
         render("/yh/departOrder/departOrderOnTripList.html");
    }
    //发车单在途供应商
    public void companyNameList(){
    	String inputStr = getPara("input");
    	String sql = "select distinct  c.abbr as company  from contact c where c.abbr  is not null "
    			+ "and c.abbr like '%"+ inputStr+ "%'";
    	List<Record> companyNameList = Db.find(sql);
    	renderJson(companyNameList);
    }
    //运输单外包供应商
    public void cpnameList(){
    	String sp_filter = getPara("sp_filter");
    	String sql = "select distinct c.abbr company from contact c where c.abbr is not null and ifnull(c.abbr,'') like '%"+sp_filter+"%'";
    	List<Record> companyNameList = Db.find(sql);
    	renderJson(companyNameList);
    }
    //运输单外包客户(有问题)
    public void customerList(){
    	String customer_filter = getPara("customer_filter");
    	String sql = "select distinct c1.abbr customer from transfer_order t "
                + " left join party p1 on t.customer_id = p1.id "
                + " left join party p2 on t.sp_id = p2.id "
                + " left join contact c1 on p1.contact_id = c1.id"
                + " left join contact c2 on p2.contact_id = c2.id"
                + " left join office o on t.office_id = o.id "
                + " where c1.abbr is not null "
                + " and operation_type ='out_source' "
                + " and c1.abbr like '%"+customer_filter+"%'";
               
    	List<Record> companyNameList = Db.find(sql);
    	renderJson(companyNameList);
    }
    //运输单外包网点
    public void officenameList(){
    	String officeName_filter = getPara("officeName_filter");
    	String sql = "select distinct o.office_name officeName  from office o "
    			+ "where o.office_name is not null "
    			+ "and o.office_name like '%"+officeName_filter+"%'";
    	List<Record> companyNameList = Db.find(sql);
    	renderJson(companyNameList);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_LIST})
    public void list() {
        String orderNo = getPara("orderNo");
        String departNo = getPara("departNo");
        String status = getPara("status");
        String sp = getPara("sp");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String office = getPara("office");
        String start = getPara("start");
        String destination = getPara("destination");

        
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        String sql = "";
        String sqlTotal = "";
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        if (orderNo == null && departNo == null && status == null && sp == null && beginTime == null && endTime == null
        		&& office == null && start == null&& destination == null ) {
            sqlTotal = "select count(1) total from depart_order deo "
                    + "left join carinfo  car on deo.driver_id=car.id"
                    + " left join depart_transfer dtf on dtf.depart_id = deo.id"
					+ " left join transfer_order tor on tor .id = dtf.order_id"
					+ " left join user_login u on u.id = tor .create_by"
					+ " left join office o on o.id = tor.office_id"
            		+ " where combine_type = '"
                    + DepartOrder.COMBINE_TYPE_DEPART + "'and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";

            sql = "select deo.id,deo.depart_no,deo.create_stamp,deo.status as depart_status,deo.arrival_time arrival_time,deo.remark remark,ifnull(deo.driver, c.driver) contact_person,ifnull(deo.phone, c.phone) phone,c.car_no,c.cartype,c.length,"
            		+ " ifnull(nullif(u.c_name,''),u.user_name) user_name,o.office_name office_name,deo.departure_time departure_time,ct.abbr abbr,"
            		+ " (select name from location where code = deo.route_from) route_from,(select name from location where code = deo.route_to) route_to,"
            		+ " (select group_concat(tr.order_no separator '\r\n') from transfer_order tr where tr.id in (select order_id from depart_transfer dt where dt.depart_id = deo.id)) as transfer_order_no, "
            		+ " deo.transfer_type as trip_type"
            		+ " from depart_order deo"
					+ " left join carinfo c on deo.carinfo_id = c.id"
					+ " left join party p on deo.sp_id = p.id"
					+ " left join contact ct on p.contact_id = ct.id"
					+ " left join depart_transfer dtf on dtf.depart_id = deo.id"
					+ " left join transfer_order tor on tor .id = dtf.order_id"
					+ " left join user_login u on u.id = tor .create_by"
					+ " left join office o on o.id = tor.office_id"
					+ " where ifnull(deo.status, '') != ''"
					+ " and combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"'"
					+ " and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
					+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') group by deo.id,o.office_name "
					+ " order by deo.status = '已收货',deo.status = '已入库',deo.status != '已收货',deo.status != '已入库',deo.status != '新建',deo.status != '已发车',deo.status != '在途',deo.status = '在途',deo.status = '已发车',deo.status='新建', deo.create_stamp desc " + sLimit;
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }
            String whereSql = "  where deo.combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"' "
                    + " and ifnull(deo.status,'') like '%" + status
                    + "%' and ifnull(deo.depart_no,'') like '%" + departNo
                    + "%' and ifnull(tor.order_no,'') like '%" + orderNo
                    + "%' and ifnull(ct.abbr,'')  like '%"+ sp
                    + "%' and ifnull(o.office_name,'') like '%"+ office
                    + "%' and ifnull(l1.name,'') like '%"+ start
                    + "%' and ifnull(l2.name,'') like '%"+ destination
                    + "%' and deo.create_stamp between '" + beginTime + "' and '" + endTime +"'"
                    + " and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
            sqlTotal = "select count(1) total from depart_order deo"
					+ " left join carinfo c on deo.carinfo_id = c.id"
					+ " left join party p on deo.sp_id = p.id"
					+ " left join contact ct on p.contact_id = ct.id"
					+ " left join depart_transfer dtf on dtf.depart_id = deo.id"
					+ " left join transfer_order tor on tor .id = dtf.order_id"
					+ " left join user_login u on u.id = tor .create_by"
					+ " left join office o on o.id = tor .office_id"
					+ " left join location l1 on l1.code = deo.route_from"
					+ " left join location l2 on l2.code = deo.route_to " + whereSql;

            sql = "select deo.id,deo.depart_no,deo.create_stamp,deo. status as depart_status,deo.arrival_time arrival_time,deo.remark remark,ifnull(deo.driver, c.driver) contact_person,ifnull(deo.phone, c.phone) phone,c.car_no,c.cartype,c.length,ifnull(nullif(u.c_name,''),u.user_name) user_name,"
            		+ " o.office_name office_name,deo.departure_time departure_time ,ct.abbr abbr,"
            		+ " (select name from location where code = deo.route_from) route_from,(select name from location where code = deo.route_to) route_to,"
            		+ " (select group_concat(tr.order_no separator '\r\n') from transfer_order tr where tr.id in (dtf.order_id)) as transfer_order_no , "
            		+ " deo.transfer_type as trip_type"
            		+ " from depart_order deo"
					+ " left join carinfo c on deo.carinfo_id = c.id"
					+ " left join party p on deo.sp_id = p.id"
					+ " left join contact ct on p.contact_id = ct.id"
					+ " left join depart_transfer dtf on dtf.depart_id = deo.id"
					+ " left join transfer_order tor on tor.id = dtf.order_id"
					+ " left join user_login u on u.id = tor.create_by"
					+ " left join office o on o.id = tor.office_id"
					+ " left join location l1 on l1.code = deo.route_from"
					+ " left join location l2 on l2.code = deo.route_to " + whereSql
                    + " group by deo.status = '已收货',deo.status = '已入库',deo.status != '已收货',deo.status != '已入库',deo.status != '新建',deo.status != '已发车',deo.status != '在途',deo.status = '在途',deo.status = '已发车',deo.status='新建', deo.create_stamp desc " + sLimit;
        }
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> departOrders = Db.find(sql);

        Map map = new HashMap();
        map.put("sEcho", pageIndex);
        map.put("iTotalRecords", rec.getLong("total"));
        map.put("iTotalDisplayRecords", rec.getLong("total"));
        map.put("aaData", departOrders);
        renderJson(map);
    }
    
    // 发车单在途列表
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_OT_LIST})
    public void onTripList() {
    	String orderNo = getPara("orderNo");
    	String departNo = getPara("departNo");
    	String status = getPara("status");
    	String sp = getPara("sp");
    	String beginTime = getPara("beginTime");
    	String endTime = getPara("endTime");
    	String sLimit = "";
    	String pageIndex = getPara("sEcho");
    	String sql = "";
    	String sqlTotal = "";
    	
    	String office =getPara("office");
    	String start =getPara("start");
    	String end =getPara("end");
    	String customer =getPara("customer");
    	
    	if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    		sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    	}
    	if (orderNo == null && departNo == null && status == null 
    			&& sp == null && beginTime == null && endTime == null
    			&& office == null && start == null && end == null
    			&& customer == null) {
    		sqlTotal = "select count(distinct deo.id) total from depart_order deo "
    				+ " left join carinfo c on deo.carinfo_id = c.id "
    				+ " left join depart_transfer dt on dt.depart_id = deo.id "
    				+ " left join transfer_order t on t.id = dt.order_id "
    				+ " left join office o on o.id = t.office_id "
    				+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on deo.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join location l1 on deo.route_from = l1.code "
					+ " left join location l2 on deo.route_to =l2.code "
    				+ " where combine_type = '"+ DepartOrder.COMBINE_TYPE_DEPART 
    				+ "' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
    				+ " and t.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
    		
    		sql = "select deo.id,deo.depart_no ,deo.charge_type,deo.create_stamp ,deo.status as depart_status,c2.contact_person driver,c2.phone,"
    				+ " c1.abbr cname,c2.abbr spname,o.office_name office_name, l1.name route_from,l2.name route_to, t.arrival_mode arrival_mode,"
    				+ " deo.arrival_time plan_time, t.arrival_time arrival_time, deo.remark, "
                    + " ((select ifnull(sum(amount),0) from transfer_order_item where order_id in (select order_id from depart_transfer dtp left join transfer_order tor on tor.id = dtp.order_id where dtp.depart_id = deo.id and tor.cargo_nature = 'cargo')) "
                    + " +(select count(0) from transfer_order_item_detail where order_id in (select order_id from depart_transfer dtp left join transfer_order tor on tor.id = dtp.order_id where dtp.depart_id = deo.id and tor.cargo_nature = 'ATM') and depart_id = deo.id)) amount, "
    				+ " (select tr.arrival_mode from transfer_order tr where tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id) group by tr.arrival_mode) arrival_mode, "
    				+ " (select group_concat(tr.order_no separator '\r\n') from transfer_order tr where tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id ))  as transfer_order_no, "		
    				+ " (select location from transfer_order_milestone tom where depart_id = deo.id order by id desc limit 0,1) location, "
    				+ " (select ifnull(exception_record,'') from transfer_order_milestone tom where depart_id = deo.id order by id desc limit 0,1) exception_record, "
    				+ " (select dt.order_id from depart_transfer dt where dt.depart_id = deo.id limit 0,1) order_id,  "
    				+ " deo.transfer_type as trip_type"
    				+ " from depart_order deo "
    				+ " left join carinfo c on deo.carinfo_id = c.id "
    				+ " left join depart_transfer dt on dt.depart_id = deo.id "
    				+ " left join transfer_order t on t.id = dt.order_id "
    				+ " left join office o on o.id = t.office_id "
    				+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on deo.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join location l1 on deo.route_from = l1.code "
					+ " left join location l2 on deo.route_to =l2.code "
    				+ " where  ifnull(deo.status,'') != 'aa'  and combine_type = '"
    				+ DepartOrder.COMBINE_TYPE_DEPART + "'  and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
    				+ " and t.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
    				+ " group by deo.id order by deo.create_stamp desc " + sLimit;
    	} else {
    		if (beginTime == null || "".equals(beginTime)) {
    			beginTime = "1-1-1";
    		}
    		if (endTime == null || "".equals(endTime)) {
    			endTime = "9999-12-31";
    		}
    		sqlTotal = "select count(distinct deo.id) total"
    				+ " from depart_order deo "
    				+ " left join carinfo c on deo.carinfo_id = c.id "
    				+ " left join depart_transfer dt on dt.depart_id = deo.id "
    				+ " left join transfer_order t on t.id = dt.order_id "
    				+ " left join office o on o.id = t.office_id "
    				+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on deo.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join location l1 on deo.route_from = l1.code "
					+ " left join location l2 on deo.route_to =l2.code "
    				+ " left join transfer_order tr  on tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id )"
    				+ "  where deo.combine_type = 'DEPART' and "
    				+ "ifnull(deo.status,'') like '%" + status + "%' and "
    				+ "ifnull(deo.depart_no,'') like '%" + departNo 
    				+ "%' and ifnull(c2.abbr,'')  like '%"+ sp 
    				+ "%' and ifnull(tr.order_no,'') like '%" + orderNo 
    				+ "%' and ifnull(o.office_name,'') like '%" + office 
    				+ "%' and ifnull(l1.name,'') like '%" + start 
    				+ "%' and ifnull(l2.name,'') like '%" + end 
    				+ "%' and ifnull(c1.abbr,'') like '%" + customer  
    				+ "%' and deo.create_stamp between '" + beginTime + "' " + "and '" + endTime +"'  and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
    				+ " and tr.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')" + sLimit;
    		
    		sql = "select deo.id,"
    				+ "deo.depart_no ,"
    				+ "deo.charge_type,"
    				+ "deo.create_stamp ,"
    				+ "deo.status as depart_status,"
    				+ "c2.contact_person driver,"
    				+ "c2.phone,"
    				+ "c1.abbr cname,"
    				+ "c2.abbr spname,"
    				+ "o.office_name office_name, "
    				+ "l1.name route_from, "
    				+ "l2.name route_to, "
    				+ "t.arrival_mode arrival_mode, "
    				+ "deo.arrival_time plan_time, "
    				+ "t.arrival_time arrival_time, "
    				+ "deo.remark, "
    				+ "(select sum(amount) from transfer_order_item where order_id in (select order_id from depart_transfer dtp where dtp.depart_id=deo.id )) amount, "
    				+ "(select tr.arrival_mode from transfer_order tr where tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id) group by tr.arrival_mode) arrival_mode, "
    				+ "(select group_concat(tr.order_no separator '\r\n') from transfer_order tr where tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id ))  as transfer_order_no, "		
    				+ "(select location from transfer_order_milestone tom where depart_id = deo.id order by id desc limit 0,1) location, "
    				+ "(select ifnull(exception_record,'') from transfer_order_milestone tom where depart_id = deo.id order by id desc limit 0,1) exception_record, "
    				+ " (select dt.order_id from depart_transfer dt where dt.depart_id = deo.id limit 0,1) order_id,  "
    				+ " deo.transfer_type as trip_type"
    				+ " from depart_order deo "
    				+ " left join carinfo c on deo.carinfo_id = c.id "
    				+ " left join depart_transfer dt on dt.depart_id = deo.id "
    				+ " left join transfer_order t on t.id = dt.order_id "
    				+ " left join office o on o.id = t.office_id "
    				+ " left join party p1 on t.customer_id = p1.id "
					+ " left join party p2 on deo.sp_id = p2.id "
					+ " left join contact c1 on p1.contact_id = c1.id "
					+ " left join contact c2 on p2.contact_id = c2.id "
					+ " left join location l1 on deo.route_from = l1.code "
					+ " left join location l2 on deo.route_to =l2.code "
    				+ " left join transfer_order tr  on tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id )"
    				+ "  where deo.combine_type = 'DEPART' "
    				+ " and ifnull(deo.status,'') like '%"+ status
    				+ "%' and ifnull(deo.depart_no,'') like '%"+ departNo
    				+ "%' and ifnull(tr.order_no,'') like '%"+ orderNo
    				+ "%' and ifnull(o.office_name,'') like '%" + office 
    				+ "%' and ifnull(l1.name,'') like '%" + start 
    				+ "%' and ifnull(l2.name,'') like '%" + end 
    				+ "%' and ifnull(c2.abbr,'') like '%" +sp
    				+ "%' and ifnull(c1.abbr,'') like '%" + customer 
    				+ "%' and deo.create_stamp between '"+ beginTime+ "' and '"+ endTime
    				+ "'  and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
    				+ "  and tr.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
    				+ " group by deo.id order by deo.create_stamp desc " + sLimit;
    	}
    	Record rec = Db.findFirst(sqlTotal);
    	logger.debug("total records:" + rec.getLong("total"));
    	List<Record> departOrders = Db.find(sql);
    	
    	Map map = new HashMap();
    	map.put("sEcho", pageIndex);
    	map.put("iTotalRecords", rec.getLong("total"));
    	map.put("iTotalDisplayRecords", rec.getLong("total"));
    	map.put("aaData", departOrders);
    	renderJson(map);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_CREATE})
    public void add() {
    	
    	Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
    	setAttr("customizeField", customizeField);
        render("/yh/departOrder/allTransferOrderList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_CREATE})
    public void addForRouteSp() {
    	
    	Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
    	setAttr("customizeField", customizeField);
    	render("/yh/departOrder/allTransferOrderListForRouteSp.html");
    }

    // 修改发车单页面
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_UPDATE})
    public void edit() {
        String sql = "select do.*,co.contact_person,co.phone,u.user_name,(select group_concat(dt.order_id  separator',')  from depart_transfer  dt "
                + "where dt.depart_id =do.id)as order_id from depart_order  do "
                + "left join contact co on co.id in( select p.contact_id  from party p where p.id=do.driver_id ) "
                + "left join user_login  u on u.id=do.create_by where do.combine_type ='"
                + DepartOrder.COMBINE_TYPE_DEPART + "' and do.id in(" + getPara("id") + ")";
        DepartOrder departOrder = DepartOrder.dao.findFirst(sql);
        setAttr("departOrder", departOrder);

        DepartTransferOrder departTransferOrder2 = DepartTransferOrder.dao.findFirst(
                "select * from depart_transfer where depart_id = ? order by id desc", departOrder.get("id"));

        TransferOrder transferOrderAttr = TransferOrder.dao.findById(departTransferOrder2.get("order_id"));
        setAttr("transferOrderAttr", transferOrderAttr);

        Long sp_id = departOrder.get("sp_id");
        if (sp_id != null) {
            Party sp = Party.dao.findById(sp_id);
            Contact spContact = Contact.dao.findById(sp.get("contact_id"));
            setAttr("spContact", spContact);
        }
        Long driverId = departOrder.get("driver_id");
        if (driverId != null) {
            Party driver = Party.dao.findById(driverId);
            Contact driverContact = Contact.dao.findById(driver.get("contact_id"));
            setAttr("driverContact", driverContact);
        }
        Long carinfoId = departOrder.get("carinfo_id");
        if (carinfoId != null) {
            Carinfo carinfo = Carinfo.dao.findById(carinfoId);
            setAttr("carinfo", carinfo);
        }
        UserLogin userLogin = UserLogin.dao.findById(departOrder.get("create_by"));
        setAttr("userLogin2", userLogin);
        setAttr("depart_id", getPara());
        String orderId = "";
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                "select * from depart_transfer where depart_id = ?", departOrder.get("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            orderId += departTransferOrder.get("order_id") + ",";
        }
        orderId = orderId.substring(0, orderId.length() - 1);
        setAttr("localArr", orderId);

        String routeFrom = departOrder.get("route_from");
        Location locationFrom = null;
        if (routeFrom != null || !"".equals(routeFrom)) {
            List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
            Location l = Location.dao
                    .findFirst("select * from location where code = (select pcode from location where code = '"
                            + routeFrom + "')");
            if (provinces.contains(l)) {
                locationFrom = Location.dao
                        .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                + routeFrom + "'");
            } else {
                locationFrom = Location.dao
                        .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                + routeFrom + "'");
            }
            setAttr("locationFrom", locationFrom);
        }

        String routeTo = departOrder.get("route_to");
        Location locationTo = null;
        if (routeTo != null || !"".equals(routeTo)) {
            List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
            Location l = Location.dao
                    .findFirst("select * from location where code = (select pcode from location where code = '"
                            + routeTo + "')");
            if (provinces.contains(l)) {
                locationTo = Location.dao
                        .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                + routeTo + "'");
            } else {
                locationTo = Location.dao
                        .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                + routeTo + "'");
            }
            setAttr("locationTo", locationTo);
        }

        TransferOrderMilestone transferOrderMilestone = TransferOrderMilestone.dao.findFirst(
                "select * from transfer_order_milestone where pickup_id = ? order by create_stamp desc",
                departOrder.get("id"));
        setAttr("transferOrderMilestone", transferOrderMilestone);
        List<Record> paymentItemList = Collections.EMPTY_LIST;
        paymentItemList = Db.find("select * from fin_item where type='应付'");
        setAttr("paymentItemList", paymentItemList);
            render("/yh/departOrder/editDepartOrder.html");
    }

    // 创建发车单的运输单列表
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_CREATE})
    public void createTransferOrderList() {
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String address = getPara("address");
        String customer = getPara("customer");
        String routeFrom = getPara("routeFrom");
        String routeTo = getPara("routeTo");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        Record rec = null;
        String sLimit = "";
        String sql = "";
        String sqlTotal = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        if (orderNo == null && status == null && address == null && customer == null && routeFrom == null
                && routeTo == null && beginTime == null && endTime == null) {
            sqlTotal = "select count(1) total  from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code"
                    + " left join office o on o.id = tor.office_id "
                    + " where ((tor.status = '已入货场' or (tor.cargo_nature = 'ATM' and tor.status = '部分已入货场')) "
                    + " or (tor.operation_type = 'out_source' and tor.status = '新建') "
                    + " or (((tor.cargo_nature = 'ATM' and tor.status = '部分已入货场') or tor.status = '已投保') and (select group_concat(cast(dt.pickup_id AS CHAR) SEPARATOR ',') pickup_id from depart_transfer dt "
                    + "  left join depart_pickup dp on dp.order_id = dt.order_id left join depart_order dord on dord.id = dt.pickup_id where dt.order_id = tor.id and dord.status = '已入货场' and dt.pickup_id not in (select pickup_id from depart_pickup where order_id = tor.id) ) is not null)"
                    + " or (tor.cargo_nature = 'cargo' and tor.pickup_assign_status = 'ALL' and (select group_concat(cast(dorr.id as char) separator ',') pickup_id from depart_transfer dt left join depart_order dorr on dorr.id = dt.pickup_id where dt.order_id = tor.id and dt.depart_id is null and dorr.status != '已入货场') is null)) "
                    + " and ifnull(tor.depart_assign_status, '') !='"+ TransferOrder.ASSIGN_STATUS_ALL + "' "
            		+ " and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
          
            sql = "select distinct tor.id,tor.order_no,tor.planning_time,tor.operation_type,tor.cargo_nature, tor.arrival_mode ,"
            		+ " round((select sum(ifnull(toi.volume,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_volume, "
                    + " round((select sum(ifnull(toi.sum_weight,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_weight, "
                    + " (select group_concat( distinct cast(dt.pickup_id as char) separator ',' ) from depart_transfer dt left join depart_pickup dp on dp.pickup_id = dt.pickup_id "
                    + " left join depart_order dd on dd.id = dt.pickup_id where dt.order_id = tor.id and (dd.status='已入货场' or dd.status='已入库') and (select group_concat(cast(pickup_id as char) separator ',') from depart_pickup where order_id = tor.id and pickup_id = dt.pickup_id) is null) pickup_id," 
                    + " (select group_concat( distinct dor.depart_no separator '\r\n' ) from depart_transfer dt left join depart_order dor on dor.id = dt.pickup_id left join depart_pickup dp on dp.pickup_id = dt.pickup_id "
                    + " where dt.order_id = tor.id and (dor.status='已入货场' or dor.status='已入库') and (select group_concat(cast(pickup_id as char) separator ',') from depart_pickup where order_id = tor.id and pickup_id = dt.pickup_id) is null ) pickup_no,"
                    + " (select sum(toi.amount) from transfer_order_item toi where toi.order_id = tor.id) as total_amount,"
                    + " tor.charge_type2,ifnull(dor.address, '') doaddress, ifnull(tor.pickup_mode, '') pickup_mode,tor.status,c.abbr cname,"
                    + " l1.name route_from,l2.name route_to,tor.create_stamp ,cont.abbr as spname,cont.id as spid,"
                    + " o.office_name office_name "
                    + " from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id left join contact cont on  cont.id=p2.contact_id "
                    + " left join depart_transfer dt on tor.id = dt.order_id left join depart_order dor on dor.id = dt.depart_id "
                    + " left join depart_pickup dp on dp.pickup_id = dt.pickup_id"
                    + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code "
                    + " left join office o on o.id=tor.office_id "
                    + " where ((tor.status = '已入货场' or (tor.cargo_nature = 'ATM' and tor.status = '部分已入货场')) "
                    + " or (tor.operation_type = 'out_source' and tor.status = '新建') "
                    + " or (((tor.cargo_nature = 'ATM' and tor.status = '部分已入货场') or tor.status = '已投保') and (select group_concat(cast(dt.pickup_id AS CHAR) SEPARATOR ',') pickup_id from depart_transfer dt "
                    + "  left join depart_pickup dp on dp.order_id = dt.order_id left join depart_order dord on dord.id = dt.pickup_id where dt.order_id = tor.id and dord.status = '已入货场' and dt.pickup_id not in (select pickup_id from depart_pickup where order_id = tor.id) ) is not null)"
                    + " or (tor.cargo_nature = 'cargo' and tor.pickup_assign_status = 'ALL' and (select group_concat(cast(dorr.id as char) separator ',') pickup_id from depart_transfer dt left join depart_order dorr on dorr.id = dt.pickup_id where dt.order_id = tor.id and dt.depart_id is null and dorr.status != '已入货场') is null)) "
                    + " and ifnull(tor.depart_assign_status, '') !='"+ TransferOrder.ASSIGN_STATUS_ALL + "' "
            		+ " and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
                    + " order by tor.planning_time desc " + sLimit;
        } else {
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
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " left join office o on o.id = tor.office_id "
                    + " where ((tor.status = '已入货场' or (tor.cargo_nature = 'ATM' and tor.status = '部分已入货场')) "
                    + " or (tor.operation_type = 'out_source' and tor.status = '新建') "
                    + " or (((tor.cargo_nature = 'ATM' and tor.status = '部分已入货场') or tor.status = '已投保') and (select group_concat(cast(dt.pickup_id AS CHAR) SEPARATOR ',') pickup_id from depart_transfer dt "
                    + "  left join depart_pickup dp on dp.order_id = dt.order_id left join depart_order dord on dord.id = dt.pickup_id where dt.order_id = tor.id and dord.status = '已入货场' and dt.pickup_id not in (select pickup_id from depart_pickup where order_id = tor.id) ) is not null)"
                    + " or (tor.cargo_nature = 'cargo' and tor.pickup_assign_status = 'ALL' and (select group_concat(cast(dorr.id as char) separator ',') pickup_id from depart_transfer dt left join depart_order dorr on dorr.id = dt.pickup_id where dt.order_id = tor.id and dt.depart_id is null and dorr.status != '已入货场') is null)) "
                    + " and ifnull(tor.depart_assign_status, '') !='"+ TransferOrder.ASSIGN_STATUS_ALL + "' "
            		+ " and tor.order_no like '%" + orderNo+ "%' "
    				+ " and tor.status like '%" + status + "%' "
					+ " and tor.address like '%" + address+ "%' "
					+ " and c.abbr like '%" + customer + "%' "
					+ " and l1.name like '%" + routeFrom + "%' "
					+ " and l2.name like '%" + routeTo + "%' "
					+ " and tor.planning_time between '" + beginTime+ "' and '" + endTime + "'"
					+ " and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";

            sql = "select distinct tor.id,tor.order_no,tor.planning_time,tor.operation_type,tor.cargo_nature, tor.arrival_mode ,"
            		+ " round((select sum(ifnull(toi.volume,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_volume, "
                    + " round((select sum(ifnull(toi.sum_weight,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_weight, "
                    + " (select group_concat( distinct cast(dt.pickup_id as char) separator ',' ) from depart_transfer dt left join depart_pickup dp on dp.pickup_id = dt.pickup_id "
                    + " left join depart_order dd on dd.id = dt.pickup_id where dt.order_id = tor.id and (dd.status='已入货场' or dd.status='已入库') and (select group_concat(cast(pickup_id as char) separator ',') from depart_pickup where order_id = tor.id and pickup_id = dt.pickup_id) is null ) pickup_id," 
                    + " (select group_concat( distinct dor.depart_no separator '\r\n' ) from depart_transfer dt left join depart_order dor on dor.id = dt.pickup_id left join depart_pickup dp on dp.pickup_id = dt.pickup_id "
                    + " where dt.order_id = tor.id and (dor.status='已入货场' or dor.status='已入库') and (select group_concat(cast(pickup_id as char) separator ',') from depart_pickup where order_id = tor.id and pickup_id = dt.pickup_id) is null ) pickup_no,"
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.charge_type2,dor.address doaddress,tor.pickup_mode,tor.status,c.abbr cname,"
                    + " (select name from location where code = tor.route_from) route_from,(select name from location where code = tor.route_to) route_to,tor.create_stamp,tor.depart_assign_status,c2.abbr spname, "
                    + " o.office_name office_name "
                    + " from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join party p2 on tor.sp_id = p2.id  left join contact c2 on p2.contact_id = c2.id "
                    + " left join depart_transfer dt on tor.id = dt.order_id left join depart_order dor on dor.id = dt.depart_id "
                    + " left join depart_pickup dp on dp.pickup_id = dt.pickup_id"
                    + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " left join office o on o.id = tor.office_id "
                    + " where ((tor.status = '已入货场' or (tor.cargo_nature = 'ATM' and tor.status = '部分已入货场')) "
                    + " or (tor.operation_type = 'out_source' and tor.status = '新建') "
                    + " or (((tor.cargo_nature = 'ATM' and tor.status = '部分已入货场') or tor.status = '已投保') and (select group_concat(cast(dt.pickup_id AS CHAR) SEPARATOR ',') pickup_id from depart_transfer dt "
                    + "  left join depart_pickup dp on dp.order_id = dt.order_id left join depart_order dord on dord.id = dt.pickup_id where dt.order_id = tor.id and dord.status = '已入货场' and dt.pickup_id not in (select pickup_id from depart_pickup where order_id = tor.id) ) is not null)"
                    + " or (tor.cargo_nature = 'cargo' and tor.pickup_assign_status = 'ALL' and (select group_concat(cast(dorr.id as char) separator ',') pickup_id from depart_transfer dt left join depart_order dorr on dorr.id = dt.pickup_id where dt.order_id = tor.id and dt.depart_id is null and dorr.status != '已入货场') is null)) "
                    + " and ifnull(tor.depart_assign_status, '') !='"+ TransferOrder.ASSIGN_STATUS_ALL + "' "
            		+ " and tor.order_no like '%" + orderNo+ "%' "
    				+ " and tor.status like '%" + status + "%' "
					+ " and tor.address like '%" + address+ "%' "
					+ " and c.abbr like '%" + customer + "%' "
					+ " and l1.name like '%" + routeFrom + "%' "
					+ " and l2.name like '%" + routeTo + "%' "
					+ " and tor.planning_time between '" + beginTime+ "' and '" + endTime + "'"
					+ " and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
                    + " order by tor.planning_time desc " + sLimit;
        }
        rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> transferOrders = Db.find(sql);

        Map transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);
        renderJson(transferOrderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_CREATE})
    // 创建发车单的运输单列表(干线供应商+整车)
    public void createTransferOrderListForRouteSp() {
    	String orderNo = getPara("orderNo");
    	String status = getPara("status");
    	String address = getPara("address");
    	String customer = getPara("customer");
    	String routeFrom = getPara("routeFrom");
    	String routeTo = getPara("routeTo");
    	String beginTime = getPara("beginTime");
    	String endTime = getPara("endTime");
    	Record rec = null;
    	String sLimit = "";
    	String sql = "";
    	String sqlTotal = "";
    	String pageIndex = getPara("sEcho");
    	if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    		sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    	}
    	if (orderNo == null && status == null && address == null && customer == null && routeFrom == null
    			&& routeTo == null && beginTime == null && endTime == null) {
    		sqlTotal = "select count(1) total  from transfer_order tor "
    				+ " left join party p on tor.customer_id = p.id "
    				+ " left join contact c on p.contact_id = c.id "
    				+ " left join location l1 on tor.route_from = l1.code "
    				+ " left join location l2 on tor.route_to = l2.code"
    				+ " left join office o on tor.office_id = o.id "
    				+ " where tor.status = '新建' "
    				+ " and ifnull(tor.pickup_assign_status, '') = '" + TransferOrder.ASSIGN_STATUS_NEW + "' "
					+ " and ifnull(tor.depart_assign_status, '') = '" + TransferOrder.ASSIGN_STATUS_NEW + "' "
					+ " and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
    				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
    		rec = Db.findFirst(sqlTotal);
    		logger.debug("total records:" + rec.getLong("total"));
    		sql = "select distinct tor.id,tor.order_no,tor.planning_time,tor.operation_type,tor.cargo_nature, tor.arrival_mode ,"
    				+ " round((select sum(ifnull(toi.volume,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_volume, "
                    + " round((select sum(ifnull(toi.sum_weight,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_weight, "
    				+ " (select sum(toi.amount) from transfer_order_item toi where toi.order_id = tor.id) as total_amount,"
    				+ " ifnull(dor.address, '') doaddress, ifnull(tor.pickup_mode, '') pickup_mode,tor.status,c.abbr cname,"
    				+ " l1.name route_from,l2.name route_to,tor.create_stamp ,cont.abbr as spname,cont.id as spid, "
    				+ " o.office_name office_name from transfer_order tor "
    				+ " left join party p on tor.customer_id = p.id "
    				+ " left join contact c on p.contact_id = c.id "
    				+ " left join party p2 on tor.sp_id = p2.id left join contact cont on  cont.id=p2.contact_id "
    				+ " left join depart_transfer dt on tor.id = dt.order_id left join depart_order dor on dor.id = dt.depart_id "
    				+ " left join location l1 on tor.route_from = l1.code "
    				+ " left join location l2 on tor.route_to = l2.code "
    				+ " left join office o on o.id = tor.office_id "
    				+ " where tor.status = '新建' "
    				+ " and ifnull(tor.pickup_assign_status, '') = '" + TransferOrder.ASSIGN_STATUS_NEW + "' "
					+ " and ifnull(tor.depart_assign_status, '') = '" + TransferOrder.ASSIGN_STATUS_NEW + "' "
					+ " and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
    				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
    				+ " order by tor.planning_time desc " + sLimit;
    	} else {
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
    				+ " left join location l2 on tor.route_to = l2.code  "
    				+ " left join office o on o.id = tor.office_id "
    				+ " where tor.status = '新建' "
    				+ " and ifnull(tor.pickup_assign_status, '') = '" + TransferOrder.ASSIGN_STATUS_NEW + "' "
					+ " and ifnull(tor.depart_assign_status, '') = '" + TransferOrder.ASSIGN_STATUS_NEW + "' "
					+ " and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
    				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
    				+ " and tor.order_no like '%" + orderNo + "%' "
					+ " and tor.status like '%" + status + "%' "
					+ " and tor.address like '%" + address + "%' "
					+ " and c.abbr like '%" + customer + "%' "
					+ " and l1.name like '%" + routeFrom + "%' "
					+ " and l2.name like '%" + routeTo + "%' "
					+ " and tor.planning_time between '" + beginTime + "' "
					+ " and '" + endTime + "' ";
    		
    		sql = "select distinct tor.id,tor.order_no,tor.planning_time,tor.operation_type,tor.cargo_nature, tor.arrival_mode ,"
    				+ " round((select sum(ifnull(toi.volume,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_volume, "
                    + " round((select sum(ifnull(toi.sum_weight,0)) from transfer_order_item toi where toi.order_id = tor.id),2) total_weight, "
    				+ " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
    				+ " dor.address doaddress,tor.pickup_mode,tor.status,c.abbr cname,"
    				+ " (select name from location where code = tor.route_from) route_from,(select name from location where code = tor.route_to) route_to,tor.create_stamp,tor.depart_assign_status,c2.abbr spname, "
    				+ " o.office_name office_name from transfer_order tor "
    				+ " left join party p on tor.customer_id = p.id "
    				+ " left join contact c on p.contact_id = c.id "
    				+ " left join party p2 on tor.sp_id = p2.id  left join contact c2 on p2.contact_id = c2.id "
    				+ " left join depart_transfer dt on tor.id = dt.order_id left join depart_order dor on dor.id = dt.depart_id "
    				+ " left join location l1 on tor.route_from = l1.code "
    				+ " left join location l2 on tor.route_to = l2.code "
    				+ " left join office o on o.id = tor.office_id "
    				+ " where tor.status = '新建' "
    				+ " and ifnull(tor.pickup_assign_status, '') = '" + TransferOrder.ASSIGN_STATUS_NEW + "' "
					+ " and ifnull(tor.depart_assign_status, '') = '" + TransferOrder.ASSIGN_STATUS_NEW + "' "
					+ " and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
    				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
    				+ " and tor.order_no like '%" + orderNo + "%' "
					+ " and tor.status like '%" + status + "%' "
					+ " and tor.address like '%" + address + "%' "
					+ " and c.abbr like '%" + customer + "%' "
					+ " and l1.name like '%" + routeFrom + "%' "
					+ " and l2.name like '%" + routeTo + "%' "
					+ " and tor.planning_time between '" + beginTime + "' "
					+ " and '" + endTime + "' "
    				+ " order by tor.planning_time desc " + sLimit;
    	}
    	rec = Db.findFirst(sqlTotal);
    	logger.debug("total records:" + rec.getLong("total"));
    	List<Record> transferOrders = Db.find(sql);
    	
    	Map transferOrderListMap = new HashMap();
    	transferOrderListMap.put("sEcho", pageIndex);
    	transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
    	transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
    	
    	transferOrderListMap.put("aaData", transferOrders);
    	renderJson(transferOrderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_CREATE})
    public void createDepartOrder() {
        String list = this.getPara("localArr");
        setAttr("localArr", list);
        setAttr("routeSp", getPara("routeSp"));
        setAttr("transfer_type",getPara("transfer_type"));
        setAttr("pickupIds",getPara("pickupIds"));
        String[] orderIds = list.split(",");
        int numone=0;
        for (int i = 0; i < orderIds.length; i++) {
            TransferOrder transferOrder = TransferOrder.dao.findById(orderIds[i]);
            if(numone == 0){
            	setAttr("transferOrder", transferOrder);
                setAttr("chargeType", transferOrder.get("charge_type"));
                
            	numone = 1;
            }
            String routeFrom = transferOrder.get("route_from");
            Location locationFrom = null;
            if (routeFrom != null || !"".equals(routeFrom)) {
                List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
                Location l = Location.dao
                        .findFirst("select * from location where code = (select pcode from location where code = '"
                                + routeFrom + "')");
                if (provinces.contains(l)) {
                    locationFrom = Location.dao
                            .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                    + routeFrom + "'");
                } else {
                    locationFrom = Location.dao
                            .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                    + routeFrom + "'");
                }
                setAttr("locationFrom", locationFrom);
            }

            String routeTo = transferOrder.get("route_to");
            Location locationTo = null;
            if (routeTo != null || !"".equals(routeTo)) {
                List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
                Location l = Location.dao
                        .findFirst("select * from location where code = (select pcode from location where code = '"
                                + routeTo + "')");
                if (provinces.contains(l)) {
                    locationTo = Location.dao
                            .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                    + routeTo + "'");
                } else {
                    locationTo = Location.dao
                            .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                    + routeTo + "'");
                }
                setAttr("locationTo", locationTo);
            }
            if (transferOrder.get("sp_id") != null) {
                Party sp = Party.dao.findById(transferOrder.get("sp_id"));
                setAttr("partySp", sp);
                Contact spContact = Contact.dao.findById(sp.get("contact_id"));
                setAttr("spContact", spContact);
                break;
            }
        }

        logger.debug("localArr" + list);
        String order_no = null;
        setAttr("saveOK", false);
        TransferOrder transferOrder = new TransferOrder();
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);
        List<Record> paymentItemList = Collections.EMPTY_LIST;
        paymentItemList = Db.find("select * from fin_item where type='应付'");
        setAttr("paymentItemList", paymentItemList);

        setAttr("status", "新建");
        setAttr("saveOK", false);
            render("/yh/departOrder/editDepartOrder.html");
    }

    // editTransferOrder 初始数据
    public void getInitDepartOrderItems() {
        String order_id = getPara("localArr");// 运输单id
        String tr_item = getPara("tr_item");// 货品id
        String item_detail = getPara("item_detail");// 单品id

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(1) total from transfer_order_item tof"
                + " left join transfer_order  t_o  on tof.order_id =t_o.id "
                + " left join contact c on c.id in (select contact_id from party p where t_o.customer_id=p.id)"
                + " where tof.order_id in(" + order_id + ")";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select tof.* , t_o.order_no as order_no, t_o.id as tr_order_id, c.abbr as customer  from transfer_order_item tof"
                + " left join transfer_order  t_o  on tof.order_id = t_o.id "
                + "left join contact c on c.id in (select contact_id from party p where t_o.customer_id=p.id)"
                + " where tof.order_id in(" + order_id + ")  order by c.id" + sLimit;
        List<Record> departOrderitem = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", departOrderitem);
        renderJson(Map);
    }

    // 保存发车单
    public void saveDepartOrder() {
    	
        String depart_id = getPara("depart_id");// 发车单id
        String charge_type = getPara("chargeType");// 供应商计费类型
        String car_type = getPara("car_type");// 供应商计费类型, 如果是整车，需要知道整车类型
        String ltlPriceType = getPara("ltlUnitType");//如果是零担，需要知道零担计费类型：按体积，按重量
        String sp_id = getPara("sp_id");// 供应商id
        // 查找创建人id
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        String driver_id = getPara("driver_id");//
        String carinfoId = getPara("carinfoId");// 司机id
        String car_follow_name = getPara("car_follow_name");// 跟车人
        String car_follow_phone = getPara("car_follow_phone");// 跟车人电话
        Date createDate = Calendar.getInstance().getTime();
        String checkedDetail = getPara("checkedDetail");
        String uncheckedDetailIds = getPara("uncheckedDetail");
        String bookingNoteNumber = getPara("booking_note_number");
        String transfer_type = getPara("transfer_type");//运输方式
        String[] orderids = getPara("orderid").split(",");
        String[] pickupIds = getPara("pickupIds").split("&");//调车单id
        
        DepartOrder dp = null;
        if ("".equals(depart_id)) {
            dp = new DepartOrder();
            dp.set("charge_type", charge_type).set("create_by", users.get(0).get("id")).set("create_stamp", createDate)
                    .set("combine_type", DepartOrder.COMBINE_TYPE_DEPART).set("depart_no", OrderNoGenerator.getNextOrderNo("FC"))
                    .set("remark", getPara("remark")).set("car_follow_name", getPara("car_follow_name"))
                    .set("car_follow_phone", getPara("car_follow_phone")).set("route_from", getPara("route_from"))
                    .set("route_to", getPara("route_to")).set("status", getPara("status"))
                    .set("ltl_price_type", ltlPriceType).set("car_type", car_type)
                    .set("driver", getPara("driver_name")).set("phone", getPara("driver_phone")).set("car_no", getPara("car_no"))
                    .set("transfer_type",transfer_type).set("booking_note_number",bookingNoteNumber);
            if (!"".equals(driver_id) && driver_id != null) {
                dp.set("driver_id", driver_id);
            }else{
            	dp.set("driver_id", null);            	
            }
            if (!"".equals(carinfoId) && carinfoId != null) {
                dp.set("carinfo_id", carinfoId);
            }
            String partySpId = getPara("partySpId");
            if ("".equals(sp_id)) {
                if (!"".equals(partySpId)) {
                    dp.set("sp_id", partySpId);
                }
            } else {
                dp.set("sp_id", sp_id);
            }
            if (!"".equals(car_follow_name)) {
                dp.set("car_follow_name", car_follow_name);
            }
            if (!"".equals(car_follow_phone)) {
                dp.set("car_follow_phone", car_follow_phone);
            }
            if(getParaToDate("arrival_time") != null){
				dp.set("arrival_time", getPara("arrival_time")); 
			}
            if(getParaToDate("departure_time") != null){
            	dp.set("departure_time", getPara("departure_time")); 
            }
            dp.set("audit_status", "新建");
            dp.set("sign_status", "未回单");
            dp.save();
            //saveDepartTransfer(dp, getPara("orderid"), checkedDetail, uncheckedDetailIds ,pickupIds);
            saveDepartOrderMilestone(dp);
            if (!"".equals(partySpId)) {
                updateTransferOrderSp(dp);
            }
            
            // 如果是整车发车单需要更新运输单的信息  
            String routeSp = getPara("routeSp");
            if(routeSp != null && !"".equals(routeSp)){
            	transferOrderForRouteSp(dp);
            }
            
            //更新运输单发车状态，新建发车单从表
            for (int i = 0; i < orderids.length; i++) {
            	TransferOrder transferOrder = TransferOrder.dao.findById(orderids[i]);
                DepartTransferOrder departTransferOrder = new DepartTransferOrder();
                departTransferOrder.set("depart_id", dp.get("id"));
                departTransferOrder.set("order_id", orderids[i]);
                departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
                departTransferOrder.save();
                //记录调车单中单品的发车单ID，//发车单从表记录所选的调车单
                if("整车".equals(transfer_type) || pickupIds[0].trim() == "" || pickupIds[0].trim() == null || "null".equals(pickupIds[0].trim())){
                	 List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find(
		                        "select * from transfer_order_item_detail where order_id = '"+orderids[i]+"';");
	                for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
	                    transferOrderItemDetail.set("depart_id", dp.get("id"));
	                    transferOrderItemDetail.update();
	                }
	                
	                DepartPickupOrder departPickup = new DepartPickupOrder();
            		departPickup.set("depart_id", dp.get("id"))
            		.set("order_id", orderids[i])
            		.save();
            		
            		transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
                    transferOrder.update();
                }else{
    	            for (int K = 0; K < pickupIds.length; K++) {
    	            	String[] pickupId = pickupIds[K].split(",");
    	            	for (int j = 0; j < pickupId.length; j++) {
    	            		if(!"".equals(pickupId[j])){
				                List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find(
				                        "select * from transfer_order_item_detail where order_id = '"+orderids[i]+"' and pickup_id = ?", pickupId[j]);
				                for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
				                    transferOrderItemDetail.set("depart_id", dp.get("id"));
				                    transferOrderItemDetail.update();
				                }
			                
				                DepartPickupOrder departPickup = new DepartPickupOrder();
			            		departPickup.set("depart_id", dp.get("id"))
			            		.set("pickup_id", pickupId[j].trim())
			            		.set("order_id", orderids[i])
			            		.save();
			                }
    	            	}
    	            }
                    //验证是否已全部发车完成，调车单全部提货完成的情况下进行判断
                	if(TransferOrder.ASSIGN_STATUS_ALL.equals(transferOrder.get("pickup_assign_status"))){
                		if(transferOrder.get("cargo_nature").equals("ATM")){
                			//运输单单品总数
        	            	Record totalTransferOrderAmount = Db.findFirst("select count(0) total from transfer_order_item_detail where order_id = " + orderids[i]);
        	            	//总提货数量（之前+现在）
        	            	Record totalPickAmount = Db.findFirst("select count(0) total from transfer_order_item_detail where depart_id is not null and order_id = " + orderids[i]);
        	            	//运输单
        					if(totalPickAmount.getLong("total") == totalTransferOrderAmount.getLong("total")){
        						transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
        					}else{
        						transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
        					}
                		}else{
                			transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
                		}
                	}else{
                		transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
                	}
                    transferOrder.update();
                }
            }
            
            //发车单记录所选的调车单
            /*if(pickupIds[0].trim() != ""){
	            for (int i = 0; i < pickupIds.length; i++) {
	            	String[] pickupId = pickupIds[i].split(",");
	            	for (int j = 0; j < pickupId.length; j++) {
	            		DepartPickupOrder departPickup = new DepartPickupOrder();
	            		departPickup.set("depart_id", dp.get("id"))
	            		.set("pickup_id", pickupId[j])
	            		.set("order_id", orderids[i])
	            		.save();
	            	}
	            }
            }*/
            
        } else {//TODO update不需要更改create_by, create_date
            dp = DepartOrder.dao.findById(Integer.parseInt(depart_id));
            dp.set("charge_type", charge_type).set("combine_type", DepartOrder.COMBINE_TYPE_DEPART)
                    .set("remark", getPara("remark")).set("car_follow_name", getPara("car_follow_name"))
                    .set("car_follow_phone", getPara("car_follow_phone")).set("route_from", getPara("route_from"))
                    .set("route_to", getPara("route_to")).set("status", getPara("status"))
                    .set("ltl_price_type", ltlPriceType).set("car_type", car_type)
                    .set("driver", getPara("driver_name")).set("phone", getPara("driver_phone"))
                    .set("car_no", getPara("car_no")).set("booking_note_number",bookingNoteNumber);
            if (!"".equals(driver_id) && driver_id != null) {
                dp.set("driver_id", driver_id);
            }else{
            	dp.set("driver_id", null);            	
            }
            if (!"".equals(carinfoId) && carinfoId != null) {
                dp.set("carinfo_id", carinfoId);
            }else{
            	dp.set("carinfo_id",null);
            }
            if (!"".equals(sp_id)) {
                dp.set("sp_id", Integer.parseInt(sp_id));
            }
            if (!"".equals(car_follow_name)) {
                dp.set("car_follow_name", car_follow_name);
            }
            if (!"".equals(car_follow_phone)) {
                dp.set("car_follow_phone", car_follow_phone);
            }
            if(getParaToDate("arrival_time") != null){
				dp.set("arrival_time", getPara("arrival_time")); 
			}
            if(getParaToDate("departure_time") != null){
            	dp.set("departure_time", getPara("departure_time")); 
            }
            dp.update();
            updateDepartTransfer(dp, getPara("orderid"), checkedDetail, uncheckedDetailIds);
            if (!"".equals(sp_id)) {
                updateTransferOrderSp(dp);
            }
        }
        renderJson(dp);
    }

    // 如果是整车发车单需要更新运输单的信息  
    private void transferOrderForRouteSp(DepartOrder dp) {
    	List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                "select * from depart_transfer where depart_id = ?", dp.get("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));  
        	transferOrder.set("pickup_mode", "routeSP");
        	transferOrder.set("charge_type", "perCar");
            transferOrder.update();
        }
	}

	// 更新运输单的供应商
    private void updateTransferOrderSp(DepartOrder dp) {
        List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                "select * from depart_transfer where depart_id = ?", dp.get("id"));
        for (DepartTransferOrder departTransferOrder : departTransferOrders) {
            TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));            
            transferOrder.set("sp_id", dp.get("sp_id"));
            transferOrder.update();
        }
    }

    // 保存发车里程碑
    private void saveDepartOrderMilestone(DepartOrder pickupOrder) {
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        transferOrderMilestone.set("status", "新建");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        transferOrderMilestone.set("exception_record", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
        transferOrderMilestone.set("depart_id", pickupOrder.get("id"));
        transferOrderMilestone.save();
    }

    // 更新中间表
    private void updateDepartTransfer(DepartOrder pickupOrder, String orderId, String checkedDetail,
            String uncheckedDetailId) {
        if (checkedDetail != null && !"".equals(checkedDetail)) {
            String[] checkedDetailIds = checkedDetail.split(",");
            TransferOrderItemDetail transferOrderItemDetail = null;
            for (int j = 0; j < checkedDetailIds.length && checkedDetailIds.length > 0; j++) {
                transferOrderItemDetail = TransferOrderItemDetail.dao.findById(checkedDetailIds[j]);
                transferOrderItemDetail.set("depart_id", pickupOrder.get("id"));
                transferOrderItemDetail.update();
            }
            TransferOrder transferOrder = TransferOrder.dao.findById(transferOrderItemDetail.get("order_id"));
            transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
            transferOrder.update();
        }
        String[] uncheckedDetailIds = uncheckedDetailId.split(",");
        if (uncheckedDetailId != null && !"".equals(uncheckedDetailId)) {
            TransferOrderItemDetail transferOrderItemDetail = null;
            for (int j = 0; j < uncheckedDetailIds.length && uncheckedDetailIds.length > 0; j++) {
                transferOrderItemDetail = TransferOrderItemDetail.dao.findById(uncheckedDetailIds[j]);
                transferOrderItemDetail.set("depart_id", null);
                transferOrderItemDetail.update();
            }
        }
        if (uncheckedDetailIds.length == 0 || "".equals(uncheckedDetailIds[0])) {
            List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
                    .find("select * from transfer_order_item_detail where order_id in(" + orderId + ")");
            String str = "";
            for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
                Long departId = transferOrderItemDetail.get("depart_id");
                if (departId == null || "".equals(departId)) {
                    str += departId;
                }
            }
            if ("".equals(str)) {
                List<TransferOrder> transferOrders = TransferOrder.dao
                        .find("select * from transfer_order where id in (" + orderId + ")");
                for (TransferOrder transferOrder : transferOrders) {
                    transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
                    transferOrder.update();
                }
            }
        }
    }

    // 将数据保存进中间表
    private void saveDepartTransfer(DepartOrder departOrder, String param, String checkedDetail,
            String uncheckedDetailId, String pickupIds) {
        DepartTransferOrder departTransferOrder = null;
        String[] params = param.split(",");
        if (checkedDetail == null || "".equals(checkedDetail)) {
            for (int i = 0; i < params.length; i++) {
            	
            	TransferOrder transferOrder = TransferOrder.dao.findById(params[i]);
                transferOrder.set("depart_assign_status", transferOrder.get("pickup_assign_status"));
                transferOrder.update();
            	
                departTransferOrder = new DepartTransferOrder();
                departTransferOrder.set("depart_id", departOrder.get("id"));
                departTransferOrder.set("order_id", params[i]);
                departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
                departTransferOrder.save();
                
                List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find(
                        "select * from transfer_order_item_detail where order_id = ?", params[i]);
                for (TransferOrderItemDetail transferOrderItemDetail : transferOrderItemDetails) {
                    if (transferOrderItemDetail.get("depart_id") == null) {
                        transferOrderItemDetail.set("depart_id", departOrder.get("id"));
                        transferOrderItemDetail.update();
                    }
                }
            }
        } else {
            for (int i = 0; i < params.length; i++) {
                departTransferOrder = new DepartTransferOrder();
                departTransferOrder.set("depart_id", departOrder.get("id"));
                departTransferOrder.set("order_id", params[i]);
                TransferOrder transferOrder = TransferOrder.dao.findById(params[i]);
                transferOrder.update();
                departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
                departTransferOrder.save();
            }
            String[] checkedDetailIds = checkedDetail.split(",");
            for (int j = 0; j < checkedDetailIds.length; j++) {
                TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
                        .findById(checkedDetailIds[j]);
                transferOrderItemDetail.set("depart_id", departOrder.get("id"));
                transferOrderItemDetail.update();
            }

            String[] uncheckedDetailIds = uncheckedDetailId.split(",");
            for (int j = 0; j < uncheckedDetailIds.length; j++) {
                TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
                        .findById(uncheckedDetailIds[j]);
                transferOrderItemDetail.set("depart_id", "");
                transferOrderItemDetail.update();
            }
        }
    }

    // 修改发车单状态
    public void updatestate() {
        String depart_id = getPara("depart_id");// 发车单id
        String order_state = getPara("order_state");// 状态
        int nummber = 0;// 没入库的货品数量

        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        String name = (String) currentUser.getPrincipal();
        UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");

        DepartOrder departOrder = DepartOrder.dao.findById(Integer.parseInt(depart_id));
        departOrder.set("status", order_state).update();
//        if ("已入库".equals(order_state)) {
//            productInWarehouse(depart_id);// 产品入库
//        }
        //if ("已发车".equals(order_state)) {
            
            // 生成应付, （如果已经有了应付，就要清除掉旧数据重新算）
            // 计件/整车/零担 生成发车单中供应商的应付，要算 item 的数量 * 合同中定义的价格
            // Depart_Order_fin_item 提货单/发车单应付明细表
            // 第一步：看发车单调度选择的计费方式是哪种：计件，整车，零担
            // 第二步：循环所选运输单中的 item, 到合同中（循环）比对去算钱。
            

            List<DepartTransferOrder> dItem = DepartTransferOrder.dao
                    .find("select order_id from depart_transfer where depart_id =" + depart_id + "");
            
            String transferIds = "";
            for (DepartTransferOrder dItem2 : dItem) {
                transferIds += dItem2.get("order_id") + ",";
            }
            if(transferIds.length()>0)
                transferIds = transferIds.substring(0, transferIds.length() - 1);

            List<Record> transferOrderItemList = Db
                    .find("select toi.*, t_o.route_from, t_o.route_to,t_o.cargo_nature from transfer_order_item toi left join transfer_order t_o on toi.order_id = t_o.id where toi.order_id in("
                            + transferIds + ") order by pickup_seq desc");
            // TODO:生成应付
            
            calcCost(departOrder, transferOrderItemList);
            
        if ("已签收".equals(order_state)) {
            // 生成回单
            Date createDate = Calendar.getInstance().getTime();
            String orderNo = OrderNoGenerator.getNextOrderNo("HD");
            
            ReturnOrder returnOrder = new ReturnOrder();
            returnOrder.set("order_no", orderNo);
            returnOrder.set("depart_order_id", depart_id);
            // returnOrder.set("customer_id", deliveryOrder.get("customer_id"));
            // returnOrder.set("notity_party_id",
            // deliveryOrder.get("notity_party_id"));
            returnOrder.set("order_type", "应收");
            returnOrder.set("transaction_status", "新建");
            returnOrder.set("creator", users.get("id"));
            returnOrder.set("create_date", createDate);
            returnOrder.save();

        }

        Map Map = new HashMap();
        Map.put("amount", nummber);
        Map.put("depart", departOrder);
        renderJson(Map);
    }

    // TODO 发车单应付计费  先获取有效期内的合同，条目越具体优先级越高
    // 级别1：计费类别 + 货  品 + 始发地 + 目的地
    // 级别2：计费类别 + 货  品 + 目的地
    // 级别3：计费类别 + 始发地 + 目的地
    // 级别4：计费类别 + 目的地
    
    // priceType 不分大小写在mysql会有问题
    private void calcCost(DepartOrder departOrder, List<Record> transferOrderItemList) {
        Long spId=departOrder.getLong("sp_id");
        
        
        
        if ( spId== null)
            return;
        getFinNoContractCost(departOrder);
        String orderCreateDate = departOrder.getDate("create_stamp").toString();
        //先获取有效期内的sp合同, 如有多个，默认取第一个
        Contract spContract= Contract.dao.findFirst("select c.* from contract c left join contract_item ci on ci.contract_id = c.id where c.type='SERVICE_PROVIDER' " +
        		"and ('"+departOrder.getDate("create_stamp")+"' between c.period_from and c.period_to) and c.party_id = "+ spId +
        		" and ci.from_id = '" +departOrder.get("route_from") + 
        		"' and ci.to_id = '" + departOrder.get("route_to") +"'");
        if(spContract==null)
            return;
        
        String chargeType = departOrder.get("charge_type");
        
        if ( spId!= null) {
            if ("perUnit".equals(chargeType)) {
                genFinPerUnit(departOrder, transferOrderItemList, spContract, chargeType);
            } else if ("perCar".equals(chargeType)) {
                genFinPerCar(departOrder,transferOrderItemList,spContract, chargeType);
            } else if ("perCargo".equals(chargeType)) {
                //每次都新生成一个helper来处理计算，防止并发问题。
                DepartOrderPaymentHelper.getInstance().genFinPerCargo(departOrder, transferOrderItemList, spContract, chargeType);
            }            
        }
    }

    
    private void genFinPerCar(DepartOrder departOrder, List<Record> transferOrderItemList,Contract spContract, String chargeType) {
        // 根据发车单整车的车型，始发地，目的地，计算合同价
    	boolean isFinContract = true;
    	for (Record record : transferOrderItemList) {
    		TransferOrder transferOrder = TransferOrder.dao.findFirst("select * from transfer_order where id = ?",record.get("order_id"));
    		Boolean isTrue = transferOrder.get("no_contract_cost");
    		if(isTrue){
    			isFinContract=false;
    		}
    		
    		
		}
    	//TransferOrder transfer= TransferOrder.dao.findFirst("select * from transfer_order where id = ?",transferOrderItemList.get(0).get("order_id"));
    	/*getFinNoContractCost(departOrder);*/
    	if(isFinContract){
    		Record contractFinItem = Db
                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                            +" and carType = '" + departOrder.get("car_type")//对应发车单的 car_type
                            +"' and from_id = '" + departOrder.get("route_from")
                            +"' and to_id = '" + departOrder.get("route_to")
                            + "' and priceType='"+chargeType+"'");
            if (contractFinItem != null) {
                genFinItem(departOrder, null, contractFinItem, chargeType);
            }else{
                contractFinItem = Db
                        .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                +" and carType = '" + departOrder.get("car_type")//对应发车单的 car_type
                                +"' and from_id = '" + departOrder.get("route_from")
                                +"' and to_id = '" + departOrder.get("route_to")
                                + "' and priceType='"+chargeType+"'");
                if (contractFinItem != null) {
                    genFinItem(departOrder, null, contractFinItem, chargeType);
                }/*else{
                    contractFinItem = Db
                            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                    +" and from_id = '" + departOrder.get("route_from")
                                    +"' and to_id = '" + departOrder.get("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    if (contractFinItem != null) {
                        genFinItem(users, departOrder, null, contractFinItem, chargeType);
                    }*/
                else{
                    contractFinItem = Db
                            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                    +" and carType = '" + departOrder.get("car_type")//对应发车单的 car_type
                                    +"' and to_id = '" + departOrder.get("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    if (contractFinItem != null) {
                        genFinItem(departOrder, null, contractFinItem, chargeType);
                    }
                }
            }
    	}
    }

    private void genFinPerUnit(DepartOrder departOrder, List<Record> transferOrderItemList, Contract spContract,
            String chargeType) {
    	
    	//TransferOrder transfer = TransferOrder.dao.findFirst("select * from transfer_order where id = ?",transferOrderItemList.get(0).get("order_id"));
    	
    	for (Record tOrderItemRecord : transferOrderItemList) {
        	//TODO:获取到运输单，并且判断是否要计算合同
        	TransferOrder transferOrder = TransferOrder.dao.findFirst("select * from transfer_order where id = ?",tOrderItemRecord.get("order_id"));
        	Boolean isTrue = transferOrder.get("no_contract_cost");
        	if(!isTrue){
        		Record contractFinItem = Db
                        .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                +" and product_id = " + tOrderItemRecord.get("product_id")
                                +" and from_id = '" + departOrder.get("route_from")
                                +"' and to_id = '" + departOrder.get("route_to")
                                + "' and priceType='"+chargeType+"'");
                if (contractFinItem != null) {
                    genFinItem(departOrder, tOrderItemRecord, contractFinItem, chargeType);
                }else{
                    contractFinItem = Db
                            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                    +" and product_id = " + tOrderItemRecord.get("product_id")
                                    +" and to_id = '" + departOrder.get("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    if (contractFinItem != null) {
                        genFinItem(departOrder, tOrderItemRecord, contractFinItem, chargeType);
                    }else{
                        contractFinItem = Db
                                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                        +" and from_id = '" + departOrder.get("route_from")
                                        +"' and to_id = '" + departOrder.get("route_to")
                                        + "' and priceType='"+chargeType+"'");
                        if (contractFinItem != null) {
                            genFinItem(departOrder, tOrderItemRecord, contractFinItem, chargeType);
                        }else{
                            contractFinItem = Db
                                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                            +" and to_id = '" + departOrder.get("route_to")
                                            + "' and priceType='"+chargeType+"'");
                            if (contractFinItem != null) {
                                genFinItem(departOrder, tOrderItemRecord, contractFinItem, chargeType);
                            }
                        }
                    }
                }
        	}
        }
    }
  
    private void genFinItem(DepartOrder departOrder, Record tOrderItemRecord, Record contractFinItem, String chargeType) {
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
        DepartOrderFinItem departOrderFinItem = new DepartOrderFinItem();
        
        departOrderFinItem.set("fin_item_id", contractFinItem.get("fin_item_id"));
        String name = (String) currentUser.getPrincipal();
        UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
    	if("perCar".equals(chargeType)){
    		//整车生成应付条目
    		genFinItemPerCar(departOrder, contractFinItem, now,
					departOrderFinItem, users);
    	}else{
    		if(tOrderItemRecord != null){
    			String cargo_nature = tOrderItemRecord.get("cargo_nature");
    			double money=0;
    			if(cargo_nature.equals("cargo")){//普货计件
    				genFinItemNormalCargo(departOrder, tOrderItemRecord,
							contractFinItem, now, departOrderFinItem, users);
    			}else{//ATM 计件
    				genFinItemPerATM(departOrder, tOrderItemRecord,
							contractFinItem, now, departOrderFinItem, users);	
    			}
    		}
    	}
    }
    
	private void genFinItemPerATM(DepartOrder departOrder,
			Record tOrderItemRecord, Record contractFinItem,
			java.sql.Timestamp now, DepartOrderFinItem departOrderFinItem,
			UserLogin users) {
		double money;
		Record record = Db.findFirst("select count(toid.id) as amount from transfer_order_item_detail toid where item_id = " + tOrderItemRecord.get("id") +" and depart_id = " +departOrder.get("id"));
		if(record.getLong("amount") != 0){
			money=contractFinItem.getDouble("amount") * Double.parseDouble(record.get("amount").toString());
			BigDecimal bg = new BigDecimal(money);
		    double amountDouble = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			departOrderFinItem.set("amount", amountDouble);
			departOrderFinItem.set("transfer_order_id", tOrderItemRecord.get("order_id"));
		    departOrderFinItem.set("transfer_order_item_id", tOrderItemRecord.get("id"));
		
		    saveFinItem(departOrder, now, departOrderFinItem, users);
		}
	}
	private void genFinItemNormalCargo(DepartOrder departOrder,
			Record tOrderItemRecord, Record contractFinItem,
			java.sql.Timestamp now, DepartOrderFinItem departOrderFinItem,
			UserLogin users) {
		double money;
		money=contractFinItem.getDouble("amount") * Double.parseDouble(tOrderItemRecord.get("amount").toString());
		BigDecimal bg = new BigDecimal(money);
		double amountDouble = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		departOrderFinItem.set("amount", amountDouble);
		departOrderFinItem.set("transfer_order_id", tOrderItemRecord.get("order_id"));
		departOrderFinItem.set("transfer_order_item_id", tOrderItemRecord.get("id"));
			
		saveFinItem(departOrder, now, departOrderFinItem, users);
	}
	private void genFinItemPerCar(DepartOrder departOrder,
			Record contractFinItem, java.sql.Timestamp now,
			DepartOrderFinItem departOrderFinItem, UserLogin users) {
		double money=contractFinItem.getDouble("amount");
		BigDecimal bg = new BigDecimal(money);
		double amountDouble = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		departOrderFinItem.set("amount", amountDouble);  
		
		saveFinItem(departOrder, now, departOrderFinItem, users);
	}
	private void saveFinItem(DepartOrder departOrder, java.sql.Timestamp now,
			DepartOrderFinItem departOrderFinItem, UserLogin users) {
		departOrderFinItem.set("depart_order_id", departOrder.getLong("id"));
		departOrderFinItem.set("status", "未完成");
		departOrderFinItem.set("creator", users.get("id"));
		departOrderFinItem.set("create_date", now);
		departOrderFinItem.set("create_name", departOrderFinItem.CREATE_NAME_SYSTEM);
		departOrderFinItem.set("cost_source", "合同费用");
		departOrderFinItem.save();
	}

    // 点击货品table的查看 ，显示对应货品的单品
    public void itemDetailList() {
        String item_id = getPara("item_id");
        String depart_id = getPara("depart_id");// 发车单id
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from transfer_order_item_detail  where item_id =" + item_id + "";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> itemdetail = null;
        if ("".equals(depart_id)) {
            String sql_tr_itemdetail_id = "select id as itemdetail_id  from transfer_order_item_detail where item_id="
                    + item_id;
            String sql_dp_itemdetail_id = "select itemdetail_id  from depart_transfer_itemdetail  where item_id="
                    + item_id;
            List<Record> sql_tr_itemdetail_idlist = Db.find(sql_tr_itemdetail_id);
            List<Record> sql_dp_itemdetail_idlist = Db.find(sql_dp_itemdetail_id);
            /* List<Record> depart_itemdetail=Db.find(sql_item); */
            sql_tr_itemdetail_idlist.removeAll(sql_dp_itemdetail_idlist);
            String detail_id = "0";
            if (sql_tr_itemdetail_idlist.size() > 0) {
                for (int i = 0; i < sql_tr_itemdetail_idlist.size(); i++) {
                    detail_id += sql_tr_itemdetail_idlist.get(i).get("itemdetail_id") + ",";
                }
                detail_id = detail_id.substring(0, detail_id.length() - 1);
            }
            String sql = "select * from transfer_order_item_detail  where ID in(" + detail_id + ")";
            itemdetail = Db.find(sql);
        } else {
            String sql = "select * from transfer_order_item_detail  where item_id in(" + item_id + ")";
            itemdetail = Db.find(sql);
        }

        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", itemdetail);
        renderJson(Map);
    }

    // 发车单删除
    public void cancel() {
        String id = getPara();
        String sql = "select * from depart_transfer  where depart_id =" + id + "";
        List<DepartTransferOrder> re_tr = DepartTransferOrder.dao.find(sql);
        for (int i = 0; i < re_tr.size(); i++) {
            DepartTransferOrder dep_tr = DepartTransferOrder.dao.findFirst(sql);
            dep_tr.set("depart_id", null).update();
            dep_tr.delete();
        }
        DepartOrder re = DepartOrder.dao.findById(id);
        re.set("driver_id", null).update();
        re.delete();
        render("/yh/departOrder/departOrderList.html");
    }

    // 单击tab里程碑
    public void transferOrderMilestoneList() {
        Map<String, List> map = new HashMap<String, List>();
        List<String> usernames = new ArrayList<String>();
        String departOrderId = getPara("departOrderId");
        if (departOrderId == "" || departOrderId == null) {
            departOrderId = "-1";
        }
        if (!"-1".equals(departOrderId)) {
            List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                    .find("select * from transfer_order_milestone where type = '"
                            + TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE + "'and depart_id=" + departOrderId);
            for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = userLogin.get("c_name");
                if(username==null||"".equals(username)){
                	username=userLogin.get("user_name");
                }
                usernames.add(username);
            }
            map.put("transferOrderMilestones", transferOrderMilestones);
            map.put("usernames", usernames);
        }
        renderJson(map);
    }

    // 编辑里程碑
    public void saveTransferOrderMilestone() {
        String milestoneDepartId = getPara("milestoneDepartId");
        Map<String, Object> map = new HashMap<String, Object>();
        if (milestoneDepartId != null && !"".equals(milestoneDepartId)) {
            DepartOrder departOrder = DepartOrder.dao.findById(milestoneDepartId);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            String status = getPara("status");
            String location = getPara("location");
            String exception_record = getPara("exception_record");
            transferOrderstatus(milestoneDepartId, status, location);
            if (!status.isEmpty()) {
                transferOrderMilestone.set("status", status);
                departOrder.set("status", status);
            } else {
                transferOrderMilestone.set("status", "在途");
                departOrder.set("status", "在途");
            }
            departOrder.update();
            if (!location.isEmpty()) {
                transferOrderMilestone.set("location", location);
            } else {
                transferOrderMilestone.set("location", "");
            }
            if(exception_record != null && "".equals(exception_record)){
                transferOrderMilestone.set("exception_record", exception_record);
            }else{
                transferOrderMilestone.set("exception_record", "");
            }
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");

            transferOrderMilestone.set("create_by", users.get(0).get("id"));

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            
            

            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("depart_id", milestoneDepartId);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
            transferOrderMilestone.save();

            map.put("transferOrderMilestone", transferOrderMilestone);
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("c_name");
            if(username==null||"".equals(username)){
            	username=userLogin.get("user_name");
            }
            map.put("username", username);
        }
        renderJson(map);
    }

    // 同步运输单状态里程碑
    public void transferOrderstatus(String de_or, String status, String location) {
        int depart_id = Integer.parseInt(de_or);
        List<DepartTransferOrder> dep = DepartTransferOrder.dao
                .find("select * from depart_transfer  where depart_id in(" + depart_id + ")");
        for (int i = 0; i < dep.size(); i++) {
            int order_id = Integer.parseInt(dep.get(i).get("order_id").toString());
            TransferOrder tr = TransferOrder.dao.findById(order_id);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            if (!"".equals(status) && status != null) {
                if (tr.get("depart_assign_status") == TransferOrder.ASSIGN_STATUS_PARTIAL) {
                    transferOrderMilestone.set("status", "部分" + status);
                    tr.set("status", "部分" + status);
                } else {
                    transferOrderMilestone.set("status", status);
                    tr.set("status", status);
                }
            } else {
                if (tr.get("depart_assign_status") == TransferOrder.ASSIGN_STATUS_PARTIAL) {
                    transferOrderMilestone.set("status", "部分在途");
                    tr.set("status", "部分在途");
                } else {
                    transferOrderMilestone.set("status", "在途");
                    tr.set("status", "在途");
                }
            }
            tr.update();
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
            transferOrderMilestone.set("create_by", users.get(0).get("id"));
            if (location == null || location.isEmpty()) {
                transferOrderMilestone.set("location", "");
            } else {
                transferOrderMilestone.set("location", location);
            }

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.set("order_id", order_id);
            transferOrderMilestone.save();

        }

    }

    // 在途运输单管理
    public void transferMilestoneIndex() {
    	Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
    	setAttr("customizeField", customizeField);
        render("/yh/departOrder/TransferOrderStatus.html");
    }

    public void ownTransferMilestone() {
    	Map transferOrderListMap = null;
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String address = getPara("address");
        String customer = getPara("customer");
        String sp = getPara("sp");
        String officeName = getPara("officeName");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "";
        String sql = "";
        Record rec = null;
        // String strWhere = DataTablesUtils.buildSingleFilter(this);
        if (orderNo == null && status == null && address == null && customer == null && sp == null && beginTime == null
                && endTime == null) {
            sqlTotal = "select count(1) total from transfer_order t where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='own'";
            sql = "select t.*,c1.abbr cname,c2.abbr spname,t.create_stamp,o.office_name oname from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='own'  order by create_stamp desc"
                    + sLimit;
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }

            sqlTotal = "select count(1) total from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id"
                    + " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='own' and t.order_no like '%"
                    + orderNo + "%' and t.status like '%" + status + "%' and t.address like '%" + address
                    + "%' and c1.abbr like '%" + customer + "%' and ifnull(c2.abbr,'') like '%" + sp
                    + "%' and o.office_name  like '%" + officeName + "%' and create_stamp between '" + beginTime
                    + "' and '" + endTime + "'";

            sql = "select t.*,c1.abbr cname,c2.abbr spname,o.office_name oname from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id"
                    + " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='own' and t.order_no like '%"
                    + orderNo + "%' and t.status like '%" + status + "%' and t.address like '%" + address
                    + "%' and c1.abbr like '%" + customer + "%' and ifnull(c2.abbr,'') like '%" + sp
                    + "%' and o.office_name  like '%" + officeName + "%' and create_stamp between '" + beginTime
                    + "' and '" + endTime + "' order by create_stamp desc" + sLimit;
        }
        rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> transferOrders = Db.find(sql);

        transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);
        renderJson(transferOrderListMap);
    }

    // 回显供应商
    public void getIntsp() {
        Contact co = Contact.dao
                .findFirst("select * from contact  where id in(select contact_id  from party p where p.id="
                        + Integer.parseInt(getPara("sp_id").toString()) + " )");
        renderJson(co);
    }

    // 修改运输单仓库
    public void updateWarehouse(String depart_id, String house_id) {
        int de_id = Integer.parseInt(depart_id);
    }

    // 外包运输单更新
    public void transferonTrip() {
	    	Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
	    	setAttr("customizeField", customizeField);
            render("/yh/departOrder/transferOrderOnTripList.html");
    }

    public void transferonTriplist() {
        Map transferOrderListMap = null;
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String address = getPara("address");
        String customer = getPara("customer");
        String sp = getPara("sp");
        String officeName = getPara("officeName");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "";
        String sql = "";
        Record rec = null;
        // String strWhere = DataTablesUtils.buildSingleFilter(this);
        if (orderNo == null && status == null && address == null && customer == null && sp == null && beginTime == null
                && endTime == null) {
            sqlTotal = "select count(1) total from transfer_order t where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='out_source'";
            sql = "select t.*,c1.abbr cname,c2.abbr spname,t.create_stamp,o.office_name oname from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id "
                    + " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='out_source'  order by create_stamp desc"
                    + sLimit;
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }

            sqlTotal = "select count(1) total from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id"
                    + " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='out_source' and t.order_no like '%"
                    + orderNo + "%' and t.status like '%" + status + "%' and t.address like '%" + address
                    + "%' and c1.abbr like '%" + customer + "%' and ifnull(c2.abbr,'') like '%" + sp
                    + "%' and o.office_name  like '%" + officeName + "%' and create_stamp between '" + beginTime
                    + "' and '" + endTime + "'";

            sql = "select t.*,c1.abbr cname,c2.abbr spname,o.office_name oname from transfer_order t "
                    + " left join party p1 on t.customer_id = p1.id "
                    + " left join party p2 on t.sp_id = p2.id "
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " left join contact c2 on p2.contact_id = c2.id"
                    + " left join office o on t.office_id = o.id where t.status in('已发车','在途') and t.arrival_mode = 'delivery' and t.operation_type ='out_source' and t.order_no like '%"
                    + orderNo + "%' and t.status like '%" + status + "%' and t.address like '%" + address
                    + "%' and c1.abbr like '%" + customer + "%' and ifnull(c2.abbr,'') like '%" + sp
                    + "%' and o.office_name  like '%" + officeName + "%' and create_stamp between '" + beginTime
                    + "' and '" + endTime + "' order by create_stamp desc" + sLimit;
        }
        rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> transferOrders = Db.find(sql);

        transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);
        renderJson(transferOrderListMap);
    }

    // 外包运输单里程碑
    public void transferMilestoneList() {
        Map<String, List> map = new HashMap<String, List>();
        List<String> usernames = new ArrayList<String>();
        String order_id = getPara("order_id");
        if (order_id == "" || order_id == null) {
            order_id = "-1";
        }
        String pickupOrderId = getPara("pickupOrderId");
        if (pickupOrderId == "" || pickupOrderId == null) {
            pickupOrderId = "-1";
        }
        if (order_id != "-1" && pickupOrderId == "-1") {
            List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                    .find("select * from transfer_order_milestone where type = '"
                            + TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE + "' and order_id=" + order_id);
            for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = userLogin.get("c_name");
                if(username==null||"".equals(username)){
                	username=userLogin.get("user_name");
                }
                usernames.add(username);
            }
            map.put("transferOrderMilestones", transferOrderMilestones);
            map.put("usernames", usernames);
        } else {
            List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                    .find("select * from transfer_order_milestone where type = '"
                            + TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE + "' and pickup_id=" + pickupOrderId);
            for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = userLogin.get("c_name");
                if(username==null||"".equals(username)){
                	username=userLogin.get("user_name");
                }
                usernames.add(username);
            }
            map.put("transferOrderMilestones", transferOrderMilestones);
            map.put("usernames", usernames);
        }
        renderJson(map);
    }

    // 保存外包运输单里程碑
    public void saveTransferMilestone() {
        String milestoneDepartId = getPara("milestoneDepartId");
        Map<String, Object> map = new HashMap<String, Object>();
        if (milestoneDepartId != null && !"".equals(milestoneDepartId)) {
            TransferOrder tr = TransferOrder.dao.findById(milestoneDepartId);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            String status = getPara("status");
            String location = getPara("location");
            transferOrderstatus(milestoneDepartId, status, location);
            if (!status.isEmpty()) {
                transferOrderMilestone.set("status", status);
                tr.set("status", status);
            } else {
                transferOrderMilestone.set("status", "在途");
                tr.set("status", "在途");
            }
            tr.update();
            if (!location.isEmpty()) {
                transferOrderMilestone.set("location", location);
            } else {
                transferOrderMilestone.set("location", "");
            }
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");

            transferOrderMilestone.set("create_by", users.get(0).get("id"));

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("order_id", milestoneDepartId);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.save();

            map.put("transferOrderMilestone", transferOrderMilestone);
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("user_name");
            map.put("username", username);
        }
        renderJson(map);
    }

    // 初始化收货人信息
    public void ginNotifyPerson() {
        int id = 0;
        String[] order_id = getPara("order_id").split(",");
        if (order_id.length == 1) {
            id = Integer.parseInt(getPara("order_id").toString());
            String Sql = "select co.* from contact  co left join transfer_order tro  on tro.id= " + id
                    + " left join party p on p.id=tro.notify_party_id " + "where co.id=p.contact_id";
            Contact co = Contact.dao.findFirst(Sql);
            renderJson(co);
        }

    }

    // 回显司机信息
    public void ginDriver() {
        String id = getPara("depart_id");
        if (id != null && id != "") {
            DepartOrder de = DepartOrder.dao.findById(Integer.parseInt(id));
            Long d_id = de.get("driver_id");
            Carinfo car = Carinfo.dao.findById(d_id);
            renderJson(car);
        }
    }

    // 从发车单货品计算货品数量
    public double productAmount(String depart_id, String item_id) {
        int de_id = Integer.parseInt(depart_id);
        int it_id = Integer.parseInt(item_id);
        double amount = 0;
        String sql = "select * from depart_transfer_itemdetail where depart_id =" + de_id + " and item_id=" + it_id
                + "";
        List<Record> de_item_amount = Db.find(sql);
        if (de_item_amount.size() == 1) {
            if (de_item_amount.get(0).get("itemdetail_id") == null) {
                TransferOrderItem tr_item = TransferOrderItem.dao.findById(it_id);
                amount = Double.parseDouble(tr_item.get("amount").toString());
            } else {
                amount = de_item_amount.size();
            }
        } else if (de_item_amount.size() > 1) {
            amount = de_item_amount.size();
        }
        return amount;

    }

    // 产品入库
    public static void productInWarehouse(String departId) {
        if (!"".equals(departId) && departId != null) {
            String orderIds = "";
            List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                    "select * from depart_transfer where depart_id = ?", departId);
            for (DepartTransferOrder departTransferOrder : departTransferOrders) {
                orderIds += departTransferOrder.get("order_id") + ",";
            }
            orderIds = orderIds.substring(0, orderIds.length() - 1);
            List<TransferOrder> transferOrders = TransferOrder.dao.find("select * from transfer_order where id in("
                    + orderIds + ")");
            for (TransferOrder transferOrder : transferOrders) {
                InventoryItem inventoryItem = null;
                List<TransferOrderItem> transferOrderItems = TransferOrderItem.dao.find(
                        "select * from transfer_order_item where order_id = ?", transferOrder.get("id"));
                for (TransferOrderItem transferOrderItem : transferOrderItems) {
                    if (transferOrderItem != null) {
                        if (transferOrderItem.get("product_id") != null) {
                        	//判断是否有库存
                            String inventoryItemSql = "select * from inventory_item where product_id = "
                                    + transferOrderItem.get("product_id") + " and warehouse_id = "
                                    + transferOrder.get("warehouse_id");
                            inventoryItem = InventoryItem.dao.findFirst(inventoryItemSql);
                            //判断发车单中的运输单是否有单品,
                            String sqlTotal = "select count(1) total from transfer_order_item_detail where depart_id = "
                                    + departId + " and order_id = " + transferOrder.get("id") + " and item_id = " + transferOrderItem.get("id");
                            Record rec = Db.findFirst(sqlTotal);
                            Long amount = rec.getLong("total");
                            if(amount == 0){
                            	//当运输单没有单品时取货品信息中的数量
                            	amount = Math.round(transferOrderItem.getDouble("amount"));
                            }
                            if (inventoryItem == null) {
                                inventoryItem = new InventoryItem();
                                inventoryItem.set("party_id", transferOrder.get("customer_id"));
                                inventoryItem.set("warehouse_id", transferOrder.get("warehouse_id"));
                                inventoryItem.set("product_id", transferOrderItem.get("product_id"));
                                inventoryItem.set("total_quantity", amount);
                                inventoryItem.set("available_quantity", amount);
                                inventoryItem.save();
                            } else {
                                inventoryItem.set("total_quantity",
                                        Double.parseDouble(inventoryItem.get("total_quantity").toString()) + amount);
                                if(inventoryItem.get("available_quantity") == null || "".equals(inventoryItem.get("total_quantity"))){
                                	inventoryItem.set("available_quantity", amount);
                                }else{
                                	inventoryItem.set("available_quantity",
                                            Double.parseDouble(inventoryItem.get("available_quantity").toString()) + amount);
                                }
                                
                                inventoryItem.update();
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * public void CreatReturnOrder() { boolean check =
     * CreatReturnOrder.CreatOrder(ReturnOrder.Depart_Order,
     * getPara("depart_id").toString()); renderJson(check); }
     */

    // TODO:应付list,查询条件（一张运输单，三张运输单，多种产品）
    public void accountPayable() {
        String id = getPara();
        if (id == null || id.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from depart_order_fin_item d "
                + "left join fin_item f on d.fin_item_id = f.id " + "where d.depart_order_id ='" + id
                + "' and f.type='应付'";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        DepartOrder depart =  DepartOrder.dao.findById(id);
        String charge_type = depart.get("charge_type");
        List<Record> record = Db.find("select tor.cargo_nature from transfer_order tor left join depart_transfer dt on tor.id = dt.order_id where dt.depart_id =?",id);
        String cargo_nature = record.get(0).get("cargo_nature");
        String conditionSql ="";
        if("ATM".equals(cargo_nature)){
        	if("perUnit".equals(charge_type)){
        		conditionSql =  " select d.*,"
    					+ " f.name,"
    					+ "  tor.order_no transfer_order_no,"
    					+ " (select count(id) as item_amount from transfer_order_item_detail where depart_id =d.depart_order_id and item_id = d.transfer_order_item_id) item_amount,"
    					+ " (select count(id) * p.volume as volume from transfer_order_item_detail where depart_id =d.depart_order_id and item_id = d.transfer_order_item_id) volume,"
    					+ " (select count(id) * p.weight as weight from transfer_order_item_detail where depart_id =d.depart_order_id and item_id = d.transfer_order_item_id) weight,"
    					+ " (select ci.amount from depart_order dor left join contract c on c.party_id = dor.sp_id left join contract_item ci on ci.contract_id = c.id where dor.id = d.depart_order_id and dor.charge_type = ci.pricetype and c.type ='SERVICE_PROVIDER' and dor.route_from = ci.from_id and dor.route_to = ci.to_id) price,"
    					+ " (select charge_type from depart_order where id = d.depart_order_id) fin_charge_type,"
    					+ " (select l.name from depart_order dor left join location l on dor.route_from = l.code where dor.id = d.depart_order_id) route_from,"
    					+ " (select lt.name from depart_order dor left join location lt on dor.route_to = lt.code where dor.id = d.depart_order_id) route_to"
    					+ " from depart_order_fin_item d "
    					+ " left join fin_item f on d.fin_item_id = f.id "
    					+ " left join transfer_order tor on tor.id = d.transfer_order_id"
    					+ " left join transfer_order_item tori on tori.id = d.transfer_order_item_id"
    					+ " left join product p on p.id = tori.product_id"
    					+ " where d.depart_order_id ="+ id +" and f.type='应付' and cost_source ='合同费用'";	
        	}else{
        		//整车和零担情况
        		conditionSql =  " select d.*,"
    					+ " f.name,"
    					+ " (select group_concat(tor.order_no separator '<br/>') from transfer_order tor left join depart_transfer dt on dt.order_id = tor.id where dt.depart_id =d.depart_order_id ) transfer_order_no,"
    					+ " (select count(id) as item_amount from transfer_order_item_detail where depart_id =d.depart_order_id) item_amount,"
    					+ " round((select sum(volume) from transfer_order_item_detail toid where toid.depart_id=d.depart_order_id),2)volume,"
    					+ " round((select sum(weight) from transfer_order_item_detail toid where toid.depart_id=d.depart_order_id),2) weight,"
    					+ " (select ci.amount from depart_order dor left join contract c on c.party_id = dor.sp_id left join contract_item ci on ci.contract_id = c.id where dor.id = d.depart_order_id and dor.charge_type = ci.pricetype and c.type ='SERVICE_PROVIDER' and dor.route_from = ci.from_id and dor.route_to = ci.to_id) price,"
    					+ " (select charge_type from depart_order where id = d.depart_order_id) fin_charge_type,"
    					+ " (select l.name from depart_order dor left join location l on dor.route_from = l.code where dor.id = d.depart_order_id) route_from,"
    					+ " (select lt.name from depart_order dor left join location lt on dor.route_to = lt.code where dor.id = d.depart_order_id) route_to"
    					+ " from depart_order_fin_item d "
    					+ " left join fin_item f on d.fin_item_id = f.id "
    					+ " left join transfer_order tor on tor.id = d.transfer_order_id"
    					+ " left join transfer_order_item tori on tori.id = d.transfer_order_item_id"
    					+ " left join product p on p.id = tori.product_id"
    					+ " where d.depart_order_id ="+ id +" and f.type='应付' and cost_source ='合同费用'";	
        	}
        }else{
        	conditionSql =  " select d.*,"
					+ " f.name,"
					+ " (select group_concat(tor.order_no separator '<br/>') from transfer_order tor left join depart_transfer dt on dt.order_id = tor.id where dt.depart_id =d.depart_order_id ) transfer_order_no,"
					+ " (select sum(toi.amount) from transfer_order_item toi left join depart_transfer dt on dt.order_id = toi.order_id where dt.depart_id = d.depart_order_id) item_amount,"
					+ " ifnull(nullif(tori.volume,0),p.volume) volume,"
					+ " ifnull(nullif(tori.weight,0),p.weight) weight,"
					+ " (select ci.amount from depart_order dor left join contract c on c.party_id = dor.sp_id left join contract_item ci on ci.contract_id = c.id where dor.id = d.depart_order_id and dor.charge_type = ci.pricetype and c.type ='SERVICE_PROVIDER' and dor.route_from = ci.from_id and dor.route_to = ci.to_id) price,"
					+ " (select charge_type from depart_order where id = d.depart_order_id) fin_charge_type,"
					+ " (select l.name from depart_order dor left join location l on dor.route_from = l.code where dor.id = d.depart_order_id) route_from,"
					+ " (select lt.name from depart_order dor left join location lt on dor.route_to = lt.code where dor.id = d.depart_order_id) route_to"
					+ " from depart_order_fin_item d "
					+ " left join fin_item f on d.fin_item_id = f.id "
					+ " left join transfer_order tor on tor.id = d.transfer_order_id"
					+ " left join transfer_order_item tori on tori.id = d.transfer_order_item_id"
					+ " left join product p on p.id = tori.product_id"
					+ " where d.depart_order_id ="+ id +" and f.type='应付' and cost_source ='合同费用'";	
        }
        
        
        String querySql = "select d.*,"
						+ " f.name,"
						+ " tor.order_no transfer_order_no,"
						+ " null item_amount,"
						+ " null volume,"
						+ " null weight,"
						+ " null price,"
						+ " null fin_charge_type,"
						+ " null route_from,"
						+ " null route_to"
						+ " from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id "
						+ " left join transfer_order tor on tor.id = d.transfer_order_id"
						+ " where d.depart_order_id =" + id
						+ " and f.type='应付' and cost_source ='运输单应付费用'"
						+ " union"
						+ conditionSql
						+ " union"
						+ " select d.*,"
						+ " f.name,"
						+ " null transfer_order_no,"
						+ " null item_amount,"
						+ " null volume,"
						+ " null weight,"
						+ " null price,"
						+ " null fin_charge_type,"
						+ " null route_from,"
						+ " null route_to"
						+ " from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id "
						+ " where d.depart_order_id ="+ id +" and f.type='应付' and cost_source is null";
       
        
        /*String querySql = "select d.*,"
		        		+ " f.name,"
		        		+ " tor.order_no transfer_order_no,"
		        		+ " ifnull(tori.item_name, p.item_name) item_name,"
		        		+ " null item_amount,"
		        		+ " null volume,"
		        		+ " null weight"
		        		+ " from depart_order_fin_item d "
		                + " left join fin_item f on d.fin_item_id = f.id "
		        		+ " left join transfer_order tor on tor.id = d.transfer_order_id"
						+ " left join transfer_order_item tori on tori.id = d.transfer_order_item_id"
						+ " left join product p on p.id = tori.product_id"
		        		+ " where d.depart_order_id =" + id
		                + " and f.type='应付'";*/
        // 获取当前页的数据
        List<Record> orders = Db.find(querySql);

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        /*List<Record> list = Db.find("select * from fin_item");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get("name") == null) {
                Fin_item.dao.deleteById(list.get(i).get("id"));
            }
        }*/
        renderJson(orderMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_ADD_COST})
    public void addNewRow() {
        
        List<FinItem> items = new ArrayList<FinItem>();
        String orderId = getPara();
        FinItem item = FinItem.dao.findFirst("select * from fin_item where type = '应付' order by id asc");
        if(item != null){
        	DepartOrderFinItem dFinItem = new DepartOrderFinItem();
	        dFinItem.set("status", "新建").set("fin_item_id", item.get("id"))
	        .set("depart_order_id", orderId).set("create_name", dFinItem.CREATE_NAME_USER)
	        .save();
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
        List<Record> list = Db.find("select * from fin_item");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get("name") == null) {
                FinItem.dao.deleteById(list.get(i).get("id"));
                List<Record> list2 = Db.find("select * from depart_order_fin_item where fin_item_id ='"
                        + list.get(i).get("id") + "'");
                List<Record> list3 = Db.find("select * from fin_item where id ='" + list2.get(0).get("fin_item_id")
                        + "'");
                if (list3.size() == 0) {
                    TransferOrderFinItem.dao.deleteById(list2.get(0).get("id"));
                }
            }
        }
        renderText(returnValue);
    }

    public void getPaymentList() {
        // String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        locationList = Db.find("select * from fin_item where type='应付'");
        renderJson(locationList);
    }    

    // 点击货品table的查看 ，显示货品对应的单品
    public void findAllItemDetail() {
        String itemId = getPara("item_id");
        String departId = getPara("departId");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String category = getPara("category");
        String sqlTotal = "select count(1) total from transfer_order_item_detail tod where tod.item_id = " + itemId
                + " and tod.depart_id = " + departId;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from transfer_order_item_detail tod where tod.item_id = " + itemId
                + " and tod.depart_id = " + departId;

        List<Record> details = Db.find(sql);

        Map productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        productListMap.put("aaData", details);
        renderJson(productListMap);
    }
    //修改应付
    public void updateDepartOrderFinItem(){
    	String paymentId = getPara("paymentId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if("amount".equals(name) && "".equals(value)){
    		value = "0";
    	}
    	if(paymentId != null && !"".equals(paymentId)){
    		DepartOrderFinItem departOrderFinItem = DepartOrderFinItem.dao.findById(paymentId);
    		departOrderFinItem.set(name, value);
    		
    		departOrderFinItem.update();
    	}
        renderJson("{\"success\":true}");
    }
    // 删除应付 
    public void finItemdel() {
        String id = getPara();
        DepartOrderFinItem.dao.deleteById(id);
        renderJson("{\"success\":true}");
    }
    //TODO：运输单带过来的费用
    public void getFinNoContractCost(DepartOrder departOrder){
    	List<TransferOrder> tran 
    			= TransferOrder.dao.find("select tor.* from transfer_order tor left join depart_transfer dt on dt.order_id = tor.id  where dt.depart_id = ?",departOrder.get("id"));
    	
    	List<TransferOrderFinItem> tofiList ;
    	if(tran.size()>0){
    		for (TransferOrder transferOrder : tran) {
					tofiList = TransferOrderFinItem.dao.find("select sum(amount) as total_cost,fin_item_id from transfer_order_fin_item where order_id = ? group by  fin_item_id",transferOrder.get("id"));
    				String name = (String) currentUser.getPrincipal();
				    UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
				    java.util.Date utilDate = new java.util.Date();
				    java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
				    if(tofiList.size()>0){
				    	for (TransferOrderFinItem transferOrderFinItem : tofiList) {
				    		DepartOrderFinItem departOrderFinItem = new DepartOrderFinItem();
				        	departOrderFinItem.set("fin_item_id", transferOrderFinItem.get("fin_item_id"));
				        	departOrderFinItem.set("amount", transferOrderFinItem.get("total_cost"));
				            departOrderFinItem.set("depart_order_id", departOrder.getLong("id"));
				            departOrderFinItem.set("status", "未完成");
				            departOrderFinItem.set("creator", users.get("id"));
				            departOrderFinItem.set("create_date", now);
				            departOrderFinItem.set("create_name", departOrderFinItem.CREATE_NAME_SYSTEM);
				            departOrderFinItem.set("transfer_order_id", transferOrder.get("id"));
				            //departOrderFinItem.set("transfer_order_item_id", toi.get("id"));
				            departOrderFinItem.set("cost_source", "运输单应付费用");
				            departOrderFinItem.save();
					}
				}
					
			}
    	}
        	
    }
}
