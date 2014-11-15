package controllers.yh.arap;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;

import controllers.yh.util.PermissionConstant;

//@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AccountAuditLogController extends Controller {
    private Logger logger = Logger.getLogger(AccountAuditLogController.class);
    Subject currentUser = SecurityUtils.getSubject();
     //@RequiresPermissions(value = {PermissionConstant.PERMSSION_PCO_LIST})
    public void index() {
    	    render("/yh/arap/AccountAuditLog/AccountAuditLogList.html");
    }

}
