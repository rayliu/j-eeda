package controllers.yh.order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.OrderNoUtil;
public class TransferOrderExeclHandeln extends TransferOrderController{
	
	/**
     * 校验execl标题是否与默认标题一致
     * @param title,execlType
     * @return 
     */
    public boolean checkoutExeclTitle(String[] title,String execlType){
    	int num = 0;
    	List<Record> titleList = Db.find("select execl_title from execl_title where execl_type = '"+ execlType +"';");
    	if(titleList != null){
    		System.out.println();
    		for (Record record : titleList) {
				if(record.get("execl_title").equals(title[num])){
					num++;
				}
			}
    	}
    	if(num >= titleList.size()){
    		return true;
    	}else{
    		return false;
    	}
    }
    /**
     * 检验数据
     * @param content
     * @return 
     */
    public Map<String,String> validatingData(List<Map<String,String>> content){
    	Map<String, String> importResult = new HashMap<String, String>();
		int causeRow = 0;
    	String title = "";
		String because = "数据不能为空";
    	for (int j = 0; j < content.size(); j++) {
    		String customerOrderNo = content.get(j).get("运输单号").trim();
    		causeRow = j+2;
    		System.out.println("数据验证至第【"+causeRow+"】行");
    		if("".equals(customerOrderNo)){
    			title = "运输单号";
    			break;
    		}
    		String planning = content.get(j).get("计划日期");
    		if("".equals(planning)){
    			title = "计划日期";
    			break;
    		}
    		String arrivl = content.get(j).get("预计到货日期");
    		if("".equals(arrivl)){
    			title = "预计到货日期";
    			break;
    		}
    		String operationType = content.get(j).get("运营方式");
    		if("".equals(operationType)){
    			title = "运营方式";
    			break;
    		}
    		String arrivalMode = content.get(j).get("到达方式");
    		if("".equals(arrivalMode)){
    			title = "到达方式";
    			break;
    		}
    		because = "数据有误";
    		//货品型号
    		Record product = null;
    		if(!"".equals(content.get(j).get("货品型号"))){
        		product = Db.findFirst("select * from product where item_no = '"+content.get(j).get("货品型号")+"';");
    			if(product == null){
    				title = "货品型号";
    				break;
    			}
    		}else{
    			title = "货品型号";
    			because = "数据不能为空";
    			break;
    		}
			//始发城市
    		Record location1 = null;
    		if(!"".equals(content.get(j).get("始发城市"))){
    			location1 = Db.findFirst("select code from location where name = '" +content.get(j).get("始发城市") +"';");
    			if(location1 == null){
    				title = "始发城市";
    				break;
    			}
    		}else{
    			title = "始发城市";
    			because = "数据不能为空";
				break;
    		}
			//目的地城市
    		Record location2 = null;
    		if(!"".equals(content.get(j).get("到达城市"))){
    			location2 = Db.findFirst("select code from location where name = '" +content.get(j).get("到达城市") +"';");
    			if(location2 == null){
    				title = "到达城市";
    				break;
    			}
    		}else{
    			title = "到达城市";
				because = "数据不能为空";
				break;
    		}
			//客户名称
    		Record customer = null;
    		if(!"".equals(content.get(j).get("客户名称(简称)"))){
    			customer = Db.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_CUSTOMER+ "' and c.abbr ='" + content.get(j).get("客户名称(简称)") + "';");
    			if(customer == null){
    				title = "客户名称(简称)";
    				break;
    			}
    		}else{
    			title = "客户名称";
    			because = "数据不能为空";
    			break;
    		}
    		//网点
    		Record office = null;
    		if(!"".equals(content.get(j).get("网点"))){
    			office = Db.findFirst("select id from office where office_name = '" + content.get(j).get("网点") + "';");
    			if(office == null){
    				title = "网点";
    				break;
    			}
    		}else{
    			title = "网点";
    			because = "数据不能为空";
    			break;
    		}
			//供应商名称
    		Record provider = null;
    		if(!"".equals(content.get(j).get("供应商名称(简称)"))){
    			provider = Db.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_SERVICE_PROVIDER+ "' and c.abbr ='" + content.get(j).get("供应商名称(简称)") + "';");
    			if(provider == null){
    				title = "供应商名称(简称)";
    				break;
    			}
    		}
    		//仓库
    		Record warehouse = null;
    		if(!"".equals(content.get(j).get("中转仓"))){
    			warehouse = Db.findFirst("select id from warehouse where warehouse_name = '" + content.get(j).get("中转仓") + "';");
    			if(warehouse == null){
    				title = "中转仓";
    				break;
    			}
    		}
			
    	}
    	if("".equals(title)){
    		importResult.put("result","true");
			importResult.put("cause", "验证数据成功");
    	}else{ 
    		importResult.put("result","false");
    		if("运输单号".equals(title)){
    			importResult.put("cause", "验证数据至第" + (causeRow-1) + "行,因第" + causeRow + "行【" + title + "】列,请为同一张运输单的运输单号做上标识（如：运输单001，运输单002等）！");
        	}else{
        		importResult.put("cause", "验证数据至第" + (causeRow-1) + "行,因第" + causeRow + "行【" + title + "】列" + because);
        	}
    	}
    	return importResult;
    }
    /**
     * 验证是否重复导入同一execl文件:同一个客户和运输单号
     * @param content
     * @return false:不是同一文件 true:重复导入
     */
    public Map<String, String> verifyDuplicate(List<Map<String,String>> content){
    	Map<String, String> importResult = new HashMap<String, String>();
    	int causeRow = 0;
		for (int j = 0; j < content.size(); j++) {
			String sql = "select p.id from transfer_order tor left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
					+ " where p.party_type ='" + Party.PARTY_TYPE_CUSTOMER+ "' "
					+ " and c.abbr ='" + content.get(j).get("客户名称(简称)") + "'"
					+ " and tor.customer_order_no = '" + content.get(j).get("运输单号").trim() + "' "
					+ " and tor.planning_time = '" + content.get(j).get("计划日期") + "' and tor.arrival_time ='" + content.get(j).get("预计到货日期") + "';";
			Record tansferOrder = Db.findFirst(sql);
			if(tansferOrder != null){
				++causeRow;
			}
		}
    	if(causeRow == content.size()){
    		importResult.put("result","true");
			importResult.put("cause", "不能导入重复的execl文件！");
    	}else{
    		importResult.put("result","false");
    		importResult.put("cause", "可进行导入的execl文件！");
    	}
    	return importResult;
    }
    /**
     * 保存运输单
     * @param content
     * @return 
     */
    public TransferOrder saveTransferOrder(Map<String,String> content,Warehouse warehouse,Location location1,Location location2,Party customer,Party provider,Office office){
    	TransferOrder transferOrder = new TransferOrder();
    	try{
	    	String name = (String) currentUser.getPrincipal();
			UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
	    	SimpleDateFormat dbDataFormat = new SimpleDateFormat("yyyy-MM-dd");
	    	String sql = "select * from transfer_order order by id desc limit 0,1";
			String orderNo = OrderNoUtil.getOrderNo(sql, "YS");
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
			.set("customer_order_no", content.get("运输单号").trim());//客户订单号
			
			//运营方式
			if("外包".equals(content.get("运营方式"))){
				transferOrder.set("operation_type", "out_source");
			}else{
				transferOrder.set("operation_type", "own");
			}
			//取货地址、收货单位
			transferOrder.set("address", content.get("始发城市"))
			.set("receiving_unit", content.get("收货单位"));
			
			//到达方式
			if("入中转仓".equals(content.get("到达方式"))){
				//入中转仓
				transferOrder.set("arrival_mode", "gateIn")
				.set("warehouse_id", warehouse.get("id"));
			}else{
				//货品直送
				transferOrder.set("arrival_mode", "delivery");
				// 保存联系人
				Contact contact = new Contact();
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
				transferOrder.set("notify_party_id", party.get("id"));
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
    	} catch (ParseException e) {
			e.printStackTrace();
		}
    	return transferOrder;
    }
    /**
     * 保存运输单货品
     * @param content
     * @return 
     */
    public TransferOrderItem saveTransferOrderItem(double itemNumber,TransferOrder tansferOrder,Product product){
		//体积相加
		double size = 0;
		if(product.get("size") != null && !"".equals(product.get("size"))){
			size = product.getDouble("size")/1000;
		}
		double width = 0;
		if(product.get("width") != null && !"".equals(product.get("width"))){
			size = product.getDouble("width")/1000;
		}
		double height = 0;
		if(product.get("height") != null && !"".equals(product.get("height"))){
			size = product.getDouble("height")/1000;
		}
		double weight = 0;
		if(product.get("weight") != null && !"".equals(product.get("weight"))){
			weight = product.getDouble("weight");
		}
		//总体积
		double sumVolume = 0;
		if(product.get("volume") != null && !"".equals(product.get("volume"))){
			sumVolume = product.getDouble("volume") * itemNumber;
		}else{
			sumVolume = size * width * height * itemNumber;
		}
		//总重量
		double num = weight * itemNumber;
		//创建保存货品明细
		TransferOrderItem item =new TransferOrderItem();
		item.set("order_id", tansferOrder.get("id"))
		.set("amount", itemNumber)
		.set("item_no", product.get("item_no"))
		.set("item_name", product.get("item_name"))
		.set("size", product.get("size"))
		.set("unit", product.get("unit"))
		.set("width", product.get("width"))
		.set("height", product.get("height"))
		.set("volume", sumVolume)
		.set("weight", product.get("weight"))
		.set("sum_weight", num)
		.set("product_id", product.get("id"))
		.save();
		
		return item;
    }
    /**
     * 保存运输单单品
     * @param content
     * @return 
     */
    public void saveTransferOrderItemDetail(Map<String,String> content,TransferOrder tansferOrder,TransferOrderItem tansferOrderItem,Product product){
    	TransferOrderItemDetail itemDatail = new TransferOrderItemDetail();
		itemDatail.set("order_id", tansferOrder.get("id"))
		.set("item_id", tansferOrderItem.get("id"))
		.set("item_no", product.get("item_no"))
		.set("serial_no", content.get("单品序列号"))
		.set("item_name", product.get("item_name"))
		.set("volume", product.get("volume"))
		.set("weight", product.get("weight"))
		.set("notify_party_company", content.get("单品收货地址"))//收货地址
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
    public void saveTransferOrderMilestone(TransferOrder transferOrder){
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
    public Map<String,String> importTransferOrder(List<Map<String,String>> content){
    	Map<String, String> importResult = new HashMap<String, String>();
		importResult = validatingData(content);
		if("true".equals(importResult.get("result"))){
			importResult = verifyDuplicate(content);
			if("false".equals(importResult.get("result"))){
				int resultNum = 0;
		    	int causeRow = 0;
				try {
					for (int j = 0; j < content.size(); j++) {
						causeRow = j+2;
						System.out.println("导入至第【"+causeRow+"】行");
		        		//货品型号
		        		Product product = Product.dao.findFirst("select * from product where item_no = '"+content.get(j).get("货品型号")+"';");
		    			//仓库
		        		Warehouse warehouse = Warehouse.dao.findFirst("select id from warehouse where warehouse_name = '" + content.get(j).get("中转仓") + "';");
		    			//始发城市
		        		Location location1 = Location.dao.findFirst("select code from location where name = '" +content.get(j).get("始发城市") +"';");
		    			//目的地城市
		        		Location location2 = Location.dao.findFirst("select code from location where name = '" +content.get(j).get("到达城市") +"';");
		    			//客户名称
		        		Party customer = Party.dao.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_CUSTOMER+ "' and c.abbr ='" + content.get(j).get("客户名称(简称)") + "';");
		    			//供应商名称
		        		Party provider = Party.dao.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='" + Party.PARTY_TYPE_SERVICE_PROVIDER+ "' and c.abbr ='" + content.get(j).get("供应商名称(简称)") + "';");
		    			//网点
		        		Office office = Office.dao.findFirst("select id from office where office_name = '" + content.get(j).get("网点") + "';");
		        		//运输单号
		        		String customerOrderNo = content.get(j).get("运输单号").trim();
		        		//发货数量
		        		double itemNumber = 0;
		    			if(!"".equals(content.get(j).get("发货数量"))){
		    				itemNumber = Double.parseDouble(content.get(j).get("发货数量"));
		    			}
		    			String sql = "select * from transfer_order where customer_id = '" + customer.get("pid") + "' and customer_order_no = '" + customerOrderNo + "'"
		    					+ " and planning_time = '" + content.get(j).get("计划日期") + "' and arrival_time ='" + content.get(j).get("预计到货日期") + "';";
		        		TransferOrder order = TransferOrder.dao.findFirst(sql);
		    			if(order != null){
		    				TransferOrderItem tansferOrderItem= TransferOrderItem.dao.findFirst("select * from transfer_order_item where order_id = '" + order.get("id") +"' and item_no = '" + product.get("item_no") + "';");
		    				if(tansferOrderItem != null){
		    					//本来这里是要修改货品明细表中amount（数量），
		    					//由于execl中“发货数量”列是货品总数，所以不用修改
		    					if("cargoNatureDetailYes".equals(order.get("cargo_nature_detail"))){
		    						//创建单品货品明细
		    						saveTransferOrderItemDetail(content.get(j),order,tansferOrderItem,product);
		    					}
		    				}else{
		    					//创建保存货品明细
			    				TransferOrderItem item = saveTransferOrderItem(itemNumber,order,product);
								//创建保存单品货品明细
								if("cargoNatureDetailYes".equals(order.get("cargo_nature_detail"))){
									//创建单品货品明细
									saveTransferOrderItemDetail(content.get(j),order,item,product);
								}
		    				}
		    			}else{
		    				//生成运输单数量
		    				++resultNum;
		    				//创建保存运输单
		    				TransferOrder transferOrder = saveTransferOrder(content.get(j), warehouse, location1, location2, customer, provider, office);
		    				//保存运输里程碑
		    				saveTransferOrderMilestone(transferOrder);
		    				//创建保存货品明细
		    				TransferOrderItem item = saveTransferOrderItem(itemNumber,transferOrder,product);
							//创建保存单品货品明细
							if("cargoNatureDetailYes".equals(transferOrder.get("cargo_nature_detail"))){
								//创建单品货品明细
								saveTransferOrderItemDetail(content.get(j),transferOrder,item,product);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("未知错误！");
					importResult.put("result","true");
					importResult.put("cause", "未知错误，已成功导入至" + (causeRow-1) + "行！");
					return importResult;
				}
				importResult.put("result","true");
	        	importResult.put("cause", "成功导入" + resultNum + "张运输单");
			}
		}
    	return importResult;  
    }
	
}
