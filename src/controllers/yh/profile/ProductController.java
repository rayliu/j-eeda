package controllers.yh.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Category;
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
		String sLimit = "";
		Map productListMap = null;
		String categoryId = getPara("categoryId");
		if(categoryId == null || "".equals(categoryId)){
			String pageIndex = getPara("sEcho");
			if (getPara("iDisplayStart") != null
					&& getPara("iDisplayLength") != null) {
				sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
						+ getPara("iDisplayLength");
			}
	
			String category = getPara("category");
			String sqlTotal = "select count(1) total from product";
			Record rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));
	
			String sql = "select * from product";
	
			List<Record> products = Db.find(sql);
	
			productListMap = new HashMap();
			productListMap.put("sEcho", pageIndex);
			productListMap.put("iTotalRecords", rec.getLong("total"));
			productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
	
			productListMap.put("aaData", products);
		}else{
			String pageIndex = getPara("sEcho");
			if (getPara("iDisplayStart") != null
					&& getPara("iDisplayLength") != null) {
				sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
						+ getPara("iDisplayLength");
			}
	
			String category = getPara("category");
			String sqlTotal = "select count(1) total from product where category_id = " + categoryId;
			Record rec = Db.findFirst(sqlTotal);
			logger.debug("total records:" + rec.getLong("total"));
	
			String sql = "select * from product where category_id = " + categoryId;
	
			List<Record> products = Db.find(sql);
	
			productListMap = new HashMap();
			productListMap.put("sEcho", pageIndex);
			productListMap.put("iTotalRecords", rec.getLong("total"));
			productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
	
			productListMap.put("aaData", products);
		}
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
		product.set("category_id", null);
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
			product.set("item_name", getPara("item_name"))
			       .set("item_no", getPara("item_no"))
	               .set("item_desc", getPara("item_desc"))
				   .set("unit", getPara("unit"))				   
				   .set("category_id", getPara("categorySelect"));
			if(size != null && !"".equals(size)){
				product.set("size", size);
			}
			if(width != null && !"".equals(weight)){
				product.set("width", width);
			}
			if(volume != null && !"".equals(volume)){
				product.set("volume", volume);
			}
			if(weight != null && !"".equals(weight)){
				product.set("weight", weight);
			}
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
				   .set("category_id", getPara("categorySelect"));
			if(size != null && !"".equals(size)){
				product.set("size", size);
			}
			if(width != null && !"".equals(weight)){
				product.set("width", width);
			}
			if(volume != null && !"".equals(volume)){
				product.set("volume", volume);
			}
			if(weight != null && !"".equals(weight)){
				product.set("weight", weight);
			}
	        product.save();
		}
		renderJson(product);;
	}
	
	// 查出客户的子类别
	public void searchCustomerCategory(){
		String customerId = getPara("customerId");
		List<Category> categories = Category.dao.find("select * from category where customer_id ="+customerId);
		renderJson(categories);
	}
	
	//查出当前节点的子节点
	public void searchNodeCategory(){
		String categoryId = getPara("categoryId");
		String customerId = getPara("customerId");
		List<Category> categories = Category.dao.find("select * from category where parent_id="+categoryId+" and customer_id ="+customerId);
		renderJson(categories);
	}
	
	// 查找产品对象
	public void getProduct(){
		Product product = Product.dao.findById(getPara("productId"));
		renderJson(product);
	}
	
	// 保存类别
	public void saveCategory(){
		String categoryId = getPara("categoryId");
		String parentId = getPara("parentId");
		Category category = null;
		if(categoryId == null || "".equals(categoryId)){
			category = new Category();
			category.set("name", getPara("name"));
			category.set("customer_id", getPara("customerId"));
			if(parentId != null && !"".equals(parentId)){
				category.set("parent_id", getPara("parentId"));				
			}
			category.save();
		}else{
			category = Category.dao.findById(categoryId);
			category.set("name", getPara("name"));
			category.set("customer_id", getPara("customerId"));
			category.update();
		}
		renderJson(category);
	}
	
	// 删除类别
	public void deleteCategory(){
		String cid = getPara("categoryId");
		removeChildern(cid);
		List<Product> products = Product.dao.find("select * from product where category_id = ?", cid);	
		for(Product product : products){
			product.delete();
		}
		Category.dao.deleteById(cid);
        renderJson("{\"success\":true}");
	}	
	
	private void removeChildern(String cid) {
		List<Category> categories = Category.dao.find("select * from category where parent_id = ?", cid);
		if(categories.size() > 0){
			for(Category c : categories){
				removeChildern(c.get("id").toString());
				List<Product> products = Product.dao.find("select * from product where category_id = ?", c.get("id"));	
				for(Product product : products){
					product.delete();
				}
				c.delete();
			}
		}else{
			List<Product> products = Product.dao.find("select * from product where category_id = ?", cid);	
			for(Product product : products){
				product.delete();
			}
			Category.dao.deleteById(cid);
		}
	}

	// 查找类别
	public void searchCategory(){
		Category category = Category.dao.findById(getPara("categoryId"));
		renderJson(category);
	}
}
