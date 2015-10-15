
$(document).ready(function() {
    $('#menu_cost').addClass('active').find('ul').addClass('in');
    $("input[name='allCheck']").click(function(){
    	$("#uncheckedCostCheck-table input[name='order_check_box']").each(function () { 
    		this.checked = !this.checked;
    		if($(this).prop("checked") == true){
    			$(this).parent().parent().clone().appendTo($("#checkedCostCheckList"));
    			ids.push($(this).attr("id"));
    			orderNos.push($(this).attr("order_no"));
    			change_amount.push($(this).attr("change_amount"));
    			$("#checkedCostCheckList").children().length++;
    			sum =eval(change_amount.join("+"));//求和
    			$("#checkedOrderId").val(ids);
    			$("#checkedOrderNo").val(orderNos);
    			$("#amount").html(sum);
    			if(ids.length>0){
    				$("#saveBtn").attr("disabled",false);
    			}
    		}
    		else{
    			ids.splice($.inArray($(this).attr('id'),ids),1);
    			change_amount.splice($.inArray($(this).attr('change_amount'),change_amount),1);
    			orderNos.splice($.inArray($(this).attr('order_no'),orderNos),1);
    			//$("#checkedCostCheckList").remove("tbody",$(this).parent().parent());
    			//$("#checkedCostCheckList").$(this).parent().parent().remove();
    			var id=$(this)[0].id;
    			var rows = $("#checkedCostCheckList").children();
    			for(var i=0; i<rows.length;i++){
    				var row = rows[i];
    				if(id==$(row).find("input").attr("id")){
    					row.remove();
    					//$("#checkedCostCheckList").children().splice(i,1);
    				}
    			}
    			var sum_f = $(this).attr("change_amount");
    			var x_sum=parseInt(sum)-parseInt(sum_f);
    			sum=parseInt(sum)-parseInt(sum_f);
    			$("#checkedOrderId").val(ids);
    			$("#checkedOrderNo").val(orderNos);
    			$("#amount").html(x_sum);
    			if(ids.length<=0){
    				$("#saveBtn").attr("disabled",true);
    			}
    		}    
  
         });

    });
	//datatable, 动态处理
    var uncheckedCostCheckTable = $('#uncheckedCostCheck-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": true, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 9999999], [10, 25, 50, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.DID);
			$(nRow).attr('ids', aData.ID);
			$(nRow).attr('order_ty', aData.BUSINESS_TYPE);
			return nRow;
		},
        "sAjaxSource": "/costCheckOrder/unSelectedList",
        "aoColumns": [ 
            { "mDataProp": null, "sWidth":"20px", "bSortable": false,
                "fnRender": function(obj) {
                	//alert(obj.aData.CHANGE_AMOUNT.find("change_amount"));
                	var amount = 0;
	               	 if($(obj.aData.CHANGE_AMOUNT).val()!=null){
	               		 amount = $(obj.aData.CHANGE_AMOUNT).val();
	               	 }else{
	               		amount = obj.aData.PAY_AMOUNT;
	               	 }
	               	 
                	var strcheck='<input  type="checkbox" name="order_check_box" change_amount="'+amount+'" id="'+obj.aData.ID+'" class="checkedOrUnchecked" order_no="'+obj.aData.BUSINESS_TYPE+'">';;
                	//判断obj.aData.ID 是否存在 list id 
                	console.log(ids);
                	 for(var i=0;i<ids.length;i++){
                		 console.log(i + ":"+ids[i]);
                		 console.log("obj.aData.ID="+obj.aData.ID);
                         if(ids[i]==obj.aData.ID){                        	 
                        	 return strcheck= '<input   checked="checked" type="checkbox" name="order_check_box" change_amount="'+amount+'" id="'+obj.aData.ID+'" class="checkedOrUnchecked" order_no="'+obj.aData.BUSINESS_TYPE+'">'; 
                        	
                         }
                     }
                	 return strcheck;
                }
            },
            {"mDataProp":"BUSINESS_TYPE", "sWidth":"80px"},            	
            {"mDataProp":"BOOKING_NOTE_NUMBER", "sWidth":"200px"},
            {"mDataProp":"SERIAL_NO", "sWidth":"200px"},
            {"mDataProp":"REF_NO", "sWidth":"200px"},
            {"mDataProp":"TO_NAME", "sWidth":"150px"},
            {"mDataProp":"PLANNING_TIME", "sWidth":"180px"},
            {"mDataProp":"AMOUNT", "sWidth":"40px"},
            {"mDataProp":"PAY_AMOUNT", "sWidth":"60px"},
            {"mDataProp":"CHANGE_AMOUNT","sWidth":"60px",
            	"fnRender": function(obj) {
                    if(obj.aData.CHANGE_AMOUNT!=''&& obj.aData.CHANGE_AMOUNT != null){
                        return "<input type='text' style='width:60px' name='change_amount' id='change' value='"+obj.aData.CHANGE_AMOUNT+"'/>";
                        
                    }
                    else {
                    	if(obj.aData.PAY_AMOUNT!=null){
                        return "<input type='text' style='width:60px' name='change_amount' value='"+obj.aData.PAY_AMOUNT+"'/>";
                    	}
                    	else{
                    		return "<input type='text' style='width:60px' name='change_amount' value='0'/>";
                    	}
                    }
                }
            },
            {"mDataProp":"OFFICE_NAME", "sWidth":"90px"}, 
            {"mDataProp":"SPNAME", "sWidth":"200px"},
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"200px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"200px"},
            {"mDataProp":"ORDER_NO", "sWidth":"200px", 
                "fnRender": function(obj) {
                	var str = "";
                    if(obj.aData.ORDER_NO.indexOf("PS") > -1){
                        str = "<a href='/delivery/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else if(obj.aData.ORDER_NO.indexOf("PC") > -1||obj.aData.ORDER_NO.indexOf("DC") > -1){
                        str = "<a href='/pickupOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else if(obj.aData.ORDER_NO.indexOf("FC") > -1){
                        str = "<a href='/departOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else {
                        str = "<a href='/insuranceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }
                    return str;
                }
            },
            {"mDataProp":"RECEIVINGUNIT", "sWidth":"130px"},
            {"mDataProp":"CREATE_STAMP", "sWidth":"180px"}, 
            {"mDataProp":"RETURN_ORDER_COLLECTION", "sWidth":"90px"},  
		    {"mDataProp":null, "sWidth":"90px",
                "fnRender": function(obj) {
                    return "未收款";
            }},
            {"mDataProp":null, "sWidth": "160px", 
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
            {"mDataProp":"FROM_NAME", "sWidth":"150px"},   
            {"mDataProp":"VOLUME", "sWidth":"50px"},                        
            {"mDataProp":"WEIGHT", "sWidth":"40px"},                                           
            {"mDataProp":"REMARK", "sWidth":"150px"}                         
        ]     
    });		

    var checkedCostCheckTable = $('#checkedCostCheck-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	//"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	//"sAjaxSource": "/chargeCheckOrder/createList2",
    	"aoColumns": [ 
			  {"mDataProp":null, "sWidth":"20px"},                        
			  {"mDataProp":null, "sWidth":"80px"},                        
			  {"mDataProp":null, "sWidth":"200px"},                        
			  {"mDataProp":null, "sWidth":"200px"},                        
			  {"mDataProp":null, "sWidth":"200px"},                        
			  {"mDataProp":null, "sWidth":"40px"},                        
			  {"mDataProp":null, "sWidth":"60px"},                        
			  {"mDataProp":null, "sWidth":"90px"},                        
			  {"mDataProp":null, "sWidth":"200px"},                        
			  {"mDataProp":null, "sWidth":"180px"},                        
			  {"mDataProp":null, "sWidth":"180px"},                        
			  {"mDataProp":null, "sWidth":"90px"},                        
			  {"mDataProp":null, "sWidth":"90px"},                        
			  {"mDataProp":null, "sWidth":"160px"},
			  {"mDataProp":null, "sWidth":"150px"},
			  {"mDataProp":null, "sWidth":"150px"},
			  {"mDataProp":null, "sWidth":"50px"},
			  {"mDataProp":null, "sWidth":"40px"},
			  {"mDataProp":null, "sWidth":"150px"}                      
		] 
    	
    });
    
    var ids = [];
    var orderNos = [];
    var change_amount = [];
    //var j_amount = [];
    var sum = 0;
    var j_sum = 0;
    // 未选中列表
	$("#uncheckedCostCheck-table").on('click', '.checkedOrUnchecked', function(e){	
		if($(this).prop("checked") == true){
			$(this).parent().parent().clone().appendTo($("#checkedCostCheckList"));
			ids.push($(this).attr('id'));
			orderNos.push($(this).attr('order_no'));
			change_amount.push($(this).attr('change_amount'));
			sum =eval(change_amount.join("+"));//求和
			$("#checkedOrderId").val(ids);
			$("#checkedOrderNo").val(orderNos);
			$("#amount").html(sum);
			if(ids.length>0){
				$("#saveBtn").attr("disabled",false);
			}
		}
		else{
			ids.splice($.inArray($(this).attr('id'),ids),1);
			change_amount.splice($.inArray($(this).attr('change_amount'),change_amount),1);
			orderNos.splice($.inArray($(this).attr('order_no'),orderNos),1);
			//$("#checkedCostCheckList").remove("tbody",$(this).parent().parent());
			//$("#checkedCostCheckList").$(this).parent().parent().remove();
			var id=$(this)[0].id;
			var rows = $("#checkedCostCheckList").children();
			for(var i=0; i<rows.length;i++){
				var row = rows[i];
				if(id==$(row).find('input').attr('id')){
					row.remove();
					//$("#checkedCostCheckList").children().splice(i,1);
				}
			}
			var sum_f = $(this).attr('change_amount');
			var x_sum=parseFloat(sum)-parseFloat(sum_f);
			sum=parseFloat(sum)-parseFloat(sum_f);
			$("#checkedOrderId").val(ids);
			$("#checkedOrderNo").val(orderNos);
			$("#amount").html(x_sum);
			if(ids.length<=0){
				$("#saveBtn").attr("disabled",true);
			}
		}
	});
	// 已选中列表
	$("#checkedCostCheck-table").on('click', '.checkedOrUnchecked', function(e){
        $(this).parent().parent().appendTo($("#uncheckedCostCheckList"));
		if($(this).prop("checked") == false){
			//j_amount.push($(this).attr('amount'));
			j_sum =$(this).attr('change_amount');//eval(j_amount.join("+"));
			var xj_sum=parseFloat(sum)-parseFloat(j_sum);
			sum=parseFloat(sum)-parseFloat(j_sum);
			//amount=parseFloat(a);
			$("#amount").html(xj_sum);
			//j_amount.splice($.inArray($(this).attr('id'),j_amount),1);
			if(ids.length != 0){
				ids.splice($.inArray($(this).attr('id'),ids),1);
				change_amount.splice($.inArray($(this).attr('change_amount'),change_amount),1);
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
	$("#uncheckedCostCheck-table").on('blur', 'input:text', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var departId = $(this).parent().parent().attr("ids");
		var ty = $(this).parent().parent().attr("order_ty");
		var name = $(this).attr("name");
		var value = $(this).val();
		 if(isNaN(value)){      
			 alert("调整金额为数字类型");
		 }else{
			 $.post('/costCheckOrder/updateDepartOrderFinItem', {ty:ty,departId:departId,paymentId:paymentId, name:name, value:value}, function(data){
				 $.scojs_message('调整金额成功', $.scojs_message.TYPE_OK);
				 $("#debitAmount").html(data.changeAmount);
				 $("#costAmount").html(data.actualAmount); 
				 $("#total_amount").val(data.changeAmount);
		    	},'json');
			 uncheckedCostCheckTable.fnDraw();
		 }
	});
	$("#checkedCostCheckOrder").click(function(){
		$("#checked").show();
	});
	$("#uncheckedCostCheckOrder").click(function(){
		uncheckedCostCheckTable.fnDraw();
	});
	
	$("#uncheckedCostCheck-table_wrapper").on('blur', 'input', function(e){
		e.preventDefault();
		var orderNos = $("#orderNos").val();
		var ids=$("#orderIds").val();
		var paymentId = $(this).parent().parent().attr("id");
		var departId = $(this).parent().parent().attr("ids");
		var ty = $(this).parent().parent().attr("order_ty");
		var name = $(this).attr("name");
		var value = $(this).val();
		 if(isNaN(value)){      
			 alert("调整金额为数字类型");
		 }else{
			 /*$.post('/costCheckOrder/updateDepartOrderFinItem', {orderNos:orderNos,ty:ty,departId:departId,paymentId:paymentId,ids:ids, name:name, value:value}, function(data){
				 $.scojs_message('调整金额成功', $.scojs_message.TYPE_OK);
				 $("#debitAmount").html(data.changeAmount);
				 $("# costAmount").html(data.actualAmount); 
				 $("#total_amount").val(data.changeAmount);
		    	},'json');*/
		 }
	}); 
	//获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter2').on('input click', function(){
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
    	var booking_id = $("#booking_id").val();
    	var orderNo = $("#orderNo_filter").val();
    	var sp = $("#sp_filter2").val();
    	var no = $("#no").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var type = $("#order_type_filter").val();
    	var status = $("#order_status_filter").val();
    	var serial_no = $("#serial_no").val();
    	uncheckedCostCheckTable.fnSettings().sAjaxSource = "/costCheckOrder/unSelectedList?sp="+sp
														+"&beginTime="+beginTime
														+"&endTime="+endTime
														+"&orderNo="+orderNo
														+"&no="+no
														+"&type="+type
														+"&status="+status
    	                                                +"&booking_id="+booking_id
    													+"&serial_no="+serial_no;
    	uncheckedCostCheckTable.fnDraw();
    	
    	
    };
    $("#orderNo_filter,#booking_id,#no,#beginTime_filter,#endTime_filter,#serial_no").on('keyup',function(){
    	refreshCreateList();
    });
    $("#order_type_filter,#order_status_filter").on('change',function(){
    	
    	refreshCreateList();
    });
} );