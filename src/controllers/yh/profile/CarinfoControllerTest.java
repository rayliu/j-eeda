package controllers.yh.profile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.CarSummaryDetail;
import models.CarSummaryDetailOilFee;
import models.CarSummaryDetailOtherFee;
import models.CarSummaryDetailRouteFee;
import models.CarSummaryDetailSalary;
import models.CarSummaryOrder;
import models.DepartOrder;
import models.TransferOrderMilestone;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import config.DataInitUtil;
import controllers.yh.LoginUserController;

public class CarinfoControllerTest extends Controller {
	private Logger logger = Logger.getLogger(CarinfoController.class);
	
	public void index() {
        HttpServletRequest re = getRequest();
        String url = re.getRequestURI();
        logger.debug("URI:" + url);
        if (url.equals("/yh/carsummary")) {
            if (LoginUserController.isAuthenticated(this))
                render("/yh/carmanage/carSummaryList.html");
        }
    }
	
	//未处理调车单
	public void untreatedCarManageList(){
		
		Map orderMap = null;
		String status = getPara("status");
		String driver = getPara("driver");
		String car_no = getPara("car_no");
		String transferOrderNo = getPara("transferOrderNo");
		String create_stamp = getPara("create_stamp");
		
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sql = "";
        String sqlTotal = "";
        
        
		if (driver == null && status == null && car_no == null
				&& transferOrderNo == null && create_stamp == null ) {

	        // 获取总条数
	        sqlTotal = "select count(1) total from depart_order dor " + ""
	                    + " left join carinfo c on dor.driver_id = c.id " + " where dor.status!='取消' and dor.car_summary_type = '未处理' and combine_type = '"
	                    + DepartOrder.COMBINE_TYPE_PICKUP + "' and (dor.status = '已入货场' or dor.status = '已入库' ) and dor.pickup_mode = 'own'";

	        // 获取当前页的数据
	        sql = "select dor.*,ct.contact_person,ct.phone,c.driver,c.cartype,c.status cstatus,c.length, (select group_concat(dt.transfer_order_no separator '\r\n')  from depart_transfer dt where pickup_id = dor.id)  as transfer_order_no  from depart_order dor "
	                    + " left join carinfo c on dor.carinfo_id = c.id "
	                    + " left join party p on dor.driver_id = p.id "
	                    + " left join contact ct on p.contact_id = ct.id "
	                    + " where dor.status!='取消' and dor.car_summary_type = '未处理' and combine_type = '"
	                    + DepartOrder.COMBINE_TYPE_PICKUP + "' and (dor.status = '已入货场' or dor.status = '已入库' ) and dor.pickup_mode = 'own' order by c.car_no,dor.create_stamp desc " + sLimit;
	        
		}else{
			
	        // 获取总条数
	        sqlTotal="select count(1) total from depart_order dor " + ""
                    + " left join carinfo c on dor.driver_id = c.id left join depart_transfer dt on dt.pickup_id = dor.id "
                    + " where dor.status!='取消' and dor.car_summary_type = '未处理' and combine_type = '"
                    + DepartOrder.COMBINE_TYPE_PICKUP + "' and (dor.status = '已入货场' or dor.status = '已入库' ) and dor.pickup_mode = 'own'"
                    + " and ifnull(c.driver, '') like '%"+driver+"%'"
					+ " and ifnull(c.status, '') like '%"+status+"%'"
					+ " and ifnull(dor.car_no, '') like '%"+car_no+"%'"
					+ " and ifnull(dt.transfer_order_no, '') like '%"+transferOrderNo+"%'"
					+ " and ifnull(dor.create_stamp, '') like '%"+create_stamp+"%'";

	        // 获取当前页的数据
	        sql = "select dor.*,ct.contact_person,ct.phone,c.driver,c.cartype,c.status cstatus,c.length, (select group_concat(dt.transfer_order_no separator '\r\n')  from depart_transfer dt where pickup_id = dor.id)  as transfer_order_no  from depart_order dor "
	                    + " left join carinfo c on dor.carinfo_id = c.id "
	                    + " left join party p on dor.driver_id = p.id "
	                    + " left join contact ct on p.contact_id = ct.id "
	                    + " left join depart_transfer dt on dt.pickup_id = dor.id"
	                    + " where dor.status!='取消' and dor.car_summary_type = '未处理' and combine_type = '"
	                    + DepartOrder.COMBINE_TYPE_PICKUP + "' and (dor.status = '已入货场' or dor.status = '已入库' ) and dor.pickup_mode = 'own' "
	                    + " and ifnull(c.driver, '') like '%"+driver+"%'"
						+ " and ifnull(c.status, '') like '%"+status+"%'"
						+ " and ifnull(dor.car_no, '') like '%"+car_no+"%'"
						+ " and ifnull(dt.transfer_order_no, '') like '%"+transferOrderNo+"%'"
						+ " and ifnull(dor.create_stamp, '') like '%"+create_stamp+"%'"
	                    + " order by c.car_no,dor.create_stamp desc " + sLimit;
		}
		Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orders = Db.find(sql);
		
	 	orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
		
	}
	
	//行车单查询
	public void CarManageList(){
		
		Map orderMap = null;
		String status = getPara("status");
		String driver = getPara("driver");
		String car_no = getPara("car_no");
		String transferOrderNo = getPara("transferOrderNo");
		String create_stamp = getPara("create_stamp");
		
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String sqlTotal = "";
        String sql = "";
		if (driver == null && status == null && car_no == null
				&& transferOrderNo == null && create_stamp == null ) {
			sqlTotal = "";
			
			sql = "";
	        
		}else{
			
			sqlTotal = "";
			
			sql = "";
		}
		Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orders = Db.find(sql);
		
	 	orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
		
		
	}
	
	//创建行车单
	public void createCarSummary(){
		String pickupIdsArray = getPara("pickupIds");
		setAttr("pickupIds", pickupIdsArray);
		if(!"".equals(pickupIdsArray) && pickupIdsArray != null){
			try {
				String pickupIds[] = pickupIdsArray.split(",");
				int num = 0;
				for (int i = 0; i < pickupIds.length; i++) {
					DepartOrder departOrder = DepartOrder.dao.findById(pickupIds[i]);
					//车牌号
					if(num == 0){
						setAttr("turnoutNumber", departOrder.get("car_no"));
					}
					num++;
				}
				//出车次数
				setAttr("turnoutCount", num);
			} catch (Exception e) {
				
			}
		}
		
		render("/yh/carManage/carSummaryEdit.html");
	}
	
	//保存行车单
	public void saveCarSummary(){
		//拼车单id
		String pickupIdArray = getPara("pickupIds");
		String pickupIds[] = pickupIdArray.split(",");
		//行车单信息
        String car_summary_id = getPara("car_summary_id");
        String car_no = getPara("car_no");
        String main_driver_name = getPara("main_driver_name");
        String main_driver_amount = getPara("main_driver_amount").equals("") ?"0":getPara("main_driver_amount");
        String minor_driver_name = getPara("minor_driver_name");
        String minor_driver_amount = getPara("minor_driver_amount").equals("") ?"0":getPara("main_driver_amount");
        String start_car_mileage = getPara("start_car_mileage").equals("") ?"0":getPara("start_car_mileage");
        String finish_car_mileage = getPara("finish_car_mileage").equals("") ?"0":getPara("finish_car_mileage");
        String month_start_car_next = getPara("month_start_car_next").equals("") ?"0":getPara("month_start_car_next");
        String month_car_run_mileage = getPara("month_car_run_mileage").equals("") ?"0":getPara("month_car_run_mileage");
        String month_refuel_amount = getPara("month_refuel_amount").equals("") ?"0":getPara("month_refuel_amount");
        String next_start_car_mileage = getPara("next_start_car_mileage").equals("") ?"0":getPara("next_start_car_mileage");
        String next_start_car_amount = getPara("next_start_car_amount").equals("") ?"0":getPara("next_start_car_amount");
        String deduct_apportion_amount = getPara("deduct_apportion_amount").equals("") ?"0":getPara("deduct_apportion_amount");
        String actual_payment_amount = getPara("actual_payment_amount").equals("") ?"0":getPara("actual_payment_amount");
        boolean result;
        String order_no = null;
        if(car_summary_id.equals("") || car_summary_id == null){ //新建时
        	CarSummaryOrder carSummaryOrder = new CarSummaryOrder();
        	java.util.Date utilDate = new java.util.Date();
        	java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        	//调车单号
        	CarSummaryOrder order = CarSummaryOrder.dao
    				.findFirst("select * from car_summary_order order by id desc limit 0,1");
        	if (order != null) {
	        	String num = order.get("order_no");
	            String str = num.substring(2, num.length());
	            Long oldTime = Long.parseLong(str);
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	            String format = sdf.format(new Date());
	            String time = format + "00001";
	            Long newTime = Long.parseLong(time);
	            if (oldTime >= newTime) {
	                order_no = String.valueOf((oldTime + 1));
	            } else {
	                order_no = String.valueOf(newTime);
	            }
        	} else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String format = sdf.format(new Date());
                order_no = format + "00001";
            }
        	
        	result = carSummaryOrder.set("order_no", "XC" + order_no).set("car_no", car_no).set("main_driver_name", main_driver_name)
            		.set("main_driver_amount", main_driver_amount).set("minor_driver_name", minor_driver_name)
            		.set("minor_driver_amount", minor_driver_amount).set("start_car_mileage", start_car_mileage)
            		.set("finish_car_mileage", finish_car_mileage).set("month_start_car_next", month_start_car_next)
            		.set("month_car_run_mileage", month_car_run_mileage).set("month_refuel_amount", month_refuel_amount)
            		.set("next_start_car_mileage", next_start_car_mileage).set("next_start_car_amount", next_start_car_amount)
            		.set("deduct_apportion_amount", deduct_apportion_amount).set("actual_payment_amount", actual_payment_amount)
            		.set("create_data", sqlDate).save();
        	
        	if(result){
        		CarSummaryOrder carSummary = CarSummaryOrder.dao
        				.findFirst("select * from car_summary_order order by id desc limit 0,1");
        		
        		long carSunmmaryId = carSummary.getLong("id");
        		
        		for (int i = 0; i < pickupIds.length; i++) {
        			//插入从表数据
        			CarSummaryDetail carSummaryDetail = new CarSummaryDetail();
        			carSummaryDetail.set("car_summary_id", carSunmmaryId);
        			carSummaryDetail.set("pickup_order_id", Long.parseLong(pickupIds[i]));
        			carSummaryDetail.save();
        			//修改调车单状态为：已处理
        			DepartOrder departOrder = DepartOrder.dao.findById(pickupIds[i]);
        			departOrder.set("car_summary_type", "已处理");
        			departOrder.update();
				}
        		//创建费用合计表初始数据
        		initCarSummaryDetailOtherFeeData(carSunmmaryId);
        		
        		car_summary_id = Long.toString(carSunmmaryId);
        	}
        }else{//修改时
        	CarSummaryOrder carSummary = CarSummaryOrder.dao.findById(car_summary_id);
        	if(carSummary != null){
        		carSummary.set("main_driver_name", main_driver_name)
	    		.set("main_driver_amount", main_driver_amount).set("minor_driver_name", minor_driver_name)
	    		.set("minor_driver_amount", minor_driver_amount).set("start_car_mileage", start_car_mileage)
	    		.set("finish_car_mileage", finish_car_mileage).set("month_start_car_next", month_start_car_next)
	    		.set("month_car_run_mileage", month_car_run_mileage).set("month_refuel_amount", month_refuel_amount)
	    		.set("next_start_car_mileage", next_start_car_mileage).set("next_start_car_amount", next_start_car_amount)
	    		.set("deduct_apportion_amount", deduct_apportion_amount).set("actual_payment_amount", actual_payment_amount)
	    		.update();
        	}
        }
        
        renderJson(car_summary_id);
	}
	
	//查询所有司机
	public void searchAllDriver(){
		String input = getPara("input");
		String sql = "";
		if(input != "" && input != null){
			sql = "select * from carinfo c where driver like '"+input+"'";
		}else{
			sql = "select * from carinfo";
		}
		List<Record> carinfoList = Db.find(sql);
		renderJson(carinfoList);
	}
	
	//线路查询
	public void findAllAddress(){
		String pickupIds = getPara("pickupIds");
		
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(1) total from depart_order tor "
         		+ " left join depart_transfer dt on dt.pickup_id = tor.id "
         		+ " where tor.id in("+pickupIds+");";
    	 
        String sql = "select tor.*, tr.order_no ,c.abbr,tr.address address1,tor.address address2,"
         		+ "( select warehouse_name from  warehouse where id = tr.warehouse_id  ) address3 "
         		+ " from depart_order tor "
         		+ " left join depart_transfer dt on dt.pickup_id = tor.id "
         		+ " left join transfer_order tr on tr.id = dt.order_id "
         		+ " left join party p on p.id = tr.customer_id "
         		+ " left join contact c ON c.id = p.contact_id "
         		+ " where tor.id in("+pickupIds+");";
         
		Record rec = Db.findFirst(sqlTotal);
		List<Record> orders = Db.find(sql);
        
        HashMap orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
		
	}
	// 货品信息
    public void findPickupOrderItems() {
    	String pickupIds = getPara("pickupIds");// 调车单id

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(0) total from depart_transfer dt left join transfer_order_item toi on toi.order_id = dt.pickup_id where dt.pickup_id  in(" + pickupIds + ")";
        
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select dor.depart_no,toi.id,ifnull(toi.item_name, pd.item_name) item_name,"
        		+ "ifnull(toi.item_no, pd.item_no) item_no,ifnull(toi.volume, pd.volume) * toi.amount volume,"
        		+ "ifnull(case toi.weight when 0.0 then null else toi.weight end,pd.weight) * toi.amount weight,"
        		+ "c.abbr customer,	tor.order_no,toi.amount,toi.remark from	depart_order dor "
        		+ " left join depart_transfer dt on dt.pickup_id = dor.id "
        		+ " left join transfer_order tor on tor.id = dt.order_id "
        		+ " left join transfer_order_item toi on toi.order_id = dt.pickup_id "
        		+ " left join party p on p.id = tor.customer_id "
        		+ " left join contact c on c.id = p.contact_id "
        		+ " left join product pd on pd.id = toi.product_id "
        		+ " where toi.order_id in ("+pickupIds+");";
        List<Record> departOrderitem = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", departOrderitem);
        renderJson(Map);
    }
    // 里程碑
    public void transferOrderMilestoneList() {
    	String pickupIds = getPara("pickupIds");// 调车单id

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(0) total from transfer_order_milestone tom"
        		+ " left join user_login u on u.id = tom.create_by"
        		+ " where tom.type = '"
                            + TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE + "' and tom.pickup_id in(" + pickupIds+");";
        logger.debug("sql :" + sqlTotal);
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select tom.*,u.user_name from transfer_order_milestone tom"
        		+ " left join user_login u on u.id = tom.create_by"
        		+ " where tom.type = '"
                            + TransferOrderMilestone.TYPE_PICKUP_ORDER_MILESTONE + "' and tom.pickup_id in(" + pickupIds+");";
        List<Record> departOrderitem = Db.find(sql);
        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", departOrderitem);
        renderJson(Map);
    }
    
    //查询路桥费明细
    public void findCarSummaryRouteFee(){
    	String car_summary_id = getPara("car_summary_id");// 调车单id
    	if(car_summary_id != ""){
	    	String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	        String sqlTotal = "select count(0) total from car_summary_detail_route_fee where car_summary_id ="+car_summary_id+";";
	        logger.debug("sql :" + sqlTotal);
	        Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        String sql = "select * from car_summary_detail_route_fee where car_summary_id ="+car_summary_id+";";
	        List<Record> departOrderitem = Db.find(sql);
	        Map Map = new HashMap();
	        Map.put("sEcho", pageIndex);
	        Map.put("iTotalRecords", rec.getLong("total"));
	        Map.put("iTotalDisplayRecords", rec.getLong("total"));
	        Map.put("aaData", departOrderitem);
	        renderJson(Map); 
    	}
    }
    // 添加路桥费明细
    public void addCarSummaryRouteFee() {
        String carSummaryId = getPara();
        if(carSummaryId != ""){
        	java.util.Date utilDate = new java.util.Date();
        	java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            
        	String sql = "select * from car_summary_detail_route_fee where  car_summary_id ='"+carSummaryId+"' order by item  desc limit 0,1;";
        	List<CarSummaryDetailRouteFee> routeFeeList = CarSummaryDetailRouteFee.dao.find(sql);
        	long item = 1;
        	if(routeFeeList.size() > 0){
        		item += routeFeeList.get(0).getLong("item");
        	}
        	CarSummaryDetailRouteFee routeFee = new CarSummaryDetailRouteFee();
            routeFee.set("item", item).set("car_summary_id", carSummaryId).set("charge_data", sqlDate).save();
        }
        renderJson("{\"success\":true}");
    }
    //删除路桥费明细
    public void delCarSummaryRouteFee(){
    	String carSummaryRouteFeeId = getPara();
    	CarSummaryDetailRouteFee.dao.deleteById(carSummaryRouteFeeId);
    	renderJson("{\"success\":true}");
    }
    //修改路桥费明细
    public void updateCarSummaryDetailRouteFee(){
    	String routeFeeId = getPara("routeFeeId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if(!"".equals(routeFeeId) && routeFeeId != null){
    		if("travel_amount".equals(name) && "".equals(value)){
    			value = "0";
    		}
    		CarSummaryDetailRouteFee carSummaryDetailrouteFee = CarSummaryDetailRouteFee.dao.findById(routeFeeId);
    		carSummaryDetailrouteFee.set(name, value);
    		carSummaryDetailrouteFee.update();
    	}
    	renderJson("{\"success\":true}");
    }
    
    //查询加油记录
    public void findCarSummaryDetailOilFee(){
    	String car_summary_id = getPara("car_summary_id");// 调车单id
    	if(car_summary_id != ""){
	    	String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	        String sqlTotal = "select count(0) total from car_summary_detail_oil_fee where car_summary_id ="+car_summary_id+";";
	        logger.debug("sql :" + sqlTotal);
	        Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        String sql = "select * from car_summary_detail_oil_fee where car_summary_id ="+car_summary_id+";";
	        List<Record> departOrderitem = Db.find(sql);
	        Map Map = new HashMap();
	        Map.put("sEcho", pageIndex);
	        Map.put("iTotalRecords", rec.getLong("total"));
	        Map.put("iTotalDisplayRecords", rec.getLong("total"));
	        Map.put("aaData", departOrderitem);
	        renderJson(Map); 
    	}
    }
    // 添加加油记录
    public void addCarSummaryDetailOilFee() {
        String carSummaryId = getPara();
        if(carSummaryId != ""){
        	
        	java.util.Date utilDate = new java.util.Date();
        	java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        	
        	String sql = "select * from car_summary_detail_oil_fee where  car_summary_id ='"+carSummaryId+"' order by item  desc limit 0,1;";
        	List<CarSummaryDetailOilFee> oilFeeList = CarSummaryDetailOilFee.dao.find(sql);
        	long item = 1;
        	if(oilFeeList.size() > 0){
        		item += oilFeeList.get(0).getLong("item");
        	}
        	CarSummaryDetailOilFee oilFee = new CarSummaryDetailOilFee();
        	oilFee.set("item", item).set("car_summary_id", carSummaryId)
        	.set("refuel_data", sqlDate).set("payment_type", "油卡").save();
        }
        renderJson("{\"success\":true}");
    }
    //删除加油记录
    public void delCarSummaryDetailOilFee(){
    	String carSummaryDetailOilFeeId = getPara();
    	CarSummaryDetailOilFee.dao.deleteById(carSummaryDetailOilFeeId);
    	renderJson("{\"success\":true}");
    }
    //修改加油记录
    public void updateCarSummaryDetailOilFee(){
    	String routeFeeId = getPara("routeFeeId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if(!"".equals(routeFeeId) && routeFeeId != null){
    		if("odometer_mileage".equals(name) || "refuel_unit_cost".equals(name)
    			|| "refuel_number".equals(name) || "refuel_amount".equals(name) 
    			|| "avg_econ".equals(name) || "load_amount".equals(name)){
    			if("".equals(value)){
    				value = "0";
    			}
    		}
    		CarSummaryDetailOilFee carSummaryDetailOilFee = CarSummaryDetailOilFee.dao.findById(routeFeeId);
    		carSummaryDetailOilFee.set(name, value);
    		carSummaryDetailOilFee.update();
    	}
    	renderJson("{\"success\":true}");
    }
    
    //查询送货员工资明细
    public void findCarSummaryDetailSalary(){
    	String car_summary_id = getPara("car_summary_id");// 调车单id
    	if(car_summary_id != ""){
	    	String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	        String sqlTotal = "select count(0) total from car_summary_detail_salary where car_summary_id ="+car_summary_id+";";
	        logger.debug("sql :" + sqlTotal);
	        Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        String sql = "select * from car_summary_detail_salary where car_summary_id ="+car_summary_id+";";
	        List<Record> departOrderitem = Db.find(sql);
	        Map Map = new HashMap();
	        Map.put("sEcho", pageIndex);
	        Map.put("iTotalRecords", rec.getLong("total"));
	        Map.put("iTotalDisplayRecords", rec.getLong("total"));
	        Map.put("aaData", departOrderitem);
	        renderJson(Map); 
    	}
    }
    // 添加送货员工资明细
    public void addCarSummaryDetailSalary() {
        String carSummaryId = getPara();
        if(carSummaryId != ""){
        	java.util.Date utilDate = new java.util.Date();
        	java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        	String sql = "select * from car_summary_detail_salary where  car_summary_id ='"+carSummaryId+"' order by item  desc limit 0,1;";
        	List<CarSummaryDetailSalary> salaryList = CarSummaryDetailSalary.dao.find(sql);
        	long item = 1;
        	if(salaryList.size() > 0){
        		item += salaryList.get(0).getLong("item");
        	}
        	CarSummaryDetailSalary salary = new CarSummaryDetailSalary();
        	salary.set("item", item).set("car_summary_id", carSummaryId)
        	.set("create_data", sqlDate).save();
        }
        renderJson("{\"success\":true}");
    }
    //删除送货员工资明细
    public void delCarSummaryDatailSalary(){
    	String carSummaryDetailSalaryId = getPara();
    	CarSummaryDetailSalary.dao.deleteById(carSummaryDetailSalaryId);
    	renderJson("{\"success\":true}");
    }
    //修改送货员工资明细
    public void updateCarSummaryDetailSalary(){
    	String routeFeeId = getPara("routeFeeId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if(!"".equals(routeFeeId) && routeFeeId != null){
    		if("deserved_amount".equals(name) && "".equals(value)){
    			value = "0";
    		}
    		CarSummaryDetailSalary salary = CarSummaryDetailSalary.dao.findById(routeFeeId);
    		salary.set(name, value);
    		salary.update();
    	}
    	renderJson("{\"success\":true}");
    }
    //查询费用合计
    public void findCarSummaryDetailOtherFee(){
    	String car_summary_id = getPara("car_summary_id");// 调车单id
    	if(car_summary_id != ""){
	    	String sLimit = "";
	        String pageIndex = getPara("sEcho");
	        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
	            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
	        }
	        String sqlTotal = "select count(0) total from car_summary_detail_other_fee where car_summary_id ="+car_summary_id+";";
	        logger.debug("sql :" + sqlTotal);
	        Record rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        String sql = "select * from car_summary_detail_other_fee where car_summary_id ="+car_summary_id+";";
	        List<Record> departOrderitem = Db.find(sql);
	        Map Map = new HashMap();
	        Map.put("sEcho", pageIndex);
	        Map.put("iTotalRecords", rec.getLong("total"));
	        Map.put("iTotalDisplayRecords", rec.getLong("total"));
	        Map.put("aaData", departOrderitem);
	        renderJson(Map); 
    	}
    }
    //修改费用合计(复杂的费用计算业务)
    public void updateCarSummaryDetailOtherFee(){
    	String routeFeeId = getPara("routeFeeId");
    	String name = getPara("name");
    	String value = getPara("value");
    	if(!"".equals(routeFeeId) && routeFeeId != null){
    		if("amount".equals(name) && "".equals(value)){
    			value = "0";
    		}
    		CarSummaryDetailOtherFee otherFee = CarSummaryDetailOtherFee.dao.findById(routeFeeId);
    		otherFee.set(name, value);
    		otherFee.update();
    	}
    	renderJson("{\"success\":true}");
    }
    //每次新增调车单时要添加的数据
    private void initCarSummaryDetailOtherFeeData(long carSummaryOrderId){
    	
    	String name[] = {"car_summary_id","item","amount_item","is_delete"};
    	String items[] = {"本次加油","本次油耗","出车补贴","司机工资","路桥费","装卸费","罚款","送货员工资","停车费","住宿费","过磅费","其他费用"};
    	String isdeletes = "是";
    	for (int i = 0; i < items.length; i++) {
    		CarSummaryDetailOtherFee c = new CarSummaryDetailOtherFee();
    		if(i == 1 || i == 3 || i == 7)
    			c.set(name[0], carSummaryOrderId).set(name[1], i+1).set(name[2], items[i]).set(name[3], isdeletes).set("amount", 0).save();
    		else
    			c.set(name[0], carSummaryOrderId).set(name[1], i+1).set(name[2], items[i]).set("amount", 0).save();
		}
    }
    
}
