package controllers.yh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ReturnOrder;
import models.TransferOrder;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ReturnOrderControllers extends Controller {
	private Logger logger = Logger.getLogger(ReturnOrderControllers.class);

	public void index() {
		if (LoginUserController.isAuthenticated(this))
			render("profile/returnorder/returnOrderList.html");
	}

	public void list() {
		String order_no = getPara("order_no");
		String tr_order_no = getPara("tr_order_no");
		String de_order_no = getPara("de_order_no");
		String stator = getPara("stator");
		String status = getPara("status");
		String time_one = getPara("time_one");
		String time_two = getPara("time_two");

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}
		Map orderMap = new HashMap();
		if (order_no == null && tr_order_no == null && de_order_no == null
		        && stator == null && status == null && time_one == null
		        && time_two == null) {
			// 获取总条数
			String totalWhere = "";
			String sql = "select count(1) total from return_order ro "
					   + "left join transfer_order to on ro.transfer_order_id = to.id "
		                + "left join delivery_order do on ro.delivery_order_id = do.id "
		                + "left join party p on ro.customer_id = p.id "
		                + "left join contact c on p.contact_id = c.id ";
			Record rec = Db.findFirst(sql + totalWhere);
			logger.debug("total records:" + rec.getLong("total"));

			// 获取当前页的数据
			List<Record> orders = Db
			        .find("SELECT ro.*, to.order_no as transfer_order_no, do.order_no as delivery_order_no, c.company_name FROM RETURN_ORDER ro "
			                + "left join transfer_order to on ro.transfer_order_id = to.id "
			                + "left join delivery_order do on ro.delivery_order_id = do.id "
			                + "left join party p on ro.customer_id = p.id "
			                + "left join contact c on p.contact_id = c.id "+sLimit);

			orderMap.put("sEcho", pageIndex);
			orderMap.put("iTotalRecords", rec.getLong("total"));
			orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
			orderMap.put("aaData", orders);

		} else {
			if (time_one == null || "".equals(time_one)) {
				time_one = "1-1-1";
			}
			if (time_two == null || "".equals(time_two)) {
				time_two = "9999-12-31";
			}
			// 获取总条数
			String totalWhere = "";
			String sql = "select count(1) total from return_order ";
			Record rec = Db.findFirst(sql + totalWhere);
			logger.debug("total records:" + rec.getLong("total"));

			// 获取当前页的数据
			List<Record> orders = Db
			        .find("SELECT ro.*, to.order_no as transfer_order_no, do.order_no as delivery_order_no, c.company_name FROM RETURN_ORDER ro "
			                + "left join transfer_order to on ro.transfer_order_id = to.id "
			                + "left join delivery_order do on ro.delivery_order_id = do.id "
			                + "left join party p on ro.customer_id = p.id "
			                + "left join contact c on p.contact_id = c.id "
			                + "where ro.ORDER_NO like '%"
			                + order_no
			                + "%' and  to.order_no like '%"
			                + tr_order_no
			                + "%' and do.order_no like '%"
			                + de_order_no
			                + "%' and ro.CREATOR like '%"
			                + stator
			                + "%' and ro.TRANSACTION_STATUS like'%"
			                + status
			                + "%' and ro.CREATE_DATE between '"
			                + time_one
			                + "' and '" + time_two + "' ");

			orderMap.put("sEcho", pageIndex);
			orderMap.put("iTotalRecords", rec.getLong("total"));
			orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
			orderMap.put("aaData", orders);
		}

		renderJson(orderMap);
	}

	// 查看回单显示
	public void checkorder() {

		int id = Integer.parseInt(getPara("locationName"));

		List<Record> message = new ArrayList<Record>();
			String sql_tr="SELECT ro.*,co.COMPANY_NAME as company_name ,co.ADDRESS as address,co.CONTACT_PERSON as contact ,co.PHONE as phone ,"
						+" con.COMPANY_NAME as pay_company ,con.ADDRESS as pay_address,con.CONTACT_PERSON as pay_contad ,con.PHONE as pay_phone ,"
						+" to.ORDER_NO  as transfer_order_no,to.CARGO_NATURE  as nature,to.PICKUP_MODE  as pickup,to.ARRIVAL_MODE  as arrival,to.REMARK as remark ,"
						+" u.user_name as counterman,lo.name as location_from,loc.name as location_to "
						+" FROM RETURN_ORDER  ro "
						+" left join CONTACT  co on co.id in (select p.CONTACT_ID from PARTY p where p.id=ro.CUSTOMER_ID ) "
						+" left join CONTACT  con on con.id in (select p.CONTACT_ID from PARTY p where p.id=ro.NOTITY_PARTY_ID )"
						+" left join TRANSFER_ORDER  to on to.id=ro.TRANSFER_ORDER_ID "
						+" left join USER_LOGIN  u on u.id =to.CREATE_BY "
						+ " left join location lo on lo.code=to.route_from"
						+ " left join location loc on loc.code=to.route_to"
						+ " where ro.id="+id+"";
			String sql_del="SELECT ro.*,co.COMPANY_NAME as company_name ,co.ADDRESS as address,co.CONTACT_PERSON as contact ,co.PHONE as phone ,"
					+" con.COMPANY_NAME as pay_company ,con.ADDRESS as pay_address,con.CONTACT_PERSON as pay_contad ,con.PHONE as pay_phone ,"
					+" to.ORDER_NO as transfer_order_no, de.ORDER_NO  as DELIVERY_ORDER_ID_NO ,to.CARGO_NATURE  as nature,to.PICKUP_MODE  as pickup,to.ARRIVAL_MODE  as arrival,to.REMARK as remark ,"
					+" u.user_name as counterman,lo.name as location_from,loc.name as location_to" 
					+" FROM RETURN_ORDER  ro "
					 +" left join CONTACT  co on co.id in (select p.CONTACT_ID from PARTY p where p.id=ro.CUSTOMER_ID )" 
					 +" left join CONTACT  con on con.id in (select p.CONTACT_ID from PARTY p where p.id=ro.NOTITY_PARTY_ID )"
					 +" left join TRANSFER_ORDER  to on to.id in (SELECT do.TRANSFER_ORDER_ID  FROM DELIVERY_ORDER do where do.id=ro.DELIVERY_ORDER_ID )"
					 +" left join USER_LOGIN  u on u.id =to.CREATE_BY" 
					 +" left join location lo on lo.code=to.route_from"
					 +" left join location loc on loc.code=to.route_to"
					 +" left join DELIVERY_ORDER  de on de.id=ro.DELIVERY_ORDER_ID"
					 +" where ro.id="+id+"";
			ReturnOrder re=ReturnOrder.dao.findById(id);
			if("null".equals(re.get("DELIVERY_ORDER_ID"))){
				message = Db.find(sql_tr);
			}else{
				message = Db.find(sql_del);
			}
		
		for (int i = 0; i < message.size(); i++) {
			String nature = message.get(i).get("nature");
			String pickup = message.get(i).get("pickup");
			String arrival = message.get(i).get("arrival");
			String transaction_status = message.get(i)
			        .get("transaction_status");
			if ("cargo".equals(nature)) {
				message.get(i).set("nature", "普通货品");
			}
			if ("routeSP".equals(pickup)) {
				message.get(i).set("pickup", "干线供应商自提");
			}
			if ("pickupSP".equals(pickup)) {
				message.get(i).set("pickup", "外包供应商自提");
			}
			if ("own".equals(pickup)) {
				message.get(i).set("pickup", "公司自提");
			}
			if ("delivery".equals(arrival)) {
				message.get(i).set("arrival", "货品直送");
			}
			if ("gateIn".equals(arrival)) {
				message.get(i).set("arrival", "入中转仓");
			}
			if ("new".equals(transaction_status)) {
				message.get(i).set("transaction_status", "新建");
			}
			if ("confirmed".equals(transaction_status)) {
				message.get(i).set("transaction_status", "确认");
			}
			if ("cancel".equals(transaction_status)) {
				message.get(i).set("transaction_status", "取消");
			}
		}

		renderJson(message);

	}

	// 收费条目
	public void paylist() {
		int id = Integer.parseInt(getPara("locationName"));
		List<Record> paylist = new ArrayList<Record>();
		paylist = Db
		        .find("select f.name,f.remark,tf.amount,tf.status from FIN_ITEM f,TRANSFER_ORDER_FIN_ITEM tf  where tf.fin_item_id =f.id and tf.order_id ='"
		                + id + "'");

		renderJson(paylist);
	}

	// 货品详细
	public void itemlist() {
		int id = Integer.parseInt(getPara("locationName"));
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from TRANSFER_ORDER_ITEM ";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> itemlist = new ArrayList<Record>();
		TransferOrder tr = TransferOrder.dao.findFirst("SELECT to.CARGO_NATURE   FROM TRANSFER_ORDER to where to.id in (select ro.TRANSFER_ORDER_ID  from RETURN_ORDER  ro where ro.id="+id+")");
		String nature = tr.getStr("cargo_nature");
		String sql_atm="SELECT toi.*,toid.SERIAL_NO  FROM TRANSFER_ORDER_ITEM toi"
			+" left join TRANSFER_ORDER_ITEM_DETAIL  toid on toid.ITEM_ID =toi.id and toid.ORDER_ID =toi.ORDER_ID" 
			+" where toi.ORDER_ID in (select ro.TRANSFER_ORDER_ID from RETURN_ORDER  ro where ro.id="+id+")";
		String sql_item="SELECT toi.* FROM TRANSFER_ORDER_ITEM toi"
			+" where toi.ORDER_ID in (select ro.TRANSFER_ORDER_ID from RETURN_ORDER  ro where ro.id=10)";
		if("ATM".equals(nature)){
			itemlist =Db.find(sql_atm);
		}else{
			itemlist =Db.find(sql_item);
		}

		// 获取货损条数和货品id
	
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", itemlist);
		renderJson(orderMap);
	}

	// 点击查看
	public void check() {
		String id = getPara();
		List<Record> DELIVERYORDERID = new ArrayList<Record>();
		DELIVERYORDERID = Db
		        .find("SELECT to.CARGO_NATURE   FROM TRANSFER_ORDER to where to.id in (select ro.TRANSFER_ORDER_ID  from RETURN_ORDER  ro where ro.id="+id+")");
		TransferOrder tr = TransferOrder.dao.findById(id);
		String nature = tr.getStr("cargo_nature");
		if (DELIVERYORDERID.get(0).get("DELIVERY_ORDER_ID") != null) {
			setAttr("check", true);
		} else {
			setAttr("check", false);
		}

		setAttr("nature", nature);
		setAttr("id", id);
		if (LoginUserController.isAuthenticated(this))
			render("profile/returnorder/returnOrder.html");
	}

	public void save() {
		int id = Integer.parseInt(getPara("id"));
		List<Record> DELIVERYORDERID = new ArrayList<Record>();
		DELIVERYORDERID = Db
		        .find("SELECT DELIVERY_ORDER_ID  FROM RETURN_ORDER ro where ro.id='"
		                + id + "'");
		if (DELIVERYORDERID.get(0).get("DELIVERY_ORDER_ID") != null) {
			setAttr("check", true);
		} else {
			setAttr("check", false);
		}
		TransferOrder tr = TransferOrder.dao.findById(id);
		String nature = tr.getStr("cargo_nature");
		ReturnOrder r = ReturnOrder.dao.findById(id);
		r.set("transaction_status", "完成").update();
		setAttr("nature", nature);
		setAttr("id", id);
		setAttr("saveOK", true);
		if (LoginUserController.isAuthenticated(this))
			render("profile/returnorder/returnOrder.html");

	}

	// 取消
	public void cancel() {
		String id = getPara();

		ReturnOrder re = ReturnOrder.dao.findById(id);
		re.set("TRANSACTION_STATUS", "cancel").update();
		renderJson("{\"success\":true}");

	}

}
