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
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.util.OrderNoGenerator;

public class TransferOrderExeclHandeln extends TransferOrderController {

	/**
	 * 校验execl标题是否与默认标题一致
	 * 
	 * @param title
	 *            ,execlType
	 * @return
	 */
	public boolean checkoutExeclTitle(String[] title, String execlType) {
		List<Record> titleRecList = Db
				.find("select execl_title from execl_title where execl_type = '"
						+ execlType + "';");
		if (titleRecList != null) {
			// 判断总数是否相等
			if (titleRecList.size() != title.length) {
				return false;
			}
			// 判断是否所有列标题一致
			List<String> titleList = new ArrayList<String>(titleRecList.size());
			for (Record record : titleRecList) {
				titleList.add(record.getStr("execl_title"));
			}
			for (int i = 0; i < title.length; i++) {
				String excelTitle = title[i];
				if (!titleList.contains(excelTitle)) {
					return false;
				}
			}

		}

		return true;

	}

	/**
	 * 检验数据：同一张单的数据是否有隔单（中间还存在其他单据数据）现象
	 * 
	 * @param content
	 * @return
	 */
	private Map<String, String> validatingOrderNo(
			List<Map<String, String>> content) {
		Map<String, String> importResult = new HashMap<String, String>();
		try {
			importResult.put("result", "true");
			importResult.put("cause", "验证数据成功");
			List<String> orderNoList = new ArrayList<String>();
			Set<String> orderNoSet = new HashSet<String>();
			for (int j = 0; j < content.size(); j++) {
				orderNoList.add(content.get(j).get("客户订单号"));
				orderNoSet.add(content.get(j).get("客户订单号"));
			}
			Iterator orderNos = orderNoSet.iterator();// 先迭代出来
			while (orderNos.hasNext()) {// 遍历
				boolean flag = false;
				String orderNo = orderNos.next().toString();
				int firstIndex = orderNoList.indexOf(orderNo);
				int lastIndex = orderNoList.lastIndexOf(orderNo);
				System.out.println("单号：" + orderNo + ",第一次出现位置：" + firstIndex
						+ ",最后一次出现位置：" + lastIndex);
				for (int i = firstIndex; i <= lastIndex; i++) {
					if (!orderNoList.get(i).equals(orderNo)) {
						importResult.put("result", "false");
						importResult.put("cause",
								"验证数据失败，同一张单号数据中:订单号不允许有断开存放的现象，而在第"
										+ (lastIndex + 2) + "行【客户订单号】列有和第"
										+ (firstIndex + 2) + "到第" + (i + 1)
										+ "行相同客户单号存在");
						flag = true;
						break;
					}
				}
				if (flag) {
					break;
				}
			}
		} catch (Exception e) {
			importResult.put("result", "false");
			importResult.put("cause", "验证数据出错，请重新整理文件数据后在导入");
		}
		return importResult;
	}

	/**
	 * 功能： 1.检验数据： a.必填列 b.execl数据与系统数据需一致的列 c.execl数据有格式要求的列
	 * 2.同一execl文件多次导入问题:同一个客户和运输单号
	 * 
	 * @param content
	 * @return
	 */
	private Map<String, String> validatingData(List<Map<String, String>> content) {
		Map<String, String> importResult = new HashMap<String, String>();
		int causeRow = 0;
		int verifyDuplicateRow = 0;
		String title = "";
		String because = "数据不能为空";
		SimpleDateFormat dbDataFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int j = 0; j < content.size(); j++) {
			causeRow = j + 2;
			System.out.println("数据验证至第【" + causeRow + "】行");
			if ("".equals(content.get(j).get("客户订单号").trim())) {
				title = "客户订单号";
				break;
			} else if ("".equals(content.get(j).get("运营方式"))) {
				title = "运营方式";
				break;
			} else if ("".equals(content.get(j).get("货品型号"))) {
				title = "货品型号";
				break;
			} else if ("".equals(content.get(j).get("货品属性"))) {
				title = "货品属性";
				break;
			} else if ("".equals(content.get(j).get("客户名称"))) {
				title = "客户名称";
				break;
			} else if ("".equals(content.get(j).get("始发城市"))) {
				title = "始发城市";
				break;
			} else if ("".equals(content.get(j).get("到达城市"))) {
				title = "到达城市";
				break;
			} else if ("".equals(content.get(j).get("网点"))) {
				title = "网点";
				break;
			}

			String arrivalMode = content.get(j).get("到达方式");
			if ("".equals(arrivalMode)) {
				title = "到达方式";
				break;
			} else {
				if ("入中转仓".equals(arrivalMode)) {
					if ("".equals(content.get(j).get("中转仓"))) {
						title = "中转仓";
						break;
					}
				}
			}

			try {
				if ("".equals(content.get(j).get("计划日期"))) {
					title = "计划日期";
					break;
				} else {
					dbDataFormat.parse(content.get(j).get("计划日期"));
				}
			} catch (ParseException e) {
				title = "计划日期";
				because = "数据有误";
				break;
			}

			try {
				if ("".equals(content.get(j).get("预计到货日期"))) {
					title = "预计到货日期";
					break;
				} else {
					dbDataFormat.parse(content.get(j).get("预计到货日期"));
				}
			} catch (ParseException e) {
				title = "预计到货日期";
				because = "数据有误";
				break;
			}

			because = "数据有误";
			// 客户名称
			Party customer = Party.dao
					.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='"
							+ Party.PARTY_TYPE_CUSTOMER
							+ "' and c.abbr ='"
							+ content.get(j).get("客户名称") + "';");
			if (customer == null) {
				title = "客户名称";
				break;
			}
			// 始发城市
			Location location1 = Location.dao
					.findFirst("select code from location where name = '"
							+ content.get(j).get("始发城市") + "';");
			if (location1 == null) {
				title = "始发城市";
				break;
			}
			// 到达城市
			Location location2 = Location.dao
					.findFirst("select code from location where name = '"
							+ content.get(j).get("到达城市") + "';");
			if (location2 == null) {
				title = "到达城市";
				break;
			}

			// 网点
			Office office = Office.dao
					.findFirst("select id from office where office_name = '"
							+ content.get(j).get("网点") + "';");
			if (office == null) {
				title = "网点";
				break;
			}

			// 供应商名称
			Party provider = null;
			if (!"".equals(content.get(j).get("供应商名称"))) {
				provider = Party.dao
						.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='"
								+ Party.PARTY_TYPE_SERVICE_PROVIDER
								+ "' and c.abbr ='"
								+ content.get(j).get("供应商名称") + "';");
				if (provider == null) {
					title = "供应商名称";
					break;
				}
			}
			// 中转仓
			Warehouse warehouse = null;
			if (!"".equals(content.get(j).get("中转仓"))) {
				warehouse = Warehouse.dao
						.findFirst("select id from warehouse where warehouse_name = '"
								+ content.get(j).get("中转仓") + "';");
				if (warehouse == null) {
					title = "中转仓";
					break;
				}
			}

			// 验证同一execl文件多次导入问题:同一个客户和运输单号
			String sql = "select p.id from transfer_order tor left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id "
					+ " where p.party_type ='"
					+ Party.PARTY_TYPE_CUSTOMER
					+ "' "
					+ " and c.abbr ='"
					+ content.get(j).get("客户名称")
					+ "'"
					+ " and tor.customer_order_no = '"
					+ content.get(j).get("客户订单号").trim()
					+ "' "
					+ " and tor.planning_time = '"
					+ content.get(j).get("计划日期")
					+ "' and tor.arrival_time ='"
					+ content.get(j).get("预计到货日期") + "';";
			Record tansferOrder = Db.findFirst(sql);
			if (tansferOrder != null) {
				++verifyDuplicateRow;
			}

		}
		if (verifyDuplicateRow == content.size()) {
			importResult.put("result", "false");
			importResult.put("cause", "不能多次导入同一excel文件！");
		} else if (!"".equals(title)) {
			importResult.put("result", "false");
			if ("客户订单号".equals(title)) {
				importResult.put("cause", "验证数据至第" + (causeRow - 1) + "行,因第"
						+ causeRow + "行【" + title
						+ "】列,请为同一张运输单的客户订单号做上标识（如：运输单001，运输单002等）！");
			} else {
				importResult.put("cause", "验证数据至第" + (causeRow - 1) + "行,因第"
						+ causeRow + "行【" + title + "】列" + because);
			}
		} else {
			importResult.put("result", "true");
			importResult.put("cause", "验证数据成功");
		}
		return importResult;
	}

	/**
	 * 保存运输单
	 * 
	 * @param content
	 * @return
	 */
	private TransferOrder saveTransferOrder(Map<String, ?> content,
			Warehouse warehouse, Location location1, Location location2,
			Party customer, Party provider, Office office) throws Exception {
		TransferOrder transferOrder = new TransferOrder();
		String name = (String) currentUser.getPrincipal();
		UserLogin user = UserLogin.dao
				.findFirst("select * from user_login where user_name='" + name
						+ "'");
		SimpleDateFormat dbDataFormat = new SimpleDateFormat("yyyy-MM-dd");
		String orderNo = OrderNoGenerator.getNextOrderNo("YS");
		Date planningTime = dbDataFormat.parse((String) content.get("计划日期"));
		Date arrivalTime = dbDataFormat.parse((String) content.get("预计到货日期"));

		transferOrder
				.set("order_no", orderNo)
				.set("order_type", "salesOrder")
				// 订单类型：默认为销售订单
				.set("charge_type", "perUnit")
				// 客户计费方式：默认计件
				.set("charge_type2", "perUnit")
				// 供应商计费方式：默认计件
				.set("pickup_assign_status", TransferOrder.ASSIGN_STATUS_NEW)
				.set("depart_assign_status", TransferOrder.ASSIGN_STATUS_NEW)
				.set("status", "新建").set("create_by", user.get("id"))// 创建人id
				.set("create_stamp", new Date())// 创建时间
				.set("planning_time", planningTime)// 计划时间
				.set("arrival_time", arrivalTime)// 预计到货时间
				.set("customer_order_no", ((String) content.get("客户订单号")).trim());// 客户订单号

		// 运营方式
		if ("外包".equals(content.get("运营方式"))) {
			transferOrder.set("operation_type", "out_source");
		} else {
			transferOrder.set("operation_type", "own");
		}
		// 取货地址、收货信息
		if("普通货品".equals(content.get("货品属性"))){
			List itemDetailList = (List) content.get("itemDetailList");
			for (int i = 0; i < itemDetailList.size(); i++) {
				Map rec = (Map) itemDetailList.get(i);
				transferOrder.set("receiving_unit", rec.get("收货单位"))
				.set("receiving_name", rec.get("单品收货人"))
				.set("receiving_address", rec.get("单品收货地址"))
				.set("receiving_phone", rec.get("单品收货人联系电话"));
			}
		}
		
			
		

		// 到达方式
		if ("入中转仓".equals(content.get("到达方式"))) {
			// 入中转仓
			transferOrder.set("arrival_mode", "gateIn").set("warehouse_id",
					warehouse.get("id"));
		} else {
			// 货品直送
			transferOrder.set("arrival_mode", "delivery");
			// 保存联系人
			/*
			 * Contact contact = new Contact(); contact.set("address",
			 * content.get("单品收货地址"));//收货地址 contact.set("contact_person",
			 * content.get("单品收货人"));//收货人 contact.set("phone",
			 * content.get("单品收货人联系电话"));//收货人电话 contact.save(); // 保存收货人 Party
			 * party = new Party(); party.set("contact_id",
			 * contact.getLong("id")); party.set("create_date", new Date());
			 * party.set("creator", currentUser.getPrincipal());
			 * party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
			 * party.save(); //收货人 transferOrder.set("notify_party_id",
			 * party.get("id"));
			 */
		}
		// 始发城市
		transferOrder.set("route_from", location1.get("code"));
		// 目的地城市
		transferOrder.set("route_to", location2.get("code"));
		// 客户名称
		transferOrder.set("customer_id", customer.get("pid"));
		//取货地址
		transferOrder.set("address", content.get("始发城市"));
		// 供应商名称
		if (provider != null) {
			transferOrder.set("sp_id", provider.get("pid"));
		}
		// 网点
		if (office != null) {
			transferOrder.set("office_id", office.get("id"));
		}
		// 货品属性
		if (content.get("货品属性").equals("ATM")) {
			transferOrder.set("cargo_nature", "ATM");
			transferOrder.set("cargo_nature_detail", "cargoNatureDetailYes");
		} else {
			transferOrder.set("cargo_nature", "cargo");
			List itemDetailList = (List) content.get("itemDetailList");
			for (int i = 0; i < itemDetailList.size(); i++) {
				Map rec = (Map) itemDetailList.get(i);
				if (!"".equals(rec.get("单品序列号"))
						|| !"".equals(rec.get("单品件数"))) {
					transferOrder
							.set("cargo_nature_detail", "cargoNatureDetailYes");
				} else {
					transferOrder.set("cargo_nature_detail", "cargoNatureDetailNo");
				}
			}
		}
		transferOrder.set("remark", "这是导入的数据");
		transferOrder.save();
		return transferOrder;
	}

	/**
	 * 保存运输单货品 注： 1.运输单有单品时叠加计算货品数量， 2.运输单没单品直接读取文件，此时“发货数量”列是货品总数，不用修改
	 * 
	 * @param content
	 * @return
	 */
	private TransferOrderItem updateTransferOrderItem(Map content,Map item,
			double itemNumber, TransferOrder tansferOrder,
			TransferOrderItem transferOrderItem, Product product)
			throws Exception {
		String unit = "";
		double size = 0;
		double width = 0;
		double height = 0;
		double weight = 0;
		// 总体积、总重量
		double sumVolume = 0;
		double sumWeight = 0;
		if (product != null) {
			if (product.get("unit") != null && !"".equals(product.get("unit"))) {
				unit = product.get("unit");
			} else {
				unit = (String) content.get("单位");
			}
			if (product.get("size") != null && !"".equals(product.get("size"))) {
				size = product.getDouble("size") / 1000;
			}
			if (product.get("width") != null
					&& !"".equals(product.get("width"))) {
				width = product.getDouble("width") / 1000;
			}
			if (product.get("height") != null
					&& !"".equals(product.get("height"))) {
				height = product.getDouble("height") / 1000;
			}
			if (product.get("weight") != null
					&& !"".equals(product.get("weight"))) {
				weight = product.getDouble("weight");
			}
			if ("cargoNatureDetailYes".equals(tansferOrder
					.get("cargo_nature_detail"))) {
				sumVolume = size * width * height;
				sumWeight = weight;
			} else {
				if (product.get("volume") != null
						&& !"".equals(product.get("volume"))) {
					sumVolume = product.getDouble("volume") * itemNumber;
				} else {
					sumVolume = size * width * height * itemNumber;
				}
				sumWeight = weight * itemNumber;
			}
			// 保留两位小数
			BigDecimal volumeBig = new BigDecimal(sumVolume);
			sumVolume = volumeBig.setScale(2, BigDecimal.ROUND_HALF_UP)
					.doubleValue();
			BigDecimal weightBig = new BigDecimal(sumWeight);
			sumWeight = weightBig.setScale(2, BigDecimal.ROUND_HALF_UP)
					.doubleValue();
		}
		if (!"".equals(transferOrderItem.get("id"))
				&& transferOrderItem.get("id") != null) {
			// 有单品，货品数量叠加计数，重新计算总体积、总重量
			if ("cargoNatureDetailYes".equals(tansferOrder
					.get("cargo_nature_detail"))) {
				transferOrderItem
						.set("amount",
								transferOrderItem.getDouble("amount") + 1)
						.set("volume",
								transferOrderItem.getDouble("volume")
										+ sumVolume)
						.set("sum_weight",
								transferOrderItem.getDouble("sum_weight")
										+ sumWeight).update();
			}
		} else {
			if ("cargoNatureDetailYes".equals(tansferOrder
					.get("cargo_nature_detail"))) {
				// 有单品时叠加计算货品数量,默认数量为1
					transferOrderItem.set("amount", 1);
			} else {
				// 没单品直接读取“发货数量”为货品总数，不用修改
				if("普通货品".equals(content.get("货品属性"))){
					transferOrderItem.set("amount", item.get("发货数量"));
			}else{
				transferOrderItem.set("amount", itemNumber);
			}	
			}
			if (product == null) {
				// 取货地址、收货信息
				if("普通货品".equals(content.get("货品属性"))){
						transferOrderItem.set("item_no", item.get("货品型号"));
				}else{
					transferOrderItem.set("item_no", item.get("货品型号"));
				}
				
			} else {
				// 创建保存货品明细
				transferOrderItem.set("item_no", product.get("item_no"))
						.set("item_name", product.get("item_name"))
						.set("size", product.get("size")).set("unit", unit)
						.set("width", product.get("width"))
						.set("height", product.get("height"))
						.set("weight", product.get("weight"))
						.set("product_id", product.get("id"));
			}
			transferOrderItem.set("order_id", tansferOrder.get("id"))
					.set("volume", sumVolume).set("sum_weight", sumWeight)			
					.set("remark", item.get("备注"))
					.save();
		}
		return transferOrderItem;
	}

	/**
	 * 保存运输单单品
	 * 
	 * @param content
	 * @return
	 */
	private void saveTransferOrderItemDetail(Map content,
			TransferOrder tansferOrder, TransferOrderItem tansferOrderItem,
			Product product) throws Exception {
		TransferOrderItemDetail itemDatail = new TransferOrderItemDetail();
		if (product != null) {
			itemDatail.set("order_id", tansferOrder.get("id"))
					.set("item_id", tansferOrderItem.get("id"))
					.set("item_no", product.get("item_no"))
					.set("serial_no", content.get("单品序列号"))
					.set("item_name", product.get("item_name"))
					.set("volume", product.get("volume"))
					.set("weight", product.get("weight"));
		} else {
			itemDatail.set("order_id", tansferOrder.get("id"))
					.set("item_id", tansferOrderItem.get("id"))
					.set("item_no", content.get("货品型号"))
					.set("serial_no", content.get("单品序列号"));
		}
		itemDatail.set("notify_party_company", content.get("单品收货地址"))// 收货地址
				.set("receive_address", content.get("收货网点"))// 收货网点
				.set("notify_party_name", content.get("单品收货人"))// 收货人
				.set("notify_party_phone", content.get("单品收货人联系电话"))// 收货人电话
				.set("sales_order_no", content.get("单品销售单号"))// 销售单号
				.set("responsible_person", content.get("责任人"))// 责任人
				.set("business_manager", content.get("业务经理"))// 业务经理
				.set("station_name", content.get("服务站名称"))// 服务站名称
				.set("service_telephone", content.get("服务站电话"));// 服务站电话
		if (!"".equals(content.get("单品件数"))) {
			itemDatail.set("pieces", content.get("单品件数"));
		}
		itemDatail.save();
	}

	/**
	 * 保存运输里程碑
	 * 
	 * @param content
	 * @return
	 */
	private void saveTransferOrderMilestone(TransferOrder transferOrder)
			throws Exception {
		String name = (String) currentUser.getPrincipal();
		UserLogin user = UserLogin.dao
				.findFirst("select * from user_login where user_name='" + name
						+ "'");
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
	 * 
	 * @param lines
	 * @return 导入结果
	 */
	@Before(Tx.class)
	public Map<String, String> importTransferOrder(
			List<Map<String, String>> lines,String strFileConfirm,String valiErrCustomerNo) {
		Connection conn = null;

		Map<String, String> importResult = new HashMap<String, String>();
		importResult = validatingOrderNo(lines);
		if ("true".equals(importResult.get("result"))) {
			;
			int causeRow = 1;
			String errCustomerNo="";
			List addCustomerOrderNo = new ArrayList();
			Map<String, Map> orders = new HashMap();
			try {
				for (int j = 0; j < lines.size(); j++) {
					String serialNo = lines.get(j).get("单品序列号");
					String customerOrderNo = lines.get(j).get("客户订单号");
					String customer = lines.get(j).get("客户名称");
					errCustomerNo=customerOrderNo;
					Map order = orders.get(customerOrderNo);
					if (order == null) {
						causeRow++;
						//list拿到不同的客户订单号
						addCustomerOrderNo.add(customerOrderNo);
						order = initOrder(lines, j);
						// 检验单据 主表 信息
						orders.put(customerOrderNo, order);
						// check 系统中是否已存在
						validateData(orders, customerOrderNo,importResult,strFileConfirm,valiErrCustomerNo);
					} else {
						causeRow++;
						addItem(lines, j, serialNo, order);
						// check 系统中是否已存在
						if(!"".equals(serialNo)){
							// 客户名称
							Party customerID = Party.dao
									.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='"
											+ Party.PARTY_TYPE_CUSTOMER
											+ "' and c.abbr ='"
											+ customer
											+ "';");
							TransferOrderItemDetail detail = TransferOrderItemDetail.dao
									.findFirst(
											"SELECT * from transfer_order_item_detail toid LEFT JOIN transfer_order tor on tor.id=toid.order_id where toid.serial_no=? and tor.customer_id=?",
											serialNo,customerID.get("pid"));
								if(detail!=null){
									throw new Exception("序列号已存在");
								}
						}
						
					}
				}

				// 开始建造运输单
				System.out.println(orders);
				
				List<TransferOrder> orderList = new ArrayList<TransferOrder>();
				int Row=1;
				// 手动控制提交
				conn = DbKit.getConfig().getDataSource().getConnection();
				DbKit.getConfig().setThreadLocalConnection(conn);
				conn.setAutoCommit(false);// 自动提交变成false
				for (int j = 0; j < orders.size(); j++) {
					String customerNo = (String) addCustomerOrderNo.get(j);
					System.out.println("导入至第【" + causeRow + "】行");
					Map order = orders.get(customerNo);
					// 客户名称
					Party customer = Party.dao
							.findFirst("select p.id as pid,p.is_inventory_control is_inventory_control from party p left join contact c on c.id = p.contact_id where p.party_type ='"
									+ Party.PARTY_TYPE_CUSTOMER
									+ "' and c.abbr ='"
									+ order.get("客户名称")
									+ "';");
					// 仓库
					Warehouse warehouse = Warehouse.dao
							.findFirst("select id from warehouse where warehouse_name = '"
									+ order.get("中转仓") + "';");
					// 始发城市
					Location location1 = Location.dao
							.findFirst("select code from location where name = '"
									+ order.get("始发城市") + "';");
					// 目的地城市
					Location location2 = Location.dao
							.findFirst("select code from location where name = '"
									+ order.get("到达城市") + "';");
					// 供应商名称
					Party provider = Party.dao
							.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='"
									+ Party.PARTY_TYPE_SERVICE_PROVIDER
									+ "' and c.abbr ='"
									+ order.get("供应商名称")
									+ "';");
					// 网点
					Office office = Office.dao
							.findFirst("select id from office where office_name = '"
									+ order.get("网点") + "';");
					// 客户订单号
					String customerOrderNo = (String) order.get("客户订单号");
					// 发货数量
					double itemNumber = 0;
					if (!"".equals(order.get("发货数量"))) {
						itemNumber = Double.parseDouble((String) order.get("发货数量"));
					}
					// 创建保存运输单
					TransferOrder transferOrder = saveTransferOrder(
							order, warehouse, location1,
							location2, customer, provider, office);
					// 已生成的运输单据保存到回滚list中
					orderList.add(transferOrder);
					// 保存运输里程碑
					saveTransferOrderMilestone(transferOrder);
					// 创建保存货品明细
					// 货品型号
					List itemDetailList = (List) order.get("itemDetailList");
					// check 当前文件 单品重复
					TransferOrderItem tansferOrderItem = null;
//					String sql = "select * from transfer_order where customer_id = '"
//							+ customer.get("pid")
//							+ "' and customer_order_no = '"
//							+ customerOrderNo
//							+ "'";
//					TransferOrder t_order = TransferOrder.dao.findFirst(sql);
					for (int i = 0; i < itemDetailList.size(); i++) {
						Row++;
						Map rec = (Map) itemDetailList.get(i);
						String orderItem = (String) rec.get("货品型号");
						TransferOrderItem item = null;
						Product product = Product.dao
								.findFirst("select p.* from product p left join category c on c.id = p.category_id where c.customer_id = '"
										+ customer.get("pid")
										+ "' and item_no =  '"
										+ orderItem
										+ "';");
						if(product!=null){
							tansferOrderItem = TransferOrderItem.dao
									.findFirst("select * from transfer_order_item where order_id = '"
											+ transferOrder.get("id")
											+ "' and item_no = '"
											+ product.get("item_no") + "';");
						}else{
							//判断客户是否需要库存管理
							if(customer.getInt("is_inventory_control")>0){
								causeRow=Row;
								throw new Exception("客户在系统里不存在货品型号"+orderItem);
							}else{
								tansferOrderItem = TransferOrderItem.dao
										.findFirst("select * from transfer_order_item where order_id = '"
												+ transferOrder.get("id")
												+ "' and item_no = '"
												+ orderItem + "';");
							}	
						}
						if (tansferOrderItem == null) {
							item = updateTransferOrderItem(
									order,rec,itemNumber,
									transferOrder, new TransferOrderItem(),
									product);
						} else {
							item = updateTransferOrderItem(
									order,rec,itemNumber,
									transferOrder,tansferOrderItem, product);
						}
						if ("cargoNatureDetailYes".equals(transferOrder
								.get("cargo_nature_detail"))) {
							// 创建单品货品明细
							saveTransferOrderItemDetail(rec, transferOrder,
									item, product);
						}
					}

					// 创建保存单品货品明细

				}
				conn.commit();
			} catch (Exception e) {
				System.out.println("导入操作异常！");
				System.out.println(e.getMessage());
				e.printStackTrace();
				try {
					if (null != conn)
						conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}

				importResult.put("result", "false");
				if(!"客户订单号已存在".equals(e.getMessage())){
					importResult.put("cause", "导入失败，数据导入至第" + (causeRow)
							+ "行时出现异常:" + e.getMessage() + "，<br/>导入数据已取消！");
				}else{
					importResult.put("cause", "导入提示，数据导入至第" + (causeRow)
							+ "行时出现异常:" +errCustomerNo+ e.getMessage() + "，<br/>确认继续导入？");
					importResult.put("errCustomerNo", errCustomerNo);
					importResult.put("strFileConfirm", strFileConfirm);
				}
				importResult.put("equal", e.getMessage());
				return importResult;
			} finally {
				try {
					if (null != conn) {
						conn.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				} finally {
					DbKit.getConfig().removeThreadLocalConnection();
				}
			}
			importResult.put("result", "true");
			importResult.put("cause", "成功导入" + orders.size() + "张运输单");

		}
		return importResult;
	}

	private void validateData(Map<String, Map> orders, String customerOrderNo,Map<String, String> result,String strFileConfirm,String valiErrCustomerNo)
			throws Exception {
		Boolean isCustomerOrderNo=false;
		if(valiErrCustomerNo!=null){
			String[] valiErrCustomerNoArr = valiErrCustomerNo.split(",");
			for (int i = 0; i < valiErrCustomerNoArr.length; i++) {
				if (customerOrderNo.equals(valiErrCustomerNoArr[i])) {
					isCustomerOrderNo=true;
					break;
				}
			}
		  }
		if(!isCustomerOrderNo){
			TransferOrder Transferorder = TransferOrder.dao
					.findFirst(
							"SELECT * from transfer_order where customer_order_no=?",
							customerOrderNo);
			if (Transferorder != null) {
				throw new Exception("客户订单号已存在");
			}
		}
		Map order =orders.get(customerOrderNo);
		//到达方式
		String arrival_mode=(String) order.get("到达方式");
		//运营方式
		String operation_type =(String) order.get("运营方式");
		//货品属性
		String cargo_nature =(String) order.get("货品属性");
		//到达方式判断
		if(!"入中转仓".equals(arrival_mode)&&!"货品直送".equals(arrival_mode)){
			throw new Exception("到达方式只能为入中转仓和货品直送");
		}
		//运营方式判断
		if(!"自营".equals(operation_type)&&!"外包".equals(operation_type)){
			throw new Exception("运营方式只能为自营和外包");
		}
		//货品属性判断
		if(!"普通货品".equals(cargo_nature)&&!"ATM".equals(cargo_nature)){
			throw new Exception("货品属性只能为普通货品和ATM");
		}
		// 客户名称
		Party customer = Party.dao
				.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='"
						+ Party.PARTY_TYPE_CUSTOMER
						+ "' and c.abbr ='"
						+ order.get("客户名称") + "';");
		if(customer==null){
			throw new Exception("客户名称有误");
		}
		// 始发城市
		Location location1 = Location.dao
				.findFirst("select code from location where name = '"
						+ order.get("始发城市") + "';");
		if(location1==null){
			throw new Exception("始发城市有误");
		}
		// 到达城市
		Location location2 = Location.dao
				.findFirst("select code from location where name = '"
						+ order.get("到达城市") + "';");
		if(location2==null){
			throw new Exception("到达城市有误");
		}
		// 网点
		Office office = Office.dao
				.findFirst("select id from office where office_name = '"
						+ order.get("网点") + "';");
		if(office==null){
			throw new Exception("网点有误");
		}
		//中转仓
		if("入中转仓".equals(order.get("到达方式"))){
			Warehouse warehouse = Warehouse.dao
					.findFirst("select id from warehouse where warehouse_name = '"
							+ order.get("中转仓") + "';");
			if(warehouse==null){
				throw new Exception("中转仓有误");
			}
		}
		// 供应商名称
		if (!"".equals(order.get("供应商名称"))) {
			Party	provider = Party.dao
								.findFirst("select p.id as pid from party p left join contact c on c.id = p.contact_id where p.party_type ='"
										+ Party.PARTY_TYPE_SERVICE_PROVIDER
										+ "' and c.abbr ='"
										+ order.get("供应商名称") + "';");
			if(provider==null){
				throw new Exception("供应商名称有误");
			}
		}
	}

	private void addItem(List<Map<String, String>> content, int j,
			String serialNo, Map order) throws Exception {
		List itemDetailList = (List) order.get("itemDetailList");

		// check 当前文件 单品重复
		for (int i = 0; i < itemDetailList.size(); i++) {
			Map rec = (Map) itemDetailList.get(i);
			String listSerialNo = (String) rec.get("单品序列号");
			if(!"".equals(serialNo)){
				if (serialNo.equals(listSerialNo)) {
					throw new Exception("导入文件存在相同的单品序列号");
				}
			}
		}

		Map serialMap = buildItemDetail(content, j);
		itemDetailList.add(serialMap);
	}

	private Map buildItemDetail(List<Map<String, String>> content, int j) {
		Map serialMap = new HashMap();
		serialMap.put("单品件数", content.get(j).get("单品件数"));
		serialMap.put("货品型号", content.get(j).get("货品型号"));
		serialMap.put("收货单位", content.get(j).get("收货单位"));
		serialMap.put("收货网点", content.get(j).get("收货网点"));
		serialMap.put("单品收货地址", content.get(j).get("单品收货地址"));
		serialMap.put("单品收货人", content.get(j).get("单品收货人"));
		serialMap.put("单品收货人联系电话", content.get(j).get("单品收货人联系电话"));
		serialMap.put("单品序列号", content.get(j).get("单品序列号"));
		serialMap.put("发货数量", content.get(j).get("发货数量"));
		serialMap.put("备注", content.get(j).get("备注"));
		return serialMap;
	}

	private Map initOrder(List<Map<String, String>> content, int j) {
		Map order = new HashMap();
		order.put("客户订单号", content.get(j).get("客户订单号"));
		order.put("客户名称", content.get(j).get("客户名称"));
		order.put("运营方式", content.get(j).get("运营方式"));
		order.put("到达方式", content.get(j).get("到达方式"));
		order.put("供应商名称", content.get(j).get("供应商名称"));
		order.put("计划日期", content.get(j).get("计划日期"));
		order.put("单位", content.get(j).get("单位"));
		order.put("发货数量", content.get(j).get("发货数量"));
		order.put("货品属性", content.get(j).get("货品属性"));
		order.put("单品销售单号", content.get(j).get("单品销售单号"));
		order.put("业务经理", content.get(j).get("业务经理"));
		order.put("服务站名称", content.get(j).get("服务站名称"));
		order.put("责任人", content.get(j).get("责任人"));
		order.put("网点", content.get(j).get("网点"));
		order.put("中转仓", content.get(j).get("中转仓"));
		order.put("始发城市", content.get(j).get("始发城市"));
		order.put("到达城市", content.get(j).get("到达城市"));
		order.put("预计到货日期", content.get(j).get("预计到货日期"));
		// item_detail_list
		List itemDetailList = new ArrayList();

		Map serialMap = buildItemDetail(content, j);
		itemDetailList.add(serialMap);

		order.put("itemDetailList", itemDetailList);
		return order;
	}

}
