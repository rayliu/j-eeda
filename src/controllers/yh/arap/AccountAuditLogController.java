package controllers.yh.arap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeItem;
import models.ArapChargeOrder;
import models.Party;
import models.UserLogin;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class AccountAuditLogController extends Controller {
    private Logger logger = Logger.getLogger(AccountAuditLogController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
    	if(LoginUserController.isAuthenticated(this))
    	    render("/yh/arap/AccountAuditLog/AccountAuditLogList.html");
    }

}
