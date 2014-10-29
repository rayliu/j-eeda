$(document).ready(function() {
	$('#menu_cost').addClass('active').find('ul').addClass('in');
    
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveCostCheckOrderBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		
		//异步向后台提交数据
		$.post('/yh/costCheckOrder/save', $("#costCheckOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#costCheckOrderId").val(data.ID);
			  	$("#style").show();
			  	$("#departureConfirmationBtn").attr("disabled", false);
			}else{
				alert('数据保存失败。');
			}
		},'json');
	});
	
    if($("#costCheckOrderStatus").text() == 'new'){
    	$("#costCheckOrderStatus").text('新建');
	}
    
	// 审核
	$("#auditBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var costCheckOrderId = $("#costCheckOrderId").val();
		$.post('/yh/costCheckOrder/auditCostCheckOrder', {costCheckOrderId:costCheckOrderId}, function(data){
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
	var parentId = "costCheckOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#costCheckOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	
	//datatable, 动态处理
    var costConfiremTable = $('#costConfirem-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/costConfirmList/list",
        "aoColumns": [ 
            { "mDataProp": null, "sWidth":"20px",
                "fnRender": function(obj) {
                  return '<input type="checkbox" name="order_check_box" id="'+obj.aData.ID+'" order_no="'+obj.aData.ORDER_NO+'">';
                }
            },
            {"mDataProp":"BUSINESS_TYPE", "sWidth":"100px"},            	
            {"mDataProp":"SPNAME", "sWidth":"200px"},
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
            {"mDataProp":"RETURN_ORDER_COLLECTION", "sWidth":"100px"},  
		    {"mDataProp":null, "sWidth":"120px",
                "fnRender": function(obj) {
                    return "未收款";
            }},
            {"mDataProp":"ORDER_NO", "sWidth":"200px"},
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"200px"},
            {"mDataProp":"CREATE_STAMP", "sWidth":"200px"},                 	
            {"mDataProp":"AMOUNT", "sWidth":"150px"},                        
            {"mDataProp":"VOLUME", "sWidth":"150px"},                        
            {"mDataProp":"WEIGHT", "sWidth":"100px"},                        
            {"mDataProp":"PAY_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                       
            {"mDataProp":"REMARK", "sWidth":"150px"}                         
        ]      
    });	
    
    $("#arapAuditList").click(function(){
    	var costCheckOrderId = $("#costCheckOrderId").val();
    	var orderNos = $("#orderNos").val();
    	var orderIds = $("#orderIds").val();
    	costConfiremTable.fnSettings().sAjaxSource = "/yh/costCheckOrder/costConfirmList?costCheckOrderId="+costCheckOrderId+"&orderNos="+orderNos+"&orderIds="+orderIds;
    	costConfiremTable.fnDraw(); 
    });
} );