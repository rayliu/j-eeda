package controllers.yh.order;

import interceptor.SetAttrLoginUserInterceptor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.Product;
import models.TransferOrder;
import models.TransferOrderItem;
import models.TransferOrderItemDetail;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
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
                + " where toi.order_id =" + trandferOrderId+"";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        String sql = "";
        sql = "select distinct toi.id,toi.product_id prod_id,ifnull(p.item_no,toi.item_no) item_no, ifnull(p.item_name,toi.item_name) item_name,"
                + " ifnull(p.size,toi.size) size, ifnull(p.width, toi.width) width, ifnull(p.height, toi.height) height,"
                + " ifnull(p.weight,toi.weight) weight, toi.volume volume,toi.amount amount,"
                + " ifnull(p.unit,toi.unit) unit,toi.sum_weight sum_weight, toi.remark from transfer_order_item toi "
                + " left join product p on p.id = toi.product_id " + " where toi.order_id =" + trandferOrderId+" order by toi.id" + sLimit;
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
            render("/yh/transferOrder/transferOrderEdit.html");
    }

    public void edit() {
            render("/yh/transferOrder/editTransferOrder.html");
    }

    public void delete() {
        long id = getParaToLong();

        Party party = Party.dao.findById(id);
        party.delete();

        Contact contact = Contact.dao.findFirst("select * from contact where id=?", party.getLong("contact_id"));
        contact.delete();
            redirect("/transferOrder");
    }

    // 保存货品
    public void saveTransferOrderItemByField() {
        String returnValue = "";
        String id = getPara("id");
        TransferOrderItem item = TransferOrderItem.dao.findById(id);
        TransferOrder transferOrder = TransferOrder.dao.findById(item.get("order_id"));
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
        if (productId == null || "".equals(productId)) {
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
                if (amount != null && !"".equals(amount)) {
            		if("cargo".equals(transferOrder.get("cargo_nature"))){
                		if("cargoNatureDetailYes".equals(transferOrder.get("cargo_nature_detail"))){
                			Double doubleAmount = Double.parseDouble(amount);
    	            		Double doubleAmount2 = item.getDouble("amount")==null?0:item.getDouble("amount");
    	            		double result = new Double(doubleAmount2 - doubleAmount).doubleValue();
    	                    if(Math.abs(result)>0){
    	                    	Double oldAmount = item.getDouble("amount")==null?0:item.getDouble("amount");
    	                    	Double subtractAmount = doubleAmount - oldAmount;
    	                    	saveTransferOrderDetail(item, productId, subtractAmount);    
    	                    }               		
                    	}
                	}else{ 
	            		Double doubleAmount = Double.parseDouble(amount);
	            		Double doubleAmount2 = item.getDouble("amount")==null?0:item.getDouble("amount");
	            		double result = new Double(doubleAmount2 - doubleAmount).doubleValue();
	                    if(Math.abs(result)>0){
	                    	Double oldAmount = item.getDouble("amount")==null?0:item.getDouble("amount");
	                    	Double subtractAmount = doubleAmount - oldAmount;
	                    	saveTransferOrderDetail(item, productId, subtractAmount);    
	                    }
                	}
            	}else{
	                if (amount != null && !"".equals(amount)) {
	                	if("cargo".equals(transferOrder.get("cargo_nature"))){
	                		if("cargoNatureDetailYes".equals(transferOrder.get("cargo_nature_detail"))){
	                			saveTransferOrderDetail(item, productId, Double.parseDouble(amount));                    		
	                    	}
	                	}else{                		
	                		saveTransferOrderDetail(item, productId, Double.parseDouble(amount));
	                	}
	                }
            	}
                item.set("amount", amount).update();
                returnValue = amount;
            } else if (!"".equals(unit) && unit != null) {
                item.set("unit", unit).update();
                returnValue = unit;
            }

            if (item.get("size") != null && item.get("width") != null && item.get("height") != null) {
                Double volume = Double.parseDouble(item.get("size") + "") / 1000 * Double.parseDouble(item.get("width") + "") / 1000
                        * Double.parseDouble(item.get("height") + "") / 1000;
                volume = Double.parseDouble(String.format("%.2f", volume));
                item.set("volume", volume).update();
            }
            updateTransferOrderItemDetail(item, product);
        } else {
            if (!"".equals(remark) && remark != null) {
                item.set("remark", remark).update();
                returnValue = remark;
            } else if (!"".equals(amount) && amount != null) {
            	if(item.get("amount") != null && !"".equals(item.get("amount"))){
            		if("cargo".equals(transferOrder.get("cargo_nature"))){
                		if("cargoNatureDetailYes".equals(transferOrder.get("cargo_nature_detail"))){
                			Double doubleAmount = Double.parseDouble(amount);
    	            		double result = new Double(item.getDouble("amount") - doubleAmount).doubleValue();
    	                    if(Math.abs(result)>0){
    	                    	Double oldAmount = item.getDouble("amount");
    	                    	Double subtractAmount = doubleAmount - oldAmount;
    	                    	saveTransferOrderDetail(item, productId, subtractAmount);    
    	                    }                 		
                    	}
                	}else{ 
	            		Double doubleAmount = Double.parseDouble(amount);
	            		double result = new Double(item.getDouble("amount") - doubleAmount).doubleValue();
	                    if(Math.abs(result)>0){
	                    	Double oldAmount = item.getDouble("amount");
	                    	Double subtractAmount = doubleAmount - oldAmount;
	                    	saveTransferOrderDetail(item, productId, subtractAmount);    
	                    }
                	}
            	}else{
	                if (amount != null && !"".equals(amount)) {
	                	if("cargo".equals(transferOrder.get("cargo_nature"))){
	                		if("cargoNatureDetailYes".equals(transferOrder.get("cargo_nature_detail"))){
	                			saveTransferOrderDetail(item, productId, Double.parseDouble(amount));                    		
	                    	}
	                	}else{                		
	                		saveTransferOrderDetail(item, productId, Double.parseDouble(amount));
	                	}
	                }
            	}
                item.set("amount", amount).update();
                returnValue = amount;
            }
        }
        renderJson(item);
        //renderText(returnValue);// 必须返回传进来的值，否则js会报错
    }
    
    // 保存货品
    public void updateTransferOrderItem() {
    	String id = getPara("id");
    	String fieldName = getPara("fieldName");
    	String value = getPara("value");
    	TransferOrderItem item = TransferOrderItem.dao.findById(id);
    	TransferOrder transferOrder = TransferOrder.dao.findById(item.get("order_id"));
    	Long productId = item.getLong("product_id");
        Product product = null;
    	if (productId == null || "".equals(productId)) {
            if ("amount".equals(fieldName) && value != null) {
            	Double itemAmount = Double.parseDouble(value);
        		if("cargo".equals(transferOrder.get("cargo_nature"))){
            		if("cargoNatureDetailYes".equals(transferOrder.get("cargo_nature_detail"))){
            			autoGenerateDetail(value, item, productId);             		
                	}
            	}else{ 
            		autoGenerateDetail(value, item, productId);
            	}

                if (item.get("size") != null && item.get("width") != null && item.get("height") != null) {
                    Double volume = item.getDouble("size") / 1000 * item.getDouble("width") / 1000 * item.getDouble("height") / 1000 * itemAmount;
                    volume = Double.parseDouble(String.format("%.2f", volume));
                    item.set("volume", volume);
                }
                if (item.get("weight") != null) {
                	item.set("sum_weight", item.getDouble("weight") * itemAmount);
                }
            }
            updateTransferOrderItemDetail(item, product);
        } else {
        	product = Product.dao.findById(productId);
            if ("amount".equals(fieldName) && value != null) {
            	if("cargo".equals(transferOrder.get("cargo_nature"))){
    				if("cargoNatureDetailYes".equals(transferOrder.get("cargo_nature_detail"))){
    					autoGenerateDetail(value, item, productId);                		
    				}
    			}else{ 
    				autoGenerateDetail(value, item, productId);
    			}
            	Double pvolume = product.getDouble("volume")==null?0:product.getDouble("volume");
            	Double pweight = product.getDouble("weight")==null?0:product.getDouble("weight");
            	BigDecimal v = new BigDecimal(pvolume*Double.parseDouble(value));
            	BigDecimal w = new BigDecimal(pweight*Double.parseDouble(value));
            	item.set("volume", v.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            	item.set("sum_weight", w.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        	}
        }
    	item.set(fieldName, value).update();
    	renderJson(item);
    }

	private void autoGenerateDetail(String value, TransferOrderItem item, Long productId) {
		Double doubleAmount = Double.parseDouble(value);
		Double dbAmount = item.getDouble("amount")==null?0:item.getDouble("amount");
		double result = new Double(dbAmount - doubleAmount).doubleValue();
		if(Math.abs(result)>0){
			Double subtractAmount = doubleAmount - dbAmount;
			saveTransferOrderDetail(item, productId, subtractAmount);    
		}
	}

    // 更新单品信息
    private void updateTransferOrderItemDetail(TransferOrderItem item, Product product) {
    	Double volume = item.get("volume");
    	if(item.getDouble("amount") != null && !"".equals(item.getDouble("amount"))){
    		if(item.get("volume") != null && !"".equals(item.get("volume"))){
    			volume = item.getDouble("volume") / item.getDouble("amount");
        		volume = Double.parseDouble(String.format("%.2f", volume));
    		}
    	}
        if (item.get("product_id") == null || "".equals(item.get("product_id"))) {
            List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find(
                    "select * from transfer_order_item_detail where item_id = ?", item.get("id"));
            for (TransferOrderItemDetail detail : transferOrderItemDetails) {
                detail.set("item_name", item.get("item_name"));
                detail.set("volume", volume);
                if(item.get("weight") != null || !"".equals(item.get("weight"))){
                	detail.set("weight", item.get("weight"));
                }
                detail.update();
            }
        } else {
            List<TransferOrderItemDetail> transferOrderItemDetails = TransferOrderItemDetail.dao.find(
                    "select * from transfer_order_item_detail where item_id = ?", item.get("id"));
            for (TransferOrderItemDetail detail : transferOrderItemDetails) {
                detail.set("item_name", product.get("item_name"));
                detail.set("volume", volume);
                if(item.get("weight") != null || !"".equals(item.get("weight"))){
                	detail.set("weight", product.get("weight"));
                }
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
                saveTransferOrderDetail(item, productId, Double.parseDouble(amount));
            }
        }
        Product product = Product.dao.findById(productId);
        renderJson(product);
    }

    // 删除item与产品的关系
    public void deleteTransferOrderItemProduct(){
    	TransferOrderItem transferOrderItem = TransferOrderItem.dao.findById(getPara("transferOrderItemId"));
        transferOrderItem.set("product_id", null);
        transferOrderItem.update();
        renderJson("{\"success\":true}");
    }
    
    // 保存货品同时保存单品
    private void saveTransferOrderDetail(TransferOrderItem item, Long productId, Double amount) {
        TransferOrderItemDetail transferOrderItemDetail = null;
        if(amount > 0){
	        if (productId == null || "".equals(productId)) {
	            for (int i = 0; i < amount; i++) {
	                transferOrderItemDetail = new TransferOrderItemDetail();
	                transferOrderItemDetail.set("item_name", item.get("item_name"));
	                transferOrderItemDetail.set("item_no", item.get("item_no"));
	                transferOrderItemDetail.set("volume", item.get("volume"));
	                transferOrderItemDetail.set("weight", item.get("weight"));
	                transferOrderItemDetail.set("pieces", "1");
	                transferOrderItemDetail.set("item_id", item.get("id"));
	                transferOrderItemDetail.set("order_id", item.get("order_id"));
	                transferOrderItemDetail.save();
	            }
	        } else {
	            Product product = Product.dao.findById(productId);
	            for (int i = 0; i < amount; i++) {
	                transferOrderItemDetail = new TransferOrderItemDetail();
	                transferOrderItemDetail.set("item_name", product.get("item_name"));
	                transferOrderItemDetail.set("item_no", product.get("item_no"));
	                transferOrderItemDetail.set("volume", product.get("volume"));
	                transferOrderItemDetail.set("weight", product.get("weight"));
	                transferOrderItemDetail.set("pieces", "1");
	                transferOrderItemDetail.set("item_id", item.get("id"));
	                transferOrderItemDetail.set("order_id", item.get("order_id"));
	                transferOrderItemDetail.save();
	            }
	        } 
        }else{
        	amount = Math.abs(amount);
        	List<TransferOrderItemDetail> details = TransferOrderItemDetail.dao.find("select id from transfer_order_item_detail where order_id = ? and item_id = ? order by id desc", item.get("order_id"), item.get("id"));
        	for (int i = 0; i < amount; i++) {
        		TransferOrderItemDetail.dao.deleteById(details.get(i).get("id"));
			}
        }
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
    
    public void updateProductNumber() {
        Map<String, Object> map = new HashMap<String, Object>();
        String id = getPara("itemId");
        String name = getPara("fieldName");
        String value = getPara("value");
        double weight = 0;
        double volume = 0;
        Record rec = Db.findFirst("select sum(amount),product_id from transfer_order_item where order_id = " + id);
        Product pro = Product.dao.findById(rec.get("product_id"));
        if("total_amount".equals(name)){
        	weight = Double.parseDouble(value) * pro.getDouble("weight");
        	volume = Double.parseDouble(value) * pro.getDouble("volume");
        }else if("total_weight".equals(name)){
        	
        }else{//普货体积
        	
        }
        TransferOrderItem transferOrderItem = TransferOrderItem.dao.findById(id);
        Product product = Product.dao.findById(transferOrderItem.get("product_id"));
        map.put("weight", weight);
        map.put("volume", volume);
        renderJson(map);
    }
    
}
