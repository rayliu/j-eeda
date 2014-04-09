package controllers.yh;

import java.util.List;

import models.UserLogin;

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
            UserLogin l = UserLogin.dao.findById(id);
            setAttr("ul", l);
        } else {
            setAttr("ul", new UserLogin());
        }
        render("/yh/profile/loginUser/addUser.html");

    }

    // 添加干线
    public void saveRoute() {
        /*
         * if (!isAuthenticated()) return;
         */
        String id = getPara("userId");
        if (id != "") {
            UserLogin user = UserLogin.dao.findById(id);
        }
        Record user = new Record();
        user.set("user_name", getPara("username"));
        user.set("password", getPara("password"));
        user.set("password_hint", getPara("pw_hint"));
        if (id != "") {
            logger.debug("update....");
            user.set("id", id);
            Db.update("user_login", user);
        } else {
            logger.debug("insert....");
            Db.save("user_login", user);
        }
        render("/yh/profile/loginUser/loginUser.html");

    }

    // 删除干线
    public void del() {
        /*
         * UserLogin.dao.find("select * from user_login");
         * UserLogin.dao.deleteById(getParaToInt());
         */
        String id = getPara();
        if (id != null) {
            UserLogin l = UserLogin.dao.findById(id);
            l.delete();
        }
        render("/yh/profile/loginUser/loginUser.html");
    }

    // input控件列出城市列表
    public void search() {
        String locationName = getPara("locationName");

        // 不能查所有
        if (locationName.trim().length() > 0) {
            List<Record> locationList = Db.find("select * from location where name like '%" + locationName + "%'");
            renderJson(locationList);
        }
    }
}
