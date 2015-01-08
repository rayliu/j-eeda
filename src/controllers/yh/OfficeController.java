package controllers.yh;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.Office;
import models.TransferOrder;
import models.UserLogin;

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
public class OfficeController extends Controller {
    private Logger logger = Logger.getLogger(LoginUserController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_O_LIST})
    public void index() {
        render("/yh/profile/office/office.html");
    }

    // 链接到添加分公司页面
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_O_CREATE})
    public void editOffice() {
    	
        render("/yh/profile/office/edit.html");
    }

    // 编辑分公司信息
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_O_UPDATE})
    public void edit() {
        String id = getPara();
        Office office = Office.dao.findById(id);
        setAttr("ul", office);
        logger.debug("abbr:"+office.getStr("abbr"));
        if(office.get("location") != null && !"".equals(office.get("location"))){
	        String code = office.get("location");
	
	        List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
	        Location l = Location.dao.findFirst("select * from location where code = (select pcode from location where code = '"+code+"')");
	        Location location = null;
	        if(provinces.contains(l)){
	        	location = Location.dao
		                .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
		                        + code + "'");
	        }else{
	        	location = Location.dao
		                .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
		                        + code + "'");
	        }
	        setAttr("location", location);
		}
        render("/yh/profile/office/edit.html");
    }

    // 添加分公司
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_O_CREATE, PermissionConstant.PERMSSION_O_UPDATE}, logical=Logical.OR)
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
        user.set("location", getPara("location"));
        user.set("abbr", getPara("abbr"));
        if (id != "") {
            logger.debug("update....");
            user.set("id", id);
            Db.update("office", user);
        } else {
            logger.debug("insert....");
            //记录总公司
			String name = (String) currentUser.getPrincipal();
	 		List<UserLogin> users = UserLogin.dao
	 				.find("select * from user_login where user_name='" + name + "'");
	 		//创建用户是总公司用户
	 		user.set("belong_office", users.get(0).get("office_id"));
	 		//创建用户是网点用户
	 		//Record rec = Db.findFirst("select belong_office  from office  where id = " + users.get(0).get("office_id"));
	 		//user.set("office_id", rec.get("belong_office"));
            
            Db.save("office", user);
        }
        
        render("/yh/profile/office/office.html");

    }

    // 删除分公司
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_O_DELETE})
    public void del() {
        /*
         * UserLogin.dao.find("select * from user_login");
         * UserLogin.dao.deleteById(getParaToInt());
         */
        String id = getPara();
        if (id != null) {
        	/*List<TransferOrder> orders = TransferOrder.dao.find("select * from transfer_order where office_id = "+id);
        	for(TransferOrder order : orders){
        		order.set("office_id", null);
        		order.update();
        	}
            Db.deleteById("office", id);*/
        	Office office = Office.dao.findById(id);
        	Object obj = office.get("is_stop");
            if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
            	office.set("is_stop", true);
            }else{
            	office.set("is_stop", false);
            }
            office.update();
        	
        }
        
        render("/yh/profile/office/office.html");
    }

    // 列出分公司信息
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_O_LIST})
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
 		+ "and address  like '%"+address+"%' " + sLimit;
        // 获取当前页的数据
        List<Record> orders = null;
        if(type==null&&name==null&&address==null&&person==null){
        	orders = Db.find("select o.*,lc.name dname from office o left join location lc on o.location = lc.code " + sLimit);
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
    
    //查询分公司所有仓库
    public void findOfficeWarehouse(){
    	String office_id = getPara();// 调车单id
    	String sLimit = "";
    	String pageIndex = getPara("sEcho");
    	if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    		sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    	}
        String sqlTotal = "";
        Record rec = null;
        String sql = "";
    	if(office_id != ""){
	        sqlTotal = "select count(0) total from warehouse where office_id ="+office_id+";";
	        logger.debug("sql :" + sqlTotal);
	        rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        //String sql = "select * from warehouse where office_id ="+office_id+";";
	        sql = "select w.*,lc.name dname from warehouse w"
			+ " left join location lc on w.location = lc.code "
			+ " where w.office_id = "+office_id+" order by w.id desc " + sLimit;
    	}
    	List<Record> warehouseList = Db.find(sql);
    	Map Map = new HashMap();
    	Map.put("sEcho", pageIndex);
    	Map.put("iTotalRecords", rec.getLong("total"));
    	Map.put("iTotalDisplayRecords", rec.getLong("total"));
    	Map.put("aaData", warehouseList);
    	renderJson(Map); 
    }
    public void checkOfficeNameExist(){
    	boolean result = true;
    	String officeName= getPara("office_name");
    	String[] str =officeName.split(",");
    	Office office= Office.dao.findFirst("select * from office where office_name = '" + str[0] + "'");
    	if(office != null){
    		if(str.length == 1){
    			result = false;
    		}else{
    			logger.debug("1:"+str[0]+","+str[1]);
    			if(!str[0].equals(str[1])){
    				result = false;
    			}
    		}
    	}
    	renderJson(result);
    }
    

}
