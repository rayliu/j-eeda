package controllers.yh;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.FinItem;
import models.ParentOfficeModel;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.util.ParentOffice;
import controllers.yh.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ReimbursementAccountController extends Controller {
	private Logger logger = Logger.getLogger(ReimbursementAccountController.class);
	Subject currentUser = SecurityUtils.getSubject();
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);

	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_T_LIST})
	public void index() {
		render("/yh/profile/reimbursementAccount/ReimbursementAccountList.html");
	}
	
	
	public void searchAccountType(){
		String name = getPara("input");
		List<FinItem> finItem = FinItem.dao.find("select * from fin_item fi where type = '报销费用' and parent_id = 0 and name like '%"+name+"%'");
		renderJson(finItem);
	}
	
	
	
	public void save(){
		String id = getPara("id");
		String typeId = getPara("typeId");
		String name = getPara("name");
		String type = "报销费用";
		String code = getPara("code");
		String remark = getPara("remark");
		Long parentID = pom.getParentOfficeId();

		FinItem f = new FinItem();
		//唯一校验
		Record r = Db.findFirst("select * from fin_item where type='报销费用' and parent_id = '"+typeId+"' and name ='"+name+"'");
			if (id == "") {
				if(r == null){
					f.set("name", name).set("code", code).set("type", type).set("parent_id", typeId).set("office_id",parentID)
					.set("remark", remark).save();
				}
			} else {
				if(r == null){
					f = FinItem.dao.findById(id);
					f.set("name", name).set("code", code).set("type", type).set("parent_id", typeId).set("office_id",parentID)
							.set("remark", remark).update();
				}
			}
		renderJson(f);
	}
	
	
	//@RequiresPermissions(value = {PermissionConstant.PERMSSION_T_LIST})
	public void list() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Long parentID = pom.getParentOfficeId();
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from fin_item  where type ='报销费用' and office_id = " + parentID;
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select *,(select name from fin_item fi1 where fi1.id = fi.parent_id ) account_type from fin_item fi where type ='报销费用' and parent_id !=0 and office_id = ?",parentID);
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);
		renderJson(orderMap);
	}
	
	

	// 编辑条目按钮
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_T_CREATE,PermissionConstant.PERMSSION_T_UPDATE},logical=Logical.OR)
	public void Edit() {
		String id = getPara();
		if (id != null) {
			FinItem f = FinItem.dao.findById(id);
			setAttr("finItem", f);
			f = FinItem.dao.findById(f.getLong("parent_id"));
			setAttr("account_type", f.get("name"));
			render("/yh/profile/reimbursementAccount/ReimbursementAccountEdit.html");
		} else {
			render("/yh/profile/reimbursementAccount/ReimbursementAccountEdit.html");
		}
	}

	// 删除条目
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_T_DELETE})
	public void delete() {
		String id = getPara();
		if (id != null) {
			FinItem l = FinItem.dao.findById(id);
			Object obj = l.get("is_stop");
            if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
            	l.set("is_stop", true);
            }else{
            	l.set("is_stop", false);
            }
            l.update();
		}
		redirect("/reimbursementAccount");
	}

}
