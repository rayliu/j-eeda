 $(document).ready(function() {
	 document.title = '发车单查询 | '+document.title;
	$('#menu_assign').addClass('active').find('ul').addClass('in');
	var dataTable =$('#dataTables-example').dataTable({
		"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
		"bFilter": false, //不需要默认的搜索框
        "bSort": false, 
		//"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/departOrder/list",
        "aoColumns": [   
            { 
                "mDataProp": null, "sWidth":"70px",
                "sWidth": "8%",                
                "fnRender": function(obj) {  
                	if(DepartOrder.isUpdate || DepartOrder.isComplete){
                		return "<a href='/departOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.DEPART_NO+"</a>";
                	}else{
                		return obj.aData.DEPART_NO;
                	}
                	
                }
            },
            {"mDataProp":"TRANSFER_ORDER_NO"},
            {"mDataProp":"OFFICE_NAME"},
            {"mDataProp":null,
            	"fnRender":function(obj){
            			return obj.aData.TRIP_TYPE;
            	}},
            {"mDataProp":"DEPART_STATUS"},
            {"mDataProp":"CUSTOMER", "sWidth":"160px"},
            {"mDataProp":null,
            	"fnRender":function(obj){
            		if(obj.aData.TOTAL_COST == '0' ){
            			return "<b style='color:red'>"+obj.aData.TOTAL_COST+"</b>";
            		}else{
            			return obj.aData.TOTAL_COST;
            		}
            }},
            {"mDataProp":"BOOKING_NOTE_NUMBER"},
            {"mDataProp":"ABBR", "sWidth":"200px"},
            {"mDataProp":"CONTACT_PERSON"},
            {"mDataProp":"PHONE"},
            {"mDataProp":"ROUTE_FROM"},
            {"mDataProp":"ROUTE_TO"},
            {"mDataProp":"DEPARTURE_TIME"},
            {"mDataProp":"ARRIVAL_TIME"},
            {"mDataProp":"USER_NAME"},
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"REMARK", "sWidth":"200px"} 
        ]      
    });
function refreshData(){
	var office =$("#officeSelect").val();
	var start =$("#start_filter").val();
	var destination=$("#destination_filter").val();
	var orderNo = $("#orderNo_filter").val();
	var departNo_filter = $("#departNo_filter").val();
	var status = $("#status_filter").val();
	var sp = $("#sp_filter").val();
	var beginTime = $("#beginTime_filter").val();
	var endTime = $("#endTime_filter").val();
	var customer = $("#customer_filter").val();
	var booking_note_number = $("#booking_note_number").val();
	var costchebox = $("input[type='checkbox']:checked").val();
	if(costchebox!='zero'){
		costchebox = '1';
	}else{
		costchebox = '0';
	}
	dataTable.fnSettings().sAjaxSource = "/departOrder/list?orderNo="+orderNo
										+"&departNo="+departNo_filter
										+"&status="+status
										+"&sp="+sp
										+"&beginTime="+beginTime
										+"&endTime="+endTime
										+"&office="+office
										+"&start="+start
										+"&destination="+destination
										+"&customer="+customer
										+"&booking_note_number="+booking_note_number
										+"&costchebox="+costchebox;
	dataTable.fnDraw();
}
$('#departNo_filter,#endTime_filter ,#beginTime_filter  ,#orderNo_filter,#start_filter,#destination_filter,#booking_note_number,#costcheckbox').on( 'keyup click', function () {
	refreshData();
} );
//供应商，状态，选择框
$('#status_filter ,#officeSelect').on( 'change', function () {
	refreshData();
} );

$.post('/transferOrder/searchPartOffice',function(data){
	 if(data.length > 0){
		 var officeSelect = $("#officeSelect");
		 officeSelect.empty();
		 
		 officeSelect.append("<option ></option>");
		
		 for(var i=0; i<data.length; i++){
			  officeSelect.append("<option value='"+data[i].OFFICE_NAME+"'>"+data[i].OFFICE_NAME+"</option>");	
		 }
	 }
 },'json');

	$('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN'
	}).on('changeDate', function(ev){
	    $('#beginTime_filter').trigger('keyup');
	});


	$('#datetimepicker2').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN', 
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
	    $('#endTime_filter').trigger('keyup');
	});

	//获取供应商的list，选中信息在下方展示其他信息
	$('#sp_filter').on('input click', function(){
		var me=this;
		var inputStr = $('#sp_filter').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
			if(inputStr!=$('#sp_filter').val()){//查询条件与当前输入值不相等，返回
				return;
			}
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
			$("#spList").css({ 
	        	left:$(me).position().left+"px", 
	        	top:$(me).position().top+32+"px" 
	        }); 
	        $('#spList').show();
		},'json');

		
	});

	// 没选中供应商，焦点离开，隐藏列表
	$('#sp_filter').on('blur', function(){
 		$('#spList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#spList').on('blur', function(){
 		$('#spList').hide();
 	});

	$('#spList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});

	// 选中供应商
	$('#spList').on('mousedown', '.fromLocationItem', function(e){
		console.log($('#spList').is(":focus"));
		var message = $(this).text();
		$('#sp_filter').val(message.substring(0, message.indexOf(" ")));
		$('#sp_id').val($(this).attr('partyId'));
		var pageSpName = $("#pageSpName");
		pageSpName.empty();
		var pageSpAddress = $("#pageSpAddress");
		pageSpAddress.empty();
		pageSpAddress.append($(this).attr('address'));
		var contact_person = $(this).attr('contact_person');
		if(contact_person == 'null'){
			contact_person = '';
		}
		pageSpName.append(contact_person+'&nbsp;');
		var phone = $(this).attr('phone');
		if(phone == 'null'){
			phone = '';
		}
		pageSpName.append(phone); 
		pageSpAddress.empty();
		var address = $(this).attr('address');
		if(address == 'null'){
			address = '';
		}
		pageSpAddress.append(address);
        $('#spList').hide();
        
        refreshData();
    });
	 $('#customer_filter').on('keyup click', function(){
	        var inputStr = $('#customer_filter').val();
	        var companyList =$("#companyList");
	        $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
	            companyList.empty();
	            for(var i = 0; i < data.length; i++)
	                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
	        },'json');
	        companyList.css({ 
		    	left:$(this).position().left+"px", 
		    	top:$(this).position().top+32+"px" 
		    });
	        companyList.show();
	    });
	    $('#companyList').on('click', '.fromLocationItem', function(e){        
	        $('#customer_filter').val($(this).text());
	        $("#companyList").hide();
	        var companyId = $(this).attr('partyId');
	        $('#customerId').val(companyId);
	        refreshData();
	    });
	    // 没选中客户，焦点离开，隐藏列表
	    $('#customer_filter').on('blur', function(){
	        $('#companyList').hide();
	    });
	
	    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
	    $('#customer_filter').on('blur', function(){
	        $('#companyList').hide();
	    });
	
	    $('#companyList').on('mousedown', function(){
	        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	    });

});