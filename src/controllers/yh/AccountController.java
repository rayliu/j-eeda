package controllers.yh;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.h2.command.dml.Select;

import models.Account;
import models.Office;
import models.UserLogin;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class AccountController extends Controller {
    private Logger logger = Logger.getLogger(LoginUserController.class);
    public  void  index(){
    	if(LoginUserController.isAuthenticated(this))
        render("/yh/profile/account/account.html");
    }
    //链接到添加金融账户页面
    public void editAccount(){
    	if(LoginUserController.isAuthenticated(this))
        render("/yh/profile/account/edit.html");
    }
  //编辑金融账户信息
    public void edit(){
        String id = getPara();
        if (id != null) {
            Account l = Account.dao.findById(id);
            setAttr("ul", l);
        }
        if(LoginUserController.isAuthenticated(this))
        render("/yh/profile/account/edit.html");
        
    }
    //添加金融账户
    public void saveAccount(){
        /*if (!isAuthenticated())
            return;
*/
        String id = getPara("accountId");
        if (id != "") {
            List<Record> list = Db.find("select * from fin_account");
        }
        Record user =new Record();
        user.set("name", getPara("name"));
        user.set("type", getPara("type"));
        user.set("currency", getPara("currency"));
        user.set("org_name", getPara("org_name"));
        user.set("account_pin", getPara("account_pin"));
        user.set("remark", getPara("remark"));
        if (id != "") {
            logger.debug("update....");
            user.set("id", id);
            Db.update("fin_account",user);
        } else {
            logger.debug("insert....");
            Db.save("fin_account", user);
        }
        if(LoginUserController.isAuthenticated(this))
        render("/yh/profile/account/account.html");
        
    }
    //删除金融账户
    public void del(){
        String id = getPara();
        if (id != null) {
        Db.deleteById("fin_account", id);
        }
        if(LoginUserController.isAuthenticated(this))
        render("/yh/profile/account/account.html");
    }
  //列出金融账户信息
    public void listAccount(){
        /*
         * Paging
         */
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        //获取总条数
        String totalWhere="";
        String sql = "select count(1) total from fin_account ";
        System.out.println(sql);
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));
        
        //获取当前页的数据
        List<Record> orders = Db.find("select * from fin_account");
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        
        renderJson(orderMap);
    }

}
