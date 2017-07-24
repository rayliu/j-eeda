
$(document).ready(function() {
	document.title = '手工成本单查询 | '+document.title;

    $('#menu_cost').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var datatable=$('#costMiscOrderList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "bSort": false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costMiscOrder/list",
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			$(nRow).attr({audit_status: aData.AUDIT_STATUS}); 
		},
        "aoColumns": [ 
			{ "mDataProp":null,"sWidth": "30px",
				"fnRender": function(obj) {
					var status="disabled";
					var creator=$("#creator").val();
					if(obj.aData.CREATE_BY==creator){
						status="";
					}
			      return '<button type="button" class="btn btn-primary delete btn-xs" '+status+' >'+
			            '<i class="fa fa-trash-o"></i> 删除</button>';
			    }
			},
            {"mDataProp":"ORDER_NO","sWidth": "80px",
            	"fnRender": function(obj) {
        			return "<a href='/costMiscOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"COST_TO_TYPE","sWidth": "100px",
                "fnRender": function(obj) {
                    if(obj.aData.COST_TO_TYPE=='customer'){
                        return '客户';
                    }else if(obj.aData.COST_TO_TYPE=='sp'){
                        return '供应商';
                    }else{
                        return '其他';
                    }
                }
            },
            {"mDataProp":"TYPE","sWidth": "100px",
                "fnRender": function(obj) {
                    if(obj.aData.TYPE=='biz'){
                        return '业务收款';
                    }else{
                        return '非业务收款';
                    }
                }
            },
            {"mDataProp":"CUSTOMER_NAME","sWidth": "100px"},
            {"mDataProp":"SP_NAME","sWidth": "100px"},
            {"mDataProp":"OTHERS_NAME","sWidth": "100px"},
            {"mDataProp":"TOTAL_AMOUNT","sWidth": "100px"},
            {"mDataProp":"AUDIT_STATUS","sWidth": "100px"},
            {"mDataProp":"CREATE_STAMP","sWidth": "150px"},
            {"mDataProp":"REF_NO","sWidth": "150px"},
            {"mDataProp":"REMARK","sWidth": "150px"}                       
        ]      
    });	 
    
    
    $('#costMiscOrderList-table').on('click','.delete',function(){
    	var self = this;
    	$(this).prop('disabled',true);
    	var tr = $(this).parent().parent();
    	var id = tr.attr('id');
    	var audit_status = tr.attr('audit_status');

    	if(!confirm("是否确定删除？")){
    		$(this).prop('disabled',false);
    		return false;
    		
    	}
    	
    	if(audit_status!='新建'){
    		$.scojs_message('单据已存在财务单据，需要先撤销对应财务单据方可删除', $.scojs_message.TYPE_FAIL);
    		$(this).prop('disabled',false);
    		return false;
    	}
    	
    	
    	if(id>0){
    		$.post('/costMiscOrder/delete', {id:id}, function(data){
    			if(data){
    				$.scojs_message("删除成功", $.scojs_message.TYPE_OK);
    				refreshData();
    			}else{
    				$.scojs_message("删除失败", $.scojs_message.TYPE_FAIL);
    			}
    		}).fail(function() {
    	        $.scojs_message('删除失败', $.scojs_message.TYPE_FAIL);
    	   });
    	}
    	
    });
    
    
  //获取客户的list，选中信息自动填写其他信息
    $('#companyName').on('keyup click', function(){
        var inputStr = $('#companyName').val();
        
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

        if(inputStr==''){
        	costCheckTable.fnFilter('', 2);
        }
        
    });
    // 没选中客户，焦点离开，隐藏列表
    $('#companyName').on('blur', function(){
        $('#companyList').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#companyList').on('blur', function(){
        $('#companyList').hide();
    });

    $('#companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    $('#beginTime_filter').on('keyup', function () {
    	refreshData();
    } );    
    $("#status_filter").on('change', function () {
        refreshData();
    });
    $('#endTime_filter').on( 'keyup click', function () {
    	refreshData();
    } );   

    $('#datetimepicker3').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter').trigger('keyup');
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
    $('#customer_filter').on('keyup click', function(event){
        var me = this;
        var inputStr = $('#customer_filter').val();
        var companyList =$("#companyList");
        $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
            companyList.empty();
            for(var i = 0; i < data.length; i++)
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
                
            companyList.css({ 
		    	left:$(me).position().left+"px", 
		    	top:$(me).position().top+32+"px" 
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
  //供应商查询
    //获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter').on('input click', function(){
    	var me = this;
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
			spList.css({ 
	        	left:$(me).position().left+"px", 
	        	top:$(me).position().top+32+"px" 
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
        refreshData();
    });
	$("#orderNo_filter").on( 'keyup click', function () {
    	refreshData();
    });
    var refreshData=function(){
        var orderNo = $("#orderNo_filter").val();//单号
        var status = $("#status_filter").val();
        var customer = $("#customer_filter").val();
        var sp = $("#sp_filter").val();
        var beginTime = $("#beginTime_filter").val();
        var endTime = $("#endTime_filter").val();
        datatable.fnSettings().sAjaxSource = "/costMiscOrder/list?companyName="+customer+"&beginTime="+beginTime+"&endTime="+endTime+"&spName="+sp+"&orderNo="+orderNo+"&status="+status;
        datatable.fnDraw();
    };
} );