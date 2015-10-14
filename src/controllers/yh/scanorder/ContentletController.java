package controllers.yh.scanorder;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ParentOfficeModel;
import models.yh.profile.AccountItem;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ContentletController extends Controller {
    private Logger logger = Logger.getLogger(ContentletController.class);
//    Subject currentUser = SecurityUtils.getSubject();
//    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    public void index() {
        render("/bz/gateOutOrder/OrderList.html");
    }
    
    public void list() {
    	String pageIndex = getPara("sEcho");
    	
    	List<Record> results = searchGateOutOrder("");
    	
    	Map orderListMap = new HashMap();
    	orderListMap.put("sEcho", pageIndex);
    	orderListMap.put("iTotalRecords", 10);
    	orderListMap.put("iTotalDisplayRecords", 10);

    	orderListMap.put("aaData", results);
		
		renderJson(orderListMap);
    }

	private List<Record> searchGateOutOrder(String condition) {
		String structureId ="1";
    	
    	String sqlField = "select field_name, field_type, field_contentlet from field where structure_id="+structureId;
    	List<Record> fields = Db.find(sqlField);
    	
    	String contentletFields = "";
    	for (Record record : fields) {
    		contentletFields += ", "+record.getStr("field_contentlet") +" as "+record.getStr("field_name");
    		
    		
    		if(("customer_id").equals(record.getStr("field_name"))){
    			contentletFields += ", (SELECT c.company_name FROM party pa left outer join contact c "
    					+ " on pa.contact_id = c.id where pa.party_type='CUSTOMER' and pa.id=cl."+record.getStr("field_contentlet")
    					+ " ) as customer_company_name ";
    		}
		} 
    	
    	String sql = "select id"+contentletFields+" from contentlet cl where structure_id = "+structureId + condition;
    	
    	List<Record> results = Db.find(sql);
		return results;
	}
	
	private Record getGateOutOrderById(String orderId) {
    	List<Record> results = searchGateOutOrder(" and id="+orderId);
    	if(!results.isEmpty() && results.size() == 1)
    		return results.get(0);
		return null;
	}
    
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_A_UPDATE})
    public void edit() {
        String orderId = getPara();
        setAttr("order", getGateOutOrderById(orderId));
        render("/bz/gateOutOrder/updateOrder.html");
    }

}
