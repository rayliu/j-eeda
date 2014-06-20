$(document).ready(function() {
	 $('#menu_assign').addClass('active').find('ul').addClass('in');
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

	 var message=$("#message").val();
     var type=$("#type").val();
     var depart_id=$("#depart_id").val();
     var tr_item=$("#tr_itemid_list").val();
     var item_detail=$("#item_detail").val();
 	 //显示货品table
 	 var datatable = $('#pickupItem-table').dataTable({
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
	$("#pickupItem-table").on('click', '.dateilEdit', function(e){
		e.preventDefault();
		
		$("#transferOrderItemDateil").show();
		 var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		tr_itemid_list.push(itemId);
		$("#item_id").val(itemId);
		$("#item_save").attr("disabled", false);
		$("#style").hide();
		detailTable.fnSettings().sAjaxSource = "/yh/departOrder/itemDetailList?item_id="+itemId+"";
		detailTable.fnDraw();  			
	});
	
	// 删除货品
	$("#pickupItem-table").on('click', '.cancelbutton', function(e){
		e.preventDefault();		
		 var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		 $("table tr:eq("+hang+")").remove(); 
	});
	var the_id="";
	var item_id = $("#item_id").val();
	var detailTable= $('#pickupDetail-table').dataTable({           
         "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",  
         "iDisplayLength": 10,
         "bServerSide": true, 
     	 "oLanguage": {
             "sUrl": "/eeda/dataTables.ch.txt"
         },
         "sAjaxSource": "/yh/departOrder/itemDetailList?item_id="+item_id+"",
       
         "aoColumns": [
              { "mDataProp": null,
                /*"fnRender": function(obj) {
             	   the_id=obj.aData.ID;
                    return '<input type="checkbox" name="order_check_box" value="'+obj.aData.ID+'">';
                }*/
              },
             { "mDataProp": "ITEM_NAME"},      
             { "mDataProp": "ITEM_NO"},
             { "mDataProp": "SERIAL_NO"},
             { "mDataProp": "VOLUME"},
             { "mDataProp": "WEIGHT"},
             { "mDataProp": "REMARK"},           
         ]        
     });
	
    //选择单品保存
    var item_detail_id=[];
    $("#item_save").click(function(){
    	
    	 $("table tr:not(:first)").each(function(){
    	        
         	$("input:checked",this).each(function(){
         		item_detail_id.push($(this).val());         	
         	});          		
         }); 
    	$("#item_detail").val(item_detail_id);
    	$("#tr_itemid_list").val(tr_itemid_list);
    	$("#style").show();
    	$("#item_save").attr("disabled", true);
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

    //获取全国省份
    $(function(){
     	var province = $("#mbProvinceFrom");
     	$.post('/yh/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
			var hideProvince = $("#hideProvince").val();
     		for(var i = 0; i < data.length; i++)				{
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
			var hideProvince = $("#hideProvince").val();
     		for(var i = 0; i < data.length; i++){
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
    
    //点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveTransferOrderBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
        if($("#pickupOrderId").val() == ""){
	    	$.post('/yh/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
				$("#pickupOrderId").val(data.ID);
				if(data.ID>0){
				  	$("#style").show();	             
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }else{
        	$.post('/yh/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
				$("#pickupOrderId").val(data.ID);
				if(data.ID>0){					
				  	$("#style").show();	            
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }
	});
} );
