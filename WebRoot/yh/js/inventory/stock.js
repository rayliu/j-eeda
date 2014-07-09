$(document).ready(function() {
	$('#menu_warehouse').addClass('active').find('ul').addClass('in');
	//库存list
	var tab =$('#example2').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	       //"sPaginationType": "bootstrap",
		 	"iDisplayLength": 10,
	       "bServerSide": true,
	       "bRetrieve": true,
	   	"oLanguage": {
	           "sUrl": "/eeda/dataTables.ch.txt"
	       },
	       "sAjaxSource":"/yh/stock/stocklist",
			"aoColumns": [
				{"mDataProp":"ITEM_NAME"},
		            {"mDataProp":"ITEM_NO"},        	
		            {"mDataProp":"EXPIRE_DATE"},
		            {"mDataProp":"LOT_NO"},
		            {"mDataProp":"CATON_NO"},
		            {"mDataProp":"TOTAL_QUANTITY"},
		            {"mDataProp":"UOM"},
		           // {"mDataProp":"UNIT_PRICE"},
		            //{"mDataProp":"UNIT_COST"},
	           ]
	} );
	
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
	tab.fnSettings().sAjaxSource = "/yh/stock/stocklist/"+id;
	tab.fnDraw();
	$('#customerList').hide();
});

});
