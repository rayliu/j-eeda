﻿
$(document).ready(function() {
	$('#menu_transfer').addClass('active').find('ul').addClass('in');
	//设置一个变量值，用来保存当前的ID
	
	$.editable.addInputType('autocompleteType', {
	    element : $.editable.types.text.element,
	    plugin: function (settings, original) {
	      $('input', this).autocomplete({
	        //source: "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id
	        source: [ "c++", "java", "php", "coldfusion", "javascript", "asp", "ruby" ]
	      });
	    }
	});
	
    //from表单验证
	var validate = $('#transferOrderUpdateForm').validate({
        rules: {
        	customerMessage: {
            required: true
          }
        },
        messages : {	             
        	customerMessage : {required:  "请选择一个客户"}
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
    	$.get('/yh/transferOrder/searchLocationFrom', {locationFrom:locationFrom}, function(data){
    		console.log(data);			
    		var provinceVal = data.PROVINCE;
    		var cityVal = data.CITY;
    		var districtVal = data.DISTRICT;
	        $.get('/yh/serviceProvider/searchAllLocation', {province:provinceVal, city:cityVal}, function(data){	
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
				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+" "+contact_person+" "+phone+"</a></li>");
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
		console.log($('#spList').is(":focus"))
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
        var chargeType = $(this).attr('chargeType');
		$("input[name='chargeType2']").each(function(){
			if(chargeType == $(this).val()){
				$(this).prop('checked', true);
			}
		});
        
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
        	alert("请先保存运输单!");
	       	return false;
        }
		//异步向后台提交数据
        if($("#order_id").val() == ""){
        	console.log($("#transferOrderUpdateForm").serialize());
	    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#departureConfirmationBtn").attr("disabled", false);
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//$("#style").show();	
				  	
				  	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				  	
				  	
	            	var order_id = $("#order_id").val();
				  	itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;
				  	itemDataTable.fnDraw(); 
			        
			        //location.href = "/yh/transferOrder";               
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
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					if(transferOrder.STATUS == '已发车' || transferOrder.STATUS == '已入库' || transferOrder.STATUS == '已签收'){
						$("#departureConfirmationBtn").attr("disabled", true);						
					}else{
						$("#departureConfirmationBtn").attr("disabled", false);
					}
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//$("#style").show();	
				  	
				  	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				  	
	            	var order_id = $("#order_id").val();
				  	itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;
				  	itemDataTable.fnDraw();
			        
			        //location.href = "/yh/transferOrder";                
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
    	if($("#transferOrderStatus").val() == '新建' || $("#transferOrderStatus").val() == ''){
	    	e.preventDefault();
	    	// 切换到货品明细时,应先保存运输单
	    	//提交前，校验数据
	    	console.log($("#transferOrderUpdateForm").valid());
	        if(!$("#transferOrderUpdateForm").valid()){
	        	alert("请先保存运输单!");
		       	return false; 
	        }
	        var bool =false;
	        if("chargeCheckOrderbasic" == parentId){
	        	bool = true;
	        }
	        
	        if($("#order_id").val() == ""){
		    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
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
					//$("#driver_id").val(transferOrder.DRIVER_ID);
					$("#id").val(transferOrder.ID);
					if(transferOrder.ID>0){
						if(transferOrder.STATUS == '已发车' || transferOrder.STATUS == '已入库' || transferOrder.STATUS == '已签收'){
							$("#departureConfirmationBtn").attr("disabled", true);						
						}else{
							$("#departureConfirmationBtn").attr("disabled", false);
						}
						$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
					  	//$("#style").show();
					
						if(bool){
							$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
						}
						
		            	var order_id = $("#order_id").val();
					  	itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;
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
	var order_id = $("#order_id").val();
	//datatable, 动态处理
    var itemDataTable = $('#itemTable').dataTable({
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id,
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
            	"sClass": "item_no"
        	},
            {
            	"mDataProp":"ITEM_NAME",
            	"sWidth": "180px",
            	"sClass": "item_name"
            },
            {
            	"mDataProp":"SIZE",            	
            	"sWidth": "50px",
            	"sClass": "size"
        	},
            {
            	"mDataProp":"WIDTH",
            	"sWidth": "50px",
            	"sClass": "width"
            },
            {
            	"mDataProp":"HEIGHT",            	
            	"sWidth": "50px",
            	"sClass": "height"
        	}, 
            {
            	"mDataProp":"WEIGHT",
            	"sWidth": "50px",
            	"sClass": "weight",
            },
        	{
            	"mDataProp":"AMOUNT",
            	"sWidth": "50px",
            	"sClass": "amount"
            }, 
            {
            	"mDataProp":"UNIT",
            	"sWidth": "50px",
            	"sClass": "unit"
            },
            {
            	"mDataProp":null,
            	"sWidth": "50px",
            	"sClass": "sumWeight",
            	"fnRender": function(obj) {
        			return obj.aData.WEIGHT * obj.aData.AMOUNT;
                }
            },
            {
            	"mDataProp":"VOLUME",
            	"sWidth": "50px",
            	"sClass": "volume",
            	"fnRender": function(obj) {
            		return obj.aData.VOLUME * obj.aData.AMOUNT;
            	}
            },            
            {"mDataProp":"REMARK"},
            {  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-success btn-xs dateilEdit' code='?id="+obj.aData.ID+"' title='单品编辑'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                            "</a> "+
                            "<a class='btn btn-danger btn-xs deleteItem' code='?item_id="+obj.aData.ID+"' title='删除'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+
                            "</a>";
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
		detailDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItemDetail/transferOrderDetailList?orderId="+orderId;
		detailDataTable.fnDraw();
	};
	
	// 刷新货品列表
	var refreshItemTable = function(){
		var order_id = $("#order_id").val();
        itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;                		
    	itemDataTable.fnDraw();
	};
	
    itemDataTable.makeEditable({
    	sUpdateURL: '/yh/transferOrderItem/saveTransferOrderItemByField',    	
    	oEditableSettings: {event: 'click'},
    	"aoColumns": [  			            
            {            
            	style: "inherit",
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"item_no",
            	placeholder: "", 
            	callback: function () {
            		/*var currentEle = this;
            		$("input[name='cargoNature']").each(function(){
            			if($(this).prop('checked') == true){
            				if($(this).val() == $(currentEle).parent().children('.item_name')[0].innerHTML){            					
            					refreshItemTable();
            				}else{
            					alert("请使用与货品属性一致的产品!");
            					return false;
            				}
            			}
            		});*/
            	}
        	},
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"item_name",
            	placeholder: "",
            	callback: function () {
            		refreshDetailTable();
            		refreshItemTable();
            	} 
            },
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"size",
            	placeholder: "",
            	callback: function () {
            		sumVolume(this);
            		// refreshDetailTable();
            		// refreshItemTable();
            	} 
            },
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"width",
            	placeholder: "",
            	callback: function () {
            		sumVolume(this);
            		// refreshDetailTable();
            		// refreshItemTable();
            	} 
            },
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"height",
            	placeholder: "",
            	callback: function () {
            		sumVolume(this);
            		// refreshDetailTable();
            		// refreshItemTable();
            	} 
            },  
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"weight",
            	placeholder: "",
            	callback: function () {
            		sumWeight(this);
            		// refreshDetailTable();
            		// refreshItemTable();
            	} 
            },    	
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"amount",
            	placeholder: "",
            	callback: function () {
            		sumVolume(this);
            		sumWeight(this);
            	} 
            },     	
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"unit",
            	type: 'select',
            	data: "{'':'', '台':'台','件':'件','套':'套'}",
            	placeholder: "",
            	callback: function () {
            		//refreshItemTable();
            	} 
            },
            null,
            null,
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"remark",
            	type: 'textarea',
            	placeholder: "",
            	callback: function () {} 
            },
            null                        
        ]      
    }).click(function(){

    	var inputBox = $(this).find('input');
      //   inputBox.bind('keydown', function(e){
      //   	var code = e.keyCode || e.which;
		    // if (code == '9') {
		    // 	var currentCell = e.target.parentNode.parentNode;
		    // 	var nextCellIndex = currentCell.cellIndex+1;
		    // 	var parentRow = $(e.target.parentNode.parentNode.parentNode);
		    // 	var nextCell = parentRow.children('td:eq('+nextCellIndex+')');
		    // 	$(currentCell).text(inputBox.val());
		    // 	nextCell.click().focus();
		    // 	//return false;
		    // }
      //   });
        inputBox.autocomplete({
	        source: function( request, response ) {
	        	if(inputBox.parent().parent()[0].cellIndex >1){//从第3列开始，不需要去后台查数据
		    		return;
		    	}
	            $.ajax({
	                url: "/yh/transferOrder/searchItemNo",
	                dataType: "json",
	                data: {
	                    customerId: $('#customerId').val(),
	                    input: request.term
	                },
	                success: function( data ) {
	                	console.log(data);
						var columnName = inputBox.parent().parent()[0].className;
		        		
	                    response($.map( data, function( data ) {
	                        return {
	                            label: '型号:'+data.ITEM_NO+' 名称:'+data.ITEM_NAME,
	                            value: columnName=='item_name'?data.ITEM_NAME:data.ITEM_NO,
	                            id: data.ID,
	                            item_no: data.ITEM_NO,
	                            item_name: data.ITEM_NAME
	                        };
	                    }));
	                }
	            });
	        },
        	select: function( event, ui ) {
        		
        		//将选择的产品id先保存到数据库
        		var itemId = $(this).parent().parent().parent()[0].id;
        		var productId = ui.item.id;
        		$.post('/yh/transferOrderItem/saveTransferOrderItem', {transferOrderItemId:itemId,productId:productId},	function(data){   					
					refreshItemTable();
					/*$("input[name='cargoNature']").each(function(){
    					if($(this).prop('checked') == true){
    						if($(this).val() == 'ATM'){
                				if(data.ITEM_NAME == 'ATM'){            					
                					refreshItemTable();
                					return;
                				}else{
                					alert("请使用与货品属性一致的产品!");
                					$.post('/yh/transferOrderItem/deleteTransferOrderItemProduct', {transferOrderItemId:itemId,productId:productId},function(data){                						
                					}, 'json');
                					return false;
                				}
    						}
            			}
					});*/
    			}, 'json');    
            },
        	minLength: 2
        });
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
	                itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;                		
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
  	  console.log(this);
  	  var inputId  = $(this).attr('id');
	  if(inputId=='cargoNature2'){
		 $("#cargoNatureDetailSpan").show();
	  }
  	});    
    
    // 当arrivalMode1为货品直送时则显示收货人的信息
    $("#arrivalModes").on('click', 'input', function(){
    	console.log(this);
    	var inputId  = $(this).attr('id');
    	if(inputId=='arrivalMode1'){
    		$("#contactInformation").show();
    		$("#warehousingConfirmBtn").attr("disabled", true);
    		$("#gateInSelect").hide();
    		$("#warehousingConfirmBtn").attr("disabled", true);
    	}else{
    		$("#contactInformation").hide();
    		$("#gateInSelect").show();
    	} 
    });    
						
	/*// 发车确认
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
	});*/
	
	$("input[name='arrivalMode']").each(
		function(){
			if($(this).attr('checked') == 'checked'){
				 $("#contactInformation").show();
				 return false; 
			}else{
				 $("#contactInformation").hide();
			}
		}			
	);
	
	// 保存单品信息
	$("#transferOrderItemDetailFormBtn").click(function(){
		$.post('/yh/transferOrderItemDetail/saveTransferOrderItemDetail', $("#transferOrderItemDetailForm").serialize(), function(transferOrderItemDetail){
			if(transferOrderItemDetail.ID > 0){
				$("#detailModal").modal('hide');
				$("#transferOrderItemDetailForm")[0].reset();
				var itemId = $("#item_id").val();
				var orderId = $("#order_id").val();
				// 刷新单品列表
				detailDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItemDetail/transferOrderDetailList?item_id="+itemId;
				detailDataTable.fnDraw();
				
				// 刷新货品列表
                itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+orderId;                		
            	itemDataTable.fnDraw();
			}			
		});
	});
	
	var transferOrderMilestone = function(){
	  	var order_id = $("#order_id").val();
		$.post('/yh/transferOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
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
		    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
					$("#transfer_order_id").val(transferOrder.ID);
					$("#update_transfer_order_id").val(transferOrder.ID);
					$("#order_id").val(transferOrder.ID);
					$("#transfer_milestone_order_id").val(transferOrder.ID);
					$("#notify_party_id").val(transferOrder.NOTIFY_PARTY_ID);
					//$("#driver_id").val(transferOrder.DRIVER_ID);
					$("#id").val(transferOrder.ID);
					if(transferOrder.ID>0){
						console.Log("\\\\\\\\\\\\\\"+parentId);
						$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
					  	//$("#style").show();	
					  	transferOrderMilestone();
					  	console.Log(parentId);
					  	if(bool){
					  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
					  	}
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
					//$("#driver_id").val(transferOrder.DRIVER_ID);
					$("#id").val(transferOrder.ID);
					if(transferOrder.ID>0){
						if(transferOrder.STATUS == '已发车' || transferOrder.STATUS == '已入库' || transferOrder.STATUS == '已签收'){
							$("#departureConfirmationBtn").attr("disabled", true);						
						}else{
							$("#departureConfirmationBtn").attr("disabled", false);
						}
						$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
					  	//alert("运输单保存成功!");
					  	//$("#style").show();
						console.log(parentId);
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
		$.post('/yh/transferOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.empty();
			for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
			{
				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
			}
		},'json');
		parentId = e.target.getAttribute("id");
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
			if($("#cargoNatureRadio").val() == 'cargo'){
				$("#cargoNatureDetailSpan").show();
				$("input[name='cargoNatureDetail']").each(function(){
					if($(this).val() == $("#cargoNatureDetailRadio").val()){
						$(this).attr('checked', true);
					}
				});
			}
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
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/yh/transferOrderItemDetail/transferOrderDetailList?orderId="+orderId,
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
            	"sClass": "serial_no",
            	"fnRender": function(obj) {
                    if(obj.aData.SERIAL_NO!='' && obj.aData.SERIAL_NO != null){
                        return "<input type='text' name='serial_no' value='"+obj.aData.SERIAL_NO+"'>";
                    }else{
                    	 return "<input type='text' name='serial_no'>";
                    }
            }},
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
            	"mDataProp":"PIECES",
            	"sWidth": "80px",
            	"sClass": "pieces",
            	"fnRender": function(obj) {
                    if(obj.aData.PIECES!='' && obj.aData.PIECES != null){
                        return "<input type='text' name='pieces' value='"+obj.aData.PIECES+"'>";
                    }else{
                    	 return "<input type='text' name='pieces'>";
                    }
            }},
            {
            	"mDataProp":"CONTACT_PERSON",
        		"sWidth": "80px",
            	"sClass": "contact_person",
            	"fnRender": function(obj) {
                    if(obj.aData.CONTACT_PERSON!='' && obj.aData.CONTACT_PERSON != null){
                        return "<input type='text' name='contact_person' value='"+obj.aData.CONTACT_PERSON+"'>";
                    }else{
                    	 return "<input type='text' name='contact_person'>";
                    }
            }},
            {
            	"mDataProp":"PHONE",
        		"sWidth": "80px",
            	"sClass": "phone",
            	"fnRender": function(obj) {
                    if(obj.aData.PHONE!='' && obj.aData.PHONE != null){
                        return "<input type='text' name='phone' value='"+obj.aData.PHONE+"'>";
                    }else{
                    	 return "<input type='text' name='phone'>";
                    }
            }},
            {
            	"mDataProp":"ADDRESS",
        		"sWidth": "80px",
            	"sClass": "address",
            	"fnRender": function(obj) {
                    if(obj.aData.ADDRESS!='' && obj.aData.ADDRESS != null){
                        return "<input type='text' name='address' value='"+obj.aData.ADDRESS+"'>";
                    }else{
                    	 return "<input type='text' name='address'>";
                    }
            }},
            {
            	"mDataProp":"REMARK",
        		"sWidth": "80px",
            	"sClass": "remark",
            	"fnRender": function(obj) {
                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                        return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
                    }else{
                    	 return "<input type='text' name='remark'>";
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

    /*detailDataTable.makeEditable({
    	sUpdateURL: '/yh/transferOrderItemDetail/saveTransferOrderItemDetailByField',    	
    	oEditableSettings: {event: 'click'},
    	"aoColumns": [  			            
            {            
            	style: "inherit",
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"serial_no",
            	placeholder: "", 
            	callback: function () {}
        	},
            null,
            null,
            null,
            null,
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"contact_person",
            	placeholder: "",
            	callback: function () {} 
            },
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"phone",
            	placeholder: "",
            	callback: function () {} 
            },  
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"address",
            	placeholder: "",
            	callback: function () {} 
            },
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"remark",
            	type: 'textarea',
            	placeholder: "",
            	callback: function () {} 
            }                       
        ]      
    }).click(function(){
    	var inputBox = $(this).find('input');
        inputBox.autocomplete({
	        source: function( request, response ) {
	        },
        });
    }); */                                                                     
        
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

		//detailDataTable.fnAddData({'SERIAL_NO':'test', 'ITEM_NAME':'test', 'VOLUME':'test','WEIGHT':'test', 'CONTACT_PERSON':'test', 'REMARK':'test'} );
		//detailDataTable.fnDraw(); 
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
		$.post('/yh/transferOrderItem/deleteTransferOrderItem', 'transfer_order_item_id='+itemId, function(data){
		},'json');
		$("#transferOrderItemDateil").hide();
		// 更新货品列表
		var order_id = $("#order_id").val();
		itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;
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
		$.post('/yh/transferOrderItemDetail/deleteTransferOrderItemDetail', {detail_id:detailId,notify_party_id:notifyPartyId}, function(data){
		},'json');
		// 更新单品列表
		var orderId = $("#order_id").val();
		detailDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItemDetail/transferOrderDetailList?orderId="+orderId;
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
					 gateInSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].WAREHOUSE_NAME+"</option>");					 
				 }else{
					 gateInSelect.append("<option value='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</option>");
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
				 if(i == 0){
					 officeSelect.append("<option ></option>");
				 }else{
					 if(data[i].ID == hideOfficeId){
						 officeSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");
					 }else{
						 officeSelect.append("<option value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");					 
					 }
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
	 
	 // 回显付款方式
	 $("input[name='payment']").each(function(){
		 if($("#paymentRadio").val() == $(this).val()){
			 $(this).attr('checked', true);
		 }
	 });

  	//获取货品的序列号list，选中信息在下方展示其他信息
 	$('#itemNoMessage').on('keyup click', function(){
 		var inputStr = $('#itemNoMessage').val();
 		var customerId = $('#customerId').val();
 		$.get('/yh/transferOrder/searchItemNo', {input:inputStr,customerId:customerId}, function(data){
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

 	// 没选中序列号，焦点离开，隐藏列表
 	$('#itemNoMessage').on('blur', function(){
  		$('#itemNoList').hide();
  	});
 	
 	//获取货品的名称list，选中信息在下方展示其他信息
 	$('#itemNameMessage').on('keyup click', function(){
 		var inputStr = $('#itemNameMessage').val();
 		var customerId = $('#customerId').val();
 		$.get('/yh/transferOrder/searchItemName', {input:inputStr,customerId:customerId}, function(data){
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
 	$("#editTransferOrderItem").click(function(){
 		var orderId = $("#order_id").val();
 		$.post('/yh/transferOrderItem/addNewRow', {orderId:orderId}, function(data){
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
    
    if($("#arrivalMode2").prop('checked') == true){
    	$("#contactInformation").hide();
    	$("#gateInSelect").show();    	
    }
    
    // 查看所有单品
    $("#findAllDetailBtn").click(function(){
    	var orderId = $("#order_id").val();
		// 刷新单品列表
		detailDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItemDetail/transferOrderDetailList?orderId="+orderId;
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
	        
	    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
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
	            	receipttable.fnSettings().sAjaxSource = "/yh/transferOrder/accountReceivable/"+order_id;
	            	receipttable.fnDraw();       

	            	paymenttable.fnSettings().sAjaxSource = "/yh/transferOrder/accountPayable/"+order_id;
					paymenttable.fnDraw(); 
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
			{"mDataProp":"TRANSFERORDERNO","sClass": "remark"},
			{"mDataProp":"TRANSFERORDERNO","sClass": "remark"},
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
		$.post('/yh/transferOrder/finItemdel/'+id,function(data){
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
        "bServerSide": true,
        "sAjaxSource": "/yh/transferOrder/accountPayable/"+order_id,
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
		$.post('/yh/transferOrder/finItemdel/'+id,function(data){
               //保存成功后，刷新列表
               console.log(data);
               paymenttable.fnDraw();
           },'text');
	});
	
	//应付
	$("#item_fin_save").click(function(){
	    var order_id =$("#order_id").val();
		$.post('/yh/transferOrder/receiptSave/'+order_id, $("#fin_form").serialize(), function(data){
			console.log(data);
			if(data.success){
				receipttable.fnDraw();
				$('#fin_item').modal('hide');
				$('#resetbutton').click();
			}else{
				
			}		
		});		
	});	
	
	//应付
	$("#addrow").click(function(){	
	    var order_id =$("#order_id").val();
		$.post('/yh/transferOrder/addNewRow/'+order_id,function(data){
			console.log(data);
			if(data[0] != null){
				paymenttable.fnSettings().sAjaxSource = "/yh/transferOrder/accountPayable/"+order_id;
				paymenttable.fnDraw(); 
				//paymenttable.fnDraw();
				//$('#fin_item2').modal('hide');
				//$('#resetbutton2').click();
			}else{
				alert("请到基础模块维护应付条目！");
			}
		});		
	});	
	//应付修改
	$("#table_fin2").on('blur', 'input,select', function(e){
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
	
	//应收
	$("#addrow2").click(function(){	
	    var order_id =$("#order_id").val();
		$.post('/yh/transferOrder/addNewRow2/'+order_id,function(data){
			console.log(data);
			if(data[0] != null){
				receipttable.fnSettings().sAjaxSource = "/yh/transferOrder/accountReceivable/"+order_id;
            	receipttable.fnDraw();  
				//$('#fin_item2').modal('hide');
				//$('#resetbutton2').click();
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
	/*if(($("#transferOrderStatus").val() == '已发车' || $("#transferOrderStatus").val() == '在途') && $("#transferOrderArrivalMode").val() == 'delivery'){
		$("#receiptBtn").attr("disabled", false);
	}
	
	// 收货确认
	$("#receiptBtn").click(function(e){
    	$(this).attr("disabled",true);
    	var orderId = $("#order_id").val();
    	$.post('/yh/transferOrderMilestone/receipt', {orderId:orderId}, function(){    	
    	});
    });*/
	
	// 选中仓库触发事件
	$("#gateInSelect").change(function(){
    	$.post('/yh/transferOrder/selectWarehouse', {warehouseId:$(this).val()}, function(data){  
		    var officeOption=$("#officeSelect>option");
		    var officeVal=data.warehouse.OFFICE_ID;
		    for(var i=0;i<officeOption.length;i++){
		    	var svalue=officeOption[i].value;
		    	if(officeVal==svalue){
		    		$("#officeSelect option[value='"+svalue+"']").attr("selected","selected");
		    	}
		    }
			
			var hideProvince = data.location.PROVINCE;
			var hideCity = data.location.CITY;
			var hideDistrict = data.location.DISTRICT;
			if(hideProvince != undefined){
				if(hideCity != undefined){
					if(hideDistrict != undefined){
	    				$("#locationTo").val(data.DISTRICTCODE);
	    			}else{    	    				
	    				$("#locationTo").val(data.CITYCODE);
	    			}
    			}else{
    				$("#locationTo").val(data.PROVINCECODE);        				
    			}    				
			}
		    //获取全国省份
		    $(function(){
		     	var province = $("#mbProvinceTo");
		     	$.post('/yh/serviceProvider/province',function(data){
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
		    $.get('/yh/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
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
		    $.get('/yh/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
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
        $('#beginTime_filter').trigger('keyup');
    });


    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter').trigger('keyup');
    });
});