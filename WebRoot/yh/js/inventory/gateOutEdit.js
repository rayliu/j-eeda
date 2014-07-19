$(document).ready(function() {
	$('#reset').hide();
$('#menu_warehouse').addClass('active').find('ul').addClass('in');

var wid = $("#warehouseorderId").val();
//出库产品list
console.log(wid);
productDataTable2 = $('#itemTable').dataTable({
       "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
       //"sPaginationType": "bootstrap",
       "iDisplayLength": 10,
       "bServerSide": true,
       "bRetrieve": true,
   	"oLanguage": {
           "sUrl": "/eeda/dataTables.ch.txt"
       },
       "sAjaxSource":"/yh/gateIn/gateInProductlist2/"+wid,
       "aoColumns": [
           {"mDataProp":"ITEM_NAME"},
           {"mDataProp":"ITEM_NO"},        	
           {"mDataProp":"EXPIRE_DATE"},
           {"mDataProp":"LOT_NO"},
           {"mDataProp":"CATON_NO"},
           {"mDataProp":"TOTAL_QUANTITY"},
           {"mDataProp":"UOM"},
           {"mDataProp":"UNIT_PRICE"},
           {"mDataProp":"UNIT_COST"},
           { 
               "mDataProp": null, 
               "sWidth": "8%",                
               "fnRender": function(obj) {                    
                   return "<a class='btn btn-success editProduct' code='"+obj.aData.ID+"'>"+
		                   "<i class='fa fa-edit fa-fw'></i>"+
			                "</a>"+
		                   "<a class='btn btn-danger deleteProduct' code='"+obj.aData.ID+"'>"+
		                        "<i class='fa fa-trash-o fa-fw'></i>"+ 
		                   "</a>";
               }
           }      
       ],      
   });
//gateInProduct编辑
$("#itemTable").on('click', '.editProduct', function(){
	 var id = $(this).attr('code');
	 $.post('/yh/gateIn/gateInProductEdit/'+id,function(data){
        //保存成功后，刷新列表
        console.log(data);
        if(data!=null){
        		$("#mylabel").text(data.TOTAL_QUANTITY);	
        	 	$("#productId").val(data.PRODUCT_ID);
     	 		$("#warehouseOrderItemId").val(data.ID);
     	 		$("#itemNameMessage").val(data.ITEM_NAME);
    			$("#itemNoMessage").val(data.ITEM_NO);
    			$("#item_desc").val(data.ITEM_DESC);
    			$("#expire_date").val(data.EXPIRE_DATE);
    			$("#lot_no").val(data.LOT_NO); 			
    			$("#total_quantity").val(data.TOTAL_QUANTITY);
    			$("#unit_price").val(data.UNIT_PRICE);
    			$("#unit_cost").val(data.UNIT_COST);
    			$("#uom").val(data.UOM);
    			$("#caton_no").val(data.CATON_NO);
    			$('#myModal').modal('show');
        }else{
            alert('取消失败');
        }
    },'json');
	});
//gateInProduct删除
$("#itemTable").on('click', '.deleteProduct', function(){
	 var id = $(this).attr('code');
	 $.post('/yh/gateIn/gateInProductDelect/'+id,function(data){
        //保存成功后，刷新列表
        console.log(data);
        if(data.success){
       	 productDataTable2.fnDraw();
        }else{
            alert('取消失败');
        }
    },'json');
	});
	//选择仓库 
 $('#warehouseSelect').on('keyup click', function(){
	var inputStr = $('#warehouseSelect').val();
	$.get('/yh/gateIn/searchAllwarehouse', {input:inputStr}, function(data){
		console.log(data);
		var warehouseList =$("#warehouseList");
		warehouseList.empty();
		for(var i = 0; i < data.length; i++)
		{
			warehouseList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</a></li>");
		}
	},'json');
	$("#warehouseList").css({ 
    	left:$(this).position().left+"px", 
    	top:$(this).position().top+32+"px" 
    }); 
    $('#warehouseList').show();
});
$('#warehouseSelect').on('blur', function(){
	$("#warehouseList").hide();
});

// 选中仓库
$('#warehouseList').on('mousedown', '.fromLocationItem', function(e){
	var id =$(this).attr('code');
	$('#warehouseSelect').val($(this).text());
	 $("#warehouseId").val(id);
	//productDataTable.fnSettings().sAjaxSource = "/yh/gateIn/gateInProductlist?categoryId="+partyId;
	//productDataTable.fnDraw();
	$('#customerList').hide();
}); 

//保存出库单
 $("#saveInventoryBtn").click(function(e){
        //阻止a 的默认响应行为，不需要跳转	
	 //var itemId = $("#item_id").val();
        e.preventDefault();
        //异步向后台提交数据
       $.post('/yh/gateIn/gateOutSave',$("#inventoryForm").serialize(), function(data){
            console.log(data);
         if(data>0){
        	 console.log(data);
         	$("#warehouseorderId").val(data);
         	$("#style").show();
         	$("#gateOutConfirmBtn").attr("disabled", false);
         }else{
             alert('数据保存失败。');
         }
         },'json');
    });
//选择客户 
 $('#customerMessage').on('keyup click', function(){
	 	var warehouseId = $('#warehouseId').val();
		var inputStr = $('#customerMessage').val();
		$.get('/yh/gateIn/searchgateOutCustomer', {input:inputStr,warehouseId:warehouseId}, function(data){
			console.log(data);
			var customerList =$("#customerList");
			customerList.empty();
			for(var i = 0; i < data.length; i++)
			{
				customerList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].PID+"'>"+data[i].COMPANY_NAME+"</a></li>");
			}
		},'json');
		$("#customerList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#customerList').show();
	});
 	$('#customerMessage').on('blur', function(){
		$("#customerList").hide();
	});
	// 选中客户
	$('#customerList').on('mousedown', '.fromLocationItem', function(e){
		var partyId =$(this).attr('code');
		$('#customerMessage').val($(this).text());
		$('#party_id').val(partyId);
		//productDataTable.fnSettings().sAjaxSource = "/yh/gateIn/gateInProductlist?categoryId="+partyId;
		//productDataTable.fnDraw();
		$('#customerList').hide();
    }); 
	
	//获取货品的名称list，选中信息在下方展示其他信息
	$('#itemNameMessage').on('keyup click', function(){
		var inputStr = $('#itemNameMessage').val();
		var customerId = $('#party_id').val();
		var warehouseId = $('#warehouseId').val();
		$.get('/yh/gateOut/searchName2', {warehouseId:warehouseId,input:inputStr,customerId:customerId}, function(data){
			console.log(data);
			var itemNameList =$("#itemNameList");
			itemNameList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var item_name = data[i].ITEM_NAME;
				if(item_name == null){
					item_name = '';
				}
				itemNameList.append("<li><a tabindex='-1' class='fromLocationItem' productId='"+data[i].PRODUCT_ID+"' id='"+data[i].ID+"' item_desc='"+data[i].ITEM_DESC+"' item_no='"+data[i].ITEM_NO+"' expire_date='"+data[i].EXPIRE_DATE+"' lot_no='"+data[i].LOT_NO+"' total_quantity='"+data[i].TOTAL_QUANTITY+"' unit_price='"+data[i].UNIT_PRICE+"' unit_cost='"+data[i].UNIT_COST+"' uom='"+data[i].UOM+"', caton_no='"+data[i].CATON_NO+"', >"+data[i].ITEM_NAME+"</a></li>");
			}
		},'json');		
		$("#itemNameList").css({ 
			left:$(this).position().left+"px", 
			top:$(this).position().top+32+"px" 
		}); 
		$('#itemNameList').show();        
	});
	$('#itemNameMessage').on('blur', function(){
	$("#itemNameList").hide();
});
// 选中产品名
	$('#itemNameList').on('mousedown', '.fromLocationItem', function(e){
		$("#itemNameMessage").val($(this).text());
		if($(this).attr('item_no') == 'null'){
			$("#item_no").val('');
		}else{
			$("#itemNoMessage").val($(this).attr('item_no'));
		}
		if($(this).attr('item_desc') == 'undefined'){
			$("#item_desc").val('');
		}else{
			$("#item_desc").val($(this).attr('item_desc'));
		}
			$("#expire_date").val($(this).attr('expire_date'));
			$("#lot_no").val($(this).attr('lot_no')); 			
			//$("#total_quantity").val($(this).attr('total_quantity'));
			$("#unit_price").val($(this).attr('unit_price'));
			$("#unit_cost").val($(this).attr('unit_cost'));
			$("#uom").val($(this).attr('uom'));
			$("#caton_no").val($(this).attr('caton_no'));
			$("#productId").val($(this).attr('productId'));
			$("#total").val($(this).attr('total_quantity'));
			$("#mylabel").text("(0~"+$(this).attr('total_quantity')+")");
			
		$('#itemNameList').hide();
	});  	
	
	//获取货品的序列号list，选中信息在下方展示其他信息
	$('#itemNoMessage').on('keyup click', function(){
		var inputStr = $('#itemNoMessage').val();
		var customerId = $('#party_id').val();
		var warehouseId = $('#warehouseId').val();
		$.get('/yh/gateOut/searchNo2', {warehouseId:warehouseId,input:inputStr,customerId:customerId}, function(data){
			console.log(data);
			var itemNoList =$("#itemNoList");
			itemNoList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var item_no = data[i].ITEM_NO;
				if(item_no == null){
					item_no = '';
				}
				itemNoList.append("<li><a tabindex='-1' class='fromLocationItem' productId='"+data[i].PRODUCT_ID+"' id='"+data[i].ID+"' item_desc='"+data[i].ITEM_DESC+"' item_name='"+data[i].ITEM_NAME+"' expire_date='"+data[i].EXPIRE_DATE+"' lot_no='"+data[i].LOT_NO+"' total_quantity='"+data[i].TOTAL_QUANTITY+"' unit_price='"+data[i].UNIT_PRICE+"' unit_cost='"+data[i].UNIT_COST+"' uom='"+data[i].UOM+"', caton_no='"+data[i].CATON_NO+"', >"+data[i].ITEM_NO+"</a></li>");
			}
		},'json');		
     $("#itemNoList").css({ 
     	left:$(this).position().left+"px", 
     	top:$(this).position().top+32+"px" 
     }); 
     $('#itemNoList').show();        
	});
	$('#itemNoMessage').on('blur', function(){
	$("#itemNoList").hide();
});
// 选中序列号
	$('#itemNoList').on('mousedown', '.fromLocationItem', function(e){
		$("#itemNoMessage").val($(this).text());
		if($(this).attr('item_name') == 'null'){
			$("#item_name").val('');
		}else{
			$("#itemNameMessage").val($(this).attr('item_name'));
		}
		if($(this).attr('item_desc') == 'undefined'){
			$("#item_desc").val('');
		}else{
			$("#item_desc").val($(this).attr('item_desc'));
		}
 			$("#expire_date").val($(this).attr('expire_date'));
 			$("#lot_no").val($(this).attr('lot_no')); 			
 			//$("#total_quantity").val($(this).attr('total_quantity'));
 			$("#unit_price").val($(this).attr('unit_price'));
 			$("#unit_cost").val($(this).attr('unit_cost'));
 			$("#uom").val($(this).attr('uom'));
 			$("#caton_no").val($(this).attr('caton_no'));
 			$("#productId").val($(this).attr('productId'));
 			$("#total").val($(this).attr('total_quantity'));
 			$("#mylabel").text("(0~"+$(this).attr('total_quantity')+")");
    $('#itemNoList').hide();
 }); 
	
// 保存货品
$("#warehouseOrderItemFormBtn").click(function(){
	console.log($("#total").val());
	if(!$("#warehouseOrderItemForm").valid())
    	return;
	var warehouseorderid = $("#warehouseorderId").val();
	$.post('/yh/gateIn/savewareOrderItem/'+warehouseorderid,$("#warehouseOrderItemForm").serialize(), function(id){
			//保存成功后，刷新列表
            console.log(id);
            if(id>0){
            	$('#myModal').modal('hide');
            	var warehouseorderid = $("#warehouseorderId").val();
            	productDataTable2.fnSettings().sAjaxSource = "/yh/gateIn/gateInProductlist/"+warehouseorderid;
            	productDataTable2.fnDraw();
            	$('#reset').click();
            }else{
                alert('保存失败！。');
            }
	},'json');
});

//校验出仓货品数量
$('#warehouseOrderItemForm').validate({
    rules: {
    	total_quantity: {
        required: true,
        //range:[0,$("#total").val()]  
		},
     },
   
});

// 出库确认
$("#gateOutConfirmBtn").click(function(){
	var orderType=$("input[name=orderType]").val();
	
	var warehouseorderid = $("#warehouseorderId").val();
	$.post('/yh/gateIn/gateOutConfirm/'+warehouseorderid,{orderType:orderType},function(data){
		 if(data.success){
			 $("#gateOutConfirmBtn").attr("disabled", true);
			 alert("出库成功！");
            }else{
                alert('失败！。');
            }
	},'json');
	
});
	var wStatus = $("#warehouseorderStatus").val();
	console.log(wStatus);
	if(wStatus=='已出库'||wStatus==''){
	   $("#gateOutConfirmBtn").attr("disabled", true); 
	}
	//reset
	 $("#addproduct").click(function(){
		 $('#reset').click();
	 });
	 
	 $("#transferOrderItemList").click(function(e){
	    	if($("#warehouseorderId").val() == ''){
		    	e.preventDefault();
		    	// 切换到货品明细时,应先保存运输单
		    	//提交前，校验数据
		        	alert("请先保存出库单!");
			       	return false; 
		        
	    	}
	   });
});