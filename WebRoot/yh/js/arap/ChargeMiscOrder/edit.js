$(document).ready(function() {
	$('#menu_finance').addClass('active').find('ul').addClass('in');
	
	var saveChargeMiscOrder = function(e){
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
        "sAjaxSource": "/chargeMiscOrder/chargeMiscOrderItemList",
        "aoColumns": [   
          	{"mDataProp":"CHARGE_ORDER_NO","sWidth": "80px"},
          	{"mDataProp":"CNAME","sWidth": "150px"},
			{"mDataProp":"NAME","sWidth": "150px",
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
			            return "<select name='fin_item_id'>"+str+"</select>";
			        }else{
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	return "<select name='fin_item_id'>"+str+"</select>";
			        }
			 }},
			{"mDataProp":"AMOUNT","sWidth": "150px",
			     "fnRender": function(obj) {
			    	 if(obj.aData.CREATE_NAME == 'system'){
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return obj.aData.AMOUNT;
				         }else{
				         	 return "";
				         }
			    	 }else{
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
				         }else{
				         	 return "<input type='text' name='amount'>";
				         }
			    	 }
			 }},  
			{"mDataProp":"REMARK","sWidth": "200px",
			    "fnRender": function(obj) {
			        if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
			            return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
			        }else{
			        	 return "<input type='text' name='remark'>";
			        }
			}}, 
			{"mDataProp":"STATUS","sWidth": "80px","sClass": "status"},                    
			{
				"mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
              		"<i class='fa fa-trash-o fa-fw'> </i> "+
              		"删除"+
              		"</a>";
                }
			}                    
        ]      
    });
    
    $.post('/chargeMiscOrder/searchAllAccount',function(data){
		 if(data.length > 0){
			 var accountTypeSelect = $("#accountTypeSelect");
			 accountTypeSelect.empty();
			 var hideAccountId = $("#hideAccountId").val();
			 accountTypeSelect.append("<option ></option>");
			 for(var i=0; i<data.length+1; i++){
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
    
    $("#paymentMethods").on('click', 'input', function(){
    	if($(this).val() == 'cash'){
    		$("#accountTypeDiv").hide();
    	}else{
    		$("#accountTypeDiv").show();    		
    	}
    }); 
	
	//应收
	$("#addFee").click(function(){	
		 var chargeMiscOrderId =$("#chargeMiscOrderId").val();
		 $.post('/chargeMiscOrder/addNewFee?chargeMiscOrderId='+chargeMiscOrderId,function(data){
			console.log(data);
			if(data.ID > 0){
				feeTable.fnSettings().sAjaxSource = "/chargeMiscOrder/chargeMiscOrderItemList?chargeMiscOrderId="+chargeMiscOrderId;
				feeTable.fnDraw();  
			}
		});		
	});	
	
	//应收修改
	$("#feeItemList-table").on('blur', 'input,select', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		$.post('/chargeMiscOrder/updateChargeMiscOrderItem', {paymentId:paymentId, name:name, value:value}, function(data){
			if(data.success){
			}else{
				alert("修改失败!");
			}
    	},'json');
	});
	
	$("#chargeMiscOrderItem").click(function(){
		feeTable.fnSettings().sAjaxSource = "/chargeMiscOrder/chargeMiscOrderItemList?chargeMiscOrderId="+$("#chargeMiscOrderId").val();
		feeTable.fnDraw();  
	});
	
	//异步删除应付
	$("#feeItemList-table").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		e.preventDefault();
		$.post('/chargeMiscOrder/finItemdel/'+id,function(data){
             //保存成功后，刷新列表
             console.log(data);
             feeTable.fnSettings().sAjaxSource = "/chargeMiscOrder/chargeMiscOrderItemList?chargeMiscOrderId="+$("#chargeMiscOrderId").val();
     		 feeTable.fnDraw();  
        },'text');
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
        			return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
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
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
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