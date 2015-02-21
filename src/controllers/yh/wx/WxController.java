package controllers.yh.wx;

import java.util.Map;

import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.weixin.demo.SignKit;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.JsTicketApi;

public class WxController extends Controller {
	//微信JS demo页面，方便参考
	public void demo() {
		String jsapi_ticket = JsTicketApi.getJsTicket().getJsTicket();
	
		Map<String, String> m = SignKit.sign(jsapi_ticket, "http://56.eeda123.com/wx/demo");//这里需要动态处理
		setAttr("timestamp", m.get("timestamp"));
		setAttr("nonceStr", m.get("nonceStr"));
		setAttr("signature", m.get("signature"));
		
		render("/yh/wx/demo.html");
	}
	//回单上传附件
	public void ro_filing() {
		render("/yh/returnOrder/returnOrderFiling.html");
	}
	
	
}
