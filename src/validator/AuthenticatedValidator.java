package validator;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;


public class AuthenticatedValidator extends Validator {
	Subject currentUser = SecurityUtils.getSubject();
	
	@Override
	protected void handleError(Controller controller) {
		//controller.redirect("/login");
	}

	@Override
	protected void validate(Controller controller) {
		System.out.println(this);
		//setShortCircuit(true);		
        if (!currentUser.isAuthenticated()) {
            controller.redirect("/yh/login");
        }
	}



}
