$(document).ready(function() {
	if(order_no){
		document.title = order_no +' | '+document.title;
	}
	$('#menu_cost').addClass('active').find('ul').addClass('in');
	var invoiceNoArr=[];
	var saveCostPreInvoiceOrder = function(e){
		$("#account_id").val($("#account").val());
		$("#paymentMethod").val($("input[name='paymentMethod']:checked").val());
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costPreInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/costPreInvoiceOrder/save',$("#costPreInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#sorder_no").html('<strong>'+data.ORDER_NO+'<strong>');
			  	$("#create_stamp").html(data.CREATE_STAMP);
			  	$("#remark").val(data.REMARK);
				$("#costPreInvoiceOrderId").val(data.ID);
				$("#auditBtn").attr("disabled",false);
				$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
			}else{
				alert('数据保存失败。');
			}
		},'json');
	};
	$.post('/costPreInvoiceOrder/allaccount',function(data){
		var account=$('#account');
		account.empty();
		account.append("<option>请选择</option>");
		for(var i=0; i<data.length; i++){
			var account_id=data[i].ID;
			var name=data[i].BANK_NAME;
			var account_no=data[i].ACCOUNT_NO;
			var bank_person=data[i].BANK_PERSON;
			account.append("<option  value='"+account_id+"'>"+name+"&nbsp;&nbsp;&nbsp;&nbsp;"+account_no+"&nbsp;&nbsp;&nbsp;&nbsp;"+bank_person+"</option>");
		
		}
	},'json');
	$("#InvoiceCheckBox1").on('click',function(){
		 var noinvice1 =$("input[name='noInvoice1']:checked").val();
		 if(noinvice1=="y1"){
				$('input[name=noInvoice]').attr("disabled","disabled");
			}
			 else{
				 $('input[name=noInvoice]').removeAttr("disabled");
			 }
	});
	
	$("#InvoiceCheckBox").on('click',function(){
    var noinvice =$("input[name='noInvoice']:checked").val();
    if(noinvice=="y"){
    	$("input[name='payeename']").removeAttr("readonly");
    	$("input[name='payeename1']").removeAttr("readonly");
    	//$("input[name='noInvoice']").removeAttr("readonly"); 
    	//$('input[name=noInvoice]').attr("checkbox","false");
    	$('input[name=noInvoice1]').attr("disabled","disabled");
    }
    else{
    	$('input[name=payeename]').attr("readonly","readonly");
    	$('input[name=payeename1]').attr("readonly","readonly");
    	$('input[name=noInvoice1]').removeAttr("disabled");
    }
	});
	// 审核
	$("#auditBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/auditCostPreInvoiceOrder', {costPreInvoiceOrderId:costPreInvoiceOrderId}, function(data){
			if(data.arapAuditOrder.ID>0){
				$("#audit_name").html(data.ul.C_NAME);
				$("#audit_stamp").html(data.arapAuditOrder.AUDIT_STAMP);
				$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
				$("#auditBtn").attr("disabled",true);
				$("#approvalBtn").attr("disabled",false);
				$("#costPreInvoiceOrderStatus").text("已审核");
			}else{
				alert('数据审核失败。');
			}
		},'json');
	});
	
	// 审批
	$("#approvalBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/approvalCostPreInvoiceOrder', {costPreInvoiceOrderId:costPreInvoiceOrderId}, function(data){
			if(data.arapAuditOrder.ID>0){
				$("#approver_name").html(data.ul.C_NAME);
				$("#approval_stamp").html(data.arapAuditOrder.APPROVAL_STAMP);
				$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
				$("#auditBtn").attr("disabled",true);
				$("#approvalBtn").attr("disabled",true);
				$("#printBtn").attr("disabled",false);
				$("#costPreInvoiceOrderStatus").text("已审批");
			}else{
				alert('数据审批失败。');
			}
			
		},'json');
		
	});
	$("#paymentMethod2,#paymentMethod1").click(function(e){
	if($('input:radio:checked').val()=="cash"){
		$("#acc").hide();
	}
	if($('input:radio:checked').val()=="transfers"){
		$("#acc").show();
	}
	});
	if($("#costPreInvoiceOrderStatus").text()=="已确认"){
		$("#auditBtn").attr("disabled",false);
		$("#approvalBtn").attr("disabled",false);
	}else if($("#costPreInvoiceOrderStatus").text()=="新建" || $("#costPreInvoiceOrderStatus").text()=="new"){
		var str=$("#costPreInvoiceOrderId").val();
		if(str !=null && str !=""){
			$("#auditBtn").attr("disabled",false);
		}
	}else{
		$("#auditBtn").attr("disabled",false);
		$("#approvalBtn").attr("disabled",false);
		$("#printBtn").attr("disabled",false);
		if($("#costPreInvoiceOrderStatus").text()=="已付款确认"){
			$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
			$("#auditBtn").attr("disabled",true);
			
			$("#approvalBtn").attr("disabled",true);
		}
	}
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
        
		$.post('/costPreInvoiceOrder/save',$("#costPreInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#costPreInvoiceOrderId").val(data.ID);
			  	//$("#style").show();
			  	$("#departureConfirmationBtn").attr("disabled", false);
			  	if("costPreInvoiceOrderbasic" == parentId){
			  		contactUrl("edit?id",data.ID);
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
            {"mDataProp":"CNAME"},
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
            {  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger deleteInvoiceItem' code='"+obj.aData.ID+"'>"+
              		"<i class='fa fa-trash-o fa-fw'> </i> "+
              		"删除"+
              		"</a>";
                }
            }     
  	        
            /*{"mDataProp":null}*/
         ]
	});	
	
	//异步删除发票
	$("#invoiceItem-table").on('click', '.deleteInvoiceItem', function(e){
		var id = $(this).attr('code');
		e.preventDefault();
		$.post('/costPreInvoiceOrder/deleteInvoiceItem/'+id,function(data){
             //保存成功后，刷新列表
             invoiceItemTable.fnDraw();
        },'text');
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
				costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderListById?costPreInvoiceOrderId="+$("#costPreInvoiceOrderId").val();   
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
        "sAjaxSource": "/costPreInvoiceOrder/costCheckOrderListById",
        "aoColumns": [     
          { "mDataProp": "INVOICE_NO",
          	"fnRender": function(obj) {
          		  var str="";
          		  if(obj.aData.INVOICE_NO != null && obj.aData.INVOICE_NO != ''){
          			  /*invoiceNoArr = obj.aData.ALL_INVOICE_NO.split(',');
          			  if(obj.aData.ALL_INVOICE_NO.indexOf(obj.aData.INVOICE_NO) > -1){
          			  	  str="<option value='"+obj.aData.INVOICE_NO+"' selected = 'selected'>"+obj.aData.INVOICE_NO+"</option>";
          			  	  invoiceNoArr.splice(obj.aData.INVOICE_NO, 1);
          			  }
          			  for(var i=0;i<invoiceNoArr.length;i++){
          				  str += "<option value="+invoiceNoArr[i]+">"+invoiceNoArr[i]+"</option>";          			            				  
          			  }*/
                      
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
          			  if(obj.aData.ALL_INVOICE_NO != null && obj.aData.ALL_INVOICE_NO != ""){
	          			  invoiceNoArr = obj.aData.ALL_INVOICE_NO.split(',');
	          			  for(var i=0;i<invoiceNoArr.length;i++){
	          				  str += "<option value="+invoiceNoArr[i]+">"+invoiceNoArr[i]+"</option>";          			            				  
	          			  }
          			  }
          		  }
                  return "<select name='invoice_no' multiple=''>"+str+"</select>";
          	   }
	        },  
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/costCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
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
            {"mDataProp":"CNAME", "sWidth":"120px"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"TOTAL_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":"DEBIT_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"COST_AMOUNT"},
            {"mDataProp":"REMARK"},
            {"mDataProp":"CREATOR_NAME"},        	
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
			    var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();	
				costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderListById?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
				costPreInvoiceOrderTable.fnDraw();
			}
    	},'json');
			
	    invoiceItemTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costInvoiceItemList?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
	    invoiceItemTable.fnDraw();		
	    
		costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderListById?costPreInvoiceOrderId="+costPreInvoiceOrderId;    
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
    $("#printBtn").on('click',function(){
    	var order_no = $("#sorder_no").text();
    	if(order_no != null && order_no != ""){
	    	$.post('/report/printPayMent', {order_no:order_no}, function(data){
	    		if(data.indexOf(",")>=0){
					var file = data.substr(0,data.length-1);
	    			var str = file.split(",");
	    			for(var i = 0 ;i<str.length;i++){
	    				window.open(str[i]);
	    			}
				}else{
					window.open(data);
				}
	    	});
    	}else{
    		$.scojs_message('当前单号为空', $.scojs_message.TYPE_ERROR);
    	}
    	

    });
} );