package controllers.yh.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.DepartOrder;
import models.InsuranceOrder;
import models.Party;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import org.apache.commons.lang.StringUtils;
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
		if(ids != null && !"".equals(ids)){
			String[] idArray = ids.split(",");
			logger.debug(String.valueOf(idArray.length));
			String sql = "SELECT c.abbr,aci.bill_type,aci.billing_unit,aci.payee_unit,aci.payee_name,aci.bank_no,aci.bank_name FROM `arap_cost_invoice_application_order` aci"
					+ " LEFT JOIN cost_application_order_rel cao on cao.application_order_id = aci.id"
					+ " LEFT JOIN arap_cost_order aco on aco.id = cao.cost_order_id "
					+ " LEFT JOIN party p ON p.id = aci.payee_id "
					+ " LEFT JOIN office o ON o.id = p.office_id "
					+ " LEFT JOIN contact c ON c.id = p.contact_id"
					+ " where aci.id = '"+idArray[0]+"'";
			Record record = Db.findFirst(sql);
			setAttr("invoiceApplicationOrder", record);
			
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
		}
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
	
	public void searchAllAccount(){
		List<Account> accounts = Account.dao.find("select * from fin_account where type != 'REC' and bank_name != '现金'");
		renderJson(accounts);
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
    	String orderNo = getPara("orderNo")==null?null:getPara("orderNo").trim();
    	String appOrderNo = getPara("applicationOrderNo")==null?null:getPara("applicationOrderNo").trim();
		String status = getPara("status")==null?null:getPara("status").trim();
		String spName = getPara("sp_name")==null?null:getPara("sp_name").trim();
		String receiverName = getPara("receiverName")==null?null:getPara("receiverName").trim();
		String beginTime = getPara("beginTime")==null?null:getPara("beginTime").trim();
		String endTime = getPara("endTime")==null?null:getPara("endTime").trim();
		
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
        		+ fromSql;
        String orderBy= " order by cpco.create_date desc ";
        
        String conditions=" where 1=1 ";
        if (StringUtils.isNotEmpty(orderNo)){
        	conditions+=" and UPPER(cpco.order_no) like '%"+orderNo.toUpperCase()+"%'";
        }
//        if (appOrderNo != null){
//        	conditions+=" fksq_no like'%"+appOrderNo+"%'";
//        }
        if (StringUtils.isNotEmpty(status)){
        	conditions+=" and cpco.status = '"+status+"'";
        }
        if (StringUtils.isNotEmpty(spName)){
        	conditions+=" and c1.abbr like '%"+spName+"%'";
        }
        if (StringUtils.isNotEmpty(receiverName)){
        	conditions+=" and cpco.receive_person like '%"+receiverName+"%'";
        }
        
        if (StringUtils.isNotEmpty(beginTime)){
        	beginTime = " and cpco.create_date between'"+beginTime+"'";
        }else{
        	beginTime =" and cpco.create_date between '1970-1-1'";
        }
        
        if (StringUtils.isNotEmpty(endTime)){
        	endTime =" and '"+endTime+"'";
        }else{
        	endTime =" and '3000-1-1'";
        }
        conditions+=beginTime+endTime;
        
        
        totalSql+=conditions;
        Record recTotal = Db.findFirst(totalSql);
        Long total = recTotal.getLong("total");
        logger.debug("total records:" + total);
        
        columsSql+=conditions + orderBy + sLimit;
        List<Record> costPayConfirmOrders = Db.find(columsSql);

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
