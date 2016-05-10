$(document).ready(function() {
	if(pickupOrder.order_no){
		document.title = pickupOrder.order_no +' | '+document.title;
	}else if(pickupOrder.depart_no){
		document.title = pickupOrder.depart_no +' | '+document.title;
	}
	
	
	
	//按钮控制
	var showFinishBut = function(){
		if($("#pickupOrderId").val() ==""){						
			$("#saveTransferOrderBtn").attr('disabled', false);					
		}else{
			if($("#orderStatus").html()=='新建'){
				$("#saveTransferOrderBtn").attr('disabled', false);	
				$("#finishBtn").attr('disabled', false);	
			}
		}
	};
	showFinishBut();
	
	
	
	
	$("#address_type").on('change',function(){
		var address_type = $('#address_type').val();
		 if(address_type=="yard"){
			 $("#replenishmentOrderId").val("")
			 $("#warehouseDiv").hide();
			 $("#addressDiv").show();
	     }else if(address_type=="warehouse"){
	    	 $("#replenishmentOrderId").val("")
	    	 $("#addressDiv").hide();
	    	 $("#warehouseDiv").show();
	     }else if(address_type=="twice_pickup"){
	    	//普货不可以二次调拨控制
    		 var cargo_nature = $('#cargo_nature').val();
    		 if(cargo_nature=='cargo'){
    			$("#address_type").val('yard');
				$.scojs_message('普货没有二次调车业务', $.scojs_message.TYPE_FALSE);
				
				$("#warehouseDiv").hide();
				$("#addressDiv").show();
    		 }else{
    			 $("#replenishmentOrderId").val("")
    	    	 $("#addressDiv").hide();
    	    	 $("#warehouseDiv").hide();
    		 }
	     }
	});
	

	//运输去向回显
	var pickup_type = $("#pickup_type").val();
	if(pickup_type ==  'yard'){
		$("#address_type").val('yard');
		$("#addressDiv").show();
	}else if(pickup_type ==  'warehouse'){
		$("#address_type").val('warehouse');
		$("#addressDiv").hide();
		$("#warehouseDiv").show();
	}else if(pickup_type ==  'twice_pickup'){
		$("#address_type").val('twice_pickup');
		$("#addressDiv").hide();
		$("#warehouseDiv").hide();
	}
	
	
	
	 $('#menu_assign').addClass('active').find('ul').addClass('in');
	 var pickupOrderId = $("#pickupOrderId").val();
	 var parentId = "chargeCheckOrderbasic";
    //from表单验证
	var validate = $('#pickupOrderForm').validate({
        rules: {
        	turnout_time: {required: true},
        	//car_no: {required: true},
        	driver_name: {required: true},
        	driver_phone: {required: true}
        },
        messages : {	             
        }
    });
			
	 // 列出所有的司机
	 /*red*/
	 /*$('#driverMessage').on('keyup click', function(){
 		var inputStr = $('#driverMessage').val();
 		//var inputStr = $('#carNoMessage').val();
		//获取到inputiD，判断是否为空
		if(inputId==null){
			inputId="pickupMode1";
		}
		//定义一个TYPE变量，用来作为车辆的条件
		var party_type ;
		if(inputId=="pickupMode1"){
			party_type = "DRIVER";
		}else{
			party_type = "SP_DRIVER";
		}
 		
 		$.get('/transferOrder/searchAllDriver', {input:inputStr, partyType:party_type}, function(data){
 			var driverList = $("#driverList");
 			driverList.empty();
 			for(var i = 0; i < data.length; i++)
 			{
 				driverList.append("<li><a tabindex='-1' class='fromLocationItem' pid='"+data[i].PID+"' phone='"+data[i].PHONE+"' contact_person='"+data[i].CONTACT_PERSON+"' > "+data[i].CONTACT_PERSON+" "+data[i].PHONE+"</a></li>");
 			}
 		},'json');
 		
 		$("#driverList").css({ 
         	left:$(this).position().left+"px", 
         	top:$(this).position().top+32+"px" 
        }); 
        $('#driverList').show();
	 });*/
	  	
 	 // 选中司机
 	 $('#driverList').on('mousedown', '.fromLocationItem', function(e){	
 		 $("#driver_id").val($(this).attr('pid'));
	  	 $('#driverMessage').val($(this).attr('contact_person'));
	  	 $('#driver_phone').val($(this).attr('phone'));  	 
	     $('#driverList').hide();   
     });

 	// 没选中司机，焦点离开，隐藏列表
 	$('#driverMessage').on('blur', function(){
  		$('#driverList').hide();
  	});
 	
 	// 列出所有的车辆
	$('#carNoMessage,#driverMessage').on('keyup click', function(){
		
		var inputStr = '';
		if($('#driverMessage').is(":focus")){
			inputStr = $('#driverMessage').val();
		}
		if($('#carNoMessage').is(":focus")){
			inputStr = $('#carNoMessage').val();
		}
		
		//获取到inputiD，判断是否为空
		if(inputId==null){
			inputId="pickupMode1";
		}
		//定义一个TYPE变量，用来作为车辆的条件
		var typeStr ;
		if(inputId=="pickupMode1"){
			typeStr = "OWN";
		}else{
			typeStr = "SP";
		}
			
		$.get('/transferOrder/searchAllCarInfo', {input:inputStr,type:typeStr}, function(data){
			
			var carNoList = $("#carNoList");
			carNoList.empty();
			for(var i = 0; i < data.length; i++)
			{ 	
				if($('#driverMessage').is(":focus")){
					var driverList = $("#driverList");
		 			driverList.empty();
					carNoList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' carNo='"+data[i].CAR_NO+"' carType='"+data[i].CARTYPE+"' length='"+data[i].LENGTH+"' driver='"+data[i].DRIVER+"' phone='"+data[i].PHONE+"'> "+data[i].DRIVER+"</a></li>");
				};
				if($('#carNoMessage').is(":focus")){
					carNoList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' carNo='"+data[i].CAR_NO+"' carType='"+data[i].CARTYPE+"' length='"+data[i].LENGTH+"' driver='"+data[i].DRIVER+"' phone='"+data[i].PHONE+"'> "+data[i].CAR_NO+"</a></li>");
				};
			}
	},'json');

	$("#carNoList,#driverList").css({ 
       	left:$(this).position().left+"px", 
       	top:$(this).position().top+32+"px" 
      }); 
      $('#carNoList').show();
	});
	 	
	// 选中车辆
	$('#carNoList,#driverList').on('mousedown', '.fromLocationItem', function(e){		
		 $("#driver_id").val('');
	     $("#carinfoId").val($(this).attr('id'));
	 	 $('#carNoMessage').val($(this).attr('carNo'));
	  	 $('#driverMessage').val($(this).attr('driver'));
	  	 $('#driver_phone').val($(this).attr('phone'));  
	 	 $('#cartype').val($(this).attr('carType'));
	 	 $('#carsize').val($(this).attr('length'));	  	 
	     $('#carNoList').hide();   
    });

	// 没选中车辆，焦点离开，隐藏列表
	$('#carNoMessage,#driverMessage').on('blur', function(){
 		$('#carNoList').hide();
 	});
	
	if($("#driverMessage").val() == ''){
		$("#driverMessage").val($("#carInfoDriverMessage").val());
	}
	
	if($("#driver_phone").val() == ''){
		$("#driver_phone").val($("#carInfoDriverPhone").val());
	}

	 //var message=$("#message").val();
     var type=$("#type").val();
     var depart_id=$("#depart_id").val();
     //var tr_item=$("#tr_itemid_list").val();
     //var item_detail=$("#item_detail").val();
 	 //显示货品table
 	 var datatable = $('#pickupItem-table').dataTable({
         "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
         //"sPaginationType": "bootstrap",
         "iDisplayLength": 10,
         "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
         "bServerSide": false,
         "bDestroy": true,
     	 "oLanguage": {
             "sUrl": "/eeda/dataTables.ch.txt"
         },
         //"sAjaxSource": "/pickupOrder/getInitPickupOrderItems?localArr="+message+"&tr_item="+tr_item+"&item_detail="+item_detail+"&pickupId="+$("#pickupOrderId").val(),
         "aoColumns": [
             { "mDataProp": "CUSTOMER" ,"sWidth": "100%"},
             { "mDataProp": "ORDER_NO" ,"sWidth": "30%"},  
             { "mDataProp": "PLANNING_TIME"}, 
             { "mDataProp": "ITEM_NO"},
             { "mDataProp": "ITEM_NAME"},
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
            			 return (obj.aData.ATMAMOUNT * obj.aData.VOLUME).toFixed(2);
            		 }else{
            			 return (obj.aData.CARGOVOLUME).toFixed(2);
            		 }
                 }
             },
             { "mDataProp": null,
            	 "fnRender": function(obj) {   
            		 if(obj.aData.CARGO_NATURE == "ATM"){
            			 return (obj.aData.ATMAMOUNT * obj.aData.WEIGHT).toFixed(2);
            		 }else{
            			 return (obj.aData.CARGOWEIGHT).toFixed(2);
            		 }
                 }
             },
             { "mDataProp": "REMARK"},
             { 
                 "mDataProp": null, 
                 "sWidth": "8%",                
                 "fnRender": function(obj) {                    
                     return "<a class='btn btn-success dateilEdit' code='?id="+obj.aData.ID+"'>"+
                                 "<i class='fa fa-search fa-fw'></i>"+
                                 "查看"+
                             "</a>";
                 },
             }                                       
         ],
         "fnInitComplete": function(oSettings, json) {
         	$("#eeda-table td").on('click', '', function(){
         	 hang = $(this).parent("tr").prevAll().length; 
        		  	hang = Number(hang)+1;
         	});         	    
         }       
     });
 	
 	var tr_itemid_list=[];
 	// 查看货品
	$("#pickupItem-table").on('click', '.dateilEdit', function(e){
		e.preventDefault();
		
		$("#transferOrderItemDateil").show();
		var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		tr_itemid_list.push(itemId);
		$("#item_id").val(itemId);
		$("#item_save").attr("disabled", false);
		$("#style").hide();
		detailTable.fnSettings().sAjaxSource = "/pickupOrder/findAllItemDetail?item_id="+itemId+"&pickupId="+$("#pickupOrderId").val();
		detailTable.fnDraw();  			
	});
	
	// 删除货品
	$("#pickupItem-table").on('click', '.cancelbutton', function(e){
		e.preventDefault();		
		 var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		 $("table tr:eq("+hang+")").remove(); 
	});
	
	var the_id="";
	var item_id = $("#item_id").val();
	var detailTable= $('#pickupDetail-table').dataTable({           
         "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",  
         "iDisplayLength": 10,
         "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
         "bServerSide": true, 
     	 "oLanguage": {
             "sUrl": "/eeda/dataTables.ch.txt"
         },
         "sAjaxSource": "/departOrder/itemDetailList?item_id="+item_id+"",
       
         "aoColumns": [
              { "mDataProp": null,
                "fnRender": function(obj) {
             	   the_id=obj.aData.ID;
                    return '<input checked="" type="checkbox" name="detailCheckBox" value="'+obj.aData.ID+'">';
                }
              },
             { "mDataProp": "ITEM_NAME"},      
             { "mDataProp": "ITEM_NO"},
             { "mDataProp": "SERIAL_NO"},
             { "mDataProp": "VOLUME"},
             { "mDataProp": "WEIGHT"},
             { "mDataProp": "REMARK"},           
         ]        
     });
	
    //选择单品保存
    var item_detail_id=[];
    $("#item_save").click(function(){    	
    	 $("table tr:not(:first)").each(function(){    	        
         	$("input:checked",this).each(function(){
         		item_detail_id.push($(this).val());         	
         	});          		
         }); 
    	$("#item_detail").val(item_detail_id);
    	$("#tr_itemid_list").val(tr_itemid_list);
    	//$("#style").show();
    	$("#item_save").attr("disabled", true);
    });

    // 回显车长
    var carSizeOption=$("#carsize>option");
    var carSizeVal=$("#carSizeSelect").val();
    for(var i=0;i<carSizeOption.length;i++){
       var svalue=carSizeOption[i].text;
       if(carSizeVal==svalue){
    	   $("#carsize option[value='"+svalue+"']").attr("selected","selected");
       }
    }
    
    // 回显车型
    var carTypeOption=$("#cartype>option");
    var carTypeVal=$("#carTypeSelect").val();
    for(var i=0;i<carTypeOption.length;i++){
    	var svalue=carTypeOption[i].text;
    	if(carTypeVal==svalue){
    		$("#cartype option[value='"+svalue+"']").attr("selected","selected");
    	}
    }
   
    // 保存
    var handlePickkupOrderDetail = function(){
    	if(!$("#saveTransferOrderBtn").prop('disabled')){
    		$("#saveTransferOrderBtn").prop('disabled',true);
	    	$.post('/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
				$("#pickupOrderId").val(data.ID);
				$("#pickup_type").val(data.PICKUP_TYPE);
				$("#addressPickupOrderId").val(data.ID);
				$("#milestonePickupId").val(data.ID);
				if(data.ID>0){
					$("#pickupId").val(data.ID);
					$("#showOrderNo").text(data.DEPART_NO);
					$("#saveTransferOrderBtn").prop('disabled',false);
			        showFinishBut();	
			        contactUrl("edit?id",data.ID);
			        $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			        
			        //
			        var message=$("#message").val();
		    		var tr_item=$("#tr_itemid_list").val();
		    		var item_detail=$("#item_detail").val();
		     	    var pickupId = $("#pickupOrderId").val();
		     	    var pickup_type = $("#pickup_type").val();
		     		datatable.fnSettings().oFeatures.bServerSide = true; 
		     		datatable.fnSettings().sAjaxSource = "/pickupOrder/getInitPickupOrderItems?localArr="+message+"&tr_item="+tr_item+"&pickup_type="+pickup_type+"&item_detail="+item_detail+"&pickupId="+pickupId;
		     		datatable.fnDraw();
		     		routeTable.fnSettings().sAjaxSource = "/pickupOrder/findAllRoute?orderIds="+$("#message").val();
            		routeTable.fnDraw(); 
				}else{
					alert('数据保存失败。');
				}
			},'json');
    	}
    };
    var savePickupOrderFunction = function(){
    	if(!$("#saveTransferOrderBtn").prop('disabled')){
	    	var detailIds = [];
	    	var uncheckedDetailIds = [];
		    $("input[name='detailCheckBox']").each(function(){
		    	if($(this).prop('checked') == true){
		    		detailIds.push($(this).val());
		    	}else{
		    		uncheckedDetailIds.push($(this).val());
		    	}
		    });
	    	$("#checkedDetail").val(detailIds);
	    	$("#uncheckedDetail").val(uncheckedDetailIds);
	    	//跟车人员
	    	var AssistantIds = [];
	        var names = [];
	        var phones = [];
	        $("#driverAssistantList tr").each(function (){
	        	AssistantIds.push($(this).attr("assistantid"));
	        	names.push($(this).find("td").find("input[name='name']").val());
	        	phones.push($(this).find("td").find("input[name='phone']").val());
			});
	        $("#driverAssistantIds").val(AssistantIds.toString());
			$("#driverAssistantNames").val(names.toString());
			$("#driverAssistantPhones").val(phones.toString());
	    	if(uncheckedDetailIds.length > 0){
	    		handlePickkupOrderDetail();
	    		// 对一张单进行多次提货,把选中的和没选中的单品区分开来,然后在进行判断s
	    		$("#detailDialog").modal('show');
	    	}else{
	    		handlePickkupOrderDetail();
	    	}
	 		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	        $("#finishBtn").attr('disabled', false);
	        //$("#cancelBtn").attr('disabled', false);
    	}
    };
    
    $("#continueCreateBtn").click(function(){
    	$("#detailCheckBoxForm").submit();
    });

    //点击保存的事件，保存拼车单信息 TODO
    var clickSavePickupOrder = function(e){
    	//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();		
		//提交前，校验数据
        if(!$("#pickupOrderForm").valid()){
	       	return false;
        }
		//异步向后台提交数据
        savePickupOrderFunction();
    };
    /*--------------------------------------------------------------------*/
    var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);
	
	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	/*--------------------------------------------------------------------*/
	//transferOrderForm 不需要提交	
 	$("#saveTransferOrderBtn").click(function(e){
 		e.preventDefault();
 		var mode=$("input[name='pickupMode']:checked").val();
 		if(mode=="routeSP"||mode=="pickupSP"){
 			if($("#sp_id").val()==""){
 				$("#acc").show();
 				return false;
 			}
 		}
 		clickSavePickupOrder(e);
	});
   
 	var pickupOrderMilestone = function(){
 		var pickupOrderId = $("#pickupOrderId").val();
		$.post('/transferOrderMilestone/transferOrderMilestoneList',{pickupOrderId:pickupOrderId},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.empty();
			for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
			{
				var location = data.transferOrderMilestones[i].LOCATION;
				if(location == null){
					location = "";
				}
				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+location+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
			}
		},'json');
 	};
 	
	// 运输里程碑
	$("#transferOrderMilestoneList").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();			
		//提交前，校验数据
        if(!$("#pickupOrderForm").valid()){
	       	return false;
        }
		//异步向后台提交数据
        if("chargeCheckOrderbasic" == parentId){
        	clickSavePickupOrder(e);
        }
        pickupOrderMilestone();
        parentId = e.target.getAttribute("id");
	});

	// 保存新里程碑
	$("#transferOrderMilestoneFormBtn").click(function(){
		$.post('/transferOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			var name = data.username;
			if(name==null){
				name="";
			}
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+name+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$('#transferOrderMilestone').modal('hide');
		pickupOrderMilestone();
	});
	
	var findAllAddress = function(){
		var pickupOrderId = $("#pickupOrderId").val();
		$.post('/pickupOrder/findAllAddress', {pickupOrderId:pickupOrderId}, function(data){
			var pickupAddressTbody = $("#pickupAddressTbody");
			pickupAddressTbody.empty();
			var num = 0;
			for(var i=0;i<data.length;i++){
				num+=data[i].RATE;
				if(data[i].AMOUNT == null || data[i].AMOUNT == "")
					data[i].AMOUNT = 0;
				if(data.length == 1){
					pickupAddressTbody.append("<tr value='"+data[i].PICKUP_SEQ+"' id='"+data[i].ID+"'><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td>"+data[i].CREATE_STAMP+"</td><td><input type='radio' name='lastStopRadio"+data[i].ID+"' checked='' value='yard"+data[i].ID+"'></td><td><input type='radio' name='lastStopRadio"+data[i].ID+"' value='warehouse"+data[i].ID+"'></td><td>"+data[i].STATUS+"</td><td>"+data[i].AMOUNT+"</td><td>"+data[i].RATE*100+"%</td><td><a href='javascript:void(0)' class='moveUp'>上移</a> <a href='javascript:void(0)' class='moveDown'>下移</a> <a href='javascript:void(0)' class='moveTop'>移至顶部</a> <a href='javascript:void(0)' class='moveButtom'>移至底部</a></td></tr>");
				}else{
					pickupAddressTbody.append("<tr value='"+data[i].PICKUP_SEQ+"' id='"+data[i].ID+"'><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td>"+data[i].CREATE_STAMP+"</td><td><input type='radio' name='lastStopRadio"+data[i].ID+"' checked='' value='yard"+data[i].ID+"'></td><td><input type='radio' name='lastStopRadio"+data[i].ID+"' value='warehouse"+data[i].ID+"'></td><td>"+data[i].STATUS+"</td><td>"+data[i].AMOUNT+"</td><td><input type='text' size='1' name='rate' id='rate' value='"+data[i].RATE*100+"'>%</td><td><a href='javascript:void(0)' class='moveUp'>上移</a> <a href='javascript:void(0)' class='moveDown'>下移</a> <a href='javascript:void(0)' class='moveTop'>移至顶部</a> <a href='javascript:void(0)' class='moveButtom'>移至底部</a></td></tr>");
				}
				/*
				if(i == 0){  
					pickupAddressTbody.append("<tr><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td><a href='javascript:void(0)'>上移</a> <a href='javascript:void(0)'>下移</a> <a href='javascript:void(0)'>移至顶部</a> <a href='javascript:void(0)'>移至底部</a></td></tr>");					
				}else if(i == data.length-1){
					pickupAddressTbody.append("<tr><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td><a href='javascript:void(0)'>上移</a> <a href='javascript:void(0)'>下移</a> <a href='javascript:void(0)'>移至顶部</a> <a href='javascript:void(0)'>移至底部</a></td></tr>");										
				}else{
					pickupAddressTbody.append("<tr><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td><a href='javascript:void(0)'>上移</a> <a href='javascript:void(0)'>下移</a> <a href='javascript:void(0)'>移至顶部</a> <a href='javascript:void(0)'>移至底部</a></td></tr>");					
				}*/
			}
			if(num > 1){
				alert("分摊比例已大于100%，请注意调整！");
			}
		},'json');
	};
	//分摊比例修改
	$("#pickupAddressTbody").on('blur', 'input', function(){
		var id = $(this).parent().parent().attr("id");
		var value = $(this).val();
		if(value > 100){
			alert("单张运输单分摊比例不能大于100%，请重新输入！");
			$(this).focus();
			return false;
		}
		$.post('/pickupOrder/updateTransferOrderFinItem', {id:id, value:value}, function(data){
			if(data.success){
				findAllAddress();
			}else{
				alert("修改失败!");
			}
    	},'json');
	});
	
	var choiceExternalTransferOrder = function(){
    	var pickupOrderId = $("#pickupOrderId").val();
        externalTable.fnSettings().sAjaxSource = "/pickupOrder/externTransferOrderList?pickupOrderId="+pickupOrderId;
        externalTable.fnDraw();
	};
	 
	// 列出所有的提货地点
	$("#addressList").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#pickupOrderForm").valid()){
	       	return false;
        }
		//异步向后台提交数据
        if($("#pickupOrderId").val()==""){
        	clickSavePickupOrder(e);
        }
		
		//handlePickkupOrderDetail();
        parentId = e.target.getAttribute("id");
	});
	
	var swapPosition = function(currentId,targetId,currentVal,targetVal){
		$.post('/pickupOrder/swapPickupSeq', {currentId:currentId,targetId:targetId,currentVal:currentVal,targetVal:targetVal}, function(data){
			//保存成功后，刷新列表
            if(data.success){
            	routeTable.fnDraw();
            }else{
                alert('移动失败');
            }
		},'json');
	};
	
	// 上移
	$("#route-table").on('click', '.moveUp', function(e){
		var currentNode = $(this).parent().parent();
		var currentVal = currentNode.attr("value");
		var targetNode = currentNode.prev();
		var targetVal = targetNode.attr("value");
		currentNode.attr("value", targetVal);
		targetNode.attr("value", currentVal);
		var currentId = currentNode.attr("id");
		var targetId = targetNode.attr("id");
		var currentNewVal = currentNode.attr("value");
		var targetNewVal = targetNode.attr("value");
		swapPosition(currentId,targetId,currentNewVal,targetNewVal);
	});
	
	// 下移
	$("#route-table").on('click', '.moveDown', function(e){
		var currentNode = $(this).parent().parent();
		var currentVal = currentNode.attr("value");
		var targetNode = currentNode.next();
		var targetVal = targetNode.attr("value");
		currentNode.attr("value", targetVal);
		targetNode.attr("value", currentVal);
		var currentId = currentNode.attr("id");
		var targetId = targetNode.attr("id");
		var currentNewVal = currentNode.attr("value");
		var targetNewVal = targetNode.attr("value");
		swapPosition(currentId,targetId,currentNewVal,targetNewVal);
	});
	
	// 移至顶部
	$("#route-table").on('click', '.moveTop', function(e){
		var currentNode = $(this).parent().parent();
		var currentVal = currentNode.attr("value");
		var targetNode = currentNode.siblings().first();
		var targetVal = targetNode.attr("value");
		currentNode.attr("value", targetVal);
		targetNode.attr("value", currentVal);
		var currentId = currentNode.attr("id");
		var targetId = targetNode.attr("id");
		var currentNewVal = currentNode.attr("value");
		var targetNewVal = targetNode.attr("value");
		swapPosition(currentId,targetId,currentNewVal,targetNewVal);
	});
	
	// 移至底部
	$("#route-table").on('click', '.moveButtom', function(e){
		var currentNode = $(this).parent().parent();
		var currentVal = currentNode.attr("value");
		var targetNode = currentNode.siblings().last();
		var targetVal = targetNode.attr("value");
		currentNode.attr("value", targetVal);
		targetNode.attr("value", currentVal);
		var currentId = currentNode.attr("id");
		var targetId = targetNode.attr("id");
		var currentNewVal = currentNode.attr("value");
		var targetNewVal = targetNode.attr("value");
		swapPosition(currentId,targetId,currentNewVal,targetNewVal);
	});

	// 回显提货方式
	$("input[name='pickupMode']").each(function(){
		if($("#pickupModeRadio").val() == $(this).val()){
			if($(this).val() != 'own'){
				$("#spDiv").show();
				$("#pickupOrderPayment").show();
			}else{	
				$("#pickupOrderPayment").hide();
			}
			$(this).attr('checked', true);
		}
		/*if($("#pickupModeRadio").val() == ''){
			if($(this).prop('checked') == true){
				if($(this).val() == 'own'){
					$("#carInfoDiv").show();
				}else{
					$("#spDiv").show();
				}
			}
		}*/
	});	
	
    // 当pickupModes为货品直送时则显示收货人的信息
	/*定义了一个全局的变量inputId 
	 * 用力啊存放每次选择不同Radio是的选中的radio Id
	 * */
	var inputId;
    $("#pickupModes").on('click', 'input', function(){
    	//没换一次，清空数据
    	$("#carNoMessage").val("");
    	$("#driverMessage").val("");
    	$("#driver_phone").val("");
    	
  	  inputId  = $(this).attr('id');
	  if(inputId=='pickupMode1'){
		  $("#spDiv").hide();
		  $("#paymentDiv").hide();
		  if($("#flag").val()=='derect' || $("#is_direct_deliver").val()=='true'){
			  $("#pickupOrderPayment").show();
		  }else{
			  $("#pickupOrderPayment").hide();
		  }
		  $("#show_follow_name").show();
	  }else{
		  $("#spDiv").show();
		  $("#paymentDiv").show();
		  $("#pickupOrderPayment").show();
		  $("#show_follow_name").hide();
	  } 
  	});
    
    if($("#flag").val()=='derect' || $("#is_direct_deliver").val()=='true'){
		  $("#pickupOrderPayment").show();
    }

	// 货品信息
	$("#pickupOrderItemList").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();				
		//提交前，校验数据
        if(!$("#pickupOrderForm").valid()){
	       	return false;
        }

        if($("#pickupOrderId").val()!=""){
        	var message=$("#message").val();
    		var tr_item=$("#tr_itemid_list").val();
    		var item_detail=$("#item_detail").val();
     	    var pickupId = $("#pickupOrderId").val();
     	    var pickup_type = $("#pickup_type").val();
     		datatable.fnSettings().oFeatures.bServerSide = true; 
     		datatable.fnSettings().sAjaxSource = "/pickupOrder/getInitPickupOrderItems?localArr="+message+"&tr_item="+tr_item+"&pickup_type="+pickup_type+"&item_detail="+item_detail+"&pickupId="+pickupId;
     		datatable.fnDraw();
     		parentId = e.target.getAttribute("id");
        }else{
        	clickSavePickupOrder(e);
        }
	});

	//获取供应商的list，选中信息在下方展示其他信息
	$('#spMessage').on('keyup click', function(){
		var inputStr = $('#spMessage').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/serviceProvider/searchSp', {input:inputStr, sp_type:'line;pickup'}, function(data){
			var spList =$("#spList");
			spList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var abbr = data[i].ABBR;
				if(abbr == null){
					abbr = '';
				}
				var company_name = data[i].COMPANY_NAME;
				if(company_name == null){
					company_name = '';
				}
				var contact_person = data[i].CONTACT_PERSON;
				if(contact_person == null){
					contact_person = '';
				}
				var phone = data[i].PHONE;
				if(phone == null){
					phone = '';
				}
				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+" "+abbr+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');

		$("#spList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#spList').show();
	});
	
	// 选中供应商
	$('#spList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		
		$('#spMessage').val(message.substring(0, message.indexOf(" ")));
		$('#sp_id').val($(this).attr('partyId'));
		var pageSpName = $("#pageSpName");
		pageSpName.empty();
		var pageSpAddress = $("#pageSpAddress");
		pageSpAddress.empty();
		pageSpAddress.append($(this).attr('address'));
		var contact_person = $(this).attr('contact_person');
		if(contact_person == 'null'){
			contact_person = '';
		}
		pageSpName.append(contact_person+'&nbsp;');
		var phone = $(this).attr('phone');
		if(phone == 'null'){
			phone = '';
		}
		pageSpName.append(phone); 
		pageSpAddress.empty();
		var address = $(this).attr('address');
		if(address == 'null'){
			address = '';
		}
		pageSpAddress.append(address);
        $('#spList').hide();
    });

	// 没选中供应商，焦点离开，隐藏列表
	$('#spMessage').on('blur', function(){
 		$('#spList').hide();
 	});
	
	// 点击已完成按钮
	$("#finishBtn").click(function(){
		$("#finishBtn").attr('disabled', true);	
		// 处理入库运输单
		$.post('/pickupOrder/getTransferOrderDestination', {warehouseIds: $("#warehouseIds").val()}, function(data){
			//保存成功后，刷新列表
            if(data.success){
            	var pickupOrderId = $("#pickupOrderId").val();
            	var priceType = $("input[name='priceType']:checked").val();
            	if(pickupOrderId != null && pickupOrderId != ""){
            		$.post('/pickupOrder/finishPickupOrder', {pickupOrderId:pickupOrderId,priceType:priceType, receiverId:$("#receiverId").val()}, function(data){
                		pickupOrderMilestone();	
                    	var pickupOrderId = $("#pickupOrderId").val();
                    	paymenttable.fnSettings().sAjaxSource = "/pickupOrder/accountPayable?pickupOrderId="+pickupOrderId;
                    	paymenttable.fnDraw(); 
                    	routeTable.fnSettings().sAjaxSource = "/pickupOrder/findAllRoute?orderIds="+$("#message").val();
                		routeTable.fnDraw(); 
                    	$("#saveTransferOrderBtn").attr('disabled', true);	
                    	if(data.PICKUP_TYPE=='direct'){
                    		$("#orderStatus").html(data.STATUS);
                    		$.scojs_message('收货成功', $.scojs_message.TYPE_OK);
                    	}else if(data.PICKUP_TYPE=='twice_pickup'){
                    		$("#orderStatus").html(data.STATUS);
                    		$.scojs_message('调拨成功', $.scojs_message.TYPE_OK);
                    	}else if(data.PICKUP_TYPE=='warehouse'){
                    		$("#orderStatus").html(data.STATUS);
                    		$.scojs_message('入库成功', $.scojs_message.TYPE_OK);
                    	}else{
                    		("#orderStatus").html(data.STATUS);
                    		$.scojs_message('已入货场', $.scojs_message.TYPE_OK);
                    	}
                	},'json');
            	}
            }else{
            	$("#finishBtn").attr('disabled', false);	
                alert('操作失败');
            }
		},'json');
	});

	
    
//	// 判断货场是否选中
//	$("#checkbox1").click(function(){
//		if($(this).prop('checked') == true){
//			$("#addressDiv").show();
//		}else{
//			$("#addressDiv").hide();			
//		}
//	});
//	
//	
//    // 判断直送收货人是否选中
//    $("#checkbox3").click(function(){
//        if($(this).prop('checked') == true){
//            $("#receiverDiv").show();
//        }else{
//            $("#receiverDiv").hide();            
//        }
//    });

	
	// 获取所有仓库
	$.post('/transferOrder/searchAllWarehouse',function(data){
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
	
	// 判断仓库是否选中
	$("#checkbox2").click(function(){
		if($(this).prop('checked') == true){
			$("#warehouseDiv").show();
		}else{
			$("#warehouseDiv").hide();			
		}
	});
	
	// 回显地址
//	if($("#address").val() != null && $("#address").val() != ''){
//		$("#addressDiv").show();
//		$("#checkbox1").prop('checked', true);
//	}else{
//	    $("#checkbox1").prop('checked', false);	
//	}
//	
//	// 回显仓库
//	if($("#hideWarehouseId").val() != null && $("#hideWarehouseId").val() != ''){
//		$("#warehouseDiv").show();
//		$("#checkbox2").prop('checked', true);
//	}else{
//		$("#warehouseDiv").hide();
//		$("#checkbox2").prop('checked', false);		
//	}
	
//	//只送人地址回显
//	if($("#flag").val()=='derect'){
//    	$("#receiverId").val($("#t_id").val());
//		$("#div1").hide();
//		$("#checkbox3").prop('checked',true);
//        $("#receiverDiv").show();
//	}else if($("#is_direct_deliver").val()=='true'){
//		$("#receiverId").val($("#t_id").val());
//		$("#div1").hide();
//		$("#checkbox3").prop('checked',true);
//        $("#receiverDiv").show();
//	}else{
//		$("#div2").hide();
//		$("#addressDiv").show();
//		$("#checkbox1").prop('checked', true);
//	}
	//datatable, 动态处理
    var externalTable = $('#external-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "bLengthChange":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/pickupOrder/externTransferOrderList",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                    return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
                 }
            },
            { "mDataProp": "ORDER_NO"},
            {"mDataProp":"ORDER_TYPE",
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
            		}else{
            			return "";
            		}}},
            {"mDataProp":"CARGO_NATURE",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return "ATM";
            		}else{
            			return "";
            		}}},
            { "mDataProp": "ADDRESS"},
            {"mDataProp":"PICKUP_MODE",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == "routeSP"){
            			return "干线供应商自提";
            		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
            			return "外包供应商提货";
            		}else if(obj.aData.PICKUP_MODE == "own"){
            			return pickupOrder.ex_type;
            			
            		}else{
            			return "";
            		}}},
            { "mDataProp": "STATUS"},
            { "mDataProp": "CNAME"},
    		{ "mDataProp": "ROUTE_FROM"},
    		{ "mDataProp": "ROUTE_TO"},                                      
    		{ "mDataProp": "CREATE_STAMP"}                                    
        ]      
    });	    

    var refreshAddress = function(data){
    	//保存成功后，刷新列表
        if(data.success){
        	var pickupOrderId = $("#pickupOrderId").val();
    		$.post('/pickupOrder/findAllAddress', {pickupOrderId:pickupOrderId}, function(data){
    			var pickupAddressTbody = $("#pickupAddressTbody");
    			pickupAddressTbody.empty();
    			for(var i=0;i<data.length;i++){
    				pickupAddressTbody.append("<tr value='"+data[i].PICKUP_SEQ+"' id='"+data[i].ID+"'><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td>"+data[i].CREATE_STAMP+"</td><td><input type='radio' name='lastStopRadio"+data[i].ID+"' checked='' value='yard"+data[i].ID+"'></td><td><input type='radio' name='lastStopRadio"+data[i].ID+"' value='warehouse"+data[i].ID+"'></td><td>"+data[i].STATUS+"</td><td><a href='javascript:void(0)' class='moveUp'>上移</a> <a href='javascript:void(0)' class='moveDown'>下移</a> <a href='javascript:void(0)' class='moveTop'>移至顶部</a> <a href='javascript:void(0)' class='moveButtom'>移至底部</a></td></tr>");					
    			}
    		},'json');
        }else{
            alert('操作失败');
        }
    };
    
    // 添加额外运输单
    $('#addExternalTransferOrderFormBtn').click(function(e){
        e.preventDefault();
    	var trArr=[];
        var tableArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		trArr.push($(this).val());
        		tableArr.push($(this).val());
        	}
        });
        tableArr.push($("#message").val());
        $("#message").val(tableArr);
        $('#transferOrderIds').val(trArr);
        $('#addExternalPickupOrderId').val($("#pickupOrderId").val());
        $.post('/pickupOrder/addExternalTransferOrder', $('#addExternalTransferOrderForm').serialize(),function(data){
        	routeTable.fnSettings().sAjaxSource = "/pickupOrder/findAllRoute?orderIds="+$("#message").val();
        	routeTable.fnDraw(); 
    	},'json');
        $('#addExternalTransferOrder').modal('hide');
    });
    
    // 判断订单类型是否是补货
    if($("#transferOrderType").val() == 'replenishmentOrder'){
    	if($("#hideWarehouseId").val() != null && $("#hideWarehouseId").val() != ''){
    		$("#checkbox2").prop('checked', true);
        	$("#warehouseDiv").show();
        	//仓库不可变
        	$("#gateInSelect").css({"cursor":"not-allowed",
        		  "background-color":"#eee",
        		  "opacity":1
        		});
        	$("#gateInSelect").attr("disabled","disabled");
    	}
    }
    
    
    //应付datatable
	var paymenttable=$('#table_fin2').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "sAjaxSource": "/pickupOrder/accountPayable?pickupOrderId="+pickupOrderId,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
			{"mDataProp":"NAME",
                "fnRender": function(obj) {
                    if(obj.aData.NAME!='' && obj.aData.NAME != null){
                    	var str="";
                    	$("#paymentItemList").children().each(function(){
                    		if(obj.aData.NAME == $(this).text()){
                    			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
                    		}else{
                    			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
                    		}
                    	});
                        return "<select name='fin_item_id'>"+str+"</select>";
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
                     if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
                         return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
                     }else{
                     	 return "<input type='text' name='amount'>";
                     }
             }},  
			{"mDataProp":"REMARK",
                 "fnRender": function(obj) {
                     if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                         return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
                     }else{
                     	 return "<input type='text' name='remark'>";
                     }
             }},  
			{"mDataProp":"STATUS"},
			{  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
              		"<i class='fa fa-trash-o fa-fw'> </i> "+
              		"删除"+
              		"</a>";
                }
            }     
        ]      
    });
	
	//异步删除应付
	$("#table_fin2").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		e.preventDefault();
		$.post('/pickupOrder/finItemdel/'+id,function(data){
             //保存成功后，刷新列表
             paymenttable.fnDraw();
        },'text');
	});
	
	$("#addrow").click(function(){	
		var pickupOrderId =$("#pickupOrderId").val();
		$.post('/pickupOrder/addNewRow/'+pickupOrderId,function(data){
			if(data[0] != null){
				paymenttable.fnSettings().sAjaxSource = "/pickupOrder/accountPayable?pickupOrderId="+pickupOrderId;   
				paymenttable.fnDraw();
			}else{
				alert("请到基础模块维护应付条目！");
			}
		});		
	});	
	
	$("#addIncomeBtn").click(function(){	
		var pickupOrderId =$("#pickupOrderId").val();
		$.post('/pickupOrder/addIncomeRow/'+pickupOrderId,function(data){
			if(data[0] != null){
				incomeTab.fnSettings().sAjaxSource = "/pickupOrder/incomePayable?pickupOrderId="+pickupOrderId;   
				incomeTab.fnDraw();
			}else{
				alert("请到基础模块维护应收条目！");
			}
		});		
	});	
	
    var pickupOrderId = $("#pickupOrderId").val();
    //应收datatable
	var pickupOrderPaymentTab = $('#table_fin').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "sAjaxSource": "/pickupOrder/pickupOrderPaymentList?pickupOrderId="+pickupOrderId,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
			{"mDataProp":"CNAME","sClass": "name"},
			{"mDataProp":"TRANSFERNO","sClass": "amount"},  
			{"mDataProp":"AMOUNT","sClass": "remark",
				"fnRender": function(obj) {
					if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
	                    return obj.aData.AMOUNT.toFixed(2);
	                }else{
	                	return "";
	                }
				}
			}
        ]      
    });
	
	$("#pickupOrderPayment").click(function(e){				
		//提交前，校验数据
        if(!$("#pickupOrderForm").valid()){
	       	return false;
        }
 		clickSavePickupOrder(e);
 		var pickupOrderId = $("#pickupOrderId").val();
 		incomeTab.fnSettings().sAjaxSource = "/pickupOrder/incomePayable?pickupOrderId="+pickupOrderId;
 		incomeTab.fnDraw();
 		pickupOrderPaymentTab.fnDraw();
 		paymenttable.fnDraw();
 		
	});
	$("#chargeCheckOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#wentDutchBtn").click(function(){
		if(confirm("确定分摊费用吗？")){
		    var pickupOrderId = $("#pickupOrderId").val();
			$.post('/pickupOrder/wentDutch', {pickupOrderId:pickupOrderId}, function(data){
				if(data.success){
					pickupOrderPaymentTab.fnSettings().sAjaxSource = "/pickupOrder/pickupOrderPaymentList?pickupOrderId="+pickupOrderId;   
					pickupOrderPaymentTab.fnDraw();
				}else{
					alert("分摊费用失败!");
				}
	    	},'json');
        } else {
            return;
        }
	});
	
	$("#table_fin2").on('blur', 'input,select', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(value != "" && value != null){
			$.post('/pickupOrder/updatePickupOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
				if(data.success){
				}else{
					alert("修改失败!");
				}
	    	},'json');
		}
	});	
		
	$("#table_fin3").on('blur', 'input,select', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var orderId = $(this).parent().parent().attr("order_id");
		var name = $(this).attr("name");
		var value = $(this).val();
		var transferOrderFinItemId = $("#transferOrderFinItemId").val();
		if(transferOrderFinItemId != null && transferOrderFinItemId != ""){
			paymentId = transferOrderFinItemId;
		}
		if(value != "" && value != null){
			$.post('/transferOrder/updateTransferOrderFinItem', {paymentId:paymentId, name:name, value:value, orderId:orderId}, function(data){
				if(data.ID > 0){
					$("#transferOrderFinItemId").val(data.ID);
				}else{
					alert("修改失败!");
				}
			},'json');
		}
	});	
	
	//异步删除应收
	$("#table_fin3").on('click', '.finItemdel', function(e){
		var orderId = $(this).attr('code');
		e.preventDefault();
		if(orderId != "" ){
			$.post('/pickupOrder/delReceivable/'+orderId,function(data){
	             //保存成功后，刷新列表
	             incomeTab.fnDraw();
	        },'text');
		}
	});
	
	
    //应收datatable
	var incomeTab = $('#table_fin3').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "sAjaxSource": "/pickupOrder/incomePayable?pickupOrderId="+pickupOrderId,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID, order_id: aData.ORDER_ID});
			return nRow;
		},
        "aoColumns": [ 
  			{"mDataProp":"ORDER_NO"}, 
			{"mDataProp":"CNAME"},
			{"mDataProp":"AMOUNT",
                "fnRender": function(obj) {
                    if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
                        return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
                    }else{
                    	 return "<input type='text' name='amount'>";
                    }
            }},  
			{"mDataProp":"NAME",
                "fnRender": function(obj) {
                    if(obj.aData.NAME!='' && obj.aData.NAME != null){
                    	var str="";
                    	$("#incomeItemList").children().each(function(){
                    		if(obj.aData.NAME == $(this).text()){
                    			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
                    		}else{
                    			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
                    		}
                    	});
                        return "<select name='fin_item_id'>"+str+"</select>";
                    }else{
                    	var str="";
                    	$("#incomeItemList").children().each(function(){
                    		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
                    	});
                    	return "<select name='fin_item_id'>"+str+"</select>";
                    }
             }},
			{"mDataProp":"REMARK",
                 "fnRender": function(obj) {
                     if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                         return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
                     }else{
                     	 return "<input type='text' name='remark'>";
                     }
             }},  
			{"mDataProp":"STATUS"},
			{  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ORDER_ID+"'>"+
              		"<i class='fa fa-trash-o fa-fw'> </i> "+
              		"删除"+
              		"</a>";
                }
            }     
        ]      
    });

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
    
    //datatable, 动态处理
    var routeTable = $('#route-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 25,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID, value: aData.PICKUP_SEQ}); 
			return nRow;
		},
        "sAjaxSource": "/pickupOrder/findAllRoute?orderIds="+$("#message").val(),
        "aoColumns": [ 
            {"mDataProp":"ORDER_NO", "sWidth":"80px"},
            {"mDataProp":"CNAME", "sWidth":"150px"},
            {"mDataProp":"ADDRESS", "sWidth":"150px"},                 	
            {"mDataProp":"CREATE_STAMP", "sWidth":"120px"},                        
            {"mDataProp":null, "sWidth":"80px",
            	"fnRender": function(obj) {
            		if(obj.aData.IS_DIRECT_DELIVER || $("#flag").val()=='derect'){
            			return "直送收货人";
            		}else{
            			if(obj.aData.PICKUP_TYPE=='warehouse'){
            				return "中转仓";
            			}else if(obj.aData.PICKUP_TYPE=='twice_pickup'){
            				return "二次调拨";
            			}else{
            				return "货场";
            			}
            		}
            }},                           
            {"mDataProp":"STATUS", "sWidth":"100px"},                        
            {"mDataProp":"REMARK", "sWidth":"150px"},                         
            {"mDataProp":null, "sWidth":"200px",
            	"fnRender": function(obj) {
                    return "<a href='javascript:void(0)' class='moveUp'>上移</a> <a href='javascript:void(0)' class='moveDown'>下移</a> <a href='javascript:void(0)' class='moveTop'>移至顶部</a> <a href='javascript:void(0)' class='moveButtom'>移至底部</a>";
            }}                         
        ]      
    });
    
    var warehouseIds = [];
    $("#route-table").on('click', "input[type='radio']", function(e){
    	var id = $(this).parent().parent().parent().attr('id');
		if($(this).val() == "warehouse"){
			warehouseIds.push(id);
			$("#warehouseIds").val(warehouseIds);
		}else{
			if(warehouseIds.length > 0){
				warehouseIds.splice($.inArray(id,warehouseIds), 1);
				$("#warehouseIds").val(warehouseIds);
			}
		}
		/*if($(this).val() == "receiver"){
			$("#receiverId").val(id);
		}*/
	});
    
    
    //查询跟车人员
    var pickupOrderId = $("#pickupOrderId").val();
    $.post('/pickupOrder/findPickupDriverAssistant',{pickupId:pickupOrderId},function(data){
  		 if(data.length > 0){
  			var driverAssistantList = $("#driverAssistantList");
  			//driverAssistantList.remove();
  			for(var i=0; i<data.length; i++){
  				driverAssistantList.append('<tr assistantid="'+data[i].DRIVER_ASSISTANT_ID+'"><td><input class="form-control name_filter" name="name" value="'+data[i].NAME+'"></td>'
  						+'<td><input class="form-control" name="phone" value="'+data[i].PHONE+'"></td>'
  		                +'<td><a class="btn removeOffice" title="删除" assistant="'+data[i].ID+'"><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
  			}
  		 }
	 },'json');
    
    
    //新增跟车人员
    $("#addAssistant").on('keyup click',function(){
		$("#driverAssistantList").append('<tr assistantid=""><td><input class="form-control name_filter" name="name"></td>'
				+'<td><input class="form-control" name="phone"></td>'
                +'<td><a class="btn removeOffice" title="删除" assistant=""><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
	});
    
    //移除跟车人员
	$("#driverAssistantTable").on('click','.removeOffice',function(){
		var id = $(this).attr("assistant");
		var tagA = $(this);
		if(id != "" && id != null){
			$.post('/pickupOrder/deletePickupDriverAssistant',{id:id},function(data){
				tagA.parent().parent().remove();
			 },'json');
		}else{
			tagA.parent().parent().remove();
		}
	});
    
	//点击文本框显示跟车人员列表
	$('#driverAssistantTable').on('keyup click', 'input[name=name]', function(){
		
		var name = $(this);
		var inputStr = $(this).val();
		$(this).next().remove();
		$.get("/pickupOrder/findDriverAssistant", {input:inputStr}, function(data){
			var str = "";
            for(var i = 0; i < data.length; i++)
            	str += "<li><a tabindex='-1' class='fromLocationItem' name='"+data[i].NAME+"' phone='"+data[i].PHONE+"' assistantid='"+data[i].ID+"'>"+data[i].NAME+" "+data[i].PHONE+"</a></li>";
            name.after('<ul class="pull-right dropdown-menu default dropdown-scroll driverAssistantList" tabindex="-1" style="top: 35%; left: 2%;">'+str+'</ul>');
            name.next().css({left:name.position().left+"px", top:name.position().top+32+"px"}).show();
        },'json');
	});
	  	
	// 选中列表跟车人员
	$('#driverAssistantTable').on('mousedown', '.fromLocationItem', function(e){	
		$(this).parent().parent().parent().parent().attr("assistantid",$(this).attr('assistantid'));
		$(this).parent().parent().parent().find("input[name='name']").val($(this).attr('name'));
		$(this).parent().parent().parent().next().find("input[name='phone']").val($(this).attr('phone'));
	    $(this).parent().parent().hide();   
    });
	
    // 没选中客户，焦点离开，隐藏列表
	$('#driverAssistantTable').on('blur', '.name_filter', function(e){	
    	$(this).next().eq(0).css({left:$(this).position().left+"px", top:$(this).position().top+32+"px"}).hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });
    
    
    //撤销调车单
    $("#cancelBtn").on('click',function(){
    	$("#cancelBtn").attr('disabled',true);
    	var pickup_id = $("#pickupId").val();
    	if(pickup_id==null || pickup_id=='')
    	{
    		$.scojs_message('对不起，当前单据为保存', $.scojs_message.TYPE_ERROR);
    		return false;
    	}
    	
    	if(!confirm("是否确认撤销此单据？")){
    		$("#cancelBtn").attr('disabled',false);
    		return false;
    	}
    	
    	$.post('/pickupOrder/cancelPickupOder', {pickupId:pickup_id}, function(data){ 
    		if(!data.success){
    			$.scojs_message('对不起，当前单据(已入货场/已入库)或有下级单据(发车单/回单)，不能撤销', $.scojs_message.TYPE_ERROR);
    		}else{
    			$("#finishBtn").attr('disabled', true);
    			$("#saveTransferOrderBtn").attr('disabled', true);
    			$.scojs_message('撤销成功!,1秒后自动返回调车单列表。。。', $.scojs_message.TYPE_OK);
    			setTimeout(function(){
					location.href="/pickupOrder";
				}, 1000);
    		}
    	});
    	
    });
});
