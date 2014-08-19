package controllers.yh.returnOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Fin_item;
import models.Location;
import models.Party;
import models.ReturnOrder;
import models.TransferOrder;
import models.UserLogin;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class ReturnOrderController extends Controller {
    private Logger logger = Logger.getLogger(ReturnOrderController.class);

    public void index() {
        if (LoginUserController.isAuthenticated(this))
            render("returnOrder/returnOrderList.html");
    }

    public static String createReturnOrderNo() {
        String orderNo = null;
        ReturnOrder order = ReturnOrder.dao.findFirst("select * from return_order order by order_no desc limit 0,1");
        if (order != null) {
            String num = order.get("order_no");
            String str = num.substring(2, num.length());
            System.out.println(str);
            Long oldTime = Long.parseLong(str);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            String time = format + "00001";
            Long newTime = Long.parseLong(time);
            if (oldTime >= newTime) {
                orderNo = String.valueOf((oldTime + 1));
            } else {
                orderNo = String.valueOf(newTime);
            }
            orderNo = "HD" + orderNo;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            orderNo = format + "00001";
            orderNo = "HD" + orderNo;
        }
        return orderNo;
    }

    public void list() {
        String order_no = getPara("order_no");
        String tr_order_no = getPara("tr_order_no");
        String de_order_no = getPara("de_order_no");
        String stator = getPara("stator");
        String status = getPara("status");
        String time_one = getPara("time_one");
        String time_two = getPara("time_two");

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        if (order_no == null && tr_order_no == null && de_order_no == null && stator == null && status == null
                && time_one == null && time_two == null) {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from return_order ro ";
            Record rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select distinct r_o.*, usl.user_name as creator_name, tor.order_no transfer_order_no, d_o.order_no as delivery_order_no, c.contact_person cname from return_order r_o "
						+ " left join transfer_order tor on tor.id = r_o.transfer_order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join delivery_order d_o on r_o.delivery_order_id = d_o.id" 
						+ " left join user_login  usl on usl.id=r_o.creator"
						+ " order by r_o.create_date desc " + sLimit);

            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);

        } else {
            if (time_one == null || "".equals(time_one)) {
                time_one = "1-1-1";
            }
            if (time_two == null || "".equals(time_two)) {
                time_two = "9999-12-31";
            }

            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from return_order r_o "
                    + "left join depart_transfer  dpt on dpt.depart_id=r_o.depart_order_id "
                    + "left join depart_order  dp on dp.id = dpt.depart_id "
                    + "left join delivery_order d_o on r_o.delivery_order_id = d_o.id "
                    + "left join party p on r_o.customer_id = p.id " + "left join contact c on p.contact_id = c.id "
                    + "left join user_login  usl on usl.id=r_o.creator " + "where ifnull(r_o.order_no,'')  like'%"
                    + order_no + "%' and  " + "ifnull(dp.depart_no,'')  like'%" + tr_order_no + "%'  and  "
                    + "ifnull(d_o.order_no,'')  like'%" + de_order_no + "%'  and "
                    + "ifnull(r_o.transaction_status ,'')  like'%" + status + "%' and "
                    + "ifnull(usl.user_name ,'')  like'%" + stator + "%'  and " + "r_o.create_date between '"
                    + time_one + "' and '" + time_two + "'";
            Record rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select distinct r_o.*, usl.user_name as creator_name, tor.order_no transfer_order_no, d_o.order_no as delivery_order_no, c.contact_person cname from return_order r_o "
							+ " left join transfer_order tor on tor.id = r_o.transfer_order_id"
							+ " left join party p on p.id = tor.customer_id"
							+ " left join contact c on c.id = p.contact_id"
							+ " left join delivery_order d_o on r_o.delivery_order_id = d_o.id" 
							+ " left join user_login  usl on usl.id=r_o.creator "
                            + "where ifnull(r_o.order_no,'')  like'%"
                            + order_no
                            + "%' and  "
                            + "ifnull(dp.depart_no,'')  like'%"
                            + tr_order_no
                            + "%'  and  "
                            + "ifnull(d_o.order_no,'')  like'%"
                            + de_order_no
                            + "%'  and "
                            + "ifnull(r_o.transaction_status ,'')  like'%"
                            + status
                            + "%' and "
                            + "ifnull(usl.user_name ,'')  like'%"
                            + stator
                            + "%'  and "
                            + "r_o.create_date between '"
                            + time_one + "' and '" + time_two + "' order by r_o.create_date desc "+sLimit);

            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);
        }

        renderJson(orderMap);
    }

    // 查看回单显示
    public void checkorder() {

        int id = Integer.parseInt(getPara("locationName"));

        List<Record> message = new ArrayList<Record>();
        String sql_tr = "select ro.*, co.address as address,co.company_name,co.contact_person as contact ,co.phone as phone , con.company_name as pay_company ,con.address as pay_address,con.contact_person as pay_contad ,con.phone as pay_phone  , dp.depart_no  as depart_order_no,tor.cargo_nature  as nature,tor.pickup_mode  as pickup,tor.arrival_mode  as arrival,tor.remark as remark , u.user_name as counterman,lo.name as location_from,loc.name as location_to, usl.user_name  as creator_name  from return_order  ro "
                + "left join contact  co on co.id in (select p.contact_id from party p where p.id=ro.customer_id  ) "
                + "left join contact  con on con.id in (select p.contact_id from party p where p.id=ro.notity_party_id ) "
                + "left join depart_transfer  dpt on dpt.depart_id=ro.depart_order_id "
                + "left join depart_order  dp on dp.id=dpt.depart_id "
                + "left join transfer_order  tor on tor.id=dpt.order_id "
                + "left join user_login  u on u.id =tor.create_by "
                + "left join location lo on lo.code=tor.route_from "
                + "left join location loc on loc.code=tor.route_to "
                + "left join user_login  usl on usl.id=ro.creator  " + "where ro.id=" + id + "";
        String sql_del = "select ro.*,co.company_name as company_name ,co.address as address,co.contact_person as contact ,co.phone as phone ,"
                + " con.company_name as pay_company ,con.address as pay_address,con.contact_person as pay_contad ,con.phone as pay_phone ,"
                + " tor.order_no as transfer_order_no, de.order_no  as delivery_order_id_no ,tor.cargo_nature  as nature,tor.pickup_mode  as pickup,tor.arrival_mode  as arrival,tor.remark as remark ,"
                + " u.user_name as counterman,lo.name as location_from,loc.name as location_to ,usl.user_name  as creator_name"
                + " from return_order  ro "
                + " left join contact  co on co.id in (select p.contact_id from party p where p.id=ro.customer_id )"
                + " left join contact  con on con.id in (select p.contact_id from party p where p.id=ro.notity_party_id )"
                + " left join transfer_order  tor on tor.id in (select delo.transfer_order_id  from delivery_order delo where delo.id=ro.delivery_order_id )"
                + " left join user_login  u on u.id =tor.create_by "
                + " left join location lo on lo.code=tor.route_from "
                + " left join location loc on loc.code=tor.route_to "
                + " left join user_login  usl on usl.id=ro.creator  "
                + " left join delivery_order  de on de.id=ro.delivery_order_id " + " where ro.id=" + id + "";
        ReturnOrder re = ReturnOrder.dao.findById(id);
        if (re.get("delivery_order_id") == null) {
            message = Db.find(sql_tr);
        } else {
            message = Db.find(sql_del);
        }

        for (int i = 0; i < message.size(); i++) {
            String nature = message.get(i).get("nature");
            String pickup = message.get(i).get("pickup");
            String arrival = message.get(i).get("arrival");
            String transaction_status = message.get(i).get("transaction_status");
            if ("cargo".equals(nature)) {
                message.get(i).set("nature", "普通货品");
            }
            if ("routeSP".equals(pickup)) {
                message.get(i).set("pickup", "干线供应商自提");
            }
            if ("pickupSP".equals(pickup)) {
                message.get(i).set("pickup", "外包供应商自提");
            }
            if ("own".equals(pickup)) {
                message.get(i).set("pickup", "公司自提");
            }
            if ("delivery".equals(arrival)) {
                message.get(i).set("arrival", "货品直送");
            }
            if ("gateIn".equals(arrival)) {
                message.get(i).set("arrival", "入中转仓");
            }
            if ("new".equals(transaction_status)) {
                message.get(i).set("transaction_status", "新建");
            }
            if ("confirmed".equals(transaction_status)) {
                message.get(i).set("transaction_status", "确认");
            }
            if ("cancel".equals(transaction_status)) {
                message.get(i).set("transaction_status", "取消");
            }
        }

        renderJson(message);

    }

    // 收费条目
    public void paylist() {
        int id = Integer.parseInt(getPara("locationName"));
        List<Record> paylist = new ArrayList<Record>();
        paylist = Db
                .find("select f.name,f.remark,tf.amount,tf.status from fin_item f,transfer_order_fin_item tf  where tf.fin_item_id =f.id and tf.order_id ='"
                        + 1 + "'");

        renderJson(paylist);
    }

    // 货品详细
    public void itemlist() {
        int id = Integer.parseInt(getPara("locationName"));
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from transfer_order_item ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> itemlist = new ArrayList<Record>();
        TransferOrder tr = null;
        ReturnOrder re = ReturnOrder.dao.findById(id);
        if (re.get("delivery_order_id") == null) {
            tr = TransferOrder.dao.findFirst(" select tor.*  from transfer_order tor "
                    + "left join return_order re on re.id=" + id + " "
                    + "left join depart_transfer  dpt on dpt.depart_id=re.depart_order_id "
                    + "where tor.id =dpt.order_id   ");
        } else {
            tr = TransferOrder.dao.findFirst(" select tor.*   from transfer_order tor "
                    + "left join return_order re on re.id=" + id + " "
                    + "left join delivery_order  deo on deo.id=re.delivery_order_id "
                    + "where tor.id =deo.transfer_order_id  ");
        }

        String nature = tr.getStr("cargo_nature");
        String sql_atm = "select toi.*,toid.serial_no  from transfer_order_item toi"
                + " left join transfer_order_item_detail  toid on toid.item_id =toi.id and toid.order_id =toi.order_id"
                + " where toi.order_id in (" + Integer.parseInt(tr.get("id").toString()) + ")";
        String sql_item = "select toi.* from transfer_order_item toi" + " where toi.order_id in ("
                + Integer.parseInt(tr.get("id").toString()) + ")";
        if ("ATM".equals(nature)) {
            itemlist = Db.find(sql_atm);
        } else {
            itemlist = Db.find(sql_item);
        }

        // 获取货损条数和货品id

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", itemlist);
        renderJson(orderMap);
    }

    // 点击查看
    public void edit() {
        ReturnOrder returnOrder = ReturnOrder.dao.findById(getPara("id"));
        Long transferOrderId = returnOrder.get("transfer_order_id");
        if (transferOrderId != null && !"".equals(transferOrderId)) {
            TransferOrder transferOrder = TransferOrder.dao.findById(transferOrderId);
            setAttr("transferOrder", transferOrder);
            Long customer_id = transferOrder.get("customer_id");
            if (customer_id != null) {
                Party customer = Party.dao.findById(customer_id);
                Contact customerContact = Contact.dao.findById(customer.get("contact_id"));
                setAttr("customerContact", customerContact);
            }
            String code = "";
            Long deliveryOrderId = returnOrder.get("delivery_order_id");
            if(deliveryOrderId != null){
            	DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(deliveryOrderId);
            	Long notifyPartyIdDo = deliveryOrder.get("notify_party_id");
                if (notifyPartyIdDo != null) {
                    Party notify = Party.dao.findById(notifyPartyIdDo);
                    Contact contact = Contact.dao.findById(notify.get("contact_id"));
                    setAttr("contact", contact);
                    Contact locationCode = Contact.dao.findById(notify.get("contact_id"));
                    code = locationCode.get("location");
            }else{            
	            Long notify_party_id = transferOrder.get("notify_party_id");
	            if (notify_party_id != null) {
	                Party notify = Party.dao.findById(notify_party_id);
	                Contact contact = Contact.dao.findById(notify.get("contact_id"));
	                setAttr("contact", contact);
	                Contact locationCode = Contact.dao.findById(notify.get("contact_id"));
	                code = locationCode.get("location");
	            } 
            }
         }
            	
            List<Location> provinces2 = Location.dao.find("select * from location where pcode ='1'");
            Location l2 = Location.dao
                    .findFirst("SELECT * FROM location where code = (select pcode from location where CODE = '"
                            + code + "')");
            Location location = null;
            if (provinces2.contains(l2)) {
                location = Location.dao
                        .findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
                                + code + "'");
            } else {
                location = Location.dao
                        .findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
                                + code + "'");
            }
            setAttr("location", location);
            
            String routeFrom = transferOrder.get("route_from");
            Location locationFrom = null;
            if (routeFrom != null || !"".equals(routeFrom)) {
                List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
                Location l = Location.dao
                        .findFirst("select * from location where code = (select pcode from location where code = '"
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

            String routeTo = transferOrder.get("route_to");
            Location locationTo = null;
            if (routeTo != null || !"".equals(routeTo)) {
                List<Location> provinces = Location.dao.find("select * from location where pcode ='1'");
                Location l = Location.dao
                        .findFirst("select * from location where code = (select pcode from location where code = '"
                                + routeTo + "')");
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
            UserLogin userLogin = UserLogin.dao.findById(transferOrder.get("create_by"));
            setAttr("userLoginOrder", userLogin);
        } 

        setAttr("returnOrder", returnOrder);
        UserLogin userLogin = UserLogin.dao.findById(returnOrder.get("creator"));
        setAttr("userLogin", userLogin);
        if (LoginUserController.isAuthenticated(this))
            render("returnOrder/returnOrder.html");
    }

    public void save() {
        ReturnOrder returnOrder = ReturnOrder.dao.findById(getPara("id"));
        returnOrder.set("remark", getPara("remark"));
        returnOrder.update();
        if (LoginUserController.isAuthenticated(this))
            renderJson(returnOrder);

    }

    // 取消
    public void cancel() {
        String id = getPara();
        ReturnOrder re = ReturnOrder.dao.findById(id);
        re.set("TRANSACTION_STATUS", "cancel").update();
        renderJson("{\"success\":true}");
    }

    // 应收list
    public void accountReceivable() {
        String id = getPara();
        ReturnOrder returnOrder = ReturnOrder.dao.findById(id);

        String sLimit = "";
        if (id == null || id.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from transfer_order_fin_item where order_id ='"
                + returnOrder.get("transfer_order_id") + "' ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select d.*,f.name,f.remark,t.order_no as transferOrderNo from transfer_order_fin_item d left join fin_item f on d.fin_item_id = f.id left join transfer_order t on t.id = d.order_id where d.order_id ='"
                        + returnOrder.get("transfer_order_id") + "' and f.type='应收'");

        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        List<Record> list = Db.find("select * from fin_item");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get("name") == null) {
                Fin_item.dao.deleteById(list.get(i).get("id"));
            }
        }
        renderJson(orderMap);
    }
}
