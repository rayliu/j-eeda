package controllers.yh.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;




import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;
import models.CostApplicationOrderRel;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;

import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import com.jfinal.core.Controller;

import controllers.yh.util.PrintPatterns;

public class ReportController extends Controller {

	private Logger logger = Logger.getLogger(ReportController.class);
	private static String contextPath = null;
			
	public void index() {

	}

	private String getContextPath() {
		if(contextPath == null){
			contextPath = getRequest( ).getSession().getServletContext().getRealPath("/");
		}
		return contextPath;
	}

	public void printCheckOrder() {
		String order_no = getPara("order_no");
		ArapCostOrder arapcostorder = ArapCostOrder.dao.findFirst("select * from arap_cost_order where order_no = ?",order_no);
		CostApplicationOrderRel costapplicationorderrel =CostApplicationOrderRel.dao.findFirst("select * from cost_application_order_rel where cost_order_id=?",arapcostorder.get("id"));
		String fileName = "report/checkOrder.jasper";
		String outFileName = "download/供应商对账单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		if(costapplicationorderrel!=null){
			hm.put("application_id", costapplicationorderrel.get("application_order_id"));
		}
		hm.put("order_no", order_no);
        fileName = getContextPath() + fileName;
        outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,
				hm);
		arapcostorder.set("amount_pdf",arapcostorder.getInt("amount_pdf")+1 ).set("is_pdf", "N").update();
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	public void printManualOrder() {
		String order_no = getPara("order_no").trim();
		String fileName = "report/arap_manual.jasper";
		String outFileName = "download/手工收入单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
        fileName = getContextPath() + fileName;
        outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,
				hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	public void printdamageCutomer() {
		String order_no = getPara("order_no").trim();
		String damageType = getPara("damageType").trim();
		String unit = getPara("unit").trim();
		String fileName = "report/damage_customer.jasper";
		String outFileName = "download/货损记录单"+damageType;
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		hm.put("damageType", damageType);
		hm.put("unit", unit);
        fileName = getContextPath() + fileName;
        outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,
				hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	public void printArapMiscCost() {
		String order_no = getPara("order_no").trim();
		String fileName = "report/arap_misc_cost.jasper";
		String outFileName = "download/手工成本单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
        fileName = getContextPath() + fileName;
        outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,
				hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	public void printCustomerOrder() {
		String order_no = getPara("order_no").trim();
		String fileName = "report/customer_checkOrder.jasper";
		String outFileName = "download/客户对账单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
        fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,
				hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	public String pritCheckOrderByPay(String order_no,long application_id) {
		String fileName = "report/checkOrder.jasper";
		String outFileName = "download/供应商对账单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		hm.put("application_id", application_id);
        fileName = getContextPath() + fileName;
        outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName,
				outFileName, hm);
		return file;
	}
	
	public void printReimburse() {
		String order_no = getPara("order_no");
		String fileName = "report/reimburse.jasper";
		String outFileName = "download/报销单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
        fileName = getContextPath() + fileName;
        outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName,
				outFileName, hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	public void printPayMent() {
		String order_no = getPara("order_no");
		ArapCostInvoiceApplication arapAuditInvoiceApplication = ArapCostInvoiceApplication.dao
				.findFirst(
						"select * from arap_cost_invoice_application_order where order_no = ?",
						order_no);
		
		String checkOrderFile = "";
		StringBuffer buffer = new StringBuffer();
		
	
		List<CostApplicationOrderRel> list1 = CostApplicationOrderRel.dao
				.find("select * from cost_application_order_rel where application_order_id = ?",
						arapAuditInvoiceApplication.get("id"));
		for (CostApplicationOrderRel costapplication : list1) {
			if ("对账单".equals(costapplication.get("order_type"))) {
				ArapCostOrder order = ArapCostOrder.dao
						.findFirst("select * FROM arap_cost_order where id ="+ costapplication.get("cost_order_id") );
				checkOrderFile = pritCheckOrderByPay(order.getStr("order_no"),arapAuditInvoiceApplication.getLong("id"));
				buffer.append(checkOrderFile.substring(checkOrderFile.indexOf("download")-1));
				buffer.append(",");
			}
			//TODO 预付单
		}
		
		
		String fileName = "report/payment.jasper";
		String outFileName ="download/付款申请单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
        fileName = getContextPath() + fileName;
        outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName,
				outFileName, hm);
		buffer.append(file.substring(file.indexOf("download")-1));
		buffer.append(",");
		renderText(buffer.toString());
	}
	public void printSign() {
		String type = getPara("sign");
		String order_no = getPara("order_no");
		String muban = type + ".jasper";
		String fileName = getContextPath()+"report/" + muban;
		String outFileName = getContextPath()+"download/";
		String path=getContextPath();
		int n=1;
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		boolean is_one = muban.contains("_one");
		TransferOrder to = TransferOrder.dao
				.findFirst(
						"select id,cargo_nature_detail from transfer_order where order_no = ?",
						order_no);
		List<TransferOrderItemDetail> list = TransferOrderItemDetail.dao
				.find("select id,serial_no from transfer_order_item_detail where order_id =?",
						to.get("id"));
		if(!is_one){
			if(list.size() < 10){
				if (type.contains("guoguang")) {
					outFileName += "国光标准单";
				} else if (type.contains("nonghang")) {
					outFileName += "农行";
				} else if (type.contains("china_post")) {
					outFileName += "中国邮政";
				} else {
					 outFileName += "中国邮储";
				}	
			}else{
				if (type.contains("guoguang")) {
					outFileName += order_no+"/国光标准单";
				} else if (type.contains("nonghang")) {
					outFileName +=order_no+"/农行";
				} else if (type.contains("china_post")) {
					outFileName +=order_no+"/中国邮政";
				} else {
					 outFileName +=order_no+"/中国邮储";
				}
			}
		}else{
			if (type.contains("guoguang")) {
				outFileName += "国光标准单";
			} else if (type.contains("nonghang")) {
				outFileName += "农行";
			} else if (type.contains("china_post")) {
				outFileName += "中国邮政";
			} else {
				 outFileName += "中国邮储";
			}
		}
		if (list.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < list.size(); i++) {
				if (is_one) {
					hm.put("id", list.get(i).get("id"));
					
					String file = PrintPatterns.getInstance().print(fileName,
							outFileName, hm);

					buffer.append(file.substring(file.indexOf("download")-1));
					buffer.append(",");
					break;
				} else {
					hm.put("id", list.get(i).get("id"));
					String file=null;
					if(list.size() < 10){
						file = PrintPatterns.getInstance().print(fileName,
								outFileName, hm);
						buffer.append(file.substring(file.indexOf("download")-1));
						buffer.append(",");
					}else{
						file = PrintPatterns.getInstance().prints(fileName,n, path,order_no,
								outFileName, hm);
						if(n==1){
							buffer.append("compress");
						}
						n++;//累加判断是不是第一个进去
					}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}

			renderText(buffer.toString());
		} else {
			if ("cargoNatureDetailNo".equals(to.get("cargo_nature_detail"))) {
				TransferOrderItem toi = TransferOrderItem.dao
						.findFirst("select * from transfer_order_item where order_id = "
								+ to.get("id"));
				// hm.put("amount", toi.get("amount"));
			}
			
			String file = PrintPatterns.getInstance().print(fileName,
					outFileName, hm);
			renderText(file.substring(file.indexOf("download")-1));
		}

	}
	public void printZJSign() {
		String type = getPara("sign");
		String item_type= "";
		String signType = getPara("zjSignType");
		String path=getContextPath();
		int n=1;
		if("DTJ".equals(signType)){
			item_type="大堂机";
		}else{
			item_type="穿墙机";
		}
		String order_no = getPara("order_no");
		String muban = type + ".jasper";
		String fileName = getContextPath()+"report/" + muban;
		String outFileName = getContextPath()+"download/";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		hm.put("item_type", item_type);
		boolean is_one = muban.contains("_one");
		TransferOrder to = TransferOrder.dao
				.findFirst(
						"select id,cargo_nature_detail from transfer_order where order_no = ?",
						order_no);
		List<TransferOrderItemDetail> list = TransferOrderItemDetail.dao
				.find("select id,serial_no from transfer_order_item_detail where order_id =?",
						to.get("id"));
		if(!is_one){
			if(list.size() < 10){
				outFileName += "签收单";	
			}else{
				outFileName +=order_no+"/签收单";
			}
		}else{
			outFileName += "签收单";
		}
		if (list.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < list.size(); i++) {
				if (is_one) {
					hm.put("id", list.get(i).get("id"));
					String file = PrintPatterns.getInstance().print(fileName,
							outFileName, hm);
					buffer.append(file.substring(file.indexOf("download")-1));
					buffer.append(",");
					break;
				} else {
					hm.put("id", list.get(i).get("id"));
					String file=null;
					if(list.size() < 10){
						file = PrintPatterns.getInstance().print(fileName,
								outFileName, hm);
						buffer.append(file.substring(file.indexOf("download")-1));
						buffer.append(",");
					}else{
						file = PrintPatterns.getInstance().prints(fileName,n, path,order_no,
								outFileName, hm);
						if(n==1){
							buffer.append("compress");
						}
						n++;//累加判断是不是第一个进去
					}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			renderText(buffer.toString());
		} else {
			if ("cargoNatureDetailNo".equals(to.get("cargo_nature_detail"))) {
				TransferOrderItem toi = TransferOrderItem.dao
						.findFirst("select * from transfer_order_item where order_id = "
								+ to.get("id"));
				// hm.put("amount", toi.get("amount"));
			}
			
			String file = PrintPatterns.getInstance().print(fileName,
					outFileName, hm);
			renderText(file.substring(file.indexOf("download")-1));
		}

	}
	public void printSignCargo() {
		String type = getPara("sign");
		String order_no = getPara("order_no");
		String muban = type + ".jasper";

		String fileName = "report/" + muban;
		String outFileName = "download/普通签收单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_no;
		
		StringBuffer buffer = new StringBuffer();
		String file = PrintPatterns.getInstance().print(fileName, outFileName,
				hm);
		buffer.append(file.substring(file.indexOf("download")-1));
		buffer.append(",");
		renderText(buffer.toString());
	}
	public void ZipOutput() throws IOException {
		String path=getContextPath()+"download/";;
		String order_no=getPara("order_no");
		 // 要被压缩的文件夹  
        File file = new File(path+order_no);  
        File zipFile = new File(path+order_no + ".zip");   
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(  
        		zipFile));  
        zipOut.setEncoding("GBK"); 
        if(file.isDirectory()){  
           InputStream input = null; 
           File[] files = file.listFiles();  
            for(int i = 0; i < files.length; ++i){  
                input = new FileInputStream(files[i]);  
                zipOut.putNextEntry(new ZipEntry(file.getName()  
                        + File.separator + files[i].getName()));  
                int temp = 0;  
                while((temp = input.read()) != -1){  
                    zipOut.write(temp);  
                }  
                input.close();  
            }  
        	System.out.println("file");
        }  
        StringBuffer buffer = new StringBuffer();
        String strFile=zipFile.getPath();
        buffer.append(strFile.substring(strFile.indexOf("download")-1));
		buffer.append(",");
        zipOut.close(); 
        renderText(buffer.toString());
	    }
		
}
