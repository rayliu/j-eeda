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

    var returnOrderIds = $("#returnOrderIds").val();
	var returnOrderTable =$('#example').dataTable( {
	    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",insert into return_order(status_code,create_date,order_type,creator,remark,transfer_order,distribution_order_id,contract_id
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeCheckOrder/returnOrderList?returnOrderIds="+returnOrderIds,
   			"aoColumns": [
   			{ "mDataProp": "ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/returnOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            { "mDataProp": "CNAME"},
            { "mDataProp": "TRANSFER_ORDER_NO"},
            { "mDataProp": "DELIVERY_ORDER_NO"},
            { "mDataProp": "CREATOR_NAME" },
            { "mDataProp": "CREATE_DATE" },
            { "mDataProp": "TRANSACTION_STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.TRANSACTION_STATUS=='new')
                        return '新建';
                    if(obj.aData.TRANSACTION_STATUS=='confirmed')
                        return '已确认';
                    if(obj.aData.TRANSACTION_STATUS=='cancel')
                        return '取消';
                    
                    return obj.aData.TRANSACTION_STATUS;
                 }
            },
            { "mDataProp": "REMARK" },
            { "mDataProp": "AMOUNT"	},
            { "mDataProp": null	,
            	"fnRender": function(obj) {
        			return "<input type='text' value='"+obj.aData.AMOUNT+"'>";
        		}}
         ]
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
} );