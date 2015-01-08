
$(document).ready(function() {
    $('#menu_assign').addClass('active').find('ul').addClass('in');
    var orderType = [];
    var officeType=[];
    
  //datatable, 动态处理
    var pickupOrder1 = $('#eeda-table1').dataTable({
    	"bSort": false, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": false,
        "bProcessing":false,
        "bInfo":false,
        "bPaginate":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "aoColumns": [
            {"mDataProp": "ORDER_NO","sClass": "order_no"},
            {"mDataProp":"OPERATION_TYPE","sWidth": "70px","sClass": "operation_type"},
    		{"mDataProp": "ROUTE_FROM","sWidth": "70px","sClass": "route_from"},
    		{"mDataProp": "ROUTE_TO","sWidth": "80px","sClass": "route_to"}, 
		    {"mDataProp":"ORDER_TYPE","sWidth": "70px","sClass": "order_type"},
            {"mDataProp":"CARGO_NATURE","sWidth": "70px","sClass": "cargo_nature"},
            {"mDataProp": "TOTAL_WEIGHT","sClass": "total_weight"},
            {"mDataProp": "TOTAL_VOLUME","sClass": "total_volume"},
            {"mDataProp": "TOTAL_AMOUNT","sClass": "total_amount"},
            {"mDataProp": "ADDRESS","sWidth": "100px","sClass": "address"},
            {"mDataProp":"PICKUP_MODE","sClass": "pickup_mode"},
    		{"mDataProp":"ARRIVAL_MODE","sClass": "arrival_mode"},
            {"mDataProp": "STATUS","sWidth": "60px","sClass": "status"},
            {"mDataProp": "CNAME","sWidth": "100px","sClass": "cname"},
            {"mDataProp": "OFFICE_NAME","sWidth": "120px","sClass": "office_name"},  
    		{"mDataProp": "CREATE_STAMP","sWidth": "150px","sClass": "create_stamp"},                                      
    		{ "mDataProp": "PICKUP_ASSIGN_STATUS","sClass": "assign_status"}                                  
        ]      
    });	
    
    //因为Btn动态生成，需要在这里做特殊响应处理
    $("#eeda-table").on('click', 'button.popover_btn', function(e){
        $(e.target).popover('show');
    });

	//datatable, 动态处理
    var pickupOrder = $('#eeda-table').dataTable({
    	"bSort": false, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/pickupOrder/createList",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                    return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
                 }
            },
            { 
            	"mDataProp": "ORDER_NO",
            	"sClass": "order_no",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "ATM"){
            			
            			return obj.aData.ORDER_NO + '<button type="button" class="btn btn-default popover_btn" data-toggle="popover" title="Popover title" data-content="And some amazing content. Itvery engaging. Right?">单品选择</button>';
            		}else{
            			return obj.aData.ORDER_NO;
            		}
                    
                 }
            },
            { 
            	"mDataProp": "CNAME",
                "sWidth": "100px",
            	"sClass": "cname"
            },
            {"mDataProp":"OPERATION_TYPE",
            	"sClass": "operation_type",
    			"fnRender": function(obj) {
    				if(obj.aData.OPERATION_TYPE == "out_source"){
    					return "外包";
    				}else if(obj.aData.OPERATION_TYPE == "own"){
    					return "自营";
    				}else{
    					return "";
    				}}
            },
    		{ 
            	"mDataProp": "ROUTE_FROM",
            	"sClass": "route_from"
            },
    		{ 
            	"mDataProp": "ROUTE_TO",
            	"sClass": "route_to"
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
            	"mDataProp": "OFFICE_NAME",
            	"sWidth": "120px",
            	"sClass": "office_name"
            },  
    		{ 
            	"mDataProp": "CREATE_STAMP",
            	"sWidth": "150px",
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
    	pickupOrder.fnSettings().sAjaxSource = "/pickupOrder/createList?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo+"&orderType="+orderType;
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
			$("#saveBtn").attr('disabled', false);
		}else{
			if(orderType.length == 0){
				$("#saveBtn").attr('disabled', true);				
			}
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
		var operation_type = $(this).parent().siblings('.operation_type')[0].textContent;		
		var route_from = $(this).parent().siblings('.route_from')[0].textContent;		
		var route_to = $(this).parent().siblings('.route_to')[0].textContent;		
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
		var create_stamp = $(this).parent().siblings('.create_stamp')[0].textContent;		
		var assign_status = $(this).parent().siblings('.assign_status')[0].textContent;
		var office_name = $(this).parent().siblings('.office_name')[0].textContent;
		var value = $(this).val();
		if($(this).prop('checked') == true){
			if(orderType.length != 0){
				if(orderType[0] != $(this).parent().siblings('.order_type')[0].innerHTML){
					alert("请选择相同的订单类型!");
					return false;
				}else{
					if(officeType[0]!=$(this).parent().siblings('.office_name')[0].innerHTML){
						//alert($(this).parent().siblings('.office_name')[0].innerHTML+"==="+officeType[0]);
						alert("请选择同一网点的运输单");
						return false;
					}
					orderType.push($(this).parent().siblings('.order_type')[0].innerHTML);
					officeType.push($(this).parent().siblings('.office_name')[0].innerHTML);
				}
				
			}else{
				if($(this).parent().siblings('.order_type')[0].innerHTML != ''){
					orderType.push($(this).parent().siblings('.order_type')[0].innerHTML);
					officeType.push($(this).parent().siblings('.office_name')[0].innerHTML);
				}
				ckeckedTransferOrderList.empty();
			}
			sumValue();
			if(arrival_mode == 'ATM'){
				ckeckedTransferOrderList.append("<tr value='"+$(this).val()+"'><td>"+order_no+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
					+total_amount+"</td><td>"+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
			}else{
				if(total_amount == 1){
					ckeckedTransferOrderList.append("<tr value='"+$(this).val()+"'><td>"+order_no+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
						+total_amount+"</td><td>"+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
				}else{
					ckeckedTransferOrderList.append("<tr value='"+$(this).val()+"'><td>"+order_no+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td><input type='text' size='5' name='total_weight' number="+value+" value="+total_weight+"></td><td><input type='text'  size='5' number="+value+"  name='total_volume' value="+total_volume+"></td><td>"
						+"<input type='text'  size='5' number="+value+"  name='total_amount' value="+total_amount+"></td><td>"+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
				}
			}
			
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
				officeType.splice($(this).parent().siblings('.office_name')[0].innerHTML,1);
			}
		}
	});
	
	
	 //获取所有客户
	 $('#customer_filter').on('keyup click', function(){
	        var inputStr = $('#customer_filter').val();
	        
	        $.get("/customerContract/search", {locationName:inputStr}, function(data){
	            console.log(data);
	            var companyList =$("#companyList");
	            companyList.empty();
	            for(var i = 0; i < data.length; i++)
	            {
	                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
	            }
	            if(data.length>0)
	                companyList.show();
	            
	        },'json');

	        if(inputStr==''){
	        	pickupOrder.fnFilter('', 2);
	        }
	        
	    });


	//选中某个客户时候
	   $('#companyList').on('click', '.fromLocationItem', function(e){        
	        $('#customer_filter').val($(this).text());
	        $("#companyList").hide();
	        //var companyId = $(this).attr('partyId');
	        //$('#customerId').val(companyId);
	        //过滤回单列表
	        //chargeCheckTable.fnFilter(companyId, 2);
	        var inputStr = $('#customer_filter').val();
	        if(inputStr!=null){
	        	var orderNo = $("#orderNo_filter").val();
	        	var status = $("#status_filter").val();
	        	var address = $("#address_filter").val();
	        	var customer = $("#customer_filter").val();
	        	var beginTime = $("#beginTime_filter").val();
	        	var endTime = $("#endTime_filter").val();
	        	var routeFrom = $("#routeFrom_filter").val();
	        	var routeTo = $("#routeTo_filter").val();
	        	var orderType = $("#orderType_filter").val();
	        	pickupOrder.fnSettings().sAjaxSource = "/pickupOrder/createList?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo+"&orderType="+orderType;
	        	pickupOrder.fnDraw(); 
	        }
	    });
	 // 没选中客户，焦点离开，隐藏列表
	    $('#customer_filter').on('blur', function(){
	        $('#companyList').hide();
	    });

	    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
	    $('#companyList').on('blur', function(){
	        $('#companyList').hide();
	    });

	    $('#companyList').on('mousedown', function(){
	        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	    });
	    
	    //修改选中货品数量
	    //TODO 我现在做到改普货数量进行调车，主要问题是你说的那个弹出框没用，弹不出来，内容的话也没怎么看懂怎么加进去，你解决这两个就可以了
	    $('#eeda-table1').on('blur', 'input', function(e){   
	    	var input = $(this);
	    	var itemId = input.attr("number");
			var fieldName = input.attr("name");
			var value = input.val();
			if(value != "" && !isNaN(value)){
				$.get("/transferOrderItem/updateProductNumber", {itemId:itemId,fieldName:fieldName,value:value}, function(data){
					 console.log("返回数据,weight:"+data.weight+",volume:"+data.volume);
					 input.parent().parent().find("td").find("input[name='total_volume']").val(data.volume);
					 input.parent().parent().find("td").find("input[name='total_weight']").val(data.weight);
		        },'json');
			}
	    });
	    
	    
	    
	    
	    
	    
	    
	    
} );
