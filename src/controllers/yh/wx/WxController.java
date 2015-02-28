package controllers.yh.wx;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.OrderAttachmentFile;
import models.ReturnOrder;

import com.jfinal.kit.PropKit;
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
		setPageAttr("http://tms.eeda123.com/wx/ro_filing");
		render("/yh/returnOrder/returnOrderFiling.html");
	}
	//获取回单数据
	public void getRo() {
		String orderNo=getPara();
		if(orderNo!=null)
			orderNo=orderNo.toUpperCase();
		ReturnOrder returnOrder = ReturnOrder.dao.findFirst("select * from return_order where order_no=?",orderNo);
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
	
	
	
	
}
