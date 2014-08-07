
$(document).ready(function() {
    $('#menu_assign').addClass('active').find('ul').addClass('in');
    var orderType = [];
	//datatable, 动态处理
    var pickupOrder = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/pickupOrder/createList",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                    return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
                 }
            },
            { 
            	"mDataProp": "ORDER_NO",
            	"sClass": "order_no"
            },
		    {"mDataProp":"ORDER_TYPE",
            	"sClass": "order_type",
            	"fnRender": function(obj) {
            		if(obj.aData.ORDER_TYPE == "salesOrder"){
            			return "销售订单";
            		}else if(obj.aData.ORDER_TYPE == "replenishmentOrder"){
            			return "补货订单";
            		}else if(obj.aData.ORDER_TYPE == "arrangementOrder"){
            			return "调拨订单";
            		}else if(obj.aData.ORDER_TYPE == "cargoReturnOrder"){
            			return "退货订单";
            		}else if(obj.aData.ORDER_TYPE == "damageReturnOrder"){
            			return "质量退单";
            		}else if(obj.aData.ORDER_TYPE == "gateOutTransferOrder"){
            			return "出库运输单";
            		}else{
            			return "";
            }}},
            {"mDataProp":"CARGO_NATURE",
            	"sClass": "cargo_nature",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return "ATM";
            		}else{
            			return "";
            }}},
            { 
            	"mDataProp": "TOTAL_WEIGHT",
            	"sClass": "total_weight"
            },
            { 
            	"mDataProp": "TOTAL_VOLUME",
            	"sClass": "total_volume"
            },
            { 
            	"mDataProp": "TOTAL_AMOUNT",
            	"sClass": "total_amount"
            },
            { 
            	"mDataProp": "ADDRESS",
            	"sClass": "address"
            },
            {"mDataProp":"PICKUP_MODE",
            	"sClass": "pickup_mode",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == "routeSP"){
            			return "干线供应商自提";
            		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
            			return "外包供应商提货";
            		}else if(obj.aData.PICKUP_MODE == "own"){
            			return "源鸿自提";
            		}else{
            			return "";
            		}}},
    		{"mDataProp":"ARRIVAL_MODE",
                "sClass": "arrival_mode",
            	"fnRender": function(obj) {
            		if(obj.aData.ARRIVAL_MODE == "delivery"){
            			return "货品直送";
            		}else if(obj.aData.ARRIVAL_MODE == "gateIn"){
            			return "入中转仓";
            		}else{
            			return "";
            		}}},
            { 
            	"mDataProp": "STATUS",
            	"sClass": "status"
            },
            { 
            	"mDataProp": "CNAME",
            	"sClass": "cname"
            },
    		{ 
            	"mDataProp": "ROUTE_FROM",
            	"sClass": "route_from"
            },
    		{ 
            	"mDataProp": "ROUTE_TO",
            	"sClass": "route_to"
            },                                      
    		{ 
            	"mDataProp": "CREATE_STAMP",
            	"sClass": "create_stamp"
            },                                      
    		{ "mDataProp": "PICKUP_ASSIGN_STATUS",
            	"sClass": "assign_status",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_ASSIGN_STATUS == "NEW"){
            			return "未发车";
            		}else if(obj.aData.PICKUP_ASSIGN_STATUS == "PARTIAL"){
            			return "部分发车";
            		}else if(obj.aData.PICKUP_ASSIGN_STATUS == "ALL"){
            			return "全部发车";
            		}else{
            			return "";
            }}},                                     
        ]      
    });	
    
    $('#saveBtn').click(function(e){
        e.preventDefault();
    	var trArr=[];
        var tableArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		trArr.push($(this).val());
        	}
        });
        tableArr.push(trArr);        
        console.log(tableArr);
        $('#pickupOrder_message').val(tableArr);
        $('#createForm').submit();
    });
    
    /*$('input.orderNo_filter').on( 'keyup click', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var routeFrom = $("#routeFrom_filter").val();
    	var routeTo = $("#routeTo_filter").val();
    	var orderType = $("#orderType_filter").val();
    	pickupOrder.fnSettings().sAjaxSource = "/yh/pickupOrder/createList?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo+"&orderType="+orderType;
    	pickupOrder.fnDraw(); 
    } );*/
    
    $('input.orderNo_filter, input.status_filter, input.address_filter, input.customer_filter, input.routeFrom_filter, #beginTime_filter, #endTime_filter, input.routeTo_filter, input.orderType_filter').on( 'keyup click', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var routeFrom = $("#routeFrom_filter").val();
    	var routeTo = $("#routeTo_filter").val();
    	var orderType = $("#orderType_filter").val();
    	pickupOrder.fnSettings().sAjaxSource = "/yh/pickupOrder/createList?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo+"&orderType="+orderType;
    	pickupOrder.fnDraw(); 
    } );
 
    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter').trigger('keyup');
    });
    
	$("#eeda-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
			orderType.push($(this).parent().siblings('.order_type')[0].innerHTML);
			$("#saveBtn").attr('disabled', false);
		}
	});
	
	var sumValue = function(){
		var sumWeightVal = 0;
		var sumVolumnVal = 0;
		$("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		sumWeightVal = sumWeightVal + parseFloat($(this).parent().siblings('.total_weight')[0].textContent == "" ? 0 : $(this).parent().siblings('.total_weight')[0].textContent);
        		sumVolumnVal = sumVolumnVal + parseFloat($(this).parent().siblings('.total_volume')[0].textContent == "" ? 0 : $(this).parent().siblings('.total_volume')[0].textContent);
        	}
        });
		$("#sumWeight").text(sumWeightVal);	
		$("#sumVolume").text(sumVolumnVal);
	};
	
	// 选中或取消事件
	$("#transferOrderList").on('click', '.checkedOrUnchecked', function(){
		var ckeckedTransferOrderList = $("#ckeckedTransferOrderList");
		var order_no = $(this).parent().siblings('.order_no')[0].textContent;		
		var order_type = $(this).parent().siblings('.order_type')[0].textContent;		
		var cargo_nature = $(this).parent().siblings('.cargo_nature')[0].textContent;		
		var total_weight = $(this).parent().siblings('.total_weight')[0].textContent;		
		var total_volume = $(this).parent().siblings('.total_volume')[0].textContent;		
		var total_amount = $(this).parent().siblings('.total_amount')[0].textContent;		
		var address = $(this).parent().siblings('.address')[0].textContent;		
		var pickup_mode = $(this).parent().siblings('.pickup_mode')[0].textContent;		
		var arrival_mode = $(this).parent().siblings('.arrival_mode')[0].textContent;		
		var status = $(this).parent().siblings('.status')[0].textContent;		
		var cname = $(this).parent().siblings('.cname')[0].textContent;		
		var route_from = $(this).parent().siblings('.route_from')[0].textContent;		
		var route_to = $(this).parent().siblings('.route_to')[0].textContent;		
		var create_stamp = $(this).parent().siblings('.create_stamp')[0].textContent;		
		var assign_status = $(this).parent().siblings('.assign_status')[0].textContent;		
		if($(this).prop('checked') == true){
			if(orderType.length != 0){
				if(orderType[0] != $(this).parent().siblings('.order_type')[0].innerHTML){
					alert("请选择相同的订单类型!");
					return false;
				}
			}
			sumValue();
			ckeckedTransferOrderList.append("<tr value='"+$(this).val()+"'><td>"+order_no+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
					+total_amount+"</td><td>"+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");			
		}else{
			sumValue();
			var allTrs = ckeckedTransferOrderList.children();
			for(var i=0;i<allTrs.length;i++){
				if(allTrs[i].attributes[0].value == $(this).val()){
					allTrs[i].remove();
				}
			}
			if(orderType.length != 0){
				orderType.splice($(this).parent().siblings('.order_type')[0].innerHTML, 1);
			}
		}
	});	
} );
