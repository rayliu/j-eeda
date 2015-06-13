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
        render("/yh/scanorder/OrderList.html");
    }
    
    public void list() {
    	String pageIndex = getPara("sEcho");
    	
    	String sqlField = "select field_name, field_type, field_contentlet from field where structure_id=1";
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
    	
    	String sql = "select COUNT(*) as seq"+contentletFields+" from contentlet cl where structure_id=1";
    	
    	List<Record> results = Db.find(sql);

    	
    	Map orderListMap = new HashMap();
    	orderListMap.put("sEcho", pageIndex);
    	orderListMap.put("iTotalRecords", 10);
    	orderListMap.put("iTotalDisplayRecords", 10);

    	orderListMap.put("aaData", results);
		
		renderJson(orderListMap);
    }
    
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_A_CREATE})
    // 链接到添加金融账户页面
    public void editAccount() {
        render("/yh/scanorder/updateOrder.html");
    }

    // 编辑金融账户信息
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_A_UPDATE})
    public void edit() {
        String id = getPara();
        if (id != null) {
            Account l = Account.dao.findById(id);
            setAttr("ul", l);
        }
        render("/yh/scanorder/updateOrder.html");

    }

}
