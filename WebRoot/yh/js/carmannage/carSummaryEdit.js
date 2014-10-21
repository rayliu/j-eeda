$(document).ready(function() {
	
	
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
					alert('数据保存失败。');
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
					alert('数据保存失败。');
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
	
 	//点击保存
	$("#saveCarSummaryBtn").click(function(e){
		if($("#carSummaryId").val() == ""){//创建行车单
			 $.post('/yh/carsummary/saveCarSummary', $("#carSummaryForm").serialize(), function(data){
					
				if(data){
					$("#saveCarSummaryBtn").attr("disabled", true);
					alert('数据保存成功。');
				}else{
					alert('数据保存失败。');
				}
				
			},'json');
		 }else{//修改行车单
			 
		 }
	});
	
	// 选显卡-线路
	$("#carmanageLine").click(function(e){
		var pickupIds = $("#pickupIds").val();
		var detailTable= $('#pickupAddressTbody').dataTable({           
	         "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",  
	         "iDisplayLength": 10,
	         "bServerSide": true, 
	     	 "oLanguage": {
	             "sUrl": "/eeda/dataTables.ch.txt"
	         },
	         "sAjaxSource": "/yh/carsummary/findAllAddress?pickupIds="+pickupIds,
	         "aoColumns": [
	             { "mDataProp": "DEPART_NO"},
	             { "mDataProp": "ORDER_NO"},      
	             { "mDataProp": "ABBR"},
	             { "mDataProp": "ADDRESS1"},
	             { "mDataProp": "CREATE_STAMP"},
	             { "mDataProp": null,
	            	 "fnRender": function(obj) {    
	            		 if(obj.aData.ADDRESS3 != null){
	            			 return "仓库（"+obj.aData.ADDRESS3+"）";
	            		 }else if(obj.aData.ADDRESS2 != null ){
	            			 return "货场（"+obj.aData.ADDRESS2+"）";
	            		 }else if(obj.aData.ADDRESS2 != null && obj.aData.ADDRESS3 != null){
	            			 return "货场（"+obj.aData.ADDRESS2+"）仓库（"+obj.aData.ADDRESS3+"）";
	            		 }else{
	            			 return "";
	            		 }
	                 },
	             }
	         ]        
	     });
		
	});
	
	// 选显卡-货品信息
	$("#carmanageLine").click(function(e){
		var pickupIds = $("#pickupIds").val();
		/*var detailTable= $('#pickupAddressTbody').dataTable({           
	         "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",  
	         "iDisplayLength": 10,
	         "bServerSide": true, 
	     	 "oLanguage": {
	             "sUrl": "/eeda/dataTables.ch.txt"
	         },
	         "sAjaxSource": "/yh/carsummary/findAllAddress?pickupIds="+pickupIds,
	         "aoColumns": [
	             { "mDataProp": "DEPART_NO"},
	             { "mDataProp": "ORDER_NO"},      
	             { "mDataProp": "ABBR"},
	             { "mDataProp": "ADDRESS1"},
	             { "mDataProp": "CREATE_STAMP"},
	             { "mDataProp": null,
	            	 "fnRender": function(obj) {    
	            		 if(obj.aData.ADDRESS3 != null){
	            			 return "仓库（"+obj.aData.ADDRESS3+"）";
	            		 }else if(obj.aData.ADDRESS2 != null ){
	            			 return "货场（"+obj.aData.ADDRESS2+"）";
	            		 }else if(obj.aData.ADDRESS2 != null && obj.aData.ADDRESS3 != null){
	            			 return "货场（"+obj.aData.ADDRESS2+"）仓库（"+obj.aData.ADDRESS3+"）";
	            		 }else{
	            			 return "";
	            		 }
	                 },
	             }
	         ]        
	     });*/
		
	});
	
	// 选显卡-路桥费明细
	$("#carmanageRoadBridge").click(function(e){
		/*var pickupIds = $("#pickupIds").val();
		var detailTable= $('#pickupDetail-table').dataTable({           
	         "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",  
	         "iDisplayLength": 10,
	         "bServerSide": true, 
	     	 "oLanguage": {
	             "sUrl": "/eeda/dataTables.ch.txt"
	         },
	         "sAjaxSource": "/yh/carsummary/findAllCarSummaryOrder="+pickupIds,
	       
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
	     });*/
		
	});
	
	
	
	
	
});


