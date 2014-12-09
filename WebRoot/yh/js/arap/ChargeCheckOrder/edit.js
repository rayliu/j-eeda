$(document).ready(function() {
	$('#menu_charge').addClass('active').find('ul').addClass('in');
	
	var saveChargeCheckOrder = function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeCheckOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/chargeCheckOrder/save', $("#chargeCheckOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#chargeCheckOrderId").val(data.ID);
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
		var chargeCheckOrderId = $("#chargeCheckOrderId").val();
		$.post('/chargeCheckOrder/auditChargeCheckOrder', {chargeCheckOrderId:chargeCheckOrderId}, function(data){
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
 		
 		saveChargeCheckOrder(e);

 		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	});
	
	$("#chargeCheckOrderItem").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeCheckOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/chargeCheckOrder/save', $("#chargeCheckOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#chargeCheckOrderId").val(data.ID);
			  	//$("#style").show();
			  	$("#departureConfirmationBtn").attr("disabled", false);
			  	if("chargeCheckOrderbasic" == parentId){
			  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  	}
			}else{
				alert('数据保存失败。');
			}
		},'json');
		parentId = e.target.getAttribute("id");
	});
	
    if($("#chargeCheckOrderStatus").text() == 'new'){
    	$("#chargeCheckOrderStatus").text('新建');
	}

    var chargeConfiremTable = $('#chargeConfirem-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeCheckOrder/returnOrderList",
        "aoColumns": [  
            {"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
        		}},
		    {"mDataProp":null, "sWidth":"120px",
                "fnRender": function(obj) {
                    return "未收款";
            }},
            {"mDataProp":"CNAME", "sWidth":"200px"},
            {"mDataProp":"DEPARTURE_TIME", "sWidth":"150px"},
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"200px"},
            {"mDataProp":"DELIVERY_ORDER_NO", "sWidth":"200px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"200px"},        	
            {"mDataProp":null, "sWidth": "120px", 
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
            {"mDataProp":"PICKUP_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":"INSURANCE_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":"STEP_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":"WAREHOUSE_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":null, "sWidth":"200px"}                       
        ]      
    });	
    
    $("#chargeCheckOrderItem").click(function(){
    	chargeConfiremTable.fnSettings().sAjaxSource = "/chargeCheckOrder/returnOrderList?returnOrderIds="+$("#returnOrderIds").val();
    	chargeConfiremTable.fnDraw();  
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
} );