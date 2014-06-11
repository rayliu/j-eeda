package controllers.yh.departOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class DepartOrderController extends Controller {

	private Logger logger = Logger.getLogger(DepartOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();


	public void index() {
		if (LoginUserController.isAuthenticated(this))
			render("departOrder/departOrderList.html");
	} 

	public void list() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total FROM DEPART_ORDER do "
				+ " left join party p on do.notify_party_id = p.id "
				+ " left join contact c on p.contact_id = c.id "
				+ " left join depart_transfer dt on do.id = dt.depart_id where combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"'";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "SELECT do.*,c.contact_person,c.phone, (select group_concat(dt.TRANSFER_ORDER_NO separator '\r\n')  FROM DEPART_TRANSFER dt where DEPART_ID = do.id)  as TRANSFER_ORDER_NO  FROM DEPART_ORDER do "
				+ " left join party p on do.notify_party_id = p.id "
				+ " left join contact c on p.contact_id = c.id where combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"'";

		List<Record> warehouses = Db.find(sql);

		Map map = new HashMap();
		map.put("sEcho", pageIndex);
		map.put("iTotalRecords", rec.getLong("total"));
		map.put("iTotalDisplayRecords", rec.getLong("total"));

		map.put("aaData", warehouses);

		renderJson(map);
	}

	public void add() {
		setAttr("saveOK", false);
		if (LoginUserController.isAuthenticated(this))
			render("/yh/departOrder/allTransferOrderList.html");
	}

	public void createTransferOrderList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from transfer_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select * from transfer_order order by create_stamp desc";

		List<Record> transferOrders = Db.find(sql);

		Map transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);

		renderJson(transferOrderListMap);
	}
		//allTranferOrderList 创建发车单
	public void addDepartOrder() {
		String list = this.getPara("localArr");
		int lang=list.length();
		if(lang>1){
			setAttr("type","many");
		}else{
			setAttr("type","one");
		}
		
		setAttr("localArr",list);
		render("departOrder/editTransferOrder.html");
	}
	//editTransferOrder 初始数据
	public void getIintDepartOrderItems(){
		// 构造前台参数
		String idlist = getPara("localArr");		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from TRANSFER_ORDER_ITEM tof"
				+ " left join TRANSFER_ORDER  or  on tof.ORDER_ID =or.id "
				+ " left join CONTACT c on c.id in (select contact_id from party p where or.customer_id=p.id)"
				+ " where tof.ORDER_ID in("+idlist+")";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "SELECT tof.* ,or.ORDER_NO as order_no,c.COMPANY_NAME as customer  FROM TRANSFER_ORDER_ITEM tof"
				+ " left join TRANSFER_ORDER  or  on tof.ORDER_ID =or.id "
				+ "left join CONTACT c on c.id in (select contact_id from party p where or.customer_id=p.id)"
				+ " where tof.ORDER_ID in("+idlist+")  order by c.id"+sLimit;
		List<Record> departOrderitem = Db.find(sql);
		for(int i=0;i<departOrderitem.size();i++){
			String itemname=departOrderitem.get(i).get("item_name");
			if("ATM".equals(itemname)){
				long itemid=departOrderitem.get(i).get("id");
				String sql2="SELECT SERIAL_NO  FROM TRANSFER_ORDER_ITEM_DETAIL  where ITEM_ID ="+itemid;
				List<Record> itemserial_no = Db.find(sql2);
				String itemno=itemserial_no.get(0).get("SERIAL_NO");
					departOrderitem.get(i).set("serial_no", itemno);
			}else{
				departOrderitem.get(i).set("serial_no", "无");
			}
		}

		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));

		Map.put("aaData", departOrderitem);

		renderJson(Map);
	
	}
	
}
