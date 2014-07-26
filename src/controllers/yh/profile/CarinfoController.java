package controllers.yh.profile;import java.util.HashMap;import java.util.List;import java.util.Map;import javax.servlet.http.HttpServletRequest;import models.yh.profile.Carinfo;import org.apache.shiro.SecurityUtils;import org.apache.shiro.subject.Subject;import com.jfinal.core.Controller;import com.jfinal.log.Logger;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;import controllers.yh.LoginUserController;public class CarinfoController extends Controller {    private Logger logger = Logger.getLogger(CarinfoController.class);    // in config route已经将路径默认设置为/yh    // me.add("/yh", controllers.yh.AppController.class, "/yh");    static Subject currentUser = SecurityUtils.getSubject();    public static boolean isAuthenticated(Controller controller) {        currentUser = SecurityUtils.getSubject();        if (!currentUser.isAuthenticated()) {            controller.redirect("/yh/login");            return false;        }        controller.setAttr("userId", currentUser.getPrincipal());        return true;    }    public void index() {        HttpServletRequest re = getRequest();        String url = re.getRequestURI();        logger.debug("URI:" + url);        if (url.equals("/yh/carinfo")) {            if (LoginUserController.isAuthenticated(this))                render("/yh/profile/carinfo/carlist.html");        }        if (url.equals("/yh/carmanage")) {            if (LoginUserController.isAuthenticated(this))                render("/yh/carmanage/carmanage.html");        }    }    public void list() {        String sLimit = "";        String pageIndex = getPara("sEcho");        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");        }        // 获取总条数        String totalWhere = "";        String sql = "select count(1) total from carinfo ";        Record rec = Db.findFirst(sql + totalWhere);        logger.debug("total records:" + rec.getLong("total"));        // 获取当前页的数据        List<Record> orders = Db.find("select * from carinfo");        Map orderMap = new HashMap();        orderMap.put("sEcho", pageIndex);        orderMap.put("iTotalRecords", rec.getLong("total"));        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));        orderMap.put("aaData", orders);        renderJson(orderMap);    }    // 发车记录单list    public void carmanageList() {        String sLimit = "";        String pageIndex = getPara("sEcho");        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");        }        // 获取总条数        String totalWhere = "";        String sql = "select count(1) total from depart_order d " + "left join carinfo c on c.id =d.driver_id "                + "where d.combine_type='PICKUP'";        Record rec = Db.findFirst(sql + totalWhere);        logger.debug("total records:" + rec.getLong("total"));        // 获取当前页的数据        List<Record> orders = Db.find("select * from depart_order d " + "left join carinfo c on c.id =d.driver_id "                + "where d.combine_type='PICKUP'");        Map orderMap = new HashMap();        orderMap.put("sEcho", pageIndex);        orderMap.put("iTotalRecords", rec.getLong("total"));        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));        orderMap.put("aaData", orders);        renderJson(orderMap);    }    public void add() {        if (LoginUserController.isAuthenticated(this))            render("/yh/profile/carinfo/edit.html");    }    // 添加车辆    public void save() {        String id = getPara("carId");        Carinfo carinfo = null;        if (id != "") {            carinfo = Carinfo.dao.findById(id);            setCarifo(carinfo);            carinfo.update();        } else {            carinfo = new Carinfo();            setCarifo(carinfo);            carinfo.save();        }        redirect("/yh/carinfo");    }    public void setCarifo(Carinfo carinfo) {        carinfo.set("driver", getPara("driver"));        carinfo.set("cartype", getPara("ctype"));        carinfo.set("car_no", getPara("car_number"));        carinfo.set("phone", getPara("phone"));        carinfo.set("length", getPara("length"));    }    public void delect() {        String id = getPara();        if (id != null) {            Carinfo.dao.deleteById(id);        }        renderJson("{\"success\":true}");    }    public void edit() {        String id = getPara();        Carinfo carinfo = Carinfo.dao.findById(id);        System.out.println(carinfo);        setAttr("lu", carinfo);        render("/yh/profile/carinfo/edit.html");    }}