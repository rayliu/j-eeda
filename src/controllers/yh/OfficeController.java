package controllers.yh;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.TransferOrder;
import models.UserLogin;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.PermissionConstant;
//@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class OfficeController extends Controller {
    private Logger logger = Logger.getLogger(LoginUserController.class);
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_O_LIST})
    public void index() {
        render("/yh/profile/office/office.html");
    }

    // 链接到添加分公司页面
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_O_CREATE})
    public void editOffice() {
    	
        render("/yh/profile/office/edit.html");
    }

    // 编辑分公司信息
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_O_UPDATE})
    public void edit() {
        String id = getPara();
        if (id != null) {
            Office l = Office.dao.findById(id);
            setAttr("ul", l);
        } else {
            setAttr("ul", new UserLogin());
        }
        
        render("/yh/profile/office/edit.html");

    }

    // 添加分公司
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_O_CREATE, PermissionConstant.PERMSSION_O_UPDATE}, logical=Logical.OR)
    public void saveOffice() {
        /*
         * if (!isAuthenticated()) return;
         */
        String id = getPara("officeId");
        if (id != "") {
            UserLogin user = UserLogin.dao.findById(id);
        }
        Record user = new Record();
        user.set("office_code", getPara("office_code"));
        user.set("office_name", getPara("office_name"));
        user.set("office_person", getPara("office_person"));
        user.set("phone", getPara("phone"));
        user.set("address", getPara("address"));
        user.set("email", getPara("email"));
        user.set("type", getPara("type"));
        user.set("company_intro", getPara("company_intro"));
        if (id != "") {
            logger.debug("update....");
            user.set("id", id);
            Db.update("office", user);
        } else {
            logger.debug("insert....");
            Db.save("office", user);
        }
        
        render("/yh/profile/office/office.html");

    }

    // 删除分公司
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_O_DELETE})
    public void del() {
        /*
         * UserLogin.dao.find("select * from user_login");
         * UserLogin.dao.deleteById(getParaToInt());
         */
        String id = getPara();
        if (id != null) {
        	List<TransferOrder> orders = TransferOrder.dao.find("select * from transfer_order where office_id = "+id);
        	for(TransferOrder order : orders){
        		order.set("office_id", null);
        		order.update();
        	}
            Db.deleteById("office", id);
        }
        
        render("/yh/profile/office/office.html");
    }

    // 列出分公司信息
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_O_LIST})
    public void listOffice() {
        /*
         * Paging
         */
    	String address=getPara("address");
    	String person=getPara("person");
    	String name=getPara("name");
    	String type=getPara("type");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from office";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));
        String list_sql= "select * from office"
 		+ " where office_name  like '%"+name+"%'  and "
 		+ "office_person like '%"+person+"%' "
 		+ "and type  like '%"+type+"%' "
 		+ "and address  like '%"+address+"%'";
        // 获取当前页的数据
        List<Record> orders = null;
        if(type==null&&name==null&&address==null&&person==null){
        	orders = Db.find("select * from office");
        }else{
        	 orders = Db.find(list_sql);
        }
       
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

}
