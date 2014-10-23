$(document).ready(function() {
	$('#menu_charge').addClass('active').find('ul').addClass('in');
	
	var savechargePreInvoiceOrder = function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargePreInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/yh/chargePreInvoiceOrder/save', $("#chargePreInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#chargePreInvoiceOrderId").val(data.ID);
			}else{
				alert('数据保存失败。');
			}
		},'json');
	};
    
	// 审核
	$("#auditBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var chargePreInvoiceOrderId = $("#chargePreInvoiceOrderId").val();
		$.post('/yh/chargePreInvoiceOrder/auditChargePreInvoiceOrder', {chargePreInvoiceOrderId:chargePreInvoiceOrderId}, function(data){
		},'json');
	});
	
	// 审批
	$("#approvalBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var chargePreInvoiceOrderId = $("#chargePreInvoiceOrderId").val();
		$.post('/yh/chargePreInvoiceOrder/approvalChargePreInvoiceOrder', {chargePreInvoiceOrderId:chargePreInvoiceOrderId}, function(data){
		},'json');
	});
	
	/*--------------------------------------------------------------------*/
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	
	//设置一个变量值，用来保存当前的ID
	var parentId = "chargePreInvoiceOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#chargePreInvoiceOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#savechargePreInvoiceOrderBtn").click(function(e){
 		
 		savechargePreInvoiceOrder(e);

 		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	});
	
	$("#chargePreInvoiceOrderItem").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargePreInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/yh/chargePreInvoiceOrder/save', $("#chargePreInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#chargePreInvoiceOrderId").val(data.ID);
			  	//$("#style").show();
			  	$("#departureConfirmationBtn").attr("disabled", false);
			  	if("chargePreInvoiceOrderbasic" == parentId){
			  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  	}
			}else{
				alert('数据保存失败。');
			}
		},'json');
		parentId = e.target.getAttribute("id");
	});
	
    if($("#chargePreInvoiceOrderStatus").text() == 'new'){
    	$("#chargePreInvoiceOrderStatus").text('新建');
	}

    var datatable=$('#chargeCheckList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/chargeCheckOrder/list",
        "aoColumns": [   
            {"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"ORDER_NO"},
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
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"CNAME"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"REMARK"},
            {"mDataProp":"CREATOR_NAME"},        	
            {"mDataProp":"CREATE_STAMP"}                       
        ]      
    });
} );