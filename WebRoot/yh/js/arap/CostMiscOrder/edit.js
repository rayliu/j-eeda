$(document).ready(function() {
	if(order_no){
		document.title = order_no +' | '+document.title;
	}
	$('#menu_finance').addClass('active').find('ul').addClass('in');
	
	var saveCostMiscOrder = function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costMiscOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/costMiscOrder/save', $("#costMiscOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#costMiscOrderId").val(data.ID);
			}else{
				alert('数据保存失败。');
			}
		},'json');
	};
   
	/*--------------------------------------------------------------------*/
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	
	//设置一个变量值，用来保存当前的ID
	var parentId = "costMiscOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#costMiscOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveCostMiscOrderBtn").click(function(e){
 		
 		saveCostMiscOrder(e);

 		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	});
	
	$("#costMiscOrderItem").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costMiscOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/costMiscOrder/save', $("#costMiscOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#costMiscOrderId").val(data.ID);
			  	//$("#style").show();
			  	$("#departureConfirmationBtn").attr("disabled", false);
			  	if("costMiscOrderbasic" == parentId){
			  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  	}
			}else{
				alert('数据保存失败。');
			}
		},'json');
		parentId = e.target.getAttribute("id");
	});
	
    if($("#costMiscOrderStatus").text() == 'new'){
    	$("#costMiscOrderStatus").text('新建');
	}

    var feeTable = $('#feeItemList-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "sAjaxSource": "/costMiscOrder/costMiscOrderItemList",
        "aoColumns": [   
          	/*{"mDataProp":"COST_ORDER_NO","sWidth": "80px"},*/
          	{"mDataProp":null,"sWidth": "100px",
			    "fnRender": function(obj) {
			        if(obj.aData.ORDER_TYPE!='' && obj.aData.ORDER_TYPE != null){
			        	var str="";
			        	$("#orderTypeList").children().each(function(){
			        		if(obj.aData.ORDER_TYPE == $(this).text()){
			        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";   
			        		}else{
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			            return "<select name='order_type' class='form-control search-control'>"+str+"</select>";
			        }else{
			        	var str="";
			        	$("#orderTypeList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	return "<select name='order_type' class='form-control search-control'>"+str+"</select>";
			        }
			}},
          	{"mDataProp": null,
           	 "fnRender": function(obj) {
		       		if(obj.aData.ORDER_STAMP!='' && obj.aData.ORDER_STAMP != null)
		           		return "<div class='input-append date'><input type='text' class='form-control search-control orderNo_filter' name='order_stamp' value='"+obj.aData.ORDER_STAMP+"'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar' onClick='datetimepicker(this)'> </i></span></div>";
		       		else
		       			return "<div class='input-append date'><input type='text' class='form-control search-control orderNo_filter' name='order_stamp'><span class='add-on'><i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar' onClick='datetimepicker(this)'> </i></span></div>";
                }
            },
			{"mDataProp": null,
           	 "fnRender": function(obj) {
	       		 	if(obj.aData.ORDER_NO!='' && obj.aData.ORDER_NO != null){
			            return "<input type='text' name='order_no' value="+obj.aData.ORDER_NO+" class='form-control search-control order_no'>";
			        }else{
			        	return "<input type='text' name='order_no' class='form-control search-control order_no'>";
			        }
                }
            },
			{"mDataProp":null,"sWidth": "70px",
			    "fnRender": function(obj) {
			        if(obj.aData.NAME!='' && obj.aData.NAME != null){
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		if(obj.aData.NAME == $(this).text()){
			        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";   
			        			$("#receivableTotal").val(obj.aData.NAME);
			        		}else{
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			            return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
			        }else{
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
			        }
			 }},
			 {"mDataProp":null,
			    "fnRender": function(obj) {
			        if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
			            return "<input type='text' name='amount' value="+obj.aData.AMOUNT+" class='form-control search-control'>";
			        }else{
			        	return "<input type='text' name='amount' class='form-control search-control'>";
			        }
			}},
			{"mDataProp":null,
			    "fnRender": function(obj) {
			        if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
			            return "<input type='text' name='remark' value="+obj.aData.REMARK+" class='form-control search-control'>";
			        }else{
			        	return "<input type='text' name='remark' class='form-control search-control'>";
			        }
			}},
			{"mDataProp": null,
                "fnRender": function(obj) {
               		 return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'><i class='fa fa-trash-o fa-fw'> </i>删除明细</a>";
                }
            }   
        ]      
    });
    
    //查询银行账号
    $.post('/costMiscOrder/searchAllAccount',function(data){
		 if(data.length > 0){
			 var accountTypeSelect = $("#accountTypeSelect");
			 accountTypeSelect.empty();
			 var hideAccountId = $("#hideAccountId").val();
			 accountTypeSelect.append("<option ></option>");
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideAccountId){
					 accountTypeSelect.append("<option value='"+data[i].ID+"' selected='selected'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");
				 }else{
					 accountTypeSelect.append("<option value='"+data[i].ID+"'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");					 
				 }
			}
		}
	},'json');
    
    $("input[name='paymentMethod']").each(function(){
		if($("#paymentMethodRadio").val() == $(this).val()){
			$(this).attr('checked', true);
			if($(this).val() == 'transfers'){	    		
	    		$("#accountTypeDiv").show();    		
	    	}
		}
	 }); 
    
    //付款方式
    $("#paymentMethods").on('click', 'input', function(){
    	if($(this).val() == 'cash'){
    		$("#accountTypeDiv").hide();
    	}else{
    		$("#accountTypeDiv").show();    		
    	}
    }); 
	
	//应收
	$("#addFee").click(function(){	
		 var costMiscOrderId =$("#costMiscOrderId").val();
		 $.post('/costMiscOrder/addNewFee?costMiscOrderId='+costMiscOrderId,function(data){
			console.log(data);
			if(data.ID > 0){
				feeTable.fnSettings().sAjaxSource = "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+costMiscOrderId;
				feeTable.fnDraw();  
			}
		});		
	});	
	
	//保存修改费用明细的方法
	var savaUpdataMethod = function(evt){
		var inputThis = $(evt);
		var costMiscOrderId = $("#costMiscOrderId").val();
		var paymentId = inputThis.parent().parent().attr("id");
		if(paymentId == "" || paymentId == null)
			paymentId = inputThis.parent().parent().parent().attr("id");
		var name = inputThis.attr("name");
		var value = inputThis.val();
		var costCheckOrderIds = $("#costCheckOrderIds").val();
		if(paymentId != "" && value != "" && costMiscOrderId != "")
		$.post('/costMiscOrder/updateCostMiscOrderItem', {paymentId:paymentId, name:name, value:value, costMiscOrderId: costMiscOrderId, costCheckOrderIds: costCheckOrderIds}, function(data){
			if(data.ID > 0){
				$("#totalAmountSpan")[0].innerHTML = data.TOTAL_AMOUNT;
			}else{
				alert("修改失败!");
			}
    	},'json');
	};
	
	//费用明细列表修改（成本单据类型），当单据改变值时，单据号值为空
	$("#feeItemList-table").on('change', 'select[name="order_type"]', function(e){
		savaUpdataMethod(this);
		$(this).parent().parent().find("td").find("input[name='order_no']").val("");
	});
	
	//费用明细列表修改(单据号，费用类型，金额，备注，日期)
	$("#feeItemList-table").on('blur', 'input[name="order_stamp"],input[name="order_no"],input[name="amount"],input[name="remark"],select[name="fin_item_id"]', function(e){
		savaUpdataMethod(this);
	});
	
	//点击文本框显示可选单号列表
	$('#feeItemList-table').on('keyup click', 'input[name="order_no"]', function(){
		var name = $(this);
		var inputStr = $(this).val();
		var orderType = $(this).parent().parent().find("td").find("select[name='order_type']").val();
		var orderStamp = $(this).parent().parent().find("td").find("div").find("input[name='order_stamp']").val();
		console.log("orderType:"+orderType+",orderStamp:"+orderStamp);
		$(this).next().remove();
		if(orderType != "otherOrder"){
			$.get("/costMiscOrder/findOrderNoByOrderType", {input:inputStr,orderType:orderType,orderStamp:orderStamp}, function(data){
				var str = "";
	            for(var i = 0; i < data.length; i++)
	            	str += "<li><a tabindex='-1' class='fromLocationItem' order_no='"+data[i].ORDER_NO+"' orderid='"+data[i].ID+"'>"+data[i].ORDER_NO+"</a></li>";
	            name.after('<ul class="pull-right dropdown-menu default dropdown-scroll driverAssistantList" tabindex="-1">'+str+'</ul>');
	            name.next().css({left:name.position().left+"px", top:name.position().top+32+"px"}).show();
	        },'json');
		}
	});
	
	// 选中列表单号
	$('#feeItemList-table').on('mousedown', '.fromLocationItem', function(e){	
		$(this).parent().parent().parent().parent().attr("orderid",$(this).attr('orderid'));
		$(this).parent().parent().parent().find("input[name='order_no']").val($(this).attr('order_no'));
	    $(this).parent().parent().hide();   
    });
	
    // 没选中单号，焦点离开，隐藏列表
	$('#feeItemList-table').on('blur', '.order_no', function(e){	
    	$(this).next().eq(0).css({left:$(this).position().left+"px", top:$(this).position().top+32+"px"}).hide();
    });

    //当用户只点击了滚动条，没选单号，再点击页面别的地方时，隐藏列表
    $('#feeItemList-table').on('blur','.order_no', function(){
        $('#companyList').hide();
    });
	
	
	$("#costMiscOrderItem").click(function(){
		feeTable.fnSettings().sAjaxSource = "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+$("#costMiscOrderId").val();
		feeTable.fnDraw();  
	});
	
	//异步删除应付
	$("#feeItemList-table").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		e.preventDefault();
		$.post('/costMiscOrder/finItemdel/'+id,function(data){
             //保存成功后，刷新列表
             console.log(data);
             feeTable.fnSettings().sAjaxSource = "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+$("#costMiscOrderId").val();
     		 feeTable.fnDraw();  
        },'text');
	});	
	
	var typeRadio = $("#typeRadio").val();
	$("input[name='type']").each(function(){
		if(typeRadio == $(this).val()){
			$(this).prop('checked', true);
		}
	});
	
	var costCheckListTab = $('#costCheckList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costMiscOrder/costCheckorderListById",
        "aoColumns": [   
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/costCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
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
            {"mDataProp":"CNAME"},
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
            {"mDataProp":null},        	
            {"mDataProp":"CREATE_STAMP"}                       
        ]      
    });	
	
	//tab-对账单明细
    $("#costCheckList").click(function(){
    	costCheckListTab.fnSettings().sAjaxSource = "/costMiscOrder/costCheckorderListById?costCheckOrderIds="+$("#costCheckOrderIds").val();
    	costCheckListTab.fnDraw();  
    });
    
    //保存费用明细客户与供应商的方法
    var savePartyInfo = function(partyId,partyType){
		var costMiscId = $("#costMiscOrderId").val();
		if(costMiscId != ""){
			$.post('/costMiscOrder/saveMiscPartyInfo',{miscId:costMiscId,partyId:partyId,partyType:partyType},function(data){
				if(!data.success){
					alert("保存出错");
				}
			});	
		}
	};
    
    //获取客户列表，自动填充
    $('#customer_filter').on('keyup click', function(){
        var inputStr = $('#customer_filter').val();
        var companyList =$("#companyList");
        $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
            companyList.empty();
            for(var i = 0; i < data.length; i++)
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
        },'json');
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
        savePartyInfo(companyId,"CUSTOMER");
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
    $('#sp_filter').on('keyup click', function(){
		var inputStr = $('#sp_filter').val();
		var spList =$("#spList");
		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
			spList.empty();
			for(var i = 0; i < data.length; i++){
				var abbr = data[i].ABBR;
				var company_name = data[i].COMPANY_NAME;
				
				if(abbr == null) 
					abbr = '';
				if(company_name == null)
					company_name = '';
				
				spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' abbr='"+abbr+"' >"+abbr+" "+company_name+"</a></li>");
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
		$('#sp_filter').val($(this).attr('abbr'));
		var provider = $(this).attr('partyId');
		$('#sp_id').val(provider);
        $('#spList').hide();
        savePartyInfo(provider,"SERVICE_PROVIDER");
    });
    
});
function datetimepicker(data){
	if(!$("#saveCarSummaryBtn").prop("disabled")){
		$('.input-append').datetimepicker({  
			    format: 'yyyy-MM-dd',  
			    language: 'zh-CN',
			    autoclose: true,
			    pickerPosition: "bottom-left"
			}).on('changeDate', function(ev){
				$(".bootstrap-datetimepicker-widget").hide();
				$(data).parent().prev("input").focus();
		});
	}
}
