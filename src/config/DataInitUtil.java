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

            // 登陆及授权的3个表
            stmt.executeUpdate("create table if not exists user_login(id bigint auto_increment PRIMARY KEY, user_name VARCHAR(50) not null, password VARCHAR(50) not null, password_hint VARCHAR(255));");
            stmt.executeUpdate("create table if not exists user_roles(id bigint auto_increment PRIMARY KEY, user_name VARCHAR(50) not null, role_name VARCHAR(255) not null, remark VARCHAR(255));");
            stmt.executeUpdate("create table if not exists role_permissions(id bigint auto_increment PRIMARY KEY, role_name VARCHAR(50) not null, role_permission VARCHAR(50), remark VARCHAR(255));");
            stmt.executeUpdate("create table if not exists location(id bigint auto_increment PRIMARY KEY, code VARCHAR(50) not null, name VARCHAR(50), pcode VARCHAR(255));");
            stmt.executeUpdate("create table if not exists office(id bigint auto_increment PRIMARY KEY, office_code VARCHAR(50) not null, office_name VARCHAR(50), contact_id VARCHAR(255));");
            stmt.executeUpdate("create table if not exists fin_account(id bigint auto_increment PRIMARY KEY, name VARCHAR(20) not null, type VARCHAR(50), currency VARCHAR(50),org_name VARCHAR(50),account_pin VARCHAR(50), remark VARCHAR(255));");
            stmt.executeUpdate("create table if not exists contract(id bigint auto_increment PRIMARY KEY, name VARCHAR(50) not null, type VARCHAR(50), party_id bigint,period_from Timestamp,period_to Timestamp, remark VARCHAR(255));");
            stmt.executeUpdate("create table if not exists role_table(id bigint auto_increment PRIMARY KEY,role_name VARCHAR(50),role_time TIMESTAMP,role_people VARCHAR(50),role_lasttime TIMESTAMP,role_lastpeople VARCHAR(50));");
            stmt.executeUpdate("create table if not exists Toll_table(id bigint auto_increment PRIMARY KEY,code VARCHAR(20),name VARCHAR(20),type VARCHAR(20),Remark VARCHAR(255));");
            stmt.executeUpdate("create table if not exists privilege_table(id bigint auto_increment PRIMARY KEY,privilege VARCHAR(50));");
            
            stmt.executeUpdate("create table if not exists contract_item(id bigint auto_increment PRIMARY KEY,contract_id bigint,route_id bigint, amount Double,remark VARCHAR(255));");
            stmt.executeUpdate("create table if not exists delivery_order(id bigint auto_increment PRIMARY KEY,Order_no VARCHAR(50),Transfer_order_id VARCHAR(50), customer_id bigint,sp_id bigint,notify_party_id bigint,appointment_stamp timestamp,status VARCHAR(50),cargo_nature Varchar(20),from_warehouse_code Varchar(20),Remark Varchar(255),Create_by bigint,Create_stamp timestamp,Last_modified_by bigint,Last_modified_stamp timestamp);");
            
            stmt.executeUpdate("create table if not exists route(id bigint auto_increment PRIMARY KEY,from_id VARCHAR(50), location_from VARCHAR(50) not null,to_id VARCHAR(50), location_to VARCHAR(50) not null, remark VARCHAR(255));");

            stmt.executeUpdate("create table if not exists leads(id bigint auto_increment PRIMARY KEY, "
                    + "title VARCHAR(255), priority varchar(50), create_date TIMESTAMP, creator varchar(50), status varchar(50),"
                    + "type varchar(50), region varchar(50), addr varchar(256), "
                    + "intro varchar(5120), remark VARCHAR(5120), lowest_price DECIMAL(20, 2), agent_fee DECIMAL(20, 2), "
                    + "introducer varchar(256), sales varchar(256), follower varchar(50), follower_phone varchar(50),"
                    + "owner varchar(50), owner_phone varchar(50), area decimal(10,2), total decimal(10,2), customer_source varchar(50), "
                    + "building_name varchar(255), building_unit varchar(50), building_no varchar(50), room_no varchar(50), is_have_car char(1) default 'N',"
                    + "is_public char(1) default 'N');");

            stmt.executeUpdate("create table if not exists support_case(id bigint auto_increment PRIMARY KEY, title VARCHAR(255), type varchar(50), create_date TIMESTAMP, creator varchar(50), status varchar(50), case_desc VARCHAR(5120), note VARCHAR(5120));");

            stmt.executeUpdate("create table if not exists order_header(id bigint auto_increment PRIMARY KEY, order_no VARCHAR(50) not null, type varchar(50), status varchar(50), creator VARCHAR(50), create_date TIMESTAMP, remark varchar(256));");
            stmt.executeUpdate("create table if not exists order_item(id bigint auto_increment PRIMARY KEY, order_id bigint, item_name VARCHAR(50), item_desc VARCHAR(50), quantity decimal(10,2), unit_price decimal(10,2), status varchar(50), FOREIGN KEY(order_id) REFERENCES order_header(id) );");

            // party 当事人，可以有各种type
            stmt.executeUpdate("create table if not exists party(id bigint auto_increment PRIMARY KEY, party_type VARCHAR(32), contact_id bigint, create_date TIMESTAMP, creator varchar(50), last_update_date TIMESTAMP, last_updator varchar(50), status varchar(50), remark VARCHAR(5120));");
            stmt.executeUpdate("create table if not exists party_attribute(id bigint auto_increment PRIMARY KEY, party_id bigint, attr_name varchar(60), attr_value VARCHAR(255), create_date TIMESTAMP, creator varchar(50), FOREIGN KEY(party_id) REFERENCES party(id));");
            stmt.executeUpdate("create table if not exists contact(id bigint auto_increment PRIMARY KEY, company_name varchar(100), contact_person varchar(100), email varchar(100), mobile varchar(100), phone varchar(100), address VARCHAR(255), city varchar(100), postal_code varchar(60),"
                    + " create_date TIMESTAMP, Last_updated_stamp TIMESTAMP);");

            // product 产品
            stmt.executeUpdate("create table if not exists product(id bigint auto_increment PRIMARY KEY,item_name varchar(50),item_no varchar(255),item_desc varchar(5120));");
            
            // warehouse 仓库
            stmt.executeUpdate("create table if not exists warehouse(id bigint auto_increment PRIMARY KEY,warehouse_name varchar(50),warehouse_address varchar(255),warehouse_desc VARCHAR(5120),contact_id bigint,FOREIGN KEY(contact_id) REFERENCES contact(id));");
            
            // order_status 里程碑
            stmt.executeUpdate("create table if not exists order_status(id bigint auto_increment PRIMARY KEY,status_code varchar(20),status_name varchar(20),order_type varchar(20),remark varchar(255));");
            
            // transfer_order 运输单
            stmt.executeUpdate("create table if not exists transfer_order(id bigint auto_increment PRIMARY KEY,order_no varchar(255),status varchar(255),"
            					+"cargo_nature VARCHAR(255),pickup_mode VARCHAR(255),arrival_mode VARCHAR(255),remark varchar(255),create_by bigint,"
            					+"create_stamp TIMESTAMP,last_modified_by bigint,last_modified_stamp TIMESTAMP,eta TIMESTAMP,route_from varchar(255),route_to varchar(255),"
            					+"route_id bigint,customer_id bigint,sp_id bigint,notify_party_id bigint,FOREIGN KEY(customer_id) REFERENCES party(id),FOREIGN KEY(sp_id) REFERENCES party(id),"
            					+"FOREIGN KEY(route_id) REFERENCES route(id),FOREIGN KEY(notify_party_id) REFERENCES contact(id));");
            // transfer_order_item 货品明细
            stmt.executeUpdate("create table if not exists transfer_order_item(id bigint auto_increment PRIMARY KEY,item_no varchar(255),item_name varchar(255),item_desc varchar(255),"
            				   +"amount varchar(255),unit varchar(255),volume varchar(255),weight varchar(255),remark varchar(5120),order_id bigint,FOREIGN KEY(order_id) REFERENCES transfer_order(id));");
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

            stmt.executeUpdate("insert into contract(name,type,party_id,period_from,period_to,remark) values('客户合同','CUSTOMER', 4,'2014-11-12','2014-11-14','无');");
            stmt.executeUpdate("insert into contract(name,type,party_id,period_from,period_to,remark) values('客户合同','CUSTOMER', 5,'2014-10-12','2014-11-15','无');");
            stmt.executeUpdate("insert into contract(name,type,party_id,period_from,period_to,remark) values('供应商合同','SERVICE_PROVIDER', 6,'2011-1-12','2014-10-14','无');");
            stmt.executeUpdate("insert into contract(name,type,party_id,period_from,period_to,remark) values('供应商合同','SERVICE_PROVIDER', 7,'2013-11-12','2014-11-14','无');");

            stmt.executeUpdate("insert into route(from_id,location_from,to_id,location_to,remark) values('110000','北京','110103','宣武区','123123');");
            stmt.executeUpdate("insert into route(from_id,location_from,to_id,location_to,remark) values('110000','北京','120000','天津','123123');");
            stmt.executeUpdate("insert into route(from_id,location_from,to_id,location_to,remark) values('120000','天津','110000','北京','123123');");

            stmt.executeUpdate("insert into contract_item(contract_id,route_id,amount,remark) values('1','1','120000','路线');");
            stmt.executeUpdate("insert into contract_item(contract_id,route_id,amount,remark) values('2','2','130000','路线2');");
            stmt.executeUpdate("insert into contract_item(contract_id,route_id,amount,remark) values('1','3','120000','路线');");
            stmt.executeUpdate("insert into contract_item(contract_id,route_id,amount,remark) values('2','1','130000','路线2');");
            stmt.executeUpdate("insert into contract_item(contract_id,route_id,amount,remark) values('3','2','120000','路线');");
            stmt.executeUpdate("insert into contract_item(contract_id,route_id,amount,remark) values('4','3','130000','路线2');");
            // 系统权限
            stmt.executeUpdate("insert into role_permissions(role_name, role_permission, remark) values('root', '123456', '1-6');");
            // alter table leads add(priority varchar(50),customer_source
            // varchar(50), building_name varchar(255), building_no varchar(50),
            // room_no varchar(50)); 2-6
            // alter table leads add(building_unit varchar(50), is_have_car
            // char(1) default 'N');
            // alter table leads add(is_public char(1) default 'N');

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

            stmt.executeUpdate(sqlPrefix + "'初始测试数据-老香洲楼盘', '1重要紧急', CURRENT_TIMESTAMP(), 'jason', '出租', " + "'1房', '老香洲', "
                    + "'老香洲楼盘 2房2卫'," + "'remark.....', 7000, 7500, " + "'介绍人金', 'kim', 'jason', '13509871234',"
                    + "'张生', '0756-12345678-123', 36, 1200, '58自来客', '五洲花城2期', '2', '1320', '3');");

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

            // stmt.executeUpdate("insert into role_table(role_name,role_time,role_people,role_lasttime,role_lastpeople) values('浠撶',CURRENT_TIMESTAMP(),'寮犱笁',CURRENT_TIMESTAMP(),'鏉庡洓');");
            // stmt.executeUpdate("insert into role_table(role_name,role_time,role_people,role_lasttime,role_lastpeople) values('璋冨害',CURRENT_TIMESTAMP(),'鐜嬩簲',CURRENT_TIMESTAMP(),'璧靛叚');");
            // 鏀惰垂浠樿垂
            // stmt.executeUpdate("insert into Toll_table(code,name,type,Remark) values('20132014','杩愯緭鏀惰垂','鏀惰垂','杩欐槸涓€寮犺繍杈撴敹璐瑰崟');");
            // stmt.executeUpdate("insert into Toll_table(code,name,type,Remark) values('20142015','杩愯緭浠樿垂','浠樿垂','杩欐槸涓€寮犺繍杈撲粯璐瑰崟');");
            // 鏉冮檺琛ㄥ畾涔
            stmt.executeUpdate("insert into privilege_table(privilege) values('*');");
            stmt.executeUpdate("insert into privilege_table(privilege) values('view');");
            stmt.executeUpdate("insert into privilege_table(privilege) values('create');");
            stmt.executeUpdate("insert into privilege_table(privilege) values('update');");
            stmt.executeUpdate("insert into privilege_table(privilege) values('delete');");

            // 收费条目定义表code VARCHAR(50),name VARCHAR(50),type VARCHAR(50),Remark
            // VARCHAR(50)
            // for(int i=0;i<15;i++){
            // stmt.executeUpdate("insert into Toll_table(code,name,type,Remark) values("
            // + "'2013201448','运输收费','付款','这是一张运输收费单');");
            // }
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
            
            // 运输单
            stmt.executeUpdate("insert into transfer_order(CARGO_NATURE, SP_ID, NOTIFY_PARTY_ID, ORDER_NO, CREATE_BY, PICKUP_MODE, CUSTOMER_ID, STATUS, CREATE_STAMP, ARRIVAL_MODE) values('ATM', '3', '1', 'ca1edc18-f698-486b-82e3-86788859525c', '3', '干线供应商自提', '1', '订单已生成', '2014-04-26 16:33:35.1', '货品直送');");
            stmt.executeUpdate("insert into transfer_order(CARGO_NATURE, SP_ID, NOTIFY_PARTY_ID, ORDER_NO, CREATE_BY, PICKUP_MODE, CUSTOMER_ID, STATUS, CREATE_STAMP, ARRIVAL_MODE) values('普通货品 ', '4', '2', 'ca1edc18-f698-486b-82e3-86788859888c', '4', '公司自提', '2', '订单已生成', '2014-04-26 16:40:35.1', '入中转仓');");
            stmt.executeUpdate("insert into transfer_order(CARGO_NATURE, SP_ID, NOTIFY_PARTY_ID, ORDER_NO, CREATE_BY, PICKUP_MODE, CUSTOMER_ID, STATUS, CREATE_STAMP, ARRIVAL_MODE) values('ATM', '3', '1', 'ca1edc18-f698-486b-82e3-86788859525c', '3', '干线供应商自提', '1', '订单已生成', '2014-04-26 16:33:35.1', '入中转仓');");
            stmt.executeUpdate("insert into transfer_order(CARGO_NATURE, SP_ID, NOTIFY_PARTY_ID, ORDER_NO, CREATE_BY, PICKUP_MODE, CUSTOMER_ID, STATUS, CREATE_STAMP, ARRIVAL_MODE) values('普通货品 ', '4', '2', 'ca1edc18-f698-486b-82e3-86788859888c', '4', '公司自提', '2', '订单已生成', '2014-04-26 16:40:35.1', '货品直送');");

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
        contact.set("mobile", "12345671").set("phone", "113527229313").set("address", "香洲珠海市香洲区老香洲为农街为农市场1").set("postal_code", "5190001")
                .save();
        Contact contact2 = new Contact();
        contact2.set("company_name", "北京制药珠海分公司").set("contact_person", "黄生").set("email", "test@test.com");
        contact2.set("mobile", "12345672").set("phone", "213527229313").set("address", "香洲珠海市香洲区老香洲为农街为农市场2").set("postal_code", "5190002")
                .save();
        Contact contact3 = new Contact();
        contact3.set("company_name", "上海能源科技有限公司").set("contact_person", "李生").set("email", "test@test.com");
        contact3.set("mobile", "12345673").set("phone", "313527229313").set("address", "香洲珠海市香洲区老香洲为农街为农市场3").set("postal_code", "5190003")
                .save();
        Contact contact4 = new Contact();
        contact4.set("company_name", "天津佛纳甘科技有限公司").set("contact_person", "何生").set("email", "test@test.com");
        contact4.set("mobile", "12345674").set("phone", "413527229313").set("address", "香洲珠海市香洲区老香洲为农街为农市场4").set("postal_code", "5190004")
                .save();

        Party p1 = new Party();
        Party p2 = new Party();
        Party p3 = new Party();
        Party p4 = new Party();
        Date createDate = Calendar.getInstance().getTime();
        p1.set("contact_id", contact.getLong("id")).set("party_type", "CUSTOMER").set("create_date", createDate).set("creator", "demo")
                .save();
        p2.set("contact_id", contact2.getLong("id")).set("party_type", "CUSTOMER").set("create_date", createDate).set("creator", "demo")
                .save();
        p3.set("contact_id", contact3.getLong("id")).set("party_type", "SERVICE_PROVIDER").set("create_date", createDate)
                .set("creator", "demo").save();
        p4.set("contact_id", contact4.getLong("id")).set("party_type", "SERVICE_PROVIDER").set("create_date", createDate)
                .set("creator", "demo").save();

    }
}
