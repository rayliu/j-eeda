
$(document).ready(function() {
    $('#menu_cost').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var uncheckedCostCheckTable = $('#uncheckedCostCheck-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costPreInvoiceOrder/costCheckOrderList",
        "aoColumns": [   
            { "mDataProp": null, "sWidth":"20px",
                "fnRender": function(obj) {
                  return '<input type="checkbox" name="order_check_box" id="'+obj.aData.ID+'" class="checkedOrUnchecked" order_no="'+obj.aData.ORDER_NO+'">';
                }
            },
            {"mDataProp":null,
            	"fnRender":function(obj){
            		return "<a href='/costCheckOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
            	}},
            {"mDataProp":"STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"CNAME", "sWidth":"150px"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"TOTAL_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":"DEBIT_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"COST_AMOUNT"},
            {"mDataProp":"REMARK"},
            {"mDataProp":"CREATOR_NAME"},        	
            {"mDataProp":"CREATE_STAMP"}                        
        ]      
    });		

    var checkedCostCheckTable = $('#checkedCostCheck-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/chargeCheckOrder/createList2",
    	"aoColumns": [ 
			  {"mDataProp":null, "sWidth":"20px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"}                      
		]          
    });
    
    var ids = [];
    // 未选中列表
	$("#uncheckedCostCheck-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
			$(this).parent().parent().appendTo($("#checkedCostCheckList"));
			ids.push($(this).attr('id'));
			$("#costCheckedOrderIds").val(ids);
		}			
	});
	
	// 已选中列表
	$("#checkedCostCheck-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == false){
			$(this).parent().parent().appendTo($("#uncheckedCostCheckList"));
			if(ids.length != 0){
				ids.splice($.inArray($(this).attr('id'),ids),1);
				$("#costCheckedOrderIds").val(ids);
			}
		}			
	});
	
	$('#saveBtn').click(function(e){
        e.preventDefault();
        $('#createForm').submit();
    });
	
	$("#checkedCostCheckOrder").click(function(){
		$("#checked").show();
	});
    
    //获取客户的list，选中信息自动填写其他信息
    $('#companyName').on('keyup', function(){
        var inputStr = $('#companyName').val();
        
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
           
            var companyList =$("#companyList");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+"</a></li>");
            }
            if(data.length>0)
                companyList.show();
        },'json');
        $("#companyList").css({ 
           	left:$(this).position().left+"px", 
           	top:$(this).position().top+32+"px" 
        }); 
        
    });

    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#companyName').val($(this).text());
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId);
		refreshCreateList();
        
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

    $('#createBtn').click(function(e){
        e.preventDefault();
        //获取选取回单的ID, 放到数组中
        var chk_value =[];    
        $('input[name="order_check_box"]:checked').each(function(){    
           chk_value.push($(this).val());    
        }); 

        var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable">'+
                        '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
                        'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
                    '</div>';
        $('body').append(alerMsg);
        $('#message_trigger_err').on('click', function(e) {
            e.preventDefault();
            
        });
        //$(body).append(chk_value.length==0 ?'你还没有选择任何应付单据！':chk_value);

        $('#ids').val(chk_value);
        if(!$('#returnOrderSearchForm').valid()){
            return false;
        }
        if(chk_value.length==0 ){
            $.scojs_message('你还没有勾选任何应收回单.', $.scojs_message.TYPE_ERROR);
            return false;
        }

        $('#createForm').submit();
    });

    $('input.beginTime_filter').on( 'change input', function () {
      
    } );
    
   var refreshCreateList = function(){
	   var sp = $("#sp_filter2").val();
       var customer = $("#customer_filter").val();
       var orderNo = $("#orderNo_filter").val();
       var beginTime = $("#beginTime_filter").val();
       var endTime = $("#endTime_filter").val();
        uncheckedCostCheckTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderList?orderNo="+orderNo       
												        +"&customer="+customer
												        +"&sp="+sp
												        +"&beginTime="+beginTime
												        +"&endTime="+endTime;
        uncheckedCostCheckTable.fnDraw();
   };
	$("#sp_filter2,#customer_filter,#orderNo_filter,#beginTime_filter,#endTime_filter").on('keyup',function(){
		refreshCreateList();
	});
    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
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
    $('#sp_filter2').on('keyup click', function(){
 		var inputStr = $('#sp_filter2').val();
 		if(inputStr == ""){
 		var pageSpName = $("#pageSpName");
 		pageSpName.empty();
 		var pageSpAddress = $("#pageSpAddress");
 		pageSpAddress.empty();
 		$('#sp_id2').val($(this).attr(''));
 		}
 		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
 			//console.log(data);
 			var spList =$("#spList2");
 			spList.empty();
 			for(var i = 0; i < data.length; i++){
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
 		},'json');
 		$("#spList2").css({ 
 			left:$(this).position().left+"px", 
 			top:$(this).position().top+32+"px" 
 		}); 
 	    $('#spList2').show();
 	});

    	// 没选中供应商，焦点离开，隐藏列表
    	$('#sp_filter2').on('blur', function(){
     		$('#spList2').hide();
     	});

    	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
    	$('#spList2').on('blur', function(){
     		$('#spList2').hide();
     	});

    	$('#spList2').on('mousedown', function(){
    		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    	});

    	// 选中供应商
    	$('#spList2').on('mousedown', '.fromLocationItem', function(e){
    		console.log($('#spList').is(":focus"))
    		var message = $(this).text();
    		$('#sp_filter2').val(message.substring(0, message.indexOf(" ")));
    		$('#spList2').val($(this).attr('partyId'));
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
         $('#spList2').hide();
 		refreshCreateList();
    });
 
} );