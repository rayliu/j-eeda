$(document).ready(function() {
	$('#menu_charge').addClass('active').find('ul').addClass('in');
	
	var saveChargeCheckOrder = function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeCheckOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/yh/chargeCheckOrder/save', $("#chargeCheckOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#chargeInvoiceOrderId").val(data.ID);
			  	$("#style").show();
			  	$("#departureConfirmationBtn").attr("disabled", false);
			}else{
				alert('数据保存失败。');
			}
		},'json');
	};
    
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveChargeCheckOrderBtn").click(function(e){
 		saveChargeCheckOrder(e);
	});
	
	$("#chargeCheckOrderItem").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeCheckOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/yh/chargeCheckOrder/save', $("#chargeCheckOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#chargeInvoiceOrderId").val(data.ID);
			  	$("#style").show();
			  	$("#departureConfirmationBtn").attr("disabled", false);
	
			    var chargeInvoiceOrderId = $("#chargeInvoiceOrderId").val();
				chargeCheckOrderTable.fnSettings().sAjaxSource = "/yh/chargeCheckOrder/returnOrderList?chargeInvoiceOrderId="+chargeInvoiceOrderId;
				chargeCheckOrderTable.fnDraw(); 
			}else{
				alert('数据保存失败。');
			}
		},'json');
	});
	
    if($("#chargeCheckOrderStatus").text() == 'new'){
    	$("#chargeCheckOrderStatus").text('新建');
	}

	var chargeCheckOrderTable =$('#example').dataTable( {
	    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/chargeInvoiceOrder/chargeInvoiceOrderList",
   			"aoColumns": [
   			{ "mDataProp": "ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/yh/chargeCheckOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
        		}},
       		{ "mDataProp": "ORDER_NO"},
            { "mDataProp": "CNAME"},
            { "mDataProp": "TRANSFER_ORDER_NO"},
            { "mDataProp": "DELIVERY_ORDER_NO"},
            { "mDataProp": "CREATOR_NAME" },
            { "mDataProp": "CREATE_DATE" },
            { "mDataProp": "TRANSACTION_STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.TRANSACTION_STATUS=='new')
                        return '新建';
                    if(obj.aData.TRANSACTION_STATUS=='confirmed')
                        return '已确认';
                    if(obj.aData.TRANSACTION_STATUS=='cancel')
                        return '取消';
                    
                    return obj.aData.TRANSACTION_STATUS;
                 }
            },
            { "mDataProp": "REMARK" },
            { "mDataProp": "AMOUNT"	},
            { "mDataProp": null	}
         ]
	});
} );