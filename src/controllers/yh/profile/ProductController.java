package controllers.yh.profile;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.Product;
import models.Role;
import models.yh.profile.Contact;

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

		String sqlTotal = "select count(1) total from product";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select * from product";

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
		long id = getParaToLong();

		Product product = Product.dao.findById(id);
		product.delete();
		if(LoginUserController.isAuthenticated(this))
		redirect("/yh/product");
	}

	public void save() {
		Product product = null;
		String id = getPara("id");
		
		if (id != null && !id.equals("")) {
			product = Product.dao.findById(id);
			product.set("item_name", getPara("itemName")).set("item_no", getPara("itemNo"))
	        .set("item_desc", getPara("itemDesc")).update();
		} else {
			product = new Product();
			String itemName = getPara("itemName");
			String itemNo = getPara("itemNo");
			String itemDesc = getPara("itemDesc");

			product.set("item_name", itemName).set("item_no", itemNo)
			        .set("item_desc", itemDesc).save();
		}
		if(LoginUserController.isAuthenticated(this))
		render("profile/product/productList.html");
	}
}
