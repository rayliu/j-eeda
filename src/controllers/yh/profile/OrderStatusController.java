package controllers.yh.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.OrderStatus;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class OrderStatusController extends Controller{

    private Logger logger = Logger.getLogger(OrderStatusController.class);
    
	public void index() {
		render("/yh/profile/orderStatus/orderStatusList.html");
	}

	public void list() {
		/*
		 * Paging
		 */
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from order_status";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select * from order_status";

		List<Record> list = Db.find(sql);

		Map productListMap = new HashMap();
		productListMap.put("sEcho", pageIndex);
		productListMap.put("iTotalRecords", rec.getLong("total"));
		productListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		productListMap.put("aaData", list);

		renderJson(productListMap);
	}

	public void add() {
		setAttr("saveOK", false);
		render("/yh/profile/orderStatus/orderStatusEdit.html");
	}

	public void edit() {
		long id = getParaToLong();

		OrderStatus orderStatus = OrderStatus.dao.findById(id);
		setAttr("orderStatus", orderStatus);
		render("/yh/profile/orderStatus/orderStatusEdit.html");
	}

	public void delete() {
		long id = getParaToLong();

		OrderStatus orderStatus = OrderStatus.dao.findById(id);
		orderStatus.delete();
		redirect("/orderStatus");
	}

	public void save() {
		OrderStatus orderStatus = null;
		String id = getPara("id");
		
		if (id != null && !id.equals("")) {
			orderStatus = OrderStatus.dao.findById(id);
			orderStatus.set("status_code", getPara("status_code")).set("status_name", getPara("status_name"))
	        .set("order_type", getPara("order_type")).set("remark", getPara("remark")).update();
		} else {
			orderStatus = new OrderStatus();
			String status_code = getPara("status_code");
			String status_name = getPara("status_name");
			String order_type = getPara("order_type");
			String remark = getPara("remark");

			orderStatus.set("status_code", status_code).set("status_name", status_name)
			        .set("order_type", order_type).set("remark", remark).save();
		}
		render("/yh/profile/orderStatus/orderStatusList.html");
	}
}
