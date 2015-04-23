package controllers.yh.util;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.yh.profile.CustomizeField;
import org.apache.shiro.util.StringUtils;
import com.jfinal.core.Controller;

import controllers.yh.LoginUserController;

public class getCustomFile {
	public Map<String, String> getCustomizeFile(Controller controller) {
		List<CustomizeField> customizeFieldList = CustomizeField.dao
				.find("select * from customize_field where office_id ="+LoginUserController.getLoginUser(controller).getLong("office_id"));
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
