$(document).ready(function() {
	document.title = '单据状态查询 | '+document.title;
    $('#menu_report').addClass('active').find('ul').addClass('in');
    $("#queryBtn").prop("disabled",true);
	//datatable, 动态处理
    var statusTable = $('#eeda-table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"bLengthChange":true,
    	"bProcessing": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [   
            {"mDataProp":"ORDER_NO", "sWidth":"90px"},
            {"mDataProp":"AMOUNT", "sWidth":"30px"},
            {"mDataProp":"ORDER_STATUS", "sWidth":"60px"},
            {"mDataProp":"ORDER_CATEGORY", "sWidth":"60px"},
            {"mDataProp":"DEPART_NO", "sWidth":"100px"},
            {"mDataProp":"ORDER_TYPE", "sWidth":"60px",
            	"fnRender": function(obj) {
            		$("#total_amount").html(obj.aData.TOTAL_AMOUNT);
            		if(obj.aData.ORDER_TYPE == "salesOrder"){
            			return "销售订单";
            		}else if(obj.aData.ORDER_TYPE == "replenishmentOrder"){
            			return "补货订单";
            		}else if(obj.aData.ORDER_TYPE == "arrangementOrder"){
            			return "调拨订单";
            		}else if(obj.aData.ORDER_TYPE == "cargoReturnOrder"){
            			return "退货订单";
            		}else if(obj.aData.ORDER_TYPE == "damageReturnOrder"){
            			return "质量退单";
            		}else if(obj.aData.ORDER_TYPE == "gateOutTransferOrder"){
            			return "出库运输单";
            		}else if(obj.aData.ORDER_TYPE == "movesOrder"){
            			return "移机单";
            		}else{
            			return "";
            		}
            	}
        	},
            {"mDataProp":"ABBR", "sWidth":"80px"},       	
            {"mDataProp":"OFFICE_NAME", "sWidth":"80px"},
            {"mDataProp":"PLANNING_TIME", "sWidth":"80px"},
            {"mDataProp":"DEPARTURE_TIME", "sWidth":"80px"},
            {"mDataProp":"ROUTE_FROM", "sWidth":"80px"},
            {"mDataProp":"ROUTE_TO", "sWidth":"80px"}
        ]  
    });	
    
    $("#customerMessage,#order_no").on('keyup click', function () {
    	/*var beginTime = $("#beginTime").val();
    	var routeTo = $("#routeTo").val();
    	var routeFrom = $("#routeFrom").val();
    	var order_no = $("#order_no").val();
    	var setOutTime = $("#setOutTime").val();
    	var customerMessage = $("#customerMessage").val();
    	var endTime=$("#endTime").val();
    	var sp_name = $("#sp_name").val();*/
    	var order_no = $("#order_no").val();
    	var customerMessage = $("#customerMessage").val();
    	if(customerMessage!= '' || order_no!= ''){
    		$("#queryBtn").prop("disabled",false);
    	}else{
    		$("#queryBtn").prop("disabled",true);
    	}
    });
    $("#queryBtn").on('click', function () {
    	var order_no_type=$("#order_no_type").val();
    	var order_no=$("#order_no").val();
    	var order_status_type = $("#order_status_type").val();
    	var transferOrder_status = $("#transferOrder_status").val();
    	var delivery_status = $("#delivery_status").val();
    	var setOutTime = $("#setOutTime").val();
    	var customer_id = $("#customer_id").val();
    	var routeFrom = $("#routeFrom").val();
    	var beginTime = $("#beginTime").val();
    	var sp_id = $("#sp_id").val();
    	var routeTo = $("#routeTo").val();
    	var endTime = $("#endTime").val();
    	$("#total_amount").html('0');
    	
		statusTable.fnSettings().oFeatures.bServerSide = true;
		statusTable.fnSettings()._iDisplayStart = 0;
    	statusTable.fnSettings().sAjaxSource = "/statusReport/orderStatusReport?beginTime="+beginTime+"&endTime="+endTime+"&setOutTime="+setOutTime
    		+"&order_no_type="+order_no_type+"&order_no="+order_no+"&order_status_type="+order_status_type+"&transferOrder_status="+transferOrder_status
    		+"&delivery_status="+delivery_status+"&customer_id="+customer_id+"&routeFrom="+routeFrom+"&sp_id="+sp_id+"&routeTo="+routeTo;
    	statusTable.fnDraw(); 
    });
    
    //获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup click', function(){
		if($('#customerMessage').val() == "")
			$("#customer_id").val("");
		$.get('/statusReport/search', {locationName:$('#customerMessage').val()}, function(data){
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
				
				var abbr = data[i].ABBR;
				if(abbr == null){
					abbr = '';
				}
				customerList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' payment='"+data[i].PAYMENT+"' partyId='"+data[i].PID+"' location='"+data[i].LOCATION+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+" "+contact_person+" "+phone+"</a></li>");   
				//customerList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' payment='"+data[i].PAYMENT+"' partyId='"+data[i].PID+"' location='"+data[i].LOCATION+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+" "+contact_person+" "+phone+"</a></li>");
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
		$('#customerMessage').focus();
		$("#customer_id").val($(this).attr('partyId'));
		$('#customerList').hide();
		$("#queryBtn").prop("disabled",false);
    }); 
    
	
	//获取供应商的list，选中信息在下方展示其他信息
	$('#sp_name').on('keyup click', function(){
		if($('#sp_name').val() == ""){
			$("#sp_id").val("");
		}
		$.get('/serviceProvider/searchSp', {input:$('#sp_name').val()}, function(data){
			console.log(data);
			var spList =$("#spList");
			spList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var abbr = data[i].ABBR;
				if(abbr == null){
					abbr = '';
				}
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
				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');

		$("#spList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#spList').show();
	});
    
    // 没选中供应商，焦点离开，隐藏列表
	$('#sp_name').on('blur', function(){
 		$('#spList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#spList').on('blur', function(){
 		$('#spList').hide();
 	});
	
	//没选中
	$('#spList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});

	// 选中供应商
	$('#spList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#sp_name').val(message.substring(0, message.indexOf(" ")));
		$('#sp_id').val($(this).attr('partyId'));
        $('#spList').hide();
    });
	
	$('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN'
	}).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
	    $('#beginTime').trigger('keyup');
	});		
	
	$('#datetimepicker2').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN', 
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
	    $('#endTime').trigger('keyup');
	});
	
	$('#datetimepicker3').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN', 
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
	    $('#setOutTime').trigger('keyup');
	});
	
	//select控制
	$("#order_no_type").change(function(){
		if($(this).val() == "transferOrder"){
			$("#order_status_type").val("transferOrderStatus");
			$("#transferOrder_status").show();
			$("#delivery_status").val("").hide();
		}else{
			$("#order_status_type").val("deliveryStatus");
			$("#transferOrder_status").val("").hide();
			$("#delivery_status").show();
		}
	});
	
	//select控制
	$("#order_status_type").change(function(){
		if($(this).val() == "transferOrderStatus"){
			$("#order_no_type").val("transferOrder");
			$("#transferOrder_status").show();
			$("#delivery_status").val("").hide();
		}else{
			$("#order_no_type").val("deliveryOrder");
			$("#transferOrder_status").val("").hide();
			$("#delivery_status").show();
		}
	});
	
	
	
	
    
});
    