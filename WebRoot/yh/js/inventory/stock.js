$(document).ready(function() {
	$('#menu_warehouse').addClass('active').find('ul').addClass('in');
	//库存list
	var tab =$('#example2').dataTable( {
		   "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	       //"sPaginationType": "bootstrap",
		   "iDisplayLength": 10,
	       "bSort": false, // 不要排序
	       "bFilter": false, //不需要默认的搜索框
	       "bServerSide": true,
	   	   "oLanguage": {
	           "sUrl": "/eeda/dataTables.ch.txt"
	       },
	       "sAjaxSource":"/stock/stocklist",
			"aoColumns": [
				{"mDataProp":"ITEM_NAME"},
	            {"mDataProp":"ITEM_NO"}, 
	            {"mDataProp":"COMPANY_NAME"},
	            {"mDataProp":"TOTAL_QUANTITY"},
	            {"mDataProp":"UNIT"},
	            {"mDataProp":"EXPIRE_DATE", "bVisible":false},
	            {"mDataProp":"LOT_NO", "bVisible":false},
	            {"mDataProp":"CATON_NO", "bVisible":false}
	           ]
	} );
	
	//选择仓库 
	 $('#warehouseSelect').on('keyup click', function(){
		var warehouseName = $(this).val();
		var officeId = $("#hiddenOfficeId").val();
		$.get('/gateIn/findWarehouseById',{"warehouseName":warehouseName,"officeId":officeId}, function(data){
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
		$('#warehouseList').hide();
		tab.fnSettings().sAjaxSource ="/stock/stocklist/"+id;
		tab.fnDraw();
		
	});
	
	// 获取所有网点
	$('#officeSelect').on('keyup click', function(){
		if($("#officeSelect").val() == "")
	    	$("#hiddenOfficeId").val("");
		$.get('/gateIn/searchAllOffice',{"officeName":$(this).val()}, function(data){
			console.log(data);
			var officeList =$("#officeList");
			officeList.empty();
			for(var i = 0; i < data.length; i++)
			{
				officeList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</a></li>");
			}
		},'json');
		$("#officeList").css({ 
	    	left:$(this).position().left+"px", 
	    	top:$(this).position().top+32+"px" 
	    }); 
	    $('#officeList').show();
	});
	
	// 选中网点
	$('#officeList').on('mousedown', '.fromLocationItem', function(e){
		var id =$(this).attr('code');
		$('#officeSelect').val($(this).text());
		$("#hiddenOfficeId").val(id);
		$('#officeList').hide();
	});
	$('#officeSelect').on('blur', function(){
		if($("#officeSelect").val() == "")
	    	$("#hiddenOfficeId").val("");
		$("#officeList").hide();
	});
	$('#officeList').on('blur', function(){
		if($("#officeSelect").val() == "")
	    	$("#hiddenOfficeId").val("");
		$('#officeList').hide();
	});

	$('#officeList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});

});
