package controllers.yh.statusReport;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.TransferOrder;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.order.TransferOrderController;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class StatusReportColler extends Controller{
	
	private Logger logger = Logger.getLogger(TransferOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	public void index() {		
		render("/yh/statusReport/productStatusReport.html");
	}
	
	public void findTransferOrdertatus() {
		String order_no = getPara("order_no");
		String customer_id = getPara("customer_id");
		String customer_order_no = getPara("customer_order_no");
		String item_no = getPara("item_no");
		String serial_no = getPara("serial_no");
		String sLimit = "";
		
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		// 获取总条数
		String totalSql = "";
		String sql = "";
		TransferOrder order = null;
		Map orderMap = new HashMap();
		/*if((order_no == null || order_no.equals("")) && (customer_order_no == null || customer_order_no.equals(""))) {
			orderMap.put("sEcho", 0);
			orderMap.put("iTotalRecords", 0);
			orderMap.put("iTotalDisplayRecords", 0);
			orderMap.put("aaData", null);
			renderJson(orderMap);
			return;
		}else{*/
			if(order_no != null && !"".equals(order_no)){
				order = TransferOrder.dao.findFirst("select * from transfer_order where order_no = '" + order_no + "';");
			}else if(customer_order_no != null &&!"".equals(customer_order_no)){
				order = TransferOrder.dao.findFirst("select * from transfer_order where customer_order_no = '" + customer_order_no + "';");
			}else if(serial_no != null &&!"".equals(serial_no)){
				order = TransferOrder.dao.findFirst("select tor.* from transfer_order_item_detail toid left join transfer_order tor on tor.id = toid.order_id where toid.serial_no = '" + serial_no + "';");
			}
		//}
		if(order != null){
			long order_id = order.getLong("id");
			//ATM，普货有单品
			if("ATM".equals(order.get("cargo_nature")) || (!"ATM".equals(order.get("cargo_nature")) && "cargoNatureDetailYes".equals(order.get("cargo_nature_detail")))){
				totalSql = "select count(0) total from transfer_order_item_detail toid"
						+ " left join transfer_order tor on tor.id = toid.order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join depart_transfer pick on pick.id = toid.pickup_id"
						+ " left join depart_order pkdo on pkdo.id = toid.pickup_id"
						+ " left join depart_transfer depart on depart.id = toid.depart_id"
						+ " left join depart_order dedo on dedo.id = toid.depart_id"
						+ " left join delivery_order dor on dor.id = toid.delivery_id"
						+ " left join return_order ro on ro.delivery_order_id = dor.id"
						+ " where toid.order_id = '" + order_id + "'";
				
				sql ="select toid.serial_no,item_no,tor.order_no transfer_no,tor.status transfer_status,tor.customer_order_no,tor.cargo_nature,tor.cargo_nature_detail,c.abbr customer,pkdo.depart_no pick_no,pkdo.status pick_status,"
						+ " dedo.depart_no depart_no,dedo.status depart_status,dor.order_no delivery_no,dor.status delivery_status,ro.order_no return_no,ro.transaction_status return_status from transfer_order_item_detail toid"
						+ " left join transfer_order tor on tor.id = toid.order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join depart_transfer pick on pick.id = toid.pickup_id"
						+ " left join depart_order pkdo on pkdo.id = toid.pickup_id"
						+ " left join depart_transfer depart on depart.id = toid.depart_id"
						+ " left join depart_order dedo on dedo.id = toid.depart_id"
						+ " left join delivery_order dor on dor.id = toid.delivery_id"
						+ " left join return_order ro on ro.delivery_order_id = dor.id"
						+ " where toid.order_id = '" + order_id + "'";
							
				//有序列号时
				if(!"".equals(serial_no) && serial_no != null){
					totalSql = totalSql + " and toid.serial_no = '" + serial_no + "'";
					sql = sql + " and toid.serial_no = '" + serial_no + "'";
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
			}else{
				/*//普货没单品
				totalSql = "select count(0) total from transfer_order_item toid "
						+ " left join transfer_order tor on tor.id = toid.order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join depart_transfer pick on pick.order_id = tor.id"
						+ " left join depart_order pkdo on pkdo.id = pick.pickup_id"
						+ " left join depart_transfer depart on depart.order_id = tor.id"
						+ " left join depart_order dedo on dedo.id = depart.depart_id"
						+ " left join delivery_order_item dori on dori.transfer_order_id = tor.id"
						+ " left join delivery_order dor on dor.id = dori.delivery_id"
						+ " left join return_order ro on ro.transfer_order_id = tor.id"
						+ " where tor.id = '" + order_id + "'";
				
				sql = "select (select '' ) serial_no,item_no,tor.order_no transfer_no,tor.status transfer_status,tor.customer_order_no,c.abbr customer,pkdo.depart_no pick_no,pkdo.status pick_status,dedo.depart_no depart_no,dedo.status depart_status,"
						+ " dor.order_no delivery_no,dor.status delivery_status,ro.order_no return_no,ro.transaction_status return_status from transfer_order_item toid "
						+ " left join transfer_order tor on tor.id = toid.order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join depart_transfer pick on pick.order_id = tor.id"
						+ " left join depart_order pkdo on pkdo.id = pick.pickup_id"
						+ " left join depart_transfer depart on depart.order_id = tor.id"
						+ " left join depart_order dedo on dedo.id = depart.depart_id"
						+ " left join delivery_order_item dori on dori.transfer_order_id = tor.id"
						+ " left join delivery_order dor on dor.id = dori.delivery_id"
						+ " left join return_order ro on ro.transfer_order_id = tor.id"
						+ " where tor.id = '" + order_id + "'";
				*/
				totalSql = "select count(0) total from transfer_order_item toid "
						+ " left join transfer_order tor on tor.id = toid.order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " where tor.id = '" + order_id + "'";
				
				sql = "select (select '' ) serial_no,item_no,tor.order_no transfer_no,tor.status transfer_status,tor.customer_order_no,tor.cargo_nature,tor.cargo_nature_detail,c.abbr customer,"
						+ " (select dd.depart_no from depart_order dd left join depart_pickup dp on dp.pickup_id = dd.id where dp.order_id = tor.id ) pick_no,"
						+ " (select dd.status from depart_order dd left join depart_pickup dp on dp.pickup_id = dd.id where dp.order_id = tor.id ) pick_status,"
						+ " (select dd.depart_no from depart_order dd left join depart_pickup dp on dp.depart_id = dd.id where dp.order_id = tor.id ) depart_no,"
						+ " (select dd.status from depart_order dd left join depart_pickup dp on dp.depart_id = dd.id where dp.order_id = tor.id ) depart_status,"
						+ " (select group_concat(concat(concat(deor.order_no,'('),deor.status,')') separator '<br>') from delivery_order deor left join delivery_order_item doi on doi.delivery_id = deor.id where doi.transfer_order_id = tor.id) delivery_no,"
						+ " (select ror.order_no from return_order ror where ror.transfer_order_id = tor.id) return_no,"
						+ " (select ror.transaction_status from return_order ror where ror.transfer_order_id = tor.id) return_status from transfer_order_item toid "
						+ " left join transfer_order tor on tor.id = toid.order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " where tor.id = '" + order_id + "'";
				
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
			}
			
			// 获取总条数
			Record rec = Db.findFirst(totalSql);
			logger.debug("total records:" + rec.getLong("total"));
			// 获取当前页的数据
			List<Record> orders =Db.find(sql);
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
	
	
	
}
