$(document).ready(function() {
	if($("#car_summary_no").val()){
		document.title = $("#car_summary_no").val() +' | '+document.title;
	}
	$('#menu_carmanage').addClass('active').find('ul').addClass('in');
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
    '</div>';
	$('body').append(alerMsg);
	//记录基本信息选项卡id，用作保存判断
	var clickTabId = "carmanagebasic";
	var num = 1;
	
	//from表单验证
	var validate = $('#carSummaryForm').validate({
        rules: {
        	main_driver_name: {
        		required: true
            },
          	main_driver_amount: {
          		number:true
          	},
          	minor_driver_amount: {
          		number:true
          	},
          	start_car_mileage:{
          		number:true
          	},
          	finish_car_mileage:{
          		number:true
          	}
        },
        messages : {
        	required:  "请输入主司机姓名！", 
        	number :"请输入数字！"
        }
    });
	//根据出车里程和收车里程自动统计行驶里程
	$("#finish_car_mileage,#start_car_mileage").on('blur', function(e){
		var isAudit = $("#isAudit").val();
		if(isAudit == "no"){
			var finish = $("#finish_car_mileage").val();
			var start =  $("#start_car_mileage").val();
			if(finish != "" && finish != 0){
				if(finish > start){
					$("#month_car_run_mileage").val(finish-start);
				}else{
					$.scojs_message('请输入正确的收车里程读数', $.scojs_message.TYPE_ERROR);
					$(this).focus();
				}
			}
		}
	});
	
	//判断行车单是否审核
	var isAudit = $("#isAudit").val();
	if(isAudit == "no"){
		//加载时隐藏"撤销审核"
		$("#delAuditBtn").hide();
		//启用“保存”
		$("#saveCarSummaryBtn").prop("disabled",false);
		$("#addCarSummaryRouteFee").prop("disabled",false);
		$("#addCarSummaryDetailOilFee").prop("disabled",false);
		$("#addCarSummaryDetailSalary").prop("disabled",false);
		$("#editProportionBtn").prop("disabled",false);
		$("#affirmBtn").prop("disabled",true);
	}else{
		//加载时隐藏"审核"
		//$("#auditBtn").hide();
		//不启用“保存”
		$("#saveCarSummaryBtn").prop("disabled",true);
		$("#addCarSummaryRouteFee").prop("disabled",true);
		$("#addCarSummaryDetailOilFee").prop("disabled",true);
		$("#addCarSummaryDetailSalary").prop("disabled",true);
		$("#editProportionBtn").prop("disabled",true);
		$("#affirmBtn").prop("disabled",true);
		$("#auditBtn").prop("disabled",true);
	}
	
 	//列出所有的副司机 - 自营
	$.get('/carsummary/searchAllDriver', null, function(data){
		var minor_driver_name = $('#minor_driver_name');
		var hidden_minor_driver_name = $('#hidden_minor_driver_name').val();
		minor_driver_name.append("<option value=''></option>");
		console.log(hidden_minor_driver_name);
		for(var i = 0; i < data.length; i++)
		{
			if(hidden_minor_driver_name == data[i].DRIVER)
				minor_driver_name.append("<option value='"+data[i].DRIVER+"'  selected='selected'>"+data[i].DRIVER+"</option>");
			else
				minor_driver_name.append("<option value='"+data[i].DRIVER+"'>"+data[i].DRIVER+"</option>");
		}
	});
	
	//table数据显示地址URL控制 TODO
 	var findTableDataService = function(tableName){
 		var car_summary_id = $("#car_summary_id").val();
 		var pickupIds = $("#pickupIds").val();
 		var orderTypes = $("#orderTypes").val();
 		tableName.fnSettings().oFeatures.bServerSide = true;
 		if(clickTabId == "carmanageLine")//线路
 			tableName.fnSettings().sAjaxSource = "/carsummary/findAllAddress?pickupIds="+pickupIds+"&orderTypes="+orderTypes;   
		else if(clickTabId == "transferOrderList")//运输单
			tableName.fnSettings().sAjaxSource = "/carsummary/findTransferOrder?pickupIds="+pickupIds+"&orderTypes="+orderTypes;   
		else if(clickTabId == "carmanageItemList")//货品信息
			tableName.fnSettings().sAjaxSource = "/carsummary/findPickupOrderItems?pickupIds="+pickupIds+"&orderTypes="+orderTypes; 
		else if(clickTabId == "carmanageMilestoneList")//里程碑
 			tableName.fnSettings().sAjaxSource = "/carsummary/transferOrderMilestoneList?car_summary_id="+car_summary_id;
		else if(clickTabId == "carmanageRoadBridge")//路桥费明细
			tableName.fnSettings().sAjaxSource = "/carsummary/findCarSummaryRouteFee?car_summary_id="+car_summary_id;
		else if(clickTabId == "carmanageRefuel")//加油记录
			tableName.fnSettings().sAjaxSource = "/carsummary/findCarSummaryDetailOilFee?car_summary_id="+car_summary_id;
		else if(clickTabId == "carmanageSalary")//送货员工资明细
			tableName.fnSettings().sAjaxSource = "/carsummary/findCarSummaryDetailSalary?car_summary_id="+car_summary_id;
		else if(clickTabId == "carmanageCostSummation")//费用合计
			tableName.fnSettings().sAjaxSource = "/carsummary/findCarSummaryDetailOtherFee?car_summary_id="+car_summary_id;
 		tableName.fnDraw();
 	};
	
 	//保存、修改数据
 	var saveCarSummaryData = function(tableName){
 		//提交前，校验数据
        if(!$("#carSummaryForm").valid()){
	       	return false;
        }
 		//判断行车单是否审核
 		if(!$("#saveCarSummaryBtn").prop("disabled")){
 			$.post('/carsummary/saveCarSummary', $("#carSummaryForm").serialize(), function(data){
 	 			if(data != null){
 	 				$("#car_summary_id").val(data.ID);
 	 				$("#car_summary_no").val(data.ORDER_NO);
 	 				contactUrl("edit?carSummaryId",data.ID);
 	 				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
 	 				findTableDataService(tableName);
 	 			}else{
 	 				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
 	 			}
 	 			$("#saveCarSummaryBtn").prop("disabled",false);
 	 		},'json');
 		}
 	};
 	
 	//点击保存
	$("#saveCarSummaryBtn").click(function(e){
		if(clickTabId != "carmanagebasic"){
			var car_summary_id = $("#car_summary_id").val();
			if(car_summary_id != ""){
				$.post('/carsummary/calculateCost',{carSummaryId:car_summary_id},function(data){
					if(data !="" && data != null){
						$.each(data, function(name, value) {
							$("#"+name+"").val(value);
						});
					}else
						$.scojs_message('费用自动统计失败', $.scojs_message.TYPE_ERROR);
				},'json');	
			}else{
				$.scojs_message('费用自动统计失败', $.scojs_message.TYPE_ERROR);
			}
		}
		clickTabId = e.target.getAttribute("id");
		$("#saveCarSummaryBtn").prop("disabled",true);
		$.post('/carsummary/saveCarSummary', $("#carSummaryForm").serialize(), function(data){
 			if(data != null){
 				$("#car_summary_id").val(data.ID);
 				$("#car_summary_no").val(data.ORDER_NO);
 				contactUrl("edit?carSummaryId",data.ID);
 				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
 			}else{
 				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
 			}
 			$("#saveCarSummaryBtn").prop("disabled",false);
	 	},'json');
	});
	
	//点击返回
	$("#goBackBtn").click(function(e){
		var isAudit = $("#isAudit").val();
		var car_summary_id = $("#car_summary_id").val();
		if(isAudit == "no"){
			if(car_summary_id != ""){
				if(clickTabId != "carmanagebasic"){
					//当用户不是从基本信息tab返回时，自动统计行车单相关数据
					$.post('/carsummary/calculateCost',{carSummaryId:car_summary_id},function(data){
						if(data =="" && data == null){
							$.scojs_message('费用自动统计失败', $.scojs_message.TYPE_ERROR);
						}
					},'json');	
				}
			}else{
				var msg = "是否保存行车单数据？";   
				if (confirm(msg)==true){   
					saveCarSummaryData(); 
				}
			}
		}
		window.location.href="/carsummary"; 
	});
	// 选项卡-基本信息 TODO
	$("#carmanagebasic").click(function(e){
		if(clickTabId != "carmanagebasic"){
			var car_summary_id = $("#car_summary_id").val();
			if(car_summary_id != ""){
				$.post('/carsummary/calculateCost',{carSummaryId:car_summary_id},function(data){
					if(data !="" && data != null){
						$.each(data, function(name, value) {
							$("#"+name+"").val(value);
						});
					}else
						$.scojs_message('费用自动统计失败', $.scojs_message.TYPE_ERROR);
				},'json');	
			}else{
				$.scojs_message('费用自动统计失败', $.scojs_message.TYPE_ERROR);
			}
		}
		clickTabId = e.target.getAttribute("id");
	});
	
	//刷新线路
	var pickupAddressTbody = $('#pickupAddressTbody').dataTable({           
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [
            { "mDataProp": "DEPART_NO"},
            { "mDataProp": "ORDER_NO"},      
            { "mDataProp": "ABBR"},
            { "mDataProp": "TRANSFERADDRESS"},
            { "mDataProp": "CREATE_STAMP"},
            { "mDataProp": null,
            	"fnRender": function(obj) {    
            		if(obj.aData.PICKUPADDRESS != "" && obj.aData.PICKUPADDRESS != null )
            			return obj.aData.PICKUPADDRESS;
            		else
            			return "";
            }},
            { "mDataProp": null,
            	"fnRender": function(obj) {    
              		if(obj.aData.WAREHOUSENAME != "" && obj.aData.WAREHOUSENAME != null)
              			return obj.aData.WAREHOUSENAME;
              		else
            			return "";
            }}
        ]        
    });
	
	// 选显卡-线路 TODO
	$("#carmanageLine").click(function(e){
		if(!$("#saveCarSummaryBtn").prop("disabled")){
		if(clickTabId == "carmanagebasic"){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(pickupAddressTbody);
		}else{
			clickTabId = e.target.getAttribute("id");
			findTableDataService(pickupAddressTbody);
		}
		}
		if($("#saveCarSummaryBtn").prop("disabled")){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(pickupAddressTbody);
			findTableDataService(pickupAddressTbody);
		}
	});
	
	//刷新货品信息
	var pickupItemTbody = $('#pickupItemTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
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
            { "mDataProp": null,
				"fnRender": function(obj) {   
					return obj.aData.ATMAMOUNT + obj.aData.CARGOAMOUNT;
				}
            },
            { "mDataProp": null,
			"fnRender": function(obj) {   
					return obj.aData.ATMVOLUME + obj.aData.CARGOVOLUME;
				}
            },
            { "mDataProp": null,
			"fnRender": function(obj) {   
					return obj.aData.ATMWEIGHT + obj.aData.CARGOWEIGHT;
				}
            },
            { "mDataProp": "REMARK"},
        ]
    });
	
	// 选显卡-货品信息 TODO
	$("#carmanageItemList").click(function(e){
		if(!$("#saveCarSummaryBtn").prop("disabled")){
		if(clickTabId == "carmanagebasic"){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(pickupItemTbody);
		}
		else{
			clickTabId = e.target.getAttribute("id");
			findTableDataService(pickupItemTbody);
		}
		}
		if($("#saveCarSummaryBtn").prop("disabled")){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(pickupItemTbody);
			findTableDataService(pickupItemTbody);
		}
	});
	//刷新里程碑
	var pickupMilestoneTbody = $('#pickupMilestoneTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
         "aoColumns": [
             { "mDataProp": "STATUS",
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
             { "mDataProp": "USER_NAME",
            	"fnRender": function(obj) {
            		if(obj.aData.C_NAME != "" && obj.aData.C_NAME != null)
            			return obj.aData.C_NAME;
            		else
            			return obj.aData.USER_NAME;
            	}
             },
             { "mDataProp": "CREATE_STAMP"}
         ]
	});
	
	
	
	//刷新路桥费明细
 	var carSummaryDetailRouteFeeTbody = $('#carSummaryDetailRouteFeeTbody').dataTable({
 		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
         "fnRowCallback": function(nRow, aData) {
 			$(nRow).attr('id', aData.ID);
 			return nRow;
 		 },
         "aoColumns": [
             { "mDataProp": "ITEM","sWidth":"40px"},
             { "mDataProp": "CHARGE_DATA", "sWidth":"220px",
            	 "fnRender": function(obj) {
                     if(obj.aData.CHARGE_DATA!='' && obj.aData.CHARGE_DATA != null){
                    	 var str = obj.aData.CHARGE_DATA;
 						 str = str.substr(0,10);
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<div id='datetimepicker' class='input-append date'><input type='text' class='form-control search-control orderNo_filter' name='charge_data' id='charge_data' value='"+str+"' disabled='true'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar' onClick='datetimepicker(this)'></i></span></div>";
                    	 else
                    		 return "<div id='datetimepicker' class='input-append date'><input type='text' class='form-control search-control orderNo_filter' name='charge_data' id='charge_data' value='"+str+"'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar' onClick='datetimepicker(this)'></i></span></div>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<div id='datetimepicker' class='input-append date'><input type='text' class='form-control search-control orderNo_filter' name='charge_data' id='charge_data' disabled='true'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar' onClick='datetimepicker(this)'></i></span></div>";
                    	 else
                    		 return "<div id='datetimepicker' class='input-append date'><input type='text' class='form-control search-control orderNo_filter' name='charge_data' id='charge_data'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar' onClick='datetimepicker(this)'></i></span></div>";
                     }
                 }
             },
             //{ "mDataProp": "CHARGE_DATA"},
             { "mDataProp": "CHARGE_SITE","sWidth":"130px",
            	 "fnRender": function(obj) {
                     if(obj.aData.CHARGE_SITE!='' && obj.aData.CHARGE_SITE != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='charge_site' value='"+obj.aData.CHARGE_SITE+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='charge_site' value='"+obj.aData.CHARGE_SITE+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='charge_site' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='charge_site'>";
                     }
                 }
             },
             { "mDataProp": "TRAVEL_AMOUNT","sWidth":"130px",
            	 "fnRender": function(obj) {
                     if(obj.aData.TRAVEL_AMOUNT!='' && obj.aData.TRAVEL_AMOUNT != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='travel_amount' value='"+obj.aData.TRAVEL_AMOUNT+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='travel_amount' value='"+obj.aData.TRAVEL_AMOUNT+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='travel_amount' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='travel_amount'>";
                     }
                 }
             },
             { "mDataProp": "REMARK","sWidth":"130px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark'>";
                     }
                 }
             },
             { "mDataProp": null,
                "sWidth": "60px",  
                 "fnRender": function(obj) {
                	 if($("#saveCarSummaryBtn").prop("disabled")){
	                     return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"' disabled='true'>"+
	               		"<i class='fa fa-trash-o fa-fw'> </i> "+
	               		"删除明细"+
	               		"</a>";
                	 }else{
                		 return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
 	               		"<i class='fa fa-trash-o fa-fw'> </i> "+
 	               		"删除明细"+
 	               		"</a>";
                	 }
                 }
             }   
         ]
	});
	
	// 选项卡-路桥费明细 TODO
	$("#carmanageRoadBridge").click(function(e){
		if(!$("#saveCarSummaryBtn").prop("disabled")){
		if(clickTabId == "carmanagebasic"){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(carSummaryDetailRouteFeeTbody);
		}else{
			clickTabId = e.target.getAttribute("id");
			findTableDataService(carSummaryDetailRouteFeeTbody);
		}
		}
		if($("#saveCarSummaryBtn").prop("disabled")){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(carSummaryDetailRouteFeeTbody);
			findTableDataService(carSummaryDetailRouteFeeTbody);
		}
	});
	
	// 新增路桥费明细
	$("#addCarSummaryRouteFee").click(function(e){
		var car_summary_id = $("#car_summary_id").val();
		if(car_summary_id != ""){
			$.post('/carsummary/addCarSummaryRouteFee/'+car_summary_id,function(data){
				console.log(data);
				if(data.success){
					findTableDataService(carSummaryDetailRouteFeeTbody);
				}else{
					$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
				}
			});	
		}else{
			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
		}
		
	});
	
	//异步删除路桥费明细
	$("#carSummaryDetailRouteFeeTbody").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		$.post('/carsummary/delCarSummaryRouteFee/'+id,function(data){
			console.log(data);
			findTableDataService(carSummaryDetailRouteFeeTbody);
        });
	});
	//修改路桥费明细
	$("#carSummaryDetailRouteFeeTbody").on('blur', 'input', function(e){
		e.preventDefault();
		var result = true;
		var car_summary_id = $("#car_summary_id").val();
		var routeFeeId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(name ==  "charge_data"){
			routeFeeId = $(this).parent().parent().parent().attr("id");
		}
		if(name == "travel_amount" && isNaN(value)){
			$.scojs_message('【通行费】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val("");
			$(this).focus();
			result = false;
		}
		console.log("routeFeeId:"+routeFeeId+",name:"+name+",value:"+value);
		if(result && value != ""){
			$.post('/carsummary/updateCarSummaryDetailRouteFee', {car_summary_id:car_summary_id,routeFeeId:routeFeeId, name:name, value:value}, function(data){
				if(!data.success){
					$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
				}
	    	},'json');
		}
	});	
	//刷新加油记录
 	var carSummaryDetailOilFeeTbody = $('#carSummaryDetailOilFeeTbody').dataTable({
 		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
         "fnRowCallback": function(nRow, aData) {
 			$(nRow).attr('id', aData.ID);
 			return nRow;
 		 },
         "aoColumns": [
             { "mDataProp": "ITEM", "sWidth":"50px"},
             { "mDataProp": "REFUEL_DATA", "sWidth":"220px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REFUEL_DATA!='' && obj.aData.REFUEL_DATA != null){
                    	 var str = obj.aData.REFUEL_DATA;
 						 str = str.substr(0,10);
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<div class='input-append date'><input type='text' class='form-control search-control orderNo_filter' name='refuel_data' id='refuel_data' value='"+str+"' disabled='true'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar' onClick='datetimepicker(this)'></i></span></div>";
                    	 else
                    		 return "<div class='input-append date'><input type='text' class='form-control search-control orderNo_filter' name='refuel_data' id='refuel_data' value='"+str+"'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar' onClick='datetimepicker(this)'></i></span></div>";
                    	 
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<div class='input-append date'><input type='text' class='form-control search-control orderNo_filter' name='refuel_data' id='refuel_data' disabled='true'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar' onClick='datetimepicker(this)'></i></span></div>";
                    	 else
                    		 return "<div class='input-append date'><input type='text' class='form-control search-control orderNo_filter' name='refuel_data' id='refuel_data'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar' onClick='datetimepicker(this)'></i></span></div>";
                     }
                 }
             },
             { "mDataProp": "ODOMETER_MILEAGE", "sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.ODOMETER_MILEAGE!='' && obj.aData.ODOMETER_MILEAGE != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='odometer_mileage' value='"+obj.aData.ODOMETER_MILEAGE+"'  disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='odometer_mileage' value='"+obj.aData.ODOMETER_MILEAGE+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='odometer_mileage' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='odometer_mileage'>";
                     }
                 }
             },
             { "mDataProp": "REFUEL_SITE", "sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REFUEL_SITE !='' && obj.aData.REFUEL_SITE != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_site' value='"+obj.aData.REFUEL_SITE+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_site' value='"+obj.aData.REFUEL_SITE+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_site' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_site'>";
                     }
                 }
             },
             { "mDataProp": "REFUEL_TYPE","sWidth":"100px",
            	 "fnRender": function(obj) {
                     /*if(obj.aData.REFUEL_TYPE!='' && obj.aData.REFUEL_TYPE != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_type' value='"+obj.aData.REFUEL_TYPE+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_type' value='"+obj.aData.REFUEL_TYPE+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_type' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_type'>";
                     }*/
                     var str="";
                     $("#refuelTypeList").children().each(function(){
                 		if(obj.aData.REFUEL_TYPE == $(this).text()){
                 			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
                 		}else{
                 			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
                 		}
                 	 });
                     if($("#saveCarSummaryBtn").prop("disabled"))
                    	 return "<select name='refuel_type' disabled='true' class='form-control search-control'>"+str+"</select>";
                     else
                    	 return "<select name='refuel_type' class='form-control search-control'>"+str+"</select>";
                 }
             },
             { "mDataProp": "REFUEL_UNIT_COST","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REFUEL_UNIT_COST!='' && obj.aData.REFUEL_UNIT_COST != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control refuel_unit_cost' name='refuel_unit_cost' value='"+obj.aData.REFUEL_UNIT_COST+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control refuel_unit_cost' name='refuel_unit_cost' value='"+obj.aData.REFUEL_UNIT_COST+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control refuel_unit_cost' name='refuel_unit_cost' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control refuel_unit_cost' name='refuel_unit_cost'>";
                     }
                 }
             },
             { "mDataProp": "REFUEL_NUMBER","sWidth":"100px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REFUEL_NUMBER !='' && obj.aData.REFUEL_NUMBER != null){
                    	 /*if($("#saveCarSummaryBtn").prop("disabled"))*/
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_number' value='"+obj.aData.REFUEL_NUMBER+"'  disabled='true'>";
                		 /*else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_number' value='"+obj.aData.REFUEL_NUMBER+"'>";*/
                     }else{
                    	 /*if($("#saveCarSummaryBtn").prop("disabled"))*/
                    		 return "<input type='text' class='form-control search-control refuel_number' name='refuel_number' disabled='true'>";
                		 /*else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='refuel_number'>";*/
                     }
            		 //return obj.aData.REFUEL_NUMBER;
                 }
             },
             { "mDataProp": "REFUEL_AMOUNT","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REFUEL_AMOUNT!='' && obj.aData.REFUEL_AMOUNT != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control refuel_amount' name='refuel_amount' value='"+obj.aData.REFUEL_AMOUNT+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control refuel_amount' name='refuel_amount' value='"+obj.aData.REFUEL_AMOUNT+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control refuel_amount' name='refuel_amount' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control refuel_amount' name='refuel_amount'>";
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
                     if($("#saveCarSummaryBtn").prop("disabled"))
                    	 return "<select name='payment_type' disabled='true' class='form-control search-control'>"+str+"</select>";
                     else
                    	 return "<select name='payment_type' class='form-control search-control'>"+str+"</select>";
                 }
             },
             { "mDataProp": "LOAD_AMOUNT","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.LOAD_AMOUNT!='' && obj.aData.LOAD_AMOUNT != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='load_amount' value='"+obj.aData.LOAD_AMOUNT+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='load_amount' value='"+obj.aData.LOAD_AMOUNT+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='load_amount' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='load_amount'>";
                     }
                 }
             },
             { "mDataProp": "AVG_ECON","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.AVG_ECON!='' && obj.aData.AVG_ECON != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='avg_econ' value='"+obj.aData.AVG_ECON+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='avg_econ' value='"+obj.aData.AVG_ECON+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='avg_econ' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='avg_econ'>";
                     }
                 }
             },
             { "mDataProp": "REMARK","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark'>";
                     }
                 }
             },
             { "mDataProp": null, "sWidth":"120px",
                 "sWidth": "60px",  
                 "fnRender": function(obj) {
                	 if($("#saveCarSummaryBtn").prop("disabled")){
                		 return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"' disabled='true'>"+
                    		"<i class='fa fa-trash-o fa-fw'> </i> "+
                    		"删除明细"+
                    		"</a>";
                	 }else{
                		 return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
                    		"<i class='fa fa-trash-o fa-fw'> </i> "+
                    		"删除明细"+
                    		"</a>";
                	 }
                 }
	         }  
         ]
	});
	
	// 选项卡-加油记录 TODO
	$("#carmanageRefuel").click(function(e){
		if(!$("#saveCarSummaryBtn").prop("disabled")){
		if(clickTabId == "carmanagebasic"){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(carSummaryDetailOilFeeTbody);
		}else{
			clickTabId = e.target.getAttribute("id");
			findTableDataService(carSummaryDetailOilFeeTbody);
		}
		}
		if($("#saveCarSummaryBtn").prop("disabled")){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(carSummaryDetailOilFeeTbody);
			findTableDataService(carSummaryDetailOilFeeTbody);
		}
		
	});
	// 新增加油记录
	$("#addCarSummaryDetailOilFee").click(function(e){
		var car_summary_id = $("#car_summary_id").val();
		if(car_summary_id != ""){
			$.post('/carsummary/addCarSummaryDetailOilFee/'+car_summary_id,function(data){
				console.log(data);
				if(data.success){
					findTableDataService(carSummaryDetailOilFeeTbody);
				}else{
					$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
				}
			});	
		}else{
			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
		}
	});
	
	//异步删除加油记录
	$("#carSummaryDetailOilFeeTbody").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		$.post('/carsummary/delCarSummaryDetailOilFee/'+id,function(data){
			findTableDataService(carSummaryDetailOilFeeTbody);
        });
	});
	//修改加油记录
	$("#carSummaryDetailOilFeeTbody").on('blur', 'input,select', function(e){
		e.preventDefault();
		var result = true;
		var car_summary_id = $("#car_summary_id").val();
		var routeFeeId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		//加油量
		var refuel_number = "";
		//平均油耗
		var avg_econ = "";
		
		if(name == "refuel_data"){
			routeFeeId = $(this).parent().parent().parent().attr("id");
		}
		if(name == "odometer_mileage" && isNaN(value)){
			$.scojs_message('【里程表读数】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val("");
			$(this).focus();
			result = false;
		}
		if(name == "refuel_unit_cost" && isNaN(value)){
			$.scojs_message('【油价】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val("");
			$(this).focus();
			result = false;
		}else if(name == "refuel_unit_cost" && value != ""){
			var refuel_amount = $(this).parent().parent().find("td").find(".refuel_amount").val();
			console.log("邮费:"+refuel_amount);
			if(refuel_amount != "" && value != ""){
				refuel_number =  ((refuel_amount * 1) / (value * 1)).toFixed(2) ;
				$(this).parent().parent().find("td").find(".refuel_number").val(refuel_number);
			}
			console.log("加油量:"+refuel_number);
		}
		/*if(name == "refuel_number" && isNaN(value)){
			$.scojs_message('【加油量】只能输入数字,请重新输入', $.scojs_message.TYPE_OK);
			$(this).val("");
			$(this).focus();
			result = false;
		}*/
		if(name == "refuel_amount" && isNaN(value)){
			$.scojs_message('【油费】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val("");
			$(this).focus();
			result = false;
		}else if(name == "refuel_amount" && value != ""){
			var refuel_unit_cost = $(this).parent().parent().find("td").find(".refuel_unit_cost").val();
			console.log("油价:"+refuel_unit_cost);
			if(refuel_unit_cost != "" && value != ""){
				refuel_number =  ((value * 1) / (refuel_unit_cost * 1)).toFixed(2) ;
				$(this).parent().parent().find("td").find(".refuel_number").val(refuel_number);
			}
			console.log("加油量:"+refuel_number);
		}
		if(name == "load_amount" && isNaN(value)){
			$.scojs_message('【载重】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val("");
			$(this).focus();
			result = false;
		}
		/*if(name == "avg_econ" && isNaN(value)){
			$.scojs_message('【平均油耗】只能输入数字,请重新输入', $.scojs_message.TYPE_OK);
			$(this).val("");
			$(this).focus();
			result = false;
		}*/
		console.log("routeFeeId:"+routeFeeId+",name:"+name+",value:"+value);
		if(result && value != ""){
			$.post('/carsummary/updateCarSummaryDetailOilFee', {car_summary_id:car_summary_id,routeFeeId:routeFeeId, name:name, value:value, refuel_number:refuel_number, avg_econ:avg_econ}, function(data){
				if(!data.success){
					$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
				}
	    	},'json');
		}
	});	
	//刷新送货员工资明细
 	var carSummaryDetailSalaryTbody = $('#carSummaryDetailSalaryTbody').dataTable({
 		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
         "fnRowCallback": function(nRow, aData) {
 			$(nRow).attr('id', aData.ID);
 			return nRow;
 		 },
         "aoColumns": [
             /*{ "mDataProp": "ITEM", "sWidth":"80px"},*/
             { "mDataProp": "USERNAME", "sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.USERNAME!='' && obj.aData.USERNAME != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='username' value='"+obj.aData.USERNAME+"' disabled='true'>";
                		 else
                			 return "<input type='text' class='form-control search-control orderNo_filter' name='username' value='"+obj.aData.USERNAME+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='username' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='username'>";
                     }
                 }
             },
             { "mDataProp": "SALARY_SHEET", "sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.SALARY_SHEET !='' && obj.aData.SALARY_SHEET != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='salary_sheet' value='"+obj.aData.SALARY_SHEET+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='salary_sheet' value='"+obj.aData.SALARY_SHEET+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='salary_sheet' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='salary_sheet'>";
                     }
                 }
             },
             { "mDataProp": "WORK_TYPE","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.WORK_TYPE!='' && obj.aData.WORK_TYPE != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='work_type' value='"+obj.aData.WORK_TYPE+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='work_type' value='"+obj.aData.WORK_TYPE+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='work_type' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='work_type'>";
                     }
                 }
             },
             { "mDataProp": "DESERVED_AMOUNT","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.DESERVED_AMOUNT!='' && obj.aData.DESERVED_AMOUNT != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='deserved_amount' value='"+obj.aData.DESERVED_AMOUNT+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='deserved_amount' value='"+obj.aData.DESERVED_AMOUNT+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='deserved_amount' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='deserved_amount'>";
                     }
                 }
             },
             { "mDataProp": "REMARK","sWidth":"120px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark'>";
                     }
                 }
             },
             { "mDataProp": null, "sWidth":"120px",
                 "sWidth": "60px",  
                 "fnRender": function(obj) {
                	 if($("#saveCarSummaryBtn").prop("disabled")){
                		 return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"' disabled='true'>"+
                    		"<i class='fa fa-trash-o fa-fw'> </i> "+
                    		"删除明细"+
                    		"</a>";
                	 }else{
                		 return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
                    		"<i class='fa fa-trash-o fa-fw'> </i> "+
                    		"删除明细"+
                    		"</a>";
                	 }
                 }
	         }  
         ]
	});
	
	// 选项卡-送货员工资明细 TODO
	$("#carmanageSalary").click(function(e){
		if(!$("#saveCarSummaryBtn").prop("disabled")){
		if(clickTabId == "carmanagebasic"){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(carSummaryDetailSalaryTbody);
		}else{
			clickTabId = e.target.getAttribute("id");
			findTableDataService(carSummaryDetailSalaryTbody);
		}
		}
		if($("#saveCarSummaryBtn").prop("disabled")){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(carSummaryDetailSalaryTbody);
			findTableDataService(carSummaryDetailSalaryTbody);
		}
	});
	// 新增送货员工资明细
	$("#addCarSummaryDetailSalary").click(function(e){
		var car_summary_id = $("#car_summary_id").val();
		if(car_summary_id != ""){
			$.post('/carsummary/addCarSummaryDetailSalary/'+car_summary_id,function(data){
				console.log(data);
				findTableDataService(carSummaryDetailSalaryTbody);
			});	
		}
	});
	//异步删除送货员工资明细
	$("#carSummaryDetailSalaryTbody").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		$.post('/carsummary/delCarSummaryDatailSalary/'+id,function(data){
			console.log(data);
			findTableDataService(carSummaryDetailSalaryTbody);
        });
	});
	//修改送货员工资明细
	$("#carSummaryDetailSalaryTbody").on('blur', 'input', function(e){
		e.preventDefault();
		var result = true;
		var car_summary_id = $("#car_summary_id").val();
		var routeFeeId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(name == "deserved_amount" && isNaN(value)){
			$.scojs_message('【应得金额】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val("");
			$(this).focus();
			result = false;
		}
		console.log("routeFeeId:"+routeFeeId+",name:"+name+",value:"+value);
		if(result && value != ""){
			$.post('/carsummary/updateCarSummaryDetailSalary', {car_summary_id:car_summary_id,routeFeeId:routeFeeId, name:name, value:value}, function(data){
				if(!data.success){
					$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
				}
	    	},'json');
		}
	});	
	
	//刷新费用合计
 	var carSummaryDetailOtherFeeTbody = $('#carSummaryDetailOtherFeeTbody').dataTable({
 		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
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
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='amount' value='"+obj.aData.AMOUNT+"' disabled='true'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='amount' value='"+obj.aData.AMOUNT+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='amount' value='"+obj.aData.AMOUNT+"'>";
                     }
                 }
             },
             { "mDataProp": "IS_DELETE", "sWidth":"60px",
            	 "fnRender": function(obj) {
            		 if(obj.aData.IS_DELETE == "是"){
            			 //if(obj.aData.ITEM == '1' || obj.aData.ITEM == '2' || obj.aData.ITEM =='4' || obj.aData.ITEM =='8' ){
            			 if(obj.aData.ITEM == '2' || obj.aData.ITEM =='4' || obj.aData.ITEM =='8' ){
            				 return "<input type='checkbox' name='is_delete' class='checkedOrUnchecked' value='"+obj.aData.ITEM+"' checked='true' disabled='true'>";
                		 }else{
                			 if($("#saveCarSummaryBtn").prop("disabled"))
            					 return "<input type='checkbox' name='is_delete' class='checkedOrUnchecked' value='"+obj.aData.ITEM+"' checked='true' disabled='true'>";
            				 else
            					 return "<input type='checkbox' name='is_delete' class='checkedOrUnchecked' value='"+obj.aData.ITEM+"' checked='true'>";
                		 }
            		 }else{
            			/* if(obj.aData.ITEM == '1'){
            				 return "<input type='checkbox' name='is_delete' class='checkedOrUnchecked' value='"+obj.aData.ITEM+"' disabled='true'>";
            			 }*/
            			 
        				 if($("#saveCarSummaryBtn").prop("disabled"))
        					 return "<input type='checkbox' name='is_delete' class='checkedOrUnchecked' value='"+obj.aData.ITEM+"' disabled='true'>";
        				 else
        					 return "<input type='checkbox' name='is_delete' class='checkedOrUnchecked' value='"+obj.aData.ITEM+"'>";
            			 
            		 }
                 }
             },
             { "mDataProp": "REMARK","sWidth":"300px",
            	 "fnRender": function(obj) {
                     if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' value='"+obj.aData.REMARK+"'>";
                     }else{
                    	 if($("#saveCarSummaryBtn").prop("disabled"))
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark' disabled='true'>";
                    	 else
                    		 return "<input type='text' class='form-control search-control orderNo_filter' name='remark'>";
                     }
                 }
             } 
         ]
	});
	
	// 选项卡-费用合计 TODO
	$("#carmanageCostSummation").click(function(e){
		if(!$("#saveCarSummaryBtn").prop("disabled")){
		if(clickTabId == "carmanagebasic"){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(carSummaryDetailOtherFeeTbody);
		}else{
			clickTabId = e.target.getAttribute("id");
			findTableDataService(carSummaryDetailOtherFeeTbody);
		}
	}
		if($("#saveCarSummaryBtn").prop("disabled")){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(carSummaryDetailOtherFeeTbody);
			findTableDataService(carSummaryDetailOtherFeeTbody);
		}
	});
	// 选项卡-里程碑 TODO
	$("#carmanageMilestoneList").click(function(e){
		if(!$("#saveCarSummaryBtn").prop("disabled")){
		if(clickTabId == "carmanagebasic"){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(pickupMilestoneTbody);
		}else{
			clickTabId = e.target.getAttribute("id");
			findTableDataService(pickupMilestoneTbody);
		}
		}
		if($("#saveCarSummaryBtn").prop("disabled")){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(pickupMilestoneTbody);
			findTableDataService(pickupMilestoneTbody);
		}
	});
	//修改费用合计
	$("#carSummaryDetailOtherFeeTbody").on('blur', 'input', function(e){
		e.preventDefault();
		var result =true;
		var routeFeeId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(name == "is_delete"){
			if($(this).prop("checked") == true){
				value = "是";
			}else{
				value = "否";
			}
		}
		if(name == "amount" && isNaN(value)){
			$.scojs_message('【金额】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val("");
			$(this).focus();
			result = false;
		}
		console.log("routeFeeId:"+routeFeeId+",name:"+name+",value:"+value);
		if(result && value != ""){
			$.post('/carsummary/updateCarSummaryDetailOtherFee', {routeFeeId:routeFeeId, name:name, value:value}, function(data){
				if(!data.success){
					$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
				}
	    	},'json');
		}
	});
	
	//费用合计-审核
	$("#auditBtn").click(function(){
		var car_summary_id = $("#car_summary_id").val();
		var value = $(this).text();
		if(car_summary_id != "" && car_summary_id != null){
			$.post('/carsummary/updateCarSummaryOrderStatus', {carSummaryId:car_summary_id,value:value}, function(data){
				if(data.success){
					$("#isAudit").val("yes");
					//$("#auditBtn").hide();
					$("#auditBtn").prop("disabled",true);
					$("#saveCarSummaryBtn").prop("disabled",true);
					$("#delAuditBtn").show();
					$("#saveCarSummaryBtn").prop("disabled",true);
					$("#addCarSummaryRouteFee").prop("disabled",true);
					$("#addCarSummaryDetailOilFee").prop("disabled",true);
					$("#addCarSummaryDetailSalary").prop("disabled",true);
					$("#editProportionBtn").prop("disabled",true);
					$("#affirmBtn").prop("disabled",true);
					//刷新当前选项卡 
					$("#"+clickTabId+"").click();
					
				}else{
					$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
				}
	    	},'json');
		}else{
			$.scojs_message('操作失败,请先保存行车单', $.scojs_message.TYPE_ERROR);
		}
	});	
	
	//费用合计-取消审核
	$("#delAuditBtn").click(function(e){
		var car_summary_id = $("#car_summary_id").val();
		var value = $(this).text();
		if(car_summary_id != "" && car_summary_id != null){
			$.post('/carsummary/updateCarSummaryOrderStatus', {carSummaryId:car_summary_id,value:value}, function(data){
				if(data.success){
					$("#isAudit").val("no");
					$("#delAuditBtn").hide();
					$("#auditBtn").show();
					$("#saveCarSummaryBtn").prop("disabled",false);
					$("#addCarSummaryRouteFee").prop("disabled",false);
					$("#addCarSummaryDetailOilFee").prop("disabled",false);
					$("#addCarSummaryDetailSalary").prop("disabled",false);
					$("#editProportionBtn").prop("disabled",false);
					$("#affirmBtn").prop("disabled",true);
					//刷新当前选项卡
					$("#"+clickTabId+"").click();
				}else{
					$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
				}
	    	},'json');
		}
	});	
	
	//刷新运输单
	var transferOrderTbody = $('#transferOrderTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"fnRowCallback": function(nRow, aData) {
 			$(nRow).attr('id', aData.ID);
 			return nRow;
 		},
        "aoColumns": [
			{ "mDataProp": null,"sWidth":"40px",
				"fnRender": function(obj) {
				return num++;
				}
			}, 
            { "mDataProp": "ORDER_NO"},
            { "mDataProp": "ABBR"},
            { "mDataProp": null,
				"fnRender": function(obj) {   
				if(obj.aData.CARGO_NATURE == "ATM"){
						return obj.aData.ATMAMOUNT;
					}else{
						return obj.aData.CARGOAMOUNT;
					}
				}
            },
            { "mDataProp": null,
			"fnRender": function(obj) {   
				if(obj.aData.CARGO_NATURE == "ATM"){
						return obj.aData.ATMVOLUME;
					}else{
						return obj.aData.CARGOVOLUME;
					}
				}
            },
            { "mDataProp": null,
			"fnRender": function(obj) {   
				if(obj.aData.CARGO_NATURE == "ATM"){
						return obj.aData.ATMWEIGHT;
					}else{
						return obj.aData.CARGOWEIGHT;
					}
				}
            },
            { "mDataProp": null,
				"fnRender": function(obj) {
					return "<input type='text' size='3' name='car_summary_order_share_ratio' id='car_summary_order_share_ratio' value='"+obj.aData.CAR_SUMMARY_ORDER_SHARE_RATIO*100+"' disabled='true'>%";
				}
            },
            { "mDataProp": "REMARK"}
        ]
    });
	
	// 选项卡-运输单 TODO
	$("#transferOrderList").click(function(e){
		if(!$("#saveCarSummaryBtn").prop("disabled")){
		if(clickTabId == "carmanagebasic"){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(transferOrderTbody);
		}else{
			clickTabId = e.target.getAttribute("id");
			findTableDataService(transferOrderTbody);
		}
		}
		if($("#saveCarSummaryBtn").prop("disabled")){
			clickTabId = e.target.getAttribute("id");
			saveCarSummaryData(transferOrderTbody);
			findTableDataService(transferOrderTbody);
		}
	});
	// 编辑比例
	$("#editProportionBtn").click(function(e){
		$(this).prop("disabled",true);
		$("#affirmBtn").prop("disabled",false);
		$('#transferOrderTbody input').prop("disabled",false);
	});

	// 比例修改确认
	$("#affirmBtn").click(function(e){
		var orderIds = [];
		var rates = [];
		var checkTates = 0;
		var result = true;
        $("#transferOrderTbody tbody tr").each(function(trindex,tritem){
        	orderIds.push($(tritem).attr("id"));
        	rates.push($(tritem).find("td").find("input").val());
        });
        for ( var i = 0; i < rates.length; i++) {
        	if(isNaN(Number(rates[i]))){
        		$.scojs_message('【分摊比例】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
    			result = false;
    			break;
    		}else{
    			checkTates = rates[i]*1+checkTates*1;
    		}
		}
        if(result && orderIds.length == rates.length){
        	 if(orderIds.length % 3 == 0 ){
             	if(!(checkTates >=99 && checkTates <=100)){
             		$.scojs_message('所有分摊比例相加为99%-100%,请重新输入', $.scojs_message.TYPE_ERROR);
             		result = false;
             	}
             }else{
             	if(checkTates != 100){
             		$.scojs_message('所有分摊比例相加为100%,请重新输入', $.scojs_message.TYPE_ERROR);
     	        	result = false;
             	}
             }
        	if(result){
				$.post('/carsummary/updateTransferOrderShareRatio',{"orderIds":orderIds.toString(),"rates":rates.toString()}, function(data){
					if(data.success){
						$("#affirmBtn").prop("disabled",true);
						$("#editProportionBtn").prop("disabled",false);
						$('#transferOrderTbody input').prop("disabled",true);
					}else{
						$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
					}
		    	},'json');
        	}
		}
	});
	
});
function datetimepicker(data){
	if(!$("#saveCarSummaryBtn").prop("disabled")){
		$('.input-append').datetimepicker({  
			    format: 'yyyy-MM-dd',  
			    language: 'zh-CN',
			    autoclose: true,
			    pickerPosition: "bottom-left"
			}).on('changeDate', function(ev){
				$(".bootstrap-datetimepicker-widget").hide();
				$(data).parent().prev("input").focus();
		});
	}
}

