$(document).ready(function() {
    $('#menu_report').addClass('active').find('ul').addClass('in');
    $("#queryBtn").prop("disabled",true);
	//datatable, 动态处理
    var statusTable = $('#eeda-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [   
            {"mDataProp":"SERIAL_NO", "sWidth":"80px"},
            {"mDataProp":"ITEM_NO", "sWidth":"100px"},
            {"mDataProp":"CUSTOMER", "sWidth":"100px"},
            {"mDataProp":"NOTIFY_PARTY_COMPANY", "sWidth":"100px"},
            {"mDataProp":null, "sWidth":"80px",
            	"fnRender": function(obj) {  
            		/*	
            		 	新建运输
						在货场
						运输在途
						在中转仓
						新建配送
						配送在途
						客户签收（回单在途）
						回单签收
						已对账
						已收款
					*/
            		var status = "新建运输";
            		if(obj.aData.TRANSACTION_STATUS == "已签收"){
            			status = "回单签收";
            		}else if(obj.aData.TRANSACTION_STATUS == "新建"){
            			status = "客户签收（回单在途）";
            		}else if(obj.aData.DELIVERY_STATUS == "已发车"){
            			status = "配送在途";
            		}else if(obj.aData.DELIVERY_STATUS == "新建"){
            			status = "新建配送";
            		}else if(obj.aData.DEPART_STATUS == "已入库"){
            			status = "在中转仓";
            		}else if(obj.aData.DEPART_STATUS == "已发车"){
            			status = "运输在途";
            		}else if(obj.aData.PICK_STATUS == "已入货场"){
            			status = "在货场";
            		}else if(obj.aData.PICK_STATUS == "新建"){
            			status = "新建运输";
            		}
            		return status;
                }
            },       	
            {"mDataProp":"PLANNING_TIME", "sWidth":"80px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"80px"},
            {"mDataProp":"TRANSFER_NO", "sWidth":"80px"},
            {"mDataProp":"WAREHOUSE_NAME", "sWidth":"80px"},
            {"mDataProp":"WAREHOUSE_STAMP", "sWidth":"120px"},
            {"mDataProp":"DELIVERY_NO", "sWidth":"80px"},
            {"mDataProp":"DELIVERY_STAMP", "sWidth":"80px"},
            {"mDataProp":"RETURN_STAMP", "sWidth":"80px"}
            
        ]  
    });	
    
    $("#serial_no,#beginTime,#endTime").on('keyup click', function () {
    	var beginTime=$("#beginTime").val();
    	var endTime=$("#endTime").val();
    	var serial_no = $("#serial_no").val();
    	if((beginTime != "" && endTime != "") || serial_no != ""){
    		$("#queryBtn").prop("disabled",false);
    	}else{
    		$("#queryBtn").prop("disabled",true);
    	}
    });
    
    $("#queryBtn").on('click', function () {
    	var beginTime=$("#beginTime").val();
    	var endTime=$("#endTime").val();
    	var serial_no = $("#serial_no").val();
    	var order_no = $("#order_no").val();
    	var customer_id = $("#customer_id").val();
    	var customer_order_no = $("#customer_order_no").val();
    	var item_no = $("#item_no").val();
    	if((beginTime != "" && endTime != "") || serial_no != ""){
    		statusTable.fnSettings().oFeatures.bServerSide = true;
	    	statusTable.fnSettings().sAjaxSource = "/statusReport/findTransferOrdertatus?beginTime="+beginTime+"&endTime="+endTime+"&serial_no="+serial_no
	    		+"&order_no="+order_no+"&customer_id="+customer_id+"&customer_order_no="+customer_order_no+"&item_no="+item_no;
	    	statusTable.fnDraw(); 
    	}
    });
    
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
    
    
});
    