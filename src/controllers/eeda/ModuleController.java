package controllers.eeda;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import models.yh.profile.Action;
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
import controllers.yh.util.PingYinUtil;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ModuleController extends Controller {

    private Logger logger = Logger.getLogger(ModuleController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PT_LIST})
    public void index() {
      //查询当前用户菜单
        String sql ="select distinct module.* from modules o, modules module "
                +"where o.parent_id = module.id and o.office_id=? and o.status = '启用' order by seq";
        List<Record> modules = Db.find(sql, pom.getParentOfficeId());
        for (Record module : modules) {
            sql ="select * from modules where parent_id =? and status = '启用' order by seq";
            List<Record> orders = Db.find(sql, module.get("id"));
            module.set("orders", orders);
        }
        setAttr("modules", modules);
        render("/yh/profile/module/moduleList.html");
    }
    
    public void getActiveModules(){
        String sql = "select id, module_name, parent_id, office_id, seq from modules where status = '启用' and office_id="
                +LoginUserController.getLoginUser(this).get("office_id");
        
        List<Record> modules = Db.find(sql);
        if(modules == null){
            modules = Collections.EMPTY_LIST;
        }else{
            for (Record module : modules) {
                String fieldSql = "select f.* from structure s, field f where  f.structure_id = s.id and s.parent_id is null and s.module_id=?";
                List<Record> fields = Db.find(fieldSql, module.get("id"));
                if(fields != null){
                    module.set("field_list", fields);
                    module.set("structure_id", fields.get(0).get("structure_id"));
                }
            }
        }
        renderJson(modules);
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
        
        //Action
        List<Map<String, String>> action_list = (ArrayList<Map<String, String>>)dto.get("action_list");
        DbUtils.handleList(action_list, Action.class, master_ref);
        
        Object is_start = dto.get("is_start");//是否启用？
        if((Boolean)is_start){
            activateModule(module_id);
        }else{
            Db.update(" update modules set status = '停用' where id=?", module_id);
        }

        Record orderDto = getOrderStructureDto(module_id);
        renderJson(orderDto);
    }

    private void activateModule(String module_id) {
        logger.debug("start to generate tables....");
        Db.update(" update modules set status = '启用' where id=?", module_id);
        //module 运输单 id=13，那么table_name 生成： T_13
        //find table record
        String structureSql = "select * from structure where module_id=?";
        List<Record> sList = Db.find(structureSql, module_id);
        
        for (Record structure : sList) {
            String structureId = structure.get("id").toString();
            
            String tableName = "T_"+ structureId;
            
            //每个子表中默认有ID, PARENT_ID两个字段，请勿添加同名字段。
            String createTableSql = "CREATE TABLE if not exists `"+tableName+"` ("
              +" `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '',"
              +" `parent_id` BIGINT(20) NULL COMMENT '',"
              + "PRIMARY KEY (`id`)  COMMENT '')";
            Db.update(createTableSql);
            
            String fieldSql = "select * from field where structure_id=?";
            List<Record> fieldList = Db.find(fieldSql, structureId);
            for (Record field : fieldList) {
                String fieldName = "F"+field.get("id").toString()+"_"+field.getStr("field_name");
                String createField ="";
                //根据ID判断字段是否已存在
                Record oldFieldRec = Db.findFirst("show columns from "+tableName+" like '"+"F"+field.get("id").toString()+"_%'");
                if("日期编辑框".equals(field.getStr("field_type"))){
                    if(oldFieldRec != null){
                        createField = "ALTER TABLE `"+tableName+"` " + "CHANGE COLUMN `"+oldFieldRec.getStr("field")+"` `"+fieldName+"` TIMESTAMP NULL DEFAULT NULL COMMENT ''";
                    }else{
                        createField = "ALTER TABLE `"+tableName+"` ADD COLUMN `" + fieldName+"` TIMESTAMP NULL COMMENT ''";
                    }
                }else{
                    if(oldFieldRec != null){
                        createField = "ALTER TABLE `"+tableName+"` " + "CHANGE COLUMN `" + oldFieldRec.getStr("field")+"` `" + fieldName + "` VARCHAR(255) NULL DEFAULT NULL COMMENT ''";
                    }else{
                        createField = "ALTER TABLE `" + tableName+"` ADD COLUMN `" + fieldName + "` VARCHAR(255) NULL COMMENT ''";
                    }
                }
                Db.update(createField);
            }
        }
    }
    
    public void getOrderStructure(){
        String module_id = getPara("module_id");
        Record rec = getOrderStructureDto(module_id);
        renderJson(rec);
    }

    public Record getOrderStructureDto(String module_id) {
        Record module = Db.findFirst("select * from modules where id=?", module_id);
        
        List<Record> sRecs = Db.find("select * from structure where module_id=?", module_id);
        for (Record structure : sRecs) {
            String fieldSql = "select * from field where structure_id=?";
            List<Record> fieldList = Db.find(fieldSql, structure.get("id"));

            structure.set("fields_list", fieldList);
        }
        
        List<Record> aRecs = Db.find("select * from structure_action where module_id=?", module_id);
        
        Record rec = new Record();
        rec.set("module_id", module_id);
        rec.set("module_name", module.get("module_name"));
        rec.set("structure_list", sRecs);
        rec.set("action_list", aRecs);
        return rec;
    }
    
    //针对字段设置生成预览页面
    public void preview(){
        String module_id =getPara();
        setAttr("module_id", module_id);
        
        UserLogin user = LoginUserController.getLoginUser(this);
        //查询当前用户菜单
        String sql ="select distinct module.* from modules o, modules module "
                +"where o.parent_id = module.id and o.office_id=? and o.status = '启用' order by seq";
        List<Record> modules = Db.find(sql, user.get("office_id"));
        for (Record module : modules) {
            sql ="select * from modules where parent_id =? and status = '启用' order by seq";
            List<Record> orders = Db.find(sql, module.get("id"));
            module.set("orders", orders);
        }
        setAttr("modules", modules);
        
        render("/yh/profile/module/editOrder.html");
    }
    
    
}
