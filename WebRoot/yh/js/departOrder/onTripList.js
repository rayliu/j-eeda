
$(document).ready(function() {

    $('#menu_status').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var detailTable = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/departOrder/onTripList",
        "aoColumns": [
			{ 
			    "mDataProp": null,
			    "sWidth": "60px",
			    "fnRender": function(obj) {
			    	console.log(obj.aData.ARRIVAL_MODE);
			    	if(obj.aData.ARRIVAL_MODE == '货品直送'){
			    		return "<a class='btn btn-primary confirmReceipt' departOrderId='"+obj.aData.ID+"' code='"+obj.aData.ORDER_ID+"'>"+"收货确认"+"</a>";
			    	}else{
			    		return "<a class='btn btn-primary confirmInWarehouse' code='"+obj.aData.ID+"'>"+"入库确认"+"</a>";
			    	}
			    }
			},
            {"mDataProp":"OFFICE_NAME","sWidth": "80px"},        
            {"mDataProp":null,
            	"sWidth": "100px",
            	"fnRender": function(obj) {
            		 return "<a href='/departOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.DEPART_NO+"</a>";
            	}
            },
            {"mDataProp":"TRANSFER_ORDER_NO",
            	"sWidth": "100px"},
            {"mDataProp":"CNAME",
            	"sWidth": "100px"}, 
            {"mDataProp":null,
            	"sWidth": "80px",
            	"fnRender": function(obj) {
            		return "";
            	}},
            {"mDataProp":null,
            	"sWidth": "90px",
                "fnRender": function(obj) {
                	if(obj.aData.LOCATION!=null && obj.aData.LOCATION!=''){
                		return obj.aData.LOCATION+"<a id='edit_status' depart_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                	}else{
                    	if(obj.aData.DEPART_STATUS==null){
                    		obj.aData.DEPART_STATUS="";
                    	}
                    	return obj.aData.DEPART_STATUS+"<a id='edit_status' depart_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                	}                	
                }
            },
            {"mDataProp":"AMOUNT",
            	"sWidth": "40px"},
            {"mDataProp":"SPNAME"},
            {"mDataProp":"DRIVER",
            	"sWidth": "60px"},
            {"mDataProp":"PHONE",
            	"sWidth": "80px"},
            {"mDataProp":"ROUTE_FROM",
            		"sWidth": "80px"},
            {"mDataProp":"ROUTE_TO",
            		"sWidth": "80px"},
            {"mDataProp":"ARRIVAL_MODE",
            		"sWidth": "80px",
                	"fnRender": function(obj) {
                		if(obj.aData.ARRIVAL_MODE == "delivery"){
                			return "货品直送";
                		}else if(obj.aData.ARRIVAL_MODE == "gateIn"){
                			return "入中转仓";
                		}else{
                			return "";
                		}}},
            {"mDataProp":"ARRIVAL_TIME",
            		"sWidth": "100px"},
            {"mDataProp":"PLAN_TIME",
            		"sWidth": "100px"},
            {"mDataProp":"EXCEPTION_RECORD"},
            {"mDataProp":"REMARK"}                
        ]  
    });	
   
    //签收完成
    $("#eeda-table").on('click', '.confirmInWarehouse', function(e){
    	var departOrderId =$(this).attr("code");
    	if(confirm("确定入库吗？")){
    		$.post('/transferOrderMilestone/warehousingConfirm',{departOrderId:departOrderId},function(data){
    			if(data.success){
    				detailTable.fnDraw(); 		
                }else{
                    alert('入库失败');
                }
    		},'json');
        } else {
            return;
        }
    });

    // 收货确认
    $("#eeda-table").on('click', '.confirmReceipt', function(e){
    	var orderId =$(this).attr("code");
    	var departOrderId =$(this).attr("departOrderId");
    	if(confirm("确定收货吗？")){
    		$.post('/transferOrderMilestone/receipt', {orderId:orderId, departOrderId:departOrderId}, function(data){    
    			if(data.success){
    				detailTable.fnDraw(); 		
                }else{
                    alert('收货出错');
                }
        	});
        } else {
            return;
        }
    });
    
    $("#eeda-table").on('click', '#edit_status', function(e){
    	e.preventDefault();	
    	var depart_id=$(this).attr("depart_id");
    	$("#milestoneDepartId").val(depart_id);
    	$.post('/departOrder/transferOrderMilestoneList',{departOrderId:depart_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.empty();
			for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
			{
				var str=data.usernames[j];
				if(str==null){
					str="";
				}
				var exception_record='';
				if(data.transferOrderMilestones[i].EXCEPTION_RECORD!=null)
					exception_record = data.transferOrderMilestones[i].EXCEPTION_RECORD;
				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+exception_record+"</th><th>"+str+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
			}
		},'json');
    	
    });
    
    // 保存新里程碑
	$("#transferOrderMilestoneFormBtn").click(function(){
		$.post('/departOrder/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			var exception_record='';
			var str=data.username;
			if(str==null){
				str="";
			}
			if(data.transferOrderMilestone.EXCEPTION_RECORD!=null)
				exception_record = data.transferOrderMilestone.EXCEPTION_RECORD;
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+exception_record+"</th><th>"+str+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
			detailTable.fnDraw();  
		},'json');
		$("#location").val("");
		$("#exception_record").val("");
		//$('#transferOrderMilestone').modal('hide');
	}); 
	
 
	//供应商暂时去除:#sp_filter ,
    $('#customer_filter,#endTime_filter ,#beginTime_filter  ,#orderNo_filter ,#departNo_filter,#start_filter,#end_filter').on( 'keyup click', function () {
    	
    	var office =$("#officeSelect").val();
    	var start =$("#start_filter").val();
    	var end =$("#end_filter").val();
    	var customer =$("#customer_filter").val();
    	
    	var orderNo = $("#orderNo_filter").val();
    	var departNo_filter = $("#departNo_filter").val();
    	var status = $("#status_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	detailTable.fnSettings().sAjaxSource = "/departOrder/onTripList?orderNo="+orderNo
											+"&departNo="+departNo_filter
											+"&status="+status
											+"&sp="+sp
											+"&beginTime="+beginTime
											+"&endTime="+endTime
											+"&office="+office
											+"&start="+start
											+"&end="+end
											+"&customer="+customer;
    	detailTable.fnDraw();
    });
    $('#status_filter,#officeSelect').on( 'change', function () {
    	
    	var office =$("#officeSelect").val();
    	var start =$("#start_filter").val();
    	var end =$("#end_filter").val();
    	var customer =$("#customer_filter").val();
    	
    	var orderNo = $("#orderNo_filter").val();
    	var departNo_filter = $("#departNo_filter").val();
    	var status = $("#status_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	detailTable.fnSettings().sAjaxSource = "/departOrder/onTripList?orderNo="+orderNo
											+"&departNo="+departNo_filter
											+"&status="+status
											+"&sp="+sp
											+"&beginTime="+beginTime
											+"&endTime="+endTime
											+"&office="+office
											+"&start="+start
											+"&end="+end
											+"&customer="+customer;
    	detailTable.fnDraw();
    });
	
    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter').trigger('keyup');
    });


    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter').trigger('keyup');
    });
    
    
  //获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter').on('keyup click', function(){
		var inputStr = $('#sp_filter').val();
		$.get('/departOrder/companyNameList',{input:inputStr}, function(data){
			console.log(data);
			var cpnameList =$("#cpnameList");
			cpnameList.empty();
			for(var i = 0; i < data.length; i++)
			{
				cpnameList.append("<li><a tabindex='-1' class='fromLocationItem' >"+data[i].COMPANY+"</a></li>");
			}
		},'json');

		$("#cpnameList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#cpnameList').show();
	});

	// 没选中供应商，焦点离开，隐藏列表
	$('#sp_filter').on('blur', function(){
 		$('#cpnameList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#cpnameList').on('blur', function(){
 		$('#cpnameList').hide();
 	});

	$('#cpnameList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});

	// 选中供应商
	$('#cpnameList').on('mousedown', '.fromLocationItem', function(e){
		console.log($('#cpnameList').is(":focus"));
		var message = $(this).text();
		$('#sp_filter').val(message);
        $('#cpnameList').hide();
        
        var office =$("#officeSelect").val();
    	var start =$("#start_filter").val();
    	var end =$("#end_filter").val();
    	var customer =$("#customer_filter").val();
        
        var orderNo = $("#orderNo_filter").val();
    	var departNo_filter = $("#departNo_filter").val();
    	var status = $("#status_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	detailTable.fnSettings().sAjaxSource = "/departOrder/onTripList?orderNo="+orderNo
    										+"&departNo="+departNo_filter
    										+"&status="+status
    										+"&sp="+sp
    										+"&beginTime="+beginTime
    										+"&endTime="+endTime
    										+"&office="+office
    										+"&start="+start
    										+"&end="+end
    										+"&customer="+customer;
    	detailTable.fnDraw();
        
    });
	 //获取客户列表，自动填充
    $('#customer_filter').on('keyup click', function(){
        var inputStr = $('#customer_filter').val();
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
            console.log(data);
            var companyList =$("#companyList");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
            }
            if(data.length>0)
                companyList.show();
            
        },'json');

    });
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).text());
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId);
        
        var office =$("#officeSelect").val();
    	var start =$("#start_filter").val();
    	var end =$("#end_filter").val();
    	var customer =$("#customer_filter").val();
    	
        
        var orderNo = $("#orderNo_filter").val();
    	var departNo_filter = $("#departNo_filter").val();
    	var status = $("#status_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	detailTable.fnSettings().sAjaxSource = "/departOrder/onTripList?orderNo="+orderNo
											+"&departNo="+departNo_filter
											+"&status="+status
											+"&sp="+sp
											+"&beginTime="+beginTime
											+"&endTime="+endTime
											+"&office="+office
											+"&start="+start
											+"&end="+end
											+"&customer="+customer;
    	detailTable.fnDraw();
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
  //获取所有的网点

	$.post('/transferOrder/searchAllOffice',function(data){
	 if(data.length > 0){
		 var officeSelect = $("#officeSelect");
		 officeSelect.empty();
		 officeSelect.append("<option ></option>");
		
		 for(var i=0; i<data.length; i++){
			 officeSelect.append("<option value='"+data[i].OFFICE_NAME+"'>"+data[i].OFFICE_NAME+"</option>");					 
		 }
		
	 }
 },'json');
} );