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
    	List<Record> offices = Db.find("select o.id,o.office_name,o.is_stop from office o where o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')");
		
    	String search_type = getPara("search_type");
		String order_no = getPara("order_no");
		String customer_id = getPara("customer_id");
		String sp_id = getPara("sp_id");
		String biz_order_no = getPara("biz_order_no");
		String process_status = getPara("process_status");
		String accident_type = getPara("accident_type");
		String accident_date_begin_time = getPara("accident_date_begin_time");
		String accident_date_end_time = getPara("accident_date_end_time");
		
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
		String conditions="  where 1=1 ";
		if (StringUtils.isNotEmpty(order_no)){                                           //运输单
			conditions += " and UPPER(tor.order_no) like '%"+order_no.toUpperCase()+"%'";
		}
		if (StringUtils.isNotEmpty(customer_id)){                                            //-- 应收对账单
			conditions += " and UPPER(deo.ref_no) like '%"+customer_id.toUpperCase()+"%'";
		}
		
		conditions += " and tor.office_id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')";
		
		String sql = "select toi.id,w.id warehouse_id,w.warehouse_name,toi.item_no,toi.item_name,toi.amount,toi.unit from transfer_order_item toi "
				+ " LEFT JOIN transfer_order tor on tor.id = toi.order_id  "
				+ " LEFT JOIN warehouse w on w.id = tor.warehouse_id "
				+ " where tor.warehouse_id is not null";

		 
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
