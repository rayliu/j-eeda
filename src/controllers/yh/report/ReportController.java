package controllers.yh.report;

import java.util.HashMap;
import java.util.List;

import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;

import controllers.yh.util.PrintPatterns;

public class ReportController extends Controller {

    private Logger logger = Logger.getLogger(ReportController.class);

    public void index() {
        
    }
    
    public static String test(String test){
    	return "test:"+test;
    }

    public void printCheckOrder(){
    	String order_no = getPara("order_no");
    	String fileName ="report/checkOrder.jasper";
    	String outFileName = "WebRoot/download/供应商对账单";
    	HashMap<String, Object> hm = new HashMap<String, Object>();
    	hm.put("order_no", order_no);
    	String file = PrintPatterns.getInstance().print(fileName,outFileName,hm);
    	renderText(file.substring(7));
    }
    public String pritCheckOrderByPay(String order_no){
    	String fileName ="report/checkOrder.jasper";
    	String outFileName = "WebRoot/download/供应商对账单";
    	HashMap<String, Object> hm = new HashMap<String, Object>();
    	hm.put("order_no", order_no);
    	String file = PrintPatterns.getInstance().print(fileName, outFileName + order_no, hm);
    	return file;
    }
    public void printReimburse(){
    	String order_no = getPara("order_no");
    	String fileName ="report/reimburse.jasper";
    	String outFileName = "WebRoot/download/报销单";
    	HashMap<String, Object> hm = new HashMap<String, Object>();
    	hm.put("order_no", order_no);
    	String file = PrintPatterns.getInstance().print(fileName, outFileName + order_no, hm);
    	renderText(file.substring(7));
    }
   public void printPayMent(){
	   String order_no = getPara("order_no");
	   ArapCostInvoiceApplication arapAuditInvoiceApplication = ArapCostInvoiceApplication.dao.findFirst("select * from arap_cost_invoice_application_order where order_no = ?",order_no);
	   List<ArapCostOrder> list = ArapCostOrder.dao.find("select * FROM arap_cost_order where application_order_id = ?",arapAuditInvoiceApplication.get("id"));
	   String checkOrderFile ="";
	   StringBuffer buffer = new StringBuffer();
	   for (ArapCostOrder arapCostOrder : list) {
		   checkOrderFile = pritCheckOrderByPay(arapCostOrder.getStr("order_no"));
		   buffer.append(checkOrderFile.substring(7));
		   buffer.append(",");
	   }
	   String fileName ="report/payment.jasper";
	   String outFileName = "WebRoot/download/付款申请单";
	   HashMap<String, Object> hm = new HashMap<String, Object>();
	   hm.put("order_no", order_no);
	   String file = PrintPatterns.getInstance().print(fileName,outFileName + order_no,hm);
	   buffer.append(file.substring(7));
	   buffer.append(",");
	   renderText(buffer.toString());
   }
   public void printSign(){
	   String type = getPara("sign");
	   String order_no = getPara("order_no");
	   String muban = type + ".jasper";
	  
	   String fileName ="report/" + muban;
	   String outFileName = "WebRoot/download/";
	   
	   if(type.contains("guoguang")){
		   outFileName  +="国光标准单";
	   }else if(type.contains("nonghang")){
		   outFileName +="农行";
	   }else if(type.contains("china_post")){
		   outFileName +="中国邮政";
	   }else{
		   outFileName +="中国邮储";
	   }
	   
	   HashMap<String, Object> hm = new HashMap<String, Object>();
	   hm.put("order_no", order_no);
	   boolean is_one = muban.contains("_one");
	   TransferOrder to = TransferOrder.dao.findFirst("select id,cargo_nature_detail from transfer_order where order_no = ?",order_no);
	   List<TransferOrderItemDetail> list = TransferOrderItemDetail.dao.find("select id,serial_no from transfer_order_item_detail where order_id =?",to.get("id"));
	   
	   if(list.size()>0){
		   StringBuffer buffer = new StringBuffer();
			   for(int i=0;i<list.size();i++){
				   if(is_one){
					   hm.put("id", list.get(i).get("id"));
					   if(list.get(i).get("serial_no") != null){
						   outFileName += "-" + list.get(i).get("serial_no");
					   }
					   
					   String file = PrintPatterns.getInstance().print(fileName,outFileName,hm);
					  
					   buffer.append(file.substring(7));
					   buffer.append(",");
					   break;
				   }else{
					   hm.put("id", list.get(i).get("id"));
					   if(list.get(i).get("serial_no") != null){
						   outFileName += "-" + list.get(i).get("serial_no");
					   }
					   
					   String file = PrintPatterns.getInstance().print(fileName,outFileName,hm);
						try {
							   Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						   
					   buffer.append(file.substring(7));
					   buffer.append(",");
				   }
				   
			   }  
		   
		   
		   renderText(buffer.toString());
	   }else{
		   if("cargoNatureDetailNo".equals(to.get("cargo_nature_detail"))){
			   TransferOrderItem toi = TransferOrderItem.dao.findFirst("select * from transfer_order_item where order_id = "+to.get("id"));
			  // hm.put("amount", toi.get("amount"));
		   }
		   
		   String file = PrintPatterns.getInstance().print(fileName,outFileName,hm);
		   renderText(file.substring(7)); 
	   }
	    
   }
   public void printSignCargo(){
	   String type = getPara("sign");
	   String order_no = getPara("order_no");
	   String muban = type + ".jasper";
	  
	   String fileName ="report/" + muban;
	   String outFileName = "WebRoot/download/";
	   outFileName += "普通签收单";
	   HashMap<String, Object> hm = new HashMap<String, Object>();
	   hm.put("order_no", order_no);
	   StringBuffer buffer = new StringBuffer();
	   String file = PrintPatterns.getInstance().print(fileName,outFileName,hm);
	   buffer.append(file.substring(7));
	   buffer.append(",");
	   renderText(buffer.toString());
	   
   }
  
}
