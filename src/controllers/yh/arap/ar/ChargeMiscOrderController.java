package controllers.yh.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;
import models.ArapChargeInvoiceApplication;
import models.ArapChargeOrder;
import models.ArapMiscChargeOrder;
import models.ArapMiscChargeOrderItem;
import models.Party;
import models.UserLogin;
import models.yh.profile.Contact;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.LoginUserController;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeMiscOrderController extends Controller {
	private Logger logger = Logger.getLogger(ChargeMiscOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CPIO_LIST })
	public void index() {
		render("/yh/arap/ChargeMiscOrder/ChargeMiscOrderList.html");
	}

	public void confirm() {
		String ids = getPara("ids");
		String[] idArray = ids.split(",");
		logger.debug(String.valueOf(idArray.length));

		String customerId = getPara("customerId");
		Party party = Party.dao.findById(customerId);

		Contact contact = Contact.dao.findById(party.get("contact_id")
				.toString());
		setAttr("customer", contact);
		setAttr("type", "CUSTOMER");
		setAttr("classify", "receivable");
		render("/yh/arap/ChargeAcceptOrder/ChargeCheckOrderEdit.html");
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CPIO_LIST })
	public void list() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from arap_misc_charge_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select amco.*,aco.order_no charge_order_no from arap_misc_charge_order amco"
				+ " left join arap_charge_order aco on aco.id = amco.charge_order_id order by amco.create_stamp desc "
				+ sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CPIO_CREATE })
	public void create() {
		String ids = getPara("ids");
		if (ids != null && !"".equals(ids)) {
			String[] idArray = ids.split(",");
			logger.debug(String.valueOf(idArray.length));

			setAttr("chargeCheckOrderIds", ids);
			ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao
					.findById(idArray[0]);
			Long customerId = arapChargeOrder.get("payee_id");
			if (!"".equals(customerId) && customerId != null) {
				Party party = Party.dao.findById(customerId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get("contact_id")
						.toString());
				setAttr("customer", contact);
				setAttr("type", "CUSTOMER");
				setAttr("classify", "");
			}
		}

		setAttr("saveOK", false);
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));

		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);
		setAttr("status", "new");

		List<Record> itemList = Collections.emptyList();
		setAttr("itemList", itemList);

		render("/yh/arap/ChargeMiscOrder/ChargeMiscOrderEdit.html");
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CPIO_CREATE,
			PermissionConstant.PERMSSION_CPIO_UPDATE }, logical = Logical.OR)
	@Before(Tx.class)
	public void save() throws Exception {
		String jsonStr = getPara("params");
		logger.debug(jsonStr);
		Gson gson = new Gson();
		Map<String, ?> dto = gson.fromJson(jsonStr, HashMap.class);

		ArapMiscChargeOrder arapMiscChargeOrder = new ArapMiscChargeOrder();
		String chargeMiscOrderId = (String) dto.get("chargeMiscOrderId");
		String biz_type = (String) dto.get("biz_type");
		String charge_from_type = (String) dto.get("charge_from_type");
		String customer_id = (String) dto.get("customer_id");
		String sp_id = (String) dto.get("sp_id");
		String others_name = (String) dto.get("others_name");
		String ref_no = (String) dto.get("ref_no");
		Double amount = (Double) dto.get("amount");
		String remark = (String) dto.get("remark");

		UserLogin user = LoginUserController.getLoginUser(this);
		String old_biz_type = "";
		if (!"".equals(chargeMiscOrderId) && chargeMiscOrderId != null) {
			arapMiscChargeOrder = ArapMiscChargeOrder.dao
					.findById(chargeMiscOrderId);

			old_biz_type = arapMiscChargeOrder.getStr("type");
			arapMiscChargeOrder.set("type", biz_type);
			arapMiscChargeOrder.set("charge_from_type", charge_from_type);
			if (sp_id != null && !"".equals(sp_id)) {
				arapMiscChargeOrder.set("sp_id", sp_id);
			}
			if (customer_id != null && !"".equals(customer_id)) {
				arapMiscChargeOrder.set("customer_id", customer_id);
			}
			arapMiscChargeOrder.set("total_amount", amount);
			arapMiscChargeOrder.set("others_name", others_name);
			arapMiscChargeOrder.set("ref_no", ref_no);
			arapMiscChargeOrder.set("remark", remark);
			arapMiscChargeOrder.update();
		} else {
			arapMiscChargeOrder.set("status", "新建");
			arapMiscChargeOrder.set("type", biz_type);
			arapMiscChargeOrder.set("charge_from_type", charge_from_type);

			if (!"".equals(customer_id) && customer_id != null) {
				arapMiscChargeOrder.set("customer_id", customer_id);
			}
			if (sp_id != null && !"".equals(sp_id)) {
				arapMiscChargeOrder.set("sp_id", sp_id);
			}

			arapMiscChargeOrder.set("order_no",
					OrderNoGenerator.getNextOrderNo("SGSK"));
			if (getPara("chargeCheckOrderIds") != null
					&& !"".equals(getPara("chargeCheckOrderIds"))) {
				arapMiscChargeOrder.set("charge_order_id",
						getPara("chargeCheckOrderIds"));
			}

			arapMiscChargeOrder.set("total_amount", amount);
			arapMiscChargeOrder.set("others_name", others_name);
			arapMiscChargeOrder.set("ref_no", ref_no);

			arapMiscChargeOrder.set("create_by", user.getLong("id"));
			arapMiscChargeOrder.set("create_stamp", new Date());
			arapMiscChargeOrder.set("office_id", user.getLong("office_id"));
			arapMiscChargeOrder.set("remark", getPara("remark"));
			arapMiscChargeOrder.save();
		}

		Db.update(
				"delete from arap_misc_charge_order_item where misc_order_id = ?",
				chargeMiscOrderId);

		List<Map> items = (List<Map>) dto.get("items");
		for (Map item : items) {
			ArapMiscChargeOrderItem arapMiscChargeOrderItem = new ArapMiscChargeOrderItem();

			arapMiscChargeOrderItem.set("status", "新建");
			arapMiscChargeOrderItem.set("creator", user.get("id"));
			arapMiscChargeOrderItem.set("create_date", new Date());
			arapMiscChargeOrderItem.set("customer_order_no",
					item.get("CUSTOMER_ORDER_NO"));
			arapMiscChargeOrderItem.set("item_desc", item.get("ITEM_DESC"));
			arapMiscChargeOrderItem.set("fin_item_id", item.get("NAME"));
			arapMiscChargeOrderItem.set("amount", item.get("AMOUNT"));
			arapMiscChargeOrderItem.set("misc_order_id",
					arapMiscChargeOrder.getLong("id"));
			arapMiscChargeOrderItem.save();
		}

		ArapMiscChargeOrder destOrder = null;
		if (!"".equals(chargeMiscOrderId) && chargeMiscOrderId != null) {// update
			// 1. 是从biz->non_biz, 新生成对应往来单
			if ("biz".equals(old_biz_type) && "non_biz".equals(biz_type)) {
				destOrder = buildNewChargeMiscOrder(arapMiscChargeOrder, user);
			} else if ("non_biz".equals(old_biz_type)
					&& "non_biz".equals(biz_type)) {
				// non_biz 不变，update 对应的信息，判断对应往来单状态是否是“新建”，
				// 是就删除整张单，不是则提示应为往来单已复核，不能改变
				deleteRefOrder(chargeMiscOrderId);
				destOrder = buildNewChargeMiscOrder(arapMiscChargeOrder, user);
			} else if ("non_biz".equals(old_biz_type) && "biz".equals(biz_type)) {
				// non_biz -> biz 删除整张对应的单，判断对应往来单状态是否是“新建”，
				// 是就删除整张单，不是则提示应为往来单已复核，不能改变
				deleteRefOrder(chargeMiscOrderId);
			}
		} else {// new
			destOrder = buildNewChargeMiscOrder(arapMiscChargeOrder, user);
		}

		if(destOrder!=null){
			arapMiscChargeOrder.set("ref_order_no", destOrder.getStr("order_no"));
		}
		renderJson(arapMiscChargeOrder);
	}

	private void deleteRefOrder(String originOrderId) throws Exception {
		ArapMiscChargeOrder refOrder = ArapMiscChargeOrder.dao.findFirst(
				"select * from arap_misc_charge_order where ref_order_id=?",
				originOrderId);
		if (refOrder == null)
			return;
		if (!"新建".equals(refOrder.getStr("status"))) {
			throw new Exception("对应的手工收入单已不是“新建”，不能修改本张单据。");
		}
		long refOrderId = refOrder.getLong("id");
		Db.update(
				"delete from arap_misc_charge_order_item where misc_order_id = ?",
				refOrderId);
		refOrder.delete();
	}

	private ArapMiscChargeOrder buildNewChargeMiscOrder(ArapMiscChargeOrder originOrder,
			UserLogin user) throws IllegalAccessException,
			InvocationTargetException {
		long originId = originOrder.getLong("id");

		ArapMiscChargeOrder orderDest = new ArapMiscChargeOrder();
		orderDest.set("type", originOrder.getStr("type"));
		orderDest.set("charge_from_type", originOrder.getStr("charge_from_type"));
		orderDest.set("sp_id", originOrder.getStr("sp_id"));
		orderDest.set("customer_id", originOrder.getStr("customer_id"));
		orderDest.set("total_amount", 0-originOrder.getDouble("total_amount"));
		orderDest.set("others_name", originOrder.getStr("others_name"));
		orderDest.set("ref_no", originOrder.getStr("ref_no"));
		orderDest.set("create_by", user.getLong("id"));
		orderDest.set("office_id", user.getLong("office_id"));
		orderDest.set("status", "新建");

		orderDest.set("order_no", OrderNoGenerator.getNextOrderNo("SGSK"));
		orderDest.set("ref_order_id", originId);
		orderDest.set("ref_order_no", originOrder.get("order_no"));
		orderDest.save();

		List<ArapMiscChargeOrderItem> originItems = ArapMiscChargeOrderItem.dao
				.find("select * from arap_misc_charge_order_item where misc_order_id = ?",
						originId);

		for (ArapMiscChargeOrderItem originItem : originItems) {
			ArapMiscChargeOrderItem newItem = new ArapMiscChargeOrderItem();
			newItem.set("status", "新建");
			newItem.set("creator", user.getLong("id"));
			newItem.set("create_date", new Date());
			newItem.set("customer_order_no",
					originItem.get("customer_order_no"));
			newItem.set("item_desc", originItem.get("item_desc"));
			newItem.set("fin_item_id", originItem.get("fin_item_id"));

			newItem.set("misc_order_id", orderDest.get("id"));
			newItem.set("amount", 0 - originItem.getDouble("amount"));
			newItem.save();
		}
		return orderDest;
	}

	// 审核
	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CPIO_APPROVAL })
	public void auditChargePreInvoiceOrder() {
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		if (chargePreInvoiceOrderId != null
				&& !"".equals(chargePreInvoiceOrderId)) {
			ArapChargeInvoiceApplication arapAuditOrder = ArapChargeInvoiceApplication.dao
					.findById(chargePreInvoiceOrderId);
			arapAuditOrder.set("status", "已审核");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");
			arapAuditOrder.set("audit_by", users.get(0).get("id"));
			arapAuditOrder.set("audit_stamp", new Date());
			arapAuditOrder.update();
		}
		renderJson("{\"success\":true}");
	}

	// 审批
	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CPIO_CONFIRMATION })
	public void approvalChargePreInvoiceOrder() {
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		if (chargePreInvoiceOrderId != null
				&& !"".equals(chargePreInvoiceOrderId)) {
			ArapChargeInvoiceApplication arapAuditOrder = ArapChargeInvoiceApplication.dao
					.findById(chargePreInvoiceOrderId);
			arapAuditOrder.set("status", "已审批");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");
			arapAuditOrder.set("approver_by", users.get(0).get("id"));
			arapAuditOrder.set("approval_stamp", new Date());
			arapAuditOrder.update();
		}
		renderJson("{\"success\":true}");
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CPIO_UPDATE })
	public void edit() throws ParseException {
		String id = getPara("id");
		List<Record> receivableItemList = Collections.emptyList();
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);

		ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao
				.findById(id);
		UserLogin userLogin = UserLogin.dao.findById(arapMiscChargeOrder
				.get("create_by"));

		setAttr("userLogin", userLogin);
		
		if(arapMiscChargeOrder.getLong("ref_order_id")!=null){
			ArapMiscChargeOrder refOrder = ArapMiscChargeOrder.dao
					.findFirst("select amcoi.* from arap_misc_charge_order amcoi where id=?", arapMiscChargeOrder.getLong("ref_order_id"));
				if(refOrder!=null)
					setAttr("ref_order_no", refOrder.get("order_no"));
		}else{
			ArapMiscChargeOrder refOrder = ArapMiscChargeOrder.dao
				.findFirst("select amcoi.* from arap_misc_charge_order amcoi where ref_order_id=?", id);
			if(refOrder!=null)
				setAttr("ref_order_no", refOrder.get("order_no"));
		}
		setAttr("arapMiscChargeOrder", arapMiscChargeOrder);

		Record r = Db
				.findFirst("select * from party p left join contact c on p.contact_id = c.id where p.id = '"
						+ arapMiscChargeOrder.getLong("customer_id") + "'");
		if (r != null)
			setAttr("customer_name", r.get("company_name"));
		Record q = Db
				.findFirst("select * from party p left join contact c on p.contact_id = c.id where p.id = '"
						+ arapMiscChargeOrder.getLong("sp_id") + "'");
		if (q != null)
			setAttr("sp_name", q.get("company_name"));

		// 获取从表
		List<Record> itemList = Db
				.find("select amcoi.*,amco.order_no charge_order_no,c.abbr cname,fi.name name from arap_misc_charge_order_item amcoi"
						+ " left join arap_misc_charge_order amco on amco.id = amcoi.misc_order_id"
						+ " left join arap_charge_order aco on aco.id = amco.charge_order_id"
						+ " left join party p on p.id = aco.payee_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join fin_item fi on amcoi.fin_item_id = fi.id"
						+ " where amco.id =?", id);
		setAttr("itemList", itemList);
		render("/yh/arap/ChargeMiscOrder/ChargeMiscOrderEdit.html");
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CPIO_CREATE })
	public void chargeMiscOrderItemList() {
		String chargeMiscOrderId = getPara("chargeMiscOrderId");
		if (chargeMiscOrderId == null || "".equals(chargeMiscOrderId)) {
			chargeMiscOrderId = "-1";
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
		String sql = "select count(1) total from arap_misc_charge_order_item amcoi "
				+ " left join arap_misc_charge_order amco on amco.id = amcoi.misc_order_id where amco.id = "
				+ chargeMiscOrderId;
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select amcoi.*,amco.order_no charge_order_no,c.abbr cname,fi.name name from arap_misc_charge_order_item amcoi"
						+ " left join arap_misc_charge_order amco on amco.id = amcoi.misc_order_id"
						+ " left join arap_charge_order aco on aco.id = amco.charge_order_id"
						+ " left join party p on p.id = aco.payee_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join fin_item fi on amcoi.fin_item_id = fi.id"
						+ " where amco.id = "
						+ chargeMiscOrderId
						+ " "
						+ sLimit);

		/*
		 * orderMap.put("sEcho", pageIndex); orderMap.put("iTotalRecords",
		 * rec.getLong("total")); orderMap.put("iTotalDisplayRecords",
		 * rec.getLong("total")); orderMap.put("aaData", orders);
		 */

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}

	public void searchAllAccount() {
		List<Account> accounts = Account.dao
				.find("select * from fin_account where type != 'PAY' and bank_name != '现金'");
		renderJson(accounts);
	}

	public void addNewFee() {
		ArapMiscChargeOrderItem arapMiscChargeOrderItem = new ArapMiscChargeOrderItem();
		arapMiscChargeOrderItem.set("status", "新建");
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		arapMiscChargeOrderItem.set("creator", users.get(0).get("id"));
		arapMiscChargeOrderItem.set("create_date", new Date());
		arapMiscChargeOrderItem.set("fin_item_id", 4);
		arapMiscChargeOrderItem.set("misc_order_id",
				getPara("chargeMiscOrderId"));
		arapMiscChargeOrderItem.save();
		renderJson(arapMiscChargeOrderItem);
	}

	public void updateChargeMiscOrderItem() {
		String paymentId = getPara("paymentId");
		String chargeMiscOrderId = getPara("chargeMiscOrderId");
		String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
		String name = getPara("name");
		String value = getPara("value");
		if ("amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		if (paymentId != null && !"".equals(paymentId)) {
			ArapMiscChargeOrderItem arapMiscChargeOrderItem = ArapMiscChargeOrderItem.dao
					.findById(paymentId);
			arapMiscChargeOrderItem.set(name, value);
			arapMiscChargeOrderItem.update();
		}
		ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao
				.findById(chargeMiscOrderId);
		Record record = Db
				.findFirst(
						"select sum(amount) sum_amount from arap_misc_charge_order_item where misc_order_id = ?",
						chargeMiscOrderId);
		arapMiscChargeOrder.set("total_amount", record.get("sum_amount"));
		arapMiscChargeOrder.update();

		if (chargeCheckOrderIds != null && !"".equals(chargeCheckOrderIds)) {
			if ("amount".equals(name) && value != null && !"".equals(value)) {
				ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao
						.findById(chargeCheckOrderIds);
				Double debit_amount = arapChargeOrder.getDouble("debit_amount");
				Double misc_total_amount = Double.parseDouble(value);
				Double total_amount = arapChargeOrder.getDouble("total_amount");
				arapChargeOrder.set("debit_amount", debit_amount
						+ misc_total_amount);
				arapChargeOrder.set("charge_amount", total_amount
						- (debit_amount + misc_total_amount));
				arapChargeOrder.update();
			}
		}
		renderJson(arapMiscChargeOrder);
	}

	public void finItemdel() {
		// ArapMiscChargeOrderItem.dao.deleteById(getPara());
		ArapMiscChargeOrderItem arapMiscChargeOrderItem = ArapMiscChargeOrderItem.dao
				.findById(getPara());
		ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao
				.findById(arapMiscChargeOrderItem.getLong("misc_order_id"));
		Double Tamount = arapMiscChargeOrder.getDouble("total_amount");
		Double amount = arapMiscChargeOrderItem.getDouble("amount");
		if ((Tamount != null && !Tamount.equals(""))
				&& (amount != null && !amount.equals(""))) {
			Double total_amount = arapMiscChargeOrder.getDouble("total_amount")
					- arapMiscChargeOrderItem.getDouble("amount");
			arapMiscChargeOrder.set("total_amount", total_amount).update();
		}
		arapMiscChargeOrderItem.delete();
		renderJson(arapMiscChargeOrder);
	}

	public void chargeCheckList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
		if (chargeCheckOrderIds == null || "".equals(chargeCheckOrderIds)) {
			chargeCheckOrderIds = "-1";
		}
		String sqlTotal = "select count(1) total from arap_charge_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select distinct aao.*, usl.user_name as creator_name,c.abbr cname"
				+ " from arap_charge_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login usl on usl.id=aao.create_by"
				+ " where aao.id in ("
				+ chargeCheckOrderIds
				+ ") order by aao.create_stamp desc " + sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	public void chargeCheckOrderList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(distinct aao.id) total from arap_charge_order aao"
				+ " left join arap_misc_charge_order amco on amco.charge_order_id = aao.id"
				+ " where ifnull(amco.charge_order_id,0)>0";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select distinct aao.*, usl.user_name as creator_name,c.abbr cname"
				+ " from arap_charge_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login usl on usl.id=aao.create_by"
				+ " left join arap_misc_charge_order amco on amco.charge_order_id = aao.id"
				+ " where ifnull(amco.charge_order_id,0)>0"
				+ " order by aao.create_stamp desc " + sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}
}
