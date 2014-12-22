package controllers.yh.report;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.DbKit;

public class ReportController extends Controller {

    private Logger logger = Logger.getLogger(ReportController.class);

    public void index() {
        
    }
    private String print(String order_no,String name){
    	 String fileName = "report/";
         String outFileName="WebRoot/download/";
         fileName +=name;
         Date date = new Date();
         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
         
    	 if(name.equals("checkOrder.jasper")){
    		 outFileName +="供应商对账单_"+format.format(date)+".pdf";
    	 }else{
         	outFileName +="付款申请单_"+format.format(date)+".pdf";
    	 }
    	HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("order_no", order_no);
        
        //hm.put("SUBREPORT_DIR", "report/");
        
        //File file = new File(fileName);
		try {
			//JasperReport jr = (JasperReport)JRLoader.loadObject(file);
			JasperPrint print = JasperFillManager.fillReport(fileName, hm, DbKit.getConnection());
			JasperExportManager.exportReportToPdfFile(print, outFileName);
		} catch (JRException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return outFileName;
    }
    public void printCheckOrder(){
    	String order_no = getPara("order_no");
    	String file = print(order_no,"checkOrder.jasper");
    	renderText(file.substring(7));
    }
   public void printPayMent(){
	   String order_no = getPara("order_no");
	   String file = print(order_no,"payment.jasper");
	   renderText(file.substring(7));
   }

}
