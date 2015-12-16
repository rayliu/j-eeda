
$(document).ready(function() {

    document.title = '退货单查询 | '+document.title;

    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');
    
    var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
    '</div>';
    
	//datatable, 动态处理
    var transferOrder = $('#eeda-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示  
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/returnTransfer/list",
        "aoColumns": [   
            {"mDataProp":"ORDER_NO", "sWidth":"70px",
            	"fnRender": function(obj) {
        			if(TransferOrder.isUpdate != ""){
        				return "<a href='/returnTransfer/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        			}else{
        				return obj.aData.ORDER_NO;
        			}  
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
    
    var refreshData=function(){
    	var order_type=$("#order_type_filter").val();
    	var plantime=$("#plantime").val();
    	var arrivarltime=$("#arrivaltime").val();
    	var customer_order_no=$("#customer_order_no_filter").val();
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var officeName = $("#officeSelect").val();

        transferOrder.fnSettings().oFeatures.bServerSide = true;
    	transferOrder.fnSettings().sAjaxSource = "/returnTransfer/list?orderNo="+orderNo +"&status="+status+"&address=" +address
    											+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime
    											+"&endTime="+endTime+"&officeName="+officeName
    											+"&order_type="+order_type
    											+"&plantime="+plantime+"&arrivarltime="+arrivarltime
    											+"&customer_order_no="+customer_order_no;
    	transferOrder.fnDraw(); 
    };
    
    //获取所有的网点
	$.post('/transferOrder/searchPartOffice',function(data){
	 if(data.length > 0){
		 var officeSelect = $("#officeSelect");
		 officeSelect.empty();
		 officeSelect.append("<option ></option>");
		 for(var i=0; i<data.length; i++)
			 officeSelect.append("<option value='"+data[i].OFFICE_NAME+"'>"+data[i].OFFICE_NAME+"</option>");					 
	 }
	},'json');

    //获取客户列表，自动填充
    $('#customer_filter').on('keyup click', function(){
        var inputStr = $('#customer_filter').val();
        var companyList =$("#companyList");
        $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
            companyList.empty();
            for(var i = 0; i < data.length; i++)
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
        },'json');
        if(inputStr=='')
        	transferOrder.fnFilter('', 2);
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

    //供应商查询
    //获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter').on('input click', function(){
		var inputStr = $('#sp_filter').val();
		var spList =$("#spList");
		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
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
		},'json');
		
		spList.css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
		
		spList.show();
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

    
    $("#searchBtn").click(function(){
        refreshData();
    });

    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
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

} );