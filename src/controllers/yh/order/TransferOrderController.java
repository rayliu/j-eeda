package controllers.yh.order;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class TransferOrderController extends Controller {
	
    private Logger logger = Logger.getLogger(TransferOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
    
	public void index() {
		render("profile/transferOrder/transferOrderList.html");
	}

	public void list() {
		/*
		 * Paging
		 */
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

		String sql = "select * from transfer_order";

		List<Record> transferOrders = Db.find(sql);

		Map transferOrderListMap = new HashMap();
		transferOrderListMap.put("sEcho", pageIndex);
		transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
		transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		transferOrderListMap.put("aaData", transferOrders);

		renderJson(transferOrderListMap);
	}

	public void add() {
		setAttr("saveOK", false);
		render("profile/transferOrder/transferOrderEdit.html");
	}

	public void edit() {
		long id = getParaToLong();

		Party party = Party.dao.findById(id);
		setAttr("party", party);

		Contact contact = Contact.dao
				.findFirst("select * from contact where id=?",
						party.getLong("contact_id"));
		setAttr("contact", contact);

		render("profile/transferOrder/transferOrderEdit.html");
	}

	public void delete() {
		long id = getParaToLong();

		Party party = Party.dao.findById(id);
		party.delete();

		Contact contact = Contact.dao
				.findFirst("select * from contact where id=?",
						party.getLong("contact_id"));
		contact.delete();

		redirect("/yh/transferOrder");
	}

	public void save() {

		String id = getPara("party_id");
		Party party = null;
		Contact contact = null;
		Date createDate = Calendar.getInstance().getTime();
		if (id != null && !id.equals("")) {
			party = Party.dao.findById(id);
			party.set("last_update_date", createDate).update();

			contact = Contact.dao.findFirst("select * from contact where id=?",
					party.getLong("contact_id"));
			setContact(contact);
			contact.update();
		} else {
			contact = new Contact();
			setContact(contact);
			contact.save();
			party = new Party();
			party.set("party_type", Party.PARTY_TYPE_SERVICE_PROVIDER);
			party.set("contact_id", contact.getLong("id"));
			party.set("creator", "test");
			party.set("create_date", createDate);
			party.save();

		}

		setAttr("saveOK", true);
		render("profile/transferOrder/transferOrderList.html");
	}

	private void setContact(Contact contact) {
		contact.set("company_name", getPara("company_name"));
		contact.set("contact_person", getPara("contact_person"));
		contact.set("email", getPara("email"));
		contact.set("mobile", getPara("mobile"));
		contact.set("phone", getPara("phone"));
		contact.set("address", getPara("address"));
		contact.set("city", getPara("city"));
		contact.set("postal_code", getPara("postal_code"));
	}
	
	// 客户列表,列出最近使用的5个客户
	public void selectCustomer(){
		List<Contact> contactjson = Contact.dao.find("select * from contact c  where id in (select contact_id from party where party_type='CUSTOMER' order by last_update_date desc limit 0,5)");			
        renderJson(contactjson);
	}
	
	// 保存客户 TODO 1.在新建页面不能直接显示,需重新进入才能显示刚添加的. 2.点击radio的label不能选中前面的按钮.	 3.显示与隐藏切换问题
	// 4.最近使用的5个客户:第一次进来时没有信息怎么处理?
	public void saveCustomer(){
		Party party = new Party();
		party.set("party_type", Party.PARTY_TYPE_CUSTOMER);
		Contact contact = new Contact();
		setContact(contact);
		contact.save();
		party.set("contact_id", contact.getLong("id"));
		party.set("create_date", new Date());
		party.set("creator", currentUser.getPrincipal());		
		party.save();
	}

	// 收货人列表
	public void selectContactr(){
		List<Contact> contacts = Contact.dao.find("select * from contact");			
		renderJson(contacts);
	}
	
}
