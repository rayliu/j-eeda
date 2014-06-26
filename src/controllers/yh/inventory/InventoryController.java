package controllers.yh.inventory;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;

import controllers.yh.LoginUserController;

public class InventoryController extends Controller {

    private Logger logger = Logger.getLogger(InventoryController.class);
    Subject currentUser = SecurityUtils.getSubject();

    private boolean isAuthenticated() {
        if (!currentUser.isAuthenticated()) {
            if (LoginUserController.isAuthenticated(this))
                redirect("/yh/login");
            return false;
        }
        setAttr("userId", currentUser.getPrincipal());
        return true;
    }

    public void index() {
        HttpServletRequest re = getRequest();
        String url = re.getRequestURI();
        logger.debug("URI:" + url);
        if (url.equals("/yh/gateIn")) {
            if (LoginUserController.isAuthenticated(this))
                render("inventory/gateIn.html");
        }
        if (url.equals("/yh/gateOut")) {
            if (LoginUserController.isAuthenticated(this))
                render("inventory/gateOut.html");
        }
    }

    public void gateIn_add() {
        render("/yh/inventory/gateInEdit.html");
    }

    public void gateOut_add() {
        render("/yh/inventory/gateOutEdit.html");
    }

}
