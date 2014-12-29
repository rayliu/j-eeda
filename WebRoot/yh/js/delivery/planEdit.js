
$(document).ready(function() {
	$('#menu_deliver').addClass('active').find('ul').addClass('in');
	
	var clickTabId = "chargeCheckOrderbasic";
	
	if($("#deliveryPlanOrderId").val() == "" || $("#deliveryPlanOrderId").val() == null){
		$("#ConfirmationBtn").attr("disabled", true);
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
            {"mDataProp":"COMPANY_NAME"},
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
            //{ "mDataProp": "ORDER_NO","sWidth": "120px"},      
            { "mDataProp": "ITEM_NO"},
            { "mDataProp": "ITEM_NAME"},
            { "mDataProp": "PRODUCT_NUMBER"},
            { "mDataProp": null,
            	"fnRender": function(obj) {
            		return obj.aData.VOLUME * obj.aData.PRODUCT_NUMBER;
            		
            }},
            { "mDataProp": null,
            	"fnRender": function(obj) {
        			return obj.aData.WEIGHT * obj.aData.PRODUCT_NUMBER;
        		
        	}},
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
 		
 		//提交前，校验数据
        if(!$("#deliveryPlanOrderForm").valid()){
	       	return false;
        }
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
 				$("#ConfirmationBtn").attr("disabled", false);
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
            	$.scojs_message('请先保存调车单', $.scojs_message.TYPE_OK);
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
    
	selectCarNo = function(data){
		
		if($(data).find("option:selected").val() == '其他车辆'){
			$(data).parent().empty().append("<input type='text' size='11' name='car_no' class='form-control' style='width:130px'>");
		}else{
			$(data).parent().nextAll("td").find("input[name='driver']").val($(data).find("option:selected").attr("driver"));
			$(data).parent().nextAll("td").find("input[name='phone']").val($(data).find("option:selected").attr("phone"));
			
			var deliveryPlanOrderCarInfoId = $(data).parent().parent().attr("id");
			var carinfoId =  $(data).find("option:selected").attr("id");
			
			console.log("driver:"+$(data).find("option:selected").attr("driver")+",phone:"+$(data).find("option:selected").attr("phone"));
			console.log("deliveryPlanOrderCarInfoId:"+deliveryPlanOrderCarInfoId+",carinfoId:"+carinfoId);
			$.post('/deliveryPlanOrder/updateDeliveryPlanOrderCarinfo', {carinfoId:carinfoId,deliveryPlanOrderCarInfoId:deliveryPlanOrderCarInfoId}, function(result){
				if(!result.success){
					$.scojs_message('操作失败', $.scojs_message.TYPE_OK);
					
				}
	    	},'json');
		}
	};
	
	//车辆列表
	var carinfoListTbody = function(){
	  	var order_id = $("#deliveryPlanOrderId").val();
	  	if(order_id != null && order_id != ""){
			$.post('/deliveryPlanOrder/findDeliveryPlanOrderCarinfoAll',{order_id:order_id},function(data){
				var carinfoTobdy = $("#carinfoTobdy");
				carinfoTobdy.empty();
				for(var i = 0; i < data.length; i++)
				{
					var exitsCar = false;
					$("#carinfoList").children().each(function(){
						if(data[i].CAR_NO == $(this).text()){
							exitsCar = true;
						}
					});
					
					if(exitsCar){
						var str="";
	                	$("#carinfoList").children().each(function(){
	                		if(data[i].CAR_NO == $(this).text()){
	                			str+="<option driver='"+$(this).attr("driver")+"' phone='"+$(this).attr("phone")+"' id='"+$(this).prop("id")+"' selected='selected'> "+$(this).val()+"</option>";                    			
	                		}else{
	                			str+="<option driver='"+$(this).attr("driver")+"' phone='"+$(this).attr("phone")+"' id='"+$(this).prop("id")+"'> "+$(this).val()+"</option>";
	                		}
	                	});
						
						carinfoTobdy.append("<tr id='"+data[i].ID+"'><td><select class='form-control caoNo' name='car_no' style='width:130px' onChange='selectCarNo(this)'><option></option>"+str+"</select></td>" +
								"<td><input type='text' size='11' name='driver' class='form-control' value='"+data[i].DRIVER+"' style='width:150px'></td>" +
								"<td><input type='text' size='11' name='phone' class='form-control' value='"+data[i].PHONE+"' style='width:150px'></td>" +
								"<td><a class='btn removeCar' title='删除'><i class='fa fa-trash-o fa-fw'></i></a></td></tr>");
					}else{
						carinfoTobdy.append("<tr id='"+data[i].ID+"'><td><input type='text' size='11' name='car_no' class='form-control'  value='"+data[i].CAR_NO+"' style='width:130px'></td>" +
								"<td><input type='text' size='11' name='driver' class='form-control' value='"+data[i].DRIVER+"' style='width:150px'></td>" +
								"<td><input type='text' size='11' name='phone' class='form-control' value='"+data[i].PHONE+"' style='width:150px'></td>" +
								"<td><a class='btn removeCar' title='删除'><i class='fa fa-trash-o fa-fw'></i></a></td></tr>");
					}
					
					
				}
			},'json');  
	  	}
	};
	
	carinfoListTbody();
	
	// 添加车辆
	$("#addCar").click(function(){
		var order_id = $("#deliveryPlanOrderId").val();
		if(order_id != null && order_id != ""){
			$.post('/deliveryPlanOrder/addDeliveryPlanOrderCarinfo',{order_id:order_id},function(data){
				//carinfoListTbody();
				var str="";
            	$("#carinfoList").children().each(function(){
            		str+="<option driver='"+$(this).attr("driver")+"' phone='"+$(this).attr("phone")+"' id='"+$(this).prop("id")+"'> "+$(this).val()+"</option>";
            	});
				
            	$("#carinfoTobdy").append("<tr id='"+data.ID+"'><td><select class='form-control caoNo' name='car_no' style='width:130px' onChange='selectCarNo(this)'><option></option>"+str+"</select></td>" +
						"<td><input type='text' size='11' name='driver' class='form-control' style='width:150px'></td>" +
						"<td><input type='text' size='11' name='phone' class='form-control' style='width:150px'></td>" +
						"<td><a class='btn removeCar' title='删除'><i class='fa fa-trash-o fa-fw'></i></a></td></tr>");
				
			},'json'); 
		}else{
			$.scojs_message('请先保存调车单', $.scojs_message.TYPE_OK);
		}
	});
	
	//移除车辆
	$("#carinfoTobdy").on('click','.removeCar',function(){
		var order_id = $(this).parent().parent().prop("id");
		$.post('/deliveryPlanOrder/delDeliveryPlanOrderCarinfo',{order_id:order_id},function(data){
			carinfoListTbody();
			//$(this).parent().parent().remove();
		},'json'); 
	});
	
	//修改车辆信息
	$("#carinfoListTbody").on('blur', 'input', function(e){
		var deliveryPlanOrderCarInfoId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(name == "phone" && isNaN(value)){
			$.scojs_message('【电话】只能输入数字,请重新输入', $.scojs_message.TYPE_OK);
			$(this).val("");
			$(this).focus();
			return false;
		}
		console.log("deliveryPlanOrderCarInfoId:"+deliveryPlanOrderCarInfoId+",name:"+name+",value:"+value);
		if(value != ""){
			$.post('/deliveryPlanOrder/updateDeliveryPlanOrderCarinfo', {deliveryPlanOrderCarInfoId:deliveryPlanOrderCarInfoId, name:name, value:value}, function(data){
				if(!data.success){
					$.scojs_message('操作失败', $.scojs_message.TYPE_OK);
				}
	    	},'json');
		}
	});
	
	
	// 应付datatable
	var paymenttable=$('#table_fin2').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, // 不需要默认的搜索框
        // "sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": false,
        "bLengthChange":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
			{"mDataProp":"FIN_ITEM_NAME",
			    "fnRender": function(obj) {
			        if(obj.aData.FIN_ITEM_NAME!='' && obj.aData.FIN_ITEM_NAME != null){
			        	var str="";
			        	$("#paymentItemList").children().each(function(){
			        		if(obj.aData.FIN_ITEM_NAME == $(this).text()){
			        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
			        		}else{
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			        	if($("#saveDeliveryPlanOrderBtn").prop("disabled")){
			        		return obj.aData.FIN_ITEM_NAME;
			        	}else{
			        		return "<select name='fin_item_id'>"+str+"</select>";
			        	}
			        }else{
			        	var str="";
			        	$("#paymentItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	return "<select name='fin_item_id'>"+str+"</select>";
			        }
			 }},
			{"mDataProp":"AMOUNT",
			     "fnRender": function(obj) {
			    	 if($("#saveDeliveryPlanOrderBtn").prop("disabled")){
				         return obj.aData.AMOUNT;
			    	 }else{
				         if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
				         }else{
				         	 return "<input type='text' name='amount'>";
				         }
			    	 }
			 }},  
			{"mDataProp":"STATUS","sClass": "status"},
			{"mDataProp":"REMARK",
                "fnRender": function(obj) {
                	if($("#saveDeliveryPlanOrderBtn").prop("disabled")){
                		return obj.aData.REMARK;
                	}else{
	                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
	                        return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
	                    }else{
	                    	return "<input type='text' name='remark'>";
	                    }
                	}
            }},  
			{  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                	if($("#saveDeliveryPlanOrderBtn").prop("disabled")){
                		return "";
                	}else{
	                    return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
	              		"<i class='fa fa-trash-o fa-fw'> </i> "+"删除"+"</a>";
                	}
                }
            }     
        ]      
    });
	
	//tab 
    $("#payment").click(function(e){
    	if(clickTabId == "chargeCheckOrderbasic"){
    		//提交前，校验数据
            if(!$("#deliveryPlanOrderForm").valid()){
            	$.scojs_message('请先保存调车单', $.scojs_message.TYPE_OK);
    	       	return false;
            }else{
            	saveDeliveryPlanOrderData();
            }
		}
		clickTabId = e.target.getAttribute("id");
		var deliveryPlanOrderId = $("#deliveryPlanOrderId").val();
    	if(deliveryPlanOrderId != "" && deliveryPlanOrderId != null){
    		paymenttable.fnSettings().oFeatures.bServerSide = true; 
    		paymenttable.fnSettings().sAjaxSource = "/deliveryPlanOrder/accountPayable/"+deliveryPlanOrderId;
			paymenttable.fnDraw();
    	}
	});
	
	// 应付
	$("#addrow").click(function(){	
		var deliveryPlanOrderId = $("#deliveryPlanOrderId").val();
		$.post('/deliveryPlanOrder/addNewRow/'+deliveryPlanOrderId,function(data){
			console.log(data);
			if(data[0] != null){
				paymenttable.fnSettings().sAjaxSource = "/deliveryPlanOrder/accountPayable/"+deliveryPlanOrderId;
				paymenttable.fnDraw();
			}else{
				alert("请到基础模块维护应付条目！");
			}
		});		
	});	
	
	// 应付修改
	$("#table_fin2").on('blur', 'input,select', function(e){
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(name == "amount" && isNaN(value)){
			$.scojs_message('【金额】只能输入数字,请重新输入', $.scojs_message.TYPE_OK);
			$(this).val("");
			$(this).focus();
			return false;
		}
		if(value != "" && value != null){
			$.post('/deliveryPlanOrder/updateDeliveryOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
				if(data.success){
				}else{
					alert("修改失败!");
				}
	    	},'json');
		}
	});
	
	//异步删除应付
	 $("#table_fin2").on('click', '.finItemdel', function(e){
		 var id = $(this).attr('code');
		  e.preventDefault();
		  $.post('/deliveryPlanOrder/finItemdel/'+id,function(data){
              //保存成功后，刷新列表
			  var deliveryPlanOrderId = $("#deliveryPlanOrderId").val();
			  paymenttable.fnSettings().sAjaxSource = "/deliveryPlanOrder/accountPayable/"+deliveryPlanOrderId;
			  paymenttable.fnDraw();
          },'json');
	 });
	
	// 发车确认
	$("#ConfirmationBtn").click(function(){
		var deliveryPlanOrderId = $("#deliveryPlanOrderId").val();
		var deliveryOrderIds = $("#deliveryOrderIds").val();
		$.post('/deliveryPlanOrder/updateDeliveryPlanOrderConfirmation',{deliveryPlanOrderId:deliveryPlanOrderId,deliveryOrderIds:deliveryOrderIds},function(data){
			paymenttable.fnSettings().sAjaxSource = "/deliveryPlanOrder/accountPayable/"+deliveryPlanOrderId;
			paymenttable.fnDraw();
		},'json');
		$("#ConfirmationBtn").attr("disabled", true);
		$("#saveDeliveryPlanOrderBtn").attr("disabled", true);
	});
	 
	 
	 
});


