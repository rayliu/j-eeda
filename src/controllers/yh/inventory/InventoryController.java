package controllers.yh.inventory;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.InventoryItem;
import models.Office;
import models.ParentOfficeModel;
import models.Party;
import models.Product;
import models.TransferOrder;
import models.TransferOrderItem;
import models.UserCustomer;
import models.UserLogin;
import models.Warehouse;
import models.WarehouseOrder;
import models.WarehouseOrderItem;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class InventoryController extends Controller {

    private Logger logger = Logger.getLogger(InventoryController.class);
    Subject currentUser = SecurityUtils.getSubject();    
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INLIST})
    public void index() {
        setAttr("inventory", "gateIn");
        render("/yh/inventory/inventoryList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_II_LIST})
    public void stockIndex(){
    	/*setAttr("disabledValue","");*/
        render("/yh/inventory/stock.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTLIST})
    public void outIndex(){
    	 setAttr("inventory", "gateOut");
         
         render("/yh/inventory/inventoryList.html");
    }

    // 入库单list
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INLIST})
    public void gateInlist() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from warehouse_order";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                + "left join party p on p.id =w_o.party_id " + "left join contact c on p.contact_id =c.id "
                + "left join warehouse w on w.id = w_o.warehouse_id where order_type='入库'");
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // 出库单list
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTLIST})
    public void gateOutlist() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from warehouse_order";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                + "left join party p on p.id =w_o.party_id " + "left join contact c on p.contact_id =c.id "
                + "left join warehouse w on w.id = w_o.warehouse_id where order_type='出库'");
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // 库存list
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_II_LIST})
    public void stocklist() {
        String customerId = getPara("customerId");
        String warehouseId = getPara("warehouseId");
        String officeId = getPara("officeId");
        String itemId = getPara("itemId");
        String itemName = getPara("itemName");
        
        if ((customerId == null && warehouseId == null && officeId == null) || ( "".equals(customerId) && "".equals(warehouseId) && "".equals(officeId))) {
        	Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
        }else{
        	searchByCondition(customerId, warehouseId, officeId, itemId);
        }
    }
	private void searchByCondition(String customerId, String warehouseId,
			String officeId, String itemId) {
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " limit " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
       
       String warehouseCondition = ""; 
       String customerCondition = "";
       String warehousePredict = "";
       String warehouseLocal = "";
       String customerLocal = "";
       if("all".equalsIgnoreCase(officeId)){
    	   officeId = "";
       }
       if("all".equalsIgnoreCase(warehouseId)){
   			warehouseId = "";
   		}
       if(warehouseId != null && !"".equals(warehouseId) || "all".equalsIgnoreCase(warehouseId)){
    	   warehouseCondition = " and tor.warehouse_id = i_t.warehouse_id ";
    	   warehousePredict = " and t_o.warehouse_id=i_t.warehouse_id ";
    	   warehouseLocal = " and d_o.from_warehouse_id=i_t.warehouse_id";
       }
      
       if((customerId != null) && !"".equals(customerId)){
    	   customerCondition = " and tor.customer_id = i_t.party_id ";
    	   customerLocal = "and d_o.customer_id = i_t.party_id";
       }
        
       //获取当前用户的总公司
       ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
       
       Long parentID = pom.getParentOfficeId();
       
       
       String sql = " from inventory_item i_t "
					+" left join product p on  i_t.product_id = p.id "
					+" left join party p2 on i_t.party_id = p2.id  "
					+" left join contact c on p2.contact_id = c.id "
					+" left join warehouse w on  i_t.warehouse_id = w.id "
					+" left join office o on w.office_id = o.id "
					+" left join office p_o on p2.office_id = p_o.id "
					+" where 1=1 and (o.id = " + parentID + " or o.belong_office = " + parentID + ") and (p_o.id = " + parentID + " or p_o.belong_office = " + parentID + ") ";
				
        String sqlCondition = " select sum(i_t.total_quantity) as total_quantity,o.id as oid,w.id as wid,p2.id as party_id, c.company_name, p.item_name, p.item_no, p.unit, p.id as pid, w.warehouse_name,o.office_name,"
       			+ " (select count(1) FROM transfer_order_item_detail toid"
       			+ " LEFT JOIN transfer_order t_o on t_o.id = toid.order_id"
       			+ " LEFT JOIN transfer_order_item toi ON toi.id = toid.item_id"
       			+ " LEFT JOIN depart_order d_o on d_o.id = toid.depart_id"
       			+ " where d_o.status='已发车'  and toi.product_id = i_t.product_id"  + warehousePredict + ") predict_amount, "
    		   	+" (select count(1) "
    		   	+ " FROM transfer_order_item_detail toid "
    		   	+ " LEFT JOIN transfer_order_item toi ON toi.id = toid.item_id "
    		   	+ " LEFT JOIN delivery_order d_o ON  d_o.id = toid.delivery_id  "
    		   	+ " where d_o.status='新建'" + warehouseLocal + " and toi.product_id = i_t.product_id and d_o.customer_id = i_t.party_id " + customerLocal + ") lock_amount,"
				+" (select count(toid.id) as valid_amount "
				+ " FROM transfer_order_item_detail toid "
				+ " LEFT JOIN transfer_order tor ON toid.order_id = tor.id"
				+ " LEFT JOIN transfer_order_item toi ON toi.id = toid.item_id  "
				+ " where toi.product_id = i_t.product_id  and tor.customer_id = i_t.party_id " + customerCondition + " and toid.delivery_id is null and toid.depart_id is not null  and toid.status ='已入库' " + warehouseCondition + ") valid_amount ";
      
        String groupCondition = " group by p.item_name, p.item_no, p.unit";
        
	    if((customerId != null) && !"".equals(customerId)){
	    	sql = sql + " and i_t.party_id =" + customerId ;
	    	
	    	groupCondition = groupCondition + ",c.company_name";
	    }
        if(warehouseId != null && !"".equals(warehouseId)){
        	
        	sql = sql + " and i_t.warehouse_id =" + warehouseId ;
        	
        	groupCondition = groupCondition + ",w.warehouse_name";
        }
        
        if((officeId != null) && !"".equals(officeId)){
        	sql = sql + " and w.office_id =" + officeId ;
        	groupCondition = groupCondition + ",o.office_name";
        }
        
        if(itemId != null && !"".equals(itemId)){
        	sql = sql + " and i_t.product_id =" + itemId;
        	
        }
        
        String sqlTotal = "select count(1) total " + sql +  groupCondition;// 获取总条数
        
        //String totalAmountSql = "select sum(i_t.total_quantity) total" + sql ;
        sql = sqlCondition + sql + groupCondition + sLimit;
       
        List<Record> rec = Db.find(sqlTotal);
        // 获取当前页的数据
        List<Record> orders = Db.find(sql);
        /*Record amoutRec = Db.findFirst(totalAmountSql);
        setAttr("totalAmount", amoutRec.get("total"));*/
        Map orderMap = new HashMap();
    	orderMap.put("iTotalRecords", rec.size());
        orderMap.put("iTotalDisplayRecords", rec.size());
       
        orderMap.put("sEcho", pageIndex);
        orderMap.put("aaData", orders);
        renderJson(orderMap);
	}
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_II_LIST})
    public void getTotalAmount() {
        String customerId = getPara("customerId");
        String warehouseId = getPara("warehouseId");
        String officeId = getPara("officeId");
        String itemId = getPara("itemId");
        if("all".equalsIgnoreCase(officeId)){
     	   officeId = "";
        }
        if("all".equalsIgnoreCase(warehouseId)){
    			warehouseId = "";
    		}
       //获取当前用户的总公司
       ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
       
       Long parentID = pom.getParentOfficeId();
       
       
       String sql = " from inventory_item i_t "
					+" left join product p on  i_t.product_id = p.id "
					+" left join party p2 on i_t.party_id = p2.id  "
					+" left join contact c on p2.contact_id = c.id "
					+" left join warehouse w on  i_t.warehouse_id = w.id "
					+" left join office o on w.office_id = o.id "
					+" left join office p_o on p2.office_id = p_o.id "
					+" where 1=1 and (o.id = " + parentID + " or o.belong_office = " + parentID + ") and (p_o.id = " + parentID + " or p_o.belong_office = " + parentID + ") ";
				
        
	    if((customerId != null) && !"".equals(customerId)){
	    	sql = sql + " and i_t.party_id =" + customerId ;
	    	
	    }
        if(warehouseId != null && !"".equals(warehouseId)){
        	
        	sql = sql + " and i_t.warehouse_id =" + warehouseId ;
        }
        
        if((officeId != null) && !"".equals(officeId)){
        	sql = sql + " and w.office_id =" + officeId ;
        }
        
        if(itemId != null && !"".equals(itemId)){
        	sql = sql + " and i_t.product_id =" + itemId;
        	
        }
        
        String totalAmountSql = "select sum(i_t.total_quantity) total" + sql ;
        Record amoutRec = Db.findFirst(totalAmountSql);
        if(amoutRec.get("total") == null || "" .equals(amoutRec.get("total"))){
        	renderJson(0);
        }else{
        	renderJson(amoutRec.get("total"));
        }
        	
        
    }
    // 入库单添加
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INCREATE})
    public void gateIn_add() {
       render("/yh/inventory/gateInEdit.html");
    }
    
    // 入库单产品删除
    public void gateInProductDelect() {
        String id = getPara();
        if (id != null) {
            WarehouseOrderItem.dao.deleteById(id);
        }
        renderJson("{\"success\":true}");
    }
   
    // 入库单产品编辑
    public void gateInProductEdit() {
        String id = getPara();
        System.out.println(id);
        WarehouseOrderItem orders = WarehouseOrderItem.dao.findFirst("select * from warehouse_order_item where id='"
                + id + "'");
        renderJson(orders);
    }

    // 出库单添加
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTCREATE})
    public void gateOut_add() {
            render("/yh/inventory/gateOutEdit.html");
    }

    // 入库单修改edit
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INUPDATE})
    public void gateInEdit() {
        String id = getPara("id");
        System.out.println(id);
        WarehouseOrder orders = WarehouseOrder.dao
                .findFirst("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                        + "left join party p on p.id =w_o.party_id " + "left join contact c on p.contact_id =c.id "
                        + "left join warehouse w on w.id = w_o.warehouse_id where w_o.id='" + id + "'");
        setAttr("warehouseOrder", orders);
            render("/yh/inventory/gateInEdit.html");
    }

    // 出库单修改edit
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTUPDATE})
    public void gateOutEdit() {
        String id = getPara("id");
        WarehouseOrder orders = WarehouseOrder.dao
                .findFirst("select w_o.*,c.company_name,w.warehouse_name from warehouse_order w_o "
                        + "left join party p on p.id =w_o.party_id " + "left join contact c on p.contact_id =c.id "
                        + "left join warehouse w on w.id = w_o.warehouse_id where w_o.id='" + id + "'");
        setAttr("warehouseOrder", orders);
            render("/yh/inventory/gateOutEdit.html");
    }

    // 查找客户
    public void searchCustomer() {
    	 ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    	Long parentID = pom.getParentOfficeId();
        String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select distinct p.*,c.*,p.id as pid from party p,contact c,office o where o.id = p.office_id and (o.id = " + parentID + " or o.belong_office = " + parentID + ") and p.contact_id = c.id and p.party_type = 'CUSTOMER' and (p.is_stop is null or p.is_stop = 0) and (company_name like '%"
                            + input
                            + "%' or contact_person like '%"
                            + input
                            + "%' or email like '%"
                            + input
                            + "%' or mobile like '%"
                            + input
                            + "%' or phone like '%"
                            + input
                            + "%' or address like '%"
                            + input + "%' or postal_code like '%" + input + "%')  limit 0,10");
        } else {
            locationList = Db
                    .find("select distinct p.*,c.*,p.id as pid from party p,contact c,office o where o.id = p.office_id and (o.id = " + parentID + " or o.belong_office = " + parentID + ") and  p.contact_id = c.id and p.party_type = '"
                            + Party.PARTY_TYPE_CUSTOMER + "' and (p.is_stop is null or p.is_stop = 0)");
        }
        renderJson(locationList);
    }

    // 入库产品list
    public void gateInProductlist() {
        String warehouseorderid = getPara();
        System.out.println(warehouseorderid);
        if (warehouseorderid == null) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        Map productListMap = null;
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String category = getPara("category");
        String sqlTotal = "select count(1) total from warehouse_order_item where warehouse_order_id = "
                + warehouseorderid;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from warehouse_order_item where warehouse_order_id = " + warehouseorderid;
        List<Record> products = Db.find(sql);
        productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
        productListMap.put("aaData", products);

        renderJson(productListMap);
    }

    // 出库产品list
    public void gateInProductlist2() {
        String warehouseorderid = getPara();
        System.out.println(warehouseorderid);
        if (warehouseorderid == null) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        Map productListMap = null;
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String category = getPara("category");
        String sqlTotal = "select count(1) total from warehouse_order_item where warehouse_order_id = "
                + warehouseorderid;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from warehouse_order_item where warehouse_order_id = " + warehouseorderid;
        List<Record> products = Db.find(sql);
        productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
        productListMap.put("aaData", products);

        renderJson(productListMap);
    }

    // 保存入库单
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INCREATE,PermissionConstant.PERMISSION_WO_INUPDATE},logical=Logical.OR)
    public void gateInSave() {
        
        String orderNo = OrderNoGenerator.getNextOrderNo("RK");
        
        WarehouseOrder warehouseOrder = new WarehouseOrder();
        String gateInId = getPara("warehouseorderId");
        System.out.println();
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        warehouseOrder.set("party_id", getPara("party_id")).set("warehouse_id", getPara("warehouseId"))
                .set("order_type", "入库").set("status", "新建").set("qualifier", getPara("qualifier"))
                .set("remark", getPara("remark"));

        if (gateInId != "") {
            warehouseOrder.set("id", gateInId).set("last_updater", users.get(0).get("id"))
                    .set("last_update_date", createDate);
            warehouseOrder.update();
        } else {
            warehouseOrder.set("creator", users.get(0).get("id")).set("create_date", createDate)
                    .set("order_no", orderNo);
            warehouseOrder.save();
        }
        renderJson(warehouseOrder.get("id"));
    }

    // 保存出库单
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTCREATE,PermissionConstant.PERMISSION_WO_OUTUPDATE},logical=Logical.OR)
    public void gateOutSave() {
        String orderNo = OrderNoGenerator.getNextOrderNo("CK");
        
        WarehouseOrder warehouseOrder = new WarehouseOrder();
        String gateOutId = getPara("warehouseorderId");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        warehouseOrder.set("party_id", getPara("party_id")).set("warehouse_id", getPara("warehouseId"))
                .set("order_no", orderNo).set("order_type", "出库").set("status", "新建")
                .set("qualifier", getPara("qualifier")).set("remark", getPara("remark"));

        if (gateOutId != "") {
            warehouseOrder.set("id", gateOutId).set("last_updater", users.get(0).get("id"))
                    .set("last_update_date", createDate);
            warehouseOrder.update();
        } else {
            warehouseOrder.set("creator", users.get(0).get("id")).set("create_date", createDate);
            warehouseOrder.save();
        }
        renderJson(warehouseOrder.get("id"));
    }

    // 保存入库单货品
    public void savewareOrderItem() {
        String warehouseorderid = getPara();
        WarehouseOrderItem warehouseOrderItem = null;
        String productId = getPara("productId");
        String warehouseOrderItemId = getPara("warehouseOderItemId");
        System.out.println(productId);
        /*
         * if (productId.equals("")) { renderJson(0); return; }
         */
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        if (warehouseOrderItemId != "") {
            warehouseOrderItem = WarehouseOrderItem.dao.findById(warehouseOrderItemId);
            setwarehouseItem(warehouseOrderItem);
            warehouseOrderItem.set("warehouse_order_id", warehouseorderid);
            warehouseOrderItem.set("last_updater", users.get(0).get("id")).set("last_update_date", createDate);
            warehouseOrderItem.update();
        } else {
            warehouseOrderItem = new WarehouseOrderItem();
            setwarehouseItem(warehouseOrderItem);
            warehouseOrderItem.set("warehouse_order_id", warehouseorderid);
            warehouseOrderItem.set("creator", users.get(0).get("id")).set("create_date", createDate);
            warehouseOrderItem.save();
        }
        renderJson(warehouseOrderItem.get("id"));
    }

    public void setwarehouseItem(WarehouseOrderItem warehouseOrderItem) {
        warehouseOrderItem.set("product_id", getPara("productId"))
                .set("item_name", getPara("item_name"))
                .set("item_no", getPara("itemNoMessage"))
                // .set("expire_date", getPara("expire_date"))
                .set("lot_no", getPara("lot_no")).set("uom", getPara("uom")).set("caton_no", getPara("caton_no"))
                .set("total_quantity", getPara("total_quantity")).set("unit_price", getPara("unit_price"))
                .set("unit_cost", getPara("unit_cost")).set("item_desc", getPara("item_desc"));
    }

    // 查找序列号
    public void searchItemNo() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select * from product where category_id in (select id from category where customer_id = "
                            + customerId + ") and item_no like '%" + input + "%' limit 0,10");
        } else {
            locationList = Db
                    .find("select * from product where category_id in (select id from category where customer_id = "
                            + customerId + ")");
        }
        renderJson(locationList);
    }

    // 查找产品名
    public void searchItemName() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db
                    .find("select * from product where category_id in (select id from category where customer_id = "
                            + customerId + ") and item_name like '%" + input + "%' limit 0,10");
        } else {
            locationList = Db
                    .find("select * from product where category_id in (select id from category where customer_id = "
                            + customerId + ")");
        }
        renderJson(locationList);
    }

    // gateOut查找序列号2
    public void searchNo2() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        String warehouseId = getPara("warehouseId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db.find("select * from inventory_item i " + "left join product p on p.id = i.product_id "
                    + "where i.party_id='" + customerId + "' and i.warehouse_id ='" + warehouseId
                    + "' p.item_name like '%" + input + "%' limit 0,10");
        } else {
            locationList = Db.find("select * from inventory_item i " + "left join product p on p.id = i.product_id "
                    + "where i.party_id='" + customerId + "' and i.warehouse_id ='" + warehouseId + "' ");
        }
        renderJson(locationList);
    }

    // gateOut查找产品名2
    public void searchName2() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        String warehouseId = getPara("warehouseId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db.find("select * from inventory_item i " + "left join product p on p.id = i.product_id "
                    + "where i.party_id='" + customerId + "' and i.warehouse_id ='" + warehouseId
                    + "' p.item_name like '%" + input + "%' limit 0,10");
        } else {
            locationList = Db.find("select * from inventory_item i " + "left join product p on p.id = i.product_id "
                    + "where i.party_id='" + customerId + "' and i.warehouse_id ='" + warehouseId + "' ");
        }
        renderJson(locationList);
    }

    // 查找仓库
    public void searchAllwarehouse() {
    	String inputStr = getPara("warehouseName");
    	String sql ="";
    	
    	 ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    	Long parentID = pom.getParentOfficeId();
    	if(inputStr!=null){
    		sql = "select w.* from warehouse w left join office o on o.id = w.office_id where w.warehouse_name like '%"+inputStr+"%' and (o.id = " + parentID + " or o.belong_office = " + parentID +")";
    	}else{
    		sql= "select w.* from warehouse w left join office o on o.id = w.office_id where (o.id = " + parentID + " or o.belong_office = " + parentID +")";
    	}
        List<Warehouse> warehouses = Warehouse.dao.find(sql);
        renderJson(warehouses);
    }

    // 查找客户
    public void searchgateOutCustomer() {
    	
    	
        String input = getPara("input");
        String warehouseId = getPara("warehouseId");
        if (warehouseId.equals("")) {
        	renderJson();
            //return;
        }
        List<Record> locationList = Collections.EMPTY_LIST;
        /*locationList = Db.find("select w_o.party_id as pid,c.company_name from `warehouse_order` w_o "
                + "left join party p on p.id = w_o.party_id "
                + "left join contact c on p.contact_id = c.id where w_o.warehouse_id ='" + warehouseId*/
        locationList = Db.find("select distinct invi.party_id as pid,c.company_name from inventory_item invi "
        		+ "left join party p on invi.party_id = p.id "
        		+ "left join contact c on c.id = p.contact_id  "
        		+ "where invi.party_id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
                + " and c.company_name like '%" + input + "%' group by c.company_name");
        renderJson(locationList);
    }

    // 入仓确认
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_INCOMPLETED})
    public void gateInConfirm() {
        String id = getPara();

        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        WarehouseOrder warehouseOrder = WarehouseOrder.dao.findById(id);

        List<Record> list = Db.find("select * from warehouse_order_item where warehouse_order_id = '" + id + "'");
        // 获取已入库的库存
        List<Record> inverntory = Db
                .find("select * from inventory_item where product_id in(select product_id from warehouse_order_item where warehouse_order_id = '"
                        + id + "' and warehouse_id ='" + warehouseOrder.get("warehouse_id") + "')");

        // 入库库存添加
        for (int i = 0; i < inverntory.size(); i++) {
            if (inverntory.size() > 0) {
                if (list.get(i).get("product_id").equals(inverntory.get(i).get("product_id"))
                        && inverntory.get(i).get("warehouse_id").equals(warehouseOrder.get("warehouse_id"))) {
                    InventoryItem inventoryItem = InventoryItem.dao.findById(inverntory.get(i).get("id"));
                    inventoryItem.set(
                            "total_quantity",
                            Double.parseDouble(inverntory.get(i).get("total_quantity").toString())
                                    + Double.parseDouble(list.get(i).get("total_quantity").toString()));
                    inventoryItem.update();
                }
            }
        }
        // 判断过滤重复的货品，已存在不添加只加数量
        List<Record> list2 = Db.find("select warehouse_id,product_id from warehouse_order_item w "
                + "left join warehouse_order w2 on w.warehouse_order_id = w2.id " + "where w.warehouse_order_id ='"
                + id + "'");
        List<Record> inverntory2 = Db
                .find("select warehouse_id,product_id from inventory_item where product_id in(select product_id from warehouse_order_item w left join warehouse_order w2 on w.warehouse_order_id = w2.id where w.warehouse_order_id ='"
                        + id + "')");
        list2.removeAll(inverntory2);

        for (int i = 0; i < list2.size(); i++) {
            List<Record> list3 = Db.find("select w2.warehouse_id,w.* from warehouse_order_item w "
                    + "left join warehouse_order w2 on w.warehouse_order_id = w2.id " + "where w.warehouse_order_id ='"
                    + id + "' and product_id ='" + list2.get(i).get("product_id") + "'");

            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem
                    .set("party_id", warehouseOrder.get("party_id"))
                    .set("warehouse_id", warehouseOrder.get("warehouse_id"))
                    .set("product_id", list3.get(0).get("product_id"))
                    // .set("item_name", list3.get(0).get("item_name"))
                    // .set("item_no", list3.get(0).get("item_no"))
                    .set("uom", list3.get(0).get("uom")).set("lot_no", list3.get(0).get("lot_no"))
                    .set("total_quantity", list3.get(0).get("total_quantity"))
                    .set("unit_price", list3.get(0).get("unit_price")).set("unit_cost", list3.get(0).get("unit_cost"))
                    .set("caton_no", list3.get(0).get("caton_no")).set("creator", users.get(0).get("id"))
                    .set("create_date", createDate);
            inventoryItem.save();

        }
        warehouseOrder.set("status", "已入库");
        warehouseOrder.update();
        renderJson("{\"success\":true}");
    }

    // 出仓确认
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_WO_OUTCOMPLETED})
    public void gateOutConfirm() {
        String id = getPara();

        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        Date createDate = Calendar.getInstance().getTime();

        // 获取从表的货品数据
        List<Record> warehouseItem = Db.find("select * from warehouse_order_item where warehouse_order_id = '" + id
                + "'");

        // 获取已入库的库存
        List<Record> inventory = Db
                .find("select * from inventory_item where product_id in(select product_id from warehouse_order_item where warehouse_order_id = '"
                        + id + "')");
        // 出库后更新数据
        for (int i = 0; i < warehouseItem.size(); i++) {
            InventoryItem inventoryItem = InventoryItem.dao.findById(inventory.get(i).get("id"));
            inventoryItem.set("total_quantity", Double.parseDouble(inventory.get(i).get("total_quantity").toString())
                    - Double.parseDouble(warehouseItem.get(i).get("total_quantity").toString()));
            inventoryItem.update();
        }
        // 删除库存为0的数据
        List<Record> list = Db
                .find("select id,total_quantity from inventory_item where product_id in(select product_id from warehouse_order_item where warehouse_order_id = '"
                        + id + "')");
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (Double.parseDouble(list.get(i).get("total_quantity").toString()) <= 0) {
                    InventoryItem.dao.deleteById(list.get(i).get("id"));
                }
            }
        }

        // 出库单完成出库
        WarehouseOrder warehouseOrder = WarehouseOrder.dao.findById(id);
        warehouseOrder.set("status", "已出库");
        warehouseOrder.update();

        // 生成运输单
        String orderType = getPara("orderType");
        if (orderType.equals("gateOutTransferOrder")) {
            creatTransferOrder(id, users, createDate, warehouseItem, inventory);
        } else {
            renderJson("{\"success\":true}");
        }

    }

    // 生成运输单
    public void creatTransferOrder(String id, List<UserLogin> users, Date createDate, List<Record> warehouseItem,
            List<Record> inventory) {
        if (inventory.size() > 0) {
            String orderNo = OrderNoGenerator.getNextOrderNo("YS");
            
            TransferOrder transferOrder = new TransferOrder();
            Party party = Party.dao
                    .findFirst(" select c.location from party p,contact c where p.contact_id =c.id and p.id='"
                            + inventory.get(0).get("party_id") + "'");

            transferOrder.set("order_no", orderNo);
            transferOrder.set("customer_id", inventory.get(0).get("party_id"));
            transferOrder.set("status", "新建");
            transferOrder.set("warehouse_id", inventory.get(0).get("warehouse_id"));
            transferOrder.set("route_from", party.get("location"));
            transferOrder.set("order_type", "gateOutTransferOrder");
            transferOrder.set("create_stamp", createDate);
            transferOrder.set("create_by", users.get(0).get("id"));
            transferOrder.save();

            for (int i = 0; i < inventory.size(); i++) {
                Product product = Product.dao.findById(inventory.get(i).get("product_id"));
                if (product != null) {
                    TransferOrderItem tItem = new TransferOrderItem();
                    tItem.set("item_no", product.get("item_no"));
                    tItem.set("item_name", product.get("item_name"));
                    tItem.set("size", product.get("size"));
                    tItem.set("width", product.get("width"));
                    tItem.set("height", product.get("height"));
                    tItem.set("volume", product.get("volume"));
                    tItem.set("weight", product.get("weight"));
                    tItem.set("amount", warehouseItem.get(0).get("total_quantity"));
                    tItem.set("unit", product.get("unit"));
                    tItem.set("product_id", inventory.get(i).get("product_id"));
                    tItem.set("order_id", transferOrder.get("id"));
                    tItem.save();
                }
            }
            renderJson(transferOrder.get("id"));
        }
    }

    // 生成运输单

    // 选中客户验证是否有产品
    public void confirmproduct() {
        String id = getPara();
        List<Record> list = Db
                .find("select  * from product p left join category c on p.category_id =c.id where c.customer_id ='"
                        + id + "'");
        if (list.size() > 0) {
            renderJson("{\"success\":true}");
        } else {
            renderJson("{\"success\":false}");
        }
    }
    //添加产品计量单位
    public void alluom(){
    	List<Record> locationList = Collections.EMPTY_LIST;
    	locationList = Db
                .find("select u.name from unit u");
    	renderJson(locationList);
    }
    // 查找所有网点
    public void searchAllOffice() {
    	String officeName = getPara("officeName");
    	String sql ="";
    	 ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    	Long parentID = pom.getParentOfficeId();
    	if("所有网点".equalsIgnoreCase(officeName)){
    		officeName = "";
    	}
    	if(officeName != null && !"".equals(officeName)){
    		sql = "select * from office where office_name like '%"+officeName+"%'and (id = " + parentID + " or belong_office = " + parentID +")";
    	}else{
    		sql = "select * from office where (id = " + parentID + " or belong_office = " + parentID +")";
    	}
        List<Office> office = Office.dao.find(sql);
        renderJson(office);
    }
    public void searchOfficeByPermission() {
    	String officeName = getPara("officeName");
    	String sql ="";
    	if(officeName != null && !"".equals(officeName)){
    		sql = "select * from office where office_name like '%"+officeName+"%' and id in where o.id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')";
    	}else{
    		sql = "select * from office where id in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')";
    	}
        List<Office> office = Office.dao.find(sql);
        renderJson(office);
    }
    // 按网点查找仓库
    public void findWarehouseById() {
    	//获取当前用户的总公司
    	
    	 ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    	Long parentID = pom.getParentOfficeId();
    	
    	
    	String warehouseName = getPara("warehouseName");
    	String officeId = getPara("officeId");
    	String customerId = getPara("customerId");
    	/*判断当前的officeId 是否是ALL*/
    	if("all".equalsIgnoreCase(officeId)){
    		officeId = "";
    	}
    	/*判断当前文本是否是所有仓库*/
    	if("所有仓库".equalsIgnoreCase(warehouseName)){
    		warehouseName = "";
    	}
    	String sql ="";
    	
    	/*if(officeId != null && !"".equals(officeId)){
    		sql = "select * from warehouse where office_id = " + officeId;
    	}else if(customerId != null && !"".equals(customerId)){
    		sql = "select distinct w.* from inventory_item ii left join warehouse w on ii.warehouse_id = w.id left join office o on o.id = w.office_id left join party p on p.id =ii.party_id where (o.id = " + parentID + " or o.belong_office = " + parentID +") and p.id = "+customerId;
    	}
    	
    	*/
    	
    	/*else if(warehouseName != null && !"".equals(warehouseName)){
    		sql = "select w.* from warehouse w left join office o on o.id = w.office_id where w.warehouse_name like '%"+warehouseName+"%' and (o.id = " + parentID + " or o.belong_office = " + parentID +")";
    	}*/
    	if(officeId != null && !"".equals(officeId)){
    		sql = "select w.* from warehouse w where w.office_id = " + officeId;
    	}else{
    		sql = "select w.* from warehouse w left join office o on o.id = w.office_id where (o.id = " + parentID + " or o.belong_office = " + parentID +") AND o.id IN (SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"') ";
    	}
    	
    	if(warehouseName != null && !"".equals(warehouseName)){
    		sql = sql + " and w.warehouse_name like '%"+warehouseName+"%'";
    	}
        List<Warehouse> warehouses = Warehouse.dao.find(sql);
        renderJson(warehouses);
    }
    public void outUserQuery(){
    	String userName = currentUser.getPrincipal().toString();
    	
    	List<UserCustomer> list = UserCustomer.dao.find("select * from user_customer where user_name = ?",userName);
    	
    	if(list.size()==1){
    		Record record = Db.findFirst("select *,p.id as pid,c.id as cid from party p left join contact c on p.contact_id = c.id where p.id = ?",list.get(0).get("customer_id"));
    		setAttr("customer", record);
    	}
    	/*setAttr("disabledValue","disabled");*/
    	render("/yh/inventory/stock.html");
    	
    }
    public void searchItem(){
    	String customerId = getPara("customerId");
    	String itemNo = getPara("itemNo");
    	String sql = "select p.* from category c left join product p on p.category_id = c.id where p.item_no is not null and customer_id = " + customerId;
    	if(itemNo != null && !"".equals(itemNo)){
    		sql = sql + " and p.item_no like '%" + itemNo + "%'"; 
    	}
    	
    	
    	List<Product> list = Product.dao.find(sql); 
    	
    	renderJson(list);
    	
    }
}
