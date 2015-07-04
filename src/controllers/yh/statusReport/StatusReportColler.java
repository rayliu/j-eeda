package controllers.yh.statusReport;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.Party;
import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.order.TransferOrderController;
import controllers.yh.util.PermissionConstant;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class StatusReportColler extends Controller{
	
	private Logger logger = Logger.getLogger(TransferOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	public void index() {		
		render("/yh/statusReport/productStatusReport.html");
	}
	
	public void productIndex() {		
		render("/yh/statusReport/productStatusReport.html");
	}
	
	public void orderIndex() {		
		render("/yh/statusReport/orderStatusReport.html");
	}
	
	//运营日报表
	public void dailyReport() {		
		render("/yh/statusReport/dailyReport.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_PRODUCTINDEX_LIST})
	public void productStatus() {
		String order_no = getPara("order_no");
		String customer_id = getPara("customer_id");
		String customer_order_no = getPara("customer_order_no");
		String item_no = getPara("item_no");
		String serial_no = getPara("serial_no");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String sLimit = "";
		
		Map orderMap = new HashMap();
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}
		
		if((serial_no != null && !"".equals(serial_no)) || (beginTime != null && !"".equals(beginTime)) || (endTime != null && !"".equals(endTime))){
			if (beginTime == null || "".equals(beginTime)) 
				beginTime = "1-1-1";
			if (endTime == null || "".equals(endTime)) 
				endTime = "9999-12-31";
			// 获取总条数
			String totalSql = "select count(0) total from transfer_order_item_detail toid"
						+ " left join transfer_order tor on tor.id = toid.order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join depart_transfer pick on pick.id = toid.pickup_id"
						+ " left join depart_order pkdo on pkdo.id = toid.pickup_id"
						+ " left join depart_transfer depart on depart.id = toid.depart_id"
						+ " left join depart_order dedo on dedo.id = toid.depart_id"
						+ " left join delivery_order dor on dor.id = toid.delivery_id"
						+ " left join return_order ro on ro.delivery_order_id = dor.id"
						+ " left join warehouse w on w.id = tor.warehouse_id"
						+ " where tor.cargo_nature_detail = 'cargoNatureDetailYes' and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
						+ " and tor.customer_id in  (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
				
			String sql = "select toid.id, toid.serial_no,item_no,c.abbr customer,toid.notify_party_company,tor.status transfer_status,pkdo.status pick_status,dedo.status depart_status,ro.transaction_status,"
						+ " dor.status delivery_status,tor.planning_time,tor.customer_order_no,tor.order_no transfer_no,w.warehouse_name,dor.order_no delivery_no,ro.create_date return_stamp, "
						+ " (select create_stamp from transfer_order_milestone where depart_id = dedo.id and status = '已发车') warehouse_stamp,"
						+ " (select create_stamp from delivery_order_milestone where delivery_id = dor.id and status = '已发车') delivery_stamp"
						+ " from transfer_order_item_detail toid "
						+ " left join transfer_order tor on tor.id = toid.order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join depart_transfer pick on pick.id = toid.pickup_id"
						+ " left join depart_order pkdo on pkdo.id = toid.pickup_id"
						+ " left join depart_transfer depart on depart.id = toid.depart_id"
						+ " left join depart_order dedo on dedo.id = toid.depart_id"
						+ " left join delivery_order dor on dor.id = toid.delivery_id"
						+ " left join return_order ro on ro.delivery_order_id = dor.id"
						+ " left join warehouse w on w.id = tor.warehouse_id"
						+ " where tor.cargo_nature_detail = 'cargoNatureDetailYes' and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
						+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
							
			//有序列号时
			if(!"".equals(serial_no) && serial_no != null){
				totalSql = totalSql + " and toid.serial_no = '" + serial_no + "'";
				sql = sql + " and toid.serial_no = '" + serial_no + "'";
			}
			
			//计划时间段
			if(beginTime != null && !"".equals(beginTime) && endTime != null && !"".equals(endTime)){
				totalSql = totalSql + " and tor.planning_time between '" + beginTime + "' and '" + endTime + "'";
				sql = sql + " and tor.planning_time between '" + beginTime + "' and '" + endTime + "'";
			}
			
			//有运输单号时
			if(!"".equals(order_no) && order_no != null){
				totalSql = totalSql + " and tor.order_no = '" + order_no + "'";
				sql = sql + " and tor.order_no = '" + order_no + "'";
			}
			
			//有客户订单号时
			if(!"".equals(customer_order_no) && customer_order_no != null){
				totalSql = totalSql + " and tor.customer_order_no = '" + customer_order_no + "'";
				sql = sql + " and tor.customer_order_no = '" + customer_order_no + "'";
			}
			
			//有货品型号时
			if(!"".equals(item_no) && item_no != null){
				totalSql = totalSql + "and toid.item_no = '" + item_no + "'";
				sql = sql + "and toid.item_no = '" + item_no + "'";
			}
			//有客户时
			if(!"".equals(customer_id) && customer_id != null){
				totalSql = totalSql + "and tor.customer_id = '" + customer_id + "'";
				sql = sql + "and tor.customer_id = '" + customer_id + "'";
			}
			
			// 获取总条数
			Record rec = Db.findFirst(totalSql);
			logger.debug("total records:" + rec.getLong("total"));
			// 获取当前页的数据
			List<Record> orders =Db.find(sql + sLimit);
			orderMap.put("sEcho", pageIndex);
			orderMap.put("iTotalRecords", rec.getLong("total"));
			orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
			orderMap.put("aaData", orders);
		}else{
			orderMap.put("sEcho", 0);
			orderMap.put("iTotalRecords", 0);
			orderMap.put("iTotalDisplayRecords", 0);
			orderMap.put("aaData", null);
		}
		renderJson(orderMap);
	}
	
	public void findAllTransferOrderNo(){
		String locationName = getPara("locationName");
	        
        List<Record> locationList = Collections.EMPTY_LIST;
        if (locationName.trim().length() > 0) {
            locationList = Db
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER' and c.company_name like '%"+locationName+"%' and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')");

        } else {
            locationList = Db.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER'  and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')");
        }
        renderJson(locationList);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_ORDERINDEX_LIST})
	public void orderStatusReport() {
		String orderNoType = getPara("order_no_type");
		String orderNo = getPara("order_no");
		String orderStatusType = getPara("order_status_type");
		String transferOrderStatus = getPara("transferOrder_status");
		String deliveryStatus = getPara("delivery_status");
		String setOutTime = getPara("setOutTime");
		String customerId = getPara("customer_id");
		String routeFrom = getPara("routeFrom");
		String beginTime = getPara("beginTime");
		String sp_id = getPara("sp_id");
		String routeTo = getPara("routeTo");
		String endTime = getPara("endTime");
		String sLimit = "";
		
		Map orderMap = new HashMap();
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}
		
		if("transferOrder".equals(orderNoType) && "transferOrderStatus".equals(orderStatusType)){
			// 获取总条数
			String totalSql = "select count(0) total from transfer_order tor"
					+ " left join depart_transfer tr on tr.order_id = tor.id "
					+ " left join depart_order dor on dor.id = tr.depart_id"
					+ " left join delivery_order_item doi on doi.transfer_order_id = tor.id"
					+ " left join delivery_order deo on deo.id = doi.delivery_id"
					+ " left join return_order ror on ror.transfer_order_id = tor.id"
					+ " left join party p1 on p1.id = tor.customer_id"
					+ " left join contact c1 on c1.id = p1.contact_id"
					+ " left join party p2 on p2.id = tor.sp_id"
					+ " left join contact c2 on c2.id = p2.contact_id"
					+ " left join location l1 on tor.route_from = l1.code "
					+ " left join location l2 on tor.route_to = l2.code"
					+ " left join office o on o.id = tor.office_id"
					+ " where dor.combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"'"
					+ " and p1.party_type = '"+Party.PARTY_TYPE_CUSTOMER+"'"
					+ " and p2.party_type = '"+Party.PARTY_TYPE_SERVICE_PROVIDER+"' and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
					+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
				
			String sql = "select tor.order_no,'运输单' as order_category,dor.depart_no,tor.order_type,c1.abbr,o.office_name,tor.planning_time,dor.departure_time,l1.name route_from,l2.name route_to,"
					+ " (select case when ror.id is not null and ror.transaction_status != '新建' then '回单签收' "
					+ " when tor.depart_assign_status = 'ALL' and (dor.status = '已入库' or dor.status = '已收货') then '到达签收' "
					+ " when tor.depart_assign_status = 'ALL' and dor.status = '已发车' then '运输在途' "
					+ " when tor.depart_assign_status = 'PARTIAL' and dor.status = '已发车' then '部分在途' else '新建运输' end) as order_status "
					+ " from transfer_order tor"
					+ " left join depart_transfer tr on tr.order_id = tor.id "
					+ " left join depart_order dor on dor.id = tr.depart_id"
					+ " left join delivery_order_item doi on doi.transfer_order_id = tor.id"
					+ " left join delivery_order deo on deo.id = doi.delivery_id"
					+ " left join return_order ror on ror.transfer_order_id = tor.id"
					+ " left join party p1 on p1.id = tor.customer_id"
					+ " left join contact c1 on c1.id = p1.contact_id"
					+ " left join party p2 on p2.id = tor.sp_id"
					+ " left join contact c2 on c2.id = p2.contact_id"
					+ " left join location l1 on tor.route_from = l1.code "
					+ " left join location l2 on tor.route_to = l2.code"
					+ " left join office o on o.id = tor.office_id"
					+ " where dor.combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"'"
					+ " and p1.party_type = '"+Party.PARTY_TYPE_CUSTOMER+"'"
					+ " and p2.party_type = '"+Party.PARTY_TYPE_SERVICE_PROVIDER+"' and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
						+ " and tor.customer_id in  (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
			//有外发日期
			if(!"".equals(setOutTime) && setOutTime != null){
				totalSql = totalSql + " and dor.departure_time = '" + setOutTime + "'";
				sql = sql + " and dor.departure_time = '" + setOutTime + "'";
			}
			
			//计划时间段
			if(beginTime != null && !"".equals(beginTime) && endTime != null && !"".equals(endTime)){
				totalSql = totalSql + " and tor.planning_time between '" + beginTime + "' and '" + endTime + "'";
				sql = sql + " and tor.planning_time between '" + beginTime + "' and '" + endTime + "'";
			}
			
			//有运输单号时
			if(!"".equals(orderNo) && orderNo != null){
				totalSql = totalSql + " and tor.order_no = '" + orderNo + "'";
				sql = sql + " and tor.order_no = '" + orderNo + "'";
			}
			
			//始发地
			if(!"".equals(routeFrom) && routeFrom != null){
				totalSql = totalSql + " and l1.name = '" + routeFrom + "'";
				sql = sql + " and l1.name = '" + routeFrom + "'";
			}
			
			//目的地
			if(!"".equals(routeTo) && routeTo != null){
				totalSql = totalSql + " and l2.name = '" + routeTo + "'";
				sql = sql + " and l2.name = '" + routeTo + "'";
			}
			
			//有供应商
			if(!"".equals(sp_id) && sp_id != null){
				totalSql = totalSql + "and tor.sp_id = '" + sp_id + "'";
				sql = sql + "and tor.sp_id = '" + sp_id + "'";
			}
			//有客户时
			if(!"".equals(customerId) && customerId != null){
				totalSql = totalSql + "and tor.customer_id = '" + customerId + "'";
				sql = sql + "and tor.customer_id = '" + customerId + "'";
			}
			
			//运输单状态
			if(!"".equals(transferOrderStatus) && transferOrderStatus != null){
				if("NEW".equals(transferOrderStatus)){
					totalSql = totalSql + " and tor.status = '新建' ";
					sql = sql + " and tor.status = '新建' ";
				}else if("PARTIAL".equals(transferOrderStatus)){
					totalSql = totalSql + " and tor.depart_assign_status = 'PARTIAL' and dor.status = '已发车' ";
					sql = sql + " and tor.depart_assign_status = 'PARTIAL' and dor.status = '已发车' ";
				}else if("ALL".equals(transferOrderStatus)){
					totalSql = totalSql + " and tor.depart_assign_status = 'ALL' and dor.status = '已发车' ";
					sql = sql + " and tor.depart_assign_status = 'ALL' and dor.status = '已发车' ";
				}else if("DELIVERY".equals(transferOrderStatus)){
					totalSql = totalSql + " and (dor.status = '已入库' or dor.status = '已收货') ";
					sql = sql + " and (dor.status = '已入库' or dor.status = '已收货') ";
				}else if("RETURN".equals(transferOrderStatus)){
					totalSql = totalSql + " and ror.transaction_status != '新建' ";
					sql = sql + " and ror.transaction_status != '新建' ";
				}
			}
			
			// 获取总条数
			//Record rec = Db.findFirst(totalSql + sLimit);
			//logger.debug("total records:" + rec.getLong("total"));
			// 获取当前页的数据
			List<Record> orders =Db.find(sql + sLimit);
			orderMap.put("sEcho", pageIndex);
			//orderMap.put("iTotalRecords", rec.getLong("total"));
			//orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
			orderMap.put("iTotalRecords", orders.size());
			orderMap.put("iTotalDisplayRecords", orders.size());
			orderMap.put("aaData", orders);
		}else if("deliveryOrder".equals(orderNoType) && "deliveryStatus".equals(orderStatusType)){
			// 获取总条数
			String totalSql = "select distinct count(0) total from delivery_order deo"
					+ " left join delivery_order_item doi on doi.delivery_id = deo.id"
					+ " left join delivery_order_milestone dom on dom.delivery_id = deo.id"
					+ " left join transfer_order tor on tor.id = doi.transfer_order_id"
					+ " left join return_order ror on ror.delivery_order_id = deo.id"
					+ " left join party p1 on p1.id = deo.customer_id"
					+ " left join contact c1 on c1.id = p1.contact_id"
					+ " left join party p2 on p2.id = deo.sp_id"
					+ " left join contact c2 on c2.id = p2.contact_id"
					+ " left join location l1 on deo.route_from = l1.code "
					+ " left join location l2 on deo.route_to = l2.code"
					+ " left join office o on o.id = tor.office_id"
					+ " left join warehouse w on deo.from_warehouse_id = w.id "
					+ " where p1.party_type = '"+Party.PARTY_TYPE_CUSTOMER+"'"
					+ " and p2.party_type = '"+Party.PARTY_TYPE_SERVICE_PROVIDER+"' and w.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
					+ " and deo.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
				
			String sql = "select distinct deo.order_no,'配送单' as order_category,'' as depart_no,tor.order_type,c1.abbr,o.office_name,tor.planning_time,l1.name route_from,l2.name route_to,"
					+ " (select create_stamp from delivery_order_milestone where delivery_id = deo.id and status = '已发车') as departure_time,"
					+ " (select case when ror.id is not null and ror.transaction_status != '新建' then '回单签收' "
					+ " when ror.id is not null and ror.transaction_status = '新建' then '配送到达'"
					+ " when deo.status = '已发车' then '配送在途' "
					+ " when deo.status = '新建' then '新建配送' end ) as order_status "
					+ " from delivery_order deo"
					+ " left join delivery_order_item doi on doi.delivery_id = deo.id"
					+ " left join delivery_order_milestone dom on dom.delivery_id = deo.id"
					+ " left join transfer_order tor on tor.id = doi.transfer_order_id"
					+ " left join return_order ror on ror.delivery_order_id = deo.id"
					+ " left join party p1 on p1.id = deo.customer_id"
					+ " left join contact c1 on c1.id = p1.contact_id"
					+ " left join party p2 on p2.id = deo.sp_id"
					+ " left join contact c2 on c2.id = p2.contact_id"
					+ " left join location l1 on deo.route_from = l1.code "
					+ " left join location l2 on deo.route_to = l2.code"
					+ " left join office o on o.id = tor.office_id"
					+ " left join warehouse w on deo.from_warehouse_id = w.id "
					+ " where p1.party_type = '"+Party.PARTY_TYPE_CUSTOMER+"'"
					+ " and p2.party_type = '"+Party.PARTY_TYPE_SERVICE_PROVIDER+"' and w.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
							+ " and deo.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
			//有外发日期
			if(!"".equals(setOutTime) && setOutTime != null){
				totalSql = totalSql + " and and (dom.status != '新建' and to_days(dom.create_stamp) = to_days('" + setOutTime + "'))";
				sql = sql + " and and (dom.status != '新建' and to_days(dom.create_stamp) = to_days('" + setOutTime + "'))";
			}
			
			//计划时间段
			if(beginTime != null && !"".equals(beginTime) && endTime != null && !"".equals(endTime)){
				totalSql = totalSql + " and tor.planning_time between '" + beginTime + "' and '" + endTime + "'";
				sql = sql + " and tor.planning_time between '" + beginTime + "' and '" + endTime + "'";
			}
			
			//有配送单号时
			if(!"".equals(orderNo) && orderNo != null){
				totalSql = totalSql + " and deo.order_no = '" + orderNo + "'";
				sql = sql + " and deo.order_no = '" + orderNo + "'";
			}
			
			//始发地
			if(!"".equals(routeFrom) && routeFrom != null){
				totalSql = totalSql + " and l1.name = '" + routeFrom + "'";
				sql = sql + " and l1.name = '" + routeFrom + "'";
			}
			
			//目的地
			if(!"".equals(routeTo) && routeTo != null){
				totalSql = totalSql + " and l2.name = '" + routeTo + "'";
				sql = sql + " and l2.name = '" + routeTo + "'";
			}
			
			//有供应商
			if(!"".equals(sp_id) && sp_id != null){
				totalSql = totalSql + "and deo.sp_id = '" + sp_id + "'";
				sql = sql + "and deo.sp_id = '" + sp_id + "'";
			}
			//有客户时
			if(!"".equals(customerId) && customerId != null){
				totalSql = totalSql + "and deo.customer_id = '" + customerId + "'";
				sql = sql + "and deo.customer_id = '" + customerId + "'";
			}
			
			//配送单状态
			if(!"".equals(transferOrderStatus) && transferOrderStatus != null){
				if("NEW".equals(transferOrderStatus)){
					totalSql = totalSql + " and deo.status = '新建' ";
					sql = sql + " and deo.status = '新建' ";
				}else if("ALL".equals(transferOrderStatus)){
					totalSql = totalSql + " and deo.status = '已发车' ";
					sql = sql + " and deo.status = '已发车' ";
				}else if("DELIVERY".equals(transferOrderStatus)){
					totalSql = totalSql + " and deo.status != '已签收' ";
					sql = sql + " and deo.status != '已签收' ";
				}else if("RETURN".equals(transferOrderStatus)){
					totalSql = totalSql + " and ror.transaction_status != '新建' ";
					sql = sql + " and ror.transaction_status != '新建' ";
				}
			}
			
			// 获取总条数
			//Record rec = Db.findFirst(totalSql + sLimit);
			//logger.debug("total records:" + rec.getLong("total"));
			// 获取当前页的数据
			List<Record> orders =Db.find(sql + sLimit);
			orderMap.put("sEcho", pageIndex);
			//orderMap.put("iTotalRecords", rec.getLong("total"));
			//orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
			orderMap.put("iTotalRecords", orders.size());
			orderMap.put("iTotalDisplayRecords", orders.size());
			orderMap.put("aaData", orders);
		}else{
			orderMap.put("sEcho", 0);
			orderMap.put("iTotalRecords", 0);
			orderMap.put("iTotalDisplayRecords", 0);
			orderMap.put("aaData", null);
		}
		renderJson(orderMap);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_DAILYREPORT_LIST})
	public void dailyReportStatus() {
		String orderNo = getPara("order_no");
		String customerId = getPara("customer_id");
		String customerOrderNo = getPara("customer_order_no");
		String itemNo = getPara("item_no");
		String serialNo = getPara("serial_no");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String cargoType = getPara("cargoType");
		String sLimit = "";
		
		Map orderMap = new HashMap();
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}
		//List<FinItem> payName = FinItem.dao.find("select name,code from fin_item where type = '应付';");
		//List<FinItem> incomeName = FinItem.dao.find("select name,code from fin_item where type = '应收';");
		if(beginTime != null && !"".equals(beginTime) && endTime != null && !"".equals(endTime)){
			if("ATM".equals(cargoType)){
				
				// 获取总条数
				String totalSql = "select count(0) total from transfer_order_item_detail toid"
						+ " left join transfer_order tor on tor.id = toid.order_id"
						+ " left join delivery_order dor on dor.id = toid.delivery_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join location l1 on tor.route_from = l1.code "
						+ " left join location l2 on tor.route_to = l2.code"
						+ " where tor.planning_time between '" + beginTime + "' and '" + endTime + "' and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
								+ "  and tor.customer_id in  (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
						//+ " group by toid.id "
						
					
				String sql = "select c.abbr,dor.order_no deliveryno,tor.order_no transferno,toid.serial_no,tor.planning_time,l1.name route_from,l2.name route_to,toid.pieces,round(toid.weight,2) weight,round(toid.volume,2) volume,"
						+ " (select case when ((select transaction_status from return_order where transfer_order_id = toid.order_id) != '新建') or ((select transaction_status from return_order where delivery_Order_id = toid.delivery_id) != '新建')  then '回单签收' "
						+ " when ((select transaction_status from return_order where transfer_order_id = toid.order_id) = '新建') or ((select transaction_status from return_order where delivery_Order_id = toid.delivery_id) = '新建')  then '新建回单' "
						+ " when (select status from delivery_order where id = toid.delivery_id) != '新建' then '配送发车' when (select status from delivery_order where id = toid.delivery_id) = '新建' then '新建配送' "
						+ " when (select status from depart_order where id = toid.depart_id) = '已收货' then '已收货' when (select status from depart_order where id = toid.depart_id) = '已入库' then '已入库' "
						+ " when (select status from depart_order where id = toid.depart_id) = '已发车' then '运输在途' when (select status from depart_order where id = toid.depart_id) = '新建' then '新建发车'"
						+ " when (select status from depart_order where id = toid.pickup_id) != '新建' then '已入货场'when (select status from depart_order where id = toid.pickup_id) = '新建' then '新建提货' else '新建运输' end) as status,"
						+ " (select case when tor.order_type = 'salesOrder' then '销售订单' when tor.order_type = 'cargoReturnOrder' then '退货订单' when tor.order_type = 'movesOrder' then '移机订单'  end ) as order_type,"
						+ " (select datediff(ifnull((select create_stamp from delivery_order_milestone where delivery_id = toid.delivery_id and status = '已发车'),curdate()),(select create_stamp from transfer_order_milestone where depart_id = toid.depart_id and type = 'DEPARTORDERMILESTONE' and status = '已发车'))) as warehousenumber,"
						
				/*for (FinItem finItem : payName) {
					sql += " ((select ifnull(sum(d.amount),0) / (select sum(amount) from transfer_order_item where order_id = toid.order_id) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.order_id and f.type = '应付' and f.name = '" + finItem.get("name") + "') + "
							+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) end) from pickup_order_fin_item d "
							+ " left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id = toid.pickup_id and f.type = '应付' and f.name = '" + finItem.get("name") + "') +"
							+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) end) from depart_order_fin_item d "
							+ " left join fin_item f on d.fin_item_id = f.id where d.depart_order_id = toid.depart_id and f.type = '应付' and f.name = '" + finItem.get("name") + "') +"
							+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) from  delivery_order_fin_item d "
							+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.delivery_id  and f.type = '应付' and f.name = '" + finItem.get("name") + "')) as pay" + finItem.get("code") + ",";
				}*/
						//应付-成本
						+ " ((select ifnull(sum(d.amount),0)/(select sum(amount) from transfer_order_item where order_id = toid.order_id) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.order_id and f.type = '应付' and f.name = '提货费') + "
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) end) from pickup_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id = toid.pickup_id and f.type = '应付' and f.name = '提货费') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) end) from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.depart_order_id = toid.depart_id and f.type = '应付' and f.name = '提货费') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) from  delivery_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.delivery_id  and f.type = '应付' and f.name = '提货费')) as paytihuo,"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) end) from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.depart_order_id = toid.depart_id and f.type = '应付' and f.name = '运输费') as payganxian,"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) from  delivery_order_fin_item d "
						+ " left join  fin_item f on d.fin_item_id = f.id where d.order_id = toid.delivery_id  and f.type = '应付' and f.name = '运输费') as paypeisong,"
						
						+ " ((select ifnull(sum(d.amount),0) / (select sum(amount) from transfer_order_item where order_id = toid.order_id) from transfer_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.order_id and f.type = '应付' and f.name = '安装费') + "
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) end) from pickup_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id = toid.pickup_id and f.type = '应付' and f.name = '安装费') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) end) from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.depart_order_id = toid.depart_id and f.type = '应付' and f.name = '安装费') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) from  delivery_order_fin_item d "
						+ " left join  fin_item f on d.fin_item_id = f.id where d.order_id = toid.delivery_id  and f.type = '应付' and f.name = '安装费')) as payanzhuang,"
						+ " ((select ifnull(sum(d.amount),0) / (select sum(amount) from transfer_order_item where order_id = toid.order_id) from transfer_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.order_id and f.type = '应付' and f.name = '台阶费') + "
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) end) from pickup_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id = toid.pickup_id and f.type = '应付' and f.name = '台阶费') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) end) from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.depart_order_id = toid.depart_id and f.type = '应付' and f.name = '台阶费') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) from  delivery_order_fin_item d "
						+ " left join  fin_item f on d.fin_item_id = f.id where d.order_id = toid.delivery_id  and f.type = '应付' and f.name = '台阶费')) as paytaijie,"
						+ " ((select ifnull(sum(d.amount),0) / (select sum(amount) from transfer_order_item where order_id = toid.order_id) from transfer_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.order_id and f.type = '应付' and f.name = '等待费') + "
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) end) from pickup_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id = toid.pickup_id and f.type = '应付' and f.name = '等待费') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) end) from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.depart_order_id = toid.depart_id and f.type = '应付' and f.name = '等待费') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) from  delivery_order_fin_item d "
						+ " left join  fin_item f on d.fin_item_id = f.id where d.order_id = toid.delivery_id  and f.type = '应付' and f.name = '等待费')) as paydengdai,"
						+ " ((select ifnull(sum(d.amount),0) / (select sum(amount) from transfer_order_item where order_id = toid.order_id) from transfer_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.order_id and f.type = '应付' and f.name = '暂存费') + "
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) end) from pickup_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id = toid.pickup_id and f.type = '应付' and f.name = '暂存费') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) end) from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.depart_order_id = toid.depart_id and f.type = '应付' and f.name = '暂存费') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) from  delivery_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.delivery_id  and f.type = '应付' and f.name = '暂存费')) as payzancun,"
						+ " ((select ifnull(sum(d.amount),0) / (select sum(amount) from transfer_order_item where order_id = toid.order_id) from transfer_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.order_id and f.type = '应付' and f.name = '其他费用') + "
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) end) from pickup_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id = toid.pickup_id and f.type = '应付' and f.name = '其他费用') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) end) from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.depart_order_id = toid.depart_id and f.type = '应付' and f.name = '其他费用') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) from  delivery_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.delivery_id  and f.type = '应付' and f.name = '其他费用')) as payqita,"
						+ " (select round(ifnull(amount,0)*ifnull(rate,0),2) from insurance_fin_item d left join fin_item f on d.fin_item_id = f.id	where	d.transfer_order_item_id = toid.item_id and f.type = '应付' and f.name = '保险费') as paybaoxian,"
				
				/*for (FinItem finItem : incomeName) {
					sql += " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) from return_order_fin_item d "
							+ " left join fin_item f on d.fin_item_id = f.id where d.delivery_order_id = toid.delivery_id and f.type = '应收' and f.name = '" + finItem.get("name") + "') as income" + finItem.get("code") + ",";
				}*/
						//应收-收入
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from delivery_order_item where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) "
						+ " from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join return_order ror on ror.id = d.return_order_id where ror.delivery_order_id = toid.delivery_id and f.type = '应收' and f.name = '提货费') as incometihuo,"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from delivery_order_item where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) "
						+ " from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join return_order ror on ror.id = d.return_order_id where ror.delivery_order_id = toid.delivery_id and f.type = '应收' and f.name = '运输费') as incomeyunshu,"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from delivery_order_item where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) "
						+ " from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join return_order ror on ror.id = d.return_order_id where ror.delivery_order_id = toid.delivery_id and f.type = '应收' and f.name = '送货费') as incomesonghuo,"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from delivery_order_item where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) "
						+ " from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join return_order ror on ror.id = d.return_order_id where ror.delivery_order_id = toid.delivery_id and f.type = '应收' and f.name = '台阶费') as incometaijie,"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from delivery_order_item where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) "
						+ " from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join return_order ror on ror.id = d.return_order_id where ror.delivery_order_id = toid.delivery_id and f.type = '应收' and f.name = '安装费') as incomeanzhuang,"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from delivery_order_item where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) "
						+ " from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join return_order ror on ror.id = d.return_order_id where ror.delivery_order_id = toid.delivery_id and f.type = '应收' and f.name = '暂存费') as incomezancun,"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from delivery_order_item where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) "
						+ " from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join return_order ror on ror.id = d.return_order_id where ror.delivery_order_id = toid.delivery_id and f.type = '应收' and f.name = '其它费用') as incomeqita,"
						+ " (select round(ifnull(amount,0)*ifnull(income_rate,0),2) from insurance_fin_item d where	d.transfer_order_item_id = toid.item_id) as incomebaoxian,"//应收保险费
						
						//运作毛利
						+ " ((select ifnull(sum(d.amount),0)/(select case when (select count(0) from delivery_order_item where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) "
						+ " from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join return_order ror on ror.id = d.return_order_id where ror.delivery_order_id = toid.delivery_id and f.type = '应收') - "
						+ " ((select ifnull(sum(d.amount),0)/(select sum(amount) from transfer_order_item where order_id = toid.order_id) from transfer_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.order_id and f.type = '应付') + "
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where pickup_id = toid.pickup_id and order_id = toid.order_id) end) from pickup_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id = toid.pickup_id and f.type = '应付') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where depart_id = toid.depart_id and order_id = toid.order_id) end) from depart_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.depart_order_id = toid.depart_id and f.type = '应付') +"
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) from  delivery_order_fin_item d "
						+ " left join fin_item f on d.fin_item_id = f.id where d.order_id = toid.delivery_id  and f.type = '应付') +"
						+ " (select round(ifnull(amount,0)*ifnull(rate,0),2) from insurance_fin_item d left join fin_item f on d.fin_item_id = f.id	where	d.transfer_order_item_id = toid.item_id and f.type = '应付' and f.name = '保险费'))) as yunzuomaoli,"
						//总收入
						+ " (select ifnull(sum(d.amount),0)/(select case when (select count(0) from delivery_order_item where delivery_id = toid.delivery_id and order_id = toid.order_id) = 0 then 1 else (select count(0) from transfer_order_item_detail where delivery_id = toid.delivery_id and order_id = toid.order_id) end) "
						+ " from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join return_order ror on ror.id = d.return_order_id where ror.delivery_order_id = toid.delivery_id and f.type = '应收') as zongshouru"
						+ " from transfer_order_item_detail toid"
						+ " left join transfer_order tor on tor.id = toid.order_id"
						+ " left join delivery_order dor on dor.id = toid.delivery_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join location l1 on tor.route_from = l1.code "
						+ " left join location l2 on tor.route_to = l2.code"
						+ " where tor.planning_time between '" + beginTime + "' and '" + endTime + "' and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
								+ "  and tor.customer_id in  (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
						//+ " group by toid.id "
						
				
				//有运输单号时
				if(!"".equals(orderNo) && orderNo != null){
					totalSql = totalSql + " and tor.order_no = '" + orderNo + "'";
					sql = sql + " and tor.order_no = '" + orderNo + "'";
				}
				
				//有客户时
				if(!"".equals(customerId) && customerId != null){
					totalSql = totalSql + "and tor.customer_id = '" + customerId + "'";
					sql = sql + "and tor.customer_id = '" + customerId + "'";
				}
				
				totalSql+= " order by tor.planning_time desc";
				sql += " order by tor.planning_time desc";
				
				// 获取总条数
				Record rec = Db.findFirst(totalSql);
				logger.debug("total records:" + rec.getLong("total"));
				// 获取当前页的数据
				List<Record> orders =Db.find(sql + sLimit);
				
				
				StringBuffer resultData = new StringBuffer();
				resultData.append("[");
				for (Record record : orders) {
					resultData.append(record.toJson()+",");
				}
				resultData.delete(resultData.length()-1, resultData.length());
				resultData.append("]");
				System.out.println("data:"+resultData.toString());
				
				
				orderMap.put("sEcho", pageIndex);
				orderMap.put("iTotalRecords", rec.getLong("total"));
				orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
				//orderMap.put("aaData", resultData.toString());
				orderMap.put("aaData", orders);
			}else{
				
				String totalSql = " select count(0) total from transfer_order tor"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join location l1 on tor.route_from = l1.code "
						+ " left join location l2 on tor.route_to = l2.code"
						+ " where tor.planning_time between '" + beginTime + "' and '" + endTime + "'"
						+ " and tor.cargo_nature_detail = 'cargoNatureDetailNo'"
						+ " and tor.cargo_nature = 'cargo'  and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
								+ "  and tor.customer_id in  (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
				
				
				
				String sql = "select c.abbr,(select '') deliveryno,tor.order_no transferno,(select '') serial_no,tor.status,tor.planning_time,l1.name route_from,l2.name route_to,(select '') warehousenumber,(select '') pieces,"
						+ " (select case when tor.order_type = 'salesOrder' then '销售订单' when tor.order_type = 'cargoReturnOrder' then '退货订单' when tor.order_type = 'movesOrder' then '移机订单'  end ) as order_type,"
						+ " (select round(sum(ifnull(sum_weight,0)),2) from transfer_order_item where order_id = tor.id) weight,(select round(sum(ifnull(volume,0)),2) from transfer_order_item where order_id = tor.id) volume,"
						
						
				/*for (FinItem finItem : payName) {
					sql += " ((select ifnull(sum(d.amount),0) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = tor.id and f.type = '应付' and f.name = '" + finItem.get("name") + "') + "
							+ " (select ifnull(sum(d.amount),0) from pickup_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id in (select pickup_id from depart_transfer where depart_id is null and order_id = tor.id) and f.type = '应付' and f.name = '" + finItem.get("name") + "') +"
							+ " (select ifnull(sum(d.amount),0) from depart_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.depart_order_id in (select depart_id from depart_transfer where pickup_id is null and order_id = tor.id) and f.type = '应付' and f.name = '" + finItem.get("name") + "') +"
							+ " (select ifnull(sum(d.amount),0) from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id where d.order_id in (select delivery_id from delivery_order_item where transfer_order_id = tor.id) and f.type = '应付' and f.name = '" + finItem.get("name") + "')) as pay" + finItem.get("code") + ",";
				}*/
						//成本-应付
						+ " ((select ifnull(sum(d.amount),0) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = tor.id and f.type = '应付' and f.name = '提货费') + "
						+ " (select ifnull(sum(d.amount),0) from pickup_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id in (select pickup_id from depart_transfer where depart_id is null and order_id = tor.id) and f.type = '应付' and f.name = '提货费') +"
						+ " (select ifnull(sum(d.amount),0) from depart_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.depart_order_id in (select depart_id from depart_transfer where pickup_id is null and order_id = tor.id) and f.type = '应付' and f.name = '提货费') +"
						+ " (select ifnull(sum(d.amount),0) from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id where d.order_id in (select delivery_id from delivery_order_item where transfer_order_id = tor.id) and f.type = '应付' and f.name = '提货费')) as paytihuo,"
						+ " ((select ifnull(sum(d.amount),0) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = tor.id and f.type = '应付' and f.name = '安装费') + "
						+ " (select ifnull(sum(d.amount),0) from pickup_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id in (select pickup_id from depart_transfer where depart_id is null and order_id = tor.id) and f.type = '应付' and f.name = '安装费') +"
						+ " (select ifnull(sum(d.amount),0) from depart_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.depart_order_id in (select depart_id from depart_transfer where pickup_id is null and order_id = tor.id) and f.type = '应付' and f.name = '安装费') +"
						+ " (select ifnull(sum(d.amount),0) from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id where d.order_id in (select delivery_id from delivery_order_item where transfer_order_id = tor.id) and f.type = '应付' and f.name = '安装费')) as payanzhuang,"
						+ " ((select ifnull(sum(d.amount),0) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = tor.id and f.type = '应付' and f.name = '台阶费') + "
						+ " (select ifnull(sum(d.amount),0) from pickup_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id in (select pickup_id from depart_transfer where depart_id is null and order_id = tor.id) and f.type = '应付' and f.name = '台阶费') +"
						+ " (select ifnull(sum(d.amount),0) from depart_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.depart_order_id in (select depart_id from depart_transfer where pickup_id is null and order_id = tor.id) and f.type = '应付' and f.name = '台阶费') +"
						+ " (select ifnull(sum(d.amount),0) from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id where d.order_id in (select delivery_id from delivery_order_item where transfer_order_id = tor.id) and f.type = '应付' and f.name = '台阶费')) as paytaijie,"
						+ " ((select ifnull(sum(d.amount),0) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = tor.id and f.type = '应付' and f.name = '提货费') + "
						+ " (select ifnull(sum(d.amount),0) from pickup_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id in (select pickup_id from depart_transfer where depart_id is null and order_id = tor.id) and f.type = '应付' and f.name = '提货费') +"
						+ " (select ifnull(sum(d.amount),0) from depart_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.depart_order_id in (select depart_id from depart_transfer where pickup_id is null and order_id = tor.id) and f.type = '应付' and f.name = '提货费') +"
						+ " (select ifnull(sum(d.amount),0) from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id where d.order_id in (select delivery_id from delivery_order_item where transfer_order_id = tor.id) and f.type = '应付' and f.name = '提货费')) as paytihuo,"
						+ " ((select ifnull(sum(d.amount),0) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = tor.id and f.type = '应付' and f.name = '等待费') + "
						+ " (select ifnull(sum(d.amount),0) from pickup_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id in (select pickup_id from depart_transfer where depart_id is null and order_id = tor.id) and f.type = '应付' and f.name = '等待费') +"
						+ " (select ifnull(sum(d.amount),0) from depart_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.depart_order_id in (select depart_id from depart_transfer where pickup_id is null and order_id = tor.id) and f.type = '应付' and f.name = '等待费') +"
						+ " (select ifnull(sum(d.amount),0) from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id where d.order_id in (select delivery_id from delivery_order_item where transfer_order_id = tor.id) and f.type = '应付' and f.name = '等待费')) as paydengdai,"
						+ " ((select ifnull(sum(d.amount),0) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = tor.id and f.type = '应付' and f.name = '暂存费') + "
						+ " (select ifnull(sum(d.amount),0) from pickup_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id in (select pickup_id from depart_transfer where depart_id is null and order_id = tor.id) and f.type = '应付' and f.name = '暂存费') +"
						+ " (select ifnull(sum(d.amount),0) from depart_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.depart_order_id in (select depart_id from depart_transfer where pickup_id is null and order_id = tor.id) and f.type = '应付' and f.name = '暂存费') +"
						+ " (select ifnull(sum(d.amount),0) from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id where d.order_id in (select delivery_id from delivery_order_item where transfer_order_id = tor.id) and f.type = '应付' and f.name = '暂存费')) as payzancun,"
						+ " ((select ifnull(sum(d.amount),0) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = tor.id and f.type = '应付' and f.name = '其他费用') + "
						+ " (select ifnull(sum(d.amount),0) from pickup_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id in (select pickup_id from depart_transfer where depart_id is null and order_id = tor.id) and f.type = '应付' and f.name = '其他费用') +"
						+ " (select ifnull(sum(d.amount),0) from depart_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.depart_order_id in (select depart_id from depart_transfer where pickup_id is null and order_id = tor.id) and f.type = '应付' and f.name = '其他费用') +"
						+ " (select ifnull(sum(d.amount),0) from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id where d.order_id in (select delivery_id from delivery_order_item where transfer_order_id = tor.id) and f.type = '应付' and f.name = '暂存费')) as payqita,"
						+ " (select ifnull(sum(d.amount),0) from depart_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.depart_order_id in (select depart_id from depart_transfer where pickup_id is null and order_id = tor.id) and f.type = '应付' and f.name = '运输费') as payganxian,"
						+ " (select ifnull(sum(d.amount),0) from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id where d.order_id in (select delivery_id from delivery_order_item where transfer_order_id = tor.id) and f.type = '应付' and f.name = '运输费') as paypeisong,"
						+ " (select round(sum(ifnull(amount,0)*ifnull(rate,0)),2) from insurance_fin_item d left join fin_item f on d.fin_item_id = f.id	where d.transfer_order_item_id in (select id from transfer_order_item where order_id = tor.id ) and f.type = '应付' and f.name = '保险费') as paybaoxian,"
				/*for (FinItem finItem : incomeName) {
					sql += " (select ifnull(sum(d.amount),0) from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.transfer_order_id = tor.id and f.type = '应收' and f.name = '" + finItem.get("name") + "') as income" + finItem.get("code") + ",";
				}*/
						//应收
						+ " (select ifnull(sum(d.amount),0) from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.transfer_order_id = tor.id and f.type = '应收' and f.name = '提货费') as incometihuo,"
						+ " (select ifnull(sum(d.amount),0) from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.transfer_order_id = tor.id and f.type = '应收' and f.name = '运输费') as incomeyunshu,"
						+ " (select ifnull(sum(d.amount),0) from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.transfer_order_id = tor.id and f.type = '应收' and f.name = '送货费') as incomesonghuo,"
						+ " (select ifnull(sum(d.amount),0) from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.transfer_order_id = tor.id and f.type = '应收' and f.name = '台阶费') as incometaijie,"
						+ " (select ifnull(sum(d.amount),0) from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.transfer_order_id = tor.id and f.type = '应收' and f.name = '安装费') as incomeanzhuang,"
						+ " (select ifnull(sum(d.amount),0) from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.transfer_order_id = tor.id and f.type = '应收' and f.name = '暂存费') as incomezancun,"
						+ " (select ifnull(sum(d.amount),0) from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.transfer_order_id = tor.id and f.type = '应收' and f.name = '其他费用') as incomeqita,"
						+ " (select round(sum(ifnull(amount,0)*ifnull(income_rate,0)),2) from insurance_fin_item d where d.transfer_order_item_id in (select id from transfer_order_item where order_id = tor.id )) as incomebaoxian,"//应收保险费
						
						//运作毛利
						+ " ((select ifnull(sum(d.amount),0) from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.transfer_order_id = tor.id and f.type = '应收') -"
						+ " ((select ifnull(sum(d.amount),0) from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = tor.id and f.type = '应付') + "
						+ " (select ifnull(sum(d.amount),0) from pickup_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.pickup_order_id in (select pickup_id from depart_transfer where depart_id is null and order_id = tor.id) and f.type = '应付') +"
						+ " (select ifnull(sum(d.amount),0) from depart_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.depart_order_id in (select depart_id from depart_transfer where pickup_id is null and order_id = tor.id) and f.type = '应付') +"
						+ " (select ifnull(sum(d.amount),0) from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id where d.order_id in (select delivery_id from delivery_order_item where transfer_order_id = tor.id) and f.type = '应付') +"
						+ " (select round(sum(ifnull(amount,0)*ifnull(rate,0)),2) from insurance_fin_item d left join fin_item f on d.fin_item_id = f.id	where d.transfer_order_item_id in (select id from transfer_order_item where order_id = tor.id ) and f.type = '应付'))) as yunzuomaoli,"
						//总收入
						+ " (select ifnull(sum(d.amount),0) from return_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.transfer_order_id = tor.id and f.type = '应收') as zongshouru"
						+ " from transfer_order tor"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join location l1 on tor.route_from = l1.code "
						+ " left join location l2 on tor.route_to = l2.code"
						+ " where tor.planning_time between '" + beginTime + "' and '" + endTime + "'"
						+ " and tor.cargo_nature_detail = 'cargoNatureDetailNo'"
						+ " and tor.cargo_nature = 'cargo' and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
								+ "  and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
				
				//有运输单号时
				if(!"".equals(orderNo) && orderNo != null){
					totalSql = totalSql + " and tor.order_no = '" + orderNo + "'";
					sql = sql + " and tor.order_no = '" + orderNo + "'";
				}
				
				//有客户时
				if(!"".equals(customerId) && customerId != null){
					totalSql = totalSql + "and tor.customer_id = '" + customerId + "'";
					sql = sql + "and tor.customer_id = '" + customerId + "'";
				}
				
				sql += " order by tor.planning_time desc";
				
				// 获取总条数
				Record rec = Db.findFirst(totalSql);
				logger.debug("total records:" + rec.getLong("total"));
				// 获取当前页的数据
				List<Record> orders =Db.find(sql + sLimit);
				
				orderMap.put("sEcho", pageIndex);
				orderMap.put("iTotalRecords", rec.getLong("total"));
				orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
				orderMap.put("aaData", orders);
			}
		}
		renderJson(orderMap);
	}
	
	
	public void searchOrderCount(){
		String pointInTime = getPara("pointInTime");
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    SimpleDateFormat dayDate =new SimpleDateFormat("yyyy-MM-dd");  
	    Calendar pastDay = Calendar.getInstance(); 
	    if("pastOneDay".equals(pointInTime))
	    	pastDay.add(Calendar.DAY_OF_WEEK, -1);
	    else if("pastSevenDay".equals(pointInTime))
	    	pastDay.add(Calendar.DAY_OF_WEEK, -7);
	    else
	    	pastDay.add(Calendar.DAY_OF_WEEK, -30);
	    String beginTime = dayDate.format(pastDay.getTime());
	    String endTime = simpleDate.format(Calendar.getInstance().getTime());
	    
	    String name = (String) currentUser.getPrincipal();
		UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
	    users.set("last_index", pointInTime).update();
	    
	    String transferOrderTotal = "select count(0) total from transfer_order t where t.status != '取消' and (t.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')) "
				+ " and t.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
				+ " and create_stamp between '" + beginTime + "' and '" + endTime + "'";
		Record transferOrderCound = Db.findFirst(transferOrderTotal);
		
		String deliveryTotal = "select count(0) total from delivery_order d left join warehouse w on w.id = d.from_warehouse_id where w.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
				+ " and d.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
				+ " and d.create_stamp between '" + beginTime + "' and '" + endTime + "'";
		Record deliveryCound = Db.findFirst(deliveryTotal);
		
		String pickupTotal = "select count(distinct dor.id) total from depart_order dor "
                + " left join depart_transfer dtf on dtf.pickup_id = dor.id "
                + " left join transfer_order t_o on t_o.id = dtf.order_id "
                + " left join office o on o.id = t_o.office_id "
                + " where dor.status!='取消' and combine_type = '"
        		+ DepartOrder.COMBINE_TYPE_PICKUP + "' and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
        		+ " and t_o.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
        		+ " and dor.create_stamp between '" + beginTime + "' and '" + endTime + "'";
		Record pickupCound = Db.findFirst(pickupTotal);
		
		String departTotal = "select count(1) total from depart_order deo "
                    + "left join carinfo  car on deo.driver_id=car.id"
                    + " left join depart_transfer dtf on dtf.depart_id = deo.id"
					+ " left join transfer_order tor on tor .id = dtf.order_id"
					+ " left join user_login u on u.id = tor .create_by"
					+ " left join office o on o.id = tor.office_id"
            		+ " where combine_type = '" + DepartOrder.COMBINE_TYPE_DEPART + "'and o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
                    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
        			+ " and deo.create_stamp between '" + beginTime + "' and '" + endTime + "'";
		Record departCound = Db.findFirst(departTotal);
		
		String returnTotal = "select count(0) total from return_order ro"
        		+ " left join delivery_order dor on dor.id = ro.delivery_order_id "
        		+ " left join transfer_order tor on tor.id = ro.transfer_order_id "
				+ " left join warehouse w on dor.from_warehouse_id = w.id "
				+ " where ifnull(w.office_id,tor.office_id) in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')"
				+ " and ifnull(dor.customer_id,tor.customer_id) in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
				+ " and ro.create_date between '" + beginTime + "' and '" + endTime + "'";
		Record returnCound = Db.findFirst(returnTotal);
		
		String insuranceTotal = "select count(distinct ior.id) total from insurance_order ior "
        		+ " left join transfer_order tor on tor.insurance_id = ior.id "
        		+ " left join office o on o.id = tor .office_id where  o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
        		+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
        		+ " and ior.create_stamp between '" + beginTime + "' and '" + endTime + "'";
		Record insuranceCound = Db.findFirst(insuranceTotal);
		
		renderJson("{\"transferOrderTotal\":"+transferOrderCound.getLong("total")+",\"deliveryTotal\":"+deliveryCound.getLong("total")+",\"pickupTotal\":"+pickupCound.getLong("total")+",\"departTotal\":"+departCound.getLong("total")+",\"returnTotal\":"+returnCound.getLong("total")+",\"insuranceTotal\":"+insuranceCound.getLong("total")+"}");
	}
	
	
	
	
	
	
	
	
}
