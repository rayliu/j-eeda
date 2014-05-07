package controllers.yh.order;

import java.util.List;

import models.Party;
import models.TransferOrderItem;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;

public class TransferOrderItemController extends Controller {

	private Logger logger = Logger.getLogger(TransferOrderItemController.class);
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

		render("transferOrder/transferOrderEdit.html");
	}

	public void edit() {
		render("transferOrder/editTransferOrder.html");
	}

	public void delete() {
		long id = getParaToLong();

		Party party = Party.dao.findById(id);
		party.delete();

		Contact contact = Contact.dao.findFirst("select * from contact where id=?", party.getLong("contact_id"));
		contact.delete();

		redirect("/yh/transferOrder");
	}

	// 保存货品
	public void saveTransferOrderItem() {
		TransferOrderItem item = null;
		String id = getPara("transfer_order_item_id");
		if (id != null && !id.equals("")) {
			item = TransferOrderItem.dao.findById(id);
			item.set("item_name", getPara("update_item_name"));
			item.set("amount", getPara("update_amount"));
			item.set("unit", getPara("update_unit"));
			item.set("volume", getPara("update_volume"));
			item.set("weight", getPara("update_weight"));
			item.set("remark", getPara("update_remark"));
			item.set("order_id", getPara("transfer_order_id"));
			item.update();
		} else {
			item = new TransferOrderItem();
			item.set("item_name", getPara("item_name"));
			item.set("amount", getPara("amount"));
			item.set("unit", getPara("unit"));
			item.set("volume", getPara("volume"));
			item.set("weight", getPara("weight"));
			item.set("remark", getPara("remark"));
			item.set("order_id", getPara("transfer_order_id"));
			item.save();
		}
		renderJson(item);
	}

	// 获取TransferOrderItem对象
	public void getTransferOrderItem() {
		String id = getPara("transfer_order_item_id");
		TransferOrderItem transferOrderItem = TransferOrderItem.dao.findById(id);
		renderJson(transferOrderItem);
	}

	// 删除TransferOrderItem
	public void deleteTransferOrderItem() {
		String id = getPara("transfer_order_item_id");
		TransferOrderItem.dao.deleteById(id);
		renderJson("{\"success\":true}");
	}
}
