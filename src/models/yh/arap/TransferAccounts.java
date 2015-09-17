package models.yh.arap;

import com.jfinal.plugin.activerecord.Model;

public class TransferAccounts extends Model<TransferAccounts> {
	public static final String ORDER_STATUS_NEW = "new";
	public static final String ORDER_STATUS_CONFIRM = "confirm";
	public static final TransferAccounts dao = new TransferAccounts();
}
