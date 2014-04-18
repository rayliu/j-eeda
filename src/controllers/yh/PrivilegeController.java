package controllers.yh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import models.Role;
import models.RolePermissions;
import models.UserLogin;
import models.UserRoles;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;


public class PrivilegeController extends Controller {
	private Logger logger = Logger.getLogger(PrivilegeController.class);
	List<String> rolejson=new ArrayList<String>();
	
	public void index(){
		render("profile/privilege/PrivilegeList.html");
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
		String sql = "select count(1) total from Toll_table where type ='收费' ";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据GROUP_CONCAT(privilege SEPARATOR ' ') as privilege
		List<Record> orders = Db.find("select ur.id,ur.user_id, (select user_name from user_login u where ur.user_id=u.id) user_name,"
				+ " ur.role_id,(select role_name from role u where ur.role_id=u.id) role_name, "
				+ " (select role_permission from role_permissions u where ur.role_id=u.role_id) as privilege from user_roles ur "
				+ "where 1=1 ");
				//+ "group by user_name limit 10"
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex); 
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);
		renderJson(orderMap);
	}
	//用户角色权限编辑方法
	public void AllSave(){		
		String uid=getPara("uid");
		StringBuffer sb = new StringBuffer();
		StringBuffer bs = new StringBuffer();					
		int roleid=Integer.parseInt(getPara("role"));
		RolePermissions	check=RolePermissions.dao.findById(roleid);
		List<String> form=new ArrayList<String>();
		String[] pr=getParaValues("per");
		boolean to=false;
		boolean t=false;
		for(int i = 0 ; i <pr.length; i ++){			
			String p=pr[i];				
		String	pf=p.substring(0,p.length()-1);
		String	pm=p.substring(1,p.length());
		form.add(pf);
		if(pf.equals("0")){				
			if(t==false){
				sb.append("运输单");
				t=true;
			}
			if(pm.equals("1")){				
				sb.append(":*/");				
			}
			if(pm.equals("2")){
				sb.append(":查看");
			}
			if(pm.equals("3")){
				sb.append(":增加");
			}
			if(pm.equals("4")){
				sb.append(":修改");
			}
			if(pm.equals("5")){
				sb.append(":删除");
			}						
		}				
			if(pf.equals("1")){
						
			if(to==false){
				bs.append("配送单");	
				to=true;
			}
			if(pm.equals("1")){
				bs.append(":*/");				
			}
			if(pm.equals("2")){
				bs.append(":查看");
			}
			if(pm.equals("3")){
				bs.append(":增加");
			}
			if(pm.equals("4")){
				bs.append(":修改");
			}
			if(pm.equals("5")){
				bs.append(":删除");
			}						
		}			
		}
		System.out.print(sb);
		boolean form1=false;
		boolean form2=false;
		for(int i=0;i<form.size();i++){
			String h=form.get(i);
			if(h.equals("0")){
				//String c=sb.toString();				
				form1=true;
			}
			if(h.equals("1")){
				form2=true;
			}
		}
		if(form1==true && form2==false){
			RolePermissions rp=new RolePermissions();			
			//rp.findById("Role_id");
			if(uid!=null ||check!=null){
				RolePermissions	up=null;
				if(uid!=null){
						up=RolePermissions.dao.findById(Integer.parseInt(uid));
				}else{
					up=RolePermissions.dao.findById(roleid);
				}
				
			up.set("role_id", roleid).set("role_permission", sb.toString()).update();
			}else{				
					rp.set("role_id", roleid).set("role_permission", sb.toString()).save();												
			}
		}
		if(form1==false && form2==true){
			RolePermissions rp=new RolePermissions();			
			//rp.findById("Role_id");
			if(uid!=null ||check!=null){
				RolePermissions	up=null;
				if(uid!=null){
						up=RolePermissions.dao.findById(Integer.parseInt(uid));
				}else{
					up=RolePermissions.dao.findById(roleid);
				}				
			up.set("role_id", roleid).set("role_permission", bs.toString()).update();
			}else{				
					rp.set("role_id", roleid).set("role_permission", bs.toString()).save();												
			}
		}
	
		if(form2==true&&form1==true){
			RolePermissions rp=new RolePermissions();
			StringBuffer ss = new StringBuffer();
			ss.append(sb);
			ss.append(":");
			ss.append(bs);
		
		
			if(uid!=null ||check!=null){
				RolePermissions	up=null;
				if(uid!=null){
						up=RolePermissions.dao.findById(Integer.parseInt(uid));
				}else{
					up=RolePermissions.dao.findById(roleid);
				}				
			up.set("role_id", roleid).set("role_permission", ss.toString()).update();
			}else{			
					rp.set("role_id", roleid).set("role_permission", ss.toString()).save();								
			}
		}
	}
	//用户角色权限保存
	public void SaveRolePrivilege(){
		String uid=getPara("uid");
		String user =getPara("user")  ;
		if(user ==null){
			AllSave();			
		}else{
			int use =Integer.parseInt(user)  ;		
			String[] names = getParaValues("remember");		
			String roleid=getPara("role");			
			if(roleid==null){
			if(names.length==0){
				UserRoles u= new UserRoles();
				u.set("user_id", use).set("role_id", names).save();
				System.out.println("进入1");
			}else{
				for(int i=0;i<names.length;i++){
					UserRoles u= new UserRoles();
					int name =Integer.parseInt(names[i]);					
					if(uid!=null){
					UserRoles us=UserRoles.dao.findById(Integer.parseInt(uid));
					us.set("user_id", use).set("role_id", name).update();
					}else{
						u.set("user_id", use).set("role_id", name).save();
					}
				}				
				System.out.println("进入2");
			}
			}else{
				int rid=Integer.parseInt(roleid);
				UserRoles u=UserRoles.dao.findById(rid);
				
				u.set("user_id", use).set("role_id", rid).update();
				System.out.println("进入3");
			}
			if(uid!=null&&user!=null){
				AllSave();
			}										
	}
	index();			
	}	
	//编辑
	public void Edit(){
		String userid=getPara();	
		UserLogin  u=new UserLogin();
	    UserLogin ui=u.findById(userid);	    
		setAttr("id", ui);
		render("profile/privilege/EditPrivilege.html");
	}
	//用户对应角色
	public void userrole(){
	
		render("profile/privilege/UserRole.html");
	}
	//角色对应权限
	public void roleprivilege(){
		
		render("profile/privilege/RolePrivilege.html");
	}
	//select用户角色权限
	public void SelectUser(){
		String select=getPara("select");
		
		if(select.equals("1")){
			List<UserLogin> userjson = UserLogin.dao.find("select * from user_login");			
	        renderJson(userjson);
		}
		
		if(select.equals("2")){
			List<Role> rolejson = Role.dao.find("select * from role");			
	        renderJson(rolejson);
		}
		if(select.equals("3")){
			
			
			rolejson.add("运输单");
			rolejson.add("配送单");
		
	        renderJson(rolejson);
		}
		
	}
	//删除
	public void delete(){
		UserRoles d=new UserRoles();
		String id=getPara();
		
	 UserRoles del	=d.findById(id);
	 del.delete();
		index();
	}
}
