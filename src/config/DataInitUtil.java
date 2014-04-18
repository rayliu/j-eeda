package config;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;

import models.Party;
import models.PartyAttribute;
import models.yh.profile.Contact;

import com.jfinal.plugin.c3p0.C3p0Plugin;

public class DataInitUtil {
    public static void initH2Tables(C3p0Plugin cp) {
        try {
            cp.start();
            Connection conn = cp.getDataSource().getConnection();
            Statement stmt = conn.createStatement();
            
            // 登陆及授权的4个表
            stmt.executeUpdate("create table if not exists user_login(id bigint auto_increment PRIMARY KEY, user_name VARCHAR(50) not null, password VARCHAR(50) not null, password_hint VARCHAR(255));");
            stmt.executeUpdate("create table if not exists permissions(id bigint auto_increment PRIMARY KEY,permissions VARCHAR(50));");
            stmt.executeUpdate("create table if not exists role(id bigint auto_increment PRIMARY KEY,role_name VARCHAR(50), remark VARCHAR(50));");
            stmt.executeUpdate("create table if not exists user_roles(id bigint auto_increment PRIMARY KEY, user_id bigint not null, role_id bigint not null, remark VARCHAR(255));");
            stmt.executeUpdate("create table if not exists role_permissions(id bigint auto_increment PRIMARY KEY, role_id bigint not null, role_permission VARCHAR(5120), remark VARCHAR(255));");
            
            
            stmt.executeUpdate("create table if not exists location(id bigint auto_increment PRIMARY KEY, code VARCHAR(50) not null, name VARCHAR(50), pcode VARCHAR(255));");
            stmt.executeUpdate("create table if not exists office(id bigint auto_increment PRIMARY KEY, office_code VARCHAR(50) not null, office_name VARCHAR(50), contact_id VARCHAR(255));");
            stmt.executeUpdate("create table if not exists fin_account(id bigint auto_increment PRIMARY KEY, name VARCHAR(20) not null, type VARCHAR(50), currency VARCHAR(50),org_name VARCHAR(50),account_pin VARCHAR(50), remark VARCHAR(255));");
			
			stmt.executeUpdate("create table if not exists Toll_table(id bigint auto_increment PRIMARY KEY,code VARCHAR(20),name VARCHAR(20),type VARCHAR(20),Remark VARCHAR(255));");	
			
			
			String leadsSql = "create table if not exists leads(id bigint auto_increment PRIMARY KEY, "
                    + "title VARCHAR(255), priority varchar(50), create_date TIMESTAMP, creator varchar(50), status varchar(50),"
                    + "type varchar(50), region varchar(50), addr varchar(256), "
                    + "intro varchar(5120), remark VARCHAR(5120), lowest_price DECIMAL(20, 2), agent_fee DECIMAL(20, 2), "
                    + "introducer varchar(256), sales varchar(256), follower varchar(50), follower_phone varchar(50),"
                    + "owner varchar(50), owner_phone varchar(50), area decimal(10,2), total decimal(10,2), customer_source varchar(50), "
                    + "building_name varchar(255), building_unit varchar(50), building_no varchar(50), room_no varchar(50), is_have_car char(1) default 'N',"
                    + "is_public char(1) default 'N');";
		
            stmt.executeUpdate(leadsSql);

            stmt.executeUpdate("create table if not exists support_case(id bigint auto_increment PRIMARY KEY, title VARCHAR(255), type varchar(50), create_date TIMESTAMP, creator varchar(50), status varchar(50), case_desc VARCHAR(5120), note VARCHAR(5120));");

            stmt.executeUpdate("create table if not exists order_header(id bigint auto_increment PRIMARY KEY, order_no VARCHAR(50) not null, type varchar(50), status varchar(50), creator VARCHAR(50), create_date TIMESTAMP, remark varchar(256));");           
            stmt.executeUpdate("create table if not exists order_item(id bigint auto_increment PRIMARY KEY, order_id bigint, item_name VARCHAR(50), item_desc VARCHAR(50), quantity decimal(10,2), unit_price decimal(10,2), status varchar(50), FOREIGN KEY(order_id) REFERENCES order_header(id) );");
            //权限关联表
           // stmt.executeUpdate("create table if not exists userrole_table(id bigint auto_increment PRIMARY KEY,user_id bigint,role_id bigint,FOREIGN KEY(role_id) REFERENCES  role_table(id),FOREIGN KEY(user_id) REFERENCES  user_login(id));");
           //stmt.executeUpdate("create table if not exists user_role_privilege_table(id bigint auto_increment PRIMARY KEY,user_id bigint,role_id bigint,privilege_id bigint,FOREIGN KEY(role_id) REFERENCES  role_table(id),FOREIGN KEY(privilege_id) REFERENCES  privilege_table(id),FOREIGN KEY(user_id) REFERENCES user_login(id));");
            // party 当事人，可以有各种type
            stmt.executeUpdate("create table if not exists party(id bigint auto_increment PRIMARY KEY, party_type VARCHAR(32), create_date TIMESTAMP, creator varchar(50), last_update_date TIMESTAMP, last_updator varchar(50), status varchar(50), remark VARCHAR(5120));");
            stmt.executeUpdate("create table if not exists party_attribute(id bigint auto_increment PRIMARY KEY, party_id bigint, attr_name varchar(60), attr_value VARCHAR(255), create_date TIMESTAMP, creator varchar(50), FOREIGN KEY(party_id) REFERENCES party(id));");
            stmt.executeUpdate("create table if not exists contact(id bigint auto_increment PRIMARY KEY, company_name varchar(100), contact_person varchar(100), email varchar(100), mobile varchar(100), phone varchar(100), address VARCHAR(255), city varchar(100), postal_code varchar(60),"
                    + " create_date TIMESTAMP, Last_updated_stamp TIMESTAMP);");

            stmt.close();
            // conn.commit();
            conn.close();
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }

    public static void initData(C3p0Plugin cp) {
        try {
            cp.start();
            Connection conn = cp.getDataSource().getConnection();
            Statement stmt = conn.createStatement();

            stmt.executeUpdate("insert into user_login(user_name, password, password_hint) values('admin', '123456', '1-6');");
            stmt.executeUpdate("insert into user_login(user_name, password, password_hint) values('d_user1', '123456', '1-6');");
            stmt.executeUpdate("insert into user_login(user_name, password, password_hint) values('d_user2', '123456', '1-6');");
            stmt.executeUpdate("insert into user_login(user_name, password, password_hint) values('demo', '123456', '1-6');");
            stmt.executeUpdate("insert into user_login(user_name, password, password_hint) values('jason', '123456', '1-6');");
            
            
            stmt.executeUpdate("insert into location(code, name, pcode) values('110000', '北京', '1');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110100', '北京市', '110000');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110101', '东城区', '110100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110102', '西城区', '110100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110103', '崇文区', '110100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110104', '朝阳区', '110100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110105', '宣武区', '110100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('120000', '天津', '1');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('120100', '天津市', '120000');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110105', '宣武区', '120100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('120101', '和平区', '120100');");

            stmt.executeUpdate("insert into office(office_code, office_name, contact_id) values('1201', '广州司', '020-111');");
            stmt.executeUpdate("insert into office(office_code, office_name, contact_id) values('121', '珠公司', '0756-111');");
            stmt.executeUpdate("insert into office(office_code, office_name, contact_id) values('101', '深圳分公司', '0751-111');");

            stmt.executeUpdate("insert into fin_account(name,type,currency,org_name,account_pin,remark) values('李志坚','收费','人民币','建设银行','12123123123','穷人');");
            stmt.executeUpdate("insert into fin_account(name,type,currency,org_name,account_pin,remark) values('李四','收费','人民币','建设银行','12123123123','穷人');");
            stmt.executeUpdate("insert into fin_account(name,type,currency,org_name,account_pin,remark) values('张三','付费','人民币','建设银行','12123123123','穷人');");
           
            String propertySql = "insert into leads(title, create_date, creator, status, type, "
                    + "region, intro, remark, lowest_price, agent_fee, introducer, sales, follower, "
                    + "follower_phone, owner, owner_phone, customer_source, building_name, building_no, room_no, building_unit) values("
                    + "'%d 初始测试数据-老香洲两盘', CURRENT_TIMESTAMP(), 'jason', '出租', " + "'1房', '老香洲', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', '58自来客', '五洲花城2期', '2', '1320', '3');";

            // for (int i = 0; i < 50; i++) {
            // String newPropertySql = String.format(propertySql, i);
            // stmt.executeUpdate(newPropertySql);
            // }
            String sqlPrefix = "insert into leads(title, priority, create_date, creator, status, type, "
                    + "region, intro, remark, lowest_price, agent_fee, introducer, sales, follower, follower_phone, "
                    + "owner, owner_phone, area, total, customer_source, building_name, building_no, room_no, building_unit) values(";

            String sql =  sqlPrefix + "'初始测试数据-老香洲楼盘', '1重要紧急', CURRENT_TIMESTAMP(), 'jason', '出租', " + "'1房', '老香洲', "
                    + "'老香洲楼盘 2房2卫'," + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', 36, 1200, '58自来客', '五洲花城2期', '2', '1320', '3');";
            stmt.executeUpdate(sql);

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-新香洲楼盘', '1重要紧急', CURRENT_TIMESTAMP(), 'jason', '出售', " + "'2房', '新香洲', "
                    + "'新香洲楼盘 2房2卫'," + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', 78, 56, '58自来客', '五洲花城2期', '3', '1321', '5');");

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-老香洲楼盘', '2重要不紧急', CURRENT_TIMESTAMP(), 'jason', '已租', " + "'3房', '老香洲', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', 92, 2300, '58自来客', '五洲花城2期', '4', '1320', '3');");

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-拱北楼盘', '2重要不紧急', CURRENT_TIMESTAMP(), 'jason', '已售', " + "'4房', '拱北', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', 150, 120, '58自来客', '五洲花城2期', '6', '1320', '3');");

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-柠溪楼盘', '3不重要紧急', CURRENT_TIMESTAMP(), 'jason', '出租', " + "'5房', '柠溪', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', 180, 5000, '58自来客', '五洲花城2期', '', '1325', '8');");

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-柠溪楼盘', '3不重要紧急', CURRENT_TIMESTAMP(), 'jason', '出租', " + "'6房', '柠溪', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', 180, 5000, '58自来客', '五洲花城2期', '2', '', '5');");

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-柠溪楼盘', '3不重要紧急', CURRENT_TIMESTAMP(), 'jason', '出租', " + "'6房以上', '柠溪', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', 180, 5000, '58自来客', '五洲花城2期', '2', '1322', '');");

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-前山地皮', '4不重要不紧急', CURRENT_TIMESTAMP(), 'd_user1', '已售', " + "'地皮', '前山', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'd_user1', '13509871234',"
                    + "'张生', '0756-12345678-123', 40000, 3000, '58自来客', '五洲花城2期', '8', '1320', '3');");

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-柠溪楼盘', '3不重要紧急', CURRENT_TIMESTAMP(), 'jason', '出租', " + "'6房以上', '柠溪', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', 180, 5000, '58自来客', '五洲花城2期', '2', '1322', '');");

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-前山地皮', '4不重要不紧急', CURRENT_TIMESTAMP(), 'd_user1', '已售', " + "'地皮', '前山', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'd_user1', '13509871234',"
                    + "'张生', '0756-12345678-123', 40000, 3000, '58自来客', '五洲花城2期', '8', '1320', '3');");

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-柠溪楼盘', '3不重要紧急', CURRENT_TIMESTAMP(), 'jason', '出租', " + "'6房以上', '柠溪', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', 180, 5000, '58自来客', '五洲花城2期', '2', '1322', '');");

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-前山地皮', '4不重要不紧急', CURRENT_TIMESTAMP(), 'd_user1', '已售', " + "'地皮', '前山', "
                    + "'本月均价8260元/㎡，环比上月 ↑0.22 ，同比去年 ↑14.67 ，查看房价详情>>二 手 房50 套 所在区域香洲 老香洲小区地址香洲珠海市香洲区老香洲为农街为农市场地图>>建筑年代1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'd_user1', '13509871234',"
                    + "'张生', '0756-12345678-123', 40000, 3000, '58自来客', '五洲花城2期', '8', '1320', '3');");

            stmt.executeUpdate("insert into support_case(title, create_date, creator, status, type, case_desc, note) values("
                    + "'这是一个建议示例', CURRENT_TIMESTAMP(), 'jason', '新提交','出错', '这是一个建议示例，您可以在这里提交你所遇到的问题，我们会尽快跟进。', '这是回答的地方');");

            stmt.executeUpdate("insert into order_header(order_no, type, status, creator, create_date,  remark) values("
                    + "'SalesOrder001', 'SALES_ORDER', 'New', 'jason', CURRENT_TIMESTAMP(), '这是一个销售订单示例');");
            stmt.executeUpdate("insert into order_item(order_id, item_name, item_desc, quantity, unit_price) values("
                    + "1, 'P001', 'iPad Air', 1, 3200);");
            // 角色表
            stmt.executeUpdate("insert into role(role_name,remark) values('系统管理员','系统管理员拥有系统的所有模块的权限');");
            stmt.executeUpdate("insert into user_roles(user_id, role_id) values(1,1);");            
            stmt.executeUpdate("insert into user_roles(user_id, role_id) values(3,2);");
            stmt.executeUpdate("insert into user_roles(user_id, role_id) values(3,3);");
            stmt.executeUpdate("insert into role_permissions(role_id, role_permission) values(1,'*');");
            stmt.executeUpdate("insert into role_permissions(role_id, role_permission) values(2,'u');");
            stmt.executeUpdate("insert into role_permissions(role_id, role_permission) values(3,'v c ');");
           
            
            stmt.executeUpdate("insert into role(role_name,remark) values('仓管', '李四');");
			stmt.executeUpdate("insert into role(role_name,remark) values('调度', '赵六');");
			stmt.executeUpdate("insert into role(role_name,remark) values('文员', '钱七');");
			//收费付费	
			stmt.executeUpdate("insert into Toll_table(code,name,type,Remark) values('20132014','运输收费','收费','这是一张运输收费单');");
			stmt.executeUpdate("insert into Toll_table(code,name,type,Remark) values('20142015','运输付费','付费','这是一张运输付费单');");
		//权限表定义
			stmt.executeUpdate("insert into permissions(permissions) values('*/');");
			stmt.executeUpdate("insert into permissions(permissions) values('查看');");
			stmt.executeUpdate("insert into permissions(permissions) values('增加');");
			stmt.executeUpdate("insert into permissions(permissions) values('修改');");
			stmt.executeUpdate("insert into permissions(permissions) values('删除');");			
			//权限关联表
			//stmt.executeUpdate("insert into user_role_privilege_table(user_id,role_id,privilege_id) values(1,1,2);");
			//stmt.executeUpdate("insert into user_role_privilege_table(user_id,role_id,privilege_id) values(1,1,3);");
			//stmt.executeUpdate("insert into user_role_privilege_table(user_id,role_id,privilege_id) values(1,1,4);");
			//stmt.executeUpdate("insert into user_role_privilege_table(user_id,role_id,privilege_id) values(1,1,5);");
			//stmt.executeUpdate("insert into user_role_privilege_table(user_id,role_id,privilege_id) values(2,1,2);");			
			
            // 贷款客户 attributes
            for (int i = 1; i <= 1; i++) {
                stmt.executeUpdate("insert into party(party_type, create_date, creator) values('贷款客户', CURRENT_TIMESTAMP(), 'demo');");
                stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(" + i + ", 'priority', '1重要紧急');");
                stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(" + i + ", 'name', '温生');");
                stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(" + i + ", 'loan_max', '15万');");
                stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(" + i
                        + ", 'mobile', '1357038829');");
                stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(" + i
                        + ", 'email', 'test@test.com');");
            }

            // 地产客户
            Party p = new Party();
            Date createDate = Calendar.getInstance().getTime();
            p.set("party_type", "地产客户").set("create_date", createDate).set("creator", "jason").save();
            long partyId = p.getLong("id");
            PartyAttribute pa = new PartyAttribute();
            pa.set("party_id", partyId).set("attr_name", "title").set("attr_value", "求2房近3小").save();
            PartyAttribute pa1 = new PartyAttribute();
            pa1.set("party_id", partyId).set("attr_name", "client_name").set("attr_value", "温生").save();
            PartyAttribute paPriority = new PartyAttribute();
            paPriority.set("party_id", partyId).set("attr_name", "priority").set("attr_value", "1重要紧急").save();
            PartyAttribute pa2 = new PartyAttribute();
            pa2.set("party_id", partyId).set("attr_name", "status").set("attr_value", "求租").save();
            PartyAttribute pa3 = new PartyAttribute();
            pa3.set("party_id", partyId).set("attr_name", "region").set("attr_value", "老香洲").save();
            PartyAttribute pa4 = new PartyAttribute();
            pa4.set("party_id", partyId).set("attr_name", "type").set("attr_value", "1房").save();

            // 外部user 创建的客户
            Party p1 = new Party();
            createDate = Calendar.getInstance().getTime();
            p1.set("party_type", "地产客户").set("create_date", createDate).set("creator", "demo").save();
            partyId = p1.getLong("id");
            PartyAttribute p1_pa = new PartyAttribute();
            p1_pa.set("party_id", partyId).set("attr_name", "title").set("attr_value", "求前山小区").save();
            PartyAttribute p1_pa1 = new PartyAttribute();
            p1_pa1.set("party_id", partyId).set("attr_name", "client_name").set("attr_value", "温生").save();
            PartyAttribute p1_paPriority = new PartyAttribute();
            p1_paPriority.set("party_id", partyId).set("attr_name", "priority").set("attr_value", "1重要紧急").save();
            PartyAttribute p1_pa2 = new PartyAttribute();
            p1_pa2.set("party_id", partyId).set("attr_name", "status").set("attr_value", "求购").save();
            PartyAttribute p1_pa3 = new PartyAttribute();
            p1_pa3.set("party_id", partyId).set("attr_name", "region").set("attr_value", "拱北").save();
            PartyAttribute p1_pa4 = new PartyAttribute();
            p1_pa4.set("party_id", partyId).set("attr_name", "type").set("attr_value", "1房").save();
            PartyAttribute p1_pa5 = new PartyAttribute();
            p1_pa5.set("party_id", partyId).set("attr_name", "area").set("attr_value", "120").save();
            PartyAttribute p1_pa6 = new PartyAttribute();
            p1_pa6.set("party_id", partyId).set("attr_name", "total").set("attr_value", "200").save();

            newCustomer();
            // 其他客户 attributes
            stmt.executeUpdate("insert into party(party_type, create_date, creator) values('其他客户', CURRENT_TIMESTAMP(), 'demo');");
            stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(1, 'note', '工商注册');");
            stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(1, 'mobile', '1357038829');");
            stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(1, 'email', 'test@test.com');");

            stmt.close();
            // conn.commit();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void newCustomer() {
        Contact contact = new Contact();
        contact.set("company_name", "珠海创诚易达信息科技有限公司").set("contact_person", "温生").set("email", "test@test.com");
        contact.set("mobile", "1234567").set("phone", "1234567").set("address", "香洲珠海市香洲区老香洲为农街为农市场").set("postal_code", "519000").save();

        Party p1 = new Party();
        Date createDate = Calendar.getInstance().getTime();
        p1.set("contact_id", contact.getLong("id")).set("party_type", "CUSTOMER").set("create_date", createDate).set("creator", "demo")
                .save();
    }
}
