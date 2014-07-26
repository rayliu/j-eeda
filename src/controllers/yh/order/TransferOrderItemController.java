package controllers.yh.order;

import java.util.Date;
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
        Map transferOrderListMap = null;
        String trandferOrderId = getPara("order_id");
        String productId = getPara("product_id");
        if (trandferOrderId == null || "".equals(trandferOrderId)) {
            trandferOrderId = "-1";
        }
        logger.debug(trandferOrderId);
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String sqlTotal = "select distinct count(1) total from transfer_order_item toi " + " left join product p on p.id = toi.product_id "
                + " where toi.order_id =" + trandferOrderId
                + " or toi.product_id in(select product_id from transfer_order_item where toi.order_id =" + trandferOrderId + ")";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        String sql = "";
        sql = "select distinct toi.id,ifnull(p.item_no,toi.item_no) item_no, ifnull(p.item_name,toi.item_name) item_name,"
                + " ifnull(p.size,toi.size) size, ifnull(p.width, toi.width) width, ifnull(p.height, toi.height) height,"
                + " ifnull(p.weight,toi.weight) weight, ifnull(p.volume, toi.volume) volume,toi.amount amount,"
                + " ifnull(p.unit,toi.unit) unit, toi.remark from transfer_order_item toi "
                + " left join product p on p.id = toi.product_id " + " where toi.order_id =" + trandferOrderId
                + " or toi.product_id in(select product_id from transfer_order_item where toi.order_id =" + trandferOrderId
                + ") order by toi.id" + sLimit;
        List<Record> transferOrders = Db.find(sql);
        transferOrderListMap = new HashMap();
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
        if (LoginUserController.isAuthenticated(this))
            render("transferOrder/transferOrderEdit.html");
    }

    public void edit() {
        if (LoginUserController.isAuthenticated(this))
            render("transferOrder/editTransferOrder.html");
    }

    public void delete() {
        long id = getParaToLong();

        Party party = Party.dao.findById(id);
        party.delete();

        Contact contact = Contact.dao.findFirst("select * from contact where id=?", party.getLong("contact_id"));
        contact.delete();
        if (LoginUserController.isAuthenticated(this))
            redirect("/yh/transferOrder");
    }

    // 保存货品
    public void saveTransferOrderItemByField() {
        String returnValue = "";
        String id = getPara("id");
        TransferOrderItem item = TransferOrderItem.dao.findById(id);
        Product product = null;
        String item_no = getPara("item_no");
        String item_name = getPara("item_name");
        String amount = getPara("amount");
        String unit = getPara("unit");
        String remark = getPara("remark");
        String size = getPara("size");
        String width = getPara("width");
        String height = getPara("height");
        String weight = getPara("weight");
        Long productId = item.getLong("product_id");
        if(productId == null || "".equals(productId)){
	        if (!"".equals(item_no) && item_no != null) {
	            item.set("item_no", item_no).update();
	            returnValue = item_no;
	        } else if (!"".equals(item_name) && item_name != null) {
	            item.set("item_name", item_name).update();
	            returnValue = item_name;
	        } else if (!"".equals(remark) && remark != null) {
	            item.set("remark", remark).update();
	            returnValue = remark;
	        } else if (!"".equals(size) && size != null) {
	            item.set("size", size).update();
	            returnValue = size;
	        } else if (!"".equals(width) && width != null) {
	            item.set("width", width).update();
	            returnValue = width;
	        } else if (!"".equals(height) && height != null) {
	            item.set("height", height).update();
	            returnValue = height;
	        } else if (!"".equals(weight) && weight != null) {
	            item.set("weight", weight).update();
	            returnValue = weight;
	        } else if (!"".equals(amount) && amount != null) {
	            item.set("amount", amount).update();
	            if (amount != null && !"".equals(amount)) {
	                saveTransferOrderDetail(item, productId);
	            }
	            returnValue = amount;
	        } else if (!"".equals(unit) && unit != null) {
	            item.set("unit", unit).update();
	            returnValue = unit;
	        }
	
	        Double volume = Double.parseDouble(item.get("size")+"")/1000 *
	        Double.parseDouble(item.get("width")+"")/1000 *
	        Double.parseDouble(item.get("height")+"")/1000;
	        item.set("volume", volume).update();
	        updateTransferOrderItemDetail(item, product);
        }else{
        	if (!"".equals(remark) && remark != null) {
	        	item.set("remark", remark).update();
	            returnValue = remark;
	        } else if (!"".equals(amount) && amount != null) {
	        	item.set("amount", amount).update();
	            if (amount != null && !"".equals(amount)) {
	                saveTransferOrderDetail(item, productId);
	            }
	            returnValue = amount;
	        }
        }
        renderText(returnValue);// 必须返回传进来的值，否则js会报错
    }

    // 更新单品信息
    private void updateTransferOrderItemDetail(TransferOrderItem item, Product product) {
		if(item.get("product_id") == null || "".equals(item.get("product_id"))){
	    	List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where item_id = ?", item.get("id"));
			for(TransferOrderItemDetail detail : transferOrderItemDetails){
				detail.set("item_name", item.get("item_name"));
				detail.set("volume", item.get("volume"));
				detail.set("weight", item.get("weight"));
				detail.update();
			}
		}else{
			List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find("select * from transfer_order_item_detail where item_id = ?", item.get("id"));
			for(TransferOrderItemDetail detail : transferOrderItemDetails){
				detail.set("item_name", product.get("item_name"));
				detail.set("volume", product.get("volume"));
				detail.set("weight", product.get("weight"));
				detail.update();
			}
		}
	}

	// 保存货品
    public void saveTransferOrderItem() {
        TransferOrderItem item = null;
        Long productId = getParaToLong("productId");
        String id = getPara("transferOrderItemId");
        if (id != null && !"".equals(id)) {
            item = TransferOrderItem.dao.findById(id);
            if (productId == null || "".equals(productId)) {
                String size = getPara("size");
                String width = getPara("width");
                String volume = getPara("volume");
                String weight = getPara("weight");
                String height = getPara("height");
                if (size != null && !"".equals(size)) {
                    item.set("size", size);
                }
                if (width != null && !"".equals(weight)) {
                    item.set("width", width);
                }
                if (volume != null && !"".equals(volume)) {
                    item.set("volume", volume);
                }
                if (weight != null && !"".equals(weight)) {
                    item.set("weight", weight);
                }
                if (height != null && !"".equals(height)) {
                    item.set("height", height);
                }
                item.set("item_no", getPara("item_no"));
                item.set("item_name", getPara("item_name"));
                item.set("unit", getPara("unit"));
                item.set("item_desc", getPara("remark"));
            } else {
                // updateProduct(productId); 运输单中的货品信息不能去更新 profile中货品信息。
                item.set("product_id", productId);
            }
            String amount = getPara("amount");
            if (amount != null && !"".equals(amount)) {
                item.set("amount", amount);
            }
            // update 不要更新order_id, order_id本来就存在了，upadte 有啥用？
            // item.set("order_id", getPara("transfer_order_id"));
            item.update();
        } else {
            item = new TransferOrderItem();
            if (productId == null || "".equals(productId)) {
                String size = getPara("size");
                String width = getPara("width");
                String volume = getPara("volume");
                String weight = getPara("weight");
                String height = getPara("height");
                if (size != null && !"".equals(size)) {
                    item.set("size", size);
                }
                if (width != null && !"".equals(weight)) {
                    item.set("width", width);
                }
                if (volume != null && !"".equals(volume)) {
                    item.set("volume", volume);
                }
                if (weight != null && !"".equals(weight)) {
                    item.set("weight", weight);
                }
                if (height != null && !"".equals(height)) {
                    item.set("height", height);
                }
                item.set("item_no", getPara("item_no"));
                item.set("item_name", getPara("item_name"));
                item.set("unit", getPara("unit"));
                item.set("item_desc", getPara("remark"));
            } else {
                // updateProduct(productId);
                item.set("product_id", productId);
            }
            String amount = getPara("amount");
            if (amount != null && !"".equals(amount)) {
                item.set("amount", amount);
            }
            item.set("order_id", getPara("transfer_order_id"));
            item.save();
            if (amount != null && !"".equals(amount)) {
                saveTransferOrderDetail(item, productId);
            }
        }
        renderJson(item);
    }

    // 保存货品同时保存单品
    private void saveTransferOrderDetail(TransferOrderItem item, Long productId) {
        TransferOrderItemDetail transferOrderItemDetail = null;
        Integer amount = Integer.parseInt(item.getStr("amount"));
        if (productId == null || "".equals(productId)) {
            for (int i = 0; i < amount; i++) {
                transferOrderItemDetail = new TransferOrderItemDetail();
                transferOrderItemDetail.set("item_name", item.get("item_name"));
                transferOrderItemDetail.set("volume", item.get("volume"));
                transferOrderItemDetail.set("weight", item.get("weight"));
                transferOrderItemDetail.set("item_id", item.get("id"));
                transferOrderItemDetail.set("order_id", item.get("order_id"));
                saveNotifyParty(transferOrderItemDetail);
                transferOrderItemDetail.save();
            }
        } else {
            Product product = Product.dao.findById(productId);
            for (int i = 0; i < amount; i++) {
                transferOrderItemDetail = new TransferOrderItemDetail();
                transferOrderItemDetail.set("item_name", product.get("item_name"));
                transferOrderItemDetail.set("volume", product.get("volume"));
                transferOrderItemDetail.set("weight", product.get("weight"));
                transferOrderItemDetail.set("item_id", item.get("id"));
                transferOrderItemDetail.set("order_id", item.get("order_id"));
                saveNotifyParty(transferOrderItemDetail);
                transferOrderItemDetail.save();
            }
        }
    }

    // 保存收货人
    private void saveNotifyParty(TransferOrderItemDetail transferOrderItemDetail) {
    	String notifyPartyId = transferOrderItemDetail.get("notify_party_id");
        Party party = null;
        if (notifyPartyId != null && !notifyPartyId.equals("")) {
            party = Party.dao.findById(notifyPartyId);
        } else {
            party = new Party();
            party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
            Contact contact = new Contact();
            contact.save();
            party.set("contact_id", contact.getLong("id"));
            party.set("create_date", new Date());
            party.set("creator", currentUser.getPrincipal());
            party.save();
        }
        transferOrderItemDetail.set("notify_party_id", party.get("id"));
	}

    // 获取TransferOrderItem对象
    public void getTransferOrderItem() {
        Map<String, Object> map = new HashMap<String, Object>();
        String id = getPara("transfer_order_item_id");
        TransferOrderItem transferOrderItem = TransferOrderItem.dao.findById(id);
        Product product = Product.dao.findById(transferOrderItem.get("product_id"));
        map.put("transferOrderItem", transferOrderItem);
        map.put("product", product);
        renderJson(map);
    }

    // 删除TransferOrderItem
    public void deleteTransferOrderItem() {
        String id = getPara("transfer_order_item_id");
        List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao
                .find("select * from transfer_order_item_detail where item_id=" + id);
        for (TransferOrderItemDetail itemDetail : transferOrderItemDetails) {
            itemDetail.delete();
        }
        TransferOrderItem.dao.deleteById(id);
        renderJson("{\"success\":true}");
    }

    public void addNewRow() {
        String orderId = getPara("orderId");
        new TransferOrderItem().set("order_id", orderId).set("size", 0).set("width", 0).set("height", 0).set("weight", 0).save();
        renderJson("{\"success\":true}");
    }
}
