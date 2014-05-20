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
			String sql = "select count(1) total from return_order ";
			Record rec = Db.findFirst(sql + totalWhere);
			logger.debug("total records:" + rec.getLong("total"));

			// 获取当前页的数据
			List<Record> orders = Db
			        .find("SELECT ro.*, to.order_no as transfer_order_no, do.order_no as delivery_order_no, c.company_name FROM RETURN_ORDER ro "
			                + "left join transfer_order to on ro.transfer_order_id = to.id "
			                + "left join delivery_order do on ro.delivery_order_id = do.id "
			                + "left join party p on ro.customer_id = p.id "
			                + "left join contact c on p.contact_id = c.id ");

			orderMap.put("sEcho", pageIndex);
			orderMap.put("iTotalRecords", rec.getLong("total"));
			orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
			orderMap.put("aaData", orders);

		} else {
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
			                + "%' and  ro.TRANSFER_ORDER_ID like '%"
			                + tr_order_no
			                + "%' and ro.DELIVERY_ORDER_ID like '%"
			                + de_order_no
			                + "%' and ro.CREATOR like '%"
			                + stator
			                + "%' and ro.CREATE_DATE like '%"
			                + time_one
			                + "%' and ro.TRANSACTION_STATUS like'%"
			                + status + "%'  ");

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

		message = Db
		        .find("select r.id,r.order_no, r.create_date,r.transaction_status,r.creator,"
		                + "(select company_name from contact c where c.id in  (SELECT p.conTACT_ID  FROM PARTY  p where p.id in (select t.notify_party_id  from  transfer_order t  where t. id=r.transfer_order_id"
		                + " )) )company_name,"
		                + "(select address from contact c where c.id in (SELECT p.conTACT_ID  FROM PARTY  p where p.id in (select t.notify_party_id  from  transfer_order t  where t. id=r.transfer_order_id"
		                + ")))address,"
		                + "(select phone from contact c where c.id in (SELECT p.conTACT_ID  FROM PARTY  p where p.id in (select t.notify_party_id  from  transfer_order t  where t. id=r.transfer_order_id"
		                + " )) )phone,"
		                + "(select contact_person from contact c where c.id in (SELECT p.conTACT_ID  FROM PARTY  p where p.id in (select t.notify_party_id  from  transfer_order t  where t. id=r.transfer_order_id"
		                + " )))contact,"
		                + "(select company_name from  contact c where c.id in (SELECT p.conTACT_ID  FROM PARTY  p where p.id in (select t.notify_party_id  from  transfer_order t  where t. id=r.transfer_order_id"
		                + "))) pay_company,"
		                + "(select address from contact c where c.id in (SELECT p.conTACT_ID  FROM PARTY  p where p.id in (select t.notify_party_id  from  transfer_order t  where t. id=r.transfer_order_id"
		                + " )))pay_address,"
		                + "(select phone from contact c where c.id in (SELECT p.conTACT_ID  FROM PARTY  p where p.id in (select t.notify_party_id  from  transfer_order t  where t. id=r.transfer_order_id"
		                + " )))pay_phone,"
		                + "(select  contact_person from  contact c where c.id in (SELECT p.conTACT_ID  FROM PARTY  p where p.id in (select t.notify_party_id  from  transfer_order t  where t. id=r.transfer_order_id"
		                + ")))pay_contad,"
		                + "(select order_no  from transfer_order t where r.transfer_order_id=t.id)  transfer_order_no ,"
		                + "(select cargo_nature  from transfer_order t where r.transfer_order_id=t.id)  nature ,"
		                + "(select  pickup_mode from transfer_order t where r.transfer_order_id=t.id) pickup  ,"
		                + "(select arrival_mode from transfer_order t where r.transfer_order_id=t.id) arrival  ,"
		                + "(select remark from transfer_order t where r.transfer_order_id=t.id) remark  ,"
		                + "(SELECT ORDER_NO  FROM DELIVERY_ORDER fo  where fo.id=r.DELIVERY_ORDER_ID ) DELIVERY_ORDER_ID_NO  ,"
		                + "(SELECT location_from  FROM ROUTE  ro  where ro.id in (select route_id from transfer_order t where t.id=r.transfer_order_id)) location_from ,"
		                + "(SELECT location_to  FROM ROUTE  ro  where ro.id in (select route_id from transfer_order t where t.id=r.transfer_order_id)) location_to ,"
		                + "(select amount from contract_item c where c.contract_id=r.notity_party_id and c.route_id=r.route_id) amount ,"
		                + "(SELECT user_name  FROM user_login u  where  u.id in (select create_by from transfer_order t where t.id=r.transfer_order_id"
		                + ")) counterman "
		                + "from return_order r where  r.id='" + id + "'");
		for (int i = 0; i < message.size(); i++) {
			String nature = message.get(i).get("nature");
			String pickup = message.get(i).get("pickup");
			String arrival = message.get(i).get("arrival");
			String transaction_status = message.get(i)
			        .get("transaction_status");
			if (nature.equals("cargo ")) {
				message.get(i).set("nature", "普通货品");
			}
			if (pickup.equals("routeSP")) {
				message.get(i).set("pickup", "干线供应商自提");
			}
			if (pickup.equals("pickupSP")) {
				message.get(i).set("pickup", "外包供应商自提");
			}
			if (pickup.equals("own")) {
				message.get(i).set("pickup", "公司自提");
			}
			if (arrival.equals("delivery")) {
				message.get(i).set("arrival", "货品直送");
			}
			if (arrival.equals("gateIn")) {
				message.get(i).set("arrival", "入中转仓");
			}
			if (transaction_status.equals("new")) {
				message.get(i).set("transaction_status", "新建");
			}
			if (transaction_status.equals("confirmed")) {
				message.get(i).set("transaction_status", "确认");
			}
			if (transaction_status.equals("cancel")) {
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
		List<Record> totallist = new ArrayList<Record>();
		TransferOrder tr = TransferOrder.dao.findById(id);
		String nature = tr.getStr("cargo_nature");

		// 获取货损条数和货品id
		totallist = Db
		        .find("select count(1) total  , ITEM_ID  from TRANSFER_ORDER_ITEM_DETAIL where order_id ='"
		                + id + "' and is_damage =true GROUP by item_id  ");
		// 货品信息
		if (nature.equals("ATM")) {
			if (totallist.size() > 0) {
				itemlist = Db
				        .find("SELECT td.id,td.item_id,td.SERIAL_NO ,td.IS_DAMAGE ,td.ESTIMATE_DAMAGE_AMOUNT,ti.ITEM_NAME,ti.VOLUME,ti.amount,ti.WEIGHT ,ti.REMARK,"
				                + "(select address from contact c where c.id  =td.NOTIFY_PARTY_ID )address,"
				                + "(select contact_person from contact c where c.id  =td.NOTIFY_PARTY_ID )contact,"
				                + "(select phone from contact c where c.id =td.NOTIFY_PARTY_ID ) phone "
				                + "FROM TRANSFER_ORDER_ITEM_DETAIL  td ,TRANSFER_ORDER_ITEM  ti where td.ITEM_ID =ti.id and ti.ORDER_ID ='"
				                + id + "'");
			} else {
				itemlist = Db
				        .find("SELECT ITEM_NAME ,AMOUNT ,VOLUME ,WEIGHT ,REMARK  FROM TRANSFER_ORDER_ITEM where ORDER_ID ='"
				                + id + "'");
				itemlist.get(0).set("SERIAL_NO", "");
				itemlist.get(0).set("ESTIMATE_DAMAGE_AMOUNT", "");
				itemlist.get(0).set("IS_DAMAGE", "");
				itemlist.get(0).set("address", "");
				itemlist.get(0).set("contact", "");
				itemlist.get(0).set("phone", "");
			}
		} else {
			itemlist = Db
			        .find("SELECT * FROM TRANSFER_ORDER_ITEM where ORDER_ID ='"
			                + id + "'");
		}

		for (int i = 0; i < itemlist.size(); i++) {
			try {
				if (itemlist.get(i).get("id") == totallist.get(i)
				        .get("item_id")
				        || itemlist.get(i).get("item_id") == totallist.get(i)
				                .get("item_id")) {
					String amount = String.valueOf(itemlist.get(i)
					        .get("amount"));
					String total = totallist.get(i).get("total").toString();
					double allamount = Double.parseDouble(amount);
					double lasttotal = Double.parseDouble(total);
					double lastamount = allamount - lasttotal;
					itemlist.get(i).set("lasttotal", lasttotal);
					itemlist.get(i).set("lastamount", lastamount);
				}
			} catch (Exception e) {
				itemlist.get(i).set("lasttotal", 0);
				itemlist.get(i)
				        .set("lastamount", itemlist.get(i).get("amount"));

			}

		}
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
		        .find("SELECT DELIVERY_ORDER_ID  FROM RETURN_ORDER ro where ro.id='"
		                + id + "'");
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
