 $(document).ready(function() {
    if(departOrder.orderNo){
    	document.title = departOrder.orderNo +' | '+document.title;
    }else if(departOrder.departNo){
    	document.title = departOrder.departNo +' | '+document.title;
    }
    
   
  //按钮控制
	if($('#departOrderStatus').val()=='新建'){
		$("#departureConfirmationBtn").attr("disabled",false);
	}else{
		$("#departureConfirmationBtn").attr("disabled",true);
	}
	if($('#audit_status').val()=='新建'){
		$("#saveDepartOrderBtn").attr("disabled",false);
	}else{
		$("#saveDepartOrderBtn").attr("disabled",true);
	}
	
	
	//撤销发车单
	$("#deleteBtn").on('click',function(){
		var status = $('#departOrderStatus').val();//单据状态
		var audit_status = $('#audit_status').val();  //财务状态
		var departOrderId = $("#departOrderId").val();  //发车单id
		//按钮控制
		$("#deleteBtn").attr("disabled",true);
		
		if(!confirm("是否确认撤销此订单？"))
    		return;
		if(departOrderId==''){
			$.scojs_message('单据未生成，无法撤销！', $.scojs_message.TYPE_FALSE);
			return;
		}
		if(audit_status!='新建'&& audit_status!='已确认'){
			$.scojs_message('此单据已做了财务，无法撤销！', $.scojs_message.TYPE_FALSE);
			return;
		}
		if(status!='新建' && status!='运输在途'){
			$.scojs_message('此单据已做了下级单据，无法撤销！', $.scojs_message.TYPE_FALSE);
			return;
		}
		$.post('/departOrder/cancelOrder',{orderId:departOrderId},function(data){
			if(!data.success){
    			$("#deleteBtn").attr('disabled',false);
    			$.scojs_message('撤销失败', $.scojs_message.TYPE_ERROR);
    		}else{
    			$.scojs_message('撤销成功!,1秒后自动返回发车单列表。。。', $.scojs_message.TYPE_OK);
    			setTimeout(function(){
					location.href="/departOrder";
				}, 1000);
    		}
		});
		
				
		
	});
	
	   
    //体积和重量回显
    var volume = $('#volume').val();
    if(volume!=''){
 	   $('#w_v').show();
    }
	
	
	 //from表单验证
	var validate = $('#orderForm').validate({
        rules: {
        	departure_time: {required: true},
        	arrival_time: {required: true}
        },
        messages : {	             
        }
    });
		
    $('#menu_assign').addClass('active').find('ul').addClass('in');
	var departOrderStatus=$("#departOrderStatus").val();
    var message=$("#message").val();
    var type=$("#type").val();
    var depart_id=$("#depart_id").val();
    $("#milestoneDepartId").val(depart_id);
    var last_detail_size=$("#last_detail_size").val();
    var hang="";
    
    //计费方式回显
	var departOrderId = $("#departOrderId").val();
	if(departOrderId != '' && departOrderId != null){
		var departOrderChargeType = $("#departOrderChargeType").val();
		
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
                    $("#carInfomation").show();
                    var departOrderCarType = $("#hiddenDepartOrderCarType").val();
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
   
    
     if(type=="one"){
    	$("#ordertypeone").attr('checked', 'checked');
     }else{
    	$("#ordertypetwo").attr('checked', 'checked');
     }
     if(last_detail_size=='false'){
     	$("#box_one").modal('show');
     }else if(last_detail_size=='true'){
     	$("#box_two").modal('show');
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

 
     
     // 列出所有的司机
   	 $('#driverMessage').on('keyup click', function(){
        var inputStr = $('#driverMessage').val();
        $.get('/transferOrder/searchAllDriver', {input:inputStr}, function(data){
        	console.log(data);
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
   	});
   	  	
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
   	$('#carNoMessage').on('keyup click', function(){
   	var inputStr = $('#carNoMessage').val();
   	$.get('/transferOrder/searchAllCarInfo', {input:inputStr}, function(data){
   		console.log(data);
   		var carNoList = $("#carNoList");
   		carNoList.empty();
   		for(var i = 0; i < data.length; i++)
   		{
   			carNoList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' carNo='"+data[i].CAR_NO+"' carType='"+data[i].CARTYPE+"' length='"+data[i].LENGTH+"' driver='"+data[i].DRIVER+"' phone='"+data[i].PHONE+"'> "+data[i].CAR_NO+"</a></li>");
   		}
   	},'json');

   	$("#carNoList").css({ 
          	left:$(this).position().left+"px", 
          	top:$(this).position().top+32+"px" 
         }); 
         $('#carNoList').show();
   	});
   	 	
   	// 选中车辆
   	$('#carNoList').on('mousedown', '.fromLocationItem', function(e){			
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
   	$('#carNoMessage').on('blur', function(){
    		$('#carNoList').hide();
    });

	if($("#driverMessage").val() == ''){
		$("#driverMessage").val($("#carInfoDriverMessage").val());
	}
	
	if($("#driver_phone").val() == ''){
		$("#driver_phone").val($("#carInfoDriverPhone").val());
	}

   	var message=$("#message").val();
    var type=$("#type").val();
    var depart_id=$("#depart_id").val();
    var tr_item=$("#tr_itemid_list").val();
    var item_detail=$("#item_detail").val();
	 //显示货品table
	 var datatable = $('#departItem-table').dataTable({
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
        "bDestroy": true,
    	 "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/pickupOrder/getInitPickupOrderItems?localArr="+message+"&tr_item="+tr_item+"&item_detail="+item_detail,
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
	        			 return (obj.aData.CARGOAMOUNT * obj.aData.VOLUME).toFixed(2);
	        		 }
	                  
	             }
            },
            { "mDataProp": null,
	           	 "fnRender": function(obj) {   
	           		if(obj.aData.CARGO_NATURE == "ATM"){
	        			 return (obj.aData.ATMAMOUNT * obj.aData.WEIGHT).toFixed(2);
	        		 }else{
	        			 return (obj.aData.CARGOAMOUNT * obj.aData.WEIGHT).toFixed(2);
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
    	$("#departItem-table").on('click', '.dateilEdit', function(e){
    		e.preventDefault();
    		
    		$("#transferOrderItemDateil").show();
    		var code = $(this).attr('code');
    		var itemId = code.substring(code.indexOf('=')+1);
    		tr_itemid_list.push(itemId);
    		$("#item_id").val(itemId);
    		$("#item_save").attr("disabled", false);
    		$("#style").hide();
    		detailTable.fnSettings().sAjaxSource = "/departOrder/findAllItemDetail?item_id="+itemId+"&departId="+$("#departOrderId").val();
    		detailTable.fnDraw();  			
    	});
    	
    		// 删除货品
    		$("#eeda-table").on('click', '.cancelbutton', function(e){
    			e.preventDefault();		
    			 var code = $(this).attr('code');
    			var itemId = code.substring(code.indexOf('=')+1);
    			 $("table tr:eq("+hang+")").remove(); 
    		});
    		
    		var the_id="";
    		var item_id = $("#item_id").val();
    		var detailTable= $('#detailTable').dataTable({           
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

    	    //获取全国省份
    	    $(function(){
    	     	var province = $("#mbProvince");
    	     	$.post('/serviceProvider/province',function(data){
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
    				$.get('/serviceProvider/city', {id:inputStr}, function(data){
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
    				$.get('/serviceProvider/area', {id:inputStr}, function(data){
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
    	     $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
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
    	     $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
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
    	    
    	    //tab-发车单里程碑
    	    $("#transferOrderMilestoneList").click(function(e){
    			e.preventDefault();
    			//提交前，校验数据
	    		if("chargeCheckOrderbasic" == parentId && !$("#saveDepartOrderBtn").attr("disabled")){
	    			clickSaveDepartOrder(e);
    	    	}
    			$.post('/departOrder/transferOrderMilestoneList',{departOrderId:$("#departOrderId").val()},function(data){
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
    			$.post('/departOrder/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
    				var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
    				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
    			},'json');
    			$("#status").val("");
    			$("#location").val("");
    			$('#transferOrderMilestone').modal('hide');
    		});
    		
    	    var handlePickkupOrderDetail = function(){
    	    	//如果计费方式为计件或者零担，此时车牌号，司机，电话为空
    	    	if($('input[name="chargeType"]:checked').val()==='perCargo'||$('input[name="chargeType"]:checked').val()==='perUnit'){
    	    		$("#carNoMessage").val("");
    	    		$("#driverMessage").val("");
    	    		$("#driver_phone").val("");
    	    		$("#carinfoId").val("");
    	    	}
    	    	
    	    	if($('#booking_note_number').val() == ''){
    	    		$.scojs_message('托运单号不能为空', $.scojs_message.TYPE_WARN);
    	    		return;
    	    	}
    	    	
    	    	
    	    	// 保存
    	    	$.post('/departOrder/saveDepartOrder', $("#orderForm").serialize(), function(data){
    				if(data.ID>0){
                        $("#depart_order_token").val(data.DEPART_ORDER_TOKEN);
    					$("#departOrderId").val(data.ID);
    					$("#depart_id").val(data.ID);
    					$("#showDepartNo").text(data.DEPART_NO);
    					$("#milestoneDepartId").val(data.ID);
    		    	    contactUrl("edit?id",data.ID);
    		    	    $.post('/departOrder/saveupdatestate', $("#orderForm").serialize(), function(){	
     	                	paymenttable.fnSettings().sAjaxSource = "/departOrder/accountPayable/"+$("#departOrderId").val();
     	                	paymenttable.fnDraw(); 
     	                	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
     	                	$("#deleteBtn").attr("disabled",false);
     	                });
    		    	    
    				}else{
    					$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
    				}
    			},'json');
    	    };
    	    
    	    var saveDepartOrderFunction = function(){
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
    	    	if(uncheckedDetailIds.length > 0){
    	    		handlePickkupOrderDetail();
    	    		// 对一张单进行多次提货,把选中的和没选中的单品区分开来,然后在进行判断
    	    		$("#detailDialog").modal('show');
    	    	}else{
    	    		handlePickkupOrderDetail();
    	    	}
    	    };

    	    $("#continueCreateBtn").click(function(){
    	    	$("#detailCheckBoxForm").submit();
    	    });

    	    //点击保存的事件，保存发车单信息
    	    var clickSaveDepartOrder = function(e){
    	    	//阻止a 的默认响应行为，不需要跳转
    			e.preventDefault();	
    			//异步向后台提交数据
    			saveDepartOrderFunction();
    	    }; 
    	    var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
    	        '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
    	        'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
    	        '</div>';
    	    $('body').append(alerMsg);

    	    $('#message_trigger_err').on('click', function(e) {
    	    	e.preventDefault();
    	    });
    	    /*--------------------------------------------------------------------*/
    	    //编辑保存
    	    $("#saveDepartOrderBtn").click(function(e){
    	    	var priceType = $("input[name='priceType']:checked").val();
    	    	e.preventDefault();
			   	clickSaveDepartOrder(e);
			   	
    	    });
    	    
    	    //tab-应付
    	    $("#arap1").click(function(e){
    			e.preventDefault();
			   	if("chargeCheckOrderbasic" == parentId && !$("#saveDepartOrderBtn").attr("disabled")){
			   		clickSaveDepartOrder(e);
    	    	}
			   	paymenttable.fnSettings().sAjaxSource = "/departOrder/accountPayable/"+$("#departOrderId").val();
            	paymenttable.fnDraw(); 
    	    	parentId = e.target.getAttribute("id");
    	    });
    	    //动态提示
    	    var parentId = "chargeCheckOrderbasic";
    	    $("#chargeCheckOrderbasic").click(function(e){
    	    	parentId = e.target.getAttribute("id");
    	    });
    	    // 点击货品信息
    	    $("#departOrderItemList").click(function(e){
    			e.preventDefault();
    			var message=$("#message").val();
    	        var type=$("#type").val();
    	        var sp=$("#sp_id").val();
    	        var tr_item=$("#tr_itemid_list").val();
    	        var item_detail=$("#item_detail").val();
    	 	    var departOrderId = $("#departOrderId").val();
    	 	    if($('#booking_note_number').val() == ''){
   	    		$.scojs_message('托运单号不能为空', $.scojs_message.TYPE_WARN);
   	    			return;
   	    	    } 
    			if("chargeCheckOrderbasic" == parentId){
    				if(!$("#saveDepartOrderBtn").attr("disabled")){
	    				// 保存单品
		 	    		$.post('/departOrder/saveDepartOrder', $("#orderForm").serialize(), function(data){
	        				$("#departOrderId").val(data.ID);
	        				if(data.ID>0){
	        					$("#depart_order_token").val(data.DEPART_ORDER_TOKEN);
	        					$("#departOrderId").val(data.ID);
	        					$("#depart_id").val(data.ID);
	        					$("#showDepartNo").text(data.DEPART_NO);
	        					$("#milestoneDepartId").val(data.ID);
                                contactUrl("edit?id",data.ID);
	        		    	    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	        				}else{
	        					$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	        				}
	        				datatable.fnSettings().oFeatures.bServerSide = true; 
	    	    	 		datatable.fnSettings().sAjaxSource = "/departOrder/getDepartOrderItem?localArr="+message+"&tr_item="+tr_item+"&item_detail="+item_detail+"&departOrderId="+data.ID;
	    	    	 		datatable.fnDraw();
	        			},'json');
    				}else{
    					datatable.fnSettings().oFeatures.bServerSide = true; 
    	    	 		datatable.fnSettings().sAjaxSource = "/departOrder/getDepartOrderItem?localArr="+message+"&tr_item="+tr_item+"&item_detail="+item_detail+"&departOrderId="+departOrderId;
    	    	 		datatable.fnDraw();
    				}
	    		}else{
	    			datatable.fnSettings().oFeatures.bServerSide = true; 
	    	 		datatable.fnSettings().sAjaxSource = "/departOrder/getDepartOrderItem?localArr="+message+"&tr_item="+tr_item+"&item_detail="+item_detail+"&departOrderId="+departOrderId;
	    	 		datatable.fnDraw();
	    		}
    			parentId = e.target.getAttribute("id");
    	    });
    	    
    	    $("#departureConfirmationBtn").click(function(e){
    	    	//提交前，校验数据
    	    	//发车之前保存发车单
                if(!$("#orderForm").valid()){
    	    		$.scojs_message('操作失败，请确认基本信息是否输入完整', $.scojs_message.TYPE_ERROR);
     	        }else if($("#departure_time").val() == "" || $("#arrival_time").val() == ""){
     	        	$.scojs_message('操作失败，请确认基本信息是否输入完整', $.scojs_message.TYPE_ERROR);
     	        	var str = '<label for="departure_time" class="error">必选字段</label>';
     	        	$("#departure_time").after(str);
     	        	$("#arrival_time").after(str);
     	        }else if($("#sp_id").val() == "" && $("#partySpId").val() == ""){
     	        	$.scojs_message('操作失败，请选择供应商', $.scojs_message.TYPE_ERROR);
     	        }else if($("#departOrderId").val() == ""){
     	        	$.scojs_message('操作失败，请先保存发车单', $.scojs_message.TYPE_ERROR);
     	        }else{
	                $("#departureConfirmationBtn").attr("disabled",true);
	    	    	var priceType = $("input[name='priceType']:checked").val();
	    	    	var departOrderId = $("#departOrderId").val();
	    	    	//发车之前保存发车单
	    	    	$.post('/departOrder/saveDepartOrder', $("#orderForm").serialize(), function(data){
	    	    		//发车确认
	    	    		$.post('/departOrder/departureConfirmation',{departOrderId:departOrderId}, function(){
	    	    			$("#departOrderStatus").val('运输在途');
	    	    			$("#departStatus").text('运输在途');
	            	    	//计算应付
              	    	  	$.scojs_message('确认成功', $.scojs_message.TYPE_OK);
    	                	paymenttable.fnSettings().sAjaxSource = "/departOrder/accountPayable/"+$("#departOrderId").val();
    	                	paymenttable.fnDraw(); 
	        	    	});
	    			},'json');
     	        }
    	    });

    	    $("#warehousingConfirmBtn").click(function(e){
    	    	$(this).attr("disabled",true);
    	    	$.post('/departOrder/updatestate?order_state='+"已入库", $("#orderForm").serialize(), function(data){
    	    		if(data.amount>0){
    	    			alert("有"+data.amount+"个货品没入库,可以到产品中维护信息！");
    	    		}                    
    	    	});
    	    });
    	    $("#receiptBtn").click(function(e){
    	    	$(this).attr("disabled",true);
    	    	$("#order_hd").attr("disabled",false);
    	    	 $("#edit_status").attr("disabled",true);
    	    	$.post('/departOrder/updatestate?order_state='+"已签收", $("#orderForm").serialize(), function(){
    	    	
    	    	});
    	    });
    	    $("#order_hd").click(function(e){
    	    	$(this).attr("disabled",true);
    	    	$.post('/departOrder/CreatReturnOrder?order_id=', $("#orderForm").serialize(), function(data){
        	    	if(data==true){
        	    		alert("已生成回单！");
        	    	}else{
        	    		alert("生成回单遇到错误！");
        	    	}
    	    	});
    	    });
    	  //获取供应商的list，选中信息在下方展示其他信息
    		$('#spMessage').on('keyup click', function(){
    			var inputStr = $('#spMessage').val();
    			if(inputStr == ""){
    				var pageSpName = $("#pageSpName");
    				pageSpName.empty();
    				var pageSpAddress = $("#pageSpAddress");
    				pageSpAddress.empty();
    				$('#sp_id').val("");
    			}
    			$.get('/serviceProvider/searchSp', {input:inputStr, sp_type:'line'}, function(data){
    				console.log(data);
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
    		$('#spList').on('mousedown', '.fromLocationItem', function(e){
    	        console.log($('#spList').is(":focus"));
    			var message = $(this).text();
    			$('#spMessage').val(message.substring(0, message.indexOf(" ")));
    			$('#sp_id').val($(this).attr('partyId'));
    	        $('#spList').hide();
    	    });
    		// 没选中供应商，焦点离开，隐藏列表
    		$('#spMessage').on('blur', function(){
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
    	    });
    		
    		//回显供应商
    		var sp_id=$("#sp_id").val();
    		if(sp_id!=""){
    			$.get('/departOrder/getIntsp', {sp_id:sp_id}, function(data){
    				//console.log(data);
    				$('#spMessage').val(data.COMPANY_NAME);
    			},'json');
    		}
    		
    		//是直送显示“确认收货”
    		var check_sh=$("#check_sh").val();
    		if(check_sh==false){
    			$("#receiptBtn").show();
    			$("#order_hd").show();
    		}else{
    			//$("#warehousingConfirmBtn").show();
    		}

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
    	    
    	    if($("#departOrderArrivalMode").val() == 'delivery' && $("#departOrderStatus").val() == '已发车'){
    	    	$("#receiptBtn").attr("disabled", false);
    	    }

    	    
    	    $("#cancelBtn").click(function(){
    	    	$("#detailDialog").modal('hide');
    	    });
    	    
    	  //应付datatable
    		var paymenttable=$('#table_fin2').dataTable({
    			"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	        "bFilter": false, //不需要默认的搜索框
    	        //"sPaginationType": "bootstrap",
    	        "iDisplayLength": 10,
    	        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	        "bServerSide": true,
    	        "sAjaxSource": "/departOrder/accountPayable/"+depart_id,
    	    	"oLanguage": {
    	            "sUrl": "/eeda/dataTables.ch.txt"
    	        },
    	        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
    				$(nRow).attr('id', aData.ID);
    				return nRow;
    			},
    	        "aoColumns": [
    	            {"mDataProp":"TRANSFER_ORDER_NO"}, 
    	            {"mDataProp":"ITEM_AMOUNT"},
    	            {"mDataProp":"VOLUME"},
    	            {"mDataProp":"WEIGHT"},
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
					        	if(obj.aData.CREATE_NAME == 'system'){
					        		return obj.aData.NAME;
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
					{"mDataProp":"PRICE"},
					{"mDataProp":"AMOUNT",
					     "fnRender": function(obj) {
				        		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
						             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
						         }else{
						         	 return "<input type='text' name='amount'>";
						         }
				        	 
					 }},
					 {"mDataProp":null,
						 "fnRender":function(obj){
							 if(obj.aData.FIN_CHARGE_TYPE != null){
								 if(obj.aData.FIN_CHARGE_TYPE = "perUnit"){
									 return "计件";
								 }else if(obj.aData.FIN_CHARGE_TYPE = "perCar"){
									 return "整车";
								 }else{
									return "零担";
								 }
							 }else{
								 return "";
							 }
						 }},
    				 {"mDataProp":"ROUTE_FROM"},
    				 {"mDataProp":"ROUTE_TO"},
					 {"mDataProp":"REMARK",
						 "fnRender": function(obj) {
		                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
		                        return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
		                    }else{
		                    	 return "<input type='text' name='remark'>";
		                    }
			         }}, 
    				{"mDataProp":"STATUS","sWidth": "80px","sClass": "status"},
    				{"mDataProp":"COST_SOURCE",
    					"sWidth": "80px",
    					"fnRender":function(obj) {
    						if(obj.aData.CREATE_NAME == 'system'){
				        		return obj.aData.COST_SOURCE;
				        	}else{
				        		return "手工录入费用";
				        	}
    				}},
    				
    				{  
		                "mDataProp": null, 
		                "sWidth": "60px",  
		            	"sClass": "remark",              
		                "fnRender": function(obj) {
		                	if(obj.aData.CREATE_NAME == 'system'){
				        		return "";
				        	}else{
				        		return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
			              		"<i class='fa fa-trash-o fa-fw'> </i> "+
			              		"删除"+
			              		"</a>";
				        	}
		                    
		                }
		            }      
    	        ]      
    	    });
    		//异步删除应付
    		 $("#table_fin2").on('click', '.finItemdel', function(e){
    			 var id = $(this).attr('code');
    			  e.preventDefault();
    			  $.post('/departOrder/finItemdel/'+id,function(data){
    	               //保存成功后，刷新列表
    	               paymenttable.fnDraw();
    	           },'json');
    		 });
    		 
    		//应付
    		$("#addrow").click(function(){	
    			var departId=$("#depart_id").val();
    			$.post('/departOrder/addNewRow/'+departId,function(data){
    			
    				if(data[0] != null){
    					paymenttable.fnSettings().sAjaxSource = "/departOrder/accountPayable/"+departId;
    					paymenttable.fnDraw();
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
    			$.post('/departOrder/updateDepartOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
    				if(data.success){
    					$.scojs_message('金额修改成功', $.scojs_message.TYPE_OK);
    				}else{
    					alert("修改失败!");
    				}
    	    	},'json');
    		});    		

	    $('#datetimepicker').datetimepicker({  
	        format: 'yyyy-MM-dd',  
	        language: 'zh-CN', 
	        autoclose: true,
	        pickerPosition: "bottom-left"
	    }).on('changeDate', function(ev){
	        $(".bootstrap-datetimepicker-widget").hide();
	        $('#departure_time').trigger('keyup');
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
    });