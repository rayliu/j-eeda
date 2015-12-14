
$(document).ready(function() {
	$('#menu_deliver').addClass('active').find('ul').addClass('in');
	
	var clickTabId = "chargeCheckOrderbasic";
	
	// 列出所有的司机
	 /*red*/
	 $('#driver').on('keyup click', function(){
		var inputStr = $('#driver').val();
		//定义一个TYPE变量，用来作为车辆的条件
		var party_type = "SP";
		$.get('/deliveryPlanOrder/searchAllCarInfo', {input:inputStr,name:"driver", type:party_type}, function(data){
			console.log(data);
			var driverList = $("#driverList");
			driverList.empty();
			for(var i = 0; i < data.length; i++)
			{
				driverList.append("<li><a tabindex='-1' class='fromLocationItem' carid='"+data[i].ID+"' phone='"+data[i].PHONE+"' carno='"+data[i].CAR_NO+"' driver='"+data[i].DRIVER+"'> "+data[i].DRIVER+"</a></li>");
			}
		},'json');
		
		$("#driverList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
       }); 
       $('#driverList').show();
	 });
	  	
	 // 选中司机
	 $('#driverList').on('mousedown', '.fromLocationItem', function(e){	
		 $("#carInfoId").val($(this).attr('carid'));
		 $("#driver").val($(this).attr('driver'));
	  	 $('#car_no').val($(this).attr('carno'));
	  	 $('#phone').val($(this).attr('phone'));  	 
	     $('#driverList').hide();   
    });

	// 没选中司机，焦点离开，隐藏列表
	$('#driver').on('blur', function(){
 		$('#driverList').hide();
 	});
	
	// 列出所有的车辆
	$('#car_no').on('keyup click', function(){
		var inputStr = $('#car_no').val();
		//定义一个TYPE变量，用来作为车辆的条件
		var typeStr = "SP";
		$.get('/deliveryPlanOrder/searchAllCarInfo', {input:inputStr,name:"car_no",type:typeStr}, function(data){
			console.log(data);
			var carNoList = $("#carNoList");
			carNoList.empty();
			for(var i = 0; i < data.length; i++)
			{
				carNoList.append("<li><a tabindex='-1' class='fromLocationItem' carid='"+data[i].ID+"' phone='"+data[i].PHONE+"' carno='"+data[i].CAR_NO+"' driver='"+data[i].DRIVER+"'> "+data[i].CAR_NO+"</a></li>");
			}
	},'json');

	$("#carNoList").css({ 
      	left:$(this).position().left+"px", 
      	top:$(this).position().top+32+"px" 
     }); 
     $('#carNoList').show();
	});
	 	
	// 选中车辆
	$('#carNoList').on('mousedown', '.fromLocationItem', function(e){		
		$("#carInfoId").val($(this).attr('carid'));
		$("#driver").val($(this).attr('driver'));
	  	$('#car_no').val($(this).attr('carno'));
	  	$('#phone').val($(this).attr('phone'));    	 
	    $('#carNoList').hide();   
   });

	// 没选中车辆，焦点离开，隐藏列表
	$('#car_no').on('blur', function(){
		$('#carNoList').hide();
	});
	
	if($("#driver").val() == ''){
		$("#driver").val($("#carInfoDriverMessage").val());
	}
	
	if($("#driver").val() == ''){
		$("#driver").val($("#carInfoDriverPhone").val());
	}
	
	$('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#turnout_time').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#return_time').trigger('keyup');
    });
    
    //线路, 动态处理
    var routeTbody =$('#routeTbody').dataTable({
    	"bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
        "bLengthChange":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			return nRow;
		},
        "aoColumns": [   
            {"mDataProp":"ORDER_NO"},
            {"mDataProp":"CUSTOMER"},
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"REMARK"},
        ]      
    });	
    
    //tab 基本信息
    $("#chargeCheckOrderbasic").click(function(e){
		clickTabId = e.target.getAttribute("id");
	});
    
    //tab 线路
    $("#addressList").click(function(e){
    	if(clickTabId == "chargeCheckOrderbasic"){
    		//提交前，校验数据
            if(!$("#deliveryPlanOrderForm").valid()){
            	$.scojs_message('请先保存运输单', $.scojs_message.TYPE_OK);
    	       	return false;
            }else{
            	saveDeliveryPlanOrderData();
            }
		}
		clickTabId = e.target.getAttribute("id");
    	var deliveryOrderIds = $("#deliveryOrderIds").val();
    	if(deliveryOrderIds != "" && deliveryOrderIds != null){
	    	routeTbody.fnSettings().oFeatures.bServerSide = true; 
	    	routeTbody.fnSettings().sAjaxSource = "/deliveryPlanOrder/findDeliveryPlanRoute?deliveryOrderIds="+deliveryOrderIds;   
	    	routeTbody.fnDraw();
    	}
	});
    
    //货品信息, 动态处理
	var deliveryItemTboby = $('#deliveryItemTboby').dataTable({
		"bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
        "bLengthChange":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			return nRow;
		},
        "aoColumns": [
            { "mDataProp": "CUSTOMER","sWidth": "170px" },
            { "mDataProp": "ORDER_NO","sWidth": "120px"},      
            { "mDataProp": "ITEM_NO"},
            { "mDataProp": "ITEM_NAME"},
            { "mDataProp": "PIECES"},
            { "mDataProp": "VOLUME"},
            { "mDataProp": "WEIGHT"},
            { "mDataProp": "REMARK"}
            /*{ 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success dateilEdit' code='?id="+obj.aData.ID+"'>"+
                                "<i class='fa fa-search fa-fw'></i>"+
                                "查看"+
                            "</a>";
                },
            }*/                                       
        ]
    });
    
    //tab 货品信息
    $("#itemList").click(function(e){
    	if(clickTabId == "chargeCheckOrderbasic"){
    		//提交前，校验数据
            if(!$("#deliveryPlanOrderForm").valid()){
            	$.scojs_message('请先保存运输单', $.scojs_message.TYPE_OK);
    	       	return false;
            }else{
            	saveDeliveryPlanOrderData();
            }
		}
		clickTabId = e.target.getAttribute("id");
    	var deliveryOrderIds = $("#deliveryOrderIds").val();
    	if(deliveryOrderIds != "" && deliveryOrderIds != null){
    		deliveryItemTboby.fnSettings().oFeatures.bServerSide = true; 
    		deliveryItemTboby.fnSettings().sAjaxSource = "/deliveryPlanOrder/findDeliveryOrderItems?deliveryOrderIds="+deliveryOrderIds;   
    		deliveryItemTboby.fnDraw();
    	}
	});
    
 	//from表单验证
	var validate = $('#deliveryPlanOrderForm').validate({
        rules: {
        	turnout_time: {required: true}
        },
        messages : {	             
        }
    });
    
    //保存、修改数据
 	var saveDeliveryPlanOrderData = function(){
 		
 		$("#saveDeliveryPlanOrderBtn").prop("disabled",true);
 		//判断行车单是否审核
		$.post('/deliveryPlanOrder/saveDeliveryPlanOrder', $("#deliveryPlanOrderForm").serialize(), function(data){
 			if(data != null){
 				$("#deliveryPlanOrderId").val(data.ID);
 				$("#showOrderNo").text(data.ORDER_NO);
 				$("#showStatus").text(data.STATUS);
 				$.post('/costReimbursement/findUser', {"userId":data.CREATE_ID}, function(data){
 					$("#showCreateName").text(data.USER_NAME);
 				});
 				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
 			}else{
 				$.scojs_message('保存失败', $.scojs_message.TYPE_OK);
 			}
 			$("#saveDeliveryPlanOrderBtn").prop("disabled",false);
 		},'json');
 	};
 	
 	//点击保存
	$("#saveDeliveryPlanOrderBtn").click(function(e){
		saveDeliveryPlanOrderData();
	});
    
	//货品信息, 动态处理
	var milestoneTbody = $('#milestoneTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
        "bLengthChange":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			return nRow;
		},
        "aoColumns": [
            { "mDataProp": "STATUS"},
            { "mDataProp": "ADDRESS"},      
            { "mDataProp": null,
                "fnRender": function(obj) {
                    if(obj.aData.C_NAME != "" && obj.aData.C_NAME != null)
                        return obj.aData.C_NAME;
                    else
                        return obj.aData.USER_NAME;
                    
                }
            },

            { "mDataProp": "CREATE_STAMP"}
            /*{ 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success dateilEdit' code='?id="+obj.aData.ID+"'>"+
                                "<i class='fa fa-search fa-fw'></i>"+
                                "查看"+
                            "</a>";
                },
            }*/                                       
        ]
    });
    
    //tab 运输里程碑
    $("#milestoneList").click(function(e){
    	if(clickTabId == "chargeCheckOrderbasic"){
    		//提交前，校验数据
            if(!$("#deliveryPlanOrderForm").valid()){
            	$.scojs_message('请先保存运输单', $.scojs_message.TYPE_OK);
    	       	return false;
            }else{
            	saveDeliveryPlanOrderData();
            }
		}
		clickTabId = e.target.getAttribute("id");
    	var deliveryPlanOrderId = $("#deliveryPlanOrderId").val();
    	if(deliveryPlanOrderId != "" && deliveryPlanOrderId != null){
    		milestoneTbody.fnSettings().oFeatures.bServerSide = true; 
    		milestoneTbody.fnSettings().sAjaxSource = "/deliveryPlanOrder/findDeliveryOrderMilestone?deliveryPlanOrderId="+deliveryPlanOrderId;   
    		milestoneTbody.fnDraw();
    	}
	});
	
    // 保存新里程碑
	$("#saveDeliveryPlanOrderMilestoneBtn").click(function(){
		
		var deliveryPlanOrderId = $("#deliveryPlanOrderId").val();
		var status = $("#status").val();
		var location = $("#location").val();
		$.post('/deliveryPlanOrder/updateDeliveryPlanOrderMilestone',{deliveryPlanOrderId:deliveryPlanOrderId,status:status,location:location},function(data){
    		milestoneTbody.fnSettings().sAjaxSource = "/deliveryPlanOrder/findDeliveryOrderMilestone?deliveryPlanOrderId="+deliveryPlanOrderId;   
    		milestoneTbody.fnDraw();
		},'json');
		$("#status").val("");
		$("#location").val("");
		$('#transferOrderMilestone').modal('hide');
	});
    
});