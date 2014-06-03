package controllers.yh.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Product;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class ProductController extends Controller{

    private Logger logger = Logger.getLogger(ProductController.class);
    
	public void index() {
		if(LoginUserController.isAuthenticated(this))
		render("profile/product/productList.html");
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

		String category = getPara("category");
		String sqlTotal = "select count(1) total from product where category = '"+category+"'";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select * from product where category = '"+category+"'";

		List<Record> products = Db.find(sql);

		Map productListMap = new HashMap();
		productListMap.put("sEcho", pageIndex);
		productListMap.put("iTotalRecords", rec.getLong("total"));
		productListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		productListMap.put("aaData", products);

		renderJson(productListMap);
	}

	public void add() {
		setAttr("saveOK", false);
		if(LoginUserController.isAuthenticated(this))
		render("profile/product/productEdit.html");
	}

	public void edit() {
		long id = getParaToLong();

		Product product = Product.dao.findById(id);
		setAttr("product", product);
		if(LoginUserController.isAuthenticated(this))
		render("profile/product/productEdit.html");
	}

	public void delete() {
		Product product = Product.dao.findById(getPara("productId"));
		product.delete();
        renderJson("{\"success\":true}");
	}

	public void save() {
		Product product = null;
		String id = getPara("id");
		
		if (id != null && !id.equals("")) {
			product = Product.dao.findById(id);
			String size = getPara("size");
			String width = getPara("width");
			String volume = getPara("volume");
			String weight = getPara("weight");

			if(!size.isEmpty()){
				product.set("size", size);
			}
			if(!width.isEmpty()){
				product.set("width", width);
			}
			if(!volume.isEmpty()){
				product.set("volume", volume);
			}
			if(!weight.isEmpty()){
				product.set("weight", weight);
			}
			product.set("item_name", getPara("item_name"))
			       .set("item_no", getPara("item_no"))
	               .set("item_desc", getPara("item_desc"))
				   .set("unit", getPara("unit"))
				   .set("category", getPara("category"));
			product.set("customer_id", getPara("customerId"));
	        product.update();
		} else {
			product = new Product();
			String itemName = getPara("item_name");
			String itemNo = getPara("item_no");
			String itemDesc = getPara("item_desc");
			String size = getPara("size");
			String width = getPara("width");
			String unit = getPara("unit");
			String volume = getPara("volume");
			String weight = getPara("weight");

			product.set("item_name", itemName)
				   .set("item_no", itemNo)
			       .set("item_desc", itemDesc)
			       .set("unit", getPara("unit"))
				   .set("category", getPara("category"));
			if(!size.isEmpty()){
				product.set("size", size);
			}
			if(!width.isEmpty()){
				product.set("width", width);
			}
			if(!volume.isEmpty()){
				product.set("volume", volume);
			}
			if(!weight.isEmpty()){
				product.set("weight", weight);
			}
			product.set("customer_id", getPara("customerId"));
	        product.save();
		}
		renderJson(product);;
	}
	
	// 查出所有的类别
	public void searchAllCategory(){
		String customerId = getPara("customerId");
		List<Product> products = Product.dao.find("select category from product where customer_id ="+customerId+" group by category");
		renderJson(products);
	}
	
	// 查找产品对象
	public void getProduct(){
		Product product = Product.dao.findById(getPara("productId"));
		renderJson(product);
	}
}
