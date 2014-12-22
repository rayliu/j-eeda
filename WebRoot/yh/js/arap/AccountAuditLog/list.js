
$(document).ready(function() {

    $('#menu_finance').addClass('active').find('ul').addClass('in');
	//datatable, 动态处理
    var accountAuditLogTable = $('#accountAuditLog-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/accountAuditLog/list",
        "aoColumns": [   
            {"mDataProp":"CREATE_DATE"},
            {"mDataProp":null,
	            "fnRender": function(obj) {
	            	return '开票记录单';
	            }
            },
            {"mDataProp":"INVOICE_ORDER_NO"},
            {"mDataProp":null,
	            "fnRender": function(obj) {
	            	var str = "";
	            	if(obj.aData.PAYMENT_METHOD == "transfers"){
	            		str = "转账";
	            	}else{
	            		str = "现金";
	            	}
	            	return str;
	            }
            },
            {"mDataProp":null},
            {"mDataProp":"REMARK"},           
            {"mDataProp":"USER_NAME"},        	
            {"mDataProp":"CREATE_DATE"}
        ]      
    });
    
    var accountTable = $('#account-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, 
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/accountAuditLog/accountList",
    	"aoColumns": [   
	        { "mDataProp": null, "sWidth":"30px",
              "fnRender": function(obj) {
                return '<input type="checkbox" name="order_check_box" value="'+obj.aData.ID+'">';
              }
            },
	        {"mDataProp":"BANK_PERSON"},
	        {"mDataProp":null},
	        {"mDataProp":"AMOUNT"},        	
	        {"mDataProp":null},
	        {"mDataProp":"REMARK"}           
	     ]      
    });    
} );