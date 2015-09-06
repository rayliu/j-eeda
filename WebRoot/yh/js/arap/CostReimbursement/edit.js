$(document).ready(function() {
	if(order_no){
		document.title = order_no+' | '+document.title;
	}
	$('#menu_finance').addClass('active').find('ul').addClass('in');
	var num = 1;
	var clickTabId = "carmanagebasic";
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
			$("#auditBtn,#cancelApprovalBtn,#cancelAuditBtn").hide();
		}else if("已审批" == status){
			$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",true);
			$("#auditBtn,#approvalBtn,#cancelAuditBtn,#cancelApprovalBtn").hide();
		}else if("取消审核" == status){
			$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",false);
			$("#approvalBtn,#cancelAuditBtn,#cancelApprovalBtn").hide();
		}else if("取消审批" == status){
			$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",true);
			$("#auditBtn,#cancelAuditBtn,#cancelApprovalBtn").hide();
		}
	}
	
	var invoicePayment = $("#invoicePayment").val();
	$("input[name='invoice_payment'][value='"+invoicePayment+"']").attr("checked","checked");
	var paymentType = $("#paymentType").val();
	$('#payment_type').val(paymentType);
	
	//from表单验证
	var validate = $('#expenseAccountForm').validate({
        rules: {
        	payment_type:{required:true},
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
        
        $("#accId").val($("#payment_info").val());
        $("#account_bank").val($("#account_bank1").val());
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
 				contactUrl("edit?id",data.ID);
 			}else{
 				$.scojs_message('保存失败', $.scojs_message.TYPE_OK);
 			}
	 	},'json');
 	};
	
	//点击保存
	$("#saveExpenseAccount").click(function(e){
		saveCarSummaryData();
    });
	 $("#printBtn").on('click',function(){
	    	var order_no = $("#order_no").val();
	    	if(order_no != null && order_no != ""){
	    		$.post('/report/printReimburse', {order_no:order_no}, function(data){
	        		window.open(data);
	        	});
	    	}else{
	    		$.scojs_message('当前单号为空', $.scojs_message.TYPE_ERROR);
	    	}
	    	
	    });
	//tab - 基本信息
	$("#carmanagebasic").click(function(e){
		clickTabId = e.target.getAttribute("id");
    });
	
	if($("#payment_type").val()=="现金"||$("#payment_type").val()==""){
		$("#payment_info").val("");
		$("#accId").val("");
		$('#payment_info').attr("disabled",true);
	}
	if($("#payment_type").val()=="转账"){
		$('#payment_info').attr("disabled",false);
	}	
	$("#payment_type").click(function () {
		var selectTxt=$("#payment_type").val();
		if(selectTxt=="现金"||selectTxt==""){
			$("#payment_info").val("");
			$("#accId").val("");
			$('#payment_info').attr("disabled",true);
		}
		if(selectTxt=="转账"){
			$("#payment_info").val("");
			$('#payment_info').attr("disabled",false);
		}	
		
	});
		$.post('/costReimbursement/getFinAccount', function(data){
		var payment_info = $("#payment_info");
		for(var i = 0; i < data.length; i++){
			var bank_name = data[i].BANK_NAME;
			var account_no = data[i].ACCOUNT_NO;
			var bank_person = data[i].BANK_PERSON;
			var acc_id = data[i].ID;
			if(bank_name == null){
				bank_name='';
			}
			if(account_no == null){
				account_no='';
			}
			if(bank_person == null){
				bank_person='';
			}
			
			payment_info.append("<option value="+acc_id+">"+bank_name+"&nbsp&nbsp&nbsp&nbsp"+account_no+"&nbsp&nbsp&nbsp&nbsp"+bank_person+"</option>");
		}
	},'json');
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
			/*if(data.AUDIT_ID != "" && data.AUDIT_ID != null){
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
				//$("#cancelAuditBtn").show();
				$("#approvalBtn").show();
			}else if("审批" == btntTxt){
				$("#cancelAuditBtn").hide();
				//$("#cancelApprovalBtn").show();
			}else if("取消审核" == btntTxt){
				$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",false);
				$("#auditBtn").show();
				$("#approvalBtn").hide();
			}else if("取消审批" == btntTxt){
				$("#saveExpenseAccount,#addReimbursementOrderFinItem").prop("disabled",true);
				$("#approvalBtn").show();
				$("#cancelAuditBtn").show();
			}*/
			num = 1;
			reimbursementOrderFinItemTbody.fnSettings().oFeatures.bServerSide = true;
			reimbursementOrderFinItemTbody.fnSettings().sAjaxSource = "/costReimbursement/accountPayable/"+reimbursementId;   
			reimbursementOrderFinItemTbody.fnDraw();
			reimbursermentMilestoneTbody.fnSettings().oFeatures.bServerSide = true;
			reimbursermentMilestoneTbody.fnSettings().sAjaxSource = "/costReimbursement/findAllMilestone/"+reimbursementId;   
			reimbursermentMilestoneTbody.fnDraw();
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
			{ "mDataProp": null,"sWidth":"40px",
				"fnRender": function(obj) {
					return num++;
				}
			}, 
			{"mDataProp":"PARENT_ITEM",
			    "fnRender": function(obj) {
			        if(obj.aData.PARENT_ITEM!='' && obj.aData.PARENT_ITEM != null){
			        	var str="";
			        	if($("#saveExpenseAccount").prop("disabled")){
				        	$("#parentItemList").children().each(function(){
				        		if(obj.aData.PARENT_ITEM == $(this).text())
				        			str+=$(this).text();
				        	});
				        	return str;
			        	}else{
			        		$("#parentItemList").children().each(function(){
				        		if(obj.aData.PARENT_ITEM == $(this).text()){
				        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";
				        		}else{
				        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
				        		}
				        	});
				        	return "<select name='parent_item_id'>"+str+"</select>";
			        	}
			        }else{
			        	var str="";
			        	$("#parentItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	if($("#saveExpenseAccount").prop("disabled"))
			        		return "";
			        	else
			        		return "<select name='parent_item_id'>"+str+"</select>";
			        }
			 }},
			{"mDataProp":"ITEM",
				"sClass": "fin_item",
			    "fnRender": function(obj) {
			        if(obj.aData.ITEM!='' && obj.aData.ITEM != null){
			        	var str="";
			        	if($("#saveExpenseAccount").prop("disabled")){
				        	$("#paymentItemList").children().each(function(){
				        		if(obj.aData.ITEM == $(this).text())
				        			str+=$(this).text();
				        	});
				        	return str;
			        	}else{
			        		$("#paymentItemList").children().each(function(){
				        		if(obj.aData.ITEM == $(this).text()){
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
			        		return "";
			        	else
			        		return "<select name='fin_item_id'>"+str+"</select>";
			        }
			 }},
			 {"mDataProp":"INVOICE_AMOUNT",
			     "fnRender": function(obj) {
			    	 
			    	 if($("#saveExpenseAccount").prop("disabled")){
			    		 if(obj.aData.INVOICE_AMOUNT!='' && obj.aData.INVOICE_AMOUNT != null){
				             return obj.aData.INVOICE_AMOUNT;
				         }else{
				         	 return "";
				         }
			    	 }else{
			    		 if(obj.aData.INVOICE_AMOUNT!='' && obj.aData.INVOICE_AMOUNT != null){
				             return "<input type='text' name='invoice_amount' value='"+obj.aData.INVOICE_AMOUNT+"'>";
				         }else{
				         	 return "<input type='text' name='invoice_amount'>";
				         }
			    	 }
			}},
			{"mDataProp":"REVOCATION_AMOUNT",
			     "fnRender": function(obj) {
			    	 if($("#saveExpenseAccount").prop("disabled")){
			    		 if(obj.aData.REVOCATION_AMOUNT!='' && obj.aData.REVOCATION_AMOUNT != null){
				             return obj.aData.REVOCATION_AMOUNT;
				         }else{
				         	 return "";
				         }
			    	 }else{
			    		 if(obj.aData.REVOCATION_AMOUNT!='' && obj.aData.REVOCATION_AMOUNT != null){
				             return "<input type='text' name='revocation_amount' value='"+obj.aData.REVOCATION_AMOUNT+"'>";
				         }else{
				         	 return "<input type='text' name='revocation_amount'>";
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
			if(clickTabId == "carmanagebasic"){
	    		//提交前，校验数据
	            if(!$("#expenseAccountForm").valid()){
	            	$.scojs_message('请先保存报销单', $.scojs_message.TYPE_OK);
	    	       	return false;
	            }else{
	            	saveCarSummaryData();
	            }
			}
		}
		clickTabId = e.target.getAttribute("id");
		var reimbursementId = $("#reimbursementId").val();
		if(reimbursementId != null && reimbursementId != ""){
			num = 1;
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
					num = 1;
					reimbursementOrderFinItemTbody.fnSettings().oFeatures.bServerSide = true;
					reimbursementOrderFinItemTbody.fnSettings().sAjaxSource = "/costReimbursement/accountPayable/"+reimbursementId;   
					reimbursementOrderFinItemTbody.fnDraw();
				});	
			}
		}
	});
	//应付修改
	$("#reimbursementOrderFinItemTbody").on('keyup click', 'input,select', function(e){
		if(!$("#saveExpenseAccount").prop("disabled")){
			var paymentId = $(this).parent().parent().attr("id");
			//var fin_item=$(".fin_item");
			var fin_item =  $(this).parent().parent().find('.fin_item').children();
			var name = $(this).attr("name");
			var value = $(this).val();
			if(name == "amount" && isNaN(value)){
				$.scojs_message('【金额】只能输入数字,请重新输入', $.scojs_message.TYPE_OK);
				$(this).val("");
				$(this).focus();
				return false;
			}
			
			if(name == 'parent_item_id'){
				$.post('/costReimbursement/findItem', {name:name, value:value}, function(data){
					fin_item.empty();
					fin_item.append("<option >--请选择--</option>");
					for(var i = 0; i < data.length; i++){
						var id = data[i].ID;
						var name = data[i].NAME;
						if(id == null){
							id='';
						}
						if(name == null){
							name='';
						}
						fin_item.append("<option value ='"+id+"' >"+name+"</option>");
					}
		    	},'json');
				return;
			}
			
			if(value != "" && value != null&&value!="--请选择--"){
				$.post('/costReimbursement/updateReimbursementOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
					$("#amount").val(data.AMOUNT);
		    	},'json');
			}
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
	             num = 1;
	             reimbursementOrderFinItemTbody.fnDraw();
	         },'json');
		 }
	 });
	
	//刷新线路
	var reimbursermentMilestoneTbody = $('#reimbursermentMilestoneTbody').dataTable({           
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	 "bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [
            { "mDataProp": "STATUS"},
            { "mDataProp": "USER_NAME",
            	"fnRender": function(obj) {
            		if(obj.aData.C_NAME != "" && obj.aData.C_NAME != null)
            			return obj.aData.C_NAME;
            		else
            			return obj.aData.USER_NAME;
            		
            }},      
            { "mDataProp": "CREATE_STAMP"}
        ]        
    });
	 
	// tab - 应付
	$("#milestoneList").click(function(e){
		if(!$("#saveExpenseAccount").prop("disabled")){
			if(clickTabId == "carmanagebasic"){
	    		//提交前，校验数据
	            if(!$("#expenseAccountForm").valid()){
	            	$.scojs_message('请先保存报销单', $.scojs_message.TYPE_OK);
	    	       	return false;
	            }else{
	            	saveCarSummaryData();
	            }
			}
		}
		clickTabId = e.target.getAttribute("id");
		var reimbursementId = $("#reimbursementId").val();
		if(reimbursementId != null && reimbursementId != ""){
			reimbursermentMilestoneTbody.fnSettings().oFeatures.bServerSide = true;
			reimbursermentMilestoneTbody.fnSettings().sAjaxSource = "/costReimbursement/findAllMilestone/"+reimbursementId;   
			reimbursermentMilestoneTbody.fnDraw();
		}
	});
    
});