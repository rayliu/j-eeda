package controllers.yh.arap.ap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.InsuranceOrder;
import models.Party;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class CostItemConfirmController extends Controller {
    private Logger logger = Logger.getLogger(CostItemConfirmController.class);

    public void index() {
    	if(LoginUserController.isAuthenticated(this))
    	    render("/yh/arap/CostItemConfirm/CostItemConfirmList.html");
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
    	if(LoginUserController.isAuthenticated(this))
        render("/yh/arap/CostAcceptOrder/CostCheckOrderEdit.html");
    }

    // 应付条目列表
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from (select distinct dor.id,dor.order_no,dor.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator,'配送' business_type, "
			+ " (select sum(amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.type = '应付') pay_amount, "
			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '\r\n')"
			+ " transfer_order_no,'有回单' return_order_collection,dor.remark"
			+ " from return_order ror "
			+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
			+ " left join party p on p.id = dor.sp_id "
			+ " left join contact c on c.id = p.contact_id "
			+ " left join delivery_order_item doi on doi.delivery_id = dor.id "
			+ " left join transfer_order_item_detail toid on toid.id = doi.transfer_item_detail_id "
			+ " left join transfer_order_item toi on toi.id = toid.item_id "
			+ " left join product prod on toi.product_id = prod.id "
			+ " left join user_login ul on ul.id = dor.create_by "
			+ " where dor.id = ror.delivery_order_id group by dor.id"
			+ " union"
			+ " select distinct dpr.id,dpr.depart_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'零担' business_type, "
			+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
			+ " transfer_order_no,'没回单' return_order_collection,dpr.remark"
			+ " from return_order ror "
			+ " left join delivery_order dor on dor.id = ror.delivery_order_id  "
			+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
			+ " left join depart_transfer dtr on dtr.order_id = doi.transfer_order_id"
			+ " left join depart_order dpr on dpr.id = dtr.depart_id"
			+ " left join transfer_order tor on tor.id = dtr.order_id "
			+ " left join transfer_order_item toi on toi.order_id = tor.id "
			+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id"
			+ " left join product prod on toi.product_id = prod.id "
			+ " left join user_login ul on ul.id = dpr.create_by "
			+ " left join party p on p.id = dpr.sp_id "
			+ " left join contact c on c.id = p.contact_id"
			+ " where dor.id = ror.delivery_order_id and (ifnull(dpr.id, 0) > 0)"
			+ " group by dpr.id"
			+ " union"
			+ " select distinct dpr.id,dpr.depart_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'提货' business_type, "
			+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
			+ " transfer_order_no,'没回单' return_order_collection,dpr.remark"
			+ " from return_order ror "
			+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
			+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
			+ " left join depart_transfer dtr on dtr.order_id = doi.transfer_order_id"
			+ " left join depart_order dpr on dpr.id = dtr.pickup_id"
			+ " left join transfer_order tor on tor.id = dtr.order_id "
			+ " left join transfer_order_item_detail toid on toid.order_id = tor.id "
			+ " left join transfer_order_item toi on toi.id = toid.item_id "
			+ " left join product prod on toi.product_id = prod.id "
			+ " left join user_login ul on ul.id = dpr.create_by "
			+ " left join party p on p.id = dpr.sp_id "
			+ " left join contact c on c.id = p.contact_id"
			+ " where dor.id = ror.delivery_order_id and (ifnull(dpr.id, 0) > 0)"
			+ " group by dpr.id)";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select distinct dor.id,dor.order_no,dor.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator,'配送' business_type, "
			+ " (select sum(amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.type = '应付') pay_amount, "
			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '\r\n')"
			+ " transfer_order_no,'有回单' return_order_collection,dor.remark"
			+ " from return_order ror "
			+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
			+ " left join party p on p.id = dor.sp_id "
			+ " left join contact c on c.id = p.contact_id "
			+ " left join delivery_order_item doi on doi.delivery_id = dor.id "
			+ " left join transfer_order_item_detail toid on toid.id = doi.transfer_item_detail_id "
			+ " left join transfer_order_item toi on toi.id = toid.item_id "
			+ " left join product prod on toi.product_id = prod.id "
			+ " left join user_login ul on ul.id = dor.create_by "
			+ " where dor.id = ror.delivery_order_id group by dor.id"
			+ " union"
			+ " select distinct dpr.id,dpr.depart_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'零担' business_type, "
			+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
			+ " transfer_order_no,'没回单' return_order_collection,dpr.remark"
			+ " from return_order ror "
			+ " left join delivery_order dor on dor.id = ror.delivery_order_id  "
			+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
			+ " left join depart_transfer dtr on dtr.order_id = doi.transfer_order_id"
			+ " left join depart_order dpr on dpr.id = dtr.depart_id"
			+ " left join transfer_order tor on tor.id = dtr.order_id "
			+ " left join transfer_order_item toi on toi.order_id = tor.id "
			+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id"
			+ " left join product prod on toi.product_id = prod.id "
			+ " left join user_login ul on ul.id = dpr.create_by "
			+ " left join party p on p.id = dpr.sp_id "
			+ " left join contact c on c.id = p.contact_id"
			+ " where dor.id = ror.delivery_order_id and (ifnull(dpr.id, 0) > 0)"
			+ " group by dpr.id"
			+ " union"
			+ " select distinct dpr.id,dpr.depart_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'提货' business_type, "
			+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
			+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n')"
			+ " transfer_order_no,'没回单' return_order_collection,dpr.remark"
			+ " from return_order ror "
			+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
			+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
			+ " left join depart_transfer dtr on dtr.order_id = doi.transfer_order_id"
			+ " left join depart_order dpr on dpr.id = dtr.pickup_id"
			+ " left join transfer_order tor on tor.id = dtr.order_id "
			+ " left join transfer_order_item_detail toid on toid.order_id = tor.id "
			+ " left join transfer_order_item toi on toi.id = toid.item_id "
			+ " left join product prod on toi.product_id = prod.id "
			+ " left join user_login ul on ul.id = dpr.create_by "
			+ " left join party p on p.id = dpr.sp_id "
			+ " left join contact c on c.id = p.contact_id"
			+ " where dor.id = ror.delivery_order_id and (ifnull(dpr.id, 0) > 0)"
			+ " group by dpr.id " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    public void costConfiremReturnOrder(){
    	String ids = getPara("ids");
    	String orderNos = getPara("orderNos");
    	String[] idArr = ids.split(",");
    	String[] orderNoArr = orderNos.split(",");
    	for(int i=0 ; i<idArr.length ; i++){
    		String preOrderNo = orderNoArr[i].substring(0,2);
    		if("PC".equals(preOrderNo)){
    			DepartOrder pickupOrder = DepartOrder.dao.findById(idArr[i]);
    			pickupOrder.set("audit_status", "已确认");
    			pickupOrder.update();
    		}else if("FC".equals(preOrderNo)){
    			DepartOrder departOrder = DepartOrder.dao.findById(idArr[i]);
    			departOrder.set("audit_status", "已确认");
    			departOrder.update();
    		}else if("PS".equals(preOrderNo)){
    			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(idArr[i]);
    			deliveryOrder.set("audit_status", "已确认");
    			deliveryOrder.update();
    		}else{
    			InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(idArr[i]);
    			insuranceOrder.set("audit_status", "已确认");
    			insuranceOrder.update();
    		}
    	}
        renderJson("{\"success\":true}");
    }
}
