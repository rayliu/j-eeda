
$(document).ready(function() {
	document.title = '运输在途查询 | '+document.title;
    $('#menu_status').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var detailTable = $('#eeda-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/departOrder/onTripList",
        "aoColumns": [
			{ 
			    "mDataProp": null,
			    "sWidth": "60px",
			    "bVisible":DepartOnTrip.isUpdate,
			    "fnRender": function(obj) {
			    	console.log(obj.aData.ARRIVAL_MODE);
			    	if(obj.aData.DEPART_STATUS != '已入库' && obj.aData.DEPART_STATUS != '已收货'){
			    		if(obj.aData.ARRIVAL_MODE == '货品直送'){
				    		return "<a class='btn  btn-primary confirmReceipt' departOrderId='"+obj.aData.ID+"' code='"+obj.aData.ORDER_ID+"'>"+"收货确认"+"</a>";
				    	}else{
				    		return "<a class='btn  btn-primary confirmInWarehouse' code='"+obj.aData.ID+"'>"+"入库确认"+"</a>";
				    	}
			    	}else{
			    		if(obj.aData.DEPART_STATUS == '已入库'){
			    			return "<a class='btn  btn-danger deleteInWarehouse' code='"+obj.aData.ID+"'>"+"撤销入库"+"</a>";
			    		}else{
			    			return "<a class='btn  btn-danger deleteReceipt' code='"+obj.aData.ID+"'>"+"撤销收货"+"</a>";
			    		}
			    	}
			    }
			},
            {"mDataProp":"OFFICE_NAME","sWidth": "80px"},        
            {"mDataProp":null,
            	"sWidth": "100px",
            	"fnRender": function(obj) {
            		if(DepartOrder.isUpdate || DepartOrder.isComplete){
            			return "<a href='/departOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.DEPART_NO+"</a>";
            		}else{
            			return obj.aData.DEPART_NO;
            		}
            		 
            	}
            },
            {"mDataProp":null,
            	"sWidth": "90px",
                "fnRender": function(obj) {
                	
                	if(obj.aData.LOCATION!=null && obj.aData.LOCATION!=''){
                		if(DepartOrder.isUpdate){
                			if(obj.aData.DEPART_STATUS == '已入库' || obj.aData.DEPART_STATUS == '已收货'){
                				return obj.aData.LOCATION;
                			}else{
                				return obj.aData.LOCATION+"<a id='edit_status' depart_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                			}
                			
                		}else{
                			return obj.aData.LOCATION;
                		}
                		
                	}else{
                    	
                    	if(DepartOrder.isUpdate){
                    		if(obj.aData.DEPART_STATUS==null){
                        		return "";
                        	}else{
                        		if(obj.aData.DEPART_STATUS == '已入库' || obj.aData.DEPART_STATUS == '已收货'){
                    				return obj.aData.DEPART_STATUS;
                    			}else{
                    				return obj.aData.DEPART_STATUS+"<a id='edit_status' depart_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                    			}
                        	}
                    		
                    	}else{
                    		if(obj.aData.DEPART_STATUS==null){
                    			obj.aData.DEPART_STATUS='';
                    		}
                    		return obj.aData.DEPART_STATUS;
                    	}
                    	
                	}                	
                }
            },
            {"mDataProp":null,
            	"sWidth": "70px", "fnRender": function(obj) {
            		return '详情'+"<a id='edit_detail' depart_id="+obj.aData.ID+" data-target='#itemDetail' data-toggle='modal'><i class='fa fa-edit fa-fw'></i></a>";
            	}
    		},
            {"mDataProp":"PLANNING_TIME",
            	"sWidth": "70px"},
            {"mDataProp":"DEPARTURE_TIME",
            	"sWidth": "70px"},
            {"mDataProp":"TRANSFER_ORDER_NO",
            	"sWidth": "100px"},
        	 {"mDataProp":"CUSTOMER_ORDER_NO",
            	"sWidth": "100px"},
            {"mDataProp":"CNAME",
            	"sWidth": "100px"}, 
            {"mDataProp":null,
            	"sWidth": "80px",
            	"fnRender": function(obj) {
            			return obj.aData.TRIP_TYPE;
            	}},
            {"mDataProp":"ROUTE_TO",
            		"sWidth": "80px"},
            {"mDataProp":"AMOUNT",
            	"sWidth": "40px"},
            {"mDataProp":"SPNAME"},
            {"mDataProp":"DRIVER",
            	"sWidth": "60px"},
            {"mDataProp":"PHONE",
            	"sWidth": "80px"},
            {"mDataProp":"ROUTE_FROM",
            		"sWidth": "80px"},
            {"mDataProp":"ARRIVAL_MODE",
            		"sWidth": "80px",
                	"fnRender": function(obj) {
                		if(obj.aData.ARRIVAL_MODE == "delivery" || obj.aData.ARRIVAL_MODE == "deliveryToWarehouse" || obj.aData.ARRIVAL_MODE == "deliveryToFactory"
                			|| obj.aData.ARRIVAL_MODE == "deliveryToFachtoryFromWarehouse"){
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
    
    var itemDetailTable = $('#itemDetail-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/departOrder/getItemDetail",
        "aoColumns": [
            {"mDataProp":"ID"},
            {"mDataProp":"ITEM_NO"},
            {"mDataProp":"SERIAL_NO"}                
        ]  
    });	
    
    
    
    $("#eeda-table").on('click', '#edit_detail', function(e){
    	e.preventDefault();	
    	var depart_id = $(this).attr("depart_id");
    	itemDetailTable.fnSettings().sAjaxSource = "/departOrder/getItemDetail?depart_id="+depart_id;
    	itemDetailTable.fnDraw();
    });
   
    //入库的确认
    $("#eeda-table").on('click', '.confirmInWarehouse', function(e){
    	var departOrderId =$(this).attr("code");
    	$(this).attr("disabled",true);
    	if(confirm("确定入库吗？")){
    		$.post('/transferOrderMilestone/warehousingConfirm',{departOrderId:departOrderId},function(data){
    			if(data.success){
    				detailTable.fnDraw(); 
    				$.scojs_message('已入库', $.scojs_message.TYPE_OK);
    				
                }else{
                	detailTable.fnDraw();
                	$.scojs_message('入库失败,请联系后台管理员查询原因', $.scojs_message.TYPE_OK);
                	$(this).attr("disabled",false);
                }
    		},'json');
        } else {
        	$(this).attr("disabled",false);
            return;
        }
    });
    
    
  //撤销入库
    $("#eeda-table").on('click', '.deleteInWarehouse', function(e){
    	var order_id =$(this).attr("code");
    	$(this).attr("disabled",true);
//    	$.scojs_message('开发中(暂不支持撤销)', $.scojs_message.TYPE_FAIL);
//    	return;
    	if(confirm("确定撤销入库吗？")){
    		$.post('/departOrder/deleteInWarehouse',{order_id:order_id},function(data){
    			if(data.success){
    				$.scojs_message('撤销成功', $.scojs_message.TYPE_OK);
    				detailTable.fnDraw(); 
                }else{
                	detailTable.fnDraw();
                	$.scojs_message('撤销失败,可能存在下级单据(或单据为普货单据，暂不支持撤销)', $.scojs_message.TYPE_FAIL);
                	$(this).attr("disabled",false);
                }
    		},'json');
        } else {
        	$(this).attr("disabled",false);
            return;
        }
    });

    // 收货确认
    $("#eeda-table").on('click', '.confirmReceipt', function(e){
    	$(this).attr("disabled",true);
    	var orderId =$(this).attr("code");
    	var departOrderId =$(this).attr("departOrderId");
    	if(confirm("确定收货吗？")){	
    		$.post('/transferOrderMilestone/receipt', {orderId:orderId, departOrderId:departOrderId}, function(data){    
    			if(data.success){
    				detailTable.fnDraw();
    				$.scojs_message('已收货', $.scojs_message.TYPE_OK);	
                }else{
                	$(this).attr("disabled",false);
                	$.scojs_message('收货失败,请联系后台管理员查询原因', $.scojs_message.TYPE_FALSE);
                	$(this).attr("disabled",false);
                }
        	});
        } else {
        	$(this).attr("disabled",false);
            return;
        }
    });
    
    
 // 撤销收货
    $("#eeda-table").on('click', '.deleteReceipt', function(e){
    	$(this).attr("disabled",true);
//    	$.scojs_message('开发中(暂不支持撤销)', $.scojs_message.TYPE_FAIL);
//    	return;
    	var order_id =$(this).attr("code");
    	if(confirm("确定撤销收货吗？")){	
    		$.post('/departOrder/deleteReceipt', { order_id:order_id}, function(data){    
    			if(data.success){
    				$.scojs_message('撤销成功', $.scojs_message.TYPE_OK);
    				detailTable.fnDraw(); 
                }else{
                	detailTable.fnDraw();
                	$.scojs_message('撤销失败,可能存在下级单据)', $.scojs_message.TYPE_FAIL);
                	$(this).attr("disabled",false);
                }
        	});
        } else {
        	$(this).attr("disabled",false);
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
	
	
	var searchMassage = function(){
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
    	var planBeginTime = $("#planBeginTime").val();
    	var planEndTime = $("#planEndTime").val();
    	detailTable.fnSettings().sAjaxSource = "/departOrder/onTripList?orderNo="+orderNo
											+"&departNo="+departNo_filter
											+"&status="+status
											+"&sp="+sp
											+"&beginTime="+beginTime
											+"&endTime="+endTime
											+"&planBeginTime="+planBeginTime
											+"&planEndTime="+planEndTime
											+"&office="+office
											+"&start="+start
											+"&end="+end
											+"&customer="+customer;
	};
	
	
	
    $("#searchBtn").on('click',function(){
    	searchMassage();
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
    
    $('#datetimepicker3').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter').trigger('keyup');
    });
    
    $('#datetimepicker4').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter').trigger('keyup');
    });
    
    $('#sp_filter').on('blur', function(){
 		$('#cpnameList').hide();
 	});
  //获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter').on('input click', function(){
    	var me=this;
		var inputStr = $('#sp_filter').val();
		$.get('/transferOrder/searchSp',{input:inputStr}, function(data){
			if(inputStr!=$('#sp_filter').val()){//查询条件与当前输入值不相等，返回
				return;
			}
			var cpnameList =$("#cpnameList");
			cpnameList.empty();
			for(var i = 0; i < data.length; i++)
			{
				cpnameList.append("<li><a tabindex='-1' class='fromLocationItem' >"+data[i].COMPANY_NAME+"</a></li>");
			}
			$("#cpnameList").css({ 
	        	left:$(me).position().left+"px", 
	        	top:$(me).position().top+32+"px" 
	        }); 
	        $('#cpnameList').show();
		},'json');

		
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
	/*----------------------------提示所需JS-----------------------------------*/
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	/*--------------------------------------------------------------------*/
} );