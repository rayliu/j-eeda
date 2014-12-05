package controllers.yh.returnOrder;import java.util.List;import models.TransferOrder;import models.UserLogin;import models.yh.contract.Contract;import models.yh.returnOrder.ReturnOrderFinItem;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;public class ReturnOrderPaymentHelperForDirect {    public ReturnOrderPaymentHelperForDirect() {            }        public static ReturnOrderPaymentHelperForDirect getInstance(){        return new ReturnOrderPaymentHelperForDirect();    }    public void genFinPerCargo(List<UserLogin> users, TransferOrder transferOrder, List<Record> transferOrderItemList, Contract cunstomerContract, String chargeType, Long returnOrderId) {        //发车上必须提供零担的计费方式：按体积，按公斤，按吨        String ltlPriceType = transferOrder.get("ltl_unit_type");                double totalQuantity=0;        String colName="";        //计算总体积        if("perCBM".equals(ltlPriceType)){            colName="volume";        }        if("perKg".equals(ltlPriceType) || "perTon".equals(ltlPriceType)){            colName="sum_weight";        }                for (Record tOrderItemRecord : transferOrderItemList) {        	totalQuantity+=tOrderItemRecord.getDouble(colName);        }        if("perTon".equals(ltlPriceType)){            totalQuantity=totalQuantity/1000;        }        totalQuantity=Double.parseDouble(String.format("%.2f", totalQuantity));                genFinItemPerCBM(users, transferOrder, transferOrderItemList, cunstomerContract, chargeType, ltlPriceType, totalQuantity, returnOrderId);    }    private void genFinItemPerCBM(List<UserLogin> users, TransferOrder transferOrder, List<Record> transferOrderItemList, Contract spContract,            String chargeType, String ltlPriceType, double totalQuantity, Long returnOrderId) {                String sqlStart="select amount*"+totalQuantity+" as amount, fin_item_id from contract_item where contract_id ="+spContract.getLong("id");        String sqlEnd=" and priceType='"+chargeType+"' and ltlunittype='"+ltlPriceType                + "' and ifnull(amountfrom, 0)<"+totalQuantity+" and "+totalQuantity + "<=ifnull(amountto, 10000000)";        Record contractFinItem = Db                .findFirst(sqlStart                        + "  and from_id = '" + transferOrder.get("route_from")                        + "' and to_id = '" + transferOrder.get("route_to")+"' "                        + sqlEnd);        if (contractFinItem != null) {            genFinItem(users, transferOrder, null, contractFinItem, returnOrderId);        }else{            contractFinItem = Db                    .findFirst(sqlStart                            +" and to_id = '" + transferOrder.get("route_to") + "' "                            + sqlEnd);            if (contractFinItem != null) {                genFinItem(users, transferOrder, null, contractFinItem, returnOrderId);            }        }    }        private void genFinItem(List<UserLogin> users, TransferOrder transferOrder, Record tOrderItemRecord, Record contractFinItem, Long returnOrderId) {		java.util.Date utilDate = new java.util.Date();		java.sql.Timestamp now = new java.sql.Timestamp(utilDate.getTime());		ReturnOrderFinItem returnOrderFinItem = new ReturnOrderFinItem();		returnOrderFinItem.set("fin_item_id", contractFinItem.get("fin_item_id"));		returnOrderFinItem.set("delivery_order_id", transferOrder.getLong("id"));		returnOrderFinItem.set("return_order_id", returnOrderId);		returnOrderFinItem.set("status", "未完成");		returnOrderFinItem.set("fin_type", "charge");// 类型是应收		returnOrderFinItem.set("contract_id", contractFinItem.get("contract_id"));// 类型是应收		returnOrderFinItem.set("creator", users.get(0).get("id"));		returnOrderFinItem.set("create_date", now);		returnOrderFinItem.set("create_name", "system");		returnOrderFinItem.set("amount", contractFinItem.getDouble("amount")); 				returnOrderFinItem.save();	}}