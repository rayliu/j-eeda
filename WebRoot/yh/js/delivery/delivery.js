
$(document).ready(function() {
	if(deliverOrder.orderNo){
		document.title = deliverOrder.orderNo +' | '+document.title;
	}
	$('#checkQrC').hover(
			function(){//in
			$('#qrcodeCanv').show();
			},function(){//out
			$('#qrcodeCanv').hide();
		});
			
		$('#qrcodeCanv').qrcode({
			width: 120,
			height: 120,
			text	: 'http://'+window.location.host+'/wx/fileUpload/'+deliverOrder.orderNo //'http://'+window.location.host+'
		});	
	
	$('#menu_deliver').addClass('active').find('ul').addClass('in');

	$('#resetbutton').hide();
	$('#resetbutton2').hide();
	
	var parentId="chargeCheckOrderbasic";
	
	var deliveryStatus=$("#deliveryOrder_status").text();
	if(deliveryStatus=="已送达"||deliveryStatus=="已签收"||deliveryStatus=="已发车"){//“已签收” 这个状态是“已送达”的旧数据
		$("#saveBtn").attr("disabled",true);
	}else{
		$("#saveBtn").attr("disabled",false);
	}

	$('#spMessage').on('keyup click', function(){
		var inputStr = $('#spMessage').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/delivery/searchPartSp', {input:inputStr}, function(data){			
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
				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');

		$("#spList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#spList').show();
	});

	// 没选中供应商，焦点离开，隐藏列表
	$('#spMessage').on('blur', function(){
 		$('#spList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#spList').on('blur', function(){
 		$('#spList').hide();
 	});

	$('#spList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	// 选中供应商
	$('#spList').on('mousedown', '.fromLocationItem', function(e){
		$('#spMessage').val($(this).text());
		$('#sp_id').val($(this).attr('spid'));
		$('#cid').val($(this).attr('code'));
		$('#a1').html($(this).attr('contact_person'));
		$('#a2').html($(this).attr('company_name'));
		$('#a3').html($(this).attr('address'));
		$('#a4').html($(this).attr('mobile'));
		
		getChargetype();
        $('#spList').hide();
    }); 
			
	var trandferOrderId = $("#tranferid").val();
	var localArr =$("#localArr").val();
	var localArr2 =$("#localArr2").val();
	var localArr3 =$("#localArr3").val();
	var aa =$("#transferstatus").val();
	
	var cargoNature =$("#cargoNature").val();
	if(cargoNature == "cargo"){
		$("#cargotable2").hide();
		$("#cargos").show();
		var warehouseId = $("#warehouse_id").val();
		var customerId = $("#customer_id").val();
		var transferItemIds = $("#transferItemIds").val();
		var productIds = $("#productIds").val();
		var shippingNumbers = $("#shippingNumbers").val();
		
		console.log("仓库：" +warehouseId+",客户："+customerId+",产品："+productIds+",数量："+shippingNumbers);
		var numbers = shippingNumbers.split(",");
		var num = 0;
		// 普货, 动态处理
		$('#cargo-table').dataTable({
			"bFilter": false, 
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "iDisplayLength": 10,
	        "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/delivery/orderListCargo?transferItemIds="+transferItemIds,
	        "aoColumns": [
	            {"mDataProp":"ITEM_NO"},  
	            {"mDataProp":"ITEM_NAME"},
	            {"mDataProp":"VOLUME"},
	            {"mDataProp":"WEIGHT"},
	            {"mDataProp":null,
	            	"fnRender": function(obj) {
	            		return numbers[num++];
	            	}
	            },   
	            {"mDataProp":"ABBR"},
	            {"mDataProp":"ORDER_NO"}
	        ]      
	    });	
	}else{
		//ATM,eedaTable
		$('#eeda-table').dataTable({
			"bFilter": false, //不需要默认的搜索框
			"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "iDisplayLength": 10,
	        "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/delivery/orderList2?localArr="+localArr+"&localArr2="+localArr2,
	        "aoColumns": [
	            {"mDataProp":"ITEM_NO"},  
	            {"mDataProp":"SERIAL_NO",
	            	 "fnRender": function(obj) {
	 			        if(obj.aData.SERIAL_NO!='' && obj.aData.SERIAL_NO != null){
	 			            return "<input type='text' name='serial_no' value='"+obj.aData.SERIAL_NO+"' class='form-control search-control'>";
	 			        }else{
	 			        	 return "<input type='text'  name='serial_no' class='form-control search-control'>";
	 			        }
	 			}},
	            {"mDataProp":"PIECES"},
	            {"mDataProp":"ITEM_NAME"},
	            {"mDataProp":"VOLUME"},
	            {"mDataProp":"WEIGHT"},
	            {"mDataProp":"ORDER_NO", "sWidth": "12%"},
				{"mDataProp":"CUSTOMER"}
	        ]      
	    });	
	}
	
	
	$("#eeda-table").on('blur', 'input,select', function(e){
		e.preventDefault();
		var delivery_id = $("#delivery_id").val();
		var detailId = $("#localArr2").val();
		var name = $(this).attr("name");
		var value = $(this).val();
		$.post('/delivery/updateTansterOrderItemDetail', {name:name, value:value,detailId:detailId}, function(data){
			if(data.ID > 0){
				$.scojs_message('更新货品明细成功', $.scojs_message.TYPE_OK);
			}else{
				$.scojs_message('更新货品明细失败', $.scojs_message.TYPE_OK);
			}
    	},'json');
	});
	
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
			
	var saveDelivery = function(){
		var spMessage = $("#spMessage").val();
		$("#sign_document_no").val($("#sign_no").val());
		var mbProvinceTo = $("#mbProvinceTo").find("option:selected").text();
		var cmbCityTo = $("#cmbCityTo").find("option:selected").text();
		var cmbAreaTo = $("#cmbAreaTo").find("option:selected").text();
		if(spMessage == ""){
			alert("请输入供应商名称");
			return false;
		}
		if(mbProvinceTo == "--请选择省份--" || mbProvinceTo == ""){
			alert("请输入目的地省份");
			return false;
		}
		if(cmbCityTo == "--请选择城市--" || cmbCityTo == ""){
			alert("请输入目的地城市");
			return false;
		}
		$("#receivingunit").val($("#notify_address").val());
		/*if(cmbAreaTo == "--请选择区(县)--" || cmbAreaTo == ""){
			alert("请输入目的地区（县）");
			return false;
		}*/
		$("#saveBtn").attr("disabled", true);
        // 异步向后台提交数据
        $.post('/delivery/deliverySave',$("#deliveryForm").serialize(), function(data){
            console.log(data);
            if(data.ID>0){
            	$("#delivery_id").val(data.ID);
            	// $("#style").show();
            	$("#ConfirmationBtn").attr("disabled", false);
            	$("#order_no").text(data.ORDER_NO);
            	contactUrl("edit?id",data.ID);
            	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
            }else{
                alert('数据保存失败。');
            }
            $("#saveBtn").attr("disabled", false);
         },'json');
            
	 };
	// 添加配送单
	$("#saveBtn").click(function(e){
        // 阻止a 的默认响应行为，不需要跳转
		// var itemId = $("#item_id").val();
		e.preventDefault();
		saveDelivery();
    });
			
	// 发车确认
	$("#ConfirmationBtn").click(function(){
		// 浏览器启动时,停到当前位置
		// debugger;
		$("#receiptBtn").attr("disabled", false); 
		var code = $("#warehouseCode").val();
		var locationTo = $("#locationTo").val();
		var delivery_id = $("#delivery_id").val();
		var priceType = $("input[name='priceType']:checked").val();
		
		var warehouseId = $("#warehouse_id").val();
		var customerId = $("#customer_id").val();
		var transferItemIds = $("#transferItemIds").val();
		var productIds = $("#productIds").val();
		var shippingNumbers = $("#shippingNumbers").val();
		var cargoNature =$("#cargoNature").val();
		var spMessage = $("#spMessage").val();
		$("#sign_document_no").val($("#sign_no").val());
		var mbProvinceTo = $("#mbProvinceTo").find("option:selected").text();
		var cmbCityTo = $("#cmbCityTo").find("option:selected").text();
		if(spMessage == ""){
			alert("请输入供应商名称");
			return false;
		}
		if(mbProvinceTo == "--请选择省份--" || mbProvinceTo == ""){
			alert("请输入目的地省份");
			return false;
		}
		if(cmbCityTo == "--请选择城市--" || cmbCityTo == ""){
			alert("请输入目的地城市");
			return false;
		}
		
		$.post('/deliveryOrderMilestone/departureConfirmation',{delivery_id:delivery_id,code:code,locationTo:locationTo,priceType:priceType,
			warehouseId:warehouseId,customerId:customerId,transferItemIds:transferItemIds,
			productIds:productIds,shippingNumbers:shippingNumbers,cargoNature:cargoNature},function(data){
			var MilestoneTbody = $("#transferOrderMilestoneTbody");
			MilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");

			paymenttable.fnSettings().sAjaxSource="/deliveryOrderMilestone/accountPayable/"+delivery_id;
			paymenttable.fnDraw();
		},'json');
		$("#ConfirmationBtn").attr("disabled", true);
		//$("#receiptBtn").attr("disabled", false);
		$("#saveBtn").attr("disabled", true);
		
	});
	//应付
	$("#arapTab").click(function(e){
		e.preventDefault();
		parentId = e.target.getAttribute("id");
	});
			  	
	// 应收
	$("#arap").click(function(e){
		e.preventDefault();
		
		parentId = e.target.getAttribute("id");
	});
	// 货品明细
	$("#departOrderItemList").click(function(e){
		e.preventDefault();
		parentId = e.target.getAttribute("id");
	});
	
	// 基本信息
	$("#chargeCheckOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	// 运输里程碑
	$("#transferOrderMilestoneList").click(function(e){
		e.preventDefault();
    	// 切换到货品明细时,应先保存运输单
    	// 提交前，校验数据
        if($("#delivery_id").val() == ""){
	    	$.post('/transferOrder/saveTransferOrder', $("#transferOrderForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					if(transferOrder.STATUS == '已发车'){
						$("#departureConfirmationBtn").attr("disabled", true);		
					}else{
						$("#departureConfirmationBtn").attr("disabled", false);
					}
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	// $("#style").show();
				  	
				  	var order_id = $("#order_id").val();
					$.post('/deliveryOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
						var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
						transferOrderMilestoneTbody.empty();
						for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
						{
							transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
						}
					},'json');  
					
					
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }else{
		  	var delivery_id = $("#delivery_id").val(); 
		  	$("#transfer_milestone_delivery_id").val(delivery_id); 
			$.post('/deliveryOrderMilestone/transferOrderMilestoneList',{delivery_id:delivery_id},function(data){
				var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
				transferOrderMilestoneTbody.empty();
				for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
				{
					transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
				}
			},'json'); 
			
        }
        parentId = e.target.getAttribute("id");
	});
			
	// 保存新里程碑
	$("#deliveryOrderMilestoneFormBtn").click(function(){
		$.post('/deliveryOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$('#deliveryOrderMilestone').modal('hide');
	});
	
	// 回单签收
	$("#receiptBtn").click(function(){
		var delivery_id = $("#delivery_id").val();
		$.post('/deliveryOrderMilestone/receipt',{delivery_id:delivery_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$("#receiptBtn").attr("disabled", true);
	});
				
	/*$(function(){
		console.log(aa);
		if(aa!=''){
			$("#cargotable").show();
			$("#cargotable2").hide();
			//$("#tranferdiv").show();
			
		}else{
			$("#cargotable").hide();
			$("#cargotable2").show();
			$("#tranferdiv").hide();
		}
	}) ;*/
	$(function(){
		if($("#deliverystatus").val()=='新建'){
			$("#ConfirmationBtn").attr("disabled", false);
		}
		if($("#deliverystatus").val()=='已发车'){
			$("#ConfirmationBtn").attr("disabled", true);
			$("#receiptBtn").attr("disabled", false);
		}
		if($("#deliverystatus").val()=='已签收'||$("#deliverystatus").val()=='已送达'){
			$("#receiptBtn").attr("disabled", true);
		}
	}) ;
				
				
	// 应付datatable
	var deliveryid =$("#delivery_id").val();
	var paymenttable=$('#table_fin2').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, // 不需要默认的搜索框
        // "sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "bLengthChange":false,
        "sAjaxSource": "/deliveryOrderMilestone/accountPayable/"+deliveryid,
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
			        	if(obj.aData.CREATE_NAME == 'system'){
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
			 {"mDataProp":"CHANGE_AMOUNT"},
			/*
			 * {"mDataProp":"FIN_ITEM_NAME","sWidth":
			 * "80px","sClass": "name"},
			 * {"mDataProp":"AMOUNT","sWidth": "80px","sClass":
			 * "amount"},
			 */
			{"mDataProp":"STATUS","sClass": "status"},
			{"mDataProp":"TRANSFERORDERNO","sClass": "amount", "bVisable":false},  
			{"mDataProp":"REMARK",
                "fnRender": function(obj) {
                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                        return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
                    }else{
                    	 return "<input type='text' name='remark'>";
                    }
            }},  
			/*
			 * {"mDataProp":"REMARK","sWidth": "80px","sClass":
			 * "remark"},
			 */
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
		
	/*
	 * //应收 $("#item_fin_save").click(function(){ var deliveryid
	 * =$("#delivery_id").val();
	 * $.post('/deliveryOrderMilestone/receiptSave/'+deliveryid,
	 * $("#fin_form").serialize(), function(data){ console.log(data);
	 * if(data.success){ //receipttable.fnDraw();
	 * $('#fin_item').modal('hide'); $('#resetbutton').click(); }else{ }
	 * 
	 * }); });
	 */
	// 应付
	$("#addrow").click(function(){	
		var deliveryid =$("#delivery_id").val();
		if(deliveryid != "" && deliveryid != null){
			$.post('/deliveryOrderMilestone/addNewRow/'+deliveryid,function(data){
				console.log(data);
				paymenttable.fnSettings().sAjaxSource = "/deliveryOrderMilestone/accountPayable/"+deliveryid;
				paymenttable.fnDraw();
			});		
		}else{
			$.scojs_message('请先保存配送单', $.scojs_message.TYPE_ERROR);
		}
	});	
	// 应付修改
	$("#table_fin2").on('blur', 'input,select', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(value != "" && value != null){
			$.post('/deliveryOrderMilestone/updateDeliveryOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
				if(!data.success){
					$.scojs_message('修改失败', $.scojs_message.TYPE_ERROR);
				}
	    	},'json');
		}
	});
	
	//异步删除应付
	 $("#table_fin2").on('click', '.finItemdel', function(e){
		 var id = $(this).attr('code');
		  e.preventDefault();
		  $.post('/deliveryOrderMilestone/finItemdel/'+id,function(data){
               //保存成功后，刷新列表
               console.log(data);
               var deliveryid =$("#delivery_id").val();
               paymenttable.fnSettings().sAjaxSource = "/deliveryOrderMilestone/accountPayable/"+deliveryid;
               paymenttable.fnDraw();
           },'json');
	 });
	/*
	 * //应收 $("#addrow2").click(function(){ var deliveryid
	 * =$("#delivery_id").val();
	 * $.post('/deliveryOrderMilestone/addNewRow2/'+deliveryid,function(data){
	 * console.log(data); if(data.success){ paymenttable.fnDraw();
	 * //$('#fin_item2').modal('hide'); //$('#resetbutton2').click(); }else{ }
	 * }); });
	 */
		
    // 获取全国省份
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
    
    // 获取省份的城市
    $('#mbProvinceTo').on('change', function(){
			var inputStr = $(this).val();
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCityTo");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			},'json');
		});
    
    // 获取城市的区县
    $('#cmbCityTo').on('change', function(){
		var inputStr = $(this).val();
		$("#locationTo").val(inputStr);
		$.get('/serviceProvider/area', {id:inputStr}, function(data){
			var cmbArea =$("#cmbAreaTo");
			cmbArea.empty();
			cmbArea.append("<option>--请选择区(县)--</option>");
			for(var i = 0; i < data.length; i++)
			{
				cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
			}
		},'json');
	});
    
    $('#cmbAreaTo').on('change', function(){
		var inputStr = $(this).val();
		$("#locationTo").val(inputStr);
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
						//$("#locationTo").val(data[i].CODE);
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
						//$("#locationTo").val(data[i].CODE);
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
    
    $("#mbProvinceTo").on('change',function(){
		$("#locationTo").val($(this).val());
		$("#cmbAreaTo").get(0).selectedIndex=0;
		$("#cmbAreaTo").empty();
	});
	$("#cmbCityTo").on('change',function(){
		$("#locationTo").val($(this).val());
		
		
	});
	$("#cmbAreaTo").on('change',function(){
		$("#locationTo").val($(this).val());
	});


    //计费方式回显
	var departOrderId = $("#delivery_id").val();
	if(departOrderId != '' && departOrderId != null){
		var departOrderChargeType = $("#chargeTypeRadio").val();

		$("input[name='chargeType']").each(function(){
			if(departOrderChargeType == $(this).val()){
				//零担
				if(departOrderChargeType == "perCargo"){
					//隐藏车辆信息
					$("#carInfomation").hide();
					
					$(this).prop('checked', true);
					$("#ltl_price_type").show();
					var hibLtlUnitType = $("#hibLtlUnitType").val();
					$("input[value='"+hibLtlUnitType+"']").prop('checked', true);
				}else if(departOrderChargeType == "perCar"){
                    //显示车辆信息                   
                    $(this).prop('checked', true);
                    $("#car_type_div").show();
                    var departOrderCarType = $("#hiddenDeliveryOrderCarType").val();
                    $("#car_type").val(departOrderCarType);
                }else{
    				if(departOrderChargeType=="perUnit"){
    					$("#carInfomation").hide();
    				}else{
    					$("#carInfomation").show();
    				}
    				$(this).prop('checked', true);
    			}
			}
		});
	}else{
		var transferOrderChargeType = $("#transferOrderChargeType").val();
		$("input[name='chargeType']").each(function(){
			
			if(transferOrderChargeType == $(this).val()){
				//零担
				if(transferOrderChargeType == "perCargo"){
					$("#carInfomation").hide();
					$(this).prop('checked', true);
					$("#ltl_price_type").show();
					$("#optionsRadiosIn1").prop('checked', true);
					//隐藏车辆信息								
    			}else if(transferOrderChargeType == "perCar"){
                    $("#carInfomation").show();
                    //显示车辆信息                   
                    $(this).prop('checked', true);
                    $("#car_type_div").show();
                }else{
    				if(transferOrderChargeType=="perUnit"){
    					$("#carInfomation").hide();
    				}else{
    					$("#carInfomation").show();
    				}
    				
    				$(this).prop('checked', true);
    			}
			}
		});
	}

     $("input[name='chargeType']").click(function(){
    	 //等于零担的时候
        if($('input[name="chargeType"]:checked').val()==='perCargo'){
            $('#ltl_price_type').show();
            $("#carInfomation").hide();
            $("#car_type_div").hide();
        }else if($('input[name="chargeType"]:checked').val()==='perCar'){
            $("#carInfomation").show();
            //显示车辆信息                   
            $(this).prop('checked', true);
            $("#car_type_div").show();
            $('#ltl_price_type').hide();
        }else{
            $('#ltl_price_type').hide();
            $("#car_type_div").hide();
            //计费方式为计件的时候
            if($('input[name="chargeType"]:checked').val()==='perUnit'){
            	$("#carInfomation").hide();
            }else{
            	$("#carInfomation").show();
            }
        }
     });
	
	
	
	$('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#order_delivery_stamp').trigger('keyup');
    });	
	
	$('#datetimepicker1').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#client_order_stamp').trigger('keyup');
    });	
	
	$('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#business_stamp').trigger('keyup');
    });	
		
		
	/*//回显初始地
	var locationFrom = $("#locationForm").val();
	if(locationFrom == "")
		$("#hideLocationFrom").val();
	//var searchAllLocationFrom = function(locationFrom){
	if(locationFrom != ""){
    	$.get('/transferOrder/searchLocationFrom', {locationFrom:locationFrom}, function(data){
    		console.log(data);			
    		var provinceVal = data.PROVINCE;
    		var cityVal = data.CITY;
    		var districtVal = data.DISTRICT;
	        $.get('/serviceProvider/searchAllLocation', {province:provinceVal, city:cityVal}, function(data){	
		        //获取全国省份
	         	var province = $("#mbProvinceFrom");
	     		province.empty();
	     		province.append("<option>--请选择省份--</option>");
	     		for(var i = 0; i < data.provinceLocations.length; i++){
					if(data.provinceLocations[i].NAME == provinceVal){
						$("#locationForm").val(data.provinceLocations[i].CODE);
						province.append("<option value= "+data.provinceLocations[i].CODE+" selected='selected'>"+data.provinceLocations[i].NAME+"</option>");
					}else{
						province.append("<option value= "+data.provinceLocations[i].CODE+">"+data.provinceLocations[i].NAME+"</option>");						
					}
				}

				var cmbCity =$("#cmbCityFrom");
	     		cmbCity.empty();
				cmbCity.append("<option  value=''>--请选择城市--</option>");
				for(var i = 0; i < data.cityLocations.length; i++)
				{
					if(data.cityLocations[i].NAME == cityVal){
						$("#locationForm").val(data.cityLocations[i].CODE);
						cmbCity.append("<option value= "+data.cityLocations[i].CODE+" selected='selected'>"+data.cityLocations[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data.cityLocations[i].CODE+">"+data.cityLocations[i].NAME+"</option>");						
					}
				}
				
				if(data.districtLocations.length > 0){
    				var cmbArea =$("#cmbAreaFrom");
    				cmbArea.empty();
    				cmbArea.append("<option  value=''>--请选择区(县)--</option>");
    				for(var i = 0; i < data.districtLocations.length; i++)
    				{
    					if(data.districtLocations[i].NAME == districtVal){
    						$("#locationForm").val(data.districtLocations[i].CODE);
    						cmbArea.append("<option value= "+data.districtLocations[i].CODE+" selected='selected'>"+data.districtLocations[i].NAME+"</option>");
    					}else{
    						cmbArea.append("<option value= "+data.districtLocations[i].CODE+">"+data.districtLocations[i].NAME+"</option>");						
    					}
    				}
    			}else{
    				var cmbArea =$("#cmbArea");
    				cmbArea.empty();
    			}
	        },'json');
    	},'json');
  	};*/
		
	//获取全国省份
    $(function(){
     	var province = $("#mbProvinceFrom");
     	$.post('/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
			var hideProvince = $("#hideProvinceFrom").val();
     		for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideProvince){
						province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
						//$("#locationForm").val(data[i].CODE);
					}else{
						province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     	},'json');
    });
    
    //获取省份的城市
    $('#mbProvinceFrom').on('change', function(){
		var inputStr = $(this).val();
		$("#locationForm").val(inputStr);
		$.get('/serviceProvider/city', {id:inputStr}, function(data){
			var cmbCity =$("#cmbCityFrom");
			cmbCity.empty();
			cmbCity.append("<option>--请选择城市--</option>");
			for(var i = 0; i < data.length; i++)
			{
				cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
			}
		},'json');
	});
    //获取城市的区县
    $('#cmbCityFrom').on('change', function(){
		var inputStr = $(this).val();
		$("#locationForm").val(inputStr);
		$.get('/serviceProvider/area', {id:inputStr}, function(data){
			var cmbArea =$("#cmbAreaFrom");
			cmbArea.empty();
			cmbArea.append("<option>--请选择区(县)--</option>");
			for(var i = 0; i < data.length; i++)
			{
				cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
			}
			
		},'json');
	});
    
    $('#cmbAreaFrom').on('change', function(){
		var inputStr = $(this).val();
		$("#locationForm").val(inputStr);
	});   
    // 回显城市
    var hideProvince = $("#hideProvinceFrom").val();
    $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
		if(data.length > 0){
			var cmbCity =$("#cmbCityFrom");
			cmbCity.empty();
			cmbCity.append("<option>--请选择城市--</option>");
			var hideCity = $("#hideCityFrom").val();
			for(var i = 0; i < data.length; i++)
			{
				if(data[i].NAME == hideCity){
					cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					$("#locationForm").val(data[i].CODE);
				}else{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			}
		}
	},'json');

    // 回显区
    var hideCity = $("#hideCityFrom").val();
    $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
		if(data.length > 0){
			var cmbArea =$("#cmbAreaFrom");
			cmbArea.empty();
			cmbArea.append("<option>--请选择区(县)--</option>");
			var hideDistrict = $("#hideDistrictFrom").val();
			for(var i = 0; i < data.length; i++)
			{
				if(data[i].NAME == hideDistrict){
					cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					$("#locationForm").val(data[i].CODE);
				}else{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			}
		}
	},'json');
    
    /*// 配送排车单应付datatable
	var paymenttable=$('#table_fin3').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, // 不需要默认的搜索框
        // "sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": false,
        "bLengthChange":false,
        //"sAjaxSource": "/deliveryOrderMilestone/accountPayablePlan/"+deliveryid,
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
			        	if(obj.aData.CREATE_NAME == 'system'){
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
			{"mDataProp":"STATUS","sClass": "status"},
			{"mDataProp":"REMARK",
                "fnRender": function(obj) {
                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                        return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
                    }else{
                    	 return "<input type='text' name='remark'>";
                    }
            }},  
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
    });*/
    
});
function getChargetype(){
	//判断修改后相应的计费方式修改
	var customer_id = $("#customer_id").val();
	var sp_id = $("#sp_id").val();
	if(customer_id != null && customer_id !="" && sp_id != null && sp_id !=""){
		//获取当前供应商客户的计费方式
		$.post("/serviceProvider/seachChargeType",{sp_id:sp_id,customer_id:customer_id},function(data){
			if(data.CHARGE_TYPE == null){
				//这里是当前客户和供应商没有数据维护的情况
				$("input[name='chargeType']").each(function(){
					if($(this).val() == 'perUnit'){
						$(this).prop('checked', true);
					}
				});
			}else{
				 
				$("input[name='chargeType']").each(function(){
					if($(this).val() == data.CHARGE_TYPE){
						$(this).prop('checked', true);
					}
				});
			}
		},'json');
	}
}
