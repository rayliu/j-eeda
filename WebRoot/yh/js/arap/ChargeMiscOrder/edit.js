$(document).ready(function() {
	if(order_no){
		document.title = order_no+' | '+document.title;
	}
	$('#menu_finance').addClass('active').find('ul').addClass('in');
	
	var saveChargeMiscOrder = function(e,callback){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeMiscOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/chargeMiscOrder/save', $("#chargeMiscOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#miscChargeOrderNo").html('<strong>'+data.ORDER_NO+'</strong>');
				$("#create_stamp").html(data.CREATE_STAMP);
				$("#chargeMiscOrderId").val(data.ID);
				contactUrl("edit?id",data.ID);
				callback(data.ID);
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
	var parentId = "chargeMiscOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#chargeMiscOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveChargeMiscOrderBtn").click(function(e){
 		saveChargeMiscOrder(e);
 		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
 		
	});
	
	$("#chargeMiscOrderItem").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeMiscOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/chargeMiscOrder/save', $("#chargeMiscOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#chargeMiscOrderId").val(data.ID);
			  	//$("#style").show();
				
			  	$("#departureConfirmationBtn").attr("disabled", false);
			  	contactUrl("edit?id",data.ID);
			  	if("chargeMiscOrderbasic" == parentId){
			  		
			  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  		
			  	}
			}else{
				alert('数据保存失败。');
			}
		},'json');
		parentId = e.target.getAttribute("id");
	});
	
    if($("#chargeMiscOrderStatus").text() == 'new'){
    	$("#chargeMiscOrderStatus").text('新建');
	}

    var feeTable = $('#feeItemList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
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
        "sAjaxSource": "/chargeMiscOrder/chargeMiscOrderItemList?chargeMiscOrderId="+$("#chargeMiscOrderId").val(),
        "aoColumns": [   
            {"mDataProp":"CUSTOMER_ORDER_NO",
            	"fnRender": function(obj) {
		        if(obj.aData.CUSTOMER_ORDER_NO!='' && obj.aData.CUSTOMER_ORDER_NO != null){
		            return "<input type='text' name='customer_order_no' value='"+obj.aData.CUSTOMER_ORDER_NO+"' class='form-control search-control'>";
		        }else{
		        	 return "<input type='text' name='customer_order_no' class='form-control search-control'>";
		        }
		     }},
          	{"mDataProp":"ITEM_DESC",
			    "fnRender": function(obj) {
			        if(obj.aData.ITEM_DESC!='' && obj.aData.ITEM_DESC != null){
			            return "<input type='text' name='item_desc' value='"+obj.aData.ITEM_DESC+"' class='form-control search-control'>";
			        }else{
			        	 return "<input type='text'  name='item_desc' class='form-control search-control'>";
			        }
			}},
			{"mDataProp":"NAME",
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
			{"mDataProp":"AMOUNT",
			     "fnRender": function(obj) {
			    	 if(obj.aData.CREATE_NAME == 'system'){
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return obj.aData.AMOUNT;
				         }else{
				         	 return "";
				         }
			    	 }else{
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"' class='form-control search-control'>";
				         }else{
				         	 return "<input type='text' name='amount' class='form-control search-control'>";
				         }
			    	 }
			 }},
			{"mDataProp":"STATUS","sClass": "status"},
            {"mDataProp": null,"sWidth": "80px",
                "fnRender": function(obj) {
                        return    "<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'><i class='fa fa-trash-o fa-fw'> </i>删除明细</a>";
                }
            }   

			/* TODO 由于暂时未处理,所以先注释
			 * {
				"mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
              		"<i class='fa fa-trash-o fa-fw'> </i> "+
              		"删除"+
              		"</a>";
                }
			}*/                    
        ]      
    });
    
   /* $.post('/chargeMiscOrder/searchAllAccount',function(data){
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
*/
    $("input[name='paymentMethod']").each(function(){
		if($("#paymentMethodRadio").val() == $(this).val()){
			$(this).attr('checked', true);
			if($(this).val() == 'transfers'){	    		
	    		$("#accountTypeDiv").show();    		
	    	}
		}
	 }); 
    
    $("#paymentMethods").on('click', 'input', function(){
    	if($(this).val() == 'cash'){
    		$("#accountTypeDiv").hide();
    	}else{
    		$("#accountTypeDiv").show();    		
    	}
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
        $.get("/transferOrder/searchPartCustomer", {input:inputStr}, function(data){
            companyList.empty();
            for(var i = 0; i < data.length; i++){
                var abbr = data[i].ABBR;
				var company_name = data[i].COMPANY_NAME;
				if(abbr == null) 
					abbr = '';
				if(company_name == null)
					company_name = '';
				companyList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' company_name='"+company_name+"'>"+abbr+" "+company_name+"</a></li>");
            }
        },'json');
        companyList.css({left:$(this).position().left+"px",top:$(this).position().top+32+"px"}).show();
    });
    
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).attr("company_name"));
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customer_id').val(companyId);
        //savePartyInfo(companyId,"CUSTOMER");
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
    
    
	//应收
	$("#addFee").click(function(){
		var insertNewFee  = function(chargeMiscOrderId){
			$.post('/chargeMiscOrder/addNewFee?chargeMiscOrderId='+chargeMiscOrderId,function(data){
				console.log(data);
				if(data.ID > 0){
					feeTable.fnSettings().sAjaxSource = "/chargeMiscOrder/chargeMiscOrderItemList?chargeMiscOrderId="+chargeMiscOrderId;
					feeTable.fnDraw();  
				}
			});		
		};
		
		var chargeMiscOrderId =$("#chargeMiscOrderId").val();
		if(chargeMiscOrderId==null || chargeMiscOrderId==''){
			saveChargeMiscOrder(event, insertNewFee);
		}else{
		 	insertNewFee(chargeMiscOrderId);
		 }
		 
	});	
	
	//应收修改
	$("#feeItemList-table").on('blur', 'input,select', function(e){
		e.preventDefault();
		var chargeMiscOrderId = $("#chargeMiscOrderId").val();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		var chargeCheckOrderIds = $("#chargeCheckOrderIds").val();
		$.post('/chargeMiscOrder/updateChargeMiscOrderItem', {paymentId:paymentId, name:name, value:value, chargeMiscOrderId: chargeMiscOrderId, chargeCheckOrderIds: chargeCheckOrderIds}, function(data){
			if(data.ID > 0){
				$("#totalAmountSpan").html(data.TOTAL_AMOUNT);
			}else{
				alert("修改失败!");
			}
    	},'json');
	});
	
	$("#chargeMiscOrderItem").click(function(){
		feeTable.fnSettings().sAjaxSource = "/chargeMiscOrder/chargeMiscOrderItemList?chargeMiscOrderId="+$("#chargeMiscOrderId").val();
		feeTable.fnDraw();  
	});
	
	//异步删除应收
	$("#feeItemList-table").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		e.preventDefault();
		$.post('/chargeMiscOrder/finItemdel/'+id,function(data){
             //保存成功后，刷新列表
			 $("#totalAmountSpan").html(data.TOTAL_AMOUNT);
             feeTable.fnSettings().sAjaxSource = "/chargeMiscOrder/chargeMiscOrderItemList?chargeMiscOrderId="+$("#chargeMiscOrderId").val();
     		 feeTable.fnDraw(); 
        },'json');
	});	
	
	var typeRadio = $("#typeRadio").val();
	$("input[name='type']").each(function(){
		if(typeRadio == $(this).val()){
			$(this).prop('checked', true);
		}
	});
	
	var chargeCheckListTab = $('#chargeCheckList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeMiscOrder/chargeCheckList",
        "aoColumns": [   
            {"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
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
            {"mDataProp":"TOTAL_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":"DEBIT_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"CHARGE_AMOUNT"},
            {"mDataProp":"REMARK"},
            {"mDataProp":"CREATOR_NAME"},        	
            {"mDataProp":"CREATE_STAMP"}                        
        ]      
    });	

    $("#chargeCheckList").click(function(){
    	chargeCheckListTab.fnSettings().sAjaxSource = "/chargeMiscOrder/chargeCheckList?chargeCheckOrderIds="+$("#chargeCheckOrderIds").val();
    	chargeCheckListTab.fnDraw();  
    });
} );