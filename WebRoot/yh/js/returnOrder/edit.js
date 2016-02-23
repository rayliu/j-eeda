$(document).ready(function() {
	if(returnOrder.orderNo){
		document.title = returnOrder.orderNo +' | '+document.title;
	}
	$('#menu_return').addClass('active').find('ul').addClass('in');
		
	
	$('#checkQrCode').hover(
		function(){//in
		$('#qrcodeCanvas').show();
		},function(){//out
		$('#qrcodeCanvas').hide();
	});
		
	$('#qrcodeCanvas').qrcode({
		width: 120,
		height: 120,
		text	: 'http://'+window.location.host+'/wx/fileUpload/'+returnOrder.orderNo //'http://'+window.location.host+'
	});	
	var returnOrderId = $("#returnId").val();
	var transferOrderId =$("#transferOrderId").val();
	//datatable, 动态处理
	var transferOrder = $('#transferOrderTable').dataTable({
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/returnOrder/transferOrderItemList?order_id="+returnOrderId+"&id="+transferOrderId,
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('ID', aData.TID);
			return nRow;
		},
        "aoColumns": [ 
			{
				"mDataProp":null,            	
				"sWidth": "80px",
				"fnRender":function(obj){
					var str = "<input type='text' name='serial_no' id='serial_no' value='"+obj.aData.SERIAL_NO+"'/>";
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
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
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
	
	if(result=='new' || result=='新建')
		$("#status span").append("新建"); 
	else if(result=='confirmed')
    	$("#status span").append("已确认"); 
    else if(result=='cancel'){
    	$("#status span").append("取消"); 
    	$("#returnOrderAccomplish").attr("disabled", true);
    }else if(result=='已签收'){
		$("#status span").append("已签收"); 
		$("#saveReturnOrderBtn").attr("disabled", true);
		$("#returnOrderAccomplish").attr("disabled", true);
		$("#returnOrderRefused").attr("disabled", true);
		/*$("#addrow2").attr("disabled",true);*/
	}else if(result=='已拒收'){
		$("#status span").append("已拒收"); 
		$("#saveReturnOrderBtn").attr("disabled", true);
		$("#returnOrderAccomplish").attr("disabled", true);
		$("#returnOrderRefused").attr("disabled", true);
		/*$("#addrow2").attr("disabled",true);*/
	}else{
		$("#status span").append(result); 
		$("#saveReturnOrderBtn").attr("disabled",true);
		$("#addrow2").attr("disabled",true);
		$("#returnOrderAccomplish").attr("disabled", true);
	}
	
	// 先保存一次，再回单签收
	$("#returnOrderAccomplish").on('click', function(e){
		e.preventDefault();
		$("#sign_document_no").val($("#sign_no").val());
		//异步向后台提交数据
    	$.post('/returnOrder/save', $("#returnOrderForm").serialize(), function(returnOrder){
			if(returnOrder.ID>0){
			  	var receivableTotal = $("#receivableTotal").val();
			
				$("#saveReturnOrderBtn").attr("disabled", true);
	     	    $("#returnOrderAccomplish").attr("disabled", true);
	     	   $("#returnOrderRefused").attr("disabled", true);
		        //异步向后台提交数据
				var id = $("#returnId").val();
				$.post('/returnOrder/returnOrderReceipt/'+id,function(data){
		           //保存成功后，刷新列表
		           if(data.success){
		        	   $.scojs_message('签收成功', $.scojs_message.TYPE_OK);
		        	   $("#status span").html("已签收");
		           }else{
		               alert('签收失败！');
		           }
		        },'json');
			}else{
				alert('数据保存失败。');
			}
		},'json');
		

	});
	$("#returnOrderRefused").on('click', function(e){
		e.preventDefault();
		//异步向后台提交数据
		if(confirm("是否拒收！")){
			$.post('/returnOrder/refused', $("#returnOrderForm").serialize(), function(data){
				 if(data.success){
		        	   $.scojs_message('拒收成功', $.scojs_message.TYPE_OK);
		        	   $("#returnOrderRefused").attr("disabled", true);
		        	   $("#saveReturnOrderBtn").attr("disabled", true);
			     	    $("#returnOrderAccomplish").attr("disabled", true);
		           }else{
		        	   $.scojs_message('拒收失败', $.scojs_message.TYPE_ERROR);
		           }
			},'json');
		}
	});
	
	//修改序列号
	$("#transferOrderTable").on('blur', 'input', function(e){
		var ids = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		 $.post('/returnOrder/updateReturnOrder', {ids:ids, name:name, value:value}, function(data){
	    	},'json');
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
	
	$("#returnOrderItemList").click(function(e){
		e.preventDefault();
		//$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	});
	$("#returnOrderPayment").click(function(e){
		e.preventDefault();
		
		receipttable.fnDraw(); 
	});
	$("#chargeCheckOrderbasic").click(function(e){
		e.preventDefault();
		/*if(bool){
			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
		}*/
	});

	
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveReturnOrderBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		$("#sign_document_no").val($("#sign_no").val());
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
 		$("#cargoNatureSpan").text(returnOrder.ex_cargo);
 	}else if($("#cargoNature").val() == 'cargo'){
 		$("#cargoNatureSpan").text('普通货品'); 		
 	}else{
 		$("#cargoNatureSpan").text(''); 		
 	}
 	
 	if($("#pickupMode").val() == 'own'){
 		$("#pickupModeSpan").text(returnOrder.ex_type);
 		
 		
 	}else if($("#pickupMode").val() == 'routeSP'){
 		$("#pickupModeSpan").text('干线供应商自提'); 		
 	}else if($("#pickupMode").val() == 'pickupSP'){
 		$("#pickupModeSpan").text('外包供应商提货'); 		
 	}else{
 		$("#pickupModeSpan").text(''); 		 		
 	}
 	if($("#refused").val() == 'YES'){
 		$("#returnOrderRefused").hide();
 	}
 	if($("#arrivalMode").val() == 'gateIn'){
 		$("#arrivalModeSpan").text('入中转仓');		
 	}else if($("#arrivalMode").val() == 'delivery'){
 		$("#arrivalModeSpan").text('货品直送'); 
 		$("#customer_deliver_no").hide();
 		$("#returnOrderRefused").hide();
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
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "bLengthChange":false,
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
			    	if(obj.aData.CREATE_NAME == 'user'){
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
		        	}else{
		        		$("#receivableTotal").val(obj.aData.NAME);
		        		return obj.aData.NAME;
		        	}
			 }},
			{"mDataProp":"AMOUNT",
			     "fnRender": function(obj) {
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"' style='width:100px;height:30px'>";
				         }else{
				         	 return "<input type='text' name='amount' style='width:100px;height:30px'>";
				         }
			 }},  
			{"mDataProp":"REMARK",
                "fnRender": function(obj) {
                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                        return "<input type='text' name='remark' value='"+obj.aData.REMARK+"' style='width:350px;height:30px' >";
                    }else{
                    	 return "<input type='text' name='remark' style='width:350px;height:30px'>";
                    }
            }}, 
			{"mDataProp":"STATUS","sWidth": "80px","sClass": "status"},
			{"mDataProp":null,
				"sWidth": "130px",
				"fnRender":function(obj) {
					var create_name = obj.aData.CREATE_NAME;
					if(create_name == 'system'){
		        		return "合同费用";
		        	}else if(create_name == 'user'){
		        		return "手工录入费用";
		        	}else if(create_name == 'insurance'){
		        		return "保险费用";
		        	}else{
		        		return "";
		        	}
			}},
			{  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                	if(obj.aData.CREATE_NAME == 'user'){
                		return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
                  		"<i class='fa fa-trash-o fa-fw'> </i> "+
                  		"删除"+
                  		"</a>";
                	}else{
                		return "";
                	}
                    
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
    
    /*//保存图片
    $("#savefile").click(function(e){
    	$("#fileupload").click();
	});
    
	$('#fileupload').fileupload({
        dataType: 'json',
        url: '/returnOrder/saveFile?return_id='+$("#returnId").val()+'&permission='+$("#permission").val(),//上传地址
        validation: {
        	allowedExtensions: ['jpeg', 'jpg', 'png' ,'gif']
    	},
    	//data: { imgPath: $("#uploadFile").val() },  
        done: function (e, data) {
        	if(data.result.result == "true"){
        		//$("#centerBody").empty().append("<h4>上传成功！</h4>");
        		$.scojs_message('上传成功,审核通过后将显示图像', $.scojs_message.TYPE_OK);
        		//console.log("data.result.cause:"+data.result.cause);
        		var showPictures = $("#showPictures");
        		var permission = $("#permission").val();
        		if(permission == "permissionYes"){
        			showPictures.empty().append('<input type="hidden" id="permission" value="permissionYes">');
        			$.each(data.result.cause,function(name,value) {
        				var aText = "待审核";
        				if(value.AUDIT == 1 || value.AUDIT == true)
        					aText = "已审核";
        				showPictures.append('<div style="width:200px;height:210px;float:left;" ><img src="/upload/fileupload/'+value.FILE_PATH+'" alt="" class="imgSign" style="width:180px;height:180px;"><p><a class="picture_audit" picture_id="'+value.ID+'" > ' + aText + ' </a><a class="picture_del" picture_id="'+value.ID+'" > 删除 </a></p></div>');
                    });
        		}else{
        			showPictures.empty().append('<input type="hidden" id="permission" value="permissionNo">');
        			$.each(data.result.cause,function(name,value) {
        				showPictures.append('<div style="width:200px;height:210px;float:left;" ><img src="/upload/fileupload/'+value.FILE_PATH+'" alt="" class="imgSign" style="width:180px;height:180px;"></div>');
                    });
        		}
        	}else{
        		$.scojs_message(data.result.cause, $.scojs_message.TYPE_ERROR);
        	}
    		$('#myModal').modal('hide');
    		$('#cancel').click();
    		//$(".modal-backdrop").remove();
        },  
        progressall: function (e, data) {//设置上传进度事件的回调函数  
        	var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .bar').css('width',progress + '%');
            $('#imgProgress').text("上传过程可能有点慢，请耐心等待：" + progress + "%");
        	$('#myModal').modal('show');
        	$("#footer").hide();
        } 
    });*/
    
    //'use strict';
    $('#file_upload_form').fileupload({
        // Uncomment the following to send cross-domain cookies:
        //xhrFields: {withCredentials: true},
        disableImageResize: false,
        validation: {allowedExtensions: ['jpeg', 'jpg', 'png' ,'gif']},
        url: '/returnOrder/saveFile?return_id='+$("#returnId").val()+'&permission='+$("#permission").val(),
        //imageMaxWidth: 800,
        //imageMaxHeight: 800,
        imageCrop: true // Force cropped images
    });
    
    
	// 删除图片
	$("#showPictures").on('click', '.picture_del', function(e){
		if(confirm("确定删除吗？")){
			var return_id = $("#returnId").val();
			var picture_id = $(this).attr("picture_id");
			var permission = $("#permission").val();
			$.post('/returnOrder/delPictureById', {picture_id:picture_id,return_id:return_id,permission:permission}, function(data){
        		var showPictures = $("#showPictures");
        		var permission = $("#permission").val();
        		if(permission == "permissionYes"){
        			showPictures.empty().append('<input type="hidden" id="permission" value="permissionYes">');
                    $.each(data,function(name,value) {
                    	var aText = "待审核";
        				if(value.AUDIT == 1 || value.AUDIT == true)
        					aText = "已审核";
                        showPictures.append('<div style="margin-right: 10px;float:left;" ><img src="/upload/img/'+value.FILE_PATH+'" alt="" class="img-thumbnail" style="height:180px;"><p><a class="picture_audit" picture_id="'+value.ID+'"> ' + aText + ' </a><a class="picture_del" picture_id="'+value.ID+'" > 删除 </a></p></div>');
                    });
        		}else{
        			showPictures.empty().append('<input type="hidden" id="permission" value="permissionNo">');
        			$.each(data,function(name,value) {
        				showPictures.append('<div style="margin-right: 10px;float:left;" ><img src="/upload/img/'+value.FILE_PATH+'" alt="" class="img-thumbnail" style="height:180px;"></div>');
                    });
        		}
			},'json');
		}
	});	
	
	// 审核图片
	$("#showPictures").on('click', '.picture_audit', function(e){
		var auditVar = $(this);
		var aText = $.trim(auditVar.text());
		if(aText == "待审核")
			aText = "审核通过";
		else
			aText = "取消审核";
		if(confirm("确定"+aText+"吗？")){
			var return_id = $("#returnId").val();
			var picture_id = $(this).attr("picture_id");
			$.post('/returnOrder/auditPictureById', {picture_id:picture_id,return_id:return_id}, function(data){
				if(data.AUDIT == 1 || data.AUDIT == true)
					auditVar.text("已审核");
				else
					auditVar.text("待审核");
			},'json');
		}
	});	
	
	//图片放大
	$("#showPictures").on('click', '.img-thumbnail', function(e){
		var imgAdd = $(this).attr("src");
		$("#lgImgDiv").empty().append("<img id='focusphoto' src='"+imgAdd+"' />"); 
		var temp = new Image();
		temp.src = imgAdd;
		var divWidth = $('#imgContent').width() - 40;
		var imgWidth = temp.width;
		console.log("弹出框宽度:"+divWidth+",图片宽度："+imgWidth+",图片高度："+temp.height);
		if(imgWidth > divWidth){
			var imgHeight = temp.height;
			var width = divWidth;
			var height = imgHeight * (divWidth / imgWidth);
			$("#lgImgDiv").empty().append("<img id='focusphoto' src='"+imgAdd+"' style='width:"+width+"px;height:"+height+"px;'/>"); 
		}else{
			$("#lgImgDiv").empty().append("<img id='focusphoto' src='"+imgAdd+"' />"); 
		}
		$('#myModal_img').modal('show');
	});	
	
	/*var jic = {
	        *//**
	         * Receives an Image Object (can be JPG OR PNG) and returns a new Image Object compressed
	         * @param {Image} source_img_obj The source Image Object
	         * @param {Integer} quality The output quality of Image Object
	         * @return {Image} result_image_obj The compressed Image Object
	         *//*
	        compress: function(source_img_obj, quality, output_format){
	             var mime_type = "image/jpeg";
	             if(output_format!=undefined && output_format=="png"){
	                mime_type = "image/png";
	             }
	             var divWidth = $('#imgContent').width() - 40;
	             var cvs = document.createElement('canvas');
	             //naturalWidth真实图片的宽度
	             if (source_img_obj.naturalWidth > divWidth) {
	            	 cvs.width = divWidth;
	            	 cvs.height = source_img_obj.naturalHeight * (divWidth / source_img_obj.naturalWidth);
		         }else{
		        	 cvs.width = source_img_obj.naturalWidth;
		             cvs.height = source_img_obj.naturalHeight;
		         }
	             var ctx = cvs.getContext("2d").drawImage(source_img_obj, 0, 0);
	             var newImageData = cvs.toDataURL(mime_type, quality/100);
	             var result_image_obj = new Image();
	             result_image_obj.src = newImageData;
	             return result_image_obj;
	        }
	};
	
	function handleFileSelect (evt) {
		var files = evt.target.files;
		for (var i = 0, f; f = files[i]; i++) {
	      // Only process image files.
	      if (!f.type.match('image.*')) {
	        continue;
	      }
	      var reader = new FileReader();
	      // Closure to capture the file information.
	      reader.onload = (function(theFile) {
	        return function(e) {
		          var i = document.getElementById("return_img");
		          var divWidth = $('#imgContent').width() - 40;
		          i.src = e.target.result;
		          console.log("原图宽："+$(i).width()+",原图高："+$(i).height());
		          if ($(i).width() > divWidth) {
		          		$(i).css('width',divWidth+'px');
		          }
		          //$(i).css('width',$(i).width()/10+'px');
		          //$(i).css('height',$(i).height()/10+'px');
		          console.log("修改后图宽："+$(i).width()+",修改后图高："+$(i).height());
		          var quality =  50;
		          i.src = jic.compress(i,quality).src;
		          //console.log(i.src);
		          //i.style.display = "block";
		          console.log("jic返回图宽："+i.width+",jic返回图高："+i.height);
		          //evt.target.files = i.src;
	    	  };
	      })(f);
	      // Read in the image file as a data URL.
	      reader.readAsDataURL(f);
	    }
		
		dropZone;
		
	}
	
	//图片压缩上传
	//document.getElementById('fileupload').addEventListener('change', handleFileSelect, false);
	
	
	
	
	
	var imgTypeArr = new Array();  
	var imgArr = new Array();  
	var isHand = 0;//1正在处理图片  
	var nowImgType = "image/jpeg";  
	var jic = {  
	        compress: function(source_img_obj,imgType){  
	            //alert("处理图片");  
	            source_img_obj.onload = function() {  
	                var cvs = document.createElement('canvas');  
	                //naturalWidth真实图片的宽度  
	                console.log("原图--"+this.width+":"+this.height);  
	                
	                var scale = 1;  
	                if(this.width > 1600 || this.height > 1600){  
	                    if(this.width > this.height){  
	                        scale = 1600 / this.width;  
	                    }else{  
	                        scale = 1600 / this.height;  
	                    }  
	                }  
	                cvs.width = this.width*scale;  
	                cvs.height = this.height*scale;  
	  
	                var ctx=cvs.getContext("2d");  
	                ctx.drawImage(this, 0, 0, cvs.width, cvs.height);  
	                var newImageData = cvs.toDataURL(imgType, 0.8);  
	                base64Img = newImageData;  
	                imgArr.push(newImageData);  
	  
	               // $("#canvasDiv").append(cvs);  
	                var img = new Image();  
	                img.src = newImageData;  
	                $(img).css('width',100+'px');  
	                $(img).css('height',100+'px');  
	                $("#canvasDiv").append(img).find("img").attr("id","upload_img").hide(); 
	                isHand = 0;  
	              
	            }  
	          
	        }  
	};
	      
    function handleFileSelect (evt) {  
        isHand = 1;  
        imgArr = [];  
        imgTypeArr = [];  
        $("#canvasDiv").html("");  
        var files = evt.target.files;  
        for (var i = 0, f; f = files[i]; i++) {  
	        // Only process image files.  
	        if (!f.type.match('image.*')) {  
	        	continue;  
	        }  
	        imgTypeArr.push(f.type);  
	        nowImgType = f.type;  
	        var reader = new FileReader();  
	        // Read in the image file as a data URL.  
	        reader.readAsDataURL(f);  
	        // Closure to capture the file information.  
	        reader.onload = (function(theFile) {  
	            return function(e) {  
	                var i = new Image();  
	                i.src = e.target.result;  
	                jic.compress(i,nowImgType);  
	                  
	            };  
	        })(f);  
          
        }  
          
    }  
	
    //绑定上传事件
    //document.getElementById('fileToUpload').addEventListener('change', handleFileSelect, false);  
      
    //消息提示  
    function show_msg(msg){  
        //消息显示时间  
        var time = arguments[1] ? arguments[1] : 1500;  
        $('#info_pop p').text(msg);  
        $("#info_pop").popup("open");  
        setTimeout('$("#info_pop").popup("close");',time);  
    }  
    
    $("#imgUploadBtn").click(function(){
    	
    	if(base64Img == ""){  
            show_msg("请选择图片！");  
            return;  
        }  
        if(isHand == 1){  
            show_msg("请等待图片处理完毕！");  
            return;  
        }  
        $('.ui-loader').show();  
        
        var data = new FormData();
        data.append('return_img', imgArr);
        $.ajax({
            url: '/returnOrder/saveFile?return_id='+$("#returnId").val()+'&permission='+$("#permission").val(),
            data: data,
            cache: false,
            //contentType: 'multipart/form-data',
            processData: false,
            type: 'POST',
            success: function(data){
                alert(data);
            }
        });
    	
        $.post("/returnOrder/saveFile",{img: imgArr,type:imgTypeArr},function(res){
            var res = eval('(' + res + ')');
            if(res.status == 1){
                o.error(res.msg);
            }else{
                o.success(res.imgurl);
            }
            console.log(res);
        });
        
       $.ajaxFileUpload({
            url: '/returnOrder/saveFile?return_id='+$("#returnId").val()+'&permission='+$("#permission").val(), //用于文件上传的服务器端请求地址
            secureuri: false, //是否需要安全协议，一般设置为false
            fileElementId: 'upload_img', //文件上传域的ID
            dataType: 'json', //返回值类型 一般设置为json
            success: function (data, status){  //服务器成功响应处理函数
                alert("成功上传");
            },
            error: function (data, status, e){//服务器响应失败处理函数
                alert(e);
            }
        });
    	
    });*/
    	
    	
    	
    	
    
    
    
	
});

