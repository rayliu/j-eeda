package models.yh.carmanage;

import com.jfinal.plugin.activerecord.Model;
@SuppressWarnings("serial")
public class CarSummaryOrder extends Model<CarSummaryOrder> {
	public static final String CAR_SUMMARY_SYSTEM_NEW = "new";
	public static final String CAR_SUMMARY_SYSTEM_CHECKED = "checked";
	public static final String CAR_SUMMARY_SYSTEM_REVOCATION = "revocation";
	public static final String CAR_SUMMARY_SYSTEM_REIMBURSEMENT = "reimbursement";
	
	public static final CarSummaryOrder dao = new CarSummaryOrder();
}
