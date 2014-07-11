package controllers.yh.departOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.DepartOrderItemdetail;
import models.DepartTransferOrder;
import models.Location;
import models.Party;
import models.TransferOrder;
import models.TransferOrderItemDetail;
import models.TransferOrderMilestone;
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

    public void onTrip() {
        if (LoginUserController.isAuthenticated(this))
            render("departOrder/departOrderOnTripList.html");
    }

    public void list() {
    	
     String orderNo=getPara("orderNo");
     String departNo=getPara("departNo");
     String status=getPara("status");
     String sp=	getPara("sp");
     String beginTime=getPara("beginTime");
     String endTime=getPara("endTime");
   
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from depart_order deo "
                + " left join party p on deo.driver_id = p.id  and p.party_type = '" + Party.PARTY_TYPE_DRIVER + "'"
                + " left join contact c on p.contact_id = c.id " + " where combine_type = '" + DepartOrder.COMBINE_TYPE_DEPART + "'";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select deo.*,c.contact_person,c.phone, (select group_concat(tr.order_no separator '\r\n') from transfer_order tr where tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id ))  as transfer_order_no  from depart_order deo "
                + " left join party p on deo.driver_id = p.id and p.party_type = '"
                + Party.PARTY_TYPE_DRIVER
                + "'"
                + " left join contact c on p.contact_id = c.id where  ifnull(deo.status,'') != 'aa'  and combine_type = '"
                + DepartOrder.COMBINE_TYPE_DEPART
                + "' order by deo.create_stamp desc";
       
       List<Record> depart = null;
        if(orderNo==null&&departNo==null&&status==null&&sp==null&&beginTime==null&&endTime==null){
        	depart = Db.find(sql);
        }else{
        	 if (beginTime == null || "".equals(beginTime)) {
                 beginTime = "1-1-1";
             }
             if (endTime == null || "".equals(endTime)) {
                 endTime = "9999-12-31";
             }
             String sql_seach ="select deo.*,c.contact_person,c.phone, group_concat(tr.order_no separator ' ') as transfer_order_no "
    				 +" from depart_order deo" 
    				+" left join party p on deo.driver_id = p.id and p.party_type = 'DRIVER' " 
    				+" left join contact c on p.contact_id = c.id "
    				+" left join transfer_order tr  on tr.id in(select order_id from depart_transfer dt where dt.depart_id=deo.id )" 
    				+"  where deo.combine_type = 'DEPART' and ifnull(deo.status,'') like '%"+status+"%' and ifnull(deo.depart_no,'') like '%"+departNo+"%' and ifnull(c.company_name,'')  like '%"+sp+"%' and ifnull(tr.order_no,'') like '%"+orderNo+"%'"
    				+ " and deo.create_stamp between '"+beginTime+"' and '"+endTime+"'group by deo.id order by deo.create_stamp desc ";
        	depart = Db.find(sql_seach);
        }
        Map map = new HashMap();
        map.put("sEcho", pageIndex);
        map.put("iTotalRecords", rec.getLong("total"));
        map.put("iTotalDisplayRecords", rec.getLong("total"));

        map.put("aaData", depart);

        renderJson(map);
    }

    // 添加编辑
    public void add() {
        if (getPara() == null) {
            if (LoginUserController.isAuthenticated(this))

                render("/yh/departOrder/allTransferOrderList.html");
        } else {
            int depart_id = Integer.parseInt(getPara());

            getIintedit(depart_id);
        }

    }

    // 弹窗
    public void boxedit() {
        if (!"".equals(getPara("edit_depart_id"))) {
            getIintedit(Integer.parseInt(getPara("edit_depart_id").toString()));
        }
    }

    public void getIintedit(int depart_id) {
        int edit_depart_id = depart_id;
        String sql = "select deo.*,co.contact_person,co.phone,u.user_name,(select group_concat(dt.order_id  separator',')  from depart_transfer  dt "
                + "where dt.depart_id =deo.id)as order_id from depart_order  deo "
                + "left join contact co on co.id in( select p.contact_id  from party p where p.id=deo.driver_id ) "
                + "left join user_login  u on u.id=deo.create_by where deo.combine_type ='DEPART' and deo.id in(" + edit_depart_id + ")";
        DepartOrder depar = DepartOrder.dao.findFirst(sql);
        String routeFrom = depar.get("route_from");
        Location locationFrom = null;
        if (routeFrom != null || !"".equals(routeFrom)) {
            List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
            Location l = Location.dao.findFirst("select * from location where code = (select pcode from location where code = '"
                    + routeFrom + "')");
            if (provinces.contains(l)) {
                locationFrom = Location.dao
                        .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                + routeFrom + "'");
            } else {
                locationFrom = Location.dao
                        .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                + routeFrom + "'");
            }
            setAttr("locationFrom", locationFrom);
        }

        String routeTo = depar.get("route_to");
        Location locationTo = null;
        if (routeTo != null || !"".equals(routeTo)) {
            List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
            Location l = Location.dao.findFirst("select * from location where code = (select pcode from location where code = '" + routeTo
                    + "')");
            if (provinces.contains(l)) {
                locationTo = Location.dao
                        .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                + routeTo + "'");
            } else {
                locationTo = Location.dao
                        .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                + routeTo + "'");
            }
            setAttr("locationTo", locationTo);
        }
        setAttr("type", "many");
        setAttr("depart_id", getPara());
        setAttr("localArr", depar.get("order_id").toString());
        setAttr("depart", depar);
        setAttr("depart_id", depart_id);
        if (LoginUserController.isAuthenticated(this))
            render("departOrder/editTransferOrder.html");

    }

    public void createTransferOrderList() {
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String address = getPara("address");
        String customer = getPara("customer");
        String routeFrom = getPara("routeFrom");
        String routeTo = getPara("routeTo");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String sLimit = "";
        List<Record> transferOrders = null;
        Record rec = null;
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        if (orderNo == null && status == null && address == null && customer == null && routeFrom == null && routeTo == null
                && beginTime == null && endTime == null) {
            String sqlTotal = "select count(1) total  from transfer_order tor " + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code" + " where tor.status = '已入货场'";
            rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));
            String sql = "select tor.id,tor.order_no,tor.cargo_nature,"
                    + " (select sum(toi.weight) from transfer_order_item toi where toi.order_id = tor.id) as total_weight,"
                    + " (select sum(toi.volume) from transfer_order_item toi where toi.order_id = tor.id) as total_volumn,"
                    + " (select sum(toi.amount) from transfer_order_item toi where toi.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.status,c.company_name cname,"
                    + " l1.name route_from,l2.name route_to,tor.create_stamp ,cont.company_name as spname,cont.id as spid from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + "left join contact cont on  cont.id=tor.sp_id " + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code" + " where tor.status = '已入货场'" + " order by tor.create_stamp desc";
            transferOrders = Db.find(sql);
        } else {
            if (beginTime == null || "".equals(beginTime)) {
                beginTime = "1-1-1";
            }
            if (endTime == null || "".equals(endTime)) {
                endTime = "9999-12-31";
            }

            String sqlTotal = "select count(1) total from transfer_order tor " + " left join party p on tor.customer_id = p.id "
                    + " left join contact c on p.contact_id = c.id " + " left join location l1 on tor.route_from = l1.code "
                    + " left join location l2 on tor.route_to = l2.code  "
                    + " where tor.status = '已入货场' and arrival_mode !='delivery' and isnull(tor.assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'" + " and l1.name like '%" + routeFrom + "%' and l2.name like '%" + routeTo
                    + "%' and tor.order_no like '%" + orderNo + "%' and tor.status like '%" + status + "%' and tor.address like '%"
                    + address + "%' and c.company_name like '%" + customer + "%' and create_stamp between '" + beginTime + "' and '"
                    + endTime + "'";
            rec = Db.findFirst(sqlTotal);
            logger.debug("total records:" + rec.getLong("total"));

            String sql = "select tor.id,tor.order_no,tor.cargo_nature,"
                    + " (select sum(tori.weight) from transfer_order_item tori where tori.order_id = tor.id) as total_weight,"
                    + " (select sum(tori.volume) from transfer_order_item tori where tori.order_id = tor.id) as total_volumn,"
                    + " (select sum(tori.amount) from transfer_order_item tori where tori.order_id = tor.id) as total_amount,"
                    + " tor.address,tor.pickup_mode,tor.status,c.company_name cname,"
                    + " (select name from location where code = tor.route_from) route_from,(select name from location where code = tor.route_to) route_to,tor.create_stamp,tor.assign_status from transfer_order tor "
                    + " left join party p on tor.customer_id = p.id " + " left join contact c on p.contact_id = c.id "
                    + " left join location l1 on tor.route_from = l1.code " + " left join location l2 on tor.route_to = l2.code  "
                    + " where tor.status ='已入货场' and arrival_mode !='delivery' and isnull(tor.assign_status, '') !='"
                    + TransferOrder.ASSIGN_STATUS_ALL + "'" + " and l1.name like '%" + routeFrom + "%' and l2.name like '%" + routeTo
                    + "%' and tor.order_no like '%" + orderNo + "%' and tor.status like '%" + status + "%' and tor.address like '%"
                    + address + "%' and c.company_name like '%" + customer + "%' and create_stamp between '" + beginTime + "' and '"
                    + endTime + "'" + " order by tor.create_stamp desc";
            transferOrders = Db.find(sql);
        }
        for (int i = 0; i < transferOrders.size();) {
            /* 运输单查询货品、单品个数 */
            int detail_total = 0;// 运输单货品/单品数
            String total_tr_item = "select * from transfer_order_item  where order_id="
                    + Integer.parseInt(transferOrders.get(i).get("id").toString());// 货品id
            List<Record> tr_itemlist = Db.find(total_tr_item);
            for (int j = 0; j < tr_itemlist.size(); j++) {
                String total_tr_detail = "select * from transfer_order_item_detail  where item_id="
                        + Integer.parseInt(tr_itemlist.get(j).get("id").toString());// 单品id
                List<Record> tr_detaillist = Db.find(total_tr_detail);
                if (tr_detaillist.size() > 0) {
                    detail_total = detail_total + tr_detaillist.size();
                } else {
                    detail_total++;
                }
            }
            /* 发车单查询货品，单品个数 */
            int depart_detail = 0;
            String total_depart_detail = "select * from depart_transfer_itemdetail  where order_id="
                    + Integer.parseInt(transferOrders.get(i).get("id").toString());// 发车单单品个数
            List<Record> depart_detaillist = Db.find(total_depart_detail);
            depart_detail = depart_detaillist.size();
            if (depart_detail == detail_total) {
                transferOrders.remove(i);
                i = i - i;
                continue;
            }
            i++;
        }

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
        String depart_no = creat_order_no();// 获取发车单号     
        String name = (String) currentUser.getPrincipal();
        setAttr("creat", name);
        setAttr("getIin_status", "新建");
        setAttr("localArr", list);
        setAttr("getIin_depart_no", depart_no);

        if (LoginUserController.isAuthenticated(this))
            render("departOrder/editTransferOrder.html");
    }

    // editTransferOrder 初始数据
    public void getIintDepartOrderItems() {
        String order_id = getPara("localArr");// 运输单id

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select count(1) total from transfer_order_item tof left join transfer_order  oro  on tof.order_id =oro.id "
                + " left join contact c on c.id in (select contact_id from party p where oro.customer_id=p.id)" + " where tof.order_id in("
                + order_id + ")";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select tof.* ,oro.order_no as order_no,oro.id as tr_order_id,c.company_name as customer  from transfer_order_item tof"
                + " left join transfer_order  oro  on tof.order_id =oro.id "
                + "left join contact c on c.id in (select contact_id from party p where oro.customer_id=p.id)"
                + " where tof.order_id in("
                + order_id + ")  order by c.id" + sLimit;
        String sql_dp_detail = "select * from depart_transfer_itemdetail";
        List<Record> departOrderitem = Db.find(sql);
        List<Record> depart_item_detail = Db.find(sql_dp_detail);
        for (int i = 0; i < departOrderitem.size(); i++) {
            for (int j = 0; j < depart_item_detail.size(); j++) {
                if (departOrderitem.get(i).get("order_id").equals(depart_item_detail.get(j).get("order_id"))
                        && departOrderitem.get(i).get("ID").equals(depart_item_detail.get(j).get("item_id"))) {
                    double amount = Double.parseDouble(departOrderitem.get(i).get("amount").toString());
                    amount--;
                    departOrderitem.get(i).set("amount", amount);
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
        DepartOrder order = DepartOrder.dao.findFirst("select * from depart_order where  combine_type= '" + DepartOrder.COMBINE_TYPE_DEPART
                + "' order by depart_no desc limit 0,1");
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
            locationList = Db.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
                    + Party.PARTY_TYPE_DRIVER + "' and (company_name like '%" + input + "%' or contact_person like '%" + input
                    + "%' or email like '%" + input + "%' or mobile like '%" + input + "%' or phone like '%" + input
                    + "%' or address like '%" + input + "%' or postal_code like '%" + input + "%') limit 0,10");
        } else {
            locationList = Db.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
                    + Party.PARTY_TYPE_DRIVER + "' limit 0,10");
        }
        renderJson(locationList);
    }

    // 判断是否单据继续创建
    public boolean CheckDepartOrder(String[] order_id) {
        int detail_total = 0;// 运输单货品/单品数
        int depart_detail = 0;// 发车单单品个数
        int last = 0;// 未处理单品个数
        boolean check = false;
        for (int i = 0; i < order_id.length; i++) {
            /* 运输单查询货品、单品个数 */

            String total_tr_item = "select * from transfer_order_item  where order_id=" + Integer.parseInt(order_id[i]);// 货品id
            List<Record> tr_itemlist = Db.find(total_tr_item);
            for (int j = 0; j < tr_itemlist.size(); j++) {
                String total_tr_detail = "select * from transfer_order_item_detail  where item_id="
                        + Integer.parseInt(tr_itemlist.get(j).get("id").toString());// 单品id
                List<Record> tr_detaillist = Db.find(total_tr_detail);
                if (tr_detaillist.size() > 0) {
                    detail_total = detail_total + tr_detaillist.size();
                } else {
                    detail_total++;
                }
            }
            /* 发车单查询货品，单品个数 */
            String total_depart_detail = "select * from depart_transfer_itemdetail  where order_id=" + Integer.parseInt(order_id[i]);// 发车单单品个数
            List<Record> depart_detaillist = Db.find(total_depart_detail);
            depart_detail = depart_detail + depart_detaillist.size();
        }
        last = detail_total - depart_detail;
        if (last > 0) {
            check = true;
        }
        return check;
    }

    // 发车单保存
    public void savedepartOrder() {
        String depart_no = getPara("getIindepart_no").trim();// 车单号
        String depart_id = getPara("depart_id");// 发车单id
        String sp_id = getPara("sp_id");// 供应商id
        String name = (String) currentUser.getPrincipal();
        String house_id=getPara("house_id");//仓库id
        // 查找创建人id
        UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");
        String creat_id = users.get("id").toString();// 创建人id
        String party_id = getPara("driverid");// 司机id
        String car_follow_name = getPara("car_follow_name");// 跟车人
        String car_follow_phone = getPara("car_follow_phone");// 跟车人电话
        String order_id2 = this.getPara("orderid");// 运输单id
        String[] order_id = this.getPara("orderid").split(",");// 运输单id
        String[] item_detail = this.getPara("item_detail").split(",");// 单品id
        String[] tr_itemid = this.getPara("tr_itemid_list").split(",");// 货品id
        Date createDate = Calendar.getInstance().getTime();
        // 如果司机id=“”,插入新司机

        if ("".equals(getPara("driverid"))) {
            Contact con = new Contact();
            con.set("phone", getPara("phone")).set("CONTACT_PERSON", getPara("customerMessage")).save();
            Long con_id = con.get("id");
            Party pt = new Party();
            pt.set("party_type", "DRIVER").set("contact_id", con_id).save();
            party_id = pt.get("id").toString();
        }
        DepartOrder dp = null;
        // 如果发车单id="",新建发车单
        String item_id = "select order_id , id as item_id from transfer_order_item  where order_id in (" + order_id2 + ")";// 货品表查询货品id
        String item_detail_id = "select order_id, item_id  from transfer_order_item_detail  where order_id in(" + order_id2
                + ")  group by item_id";// 明细表查询货品id
        List<Record> item_idlist = Db.find(item_id);
        List<Record> item_detail_idlist = Db.find(item_detail_id);
        if ("".equals(depart_id)) {
            dp = new DepartOrder();
            dp.set("create_by", Integer.parseInt(creat_id)).set("create_stamp", createDate)
                    .set("combine_type", DepartOrder.COMBINE_TYPE_DEPART).set("car_no", getPara("car_no"))
                    .set("car_type", getPara("cartype")).set("depart_no", depart_no).set("driver_id", Integer.parseInt(party_id))
                    .set("car_size", getPara("carsize")).set("remark", getPara("remark"))
                    .set("car_follow_name", getPara("car_follow_name")).set("car_follow_phone", getPara("car_follow_phone"))
                    .set("route_from", getPara("route_from")).set("route_to", getPara("route_to")).set("status", "新建");
            if (!"".equals(sp_id)) {
                dp.set("sp_id", Integer.parseInt(sp_id));
            }
            if (!"".equals(car_follow_name)) {
                dp.set("car_follow_name", car_follow_name);
            }
            if (!"".equals(car_follow_phone)) {
                dp.set("car_follow_phone", car_follow_phone);
            }
            dp.save();
            savePickupOrderMilestone(dp, null);
            if ("".equals(getPara("item_detail"))) {
                // 保存没单品的货品到发车单货品表
                String tr_item_detail = "select * from transfer_order_item_detail where order_id in (" + order_id2 + ")";// 查询单品id
                List<Record> tr_item_list = Db.find(item_id);
                List<Record> item_detail_list = Db.find(tr_item_detail);
                item_idlist.removeAll(item_detail_idlist);
                // 没勾选单品
                if (item_idlist.size() > 0) {
                    for (int i = 0; i < item_idlist.size(); i++) {
                        DepartOrderItemdetail deid = new DepartOrderItemdetail();
                        deid.set("order_id", Integer.parseInt(item_idlist.get(i).get("order_id").toString()))
                                .set("item_id", Integer.parseInt(item_idlist.get(i).get("item_id").toString()))
                                .set("depart_id", Integer.parseInt(dp.get("id").toString())).save();
                        for (int k = 0; k < tr_item_list.size(); k++) {
                            if (tr_item_list.get(k).get("item_id").equals(item_idlist.get(i).get("item_id"))) {
                                tr_item_list.remove(k);
                            }
                        }
                    }
                }
                for (int h = 0; h < tr_item_list.size(); h++) {
                    for (int j = 0; j < item_detail_list.size(); j++) {
                        if (tr_item_list.get(h).get("item_id").equals(item_detail_list.get(j).get("item_id"))) {
                            DepartOrderItemdetail dei = new DepartOrderItemdetail();
                            dei.set("order_id", Integer.parseInt(tr_item_list.get(h).get("order_id").toString()))
                                    .set("item_id", Integer.parseInt(tr_item_list.get(h).get("item_id").toString()))
                                    .set("itemdetail_id", Integer.parseInt(item_detail_list.get(j).get("id").toString()))
                                    .set("depart_id", Integer.parseInt(dp.get("id").toString())).save();
                        }
                    }
                }

            } else {
                // 勾选了单品
                item_idlist.removeAll(item_detail_idlist);
                if (item_idlist.size() > 0) {
                    for (int i = 0; i < item_idlist.size(); i++) {
                        String sql_dp_detail = "select * from depart_transfer_itemdetail where item_id="
                                + Integer.parseInt(item_idlist.get(i).get("item_id").toString());
                        DepartOrderItemdetail dep = DepartOrderItemdetail.dao.findFirst(sql_dp_detail);
                        if (dep == null) {
                            DepartOrderItemdetail deid = new DepartOrderItemdetail();
                            deid.set("order_id", Integer.parseInt(item_idlist.get(i).get("order_id").toString()))
                                    .set("item_id", Integer.parseInt(item_idlist.get(i).get("item_id").toString()))
                                    .set("depart_id", Integer.parseInt(dp.get("id").toString())).save();
                        }

                    }
                }
                if (!"".equals(getPara("item_detail")) && !"".equals(getPara("tr_itemid_list"))) {
                    for (int k = 0; k < order_id.length; k++) {// 运输单id
                        for (int i = 0; i < tr_itemid.length; i++) {// 货品id
                            for (int j = 0; j < item_detail.length; j++) {// 单品id
                                TransferOrderItemDetail tr_item_de = TransferOrderItemDetail.dao.findById(Integer.parseInt(item_detail[j]));
                                if (tr_item_de.get("item_id").toString().equals(tr_itemid[i].toString())
                                        && tr_item_de.get("order_id").toString().equals(order_id[k].toString())) {
                                    DepartOrderItemdetail de_item_detail = new DepartOrderItemdetail();
                                    de_item_detail.set("depart_id", Integer.parseInt(dp.get("id").toString()))
                                            .set("order_id", Integer.parseInt(order_id[k])).set("item_id", Integer.parseInt(tr_itemid[i]))
                                            .set("itemdetail_id", Integer.parseInt(item_detail[j])).save();
                                }
                            }
                        }
                    }
                    setAttr("item_detail", getPara("item_detail"));
                    setAttr("tr_itemid", getPara("tr_itemid_list"));
                }
            }
            int de_id = Integer.parseInt(dp.get("id").toString());// 获取发车单id
            // 根据勾选了多少个运输单，循环插入发车运输单中间表
            for (int i = 0; i < order_id.length; i++) {
                DepartTransferOrder dt = new DepartTransferOrder();
                dt.set("depart_id", de_id).set("order_id", order_id[i]).set("transfer_order_no", order_id[i]).save();
            }
            updateTransferSp(dp.get("id").toString(), sp_id);
            //String getIindepart_no = creat_order_no();
            boolean last_detail_size = CheckDepartOrder(order_id);
            //setAttr("getIin_depart_no", getIindepart_no);
            setAttr("last_detail_size", last_detail_size);
				setAttr("edit_depart_id", dp.get("id").toString());
            setAttr("creat", name);// 创建人

            // 根据运输单个数，判断发车单类型
            int lang = order_id.length;

            if (lang > 1) {
                setAttr("type", "many");
            } else {
                setAttr("type", "one");
            }

            // setAttr("localArr", order_id2);//运输单id,回显货品table
            if (LoginUserController.isAuthenticated(this))
                render("departOrder/editTransferOrder.html");
        } else {// 编辑发车单

            dp = DepartOrder.dao.findById(Integer.parseInt(depart_id));
            dp.set("create_by", Integer.parseInt(creat_id)).set("create_stamp", createDate)
                    .set("combine_type", DepartOrder.COMBINE_TYPE_DEPART).set("car_no", getPara("car_no"))
                    .set("car_type", getPara("cartype")).set("driver_id", Integer.parseInt(party_id)).set("car_size", getPara("carsize"))
                    .set("remark", getPara("remark")).set("car_follow_name", getPara("car_follow_name"))
                    .set("car_follow_phone", getPara("car_follow_phone")).set("route_from", getPara("route_from"))
                    .set("route_to", getPara("route_to"));
            if (!"".equals(sp_id)) {
                dp.set("sp_id", Integer.parseInt(sp_id));
            }
            if (!"".equals(car_follow_name)) {
                dp.set("car_follow_name", car_follow_name);
            }
            if (!"".equals(car_follow_phone)) {
                dp.set("car_follow_phone", car_follow_phone);
            }
            dp.update();
            updateTransferSp(dp.get("id").toString(), sp_id);
            if (!"".equals(getPara("item_detail")) && !"".equals(getPara("tr_itemid_list"))) {
                String tr_detail = "select id as itemdetail_id  from transfer_order_item_detail where id in (" + getPara("item_detail")
                        + ")";// tr单品id
                String dp_detail = "select itemdetail_id  from depart_transfer_itemdetail where depart_id =" + depart_id;// dp单品id
                List<Record> tr_detail_list = Db.find(tr_detail);
                List<Record> dp_deatil_list = Db.find(dp_detail);
                tr_detail_list.removeAll(dp_deatil_list);
                dp_deatil_list.addAll(tr_detail_list);
                System.out.println("-------------" + dp_deatil_list);
                for (int d = 0; d < dp_deatil_list.size();) {
                    if (dp_deatil_list.get(d).get("itemdetail_id") == null) {
                        dp_deatil_list.remove(d);
                        d = d - d;
                        continue;
                    }
                    d++;
                }
                for (int p = 0; p < dp_deatil_list.size();) {
                    String delete_detail = "select * from depart_transfer_itemdetail where itemdetail_id="
                            + Integer.parseInt(dp_deatil_list.get(p).get("itemdetail_id").toString());
                    DepartOrderItemdetail depdetail = DepartOrderItemdetail.dao.findFirst(delete_detail);
                    if (depdetail != null) {
                        boolean check = false;
                        for (int t = 0; t < item_detail.length; t++) {
                            if (item_detail[t].equals(dp_deatil_list.get(p).get("itemdetail_id").toString())) {
                                check = true;
                            }
                        }
                        if (check == false) {
                            String delete_sql = "select * from depart_transfer_itemdetail where itemdetail_id="
                                    + Integer.parseInt(dp_deatil_list.get(p).get("itemdetail_id").toString());
                            DepartOrderItemdetail dp_delete = DepartOrderItemdetail.dao.findFirst(delete_sql);
                            dp_delete.set("depart_id", null).set("item_id", null).set("itemdetail_id", null).update();
                            dp_delete.delete();
                        }
                        dp_deatil_list.remove(p);
                        p = p - p;
                        continue;
                    }
                    p++;
                }

                for (int k = 0; k < order_id.length; k++) {// 运输单id
                    for (int i = 0; i < tr_itemid.length; i++) {// 货品id
                        for (int f = 0; f < dp_deatil_list.size(); f++) {// 单品id
                            TransferOrderItemDetail tr_item_de = TransferOrderItemDetail.dao.findById(Integer.parseInt(dp_deatil_list
                                    .get(f).get("itemdetail_id").toString()));
                            if (tr_item_de.get("item_id").toString().equals(tr_itemid[i].toString())
                                    && tr_item_de.get("order_id").toString().equals(order_id[k].toString())) {
                                DepartOrderItemdetail de_item_detail = null;
                                de_item_detail = new DepartOrderItemdetail();
                                de_item_detail.set("depart_id", Integer.parseInt(dp.get("id").toString()))
                                        .set("order_id", Integer.parseInt(order_id[k])).set("item_id", Integer.parseInt(tr_itemid[i]))
                                        .set("itemdetail_id", Integer.parseInt(dp_deatil_list.get(f).get("itemdetail_id").toString()))
                                        .save();
                            }
                        }
                    }
                }
            }
            renderJson(dp);
        }

    }

    // 修改发车单状态
    public void updatestate() {
        String depart_id = getPara("depart_id");// 发车单id
        String order_state = getPara("order_state");// 发车单id
        DepartOrder dp = DepartOrder.dao.findById(Integer.parseInt(depart_id));
        dp.set("status", order_state).update();
        savePickupOrderMilestone(dp, order_state);
        renderJson(dp);
    }

    // 点击货品table的查看 ，显示对应货品的单品
    public void itemDetailList() {
        String item_id = getPara("item_id");
        String depart_id = getPara("depart_id");// 发车单id
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from transfer_order_item_detail  where item_id =" + item_id + "";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        /*
         * String
         * sql_item="select * from depart_transfer_itemdetail where item_id="
         * +item_id;
         */
        List<Record> itemdetail = null;
        if ("".equals(depart_id)) {
            String sql_tr_itemdetail_id = "select id as itemdetail_id  from transfer_order_item_detail where item_id=" + item_id;
            String sql_dp_itemdetail_id = "select itemdetail_id  from depart_transfer_itemdetail  where item_id=" + item_id;
            List<Record> sql_tr_itemdetail_idlist = Db.find(sql_tr_itemdetail_id);
            List<Record> sql_dp_itemdetail_idlist = Db.find(sql_dp_itemdetail_id);
            /* List<Record> depart_itemdetail=Db.find(sql_item); */
            sql_tr_itemdetail_idlist.removeAll(sql_dp_itemdetail_idlist);
            String detail_id = "0";
            if (sql_tr_itemdetail_idlist.size() > 0) {
                for (int i = 0; i < sql_tr_itemdetail_idlist.size(); i++) {
                    detail_id += sql_tr_itemdetail_idlist.get(i).get("itemdetail_id") + ",";
                }
                detail_id = detail_id.substring(0, detail_id.length() - 1);
            }
            String sql = "select * from transfer_order_item_detail  where ID in(" + detail_id + ")";
            itemdetail = Db.find(sql);
        } else {
            String sql = "select * from transfer_order_item_detail  where item_id in(" + item_id + ")";
            itemdetail = Db.find(sql);
        }

        Map Map = new HashMap();
        Map.put("sEcho", pageIndex);
        Map.put("iTotalRecords", rec.getLong("total"));
        Map.put("iTotalDisplayRecords", rec.getLong("total"));
        Map.put("aaData", itemdetail);
        renderJson(Map);
    }

    // 发车单删除
    public void cancel() {
        String id = getPara();
        String sql = "select * from depart_transfer  where depart_id =" + id + "";
        List<DepartTransferOrder> re_tr = DepartTransferOrder.dao.find(sql);
        for (int i = 0; i < re_tr.size(); i++) {
            DepartTransferOrder dep_tr = DepartTransferOrder.dao.findFirst(sql);
            dep_tr.set("depart_id", null).update();
            dep_tr.delete();
        }
        String delete_detail_sql = "select * from depart_transfer_itemdetail  where depart_id=" + id;
        List<DepartOrderItemdetail> dep = DepartOrderItemdetail.dao.find(delete_detail_sql);
        for (int j = 0; j < dep.size(); j++) {
            DepartOrderItemdetail dep_detail = DepartOrderItemdetail.dao.findById(Integer.parseInt(dep.get(j).get("id").toString()));
            dep_detail.set("depart_id", null).set("item_id", null).set("itemdetail_id", null).update();
            dep_detail.delete();
        }
        DepartOrder re = DepartOrder.dao.findById(id);
        re.set("driver_id", null).update();
        re.delete();
        render("departOrder/departOrderList.html");
    }

    // 保存发车里程碑
    private TransferOrderMilestone savePickupOrderMilestone(DepartOrder departOrder, String status) {
        TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
        if (status == null) {
            transferOrderMilestone.set("status", "新建");
        } else {
            transferOrderMilestone.set("status", status);
            transferOrderstatus(departOrder.get("id").toString(), status, null);
        }
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        transferOrderMilestone.set("create_by", users.get(0).get("id"));
        transferOrderMilestone.set("location", "");
        java.util.Date utilDate = new java.util.Date();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
        transferOrderMilestone.set("create_stamp", sqlDate);
        transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
        transferOrderMilestone.set("depart_id", departOrder.get("id"));
        transferOrderMilestone.save();
        return transferOrderMilestone;
    }

    // 单击tab里程碑
    public void transferOrderMilestoneList() {
        Map<String, List> map = new HashMap<String, List>();
        List<String> usernames = new ArrayList<String>();
        String departOrderId = getPara("departOrderId");
        if (departOrderId == "" || departOrderId == null) {
            departOrderId = "-1";
        }
        if (!"-1".equals(departOrderId)) {
            List<TransferOrderMilestone> transferOrderMilestones = TransferOrderMilestone.dao
                    .find("select * from transfer_order_milestone where type = '" + TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE
                            + "'and depart_id=" + departOrderId );
            for (TransferOrderMilestone transferOrderMilestone : transferOrderMilestones) {
                UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
                String username = userLogin.get("user_name");
                usernames.add(username);
            }
            map.put("transferOrderMilestones", transferOrderMilestones);
            map.put("usernames", usernames);
        }
        renderJson(map);
    }

    // 编辑里程碑
    public void saveTransferOrderMilestone() {
        String milestoneDepartId = getPara("milestoneDepartId");
        Map<String, Object> map = new HashMap<String, Object>();
        if (milestoneDepartId != null && !"".equals(milestoneDepartId)) {
            DepartOrder departOrder = DepartOrder.dao.findById(milestoneDepartId);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            String status = getPara("status");
            String location = getPara("location");
            transferOrderstatus(milestoneDepartId, status, location);
            if (!status.isEmpty()) {
                transferOrderMilestone.set("status", status);
                departOrder.set("status", status);
            } else {
                transferOrderMilestone.set("status", "在途");
                departOrder.set("status", "在途");
            }
            departOrder.update();
            if (!location.isEmpty()) {
                transferOrderMilestone.set("location", location);
            } else {
                transferOrderMilestone.set("location", "");
            }
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");

            transferOrderMilestone.set("create_by", users.get(0).get("id"));

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("depart_id", milestoneDepartId);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_DEPART_ORDER_MILESTONE);
            transferOrderMilestone.save();

            map.put("transferOrderMilestone", transferOrderMilestone);
            UserLogin userLogin = UserLogin.dao.findById(transferOrderMilestone.get("create_by"));
            String username = userLogin.get("user_name");
            map.put("username", username);
        }
        renderJson(map);
    }

    // 同步运输单状态里程碑
    public void transferOrderstatus(String de_or, String status, String location) {
        int depart_id = Integer.parseInt(de_or);
        List<DepartTransferOrder> dep = DepartTransferOrder.dao
                .find("select * from depart_transfer  where depart_id in(" + depart_id + ")");
        for (int i = 0; i < dep.size(); i++) {
            int order_id = Integer.parseInt(dep.get(i).get("order_id").toString());
            TransferOrder tr = TransferOrder.dao.findById(order_id);
            TransferOrderMilestone transferOrderMilestone = new TransferOrderMilestone();
            if (!status.isEmpty()) {
                transferOrderMilestone.set("status", status);
                tr.set("status", status);
            } else {
                transferOrderMilestone.set("status", "在途");
                tr.set("status", "在途");
            }
            tr.update();
            String name = (String) currentUser.getPrincipal();
            List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
            transferOrderMilestone.set("create_by", users.get(0).get("id"));
            if (location == null || location.isEmpty()) {
                transferOrderMilestone.set("location", "");
            } else {
                transferOrderMilestone.set("location", location);
            }

            java.util.Date utilDate = new java.util.Date();
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
            transferOrderMilestone.set("create_stamp", sqlDate);
            transferOrderMilestone.set("type", TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE);
            transferOrderMilestone.set("order_id", order_id);
            transferOrderMilestone.save();

        }

    }

    // 在途运输单管理
    public void transferMilestoneindex() {
        if (LoginUserController.isAuthenticated(this))
            render("departOrder/TransferOrderStatus.html");
    }

    public void transferMilestone() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from office";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));
        List<TransferOrderMilestone> transferOrderMilestone = TransferOrderMilestone.dao
                .find("select trom.*,tor.order_no as order_no,us.user_name as usernames from transfer_order_milestone trom "
                        + "left join transfer_order tor on tor.id=trom.order_id " + "left join user_login  us on us.id=trom.create_by "
                        + "where trom.status='在途' and trom. type='" + TransferOrderMilestone.TYPE_TRANSFER_ORDER_MILESTONE + "'");
       
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", transferOrderMilestone);

        renderJson(orderMap);
    }

    // 回显供应商
    public void getIntsp() {
        Contact co = Contact.dao.findFirst("select * from contact  where id in(select contact_id  from party p where p.id="
                + Integer.parseInt(getPara("sp_id").toString()) + " )");
        renderJson(co);
    }

    // 修改运输单供应商
    public void updateTransferSp(String depart_id, String SP_id) {
        int de_id = Integer.parseInt(depart_id);
       
        if (!"".equals(SP_id)) {
        	 int edit_sp_id = Integer.parseInt(SP_id);
            List<DepartTransferOrder> dp = DepartTransferOrder.dao.find("select * from depart_transfer where depart_id=" + de_id + "");
            for (int i = 0; i < dp.size(); i++) {
                TransferOrder tr = TransferOrder.dao.findById(Integer.parseInt(dp.get(i).get("order_id").toString()));
                tr.set("sp_id", edit_sp_id).update();
            }
        }else{
        	 List<DepartTransferOrder> dp = DepartTransferOrder.dao.find("select * from depart_transfer where depart_id=" + de_id + "");
            int r_sp_id=0;
            boolean check=true;
        	 for (int i = 0; i < dp.size(); i++) {
                 TransferOrder tr = TransferOrder.dao.findById(Integer.parseInt(dp.get(i).get("order_id").toString()));
                 if(tr.get("sp_id")!=null){
                 if(check=true){
                	 r_sp_id=Integer.parseInt(tr.get("sp_id").toString());
                	 check=false;
                 }
                	 
                 }
                 if(tr.get("sp_id")==null){
                	 tr.set("sp_id", r_sp_id).update();
                 } 
             }
        	 DepartOrder de=DepartOrder.dao.findById(de_id);
        	 if(de.get("sp_id")==null){
        		 de.set("sp_id", r_sp_id).update();
        	 }
        }

    }
    //修改运输单仓库
    public void  updateWarehouse(String depart_id,String house_id){
    	 int de_id = Integer.parseInt(depart_id);
    }

}
