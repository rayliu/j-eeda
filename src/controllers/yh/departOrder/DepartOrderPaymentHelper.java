package controllers.yh.departOrder;import java.math.BigDecimal;import java.util.List;import org.apache.shiro.SecurityUtils;import org.apache.shiro.subject.Subject;import models.DepartOrder;import models.DepartOrderFinItem;import models.TransferOrder;import models.UserLogin;import models.yh.contract.Contract;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;public class DepartOrderPaymentHelper {	Subject currentUser = SecurityUtils.getSubject();    public DepartOrderPaymentHelper() {            }        public static DepartOrderPaymentHelper getInstance(){        return new DepartOrderPaymentHelper();    }    public void genFinPerCargo(DepartOrder departOrder, List<Record> transferOrderItemList, Contract spContract,            String chargeType) {        //发车上必须提供零担的计费方式：按体积，按公斤，按吨        String ltlPriceType = departOrder.get("ltl_price_type");                double totalQuantity=0;        String colName="";        //计算总体积        if("perCBM".equals(ltlPriceType)){            colName="volume";        }        if("perKg".equals(ltlPriceType) || "perTon".equals(ltlPriceType)){            colName="sum_weight";        }        boolean isFinContract = true;        DepartOrderController doc = new DepartOrderController();        for (Record tOrderItemRecord : transferOrderItemList) {        	TransferOrder transferOrder = TransferOrder.dao.findFirst("select * from transfer_order where id = ?",tOrderItemRecord.get("order_id"));        	Boolean isTrue = transferOrder.get("no_contract_cost");    		if(isTrue){    			isFinContract=false;    		}else{    			totalQuantity+=tOrderItemRecord.getDouble(colName);    		}    		        	        }                doc.getFinNoContractCost(departOrder);        if("perTon".equals(ltlPriceType)){            totalQuantity=totalQuantity/1000;        }        //统一向上取整，不足一 立方(/公斤/吨)，按一 立方(/公斤/吨) 算        //totalQuantity=Math.ceil(totalQuantity);        totalQuantity=Double.parseDouble(String.format("%.2f", totalQuantity));        if(isFinContract){        	genFinItemPerCBM(departOrder, transferOrderItemList, spContract, chargeType, ltlPriceType, totalQuantity);        }            }    private void genFinItemPerCBM(DepartOrder departOrder, List<Record> transferOrderItemList, Contract spContract,            String chargeType, String ltlPriceType, double totalQuantity) {                String sqlStart="select amount*"+totalQuantity+" as amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id");        String sqlEnd=" and priceType='"+chargeType+"' and ltlunittype='"+ltlPriceType                + "' and ifnull(amountfrom, 0)<"+totalQuantity+" and "+totalQuantity + "<=ifnull(amountto, 10000000)";        Record contractFinItem = Db                .findFirst(sqlStart                        + "  and from_id = '" + departOrder.get("route_from")                        + "' and to_id = '" + departOrder.get("route_to")+"' "                        + sqlEnd);        if (contractFinItem != null) {            genFinItem(departOrder, transferOrderItemList, contractFinItem);        }else{            contractFinItem = Db                    .findFirst(sqlStart                            +" and to_id = '" + departOrder.get("route_to") + "' "                            + sqlEnd);            if (contractFinItem != null) {                genFinItem(departOrder, transferOrderItemList, contractFinItem);            }        }    }        private void genFinItem(DepartOrder departOrder, List<Record> tOrderItemRecord, Record contractFinItem) {		String name = (String) currentUser.getPrincipal();		UserLogin users = UserLogin.dao.findFirst("select * from user_login where user_name='" + name + "'");    	java.util.Date utilDate = new java.util.Date();        java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());        double money =contractFinItem.getDouble("amount");        BigDecimal bg = new BigDecimal(money);        double amountDouble = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();        DepartOrderFinItem departOrderFinItem = new DepartOrderFinItem();        departOrderFinItem.set("fin_item_id", contractFinItem.get("fin_item_id"));        departOrderFinItem.set("amount", amountDouble);        departOrderFinItem.set("depart_order_id", departOrder.getLong("id"));        departOrderFinItem.set("status", "未完成");        departOrderFinItem.set("creator", users.get("id"));        departOrderFinItem.set("create_date", now);        departOrderFinItem.set("create_name", "system");        //departOrderFinItem.set("transfer_order_id", tOrderItemRecord.get(0).get("order_id"));        //departOrderFinItem.set("transfer_order_item_id", tOrderItemRecord.get(0).get("id"));        departOrderFinItem.set("cost_source", "合同费用");        departOrderFinItem.save();    }}