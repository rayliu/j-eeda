package controllers.yh.carmanage;

import javax.servlet.http.HttpServletRequest;

import models.DepartOrder;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;

import controllers.yh.profile.CarinfoController;

public class CarReimbursementController extends Controller {
	private Logger logger = Logger.getLogger(CarinfoController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	public void index() {
        HttpServletRequest re = getRequest();
        String url = re.getRequestURI();
        logger.debug("URI:" + url);
        if (url.equals("/carreimbursement")) {
                render("/yh/carmanage/carReimbursementList.html");
        }
    }
	//创建行车单
	public void createCarReimbursement(){
		render("/yh/carmanage/carReimbursementEdit.html");
	}
	
}
