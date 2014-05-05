package controllers.yh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ReturnOrder;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ReturnOrderControllers extends Controller {
	private Logger logger = Logger.getLogger(ReturnOrderControllers.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render("profile/returnorder/returnOrderList.html");
	}

	public void list() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from return_order ";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db.find("select * from return_order");
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);
		renderJson(orderMap);
	}

	// 查看回单显示
	public void checkorder() {
		// status_code,create_date,transaction_status,order_type,creator,remark,transfer_order_id,distribution_order_id,contract_id

		int id = Integer.parseInt(getPara("locationName"));
		// setAttr("id",id);

		List<Record> message = new ArrayList<Record>();
		message = Db
				.find("select r.id,r.status_code,r.create_date,r.transaction_status,r.creator,"
						+ "(select company_name from contact c where c.id in (select t.notify_party_id  from  transfer_order t  where t. id in (select transfer_order_id from return_order  where t.id='"
						+ id
						+ "' )) )company_name,"
						+ "(select address from contact c where c.id in (select t.notify_party_id  from  transfer_order t  where t. id in (select transfer_order_id from return_order  where t.id='"
						+ id
						+ "' )))address,"
						+ "(select phone from contact c where c.id in (select t.notify_party_id  from  transfer_order t  where t. id in (select transfer_order_id from return_order  where t.id='"
						+ id
						+ "' )) )phone,"
						+ "(select contact_person from contact c where c.id in (select t.notify_party_id  from  transfer_order t  where t. id in (select transfer_order_id from return_order  where t.id='"
						+ id
						+ "' )))contact,"
						+ "(select company_name from  contact c where c.id in (select t.customer_id  from  transfer_order t  where t. id in (select transfer_order_id from return_order  where t.id='"
						+ id
						+ "'))) pay_company,"
						+ "(select address from contact c where c.id in (select t.customer_id  from  transfer_order t  where t. id in (select transfer_order_id from return_order  where t.id='"
						+ id
						+ "' )))pay_address,"
						+ "(select phone from contact c where c.id in (select t.customer_id  from  transfer_order t  where t. id in (select transfer_order_id from return_order where t.id='"
						+ id
						+ "' )))pay_phone,"
						+ "(select  contact_person from  contact c where c.id in (select t.customer_id  from  transfer_order t  where t. id in (select transfer_order_id from return_order  where t.id='"
						+ id
						+ "')))pay_contad,"
						+ "(select order_no  from transfer_order t where r.transfer_order_id=t.id)  order_no ,"
						+ "(select cargo_nature  from transfer_order t where r.transfer_order_id=t.id)  nature ,"
						+ "(select  pickup_mode from transfer_order t where r.transfer_order_id=t.id) pickup  ,"
						+ "(select arrival_mode from transfer_order t where r.transfer_order_id=t.id) arrival  ,"
						+ "(select remark from transfer_order t where r.transfer_order_id=t.id) remark  ,"
						+ "(SELECT location_from  FROM ROUTE  ro  where ro.id in (select route_id from transfer_order t where t.id='"
						+ id
						+ "') ) location_from ,"
						+ "(SELECT location_to  FROM ROUTE  ro  where ro.id in (select route_id from transfer_order t where t.id='"
						+ id
						+ "') ) location_to ,"
						+ "(select amount from contract_item c where c.contract_id=r.notity_party_id and c.route_id=r.route_id) amount ,"
						+ "(SELECT user_name  FROM user_login u  where  u.id in (select create_by from transfer_order t where t.id='"
						+ id
						+ "')) counterman "
						+ "from return_order r where  r.transfer_order_id='"
						+ id + "'");

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
		List<Record> itemlist = new ArrayList<Record>();
		itemlist = Db
				.find("SELECT * FROM TRANSFER_ORDER_ITEM where ORDER_ID ='"
						+ id + "'");

		renderJson(itemlist);
	}

	// 点击查看
	public void check() {
		String user = currentUser.getPrincipal().toString();
		String id = getPara();
		setAttr("user", user);
		setAttr("id", id);
		render("profile/returnorder/returnOrder.html");
	}

	public void save() {
		int id = Integer.parseInt(getPara("locationName"));

		ReturnOrder r = ReturnOrder.dao.findById(id);
		r.set("transaction_status", "完成").update();

	}

	// 删除
	public void delete() {
		String id = getPara();
		ReturnOrder re = new ReturnOrder();
		re.dao.deleteById(id);
		index();
	}

}
