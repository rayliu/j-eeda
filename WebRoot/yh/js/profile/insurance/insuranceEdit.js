
$(document).ready(function(){
	$('#menu_profile').addClass('active').find('ul').addClass('in');
	
	var insuranceId = $("#insuranceId").val();
	if(insuranceId == "" || insuranceId == null){
		$("#assRateBtn").prop("disabled",true);
	}else{
		$("#assRateBtn").prop("disabled",false);
	}
	
	var dataTable = $('#dataTables-example').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "bFilter": false,
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "/insurance/findAllInsuranceItem?insuranceId="+insuranceId,
        "aoColumns": [  
			{"mDataProp":"ABBR"},   
            {"mDataProp":"INSURANCE_RATE"},
            {"mDataProp":"BEGINTIME"},
            {"mDataProp":"ENDTIME"},
            {"mDataProp":"REMARK"},
            {"mDataProp": null, 
	            "sWidth": "11%",
	            "fnRender": function(obj) { 
	            	if(obj.aData.IS_STOP != true){
	            		return "<nobr><a class='btn  btn-primary btn-sm rateEdit' code='"+obj.aData.ID+"'><i class='fa fa-edit'></i>编辑</a> "+
		                        "<a class='btn btn-danger  btn-sm rateDel' code='"+obj.aData.ID+"'><i class='fa fa-trash-o fa-fw'></i>停用</a>";
	            	}else{
	            		return "<nobr><a class='btn  btn-primary btn-sm rateEdit' code='"+obj.aData.ID+"'><i class='fa fa-edit'></i>编辑</a> "+
		                        "<a class='btn btn-success  btn-sm rateDel' code='"+obj.aData.ID+"' ><i class='fa fa-trash-o fa-fw'></i>启用</a>";
	            	}
	            }
            }                      
        ]
     });
	
	//保险公司表单验证
	var validate1 = $('#insuranceForm').validate({
        rules: {
        	insuranceName: {
        		required: true
            },
            company_name: {
            	required: true
          	},
          	abbr: {
          		required: true
          	}
        }
    });
	
	//保存保险单
    $('#saveBtn').click(function(e){
    	//提交前，校验数据
        if(!$("#insuranceForm").valid()){
	       	return false;
        }
        $.post('/insurance/saveInsurance', $("#insuranceForm").serialize(), function(order){
        	if(order.ID){
	    		$("#insuranceId").val(order.ID);
	    		$("#rateInsuranceId").val(order.ID);
	    		$("#assRateBtn").prop("disabled",false);
	    		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    	}else{
	    		$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    	}
    	});
    });
    
    //保存保险单
    $('#assRateBtn').click(function(e){
    	$("#customer_id").val("");
    	$("#customer_name").val("");
    	$("#insurance_rate").val("");
    	$("#remark1").val("");
    	$("#beginTime").val("");
    	$("#endTime").val("");
    	$("#rateItemId").val("");
    });
	
	//保险费率表单验证
	var validate2 = $('#rateFrom').validate({
        rules: {
        	customer_id: {
        		required: true
            },
            customer_name: {
        		required: true
            },
            insurance_rate: {
            	required: true,
            	number:true
          	},
          	beginTime: {
          		required: true
          	},
          	endTime: {
          		required: true
          	}
        }
    });
	
    //保存费率
    $('#saveRateBtn').click(function(e){
    	//提交前，校验数据
        if(!$("#rateFrom").valid()){
	       	return false;
        }
        $.post('/insurance/saveInsuranceItem', $("#rateFrom").serialize(), function(order){
        	var id = $("#insuranceId").val();
        	dataTable.fnSettings().sAjaxSource = "/insurance/findAllInsuranceItem?insuranceId="+id;
        	dataTable.fnDraw(); 
            $('#insuranceRateItem').modal('hide');
    	});
        
    });
    
    //费率编辑
	$("#dataTables-example").on('click', '.rateEdit', function(){
		$("#customer_id").val("");
    	$("#customer_name").val("");
    	$("#insurance_rate").val("");
    	$("#remark1").val("");
    	$("#beginTime").val("");
    	$("#endTime").val("");  
		var id = $(this).attr('code');
		
		$.post('/insurance/rateEdit/'+id,null,function(data){
			//保存成功后，刷新列表
            if(data !=null){
	           	$('#insuranceRateItem').modal('show');
	           //	$('#rateInsuranceId').val(insuranceId);
	           	$('#rateItemId').val(data.ID);
	           	$('#customer_id').val(data.CUSTOMER_ID);
	        	$('#customer_name').val(data.COMPANY_NAME);
	           	$("#insurance_rate").val(data.INSURANCE_RATE);
	           	$('#remark1').val(data.REMARK);
	           	$('#beginTime').val(data.BEGINTIME);
	           	$('#endTime').val(data.ENDTIME);
	           	
            }else{
                alert('编辑失败');
            }
        },'json');
	});
    
	//费率删除
	$("#dataTables-example").on('click', '.rateDel', function(){
		var id = $(this).attr('code');
		$.post('/insurance/rateDel/'+id,null,function(data){
			dataTable.fnDraw(); 
        },'json');
	});
    
    //获取客户的list，选中信息在下方展示其他信息
	$('#customer_name').on('keyup click', function(){
		if($('#customer_name').val() == "")
			$("#customer_id").val("");
		$.get('/customerContract/search', {locationName:$('#customer_name').val()}, function(data){
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
	$('#customer_name').on('blur', function(){
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
		$('#customer_name').val(message.substring(0, message.indexOf(" ")));
		$("#customer_id").val($(this).attr('partyId'));
		$('#customerList').hide();
    }); 
	
	$('#datetimepicker1').datetimepicker({  
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