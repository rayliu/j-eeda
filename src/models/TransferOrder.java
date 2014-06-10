package models;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;
@SuppressWarnings("serial")
public class TransferOrder extends Model<TransferOrder> {
	
	public static final TransferOrder dao = new TransferOrder();
	
	private List<TransferOrderItem> itemList= new ArrayList<TransferOrderItem>();
}
