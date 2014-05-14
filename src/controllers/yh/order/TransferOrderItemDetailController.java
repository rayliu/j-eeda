package controllers.yh.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;

import controllers.yh.LoginUserController;

public class TransferOrderItemDetailController extends Controller {

	private Logger logger = Logger.getLogger(TransferOrderItemDetailController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void transferOrderItemList() {
		List<TransferOrderItem> transferOrderItems = TransferOrderItem.dao.find("select * from transfer_order_item");
		renderJson(transferOrderItems);
	}

	public void edit1() {
		long id = getParaToLong();

		Party party = Party.dao.findById(id);
		setAttr("party", party);

		Contact contact = Contact.dao.findFirst("select * from contact where id=?", party.getLong("contact_id"));
		setAttr("contact", contact);
		if(LoginUserController.isAuthenticated(this))
		render("transferOrder/transferOrderEdit.html");
	}

	public void edit() {
		if(LoginUserController.isAuthenticated(this))
		render("transferOrder/editTransferOrder.html");
	}

	public void delete() {
		long id = getParaToLong();

		Party party = Party.dao.findById(id);
		party.delete();

		Contact contact = Contact.dao.findFirst("select * from contact where id=?", party.getLong("contact_id"));
		contact.delete();
		if(LoginUserController.isAuthenticated(this))
		redirect("/yh/transferOrder");
	}

	// 保存单品
	public void saveTransferOrderItemDetail() {
		TransferOrderItemDetail item = null;
		String id = getPara("transfer_order_item_detail_id");
		if (id != null && !id.equals("")) {
			item = TransferOrderItemDetail.dao.findById(id);
			item.set("serial_no", getPara("update_serial_no"));
			item.set("item_name", getPara("update_detail_item_name"));
			item.set("volume", getPara("update_detail_volume"));
			item.set("weight", getPara("update_detail_weight"));
			item.set("remark", getPara("update_detail_remark"));
			item.set("is_damage", getPara("update_detail_is_damage"));
			item.set("estimate_damage_amount", getPara("update_detail_estimate_damage_amount"));
			item.set("damage_revenue", getPara("update_detail_damage_revenue"));
			item.set("damage_payment", getPara("update_detail_damage_payment"));
			item.set("damage_remark", getPara("update_detail_damage_remark"));
			Party party = Party.dao.findById(getPara("notify_party_id"));
			Contact contact = Contact.dao.findFirst("select * from contact where id=(select contact_id from party where id="+party.get("id")+")");
			contact.set("contact_person", getPara("update_detail_contact_person"));
			contact.set("phone", getPara("update_detail_phone"));
			contact.set("address", getPara("update_detail_address"));
			contact.update();
			party.set("contact_id", contact.get("id"));
			party.update();
			
			item.set("notify_party_id", party.get("id"));
			item.set("order_id", getPara("transfer_order_id"));
			item.set("item_id", getPara("transfer_order_item_id"));
			item.update();
		} else {
			item = new TransferOrderItemDetail();
			item.set("serial_no", getPara("serial_no"));
			item.set("item_name", getPara("detail_item_name"));
			item.set("volume", getPara("detail_volume"));
			item.set("weight", getPara("detail_weight"));
			item.set("remark", getPara("detail_remark"));
			item.set("is_damage", getPara("detail_is_damage"));
			item.set("estimate_damage_amount", getPara("detail_estimate_damage_amount"));
			item.set("damage_revenue", getPara("detail_damage_revenue"));
			item.set("damage_payment", getPara("detail_damage_payment"));
			item.set("damage_remark", getPara("detail_damage_remark"));
			Party party = setParty();

			item.set("notify_party_id", party.get("id"));
			item.set("order_id", getPara("transfer_order_id"));
			item.set("item_id", getPara("transfer_order_item_id"));
			item.save();
		}
		renderJson(item);
	}

	// 保存收货人
	private Party setParty() {
		Party party = new Party();
		Contact contact = new Contact();
		contact.set("contact_person", getPara("detail_contact_person"));
		contact.set("phone", getPara("detail_phone"));
		contact.set("address", getPara("detail_address"));
		contact.save();
		party.set("contact_id", contact.get("id"));
		party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
		party.save();
		return party;
	}

	// 获取getTransferOrderItemDetail对象
	public void getTransferOrderItemDetail() {
		Map<String,Object> map = new HashMap<String, Object>();
		String id = getPara("detail_id");
		String notify_party_id = getPara("notify_party_id");
		TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao.findById(id);
		Contact contact = (Contact) Contact.dao.findFirst("select * from contact where id=(select contact_id from party where id="+ notify_party_id + ")");
		map.put("transferOrderItemDetail", transferOrderItemDetail);
		map.put("contact", contact);
		renderJson(map);
	}

	// 删除TransferOrderItem
	public void deleteTransferOrderItemDetail() {
		String notify_party_id = getPara("notify_party_id");
		Party party = Party.dao.findById(getPara("notify_party_id"));
		Contact contact = Contact.dao.findFirst("select * from contact where id=(select contact_id from party where id="+party.get("id")+")");
		party.set("contact_id", null);
		contact.delete();
		String id = getPara("detail_id");
		TransferOrderItemDetail transferOrderItemDetail = TransferOrderItemDetail.dao.findById(id);
		transferOrderItemDetail.set("order_id", null);
		transferOrderItemDetail.set("item_id", null);
		transferOrderItemDetail.set("notify_party_id", null);
		TransferOrderItemDetail.dao.deleteById(id);
		party.delete();
		renderJson("{\"success\":true}");
	}

	// 获取所有单品
	public void getAllTransferOrderItemDetail() {
		String item_id = getPara("transfer_order_item_id");
		Map<String, List> map = new HashMap<String, List>();
		List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find("select * from TRANSFER_ORDER_ITEM_DETAIL where item_id=" + item_id);
		List<Contact> contacts = Contact.dao.find("select * from contact  where id in(select contact_id from party where id in(SELECT NOTIFY_PARTY_ID FROM TRANSFER_ORDER_ITEM_DETAIL where item_id="+ item_id + "))");
		/*
		 * (
		 * "select d.*,(select c.contact_person from contact c where id in(select contact_id from party where id in(SELECT NOTIFY_PARTY_ID FROM TRANSFER_ORDER_ITEM_DETAIL where item_id="
		 * + item_id +
		 * ")))  contact_person,(select c.phone from contact c where id in(select contact_id from party where id in(SELECT NOTIFY_PARTY_ID FROM TRANSFER_ORDER_ITEM_DETAIL where item_id="
		 * + item_id +
		 * "))) phone,(select c.address from contact c where id in(select contact_id from party where id in(SELECT NOTIFY_PARTY_ID FROM TRANSFER_ORDER_ITEM_DETAIL where item_id="
		 * + item_id +
		 * "))) address from TRANSFER_ORDER_ITEM_DETAIL d where item_id=" +
		 * item_id);
		 */
		map.put("transferOrderItemDetails", transferOrderItemDetails);
		map.put("contacts", contacts);
		renderJson(map);
	}
}
