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
	       "bAutoWidth":true,
	   	   "oLanguage": {
	           "sUrl": "/eeda/dataTables.ch.txt"
	       },
	       "sAjaxSource":"/stock/stocklist",
			"aoColumns": [
				{"mDataProp":"ITEM_NAME", "sWidth":"100px"},
	            {"mDataProp":"ITEM_NO", "sWidth":"80px"}, 
	            {"mDataProp":"COMPANY_NAME", "sWidth":"130px"},
	            {"mDataProp":"PREDICT_AMOUNT","sWidth":"40px"},
	            {"mDataProp":"VALID_AMOUNT","sWidth":"40px"},
	            {"mDataProp":"LOCK_AMOUNT","sWidth":"40px"},
	            {"mDataProp":null, "sWidth":"80px",
	            	"fnRender":function(obj){
	            		return obj.aData.VALID_AMOUNT + obj.aData.LOCK_AMOUNT;
	            	}},
	            {"mDataProp":"UNIT", "sWidth":"30px"},
	            {"mDataProp":"WAREHOUSE_NAME", "sWidth":"120px"},
	            {"mDataProp":"OFFICE_NAME", "sWidth":"100px"}
	           ]
	} );
	
	//选择仓库 
	 $('#warehouseSelect').on('keyup click', function(){
		if($("#warehouseList").val() == "")
	    	$("#warehouseId").val("");
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
	    
	    var customerId = $("#hiddenCustomerId").val();
		var warehouseId = $("#warehouseId").val();
		var offeceId = $("#hiddenOfficeId").val();
	    tab.fnSettings().sAjaxSource ="/stock/stocklist?customerId="+customerId+"&warehouseId="+warehouseId+"&offeceId="+offeceId;
		tab.fnDraw();
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
		/*tab.fnSettings().sAjaxSource ="/stock/stocklist/"+id;
		tab.fnDraw();*/
		
		var customerId = $("#hiddenCustomerId").val();
		var offeceId = $("#hiddenOfficeId").val();
		tab.fnSettings().sAjaxSource ="/stock/stocklist?customerId="+customerId+"&warehouseId="+id+"&offeceId="+offeceId;
		tab.fnDraw();
		
	});
	
	// 获取所有网点
	$('#officeSelect').on('keyup click', function(){
		if($("#officeSelect").val() == "")
	    	$("#hiddenOfficeId").val("");
		$.get('/gateIn/searchOfficeByPermission',{"officeName":$(this).val()}, function(data){
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
		$('#officeList').hide();
		$("#hiddenOfficeId").val(id);
		
		var customerId = $("#hiddenCustomerId").val();
		var warehouseId = $("#warehouseId").val();
	    tab.fnSettings().sAjaxSource ="/stock/stocklist?customerId="+customerId+"&warehouseId="+warehouseId+"&offeceId="+id;
		tab.fnDraw();
	});
	$('#officeSelect').on('blur', function(){
		$("#officeList").hide();
	});
	$('#officeList').on('blur', function(){
		$('#officeList').hide();
	});

	$('#officeList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	
	//获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup click', function(){
		if($('#customerMessage').val() == "")
			$("#hiddenCustomerId").val("");
		$.get('/customerContract/searcCustomer', {locationName:$('#customerMessage').val()}, function(data){
			console.log(data);
			var customerList =$("#customerList");
			customerList.empty();
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
				customerList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' payment='"+data[i].PAYMENT+"' partyId='"+data[i].PID+"' location='"+data[i].LOCATION+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');
		$("#customerList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#customerList').show();
        
        var customerId = $("#hiddenCustomerId").val();
		var warehouseId = $("#warehouseId").val();
		var offeceId = $("#hiddenOfficeId").val();
	    tab.fnSettings().sAjaxSource ="/stock/stocklist?customerId="+customerId+"&warehouseId="+warehouseId+"&offeceId="+offeceId;
		tab.fnDraw();
	});

 	// 没选中客户，焦点离开，隐藏列表
	$('#customerMessage').on('blur', function(){
 		$('#customerList').hide();
 	});

	//当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
	$('#customerList').on('blur', function(){
 		$('#customerList').hide();
 	});

	$('#customerList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	// 选中客户
	$('#customerList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		var customerId = $(this).attr('partyId');
		var warehouseId = $("#warehouseId").val();
		var offeceId = $("#hiddenOfficeId").val();
		$('#customerMessage').val(message.substring(0, message.indexOf(" ")));
		$("#hiddenCustomerId").val($(this).attr('partyId'));
		$('#customerList').hide();
		
		tab.fnSettings().sAjaxSource ="/stock/stocklist?customerId="+customerId+"&warehouseId="+warehouseId+"&offeceId="+offeceId;
		tab.fnDraw();
    }); 

});
