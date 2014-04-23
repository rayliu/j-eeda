package models;

import com.jfinal.plugin.activerecord.Model;
@SuppressWarnings("serial")
public class TransferOrder extends Model<TransferOrder> {
	// 货品属性
	// ATM
	public static final String CARGO_NATURE_ATM = "ATM";
	// 普通货品
	public static final String CARGO_NATURE_COMMON = "COMMON";
	// 损坏货品
	public static final String CARGO_NATURE_DAMAGED = "DAMAGED";
	
	// 提货方式
	// 干线供应商自提
	public static final String PICKUP_MODE_SERVICE_PROVIDER = "SERVICE_PROVIDER";
	// 公司自提
	public static final String PICKUP_MODE_COMPANY = "COMPANY";
	// 外包供应商提货
	public static final String PICKUP_MODE_OUTSOURCER_SERVICE_PROVIDER = "OUTSOURCER_SERVICE_PROVIDER";
	
	// 到达方式
	// 货品直送
	public static final String ARRIVAL_MODE_DIRECT = "DIRECT";
	// 入中转仓
	public static final String ARRIVAL_MODE_TRANSIT_WAREHOUSE = "TRANSIT_WAREHOUSE";
	
	public static final TransferOrder dao = new TransferOrder();
}
