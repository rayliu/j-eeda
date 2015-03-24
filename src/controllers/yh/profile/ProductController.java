package controllers.yh.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Category;
import models.Office;
import models.Party;
import models.Product;
import models.UserOffice;
import models.yh.profile.Contact;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ProductController extends Controller {

    private Logger logger = Logger.getLogger(ProductController.class);
    Subject currentUser = SecurityUtils.getSubject();
    String userName = currentUser.getPrincipal().toString();
    UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
    Office parentOffice = Office.dao.findFirst("select * from office where id = ?",currentoffice.get("office_id"));
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PT_LIST})
    public void index() {
        render("/yh/profile/product/productList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PT_LIST})
    public void list() {
        String sLimit = "";
        Map productListMap = null;
        String categoryId = getPara("categoryId");
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
        	sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String category = getPara("category");
        String sql = "";
        String sqlTotal = "";
        if (categoryId == null || "".equals(categoryId)) {
            sqlTotal = "select count(1) total from product";
            sql = "select *,(select name from category  where id = "+categoryId+") category_name from product "+sLimit;
        } else {
            sqlTotal = "select count(1) total from product where category_id = " + categoryId;
            sql = "select *,(select name from category where id = "+categoryId+") category_name from product where category_id = " + categoryId + " "+sLimit;
        }
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> products = Db.find(sql);
        productListMap = new HashMap();
        productListMap.put("sEcho", pageIndex);
        productListMap.put("iTotalRecords", rec.getLong("total"));
        productListMap.put("iTotalDisplayRecords", rec.getLong("total"));
        productListMap.put("aaData", products);
        renderJson(productListMap);
    }

    public void add() {
        setAttr("saveOK", false);
        render("/yh/profile/product/productEdit.html");
    }

    public void edit() {
        long id = getParaToLong();

        Product product = Product.dao.findById(id);
        setAttr("product", product);
        render("/yh/profile/product/productEdit.html");
    }

    public void delete() {
        Product product = Product.dao.findById(getPara("productId"));
        /*product.set("category_id", null);
        product.delete();*/
        Object obj = product.get("is_stop");
        if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
        	product.set("is_stop", true);
        }else{
        	product.set("is_stop", false);
        }
        product.update();
        renderJson("{\"success\":true}");
    }
    @Before(Tx.class)
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
            product.set("item_name", getPara("item_name")).set("serial_no", getPara("serial_no")).set("item_no", getPara("item_no")).set("item_desc", getPara("item_desc"))
                    .set("unit", getPara("unit")).set("category_id", getPara("categoryId"));
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

            product.set("item_name", itemName).set("serial_no", getPara("serial_no")).set("item_no", itemNo).set("item_desc", itemDesc).set("unit", getPara("unit"))
                    .set("category_id", getPara("categoryId"));
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
        Product product = Product.dao.findFirst("select p.*,c.name cname from product p left join category c on c.id = p.category_id where p.id = ?", getPara("productId"));
        renderJson(product);
    }

    // 保存类别
    @Before(Tx.class)
    public void saveCategory() {
        String categoryId = getPara("categoryId");
        Category category = Category.dao.findById(categoryId);
        /*String categoryName = "";
    	List<Category> categories = Category.dao.find("select * from category where parent_id = ? and id != ?", category.get("parent_id"), categoryId);
    	for(Category c :  categories){
    		categoryName += c.get("name");
    	}
    	if(!categoryName.contains(name)){
    		category.set("name", getPara("name"));
	        category.set("customer_id", getPara("customerId"));
	        category.update();
    	}*/
		category.set("name", getPara("categoryName"));
        category.set("customer_id", getPara("customerId"));
        category.update();
        renderJson(category);
    }

    // 新增类别
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PT_CREATE})
    @Before(Tx.class)
    public void addCategory() {
        String parentId = getPara("categoryId");
        Category category = null;
        if (parentId != null) {
            category = new Category();
            Category c = Category.dao.findFirst("select * from category where name like '%新类别%' and parent_id = ? order by name desc limit 0,1", parentId);
            if(c != null){
            	String categoryName = c.get("name");
            	Pattern pattern = Pattern.compile("\\d+");  
                Matcher matcher = pattern.matcher(categoryName); 
                Integer integer = 1;
                if(matcher.find()){
                	integer = Integer.parseInt(matcher.group(0)) + 1;
                }
                category.set("name", "新类别" + integer);
            }else{            	
            	category.set("name", "新类别1");
            }
            category.set("customer_id", getPara("customerId"));
            category.set("parent_id", parentId);
            category.save();
        }
        renderJson(category);
    }

    // 删除类别
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PT_DELETE})
    @Before(Tx.class)
    public void deleteCategory() {
    	boolean flag = true;
        String cid = getPara("categoryId");
        // removeChildern(cid);
        List<Product> products = Product.dao.find("select * from product where category_id = ?", cid);
        try {
			for (Product product : products) {
			    product.delete();
			}
			Category.dao.deleteById(cid);
		} catch (RuntimeException e) {
			flag = false;
		}
        if(flag){
        	renderJson("{\"success\":true}");
        }else{
			renderJson("{\"success\":false}");
        }
    }

    // 删除子类别
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PT_DELETE})
    @Before(Tx.class)
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
    public void findCategory() {
    	Category category = Category.dao.findById(getPara("categoryId"));
    	renderJson(category);    		
    }    
    
    // 查找客户
    public void searchAllCustomer() {
    	Long parentID = parentOffice.get("belong_office");
    	if(parentID == null || "".equals(parentID)){
    		parentID = parentOffice.getLong("id");
    	}
    	
        List<Party> parties = Party.dao
                .find("select p.id party_id, c.*, cat.id cat_id from party p left join contact c on c.id = p.contact_id left join category cat on p.id = cat.customer_id left join office o on p.office_id = o.id  where party_type = ? and cat.parent_id is null and (o.id = ? or o.belong_office = ? or o.belong_office is null)",
                        Party.PARTY_TYPE_CUSTOMER,parentOffice.get("id"),parentID);
        createRootForParty(parties);

        List<Party> rootParties = Party.dao
                .find("select p.id pid, c.*, cat.id cat_id from party p left join contact c on c.id = p.contact_id left join category cat on p.id = cat.customer_id left join office o on p.office_id = o.id where party_type = ? and cat.parent_id is null and (o.id = ? or o.belong_office = ? or o.belong_office is null) ",
                        Party.PARTY_TYPE_CUSTOMER,parentOffice.get("id"),parentID);
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
    
    // 添加一行新数据
    public void addNewRow() {
        String categoryId = getPara("categoryId");
        Product p = new Product();
        p.set("category_id", categoryId).set("size", 0).set("width", 0).set("height", 0).set("weight", 0).save();
        renderJson("{\"success\":true}");
    }

    // 保存产品
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PT_CREATE, PermissionConstant.PERMSSION_PT_UPDATE}, logical=Logical.OR)
    public void saveProductByField() {
        String returnValue = "";
        String id = getPara("id");
        Product item = Product.dao.findById(id);
        String item_no = getPara("item_no");
        String serial_no = getPara("serial_no");
        String item_name = getPara("item_name");
        String unit = getPara("unit");
        String itemDesc = getPara("item_desc");
        String size = getPara("size");
        String width = getPara("width");
        String height = getPara("height");
        String weight = getPara("weight");
        String insuranceAmount = getPara("insurance_amount");
        if (!"".equals(item_no) && item_no != null) {
        	Category category = Category.dao.findById(item.get("category_id"));
        	Product product = Product.dao.findFirst("select * from product p left join category c on c.id = p.category_id where p.item_no = '"+item_no+"' and c.customer_id = "+category.get("customer_id"));
        	if(product == null){
        		item.set("item_no", item_no).update();
        		returnValue = item_no;
        	}else{
        		// 产品型号已存在
        		returnValue = "repetition";
        	}
        } else if (!"".equals(item_name) && item_name != null) {
            item.set("item_name", item_name).update();
            returnValue = item_name;
        } else if (!"".equals(itemDesc) && itemDesc != null) {
            item.set("item_desc", itemDesc).update();
            returnValue = itemDesc;
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
        } else if (!"".equals(unit) && unit != null) {
            item.set("unit", unit).update();
            returnValue = unit;
        } else if (!"".equals(serial_no) && serial_no != null) {
	    	item.set("serial_no", serial_no).update();
	    	returnValue = serial_no;
	    } else if (!"".equals(insuranceAmount) && insuranceAmount != null) {
	    	item.set("insurance_amount", insuranceAmount).update();
	    	returnValue = insuranceAmount;
	    }
        if (item.get("size") != null && item.get("width") != null && item.get("height") != null) {
	        Double volume = Double.parseDouble(item.get("size")+"")/1000 * Double.parseDouble(item.get("width")+"")/1000 * Double.parseDouble(item.get("height")+"")/1000;
	        volume = Double.parseDouble(String.format("%.2f", volume));
	        item.set("volume", volume).update();
        }
        renderText(returnValue);// 必须返回传进来的值，否则js会报错
    }
    
    // 校验类别是否已存在
    public void checkCategory(){
    	String id = getPara("id");
    	String name = getPara("name");
    	Category category = Category.dao.findById(id);
    	List<Category> categories = Category.dao.find("select * from category where parent_id = ? and id != ?", category.get("parent_id"), id);
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("name", name);
    	map.put("categories", categories);
    	renderJson(map);
    }
}
