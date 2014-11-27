package interceptor;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;

public class SetAttrLoginUserInterceptor implements Interceptor{

	@Override
	public void intercept(ActionInvocation ai) {
		Subject currentUser = SecurityUtils.getSubject();
		ai.getController().setAttr("userId", currentUser.getPrincipal());
		ai.invoke();
	}

}
