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

import controllers.yh.AppController;

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
			setSysTitle(ai.getController());
		}
		ai.invoke();
	}
	
	private void setSysTitle(Controller controller) {
		HttpServletRequest request = controller.getRequest();
		String serverName = request.getServerName();
        String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
        
        logger.debug(basePath);;
        OfficeCofig of = OfficeCofig.dao.findFirst("select * from office_config where domain like '"+serverName +"%' or domain like '%"+serverName +"%'");
        controller.setAttr("SYS_CONFIG", of);
	}

}
