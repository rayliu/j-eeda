$(document).ready(function() {
	$('#menu_carmanage').addClass('active').find('ul').addClass('in');
	
	var pickupIds = [];
	var carNo = [];
    var createStamp = [];
	var num = 1;
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
					unDisposePickuoIds.push(obj.aData.ID);
					return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
				}
			},  
			{ "mDataProp": null,"sWidth":"40px",
					"fnRender": function(obj) {
					return num++;
				}
			}, 
			{"mDataProp":"DEPART_NO","sWidth":"150px",
					"fnRender": function(obj) {
					return "<a href='/yh/pickupOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.DEPART_NO+"</a>";
				}
			},
			{"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"200px"},
			{"mDataProp":"STATUS","sWidth":"100px"},
			{"mDataProp":"CAR_NO","sClass": "CAR_NO","sWidth":"150px"},
			{"mDataProp":"DRIVER","sWidth":"80px"},
			{"mDataProp":"CREATE_STAMP","sClass": "CREATE_STAMP","sWidth":"200px"}, 
			{"mDataProp":"ROUTE_FROM","sWidth":"150px"}, 
			{"mDataProp":"CAR_FOLLOW_NAME", "sWidth":"150px"},  
			
			{"mDataProp":null, "sWidth":"150px"},                        
			{"mDataProp":null, "sWidth":"150px"},                        
			{"mDataProp":null, "sWidth":"150px"},                        
			{"mDataProp":null, "sWidth":"150px"},                        
			{"mDataProp":"REMARK", "sWidth":"200px"}                       
	      ]          
    });
	//行车单查询，dataTable
    var travellingCraneReceipts_table = $('#travellingCraneReceipts_table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "",
    	"aoColumns": [ 
	          {"mDataProp": null},  
	          {"mDataProp":null, "bVisible": false},
	          {"mDataProp":null},
	          {"mDataProp":null, "sWidth":"120px"},
			  {"mDataProp":null, "sWidth":"200px"},
			  {"mDataProp":null, "sWidth":"150px"},
			  {"mDataProp":null, "sWidth":"200px"},
			  {"mDataProp":null, "sWidth":"200px"},
			  {"mDataProp":null, "sWidth":"200px"},        	
			  {"mDataProp":null, "sWidth": "120px"},           
			  {"mDataProp":null, "sWidth":"150px"},        	
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"200px"}                      
		]          
    });
    
    //时间按钮
    $('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN',
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
	    $('#create_stamp').trigger('keyup');
	});
    
    //未处理行车单各种搜索
    $('#status ,#driver ,#car_no ,#transferOrderNo ,#create_stamp').on( 'keyup click', function () {
		var status = $("#status").val();
		var driver = $("#driver").val();
		var car_no = $("#car_no").val();
		var transferOrderNo = $("#transferOrderNo").val();
		var create_stamp = $("#create_stamp").val();
		num = 1;
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
	
	// 选中或取消事件(未完成)
	$("#unDispose_table").on('click', '.checkedOrUnchecked', function(){
		if($(this).prop("checked") == true){
			if(carNo.length != 0){
				var checkData = $(this).parent().siblings('.CREATE_STAMP')[0].innerHTML;
				var dataTo = checkData.split("-");
				var dataFrom = createStamp[0].split("-");
				
				if(carNo[0] != $(this).parent().siblings('.CAR_NO')[0].innerHTML && $(this).parent().siblings('.CAR_NO')[0].innerHTML != ''){
					alert("请选择同一车辆!");
					return false;
				}else if(dataFrom[0] != dataTo[0] && dataFrom[1] != dataTo[1]){
					
					alert("请选择连续性出车日期(同一个月或同一天)!");
					return false;
				}else{
					pickupIds.push($(this).val());
				}
			}else{
				pickupIds.push($(this).val());
				carNo.push($(this).parent().siblings('.CAR_NO')[0].innerHTML);
				createStamp.push($(this).parent().siblings('.CREATE_STAMP')[0].innerHTML);
				$("#saveBtn").attr('disabled', false);
			}
		}else{
			if(carNo.length != 0){
				carNo.splice($(this).parent().siblings('.CAR_NO')[0].innerHTML, 1);
			}else{
				$("#saveBtn").attr('disabled', true);
			}
			if(createStamp.length != 0){
				createStamp.splice($(this).parent().siblings('.CREATE_STAMP')[0].innerHTML, 1);
			}
			if(pickupIds.length != 0){
				pickupIds.splice(pickupIds.indexOf($(this).val()), 1); 
			}
		}
		
	});
	
});