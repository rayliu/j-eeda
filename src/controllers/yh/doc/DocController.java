package controllers.yh.doc;


import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
/*
 * 帮助文档的类
 */
public class DocController  extends Controller{
	
	private Logger logger = Logger.getLogger(DocController.class);
	
	public void index(){
		render("/yh/doc/index.html");
	}
	
	public void faq(){
		render("/yh/doc/faq.html");
	}
}
