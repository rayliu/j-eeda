package controllers.yh.order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Location;
import models.Office;
import models.Party;
import models.Product;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
import models.UserLogin;
import models.Warehouse;
import models.yh.profile.Contact;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.util.OrderNoGenerator;
public class TransferOrderExeclHandeln extends TransferOrderController{
	
	/**
     * 校验execl标题是否与默认标题一致
     * @param title,execlType
     * @return 
     */
	public boolean checkoutExeclTitle(String[] title,String execlType){
    	List<Record> titleRecList = Db.find("select execl_title from execl_title where execl_type = '"+ execlType +"';");
    	if(titleRecList != null){
    		//判断总数是否相等
    		if(titleRecList.size() != title.length){
    			return false;
    		}
    		//判断是否所有列标题一致
    		List<String> titleList = new ArrayList<String>(titleRecList.size());
    		for (Record record : titleRecList) {
    			titleList.add(record.getStr("execl_title"));
			}
    		for (int i = 0; i < title.length; i++) {
				String excelTitle = title[i];
				if(!titleList.contains(excelTitle)){
					return false;
				}
			}
    		
    	}
    	
        return true;
    	
    }
	
	/**
     * 检验数据：同一张单的数据是否有隔单（中间还存在其他单据数据）现象
     * @param content
     * @return 
     */
	private Map<String, String> validatingOrderNo(List<Map<String,String>> content){
		Map<String, String> importResult = new HashMap<String, String>();
		try {
			importResult.put("result","true");
			importResult.put("cause", "验证数据成功");
			List<String> orderNoList = new ArrayList<String>();
			Set<String> orderNoSet = new HashSet<String>();
			for (int j = 0; j < content.size(); j++) {
				orderNoList.add(content.get(j).get("客户订单号"));
				orderNoSet.add(content.get(j).get("客户订单号"));
			}
			Iterator orderNos = orderNoSet.iterator();//先迭代出来  
	        while(orderNos.hasNext()){//遍历  
	        	boolean flag = false; 
	        	String orderNo = orderNos.next().toString();
	        	int firstIndex = orderNoList.indexOf(orderNo);
	        	int lastIndex = orderNoList.lastIndexOf(orderNo);
	        	System.out.println("单号："+orderNo+",第一次出现位置："+firstIndex+",最后一次出现位置："+lastIndex);
	        	for (int i = firstIndex; i <= lastIndex; i++) {
					if(!orderNoList.get(i).equals(orderNo)){
						importResult.put("result","false");
						importResult.put("cause", "验证数据失败，同一张单号数据中:订单号不允许有断开存放的现象，而在第"+(lastIndex+2)+"行【客户订单号】列有和第"+(firstIndex+2)+"到第"+(i+1)+"行相同客户单号存在");
						flag = true; 
						break;
					}
				}
	        	if(flag){
	        		break;
        		} 
	        }  
		} catch (Exception e) {
			importResult.put("result","false");
			importResult.put("cause", "验证数据出错，请重新整理文件数据后在导入");
		}
		return importResult;
	}
	
    /**
     * 功能：
     * 	1.检验数据：
     * 		a.必填列
     * 		b.execl数据与系统数据需一致的列
     * 		c.execl数据有格式要求的列
     *	2.同一execl文件多次导入问题:同一个客户和运输单号
     * @param content
     * @return 
     */
	private Map<String,String> validatingData(List<Map<String,String>> content){
    	Map<String, String> importResult = new HashMap<String, String>();
		int causeRow = 0;
		int verifyDuplicateRow = 0;
    	String title = "";
		String because = "数据不能为空";
		SimpleDateFormat dbDataFormat = new SimpleDateFormat("yyyy-MM-dd");
    	for (int j = 0; j < content.size(); j++) {
    		causeRow = j+2;
    		System.out.println("数据验证至第【"+causeRow+"】行");
    		if("".equals(content.get(j).get("客户订单号").trim())){
    			title = "客户订单号";
    			break;
    		}else if("".equals(content.get(j).get("运营方式"))){
    			title = "运营方式";
    			break;
    		}else if("".equals(content.get(j).get("货品型号"))){
    			title = "货品型号";
    			break;
    		}else if("".equals(content.get(j).get("货品属性"))){
    			title = "货品属性";
    			break;
    		}else if("".equals(content.get(j).get("客户名称(简称)"))){
    			title = "客户名称(简称)";
    			break;
    		}else if("".equals(content.get(j).get("始发城市"))){
    			title = "始发城市";
    			break;
    		}else if("".equals(content.get(j).get("到达城市"))){
    			title = "到达城市";
    			break;
    		}else if("".equals(content.get(j).get("网点"))){
    			title = "网点";
    			break;
    		}
    		
    		String arrivalMode = content.get(j).get("到达方式");
    		if("".equals(arrivalMode)){
    			title = "到达方式";
    			break;
    		}else{
    			if("入中转仓".equals(arrivalMode)){
    				if("".equals(content.get(j).get("中转仓"))){
    					title = "中转仓";
    	    			break;
    				}
    			}
    		}
    		
    		try {
    			if("".equals(content.get(j).get("计划日期"))){
        			title = "计划日期";
        			break;
        		}else{
        			dbDataFormat.parse(content.get(j).get("计划日期"));
        		}
			} catch (ParseException e) {
				title = "计划日期";
				because = "数据有误";
				break;
			}
    		
    		try {
        		if("".equals(content.get(j).get("预计到货日期"))){
        			title = "预计到货日期";
        			break;
        		}else{
        			dbDataFormat.parse(content.get(j).get("预计到货日期"));
        		}
			} catch (ParseException e) {
				title = "预计到货日期";
				because = "数据有误";
				break;
			}
    		
    		because = "数据有误";
    		//客户名称
    		Party customer = Party.dao.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_CUSTOMER+ "' and c.abbr ='" + content.get(j).get("客户名称(简称)") + "';");
			if(customer == null){
				title = "客户名称(简称)";
				break;
			}
			//始发城市
			Location location1 = Location.dao.findFirst("select code from location where name = '" +content.get(j).get("始发城市") +"';");
			if(location1 == null){
				title = "始发城市";
				break;
			}
			//到达城市
			Location location2 = Location.dao.findFirst("select code from location where name = '" +content.get(j).get("到达城市") +"';");
			if(location2 == null){
				title = "到达城市";
				break;
			}
			
    		//网点
			Office office = Office.dao.findFirst("select id from office where office_name = '" + content.get(j).get("网点") + "';");
			if(office == null){
				title = "网点";
				break;
    		}
    		
			//供应商名称
			Party provider = null;
    		if(!"".equals(content.get(j).get("供应商名称(简称)"))){
    			provider = Party.dao.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_SERVICE_PROVIDER+ "' and c.abbr ='" + content.get(j).get("供应商名称(简称)") + "';");
    			if(provider == null){
    				title = "供应商名称(简称)";
    				break;
    			}
    		}
    		//中转仓
    		Warehouse warehouse = null;
    		if(!"".equals(content.get(j).get("中转仓"))){
    			warehouse = Warehouse.dao.findFirst("select id from warehouse where warehouse_name = '" + content.get(j).get("中转仓") + "';");
    			if(warehouse == null){
    				title = "中转仓";
    				break;
    			}
    		}
    		
    		//验证同一execl文件多次导入问题:同一个客户和运输单号
    		String sql = "select p.id from transfer_order tor left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
					+ " where p.party_type ='" + Party.PARTY_TYPE_CUSTOMER+ "' "
					+ " and c.abbr ='" + content.get(j).get("客户名称(简称)") + "'"
					+ " and tor.customer_order_no = '" + content.get(j).get("客户订单号").trim() + "' "
					+ " and tor.planning_time = '" + content.get(j).get("计划日期") + "' and tor.arrival_time ='" + content.get(j).get("预计到货日期") + "';";
			Record tansferOrder = Db.findFirst(sql);
			if(tansferOrder != null){
				++verifyDuplicateRow;
			}
    		
    	}
    	if(verifyDuplicateRow == content.size()){
    		importResult.put("result","false");
			importResult.put("cause", "不能多次导入同一excel文件！");
    	}else if(!"".equals(title)){
    		importResult.put("result","false");
    		if("客户订单号".equals(title)){
    			importResult.put("cause", "验证数据至第" + (causeRow-1) + "行,因第" + causeRow + "行【" + title + "】列,请为同一张运输单的客户订单号做上标识（如：运输单001，运输单002等）！");
        	}else{
        		importResult.put("cause", "验证数据至第" + (causeRow-1) + "行,因第" + causeRow + "行【" + title + "】列" + because);
        	}
    	}else{
    		importResult.put("result","true");
			importResult.put("cause", "验证数据成功");
    	}
    	return importResult;
    }
	
    /**
     * 保存运输单
     * @param content
     * @return 
     */
	private TransferOrder saveTransferOrder(Map<String,String> content,Warehouse warehouse,Location location1,Location location2,Party customer,Party provider,Office office) throws Exception{
    	TransferOrder transferOrder = new TransferOrder();
    	String name = (String) currentUser.getPrincipal();
		UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
    	SimpleDateFormat dbDataFormat = new SimpleDateFormat("yyyy-MM-dd");
		String orderNo = OrderNoGenerator.getNextOrderNo("YS");
		Date planningTime = dbDataFormat.parse(content.get("计划日期"));
		Date arrivalTime = dbDataFormat.parse(content.get("预计到货日期"));
		
		transferOrder.set("order_no", orderNo)
		.set("order_type", "salesOrder")//订单类型：默认为销售订单
		.set("charge_type", "perUnit")//客户计费方式：默认计件
		.set("charge_type2", "perUnit")//供应商计费方式：默认计件
		.set("pickup_assign_status",TransferOrder.ASSIGN_STATUS_NEW)
		.set("depart_assign_status",TransferOrder.ASSIGN_STATUS_NEW)
		.set("status", "新建")
		.set("create_by", user.get("id"))//创建人id
		.set("create_stamp", new Date())//创建时间
		.set("planning_time", planningTime)//计划时间
		.set("arrival_time", arrivalTime)//预计到货时间
		.set("customer_order_no", content.get("客户订单号").trim());//客户订单号
		
		//运营方式
		if("外包".equals(content.get("运营方式"))){
			transferOrder.set("operation_type", "out_source");
		}else{
			transferOrder.set("operation_type", "own");
		}
		//取货地址、收货信息
		transferOrder.set("address", content.get("始发城市"))
		.set("receiving_unit", content.get("收货单位"))
		.set("receiving_name", content.get("单品收货人"))
		.set("receiving_address", content.get("单品收货地址"))
		.set("receiving_phone", content.get("单品收货人联系电话"));
		
		//到达方式
		if("入中转仓".equals(content.get("到达方式"))){
			//入中转仓
			transferOrder.set("arrival_mode", "gateIn")
			.set("warehouse_id", warehouse.get("id"));
		}else{
			//货品直送
			transferOrder.set("arrival_mode", "delivery");
			// 保存联系人
			/*Contact contact = new Contact();
			contact.set("address", content.get("单品收货地址"));//收货地址
			contact.set("contact_person", content.get("单品收货人"));//收货人
			contact.set("phone", content.get("单品收货人联系电话"));//收货人电话
			contact.save();
			// 保存收货人
			Party party = new Party();
			party.set("contact_id", contact.getLong("id"));
			party.set("create_date", new Date());
			party.set("creator", currentUser.getPrincipal());
			party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
			party.save();
			//收货人
			transferOrder.set("notify_party_id", party.get("id"));*/
		}
		//始发城市
		transferOrder.set("route_from",location1.get("code"));
		//目的地城市
		transferOrder.set("route_to",location2.get("code"));
		//客户名称
		transferOrder.set("customer_id",customer.get("pid"));
		//供应商名称
		if(provider != null){
			transferOrder.set("sp_id", provider.get("pid"));
		}
		//网点
		if(office != null){
			transferOrder.set("office_id", office.get("id"));
		}
		//货品属性
		if(content.get("货品属性").equals("ATM")){
			transferOrder.set("cargo_nature","ATM");
			transferOrder.set("cargo_nature_detail","cargoNatureDetailYes");
		}else{
			transferOrder.set("cargo_nature","cargo");
			if(!"".equals(content.get("单品序列号")) || !"".equals(content.get("单品件数"))){
				transferOrder.set("cargo_nature_detail","cargoNatureDetailYes");
			}else{
				transferOrder.set("cargo_nature_detail","cargoNatureDetailNo");
			}
		}
		transferOrder.set("remark","这是导入的数据");
		transferOrder.save();
    	return transferOrder;
    }
    /**
     * 保存运输单货品
     * 注：       1.运输单有单品时叠加计算货品数量，
     * 		2.运输单没单品直接读取文件，此时“发货数量”列是货品总数，不用修改
     * @param content
     * @return 
     */
	private TransferOrderItem updateTransferOrderItem(Map<String,String> content,double itemNumber,TransferOrder tansferOrder,TransferOrderItem transferOrderItem,Product product) throws Exception{
		String unit="";
		double size = 0;
		double width = 0;
		double height = 0;
		double weight = 0;
		//总体积、总重量
		double sumVolume = 0;
		double sumWeight = 0;
		if(product != null){
			if(product.get("unit") != null && !"".equals(product.get("unit"))){
				unit = product.get("unit");
			}else{
				unit = content.get("单位");
			}
			if(product.get("size") != null && !"".equals(product.get("size"))){
				size = product.getDouble("size")/1000;
			}
			if(product.get("width") != null && !"".equals(product.get("width"))){
				width = product.getDouble("width")/1000;
			}
			if(product.get("height") != null && !"".equals(product.get("height"))){
				height = product.getDouble("height")/1000;
			}
			if(product.get("weight") != null && !"".equals(product.get("weight"))){
				weight = product.getDouble("weight");
			}
			if("cargoNatureDetailYes".equals(tansferOrder.get("cargo_nature_detail"))){
				sumVolume = size * width * height;
				sumWeight = weight;
			}else{
				if(product.get("volume") != null && !"".equals(product.get("volume"))){
					sumVolume = product.getDouble("volume") * itemNumber;
				}else{
					sumVolume = size * width * height * itemNumber;
				}
				sumWeight = weight * itemNumber;
			}
			//保留两位小数
			BigDecimal volumeBig = new BigDecimal(sumVolume);
			sumVolume = volumeBig.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    	BigDecimal weightBig = new BigDecimal(sumWeight);
	    	sumWeight = weightBig.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		
    	if(!"".equals(transferOrderItem.get("id")) && transferOrderItem.get("id") != null){
			//有单品，货品数量叠加计数，重新计算总体积、总重量
			if("cargoNatureDetailYes".equals(tansferOrder.get("cargo_nature_detail"))){
				transferOrderItem.set("amount", transferOrderItem.getDouble("amount") + 1)
				.set("volume", transferOrderItem.getDouble("volume") + sumVolume)
				.set("sum_weight", transferOrderItem.getDouble("sum_weight") + sumWeight)
				.update();
    		}
    	}else{
    		if("cargoNatureDetailYes".equals(tansferOrder.get("cargo_nature_detail"))){
    			//有单品时叠加计算货品数量,默认数量为1
    			transferOrderItem.set("amount", 1);
    		}else{
    			//没单品直接读取“发货数量”为货品总数，不用修改
    			transferOrderItem.set("amount", itemNumber);
    		}
    		if(product == null){
    			transferOrderItem.set("item_no", content.get("货品型号"));
    		}else{
    			//创建保存货品明细
    			transferOrderItem.set("item_no", product.get("item_no"))
    			.set("item_name", product.get("item_name"))
    			.set("size", product.get("size"))
    			.set("unit", unit)
    			.set("width", product.get("width"))
    			.set("height", product.get("height"))
    			.set("weight", product.get("weight"))
    			.set("product_id", product.get("id"));
    		}
    		transferOrderItem.set("order_id", tansferOrder.get("id")).set("volume", sumVolume).set("sum_weight", sumWeight).save();
    	}
		return transferOrderItem;
    }
    /**
     * 保存运输单单品
     * @param content
     * @return 
     */
	private void saveTransferOrderItemDetail(Map<String,String> content,TransferOrder tansferOrder,TransferOrderItem tansferOrderItem,Product product) throws Exception{
    	TransferOrderItemDetail itemDatail = new TransferOrderItemDetail();
    	if(product != null){
    		itemDatail.set("order_id", tansferOrder.get("id"))
    		.set("item_id", tansferOrderItem.get("id"))
    		.set("item_no", product.get("item_no"))
    		.set("serial_no", content.get("单品序列号"))
    		.set("item_name", product.get("item_name"))
    		.set("volume", product.get("volume"))
    		.set("weight", product.get("weight"));
    	}else{
    		itemDatail.set("order_id", tansferOrder.get("id"))
    		.set("item_id", tansferOrderItem.get("id"))
    		.set("item_no", content.get("货品型号"))
    		.set("serial_no", content.get("单品序列号"));
    	}
    	itemDatail.set("notify_party_company", content.get("单品收货地址"))//收货地址
    	.set("receive_address", content.get("收货网点"))//收货网点
		.set("notify_party_name", content.get("单品收货人"))//收货人
		.set("notify_party_phone", content.get("单品收货人联系电话"))//收货人电话
		.set("sales_order_no", content.get("单品销售单号"))//销售单号
		.set("responsible_person", content.get("责任人"))//责任人
		.set("business_manager", content.get("业务经理"))//业务经理
		.set("station_name", content.get("服务站名称"))//服务站名称
		.set("service_telephone", content.get("服务站电话"));//服务站电话
		if(!"".equals(content.get("单品件数"))){
			itemDatail.set("pieces", content.get("单品件数"));
		}
		itemDatail.save();
    }
    /**
     * 保存运输里程碑
     * @param content
     * @return 
     */
	private void saveTransferOrderMilestone(TransferOrder transferOrder) throws Exception{
    	String name = (String) currentUser.getPrincipal();
		UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
    	TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
		transferOrderMilestone.set("status", "新建");
		transferOrderMilestone.set("create_by", user.get("id"));
		transferOrderMilestone.set("location", "");
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		transferOrderMilestone.set("create_stamp", sqlDate);
		transferOrderMilestone.set("type",
				TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
		transferOrderMilestone.set("order_id", transferOrder.get("id"));
		transferOrderMilestone.save();
    }
	
    /**
     * 导入数据
     * @param content
     * @return 导入结果
     */
	@Before(Tx.class)
    public Map<String,String> importTransferOrder(List<Map<String,String>> content){
		Connection conn=null;
		
    	Map<String, String> importResult = new HashMap<String, String>();
    	importResult = validatingOrderNo(content);
    	if("true".equals(importResult.get("result"))){
			importResult = validatingData(content);
			if("true".equals(importResult.get("result"))){
				int resultNum = 0;
		    	int causeRow = 0;
		    	//回滚运输单信息
		    	List<TransferOrder> orderList = new ArrayList<TransferOrder>();
				try {
					//手动控制提交
					conn=DbKit.getConfig().getDataSource().getConnection();
		            DbKit.getConfig().setThreadLocalConnection(conn);
		            conn.setAutoCommit(false);//自动提交变成false
					
					for (int j = 0; j < content.size(); j++) {
						causeRow = j+2;
						System.out.println("导入至第【"+causeRow+"】行");
						//客户名称
		        		Party customer = Party.dao.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_CUSTOMER+ "' and c.abbr ='" + content.get(j).get("客户名称(简称)") + "';");
		        		//货品型号
		        		Product product = Product.dao.findFirst("select p.* from product p left join category c on c.id = p.category_id where c.customer_id = '" + customer.get("pid") + "' and item_no =  '"+content.get(j).get("货品型号")+"';");
		    			//仓库
		        		Warehouse warehouse = Warehouse.dao.findFirst("select id from warehouse where warehouse_name = '" + content.get(j).get("中转仓") + "';");
		    			//始发城市
		        		Location location1 = Location.dao.findFirst("select code from location where name = '" +content.get(j).get("始发城市") +"';");
		    			//目的地城市
		        		Location location2 = Location.dao.findFirst("select code from location where name = '" +content.get(j).get("到达城市") +"';");
		    			//供应商名称
		        		Party provider = Party.dao.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_SERVICE_PROVIDER+ "' and c.abbr ='" + content.get(j).get("供应商名称(简称)") + "';");
		    			//网点
		        		Office office = Office.dao.findFirst("select id from office where office_name = '" + content.get(j).get("网点") + "';");
		        		//客户订单号
		        		String customerOrderNo = content.get(j).get("客户订单号").trim();
		        		//发货数量
		        		double itemNumber = 0;
		    			if(!"".equals(content.get(j).get("发货数量"))){
		    				itemNumber = Double.parseDouble(content.get(j).get("发货数量"));
		    			}
		    			String sql = "select * from transfer_order where customer_id = '" + customer.get("pid") + "' and customer_order_no = '" + customerOrderNo + "'"
		    					+ " and planning_time = '" + content.get(j).get("计划日期") + "' and arrival_time ='" + content.get(j).get("预计到货日期") + "';";
		        		TransferOrder order = TransferOrder.dao.findFirst(sql);
		    			if(order != null){
		    				TransferOrderItem tansferOrderItem = null;
		    				if(content.get(j).get("货品属性")=="ATM"){
		    					product = Product.dao.findFirst("select p.* from product p left join category c on c.id = p.category_id where c.customer_id = '" + customer.get("pid") + "' and item_no =  '"+content.get(j).get("货品型号")+"';");
		    				}
		    				/*if(product==null){
		    					importResult.put("cause", "第"+causeRow+"行，货品属性信息有误");
		    					return importResult;
		    				}*/
		    				else{
		    					tansferOrderItem = TransferOrderItem.dao.findFirst("select * from transfer_order_item where order_id = '" + order.get("id") +"' and item_no = '" + product.get("item_no") + "';");
		    				}
		    				if(tansferOrderItem != null){
		    					//运输单有单品时叠加计算货品数量，没单品时直接读取文件，此时“发货数量”列是货品总数，不用修改
		    					tansferOrderItem = updateTransferOrderItem(content.get(j),itemNumber,order,tansferOrderItem,product);
		    				}else{
		    					//创建保存货品明细
		    					tansferOrderItem = updateTransferOrderItem(content.get(j),itemNumber,order,new TransferOrderItem(),product);
		    				}
		    				//创建保存单品货品明细
		    				if("cargoNatureDetailYes".equals(order.get("cargo_nature_detail"))){
	    						//创建单品货品明细
	    						saveTransferOrderItemDetail(content.get(j),order,tansferOrderItem,product);
	    					} 
		    			}else{
		    				//生成运输单数量
		    				++resultNum;
		    				//创建保存运输单
		    				TransferOrder transferOrder = saveTransferOrder(content.get(j), warehouse, location1, location2, customer, provider, office);
		    				//已生成的运输单据保存到回滚list中
		    				orderList.add(transferOrder);
		    				//保存运输里程碑
		    				saveTransferOrderMilestone(transferOrder);
		    				//创建保存货品明细
		    				TransferOrderItem item = updateTransferOrderItem(content.get(j),itemNumber,transferOrder,new TransferOrderItem(),product);
							//创建保存单品货品明细
							if("cargoNatureDetailYes".equals(transferOrder.get("cargo_nature_detail"))){
								//创建单品货品明细
								saveTransferOrderItemDetail(content.get(j),transferOrder,item,product);
							}
						}
					}
					conn.commit();
				} catch (Exception e) {
					
					System.out.println("导入操作异常！");
					e.printStackTrace();
					try {
						if(null!=conn) conn.rollback();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					
					importResult.put("result","false");
					importResult.put("cause", "导入失败，数据导入至第" + (causeRow) + "行时出现异常，<br/>导入数据已取消！");
											
					return importResult;
				}finally{
		            try
		            {
		                if(null!=conn){
		                    conn.close();
		                }
		            }
		            catch (Exception e2)
		            {
		                e2.printStackTrace();
		            }finally{
		                DbKit.getConfig().removeThreadLocalConnection();
		            }
		        }
				importResult.put("result","true");
	        	importResult.put("cause", "成功导入" + resultNum + "张运输单");
			}
    	}
    	return importResult;  
    }
	
}
