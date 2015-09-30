package controllers.yh.app;

import java.util.List;

import models.TransferOrder;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
//import org.json.JSONObject;

public class AppController extends Controller {
	private Logger logger = Logger.getLogger(AppController.class);
	
	public void searchOrder(){
		String para = getPara();
		if(para==null)
			para="";
		logger.debug("para:"+para);
		String sql = "select * from transfer_order where order_no like '%"+para.trim()+"%'";
		List<TransferOrder> list = TransferOrder.dao.find(sql);
		renderJson(list);
	}
}
