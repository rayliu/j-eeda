$(document).ready(function() {
	$('#reset').hide();
	$('#menu_warehouse').addClass('active').find('ul').addClass('in');

	var wid = $("#warehouseorderId").val();
	//出库产品list
	productDataTable2 = $('#itemTable').dataTable({
       "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
       //"sPaginationType": "bootstrap",
       "iDisplayLength": 10,
       "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
       "bServerSide": true,
       "bRetrieve": true,
   			"oLanguage": {
           "sUrl": "/eeda/dataTables.ch.txt"
       },
       "sAjaxSource":"/gateIn/gateInProductlist2/"+wid,
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
	 $.post('/gateIn/gateInProductEdit/'+id,function(data){
        //保存成功后，刷新列表
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
	 $.post('/gateIn/gateInProductDelect/'+id,function(data){
        //保存成功后，刷新列表
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
	$.get('/gateIn/searchAllwarehouse', {input:inputStr}, function(data){
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

 	$('#warehouseList').on('blur', function(){
		$('#warehouseList').hide();
	});

	 $('#warehouseList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	 });
	// 选中仓库
	$('#warehouseList').on('mousedown', '.fromLocationItem', function(e){
		var id =$(this).attr('code');
		$('#warehouseSelect').val($(this).text());
		 $("#warehouseId").val(id);
		//productDataTable.fnSettings().sAjaxSource = "/gateIn/gateInProductlist?categoryId="+partyId;
		//productDataTable.fnDraw();
		$('#warehouseList').hide();
	});
/*--------------------------------------------------------------------*/
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable">'+
				'<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
				'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
				'</div>';
	$('body').append(alerMsg);
	
	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	
	$("#transferOrderMilestoneList").click(function(e){
		e.preventDefault();
		
		parentId = e.target.getAttribute("id");
	});
	$("#transferOrderarap").click(function(e){
		e.preventDefault();
		parentId = e.target.getAttribute("id");
	});

	//保存出库单
	$("#saveInventoryBtn").click(function(e){
        //阻止a 的默认响应行为，不需要跳转	
	 //var itemId = $("#item_id").val();
        e.preventDefault();
        //异步向后台提交数据
       $.post('/gateIn/gateOutSave',$("#inventoryForm").serialize(), function(data){

         if(data>0){
         	$("#warehouseorderId").val(data);
         	//$("#style").show();
         	$("#gateOutConfirmBtn").attr("disabled", false);
         	if("transferOrderbasic" ==parentId || "saveInventoryBtn"==e.target.getAttribute("id")){
         		contactUrl("gateOutEdit?id",data);
         		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
         	}
         	
         }else{
             $.scojs_message('数据保存失败', $.scojs_message.TYPE_ERROR);
         }
         },'json');
    });
	//选择客户 
	$('#customerMessage').on('keyup click', function(){
	 	var warehouseId = $('#warehouseId').val();
		var inputStr = $('#customerMessage').val();
		$.get('/gateIn/searchgateOutCustomer', {input:inputStr,warehouseId:warehouseId}, function(data){
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

 	$('#customerList').on('blur', function(){
 			$('#customerList').hide();
 		});

 	$('#customerList').on('mousedown', function(){
 		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
 	});
	// 选中客户
	$('#customerList').on('mousedown', '.fromLocationItem', function(e){
		var partyId =$(this).attr('code');
		$('#customerMessage').val($(this).text());
		$('#party_id').val(partyId);
		//productDataTable.fnSettings().sAjaxSource = "/gateIn/gateInProductlist?categoryId="+partyId;
		//productDataTable.fnDraw();
		$('#customerList').hide();
    }); 
	
	//获取货品的名称list，选中信息在下方展示其他信息
	$('#itemNameMessage').on('keyup click', function(){
		var inputStr = $('#itemNameMessage').val();
		var customerId = $('#party_id').val();
		var warehouseId = $('#warehouseId').val();
		$.get('/gateOut/searchName2', {warehouseId:warehouseId,input:inputStr,customerId:customerId}, function(data){
			var itemNameList =$("#itemNameList");
			itemNameList.empty();
			for(var i = 0; i < data.length; i++)//test
			{
				var item_name = data[i].ITEM_NAME;
				if(item_name == null||item_name==''){
					item_name =data[i].ITEM_NO ;
				}
				//需要对元素尽心是否为空的判断
				var item_desc = data[i].ITEM_DESC;
				if(item_desc == null){
					item_desc = '';
				}
				var item_no = data[i].ITEM_NO;
				if(item_no == null){
					item_no = '';
				}
				var expire_date = data[i].EXPIRE_DATE;
				if(expire_date == null){
					expire_date = '';
				}
				var lot_no = data[i].LOT_NO;
				if(lot_no == null){
					lot_no = '';
				}
				var item_no = data[i].ITEM_NO;
				if(item_no == null){
					item_no = '';
				}
				var unit_price = data[i].UNIT_PRICE;
				if(unit_price == null){
					unit_price = '';
				}
				var unit_cost = data[i].UNIT_COST;
				if(unit_cost == null){
					unit_cost = '';
				}
				var caton_no = data[i].CATON_NO;
				if(caton_no == null){
					caton_no = '';
				}
				itemNameList.append("<li><a tabindex='-1' class='fromLocationItem' productId='"+data[i].PRODUCT_ID+"' id='"+data[i].ID+"' item_desc='"+item_desc+"' item_no='"+item_no+"' expire_date='"+expire_date+"' lot_no='"+lot_no+"' total_quantity='"+data[i].TOTAL_QUANTITY+"' unit_price='"+unit_price+"' unit_cost='"+unit_cost+"' uom='"+data[i].UOM+"', caton_no='"+caton_no+"', >"+item_name+"</a></li>");
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

 	$('#itemNameList').on('blur', function(){
 			$('#itemNameList').hide();
 		});

 	$('#itemNameList').on('mousedown', function(){
 		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
 	});
 	// 选中产品名
	$('#itemNameList').on('mousedown', '.fromLocationItem', function(e){
		$("#itemNameMessage").val($(this).text());
		if($(this).attr('item_no') == 'null'){
			$("#item_no").val('');
		}else{
			$("#itemNoMessage").val($(this).attr('item_no'));
		}
		if($(this).attr('item_desc') == 'undefined' || $(this).attr('item_desc') == "null"){
			$("#item_desc").val('');
		}else{
			$("#item_desc").val($(this).attr('item_desc'));
		}
		if($(this).attr('expire_date') == "null"){
			$("#expire_date").val('');
		}else{
			$("#expire_date").val($(this).attr('expire_date'));
		}
			
			$("#lot_no").val($(this).attr('lot_no')); 	
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
		$.get('/gateOut/searchNo2', {warehouseId:warehouseId,input:inputStr,customerId:customerId}, function(data){
			var itemNoList =$("#itemNoList");
			itemNoList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var item_no = data[i].ITEM_NO;
				if(item_no == null){
					item_no = '';
				}
			
				var item_name = data[i].ITEM_NAME;
				if(item_name == null){
					item_name = '';
				}
				//需要对元素尽心是否为空的判断
				var item_desc = data[i].ITEM_DESC;
				if(item_desc == null){
					item_desc = '';
				}
				var expire_date = data[i].EXPIRE_DATE;
				if(expire_date == null){
					expire_date = '';
				}
				var lot_no = data[i].LOT_NO;
				if(lot_no == null){
					lot_no = '';
				}
				var item_no = data[i].ITEM_NO;
				if(item_no == null){
					item_no = '';
				}
				var unit_price = data[i].UNIT_PRICE;
				if(unit_price == null){
					unit_price = '';
				}
				var unit_cost = data[i].UNIT_COST;
				if(unit_cost == null){
					unit_cost = '';
				}
				var caton_no = data[i].CATON_NO;
				if(caton_no == null){
					caton_no = '';
				}
				itemNoList.append("<li><a tabindex='-1' class='fromLocationItem' productId='"+data[i].PRODUCT_ID+"' id='"+data[i].ID+"' item_desc='"+item_desc+"' item_name='"+item_name+"' expire_date='"+expire_date+"' lot_no='"+lot_no+"' total_quantity='"+data[i].TOTAL_QUANTITY+"' unit_price='"+unit_price+"' unit_cost='"+unit_cost+"' uom='"+data[i].UOM+"', caton_no='"+caton_no+"', >"+item_no+"</a></li>");
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
	$('#itemNoList').on('blur', function(){
			$('#itemNoList').hide();
		});

	$('#itemNoList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
// 选中序列号
	$('#itemNoList').on('mousedown', '.fromLocationItem', function(e){
		$("#itemNoMessage").val($(this).text());
		if($(this).attr('item_name') == 'null'){
			$("#item_name").val('');
		}else{
			$("#itemNameMessage").val($(this).attr('item_name'));
		}
		if($(this).attr('item_desc') == 'undefined' || $(this).attr('item_desc') == "null"){
			$("#item_desc").val('');
		}else{
			$("#item_desc").val($(this).attr('item_desc'));
		}
		if($(this).attr('expire_date') == "null"){
			$("#expire_date").val('');
		}else{
			$("#expire_date").val($(this).attr('expire_date'));
		}
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
	if(!$("#warehouseOrderItemForm").valid())
    	return;
	var warehouseorderid = $("#warehouseorderId").val();
	$.post('/gateIn/savewareOrderItem/'+warehouseorderid,$("#warehouseOrderItemForm").serialize(), function(id){
			//保存成功后，刷新列表
            if(id>0){
            	$('#myModal').modal('hide');
            	var warehouseorderid = $("#warehouseorderId").val();
            	productDataTable2.fnSettings().sAjaxSource = "/gateIn/gateInProductlist/"+warehouseorderid;
            	productDataTable2.fnDraw();
            	$('#reset').click();
            }else{
                alert('保存失败！。');
            }
	},'json');
});
//计量单位
	$.post('/gateOut/alluom',function(data){
		var uom=$('#uom');
		uom.empty();
		uom.append("<option>请选择</option>");
		for(var i=0; i<data.length; i++){
			var name=data[i].NAME;
			uom.append("<option value='"+name+"'>"+name+"</option>");
		}
	},'json'); 
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
	var orderType=$("input[name='orderType']:checked").val();
	
	var warehouseorderid = $("#warehouseorderId").val();
	$.post('/gateIn/gateOutConfirm/'+warehouseorderid,{orderType:orderType},function(data){
		 /*if(data>0){
			 window.location.href="/transferOrder/edit?id="+data;
		 }
		 if(data.success){*/
		if(data>0){
			 $("#gateOutConfirmBtn").attr("disabled", true);
			 //$.scojs_message('出库成功', $.scojs_message.TYPE_ERROR);
			 window.location.href="/transferOrder/edit?id="+data;
         }else{
             $.scojs_message('出库失败', $.scojs_message.TYPE_ERROR);
         }
	},'json');
	
});
	var wStatus = $("#warehouseorderStatus").val();
	if(wStatus=='已出库'||wStatus==''){
	   $("#gateOutConfirmBtn").attr("disabled", true); 
	}
	//reset
	 $("#addproduct").click(function(){
		 $('#reset').click();
	 });
	 var parentId = "transferOrderbasic";
	 $("#transferOrderItemList").click(function(e){
		 	$("#saveInventoryBtn").click();
		 	/*if("basic" == parentId){
		 		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
		 	}*/
		 	parentId = e.target.getAttribute("id");
	   });
	
	 $("#transferOrderbasic").click(function(e){
			e.preventDefault();
			parentId = e.target.getAttribute("id");
	});
});