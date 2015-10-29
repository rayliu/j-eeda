package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.ReturnOrder;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.profile.Contact;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeItemConfirmController extends Controller {
	private Logger logger = Logger.getLogger(ChargeItemConfirmController.class);
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_AFFIRM})
	public void index() {
		render("/yh/arap/ChargeItemConfirm/ChargeItemConfirmList.html");
	}

	public void confirm() {
		String ids = getPara("ids");
		String[] idArray = ids.split(",");
		logger.debug(String.valueOf(idArray.length));

		String customerId = getPara("customerId");
		Party party = Party.dao.findById(customerId);

		Contact contact = Contact.dao.findById(party.get("contact_id")
				.toString());
		setAttr("customer", contact);
		setAttr("type", "CUSTOMER");
		setAttr("classify", "receivable");
		render("/yh/arap/ChargeAcceptOrder/ChargeCheckOrderEdit.html");
	}

	//
	public void list() {
		String customer = getPara("customer");//客户
		String beginTime = getPara("beginTime");//开始时间
		String endTime = getPara("endTime");//结束时间
		String transferOrderNo = getPara("transferOrderNo");//运输单号
		String customerNo = getPara("customerNo");//客户订单号
		String start = getPara("start");//始发地
		String orderNo= getPara("orderNo");//业务单号

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
		
		String sqlTotal = "";
		String sql = "";
		// 收入状态条件没有过滤

		if (customer == null && beginTime == null && endTime == null
				&& transferOrderNo == null && customerNo == null && start == null && orderNo==null) {
			sqlTotal = "select count(1) total from return_order ror where ror.transaction_status = '已签收' ";
			sql = "select distinct ror.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no,'回单' order_tp, dvr.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,"
					+ " tor.planning_time, c.address address, "
					+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,"
					+ " ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) route_from,"
					+ " ifnull((select name from location where code = tor.route_to),(select name from location where code = dvr.route_to)) route_to,"
					+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id and rofi.fin_type = 'charge' and rofi.contract_id !='') contract_amount,"
					+ " DATE(dvr.appointment_stamp) AS depart_time,"
					+ " ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)) pickup_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '等待费') wait_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '其他费用') other_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '装卸费') load_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount,"
					+ " (select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '运输费') transfer_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '送货费') send_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '安装费') installation_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '超里程费') super_mileage_amount,"
					+ " ifnull((select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收'),0) as charge_total_amount,"
					+ " (select round(sum(rofi.amount),2) from return_order_fin_item rofi left join fin_item fi on rofi.fin_item_id = fi.id where fi.name = '保险费' and rofi.return_order_id = ror.id) insurance_amount,"
					/*
					 * +
					 * " ifnull((select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor.id order by dtr.turnout_time asc limit 0,1), (select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor2.id order by dtr.turnout_time asc limit 0,1)) departure_time"
					 */
					+ " null sp "
					+ " from return_order ror"
					+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
					+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
					+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
					+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
					+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
					+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator where ror.transaction_status = '已签收' group by ror.id,tor2.id"
					+ " UNION"
					+ " (SELECT amco.id id , amco.order_no order_no,NULL status_code,amco.create_stamp create_date,NULL receipt_date,amco. STATUS transaction_status,NULL order_type,"
					+ " amco.create_by creator,amco.remark remark,NULL import_ref_num,NULL _id,NULL delivery_order_id,NULL transfer_order_id,NULL notity_party_id,amco.customer_id customer_id,amco.total_amount total_amount,"
					+ " NULL path,NULL creator_name,NULL transfer_order_no,'收入单' order_tp,NULL delivery_order_no,c.abbr cname,NULL planning_time,NULL address,"
					+ " (select GROUP_CONCAT(DISTINCT amcoi.customer_order_no SEPARATOR '\r\n') "
					+ " 	from arap_misc_charge_order_item amcoi "
					+ " 	where amcoi.misc_order_id = amco.id) customer_order_no,"
					+ " NULL route_from,NULL route_to,NULL contract_amount,"
					+ " NULL depart_time,NULL pickup_amount,NULL step_amount,NULL wait_amount,NULL other_amount,NULL load_amount,NULL warehouse_amount,NULL transfer_amount,NULL send_amount,NULL installation_amount,"
					+ " NULL super_mileage_amount,amco.total_amount charge_total_amount,NULL insurance_amount , c1.abbr sp "
					+ " FROM arap_misc_charge_order amco "
					+ " LEFT JOIN party p on p.id = amco.customer_id "
					+ " LEFT JOIN contact c ON c.id = p.id"
					+ " LEFT JOIN party p1 on p1.id = amco.sp_id "
					+ " LEFT JOIN contact c1 ON c1.id = p1.id "
					+ " where amco. STATUS='新建' and amco.type = 'biz' and amco.total_amount!=0 )"
					+ " order by create_date desc ";
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			sqlTotal = "select count(*) total from (select distinct ror.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no,'回单' order_tp, dvr.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,"
					+ " tor.planning_time, c.address address, "
					+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,"
					+ " ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) route_from,"
					+ " ifnull((select name from location where code = tor.route_to),(select name from location where code = dvr.route_to)) route_to,"
					+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id and rofi.fin_type = 'charge' and rofi.contract_id !='') contract_amount,"
					+ " DATE(dvr.appointment_stamp) AS depart_time,"
					+ " ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)) pickup_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '等待费') wait_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '其他费用') other_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '装卸费') load_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount,"
					+ " (select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '运输费') transfer_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '送货费') send_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '安装费') installation_amount,"
					+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '超里程费') super_mileage_amount,"
					+ " ifnull((select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收'),0) as charge_total_amount,"
					+ " (select round(sum(rofi.amount),2) from return_order_fin_item rofi left join fin_item fi on rofi.fin_item_id = fi.id where fi.name = '保险费' and rofi.return_order_id = ror.id) insurance_amount,"
					+ " null sp"
					+ " from return_order ror"
					+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
					+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
					+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
					+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
					+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
					+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator where ror.transaction_status = '已签收' group by ror.id,tor2.id"
					+ " UNION"
					+ " (SELECT amco.id id,amco.order_no order_no,NULL status_code,amco.create_stamp create_date,NULL receipt_date,amco. STATUS transaction_status,NULL order_type,"
					+ " amco.create_by creator,amco.remark remark,NULL import_ref_num,NULL _id,NULL delivery_order_id,NULL transfer_order_id,NULL notity_party_id,amco.customer_id customer_id,amco.total_amount total_amount,"
					+ " NULL path,NULL creator_name,NULL transfer_order_no,'收入单' order_tp,NULL delivery_order_no,c.abbr cname,NULL planning_time,NULL address,"
					+ " (select GROUP_CONCAT(DISTINCT amcoi.customer_order_no SEPARATOR '\r\n') "
							+ " 	from arap_misc_charge_order_item amcoi "
							+ " 	where amcoi.misc_order_id = amco.id) customer_order_no,"
					+ "NULL route_from,NULL route_to,NULL contract_amount,"
					+ " NULL depart_time,NULL pickup_amount,NULL step_amount,NULL wait_amount,NULL other_amount,NULL load_amount,NULL warehouse_amount,NULL transfer_amount,NULL send_amount,NULL installation_amount,"
					+ " NULL super_mileage_amount,amco.total_amount charge_total_amount,NULL insurance_amount,c1.abbr sp"
					+ " FROM arap_misc_charge_order amco"
					+ " LEFT JOIN contact c ON c.id=amco.customer_id"
					+ " LEFT JOIN contact c1 ON c1.id=amco.sp_id"
					+ " where amco. STATUS='新建' and amco.type = 'biz' and amco.total_amount!=0 )) ror"
					+ " where ifnull(cname,'')like '%"+customer+"%'"
				    + " and ifnull(route_from,'') like '%"+start+"%' and ifnull(order_no,'') like '%"+orderNo+"%'  and ifnull(customer_order_no,'')like '%"+customerNo+"%'"
				    + " and IFNULL(transfer_order_no,'') like '%"+transferOrderNo+"%' and IFNULL(planning_time,'1-1-1')  between '"
					+ beginTime
					+ "' and '"
					+ endTime
					+ "' ";
			sql = "select * from (select distinct ror.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no,'回单' order_tp, dvr.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,"
			+ " tor.planning_time, c.address address, "
			+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,"
			+ " ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) route_from,"
			+ " ifnull((select name from location where code = tor.route_to),(select name from location where code = dvr.route_to)) route_to,"
			+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id and rofi.fin_type = 'charge' and rofi.contract_id !='') contract_amount,"
			+ " DATE(dvr.appointment_stamp) AS depart_time,"
			+ " ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)) pickup_amount,"
			+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount,"
			+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '等待费') wait_amount,"
			+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '其他费用') other_amount,"
			+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '装卸费') load_amount,"
			+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount,"
			+ " (select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '运输费') transfer_amount,"
			+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '送货费') send_amount,"
			+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '安装费') installation_amount,"
			+ " (select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '超里程费') super_mileage_amount,"
			+ " ifnull((select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收'),0) as charge_total_amount,"
			+ " (select round(sum(rofi.amount),2) from return_order_fin_item rofi left join fin_item fi on rofi.fin_item_id = fi.id where fi.name = '保险费' and rofi.return_order_id = ror.id) insurance_amount,"
			+ " null sp"
			+ " from return_order ror"
			+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
			+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
			+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
			+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
			+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
			+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator where ror.transaction_status = '已签收' group by ror.id,tor2.id"
			+ " UNION"
			+ " (SELECT amco.id id,amco.order_no order_no,NULL status_code,amco.create_stamp create_date,NULL receipt_date,amco. STATUS transaction_status,NULL order_type,"
			+ " amco.create_by creator,amco.remark remark,NULL import_ref_num,NULL _id,NULL delivery_order_id,NULL transfer_order_id,NULL notity_party_id,amco.customer_id customer_id,amco.total_amount total_amount,"
			+ " NULL path,NULL creator_name,NULL transfer_order_no,'收入单' order_tp,NULL delivery_order_no,c.abbr cname,NULL planning_time,NULL address,"
			+ " (select GROUP_CONCAT(DISTINCT amcoi.customer_order_no SEPARATOR '\r\n') "
					+ " 	from arap_misc_charge_order_item amcoi "
					+ " 	where amcoi.misc_order_id = amco.id) customer_order_no,"
			+ "NULL route_from,NULL route_to,NULL contract_amount,"
			+ " NULL depart_time,NULL pickup_amount,NULL step_amount,NULL wait_amount,NULL other_amount,NULL load_amount,NULL warehouse_amount,NULL transfer_amount,NULL send_amount,NULL installation_amount,"
			+ " NULL super_mileage_amount,amco.total_amount charge_total_amount,NULL insurance_amount,c1.abbr sp"
			+ " FROM arap_misc_charge_order amco"
			+ " LEFT JOIN contact c ON c.id=amco.customer_id"
			+ " LEFT JOIN contact c1 ON c1.id=amco.sp_id"
			+ " where amco. STATUS='新建' and amco.type = 'biz' and amco.total_amount!=0 )) a"
			+ " where ifnull(cname,'')like '%"+customer+"%'"
		    + " and ifnull(route_from,'') like '%"+start+"%' and ifnull(order_no,'') like '%"+orderNo+"%' and ifnull(customer_order_no,'')like '%"+customerNo+"%'"
		    + " and IFNULL(transfer_order_no,'') like '%"+transferOrderNo+"%' and IFNULL(planning_time,'1-1-1')  between '"
			+ beginTime
			+ "' and '"
			+ endTime
			+ "' "
			+ " order by create_date desc ";
		}
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		List<Record> BillingOrders = Db.find("select * from (" + sql + ") A" + orderByStr  + sLimit);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_AFFIRM})
	public void chargeConfiremReturnOrder() {
		String orderno = getPara("orderno");
		String returnOrderIds = getPara("returnOrderIds");
		String[] returnOrderArr = returnOrderIds.split(",");
		String[] ordernoArr = orderno.split(",");
		for (int i = 0; i < returnOrderArr.length; i++) {
			if ("回单".equals(ordernoArr[i])) {
				ReturnOrder returnOrder = ReturnOrder.dao
						.findById(returnOrderArr[i]);
				returnOrder.set("transaction_status", "已确认");
				returnOrder.update();
			}
			if ("收入单".equals(ordernoArr[i])) {
				ArapMiscChargeOrder arapmiscchargeorder = ArapMiscChargeOrder.dao
						.findById(returnOrderArr[i]);
				arapmiscchargeorder.set("STATUS", "已确认");
				arapmiscchargeorder.update();
			}
		}
		renderJson("{\"success\":true}");
	}
}
