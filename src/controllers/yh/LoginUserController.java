package controllers.yh;import interceptor.SetAttrLoginUserInterceptor;import java.util.ArrayList;import java.util.Arrays;import java.util.HashMap;import java.util.List;import java.util.Map;import models.Office;import models.UserCustomer;import models.UserLogin;import models.UserOffice;import models.UserRole;import org.apache.shiro.SecurityUtils;import org.apache.shiro.authz.annotation.Logical;import org.apache.shiro.authz.annotation.RequiresAuthentication;import org.apache.shiro.authz.annotation.RequiresPermissions;import org.apache.shiro.subject.Subject;import com.jfinal.aop.Before;import com.jfinal.core.Controller;import com.jfinal.log.Logger;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;import controllers.yh.util.CompareStrList;import controllers.yh.util.PermissionConstant;@RequiresAuthentication@Before(SetAttrLoginUserInterceptor.class)public class LoginUserController extends Controller {    private Logger logger = Logger.getLogger(LoginUserController.class);    // in config route已经将路径默认设置为/yh    // me.add("/yh", controllers.yh.AppController.class, "/yh");    Subject currentUser = SecurityUtils.getSubject();        public static Long getLoginUserId(Controller controller) {    	Subject currentUserNew = SecurityUtils.getSubject();                UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='"+currentUserNew.getPrincipal().toString()+"'");        if (user!=null) {            return user.getLong("id");        }        return -1L;    }    @RequiresPermissions(value = {PermissionConstant.PERMSSION_U_LIST})    public void index() {        render("/yh/profile/loginUser/loginUser.html");    }    // show增加用户页面    @RequiresPermissions(value = {PermissionConstant.PERMSSION_U_CREATE})    public void addUser() {        render("/yh/profile/loginUser/addUser.html");    }    // show编辑用户    @RequiresPermissions(value = {PermissionConstant.PERMSSION_U_UPDATE})    public void edit() {    	String username = currentUser.getPrincipal().toString();        String id = getPara();        if (id != null) {            UserLogin user = UserLogin.dao.findById(id);            setAttr("lu", user);            render("/yh/profile/loginUser/addUser.html");        }        if (username != null && id == null) {            UserLogin user = UserLogin.dao.findFirst(                    "select * from user_login where user_name=?", username);            setAttr("lu", user);            render("/yh/profile/loginUser/addUser.html");        }    }    // 添加登陆用户    @RequiresPermissions(value = {PermissionConstant.PERMSSION_U_CREATE, PermissionConstant.PERMSSION_U_UPDATE}, logical=Logical.OR)    public void saveUser() {            	UserLogin user=null;    	String username=getPara("username");        String id = getPara("userId");        String name = getPara("name");        /*String offices =getPara("officeSelect");*/        /*String is_main =getPara("isMain_radio");*/        /*String officeId = getPara("officeSelect");*/                if (id != "") {            user = UserLogin.dao.findById(id);        }else{        	user = new UserLogin();        	user.set("user_name", username);        }        user.set("password", getPara("password"));        user.set("password_hint", getPara("pw_hint"));        user.set("c_name", name);                   /*if(is_main != null && !"".equals(is_main)){        	user.set("office_id", is_main);        }*/        if (id != "") {           // logger.debug("update....");            user.update();        } else {            user.save();        }        //index();        setAttr("lu", user);        render("/yh/profile/loginUser/addUser.html");    }    // 删除用户    @RequiresPermissions(value = {PermissionConstant.PERMSSION_U_DELETE})    public void del() {        String id = getPara();        if (id != null) {        	UserLogin u = UserLogin.dao.findFirst("select * from user_login where id = ?",id);        	if(u.get("is_stop") == null || "".equals(u.get("is_stop")) || u.get("is_stop").equals(false)        			|| u.get("is_stop").equals(0)){        		u.set("is_stop", true);        	}else{        		u.set("is_stop", false);        	}        	u.update();        }        render("/yh/profile/loginUser/loginUser.html");    }    // 列出用户信息    @RequiresPermissions(value = {PermissionConstant.PERMSSION_U_LIST})    public void listUser() {        String sLimit = "";        String pageIndex = getPara("sEcho");        if (getPara("iDisplayStart") != null                && getPara("iDisplayLength") != null) {            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "                    + getPara("iDisplayLength");        }        // 获取总条数        String totalWhere = "";        String sql = "select count(1) total from user_login ";        Record rec = Db.findFirst(sql + totalWhere);        logger.debug("total records:" + rec.getLong("total"));        // 获取当前页的数据        List<Record> orders = Db                .find("select id,user_name,password_hint,c_name,is_stop from user_login");        Map orderMap = new HashMap();        orderMap.put("sEcho", pageIndex);        orderMap.put("iTotalRecords", rec.getLong("total"));        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));        orderMap.put("aaData", orders);        renderJson(orderMap);    }    public void officeList(){    	String id = getPara("userId");    	if(id==null || "".equals(id)){			 Map<String, List> map = new HashMap<String, List>();			 map.put("userOffice", null);			 renderJson(map);             return;    	}    	UserLogin user= UserLogin.dao.findById(id);    	List<UserOffice> ulist = UserOffice.dao.find("select uo.user_name,uo.office_id,o.office_name,uo.is_main from user_office uo left join office o on o.id = uo.office_id"    			+ "  where user_name =?",user.get("user_name"));		 Map<String, List> map = new HashMap<String, List>();		 map.put("userOffice", ulist);        renderJson(map);    	    }    public void saveOffice(){    	String id = getPara("id");    	String ids = getPara("officeIds");    	UserLogin user= UserLogin.dao.findById(id);    	List<UserOffice> ulist = UserOffice.dao.find("select * from user_office where user_name=?",user.get("user_name"));    	    	if(ids!=null&&!"".equals(ids)){    		String[] officeIds=ids.split(",");    		if(ulist.size()>0){    			List<Object> oldList = new ArrayList<Object>();    			for (UserOffice userOffice : ulist) {					oldList.add(userOffice.get("office_id"));				}     			List<String> strList = new ArrayList(Arrays.asList(officeIds));    	           			List<Object> temp = new ArrayList<Object>();    			for(int i=0;i<oldList.size();i++){    	        	for(int j=0;j<strList.size();j++){    	        		 if(oldList.get(i).toString().equals(strList.get(j).toString())){    	                     temp.add(oldList.get(i));                        	                 }    	        	}    	        }    			 for(int i = 0;i<temp.size();i++){    				 oldList.remove(temp.get(i));    				     	             strList.remove(temp.get(i).toString());    	        }    			if(oldList.size()>0){    				for (Object object : oldList) {    					UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name=? and office_id=?",user.get("user_name"),object);    					uo.delete();    				}    			}    			    			    			for (Object object : strList) {    				if(object!=null&&!"".equals(object)){    					UserOffice userOffice= new UserOffice();        				userOffice.set("user_name", user.get("user_name"));        				userOffice.set("office_id", object);        				userOffice.save();    				}    								}    		}else{    			for (String string : officeIds) {    				if(string!=null&&!"".equals(string)){	    				UserOffice userOffice= new UserOffice();	    				userOffice.set("user_name", user.get("user_name"));	    				userOffice.set("office_id", string);	    				userOffice.save();    				}    			}    		}    	}else{    		for (UserOffice userOffice : ulist) {    			userOffice.delete();    		}    	}    	    	renderJson(user);    }    public void searchAllOffice() {		List<Office> offices = Office.dao.find("select id,office_name from office");		renderJson(offices);	}    public void saveIsmain(){    	UserLogin ul = UserLogin.dao.findById(getPara("id"));    	UserRole ur = UserRole.dao.findFirst("select * from user_role where user_name=? and role_code='admin'",ul.get("user_name"));    	UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name=? and office_id=?",ul.get("user_name"),getPara("office_id"));    	/*判断当前用户不是管理员*/    	if(ur==null||"".equals(ur)){    		UserOffice user_office = UserOffice.dao.findFirst("select * from user_office where user_name=? and is_main=?",ul.get("user_name"),true);        	if(user_office!=null&&!"".equals(user_office)&&!user_office.equals(false)){        		user_office.set("is_main", false);        		user_office.update();        	}        	String office_id=getPara("office_id");        	boolean is_mian;        	if(office_id==null||"".equals(office_id)){        		is_mian=false;        	}else{        		is_mian=true;        	}        	if(uo==null){        		uo=new UserOffice();        		uo.set("user_name", ul.get("user_name"));        		uo.set("office_id", getPara("office_id"));        		uo.set("is_main",is_mian);        		uo.save();        	}else{        		//TODO        		uo.set("is_main",is_mian);        		uo.update();        	}        	ul.set("office_id",getPara("office_id"));        	ul.update();    	}    	renderJson();    }    public void searchAllCustomer(){    	List<Record> partys =Db.find("select p.id pid, c.company_name from party p left join  contact c on p.contact_id = c.id where p.party_type = 'CUSTOMER'");		renderJson(partys);    }    public void saveUserCustomer(){    	String id = getPara("id");    	UserLogin user= UserLogin.dao.findById(id);    	List<UserCustomer> uclist = UserCustomer.dao.find("select *from user_customer where user_name=?",user.get("user_name"));    	String cutomers = getPara("customers");    	if(cutomers!=null&&!"".equals(cutomers)){    		String[] ids = cutomers.split(",");    		    		if(uclist==null||"".equals(uclist)){    			for (String string : ids) {    				if(string!=null&&!"".equals(string)){    					UserCustomer uc= new UserCustomer();        				uc.set("user_name", user.get("user_name"));        				uc.set("customer_id", string);        				uc.save();    				}    				    			}    		}else{    			List<Object> uc_ids = new ArrayList<Object>();    			for (UserCustomer u_c: uclist) {					uc_ids.add( u_c.get("customer_id"));				}    			CompareStrList com = new CompareStrList();    			List<Object> list = com.compare(uc_ids, ids);    			List<Object> removerList = new ArrayList<Object>();    			removerList=(List<Object>) list.get(0);    			if(removerList.size()>0){    				for (Object str : removerList) {    					UserCustomer uc = UserCustomer.dao.findFirst("select * from user_customer where user_name=? and customer_id=?",user.get("user_name"),str);    					uc.delete();    				}    			}    			    			List<Object> addList = (List<Object>) list.get(1);    			for (Object str : addList) {    				if(str!=null&&!"".equals(str)){    					UserCustomer uc= new UserCustomer();        				uc.set("user_name", user.get("user_name"));        				uc.set("customer_id", str);        				uc.save();    				}    								}    			    		}    		    	}else{    		for (UserCustomer u_c: uclist) {				u_c.delete();			}    	}    	renderJson();    }    public void delOffice(){    	String id= getPara("id");    	String office_id = getPara("office_id");    	UserLogin user= UserLogin.dao.findById(id);    	UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name=? and office_id=?",user.get("user_name"),office_id);    	    	UserRole userRole = UserRole.dao.findFirst("select * from user_role where user_name=? and role_code='admin'",user.get("user_name"));    			if(uo.get("is_main")!=null&&!"".equals(uo.get("is_main"))&&!"0".equals(uo.get("is_main"))){    		user.set("office_id", null);    		user.update();    		if(userRole==null||"".equals(userRole)){    			uo.delete();    		}    	}else{    		uo.delete();    	}    	    	renderJson();    }    public void delCustomer(){    	String id= getPara("id");    	String customer_id = getPara("customer_id");    	UserLogin user= UserLogin.dao.findById(id);    	UserCustomer uc = UserCustomer.dao.findFirst("select * from user_customer where user_name=? and customer_id=?",user.get("user_name"),customer_id);    	uc.delete();    	renderJson();    }    public void customerList(){    	String id = getPara("userId");    	if(id==null || "".equals(id)){			 Map<String, List> map = new HashMap<String, List>();			 map.put("customerlist", null);			 renderJson(map);             return;    	}    	UserLogin user= UserLogin.dao.findById(id);    	List<UserCustomer> ulist = UserCustomer.dao.find("select uc.id,uc.customer_id, c.company_name from user_customer uc left join party p on p.id = uc.customer_id left join contact c on p.contact_id = c.id where user_name=?",user.get("user_name"));		Map<String, List> map = new HashMap<String, List>();		map.put("customerlist", ulist);        renderJson(map);    }}