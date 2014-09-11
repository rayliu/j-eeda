package controllers.yh.arap.ar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAuditItem;
import models.ArapAuditOrder;
import models.Party;
import models.UserLogin;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class ChargeCheckOrderController extends Controller {
    private Logger logger = Logger.getLogger(ChargeCheckOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
    	if(LoginUserController.isAuthenticated(this))
    	    render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderList.html");
    }

    public void add() {
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "");
    	if(LoginUserController.isAuthenticated(this))
        render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderCreateSearchList.html");
    }

    public void create() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        setAttr("returnOrderIds", ids);	 
        String beginTime = getPara("beginTime");
        if(beginTime != null && !"".equals(beginTime)){
        	setAttr("beginTime", beginTime);
        }
        String endTime = getPara("endTime");
        if(endTime != null && !"".equals(endTime)){
        	setAttr("endTime", endTime);	
        }
        String customerId = getPara("customerId");
        if(!"".equals(customerId) && customerId != null){
	        Party party = Party.dao.findById(customerId);
	        setAttr("party", party);	        
	        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
	        setAttr("customer", contact);
	        setAttr("type", "CUSTOMER");
	    	setAttr("classify", "");
        }
        
        String order_no = null;
        setAttr("saveOK", false);
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));

        ArapAuditOrder order = ArapAuditOrder.dao.findFirst("select * from arap_audit_order order by order_no desc limit 0,1");
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
            setAttr("order_no", "YSDZ" + order_no);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String format = sdf.format(new Date());
            order_no = format + "00001";
            setAttr("order_no", "YSDZ" + order_no);
        }

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);

        setAttr("status", "new");
    	if(LoginUserController.isAuthenticated(this))
    		render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
    }

    // 创建应收对帐单时，先选取合适的回单，条件：客户，时间段
    public void createList() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 根据company_id 过滤
        String colsLength = getPara("iColumns");
        String fieldsWhere = "AND (";
        for (int i = 0; i < Integer.parseInt(colsLength); i++) {
            String mDataProp = getPara("mDataProp_" + i);
            String searchValue = getPara("sSearch_" + i);
            logger.debug(mDataProp + "[" + searchValue + "]");
            if (searchValue != null && !"".equals(searchValue)) {
                if (mDataProp.equals("COMPANY_ID")) {
                    fieldsWhere += "p.id" + " = " + searchValue + " AND ";
                } else {
                    fieldsWhere += mDataProp + " like '%" + searchValue + "%' AND ";
                }
            }
        }
        logger.debug("2nd filter:" + fieldsWhere);
        if (fieldsWhere.length() > 8) {
            fieldsWhere = fieldsWhere.substring(0, fieldsWhere.length() - 4);
            fieldsWhere += ')';
        } else {
            fieldsWhere = "";
        }
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from return_order ro left join party p on ro.customer_id = p.id "
                + "where ro.transaction_status = '已签收' ";
        Record rec = Db.findFirst(sql + fieldsWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select distinct r_o.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no, d_o.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,ifnull(p2.id,p.id) company_id from return_order r_o " 
							+" left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id  "
							+" left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join delivery_order_item doi on doi.delivery_id = d_o.id "
							+" left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=r_o.creator where r_o.transaction_status = '已签收' order by r_o.create_date desc " + fieldsWhere);
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    // billing order 列表
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_audit_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aao.*,c.abbr cname,ror.order_no return_order_no,tor.order_no transfer_order_no,dor.order_no delivery_order_no,ul.user_name creator_name from arap_audit_order aao "
						+" left join party p on p.id = aao.payee_id "
						+" left join contact c on c.id = p.contact_id "
						+" left join arap_audit_item aai on aai.audit_order_id= aao.id "
						+" left join return_order ror on ror.id = aai.ref_order_id "
						+" left join transfer_order tor on tor.id = ror.transfer_order_id "
						+" left join delivery_order dor on dor.id = ror.delivery_order_id "
				        +" left join user_login ul on ul.id = aao.create_by order by aao.create_stamp desc";

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    public void save(){
    	ArapAuditOrder arapAuditOrder = null;
    	String chargeCheckOrderId = getPara("chargeCheckOrderId");
    	if("".equals(chargeCheckOrderId) || chargeCheckOrderId == null){
	    	arapAuditOrder = new ArapAuditOrder();
	    	arapAuditOrder.set("order_no", getPara("order_no"));
	    	//arapAuditOrder.set("order_type", );
	    	arapAuditOrder.set("status", "new");
	    	arapAuditOrder.set("payee_id", getPara("customer_id"));
	    	arapAuditOrder.set("create_by", getPara("create_by"));
	    	arapAuditOrder.set("create_stamp", new Date());
	    	arapAuditOrder.set("remark", getPara("remark"));
	    	arapAuditOrder.save();
	    	
	    	String returnOrderIds = getPara("returnOrderIds");
	    	String[] returnOrderIdsArr = returnOrderIds.split(",");
	    	for(int i=0;i<returnOrderIdsArr.length;i++){
		    	ArapAuditItem arapAuditItem = new ArapAuditItem();
		    	//arapAuditItem.set("ref_order_type", );
		    	arapAuditItem.set("ref_order_id", returnOrderIdsArr[i]);
		    	arapAuditItem.set("audit_order_id", arapAuditOrder.get("id"));
		    	//arapAuditItem.set("item_status", "");
		    	arapAuditItem.set("create_by", getPara("create_by"));
		    	arapAuditItem.set("create_stamp", new Date());
		    	arapAuditItem.save();
	    	}
    	}else{
    		arapAuditOrder = ArapAuditOrder.dao.findById(chargeCheckOrderId);
	    	arapAuditOrder.set("order_no", getPara("order_no"));
	    	//arapAuditOrder.set("order_type", );
	    	arapAuditOrder.set("status", "new");
	    	arapAuditOrder.set("payee_id", getPara("customer_id"));
	    	arapAuditOrder.set("create_by", getPara("create_by"));
	    	arapAuditOrder.set("create_stamp", new Date());
	    	arapAuditOrder.set("remark", getPara("remark"));
	    	arapAuditOrder.set("last_modified_by", getPara("create_by"));
	    	arapAuditOrder.set("LAST_MODIFIED_STAMP", new Date());
	    	arapAuditOrder.update();
	    	
	    	List<ArapAuditItem> arapAuditItems = ArapAuditItem.dao.find("select * from arap_audit_item where audit_order_id = ?", arapAuditOrder.get("id"));
	    	for(ArapAuditItem arapAuditItem : arapAuditItems){
		    	//arapAuditItem.set("ref_order_type", );
		    	//arapAuditItem.set("item_status", "");
		    	arapAuditItem.set("create_by", getPara("create_by"));
		    	arapAuditItem.set("create_stamp", new Date());
		    	arapAuditItem.update();
	    	}
    	}
        renderJson(arapAuditOrder);;
    }
    
    public void edit() throws ParseException{
    	ArapAuditOrder arapAuditOrder = ArapAuditOrder.dao.findById(getPara("id"));
    	String customerId = arapAuditOrder.get("payee_id");
    	if(!"".equals(customerId) && customerId != null){
	    	Party party = Party.dao.findById(customerId);
	        setAttr("party", party);	        
	        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
	        setAttr("customer", contact);
    	}    	
    	UserLogin userLogin = UserLogin.dao.findById(arapAuditOrder.get("create_by"));
    	setAttr("userLogin", userLogin);
    	setAttr("arapAuditOrder", arapAuditOrder);
    	
    	Date beginTimeDate = arapAuditOrder.get("begin_time");
    	Date endTimeDate = arapAuditOrder.get("end_time");
    	String beginTime = "";
    	String endTime = "";
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	if(!"".equals(beginTimeDate) && beginTimeDate != null){
    		beginTime = simpleDateFormat.format(beginTimeDate);
    	}
    	if(!"".equals(endTimeDate) && endTimeDate != null){
    		endTime = simpleDateFormat.format(endTimeDate);
    	}
    	setAttr("beginTime", beginTime);
    	setAttr("endTime", endTime);
    	if(LoginUserController.isAuthenticated(this))
    		render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
    }
    
    public void returnOrderList() {
    	String chargeCheckOrderId = getPara("chargeCheckOrderId");
    	if(chargeCheckOrderId == null || "".equals(chargeCheckOrderId)){
    		chargeCheckOrderId = "-1";
    	}
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from return_order ro ";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select sum(tofi.amount) amount,ror.*, usl.user_name as creator_name,dor.order_no as delivery_order_no, ifnull(c.contact_person,c2.contact_person) cname, "
                		+ " ifnull(tor.order_no,(select group_concat(tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no from arap_audit_order aao "
	private Logger logger = Logger.getLogger(ChargeCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		if (LoginUserController.isAuthenticated(this))
			render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderList.html");
	}

	public void add() {
		setAttr("type", "CUSTOMER");
		setAttr("classify", "");
		if (LoginUserController.isAuthenticated(this))
			render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderCreateSearchList.html");
	}

	public void create() {
		String ids = getPara("ids");
		String[] idArray = ids.split(",");
		logger.debug(String.valueOf(idArray.length));

		setAttr("returnOrderIds", ids);
		String beginTime = getPara("beginTime");
		if (beginTime != null && !"".equals(beginTime)) {
			setAttr("beginTime", beginTime);
		}
		String endTime = getPara("endTime");
		if (endTime != null && !"".equals(endTime)) {
			setAttr("endTime", endTime);
		}
		String customerId = getPara("customerId");
		if (!"".equals(customerId) && customerId != null) {
			Party party = Party.dao.findById(customerId);
			setAttr("party", party);
			Contact contact = Contact.dao.findById(party.get("contact_id")
					.toString());
			setAttr("customer", contact);
			setAttr("type", "CUSTOMER");
			setAttr("classify", "");
		}

		String order_no = null;
		setAttr("saveOK", false);
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));

		ArapAuditOrder order = ArapAuditOrder.dao
				.findFirst("select * from arap_audit_order order by order_no desc limit 0,1");
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
			setAttr("order_no", "DZ" + order_no);
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String format = sdf.format(new Date());
			order_no = format + "00001";
			setAttr("order_no", "DZ" + order_no);
		}

		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		setAttr("status", "new");
		if (LoginUserController.isAuthenticated(this))
			render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}

	// 创建应收对帐单时，先选取合适的回单，条件：客户，时间段
	public void createList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		// 获取数据
		String companyName = getPara("companyName");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String receiptBegin = getPara("receiptBegin");
		String receiptEnd = getPara("receiptEnd");
		String sql = "";
		Record rec;
		List<Record> orders;

		String colsLength = getPara("iColumns");
		String fieldsWhere = "AND (";
		for (int i = 0; i < Integer.parseInt(colsLength); i++) {
			String mDataProp = getPara("mDataProp_" + i);
			String searchValue = getPara("sSearch_" + i);

			logger.debug(mDataProp + "[" + searchValue + "]");

			if (searchValue != null && !"".equals(searchValue)) {
				if (mDataProp.equals("COMPANY_ID")) {
					fieldsWhere += "p.id" + " = " + searchValue + " AND ";
				} else {
					fieldsWhere += mDataProp + " like '%" + searchValue
							+ "%' AND ";
				}
			}
		}

		logger.debug("2nd filter:" + fieldsWhere);

		if (fieldsWhere.length() > 8) {
			fieldsWhere = fieldsWhere.substring(0, fieldsWhere.length() - 4);
			fieldsWhere += ')';
		} else {
			fieldsWhere = "";
		}
		// 获取总条数
		String totalWhere = "";
		sql = "select count(1) total from return_order ro left join party p on ro.customer_id = p.id "
				+ "where ro.transaction_status = '已签收' ";
		rec = Db.findFirst(sql + fieldsWhere);
		logger.debug("total records:" + rec.getLong("total"));

		if (companyName == null && beginTime == null && endTime == null) {

			// 获取当前页的数据
			orders = Db
					.find("select distinct r_o.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no, d_o.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,ifnull(p2.id,p.id) company_id from return_order r_o "
							+ " left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id  "
							+ " left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join delivery_order_item doi on doi.delivery_id = d_o.id "
							+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=r_o.creator where r_o.transaction_status = '已签收' order by r_o.create_date desc "
							+ fieldsWhere);
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			if (receiptBegin == null || "".equals(receiptBegin)) {
				receiptBegin = "1-1-1";
			}
			if (receiptEnd == null || "".equals(receiptEnd)) {
				receiptEnd = "9999-12-31";
			}
			if (companyName == null || "".equals(companyName)) {

				// 获取当前页的数据
				orders = Db
						.find("select distinct r_o.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no, d_o.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,ifnull(p2.id,p.id) company_id from return_order r_o "
								+ " left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id  "
								+ " left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join delivery_order_item doi on doi.delivery_id = d_o.id "
								+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=r_o.creator where r_o.transaction_status = '已签收' "
								+ " and (r_o.create_date between '"
								+ beginTime
								+ "' and '"
								+ endTime
								+ "')"
								+ " and (r_o.receipt_date between '"
								+ receiptBegin
								+ "' and '"
								+ receiptEnd
								+ "') order by r_o.create_date desc"
								+ fieldsWhere);
			} else {
				// 获取当前页的数据
				orders = Db
						.find("select distinct r_o.*, usl.user_name as creator_name, ifnull(tor.order_no,(select group_concat(tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no, d_o.order_no as delivery_order_no, ifnull(c.abbr,c2.abbr) cname,ifnull(p2.id,p.id) company_id from return_order r_o "
								+ " left join transfer_order tor on tor.id = r_o.transfer_order_id left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id  "
								+ " left join delivery_order d_o on r_o.delivery_order_id = d_o.id left join delivery_order_item doi on doi.delivery_id = d_o.id "
								+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=r_o.creator where r_o.transaction_status = '已签收' "
								+ " and c.abbr like '%"
								+ companyName
								+ "%'"
								+ " or c2.abbr like '%"
								+ companyName
								+ "%' and (r_o.create_date between '"
								+ beginTime
								+ "' and '"
								+ endTime
								+ "')"
								+ " and (r_o.receipt_date between '"
								+ receiptBegin
								+ "' and '"
								+ receiptEnd
								+ "') order by r_o.create_date desc"
								+ fieldsWhere);
			}

		}

		/*
		 * .find(
		 * "select ror.*, tor.order_no as transfer_order_no, dor.order_no as delivery_order_no, p.id as company_id, c.abbr cname from return_order ror "
		 * + "left join transfer_order tor on ror.transfer_order_id = tor.id " +
		 * "left join delivery_order dor on ror.delivery_order_id = dor.id " +
		 * "left join party p on ror.customer_id = p.id left join contact c on p.contact_id = c.id "
		 * + "where ror.transaction_status = '已签收' " + fieldsWhere);
		 */

		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);
		renderJson(orderMap);
	}

	// billing order 列表
	public void list() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from arap_audit_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select aao.*,c.contact_person cname,ror.order_no return_order_no,tor.order_no transfer_order_no,dor.order_no delivery_order_no,ul.user_name creator_name from arap_audit_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id "
				+ " left join arap_audit_item aai on aai.audit_order_id= aao.id "
				+ " left join return_order ror on ror.id = aai.ref_order_id "
				+ " left join transfer_order tor on tor.id = ror.transfer_order_id "
				+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
				+ " left join user_login ul on ul.id = aao.create_by order by aao.create_stamp desc";

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	public void save() {
		ArapAuditOrder arapAuditOrder = null;
		String chargeCheckOrderId = getPara("chargeCheckOrderId");
		if ("".equals(chargeCheckOrderId) || chargeCheckOrderId == null) {
			arapAuditOrder = new ArapAuditOrder();
			arapAuditOrder.set("order_no", getPara("order_no"));
			// arapAuditOrder.set("order_type", );
			arapAuditOrder.set("status", "new");
			arapAuditOrder.set("payee_id", getPara("customer_id"));
			arapAuditOrder.set("create_by", getPara("create_by"));
			arapAuditOrder.set("create_stamp", new Date());
			arapAuditOrder.set("remark", getPara("remark"));
			arapAuditOrder.save();

			String returnOrderIds = getPara("returnOrderIds");
			String[] returnOrderIdsArr = returnOrderIds.split(",");
			for (int i = 0; i < returnOrderIdsArr.length; i++) {
				ArapAuditItem arapAuditItem = new ArapAuditItem();
				// arapAuditItem.set("ref_order_type", );
				arapAuditItem.set("ref_order_id", returnOrderIdsArr[i]);
				arapAuditItem.set("audit_order_id", arapAuditOrder.get("id"));
				// arapAuditItem.set("item_status", "");
				arapAuditItem.set("create_by", getPara("create_by"));
				arapAuditItem.set("create_stamp", new Date());
				arapAuditItem.save();
			}
		} else {
			arapAuditOrder = ArapAuditOrder.dao.findById(chargeCheckOrderId);
			arapAuditOrder.set("order_no", getPara("order_no"));
			// arapAuditOrder.set("order_type", );
			arapAuditOrder.set("status", "new");
			arapAuditOrder.set("payee_id", getPara("customer_id"));
			arapAuditOrder.set("create_by", getPara("create_by"));
			arapAuditOrder.set("create_stamp", new Date());
			arapAuditOrder.set("remark", getPara("remark"));
			arapAuditOrder.set("last_modified_by", getPara("create_by"));
			arapAuditOrder.set("LAST_MODIFIED_STAMP", new Date());
			arapAuditOrder.update();

			List<ArapAuditItem> arapAuditItems = ArapAuditItem.dao.find(
					"select * from arap_audit_item where audit_order_id = ?",
					arapAuditOrder.get("id"));
			for (ArapAuditItem arapAuditItem : arapAuditItems) {
				// arapAuditItem.set("ref_order_type", );
				// arapAuditItem.set("item_status", "");
				arapAuditItem.set("create_by", getPara("create_by"));
				arapAuditItem.set("create_stamp", new Date());
				arapAuditItem.update();
			}
		}
		renderJson(arapAuditOrder);;
	}

	public void edit() throws ParseException {
		ArapAuditOrder arapAuditOrder = ArapAuditOrder.dao
				.findById(getPara("id"));
		String customerId = arapAuditOrder.get("payee_id");
		if (!"".equals(customerId) && customerId != null) {
			Party party = Party.dao.findById(customerId);
			setAttr("party", party);
			Contact contact = Contact.dao.findById(party.get("contact_id")
					.toString());
			setAttr("customer", contact);
		}
		UserLogin userLogin = UserLogin.dao.findById(arapAuditOrder
				.get("create_by"));
		setAttr("userLogin", userLogin);
		setAttr("arapAuditOrder", arapAuditOrder);

		Date beginTimeDate = arapAuditOrder.get("begin_time");
		Date endTimeDate = arapAuditOrder.get("end_time");
		String beginTime = "";
		String endTime = "";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		if (!"".equals(beginTimeDate) && beginTimeDate != null) {
			beginTime = simpleDateFormat.format(beginTimeDate);
		}
		if (!"".equals(endTimeDate) && endTimeDate != null) {
			endTime = simpleDateFormat.format(endTimeDate);
		}
		setAttr("beginTime", beginTime);
		setAttr("endTime", endTime);
		if (LoginUserController.isAuthenticated(this))
			render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}

	public void returnOrderList() {
		String chargeCheckOrderId = getPara("chargeCheckOrderId");
		if (chargeCheckOrderId == null || "".equals(chargeCheckOrderId)) {
			chargeCheckOrderId = "-1";
		}
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map orderMap = new HashMap();
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from return_order ro ";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select sum(tofi.amount) amount,ror.*, usl.user_name as creator_name,dor.order_no as delivery_order_no, ifnull(c.contact_person,c2.contact_person) cname, "
						+ " ifnull(tor.order_no,(select group_concat(tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no from arap_audit_order aao "
						+ "	left join arap_audit_item aai on aai.audit_order_id = aao.id"
						+ "	left join return_order ror on ror.id = aai.ref_order_id"
						+ "	left join transfer_order tor on tor.id = ror.transfer_order_id  left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id"
						+ "	left join delivery_order dor on ror.delivery_order_id = dor.id left join delivery_order_item doi on doi.delivery_id = dor.id"
						+ "	left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=ror.creator "
						+ "	left join transfer_order_fin_item tofi on tofi.order_id = ifnull(tor.id,tor2.id)"
						+ "	left join fin_item fi on fi.id = tofi.fin_item_id"
						+ "	where fi.type = '应收' and aai.ref_order_id = ror.id and aao.id = "
						+ chargeCheckOrderId
						+ " order by ror.create_date desc " + sLimit);

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}

}
