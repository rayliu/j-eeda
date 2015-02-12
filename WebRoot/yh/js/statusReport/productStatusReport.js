$(document).ready(function() {
    $('#menu_report').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var statusTable = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/statusReport/findTransferOrdertatus?order_no=''&customer_id=''"
						+"&customer_order_no=''&item_no=''&serial_no=''",
        "aoColumns": [   
            {"mDataProp":"SERIAL_NO", "sWidth":"80px"},
            {"mDataProp":"ITEM_NO", "sWidth":"100px"},
            {"mDataProp":"CUSTOMER", "sWidth":"100px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"80px"},
            {"mDataProp":null, "sWidth":"180px",
            	"fnRender": function(obj) {  
                		return obj.aData.TRANSFER_NO + "(" + obj.aData.TRANSFER_STATUS + ")";
                }
            },       	
            {"mDataProp":null, "sWidth":"150px",
            	"fnRender": function(obj) {  
            		if(obj.aData.PICK_NO != null )
                		return obj.aData.PICK_NO + "(" + obj.aData.PICK_STATUS + ")";
            		else
            			return "";
                }
            }, 
            {"mDataProp":null, "sWidth":"150px",
            	"fnRender": function(obj) {  
            		if(obj.aData.DEPART_NO != null )
            			return obj.aData.DEPART_NO + "(" + obj.aData.DEPART_STATUS + ")";
            		else
            			return "";
                }
            }, 
            {"mDataProp":null, "sWidth":"150px",
            	"fnRender": function(obj) {  
            		if(obj.aData.CARGO_NATURE == "cargo" && obj.aData.CARGO_NATURE_DETAIL == "cargoNatureDetailNo"){
            			return obj.aData.DELIVERY_NO;
            		}else{
            			if(obj.aData.DELIVERY_NO != null )
                			return obj.aData.DELIVERY_NO + "(" + obj.aData.DELIVERY_STATUS + ")";
                		else
                			return "";
            		}
                }
            }, 
            {"mDataProp":null, "sWidth":"150px",
            	"fnRender": function(obj) {  
            		if(obj.aData.RETURN_NO != null )
            			return obj.aData.RETURN_NO + "(" + obj.aData.RETURN_STATUS + ")";
            		else
            			return "";
                }
            }
        ]  
    });	
    
    var execResult = function(){
    	var order_no=$("#order_no").val();
    	var item_no=$("#item_no").val();
    	var customer_id=$("#customer_id").val();
    	var serial_no = $("#serial_no").val();
    	var customer_order_no = $("#customer_order_no").val();
    	if(order_no != "" || customer_order_no != "" || serial_no != ""){
	    	statusTable.fnSettings().sAjaxSource = "/statusReport/findTransferOrdertatus?order_no="+order_no +"&customer_id="+customer_id
	    											+"&customer_order_no="+customer_order_no+"&item_no="+item_no+"&serial_no="+serial_no;
	    	statusTable.fnDraw(); 
    	}
    };
    
    $("#order_no,#item_no,#customer_id,#serial_no,#customer_order_no").on('keyup click', function () {
    	execResult();
    });
    
    /*// 获取所有运输单号
	$('#order_no').on('keyup click', function(){
		if($("#order_no").val() == "")
	    	$("#hiddenOfficeId").val("");
		$.get('/gateIn/searchAllOffice',{"officeName":$(this).val()}, function(data){
			console.log(data);
			var orderNoList =$("#orderNoList");
			orderNoList.empty();
			for(var i = 0; i < data.length; i++)
			{
				orderNoList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].ID+"'>"+data[i].ORDER_NO+"</a></li>");
			}
		},'json');
		$("#orderNoList").css({ 
	    	left:$(this).position().left+"px", 
	    	top:$(this).position().top+32+"px" 
	    }); 
	    $('#orderNoList').show();
	});
	
	// 选中运输单号
	$('#orderNoList').on('mousedown', '.fromLocationItem', function(e){
		var id =$(this).attr('code');
		$('#officeSelect').val($(this).text());
		$('#orderNoList').hide();
		$("#hiddenOfficeId").val(id);
		
		var customerId = $("#hiddenCustomerId").val();
		var warehouseId = $("#warehouseId").val();
	    tab.fnSettings().sAjaxSource ="/stock/stocklist?customerId="+customerId+"&warehouseId="+warehouseId+"&offeceId="+id;
		tab.fnDraw();
	});
	$('#order_no').on('blur', function(){
		$("#orderNoList").hide();
	});
	$('#orderNoList').on('blur', function(){
		$('#orderNoList').hide();
	});

	$('#orderNoList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});*/
    
    //获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup click', function(){
		if($('#customerMessage').val() == "")
			$("#customer_id").val("");
		$.get('/customerContract/search', {locationName:$('#customerMessage').val()}, function(data){
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
        execResult();
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
		$('#customerMessage').focus();
		$("#customer_id").val($(this).attr('partyId'));
		$('#customerList').hide();
		execResult();
    }); 
    
    
    
});
    