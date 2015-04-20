package controllers.yh.wx;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.OrderAttachmentFile;
import models.ReturnOrder;
import models.TransferOrder;
import models.TransferOrderItemDetail;
import models.UserOffice;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.jfinal.weixin.demo.SignKit;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.JsTicketApi;
import com.jfinal.weixin.sdk.jfinal.ApiController;

public class WxController extends ApiController {
	
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
	
	private void setPageAttr(String url) {
		String jsapi_ticket = JsTicketApi.getJsTicket().getJsTicket();
		
		Map<String, String> m = SignKit.sign(jsapi_ticket, url);//这里需要动态处理
		
		String appId = ApiConfigKit.getApiConfig().getAppId();
		
		setAttr("appId", appId);
		setAttr("timestamp", m.get("timestamp"));
		setAttr("nonceStr", m.get("nonceStr"));
		setAttr("signature", m.get("signature"));
	}
	
	//微信JS demo页面，方便参考
	public void demo() {
		setPageAttr("http://tms.eeda123.com/wx/demo");		
		render("/yh/wx/demo.html");
	}
	//回单上传附件页面
	public void ro_filing() {
		setAttr("type", "default");
		setPageAttr("http://tms.eeda123.com/wx/ro_filing");
		render("/yh/returnOrder/returnOrderFiling.html");
	}
	//回单上传附件页面 - 直送
	public void directSend() {
		setAttr("type", "directSend");
		setPageAttr("http://tms.eeda123.com/wx/directSend");
		render("/yh/returnOrder/returnOrderFiling.html");
	}
	//回单上传附件页面 - 配送
	public void distribution() {
		setAttr("type", "distribution");
		setPageAttr("http://tms.eeda123.com/wx/distribution");
		render("/yh/returnOrder/returnOrderFiling.html");
	}
	
	//获取回单数据
	public void getRo() {
		String type = getPara("type");
		String orderNo = getPara("orderNo");
		String transferOrderNo = getPara("transferOrderNo");
		String customerOrderNo = getPara("customerOrderNo");
		String serialNo = getPara("serialNo");
		String sqId = getPara("sqId");
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
			if(serialNo != null && !"".equals(serialNo) && sqId != null && !"".equals(sqId)){
				detailList = TransferOrderItemDetail.dao.find("select toid.delivery_id from transfer_order_item_detail toid left join transfer_order tor on tor.id = toid.order_id where toid.serial_no = ? and tor.sp_id = ? and toid.delivery_id is not null",serialNo,sqId);
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
	
	public void saveFile(){
    	String id = getPara("return_id");
    	List<UploadFile> uploadFiles = getFiles("fileupload");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	ReturnOrder returnOrder = ReturnOrder.dao.findById(id);
    	boolean result = true;
		if(returnOrder != null){
	    	for (int i = 0; i < uploadFiles.size(); i++) {
	    		File file = uploadFiles.get(i).getFile();
	    		String fileName = file.getName();
	    		String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
	    		if("gif".equals(suffix) || "jpeg".equals(suffix) || "png".equals(suffix) || "jpg".equals(suffix)){
        			OrderAttachmentFile orderAttachmentFile = new OrderAttachmentFile();
        			orderAttachmentFile.set("order_id", id).set("order_type", orderAttachmentFile.OTFRT_TYPE_RETURN).set("file_path", uploadFiles.get(0).getFileName()).save();
	    		}else{
	    			result = false;
	    			break;
	    		}
			}
		}
		if(result){
			resultMap.put("result", "true");
			List<OrderAttachmentFile> OrderAttachmentFileList = OrderAttachmentFile.dao.find("select * from order_attachment_file where order_id = '" + id + "';");
	    	resultMap.put("cause", OrderAttachmentFileList);
		}else{
			resultMap.put("result", "false");
	    	resultMap.put("cause", "上传失败，请选择正确的图片文件");
		}
    	renderJson(resultMap);
    }
	
	public void searchPartSp() {
		String input = getPara("input");
		//String userName = currentUser.getPrincipal().toString();
		//UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
		UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where is_main = ?",true);
		Office parentOffice = Office.dao.findFirst("select * from office where id = ?",currentoffice.get("office_id"));
		Long parentID = parentOffice.get("belong_office");
		if(parentID == null || "".equals(parentID)){
			parentID = parentOffice.getLong("id");
		}
		String sql = "";
		if(input!=null&&input!=""){
			sql= "select p.id pid,p.*, c.*,c.id cid from party p left join contact c on c.id = p.contact_id left join office o on o.id = p.office_id where sp_type = 'delivery' and (p.is_stop is null or p.is_stop = 0) and c.abbr like '%"+input+"%'  and (o.id = "+parentID + " or o.belong_office = "+parentID + ")";
		}else{
			sql= "select p.id pid,p.*, c.*,c.id cid from party p left join contact c on c.id = p.contact_id left join office o on o.id = p.office_id where sp_type = 'delivery' and (p.is_stop is null or p.is_stop = 0) and (o.id = "+parentID + " or o.belong_office = "+parentID + ")";
		}
		List<Record> locationList = Db.find(sql);
		renderJson(locationList);
	}
	
	
}
