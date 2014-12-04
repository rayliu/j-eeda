package controllers.yh.returnOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.Fin_item;
import models.Location;
import models.Party;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.contract.Contract;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;
import models.yh.returnOrder.ReturnOrderFinItem;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;
import controllers.yh.util.PermissionConstant;

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
		String order_no = getPara("order_no");
		String tr_order_no = getPara("tr_order_no");
		String de_order_no = getPara("de_order_no");
		String stator = getPara("stator");
		String status = getPara("status");
		String time_one = getPara("time_one");
		String time_two = getPara("time_two");
		String customer = getPara("customer");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map orderMap = new HashMap();
		if (order_no == null && tr_order_no == null && de_order_no == null
				&& stator == null && status == null && time_one == null
				&& time_two == null && customer == null) {
			// 获取总条数
			String totalWhere = "";
			String sql = "select count(1) total from return_order ro ";
			Record rec = Db.findFirst(sql + totalWhere);
			logger.debug("total records:" + rec.getLong("total"));

			// 获取当前页的数据
			List<Record> orders = Db
					.find("select distinct r_o.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id where r_o.delivery_order_id = dor.id)) transfer_order_no, d_o.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname"
							+ " from return_order r_o "
							+ " left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id  "
							+ " left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join delivery_order_item doi on doi.delivery_id = d_o.id "
							+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=r_o.creator "
							+ " order by r_o.create_date desc " + sLimit);

			orderMap.put("sEcho", pageIndex);
			orderMap.put("iTotalRecords", rec.getLong("total"));
			orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
			orderMap.put("aaData", orders);

		} else {
			if (time_one == null || "".equals(time_one)) {
				time_one = "1-1-1";
			}
			if (time_two == null || "".equals(time_two)) {
				time_two = "9999-12-31";
			}

			// 获取总条数
			String totalWhere = "";
			String sql = "select count(distinct r_o.id) total from return_order r_o "
					+ " left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id  "
					+ " left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join delivery_order_item doi on doi.delivery_id = d_o.id "
					+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=r_o.creator "
					+ " where ifnull(r_o.order_no,'')  like'%"
					+ order_no
					+ "%' and  "
					+ "ifnull(tor.order_no,tor2.order_no)  like'%"
					+ tr_order_no
					+ "%'  and  "
					+ "ifnull(d_o.order_no,'')  like'%"
					+ de_order_no
					+ "%'  and "
					+ "ifnull(r_o.transaction_status ,'')  like'%"
					+ status
					+ "%' and "
					+ "ifnull(usl.user_name ,'')  like'%"
					+ stator
					+ "%'"
					+ " and ifnull(c.abbr,c2.abbr) like '%"
					+ customer
					+ "%' and "
					+ "r_o.create_date between '"
					+ time_one + "' and '" + time_two + "'";
			Record rec = Db.findFirst(sql + totalWhere);
			logger.debug("total records:" + rec.getLong("total"));

			// 获取当前页的数据
			List<Record> orders = Db
					.find("select distinct r_o.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(distinct tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id where r_o.delivery_order_id = dor.id)) transfer_order_no, d_o.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname"
							+ " from return_order r_o "
							+ " left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id  "
							+ " left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join delivery_order_item doi on doi.delivery_id = d_o.id "
							+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=r_o.creator "
							+ " where ifnull(r_o.order_no,'')  like'%"
							+ order_no
							+ "%' and  "
							+ "ifnull(tor.order_no,tor2.order_no)  like'%"
							+ tr_order_no
							+ "%'  and  "
							+ "ifnull(d_o.order_no,'')  like'%"
							+ de_order_no
							+ "%'  and "
							+ "ifnull(r_o.transaction_status ,'')  like'%"
							+ status
							+ "%' and "
							+ "ifnull(usl.user_name ,'')  like'%"
							+ stator
							+ "%' and ifnull(c.abbr,c2.abbr) like '%"
							+ customer
							+ "%' and "
							+ "r_o.create_date between '"
							+ time_one
							+ "' and '"
							+ time_two
							+ "' order by r_o.create_date desc " + sLimit);

			orderMap.put("sEcho", pageIndex);
			orderMap.put("iTotalRecords", rec.getLong("total"));
			orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
			orderMap.put("aaData", orders);
		}

		renderJson(orderMap);
	}

	// 点击查看
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RO_UPDATE})
	public void edit() {
		ReturnOrder returnOrder = ReturnOrder.dao.findById(getPara("id"));
		TransferOrder transferOrder = null;
		Long deliveryId = returnOrder.get("delivery_order_id");
		Long transferOrderId = returnOrder.get("transfer_order_id");
		Long notify_party_id;
		String code = "";
		String routeTo = "";

		if (deliveryId == null) {
			transferOrder = TransferOrder.dao.findById(transferOrderId);
			notify_party_id = transferOrder.get("notify_party_id");
			routeTo = transferOrder.get("route_to");
		} else {
			DeliveryOrder deliveryOrder = DeliveryOrder.dao
					.findById(deliveryId);
			// TODO 一张配送单对应多张运输单时回单怎样取出信息
			routeTo = deliveryOrder.get("route_to");
			List<DeliveryOrderItem> deliveryOrderItems = DeliveryOrderItem.dao
					.find("select * from delivery_order_item where delivery_id = ?",
							deliveryId);
			for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItems) {
				transferOrder = TransferOrder.dao.findById(deliveryOrderItem
						.get("transfer_order_id"));
				break;
			}
			notify_party_id = deliveryOrder.get("notify_party_id");
		}
		setAttr("transferOrder", transferOrder);

		Long customer_id = transferOrder.get("customer_id");
		if (customer_id != null) {
			Party customer = Party.dao.findById(customer_id);
			Contact customerContact = Contact.dao.findById(customer
					.get("contact_id"));
			setAttr("customerContact", customerContact);
		}
		if (notify_party_id != null) {
			Party notify = Party.dao.findById(notify_party_id);
			Contact contact = Contact.dao.findById(notify.get("contact_id"));
			setAttr("contact", contact);
			Contact locationCode = Contact.dao.findById(notify
					.get("contact_id"));
			code = locationCode.get("location");
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

		String routeFrom = transferOrder.get("route_from");
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

		Location locationTo = null;
		if (routeTo != null || !"".equals(routeTo)) {
			List<Location> provinces = Location.dao
					.find("select * from location where pcode ='1'");
			Location l = Location.dao
					.findFirst("select * from location where code = (select pcode from location where code = '"
							+ routeTo + "')");
			if (provinces.contains(l)) {
				locationTo = Location.dao
						.findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
								+ routeTo + "'");
			} else {
				locationTo = Location.dao
						.findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
								+ routeTo + "'");
			}
			setAttr("locationTo", locationTo);
		}

		setAttr("returnOrder", returnOrder);
		UserLogin userLogin = UserLogin.dao
				.findById(returnOrder.get("creator"));
		setAttr("userLogin", userLogin);
		UserLogin userLoginTo = UserLogin.dao.findById(transferOrder
				.get("create_by"));
		setAttr("userLoginTo", userLoginTo);
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);
		render("/yh/returnOrder/returnOrder.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RO_UPDATE})
	public void save() {
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
		ReturnOrder returnOrder = ReturnOrder.dao.findById(getPara("id"));
		Long deliveryId = returnOrder.get("delivery_order_id");
		String routeTo = getPara("route_to");
		boolean isLocationChanged = getParaToBoolean("locationChanged");
		Long notifyPartyId;
		if (deliveryId == null) {
			// 直送
			TransferOrder transferOrder = TransferOrder.dao
					.findById(returnOrder.get("transfer_order_id"));
			if (!"".equals(routeTo) && routeTo != null) {
				transferOrder.set("route_to", routeTo);
			}
			transferOrder.update();
			notifyPartyId = transferOrder.get("notify_party_id");
			if (notifyPartyId != null) {
				updateContact(notifyPartyId);
			}
			// 如果目的地发生变化，保存时先删除以前计算的应收，再重新计算合同应收
			if (isLocationChanged) {
				deleteContractFinItemByTransfer(transferOrder);
			}
			// 计算配送单的触发的应收
			calculateChargeByCustomer(transferOrder, returnOrder.getLong("id"), users);
		} else {
			// 非直送
			DeliveryOrder deliveryOrder = DeliveryOrder.dao
					.findById(deliveryId);
			if (!"".equals(routeTo) && routeTo != null) {
				deliveryOrder.set("route_to", routeTo);
			}
			deliveryOrder.update();
			notifyPartyId = deliveryOrder.get("notify_party_id");
			if (notifyPartyId != null) {
				updateContact(notifyPartyId);
			}
			// 如果目的地发生变化，保存时先删除以前计算的应收，再重新计算合同应收
			if (isLocationChanged) {
				deleteContractFinItem(deliveryOrder);
			}
			// 计算配送单的触发的应收
			/*List<Record> transferOrderItemDetailList = Db.
					find("select toid.* from transfer_order_item_detail toid left join delivery_order_item doi on toid.id = doi.transfer_item_detail_id where doi.delivery_id = ?", delivery_id);
	        calculateCharge(users, deliveryOrder, returnOrder.getLong("id"), transferOrderItemDetailList);*/
		}
		returnOrder.set("remark", getPara("remark"));
		returnOrder.update();
		renderJson(returnOrder);

	}

	private void deleteContractFinItem(DeliveryOrder deliveryOrder) {
		Long customerId = deliveryOrder.getLong("customer_id");
		// 先获取有效期内的客户合同, 如有多个，默认取第一个
		Contract customerContract = Contract.dao
				.findFirst("select * from contract where type='CUSTOMER' "
						+ "and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="
						+ customerId);
		if (customerContract == null)
			return;

		Db.update("delete from return_order_fin_item where contract_id="
				+ customerContract.getLong("id"));
	}
	
	private void deleteContractFinItemByTransfer(TransferOrder deliveryOrder) {
		Long customerId = deliveryOrder.getLong("customer_id");
		// 先获取有效期内的客户合同, 如有多个，默认取第一个
		Contract customerContract = Contract.dao
				.findFirst("select * from contract where type='CUSTOMER' "
						+ "and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="
						+ customerId);
		if (customerContract == null)
			return;
		
		Db.update("delete from return_order_fin_item where contract_id="
				+ customerContract.getLong("id"));
	}

	// 更新收货人信息
	private void updateContact(Long notifyPartyId) {
		Party party = Party.dao.findById(notifyPartyId);
		Contact contact = Contact.dao.findById(party.get("contact_id"));
		contact.set("company_name", getPara("company_name"));
		contact.set("address", getPara("address"));
		contact.set("contact_person", getPara("contact_person"));
		contact.set("phone", getPara("phone"));
		contact.update();
	}

	// 回单签收
	public void returnOrderReceipt() {
		String id = getPara();
		ReturnOrder returnOrder = ReturnOrder.dao.findById(id);
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		returnOrder.set("transaction_status", "已签收")
				.set("receipt_date", sqlDate).update();
		Long deliveryId = returnOrder.get("delivery_order_id");
		if (deliveryId != null && !"".equals(deliveryId)) {
			DeliveryOrder deliveryOrder = DeliveryOrder.dao
					.findById(returnOrder.get("delivery_order_id"));
			deliveryOrder.set("status", "已签收");
			deliveryOrder.update();

			DeliveryOrderMilestone transferOrderMilestone = new DeliveryOrderMilestone();
			transferOrderMilestone.set("status", "已签收");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");
			transferOrderMilestone.set("create_by", users.get(0).get("id"));
			transferOrderMilestone.set("location", "");
			utilDate = new java.util.Date();
			sqlDate = new java.sql.Timestamp(utilDate.getTime());
			transferOrderMilestone.set("create_stamp", sqlDate);
			transferOrderMilestone.set("delivery_id", deliveryOrder.get("id"));
			// transferOrderMilestone.set("type",
			// TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
			transferOrderMilestone.save();

		} else {
			TransferOrder transferOrder = TransferOrder.dao
					.findById(returnOrder.get("transfer_order_id"));
			transferOrder.set("status", "已签收");
			transferOrder.update();

			TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
			transferOrderMilestone.set("status", "已签收");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");
			transferOrderMilestone.set("create_by", users.get(0).get("id"));
			transferOrderMilestone.set("location", "");
			utilDate = new java.util.Date();
			sqlDate = new java.sql.Timestamp(utilDate.getTime());
			transferOrderMilestone.set("create_stamp", sqlDate);
			transferOrderMilestone.set("order_id", transferOrder.get("id"));
			transferOrderMilestone.set("type",
					TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
			transferOrderMilestone.save();
		}
		renderJson("{\"success\":true}");
	}

	public void calculateCharge(List<UserLogin> users, DeliveryOrder deliveryOrder, Long returnOrderId, List<Record> transferOrderItemDetailList) {
		// TODO 运输单的计费类型,当一张配送单对应多张运输单时chargeType如何处理?
		//String chargeType = "perUnit";
		List<DeliveryOrderItem> deliveryOrderItems = DeliveryOrderItem.dao.find("select * from delivery_order_item where delivery_id = ?", deliveryOrder.get("id"));
		TransferOrder transferOrder = TransferOrder.dao.findById(deliveryOrderItems.get(0).get("transfer_order_id"));
		String chargeType = transferOrder.get("charge_type");

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
            genFinPerUnit(customerContract, chargeType, deliveryOrder.getLong("id"), transferOrder, returnOrderId);
        } else if ("perCar".equals(chargeType)) {
        	genFinPerCar(customerContract, chargeType, deliveryOrder, transferOrder, returnOrderId);
        } else if ("perCargo".equals(chargeType)) {
        	//每次都新生成一个helper来处理计算，防止并发问题。
            ReturnOrderPaymentHelper.getInstance().genFinPerCargo(users, deliveryOrder, transferOrderItemDetailList, customerContract, chargeType, returnOrderId, transferOrder);
        } 
	}

    private void genFinPerCar(Contract spContract, String chargeType, DeliveryOrder deliverOrder, TransferOrder transferOrder, Long returnOrderId) {
        Long deliverOrderId = deliverOrder.getLong("id");
    
        Record contractFinItem = Db
                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                        +" and carType = '" + transferOrder.get("car_type") +"' "
                        +" and carlength = " + deliverOrder.get("car_size")
                        +" and from_id = '"+ transferOrder.get("route_from")
                        +"' and to_id = '"+ deliverOrder.get("route_to")
                        + "' and priceType='"+chargeType+"'");
        
        if (contractFinItem != null) {
            genFinItem(deliverOrderId, null, contractFinItem, chargeType, returnOrderId);
        }else{
            contractFinItem = Db
                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                            +" and cartype = '" + transferOrder.get("car_type") +"' "
                            +" and from_id = '"+ transferOrder.get("route_from")
                            +"' and to_id = '"+ deliverOrder.get("route_to")
                            + "' and priceType='"+chargeType+"'");
            
            if (contractFinItem != null) {
            	genFinItem(deliverOrderId, null, contractFinItem, chargeType, returnOrderId);
            }else{
                contractFinItem = Db
                        .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                +" and from_id = '"+ transferOrder.get("route_from")
                                +"' and to_id = '"+ deliverOrder.get("route_to")
                                + "' and priceType='"+chargeType+"'");
                
                if (contractFinItem != null) {
                	genFinItem(deliverOrderId, null, contractFinItem, chargeType, returnOrderId);
                }else{
                    contractFinItem = Db
                            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                    +" and to_id = '"+ deliverOrder.get("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    
                    if (contractFinItem != null) {
                    	genFinItem(deliverOrderId, null, contractFinItem, chargeType, returnOrderId);
                    }
                }
            }
        }        
    } 
    
    private void genFinPerUnit(Contract spContract, String chargeType, Long deliverOrderId, TransferOrder transferOrder, Long returnOrderId) {
        List<Record> deliveryOrderItemList = Db
                .find("SELECT count(1) amount, toi.product_id, d_o.route_from, d_o.route_to FROM "+
					    "delivery_order_item doi LEFT JOIN transfer_order_item_detail toid ON doi.transfer_item_detail_id = toid.id "+
					        "LEFT JOIN transfer_order_item toi ON toid.item_id = toi.id "+
					        "LEFT JOIN delivery_order d_o ON doi.delivery_id = d_o.id "+
                		"WHERE  doi.delivery_id = "+ deliverOrderId+" group by toi.product_id, d_o.route_from, d_o.route_to" );
        for (Record dOrderItemRecord : deliveryOrderItemList) {
            Record contractFinItem = Db
                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                            + " and product_id ="+dOrderItemRecord.get("product_id")
                            +" and from_id = '"+ transferOrder.get("route_from")
                            +"' and to_id = '"+ dOrderItemRecord.get("route_to")
                            + "' and priceType='"+chargeType+"'");
            
            if (contractFinItem != null) {
                genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId);
            }else{
                contractFinItem = Db
                        .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                + " and product_id ="+dOrderItemRecord.get("product_id")
                                +" and to_id = '"+ dOrderItemRecord.get("route_to")
                                + "' and priceType='"+chargeType+"'");
                
                if (contractFinItem != null) {
                	genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId);
                }else{
                    contractFinItem = Db
                            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                    +" and from_id = '"+ transferOrder.get("route_from")
                                    +"' and to_id = '"+ dOrderItemRecord.get("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    
                    if (contractFinItem != null) {
                    	genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId);
                    }else{
                        contractFinItem = Db
                                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                        +" and to_id = '"+ dOrderItemRecord.get("route_to")
                                        +"' and priceType='"+chargeType+"'");
                        
                        if (contractFinItem != null) {
                        	genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId);
                        }
                    }
                }
            }
        }
    }

	private void genFinItem(Long deliveryOrderId, Record tOrderItemRecord, Record contractFinItem, String chargeType, Long returnOrderId) {
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
		ReturnOrderFinItem returnOrderFinItem = new ReturnOrderFinItem();
		returnOrderFinItem.set("fin_item_id", contractFinItem.get("fin_item_id"));
		if("perCar".equals(chargeType)){
			returnOrderFinItem.set("amount", contractFinItem.getDouble("amount"));        		
    	}else{
    		if(tOrderItemRecord != null){
    			returnOrderFinItem.set("amount", contractFinItem.getDouble("amount") * Double.parseDouble(tOrderItemRecord.get("amount").toString()));
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
		
		returnOrderFinItem.save();
	}
	
	private void calcRevenuePerCar(Contract spContract, String chargeType, TransferOrder transferOrder, Long returnOrderId) {
        Record contractFinItem = Db
                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                        +" and carType = '" + transferOrder.get("car_type") +"' "
                        +" and carlength = " + transferOrder.get("car_size")
                        +" and from_id = '"+ transferOrder.get("route_from")
                        +"' and to_id = '"+ transferOrder.get("route_to")
                        + "' and priceType='"+chargeType+"'");
        
        if (contractFinItem != null) {
            genFinItem2(transferOrder.getLong("id"), null, contractFinItem, chargeType, returnOrderId);
        }else{
            contractFinItem = Db
                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                            +" and carType = '" + transferOrder.get("car_type") +"' "
                            +" and from_id = '"+ transferOrder.get("route_from")
                            +"' and to_id = '"+ transferOrder.get("route_to")
                            + "' and priceType='"+chargeType+"'");
            
            if (contractFinItem != null) {
            	genFinItem2(transferOrder.getLong("id"), null, contractFinItem, chargeType, returnOrderId);
            }else{
                contractFinItem = Db
                        .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                +" and from_id = '"+ transferOrder.get("route_from")
                                +"' and to_id = '"+ transferOrder.get("route_to")
                                + "' and priceType='"+chargeType+"'");
                
                if (contractFinItem != null) {
                	genFinItem2(transferOrder.getLong("id"), null, contractFinItem, chargeType, returnOrderId);
                }else{
                    contractFinItem = Db
                            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                    +" and to_id = '"+ transferOrder.get("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    
                    if (contractFinItem != null) {
                    	genFinItem2(transferOrder.getLong("id"), null, contractFinItem, chargeType, returnOrderId);
                    }
                }
            }
        }        
    } 
	
	public void calculateChargeByCustomer(TransferOrder transferOrder, Long returnOrderId, List<UserLogin> users) {
		String chargeType = transferOrder.get("charge_type");
		
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
		if("perUnit".equals(chargeType)){//计件
			calcRevenuePerUnit(returnOrderId, chargeType, transferOrderId, customerContract);
		}else if ("perCar".equals(chargeType)){//整车
			calcRevenuePerCar(customerContract, chargeType, transferOrder, returnOrderId);
		}else if("perCargo".equals(chargeType)){//零担
			List<Record> transferOrderItemList = Db.find("select toi.* from transfer_order_item toi left join transfer_order tor on tor.id = toi.order_id where tor.id = ?", transferOrder.get("id"));
			//每次都新生成一个helper来处理计算，防止并发问题。
            ReturnOrderPaymentHelperForDirect.getInstance().genFinPerCargo(users, transferOrder, transferOrderItemList, customerContract, chargeType, returnOrderId);
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
							+ dOrderItemRecord.get("product_id")
							+ " and from_id = '"
							+ dOrderItemRecord.get("route_from")
							+ "' and to_id = '"
							+ dOrderItemRecord.get("route_to")
							+ "' and priceType='" + chargeType + "'");
			
			if (contractFinItem != null) {
				genFinItem2(transferOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId);
			} else {
				contractFinItem = Db
						.findFirst("select amount, fin_item_id, contract_id from contract_item where contract_id ="
								+ customerContract.getLong("id")
								+ " and product_id ="
								+ dOrderItemRecord.get("product_id")
								+ " and to_id = '"
								+ dOrderItemRecord.get("route_to")
								+ "' and priceType='" + chargeType + "'");
				
				if (contractFinItem != null) {
					genFinItem2(transferOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId);
				} else {
					contractFinItem = Db
							.findFirst("select amount, fin_item_id, contract_id from contract_item where contract_id ="
									+ customerContract.getLong("id")
									+ " and from_id = '"
									+ dOrderItemRecord.get("route_from")
									+ "' and to_id = '"
									+ dOrderItemRecord.get("route_to")
									+ "' and priceType='" + chargeType + "'");
					
					if (contractFinItem != null) {
						genFinItem2(transferOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId);
					} else {
						contractFinItem = Db
								.findFirst("select amount, fin_item_id, contract_id from contract_item where contract_id ="
										+ customerContract.getLong("id")
										+ " and to_id = '"
										+ dOrderItemRecord.get("route_to")
										+ "' and priceType='"
										+ chargeType
										+ "'");
						
						if (contractFinItem != null) {
							genFinItem2(transferOrderId, dOrderItemRecord, contractFinItem, chargeType, returnOrderId);
						}
					}
				}
			}
		}
	}
	
	private void genFinItem2(Long transferOrderId, Record tOrderItemRecord,	Record contractFinItem, String chargeType, Long returnOrderId) {
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
		ReturnOrderFinItem returnOrderFinItem = new ReturnOrderFinItem();
		returnOrderFinItem.set("fin_item_id", contractFinItem.get("fin_item_id"));
		if("perCar".equals(chargeType)){
			returnOrderFinItem.set("amount", contractFinItem.getDouble("amount"));        		
    	}else{
    		if(tOrderItemRecord != null){
    			returnOrderFinItem.set("amount", contractFinItem.getDouble("amount") * Double.parseDouble(tOrderItemRecord.get("amount").toString()));
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

		Record transferOrder = Db
				.findFirst("select cargo_nature,cargo_nature_detail from transfer_order where id ="
						+ transferOrderId);
		//判断是否为ATM机
		if (transferOrder.get("cargo_nature").equals("ATM")) {
			sqlTotal = "select distinct count(1) total "
					+ "from transfer_order_item_detail toid "
					+ "left join transfer_order_item toi on toid.item_id = toi.id "
					+ "left join return_order r on (toid.delivery_id= r.delivery_order_id or toi.order_id = r.transfer_order_id) "
					+ "left join product p on toi.product_id = p.id where r.id ="
					+ returnOrderId;

			sql = "select distinct count(*) as amount ,item_no,item_name,width,size,weight,height,volume,unit,remark,tid,serial_no,pieces from ( "
					+ "select toi.id as id, "
					+ "toid.id as tid, "
					+ "toid.serial_no serial_no, "
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
					+ "from transfer_order_item_detail toid "
					+ "left join transfer_order_item toi ON toid.item_id = toi.id "
					+ "left join return_order r on (toid.delivery_id= r.delivery_order_id or toi.order_id = r.transfer_order_id) "
					+ "left join product p on toi.product_id = p.id where r.id ="
					+ returnOrderId + ") toid group by tid" + sLimit;
		} else {
			sqlTotal = "select distinct count(1) total "
					+ " from transfer_order_item toi "
					+ " left join return_order r on r.transfer_order_id = toi.order_id "
					+ " left join product p on toi.product_id = p.id "
					+ " where r.id = " + returnOrderId + " group by toi.id ";
			if (transferOrder.get("cargo_nature_detail").equals("cargoNatureDetailYes")) {
				sqlTotal = "select distinct count(1) total "
						+ "from transfer_order_item_detail toid "
						+ "left join transfer_order_item toi on toid.item_id = toi.id "
						+ "left join return_order r on (toid.delivery_id= r.delivery_order_id or toi.order_id = r.transfer_order_id) "
						+ "left join product p on toi.product_id = p.id where r.id ="
						+ returnOrderId;
				
				sql = "select distinct count(*) as amount ,item_no,item_name,width,size,weight,height,volume,unit,remark,tid,serial_no,pieces from ( "
						+ "select toi.id as id,"
						+ "toid.id as tid,"
						+ "toid.serial_no serial_no, "
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
						+ "from transfer_order_item_detail toid "
						+ "left join transfer_order_item toi on toid.item_id = toi.id "
						+ "left join return_order r on (toid.delivery_id= r.delivery_order_id or toi.order_id = r.transfer_order_id) "
						+ "left join product p on toi.product_id = p.id where r.id ="
						+ returnOrderId + ") toid group by tid" + sLimit;
						
			} else {
				sql = "select toi.id,"
						+ " ifnull(p.item_no, toi.item_no) item_no, "
						+ " ifnull(p.item_name, toi.item_name) item_name,"
						+ " ifnull(p.size, toi.size) size, "
						+ " ifnull(p.width, toi.width) width, "
						+ " ifnull(p.height, toi.height) height, "
						+ " ifnull(p.weight, toi.weight) weight, "
						+ " ifnull(p.volume, toi.volume) volume,"
						+ " ifnull(p.unit, toi.unit) unit, "
						+ " toi.amount amount, "
						+ " toi.remark "
						+ " from transfer_order_item toi "
						+ " left join return_order r on r.transfer_order_id = toi.order_id "
						+ " left join product p on toi.product_id = p.id "
						+ " where r.id = " + returnOrderId
						+ " group by toi.id " + sLimit;
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
		List<Fin_item> items = new ArrayList<Fin_item>();
		String returnOrderId = getPara();
		Fin_item item = Fin_item.dao
				.findFirst("select * from fin_item where type = '应收' order by id asc");
		if (item != null) {
			ReturnOrderFinItem dFinItem = new ReturnOrderFinItem();
			dFinItem.set("status", "新建").set("fin_item_id", item.get("id"))
					.set("return_order_id", returnOrderId)
					.set("create_date", new Date());
			dFinItem.save();
		}
		items.add(item);
		renderJson(items);
	}

	// 修改应付
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RO_ADD_REVENUE})
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
		sql = "select distinct f.name name, rofi.*,ifnull(tor.order_no,(select group_concat(distinct tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id where r_o.delivery_order_id = dor.id)) transfer_order_no, d_o.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname"
				+ " from return_order_fin_item rofi "
				+ " left join return_order r_o on r_o.id = rofi.return_order_id"
				+ " left join fin_item f on rofi.fin_item_id = f.id "
				+ " left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id  "
				+ " left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join delivery_order_item doi on doi.delivery_id = d_o.id "
				+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id where r_o.id = "
				+ id + " order by create_date";
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
}
