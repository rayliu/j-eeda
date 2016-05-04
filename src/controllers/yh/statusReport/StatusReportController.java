package controllers.yh.statusReport;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.Party;
import models.UserLogin;

import org.apache.commons.lang.StringUtils;
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
public class StatusReportController extends Controller{
	
	private Logger logger = Logger.getLogger(TransferOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	public void index() {		
		render("/yh/statusReport/productStatusReport.html");
	}
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_ORDERINDEX_LIST})
	public void orderFlow() {
		render("/yh/statusReport/orderFlowList.html");
	}
	
	public void orderFlowList() {	
		String transfer_order_no = getPara("transfer_order_no");
		String pickup_order_no = getPara("pickup_order_no");
		String depart_order_no = getPara("depart_order_no");
		String delivery_order_no = getPara("delivery_order_no");
		String return_order_no = getPara("return_order_no");
		String charge_order_no = getPara("charge_order_no");
		String cost_order_no = getPara("cost_order_no");
		String sign_no = getPara("sign_no");
		String serial_no = getPara("serial_no");
		
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
		String conditions="  where 1=1 ";
		if (StringUtils.isNotEmpty(transfer_order_no)){                                           //运输单
			conditions += " and UPPER(tor.order_no) like '%"+transfer_order_no.toUpperCase()+"%'";
		}
		if (StringUtils.isNotEmpty(pickup_order_no)){                                            // 调车单
			conditions += " and UPPER(dor_pi.depart_no) like '%"+pickup_order_no.toUpperCase()+"%'";
		}
		if (StringUtils.isNotEmpty(depart_order_no)){                                            //发车单
			conditions += " and UPPER(dor_de.depart_no) like '%"+depart_order_no.toUpperCase()+"%'";
		}
		if (StringUtils.isNotEmpty(delivery_order_no)){                                          // 配送单
			conditions += " and UPPER(deo.order_no) like '%"+delivery_order_no.toUpperCase()+"%'";
		}
		if (StringUtils.isNotEmpty(return_order_no)){                                            // 回单
			conditions += " and (UPPER(ror1.order_no) like '%"+return_order_no.toUpperCase()+"%'"
					    + " or UPPER(ror2.order_no) like '%"+return_order_no.toUpperCase()+"%')";
		}
		if (StringUtils.isNotEmpty(charge_order_no)){                                            //-- 应收对账单
			conditions += " and UPPER(aco.order_no) like '%"+charge_order_no.toUpperCase()+"%'";
		}
		if (StringUtils.isNotEmpty(cost_order_no)){                                              //-- 应付对账单
			conditions += " and (UPPER(acoo1.order_no) like '%"+cost_order_no.toUpperCase()+"%' "
					    + " or UPPER(acoo2.order_no) like '%"+cost_order_no.toUpperCase()+"%' "
					    + " or UPPER(acoo3.order_no) like '%"+cost_order_no.toUpperCase()+"%')";
		}
		if (StringUtils.isNotEmpty(sign_no)){                                            //-- 应收对账单
			conditions += " and UPPER(deo.ref_no) like '%"+sign_no.toUpperCase()+"%'";
		}
		if (StringUtils.isNotEmpty(serial_no)){                                            //-- 应收对账单
			conditions += " and ifnull((SELECT GROUP_CONCAT(toid.serial_no) from transfer_order_item_detail toid"
					+ " where toid.delivery_id = deo.id), (SELECT GROUP_CONCAT(toid.serial_no) from transfer_order_item_detail toid"
					+ " where toid.order_id = tor.id)) like '%"+serial_no.toUpperCase()+"%'";
		}
		conditions += " and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
				    + " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
		            + " GROUP BY tor.id ";
		
		String sql = " SELECT CONCAT(cast(tor.id as char),':',tor.order_no, '-', tor. STATUS) transfer_order_no, "
				+ " (select c.abbr  from contact c where id = tor.customer_id) customer_name,"
				+ " GROUP_CONCAT(DISTINCT( SELECT group_concat(cast(dor.id as char),':', dor.depart_no ,'-',dor.`STATUS` )"
				+ " FROM depart_order dor "
				+ " WHERE dor.combine_type = 'PICKUP' AND dor.id = dt.pickup_id ) SEPARATOR '<br/>') pickup_order_no"
				+ " ,"
				+ " group_concat(DISTINCT "
				+ "(select group_concat(cast(dor.id as char),':', dor.depart_no ,'-',dor.`STATUS` ) "
				+ " from depart_order dor "
				+ " where dor.combine_type='DEPART' and dor.id = dt.depart_id)"
				+ "  SEPARATOR '<br/>') depart_order_no"
				+ " ,"
				+ " GROUP_CONCAT(DISTINCT( SELECT group_concat(cast(deo.id as char),':',deo.order_no ,'+',deo.`STATUS`) "
				+ " FROM delivery_order deo "
				+ " WHERE deo.id = doi.delivery_id ) SEPARATOR '<br/>') delivery_order_no"
				+ " ,"
				+ " group_concat(DISTINCT( ifnull((select group_concat( cast(ror.id as char),':', ror.order_no ,'-',ror.transaction_status ) "
				+ " from return_order ror"
				+ " where ror.id = ror1.id )"
				+ "  ,"
				+ " (select group_concat(cast(ror.id as char),':', ror.order_no,'-',ror.transaction_status ) "
				+ " from return_order ror"
				+ " where ror.id = ror2.id ))) SEPARATOR '<br/>') return_order_no"
				+ " ,"
				+ " GROUP_CONCAT(DISTINCT( SELECT group_concat(cast(aco.id as char),':',  aco.order_no ,'-',aco.`STATUS` )"
				+ " FROM arap_charge_order aco"
				+ " WHERE aco.id = aci.charge_order_id ) SEPARATOR '<br/>') charge_order_no"
				+ " ,"
				+ " GROUP_CONCAT(DISTINCT( SELECT group_concat(cast(acor.id as char),':',  acor.order_no ,'-',acor.`STATUS` )"
				+ " FROM arap_cost_order acor"
				+ " WHERE acor.id = acoi1.cost_order_id ) SEPARATOR '<br/>') cost_order_no1"
				+ " ,"
				+ " GROUP_CONCAT(DISTINCT( SELECT group_concat(cast(acor.id as char),':', acor.order_no ,'-',acor.`STATUS` )  "
				+ " FROM arap_cost_order acor "
				+ " WHERE acor.id = acoi2.cost_order_id ) SEPARATOR '<br/>') cost_order_no2"
				+ " ,"
				+ " GROUP_CONCAT(DISTINCT( SELECT group_concat( cast(acor.id as char),':', acor.order_no ,'-',acor.`STATUS` )"
				+ " FROM arap_cost_order acor"
				+ " WHERE acor.id = acoi3.cost_order_id ) SEPARATOR '<br/>') cost_order_no3"
				+ " FROM"
				+ " transfer_order tor"
				+ " LEFT JOIN depart_transfer dt ON dt.order_id = tor.id"
				+ " LEFT JOIN depart_order dor_pi on dor_pi.id = dt.pickup_id"
				+ " LEFT JOIN depart_pickup dp ON dt.pickup_id = dp.pickup_id"
				+ " LEFT JOIN depart_order dor_de on dor_de.id = dt.depart_id"
//				+ " LEFT JOIN depart_order dor_de2 on dor_de2.id = dp.depart_id"
				+ " LEFT JOIN delivery_order_item doi on doi.transfer_order_id = tor.id"
				+ " LEFT JOIN delivery_order deo on deo.id = doi.delivery_id"
				+ " LEFT JOIN return_order ror1 on ror1.transfer_order_id = tor.id"
				+ " LEFT JOIN return_order ror2 on ror2.delivery_order_id = doi.delivery_id"
				+ " LEFT JOIN arap_charge_item aci on aci.ref_order_id = IFNULL(ror1.id,ror2.id)"
				+ " LEFT JOIN arap_charge_order aco on aco.id = aci.charge_order_id"
				+ " LEFT JOIN arap_cost_item acoi1 on acoi1.ref_order_id = dt.pickup_id and acoi1.ref_order_no = '提货'"
				+ " LEFT JOIN arap_cost_item acoi2 on acoi2.ref_order_id = dt.depart_id and acoi2.ref_order_no = '零担'"
				+ " LEFT JOIN arap_cost_item acoi3 on acoi3.ref_order_id = doi.delivery_id and acoi3.ref_order_no = '配送'"
				+ " LEFT JOIN arap_cost_order acoo1 on acoo1.id = acoi1.cost_order_id"
				+ " LEFT JOIN arap_cost_order acoo2 on acoo2.id = acoi2.cost_order_id"
				+ " LEFT JOIN arap_cost_order acoo3 on acoo3.id = acoi3.cost_order_id";
		
//	     List<Record> orderList = Collections.EMPTY_LIST;
//		 orderList = Db.find(sql + conditions);
//		 renderJson(orderList);
		 
		 
		 String sqlTotal = "select count(1) total from ("+sql+ conditions+") B";
         Record rec = Db.findFirst(sqlTotal);
         logger.debug("total records:" + rec.getLong("total"));
        
         List<Record> BillingOrders = Db.find(sql+ conditions + sLimit);

         
         Map BillingOrderListMap = new HashMap();
         BillingOrderListMap.put("draw", pageIndex);//显示第几页
         BillingOrderListMap.put("recordsTotal", rec.getLong("total"));
         BillingOrderListMap.put("recordsFiltered", rec.getLong("total"));
         BillingOrderListMap.put("data", BillingOrders);

         renderJson(BillingOrderListMap);
		 
	}
	
	public void productIndex() {		
		render("/yh/statusReport/productStatusReport.html");
	}
	
	public void orderIndex() {		
		render("/yh/statusReport/orderStatusReport.html");
	}
	//营收状态
	public void revenueIndex() {		
		String customer_id = getPara("customer_id");
		String years_date = getPara("years_date");
		List revenue = new ArrayList();//收入
		List cost = new ArrayList();//成本
		List profit = new ArrayList();//毛利
		List profit_rate = new ArrayList();//毛利率
		double sum_cost = 0.0;
		double sum_profit=0.0;
		Record rec =null;
		for(int i=1; i<=12;i++){
			String sql=" SELECT round(ifnull((sum((yf_pickup + yf_depart + yf_insurance + delivery))),0),2) cost_sum, "
			        + " round( ifnull((sum((ys_insurance + return_amount))),0),2) revenue_sum,"
//			        + " round(ifnull((sum((ys_insurance + return_amount)) - sum((yf_pickup + yf_depart + yf_insurance + delivery))),0),2) profit,"
//                    + " round(ifnull(((sum((ys_insurance + return_amount)) - sum((yf_pickup + yf_depart + yf_insurance + delivery))) / sum((ys_insurance + return_amount))),0),2) profit_rate"
					+ " if(round(ifnull((sum((ys_insurance + return_amount)) - sum((yf_pickup + yf_depart + yf_insurance + delivery))),0),2)<0,0, "
					+ "    round(ifnull((sum((ys_insurance + return_amount)) - sum((yf_pickup + yf_depart + yf_insurance + delivery))),0),2) ) profit,"
					+ " if(round(ifnull(((sum((ys_insurance + return_amount)) - sum((yf_pickup + yf_depart + yf_insurance + delivery))) / sum((ys_insurance + return_amount))),0),2)<0, 0,"
					+ "    round(ifnull(((sum((ys_insurance + return_amount)) - sum((yf_pickup + yf_depart + yf_insurance + delivery))) / sum((ys_insurance + return_amount))),0),2) ) profit_rate"
					+ " FROM"
					+ " (SELECT ifnull((SELECT IFNULL(sum(pofi.amount), 0) FROM pickup_order_fin_item pofi LEFT JOIN depart_order d_o ON d_o.id = pofi.pickup_order_id LEFT JOIN depart_transfer dt ON dt.pickup_id = d_o.id LEFT JOIN fin_item fi ON fi.id = pofi.fin_item_id WHERE"
					+ " dt.order_id = tor.id AND fi.type = '应付' AND pofi.fin_item_id != 7 AND d_o.audit_status = '对账已确认' AND d_o.combine_type = 'PICKUP') / ( SELECT count(0) FROM transfer_order_item_detail WHERE pickup_id = toid.pickup_id),0) yf_pickup,"
					+ " ifnull((SELECT IFNULL(sum(dofi.amount), 0) FROM depart_order_fin_item dofi LEFT JOIN depart_order d_o ON d_o.id = dofi.depart_order_id LEFT JOIN depart_transfer dt ON dt.depart_id = d_o.id LEFT JOIN fin_item fi ON fi.id = dofi.fin_item_id"
					+ " WHERE dt.order_id = tor.id AND fi.type = '应付' AND d_o.audit_status = '对账已确认' AND d_o.combine_type = 'DEPART' ) / ( SELECT count(0) FROM transfer_order_item_detail WHERE depart_id = toid.depart_id ), 0) yf_depart,"
					+ " ifnull((SELECT (sum(IFNULL(ifi.insurance_amount, 0)) / sum(toi.amount)) FROM insurance_fin_item ifi LEFT JOIN transfer_order_item toi ON toi.id = ifi.transfer_order_item_id LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id"
					+ " WHERE i_o.id = tor.insurance_id AND i_o.audit_status = '对账已确认' AND fi.type = '应付'),0) yf_insurance,"
					+ " ifnull((SELECT(sum(	IFNULL(ifi.insurance_amount, 0)) / sum(toi.amount)) FROM insurance_fin_item ifi LEFT JOIN transfer_order_item toi ON toi.id = ifi.transfer_order_item_id LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id WHERE i_o.id = tor.insurance_id AND fi.type = '应收'),0) ys_insurance,"
					+ " ifnull(((SELECT IFNULL(sum(dofi1.amount), 0) FROM delivery_order d_o LEFT JOIN delivery_order_fin_item dofi1 ON dofi1.order_id = d_o.id LEFT JOIN fin_item fi ON fi.id = dofi1.fin_item_id WHERE d_o.id = toid.delivery_id AND d_o.audit_status = '对账已确认' AND fi.type = '应付') / ("
					+ " SELECT count(0) FROM transfer_order_item_detail WHERE delivery_id = toid.delivery_id)) + (( SELECT IFNULL(sum(dofi1.amount), 0) FROM delivery_order d_o LEFT JOIN delivery_order_fin_item dofi1 ON dofi1.order_id = d_o.id LEFT JOIN fin_item fi ON fi.id = dofi1.fin_item_id"
					+ " WHERE d_o.id = toid.delivery_refused_id AND d_o.audit_status = '对账已确认' AND fi.type = '应付' ) / ( SELECT count(0) FROM transfer_order_item_detail WHERE delivery_refused_id = toid.delivery_refused_id)),0) delivery,"
					+ " ifnull((SELECT sum(rof.amount)FROM return_order_fin_item rof LEFT JOIN return_order ror ON ror.id = rof.return_order_id LEFT JOIN delivery_order dor ON dor.id = ror.delivery_order_id WHERE dor.id = toid.delivery_id AND ( ror.transaction_status != '新建' AND ror.transaction_status != '对账中' AND ror.transaction_status != '已确认' AND ror.transaction_status != '已签收' AND ror.transaction_status != '手动删除')),"
					+ " (SELECT ifnull(sum(rof1.amount), 0) FROM return_order_fin_item rof1 LEFT JOIN return_order ror ON ror.id = rof1.return_order_id WHERE ror.transfer_order_id = toid.order_id AND ( ror.transaction_status != '新建' AND ror.transaction_status != '对账中' AND ror.transaction_status != '已确认' AND ror.transaction_status != '已签收' AND ror.transaction_status != '手动删除'))) return_amount"
					+ " FROM transfer_order_item_detail toid"
					+ " LEFT JOIN transfer_order tor ON tor.id = toid.order_id"
					+ " LEFT JOIN party p ON p.id = tor.customer_id"
					+ " LEFT JOIN contact c ON c.id = p.contact_id"
					+ " LEFT JOIN location l1 ON tor.route_from = l1. CODE"
					+ " LEFT JOIN location l2 ON tor.route_to = l2. CODE"
					+ " WHERE tor.cargo_nature = 'ATM'"
					+ " and MONTH (tor.planning_time) = "+i+" and year(tor.planning_time) ="+years_date+" and tor.customer_id="+customer_id+""
					+ " UNION ALL"
					+ " SELECT  IFNULL(sum((SELECT IFNULL(sum(amount), 0) / (SELECT count(*) FROM depart_transfer dof WHERE dof.pickup_id = dor1.id) FROM pickup_order_fin_item pof LEFT JOIN depart_order dor1 ON dor1.id = pof.pickup_order_id WHERE dor1.id = dtr.pickup_id AND dor1.audit_status = '对账已确认' AND pof.fin_item_id != 7)),0) yf_pickup,"
					+ " ifnull(sum((SELECT IFNULL(sum(amount), 0) / (SELECT count(*) FROM depart_transfer dof WHERE dof.depart_id = dor1.id) FROM depart_order_fin_item dof LEFT JOIN depart_order dor1 ON dor1.id = dof.depart_order_id WHERE dor1.id = dtr.depart_id AND dor1.audit_status = '对账已确认'AND dof.fin_item_id != 7)),0) yf_depart,"
					+ " ifnull((SELECT IFNULL(sum(ifi.insurance_amount),0) FROM insurance_fin_item ifi LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id WHERE i_o.id = tor.insurance_id AND i_o.audit_status = '对账已确认' AND fi.type = '应付') "
					+ " / (SELECT COUNT(*) FROM insurance_order i LEFT JOIN transfer_order t ON t.insurance_id = i.id WHERE i.id = (SELECT id FROM insurance_order WHERE id = tor.insurance_id)),0) yf_insurance,"
					+ " ifnull((SELECT IFNULL( sum(ifi.insurance_amount),0) FROM insurance_fin_item ifi LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id WHERE i_o.id = tor.insurance_id AND fi.type = '应收') / (SELECT COUNT(*)"
					+ " FROM insurance_order i LEFT JOIN transfer_order t ON t.insurance_id = i.id WHERE i.id = (SELECT id FROM insurance_order WHERE id = tor.insurance_id)),0) ys_insurance,"
					+ " (SELECT IFNULL(sum(dofi1.amount), 0) FROM delivery_order d_o LEFT JOIN delivery_order_fin_item dofi1 ON dofi1.order_id = d_o.id LEFT JOIN delivery_order_item doi ON doi.delivery_id = d_o.id LEFT JOIN fin_item fi ON fi.id = dofi1.fin_item_id WHERE doi.transfer_order_id = tor.id AND d_o.audit_status = '对账已确认' AND fi.type = '应付') delivery,"
					+ " ifnull((SELECT sum(rof.amount) FROM return_order_fin_item rof LEFT JOIN return_order ror ON ror.id = rof.return_order_id LEFT JOIN delivery_order dor ON dor.id = ror.delivery_order_id LEFT JOIN delivery_order_item doi ON doi.delivery_id = dor.id WHERE"
					+ " doi.transfer_order_id = tor.id AND (ror.transaction_status != '新建' AND ror.transaction_status != '对账中' AND ror.transaction_status != '已确认' AND ror.transaction_status != '已签收' AND ror.transaction_status != '手动删除')),"
					+ " (SELECT	ifnull(sum(rof1.amount), 0) FROM return_order_fin_item rof1 LEFT JOIN return_order ror ON ror.id = rof1.return_order_id WHERE ror.transfer_order_id = tor.id AND (ror.transaction_status != '新建' AND ror.transaction_status != '对账中' AND ror.transaction_status != '已确认' AND ror.transaction_status != '已签收' AND ror.transaction_status != '手动删除' ))) return_amount "
					+ " FROM transfer_order tor"
					+ " LEFT JOIN depart_transfer dtr ON dtr.order_id = tor.id"
					+ " LEFT JOIN party p ON p.id = tor.customer_id"
					+ " LEFT JOIN contact c ON c.id = p.contact_id"
					+ " LEFT JOIN location l1 ON tor.route_from = l1. CODE"
					+ " LEFT JOIN location l2 ON tor.route_to = l2. CODE"
					+ " WHERE tor.cargo_nature = 'cargo' and MONTH (tor.planning_time) = "+i+" and year(tor.planning_time) ="+years_date+" and tor.customer_id="+customer_id+" GROUP BY tor.id ) a";
			 rec = Db.findFirst(sql);
			 sum_cost+= rec.getDouble("cost_sum");//累加成本
			 sum_profit+=rec.getDouble("profit");//累加毛利
			 revenue.add(rec.get("revenue_sum")); //收入
			 cost.add(rec.get("cost_sum")); //成本
			 profit.add(rec.get("profit")); //毛利
			 Double profitRate=Double.valueOf(String.format("%.2f", rec.getDouble("profit_rate")*100));
			 profit_rate.add(profitRate);//毛利率
		}
			Map BillingOrderListMap = new HashMap();
			BillingOrderListMap.put("revenue", revenue);
			BillingOrderListMap.put("cost", cost);
			BillingOrderListMap.put("profit", profit);
			BillingOrderListMap.put("profit_rate", profit_rate);
			BillingOrderListMap.put("sum_cost", sum_cost);
			BillingOrderListMap.put("sum_profit", sum_profit);
			renderJson(BillingOrderListMap);
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
		String sign_no = getPara("sign_no");
		String sLimit = "";
		
		Map orderMap = new HashMap();
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}
		
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
			if (StringUtils.isNotEmpty(beginTime)){
	        	beginTime = " and tor.planning_time between'"+beginTime+"'";
	        }else{
	        	beginTime =" and tor.planning_time between '1970-1-1'";
	        }
	        
	        if (StringUtils.isNotEmpty(endTime)){
	        	endTime =" and '"+endTime+" 23:59:59'";
	        }else{
	        	endTime =" and '2037-12-31'";
	        }
	        totalSql += beginTime+endTime;
	        sql += beginTime+endTime;
			

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
				totalSql = totalSql + " and toid.item_no = '" + item_no + "'";
				sql = sql + " and toid.item_no = '" + item_no + "'";
			}
			//有客户时
			if(!"".equals(customer_id) && customer_id != null){
				totalSql = totalSql + " and tor.customer_id = '" + customer_id + "'";
				sql = sql + " and tor.customer_id = '" + customer_id + "'";
			}
			
			//有客户时
			if (StringUtils.isNotEmpty(sign_no)){
				totalSql = totalSql + " and dor.ref_no LIKE '%" + sign_no + "%'";
				sql = sql + " and dor.ref_no like '%" + sign_no + "%'";
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
		Double total_amount = 0.00;
		Map orderMap = new HashMap();
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}
		
		if("transferOrder".equals(orderNoType) && "transferOrderStatus".equals(orderStatusType)){
			// 获取总条数
			String totalSql = "select count(distinct tor.id) total,"
					+ " sum(( SELECT CASE"
                    + " WHEN tor.cargo_nature ='ATM' THEN ("
                    + " select count(1) from transfer_order_item toi,  transfer_order_item_detail toid"
                    + "     where toid.item_id = toi.id and toi.order_id = tor.id"
                    + " )"
                    + " WHEN tor.cargo_nature ='cargo' THEN ("
                    + "     select sum(toi.amount) from transfer_order_item toi"
                    + "         where toi.order_id = tor.id"
                    + " )"
                    + " END )) total_amount "
					+ " from transfer_order tor"
					+ " left join party p1 on p1.id = tor.customer_id"
					+ " left join contact c1 on c1.id = p1.contact_id"
					+ " left join party p2 on p2.id = tor.sp_id"
					+ " left join contact c2 on c2.id = p2.contact_id"
					+ " left join location l1 on tor.route_from = l1.code "
					+ " left join location l2 on tor.route_to = l2.code"
					+ " left join office o on o.id = tor.office_id"
					+ " where  tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
					+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
				
			
			//有外发日期
			String limi = "";
			if(!"".equals(setOutTime) && setOutTime != null){
				totalSql = totalSql + " and dor.departure_time = '" + setOutTime + "'";
				limi = limi + " and dor.departure_time = '" + setOutTime + "'";
			}
			
			//计划时间段
			if(beginTime != null && !"".equals(beginTime) && endTime != null && !"".equals(endTime)){
				totalSql = totalSql + " and tor.planning_time between '" + beginTime + "' and '" + endTime + "'";
				limi = limi + " and tor.planning_time between '" + beginTime + "' and '" + endTime + "'";
			}
			
			//有运输单号时
			if(!"".equals(orderNo) && orderNo != null){
				totalSql = totalSql + " and tor.order_no like '%" + orderNo + "%'";
				limi = limi + " and tor.order_no like '%" + orderNo + "%'";
			}
			
			//始发地
			if(!"".equals(routeFrom) && routeFrom != null){
				totalSql = totalSql + " and l1.name like '%" + routeFrom + "%'";
				limi = limi + " and l1.name like '%" + routeFrom + "%'";
			}
			
			//目的地
			if(!"".equals(routeTo) && routeTo != null){
				totalSql = totalSql + " and l2.name like '%" + routeTo + "%'";
				limi = limi + " and l2.name like '%" + routeTo + "%'";
			}
			
			//有供应商
			if(!"".equals(sp_id) && sp_id != null){
				totalSql = totalSql + "and tor.sp_id = '" + sp_id + "'";
				limi = limi + "and tor.sp_id = '" + sp_id + "'";
			}
			//有客户时
			if(!"".equals(customerId) && customerId != null){
				totalSql = totalSql + "and tor.customer_id = '" + customerId + "'";
				limi = limi + "and tor.customer_id = '" + customerId + "'";
			}
			
			//运输单状态
			if(!"".equals(transferOrderStatus) && transferOrderStatus != null){
				if("NEW".equals(transferOrderStatus)){
					totalSql = totalSql + " and tor.status = '新建' ";
					limi = limi + " and tor.status = '新建' ";
				}else if("PARTIAL".equals(transferOrderStatus)){
					totalSql = totalSql + " and tor.depart_assign_status = 'PARTIAL' and dor.status = '已发车' ";
					limi = limi + " and tor.depart_assign_status = 'PARTIAL' and dor.status = '已发车' ";
				}else if("ALL".equals(transferOrderStatus)){
					totalSql = totalSql + " and tor.depart_assign_status = 'ALL' and dor.status = '已发车' ";
					limi = limi + " and tor.depart_assign_status = 'ALL' and dor.status = '已发车' ";
				}else if("DELIVERY".equals(transferOrderStatus)){
					totalSql = totalSql + " and (dor.status = '已入库' or dor.status = '已收货') ";
					limi = limi + " and (dor.status = '已入库' or dor.status = '已收货') ";
				}else if("RETURN".equals(transferOrderStatus)){
					totalSql = totalSql + " and ror.transaction_status != '新建' ";
					limi = limi + " and ror.transaction_status != '新建' ";
				}
			}
			// 获取总条数
			Record rec = Db.findFirst(totalSql);
			logger.debug("total records:" + rec.getLong("total"));
			logger.debug("total records:" + rec.getDouble("total_amount"));
			total_amount = rec.getDouble("total_amount");
			
			
			String sql = "select DISTINCT tor.order_no,'运输单' as order_category, "
					+ " ( SELECT group_concat( DISTINCT dor.depart_no SEPARATOR '<br/>' ) "
					+ " FROM depart_order dor "
					+ " LEFT JOIN depart_transfer dt on dt.depart_id = dor.id "
					+ " WHERE dt.order_id = tor.id ) depart_no,"
					+ " tor.order_type,c1.abbr,o.office_name,tor.planning_time,"
					+ " ifnull("+total_amount+",0) total_amount ,"
					+ " ( SELECT group_concat( cast(dor.departure_time as char) SEPARATOR '<br/>' ) "
					+ " FROM depart_order dor "
					+ " LEFT JOIN depart_transfer dt on dt.depart_id = dor.id "
					+ " WHERE dt.order_id = tor.id ) departure_time,"
					+ " l1.name route_from,l2.name route_to,"
					+ " tor.status order_status ,"
					+" (SELECT CASE"
                    + " WHEN tor.cargo_nature ='ATM' THEN ("
                    + " select count(1) from transfer_order_item toi,  transfer_order_item_detail toid"
                    + "     where toid.item_id = toi.id and toi.order_id = tor.id"
                    + " )"
                    + " WHEN tor.cargo_nature ='cargo' THEN ("
                    + "     select sum(toi.amount) from transfer_order_item toi"
                    + "         where toi.order_id = tor.id"
                    + " )"
                    + " END ) amount "
					+ " from transfer_order tor"
					+ " left join party p1 on p1.id = tor.customer_id"
					+ " left join contact c1 on c1.id = p1.contact_id"
					+ " left join party p2 on p2.id = tor.sp_id"
					+ " left join contact c2 on c2.id = p2.contact_id"
					+ " left join location l1 on tor.route_from = l1.code "
					+ " left join location l2 on tor.route_to = l2.code"
					+ " left join office o on o.id = tor.office_id"
					+ " where  tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
					+ " and tor.customer_id in  (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')";
			
			
			List<Record> orders =Db.find(sql + limi + " order by tor.planning_time desc "+sLimit);
			orderMap.put("sEcho", pageIndex);
			orderMap.put("iTotalRecords", rec.getLong("total"));
			orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
			orderMap.put("aaData", orders);
		}else if("deliveryOrder".equals(orderNoType) && "deliveryStatus".equals(orderStatusType)){
			// 获取总条数
			String totalSql = "select distinct count(0) total,"
					+ " sum((SELECT CASE"
					+ "  WHEN deo.cargo_nature ='ATM' THEN ("
		            + " select sum(doi1.amount) from delivery_order_item doi1 where doi1.delivery_id = deo.id "
			        + " )"
			        + " WHEN deo.cargo_nature ='cargo' THEN ifnull(("
		            + " SELECT sum(toi.amount) FROM transfer_order_item toi  "
		            + " LEFT JOIN delivery_order_item doi2 on doi2.transfer_item_id = toi.id  "
		            + " WHERE doi2.delivery_id = deo.id ),(select sum(doi1.amount) from delivery_order_item doi1 where doi1.delivery_id = deo.id))"
			        + " END )) total_amount from delivery_order deo"
					+ " left join party p1 on p1.id = deo.customer_id"
					+ " left join contact c1 on c1.id = p1.contact_id"
					+ " left join party p2 on p2.id = deo.sp_id"
					+ " left join contact c2 on c2.id = p2.contact_id"
					+ " left join location l1 on deo.route_from = l1.code "
					+ " left join location l2 on deo.route_to = l2.code"
					+ " left join warehouse w on deo.from_warehouse_id = w.id "
					+ " where w.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"') "
					+ " and deo.customer_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
				
			String limi = "";
			//有外发日期
			if(!"".equals(setOutTime) && setOutTime != null){
				totalSql = totalSql + " and and (dom.status != '新建' and to_days(dom.create_stamp) = to_days('" + setOutTime + "'))";
				limi = limi + " and (dom.status != '新建' and to_days(dom.create_stamp) = to_days('" + setOutTime + "'))";
			}
			
			//计划时间段
			if(beginTime != null && !"".equals(beginTime) && endTime != null && !"".equals(endTime)){
				totalSql = totalSql + " and tor.planning_time between '" + beginTime + "' and '" + endTime + "'";
				limi = limi + " and tor.planning_time between '" + beginTime + "' and '" + endTime + "'";
			}
			
			//有配送单号时
			if(!"".equals(orderNo) && orderNo != null){
				totalSql = totalSql + " and deo.order_no = '" + orderNo + "'";
				limi = limi + " and deo.order_no = '" + orderNo + "'";
			}
			
			//始发地
			if(!"".equals(routeFrom) && routeFrom != null){
				totalSql = totalSql + " and l1.name = '" + routeFrom + "'";
				limi = limi + " and l1.name = '" + routeFrom + "'";
			}
			
			//目的地
			if(!"".equals(routeTo) && routeTo != null){
				totalSql = totalSql + " and l2.name = '" + routeTo + "'";
				limi = limi + " and l2.name = '" + routeTo + "'";
			}
			
			//有供应商
			if(!"".equals(sp_id) && sp_id != null){
				totalSql = totalSql + "and deo.sp_id = '" + sp_id + "'";
				limi = limi + "and deo.sp_id = '" + sp_id + "'";
			}
			//有客户时
			if(!"".equals(customerId) && customerId != null){
				totalSql = totalSql + "and deo.customer_id = '" + customerId + "'";
				limi = limi + "and deo.customer_id = '" + customerId + "'";
			}
			
			//配送单状态
			if(!"".equals(deliveryStatus) && deliveryStatus != null){
				if("NEW".equals(deliveryStatus)){
					totalSql = totalSql + " and deo.status = '新建' ";
					limi = limi + " and deo.status = '新建' ";
				}else if("ONTRIP".equals(deliveryStatus)){
					totalSql = totalSql + " and deo.status = '已发车' ";
					limi = limi + " and deo.status = '已发车' ";
				}else if("DELIVERY".equals(deliveryStatus)){
					totalSql = totalSql + " and ror.id IS NOT NULL AND ror.transaction_status = '新建' ";
					limi = limi + " and ror.id IS NOT NULL AND ror.transaction_status = '新建' ";
				}else if("RETURN".equals(deliveryStatus)){
					totalSql = totalSql + " and ror.id IS NOT NULL AND ror.transaction_status != '新建'  ";
					limi = limi + " and ror.id IS NOT NULL AND ror.transaction_status != '新建' ";
				}
			}
			
			// 获取总条数
			Record rec = Db.findFirst(totalSql);
			logger.debug("total records:" + rec.getLong("total"));
			logger.debug("total records:" + rec.getDouble("total_amount"));
			total_amount = rec.getDouble("total_amount");
			
			
			String sql = "select distinct deo.order_no,'配送单' as order_category,'' as depart_no,tor.order_type,c1.abbr,o.office_name,"
					+ "ifnull("+total_amount+",0) total_amount ,"
					+ " ( SELECT tor.planning_time FROM transfer_order tor "
					+ " LEFT JOIN delivery_order_item doi on doi.transfer_order_id = tor.id "
					+ " LEFT JOIN delivery_order dor1 on dor1.id = doi.delivery_id WHERE "
					+ " dor1.id = deo.id GROUP BY deo.id ) AS planning_time,"
					+ " l1.name route_from,l2.name route_to,"
					+ " (select GROUP_CONCAT(cast(create_stamp  as char) SEPARATOR '</br>') from delivery_order_milestone where delivery_id = deo.id and status = '已发车') as departure_time,"
					+ " (select case when ror.id is not null and ror.transaction_status != '新建' then '回单签收' "
					+ " when ror.id is not null and ror.transaction_status = '新建' then '配送到达'"
					+ " when deo.status = '已发车' then '配送在途' "
					+ " when deo.status = '新建' then '新建配送' end ) as order_status, "
					+ " (SELECT CASE"
					+ "  WHEN deo.cargo_nature ='ATM' THEN ("
		            + " select sum(doi1.amount) from delivery_order_item doi1 where doi1.delivery_id = deo.id )"
			        + " WHEN deo.cargo_nature ='cargo' THEN ifnull(("
			        + " SELECT sum(toi.amount) FROM transfer_order_item toi  "
		            + " LEFT JOIN delivery_order_item doi2 on doi2.transfer_item_id = toi.id  "
		            + " WHERE doi2.delivery_id = deo.id ),"
		            + " (select sum(doi1.amount) from delivery_order_item doi1 where doi1.delivery_id = deo.id))"
			        + " END ) amount "
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
			
			
			// 获取当前页的数据
			List<Record> orders =Db.find(sql  + limi +  "order by tor.planning_time desc "+sLimit);
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
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_DAILYREPORT_LIST})
	public void dailyReportStatus() {
		String orderNo = getPara("order_no");
		String customerId = getPara("customer_id");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String route_to = getPara("route_to");
		String serial_no = getPara("serial_no");
		String receive = getPara("receive");
		String noreceive = getPara("noreceive");
		String inventory = getPara("inventory");
		String sLimit = "";
		
		Map orderMap = new HashMap();
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}
		//List<FinItem> payName = FinItem.dao.find("select name,code from fin_item where type = '应付';");
		//List<FinItem> incomeName = FinItem.dao.find("select name,code from fin_item where type = '应收';");
				
				// 获取总条数
				String totalSql = "SELECT count(0) total from (SELECT c.id cid,"
						+"		(case   when (select l.id from location l  "
		                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
		                +" 		where l.code = tor.route_to and l2.pcode = 1) is null"
		                +" 		then"
		                +" 		(select l.name from location l "
		                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
		                +" 		LEFT JOIN location l3 on l3.pcode = l2.code"
		                +" 		where l3.code = tor.route_to)"
		                +" 		when "
		                +" 		(select l.id from location l "
		                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
		                +" 		where l.code = tor.route_to  and l2.pcode = 1) is not null"
		                +" 		then"
		                +" 		 (select l.name from location l "
		                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
		                +" 		where l2.code = tor.route_to)"
		                +" 		end"
		                +" 		) province, "
						+ " c.abbr,(SELECT	dor.order_no FROM delivery_order dor	WHERE	dor.id = toid.delivery_id) deliveryno,tor.order_no transferno,toid.serial_no,tor.planning_time, l1. NAME route_from,l2. NAME route_to,toid.pieces,round(toid.weight, 2) weight,round(toid.volume, 2) volume,"
						+ " round(ifnull((SELECT sum(pofi.amount) FROM pickup_order_fin_item pofi	LEFT JOIN depart_order d_o ON d_o.id = pofi.pickup_order_id	LEFT JOIN depart_transfer dt ON dt.pickup_id = d_o.id	LEFT JOIN fin_item fi ON fi.id = pofi.fin_item_id	WHERE	dt.order_id = tor.id AND fi.type = '应付'"
						+ " AND pofi.fin_item_id != 7 AND d_o.combine_type = 'PICKUP') / (SELECT count(0)	FROM transfer_order_item_detail	WHERE	pickup_id = toid.pickup_id),0),2) yf_pickup,"
						+ " round(ifnull((SELECT sum(dofi.amount)	FROM depart_order_fin_item dofi	LEFT JOIN depart_order d_o ON d_o.id = dofi.depart_order_id LEFT JOIN depart_transfer dt ON dt.depart_id = d_o.id	LEFT JOIN fin_item fi ON fi.id = dofi.fin_item_id	WHERE	dt.order_id = tor.id AND fi.type = '应付'"
						+ " AND d_o.combine_type = 'DEPART')/(SELECT count(0) FROM	transfer_order_item_detail WHERE depart_id = toid.depart_id),0),2) yf_depart,"
						+ " round(ifnull((SELECT(sum(ifi.insurance_amount) / sum(toi.amount))	FROM insurance_fin_item ifi	LEFT JOIN transfer_order_item toi ON toi.id = ifi.transfer_order_item_id LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id	LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id"
						+ " WHERE	i_o.id = tor.insurance_id	AND fi.type = '应付'),0),2) yf_insurance,"
						+ " round(ifnull((SELECT(sum(ifi.insurance_amount) / sum(toi.amount))	FROM insurance_fin_item ifi	LEFT JOIN transfer_order_item toi ON toi.id = ifi.transfer_order_item_id LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id	LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id"
						+ " WHERE	i_o.id = tor.insurance_id	AND fi.type = '应收'),0),2) ys_insurance,"
						+ " round(ifnull((SELECT sum(dofi1.amount)FROM delivery_order d_o	LEFT JOIN delivery_order_fin_item dofi1 ON dofi1.order_id = d_o.id LEFT JOIN fin_item fi ON fi.id = dofi1.fin_item_id	WHERE	d_o.id = toid.delivery_id	AND fi.type = '应付')/(SELECT	count(0) FROM	transfer_order_item_detail"
						+ " WHERE	delivery_id = toid.delivery_id),0),2) delivery,"
						+ " ifnull((SELECT ifnull(sum(rof.amount), 0)	FROM return_order_fin_item rof LEFT JOIN return_order ror ON ror.id = rof.return_order_id	LEFT JOIN delivery_order dor ON dor.id = ror.delivery_order_id WHERE dor.id = toid.delivery_id),(SELECT ifnull(sum(rof1.amount), 0) FROM	return_order_fin_item rof1"
						+ " LEFT JOIN return_order ror ON ror.id = rof1.return_order_id WHERE	ror.transfer_order_id = toid.order_id)) return_amount "
						+ " FROM transfer_order_item_detail toid"
						+ " LEFT JOIN transfer_order tor ON tor.id = toid.order_id"
						+ " LEFT JOIN party p ON p.id = tor.customer_id"
						+ " LEFT JOIN contact c ON c.id = p.contact_id"
						+ " LEFT JOIN location l1 ON tor.route_from = l1. CODE"
						+ " LEFT JOIN location l2 ON tor.route_to = l2. CODE"
						+ " WHERE tor.cargo_nature = 'ATM'"
						+ " UNION"
						+ " SELECT c.id cid,"
						+ ""
						+"		(case   when (select l.id from location l  "
		                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
		                +" 		where l.code = tor.route_to and l2.pcode = 1) is null"
		                +" 		then"
		                +" 		(select l.name from location l "
		                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
		                +" 		LEFT JOIN location l3 on l3.pcode = l2.code"
		                +" 		where l3.code = tor.route_to)"
		                +" 		when "
		                +" 		(select l.id from location l "
		                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
		                +" 		where l.code = tor.route_to  and l2.pcode = 1) is not null"
		                +" 		then"
		                +" 		 (select l.name from location l "
		                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
		                +" 		where l2.code = tor.route_to)"
		                +" 		end"
		                +" 		) province, "
						+ ""
						+ " c.abbr,(SELECT d_o.order_no FROM	delivery_order d_o LEFT JOIN delivery_order_item doi ON doi.delivery_id = d_o.id WHERE doi.transfer_order_id = tor.id) deliveryno,tor.order_no transferno,'' serial_no,tor.planning_time,l1. NAME route_from,l2. NAME route_to,"
						+ " (SELECT ifnull(sum(amount), 0)	FROM	transfer_order_item toi	WHERE	toi.order_id = tor.id) pieces,(SELECT IFNULL(round(sum(weight),2), 0)FROM transfer_order_item toi WHERE toi.order_id = tor.id) weight,(SELECT ifnull(round(sum(volume),2), 0)FROM	transfer_order_item toi	WHERE	toi.order_id = tor.id) volume,"
						+ " ROUND(IFNULL(sum((SELECT sum(amount)/(SELECT count(*)	FROM depart_transfer dof WHERE dof.pickup_id = dor1.id)	FROM	pickup_order_fin_item pof	LEFT JOIN depart_order dor1 ON dor1.id = pof.pickup_order_id WHERE dor1.id = dtr.pickup_id	AND pof.fin_item_id != 7)),	0),2) dddd,"
						+ " ROUND(ifnull(sum((SELECT sum(IFNULL(amount, 0))/(SELECT	count(*) FROM depart_transfer dof	WHERE	dof.depart_id = dor1.id) FROM	depart_order_fin_item dof	LEFT JOIN depart_order dor1 ON dor1.id = dof.depart_order_id WHERE dor1.id = dtr.depart_id AND dof.fin_item_id != 7)),0),2) asd,"
						+ " round(ifnull((SELECT sum(ifi.insurance_amount) FROM	insurance_fin_item ifi LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id WHERE i_o.id = tor.insurance_id	AND fi.type = '应付')/(SELECT	COUNT(*) FROM	insurance_order i"
						+ " LEFT JOIN transfer_order t ON t.insurance_id = i.id	WHERE	i.id = (SELECT id	FROM insurance_order WHERE id = tor.insurance_id)),0),2) yf_insurance,"
						+ " round(ifnull((SELECT sum(ifi.insurance_amount) FROM	insurance_fin_item ifi LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id	LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id WHERE i_o.id = tor.insurance_id	AND fi.type = '应收')/(SELECT	COUNT(*) FROM	insurance_order i"
						+ " LEFT JOIN transfer_order t ON t.insurance_id = i.id	WHERE	i.id = (SELECT id	FROM insurance_order WHERE id = tor.insurance_id)),0),2) ys_insurance,"
						+ " round((SELECT	ifnull(sum(dofi1.amount), 0) FROM	delivery_order d_o LEFT JOIN delivery_order_fin_item dofi1 ON dofi1.order_id = d_o.id LEFT JOIN delivery_order_item doi ON doi.delivery_id = d_o.id LEFT JOIN fin_item fi ON fi.id = dofi1.fin_item_id WHERE doi.transfer_order_id = tor.id AND fi.type = '应付'),2) delivery,"
						+ " ifnull((SELECT ifnull(sum(rof.amount), 0)	FROM return_order_fin_item rof LEFT JOIN return_order ror ON ror.id = rof.return_order_id	LEFT JOIN delivery_order dor ON dor.id = ror.delivery_order_id LEFT JOIN delivery_order_item doi ON doi.delivery_id = dor.id"
						+ " WHERE	doi.transfer_order_id = tor.id),(SELECT	ifnull(sum(rof1.amount), 0)FROM	return_order_fin_item rof1 LEFT JOIN return_order ror ON ror.id = rof1.return_order_id WHERE ror.transfer_order_id = tor.id)) return_amount"
						+ " FROM transfer_order tor"
						+ " LEFT JOIN depart_transfer dtr ON dtr.order_id = tor.id"
						+ " LEFT JOIN party p ON p.id = tor.customer_id"
						+ " LEFT JOIN contact c ON c.id = p.contact_id"
						+ " LEFT JOIN location l1 ON tor.route_from = l1. CODE"
						+ " LEFT JOIN location l2 ON tor.route_to = l2. CODE"
						+ " WHERE tor.cargo_nature = 'cargo' GROUP BY	tor.id) a";
						
					
				String sql = "SELECT *,round((yf_pickup+yf_depart+yf_insurance+delivery),2) yf_sum,round((ys_insurance+return_amount),2) ys_sum,round(((ys_insurance+return_amount)-(yf_pickup+yf_depart+yf_insurance+delivery)),2)yz_amount,round(ifnull((((ys_insurance+return_amount)-(yf_pickup+yf_depart+yf_insurance+delivery))/(ys_insurance+return_amount)),0),2) maolilv from "
						+ " (SELECT c.id cid,"
						+ ""
						+"		(case   when (select l.id from location l  "
		                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
		                +" 		where l.code = tor.route_to and l2.pcode = 1) is null"
		                +" 		then"
		                +" 		(select l.name from location l "
		                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
		                +" 		LEFT JOIN location l3 on l3.pcode = l2.code"
		                +" 		where l3.code = tor.route_to)"
		                +" 		when "
		                +" 		(select l.id from location l "
		                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
		                +" 		where l.code = tor.route_to  and l2.pcode = 1) is not null"
		                +" 		then"
		                +" 		 (select l.name from location l "
		                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
		                +" 		where l2.code = tor.route_to)"
		                +" 		end"
		                +" 		) province, "
						+ ""
						+ "c.abbr,concat((SELECT dor.order_no FROM delivery_order dor WHERE dor.id = toid.delivery_id ),'<br/>',ifnull((SELECT dor.order_no FROM delivery_order dor WHERE dor.id = toid.delivery_refused_id),'')) deliveryno,tor.order_no transferno,tor.STATUS,'' ORDER_TYPE ,toid.serial_no,tor.planning_time,	l1. NAME route_from,	l2. NAME route_to,toid.pieces,round(toid.weight, 2) weight,round(toid.volume, 2) volume,"
						+ " round(ifnull((SELECT IFNULL(sum(pofi.amount),0) FROM pickup_order_fin_item pofi	LEFT JOIN depart_order d_o ON d_o.id = pofi.pickup_order_id	LEFT JOIN depart_transfer dt ON dt.pickup_id = d_o.id	LEFT JOIN fin_item fi ON fi.id = pofi.fin_item_id	WHERE	dt.order_id = tor.id AND fi.type = '应付'"
						+ " AND pofi.fin_item_id != 7 and d_o.audit_status='对账已确认' AND d_o.combine_type = 'PICKUP') / (SELECT count(0)	FROM transfer_order_item_detail	WHERE	pickup_id = toid.pickup_id),0),2) yf_pickup,"
						+ " round(ifnull((SELECT IFNULL(sum(dofi.amount),0)	FROM depart_order_fin_item dofi	LEFT JOIN depart_order d_o ON d_o.id = dofi.depart_order_id LEFT JOIN depart_transfer dt ON dt.depart_id = d_o.id	LEFT JOIN fin_item fi ON fi.id = dofi.fin_item_id	WHERE	dt.order_id = tor.id AND fi.type = '应付'"
						+ " and d_o.audit_status='对账已确认' AND d_o.combine_type = 'DEPART')/(SELECT count(0) FROM	transfer_order_item_detail WHERE depart_id = toid.depart_id),0),2) yf_depart,"
						+ " round(ifnull((SELECT(sum(IFNULL(ifi.insurance_amount,0)) / sum(toi.amount))	FROM insurance_fin_item ifi	LEFT JOIN transfer_order_item toi ON toi.id = ifi.transfer_order_item_id LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id	LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id"
						+ " WHERE	i_o.id = tor.insurance_id and i_o.audit_status='对账已确认' AND fi.type = '应付'),0),2) yf_insurance,"
						+ " round(ifnull((SELECT(sum(IFNULL(ifi.insurance_amount,0)) / sum(toi.amount))	FROM insurance_fin_item ifi	LEFT JOIN transfer_order_item toi ON toi.id = ifi.transfer_order_item_id LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id	LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id"
						+ " WHERE	i_o.id = tor.insurance_id	AND fi.type = '应收'),0),2) ys_insurance,"
						+ " round(ifnull(((SELECT IFNULL(sum(dofi1.amount), 0) FROM delivery_order d_o LEFT JOIN delivery_order_fin_item dofi1 ON dofi1.order_id = d_o.id LEFT JOIN fin_item fi ON fi.id = dofi1.fin_item_id WHERE d_o.id = toid.delivery_id AND d_o.audit_status = '对账已确认' AND fi.type = '应付') / (SELECT count(0) FROM transfer_order_item_detail WHERE delivery_id = toid.delivery_id))+ "
						+ " ((SELECT IFNULL(sum(dofi1.amount), 0) FROM delivery_order d_o LEFT JOIN delivery_order_fin_item dofi1 ON dofi1.order_id = d_o.id LEFT JOIN fin_item fi ON fi.id = dofi1.fin_item_id WHERE d_o.id = toid.delivery_refused_id AND d_o.audit_status = '对账已确认' AND fi.type = '应付') / (SELECT count(0) FROM transfer_order_item_detail WHERE delivery_refused_id = toid.delivery_refused_id)),0),2) delivery,"
						+ " ifnull((SELECT sum(rof.amount) FROM return_order_fin_item rof LEFT JOIN return_order ror ON ror.id = rof.return_order_id LEFT JOIN delivery_order dor ON dor.id = ror.delivery_order_id WHERE dor.id = toid.delivery_id and (ror.transaction_status!='新建' and ror.transaction_status!='对账中' and ror.transaction_status!='已确认' and ror.transaction_status!='已签收'and ror.transaction_status!='手动删除')),"
						+ " (SELECT ifnull(sum(rof1.amount), 0) FROM	return_order_fin_item rof1 LEFT JOIN return_order ror ON ror.id = rof1.return_order_id WHERE	ror.transfer_order_id = toid.order_id and (ror.transaction_status!='新建' and ror.transaction_status!='对账中' and ror.transaction_status!='已确认' and ror.transaction_status!='已签收'and ror.transaction_status!='手动删除'))) return_amount "
						+ " FROM transfer_order_item_detail toid"
						+ " LEFT JOIN transfer_order tor ON tor.id = toid.order_id"
						+ " LEFT JOIN party p ON p.id = tor.customer_id"
						+ " LEFT JOIN contact c ON c.id = p.contact_id"
						+ " LEFT JOIN location l1 ON tor.route_from = l1. CODE"
						+ " LEFT JOIN location l2 ON tor.route_to = l2. CODE"
						+ " WHERE tor.cargo_nature = 'ATM'"
						+ " UNION all"
						+ " SELECT c.id cid,"
						+"		(case   when (select l.id from location l  "
		                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
		                +" 		where l.code = tor.route_to and l2.pcode = 1) is null"
		                +" 		then"
		                +" 		(select l.name from location l "
		                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
		                +" 		LEFT JOIN location l3 on l3.pcode = l2.code"
		                +" 		where l3.code = tor.route_to)"
		                +" 		when "
		                +" 		(select l.id from location l "
		                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
		                +" 		where l.code = tor.route_to  and l2.pcode = 1) is not null"
		                +" 		then"
		                +" 		 (select l.name from location l "
		                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
		                +" 		where l2.code = tor.route_to)"
		                +" 		end"
		                +" 		) province, "
						+ ""
						+ " c.abbr,(SELECT	d_o.order_no FROM	delivery_order d_o LEFT JOIN delivery_order_item doi ON doi.delivery_id = d_o.id WHERE doi.transfer_order_id = tor.id) deliveryno,tor.order_no transferno,tor.STATUS,'' ORDER_TYPE,'' serial_no,tor.planning_time,l1. NAME route_from,l2. NAME route_to,"
						+ " (SELECT ifnull(sum(amount), 0)	FROM	transfer_order_item toi	WHERE	toi.order_id = tor.id) pieces,(SELECT	IFNULL(sum(weight), 0)FROM transfer_order_item toi WHERE toi.order_id = tor.id) weight,(SELECT ifnull(sum(volume), 0)FROM	transfer_order_item toi	WHERE	toi.order_id = tor.id) volume,"
						+ " ROUND(IFNULL(sum((SELECT IFNULL(sum(amount),0)/(SELECT count(*)	FROM depart_transfer dof WHERE dof.pickup_id = dor1.id)	FROM	pickup_order_fin_item pof	LEFT JOIN depart_order dor1 ON dor1.id = pof.pickup_order_id WHERE dor1.id = dtr.pickup_id and dor1.audit_status='对账已确认' AND pof.fin_item_id != 7)),	0),2) yf_pickup,"
						+ " ROUND(ifnull(sum((SELECT IFNULL(sum(amount),0)/(SELECT	count(*) FROM depart_transfer dof	WHERE	dof.depart_id = dor1.id) FROM	depart_order_fin_item dof	LEFT JOIN depart_order dor1 ON dor1.id = dof.depart_order_id WHERE dor1.id = dtr.depart_id and dor1.audit_status='对账已确认' AND dof.fin_item_id != 7)),0),2) yf_depart,"
						+ " round(ifnull((SELECT IFNULL(sum(ifi.insurance_amount),0) FROM	insurance_fin_item ifi LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id WHERE i_o.id = tor.insurance_id and i_o.audit_status='对账已确认' AND fi.type = '应付')/(SELECT	COUNT(*) FROM	insurance_order i"
						+ " LEFT JOIN transfer_order t ON t.insurance_id = i.id	WHERE	i.id = (SELECT id	FROM insurance_order WHERE id = tor.insurance_id)),0),2) yf_insurance,"
						+ " round(ifnull((SELECT IFNULL(sum(ifi.insurance_amount),0) FROM	insurance_fin_item ifi LEFT JOIN insurance_order i_o ON i_o.id = ifi.insurance_order_id	LEFT JOIN fin_item fi ON fi.id = ifi.fin_item_id WHERE i_o.id = tor.insurance_id	AND fi.type = '应收')/(SELECT	COUNT(*) FROM	insurance_order i"
						+ " LEFT JOIN transfer_order t ON t.insurance_id = i.id	WHERE	i.id = (SELECT id	FROM insurance_order WHERE id = tor.insurance_id)),0),2) ys_insurance,"
						+ " round((SELECT	IFNULL(sum(dofi1.amount),0) FROM	delivery_order d_o LEFT JOIN delivery_order_fin_item dofi1 ON dofi1.order_id = d_o.id LEFT JOIN delivery_order_item doi ON doi.delivery_id = d_o.id LEFT JOIN fin_item fi ON fi.id = dofi1.fin_item_id WHERE doi.transfer_order_id = tor.id and d_o.audit_status='对账已确认' AND  fi.type = '应付'),2) delivery,"
						+ " ifnull((SELECT sum(rof.amount)	FROM return_order_fin_item rof LEFT JOIN return_order ror ON ror.id = rof.return_order_id	LEFT JOIN delivery_order dor ON dor.id = ror.delivery_order_id LEFT JOIN delivery_order_item doi ON doi.delivery_id = dor.id"
						+ " WHERE doi.transfer_order_id = tor.id and (ror.transaction_status!='新建' and ror.transaction_status!='对账中' and ror.transaction_status!='已确认' and ror.transaction_status!='已签收'and ror.transaction_status!='手动删除')),(SELECT	ifnull(sum(rof1.amount), 0)FROM	return_order_fin_item rof1 LEFT JOIN return_order ror ON ror.id = rof1.return_order_id WHERE ror.transfer_order_id = tor.id and (ror.transaction_status!='新建' and ror.transaction_status!='对账中' and ror.transaction_status!='已确认' and ror.transaction_status!='已签收'and ror.transaction_status!='手动删除'))) return_amount"
						+ " FROM transfer_order tor"
						+ " LEFT JOIN depart_transfer dtr ON dtr.order_id = tor.id"
						+ " LEFT JOIN party p ON p.id = tor.customer_id"
						+ " LEFT JOIN contact c ON c.id = p.contact_id"
						+ " LEFT JOIN location l1 ON tor.route_from = l1. CODE"
						+ " LEFT JOIN location l2 ON tor.route_to = l2. CODE"
						+ " WHERE tor.cargo_nature = 'cargo' GROUP BY	tor.id) a";
						
				String condition = "";
				if(orderNo != null || customerId != null || beginTime != null|| endTime != null){
					String time ="";
					if ((beginTime == null || "".equals(beginTime))&&(endTime == null || "".equals(endTime))) {
						time = "1970-01-01";
					}
		    		if (beginTime == null || "".equals(beginTime)) {
						beginTime = "1970-01-01";
					}
		    		
					if (endTime == null || "".equals(endTime)) {
						endTime = "2037-12-31";
					}
					condition =" where 1=1";
					if(!"".equals(customerId)){
						condition +=" and ifnull(cid,'') ="+ customerId + " ";
					}
					if(!"".equals(orderNo)){
						condition +=" and ifnull(transferno,'') like '%" + orderNo + "%' ";
					}
					if(!"".equals(route_to)){
						condition +=" and ifnull(province,'') like '%" + route_to + "%' ";
					}
					if(!"".equals(serial_no)){
						condition +=" and ifnull(serial_no,'') like '%" + serial_no + "%' ";
					}
					if ((beginTime != null && !"".equals(beginTime))||(endTime != null && !"".equals(endTime))) {
		        		if (beginTime == null || "".equals(beginTime)) {
		    				beginTime = "1970-01-01";
		    			}
		        		
		    			if (endTime == null || "".equals(endTime)) {
		    				endTime = "2037-12-31";
		    			}
		    			condition += " and ifnull(planning_time,'1970-01-01') between '" + beginTime + "' and '" + endTime + " 23:59:59' ";
					}
				}
				
				// 获取总条数
				Record rec = Db.findFirst(totalSql + condition);
				logger.debug("total records:" + rec.getLong("total"));
				// 获取当前页的数据
				List<Record> orders =Db.find(sql+ condition +" order by planning_time desc" + sLimit);
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
	
	
	
	 // 列出客户公司名称
    public void search() {
        String locationName = getPara("locationName");
        
        List<Record> locationList = Collections.EMPTY_LIST;
        if (locationName.trim().length() > 0) {
            locationList = Db
                    .find("select *,p.id as pid,p.payment from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER' and c.abbr like '%"+locationName+"%' and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')");

        } else {
            locationList = Db.find("select *,p.id as pid,p.payment from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER'  and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')");

        }
        renderJson(locationList);
    }
	
    @RequiresPermissions(value = {PermissionConstant.revenueQueryReport})
	public void revenueQueryReport(){
        render("/yh/statusReport/revenueQueryReport.html");
    }
    
    @RequiresPermissions(value = {PermissionConstant.costQueryReport})
    public void costQueryReport(){
        render("/yh/statusReport/costQueryReport.html");
    }
    
    public void revenueQueryReportSearch(){
        String customer = getPara("customer");//客户
        String beginTime = getPara("beginTime");//开始时间
        String endTime = getPara("endTime");//结束时间
        String transferOrderNo = getPara("transferOrderNo");//运输单号
        String customerNo = getPara("customerNo");//客户订单号
        String start = getPara("start");//始发地
        String orderNo= getPara("orderNo");//业务单号
        String serial_no = getPara("serial_no");//始发地
        String ref_no= getPara("ref_no");//业务单号
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }
        
        //升降序
        String sortColIndex = getPara("iSortCol_0");
        String sortBy = getPara("sSortDir_0");
        String colName = getPara("mDataProp_"+sortColIndex);
        
        String orderByStr = " order by A.planning_time desc ";
        if(colName.length()>0){
            orderByStr = " order by A."+colName+" "+sortBy;
        }
        
        String sqlTotal = "";
        String sql = "";
        // 收入状态条件没有过滤

        sql = "SELECT "
                + " ror.id,"
                + " ror.order_no,"
                + " ror.transaction_status,"
               +" if(ror.transaction_status ='已签收', '应收明细未确认',"
               +"     if( (select count(1) from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id)=0,"
               +"         '应收明细已确认', "
               +"         (case  /*'have_invoice'*/"
               +"             when (select ifnull(have_invoice, 'N') from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1))='Y'"
               +"                 then if( "
               +"                         (select count(invoice_order_id) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1))=0,"
               +"                         (select concat(order_no, '-', status) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1)),"
               +"                         if("
               +"                             (select count(charge_order_id) from charge_application_order_rel "
               +"                                 where charge_order_id =("
               +"                                             select invoice_order_id from arap_charge_order where id ="
               +"                                                 (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1)"
               +"                                         )"
               +"                             )=0, "
               +"                             ( /*无收款申请单*/"
               +"                                 concat("
               +"                                     (select concat(order_no, '-', status) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1)),"
               +"                                     '<br/>',"
               +"                                     (select concat(order_no, '-', status) from arap_charge_invoice where id = (select invoice_order_id from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1)))"
               +"                                     )"
               +"                             ),"
               +"                             ( /* '有收款申请单' */"
               +"                                 concat("
               +"                                     (select concat(order_no, '-', status) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1)),"
               +"                                     '<br/>',"
               +"                                     (select concat(order_no, '-', status) from arap_charge_invoice where id = (select invoice_order_id from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1))),"
               +"                                     '<br/>',"
               +"                                     ("
               +"                                        select group_concat(concat(order_no, '-', status) separator '<br/>') from arap_charge_invoice_application_order aao left join charge_application_order_rel ar "
               +"                                          on aao.id = ar.application_order_id where ar.order_type='开票记录单' and ar.charge_order_id = ("
               +"                                             select invoice_order_id from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1) "
               +"                                          ) "
               +"                                     )"
               +"                                 "
               +"                                 )"
               +"                             )"
               +"                         )"
               +"                     ) "
               +"              /* 不需开票 */        "
               +"             when (select ifnull(have_invoice, 'N') from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1))='N'"
               +"                 then if("
               +"                         (select count(charge_order_id) from charge_application_order_rel "
               +"                                 where charge_order_id =("
               +"                                             select id from arap_charge_order where id ="
               +"                                                 (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1)"
               +"                                         )"
               +"                             )=0, "
               +"                             (select concat(order_no, '-', status) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1)),"
               +"                             concat("
               +"                                 (select concat(order_no, '-', status) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1)),"
               +"                                 '<br/>',"
               +"                                 ("
               +"                                    select group_concat(concat(order_no, '-', status) separator '<br/>') from arap_charge_invoice_application_order aao left join charge_application_order_rel ar "
               +"                                      on aao.id = ar.application_order_id where ar.charge_order_id = ("
               +"                                         select id from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='回单' and ref_order_id=ror.id limit 1) "
               +"                                      ) "
               +"                                 )"
               +"                             "
               +"                             )"
               +"                     )"
               +"          end"
               +"         )"
               +"     )"
               +" ) audit_status,"
                + " ror.remark,"
                + " ror.receipt_date,"
                + " ror.delivery_order_id,"
                + " ror.transfer_order_id,"
                + " ror.notity_party_id,"
                + " ror.customer_id,"
                + " (SELECT group_concat( DISTINCT toid.serial_no SEPARATOR '<br/>')"
                + " FROM transfer_order_item_detail toid"
                + " LEFT JOIN delivery_order_item doi ON toid.id = doi.transfer_item_detail_id"
                + " LEFT JOIN delivery_order d_o ON d_o.id = doi.delivery_id"
                + " WHERE d_o.id = ror.delivery_order_id) serial_no,"
                + " dvr.ref_no ref_no,"
                + "       (select sum(amount) from return_order_fin_item rofi where rofi.return_order_id= ror.id) total_amount,"
                + "        ror.path,"
                + "        ror.create_date,"
                + "        null change_amount,"
                + "            IFNULL(tor.order_no, (SELECT "
                + "                    GROUP_CONCAT(DISTINCT tor.order_no"
                + "                            SEPARATOR '<br/>')"
                + "                FROM"
                + "                    delivery_order dvr"
                + "                LEFT JOIN delivery_order_item doi ON doi.delivery_id = dvr.id"
                + "                LEFT JOIN transfer_order tor ON tor.id = doi.transfer_order_id"
                + "                WHERE"
                + "                    dvr.id = ror.delivery_order_id)) transfer_order_no,"
                + "            '回单' order_tp,"
                + "            dvr.order_no AS delivery_order_no,"
                + "            IFNULL(c.abbr, c2.abbr) cname,"
                + "            IFNULL(tor.planning_time, (SELECT "
                + "                    GROUP_CONCAT(tor.planning_time SEPARATOR '<br/>')"
                +"                FROM"
                +"                    delivery_order dvr"
                +"                LEFT JOIN delivery_order_item doi ON doi.delivery_id = dvr.id"
                +"                LEFT JOIN transfer_order tor ON tor.id = doi.transfer_order_id"
                +"                WHERE"
                +"                    dvr.id = ror.delivery_order_id)) planning_time,"
                +"            ifnull(c.address, "
                +"            (select c.address from party p "
                +"                  LEFT JOIN contact c ON c.id = p.contact_id"
                +"                    where p.id = dvr.notify_party_id)"
                +"            ) address,"
                +"            IFNULL(tor.customer_order_no, tor2.customer_order_no) customer_order_no,"
                +"            IFNULL((SELECT "
                +"                    name"
                +"                FROM"
                +"                    location"
                +"                WHERE"
                +"                    code = tor.route_from), (SELECT "
                +"                    name"
                +"                FROM"
                +"                    location"
                +"                WHERE"
                +"                    code = tor2.route_from)) route_from,"
                +"            IFNULL((SELECT "
                +"                    name"
                +"                FROM"
                +"                    location"
                +"                WHERE"
                +"                    code = tor.route_to), (SELECT "
                +"                    name"
                +"                FROM"
                +"                    location"
                +"                WHERE"
                +"                    code = dvr.route_to)) route_to,"
                +"            (SELECT "
                +"                    SUM(rofi.amount)"
                +"                FROM"
                +"                    return_order_fin_item rofi"
                +"                WHERE"
                +"                    rofi.return_order_id = ror.id"
                +"                        AND rofi.fin_type = 'charge'"
                +"                        AND rofi.contract_id != '') contract_amount,"
                +"            DATE(dvr.appointment_stamp) AS depart_time,"
                +"            NULL pickup_amount,"
                +"            NULL step_amount,"
                +"            NULL wait_amount,"
                +"            NULL other_amount,"
                +"            NULL load_amount,"
                +"            NULL warehouse_amount,"
                +"            NULL transfer_amount,"
                +"            NULL send_amount,"
                +"            NULL installation_amount,"
                +"            NULL super_mileage_amount,"
                +"            NULL AS charge_total_amount,"
                +"            NULL insurance_amount,"
                +"            case tor.sp_id "
                +"            when tor.sp_id is not null then(  "
                +"              select c.abbr from party p  "
                +"                  LEFT JOIN contact c ON c.id = p.contact_id "
                +"                    where p.id = tor.sp_id "
                +"                ) "
                +"            else ( "
                +"              select c.abbr from party p  "
                +"                  LEFT JOIN contact c ON c.id = p.contact_id "
                +"                    where p.id = tor2.sp_id "
                +"              ) "
                +"            end sp    "
                +"    FROM"
                +"        return_order ror"
                +"    LEFT JOIN transfer_order tor ON tor.id = ror.transfer_order_id"
                +"    LEFT JOIN party p ON p.id = tor.customer_id"
                +"    LEFT JOIN contact c ON c.id = p.contact_id"
                +"    LEFT JOIN depart_transfer dt ON (dt.order_id = tor.id"
                +"        AND IFNULL(dt.pickup_id, 0) > 0)"
                +"    LEFT JOIN delivery_order dvr ON ror.delivery_order_id = dvr.id"
                +"    LEFT JOIN delivery_order_item doi ON doi.delivery_id = dvr.id"
                +"    LEFT JOIN transfer_order tor2 ON tor2.id = doi.transfer_order_id"
                +"    LEFT JOIN party p2 ON p2.id = tor2.customer_id"
                +"    LEFT JOIN contact c2 ON c2.id = p2.contact_id"
                +"    LEFT JOIN depart_order dor ON dor.id = dt.pickup_id"
                +"    LEFT JOIN pickup_order_fin_item dofi ON dofi.pickup_order_id = dor.id"
                +"    LEFT JOIN user_login usl ON usl.id = ror.creator"
                +"    WHERE"
                +"         ror.customer_id IN ( SELECT customer_id FROM user_customer WHERE user_name = '"+currentUser.getPrincipal()+"' )"
                +"    GROUP BY ror.id"
                +"    UNION (SELECT "
                +"          amco.id id,"
                +"            amco.order_no order_no,"
                +"            amco.status transaction_status,"
               +" if(amco.status ='新建', '应收明细未确认',"
               +"     if( (select count(1) from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id)=0,"
               +"         '应收明细已确认', "
               +"         (case  /*'have_invoice'*/"
               +"             when (select ifnull(have_invoice, 'N') from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1))='Y'"
               +"                 then "
               +"                 if( "
               +"                         (select count(invoice_order_id) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1))=0,"
               +"                         (select concat(order_no, '-', status) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1)),"
               +"                         if("
               +"                             (select count(charge_order_id) from charge_application_order_rel "
               +"                                 where charge_order_id =("
               +"                                             select invoice_order_id from arap_charge_order where id ="
               +"                                                 (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1)"
               +"                                         )"
               +"                             )=0,"
               +"                             ("
               +"                                 concat("
               +"                                     (select concat(order_no, '-', status) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1)),"
               +"                                     '<br/>',"
               +"                                     (select concat(order_no, '-', status) from arap_charge_invoice where id = (select invoice_order_id from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1)))"
               +"                                     )"
               +"                             ),"
               +"                             ( "
               +"                                 concat("
               +"                                     (select concat(order_no, '-', status) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1)),"
               +"                                     '<br/>',"
               +"                                     (select concat(order_no, '-', status) from arap_charge_invoice where id = (select invoice_order_id from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1))),"
               +"                                     '<br/>',"
               +"                                     ("
               +"                                        select group_concat(concat(order_no, '-', status) separator '<br/>') from arap_charge_invoice_application_order aao left join charge_application_order_rel ar "
               +"                                          on aao.id = ar.application_order_id where ar.charge_order_id = ("
               +"                                             select invoice_order_id from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1) "
               +"                                          ) "
               +"                                     )"
               +"                                 )"
               +"                             )"
               +"                         )"
               +"                 ) "
               +"              /* 不需开票 */        "
               +"             when (select ifnull(have_invoice, 'N') from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1))='N'"
               +"                 then if("
               +"                         (select count(charge_order_id) from charge_application_order_rel "
               +"                                 where charge_order_id =("
               +"                                             select id from arap_charge_order where id ="
               +"                                                 (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1)"
               +"                                         )"
               +"                             )=0, "
               +"                             (select concat(order_no, '-', status) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1)),"
               +"                             concat("
               +"                                 (select concat(order_no, '-', status) from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1)),"
               +"                                 '<br/>',"
               +"                                 ("
               +"                                    select group_concat(concat(order_no, '-', status) separator '<br/>') from arap_charge_invoice_application_order aao left join charge_application_order_rel ar "
               +"                                      on aao.id = ar.application_order_id where ar.charge_order_id = ("
               +"                                         select id from arap_charge_order where id = (select charge_order_id from arap_charge_item where ref_order_type='收入单' and ref_order_id=amco.id limit 1) "
               +"                                      ) "
               +"                                 )"
               +"                             "
               +"                             )"
               +"                     )"
               +"          end"
               +"         )"
               +"     )"
               +" ) audit_status,"
                +"            NULL receipt_date,"
                +"            amco.remark remark,"
                +"            NULL delivery_order_id,"
                +"            NULL transfer_order_id,"
                +"            NULL notity_party_id,"
                +"            amco.customer_id customer_id,"
                +"            null serial_no,"
                +"            null ref_no,"
                +"            amco.total_amount total_amount,"
                +"            NULL path,"
                +"            amco.create_stamp create_date,"
                +"            null change_amount,"
                +"            NULL transfer_order_no,"
                +"            '收入单' order_tp,"
                +"            NULL delivery_order_no,"
                +"            c.abbr cname,"
                +"            NULL planning_time,"
                +"            NULL address,"
                +"            (SELECT "
                +"                    GROUP_CONCAT(DISTINCT amcoi.customer_order_no"
                +"                            SEPARATOR '<br/>')"
                +"                FROM"
                +"                    arap_misc_charge_order_item amcoi"
                +"                WHERE"
                +"                    amcoi.misc_order_id = amco.id) customer_order_no,"
                +"            NULL route_from,"
                +"            NULL route_to,"
                +"            NULL contract_amount,"
                +"            NULL depart_time,"
                +"            NULL pickup_amount,"
                +"            NULL step_amount,"
                +"            NULL wait_amount,"
                +"            NULL other_amount,"
                +"            NULL load_amount,"
                +"            NULL warehouse_amount,"
                +"            NULL transfer_amount,"
                +"            NULL send_amount,"
                +"            NULL installation_amount,"
                +"            NULL super_mileage_amount,"
                +"            amco.total_amount charge_total_amount,"
                +"            NULL insurance_amount,"
                +"            c1.abbr sp"
                +"    FROM"
                +"        arap_misc_charge_order amco"
                +"    LEFT JOIN party p ON p.id = amco.customer_id"
                +"    LEFT JOIN contact c ON c.id = p.id"
                +"    LEFT JOIN party p1 ON p1.id = amco.sp_id"
                +"    LEFT JOIN contact c1 ON c1.id = p1.id"
                +"    WHERE"
                +"            amco.type = 'biz'"
                +"            AND amco.total_amount != 0)";
        if (customer == null && beginTime == null && endTime == null
                && transferOrderNo == null && customerNo == null && start == null && orderNo==null&& serial_no==null&& ref_no==null) {
            sqlTotal = " select count(1) total from ("+ sql +") A";
            sql = "select * from ("+ sql +") A ";
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1970-01-01";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "2037-12-31";
            }
            
             String conditions = " where ifnull(A.cname,'')like '%"+customer+"%'"
                    + " and ifnull(A.route_from,'') like '%"+start
                    +"%' and ifnull(A.order_no,'') like '%"+orderNo
                    +"%' and ifnull(A.customer_order_no,'')like '%"+customerNo+"%'"
                    +"   and ifnull(A.serial_no,'')like '%"+serial_no+"%'"
                    +"   and ifnull(A.ref_no,'')like '%"+ref_no+"%'"
                    +"   and IFNULL(A.transfer_order_no,'') like '%"+transferOrderNo
                    +"%' and IFNULL(A.planning_time,'1970-01-01')  between '"
                    + beginTime
                    + "' and '"
                    + endTime
                    + "' ";
            
            sqlTotal = " select count(1) total from ("+ sql +") A" + conditions;
             
            sql = "select * from ("+ sql +") A " + conditions;
        }
        
        
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> BillingOrders = Db.find(sql + orderByStr  + sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    public void costQueryReportSearch(){
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }
            
            String sortColIndex = getPara("iSortCol_0");
            String sortBy = getPara("sSortDir_0");
            String colName = getPara("mDataProp_"+sortColIndex);
            
            String orderNo = getPara("orderNo");
            String sp = getPara("sp");
            String no = getPara("no");
            String beginTime = getPara("beginTime");
            String plantime = getPara("plantime");
            String arrivaltime = getPara("arrivaltime");
            String endTime = getPara("endTime");
            String status = getPara("status");
            String type = getPara("type");
            String booking_note_number = getPara("booking_note_number");
            String route_from =getPara("route_from");
            String route_to=getPara("route_to");
            String customer_name = getPara("customer_name");
            String serial_no=getPara("serial_no");
            String sign_no = getPara("sign_no");
           
            String sqlTotal = "";
            String sql = "select cast(planning_time as CHAR) planning_time1, A.* from (select distinct dor.id,"
                    + " dor.order_no order_no,"
                    + " dor.status,"
                    +" IF( dor.audit_status ='新建', '应付明细未确认', "
                    +" if("
                    +"    (select count(1) from arap_cost_item where ref_order_id = dor.id)=0,"
                    +"     '应付明细已确认',"
                    +"    if("
                    +"        (select count(1) from cost_application_order_rel where cost_order_id = (select cost_order_id from arap_cost_item where ref_order_id = dor.id limit 1))=0, "
                    +"            (select CONCAT(order_no, '-', status) from arap_cost_order where id = (select cost_order_id from arap_cost_item where ref_order_id = dor.id limit 1)),"
                    +"      CONCAT("
                    +"           (select CONCAT(order_no, '-', status) from arap_cost_order where id = (select cost_order_id from arap_cost_item where ref_order_id = dor.id limit 1)),"
                    +"            '<br/>',"
                    +"             (select group_concat(concat(order_no, '-', status) separator '<br/>') from arap_cost_invoice_application_order ca "
                    +"                  left join cost_application_order_rel car"
                    +"                    on ca.id= car.application_order_id where car.cost_order_id= (select cost_order_id from arap_cost_item where ref_order_id = dor.id limit 1))"
                    +"            )"
                    +"      )"
                    +"   )"
                    +" ) audit_status,"
                    + " ifnull((SELECT NAME FROM location WHERE CODE = dor.route_from),"
                    + " (SELECT NAME FROM location WHERE CODE = dor.route_from )) route_from,"
                    + " ifnull((SELECT NAME FROM location WHERE CODE = dor.route_to),"
                    + " (SELECT NAME FROM location WHERE CODE = dor.route_to)) route_to,"
                    + " IFNULL(c3.address,IFNULL(toid.notify_party_company,'')) receivingunit, c.abbr spname,"
                    + " (SELECT sum(doi1.amount) FROM delivery_order_item doi1 WHERE doi1.delivery_id = dor.id ) amount,"
                    + " round(ifnull(prod.volume,toi.volume)*toi.amount,2) volume,"
                    + " round(ifnull(prod.weight,toi.weight)*toi.amount,2) weight,"
                    + " DATE(dor.create_stamp) create_stamp,"
                    + " ifnull(ul.c_name, ul.user_name) creator,tor.customer_order_no customer_order_no,"
                    + " dor.ref_no ref_no,"
                    + " '配送' business_type, "
                    + " null as booking_note_number,"
                    + " GROUP_CONCAT((SELECT toid.serial_no from transfer_order_item_detail toid WHERE  doi.transfer_item_detail_id=toid.id) SEPARATOR '<br/>') serial_no,"
                    + " (select ifnull(sum(dofi.amount),0) from delivery_order_fin_item dofi where dofi.order_id = dor.id and IFNULL(dofi.cost_source,'') != '对账调整金额') pay_amount,"
                    + " (SELECT ifnull(sum(amount),0) FROM delivery_order_fin_item dofi WHERE dofi.order_id = dor.id) change_amount,"
                    + " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '<br/>') transfer_order_no,"
                    + " dor.sign_status return_order_collection,"
                    + " dor.remark,"
                    + " DATE(dor.depart_stamp) as depart_time,"
                    + " group_concat(distinct(SELECT CAST(tor.planning_time AS CHAR) FROM transfer_order tor WHERE tor.id = doi.transfer_order_id GROUP BY tor.id) SEPARATOR '<br/>') planning_time,"
                    + " oe.office_name office_name,"
                    + " c1.abbr cname,"
//                  + " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='运输费') as transport_cost,"
//                  + " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='搬运费') as carry_cost,"
//                  + " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='上楼费') as climb_cost,"
//                  + " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='保险费') as insurance_cost,"
//                  + " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='提货费') as take_cost,"
//                  + " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='安装费') as anzhuang_cost,"
//                  + " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='仓储费') as cangchu_cost,"
//                  + " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='其他费用') as other_cost"
                    + " 0 AS transport_cost,"
                    + " 0 AS carry_cost,"
                    + " 0 AS climb_cost,"
                    + " 0 AS insurance_cost,"
                    + " 0 AS take_cost,"
                    + " 0 AS anzhuang_cost,"
                    + " 0 AS cangchu_cost,"
                    + " 0 AS other_cost"
                    + " from delivery_order dor"
                    + " left join return_order ror on ror.delivery_order_id = dor.id"
                    + " left join party p on dor.sp_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join delivery_order_item doi on doi.delivery_id = dor.id "
                    + " left join delivery_order_fin_item dofi on dor.id = dofi.order_id "
                    + " left join transfer_order_item toi on doi.transfer_item_id = toi.id "
                    + " LEFT JOIN transfer_order_item_detail toid ON toid.id = doi.transfer_item_detail_id"
                    + " left join product prod on toi.product_id = prod.id "
                    + " left join transfer_order tor ON tor.id = doi.transfer_order_id"
                    + " left join party p1 on tor.customer_id = p1.id"
                    + " left join contact c1 on p1.contact_id = c1.id"
                    + " LEFT JOIN party p3 ON dor.notify_party_id = p3.id"
                    + " LEFT JOIN contact c3 ON p3.contact_id = c3.id"
                    + " left join user_login ul on dor.create_by = ul.id "
                    + " left join warehouse w on dor.from_warehouse_id = w.id "
                    + " left join office oe on w.office_id = oe.id "
                    + " where ror.transaction_status != '新建' and unix_timestamp(dor.appointment_stamp) > unix_timestamp('2015-06-01 10:34:36') "
//                    + " and  dor.audit_status='新建'"
                    + " and (dor.status !='新建' or dor.status !='计划中' or dor.status != '初始化') and p.party_type='SERVICE_PROVIDER' group by dor.id "
                    + " union"
                    + " select distinct dpr.id,"
                    + " dpr.depart_no order_no,"
                    + " dpr.status,"
                    +" IF( dpr.audit_status ='新建', '应付明细未确认', "
                    +"   if("
                    +"       (select count(1) from arap_cost_item where ref_order_id = dpr.id)=0,"
                    +"        '应付明细已确认',"
                    +"        if("
                    +"            (select count(1) from cost_application_order_rel where cost_order_id = (select cost_order_id from arap_cost_item where ref_order_id = dpr.id limit 1))=0, "
                    +"                (select CONCAT(order_no, '-', status) from arap_cost_order where id = (select cost_order_id from arap_cost_item where ref_order_id = dpr.id limit 1)),"
                    +"                CONCAT("
                    +"                    (select CONCAT(order_no, '-', status) from arap_cost_order where id = (select cost_order_id from arap_cost_item where ref_order_id = dpr.id limit 1)),"
                    +"                    '<br/>',"
                    +"                    (select group_concat(concat(order_no, '-', status) separator '<br/>') from arap_cost_invoice_application_order ca "
                    +"                        left join cost_application_order_rel car"
                    +"                         on ca.id= car.application_order_id where car.cost_order_id= (select cost_order_id from arap_cost_item where ref_order_id = dpr.id limit 1))"
                    +"                )"
                    +"        )"
                    +"    )"
                    +" ) audit_status,"
                    + " ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_from),"
                    + " (SELECT NAME FROM location WHERE CODE = dpr.route_from )) route_from,"
                    + " ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_to),"
                    + " (SELECT NAME FROM location WHERE CODE = dpr.route_to)) route_to,"
                    + " (CASE tor.arrival_mode WHEN 'gateIn' THEN w.warehouse_name WHEN 'delivery' THEN tor.receiving_address WHEN 'deliveryToFactory' THEN tor.receiving_address WHEN 'deliveryToWarehouse' OR 'deliveryToFachtoryFromWarehouse' THEN w.warehouse_name ELSE tor.receiving_address END) receivingunit, c.abbr spname,"
                    + " ( SELECT CASE WHEN tor.cargo_nature = 'ATM' THEN ( SELECT count(toid.id) FROM   transfer_order_item_detail toid WHERE toid.depart_id = dpr.id ) WHEN tor.cargo_nature = 'cargo'  "
                    + " THEN ( SELECT sum(toi.amount) FROM "
                    + " depart_order dpr2 LEFT JOIN depart_transfer dt on dt.depart_id = dpr2.id LEFT JOIN transfer_order_item toi on toi.order_id = dt.order_id "
                    + " where dpr2.id = dpr.id ) END ) amount,"
                    //+ " (select count(*) from transfer_order_item_detail where depart_id =dpr.id ) as amount,"
                    + " round((select count(id) * volume as volume from transfer_order_item_detail where depart_id =dpr.id),2) volume,"
                    + " round((select count(id) * weight as weight from transfer_order_item_detail where depart_id =dpr.id),2) weight,"
                    + " DATE(dpr.create_stamp) create_stamp,"
                    + " ifnull(ul.c_name, ul.user_name) creator,tor.customer_order_no customer_order_no,"
                    + " null as ref_no,"
                    + " dpr.transfer_type business_type, "
                    + " dpr.booking_note_number as booking_note_number,"
                    + " null as customer_delivery_no,"
                    + " (select ifnull(sum(dofi.amount),0) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id  and fi.type = '应付' and IFNULL(dofi.cost_source,'') != '对账调整金额') pay_amount, "
                    + " (SELECT sum(amount) FROM depart_order_fin_item dofi LEFT JOIN fin_item fi ON fi.id = dofi.fin_item_id WHERE dofi.depart_order_id = dpr.id AND fi.type = '应付') change_amount,"
                    + " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '<br/>') transfer_order_no,"
                    + " dpr.sign_status return_order_collection,"
                    + " dpr.remark,"
                    + " DATE(dpr.departure_time) as depart_time, "
                    + " group_concat( distinct(select cast(tor.planning_time AS CHAR) from transfer_order tor where tor.id = dtr.order_id) separator '<br/>') planning_time,"
                    + " oe.office_name office_name,"
                    + " c1.abbr cname,"
//                  + " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='运输费' and fi.type = '应付') transport_cost, "
//                  + " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='搬运费' and fi.type = '应付') carry_cost,"
//                  + " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='上楼费' and fi.type = '应付') climb_cost,"
//                  + " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='保险费' and fi.type = '应付') insurance_cost,"
//                  + " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='提货费' and fi.type = '应付') take_cost, "
//                  + " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='安装费' and fi.type = '应付') anzhuang_cost,"
//                  + " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='仓储费' and fi.type = '应付') cangchu_cost,"
//                  + " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='其他费用' and fi.type = '应付') other_cost"
                    + " 0 AS transport_cost,"
                    + " 0 AS carry_cost,"
                    + " 0 AS climb_cost,"
                    + " 0 AS insurance_cost,"
                    + " 0 AS take_cost,"
                    + " 0 AS anzhuang_cost,"
                    + " 0 AS cangchu_cost,"
                    + " 0 AS other_cost"
                    + " from depart_order dpr "
                    + " left join depart_transfer dtr on dtr.depart_id = dpr.id"
                    + " left join transfer_order tor on tor.id = dtr.order_id "
                    + " left join party p1 on tor.customer_id = p1.id"
                    + " left join contact c1 on p1.contact_id = c1.id"
//                  + " left join transfer_order_item toi on toi.order_id = tor.id "
                    + " left join depart_order_fin_item dofi on dofi.depart_order_id = dpr.id "
//                  + " left join product prod on toi.product_id = prod.id "
                    + " left join user_login ul on ul.id = dpr.create_by left join party p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
                    + " LEFT JOIN warehouse w ON w.id = tor.warehouse_id"
                    + " left join office oe on oe.id = tor.office_id"
                    + "  where  unix_timestamp(dpr.create_stamp) > unix_timestamp('2015-06-01 10:34:36') and (ifnull(dtr.depart_id, 0) > 0) "
                    //+ " and dpr.audit_status='新建'  "
                    + " and dpr.combine_type='DEPART'"
                    + "     and p.party_type='SERVICE_PROVIDER'  group by dpr.id"
                    + " union "
                    + " select distinct dpr.id,"
                    + " dpr.depart_no order_no,"
                    + " dpr.status,"
                    +" IF( dpr.audit_status ='新建', '应付明细未确认', "
                    +"      if("
                    +"          (select count(1) from arap_cost_item where ref_order_id = dpr.id)=0,"
                    +"          '应付明细已确认',"
                    +"         if("
                    +"          (select count(1) from cost_application_order_rel where cost_order_id = (select cost_order_id from arap_cost_item  where ref_order_id = dpr.id limit 1))=0, "
                    +"              (select CONCAT(order_no, '-', status) from arap_cost_order where id = (select cost_order_id from arap_cost_item  where ref_order_id = dpr.id limit 1)),"
                    +"                 CONCAT("
                    +"                  (select CONCAT(order_no, '-', status) from arap_cost_order where id = (select cost_order_id from  arap_cost_item where ref_order_id = dpr.id limit 1)),"
                    +"                     '<br/>',"
                    +"                  (select group_concat(concat(order_no, '-', status) separator '<br/>') from  arap_cost_invoice_application_order ca "
                    +"                      left join cost_application_order_rel car"
                    +"                          on ca.id= car.application_order_id where car.cost_order_id= (select cost_order_id from arap_cost_item  where ref_order_id = dpr.id limit 1))"
                    +"              )"
                    +"         )"
                    +"      )"
                    +"  ) audit_status,"
                    + " ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_from),"
                    + " (SELECT NAME FROM location WHERE CODE = dpr.route_from )) route_from,"
                    + " ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_to),"
                    + " (SELECT NAME FROM location WHERE CODE = dpr.route_to)) route_to,"
                    + " (CASE tor.arrival_mode WHEN 'gateIn' THEN w.warehouse_name WHEN 'delivery' THEN tor.receiving_address WHEN 'deliveryToFactory' THEN tor.receiving_address WHEN 'deliveryToWarehouse' OR 'deliveryToFachtoryFromWarehouse' THEN w.warehouse_name ELSE tor.receiving_address END) receivingunit, c.abbr spname,"
                    + " ( SELECT CASE WHEN tor.cargo_nature = 'ATM' THEN ( SELECT count(toid.id) FROM transfer_order_item_detail toid"
                    + " WHERE  toid.pickup_id = dpr.id ) WHEN tor.cargo_nature = 'cargo' THEN ( SELECT sum(toi.amount) FROM "
                    + " depart_order dpr2 LEFT JOIN depart_transfer dt on dt.pickup_id = dpr2.id LEFT JOIN transfer_order_item toi on toi.order_id = dt.order_id "
                    + " where dpr2.id = dpr.id ) END ) amount,"
                    //+ " (select count(id) from transfer_order_item_detail where pickup_id = pofi.pickup_order_id)as amount,"
                    + " round((SELECT sum(volume)   FROM transfer_order_item_detail WHERE   pickup_id = dtr.pickup_id),2) volume,"
                    + " round((SELECT   sum(weight) FROM transfer_order_item_detail WHERE pickup_id = dtr.pickup_id ),2) weight,"
                    + " DATE(dpr.create_stamp) create_stamp, ifnull(ul.c_name, ul.user_name) creator,tor.customer_order_no customer_order_no,"
                    + " null as ref_no,"
                    + " '提货' business_type, "
                    + " null as booking_note_number,"
                    + " null as customer_delivery_no,"
                    + " (select ifnull(sum(pofi.amount),0) from pickup_order_fin_item pofi  where pofi.pickup_order_id = dpr.id and fin_item_id!=7 and IFNULL(pofi.cost_source,'') != '对账调整金额') pay_amount, "
                    + " (select sum(amount) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付') change_amount,"
                    + " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '<br/>') transfer_order_no,"
                    + " dpr.sign_status return_order_collection,"
                    + " dpr.remark,"
                    + " DATE(dpr.turnout_time) as depart_time,"
                    + " group_concat(distinct(select cast(tor.planning_time AS CHAR) from transfer_order tor where tor.id = dtr.order_id) separator '<br/>') planning_time,"
                    + " oe.office_name office_name,"
                    + " c1.abbr cname,"
//                  + " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='运输费') as transport_cost,"
//                  + " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='搬运费') as carry_cost,"
//                  + " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='上楼费') as climb_cost,"
//                  + " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='保险费') as insurance_cost,"
//                  + " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='提货费') as take_cost,"
//                  + " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='安装费') as anzhuang_cost,"
//                  + " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='仓储费') as cangchu_cost,"
//                  + " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='其他费用') as other_cost"
                    + " 0 AS transport_cost,"
                    + " 0 AS carry_cost,"
                    + " 0 AS climb_cost,"
                    + " 0 AS insurance_cost,"
                    + " 0 AS take_cost,"
                    + " 0 AS anzhuang_cost,"
                    + " 0 AS cangchu_cost,"
                    + " 0 AS other_cost"
                    + " from depart_order dpr "
                    + " left join depart_transfer dtr on dtr.pickup_id = dpr.id"
                    + " left join transfer_order tor on tor.id = dtr.order_id "
                    + " left join party p1 on tor.customer_id = p1.id"
                    + " left join contact c1 on p1.contact_id = c1.id"
//                  + " left join transfer_order_item toi on toi.order_id = tor.id "
//                  + " left join transfer_order_item_detail toid on toid.order_id = tor.id "
//                  + " left join product prod on toi.product_id = prod.id "
                    //+ " left join pickup_order_fin_item pofi on pofi.pickup_order_id = dtr.pickup_id"
                    + " left join user_login ul on ul.id = dpr.create_by left join party p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
                    + " LEFT JOIN warehouse w ON w.id = tor.warehouse_id"
                    + " left join office oe on oe.id = tor.office_id "
                    + " where unix_timestamp(dpr.create_stamp) > unix_timestamp('2015-06-01 10:34:36') "
                    + " and (ifnull(dtr.pickup_id, 0) > 0) "
//                    + " and dpr.audit_status='新建' "
                    + " and p.party_type='SERVICE_PROVIDER' and dpr.combine_type='PICKUP' "
                    + " group by dpr.id"
                    + " union "
                    + " select distinct ior.id,"
                    + " ior.order_no order_no,"
                    + " ior.status,"
                    + " IF( ior.audit_status ='新建', '应付明细未确认', "
                    + "     if("
                    + "         (select count(1) from arap_cost_item where ref_order_id = ior.id)=0,"
                    + "          '应付明细已确认',"
                    + "         if("
                    + "             (select count(1) from cost_application_order_rel where cost_order_id = (select cost_order_id from arap_cost_item where ref_order_id = ior.id limit 1))=0, "
                    + "                 (select CONCAT(order_no, '-', status) from arap_cost_order where id = (select cost_order_id from arap_cost_item where ref_order_id = ior.id limit 1)),"
                    + "                 CONCAT("
                    + "                     (select CONCAT(order_no, '-', status) from arap_cost_order where id = (select cost_order_id from arap_cost_item where ref_order_id = ior.id limit 1)),"
                    + "                     '<br/>',"
                    + "                     (select group_concat(concat(order_no, '-', status) separator '<br/>') from arap_cost_invoice_application_order ca "
                    + "                         left join cost_application_order_rel car"
                    + "                          on ca.id= car.application_order_id where car.cost_order_id= (select cost_order_id from arap_cost_item where ref_order_id = ior.id limit 1))"
                    + "                 )"
                    + "         )"
                    + "     )"
                    + "  ) audit_status,"
                    + " ifnull((SELECT NAME FROM location WHERE CODE = tor.route_from),"
                    + " (SELECT NAME FROM location WHERE CODE = tor.route_from )) route_from,"
                    + " ifnull((SELECT NAME FROM location WHERE CODE = tor.route_to),"
                    + " (SELECT NAME FROM location WHERE CODE = tor.route_to)) route_to,"
                    + " '' receivingunit, c.abbr spname,"
//                  + " (select sum(toi.amount) from transfer_order_item toi where toi.order_id in(select tro.id from transfer_order tro where tro.insurance_id  = ior.id )) amount,"
//                  + " (select round(sum(ifnull(prod.volume,toi.volume)*amount),2) from transfer_order_item toi left join product prod on toi.product_id = prod.id  where toi.order_id in(select tro.id from transfer_order tro where tro.insurance_id  = ior.id )) volume,"
//                  + " (select round(sum(ifnull(prod.weight,toi.weight)*amount),2) from transfer_order_item toi left join product prod on toi.product_id = prod.id  where toi.order_id in(select tro.id from transfer_order tro where tro.insurance_id  = ior.id )) weight,"
                    + " 0 amount,"
                    + " 0 volume,"
                    + " 0 weight,"
                    + " ior.create_stamp create_stamp,"
                    + " ifnull(ul.c_name, ul.user_name) creator,tor.customer_order_no customer_order_no,"
                    + " null as ref_no,"
                    + " '保险' business_type, "
                    + " null as booking_note_number,"
                    + " null as customer_delivery_no,"
                    + " (select ifnull(sum(ifi.insurance_amount),0) from insurance_fin_item ifi  where ifi.insurance_order_id = ior.id and IFNULL(ifi.cost_source,'') != '对账调整金额') pay_amount, "
                    + " round((SELECT sum(insurance_amount) FROM insurance_fin_item dofi LEFT JOIN fin_item fi ON fi.id = dofi.fin_item_id WHERE dofi.insurance_order_id = ior.id AND fi.type = '应付' ),2) change_amount,"
                    + " group_concat(distinct tor.order_no separator '<br/>') transfer_order_no,"
                    + " ior.sign_status return_order_collection,"
                    + " ior.remark,"
                    + " ior.create_stamp as depart_time,"
                    + " cast(tor.planning_time AS CHAR) planning_time,"
                    + " oe.office_name office_name,"
                    + " c1.abbr cname,"
                    + " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='运输费') as transport_cost,"
                    + " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='搬运费') as carry_cost,"
                    + " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='上楼费') as climb_cost,"
                    + " (select sum(ifi.insurance_amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='保险费') as insurance_cost,"
                    + " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='提货费') as take_cost,"
                    + " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='安装费') as anzhuang_cost,"
                    + " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='仓储费') as cangchu_cost,"
                    + " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='其他费用') as other_cost"
                    + " from insurance_order ior "
                    + " left join insurance_fin_item ifit on ifit.insurance_order_id = ior.id"
                    + " left join transfer_order tor on ior.id = tor.insurance_id "
                    + " left join party p1 on tor.customer_id = p1.id"
                    + " left join contact c1 on p1.contact_id = c1.id"
                    //+ " left join transfer_order_item toi on toi.order_id = tor.id "
                    + " left join user_login ul on ul.id = ior.create_by"
                    + " left join party p on ior.insurance_id = p.id "
                    + " left join contact c on p.contact_id = c.id "
                    + " left join office oe on oe.id = tor.office_id "
                    + " where unix_timestamp(ior.create_stamp) > unix_timestamp('2015-06-01 10:34:36') "
//                    + " and ior.audit_status='新建' "
                    + " and p.party_type = 'INSURANCE_PARTY' and ifit.id is not null group by ior.id "
                    + " union "
                    + " SELECT DISTINCT amco.id,amco.order_no, amco.STATUS,"
                    +" IF( amco.audit_status ='新建', '应付明细未确认', "
                    +"     if("
                    +"         (select count(1) from arap_cost_item where ref_order_id = amco.id)=0,"
                    +"          '应付明细已确认',"
                    +"         if("
                    +"             (select count(1) from cost_application_order_rel where cost_order_id = (select cost_order_id from arap_cost_item where ref_order_id = amco.id limit 1))=0, "
                    +"                 (select CONCAT(order_no, '-', status) from arap_cost_order where id = (select cost_order_id from arap_cost_item where ref_order_id = amco.id limit 1)),"
                    +"                 CONCAT("
                    +"                     (select CONCAT(order_no, '-', status) from arap_cost_order where id = (select cost_order_id from arap_cost_item where ref_order_id = amco.id limit 1)),"
                    +"                     '<br/>',"
                    +"                     (select group_concat(concat(order_no, '-', status) separator '<br/>') from arap_cost_invoice_application_order ca "
                    +"                         left join cost_application_order_rel car"
                    +"                          on ca.id= car.application_order_id where car.cost_order_id= (select cost_order_id from arap_cost_item where ref_order_id = amco.id limit 1))"
                    +"                 )"
                    +"         )"
                    +"     )"
                    +"  ) audit_status,"
                    + " l.name route_from,"
                    + " l1.name route_to,'' receivingunit,c.abbr spname,NULL as amount,NULL as volume,NULL as weight,"
                    + " amco.create_stamp, ifnull(ul.c_name, ul.user_name) creator,'' customer_order_no,null as ref_no,"
                    + " '成本单' business_type,"
                    + " NULL as booking_note_number,null as customer_delivery_no,"
                    + " ifnull(amco.total_amount,0) pay_amount,"
                    + " (SELECT sum(amount) FROM arap_misc_cost_order_item amcoi LEFT JOIN fin_item fi ON fi.id = amcoi.fin_item_id WHERE amcoi.misc_order_id = amco.id AND fi.type = '应付') change_amount,"
                    + " NULL as transfer_order_no,NULL as return_order_collection,amco.remark remark,"
                    + " NULL as depart_time, NULL as planning_time, o.office_name,c1.abbr cname,"
                    + " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
                    + " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
                    + " WHERE amcoi.misc_order_id = amco.id"
                    + " AND fi.NAME = '运输费') transport_cost,"
                    + " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
                    + " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
                    + " WHERE amcoi.misc_order_id = amco.id "
                    + " AND fi.NAME = '搬运费') carry_cost,"
                    + " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
                    + " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
                    + " WHERE amcoi.misc_order_id = amco.id"
                    + " AND fi.NAME = '上楼费') climb_cost,"
                    + " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
                    + " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
                    + " WHERE amcoi.misc_order_id = amco.id"
                    + " AND fi.NAME = '提货费') insurance_cost,"
                    + " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
                    + " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
                    + " WHERE amcoi.misc_order_id = amco.id"
                    + " AND fi.NAME = '保险费') take_cost,"
                    + " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
                    + " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
                    + " WHERE amcoi.misc_order_id = amco.id"
                    + " AND fi.NAME = '安装费') anzhuang_cost,"
                    + " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
                    + " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
                    + " WHERE amcoi.misc_order_id = amco.id"
                    + " AND fi.NAME = '仓库费') cangchu_cost,"
                    + " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
                    + " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
                    + " WHERE amcoi.misc_order_id = amco.id"
                    + " AND fi.NAME = '其他费用') other_cost"
                    + " FROM arap_misc_cost_order amco"
                    + " LEFT JOIN user_login ul ON ul.id = amco.create_by"
                    + " LEFT JOIN party p1 ON amco.customer_id = p1.id"
                    + " LEFT JOIN contact c1 ON p1.contact_id = c1.id"
                    + " LEFT JOIN party p ON amco.sp_id = p.id"
                    + " LEFT JOIN contact c ON p.contact_id = c.id"
                    + " LEFT JOIN location l ON amco.route_from=l.code"
                    + " LEFT JOIN location l1 ON amco.route_to=l1.code"
                    + " LEFT JOIN office o ON o.id=amco.office_id"
                    + " where amco.type = 'biz' and amco.total_amount!=0"
                    + " GROUP BY amco.id) as A ";
            String condition = "";
          
            if(orderNo != null || sp != null || no != null || beginTime != null
                || endTime !=null || status != null || type != null){
                if (beginTime == null || "".equals(beginTime)) {
                    beginTime = "1970-01-01";
                }
                if (endTime == null || "".equals(endTime)) {
                    endTime = "2037-12-31";
                }
                if (plantime == null || "".equals(plantime)) {
                    plantime = "1970-01-01";
                }
                if (arrivaltime == null || "".equals(arrivaltime)) {
                    arrivaltime = "2037-12-31";
                }
                 
                condition =  " where ifnull(order_no,'') like '%" + no + "%' "
                        + " and ifnull(transfer_order_no,'') like '%" + orderNo + "%' "
                        + " and ifnull(status,'') like '%" + status + "%' "
                        + " and ifnull(spname,'') like '%" + sp + "%' "
                        + " and ifnull(depart_time, '1970-01-01') between '" + beginTime + "' and '" + endTime + " 23:59:59' "
                        + " and ifnull(business_type,'') like '%" + type + "%'"
                        + " and ifnull(route_from,'') like '%" + route_from + "%'"
                        + " and ifnull(route_to,'') like '%" + route_to + "%'"
                        + " and ifnull(booking_note_number,'') like '%" + booking_note_number + "%'"
                        + " and ifnull(planning_time, '1970-01-01') between '" + plantime + "' and '" + arrivaltime + " 23:59:59' "
                        + " and ifnull(cname,'') like '%" + customer_name + "%'"
                        + " and ifnull(status, '') != '手动删除'";
                if (StringUtils.isNotEmpty(serial_no)){
                    condition += " and serial_no like '%" + serial_no + "%'";
                }
                if (StringUtils.isNotEmpty(sign_no)){
                    condition += " and ref_no like '%" + sign_no + "%'";
                }
            }
            if (condition == "" ){
                condition=" where ifnull(status, '') != '手动删除' ";
            }
            sqlTotal = " select count(1) total from (" + sql + condition + ") as B"; 
            Record rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));
            
            long sTime = Calendar.getInstance().getTimeInMillis();
            
            String orderByStr = " order by A.depart_time desc ";
            if(colName.length()>0){
                orderByStr = " order by A."+colName+" "+sortBy;
            }
            List<Record> BillingOrders = Db.find(sql + condition + orderByStr + sLimit);
            long eTime = Calendar.getInstance().getTimeInMillis();
            logger.debug("time cost:" + (eTime-sTime));
            
            Map BillingOrderListMap = new HashMap();
            BillingOrderListMap.put("sEcho", pageIndex);
            BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
            BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

            BillingOrderListMap.put("aaData", BillingOrders);

            renderJson(BillingOrderListMap);
         
    }
	
	
}
