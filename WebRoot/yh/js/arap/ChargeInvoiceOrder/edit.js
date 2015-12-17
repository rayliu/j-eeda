$(document).ready(function() {
	if(order_no){
		document.title = order_no+' | '+document.title;
	}
	$('#menu_charge').addClass('active').find('ul').addClass('in');
		
	var saveChargeCheckOrder = function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/chargeInvoiceOrder/save', $("#chargeInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){
				contactUrl("edit?id", data.ID);
				$("#chargeInvoiceOrderId").val(data.ID);
				$("#arapAuditInvoice_order_no").text(data.ORDER_NO);
			  	$("#departureConfirmationBtn").attr("disabled", false);
			  	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
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
	$("#departureConfirmationBtn").click(function(){
		$(this).attr("disabled", true);
		var id = $("#chargeInvoiceOrderId").val();
		var order_type = $("#order_type").val();
		var chargePreInvoiceOrderIds = $("#chargePreInvoiceOrderIds").val();
		if(id != null && id != ""){
			$.post('/chargeInvoiceOrder/confirm', {chargeInvoiceOrderId:id,chargePreInvoiceOrderIds:chargePreInvoiceOrderIds,order_type:order_type}, function(data){
				if(data.ID>0){
					$("#chargeInvoiceOrderId").val(data.ID);
					$("#saveChargeInvoiceOrderBtn").attr("disabled", true);
					$("#chargeInvoiceOrderStatus").html(data.STATUS);
					chargePreInvoiceOrderTable.fnDraw();
				}else{
					$("#departureConfirmationBtn").attr("disabled", false);
					alert('数据保存失败。');
				}
			},'json');
		}
		
	});
	
	
	if($("#chargeCheckOrderStatus").text() == 'new'){
    	$("#chargeCheckOrderStatus").text('新建');
	}
		
	var chargePreInvoiceOrderTable=$('#chargePreInvoiceOrder-table').dataTable({
		"bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "sAjaxSource": "/chargeInvoiceOrder/chargePreInvoiceOrderList",
        "aoColumns": [    
            { "mDataProp": null, "sWidth":"100px",
            	"fnRender": function(obj) {
  	            	if(obj.aData.INVOICE_NO == null){
  	            		str = "<input type='text' name='invoice_no'>";
  	            	}else{
  	            		if($("#chargeInvoiceOrderStatus").text()=='新建')
  	            			str = "<input type='text' name='invoice_no' value='"+obj.aData.INVOICE_NO+"'>";
  	            		else
  	            			str = obj.aData.INVOICE_NO;
  	            	}
  	            	return str;
  	            }
  	        },    
            {"mDataProp":"ORDER_NO", "sWidth":"120px",
            	"fnRender": function(obj) {
            		if($("#order_type").val()=='申请单'){
            			return "<a href='/chargePreInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		}else if($("#order_type").val()=='对账单'){
            			return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		}else{
            			return obj.aData.ORDER_NO;
            		}
        			
            	}
  	        },
            {"mDataProp":"STATUS", "sWidth":"60px"},
            {"mDataProp":"CNAME", "sWidth":"80px"},
            {"mDataProp":"TOTAL_AMOUNT", "sWidth":"80px"},
            {"mDataProp":"REMARK", "sWidth":"80px"},
            {"mDataProp":"CREATE_BY", "sWidth":"80px"},
            {"mDataProp":"CREATE_STAMP", "sWidth":"80px"},                       
        ]      
    });	
	
	
	$("#chargePreInvoiceOrder-table").on('blur', 'input', function(e){
		e.preventDefault();
		var chargeId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		$.post('/chargeInvoiceOrder/updateInvoiceItem', {chargeId:chargeId, name:name, value:value}, function(data){
			if(data.ID> 0){
				$.scojs_message('添加成功', $.scojs_message.TYPE_OK);
			}
    	},'json');
	});
	
	
	$("#invoiceItemNo").click(function(e){	
		var chargeInvoiceOrderId = $("#chargeInvoiceOrderId").val();
		var status  = $("#chargeInvoiceOrderStatus").text();
		if(status == '新建'){
			$.post('/chargeInvoiceOrder/findAllInvoiceItemNo', {chargeInvoiceOrderId:chargeInvoiceOrderId}, function(data){
				if(data.length > 0){
					var itemInvoiceNoList = $("#itemInvoiceNoList");
					itemInvoiceNoList.empty();
					var option = "<option></option>";
					for(var i=0;i<data.length;i++){
						option += "<option value='"+data[i].INVOICE_NO+"'>"+data[i].INVOICE_NO+"</option>";
					}
					itemInvoiceNoList.append(option);	
				}
	    	},'json');	
			saveChargeCheckOrder(e);
			chargePreInvoiceOrderTable.fnSettings().sAjaxSource = "/chargeInvoiceOrder/chargePreInvoiceOrderList?chargePreInvoiceOrderIds="+$("#chargePreInvoiceOrderIds").val()+"&order_type="+$("#order_type").val();  
			chargePreInvoiceOrderTable.fnDraw();
		}else{
			chargePreInvoiceOrderTable.fnSettings().sAjaxSource = "/chargeInvoiceOrder/chargePreInvoiceOrderList?chargePreInvoiceOrderIds="+$("#chargePreInvoiceOrderIds").val()+"&order_type="+$("#order_type").val();  
			chargePreInvoiceOrderTable.fnDraw();
		}
		
	});	
	
	
	
	if($("#chargeInvoiceOrderId").val() == ""){
		$('#saveChargeInvoiceOrderBtn').attr('disabled', false);
	}else{
		if($("#chargeInvoiceOrderStatus").text() == "新建"){
			$('#saveChargeInvoiceOrderBtn').attr('disabled', false);
			$('#departureConfirmationBtn').attr('disabled', false);
		}
	}
} );