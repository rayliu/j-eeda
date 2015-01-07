package controllers.yh.report;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.TransferOrder;
import models.TransferOrderItemDetail;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.DbKit;

public class ReportController extends Controller {

    private Logger logger = Logger.getLogger(ReportController.class);

    public void index() {
        
    }
    private String print(String order_no,String name,Object id){
    	
    	 String fileName = "report/";
         String outFileName="WebRoot/download/";
         File file = new File(outFileName);
         if(!file.exists()){
        	 file.mkdir();
         }
         fileName +=name;
         Date date = new Date();
         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
         
    	 if(name.equals("checkOrder.jasper")){
    		 outFileName +="供应商对账单_"+format.format(date)+".pdf";
    	 }else if(name.equals("payment.jasper")){
         	outFileName +="付款申请单_"+format.format(date)+".pdf";
    	 }else if(name.equals("guoguang.jasper")){
    		 outFileName +="国光标准单"+format.format(date)+".pdf";
    	 }else if(name.equals("nonghang.jasper")){
    		 outFileName +="农行"+format.format(date)+".pdf";
    	 }else if(name.equals("china_post.jaspe")){
    		 outFileName +="中国邮政"+format.format(date)+".pdf";
    	 }else{
    		 outFileName +="中国邮储"+format.format(date)+".pdf";
    	 }
    	HashMap<String, Object> hm = new HashMap<String, Object>();
    	hm.put("order_no", order_no);
    	if( !"0".equals(id)){
        	hm.put("id", id);
    	}
		try {
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
    	String file = print(order_no,"checkOrder.jasper","0");
    	renderText(file.substring(7));
    }
   public void printPayMent(){
	   String order_no = getPara("order_no");
	   String file = print(order_no,"payment.jasper","0");
	   renderText(file.substring(7));
   }
   public void printSign(){
	   String type = getPara("sign");
	   String order_no = getPara("order_no");
	   String muban = "";
	   if(type.equals("signGuoguang")){
		   muban = "guoguang.jasper";
	   }else if(type.equals("signNonghang")){
		   muban = "nonghang.jasper";
	   }else if(type.equals("post")){
		   muban = "china_post.jasper";
	   }else{
		   muban = "china_postal.jasper";
	   }
	   TransferOrder to = TransferOrder.dao.findFirst("select id from transfer_order where order_no = ?",order_no);
	   List<TransferOrderItemDetail> list = TransferOrderItemDetail.dao.find("select id from transfer_order_item_detail where order_id =?",to.get("id"));
	   if(list.size()>0){
		   StringBuffer buffer = new StringBuffer();
		   for(int i=0;i<list.size();i++){
			   String file = print(order_no,muban,list.get(i).get("id"));
				try {
					   Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				   
			   buffer.append(file.substring(7));
			   buffer.append(",");
		   }
		   renderText(buffer.toString());
	   }else{
		   String file = print(order_no,muban,"0");
		   renderText(file.substring(7)); 
	   }
	    
   }
  
}
