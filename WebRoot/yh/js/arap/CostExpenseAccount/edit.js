$(document).ready(function() {
    $('#menu_cost').addClass('active').find('ul').addClass('in');
    
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
    '</div>';
	
	var reimbursementId = $("#reimbursementId").val();
	if(reimbursementId == "" || reimbursementId == null){
		$("#auditBtn").prop("disabled",true);
		$("#approvalBtn,#cancelAuditBtn,#cancelApprovalBtn").hide();
	}else{
		var status = $("#status").val();
		if("新建" == status){
			$("#approvalBtn,#cancelAuditBtn,#cancelApprovalBtn").hide();
		}else if("已审核" == status){
			$("#auditBtn,#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",true);
			$("#auditBtn,#cancelApprovalBtn").hide();
		}else if("已审批" == status){
			$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",true);
			$("#auditBtn,#approvalBtn,#cancelAuditBtn").hide();
		}else if("取消审核" == status){
			$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",false);
			$("#approvalBtn,#cancelAuditBtn,#cancelApprovalBtn").hide();
		}else if("取消审批" == status){
			$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",true);
			$("#auditBtn,#cancelAuditBtn,#cancelApprovalBtn").hide();
		}
	}
	
	//from表单验证
	var validate = $('#expenseAccountForm').validate({
        rules: {
        	account_name: {required:true},
        	account_no: {required:true,number: true}
        },
        messages : {	             
        	customerMessage : {number:"请输入数字",required:"必填项"}
        }
    });
	
	//保存、修改数据
 	var saveCarSummaryData = function(){
 		//提交前，校验数据
        if(!$("#expenseAccountForm").valid()){
	       	return false;
        }
		$.post('/costReimbursement/saveReimbursementOrder', $("#expenseAccountForm").serialize(), function(data){
 			if(data != null){
 				$("#reimbursementId").val(data.ID);
 				$("#order_no").val(data.ORDER_NO);
 				$("#status").val(data.STATUS);
 				$("#create_stamp").val(data.CREATE_STAMP);
 				$.post('/costReimbursement/findUser', {"userId":data.CREATE_ID}, function(data){
 					$("#create_name").val(data.USER_NAME);
 				});
 				$("#auditBtn,#approvalBtn").prop("disabled",false);
 				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
 			}else{
 				$.scojs_message('保存失败', $.scojs_message.TYPE_OK);
 			}
	 	},'json');
 	};
	
	//点击保存
	$("#saveExpenseAccount").click(function(e){
		saveCarSummaryData();
    });
    
	//点击审核、审批、取消审核、取消审批
	$("#auditBtn,#approvalBtn,#cancelAuditBtn,#cancelApprovalBtn").click(function(e){
		$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",true);
		var btntTxt = $(this).text();
		$(this).hide();
		var reimbursementId = $("#reimbursementId").val();
		$.post('/costReimbursement/updateReimbursement', {reimbursementId:reimbursementId,btntTxt:btntTxt}, function(data){
			$("#status").val(data.STATUS);
			$("#audit_stamp").val(data.AUDIT_STAMP);
			$("#approval_stamp").val(data.APPROVAL_STAMP);
				
			if(data.AUDIT_ID != "" && data.AUDIT_ID != null){
				$.post('/costReimbursement/findUser', {"userId":data.CREATE_ID}, function(data){
					$("#audit_name").val(data.USER_NAME);
				});
			}else{
				$("#audit_name").val("");
			}
			
			if(data.APPROVAL_ID != "" && data.APPROVAL_ID != null){
				$.post('/costReimbursement/findUser', {"userId":data.CREATE_ID}, function(data){
					$("#approval_name").val(data.USER_NAME);
				});
			}else{
				$("#approval_name").val("");
			}
			if("审核" == btntTxt){
				$("#cancelAuditBtn").show();
				$("#approvalBtn").show();
			}else if("审批" == btntTxt){
				$("#cancelAuditBtn").hide();
				$("#cancelApprovalBtn").show();
			}else if("取消审核" == btntTxt){
				$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",false);
				$("#auditBtn").show();
				$("#approvalBtn").hide();
			}else if("取消审批" == btntTxt){
				$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",true);
				$("#approvalBtn").show();
				$("#cancelAuditBtn").show();
			}
			reimbursementOrderFinItemTbody.fnSettings().oFeatures.bServerSide = true;
			reimbursementOrderFinItemTbody.fnSettings().sAjaxSource = "/costReimbursement/accountPayable/"+reimbursementId;   
			reimbursementOrderFinItemTbody.fnDraw();
	 	},'json');
    });
	//应付datatable
	var reimbursementOrderFinItemTbody=$('#reimbursementOrderFinItemTbody').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
			{"mDataProp":"NAME",
			    "fnRender": function(obj) {
			        if(obj.aData.NAME!='' && obj.aData.NAME != null){
			        	var str="";
			        	if($("#saveExpenseAccount").prop("disabled")){
				        	$("#paymentItemList").children().each(function(){
				        		if(obj.aData.NAME == $(this).text())
				        			str+=$(this).text();
				        	});
				        	return str;
			        	}else{
			        		$("#paymentItemList").children().each(function(){
				        		if(obj.aData.NAME == $(this).text()){
				        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";
				        		}else{
				        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
				        		}
				        	});
				        	return "<select name='fin_item_id'>"+str+"</select>";
			        	}
			        }else{
			        	var str="";
			        	$("#paymentItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	if($("#saveExpenseAccount").prop("disabled"))
			        		return "<select name='fin_item_id' disabled='true'>"+str+"</select>";
			        	else
			        		return "<select name='fin_item_id'>"+str+"</select>";
			        }
			 }},
			{"mDataProp":"AMOUNT",
			     "fnRender": function(obj) {
			    	 
			    	 if($("#saveExpenseAccount").prop("disabled")){
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return obj.aData.AMOUNT;
				         }else{
				         	 return "";
				         }
			    	 }else{
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
				         }else{
				         	 return "<input type='text' name='amount'>";
				         }
			    	 }
			 }},
			 {"mDataProp":"REMARK",
				 "fnRender": function(obj) {
                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                    	if($("#saveExpenseAccount").prop("disabled"))
                    		return obj.aData.REMARK;
                    	else
                    		return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
                    }else{
                    	if($("#saveExpenseAccount").prop("disabled"))
                    		return "";
                    	else
                    		return "<input type='text' name='remark'>";
                    }
	         }}, 
			{  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                	if($("#saveExpenseAccount").prop("disabled")){
		        		return "";
		        	}else{
		        		return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
	              		"<i class='fa fa-trash-o fa-fw'> </i> "+
	              		"删除"+
	              		"</a>";
		        	}
                    
                }
            }      
        ]      
    });
	
	// tab - 应付
	$("#carmanageLine").click(function(e){
		if(!$("#saveExpenseAccount").prop("disabled")){
			saveCarSummaryData();
		}
		var reimbursementId = $("#reimbursementId").val();
		if(reimbursementId != null && reimbursementId != ""){
			reimbursementOrderFinItemTbody.fnSettings().oFeatures.bServerSide = true;
			reimbursementOrderFinItemTbody.fnSettings().sAjaxSource = "/costReimbursement/accountPayable/"+reimbursementId;   
			reimbursementOrderFinItemTbody.fnDraw();
		}
	});
	// 新增
	$("#addReimbursementOrderFinItem").click(function(e){
		if(!$("#saveExpenseAccount").prop("disabled")){
			var reimbursementId = $("#reimbursementId").val();
			if(reimbursementId != ""){
				$.post('/costReimbursement/addNewRow/'+reimbursementId,function(data){
					reimbursementOrderFinItemTbody.fnSettings().oFeatures.bServerSide = true;
					reimbursementOrderFinItemTbody.fnSettings().sAjaxSource = "/costReimbursement/accountPayable/"+reimbursementId;   
					reimbursementOrderFinItemTbody.fnDraw();
				});	
			}
		}
	});
	//应付修改
	$("#reimbursementOrderFinItemTbody").on('blur', 'input,select', function(e){
		if(!$("#saveExpenseAccount").prop("disabled")){
			var paymentId = $(this).parent().parent().attr("id");
			var name = $(this).attr("name");
			var value = $(this).val();
			if(name == "amount" && isNaN(value)){
				$.scojs_message('【金额】只能输入数字,请重新输入', $.scojs_message.TYPE_OK);
				$(this).val("");
				$(this).focus();
				return false;
			}
			
			$.post('/costReimbursement/updateReimbursementOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
				$("#amount").val(data.AMOUNT);
	    	},'json');
		}
	});    
	//异步删除应付
	 $("#reimbursementOrderFinItemTbody").on('click', '.finItemdel', function(e){
		 if(!$("#saveExpenseAccount").prop("disabled")){
			 var id = $(this).attr('code');
			 e.preventDefault();
			 $.post('/costReimbursement/finItemdel/'+id,function(data){
	             //保存成功后，刷新列表
	             console.log(data);
	             reimbursementOrderFinItemTbody.fnDraw();
	         },'json');
		 }
	 });
	
    
});