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
	/*--------------------------------------------------------------------*/
	
    /*var returnOrderIds = $("#returnOrderIds").val();
	var returnOrderTable =$('#example').dataTable( {
	    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",insert into return_order(status_code,create_date,order_type,creator,remark,transfer_order,distribution_order_id,contract_id
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/costCheckOrder/returnOrderList?returnOrderIds="+returnOrderIds,
   			"aoColumns": [
   			{ "mDataProp": "ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/yh/returnOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
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
	});*/
} );