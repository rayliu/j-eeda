package config;

import java.sql.Statement;

public class ProfileDataInit {
    public static void initProfile(Statement stmt) {
        try {
            // 客户信息
            stmt.executeUpdate("insert into contact(COMPANY_NAME, ABBR, CONTACT_PERSON, LOCATION, INTRODUCTION, EMAIL, MOBILE, ADDRESS) values('广州广电运通金融电子股份有限公司','广电运通','蒙思哲','440100','（简单说明客户性质和产品情况）','mszhe@grgbanking.com','82188856','广州市萝岗区科林路11号');");
            stmt.executeUpdate("insert into contact(COMPANY_NAME, ABBR, CONTACT_PERSON, LOCATION, INTRODUCTION, EMAIL, MOBILE, ADDRESS) values('湖南长城信息金融设备有限责任公司','长城信息','任学安','430100','国企，我司主要负责运输客户大型ATM机器','renxuean@gwi.com.cn','0731-84932734/13787054674','长沙经济技术开发区东3路5号');");
            stmt.executeUpdate("insert into contact(COMPANY_NAME, ABBR, CONTACT_PERSON, LOCATION, INTRODUCTION, EMAIL, MOBILE, ADDRESS) values('中钞科宝现金处理技术（北京）有限公司','中钞科堡','杨济如','110100','','yangjiru@cbpm-keba.com','010-56627760','北京市海淀区上地创业路20号');");

            stmt.executeUpdate("insert into party(PARTY_TYPE, CONTACT_ID, CREATOR, RECEIPT) values('CUSTOMER','1','demo','广州广电运通金融电子股份有限公司');");
            stmt.executeUpdate("insert into party(PARTY_TYPE, CONTACT_ID, CREATOR, RECEIPT) values('CUSTOMER','2','demo','湖南长城信息金融设备有限责任公司');");
            stmt.executeUpdate("insert into party(PARTY_TYPE, CONTACT_ID, CREATOR, RECEIPT) values('CUSTOMER','3','demo','中钞科宝现金处理技术（北京）有限公司');");

            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('源鸿广州仓','广州市萝岗区宏明路严天商业街11号','2000','自营仓库','广州源鸿物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('济南中转仓','济南市天桥区蓝翔中路2-1号 ','1000','配送供应商仓库','济南骏运展达物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('青岛中转仓','青岛市西元庄物流市场45.46号库','450','配送供应商仓库','济南骏运展达物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('北京中转仓','北京市朝阳区东风乡高庙村68号','1000','配送供应商仓库','北京文成物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('天津中转仓','天津市北辰区外环线与北辰西道交口天津韩家墅海吉星物流园区商业31-32号，仓库4栋1号','450','配送供应商仓库','天津源鸿物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('太原中转仓','太原小店区许坦东街23号','450','配送供应商仓库','山西启程物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('上海中转仓','上海市南翔嘉好路230号','450','配送供应商仓库','上海源鸿物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('石家庄中转仓','石家庄市西二环北路106号','450','配送供应商仓库','石家庄源鸿物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('杭州中转仓','杭州市江干区九堡家苑二区多层公寓三幢三单元11-12号','450','配送供应商仓库','杭州汉唐货运有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('义乌中转仓','义乌市西城一区22幢1号','300','配送供应商仓库','杭州汉唐货运有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('宁波中转仓','宁波市江北区康庄南路','300','配送供应商仓库','宁波天地货运');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('西安中转仓','西安市石化大道北徐十字西口广利丰物流园7号库，门口挂的是徐工集团的牌子 ','450','配送供应商仓库','西安乐三家物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('贵阳中转仓','贵阳市南明区龙洞堡','450','配送供应商仓库','贵州宝彩物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('厦门中转仓','厦门市湖里区电前6路','450','配送供应商仓库','福建畅佳物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('福州中转仓','福州市仓山区战备路580盘屿仓库','450','配送供应商仓库','福州宅急送物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('南宁中转仓','南宁市高新区科园大道70号','600','配送供应商仓库','南宁源鸿物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('海口中转仓','海口市椰海大道苍西村路口进来村1000米，二十六小便利店对面','300','配送供应商仓库','海南嘉诚物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('兰州中转仓','兰州市城关区古城坪40号','300','配送供应商仓库','兰州诚博物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('拉萨中转仓','拉萨市藏大西路','150','配送供应商仓库','拉萨蓝天物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('西宁中转仓','西宁市城东区互助中路92号东方物流','150','配送供应商仓库','西宁东方货运有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('武汉中转仓','武汉市硚口区汉正西物流中心A座24号','150','配送供应商仓库','武汉正旭物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('长沙中转仓','长沙市雨花区高桥物流中心新2号小区C23栋3单元302室','100','配送供应商仓库','长沙源鸿物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('呼市中转仓','内蒙古呼和浩特市110国道503公里处路南广硕物流园南方物流','200','配送供应商仓库','南方物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('成都中转仓','成都市武侯区金花镇金兴北路518号','200','配送供应商仓库','成都千顺物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('郑州中转仓','郑州市航海路七里河美景鸿5-1-2402 ','300','配送供应商仓库','郑州晨曦物流有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('沈阳中转仓','沈阳市和平区三好街118号','300','配送供应商仓库','沈阳一运实业有限责任公司科技商城配送中心');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('长春中转仓','长春市绿园区青年路与荣祥路交汇西行300米金星技校院内联捷物流','300','配送供应商仓库','吉林省联捷物流仓储有限公司');");
            stmt.executeUpdate("insert into warehouse(WAREHOUSE_NAME, WAREHOUSE_ADDRESS, WAREHOUSE_AREA,WAREHOUSE_TYPE,sp_name) values('重庆中转仓','重庆市九龙坡区谢家湾正街3号','200','配送供应商仓库','重庆兵工物流有限公司');");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
