package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.InsuranceOrder;
import models.Party;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.delivery.DeliveryOrder;
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
public class CostConfirmController extends Controller {
    private Logger logger = Logger.getLogger(CostConfirmController.class);
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CONFIRMATION})
    public void index() {
    	    //render("/yh/arap/CostConfirm/CostConfirmList.html");
    	render("/yh/arap/CostConfirm/CostConfrimAdd.html");
    }
    
    
    
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	public void create() {
		String ids = getPara("ids");
		setAttr("invoiceApplicationOrderIds", ids);
//		if(ids != null && !"".equals(ids)){
//			String[] idArray = ids.split(",");
//			logger.debug(String.valueOf(idArray.length));
//	
//			setAttr("chargeCheckOrderIds", ids);
//			ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(idArray[0]);
//			Long customerId = arapChargeOrder.get("payee_id");
//			if (!"".equals(customerId) && customerId != null) {
//				Party party = Party.dao.findById(customerId);
//				setAttr("party", party);
//				Contact contact = Contact.dao.findById(party.get("contact_id").toString());
//				setAttr("customer", contact);
//				setAttr("type", "CUSTOMER");
//				setAttr("classify", "");
//			}
//		}
//
//		setAttr("saveOK", false);
//		String name = (String) currentUser.getPrincipal();
//		List<UserLogin> users = UserLogin.dao
//				.find("select * from user_login where user_name='" + name + "'");
//		setAttr("create_by", users.get(0).get("id"));
//		
//		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
//		setAttr("userLogin", userLogin);
//
//		List<Record> receivableItemList = Collections.EMPTY_LIST;
//		receivableItemList = Db.find("select * from fin_item where type='应收'");
//		setAttr("receivableItemList", receivableItemList);
//		setAttr("status", "new");
		render("/yh/arap/CostConfirm/CostConfrimAdd.html");
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
        render("/yh/arap/CostAcceptOrder/CostCheckOrderEdit.html");
    }

    // 应付申请列表
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        String invoiceApplicationOrderIds = getPara("invoiceApplicationOrderIds");
        String sqlTotal = "";
        String sql = "";
        List<Record> record = null;
        Record re = null;
        if (invoiceApplicationOrderIds != null && !"".equals(invoiceApplicationOrderIds)) {
			String[] idArray = invoiceApplicationOrderIds.split(",");
			sql = "SELECT aci.*,"
					+ "( SELECT group_concat( DISTINCT aco.order_no SEPARATOR '<br/>' ) "
					+ " FROM "
					+ " arap_cost_order aco LEFT JOIN cost_application_order_rel caor "
					+ " ON caor.cost_order_id = aco.id"
					+ " WHERE "
					+ " caor.application_order_id = aci.id ) cost_order_no,"
					+ " ( SELECT sum(caor.pay_amount) "
					+ " FROM "
					+ " arap_cost_order aco LEFT JOIN cost_application_order_rel caor "
					+ " ON caor.cost_order_id = aco.id"
					+ " WHERE caor.application_order_id = aci.id ) pay_amount,"
					+ "aco.create_stamp cost_stamp FROM arap_cost_invoice_application_order aci "
	        		+ " LEFT JOIN cost_application_order_rel cao on cao.application_order_id = aci.id "
	        		+ " LEFT JOIN arap_cost_order aco on aco.id = cao.cost_order_id "
	        		+ " where aci.id in(" + invoiceApplicationOrderIds + ") GROUP BY aci.id " ;
			record = Db.find(sql);
			//re = Db.findFirst("select count(")
//			Long spId = arapCostOrder.getLong("payee_id");
//			if (!"".equals(spId) && spId != null) {
//				Party party = Party.dao.findById(spId);
//				setAttr("party", party);
//				Contact contact = Contact.dao.findById(party.get("contact_id")
//						.toString());
//				setAttr("customer", contact);
//			}
			for (int i = 0; i < idArray.length; i++) {
				
			}
		}
        //sqlTotal = " select count(1) total from (" + sql + condition + ") as B"; 
        //Record rec = Db.findFirst(sqlTotal);
        //logger.debug("total records:" + rec.getLong("total"));
        
//        List<Record> BillingOrders = Db.find(sql + condition + " order by create_stamp desc" + sLimit);
//
        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", record.size());
        BillingOrderListMap.put("iTotalDisplayRecords", record.size());

        BillingOrderListMap.put("aaData", record);

        renderJson(BillingOrderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void costConfiremReturnOrder(){
    	String ids = getPara("ids");
    	String orderNos = getPara("orderNos");
    	String[] idArr = ids.split(",");
    	String[] orderNoArr = orderNos.split(",");
    	for(int i=0 ; i<idArr.length ; i++){
    		if("提货".equals(orderNoArr[i])){
    			DepartOrder pickupOrder = DepartOrder.dao.findById(idArr[i]);
    			pickupOrder.set("audit_status", "已确认");
    			pickupOrder.update();
    		}else if("零担".equals(orderNoArr[i]) || "整车".equals(orderNoArr[i])){
    			DepartOrder departOrder = DepartOrder.dao.findById(idArr[i]);
    			departOrder.set("audit_status", "已确认");
    			departOrder.update();
    		}else if("配送".equals(orderNoArr[i])){
    			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(idArr[i]);
    			deliveryOrder.set("audit_status", "已确认");
    			deliveryOrder.update();
    		}else if("成本单".equals(orderNoArr[i])){
    			ArapMiscCostOrder arapmisccostOrder = ArapMiscCostOrder.dao.findById(idArr[i]);
    			arapmisccostOrder.set("audit_status", "已确认");
    			arapmisccostOrder.update();
    		}else{
    			InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(idArr[i]);
    			insuranceOrder.set("audit_status", "已确认");
    			insuranceOrder.update();
    		}
    	}
        renderJson("{\"success\":true}");
    }
}
