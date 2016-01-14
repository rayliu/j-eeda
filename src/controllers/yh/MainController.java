package controllers.yh;

import interceptor.SetAttrLoginUserInterceptor;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.Office;
import models.ParentOfficeModel;
import models.UserLogin;
import models.UserOffice;
import models.yh.profile.OfficeCofig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.plugin.shiro.ShiroKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.eeda.ModuleController;
import controllers.yh.util.EedaCommonHandler;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.getCurrentPermission;

public class MainController extends Controller {
	private Logger logger = Logger.getLogger(MainController.class);
    // in config route已经将路径默认设置为/yh
    // me.add("/yh", controllers.yh.AppController.class, "/yh");
    Subject currentUser = SecurityUtils.getSubject();

    private boolean isAuthenticated() {

        // remember me 处理，自动帮user 登陆
        if (!currentUser.isAuthenticated() && currentUser.isRemembered()) {
            Object principal = currentUser.getPrincipal();
            if (null != principal) {
                UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='" + String.valueOf(principal) + "' and (is_stop = 0 or is_stop is null)");
                if(user==null){//这里是预防user使用了remember me, 但是user ID在表中已删除
                	redirect("/login");
                	return false;
                }
                String password = user.getStr("password");
                UsernamePasswordToken token = new UsernamePasswordToken(user.getStr("user_name"), password);
                token.setRememberMe(true);
                currentUser.login(token);// 登录
            }
        }

        if (!currentUser.isAuthenticated()) {
            redirect("/login");
            return false;
        }
        setAttr("userId", currentUser.getPrincipal());
        // timeout:-1000ms 这样设置才能永不超时 
    	currentUser.getSession().setTimeout(-1000L);
    	
        return true;
    }

    public void index() {
    	setSysTitle();
        if (isAuthenticated()) {
        	UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=?", currentUser.getPrincipal());
        	
            if(user.get("c_name")!=null&&!"".equals(user.get("c_name"))){
            	setAttr("userId", user.get("c_name"));
            }else{
            	setAttr("userId", currentUser.getPrincipal());
            }
            
            setAttr("user_login_id", currentUser.getPrincipal());
            setAttr("login_time",user.get("last_login"));
            setAttr("lastIndex",user.get("last_index") == null ? "pastOneDay" : user.get("last_index"));
            
            //查询两个月内即将过期的客户合同
            String sql ="select c.id, c.name, c.period_to from user_customer uc"
					+" left join contract c on c.party_id= uc.customer_id  "
					+" LEFT JOIN party p ON c.party_id = p.id and p.party_type = 'CUSTOMER'"
				    +" where uc.user_name='"+currentUser.getPrincipal()+"'"
					+" and c.period_to > SYSDATE()"
					+" and c.period_to < DATE_ADD(SYSDATE(), INTERVAL 60 DAY);  ";
            List<Record> contractList = Db.find(sql);
            setAttr("contractList", contractList);
            //更新当前用户最后一次登陆的时间
            updateLastLogin(user);
            
            //查询当前用户权限，并且将其设置到会话当中
            setPermissionToSession();	

            String savedRequestUrl = this.getSessionAttr(ShiroKit.getSavedRequestKey());
            if(savedRequestUrl!=null){
            	setSessionAttr(ShiroKit.getSavedRequestKey(), null);
            	redirect(savedRequestUrl);
            }else{
            	String officeConfig="select oc.index_page_path from office_config oc "
            			+ " where oc.office_id =?";
            	Record rec = Db.findFirst(officeConfig, user.getLong("office_id"));
            	if(rec == null || rec.getStr("index_page_path") == null){
            		render("/yh/index.html");
            	}else{
            		render(rec.getStr("index_page_path"));
            	}
            }
        }

    }

	private void setPermissionToSession() {
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		getCurrentPermission getPermission = getCurrentPermission.getInstance();
		Map<String,String> map = getPermission.currentHasPermission(currentUser,pom);
		setSessionAttr("permissionMap", map);
		setAttr("permissionMap", map);
	}

	private void updateLastLogin(UserLogin user) {
		Date now = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");//可以方便地修改日期格式
		String currentTime = dateFormat.format( now );
		user.set("last_login", currentTime);
		user.update();
	}

    public void login() {

    	if (isAuthenticated()) {
    		redirect("/");
    	}
        String username = getPara("username");
        
        setSysTitle();
        
        if (username == null) {
            render("/yh/login.html");
            return;
        }
        UsernamePasswordToken token = new UsernamePasswordToken(getPara("username"), getPara("password"));

        if (getPara("remember") != null && "Y".equals(getPara("remember")))
            token.setRememberMe(true);

        String errMsg = "";
        try {
            currentUser.login(token);
            if (getPara("remember") != null && "Y".equals(getPara("remember"))){
                // timeout:-1000ms 这样设置才能永不超时 
            	currentUser.getSession().setTimeout(-1000L);
            }

        } catch (UnknownAccountException uae) {
            errMsg = "用户名不存在";
            errMsg = "用户名/密码不正确";
            uae.printStackTrace();
        } catch (IncorrectCredentialsException ice) {
            errMsg = "密码不正确";
            errMsg = "用户名/密码不正确";
            ice.printStackTrace();
        } catch (LockedAccountException lae) {
            errMsg = "用户名已被停用";
            lae.printStackTrace();
        } catch (AuthenticationException ae) {
            errMsg = "用户名/密码不正确";
            ae.printStackTrace();
        }

        if (errMsg.length()==0) {
        	
        	UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=? and (is_stop = 0 or is_stop is null)",currentUser.getPrincipal());
        	
        	
        	if(user==null){
            	errMsg = "用户名不存在或已被停用";
            	setAttr("errMsg", errMsg);
            	render("/yh/login.html");
            }else if(user.get("c_name") != null && !"".equals(user.get("c_name"))){
            	setAttr("userId", user.get("c_name"));
            	/*setAttr("login_time",user.get("last_login"));*/
            	redirect("/");
            	//render("/yh/index.html");
            }else{
            	setAttr("userId",currentUser.getPrincipal());
            	/*setAttr("login_time",user.get("last_login"));*/
            	redirect("/");
            	//render("/yh/index.html");
            };
          
            
        } else {
            setAttr("errMsg", errMsg);
            render("/yh/login.html");
        }
    }

	private void setSysTitle() {
		String serverName = getRequest().getServerName();
        String basePath = getRequest().getScheme()+"://"+getRequest().getServerName()+":"+getRequest().getServerPort()+"/";
        
        logger.debug(serverName);
        OfficeCofig of = OfficeCofig.dao.findFirst("select * from office_config where domain like '"+serverName +"%' or domain like '%"+serverName +"%'");
        if(of==null){//没有配置公司的信息会导致页面出错，显示空白页
        	of = new OfficeCofig();
        	of.set("system_title", "易达物流");
        	of.set("logo", "/yh/img/eeda_logo.ico");
        }
        UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name ='"+currentUser.getPrincipal()+"' and is_main=1");
        if(uo != null){
            Office office = Office.dao.findById(uo.get("office_id"));
            setAttr("office_name", office.get("office_name"));
        }
        setAttr("SYS_CONFIG", of);
	}

    public void logout() {
        currentUser.logout();
        redirect("/login");
    }

    // 使用common-email, javamail
    public void testMail() throws Exception {
        Email email = new SimpleEmail();
        email.setHostName("smtp.exmail.qq.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("",""));
        email.setSSLOnConnect(true);

        email.setFrom("");
        email.setSubject("忘记密码");
        email.setMsg("你的密码已重置");
        email.addTo("");
        email.send();
        
    }
    
    public void getTodoList(){
        Map orderMap = new HashMap();
        String pageIndex = getPara("sEcho");
        String sLimit = "";
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        Calendar pastDay = Calendar.getInstance();
        pastDay.add(Calendar.DAY_OF_WEEK, -5);
        String sql = " select * from (SELECT "
                +"     'PS' type, dor.id, dor.order_no, cast(dor.business_stamp as char) business_stamp, "
                +"     (select group_concat(serial_no separator ',') from transfer_order_item_detail toid where toid.delivery_id = dor.id) serial_no,"
                +"     status, dor.route_from, lf.name from_name, dor.route_to, lt.name to_name, "
                +" 	   ifnull(GROUP_CONCAT(cast(o.id as char)),(select GROUP_CONCAT(cast(o.id as char)) from office o LEFT JOIN location l on l.pcode = o.location where l.code = dor.route_from)) office_id,"
                +"     ifnull(GROUP_CONCAT(cast(o.office_name as char)),(select GROUP_CONCAT(cast(o.office_name as char)) from office o LEFT JOIN location l on l.pcode = o.location where l.code = dor.route_from)) office_name,"
                +"		(case   when (select l.id from location l  "
                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
                +" 		where l.code = dor.route_from and l2.pcode = 1) is null"
                +" 		then"
                +" 		(select l.code from location l "
                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
                +" 		LEFT JOIN location l3 on l3.pcode = l2.code"
                +" 		where l3.code = dor.route_from)"
                +" 		when "
                +" 		(select l.id from location l "
                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
                +" 		where l.code = dor.route_from  and l2.pcode = 1) is not null"
                +" 		then"
                +" 		 (select l.code from location l "
                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
                +" 		where l2.code = dor.route_from)"
                +" 		end"
                +" 		) province "
                +" FROM"
                +"     delivery_order dor"
                +"     left join location lf on lf.code = dor.route_from"
                +"     left join location lt on lt.code = dor.route_to"
                +"     left join office o on o.location = dor.route_from"
                +"     left join user_office uo on o.id = uo.office_id and uo.user_name = '"+currentUser.getPrincipal()+"'"
                +" WHERE"
                +"     status = '新建'"
                +"         AND (business_stamp > DATE_SUB(NOW(), INTERVAL 5 DAY)"
                +"         OR NOW() >= business_stamp)"
                +" union"
                +"    select 'YS' type, tor.id, tor.order_no, '' business_stamp, '' serial_no, group_concat(distinct tor.status separator ',') status, "
                +"    tor.route_from, lf.name from_name, tor.route_to, lt.name to_name, "
                +"    ifnull(GROUP_CONCAT(cast(o.id as char)),(select GROUP_CONCAT(cast(o.id as char)) from office o LEFT JOIN location l on l.pcode = o.location where l.code = tor.route_to)) office_id, "
                +"    ifnull(GROUP_CONCAT(cast(o.office_name as char)),(select GROUP_CONCAT(cast(o.office_name as char)) from office o LEFT JOIN location l on l.pcode = o.location where l.code = tor.route_to)) office_name,"
                +"		(case   when (select l.id from location l  "
                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
                +" 		where l.code = tor.route_to and l2.pcode = 1) is null"
                +" 		then"
                +" 		(select l.code from location l "
                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
                +" 		LEFT JOIN location l3 on l3.pcode = l2.code"
                +" 		where l3.code = tor.route_to)"
                +" 		when "
                +" 		(select l.id from location l "
                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
                +" 		where l.code = tor.route_to  and l2.pcode = 1) is not null"
                +" 		then"
                +" 		 (select l.code from location l "
                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
                +" 		where l2.code = tor.route_to)"
                +" 		end"
                +" 		) province "
                +" from transfer_order tor"
                +"      left join depart_pickup dp on tor.id = dp.order_id"
                +"      left join depart_transfer dt on tor.id = dt.order_id "
                +"      left join location lf on lf.code = tor.route_from"
                +"      left join location lt on lt.code = tor.route_to"
                +"      left join office o on o.location = tor.route_to"
                +"      left join user_office uo on o.id = uo.office_id and uo.user_name = '"+currentUser.getPrincipal()+"'"
                +"    where tor.status not in ('新建', '已签收', '已入库' ,'已收货','配送中', '取消', '部分配送中', '手动删除', '已投保', '部分已签收')"
                +"    group by tor.id) B"
                +" 	  where "
                +" 	  province IN ( SELECT l.pcode FROM user_office uo  "
                +" 	  LEFT JOIN office o on o.id = uo.office_id "
                +" 	  LEFT JOIN location l on l.`code` = o.location"
                +" 	  WHERE"
                +" 	  user_name = '" + currentUser.getPrincipal() +"')";
//                + "office_id in (select office_id from user_office where user_name = '" + currentUser.getPrincipal() +"')";
        
        Record rec = Db.findFirst("select count(1) total from (" + sql + ") A");
        
        List<Record> list = Db.find(sql + sLimit);
        
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", list);
        renderJson(orderMap);
    }
    
    @Before(SetAttrLoginUserInterceptor.class)
    public void m() {
        String module_id = getPara(0);
        String param1 = getPara(1);
        
        String page = "";
        if(param1 == null){
            page = "/yh/profile/module/searchOrder.html";
        }else{
            if(StringUtils.isNumeric(param1)){//edit
                setAttr("order_id", param1);
            }else if("add".equals(param1)){
                
            }
            page = "/yh/profile/module/editOrder.html";
        }

        UserLogin user = LoginUserController.getLoginUser(this);
        //查询当前用户菜单
        String sql ="select distinct module.* from modules o, modules module "
                +"where o.parent_id = module.id and o.office_id=? and o.status = '启用' order by seq";
        List<Record> modules = Db.find(sql, user.get("office_id"));
        for (Record module : modules) {
            sql ="select * from modules where parent_id =? and status = '启用' order by seq";
            List<Record> orders = Db.find(sql, module.get("id"));
            module.set("orders", orders);
        }
        setAttr("modules", modules);
        setAttr("module_id", module_id);
        render(page);
    }
    
    @Before(Tx.class)
    public void m_save() {
        String jsonStr=getPara("params");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        String orderId = dto.get("id").toString();
        if(StringUtils.isNotEmpty(orderId)){//update
            EedaCommonHandler.commonUpdate(dto);
        }else{//insert
            orderId = EedaCommonHandler.commonInsert(dto);
        }
        
        //返回order
//        String module_id = dto.get("module_id").toString();
//        ModuleController mc = new ModuleController();
//        Record sRec = mc.getOrderStructureDto(module_id);
//        sRec.set("id", orderId);
//        Record orderDto =EedaCommonHandler.getOrderDto(sRec.toJson());
        Record orderDto = new Record();
        orderDto.set("id", orderId);
        renderJson(orderDto);
    }

    
    public void m_getOrderData() {
        Record orderDto = new Record();
        String jsonStr=getPara("params");
        orderDto = EedaCommonHandler.getOrderDto(jsonStr);
        renderJson(orderDto);
    }
    
    public void m_search() {
        Enumeration<String>  paraNames= getParaNames();
        Map map= EedaCommonHandler.searchOrder(paraNames, getRequest());
        renderJson(map);
    }
}
