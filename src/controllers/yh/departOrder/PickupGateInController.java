package controllers.yh.departOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.PickupGateInArapItem;
import models.PickupGateInOrder;
import models.UserLogin;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.OfficeController;
import controllers.yh.util.DbUtils;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PickupGateInController extends Controller {

	private Logger logger = Logger.getLogger(PickupGateInController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_LIST})
	public void index() {
		render("/yh/pickupGateIn/list.html");
	}
	
	public void create() {
		String departId = getPara("departId");
		String office_id = getPara("office_id");
		
		//获取明细
		String itemSql = "select "
				+ " tor.order_no,GROUP_CONCAT(toi.item_no) item_no,"
				+ " GROUP_CONCAT(toi.item_name) item_name, c.abbr, tor.planning_time, tor.remark,"
				+ " ( case when tor.operation_type != 'out_source'"
				+ " THEN ( SELECT dt.amount FROM depart_order deo"
				+ " LEFT JOIN depart_pickup dpi ON dpi.depart_id = deo.id"
				+ " LEFT JOIN depart_transfer dt ON dt.pickup_id = dpi.pickup_id"
				+ " WHERE"
				+ " deo.id = "+departId
				+ " AND tor.id = dt.order_id"
				+ " GROUP BY toi.id )"
				+ " when tor.operation_type = 'out_source'"
				+ " then sum(toi.amount) end ) amount"
				+ " from transfer_order tor "
				+ " LEFT JOIN party p on p.id = tor.customer_id"
				+ " LEFT JOIN contact c on c.id  = p.contact_id"
				+ " LEFT JOIN transfer_order_item toi on toi.order_id = tor.id"
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " where dt.depart_id = "+departId
				+ " GROUP BY tor.id";

		List<Record> finItemList = Db.find("select * from fin_item where type='应付'");
        setAttr("finItemList", finItemList);
		List<Record> reList = Db.find(itemSql);
		setAttr("itemList", reList);
		setAttr("departId", departId);
		setAttr("office_id", office_id);

		render("/yh/pickupGateIn/edit.html");
	}
	
	@Before(Tx.class)
	public void save() throws Exception {		
		String jsonStr=getPara("params");
    	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
         
        PickupGateInOrder order = null;
		String id = (String) dto.get("id");
		Long user_id =  LoginUserController.getLoginUserId(this);
		if (StringUtils.isNotEmpty(id)) {
			order = PickupGateInOrder.dao.findById(id);
			DbUtils.setModelValues(dto, order);
			order.update();
		} else {
			order = new PickupGateInOrder();
			DbUtils.setModelValues(dto, order);
			
			//需后台处理的字段
			order.set("order_no", OrderNoGenerator.getNextOrderNo("PGI"));
			order.set("create_by", user_id);
			order.set("create_stamp", new Date());
			order.set("status", "新建");
			order.save();
			id = order.get("id").toString();
			
			String depart_id = order.get("depart_id").toString();
			Record re = new Record();
			re.set("order_id", id);
			re.set("depart_id", depart_id);
			Db.save("pickup_gate_in_item", re);
		}
		
		List<Map<String, String>> costList = (ArrayList<Map<String, String>>)dto.get("cost_list");
		//DbUtils.handleList(costList, id, PickupGateInArapItem.class, "order_id");
		
		for(Map<String, String> map: costList){
			String cost_id = (String)map.get("id");
			String fin_item_id = (String)map.get("fin_item_id");
			String amount = (String)map.get("amount");
			String remark = (String)map.get("remark");
			String action = (String)map.get("action");
			
			if(StringUtils.isNotBlank(cost_id)){
				if("DELETE".equals(action)){
					Db.deleteById("pickup_gate_in_arap_item", cost_id);
				}else if("UPDATE".equals(action)){
					//更新
					Record item = Db.findById("pickup_gate_in_arap_item", cost_id);
					if(StringUtils.isNotBlank(fin_item_id)){
						item.set("fin_item_id", fin_item_id);
					}
					if(StringUtils.isNotBlank(amount)){
						item.set("amount", amount);
					}
					if(StringUtils.isNotBlank(remark)){
						item.set("remark", remark);
					}
					item.set("create_time", new Date());
					item.set("order_id", id);
					Db.update("pickup_gate_in_arap_item", item);
				}
			}else{
				//新建
				Record item = new Record();
				if(StringUtils.isNotBlank(fin_item_id)){
					item.set("fin_item_id", fin_item_id);
				}
				if(StringUtils.isNotBlank(amount)){
					item.set("amount", amount);
				}
				if(StringUtils.isNotBlank(remark)){
					item.set("remark", remark);
				}
				item.set("order_id", id);
				Db.save("pickup_gate_in_arap_item", item);
			}
		}
		
		Long create_by = order.getLong("create_by");
		UserLogin ulRe = UserLogin.dao.findById(create_by);
		String create_name = ulRe.getStr("c_name");
		
		Record orderRe = order.toRecord();
		orderRe.set("create_name", create_name);
		
		renderJson(orderRe);
	}
	
	
	public void edit(){
		String id = getPara("id");
		
		PickupGateInOrder order = PickupGateInOrder.dao.findById(id);
		setAttr("order",order);
		
		Long sp_id = order.getLong("sp_id");
		Record spRe = Db.findFirst("select c.* from party p"
				+ " left join contact c on c.id = p.contact_id"
				+ " where p.id = ?",sp_id);
		setAttr("spRe", spRe);
		
		Long create_by = order.getLong("create_by");
		UserLogin ulRe = UserLogin.dao.findById(create_by);
		setAttr("ulRe", ulRe);
		
		List<Record> costList = Db.find("select pgia.*,fi.name fin_item_name from pickup_gate_in_arap_item pgia"
				+ "	left join fin_item fi on fi.id = pgia.fin_item_id where pgia.order_id = ?",id);
		setAttr("costList", costList);
		
		List<Record> finItemList = Db.find("select * from fin_item where type='应付'");
        setAttr("finItemList", finItemList);
		
		//获取明细
		String itemSql = "select "
						+ " tor.order_no,GROUP_CONCAT(toi.item_no) item_no,"
						+ " GROUP_CONCAT(toi.item_name) item_name, c.abbr, tor.planning_time, tor.remark,"
						+ " ( case when tor.operation_type != 'out_source'"
						+ " THEN ( SELECT dt.amount FROM depart_order deo"
						+ " LEFT JOIN depart_pickup dpi ON dpi.depart_id = deo.id"
						+ " LEFT JOIN depart_transfer dt ON dt.pickup_id = dpi.pickup_id"
						+ " WHERE"
						+ " deo.id = pgi.depart_id"
						+ " AND tor.id = dt.order_id"
						+ " GROUP BY toi.id )"
						+ " when tor.operation_type = 'out_source'"
						+ " then sum(toi.amount) end ) amount"
						+ " from transfer_order tor "
						+ " LEFT JOIN party p on p.id = tor.customer_id"
						+ " LEFT JOIN contact c on c.id  = p.contact_id"
						+ " LEFT JOIN transfer_order_item toi on toi.order_id = tor.id"
						+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
						+ "	left join pickup_gate_in_item pgi on pgi.depart_id = dt.depart_id"
						+ " where pgi.order_id = ?"
						+ " GROUP BY tor.id";
		List<Record> itemList = Db.find(itemSql,id);
		
		setAttr("itemList", itemList);
		
		render("/yh/pickupGateIn/edit.html");
	}

	
	@RequiresPermissions(value = {PermissionConstant.PERMISSION_DO_LIST})
	public void list() {
		String orderNo = getPara("order_no");
		String sp_id= getPara("sp_id");
		String status= getPara("status_filter");
		String beginTime = getPara("begin_time");
		String endTime = getPara("end_time");

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		String sql = "";
		String totalSql = "";
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String conditions=" where 1=1 ";
		if (StringUtils.isNotEmpty(orderNo)){
        	conditions+=" and gg.order_no like '%"+orderNo+"%'";
        }
		if (StringUtils.isNotEmpty(sp_id)){
        	conditions+=" and gg.sp_id like '%"+sp_id+"%'";
        }
		if (StringUtils.isNotEmpty(status)){
        	conditions+=" and gg.status='"+status+"'";
        }

        if (StringUtils.isNotEmpty(beginTime)&&StringUtils.isNotEmpty(endTime)){
        	conditions+= " and (gg.create_stamp between'"+beginTime+"' and '"+endTime+" 23:59:59')";
        }else{
        	conditions+=" and (gg.create_stamp between '2000-1-1' and '3000-1-1')";
        }
       
        conditions += " and gg.office_id in (select office_id from user_office where user_name='"
				+ currentUser.getPrincipal()
				+ "') GROUP BY gg.id";
        
      	sql = "SELECT "
      			+ " gg.id,"
      			+ " gg.order_no,"
      			+ " c.abbr sp_name,"
      			+ " cin.driver,"
      			+ " gg.status,"
      			+ " gg.audit_status,"
      			+ " gg.pickup_mode,"
      			+ " gg.create_stamp,"
      			+ " gg.remark"
      			+ " FROM `pickup_gate_in_order` gg"
      			+ " LEFT JOIN party p on p.id = gg.sp_id "
      			+ " LEFT JOIN contact c on c.id = p.contact_id"
      			+ " LEFT JOIN depart_order dor on dor.id = gg.depart_id"
      			+ " LEFT JOIN carinfo cin on cin.id  = gg.car_id"
      			+ "   ";
				

		Record rec = Db.findFirst("select count(1) total from (" + sql+ conditions +" ) B");
		List<Record> departOrders = Db.find( sql + conditions  + sLimit);
		Map map = new HashMap();
		map.put("sEcho", pageIndex);
		map.put("iTotalRecords", rec.getLong("total"));
		map.put("iTotalDisplayRecords", rec.getLong("total"));
		map.put("aaData", departOrders);
		renderJson(map);
	}
	
	
	public void tableList(){
		String order_id = getPara("order_id");
		List<Record> costList = Db.find("select pgia.*,fi.name fin_item_name from pickup_gate_in_arap_item pgia"
				+ "	left join fin_item fi on fi.id = pgia.fin_item_id where pgia.order_id = ?",order_id);
		Map map = new HashMap();
		map.put("sEcho", 0);
		map.put("iTotalRecords", costList.size());
		map.put("iTotalDisplayRecords", costList.size());
		map.put("aaData", costList);
		renderJson(map);
	}
	
	
	public void gateIn(){
		String order_id = getPara("order_id");
		PickupGateInOrder order = PickupGateInOrder.dao.findById(order_id);
		order.set("status", "已入库");
		order.set("gate_in_by", LoginUserController.getLoginUserId(this));
		order.set("gate_in_time", new Date());
		order.update();
		
		renderJson(order);
	}

}
