package controllers.yh.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.Product;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class TransferOrderItemController extends Controller {

	private Logger logger = Logger.getLogger(TransferOrderItemController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void transferOrderItemList() {
		String trandferOrderId = getPara("order_id");
		if(trandferOrderId.isEmpty()){
			trandferOrderId="-1";
		}		
		logger.debug(trandferOrderId);
		
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from transfer_order_item where order_id ="
                + trandferOrderId;
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select * from transfer_order_item where order_id ="
                + trandferOrderId;

        List<Record> transferOrders = Db.find(sql);

        Map transferOrderListMap = new HashMap();
        transferOrderListMap.put("sEcho", pageIndex);
        transferOrderListMap.put("iTotalRecords", rec.getLong("total"));
        transferOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        transferOrderListMap.put("aaData", transferOrders);

        renderJson(transferOrderListMap);
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

	// 保存货品
	public void saveTransferOrderItem() {
		TransferOrderItem item = null;
		String productId = getPara("productId");
		String id = getPara("transfer_order_item_id");
		if (id != null && !id.equals("")) {
			item = TransferOrderItem.dao.findById(id);
			item.set("item_no", getPara("update_item_no"));
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
			if(productId == null || "".equals(productId)){
				saveProduct();
			}else{
				updateProduct(productId);
			}
			item.set("item_no", getPara("item_no"));    
			item.set("item_name", getPara("item_name"));
			item.set("size", getPara("size"));          
			item.set("width", getPara("width"));        
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
	
	// 更新产品
	private void updateProduct(String productId) {
		Product product = Product.dao.findById(productId);
		product.set("item_no", getPara("item_no"));
		product.set("item_name", getPara("item_name"));
		product.set("size", getPara("size"));
		product.set("width", getPara("width"));
		product.set("amount", getPara("amount"));
		product.set("unit", getPara("unit"));
		product.set("volume", getPara("volume"));
		product.set("weight", getPara("weight"));
		product.set("item_desc", getPara("remark"));
		product.set("category_id", getPara("categorySelect"));
		product.update();
	}

	// 保存产品
	private void saveProduct() {
		Product product = new Product();
		product.set("item_no", getPara("item_no"));
		product.set("item_name", getPara("item_name"));
		product.set("size", getPara("size"));
		product.set("width", getPara("width"));
		product.set("amount", getPara("amount"));
		product.set("unit", getPara("unit"));
		product.set("volume", getPara("volume"));
		product.set("weight", getPara("weight"));
		product.set("item_desc", getPara("remark"));
		product.set("category_id", getPara("categorySelect"));
		product.save();
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
		List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where item_id="+id);
		for(TransferOrderItemDetail itemDetail : transferOrderItemDetails){
			itemDetail.delete();
		}
		TransferOrderItem.dao.deleteById(id);
		renderJson("{\"success\":true}");
	}
}
