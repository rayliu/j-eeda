$(document).ready(function() {
	
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
    '</div>';
	$('body').append(alerMsg);
	
	// 判断货场是否选中
	$("#checkbox1").click(function(){
		if($(this).prop('checked') == true){
			$("#addressDiv").show();
		}else{
			$("#addressDiv").hide();			
		}
	});
	
	// 列出所有的提货地点
	$("#addressList").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var bool = false;
		if("chargeCheckOrderbasic" == parentId){
			bool= true;
		}
		
        if($("#pickupOrderId").val() == ""){
	    	$.post('/yh/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
				$("#pickupOrderId").val(data.ID);
				$("#addressPickupOrderId").val(data.ID);
				$("#milestonePickupId").val(data.ID);
				if(data.ID>0){
					$("#pickupId").val(data.ID);
			        showFinishBut();
					findAllAddress();
				  	//$("#style").show();
				  	choiceExternalTransferOrder();
			        if($("#transferOrderType").val() == 'replenishmentOrder'){
			        	
			        }
			        if(bool){
			        	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			        }
				}else{
					$.scojs_message('保存失败', $.scojs_message.TYPE_OK);
				}
			},'json');
        }else{
        	$.post('/yh/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
				$("#pickupOrderId").val(data.ID);
				$("#addressPickupOrderId").val(data.ID);
				$("#milestonePickupId").val(data.ID);
				if(data.ID>0){		
					$("#pickupId").val(data.ID);	
			        showFinishBut();
					findAllAddress();
				  	//$("#style").show();	 
				  	choiceExternalTransferOrder();
			        if($("#transferOrderType").val() == 'replenishmentOrder'){
			        	
			        }
			        if(bool){
			        	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			        }
				}else{
					$.scojs_message('保存失败', $.scojs_message.TYPE_OK);
				}
			},'json');
        }
        parentId = e.target.getAttribute("id");
	});
	
	// 判断仓库是否选中
	$("#checkbox2").click(function(){
		if($(this).prop('checked') == true){
			$("#warehouseDiv").show();
		}else{
			$("#warehouseDiv").hide();			
		}
	});
	
	// 获取所有仓库
	$.post('/yh/transferOrder/searchAllWarehouse',function(data){
		if(data.length > 0){
		 var gateInSelect = $("#gateInSelect");
		 gateInSelect.empty();
		 var hideWarehouseId = $("#hideWarehouseId").val();
		 var WarehouseId = $("#replenishmentOrderId").val();
		 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideWarehouseId || data[i].ID == WarehouseId){
					 gateInSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].WAREHOUSE_NAME+"</option>");					 
				 }else{
					 gateInSelect.append("<option value='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</option>");
				 }
			}
		}
	},'json');
	
	// 列出所有的主司机
	$('#main_driver_name').on('keyup click', function(){
		var inputStr = $('#main_driver_name').val();
		
		$.get('/yh/carsummary/searchAllDriver', {input:inputStr}, function(data){
			console.log(data);
			var driverList1 = $("#driverList1");
			driverList1.empty();
			for(var i = 0; i < data.length; i++)
			{
				driverList1.append("<li><a tabindex='-1' class='fromLocationItem' pid='"+data[i].ID+"' > "+data[i].DRIVER+"</a></li>");
			}
		},'json');
		
		$("#driverList1").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
       }); 
       $('#driverList1').show();
	 });
	
	// 选中主司机
 	$('#driverList1').on('mousedown', '.fromLocationItem', function(e){	
 		 //$("#driver_id").val($(this).attr('pid'));
	  	 $('#main_driver_name').val($(this).html());
	     $('#driverList1').hide();   
     });
 	
 	// 没选中主司机，焦点离开，隐藏列表
 	$('#main_driver_name').on('blur', function(){
  		$('#driverList1').hide();
  	});
 	
 	//列出所有的副司机
 	$('#minor_driver_name').on('keyup click', function(){
		var inputStr = $('#minor_driver_name').val();
		
		$.get('/yh/carsummary/searchAllDriver', {input:inputStr}, function(data){
			console.log(data);
			var driverList1 = $("#driverList2");
			driverList1.empty();
			for(var i = 0; i < data.length; i++)
			{
				driverList1.append("<li><a tabindex='-1' class='fromLocationItem' pid='"+data[i].ID+"' > "+data[i].DRIVER+"</a></li>");
			}
		},'json');
		
		$("#driverList2").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
       }); 
       $('#driverList2').show();
	 });
 	
 	// 选中副司机
 	$('#driverList2').on('mousedown', '.fromLocationItem', function(e){	
 		 //$("#driver_id").val($(this).attr('pid'));
	  	 $('#minor_driver_name').val($(this).html());
	     $('#driverList2').hide();   
    });
 	
 	// 没选副司机，焦点离开，隐藏列表
 	$('#minor_driver_name').on('blur', function(){
  		$('#driverList2').hide();
  	});
	
 	//保存创建或修改数据
 	var saveCarSummaryData = function(){
 		var result = $("#saveCarSummaryBtn").attr("disabled");
 		if(!result){
 			$.post('/yh/carsummary/saveCarSummary', $("#carSummaryForm").serialize(), function(data){
 	 			if(data != null){
 	 				$("#saveCarSummaryBtn").attr("disabled", true);
 	 				$("#car_summary_id").val(data);
 	 				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
 	 			}else{
 	 				$.scojs_message('保存失败', $.scojs_message.TYPE_OK);
 	 			}
 	 		},'json');
 		}
 	};
 	
 	//点击保存
	$("#saveCarSummaryBtn").click(function(e){
		saveCarSummaryData();
	});
	//刷新线路
	var pickupAddressTbody = $('#pickupAddressTbody').dataTable({           
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [
            { "mDataProp": "DEPART_NO"},
            { "mDataProp": "ORDER_NO"},      
            { "mDataProp": "ABBR"},
            { "mDataProp": "ADDRESS1"},
            { "mDataProp": "CREATE_STAMP"},
            { "mDataProp": null,
           	 "fnRender": function(obj) {    
           		 if(obj.aData.ADDRESS2 != "" && obj.aData.ADDRESS2 != null && obj.aData.ADDRESS3 != "" && obj.aData.ADDRESS3 != null){
          			 return "货场（"+obj.aData.ADDRESS2+"）仓库（"+obj.aData.ADDRESS3+"）";
          		 }
           		 if(obj.aData.ADDRESS3 != "" && obj.aData.ADDRESS3 != ""){
           			 return "仓库（"+obj.aData.ADDRESS3+"）";
           		 }
           		 if(obj.aData.ADDRESS2 != "" && obj.aData.ADDRESS2 != "" ){
           			 return "货场（"+obj.aData.ADDRESS2+"）";
           		 }

                }
            }
        ]        
    });
	
	// 选显卡-线路
	$("#carmanageLine").click(function(){
		saveCarSummaryData();
		var pickupIds = $("#pickupIds").val();
		pickupAddressTbody.fnSettings().oFeatures.bServerSide = true; 
		pickupAddressTbody.fnSettings().sAjaxSource = "/yh/carsummary/findAllAddress?pickupIds="+pickupIds;   
		pickupAddressTbody.fnDraw();
	});
	
	//刷新货品信息
	var pickupItemTbody = $('#pickupItemTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [
            { "mDataProp": "DEPART_NO"},
            { "mDataProp": "ORDER_NO"},
            { "mDataProp": "CUSTOMER"},
            { "mDataProp": "ITEM_NO"},
            { "mDataProp": "ITEM_NAME"},
            { "mDataProp": "AMOUNT"},
            { "mDataProp": "VOLUME"},
            { "mDataProp": "WEIGHT"},
            { "mDataProp": "REMARK"},
        ]
    });
	
	// 选显卡-货品信息
	$("#carmanageItemList").click(function(){
		saveCarSummaryData();
		var pickupIds = $("#pickupIds").val();
		pickupItemTbody.fnSettings().oFeatures.bServerSide = true;
		pickupItemTbody.fnSettings().sAjaxSource = "/yh/carsummary/findPickupOrderItems?pickupIds="+pickupIds;   
		pickupItemTbody.fnDraw();
	});
	//刷新里程碑
	var pickupMilestoneTbody = $('#pickupMilestoneTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
         "aoColumns": [
             { "mDataProp": "STATUS"},
             { "mDataProp": "USER_NAME"},
             { "mDataProp": "CREATE_STAMP"}
         ]
	});
	// 选项卡-里程碑
	$("#carmanageMilestoneList").click(function(){
		saveCarSummaryData();
		var pickupIds = $("#pickupIds").val();
		pickupMilestoneTbody.fnSettings().oFeatures.bServerSide = true;
		pickupMilestoneTbody.fnSettings().sAjaxSource = "/yh/carsummary/transferOrderMilestoneList?pickupIds="+pickupIds;   
		pickupMilestoneTbody.fnDraw();
	});
	
	
	//刷新路桥费明细
 	var carSummaryDetailRouteFeeTbody = $('#carSummaryDetailRouteFeeTbody').dataTable({
 		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
         "fnRowCallback": function(nRow, aData) {
 			$(nRow).attr('id', aData.ID);
 			return nRow;
 		 },
         "aoColumns": [
             { "mDataProp": "ITEM"},
             { "mDataProp": "CHARGE_DATA"},
             { "mDataProp": "CHARGE_SITE",
            	 "fnRender": function(obj) {
                     if(obj.aData.CHARGE_SITE!='' && obj.aData.CHARGE_SITE != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='charge_site' value='"+obj.aData.CHARGE_SITE+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='charge_site'>";
                     }
                 }
             },
             { "mDataProp": "TRAVEL_AMOUNT",
            	 "fnRender": function(obj) {
                     if(obj.aData.TRAVEL_AMOUNT!='' && obj.aData.TRAVEL_AMOUNT != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='travel_amount' value='"+obj.aData.TRAVEL_AMOUNT+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='travel_amount'>";
                     }
                 }
             },
             { "mDataProp": "REMARK",
            	 "fnRender": function(obj) {
                     if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='remark'>";
                     }
                 }
             },
             { "mDataProp": null, 
                "sWidth": "60px",  
                 "fnRender": function(obj) {
                     return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
               		"<i class='fa fa-trash-o fa-fw'> </i> "+
               		"删除明细"+
               		"</a>";
                 }
             }   
         ]
	});
	
	// 选项卡-路桥费明细
	$("#carmanageRoadBridge").click(function(e){
		saveCarSummaryData();
		var car_summary_id = $("#car_summary_id").val();
		if(car_summary_id != "" && car_summary_id != null){
			carSummaryDetailRouteFeeTbody.fnSettings().oFeatures.bServerSide = true;
			carSummaryDetailRouteFeeTbody.fnSettings().sAjaxSource = "/yh/carsummary/findCarSummaryRouteFee?car_summary_id="+car_summary_id;   
			carSummaryDetailRouteFeeTbody.fnDraw();
		}
	});
	
	// 新增路桥费明细
	$("#addCarSummaryRouteFee").click(function(e){
		var car_summary_id = $("#car_summary_id").val();
		if(car_summary_id != ""){
			$.post('/yh/carsummary/addCarSummaryRouteFee/'+car_summary_id,function(data){
				console.log(data);
				if(data.success){
					carSummaryDetailRouteFeeTbody.fnSettings().sAjaxSource = "/yh/carsummary/findCarSummaryRouteFee?car_summary_id="+car_summary_id;   
					carSummaryDetailRouteFeeTbody.fnDraw();
				}else{
					$.scojs_message('操作失败', $.scojs_message.TYPE_OK);
				}
			});	
		}else{
			$.scojs_message('操作失败', $.scojs_message.TYPE_OK);
		}
		
	});
	
	//异步删除路桥费明细
	$("#carSummaryDetailRouteFeeTbody").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		var car_summary_id = $("#car_summary_id").val();
		$.post('/yh/carsummary/delCarSummaryRouteFee/'+id,function(data){
			console.log(data);
			carSummaryDetailRouteFeeTbody.fnSettings().sAjaxSource = "/yh/carsummary/findCarSummaryRouteFee?car_summary_id="+car_summary_id;   
			carSummaryDetailRouteFeeTbody.fnDraw();
			
        });
	});
	//修改路桥费明细
	$("#carSummaryDetailRouteFeeTbody").on('blur', 'input', function(e){
		e.preventDefault();
		var routeFeeId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		console.log("routeFeeId:"+routeFeeId+",name:"+name+",value:"+value);
		$.post('/yh/carsummary/updateCarSummaryDetailRouteFee', {routeFeeId:routeFeeId, name:name, value:value}, function(data){
			if(data.success){
			}else{
				$.scojs_message('操作失败', $.scojs_message.TYPE_OK);
			}
    	},'json');
	});	
	//刷新加油记录
 	var carSummaryDetailOilFeeTbody = $('#carSummaryDetailOilFeeTbody').dataTable({
 		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
         "fnRowCallback": function(nRow, aData) {
 			$(nRow).attr('id', aData.ID);
 			return nRow;
 		 },
         "aoColumns": [
             { "mDataProp": "ITEM", "sWidth":"50px"},
             /*{ "mDataProp": "REFUEL_DATA", "sWidth":"150px",
            	 "fnRender": function(obj) {
                     if(obj.aData.ODOMETER_MILEAGE!='' && obj.aData.ODOMETER_MILEAGE != null){
                         return "<div id='datetimepicker' class='input-append date'><input type='text' name='refuel_data' id='refuel_data' value='"+obj.aData.REFUEL_DATA+"'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar'></i></span></div>";
                     }else{
                     	 return "<div id='datetimepicker' class='input-append date'><input type='text' name='refuel_data' id='refuel_data'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar'></i></span></div>";
                     }
                 }
             },*/
             { "mDataProp": "REFUEL_DATA", "sWidth":"150px"},
             { "mDataProp": "ODOMETER_MILEAGE", "sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.ODOMETER_MILEAGE!='' && obj.aData.ODOMETER_MILEAGE != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='odometer_mileage' value='"+obj.aData.ODOMETER_MILEAGE+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='odometer_mileage'>";
                     }
                 }
             },
             { "mDataProp": "REFUEL_SITE", "sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REFUEL_SITE !='' && obj.aData.REFUEL_SITE != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_site' value='"+obj.aData.REFUEL_SITE+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_site'>";
                     }
                 }
             },
             { "mDataProp": "REFUEL_TYPE","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REFUEL_TYPE!='' && obj.aData.REFUEL_TYPE != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_type' value='"+obj.aData.REFUEL_TYPE+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_type'>";
                     }
                 }
             },
             { "mDataProp": "REFUEL_UNIT_COST","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REFUEL_UNIT_COST!='' && obj.aData.REFUEL_UNIT_COST != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_unit_cost' value='"+obj.aData.REFUEL_UNIT_COST+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_unit_cost'>";
                     }
                 }
             },
             { "mDataProp": "REFUEL_NUMBER","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REFUEL_NUMBER !='' && obj.aData.REFUEL_NUMBER != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_number' value='"+obj.aData.REFUEL_NUMBER+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_number'>";
                     }
                 }
             },
             { "mDataProp": "REFUEL_AMOUNT","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REFUEL_AMOUNT!='' && obj.aData.REFUEL_AMOUNT != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_amount' value='"+obj.aData.REFUEL_AMOUNT+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_amount'>";
                     }
                 }
             },
             { "mDataProp": "PAYMENT_TYPE","sWidth":"80px",
            	 "fnRender": function(obj) {
            		 var str="";
                     $("#paymentTypeList").children().each(function(){
                 		if(obj.aData.PAYMENT_TYPE == $(this).text()){
                 			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
                 		}else{
                 			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
                 		}
                 	 });
                     return "<select name='payment_type'>"+str+"</select>";
                 }
             },
             { "mDataProp": "LOAD_AMOUNT","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.LOAD_AMOUNT!='' && obj.aData.LOAD_AMOUNT != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='load_amount' value='"+obj.aData.LOAD_AMOUNT+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='load_amount'>";
                     }
                 }
             },
             { "mDataProp": "AVG_ECON","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.AVG_ECON!='' && obj.aData.AVG_ECON != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='avg_econ' value='"+obj.aData.AVG_ECON+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='avg_econ'>";
                     }
                 }
             },
             { "mDataProp": "REMARK","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='remark'>";
                     }
                 }
             },
             { "mDataProp": null, "sWidth":"120px",
                 "sWidth": "60px",  
                 "fnRender": function(obj) {
                     return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
               		"<i class='fa fa-trash-o fa-fw'> </i> "+
               		"删除明细"+
               		"</a>";
                 }
	         }  
         ]
	});
	
	// 选项卡-加油记录
	$("#carmanageRefuel").click(function(e){
		saveCarSummaryData();
		var car_summary_id = $("#car_summary_id").val();
		if(car_summary_id != "" && car_summary_id != null){
			carSummaryDetailOilFeeTbody.fnSettings().oFeatures.bServerSide = true;
			carSummaryDetailOilFeeTbody.fnSettings().sAjaxSource = "/yh/carsummary/findCarSummaryDetailOilFee?car_summary_id="+car_summary_id;   
			carSummaryDetailOilFeeTbody.fnDraw();
		}
		
		/*//时间按钮
	    $('#datetimepicker').datetimepicker({  
		    format: 'yyyy-MM-dd',  
		    language: 'zh-CN',
		    autoclose: true,
		    pickerPosition: "bottom-left"
		}).on('changeDate', function(ev){
		    $('#refuel_data').trigger('keyup');
		});*/
		
		
	});
	// 新增加油记录
	$("#addCarSummaryDetailOilFee").click(function(e){
		var car_summary_id = $("#car_summary_id").val();
		if(car_summary_id != ""){
			$.post('/yh/carsummary/addCarSummaryDetailOilFee/'+car_summary_id,function(data){
				console.log(data);
				if(data.success){
					carSummaryDetailOilFeeTbody.fnSettings().sAjaxSource = "/yh/carsummary/findCarSummaryDetailOilFee?car_summary_id="+car_summary_id;   
					carSummaryDetailOilFeeTbody.fnDraw();
				}else{
					$.scojs_message('操作失败', $.scojs_message.TYPE_OK);
				}
			});	
		}else{
			$.scojs_message('操作失败', $.scojs_message.TYPE_OK);
		}
	});
	
	//异步删除加油记录
	$("#carSummaryDetailOilFeeTbody").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		var car_summary_id = $("#car_summary_id").val();
		$.post('/yh/carsummary/delCarSummaryDetailOilFee/'+id,function(data){
			console.log(data);
			carSummaryDetailOilFeeTbody.fnSettings().sAjaxSource = "/yh/carsummary/findCarSummaryDetailOilFee?car_summary_id="+car_summary_id;   
			carSummaryDetailOilFeeTbody.fnDraw();
			
        });
	});
	//修改加油记录
	$("#carSummaryDetailOilFeeTbody").on('blur', 'input,select', function(e){
		e.preventDefault();
		var routeFeeId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		console.log("routeFeeId:"+routeFeeId+",name:"+name+",value:"+value);
		$.post('/yh/carsummary/updateCarSummaryDetailOilFee', {routeFeeId:routeFeeId, name:name, value:value}, function(data){
			if(data.success){
			}else{
				$.scojs_message('操作失败', $.scojs_message.TYPE_OK);
			}
    	},'json');
	});	
	//刷新送货员工资明细
 	var carSummaryDetailSalaryTbody = $('#carSummaryDetailSalaryTbody').dataTable({
 		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
         "fnRowCallback": function(nRow, aData) {
 			$(nRow).attr('id', aData.ID);
 			return nRow;
 		 },
         "aoColumns": [
             { "mDataProp": "ITEM", "sWidth":"50px"},
             { "mDataProp": "USERNAME", "sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.USERNAME!='' && obj.aData.USERNAME != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='username' value='"+obj.aData.USERNAME+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='username'>";
                     }
                 }
             },
             { "mDataProp": "SALARY_SHEET", "sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.SALARY_SHEET !='' && obj.aData.SALARY_SHEET != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='salary_sheet' value='"+obj.aData.SALARY_SHEET+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='salary_sheet'>";
                     }
                 }
             },
             { "mDataProp": "WORK_TYPE","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.WORK_TYPE!='' && obj.aData.WORK_TYPE != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='work_type' value='"+obj.aData.WORK_TYPE+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='work_type'>";
                     }
                 }
             },
             { "mDataProp": "DESERVED_AMOUNT","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.DESERVED_AMOUNT!='' && obj.aData.DESERVED_AMOUNT != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='deserved_amount' value='"+obj.aData.DESERVED_AMOUNT+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='deserved_amount'>";
                     }
                 }
             },
             { "mDataProp": "REMARK","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='remark'>";
                     }
                 }
             },
             { "mDataProp": null, "sWidth":"120px",
                 "sWidth": "60px",  
                 "fnRender": function(obj) {
                     return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
               		"<i class='fa fa-trash-o fa-fw'> </i> "+
               		"删除明细"+
               		"</a>";
                 }
	         }  
         ]
	});
	
	// 选项卡-送货员工资明细
	$("#carmanageSalary").click(function(e){
		saveCarSummaryData();
		var car_summary_id = $("#car_summary_id").val();
		if(car_summary_id != "" && car_summary_id != null){
			carSummaryDetailSalaryTbody.fnSettings().oFeatures.bServerSide = true;
			carSummaryDetailSalaryTbody.fnSettings().sAjaxSource = "/yh/carsummary/findCarSummaryDetailSalary?car_summary_id="+car_summary_id;   
			carSummaryDetailSalaryTbody.fnDraw();
		}
	});
	// 新增送货员工资明细
	$("#addCarSummaryDetailSalary").click(function(e){
		var car_summary_id = $("#car_summary_id").val();
		if(car_summary_id != ""){
			$.post('/yh/carsummary/addCarSummaryDetailSalary/'+car_summary_id,function(data){
				console.log(data);
				carSummaryDetailSalaryTbody.fnSettings().sAjaxSource = "/yh/carsummary/findCarSummaryDetailSalary?car_summary_id="+car_summary_id;   
				carSummaryDetailSalaryTbody.fnDraw();
			});	
		}
	});
	//异步删除送货员工资明细
	$("#carSummaryDetailSalaryTbody").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		var car_summary_id = $("#car_summary_id").val();
		$.post('/yh/carsummary/delCarSummaryDatailSalary/'+id,function(data){
			console.log(data);
			carSummaryDetailSalaryTbody.fnSettings().sAjaxSource = "/yh/carsummary/findCarSummaryDetailSalary?car_summary_id="+car_summary_id;   
			carSummaryDetailSalaryTbody.fnDraw();
        });
	});
	//修改送货员工资明细
	$("#carSummaryDetailSalaryTbody").on('blur', 'input', function(e){
		e.preventDefault();
		var routeFeeId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		console.log("routeFeeId:"+routeFeeId+",name:"+name+",value:"+value);
		$.post('/yh/carsummary/updateCarSummaryDetailSalary', {routeFeeId:routeFeeId, name:name, value:value}, function(data){
			if(data.success){
			}else{
				$.scojs_message('操作失败', $.scojs_message.TYPE_OK);
			}
    	},'json');
	});	
	//刷新费用合计
 	var carSummaryDetailOtherFeeTbody = $('#carSummaryDetailOtherFeeTbody').dataTable({
 		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 12,
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
         "fnRowCallback": function(nRow, aData) {
 			$(nRow).attr('id', aData.ID);
 			return nRow;
 		 },
         "aoColumns": [
             { "mDataProp": "ITEM", "sWidth":"120px"},
             { "mDataProp": "AMOUNT_ITEM", "sWidth":"70px"},
             { "mDataProp": "AMOUNT", "sWidth":"300px",
            	 "fnRender": function(obj) {
                     if(obj.aData.ITEM =='1' || obj.aData.ITEM == '2' || obj.aData.ITEM =='4' ||
                    		 obj.aData.ITEM =='5' ||obj.aData.ITEM =='8' ){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='amount' value='"+obj.aData.AMOUNT+"' readonly='true'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='amount' value='"+obj.aData.AMOUNT+"'>";
                     }
                 }
             },
             { "mDataProp": "IS_DELETE", "sWidth":"60px",
            	 "fnRender": function(obj) {
            		 if(obj.aData.IS_DELETE != "" && obj.aData.IS_DELETE != null){
            			 if(obj.aData.ITEM == '1' && obj.aData.IS_DELETE == "是"){
            				 return "<input type='checkbox' name='is_delete' class='checkedOrUnchecked' value='"+obj.aData.ITEM+"' checked='true'>";
            			 }
            			 if(obj.aData.ITEM == '2' || obj.aData.ITEM =='4' || obj.aData.ITEM =='8' ){
            				 return "<input type='checkbox' name='is_delete' class='checkedOrUnchecked' value='"+obj.aData.ITEM+"' checked='true'>";
                		 }
            		 }else{
            			 return "<input type='checkbox' name='is_delete' class='checkedOrUnchecked' value='"+obj.aData.ITEM+"'>";
            		 }
                 }
             },
             { "mDataProp": "REMARK","sWidth":"300px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                         return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"'>";
                     }else{
                     	 return "<input type='text' class='form-control search-control orderNo_filter' name='remark'>";
                     }
                 }
             } 
         ]
	});
	
	// 选项卡-费用合计
	$("#carmanageCostSummation").click(function(e){
		saveCarSummaryData();
		var car_summary_id = $("#car_summary_id").val();
		if(car_summary_id != "" && car_summary_id != null){
			carSummaryDetailOtherFeeTbody.fnSettings().oFeatures.bServerSide = true;
			carSummaryDetailOtherFeeTbody.fnSettings().sAjaxSource = "/yh/carsummary/findCarSummaryDetailOtherFee?car_summary_id="+car_summary_id;   
			carSummaryDetailOtherFeeTbody.fnDraw();
		}
	});
	//修改费用合计
	$("#carSummaryDetailOtherFeeTbody").on('blur', 'input', function(e){
		e.preventDefault();
		var routeFeeId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(name == "is_delete"){
			if(value == '1' || value == '2' || value =='4' || value =='8'){
				$.scojs_message('所选为不可更改项目，操作失败', $.scojs_message.TYPE_OK);
				if($(this).prop("checked") == true){
					$(this).prop("checked",false);
				}else{
					$(this).prop("checked",true);
				}
				return false;
			}
			if($(this).prop("checked") == true){
				value = "是";
			}else{
				value = "否";
			}
		}
		console.log("routeFeeId:"+routeFeeId+",name:"+name+",value:"+value);
		$.post('/yh/carsummary/updateCarSummaryDetailOtherFee', {routeFeeId:routeFeeId, name:name, value:value}, function(data){
			if(data.success){
			}else{
				$.scojs_message('操作失败', $.scojs_message.TYPE_OK);
			}
    	},'json');
	});
	
	//刷新运输单
	var transferOrderTbody = $('#transferOrderTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [
            { "mDataProp": "DEPART_NO"},
            { "mDataProp": "ORDER_NO"},
            { "mDataProp": "CUSTOMER"},
            { "mDataProp": "ITEM_NO"},
            { "mDataProp": "ITEM_NAME"},
            { "mDataProp": "AMOUNT"},
            { "mDataProp": "VOLUME"},
            { "mDataProp": "WEIGHT"},
            { "mDataProp": "REMARK"},
        ]
    });
	
});


