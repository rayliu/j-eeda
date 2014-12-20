$(document).ready(function() {
	$('#menu_cost').addClass('active').find('ul').addClass('in');
	
	var saveCostPreInvoiceOrder = function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costPreInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/costPreInvoiceOrder/save', $("#costPreInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#costPreInvoiceOrderId").val(data.ID);
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
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/auditCostPreInvoiceOrder', {costPreInvoiceOrderId:costPreInvoiceOrderId}, function(data){
		},'json');
	});
	
	// 审批
	$("#approvalBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/approvalCostPreInvoiceOrder', {costPreInvoiceOrderId:costPreInvoiceOrderId}, function(data){
		},'json');
		$("#printBtn").show();
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
	var parentId = "costPreInvoiceOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#costPreInvoiceOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveCostPreInvoiceOrderBtn").click(function(e){
 		
 		saveCostPreInvoiceOrder(e);

 		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	});
	
	$("#costPreInvoiceOrderItem").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costPreInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/costPreInvoiceOrder/save', $("#costPreInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#costPreInvoiceOrderId").val(data.ID);
			  	//$("#style").show();
			  	$("#departureConfirmationBtn").attr("disabled", false);
			  	if("costPreInvoiceOrderbasic" == parentId){
			  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  	}
			}else{
				alert('数据保存失败。');
			}
		},'json');
		parentId = e.target.getAttribute("id");		

	    var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();	
		invoiceItemTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costInvoiceItemList?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
	    invoiceItemTable.fnDraw();
		var costCheckOrderIds = $("#costCheckOrderIds").val();	
		costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderList?costCheckOrderIds="+costCheckOrderIds;   
		costPreInvoiceOrderTable.fnDraw();
	});
	
    if($("#costPreInvoiceOrderStatus").text() == 'new'){
    	$("#costPreInvoiceOrderStatus").text('新建');
	}    

	var invoiceItemTable =$('#invoiceItem-table').dataTable( {
	    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "sAjaxSource": "/costPreInvoiceOrder/costInvoiceItemList",
   			"aoColumns": [
            { "mDataProp": null,
  	            "fnRender": function(obj) {
  	            	var str;
  	            	if(obj.aData.INVOICE_NO == null){
  	            		str = "<input type='text' name='invoice_no'>";
  	            	}else{
  	            		str = "<input type='text' name='invoice_no' value='"+obj.aData.INVOICE_NO+"'>";
  	            	}
  	            	return str;
  	            }
  	        },
            {"mDataProp":null},
            { "mDataProp": null,
  	            "fnRender": function(obj) {
  	            	var str;
  	            	if(obj.aData.AMOUNT == null){
  	            		str = "<input type='text' name='amount'>";
  	            	}else{
  	            		str = "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
  	            	}
  	            	return str;
  	            }
  	        },
            {"mDataProp":"COST_ORDER_NO"},            
            {"mDataProp":null}
         ]
	});	

	$("#invoiceItem-table").on('blur', 'input', function(e){
		e.preventDefault();
		var invoiceItemId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/updateInvoiceItem', {costPreInvoiceOrderId:costPreInvoiceOrderId, invoiceItemId:invoiceItemId, name:name, value:value}, function(data){
			if(data.length > 0){
				var itemInvoiceNoList = $("#itemInvoiceNoList");
				itemInvoiceNoList.empty();
				var option = "<option></option>";
				for(var i=0;i<data.length;i++){
					option += "<option value='"+data[i].INVOICE_NO+"'>"+data[i].INVOICE_NO+"</option>";
				}
				itemInvoiceNoList.append(option);	
			    var costCheckOrderIds = $("#costCheckOrderIds").val();	
				costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderList?costCheckOrderIds="+costCheckOrderIds;   
				costPreInvoiceOrderTable.fnDraw();
			}
    	},'json');
	});	
	
	var costPreInvoiceOrderTable=$('#costPreInvoiceOrder-table').dataTable({
		"bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "sAjaxSource": "/costCheckOrder/list",
        "aoColumns": [     
          { "mDataProp": "INVOICE_NO",
          	"fnRender": function(obj) {
                  var str="";
                  if(obj.aData.INVOICE_NO!='' && obj.aData.INVOICE_NO != null){
                  	$("#itemInvoiceNoList").children().each(function(){
                  		if(obj.aData.INVOICE_NO.indexOf($(this).text()) > -1 && $(this).text() != ''){
                  			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
                  		}else{
                  			if($(this).text != null){
                  				str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
                  			}
                  		}
                  	});
                  }else{
                  	$("#itemInvoiceNoList").children().each(function(){
                  		if($(this).text != null){
              				str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
              			}
                  	});
                  }
                  return "<select name='invoice_no' multiple=''>"+str+"</select>";
          	   }
	        },  
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/costCheckOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
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
            {"mDataProp":null},
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
            {"mDataProp":null},        	
            {"mDataProp":"CREATE_STAMP"}                        
        ]      
    });	
	
	$("#addInvoiceItemBtn").click(function(){		
		$.post('/costPreInvoiceOrder/addInvoiceItem', {costPreInvoiceOrderId:$("#costPreInvoiceOrderId").val()}, function(data){
			if(data.success){
			    var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();	
				invoiceItemTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costInvoiceItemList?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
			    invoiceItemTable.fnDraw();
			}
		},'json');
	});
	
	$("#costPreInvoiceOrderItem").click(function(){	
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/findAllInvoiceItemNo', {costPreInvoiceOrderId:costPreInvoiceOrderId}, function(data){
			if(data.length > 0){
				var itemInvoiceNoList = $("#itemInvoiceNoList");
				itemInvoiceNoList.empty();
				var option = "<option></option>";
				for(var i=0;i<data.length;i++){
					option += "<option value='"+data[i].INVOICE_NO+"'>"+data[i].INVOICE_NO+"</option>";
				}
				itemInvoiceNoList.append(option);	
			    var costCheckOrderIds = $("#costCheckOrderIds").val();	
				costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderList?costCheckOrderIds="+costCheckOrderIds;   
				costPreInvoiceOrderTable.fnDraw();
			}
    	},'json');
			
	    invoiceItemTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costInvoiceItemList?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
	    invoiceItemTable.fnDraw();		
	    var costCheckOrderIds = $("#costCheckOrderIds").val();	
		costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderList?costCheckOrderIds="+costCheckOrderIds;    
		costPreInvoiceOrderTable.fnDraw();
	});	

	$("#costPreInvoiceOrder-table").on('blur', 'select', function(e){
		e.preventDefault();
		var costCheckOrderId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/updatePreInvoice', {costPreInvoiceOrderId:costPreInvoiceOrderId, costCheckOrderId:costCheckOrderId, name:name, value:value}, function(data){
			if(data.success){
			    var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();	
				invoiceItemTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costInvoiceItemList?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
			    invoiceItemTable.fnDraw();
			}
    	},'json');
	});

    $.post('/costPreInvoiceOrder/searchAllAccount',function(data){
		 if(data.length > 0){
			 var accountTypeSelect = $("#accountTypeSelect");
			 accountTypeSelect.empty();
			 var hideAccountId = $("#hideAccountId").val();
			 accountTypeSelect.append("<option ></option>");
			 for(var i=0; i<data.length+1; i++){
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
    $("#printBtn").on('click',function(){
    	var order_no = $("#order_no").text();
    	$.post('/report/printPayMent', {order_no:order_no}, function(data){
    		window.open(data);
    	});

    });
} );