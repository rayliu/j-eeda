
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
    //单品id集合-临时\修改前
    var detailIdsTestOld = [];
    
    //单品序列号集合-临时\修改后
    var detailSerialTest = [];
    //单品序列号集合-临时\修改前
    var detailSerialTestOld = [];
    
    //普货选取数量-正式
    var amounts = [];
    //普货选取前货品数量-临时\修改后
    var amountsTest = [];
    //判断全选box是否选中
    var number = 0;
    var datailNumber = 0;
    //数量填写框控制
    var have_detail = "";
    //用于控制选择的数量变化
    var select_item_id = '';
    //判断二次调拨
    var pickup_type = '';
    
    
    var clean = function(){
    	datailNumber = 0;
    	amountsTest = [];
    	detailIdsTest = [];
    	detailSerialTest = [];
    	detailIdsTestOld = [];
    	detailSerialTestOld = [];
    };

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
            		var val = "选择单品";
            		if(obj.aData.CARGO_NATURE =='cargo'){
            			val = "选择件数";
            		}
	            	if(flag!='derect'){
	            		if(obj.aData.TOTAL_AMOUNT>0){
	            			var str1 = '<button type="button" name="selectDetailBtn" class="btn  btn-primary sm selectDetailBtn" data-toggle="modal" data-target="#myModal" cargoNature="'+obj.aData.CARGO_NATURE+'" value="'+obj.aData.ID+'">'+val+'</button>';
	            			return obj.aData.ORDER_NO + str1;
	            		}else{
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
        "aoColumns": [
            {"mDataProp": "ITEM_NO"},
            {"mDataProp": "AMOUNT"},
    		{"mDataProp": null,
    			"fnRender": function(obj) {
    				var cargoNature = $("#transferCrgoNature").val();
    				if(cargoNature == "ATM"){
    					return obj.aData.ATMAMOUNT;
    				}else{
    					var number = 0;
    					if(obj.aData.PICKUP_NUMBER != null && obj.aData.PICKUP_NUMBER != ''){
    						number = obj.aData.AMOUNT - obj.aData.PICKUP_NUMBER ;
    						amountsTest.push(number);
    					}else{
    						number = obj.aData.AMOUNT * 1;
    						amountsTest.push(number);
    					}
    					if(number > 0){
    						if(have_detail == 'yes'){
    							return number;
    						}else{
    							return "<input type='text' name='amount' size='3' value='"+number+"' oldValue='"+number+"'>";
    						}
    					}
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
			    }
			},
            {"mDataProp": "SERIAL_NO","sClass": "serial_no","sWidth": "100px"},
    		{"mDataProp": "PIECES","sWidth": "50px"},
        ]      
    });	
    
    
    //点击创建时
    $('#saveBtn').click(function(e){
        e.preventDefault();
    	var ids=[];
        var array = [];
        $("#ckeckedTransferOrderList tr").each(function (){

        	
        	var obj={};
    		obj.id = $(this).attr("value");
    		obj.order_type = $(this).find("td").eq(7).text();
    		var number = $($(this).find('td').get(2)).text();
    		var	number2 = $(this).attr("amount");
    		if(number!=number2){
    			number = number2;
    		}
    		obj.number = number;       //货品数量
    		obj.cargoItemId = $(this).attr("itemids");      //普货item表ID
    		obj.detail_ids = $(this).attr("detail_ids");  
    		array.push(obj);
        	
        	ids.push($(this).attr("value"));
		});
        
        var str_JSON = JSON.stringify(array);
    	console.log(str_JSON);
    	$("#detailJson").val(str_JSON);
    	$("#ids").val(ids);
    	
        $('#createForm').submit();
    });
    
    
    $('#searchBtn').on( 'click', function () {
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
			//总重量、总体积
			//sumWeightVal = sumWeightVal + (parseFloat($("#sumWeight").text() == "" ? 0 :parseFloat($("#sumWeight").text())) + total_weight * 1).toFixed(2);	
			//sumVolumnVal = sumVolumnVal + (parseFloat($("#sumVolume").text() == "" ? 0 :parseFloat($("#sumVolume").text())) + total_volume * 1).toFixed(2);
			sumWeightVal = sumWeightVal + parseFloat($(this).find("td").eq(8).text() == "" ? 0 : $(this).find("td").eq(8).text());
			sumVolumnVal = sumVolumnVal + parseFloat($(this).find("td").eq(9).text() == "" ? 0 : $(this).find("td").eq(9).text());
		});
    	//总重量、总体积
    	$("#sumWeight").text(sumWeightVal.toFixed(2));	
		$("#sumVolume").text(sumVolumnVal.toFixed(2));
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
		
		pickup_type = '';
    	if($(this).parent().siblings('.order_no')[0].textContent.indexOf("二次调拨")>0){
    		pickup_type ='twice_pickup';
    	};
		
		if(total_amount==0){
			$.scojs_message('运输单货品可用数量不能为0', $.scojs_message.TYPE_FAIL);
			$(this).prop('checked',false);
			return;
		}
		if($(this).prop('checked') == true){
			if(orderType.length != 0){
				if(orderType[0] != order_type){
					$.scojs_message('请选择相同的订单类型!', $.scojs_message.TYPE_FAIL);
					return false;
				}else if(officeType[0] != office_name){
					$.scojs_message('请选择同一网点的运输单', $.scojs_message.TYPE_FAIL);
					return false;
				}
				if(flag=="derect"){
					if(order_no[0] != cargo_nature){
						$.scojs_message('只能选择一张订单', $.scojs_message.TYPE_FAIL);
						return false;
					}
				}
				orderType.push(order_type);
				officeType.push(office_name);
			}else{
				orderType.push(order_type);
				officeType.push(office_name);
				cargoNature.push(cargo_nature);
				if(ckeckedTransferOrderList.find("tr").find("td").eq(0).text() == "表中数据为空"){
					ckeckedTransferOrderList.empty();
				}
			}
			
			transferOrderIds.push(value);
			if(cargo_nature == 'ATM'){
				$.get("/pickupOrder/findSerialNoByOrderId", {order_id:value,pickup_type:pickup_type}, function(data){
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
			    	$("#sumWeight").text((parseFloat($("#sumWeight").text() == "" ? 0 :parseFloat($("#sumWeight").text())) + weight * 1).toFixed(2));	
					$("#sumVolume").text((parseFloat($("#sumVolume").text() == "" ? 0 :parseFloat($("#sumVolume").text())) + volume * 1).toFixed(2));
					console.log("单品id集合-正式:"+detailIds);
					ckeckedTransferOrderList.append("<tr value='"+value+"' detail_ids='"+data.ID+"' amount='"+ids.length+"' itemids=''><td>"+order_no+"</td><td>"+serial_no+"</td><td>"+ids.length+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+weight+"</td><td>"+volume+"</td><td>"
							+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
				},'json');
			}else{
				$.get("/pickupOrder/findNumberByOrderId", {order_id:value}, function(data){
					var amount = data.AMOUNTS;      //总数量
					var ids = data.IDS;
					var detail_ids = data.DETAIL_IDS;
					var total_amounts = data.TOTAL_AMOUNTS;   //目前的实际
					//var idsArray = ids.split(",");
					var pickup_numbers = data.PICKUP_NUMBERS; //已经提了的数量
					var serial_no = data.SERIAL_NOS;
					//var pickup_numbers_array = [];
					var itemNumbers = [];
					var idNumbers = [];
					
					var total_number = 0.0;
					
					if(detail_ids != null && detail_ids != ''){
						var detail_idsArray = detail_ids.split(",");
						for ( var i = 0; i < detail_idsArray.length; i++) {
							detailIds.push(detail_idsArray[i]);
						}
					}else{
						detail_ids = "";
					}
					//序列号
					if(serial_no == null){
						serial_no = "";
					}
					
	                if(total_amounts != null){
	                	var amountArray = total_amounts.split(",");
	                	var idsArray = ids.split(",");
	                	for ( var i = 0; i < amountArray.length; i++) {
	                		total_number+=parseFloat(amountArray[i]);
	                	
	                		idNumbers.push(idsArray[i]);
	                	}
	                }
	                itemNumbers.push(total_number);

					console.log("amount:"+amount+"<>pickup_numbers:"+pickup_numbers);
					ckeckedTransferOrderList.append("<tr value='"+value+"' detail_ids='"+detail_ids+"' amount='"+total_amounts+"' itemids='"+idNumbers+"'><td>"+order_no+"</td><td>"+serial_no+"</td><td>"+total_amount+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+total_weight+"</td><td>"+total_volume+"</td><td>"
							+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
					console.log("普货选取数量-正式:"+itemNumbers);
					console.log("普货选取的单品id-正式:"+detailIds);
				},'json');
				//总重量、总体积
		    	$("#sumWeight").text((parseFloat($("#sumWeight").text() == "" ? 0 :parseFloat($("#sumWeight").text())) + total_weight * 1).toFixed(2));	
				$("#sumVolume").text((parseFloat($("#sumVolume").text() == "" ? 0 :parseFloat($("#sumVolume").text())) + total_volume * 1).toFixed(2));
			}
		}else{
			if(cargo_nature == 'ATM'){
				$.get("/pickupOrder/findSerialNoByOrderId", {order_id:value,pickup_type:pickup_type}, function(data){
					console.log("单品id集合-正式:"+detailIds);
					console.log("要删除的单品id:"+data.ID);
					var ids = data.ID.split(",");
					for ( var i = 0; i < ids.length; i++) {
						detailIds.splice(detailIds.indexOf(ids[i]), 1); 
					}
					console.log("删除后-单品id集合-正式:"+detailIds);
				},'json');
			}
			
			if(cargo_nature == '普通货品'){
				$.get("/pickupOrder/findNumberByOrderId", {order_id:value}, function(data){
					console.log("单品id集合-正式:"+detailIds);
					console.log("要删除的单品id:"+data.ID);
					var detail_ids = data.DETAIL_IDS.split(",");
					for ( var i = 0; i < detail_ids.length; i++) {
						detailIds.splice(detailIds.indexOf(detail_ids[i]), 1); 
					}
					console.log("删除后-单品id集合-正式:"+detailIds);
				},'json');
			}
			
			transferOrderIds.splice(transferOrderIds.indexOf(value), 1); 
			if(orderType.length != 0){
				orderType.splice(order_type);
				officeType.splice(office_name);
				cargoNature.splice(cargo_nature);
			}
			sumValue();
			var allTrs = ckeckedTransferOrderList.children();
			for(var i=0;i<allTrs.length;i++){
				if(allTrs[i].attributes[0].value == $(this).val()){
					allTrs[i].remove();
					//总重量、总体积
			    	$("#sumWeight").text((parseFloat($("#sumWeight").text() == "" ? 0 :parseFloat($("#sumWeight").text())) + total_weight * 1).toFixed(2));	
					$("#sumVolume").text((parseFloat($("#sumVolume").text() == "" ? 0 :parseFloat($("#sumVolume").text())) + total_volume * 1).toFixed(2));
				}
			}
		}
	});
	
	
	
	
	var itemTableLoad = function(transferId,pickup_type){
    	itemTable.fnSettings().oFeatures.bServerSide = true; 
		itemTable.fnSettings().sAjaxSource = "/pickupOrder/findTransferOrderItem?order_id="+transferId+"&pickup_type="+pickup_type,
		itemTable.fnDraw();
    };
    
    //点击按钮 - 弹出模态窗
    $('#eeda-table').on('click', 'button', function(e){     
    	have_detail = [];
    	var transferId = $(this).val();
    	var cargo_nature = $(this).attr("cargoNature");
    	pickup_type = '';
    	var orderNo = $(this).parent().text();
    	if(orderNo.indexOf("二次调拨")>0){
    		pickup_type ='twice_pickup';
    	};
    	//判断为修改运输单单品的时候，取出原有的单品id集合、单品序列号
    	if($(this).parent().parent().find("td").find("input[type='checkbox'][class='checkedOrUnchecked']").prop('checked') == true){
    		$("#ckeckedTransferOrderList tr").each(function (){
                if($(this).attr("value") == transferId){
                	if(true){
	                	var ids = $(this).attr("detail_ids").split(",");
	                	var serialNo = $(this).find("td").eq(1).text().split(" ");
						for ( var i = 0; i < ids.length; i++) {
							detailIdsTest.push(ids[i]);
							detailSerialTest.push(serialNo[i]);
							detailIdsTestOld.push(ids[i]);
							detailSerialTestOld.push(serialNo[i]);
						}
                	}
                }
    		});
    	}
    	
    	
    	$("#transferId").val(transferId);
		$("#transferCrgoNature").val(cargo_nature);
		$("#transferOrderNo").empty().html($(this).parent().html().substr(0, 15));
    	//这里可调整ATM与普货的模态窗显示样式，先不做
    	if(cargo_nature == "ATM"){
    		$('#sureBtn').attr('disabled', true);
    		
    		itemTableLoad(transferId,pickup_type);
    	}else{
    		$.get("/pickupOrder/findNumberByOrderId", {order_id:transferId}, function(data){
        		if(data.DETAIL_IDS != null){
        			have_detail = "yes";
        			$('#sureBtn').attr('disabled', true);
        			itemTableLoad(transferId,pickup_type);
        		}else{
        			itemTableLoad(transferId,pickup_type);
        			$('#sureBtn').attr('disabled', false);
        		}
        	});
    		amounts = [];
    		amountsTest = [];
    	}
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
    		var this_pickup_type = '';
        	if($(this).parent().siblings('.order_no')[0].textContent.indexOf("二次调拨")>0){
        		this_pickup_type ='twice_pickup';
        	};
    		//当运输单没有选中时，已选列表不存在此数据
        	if($(this).val() == transferId && $(this).prop('checked') == false && this_pickup_type == pickup_type){
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
    			
    			//保存选中的单品ID
    			for ( var i = 0; i < detailIdsTest.length; i++) {
    				detailIds.push(detailIdsTest[i]);
    			}
    			transferOrderIds.push(transferId);
    			
    			//加载序列号
    			var serialArray = "";
				for ( var i = 0; i < detailSerialTest.length; i++) {
					serialArray += detailSerialTest[i] + " ";
				}
    			
    			if(cargo_nature == 'ATM'){
    				//修改数组中序列号为空格分隔
    				
    				//体积、重量
					var volume = (total_volume * (detailIdsTest.length / total_amount)).toFixed(2);
					var weight = (total_weight * (detailIdsTest.length / total_amount)).toFixed(2);
    				ckeckedTransferOrderList.append("<tr value='"+$(this).val()+"' detail_ids='"+detailIdsTest+"' amount='"+detailIdsTest.length +"' itemids=''><td>"+order_no+"</td><td>"+serialArray+"</td><td>"+detailIdsTest.length+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+weight+"</td><td>"+volume+"</td><td>"
    					+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
    			}else{
    				var input = $(this);
    				//获取普货的全部数量
    				var sumAmount = 0;  //选择的总数量
     				var itemNumbers = [];
     				var itemIds = [];  //选择的itemId
    				
     				
     				
    				if(have_detail=='yes'){
    					sumAmount = detailIdsTest.length;;
    					$("#itemTbody tr").each(function (){
    						itemNumbers.push($(this).find('td').eq(1).text());
    						itemIds.push($(this).attr("id"));
        				});
    				}else{
    					$("#itemTbody tr").each(function (){
    						itemNumbers.push($(this).find("input").val());
    						itemIds.push($(this).attr("id"));
    						sumAmount += $(this).find("input").val() * 1;
        				});
    				}
    				
						
					ckeckedTransferOrderList.append("<tr value='"+input.val()+"' detail_ids='"+detailIdsTest+"' amount='"+itemNumbers+"' itemids='"+itemIds+"'><td>"+order_no+"</td><td>"+serialArray+"</td><td>"+sumAmount+"</td><td>"+operation_type+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+order_type+"</td><td>"+cargo_nature+"</td><td>"+" "+"</td><td>"+" "+"</td><td>"
    						+address+"</td><td>"+pickup_mode+"</td><td>"+arrival_mode+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+office_name+"</td><td>"+create_stamp+"</td><td>"+assign_status+"</td></tr>");
				
					
//						//体积、重量 TODO
//	                	$.get("/pickupOrder/productItemsCalculate", {item_id:idNumbers.toString(),itemNumbers:itemNumbers.toString()}, function(data){
//	                		var weight = $("#sumWeight").text() == ""?0:$("#sumWeight").text();
//	                		var volume = $("#sumVolume").text() == ""?0:$("#sumVolume").text();
//	                		$("#sumWeight").text((weight * 1 + (data.sumWeight) * 1).toFixed(2));	
//	                		$("#sumVolume").text((volume * 1 + (data.sumVolume) * 1).toFixed(2));
//	                	},'json');
//    				},'json');
    				
    				
    			}	
        	}else if($(this).val() == transferId && $(this).prop('checked') == true && this_pickup_type == pickup_type){
        		//当运输单已选中时，已选列表存在此数据
        		var cargo_nature = $(this).parent().siblings('.cargo_nature')[0].textContent;	
        		$("#ckeckedTransferOrderList tr").each(function (){
                	if(true){
                		console.log("这次选中的单品id集合:"+detailIdsTest);
                		console.log("总单品id集合:"+detailIds);
                		console.log("这张单重选前的单品id集合:"+detailIdsTestOld);
                		console.log("这次选中的序列号:"+detailSerialTest);
                		var serial = [];
                		var itemIds = [];
                		var itemNumbers = [];
                		var amount = 0;
                		
                		for(var i=0 ; i<detailSerialTest.length ; i++){
                			serial.push(detailSerialTest[i]+' ');
                		}
                		
                		
                		var detailTR = $(this);
                		
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

                    	$("#itemTbody tr").each(function (){
                			if(cargo_nature=='ATM' || have_detail=='yes'){
                				itemNumbers.push($(this).find("td").eq(1).text());
                				amount += $(this).find("td").eq(1).text() * 1;
                			}else{
                				itemNumbers.push($(this).find("input").val());
                				amount += $(this).find("input").val() * 1;
                			}
    						itemIds.push($(this).attr("id"));
        				});
                    	
                    	if(have_detail == '' || have_detail == null){
                    		detailTR.attr("detail_ids",detailIdsTest);
                        	detailTR.attr("amount",itemNumbers);
                        	detailTR.attr("itemIds",itemIds);
                        	$(detailTR.find('td').get(2)).text(amount);
                        	$(detailTR.find('td').get(1)).text(serial);
                    	}else{
                    		amount = detailIdsTest.length;
                    		detailTR.attr("detail_ids",detailIdsTest);
                        	detailTR.attr("amount",amount);
                        	detailTR.attr("itemIds",itemIds);
                        	$(detailTR.find('td').get(2)).text(detailIdsTest.length);
                        	$(detailTR.find('td').get(1)).text(serial);
                    	}
                    	
                    	
                    	$.get("/pickupOrder/detailItemsCalculate", {detailIds:detailIdsTest.toString()}, function(data){
                    		$(detailTR.find("td").eq(9)).text(data.VOLUME);
	                		$(detailTR.find("td").eq(8)).text(data.WEIGHT);
	    				},'json');
                	}
        		});
        	}
        	$("#saveBtn").attr('disabled', false);
        });               
    	
    	sumValue1();
    	$("#closeBtn").click();
    });
	
    //点击货品查找单品
	$("#itemTable").on('click', 'tr', function(e){   
		select_item_id = $(this).attr("id");
		console.log("item_id:"+$(this).attr("id"));
		var item_id = $(this).attr("id");
		datailNumber = $(this).find("td").eq(1).text();
		if(true){
			number = 0;
			$("#checkboxAll").prop('checked',false);
			detailTable.fnSettings().oFeatures.bServerSide = true; 
			detailTable.fnSettings().sAjaxSource = "/pickupOrder/findTransferOrderItemDetail?item_id="+item_id+"&pickup_type="+pickup_type,
			detailTable.fnDraw();
		}
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
		
		//修改
		var select_amount = 0;
		$("#itemTbody tr").each(function (){
			if(select_item_id == $(this).attr("id")){
				$("#detailTbody tr").each(function (){
					if($(this).find('input').prop('checked') == true){
						select_amount += parseInt(1);
					}
				});
				$(this).find("td").eq(1).text(select_amount);
			}
		});
		
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
	    
} );

