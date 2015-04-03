package controllers.yh.profile;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class InsuranceController extends Controller{
	public void index() {	
	    render("/yh/profile/insurance/insuranceList.html"); 
    }
}
