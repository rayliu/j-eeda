$(document).ready(function() {
	 $('#menu_damage').addClass('active').find('ul').addClass('in');
	 
	 var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);
	 
	 //记录基本信息选项卡id，用作保存判断
	 clickTabId = "chargeCheckOrderbasic";
	 
 	 //显示货品table
 	 var pickupItemTable = $('#pickupItem-table').dataTable({
 		"bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": false,
     	 "oLanguage": {
             "sUrl": "/eeda/dataTables.ch.txt"
         },
         "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
 			$(nRow).attr({id: aData.ID});
 			return nRow;
 		 },
         "aoColumns": [
             { "mDataProp": "CUSTOMER" ,"sWidth": "150px"},
             { "mDataProp": "ORDER_NO" ,"sWidth": "80px"},      
             { "mDataProp": "ITEM_NO","sWidth": "150px"},
             { "mDataProp": "ITEM_NAME","sWidth": "150px"},
             { "mDataProp": "AMOUNT","sWidth": "50px","sClass": "amount"},
             { "mDataProp": "REMARK","sWidth": "100px"},
             {"mDataProp":null,
            	 "sWidth": "120px",
            	 "sClass": "insurance_category", 
                 "fnRender": function(obj) {
                 return "<span>综合险,附加险</span>";
             }},
             {"mDataProp": null,
            	 "sWidth": "100px",
            	 "sClass": "fin_amount", 
                 "fnRender": function(obj) {
                     if(obj.aData.FIN_AMOUNT !='' && obj.aData.FIN_AMOUNT != null){
                    	 return "<input type='text' name='amount' value='"+obj.aData.FIN_AMOUNT+"'>";
                     }else{
                    	 return "<input type='text' name='amount' value=''>";
                     }
             }},
             {"mDataProp": "TOTAL_AMOUNT","sWidth": "50px","sClass": "total_amount"},
             {"mDataProp": null,
            	 "sWidth": "100px", 
            	 "sClass": "rate", 
                 "fnRender": function(obj) {
                     if(obj.aData.RATE!='' && obj.aData.RATE != null){
                    	 return "<input type='text' name='rate' value='"+obj.aData.RATE+"'>";
                     }else{
                    	 return "<input type='text' name='rate' value=''>";
                     }
             }},
             { "mDataProp": "START_CREATE_STAMP","sWidth": "150px"},
             { "mDataProp": null,
            	 "sWidth": "150px",
            	 "fnRender": function(obj) {
            		 var route_from = obj.aData.ROUTE_FROM;
            		 var route_to = obj.aData.ROUTE_TO;
            		 if(obj.aData.ROUTE_FROM == null){
            			 reute_from = "";
            		 }
            		 if(obj.aData.ROUTE_TO == null){
            			 reute_to = "";
            		 }
                	 return route_from +" - "+ route_to;
             }},
             { "mDataProp": null,
            	 "sWidth": "80px",
            	 "sClass": "insurance_amount", 
                 "fnRender": function(obj) {
                 	if(obj.aData.INSURANCE_AMOUNT == null){
                 		return "";
                 	}else{
                 		return obj.aData.INSURANCE_AMOUNT.toFixed(2);
                 	}
             }},
             {"mDataProp": null,
            	 "sWidth": "100px",
            	 "sClass": "insurance_no", 
                 "fnRender": function(obj) {
                     if(obj.aData.INSURANCE_NO !='' && obj.aData.INSURANCE_NO != null){
                    	 return "<input type='text' name='insurance_no' value='"+obj.aData.INSURANCE_NO+"'>";
                     }else{
                    	 return "<input type='text' name='insurance_no'>";
                     }
             }}                                  
         ]       
     });
 	
 	var findInsuranceItems =  function(){
 		var insuranceOrderId=$("#insuranceOrderId").val();
		pickupItemTable.fnSettings().oFeatures.bServerSide = true; 
		pickupItemTable.fnSettings().sAjaxSource = "/insuranceOrder/getInitPickupOrderItems?insuranceOrderId="+insuranceOrderId;
		pickupItemTable.fnDraw();
 	};
 	
 	//保存保险单的方法
 	var saveInsuranceOrder = function(){
  		$.post('/insuranceOrder/save', $("#insuranceOrderForm").serialize(), function(data){
 			$("#insuranceOrderId").val(data.ID);
 			if(data.ID>0){
 				$("#insuranceId").val(data.ID);
 				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
 				if(clickTabId == "insuranceOrderItemList"){
 					findInsuranceItems();
 				}else if(clickTabId == "insuranceOrderPayment"){
 					findInsurancePayment();
 				}
 			}else{
 				$.scojs_message('保存成功', $.scojs_message.TYPE_ERROR);
 			}
 		},'json');
 	};
 	
 	//点击“保存”按钮
  	$("#saveInsuranceOrderBtn").click(function(e){
  		saveInsuranceOrder();
 	});
  	//tab 基本信息
  	$("#chargeCheckOrderbasic").click(function(e){
		clickTabId = e.target.getAttribute("id");
	});
 	
  	//tab 货品信息
	$("#insuranceOrderItemList").click(function(e){
		if(clickTabId == "chargeCheckOrderbasic"){
			saveInsuranceOrder();
		}else{
			findInsuranceItems();
		}
		clickTabId = e.target.getAttribute("id");
	});

 	// 投保
 	$("#pickupItem-table").on('blur', 'input,select', function(e){
 		e.preventDefault();
 		var itemId = $(this).parent().parent().attr("id");
 		var name = $(this).attr("name");
 		var value = $(this).val();
 		if(value != ""){
 			var insuranceAmount = 0;
	 		if(name == 'amount'){
	 			if(isNaN(value)){
 					$.scojs_message('【保额】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
 					$(this).val("");
 					$(this).focus();
 					return false;
	 			}else{
	 				var amount = $(this).parent().siblings('.amount')[0].textContent;	
		 			if(amount != null && amount != ''){
		 				var totalAmount = (value * amount).toFixed(2);
		 				var rate = $(this).parent().parent().find("td").find("input[name='rate']").val();
		 		 		$(this).parent().parent().find("td").eq(8).text(totalAmount);
		 		 		if(rate != "" && !isNaN(rate)){
			 				insuranceAmount = (totalAmount * rate).toFixed(2);
			 				$(this).parent().parent().find("td").eq(12).text(insuranceAmount);
			 			}
		 			}
	 			}
	 		}else if(name == 'rate' && !isNaN(value)){
	 			if(isNaN(value)){
 					$.scojs_message('【应付费率】只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
 					$(this).val("");
 					$(this).focus();
 					return false;
	 			}else{
	 				var totalAmount = $(this).parent().siblings('.total_amount')[0].textContent;	
		 			if(totalAmount != null && totalAmount != ''){
		 				insuranceAmount = (value * totalAmount).toFixed(2);
		 				$(this).parent().parent().find("td").eq(12).text(insuranceAmount);
		 			}	
	 			}
	 		}
			$.post('/insuranceOrder/updateInsuranceOrderFinItem', {itemId:itemId, insuranceOrderId:insuranceOrderId, name:name, value:value,insuranceAmount:insuranceAmount}, function(data){
	 			if(!data.success){
	 				$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	 			}
	     	},'json');
 		}
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
		  	/*{"mDataProp":"SUM_AMOUNT","sClass": "income_rate"},  */ 
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
	
	var findInsurancePayment =  function(){
		var insuranceOrderId = $("#insuranceOrderId").val();
		accountTab.fnSettings().sAjaxSource = "/insuranceOrder/accountPayable?insuranceOrderId="+insuranceOrderId;
		accountTab.fnDraw();
		incomeTab.fnSettings().sAjaxSource = "/insuranceOrder/incomePayable?insuranceOrderId="+insuranceOrderId;
		incomeTab.fnDraw();
 	};
	
	//tab 应收应付
	$("#insuranceOrderPayment").click(function(e){
		if(clickTabId == "chargeCheckOrderbasic"){
			saveInsuranceOrder();
		}else{
			findInsurancePayment();
		}
		clickTabId = e.target.getAttribute("id");
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
 			if(!data.success){
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
