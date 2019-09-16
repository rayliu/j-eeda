package controllers.yh.delivery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderFinItem;
import models.DeliveryOrderItem;
import models.DeliveryOrderMilestone;
import models.FinItem;
import models.InventoryItem;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderFinItem;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.contract.Contract;
import models.yh.delivery.DeliveryOrder;
import models.yh.returnOrder.ReturnOrderFinItem;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.returnOrder.ReturnOrderController;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
public class DeliveryOrderMilestoneController extends Controller {

    private Logger logger = Logger.getLogger(DeliveryOrderMilestoneController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void transferOrderMilestoneList() {
        Map<String, List> map = new HashMap<String, List>();
        List<String> usernames = new ArrayList<String>();
        String delivery_id = getPara("delivery_id");
        if (delivery_id == "") {
            delivery_id = "-1";
        }
        List<DeliveryOrderMilestone> transferOrderMilestones = DeliveryOrderMilestone.dao
                .find("select * from delivery_order_milestone where delivery_id=" + delivery_id);
        for (DeliveryOrderMilestone transferOrderMilestone : transferOrderMilestones) {
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.getLong("create_by"));
            String username = userLogin.getStr("c_name");
            if(username==null||"".equals(username)){
            	username=userLogin.getStr("user_name");
            }
            usernames.add(username);
        }
        map.put("transferOrderMilestones", transferOrderMilestones);
        map.put("usernames", usernames);
        renderJson(map);
    }

    // 发车确认
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_COMPLETED})
    @Before(Tx.class)
    public void departureConfirmation() {
    	String warehouseId = getPara("warehouseId");
		String customerId = getPara("customerId");
		String cargoNature = getPara("cargoNature");
		String[] transferItemId =  getPara("transferItemIds").split(",");
		String[] productId =  getPara("productIds").split(",");
		String[] shippingNumber =  getPara("shippingNumbers").split(",");
		String depart_date = getPara("depart_date");
    	
        Long delivery_id = Long.parseLong(getPara("delivery_id"));
        DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(delivery_id);
        deliveryOrder.set("status", "配送在途");
        if(StringUtils.isNotEmpty(depart_date)){
			deliveryOrder.set("depart_stamp", depart_date);
		}else{
			deliveryOrder.set("depart_stamp", new Date());
		}
        deliveryOrder.update();
        DeliveryOrder deliveryOrder1 = DeliveryOrder.dao.findById(deliveryOrder.getLong("delivery_id"));//调拨仓库
        if(deliveryOrder1!=null){
            deliveryOrder1.set("status", "已发车");
            deliveryOrder1.update();
        }
        Map<String, Object> map = new HashMap<String, Object>();
        DeliveryOrderMilestone transferOrderMilestone = new DeliveryOrderMilestone();
        transferOrderMilestone.set("status", "已发车");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        
        if("ATM".equals(cargoNature)){
        	//有可能勾选多个运输单做配送单
        	String sql ="select toi.amount, ifnull(toi.complete_amount,0) complete_amount, toid.* "
					+ " from delivery_order_item doi, transfer_order_item_detail toid, transfer_order_item toi"
					+ " where doi.transfer_item_detail_id = toid.id and toid.item_id = toi.id"
					+ " and doi.delivery_id = '"+delivery_id+"'";
        	List<Record> list = Db.find(sql);
        	for (Record record : list) {//循环 更新运输单中的已配送数量				
				Long toItemId= record.getLong("item_id");
				TransferOrderItem toi = TransferOrderItem.dao.findById(toItemId);
				double c_amount = record.getDouble("complete_amount");
				toi.set("complete_amount", c_amount+1).update();
			}
//        	String toSql ="select distinct toid.order_id "
//					+ " from delivery_order_item doi, transfer_order_item_detail toid, transfer_order_item toi"
//					+ " where doi.transfer_item_detail_id = toid.id and toid.item_id = toi.id"
//					+ " and doi.delivery_id = '"+delivery_id+"'";
//        	List<Record> toList = Db.find(sql);
//        	for (Record record : toList) {//循环 更新运输单中的已配送数量
//        		Long orderId = record.getLong("order_id");
//        		TransferOrder tOrder = TransferOrder.dao.findById(orderId);
//        		String leftAmountSql= "select sum(amount)-ifnull(sum(complete_amount),0) left_amount from transfer_order_item toi where order_id='"+orderId+"'";
//        		Record rec = Db.findFirst(leftAmountSql);
//        		if(rec!=null && rec.getDouble("left_amount")>0){
//        			tOrder.set("status", "部分配送中");
//				}else{
//					tOrder.set("status", "配送中");
//				}
//        		tOrder.update();
//        	}
        }else if("cargo".equals(cargoNature)){//货品属性：一站式普通货品，还有普通货品配送没做
  			for (int i = 0; i < productId.length; i++) {
  				//修改实际库存
  				//InventoryItem item = InventoryItem.dao.findFirst("select * from inventory_item ii where ii.warehouse_id = '" + warehouseId + "' and ii.product_id = '" + productId[i] + "' and ii.party_id = '" + customerId + "';");
  				//item.set("total_quantity", item.getDouble("total_quantity") - Double.parseDouble(shippingNumber[i])).update();
  				//修改运输单已完成数量
//  				TransferOrderItem transferOrderItem = TransferOrderItem.dao.findById(transferItemId[i]);
//  				double outCompleteAmount = transferOrderItem.getDouble("complete_amount")==null?0:transferOrderItem.getDouble("complete_amount");
//  				String shiNumber = shippingNumber[i];
//  				if(StringUtils.isBlank(shippingNumber[i])){
//  					shiNumber = transferOrderItem.getDouble("amount").toString();
//  				}
//  				double newCompleteAmount = outCompleteAmount + Double.parseDouble(shiNumber);
//  				transferOrderItem.set("complete_amount", newCompleteAmount).update();
  				
//  				//货品明细表
//				long transfer_id = transferOrderItem.getLong("order_id");
//				// 改变运输单状态
//				TransferOrder tOrder = TransferOrder.dao.findById(transfer_id);
//				Double total_amount = transferOrderItem.getDouble("amount");
//				if(total_amount == newCompleteAmount){
//					tOrder.set("status", "配送中");
//				}else{
//					tOrder.set("status", "部分配送中");
//				}
//				tOrder.update();
  			}
  		}
        
        transferOrderMilestone.set("create_by", LoginUserController.getLoginUserId(this));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("delivery_id", getPara("delivery_id"));
        transferOrderMilestone.save();
        map.put("transferOrderMilestone", transferOrderMilestone);
        map.put("username", LoginUserController.getLoginUserName(this));
        
        renderJson(map);
        // 扣库存
        //gateOutProduct(deliveryOrder);
        
        List<Record> transferOrderItemDetailList = Db.
        										find("select toid.* from transfer_order_item_detail toid left join delivery_order_item doi on toid.id = doi.transfer_item_detail_id where doi.delivery_id = ?", delivery_id);
        // 生成应付
        setPay(deliveryOrder, users, sqlDate, transferOrderItemDetailList);

    }

    // 配送单 发车应付计费  先获取有效期内的合同，条目越具体优先级越高
    // 级别1：计费类别 + 货  品 + 始发地 + 目的地
    // 级别2：计费类别 + 货  品 + 目的地
    // 级别3：计费类别 + 始发地 + 目的地
    // 级别4：计费类别 + 目的地
    private void setPay(DeliveryOrder deliverOrder, List<UserLogin> users, java.sql.Timestamp sqlDate, List<Record> transferOrderItemDetailList) {
        // 生成应付
        Long spId=deliverOrder.getLong("sp_id");
        if ( spId== null)
            return;
        
        //先获取有效期内的配送sp合同, 如有多个，默认取第一个
//        Contract spContract= Contract.dao.findFirst("select * from contract where type='DELIVERY_SERVICE_PROVIDER' " +
//                "and (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="+spId);
        Contract spContract= Contract.dao.findFirst("select * from contract where  " +
                " (CURRENT_TIMESTAMP() between period_from and period_to) and party_id="+spId);
        if(spContract==null)
            return;
        
        String chargeType = deliverOrder.getStr("priceType");
        
        Long deliverOrderId = deliverOrder.getLong("id");
        if (spId != null) {
            if ("perUnit".equals(chargeType)) {
                genFinPerUnit(spContract, chargeType, deliverOrderId);
            } else if ("perCar".equals(chargeType)) {
                genFinPerCar(spContract, chargeType, deliverOrder);
            } else if ("perCargo".equals(chargeType)) {
            	//每次都新生成一个helper来处理计算，防止并发问题。
                DeliveryOrderPaymentHelper.getInstance().genFinPerCargo(users, deliverOrder, transferOrderItemDetailList, spContract, chargeType);
            }
            
            
        }
    }

    public void genFinPerCar(Contract spContract, String chargeType, DeliveryOrder deliverOrder) {
        Long deliverOrderId = deliverOrder.getLong("id");
    
        Record contractFinItem = Db
                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                        +" and carType = '" + deliverOrder.getStr("car_type") +"' "
                        +" and carlength = " + deliverOrder.getStr("car_size")
                        +" and from_id = '"+ deliverOrder.getStr("route_from")
                        +"' and to_id = '"+ deliverOrder.getStr("route_to")
                        + "' and priceType='"+chargeType+"'");
        
        if (contractFinItem != null) {
            genFinItem(deliverOrderId, null, contractFinItem, chargeType);
        }else{
            contractFinItem = Db
                    .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                            +" and carType = '" + deliverOrder.getStr("car_type") +"' "
                            +" and from_id = '"+ deliverOrder.getStr("route_from")
                            +"' and to_id = '"+ deliverOrder.getStr("route_to")
                            + "' and priceType='"+chargeType+"'");
            
            if (contractFinItem != null) {
                genFinItem(deliverOrderId, null, contractFinItem, chargeType);
            }else{
			    contractFinItem = Db
			            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
			                    +" and carType = '" + deliverOrder.getStr("car_type")//对应发车单的 car_type
			                    +"' and to_id = '" + deliverOrder.getStr("route_to")
			                    + "' and priceType='"+chargeType+"'");
			    if (contractFinItem != null) {
			        genFinItem(deliverOrderId, null, contractFinItem, chargeType);
			    }
			}
        }        
    }
    
    public void genFinPerUnit(Contract spContract, String chargeType, Long deliverOrderId) {
        List<Record> deliveryOrderItemList = Db
                .find("SELECT count(1) amount, toi.product_id, d_o.route_from, d_o.route_to, IFNULL(toi.sum_weight,0) sum_weight FROM "+
					    "delivery_order_item doi LEFT JOIN transfer_order_item_detail toid ON doi.transfer_item_detail_id = toid.id "+
					        "LEFT JOIN transfer_order_item toi ON toid.item_id = toi.id "+
					        "LEFT JOIN delivery_order d_o ON doi.delivery_id = d_o.id "+
                		"WHERE  doi.delivery_id = "+ deliverOrderId+" group by toi.product_id, d_o.route_from, d_o.route_to" );
        
        
        for (Record dOrderItemRecord : deliveryOrderItemList) {
            Record contractFinItem = Db
                    .findFirst("select amount, amount1, amount2, amount3, amount4, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
//                            + " and product_id ="+dOrderItemRecord.getLong("product_id")
                            +" and from_id = '"+ dOrderItemRecord.getStr("route_from")
                            +"' and to_id = '"+ dOrderItemRecord.getStr("route_to")
                            + "' and priceType='"+chargeType+"'");
            
            if (contractFinItem != null) {
                genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType);
            }else{
                contractFinItem = Db
                        .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
//                                + " and product_id ="+dOrderItemRecord.getLong("product_id")
                                +" and to_id = '"+ dOrderItemRecord.getStr("route_to")
                                + "' and priceType='"+chargeType+"'");
                
                if (contractFinItem != null) {
                    genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType);
                }else{
                    contractFinItem = Db
                            .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                    +" and from_id = '"+ dOrderItemRecord.getStr("route_from")
                                    +"' and to_id = '"+ dOrderItemRecord.getStr("route_to")
                                    + "' and priceType='"+chargeType+"'");
                    
                    if (contractFinItem != null) {
                        genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType);
                    }else{
                        contractFinItem = Db
                                .findFirst("select amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id")
                                        +" and to_id = '"+ dOrderItemRecord.getStr("route_to")
                                        +"' and priceType='"+chargeType+"'");
                        
                        if (contractFinItem != null) {
                            genFinItem(deliverOrderId, dOrderItemRecord, contractFinItem, chargeType);
                        }
                    }
                }
            }
        }
    }

    private void genFinItem(Long departOrderId, Record tOrderItemRecord, Record contractFinItem, String chargeType) {
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());
        DeliveryOrderFinItem deliveryFinItem = new DeliveryOrderFinItem();
        deliveryFinItem.set("fin_item_id", contractFinItem.getLong("fin_item_id"));
        
        double t_weight_one = tOrderItemRecord.getDouble("sum_weight") ;
        double t_weight_all = tOrderItemRecord.getDouble("sum_weight") ;
        
        double amount =0.0;
        if(contractFinItem.getDouble("amount")!=null) {
        	amount = contractFinItem.getDouble("amount");
        };  //0-100kg
        double amount1 = 0.0;  //100-200kg
        if(contractFinItem.getDouble("amount1")!=null) {
        	amount1 = contractFinItem.getDouble("amount1");
        };  //0-100kg
        double amount2 = 0.0; //201-300kg
        if(contractFinItem.getDouble("amount2")!=null) {
        	amount2 = contractFinItem.getDouble("amount2");
        };  //0-100kg
        double amount3 = 0.0; //301-400kg
        if(contractFinItem.getDouble("amount3")!=null) {
        	amount3 = contractFinItem.getDouble("amount3");
        };  //0-100kg
        double amount4 = 0.0; //401kg~
        if(contractFinItem.getDouble("amount4")!=null) {
        	amount4 = contractFinItem.getDouble("amount4");
        };  //0-100kg
        //配送单合同
        if("perCar".equals(chargeType)){
        	if (t_weight_all < 100){
        		deliveryFinItem.set("amount", amount);     
        	} else if(100 <= t_weight_all && t_weight_all <= 200){
        		deliveryFinItem.set("amount", amount1);  
        	} else if(200 < t_weight_all && t_weight_all <= 300){
        		deliveryFinItem.set("amount", amount2); 
        	} else if(300 < t_weight_all && t_weight_all <= 400){
        		deliveryFinItem.set("amount", amount3);     
        	} else if(400 < t_weight_all){
        		deliveryFinItem.set("amount", amount4);     
        	} else {
        		deliveryFinItem.set("amount", amount);     
        	}      		
    	}else{
    		if(tOrderItemRecord != null){
	    		if (t_weight_one < 100){
	        		deliveryFinItem.set("amount", amount);     
	        	} else if(100 <= t_weight_one && t_weight_one <= 200){
	        		deliveryFinItem.set("amount", amount1);  
	        	} else if(200 < t_weight_one && t_weight_one <= 300){
	        		deliveryFinItem.set("amount", amount2); 
	        	} else if(300 < t_weight_one && t_weight_one <= 400){
	        		deliveryFinItem.set("amount", amount3);     
	        	} else if(400 < t_weight_one){
	        		deliveryFinItem.set("amount", amount4);     
	        	} else {
	        		deliveryFinItem.set("amount", amount);     
	        	}   
    		}
    	}
        deliveryFinItem.set("order_id", departOrderId);
        deliveryFinItem.set("status", "未完成");
        deliveryFinItem.set("creator", LoginUserController.getLoginUserId(this));
        deliveryFinItem.set("create_date", now);
        deliveryFinItem.set("create_name", deliveryFinItem.CREATE_NAME_SYSTEM);
        deliveryFinItem.save();
    }
    
    // 扣库存
    private void gateOutProduct(DeliveryOrder deliveryOrder) {
        Long deliveryOrderId=deliveryOrder.getLong("id");
        Long warehouseId=deliveryOrder.getLong("from_warehouse_id");
        // 获取配送单的item list TODO 只针对ATM， 普通货品需要再验证
        List<Record> itemList = Db.find("select di.*, ti.product_id, c.name as c_name from delivery_order_item di "
                                        +"left join transfer_order_item_detail toid on di.transfer_item_detail_id = toid.id  "
                                        +"left join transfer_order_item ti on toid.item_id = ti.id "
                                        +"left join product p on ti.product_id = p.id  "
                                        +"left join category c on p.category_id = c.id  "
                                        +"where di.delivery_id ="+ deliveryOrderId);
        for (Record dOrderItemRecord : itemList) {
            //如果没有product_id, 则不用计算库存
            if(dOrderItemRecord.getLong("product_id")==null)
                continue;
            
            Long productId=dOrderItemRecord.getLong("product_id");
            if("ATM".equals(dOrderItemRecord.getStr("c_name"))){
                InventoryItem item = InventoryItem.dao.findFirst("select * from inventory_item where product_id=? and warehouse_id=?", productId, warehouseId);
                if(item!=null){
                    //TODO 如果库存不够，应该报错提示，并且回滚，不能发车
                    item.set("total_quantity", item.getDouble("total_quantity")-1).update();
                }
                //在运输单中把单品设置为已出库（配送）
                TransferOrderItemDetail tDetail= TransferOrderItemDetail.dao.findById(dOrderItemRecord.getLong("transfer_item_detail_id"));
                if(item!=null){
                    tDetail.set("is_delivered", true).update();
                }
            }else{
                InventoryItem item = InventoryItem.dao.findFirst("select * from inventory_item where product_id=? and warehouse_id=?", productId, warehouseId);
                if(item!=null){
                    //TODO 如果是普通货品，还是-1？？
                    item.set("total_quantity", item.getDouble("total_quantity")-dOrderItemRecord.getDouble("amount")).update();
                }
            }
        }
        
    }

    // 保存里程碑
    public void saveTransferOrderMilestone() {
        Long delivery_id = Long.parseLong(getPara("delivery_id"));
        DeliveryOrder transferOrder = DeliveryOrder.dao.findById(delivery_id);
        Map<String, Object> map = new HashMap<String, Object>();
        DeliveryOrderMilestone transferOrderMilestone = new DeliveryOrderMilestone();
        String status = getPara("status");
        String location = getPara("location");
        if (!status.isEmpty()) {
            transferOrderMilestone.set("status", status);
            transferOrder.set("status", status);
        } else {
            transferOrderMilestone.set("status", "在途");
            transferOrder.set("status", "在途");
        }
        transferOrder.update();
        if (!location.isEmpty()) {
            transferOrderMilestone.set("location", location);
        } else {
            transferOrderMilestone.set("location", "");
        }
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");

        transferOrderMilestone.set("create_by", users.get(0).getLong("id"));

        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("delivery_id", getPara("delivery_id"));
        transferOrderMilestone.save();

        map.put("transferOrderMilestone", transferOrderMilestone);
        UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.getLong("create_by"));
        String username = userLogin.getStr("c_name");
        if(username==null||"".equals(username)){
        	username=userLogin.getStr("user_name");
        }
        map.put("username", username);
        renderJson(map);
    }

    // 配送单  到达确认
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_DOM_COMPLETED})
    @Before(Tx.class)
    public void receipt() {
    	String order_type = getPara("order_type");
    	String userId = getPara("userId");
    	long return_id = 0;
        long delivery_id = getParaToLong("delivery_id");
        DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(delivery_id);
        deliveryOrder.set("status", "已完成");
        deliveryOrder.set("arrive_stamp", new Date());
        deliveryOrder.update();

        Map<String, Object> map = new HashMap<String, Object>();
        DeliveryOrderMilestone transferOrderMilestone = new DeliveryOrderMilestone();
        transferOrderMilestone.set("status", "已送达");
        if(StringUtils.isEmpty(userId))
        	userId = LoginUserController.getLoginUserId(this).toString();
        transferOrderMilestone.set("create_by", userId);
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("delivery_id", getPara("delivery_id"));
        transferOrderMilestone.save();
        map.put("transferOrderMilestone", transferOrderMilestone);
        UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.getStr("create_by"));
        String username = userLogin.getStr("user_name");
        map.put("username", username);
        
        Record rRec = Db.findFirst("select * from return_order where delivery_order_id = ?",delivery_id);
        if(rRec == null){
        String isNullOrder = deliveryOrder.getStr("isNullOrder");
        if(!isNullOrder.equals("Y")){
        	Date createDate = Calendar.getInstance().getTime();
        	String orderNo = null;
        	ReturnOrder returnOrder = null;
            Record  transferDetail= Db
    				.findFirst("select * from transfer_order_item_detail where delivery_refused_id =?",delivery_id);
            //查询配送单中的运输单,如果是普货配送就验证是否以配送完成
            if(!"ATM".equals(deliveryOrder.getStr("cargo_nature"))){
//            	Record deliveryTotal = Db.findFirst("SELECT * FROM delivery_order_item doi "
//            			+ " LEFT JOIN delivery_order dor on dor.id = doi.delivery_id "
//            			+ " LEFT JOIN transfer_order_item toi on toi.id = doi.transfer_item_id "
//            			+ " where dor.id = '" + delivery_id + "';");
//        		double SumDelivery = deliveryTotal.getDouble("complete_amount");//已配送的数量
//        		double totalamount = deliveryTotal.getDouble("amount"); //货品总数
//        		
//            	//因为现在普货的话只能是一站式配送，所以只有一张运输单的数据
            	List<DeliveryOrderItem> items = DeliveryOrderItem.dao.find("select * from delivery_order_item where delivery_id = '" + delivery_id + "' group by transfer_order_id;");
            	for(DeliveryOrderItem item :items){
            		orderNo = OrderNoGenerator.getNextOrderNo("HD");
            		returnOrder = new ReturnOrder();
            		long transferOrderId = item.getLong("transfer_order_id");
            		//当运输单配送完成时生成回单
    				if(transferDetail!=null){
    					Record  returnRefusedOrder= Db
    							.findFirst("select * from return_order where delivery_order_id =?",transferDetail.getLong("delivery_id"));
    					returnOrder.set("order_no", returnRefusedOrder.getStr("order_no")+"-1");
    				}else{
    					returnOrder.set("order_no", orderNo);
    				}
    				
    	            returnOrder.set("delivery_order_id", delivery_id);
    	            returnOrder.set("customer_id", deliveryOrder.getLong("customer_id"));
    	            returnOrder.set("notity_party_id", deliveryOrder.getLong("notity_party_id"));
    	            returnOrder.set("transfer_order_id", transferOrderId);
    	            returnOrder.set("order_type", "应收");
    	            returnOrder.set("transaction_status", "新建");
    	            returnOrder.set("creator", userId);
    	            

    	            Record transfer = Db.findFirst("select tor.* from delivery_order_item doi"
    	            		+ " LEFT JOIN transfer_order tor on tor.id = doi.transfer_order_id"
    	            		+ " where doi.delivery_id = ? ",delivery_id);
    	            returnOrder.set("office_id", transfer.getLong("office_id"));
    	            returnOrder.set("create_date", createDate);
    	            returnOrder.set("customer_id", deliveryOrder.getLong("customer_id"));
    	            returnOrder.save();
    	            return_id = returnOrder.getLong("id");
    	            ReturnOrderController roController = new ReturnOrderController(); 
    	            //把运输单的应收带到回单中
    	            roController.tansferIncomeFinItemToReturnFinItem(returnOrder, delivery_id, transferOrderId);
    	            //计算普货合同应收，算没单品的，有单品暂时没做
    	            TransferOrder order = TransferOrder.dao.findById(transferOrderId);
    	            if(!order.getBoolean("no_contract_revenue")){
    	            	List<Record> transferOrderItemList = Db.find("select toid.* from transfer_order_item toid left join delivery_order_item doi on toid.id = doi.transfer_item_id where doi.delivery_id = ?", delivery_id);
    	            	roController.calculateChargeGeneral(Long.parseLong(userId), deliveryOrder, returnOrder.getLong("id"), transferOrderItemList);
    	            }
            	}
            }else{
            	returnOrder = new ReturnOrder();
            	orderNo = OrderNoGenerator.getNextOrderNo("HD");
            	//ATM
            	if(transferDetail!=null){
    				Record  returnRefusedOrder= Db
    						.findFirst("select * from return_order where delivery_order_id =?",transferDetail.getLong("delivery_id"));
    				returnOrder.set("order_no", returnRefusedOrder.getStr("order_no")+"-1");
    			}else{
    				returnOrder.set("order_no", orderNo);
    			}
            	//如果是配送单生成回单：一张配送单只生成一张回单
                returnOrder.set("delivery_order_id", delivery_id);
                returnOrder.set("customer_id", deliveryOrder.getLong("customer_id"));
                returnOrder.set("notity_party_id", deliveryOrder.getLong("notity_party_id"));
                returnOrder.set("order_type", "应收");
                returnOrder.set("transaction_status", "新建");
                returnOrder.set("creator", userId);
                
                Record tor = new Record();
                if(deliveryOrder.getStr("order_no").endsWith("-1")){
                	tor = Db.findFirst("select * from transfer_order_item_detail toid"
    	            		+ " left join transfer_order tor on tor.id = toid.order_id "
    	            		+ " where toid.delivery_refused_id = ?",delivery_id);
                }else{
                	tor = Db.findFirst("select * from transfer_order_item_detail toid"
    	            		+ " left join transfer_order tor on tor.id = toid.order_id "
    	            		+ " where toid.delivery_id = ?",delivery_id);
                }
	            returnOrder.set("office_id", tor.getLong("office_id"));
                returnOrder.set("create_date", createDate);
                returnOrder.set("customer_id", deliveryOrder.getLong("customer_id"));
                returnOrder.save();
                return_id = returnOrder.getLong("id");
                // 生成应收
                //ATM
                //if("ATM".equals(deliveryOrder.get("cargo_nature"))){
        	        ReturnOrderController roController= new ReturnOrderController();
        	        List<Record> transferOrderItemDetailList = Db.find("select toid.* from transfer_order_item_detail toid left join delivery_order_item doi on toid.id = doi.transfer_item_detail_id where doi.delivery_id = ?", delivery_id);
        	        roController.calculateCharge(Long.parseLong(userId), deliveryOrder, returnOrder.getLong("id"), transferOrderItemDetailList);
        	        
                //}
            }
        }else{  
        	/**
        	 * 空白配送单生成回单流程
        	 */
        	String orderNo = OrderNoGenerator.getNextOrderNo("HD");
        	ReturnOrder order = new ReturnOrder();
        	order.set("order_no", orderNo);
        	//如果是配送单生成回单：一张配送单只生成一张回单
        	order.set("delivery_order_id", delivery_id);
        	order.set("customer_id", deliveryOrder.getLong("customer_id"));
            order.set("notity_party_id", deliveryOrder.getLong("notity_party_id"));
            order.set("order_type", "应收");
            order.set("transaction_status", "新建");
            order.set("creator", userId);
            
            order.set("office_id", deliveryOrder.getLong("office_id"));
            order.set("create_date", new Date());
            order.set("customer_id", deliveryOrder.getLong("customer_id"));
            order.save();
        }
    	}
        
        if("wx".equals(order_type)){
        	renderJson(return_id);
        }else{
        	renderJson(map);
        }
    }

	
	
    // 入库确认
    public void warehousingConfirm() {
        Map<String, Object> map = new HashMap<String, Object>();
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        transferOrderMilestone.set("status", "已入库");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).getLong("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("delivery_id", getPara("delivery_id"));
        transferOrderMilestone.save();
        map.put("transferOrderMilestone", transferOrderMilestone);
        UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.getLong("create_by"));
        String username = userLogin.getStr("user_name");
        map.put("username", username);
        renderJson(map);
    }

   
    // 应付list
    public void accountPayable() {
        //String id = getPara()==null?"-1":getPara();
    	String id = getPara();
        if (id == null || id.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }
        
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数

        String sql = "SELECT count(1) total FROM  delivery_order_fin_item d LEFT JOIN  fin_item f ON d.fin_item_id = f.id WHERE d.order_id = "+id+"  and f.type='应付'";
        Record rec = Db.findFirst(sql);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("SELECT d.*, f.name AS fin_item_name, "+
                	  "(SELECT group_concat(distinct transfer_no separator ' ') FROM delivery_order_item where delivery_id=d.order_id) AS transferorderno "+
                	  "FROM  delivery_order_fin_item d LEFT JOIN  fin_item f ON d.fin_item_id = f.id "+
                	  "WHERE d.order_id = "+id+"  and f.type='应付'");

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    @RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_ADD_COST})
    public void addNewRow() {
        List<FinItem> items = new ArrayList<FinItem>();
        String orderId = getPara();
        FinItem item = FinItem.dao.findFirst("select * from fin_item where type = '应付' order by id asc");
        if(item != null){
        	DeliveryOrderFinItem dFinItem = new DeliveryOrderFinItem();
	        dFinItem.set("status", "新建").set("fin_item_id", item.getLong("id"))
	        .set("order_id", orderId).set("create_name", dFinItem.CREATE_NAME_USER)
	        .save();
        }
        items.add(item);
        renderJson(items);
    }

    // 添加应付
    public void paymentSave() {
        String returnValue = "";
        String id = getPara("id");
        String finItemId = getPara("finItemId");
        TransferOrderFinItem dFinItem = TransferOrderFinItem.dao.findById(id);

        String amount = getPara("amount");

        String username = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + username + "'");
        Date createDate = Calendar.getInstance().getTime();

        if (!"".equals(finItemId) && finItemId != null) {
            dFinItem.set("fin_item_id", finItemId).update();
            returnValue = finItemId;
        } else if (!"".equals(amount) && amount != null) {
            dFinItem.set("amount", amount).update();
            returnValue = amount;
        }
        List<Record> list = Db.find("select * from fin_item");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getStr("name") == null) {
                FinItem.dao.deleteById(list.get(i).getLong("id"));
            }
        }
        renderText(returnValue);
    }

    public void fin_item() {
        // String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        locationList = Db.find("select * from fin_item where type='应付'");
        renderJson(locationList);
    }

    public void fin_item2() {
        // String input = getPara("input");
        List<Record> locationList = Collections.EMPTY_LIST;
        locationList = Db.find("select * from fin_item where type='应收'");
        renderJson(locationList);
    }
    
    //修改应付

    @RequiresPermissions(value = {PermissionConstant.PERMSSION_DYO_ADD_COST})
    public void updateDeliveryOrderFinItem(){
    	boolean update_check = false;
    	String paymentId = getPara("paymentId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if("amount".equals(name) && "".equals(value)){
    		value = "0";
    	}
    	if(paymentId != null && !"".equals(paymentId)){
    		DeliveryOrderFinItem deliveryOrderFinItem = DeliveryOrderFinItem.dao.findById(paymentId);
    		if(deliveryOrderFinItem!=null) {
    				Record cost_order = Db.findFirst("SELECT IFNULL(aco.`status`,'新建')  cost_status FROM arap_cost_order aco"
    						+ " LEFT JOIN arap_cost_item aci ON aci.`cost_order_id` = aco.`id` WHERE aci.`ref_order_no` = '配送'"
    						+ " AND aci.`ref_order_id` = ?",deliveryOrderFinItem.getLong("order_id"));
    				if(cost_order!=null) {
    					if("新建".equals(cost_order.getStr("cost_status"))) {
    						update_check = true;
    					}
    				}else {
    					update_check = true;
    				}
    		}
    		if(update_check) {
    			deliveryOrderFinItem.set(name, value);
        		deliveryOrderFinItem.update();
    		}
    	}
        renderJson("{\"success\":"+update_check+"}");
    }
    // 删除应付 
    public void finItemdel() {
        String id = getPara();
        boolean delete_check = false;
        DeliveryOrderFinItem deliveryOrderFinItem = DeliveryOrderFinItem.dao.findById(id);
		if(deliveryOrderFinItem!=null) {
				Record cost_order = Db.findFirst("SELECT IFNULL(aco.`status`,'新建')  cost_status FROM arap_cost_order aco"
						+ " LEFT JOIN arap_cost_item aci ON aci.`cost_order_id` = aco.`id`"
						+ " WHERE aci.`ref_order_no` = '配送'"
						+ " AND aci.`ref_order_id` = ?",deliveryOrderFinItem.getLong("order_id"));
				if(cost_order!=null) {
					if("新建".equals(cost_order.getStr("cost_status"))) {
						delete_check = true;
					}
				}else {
					delete_check = true;
				}
		}
		if(delete_check) {
			DeliveryOrderFinItem.dao.deleteById(id);
		}
        renderJson("{\"success\":"+delete_check+"}");
    }
    
    /*// 配送排车应付list
    public void accountPayablePlan() {
    	String id = getPara();
    	String sqlOne = "select delivery_plan_order_id from delivery_id = " + id;
    	DeliveryPlanOrderDetail detailList = DeliveryPlanOrderDetail.dao.findFirst(sqlOne);
    	 Map orderMap = new HashMap();
        if (id == null || id.equals("")) {
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }else if(detailList == null ){
             orderMap.put("sEcho", 0);
             orderMap.put("iTotalRecords", 0);
             orderMap.put("iTotalDisplayRecords", 0);
             orderMap.put("aaData", null);
             renderJson(orderMap);
             return;
    	}else{
    		String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }

            // 获取总条数
            String sql = "select count(1) total from  delivery_order_fin_item d left join  fin_item f ON d.fin_item_id = f.id WHERE d.delivery_order_fin_item = "+detailList.get("delivery_plan_order_id")+"  and f.type='应付'";
            Record rec = Db.findFirst(sql);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select d.*, f.name as fin_item_name, "+
                    	  "(select group_concat(distinct transfer_no separator ' ') from delivery_order_item where delivery_id=d.order_id) as transferorderno "+
                    	  "from  delivery_order_fin_item d left join  fin_item f on d.fin_item_id = f.id "+
                    	  "where d.delivery_order_fin_item = "+detailList.get("delivery_plan_order_id")+"  and f.type='应付'");

            
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

            orderMap.put("aaData", orders);
    	}
        
        renderJson(orderMap);
    }*/
    
    @Before(Tx.class)
    public void deleteReceipt(){
    	String delivery_id = getPara("delivery_id");
    	
    	if("".equals(delivery_id)||delivery_id==null)
			return;
    	
    	DeliveryOrder dor = DeliveryOrder.dao.findById(delivery_id);
    	
    	//检查是否有财务流转了（应付）
    	String audit_status = dor.getStr("audit_status");
    	if(!"新建".equals(audit_status) && !"已确认".equals(audit_status)){
    		renderJson("{\"success\":false}");
    		return ;
    	}
    	
    	
    	
    	List<ReturnOrder> rors = ReturnOrder.dao.find("select * from return_order where delivery_order_id = ?",delivery_id);
    	for(ReturnOrder ror : rors ){
    		long return_id = ror.getLong("id");
    		
    		//检查是否有财务流转了（应收）
    		String return_status = ror.getStr("transaction_status");
    		if(!"新建".equals(return_status) && !"已确认".equals(return_status) && !"已签收".equals(return_status)){
        		renderJson("{\"success\":false}");
        		return ;
        	}
    		
    		//删除回单字表
    		List<ReturnOrderFinItem> rofis = ReturnOrderFinItem.dao.find("select * from return_order_fin_item where return_order_id = ?",return_id);
    		for(ReturnOrderFinItem rofi: rofis){
    			rofi.delete();
    		}
    		
    		//删除回单主表
    		ror.delete();
    	}
    	
    	//更新配送单状态
    	dor.set("status", "配送在途").update();
    	
    	renderJson("{\"success\":true}");
    }  
}
