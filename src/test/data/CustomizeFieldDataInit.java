package test.data;

import java.sql.Statement;

public class CustomizeFieldDataInit {

	public static void initCustomizeField(Statement stmt) {
		try {
			// 运输单
			String instr = "insert into customize_field(order_type, office_id, field_code, field_name, field_desc, is_hidden, customize_name, remark)";
			stmt.executeUpdate(instr
					+ " values('TO', 1, 'EX_CARGO','贵重单品','贵重货品的名称', 0, null, null);");
			stmt.executeUpdate(instr
					+ " values('TO', 1, 'EXE_OFFICE','执行办事处','分公司的名称', 0, null, null);");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void initCustomizeTables(Statement stmt) {
		try {
			String sql = "CREATE TABLE IF NOT EXISTS `structure` ("
					+ "`id` bigint auto_increment primary key,"
					+ "`name` VARCHAR(255) NULL DEFAULT NULL,"
					+ "`description` VARCHAR(255) NULL DEFAULT NULL,"
					+ "`default_structure` TINYINT(1) NULL DEFAULT NULL,"
					+ "`review_interval` VARCHAR(255) NULL DEFAULT NULL,"
					+ "`reviewer_role` VARCHAR(255) NULL DEFAULT NULL,"
					+ "`page_detail` VARCHAR(36) NULL DEFAULT NULL,"
					+ "`structuretype` INT(11) NULL DEFAULT NULL,"
					+ "`system` TINYINT(1) NULL DEFAULT NULL,"
					+ "`fixed` TINYINT(1) NOT NULL DEFAULT '0',"
					+ "`velocity_var_name` VARCHAR(255) ,"
					+ "`url_map_pattern` TEXT NULL DEFAULT NULL,"
					+ "`host` VARCHAR(100) NOT NULL DEFAULT 'system_host',"
					+ "`folder` VARCHAR(100) NOT NULL DEFAULT 'system_folder',"
					+ "`expire_date_var` VARCHAR(255) NULL DEFAULT NULL,"
					+ "`publish_date_var` VARCHAR(255) NULL DEFAULT NULL,"
					+ "`mod_date` DATETIME NULL DEFAULT NULL)";
			stmt.executeUpdate(sql);
			stmt.executeUpdate("insert into structure(name, description) values('boz_order', '博兆出货单');");

			//这个表记录的是默认的表：字段名， 显示名
			//如果需要对某个企业的单据进行字段的重命名，可以使用office_config中的customize_fields（JSON）
			sql = "CREATE TABLE IF NOT EXISTS `field` ("
					+ "  `id` bigint auto_increment primary key,"
					+ "  `structure_id` bigint,"
					+ "  `field_name` VARCHAR(255) NULL DEFAULT NULL,"//用于构造SQL字段
					+ "  `field_display_name` VARCHAR(255) NULL DEFAULT NULL,"//用于构造页面显示的字段（默认）名称
					+ "  `field_type` VARCHAR(255) NULL DEFAULT NULL,"
					+ "  `field_relation_type` VARCHAR(255) NULL DEFAULT NULL,"
					+ "  `field_contentlet` VARCHAR(255) NULL DEFAULT NULL,"  //对应在contentlet表的字段
					+ "  `required` TINYINT(1) NULL DEFAULT NULL,"
					+ "  `indexed` TINYINT(1) NULL DEFAULT NULL,"
					+ "  `listed` TINYINT(1) NULL DEFAULT NULL,"
					+ "  `velocity_var_name` VARCHAR(255) NULL DEFAULT NULL,"
					+ "  `sort_order` INT(11) NULL DEFAULT NULL,"
					+ "  `field_values` LONGTEXT NULL DEFAULT NULL,"
					+ "  `regex_check` VARCHAR(255) NULL DEFAULT NULL,"
					+ "  `hint` VARCHAR(255) NULL DEFAULT NULL,"
					+ "  `default_value` VARCHAR(255) NULL DEFAULT NULL,"
					+ "  `fixed` TINYINT(1) NOT NULL DEFAULT '0',"
					+ "  `read_only` TINYINT(1) NOT NULL DEFAULT '1',"
					+ "  `searchable` TINYINT(1) NULL DEFAULT NULL,"
					+ "  `unique_` TINYINT(1) NULL DEFAULT NULL,"
					+ "  `mod_date` DATETIME NULL DEFAULT NULL)";
			stmt.executeUpdate(sql);
			//博兆出货单 对应的字段
			stmt.executeUpdate("insert into field(structure_id, field_name, field_display_name, field_type, field_contentlet) values(1, 'order_no', '货品序列号', 'text', 'text1');");
			stmt.executeUpdate("insert into field(structure_id, field_name, field_display_name, field_type, field_contentlet) values(1, 'create_time', '创建时间', 'date_time', 'date1');");
			stmt.executeUpdate("insert into field(structure_id, field_name, field_display_name, field_type, field_contentlet) values(1, 'customer_id', '', 'text', 'integer1');");
			stmt.executeUpdate("insert into field(structure_id, field_name, field_display_name, field_type, field_contentlet) values(1, 'remark', '备注', 'text', 'text2');");
			
			sql = "CREATE TABLE IF NOT EXISTS `contentlet` ("
			+"  `id` bigint auto_increment primary key,"
			+"  `structure_id` bigint,"
			+"  `show_on_menu` TINYINT(1) NULL DEFAULT NULL,"
			+"  `title` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `mod_date` DATETIME NULL DEFAULT NULL,"
			+"  `mod_user` VARCHAR(100) NULL DEFAULT NULL,"
			+"  `sort_order` INT(11) NULL DEFAULT NULL,"
			+"  `friendly_name` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `last_review` DATETIME NULL DEFAULT NULL,"
			+"  `next_review` DATETIME NULL DEFAULT NULL,"
			+"  `review_interval` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `disabled_wysiwyg` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `identifier` VARCHAR(36) NULL DEFAULT NULL,"
			+"  `language_id` BIGINT(20) NULL DEFAULT NULL,"
			+"  `date1` DATETIME NULL DEFAULT NULL,"
			+"  `date2` DATETIME NULL DEFAULT NULL,"
			+"  `date3` DATETIME NULL DEFAULT NULL,"
			+"  `date4` DATETIME NULL DEFAULT NULL,"
			+"  `date5` DATETIME NULL DEFAULT NULL,"
			+"  `date6` DATETIME NULL DEFAULT NULL,"
			+"  `date7` DATETIME NULL DEFAULT NULL,"
			+"  `date8` DATETIME NULL DEFAULT NULL,"
			+"  `date9` DATETIME NULL DEFAULT NULL,"
			+"  `date10` DATETIME NULL DEFAULT NULL,"
			+"  `date11` DATETIME NULL DEFAULT NULL,"
			+"  `date12` DATETIME NULL DEFAULT NULL,"
			+"  `date13` DATETIME NULL DEFAULT NULL,"
			+"  `date14` DATETIME NULL DEFAULT NULL,"
			+"  `date15` DATETIME NULL DEFAULT NULL,"
			+"  `date16` DATETIME NULL DEFAULT NULL,"
			+"  `date17` DATETIME NULL DEFAULT NULL,"
			+"  `date18` DATETIME NULL DEFAULT NULL,"
			+"  `date19` DATETIME NULL DEFAULT NULL,"
			+"  `date20` DATETIME NULL DEFAULT NULL,"
			+"  `date21` DATETIME NULL DEFAULT NULL,"
			+"  `date22` DATETIME NULL DEFAULT NULL,"
			+"  `date23` DATETIME NULL DEFAULT NULL,"
			+"  `date24` DATETIME NULL DEFAULT NULL,"
			+"  `date25` DATETIME NULL DEFAULT NULL,"
			+"  `text1` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text2` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text3` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text4` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text5` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text6` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text7` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text8` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text9` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text10` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text11` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text12` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text13` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text14` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text15` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text16` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text17` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text18` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text19` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text20` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text21` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text22` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text23` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text24` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text25` VARCHAR(255) NULL DEFAULT NULL,"
			+"  `text_area1` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area2` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area3` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area4` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area5` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area6` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area7` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area8` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area9` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area10` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area11` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area12` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area13` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area14` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area15` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area16` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area17` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area18` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area19` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area20` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area21` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area22` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area23` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area24` LONGTEXT NULL DEFAULT NULL,"
			+"  `text_area25` LONGTEXT NULL DEFAULT NULL,"
			+"  `integer1` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer2` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer3` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer4` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer5` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer6` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer7` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer8` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer9` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer10` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer11` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer12` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer13` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer14` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer15` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer16` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer17` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer18` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer19` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer20` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer21` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer22` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer23` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer24` BIGINT(20) NULL DEFAULT NULL,"
			+"  `integer25` BIGINT(20) NULL DEFAULT NULL,"
			+"  `float1` FLOAT NULL DEFAULT NULL,"
			+"  `float2` FLOAT NULL DEFAULT NULL,"
			+"  `float3` FLOAT NULL DEFAULT NULL,"
			+"  `float4` FLOAT NULL DEFAULT NULL,"
			+"  `float5` FLOAT NULL DEFAULT NULL,"
			+"  `float6` FLOAT NULL DEFAULT NULL,"
			+"  `float7` FLOAT NULL DEFAULT NULL,"
			+"  `float8` FLOAT NULL DEFAULT NULL,"
			+"  `float9` FLOAT NULL DEFAULT NULL,"
			+"  `float10` FLOAT NULL DEFAULT NULL,"
			+"  `float11` FLOAT NULL DEFAULT NULL,"
			+"  `float12` FLOAT NULL DEFAULT NULL,"
			+"  `float13` FLOAT NULL DEFAULT NULL,"
			+"  `float14` FLOAT NULL DEFAULT NULL,"
			+"  `float15` FLOAT NULL DEFAULT NULL,"
			+"  `float16` FLOAT NULL DEFAULT NULL,"
			+"  `float17` FLOAT NULL DEFAULT NULL,"
			+"  `float18` FLOAT NULL DEFAULT NULL,"
			+"  `float19` FLOAT NULL DEFAULT NULL,"
			+"  `float20` FLOAT NULL DEFAULT NULL,"
			+"  `float21` FLOAT NULL DEFAULT NULL,"
			+"  `float22` FLOAT NULL DEFAULT NULL,"
			+"  `float23` FLOAT NULL DEFAULT NULL,"
			+"  `float24` FLOAT NULL DEFAULT NULL,"
			+"  `float25` FLOAT NULL DEFAULT NULL,"
			+"  `bool1` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool2` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool3` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool4` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool5` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool6` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool7` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool8` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool9` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool10` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool11` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool12` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool13` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool14` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool15` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool16` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool17` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool18` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool19` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool20` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool21` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool22` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool23` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool24` TINYINT(1) NULL DEFAULT NULL,"
			+"  `bool25` TINYINT(1) NULL DEFAULT NULL)";
			stmt.executeUpdate(sql);
			
			stmt.executeUpdate("insert into contentlet(structure_id, text1, date1, integer1) values(1, '1002', sysdate, 1);");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
