package controllers.yh.order;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DeliveryOrderItem;
import models.DepartOrder;
import models.DepartTransferOrder;
import models.Party;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.Warehouse;
import models.yh.delivery.DeliveryOrder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.departOrder.DepartOrderController;
import controllers.yh.returnOrder.ReturnOrderController;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
public class TransferOrderMilestoneController extends Controller {

    private Logger logger = Logger.getLogger(TransferOrderMilestoneController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void transferOrderMilestoneList() {
        Map<String, List> map = new HashMap<String, List>();
        List<String> usernames = new ArrayList<String>();
        String order_id = getPara("order_id");
        if (order_id == "" || order_id == null) {
            order_id = "-1";
        }
        String pickupOrderId = getPara("pickupOrderId");
        if (pickupOrderId == "" || pickupOrderId == null) {
            pickupOrderId = "-1";
        }
        if (order_id != "-1" && pickupOrderId == "-1") {
            List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                    .find("select * from transfer_order_milestone where type = '"
                            + TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE + "' and order_id=" + order_id);
            for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = "";
                if(userLogin.get("c_name")!=null&&!"".equals(userLogin.get("c_name"))){
                	username=userLogin.get("c_name");
                }else{
                	username=userLogin.get("user_name");
                }
                usernames.add(username);
            }
            map.put("transferOrderMilestones", transferOrderMilestones);
            map.put("usernames", usernames);
        } else {
            List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                    .find("select * from transfer_order_milestone where type = '"
                            + TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE + "' and pickup_id=" + pickupOrderId);
            for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = "";
                if(userLogin.get("c_name")!=null&&!"".equals(userLogin.get("c_name"))){
                	username=userLogin.get("c_name");
                }else{
                	username=userLogin.get("user_name");
                }
                usernames.add(username);
            }
            map.put("transferOrderMilestones", transferOrderMilestones);
            map.put("usernames", usernames);
        }
        renderJson(map);
    }

    // 保存里程碑
    public void saveTransferOrderMilestone() {
        String order_id = getPara("order_id");
        String milestonePickupId = getPara("milestonePickupId");
        String milestoneDepartId = getPara("milestoneDepartId");
        Map<String, Object> map = new HashMap<String, Object>();
        if (order_id != null && !"".equals(order_id)) {
            TransferOrder transferOrder = TransferOrder.dao.findById(order_id);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
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

            transferOrderMilestone.set("create_by", users.get(0).get("id"));

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("order_id", getPara("order_id"));
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.save();

            map.put("transferOrderMilestone", transferOrderMilestone);
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("c_name");
            if(username==null||"".equals(username)){
            	username=userLogin.get("user_name");
            }
            map.put("username", username);
        } else if (milestonePickupId != null && !"".equals(milestonePickupId)) {
            TransferOrder transferOrder = null;
            DepartOrder departOrder = DepartOrder.dao.findById(milestonePickupId);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            String status = getPara("status");
            String location = getPara("location");
            if (!status.isEmpty()) {
                transferOrderMilestone.set("status", status);
                departOrder.set("status", status);
            } else {
                transferOrderMilestone.set("status", "在途");
                departOrder.set("status", "在途");
            }
            departOrder.update();
            if (!location.isEmpty()) {
                transferOrderMilestone.set("location", location);
            } else {
                transferOrderMilestone.set("location", "");
            }
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");

            transferOrderMilestone.set("create_by", users.get(0).get("id"));

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("pickup_id", milestonePickupId);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE);
            transferOrderMilestone.save();

            // 更新运输单状态
            List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find(
                    "select * from depart_transfer where depart_id = ?", departOrder.get("id"));
            for (DepartTransferOrder departTransferOrder : departTransferOrders) {
                transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
                TransferOrderMilestone tom = new TransferOrderMilestone();
                String status2 = getPara("status");
                String location2 = getPara("location");
                if (!status.isEmpty()) {
                    tom.set("status", status2);
                    transferOrder.set("status", status2);
                } else {
                    tom.set("status", "提货在途");
                    transferOrder.set("status", "提货在途");
                }
                departOrder.update();
                if (!location.isEmpty()) {
                    tom.set("location", location2);
                } else {
                    tom.set("location", "");
                }
                String name2 = (String) currentUser.getPrincipal();
                List<UserLogin> users2 = UserLogin.dao.find("select * from user_login where user_name='" + name2 + "'");

                tom.set("create_by", users.get(0).get("id"));

                java.util.Date utilDate2 = new java.util.Date();
                java.sql.Timestamp sqlDate2 = new java.sql.Timestamp(utilDate2.getTime());
                tom.set("create_stamp", sqlDate2);
                tom.set("order_id", departTransferOrder.get("order_id"));
                tom.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
                tom.save();
                transferOrder.update();
            }

            map.put("transferOrderMilestone", transferOrderMilestone);
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("c_name");
            if(username==null||"".equals(username)){
            	username=userLogin.get("user_name");
            }
            map.put("username", username);
        } else if (milestoneDepartId != null && !"".equals(milestoneDepartId)) {
            DepartOrder departOrder = DepartOrder.dao.findById(milestoneDepartId);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            String status = getPara("status");
            String location = getPara("location");
            if (!status.isEmpty()) {
                transferOrderMilestone.set("status", status);
                departOrder.set("status", status);
            } else {
                transferOrderMilestone.set("status", "在途");
                departOrder.set("status", "在途");
            }
            departOrder.update();
            if (!location.isEmpty()) {
                transferOrderMilestone.set("location", location);
            } else {
                transferOrderMilestone.set("location", "");
            }
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");

            transferOrderMilestone.set("create_by", users.get(0).get("id"));

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("depart_id", milestoneDepartId);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
            transferOrderMilestone.save();

            map.put("transferOrderMilestone", transferOrderMilestone);
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("c_name");
            if(username==null||"".equals(username)){
            	username=userLogin.get("user_name");
            }
            map.put("username", username);
        }
        renderJson(map);
    }

    //TODO:  直送运输单-收货确认：不可以多次提货，整单发车
    /**
     * 直送时要判断是什么单据，如果是退货单，要明确是入仓还是直送到客户工厂，其中入仓的需要增加库存
     */
    @Before(Tx.class)
    public void receipt() {
    	String order_type = getPara("order_type");
    	String userId = getPara("userId");
    	long return_id = 0;
        Long departOrderId = Long.parseLong(getPara("departOrderId"));
        //通过发车单获取运输单ID
        List<Record> transferOrderIds = Db.find("select order_id from depart_transfer where depart_id = ? ;",departOrderId);
        
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        if(StringUtils.isEmpty(userId))
        	userId =  LoginUserController.getLoginUserId(this).toString();
        UserLogin users = UserLogin.dao.findById(userId);
        //判断当前直送的模式，是入中转仓还是直接发送给客户工厂，如果是入中转仓，增加库存
        DepartOrderController.productInWarehouse(getPara("departOrderId"));
        //修改发车单信息
        DepartOrder departOrder = DepartOrder.dao.findById(departOrderId);
        departOrder.set("status", "已收货").update();
        
        //更新单品详细表
        List<TransferOrderItemDetail> toids = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where depart_id = ?",departOrderId);
        for(TransferOrderItemDetail toid:toids){
        	toid.set("status", "已收货").update();
        }
        
        for (Record record : transferOrderIds) {
        	//获取运输单ID
        	long transerOrderId = record.getLong("order_id");
       	 	//直接生成回单，在把合同等费用带到回单中
            TransferOrder transferOrder = TransferOrder.dao.findById(transerOrderId);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        	//退货单生成配送单
            if("cargoReturnOrder".equals(transferOrder.getStr("order_type"))){
		    	DeliveryOrder deliveryOrder = new DeliveryOrder();
		    	String orderNo = OrderNoGenerator.getNextOrderNo("PS");
		        List<TransferOrderItemDetail> transferorderitemdetail =TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where order_id = "+transerOrderId+" and depart_id ="+ departOrderId);
		        Warehouse warehouse = Warehouse.dao.findFirst("SELECT * from warehouse where id=?",transferOrder.get("warehouse_id"));
				deliveryOrder.set("order_no", orderNo)
				.set("customer_id", transferOrder.get("customer_id"))
				.set("create_stamp", sqlDate).set("create_by", userId).set("status", "新建")
				.set("route_from",transferOrder.get("route_to"))
				.set("route_to",transferOrder.get("route_to"))
				.set("pricetype", getPara("chargeType"))
				.set("from_warehouse_id", transferOrder.get("warehouse_id"))
				.set("cargo_nature", transferOrder.get("cargo_nature"))
				.set("priceType", departOrder.get("charge_type"))
				.set("deliveryMode", "own")
				.set("warehouse_nature", "warehouseNatureNo")
				.set("ltl_price_type", departOrder.get("ltl_price_type")).set("car_type", departOrder.get("car_type"))
				.set("audit_status", "新建").set("sign_status", "未回单");
				if(warehouse!=null){
					deliveryOrder.set("sp_id", warehouse.get("sp_id")).set("office_id", warehouse.get("office_id"));
				}else{
					deliveryOrder.set("office_id", users.get("office_id"));
				}if(transferorderitemdetail!=null){
					deliveryOrder.set("notify_party_id", transferorderitemdetail.get(0).getLong("notify_party_id"));
				}
				deliveryOrder.save();
				if("ATM".equals(transferOrder.getStr("cargo_nature"))){
					for (TransferOrderItemDetail transferdetail:transferorderitemdetail) {
						DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
						deliveryOrderItem.set("delivery_id",deliveryOrder.get("id"))
						.set("transfer_order_id",transferOrder.get("id"))
						.set("transfer_no",transferOrder.get("order_no"))
						.set("transfer_item_detail_id",transferdetail.getLong("id"))
						.set("amount", 1);
						deliveryOrderItem.save();
						//在单品中设置delivery_id
						TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
								.findById(transferdetail.getLong("id"));
						transferOrderItemDetail.set("delivery_id",deliveryOrder.get("id"));
						transferOrderItemDetail.set("is_delivered", true);
						transferOrderItemDetail.update();
					}
				}else{
					List<TransferOrderItem> transferorderitem =TransferOrderItem.dao.find("select * from transfer_order_item where order_id = "+transerOrderId+"");
					for (TransferOrderItem transferitem:transferorderitem) {
						DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
						deliveryOrderItem.set("delivery_id",deliveryOrder.get("id"))
						.set("transfer_order_id",transferOrder.get("id"))
						.set("transfer_no",transferOrder.get("order_no"))
						.set("transfer_item_id",transferitem.getLong("id"))
						.set("amount", transferitem.get("amount"));
						deliveryOrderItem.save();
					}
				}
    		}
            String cargo_nature = transferOrder.getStr("cargo_nature");
            if(cargo_nature.equals("ATM")){
            	String sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + transerOrderId;
        		Record rec = Db.findFirst(sqlTotal); 
        		Long totalAmount = rec.getLong("total");  //运输单总单品数量
        		
        		sqlTotal = "select count(1) departAmount from transfer_order_item_detail toid "
        				+ " left join depart_order dor on dor.id = toid.depart_id "
        				+ " where dor.status = '已收货'"
        				+ " and toid.order_id = " + transerOrderId ;
        		rec = Db.findFirst(sqlTotal);
        		Long departAmount = rec.getLong("departAmount");   ///运输单中已收货单品数量(包括这次数量)
        		
        		if(departAmount.equals(totalAmount)){
        			transferOrderMilestone.set("status", "已收货");
        			transferOrder.set("status", "已完成").update();
        			//生成回单
        			if(!"cargoReturnOrder".equals(transferOrder.getStr("order_type"))){
        				return_id = createReturnOrder(transferOrder);
        			}
        		}else{
        			transferOrderMilestone.set("status", "部分已收货");
        		}
            }else{
    			Record re = Db.findFirst("select sum(toi.amount) total from transfer_order_item toi where toi.order_id = ?",transerOrderId);
    			double totalAmount = re.getDouble("total");     //总货品数量
    			double pickupAmount = 0.0;  //已入库的数量（包括这次）
    			if(!transferOrder.getStr("operation_type").equals("out_source")){
    				re = Db.findFirst("select sum(amount) yishou from (select dt.* from depart_pickup dp"
    						+ " LEFT JOIN depart_order dor on dor.id = dp.depart_id"
    						+ " LEFT JOIN depart_transfer dt on dt.pickup_id = dp.pickup_id"
    						+ " where dor.STATUS = '已收货' and dt.order_id = ? and dt.twice_pickup_flag = 'N' group by dt.id ) A",transerOrderId);
    				pickupAmount = re.getDouble("yishou");   //运输单已收货的总数量	
    			}else{
    				pickupAmount = totalAmount; //外包（因无法多次发车，所以数量和总数量相同）
    			}
    			
    			if(totalAmount == pickupAmount){
    				transferOrder.set("status", "已完成").update();
    				transferOrderMilestone.set("status", "已收货");
    				//生成回单
    				return_id = createReturnOrder(transferOrder);
    			}else{
    				transferOrderMilestone.set("status", "部分已收货");
    			}
            }
        	
			//设置回单客户信息，必须是同一个客户
            transferOrderMilestone.set("create_by", userId)
            .set("location", "")
            .set("create_stamp", sqlDate)
            .set("order_id", transerOrderId)
            .set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE)
            .save();

            transferOrderMilestone = new TransferOrderMilestone();
            transferOrderMilestone.set("status", "已收货")
            .set("create_by", userId)
            .set("location", "")
            .set("create_stamp", sqlDate)
            .set("depart_id", departOrderId)
            .set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE)
            .save();
          
           
		}
        if("wx".equals(order_type)){
        	renderJson(return_id);
        }else{
        	renderJson("{\"success\":true}");
        }
    }
    
    
    //自动生成回单
    public long createReturnOrder(TransferOrder transfer){
    	Record re  = Db.findFirst("select count(*) total from return_order ror where transfer_order_id = ?",transfer.getLong("id"));
    	long return_id = 0;
    	if(re.getLong("total")==0){
    		String orderNo = OrderNoGenerator.getNextOrderNo("HD");
            ReturnOrder returnOrder = new ReturnOrder();
            returnOrder.set("order_no", orderNo)
            .set("transaction_status", "新建")
            .set("customer_id", transfer.getLong("customer_id"))
            .set("creator", LoginUserController.getLoginUserId(this))
            .set("create_date", new Date())
            .set("transfer_order_id", transfer.getLong("id")).save();
            return_id = returnOrder.getLong("id");
            
            ReturnOrderController roController= new ReturnOrderController(); 
            //把运输单的应收带到回单中
            roController.tansferIncomeFinItemToReturnFinItem(returnOrder, Long.parseLong("0"),  transfer.getLong("id"));
            //直送时把保险单费用带到回单
            roController.addInsuranceFin(transfer, returnOrder);
            //TODO:根据合同生成费用
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + LoginUserController.getLoginUserName(this) + "'");
            roController.calculateChargeByCustomer(transfer, returnOrder.getLong("id"), users);
    	}
    	return return_id;
    }

   
    
    //TODO:  入库确认
    @RequiresPermissions(value = {PermissionConstant.PERMISSION_OT_UPDATE})
    @Before(Tx.class)
    public void warehousingConfirm() {
    	String departOrderId = getPara("departOrderId");
    	
    	List<DepartTransferOrder> departTransferOrders = DepartTransferOrder.dao.find("select * from depart_transfer where depart_id = ?", departOrderId);
    	for(DepartTransferOrder departTransferOrder : departTransferOrders){
    		TransferOrder transferOrder = TransferOrder.dao.findById(departTransferOrder.get("order_id"));
    		Long transferId = departTransferOrder.getLong("order_id");
    		if("ATM".equals(transferOrder.get("cargo_nature"))){
    			//这里只能算单品的总数,缺失判断不同货品的情况
        		//运输单中所有单品
        		String sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + transferId;
        		Record rec = Db.findFirst(sqlTotal);
        		Long detailTotal = rec.getLong("total");  //总数量
        		//运输单中此次发车单品数量
        		sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + transferId + " and depart_id = " + departOrderId;
        		rec = Db.findFirst(sqlTotal);
        		Long this_amount = rec.getLong("total");
        		
        		//自动生成配送单（每一单品----一张配送）
        		createDeliveryOrder(transferOrder,departOrderId);
        		
        		sqlTotal = "select count(1) total from transfer_order_item_detail where order_id = " + transferId + " and status = '已入库'";
        		rec = Db.findFirst(sqlTotal);    //运输单中已入库的数量(不包括这次)
        		Long departTotal2 = rec.getLong("total");
        		Long departTotal = this_amount + departTotal2;
        		if(detailTotal == departTotal){
    				transferOrder.set("status", "已完成");
    				transferOrder.update();
    				
    				TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
    				transferOrderMilestone = milestoneMessages(transferOrderMilestone);
    				transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
    				transferOrderMilestone.set("order_id", transferOrder.get("id"));
    				transferOrderMilestone.set("status", "已入库");
    				transferOrderMilestone.save();
        		}else{
    				TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
    				transferOrderMilestone = milestoneMessages(transferOrderMilestone);
    				transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
    				transferOrderMilestone.set("order_id", transferOrder.get("id"));
    				transferOrderMilestone.set("status", "部分已入库");
    				transferOrderMilestone.save();
        		}
        		
        		List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where order_id = " + transferId + " and depart_id = " + departOrderId);
        		for(TransferOrderItemDetail detail : transferOrderItemDetails){
        			detail.set("status", "已入库");
        			detail.update();
        		}		
    		}else{
    			//普货货品总数量
    			Record re = Db.findFirst("select sum(toi.amount) total from transfer_order_item toi where toi.order_id = ?",transferId);
    			double totalAmount = re.getDouble("total");     //总货品数量
    			TransferOrder transfer = TransferOrder.dao.findById(transferId);
    			double pickupAmount = 0.0;  //已入库的数量（包括这次）
    			double this_amount = 0.0;
    			double havingTotal = 0.0;
    			if(!transfer.getStr("operation_type").equals("out_source")){
    				re = Db.findFirst("select sum(amount) yishou from (select dt.* from depart_pickup dp"
    						+ " LEFT JOIN depart_order dor on dor.id = dp.depart_id"
    						+ " LEFT JOIN depart_transfer dt on dt.pickup_id = dp.pickup_id"
    						+ " where dor.STATUS = '已入库' and dt.order_id = ? and dt.twice_pickup_flag = 'N'  group by dt.id )A",transferId);
    				havingTotal = re.getDouble("yishou")==null?0:re.getDouble("yishou");   //已入库（不包括这次）
    				re = Db.findFirst("select sum(ifnull(dt.amount,0)) amount from depart_pickup dp"
    						+ " LEFT JOIN depart_transfer dt on dt.pickup_id = dp.pickup_id"
    						+ " where dp.depart_id is not null and dp.depart_id =?",departOrderId);
    				this_amount = re.getDouble("amount")==null?0:re.getDouble("amount");
    				pickupAmount = havingTotal + this_amount;	
    			}else{
    				pickupAmount = totalAmount; //外包（因无法多次发车，所以数量和总数量相同）
    			}
    			
    			TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
    			if(totalAmount == pickupAmount){
    				transferOrder.set("status", "已完成").update();
    				transferOrderMilestone.set("status", "已入库");
    			}else{
    				transferOrderMilestone.set("status", "部分已入库");
    			}
    			transferOrderMilestone = milestoneMessages(transferOrderMilestone);
				transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
				transferOrderMilestone.set("order_id", transferOrder.get("id"));
				transferOrderMilestone.save();
				
				//自动生成配送单（每一单品----一张配送）
        		//createDeliveryOrder(transferOrder,departOrderId);
    		}
    	}
    	DepartOrder departOrder = DepartOrder.dao.findById(departOrderId);
		departOrder.set("status", "已入库");
		departOrder.update();
		
		TransferOrderMilestone departOrderMilestone = new TransferOrderMilestone();
		departOrderMilestone = milestoneMessages(departOrderMilestone);
		departOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
		departOrderMilestone.set("status", "已入库");
		departOrderMilestone.set("depart_id", departOrder.get("id"));
		departOrderMilestone.save();
    	DepartOrderController.productInWarehouse(departOrderId);
    	renderJson("{\"success\":true}");        
    }
    
    @Before(Tx.class)
    public void createDeliveryOrder(TransferOrder transferOrder,String departOrderId){
    	Party party=Party.dao.findById(transferOrder.get("customer_id"));
    	DepartOrder departOrder1= DepartOrder.dao.findById(departOrderId);
    	if("Y".equals(party.get("is_auto_ps"))){
    		List<TransferOrderItemDetail> transferorderitemdetail =TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where order_id = "+transferOrder.get("id")+" and depart_id ="+ departOrderId);
    		for (TransferOrderItemDetail transferdetail:transferorderitemdetail) {
    			if(transferdetail.getLong("delivery_id")==null){
    				DeliveryOrder deliveryOrder = null;
            		String orderNo = OrderNoGenerator.getNextOrderNo("PS");
            		Date createDate = Calendar.getInstance().getTime();
            		String name = (String) currentUser.getPrincipal();
            		List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
            		Warehouse warehouse = Warehouse.dao.findFirst("SELECT * from warehouse where id=?",transferOrder.get("warehouse_id")); 
            		deliveryOrder = new DeliveryOrder();
            		deliveryOrder.set("order_no", orderNo)
    				.set("customer_id", transferOrder.get("customer_id"))
    				.set("notify_party_id", transferdetail.getLong("notify_party_id"))
    				.set("create_stamp", createDate).set("create_by", users.get(0).get("id")).set("status", "新建")
    				.set("route_from",transferOrder.get("route_to"))
    				.set("route_to",transferOrder.get("route_to"))
    				.set("pricetype", getPara("chargeType"))
    				.set("office_id", warehouse.get("office_id"))
    				.set("from_warehouse_id", transferOrder.get("warehouse_id"))
    				.set("cargo_nature", transferOrder.get("cargo_nature"))
    				.set("priceType", departOrder1.get("charge_type"))
    				.set("deliveryMode", "out_source")
    				.set("ref_no",transferOrder.getStr("sign_in_no"))
    				.set("warehouse_nature", "warehouseNatureNo")
    				.set("ltl_price_type", departOrder1.get("ltl_price_type")).set("car_type", departOrder1.get("car_type"))
    				.set("audit_status", "新建").set("sign_status", "未回单");
            		if(warehouse!=null){
                        deliveryOrder.set("sp_id", warehouse.get("sp_id"));
            			//deliveryOrder.set("sp_id", transferOrder.getLong("sp_id"));
            		}
            		deliveryOrder.save();
    				DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
    				deliveryOrderItem.set("delivery_id",deliveryOrder.get("id"))
    				.set("transfer_order_id",transferOrder.get("id"))
    				.set("transfer_no",transferOrder.get("order_no"))
    				.set("transfer_item_detail_id",transferdetail.getLong("id"))
    				.set("amount", 1);
    				deliveryOrderItem.save();
    				//在单品中设置delivery_id
    				TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao
    						.findById(transferdetail.getLong("id"));
    				transferOrderItemDetail.set("delivery_id",deliveryOrder.get("id"));
    				transferOrderItemDetail.set("is_delivered", true);
    				transferOrderItemDetail.update();
    			}
    		}
    	}
    }
    
    
    
    
    // 里程碑信息
    private TransferOrderMilestone milestoneMessages(TransferOrderMilestone transferOrderMilestone){
    	String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("order_id", getPara("order_id"));
    	return transferOrderMilestone;
    }
}
