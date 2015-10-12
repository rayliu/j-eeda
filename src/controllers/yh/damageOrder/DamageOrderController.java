package controllers.yh.damageOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;
import models.DepartOrder;
import models.Location;
import models.ParentOfficeModel;
import models.Party;
import models.UserLogin;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ArapMiscCostOrderDTO;
import models.yh.arap.ArapMiscCostOrderItem;
import models.yh.profile.Contact;

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
import controllers.yh.util.LocationUtil;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class DamageOrderController extends Controller {
    private Logger logger = Logger.getLogger(DamageOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_LIST})
    public void index() {
		List<Record> offices = Db.find("select o.id,o.office_name,o.is_stop from office o where o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')");
		setAttr("userOffices", offices);
    	render("/yh/DamageOrder/orderList.html");
    }

    public void list() {
    	String sp = getPara("sp");
        String customer = getPara("companyName");
        String spName = getPara("spName");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        
        String sql = "select amco.*, c1.abbr customer_name, c2.abbr sp_name from arap_misc_cost_order amco"
					+ " left join party p1 on amco.customer_id = p1.id"
					+ " left join party p2 on amco.sp_id = p2.id"
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id"
					+ " where amco.order_no not like 'SGSK%'";
        logger.debug("sql:" + sql);
        String condition = "";
        //TODO 始发地和目的地 客户没有做
        if(sp != null || customer != null || spName != null
        		|| status != null || beginTime != null || endTime != null|| orderNo != null){
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
			if (status!=null && "业务收款".equals(status)) {
				status = "biz";
			}
			if (status!=null && "非业务收款".equals(status)) {
				status = "non_biz";
			}
			condition = " and "
					+ " ifnull(c2.abbr,'') like '%" + spName + "%' "
					+ " and ifnull(c1.abbr,'') like '%" + customer + "%' "
					+ " and amco.order_no like '%" + orderNo + "%' "
					+ " and amco.create_stamp between '" + beginTime + "' and '" + endTime+ " 23:59:59' ";
			if(status!=null && status.length()>0){
				condition  += " and amco.type = '" + status + "' ";
			}
			
        }
        

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql+ condition + " order by amco.create_stamp desc " +sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
	public void getListForCreate() {
		String ids = getPara("ids");
		if(ids != null && !"".equals(ids)){
			String[] idArray = ids.split(",");
			logger.debug(String.valueOf(idArray.length));
	
			setAttr("costCheckOrderIds", ids); 
			ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(idArray[0]);
			Long spId = arapCostOrder.get("payee_id");
			if (!"".equals(spId) && spId != null) {
				Party party = Party.dao.findById(spId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get("contact_id").toString());
				setAttr("sp", contact); 
			}
		}

		setAttr("saveOK", false);
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));
		
		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("receivableItemList", receivableItemList);
		setAttr("status", "new");
		
		List<Record> itemList = Collections.emptyList();
		setAttr("itemList", itemList);
		
			render("/yh/arap/CostMiscOrder/CostMiscOrderEdit.html");
	}
    
    @Before(Tx.class)
	public void save() throws Exception {		
		String jsonStr=getPara("params");
    	logger.debug(jsonStr);
    	 Gson gson = new Gson();  
         Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
         
        ArapMiscCostOrder arapMiscCostOrder = new ArapMiscCostOrder();
		String costMiscOrderId = (String) dto.get("costMiscOrderId");
		String biz_type = (String) dto.get("biz_type");
		String customerId = (String) dto.get("customer_id");
		String spId = (String) dto.get("sp_id");
		String routeFrom = (String) dto.get("route_from");
		String routeTo = (String) dto.get("route_to");
		String others_name = (String) dto.get("others_name");
		String ref_no = (String) dto.get("ref_no");
		String remark = (String) dto.get("remark");
		Double amount = (Double) dto.get("amount");
		String cost_to_type = (String) dto.get("cost_to_type");
		UserLogin user = LoginUserController.getLoginUser(this);
		
		String old_biz_type = "";
		if (!"".equals(costMiscOrderId) && costMiscOrderId != null) {
			//update
		} else {
			//create
		}
		
		//return dto
		ArapMiscCostOrderDTO returnDto = getOrderDto(arapMiscCostOrder,
				costMiscOrderId);
		renderJson(returnDto);
	}

	private ArapMiscCostOrderDTO getOrderDto(
			ArapMiscCostOrder arapMiscCostOrder, String costMiscOrderId) {
		List<ArapMiscCostOrderItem> itemList = 
				ArapMiscCostOrderItem.dao.find("select amcoi.*, fi.name from arap_misc_cost_order_item amcoi, fin_item fi"
						+ " where amcoi.fin_item_id=fi.id and misc_order_id=?", costMiscOrderId);
		
		ArapMiscCostOrderDTO returnDto = new ArapMiscCostOrderDTO();
		returnDto.setOrder(arapMiscCostOrder);
		returnDto.setItemList(itemList);
		return returnDto;
	}
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_UPDATE})
	public void edit() {
		String id = getPara("id");
		
//		ArapMiscCostOrderDTO orderDto = getOrderDto(arapMiscCostOrder,
//				costMiscOrderId);
//		
//		setAttr("orderDto", orderDto);
		render("/yh/DamageOrder/DamageOrderEdit.html");
	}
	
}
