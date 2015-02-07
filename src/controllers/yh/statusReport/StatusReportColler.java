package controllers.yh.statusReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.order.TransferOrderController;

public class StatusReportColler extends Controller{
	
	private Logger logger = Logger.getLogger(TransferOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	public void index() {		
		render("/yh/statusReport/productStatusReport.html");
	}
	
	public void accountReceivable() {
		String id = getPara();

		String sLimit = "";
		if (id == null || id.equals("")) {
			Map orderMap = new HashMap();
			orderMap.put("sEcho", 0);
			orderMap.put("iTotalRecords", 0);
			orderMap.put("iTotalDisplayRecords", 0);
			orderMap.put("aaData", null);
			renderJson(orderMap);
			return;
		}
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		// 获取总条数
		String totalWhere = "";
		String sql = "select count(0) total from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join transfer_order t on t.id = d.order_id where d.order_id ='"
				+ id + "' and f.type='应收'"; //and f.type='应收' TODO： 有问题
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		/*List<Record> orders = Db
				.find("select d.*,f.name,t.order_no as transferOrderNo from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join transfer_order t on t.id = d.order_id where d.order_id ='"
						+ id + "' and f.type='应收'");*/
		List<Record> orders =Db.find("select d.*, f.name, t.order_no as transferOrderNo from "
				+ "transfer_order_fin_item d "
				+ "left join fin_item f on d.fin_item_id = f.id "
				//+ "LEFT JOIN return_order r ON d.order_id = r.id "
				+ "left join transfer_order t on t.id = d.order_id "
				+ "where d.order_id = " + id + " and f.type = '应收'");
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}
}
