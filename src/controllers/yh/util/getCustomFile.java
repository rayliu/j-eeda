package controllers.yh.util;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.yh.profile.CustomizeField;

import org.apache.shiro.util.StringUtils;

import com.jfinal.core.Controller;

import controllers.yh.LoginUserController;

public class getCustomFile {
	private getCustomFile(){
		
	}
	
	public static getCustomFile getInstance(){
		return new getCustomFile();
	}
	public Map<String, String> getCustomizeFile(Controller controller) {
		Long id = LoginUserController.getLoginUser(controller).getLong("office_id");
		Office office = Office.dao.findById(id);
		Long parentID = office.get("belong_office");
		if(parentID == null || "".equals(parentID)){
			parentID = office.getLong("id");
		}
		
		List<CustomizeField> customizeFieldList = CustomizeField.dao
				.find("select * from customize_field where office_id =" + parentID);
		Map<String, String> customizeField = new HashMap<String, String>();
		for (int i = 0; i < customizeFieldList.size(); i++) {
			CustomizeField field = customizeFieldList.get(i);
			String fieldCode = field.getStr("field_code");
			String fieldName = field.getStr("field_name");
			String customizeName = field.getStr("customize_name");
			if(StringUtils.hasText(customizeName))
				fieldName = customizeName;
			customizeField.put(fieldCode, fieldName);
		}
		return customizeField;
	}
}
