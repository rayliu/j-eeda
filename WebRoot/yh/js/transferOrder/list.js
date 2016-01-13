
$(document).ready(function() {

    document.title = '运输单查询 | '+document.title;

    $('#menu_transfer').addClass('active').find('ul').addClass('in');
       
	//datatable, 动态处理
    var transferOrder = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示        
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt",//datatable的中文翻译
            "sProcessing":     "Procesando..."
        },
        //"sAjaxSource": "/transferOrder/list",
        "aoColumns": [   
            {"mDataProp":"ORDER_NO", "sWidth":"70px",
            	"fnRender": function(obj) {
        			if(TransferOrder.isUpdate)
        				return "<a href='/transferOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        			else
        				return obj.aData.ORDER_NO;
        		}
            },
            {"mDataProp":"PLANNING_TIME", "sWidth":"100px"},
            {"mDataProp":"STATUS", "sWidth":"90px"},
            {"mDataProp":"CNAME", "sWidth":"100px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"80px"},
            {"mDataProp":"ORDER_TYPE", "sWidth":"60px",
            	"fnRender": function(obj) {
            		if(obj.aData.ORDER_TYPE == "salesOrder")
            			return "销售订单";
            		else if(obj.aData.ORDER_TYPE == "replenishmentOrder")
            			return "补货订单";
            		else if(obj.aData.ORDER_TYPE == "arrangementOrder")
            			return "调拨订单";
            		else if(obj.aData.ORDER_TYPE == "cargoReturnOrder")
            			return "退货订单";
            		else if(obj.aData.ORDER_TYPE == "damageReturnOrder")
            			return "质量退单";
            		else if(obj.aData.ORDER_TYPE == "gateOutTransferOrder")
            			return "出库运输单";
            		else if(obj.aData.ORDER_TYPE == "movesOrder")
            			return "移机单";
            		else
            			return "";
        		}
            },
            {"mDataProp":"CARGO_NATURE", "sWidth":"60px",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo")
            			return "普通货品";
            		else if(obj.aData.CARGO_NATURE == "damageCargo")
            			return "损坏货品";
            		else if(obj.aData.CARGO_NATURE == "ATM")
            			return TransferOrder.ex_cargo;
            		else
            			return "";
        		}
            }, 
            {"mDataProp":"ROUTE_FROM", "sWidth":"60px"},
            {"mDataProp":"ROUTE_TO", "sWidth":"60px"},       	
    		{"mDataProp":"OPERATION_TYPE", "sWidth":"60px",
    			"fnRender": function(obj) {
    				if(obj.aData.OPERATION_TYPE == "out_source")
    					return "外包";
    				else if(obj.aData.OPERATION_TYPE == "own")
    					return "自营";
    				else
    					return "";
				}
            },
    		{"mDataProp":"ARRIVAL_MODE", "sWidth":"60px",
            	"fnRender": function(obj) {
            		if(obj.aData.ARRIVAL_MODE == "delivery")
            			return "货品直送";
            		else if(obj.aData.ARRIVAL_MODE == "gateIn")
            			return "入中转仓";
            		else
            			return "";
        		}
            },
            {"mDataProp":"AMOUNT", "sWidth":"50px"},
            {"mDataProp":"PIECES", "sWidth":"50px"},
            {"mDataProp":"VOLUME", "sWidth":"50px"},
            {"mDataProp":"WEIGHT", "sWidth":"50px"},
            {"mDataProp":"ARRIVAL_TIME", "sWidth":"90px"},
            {"mDataProp":"ADDRESS", "sWidth":"100px",},
            {"mDataProp":"SPNAME", "sWidth":"200px"},
            {"mDataProp":"ONAME", "sWidth":"150px"},
            {"mDataProp":"PICKUP_MODE", "sWidth":"60px",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == "routeSP")
            			return "干线供应商自提";
            		else if(obj.aData.PICKUP_MODE == "pickupSP")
            			return "外包供应商提货";
            		else if(obj.aData.PICKUP_MODE == "own")
            			return TransferOrder.ex_type ;
            		else
            			return "";
        		}
            },
            {"mDataProp":"CREATE_STAMP", "sWidth":"80px",
    			"fnRender":function(obj){
    				var create_stamp=obj.aData.CREATE_STAMP;
    				var str=create_stamp.substr(0,10);
    				return str;
    			}
            },
            {"mDataProp":"REMARK"}
        ]  
    });	
    
    $("#dowmload").on('click', function(e){
    	if(confirm("确认下载吗？"))
    		window.location.href="/transferOrder/downloadTransferOrderTemplate";
    });
    
    $("#eeda-table").on('click', '.cancelbutton', function(e){
    	e.preventDefault();
    	//异步向后台提交数据
    	var id = $(this).attr('code');
		$.post('/transferOrder/cancel/'+id,function(data){
			//保存成功后，刷新列表
			console.log(data);
			if(data.success)
				transferOrder.fnDraw();
			else
				alert('取消失败');
		},'json');
	});

    //获取客户列表，自动填充
    $('#customer_filter').on('keyup click', function(event){
        var me = this;
        var inputStr = $('#customer_filter').val();
        var companyList =$("#companyList");
        $.get("/customerContract/search", {customerName:inputStr}, function(data){
            companyList.empty();
            for(var i = 0; i < data.length; i++)
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
                
            companyList.css({ 
		    	left:$(me).position().left+"px", 
		    	top:$(me).position().top+28+"px" 
		    });
	        companyList.show();    
        },'json');
        /*if(inputStr=='')
        	transferOrder.fnFilter('', 2);*/
        
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

    //供应商查询
    //获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter').on('input click', function(){
    	var me = this;
		var inputStr = $('#sp_filter').val();
		var spList =$("#spList");

		$.get('/customerContract/searchSp', {spName:inputStr}, function(data){
			if(inputStr!=$('#sp_filter').val()){//查询条件与当前输入值不相等，返回
				return;
			}
			spList.empty();
			for(var i = 0; i < data.length; i++){
				var abbr = data[i].ABBR;
				var company_name = data[i].COMPANY_NAME;
				var contact_person = data[i].CONTACT_PERSON;
				var phone = data[i].PHONE;
				
				if(abbr == null) 
					abbr = '';
				if(company_name == null)
					company_name = '';
				if(contact_person == null)
					contact_person = '';
				if(phone == null)
					phone = '';
				
				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
			}
			spList.css({ 
	        	left:$(me).position().left+"px", 
	        	top:$(me).position().top+28+"px" 
	        }); 
			
			spList.show();
			
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
		if(contact_person == 'null')
			contact_person = '';
		pageSpName.append(contact_person+'&nbsp;');
		var phone = $(this).attr('phone');
		if(phone == 'null')
			phone = '';
		pageSpName.append(phone); 
		pageSpAddress.empty();
		var address = $(this).attr('address');
		if(address == 'null')
			address = '';
		pageSpAddress.append(address);
        $('#spList').hide();
    });

    
    // 导入运输单
    $("#fileUploadBtn").click(function(){
    	$("#toFileUpload").click();
    });
    var str=null;
    var errCustomerNo=null;
    var errCustomerNoArr=[];
	$('#toFileUpload').fileupload({
        dataType: 'json',
        done: function (e, data) {
        	$("#footer").show();
        	$("#centerBody").empty().append("<h4>"+data.result.cause+"</h4>");
        	if(data.result.equal=="客户订单号已存在"){
        		$("#confirm").show();
        		errCustomerNoArr.push(data.result.errCustomerNo)
        		$("#customer").val(errCustomerNoArr);
        		errCustomerNo=$("#customer").val();
        		str=data.result.strFile;
        	}
        	transferOrder.fnDraw();
        },  
        progressall: function (e, data) {//设置上传进度事件的回调函数  
        	str=null;
            errCustomerNo=null;
            errCustomerNoArr=[];
        	$('#centerBody').empty().append('<img src="/yh/image/loading5.gif" width="20%"><h4>导入过程可能需要一点时间，请勿退出页面！</h4>');
        	$('#myModal').modal('show');
        	$("#footer").hide();
        } 
    }).error(function (jqXHR, textStatus, errorThrown) {
        alert("出错了，请刷新页面重新尝试。")
        console.log(errorThrown);
        // if (errorThrown === 'abort') {
        //     alert('File Upload has been canceled');
        // }
    });;
    $('#confirm').click(function(){
    	$("#confirm").hide();
    	$('#centerBody').empty().append('<img src="/yh/image/loading5.gif" width="20%"><h4>导入过程可能需要一点时间，请勿退出页面！</h4>');
    	$.post('/transferOrder/importTransferOrder', {str:str,errCustomerNo:errCustomerNo}, function(data){
    		if(data.equal=="客户订单号已存在"){
    			$("#confirm").show();
    			str=data.strFileConfirm
    			errCustomerNoArr.push(data.errCustomerNo)
        		$("#customer").val(errCustomerNoArr);
    			errCustomerNo=$("#customer").val();
    		}else{
    			str=null;
        		errCustomerNo=null;
    		}
    		$('#centerBody').empty().append("<h4>"+data.cause+"</h4>");
    		
    	})
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
    
    $('#arrivaldatetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#arrivaltime').trigger('keyup');
    });

    $('#plandatetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#plantime').trigger('keyup');
    });

    $("#searchBtn").click(function(){
        refreshData();
    });

    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
        saveConditions();
    });

    var saveConditions=function(){
        var conditions={
            order_type:$("#order_type_filter").val(),
            plantime :$("#plantime").val(),
            arrivarltime:$("#arrivaltime").val(),
            customer_order_no:$("#order_no_filter").val(),
            orderNo : $("#orderNo_filter").val(),
            status : $("#status_filter").val(),
            address : $("#address_filter").val(),
            customer : $("#customer_filter").val(),
            sp : $("#sp_filter").val(),
            beginTime : $("#beginTime_filter").val(),
            endTime : $("#endTime_filter").val(),
            officeName : $("#officeSelect").val(),
            operation_type : $("#operation_type_filter").val(),
            to_route : $("#to_route").val()
        };
        if(!!window.localStorage){//查询条件处理
            localStorage.setItem("query_to", JSON.stringify(conditions));
        }
    };

    var refreshData=function(){
        var order_type=$("#order_type_filter").val();
        var plantime=$("#plantime").val();
        var arrivarltime=$("#arrivaltime").val();
        var customer_order_no=$("#order_no_filter").val();
        var orderNo = $("#orderNo_filter").val();
        var status = $("#status_filter").val();
        var address = $("#address_filter").val();
        var customer = $("#customer_filter").val();
        var sp = $("#sp_filter").val();
        var beginTime = $("#beginTime_filter").val();
        var endTime = $("#endTime_filter").val();
        var officeName = $("#officeSelect").val();
        var operation_type = $("#operation_type_filter").val();
        var to_route = $("#to_route").val();

        transferOrder.fnSettings().oFeatures.bServerSide = true;
        transferOrder.fnSettings().sAjaxSource = "/transferOrder/list?orderNo="+orderNo +"&status="+status+"&address=" +address
                                                +"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime
                                                +"&endTime="+endTime+"&officeName="+officeName
                                                +"&order_type="+order_type
                                                +"&plantime="+plantime+"&arrivarltime="+arrivarltime
                                                +"&customer_order_no="+customer_order_no
                                                +"&to_route="+to_route
                                                +"&operation_type="+operation_type;
        transferOrder.fnDraw(); 
        saveConditions();
    };

    var loadConditions=function(){
        if(!!window.localStorage){//查询条件处理
            var query_to = localStorage.getItem('query_to');
            if(!query_to)
                return;

            var conditions = JSON.parse(localStorage.getItem('query_to'));
            $("#order_type_filter").val(conditions.order_type);
            $("#plantime").val(conditions.plantime);
            $("#arrivaltime").val(conditions.arrivarltime);
            $("#order_no_filter").val(conditions.customer_order_no);
            $("#orderNo_filter").val(conditions.orderNo);
            $("#status_filter").val(conditions.status);
            $("#address_filter").val(conditions.address);
            $("#customer_filter").val(conditions.customer);
            $("#sp_filter").val(conditions.sp);
            $("#beginTime_filter").val(conditions.beginTime);
            $("#endTime_filter").val(conditions.endTime);
            $("#officeSelect").val(conditions.officeName);
            $("#operation_type_filter").val(conditions.operation_type);
            $("#to_route").val(conditions.to_route);
        }
    };

    loadConditions();

} );