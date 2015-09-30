package controllers.yh.app;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
//import org.json.JSONObject;

public class AppController extends Controller {
	private Logger logger = Logger.getLogger(AppController.class);
	
	public void searchOrder(){
		String para = getPara();
		logger.debug("para:"+para);
		renderJson(para);
	}
}
