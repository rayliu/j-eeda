$(document).ready(function() {
	$('#menu_carmanage').addClass('active').find('ul').addClass('in');
	
	var pickupIds = [];
	var checkedTest = [];
	var carNo = [];
    var createStamp = [];
	var num = 1;
	var num2 = 1;
	//收集所有未处理调车单id，用来勾选条件判断
	var unDisposePickuoIds = [];
	
	$("#saveBtn").attr('disabled', true);
	
	//未处理行车单，datatable
    var unDispose_table = $('#unDispose_table').dataTable({
        "bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 20,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/carsummary/untreatedCarManageList",
        "aoColumns": [ 
			{ "mDataProp": null,"sWidth":"10px",
				"fnRender": function(obj) {
					unDisposePickuoIds.push("S"+obj.aData.ID);
					return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
				}
			},  
			{ "mDataProp": null,"sWidth":"40px",
					"fnRender": function(obj) {
					return num++;
				}
			}, 
			{"mDataProp":null,"sWidth":"120px",
					"fnRender": function(obj) {
					return "<a href='/yh/pickupOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.DEPART_NO+"</a>";
				}
			},
			{"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"120px"},
			{"mDataProp":"STATUS","sWidth":"60px"},
			{"mDataProp":"CAR_NO","sClass": "CAR_NO","sWidth":"60px"},
			{"mDataProp":"CONTACT_PERSON","sWidth":"60px"},
			{"mDataProp":null,"sClass": "CREATE_STAMP","sWidth":"80px",
				"fnRender": function(obj) {
					if(obj.aData.TURNOUT_TIME != "" && obj.aData.TURNOUT_TIME != null){
						var str = obj.aData.TURNOUT_TIME;
						str = str.substr(0,10);
						return str;
					}else{
						return "";
					}
				}}, 
			{"mDataProp":"OFFICE_NAME","sWidth":"100px"}, 
			{"mDataProp":"VOLUME", "sWidth":"60px"},                        
			{"mDataProp":"WEIGHT", "sWidth":"60px"},                        
			{"mDataProp":"USER_NAME", "sWidth":"60px"},                        
			{"mDataProp":"REMARK", "sWidth":"150px"}                       
	      ]          
    });
	//行车单查询，dataTable
    var travellingCraneReceipts_table = $('#travellingCraneReceipts_table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"aoColumns": [ 
			  { "mDataProp": null,"sWidth":"40px",
					"fnRender": function(obj) {
					return num2++;
				}
			  },
	          {"mDataProp": null,
				  "fnRender": function(obj) {
          			return "<a href='/yh/carsummary/edit?carSummaryId="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
          	  }},  
	          {"mDataProp":"PICKUP_NO", "sWidth":"120px"},
	          {"mDataProp":"TRANSFER_ORDER_NO","sWidth":"120px"},
	          {"mDataProp":"STATUS", "sWidth":"60px",
					"fnRender": function(obj) {
						if("new" == obj.aData.STATUS){
			    			return "新建";
			    		}else if("checked" == obj.aData.STATUS){
			    			return "已审核";
			    		}else if("revocation" == obj.aData.STATUS){
			    			return "已撤销";
			    		}else if("reimbursement" == obj.aData.STATUS){
			    			return "已报销";
			    		}else{
			    			return "";
			    		}
					}
	          },
			  {"mDataProp":"CAR_NO", "sWidth":"70px"},
			  {"mDataProp":"MAIN_DRIVER_NAME", "sWidth":"70px"},
			  {"mDataProp":"TURNOUT_TIME", "sWidth":"80px"},
			  {"mDataProp":"RETURN_TIME", "sWidth":"80px"},
			  {"mDataProp":null, "sWidth":"70px",
				  "fnRender": function(obj) {
						var data1 = obj.aData.TURNOUT_TIME.split("-");
						var data2 = obj.aData.RETURN_TIME.split("-");
						return data2[2] - data1[2];
					}
			  },        	
			  {"mDataProp":"VOLUME", "sWidth":"70px"},           
			  {"mDataProp":"WEIGHT", "sWidth":"70px"},        	
			  {"mDataProp":"CARSUMMARYMILEAGE", "sWidth":"80px"},                        
			  {"mDataProp":"MONTH_REFUEL_AMOUNT", "sWidth":"90px"},                        
			  {"mDataProp":null, "sWidth":"90px"},                        
			  {"mDataProp":"SUBSIDY", "sWidth":"70px"},                        
			  {"mDataProp":"DRIVER_SALARY", "sWidth":"70px"},                        
			  {"mDataProp":"TOLL_CHARGE", "sWidth":"60px"},                        
			  {"mDataProp":"HANDLING_CHARGES", "sWidth":"60px"},                        
			  {"mDataProp":"FINE", "sWidth":"60px"},                        
			  {"mDataProp":"DELIVERYMAN_SALARY", "sWidth":"80px"},                        
			  {"mDataProp":"PARKING_CHARGE", "sWidth":"60px"},                        
			  {"mDataProp":"QUARTERAGE", "sWidth":"60px"},                        
			  {"mDataProp":"WEIGHING_CHARGE", "sWidth":"60px"},                        
			  {"mDataProp":"OTHER_CHARGES", "sWidth":"70px"},                        
			  {"mDataProp":"TOTAL_COST", "sWidth":"100px"},           
			  {"mDataProp":"DEDUCT_APPORTION_AMOUNT", "sWidth":"100px"},  
			  {"mDataProp":"ACTUAL_PAYMENT_AMOUNT", "sWidth":"100px"}
		]          
    });
    
    //未处理调车单-时间按钮
    $('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN',
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
		$(".bootstrap-datetimepicker-widget").hide();
	    $('#create_stamp').trigger('keyup');
	});
    
    //行车单查询-时间按钮
    $('#datetimepicker2').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN',
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
		$(".bootstrap-datetimepicker-widget").hide();
	    $('#create_stamp').trigger('keyup');
	});
    
    // 选显卡-未处理行车单查询
	$("#chargeCheckOrderbasic").click(function(){
		num = 1;
		unDispose_table.fnSettings().oFeatures.bServerSide = true; 
		unDispose_table.fnSettings().sAjaxSource = "/yh/carsummary/untreatedCarManageList";
		unDispose_table.fnDraw();
	});
    
    //未处理行车单各种搜索
    $('#status ,#driver ,#car_no ,#transferOrderNo ,#create_stamp').on( 'keyup click', function () {
		var status = $("#status").val();
		var driver = $("#driver").val();
		var car_no = $("#car_no").val();
		var transferOrderNo = $("#transferOrderNo").val();
		var create_stamp = $("#create_stamp").val();
		num = 1;
		unDispose_table.fnSettings().oFeatures.bServerSide = true; 
		unDispose_table.fnSettings().sAjaxSource = "/yh/carsummary/untreatedCarManageList?status="+status+"&driver="+driver+"&car_no="+car_no+"&transferOrderNo="+transferOrderNo+"&create_stamp="+create_stamp;
		unDispose_table.fnDraw();
	} );
    
	//创建行车单
    $('#saveBtn').click(function(e){
        e.preventDefault();
        if(pickupIds.length > 0){
        	$("#pickupIds").val(pickupIds);
            $('#createForm').submit();
        }else{
        	alert("请选择要创建的调车单");
        }
        
    });
    
	// 选中或取消事件
	$("#unDispose_table").on('click', '.checkedOrUnchecked', function(){
		if($(this).prop("checked") == true){
			if(carNo.length != 0){
				var checkData = $(this).parent().siblings('.CREATE_STAMP')[0].innerHTML;
				var dataTo = checkData.split("-");
				var dataFrom = createStamp[0].split("-");
				if(carNo[0] == $(this).parent().siblings('.CAR_NO')[0].innerHTML && $(this).parent().siblings('.CAR_NO')[0].innerHTML != ''){
					if(dataFrom[0] == dataTo[0] && dataFrom[1] == dataTo[1]){
						var num = unDisposePickuoIds.indexOf(checkedTest[checkedTest.length-1]);
						var select = "S"+$(this).val();
						if(select == unDisposePickuoIds[num-1] || select == unDisposePickuoIds[num+1]){
							checkedTest.push("S"+$(this).val());
							pickupIds.push($(this).val());
						}else{
							alert("请选择连续性出车日期的调车单!");
							return false;
						}
					}else{
						alert("请选择出车日期为同一个月的调车单!");
						return false;
					}
				}else{
					alert("请选择同一车辆!");
					return false;
				}
			}else{
				pickupIds.push($(this).val());
				checkedTest.push("S"+$(this).val());
				carNo.push($(this).parent().siblings('.CAR_NO')[0].innerHTML);
				createStamp.push($(this).parent().siblings('.CREATE_STAMP')[0].innerHTML);
				$("#saveBtn").attr('disabled', false);
			}
		}else{
			if(checkedTest.length != 0){
				if(checkedTest.indexOf("S"+$(this).val()) == 0 || checkedTest.indexOf("S"+$(this).val()) ==checkedTest.length-1){
					checkedTest.splice(checkedTest.indexOf("S"+$(this).val()), 1); 
					pickupIds.splice(pickupIds.indexOf($(this).val()), 1); 
					carNo.splice($(this).parent().siblings('.CAR_NO')[0].innerHTML, 1);
					createStamp.splice($(this).parent().siblings('.CREATE_STAMP')[0].innerHTML, 1);
					if(checkedTest.length == 0)
						$("#saveBtn").attr('disabled', true);
				}
				else{
					alert("按连续性要求，您只能取消开头或最后的调车单！");
					$(this).prop("checked",true);
				}
			}else{
				$("#saveBtn").attr('disabled', true);
			}
		}
	});
	
	// 选显卡-行车单查询
	$("#carSummaryOrderList").click(function(){
		num2 = 1;
		unDispose_table.fnSettings().oFeatures.bServerSide = false; 
		travellingCraneReceipts_table.fnSettings().oFeatures.bServerSide = true; 
		travellingCraneReceipts_table.fnSettings().sAjaxSource = "/yh/carsummary/carSummaryOrderList";
		travellingCraneReceipts_table.fnDraw();
	});
	//行车单查询各种搜索
    $('#carSummary_status ,#carSummary_car_no ,#carSummary_driver ,#carSummary_transfer_order ,#carSummary_pickup_order,#carSummary_start_data').on( 'keyup click', function () {
		var status = $("#carSummary_status").val();
		var car_no = $("#carSummary_car_no").val();
		var driver = $("#carSummary_driver").val();
		var transferOrderNo = $("#carSummary_transfer_order").val();
		var order_no = $("#carSummary_pickup_order").val();
		var start_data = $("#carSummary_start_data").val();
		num2 = 1;
		travellingCraneReceipts_table.fnSettings().sAjaxSource = "/yh/carsummary/carSummaryOrderList?status="+status+"&driver="+driver+"&car_no="+car_no+"&transferOrderNo="+transferOrderNo+"&order_no="+order_no+"&start_data="+start_data;
		travellingCraneReceipts_table.fnDraw();
	} );
    
});