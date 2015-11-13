
$(document).ready(function() {
    $('#menu_assign').addClass('active').find('ul').addClass('in');
    var orderType = [];
    var officeType= [];
    var cargoNature = [];
    var transferOrderIds = [];
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
    //普货选取前货品数量-临时\修改后
    var amountsTest = [];
    //判断全选box是否选中
    var number = 0;
    var datailNumber = 0;

    //datatable, 已选中的运输单列表
    var pickupOrder1 = $('#eeda-table1').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示  
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
				{"mDataProp": "ORDER_NO","sWidth": "100px","sClass": "order_no"},
				{"mDataProp": "CNAME","sWidth": "70px","sClass": "cname"},
                {"mDataProp": null, "sWidth": "60px", "sClass": "total_amount"},
				{"mDataProp":"OPERATION_TYPE","sWidth": "60px","sClass": "operation_type"},
				{"mDataProp": "ROUTE_FROM","sWidth": "60px","sClass": "route_from"},
				{"mDataProp": "ROUTE_TO","sWidth": "60px","sClass": "route_to"}, 
				{"mDataProp":"ORDER_TYPE","sWidth": "70px","sClass": "order_type"},
				{"mDataProp":"CARGO_NATURE","sWidth": "60px", "sClass": "cargo_nature"},
				{"mDataProp": null,"sWidth": "60px", "sClass": "total_weight"},
				{"mDataProp": null,"sWidth": "60px", "sClass": "total_volume"},
				{"mDataProp": "ADDRESS","sClass": "address"},
				{"mDataProp":"PICKUP_MODE","sClass": "pickup_mode"},
				{"mDataProp":"ARRIVAL_MODE","sClass": "arrival_mode"},
				{"mDataProp": "STATUS","sWidth": "60px","sClass": "status"},
				{"mDataProp": "CNAME","sWidth": "120px","sClass": "office_name"},  
				{"mDataProp": "OFFICE_NAME","sWidth": "120px","sClass": "cname"},  
				{"mDataProp": "CREATE_STAMP","sWidth": "150px","sClass": "create_stamp"},                                      
				{ "mDataProp": "PICKUP_ASSIGN_STATUS","sClass": "assign_status"}                                                               
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
    var flag=$("#flag").val();
    var pickupOrder = $('#eeda-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bSort": false, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/pickupOrder/createList?flag="+flag,
        "aoColumns": [
            { "mDataProp": null,
				 "fnRender": function(obj) {
					 var result = false;
					 for ( var i = 0; i < transferOrderIds.length; i++) {
						 if(obj.aData.ID == transferOrderIds[i]){
							 result = true;
							 break;
						 }
					 }
					 if(result){
						 return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'" checked="checked">';
					 }else{
						 return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
					 }
				 }
            },
            { 
            	"mDataProp": "ORDER_NO",
            	"sWidth": "100px",
            	"sClass": "order_no",
            	"fnRender": function(obj) {
            		//atm、补货订单、不是直送运输单
            		
	            	if(flag!='derect'){
	            		if(obj.aData.CARGO_NATURE == "ATM"/* || (obj.aData.CARGO_NATURE == "cargo" && obj.aData.CARGO_NATURE_DETAIL == "cargoNatureDetailYes")*/){
	            			var str1 = '<button type="button" name="selectDetailBtn" class="btn  btn-primary sm selectDetailBtn" data-toggle="modal" data-target="#myModal" cargoNature="'+obj.aData.CARGO_NATURE+'" value="'+obj.aData.ID+'">选择单品</button>';
	            			return obj.aData.ORDER_NO + str1;
	            		}/*else if(obj.aData.CARGO_NATURE == "cargo" && obj.aData.CARGO_NATURE_DETAIL == "cargoNatureDetailNo"){
	            			var str1 = '<button type="button" name="selectDetailBtn" class="btn  btn-primary sm selectDetailBtn" data-toggle="modal" data-target="#myModal" cargoNature="'+obj.aData.CARGO_NATURE+'" value="'+obj.aData.ID+'">选择货品</button>';
	            			return obj.aData.ORDER_NO + str1;
	            		}*/else{
	            			return obj.aData.ORDER_NO;
	            		}
            		}else{
            			return obj.aData.ORDER_NO;
            		}
                 }
            },
            { 
            	"mDataProp": "PLANNING_TIME",
                "sWidth": "70px",
            	"sClass": "planning_time"
            },
            { 
                "mDataProp": null, "sWidth": "60px",
                "sClass": "total_amount",
                "fnRender": function(obj) {
                    if(obj.aData.CARGO_NATURE == "ATM"){
                        return obj.aData.ATMAMOUNT;
                    }else{
                        return obj.aData.TOTAL_AMOUNT;
                    }
                }
            },
            { 
            	"mDataProp": "CNAME",
                "sWidth": "60px",
            	"sClass": "cname"
            },
            {"mDataProp":"OPERATION_TYPE",
            	"sWidth": "60px",
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
            	"sWidth": "60px",
            	"sClass": "route_from"
            },
    		{ 
            	"mDataProp": "ROUTE_TO",
            	"sWidth": "60px",
            	"sClass": "route_to"
            }, 
		    {"mDataProp":"ORDER_TYPE",
            	"sWidth": "60px",
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
            {"mDataProp":null,
            	"sWidth": "60px",
            	"sClass": "cargo_nature",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return pickupOrderSearcheTransfer.ex_cargo;
            		}else{
            			return "";
            }}},
            { 
            	"mDataProp": null,
            	"sClass": "total_weight",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "ATM"){
            			return obj.aData.ATMWEIGHT;
            		}else{
            			return obj.aData.TOTAL_WEIGHT;
            		}
            	}
            },
            { 
            	"mDataProp": null,
            	"sClass": "total_volume",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "ATM"){
            			return obj.aData.ATMVOLUME;
            		}else{
            			return obj.aData.TOTAL_VOLUME;
            		}
            	}
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
            			return pickupOrderSearcheTransfer.ex_type;
            			
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
    					var number = 0;
    					if(obj.aData.PICKUP_NUMBER != null && obj.aData.PICKUP_NUMBER != ''){
    						var number = obj.aData.AMOUNT - obj.aData.PICKUP_NUMBER ;
    						amountsTest.push(number);
    					}else{
    						var number = obj.aData.AMOUNT * 1;
    						amountsTest.push(number);
    					}
    					if(number > 0)
							return "<input type='text' name='amount' size='3' value='"+number+"' oldValue='"+number+"'>";
						else
							return 0;
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
				    			result = checked;
				    			if(number == datailNumber){
				    				$("#checkboxAll").prop('checked',true);
				    			}
				    			$('#sureBtn').attr('disabled', false);
				    		}
				        }); 
				    	return result;
			    	}else if(detailIds.length != 0){
			    		$.each(detailIds,function(n,value) {  
			    			if(obj.aData.ID == value){
				    			number+=1;
				    			result = checked;
				    			if(number == datailNumber){
				    				$("#checkboxAll").prop('checked',true);
				    			}
				    			$('#sureBtn').attr('disabled', false);
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
        /*$("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		trArr.push($(this).val());
        	}
        });*/
        var cargoIds = [];
        //var cargoNumbers = [];
        var cargoNumbers = "";
        var cargoItemIds = [];
        $("#ckeckedTransferOrderList tr").each(function (){
        	var cargo_nature = $(this).find("td").eq(6).text();
        	if(cargo_nature != 'ATM'){
        		cargoIds.push($(this).attr("value"));
        		var itemNumverArray = $(this).attr("amount");
        		var itemArray = itemNumverArray.split(",");
        		//cargoNumbers.push(itemArray + "&");
        		cargoNumbers += itemArray + "&";
        		var itemIdArray = $(this).attr("itemids");
        		var idArray = itemIdArray.split(",");
        		cargoItemIds.push(idArray);
        	}
        	trArr.push($(this).attr("value"));
		});
        //全部选中运输单id
        tableArr.push(trArr);
        $('#pickupOrder_message').val(tableArr);
        console.log(tableArr);
        //所有单品id
        $("#detailIds").val(detailIds);
        //普货运输单(id)
        $("#cargoIds").val(cargoIds);
        $("#cargoNumbers").val(cargoNumbers);
        $("#cargoItemIds").val(cargoItemIds);
        console.log("货品id:"+cargoItemIds+",货品数量："+cargoNumbers);
        $('#createForm').submit();
    });
    
    $('input.orderNo_filter, input.status_filter, input.address_filter, input.routeFrom_filter, #beginTime_filter, #endTime_filter, input.routeTo_filter, input.orderType_filter').on( 'keyup click', function () {
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
	
	var sumValue1 = function(){
		
		var sumWeightVal = 0;
		var sumVolumnVal = 0;
		$("#ckeckedTransferOrderList tr").each(function (){
			sumWeightVal = sumWeightVal + parseFloat($(this).find("td").eq(7).text() == "" ? 0 : $(this).find("td").eq(7).text());
			sumVolumnVal = sumVolumnVal + parseFloat($(this).find("td").eq(8).text() == "" ? 0 : $(this).find("td").eq(8).text());
		});
    	//总重量、总体积
    	$("#sumWeight").text(sumWeightVal.toFixed(2));	
		$("#sumVolume").text(sumVolumnVal.toFixed(2));
	};
	
	
	// 选中或取消事件
	$("#transferOrderList").on('click', '.checkedOrUnchecked', function(){
		var ckeckedTransferOrderList = $("#ckeckedTransferOrderList");
		var order_no = $(this).parent().siblings('.order_no')[0].textContent.substr(0, 15);		
		var planning_time = $(this).parent().siblings('.planning_time')[0].textContent;		
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
		if(total_amount==0){
			$.scojs_message('运输单货品可用数量不能为0', $.scojs_message.TYPE_FAIL);
			$(this).prop('checked',false);
			return;
		}
		if($(this).prop('checked') == true){
			if(orderType.length != 0){
				if(orderType[0] != $(this).parent().siblings('.order_type')[0].innerHTML){
					alert("请选择相同的订单类型!");
					return false;
				}else if(officeType[0]!=$(this).parent().siblings('.office_name')[0].innerHTML){
					alert("请选择同一网点的运输单");
					return false;
				}else if(cargoNature[0]!=$(this).parent().siblings('.cargo_nature')[0].innerHTML){
					alert("请选择同一货品属性的运输单");
					return false;
				}
				if(flag=="derect"){
					if(order_no[0]!=$(this).parent().siblings('.order_no')[0].innerHTML){
						alert("只能选择一张订单");
						return false;
					}
				}
				orderType.push($(this).parent().siblings('.order_type')[0].innerHTML);
				officeType.push($(this).parent().siblings('.office_name')[0].innerHTML);
			}else{
				if($(this).parent().siblings('.order_type')[0].innerHTML != ''){
					orderType.push($(this).parent().siblings('.order_type')[0].innerHTML);
					officeType.push($(this).parent().siblings('.office_name')[0].innerHTML);
					cargoNature.push($(this).parent().siblings('.cargo_nature')[0].innerHTML);
				}
				if(ckeckedTransferOrderList.find("tr").find("td").eq(0).text() == "表中数据为空"){
					ckeckedTransferOrderList.empty();
				}
			}
			//sumValue();
			
			transferOrderIds.push(value);
			if(cargo_nature == 'ATM'){
				$.get("/pickupOrder/findSerialNoByOrderId", {order_id:value}, function(data){
					var ids = data.ID.split(",");
					var serial_no = data.SERIAL_NO;
					for ( var i = 0; i < ids.length; i++) {
						detailIds.push(ids[i]);
					}
					if(serial_no == null){
						serial_no = "";
					}
					//体积、重量
					var volume = (total_volume * (ids.length / total_amount)).toFixed(2);
					var weight = (total_weight * (ids.length / total_amount)).toFixed(2);
					//总重量、总体积
			    	$("#sumWeight").text((parseFloat($("#sumWeight").text() == "" ? 0 :$("#sumWeight").text()) + weight * 1).toFixed(2));	
					$("#sumVolume").text((parseFloat($("#sumVolume").text() == "" ? 0 :$("#sumVolume").text()) + volume * 1).toFixed(2));
					console.log("单品id集合-正式:"+detailIds);
					//<td>"+planning_time+"</td>
					ckeckedTransferOrderList.append("<tr value='"+value+"' serial='"+data.ID+"' amount='' itemids=''><td>"+order_no+"</td><td>"+serial_no+"</td><td>"+ids.length+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+weight+"</td><td>"+volume+"</td><td>"
							+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
				},'json');
			}else{
				$.get("/pickupOrder/findNumberByOrderId", {order_id:value}, function(data){
					var amount = data.AMOUNTS;
					var amountArray = amount.split(",");
					var ids = data.IDS;
					var idsArray = ids.split(",");
					var pickup_numbers = data.PICKUP_NUMBERS;
					var pickup_numbers_array = [];
					var itemNumbers = [];
					var idNumbers = [];
					if(pickup_numbers != null && pickup_numbers != ""){
						pickup_numbers_array = pickup_numbers.split(",");
						for ( var i = 0; i < pickup_numbers_array.length; i++) {
							if(pickup_numbers_array[i] != null && amountArray[i]  != null && pickup_numbers_array[i]  != "" && amountArray[i]  != ""){
								//amounts.push(amountArray[i] - pickup_numbers_array[i]);
								itemNumbers.push(amountArray[i] - pickup_numbers_array[i]);
							}
							idNumbers.push(idsArray[i]);
						}
					}else{
						for ( var i = 0; i < amountArray.length; i++) {
							//amounts.push(amountArray[i]);
							itemNumbers.push(amountArray[i]);
							idNumbers.push(idsArray[i]);
						}
					}
					console.log("amount:"+amount+"<>pickup_numbers:"+pickup_numbers);
                    total_amount = "<input type='text' value='"+total_amount+"' size='8'/>"
					ckeckedTransferOrderList.append("<tr value='"+value+"'serial='' amount='"+itemNumbers+"' itemids='"+idNumbers+"'><td>"+order_no+"</td><td></td><td>"+total_amount+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
							+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
					console.log("普货选取数量-正式:"+amounts);
				},'json');
				//<td>"+planning_time+"</td>
				//ckeckedTransferOrderList.append("<tr value='"+value+"'serial=''><td>"+order_no+"</td><td></td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
						//+total_amount+"</td><td>"+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
				//总重量、总体积
		    	$("#sumWeight").text((parseFloat($("#sumWeight").text() == "" ? 0 :$("#sumWeight").text()) + total_weight * 1).toFixed(2));	
				$("#sumVolume").text((parseFloat($("#sumVolume").text() == "" ? 0 :$("#sumVolume").text()) + total_volume * 1).toFixed(2));
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
				/*var attrAmount = $("#ckeckedTransferOrderList").find("tr").attr("amount");
				console.log("attrAmount:"+attrAmount);
				var ids = attrAmount.split(",");
				for ( var i = 0; i < ids.length; i++) {
					amounts.splice(amounts.indexOf(ids[i]), 1); 
				}
				console.log("删除后-普货数量集合-正式:"+amounts);*/
			}
			transferOrderIds.splice(transferOrderIds.indexOf(value), 1); 
			if(orderType.length != 0){
				orderType.splice($(this).parent().siblings('.order_type')[0].innerHTML, 1);
				officeType.splice($(this).parent().siblings('.office_name')[0].innerHTML,1);
				cargoNature.splice($(this).parent().siblings('.cargo_nature')[0].innerHTML,1);
			}
			sumValue();
			var allTrs = ckeckedTransferOrderList.children();
			for(var i=0;i<allTrs.length;i++){
				if(allTrs[i].attributes[0].value == $(this).val()){
					allTrs[i].remove();
					//总重量、总体积
			    	$("#sumWeight").text((parseFloat(allTrs[i].find("td").eq(7).text("").text()== "" ? 0 :allTrs[i].find("td").eq(7).text("").text()) - total_weight * 1).toFixed(2));	
					$("#sumVolume").text((parseFloat(allTrs[i].find("td").eq(8).text("").text()== "" ? 0 :allTrs[i].find("td").eq(8).text("").text()) - total_volume * 1).toFixed(2));
				}
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
    	var cargo_nature = $(this).attr("cargoNature");
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
                		//临时货品数量已在input中oldValue属性记录，
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
		//console.log("单品id集合-临时:"+detailIdsTest);
		//console.log("单品序列号集合-临时:"+detailSerialTest);
    });
    
	//关闭模态窗
    $('#closeBtn').click(function(e){
    	datailNumber = 0;
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
    	//console.log("单品id集合-临时:"+detailIdsTest);
    	//console.log("单品id集合-正式:"+detailIds);
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
        		var planning_time = $(this).parent().siblings('.planning_time')[0].textContent;
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
    				//体积、重量
					var volume = (total_volume * (detailIdsTest.length / total_amount)).toFixed(2);
					var weight = (total_weight * (detailIdsTest.length / total_amount)).toFixed(2);
    				ckeckedTransferOrderList.append("<tr value='"+$(this).val()+"' serial='"+detailIdsTest+"' amount='' itemids=''><td>"+order_no+"</td><td>"+serialArray+"</td><td>"+detailIdsTest.length+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+weight+"</td><td>"+volume+"</td><td>"
    					+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
    			}else{
    				var input = $(this);
    				//获取普货的全部数量
    				var sumAmount = 0;
    				var itemNumbers = [];
    				$("#itemTbody tr").each(function (){
    					itemNumbers.push($(this).find("td").eq(1).find("input[name='amount']").val());
    					amounts.push($(this).find("td").eq(1).find("input[name='amount']").val());
    					sumAmount += $(this).find("td").eq(1).find("input[name='amount']").val() * 1;
    				});
    				
    				$.get("/pickupOrder/findNumberByOrderId", {order_id:transferId}, function(data){
    					var ids = data.IDS;
    					var idsArray = ids.split(",");
    					var idNumbers = [];
						for ( var i = 0; i < idsArray.length; i++) {
							idNumbers.push(idsArray[i]);
						}
						//体积、重量 TODO
	                	$.get("/pickupOrder/productItemsCalculate", {item_id:idNumbers.toString(),itemNumbers:itemNumbers.toString()}, function(data){
	                		var weight = $("#sumWeight").text() == ""?0:$("#sumWeight").text();
	                		var volume = $("#sumVolume").text() == ""?0:$("#sumVolume").text();
	                		$("#sumWeight").text((weight * 1 + (data.sumWeight) * 1).toFixed(2));	
	                		$("#sumVolume").text((volume * 1 + (data.sumVolume) * 1).toFixed(2));
	                		ckeckedTransferOrderList.append("<tr value='"+input.val()+"' serial='' amount='"+itemNumbers+"' itemids='"+idNumbers+"'><td>"+order_no+"</td><td></td><td>"+sumAmount+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+data.sumWeight+"</td><td>"+data.sumVolume+"</td><td>"
		    						+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
	    				},'json');
    				},'json');
    			}
    			//保存选中的单品ID
    			for ( var i = 0; i < detailIdsTest.length; i++) {
    				detailIds.push(detailIdsTest[i]);
    			}
    			transferOrderIds.push(transferId);
        	}else if($(this).val() == transferId && $(this).prop('checked') == true){
        		//当运输单已选中时，已选列表存在此数据
        		var cargo_nature = $(this).parent().siblings('.cargo_nature')[0].textContent;
        		var total_amount = $(this).parent().siblings('.total_amount')[0].textContent;
        		var total_weight = $(this).parent().siblings('.total_weight')[0].textContent;		
    			var total_volume = $(this).parent().siblings('.total_volume')[0].textContent;		
    			var total_amount = $(this).parent().siblings('.total_amount')[0].textContent;		
        		$("#ckeckedTransferOrderList tr").each(function (){
                    if($(this).attr("value") == transferId){
                    	if(cargo_nature == 'ATM'){
                    		console.log("单品id集合-临时:"+detailIdsTest);
                    		console.log("单品序列号集合-临时:"+detailSerialTest);
                        	//修改后需新增的数据
                        	for ( var i = 0; i < detailIdsTest.length; i++) {
                        		if(detailIdsTestOld.indexOf(detailIdsTest[i]) == -1){
                        			//直接添加
                        			detailIds.push(detailIdsTest[i]);
                        		}
                    		}
                        	//修改后需删除的数据
                        	for ( var i = 0; i < detailIdsTestOld.length; i++) {
                        		if(detailIdsTest.indexOf(detailIdsTestOld[i]) == -1){
                        			//直接删除
                        			detailIds.splice(detailIds.indexOf(detailIdsTestOld[i]), 1);
                        		}
                    		}
                        	$("#ckeckedTransferOrderList tr").each(function (){
                                if($(this).attr("value") == transferId){
                                	var detailTR = $(this);
                                	//更新单品id
                                	detailTR.attr("serial",detailIdsTest);
                                	//更新单品序列号
                                	var serialArray = "";
                    				for ( var i = 0; i < detailSerialTest.length; i++) {
                    					if(detailSerialTest[i] != "" && detailSerialTest[i] != null){
                    						serialArray += detailSerialTest[i] + " ";
                    					}
                    				}
                    				//序列号
                    				detailTR.find("td").eq(1).text("").text(serialArray);
                    				//实发数量
                    				detailTR.find("td").eq(9).text("").text(detailIdsTest.length);
                    				//体积、重量 
            	                	$.get("/pickupOrder/detailItemsCalculate", {detailIds:detailIdsTest.toString()}, function(data){
            	                		detailTR.find("td").eq(8).text("").text(data.VOLUME);
            	                		detailTR.find("td").eq(7).text("").text(data.WEIGHT);
            	    				},'json');
                                }
                    		});
                    	}else{
                    		//获取普货的全部数量
            				var sumAmount = 0;
            				var itemNumbers = [];
            				$("#itemTbody tr").each(function (){
            					amounts.push($(this).find("td").eq(1).find("input[name='amount']").val());
            					itemNumbers.push($(this).find("td").eq(1).find("input[name='amount']").val());
            					sumAmount += $(this).find("td").eq(1).find("input[name='amount']").val() * 1;
            				});
            				$("#ckeckedTransferOrderList tr").each(function (){
                                if($(this).attr("value") == transferId){
                                	var itemTR = $(this);
                                	var idNumbers = itemTR.attr("itemids");
                                	//更新单个货品数量
                                	itemTR.attr("amount",itemNumbers);
                                	//更新货品数量
                                	itemTR.find("td").eq(9).text(sumAmount);
                                	//体积、重量 TODO
            	                	$.get("/pickupOrder/productItemsCalculate", {item_id:idNumbers.toString(),itemNumbers:itemNumbers.toString()}, function(data){
            	                		var weight = $("#sumWeight").text() == ""?0:$("#sumWeight").text();
            	                		var volume = $("#sumVolume").text() == ""?0:$("#sumVolume").text();
            	                		itemTR.find("td").eq(8).text("").text(data.sumVolume);
                                		itemTR.find("td").eq(7).text("").text(data.sumWeight);
            	                		$("#sumWeight").text((weight * 1 + (data.sumWeight) * 1).toFixed(2));	
            	                		$("#sumVolume").text((volume * 1 + (data.sumVolume) * 1).toFixed(2));
            	    				},'json');
                                }
                    		});
            				
                    	}
                    }
        		});
        	}
        	$("#saveBtn").attr('disabled', false);
        });                
    	
    	sumValue1();
    	$("#closeBtn").click();
    	//console.log("单品序列号集合-临时:"+detailSerialTest);
    	//console.log("单品id集合-临时:"+detailIdsTest);
    	//console.log("单品id集合-正式:"+detailIds);
    });
	
    //点击货品查找单品
	$("#itemTable").on('click', 'tr', function(e){   
		console.log("item_id:"+$(this).attr("id"));
		var item_id = $(this).attr("id");
		datailNumber = $(this).find("td").eq(1).text();
		/*$(this).css({color:"green", fontWeight:"bold"}); */
		if($("#transferCrgoNature").val() == "ATM"){
			number = 0;
			$("#checkboxAll").prop('checked',false);
			detailTable.fnSettings().oFeatures.bServerSide = true; 
			detailTable.fnSettings().sAjaxSource = "/pickupOrder/findTransferOrderItemDetail?item_id="+item_id,
			detailTable.fnDraw();
		}
		//console.log("单品数量:"+datailNumber);
	});
	    
	//全选
	$("#checkboxAll").click(function(e){
		if($(this).prop('checked') == true){
			$("input[type='checkbox'][class='detailCheckbox']").each(function(){
				$(this).prop('checked',true);
				if(detailIdsTest.indexOf($(this).val()) == -1){
					detailIdsTest.push($(this).val());
					detailSerialTest.push($(this).parent().siblings('.serial_no')[0].textContent);
				}
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
		//console.log("单品id集合-临时:"+detailIdsTest);
		//console.log("单品序列号集合-临时:"+detailSerialTest);
	});
	
	//选择单品
	$("#detailTbody").on('click', '.detailCheckbox', function(){
		if($(this).prop('checked') == true){
			detailIdsTest.push($(this).val());
			detailSerialTest.push($(this).parent().siblings('.serial_no')[0].textContent);
			if(detailIdsTest.length == (datailNumber * 1)){
				$("#checkboxAll").prop('checked',true);
			}
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
		//console.log("单品id集合-临时:"+detailIdsTest);
		//console.log("单品序列号集合-临时:"+detailSerialTest);
	});
	    
	//修改普货数量
	$("#itemTbody").on('blur', 'input', function(e){
		var oldValue = $(this).attr("oldValue");
		var value = $(this).val();
		if(value == ""){
			$.scojs_message('【数量】不能为空,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val(oldValue);
			$(this).focus();
			return false;
		}else if(isNaN(value)){
			$.scojs_message('【数量】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val(oldValue);
			$(this).focus();
			return false;
		}else if(value*1 > oldValue*1){
			$.scojs_message('【数量】不能大于可选数量,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val(oldValue);
			$(this).focus();
			return false;
		}
	});	
	    
} );

