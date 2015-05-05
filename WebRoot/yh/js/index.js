$(document).ready(function() {
	document.title = '系统监控 | '+document.title;
    
	console.log("当前选择："+$("input[name='optionsRadiosInline'][checked]").val());
	
	var transferOrderTypeTbody = $('#transferOrderTypeTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"bServerSide": true,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/transferOrder/findTransferOrderType?pointInTime="+$("input[name='optionsRadiosInline'][checked]").val(),
        "aoColumns": [
            { "mDataProp": "ORDER_NO","sWidth":"100px",
            	"fnRender":function(obj){
            		return "<a href='/transferOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            	}},
            {"mDataProp":null, "sWidth":"150px",
            	"fnRender": function(obj) {
            		return obj.aData.ROUTE_FROM + " —— " + obj.aData.ROUTE_TO;
            	}
            },
            { "mDataProp": "STATUS", "sWidth":"100px"},
            { "mDataProp": "CREATE_STAMP", "sWidth":"150px"},
            { "mDataProp": null,"sWidth":"100px"},
        ]
    });
	
	var deliveryOrderTypeTbody = $('#deliveryOrderTypeTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"bServerSide": true,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/delivery/findDeliveryOrderType?pointInTime="+$("input[name='optionsRadiosInline'][checked]").val(),
        "aoColumns": [
            { "mDataProp": "ORDER_NO","sWidth":"100px",
            	"fnRender":function(obj){
            		return  "<a href='/delivery/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            	}},
            {"mDataProp":null, "sWidth":"150px",
            	"fnRender": function(obj) {
            		return obj.aData.ROUTE_FROM + " —— " + obj.aData.ROUTE_TO;
            	}
            },
            { "mDataProp": "STATUS", "sWidth":"100px"},
            { "mDataProp": "CREATE_STAMP", "sWidth":"150px"},
            { "mDataProp": null,"sWidth":"100px"},
        ]
    });
	
	var returnOrderTypeTbody = $('#returnOrderTypeTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"bServerSide": true,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/returnOrder/findReturnOrderType?pointInTime="+$("input[name='optionsRadiosInline'][checked]").val(),
        "aoColumns": [
            { "mDataProp": "ORDER_NO","sWidth":"100px",
            	"fnRender":function(obj){
            		return "<a href='/returnOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            	}},
            {"mDataProp":null, "sWidth":"150px",
            	"fnRender": function(obj) {
            		return obj.aData.ROUTE_FROM + " —— " + obj.aData.ROUTE_TO
            	}
            },
            { "mDataProp": "TRANSACTION_STATUS", "sWidth":"100px"},
            { "mDataProp": "CREATE_DATE", "sWidth":"150px"},
            { "mDataProp": "AMOUNT","sWidth":"100px"},
        ]
    });
	$("#btn,#clbtn").on('click',function(){
		$("#exampleModal").css("display","none");
	});
	
	var findAllCount = function(pointInTime){
		//查询系统单据数量合计
		$.get('/statusReport/searchOrderCount', {pointInTime:pointInTime}, function(data){
			$("#transferOrderTotal").empty().text(data.transferOrderTotal);
			$("#pickupTotal").empty().text(data.pickupTotal);
			$("#departTotal").empty().text(data.departTotal);
			$("#deliveryTotal").empty().text(data.deliveryTotal);
			$("#returnTotal").empty().text(data.returnTotal);
			$("#insuranceTotal").empty().text(data.insuranceTotal);
		});
	};
	findAllCount($("input[name='optionsRadiosInline'][checked]").val());
	//切换查询
	$("input[name='optionsRadiosInline']").change(function() {
		findAllCount($(this).val());
		transferOrderTypeTbody.fnSettings().sAjaxSource = "/transferOrder/findTransferOrderType?pointInTime="+$(this).val();
		transferOrderTypeTbody.fnDraw(); 
		deliveryOrderTypeTbody.fnSettings().sAjaxSource = "/delivery/findDeliveryOrderType?pointInTime="+$(this).val();
		deliveryOrderTypeTbody.fnDraw();
		returnOrderTypeTbody.fnSettings().sAjaxSource = "/returnOrder/findReturnOrderType?pointInTime="+$(this).val();
		returnOrderTypeTbody.fnDraw();
	}); 
	
	
	
});