package controllers.yh.returnOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.DepartOrder;
import models.DepartTransferOrder;
import models.FinItem;
import models.InsuranceFinItem;
import models.InsuranceOrder;
import models.Location;
import models.OrderAttachmentFile;
import models.Party;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.contract.Contract;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;
import models.yh.returnOrder.ReturnOrderFinItem;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.yh.LoginUserController;
import controllers.yh.util.FileUtil;
import controllers.yh.util.LocationUtil;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;
import controllers.yh.util.getCustomFile;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ReturnOrderController extends Controller {
	private Logger logger = Logger.getLogger(ReturnOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RO_LIST})
	public void index() {
			render("/yh/returnOrder/returnOrderList.html");
	}

	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RO_LIST})
	public void list() {
		String order_no = getPara("order_no")==null?"":getPara("order_no").trim();
		String tr_order_no = getPara("tr_order_no")==null?"":getPara("tr_order_no").trim();
		String de_order_no = getPara("de_order_no")==null?"":getPara("de_order_no").trim();

		String status = getPara("status")==null?"":getPara("status").trim();
		String time_one = getPara("time_one")==null?"":getPara("time_one").trim();
		String time_two = getPara("time_two")==null?"":getPara("time_two").trim();
		String customer = getPara("customer")==null?"":getPara("customer").trim();
		String return_type = getPara("return_type")==null?"":getPara("return_type").trim();
		String transfer_type = getPara("transfer_type")==null?"":getPara("transfer_type").trim();
		String warehouse = getPara("warehouse")==null?"":getPara("warehouse").trim();
		String serial_no = getPara("serial_no")==null?"":getPara("serial_no").trim();
		String to_name = getPara("to_name")==null?"":getPara("to_name").trim();
		String province = getPara("province")==null?"":getPara("province").trim();
		String imgaudit = getPara("imgaudit")==null?"":getPara("imgaudit").trim();
		String photo_type = getPara("photo_type")==null?"":getPara("photo_type").trim();
		String sign_no = getPara("sign_no")==null?"":getPara("sign_no").trim();
		String officeSelect = getPara("officeSelect")==null?"":getPara("officeSelect").trim();
		String officeSelect2 = getPara("officeSelect2")==null?"":getPara("officeSelect2").trim();
		String delivery_date_begin_time = getPara("delivery_date_begin_time")==null?"":getPara("delivery_date_begin_time").trim();
		String delivery_date_end_time = getPara("delivery_date_end_time")==null?"":getPara("delivery_date_end_time").trim();
		String q_begin = getPara("q_begin")==null?"":getPara("q_begin").trim();
		String q_end = getPara("q_end")==null?"":getPara("q_end").trim();
		
		
		String pageIndex = getPara("sEcho");
		
		String sLimit = "";
		String sqlTotal = "";
		String sql = "";
		String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		Map orderMap = new HashMap();
		
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}
		

		String conditions=" where 1=1 ";
		
		if (StringUtils.isNotEmpty(imgaudit)){
        	conditions+=" and UPPER(imgaudit) like '%"+imgaudit+"%'";
        }
		if (StringUtils.isNotEmpty(photo_type)){
            conditions+=" and UPPER(photo_type) like '%"+photo_type+"%'";
        }
		if (StringUtils.isNotEmpty(tr_order_no)){
        	conditions+=" and UPPER(transfer_order_no) like '%"+tr_order_no+"%'";
        }
		if (StringUtils.isNotEmpty(de_order_no)){
        	conditions+=" and UPPER(delivery_order_no) like '%"+de_order_no+"%'";
        }
         
        if (StringUtils.isNotEmpty(sign_no)){
        	conditions+=" and UPPER(sign_no) like '%"+sign_no+"%'";
        }
        if (StringUtils.isNotEmpty(serial_no)){
        	conditions+=" and UPPER(serial_no) like '%"+serial_no+"%'";
        }
        
        if (StringUtils.isNotEmpty(return_type)){
        	conditions+=" and UPPER(return_type) like '%"+return_type+"%'";
        }
        if (StringUtils.isNotEmpty(warehouse)){
        	conditions+=" and UPPER(warehouse_name) like '%"+warehouse+"%'";
        }
        if (StringUtils.isNotEmpty(province)){
        	conditions+=" and UPPER(province) like '%"+province+"%'";
        }
        if (StringUtils.isNotEmpty(to_name)){
        	conditions+=" and UPPER(to_name) like '%"+to_name+"%'";
        }
        if (StringUtils.isNotEmpty(return_type)){
        	conditions+=" and UPPER(return_type) like '%"+return_type+"%'";
        }
        if (StringUtils.isNotEmpty(transfer_type)){
        	conditions+=" and UPPER(transfer_type) like '%"+transfer_type+"%'";
        }
        if(StringUtils.isNotEmpty(officeSelect)){
        	conditions+=" and dor_office_id = '" + officeSelect+"'";
        }
        if(StringUtils.isNotEmpty(officeSelect2)){
        	conditions+=" and tor_office_id = '" + officeSelect2+"'";
        }
        if (StringUtils.isNotEmpty(time_one)){
        	time_one = " and ifnull(planning_time,'2000-1-1') between'"+time_one+"'";
        }else{
        	time_one =" and ifnull(planning_time,'2000-1-1') between '2000-1-1'";
        }
        if (StringUtils.isNotEmpty(time_two)){
        	time_two =" and '"+time_two+"'";
        }else{
        	time_two =" and '2050-1-1'";
        }
        conditions += time_one + time_two;
        
        
        if(StringUtils.isNotEmpty(delivery_date_begin_time) || StringUtils.isNotEmpty(delivery_date_end_time)){
			if (!StringUtils.isNotEmpty(delivery_date_begin_time)){
				delivery_date_begin_time = "2000-01-01";
			}
			if (StringUtils.isNotEmpty(delivery_date_end_time)){
				delivery_date_end_time+= " 23:23:59";
			}else{
				delivery_date_end_time = "2037-12-31";
			}
			conditions += " and business_stamp between '"+ delivery_date_begin_time+ "' and '" + delivery_date_end_time + "' ";
		}
        
        if (StringUtils.isNotEmpty(q_begin)){
        	q_begin = " and receipt_date between'"+q_begin+"'";
        }else{
        	q_begin =" and receipt_date between '2000-1-1'";
        }
        if (StringUtils.isNotEmpty(q_end)){
        	q_end =" and '"+q_end+"'";
        }else{
        	q_end =" and '2050-1-1'";
        }
        if(!status.equals("'新建'"))
        	conditions += q_begin + q_end;
        

        		//+ " and (!(unix_timestamp(planning_time) < unix_timestamp('2015-07-01')) AND cname = '江苏国光') " ;
        String totalSql = " SELECT ror.id, ror.order_no,ror.customer_id,'' create_date,'' remark ,"
    		    + " ( CASE tor.arrival_mode  "
				+ " WHEN 'gateIn' THEN '配送' "
				+ " WHEN 'delivery' THEN '运输'"
				+ " WHEN 'deliveryToFactory' THEN '退货直送' "
				+ " WHEN 'deliveryToWarehouse' OR 'deliveryToFachtoryFromWarehouse' "
				+ " THEN '退货配送' ELSE '配送' END ) return_type,"
				+ " ( CASE tor.order_type "
				+ "  WHEN 'salesOrder' THEN '销售订单'"
				+ " WHEN 'replenishmentOrder' THEN '补货订单' "
				+ " WHEN 'arrangementOrder' THEN '调拨订单' "
				+ " WHEN 'cargoReturnOrder' THEN '退货订单'"
				+ " WHEN 'gateOutTransferOrder' THEN '出库运输单'"
				+ " WHEN 'movesOrder' THEN '移机单' ELSE '销售订单' END"
				+ " ) transfer_type,"
				+ " ifnull( cast(tor.planning_time as char),"
				+ " ( SELECT group_concat( DISTINCT CAST(tor.planning_time AS CHAR) SEPARATOR '<br/>' )"
				+ " FROM transfer_order tor"
				+ " LEFT JOIN delivery_order_item doi ON doi.transfer_order_id = tor.id"
				+ " WHERE doi.delivery_id = dor.id )"
				+ " ) planning_time,"
				+ " ( CASE"
				+ " WHEN ror.delivery_order_id IS NOT NULL "
				+ " THEN ( SELECT group_concat( DISTINCT toid.serial_no SEPARATOR '<br/>' )"
				+ " FROM transfer_order_item_detail toid"
				+ " WHERE toid.delivery_id = dor.id )"
				+ " ELSE ( SELECT group_concat( DISTINCT toid.serial_no SEPARATOR '<br/>' )"
				+ " FROM transfer_order_item_detail toid"
				+ " WHERE toid.order_id = tor.id ) END "
				+ " ) serial_no,"
				+ " ( CASE"
				+ " WHEN ror.delivery_order_id IS NOT NULL THEN"
				+ " ( SELECT group_concat( toid.item_no SEPARATOR '<br/>' )"
				+ " FROM transfer_order_item_detail toid"
				+ " WHERE toid.delivery_id = dor.id )"
				+ " ELSE ( SELECT group_concat( toi.item_no SEPARATOR '<br/>' )"
				+ " FROM transfer_order_item toi "
				+ " WHERE toi.order_id = ror.transfer_order_id ) END ) item_no,"
				+ " ifnull( c.contact_person, tor.receiving_name ) receipt_person,"
				+ " ifnull( c.phone, tor.receiving_phone ) receipt_phone,"
				+ " ifnull( (c.company_name), tor.receiving_unit ) receiving_unit,"
				+ " ifnull( c.address, tor.receiving_address ) receipt_address,"
				+ " ifnull( w.warehouse_name, w1.warehouse_name ) warehouse_name,"
				+ " ifnull(( SELECT sum(amount) FROM delivery_order_item doi "
				+ " WHERE doi.delivery_id = dor.id ), "
				+ " (SELECT sum(amount) FROM transfer_order_item "
				+ " WHERE order_id = tor.id) ) a_amount,  c2.abbr cname,"
				+ " ifnull( tor.order_no, "
				+ " ( SELECT group_concat( DISTINCT CAST(tor.order_no AS CHAR) SEPARATOR '<br/>' )"
				+ " FROM transfer_order tor"
				+ " LEFT JOIN delivery_order_item doi ON doi.transfer_order_id = tor.id"
				+ " WHERE doi.delivery_id = dor.id )"
				+ " ) transfer_order_no,"
				+ " lo. NAME from_name, lo2. NAME to_name,"
				+ " ifnull( ( SELECT NAME FROM location WHERE CODE = lo2.pcode AND pcode = 1 ),"
				+ " ( SELECT l. NAME FROM location l"
				+ " LEFT JOIN location lo3 ON lo3.pcode = l. CODE"
				+ "  WHERE lo3. CODE = lo2.pcode AND l.pcode = 1 )"
				+ " ) province,"
				+ " ifnull( tor.address, ( SELECT group_concat( DISTINCT CAST(tor.address AS CHAR) SEPARATOR '<br/>' )"
				+ " FROM transfer_order tor"
				+ " LEFT JOIN delivery_order_item doi ON doi.transfer_order_id = tor.id"
				+ " WHERE doi.delivery_id = dor.id )"
				+ " ) address, dor.order_no delivery_order_no, ifnull(ul.c_name, ul.user_name) creator_name,"
				+ " ror.receipt_date, ror.transaction_status, "
				+ "( SELECT CASE "
				+ " WHEN ( SELECT count(0) FROM order_attachment_file"
				+ "    WHERE order_type = 'RETURN' AND order_id = ror.id ) = 0 "
				+ "  THEN '无图片'"
				+ " WHEN ( SELECT count(0) FROM order_attachment_file"
				+ "    WHERE order_type = 'RETURN' AND order_id = ror.id AND (audit = 0 OR audit IS NULL) and file_path is not null ) > 0 "
				+ "  THEN '有图片待审核'"
				+ " ELSE '有图片已审核' END "
				+ ") imgaudit, "
				+ "( SELECT CASE "
                + " WHEN ( SELECT ("
                + "     SELECT count(0) FROM order_attachment_file  WHERE order_type = 'RETURN' AND order_id = ror.id and photo_type='回单签收')>0 "
                + " && "
                + " (SELECT count(0) FROM order_attachment_file  WHERE order_type = 'RETURN' AND order_id = ror.id and photo_type='现场安装')=0) "
                + "  THEN '回单签收'"
                + " WHEN ( SELECT ("
                + "     SELECT count(0) FROM order_attachment_file  WHERE order_type = 'RETURN' AND order_id = ror.id and photo_type='回单签收')=0 "
                + " && "
                + " (SELECT count(0) FROM order_attachment_file  WHERE order_type = 'RETURN' AND order_id = ror.id and photo_type='现场安装')>0) "
                + "  THEN '现场安装'"
                + " ELSE '' END "
                + ") photo_type, "

				+ " dor.ref_no sign_no,"
				+ " dor.office_id dor_office_id,"
				+ " tor.office_id tor_office_id,"
				+ " o.office_name dor_office_name,"
				+ " o2.office_name tor_office_name,"
				+ " dor.business_stamp,ror.office_id ror_office_id"
				+ " FROM return_order ror"
				+ " LEFT JOIN transfer_order tor ON tor.id = ror.transfer_order_id"
				+ " LEFT JOIN delivery_order dor ON dor.id = ror.delivery_order_id"
				+ " LEFT JOIN office o ON dor.office_id = o.id "
				+ " LEFT JOIN office o2 ON tor.office_id = o2.id "
				+ " LEFT JOIN party p ON p.id = dor.notify_party_id"
				+ " LEFT JOIN contact c ON c.id = p.contact_id"
				+ " LEFT JOIN warehouse w ON tor.warehouse_id = w.id"
				+ " LEFT JOIN warehouse w1 ON ifnull( dor.change_warehouse_id, dor.from_warehouse_id ) = w1.id"
				+ " LEFT JOIN party p2 ON p2.id = ror.customer_id"
				+ " LEFT JOIN contact c2 ON c2.id = p2.contact_id"
				+ " LEFT JOIN location lo ON lo. CODE = ifnull(tor.route_from, dor.route_from )"
				+ " LEFT JOIN location lo2 ON lo2. CODE = ifnull(tor.route_to, dor.route_to)"
				+ " LEFT JOIN user_login ul ON ul.id = ror.creator "
				+ " where "
				+ " ifnull(if(dor.isNullOrder = 'Y',dor.office_id,tor.office_id),(select DISTINCT tor.office_id from transfer_order tor"
	                + " LEFT JOIN delivery_order_item doi on doi.transfer_order_id = tor.id"
	                + " where doi.delivery_id = dor.id )) IN ( SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"' )"
				+ " and ror.customer_id in (select customer_id from user_customer where user_name='" + currentUser.getPrincipal() + "')" ;

			// 获取当前页的数据
	    sql = " SELECT ror.id, ror.order_no,ror.customer_id,'' create_date,'' remark ,"
	    		    + " ( CASE tor.arrival_mode  "
					+ " WHEN 'gateIn' THEN '配送' "
					+ " WHEN 'delivery' THEN '运输'"
					+ " WHEN 'deliveryToFactory' THEN '退货直送' "
					+ " WHEN 'deliveryToWarehouse' OR 'deliveryToFachtoryFromWarehouse' "
					+ " THEN '退货配送' ELSE '配送' END ) return_type,"
					+ " ( CASE tor.order_type "
					+ "  WHEN 'salesOrder' THEN '销售订单'"
					+ " WHEN 'replenishmentOrder' THEN '补货订单' "
					+ " WHEN 'arrangementOrder' THEN '调拨订单' "
					+ " WHEN 'cargoReturnOrder' THEN '退货订单'"
					+ " WHEN 'gateOutTransferOrder' THEN '出库运输单'"
					+ " WHEN 'movesOrder' THEN '移机单' ELSE '销售订单' END"
					+ " ) transfer_type,"
					+ " ifnull( cast(tor.planning_time as char),"
					+ " ( SELECT group_concat( DISTINCT CAST(tor.planning_time AS CHAR) SEPARATOR '<br/>' )"
					+ " FROM transfer_order tor"
					+ " LEFT JOIN delivery_order_item doi ON doi.transfer_order_id = tor.id"
					+ " WHERE doi.delivery_id = dor.id )"
					+ " ) planning_time,"
					+ " ( CASE"
					+ " WHEN ror.delivery_order_id IS NOT NULL "
					+ " THEN ( SELECT group_concat( DISTINCT toid.serial_no SEPARATOR '<br/>' )"
					+ " FROM transfer_order_item_detail toid"
					+ " WHERE toid.delivery_id = dor.id )"
					+ " ELSE ( SELECT group_concat( DISTINCT toid.serial_no SEPARATOR '<br/>' )"
					+ " FROM transfer_order_item_detail toid"
					+ " WHERE toid.order_id = tor.id ) END "
					+ " ) serial_no,"
					+ " ( CASE"
					+ " WHEN ror.delivery_order_id IS NOT NULL THEN"
					+ " ( SELECT group_concat( toid.item_no SEPARATOR '<br/>' )"
					+ " FROM transfer_order_item_detail toid"
					+ " WHERE toid.delivery_id = dor.id )"
					+ " ELSE ( SELECT group_concat( toi.item_no SEPARATOR '<br/>' )"
					+ " FROM transfer_order_item toi "
					+ " WHERE toi.order_id = ror.transfer_order_id ) END ) item_no,"
					+ " ifnull( c.contact_person, tor.receiving_name ) receipt_person,"
					+ " ifnull( c.phone, tor.receiving_phone ) receipt_phone,"
					+ " ifnull( (c.company_name), tor.receiving_unit ) receiving_unit,"
					+ " ifnull( c.address, tor.receiving_address ) receipt_address,"
					+ " ifnull( w.warehouse_name, w1.warehouse_name ) warehouse_name,"
					+ " ifnull(( SELECT sum(amount) FROM delivery_order_item doi "
					+ " WHERE doi.delivery_id = dor.id ), "
					+ " (SELECT sum(amount) FROM transfer_order_item "
					+ " WHERE order_id = tor.id) ) a_amount,  c2.abbr cname,"
					+ " ifnull( tor.order_no, "
					+ " ( SELECT group_concat( DISTINCT CAST(tor.order_no AS CHAR) SEPARATOR '<br/>' )"
					+ " FROM transfer_order tor"
					+ " LEFT JOIN delivery_order_item doi ON doi.transfer_order_id = tor.id"
					+ " WHERE doi.delivery_id = dor.id )"
					+ " ) transfer_order_no,"
					+ " lo. NAME from_name, lo2. NAME to_name,"
					+ " ifnull( ( SELECT NAME FROM location WHERE CODE = lo2.pcode AND pcode = 1 ),"
					+ " ( SELECT l. NAME FROM location l"
					+ " LEFT JOIN location lo3 ON lo3.pcode = l. CODE"
					+ "  WHERE lo3. CODE = lo2.pcode AND l.pcode = 1 )"
					+ " ) province,"
					+ " ifnull( tor.address, ( SELECT group_concat( DISTINCT CAST(tor.address AS CHAR) SEPARATOR '<br/>' )"
					+ " FROM transfer_order tor"
					+ " LEFT JOIN delivery_order_item doi ON doi.transfer_order_id = tor.id"
					+ " WHERE doi.delivery_id = dor.id )"
					+ " ) address, dor.order_no delivery_order_no, ifnull(ul.c_name, ul.user_name) creator_name,"
					+ " ror.receipt_date, ror.transaction_status, ( SELECT CASE WHEN ( SELECT count(0) FROM order_attachment_file"
					+ " WHERE order_type = 'RETURN' AND order_id = ror.id ) = 0 THEN '无图片'"
					+ " WHEN ( SELECT count(0) FROM order_attachment_file"
					+ " WHERE order_type = 'RETURN' AND order_id = ror.id AND (audit = 0 OR audit IS NULL) and file_path is not null ) > 0 THEN '有图片待审核'"
					+ " ELSE '有图片已审核' END ) imgaudit,"
					+ "( SELECT CASE "
	                + " WHEN ( SELECT ("
	                + "     SELECT count(0) FROM order_attachment_file  WHERE order_type = 'RETURN' AND order_id = ror.id and photo_type='回单签收')>0 "
	                + " && "
	                + " (SELECT count(0) FROM order_attachment_file  WHERE order_type = 'RETURN' AND order_id = ror.id and photo_type='现场安装')=0) "
	                + "  THEN '回单签收'"
	                + " WHEN ( SELECT ("
	                + "     SELECT count(0) FROM order_attachment_file  WHERE order_type = 'RETURN' AND order_id = ror.id and photo_type='回单签收')=0 "
	                + " && "
	                + " (SELECT count(0) FROM order_attachment_file  WHERE order_type = 'RETURN' AND order_id = ror.id and photo_type='现场安装')>0) "
	                + "  THEN '现场安装'"
	                + " ELSE '' END "
	                + ") photo_type, "
					+ " dor.ref_no sign_no," 
	                + " dor.office_id dor_office_id,"
	                + " ifnull(tor.office_id,(select DISTINCT tor.office_id from transfer_order tor"
	                + " LEFT JOIN delivery_order_item doi on doi.transfer_order_id = tor.id"
	                + " where doi.delivery_id = dor.id )) tor_office_id,"
					+ " o.office_name dor_office_name,"
					+ " o2.office_name tor_office_name,"
					+ " dor.business_stamp,ror.office_id ror_office_id"
					+ " FROM return_order ror"
					+ " LEFT JOIN transfer_order tor ON tor.id = ror.transfer_order_id"
					+ " LEFT JOIN delivery_order dor ON dor.id = ror.delivery_order_id"
					+ " LEFT JOIN office o ON dor.office_id = o.id "
					+ " LEFT JOIN office o2 ON tor.office_id = o2.id "
					+ " LEFT JOIN party p ON p.id = dor.notify_party_id"
					+ " LEFT JOIN contact c ON c.id = p.contact_id"
					+ " LEFT JOIN warehouse w ON tor.warehouse_id = w.id"
					+ " LEFT JOIN warehouse w1 ON ifnull( dor.change_warehouse_id, dor.from_warehouse_id ) = w1.id"
					+ " LEFT JOIN party p2 ON p2.id = ror.customer_id"
					+ " LEFT JOIN contact c2 ON c2.id =p2.contact_id"
					+ " LEFT JOIN location lo ON lo. CODE = ifnull(tor.route_from, dor.route_from )"
					+ " LEFT JOIN location lo2 ON lo2. CODE = ifnull(tor.route_to, dor.route_to)"
					+ " LEFT JOIN user_login ul ON ul.id = ror.creator "
					+ " where "
					+ " ifnull(if(dor.isNullOrder = 'Y',dor.office_id,tor.office_id),(select DISTINCT tor.office_id from transfer_order tor"
	                + " LEFT JOIN delivery_order_item doi on doi.transfer_order_id = tor.id"
	                + " where doi.delivery_id = dor.id )) IN ( SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"' )"
					+ " and ror.customer_id in (select customer_id from user_customer where user_name='" + currentUser.getPrincipal() + "')" ;
	    if (StringUtils.isNotEmpty(order_no)){
	        sql+=" and UPPER(ror.order_no) like '%"+order_no+"%'";
        }
	    if (StringUtils.isNotEmpty(status)){
	        sql+=" and UPPER(ror.transaction_status) in(" + status+")";
        }  
	    if (StringUtils.isNotEmpty(customer)){
	        sql+=" and ror.customer_id ="+customer;
        }
		String orderByStr = " order by planning_time asc ";
        if(colName!=null && colName.length()>0){
        	orderByStr = " order by A."+colName+" "+sortBy;
        }	
			
		// 获取总条数
		sqlTotal = "select count(1) total from ( SELECT  *  from ("+ sql+") A " + conditions+ ") B";
		Record rec = Db.findFirst(sqlTotal);
		
		
		List<Record> orders = Db.find(" SELECT  *  from(" + sql + ") A" + conditions + orderByStr + sLimit);	
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);
		renderJson(orderMap);
	}
	
	// 点击查看
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RO_UPDATE})
	public void edit() {
		ReturnOrder returnOrder = ReturnOrder.dao.findById(getPara("id"));
		TransferOrder transferOrder = null;
		Long deliveryId = returnOrder.getLong("delivery_order_id");
		Long transferOrderId = returnOrder.getLong("transfer_order_id");
		Long notify_party_id = null;
		String code = "";
		String routeTo = "";
		String isNull_flag = "N";

		if (deliveryId == null) {
			transferOrder = TransferOrder.dao.findById(transferOrderId);
			if(transferOrder != null){
				setAttr("transferOrder", transferOrder);
				routeTo = transferOrder.getStr("route_to");
			}
			setAttr("isRefused", "NO");
		} else {
			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(deliveryId);
			isNull_flag = deliveryOrder.getStr("isNullOrder");
			if("Y".equals(isNull_flag)){
				List<TransferOrderItem> itemList = TransferOrderItem.dao.find("select * from transfer_order_item toi"
						+ " where delivery_id = ? ",deliveryId);	
				setAttr("itemList", itemList);
			}
			
			notify_party_id = deliveryOrder.getLong("notify_party_id");
			TransferOrderItemDetail detail =TransferOrderItemDetail.dao.findFirst("SELECT * from transfer_order_item_detail where delivery_refused_id=?",deliveryId);
			// TODO 一张配送单对应多张运输单时回单怎样取出信息
			if(detail!=null){
				setAttr("isRefused", "YES");
			}
			else{
				setAttr("isRefused", "NO");
			}
			if(deliveryOrder != null){
				routeTo = deliveryOrder.getStr("route_to");
				List<DeliveryOrderItem> deliveryOrderItems = DeliveryOrderItem.dao
						.find("select * from delivery_order_item where delivery_id = ?", deliveryId);
				//for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItems) {
					transferOrder = TransferOrder.dao.findById(transferOrderId);
				//	break;
				//}
				setAttr("deliveryOrder", deliveryOrder);
				setAttr("transferOrder", transferOrder);
			}
		}

		if(transferOrder != null){
			Long customer_id = transferOrder.getLong("customer_id");
			if (customer_id != null) {
				Party customer = Party.dao.findById(customer_id);
				Contact customerContact = Contact.dao.findById(customer
						.getLong("contact_id"));
				setAttr("customerContact", customerContact);
			}
			if (notify_party_id != null) {
				Party notify = Party.dao.findById(notify_party_id);
				Contact contact = Contact.dao.findById(notify.getLong("contact_id"));
				if(contact!=null){
					setAttr("contact", contact);
				}
				else{
					setAttr("receiving_unit", transferOrder.get("receiving_unit"));
					setAttr("receiving_address", transferOrder.get("receiving_address"));
					setAttr("receiving_name", transferOrder.get("receiving_name"));
					setAttr("receiving_phone", transferOrder.get("receiving_phone"));
				}
				Contact locationCode = Contact.dao.findById(notify
						.getLong("contact_id"));
				code = locationCode.getStr("location");
			}
		}else{
			if (returnOrder.get("customer_id") != null) {
				Party customer = Party.dao.findById(returnOrder.getLong("customer_id"));
				Contact customerContact = Contact.dao.findById(customer
						.getLong("contact_id"));
				setAttr("customerContact", customerContact);
			}
			
			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(deliveryId);
			if (deliveryOrder.get("notify_party_id") != null) {
				Party notify = Party.dao.findById(deliveryOrder.getLong("notify_party_id"));
				Contact contact = Contact.dao.findById(notify.getLong("contact_id"));
				if(contact!=null){
					setAttr("contact", contact);
				}
				Contact locationCode = Contact.dao.findById(notify
						.getLong("contact_id"));
				code = locationCode.getStr("location");
			}
		}

		List<Location> provinces2 = Location.dao
				.find("select * from location where pcode ='1'");
		Location l2 = Location.dao
				.findFirst("SELECT * FROM location where code = (select pcode from location where CODE = '"
						+ code + "')");
		Location location = null;
		if (provinces2.contains(l2)) {
			location = Location.dao
					.findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
							+ code + "'");
		} else {
			location = Location.dao
					.findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
							+ code + "'");
		}
		setAttr("location", location);

		if(transferOrder != null){
			String routeFrom = transferOrder.getStr("route_from");
			Location locationFrom = null;
			if (routeFrom != null || !"".equals(routeFrom)) {
				List<Location> provinces = Location.dao
						.find("select * from location where pcode ='1'");
				Location l = Location.dao
						.findFirst("select * from location where code = (select pcode from location where code = '"
								+ routeFrom + "')");
				if (provinces.contains(l)) {
					locationFrom = Location.dao
							.findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
									+ routeFrom + "'");
				} else {
					locationFrom = Location.dao
							.findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
									+ routeFrom + "'");
				}
				setAttr("locationFrom", locationFrom);
			}
		}else{
			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(deliveryId);
			String routeFrom = deliveryOrder.getStr("route_from");
			Location locationFrom = null;
			if (routeFrom != null || !"".equals(routeFrom)) {
				List<Location> provinces = Location.dao
						.find("select * from location where pcode ='1'");
				Location l = Location.dao
						.findFirst("select * from location where code = (select pcode from location where code = '"
								+ routeFrom + "')");
				if (provinces.contains(l)) {
					locationFrom = Location.dao
							.findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
									+ routeFrom + "'");
				} else {
					locationFrom = Location.dao
							.findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
									+ routeFrom + "'");
				}
				setAttr("locationFrom", locationFrom);
			}
		}

		Location locationTo = null;
		if (routeTo != null || !"".equals(routeTo)) {
			locationTo = LocationUtil.getLocation(routeTo);
			setAttr("locationTo", locationTo);
		}
		
		List<OrderAttachmentFile> OrderAttachmentFileList = OrderAttachmentFile.dao.find("select * from order_attachment_file where order_id = '" + getPara("id") + "' and order_type = '" + OrderAttachmentFile.OTFRT_TYPE_RETURN + "' and ifnull(photo_type,'') != '现场照片';");
		setAttr("OrderAttachmentFileList", OrderAttachmentFileList);
		List<OrderAttachmentFile> OrderAttachmentFileList2 = OrderAttachmentFile.dao.find("select * from order_attachment_file where order_id = '" + getPara("id") + "' and order_type = '" + OrderAttachmentFile.OTFRT_TYPE_RETURN + "' and ifnull(photo_type,'') = '现场照片';");
		setAttr("OrderAttachmentFileList2", OrderAttachmentFileList2);
		setAttr("returnOrder", returnOrder);
		UserLogin userLogin = UserLogin.dao
				.findById(returnOrder.getLong("creator"));
		setAttr("userLogin", userLogin);
		if(transferOrder != null){
			UserLogin userLoginTo = UserLogin.dao.findById(transferOrder.getLong("create_by"));
			setAttr("userLoginTo", userLoginTo);
		}
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);
		
		
		Map<String, String> customizeField = getCustomFile.getInstance().getCustomizeFile(this);
		setAttr("customizeField", customizeField);
		setAttr("isNull_flag", isNull_flag);
		render("/yh/returnOrder/returnOrder.html");
		
	}
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RO_UPDATE})
	public void save() {
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
		ReturnOrder returnOrder = ReturnOrder.dao.findById(getPara("id"));
		Long deliveryId = returnOrder.getLong("delivery_order_id");
		String routeTo = getPara("route_to");
		boolean isLocationChanged = getParaToBoolean("locationChanged");
		Long notifyPartyId;
		if (deliveryId == null) {
			// 直送
			TransferOrder transferOrder = TransferOrder.dao
					.findById(returnOrder.getLong("transfer_order_id"));
			if (!"".equals(routeTo) && routeTo != null) {
				transferOrder.set("route_to", routeTo);
			}
            transferOrder.set("receiving_unit", getPara("company_name")); 
            transferOrder.set("receiving_name", getPara("contact_person")); 
            transferOrder.set("receiving_address", getPara("address")); 
            transferOrder.set("receiving_phone", getPara("phone")); 
			transferOrder.update();

			// 如果目的地发生变化，保存时先删除以前计算的应收，再重新计算合同应收
			if (isLocationChanged) {
				deleteContractFinItemByTransfer(transferOrder, returnOrder.getLong("id"));
				// 计算配送单的触发的应收
				calculateChargeByCustomer(transferOrder, returnOrder.getLong("id"), users);
			}
		} else {
			// 非直送
			DeliveryOrder deliveryOrder = DeliveryOrder.dao
					.findById(deliveryId);
			if (!"".equals(routeTo) && routeTo != null) {
				deliveryOrder.set("route_to", routeTo);
			}
			if(!"".equals(getPara("customer_delivery_no")) && getPara("customer_delivery_no") != null){
				if(!getPara("customer_delivery_no").equals(deliveryOrder.getStr("customer_delivery_no"))){
					deliveryOrder.set("customer_delivery_no", getPara("customer_delivery_no"));
				}
			}
			if(!"".equals(getPara("sign_document_no")) && getPara("sign_document_no") != null){
					deliveryOrder.set("ref_no", getPara("sign_document_no"));
			}
			deliveryOrder.update();
		
			// 如果目的地发生变化，保存时先删除以前计算的应收，再重新计算合同应收
			if (isLocationChanged) {
				deleteContractFinItem(deliveryOrder, returnOrder.getLong("id"));
				// 计算配送单的触发的应收
				List<Record> transferOrderItemDetailList = Db.
						find("select toid.* from transfer_order_item_detail toid left join delivery_order_item doi on toid.id = doi.transfer_item_detail_id where doi.delivery_id = ?", deliveryOrder.getLong("id"));
		        calculateCharge(users.get(0).getLong("id"), deliveryOrder, returnOrder.getLong("id"), transferOrderItemDetailList);
			}
		}
		returnOrder.set("remark", getPara("remark"));
		returnOrder.update();
		renderJson(returnOrder);

	}

	private void deleteContractFinItem(DeliveryOrder deliveryOrder, Long returnOrderId) {
		Long customerId = deliveryOrder.getLong("customer_id");
		// 先获取有效期内的客户合同, 如有多个，默认取第一个
		Contract customerContract = Contract.dao
				.findFirst("select * from contract where type='CUSTOMER' "
						+ "and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="
						+ customerId);
		if (customerContract == null)
			return;

		Db.update("delete from return_order_fin_item where contract_id="+ customerContract.getLong("id")+" and return_order_id = "+returnOrderId);
	}
	
	private void deleteContractFinItemByTransfer(TransferOrder deliveryOrder, Long returnOrderId) {
		Long customerId = deliveryOrder.getLong("customer_id");
		// 先获取有效期内的客户合同, 如有多个，默认取第一个
		Contract customerContract = Contract.dao
				.findFirst("select * from contract where type='CUSTOMER' "
						+ "and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="
						+ customerId);
		if (customerContract == null)
			return;
		
		Db.update("delete from return_order_fin_item where contract_id="+ customerContract.getLong("id")+" and return_order_id = "+returnOrderId);
	}

	// 更新收货人信息
	/*private void updateContact(Long notifyPartyId) {
		Party party = Party.dao.findById(notifyPartyId);
		Contact contact = Contact.dao.findById(party.get("contact_id"));
		contact.set("company_name", getPara("company_name"));
		contact.set("address", getPara("address"));
		contact.set("contact_person", getPara("contact_person"));
		contact.set("phone", getPara("phone"));
		contact.update();
	}*/

	// 回单签收
	@Before(Tx.class)
	public void returnOrderReceipt() {
		String id = getPara("id");
		String[] array = id.split(",");
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		
		for(int i=0;i<array.length;i++){
			ReturnOrder returnOrder = ReturnOrder.dao.findById(array[i]);		
			returnOrder.set("transaction_status", "已签收").set("receipt_date", sqlDate).update();
			
			Long deliveryId = returnOrder.getLong("delivery_order_id");
			if (deliveryId != null && !"".equals(deliveryId)) {
				DeliveryOrderMilestone doMilestone = new DeliveryOrderMilestone();
				doMilestone.set("status", "已签收");
				String name = (String) currentUser.getPrincipal();
				
				List<UserLogin> users = UserLogin.dao
						.find("select * from user_login where user_name='" + name
								+ "'");
				doMilestone.set("create_by", users.get(0).getLong("id"));
				doMilestone.set("location", "");
				utilDate = new java.util.Date();
				sqlDate = new java.sql.Timestamp(utilDate.getTime());
				doMilestone.set("create_stamp", sqlDate); 
				doMilestone.set("delivery_id", deliveryId);
				doMilestone.save();
				
				//更新配送当回单状态
				DeliveryOrder deliveryOrder	= DeliveryOrder.dao.findById(deliveryId);
				if(deliveryOrder != null){
					deliveryOrder.set("sign_status","已签收");
					deliveryOrder.update();
				}
			} else {
				DepartTransferOrder departTransferOrder = DepartTransferOrder.dao.findFirst("select * from depart_transfer where order_id = ? ", returnOrder.getLong("transfer_order_id"));
				DepartOrder departOrder = DepartOrder.dao.findById(departTransferOrder.getLong("pickup_id"));
				if(departOrder!=null){
					
					departOrder.set("sign_status", "已签收");
					departOrder.update();
				}		
			}
		}
		
		renderJson("{\"success\":true}");
	}
	//计算ATM合同费用
	public void calculateCharge(Long userId, DeliveryOrder deliveryOrder, Long returnOrderId, List<Record> transferOrderItemDetailList) {
		// TODO 运输单的计费类型,当一张配送单对应多张运输单时chargeType如何处理?
		//String chargeType = "perUnit";
		List<DeliveryOrderItem> deliveryOrderItems = DeliveryOrderItem.dao.find("select * from delivery_order_item where delivery_id = ?", deliveryOrder.getLong("id"));
		TransferOrder transferOrder = TransferOrder.dao.findById(deliveryOrderItems.get(0).getLong("transfer_order_id"));
		String chargeType = transferOrder.getStr("charge_type");
		//将保险单的应收费用显示在回单应收里面
		InsertinsuranceFin(deliveryOrder);
		Long deliveryOrderId = deliveryOrder.getLong("id");
		// 找到该回单对应的配送单中的ATM
		Long customerId = deliveryOrder.getLong("customer_id");
		// 先获取有效期内的客户合同, 如有多个，默认取第一个
		Contract customerContract = Contract.dao
				.findFirst("select * from contract where type='CUSTOMER' "
						+ "and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="
						+ customerId);
		if (customerContract == null)
			return;

		if ("perUnit".equals(chargeType)) {
            genFinPerUnit(customerContract, chargeType, deliveryOrder, transferOrder, returnOrderId);
        } else if ("perCar".equals(chargeType)) {
        	//genFinPerCar(customerContract, chargeType, deliveryOrder, transferOrder, returnOrderId);
        } else if ("perCargo".equals(chargeType)) {
        	//每次都新生成一个helper来处理计算，防止并发问题。
           // ReturnOrderPaymentHelper.getInstance().genFinPerCargo(users, deliveryOrder, transferOrderItemDetailList, customerContract, chargeType, returnOrderId, transferOrder);
        } 
	}
	
	//计算普货合同费用
	public void calculateChargeGeneral(Long userId, DeliveryOrder deliveryOrder, Long returnOrderId, List<Record> transferOrderItemList) {
		// TODO 运输单的计费类型,当一张配送单对应多张运输单时chargeType如何处理?
		//String chargeType = "perUnit";
		List<DeliveryOrderItem> deliveryOrderItems = DeliveryOrderItem.dao.find("select * from delivery_order_item where delivery_id = ?", deliveryOrder.getLong("id"));
		TransferOrder transferOrder = TransferOrder.dao.findById(deliveryOrderItems.get(0).getLong("transfer_order_id"));
		String chargeType = transferOrder.getStr("charge_type");

		Long deliveryOrderId = deliveryOrder.getLong("id");
		// 找到该回单对应的配送单中的ATM
		Long customerId = deliveryOrder.getLong("customer_id");
		// 先获取有效期内的客户合同, 如有多个，默认取第一个
		Contract customerContract = Contract.dao
				.findFirst("select * from contract where type='CUSTOMER' "
						+ "and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="
						+ customerId);
		if (customerContract == null)
			return;

		if ("perUnit".equals(chargeType)) {
            genFinPerUnit(customerContract, chargeType, deliveryOrder, transferOrder, returnOrderId);
        } else if ("perCar".equals(chargeType)) {
        	genFinPerCar(customerContract, chargeType, deliveryOrder, transferOrder, returnOrderId);
        } else if ("perCargo".equals(chargeType)) {
        	//每次都新生成一个helper来处理计算，防止并发问题。
            ReturnOrderPaymentHelper.getInstance().genFinPerCargo(userId, deliveryOrder, transferOrderItemList, customerContract, chargeType, returnOrderId, transferOrder);
        } 
	}

    private void genFinPerCar(Contract spContract, String chargeType, DeliveryOrder deliverOrder, TransferOrder transferOrder, Long returnOrderId) {
        Long deliverOrderId = deliverOrder.getLong("id");
    
        Record contractFinItem = Db
                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                        +" and carType = '" + transferOrder.getStr("car_type") +"' "
                        +" and carlength = " + deliverOrder.getStr("car_size")
                        +" and from_id = '"+ transferOrder.getStr("route_from")
                        +"' and to_id = '"+ deliverOrder.getStr("route_to")
                        + "' and priceType='"+chargeType+"'");
        
        if (contractFinItem != null) {
            genFinItem(deliverOrderId, null, contractFinItem, chargeType, returnOrderId, spContract);
        }else{
            contractFinItem = Db
                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                            +" and cartype = '" + transferOrder.getStr("car_type") +"' "
                            +" and from_id = '"+ transferOrder.getStr("route_from")
                            +"' and to_id = '"+ deliverOrder.getStr("route_to")
                            + "' and priceType='"+chargeType+"'");
            
            if (contractFinItem != null) {
            	genFinItem(deliverOrderId, null, contractFinItem, chargeType, returnOrderId, spContract);
            }else{
			    contractFinItem = Db
			            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
			                    +" and carType = '" + deliverOrder.getStr("car_type")//对应发车单的 car_type
			                    +"' and to_id = '" + deliverOrder.getStr("route_to")
			                    + "' and priceType='"+chargeType+"'");
			    if (contractFinItem != null) {
			        genFinItem(deliverOrderId, null, contractFinItem, chargeType, returnOrderId, spContract);
			    }
			}
        }        
    } 
    
    private void genFinPerUnit(Contract spContract, String chargeType, DeliveryOrder deliveryOrder, TransferOrder transferOrder, Long returnOrderId) {
    	String sql = "";
    	long  deliverOrderId = deliveryOrder.getLong("id");
        if("ATM".equals(deliveryOrder.getStr("cargo_nature"))){
        	 sql = "SELECT count(1) amount, toi.product_id, d_o.route_from, d_o.route_to FROM "+
			    "delivery_order_item doi LEFT JOIN transfer_order_item_detail toid ON doi.transfer_item_detail_id = toid.id "+
			        "LEFT JOIN transfer_order_item toi ON toid.item_id = toi.id "+
			        "LEFT JOIN delivery_order d_o ON doi.delivery_id = d_o.id "+
         		"WHERE  doi.delivery_id = "+ deliverOrderId+" group by toi.product_id, d_o.route_from, d_o.route_to";
        }else{
    	   sql = "select toi.amount, toi.product_id, d_o.route_from, d_o.route_to from delivery_order_item doi "
           		+ " left join transfer_order_item toi on toi.order_id = doi.transfer_order_id"
           		+ " left join delivery_order d_o on doi.delivery_id = d_o.id"
           		+ " where doi.delivery_id = 309 group by toi.product_id,d_o.route_from,	d_o.route_to";
        }
        List<Record> deliveryOrderItemList = Db.find(sql);
        for (Record dOrderItemRecord : deliveryOrderItemList) {
            Record contractFinItem = Db
                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                            + " and product_id ="+dOrderItemRecord.getLong("product_id")
                            +" and from_id = '"+ transferOrder.getStr("route_from")
                            +"' and to_id = '"+ dOrderItemRecord.getStr("route_to")
                            + "' and priceType='"+chargeType+"'");
            
            if (contractFinItem != null) {
                genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId, spContract);
            }else{
                contractFinItem = Db
                        .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                + " and product_id ="+dOrderItemRecord.getLong("product_id")
                                +" and to_id = '"+ dOrderItemRecord.getStr("route_to")
                                + "' and priceType='"+chargeType+"'");
                
                if (contractFinItem != null) {
                	genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId, spContract);
                }else{
                    contractFinItem = Db
                            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                    +" and from_id = '"+ transferOrder.getStr("route_from")
                                    +"' and to_id = '"+ dOrderItemRecord.getStr("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    
                    if (contractFinItem != null) {
                    	genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId, spContract);
                    }else{
                        contractFinItem = Db
                                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                        +" and to_id = '"+ dOrderItemRecord.getStr("route_to")
                                        +"' and priceType='"+chargeType+"'");
                        
                        if (contractFinItem != null) {
                        	genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId, spContract);
                        }
                    }
                }
            }
        }
    }

	private void genFinItem(Long deliveryOrderId, Record tOrderItemRecord, Record contractFinItem, String chargeType, Long returnOrderId, Contract contract) {
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
		ReturnOrderFinItem returnOrderFinItem = new ReturnOrderFinItem();
		returnOrderFinItem.set("fin_item_id", contractFinItem.get("fin_item_id"));
		if("perCar".equals(chargeType)){
			returnOrderFinItem.set("amount", contractFinItem.getDouble("amount"));        		
    	}else{
    		if(tOrderItemRecord != null){
    			returnOrderFinItem.set("amount", contractFinItem.getDouble("amount") * Double.parseDouble(tOrderItemRecord.getStr("amount")));
    		}
    	}
		returnOrderFinItem.set("delivery_order_id", deliveryOrderId);
		returnOrderFinItem.set("return_order_id", returnOrderId);
		returnOrderFinItem.set("status", "未完成");
		returnOrderFinItem.set("fin_type", "charge");// 类型是应收
		returnOrderFinItem.set("contract_id", contractFinItem.get("contract_id"));// 类型是应收
		returnOrderFinItem.set("creator", LoginUserController.getLoginUserId(this));
		returnOrderFinItem.set("create_date", now);
		returnOrderFinItem.set("create_name", "system");
		returnOrderFinItem.set("contract_id", contract.get("id"));
		
		returnOrderFinItem.save();
	}
	
	private void calcRevenuePerCar(Contract spContract, String chargeType, TransferOrder transferOrder, Long returnOrderId) {
        Record contractFinItem = Db
                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                        +" and carType = '" + transferOrder.getStr("car_type") +"' "
                        +" and carlength = " + transferOrder.getStr("car_size")
                        +" and from_id = '"+ transferOrder.getStr("route_from")
                        +"' and to_id = '"+ transferOrder.getStr("route_to")
                        + "' and priceType='"+chargeType+"'");
        
        if (contractFinItem != null) {
            genFinItem2(transferOrder.getLong("id"), null, contractFinItem, chargeType, returnOrderId, spContract);
        }else{
            contractFinItem = Db
                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                            +" and carType = '" + transferOrder.getStr("car_type") +"' "
                            +" and from_id = '"+ transferOrder.getStr("route_from")
                            +"' and to_id = '"+ transferOrder.getStr("route_to")
                            + "' and priceType='"+chargeType+"'");
            
            if (contractFinItem != null) {
            	genFinItem2(transferOrder.getLong("id"), null, contractFinItem, chargeType, returnOrderId, spContract);
            }else{
			    contractFinItem = Db
			            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
			                    +" and carType = '" + transferOrder.getStr("car_type")//对应发车单的 car_type
			                    +"' and to_id = '" + transferOrder.getStr("route_to")
			                    + "' and priceType='"+chargeType+"'");
			    if (contractFinItem != null) {
			        genFinItem2(transferOrder.getLong("id"), null, contractFinItem, chargeType, returnOrderId, spContract);
			    }
			}
        }        
    } 
	
	public void calculateChargeByCustomer(TransferOrder transferOrder, Long returnOrderId, List<UserLogin> users) {
		String chargeType = transferOrder.getStr("charge_type");
		
		Long transferOrderId = transferOrder.getLong("id");
		// 找到该回单对应的配送单中的ATM
		Long customerId = transferOrder.getLong("customer_id");
		// 先获取有效期内的客户合同, 如有多个，默认取第一个
		Contract customerContract = Contract.dao
				.findFirst("select * from contract where type='CUSTOMER' "
						+ "and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="
						+ customerId);
		if (customerContract == null)
			return;
		
		// 运输单的始发地, 配送单的目的地
		// 算最长的路程的应收
		//直送增加保险费用
		
		if("perUnit".equals(chargeType)){//计件
			calcRevenuePerUnit(returnOrderId, chargeType, transferOrderId, customerContract);
		}else if ("perCar".equals(chargeType)){//整车
			//calcRevenuePerCar(customerContract, chargeType, transferOrder, returnOrderId);
		}else if("perCargo".equals(chargeType)){//零担
			List<Record> transferOrderItemList = Db.find("select toi.* from transfer_order_item toi left join transfer_order tor on tor.id = toi.order_id where tor.id = ?", transferOrder.getLong("id"));
			//每次都新生成一个helper来处理计算，防止并发问题。
           // ReturnOrderPaymentHelperForDirect.getInstance().genFinPerCargo(users, transferOrder, transferOrderItemList, customerContract, chargeType, returnOrderId);
		}
	}

	private void calcRevenuePerUnit(Long returnOrderId, String chargeType,
			Long transferOrderId, Contract customerContract) {
		List<Record> transferOrderItemList = Db
				.find("select distinct toi.amount amount, toi.product_id, t_o.id, t_o.route_from, t_o.route_to from transfer_order_item toi "
						+ " left join transfer_order_item_detail toid on toid.item_id = toi.id "
						+ " left join transfer_order t_o on t_o.id = toi.order_id "
						+ " where toi.order_id = "+ transferOrderId);
		for (Record dOrderItemRecord : transferOrderItemList) {
			Record contractFinItem = Db
					.findFirst("select amount, fin_item_id, contract_id from contract_item where contract_id ="
							+ customerContract.getLong("id")
							+ " and product_id ="
							+ dOrderItemRecord.getLong("product_id")
							+ " and from_id = '"
							+ dOrderItemRecord.getStr("route_from")
							+ "' and to_id = '"
							+ dOrderItemRecord.getStr("route_to")
							+ "' and priceType='" + chargeType + "'");
			
			if (contractFinItem != null) {
				genFinItem2(transferOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId, customerContract);
			} else {
				contractFinItem = Db
						.findFirst("select amount, fin_item_id, contract_id from contract_item where contract_id ="
								+ customerContract.getLong("id")
								+ " and product_id ="
								+ dOrderItemRecord.getLong("product_id")
								+ " and to_id = '"
								+ dOrderItemRecord.getStr("route_to")
								+ "' and priceType='" + chargeType + "'");
				
				if (contractFinItem != null) {
					genFinItem2(transferOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId, customerContract);
				} else {
					contractFinItem = Db
							.findFirst("select amount, fin_item_id, contract_id from contract_item where contract_id ="
									+ customerContract.getLong("id")
									+ " and from_id = '"
									+ dOrderItemRecord.getStr("route_from")
									+ "' and to_id = '"
									+ dOrderItemRecord.getStr("route_to")
									+ "' and priceType='" + chargeType + "'");
					
					if (contractFinItem != null) {
						genFinItem2(transferOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId, customerContract);
					} else {
						contractFinItem = Db
								.findFirst("select amount, fin_item_id, contract_id from contract_item where contract_id ="
										+ customerContract.getLong("id")
										+ " and to_id = '"
										+ dOrderItemRecord.getStr("route_to")
										+ "' and priceType='"
										+ chargeType
										+ "'");
						
						if (contractFinItem != null) {
							genFinItem2(transferOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId, customerContract);
						}
					}
				}
			}
		}
	}
	
	private void genFinItem2(Long transferOrderId, Record tOrderItemRecord,	Record contractFinItem, String chargeType, Long returnOrderId, Contract contract) {
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
		ReturnOrderFinItem returnOrderFinItem = new ReturnOrderFinItem();
		returnOrderFinItem.set("fin_item_id", contractFinItem.get("fin_item_id"));
		if("perCar".equals(chargeType)){
			returnOrderFinItem.set("amount", contractFinItem.getDouble("amount"));        		
    	}else{
    		if(tOrderItemRecord.get("amount") != null){
    			returnOrderFinItem.set("amount", contractFinItem.getDouble("amount") * Double.parseDouble(tOrderItemRecord.getStr("amount")));
    		}
    	}
		returnOrderFinItem.set("transfer_order_id", transferOrderId);
		returnOrderFinItem.set("return_order_id", returnOrderId);
		returnOrderFinItem.set("status", "未完成");
		returnOrderFinItem.set("fin_type", "charge");// 类型是应收
		returnOrderFinItem.set("contract_id", contractFinItem.get("contract_id"));// 类型是应收
		returnOrderFinItem.set("creator", LoginUserController.getLoginUserId(this));
		returnOrderFinItem.set("create_date", now);
		returnOrderFinItem.set("create_name", "system");
		//returnOrderFinItem.set("contract_id", contract.get("id"));
		
		returnOrderFinItem.save();
	}
	
	// 取消
	public void cancel() {
		String id = getPara();
		ReturnOrder re = ReturnOrder.dao.findById(id);
		re.set("TRANSACTION_STATUS", "cancel").update();
		renderJson("{\"success\":true}");
	}

	public void transferOrderDetailList() {
		String deliveryOrderId = getPara("deliveryOrderId");
		String orderId = getPara("orderId");
		if (deliveryOrderId == null || "".equals(deliveryOrderId)) {
			deliveryOrderId = "-1";
		}
		if (orderId == null || "".equals(orderId)) {
			orderId = "-1";
		}
		logger.debug(deliveryOrderId);

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sql = "";
		String sqlTotal = "";
		if (deliveryOrderId != "-1") {
			sqlTotal = "select count(1) total from transfer_order_item_detail where delivery_id ="
					+ deliveryOrderId;

			sql = "select d.*,c.contact_person,c.phone,c.address from transfer_order_item_detail d"
					+ " left join party p on d.notify_party_id = p.id"
					+ " left join contact c on p.contact_id = c.id"
					+ " where d.delivery_id =" + deliveryOrderId + sLimit;
		} else {
			sqlTotal = "select count(1) total from transfer_order_item_detail where order_id="
					+ orderId;

			sql = "select d.*,c.contact_person,c.phone,c.address from transfer_order_item_detail d"
					+ " left join party p on d.notify_party_id = p.id"
					+ " left join contact c on p.contact_id = c.id"
					+ " where order_id = " + orderId + sLimit;
		}

		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		List<Record> transferOrders = Db.find(sql);
		Map transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);

		renderJson(transferOrderListMap);
	}

	// 编辑发车单,查看运输单信息
	public void transferOrderList() {
		String returnOrderId = getPara("returnOrderId");
		String sLimit = "";
		String categoryId = getPara("categoryId");
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String category = getPara("category");
		String sql = "";
		String sqlTotal = "";
		sqlTotal = "select count(1) total from return_order ror where id = "
				+ returnOrderId;
		sql = "select ifnull(tor1.id,tor2.id) id,"
				+ "ifnull(tor1.order_no,tor2.order_no) order_no,"
				+ "ifnull(t1.serial_no,t2.serial_no) serial_no,"
				+ "ifnull(tor1.status,tor2.status) status,"
				+ "ifnull(tor1.cargo_nature,tor2.cargo_nature) cargo_nature,"
				+ "ifnull(tor1.pickup_mode,tor2.pickup_mode) pickup_mode,"
				+ "ifnull(tor1.arrival_mode,tor2.arrival_mode) arrival_mode,"
				+ "ifnull(tor1.operation_type,tor2.operation_type) operation_type,"
				+ "ifnull(tor1.create_stamp,tor2.create_stamp) create_stamp,"
				+ "ifnull(tor1.remark,tor2.remark) remark,"
				+ "ifnull(tor1.order_type,tor2.order_type) order_type  "
				+ "from return_order ror "
				+ " left join transfer_order tor1 on tor1.id = ror.transfer_order_id  "
				+ " left join delivery_order dor on dor.id = ror.delivery_order_id"
				+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
				+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id"
				+ " left join transfer_order_item_detail t1 on t1.order_Id=  tor1.id"
				+ " left join transfer_order_item_detail t2 on t2.order_Id=  tor2.id"
				+ " where ror.id = " + returnOrderId;
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		List<Record> products = Db.find(sql);
		Map map = new HashMap();
		map.put("sEcho", pageIndex);
		map.put("iTotalRecords", rec.getLong("total"));
		map.put("iTotalDisplayRecords", rec.getLong("total"));
		map.put("aaData", products);
		renderJson(map);
	}

	// 货品明细
	public void transferOrderItemList() {
		Map transferOrderListMap = null;
		String returnOrderId = getPara("order_id");
		String productId = getPara("product_id");
		String transferOrderId = getPara("id");
		if (returnOrderId == null || "".equals(returnOrderId)) {
			returnOrderId = "-1";
		}
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sqlTotal = "";
		String sql = "";
		ReturnOrder returnorder=ReturnOrder.dao.findById(returnOrderId);
		Record transferOrder = new Record();
		if(returnorder.getLong("transfer_order_id")!=null){
			transferOrder =  Db.findFirst("select cargo_nature,cargo_nature_detail from transfer_order where id =?",returnorder.getLong("transfer_order_id"));
		}else if(returnorder.getLong("delivery_order_id")!=null){
			transferOrder = Db.findFirst("SELECT tro.cargo_nature,tro.cargo_nature_detail FROM transfer_order tro "
							+ " LEFT JOIN delivery_order_item doi ON doi.transfer_order_id = tro.id  WHERE doi.delivery_id = ?",returnorder.getLong("delivery_order_id"));
		}
		Record  transferDetail= Db
				.findFirst("select * from transfer_order_item_detail where delivery_refused_id =?",returnorder.getLong("delivery_order_id"));
		//判断是否为ATM机
		if (transferOrder.getStr("cargo_nature").equals("ATM")) {
			if(transferDetail!=null){
				sqlTotal = "select distinct count(1) total "
						+ "from transfer_order_item_detail toid "//TODO 这里性能有问题，用了大表关联小表
						+ "left join transfer_order_item toi on toid.item_id = toi.id "
						+ "left join return_order r on (toid.delivery_refused_id= r.delivery_order_id or toi.order_id = r.transfer_order_id) "
						+ "left join product p on toi.product_id = p.id where r.id ="
						+ returnOrderId;
				sql = "select distinct count(*) as amount ,id,tid,item_no,item_name,width,size,weight,height,volume,unit,remark,tid,serial_no,pieces from ( "
						+ "select toi.id as id, "
						+ "toid.id as tid, "
						+ "ifnull(toid.serial_no,'') serial_no, "
						+ "toid.pieces pieces, "
						+ "ifnull(p.item_no, toi.item_no) item_no, "
						+ "ifnull(p.item_name, toi.item_name) item_name,"
						+ "ifnull(p.size, toi.size) size, "
						+ "ifnull(p.width, toi.width) width, "
						+ "ifnull(p.height, toi.height) height, "
						+ "ifnull(p.weight, toi.weight) weight, "
						+ "ifnull(p.volume, toi.volume) volume,"
						+ "ifnull(p.unit, toi.unit) unit, "
						+ "toi.remark "
						+ "from transfer_order_item_detail toid "//TODO: 这里性能有问题，用了大表关联小表
						+ "left join transfer_order_item toi ON toid.item_id = toi.id "
						+ "left join return_order r on (toid.delivery_refused_id= r.delivery_order_id or toi.order_id = r.transfer_order_id) "
						+ "left join product p on toi.product_id = p.id where r.id ="
						+ returnOrderId + ") toid group by tid" + sLimit;
			}else{
				sqlTotal = "select distinct count(1) total "
						+ "from transfer_order_item_detail toid "//TODO 这里性能有问题，用了大表关联小表
						+ "left join transfer_order_item toi on toid.item_id = toi.id "
						+ "left join return_order r on (toid.delivery_id= r.delivery_order_id or toi.order_id = r.transfer_order_id) "
						+ "left join product p on toi.product_id = p.id where r.id ="
						+ returnOrderId;
				sql = "select distinct count(*) as amount ,id,tid,item_no,item_name,width,size,weight,height,volume,unit,remark,tid,serial_no,pieces from ( "
						+ "select toi.id as id, "
						+ "toid.id as tid, "
						+ "ifnull(toid.serial_no,'') serial_no, "
						+ "toid.pieces pieces, "
						+ "ifnull(p.item_no, toi.item_no) item_no, "
						+ "ifnull(p.item_name, toi.item_name) item_name,"
						+ "ifnull(p.size, toi.size) size, "
						+ "ifnull(p.width, toi.width) width, "
						+ "ifnull(p.height, toi.height) height, "
						+ "ifnull(p.weight, toi.weight) weight, "
						+ "ifnull(p.volume, toi.volume) volume,"
						+ "ifnull(p.unit, toi.unit) unit, "
						+ "toi.remark "
						+ "from transfer_order_item_detail toid "//TODO: 这里性能有问题，用了大表关联小表
						+ "left join transfer_order_item toi ON toid.item_id = toi.id "
						+ "left join return_order r on (toid.delivery_id= r.delivery_order_id or toi.order_id = r.transfer_order_id) "
						+ "left join product p on toi.product_id = p.id where r.id ="
						+ returnOrderId + ") toid group by tid" + sLimit;
			}
		} else {
			sqlTotal = "select distinct count(1) total "
					+ " from transfer_order_item toi "
					+ " left join return_order r on r.transfer_order_id = toi.order_id "
					+ " left join product p on toi.product_id = p.id "
					+ " where r.id = '" + returnOrderId + "'";
			if (transferOrder.getStr("cargo_nature_detail").equals("cargoNatureDetailYes")) {
				sqlTotal = "select distinct count(1) total "
						+ "from transfer_order_item_detail toid " //TODO: 这里性能有问题，用了大表关联小表
						+ "left join transfer_order_item toi on toid.item_id = toi.id "
						+ "left join return_order r on (toid.delivery_id= r.delivery_order_id or toi.order_id = r.transfer_order_id) "
						+ "left join product p on toi.product_id = p.id where r.id ="
						+ returnOrderId;
				
				sql = "select distinct count(*) as amount ,item_no,item_name,width,size,weight,height,volume,unit,remark,tid,serial_no,pieces from ( "
						+ "select toi.id as id,"
						+ "toid.id as tid,"
						+ "ifnull(toid.serial_no,'') serial_no, "
						+ "ifnull(toid.pieces,'') pieces, "
						+ "ifnull(p.item_no, toi.item_no) item_no, "
						+ "ifnull(p.item_name, toi.item_name) item_name,"
						+ "ifnull(p.size, toi.size) size, "
						+ "ifnull(p.width, toi.width) width, "
						+ "ifnull(p.height, toi.height) height, "
						+ "ifnull(p.weight, toi.weight) weight, "
						+ "toi.volume volume,"
						+ "ifnull(p.unit, toi.unit) unit, "
						+ "toi.sum_weight, "
						+ "toi.remark "
						+ "from transfer_order_item_detail toid " //TODO: 这里性能有问题，用了大表关联小表
						+ "left join transfer_order_item toi on toid.item_id = toi.id "
						+ "left join return_order r on (toid.delivery_id= r.delivery_order_id or toi.order_id = r.transfer_order_id) "
						+ "left join product p on toi.product_id = p.id where r.id ="
						+ returnOrderId + ") toid " + sLimit;						
			} else {
				sql = "select toi.id,"
						+ " ifnull(p.serial_no, '') serial_no,"
						+ " ifnull(p.item_no, toi.item_no) item_no, "
						+ " ifnull(p.item_name, toi.item_name) item_name,"
						+ " ifnull(p.size, toi.size) size, "
						+ " ifnull(p.width, toi.width) width, "
						+ " ifnull(p.height, toi.height) height, "
						+ " ifnull(p.weight, toi.weight) weight, "
						+ "toi.volume volume,"
						+ " ifnull(p.unit, toi.unit) unit, "
						+ " toi.sum_weight, "
						+ " ifnull(( SELECT sum(amount) FROM delivery_order_item doi "
						+ " WHERE doi.delivery_id = r.delivery_order_id ), "
						+ " ( SELECT sum(amount) FROM transfer_order_item"
						+ "  WHERE order_id = r.transfer_order_id ) ) amount, "
						+ " toi.remark "
						+ " from transfer_order_item toi " //TODO: 这里性能有问题，用了大表关联小表
						+ " left join return_order r on r.transfer_order_id = toi.order_id "
						+ " left join product p on toi.product_id = p.id "
						+ " where r.id = '" + returnOrderId + "' " + sLimit;
			}
		}

		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		List<Record> transferOrders = Db.find(sql);
		transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
		transferOrderListMap.put("aaData", transferOrders);
		renderJson(transferOrderListMap);
	}

	// 单品
	public void transferOrderDetailList2() {
		String itemId = getPara("item_id");
		String orderId = getPara("orderId");
		if (itemId == null || "".equals(itemId)) {
			itemId = "-1";
		}
		if (orderId == null || "".equals(orderId)) {
			orderId = "-1";
		}
		logger.debug(itemId);

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sql = "";
		String sqlTotal = "";
		if (itemId != "-1") {
			sqlTotal = "select count(1) total from transfer_order_item_detail where item_id ="
					+ itemId;

			sql = "select d.*,c.contact_person,c.phone,c.address from transfer_order_item_detail d"
					+ " left join party p on d.notify_party_id = p.id"
					+ " left join contact c on p.contact_id = c.id"
					+ " where d.item_id =" + itemId + sLimit;
		} else {
			sqlTotal = "select count(1) total from transfer_order_item_detail where order_id="
					+ orderId;

			sql = "select d.*,c.contact_person,c.phone,c.address from transfer_order_item_detail d"
					+ " left join party p on d.notify_party_id = p.id"
					+ " left join contact c on p.contact_id = c.id"
					+ " where order_id = " + orderId + sLimit;
		}

		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		List<Record> transferOrders = Db.find(sql);
		Map transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);

		renderJson(transferOrderListMap);
	}

	// 删除TransferOrderItem
	public void deleteTransferOrderItem() {
		String id = getPara("transfer_order_item_id");
		List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
				.find("select * from transfer_order_item_detail where item_id="
						+ id);
		for (TransferOrderItemDetail itemDetail : transferOrderItemDetails) {
			itemDetail.delete();
		}
		TransferOrderItem.dao.deleteById(id);
		renderJson("{\"success\":true}");
	}

	// 应收
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RO_ADD_REVENUE})
	public void addNewRow() {
		List<FinItem> items = new ArrayList<FinItem>();
		String returnOrderId = getPara();
		FinItem item = FinItem.dao
				.findFirst("select * from fin_item where type = '应收' order by id asc");
		if (item != null) {
			ReturnOrderFinItem dFinItem = new ReturnOrderFinItem();
			dFinItem.set("status", "新建").set("fin_item_id", item.get("id"))
					.set("return_order_id", returnOrderId)
					.set("create_date", new Date())
					.set("create_name", "user")
					.save();
		}
		items.add(item);
		renderJson(items);
	}

	// 修改应付
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RO_ADD_REVENUE})
	@Before(Tx.class)
	public void updateTransferOrderFinItem() {
		String paymentId = getPara("paymentId");
		String name = getPara("name");
		String value = getPara("value");
		if ("amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		if (paymentId != null && !"".equals(paymentId)) {
			
			ReturnOrderFinItem returnOrderFinItem = ReturnOrderFinItem.dao
					.findById(paymentId);
			String ggname = (String) currentUser.getPrincipal();
    		List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + ggname + "'");
    		String createDate =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			if ("amount".equals(name)) {
				returnOrderFinItem.set("remark",users.get(0).getStr("c_name")+">"+createDate+">金额"+returnOrderFinItem.getDouble("amount")+"改为"+value+"");
			}
			returnOrderFinItem.set(name, value);
			returnOrderFinItem.update();
		}
		renderJson("{\"success\":true}");
	}

	// 应收list
	public void accountReceivable() {
		String id = getPara();

		String sLimit = "";
		if (id == null || id.equals("")) {
			Map orderMap = new HashMap();
			orderMap.put("sEcho", 0);
			orderMap.put("iTotalRecords", 0);
			orderMap.put("iTotalDisplayRecords", 0);
			orderMap.put("aaData", null);
			renderJson(orderMap);
			return;
		}
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from return_order_fin_item rofi left join return_order ror on ror.id = rofi.return_order_id where ror.id = '"
				+ id + "' "; // and f.type='应收' TODO： 有问题
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		sql = "select distinct f.name name, rofi.*,ifnull(tor.order_no,(select group_concat(distinct tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id where r_o.delivery_order_id = dor.id)) transfer_order_no, d_o.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,r_o.transaction_status new_status"
				+ " from return_order_fin_item rofi "
				+ " left join return_order r_o on r_o.id = rofi.return_order_id"
				+ " left join fin_item f on rofi.fin_item_id = f.id "
				+ " left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id  "
				+ " left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join delivery_order_item doi on doi.delivery_id = d_o.id "
				+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id where r_o.id = "
				+ id + " and f.type='应收' order by create_date " + sLimit;
		List<Record> orders = Db.find(sql);
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}
	// 删除应收
    public void finItemdel() {
        String id = getPara();
        ReturnOrderFinItem.dao.deleteById(id);
        renderJson("{\"success\":true}");
    }
    
    //回单状态
    public void findReturnOrderType(){
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        String orderNo = getPara("pointInTime");
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");  
	    Calendar pastDay = Calendar.getInstance(); 
	    if("pastOneDay".equals(orderNo))
	    	pastDay.add(Calendar.DAY_OF_WEEK, -1);
	    else if("pastSevenDay".equals(orderNo))
	    	pastDay.add(Calendar.DAY_OF_WEEK, -7);
	    else
	    	pastDay.add(Calendar.DAY_OF_WEEK, -30);
	    String beginTime = df.format(pastDay.getTime());
	    String endTime = simpleDate.format(Calendar.getInstance().getTime());
        
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(0) total from return_order ro"
		        		+ " left join delivery_order dor on dor.id = ro.delivery_order_id "
		        		+ " left join transfer_order tor on tor.id = ro.transfer_order_id "
						+ " left join warehouse w on dor.from_warehouse_id = w.id "
						+ " where ifnull(w.office_id,tor.office_id) in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')"
						+ " and ifnull(dor.customer_id,tor.customer_id) in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
						+ " and ro.create_date between '" + beginTime + "' and '" + endTime + "'";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select ro.id,ro.order_no,ro.transaction_status,ro.create_date,"
        		+ " ifnull(tor.receiving_unit,'') notify_party_name,"
        		+ " ifnull((select name from location where code = dor.route_from ), '') route_from,"
        		+ " ifnull((select name from location where code = dor.route_to ), '') route_to,"
        		+ " (select sum(amount) from return_order_fin_item where return_order_id = ro.id ) amount"
        		+ " from return_order ro "
        		+ " left join delivery_order dor on dor.id = ro.delivery_order_id "
        		+ " left join transfer_order tor on tor.id = ro.transfer_order_id "
				+ " left join warehouse w on dor.from_warehouse_id = w.id "
				+ " where ifnull(w.office_id,tor.office_id) in (select office_id from user_office where user_name='"+currentUser.getPrincipal()+"')"
				+ " and ifnull(dor.customer_id,tor.customer_id) in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') "
        		+ " and ro.create_date between '" + beginTime + "' and '" + endTime + "' order by ro.id desc " + sLimit;
        List<Record> transferOrderItems = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", transferOrderItems);
        renderJson(Map); 
    }
    
    /**
     * ATM配送时将保险费用带到回单
     */
    public void InsertinsuranceFin(DeliveryOrder deliveryOrder){
    	java.util.Date utilDate = new java.util.Date();
    	java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
		ReturnOrder returnOrder = ReturnOrder.dao.findFirst("select id from return_order where delivery_order_id = ?",deliveryOrder.getLong("id"));
		FinItem fi =  FinItem.dao.findFirst("select * from fin_item where name = '保险费' and type = '应收'");
		/*Record record = Db.findFirst("select sum(amount * income_rate) as total_amount,(select sum(amount) from delivery_order_item where delivery_id = dor.id) delivery_number from delivery_order dor "
				+ " left join transfer_order_item_detail toid on  dor.id = toid.delivery_id "
				+ " left join insurance_fin_item ifi on ifi.transfer_order_item_id = toid.item_id where dor.id = ?",deliveryOrder.get("id"));
		*/
		//计算总保险费，根据配送单中的货品型号与保险单中的货品型号相匹配，一次性算出单种货品型号的总保险费，再把配送单中所有货品型号的总保险费进行叠加得出此次配送的保险费
		//注意，这里的要求是：一张运输单只能做一次保险，否则默认取第一次做得保险信息
		double sum_insurance = 0;
		List<Record> detailList = Db.find("select count(0) delivery_number,item_id from transfer_order_item_detail where delivery_id = ? group by item_id;",deliveryOrder.getLong("id"));
		for (int i = 0; i < detailList.size(); i++) {
			Record insuranceItem = Db.findFirst("select amount * income_rate detail_insurance from insurance_fin_item where transfer_order_item_id = ?",detailList.get(i).getLong("item_id"));
			if(insuranceItem != null && insuranceItem.getDouble("detail_insurance") !=null ){				
				sum_insurance += detailList.get(i).getLong("delivery_number") * insuranceItem.getDouble("detail_insurance");
			}
		}
		BigDecimal bg = new BigDecimal(sum_insurance);
        double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		if(sum_insurance != 0){
			ReturnOrderFinItem returnOrderFinItem = new ReturnOrderFinItem();
			returnOrderFinItem.set("fin_item_id", fi.get("id"));
			returnOrderFinItem.set("amount",f1);        		
			//returnOrderFinItem.set("transfer_order_id", transferOrderId);
			returnOrderFinItem.set("return_order_id", returnOrder.get("id"));
			returnOrderFinItem.set("status", "未完成");
			returnOrderFinItem.set("fin_type", "charge");// 类型是应收
			//returnOrderFinItem.set("contract_id", null);// 类型是应收
			returnOrderFinItem.set("creator", LoginUserController.getLoginUserId(this));
			returnOrderFinItem.set("create_date", now);
			returnOrderFinItem.set("create_name", "insurance");
			//returnOrderFinItem.set("contract_id", contract.get("id"));
			returnOrderFinItem.save();
		}
		
    }
    /**
     * TODO:ATM直送时将保险费用带到回单
     * 用循环出现的问题是：运输单货品信息多少个条目，回单里面就有多少个保险费用
     */
    public void addInsuranceFin(TransferOrder transferOrder,ReturnOrder returnOrder){
    	List<TransferOrderItem> transferOrderItemList = TransferOrderItem.dao.find("select id,amount from transfer_order_item where order_id = " + transferOrder.getLong("id"));
    	//查询应收条目中的保险费
    	FinItem finItem = FinItem.dao.findFirst("select id from fin_item where type = '应收' and `name` = '保险费';");
    	for (int i = 0; i < transferOrderItemList.size(); i++) {
    		List<InsuranceFinItem> InsuranceFinItemList = InsuranceFinItem.dao.find("select * from insurance_fin_item where transfer_order_item_id = " + transferOrderItemList.get(i).getLong("id"));
    		for (int j = 0; j < InsuranceFinItemList.size(); j++) {
    			InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(InsuranceFinItemList.get(j).getLong("insurance_order_id"));
    			ReturnOrderFinItem returnOrderFinItem = new ReturnOrderFinItem();
    			double amount1 =  InsuranceFinItemList.get(j).getDouble("amount")==null?0.0:InsuranceFinItemList.get(j).getDouble("amount");
    			double amount2 =  InsuranceFinItemList.get(j).getDouble("income_rate")==null?0.0:InsuranceFinItemList.get(j).getDouble("income_rate");
    			double amount3 =  transferOrderItemList.get(i).getDouble("amount")==null?0.0:transferOrderItemList.get(i).getDouble("amount");
    			double amount =  amount1 * amount2 * amount3 ;
    			BigDecimal bg = new BigDecimal(amount);
    	        double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    			if(f1 != 0){
	    			returnOrderFinItem.set("fin_item_id", finItem.get("id"))
	    			.set("amount", f1)
		    		.set("return_order_id", returnOrder.get("id"))
		    		.set("status", insuranceOrder.get("status"))
		    		.set("fin_type", "charge")// 类型是应收
		    		.set("creator", InsuranceFinItemList.get(j).get("create_by"))
		    		.set("create_date", InsuranceFinItemList.get(j).get("create_date"))
		    		.set("create_name", "insurance")
		    		.save();
	    		}
    			
			}
		}
    }
    
    //把运输单的应收带到回单中
  	public void tansferIncomeFinItemToReturnFinItem(ReturnOrder returnOrder,long deliveryId, long transferOrderId) {
  		if(transferOrderId > 0){
  			List<TransferOrderFinItem> finTiems = TransferOrderFinItem.dao.find("select d.* from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id where d.order_id = '" + transferOrderId + "' and f.type = '应收'");
  	  		for (TransferOrderFinItem transferOrderFinItem : finTiems) {
  	  			//ReturnOrderFinItem returnOrderFinItems = ReturnOrderFinItem.dao.findFirst("select * from return_order_fin_item where return_order_id = '" + returnOrder.get("id") + "' and fin_item_id = '" + transferOrderFinItem.get("fin_item_id") + "'");
  	  			//if(returnOrderFinItems == null){
  	  				ReturnOrderFinItem returnOrderFinItem = new ReturnOrderFinItem();
  	  				returnOrderFinItem.set("fin_item_id", transferOrderFinItem.get("fin_item_id"))
  	  				.set("amount", transferOrderFinItem.get("amount"))
  	  				.set("return_order_id", returnOrder.get("id"))
  	  				.set("status", transferOrderFinItem.get("status"))
  	  				.set("fin_type", "charge")// 类型是应收
  	  				.set("creator", LoginUserController.getLoginUserId(this))
  	  				.set("create_date", transferOrderFinItem.get("create_date"))
  	  				.set("create_name", transferOrderFinItem.get("create_name"))
  	  				.set("remark", transferOrderFinItem.get("remark"));
  	  				if(deliveryId != 0)
  	  					returnOrderFinItem.set("delivery_order_id", deliveryId);
  	  				if(transferOrderId != 0)
  	  					returnOrderFinItem.set("transfer_order_id", transferOrderId);
  	  				returnOrderFinItem.save();
  	  			//}else{
  	  				//returnOrderFinItems.set("amount", transferOrderFinItem.getDouble("amount") + returnOrderFinItems.getDouble("amount")).update();
  	  			//}
  	  		}
  		}
  	}
    
    
    public void saveFile(){
    	String id = getPara("return_id");
    	String permission = getPara("permission");
    	List<UploadFile> returnImg = getFiles("img");
    	String type = getPara("type");
    	//List<UploadFile> returnImg = getFiles("return_img");
    	//List<UploadFile> returnImg = getFiles();
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	ReturnOrder returnOrder = ReturnOrder.dao.findById(id);
    	boolean result = true;
    	if("return".equals(type)){
    		type = "回单照片";
    	}else{
    		type = "现场照片";
    	}
    	
		if(returnOrder != null){
	    	//for (int i = 0; i < uploadFiles.size(); i++) {
				//File file = uploadFiles.get(i).getFile();
			for (int i = 0; i < returnImg.size(); i++) {
	    		File file = returnImg.get(i).getFile();
	    		String fileName = file.getName();
	    		String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
	    		if("gif".equals(suffix) || "jpeg".equals(suffix) || "png".equals(suffix) || "jpg".equals(suffix)){
        			OrderAttachmentFile orderAttachmentFile = new OrderAttachmentFile();
        			orderAttachmentFile.set("order_id", id);
        			orderAttachmentFile.set("photo_type", type);
        			orderAttachmentFile.set("order_type", orderAttachmentFile.OTFRT_TYPE_RETURN);
        			orderAttachmentFile.set("file_path", returnImg.get(0).getFileName());
        			orderAttachmentFile.save();
	    		}else{
	    			result = false;
	    			break;
	    		}
			}
		}
		if(result){
			List<OrderAttachmentFile> orderAttachmentFileList = null;
			resultMap.put("result", "true");
			if(permission.equals("permissionYes"))
				orderAttachmentFileList = OrderAttachmentFile.dao.find("select * from order_attachment_file where order_id = '" + id + "' and photo_type = '"+type+"';");
			else
				orderAttachmentFileList = OrderAttachmentFile.dao.find("select * from order_attachment_file where order_id = '" + id + "' and audit = true and photo_type = '"+type+"';");
	    	resultMap.put("cause", orderAttachmentFileList);
		}else{
			resultMap.put("result", "false");
	    	resultMap.put("cause", "上传失败，请选择正确的图片文件");
		}
    	renderJson(resultMap);
		//renderJson("OK");
    }
    //删除图片
    public void delPictureById(){
    	String type = getPara("type");
    	String permission = getPara("permission");
    	OrderAttachmentFile.dao.deleteById(getPara("picture_id"));
    	List<OrderAttachmentFile> orderAttachmentFileList = null;
		if(permission.equals("permissionYes"))
			orderAttachmentFileList = OrderAttachmentFile.dao.find("select * from order_attachment_file where order_id = '" + getPara("return_id") + "' and photo_type = '"+type+"';");
		else
			orderAttachmentFileList = OrderAttachmentFile.dao.find("select * from order_attachment_file where order_id = '" + getPara("return_id") + "' and audit = true and photo_type = '"+type+"';");
    	renderJson(orderAttachmentFileList);
    }
    
    //审核图片
    public void auditPictureById(){
    	OrderAttachmentFile file = OrderAttachmentFile.dao.findById(getPara("picture_id"));
		Object obj = file.get("audit");
		if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
			file.set("audit", true);
		}else{
			file.set("audit", false);
		}
		file.update();
        renderJson(file);
    }
    public void updateReturnOrder(){
    	String id =getPara("ids");
    	String name =getPara("name");
    	String value =getPara("value");
    	if(id!=null){
    	TransferOrderItemDetail transferorderutemdetail =TransferOrderItemDetail.dao.findById(id);
    	if(transferorderutemdetail.get("serial_no")!=null){
    		transferorderutemdetail.set(name,value);
    		transferorderutemdetail.update();
    	}
    	}
    	
        renderJson("{\"success\":true}");
    }
   public void refused(){
	   	String id =getPara("id");
	   	ReturnOrder returnOrder = ReturnOrder.dao.findById(id);
	   	java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		Long deliveryId = returnOrder.getLong("delivery_order_id");
		DeliveryOrder delivery=DeliveryOrder.dao.findById(deliveryId);
		DeliveryOrder deliveryOrder = null;
		Date createDate = Calendar.getInstance().getTime();
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
		deliveryOrder = new DeliveryOrder();
		deliveryOrder.set("order_no", delivery.getStr("order_no")+"-1")
		.set("customer_id", delivery.get("customer_id"))
		.set("sp_id", delivery.get("sp_id"))
		.set("notify_party_id", delivery.get("notify_party_id"))
		.set("create_stamp", createDate).set("create_by", users.get(0).getLong("id")).set("status", "新建")
		.set("route_to",delivery.get("route_to"))
		.set("route_from", delivery.get("route_from"))
		.set("pricetype", delivery.get("pricetype"))
		.set("from_warehouse_id", delivery.get("from_warehouse_id"))
		.set("cargo_nature", delivery.get("cargo_nature"))
		.set("priceType", delivery.get("priceType"))
		.set("office_id", delivery.get("office_id"))
		.set("ltl_price_type", delivery.get("ltl_price_type")).set("car_type", delivery.get("car_type"))
		.set("audit_status", "新建").set("sign_status", "未回单");
		deliveryOrder.save();
		returnOrder.set("transaction_status", "已拒收").set("receipt_date", sqlDate).update();;
		returnOrder.update();
		List<DeliveryOrderItem> deliveryItem =DeliveryOrderItem.dao.find("SELECT * from delivery_order_item where delivery_id=?",deliveryId);
		for(int i=0;i<deliveryItem.size();i++){
			DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
			deliveryOrderItem.set("delivery_id",deliveryOrder.get("id"))
			.set("transfer_order_id",deliveryItem.get(i).get("transfer_order_id"))
			.set("transfer_no",deliveryItem.get(i).get("transfer_no"))
			.set("transfer_item_detail_id",deliveryItem.get(i).get("transfer_item_detail_id"))
			.set("amount", deliveryItem.get(i).get("amount"));
			deliveryOrderItem.save();
			if(deliveryItem.get(i).get("transfer_item_detail_id")!=null){
				TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
						.findById(deliveryItem.get(i).getLong("transfer_item_detail_id"));
				transferOrderItemDetail.set("delivery_refused_id",deliveryOrder.get("id"));
				transferOrderItemDetail.update();
			}
		}
		 renderJson("{\"success\":true}");
   }
   
   public void download() throws IOException{
       String order_no = getPara("order_no")==null?"":getPara("order_no").trim();
       String tr_order_no = getPara("tr_order_no")==null?"":getPara("tr_order_no").trim();
       String de_order_no = getPara("de_order_no")==null?"":getPara("de_order_no").trim();

       String status = getPara("status")==null?"":getPara("status").trim();
       String time_one = getPara("time_one")==null?"":getPara("time_one").trim();
       String time_two = getPara("time_two")==null?"":getPara("time_two").trim();
       String customer = getPara("customer")==null?"":getPara("customer").trim();
       String return_type = getPara("return_type")==null?"":getPara("return_type").trim();
       String transfer_type = getPara("transfer_type")==null?"":getPara("transfer_type").trim();
       String warehouse = getPara("warehouse")==null?"":getPara("warehouse").trim();
       String serial_no = getPara("serial_no")==null?"":getPara("serial_no").trim();
       String to_name = getPara("to_name")==null?"":getPara("to_name").trim();
       String province = getPara("province")==null?"":getPara("province").trim();
       String imgaudit = getPara("imgaudit")==null?"":getPara("imgaudit").trim();
       String photo_type = getPara("photo_type")==null?"":getPara("photo_type").trim();
       String sign_no = getPara("sign_no")==null?"":getPara("sign_no").trim();
       String officeSelect = getPara("officeSelect")==null?"":getPara("officeSelect").trim();
       String officeSelect2 = getPara("officeSelect2")==null?"":getPara("officeSelect2").trim();
       String delivery_date_begin_time = getPara("delivery_date_begin_time")==null?"":getPara("delivery_date_begin_time").trim();
       String delivery_date_end_time = getPara("delivery_date_end_time")==null?"":getPara("delivery_date_end_time").trim();
       String q_begin = getPara("q_begin")==null?"":getPara("q_begin").trim();
       String q_end = getPara("q_end")==null?"":getPara("q_end").trim();

       
       String sql = "";
       String sortColIndex = getPara("iSortCol_0");
       String sortBy = getPara("sSortDir_0");
       String colName = getPara("mDataProp_"+sortColIndex);
       Map orderMap = new HashMap();

       String conditions=" where 1=1 ";
       if (StringUtils.isNotEmpty(order_no)){
           conditions+=" and UPPER(order_no) like '%"+order_no+"%'";
       }
       if (StringUtils.isNotEmpty(imgaudit)){
           conditions+=" and UPPER(imgaudit) like '%"+imgaudit+"%'";
       }
       if (StringUtils.isNotEmpty(photo_type)){
           conditions+=" and photo_type = '"+photo_type+"'";
       }
       if (StringUtils.isNotEmpty(tr_order_no)){
           conditions+=" and UPPER(transfer_order_no) like '%"+tr_order_no+"%'";
       }
       if (StringUtils.isNotEmpty(de_order_no)){
           conditions+=" and UPPER(delivery_order_no) like '%"+de_order_no+"%'";
       }
       if (StringUtils.isNotEmpty(status)){
           conditions+=" and UPPER(transaction_status) in(" + status+")";
       }   
       if (StringUtils.isNotEmpty(sign_no)){
           conditions+=" and UPPER(sign_no) like '%"+sign_no+"%'";
       }
       if (StringUtils.isNotEmpty(serial_no)){
           conditions+=" and UPPER(serial_no) like '%"+serial_no+"%'";
       }
       if (StringUtils.isNotEmpty(customer)){
           conditions+=" and UPPER(customer_id) = '"+customer+"'";
       }
       if (StringUtils.isNotEmpty(return_type)){
           conditions+=" and UPPER(return_type) like '%"+return_type+"%'";
       }
       if (StringUtils.isNotEmpty(warehouse)){
           conditions+=" and UPPER(warehouse_name) like '%"+warehouse+"%'";
       }
       if (StringUtils.isNotEmpty(province)){
           conditions+=" and UPPER(province) like '%"+province+"%'";
       }
       if (StringUtils.isNotEmpty(to_name)){
           conditions+=" and UPPER(to_name) like '%"+to_name+"%'";
       }
       if (StringUtils.isNotEmpty(return_type)){
           conditions+=" and UPPER(return_type) like '%"+return_type+"%'";
       }
       if (StringUtils.isNotEmpty(transfer_type)){
           conditions+=" and UPPER(transfer_type) like '%"+transfer_type+"%'";
       }
       if(StringUtils.isNotEmpty(officeSelect)){
           conditions+=" and dor_office_id = '" + officeSelect+"'";
       }
       if(StringUtils.isNotEmpty(officeSelect2)){
           conditions+=" and tor_office_id = '" + officeSelect2+"'";
       }
//       if (StringUtils.isNotEmpty(time_one)){
//           time_one = " and planning_time between'"+time_one+"'";
//       }else{
//           time_one =" and planning_time between '2000-1-1'";
//       }
//       if (StringUtils.isNotEmpty(time_two)){
//           time_two =" and '"+time_two+"'";
//       }else{
//           time_two =" and '2050-1-1'";
//       }
//       conditions += time_one + time_two;
       
       
       if(StringUtils.isNotEmpty(delivery_date_begin_time) || StringUtils.isNotEmpty(delivery_date_end_time)){
           if (!StringUtils.isNotEmpty(delivery_date_begin_time)){
               delivery_date_begin_time = "2000-01-01";
           }
           if (StringUtils.isNotEmpty(delivery_date_end_time)){
               delivery_date_end_time+= " 23:23:59";
           }else{
               delivery_date_end_time = "2037-12-31";
           }
           conditions += " and business_stamp between '"+ delivery_date_begin_time+ "' and '" + delivery_date_end_time + "' ";
       }
       
       if (StringUtils.isNotEmpty(q_begin)){
           q_begin = " and receipt_date between'"+q_begin+"'";
       }else{
           q_begin =" and receipt_date between '2000-1-1'";
       }
       if (StringUtils.isNotEmpty(q_end)){
           q_end =" and '"+q_end+"'";
       }else{
           q_end =" and '2050-1-1'";
       }
       if(!status.equals("'新建'"))
           conditions += q_begin + q_end;
       
       // 获取当前页的数据
       sql = " SELECT ror.id, ror.order_no, af.id file_id, af.file_path, ror.customer_id,'' create_date,'' remark ,"
                   + " '' transfer_type,"
                   + " '' planning_time,"
                   + " ( CASE"
                   + " WHEN ror.delivery_order_id IS NOT NULL "
                   + " THEN ( SELECT group_concat( DISTINCT toid.serial_no SEPARATOR '<br/>' )"
                   + " FROM transfer_order_item_detail toid"
                   + " WHERE toid.delivery_id = dor.id )"
                   + " ELSE ( SELECT group_concat( DISTINCT toid.serial_no SEPARATOR '<br/>' )"
                   + " FROM transfer_order_item_detail toid"
                   + " WHERE toid.order_id = tor.id ) END "
                   + " ) serial_no,"
                   + " ( CASE"
                   + " WHEN ror.delivery_order_id IS NOT NULL THEN"
                   + " ( SELECT group_concat( toid.item_no SEPARATOR '<br/>' )"
                   + " FROM transfer_order_item_detail toid"
                   + " WHERE toid.delivery_id = dor.id )"
                   + " ELSE ( SELECT group_concat( toi.item_no SEPARATOR '<br/>' )"
                   + " FROM transfer_order_item toi "
                   + " WHERE toi.order_id = ror.transfer_order_id ) END ) item_no,"
                   + " ifnull( c.contact_person, tor.receiving_name ) receipt_person,"
                   + " ifnull( c.phone, tor.receiving_phone ) receipt_phone,"
                   + " ifnull( (c.company_name), tor.receiving_unit ) receiving_unit,"
                   + " ifnull( c.address, tor.receiving_address ) receipt_address,"
                   + " ifnull( w.warehouse_name, w1.warehouse_name ) warehouse_name,"
                   + " ifnull(( SELECT sum(amount) FROM delivery_order_item doi "
                   + " WHERE doi.delivery_id = dor.id ), "
                   + " (SELECT sum(amount) FROM transfer_order_item "
                   + " WHERE order_id = tor.id) ) a_amount,  c2.abbr cname,"
                   + " ifnull( tor.order_no, "
                   + " ( SELECT group_concat( DISTINCT CAST(tor.order_no AS CHAR) SEPARATOR '<br/>' )"
                   + " FROM transfer_order tor"
                   + " LEFT JOIN delivery_order_item doi ON doi.transfer_order_id = tor.id"
                   + " WHERE doi.delivery_id = dor.id )"
                   + " ) transfer_order_no,"
                   + " lo. NAME from_name, lo2. NAME to_name,"
                   + " ifnull( ( SELECT NAME FROM location WHERE CODE = lo2.pcode AND pcode = 1 ),"
                   + " ( SELECT l. NAME FROM location l"
                   + " LEFT JOIN location lo3 ON lo3.pcode = l. CODE"
                   + "  WHERE lo3. CODE = lo2.pcode AND l.pcode = 1 )"
                   + " ) province,"
                   + " ifnull( tor.address, ( SELECT group_concat( DISTINCT CAST(tor.address AS CHAR) SEPARATOR '<br/>' )"
                   + " FROM transfer_order tor"
                   + " LEFT JOIN delivery_order_item doi ON doi.transfer_order_id = tor.id"
                   + " WHERE doi.delivery_id = dor.id )"
                   + " ) address, dor.order_no delivery_order_no, ifnull(ul.c_name, ul.user_name) creator_name,"
                   + " ror.receipt_date, ror.transaction_status, ( SELECT CASE WHEN ( SELECT count(0) FROM order_attachment_file"
                   + " WHERE order_type = 'RETURN' AND order_id = ror.id ) = 0 THEN '无图片'"
                   + " WHEN ( SELECT count(0) FROM order_attachment_file"
                   + " WHERE order_type = 'RETURN' AND order_id = ror.id AND (audit = 0 OR audit IS NULL) and file_path is not null ) > 0 THEN '有图片待审核'"
                   + " ELSE '有图片已审核' END ) imgaudit,"
                   + " af.photo_type, "
                   + " dor.ref_no sign_no," 
                   + " dor.office_id dor_office_id,"
                   + " tor.office_id tor_office_id,"
                   + " o.office_name dor_office_name,"
                   + " dor.business_stamp"
                   + " FROM return_order ror"
                   + " left join order_attachment_file af on ror.id = af.order_id and af.order_type='RETURN'"
                   + " LEFT JOIN transfer_order tor ON tor.id = ror.transfer_order_id"
                   + " LEFT JOIN delivery_order dor ON dor.id = ror.delivery_order_id"
                   + " LEFT JOIN office o ON dor.office_id = o.id "
                   + " LEFT JOIN party p ON p.id = dor.notify_party_id"
                   + " LEFT JOIN contact c ON c.id = p.contact_id"
                   + " LEFT JOIN warehouse w ON tor.warehouse_id = w.id"
                   + " LEFT JOIN warehouse w1 ON ifnull( dor.change_warehouse_id, dor.from_warehouse_id ) = w1.id"
                   + " LEFT JOIN party p2 ON p2.id = ror.customer_id"
                   + " LEFT JOIN contact c2 ON c2.id =p2.id"
                   + " LEFT JOIN location lo ON lo. CODE = ifnull(tor.route_from, dor.route_from )"
                   + " LEFT JOIN location lo2 ON lo2. CODE = ifnull(tor.route_to, dor.route_to)"
                   + " LEFT JOIN user_login ul ON ul.id = ror.creator "
                   + " where "
                   + " ifnull(tor.office_id,dor.office_id) IN ( SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"' )"
                   + " and ror.customer_id in (select customer_id from user_customer where user_name='" 
                   + currentUser.getPrincipal() + "'"+") and file_path is not null" ;

       String orderByStr = " order by id asc ";
       
       List<Record> orders = Db.find(" SELECT  *  from(" + sql + ") A" + conditions + orderByStr);    
       logger.debug("total = "+orders.size());
       if(orders.size()>0){
           String contextPath = getRequest( ).getSession().getServletContext().getRealPath("/");
           String uuid = UUID.randomUUID().toString().substring(0, 4);
           (new File(contextPath +"/download/"+uuid+"_return_pics")).mkdirs();//创建临时目录
           
           for (Record record : orders) {
               String serialNo= record.getStr("serial_no");
               String type= record.getStr("photo_type");
               String fileName = record.getStr("file_path");
               
               String filePostFix = fileName.substring(fileName.indexOf("."));
               logger.debug("copying file: "+ contextPath +"/upload/img/"+ fileName);
               File sourceFile = new File(contextPath +"/upload/img/"+ fileName);
               File targetFile = new File(contextPath +"/download/"+uuid+"_return_pics/"+ serialNo+"_"+type+"_"+record.getLong("file_id")+filePostFix);
               FileUtil.copyFile(sourceFile, targetFile);
           }
           String zipFileName = zipOutput(uuid+"_return_pics");
//           FileUtil.del(contextPath +"/download/"+uuid+"_return_pics");//TODO: 删除临时目录
           
           renderText(zipFileName);
       }else{
           renderText("noFile");
       }
   }
   
   private String zipOutput(String folder_name) throws IOException {
       String contextPath = getRequest( ).getSession().getServletContext().getRealPath("/");
       String path = contextPath+"/download/";;
//       String folder_name = "return_pics";
        // 要被压缩的文件夹  
       File file = new File(path+folder_name);  
       File zipFile = new File(path+folder_name + ".zip");   
       ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(  
               zipFile));  
       zipOut.setEncoding("GBK"); 
       if(file.isDirectory()){  
          InputStream input = null; 
          File[] files = file.listFiles();  
          logger.debug("folder files length: "+ files.length);
           for(int i = 0; i < files.length; ++i){  
               input = new FileInputStream(files[i]);  
               zipOut.putNextEntry(new ZipEntry(file.getName()  
                       + File.separator + files[i].getName()));  
               int temp = 0;  
               while((temp = input.read()) != -1){  
                   zipOut.write(temp);  
               }  
               input.close();  
           }
       }  
       StringBuffer buffer = new StringBuffer();
       String strFile=zipFile.getPath();
       buffer.append(strFile.substring(strFile.indexOf("download")-1));
//       buffer.append(",");
       zipOut.close(); 
       logger.debug(buffer.toString());
       return buffer.toString();
   }
   
	public void charge_amount_list(){
		String return_id = getPara("return_id");
		//先判断运单产品类型
		Record re = Db.findFirst("select tor.* from return_order ror "
				+ " LEFT JOIN delivery_order dor on dor.id = ror.delivery_order_id and ror.delivery_order_id is not null "
				+ " LEFT JOIN delivery_order_item doi on doi.delivery_id = dor.id "
				+ " LEFT JOIN transfer_order tor on tor.id = ifnull(ror.transfer_order_id,doi.transfer_order_id) "
				+ " where ror.id = ?", return_id);
		String cargo_nature = re.getStr("cargo_nature");
		String cargo_nature_detail = re.getStr("cargo_nature_detail");
		String arrival_mode = re.getStr("arrival_mode");
		
		String sql = "";
		if("ATM".equals(cargo_nature)){
			//atm
			if("delivery".equals(arrival_mode)){
				sql = "select toid.*,tor.order_no from transfer_order_item_detail toid"
						+ " LEFT JOIN transfer_order tor on tor.id = toid.order_id "
						+ " LEFT JOIN return_order ror on ror.transfer_order_id = tor.id "
						+ " where ror.id = ? and toid.charge_amount > 0"
						+ " GROUP BY toid.id";
			}else{
				sql = "select toid.*,tor.order_no from transfer_order_item_detail toid "
						+ " LEFT JOIN transfer_order tor on tor.id = toid.order_id "
						+ " LEFT JOIN delivery_order_item doi on doi.transfer_item_detail_id = toid.id "
						+ " LEFT JOIN delivery_order dor on dor.id = doi.delivery_id "
						+ " LEFT JOIN return_order ror on ror.delivery_order_id = dor.id "
						+ " where ror.id = ?  and toid.charge_amount > 0"
						+ " GROUP BY toid.id";
			}
		}else{
			if("cargoNatureDetailYes".equals(cargo_nature_detail)){
				//atm
				if("delivery".equals(arrival_mode)){
					sql = "select toid.*,tor.order_no from transfer_order_item_detail toid"
							+ " LEFT JOIN transfer_order tor on tor.id = toid.order_id "
							+ " LEFT JOIN return_order ror on ror.transfer_order_id = tor.id "
							+ " where ror.id = ? and toid.charge_amount > 0"
							+ "  GROUP BY toid.id";
				}else{
					sql = "select toid.*,tor.order_no from transfer_order_item_detail toid "
							+ " LEFT JOIN transfer_order tor on tor.id = toid.order_id "
							+ " LEFT JOIN delivery_order_item doi on doi.transfer_item_detail_id = toid.id "
							+ " LEFT JOIN delivery_order dor on dor.id = doi.delivery_id "
							+ " LEFT JOIN return_order ror on ror.delivery_order_id = dor.id "
							+ " where ror.id = ?  and toid.charge_amount > 0"
							+ " GROUP BY toid.id";
				}
			}else{
				//cargo
				if("delivery".equals(arrival_mode)){
					sql = "select toi.*,tor.order_no from transfer_order_item toi"
							+ " LEFT JOIN transfer_order tor on tor.id = toi.order_id "
							+ " LEFT JOIN return_order ror on ror.transfer_order_id = tor.id "
							+ " where ror.id = ? and toi.charge_amount > 0"
							+ " GROUP BY toi.id";
				}else{
					sql = "select toi.*,tor.order_no from transfer_order_item toi "
							+ " LEFT JOIN transfer_order tor on tor.id = toi.order_id "
							+ " LEFT JOIN delivery_order_item doi on doi.transfer_item_id = toi.id "
							+ " LEFT JOIN delivery_order dor on dor.id = doi.delivery_id "
							+ " LEFT JOIN return_order ror on ror.delivery_order_id = dor.id "
							+ " where ror.id = ? and toi.charge_amount > 0"
							+ " GROUP BY toi.id";
				}
			}
		}
		
		String pageIndex = getPara("sEcho");
		Record rec = Db.findFirst("select count(*) total from ("+sql+") A", return_id);
		List<Record> transferOrders = Db.find(sql, return_id);
		Map transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);

		renderJson(transferOrderListMap);
	}
}
