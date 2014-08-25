
$(document).ready(function() {
    $('#menu_assign').addClass('active').find('ul').addClass('in'); 
    var spName = [];
    var routeArr = [];
    var spNameUnchecked = [];
	//datatable, 动态处理
    var datatable = $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/departOrder/createTransferOrderList",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                	 return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
                 }
            },
            { "mDataProp": "ORDER_NO"},
            { "mDataProp":"OPERATION_TYPE",
            	"sClass": "operation_type",
    			"fnRender": function(obj) {
    				if(obj.aData.OPERATION_TYPE == "out_source"){
    					return "外包";
    				}else if(obj.aData.OPERATION_TYPE == "own"){
    					return "自营";
    				}else{
    					return "";
    				}}}, 
            { "mDataProp":"CARGO_NATURE",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return "ATM";
            		}else{
            			return "";
            		}}},
            { "mDataProp": "DOADDRESS"},
            { "mDataProp": "ARRIVAL_MODE",
            	"fnRender": function(obj) {
            		if(obj.aData.ARRIVAL_MODE=="delivery"){
            			return "直送";
            		}else{
            			return "入中转仓";
            		}
            	},
            },
            {"mDataProp":"PICKUP_MODE",
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
            { 
            	"mDataProp": "SPNAME",
            	"sClass": "spname"
            },
            { 
            	"mDataProp": "ROUTE_FROM",
                "sClass": "route_from"
            },
            { 
            	"mDataProp": "ROUTE_TO",
                "sClass": "route_to"
            }
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
    
    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $('#beginTime_filter').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $('#endTime_filter').trigger('keyup');
    });
    
    $("#routeTo_filter ,#endTime_filter ,#beginTime_filter ,#routeFrom_filter ,#customer_filter ,#address_filter ,#status_filter ,#orderNo_filter").on( 'keyup click', function () {    	 	
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var routeFrom = $("#routeFrom_filter").val();
    	var routeTo = $("#routeTo_filter").val();
    	datatable.fnSettings().sAjaxSource = "/yh/departOrder/createTransferOrderList?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo;
    	datatable.fnDraw();
    	
      });
    
    $("#eeda-table").on('click', '.checkedOrUnchecked', function(e){
    	if(spName.length == 0){
    		$("#saveBtn").attr('disabled', true);
    	}
		if($(this).prop("checked") == true){
			$("#saveBtn").attr('disabled', false);
			if(spName.length != 0){
				if(spName[0] != $(this).parent().siblings('.spname')[0].innerHTML && $(this).parent().siblings('.spname')[0].innerHTML != ''){
					alert("请选择同一供应商!");
					return false;
				}else{
					if(routeArr.length != 0){
						if(routeArr[0] != $(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML && $(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML != ''){
							alert("请选择同一线路的运输单!");
							return false;
						}else{
							spName.push($(this).parent().siblings('.spname')[0].innerHTML);
							routeArr.push($(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML);
						}
					}else{
						if($(this).parent().siblings('.operation_type')[0].innerHTML != ''){
							routeArr.push($(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML);
						}
					}
				}
			}else{
				if($(this).parent().siblings('.spname')[0].innerHTML != ''){
					spName.push($(this).parent().siblings('.spname')[0].innerHTML);
					routeArr.push($(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML);
				}
			}
		}else{
			if(spName.length != 0){
				spName.splice($(this).parent().siblings('.spname')[0].innerHTML, 1);
			}
			if(routeArr.length != 0){
				routeArr.splice($(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML, 1);
			}
			if(spName.length == 0){
				$("#saveBtn").attr('disabled', true);
			}
		}
	});
});
