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
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
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
            {"mDataProp":"PLANNING_TIME"},
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
	var sp = $("#sp_filter_input").val();
	var beginTime = $("#beginTime_filter").val();
	var endTime = $("#endTime_filter").val();
	var planBeginTime = $("#beginTime_filter1").val();
	var planEndTime = $("#endTime_filter1").val();transfer_type
	var customer = $("#customer_filter").val();
	var transfer_type = $("#transfer_type").val();
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
										+"&transfer_type="+transfer_type
										+"&beginTime="+beginTime
										+"&endTime="+endTime
										+"&planBeginTime="+planBeginTime
										+"&planEndTime="+planEndTime
										+"&office="+office
										+"&start="+start
										+"&destination="+destination
										+"&customer="+customer
										+"&booking_note_number="+booking_note_number
										+"&costchebox="+costchebox;
	dataTable.fnDraw();
}

$('#query').on('click', function () {
	refreshData();
});


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
	$('#datetimepicker3').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN'
	}).on('changeDate', function(ev){
	    $('#beginTime_filter1').trigger('keyup');
	});


	$('#datetimepicker2').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN', 
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
	    $('#endTime_filter').trigger('keyup');
	});
	$('#datetimepicker4').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN', 
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
	    $('#endTime_filter1').trigger('keyup');
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