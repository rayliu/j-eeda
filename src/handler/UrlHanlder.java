package handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;
import com.jfinal.log.Logger;

public class UrlHanlder extends Handler {
	private Logger logger = Logger.getLogger(UrlHanlder.class);
	@Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		//logger.debug("dynamic handle url:"+target);
		//login page handle
		if(target.indexOf("/login")>0 && target.split("/").length>2){
			target = target.substring(target.indexOf("/", 2));
		}
		//logger.debug("content path:"+target);
		nextHandler.handle(target, request, response, isHandled);
	}

}
