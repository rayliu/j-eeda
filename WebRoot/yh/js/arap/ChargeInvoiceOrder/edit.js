$(document).ready(function() {
	$('#menu_charge').addClass('active').find('ul').addClass('in');
	
	var saveChargeCheckOrder = function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/yh/chargeInvoiceOrder/save', $("#chargeInvoiceOrderForm").serialize(), function(data){
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
 	$("#saveChargeInvoiceOrderBtn").click(function(e){
 		saveChargeCheckOrder(e);
	});
	
	if($("#chargeCheckOrderStatus").text() == 'new'){
    	$("#chargeCheckOrderStatus").text('新建');
	}

    var chargeCheckOrderIds = $("#chargeCheckOrderIds").val();
	var chargeCheckOrderTable =$('#example').dataTable( {
	    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/chargeInvoiceOrder/chargeInvoiceOrderList?chargeCheckOrderIds="+chargeCheckOrderIds,
   			"aoColumns": [
   			{"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/yh/chargeCheckOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"CNAME"},
            {"mDataProp":"STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },
            {"mDataProp":"RETURN_ORDER_NO"},
            {"mDataProp":"TRANSFER_ORDER_NO"},
            {"mDataProp":"DELIVERY_ORDER_NO"},            
            {"mDataProp":"CREATOR_NAME"},        	
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"REMARK"},
            { "mDataProp": "AMOUNT"	},
            { "mDataProp": null	,
            	"fnRender": function(obj) {
        			return "<input type='text' value='"+obj.aData.AMOUNT+"'>";
        		}}
         ]
	});
} );