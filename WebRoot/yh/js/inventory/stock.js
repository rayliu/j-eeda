$(document).ready(function() {
	document.title = '库存查询 | '+document.title;
	$('#menu_warehouse').addClass('active').find('ul').addClass('in');
	//库存list

   var tab =$('#example').dataTable( {
	   "bProcessing": false, //table载入数据时，是否显示‘loading...’提示
	   	"bFilter": false, //不需要默认的搜索框
	   	"bSort": false, // 不要排序
	   	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
	   	"iDisplayLength": 10,
	   	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
	   	"bServerSide": true,
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
            {"mDataProp":"TOTAL_QUANTITY", "sWidth":"80px"},
            {"mDataProp":"UNIT", "sWidth":"30px"},
            {"mDataProp":"WAREHOUSE_NAME",'bVisible':false,"sWidth":"120px"},
            {"mDataProp":"OFFICE_NAME",'bVisible':false,"sWidth":"100px"}
           ]
	});
	
	//选择仓库 
	 $('#warehouseSelect').on('keyup click', function(){
		if($("#warehouseList").val() == "")
	    	$("#warehouseId").val("");
		var warehouseName = $(this).val();
		var officeId = $("#hiddenOfficeId").val();
		var customerId = $("#hiddenCustomerId").val();
		if(officeId != "" || customerId != ""){
			$.get('/gateIn/findWarehouseById',{"warehouseName":warehouseName,"officeId":officeId,"customerId":customerId}, function(data){
				var warehouseList =$("#warehouseList");
				warehouseList.empty();
				if(data.length > 1){
					warehouseList.append("<li><a tabindex='-1' class='fromLocationItem'  code='All'>所有仓库</a></li>");
				}
				for(var i = 0; i < data.length; i++)
				{
					warehouseList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</a></li>");
				}
			},'json');
		}
		
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
	
		
	});
	
	// 获取所有网点
	$('#officeSelect').on('keyup click', function(){
		if($("#officeSelect").val() == ""){
			$("#hiddenOfficeId").val("");
			if($("#hiddenCustomerId").val() == ""){
				$("#warehouseList").empty();
				$("#warehouseId").val("");
				$('#warehouseSelect').val("");
			};
			
		}
	    	
		$.get('/officeConfig/searchAllOffice',{"officeName":$(this).val()}, function(data){
			
			var officeList =$("#officeList");
			officeList.empty();
			var customerId = $("#hiddenCustomerId").val();
			if(customerId != null && customerId != "" && data.length>1){
				officeList.append("<li><a tabindex='-1' class='fromLocationItem'  code='all'>所有网点</a></li>");
			}
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
		
		
		 var warehouseId = $("#warehouseId").val();
		 
		 if(warehouseId == ""){
			 $("#warehouseSelect").val("所有仓库");
			 $("#warehouseId").val("all");
		 }
		
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
		if($('#customerMessage').val() == ""){
			$("#hiddenCustomerId").val("");
			if($("#hiddenOfficeId").val() == ""){
				$("#warehouseList").empty();
				$("#warehouseId").val("");
				$('#warehouseSelect').val("");
				//清空产品型号
				$("#itemList").empty();
				$("#hiddenItemId").val("");
				$('#ItemMessage').val("");
			}
		}	
		$.get('/customerContract/searcCustomer', {locationName:$('#customerMessage').val()}, function(data){
			var customerList =$("#customerList");
			customerList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var company_name = data[i].COMPANY_NAME;
				if(company_name == null){
					company_name = '';
				}
				var abbr = data[i].ABBR;
				if(abbr == null){
					abbr = "";
				}
				var contact_person = data[i].CONTACT_PERSON;
				if(contact_person == null){
					contact_person = '';
				}
				var phone = data[i].PHONE;
				if(phone == null){
					phone = '';
				}
				customerList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' payment='"+data[i].PAYMENT+"' partyId='"+data[i].PID+"' location='"+data[i].LOCATION+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');
		$("#customerList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#customerList').show();

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
		
		$('#customerMessage').val(message.substring(0, message.indexOf(" ")));
		$("#hiddenCustomerId").val($(this).attr('partyId'));
		$('#customerList').hide();
		
		
		 var warehouseId = $("#warehouseId").val();
		 var officeId = $("#hiddenOfficeId").val();
		 if(officeId == ""){
			 $("#officeSelect").val("所有网点");
			 $("#hiddenOfficeId").val("all");
		 }
		 if(warehouseId == ""){
			 $("#warehouseSelect").val("所有仓库");
			 $("#warehouseId").val("all");
		 }
    }); 
	
	
	$("#queryBtn").click(function(){
		 var customerId = $("#hiddenCustomerId").val();
		 var warehouseId = $("#warehouseId").val();
		 var officeId = $("#hiddenOfficeId").val();
		 var itemId = $("#hiddenItemId").val();
		 
		 if(warehouseId == "" || warehouseId == "all"){
			 tab.fnSetColumnVis( 8, false);
		 }else{
			 tab.fnSetColumnVis( 8, true);
		 }
		 if(officeId == "" || officeId == "all"){
			 tab.fnSetColumnVis( 9, false);
		 }else{
			 tab.fnSetColumnVis( 9, true);
		 }
		 
	     tab.fnSettings().sAjaxSource ="/stock/stocklist?customerId="+customerId
	     					+"&warehouseId="+warehouseId+"&officeId="+officeId
	     					+"&itemId="+itemId;
	     if(customerId !="" || warehouseId != "" || officeId != ""){
	    	 $.ajax({  
		            type : "post",  
		            url : "/stock/getTotalAmount",
		            dataType: "json",
		            data : {customerId: customerId, warehouseId: warehouseId, officeId: officeId, itemId: itemId},  
		            success : function(data){  
		            	/*$("#totalAmount").val(data);*/
		            	$("#totalAmount").text(data);
		            }  
		        });
	     }
	     
		 tab.fnDraw();
	});
	
	
	// 获取产品型号
	$('#ItemMessage').on('keyup click', function(){
		if($('#ItemMessage').val() == "" || $('#ItemMessage').val() == null){
			$("#hiddenItemId").val("");
		}
		 var customerId = $("#hiddenCustomerId").val();
		 var itemNo = $('#ItemMessage').val();
		 if(customerId != null && customerId != ""){
			 $.get('/gateIn/searchItem',{"customerId":customerId,"itemNo":itemNo}, function(data){
					
					var itemList =$("#itemList");
					itemList.empty();
					for(var i = 0; i < data.length; i++)
					{
						itemList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].ID+"'>"+data[i].ITEM_NO+"</a></li>");
					}
				},'json');
		 }
		
		$("#itemList").css({ 
	    	left:$(this).position().left+"px", 
	    	top:$(this).position().top+32+"px" 
	    }); 
	    $('#itemList').show();
	});
	
	// 
	$('#itemList').on('mousedown', '.fromLocationItem', function(e){
		var id =$(this).attr('code');
		$('#ItemMessage').val($(this).text());
		$('#itemList').hide();
		$("#hiddenItemId").val(id);
		
	});
	$('#ItemMessage').on('blur', function(){
		$("#itemList").hide();
	});
	$('#itemList').on('blur', function(){
		$('#itemList').hide();
	});

	$('#itemList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});

});
