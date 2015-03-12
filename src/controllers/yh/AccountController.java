package controllers.yh;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.Office;
import models.UserOffice;
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

import controllers.yh.util.PermissionConstant;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AccountController extends Controller {
    private Logger logger = Logger.getLogger(LoginUserController.class);
    Subject currentUser = SecurityUtils.getSubject();
    String userName = currentUser.getPrincipal().toString();
	UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
	Office parentOffice = Office.dao.findFirst("select * from office where id = ?",currentoffice.get("office_id"));
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_LIST})
    public void index() {
        render("/yh/profile/account/account.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_CREATE})
    // 链接到添加金融账户页面
    public void editAccount() {
        render("/yh/profile/account/edit.html");
    }

    // 编辑金融账户信息
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_UPDATE})
    public void edit() {
        String id = getPara();
        if (id != null) {
            Account l = Account.dao.findById(id);
            setAttr("ul", l);
        }
        render("/yh/profile/account/edit.html");

    }

    // 添加金融账户
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_CREATE, PermissionConstant.PERMSSION_A_UPDATE}, logical=Logical.OR)
    public void save() {
    	Long parentID = parentOffice.get("belong_office");
    	if(parentID == null || "".equals(parentID)){
    		parentID = parentOffice.getLong("id");
    	}
        /*
         * if (!isAuthenticated()) return;
         */
        String id = getPara("accountId");
        if (id != "") {
            List<Record> list = Db.find("select * from fin_account");
        }
        Record account = new Record();
        account.set("bank_name", getPara("bank_name"));
        account.set("type", getPara("type"));
        account.set("account_no", getPara("account_no"));
        account.set("bank_person", getPara("bank_person"));
        account.set("currency", getPara("currency"));
        
        account.set("remark", getPara("remark"));
        if (id != "") {
            logger.debug("update....");
            account.set("id", id);
            Db.update("fin_account", account);
        } else {
            logger.debug("insert....");
            account.set("office_id", currentoffice.get("office_id"));
            Db.save("fin_account", account);
        }
        renderJson(account);
    }

    // 删除金融账户
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_DELETE})
    public void del() {
        String id = getPara();
        if (id != null) {
            /*Db.deleteById("fin_account", id);*/
        	Account account = Account.dao.findById(id);
        	 Object obj = account.get("is_stop");
             if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
            	 account.set("is_stop", true);
             }else{
            	 account.set("is_stop", false);
             }
             account.update();
        }
        render("/yh/profile/account/account.html");
    }

    // 列出金融账户信息
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_LIST})
    public void listAccount() {
        /*
         * Paging
         */
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }
        
       
        // 获取总条数
        String totalWhere = "";
        String sql = "";
        String querySql ="";
        Long parentID = parentOffice.get("belong_office");
        if(parentID == null || "".equals(parentID)){
        	sql = "select count(1) total from fin_account f left join office o on f.office_id = o.id where o.id = "+parentOffice.get("id") +" or o.belong_office = "+parentOffice.get("id") ;
        	querySql= "select * from fin_account f left join office o on f.office_id = o.id where o.id = "+parentOffice.get("id") +" or o.belong_office = "+parentOffice.get("id") ;
        }else{
        	sql = "select count(1) total from fin_account where office_id = "+parentOffice.get("id");
        	querySql= "select * from fin_account where office_id = "+parentOffice.get("id");
        }
        
        
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find(querySql + sLimit);
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    public void accountItem() {

        String id = getPara("accountId");
        System.out.println(id);
        if (id.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        String sql = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        if (id != null && id.length() > 0) {
            sql = "select count(1) total from fin_account where id = " + id;
        }
        System.out.println(sql);
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = null;
        if (id != null && id.length() > 0) {
            orders = Db.find("select * from fin_account where id = " + id + " " + sLimit);
        }
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // accountItem编辑
    /*public void eidtAcountItem() {
        String id = getPara();

        String accountId = getPara("accountId");
        System.out.println(id);
        // Route route = Route.dao.findById(id);
        List<Record> list = Db
                .find("select * from fin_account_item where id = '" + id
                        + "' and account_id = '" + accountId + "'");
        renderJson(list);
    }*/

    // 删除accountItem
    public void delectAcountItem() {
        String id = getPara();
        if (id != null) {
            Db.deleteById("fin_account_item", id);
        }
        renderJson("{\"success\":true}");
    }

    // 添加accountItem
    public void saveAccountItemBtn() {
        AccountItem fin = new AccountItem();
        String accountItem = getPara("accountItemId");
        fin.set("ACCOUNT_ID", getPara("accountId2"))
                .set("CURRENCY", getPara("currency"))
                .set("ORG_NAME", getPara("org_name"))
                .set("ACCOUNT_PIN", getPara("account_pin"))
                .set("ORG_PERSON", getPara("account_person"));
        if (accountItem == null || accountItem.equals("")) {
            fin.save();
            renderJson("{\"success\":true}");
        } else {
            fin.set("id", accountItem);
            fin.update();
            renderJson("{\"success\":true}");
        }
    }
}
