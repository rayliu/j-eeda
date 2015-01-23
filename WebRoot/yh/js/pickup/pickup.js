
$(document).ready(function() {
    $('#menu_assign').addClass('active').find('ul').addClass('in');
    var orderType = [];
    var officeType=[];
    //单品id集合-正式
    var detailIds = [];
    //单品id集合-临时\修改后
    var detailIdsTest = [];
    //单品序列号集合-临时\修改后
    var detailSerialTest = [];
    //单品id集合-临时\修改前
    var detailIdsTestOld = [];
    //单品序列号集合-临时\修改前
    var detailSerialTestOld = [];
    //普货选取数量-正式
    var amounts = [];
    //普货选取前货品数量-临时
    var amountsTest = [];
    //判断全选box是否选中
    var number = 0;
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
            {"mDataProp": "ORDER_NO","sWidth": "80px","sClass": "order_no"},
            {"mDataProp": "AMOUNT","sWidth": "120px","sClass": "serial_no"},
            {"mDataProp":"OPERATION_TYPE","sWidth": "70px","sClass": "operation_type"},
    		{"mDataProp": "ROUTE_FROM","sWidth": "70px","sClass": "route_from"},
    		{"mDataProp": "ROUTE_TO","sWidth": "80px","sClass": "route_to"}, 
		    {"mDataProp":"ORDER_TYPE","sWidth": "70px","sClass": "order_type"},
            {"mDataProp":"CARGO_NATURE","sWidth": "70px","sClass": "cargo_nature"},
            {"mDataProp": "TOTAL_WEIGHT","sWidth": "60px","sClass": "total_weight"},
            {"mDataProp": "TOTAL_VOLUME","sWidth": "60px","sClass": "total_volume"},
            {"mDataProp": "TOTAL_AMOUNT","sWidth": "60px","sClass": "total_amount"},
            {"mDataProp": "ADDRESS","sWidth": "100px","sClass": "address"},
            {"mDataProp":"PICKUP_MODE","sWidth": "80px","sClass": "pickup_mode"},
    		{"mDataProp":"ARRIVAL_MODE","sWidth": "80px","sClass": "arrival_mode"},
            {"mDataProp": "STATUS","sWidth": "60px","sClass": "status"},
            {"mDataProp": "CNAME","sWidth": "100px","sClass": "cname"},
            {"mDataProp": "OFFICE_NAME","sWidth": "120px","sClass": "office_name"},  
    		{"mDataProp": "CREATE_STAMP","sWidth": "150px","sClass": "create_stamp"},                                      
    		{ "mDataProp": "PICKUP_ASSIGN_STATUS","sWidth": "80px","sClass": "assign_status"}                                  
        ]      
    });	
    //因为Btn动态生成，需要在这里做特殊响应处理
    $("#eeda-table").on('click', 'button.popover_btn', function(e){
        var btn = $(e.target);
        var popup = btn.parent().find('.popover');
        if(popup.length>0){
            btn.popover('toggle');
        }else{
            btn.popover('destroy');
            btn.popover('show');
        }
    	
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
            	"sWidth": "100px",
            	"sClass": "order_no",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "ATM"){
            			var str1 = '<button type="button" name="selectDetailBtn" class="btn btn-default sm selectDetailBtn" data-toggle="modal" data-target="#myModal" value="'+obj.aData.ID+'">选择单品</button>';
            			return obj.aData.ORDER_NO + str1;
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
            	"sWidth": "80px",
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
            	"sWidth": "80px",
            	"sClass": "route_from"
            },
    		{ 
            	"mDataProp": "ROUTE_TO",
            	"sWidth": "80px",
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
    
    //货品表
    var itemTable = $('#itemTable').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 8,
    	"bServerSide": false,
    	"bLengthChange":false,
    	"bPaginate":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
		//"sAjaxSource": "#",
        "aoColumns": [
            //{"mDataProp": "ORDER_NO"},
            {"mDataProp": "ITEM_NO"},
    		{"mDataProp": null,
    			"fnRender": function(obj) {
    				var cargoNature = $("#transferCrgoNature").val();
    				if(cargoNature == "ATM"){
    					return obj.aData.ATMAMOUNT;
    				}else{
    					if(obj.aData.PICKUP_NUMBER != null && obj.aData.PICKUP_NUMBER != ''){
    						var number = obj.aData.AMOUNT - obj.aData.PICKUP_NUMBER ;
    						amountsTest.push(number);
    						return "<input type='text' name='amount' size='3' value='"+number+"' oldValue='"+number+"'>";
    					}else{
    						amountsTest.push(obj.aData.AMOUNT);
    						return "<input type='text' name='amount' size='3' value='"+obj.aData.AMOUNT+"' oldValue='"+obj.aData.AMOUNT+"'>";
    					}
    				}
    			}
    		},
    		{"mDataProp": "UNIT"}, 
            {"mDataProp": "SUM_WEIGHT"},
            {"mDataProp": "SUM_VOLUME"},
            {"mDataProp":"REMARK"}
        ]      
    });	
    
    //单品表
    var detailTable = $('#detailTable').dataTable({
    	"bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 8,
    	"bServerSide": false,
    	"bLengthChange":false,
    	"bPaginate":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/pickupOrder/createList",
        "aoColumns": [
			{ "mDataProp": null,
				"sWidth": "50px",
			    "fnRender": function(obj) {
			    	var checked = '<input type="checkbox" class="detailCheckbox" name="detailCheckbox" value="'+obj.aData.ID+'" checked="checked">';
			    	var unchecked = '<input type="checkbox" class="detailCheckbox" name="detailCheckbox" value="'+obj.aData.ID+'">';
			    	var result = unchecked;
			    	if(detailIdsTest.length != 0){
				    	$.each(detailIdsTest,function(n,value) {  
				    		if(obj.aData.ID == value){
				    			number+=1;
				    			if(number == detailIdsTest.length){
				    				$("#checkboxAll").prop('checked',true);
				    				$('#sureBtn').attr('disabled', false);
				    			}
				    			result = checked;
				    		}
				        }); 
				    	return result;
			    	}else if(detailIds.length != 0){
			    		$.each(detailIds,function(n,value) {  
				    		if(obj.aData.ID == value){
				    			number+=1;
				    			if(number == detailIds.length){
				    				$("#checkboxAll").prop('checked',true);
				    				$('#sureBtn').attr('disabled', false);
				    			}
				    			result = checked;
				    		}
				        }); 
			    		return result;
			    	}else{
			    		return unchecked;
			    	}
					//return '<input type="checkbox" class="detailCheckbox" name="detailCheckbox" value="'+obj.aData.ID+'">';
			    }
			},
            {"mDataProp": "SERIAL_NO","sClass": "serial_no","sWidth": "100px"},
    		{"mDataProp": "PIECES","sWidth": "50px"},
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
        $("#detailIds").val(detailIds);
        
        //普货信息(id)
        var cargoIds = [];
        //var cargoNumbers = [];
        $("#ckeckedTransferOrderList tr").each(function (){
        	var cargo_nature = $(this).find("td").eq(6).text();
        	if(cargo_nature != 'ATM'){
        		cargoIds.push($(this).attr("value"));
        		//cargoNumbers.push($(this).attr("amount")+"&");
        	}
		});
        $("#cargoIds").val(cargoIds);
        //$("#cargoNumbers").val(cargoNumbers);*/
        // console.log("单品id:"+$("#detailIds").val()+",货品数量："+cargoNumbers);
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
				if($("#ckeckedTransferOrderList").children("tr").length == 0){
					$("#saveBtn").attr('disabled', true);
				}
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
		var order_no = $(this).parent().siblings('.order_no')[0].textContent.substr(0, 15);		
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
			if(cargo_nature == 'ATM'){
				$.get("/pickupOrder/findSerialNoByOrderId", {order_id:value}, function(data){
					var ids = data.ID.split(",");
					for ( var i = 0; i < ids.length; i++) {
						detailIds.push(ids[i]);
					}
					console.log("单品id集合-正式:"+detailIds);
					ckeckedTransferOrderList.append("<tr value='"+value+"' serial='"+data.ID+"'><td>"+order_no+"</td><td>"+data.SERIAL_NO+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
							+total_amount+"</td><td>"+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
				},'json');
			}else{
				/*$.get("/pickupOrder/findNumberByOrderId", {order_id:value}, function(data){
					var amount = data.AMOUNTS;
					var amountArray = amount.split(",");
					var pickup_numbers = data.PICKUP_NUMBERS;
					var pickup_numbers_array = [];
					if(pickup_numbers != null && pickup_numbers != ""){
						pickup_numbers_array = pickup_numbers.split(",");
						for ( var i = 0; i < pickup_numbers_array.length; i++) {
							if(pickup_numbers_array[i] != null && amountArray[i]  != null && pickup_numbers_array[i]  != "" && amountArray[i]  != ""){
								amounts.push(amountArray[i] - pickup_numbers_array[i]);
							}
						}
					}else{
						for ( var i = 0; i < amountArray.length; i++) {
							amounts.push(amountArray[i]);
						}
					}
					console.log("amount:"+amount+"<>pickup_numbers:"+pickup_numbers);
					ckeckedTransferOrderList.append("<tr value='"+value+"'serial='' amount='"+amounts+"'><td>"+order_no+"</td><td></td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
							+total_amount+"</td><td>"+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
					console.log("普货选取数量-正式:"+amounts);
					
				},'json');*/
				ckeckedTransferOrderList.append("<tr value='"+value+"'serial=''><td>"+order_no+"</td><td></td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
						+total_amount+"</td><td>"+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
			}
		}else{
			
			if(cargo_nature == 'ATM'){
				$.get("/pickupOrder/findSerialNoByOrderId", {order_id:value}, function(data){
					console.log("单品id集合-正式:"+detailIds);
					console.log("要删除的单品id:"+data.ID);
					var ids = data.ID.split(",");
					for ( var i = 0; i < ids.length; i++) {
						detailIds.splice(detailIds.indexOf(ids[i]), 1); 
					}
					console.log("删除后-单品id集合-正式:"+detailIds);
				},'json');
			}else{
				//删除取消的普货数量
				var attrAmount = $("#ckeckedTransferOrderList").find("tr").attr("amount");
				console.log("attrAmount:"+attrAmount);
				var ids = attrAmount.split(",");
				for ( var i = 0; i < ids.length; i++) {
					amounts.splice(amounts.indexOf(ids[i]), 1); 
				}
				console.log("删除后-普货数量集合-正式:"+amounts);
			}
			
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
    
    //点击按钮 - 弹出模态窗
    $('#eeda-table').on('click', 'button', function(e){      
    	var transferId = $(this).val();
    	var cargo_nature = $(this).parent().siblings('.cargo_nature')[0].textContent;
    	//判断为修改运输单单品的时候，取出原有的单品id集合、单品序列号
    	if($(this).parent().parent().find("td").find("input[type='checkbox'][class='checkedOrUnchecked']").prop('checked') == true){
    		$("#ckeckedTransferOrderList tr").each(function (){
                if($(this).attr("value") == transferId){
                	if(cargo_nature == "ATM"){
	                	var ids = $(this).attr("serial").split(",");
	                	var serialNo = $(this).find("td").eq(1).text().split(" ");
						for ( var i = 0; i < ids.length; i++) {
							detailIdsTest.push(ids[i]);
							detailSerialTest.push(serialNo[i]);
							detailIdsTestOld.push(ids[i]);
							detailSerialTestOld.push(serialNo[i]);
						}
                	}else{
                		//这里要记录临时货品数量的，先不做
                		//amounts = [];
                	}
                }
    		});
    	}
    	//这里可调整ATM与普货的模态窗显示样式，先不做
    	if(cargo_nature == "ATM"){
    		$('#sureBtn').attr('disabled', true);
    		//$("#detailDiv").css("background:#ffffff;");
    	}else{
    		amounts = [];
    		amountsTest = [];
    		//$("#detailDiv").css("background:#f5f5f5;");
    	}
		$("#transferId").val(transferId);
		$("#transferCrgoNature").val(cargo_nature);
		$("#transferOrderNo").empty().html($(this).parent().html().substr(0, 15));
		itemTable.fnSettings().oFeatures.bServerSide = true; 
		itemTable.fnSettings().sAjaxSource = "/pickupOrder/findTransferOrderItem?order_id="+transferId,
		itemTable.fnDraw();
		console.log("单品id集合-临时:"+detailIdsTest);
		console.log("单品序列号集合-临时:"+detailSerialTest);
    });
    
	//关闭模态窗
    $('#closeBtn').click(function(e){
    	amountsTest = [];
    	detailIdsTest = [];
    	detailSerialTest = [];
    	detailIdsTestOld = [];
    	detailSerialTestOld = [];
    	$("#checkboxAll").prop('checked',false);
    	$("#detailTbody").empty().append("<tr><td colspan='3'>表中数据为空</td></tr>");
    	$("#detailTable_info").empty().html("显示第 0 至 0项结果，共 0 项"); 
    	$("#itemTbody").empty();
    	$("#itemTable_info").empty().html("显示第 0 至 0项结果，共 0 项");
    	console.log("单品id集合-临时:"+detailIdsTest);
    	console.log("单品id集合-正式:"+detailIds);
    });
    //关闭模态窗
    $("#close").click(function(e){
    	$("#closeBtn").click();
    });
    //模态窗点击确定
    $('#sureBtn').click(function(e){
    	var transferId = $("#transferId").val();
    	if($("#ckeckedTransferOrderList").find("tr").find("td").text() == "表中数据为空"){
			$("#ckeckedTransferOrderList").empty();
		}
    	$("input[type='checkbox'][class='checkedOrUnchecked']").each(function(){
    		//当运输单没有选中时，已选列表不存在此数据
        	if($(this).val() == transferId && $(this).prop('checked') == false){
        		$(this).prop('checked',true);
        		var ckeckedTransferOrderList = $("#ckeckedTransferOrderList");
        		var order_no = $(this).parent().siblings('.order_no')[0].textContent.substr(0, 15);			
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
    			if(cargo_nature == 'ATM'){
    				//修改数组中序列号为空格分隔
    				var serialArray = "";
    				for ( var i = 0; i < detailIdsTest.length; i++) {
    					serialArray += detailSerialTest[i] + " ";
    				}
    				ckeckedTransferOrderList.append("<tr value='"+$(this).val()+"' serial='"+detailIdsTest+"'><td>"+order_no+"</td><td>"+serialArray+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
    					+total_amount+"</td><td>"+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
    			}else{
    				/*//获取普货的全部数量
    				var sumAmount = 0;
    				$("#itemTbody tr").each(function (){
    					amounts.push($(this).find("td").eq(1).find("input[name='amount']").val());
    					sumAmount += $(this).find("td").eq(1).find("input[name='amount']").val() * 1;
    				});*/
    				ckeckedTransferOrderList.append("<tr value='"+$(this).val()+"' serial=''><td>"+order_no+"</td><td></td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
    						+sumAmount+"</td><td>"+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
    			}
    			//保存选中的单品ID
    			for ( var i = 0; i < detailIdsTest.length; i++) {
    				detailIds.push(detailIdsTest[i]);
    			}
        	}else if($(this).val() == transferId && $(this).prop('checked') == true){
        		//当运输单已选中时，已选列表存在此数据
        		var cargo_nature = $(this).parent().siblings('.cargo_nature')[0].textContent;
        		var total_amount = $(this).parent().siblings('.total_amount')[0].textContent;
        		$("#ckeckedTransferOrderList tr").each(function (){
                    if($(this).attr("value") == transferId){
                    	if(cargo_nature == 'ATM'){
                        	//修改后需新增的数据
                        	for ( var i = 0; i < detailIdsTest.length; i++) {
                        		if(detailIdsTestOld.indexOf(detailIdsTest[i]) == -1){
                        			//直接添加
                        			detailIds.push(detailIdsTest[i]);
                        			console.log("新增后（"+detailIdsTest[i]+"）单品id集合-正式:"+detailIds);
                        		}
                    		}
                        	//修改后需删除的数据
                        	for ( var i = 0; i < detailIdsTestOld.length; i++) {
                        		if(detailIdsTest.indexOf(detailIdsTestOld[i]) == -1){
                        			//直接删除
                        			detailIds.splice(detailIds.indexOf(detailIdsTestOld[i]), 1);
                        			console.log("删除后（"+detailIdsTestOld[i]+"）单品id集合-正式:"+detailIds);
                        		}
                    		}
                        	$("#ckeckedTransferOrderList tr").each(function (){
                                if($(this).attr("value") == transferId){
                                	//更新单品id
                                	$(this).attr("serial",detailIds);
                                	//更新单品序列号
                                	var serialArray = "";
                    				for ( var i = 0; i < detailIdsTest.length; i++) {
                    					serialArray += detailSerialTest[i] + " ";
                    				}
                                	$(this).find("td").eq(1).text(serialArray);
                                }
                    		});
                    	}else{
                    		//获取普货的全部数量
            				var sumAmount = 0;
            				$("#itemTbody tr").each(function (){
            					amounts.push($(this).find("td").eq(1).find("input[name='amount']").val());
            					sumAmount += $(this).find("td").eq(1).find("input[name='amount']").val() * 1;
            				});
            				$("#ckeckedTransferOrderList tr").each(function (){
                                if($(this).attr("value") == transferId){
                                	//更新单个货品数量
                                	$(this).attr("amount",amounts);
                                	//更新货品数量
                                	$(this).find("td").eq(9).text(sumAmount);
                                }
                    		});
                    	}
                    }
        		});
        	}
        	$("#saveBtn").attr('disabled', false);
        });                              
    	
    	$("#closeBtn").click();
    	console.log("单品序列号集合-临时:"+detailSerialTest);
    	console.log("单品id集合-临时:"+detailIdsTest);
    	console.log("单品id集合-正式:"+detailIds);
    });
	
    //点击货品查找单品
	$("#itemTable").on('click', 'tr', function(e){   
		console.log("item_id:"+$(this).attr("id"));
		var item_id = $(this).attr("id");
		/*$(this).css({color:"green", fontWeight:"bold"}); */
		if($("#transferCrgoNature").val() == "ATM"){
			number = 0;
			$("#checkboxAll").prop('checked',false);
			detailTable.fnSettings().oFeatures.bServerSide = true; 
			detailTable.fnSettings().sAjaxSource = "/pickupOrder/findTransferOrderItemDetail?item_id="+item_id,
			detailTable.fnDraw();
		}
	});
	    
	//全选
	$("#checkboxAll").click(function(e){
		
		if($(this).prop('checked') == true){
			$("input[type='checkbox'][class='detailCheckbox']").each(function(){
				$(this).prop('checked',true);
				detailIdsTest.push($(this).val());
				detailSerialTest.push($(this).parent().siblings('.serial_no')[0].textContent);
			});
		}else{
			$("input[type='checkbox'][class='detailCheckbox']").each(function(){
				$(this).prop('checked',false);
				detailIdsTest.splice(detailIdsTest.indexOf($(this).val()), 1);
				detailSerialTest.splice(detailSerialTest.indexOf($(this).parent().siblings('.serial_no')[0].innerHTML), 1);
			});
		}
		
		if(detailIdsTest.length == 0){
			$('#sureBtn').attr('disabled', true);
		}else{
			$('#sureBtn').attr('disabled', false);
		}
		console.log("单品id集合-临时:"+detailIdsTest);
		console.log("单品序列号集合-临时:"+detailSerialTest);
	});
	
	//选择单品
	$("#detailTbody").on('click', '.detailCheckbox', function(){
		if($(this).prop('checked') == true){
			detailIdsTest.push($(this).val());
			detailSerialTest.push($(this).parent().siblings('.serial_no')[0].textContent);
		}else{
			detailIdsTest.splice(detailIdsTest.indexOf($(this).val()), 1);
			detailSerialTest.splice(detailSerialTest.indexOf($(this).parent().siblings('.serial_no')[0].innerHTML), 1);
			$("#checkboxAll").prop('checked',false);
		}
		if(detailIdsTest.length == 0){
			$('#sureBtn').attr('disabled', true);
		}else{
			$('#sureBtn').attr('disabled', false);
		}
		console.log("单品id集合-临时:"+detailIdsTest);
		console.log("单品序列号集合-临时:"+detailSerialTest);
	});
	    
	//修改普货数量
	$("#itemTbody").on('blur', 'input', function(e){
		var oldValue = $(this).attr("oldValue");
		var value = $(this).val();
		if(value == ""){
			$.scojs_message('【数量】不能为空,请重新输入', $.scojs_message.TYPE_OK);
			$(this).val(oldValue);
			$(this).focus();
			return false;
		}
		if(isNaN(value)){
			$.scojs_message('【数量】只能输入数字,请重新输入', $.scojs_message.TYPE_OK);
			$(this).val(oldValue);
			$(this).focus();
			return false;
		}
		if(value*1 > oldValue*1){
			$.scojs_message('【数量】不能大于配送数量,请重新输入', $.scojs_message.TYPE_OK);
			$(this).val(oldValue);
			$(this).focus();
			return false;
		}
		
	});	
	    
} );

