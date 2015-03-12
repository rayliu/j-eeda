package controllers.yh;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.Toll;
import models.UserOffice;
import models.yh.profile.Carinfo;

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

import controllers.yh.util.PermissionConstant;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PayController extends Controller {
    private Logger logger = Logger.getLogger(PayController.class);
    Subject currentUser = SecurityUtils.getSubject();
    String userName = currentUser.getPrincipal().toString();
    UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
    Office parentOffice = Office.dao.findFirst("select * from office where id = ?",currentoffice.get("office_id"));
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PAY_LIST})
    public void index() {
        /**
         * String page=getPara("page"); //System.out.print(page);
         * 
         * if(page.equals("收费")){ //System.out.print("没获取参数page");
         * setAttr("page", "收款条目定义"); } if(page.equals("付款")){
         * //System.out.print("没获取参数page"); setAttr("page", "付款条目定义"); }
         **/
        
        	
	     render("/yh/profile/toll/PayList.html");
       
    }
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_PAY_LIST})
    public void ownCarPayIndex(){
    	render("/yh/profile/toll/ownCarPayList.html");
    }

    /**
     *  付费条目
     */
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PAY_LIST})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }
        
        Long parentID = parentOffice.get("belong_office");
        if(parentID == null || "".equals(parentID)){
        	parentID = parentOffice.getLong("id");
        }
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from fin_item  where type ='应付' and office_id = " +parentID;
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select * from fin_item  where type ='应付' and office_id = " + parentID + sLimit);
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);

    }

    // 编辑条目按钮
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PAY_CREATE, PermissionConstant.PERMSSION_PAY_UPDATE}, logical=Logical.OR)
    public void Edit() {
        String id = getPara();
        if (id != null) {
            Toll h = Toll.dao.findById(id);
            setAttr("to", h);
            render("/yh/profile/toll/PayEdit.html");
        } else {
            render("/yh/profile/toll/PayEdit.html");
        }
    }

    // 删除条目
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PAY_DELETE})
    public void delete() {
        String id = getPara();
        if (id != null) {
            Toll l = Toll.dao.findById(id);
            Object obj = l.get("is_stop");
            if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
            	l.set("is_stop", true);
            }else{
            	l.set("is_stop", false);
            }
            l.update();
        }
        redirect("/pay");
    }

    // 添加编辑保存
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PAY_CREATE, PermissionConstant.PERMSSION_PAY_UPDATE}, logical=Logical.OR)
    public void SaveEdit() {

        String id = getPara("id");

        String name = getPara("name");
        String type = "应付";
        String code = getPara("code");
        String remark = getPara("remark");
        
        Long parentID = parentOffice.get("belong_office");
        if(parentID == null || "".equals(parentID)){
        	parentID = parentOffice.getLong("id");
        }
        
        if (id == "") {
            Toll r = new Toll();

            boolean s = r.set("name", name).set("code", code).set("type", type).set("office_id", parentID)
                    .set("Remark", remark).save();
            if (s == true) {
                render("/yh/profile/toll/PayList.html");
                // render("profile/toll/TollList.html");
            }
        } else {
            Toll toll = Toll.dao.findById(id);
            boolean b = toll.set("name", name).set("type", type)
                    .set("code", code).set("Remark", remark).update();
            render("/yh/profile/toll/PayList.html");
        }

    }
    
    /**
     *  自营车辆付费条目
     */
    public void ownCarList() {
    	String sLimit = "";
    	String pageIndex = getPara("sEcho");
    	if (getPara("iDisplayStart") != null
    			&& getPara("iDisplayLength") != null) {
    		sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
    				+ getPara("iDisplayLength");
    	}
    	
    	// 获取总条数
    	String totalWhere = "";
    	String sql = "select count(1) total from fin_item  where type ='应付' and driver_type = '"+Carinfo.CARINFO_TYPE_OWN+"'";
    	Record rec = Db.findFirst(sql + totalWhere);
    	logger.debug("total records:" + rec.getLong("total"));
    	
    	// 获取当前页的数据
    	List<Record> orders = Db
    			.find("select * from fin_item  where type ='应付' and driver_type = '"+Carinfo.CARINFO_TYPE_OWN+"' " + sLimit);
    	Map orderMap = new HashMap();
    	orderMap.put("sEcho", pageIndex);
    	orderMap.put("iTotalRecords", rec.getLong("total"));
    	orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
    	
    	orderMap.put("aaData", orders);
    	
    	renderJson(orderMap);
    	
    }
    
    // 编辑条目按钮
    public void ownCarEdit() {
    	String id = getPara();
    	if (id != null) {
    		Toll h = Toll.dao.findById(id);
    		setAttr("to", h);
    	}
		render("/yh/profile/toll/ownCarPayEdit.html");
    }
    
    // 删除条目
    public void ownCardelete() {
    	String id = getPara();
    	if (id != null) {
    		Toll l = Toll.dao.findById(id);
    		l.delete();
    	}
		redirect("/ownCarPay");
    }
    
    // 添加编辑保存
    public void ownCarSave() {   
    	Toll toll = null;
    	String id = getPara("id");    	
    	String name = getPara("name");
    	String type = "应付";
    	String code = getPara("code");
    	String remark = getPara("remark");    	
    	if (id != null && !"".equals(id)) {
    		toll = Toll.dao.findById(id);
    		toll.set("name", name).set("type", type).set("code", code).set("remark", remark).update();
    	} else {
    		toll = new Toll();    		
    		toll.set("name", name).set("code", code).set("type", type).set("driver_type", Carinfo.CARINFO_TYPE_OWN).set("remark", remark).save();
    	} 
    	
		render("/yh/profile/toll/ownCarPayList.html");
    }
}
