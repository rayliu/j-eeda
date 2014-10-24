$(document).ready(function() {
	$('#menu_cost').addClass('active').find('ul').addClass('in');
	
	var saveCostCheckOrder = function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/yh/costInvoiceOrder/save', $("#costInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#costInvoiceOrderId").val(data.ID);
			  	//$("#style").show();
			  	$("#departureConfirmationBtn").attr("disabled", false);
			}else{
				alert('数据保存失败。');
			}
		},'json');
	};
    
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveCostInvoiceOrderBtn").click(function(e){
 		saveCostCheckOrder(e);
	});
	
	if($("#costCheckOrderStatus").text() == 'new'){
    	$("#costCheckOrderStatus").text('新建');
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
        "sAjaxSource": "/yh/costInvoiceOrder/costInvoiceItemList",
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
            {"mDataProp":"PRE_ORDER_NO"},            
            {"mDataProp":null}
         ]
	});	

	$("#invoiceItem-table").on('blur', 'input', function(e){
		e.preventDefault();
		var invoiceItemId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		var costInvoiceOrderId = $("#costInvoiceOrderId").val();
		$.post('/yh/costInvoiceOrder/updateInvoiceItem', {costInvoiceOrderId:costInvoiceOrderId, invoiceItemId:invoiceItemId, name:name, value:value}, function(data){
			if(data.length > 0){
				var itemInvoiceNoList = $("#itemInvoiceNoList");
				itemInvoiceNoList.empty();
				var option = "<option></option>";
				for(var i=0;i<data.length;i++){
					option += "<option value='"+data[i].INVOICE_NO+"'>"+data[i].INVOICE_NO+"</option>";
				}
				itemInvoiceNoList.append(option);	
			    var costInvoiceOrderId = $("#costInvoiceOrderId").val();	
				costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/yh/costInvoiceOrder/costPreInvoiceOrderList";   
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
        "sAjaxSource": "/yh/costInvoiceOrder/costPreInvoiceOrderList",
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
        			return "<a href='/yh/costPreInvoiceOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
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
            {"mDataProp":"REMARK"},
            {"mDataProp":"CREATE_BY"},
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"AUDIT_BY"},
            {"mDataProp":"AUDIT_STAMP"},
            {"mDataProp":"APPROVAL_BY"},
            {"mDataProp":"APPROVAL_STAMP"}                        
        ]      
    });	
	
	$("#addInvoiceItemBtn").click(function(){		
		$.post('/yh/costInvoiceOrder/addInvoiceItem', {costInvoiceOrderId:$("#costInvoiceOrderId").val()}, function(data){
			if(data.success){
			    var costInvoiceOrderId = $("#costInvoiceOrderId").val();	
				invoiceItemTable.fnSettings().sAjaxSource = "/yh/costInvoiceOrder/costInvoiceItemList?costInvoiceOrderId="+costInvoiceOrderId;   
			    invoiceItemTable.fnDraw();
			}
		},'json');
	});
	
	$("#invoiceItemNo").click(function(){	
		var costInvoiceOrderId = $("#costInvoiceOrderId").val();
		$.post('/yh/costInvoiceOrder/findAllInvoiceItemNo', {costInvoiceOrderId:costInvoiceOrderId}, function(data){
			if(data.length > 0){
				var itemInvoiceNoList = $("#itemInvoiceNoList");
				itemInvoiceNoList.empty();
				var option = "<option></option>";
				for(var i=0;i<data.length;i++){
					option += "<option value='"+data[i].INVOICE_NO+"'>"+data[i].INVOICE_NO+"</option>";
				}
				itemInvoiceNoList.append(option);	
			    costInvoiceOrderId = $("#costInvoiceOrderId").val();	
				costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/yh/costInvoiceOrder/costPreInvoiceOrderList";   
				costPreInvoiceOrderTable.fnDraw();
			}
    	},'json');
			
	    invoiceItemTable.fnSettings().sAjaxSource = "/yh/costInvoiceOrder/costInvoiceItemList?costInvoiceOrderId="+costInvoiceOrderId;   
	    invoiceItemTable.fnDraw();		
		costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/yh/costInvoiceOrder/costPreInvoiceOrderList";   
		costPreInvoiceOrderTable.fnDraw();
	});	

	$("#costPreInvoiceOrder-table").on('blur', 'select', function(e){
		e.preventDefault();
		var preInvoiceId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		var costInvoiceOrderId = $("#costInvoiceOrderId").val();
		$.post('/yh/costInvoiceOrder/updatePreInvoice', {costInvoiceOrderId:costInvoiceOrderId, preInvoiceId:preInvoiceId, name:name, value:value}, function(data){
			if(data.success){
			    var costInvoiceOrderId = $("#costInvoiceOrderId").val();	
				invoiceItemTable.fnSettings().sAjaxSource = "/yh/costInvoiceOrder/costInvoiceItemList?costInvoiceOrderId="+costInvoiceOrderId;   
			    invoiceItemTable.fnDraw();
			}
    	},'json');
	});	
} );