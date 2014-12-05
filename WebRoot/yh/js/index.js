$(document).ready(function() {
	
	$('#transferOrderTypeTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"bServerSide": true,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/transferOrder/findTransferOrderType",
        "aoColumns": [
            { "mDataProp": "ORDER_NO"},
            {"mDataProp":null, "sWidth":"150px",
            	"fnRender": function(obj) {
            		return obj.aData.ROUTE_FROM + " —— " + obj.aData.ROUTE_TO
            	}
            },
            { "mDataProp": "STATUS", "sWidth":"100px"},
            { "mDataProp": "CREATE_STAMP", "sWidth":"150px"},
            { "mDataProp": null},
        ]
    });
	
	$('#deliveryOrderTypeTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"bServerSide": true,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/delivery/findDeliveryOrderType",
        "aoColumns": [
            { "mDataProp": "ORDER_NO"},
            {"mDataProp":null, "sWidth":"150px",
            	"fnRender": function(obj) {
            		return obj.aData.ROUTE_FROM + " —— " + obj.aData.ROUTE_TO
            	}
            },
            { "mDataProp": "STATUS", "sWidth":"100px"},
            { "mDataProp": "CREATE_STAMP", "sWidth":"150px"},
            { "mDataProp": null},
        ]
    });
	
	
	
	
	
	
	
	
	
	
	
	
});