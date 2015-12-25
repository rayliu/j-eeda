$(document).ready(function () {
    document.title = '管理看板 | ' + document.title;

    console.log("当前选择：" + $("input[name='optionsRadiosInline'][checked]").val());

    var todoListTable = $('#todo_list_table').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": true,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/getTodoList",
        "aoColumns": [
            { "mDataProp": "ORDER_NO", "sWidth":"100px",
            	"fnRender":function(obj){
            		return  "<a href='/delivery/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            	}},
            {"mDataProp":null,
            	"fnRender": function(obj) {
            		return " 该配送单业务要求配送时间为：" + obj.aData.BUSINESS_STAMP  + ", 请安排配送。序列号："+obj.aData.SERIAL_NO;
            	}
            }
        ]
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
		
		// deliveryOrderTypeTbody.fnSettings().sAjaxSource = "/delivery/findDeliveryOrderType?pointInTime="+$(this).val();
		// deliveryOrderTypeTbody.fnDraw();
	}); 
	
	
	
});