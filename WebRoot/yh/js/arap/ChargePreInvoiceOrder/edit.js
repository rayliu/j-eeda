$(document).ready(function() {
	$('#menu_charge').addClass('active').find('ul').addClass('in');
	
	if($("#chargePreInvoiceOrderId").val() == ""){
		$('#auditBtn').attr('disabled', true);
		$('#approvalBtn').attr('disabled', true);
	}else{
		if($("#chargePreInvoiceOrderStatus").text() == "新建"){
			$('#auditBtn').attr('disabled', false);
			$('#approvalBtn').attr('disabled', true);
		}else if($("#chargePreInvoiceOrderStatus").text() == "已审核"){
			$('#savechargePreInvoiceOrderBtn').attr('disabled', true);
			$('#auditBtn').attr('disabled', true);
			$('#approvalBtn').attr('disabled', false);
		}else if($("#chargePreInvoiceOrderStatus").text() == "已审批"){
			$('#savechargePreInvoiceOrderBtn').attr('disabled', true);
			$('#auditBtn').attr('disabled', true);
			$('#approvalBtn').attr('disabled', true);
		}
	}
	
	var savechargePreInvoiceOrder = function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargePreInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/chargePreInvoiceOrder/save', $("#chargePreInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#chargePreInvoiceOrderId").val(data.ID);
				$('#auditBtn').attr('disabled', false);
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
		$.post('/chargePreInvoiceOrder/auditChargePreInvoiceOrder', {chargePreInvoiceOrderId:chargePreInvoiceOrderId}, function(data){
			if(data.ID != null){
				$('#savechargePreInvoiceOrderBtn').attr('disabled', true);
				$('#auditBtn').attr('disabled', true);
				$('#approvalBtn').attr('disabled', false);
			}
			
		},'json');
	});
	
	// 审批
	$("#approvalBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var chargePreInvoiceOrderId = $("#chargePreInvoiceOrderId").val();
		$.post('/chargePreInvoiceOrder/approvalChargePreInvoiceOrder', {chargePreInvoiceOrderId:chargePreInvoiceOrderId}, function(data){
			$('#savechargePreInvoiceOrderBtn').attr('disabled', true);
			$('#auditBtn').attr('disabled', true);
			$('#approvalBtn').attr('disabled', true);
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
		$.post('/chargePreInvoiceOrder/save', $("#chargePreInvoiceOrderForm").serialize(), function(data){
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
		
		chargeCheckListTab.fnSettings().sAjaxSource = "/chargePreInvoiceOrder/chargeOrderListByIds?chargeCheckOrderIds="+$("#chargeCheckOrderIds").val();
		chargeCheckListTab.fnDraw(); 
	});
	
    if($("#chargePreInvoiceOrderStatus").text() == 'new'){
    	$("#chargePreInvoiceOrderStatus").text('新建');
	}

    var chargeCheckListTab = $('#chargeCheckList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargePreInvoiceOrder/chargeOrderListByIds",
        "aoColumns": [   
            {"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
            		if(ChargeCheck.isUpdate || ChargeCheck.isAffirm){
            			return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		}else{
            			return obj.aData.ORDER_NO;
            		}
        			
        	}},
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
            {"mDataProp":"TOTAL_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":"DEBIT_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"CHARGE_AMOUNT"},
            {"mDataProp":"REMARK"},
            {"mDataProp":"CREATOR_NAME"},        	
            {"mDataProp":"CREATE_STAMP"}                       
        ]      
    });
    
    $.post('/chargePreInvoiceOrder/searchAllAccount',function(data){
		 if(data.length > 0){
			 var accountTypeSelect = $("#accountTypeSelect");
			 accountTypeSelect.empty();
			 var hideAccountId = $("#hideAccountId").val();
			 accountTypeSelect.append("<option ></option>");
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideAccountId){
					 accountTypeSelect.append("<option value='"+data[i].ID+"' selected='selected'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");
				 }else{
					 accountTypeSelect.append("<option value='"+data[i].ID+"'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");					 
				 }
			}
		}
	},'json');

    $("input[name='paymentMethod']").each(function(){
		if($("#paymentMethodRadio").val() == $(this).val()){
			$(this).attr('checked', true);
			if($(this).val() == 'transfers'){	    		
	    		$("#accountTypeDiv").show();    		
	    	}
		}
	 }); 
    
    $("#paymentMethods").on('click', 'input', function(){
    	if($(this).val() == 'cash'){
    		$("#accountTypeDiv").hide();
    	}else{
    		$("#accountTypeDiv").show();    		
    	}
    }); 
    
} );