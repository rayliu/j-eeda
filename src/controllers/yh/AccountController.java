package controllers.yh;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.yh.profile.AccountItem;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class AccountController extends Controller {
    private Logger logger = Logger.getLogger(LoginUserController.class);

    public void index() {
        if (LoginUserController.isAuthenticated(this))
            render("/yh/profile/account/account.html");
    }

    // 链接到添加金融账户页面
    public void editAccount() {
        if (LoginUserController.isAuthenticated(this))
            render("/yh/profile/account/edit.html");
    }

    // 编辑金融账户信息
    public void edit() {
        String id = getPara();
        if (id != null) {
            Account l = Account.dao.findById(id);
            setAttr("ul", l);
        }
        if (LoginUserController.isAuthenticated(this))
            render("/yh/profile/account/edit.html");

    }

    // 添加金融账户
    public void save() {
        /*
         * if (!isAuthenticated()) return;
         */
        String id = getPara("accountId");
        if (id != "") {
            List<Record> list = Db.find("select * from fin_account");
        }
        Record account = new Record();
        account.set("name", getPara("name"));
        account.set("type", getPara("type"));
        account.set("remark", getPara("remark"));
        if (id != "") {
            logger.debug("update....");
            account.set("id", id);
            Db.update("fin_account", account);
        } else {
            logger.debug("insert....");
            Db.save("fin_account", account);
        }
        renderJson(account.get("id"));
    }

    // 删除金融账户
    public void del() {
        String id = getPara();
        if (id != null) {
            Db.deleteById("fin_account", id);
        }
        if (LoginUserController.isAuthenticated(this))
            render("/yh/profile/account/account.html");
    }

    // 列出金融账户信息
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
        String sql = "select count(1) total from fin_account";
        System.out.println(sql);
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select * from fin_account" + sLimit);
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
            sql = "select count(1) total from fin_account f,fin_account_item f1 where f.id = f1.account_id and f.id = '"
                    + id + "'";
        }
        System.out.println(sql);
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = null;
        if (id != null && id.length() > 0) {
            orders = Db
                    .find("select *,f1.id as fid from fin_account f,fin_account_item f1 where f.id = f1.account_id and f.id = '"
                            + id + "'" + sLimit);
        }
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // accountItem编辑
    public void eidtAcountItem() {
        String id = getPara();

        String accountId = getPara("accountId");
        System.out.println(id);
        // Route route = Route.dao.findById(id);
        List<Record> list = Db
                .find("select * from fin_account_item where id = '" + id
                        + "' and account_id = '" + accountId + "'");
        renderJson(list);
    }

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
