package controllers.yh.report;

import java.util.HashMap;
import java.util.List;

import models.ArapCostInvoiceApplication;
import models.ArapCostItem;
import models.ArapCostOrder;
import models.CostApplicationOrderRel;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;

import org.apache.log4j.Logger;

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
		String fileName = getContextPath() +"report/checkOrder.jasper";
		String outFileName = getContextPath() +"download/供应商对账单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		if(costapplicationorderrel!=null){
			hm.put("application_id", costapplicationorderrel.get("application_order_id"));
		}
		hm.put("order_no", order_no);
		String file = PrintPatterns.getInstance().print(fileName, outFileName,
				hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	public void printManualOrder() {
		String order_no = getPara("order_no").trim();
		String fileName = getContextPath() +"report/arap_manual.jasper";
		String outFileName = getContextPath() +"download/手工收入单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		String file = PrintPatterns.getInstance().print(fileName, outFileName,
				hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	public void printArapMiscCost() {
		String order_no = getPara("order_no").trim();
		String fileName = getContextPath() +"report/arap_misc_cost.jasper";
		String outFileName = getContextPath() +"download/手工成本单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		String file = PrintPatterns.getInstance().print(fileName, outFileName,
				hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	public void printCustomerOrder() {
		String order_no = getPara("order_no").trim();
		String fileName = getContextPath() +"report/customer_checkOrder.jasper";
		String outFileName = getContextPath() +"download/客户对账单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,
				hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	public String pritCheckOrderByPay(String order_no,long application_id) {
		String fileName = getContextPath() +"report/checkOrder.jasper";
		String outFileName = getContextPath() +"download/供应商对账单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		hm.put("application_id", application_id);
		
		String file = PrintPatterns.getInstance().print(fileName,
				outFileName, hm);
		return file;
	}
	
	public void printReimburse() {
		String order_no = getPara("order_no");
		String fileName = getContextPath() +"report/reimburse.jasper";
		String outFileName = getContextPath() +"download/报销单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);

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
		
		
		String fileName = getContextPath() +"report/payment.jasper";
		String outFileName =getContextPath() +"download/付款申请单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		
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
		if (type.contains("guoguang")) {
			outFileName += "国光标准单";
		} else if (type.contains("nonghang")) {
			outFileName += "农行";
		} else if (type.contains("china_post")) {
			outFileName += "中国邮政";
		} else {
			outFileName += "中国邮储";
		}

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
					
					String file = PrintPatterns.getInstance().print(fileName,
							outFileName, hm);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					buffer.append(file.substring(file.indexOf("download")-1));
					buffer.append(",");
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
		if("DTJ".equals(signType)){
			item_type="大堂机";
		}else{
			item_type="穿墙机";
		}
		String order_no = getPara("order_no");
		String muban = type + ".jasper";
		String fileName = getContextPath()+"report/" + muban;
		String outFileName = getContextPath()+"download/签收单";
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
					String file = PrintPatterns.getInstance().print(fileName,
							outFileName, hm);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					buffer.append(file.substring(file.indexOf("download")-1));
					buffer.append(",");
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

}
