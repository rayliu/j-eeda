package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeItem;
import models.ArapChargeOrder;
import models.ArapMiscChargeOrder;
import models.Party;
import models.ReturnOrder;
import models.UserLogin;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.OrderNoUtil;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeCheckOrderController extends Controller {
	private Logger logger = Logger.getLogger(ChargeCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCO_LIST})
	public void index() {
			render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderList.html");
	}

	public void add() {
		setAttr("type", "CUSTOMER");
		setAttr("classify", "");
			render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderCreateSearchList.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCO_CREATE})
	public void create() {
		String ids = getPara("ids");

		setAttr("returnOrderIds", ids);
		String beginTime = getPara("beginTime");
		if (beginTime != null && !"".equals(beginTime)) {
			setAttr("beginTime", beginTime);
		}
		String endTime = getPara("endTime");
		if (endTime != null && !"".equals(endTime)) {
			setAttr("endTime", endTime);
		}
		if(ids != null && !"".equals(ids)){
			String[] idArray = ids.split(",");
			logger.debug(String.valueOf(idArray.length));
			Double totalAmount = 0.0;
			for(int i=0;i<idArray.length;i++){
				ReturnOrder rOrder = ReturnOrder.dao.findById(idArray[i]);
				totalAmount = totalAmount + rOrder.getDouble("total_amount");
			}
			setAttr("totalAmount", totalAmount);
			
			ReturnOrder returnOrder = ReturnOrder.dao.findById(idArray[0]);
			Long customerId = returnOrder.getLong("customer_id");
			if (!"".equals(customerId) && customerId != null) {
				Party party = Party.dao.findById(customerId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get("contact_id")
						.toString());
				setAttr("customer", contact);
				setAttr("type", "CUSTOMER");
				setAttr("classify", "");
			}
		}

		setAttr("saveOK", false);
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));

		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		setAttr("status", "new");
			render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}

	// 创建应收对帐单时，先选取合适的回单，条件：客户，时间段
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCO_CREATE})
	public void createList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		// 获取数据
		String companyName = getPara("companyName");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String receiptBegin = getPara("receiptBegin");
		String receiptEnd = getPara("receiptEnd");
		String sql = "";
		Record rec;
		List<Record> orders;

		String colsLength = getPara("iColumns");
		String fieldsWhere = "AND (";
		for (int i = 0; i < Integer.parseInt(colsLength); i++) {
			String mDataProp = getPara("mDataProp_" + i);
			String searchValue = getPara("sSearch_" + i);

			logger.debug(mDataProp + "[" + searchValue + "]");

			if (searchValue != null && !"".equals(searchValue)) {
				if (mDataProp.equals("COMPANY_ID")) {
					fieldsWhere += "p.id" + " = " + searchValue + " AND ";
				} else {
					fieldsWhere += mDataProp + " like '%" + searchValue
							+ "%' AND ";
				}
			}
		}

		logger.debug("2nd filter:" + fieldsWhere);

		if (fieldsWhere.length() > 8) {
			fieldsWhere = fieldsWhere.substring(0, fieldsWhere.length() - 4);
			fieldsWhere += ')';
		} else {
			fieldsWhere = "";
		}
		// 获取总条数
		String totalWhere = "";
		sql = "select count(1) total from return_order where transaction_status = '已确认'";
		rec = Db.findFirst(sql + fieldsWhere);
		logger.debug("total records:" + rec.getLong("total"));

		if (companyName == null && beginTime == null && endTime == null) {

			// 获取当前页的数据
			orders = Db.find("select distinct ror.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no, dvr.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,"
					+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,"
					+ " ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) route_from,"
					+ " ifnull((select name from location where code = tor.route_to),(select name from location where code = dvr.route_to)) route_to,"
					+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id) contract_amount"
					+ " ,ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)) pickup_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '送货费') send_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '安装费') installation_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '超里程费') super_mileage_amount"
					+ " ,ifnull((select sum(ifit.amount*ifit.income_rate) from transfer_order tord left join transfer_order_item toit on toit.order_id = tord.id left join insurance_fin_item ifit on ifit.transfer_order_item_id = toit.id where tord.id in(tor.id)) , (select sum(ifit.amount*ifit.income_rate) from transfer_order tord left join transfer_order_item toit on toit.order_id = tord.id left join insurance_fin_item ifit on ifit.transfer_order_item_id = toit.id where tord.id in(tor2.id))) insurance_amount,"
					+ " ifnull((select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor.id order by dtr.turnout_time asc limit 0,1), (select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor2.id order by dtr.turnout_time asc limit 0,1)) departure_time"
					+ " from return_order ror"
					+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
					+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
					+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
					+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
					+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
					+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator where ror.transaction_status = '已确认' group by ror.id,tor2.id"
					+ " order by ror.create_date desc " + fieldsWhere);
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			if (receiptBegin == null || "".equals(receiptBegin)) {
				receiptBegin = "1-1-1";
			}
			if (receiptEnd == null || "".equals(receiptEnd)) {
				receiptEnd = "9999-12-31";
			}
			if (companyName == null || "".equals(companyName)) {

				// 获取当前页的数据
				orders = Db
						.find("select distinct ror.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no, dvr.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,"
								+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,ifnull(tor.route_from,tor2.route_from),ifnull(tor.route_to,dvr.route_to),"
								+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id) contract_amount"
								+ " ,ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)) pickup_amount"
								+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount"
								+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount"
								+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '送货费') send_amount"
								+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '安装费') installation_amount"
								+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '超里程费') super_mileage_amount"
								+ " ,ifnull((select sum(ifit.amount*ifit.income_rate) from transfer_order tord left join transfer_order_item toit on toit.order_id = tord.id left join insurance_fin_item ifit on ifit.transfer_order_item_id = toit.id where tord.id in(tor.id)) , (select sum(ifit.amount*ifit.income_rate) from transfer_order tord left join transfer_order_item toit on toit.order_id = tord.id left join insurance_fin_item ifit on ifit.transfer_order_item_id = toit.id where tord.id in(tor2.id))) insurance_amount,"
								+ " ifnull((select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor.id order by dtr.turnout_time asc limit 0,1), (select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor2.id order by dtr.turnout_time asc limit 0,1)) departure_time"
								+ " from return_order ror"
								+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
								+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
								+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
								+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
								+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
								+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator where ror.transaction_status = '已确认' "
								+ " and (ror.create_date between '"
								+ beginTime
								+ "' and '"
								+ endTime
								+ "')"
								+ " and (ror.receipt_date between '"
								+ receiptBegin
								+ "' and '"
								+ receiptEnd
								+ "') group by ror.id,tor2.id order by ror.create_date desc "
								+ fieldsWhere);
			} else {
				// 获取当前页的数据
				orders = Db
						.find("select distinct ror.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no, dvr.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,"
								+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,ifnull(tor.route_from,tor2.route_from),ifnull(tor.route_to,dvr.route_to),"
								+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id) contract_amount"
								+ " ,ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)) pickup_amount"
								+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount"
								+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount"
								+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '送货费') send_amount"
								+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '安装费') installation_amount"
								+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '超里程费') super_mileage_amount"
								+ " ,ifnull((select sum(ifit.amount*ifit.income_rate) from transfer_order tord left join transfer_order_item toit on toit.order_id = tord.id left join insurance_fin_item ifit on ifit.transfer_order_item_id = toit.id where tord.id in(tor.id)) , (select sum(ifit.amount*ifit.income_rate) from transfer_order tord left join transfer_order_item toit on toit.order_id = tord.id left join insurance_fin_item ifit on ifit.transfer_order_item_id = toit.id where tord.id in(tor2.id))) insurance_amount,"
								+ " ifnull((select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor.id order by dtr.turnout_time asc limit 0,1), (select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor2.id order by dtr.turnout_time asc limit 0,1)) departure_time"
								+ " from return_order ror"
								+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
								+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
								+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
								+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
								+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
								+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator where ror.transaction_status = '已确认' "
								+ " and c.abbr like '%"
								+ companyName
								+ "%'"
								+ " or c2.abbr like '%"
								+ companyName
								+ "%' and (ror.create_date between '"
								+ beginTime
								+ "' and '"
								+ endTime
								+ "')"
								+ " and (ror.receipt_date between '"
								+ receiptBegin
								+ "' and '"
								+ receiptEnd
								+ "') group by ror.id,tor2.id order by ror.create_date desc "
								+ fieldsWhere);
			}

		}
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);
		renderJson(orderMap);

	}
	
	public void createList2() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		List<Record> BillingOrders = null;

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCO_UPDATE, PermissionConstant.PERMSSION_CCO_CREATE}, logical=Logical.OR)
	public void save() {
		ArapChargeOrder arapAuditOrder = null;
		String chargeCheckOrderId = getPara("chargeCheckOrderId");
		String total_amount = getPara("total_amount");
		String debit_amount = getPara("debit_amount");
		if (!"".equals(chargeCheckOrderId) && chargeCheckOrderId != null) {
			arapAuditOrder = ArapChargeOrder.dao.findById(chargeCheckOrderId);
			arapAuditOrder.set("status", "new");
			arapAuditOrder.set("payee_id", getPara("customer_id"));
			arapAuditOrder.set("create_by", getPara("create_by"));
			arapAuditOrder.set("create_stamp", new Date());
			arapAuditOrder.set("remark", getPara("remark"));
			arapAuditOrder.set("last_modified_by", getPara("create_by"));
			arapAuditOrder.set("last_modified_stamp", new Date());
			if(getParaToDate("begin_time") != null){
				arapAuditOrder.set("begin_time", getPara("begin_time")); 
			}
            if(getParaToDate("end_time") != null){
            	arapAuditOrder.set("end_time", getPara("end_time")); 
            }
            arapAuditOrder.set("total_amount", total_amount);
            if(total_amount != null && !"".equals(total_amount) && debit_amount != null && !"".equals(debit_amount)){
            	arapAuditOrder.set("charge_amount", Double.parseDouble(total_amount) - Double.parseDouble(debit_amount));
            }
			arapAuditOrder.update();

			List<ArapChargeItem> arapAuditItems = ArapChargeItem.dao.find(
					"select * from arap_charge_item where charge_order_id = ?",
					arapAuditOrder.get("id"));
			for (ArapChargeItem arapAuditItem : arapAuditItems) {
				// arapAuditItem.set("ref_order_type", );
				// arapAuditItem.set("item_status", "");
				arapAuditItem.set("create_by", getPara("create_by"));
				arapAuditItem.set("create_stamp", new Date());
				arapAuditItem.update();
			}
		} else {
			arapAuditOrder = new ArapChargeOrder();
			arapAuditOrder.set("order_no", getPara("order_no"));
			// arapAuditOrder.set("order_type", );
			arapAuditOrder.set("status", "新建");
			arapAuditOrder.set("payee_id", getPara("customer_id"));
			arapAuditOrder.set("create_by", getPara("create_by"));
			arapAuditOrder.set("create_stamp", new Date());
			arapAuditOrder.set("remark", getPara("remark"));
			String sql = "select * from arap_charge_order order by id desc limit 0,1";
			arapAuditOrder.set("order_no", OrderNoUtil.getOrderNo(sql, "YSDZ"));
			if(getParaToDate("begin_time") != null){
				arapAuditOrder.set("begin_time", getPara("begin_time")); 
			}
            if(getParaToDate("end_time") != null){
            	arapAuditOrder.set("end_time", getPara("end_time")); 
            }
            arapAuditOrder.set("total_amount", total_amount);
            if(total_amount != null && !"".equals(total_amount) && debit_amount != null && !"".equals(debit_amount)){
            	arapAuditOrder.set("charge_amount", Double.parseDouble(total_amount) - Double.parseDouble(debit_amount));
            }
			arapAuditOrder.save();

			String returnOrderIds = getPara("returnOrderIds");
			String[] returnOrderIdsArr = returnOrderIds.split(",");
			for (int i = 0; i < returnOrderIdsArr.length; i++) {
				ArapChargeItem arapAuditItem = new ArapChargeItem();
				// arapAuditItem.set("ref_order_type", );
				arapAuditItem.set("ref_order_id", returnOrderIdsArr[i]);
				arapAuditItem.set("charge_order_id", arapAuditOrder.get("id"));
				// arapAuditItem.set("item_status", "");
				arapAuditItem.set("create_by", getPara("create_by"));
				arapAuditItem.set("create_stamp", new Date());
				arapAuditItem.save();
				
				ReturnOrder returnOrder = ReturnOrder.dao.findById(returnOrderIdsArr[i]);
				returnOrder.set("transaction_status", "对账中");
				returnOrder.update();
			}
		}
		renderJson(arapAuditOrder);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCO_UPDATE})
	public void edit() throws ParseException {
		String id = getPara("id");
		ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(id);
		String customerId = arapAuditOrder.get("payee_id");
		if (!"".equals(customerId) && customerId != null) {
			Party party = Party.dao.findById(customerId);
			setAttr("party", party);
			Contact contact = Contact.dao.findById(party.get("contact_id")
					.toString());
			setAttr("customer", contact);
		}
		UserLogin userLogin = UserLogin.dao.findById(arapAuditOrder
				.get("create_by"));
		setAttr("userLogin", userLogin);
		setAttr("arapAuditOrder", arapAuditOrder);

		Date beginTimeDate = arapAuditOrder.get("begin_time");
		Date endTimeDate = arapAuditOrder.get("end_time");
		String beginTime = "";
		String endTime = "";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		if (!"".equals(beginTimeDate) && beginTimeDate != null) {
			beginTime = simpleDateFormat.format(beginTimeDate);
		}
		if (!"".equals(endTimeDate) && endTimeDate != null) {
			endTime = simpleDateFormat.format(endTimeDate);
		}
		String returnOrderIds = "";
		List<ArapChargeItem> arapAuditItems = ArapChargeItem.dao.find("select * from arap_charge_item where charge_order_id = ?", id);
		for(ArapChargeItem arapAuditItem : arapAuditItems){
			returnOrderIds += arapAuditItem.get("ref_order_id") + ",";
		}
		returnOrderIds = returnOrderIds.substring(0, returnOrderIds.length() - 1);
		setAttr("returnOrderIds", returnOrderIds);
		setAttr("beginTime", beginTime);
		setAttr("endTime", endTime);
			render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}

	public void returnOrderList() {
		String returnOrderIds = getPara("returnOrderIds");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map orderMap = new HashMap();
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from return_order ror where ror.id in ("+returnOrderIds+")";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据 + sLimit;
		List<Record> orders = Db
				.find("select distinct ror.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no, dvr.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,"
				+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,"
				+ " ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) route_from,"
				+ " ifnull((select name from location where code = tor.route_to),(select name from location where code = dvr.route_to)) route_to,"
				+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id) contract_amount"
				+ " ,ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)) pickup_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount"
				+ " ,ifnull((select sum(ifi.amount)*ifi.income_rate from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.transfer_order_item_id in (select id from transfer_order_item toi where tor.id = toi.order_id) and fi.type = '应收' and fi.name = '保险费' group by ifi.income_rate),"
				+ " sum((select sum(ifi.amount*ifi.income_rate) from insurance_order ior left join insurance_fin_item ifi on ior.id = ifi.insurance_order_id left join fin_item fi2 on fi2.id = ifi.fin_item_id where ior.id in(tor2.insurance_id) and fi2.type='应收' and fi2.name='保险费'))) insurance_amount,"
				+ " ifnull((select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor.id order by dtr.turnout_time asc limit 0,1), (select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor2.id order by dtr.turnout_time asc limit 0,1)) departure_time"
				+ " from return_order ror"
				+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
				+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
				+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
				+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
				+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
				+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator "
				+ " where ror.id in ("+returnOrderIds+") group by ror.id,tor2.id order by ror.create_date desc " + sLimit);

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}
	
	// 审核
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCO_AFFIRM})
	public void auditChargeCheckOrder(){
		String chargeCheckOrderId = getPara("chargeCheckOrderId");
		if(chargeCheckOrderId != null && !"".equals(chargeCheckOrderId)){
			ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(chargeCheckOrderId);
			arapAuditOrder.set("status", "已确认");
			arapAuditOrder.update();
			
			updateReturnOrderStatus(arapAuditOrder, "对账已确认");
		}
        renderJson("{\"success\":true}");
	}
	
	// 更新回单状态
	private void updateReturnOrderStatus(ArapChargeOrder arapAuditOrder, String status){
		List<ArapChargeItem> arapAuditItems = ArapChargeItem.dao.find("select * from arap_charge_item where charge_order_id = ?", arapAuditOrder.get("id"));
		for(ArapChargeItem arapAuditItem : arapAuditItems){
			ReturnOrder returnOrder = ReturnOrder.dao.findById(arapAuditItem.get("ref_order_id"));
			returnOrder.set("transaction_status", status);
			returnOrder.update();
		}
	}
	
	// billing order 列表
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CCO_LIST})
	public void list() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from arap_charge_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select distinct aao.*, usl.user_name as creator_name,c.abbr cname"
				+ " from arap_charge_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login usl on usl.id=aao.create_by"
				+ " order by aao.create_stamp desc " + sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}
	
	public void checkChargeMiscList(){
		String chargeCheckOrderId = getPara("chargeCheckOrderId");
		if(chargeCheckOrderId == null || "".equals(chargeCheckOrderId)){
			chargeCheckOrderId = "-1";
		}
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map orderMap = new HashMap();
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(amco.id) total from arap_misc_charge_order amco"
					+ " left join arap_charge_order aco on amco.charge_order_id = aco.id"
					+ " left join arap_misc_charge_order_item amcoi on amcoi.misc_order_id = amco.id where aco.id = "+chargeCheckOrderId;
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select amcoi.*,amco.order_no misc_order_no,c.abbr cname,fi.name name from arap_misc_charge_order amco"
					+ " left join arap_charge_order aco on aco.id = amco.charge_order_id "
					+ " left join arap_misc_charge_order_item amcoi on amcoi.misc_order_id = amco.id "
					+ " left join party p on p.id = aco.payee_id left join contact c on c.id = p.contact_id "
					+ " left join fin_item fi on amcoi.fin_item_id = fi.id where aco.id =  "+ chargeCheckOrderId +" " + sLimit);

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}
	
	public void externalMiscOrderList(){
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_misc_charge_order where ifnull(charge_order_id, 0) = 0";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select amco.*,aco.order_no charge_order_no from arap_misc_charge_order amco"
					+ " left join arap_charge_order aco on aco.id = amco.charge_order_id"
					+ " where ifnull(charge_order_id, 0) = 0 order by amco.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
	}
	
	public void updateChargeMiscOrder(){
		String micsOrderIds = getPara("micsOrderIds");
		String chargeCheckOrderId = getPara("chargeCheckOrderId");
		ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(chargeCheckOrderId);
		if(micsOrderIds != null && !"".equals(micsOrderIds)){
			String[] micsOrderIdArr = micsOrderIds.split(",");
			for(int i=0;i<micsOrderIdArr.length;i++){
				ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(micsOrderIdArr[i]);
				arapMiscChargeOrder.set("charge_order_id", chargeCheckOrderId);
				arapMiscChargeOrder.update();
			}
			
			Record record = Db.findFirst("select sum(amount) sum_amount from arap_misc_charge_order mco"
								+ " left join arap_misc_charge_order_item mcoi on mcoi.misc_order_id = mco.id where mco.id in("+micsOrderIds+")");
			Double total_amount = arapChargeOrder.getDouble("total_amount");
			Double debit_amount = record.getDouble("sum_amount");
			arapChargeOrder.set("debit_amount", debit_amount);
			arapChargeOrder.set("charge_amount", total_amount - debit_amount);
			arapChargeOrder.update();
		}
        renderJson(arapChargeOrder);
	}
}
