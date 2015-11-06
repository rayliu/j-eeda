$(document).ready(function() {
	document.title = '付款申请复核单| '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');
    
   

	//datatable, 动态处理
    var ids = $("#ids").val();
    var total = 0.00;
    var nopay = 0.00;
    var pay = 0.00;
    var datatable = $('#CostOrder-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bPaginate" : false, //显示分页器
        "bSort": false, // 不要排序
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costPreInvoiceOrder/costOrderList?ids="+ids+"&application_id="+$("#application_id").val(),
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID});
			//$(nRow).attr({payee_id:aData.PAYEE_ID});
		},
        "aoColumns": [   
             {"mDataProp":"ORDER_TYPE","sWidth": "100px","sClass":'order_type'},
             {"mDataProp":"ORDER_NO","sWidth": "120px",
            	"fnRender": function(obj) {
        			//return "<a href='/costCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		return obj.aData.ORDER_NO;
        		}},
        	{"mDataProp":"CNAME","sWidth": "250px"},
        	{"mDataProp":"PAYEE_NAME","sWidth": "120px"},
    		{"mDataProp":"COST_AMOUNT","sWidth": "100px",
    			"fnRender": function(obj) {
					total = total + parseFloat(obj.aData.COST_AMOUNT) ;
					$("#total").html(total);
//					$("#payee_filter").val(obj.aData.CNAME);
//					$("#payee_id").val(obj.aData.PAYEE_ID);
//					$("#payee_name").val(obj.aData.PAYEE_NAME);
					return obj.aData.COST_AMOUNT;
    			}
    		},
    		{"mDataProp":"YUFU_AMOUNT","sWidth": "100px","sClass":'yufu_amount',
    			"fnRender": function(obj) {
					nopay = nopay + parseFloat(obj.aData.YUFU_AMOUNT) ;
					$("#nopay").html(nopay);
					return obj.aData.YUFU_AMOUNT;
    			}
    		},
    		{"mDataProp":null,"sWidth": "100px",
    			"fnRender": function(obj) {
					if($('#application_id').val()==''){
						pay = pay + parseFloat(obj.aData.YUFU_AMOUNT) ;
						$("#pay").html(pay);
						$("#pay_amount").val(pay);
						return "<input type ='text' name='amount' style='width:80px' id ='amount' value='"+obj.aData.YUFU_AMOUNT+"'>";
					}
					else{
						if(obj.aData.PAY_AMOUNT==0){
	    					obj.aData.PAY_AMOUNT = obj.aData.YUFU_AMOUNT;
	    				}
						pay = pay + parseFloat(obj.aData.PAY_AMOUNT) ;
						$("#pay").html(pay);
						$("#pay_amount").val(pay);
						return "<input type ='text' name='amount' style='width:80px' id ='amount' value='"+obj.aData.PAY_AMOUNT+"'>";
					}
    			}
    		},
    		{"mDataProp":"CREATOR_NAME","sWidth": "120px"},
    		{"mDataProp":"CREATE_STAMP","sWidth": "150px"},
    		{"mDataProp":"REMARK","sWidth": "150px"}
        ]      
    });	
    
    var orderjson = function(){
    	var array=[];
    	var sum=0.0;
    	$("#CostOrder-table input[name='amount']").each(function(){
    		var obj={};
    		obj.id = $(this).parent().parent().attr('id');
    		obj.order_type = $(this).parent().parent().find('.order_type').text();
    		obj.value = $(this).val();
    		sum+=parseFloat(obj.value);
    		array.push(obj);
    	});
    	
    	$("#total_amount").val(sum);
    	var str_JSON = JSON.stringify(array);
    	console.log(str_JSON);
    	$("#detailJson").val(str_JSON);
    };
    
    
    
    

    //申请保存
  //付款确认
	$("#saveBtn").on('click',function(){
		$("#saveBtn").attr("disabled", true);
		$("#printBtn").attr("disabled", true);
	
		orderjson();
	
		if($("#payment_method").val()=='transfers'){
			if($("#deposit_bank").val()=='' && $("#bank_no").val()==''&& $("#account_name").val()==''){
				$.scojs_message('转账的信息不能为空', $.scojs_message.TYPE_FALSE);
				return false;
			}
		}
		$.get('/costPreInvoiceOrder/save',$("#checkForm").serialize(), function(data){
			if(data.ID>0){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#application_id").val(data.ID);
				$("#application_no").val(data.ORDER_NO);
				$("#application_date").val(data.CREATE_STAMP);
				$("#saveBtn").attr("disabled", false);
				$("#printBtn").attr("disabled", false);
				$("#checkBtn").attr('disabled',false);
				contactUrl("edit?id",data.ID);
				total = 0.00;
				nopay = 0.00;
				pay = 0.00;
				datatable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costOrderList?application_id="+$("#application_id").val();;
				datatable.fnDraw();
			}else{
				$.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
			}
		},'json');
		
	});
	
    //打印
	 $("#printBtn").on('click',function(){
	    	var order_no = $("#application_no").val();
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
	  $("#checkBtn").on('click',function(){
		  	$("#checkBtn").attr("disabled", true);
		  	$("#saveBtn").attr("disabled", true);
		  	
		  	orderjson();
		  	
			$.get("/costPreInvoiceOrder/checkStatus", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
				if(data.ID>0){
					$("#check_name").val();
					$("#check_stamp").val(data.CHECK_STAMP);
					$("#status").val(data.STATUS);
					$.scojs_message('复核成功', $.scojs_message.TYPE_OK);
					$("#returnBtn").attr("disabled", false);
					$("#confirmBtn").attr("disabled", false);
				}else{
					$("#checkBtn").attr("disabled", false);
					$.scojs_message('复核失败', $.scojs_message.TYPE_FALSE);
				}
			},'json');
		});
	  
	  
	  //撤回
	  $("#returnBtn").on('click',function(){
		  	$("#returnBtn").attr("disabled", true);
		  	orderjson();
			$.get("/costPreInvoiceOrder/returnOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
				if(data.success){
					$.scojs_message('退回成功', $.scojs_message.TYPE_OK);
					$("#checkBtn").attr("disabled", false);
				  	$("#saveBtn").attr("disabled", false);
				}else{
					$("#returnBtn").attr("disabled", false);
					$.scojs_message('退回失败', $.scojs_message.TYPE_FALSE);
				}
			},'json');
		});
	  
	  
	  //确认
	  $("#confirmBtn").on('click',function(){
		  	$("#confirmBtn").attr("disabled", true);
		  	
		  	orderjson();
		  	
			$.get("/costPreInvoiceOrder/confirmOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val(),pay_time:$('#pay_date').val(),pay_type:$('#pay_type').val(),pay_bank:$('#pay_bank').val()}, function(data){
				if(data.success){
					$("#returnBtn").attr("disabled", true);
					$.scojs_message('付款成功', $.scojs_message.TYPE_OK);
				}else{
					$("#confirmBtn").attr("disabled", false);
					$.scojs_message('付款失败', $.scojs_message.TYPE_FALSE);
				}
			},'json');
		});
	
	
	  
	 //异步显示总金额
    $("#CostOrder-table").on('input', 'input', function(e){
		e.preventDefault();
		var value = 0.00;
		var currentValue = $(this).val();
		var $totalAmount = $(this).parent().parent().find('.yufu_amount').text();
		if(parseFloat($totalAmount)-parseFloat(currentValue)<0){
			$(this).val(0);
			$.scojs_message('支付金额不能大于待付金额', $.scojs_message.TYPE_FALSE);
			return false;
		}
		$("input[name='amount']").each(function(){
			if($(this).val()!=null&&$(this).val()!=''){
				value = value + parseFloat($(this).val());
			}else{
				$("#InvorceApplication-table").on('blur', 'input', function(e){
					$(this).val(0);
				});
			}
	    });		
		$("#pay").html(value);
		$("#pay_amount").val(value);
	});	
    
 
    
    var a = '';
    var payment = function(){
    	if($('#payment_method').val()=='transfers'){
    		if(a!=''){
    			$("#transfers_massage").html(a);
    		}
    	}else if($('#payment_method').val()=='cash'){
    		a = $("#transfers_massage").html();
    		$("#transfers_massage").html("<div class='form-group'></div>"+"<div class='form-group'></div>"+"<div class='form-group'></div>");
    	}
    }; 	
    
  //付款方式文本框控制
    $('#payment_method').on('change',function(){
    	payment();
    });
    
    
    
    var payType = function(){
    	if($('#pay_type').val()=='cash'){
    		$("#pay_bank").val('');
    		$("#pay_type_massage").hide();
    	}else{
    		$("#pay_type_massage").show();
    	}
    };
    //付款方式（付款确认）控制
    $('#pay_type').on('change',function(){
    	payType();
    });
    
   
    //按钮控制

	if($('#status').val()=='new'){
		$("#saveBtn").attr('disabled',false);
	}else if($('#status').val()=='新建' || $('#status').val()=='已审批'){
		if($('#application_id').val()!=''){
			$("#saveBtn").attr('disabled',false);
			$("#printBtn").attr('disabled',false);
			$("#checkBtn").attr('disabled',false);
		}
	}else if($('#status').val()=='已复核'){
		$("#printBtn").attr('disabled',false);
		$("#returnBtn").attr('disabled',false);
		$("#confirmBtn").attr('disabled',false);
	}else if($('#status').val()=='已付款'){
		$("#printBtn").attr('disabled',false);
	}
//	
//	//根据供应商信息回显账户银行等
//	if($('#application_id').val()==''){
//		var payee_id = $('#payee_id').val();
//		if(payee_id!==''&&payee_id!=null){
//			$.get('/costPreInvoiceOrder/getSpMassage',{payee_id:payee_id},function(data){
//				if(data.ID>0){
//					$('#deposit_bank').val(data.BANK_NAME);
//					$('#bank_no').val(data.BANK_NO);
//					$('#account_name').val(data.RECEIVER);
//				}
//			});
//		}
//	}
//	
	


    //回显
    //付款类型
    $('#payment_method').val($('#payment_method_show').val());
    
    //开票类型
    $('#invoice_type').val($('#invoice_type_show').val());
    
    ////付款方式（付款确认）回显控制
    $('#pay_type').val($('#pay_type_show').val());
    payType();
    
    
    //付款银行回显
    $('#pay_bank').val($('#pay_banks').val());
    
    if($('#deposit_bank').val()!='' || $('#bank_no').val()!='' || $('#account_name').val()!=''){
    	$('#payment_method').val('transfers');
    }else{
    	$('#payment_method').val('cash');
    	payment();
    }
});