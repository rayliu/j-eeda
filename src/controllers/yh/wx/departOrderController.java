package controllers.yh.wx;

import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.DepartOrder;
import models.DepartOrderFinItem;
import models.DepartPickupOrder;
import models.DepartTransferOrder;
import models.OrderAttachmentFile;
import models.Party;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.yh.profile.Contact;
import models.yh.structure.Contentlet;
import models.yh.wx.WechatLocation;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
//import org.json.JSONObject;









import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.apache.tools.ant.taskdefs.Definer.Format;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.jfinal.weixin.eeda.SignKit;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.JsTicketApi;
import com.jfinal.weixin.sdk.jfinal.ApiController;

import controllers.yh.LoginUserController;
import controllers.yh.util.EedaHttpKit;
import controllers.yh.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class departOrderController extends ApiController {
	private Logger logger = Logger.getLogger(departOrderController.class);
	
	private static String getMediaUrl = "http://file.api.weixin.qq.com/cgi-bin/media/get";
	/**
	 * 如果要支持多公众账号，只需要在此返回各个公众号对应的  ApiConfig 对象即可
	 * 可以通过在请求 url 中挂参数来动态从数据库中获取 ApiConfig 属性值
	 */
	public ApiConfig getApiConfig() {
		ApiConfig ac = new ApiConfig();
		
		// 配置微信 API 相关常量
		ac.setToken(PropKit.get("token"));
		ac.setAppId(PropKit.get("appId"));
		ac.setAppSecret(PropKit.get("appSecret"));
		
		/**
		 *  是否对消息进行加密，对应于微信平台的消息加解密方式：
		 *  1：true进行加密且必须配置 encodingAesKey
		 *  2：false采用明文模式，同时也支持混合模式
		 */
		ac.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
		ac.setEncodingAesKey(PropKit.get("encodingAesKey", "setting it in config file"));
		return ac;
	}
	

	//查询调车单
//	public void index() {		
//		render("/yh/wx/yh/searchPickupOrder.html");
//	}
	
	public void createDepartOrder(){
		String openid = getPara("openid");
		setAttr("openid", openid);
		
		String transferIds = getPara("transferIds");
		setAttr("transferIds", transferIds);
		
		String pickupOrder = getPara("orderNo");
		setAttr("pickupOrder", pickupOrder);
		
		List<Record> re = Db.find("select order_no from transfer_order where id in ("+transferIds+")");
		setAttr("transferList", re);
		
		String[] array = transferIds.split(",");
		TransferOrder transfer = TransferOrder.dao.findById(array[0]);
		setAttr("transfer", transfer);
		
		//获取供应商
		long customer_id = transfer.getLong("customer_id");
		String route_from = transfer.getStr("route_from");
		String route_to = transfer.getStr("route_to");

		Record c_r_p  = Db.findFirst("SELECT cr.*,l_f.name routeFrom,l_t.name routeTo,"
				+ " (case"
				+ " WHEN cr.charge_type = 'perCargo' and cr.ltl_price_type = 'perCBM' "
				+ " THEN '零担(每立方米)'"
				+ " WHEN cr.charge_type = 'perCargo' and cr.ltl_price_type = 'perKg' "
				+ " THEN '零担(每公斤)'"
				+ " WHEN cr.charge_type = 'perCargo' and cr.ltl_price_type = 'perTon'"
				+ " THEN '零担(每吨)'"
				+ " when cr.charge_type='perCar'"
				+ " then CONCAT('整车(',cr.car_type,')')"
				+ " else '计件'"
				+ " end) chargeType,"
				+ " c_c.company_name customer_name,"
				+ " c.company_name FROM `customer_route_provider` cr"
				+ " LEFT JOIN contact c on c.id = cr.sp_id"
				+ " LEFT JOIN contact c_c on c_c.id = cr.customer_id"
				+ " LEFT JOIN location l_t on l_t.code = cr.location_to"
				+ " LEFT JOIN location l_f on l_f.code = cr.location_from"
				+ " where cr.customer_id = "+customer_id+" and cr.location_from ='"+route_from+"' and cr.location_to = '"+ route_to + "'");
		if(c_r_p==null){
			c_r_p =Db.findFirst("select tor.*,l_f.name routeFrom,l_t.name routeTo,"
				+ " (case"
				+ " WHEN tor.charge_type = 'perCargo' and tor.ltl_unit_type = 'perCBM' "
				+ " THEN '零担(每立方米)'"
				+ " WHEN tor.charge_type = 'perCargo' and tor.ltl_unit_type = 'perKg' "
				+ " THEN '零担(每公斤)'"
				+ " WHEN tor.charge_type = 'perCargo' and tor.ltl_unit_type = 'perTon'"
				+ " THEN '零担(每吨)'"
				+ " when tor.charge_type='perCar'"
				+ " then CONCAT('整车(',tor.car_type,')')"
				+ " else '计件'"
				+ " end) chargeType,"
				+ " c_c.company_name customer_name from transfer_order tor"
				+ " LEFT JOIN contact c_c on c_c.id = tor.customer_id"
				+ " LEFT JOIN location l_t on l_t.code = tor.route_to"
				+ " LEFT JOIN location l_f on l_f.code = tor.route_from"
				+ " where tor.id="+array[0]);
		}
		setAttr("c_r_p", c_r_p);
		
		render("/yh/wx/yh/createDepartOrder.html");
	}
	
	@Before(Tx.class)
	public void departOrderCreate(){
		String openid = getPara("openid");
		String sp_id = getPara("sp_id");
		String route_from = getPara("route_from");
		String route_to = getPara("route_to");
		String charge_type = getPara("charge_type");
		String car_type = getPara("car_type");
		String ltl_price_type = getPara("ltl_price_type");
		
		String weight = getPara("weigh");
		String volume = getPara("volume");
		String pay = getPara("pay");
		String booking_note_number = getPara("booking_note_number");
		String arrival_time = getPara("arrival_time");
		String pickupOrderNo = getPara("pickupOrder");
		String transferIds = getPara("transferIds");  //运单IDs
		
		Record userRec = Db.findFirst("select * from user_login where wechat_openid =?", openid);
		long userId = userRec.getLong("id");
		//创建发车单
		DepartOrder dp = new DepartOrder();
		dp.set("charge_type", charge_type)
				.set("route_from", route_from)
				.set("route_to", route_to)
				.set("ltl_price_type", ltl_price_type)
				.set("car_type", car_type)
				.set("booking_note_number", booking_note_number)
				.set("departure_time", new Date())
				.set("create_by", userId)
				.set("create_stamp", new Date())
				.set("combine_type", DepartOrder.COMBINE_TYPE_DEPART)
				.set("depart_no", OrderNoGenerator.getNextOrderNo("FC"))
				.set("weight", weight)
				.set("volume", volume)
				.set("status", "运输在途")
				.set("audit_status", "新建")
				.set("sp_id", sp_id);
		if (arrival_time != null) {
			dp.set("arrival_time", arrival_time);
		}
		dp.set("sign_status", "未回单");
		dp.save();	
		
		//保存中间表
		DepartOrder pickupOrder = DepartOrder.dao.findFirst("select * from depart_order where depart_no = '"+pickupOrderNo+"'");
		long pickupId = pickupOrder.getLong("id");
		long departId = dp.getLong("id");
		String[] array = transferIds.split(",");
		for (int i = 0; i < array.length; i++) {
			String transferId = array[i];
			
			String sql = "select * from transfer_order_item_detail toid where toid.order_id = "
					+transferId+" and toid.pickup_id ="+pickupId;
			List<TransferOrderItemDetail> detail = TransferOrderItemDetail.dao.find(sql);
			for(TransferOrderItemDetail re :detail){
				re.set("depart_id", departId).update();
			}
			
			//保存depart_pickup
			DepartPickupOrder departPickup = new DepartPickupOrder();
			departPickup.set("depart_id", departId)
					.set("pickup_id", pickupId)
					.set("order_id", transferId).save();

			//保存depart_transfer
			TransferOrder transferOrder = TransferOrder.dao.findById(transferIds);
			DepartTransferOrder departTransferOrder = new DepartTransferOrder();
			departTransferOrder.set("depart_id", departId);
			departTransferOrder.set("order_id",transferId);
			departTransferOrder.set("transfer_order_no", transferOrder.get("order_no"));
			departTransferOrder.save();
		
            //更新运输单的日记账 
			TransferOrderMilestone departOrderMilestone = new TransferOrderMilestone();
	    	departOrderMilestone.set("create_by", userId);
	    	departOrderMilestone.set("create_stamp", new Date());
	        departOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
	        departOrderMilestone.set("location", "");
	        departOrderMilestone.set("order_id", transferId);
	        
			// 验证是否已全部发车完成，调车单全部提货完成的情况下进行判断
			if (TransferOrder.ASSIGN_STATUS_ALL.equals(transferOrder
					.get("pickup_assign_status"))) {
				if (transferOrder.get("cargo_nature").equals("ATM")) {
					// 运输单单品总数
					Record totalTransferOrderAmount = Db
							.findFirst("select count(0) total from transfer_order_item_detail where order_id = "
									+ transferIds);
					// 总提货数量（之前+现在）
					Record totalPickAmount = Db
							.findFirst("select count(0) total from transfer_order_item_detail where depart_id is not null and order_id = "
									+ transferIds);
					// 运输单
					if (totalPickAmount.getLong("total") == totalTransferOrderAmount
							.getLong("total")) {
						transferOrder.set("depart_assign_status",
								TransferOrder.ASSIGN_STATUS_ALL);
						departOrderMilestone.set("status", "已发车");
					} else {
						transferOrder.set("depart_assign_status",
								TransferOrder.ASSIGN_STATUS_PARTIAL);
						departOrderMilestone.set("status", "部分已发车");	
					}
				} else {
					//普货已发车总数量
					Record re = Db.findFirst("select sum(amount) total from transfer_order_item where order_id = ?", transferIds);
				    double total = re.getDouble("total");
					
				    
					if(!transferOrder.getStr("operation_type").equals("out_source")){
						 re = Db.findFirst("select sum(amount) yifa from (select dt.* from depart_transfer dt"
										+ " LEFT JOIN depart_pickup dp on dt.pickup_id = dp.pickup_id"
										+ " where dp.depart_id is not null "
										+ " and dt.order_id = ? group by dt.id ) a", transferIds);
						double amount = re.getDouble("yifa");//已做发车单的数量（之前+现在）
						
						if(total == amount){
							transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
							departOrderMilestone.set("status", "已发车");
						}else{
							transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_PARTIAL);
							departOrderMilestone.set("status", "部分已发车");
						}
					}else{
						transferOrder.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_ALL);
					}
				}
				departOrderMilestone.save();
			} else {
				transferOrder.set("depart_assign_status",
						TransferOrder.ASSIGN_STATUS_PARTIAL);
			}
			transferOrder.update();
		}
		//保存 应付
		departOrderFinItem(departId,pay);
		
		// 保存发车里程碑
		saveDepartOrderMilestone(departId,userId);
		
		renderJson(dp);
	}
	
	// 保存发车里程碑
	private void saveDepartOrderMilestone(long departId,long userId) {
		TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
		transferOrderMilestone.set("status", "新建");
		transferOrderMilestone.set("create_by", userId);
		transferOrderMilestone.set("location", "");
		transferOrderMilestone.set("exception_record", "");
		transferOrderMilestone.set("create_stamp", new Date());
		transferOrderMilestone.set("type",
				TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
		transferOrderMilestone.set("depart_id", departId);
		transferOrderMilestone.save();
		
		TransferOrderMilestone nilestone = new TransferOrderMilestone();
		nilestone.set("status", "已发车");
		nilestone.set("create_by", userId);
		nilestone.set("location", "");
		nilestone.set("exception_record", "");
		nilestone.set("create_stamp", new Date());
		nilestone.set("type",
				TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
		nilestone.set("depart_id", departId);
		nilestone.save();
	}
	
	//保存应付
	public void departOrderFinItem(long departId,String pay) {
		DepartOrderFinItem departOrderFinItem = new DepartOrderFinItem();
		departOrderFinItem.set("depart_order_id", departId);
		departOrderFinItem.set("fin_item_id", 1);
		departOrderFinItem.set("amount", pay);
		departOrderFinItem.set("status", "新建");
		departOrderFinItem.set("create_date", new Date());
		departOrderFinItem.set("create_name", "user");
        departOrderFinItem.save();
	}
}
