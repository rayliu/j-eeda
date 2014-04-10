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

            // 鐧婚檰鍙婃巿鏉冪殑3涓〃
            stmt.executeUpdate("create table if not exists user_login(id bigint auto_increment PRIMARY KEY, user_name VARCHAR(50) not null, password VARCHAR(50) not null, password_hint VARCHAR(255));");
            stmt.executeUpdate("create table if not exists user_roles(id bigint auto_increment PRIMARY KEY, user_name VARCHAR(50) not null, role_name VARCHAR(255) not null, remark VARCHAR(255));");
            stmt.executeUpdate("create table if not exists role_permissions(id bigint auto_increment PRIMARY KEY, role_name VARCHAR(50) not null, role_permission VARCHAR(50), remark VARCHAR(255));");
            stmt.executeUpdate("create table if not exists location(id bigint auto_increment PRIMARY KEY, code VARCHAR(50) not null, name VARCHAR(50), pcode VARCHAR(255));");
            stmt.executeUpdate("create table if not exists office(id bigint auto_increment PRIMARY KEY, office_code VARCHAR(50) not null, office_name VARCHAR(50), contact_id VARCHAR(255));");
            stmt.executeUpdate("create table if not exists fin_account(id bigint auto_increment PRIMARY KEY, name VARCHAR(20) not null, type VARCHAR(50), currency VARCHAR(50),org_name VARCHAR(50),account_pin VARCHAR(50), remark VARCHAR(255));");
			stmt.executeUpdate("create table if not exists role_table(id bigint auto_increment PRIMARY KEY,role_name VARCHAR(50),role_time TIMESTAMP,role_people VARCHAR(50),role_lasttime TIMESTAMP,role_lastpeople VARCHAR(50));");
			stmt.executeUpdate("create table if not exists Toll_table(id bigint auto_increment PRIMARY KEY,code VARCHAR(20),name VARCHAR(20),type VARCHAR(20),Remark VARCHAR(255));");	
			stmt.executeUpdate("create table if not exists privilege_table(id bigint auto_increment PRIMARY KEY,privilege VARCHAR(50));");	
			

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

            // party 褰撲簨浜猴紝鍙互鏈夊悇绉峵ype
            stmt.executeUpdate("create table if not exists party(id bigint auto_increment PRIMARY KEY, party_type VARCHAR(32), contact_id bigint, create_date TIMESTAMP, creator varchar(50), last_update_date TIMESTAMP, last_updator varchar(50), status varchar(50), remark VARCHAR(5120));");
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

            stmt.executeUpdate("insert into user_login(user_name, password, password_hint) values('d_user1', '123456', '1-6');");
            stmt.executeUpdate("insert into user_login(user_name, password, password_hint) values('d_user2', '123456', '1-6');");
            stmt.executeUpdate("insert into user_login(user_name, password, password_hint) values('demo', '123456', '1-6');");
            stmt.executeUpdate("insert into user_login(user_name, password, password_hint) values('jason', '123456', '1-6');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110000', '鍖椾含', '1');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110100', '鍖椾含甯, '110000');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110101', '涓滃煄鍖, '110100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110102', '瑗垮煄鍖, '110100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110103', '宕囨枃鍖, '110100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110104', '鏈濋槼鍖, '110100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110105', '瀹ｆ鍖, '110100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('120000', '澶╂触', '1');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('120100', '澶╂触甯, '120000');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('110105', '瀹ｆ鍖, '120100');");
            stmt.executeUpdate("insert into location(code, name, pcode) values('120101', '鍜屽钩鍖, '120100');");

            stmt.executeUpdate("insert into office(office_code, office_name, contact_id) values('1201', '骞垮窞鍙, '020-111');");
            stmt.executeUpdate("insert into office(office_code, office_name, contact_id) values('121', '鐝犲叕鍙, '0756-111');");
            stmt.executeUpdate("insert into office(office_code, office_name, contact_id) values('101', '娣卞湷鍒嗗叕鍙, '0751-111');");

            stmt.executeUpdate("insert into fin_account(name,type,currency,org_name,account_pin,remark) values('鏉庡織鍧,'鏀惰垂','浜烘皯甯,'寤鸿閾惰','12123123123','绌蜂汉');");
            stmt.executeUpdate("insert into fin_account(name,type,currency,org_name,account_pin,remark) values('鏉庡洓','鏀惰垂','浜烘皯甯,'寤鸿閾惰','12123123123','绌蜂汉');");
            stmt.executeUpdate("insert into fin_account(name,type,currency,org_name,account_pin,remark) values('寮犱笁','浠樿垂','浜烘皯甯,'寤鸿閾惰','12123123123','绌蜂汉');");
            
            stmt.executeUpdate("insert into contract(name,type,party_id,period_from,period_to,remark) values('瀹㈡埛鍚堝悓','CUSTOMER','1101','2014-11-12','2014-11-14','鏃);");
            stmt.executeUpdate("insert into contract(name,type,party_id,period_from,period_to,remark) values('瀹㈡埛鍚堝悓','CUSTOMER','1102','2014-10-12','2014-11-15','鏃);");
            stmt.executeUpdate("insert into contract(name,type,party_id,period_from,period_to,remark) values('渚涘簲鍟嗗悎鍚,'SERVICE_PROVIDER','2101','2011-1-12','2014-10-14','鏃);");
            stmt.executeUpdate("insert into contract(name,type,party_id,period_from,period_to,remark) values('渚涘簲鍟嗗悎鍚,'SERVICE_PROVIDER','2102','2013-11-12','2014-11-14','鏃);");
            // 绯荤粺鏉冮檺
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
                    + "'%d 鍒濆娴嬭瘯鏁版嵁-鑰侀娲蹭袱鐩, CURRENT_TIMESTAMP(), 'jason', '鍑虹', " + "'1鎴, '鑰侀娲, "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'jason', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '2', '1320', '3');";

            // for (int i = 0; i < 50; i++) {
            // String newPropertySql = String.format(propertySql, i);
            // stmt.executeUpdate(newPropertySql);
            // }
            String sqlPrefix = "insert into leads(title, priority, create_date, creator, status, type, "
                    + "region, intro, remark, lowest_price, agent_fee, introducer, sales, follower, follower_phone, "
                    + "owner, owner_phone, area, total, customer_source, building_name, building_no, room_no, building_unit) values(";

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鑰侀娲叉ゼ鐩, '1閲嶈绱ф€, CURRENT_TIMESTAMP(), 'jason', '鍑虹', " + "'1鎴, '鑰侀娲, "
                    + "'鑰侀娲叉ゼ鐩2鎴鍗," + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'jason', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 36, 1200, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '2', '1320', '3');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鏂伴娲叉ゼ鐩, '1閲嶈绱ф€, CURRENT_TIMESTAMP(), 'jason', '鍑哄敭', " + "'2鎴, '鏂伴娲, "
                    + "'鏂伴娲叉ゼ鐩2鎴鍗," + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'jason', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 78, 56, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '3', '1321', '5');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鑰侀娲叉ゼ鐩, '2閲嶈涓嶇揣鎬, CURRENT_TIMESTAMP(), 'jason', '宸茬', " + "'3鎴, '鑰侀娲, "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'jason', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 92, 2300, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '4', '1320', '3');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鎷卞寳妤肩洏', '2閲嶈涓嶇揣鎬, CURRENT_TIMESTAMP(), 'jason', '宸插敭', " + "'4鎴, '鎷卞寳', "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'jason', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 150, 120, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '6', '1320', '3');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鏌犳邯妤肩洏', '3涓嶉噸瑕佺揣鎬, CURRENT_TIMESTAMP(), 'jason', '鍑虹', " + "'5鎴, '鏌犳邯', "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'jason', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 180, 5000, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '', '1325', '8');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鏌犳邯妤肩洏', '3涓嶉噸瑕佺揣鎬, CURRENT_TIMESTAMP(), 'jason', '鍑虹', " + "'6鎴, '鏌犳邯', "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'jason', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 180, 5000, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '2', '', '5');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鏌犳邯妤肩洏', '3涓嶉噸瑕佺揣鎬, CURRENT_TIMESTAMP(), 'jason', '鍑虹', " + "'6鎴夸互涓, '鏌犳邯', "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'jason', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 180, 5000, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '2', '1322', '');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鍓嶅北鍦扮毊', '4涓嶉噸瑕佷笉绱ф€, CURRENT_TIMESTAMP(), 'd_user1', '宸插敭', " + "'鍦扮毊', '鍓嶅北', "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'd_user1', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 40000, 3000, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '8', '1320', '3');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鏌犳邯妤肩洏', '3涓嶉噸瑕佺揣鎬, CURRENT_TIMESTAMP(), 'jason', '鍑虹', " + "'6鎴夸互涓, '鏌犳邯', "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'jason', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 180, 5000, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '2', '1322', '');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鍓嶅北鍦扮毊', '4涓嶉噸瑕佷笉绱ф€, CURRENT_TIMESTAMP(), 'd_user1', '宸插敭', " + "'鍦扮毊', '鍓嶅北', "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'd_user1', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 40000, 3000, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '8', '1320', '3');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鏌犳邯妤肩洏', '3涓嶉噸瑕佺揣鎬, CURRENT_TIMESTAMP(), 'jason', '鍑虹', " + "'6鎴夸互涓, '鏌犳邯', "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'jason', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 180, 5000, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '2', '1322', '');");

            stmt.executeUpdate(sqlPrefix + "'鍒濆娴嬭瘯鏁版嵁-鍓嶅北鍦扮毊', '4涓嶉噸瑕佷笉绱ф€, CURRENT_TIMESTAMP(), 'd_user1', '宸插敭', " + "'鍦扮毊', '鍓嶅北', "
                    + "'鏈湀鍧囦环8260鍏銕★紝鐜瘮涓婃湀 鈫.22 锛屽悓姣斿幓骞鈫4.67 锛屾煡鐪嬫埧浠疯鎯>浜鎵鎴0 濂鎵€鍦ㄥ尯鍩熼娲鑰侀娲插皬鍖哄湴鍧€棣欐床鐝犳捣甯傞娲插尯鑰侀娲蹭负鍐滆涓哄啘甯傚満鍦板浘>>寤虹瓚骞翠唬1995-01-01',"
                    + "'remark.....', 7000, 7500, " + "'浠嬬粛浜洪噾', 'kim', 'd_user1', '13509871234',"
                    + "'寮犵敓', '0756-12345678-123', 40000, 3000, '58鑷潵瀹, '浜旀床鑺卞煄2鏈, '8', '1320', '3');");

            stmt.executeUpdate("insert into support_case(title, create_date, creator, status, type, case_desc, note) values("
                    + "'杩欐槸涓€涓缓璁ず渚, CURRENT_TIMESTAMP(), 'jason', '鏂版彁浜,'鍑洪敊', '杩欐槸涓€涓缓璁ず渚嬶紝鎮ㄥ彲浠ュ湪杩欓噷鎻愪氦浣犳墍閬囧埌鐨勯棶棰橈紝鎴戜滑浼氬敖蹇窡杩涖€, '杩欐槸鍥炵瓟鐨勫湴鏂);");

            stmt.executeUpdate("insert into order_header(order_no, type, status, creator, create_date,  remark) values("
                    + "'SalesOrder001', 'SALES_ORDER', 'New', 'jason', CURRENT_TIMESTAMP(), '杩欐槸涓€涓攢鍞鍗曠ず渚);");
            stmt.executeUpdate("insert into order_item(order_id, item_name, item_desc, quantity, unit_price) values("
                    + "1, 'P001', 'iPad Air', 1, 3200);");
            // 瑙掕壊琛
            stmt.executeUpdate("insert into role_table(role_name,role_time,role_people,role_lasttime,role_lastpeople) values('浠撶',CURRENT_TIMESTAMP(),'寮犱笁',CURRENT_TIMESTAMP(),'鏉庡洓');");
			stmt.executeUpdate("insert into role_table(role_name,role_time,role_people,role_lasttime,role_lastpeople) values('璋冨害',CURRENT_TIMESTAMP(),'鐜嬩簲',CURRENT_TIMESTAMP(),'璧靛叚');");
			//鏀惰垂浠樿垂	
			stmt.executeUpdate("insert into Toll_table(code,name,type,Remark) values('20132014','杩愯緭鏀惰垂','鏀惰垂','杩欐槸涓€寮犺繍杈撴敹璐瑰崟');");
			stmt.executeUpdate("insert into Toll_table(code,name,type,Remark) values('20142015','杩愯緭浠樿垂','浠樿垂','杩欐槸涓€寮犺繍杈撲粯璐瑰崟');");
			//鏉冮檺琛ㄥ畾涔
			stmt.executeUpdate("insert into privilege_table(privilege) values('*');");
			stmt.executeUpdate("insert into privilege_table(privilege) values('view');");
			stmt.executeUpdate("insert into privilege_table(privilege) values('create');");
			stmt.executeUpdate("insert into privilege_table(privilege) values('update');");
			stmt.executeUpdate("insert into privilege_table(privilege) values('delete');");			

            // 鏀惰垂鏉＄洰瀹氫箟琛╟ode VARCHAR(50),name VARCHAR(50),type VARCHAR(50),Remark
            // VARCHAR(50)
            // for(int i=0;i<15;i++){
            // stmt.executeUpdate("insert into Toll_table(code,name,type,Remark) values("
            // + "'2013201448','杩愯緭鏀惰垂','浠樻','杩欐槸涓€寮犺繍杈撴敹璐瑰崟');");
            // }
            // 璐锋瀹㈡埛 attributes
            for (int i = 1; i <= 3; i++) {
                stmt.executeUpdate("insert into party(party_type, create_date, creator) values('璐锋瀹㈡埛', CURRENT_TIMESTAMP(), 'demo');");
                stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(" + i + ", 'priority', '1閲嶈绱ф€);");
                stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(" + i + ", 'name', '娓╃敓');");
                stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(" + i + ", 'loan_max', '15涓);");
                stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(" + i + ", 'mobile', '1357038829');");
                stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(" + i + ", 'email', 'test@test.com');");
            }

            // 鍦颁骇瀹㈡埛
            Party p = new Party();
            Date createDate = Calendar.getInstance().getTime();
            p.set("party_type", "鍦颁骇瀹㈡埛").set("create_date", createDate).set("creator", "jason").save();
            long partyId = p.getLong("id");
            PartyAttribute pa = new PartyAttribute();
            pa.set("party_id", partyId).set("attr_name", "title").set("attr_value", "姹鎴胯繎3灏).save();
            PartyAttribute pa1 = new PartyAttribute();
            pa1.set("party_id", partyId).set("attr_name", "client_name").set("attr_value", "娓╃敓").save();
            PartyAttribute paPriority = new PartyAttribute();
            paPriority.set("party_id", partyId).set("attr_name", "priority").set("attr_value", "1閲嶈绱ф€).save();
            PartyAttribute pa2 = new PartyAttribute();
            pa2.set("party_id", partyId).set("attr_name", "status").set("attr_value", "姹傜").save();
            PartyAttribute pa3 = new PartyAttribute();
            pa3.set("party_id", partyId).set("attr_name", "region").set("attr_value", "鑰侀娲).save();
            PartyAttribute pa4 = new PartyAttribute();
            pa4.set("party_id", partyId).set("attr_name", "type").set("attr_value", "1鎴).save();

            // 澶栭儴user 鍒涘缓鐨勫鎴
            Party p1 = new Party();
            createDate = Calendar.getInstance().getTime();
            p1.set("party_type", "鍦颁骇瀹㈡埛").set("create_date", createDate).set("creator", "demo").save();
            partyId = p1.getLong("id");
            PartyAttribute p1_pa = new PartyAttribute();
            p1_pa.set("party_id", partyId).set("attr_name", "title").set("attr_value", "姹傚墠灞卞皬鍖).save();
            PartyAttribute p1_pa1 = new PartyAttribute();
            p1_pa1.set("party_id", partyId).set("attr_name", "client_name").set("attr_value", "娓╃敓").save();
            PartyAttribute p1_paPriority = new PartyAttribute();
            p1_paPriority.set("party_id", partyId).set("attr_name", "priority").set("attr_value", "1閲嶈绱ф€).save();
            PartyAttribute p1_pa2 = new PartyAttribute();
            p1_pa2.set("party_id", partyId).set("attr_name", "status").set("attr_value", "姹傝喘").save();
            PartyAttribute p1_pa3 = new PartyAttribute();
            p1_pa3.set("party_id", partyId).set("attr_name", "region").set("attr_value", "鎷卞寳").save();
            PartyAttribute p1_pa4 = new PartyAttribute();
            p1_pa4.set("party_id", partyId).set("attr_name", "type").set("attr_value", "1鎴).save();
            PartyAttribute p1_pa5 = new PartyAttribute();
            p1_pa5.set("party_id", partyId).set("attr_name", "area").set("attr_value", "120").save();
            PartyAttribute p1_pa6 = new PartyAttribute();
            p1_pa6.set("party_id", partyId).set("attr_name", "total").set("attr_value", "200").save();

            newCustomer();
            // 鍏朵粬瀹㈡埛 attributes
            stmt.executeUpdate("insert into party(party_type, create_date, creator) values('鍏朵粬瀹㈡埛', CURRENT_TIMESTAMP(), 'demo');");
            stmt.executeUpdate("insert into party_attribute(party_id, attr_name, attr_value) values(1, 'note', '宸ュ晢娉ㄥ唽');");
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
