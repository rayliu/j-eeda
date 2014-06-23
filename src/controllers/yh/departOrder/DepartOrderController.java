package controllers.yh.departOrder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartOrderItemdetail;
import models.DepartTransferOrder;
import models.Party;
import models.TransferOrderItemDetail;
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
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total FROM DEPART_ORDER do "
				+ " left join party p on do.driver_id = p.id  and p.party_type = '"+Party.PARTY_TYPE_DRIVER+"'" 
				+ " left join contact c on p.contact_id = c.id "
				+ " where combine_type = '"
				+ DepartOrder.COMBINE_TYPE_DEPART + "'";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "SELECT do.*,c.contact_person,c.phone, (select group_concat(tr.ORDER_NO separator '\r\n') FROM TRANSFER_ORDER tr where tr.id in(select ORDER_ID from DEPART_TRANSFER dt where dt.DEPART_ID=do.id ))  as TRANSFER_ORDER_NO  FROM DEPART_ORDER do "
				+ " left join party p on do.driver_id = p.id and p.party_type = '"+Party.PARTY_TYPE_DRIVER+"'"
				+ " left join contact c on p.contact_id = c.id where combine_type = '"
				+ DepartOrder.COMBINE_TYPE_DEPART + "'";

		List<Record> warehouses = Db.find(sql);

		Map map = new HashMap();
		map.put("sEcho", pageIndex);
		map.put("iTotalRecords", rec.getLong("total"));
		map.put("iTotalDisplayRecords", rec.getLong("total"));

		map.put("aaData", warehouses);

		renderJson(map);
	}

	// 添加编辑
	public void add() {
		if (getPara() == null) {
			if (LoginUserController.isAuthenticated(this))

				render("/yh/departOrder/allTransferOrderList.html");
		} else {
			String sql = "SELECT do.*,co.CONTACT_PERSON,co.phone,u.USER_NAME,(select group_concat(dt.ORDER_ID  separator',')  FROM DEPART_TRANSFER  dt "
					+ "where dt.DEPART_ID =do.id)as order_id FROM DEPART_ORDER  do "
					+ "left join CONTACT co on co.id in( SELECT p.CONTACT_ID  FROM PARTY p where p.id=do.DRIVER_ID ) "
					+ "left join USER_LOGIN  u on u.id=do.CREATE_BY where do.COMBINE_TYPE ='DEPART' and do.id in("
					+ Integer.parseInt(getPara()) + ")";
			DepartOrder depar = DepartOrder.dao.findFirst(sql);
			setAttr("type", "many");
			setAttr("depart_id", getPara());
			setAttr("localArr", depar.get("order_id"));
			setAttr("depart", depar);
			if (LoginUserController.isAuthenticated(this))
				render("departOrder/editTransferOrder.html");
		}

	}

	public void createTransferOrderList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total  from transfer_order to "
					+" left join party p on to.customer_id = p.id "
					+" left join contact c on p.contact_id = c.id "
					+" left join location l1 on to.route_from = l1.code "
					+" left join location l2 on to.route_to = l2.code"
					+" where to.status not in ('已入库','已签收') and arrival_mode not in ('delivery')";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select to.id,to.order_no,to.cargo_nature,"
					+" (select sum(toi.weight) from transfer_order_item toi where toi.order_id = to.id) as total_weight,"  
					+" (select sum(toi.volume) from transfer_order_item toi where toi.order_id = to.id) as total_volumn," 
					+" (select sum(toi.amount) from transfer_order_item toi where toi.order_id = to.id) as total_amount," 
					+" to.address,to.pickup_mode,to.status,c.company_name cname,"
					+" l1.name route_from,l2.name route_to,to.CREATE_STAMP from transfer_order to "
					+" left join party p on to.customer_id = p.id "
					+" left join contact c on p.contact_id = c.id "
					+" left join location l1 on to.route_from = l1.code "
					+" left join location l2 on to.route_to = l2.code"
					+" where to.status not in ('已入库','已签收') and arrival_mode not in ('delivery')"
					+" order by to.CREATE_STAMP desc";

		List<Record> transferOrders = Db.find(sql);

		Map transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);

		renderJson(transferOrderListMap);
	}

	// allTranferOrderList 创建发车单
	public void addDepartOrder() {
		String list = getPara("localArr");
		int lang = list.length();
		if (lang > 1) {
			setAttr("type", "many");
		} else {
			setAttr("type", "one");
		}
		String name = (String) currentUser.getPrincipal();
		setAttr("creat", name);
		setAttr("localArr", list);

		if (LoginUserController.isAuthenticated(this))
			render("departOrder/editTransferOrder.html");
	}

	// editTransferOrder 初始数据
	public void getIintDepartOrderItems() {
		String order_id = getPara("localArr");//运输单id
		String tr_item=getPara("tr_item");//货品id
		String item_detail=getPara("item_detail");//单品id
		
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}
		String sqlTotal = "select count(1) total from TRANSFER_ORDER_ITEM tof"
				+ " left join TRANSFER_ORDER  or  on tof.ORDER_ID =or.id "
				+ " left join CONTACT c on c.id in (select contact_id from party p where or.customer_id=p.id)"
				+ " where tof.ORDER_ID in(" + order_id + ")";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "SELECT tof.* ,or.ORDER_NO as order_no,or.id as tr_order_id,c.COMPANY_NAME as customer  FROM TRANSFER_ORDER_ITEM tof"
				+ " left join TRANSFER_ORDER  or  on tof.ORDER_ID =or.id "
				+ "left join CONTACT c on c.id in (select contact_id from party p where or.customer_id=p.id)"
				+ " where tof.ORDER_ID in(" + order_id + ")  order by c.id" + sLimit;
		String sql_dp_detail="SELECT * FROM DEPART_TRANSFER_ITEMDETAIL";
		List<Record> departOrderitem = Db.find(sql);
		List<Record> depart_item_detail = Db.find(sql_dp_detail);
		for(int i=0;i<departOrderitem.size();i++){
			for(int j=0;j<depart_item_detail.size();j++ ){
				if(departOrderitem.get(i).get("ORDER_ID").equals(depart_item_detail.get(j).get("ORDER_ID")) 
						&& departOrderitem.get(i).get("ID").equals(depart_item_detail.get(j).get("ITEM_ID"))){
				double amount=Double.parseDouble(departOrderitem.get(i).get("AMOUNT").toString());
				amount--;
				departOrderitem.get(i).set("AMOUNT",amount);
				}
			}
		}
		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));
		Map.put("aaData", departOrderitem);
		renderJson(Map);
	}

	// 构造单号
	public String creat_order_no() {
		String order_no = null;
		String the_order_no = null;
		DepartOrder order = DepartOrder.dao.findFirst("select * from DEPART_ORDER where  COMBINE_TYPE= '"
				+ DepartOrder.COMBINE_TYPE_DEPART + "' order by DEPART_no desc limit 0,1");
		if (order != null) {
			String num = order.get("DEPART_no");
			String str = num.substring(2, num.length());
			System.out.println(str);
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
			the_order_no = "FC" + order_no;
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String format = sdf.format(new Date());
			order_no = format + "00001";
			the_order_no = "FC" + order_no;
		}
		return the_order_no;
	}

	// 查找客户
	public void searchCustomer() {
		String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'DRIVER' and (company_name like '%"
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
							+ input + "%' or postal_code like '%" + input + "%') limit 0,10");
		}
		renderJson(locationList);
	}

	// 发车单保存
	public void savedepartOrder() {
		String depart_no = creat_order_no();//构造发车单号
		String depart_id = getPara("depart_id");//发车单id
		String name = (String) currentUser.getPrincipal();
		//查找创建人id
		UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
		String creat_id = users.get("id").toString();//创建人id
		String party_id = getPara("driverid");//司机id
		String order_id2 = this.getPara("orderid");//运输单id
		String[] order_id = this.getPara("orderid").split(",");//运输单id
		String[] item_detail = this.getPara("item_detail").split(",");//单品id
		String[] tr_itemid = this.getPara("tr_itemid_list").split(",");//货品id
		Date createDate = Calendar.getInstance().getTime();
		//如果司机id=“”,插入新司机
		if ("".equals(getPara("driverid"))) {
			Contact con = new Contact();
			con.set("phone", getPara("phone")).set("CONTACT_PERSON", getPara("customerMessage")).save();
			Long con_id = con.get("id");
			Party pt = new Party();
			pt.set("PARTY_TYPE", "DRIVER").set("CONTACT_ID", con_id).save();
			party_id = pt.get("id").toString();
		}
		DepartOrder dp = null;
		//如果发车单id="",新建发车单
		String item_id="SELECT order_id , id as item_id FROM TRANSFER_ORDER_ITEM  where order_id in ("+order_id2+")";//货品表查询货品id
		String item_detail_id="SELECT order_id, item_id  FROM TRANSFER_ORDER_ITEM_DETAIL  where order_id in("+order_id2+")  group by item_id";//明细表查询货品id
		List<Record> item_idlist=Db.find(item_id);
		List<Record> item_detail_idlist=Db.find(item_detail_id);
		if ("".equals(depart_id)) {
		
				dp = new DepartOrder();
				dp.set("CREATE_BY", Integer.parseInt(creat_id)).set("create_stamp", createDate)
				.set("combine_type", "DEPART").set("car_no", getPara("car_no")).set("car_type", getPara("cartype"))
				.set("depart_no", depart_no).set("DRIVER_ID", Integer.parseInt(party_id))
				.set("car_size", getPara("carsize")).set("remark", getPara("remark"));
				dp.save();
				if("".equals(getPara("item_detail"))){
				//保存没单品的货品到发车单货品表
			String tr_item_detail="SELECT * FROM TRANSFER_ORDER_ITEM_DETAIL where order_id in ("+order_id2+")";//查询单品id
			List<Record> tr_item_list=Db.find(item_id);
			List<Record> item_detail_list=Db.find(tr_item_detail);
			item_idlist.removeAll(item_detail_idlist);
			//没勾选单品
			if(item_idlist.size()>0){
				for(int i=0;i<item_idlist.size();i++){
					DepartOrderItemdetail deid=new DepartOrderItemdetail();
					deid.set("order_id",Integer.parseInt(item_idlist.get(i).get("order_id").toString()))
					.set("item_id",Integer.parseInt(item_idlist.get(i).get("item_id").toString()))
					.set("depart_id",Integer.parseInt(dp.get("id").toString())).save();
					for(int k=0;k<tr_item_list.size();k++){
						if(tr_item_list.get(k).get("item_id").equals(item_idlist.get(i).get("item_id"))){
							tr_item_list.remove(k);
						}
					}
				}
			}
			
			for(int h=0;h<tr_item_list.size();h++){
				for(int j=0;j<item_detail_list.size();j++){
					if(tr_item_list.get(h).get("item_id").equals(item_detail_list.get(j).get("item_id"))){
						DepartOrderItemdetail dei=new DepartOrderItemdetail();
						dei.set("order_id",Integer.parseInt(tr_item_list.get(h).get("order_id").toString()))
						.set("item_id",Integer.parseInt(tr_item_list.get(h).get("item_id").toString()))
						.set("itemdetail_id", Integer.parseInt(item_detail_list.get(j).get("id").toString()))
						.set("depart_id",Integer.parseInt(dp.get("id").toString())).save();
					}
				}
			}
			}else{
				//勾选了单品
				item_idlist.removeAll(item_detail_idlist);
				if(item_idlist.size()>0){
					for(int i=0;i<item_idlist.size();i++){
						String sql_dp_detail="SELECT * FROM DEPART_TRANSFER_ITEMDETAIL where item_id="+Integer.parseInt(item_idlist.get(i).get("item_id").toString());
						DepartOrderItemdetail dep=DepartOrderItemdetail.dao.findFirst(sql_dp_detail);
						if(dep==null){
							DepartOrderItemdetail deid=new DepartOrderItemdetail();
							deid.set("order_id",Integer.parseInt(item_idlist.get(i).get("order_id").toString()))
							.set("item_id",Integer.parseInt(item_idlist.get(i).get("item_id").toString()))
							.set("depart_id",Integer.parseInt(dp.get("id").toString())).save();
						}
						
					}
				}
				if(!"".equals(getPara("item_detail")) && !"".equals(getPara("tr_itemid_list")) ){
				for(int k=0;k<order_id.length;k++){//运输单id
					for(int i=0; i<tr_itemid.length;i++){//货品id
						for(int j=0;j<item_detail.length;j++){//单品id
							TransferOrderItemDetail tr_item_de=TransferOrderItemDetail.dao.findById(Integer.parseInt(item_detail[j]));
						if(tr_item_de.get("ITEM_ID").toString().equals(tr_itemid[i].toString())&&
							tr_item_de.get("ORDER_ID").toString().equals(order_id[k].toString())){
							DepartOrderItemdetail de_item_detail=new DepartOrderItemdetail(); 
							de_item_detail.set("DEPART_ID",Integer.parseInt(dp.get("id").toString())).set("ORDER_ID",Integer.parseInt(order_id[k]))
							.set("ITEM_ID",Integer.parseInt(tr_itemid[i])).set("ITEMDETAIL_ID",Integer.parseInt(item_detail[j])).save();
							}
						}
					}
					}
					setAttr("item_detail", getPara("item_detail"));
					setAttr("tr_itemid", getPara("tr_itemid_list"));
				}
			}
	
		} else {//编辑发车单
			dp = DepartOrder.dao.findById(Integer.parseInt(depart_id));
			dp.set("CREATE_BY", Integer.parseInt(creat_id)).set("create_stamp", createDate)
			.set("combine_type", "DEPART").set("car_no", getPara("car_no")).set("car_type", getPara("cartype"))
			.set("depart_no", depart_no).set("DRIVER_ID", Integer.parseInt(party_id))
			.set("car_size", getPara("carsize")).set("remark", getPara("remark"));
			dp.update();
			if(!"".equals(getPara("item_detail")) && !"".equals(getPara("tr_itemid_list")) ){
			String sql="SELECT * FROM DEPART_TRANSFER_ITEMDETAIL where DEPART_ID="+depart_id;
			List<DepartOrderItemdetail> doitsize=DepartOrderItemdetail.dao.find(sql);
			for(int f=0;f<doitsize.size();f++){
			for(int k=0;k<order_id.length;k++){//运输单id
				for(int i=0; i<tr_itemid.length;i++){//货品id
					for(int j=0;j<item_detail.length;j++){//单品id
						TransferOrderItemDetail tr_item_de=TransferOrderItemDetail.dao.findById(Integer.parseInt(item_detail[j]));
					if(tr_item_de.get("ITEM_ID").toString().equals(tr_itemid[i].toString())&&
						tr_item_de.get("ORDER_ID").toString().equals(order_id[k].toString())){
						DepartOrderItemdetail de_item_detail=DepartOrderItemdetail.dao.findById(Integer.parseInt(doitsize.get(f).get("id").toString())); 
						de_item_detail.set("DEPART_ID",Integer.parseInt(dp.get("id").toString())).set("ORDER_ID",Integer.parseInt(order_id[k]))
						.set("ITEM_ID",Integer.parseInt(tr_itemid[i])).set("ITEMDETAIL_ID",Integer.parseInt(item_detail[j])).update();
						}
					}
				}
				}
			}
			}
			setAttr("depart_id", depart_id);
		}
		int de_id = Integer.parseInt(dp.get("id").toString());//获取发车单id
		//根据勾选了多少个运输单，循环插入发车运输单中间表
		for (int i = 0; i < order_id.length; i++) {
			DepartTransferOrder dt = new DepartTransferOrder();
			dt.set("depart_id", de_id).set("ORDER_ID", order_id[i]).set("TRANSFER_ORDER_NO", order_id[i]).save();
		}
		//根据运输单个数，判断发车单类型
		int lang = order_id.length;
		if (lang > 1) {
			setAttr("type", "many");
		} else {
			setAttr("type", "one");
		}
		setAttr("creat", name);//创建人
		setAttr("localArr", order_id2);//运输单id,回显货品table
		//返回编辑发车单页面
		if (LoginUserController.isAuthenticated(this))
			render("departOrder/editTransferOrder.html");
	}

	// 点击货品table的查看 ，显示对应货品的单品
	public void itemDetailList() {
		String item_id = getPara("item_id");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from TRANSFER_ORDER_ITEM_DETAIL  where ITEM_ID =" + item_id + "";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		/*String sql_item="SELECT * FROM DEPART_TRANSFER_ITEMDETAIL where ITEM_ID="+item_id;*/
		
		String sql_tr_itemdetail_id="SELECT id as ITEMDETAIL_ID  FROM TRANSFER_ORDER_ITEM_DETAIL where ITEM_ID="+item_id;
		String sql_dp_itemdetail_id="SELECT ITEMDETAIL_ID  FROM DEPART_TRANSFER_ITEMDETAIL  where ITEM_ID="+item_id;
		List<Record> sql_tr_itemdetail_idlist=Db.find(sql_tr_itemdetail_id);
		List<Record> sql_dp_itemdetail_idlist=Db.find(sql_dp_itemdetail_id);
		/*List<Record> depart_itemdetail=Db.find(sql_item);*/
		sql_tr_itemdetail_idlist.removeAll(sql_dp_itemdetail_idlist);
		String  detail_id="0";
		if(sql_tr_itemdetail_idlist.size()>0){
			for(int i=0;i<sql_tr_itemdetail_idlist.size();i++){
				detail_id +=sql_tr_itemdetail_idlist.get(i).get("ITEMDETAIL_ID")+",";
			}
			detail_id=detail_id.substring(0,detail_id.length()-1);
		}
		String sql = "SELECT * FROM TRANSFER_ORDER_ITEM_DETAIL  where ID in("+ detail_id+")" ;
		List<Record> itemdetail = Db.find(sql);
		Map Map = new HashMap();
		Map.put("sEcho", pageIndex);
		Map.put("iTotalRecords", rec.getLong("total"));
		Map.put("iTotalDisplayRecords", rec.getLong("total"));
		Map.put("aaData", itemdetail);
		renderJson(Map);
	}
	// 取消
		public void cancel() {
			String id = getPara();
			String sql="SELECT * FROM DEPART_TRANSFER  where DEPART_ID ="+id+"";
			List<DepartTransferOrder> re_tr=DepartTransferOrder.dao.find(sql);
			for(int i=0;i<re_tr.size();i++){
				DepartTransferOrder dep_tr=DepartTransferOrder.dao.findFirst(sql);
				dep_tr.set("DEPART_ID", null);
				dep_tr.delete();
			}
			DepartOrder re = DepartOrder.dao.findById(id);
			re.set("driver_id", null).update();
			re.delete();
			render("departOrder/departOrderList.html");
		}

}
