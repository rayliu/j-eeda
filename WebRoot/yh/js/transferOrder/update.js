
$(document).ready(function() {
	$('#menu_transfer').addClass('active').find('ul').addClass('in');
    //from表单验证
	var validate = $('#transferOrderUpdateForm').validate({
        rules: {
        	customerMessage: {
            required: true
          },
			spMessage: {
            required: true
          }
        },
        messages : {	             
        	customerMessage : {required:  "请选择一个客户"}, 
        	spMessage : {required:  "请选择一个供应商"}, 
        }
    });
		
     // tooltip demo
     $('.tooltip-demo').tooltip({
       selector: "[data-toggle=tooltip]",
       container: "body"
     });
		
	//获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup', function(){
		var inputStr = $('#customerMessage').val();
		if(inputStr == ""){
			var pageCustomerName = $("#pageCustomerName");
			pageCustomerName.empty();
			var pageCustomerAddress = $("#pageCustomerAddress");
			pageCustomerAddress.empty();
			$('#customer_id').val($(this).attr(''));
		}
		$.get('/yh/transferOrder/searchCustomer', {input:inputStr}, function(data){
			console.log(data);
			var customerList =$("#customerList");
			customerList.empty();
			for(var i = 0; i < data.length; i++)
			{
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
				customerList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' location='"+data[i].LOCATION+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+" "+data[i].CONTACT_PERSON+" "+data[i].PHONE+"</a></li>");
			}
		},'json');
		$("#customerList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#customerList').show();
	});
	
	// 选中客户
	$('#customerList').on('click', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#customerMessage').val(message.substring(0, message.indexOf(" ")));
		$('#customer_id').val($(this).attr('partyId'));
		var location = $(this).attr('location');
		$('#hideLocationFrom').val(location);	
		$('#locationForm').val(location);		
		var pageCustomerName = $("#pageCustomerName");
		pageCustomerName.empty();
		var contact_person = $(this).attr('contact_person');
		if(contact_person == 'null'){
			contact_person = '';
		}
		pageCustomerName.append(contact_person+'&nbsp;');
		var phone = $(this).attr('phone');
		if(phone == 'null'){
			phone = '';
		}
		pageCustomerName.append(phone); 
		var pageCustomerAddress = $("#pageCustomerAddress");
		pageCustomerAddress.empty();
		var address = $(this).attr('address');
		if(address == 'null'){
			address = '';
		}
		pageCustomerAddress.append(address);

        var locationFrom = $('#hideLocationFrom').val();
        $.get('/yh/transferOrder/searchLocationFrom', {locationFrom:locationFrom}, function(data){
			console.log(data);
			$("#hideProvinceFrom").val(data.PROVINCE);
			$("#hideCityFrom").val(data.CITY);
			$("#hideDistrictFrom").val(data.DISTRICT);
			

	        //获取全国省份
	        $(function(){
	         	var province = $("#mbProvinceFrom");
	         	$.post('/yh/serviceProvider/province',function(data){
	         		province.empty();
	         		province.append("<option>--请选择省份--</option>");
	    			var hideProvince = $("#hideProvinceFrom").val();
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

	        var checkProvince= function(provinceFrom){
	        	if(provinceFrom == '广东省'){
					$("#customerProvince2").prop('checked', false);
					$("#customerProvince1").prop('checked', true);
				}else{
					$("#customerProvince1").prop('checked', false);    			
					$("#customerProvince2").prop('checked', true);    			
				}
	        };
	        
	        // 回显出发城市
	        var hideProvince = $("#hideProvinceFrom").val();
	        $.get('/yh/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
	    			if(data.length > 0){
	    				var cmbCity =$("#cmbCityFrom");
	    				cmbCity.empty();
	    				cmbCity.append("<option>--请选择城市--</option>");
	    				var hideCity = $("#hideCityFrom").val();
	    				for(var i = 0; i < data.length; i++)
	    				{
	    					if(data[i].NAME == hideCity){
	    						var district = $("#hideDistrictFrom").val();
	    						if(district == ''){
	    							$("#address").val($("#hideProvinceFrom").val() +" "+ $("#hideCityFrom").val());
	    							checkProvince($("#hideProvinceFrom").val());
	    							
	    						}else{
	    							$("#address").val($("#hideProvinceFrom").val() +" "+ $("#hideCityFrom").val() +" "+ $("#hideDistrictFrom").val());
	    							checkProvince($("#hideProvinceFrom").val());
	    						}
	    						cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
	    					}else{
	    						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
	    					}
	    				}
	    			}
	    		},'json');

	        // 回显区
	        var hideCity = $("#hideCityFrom").val();
	        $.get('/yh/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
	    			if(data.length > 0){
	    				var cmbArea =$("#cmbAreaFrom");
	    				cmbArea.empty();
	    				cmbArea.append("<option>--请选择区(县)--</option>");
	    				var hideDistrict = $("#hideDistrictFrom").val();
	    				for(var i = 0; i < data.length; i++)
	    				{
	    					if(data[i].NAME == hideDistrict){
	    						$("#address").val($("#hideProvinceFrom").val() +" "+ $("#hideCityFrom").val() +" "+ $("#hideDistrictFrom").val());
	    						cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
	    					}else{
	    						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
	    					}
	    				}
	    			}else{
	    				var cmbArea =$("#cmbAreaFrom");
	    				cmbArea.empty();
	    			}
	    		},'json');
	        
		},'json');
        
        $('#customerList').hide();
    }); 
	
	//获取供应商的list，选中信息在下方展示其他信息
	$('#spMessage').on('keyup', function(){
		var inputStr = $('#spMessage').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/yh/transferOrder/searchSp', {input:inputStr}, function(data){
			console.log(data);
			var spList =$("#spList");
			spList.empty();
			for(var i = 0; i < data.length; i++)
			{
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
				spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+" "+data[i].CONTACT_PERSON+" "+data[i].PHONE+"</a></li>");
			}
		},'json');

		$("#spList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#spList').show();
	});
	
	// 选中供应商
	$('#spList').on('click', '.fromLocationItem', function(e){
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
	
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveTransferOrderBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#transferOrderUpdateForm").valid()){
        	alert("请先保存运输单!");
	       	return false;
        }
		//异步向后台提交数据
        if($("#order_id").val() == ""){
	    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
				$("#driver_id").val(transferOrder.DRIVER_ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#departureConfirmationBtn").attr("disabled", false);
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	$("#style").show();	
				  	
	            	var order_id = $("#order_id").val();
				  	itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;
				  	itemDataTable.fnDraw();                
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }else{
        	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
				$("#driver_id").val(transferOrder.DRIVER_ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					if(transferOrder.STATUS == '已发车' || transferOrder.STATUS == '已入库' || transferOrder.STATUS == '已签收'){
						$("#departureConfirmationBtn").attr("disabled", true);						
					}else{
						$("#departureConfirmationBtn").attr("disabled", false);
					}
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	$("#style").show();	
				  	
	            	var order_id = $("#order_id").val();
				  	itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;
				  	itemDataTable.fnDraw();                
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }
	});

    //货品明细的table 编辑
    $("table.table tr td").bind("click", dataClick);
    function dataClick(e) {
        console.log(e);
        if (e.currentTarget.innerHTML != "") return;
        if(e.currentTarget.contentEditable != null){
            $(e.currentTarget).attr("contentEditable",true);
        }else{
            $(e.currentTarget).append("<input type='text' value="+e.currentTarget.innerHTML+">");
        }    
    }

    // 单击货品明细时,应列表显示所有的货品
    $("#transferOrderItemList").click(function(e){
        e.preventDefault();
    	// 切换到货品明细时,应先保存运输单
    	//提交前，校验数据
        if(!$("#transferOrderUpdateForm").valid()){
        	alert("请先保存运输单!");
	       	return false; 
        }

        if($("#order_id").val() == ""){
	    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
				$("#driver_id").val(transferOrder.DRIVER_ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	$("#style").show();	

	            	var order_id = $("#order_id").val();
	            	var productId = $("#productIdHidden").val();
				  	itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id+"&product_id="+productId;
				  	itemDataTable.fnDraw();                
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }else{
        	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
				$("#driver_id").val(transferOrder.DRIVER_ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					if(transferOrder.STATUS == '已发车' || transferOrder.STATUS == '已入库' || transferOrder.STATUS == '已签收'){
						$("#departureConfirmationBtn").attr("disabled", true);						
					}else{
						$("#departureConfirmationBtn").attr("disabled", false);
					}
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	$("#style").show();	
				  	
	            	var order_id = $("#order_id").val();
	            	var productId = $("#productIdHidden").val();
				  	itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id+"&product_id="+productId;
				  	itemDataTable.fnDraw();             
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }
    });	
    
	var order_id = $("#order_id").val();
	//datatable, 动态处理
    var itemDataTable = $('#itemTable').dataTable({
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "aoColumns": [  			            
            {"mDataProp":"ITEM_NO"},
            {"mDataProp":"AMOUNT"},       	
            {"mDataProp":"UNIT"},
            {"mDataProp":"REMARK"},
            {  
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {
                    return	"<a class='btn btn-success dateilEdit' code='?id="+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "单品编辑"+
                            "</a>"+
                    		"<a class='btn btn-success editItem' code='?item_id="+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger deleteItem' code='?item_id="+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ]      
    });	
    
    // 保存货品
    $("#transferOrderItemFormBtn").click(function(){
    	$.post('/yh/transferOrderItem/saveTransferOrderItem', $("#transferOrderItemForm").serialize(), function(data){
			if(data.ID > 0){
				//保存成功后，刷新列表
                console.log(data);
                if(data.ORDER_ID>0){
                	$("#transferOrderItemForm")[0].reset();
                	var order_id = $("#order_id").val();
                	$("#productIdHidden").val(data.ORDER_ID);
	                itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id+"&product_id="+data.PRODUCT_ID;                		
                	itemDataTable.fnDraw();
                }else{
                    alert('数据保存失败。');
                }
                $("#transferOrderItemForm")[0].reset();
				$('#myModal').modal('hide');
			}
		},'json');
    });
    
    // 当arrivalMode1为货品直送时则显示收货人的信息
    $("#arrivalModes").on('click', 'input', function(){
  	  console.log(this);
  	  var inputId  = $(this).attr('id');
	  if(inputId=='arrivalMode1'){
		 $("#contactInformation").show();
		 $("#warehousingConfirmBtn").attr("disabled", true);
		 var gateInSelect = $("#gateInSelect");
		 gateInSelect.empty();
		 $("#gateInSelect").hide();
		 $("#warehousingConfirmBtn").attr("disabled", true);
	  }else{
		 $("#contactInformation").hide();
		 $("#gateInSelect").show();
	  } 
  	});    
						
	// 发车确认
	$("#departureConfirmationBtn").click(function(){
		// 浏览器启动时,停到当前位置
		//debugger;
		$("#departureConfirmationBtn").attr("disabled", true);
		if($("#arrivalModeVal").val() == 'delivery'){
			$("#warehousingConfirmBtn").attr("disabled", true);
			$("#receiptBtn").attr("disabled", false); 
		}else{
			$("#warehousingConfirmBtn").attr("disabled", false);	
			$("#receiptBtn").attr("disabled", true); 	
		} 

		var order_id = $("#order_id").val();
		$.post('/yh/transferOrderMilestone/departureConfirmation',{order_id:order_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
	});
	
	$("input[name='arrivalMode']").each(
		function(){
			if($(this).attr('checked') == 'checked'){
				 $("#contactInformation").show();
				 return false; 
			}else{
				 $("#contactInformation").hide();
			}
		}			
	)
	
	// 保存单品信息
	$("#transferOrderItemDetailFormBtn").click(function(){
		$.post('/yh/transferOrderItemDetail/saveTransferOrderItemDetail', $("#transferOrderItemDetailForm").serialize(), function(transferOrderItemDetail){
			if(transferOrderItemDetail.ID > 0){
				$("#detailModal").modal('hide');
				$("#transferOrderItemDetailForm")[0].reset();
				var itemId = $("#item_id").val();
				var orderId = $("#order_id").val();
				detailDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItemDetail/transferOrderDetailList?item_id="+itemId;
				detailDataTable.fnDraw();
			}			
		});
	});
	  
	// 更新单品		  	    	
	$("#transferOrderItemDetailUpdateFormBtn").click(function(){
		$.post('/yh/transferOrderItemDetail/saveTransferOrderItemDetail', $("#transferOrderItemDetailUpdateForm").serialize(), function(data){
			if(data.ID > 0){
				// 模态框:修改单品明细
				$('#updateDetailModal').modal('hide');
				// 更新货品列表
				var itemId = $("#item_id").val();
				detailDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItemDetail/transferOrderDetailList?item_id="+itemId;
				detailDataTable.fnDraw();
			}
		},'json');
	});
				                       
	// 运输里程碑
	$("#transferOrderMilestoneList").click(function(e){
		e.preventDefault();
    	// 切换到货品明细时,应先保存运输单
    	//提交前，校验数据
        if(!$("#transferOrderUpdateForm").valid()){
        	alert("请先保存运输单!");
	       	return false; 
        }
        
        if($("#order_id").val() == ""){
	    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
				$("#driver_id").val(transferOrder.DRIVER_ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	$("#style").show();	
				  	
				  	var order_id = $("#order_id").val();
					$.post('/yh/transferOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
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
        	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
				$("#driver_id").val(transferOrder.DRIVER_ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					if(transferOrder.STATUS == '已发车' || transferOrder.STATUS == '已入库' || transferOrder.STATUS == '已签收'){
						$("#departureConfirmationBtn").attr("disabled", true);						
					}else{
						$("#departureConfirmationBtn").attr("disabled", false);
					}
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//alert("运输单保存成功!");
				  	$("#style").show();	
				  	
				  	var order_id = $("#order_id").val();
					$.post('/yh/transferOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
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
        }
    	
		var order_id = $("#order_id").val();
		$.post('/yh/transferOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.empty();
			for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
			{
				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
			}
		},'json');
	});
	
	// 保存新里程碑
	$("#transferOrderMilestoneFormBtn").click(function(){
		$('#transfer_milestone_order_id').val($('#order_id').val());
		$.post('/yh/transferOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$('#transferOrderMilestone').modal('hide');
		$('#transferOrderMilestoneList').click();
	});
	
	// 回单签收
	$("#receiptBtn").click(function(){
		$("#receiptBtn").attr("disabled", true);
		var order_id = $("#order_id").val();
		$.post('/yh/transferOrderMilestone/receipt',{order_id:order_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
	});
	
	// 入库确认
	$("#warehousingConfirmBtn").click(function(){
		$("#warehousingConfirmBtn").attr("disabled", true);
		var order_id = $("#order_id").val();
		$.post('/yh/transferOrderMilestone/warehousingConfirm',{order_id:order_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
	});
	
	// 回显货品属性
	$("input[name='cargoNature']").each(function(){
		if($("#cargoNatureRadio").val() == $(this).val()){
			$(this).attr('checked', true);
		}
	});
	
	// 回显提货方式
	$("input[name='pickupMode']").each(function(){
		if($("#pickupModeRadio").val() == $(this).val()){
			$(this).attr('checked', true);
		}
	});
	
	// 回显到达方式
	$("input[name='arrivalMode']").each(function(){
		if($("#arrivalModeRadio").val() == $(this).val()){
			$(this).attr('checked', true);			
			if($(this).val() == 'gateIn'){
				$("#gateInSelect").show();
				$("#contactInformation").hide();
			}
		}
	});
	
	// 回显省内省外客户
	$("input[name='customerProvince']").each(function(){
		if($("#customerProvinceRadio").val() == $(this).val()){
			$(this).attr('checked', true);			
		}
	});

	var item_id = $("#item_id").val();
	//datatable, 动态处理
    var detailDataTable = $('#detailTable').dataTable({
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/yh/transferOrderItemDetail/transferOrderDetailList?item_id="+item_id,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "aoColumns": [  			            
            {"mDataProp":"SERIAL_NO"},
            {"mDataProp":"ITEM_NAME"},       	
            {"mDataProp":"VOLUME"},
            {"mDataProp":"WEIGHT"},
            {"mDataProp":"CONTACT_PERSON",
            	"fnRender": function(obj) {
            			return obj.aData.CONTACT_PERSON+"<br/>"+obj.aData.PHONE+"<br/>"+obj.aData.ADDRESS;
            		}},
            {"mDataProp":"REMARK"},
            {  
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {
                    return	"<a class='btn btn-success editDetail' code='?item_id="+obj.aData.ID+"&notify_party_id="+obj.aData.NOTIFY_PARTY_ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger deleteDetail' code='?item_id="+obj.aData.ID+"&notify_party_id="+obj.aData.NOTIFY_PARTY_ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
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
	
	// 编辑货品
	$("#itemTable").on('click', '.editItem', function(e){
		var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		$("#item_id").val(itemId);
		
  	    $("#transfer_order_item_id").val(itemId);
  	    $.post('/yh/transferOrderItem/getTransferOrderItem', 'transfer_order_item_id='+itemId, function(data){
  	    	// 编辑时回显数据
  	    	$("#transfer_order_id").val(data.transferOrderItem.ORDER_ID);
  	    	$("#transferOrderItemId").val(data.transferOrderItem.ID);
  	    	$("#productId").val(data.transferOrderItem.PRODUCT_ID);
  	    	$("#itemNameMessage").val(data.product.ITEM_NAME);
  	 		$("#itemNoMessage").val(data.product.ITEM_NO);
	  	 	$("#size").val(data.product.SIZE);
	  	 	$("#width").val(data.product.WIDTH);
	  	 	$("#unit").val(data.product.UNIT); 	
	  	 	$("#volume").val(data.product.VOLUME);
	  	 	$("#weight").val(data.product.WEIGHT);
	  	 	$("#height").val(data.product.HEIGHT);
	  	 	$("#remark").val(data.product.ITEM_DESC);
	  	 	$("#amount").val(data.transferOrderItem.AMOUNT);
  	    	// 模态框:修改货品明细
  	    	$('#myModal').modal('show');	
		},'json');
	});
	
	// 删除货品
	$("#itemTable").on('click', '.deleteItem', function(e){
		var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		$("#item_id").val(itemId);
		$.post('/yh/transferOrderItem/deleteTransferOrderItem', 'transfer_order_item_id='+itemId, function(data){
		},'json');
		$("#transferOrderItemDateil").hide();
		// 更新货品列表
		var order_id = $("#order_id").val();
		var product_id = $("#productIdHidden").val();
		itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id+"&product_id="+product_id;
	  	itemDataTable.fnDraw(); 	  	
	});	
	
	// 是否货损
	$("input[name='detail_is_damage']").click(function(){
		if($(this).val() == 'true'){
			$("#isDamageMessage").show();
		}else{
			$("#isDamageMessage").hide();
		}
	});	
	
	// 是否货损
	$("input[name='update_detail_is_damage']").click(function(){
		if($(this).val() == 'true'){
			$("#isDamageMessageUpdate").show();
		}else{
			$("#isDamageMessageUpdate").hide();
		}
	});
	
	// 删除单品
	$("#detailTable").on('click', '.deleteDetail', function(e){
		var code = $(this).attr('code');
		var detail = code.substring(0,code.indexOf('&'));
		var detailId = detail.substring(detail.indexOf('=')+1);
		var notifyParty = code.substring(code.indexOf('&')+1);
		var notifyPartyId = notifyParty.substring(notifyParty.indexOf('=')+1);
		$.post('/yh/transferOrderItemDetail/deleteTransferOrderItemDetail', {detail_id:detailId,notify_party_id:notifyPartyId}, function(data){
		},'json');
		// 更新货品列表
		var itemId = $("#item_id").val();
		detailDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItemDetail/transferOrderDetailList?item_id="+itemId;
		detailDataTable.fnDraw();
	});	
	
	// 编辑单品
	$("#detailTable").on('click', '.editDetail', function(e){
		var code = $(this).attr('code');
		var detail = code.substring(0,code.indexOf('&'));
		var detailId = detail.substring(detail.indexOf('=')+1);
		var notifyParty = code.substring(code.indexOf('&')+1);
		var notifyPartyId = notifyParty.substring(notifyParty.indexOf('=')+1);
		var itemId = $("item_id").val();
  	    $.post('/yh/transferOrderItemDetail/getTransferOrderItemDetail', {detail_id:detailId,notify_party_id:notifyPartyId}, function(data){
  	    	// 编辑时回显数据
  	    	$("#update_detail_transfer_order_id").val(data.transferOrderItemDetail.ORDER_ID);
  	    	$("#update_detail_transfer_order_item_id").val(data.transferOrderItemDetail.ITEM_ID);
  	    	$("#detail_transfer_order_item_detail_id").val(data.transferOrderItemDetail.ID);
  	    	$("#detail_notify_party_id").val(notifyPartyId);  	    	
  	    	
  	    	$("#update_serial_no").val(data.transferOrderItemDetail.SERIAL_NO);
  	    	$("#update_detail_item_name").val(data.transferOrderItemDetail.ITEM_NAME);
  	    	$("#update_detail_volume").val(data.transferOrderItemDetail.VOLUME);
  	    	$("#update_detail_weight").val(data.transferOrderItemDetail.WEIGHT);
  	    	$("#update_detail_remark").val(data.transferOrderItemDetail.REMARK);
  	    	$("#update_detail_contact_person").val(data.contact.CONTACT_PERSON);
  	    	$("#update_detail_phone").val(data.contact.PHONE);
  	    	$("#update_detail_address").val(data.contact.ADDRESS);
  	    	$("#update_detail_is_damage").val(data.transferOrderItemDetail.IS_DAMAGE);
  	    	
  	    	var isDamage = data.transferOrderItemDetail.IS_DAMAGE;
  	    	$("input[name='update_detail_is_damage']").each(function(){
  	  		if($(this).val() == isDamage+''){
  	  			$(this).attr('checked', true);
  	  			if($(this).val() == 'true'){
  	  				$("#isDamageMessageUpdate").show();
  	  			}else{
  	  				$("#isDamageMessageUpdate").hide();
  	  			}
  	  		}
  	  	});
  	    	$("#update_detail_estimate_damage_amount").val(data.transferOrderItemDetail.ESTIMATE_DAMAGE_AMOUNT);
  	    	$("#update_detail_damage_revenue").val(data.transferOrderItemDetail.DAMAGE_REVENUE);
  	    	$("#update_detail_damage_payment").val(data.transferOrderItemDetail.DAMAGE_PAYMENT);
  	    	$("#update_detail_damage_remark").val(data.transferOrderItemDetail.DAMAGE_REMARK);
		},'json');
  		// 模态框:修改货品明细
		$('#updateDetailModal').modal('show');	
	});
	
	// 清空单品表单
	$("#transferOrderItemDetailUpdateFormCancel").click(function(){
		$("#transferOrderItemDetailUpdateForm")[0].reset();
	});


    //获取全国省份
    $(function(){
     	var province = $("#mbProvince");
     	$.post('/yh/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
				var hideProvince = $("#hideProvince").val();
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
     $('#mbProvince').on('change', function(){
     	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			$.get('/yh/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCity");
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
     $('#cmbCity').on('change', function(){
     	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			var code = $("#notify_location").val(inputStr);
			$.get('/yh/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				toLocationList.show();
			},'json');
		});
     
     $('#cmbArea').on('change', function(){
     	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			var code = $("#notify_location").val(inputStr);
		});         

     // 回显城市
     var hideProvince = $("#hideProvince").val();
     $.get('/yh/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
			if(data.length > 0){
				var cmbCity =$("#cmbCity");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				var hideCity = $("#hideCity").val();
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
     var hideCity = $("#hideCity").val();
     $.get('/yh/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
			if(data.length > 0){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				var hideDistrict = $("#hideDistrict").val();
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
    
    //获取全国省份
    $(function(){
     	var province = $("#mbProvinceFrom");
     	$.post('/yh/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
				var hideProvince = $("#hideProvinceFrom").val();
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
    $('#mbProvinceFrom').on('change', function(){
			var inputStr = $(this).val();
			$.get('/yh/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCityFrom");
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
    $('#cmbCityFrom').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationForm").val(inputStr);
			$.get('/yh/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbAreaFrom");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				toLocationList.show();
			},'json');
		});
    
    $('#cmbAreaFrom').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationForm").val(inputStr);
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
    var hideProvince = $("#hideProvinceFrom").val();
    $.get('/yh/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
			if(data.length > 0){
				var cmbCity =$("#cmbCityFrom");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				var hideCity = $("#hideCityFrom").val();
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
    var hideCity = $("#hideCityFrom").val();
    $.get('/yh/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
			if(data.length > 0){
				var cmbArea =$("#cmbAreaFrom");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				var hideDistrict = $("#hideDistrictFrom").val();
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
    
     // 获取所有仓库
	 $.post('/yh/transferOrder/searchAllWarehouse',function(data){
		 if(data.length > 0){
			 var gateInSelect = $("#gateInSelect");
			 gateInSelect.empty();
			 var hideWarehouseId = $("#hideWarehouseId").val();
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideWarehouseId){
					 gateInSelect.append("<option class='form-control' value='"+data[i].ID+"' selected='selected'>"+data[i].WAREHOUSE_NAME+"</option>");					 
				 }else{
					 gateInSelect.append("<option class='form-control' value='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</option>");
				 }
			 }
		 }
	 },'json');	 

	 // 获取所有城市
	 $.post('/yh/transferOrder/searchAllOffice',function(data){
		 if(data.length > 0){
			 var officeSelect = $("#officeSelect");
			 officeSelect.empty();
			 var hideOfficeId = $("#hideOfficeId").val();
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideOfficeId){
					 officeSelect.append("<option class='form-control' value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");
				 }else{
					 officeSelect.append("<option class='form-control' value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");					 
				 }
			 }
		 }
	 },'json');
	 
	 // 回显订单类型
	 $("input[name='orderType']").each(function(){
		if($("#orderTypeRadio").val() == $(this).val()){
			$(this).attr('checked', true);
		}
	 });

	 // 列出所有的司机
	 $('#driverMessage').on('keyup', function(){
  		var inputStr = $('#driverMessage').val();
  		if(inputStr == ""){
  			$('#driver_phone').val($(this).attr(""));
  		}
  		$.get('/yh/transferOrder/searchAllDriver', {input:inputStr}, function(data){
  			console.log(data);
  			var driverList = $("#driverList");
  			driverList.empty();
  			for(var i = 0; i < data.length; i++)
  			{
  				driverList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', > "+data[i].CONTACT_PERSON+" "+data[i].PHONE+"</a></li>");
  			}
  		},'json');
  		
  		$("#driverList").css({ 
          	left:$(this).position().left+"px", 
          	top:$(this).position().top+32+"px" 
          }); 
          $('#driverList').show();
	 });
	  	
  	 // 选中司机
  	 $('#driverList').on('click', '.fromLocationItem', function(e){	
  		   $("#driver_id").val($(this).attr('partyId'));
	  	   $('#driverMessage').val($(this).attr('CONTACT_PERSON'));
	  	   $('#driver_phone').val($(this).attr('phone'));
	       $('#driverList').hide();   
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
	
  	//获取货品的序列号list，选中信息在下方展示其他信息
 	$('#itemNoMessage').on('keyup', function(){
 		var inputStr = $('#itemNoMessage').val();
 		$.get('/yh/transferOrder/searchItemNo', {input:inputStr}, function(data){
 			console.log(data);
 			var itemNoList =$("#itemNoList");
 			itemNoList.empty();
 			for(var i = 0; i < data.length; i++)
 			{
 				var item_no = data[i].ITEM_NO;
 				if(item_no == null){
 					item_no = '';
 				}
 				itemNoList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' cid='"+data[i].CATEGORY_ID+"' item_name='"+data[i].ITEM_NAME+"' size='"+data[i].SIZE+"' height='"+data[i].HEIGHT+"' width='"+data[i].WIDTH+"' unit='"+data[i].UNIT+"' volume='"+data[i].VOLUME+"' weight='"+data[i].WEIGHT+"', item_desc='"+data[i].ITEM_DESC+"', >"+data[i].ITEM_NO+"</a></li>");
 			}
 		},'json');		
         $("#itemNoList").css({ 
         	left:$(this).position().left+"px", 
         	top:$(this).position().top+32+"px" 
         }); 
         $('#itemNoList').show();        
 	});
 	
 	// 选中序列号
 	$('#itemNoList').on('click', '.fromLocationItem', function(e){
 		$("#itemNoMessage").val($(this).text());
 		if($(this).attr('item_name') == 'null'){
 			$("#item_name").val('');
 		}else{
 			$("#itemNameMessage").val($(this).attr('item_name'));
 		}
 		if($(this).attr('size') == 'null'){
 			$("#size").val('');
 		}else{
 			$("#size").val($(this).attr('size'));
 		}
 		if($(this).attr('width') == 'null'){
 			$("#width").val('');
 		}else{
 			$("#width").val($(this).attr('width'));
 		}
 		if($(this).attr('unit') == 'null'){
 			$("#unit").val('');
 		}else{
 			$("#unit").val($(this).attr('unit')); 			
 		}
 		if($(this).attr('volume') == 'null'){
 			$("volume").val('');
 		}else{
 			$("#volume").val($(this).attr('volume'));
 		}
 		if($(this).attr('weight') == 'null'){
 			$("weight").val('');
 		}else{
 			$("#weight").val($(this).attr('weight'));
 		}
 		if($(this).attr('height') == 'null'){
 			$("height").val('');
 		}else{
 			$("#height").val($(this).attr('height'));
 		}
 		if($(this).attr('item_desc') == 'null'){
 			$("remark").val('');
 		}else{
 			$("#remark").val($(this).attr('item_desc'));
 		}
 		$("#productId").val($(this).attr('id'));
        $('#itemNoList').hide();
     }); 
 	
 	//获取货品的名称list，选中信息在下方展示其他信息
 	$('#itemNameMessage').on('keyup', function(){
 		var inputStr = $('#itemNameMessage').val();
 		$.get('/yh/transferOrder/searchItemName', {input:inputStr}, function(data){
 			console.log(data);
 			var itemNameList =$("#itemNameList");
 			itemNameList.empty();
 			for(var i = 0; i < data.length; i++)
 			{
 				var item_name = data[i].ITEM_NAME;
 				if(item_name == null){
 					item_name = '';
 				}
 				itemNameList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' cid='"+data[i].CATEGORY_ID+"' item_no='"+data[i].ITEM_NO+"' size='"+data[i].SIZE+"' height='"+data[i].HEIGHT+"' width='"+data[i].WIDTH+"' unit='"+data[i].UNIT+"' volume='"+data[i].VOLUME+"' weight='"+data[i].WEIGHT+"', item_desc='"+data[i].ITEM_DESC+"', >"+data[i].ITEM_NAME+"</a></li>");
 			}
 		},'json');		
 		$("#itemNameList").css({ 
 			left:$(this).position().left+"px", 
 			top:$(this).position().top+32+"px" 
 		}); 
 		$('#itemNameList').show();        
 	});
 	
 	// 选中产品名
 	$('#itemNameList').on('click', '.fromLocationItem', function(e){
 		$("#itemNameMessage").val($(this).text());
 		if($(this).attr('item_no') == 'null'){
 			$("#item_no").val('');
 		}else{
 			$("#itemNoMessage").val($(this).attr('item_no'));
 		}
 		if($(this).attr('size') == 'null'){
 			$("#size").val('');
 		}else{
 			$("#size").val($(this).attr('size'));
 		}
 		if($(this).attr('width') == 'null'){
 			$("#width").val('');
 		}else{
 			$("#width").val($(this).attr('width'));
 		}
 		if($(this).attr('unit') == 'null'){
 			$("#unit").val('');
 		}else{
 			$("#unit").val($(this).attr('unit')); 			
 		}
 		if($(this).attr('volume') == 'null'){
 			$("volume").val('');
 		}else{
 			$("#volume").val($(this).attr('volume'));
 		}
 		if($(this).attr('weight') == 'null'){
 			$("weight").val('');
 		}else{
 			$("#weight").val($(this).attr('weight'));
 		}
 		if($(this).attr('height') == 'null'){
 			$("height").val('');
 		}else{
 			$("#height").val($(this).attr('height'));
 		}
 		if($(this).attr('item_desc') == 'null'){
 			$("remark").val('');
 		}else{
 			$("#remark").val($(this).attr('item_desc'));
 		}
 		$("#productId").val($(this).attr('id'));
 		$('#itemNameList').hide();
 	});  	
});