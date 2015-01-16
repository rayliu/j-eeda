$(document).ready(function() {
	 $('#menu_damage').addClass('active').find('ul').addClass('in');	 
	
 	 //显示货品table
 	 var pickupItemTable = $('#pickupItem-table').dataTable({
 		"bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
     	 "oLanguage": {
             "sUrl": "/eeda/dataTables.ch.txt"
         },
         "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
 			$(nRow).attr({item_id: aData.ITEM_ID, fin_id: aData.FIN_ID});
 			return nRow;
 		 },
         "sAjaxSource": "/insuranceOrder/getInitPickupOrderItems",
         "aoColumns": [
             { "mDataProp": "CUSTOMER" ,"sWidth": "150px"},
             { "mDataProp": "ORDER_NO" ,"sWidth": "80px"},      
             { "mDataProp": "ITEM_NO","sWidth": "60px"},
             { "mDataProp": "ITEM_NAME","sWidth": "150px"},
             { "mDataProp": "AMOUNT","sWidth": "50px"},
             { "mDataProp": "REMARK","sWidth": "100px"},
             {"mDataProp":null,"sWidth": "150px",
            	 "sClass": "insurance_category", 
                 "fnRender": function(obj) {
             	 /*var str = "<option value='综合险,附加险' selected = 'selected'>综合险,附加险</option>";                    			
                 return "<select name='insurance_category'>"+str+"</select>";*/
                 return "<span>综合险,附加险</span>";
             }},
             {"mDataProp": "FIN_AMOUNT",
            	 "sWidth": "100px",
            	 "sClass": "fin_amount", 
                 "fnRender": function(obj) {
                	 var str = "";
                     if(obj.aData.FIN_AMOUNT!='' && obj.aData.FIN_AMOUNT != null){
                         str = "<input type='text' name='amount' value='"+obj.aData.FIN_AMOUNT+"'>";
                     }else{
                     	 str = "<input type='text' name='amount' value='"+obj.aData.AMOUNTS+"'>";
                     }
                	 return str;
             }},
             {"mDataProp": "RATE",
            	 "sWidth": "100px", 
            	 "sClass": "rate", 
                 "fnRender": function(obj) {
                	 var str = "";
                     if(obj.aData.RATE!='' && obj.aData.RATE != null){
                         str = "<input type='text' name='rate' value='"+obj.aData.RATE+"'>";
                     }else{
                     	 str = "<input type='text' name='rate' value='"+obj.aData.RATES+"'>";
                     }
                	 return str;
             }},
             { "mDataProp": "START_CREATE_STAMP","sWidth": "150px"},
             { "mDataProp": "ROUTE_FROM",
            	 "sWidth": "150px",
            	 "fnRender": function(obj) {
            		 var route_from = obj.aData.ROUTE_FROM;
            		 if(obj.aData.ROUTE_FROM == null){
            			 reute_from = "";
            		 }
            		 var route_to = obj.aData.ROUTE_TO;
            		 if(obj.aData.ROUTE_TO == null){
            			 reute_to = "";
            		 }
                	 return route_from +" - "+ route_to;
             }},
             { "mDataProp": "INSURANCE_AMOUNT",
            	 "sWidth": "60px",
            	 "sClass": "insurance_amount", 
                 "fnRender": function(obj) {
                	var str = obj.aData.INSURANCE_AMOUNT;
                 	if(obj.aData.INSURANCE_AMOUNT == null){
                 		str = "";
                 	}
                 	return "<span>"+str+"</span>";
             }},
             {"mDataProp": "INSURANCE_NO",
            	 "sWidth": "100px",
            	 "sClass": "insurance_no", 
                 "fnRender": function(obj) {
                	 var str = "";
                     if(obj.aData.INSURANCE_NO!='' && obj.aData.INSURANCE_NO != null){
                         str = "<input type='text' name='insurance_no' value='"+obj.aData.INSURANCE_NO+"'>";
                     }else{
                     	 str = "<input type='text' name='insurance_no'>";
                     }
                	 return str;
             }}                                  
         ]       
     });
 	 
 	 $("#insuranceOrderItemList").click(function(e){
  		 saveInsuranceOrder(e);
 		 var message=$("#message").val();
 	     var tr_item=$("#tr_itemid_list").val();
 	     var item_detail=$("#item_detail").val();
 	     var insuranceOrderId=$("#insuranceOrderId").val();
 	     pickupItemTable.fnSettings().sAjaxSource = "/insuranceOrder/getInitPickupOrderItems?insuranceOrderId="+insuranceOrderId+"&localArr="+message+"&tr_item="+tr_item+"&item_detail="+item_detail,
 	     pickupItemTable.fnDraw();
 	 });

 	// 投保
 	$("#pickupItem-table").on('blur', 'input,select', function(e){
 		e.preventDefault();
 		var itemId = $(this).parent().parent().attr("item_id");
 		var name = $(this).attr("name");
 		var value = $(this).val();
 		if(name == 'amount'){
 			var rate = $(this).parent().siblings(".rate")[0].children[0].value;
 			if(rate != null && rate != '' && value != null && value != ''){
 				$(this).parent().siblings(".insurance_amount")[0].children[0].innerHTML = value * rate;
 			}
 		}else if(name == 'rate'){
 			var amount = $(this).parent().siblings(".fin_amount")[0].children[0].value;
 			if(amount != null && amount != '' && value != null && value != ''){
 				$(this).parent().siblings(".insurance_amount")[0].children[0].innerHTML = (value * amount).toFixed(2);
 			}
 		}
 		var insuranceAmount = $(this).parent().siblings(".insurance_amount")[0].children[0].innerHTML;
 		var insuranceOrderId = $("#insuranceOrderId").val();
 		$.post('/insuranceOrder/updateInsuranceOrderFinItem', {itemId:itemId, insuranceOrderId:insuranceOrderId, name:name, value:value, insuranceAmount:insuranceAmount}, function(data){
 			if(data.success){
 			}else{
 				alert("修改失败!");
 			}
     	},'json');
 	});
 	
 	var tr_itemid_list=[];
 	// 查看货品
	$("#pickupItem-table").on('click', '.dateilEdit', function(e){
		e.preventDefault();
		
		$("#transferOrderItemDateil").show();
		var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		tr_itemid_list.push(itemId);
		$("#item_id").val(itemId);
		$("#item_save").attr("disabled", false);
		$("#style").hide();
		detailTable.fnSettings().sAjaxSource = "/pickupOrder/findAllItemDetail?item_id="+itemId+"&pickupId="+$("#pickupOrderId").val();
		detailTable.fnDraw();  			
	});
		
	var the_id="";
	var item_id = $("#item_id").val();
	var detailTable= $('#pickupDetail-table').dataTable({           
         "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",  
         "iDisplayLength": 10,
         "bServerSide": true, 
     	 "oLanguage": {
             "sUrl": "/eeda/dataTables.ch.txt"
         },
         "sAjaxSource": "/departOrder/itemDetailList?item_id="+item_id+"",
       
         "aoColumns": [
              { "mDataProp": null,
                "fnRender": function(obj) {
             	   the_id=obj.aData.ID;
                    return '<input checked="" type="checkbox" name="detailCheckBox" value="'+obj.aData.ID+'">';
                }
              },
             { "mDataProp": "ITEM_NAME"},      
             { "mDataProp": "ITEM_NO"},
             { "mDataProp": "SERIAL_NO"},
             { "mDataProp": "VOLUME"},
             { "mDataProp": "WEIGHT"},
             { "mDataProp": "REMARK"},           
         ]        
     });
	
    $("#continueCreateBtn").click(function(){
    	$("#detailCheckBoxForm").submit();
    });

    /*--------------------------------------------------------------------*/
    var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);
	
	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	/*--------------------------------------------------------------------*/
	
	var saveInsuranceOrder = function(e){
		e.preventDefault();
 		var bool = false;
		if("chargeCheckOrderbasic" == parentId){
			bool = true;
		}
 		$.post('/insuranceOrder/save', $("#insuranceOrderForm").serialize(), function(data){
			$("#insuranceOrderId").val(data.ID);
			if(data.ID>0){
				$("#insuranceId").val(data.ID);
				if(bool){
					$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				}
			}else{
				alert('数据保存失败。');
			}
		},'json');
        parentId = e.target.getAttribute("id");
	};
	
	//transferOrderForm 不需要提交
	var parentId = "chargeCheckOrderbasic";
 	$("#saveInsuranceOrderBtn").click(function(e){
 		saveInsuranceOrder(e);
	});
   
 	/*var pickupOrderMilestone = function(){
 		var pickupOrderId = $("#pickupOrderId").val();
		$.post('/transferOrderMilestone/transferOrderMilestoneList',{pickupOrderId:pickupOrderId},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.empty();
			for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
			{
				var location = data.transferOrderMilestones[i].LOCATION;
				if(location == null){
					location = "";
				}
				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+location+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
			}
		},'json');
 	};
 	
	// 运输里程碑
	$("#transferOrderMilestoneList").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var bool = false;
		if("chargeCheckOrderbasic" == parentId ||"addressList" == parentId||"pickupOrderPayment" == parentId){
			bool = true;
		}
		
        if($("#pickupOrderId").val() == ""){
	    	$.post('/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
				$("#pickupOrderId").val(data.ID);
				$("#addressPickupOrderId").val(data.ID);
				$("#milestonePickupId").val(data.ID);
				if(data.ID>0){
					$("#pickupId").val(data.ID);
					pickupOrderMilestone();
				  	//$("#style").show();
					if(bool){
						$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
					}
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }else{
        	$.post('/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
				$("#pickupOrderId").val(data.ID);
				$("#addressPickupOrderId").val(data.ID);
				$("#milestonePickupId").val(data.ID);
				if(data.ID>0){		
					$("#pickupId").val(data.ID);	
					pickupOrderMilestone();
				  	//$("#style").show();
					if(bool){
						$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
					}
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }
        parentId = e.target.getAttribute("id");
	});

	// 保存新里程碑
	$("#transferOrderMilestoneFormBtn").click(function(){
		$.post('/transferOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$('#transferOrderMilestone').modal('hide');
		
	});
	
	var findAllAddress = function(){
		var pickupOrderId = $("#pickupOrderId").val();
		$.post('/pickupOrder/findAllAddress', {pickupOrderId:pickupOrderId}, function(data){
			var pickupAddressTbody = $("#pickupAddressTbody");
			pickupAddressTbody.empty();
			for(var i=0;i<data.length;i++){
				pickupAddressTbody.append("<tr value='"+data[i].PICKUP_SEQ+"' id='"+data[i].ID+"'><td>"+data[i].ORDER_NO+"</td><td>"+data[i].CNAME+"</td><td>"+data[i].ADDRESS+"</td><td>"+data[i].CREATE_STAMP+"</td><td><input type='radio' name='lastStopRadio"+data[i].ID+"' checked='' value='yard"+data[i].ID+"'></td><td><input type='radio' name='lastStopRadio"+data[i].ID+"' value='warehouse"+data[i].ID+"'></td><td>"+data[i].STATUS+"</td><td><a href='javascript:void(0)' class='moveUp'>上移</a> <a href='javascript:void(0)' class='moveDown'>下移</a> <a href='javascript:void(0)' class='moveTop'>移至顶部</a> <a href='javascript:void(0)' class='moveButtom'>移至底部</a></td></tr>");					
			}
		},'json');
	};*/
	
	
	/*//异步删除应付
	$("#table_fin2").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		e.preventDefault();
		$.post('/pickupOrder/finItemdel/'+id,function(data){
             //保存成功后，刷新列表
             console.log(data);
             paymenttable.fnDraw();
        },'text');
	});
	
	$("#addrow").click(function(){	
		var pickupOrderId =$("#pickupOrderId").val();
		$.post('/pickupOrder/addNewRow/'+pickupOrderId,function(data){
			console.log(data);
			if(data[0] != null){
				paymenttable.fnSettings().sAjaxSource = "/pickupOrder/accountPayable?pickupOrderId="+pickupOrderId;   
				paymenttable.fnDraw();
			}else{
				alert("请到基础模块维护应付条目！");
			}
		});		
	});	
	
    var pickupOrderId = $("#pickupOrderId").val();
    //应收datatable
	var pickupOrderPaymentTab = $('#table_fin').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/pickupOrder/pickupOrderPaymentList?pickupOrderId="+pickupOrderId,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
			{"mDataProp":"CNAME","sClass": "name"},
			{"mDataProp":"TRANSFERNO","sClass": "amount"},  
			{"mDataProp":"AMOUNT","sClass": "remark"}
        ]      
    });
	
	$("#pickupOrderPayment").click(function(e){
 		clickSavePickupOrder(e);
 		if("chargeCheckOrderbasic" == parentId || "transferOrderMilestoneList" == parentId||"addressList" == parentId){
 			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
 		}
 		
 		pickupOrderPaymentTab.fnDraw();
 		paymenttable.fnDraw();
 		parentId = e.target.getAttribute("id");
	});
	$("#chargeCheckOrderbasic").click(function(e){
		clickSavePickupOrder(e);
		if("pickupOrderPayment" == parentId || "transferOrderMilestoneList" == parentId||"addressList" == parentId){
 			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
 		}
		parentId = e.target.getAttribute("id");
	});
	
	$("#table_fin2").on('blur', 'input,select', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		$.post('/pickupOrder/updatePickupOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
			if(data.success){
			}else{
				alert("修改失败!");
			}
    	},'json');
	});*/
 	
 	// 应付datatable
    var insuranceOrderId = $("#insuranceOrderId").val();
	var accountTab = $('#accountTab').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/insuranceOrder/accountPayable?insuranceOrderId="+insuranceOrderId,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
			{"mDataProp":null,
                "fnRender": function(obj) {
                	return "保险费";
                }
             },
			{"mDataProp":"SUM_AMOUNT"}   
        ]      
    });	
	
	// 应收datatable
	var incomeTab = $('#incomeTab').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		"bFilter": false, //不需要默认的搜索框
		//"sPaginationType": "bootstrap",
		"iDisplayLength": 10,
		"bServerSide": true,
		"sAjaxSource": "/insuranceOrder/incomePayable",
		"oLanguage": {
			"sUrl": "/eeda/dataTables.ch.txt"
		},
		"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('order_id', aData.ORDER_ID);
			return nRow;
		},
		"aoColumns": [
		  	{"mDataProp":"CNAME"},   
		  	{"mDataProp":"TRANSFER_ORDER_NO","sWidth": "100%"},   
	        {"mDataProp":null,
		      	"fnRender": function(obj) {
		      		return "保险费";
		      	}
	        },   
		  	{"mDataProp":"SUM_INSURANCE","sClass": "sum_insurance"},   
		  	{"mDataProp":"SUM_AMOUNT","sClass": "income_rate"},   
		  	{"mDataProp":"INCOME_RATE",
		      	"fnRender": function(obj) {
		      		var str = "";
		      		if(obj.aData.INCOME_RATE != null){
                		str = "<input type='text' name='income_rate' value='"+obj.aData.INCOME_RATE+"'>";
                	}else{
                		str = "<input type='text' name='income_rate'>";
                	}
		      		return str;
		    }},
	        {"mDataProp":null,
		    	"sClass": "income_insurance_amount",
                "fnRender": function(obj) {
                	var str = obj.aData.INCOME_INSURANCE_AMOUNT;
                	if(obj.aData.INCOME_INSURANCE_AMOUNT == null){
                		str = "";
                	}
                	return "<span>"+str+"</span>";
            }}   
        ]      
	});
	
	$("#insuranceOrderPayment").click(function(){
		var insuranceOrderId = $("#insuranceOrderId").val();
		accountTab.fnSettings().sAjaxSource = "/insuranceOrder/accountPayable?insuranceOrderId="+insuranceOrderId;
		accountTab.fnDraw();
		incomeTab.fnSettings().sAjaxSource = "/insuranceOrder/incomePayable?insuranceOrderId="+insuranceOrderId;
		incomeTab.fnDraw();
	});

 	// 应收
 	$("#incomeTab").on('blur', 'input', function(e){
 		e.preventDefault();
 		var order_id = $(this).parent().parent().attr("order_id");
 		var name = $(this).attr("name");
 		var value = $(this).val();
		if(value != null && value != ''){
			var income_rate = $(this).parent().siblings(".sum_insurance")[0].innerHTML;
			$(this).parent().siblings(".income_insurance_amount")[0].children[0].innerHTML = (value * income_rate).toFixed(2);
		}
 		$.post('/insuranceOrder/incomeFinItem', {orderId:order_id, name:name, value:value}, function(data){
 			if(data.success){
 			}else{
 				alert("修改失败!");
 			}
     	},'json');
 	});

	// 获取所有城市
	$.post('/transferOrder/searchPartOffice',function(data){
	 if(data.length > 0){
		 var officeSelect = $("#officeSelect");
		 officeSelect.empty();
		 var hideOfficeId = $("#hideOfficeId").val();
		 officeSelect.append("<option ></option>");
		 for(var i=0; i<data.length; i++){
			 if(data[i].ID == hideOfficeId){
				 officeSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");
			 }else{
				 if(data[i].IS_STOP != true){
				 	officeSelect.append("<option value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");					 
				 }
			}
		 }
	 }
	},'json');
});
