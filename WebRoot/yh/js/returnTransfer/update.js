
$(document).ready(function() {
	if(Transfer.orderNo){
        document.title = Transfer.orderNo +' | '+document.title;
    }
	
	$('#checkQrCode').hover(
		function(){//in
		$('#qrcodeCanvas').show();
		},function(){//out
		$('#qrcodeCanvas').hide();
	});
		
	$('#qrcodeCanvas').qrcode({
		width: 120,
		height: 120,
		text	: 'http://'+window.location.host+'/wx/fileUpload/'+Transfer.orderNo //'http://'+window.location.host+'
	});	
	
	$('#menu_returnTransfer').addClass('active').find('ul').addClass('in');

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

	var chargeType = $("#chargeTypeRadio").val();
	
	$("input[name='chargeType']").each(function(){
		if(chargeType == $(this).val()){
			$(this).prop('checked', true);
		}
	});
	//新建状态去除出库运输单选项
	
	if($("#order_id").val() == "" || $("#order_id").val() == null){
		$("#gateOute").hide();
	}else{
		$("#gateOute").show();
	}
	//客户计费方式回显
	var transferOrderId = $("#order_id").val();
	if(transferOrderId != '' && transferOrderId != null){
		var customerChargeType = $("#customerChargeType").val();
		$("input[name='chargeType']").each(function(){
			if(chargeType == $(this).val()){
				//零担
				if(chargeType == "perCargo"){
					//隐藏车辆信息
					$("#carInfomation").hide();					
					$(this).prop('checked', true);
					$("#ltl_price_type").show();
					var hibLtlUnitType = $("#hibLtlUnitType").val();
					$("input[value='"+hibLtlUnitType+"']").prop('checked', true);
				}else if(chargeType == "perCar"){
                    //显示车辆信息                   
                    $(this).prop('checked', true);
                    $("#car_type_div").show();
                    var departOrderCarType = $("#hiddenOrderCarType").val();
                    $("#car_type").val(departOrderCarType);
                }else{
    				if(chargeType=="perUnit"){
    					$("#carInfomation").hide();
    				}else{
    					$("#carInfomation").show();
    				}
    				$(this).prop('checked', true);
    			}
			}
		});
	}
	
    //from表单验证
	var validate = $('#transferOrderUpdateForm').validate({
        rules: {
        	customerMessage: {required: true},
        	planning_time: {required: true},
        	arrival_time: {required: true},
        	officeSelect: {required:true},
        	cmbCityFrom: {required:true},
        	cmbCityTo: {required:true}
        },
        messages : {	             
        	customerMessage : {required:  "请选择一个客户"},
        	officeSelect: {required: "运作网点不能为空"},
        	cmbCityFrom: {required:"始发城市不能为空"},
        	cmbCityTo: {required:"目的地城市不能为空"}
        }
    });
		
     // tooltip demo
     $('.tooltip-demo').tooltip({
       selector: "[data-toggle=tooltip]",
       container: "body"
     });
		
	//获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup click', function(){
		var inputStr = $('#customerMessage').val();
		$("label[name = 'errorMessage']").empty().remove();
		if(inputStr == ""){
			var pageCustomerName = $("#pageCustomerName");
			pageCustomerName.empty();
			var pageCustomerAddress = $("#pageCustomerAddress");
			pageCustomerAddress.empty();
			$('#customer_id').val($(this).attr(''));
			
		}
		
		$.get('/transferOrder/searchPartCustomer', {input:inputStr}, function(data){
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
				customerList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' payment='"+data[i].PAYMENT+"' partyId='"+data[i].PID+"' location='"+data[i].LOCATION+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');
		$("#customerList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#customerList').show();
        
	});

 	// 没选中客户，焦点离开，隐藏列表
	$('#customerMessage').on('blur', function(){
		//自动填充数据
		var inputStr = $('#customerMessage').val();
		$("label[name = 'errorMessage']").empty().remove();
		if(inputStr == "" && inputStr == null){
			var pageCustomerName = $("#pageCustomerName");
			pageCustomerName.empty();
			var pageCustomerAddress = $("#pageCustomerAddress");
			pageCustomerAddress.empty();
			$('#customer_id').val($(this).attr(''));
		}else if($('#customer_id').val() == ""){
			$.get('/transferOrder/searchPartCustomer', {input:inputStr}, function(data){
				if(data.length == 0){
					$('#customerMessage').after("<label  name ='errorMessage' for='customerMessage' class='error'>当前客户没有维护，请" +
							"<a href='/customer/add'>维护客户</a></label>");
				}
				if(data.length ==1){
					
					$('#customerMessage').val(data[0].COMPANY_NAME);
					$('#customer_id').val(data[0].PID);
					$('#customerId').val(data[0].PID);
					$('#hideLocationFrom').val(data[0].LOCATION);	
					$('#locationForm').val(data[0].LOCATION);		
					var pageCustomerName = $("#pageCustomerName");
					pageCustomerName.empty();
					var contact_person = data[0].CONTACT_PERSON;
					if(contact_person == 'null'){
						contact_person = '';
					}
					pageCustomerName.append(contact_person+'&nbsp;');
					var phone = data[0].PHONE;
					if(phone == 'null'){
						phone = '';
					}
					pageCustomerName.append(phone); 
					var pageCustomerAddress = $("#pageCustomerAddress");
					pageCustomerAddress.empty();
					var address = data[0].ADDRESS;
					if(address == 'null'){
						address = '';
					}
					pageCustomerAddress.append(address);
					
					var chargeType =data[0].CHARGE_TYPE;
					//等于零担的时候
			        if(chargeType==='perCargo'){
			            $('#ltl_price_type').show();
			            $("#carInfomation").hide();
			            $("#car_type_div").hide();
			        }else if(chargeType==='perCar'){
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
					
					searchAllLocation();
				}
			},'json');
		}
		
		
		getChargetype();
		
		
 		$('#customerList').hide();
 	});

	//当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
	$('#customerList').on('blur', function(){
 		$('#customerList').hide();
 	});

	$('#customerList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
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
    
    var searchAllLocation = function(){
    	var locationFrom = $('#hideLocationFrom').val();
    	$.get('/transferOrder/searchLocationFrom', {locationFrom:locationFrom}, function(data){
    					
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
						province.append("<option value= "+data.provinceLocations[i].CODE+" selected='selected'>"+data.provinceLocations[i].NAME+"</option>");
					}else{
						province.append("<option value= "+data.provinceLocations[i].CODE+">"+data.provinceLocations[i].NAME+"</option>");						
					}
				}

				var cmbCity =$("#cmbCityFrom");
	     		cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.cityLocations.length; i++)
				{
					if(data.cityLocations[i].NAME == cityVal){
						if(districtVal == undefined){
							$("#address").val(provinceVal +" "+ cityVal);
							checkProvince(provinceVal);
							
						}else{
							$("#address").val(provinceVal +" "+ cityVal +" "+ districtVal);
							checkProvince(provinceVal);
						}
						cmbCity.append("<option value= "+data.cityLocations[i].CODE+" selected='selected'>"+data.cityLocations[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data.cityLocations[i].CODE+">"+data.cityLocations[i].NAME+"</option>");						
					}
				}
				
				if(data.districtLocations.length > 0){
    				var cmbArea =$("#cmbAreaFrom");
    				cmbArea.empty();
    				cmbArea.append("<option>--请选择区(县)--</option>");
    				for(var i = 0; i < data.districtLocations.length; i++)
    				{
    					if(data.districtLocations[i].NAME == districtVal){
    						$("#address").val(provinceVal +" "+ cityVal +" "+ districtVal);
    						cmbArea.append("<option value= "+data.districtLocations[i].CODE+" selected='selected'>"+data.districtLocations[i].NAME+"</option>");
    					}else{
    						cmbArea.append("<option value= "+data.districtLocations[i].CODE+">"+data.districtLocations[i].NAME+"</option>");						
    					}
    				}
    			}else{
    				var cmbArea =$("#cmbAreaFrom");
    				cmbArea.empty();
    			}
	        },'json');
    	},'json');
    };
    
	// 选中客户
	$('#customerList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#customerMessage').val(message.substring(0, message.indexOf(" ")));
		$('#customer_id').val($(this).attr('partyId'));
		$('#customerId').val($(this).attr('partyId'));
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
		
		var chargeType = $(this).attr('chargeType');
		// 回显计费方式
		$("input[name='chargeType']").each(function(){
			if(chargeType == $(this).val()){
				$(this).prop('checked', true);
			}
		});

		//等于零担的时候
        if(chargeType==='perCargo'){
            $('#ltl_price_type').show();
            $("#carInfomation").hide();
            $("#car_type_div").hide();
        }else if(chargeType==='perCar'){
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
		
		searchAllLocation();
		
        $('#customerList').hide();
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
		$.get('/serviceProvider/searchSp', {input:inputStr}, function(data){
			//console.log(data);
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
				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
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
        
        //回显供应商付款方式
        /*var chargeType = $(this).attr('chargeType');
		$("input[name='chargeType2']").each(function(){
			if(chargeType == $(this).val()){
				$(this).prop('checked', true);
			}
		});*/
        /*if($("#chargeTypeRadio2").val() == null || $("#chargeTypeRadio2").val() == ""){*/
			getChargetype();
		/*}*/
    });
	
	 //回显供应商计费方式
    var chargeTypeRadio2 = $("#chargeTypeRadio2").val();
	$("input[name='chargeType2']").each(function(){
		if(chargeTypeRadio2 == $(this).val()){
			$(this).prop('checked', true);
		}
	});
	
	/*--------------------------------------------------------------------*/
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	/*--------------------------------------------------------------------*/
	
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveTransferOrderBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#transferOrderUpdateForm").valid()){
	       	return false;
        }
        $("#saveTransferOrderBtn").attr("disabled", true);
		//异步向后台提交数据
        if($("#order_id").val() == ""){
        	//console.log($("#transferOrderUpdateForm").serialize());
	    	$.post('/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//$("#style").show();	
					$("#saveTransferOrderBtn").attr("disabled", false);
					$("#showOrderNo").text(transferOrder.ORDER_NO);
					contactUrl("edit?id",transferOrder.ID);
				  	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				  	$("#printBtn").attr('disabled',false);
				  	
	            	var order_id = $("#order_id").val();
				  	itemDataTable.fnSettings().sAjaxSource = "/transferOrderItem/transferOrderItemList?order_id="+order_id;
				  	itemDataTable.fnDraw(); 
			        
			        //location.href = "/transferOrder";               
				}else{
					$.scojs_message('数据保存失败', $.scojs_message.TYPE_ERROR);
				}
			},'json');
        }else{
        	$.post('/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//$("#style").show();	
					$("#saveTransferOrderBtn").attr("disabled", false);
				  	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				  	$("#printBtn").attr('disabled',false);
	            	var order_id = $("#order_id").val();
				  	itemDataTable.fnSettings().sAjaxSource = "/transferOrderItem/transferOrderItemList?order_id="+order_id;
				  	itemDataTable.fnDraw();
			        
			        //location.href = "/transferOrder";                
				}else{
					$.scojs_message('数据保存失败', $.scojs_message.TYPE_ERROR);
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
    	if($("#transferOrderStatus").val() == '新建' || $("#transferOrderStatus").val() == ''){
	    	e.preventDefault();
	    	// 切换到货品明细时,应先保存运输单
	    	//提交前，校验数据
	   
	        if(!$("#transferOrderUpdateForm").valid()){
	        	alert("请先保存运输单!");
		       	return false; 
	        }
	        var bool =false;
	        if("chargeCheckOrderbasic" == parentId){
	        	bool = true;
	        }
	        
	        if($("#order_id").val() == ""){
		    	$.post('/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
					$("#transfer_order_id").val(transferOrder.ID);
					$("#update_transfer_order_id").val(transferOrder.ID);
					$("#order_id").val(transferOrder.ID);
					$("#transfer_milestone_order_id").val(transferOrder.ID);
					$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
					//$("#driver_id").val(transferOrder.DRIVER_ID);
					$("#id").val(transferOrder.ID);
					if(transferOrder.ID>0){
						$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
						$("#showOrderNo").text(transferOrder.ORDER_NO);
					  	//$("#style").show();
	
						if(bool){
							$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
						}
		            	var order_id = $("#order_id").val();
					  	itemDataTable.fnSettings().sAjaxSource = "/transferOrderItem/transferOrderItemList?order_id="+order_id;
					  	itemDataTable.fnDraw();             
					}else{
						alert('数据保存失败。');
					}
				},'json');
	        }else{
	        	$.post('/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
					$("#transfer_order_id").val(transferOrder.ID);
					$("#update_transfer_order_id").val(transferOrder.ID);
					$("#order_id").val(transferOrder.ID);
					$("#transfer_milestone_order_id").val(transferOrder.ID);
					$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
					//$("#driver_id").val(transferOrder.DRIVER_ID);
					$("#id").val(transferOrder.ID);
					if(transferOrder.ID>0){
						
						$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
					  	//$("#style").show();
					
						if(bool){
							$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
						}
						
		            	var order_id = $("#order_id").val();
					  	itemDataTable.fnSettings().sAjaxSource = "/transferOrderItem/transferOrderItemList?order_id="+order_id;
					  	itemDataTable.fnDraw();             
					}else{
						alert('数据保存失败。');
					}
				},'json');
	        }
    	}
    	parentId = e.target.getAttribute("id");
    });	
    
    var parentId = "chargeCheckOrderbasic";
    $("#chargeCheckOrderbasic").click(function(e){
    	/*if("transferOrderArap" == parentId ||"transferOrderItemList" == parentId){
    		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
    	}*/
    
    	parentId = e.target.getAttribute("id");
   
    });
    
    $.post('/transferOrder/searchAllUnit',function(data){
   	 if(data.length > 0){
   		 var unitOptions = $("#unitOptions");
   		 unitOptions.empty();
   		 unitOptions.append("<option ></option>");
   		 for(var i=0; i<data.length; i++){
   			unitOptions.append("<option value='"+data[i].NAME+"'>"+data[i].NAME+"</option>");	
   		 }
   		
   	 }
    },'json');
    
    
	var order_id = $("#order_id").val();
	//datatable, 动态处理
    var itemDataTable = $('#itemTable').dataTable({
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "bLengthChange":false,
        "sAjaxSource": "/transferOrderItem/transferOrderItemList?order_id="+order_id,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [  			            
            {
            	"mDataProp":"ITEM_NO",            	
            	"sWidth": "80px",
            	"sClass": "item_no",
            	"fnRender": function(obj) {
            		if(obj.aData.ITEM_NO==null)
            			obj.aData.ITEM_NO='';
            		var inputBox = "<input type='text' name='item_no' value='"+obj.aData.ITEM_NO+"' placeholder='至少输入两个字符查询'>";
        			return inputBox;
                }
        	},
            {
            	"mDataProp":"ITEM_NAME",
            	"sWidth": "180px",
            	"sClass": "item_name",
            	"fnRender": function(obj) {
            		var inputBox = "";
            		if(obj.aData.PROD_ID != null){
            			if(obj.aData.ITEM_NAME != null)
            				inputBox = obj.aData.ITEM_NAME;
            			else
            				inputBox = '';
            		}else{
	            		if(obj.aData.ITEM_NAME==null)
	            			obj.aData.ITEM_NAME='';
	            		inputBox = "<input type='text' name='item_name' value='"+obj.aData.ITEM_NAME+"'>";
            		}
        			return inputBox;
                }
            },
            {
            	"mDataProp":"SIZE",            	
            	"sWidth": "50px",
            	"sClass": "size",
            	"fnRender": function(obj) {
            		var inputBox = "";
            		if(obj.aData.PROD_ID != null){
            			if(obj.aData.SIZE != null)
            				inputBox = obj.aData.SIZE + '';
            			else
            				inputBox = '';
            		}else{
	            		if(obj.aData.SIZE==null)
	            			obj.aData.SIZE='';
	            		inputBox = "<input type='text' name='size' style='width:60px;' value='"+obj.aData.SIZE+"'>";
            		}
            		return inputBox;
                }
        	},
            {
            	"mDataProp":"WIDTH",
            	"sWidth": "50px",
            	"sClass": "width",
            	"fnRender": function(obj) {
            		var inputBox = "";
            		if(obj.aData.PROD_ID != null){
            			if(obj.aData.WIDTH != null)
            				inputBox = obj.aData.WIDTH + '';
            			else
            				inputBox = '';
            		}else{
	            		if(obj.aData.WIDTH==null)
	            			obj.aData.WIDTH='';
	            		inputBox = "<input type='text' name='width' style='width:60px;' value='"+obj.aData.WIDTH+"'>";;
            		}
            		return inputBox;
                }
            },
            {
            	"mDataProp":"HEIGHT",            	
            	"sWidth": "50px",
            	"sClass": "height",
            	"fnRender": function(obj) {
            		var inputBox = "";
            		if(obj.aData.PROD_ID != null){
            			if(obj.aData.HEIGHT != null)
            				inputBox = obj.aData.HEIGHT + '';
            			else
            				inputBox = '';
            		}else{
	            		if(obj.aData.HEIGHT==null)
	            			obj.aData.HEIGHT='';
	            		inputBox = "<input type='text' name='height' style='width:60px;' value='"+obj.aData.HEIGHT+"'>";
            		}
            		return inputBox;
                }
        	}, 
            {
            	"mDataProp":"WEIGHT",
            	"sWidth": "60px",
            	"sClass": "weight",
            	"fnRender": function(obj) {
            		var inputBox = "";
            		if(obj.aData.PROD_ID != null){
            			if(obj.aData.WEIGHT != null)
            				inputBox = obj.aData.WEIGHT + '';
            			else
            				inputBox = '';
            		}else{
	            		if(obj.aData.WEIGHT==null)
	            			obj.aData.WEIGHT='';
	            		inputBox = "<input type='text' name='weight' style='width:70px;' value='"+obj.aData.WEIGHT+"'>";
            		}
        			return inputBox;
                }
            },
        	{
            	"mDataProp":"AMOUNT",
            	"sWidth": "50px",
            	"sClass": "amount",
            	"fnRender": function(obj) {
            		if(obj.aData.AMOUNT==null)
            			obj.aData.AMOUNT='';
            		var inputBox = "<input type='text' name='amount' style='width:60px;' value='"+obj.aData.AMOUNT+"'>";
        			return inputBox;
                }
            }, 
            {
            	"mDataProp":"UNIT",
            	"sWidth": "50px",
            	"sClass": "unit",
            	"fnRender": function(obj) {
            		
            		if(obj.aData.PROD_ID !='' && obj.aData.PROD_ID != null){
            			if(obj.aData.UNIT != null)
            				inputBox = obj.aData.UNIT + '';
            			else
            				inputBox = '';
			        }else{
			        	var str="";
			        	$("#unitOptions").children().each(function(){
			        		if(obj.aData.UNIT == $(this).val()){
			        			str+="<option value='"+$(this).val()+"' selected=''>"+$(this).text()+"</option>";			        			
			        		}else{			        			
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			        	inputBox = "<select name='unit'>"+str+"</select>";
			        }
        			return inputBox;
                }
            },
            {
            	"mDataProp":null,
            	"sWidth": "80px",
            	"sClass": "sumWeight",
            	"fnRender": function(obj) {
            		var inputBox = "";
            		if(obj.aData.SUM_WEIGHT != null){
            			inputBox = "<input type='text' name='sum_weight' style='width:80px;' value='"+obj.aData.SUM_WEIGHT+"'>";
            		}else{
            			inputBox = "<input type='text' name='sum_weight' style='width:80px;' value=''>";
            		}
            		return inputBox;
                }
            },
            {
            	"mDataProp":"VOLUME",
            	"sWidth": "80px",
            	"sClass": "volume",
            	"fnRender": function(obj) {
            		var inputBox = "";
            		if(obj.aData.VOLUME != null){
            			inputBox = "<input type='text' name='volume' style='width:80px;' value='"+obj.aData.VOLUME+"'>";
            		}else{
            			inputBox = "<input type='text' name='volume' style='width:80px;' value=''>";
            		}
            		//var sumVolume = (obj.aData.SIZE / 1000 * obj.aData.WIDTH / 1000 * obj.aData.HEIGHT / 1000 * $(obj.aData.AMOUNT).val()).toFixed(2);
            		return inputBox;
                }
            },            
            {   
            	"mDataProp":"REMARK",
            	"fnRender": function(obj) {
            		if(obj.aData.REMARK==null)
            			obj.aData.REMARK='';
            		var inputBox = "<input type='text' style='width:360px;' name='remark' value='"+obj.aData.REMARK+"'>";
        			return inputBox;
                }
            },
            {  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                	if(Transfer.isUpdate){
	                    return	"<a class='btn btn-success btn-xs dateilEdit' code='?id="+obj.aData.ID+"' title='单品编辑'>"+
	                                "<i class='fa fa-edit fa-fw'></i>"+
	                            "</a> "+
	                            "<a class='btn btn-danger btn-xs deleteItem' code='?item_id="+obj.aData.ID+"' title='删除'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+
	                            "</a>";
                	}
                }
            }                         
        ]      
    });	

    // 计算体积
	var sumVolume = function(currentEle){
		$(currentEle).parent().children('.volume')[0].innerHTML = parseFloat($(currentEle).parent().children('.size')[0].innerHTML)/1000 * parseFloat($(currentEle).parent().children('.width')[0].innerHTML)/1000 * parseFloat($(currentEle).parent().children('.height')[0].innerHTML)/1000 * parseFloat($(currentEle).parent().children('.amount')[0].innerHTML);
	};
	
	// 计算总重量
	var sumWeight = function(currentEle){
		$(currentEle).parent().children('.sumWeight')[0].innerHTML = parseFloat($(currentEle).parent().children('.weight')[0].innerHTML) * parseFloat($(currentEle).parent().children('.amount')[0].innerHTML);
	};
    
	// 刷新单品列表
	var refreshDetailTable = function(){
		var orderId = $("#order_id").val();
		// 刷新单品列表
		detailDataTable.fnSettings().sAjaxSource = "/transferOrderItemDetail/transferOrderDetailList?orderId="+orderId;
		detailDataTable.fnDraw();
	};
	
	// 刷新货品列表
	var refreshItemTable = function(){
		var order_id = $("#order_id").val();
        itemDataTable.fnSettings().sAjaxSource = "/transferOrderItem/transferOrderItemList?order_id="+order_id;                		
    	itemDataTable.fnDraw();
	};

	//item_no
	$('#itemTable').on('click', 'input[name=item_no]', function(){
		var inputBox=$(this);
		inputBox.autocomplete({
	        source: function( request, response ) {
	        	if(inputBox.parent().parent()[0].cellIndex >1){//从第3列开始，不需要去后台查数据
		    		return;
		    	}
	            $.ajax({
	                url: "/transferOrder/searchItemNo",
	                dataType: "json",
	                data: {
	                    customerId: $('#customerId').val(),
	                    warehouseId:$("#gateOutSelect").val(),
	                    orderType:$("input[name='orderType']:checked").val(),
	                    input: request.term
	                },
	                success: function( data ) {
						var columnName = inputBox.parent().parent()[0].className;
						var itemNos =[];
		        		$("input[name=item_no]").each(function(){
		       				if($(this).val()!=null&&$(this).val()!=""){
		       					itemNos.push($(this).val());
		       				}
		    	   		});
		        		if(data.length>0){
		        			response($.map( data, function( data ) {
		                    	var complete="";
		                    	for(var i=0;i<itemNos.length;i++){
		                    		if(data.ITEM_NO == itemNos[i]){
		                    			complete = data.ITEM_NO;
		                    		}
		                    	}
		                    	if(complete != data.ITEM_NO){
		                    		return {
		 	                            label: '型号:'+data.ITEM_NO+' 名称:'+data.ITEM_NAME,
		 	                            value: columnName=='item_name'?data.ITEM_NAME:data.ITEM_NO,
		 	                            id: data.ID,
		 	                            item_no: data.ITEM_NO,
		 	                            item_name: data.ITEM_NAME
		 	                       }
		                    	}
		                    	
		                			
		                    }));
		        		}else{
		        			var d = new Array(1);
		        			if($("input[name='orderType']:checked").val() == "arrangementOrder"){
		        				d[0] = "当前仓库内客户没有此类产品库存";
		        			}else{
		        				d[0] = "当前客户没有维护此类产品";
		        			}
		        			
		        			response($.map(d, function( i ) {
		        				return {
	 	                            value:'警告:'+i,
	 	                            id: '',
	 	                            item_no: '',
	 	                            item_name: '',
	 	                            label: ''
	 	                       }
		                    }));
		        			
		        		}
	                    
	                }
	            });
	        },
        	select: function( event, ui ) {   
        		
        		//将选择的产品id先保存到数据库
        		ui.item.value='';
        		if(ui.item.id != ''){
        			var itemId = $(this).parent().parent()[0].id;
            		var productId = ui.item.id;
            		$.post('/transferOrderItem/saveTransferOrderItem', {transferOrderItemId:itemId,productId:productId},	function(data){   					
    					refreshItemTable();					
        			}, 'json');
            		
        		}
        		    
            },
        	minLength: 2
        });
	});


	$('#itemTable').on('blur', 'input,select', function(){
		var itemId = $(this).parent().parent()[0].id;
		var fieldName=$(this).attr("name");
		var value= $(this).val();
		var weight;
		var volume;
		/*if(filedName == 'amount'){
			if(/^\d+$/.test(value)){
				if($("input[name='orderType']:checked").val() == "arrangementOrder"){
					$.post('',{itemId:itemId},function(){
						
					},'json');
				}
			}else{
				$.scojs_message('数量只能是整数', $.scojs_message.TYPE_ERROR);
				return false;
			}
			
		}*/
		
		if(value != ''){
			$.ajax({  
	            type : "post",  
	            url : "/transferOrderItem/updateTransferOrderItem",  
	            data : {order_id: $("#order_id").val(), id: itemId, fieldName: fieldName, value: value},  
	            async : false,  
	            success : function(data){  
	            	weight = data.SUM_WEIGHT;
	            	volume = data.VOLUME;
	            }  
	        });
		}
		
		var amount = $(this).parent().parent().children('.amount').children().val();
		if(amount == ""){
			amount = 0;
		}
		$(this).parent().parent().children('.sumWeight').children().val(weight);
		$(this).parent().parent().children('.volume').children().val(volume);
	});
	
    // 保存货品
    $("#transferOrderItemFormBtn").click(function(){
    	$.post('/transferOrderItem/saveTransferOrderItem', $("#transferOrderItemForm").serialize(), function(data){
			if(data.ID > 0){
				//保存成功后，刷新列表
                
                if(data.ORDER_ID>0){
                	$("#transferOrderItemForm")[0].reset();
                	var order_id = $("#order_id").val();
	                itemDataTable.fnSettings().sAjaxSource = "/transferOrderItem/transferOrderItemList?order_id="+order_id;                		
                	itemDataTable.fnDraw();
                }else{
                    alert('数据保存失败。');
                }
                $("#transferOrderItemForm")[0].reset();
				$('#myModal').modal('hide');
			}
		},'json');
    });
    
    // 当cargoNature2为为普通货品是需要知道是否需要单品
    $("#cargoNatures").on('click', 'input', function(){
  	  var inputId  = $(this).attr('id');
	  if(inputId=='cargoNature2'){
		 $("#cargoNatureDetailSpan").show();
		 $("#addRevenueDiv").show();
		 $("#addCostDiv").show();
		 $("input[name='cargoNatureDetail']").each(function(){
			if($(this).val() == "cargoNatureDetailNo" && $(this).prop('checked')){
				$("#transferOrderItemDateil").hide(); 
			}
		 });
	  }else{
		  if(inputId=='cargoNature1'){
			   $("#cargoNatureDetailSpan").hide(); 
			   $("#addRevenueDiv").hide();
			   $("#addCostDiv").hide();
			   $("#transferOrderItemDateil").show(); 
		  }		  
	  }	  
  	}); 
    
    $("#cargoNatureDetailSpan").on('click', 'input', function(){
    	var inputId  = $(this).attr('id');
    	if(inputId=='cargoNatureDetail2'){
    		$("#transferOrderItemDateil").show();
    	}else{
    		$("#transferOrderItemDateil").hide(); 
    	}	  
    }); 

    
    // 点击退货类型radio事件
    $("#arrivalModes").on('click', 'input', function(){
    	var model  = $(this).val();
    	if(model=='deliveryToWarehouse'){
    		$("#contactInformation").hide();
    		$("#gateOutDiv").hide();
    		$("#gateInDiv").show();
    	}else if(model=='deliveryToFactory'){
    		$("#contactInformation").show();
    		$("#gateOutDiv").hide();
    		$("#gateInDiv").hide();
    	}else{
    		$("#contactInformation").hide();
    		$("#gateOutDiv").show();
    		$("#gateInDiv").hide();
    	}
    });    
    // 回显退货类型
	$("input[name='arrivalMode']").each(function(){
		var val = $(this).val();
		if($("#arrivalModeRadio").val() == val){
			console.log("OK");
			if(val=='deliveryToWarehouse'){
				$(this).attr('checked', true);
	    		$("#contactInformation").hide();
	    		$("#gateOutDiv").hide();
	    		$("#gateInDiv").show();
	    	}else if(val=='deliveryToFactory'){
	    		$(this).attr('checked', true);
	    		$("#contactInformation").show();
	    		$("#gateOutDiv").hide();
	    		$("#gateInDiv").hide();
	    	}else{
	    		$(this).attr('checked', true);
	    		$("#contactInformation").hide();
	    		$("#gateOutDiv").show();
	    		$("#gateInDiv").hide();
	    	}
		}else{
			if(val=='deliveryToWarehouse'){
				$(this).attr('checked', true);
	    		$("#contactInformation").hide();
	    		$("#gateOutDiv").hide();
	    		$("#gateInDiv").show();
			}
		}
	});					
	
	
	/*$("input[name='arrivalMode']").each(
		function(){
			if($(this).attr('checked') == 'checked'){
				 $("#contactInformation").show();
				 return false; 
			}else{
				 $("#contactInformation").hide();
			}
		}			
	);*/
	
	// 保存单品信息
	$("#transferOrderItemDetailFormBtn").click(function(){
		$.post('/transferOrderItemDetail/saveTransferOrderItemDetail', $("#transferOrderItemDetailForm").serialize(), function(transferOrderItemDetail){
			if(transferOrderItemDetail.ID > 0){
				$("#detailModal").modal('hide');
				$("#transferOrderItemDetailForm")[0].reset();
				var itemId = $("#item_id").val();
				var orderId = $("#order_id").val();
				// 刷新单品列表
				detailDataTable.fnSettings().sAjaxSource = "/transferOrderItemDetail/transferOrderDetailList?item_id="+itemId;
				detailDataTable.fnDraw();
				
				// 刷新货品列表
                itemDataTable.fnSettings().sAjaxSource = "/transferOrderItem/transferOrderItemList?order_id="+orderId;                		
            	itemDataTable.fnDraw();
			}			
		});
	});
	
	var transferOrderMilestone = function(){
	  	var order_id = $("#order_id").val();
		$.post('/transferOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
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
		if($("#transferOrderStatus").val() == '新建' || $("#transferOrderStatus").val() == ''){
			e.preventDefault();
	    	// 切换到货品明细时,应先保存运输单
	    	//提交前，校验数据
	        if(!$("#transferOrderUpdateForm").valid()){
	        	alert("请先保存运输单!");
		       	return false; 
	        }
	        var bool = false;
	        if("chargeCheckOrderbasic" == parentId){
	        	bool= true;
	        }

	        if($("#order_id").val() == ""){
		    	$.post('/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
					$("#transfer_order_id").val(transferOrder.ID);
					$("#update_transfer_order_id").val(transferOrder.ID);
					$("#order_id").val(transferOrder.ID);
					$("#transfer_milestone_order_id").val(transferOrder.ID);
					$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
					$("#id").val(transferOrder.ID);
					if(transferOrder.ID>0){
						$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
					  	transferOrderMilestone();
					  	if(bool){
					  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
					  	}
					}else{
						alert('数据保存失败。');
					}
				},'json');
	        }else{
	        	$.post('/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
					$("#transfer_order_id").val(transferOrder.ID);
					$("#update_transfer_order_id").val(transferOrder.ID);
					$("#order_id").val(transferOrder.ID);
					$("#transfer_milestone_order_id").val(transferOrder.ID);
					$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
					$("#id").val(transferOrder.ID);
					if(transferOrder.ID>0){
						$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
						
					  	transferOrderMilestone();  
					  	if(bool){
					  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
					  	}
					}else{
						alert('数据保存失败。');
					}
				},'json');
	        }
	        
		}
    	
		var order_id = $("#order_id").val();
		$.post('/transferOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.empty();
			for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
			{
				var str = data.usernames[j];
				if(str==null){
					str="";
				}
				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+str+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
			}
		},'json');
		parentId = e.target.getAttribute("id");
	});
	
	// 保存新里程碑
	$("#transferOrderMilestoneFormBtn").click(function(){
		$('#transfer_milestone_order_id').val($('#order_id').val());
		$.post('/transferOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$('#transferOrderMilestone').modal('hide');
		$('#transferOrderMilestoneList').click();
	});
	
	// 回显货品属性
	$("input[name='cargoNature']").each(function(){		
		if($("#cargoNatureRadio").val() != null && $("#cargoNatureRadio").val() != ''){
			if($("#cargoNatureRadio").val() == $(this).val()){
				$(this).attr('checked', true);
				if($("#cargoNatureRadio").val() == 'cargo'){
					$("#cargoNatureDetailSpan").show();
					$("#addCostDiv").show();
					$("#addRevenueDiv").show();
					$("input[name='cargoNatureDetail']").each(function(){
						if($(this).val() == $("#cargoNatureDetailRadio").val()){
							$(this).attr('checked', true);
							if($(this).val() == "cargoNatureDetailYes"){
								$("#transferOrderItemDateil").show(); 
							}else{
								$("#transferOrderItemDateil").hide(); 
							}
						}
					});
				}else{
					$("#transferOrderItemDateil").show(); 
				}
			}
		}else{
			if($(this).val() == 'ATM'){
				$("#transferOrderItemDateil").show(); 				
			}
		}
	});
	
	// 回显提货方式
	$("input[name='pickupMode']").each(function(){
		if($("#pickupModeRadio").val() == $(this).val()){
			$(this).attr('checked', true);
		}
	});
	
	
	
	// 回显运营方式
	$("input[name='operationType']").each(function(){
		if($("#operationTypeRadio").val() == $(this).val()){
			$(this).attr('checked', true);		
		}
	});
	
	// 回显付款方式
	$("input[name='chargeType']").each(function(){
		if($("#chargeTypeRadio").val() == $(this).val()){
			$(this).attr('checked', true);		
		}
	});

	var orderId = $("#order_id").val();
	//datatable, 动态处理
    var detailDataTable = $('#detailTable').dataTable({
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bLengthChange":false,
        "bServerSide": true,
        "sAjaxSource": "/transferOrderItemDetail/transferOrderDetailList?orderId="+orderId,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({'id': aData.ID, 'notify_party_id': aData.PID});
			return nRow;
		},
        "aoColumns": [
            {"mDataProp":"ID","sWidth": "80px" },
            {
            	"mDataProp":"SERIAL_NO",
        		"sWidth": "80px",
            	"sClass": "serial_no",
            	"fnRender": function(obj) {
                    if(obj.aData.SERIAL_NO!='' && obj.aData.SERIAL_NO != null){
                        return "<input type='text' style='width:120px;' name='serial_no' value='"+obj.aData.SERIAL_NO+"'>";
                    }else{
                    	 return "<input type='text' style='width:120px;' name='serial_no'>";
                    }
            }},
            {
            	"mDataProp":"SALES_ORDER_NO",
        		"sWidth": "80px",
            	"sClass": "sales_order_no",
            	"fnRender": function(obj) {
                    if(obj.aData.SALES_ORDER_NO!='' && obj.aData.SALES_ORDER_NO != null){
                        return "<input type='text' style='width:120px;' name='sales_order_no' value='"+obj.aData.SALES_ORDER_NO+"'>";
                    }else{
                    	 return "<input type='text' style='width:120px;' name='sales_order_no'>";
                    }
            }},
            {
            	"mDataProp":"ITEM_NO",
        		"sWidth": "150px",
            	"sClass": "item_no"            		
            },  
		    {
		    	"mDataProp":"ITEM_NAME",
		    	"sWidth": "180px",
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
            	"mDataProp":"PIECES",
            	"sWidth": "80px",
            	"sClass": "pieces",
            	"fnRender": function(obj) {
                    if(obj.aData.PIECES!='' && obj.aData.PIECES != null){
                        return "<input type='text' style='width:60px;' name='pieces' value='"+obj.aData.PIECES+"'>";
                    }else{
                    	 return "<input type='text' style='width:60px;' name='pieces'>";
                    }
            }},
            {
            	"mDataProp":"NOTIFY_PARTY_NAME",
        		"sWidth": "80px",
            	"sClass": "notify_party_name",
            	"fnRender": function(obj) {
                    if(obj.aData.NOTIFY_PARTY_NAME!='' && obj.aData.NOTIFY_PARTY_NAME != null){
                        return "<input type='text' name='notify_party_name' value='"+obj.aData.NOTIFY_PARTY_NAME+"'>";
                    }else{
                    	 return "<input type='text' name='notify_party_name'>";
                    }
            }},
            {
            	"mDataProp":"NOTIFY_PARTY_PHONE",
        		"sWidth": "80px",
            	"sClass": "notify_party_phone",
            	"fnRender": function(obj) {
                    if(obj.aData.NOTIFY_PARTY_PHONE!='' && obj.aData.NOTIFY_PARTY_PHONE != null){
                        return "<input type='text' name='notify_party_phone' value='"+obj.aData.NOTIFY_PARTY_PHONE+"'>";
                    }else{
                    	 return "<input type='text' name='notify_party_phone'>";
                    }
            }},
            {
            	"mDataProp":"NOTIFY_PARTY_COMPANY",
        		"sWidth": "80px",
            	"sClass": "notify_party_company",
            	"fnRender": function(obj) {
                    if(obj.aData.NOTIFY_PARTY_COMPANY !='' && obj.aData.NOTIFY_PARTY_COMPANY != null){
                        return "<input type='text' name='notify_party_company' value='"+obj.aData.NOTIFY_PARTY_COMPANY +"'>";
                    }else{
                    	 return "<input type='text' name='notify_party_company'>";
                    }
            }},
            {
            	"mDataProp":"BUSINESS_MANAGER",
        		"sWidth": "80px",
            	"sClass": "business_manager",
            	"fnRender": function(obj) {
                    if(obj.aData.BUSINESS_MANAGER !='' && obj.aData.BUSINESS_MANAGER != null){
                        return "<input type='text' name='business_manager' value='"+obj.aData.BUSINESS_MANAGER +"'>";
                    }else{
                    	 return "<input type='text' name='business_manager'>";
                    }
            }},
            
            {
            	"mDataProp":"SERVICE_TELEPHONE",
        		"sWidth": "80px",
            	"sClass": "service_telephone",
            	"fnRender": function(obj) {
                    if(obj.aData.SERVICE_TELEPHONE !='' && obj.aData.SERVICE_TELEPHONE != null){
                        return "<input type='text' name='service_telephone' value='"+obj.aData.SERVICE_TELEPHONE +"'>";
                    }else{
                    	 return "<input type='text' name='service_telephone'>";
                    }
            }},
            {
            	"mDataProp":"STATION_NAME",
        		"sWidth": "80px",
            	"sClass": "station_name",
            	"fnRender": function(obj) {
                    if(obj.aData.STATION_NAME !='' && obj.aData.STATION_NAME != null){
                        return "<input type='text' name='station_name' value='" + obj.aData.STATION_NAME +"'>";
                    }else{
                    	 return "<input type='text' name='station_name'>";
                    }
            }},
            {
            	"mDataProp":"RESPONSIBLE_PERSON",
        		"sWidth": "80px",
            	"sClass": "responsible_person",
            	"fnRender": function(obj) {
                    if(obj.aData.RESPONSIBLE_PERSON !='' && obj.aData.RESPONSIBLE_PERSON != null){
                        return "<input type='text' name='responsible_person' value='"+obj.aData.RESPONSIBLE_PERSON +"'>";
                    }else{
                    	 return "<input type='text' name='responsible_person'>";
                    }
            }},
            {
            	"mDataProp":"REMARK",
        		"sWidth": "80px",
            	"sClass": "remark",
            	"fnRender": function(obj) {
                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                        return "<input type='text' style='width:360px;' name='remark' value='"+obj.aData.REMARK+"'>";
                    }else{
                    	 return "<input type='text' style='width:360px;' name='remark'>";
                    }
            }},
            /*,
            {  
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger btn-xs deleteDetail' code='?item_id="+obj.aData.ID+"&notify_party_id="+obj.aData.NOTIFY_PARTY_ID+"'' title='删除'>"+
		                        "<i class='fa fa-trash-o fa-fw'></i>"+
		                    "</a>";
                }
            } */                        
        ]      
    });

	$("#detailTable").on('blur', 'input', function(e){
		e.preventDefault();
		var detailId = $(this).parent().parent().attr("id");
		var pId = $(this).parent().parent().attr("notify_party_id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(value != ""){
			$.post('/transferOrderItemDetail/saveTransferOrderItemDetailByField', {detailId: detailId, pId: pId, name: name, value: value}, function(data){
				if(data.success){
				}else{
					alert("修改失败!");
				}
	    	},'json');
		}
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
		
		detailDataTable.fnSettings().sAjaxSource = "/transferOrderItemDetail/transferOrderDetailList?item_id="+itemId;
		detailDataTable.fnDraw();  

		//detailDataTable.fnAddData({'SERIAL_NO':'test', 'ITEM_NAME':'test', 'VOLUME':'test','WEIGHT':'test', 'CONTACT_PERSON':'test', 'REMARK':'test'} );
		//detailDataTable.fnDraw(); 
	});
	
	// 编辑货品
	$("#itemTable").on('click', '.editItem', function(e){
		var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		$("#item_id").val(itemId);
		
  	    $.post('/transferOrderItem/getTransferOrderItem', 'transfer_order_item_id='+itemId, function(data){
  	    	// 编辑时回显数据
  	    	$("#transfer_order_id").val(data.transferOrderItem.ORDER_ID);
  	    	$("#transferOrderItemId").val(data.transferOrderItem.ID);
  	    	$("#productId").val(data.transferOrderItem.PRODUCT_ID);
  	    	if(data.product != null){
	  	    	$("#itemNameMessage").val(data.product.ITEM_NAME);
	  	 		$("#itemNoMessage").val(data.product.ITEM_NO);
		  	 	$("#size").val(data.product.SIZE);
		  	 	$("#width").val(data.product.WIDTH);
		  	 	$("#unit").val(data.product.UNIT); 	
		  	 	$("#volume").val(data.product.VOLUME);
		  	 	$("#weight").val(data.product.WEIGHT);
		  	 	$("#height").val(data.product.HEIGHT);
		  	 	$("#remark").val(data.product.ITEM_DESC);
  	    	}else{
  	    		$("#itemNameMessage").val(data.transferOrderItem.ITEM_NAME);
	  	 		$("#itemNoMessage").val(data.transferOrderItem.ITEM_NO);
		  	 	$("#size").val(data.transferOrderItem.SIZE);
		  	 	$("#width").val(data.transferOrderItem.WIDTH);
		  	 	$("#unit").val(data.transferOrderItem.UNIT); 	
		  	 	$("#volume").val(data.transferOrderItem.VOLUME);
		  	 	$("#weight").val(data.transferOrderItem.WEIGHT);
		  	 	$("#height").val(data.transferOrderItem.HEIGHT);
		  	 	$("#remark").val(data.transferOrderItem.ITEM_DESC);
  	    	}
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
		$.post('/transferOrderItem/deleteTransferOrderItem', 'transfer_order_item_id='+itemId, function(data){
		},'json');
		$("#transferOrderItemDateil").hide();
		// 更新货品列表
		var order_id = $("#order_id").val();
		itemDataTable.fnSettings().sAjaxSource = "/transferOrderItem/transferOrderItemList?order_id="+order_id;
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
	
	// 删除单品
	$("#detailTable").on('click', '.deleteDetail', function(e){
		var code = $(this).attr('code');
		var detail = code.substring(0,code.indexOf('&'));
		var detailId = detail.substring(detail.indexOf('=')+1);
		var notifyParty = code.substring(code.indexOf('&')+1);
		var notifyPartyId = notifyParty.substring(notifyParty.indexOf('=')+1);
		$.post('/transferOrderItemDetail/deleteTransferOrderItemDetail', {detail_id:detailId,notify_party_id:notifyPartyId}, function(data){
		},'json');
		// 更新单品列表
		var orderId = $("#order_id").val();
		detailDataTable.fnSettings().sAjaxSource = "/transferOrderItemDetail/transferOrderDetailList?orderId="+orderId;
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
  	    $.post('/transferOrderItemDetail/getTransferOrderItemDetail', {detail_id:detailId,notify_party_id:notifyPartyId}, function(data){
	  	    	// 编辑时回显数据
	  	    	$("#detail_transfer_order_id").val(data.transferOrderItemDetail.ORDER_ID);
	  	    	$("#detail_transfer_order_item_id").val(data.transferOrderItemDetail.ITEM_ID);
	  	    	$("#detail_transfer_order_item_detail_id").val(data.transferOrderItemDetail.ID);
	  	    	$("#detail_notify_party_id").val(notifyPartyId);  	    	
	  	    	
	  	    	$("#serial_no").val(data.transferOrderItemDetail.SERIAL_NO);
	  	    	$("#detail_item_name").val(data.transferOrderItemDetail.ITEM_NAME);
	  	    	$("#detail_volume").val(data.transferOrderItemDetail.VOLUME);
	  	    	$("#detail_weight").val(data.transferOrderItemDetail.WEIGHT);
	  	    	$("#detail_remark").val(data.transferOrderItemDetail.REMARK);
	  	    	$("#detail_contact_person").val(data.contact.CONTACT_PERSON);
	  	    	$("#detail_phone").val(data.contact.PHONE);
	  	    	$("#detail_address").val(data.contact.ADDRESS);
		},'json');
  		// 模态框:修改货品明细
		$('#detailModal').modal('show');	
	});
	
	// 清空单品表单
	$("#transferOrderItemDetailUpdateFormCancel").click(function(){
		$("#transferOrderItemDetailUpdateForm")[0].reset();
	});

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
     				
     				
					}else{
     				province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     		
     	},'json');
    });
    
    //获取省份的城市
    $('#mbProvinceFrom').on('change', function(){
			var inputStr = $(this).val();
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCityFrom");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				//toLocationList.show();
			},'json');
		});
    
    //获取城市的区县
    $('#cmbCityFrom').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationForm").val(inputStr);
			$.get('/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbAreaFrom");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				//toLocationList.show();
			},'json');
		});
    
    $('#cmbAreaFrom').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationForm").val(inputStr);
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
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCityTo");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				//toLocationList.show();
			},'json');
		});
    
    //获取城市的区县
    $('#cmbCityTo').on('change', function(){
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
				//toLocationList.show();
			},'json');
		});
    
    $('#cmbAreaTo').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
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
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
    

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
    //RDC仓库操作
	 $('#deliveryOfficeSelect').on('change', function(){ 
		 if($(this).val() != ""){
			 // 获取所有仓库
			 findWarehouseForOffice($(this).val());
		 }else{
			 $("#gateInSelect").empty();
		 }
	 });
	 
	 var findWarehouseForOffice = function(officeId){
		 $.post('/transferOrder/searchAllWarehouse', {officeId: officeId},function(data){
			 if(data.length > 0){
				 var gateInSelect = $("#gateInSelect");
				 gateInSelect.empty();
				 var hideWarehouseId = $("#hideWarehouseId").val();
				 for(var i=0; i<data.length; i++){
					 if(data[i].ID == hideWarehouseId){
						 gateInSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].WAREHOUSE_NAME+"</option>");
						 //$("#gateInSelect").val(data[i].ID);
					 }else{
						 gateInSelect.append("<option value='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</option>");
					 }
				 }
				 
				 if($("#gateInSelect").val() != "" && $("#gateInSelect").val() != null)
					 $("#gateInSelect").change();
				 
			 }else{
				 $("#gateInSelect").empty();
			 }
		 },'json');	
	 };
	 
	 var deliveryOfficeId = $("#hideDeliveryOfficeSelect").val();
	 if(deliveryOfficeId != null && deliveryOfficeId != ""){
		 findWarehouseForOffice(deliveryOfficeId);
	 }
	 
	 // 获取所有网点
	 $.post('/transferOrder/searchAllOffice',function(data){
		 if(data.length > 0){
			 //console.log(data);
			 var deliveryOfficeSelect = $("#deliveryOfficeSelect");
			 deliveryOfficeSelect.empty();
			 var hideDeliveryOfficeSelect = $("#hideDeliveryOfficeSelect").val();
			 deliveryOfficeSelect.append("<option ></option>");	
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideDeliveryOfficeSelect){
					 deliveryOfficeSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");
				 }else{
					 if(data[i].IS_STOP != true){
						 deliveryOfficeSelect.append("<option value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");					 
					 };
					
				 };
			 };
		 };
	 },'json');
	 // 获取用户拥有的网点
	 $.post('/transferOrder/searchPartOffice',function(data){
		 if(data.length > 0){
			 var officeSelect = $("#officeSelect");
			 officeSelect.empty();
			 var hideOfficeId = $("#hideOfficeId").val();
			 officeSelect.append("<option ></option>");	
			 for(var i=0; i<data.length; i++){
				 /*data[i].IS_STOP != true*/
				 if(data[i].ID == hideOfficeId){
					 officeSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");					 
				 }else{
					 if(data[i].IS_STOP != true){
						 officeSelect.append("<option value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");
					 };
					 
				 };
			 };
		 };
	 },'json');
	 
	//R出库仓库操作
	 $('#outOfficeSelect').on('change', function(){ 
		 if($(this).val() != ""){
			 // 获取所有仓库
			 findOutWarehouseForOffice($(this).val());
		 }else{
			 $("#gateOutSelect").empty();
		 }
	 });
	 
	 var findOutWarehouseForOffice = function(officeId){
		 $.post('/transferOrder/searchAllWarehouse', {officeId: officeId},function(data){
			 if(data.length > 0){
				 var gateOutSelect = $("#gateOutSelect");
				 gateOutSelect.empty();
				 var hideWarehouseId = $("#hideOutWarehouseId").val();
				 for(var i=0; i<data.length; i++){
					 if(data[i].ID == hideWarehouseId){
						 gateOutSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].WAREHOUSE_NAME+"</option>");
						 //$("#gateInSelect").val(data[i].ID);
					 }else{
						 gateOutSelect.append("<option value='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</option>");
					 }
				 }
				 
				 if($("#gateOutSelect").val() != "" && $("#gateOutSelect").val() != null)
					 $("#gateOutSelect").change();
				 
			 }else{
				 $("#gateOutSelect").empty();
			 }
		 },'json');	
	 };
	 
	 var hideOutOfficeSelect = $("#hideOutOfficeSelect").val();
	 if(hideOutOfficeSelect != null && hideOutOfficeSelect != ""){
		 findOutWarehouseForOffice(hideOutOfficeSelect);
	 }
	 $.post('/transferOrder/searchPartOffice',function(data){
		 if(data.length > 0){
			 var outOfficeSelect = $("#outOfficeSelect");
			 outOfficeSelect.empty();
			 var hideOfficeId = $("#hideOutOfficeSelect").val();
			 outOfficeSelect.append("<option ></option>");	
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideOfficeId){
					 outOfficeSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");					 
				 }else{
					 if(data[i].IS_STOP != true){
						 outOfficeSelect.append("<option value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");
					 };
					 
				 };
			 };
		 };
	 },'json');
	 
	 // 回显订单类型
	 $("input[name='orderType']").each(function(){
		if($("#orderTypeRadio").val() == $(this).val()){
			$(this).attr('checked', true);
			if($(this).val() == "arrangementOrder"){
				 $("#gateOutDiv").show();
			 }
		}
		
	 });
	 /*$("input[name='orderType']").click(function(){
		 if($(this).val() == "deliveryToWarehouse"){
			 $("#gateInDiv").show();
			 $("#gateOutDiv").hide();
			 $("#contactInformation").hide();
		 }else{
			 $("#gateOutDiv").hide();
			// $("#direct_model").show();
		 }
		 
	});*/
	 // 回显应付方式
	 $("input[name='payment']").each(function(){
		 if($("#paymentRadio").val() == $(this).val()){
			 $(this).attr('checked', true);
		 }
	 });

  	//获取货品的序列号list，选中信息在下方展示其他信息
 	$('#itemNoMessage').on('keyup click', function(){		
 		var inputStr = $('#itemNoMessage').val();
 		var customerId = $('#customerId').val();
 		$.get('/transferOrder/searchItemNo', {input:inputStr,customerId:customerId}, function(data){
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
 	$('#itemNoList').on('mousedown', '.fromLocationItem', function(e){
 		if($(this).text().indexOf("当前") >0){
 			$("#itemNoMessage").val('');
 		}else{
 			$("#itemNoMessage").val($(this).text());
 		}
 		
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

 	// 没选中序列号，焦点离开，隐藏列表
 	$('#itemNoMessage').on('blur', function(){
  		$('#itemNoList').hide();
  	});
 	
 	//获取货品的名称list，选中信息在下方展示其他信息
 	$('#itemNameMessage').on('keyup click', function(){
 		var inputStr = $('#itemNameMessage').val();
 		var customerId = $('#customerId').val();
 		$.get('/transferOrder/searchItemName', {input:inputStr,customerId:customerId}, function(data){
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
 	$('#itemNameList').on('mousedown', '.fromLocationItem', function(e){
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

 	// 没选中序列号，焦点离开，隐藏列表
 	$('#itemNameMessage').on('blur', function(){
  		$('#itemNameList').hide();
  	});
 	
 	// 清除上一次留下的ID
 	$("#editTransferOrderItem").click(function(e){
 		e.preventDefault();
 		var orderId = $("#order_id").val();
 		$.post('/transferOrderItem/addNewRow', {orderId:orderId}, function(data){
 			itemDataTable.fnDraw(); 
 		});

 		$("#transferOrderItemId").val("");
 		$("#productId").val("");
 	});
 	
    // 计算货品体积
    $('#height, #width, #size').on('keyup click', function() { 
    	var height = $('#height').val();
    	var width = $('#width').val();
    	var size = $('#size').val();
    	$("#volume").val((height/1000)*(width/1000)*(size/1000));
    });
    
    // 只有是新建状态才能编辑
    if($("#transferOrderStatus").val() == '新建' || $("#transferOrderStatus").val() == ''){
    	$("#saveTransferOrderBtn").attr('disabled', false);
    	
    }else{
    	$("#saveTransferOrderBtn").attr('disabled', true); 
    	
    }
    if($("#order_id").val() != '' && $("#order_id").val() != null){
    	$("#printBtn").attr('disabled', false);
    }else{
    	$("#printBtn").attr('disabled', true);
    }
    
    
    // 查看所有单品
    $("#findAllDetailBtn").click(function(){
    	var orderId = $("#order_id").val();
		// 刷新单品列表
		detailDataTable.fnSettings().sAjaxSource = "/transferOrderItemDetail/transferOrderDetailList?orderId="+orderId;
		detailDataTable.fnDraw();
    });

    // 单击应收应付
    $("#transferOrderArap").click(function(e){
    	if($("#transferOrderStatus").val() == '新建' || $("#transferOrderStatus").val() == ''){
	    	e.preventDefault();
	    	// 切换到货品明细时,应先保存运输单
	    	//提交前，校验数据
	        if(!$("#transferOrderUpdateForm").valid()){
	        	alert("请先保存运输单!");
		       	return false; 
	        }
	        var bool = false;
	        if("chargeCheckOrderbasic" == parentId){
	        	bool=true;
	        }
	        
	    	$.post('/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
				$("#id").val(transferOrder.ID);
			
				
				if(transferOrder.ID>0){
					
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//$("#style").show();
					
					if(bool){
						$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
					}
					
	            	var order_id = $("#order_id").val();
	            	receipttable.fnSettings().sAjaxSource = "/transferOrder/accountReceivable/"+order_id;
	            	receipttable.fnDraw();       

				}else{
					alert('数据保存失败。');
				}
			},'json');
	    	
        }
    	parentId = e.target.getAttribute("id");
 
    });	

    //应收应付
    var order_id =$("#order_id").val();
	//应收datatable
	var receipttable =$('#table_fin').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "bLengthChange":false,
        "sAjaxSource":"/transferOrder/accountReceivable/"+order_id,
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
			        	if(obj.aData.CREATE_NAME == 'system'){
			        		return obj.aData.NAME;
			        	}else{
			        		return "<select name='fin_item_id'>"+str+"</select>";
			        	}
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
			{"mDataProp":"TRANSFERORDERNO","sClass": "remark"},
			{"mDataProp":"DELIVERYORDERNO","sClass": "remark"},
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
	//异步删除应收
	$("#table_fin").on('click', '.finItemdel', function(e){
		 var id = $(this).attr('code');
		  e.preventDefault();
		$.post('/transferOrder/finItemdel/'+id,function(data){
            //保存成功后，刷新列表
            console.log(data);
            	receipttable.fnDraw();
        },'text');
	});
	 
	//应付datatable
	var order_id =$("#order_id").val();
	var paymenttable=$('#table_fin2').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "sAjaxSource": "/transferOrder/accountPayable/"+order_id,
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
		$.post('/transferOrder/finItemdel/'+id,function(data){
               //保存成功后，刷新列表
               //console.log(data);
               paymenttable.fnDraw();
           },'text');
	});
	
	//应付
	$("#item_fin_save").click(function(){
	    var order_id =$("#order_id").val();
		$.post('/transferOrder/receiptSave/'+order_id, $("#fin_form").serialize(), function(data){
			//console.log(data);
			if(data.success){
				receipttable.fnDraw();
				$('#fin_item').modal('hide');
				$('#resetbutton').click();
			}else{
				
			}		
		});		
	});	
	
	//应付
	$("#addrow").click(function(e){	
		e.preventDefault();
	    var order_id =$("#order_id").val();
		$.post('/transferOrder/addNewRow/'+order_id,function(data){
			if(data[0] != null){
				paymenttable.fnSettings().sAjaxSource = "/transferOrder/accountPayable/"+order_id;
				paymenttable.fnDraw(); 
				//paymenttable.fnDraw();
				//$('#fin_item2').modal('hide');
				//$('#resetbutton2').click();
			}else{
				alert("请到基础模块维护应付条目！");
			}
		});		
	});	
	//应付修改 TODO 
	$("#table_fin2").on('blur', 'input', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		$.post('/transferOrder/updateTransferOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
			if(data != null){
				paymenttable.fnSettings().sAjaxSource = "/transferOrder/accountPayable/"+order_id;
				paymenttable.fnDraw(); 
			}else{
				$.scojs_message('修改失败!', $.scojs_message.TYPE_ERROR);
			}
    	},'json');
	});
	$("#table_fin2").on('change', 'select', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		$.post('/transferOrder/updateTransferOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
			if(data != null){
				paymenttable.fnSettings().sAjaxSource = "/transferOrder/accountPayable/"+order_id;
				paymenttable.fnDraw(); 
			}else{
				$.scojs_message('修改失败!', $.scojs_message.TYPE_ERROR);
			}
    	},'json');
	});
	//应收
	$("#addrow2").click(function(e){	
		e.preventDefault();
	    var order_id =$("#order_id").val();
		$.post('/transferOrder/addNewRow2/'+order_id,function(data){
			if(data[0] != null){
				receipttable.fnSettings().sAjaxSource = "/transferOrder/accountReceivable/"+order_id;
            	receipttable.fnDraw();  
				
			}else{
				alert("请到基础模块维护应收条目！");
			}
		});		
	});	
	//应收修改
	$("#table_fin").on('blur', 'input', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		$.post('/transferOrder/updateTransferOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
			if(data.ID > 0){
			}else{
				alert("修改失败!");
			}
    	},'json');
	});
	$("#table_fin").on('change', 'select', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		$.post('/transferOrder/updateTransferOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
			if(data.ID > 0){
				$.scojs_message('更新成功!', $.scojs_message.TYPE_OK);
			}else{
				//alert("修改失败!");
				$.scojs_message('修改失败!', $.scojs_message.TYPE_ERROR);
			}
    	},'json');
	});

	// 选中仓库触发事件
	$("#gateInSelect").change(function(){
    	$.post('/transferOrder/selectWarehouse', {warehouseId:$(this).val()}, function(data){  
		   
			var hideProvince = data.location.PROVINCE;
			var hideCity = data.location.CITY;
			var hideDistrict = data.location.DISTRICT;
			
			$("#locationTo").val(data.warehouse.LOCATION);
		    //获取全国省份
		    $(function(){
		     	var province = $("#mbProvinceTo");
		     	$.post('/serviceProvider/province',function(data){
		     		province.append("<option>--请选择省份--</option>");
		     		for(var i = 0; i < data.length; i++){
						if(data[i].NAME == hideProvince){
							province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");		
						}else{
							province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
						}
					}    		     		
		     	},'json');
		    });
		    
		    // 回显城市
		    $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
				if(data.length > 0){
					var cmbCity =$("#cmbCityTo");
					cmbCity.empty();
					cmbCity.append("<option>--请选择城市--</option>");
					for(var i = 0; i < data.length; i++){
						if(data[i].NAME == hideCity){
							cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
						}else{
							cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
						}
					}
				}
			},'json');

		    // 回显区
		    $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
				if(data.length > 0){
					var cmbArea =$("#cmbAreaTo");
					cmbArea.empty();
					cmbArea.append("<option>--请选择区(县)--</option>");
					for(var i = 0; i < data.length; i++){
						if(data[i].NAME == hideDistrict){
							cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
						}else{
							cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
						}
					}
				}
			},'json');
    	}, 'json');
	});

    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#planning_time').trigger('keyup');
    });


    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#arrival_time').trigger('keyup');
    });
    $("#printBtn").on('click',function(){
    	var cargoNature = $("input[name='cargoNature']:checked").val();
    	if(cargoNature == 'cargo'){
    		$("#muban").show();
    		$("#putong").show();
    		$("#biaozhun").hide();
    		$("#pdfSign_n").hide();
    	}else{
    		var customer = $("#customerMessage").val();
        	if(customer=="江苏国光信息产业股份有限公司"){
        		$("#muban").show();
        		$("#putong").hide();
        		$("#biaozhun").show();
        		$("#pdfSign_n").show();
        	}else{
        		$("#printBtn").removeAttr('data-target');
        		$.scojs_message('对不起，当前客户没有定义单据打印格式', $.scojs_message.TYPE_ERROR);
        	}
    	}
    	
    });
    $("#btnOK").on('click',function(){
    	var signNO = $("input[name='sign']:checked").val();
    	/*var shzm = $("input[name='shmb']:checked").val();*/
    	//打印签收单
    	var customer = $("#customerMessage").val();
    	var order_no = $("#showOrderNo").text();
    	var pdf_sign = $("input[name='pdfSign']:checked").val();
    	var pdf_muban = signNO + "_" + pdf_sign;
    	var cargoNature = $("input[name='cargoNature']:checked").val();
    	if(cargoNature == 'cargo'){
    		$.post('/report/printSignCargo', {order_no:order_no,sign:pdf_muban}, function(data){
    			openData(data);
        	});   		
        	$("#close").click();
    	}else{
    		if(customer=="江苏国光信息产业股份有限公司"){
        		$.post('/report/printSign', {order_no:order_no,sign:pdf_muban}, function(data){
        			openData(data);
            	});   		
            	$("#close").click();
        	}else{
        		$.scojs_message('对不起，当前客户没有定义单据打印格式', $.scojs_message.TYPE_ERROR);
        	}
    	}
    });
    
    //应收,应付checkbox
    $("#revenueCheckBox,#costCheckBox").click(function(e){
    	var order_id = $("#order_id").val();
    	var name = $(this).attr("name");
    	var revenue = $(this).prop('checked');
		if(order_id != ""){
			$.post('/transferOrder/updateTransferOrderRevenue', {order_id:order_id,name:name,revenue:revenue}, function(data){
				if(!data.success){
					alert("操作失败!");
				}
	    	}); 
		}
	});
   
    
    
    
});
function openData(data){
	if(data.indexOf(",")>=0){
		var file = data.substr(0,data.length-1);
		var str = file.split(",");
		for(var i = 0 ;i<str.length;i++){
			window.open(str[i]);
		}
	}else{
		window.open(data);
	}
}
function getChargetype(){
	//判断修改后相应的计费方式修改
	var customer_id = $("#customer_id").val();
	var sp_id = $("#sp_id").val();
	if(customer_id != null && customer_id !="" && sp_id != null && sp_id !=""){
		//获取当前供应商客户的计费方式
		$.post("/serviceProvider/seachChargeType",{sp_id:sp_id,customer_id:customer_id},function(data){
			if(data.CHARGE_TYPE == null){
				//这里是当前客户和供应商没有数据维护的情况
				$("input[name='chargeType2']").each(function(){
					if($(this).val() == 'perUnit'){
						$(this).prop('checked', true);
					}
				});
			}else{
				 //var chargeTypeRadio2 = $("#chargeTypeRadio2").val();
				$("input[name='chargeType2']").each(function(){
					if($(this).val() == data.CHARGE_TYPE){
						$(this).prop('checked', true);
					}
				});
			}
		},'json');
	}
}