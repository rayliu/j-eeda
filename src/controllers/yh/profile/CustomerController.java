package controllers.yh.profile;import java.util.Calendar;import java.util.Date;import java.util.HashMap;import java.util.List;import java.util.Map;import models.Party;import models.TransferOrder;import models.yh.delivery.DeliveryOrder;import models.yh.profile.Contact;import com.jfinal.core.Controller;import com.jfinal.log.Logger;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;import controllers.yh.LoginUserController;public class CustomerController extends Controller {	private Logger logger = Logger.getLogger(CustomerController.class);	// in config route已经将路径默认设置为/yh	// me.add("/yh", controllers.yh.AppController.class, "/yh");	public void index() {		if(LoginUserController.isAuthenticated(this))		render("profile/customer/CustomerList.html");	}	public void list() {		/*		 * Paging		 */		String sLimit = "";		String pageIndex = getPara("sEcho");		if (getPara("iDisplayStart") != null				&& getPara("iDisplayLength") != null) {			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "					+ getPara("iDisplayLength");		}		String sqlTotal = "select count(1) total from party ";		Record rec = Db.findFirst(sqlTotal);		logger.debug("total records:" + rec.getLong("total"));		String sql = "select p.id, p.creator, p.create_date, c.company_name, c.contact_person from party p, contact c where p.party_type='CUSTOMER' and p.contact_id=c.id order by p.create_date desc ";		List<Record> customers = Db.find(sql);		Map customerListMap = new HashMap();		customerListMap.put("sEcho", pageIndex);		customerListMap.put("iTotalRecords", rec.getLong("total"));		customerListMap.put("iTotalDisplayRecords", rec.getLong("total"));		customerListMap.put("aaData", customers);		renderJson(customerListMap);	}	public void add() {		setAttr("saveOK", false);		if(LoginUserController.isAuthenticated(this))		render("profile/customer/CustomerEdit.html");	}	public void edit() {		String id = getPara();		Party party = Party.dao.findById(id);		setAttr("party", party);		Contact contact = Contact.dao.findFirst(				"select * from contact where id=?", Integer.parseInt(id));		setAttr("contact", contact);		if(LoginUserController.isAuthenticated(this))		render("profile/customer/CustomerEdit.html");	}	public void delete() {		 long id = getParaToLong();	                Party party = Party.dao.findById(id);        List<TransferOrder> transferOrders = TransferOrder.dao.find("select * from transfer_order where customer_id="+party.get("id"));        for(TransferOrder transferOrder : transferOrders){        	transferOrder.set("customer_id", null);        	transferOrder.update();        }        List<DeliveryOrder> deliveryOrders = DeliveryOrder.dao.find("select * from delivery_order where customer_id="+party.get("id"));        for(DeliveryOrder deliveryOrder : deliveryOrders){        	deliveryOrder.set("customer_id", null);        	deliveryOrder.update();        }        Contact contact = Contact.dao                .findFirst("select * from contact where id=?",                        party.getLong("contact_id"));        contact.delete();        party.delete();        if(LoginUserController.isAuthenticated(this))		redirect("/yh/customer");	}	public void save() {		String id = getPara("party_id");		Party party = null;		Contact contact = null;		Date createDate = Calendar.getInstance().getTime();		if (!id.equals("")) {			party = Party.dao.findById(id);			party.set("last_update_date", createDate).update();			contact = Contact.dao.findFirst("select * from contact where id=?",					Integer.parseInt(id));			setContact(contact);			contact.update();		} else {			contact = new Contact();			setContact(contact);			contact.save();			party = new Party();			party.set("party_type", Party.PARTY_TYPE_CUSTOMER);			party.set("contact_id", contact.getLong("id"));			party.set("creator", "test");			party.set("create_date", createDate);			party.save();		}		setAttr("saveOK", true);		if(LoginUserController.isAuthenticated(this))		render("profile/customer/CustomerEdit.html");	}	private void setContact(Contact contact) {		contact.set("company_name", getPara("company_name"));		contact.set("contact_person", getPara("contact_person"));		contact.set("email", getPara("email"));		contact.set("mobile", getPara("mobile"));		contact.set("phone", getPara("phone"));		contact.set("address", getPara("address"));		contact.set("city", getPara("city"));		contact.set("postal_code", getPara("postal_code"));	}}