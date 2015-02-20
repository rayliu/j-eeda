package controllers.yh.wx;

import com.jfinal.core.Controller;

public class WxController extends Controller {

	//微信JS demo页面，方便参考
	public void demo() {
		render("/yh/wx/demo.html");
	}
	//回单上传附件
	public void ro_filing() {
		render("/yh/returnOrder/returnOrderFiling.html");
	}
	
}
