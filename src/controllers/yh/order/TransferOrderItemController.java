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

    public void transferOrderItemList(){
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
    	TransferOrderItem item = new TransferOrderItem();
        item.set("item_name",getPara("item_name"));
        item.set("amount",getPara("amount"));
        item.set("unit",getPara("unit"));
        item.set("volume",getPara("volume"));
        item.set("weight",getPara("weight"));   
        item.set("remark",getPara("remark"));   
        item.set("order_id",getPara("transfer_order_id"));   
        item.save();
        renderJson(item.get("id"));
    }
}
