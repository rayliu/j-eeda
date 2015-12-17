 $(document).ready(function() {
	document.title = '调车单查询 | '+document.title;
	$('#menu_assign').addClass('active').find('ul').addClass('in');
    	
	var pickupOrder = $('#dataTables-example').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示  
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/pickupOrder/pickuplist",
        "aoColumns": [   
		    {"mDataProp":"DEPART_NO", "sWidth":"70px",
            	"fnRender": function(obj) {
            		if(Pickup.isUpdate || Pickup.isCompleted)
            			return "<a href='/pickupOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.DEPART_NO+"</a>";
            		else
            			return obj.aData.DEPART_NO;
            	}
		    },
		    {"mDataProp":"TURNOUT_TIME", "sWidth":"80px",
		    	"fnRender":function(obj){
    				var create_stamp=obj.aData.TURNOUT_TIME;
    				var str=create_stamp.substr(0,10);
    				return str;
    			}
		    }, 
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"80px"},
            {"mDataProp":"OFFICE_NAME", "sWidth":"70px"},
		    {"mDataProp":"STATUS", "sWidth":"60px"},
		    {"mDataProp":"PICKUP_MODE", "sWidth":"90px",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == "routeSP")
            			return "干线供应商自提";
            		else if(obj.aData.PICKUP_MODE == "pickupSP")
            			return "外包供应商提货";
            		else if(obj.aData.PICKUP_MODE == "own")
            				return Pickup.ex_type;
            		else
            			return "";
            	}
		    },
            {"mDataProp":"CAR_NO", "sWidth":"70px"},
		    {"mDataProp":"SP_NAME", "sWidth":"60px"},
		    {"mDataProp":"COST_AMOUNT", "sWidth":"60px"},
		    {"mDataProp":"PHONE", "sWidth":"80px"},
		    {"mDataProp":"CARTYPE", "sWidth":"80px"},     
		    
		    {"mDataProp":null, "sWidth":"50px",
				"fnRender":function(obj){
    				return obj.aData.ATMVOLUME + obj.aData.CARGOVOLUME;
    			}
		    },
		    {"mDataProp":null, "sWidth":"50px",
				"fnRender":function(obj){
    				return obj.aData.ATMWEIGHT + obj.aData.CARGOWEIGHT;
    			}
    		},
    		{"mDataProp":"CUSTOMERNAMES", "sWidth":"100px"},
		    {"mDataProp":"USER_NAME", "sWidth":"90px"},
		    {"mDataProp":"REMARK"}
        ]      
    });	

    $("#dataTables-example").on('click', '.cancelbutton', function(e){
		e.preventDefault();
       //异步向后台提交数据
	   var id = $(this).attr('code');
	   $.post('/pickupOrder/cancel/'+id,function(data){
           //保存成功后，刷新列表
           console.log(data);
           if(data.success)
        	   pickupOrder.fnDraw();
           else
               alert('取消失败');
       },'json');
	});

	$("#searchBtn").click(function(){
        refreshData();
    });

    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
    });
    
    var refreshData=function(){
    	var sp_filter = $("#sp_filter").val();
    	var carNo = $("#carNo_filter").val();
    	var take = $("#take_filter").val();
    	var status = $("#status_filter").val();
    	var office =$("#officeSelect").val();
    	var customer_filter =$("#customer_filter").val();
    	var orderNo = $("#orderNo_filter").val();
		var departNo_filter = $("#departNo_filter").val();
		var beginTime = $("#beginTime_filter").val();
		var endTime = $("#endTime_filter").val();

		pickupOrder.fnSettings().oFeatures.bServerSide = true;
		pickupOrder.fnSettings().sAjaxSource = "/pickupOrder/pickuplist?orderNo="+orderNo+"&departNo="+departNo_filter+"&beginTime="+beginTime+"&endTime="+endTime+"&carNo="+carNo+"&take="+take+"&status="+status+"&office="+office+"&customer_filter="+customer_filter+"&sp_filter="+sp_filter;
		pickupOrder.fnDraw();
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
        var companyList = $("#companyList");
        if(inputStr == ""){
        	$("#customer_id").val(null);
        }
    	$.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
	        companyList.empty();
	        for(var i = 0; i < data.length; i++)
	            companyList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"'>"+data[i].ABBR+"</a></li>");
        },'json');
    	companyList.css({ 
 	    	left:$(this).position().left+"px", 
 	    	top:$(this).position().top+32+"px" 
 	    }); 
        companyList.show();
    });
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).text());
        $('#customer_id').val($(this).attr('partyId'));
        $('#companyList').hide();
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
    });
	
});