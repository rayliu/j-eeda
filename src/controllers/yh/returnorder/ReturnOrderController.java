package controllers.yh.returnorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ReturnOrder;
import models.TransferOrder;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class ReturnOrderController extends Controller {
    private Logger logger = Logger.getLogger(ReturnOrderController.class);

    public void index() {
        if (LoginUserController.isAuthenticated(this))
            render("profile/returnorder/returnOrderList.html");
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
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        if (order_no == null && tr_order_no == null && de_order_no == null
                && stator == null && status == null && time_one == null
                && time_two == null) {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from return_order ro ";
            Record rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select r_o.*, usl.user_name as creator_name, dp.depart_no as depart_order_no, d_o.order_no as delivery_order_no, c.company_name from return_order r_o "
                    		+"left join depart_transfer  dpt on dpt.depart_id=r_o.depart_order_id "
                    		+"left join depart_order  dp on dp.id = dpt.depart_id "
                            + "left join delivery_order d_o on r_o.delivery_order_id = d_o.id "
                            + "left join party p on r_o.customer_id = p.id "
                            + "left join contact c on p.contact_id = c.id "
                            +"left join user_login  usl on usl.id=r_o.creator "
                            + sLimit);

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
            +"left join depart_transfer  dpt on dpt.depart_id=r_o.depart_order_id " 
			+"left join depart_order  dp on dp.id = dpt.depart_id "
			+"left join delivery_order d_o on r_o.delivery_order_id = d_o.id " 
			+"left join party p on r_o.customer_id = p.id "
			+"left join contact c on p.contact_id = c.id "
			+"left join user_login  usl on usl.id=r_o.creator "
			+"where ifnull(r_o.order_no,'')  like'%"+order_no+"%' and  "
			+ "ifnull(dp.depart_no,'')  like'%"+tr_order_no+"%'  and  "
			+ "ifnull(d_o.order_no,'')  like'%"+de_order_no+"%'  and "
			+ "ifnull(r_o.transaction_status ,'')  like'%"+status+"%' and "
			+ "ifnull(usl.user_name ,'')  like'%"+stator+"%'  and "
			+ "r_o.create_date between '"+time_one+"' and '"+time_two+"'";
            Record rec = Db.findFirst(sql + totalWhere);
           logger.debug("total records:" + rec.getLong("total"));
            
            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select r_o.*, usl.user_name as creator_name, dp.depart_no as depart_order_no, d_o.order_no as delivery_order_no, c.company_name from return_order r_o "
						+"left join depart_transfer  dpt on dpt.depart_id=r_o.depart_order_id " 
						+"left join depart_order  dp on dp.id = dpt.depart_id "
						+"left join delivery_order d_o on r_o.delivery_order_id = d_o.id " 
						+"left join party p on r_o.customer_id = p.id "
						+"left join contact c on p.contact_id = c.id "
						+"left join user_login  usl on usl.id=r_o.creator "
						+"where ifnull(r_o.order_no,'')  like'%"+order_no+"%' and  "
						+ "ifnull(dp.depart_no,'')  like'%"+tr_order_no+"%'  and  "
						+ "ifnull(d_o.order_no,'')  like'%"+de_order_no+"%'  and "
						+ "ifnull(r_o.transaction_status ,'')  like'%"+status+"%' and "
						+ "ifnull(usl.user_name ,'')  like'%"+stator+"%'  and "
						+ "r_o.create_date between '"+time_one+"' and '"+time_two+"'");

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
				+"left join contact  co on co.id in (select p.contact_id from party p where p.id=ro.customer_id  ) " 
				+"left join contact  con on con.id in (select p.contact_id from party p where p.id=ro.notity_party_id ) "
				+"left join depart_transfer  dpt on dpt.depart_id=ro.depart_order_id "
				+"left join depart_order  dp on dp.id=dpt.depart_id "
				+"left join transfer_order  tor on tor.id=dpt.order_id "
				+"left join user_login  u on u.id =tor.create_by " 
				+"left join location lo on lo.code=tor.route_from "
				+"left join location loc on loc.code=tor.route_to " 
				+"left join user_login  usl on usl.id=ro.creator  "
				+"where ro.id="+id+"";
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
                + " left join delivery_order  de on de.id=ro.delivery_order_id "
                + " where ro.id=" + id + "";
        ReturnOrder re = ReturnOrder.dao.findById(id);
        if (re.get("delivery_order_id")==null) {
            message = Db.find(sql_tr);
        } else {
            message = Db.find(sql_del);
        }

        for (int i = 0; i < message.size(); i++) {
            String nature = message.get(i).get("nature");
            String pickup = message.get(i).get("pickup");
            String arrival = message.get(i).get("arrival");
            String transaction_status = message.get(i)
                    .get("transaction_status");
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
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from transfer_order_item ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));
      
        // 获取当前页的数据
        List<Record> itemlist = new ArrayList<Record>();
        TransferOrder tr =null;
        ReturnOrder re = ReturnOrder.dao.findById(id);
        if (re.get("delivery_order_id")==null) {
        	 tr = TransferOrder.dao
                     .findFirst(" select tor.*  from transfer_order tor " 
								+"left join return_order re on re.id="+id+" "
								+"left join depart_transfer  dpt on dpt.depart_id=re.depart_order_id "
								+"where tor.id =dpt.order_id   ");
        } else {
        	 tr = TransferOrder.dao
                     .findFirst(" select tor.*   from transfer_order tor "
								+"left join return_order re on re.id="+id+" "
								+"left join delivery_order  deo on deo.id=re.delivery_order_id "
								+"where tor.id =deo.transfer_order_id  ");
        }
       
        String nature = tr.getStr("cargo_nature");
        String sql_atm = "select toi.*,toid.serial_no  from transfer_order_item toi"
                + " left join transfer_order_item_detail  toid on toid.item_id =toi.id and toid.order_id =toi.order_id"
                + " where toi.order_id in ("+Integer.parseInt(tr.get("id").toString())+")";
        String sql_item = "select toi.* from transfer_order_item toi"
                + " where toi.order_id in ("+Integer.parseInt(tr.get("id").toString())+")";
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
    public void check() {
        String id = getPara();
        ReturnOrder re = ReturnOrder.dao.findById(id);
        if (re.get("delivery_order_id") != null) {
        	  TransferOrder tr = TransferOrder.dao
                      .findFirst("select trf.* from transfer_order  trf "
                    		  	+"left join return_order re on re.id="+Integer.parseInt(id)+" "
      							+"left join delivery_order  deo on deo.id=re.delivery_order_id "
      							+"where deo.transfer_order_id =trf.id ");
              String nature = tr.get("cargo_nature");
              setAttr("nature", nature);
              setAttr("check", true);
        } else {
        	 TransferOrder tr = TransferOrder.dao
                     .findFirst("select trf.* from transfer_order  trf "
                    		 	+"left join return_order re on re.id="+Integer.parseInt(id)+" "
								+"left join depart_transfer   dp on dp.depart_id=re.depart_order_id "
								+"where dp.order_id =trf.id");
             String nature = tr.getStr("cargo_nature");
             setAttr("nature", nature);
            setAttr("check", false);
        }

       
        setAttr("id", id);
        if (LoginUserController.isAuthenticated(this))
            render("profile/returnorder/returnOrder.html");
    }

    public void save() {
        int id = Integer.parseInt(getPara("id"));
        List<Record> DELIVERYORDERID = new ArrayList<Record>();
        DELIVERYORDERID = Db
                .find("select delivery_order_id  from return_order ro where ro.id='"
                        + id + "'");
        if (DELIVERYORDERID.get(0).get("delivery_order_id") != null) {
            setAttr("check", true);
        } else {
            setAttr("check", false);
        }
        TransferOrder tr = TransferOrder.dao.findById(id);
        String nature = tr.getStr("cargo_nature");
        ReturnOrder r = ReturnOrder.dao.findById(id);
        r.set("transaction_status", "完成").update();
        setAttr("nature", nature);
        setAttr("id", id);
        setAttr("saveOK", true);
        if (LoginUserController.isAuthenticated(this))
            render("profile/returnorder/returnOrder.html");

    }

    // 取消
    public void cancel() {
        String id = getPara();

        ReturnOrder re = ReturnOrder.dao.findById(id);
        re.set("TRANSACTION_STATUS", "cancel").update();
        renderJson("{\"success\":true}");

    }

}
