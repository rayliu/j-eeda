package controllers.yh.wx;

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

import models.OrderAttachmentFile;
import models.Party;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderItemDetail;
import models.yh.structure.Contentlet;
import models.yh.wx.WechatLocation;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
//import org.json.JSONObject;

import com.google.gson.Gson;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.jfinal.weixin.demo.SignKit;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.JsTicketApi;
import com.jfinal.weixin.sdk.jfinal.ApiController;

import controllers.yh.util.EedaHttpKit;

public class WxController extends ApiController {
	private Logger logger = Logger.getLogger(WxController.class);
	
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
	
	private void setPageAttr(String contextPath) {
	    
	    
		//这里动态处理URL
		HttpServletRequest request = this.getRequest();
		logger.debug("绝对路径 = " + request.getSession().getServletContext().getRealPath(""));
		String path = request.getContextPath();
	    String basePath = request.getScheme()+"://"+request.getServerName();
	    
	    String url = basePath+contextPath;
	    if(request.getQueryString()!=null){
	    	url=url	+"?"+request.getQueryString();
	    }
	    logger.debug("url = "+url);
	    
		String jsapi_ticket = JsTicketApi.getJsTicket().getJsTicket();
		
		Map<String, String> m = SignKit.sign(jsapi_ticket, url);
		
		String appId = ApiConfigKit.getApiConfig().getAppId();
		
		setAttr("appId", appId);
		setAttr("appSecret", PropKit.get("appSecret"));
		setAttr("timestamp", m.get("timestamp"));
		setAttr("nonceStr", m.get("nonceStr"));
		setAttr("signature", m.get("signature"));
		setAttr("ticket", m.get("jsapi_ticket"));
		setAttr("token", PropKit.get("token"));
	}
	
	public void getWechatUserName() throws Exception{
		String code = getPara("code");
		String openId = getPara("openId");
				
		logger.debug("getWechatUserName...  code:"+code);
		String openIdUrl="https://api.weixin.qq.com/sns/oauth2/access_token?appid="+ApiConfigKit.getApiConfig().getAppId()
				+"&secret="+PropKit.get("appSecret")+"&code="+code+"&grant_type=authorization_code";
		logger.debug("  openIdUrl:"+openIdUrl);
		String status="ok";
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
        try {                        
            HttpGet httpGet = new HttpGet(openIdUrl);
            CloseableHttpResponse response1 = httpclient.execute(httpGet);
            try {
                HttpEntity entity = response1.getEntity();
                String jsonStr=EntityUtils.toString(entity);
                Gson gson = new Gson();
        		Map<String, ?> json = gson.fromJson(jsonStr, HashMap.class);
//                JSONObject json = new JsonObject(jsonStr);
                logger.debug("json:"+jsonStr);
                                
                if(json.get("errcode")!=null){
                	status="error";
                	renderJson(jsonStr);
                	return;
                }
                
                String accessToken = (String) json.get("access_token");
                String openid = (String) json.get("openid");
                //String unionid = json.getString("unionid");
                logger.debug("accessToken:"+accessToken+", openid:"+openid);
                               
                String nickname = getUserNickName(accessToken, openid);
                logger.debug("nickname:"+nickname);
               
                logger.debug("json:"+json.toString());
                renderJson("{\"nickname\":\""+nickname+"\", \"openid\":\""+openid+"\"}");
            }catch(Exception e){
            	status="error";
            	logger.debug("1............"+e.getMessage());
            	e.printStackTrace();
            } finally {
                response1.close();
            }
        }catch(Exception e){
        	status="error";
        	logger.debug("2............"+e.getMessage());
        	e.printStackTrace();
        } finally {
            httpclient.close();
            //renderJson("{\"status\":\""+status+"\"}");
        }
	}
	
	private String getUserNickName(String accessToken, String openid) throws Exception{
		String userName="";
		
		String userInfoUrl="https://api.weixin.qq.com/sns/userinfo?access_token="+accessToken+"&openid="+openid+"&lang=zh_CN";
		CloseableHttpClient httpclient = HttpClients.createDefault();
        try {            
            HttpGet httpGet = new HttpGet(userInfoUrl);
            CloseableHttpResponse response1 = httpclient.execute(httpGet);
            try {
                logger.debug("userInfoUrl:"+userInfoUrl);
                httpGet = new HttpGet(userInfoUrl);
                response1 = httpclient.execute(httpGet);
                HttpEntity entity = response1.getEntity();
                
                String jsonStr=EntityUtils.toString(entity);
                Gson gson = new Gson();
        		Map<String, ?> json = gson.fromJson(jsonStr, HashMap.class);
//                JSONObject json = new JSONObject(jsonStr); 
                logger.debug("json2:"+jsonStr);
                
                userName = (String) json.get("nickname");
                logger.debug("nickname:"+userName);
                //json.append("nickname", nickname);
                
                logger.debug("json:"+json.toString());
            }catch(Exception e){
            	logger.debug("getUserName()............"+e.getMessage());
            	e.printStackTrace();
            	throw e;
            } finally {
                response1.close();
            }
        }catch(Exception e){
        	logger.debug("getUserName()............"+e.getMessage());
        	e.printStackTrace();
        	throw e;
        } finally {
            httpclient.close();
        }
		return userName;
	}
	
	//微信JS demo页面，方便参考
	public void demo() {
		setPageAttr("/wx/demo");		
		render("/yh/wx/demo.html");
	}
	//汇报位置
	public void myLocation() {
		setPageAttr("/wx/myLocation");		
		render("/yh/wx/location.html");
	}
	//回单上传附件页面
	public void ro_filing() {
		setAttr("type", "default");
		setAttr("orderNo", getPara());
		setPageAttr("/wx/ro_filing");
		render("/yh/returnOrder/returnOrderFiling.html");
	}
	//回单上传附件页面 - 直送
	public void directSend() {
		setAttr("type", "directSend");
		setAttr("transferOrderNo", getPara());
		setPageAttr("/wx/directSend");
		render("/yh/returnOrder/returnOrderFiling.html");
	}
	//回单上传附件页面 - 配送
	public void distribution() {
		setAttr("type", "distribution");
		setPageAttr("/wx/distribution");
		render("/yh/returnOrder/returnOrderFiling.html");
	}
	
	//回单上传附件页面 - 统一单号
	public void fileUpload() {
		setAttr("orderNo", getPara());
		setPageAttr("/wx/fileUpload");
		render("/yh/returnOrder/returnOrderUploadFile.html");
	}
	
	//客户查询
	public void searchOrder() {
		setPageAttr("/wx/searchOrder");
		render("/yh/wx/searchOrder.html");
	}
	
	//扫单助手
	public void scanOrder() {
		setPageAttr("/wx/scanOrder");
		render("/yh/wx/scanOrder.html");
	}
	
	//获取回单数据
	public void getRo() {
		String type = getPara("type");
		String orderNo = getPara("orderNo");
		String transferOrderNo = getPara("transferOrderNo");
		String customerOrderNo = getPara("customerOrderNo");
		String serialNo = getPara("serialNo");
		String customerId = getPara("customerId");
		ReturnOrder returnOrder = null;
		if("default".equals(type)){
			if(orderNo != null)
				orderNo=orderNo.toUpperCase();
			returnOrder = ReturnOrder.dao.findFirst("select * from return_order where order_no=?",orderNo);
		}else if("directSend".equals(type)){
			//直送，运输单号、客户订单号
			TransferOrder order = null;
			if(transferOrderNo != null && !"".equals(transferOrderNo) && customerOrderNo != null && !"".equals(customerOrderNo)){
				transferOrderNo = transferOrderNo.toUpperCase();
				order = TransferOrder.dao.findFirst("select id from transfer_order where order_no = ? and customer_order_no = ?",transferOrderNo,customerOrderNo);
			}else if(transferOrderNo != null && !"".equals(transferOrderNo)){
				transferOrderNo = transferOrderNo.toUpperCase();
				order = TransferOrder.dao.findFirst("select id from transfer_order where order_no = ?",transferOrderNo);
			}else if(customerOrderNo != null && !"".equals(customerOrderNo))
				order = TransferOrder.dao.findFirst("select id from transfer_order where customer_order_no = ?",customerOrderNo);
			if(order != null)
				returnOrder = ReturnOrder.dao.findFirst("select * from return_order where transfer_order_id = ?",order.get("id"));
		}else if("distribution".equals(type)){
			//配送，序列号、供应商
			List<TransferOrderItemDetail> detailList = new ArrayList<TransferOrderItemDetail>();
			if(serialNo != null && !"".equals(serialNo) && customerId != null && !"".equals(customerId)){
				detailList = TransferOrderItemDetail.dao.find("select toid.delivery_id from transfer_order_item_detail toid left join transfer_order tor on tor.id = toid.order_id where toid.serial_no = ? and tor.customer_id = ? and toid.delivery_id is not null",serialNo,customerId);
			}else if(serialNo != null && !"".equals(serialNo)){
				detailList = TransferOrderItemDetail.dao.find("select delivery_id from transfer_order_item_detail where serial_no = ? and delivery_id is not null",serialNo);
			}
			//序列号唯一
			if(detailList.size() == 1)
				returnOrder = ReturnOrder.dao.findFirst("select * from return_order where delivery_order_id = ?",detailList.get(0).get("delivery_id"));
		}
		if(returnOrder==null)
			returnOrder = new ReturnOrder();
		renderJson(returnOrder);
	}
	
	public void saveReturnOrderPic(){
	    String return_order_id = getPara("return_order_id");
	    String serverId = getPara("serverId");
	    ReturnOrder returnOrder = ReturnOrder.dao.findById(return_order_id);
	    if(returnOrder != null){
    	    Map<String, String> queryMap = new HashMap<String,String>();
    	    logger.debug("access_token:" + AccessTokenApi.getAccessToken().getAccessToken());
    	    queryMap.put("access_token", AccessTokenApi.getAccessToken().getAccessToken());
    	    queryMap.put("media_id", serverId);
    	    
    	    String fileFolder = getRequest().getSession().getServletContext().getRealPath("")+"/upload/img/";
    	    String fileName = EedaHttpKit.getFile(getMediaUrl, queryMap, fileFolder);
    	    OrderAttachmentFile orderAttachmentFile = new OrderAttachmentFile();
            orderAttachmentFile.set("order_id", return_order_id)
                    .set("order_type", orderAttachmentFile.OTFRT_TYPE_RETURN)
                    .set("file_path", fileName).save();
    	    renderText("OK");
	    }else{
	        renderText("FAIL");
	    }
	    
	}
	
	public void saveFile(){
    	String id = getPara("return_id");
    	List<UploadFile> uploadFiles = getFiles("img");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	List<String> fileNames = new ArrayList<String>();
    	ReturnOrder returnOrder = ReturnOrder.dao.findById(id);
    	boolean result = true;
		if(returnOrder != null){
	    	for (int i = 0; i < uploadFiles.size(); i++) {
	    		File file = uploadFiles.get(i).getFile();
	    		String fileName = file.getName();
	    		long fileSize = 0;
                try {
                	FileInputStream fis = new FileInputStream(file);
                	fileSize = fis.available();
				} catch (FileNotFoundException e) {
					System.out.println("读取文件大小,文件没有找到");
				} catch (IOException e) {
					System.out.println("读取文件大小,读取出错");
				}
                fileNames.add(fileName+"("+new BigDecimal(fileSize/1024).setScale(0, BigDecimal.ROUND_HALF_UP)+"KB)");
	    		String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
	    		if(("gif".equals(suffix) || "jpeg".equals(suffix) || "png".equals(suffix) || "jpg".equals(suffix)) && fileSize > 0){
        			OrderAttachmentFile orderAttachmentFile = new OrderAttachmentFile();
        			orderAttachmentFile.set("order_id", id).set("order_type", orderAttachmentFile.OTFRT_TYPE_RETURN).set("file_path", uploadFiles.get(0).getFileName()).save();
	    		}else{
	    			result = false;
	    			break;
	    		}
			}
		}else{
			result = false;
		}
		if(result){
			resultMap.put("result", "true");
	    	resultMap.put("cause", fileNames);
		}else{
			resultMap.put("result", "false");
	    	resultMap.put("cause", "上传失败!");
		}
    	renderJson(resultMap);
    }
	
	public void findAllCustomer() {
		String input = getPara("input");
        List<Record> locationList = new ArrayList<Record>();
        if (input.trim().length() > 0) 
            locationList = Db.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '" + Party.PARTY_TYPE_CUSTOMER + "' and c.company_name like '%"+input+"%'");
        else 
            locationList = Db.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '" + Party.PARTY_TYPE_CUSTOMER + "'");
        renderJson(locationList);
	}
	
	//获取回单数据
	public void findReturnOrder() {
		String orderNo = getPara("orderNo").toUpperCase();
		String serialNo = getPara("serialNo");
		String customerId = getPara("customerId");
		ReturnOrder returnOrder = null;
		//单号不为空时
		if(orderNo != null && !"".equals(orderNo)){
			try {
				String orderHead = orderNo.substring(0, 2);
				if(orderHead.equals("HD")){
					returnOrder = ReturnOrder.dao.findFirst("select * from return_order where order_no=?",orderNo);
				}else if(orderHead.equals("YS")){
					TransferOrder order = TransferOrder.dao.findFirst("select id from transfer_order where order_no = ?",orderNo);
					if(order != null)
						returnOrder = ReturnOrder.dao.findFirst("select * from return_order where transfer_order_id = ?",order.get("id"));
				}else{
					//单号为客户订单号时
					TransferOrder order = TransferOrder.dao.findFirst("select id from transfer_order where customer_order_no = ?",orderNo);
					if(order != null)
						returnOrder = ReturnOrder.dao.findFirst("select * from return_order where transfer_order_id = ?",order.get("id"));
				}
			} catch (Exception e) {
				//单号为客户订单号时
				TransferOrder order = TransferOrder.dao.findFirst("select id from transfer_order where customer_order_no = ?",orderNo);
				if(order != null)
					returnOrder = ReturnOrder.dao.findFirst("select * from return_order where transfer_order_id = ?",order.get("id"));
			}
		}else{
			//单号为空，则默认为配送签收
			List<TransferOrderItemDetail> detailList = new ArrayList<TransferOrderItemDetail>();
			if(serialNo != null && !"".equals(serialNo) && customerId != null && !"".equals(customerId)){
				detailList = TransferOrderItemDetail.dao.find("select toid.delivery_id from transfer_order_item_detail toid left join transfer_order tor on tor.id = toid.order_id where toid.serial_no = ? and tor.customer_id = ? and toid.delivery_id is not null",serialNo,customerId);
			}else if(serialNo != null && !"".equals(serialNo)){
				detailList = TransferOrderItemDetail.dao.find("select delivery_id from transfer_order_item_detail where serial_no = ? and delivery_id is not null",serialNo);
			}
			//序列号唯一
			if(detailList.size() == 1)
				returnOrder = ReturnOrder.dao.findFirst("select * from return_order where delivery_order_id = ?",detailList.get(0).get("delivery_id"));
		}
		if(returnOrder==null)
			returnOrder = new ReturnOrder();
		renderJson(returnOrder);
	}
	
	
	
	public void findOrderNo(){
		String serialNo = getPara("serialNo").trim();
		String sql = "";
		serialNo = serialNo.toUpperCase();
		String orderHead = serialNo.substring(0, 2);
		if(orderHead.equals("PS")){
			sql ="select de.order_no, (select '配送单') as order_type, de.status, loc.name as beginCity, loc2.name as endCity, con.company_name as sp_name, con2.company_name as customer_name from delivery_order de"
					+ " left join location loc on loc.code = de.route_from"
					+ " left join location loc2 on loc2.code = de.route_to"
					+ " left join party p on p.id = de.sp_id"
					+ " left join contact con on con.id = p.contact_id"
					+ " left join party pa on pa.id = de.customer_id"
					+ " left join contact con2 on con2.id = pa.contact_id"
					+ " where de.order_no = '"+serialNo+"'";
		}else{
			sql="select tr.order_no,tr.order_type, tr.status, loc.name as beginCity ,loc2.name as endCity ,con.company_name as sp_name ,con2.company_name as customer_name from transfer_order tr"
					+ " left join location loc on loc.code = tr.route_from"
					+ " left join location loc2 on loc2.code = tr.route_to"
					+ " left join party p on p.id = tr.sp_id"
					+ " left join contact con on con.id = p.contact_id"
					+ " left join party pa on pa.id = tr.customer_id"
					+ " left join contact con2 on con2.id=pa.contact_id"
					+ " where tr.order_no='"+serialNo+"'";
		}
		Record transferOrder = Db.findFirst(sql);
		if(transferOrder == null)
			transferOrder = new Record();
		renderJson(transferOrder);
	}
	
	public void saveLocationInfo(){
		String longitude = getPara("longitude");
		String latitude = getPara("latitude");
		String address = getPara("address");
		String wechatOpenId = getPara("openId");
		WechatLocation  wechatLocation = new WechatLocation();
		wechatLocation.set("wechat_openid", wechatOpenId).set("longitude", longitude).set("latitude", latitude).set("address", address).set("update_stamp", new Date()).save();
		renderJson(wechatLocation);
	}
	
	public void saveScanResult(){
		String resultStr = getPara("serialNo");
		
		Contentlet scanOrder = new Contentlet();
		scanOrder.set("structure_id", 1).set("text1", resultStr).set("date1", new Date()).save();
		renderJson("{\"status\":\"ok\"}");
	}
}
