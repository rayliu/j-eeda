package controllers.yh.arap.ap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.ReturnOrder;
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

        // TODO 条数怎么统计
        String sqlTotal = "select count(1) total from return_order ror where ror.transaction_status = '新建'";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select distinct dor.id,dor.order_no,dor.status,ror.transaction_status,c.abbr spname,dor.route_from,dor.route_to,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator from return_order ror"  
				+ " left join delivery_order dor on dor.id = ror.delivery_order_id"
				+ " left join party p on p.id = dor.sp_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
				+ " left join transfer_order_item_detail toid on toid.id = doi.transfer_item_detail_id"
				+ " left join transfer_order_item toi on toi.id = toid.item_id"
				+ " left join product prod on toi.product_id = prod.id"
				+ " left join user_login ul on ul.id = dor.create_by"
				+ " where dor.id = ror.delivery_order_id"
				+ " union"
				+ " select distinct tor.id,tor.order_no,tor.status,ror.transaction_status,c.abbr spname,tor.route_from,tor.route_to,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,tor.create_stamp create_stamp,ul.user_name creator from return_order ror "
				+ " left join delivery_order dor on dor.id = ror.delivery_order_id"
				+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
				+ " left join transfer_order tor on tor.id = doi.transfer_order_id"
				+ " left join party p on p.id = tor.sp_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " left join transfer_order_item toi on toi.order_id = tor.id"
				+ " left join product prod on toi.product_id = prod.id"
				+ " left join user_login ul on ul.id = tor.create_by"
				+ " where dor.id = ror.delivery_order_id " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
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
