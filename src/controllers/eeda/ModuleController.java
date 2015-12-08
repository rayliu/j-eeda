package controllers.eeda;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Category;
import models.ParentOfficeModel;
import models.Party;
import models.Product;
import models.UserLogin;
import models.yh.profile.Contact;
import models.yh.profile.Module;
import models.yh.structure.Field;
import models.yh.structure.Structure;
import net.sf.cglib.core.CollectionUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.bz.gateOutOrder.models.BzGateOutOrder;
import controllers.bz.gateOutOrder.models.BzGateOutOrderItem;
import controllers.yh.LoginUserController;
import controllers.yh.util.DbUtils;
import controllers.yh.util.OrderNoGenerator;
import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ModuleController extends Controller {

    private Logger logger = Logger.getLogger(ModuleController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PT_LIST})
    public void index() {
        render("/yh/profile/module/moduleList.html");
    }
    
    public void searchModule(){
    	String parent_id = getPara("id");
    	String cons = "";
    	String sql = "select id, module_name, parent_id, office_id, seq from modules where office_id="
    			+LoginUserController.getLoginUser(this).get("office_id");
    	
    	List<Record> modules = null;
    	if(StringUtils.isEmpty(parent_id)){
    		modules = Db.find(sql+" and parent_id is null order by seq");
    	}else{
    		modules = Db.find(sql+" and parent_id =? order by seq", parent_id);
    	}
    	renderJson(modules);
    }
    
    public void addModule(){
    	String id = getPara("id");
    	String parent_id = getPara("parent_id");
    	String module_name = getPara("name");
    	
    	Module module = new Module();
    	if(!StringUtils.isEmpty(parent_id)){
    		module.set("parent_id", parent_id);
    	}
    	module.set("module_name", module_name);
    	module.set("office_id", LoginUserController.getLoginUser(this).get("office_id"));
    	
    	String sql = "select max(seq) seq from modules where office_id="
    			+LoginUserController.getLoginUser(this).get("office_id");
    	Module m = Module.dao.findFirst(sql);
    	if(m.getDouble("seq")!=null){
    	    module.set("seq", m.getDouble("seq")+1);
    	}else{
    	    module.set("seq", 1);
    	}
    	module.save();
    	
    	renderJson(module);
    }
    
    public void updateModule(){
    	String id = getPara("id");
    	String module_name = getPara("module_name");
    	String parent_id = getPara("parent_id");
    	
    	Module module = Module.dao.findById(id);
    	if(module !=null){
	    	if(!StringUtils.isEmpty(parent_id)){
	    		module.set("parent_id", parent_id);
	    	}
	    	module.set("module_name", module_name);
	    	module.update();
    	}
    	
    	renderJson(module);
    }
    
    @Before(Tx.class)
    public void updateModuleSeq(){
    	String node_id = getPara("node_id");
    	String target_node_id = getPara("target_node_id");
    	String move_type = getPara("move_type");
    	
    	Module module = Module.dao.findById(node_id);
    	Module target_module = Module.dao.findById(target_node_id);
    	
    	if(module !=null && target_module != null){
    		//移动单据到另一个模块下
    		if(module.getLong("parent_id") != target_module.getLong("parent_id")){
    			module.set("parent_id", target_module.getLong("parent_id")).update();
    		}
    		
    		if("inner".equals(move_type)){
    			module.set("parent_id", target_node_id).update();
    		}else if("prev".equals(move_type)){
	    		double target_seq = target_module.getDouble("seq");
	    		module.set("seq", target_seq-0.5).update();
	    	}else{
	    		double target_seq = target_module.getDouble("seq");
	    		module.set("seq", target_seq+0.5).update();
	    	}
    	}
    	
    	//重新算序号
    	String sql = "select id, module_name, parent_id, office_id, seq from modules where office_id="
    			+LoginUserController.getLoginUser(this).get("office_id")+" order by seq";
    	List<Module> modules = Module.dao.find(sql);
    	int newSeq = 1;
    	for (Module m : modules) {
			m.set("seq", newSeq).update();
			newSeq++;
		}
    	
    	renderJson(module);
    }
    
    @Before(Tx.class)
    public void saveStructure() throws InstantiationException, IllegalAccessException{
    	String jsonStr=getPara("params");
    	
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String module_id = (String) dto.get("module_id");
        UserLogin user = LoginUserController.getLoginUser(this);
        
        //Field
        List<Map<String, String>> structure_list = (ArrayList<Map<String, String>>)dto.get("structure_list");
        
        Map<String, String> master_ref= new HashMap<String, String>();
        master_ref.put("module_id", module_id);
        DbUtils.handleList(structure_list, Structure.class, master_ref);

        renderJson(dto);
    }
    
    public void getOrderStructure(){
        String module_id = getPara("module_id");
        
        List<Record> sRecs = Db.find("select * from structure where module_id=?", module_id);
        for (Record structure : sRecs) {
            String fieldSql = "select * from field where structure_id=?";
            List<Record> fieldList = Db.find(fieldSql, structure.get("id"));

            structure.set("fields_list", fieldList);
        }
        
        Record rec = new Record();
        rec.set("module_id", module_id);
        rec.set("structure_list", sRecs);
        renderJson(rec);
    }
    
    //------------------------------------------------
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PT_LIST})
    public void list() {
    	String searchStr = getPara("sSearch");//查询文本框
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
        String searchConditions = "";
        if(StringUtils.isNotEmpty(searchStr)){
        	searchConditions=" and (item_name like '%"+searchStr+"%'" 
        			+ " or item_no like '%"+searchStr+"%'" 
        			+ " or serial_no like '%"+searchStr+"%'"
        			+ " or item_desc like '%"+searchStr+"%')";
        } 
        if (categoryId == null || "".equals(categoryId)) {
            sqlTotal = "select count(1) total from product";
            sql = "select *,(select name from category  where id = "+categoryId+") category_name from product order by id desc"+sLimit;
        } else {
            sqlTotal = "select count(1) total from product where category_id = " + categoryId;
            sql = "select *,(select name from category where id = "+categoryId+") category_name from product "
            		+ "where category_id = " + categoryId 
            		+ searchConditions
            		+ " order by id desc "+sLimit;
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
    	Long parentID = pom.getParentOfficeId();
    	
        List<Party> parties = Party.dao
                .find("select p.id party_id, c.*, cat.id cat_id from party p left join contact c on c.id = p.contact_id left join category cat on p.id = cat.customer_id left join office o on p.office_id = o.id  where party_type = ? and cat.parent_id is null and (o.id = ? or o.belong_office = ? )",
                        Party.PARTY_TYPE_CUSTOMER,parentID,parentID);
        createRootForParty(parties);

        List<Party> rootParties = Party.dao
                .find("select p.id pid, c.*, cat.id cat_id from party p left join contact c on c.id = p.contact_id left join category cat on p.id = cat.customer_id left join office o on p.office_id = o.id where party_type = ? and cat.parent_id is null and (o.id = ? or o.belong_office = ? ) ",
                        Party.PARTY_TYPE_CUSTOMER,parentID,parentID);
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
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PT_UPDATE})
    public void updateProductById(){
    	 String id = getPara("id");
    	 String filedName = getPara("fieldName");
    	 String value = getPara("value");
    	 Product product = null;

         if (id != null && !id.equals("")) {
             product = Product.dao.findById(id);
    		 product.set(filedName, value).update();
    	 }
         renderJson(product);
    }
    
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
    public void searchAllUnit(){
		List<Record> offices = Db.find("select * from unit");
		renderJson(offices); 
    }
}
