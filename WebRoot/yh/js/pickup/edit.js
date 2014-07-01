$(document).ready(function() {
	 $('#menu_assign').addClass('active').find('ul').addClass('in');
	 // 列出所有的司机
	 $('#driverMessage').on('keyup click', function(){
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

    //点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveTransferOrderBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
        if($("#pickupOrderId").val() == ""){
	    	$.post('/yh/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
				$("#pickupOrderId").val(data.ID);
				$("#milestonePickupId").val(data.ID);
				if(data.ID>0){
				  	$("#style").show();	             
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }else{
        	$.post('/yh/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
				$("#pickupOrderId").val(data.ID);
				$("#milestonePickupId").val(data.ID);
				if(data.ID>0){					
				  	$("#style").show();	            
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }
        $("#finishBtn").attr('disabled', false);
	});
   
 	var pickupOrderMilestone = function(){
 		var pickupOrderId = $("#pickupOrderId").val();
		$.post('/yh/transferOrderMilestone/transferOrderMilestoneList',{pickupOrderId:pickupOrderId},function(data){
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
		pickupOrderMilestone();
	});

	// 保存新里程碑
	$("#transferOrderMilestoneFormBtn").click(function(){
		$.post('/yh/transferOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$('#transferOrderMilestone').modal('hide');
	});
	
	// 点击已完成按钮
	$("#finishBtn").click(function(){
		var pickupOrderId = $("#pickupOrderId").val();
		$.post('/yh/pickupOrder/finishPickupOrder', {pickupOrderId:pickupOrderId}, function(){
			pickupOrderMilestone();
		},'json');
	});
	
	// 列出所有的提货地点
	$("#addressList").click(function(){
		var pickupOrderId = $("#pickupOrderId").val();
		$.post('/yh/pickupOrder/findAllAddress', {pickupOrderId:pickupOrderId}, function(data){
			var pickupAddressTbody = $("#pickupAddressTbody");
			pickupAddressTbody.empty();
			for(var i=0;i<data.length;i++){
				pickupAddressTbody.append("<tr value='"+data[i].POSITION+"'><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td><a href='javascript:void(0)' class='moveUp'>上移</a> <a href='javascript:void(0)' class='moveDown'>下移</a> <a href='javascript:void(0)' class='moveTop'>移至顶部</a> <a href='javascript:void(0)' class='moveButtom'>移至底部</a></td></tr>");					
				/*if(i == 0){
					pickupAddressTbody.append("<tr><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td><a href='javascript:void(0)'>上移</a> <a href='javascript:void(0)'>下移</a> <a href='javascript:void(0)'>移至顶部</a> <a href='javascript:void(0)'>移至底部</a></td></tr>");					
				}else if(i == data.length-1){
					pickupAddressTbody.append("<tr><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td><a href='javascript:void(0)'>上移</a> <a href='javascript:void(0)'>下移</a> <a href='javascript:void(0)'>移至顶部</a> <a href='javascript:void(0)'>移至底部</a></td></tr>");										
				}else{
					pickupAddressTbody.append("<tr><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td><a href='javascript:void(0)'>上移</a> <a href='javascript:void(0)'>下移</a> <a href='javascript:void(0)'>移至顶部</a> <a href='javascript:void(0)'>移至底部</a></td></tr>");					
				}*/
			}
		},'json');
	});
	

	$("#pickupAddressTbody").on('click', '.moveUp', function(e){
		var currentNode = $(this).parent().parent();
		var currentVal = currentNode.attr("value");
		var prevNode = currentNode.prev();
		var prevVal = prevNode.attr("value");
		currentNode.attr("value", prevVal);
		prevNode.attr("value", currentVal);
	});
} );
