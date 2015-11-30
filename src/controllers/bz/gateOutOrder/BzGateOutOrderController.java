package controllers.bz.gateOutOrder;

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
import models.yh.damageOrder.DamageOrderItem;
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

import controllers.bz.gateOutOrder.models.BzGateOutOrder;
import controllers.bz.gateOutOrder.models.BzGateOutOrderItem;
import controllers.yh.LoginUserController;
import controllers.yh.util.DbUtils;
import controllers.yh.util.LocationUtil;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class BzGateOutOrderController extends Controller {
    private Logger logger = Logger.getLogger(BzGateOutOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	String orderListPage = "/bz/gateOutOrder/orderList.html";
	String orderEditPage = "/bz/gateOutOrder/orderEdit.html";
	
    public void index() {
    	render(orderListPage);
    }
	
	public void create() {
    	render(orderEditPage);
    }

    public void list() {
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }

        String sql = "select m.*,"
        		+ " (select group_concat(distinct d.product_no separator '<br>') from bz_gate_out_order_item d where order_id = m.id) product_no,"
        		+ " (select group_concat(distinct d.serial_no separator '<br>') from bz_gate_out_order_item d where order_id = m.id) serial_no,"
        		+ " (select group_concat(distinct d.remark separator '<br>') from bz_gate_out_order_item d where order_id = m.id) remark,"
        		+ " ifnull(u.c_name, u.user_name) creator_name from bz_gate_out_order m"
				+ " left join user_login u on u.id = m.creator where 1 =1 ";
        logger.debug("sql:" + sql);
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+") B where 1=1 "+ condition;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find("select * from ("+sql+") A where 1=1 "+ condition + " order by create_date desc " +sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    @Before(Tx.class)
	public void save() throws Exception {		
		String jsonStr=getPara("params");
    	
    	 Gson gson = new Gson();  
         Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
         
        ArapMiscCostOrder arapMiscCostOrder = new ArapMiscCostOrder();
		String id = (String) dto.get("id");
		String customer_name = (String) dto.get("customer_name");
		
		UserLogin user = LoginUserController.getLoginUser(this);
		
		if (StringUtils.isNotEmpty(id)) {
			//update
			BzGateOutOrder order = BzGateOutOrder.dao.findById(id);
			order.set("customer_name", customer_name);
			order.update();
		} else {
			//create
			BzGateOutOrder order = new BzGateOutOrder();
			order.set("order_no", OrderNoGenerator.getNextOrderNo("BZCH"));
			order.set("customer_name", customer_name);
			
			order.set("creator", user.get("id"));
			order.set("create_date", new Date());
			order.save();
			id = order.getLong("id").toString();
		}
		//处理从表
		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, BzGateOutOrderItem.class);
		//return dto
		Record returnDto = getOrderDto(id);
		renderJson(returnDto);
	}

	private Record getOrderDto(String orderId) {
		Map order = new HashMap();
		String sql = "select m.*,'' product_no, '' serial_no,"
				+ " ifnull(u.c_name, u.user_name) creator_name from bz_gate_out_order m"
				+ " left join user_login u on u.id = m.creator where 1 =1 and m.id=?";
		Record orderRec = Db.findFirst(sql, orderId);
		
		String itemSql = "select * from bz_gate_out_order_item where order_id=?";
		List<Record> itemList = Db.find(itemSql, orderId);
		orderRec.set("item_list", itemList);
		
		
		return orderRec;
	}
    
	public void edit() {
		String id = getPara("id");
		
		setAttr("order", getOrderDto(id));
		render(this.orderEditPage);
	}
	
	public void cancel() {
		String id = getPara("id");
		
		if (StringUtils.isNotEmpty(id)) {
			//update
			BzGateOutOrder order = BzGateOutOrder.dao.findById(id);
			order.set("status", "已取消");
			order.update();
		}
		renderText("ok");
	}
	
}
