
$(document).ready(function() {
    $('#menu_cost').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var uncheckedCostCheckTable = $('#uncheckedCostCheck-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costCheckOrder/costConfirmOrderList",
        "aoColumns": [ 
            { "mDataProp": null, "sWidth":"20px",
                "fnRender": function(obj) {
                  return '<input type="checkbox" name="order_check_box" id="'+obj.aData.ID+'" class="checkedOrUnchecked" order_no="'+obj.aData.BUSINESS_TYPE+'">';
                }
            },
            {"mDataProp":"BUSINESS_TYPE", "sWidth":"80px"},            	
            {"mDataProp":"SPNAME", "sWidth":"200px"},
            {"mDataProp":null, "sWidth": "90px", 
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
            {"mDataProp":"RETURN_ORDER_COLLECTION", "sWidth":"90px"},  
		    {"mDataProp":null, "sWidth":"90px",
                "fnRender": function(obj) {
                    return "未收款";
            }},
            {"mDataProp":"ORDER_NO", "sWidth":"200px", 
                "fnRender": function(obj) {
                	var str = "";
                    if(obj.aData.ORDER_NO.indexOf("PS") > -1){
                        str = "<a href='/delivery/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else if(obj.aData.ORDER_NO.indexOf("PC") > -1){
                        str = "<a href='/pickupOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else if(obj.aData.ORDER_NO.indexOf("FC") > -1){
                        str = "<a href='/departOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else {
                        str = "<a href='/insuranceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }
                    return str;
                }
            },
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"200px"},
            {"mDataProp":"CREATE_STAMP", "sWidth":"140px"},                 	
            {"mDataProp":"AMOUNT", "sWidth":"40px"},                        
            {"mDataProp":"VOLUME", "sWidth":"50px"},                        
            {"mDataProp":"WEIGHT", "sWidth":"40px"},                        
            {"mDataProp":"PAY_AMOUNT", "sWidth":"60px"},                        
            {"mDataProp":"OFFICE_NAME", "sWidth":"90px"},                       
            {"mDataProp":"REMARK", "sWidth":"150px"}                         
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
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"200px"}                      
		]          
    });
    
    var ids = [];
    var orderNos = [];
    // 未选中列表
	$("#uncheckedCostCheck-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
			$(this).parent().parent().appendTo($("#checkedCostCheckList"));
			ids.push($(this).attr('id'));
			orderNos.push($(this).attr('order_no'));
			$("#checkedOrderId").val(ids);
			$("#checkedOrderNo").val(orderNos);
			if(ids.length>0){
				$("#saveBtn").attr("disabled",false);
			}
		}			
	});
	
	// 已选中列表
	$("#checkedCostCheck-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == false){
			$(this).parent().parent().appendTo($("#uncheckedCostCheckList"));
			if(ids.length != 0){
				ids.splice($.inArray($(this).attr('id'),ids),1);
				$("#checkedOrderId").val(ids);
				if(ids.length<=0){
					$("#saveBtn").attr("disabled",true);
				}
			}
			if(orderNos.length != 0){
				orderNos.splice($.inArray($(this).attr('order_no'),orderNos),1);
				$("#checkedOrderNo").val(orderNos);
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
	
	//获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter2').on('input', function(){
    		var me=this;
    		var inputStr = $('#sp_filter2').val();
    		if(inputStr == ""){
    			var pageSpName = $("#pageSpName");
    			pageSpName.empty();
    			var pageSpAddress = $("#pageSpAddress");
    			pageSpAddress.empty();
    			$('#sp_id2').val($(this).attr(''));
    		}
    		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
    			if(inputStr!=$('#sp_filter2').val()){//查询条件与当前输入值不相等，返回
					return;
				}
    			var spList =$("#spList2");
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
 			$("#spList2").css({ 
            	left:$(me).position().left+"px", 
            	top:$(me).position().top+32+"px" 
            }); 
            $('#spList2').show();
    		},'json');

    		
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
            $('#spList2').hide();
            
            refreshCreateList();
            
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
    var refreshCreateList = function() {
    	var orderNo = $("#orderNo_filter").val();
    	var sp = $("#sp_filter2").val();
    	var no = $("#no").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var type = $("#order_type_filter").val();
    	var status = $("#order_status_filter").val();
    	
    	uncheckedCostCheckTable.fnSettings().sAjaxSource = "/costCheckOrder/costConfirmOrderList?sp="+sp
														+"&beginTime="+beginTime
														+"&endTime="+endTime
														+"&orderNo="+orderNo
														+"&no="+no
														+"&type="+type
														+"&status="+status;
    	uncheckedCostCheckTable.fnDraw();
    	
    	
    };
    $("#orderNo_filter,#no,#beginTime_filter,#endTime_filter").on('keyup',function(){
    	refreshCreateList();
    });
    $("#order_type_filter,#order_status_filter").on('change',function(){
    	
    	refreshCreateList();
    });
} );