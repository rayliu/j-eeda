 $(document).ready(function() {
        $('#menu_assign').addClass('active').find('ul').addClass('in');
       
    	var status=$("#status").val();
        var message=$("#message").val();
        var type=$("#type").val();
        var depart_id=$("#depart_id").val();
        $("#milestoneDepartId").val(depart_id);
        var last_detail_size=$("#last_detail_size").val();
        var hang="";
        if(status!="新建"){
        	$("#order_edit").attr("disabled",true);
        	//$("#order_fc").attr("disabled",false);
        }
        if(status=='新建'){
        	$("#order_fc").attr("disabled",false);
        }
        if(status=="已发车"||status=="在途"){
        	$("#order_rk").attr("disabled",false);
        }
        if(status=="已入库"&&$(order_rk).prop("disabled") == true){
        	$("#order_sh").attr("disabled",false);
        }
        if(status=="已签收"){
        	$("#order_sh").attr("disabled",true);
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
      
      $('#customerMessage').on('keyup click', function(){
  		var inputStr = $('#customerMessage').val();
  		$('#driverId').val("");
  		if(inputStr == ""){
  			$('#phone').val($(this).attr(""));
  		}
  		$.get('/yh/transferOrder/searchAllDriver', {input:inputStr}, function(data){
  			console.log(data);
  			var customerList =$("#customerList");
  			customerList.empty();
  			for(var i = 0; i < data.length; i++)
  			{
  				customerList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' carNo='"+data[i].CAR_NO+"' carType='"+data[i].CARTYPE+"' length='"+data[i].LENGTH+"' phone='"+data[i].PHONE+"' driver='"+data[i].DRIVER+"' > "+data[i].DRIVER+" "+data[i].PHONE+" "+data[i].CAR_NO+"</a></li>");
  			}
  		},'json');
  		
  		$("#customerList").css({ 
          	left:$(this).position().left+"px", 
          	top:$(this).position().top+32+"px" 
          }); 
          $('#customerList').show();
  	});
  	
  	// 选中客户
  	$('#customerList').on('keyup click', '.fromLocationItem', function(e){
  		var message = $(this).text();
  		$('#driverId').val($(this).attr('id'));
  		$('#customerMessage').val($(this).attr('driver'));
  		$('#phone').val($(this).attr('phone'));
  		$('#carsize').val($(this).attr('length'));
  		$('#cartype').val($(this).attr('carType'));
  		$('#car_no').val($(this).attr('carNo'));
          $('#customerList').hide();
          
         
      }); 
  	//显示货品table
    	var datatable = $('#eeda-table').dataTable({
            //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
            "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
            //"sPaginationType": "bootstrap",
            "iDisplayLength": 10,
            "bServerSide": true,
            "bDestroy": true,
        	"oLanguage": {
                "sUrl": "/eeda/dataTables.ch.txt"
            },
            "sAjaxSource": "/yh/departOrder/getIintDepartOrderItems?localArr="+message,
            "aoColumns": [
                { "mDataProp": "CUSTOMER" ,"sWidth": "100%"},
                { "mDataProp": "ORDER_NO" ,"sWidth": "30%"},
                { "mDataProp": "WAREHOUSE_NAME"},
                { "mDataProp": "ITEM_NO"},
                { "mDataProp": "ITEM_NAME"},//co.contact_person ,co.mobile ,co.address
                { "mDataProp": "AMOUNT"},
                { "mDataProp": "VOLUME"},
                { "mDataProp": "WEIGHT"},
                { "mDataProp": "REMARK"},
                { "mDataProp": "CONTACT_PERSON"},
                { "mDataProp": "MOBILE"},
                { "mDataProp": "ADDRESS"},
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
    	var tr_itemid_list=[];//运输单货品id字符串
 	    var item_detail_id=[];//运输单单品id字符串
 	    var check_detail_id=[];//单品对比字符串
 	    var item_id="";
    	// 查看货品
    		$("#eeda-table").on('click', '.dateilEdit', function(e){
    			e.preventDefault();
    			 var even_detail_id=[];
    			/*保存上一次勾选单品id,货品id*/
    			 $("table tr:not(:first)").each(function(){ 
     	         	$("input",this).each(function(){
     	         		var code=$(this).val();
     	         		var detail_id=code.substring(code.indexOf('{')+1,code.indexOf(','));
     	         	  even_detail_id.push(detail_id);
     	         	});          		
 	         	}); 
    			 
    			 $("table tr:not(:first)").each(function(){ 
    	         	$("input:checked",this).each(function(){
    	         	var code=$(this).val();
    	         	var detail_id=code.substring(code.indexOf('{')+1,code.indexOf(','));
    	         	    item_id=code.substring(code.indexOf(',')+1,code.indexOf('}'));
    	         	   if(check_detail_id.length>0){
    	         	    	for(var g=0;g<check_detail_id.length;g++){
    	         	    		for(var h=0;h<check_detail_id[g].length;h++){
    	         	    			if(check_detail_id[g][h]==detail_id){
    	         	    				for(var j=0;j<check_detail_id[g].length;){
   	         	    						var index=item_detail_id.indexOf(check_detail_id[g][j]);
   	         	    						item_detail_id.splice(index,1);
    	         	    					check_detail_id[g].splice(j,1);	
    	         	    					j=j-j;
    	         	    				}
    	         	    			}
    	         	    			
    	         	    		}
    	         	    	}
    	         	    }
    	         	  item_detail_id.push(detail_id);         	 
    	         	
    	         	});          		
    	         	}); 
    			 check_detail_id.push(even_detail_id);
    	    	 tr_itemid_list.push(item_id);
    	    	 if(tr_itemid_list.length>0){
    	    		 for(var i=0;i<tr_itemid_list.length;){
    	    			 var size=0;
 	         	    	for(var j=0;j<tr_itemid_list.length;j++){
 	         	    		if(tr_itemid_list[j]==item_id){
 	         	    			size++;
 	         	    		}
 	         	    	}
 	         	    	if(size>1){
 	         	    		tr_itemid_list.splice(i,1);
 	         	    		i=i-i;
 	         	    		continue;
 	         	    	};
 	         	    	i++;
 	         	    };
    	    	 };
    	    	
    	    	$("#item_detail").val(item_detail_id);
    	    	$("#tr_itemid_list").val(tr_itemid_list);
    			//根据货品id显示对应单品
    			$("#transferOrderItemDateil").show();
    			var code = $(this).attr('code');
    			var itemId = code.substring(code.indexOf('=')+1);
    			$("#item_id").val(itemId);
    			$("#item_save").attr("disabled", false);
    			$("#style").hide();
    			detailTable.fnSettings().sAjaxSource = "/yh/departOrder/itemDetailList?item_id="+itemId+"&depart_id="+depart_id;
    			detailTable.fnDraw();  
    		});
    		// 删除货品
    		$("#eeda-table").on('click', '.cancelbutton', function(e){
    			e.preventDefault();		
    			 var code = $(this).attr('code');
    			var itemId = code.substring(code.indexOf('=')+1);
    			 $("table tr:eq("+hang+")").remove(); 
    		});
    		var item_id = $("#item_id").val();
    		var detailTable= $('#detailTable').dataTable({           
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",  
                "iDisplayLength": 10,
                "bServerSide": true, 
            	"oLanguage": {
                    "sUrl": "/eeda/dataTables.ch.txt"
                },
               "sAjaxSource": "/yh/departOrder/itemDetailList?item_id="+item_id+"&depart_id="+depart_id,
              
              "aoColumns": [
                     { "mDataProp": null,
                       "fnRender": function(obj) {
                    	  
                    	   var Returnhtml_one="<input type='checkbox' class='checkedOrUnchecked' name='order_check_box' checked='checked' value={"+obj.aData.ID+","+obj.aData.ITEM_ID+"}>";
                    	   var Returnhtml_two="<input type='checkbox' class='checkedOrUnchecked' name='order_check_box'  value={"+obj.aData.ID+","+obj.aData.ITEM_ID+"}>";
                    	   if(item_detail_id.length>0){
                    		   var size=0;
                    		   for(var i=0;i<item_detail_id.length;i++){
                        		   if(item_detail_id[i]==obj.aData.ID){
                        			   size++;
                        		   }
                        	   }
                    		   if(size>=1){
                    			   return Returnhtml_one;
                    		   }else{
                    			   return Returnhtml_two;
                    		   }
                    	   }else{
                    		   return Returnhtml_one;
                    	   }
                    	  // return Returnhtml_one;
                          
                      }
                     },
                    { "mDataProp": "ITEM_NAME"},      
                    { "mDataProp": "ITEM_NO"},
                    { "mDataProp": "SERIAL_NO"},
                    { "mDataProp": "VOLUME"},
                    { "mDataProp": "WEIGHT"},
                    { "mDataProp": "REMARK"},
                ],
                "fnInitComplete": function(oSettings, json) {
                	//设置checkbok 选中  
                }       
              
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
    	    // 回显车长
    	   /* var carSizeOption=$("#carsize>option");
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
    	    }*/
    	    //单机tab里程碑
    	    $("#transferOrderMilestoneList").click(function(e){
    			$.post('/yh/departOrder/transferOrderMilestoneList',{departOrderId:depart_id},function(data){
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
    			if(depart_id==false){
    				alert("请先保存发车单！");
    				return;
    			}
    			$.post('/yh/departOrder/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
    				var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
    				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
    			},'json');
    			$('#transferOrderMilestone').modal('hide');
    		});
    	
        	   //保存发车单
    	    $("#order_save").click(function(e){
    	        e.preventDefault();
    	        if(!$("#orderForm").valid()){
    		       	return false;
    	        }
    	        $("table tr:not(:first)").each(function(){ 
    	         	$("input:checked",this).each(function(){
    	         	var code=$(this).val();
    	         	var detail_id=code.substring(code.indexOf('{')+1,code.indexOf(','));
    	         	    item_id=code.substring(code.indexOf(',')+1,code.indexOf('}'));
    	         		item_detail_id.push(detail_id);
    	         		 for(var i=0;i<item_detail_id.length;){
    	         			 var size=0;
    	 	         	    	for(var j=0;j<item_detail_id.length;j++){
    	 	         	    		if(item_detail_id[j]==detail_id){
    	 	         	    			size++;
    	 	         	    		};
    	 	         	    	}
    		         	    	if(size>1){
    		         	    		item_detail_id.splice(i,1);
    		         	    		i=i-i;
    		         	    		continue;
    		         	    	};
    		         	    	i++;
    		         	    };
    	         	});          		
    	         	}); 
    	    	 tr_itemid_list.push(item_id);
    	    	 for(var i=0;i<tr_itemid_list.length;){
    	    		 var size=0;
	         	    	for(var j=0;j<tr_itemid_list.length;j++){
	         	    		if(tr_itemid_list[j]==item_id){
	         	    			size++;
	         	    		}
	         	    	}
	         	    	if(size>1){
	         	    		tr_itemid_list.splice(i,1);
	         	    		i=i-i;
	         	    		continue;
	         	    	};
	         	    	i++;
	         	    };
	         	
    	    	$("#item_detail").val(item_detail_id);
    	    	$("#tr_itemid_list").val(tr_itemid_list);
    	    	var getIindepart_no=$("#getIin_depart_no").text();
    	    	$("#getIindepart_no").val(getIindepart_no);
    	       $('#orderForm').submit();
    	    });
    	    
    	    $("#box_one_edit").click(function(e){
    	    	 $('#boxoneForm').submit();
    	    });
    	    $("#box_two_edit").click(function(e){
   	    	 $('#boxtwoForm').submit();
    	    });
    	    $("#box_two_config").click(function(e){
    	    	$("#box_two").modal('hide');
      	    });
    	    //编辑保存
    	    $("#order_edit").click(function(e){
    	    	$(this).attr("disabled",true);
    	    	$.post('/yh/departOrder/savedepartOrder', $("#orderForm").serialize(), function(dp){
    	    		
        	    	$("#style").show();
        	    	if(dp.STATUS=="新建"||dp.STATUS=="在途"){
        	    		$("#order_fc").attr("disabled",false);
        	    	}
        	    	
    	    	});
    	    	
    	    });
    	    $("#order_fc").click(function(e){
    	    	$(this).attr("disabled",true);
    	    	$("#order_edit").attr("disabled",true);
    	    	$.post('/yh/departOrder/updatestate?order_state='+"已发车", $("#orderForm").serialize(), function(){
    	    	
    	    	$("#order_rk").attr("disabled",false);
    	    	});
    	    });
    	    $("#order_rk").click(function(e){
    	    	$(this).attr("disabled",true);
    	    	$.post('/yh/departOrder/updatestate?order_state='+"已入库", $("#orderForm").serialize(), function(){
    	    	
    	    	$("#order_sh").attr("disabled",false);
    	    	});
    	    });
    	    $("#order_sh").click(function(e){
    	    	$(this).attr("disabled",true);
    	    	 $("#edit_status").attr("disabled",true);
    	    	$.post('/yh/departOrder/updatestate?order_state='+"已签收", $("#orderForm").serialize(), function(){
    	    	
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
    		$('#spList').on('click', '.fromLocationItem', function(e){
    			var message = $(this).text();
    			$('#spMessage').val(message.substring(0, message.indexOf(" ")));
    			$('#sp_id').val($(this).attr('partyId'));
    			
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
				$('#notify_address').text(data.ADDRESS);
				$('#notify_contact_person').text(data.CONTACT_PERSON);
				$('#notify_phone').text(data.MOBILE);
			},'json');
    		//回显司机信息
    		$.get('/yh/departOrder/ginDriver', {depart_id:depart_id}, function(data){
				console.log(data);
				$('#driverId').val(data.ID);
		  		$('#customerMessage').val(data.DRIVER);
		  		$('#phone').val(data.PHONE);
		  		$('#carsize').val(data.LENGTH);
		  		$('#cartype').val(data.CARTYPE);
		  		$('#car_no').val(data.CAR_NO);
			},'json');
    });