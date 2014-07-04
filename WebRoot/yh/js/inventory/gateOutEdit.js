$(document).ready(function() {
	$('#reset').hide();
$('#menu_warehouse').addClass('active').find('ul').addClass('in');
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
	$("#warehouseList").delay(120).hide(1);
});

// 选中仓库
$('#warehouseList').on('click', '.fromLocationItem', function(e){
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
         	//$("#ConfirmationBtn").attr("disabled", false);
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
		$("#customerList").delay(120).hide(1);
	});
	// 选中客户
	$('#customerList').on('click', '.fromLocationItem', function(e){
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
		$.get('/yh/gateOut/searchName2', {input:inputStr,customerId:customerId}, function(data){
			console.log(data);
			var itemNameList =$("#itemNameList");
			itemNameList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var item_name = data[i].ITEM_NAME;
				if(item_name == null){
					item_name = '';
				}
				itemNameList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' item_desc='"+data[i].ITEM_DESC+"' item_no='"+data[i].ITEM_NO+"' expire_date='"+data[i].EXPIRE_DATE+"' lot_no='"+data[i].LOT_NO+"' total_quantity='"+data[i].TOTAL_QUANTITY+"' unit_price='"+data[i].UNIT_PRICE+"' unit_cost='"+data[i].UNIT_COST+"' uom='"+data[i].UOM+"', caton_no='"+data[i].CATON_NO+"', >"+data[i].ITEM_NAME+"</a></li>");
			}
		},'json');		
		$("#itemNameList").css({ 
			left:$(this).position().left+"px", 
			top:$(this).position().top+32+"px" 
		}); 
		$('#itemNameList').show();        
	});
	$('#itemNameMessage').on('blur', function(){
	$("#itemNameList").delay(120).hide(1);
});
// 选中产品名
	$('#itemNameList').on('click', '.fromLocationItem', function(e){
		$("#itemNameMessage").val($(this).text());
		if($(this).attr('item_no') == 'null'){
			$("#item_no").val('');
		}else{
			$("#itemNoMessage").val($(this).attr('item_no'));
		}if($(this).attr('item_desc') == 'null'){
 			$("#item_desc").val('');
 		}else{
 			$("#item_desc").val($(this).attr('item_desc'));
 		}
 		if($(this).attr('expire_date') == 'null'){
 			$("#expire_date").val('');
 		}else{
 			$("#expire_date").val($(this).attr('expire_date'));
 		}
 		if($(this).attr('lot_no') == 'null'){
 			$("#lot_no").val('');
 		}else{
 			$("#lot_no").val($(this).attr('lot_no')); 			
 		}
 		if($(this).attr('total_quantity') == 'null'){
 			$("total_quantity").val('');
 		}else{
 			$("#total_quantity").val($(this).attr('total_quantity'));
 		}
 		if($(this).attr('unit_price') == 'null'){
 			$("unit_price").val('');
 		}else{
 			$("#unit_price").val($(this).attr('unit_price'));
 		}
 		if($(this).attr('unit_cost') == 'null'){
 			$("unit_cost").val('');
 		}else{
 			$("#unit_cost").val($(this).attr('unit_cost'));
 		}
 		if($(this).attr('uom') == 'null'){
 			$("uom").val('');
 		}else{
 			$("#uom").val($(this).attr('uom'));
 		}
 		if($(this).attr('caton_no') == 'null'){
 			$("caton_no").val('');
 		}else{
 			$("#caton_no").val($(this).attr('caton_no'));
 		}
		$("#productId").val($(this).attr('id'));
		$('#itemNameList').hide();
	});  	
	
	//获取货品的序列号list，选中信息在下方展示其他信息
	$('#itemNoMessage').on('keyup click', function(){
		var inputStr = $('#itemNoMessage').val();
		var customerId = $('#party_id').val();
		$.get('/yh/gateOut/searchNo2', {input:inputStr,customerId:customerId}, function(data){
			console.log(data);
			var itemNoList =$("#itemNoList");
			itemNoList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var item_no = data[i].ITEM_NO;
				if(item_no == null){
					item_no = '';
				}
				itemNoList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' item_desc='"+data[i].ITEM_DESC+"' item_name='"+data[i].ITEM_NAME+"' expire_date='"+data[i].EXPIRE_DATE+"' lot_no='"+data[i].LOT_NO+"' total_quantity='"+data[i].TOTAL_QUANTITY+"' unit_price='"+data[i].UNIT_PRICE+"' unit_cost='"+data[i].UNIT_COST+"' uom='"+data[i].UOM+"', caton_no='"+data[i].CATON_NO+"', >"+data[i].ITEM_NO+"</a></li>");
			}
		},'json');		
     $("#itemNoList").css({ 
     	left:$(this).position().left+"px", 
     	top:$(this).position().top+32+"px" 
     }); 
     $('#itemNoList').show();        
	});
	$('#itemNoMessage').on('blur', function(){
	$("#itemNoList").delay(120).hide(1);
});
// 选中序列号
	$('#itemNoList').on('click', '.fromLocationItem', function(e){
		$("#itemNoMessage").val($(this).text());
		if($(this).attr('item_name') == 'null'){
			$("#item_name").val('');
		}else{
			$("#itemNameMessage").val($(this).attr('item_name'));
		}if($(this).attr('item_desc') == 'null'){
 			$("#item_desc").val('');
 		}else{
 			$("#item_desc").val($(this).attr('item_desc'));
 		}
 		if($(this).attr('expire_date') == 'null'){
 			$("#expire_date").val('');
 		}else{
 			$("#expire_date").val($(this).attr('expire_date'));
 		}
 		if($(this).attr('lot_no') == 'null'){
 			$("#lot_no").val('');
 		}else{
 			$("#lot_no").val($(this).attr('lot_no')); 			
 		}
 		if($(this).attr('total_quantity') == 'null'){
 			$("total_quantity").val('');
 		}else{
 			$("#total_quantity").val($(this).attr('total_quantity'));
 		}
 		if($(this).attr('unit_price') == 'null'){
 			$("unit_price").val('');
 		}else{
 			$("#unit_price").val($(this).attr('unit_price'));
 		}
 		if($(this).attr('unit_cost') == 'null'){
 			$("unit_cost").val('');
 		}else{
 			$("#unit_cost").val($(this).attr('unit_cost'));
 		}
 		if($(this).attr('uom') == 'null'){
 			$("uom").val('');
 		}else{
 			$("#uom").val($(this).attr('uom'));
 		}
 		if($(this).attr('caton_no') == 'null'){
 			$("caton_no").val('');
 		}else{
 			$("#caton_no").val($(this).attr('caton_no'));
 		}
		$("#productId").val($(this).attr('id'));
    $('#itemNoList').hide();
 }); 
	
});