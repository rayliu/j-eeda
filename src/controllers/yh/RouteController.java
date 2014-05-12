package controllers.yh;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.yh.profile.Route;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class RouteController extends Controller {
    private Logger logger = Logger.getLogger(LoginUserController.class);

    public void index() {
        render("/yh/profile/route/route.html");
    }

    // 链接到添加干线页面
    public void editRoute() {
        render("/yh/profile/route/edit.html");
    }

    // 编辑干线
    public void edit() {
        String id = getPara();
        if (id != null) {
            Route l = Route.dao.findById(id);
            setAttr("ul", l);
        }
        render("/yh/profile/route/edit.html");

    }

    // 添加干线
    public void saveRoute() {
        /*
         * if (!isAuthenticated()) return;
         */
        String id = getPara("routeId");
        if (id != "") {
            Route user = Route.dao.findById(id);
        }
        Record user = new Record();
        user.set("from_id", getPara("from_id"));
        user.set("to_id", getPara("to_id"));
        user.set("location_from", getPara("fromName"));
        user.set("location_to", getPara("toName"));
        user.set("remark", getPara("remark"));
        if (id != "") {
            logger.debug("update....");
            user.set("id", id);
            Db.update("route", user);
        } else {
            logger.debug("insert....");
            Db.save("route", user);
        }
        render("/yh/profile/route/route.html");

    }

    // 删除干线
    public void del() {
        /*
         * UserLogin.dao.find("select * from user_login");
         * UserLogin.dao.deleteById(getParaToInt());
         */
        String id = getPara();
        if (id != null) {
            Db.deleteById("route", id);
        }
        render("/yh/profile/route/route.html");
    }

    // input控件列出城市列表
    public void search() {
        String locationName = getPara("locationName");

        // 不能查所有
        if (locationName.trim().length() > 0) {
            List<Record> locationList = Db
                    .find("select * from location where name like '%"
                            + locationName + "%' or code like '%"
                            + locationName + "%' or pcode like '%"
                            + locationName + "%'");
            renderJson(locationList);
        }
    }

    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from route ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select * from route");
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }
}
