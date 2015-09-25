$(document).ready(function() {
	if(order_no){
		document.title = order_no+' | '+document.title;
	}
	$('#menu_charge').addClass('active').find('ul').addClass('in');
	
	
	if($("#chargeCheckOrderId").val() == ""){
		$('#auditBtn').attr('disabled', true);
	}else{
		if($("#chargeCheckOrderStatus").text() == "已确认"){
			$('#auditBtn').attr('disabled', true);
			$('#saveChargeCheckOrderBtn').attr('disabled', true);
		}else if($("#chargeCheckOrderStatus").text() == "开票申请中"){
			$('#auditBtn').attr('disabled', true);
			$('#saveChargeCheckOrderBtn').attr('disabled', true);
		}else{
			$('#auditBtn').attr('disabled', false);
		}
	}
	 
	var saveChargeCheckOrder = function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeCheckOrderForm").valid()){
	       	return;
        }
        //数据响应之前回调按钮
        $('#saveChargeCheckOrderBtn').attr('disabled', true);

        var tableRows = $("#chargeConfirem-table tr");
        var itemsArray=[];
        for(var index=0; index<tableRows.length; index++){
            if(index==0)
                continue;

            var row = tableRows[index];
            var get_item_change_amount  = function(td){
                var element = td.find('input');
                if(element.length>0){
                    return element.val();
                }else{
                    return td.text();
                }
            };
            var item={
                ORDER_ID: $(row).attr('id'), 
                ORDER_TYPE: $(row).attr('order_type'),
                AMOUNT: $(row.children[2]).text(),
                CHANGE_AMOUNT: get_item_change_amount($(row.children[3]))
            };
            itemsArray.push(item);
        }

        var total_amount = 0.00;
        var change_amount = 0;
        for(var i=0; i<itemsArray.length; i++){
            total_amount += Number(itemsArray[i].AMOUNT);
            change_amount += Number(itemsArray[i].CHANGE_AMOUNT);
            $('#chargeAmount').html(total_amount);
        }

        var order={
            chargeCheckOrderId: $('#chargeCheckOrderId').val(),
            customer_id: $('#customer_id').val(),
            remark: $('#remark').val(),
            total_amount: total_amount,
            change_amount: change_amount,
            items: itemsArray
        };

        console.log(order);
		//异步向后台提交数据
		$.post('/chargeCheckOrder/save', {params:JSON.stringify(order)}, function(data){
			if(data.ID>0){
				$("#chargeCheckOrderId").val(data.ID);
				$("#chargeAmount")[0].innerHTML = data.CHARGE_AMOUNT;
				$('#auditBtn').attr('disabled', false);
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				contactUrl("edit?id",data.ID);
				$("#arap_order_no").text(data.ORDER_NO);
				$('#saveChargeCheckOrderBtn').attr('disabled', false);
			}else{
				alert('数据保存失败。');
			}
		},'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
        });
	};
    
	// 审核
	$("#auditBtn").click(function(e){
		$('#auditBtn').attr('disabled', true);
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		$('#saveChargeCheckOrderBtn').attr('disabled', true);
		var chargeCheckOrderId = $("#chargeCheckOrderId").val();
		$.post('/chargeCheckOrder/auditChargeCheckOrder', {chargeCheckOrderId:chargeCheckOrderId}, function(data){
			$("#chargeCheckOrderStatus").html(data.STATUS);
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
	var parentId = "chargeCheckOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#chargeCheckOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveChargeCheckOrderBtn").click(function(e){
 		//数据响应之前回调按钮
        $('#saveChargeCheckOrderBtn').attr('disabled', true);
 		saveChargeCheckOrder(e);

 		//$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	});
	
	// $("#chargeCheckOrderItem").click(function(e){
	// 	//阻止a 的默认响应行为，不需要跳转
	// 	e.preventDefault();
	// 	//提交前，校验数据
 //        if(!$("#chargeCheckOrderForm").valid()){
	//        	return;
 //        }
	// 	//异步向后台提交数据
	// 	$.post('/chargeCheckOrder/save', $("#chargeCheckOrderForm").serialize(), function(data){
	// 		if(data.ID>0){
	// 			$("#chargeCheckOrderId").val(data.ID);
	// 		  	//$("#style").show();
	// 		  	$("#departureConfirmationBtn").attr("disabled", false);
	// 		  	if("chargeCheckOrderbasic" == parentId){
	// 		  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	// 		  	}
	// 		}else{
	// 			alert('数据保存失败。');
	// 		}
	// 	},'json');
	// 	parentId = e.target.getAttribute("id");
	// });
	
    if($("#chargeCheckOrderStatus").text() == 'new'){
    	$("#chargeCheckOrderStatus").text('新建');
	}
   
    var chargeConfiremTable = $('#chargeConfirem-table').dataTable({
        //"bPaginate": false, //翻页功能
        //"bInfo": false,//页脚信息
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, // 不要排序
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        //"bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/chargeCheckOrder/returnOrderList",
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
            $(nRow).attr('id', aData.ID);
            $(nRow).attr('order_type', aData.TPORDER);
            return nRow;
        },
        "aoColumns": [  
            {"mDataProp":"ID", "bVisible":false},
            {"mDataProp":"TPORDER", "bVisible":false},
            {"mDataProp":"ORDER_NO", "sWidth":"100px",
            	"fnRender": function(obj) {
            		if(Return.isUpdate || Return.isComplete){
            			return "<a href='/returnOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		}else{
            			return obj.aData.ORDER_NO;
            		}
        		}},
		    {"mDataProp":null, "sWidth":"100px",
                "fnRender": function(obj) {
                    return "未收款";
                }
            },
            {"mDataProp":"CHARGE_TOTAL_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":"CHANGE_AMOUNT", "sWidth":"120px",
                "fnRender": function(obj) {
                    // return "<input style='width: 100%;' type='text' name='change_amount' value='0'/>";
                    if( obj.aData.TPORDER == "收入单"||$("#chargeCheckOrderStatus").text()=="已确认"||$("#chargeCheckOrderStatus").text()=="收款申请中"
                        ||$("#chargeCheckOrderStatus").text()=="收款确认中"||$("#chargeCheckOrderStatus").text()=="已收款确认"){
                        if(obj.aData.CHANGE_AMOUNT!=''&& obj.aData.CHANGE_AMOUNT != null){
                            return obj.aData.CHANGE_AMOUNT;  
                        }
                        else {
                            return obj.aData.CHARGE_TOTAL_AMOUNT;
                        }
                    }
                    else{
                        if(obj.aData.CHANGE_AMOUNT!=''&& obj.aData.CHANGE_AMOUNT != null ){
                            return "<input style='width: 100%;' type='text' name='change_amount' id='change' value='"+obj.aData.CHANGE_AMOUNT+"'/>";
                            
                        }
                        else {
                            if(obj.aData.CHARGE_TOTAL_AMOUNT!=null){
                                return "<input style='width: 100%;' type='text' name='change_amount' value='"+obj.aData.CHARGE_TOTAL_AMOUNT+"'/>";
                            }
                            else{
                                return "<input style='width: 100%;' type='text' name='change_amount' value='0'/>";
                            }
                        }
                    }
                }
            },
            {"mDataProp":"CNAME", "sWidth":"200px"},
            {"mDataProp":null, "sWidth":"150px"},
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"120px"},
            {"mDataProp":"DELIVERY_ORDER_NO", "sWidth":"120px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"120px"},        	
            {"mDataProp":"TRANSACTION_STATUS", "sWidth": "120px", 
                "fnRender": function(obj) {
                    if(obj.aData.TRANSACTION_STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.TRANSACTION_STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.TRANSACTION_STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.TRANSACTION_STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.TRANSACTION_STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.TRANSACTION_STATUS;
                }
            },           
            {"mDataProp":"RECEIPT_DATE", "sWidth":"150px"},        	
            {"mDataProp":"ROUTE_FROM", "sWidth":"100px"},                        
            {"mDataProp":"ROUTE_TO", "sWidth":"100px"},                        
            /*{"mDataProp":null, "sWidth":"150px"},                         
            {"mDataProp":null, "sWidth":"100px"},*/                        
            {"mDataProp":"CONTRACT_AMOUNT", "sWidth":"150px"},                        
            //{"mDataProp":"PICKUP_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                       
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":"INSURANCE_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":"STEP_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":"WAREHOUSE_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"}, 
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":null, "sWidth":"200px"}                       
        ]      
    });	
    
    $("#chargeCheckOrderItem").click(function(){
    	chargeConfiremTable.fnSettings().sAjaxSource = "/chargeCheckOrder/returnOrderList?order="+$("#order").val()+"&returnOrderIds="+$("#returnOrderIds").val();
    	chargeConfiremTable.fnDraw();   
    });
    
    var chargeMiscListTable = $('#chargeMiscList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
          "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeCheckOrder/checkChargeMiscList",
        "aoColumns": [   
            {"mDataProp":"MISC_ORDER_NO", "sWidth":"80px",
                "fnRender": function(obj) {
                    return "<a href='/chargeMiscOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.MISC_ORDER_NO+"</a>";
                }},
            {"mDataProp":"CNAME", "sWidth":"200px"},
            {"mDataProp":"NAME", "sWidth":"200px"},
            {"mDataProp":"AMOUNT", "sWidth":"100px"},
            {"mDataProp":"REMARK", "sWidth":"200px"}                        
        ]      
    });
    
    $("#chargeMiscList").click(function(){
    	chargeMiscListTable.fnSettings().sAjaxSource = "/chargeCheckOrder/checkChargeMiscList?chargeCheckOrderId="+$("#chargeCheckOrderId").val();
    	chargeMiscListTable.fnDraw();  
    });

    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#departure_time').trigger('keyup');
    });	 
    
    $('#datetimepicker2').datetimepicker({  
    	format: 'yyyy-MM-dd',  
    	language: 'zh-CN', 
    	autoclose: true,
    	pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
    	$('#arrival_time').trigger('keyup');
    });	
    
    var externalTab = $('#external-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeCheckOrder/externalMiscOrderList",
        "aoColumns": [      
	        { "mDataProp": null,
	            "fnRender": function(obj) {
	              return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
	            }
	        }, 
            {"mDataProp":"ORDER_NO","sWidth": "80px",
            	"fnRender": function(obj) {
        			return "<a href='/chargeMiscOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"TYPE","sWidth": "100px",
            	"fnRender": function(obj) {
                    if(obj.aData.TYPE=='ordinary_receivables'){
                        return '普通收款';
                    }else if(obj.aData.TYPE=='offset_payment'){
                        return '抵销货款';
                    }
                    return obj.aData.TYPE;
                }
            },
            {"mDataProp":"STATUS","sWidth": "100px",
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
            {"mDataProp":"CREATE_STAMP","sWidth": "150px"},
            {"mDataProp":"CHARGE_ORDER_NO","sWidth": "150px"},
            {"mDataProp":"REMARK","sWidth": "150px"}                       
        ]      
    });	

    $("#addExternalMiscOrderBtn").click(function(){
    	externalTab.fnSettings().sAjaxSource = "/chargeCheckOrder/externalMiscOrderList";
    	externalTab.fnDraw();  
    });    

    var ids = [];
    // 未选中列表
	$("#external-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
			ids.push($(this).val());
			$("#micsOrderIds").val(ids);
		}			
	});
	
	// 已选中列表
	$("#external-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == false){
			if(ids.length != 0){
				ids.splice($.inArray($(this).val(),ids),1);
				$("#micsOrderIds").val(ids);
			}
		}			
	});
	
	$("#addExternalFormBtn").click(function(){
		var micsOrderIds = $("#micsOrderIds").val();
		var chargeCheckOrderId = $("#chargeCheckOrderId").val();
		$.post('/chargeCheckOrder/updateChargeMiscOrder', {micsOrderIds: micsOrderIds, chargeCheckOrderId: chargeCheckOrderId}, function(data){
			if(data.ID > 0){
				$("#debitAmountSpan")[0].innerHTML = data.DEBIT_AMOUNT;
				$("#debitAmount")[0].innerHTML = data.DEBIT_AMOUNT;
				$("#chargeAmount")[0].innerHTML = data.CHARGE_AMOUNT;
				$("#hiddenDebitAmount").val(data.DEBIT_AMOUNT);
				
				$('#addExternalMiscOrder').modal('hide');
		    	chargeMiscListTable.fnSettings().sAjaxSource = "/chargeCheckOrder/checkChargeMiscList?chargeCheckOrderId="+$("#chargeCheckOrderId").val();
		    	chargeMiscListTable.fnDraw();  
			}
		},'json');
	});
	
	
	
	
} );