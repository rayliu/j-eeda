package controllers.yh.app;

import java.util.HashMap;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresAuthentication;

import models.TransferOrder;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
//import org.json.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@RequiresAuthentication
public class AppController extends Controller {
	private Logger logger = Logger.getLogger(AppController.class);
	private static int pageInterval = 20;//每页20行
	
	public void searchOrder(){
		String para = getPara();
		if(para==null)
			para="";
		
		Integer pageIndex = getParaToInt("pageIndex");
		if(pageIndex==null)
		    pageIndex=0;
		String sql = "select tor.*, c.abbr customer_name from transfer_order tor, contact c where tor.customer_id = c.id and tor.order_no like '%"+para.trim()+"%'";
		
		Record totalRec = Db.findFirst("select count(1) total from ("+sql+") A");
		
		List<TransferOrder> list = TransferOrder.dao.find(sql+" limit "+ (pageIndex*pageInterval)+", "+pageInterval);
		

		HashMap transferOrderListMap = new HashMap();
        transferOrderListMap.put("pageIndex", pageIndex);
        transferOrderListMap.put("iTotalRecords", totalRec.getLong("total").intValue());

        transferOrderListMap.put("orderList", list);
        
		renderJson(transferOrderListMap);
	}
}
