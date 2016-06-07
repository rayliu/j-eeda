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
import models.UserLogin;
import models.yh.structure.Contentlet;
import models.yh.wx.WechatLocation;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
//import org.json.JSONObject;






import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

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

import controllers.yh.order.TransferOrderMilestoneController;
import controllers.yh.util.EedaHttpKit;

public class WxController extends ApiController {
	private Logger logger = Logger.getLogger(WxController.class);
	Subject currentUser = SecurityUtils.getSubject();
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
		System.out.println("jsapi_ticket : " + jsapi_ticket);
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
	
	//取网页授权access_token
	private String getOpenId(String code) throws Exception{
        String openId = "";
        String openIdUrl="https://api.weixin.qq.com/sns/oauth2/access_token?appid="+ApiConfigKit.getApiConfig().getAppId()
                +"&secret="+PropKit.get("appSecret")+"&code="+code+"&grant_type=authorization_code";
        logger.debug("get openIdUrl:"+openIdUrl);
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
                    logger.debug("errcode:"+jsonStr);
                    return "";
                }
                
                String accessToken = (String) json.get("access_token");
                openId = (String) json.get("openid");
                //String unionid = json.getString("unionid");
                logger.debug("accessToken:"+accessToken+", openid:"+openId);
               
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
        }
        return openId;
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
	
	//微信登录页面 - 未授权的用户不能查询
    public void index() {
        String openid = getPara("openid");
        String redirect = getPara("redirect");
        
        setAttr("openid", openid);
        setAttr("redirect", redirect);
        render("/yh/wx/login.html");
    }
    
    //微信登录后- 记录该用户的openid到对应的user
    @Before(Tx.class)
    public void wxlogin() {
        String user_name = getPara("user_name");
        String pwd = getPara("pwd");
        String openid = getPara("openid");
        String redirect = getPara("redirect");
        
        UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=? and password=?", user_name, pwd);
         
        if(user != null){
        	UsernamePasswordToken token = new UsernamePasswordToken(user_name, pwd);
            token.setRememberMe(true);
            currentUser.login(token);
            currentUser.getSession().setTimeout(-1000L);
            user.set("wechat_openid", openid).update();
            redirect(redirect+"?openid="+openid);
        }else{
            setAttr("err", true);
            setAttr("openid", openid);
            setAttr("redirect", redirect);
            render("/yh/wx/login.html");
        }
    }
    
	//回单上传附件页面 - 统一单号
	public void fileUpload()  throws Exception {
	    String openid = getPara("openid");
	    if(openid == null){
    	    logger.debug(getRequest().getQueryString());
    	    //第一步：用户同意授权，获取code
    	    String code = getPara("code");//该code用来向微信服务器请求获得openId
    	    logger.debug("code: " + code);
    	    //第二步：通过code换取网页授权access_token
    	    openid = getOpenId(code);
	    }   
    	    Record userRec = Db.findFirst("select * from user_login where wechat_openid =?", openid);
    	    if(userRec != null){
    	        setAttr("openid", openid);
    	        setAttr("userId",userRec.getLong("id"));
    	        setPageAttr("/wx/fileUpload");
    	        render("/yh/returnOrder/returnOrderUploadFile.html");
    	    }else{
    	        setAttr("openid", openid);
    	        setAttr("redirect", "fileUpload");
    	        render("/yh/wx/login.html");
    	    }
	    
	}
	
	//单据状态查询
    public void queryStatus() throws Exception{
        String openid = getPara("openid");
        if(openid == null){
            logger.debug(getRequest().getQueryString());
            //第一步：用户同意授权，获取code
            String code = getPara("code");//该code用来向微信服务器请求获得openId
            logger.debug("code: " + code);
            //第二步：通过code换取网页授权access_token
            openid = getOpenId(code);
        }
        Record userRec = Db.findFirst("select * from user_login where wechat_openid =?", openid);
        if(userRec != null){
            setAttr("orderNo", getPara());
            setPageAttr("/wx/queryStatus");
            render("/yh/wx/queryStatus.html");
        }else{
            setAttr("openid", openid);
            setAttr("redirect", "queryStatus");
            render("/yh/wx/login.html");
        }
    }
    
    //单据状态查询
    public void queryStatusJson() {
        String orderNo = getPara("orderNo").toUpperCase();
        String openid = getPara("openid");
        Record userRec = Db.findFirst("select * from user_login where wechat_openid =?", openid);
        //String sql = "";
        // if("YS".equals(orderNo.substring(0,2))){
        String sql1 = "select "
        	 		+ " (CONCAT('订单成功接收','-',cast(tor.create_stamp as char)) "
        	 		+ " ) transfer_order, "
        	 		+ " (select (select if((max(dor_pi.create_stamp)=dor_pi.create_stamp),concat((select if(dor_pi.status='新建','提货计划中','已提货')),'-',cast(dor_pi.create_stamp as char)),''))  "
        	 		+ " from depart_order dor_pi  "
        	 		+ " LEFT JOIN depart_transfer dt on dt.pickup_id = dor_pi.id "
        	 		+ " where dt.order_id = tor.id"
        	 		+ " ) pickup_order, "
        	 		+ " (select (select if((max(dor_de.create_stamp)=dor_de.create_stamp),concat((select if(dor_de.status='新建','发车计划中','已发车')),'-',cast(dor_de.create_stamp as char)),''))  "
        	 		+ " from depart_order dor_de  "
        	 		+ " LEFT JOIN depart_transfer dt on dt.depart_id = dor_de.id "
        	 		+ " where dt.order_id = tor.id "
        	 		+ " ) depart_order, "
        	 		+ " (select (select if((max(deo.create_stamp)=deo.create_stamp),concat((select if(deo.status='新建','配送计划中','已配送')),'-',cast(deo.create_stamp as char)),''))  "
        	 		+ " from delivery_order deo  "
        	 		+ " LEFT JOIN delivery_order_item doi on doi.delivery_id = deo.id "
        	 		+ " where doi.transfer_order_id = tor.id "
        	 		+ " ) delivery_order,"
        	 		+ " ifnull((select (select if((max(ror.create_date)=ror.create_date),concat((select if(ror.transaction_status='新建','签收计划中','已签收')),'-',cast(ror.create_date as char)),null))  "
        	 		+ " from return_order ror  "
        	 		+ " where ror.transfer_order_id = tor.id) "
        	 		+ " , "
        	 		+ " (select (select if((max(ror.create_date)=ror.create_date),concat((select if(ror.transaction_status='新建','签收计划中','已签收')),'-',cast(ror.create_date as char)),'')) "
        	 		+ " from return_order ror  "
        	 		+ " LEFT JOIN delivery_order_item doi on doi.delivery_id = ror.delivery_order_id "
        	 		+ " where doi.transfer_order_id = tor.id) "
        	 		+ " ) return_order"
        	 		+ " from transfer_order tor "
        	 		+ " where tor.customer_order_no ='"+ orderNo +"'"
            		+ " and tor.office_id in (select office_id from user_office where user_name='"+userRec.getStr("user_name")+"') "
            		+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+userRec.getStr("user_name")+"') " ;
        // }else{
        String sql2 = "select  "
        			+ " (select CONCAT('订单成功接收','-',cast(tor.create_stamp as char)) from transfer_order tor  "
        			+ " where tor.id = toid.order_id"
        			+ " ) transfer_order,"
        			+ " (select CONCAT(if(dor_pi.status='新建','提货计划中','已提货'),'-',cast(dor_pi.create_stamp as char)) from depart_order dor_pi  "
        			+ " where dor_pi.id = toid.pickup_id "
        			+ " ) pickup_order, "
        			+ " (select CONCAT(if(dor_de.status='新建','发车计划中','运输在途'),'-',cast(dor_de.create_stamp as char)) from depart_order dor_de  "
        			+ " where dor_de.id = toid.depart_id "
        			+ " ) depart_order, "
        			+ " (select CONCAT(if(deo.status='新建','配送计划中','配送在途'),'-',cast(deo.create_stamp as char)) from delivery_order deo  "
        			+ " where deo.id = toid.delivery_id "
        			+ " ) delivery_order, "
        			+ " (select CONCAT(if(ror.transaction_status='新建','签收计划中','已签收'),'-',cast(ror.create_date as char)) from return_order ror"
        			+ " LEFT JOIN delivery_order deo on deo.id = ror.delivery_order_id "
        			+ " where ror.transfer_order_id = toid.order_id or deo.id = toid.delivery_id "
        			+ " ) return_order "
        			+ " from transfer_order_item_detail toid"
        			+ " left join transfer_order tor on tor.id = toid.order_id  "
        			+ " where serial_no = '"+orderNo+"'"
        			+ " and tor.office_id in (select office_id from user_office where user_name='"+userRec.getStr("user_name")+"') "
            		+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+userRec.getStr("user_name")+"') " ;
       //  }
        
        List<Record> rec = null;
        rec = Db.find(sql1);
        if(rec.size()==0){
        	 rec = Db.find(sql2);
        };
        
        renderJson(rec);
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
	    String photo_type = getPara("photo_type");
	    ReturnOrder returnOrder = ReturnOrder.dao.findById(return_order_id);
	    if(returnOrder != null){
	        System.out.println("appId:" + PropKit.get("appId"));
            System.out.println("appSecret:" + PropKit.get("appSecret"));
            System.out.println("token:" + PropKit.get("token"));
	        
    	    Map<String, String> queryMap = new HashMap<String,String>();
    	    System.out.println("access_token:" + AccessTokenApi.getAccessToken().getAccessToken());
    	    System.out.println("media_id:" + serverId);
    	    queryMap.put("access_token", AccessTokenApi.getAccessToken().getAccessToken());
    	    queryMap.put("media_id", serverId);
    	    
    	    String fileFolder = getRequest().getSession().getServletContext().getRealPath("")+"/upload/img/";
    	    String fileName = EedaHttpKit.getFile(getMediaUrl, queryMap, fileFolder);
    	    OrderAttachmentFile orderAttachmentFile = new OrderAttachmentFile();
            orderAttachmentFile.set("order_id", return_order_id)
                    .set("order_type", orderAttachmentFile.OTFRT_TYPE_RETURN)
                    .set("file_path", fileName)
                    .set("photo_type", photo_type)
                    .save();
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
	
	//查找回单
	public void findReturnOrder() {
		String orderNo = getPara("orderNo").toUpperCase();
		String customer = getPara("customer").toUpperCase();
		//单号不为空时
		if(orderNo != null && !"".equals(orderNo)){
			//单号可能为序列号/签收单号/运输单号
		    String sql ="select * from(select ifnull(ro.id,ro2.id) id, ifnull(toid.serial_no,'') serial_no, "
		    		+ " ifnull(dor.status,dep.`STATUS`) status,"
		    		+ "	dor.status del_status,"
		    		+ " dep.id depart_id,dep.`STATUS` dep_status,"
		    		+ "	IfNULL(dor.ref_no,'') ref_no ,"
		    		+ " IfNULL(tor.order_no,'') to_order_no,IfNULL(tor.customer_order_no,'') customer_order_no ,"
		    		+ " ifnull(ro.order_no,ro2.order_no) order_no, toid.order_id, toid.delivery_id,c.abbr,c.id cid "
		    		+ " from transfer_order_item_detail toid"
		            + " left join return_order ro on toid.order_id=ro.transfer_order_id "
		            + " LEFT JOIN return_order ro2 ON toid.delivery_id = ro2.delivery_order_id"
		            + " left join delivery_order dor on dor.id = toid.delivery_id"
		            + " left join transfer_order tor on tor.id = toid.order_id"
		            + "	LEFT JOIN depart_order dep on dep.id = toid.depart_id" 
		            + " left join party p on p.id=ro.customer_id"
		            + " left join contact c on c.id=p.contact_id"
		            + " ) A"
		            + " where ("
	                + " A.serial_no = '"+orderNo+"'"
	                + " or A.ref_no = '"+orderNo+"'"
	                + " or A.to_order_no = '"+orderNo+"') "
	                + " and (A.del_status not in('新建','计划中') "
	                + " and A.dep_status not in('新建'))";
		    if(!"-1".equals(customer)){
		    	sql += " and A.cid='"+customer+"'";
		    }
			List<Record> list = Db.find(sql);
			if(list.size() > 0){
			    renderJson(list);
			}else{
			    renderJson(new Record());
			}
		}
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
	

	
	//查询调车单
	public void searchPickupOrder() throws Exception{	
		String openid = getPara("openid");
        if(openid == null){
            logger.debug(getRequest().getQueryString());
            //第一步：用户同意授权，获取code
            String code = getPara("code");//该code用来向微信服务器请求获得openId
            logger.debug("code: " + code);
            //第二步：通过code换取网页授权access_token
            openid = getOpenId(code);
        }
        Record userRec = Db.findFirst("select * from user_login where wechat_openid =?", openid);
        
        if(userRec != null){
        	setAttr("openid", openid);
        	setPageAttr("/wx/searchPickupOrder");
            render("/yh/wx/yh/searchPickupOrder.html");
        }else{
        	setAttr("openid", openid);
            setAttr("redirect", "searchPickupOrder");
            render("/yh/wx/login.html");
        }
	}
	
	public void searchTransferOrder(){
		String openid = getPara("openid");
		String departOrder = getPara("orderNo").toUpperCase();
		String carNo = getPara("carNo").toUpperCase();
		Record userRec = Db.findFirst("select * from user_login where wechat_openid =?", openid);
		String conditions=" where 1 = 1 ";
		if(StringUtils.isNotEmpty(departOrder)){
			conditions += " and dor.depart_no like '%"+departOrder+"%'";
		}
			
		if(StringUtils.isNotEmpty(carNo)){
			conditions += " and dor.car_no like '%"+carNo+"%'";
		}

		String sql = "select tor.id,dor.id pickup_id,dor.depart_no pickup_no,dt.amount,dor.car_no,tor.customer_id,tor.order_no,l.`name` route_to, "
				+ " (select count(*) from depart_pickup where order_id = tor.id and pickup_id = dor.id and depart_id is not null) disabled ,"
				+ " dor.status status "
				+ " from transfer_order tor"
				+ " LEFT JOIN depart_transfer dt on dt.order_id = tor.id"
				+ " LEFT JOIN depart_order dor on dor.id = dt.pickup_id"
				+ " LEFT JOIN location l on l.`code` = tor.route_to"
				+ conditions
				+ " and tor.office_id in (select office_id from user_office where user_name='"+userRec.getStr("user_name")+"') "
				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+userRec.getStr("user_name")+"')"
				+ " ";
		List<Record> re = Db.find("select * from ("+sql+") A order by A.disabled,A.status ");
		renderJson(re);
	}	
}
