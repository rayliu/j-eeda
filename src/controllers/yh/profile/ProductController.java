package controllers.yh.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Category;
import models.Party;
import models.Product;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class ProductController extends Controller {

    private Logger logger = Logger.getLogger(ProductController.class);

    public void index() {
        if (LoginUserController.isAuthenticated(this))
            render("profile/product/productList.html");
    }

    public void list() {
        String sLimit = "";
        Map productListMap = null;
        String categoryId = getPara("categoryId");
        if (categoryId == null || "".equals(categoryId)) {
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
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
        } else {
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
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
        if (LoginUserController.isAuthenticated(this))
            render("profile/product/productEdit.html");
    }

    public void edit() {
        long id = getParaToLong();

        Product product = Product.dao.findById(id);
        setAttr("product", product);
        if (LoginUserController.isAuthenticated(this))
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
            String height = getPara("height");
            product.set("item_name", getPara("item_name")).set("item_no", getPara("item_no")).set("item_desc", getPara("item_desc"))
                    .set("unit", getPara("unit")).set("category_id", getPara("categorySelect"));
            if (size != null && !"".equals(size)) {
                product.set("size", size);
            }
            if (width != null && !"".equals(weight)) {
                product.set("width", width);
            }
            if (volume != null && !"".equals(volume)) {
                product.set("volume", volume);
            }
            if (weight != null && !"".equals(weight)) {
                product.set("weight", weight);
            }
            if (height != null && !"".equals(height)) {
                product.set("height", height);
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
            String height = getPara("height");

            product.set("item_name", itemName).set("item_no", itemNo).set("item_desc", itemDesc).set("unit", getPara("unit"))
                    .set("category_id", getPara("categorySelect"));
            if (size != null && !"".equals(size)) {
                product.set("size", size);
            }
            if (width != null && !"".equals(weight)) {
                product.set("width", width);
            }
            if (volume != null && !"".equals(volume)) {
                product.set("volume", volume);
            }
            if (weight != null && !"".equals(weight)) {
                product.set("weight", weight);
            }
            if (height != null && !"".equals(height)) {
                product.set("height", height);
            }
            product.save();
        }
        renderJson(product);
    }

    // 查出客户的根类别
    public void searchCustomerCategory() {
        String customerId = getPara("customerId");
        Category category = Category.dao.findFirst("select * from category where customer_id =? and parent_id is null", customerId);
        if (category == null) {
            category = new Category();
            category.set("name", "root");
            category.set("customer_id", customerId);
            category.save();
        }

        renderJson(category);
    }

    // 查出客户的根类别
    public void searchCustomerCategory2() {
        // String customerId = getPara("customerId");
        List<Category> categories = new ArrayList<Category>();
        List<Product> list = Product.dao.find("select * from product");
        for (Product product : list) {
            Category category = Category.dao.findFirst("select * from category where customer_id =? and parent_id is null",
                    product.get("customer_id"));
            if (category == null) {
                category = new Category();
                category.set("name", "root");
                category.set("customer_id", product.get("customer_id"));
                category.save();
            }
            categories.add(category);
        }
        renderJson(categories);
    }

    // 查出当前节点的子节点, 如果当前节点没有categoryId, 它就是公司的根节点（新建一个）
    public void searchNodeCategory() {
        Long categoryId = getParaToLong("categoryId");
        Long customerId = getParaToLong("customerId");
        logger.debug("categoryId=" + categoryId + ", customerId=" + customerId);
        List<Category> categories = Category.dao
                .find("select * from category where parent_id=? and customer_id =?", categoryId, customerId);
        renderJson(categories);

    }

    // 查找产品对象
    public void getProduct() {
        Product product = Product.dao.findById(getPara("productId"));
        renderJson(product);
    }

    // 保存类别
    public void saveCategory() {
        String categoryId = getPara("categoryId");
        // String parentId = getPara("parentId");
        Category category = Category.dao.findById(categoryId);
        category.set("name", getPara("name"));
        category.set("customer_id", getPara("customerId"));
        category.update();

        renderJson(category);
    }

    // 新增类别
    public void addCategory() {
        String parentId = getPara("categoryId");
        Category category = null;
        if (parentId != null) {
            category = new Category();
            category.set("name", getPara("name"));
            category.set("customer_id", getPara("customerId"));
            category.set("parent_id", parentId);
            category.save();
        }
        renderJson(category);
    }

    // 删除类别
    public void deleteCategory() {
        String cid = getPara("categoryId");
        removeChildern(cid);
        List<Product> products = Product.dao.find("select * from product where category_id = ?", cid);
        for (Product product : products) {
            product.delete();
        }
        Category.dao.deleteById(cid);
        renderJson("{\"success\":true}");
    }

    private void removeChildern(String cid) {
        List<Category> categories = Category.dao.find("select * from category where parent_id = ?", cid);
        if (categories.size() > 0) {
            for (Category c : categories) {
                removeChildern(c.get("id").toString());
                List<Product> products = Product.dao.find("select * from product where category_id = ?", c.get("id"));
                for (Product product : products) {
                    product.delete();
                }
                c.delete();
            }
        } else {
            List<Product> products = Product.dao.find("select * from product where category_id = ?", cid);
            for (Product product : products) {
                product.delete();
            }
            Category.dao.deleteById(cid);
        }
    }

    // 查找类别
    public void searchCategory() {
        Category category = Category.dao.findById(getPara("categoryId"));
        renderJson(category);
    }

    // 查找类别
    public void findAllCategory() {
        List<Category> categories = Category.dao.find("select * from category where customer_id = ?", getPara("customerId"));
        renderJson(categories);
    }

    // 查找客户
    public void searchAllCustomer() {
        List<Party> parties = Party.dao
                .find("select p.id party_id, c.*, cat.id cat_id from party p left join contact c on c.id = p.contact_id left join category cat on p.id = cat.customer_id where party_type = ? and cat.parent_id is null",
                        Party.PARTY_TYPE_CUSTOMER);
        createRootForParty(parties);

        List<Party> rootParties = Party.dao
                .find("select p.id pid, c.*, cat.id cat_id from party p left join contact c on c.id = p.contact_id left join category cat on p.id = cat.customer_id where party_type = ? and cat.parent_id is null",
                        Party.PARTY_TYPE_CUSTOMER);
        renderJson(rootParties);
    }

    private void createRootForParty(List<Party> parties) {
        for (Party party : parties) {
            Long customerId = party.getLong("party_id");
            List<Category> categories = Category.dao.find("select * from category where customer_id = ?", customerId);
            if (categories.size() == 0) {
                Category category = new Category();
                Contact customerParty = Contact.dao
                        .findFirst("select c.* from party p left join contact c on c.id = p.contact_id where p.id=" + customerId);
                category.set("name", customerParty.get("company_name"));
                category.set("customer_id", customerId);
                category.save();
            }
        }
    }
}
