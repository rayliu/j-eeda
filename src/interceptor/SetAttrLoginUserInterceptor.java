package interceptor;

import javax.servlet.http.HttpServletRequest;

import models.UserLogin;
import models.yh.profile.OfficeCofig;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;

public class SetAttrLoginUserInterceptor implements Interceptor{
	private Logger logger = Logger.getLogger(SetAttrLoginUserInterceptor.class);
	@Override
	public void intercept(ActionInvocation ai) {
		Subject currentUser = SecurityUtils.getSubject();
		if(currentUser.isAuthenticated()){
			UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=?",currentUser.getPrincipal());
			if(user.get("c_name") != null && !"".equals(user.get("c_name"))){
				ai.getController().setAttr("userId", user.get("c_name"));
			}else{
				ai.getController().setAttr("userId", currentUser.getPrincipal());
			}
			ai.getController().setAttr("user_login_id", currentUser.getPrincipal());
			setSysTitle(ai.getController());
			ai.getController().setAttr("permissionMap", ai.getController().getSessionAttr("permissionMap"));
		}
		ai.invoke();
	}
	
	private void setSysTitle(Controller controller) {
		HttpServletRequest request = controller.getRequest();
		String serverName = request.getServerName();
        String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
        
        logger.debug("Current host path:"+basePath);
        OfficeCofig of = OfficeCofig.dao.findFirst("select * from office_config where domain like '"+serverName +"%' or domain like '%"+serverName +"%'");
        if(of==null){//没有配置公司的信息会导致页面出错，显示空白页
        	of = new OfficeCofig();
        	of.set("system_title", "");
        	of.set("logo", "");
        }
        controller.setAttr("SYS_CONFIG", of);
	}

}
