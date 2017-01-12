package controllers.yh.customerQuery;

import interceptor.SetAttrLoginUserInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.Account;
import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;
import models.DepartOrder;
import models.Location;
import models.ParentOfficeModel;
import models.Party;
import models.UserLogin;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ArapMiscCostOrderDTO;
import models.yh.arap.ArapMiscCostOrderItem;
import models.yh.carmanage.CarSummaryDetailOtherFee;
import models.yh.damageOrder.DamageOrder;
import models.yh.damageOrder.DamageOrderFinItem;
import models.yh.damageOrder.DamageOrderItem;
import models.yh.profile.Contact;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.util.DbUtils;
import controllers.yh.util.LocationUtil;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomerQueryController extends Controller {
    private Logger logger = Logger.getLogger(CustomerQueryController.class);
	Subject currentUser = SecurityUtils.getSubject();

    public void orderStatus() {
		render("/yh/customerQuery/orderStatus/list.html");
    }
    
    public void orderStatusSearch() {
    	String search_type = getPara("search_type");
		String customer_order_no = getPara("customer_order_no");
		String customer_id = getPara("customer_id");
		String route_to = getPara("route_to");
		String begin_time = getPara("begin_time");
		String end_time = getPara("end_time");
		
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
		String conditions="  where 1=1 ";
		if (StringUtils.isNotEmpty(customer_order_no)){                                         
			conditions += " and UPPER(tor.customer_order_no) like '%"+customer_order_no.toUpperCase()+"%'";
		}
		if (StringUtils.isNotEmpty(customer_id)){                                           
			conditions += " and tor.customer_id = '"+customer_id+"'";
		}
		if (StringUtils.isNotEmpty(route_to)){                                           
			conditions += " and l_t. NAME like '%"+route_to+"%'";
		}
		
		if (StringUtils.isEmpty(begin_time)){                                           
			begin_time = "2000-01-01";
		}
		if (StringUtils.isNotEmpty(end_time)){                                           
			end_time = end_time+" 23:59:59";
		}else{
			end_time = "2037-01-01";
		}
		conditions += " and tor.planning_time between '"+begin_time+"' and '"+end_time+"'";
		
		conditions += " and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')";
		String totalsql = ""
				+ " SELECT count(1) total"
				+ " FROM "
				+ " 	transfer_order tor "
				+ conditions;
		
		
		String sql = " SELECT "
				+ " 	tor.order_no, "
				+ " 	tor.customer_order_no, "
				+ " 	l_t. NAME route_to, "
				+ " 	tor.planning_time, "
				+ " 	(select GROUP_CONCAT(cast(dor.departure_time as char) SEPARATOR '<br/>') from depart_order dor "
				+ " 		LEFT JOIN depart_transfer dt on dt.depart_id = dor.id  "
				+ " 	where dt.order_id = tor.id) departure_time, "
				+ " 	sum(toi.amount) amount, "
				+ " 	(case  "
				+ " when tor.`STATUS`='新建' "
				+ " then tor.`STATUS` "
				+ " when tor.`STATUS` = '处理中' "
				+ " then ( "
				+ " 	if((select GROUP_CONCAT(dor.id SEPARATOR '<br/>') from depart_order dor "
				+ " 		LEFT JOIN depart_transfer dt on dt.depart_id = dor.id  "
				+ " 	where dt.order_id = tor.id ) is not null,'在途','已提货')  "
				+ " 	) "
				+ " ELSE "
				+ " if(tor.arrival_mode='delivery','已到达','已到中转仓')  "
				+ " end "
				+ " ) transfer_status, "
				+ " (case  "
				+ " when tor.arrival_mode='delivery' "
				+ " then (select GROUP_CONCAT(cast(receipt_date as char) SEPARATOR '<br/>') from return_order where transfer_order_id = tor.id) "
				+ " else  "
				+ " (select GROUP_CONCAT(cast(ror.receipt_date as char) SEPARATOR '<br/>') from delivery_order dor "
				+ " LEFT JOIN delivery_order_item doi on doi.delivery_id = dor.id "
				+ " LEFT JOIN return_order ror on ror.delivery_order_id = dor.id "
				+ " 	where doi.transfer_order_id = tor.id "
				+ " 	) "
				+ " end "
				+ "  ) signIn_time, "
				+ " (case  "
				+ " when tor.arrival_mode='delivery' "
				+ " then (select GROUP_CONCAT(if(transaction_status='新建','回单在途','已回收') SEPARATOR '<br/>') from return_order where transfer_order_id = tor.id)" 
				+ " else  "
				+ " (select GROUP_CONCAT(if(ror.transaction_status='新建','回单在途','已回收') SEPARATOR '<br/>') from delivery_order dor "
				+ " LEFT JOIN delivery_order_item doi on doi.delivery_id = dor.id "
				+ " LEFT JOIN return_order ror on ror.delivery_order_id = dor.id "
				+ " where doi.transfer_order_id = tor.id and ror.id is not null"
				+ " ) "
				+ " end "
				+ "  ) return_status, "
				+ " (case  "
				+ " when tor.arrival_mode='delivery' "
				+ " then tor.receiving_address "
				+ " else  "
				+ " (select GROUP_CONCAT(ifnull(c.address,dor.receivingunit) SEPARATOR '<br/>') from delivery_order dor "
				+ " LEFT JOIN party p on p.id = dor.notify_party_id "
				+ " LEFT JOIN contact c on c.id = p.contact_id "
				+ " LEFT JOIN delivery_order_item doi on doi.delivery_id = dor.id "
				+ " where doi.transfer_order_id = tor.id "
				+ " 	) "
				+ " end "
				+ "  ) delivery_address "
				+ "  "
				+ " FROM "
				+ " 	transfer_order tor "
				+ " LEFT JOIN location l_t ON l_t. CODE = tor.route_to "
				+ " LEFT JOIN transfer_order_item toi on toi.order_id = tor.id "
				+ conditions
				+ " GROUP BY tor.id ";

		
         Record rec = Db.findFirst(totalsql);
        
         List<Record> BillingOrders = Db.find(sql + sLimit);

         
         Map BillingOrderListMap = new HashMap();
         BillingOrderListMap.put("draw", pageIndex);//显示第几页
         BillingOrderListMap.put("recordsTotal", rec.getLong("total"));
         BillingOrderListMap.put("recordsFiltered", rec.getLong("total"));
         BillingOrderListMap.put("data", BillingOrders);

         renderJson(BillingOrderListMap);
    }
    
    
    
    
    public void orderSerialNoSearch() {
		String serial_no = getPara("serial_no");
		String customer_id = getPara("customer_id");
		String begin_time = getPara("begin_time");
		String end_time = getPara("end_time");
		
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
		String conditions="  where 1=1 ";
		if (StringUtils.isNotEmpty(serial_no)){                                         
			conditions += " and UPPER(toid.serial_no) like '%"+serial_no.toUpperCase()+"%'";
		}
		if (StringUtils.isNotEmpty(customer_id)){                                           
			conditions += " and tor.customer_id = '"+customer_id+"'";
		}

		
		conditions += " and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')";
		String totalsql = ""
				+ " SELECT count(1) total"
				+ " FROM "
				+ " transfer_order_item_detail toid"
				+ " left join transfer_order tor on tor.id = toid.order_id "
				+ conditions;
		
		
		String sql = ""
				+ " select toid.serial_no,ifnull(toid.item_no,toid.item_name) item_no,toid.pieces amount ,"
				+ " l_t.name route_to,toid.notify_party_company delivery_address,toid.notify_party_name,"
				+ " dor.depart_stamp delivery_time,dor.status delivery_status,"
				+ " (case "
				+ " when tor.arrival_mode='delivery'"
				+ " then (select GROUP_CONCAT(if(transaction_status='新建','回单在途','已回收') SEPARATOR '<br/>') from return_order where transfer_order_id = tor.id)" 
				+ " else "
				+ " (select GROUP_CONCAT(if(ror.transaction_status='新建','回单在途','已回收') SEPARATOR '<br/>') from return_order ror"
				+ " 	where ror.delivery_order_id = toid.delivery_id"
				+ " )"
				+ " end"
				+ "  ) return_status,"
				+ " (case "
				+ " when tor.arrival_mode='delivery'"
				+ " then toid.receive_address"
				+ " else "
				+ " ifnull(dor.receivingunit,tor.receiving_unit)"
				+ " end"
				+ "  ) return_unit,"
				+ " (case "
				+ " when tor.arrival_mode='delivery'"
				+ " then toid.notify_party_company"
				+ " else "
				+ " (select ifnull(c.abbr,ifnull(tor.receiving_address,toid.notify_party_company)) from party p "
				+ " LEFT JOIN contact c on c.id = p.contact_id"
				+ " where c.id = dor.notify_party_id)"
				+ " end"
				+ "  ) receive_address"
				+ " from transfer_order_item_detail toid"
				+ " LEFT JOIN delivery_order dor on dor.id = toid.delivery_id"
				+ " LEFT JOIN transfer_order tor on tor.id = toid.order_id"
				+ " LEFT JOIN location l_t on l_t.code = tor.route_to"
				+ conditions;

		
         Record rec = Db.findFirst(totalsql);
        
         List<Record> BillingOrders = Db.find(sql + sLimit);

         
         Map BillingOrderListMap = new HashMap();
         BillingOrderListMap.put("draw", pageIndex);//显示第几页
         BillingOrderListMap.put("recordsTotal", rec.getLong("total"));
         BillingOrderListMap.put("recordsFiltered", rec.getLong("total"));
         BillingOrderListMap.put("data", BillingOrders);

         renderJson(BillingOrderListMap);
    }
    
	
	public void inventory() {
		
	    render("/yh/customerQuery/inventory/list.html");
    }

	public void inventorySearch() {
		String warehouse_id = getPara("warehouse_id");
		String item_no = getPara("item_no");
		
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
		String conditions="";
		if (StringUtils.isNotEmpty(warehouse_id)){                                 
			conditions += " and tor.warehouse_id = '"+warehouse_id+"'";
		}
		if (StringUtils.isNotEmpty(item_no)){                                          
			conditions += " and toi.item_no like '%"+item_no+"%'";
		}
		conditions += "";
		
		String sql = "select toi.id,w.warehouse_name,w.id warehouse_id,toi.item_no,toi.item_name,sum(toi.amount) amount,toi.unit"
				+ " from transfer_order_item toi "
				+ " LEFT JOIN transfer_order tor on tor.id = toi.order_id  "
				+ " left join transfer_order_item_detail toid on toid.order_id = toi.order_id "
				+ " LEFT JOIN warehouse w on w.id = tor.warehouse_id "
				+ " where tor.warehouse_id is not null and tor.status = '已完成' "
				+ conditions
				+ " group by tor.warehouse_id,toi.item_no";

		 
		 String sqlTotal = "select count(1) total from ("+sql+") B";
         Record rec = Db.findFirst(sqlTotal);
         logger.debug("total records:" + rec.getLong("total"));
        
         List<Record> BillingOrders = Db.find(sql + sLimit);

         
         Map BillingOrderListMap = new HashMap();
         BillingOrderListMap.put("draw", pageIndex);//显示第几页
         BillingOrderListMap.put("recordsTotal", rec.getLong("total"));
         BillingOrderListMap.put("recordsFiltered", rec.getLong("total"));
         BillingOrderListMap.put("data", BillingOrders);

         renderJson(BillingOrderListMap);
    }
	
	
	public void getItemDetail(){
  		String warehouse_id = getPara("warehouse_id");
  		String item_no = getPara("item_no");
  		
  		String sLimit = "";
  		String pageIndex = getPara("sEcho");
  		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
  		
  		String sql = " "//有单品
  				+ " select tor.order_no,toid.item_no,toid.serial_no, 1 amount from transfer_order_item_detail toid "
  				+ " LEFT JOIN transfer_order tor on tor.id = toid.order_id"
  				+ " LEFT JOIN depart_order deo on deo.id = toid.depart_id where deo.id = "+warehouse_id
  				+ " and (tor.cargo_nature = 'ATM' "
  				+ " or (tor.cargo_nature = 'cargo' and tor.cargo_nature_detail='cargoNatureDetailYes'))"
  				+ " union all" //普货(无单品)
  				+ " select tor.order_no,toi.item_no,null serial_no,dt.amount from transfer_order_item toi"
  				+ " LEFT JOIN transfer_order tor on tor.id = toi.order_id"
  				+ " LEFT JOIN depart_transfer dt on dt.order_item_id = toi.id"
  				+ " LEFT JOIN depart_pickup dp on dp.pickup_id = dt.pickup_id and dp.order_id = toi.order_id "
  				+ " WHERE"
  				+ " dp.depart_id = "+warehouse_id
  				+ " and (tor.cargo_nature = 'cargo' and tor.cargo_nature_detail='cargoNatureDetailNo')"
  				+ " GROUP BY toi.id "
  				+ " union all "   //外包
  				+ " select tor.order_no,toi.item_no,null serial_no,toi.amount from transfer_order_item toi"
  				+ " LEFT JOIN depart_transfer dt on dt.order_id = toi.order_id"
  				+ " LEFT JOIN transfer_order tor on tor.id = dt.order_id"
  				+ " WHERE"
  				+ " dt.depart_id= "+warehouse_id
  				+ " and tor.cargo_nature = 'cargo' and tor.operation_type = 'out_source' "
  				+ " GROUP BY toi.id" ;
  		Record total = Db.findFirst("select count(1) total from ("+sql+") A");
  		List<Record> re = Db.find(sql+sLimit);
  		Map Map = new HashMap();
  		Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", total.get("total"));
        Map.put("iTotalDisplayRecords", total.get("total"));
        Map.put("aaData", re);
        renderJson(Map);
  	}
}
