package controllers.yh.departOrder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartTransferOrder;
import models.Party;
import models.UserLogin;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class DepartOrderController extends Controller {

	private Logger logger = Logger.getLogger(DepartOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();


	public void index() {
		if (LoginUserController.isAuthenticated(this))
			render("departOrder/departOrderList.html");
	} 

	public void list() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total FROM DEPART_ORDER do "
				+ " left join party p on do.notify_party_id = p.id "
				+ " left join contact c on p.contact_id = c.id "
				+ " left join depart_transfer dt on do.id = dt.depart_id where combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"'";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));   

		String sql = "SELECT do.*,c.contact_person,c.phone, (select group_concat(tr.ORDER_NO separator '\r\n') FROM TRANSFER_ORDER tr where tr.id in(select ORDER_ID from DEPART_TRANSFER dt where dt.DEPART_ID=do.id ))  as TRANSFER_ORDER_NO  FROM DEPART_ORDER do "
				+ " left join party p on do.notify_party_id = p.id "
				+ " left join contact c on p.contact_id = c.id where combine_type = '"+DepartOrder.COMBINE_TYPE_DEPART+"'";

		List<Record> warehouses = Db.find(sql);

		Map map = new HashMap();
		map.put("sEcho", pageIndex);
		map.put("iTotalRecords", rec.getLong("total"));
		map.put("iTotalDisplayRecords", rec.getLong("total"));

		map.put("aaData", warehouses);

		renderJson(map);
	}
//添加编辑
	public void add() {
		if(getPara()==null){
			if (LoginUserController.isAuthenticated(this))
			
				render("/yh/departOrder/allTransferOrderList.html");
		}else{
			String sql="SELECT do.*,co.CONTACT_PERSON,co.phone,u.USER_NAME,(select group_concat(dt.ORDER_ID  separator',')  FROM DEPART_TRANSFER  dt "
					+ "where dt.DEPART_ID =do.id)as order_id FROM DEPART_ORDER  do "
					+ "left join CONTACT co on co.id in( SELECT p.CONTACT_ID  FROM PARTY p where p.id=do.NOTIFY_PARTY_ID ) "
					+ "left join USER_LOGIN  u on u.id=do.CREATE_BY where do.COMBINE_TYPE ='DEPART' and do.id in("+Integer.parseInt(getPara())+")";
			DepartOrder depar=DepartOrder.dao.findFirst(sql);
			setAttr("type","many");
			setAttr("depart_id",getPara());
			setAttr("localArr",depar.get("order_id"));
			setAttr("depart",depar);
			if (LoginUserController.isAuthenticated(this))
				render("departOrder/editTransferOrder.html");
		}
		
	}
	

	public void createTransferOrderList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from transfer_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select * from transfer_order order by create_stamp desc";

		List<Record> transferOrders = Db.find(sql);

		Map transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);

		renderJson(transferOrderListMap);
	}
		//allTranferOrderList 创建发车单
	public void addDepartOrder() {
		String list = this.getPara("localArr");
		int lang=list.length();
		if(lang>1){
			setAttr("type","many");
		}else{
			setAttr("type","one");
		}
		String name = (String) currentUser.getPrincipal();
		setAttr("creat",name);
		setAttr("localArr",list);
		
		if (LoginUserController.isAuthenticated(this))
		render("departOrder/editTransferOrder.html");
	}
	//editTransferOrder 初始数据
	public void getIintDepartOrderItems(){
		// 构造前台参数
		String idlist = getPara("localArr");		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from TRANSFER_ORDER_ITEM tof"
				+ " left join TRANSFER_ORDER  or  on tof.ORDER_ID =or.id "
				+ " left join CONTACT c on c.id in (select contact_id from party p where or.customer_id=p.id)"
				+ " where tof.ORDER_ID in("+idlist+")";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "SELECT tof.* ,or.ORDER_NO as order_no,c.COMPANY_NAME as customer  FROM TRANSFER_ORDER_ITEM tof"
				+ " left join TRANSFER_ORDER  or  on tof.ORDER_ID =or.id "
				+ "left join CONTACT c on c.id in (select contact_id from party p where or.customer_id=p.id)"
				+ " where tof.ORDER_ID in("+idlist+")  order by c.id"+sLimit;
		List<Record> departOrderitem = Db.find(sql);
		for(int i=0;i<departOrderitem.size();i++){
			String itemname=departOrderitem.get(i).get("item_name");
			if("ATM".equals(itemname)){
				Long itemid= departOrderitem.get(i).get("id");
				String sql2="SELECT SERIAL_NO  FROM TRANSFER_ORDER_ITEM_DETAIL  where ITEM_ID ="+itemid;
				List<Record> itemserial_no = Db.find(sql2);
				String itemno=itemserial_no.get(0).get("SERIAL_NO");
					departOrderitem.get(i).set("serial_no", itemno);
			}else{
				departOrderitem.get(i).set("serial_no", "无");
			}
		}

		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));

		Map.put("aaData", departOrderitem);

		renderJson(Map);
	
	}
	//构造单号
	 public String creat_order_no() {
	    	String order_no = null;
	    	String the_order_no=null;
	    	DepartOrder order = DepartOrder.dao.findFirst("select * from DEPART_ORDER where  COMBINE_TYPE= '"+DepartOrder.COMBINE_TYPE_DEPART+"' order by DEPART_no desc limit 0,1");
	        if (order != null) {
	            String num = order.get("DEPART_no");
	            String str = num.substring(2, num.length());
	            System.out.println(str);
	            Long oldTime = Long.parseLong(str);
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	            String format = sdf.format(new Date());
	            String time = format + "00001";
	            Long newTime = Long.parseLong(time);
	            if(oldTime >= newTime){
	            	order_no = String.valueOf((oldTime + 1));
	            }else{
	            	order_no = String.valueOf(newTime);
	            }
	            the_order_no="FC"+order_no;
	        } else {
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	            String format = sdf.format(new Date());
	            order_no = format + "00001";
	           the_order_no = "FC"+order_no;
	        }
	        return the_order_no;
	 }
	 
	 // 查找客户
	    public void searchCustomer() {
	        String input = getPara("input");
	        List<Record> locationList = Collections.EMPTY_LIST;
	        if (input.trim().length() > 0) {
	            locationList = Db
	                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'NOTIFY_PARTY' and (company_name like '%"
	                            + input
	                            + "%' or contact_person like '%"
	                            + input
	                            + "%' or email like '%"
	                            + input
	                            + "%' or mobile like '%"
	                            + input
	                            + "%' or phone like '%"
	                            + input
	                            + "%' or address like '%"
	                            + input
	                            + "%' or postal_code like '%"
	                            + input
	                            + "%') limit 0,10");
	        }
	        renderJson(locationList);
	    }

	//发车单保存
	public void savedepartOrder(){
		String	depart_no=creat_order_no();
		String depart_id=getPara("depart_id");
		String name = (String) currentUser.getPrincipal();
        UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
       String creat_id=users.get("id").toString();
		String order_id2=this.getPara("orderid");
		String[] order_id=this.getPara("orderid").split(",");
		String party_id=getPara("driverid");
		 Date createDate = Calendar.getInstance().getTime();
			if("".equals(getPara("driverid"))){
				
					Contact con=new Contact();
					con.set("phone", getPara("phone")).set("CONTACT_PERSON", getPara("customerMessage")).save();
					Long con_id=con.get("id");
					Party pt=new Party();
					pt.set("PARTY_TYPE", "NOTIFY_PARTY").set("CONTACT_ID", con_id).save();
					party_id=pt.get("id").toString();

			}
			DepartOrder dp=DepartOrder.dao.set("CREATE_BY",Integer.parseInt(creat_id))
					.set("create_stamp", createDate).set("combine_type", "DEPART")
					.set("car_no", getPara("car_no")).set("car_type", getPara("cartype"))
					.set("depart_no",depart_no ).set("notify_party_id",Integer.parseInt(party_id))
					.set("car_size",getPara("carsize"));
			if("".equals(depart_id)){
				dp.save();
				 setAttr("depart_id","no");
			}else{
				
				 dp.findById(Integer.parseInt(depart_id));
				dp.update();
				 setAttr("depart_id",depart_id);
			}
				
					
		//DepartOrder der=DepartOrder.dao.findFirst("SELECT * FROM DEPART_ORDER where DEPART_NO  ='"+depart_no+"'");
		int de_id=Integer.parseInt(dp.get("id").toString());
		
		for(int i=0;i<order_id.length;i++){
			DepartTransferOrder dt=new DepartTransferOrder();
			dt.set("depart_id",de_id).set("ORDER_ID",order_id[i]).set("TRANSFER_ORDER_NO",order_id[i]).save();
		}
		int lang=order_id.length;
		if(lang>1){
			setAttr("type","many");
		}else{
			setAttr("type","one");
		}
		setAttr("creat",name);
		setAttr("localArr",order_id2);
		
		if (LoginUserController.isAuthenticated(this))
		render("departOrder/editTransferOrder.html");
	}
	//货品明细
	public void itemDetailList(){
		String item_id = getPara("item_id");		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from TRANSFER_ORDER_ITEM_DETAIL  where ITEM_ID ="+item_id+"";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "SELECT * FROM TRANSFER_ORDER_ITEM_DETAIL  where ITEM_ID ="+item_id+""+sLimit;
		List<Record> itemdetail = Db.find(sql);
		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));

		Map.put("aaData", itemdetail);

		renderJson(Map);
	}
	
}
