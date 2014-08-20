 $(document).ready(function() {

    $('#menu_assign').addClass('active').find('ul').addClass('in');
	var departOrderStatus=$("#departOrderStatus").val();
    var message=$("#message").val();
    var type=$("#type").val();
    var depart_id=$("#depart_id").val();
    $("#milestoneDepartId").val(depart_id);
    var last_detail_size=$("#last_detail_size").val();
    var hang="";
    
    if(departOrderStatus=='新建'){
        $("#order_edit").attr("disabled",true);
    	$("#departureConfirmationBtn").attr("disabled",false);
    }else if(departOrderStatus=="已发车"||departOrderStatus=="在途"){
    	$("#warehousingConfirmBtn").attr("disabled",false);
    }else if(departOrderStatus=="已发车" && $(departureConfirmationBtn).prop("disabled") == true){
    	$("#receiptBtn").attr("disabled",false);
    }else if(departOrderStatus=="已签收"){
    	$("#receiptBtn").attr("disabled",true);
    	$("#order_hd").attr("disabled",false);
        $("#edit_status").attr("disabled",true);
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
      
     // 列出所有的司机
   	 $('#driverMessage').on('keyup click', function(){
        var inputStr = $('#driverMessage').val();
        $.get('/yh/transferOrder/searchAllDriver', {input:inputStr}, function(data){
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
   	$.get('/yh/transferOrder/searchAllCarInfo', {input:inputStr}, function(data){
   		console.log(data);
   		var carNoList = $("#carNoList");
   		carNoList.empty();
   		for(var i = 0; i < data.length; i++)
   		{
   			carNoList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' carNo='"+data[i].CAR_NO+"' carType='"+data[i].CARTYPE+"' length='"+data[i].LENGTH+"' driver='"+data[i].DRIVER+"' > "+data[i].CAR_NO+"</a></li>");
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
   	     $("#carinfoId").val($(this).attr('id'));
   	 	 $('#carNoMessage').val($(this).attr('carNo'));
   	 	 $('#cartype').val($(this).attr('carType'));
   	 	 $('#carsize').val($(this).attr('length'));	  	 
   	     $('#carNoList').hide();   
       });

   	// 没选中车辆，焦点离开，隐藏列表
   	$('#carNoMessage').on('blur', function(){
    		$('#carNoList').hide();
    });

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
        "bServerSide": true,
        "bDestroy": true,
    	 "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/pickupOrder/getInitPickupOrderItems?localArr="+message+"&tr_item="+tr_item+"&item_detail="+item_detail,
        "aoColumns": [
            { "mDataProp": "CUSTOMER" ,"sWidth": "100%"},
            { "mDataProp": "ORDER_NO" ,"sWidth": "30%"},      
            { "mDataProp": "ITEM_NO"},
            { "mDataProp": "ITEM_NAME"},
            { "mDataProp": "AMOUNT"},
            { "mDataProp": "VOLUME"},
            { "mDataProp": "WEIGHT"},
            { "mDataProp": "REMARK"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success dateilEdit' code='?id="+obj.aData.ID+"'>"+
                                "<i class='fa fa-search fa-fw'></i>"+
                                "查看"+
                            "</a>"+					
                            "<a class='btn btn-danger cancelbutton' code='?id="+obj.aData.TR_ORDER_ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
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
    		detailTable.fnSettings().sAjaxSource = "/yh/departOrder/findAllItemDetail?item_id="+itemId+"&departId="+$("#departOrderId").val();
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
    	         "bServerSide": true, 
    	     	 "oLanguage": {
    	             "sUrl": "/eeda/dataTables.ch.txt"
    	         },
    	         "sAjaxSource": "/yh/departOrder/itemDetailList?item_id="+item_id+"",
    	       
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
    	    
    	    //发车单里程碑
    	    $("#transferOrderMilestoneList").click(function(e){
    	    	if($("#departOrderStatus").val() == '' || $("#departOrderStatus").val() == '新建'){
    	    		clickSaveDepartOrder(e);
    	    	}
    			$.post('/yh/departOrder/transferOrderMilestoneList',{departOrderId:$("#departOrderId").val()},function(data){
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
    			$.post('/yh/departOrder/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
    				var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
    				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
    			},'json');
    			$('#transferOrderMilestone').modal('hide');
    		});
    		
    	    var handlePickkupOrderDetail = function(){
    	    	// 保存单品
    	    	$.post('/yh/departOrder/saveDepartOrder', $("#orderForm").serialize(), function(data){
    				$("#departOrderId").val(data.ID);
    				if(data.ID>0){
    					$("#departOrderId").val(data.ID);
    					$("#depart_id").val(data.ID);
    				  	$("#style").show();	

    		    	    $("#departureConfirmationBtn").attr("disabled", false);
    				}else{
    					alert('数据保存失败。');
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
    	    
    	    //编辑保存
    	    $("#saveDepartOrderBtn").click(function(e){
    	    	if($("#departOrderStatus").val() == '' || $("#departOrderStatus").val() == '新建'){
    	    		clickSaveDepartOrder(e);
    	    	}
    	    });
    	    
    	    // 点击货品信息
    	    $("#departOrderItemList").click(function(e){
    	    	if($("#departOrderStatus").val() == '' || $("#departOrderStatus").val() == '新建'){
    	    		clickSaveDepartOrder(e);
    	    	} 
    	    });
    	    
    	    $("#departureConfirmationBtn").click(function(e){
    	    	//计费方式
    	    	var priceType = $("input[name='priceType']:checked").val();
    	    	$(this).attr("disabled",true);
    	    	$("#order_edit").attr("disabled",true);
    	    	$.post('/yh/departOrder/updatestate?order_state='+"已发车"+"&priceType="+priceType, $("#orderForm").serialize(), function(){
    	    	  $("#warehousingConfirmBtn").attr("disabled",false);
                });
    	    });
    	    $("#warehousingConfirmBtn").click(function(e){
    	    	$(this).attr("disabled",true);
    	    	$.post('/yh/departOrder/updatestate?order_state='+"已入库", $("#orderForm").serialize(), function(data){
    	    		if(data.amount>0){
    	    			alert("有"+data.amount+"个货品没入库,可以到产品中维护信息！");
    	    		}                    
    	    	});
    	    });
    	    $("#receiptBtn").click(function(e){
    	    	$(this).attr("disabled",true);
    	    	$("#order_hd").attr("disabled",false);
    	    	 $("#edit_status").attr("disabled",true);
    	    	$.post('/yh/departOrder/updatestate?order_state='+"已签收", $("#orderForm").serialize(), function(){
    	    	
    	    	});
    	    });
    	    $("#order_hd").click(function(e){
    	    	$(this).attr("disabled",true);
    	    	$.post('/yh/departOrder/CreatReturnOrder?order_id=', $("#orderForm").serialize(), function(data){
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
    						data[i].COMPANY_NAME = '';
    					}
    					var contact_person = data[i].CONTACT_PERSON;
    					if(contact_person == null){
    						data[i].CONTACT_PERSON = '';
    					}
    					var phone = data[i].PHONE;
    					if(phone == null){
    						data[i].PHONE = '';
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
    			$.get('/yh/departOrder/getIntsp', {sp_id:sp_id}, function(data){
    				console.log(data);
    				$('#spMessage').val(data.COMPANY_NAME);
    			},'json');
    		}
    		//会显收货人
    		$.get('/yh/departOrder/ginNotifyPerson', {order_id:message}, function(data){
				console.log(data);
				if(data.ADDRESS==null){
					data.ADDRESS='';
				}
				if(data.CONTACT_PERSON==null){
					data.CONTACT_PERSON='';				
								}
				if(data.PHONE==null){
					data.PHONE='';
				}
				$('#notify_address').text(data.ADDRESS);
				$('#notify_contact_person').text(data.CONTACT_PERSON);
				$('#notify_phone').text(data.PHONE);
			},'json');
    		//是直送显示“确认收货”
    		var check_sh=$("#check_sh").val();
    		if(check_sh==false){
    			$("#receiptBtn").show();
    			$("#order_hd").show();
    		}else{
    			$("#warehousingConfirmBtn").show();
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
    	    
    	    if($("#departOrderArrivalMode").val() == 'gateIn' && $("#departOrderStatus").val() == '已发车'){
    	    	$("#warehousingConfirmBtn").attr("disabled", false);
    	    }
    	    
    	    if($("#departOrderStatus").val() != '' && $("#departOrderStatus").val() != '新建'){
	    		$("#saveDepartOrderBtn").attr("disabled", true);
	    	}
    	    
    	    /*if($("#departOrderId").val() != ''){
    	    	$("#departureConfirmationBtn").attr("disabled", false);
    	    }*/
    	    
    	    $("#cancelBtn").click(function(){
    	    	$("#detailDialog").modal('hide');
    	    });
    	    
    	  //应收应付datatable
    		var paymenttable=$('#table_fin2').dataTable({
    			"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
    	        "bFilter": false, //不需要默认的搜索框
    	        //"sPaginationType": "bootstrap",
    	        "iDisplayLength": 10,
    	        "bServerSide": true,
    	        "sAjaxSource": "/yh/departOrder/accountPayable/"+depart_id,
    	    	"oLanguage": {
    	            "sUrl": "/eeda/dataTables.ch.txt"
    	        },
    	        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
    				$(nRow).attr('id', aData.ID);
    				return nRow;
    			},
    	        "aoColumns": [
    				{"mDataProp":"NAME","sWidth": "80px","sClass": "name"},
    				{"mDataProp":"AMOUNT","sWidth": "80px","sClass": "amount"},  
    				{"mDataProp":"REMARK","sWidth": "80px","sClass": "remark"},
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
    			$.post('/yh/pickupOrder/finItemdel/'+id,function(data){
    	               //保存成功后，刷新列表
    	               console.log(data);
    	               paymenttable.fnDraw();
    	           },'text');
    			  });
    		 
    		paymenttable.makeEditable({
    	    	sUpdateURL: '/yh/departOrder/paymentSave',    	
    	    	oEditableSettings: {event: 'click'},
    	    	"aoColumns": [  			            
    	            {            
    	            	style: "inherit",
    	            	indicator: '正在保存...',
    	            	onblur: 'submit',
    	            	tooltip: '点击可以编辑',
    	            	name:"name",
    	            	placeholder: "", 
    	            	callback: function () {
    	            		
    	            	}
    	        	},
    	            {
    	            	indicator: '正在保存...',
    	            	onblur: 'submit',
    	            	tooltip: '点击可以编辑',
    	            	name:"amount",
    	            	placeholder: "",
    	            	callback: function () {} 
    	            }
    	        ]      
    	    }).click(function(){
    	    	var inputBox = $(this).find('input');
    	        inputBox.autocomplete({
    		        source: function( request, response ) {
    		        	if(inputBox.parent().parent()[0].cellIndex >0){//从第2列开始，不需要去后台查数据
    			    		return;
    			    	}
    		            $.ajax({
    		                url: "/yh/departOrder/getPaymentList",
    		                dataType: "json",
    		                data: {
    		                    input: request.term
    		                },
    		                success: function( data ) {
    		                    response($.map( data, function( data ) {
    		                        return {
    		                            label: data.NAME,
    		                            value: data.NAME,
    		                            id: data.ID,
    		                            name: data.NAME
    		                        };
    		                    }));
    		                }
    		            });
    		        },select: function( event, ui ) {
    	        		//将选择的条目id先保存到数据库
    		        	var finId = $(this).parent().parent().parent()[0].id;
    	        		var finItemId = ui.item.id;
    	        		$.post('/yh/departOrder/paymentSave',{id:finId, finItemId:finItemId},
    	        			function(){ paymenttable.fnDraw();  });        		
    	            },
    	        	minLength: 2
    	        });
    	    }); 
    		
    		$("#addrow").click(function(){	
    			$.post('/yh/departOrder/addNewRow/'+depart_id,function(data){
    				console.log(data);
    				if(data.success){
    					paymenttable.fnDraw();
    					//$('#fin_item2').modal('hide');
    					//$('#resetbutton2').click();
    				}else{
    					
    				}
    			});		
    		});	  
    });