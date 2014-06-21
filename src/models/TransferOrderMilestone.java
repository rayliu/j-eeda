package models;

import com.jfinal.plugin.activerecord.Model;
@SuppressWarnings("serial")
public class TransferOrderMilestone extends Model<TransferOrderMilestone> {
    public static final String TYPE_TRANSFER_ORDER_MILESTONE = "TRANSFERORDERMILESTONE";
    public static final String TYPE_PICKUP_ORDER_MILESTONE = "PICKUPORDERMILESTONE";
	
	public static final TransferOrderMilestone dao = new TransferOrderMilestone();
}
