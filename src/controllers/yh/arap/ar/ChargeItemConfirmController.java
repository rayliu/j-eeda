package controllers.yh.arap.ar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAuditOrder;
import models.Party;
import models.yh.profile.Contact;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class ChargeItemConfirmController extends Controller {
    private Logger logger = Logger.getLogger(ChargeItemConfirmController.class);

    public void index() {
    	if(LoginUserController.isAuthenticated(this))
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
    	if(LoginUserController.isAuthenticated(this))
        render("/yh/arap/ChargeAcceptOrder/ChargeCheckOrderEdit.html");
    }

    // 应付条目列表
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_audit_order aao where aao.status = 'confirmed'";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aao.*,c.abbr cname,ror.order_no return_order_no,tor.order_no transfer_order_no,dor.order_no delivery_order_no,ul.user_name creator_name from arap_audit_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id "
				+ " left join arap_audit_item aai on aai.audit_order_id= aao.id "
				+ " left join return_order ror on ror.id = aai.ref_order_id "
				+ " left join transfer_order tor on tor.id = ror.transfer_order_id "
				+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
				+ " left join user_login ul on ul.id = aao.create_by where aao.status = 'confirmed' order by aao.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
}
