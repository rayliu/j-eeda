$(document).ready(function() {
	$('#menu_return').addClass('active').find('ul').addClass('in');
		
	var returnOrderId = $("#returnId").val();
	var transferOrderId =$("#transferOrderId").val();
	console.log(transferOrderId);
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
        "sAjaxSource": "/returnOrder/transferOrderItemList?order_id="+returnOrderId+"&id="+transferOrderId,
        "aoColumns": [ 
			{
				"mDataProp":null,            	
				"sWidth": "80px",
				"fnRender":function(obj){
					var str = "";
            		if(obj.aData.SERIAL_NO != undefined && obj.aData.SERIAL_NO != null && obj.aData.SERIAL_NO != ""){
            			str = obj.aData.SERIAL_NO;
            		}
            		return str;
				}
			},
            {
            	"mDataProp":"ITEM_NO",            	
            	"sWidth": "80px",
            	"sClass": "item_no"
        	},
            {
            	"mDataProp":"ITEM_NAME",
            	"sWidth": "100px",
            	"sClass": "item_name"
            },
            {
            	"mDataProp":"SIZE",            	
            	"sWidth": "80px",
            	"sClass": "size"
        	},
            {
            	"mDataProp":"WIDTH",
            	"sWidth": "80px",
            	"sClass": "width"
            },
            {
            	"mDataProp":"HEIGHT",            	
            	"sWidth": "80px",
            	"sClass": "height"
        	}, 
            {
            	"mDataProp":"WEIGHT",
            	"sWidth": "80px",
            	"sClass": "weight",
            },
        	{
            	"mDataProp":"AMOUNT",
            	"sWidth": "60px",
            	"sClass": "amount"
            }, 
        	{
            	"mDataProp":null,
            	"sWidth": "60px",
            	"fnRender":function(obj){
            		if(obj.aData.TIEM_NAME =="ATM"){
						return obj.Data.PIECES;
					}
					return "";
				}
            },
            {
            	"mDataProp":"UNIT",
            	"sWidth": "60px",
            	"sClass": "unit"
            },
            {
            	"mDataProp":null,
            	"sWidth": "95px",
            	"sClass": "sumWeight",
            	"fnRender": function(obj) {
            		var str = "";
            		if(obj.aData.SUM_WEIGHT != undefined && obj.aData.SUM_WEIGHT != null && obj.aData.SUM_WEIGHT != ""){
            			str = obj.aData.SUM_WEIGHT;
            		}
            		return str;
                }
            },
            {
            	"mDataProp":"VOLUME",
            	"sWidth": "95px",
            	"sClass": "volume",
            	"fnRender": function(obj) {
            		return obj.aData.VOLUME;
            	}
            },            
            {"mDataProp":"REMARK"},
                              
        ]  
    });	
	var orderId = $("#order_id").val();
	//datatable, 动态处理
    var detailDataTable = $('#detailTable').dataTable({
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/returnOrder/transferOrderDetailList2?item_id="+returnOrderId,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [  			            
            {
            	"mDataProp":"SERIAL_NO",
        		"sWidth": "80px",
            	"sClass": "serial_no"	
            },
            {
            	"mDataProp":"ITEM_NO",
        		"sWidth": "80px",
            	"sClass": "item_no"            		
            },  
		    {
		    	"mDataProp":"ITEM_NAME",
		    	"sWidth": "80px",
		    	"sClass": "item_name"            		
		    },       	
            {
            	"mDataProp":"VOLUME",
        		"sWidth": "80px",
            	"sClass": "volume"            		
            },
            {
            	"mDataProp":"WEIGHT",
        		"sWidth": "80px",
            	"sClass": "weight"
            },
            {
            	"mDataProp":"CONTACT_PERSON",
        		"sWidth": "80px",
            	"sClass": "contact_person"
            },
            {
            	"mDataProp":"PHONE",
        		"sWidth": "80px",
            	"sClass": "phone"
            },
            {
            	"mDataProp":"ADDRESS",
        		"sWidth": "80px",
            	"sClass": "address"
            },
            {
            	"mDataProp":"REMARK",
        		"sWidth": "80px",
            	"sClass": "remark"
            },
            {  
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger btn-xs deleteDetail' code='?item_id="+obj.aData.ID+"&notify_party_id="+obj.aData.NOTIFY_PARTY_ID+"'' title='删除'>"+
		                        "<i class='fa fa-trash-o fa-fw'></i>"+
		                    "</a>";
                }
            }                         
        ]      
    });
	
	//已签收/取消  按钮不可用
	var result = $("#returnStatus").val();
	if(result=='new')
		$("#status span").append("新建"); 
	else if(result=='confirmed')
    	$("#status span").append("已确认"); 
    else if(result=='cancel'){
    	$("#status span").append("取消"); 
    	$("#returnOrderAccomplish").attr("disabled", true);
    }else if(result=='已签收'){
		$("#status span").append("已签收"); 
		$("#returnOrderAccomplish").attr("disabled", true);
	}else{
		$("#status span").append(result); 
	}
	
	// 回单签收
	$("#returnOrderAccomplish").on('click', function(e){
		var receivableTotal = $("#receivableTotal").val();
 		if(receivableTotal != null && receivableTotal != ""){
			e.preventDefault();
	        //异步向后台提交数据
			var id = $("#returnId").val();
			$.post('/returnOrder/returnOrderReceipt/'+id,function(data){
	           //保存成功后，刷新列表
	           console.log(data);
	           if(data.success){
	        	   //alert('签收成功！');
	        	   $("#status span").html("已签收");
	        	   $("#returnOrderAccomplish").attr("disabled", true);
	           }else{
	               alert('签收失败！');
	           }
	        },'json');
 		}else{
 			alert("请收款(新增应收)后再签收！");
 		}
	});
	
 	// 查看货品
	$("#transferOrderTable").on('click', '.dateilEdit', function(e){
		e.preventDefault();
		
		$("#transferOrderItemDateil").show();
		var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		$("#item_id").val(itemId);
		
		detailDataTable.fnSettings().sAjaxSource = "/returnOrder/transferOrderDetailList2?item_id="+itemId;
		detailDataTable.fnDraw();  			
	});
	// 删除货品
	$("#transferOrderTable").on('click', '.deleteItem', function(e){
		var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		$("#item_id").val(itemId);
		$.post('/returnOrder/deleteTransferOrderItem', 'transfer_order_item_id='+itemId, function(data){
		},'json');
		$("#transferOrderItemDateil").hide();
		// 更新货品列表
		var returnOrderId = $("#returnId").val();
		transferOrder.fnSettings().sAjaxSource = "/returnOrder/transferOrderItemList?order_id="+returnOrderId;
		transferOrder.fnDraw(); 	  	
	});	

	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	var parentId = "chargeCheckOrderbasic";
	
	$("#returnOrderItemList").click(function(e){
		e.preventDefault();
		//$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
		parentId = e.target.getAttribute("id");
	});
	$("#returnOrderPayment").click(function(e){
		e.preventDefault();
		parentId = e.target.getAttribute("id");
		
		receipttable.fnDraw(); 
	});
	$("#chargeCheckOrderbasic").click(function(e){
		e.preventDefault();
		/*if(bool){
			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
		}*/
		parentId = e.target.getAttribute("id");
	});

	
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveReturnOrderBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
    	$.post('/returnOrder/save', $("#returnOrderForm").serialize(), function(returnOrder){
			if(returnOrder.ID>0){
			  	//$("#style").show();
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
        "sAjaxSource":"/returnOrder/accountReceivable/"+order_id,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
  			{"mDataProp":"CNAME","sWidth": "80px"},
  			{"mDataProp":"TRANSFER_ORDER_NO","sWidth": "80px"},
			{"mDataProp":"DELIVERY_ORDER_NO","sWidth": "80px"},
			{"mDataProp":"NAME",
			    "fnRender": function(obj) {
			    	if(obj.aData.CREATE_NAME == 'system'){
			    		$("#receivableTotal").val(obj.aData.NAME);
		        		return obj.aData.NAME;
		        	}else{
				        if(obj.aData.NAME!='' && obj.aData.NAME != null){
				        	var str="";
				        	$("#receivableItemList").children().each(function(){
				        		if(obj.aData.NAME == $(this).text()){
				        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";   
				        			$("#receivableTotal").val(obj.aData.NAME);
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
		        	}
			 }},
			{"mDataProp":"AMOUNT",
			     "fnRender": function(obj) {
			    	 if(obj.aData.CREATE_NAME == 'system'){
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return obj.aData.AMOUNT;
				         }else{
				         	 return "";
				         }
			    	 }else{
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
				         }else{
				         	 return "<input type='text' name='amount'>";
				         }
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
			{"mDataProp":"STATUS","sWidth": "80px","sClass": "status"},
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
	
	//应收
	$("#addrow2").click(function(){	
		 var order_id =$("#returnOrderid").val();
		 $.post('/returnOrder/addNewRow/'+order_id,function(data){
			console.log(data);
			if(data[0] != null){
				receipttable.fnSettings().sAjaxSource = "/returnOrder/accountReceivable/"+order_id;
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
		$.post('/returnOrder/updateTransferOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
			if(data.success){
			}else{
				alert("修改失败!");
			}
    	},'json');
	});
	//异步删除应付
	 $("#table_fin").on('click', '.finItemdel', function(e){
		 var id = $(this).attr('code');
		  e.preventDefault();
		  $.post('/returnOrder/finItemdel/'+id,function(data){
              //保存成功后，刷新列表
              console.log(data);
              receipttable.fnDraw();
          },'json');
	 });

    //获取全国省份
    $(function(){
     	var province = $("#mbProvinceTo");
     	$.post('/serviceProvider/province',function(data){
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
			$('#locationChanged').val('true');
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
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
    		$('#locationChanged').val('true');
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
			$.get('/serviceProvider/area', {id:inputStr}, function(data){
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
    		$('#locationChanged').val('true');
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
		});  
    
    // 回显城市
    var hideProvince = $("#hideProvinceTo").val();
    $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
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
    $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
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
