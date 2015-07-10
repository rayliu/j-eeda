package controllers.yh;import interceptor.SetAttrLoginUserInterceptor;import java.util.HashMap;import java.util.List;import java.util.Map;import org.apache.shiro.authz.annotation.RequiresAuthentication;import com.jfinal.aop.Before;import com.jfinal.core.Controller;import com.jfinal.log.Logger;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;@RequiresAuthentication@Before(SetAttrLoginUserInterceptor.class)public class LocationController extends Controller {    private Logger logger = Logger.getLogger(LocationController.class);    // in config route已经将路径默认设置为/yh    // me.add("/yh", controllers.yh.AppController.class, "/yh");    public void index() {        render("/yh/profile/location.html");    }    // 列出城市信息    public void listLocation() {        String sLimit = "";        String sSearch = getPara("sSearch");        String pageIndex = getPara("sEcho");        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");        }        // 获取总条数        String totalWhere = "";        String sql = "select count(1) total from location ";                // 获取当前页的数据        List<Record> orders = null;        String sql1 = "select concat(t.name, ' ',t1.name,' ',t3.name) as name,t.name as province,t1.name as city,t3.name as district from location t left join location t1 on t.code=t1.pcode inner join location t3 on t3.pcode=t1.code";        if(sSearch==null ||sSearch.equals("")){        	orders = Db.find(sql1+ sLimit);        }else{        	sql = "select count(*) as total from location t left join location t1 on t.code=t1.pcode inner join location t3 on t3.pcode=t1.code"+" where t.name like '%"+sSearch+"%' or t1.name like '%"+sSearch+"%' or t3.name like '%"+sSearch+"%' ";        	orders = Db.find(sql1+ " where t.name like '%"+sSearch+"%' or t1.name like '%"+sSearch+"%' or t3.name like '%"+sSearch+"%' "+ sLimit);        }                Record rec = Db.findFirst(sql + totalWhere);        logger.debug("total records:" + rec.getLong("total"));        // List<Record> orders =        // Db.find("select l.*,l2.name as pname from `location` l left join location l2 on l2.code =l.code and l.code like '__' order by l.id"+        // sLimit);        Map orderMap = new HashMap();        orderMap.put("sEcho", pageIndex);        orderMap.put("iTotalRecords", rec.getLong("total"));        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));        orderMap.put("aaData", orders);        renderJson(orderMap);    }}