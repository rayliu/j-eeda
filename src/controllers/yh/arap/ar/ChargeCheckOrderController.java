package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeItem;
import models.ArapChargeOrder;
import models.Party;
import models.ReturnOrder;
import models.UserLogin;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.profile.Contact;
import models.yh.returnOrder.ReturnOrderFinItem;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeCheckOrderController extends Controller {
	private Logger logger = Logger.getLogger(ChargeCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCO_LIST })
	public void index() {
		render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderList.html");
	}

	public void add() {
		setAttr("type", "CUSTOMER");
		setAttr("classify", "");
		render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderCreateSearchList.html");
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCO_CREATE })
	public void create() {
		String returnOrderIds = getPara("returnOrderIds");
		String miscOrderIds = getPara("miscOrderIds");

		setAttr("returnOrderIds", returnOrderIds);
		setAttr("miscOrderIds", miscOrderIds);

		String beginTime = getPara("beginTime");
		if (beginTime != null && !"".equals(beginTime)) {
			setAttr("beginTime", beginTime);
		}
		String endTime = getPara("endTime");
		if (endTime != null && !"".equals(endTime)) {
			setAttr("endTime", endTime);
		}

		ArapMiscChargeOrder arapMiscChargeOrder = null;
		ReturnOrder rOrder = null;
		Long customerId =null;
		Long spId = null;
		String[] returnOrderIdArray = new String[]{};
		if(!returnOrderIds.equals("")){
			returnOrderIdArray = returnOrderIds.split(",");
		}
		Double totalAmount = 0.0;
		for (int i = 0; i < returnOrderIdArray.length; i++) {
			rOrder = ReturnOrder.dao.findById(returnOrderIdArray[i]);
			Record record = Db
					.findFirst(
							"select ifnull(round(sum(amount),2),0) as total_amount from return_order_fin_item where return_order_id = ?",
							rOrder.get("id"));
			totalAmount = totalAmount + record.getDouble("total_amount");
			
			customerId = rOrder.getLong("customer_id");
			if (!"".equals(customerId) && customerId != null) {
				Party party = Party.dao.findById(customerId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get(
						"contact_id").toString());
				setAttr("customer", contact);
				setAttr("type", "CUSTOMER");
				setAttr("classify", "");
			}
		}
		
		String[] miscOrderIdArray = new String[]{};
		if(!miscOrderIds.equals("")){
			miscOrderIdArray = miscOrderIds.split(",");
		}

		for (int i = 0; i < miscOrderIdArray.length; i++) {
			arapMiscChargeOrder = ArapMiscChargeOrder.dao
					.findById(miscOrderIdArray[i]);
			Record record = Db
					.findFirst(
							"select round(sum(total_amount),2) as total_amount from arap_misc_charge_order where id = ?",
							arapMiscChargeOrder.get("id"));
			totalAmount = totalAmount + record.getDouble("total_amount");
			
			spId = arapMiscChargeOrder.getLong("sp_id");
			setAttr("spId", spId);
			if(customerId ==null){
				customerId = arapMiscChargeOrder.getLong("customer_id");
				if (!"".equals(customerId) && customerId != null) {
					Party party = Party.dao.findById(customerId);
					setAttr("party", party);
					Contact contact = Contact.dao.findById(party.get(
							"contact_id").toString());
					setAttr("customer", contact);
					setAttr("type", "CUSTOMER");
					setAttr("classify", "");			
				}
			}
		}

		setAttr("totalAmount", Double.valueOf(String.format("%.2f",totalAmount)));
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));

		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		setAttr("status", "new");
		
		List<Record> itemList = getItemLIst(returnOrderIds, miscOrderIds);
		setAttr("itemList", itemList);
		render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}

	private List<Record> getItemLIst(String returnOrderIds, String miscOrderIds) {
		String itemReturnSql = "select distinct ror.*, "
				+ " usl.user_name as creator_name, "
				+ " ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no, "
				+ " dvr.order_no as delivery_order_no, '回单' as tporder,"
				+ " ifnull(c.abbr,c2.abbr) cname,"
				+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,"
				+ " ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) route_from,"
				+ " ifnull((select name from location where code = tor.route_to),(select name from location where code = dvr.route_to)) route_to,"
				+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id  and rofi.contract_id !='') contract_amount"
				+ " ,ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)) pickup_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '送货费') send_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '安装费') installation_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '超里程费') super_mileage_amount"
				+ " ,(select round(sum(rofi.amount),2) from return_order_fin_item rofi left join fin_item fi on rofi.fin_item_id = fi.id where fi.name = '保险费' and rofi.return_order_id = ror.id) insurance_amount,"
				+ " ifnull((select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and ifnull(rofi.remark,'') !='对账调整金额'),0) as charge_total_amount,"
				+ " (select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收') as change_amount, "
				+ " null sp"
				+ " from return_order ror"
				+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
				+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
				+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
				+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
				+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
				+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator "
				+ " where ror.id in("+ returnOrderIds +") group by ror.id,tor2.id";
				
			String itemMiscSql=" (SELECT amco.id id,amco.order_no order_no,NULL status_code,amco.create_stamp create_date,"
				+ " NULL receipt_date,amco. STATUS transaction_status,NULL order_type,amco.create_by creator,"
				+ " amco.remark remark,NULL import_ref_num,NULL _id,NULL delivery_order_id,NULL transfer_order_id,"
				+ " NULL notity_party_id,amco.customer_id customer_id,amco.total_amount total_amount,NULL path,"
				+ " NULL creator_name,NULL transfer_order_no,NULL delivery_order_no,'收入单' as tporder,c.abbr cname,"
				+ " (select GROUP_CONCAT(DISTINCT amcoi.customer_order_no SEPARATOR '\r\n') "
				+ " 	from arap_misc_charge_order_item amcoi "
				+ " 	where amcoi.misc_order_id = amco.id) customer_order_no,"
				+ " NULL route_from,NULL route_to,NULL contract_amount,NULL pickup_amount,NULL step_amount,NULL warehouse_amount,NULL send_amount,"
				+ " NULL installation_amount,NULL super_mileage_amount,NULL insurance_amount,"
				+ " ifnull(amco.total_amount,0) charge_total_amount,"
				+ " amco.total_amount change_amount,c1.abbr sp"
				+ " FROM arap_misc_charge_order amco"
				+ " LEFT JOIN contact c ON c.id = amco.customer_id"
				+ " LEFT JOIN contact c1 ON c1.id = amco.sp_id"
				+ " WHERE amco.id in ("+ miscOrderIds +")"
				+ " )  ";
		String itemSql ="";
		if(returnOrderIds.length()>0 && miscOrderIds.length()>0){
			itemSql = itemReturnSql + " UNION " + itemMiscSql;
		}else if(returnOrderIds.length() > 0 && miscOrderIds.length() == 0){
			itemSql = itemReturnSql;
		}else if(returnOrderIds.length() == 0 && miscOrderIds.length() > 0){
			itemSql = itemMiscSql;
		}else{
			return Collections.emptyList();
		}
		List<Record> itemList = Db.find(itemSql +" order by create_date desc");
		return itemList;
	}

	// 创建应收对帐单时，先选取合适的回单，条件：客户，时间段

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCO_CREATE })
	public void createList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		
		//升降序
    	String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		
		String orderByStr = " order by A.create_date desc ";
        if(colName.length()>0){
        	orderByStr = " order by A."+colName+" "+sortBy;
        }

		// 获取数据
		String customer = getPara("customer");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String planningBeginTime = getPara("planningBeginTime");
		String planningEndTime = getPara("planningEndTime");
		String orderNo = getPara("orderNo");
		String customerNo = getPara("customerNo");
		String address = getPara("address");
		String status = getPara("status");

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
		String sqlTotal = "";
		String sql = "";
		String sql2 = "";
		String sql3 = "";
		String condition = "";
		String condition2 = "";
		Record rec;
		List<Record> orders;
		// TODO 收入状态条件过滤未做
		sql = "select distinct ror.*, "
				+ " usl.user_name as creator_name, "
				+ " ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no, "
				+ " dvr.order_no as delivery_order_no, '回单' as tporder,"
				+ " ifnull(c.abbr,c2.abbr) cname,"
				+ " ifnull(c3.address,tor.receiving_address) address,"
				+ " ifnull(tor2.planning_time,tor.planning_time) planning_time,"
				+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,"
				+ " ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) route_from,"
				+ " ifnull((select name from location where code = tor.route_to),(select name from location where code = dvr.route_to)) route_to,"
				+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id  and rofi.contract_id !='') contract_amount"
				+ " ,ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)) pickup_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '送货费') send_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '安装费') installation_amount"
				+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '超里程费') super_mileage_amount"
				+ " ,(select round(sum(rofi.amount),2) from return_order_fin_item rofi left join fin_item fi on rofi.fin_item_id = fi.id where fi.name = '保险费' and rofi.return_order_id = ror.id) insurance_amount,"
				+ " ifnull((select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收'),0) as charge_total_amount ,"
				/*
				 * +
				 * " ifnull((select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor.id order by dtr.turnout_time asc limit 0,1), (select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor2.id order by dtr.turnout_time asc limit 0,1)) departure_time"
				 */
				+ " null sp"
				+ " from return_order ror"
				+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
				+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
				+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id "
				+ " LEFT JOIN party p3 ON p3.id = dvr.notify_party_id"
				+ " LEFT JOIN contact c3 ON c3.id = p3.contact_id"
				+ " left join delivery_order_item doi on doi.delivery_id = dvr.id "
				+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
				+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
				+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator where ror.transaction_status = '已确认' ";
		sql2 = " group by ror.id,tor2.id"
				+ " UNION"
				+ " (SELECT amco.id id,amco.order_no order_no,NULL status_code,amco.create_stamp create_date,"
				+ " NULL receipt_date,amco. STATUS transaction_status,NULL order_type,amco.create_by creator,"
				+ " amco.remark remark,NULL import_ref_num,NULL _id,NULL delivery_order_id,NULL transfer_order_id,"
				+ " NULL notity_party_id,amco.customer_id customer_id,amco.total_amount total_amount,NULL path,"
				+ " NULL creator_name,NULL transfer_order_no,NULL delivery_order_no,'收入单' as tporder,c.abbr cname,"
				+ " null address,null planning_time,"
				+ " (select GROUP_CONCAT(DISTINCT amcoi.customer_order_no SEPARATOR '\r\n') "
				+ " 	from arap_misc_charge_order_item amcoi "
				+ " 	where amcoi.misc_order_id = amco.id) customer_order_no,"
				+ " NULL route_from,NULL route_to,NULL contract_amount,NULL pickup_amount,NULL step_amount,NULL warehouse_amount,NULL send_amount,"
				+ " NULL installation_amount,NULL super_mileage_amount,NULL insurance_amount,amco.total_amount charge_total_amount , "
				+ " c1.abbr sp "
				+ " FROM arap_misc_charge_order amco"
				+ " LEFT JOIN contact c ON c.id = amco.customer_id"
				+ " LEFT JOIN contact c1 ON c1.id = amco.sp_id"
				+ " WHERE amco. STATUS = '已确认' ";
		sql3 = " ) order by create_date desc ";
		if (customer == null && beginTime == null && endTime == null
				&& orderNo == null && customerNo == null && address == null && planningBeginTime == null && planningEndTime == null) {
			condition = " ";
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			if (planningBeginTime == null || "".equals(planningBeginTime)) {
				planningBeginTime = "1-1-1";
			}
			if (planningEndTime == null || "".equals(planningEndTime)) {
				planningEndTime = "9999-12-31";
			}
			condition = " and (ifnull(c.abbr,'') like '%"
					+ customer
					+ "%' or ifnull(c2.abbr,'') like '%"
					+ customer
					+ "%') "
					+ " and (ror.create_date between '"
					+ beginTime
					+ "' and '"
					+ endTime
					+ "') "
					+ " and (ifnull(tor2.planning_time,tor.planning_time) between'"
					+ planningBeginTime
					+ "' and '"
					+ planningEndTime
					+ "') "
					+ " and (ifnull(tor.customer_order_no,'')  like '%"
					+ customerNo
					+ "%' or ifnull(tor2.customer_order_no,'')  like '%"
					+ customerNo
					+ "%') "
					+ " and ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id))  like '%"
					+ orderNo
					+ "%' "
					+ " and ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from))  like '%"
					+ address + "%' ";
			condition2 = " and c.abbr like '%" + customer
					+ "%' and (SELECT GROUP_CONCAT(DISTINCT amcoi.customer_order_no SEPARATOR '')FROM arap_misc_charge_order_item amcoi WHERE amcoi.misc_order_id = amco.id) like '%" + customerNo
					+ "%'";
		}
		sqlTotal = "select count(1) total from (" + sql + condition + sql2
				+ condition2 + sql3 + ") as A";
		rec = Db.findFirst(sqlTotal + fieldsWhere );
		logger.debug("total records:" + rec.getLong("total"));

		orders = Db.find("select * from ("+sql + condition + sql2 + condition2 + sql3
				+ fieldsWhere + ") A "+ orderByStr + sLimit) ;
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

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCO_UPDATE,
			PermissionConstant.PERMSSION_CCO_CREATE }, logical = Logical.OR)
	@Before(Tx.class)
	public void save() {
		String jsonStr=getPara("params");
    	logger.debug(jsonStr);
    	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class); 
         
		ArapChargeOrder arapChargeOrder = null;
		String chargeCheckOrderId = (String)dto.get("chargeCheckOrderId");
		Double total_amount = (Double)dto.get("total_amount");
		Double change_amount = (Double)dto.get("change_amount");
		String remark = (String)dto.get("remark");
		String customerId = (String)dto.get("customer_id");
		String spId = (String)dto.get("sp_id");
		String billing_unit = (String)dto.get("billing_unit");
		String beginTime_filter = (String)dto.get("beginTime_filter");
		String endTime_filter = (String)dto.get("endTime_filter");
		String payee = (String)dto.get("payee");
		String haveInvoice = (String)dto.get("have_invoice");
		
		UserLogin user = LoginUserController.getLoginUser(this);
		if (!"".equals(chargeCheckOrderId) && chargeCheckOrderId != null) {
			arapChargeOrder = ArapChargeOrder.dao.findById(chargeCheckOrderId);
			
			arapChargeOrder.set("remark", remark);
			arapChargeOrder.set("last_modified_by", user.getLong("id"));
			arapChargeOrder.set("last_modified_stamp", new Date());
			
			if (beginTime_filter != null) {
				arapChargeOrder.set("begin_time", beginTime_filter);
			}
			if (endTime_filter != null) {
				arapChargeOrder.set("end_time", endTime_filter);
			}
			arapChargeOrder.set("total_amount", total_amount);
			arapChargeOrder.set("charge_amount", change_amount);
			arapChargeOrder.set("have_invoice", haveInvoice);
			arapChargeOrder.set("billing_unit", billing_unit);
			arapChargeOrder.set("payee", payee);
			
			arapChargeOrder.update();

			boolean isCreate = false;
			updateItems(arapChargeOrder.getLong("id"), dto, change_amount, isCreate);
		} else {
			arapChargeOrder = new ArapChargeOrder();
			arapChargeOrder.set("order_no", getPara("order_no"));
			// arapAuditOrder.set("order_type", );
			arapChargeOrder.set("status", "新建");
			
			if(customerId != null && !customerId.equals("")){
				arapChargeOrder.set("payee_id", customerId);
			}
			if(spId != null && !spId.equals("")){
				arapChargeOrder.set("sp_id", spId);
			}
			arapChargeOrder.set("create_by", user.getLong("id"));
			arapChargeOrder.set("create_stamp", new Date());
			arapChargeOrder.set("remark", remark);
			arapChargeOrder.set("order_no",
					OrderNoGenerator.getNextOrderNo("YSDZ"));

			if (beginTime_filter != null) {
				arapChargeOrder.set("begin_time", beginTime_filter);
			}
			if (endTime_filter != null) {
				arapChargeOrder.set("end_time", endTime_filter);
			}
			arapChargeOrder.set("total_amount", total_amount);
			arapChargeOrder.set("charge_amount", change_amount);
			arapChargeOrder.set("have_invoice", haveInvoice);
			arapChargeOrder.set("billing_unit", billing_unit);
			arapChargeOrder.set("payee", payee);
			arapChargeOrder.save();

			boolean isCreate = true;
			updateItems(arapChargeOrder.getLong("id"), dto, change_amount, isCreate);
		}
		renderJson(arapChargeOrder);
	}

	private void updateItems(Long orderId, Map<String, ?> dto, Double change_amount, boolean isCreate) {
		List<Map> items =  (List<Map>) dto.get("items");
		for(Map item : items){
			String str_ref_order_id = (String)item.get("ORDER_ID");
			Long ref_order_id = Long.parseLong(str_ref_order_id);
			if("回单".equals(item.get("ORDER_TYPE"))){
				ReturnOrder ro = ReturnOrder.dao.findById(ref_order_id);
				ro.set("transaction_status", "对账中").update();
				
				//按比例分摊改后金额
				List<ReturnOrderFinItem> ordeItems = ReturnOrderFinItem.dao.find("select * from return_order_fin_item where return_order_id=?", ref_order_id);
				Double originTotal = 0.0;
				for(ReturnOrderFinItem orderItem : ordeItems){
					originTotal += orderItem.getDouble("amount");
				}
				
				for(ReturnOrderFinItem orderItem : ordeItems){
					Double newAmount=0.0;
					if(originTotal>0){
					 newAmount = Double.parseDouble((String)item.get("CHANGE_AMOUNT")) * (orderItem.getDouble("amount")/originTotal);
					}
					else{
						newAmount=Double.parseDouble((String)item.get("CHANGE_AMOUNT"));
					}
					orderItem.set("status", "对账中");
					orderItem.set("change_amount", newAmount).update();
				}
				if(isCreate){
					ArapChargeItem chargeItem = new ArapChargeItem();
					chargeItem.set("charge_order_id", orderId);
					chargeItem.set("ref_order_type", "回单");
					chargeItem.set("ref_order_id", ref_order_id);
					chargeItem.save();
					
					
					//回单列表的列表添加新的明细（记录调整金额）
					String returnOrderId = (String)item.get("ORDER_ID");
					Double newAmount = Double.parseDouble((String)item.get("CHANGE_AMOUNT"))-Double.parseDouble((String)item.get("AMOUNT"));
					if(newAmount != 0){
						ReturnOrderFinItem orderItem = new ReturnOrderFinItem();
						orderItem.set("return_order_id", returnOrderId);
						orderItem.set("amount", newAmount);
						orderItem.set("fin_item_id", 4);
						orderItem.set("status", "对账中");
						orderItem.set("remark", "对账调整金额");
						orderItem.set("create_date", new Date());
						orderItem.save();
					}
				}else{
					//回单列表的列表添加新的明细（记录调整金额）
					String returnOrderId = (String)item.get("ORDER_ID");
					Double newAmount = Double.parseDouble((String)item.get("CHANGE_AMOUNT"))-Double.parseDouble((String)item.get("AMOUNT"));
					ReturnOrderFinItem orderItem = ReturnOrderFinItem.dao.findFirst("select * from return_order_fin_item where return_order_id = ? and remark = '对账调整金额'",returnOrderId);
					if(orderItem == null){
						if(newAmount != 0 ){
							ReturnOrderFinItem orderItem1 = new ReturnOrderFinItem();
							orderItem1.set("return_order_id", returnOrderId);
							orderItem1.set("amount", newAmount);
							orderItem1.set("fin_item_id", 4);
							orderItem1.set("status", "对账中");
							orderItem1.set("remark", "对账调整金额");
							orderItem1.set("create_date", new Date());
							orderItem1.save();
						}
					}else{
						orderItem.set("amount", newAmount);
						orderItem.set("create_date", new Date());
						orderItem.update();
					}
				}	
			}else{
				//手工单就不允许改确认金额了
				ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(item.get("ORDER_ID"));
				arapMiscChargeOrder.set("status", "对账中").update();
				if(isCreate){
					ArapChargeItem chargeItem = new ArapChargeItem();
					chargeItem.set("charge_order_id", orderId);
					chargeItem.set("ref_order_type", "收入单");
					chargeItem.set("ref_order_id", ref_order_id);
					chargeItem.save();
				}
			}
			
		}
			
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCO_UPDATE })
	public void edit() throws ParseException {
		String id = getPara("id");
		ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(id);
		Long customerId = arapAuditOrder.get("payee_id");
		if (!"".equals(customerId) && customerId != null && customerId != 0) {
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
		String miscOrderIds = "";
		List<ArapChargeItem> arapAuditItems = ArapChargeItem.dao.find(
				"select * from arap_charge_item where charge_order_id = ?",
				arapAuditOrder.get("id"));
		for (ArapChargeItem arapAuditItem : arapAuditItems) {
			if("回单".equals(arapAuditItem.get("ref_order_type"))){
				returnOrderIds += arapAuditItem.get("ref_order_id") + ",";
			}else{
				miscOrderIds += arapAuditItem.get("ref_order_id") + ",";
			}
		}
		//去掉最后一个逗号
		if(returnOrderIds.length()>0)
			returnOrderIds = returnOrderIds.substring(0,
				returnOrderIds.length() - 1);
		if(miscOrderIds.length()>0)
			miscOrderIds = miscOrderIds.substring(0,
				miscOrderIds.length() - 1);
		
		setAttr("beginTime", beginTime);
		setAttr("endTime", endTime);

		List<Record> itemList = getItemLIst(returnOrderIds, miscOrderIds);
		setAttr("itemList", itemList);
		render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}

	public void returnOrderList() {
		String returnOrderIds = getPara("returnOrderIds");
		String order = getPara("order");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		Double totalAmount = 0.0;
		Map orderMap = new HashMap();
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from return_order ror where ror.id in ("
				+ returnOrderIds + ")";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));
		if (order != null) {
			String[] orderArray = order.split(",");
			for (int i = 0; i < orderArray.length; i++) {
				if (orderArray[i].equals("回单")) {
					// 获取当前页的数据 + sLimit;
					List<Record> orders = Db
							.find("select distinct ror.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no, dvr.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,"
									+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,"
									+ " ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) route_from,"
									+ " ifnull((select name from location where code = tor.route_to),(select name from location where code = dvr.route_to)) route_to,"
									+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id  and rofi.contract_id !='') contract_amount"
									+ " ,ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)) pickup_amount"
									+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount"
									+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount"
									+ " ,(select round(sum(rofi.amount),2) from return_order_fin_item rofi left join fin_item fi on rofi.fin_item_id = fi.id where fi.name = '保险费' and rofi.return_order_id = ror.id) insurance_amount,"
									+ " ifnull((select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and ifnull(rofi.remark,'') !='对账调整金额'),0) as charge_total_amount,"
									+ " (select sum(rofi.amount) from return_order_fin_item rofi, fin_item fi where fi.id = rofi.fin_item_id and rofi.return_order_id = ror.id and fi.type = '应收') as change_amount, "
									+ " null sp"
									+ " from return_order ror"
									+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
									+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
									+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
									+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
									+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
									+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator "
									+ " where ror.id in ("
									+ returnOrderIds
									+ ") group by ror.id,tor2.id order by ror.create_date desc "
									+ sLimit);

					orderMap.put("sEcho", pageIndex);
					orderMap.put("iTotalRecords", rec.getLong("total"));
					orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
					orderMap.put("aaData", orders);
				}
				if (orderArray[i].equals("收入单")) {
					// 获取当前页的数据 + sLimit;
					List<Record> orders = Db
							.find("SELECT amco.id id,amco.order_no order_no,NULL status_code,amco.create_stamp create_date,NULL receipt_date,amco. STATUS transaction_status,NULL order_type,"
									+ " amco.create_by creator,amco.remark remark,NULL import_ref_num,NULL _id,NULL delivery_order_id,NULL transfer_order_id,NULL notity_party_id,amco.customer_id customer_id,amco.total_amount total_amount,"
									+ " NULL path,NULL creator_name,NULL transfer_order_no,'收入单' order_tp,NULL delivery_order_no,c.abbr cname,NULL planning_time,NULL address,amcoi.customer_order_no customer_order_no,NULL route_from,NULL route_to,NULL contract_amount,"
									+ " NULL depart_time,NULL pickup_amount,NULL step_amount,NULL wait_amount,NULL other_amount,NULL load_amount,NULL warehouse_amount,NULL transfer_amount,NULL send_amount,NULL installation_amount,"
									+ " NULL super_mileage_amount,amco.total_amount charge_total_amount,NULL insurance_amount, "
									+ " c1.abbr sp"
									+ " FROM arap_misc_charge_order amco"
									+ " LEFT JOIN arap_misc_charge_order_item amcoi ON amcoi.misc_order_id = amco.id"
									+ " LEFT JOIN contact c ON c.id=amco.customer_id"
									+ " LEFT JOIN contact c1 ON c1.id=amco.sp_id"
									+ " where amco.id in ("
									+ returnOrderIds
									+ ")order by create_date desc " + sLimit);

					orderMap.put("sEcho", pageIndex);
					orderMap.put("iTotalRecords", rec.getLong("total"));
					orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
					orderMap.put("aaData", orders);
				}
			}

		}

		/*
		 * orderMap.put("sEcho", pageIndex); orderMap.put("iTotalRecords",
		 * rec.getLong("total")); orderMap.put("iTotalDisplayRecords",
		 * rec.getLong("total")); orderMap.put("aaData", orders);
		 */

		renderJson(orderMap);
	}

	// 审核
	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCO_AFFIRM })
	@Before(Tx.class)
	public void auditChargeCheckOrder() {
		String chargeCheckOrderId = getPara("chargeCheckOrderId");
		ArapChargeOrder arapAuditOrder = null;
		if (chargeCheckOrderId != null && !"".equals(chargeCheckOrderId)) {
			arapAuditOrder = ArapChargeOrder.dao.findById(chargeCheckOrderId);
			arapAuditOrder.set("status", "已确认");
			arapAuditOrder.update();
			List<ArapMiscChargeOrder> list = ArapMiscChargeOrder.dao
					.find("select * from arap_misc_charge_order where charge_order_id = ?",
							arapAuditOrder.get("id"));
			if (list.size() > 0) {
				for (ArapMiscChargeOrder arapMiscChargeOrder : list) {
					arapMiscChargeOrder.set("status", "对账已确认");
					arapMiscChargeOrder.update();
				}
			}
			updateReturnOrderStatus(arapAuditOrder, "对账已确认");
		}
		renderJson(arapAuditOrder);
	}

	// 更新回单状态
	private void updateReturnOrderStatus(ArapChargeOrder arapAuditOrder,
			String status) {
		List<ArapChargeItem> arapAuditItems = ArapChargeItem.dao.find(
				"select * from arap_charge_item where charge_order_id = ?",
				arapAuditOrder.get("id"));
		for (ArapChargeItem arapAuditItem : arapAuditItems) {
			ReturnOrder returnOrder = ReturnOrder.dao.findById(arapAuditItem
					.get("ref_order_id"));
			returnOrder.set("transaction_status", status);
			returnOrder.update();
		}
	}

	// billing order 列表
	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCO_LIST })
	public void list() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		
		
		 //升降序
    	String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		
		String orderByStr = " order by A.create_stamp desc ";
        if(colName.length()>0){
        	orderByStr = " order by A."+colName+" "+sortBy;
        }

		String orderNo = getPara("orderNo");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String status = getPara("status");
		String customer = getPara("customer");
		String sp = getPara("sp");
		String tihuo = getPara("tihuo");
		String office = getPara("office");
		String sqlTotal = "";
		String sql = "select distinct "//aao.*, 
				+ " aao.id,"
				+ " aao.order_no,"
			    + " aao.order_type,"
			    + " aao.payee_id,"
			    + " aao.remark,"
			    + " aao.create_stamp,"
			    + " round(aao.total_amount,2) total_amount,"
			    + " round(aao.debit_amount,2) debit_amount,"
			    + " round(aao.charge_amount,2) charge_amount,"
				+ " usl.user_name as creator_name,"
				+ " c.abbr cname,usl.c_name,"
				+ " (select case "
				+ "	when aci.status = '已收款确认' then aci.status "
				+ " when aci.status ='已审批' then '已记录'"
				+ "	when aci.status != '已收款确认' and aci.status != '' then '开票记录中'"
				+ " when aciao.status = '已审批' then '已开票'"
				+ "	when aciao.status != '已审批' and aciao.status !='' then '开票申请中'"
				+ " else aco.status"
				+ "	end "
				+ "	from arap_charge_order aco"
				+ "	left join arap_charge_invoice_application_item aciai on aco.id = aciai.charge_order_id"
				+ "	left join arap_charge_invoice_application_order aciao on aciai.invoice_application_id = aciao.id"
				+ "	left join arap_charge_invoice aci on aciao.invoice_order_id = aci.id"
				+ " where aco.id = aao.id) as order_status,"
				+ " c1.abbr sp "
				+ " from arap_charge_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join contact c1 on c1.id = aao.sp_id"
				+ " left join user_login usl on usl.id=aao.create_by ";

		String condition = "";

		// TODO 订单号，客户，状态，开始和结束时间 已做完
		if (orderNo == null && beginTime == null && endTime == null
				&& status == null && customer == null && sp == null
				&& tihuo == null && office == null) {

			condition = " order by aao.create_stamp desc ";
		} else {

			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			condition = " where ifnull(aao.order_no,'') like '%" + orderNo
					+ "%' " + " and ifnull(c.abbr,'') like '%" + customer
					+ "%' " + " and ifnull(aao.status,'') like '%" + status
					+ "%' " + " and aao.create_stamp between '" + beginTime
					+ "' and '" + endTime + "' "
					+ " order by aao.create_stamp desc ";
		}
		sqlTotal = "select count(1) total from (" + sql + condition + ") as A";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		// logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find("select * from (" + sql + condition+") A" + orderByStr + sLimit);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	public void checkChargeMiscList() {
		String chargeCheckOrderId = getPara("chargeCheckOrderId");
		if (chargeCheckOrderId == null || "".equals(chargeCheckOrderId)) {
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
				+ " left join arap_misc_charge_order_item amcoi on amcoi.misc_order_id = amco.id where aco.id = "
				+ chargeCheckOrderId;
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select amcoi.*,amco.order_no misc_order_no,c.abbr cname,fi.name name from arap_misc_charge_order amco"
						+ " left join arap_charge_order aco on aco.id = amco.charge_order_id "
						+ " left join arap_misc_charge_order_item amcoi on amcoi.misc_order_id = amco.id "
						+ " left join party p on p.id = aco.payee_id left join contact c on c.id = p.contact_id "
						+ " left join fin_item fi on amcoi.fin_item_id = fi.id where aco.id =  "
						+ chargeCheckOrderId + " " + sLimit);

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

	public void externalMiscOrderList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from arap_misc_charge_order where ifnull(charge_order_id, 0) = 0";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select amco.*,aco.order_no charge_order_no from arap_misc_charge_order amco"
				+ " left join arap_charge_order aco on aco.id = amco.charge_order_id"
				+ " where ifnull(charge_order_id, 0) = 0 order by amco.create_stamp desc "
				+ sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	public void updateChargeMiscOrder() {
		String micsOrderIds = getPara("micsOrderIds");
		String chargeCheckOrderId = getPara("chargeCheckOrderId");
		ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao
				.findById(chargeCheckOrderId);
		if (micsOrderIds != null && !"".equals(micsOrderIds)) {
			String[] micsOrderIdArr = micsOrderIds.split(",");
			for (int i = 0; i < micsOrderIdArr.length; i++) {
				ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao
						.findById(micsOrderIdArr[i]);
				arapMiscChargeOrder.set("charge_order_id", chargeCheckOrderId);
				arapMiscChargeOrder.set("status", "已入对账单");
				arapMiscChargeOrder.update();
			}

			Record record = Db
					.findFirst(
							"select sum(amount) sum_amount from arap_misc_charge_order mco"
									+ " left join arap_misc_charge_order_item mcoi on mcoi.misc_order_id = mco.id where mco.charge_order_id = ?",
							chargeCheckOrderId);
			Double total_amount = arapChargeOrder.getDouble("total_amount");
			Double debit_amount = record.getDouble("sum_amount");
			arapChargeOrder.set("debit_amount", debit_amount);
			arapChargeOrder.set("charge_amount", total_amount - debit_amount);
			arapChargeOrder.update();
		}
		renderJson(arapChargeOrder);
	}
}
