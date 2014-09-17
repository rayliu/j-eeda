package controllers.yh;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.Toll;
import models.yh.profile.Carinfo;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class PayController extends Controller {
    private Logger logger = Logger.getLogger(PayController.class);

    public void index() {
        /**
         * String page=getPara("page"); //System.out.print(page);
         * 
         * if(page.equals("收费")){ //System.out.print("没获取参数page");
         * setAttr("page", "收款条目定义"); } if(page.equals("付款")){
         * //System.out.print("没获取参数page"); setAttr("page", "付款条目定义"); }
         **/
        HttpServletRequest re = getRequest();
        String url = re.getRequestURI();logger.debug("URI:" + url);
        if (url.equals("/yh/pay")) {
	        if (LoginUserController.isAuthenticated(this))
	            render("profile/toll/PayList.html");
        }
        if (url.equals("/yh/ownCarPay")) {
        	if (LoginUserController.isAuthenticated(this))
        		render("profile/toll/ownCarPayList.html");
        }
    }

    /**
     *  付费条目
     */
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
        String sql = "select count(1) total from fin_item  where type ='应付'";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select * from fin_item  where type ='应付'" + sLimit);
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);

    }

    // 编辑条目按钮
    public void Edit() {
        String id = getPara();
        if (id != null) {
            Toll h = Toll.dao.findById(id);
            setAttr("to", h);
            if (LoginUserController.isAuthenticated(this))
                render("profile/toll/PayEdit.html");
        } else {
            if (LoginUserController.isAuthenticated(this))
                render("profile/toll/PayEdit.html");
        }
    }

    // 删除条目
    public void delete() {
        String id = getPara();
        if (id != null) {
            Toll l = Toll.dao.findById(id);
            l.delete();
        }
        if (LoginUserController.isAuthenticated(this))
            redirect("/yh/pay");
    }

    // 添加编辑保存
    public void SaveEdit() {

        String id = getPara("id");

        String name = getPara("name");
        String type = "应付";
        String code = getPara("code");
        String remark = getPara("remark");

        if (id == "") {
            Toll r = new Toll();

            boolean s = r.set("name", name).set("code", code).set("type", type)
                    .set("Remark", remark).save();
            if (s == true) {
                render("profile/toll/PayList.html");
                // render("profile/toll/TollList.html");
            }
        } else {
            Toll toll = Toll.dao.findById(id);
            boolean b = toll.set("name", name).set("type", type)
                    .set("code", code).set("Remark", remark).update();
            if (LoginUserController.isAuthenticated(this))
                render("profile/toll/PayList.html");
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
		if (LoginUserController.isAuthenticated(this))
			render("profile/toll/ownCarPayEdit.html");
    }
    
    // 删除条目
    public void ownCardelete() {
    	String id = getPara();
    	if (id != null) {
    		Toll l = Toll.dao.findById(id);
    		l.delete();
    	}
    	if (LoginUserController.isAuthenticated(this))
    		redirect("/yh/ownCarPay");
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
    	if (LoginUserController.isAuthenticated(this))
    		render("profile/toll/ownCarPayList.html");
    }
}
