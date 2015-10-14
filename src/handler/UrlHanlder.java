package handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;
import com.jfinal.log.Logger;

public class UrlHanlder extends Handler {
	private Logger logger = Logger.getLogger(UrlHanlder.class);

	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, boolean[] isHandled) {
		String ip = getRemortIP(request);// 返回发出请求的IP地址
		String params = request.getQueryString();// 返回请求行中的参数部分
		String host = request.getRemoteHost();// 返回发出请求的客户机的主机名
		int port = request.getRemotePort();// 返回发出请求的客户机的端口号。

		logger.debug("handle url: " + target);
		logger.debug("IP: " + ip);
		logger.debug("params: " + params);
		logger.debug("host: " + host + ":" + port);
		logger.debug("----------------------------------------------------------");
		// login page handle
		if (target.indexOf("/login") > 0 && target.split("/").length > 2) {
			target = target.substring(target.indexOf("/", 2));
		}
		// logger.debug("content path:"+target);
		nextHandler.handle(target, request, response, isHandled);
	}

	private String getRemortIP(HttpServletRequest request) {
		if (request.getHeader("x-forwarded-for") == null) {
			return request.getRemoteAddr();
		}
		return request.getHeader("x-forwarded-for");
	}
}
