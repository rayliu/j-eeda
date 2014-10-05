package controllers.yh.arap.ar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeInvoiceApplication;
import models.ArapChargeInvoiceApplicationItem;
import models.ArapChargeOrder;
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

public class ChargePreInvoiceOrderController extends Controller {
    private Logger logger = Logger.getLogger(ChargePreInvoiceOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

    public void index() {
    	if(LoginUserController.isAuthenticated(this))
    	    render("/yh/arap/ChargePreInvoiceOrder/ChargePreInvoiceOrderList.html");
    }


    public void confirm() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        String customerId = getPara("customerId");
        Party party = Party.dao.findById(customerId);

        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
        setAttr("customer", contact);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
    	if(LoginUserController.isAuthenticated(this))
        render("/yh/arap/ChargeAcceptOrder/ChargeCheckOrderEdit.html");
    }

    // 应付条目列表
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_charge_invoice_application_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aaia.*,ul.user_name create_by,ul2.user_name charge_by,ul3.user_name approval_by from arap_charge_invoice_application_order aaia "
				+ " left join user_login ul on ul.id = aaia.create_by"
				+ " left join user_login ul2 on ul2.id = aaia.charge_by"
				+ " left join user_login ul3 on ul3.id = aaia.approver_by order by aaia.create_stamp desc " + sLimit;

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }

	public void create() {
		String ids = getPara("ids");
		String[] idArray = ids.split(",");
		logger.debug(String.valueOf(idArray.length));

		setAttr("chargeCheckOrderIds", ids);
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

		ArapChargeInvoiceApplication order = ArapChargeInvoiceApplication.dao
				.findFirst("select * from arap_charge_invoice_application_order order by order_no desc limit 0,1");
		if (order != null) {
			String num = order.get("order_no");
            // TODO num.substring(2, num.length()); 该方法不通用
            String str = num.substring(4, num.length());
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
			setAttr("order_no", "YSSQ" + order_no);
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String format = sdf.format(new Date());
			order_no = format + "00001";
			setAttr("order_no", "YSSQ" + order_no);
		}

		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		setAttr("status", "new");
		if (LoginUserController.isAuthenticated(this))
			render("/yh/arap/ChargePreInvoiceOrder/ChargePreInvoiceOrderEdit.html");
	}

	public void save() {
		ArapChargeInvoiceApplication arapAuditInvoiceApplication = null;
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		if (!"".equals(chargePreInvoiceOrderId) && chargePreInvoiceOrderId != null) {
			arapAuditInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId);
			arapAuditInvoiceApplication.set("order_no", getPara("order_no"));
			// arapAuditOrder.set("order_type", );
			arapAuditInvoiceApplication.set("status", "new");
			arapAuditInvoiceApplication.set("payee_id", getPara("customer_id"));
			arapAuditInvoiceApplication.set("create_by", getPara("create_by"));
			arapAuditInvoiceApplication.set("create_stamp", new Date());
			arapAuditInvoiceApplication.set("remark", getPara("remark"));
			arapAuditInvoiceApplication.set("last_modified_by", getPara("create_by"));
			arapAuditInvoiceApplication.set("last_modified_stamp", new Date());
			arapAuditInvoiceApplication.update();
		} else {
			arapAuditInvoiceApplication = new ArapChargeInvoiceApplication();
			arapAuditInvoiceApplication.set("order_no", getPara("order_no"));
			arapAuditInvoiceApplication.set("status", "新建");
			//arapAuditInvoiceApplication.set("payee_id", getPara("customer_id"));
			arapAuditInvoiceApplication.set("create_by", getPara("create_by"));
			arapAuditInvoiceApplication.set("create_stamp", new Date());
			arapAuditInvoiceApplication.set("remark", getPara("remark"));
			arapAuditInvoiceApplication.save();

			String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
			String[] chargeCheckOrderIdsArr = chargeCheckOrderIds.split(",");
			for (int i = 0; i < chargeCheckOrderIdsArr.length; i++) {
				ArapChargeInvoiceApplicationItem arapAuditInvoiceApplicationItem = new ArapChargeInvoiceApplicationItem();
				arapAuditInvoiceApplicationItem.set("invoice_application_id", arapAuditInvoiceApplication.get("id"));
				arapAuditInvoiceApplicationItem.set("charge_order_id", chargeCheckOrderIdsArr[i]);
				arapAuditInvoiceApplicationItem.save();
				
				ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(chargeCheckOrderIdsArr[i]);
				arapAuditOrder.set("status", "开票申请中");
				arapAuditOrder.update();
			}
		}
		renderJson(arapAuditInvoiceApplication);;
	}
	
	// 审核
	public void auditChargePreInvoiceOrder(){
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		if(chargePreInvoiceOrderId != null && !"".equals(chargePreInvoiceOrderId)){
			ArapChargeInvoiceApplication arapAuditOrder = ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId);
			arapAuditOrder.set("status", "已审核");
            String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
			arapAuditOrder.set("charge_by", users.get(0).get("id"));
			arapAuditOrder.set("charge_stamp", new Date());
			arapAuditOrder.update();
		}
        renderJson("{\"success\":true}");
	}
	
	// 审批
	public void approvalChargePreInvoiceOrder(){
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		if(chargePreInvoiceOrderId != null && !"".equals(chargePreInvoiceOrderId)){
			ArapChargeInvoiceApplication arapAuditOrder = ArapChargeInvoiceApplication.dao.findById(chargePreInvoiceOrderId);
			arapAuditOrder.set("status", "已审批");
            String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
			arapAuditOrder.set("approver_by", users.get(0).get("id"));
			arapAuditOrder.set("approval_stamp", new Date());
			arapAuditOrder.update();
		}
		renderJson("{\"success\":true}");
	}

	public void edit() throws ParseException {
		String id = getPara("id");
		ArapChargeInvoiceApplication arapAuditInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(id);
		String customerId = arapAuditInvoiceApplication.get("payee_id");
		if (!"".equals(customerId) && customerId != null) {
			Party party = Party.dao.findById(customerId);
			setAttr("party", party);
			Contact contact = Contact.dao.findById(party.get("contact_id")
					.toString());
			setAttr("customer", contact);
		}
		UserLogin userLogin = UserLogin.dao.findById(arapAuditInvoiceApplication.get("create_by"));
		setAttr("userLogin", userLogin);
		setAttr("arapAuditInvoiceApplication", arapAuditInvoiceApplication);

		Date beginTimeDate = arapAuditInvoiceApplication.get("begin_time");
		Date endTimeDate = arapAuditInvoiceApplication.get("end_time");
		String beginTime = "";
		String endTime = "";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		if (!"".equals(beginTimeDate) && beginTimeDate != null) {
			beginTime = simpleDateFormat.format(beginTimeDate);
		}
		if (!"".equals(endTimeDate) && endTimeDate != null) {
			endTime = simpleDateFormat.format(endTimeDate);
		}
		String chargeCheckOrderIds = "";
		List<ArapChargeInvoiceApplicationItem> arapAuditInvoiceApplicationItems = ArapChargeInvoiceApplicationItem.dao.find("select * from arap_charge_invoice_application_item where charge_order_id = ?", id);
		for(ArapChargeInvoiceApplicationItem arapAuditInvoiceApplicationItem : arapAuditInvoiceApplicationItems){
			chargeCheckOrderIds += arapAuditInvoiceApplicationItem.get("charge_order_id") + ",";
		}
		chargeCheckOrderIds = chargeCheckOrderIds.substring(0, chargeCheckOrderIds.length() - 1);
		setAttr("chargeCheckOrderIds", chargeCheckOrderIds);
		setAttr("beginTime", beginTime);
		setAttr("endTime", endTime);
		if (LoginUserController.isAuthenticated(this))
			render("/yh/arap/ChargePreInvoiceOrder/ChargePreInvoiceOrderEdit.html");
	}
	
	public void returnOrderList() {
		String returnOrderIds = getPara("returnOrderIds");
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
		String sql = "select count(1) total from return_order ror where ror.id in ("+returnOrderIds+")";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select sum(tofi.amount) amount,ror.id,ror.order_no,ror.transaction_status,ror.remark remark,ror.create_date,ror.receipt_date, usl.user_name as creator_name,dor.order_no as delivery_order_no,ifnull(c.abbr,c2.abbr) cname,"
						+ " ifnull(tor.order_no,(select	group_concat(tor3.order_no separator '\r\n') from delivery_order dor left join delivery_order_item doi2 on doi2.delivery_id = dor.id left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no"
						+ " from return_order ror" 
						+ " left join transfer_order tor on tor.id = ror.transfer_order_id"
						+ " left join party p on p.id = tor.customer_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join delivery_order dor on ror.delivery_order_id = dor.id"
						+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
						+ " left join transfer_order tor2 on tor2.id = doi.transfer_order_id"
						+ " left join party p2 on p2.id = tor2.customer_id"
						+ " left join contact c2 on c2.id = p2.contact_id"
						+ " left join user_login usl on usl.id = ror.creator"
						+ " left join transfer_order_fin_item tofi on tofi.order_id = ifnull(tor.id, tor2.id)"
						+ " left join fin_item fi on fi.id = tofi.fin_item_id"
						+ " where ror.id in ("+returnOrderIds+") group by ror.id order by ror.create_date desc " + sLimit);

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
