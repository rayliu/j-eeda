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
    	render("/yh/arap/CostConfirm/CostConfirmList.html");
    }
    
   	public void edit() {
   		String ids = getPara("id");
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

    // 付款确认单列表
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void list() {
    	String orderNo = getPara("orderNo");
    	String appOrderNo = getPara("applicationOrderNo");
		String status = getPara("status");
		String sp = getPara("sp");
		String officeName = getPara("receiverName");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		
    	String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
               
        String fromSql = " from arap_cost_pay_confirm_order cpco "
        			+ " left join party p1 on cpco.sp_id = p1.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join user_login ul on ul.id=cpco.creator";
        
        String totalSql = "select count(1) total" + fromSql;
        
        String columsSql = "select cpco.*, 'afdafa' fksq_no, c1.abbr sp_name,"
        		+ "ifnull(nullif(ul.c_name,''), ul.user_name) user_name "
        		+ fromSql +" order by cpco.create_date desc ";
        
        if (orderNo != null && status != null && appOrderNo != null){
        	String conditions="";
        	columsSql+=conditions;
        }
        
        Record recTotal = Db.findFirst(totalSql);
        Long total = recTotal.getLong("total");
        logger.debug("total records:" + total);
        
        List<Record> costPayConfirmOrders = Db.find(columsSql+sLimit);

        Map orderListMap = new HashMap();
        orderListMap.put("sEcho", pageIndex);
        orderListMap.put("iTotalRecords", total);
        orderListMap.put("iTotalDisplayRecords", costPayConfirmOrders.size());

        orderListMap.put("aaData", costPayConfirmOrders);

        renderJson(orderListMap);
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
