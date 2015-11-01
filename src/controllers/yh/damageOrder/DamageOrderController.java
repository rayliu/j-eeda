package controllers.yh.damageOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import models.yh.carmanage.CarSummaryDetailOtherFee;
import models.yh.damageOrder.DamageOrder;
import models.yh.profile.Contact;

import org.apache.commons.lang.StringUtils;
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
import controllers.yh.util.DbUtils;
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
	
	public void create() {
		List<Record> paymentItemList = Collections.EMPTY_LIST;
		paymentItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("paymentItemList", paymentItemList);
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);
		
    	render("/yh/DamageOrder/DamageOrderEdit.html");
    }

    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "SELECT dao.*, c1.abbr customer_name, c2.abbr sp_name, "
    			+ " ifnull(u.c_name, u.user_name) creator_name from damage_order dao"
    			+"	left join party p1 on dao.customer_id = p1.id "
    			+"		left join contact c1 on p1.contact_id = c1.id"
    			+"	    left join party p2 on dao.sp_id = p2.id "
    			+"		left join contact c2 on p2.contact_id = c2.id "
    			+"      left join user_login u on u.id = dao.creator where 1 =1 ";
        logger.debug("sql:" + sql);
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql+ condition + " order by create_date desc " +sLimit);

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
    	
    	 Gson gson = new Gson();  
         Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
         
        ArapMiscCostOrder arapMiscCostOrder = new ArapMiscCostOrder();
		String id = (String) dto.get("id");
		String customer_id = (String) dto.get("customer_id");
		String sp_id = (String) dto.get("sp_id");
		String order_type = (String) dto.get("order_type");
		String biz_order_no = (String) dto.get("biz_order_no");
		String process_status = (String) dto.get("process_status");
		String accident_type = (String) dto.get("accident_type");
		String accident_desc = (String) dto.get("accident_desc");
		String accident_date = (String) dto.get("accident_date");
		String remark = (String) dto.get("remark");
		
		UserLogin user = LoginUserController.getLoginUser(this);
		
		if (StringUtils.isNotEmpty(id)) {
			//update
			DamageOrder order = DamageOrder.dao.findById(id);
			order.set("customer_id", customer_id);
			order.set("sp_id", sp_id);
			order.set("order_type", order_type);
			order.set("biz_order_no", biz_order_no);
			order.set("process_status", process_status);
			order.set("accident_type", accident_type);
			order.set("accident_desc", accident_desc);
			order.set("accident_date", accident_date);
			order.set("remark", remark);
			order.update();
		} else {
			//create
			DamageOrder order = new DamageOrder();
			order.set("order_no", OrderNoGenerator.getNextOrderNo("HSD"));
			order.set("customer_id", customer_id);
			order.set("sp_id", sp_id);
			order.set("order_type", order_type);
			order.set("biz_order_no", biz_order_no);
			order.set("process_status", process_status);
			order.set("accident_type", accident_type);
			order.set("accident_desc", accident_desc);
			order.set("accident_date", accident_date);
			order.set("remark", remark);
			
			order.set("creator", user.get("id"));
			order.set("office_id", user.get("office_id"));
			order.set("create_date", new Date());
			order.set("status", "新建");
			order.save();
			id = order.getInt("id").toString();
		}
		
		//return dto
		Record returnDto = getOrderDto(id);
		renderJson(returnDto);
	}

	private Record getOrderDto(String orderId) {
		Map order = new HashMap();
		String sql = "SELECT dao.*, c1.abbr customer_name, c2.abbr sp_name, "
			+ " ifnull(u.c_name, u.user_name) creator_name from damage_order dao"
			+"	left join party p1 on dao.customer_id = p1.id "
			+"		left join contact c1 on p1.contact_id = c1.id"
			+"	    left join party p2 on dao.sp_id = p2.id "
			+"		left join contact c2 on p2.contact_id = c2.id "
			+"      left join user_login u on u.id = dao.creator"
			+ " where dao.id=?";
		Record orderRec = Db.findFirst(sql, orderId);
		
		order.put("itemList", orderRec);
		
		return orderRec;
	}
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_UPDATE})
	public void edit() {
		String id = getPara("id");
		
		setAttr("order", getOrderDto(id));
		render("/yh/DamageOrder/DamageOrderEdit.html");
	}
	
}
