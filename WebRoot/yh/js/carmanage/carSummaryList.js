$(document).ready(function() {
	document.title = '行车单查询 | '+document.title;
	$('#menu_carmanage').addClass('active').find('ul').addClass('in');
	
	var pickupIds = [];
	var orderTypes = [];
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
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/carsummary/untreatedCarManageList",
        "aoColumns": [ 
			{ "mDataProp": null,"sWidth":"10px",
				"fnRender": function(obj) {
					unDisposePickuoIds.push("S"+obj.aData.ID);
					return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'" order_type="'+obj.aData.ORDER_TYPE+'">';
				}
			},  
			{ "mDataProp": null,"sWidth":"40px",
					"fnRender": function(obj) {
					return num++;
				}
			}, 
			{"mDataProp":null,"sWidth":"120px",
				"fnRender": function(obj) {
					if(Pickup.isUpdate || Pickup.isCompleted){
						return "<a href='/pickupOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.DEPART_NO+"</a>";
					}else{
						return obj.aData.DEPART_NO;
					}
				}
			},
			{"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"120px"},
			{"mDataProp":"STATUS","sWidth":"60px"},
			{"mDataProp":"CAR_NO","sClass": "CAR_NO","sWidth":"60px"},
			{"mDataProp":"CONTACT_PERSON","sWidth":"60px"},
			{"mDataProp":"PHONE","sWidth":"100px"},
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
			{"mDataProp":"OFFICE_NAME","sWidth":"120px"}, 
			{"mDataProp":null, "sWidth":"60px",
				"fnRender":function(obj){
    				return obj.aData.ATMVOLUME + obj.aData.CARGOVOLUME;
				}
			},
		    {"mDataProp":null, "sWidth":"76px",
				"fnRender":function(obj){
    				return obj.aData.ATMWEIGHT + obj.aData.CARGOWEIGHT;
				}
			},                      
			{"mDataProp":"USER_NAME", "sWidth":"60px"},                        
			{"mDataProp":"REMARK", "sWidth":"150px"}                       
	      ]          
    });
	//行车单查询，dataTable
    var travellingCraneReceipts_table = $('#travellingCraneReceipts_table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
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
					  if(CarSummary.isUpdate || CarSummary.isApproval){
						  return "<a href='/carsummary/edit?carSummaryId="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
					  }else{
						  return obj.aData.ORDER_NO;
					  }
          			
          	  }},  
	          {"mDataProp":"PICKUP_NO", "sWidth":"120px"},
	          {"mDataProp":"TRANSFER_ORDER_NO","sWidth":"120px"},
	          {"mDataProp":"STATUS", "sWidth":"60px"/*,
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
					}*/
	          },
			  {"mDataProp":"CAR_NO", "sWidth":"70px"},
			  {"mDataProp":"MAIN_DRIVER_NAME", "sWidth":"70px"},
			  {"mDataProp":"TURNOUT_TIME", "sWidth":"80px"},
			  {"mDataProp":"RETURN_TIME", "sWidth":"80px"},
			  {"mDataProp":null, "sWidth":"70px",
				  "fnRender": function(obj) {
						/*var data1 = obj.aData.TURNOUT_TIME.split("-");
						var data2 = obj.aData.RETURN_TIME.split("-");
						return data2[2] - data1[2];*/
						if(obj.aData.RETURN_TIME!=null&&obj.aData.TURNOUT_TIME!=null){
							return DateDiff(obj.aData.RETURN_TIME,obj.aData.TURNOUT_TIME);
						}
						else{
							return "";
						}
						
					}
			  },        	
			  {"mDataProp":"VOLUME", "sWidth":"70px"},           
			  {"mDataProp":"WEIGHT", "sWidth":"70px"},        	
			  {"mDataProp":"CARSUMMARYMILEAGE", "sWidth":"80px"},                        
			  {"mDataProp":"MONTH_REFUEL_AMOUNT", "sWidth":"90px"},                        
			  {"mDataProp":"REFUEL_CONSUME", "sWidth":"90px"},                        
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
		unDispose_table.fnSettings().sAjaxSource = "/carsummary/untreatedCarManageList";
		unDispose_table.fnDraw();
	});
    var refreshData=function(){
		var status = $("#status").val();
		var driver = $("#driver").val();
		var car_no = $("#car_no").val();
		var transferOrderNo = $("#transferOrderNo").val();
		var create_stamp = $("#create_stamp").val();
		num = 1;
		unDispose_table.fnSettings().oFeatures.bServerSide = true; 
		unDispose_table.fnSettings().sAjaxSource = "/carsummary/untreatedCarManageList?status="+status+"&driver="+driver+"&car_no="+car_no+"&transferOrderNo="+transferOrderNo+"&create_stamp="+create_stamp;
		unDispose_table.fnDraw();
		saveConditions();
	};
    
	//创建行车单
    $('#saveBtn').click(function(e){
        e.preventDefault();
        if(pickupIds.length > 0){
        	$("#pickupIds").val(pickupIds);
        	$("#orderTypes").val(orderTypes);
            $('#createForm').submit();
            
        }else{
        	alert("请选择要创建的调车单");
        }
    });
    $("#searchBtn").click(function(){
        refreshData();
    });
    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
        saveConditions();
    });
    var saveConditions=function(){
        var conditions={
        	status:$("#status").val(),
            driver :$("#driver").val(),
            car_no:$("#car_no").val(),
            transferOrderNo:$("#transferOrderNo").val(),
            create_stamp : $("#create_stamp").val(),
        };
        if(!!window.localStorage){//查询条件处理
            localStorage.setItem("query_to", JSON.stringify(conditions));
        }
    };
    var loadConditions=function(){
        if(!!window.localStorage){//查询条件处理
            var query_to = localStorage.getItem('query_to');
            if(!query_to)
                return;

            var conditions = JSON.parse(localStorage.getItem('query_to'));
            $("#status").val(conditions.status);
            $("#driver").val(conditions.driver);
            $("#car_no").val(conditions.car_no);
            $("#transferOrderNo").val(conditions.transferOrderNo);
            $("#create_stamp").val(conditions.create_stamp);
        }
    };
    loadConditions();
	// 选中或取消事件
	$("#unDispose_table").on('click', '.checkedOrUnchecked', function(){
		if($(this).prop("checked") == true){
			if(carNo.length != 0){
				var checkData = $(this).parent().siblings('.CREATE_STAMP')[0].innerHTML;
				var dataTo = checkData.split("-");
				var dataFrom = createStamp[0].split("-");
				if(carNo[0] == $(this).parent().siblings('.CAR_NO')[0].innerHTML && $(this).parent().siblings('.CAR_NO')[0].innerHTML != ''){
					if(dataFrom[0] == dataTo[0] && dataFrom[1] == dataTo[1] && dataFrom[2] == dataTo[2]){
						/*var num = unDisposePickuoIds.indexOf(checkedTest[checkedTest.length-1]);
						var select = "S"+$(this).val();
						if(select == unDisposePickuoIds[num-1] || select == unDisposePickuoIds[num+1]){*/
							checkedTest.push("S"+$(this).val());
							pickupIds.push($(this).val());
							orderTypes.push($(this).attr("order_type"));
						/*}else{
							//alert("请选择连续性出车日期的调车单!");
							$.scojs_message('请选择连续性出车日期的调车单!', $.scojs_message.TYPE_ERROR);
							return false;
						}*/
					}else{
						//alert("请选择出车日期为同一个月的调车单!");
						$.scojs_message('请选择出车日期为同一天的调车单!', $.scojs_message.TYPE_ERROR);
						return false;
					}
				}else{
					//alert("请选择同一车辆!");
					$.scojs_message('请选择同一车辆!', $.scojs_message.TYPE_ERROR);
					return false;
				}
			}else{
				pickupIds.push($(this).val());
				orderTypes.push($(this).attr("order_type"));
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
					orderTypes.splice(orderTypes.indexOf($(this).attr("order_type")), 1); 
					carNo.splice($(this).parent().siblings('.CAR_NO')[0].innerHTML, 1);
					createStamp.splice($(this).parent().siblings('.CREATE_STAMP')[0].innerHTML, 1);
					if(checkedTest.length == 0)
						$("#saveBtn").attr('disabled', true);
				}
				else{
					//alert("按连续性要求，您只能取消开头或最后的调车单！");
					$.scojs_message('按连续性要求，您只能取消开头或最后的调车单', $.scojs_message.TYPE_ERROR);
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
		travellingCraneReceipts_table.fnSettings().sAjaxSource = "/carsummary/carSummaryOrderList";
		travellingCraneReceipts_table.fnDraw();
	});
	//行车单查询各种搜索
	refreshData1=function () {
		var status = $("#carSummary_status").val();
		var car_no = $("#carSummary_car_no").val();
		var driver = $("#carSummary_driver").val();
		var transferOrderNo = $("#carSummary_transfer_order").val();
		var order_no = $("#carSummary_pickup_order").val();
		var start_data = $("#carSummary_start_data").val();
		num2 = 1;
		travellingCraneReceipts_table.fnSettings().sAjaxSource = "/carsummary/carSummaryOrderList?status="+status+"&driver="+driver+"&car_no="+car_no+"&transferOrderNo="+transferOrderNo+"&order_no="+order_no+"&start_data="+start_data;
		travellingCraneReceipts_table.fnDraw();
		saveConditions1();
	};
	$("#searchBtn1").click(function(){
        refreshData1();
    });
    $("#resetBtn1").click(function(){
        $('#searchForm1')[0].reset();
        saveConditions1();
    });
    var saveConditions1=function(){
        var conditions={
        	status:$("#carSummary_status").val(),
        	car_no :$("#carSummary_car_no").val(),
            driver:$("#carSummary_driver").val(),
            transfer_order:$("#carSummary_transfer_order").val(),
            pickup_order : $("#carSummary_pickup_order").val(),
            start_data : $("#carSummary_start_data").val(),
        };
        if(!!window.localStorage){//查询条件处理
            localStorage.setItem("query_to_one", JSON.stringify(conditions));
        }
    };
    var loadConditions1=function(){
        if(!!window.localStorage){//查询条件处理
            var query_to = localStorage.getItem('query_to_one');
            if(!query_to)
                return;
            var conditions = JSON.parse(localStorage.getItem('query_to_one'));
            $("#carSummary_status").val(conditions.status);
            $("#carSummary_driver").val(conditions.driver);
            $("#carSummary_car_no").val(conditions.car_no);
            $("#carSummary_transfer_order").val(conditions.transfer_order);
            $("#carSummary_pickup_order").val(conditions.pickup_order);
            $("#carSummary_start_data").val(conditions.start_data);
        }
    };
    loadConditions1();
    
});
/*function DateDiff(d1,d2){ 
    var day = 24 * 60 * 60 *1000; 
	try{     
	   var dateArr = d1.split("-"); 
	   var checkDate = new Date(); 
	   checkDate.setFullYear(dateArr[0], dateArr[1]-1, dateArr[2]); 
	   var checkTime = checkDate.getTime(); 
	   
	   var dateArr2 = d2.split("-"); 
	   var checkDate2 = new Date(); 
	   checkDate2.setFullYear(dateArr2[0], dateArr2[1]-1, dateArr2[2]); 
	   var checkTime2 = checkDate2.getTime(); 
	     
	   var cha = (checkTime - checkTime2)/day;   
	        return cha; 
    }catch(e){ 
    	return false; 
    }
};*/
function DateDiff(sDate1, sDate2) {  //sDate1和sDate2是yyyy-MM-dd格式
    var aDate, oDate1, oDate2, iDays;
    aDate = sDate1.split("-");
    oDate1 = new Date(aDate[1] + '-' + aDate[2] + '-' + aDate[0]);  //转换为yyyy-MM-dd格式
    aDate = sDate2.split("-");
    oDate2 = new Date(aDate[1] + '-' + aDate[2] + '-' + aDate[0]);
    iDays = parseInt(Math.abs(oDate1 - oDate2) / 1000 / 60 / 60 / 24); //把相差的毫秒数转换为天数
    var beginHour = parseInt(iDays, 10);
    return beginHour;  //返回相差天数
}