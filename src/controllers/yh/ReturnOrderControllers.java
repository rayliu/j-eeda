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
						+ "(select cargo_nature  from transfer_order t where r.transfer_order_id=t.id)  nature ,"
						+ "(select  pickup_mode from transfer_order t where r.transfer_order_id=t.id) pickup  ,"
						+ "(select arrival_mode from transfer_order t where r.transfer_order_id=t.id) arrival  ,"
						+ "(select item_name from transfer_order_item t where t.order_id= r.transfer_order_id ) item_name ,"
						+ "(select item_desc from transfer_order_item t where t.order_id= r.transfer_order_id ) item_desc ,"
						+ "(select amount from transfer_order_item t where t.order_id= r.transfer_order_id ) amoumt ,"
						+ "(select unit from transfer_order_item t where t.order_id= r.transfer_order_id ) unit ,"
						+ "(select volume from transfer_order_item t where t.order_id= r.transfer_order_id ) volume,"
						+ "(select weight from transfer_order_item t where t.order_id= r.transfer_order_id ) weight ,"
						+ "(select remark from transfer_order_item t where t.order_id= r.transfer_order_id ) remark ,"
						+ "(select location_from from route rt where rt.id=r.route_id ) location_from ,"
						+ "(select location_to from route rt  where rt.id=r.route_id ) location_to ,"
						+ "(select amount from contract_item c where c.contract_id=r.notity_party_id and c.route_id=r.route_id) amount "
						+ "from return_order r where  r.transfer_order_id='"
						+ id + "'");

		renderJson(message);

	}

	public void check() {
		String user = currentUser.getPrincipal().toString();
		String id = getPara();
		setAttr("user", user);
		setAttr("id", id);
		render("profile/returnorder/returnOrder.html");
	}

	public void delete() {
		String id = getPara();
		ReturnOrder re = new ReturnOrder();

		re.dao.deleteById(id);
		index();
	}

}
