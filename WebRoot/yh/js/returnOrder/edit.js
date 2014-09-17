$(document).ready(function() {
	$('#menu_return').addClass('active').find('ul').addClass('in');
		
	var returnOrderId = $("#returnId").val();
	//datatable, 动态处理
	var transferOrder = $('#transferOrderTable').dataTable({
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/returnOrder/transferOrderList?returnOrderId="+returnOrderId,
        "aoColumns": [   
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
            			return "<a href='/yh/transferOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
            		}},
            
            {"mDataProp":"STATUS"},
            {"mDataProp":"CARGO_NATURE",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "damageCargo"){
            			return "损坏货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return "ATM";
            		}else{
            			return "";
            		}}},   
            {"mDataProp":"SERIAL_NO"},
    		{"mDataProp":"OPERATION_TYPE",
    			"fnRender": function(obj) {
    				if(obj.aData.OPERATION_TYPE == "out_source"){
    					return "外包";
    				}else if(obj.aData.OPERATION_TYPE == "own"){
    					return "自营";
    				}else{
    					return "";
    				}}},        	
            {"mDataProp":"PICKUP_MODE",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == "routeSP"){
            			return "干线供应商自提";
            		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
            			return "外包供应商提货";
            		}else if(obj.aData.PICKUP_MODE == "own"){
            			return "源鸿自提";
            		}else{
            			return "";
            		}}},
            {"mDataProp":"ARRIVAL_MODE",
            	"fnRender": function(obj) {
            		if(obj.aData.ARRIVAL_MODE == "delivery"){
            			return "货品直送";
            		}else if(obj.aData.ARRIVAL_MODE == "gateIn"){
            			return "入中转仓";
            		}else{
            			return "";
            		}}},
            {"mDataProp":"CREATE_STAMP"},
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
            		}else if(obj.aData.ORDER_TYPE == "gateOutTransferOrder"){
            			return "出库运输单";
            		}else{
            			return "";
            		}}},
            {"mDataProp":"REMARK"}                      
        ]  
    });	
	
	// 编辑单品
	$("#itemTable").on('click', '.dateilEdit', function(e){
		e.preventDefault();
		var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		$("#item_id").val(itemId);
		$("#transferOrderItemDateil").show();

		// 设置单品信息
		$("#detail_transfer_order_id").val($("#order_id").val());
		$("#detail_transfer_order_item_id").val(itemId);
		
		detailDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItemDetail/transferOrderDetailList?item_id="+itemId;
		detailDataTable.fnDraw();  
	});

	/*--------------------------------------------------------------------*/
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	
	$("#returnOrderItemList").click(function(e){
		e.preventDefault();
		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	});
	$("#returnOrderPayment").click(function(e){
		e.preventDefault();
		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	});
	/*--------------------------------------------------------------------*/
	
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveReturnOrderBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
    	$.post('/yh/returnOrder/save', $("#returnOrderForm").serialize(), function(returnOrder){
			if(returnOrder.ID>0){
			  	$("#style").show();
			  	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			}else{
				alert('数据保存失败。');
			}
		},'json');
	});
 	
 	if($("#status").val() == 'new'){
 		$("#statusSpan").text('新建');
 	}else if($("#status").val() == 'confirmed'){
 		$("#statusSpan").text('确认'); 		
 	}else{
 		$("#statusSpan").text(''); 		
 	}
 	
 	if($("#cargoNature").val() == 'ATM'){
 		$("#cargoNatureSpan").text('ATM');
 	}else if($("#cargoNature").val() == 'cargo'){
 		$("#cargoNatureSpan").text('普通货品'); 		
 	}else{
 		$("#cargoNatureSpan").text(''); 		
 	}
 	
 	if($("#pickupMode").val() == 'own'){
 		$("#pickupModeSpan").text('源鸿自提');
 	}else if($("#pickupMode").val() == 'routeSP'){
 		$("#pickupModeSpan").text('干线供应商自提'); 		
 	}else if($("#pickupMode").val() == 'pickupSP'){
 		$("#pickupModeSpan").text('外包供应商提货'); 		
 	}else{
 		$("#pickupModeSpan").text(''); 		 		
 	}
 	
 	if($("#arrivalMode").val() == 'gateIn'){
 		$("#arrivalModeSpan").text('入中转仓');
 	}else if($("#arrivalMode").val() == 'delivery'){
 		$("#arrivalModeSpan").text('货品直送'); 		
 	}else{
 		$("#arrivalModeSpan").text(''); 		
 	}
 	
 	//应收datatable
 	 var order_id =$("#returnOrderid").val();
	var receipttable =$('#table_fin').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource":"/yh/transferOrder/accountReceivable/"+order_id,
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
			        	$("#receivableItemList").children().each(function(){
			        		if(obj.aData.NAME == $(this).text()){
			        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
			        		}else{
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			            return "<select name='fin_item_id'>"+str+"</select>";
			        }else{
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
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
			{"mDataProp":"TRANSFERORDERNO","sWidth": "80px","sClass": "remark"},
			{"mDataProp":"REMARK",
                "fnRender": function(obj) {
                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                        return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
                    }else{
                    	 return "<input type='text' name='remark'>";
                    }
            }}, 
			{"mDataProp":"STATUS","sWidth": "80px","sClass": "status"},
        ]      
    });
	//应收
	$("#addrow2").click(function(){	
		 var order_id =$("#returnOrderid").val();
		 $.post('/yh/transferOrder/addNewRow2/'+order_id,function(data){
			console.log(data);
			if(data[0] != null){
				receipttable.fnSettings().sAjaxSource = "/yh/transferOrder/accountReceivable/"+order_id;
				receipttable.fnDraw();  
			}else{
				alert("请到基础模块维护应收条目！");
			}
		});		
	});	
	//应收修改
	$("#table_fin").on('blur', 'input,select', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		$.post('/yh/transferOrder/updateTransferOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
			if(data.success){
			}else{
				alert("修改失败!");
			}
    	},'json');
	});
	

    //获取全国省份
    $(function(){
     	var province = $("#mbProvinceTo");
     	$.post('/yh/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
				var hideProvince = $("#hideProvinceTo").val();
     		for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideProvince){
     				province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
     				
     				
					}else{
     				province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     		
     	},'json');
    });
    
    //获取省份的城市
    $('#mbProvinceTo').on('change', function(){
			var inputStr = $(this).val();
			$.get('/yh/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCityTo");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				toLocationList.show();
			},'json');
		});
    
    //获取城市的区县
    $('#cmbCityTo').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
			$.get('/yh/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbAreaTo");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				toLocationList.show();
			},'json');
		});
    
    $('#cmbAreaTo').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
		});  
    
    // 回显城市
    var hideProvince = $("#hideProvinceTo").val();
    $.get('/yh/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
		if(data.length > 0){
			var cmbCity =$("#cmbCityTo");
			cmbCity.empty();
			cmbCity.append("<option>--请选择城市--</option>");
			var hideCity = $("#hideCityTo").val();
			for(var i = 0; i < data.length; i++)
			{
				if(data[i].NAME == hideCity){
					cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
				}else{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			}
		}
	},'json');

    // 回显区
    var hideCity = $("#hideCityTo").val();
    $.get('/yh/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
		if(data.length > 0){
			var cmbArea =$("#cmbAreaTo");
			cmbArea.empty();
			cmbArea.append("<option>--请选择区(县)--</option>");
			var hideDistrict = $("#hideDistrictTo").val();
			for(var i = 0; i < data.length; i++)
			{
				if(data[i].NAME == hideDistrict){
					cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
				}else{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			}
		}
	},'json');
});
