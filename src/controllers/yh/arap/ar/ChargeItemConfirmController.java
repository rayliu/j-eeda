package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.ReturnOrder;
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

        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
        setAttr("customer", contact);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
        render("/yh/arap/ChargeAcceptOrder/ChargeCheckOrderEdit.html");
    }

    // 
    public void list() {
    	String customer = getPara("customer");
    	String beginTime = getPara("beginTime");
    	String endTime = getPara("endTime");
    	String orderNo = getPara("orderNo");
    	String customerNo = getPara("customerNo");
    	String start = getPara("start");
    	String status = getPara("status");
    	
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal ="";
        String sql="";
        //收入状态条件没有过滤
        
        if(customer==null&&beginTime==null&&endTime==null&&orderNo==null
        		&&customerNo==null&&start==null){
        	sqlTotal= "select count(1) total from return_order ror where ror.transaction_status = '已签收'";
	        sql= "select distinct ror.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no, dvr.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,"
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
					+ " (select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收') as charge_total_amount,"
					+ " (select round(sum(rofi.amount),2) from return_order_fin_item rofi left join fin_item fi on rofi.fin_item_id = fi.id where fi.name = '保险费' and rofi.return_order_id = ror.id) insurance_amount"
					/*+ " ifnull((select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor.id order by dtr.turnout_time asc limit 0,1), (select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor2.id order by dtr.turnout_time asc limit 0,1)) departure_time"*/			
					+ " from return_order ror"
					+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
					+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
					+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
					+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
					+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
					+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator where ror.transaction_status = '已签收' group by ror.id,tor2.id"
					+ " order by ror.create_date desc " + sLimit;
        }else{
        	if (beginTime == null || "".equals(beginTime)) {
        		beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
        	sqlTotal= "select count(*) total from (select ror.id from return_order ror"
					+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
					+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
					+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
					+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
					+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
					+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator "
					+ " where ror.transaction_status = '已签收' "
					+ " and (ifnull(tor.customer_order_no,'') like '%" + customerNo+"%' or ifnull(tor2.customer_order_no,'') like '%" + customerNo+"%') "
					+ " and ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) like '%"+start
					+ "%' and ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) like '%"+orderNo
					+ "%' and ifnull(c.abbr,c2.abbr) like '%"+customer
					+ "%' and tor.planning_time between '"+beginTime+"' and '"+endTime+"' "
					+ " group by ror.id"
					+ " order by ror.create_date desc ) ror";
	        sql= "select distinct ror.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) transfer_order_no, dvr.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,"
					+ " tor.planning_time, c.address address,"
	        		+ " DATE(dvr.appointment_stamp) AS depart_time,"
					+ " ifnull(tor.customer_order_no,tor2.customer_order_no) customer_order_no,"
					+ " ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) route_from,"
					+ " ifnull((select name from location where code = tor.route_to),(select name from location where code = dvr.route_to)) route_to,"
					+ " (select sum(rofi.amount) from return_order_fin_item rofi where rofi.return_order_id = ror.id and rofi.fin_type = 'charge' and rofi.contract_id !='') contract_amount"
					+ " ,round(ifnull(dofi.amount,(select sum(dofi.amount) from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id left join depart_transfer dt on dt.order_id = tor.id left join depart_order dor on dor.id = dt.pickup_id and dor.combine_type = 'PICKUP' left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费' where dvr.id = ror.delivery_order_id)),2) pickup_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '台阶费') step_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '等待费') wait_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '其他费用') other_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '装卸费') load_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '仓租费') warehouse_amount"
					+ " ,(select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '运输费') transfer_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '送货费') send_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '安装费') installation_amount"
					+ " ,(select rofi.amount from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收' and fi.name = '超里程费') super_mileage_amount"
					+ " ,(select sum(rofi.amount) from return_order_fin_item rofi left join fin_item fi on fi.id = rofi.fin_item_id where rofi.return_order_id = ror.id and fi.type = '应收') as charge_total_amount,"
					+ " ,(select round(sum(rofi.amount),2) from return_order_fin_item rofi left join fin_item fi on rofi.fin_item_id = fi.id where fi.name = '保险费' and rofi.return_order_id = ror.id) insurance_amount"
					/*+ " ifnull((select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor.id order by dtr.turnout_time asc limit 0,1), (select dtr.departure_time from depart_transfer dt left join depart_order dtr on dtr.id = dt.depart_id where ifnull(dt.depart_id, 0) > 0 and dt.order_id = tor2.id order by dtr.turnout_time asc limit 0,1)) departure_time"*/
					+ " from return_order ror"
					+ " left join transfer_order tor on tor.id = ror.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
					+ " left join depart_transfer dt on (dt.order_id = tor.id and ifnull(dt.pickup_id, 0)>0)"
					+ " left join delivery_order dvr on ror.delivery_order_id = dvr.id left join delivery_order_item doi on doi.delivery_id = dvr.id "
					+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id "
					+ " left join transfer_order_fin_item tofi on tor.id = tofi.order_id left join depart_order dor on dor.id = dt.pickup_id left join pickup_order_fin_item dofi on dofi.pickup_order_id = dor.id left join fin_item fi on fi.id = dofi.fin_item_id and fi.type='应收' and fi.name='提货费'"
					+ " left join transfer_order_fin_item tofi2 on tor.id = tofi2.order_id left join user_login usl on usl.id=ror.creator "
					+ " where ror.transaction_status = '已签收' "
					+ " and (ifnull(tor.customer_order_no,'') like '%" + customerNo+"%' or ifnull(tor2.customer_order_no,'') like '%" + customerNo+"%') "
					+ " and ifnull((select name from location where code = tor.route_from),(select name from location where code = tor2.route_from)) like '%"+start
					+ "%' and ifnull(tor.order_no,(select group_concat(distinct tor.order_no separator '\r\n') from delivery_order dvr left join delivery_order_item doi on doi.delivery_id = dvr.id left join transfer_order tor on tor.id = doi.transfer_order_id where dvr.id = ror.delivery_order_id)) like '%"+orderNo
					+ "%' and ifnull(c.abbr,c2.abbr) like '%"+customer
					+ "%' and tor.planning_time between '"+beginTime+"' and '"+endTime+"' "
					+ " group by ror.id,tor2.id"
					+ " order by ror.create_date desc " + sLimit;
        }
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total")); 
        
        
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_AFFIRM})
    public void chargeConfiremReturnOrder(){
    	String returnOrderIds = getPara("returnOrderIds");
    	String[] returnOrderArr = returnOrderIds.split(",");
    	for(int i=0 ; i<returnOrderArr.length ; i++){
    		ReturnOrder returnOrder = ReturnOrder.dao.findById(returnOrderArr[i]);
    		returnOrder.set("transaction_status", "已确认");
    		returnOrder.update();
    	}
        renderJson("{\"success\":true}");
    }
}
