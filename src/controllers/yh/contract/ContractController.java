package controllers.yh.contract;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.Fin_item;
import models.yh.contract.Contract;
import models.yh.contract.ContractItem;
import models.yh.profile.Contact;

import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.LoginUserController;

public class ContractController extends Controller {

    private Logger logger = Logger.getLogger(ContractController.class);

    // in config route已经将路径默认设置为/yh
    // me.add("/yh", controllers.yh.AppController.class, "/yh");
    public void index() {
        HttpServletRequest re = getRequest();
        String url = re.getRequestURI();
        logger.debug("URI:" + url);
        if (url.equals("/yh/customerContract")) {
            setAttr("contractType", "CUSTOMER");
            if (LoginUserController.isAuthenticated(this))
                render("contract/ContractList.html");
        }
        if (url.equals("/yh/deliverySpContract")) {
            setAttr("contractType", "DELIVERY_SERVICE_PROVIDER");
            if (LoginUserController.isAuthenticated(this))
                render("contract/ContractList.html");
        }
        if (url.equals("/yh/spContract")) {
            setAttr("contractType", "SERVICE_PROVIDER");
            if (LoginUserController.isAuthenticated(this))
                render("contract/ContractList.html");
        }

    }

    public void companyNameList() {
        String type = getPara("type");
        String sql = "select c1.company_name as company  from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type = '"
                + type + "'";
        List<Record> companyNameList = Db.find(sql);
        renderJson(companyNameList);
    }

    // 客户合同列表
    public void customerList() {
        String contractName_filter = getPara("contractName_filter");
        String contactPerson_filter = getPara("contactPerson_filter");
        String periodFrom_filter = getPara("periodFrom_filter");
        String companyName_filter = getPara("companyName_filter");
        String phone_filter = getPara("phone_filter");
        String periodTo_filter = getPara("periodTo_filter");

        Map orderMap = new HashMap();
        if (contractName_filter == null && contactPerson_filter == null && periodFrom_filter == null && companyName_filter == null
                && phone_filter == null && periodTo_filter == null) {
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='CUSTOMER'";
            System.out.println(sql);
            Record rec = Db.findFirst(sql + totalWhere);
            long total = rec.getLong("total");
            logger.debug("total records:" + total);

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='CUSTOMER'"
                            + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", total);
            orderMap.put("iTotalDisplayRecords", total);
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        } else {
            /*
             * if (periodFrom_filter == null || "".equals(periodFrom_filter)) {
             * periodFrom_filter = "1-1-1"; } if (periodTo_filter == null ||
             * "".equals(periodTo_filter)) { periodTo_filter = "9999-12-31"; }
             */
            String sLimit = "";
            String pageIndex = getPara("sEcho");
            if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
                sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
            }
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='CUSTOMER'";
            System.out.println(sql);
            Record rec = Db.findFirst(sql + totalWhere);
            long total = rec.getLong("total");
            logger.debug("total records:" + total);
            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='CUSTOMER' "
                            + "and c.name like '%"
                            + contractName_filter
                            + "%' and ifnull(c1.contact_person,'') like '%"
                            + contactPerson_filter
                            + "%' and ifnull(c1.company_name,'') like '%"
                            + companyName_filter
                            + "%' and ifnull(c1.mobile,'') like '%"
                            + phone_filter
                            + "%' and c.period_from like '%"
                            + periodFrom_filter
                            + "%' and c.period_to like '%" + periodTo_filter + "%'" + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", total);
            orderMap.put("iTotalDisplayRecords", total);
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        }
    }

    // 配送供应商合同列表
    public void deliveryspList() {
        String contractName_filter = getPara("contractName_filter");
        String contactPerson_filter = getPara("contactPerson_filter");
        String periodFrom_filter = getPara("periodFrom_filter");
        String companyName_filter = getPara("companyName_filter");
        String phone_filter = getPara("phone_filter");
        String periodTo_filter = getPara("periodTo_filter");

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        if (contractName_filter == null && contactPerson_filter == null && periodFrom_filter == null && companyName_filter == null
                && phone_filter == null && periodTo_filter == null) {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='DELIVERY_SERVICE_PROVIDER'";
            System.out.println(sql);
            Record rec = Db.findFirst(sql + totalWhere);
            long total = rec.getLong("total");
            logger.debug("total records:" + total);

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='DELIVERY_SERVICE_PROVIDER'"
                            + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", total);
            orderMap.put("iTotalDisplayRecords", total);
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        } else {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='DELIVERY_SERVICE_PROVIDER'";
            System.out.println(sql);
            Record rec = Db.findFirst(sql + totalWhere);
            long total = rec.getLong("total");
            logger.debug("total records:" + total);

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='DELIVERY_SERVICE_PROVIDER'"
                            + "and c.name like '%"
                            + contractName_filter
                            + "%' and ifnull(c1.contact_person,'') like '%"
                            + contactPerson_filter
                            + "%' and ifnull(c1.company_name,'') like '%"
                            + companyName_filter
                            + "%' and ifnull(c1.mobile,'') like '%"
                            + phone_filter
                            + "%' and c.period_from like '%"
                            + periodFrom_filter
                            + "%' and c.period_to like '%" + periodTo_filter + "%'" + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", total);
            orderMap.put("iTotalDisplayRecords", total);
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        }

    }

    // 干线供应商合同列表
    public void spList() {
        String contractName_filter = getPara("contractName_filter");
        String contactPerson_filter = getPara("contactPerson_filter");
        String periodFrom_filter = getPara("periodFrom_filter");
        String companyName_filter = getPara("companyName_filter");
        String phone_filter = getPara("phone_filter");
        String periodTo_filter = getPara("periodTo_filter");

        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        if (contractName_filter == null && contactPerson_filter == null && periodFrom_filter == null && companyName_filter == null
                && phone_filter == null && periodTo_filter == null) {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='SERVICE_PROVIDER'";
            System.out.println(sql);
            Record rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='SERVICE_PROVIDER'"
                            + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        } else {
            // 获取总条数
            String totalWhere = "";
            String sql = "select count(1) total from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='SERVICE_PROVIDER'";
            System.out.println(sql);
            Record rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            List<Record> orders = Db
                    .find("select *,c.id as cid from contract c,party p,contact c1 where c.party_id= p.id and p.contact_id = c1.id and c.type='SERVICE_PROVIDER' "
                            + "and c.name like '%"
                            + contractName_filter
                            + "%' and ifnull(c1.contact_person,'') like '%"
                            + contactPerson_filter
                            + "%' and ifnull(c1.company_name,'') like '%"
                            + companyName_filter
                            + "%' and ifnull(c1.mobile,'') like '%"
                            + phone_filter
                            + "%' and c.period_from like '%"
                            + periodFrom_filter
                            + "%' and c.period_to like '%" + periodTo_filter + "%'" + sLimit);
            orderMap.put("sEcho", pageIndex);
            orderMap.put("iTotalRecords", rec.getLong("total"));
            orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);
            renderJson(orderMap);
        }

    }

    public void add() {
        HttpServletRequest re = getRequest();
        String url = re.getRequestURI();
        logger.debug("URI:" + url);
        if (url.equals("/yh/customerContract/add")) {
            setAttr("contractType", "CUSTOMER");
            setAttr("saveOK", false);
            List<Fin_item> finItemList = Fin_item.dao.find("select * from fin_item where type='应付'");
            setAttr("finItemList", finItemList);
            if (LoginUserController.isAuthenticated(this))
                render("/yh/contract/ContractEdit.html");
        }
        if (url.equals("/yh/deliverySpContract/add")) {
            setAttr("contractType", "DELIVERY_SERVICE_PROVIDER");
            setAttr("saveOK", false);
            List<Fin_item> finItemList = Fin_item.dao.find("select * from fin_item where type='应付'");
            setAttr("finItemList", finItemList);
            if (LoginUserController.isAuthenticated(this))
                render("/yh/contract/ContractEdit.html");
        }
        if (url.equals("/yh/spContract/add")) {
            setAttr("contractType", "SERVICE_PROVIDER");
            setAttr("saveOK", false);
            List<Fin_item> finItemList = Fin_item.dao.find("select * from fin_item where type='应付'");
            setAttr("finItemList", finItemList);
            if (LoginUserController.isAuthenticated(this))
                render("/yh/contract/ContractEdit.html");
        }
        setAttr("saveOK", false);

    }

    public void edit() {
        String id = getPara();
        if (id != null) {
            Contract contract = Contract.dao.findById(id);
            Contact contact = Contact.dao.findFirst("select * from party p left join contact c on p.contact_id =c.id where p.id ='"
                    + contract.get("party_id") + "'");
            System.out.println(contact);
            setAttr("c", contact);
            setAttr("ul", contract);

            List<Fin_item> finItemList = Fin_item.dao.find("select * from fin_item where type='应付'");
            setAttr("finItemList", finItemList);
        }
        if (LoginUserController.isAuthenticated(this))
            render("/yh/contract/ContractEdit.html");
    }

    public void save() {
        String id = getPara("contractId");
        Date createDate = Calendar.getInstance().getTime();
        if (id != "") {
            Contract c = Contract.dao.findById(id);
        }
        Record c = new Record();
        c.set("name", getPara("contract_name"));
        c.set("party_id", getParaToInt("partyid"));
        c.set("period_from", getPara("period_from"));
        c.set("period_to", getPara("period_to"));
        c.set("remark", getPara("remark"));
        if (id != "") {
            logger.debug("update....");
            c.set("id", id);
            c.set("type", getPara("type3"));
            Db.update("contract", c);
        } else {
            logger.debug("insert....");
            c.set("type", getPara("type2"));
            Db.save("contract", c);
        }
        renderJson(c.get("id"));
    }

    public void delete() {
        String id = getPara();
        if (id != null) {
            Db.deleteById("contract", id);
        }
        if (LoginUserController.isAuthenticated(this))
            redirect("/yh/customerContract");
    }

    public void delete2() {
        String id = getPara();
        if (id != null) {
            Db.deleteById("contract", id);
        }
        if (LoginUserController.isAuthenticated(this))
            redirect("/yh/spContract");
    }

    // 列出客户公司名称
    public void search() {
        String locationName = getPara("locationName");
        // 不能查所有
        List<Record> locationList = Collections.EMPTY_LIST;
        if (locationName.trim().length() > 0) {
            locationList = Db
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER' and c.company_name like ?",
                            "%" + locationName + "%");

        } else {
            locationList = Db.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'CUSTOMER'");

        }
        renderJson(locationList);
    }

    // 列出供应商公司名称
    public void search2() {
        String locationName = getPara("locationName");
        // 不能查所有
        if (locationName.trim().length() > 0) {
            List<Record> locationList = Db
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'SERVICE_PROVIDER' and c.company_name like ?",
                            "%" + locationName + "%");
            renderJson(locationList);
        } else {
            List<Record> locationList = Db
                    .find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = 'SERVICE_PROVIDER'");
            renderJson(locationList);
        }
    }

    // 合同运价（计件）
    public void routeEdit() {
        String contractId = getPara("routId");
        System.out.println(contractId);
        if (contractId.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        String sql = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        if (contractId != null && contractId.length() > 0) {
            sql = "select count(1) total from contract_item c where c.contract_id = " + contractId + " and PRICETYPE ='perUnit'";
        }

        System.out.println(sql);
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = null;
        if (contractId != null && contractId.length() > 0) {
            orders = Db
                    .find("select c.*, fi.name as fin_item_name , p.item_name from  contract_item c left join product p on c.product_id = p.id left join fin_item fi on c.fin_item_id = fi.id where c.contract_id = "
                            + contractId + " and PRICETYPE ='perUnit' order by id desc" + sLimit);
        }
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    // 合同运价（整车）
    public void routeEdit2() {
        String contractId = getPara("routId");
        System.out.println(contractId);
        if (contractId.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        String sql = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        if (contractId != null && contractId.length() > 0) {
            sql = "select count(1) total from contract_item c where c.contract_id = " + contractId + " and PRICETYPE ='perCar'";
        }

        System.out.println(sql);
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = null;
        if (contractId != null && contractId.length() > 0) {
            orders = Db
                    .find("select c.*, fi.name as fin_item_name, p.item_name from  contract_item c left join product p on c.product_id = p.id left join fin_item fi on c.fin_item_id = fi.id where c.contract_id = "
                            + contractId + " and PRICETYPE ='perCar'  order by id desc" + sLimit);
        }
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    // 合同运价（零担）
    public void routeEdit3() {
        String contractId = getPara("routId");
        System.out.println(contractId);
        if (contractId.equals("")) {
            Map orderMap = new HashMap();
            orderMap.put("sEcho", 0);
            orderMap.put("iTotalRecords", 0);
            orderMap.put("iTotalDisplayRecords", 0);
            orderMap.put("aaData", null);
            renderJson(orderMap);
            return;
        }

        String sLimit = "";
        String sql = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        if (contractId != null && contractId.length() > 0) {
            sql = "select count(1) total from contract_item c where c.contract_id = " + contractId + " and pricetype ='perCargo'";
        }

        System.out.println(sql);
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = null;
        if (contractId != null && contractId.length() > 0) {
            orders = Db
                    .find("select c.*, fi.name as fin_item_name, p.item_name from  contract_item c left join product p on c.product_id = p.id left join fin_item fi on c.fin_item_id = fi.id where c.contract_id = "
                            + contractId + " and pricetype ='perCargo'  order by id desc" + sLimit);
        }
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    public void routeAdd() {
        ContractItem item = new ContractItem();
        String contractId = getPara("routeContractId");
        String routeItemId = getPara("routeItemId");
        String priceType = getPara("priceType");
        String mbProvinceFrom = getPara("hideProvinceFrom");
        String cmbCityFrom = getPara("hideCityFrom");
        String cmbAreaFrom = getPara("hideDistrictFrom");
        String mbProvinceTo = getPara("hideProvinceTo");
        String cmbCityTo = getPara("hideCityTo");
        String cmbAreaTo = getPara("hideDistrictTo");
        
        StringBuffer fromName = new StringBuffer(); 
        fromName.append(mbProvinceFrom+" ");
        fromName.append(cmbCityFrom+" ");
        fromName.append(cmbAreaFrom);
        
        StringBuffer toName = new StringBuffer(); 
        toName.append(mbProvinceTo+" ");
        toName.append(cmbCityTo+" ");
        toName.append(cmbAreaTo+" ");
        
        System.out.println(">>>>>>>>>>>>>>>>>fromName:"+fromName+",toName:"+toName+"<<<<<<<<<<<<<<<<<<");
        // 判断合同干线是否存在
        item.set("contract_id", contractId).set("fin_item_id", getPara("fin_item")).set("pricetype", getPara("priceType"))
                .set("from_id", getPara("from_id")).set("location_from", fromName.toString()).set("to_id", getPara("to_id"))
                .set("location_to", toName.toString()).set("amount", getPara("price")).set("dayfrom", getPara("day"))
                .set("dayto", getPara("day2"));
        if (getPara("productId").equals("")) {
            item.set("product_id", null);
        } else {
            item.set("product_id", getPara("productId"));
        }
        if (routeItemId != "") {

            if (priceType.equals("perUnit")) {
                item.set("id", getPara("routeItemId")).set("cartype", null).set("carlength", null).set("fin_item_id", getPara("fin_item"))
                        .set("ltlUnitType", null);
                item.set("unit", getPara("unit2"));

                item.update();
                renderJson("{\"success\":true}");
            }
            if (priceType.equals("perCar")) {
                item.set("id", getPara("routeItemId"));
                item.set("cartype", getPara("carType2")).set("carlength", getPara("carLength2")).set("fin_item_id", getPara("fin_item"))
                        .set("ltlUnitType", null);

                item.update();
                renderJson("{\"success\":true}");
            }
            if (priceType.equals("perCargo")) {
                item.set("id", getPara("routeItemId")).set("amountFrom", getPara("amountFrom")).set("fin_item_id", getPara("fin_item"))
                        .set("amountTo", getPara("amountTo"));

                item.set("ltlUnitType", getPara("ltlUnitType")).set("cartype", null).set("carlength", null);
                item.update();
                renderJson("{\"success\":true}");
            }
        } else {
            if (priceType.equals("perUnit")) {
                item.set("unit", getPara("unit2"));
                item.save();
                renderJson("{\"success\":true}");
            }
            if (priceType.equals("perCar")) {
                item.set("cartype", getPara("carType2")).set("carlength", getPara("carLength2"));
                item.save();
                renderJson("{\"success\":true}");
            }
            if (priceType.equals("perCargo")) {
                item.set("ltlUnitType", getPara("ltlUnitType")).set("amountFrom", getPara("amountFrom"))
                        .set("amountTo", getPara("amountTo"));
                item.save();
                renderJson("{\"success\":true}");
            }
        }
    }

    /*
     * // 通过输入起点和终点判断干线id public void searchRoute() { String fromName =
     * getPara("fromName"); String toName = getPara("toName");
     * System.out.println(fromName); System.out.println(toName); List<Route>
     * routeId = Route.dao
     * .find("select id as rId from CONTRACT_ITEM where location_from like '%" +
     * fromName + "%' and location_to like '%" + toName + "%'");
     * System.out.println(routeId); renderJson(routeId); }
     */

    public void contractRouteEdit() {
        String id = getPara();

        String contractId = getPara("contractId");
        System.out.println(id);
        // Route route = Route.dao.findById(id);
        Contract contract = Contract.dao.findById(contractId);
        List<Record> list = null;
        if (contract.get("party_id") != null) {
            list = Db
                    .find("select c.*,p.id as pid,p.item_name from contract_item c left join product p on p.id =c.product_id where c.contract_id ='"
                            + contractId + "' and c.id = '" + id + "'");
        } else {
            list = Db.find("select c.*,'null' as item_name from contract_item c where c.contract_id ='" + contractId + "' and c.id = '"
                    + id + "'");

        }
        renderJson(list);
    }

    public void routeDelete() {
        String id = getPara();
        if (id != null) {
            Db.deleteById("contract_item", id);
        }
        renderJson("{\"success\":true}");
    }

    // 查找产品名
    public void searchItemName() {
        String input = getPara("input");
        String customerId = getPara("customerId");
        List<Record> locationList = Collections.EMPTY_LIST;
        if (input.trim().length() > 0) {
            locationList = Db.find("select * from product where category_id in (select id from category where customer_id = " + customerId
                    + ") and item_name like '%" + input + "%' limit 0,10");
        } else {
            locationList = Db.find("select * from product where category_id in (select id from category where customer_id = " + customerId
                    + ")");
        }
        renderJson(locationList);
    }

    // input控件列出城市列表
    public void searchlocation() {
        String locationName = getPara("locationName");

        // 不能查所有
        if (locationName.trim().length() > 0) {

            locationName = locationName.replaceAll("'", "''");// 转义，地点查询中经常出现单引号
            List<Record> locationList = Db.find("select * from location where name like '%" + locationName + "%' or code like '%"
                    + locationName + "%' or pcode like '%" + locationName + "%' order by id limit 10");
            renderJson(locationList);
        } else {
            renderJson("");
        }
    }
}
