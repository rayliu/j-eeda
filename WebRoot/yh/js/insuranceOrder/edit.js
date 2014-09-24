$(document).ready(function() {
	 $('#menu_damage').addClass('active').find('ul').addClass('in');	 
	
	 var message=$("#message").val();
     var type=$("#type").val();
     var depart_id=$("#depart_id").val();
     var tr_item=$("#tr_itemid_list").val();
     var item_detail=$("#item_detail").val();
 	 //显示货品table
 	 var datatable = $('#pickupItem-table').dataTable({
         "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
         //"sPaginationType": "bootstrap",
         "iDisplayLength": 10,
         "bServerSide": true,
         "bDestroy": true,
     	 "oLanguage": {
             "sUrl": "/eeda/dataTables.ch.txt"
         },
         "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
 			$(nRow).attr({item_id: aData.ITEM_ID, fin_id: aData.FIN_ID});
 			return nRow;
 		 },
         "sAjaxSource": "/yh/insuranceOrder/getInitPickupOrderItems?localArr="+message+"&tr_item="+tr_item+"&item_detail="+item_detail,
         "aoColumns": [
             { "mDataProp": "CUSTOMER" ,"sWidth": "100%"},
             { "mDataProp": "ORDER_NO" ,"sWidth": "30%"},      
             { "mDataProp": "ITEM_NO"},
             { "mDataProp": "ITEM_NAME"},
             { "mDataProp": "AMOUNT"},
             { "mDataProp": "REMARK"},
             {"mDataProp":null,
            	 "sClass": "insurance_category", 
                 "fnRender": function(obj) {
             	 var str = "<option value='综合险,附加险' selected = 'selected'>综合险,附加险</option>";                    			
                 return "<select name='insurance_category'>"+str+"</select>";
             }},
             {"mDataProp": "FIN_AMOUNT",
            	 "sClass": "fin_amount", 
                 "fnRender": function(obj) {
                	 var str = "";
                     if(obj.aData.FIN_AMOUNT!='' && obj.aData.FIN_AMOUNT != null){
                         str = "<input type='text' name='amount' value='"+obj.aData.FIN_AMOUNT+"'>";
                     }else{
                     	 str = "<input type='text' name='amount'>";
                     }
                	 return str;
             }},
             {"mDataProp": "RATE",
            	 "sClass": "rate", 
                 "fnRender": function(obj) {
                	 var str = "";
                     if(obj.aData.RATE!='' && obj.aData.RATE != null){
                         str = "<input type='text' name='rate' value='"+obj.aData.RATE+"'>";
                     }else{
                     	 str = "<input type='text' name='rate'>";
                     }
                	 return str;
             }},
             { "mDataProp": "START_CREATE_STAMP"},
             { "mDataProp": "ROUTE_FROM",
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
            	 "sClass": "insurance_amount", 
                 "fnRender": function(obj) {
                	 /*var str = "";
                     if(obj.aData.INSURANCE_AMOUNT!='' && obj.aData.INSURANCE_AMOUNT != null){
                         str = "<input type='text' name='insurance_amount' disabled value='"+obj.aData.INSURANCE_AMOUNT+"'>";
                     }else{
                     	 str = "<input type='text' name='insurance_amount' disabled>";
                     }
                	 return str;*/
                	var str = obj.aData.INSURANCE_AMOUNT;
                 	if(obj.aData.INSURANCE_AMOUNT == null){
                 		str = "";
                 	}
                 	return "<span>"+str+"</span>";
             }},
             {"mDataProp": "INSURANCE_NO",
            	 "sClass": "insurance_no", 
                 "fnRender": function(obj) {
                	 var str = "";
                     if(obj.aData.INSURANCE_NO!='' && obj.aData.INSURANCE_NO != null){
                         str = "<input type='text' name='insurance_no' value='"+obj.aData.INSURANCE_NO+"'>";
                     }else{
                     	 str = "<input type='text' name='insurance_no'>";
                     }
                	 return str;
             }},
             /*{ 
                 "mDataProp": null, 
                 "sWidth": "8%",                
                 "fnRender": function(obj) {                    
                     return "<a class='btn btn-success dateilEdit' code='?id="+obj.aData.ID+"'>"+
                                 "<i class='fa fa-search fa-fw'></i>"+
                                 "查看"+
                             "</a>";
                 },
             }    */                                   
         ],
         "fnInitComplete": function(oSettings, json) {
         	$("#eeda-table td").on('click', '', function(){
         	 hang = $(this).parent("tr").prevAll().length; 
        		  	hang = Number(hang)+1;
         	});         	    
         }       
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
 				$(this).parent().siblings(".insurance_amount")[0].children[0].innerHTML = value * amount;
 			}
 		}
 		var insuranceAmount = $(this).parent().siblings(".insurance_amount")[0].children[0].innerHTML;
 		var insuranceOrderId = $("#insuranceOrderId").val();
 		$.post('/yh/insuranceOrder/updateInsuranceOrderFinItem', {itemId:itemId, insuranceOrderId:insuranceOrderId, name:name, value:value, insuranceAmount:insuranceAmount}, function(data){
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
		detailTable.fnSettings().sAjaxSource = "/yh/pickupOrder/findAllItemDetail?item_id="+itemId+"&pickupId="+$("#pickupOrderId").val();
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
         "sAjaxSource": "/yh/departOrder/itemDetailList?item_id="+item_id+"",
       
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
	
	//transferOrderForm 不需要提交
	var parentId = "chargeCheckOrderbasic";
 	$("#saveInsuranceOrderBtn").click(function(e){
 		e.preventDefault();
 		var bool = false;
		if("chargeCheckOrderbasic" == parentId){
			bool = true;
		}
 		$.post('/yh/insuranceOrder/save', $("#insuranceOrderForm").serialize(), function(data){
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
	});
   
 	/*var pickupOrderMilestone = function(){
 		var pickupOrderId = $("#pickupOrderId").val();
		$.post('/yh/transferOrderMilestone/transferOrderMilestoneList',{pickupOrderId:pickupOrderId},function(data){
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
	    	$.post('/yh/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
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
        	$.post('/yh/pickupOrder/savePickupOrder', $("#pickupOrderForm").serialize(), function(data){
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
		$.post('/yh/transferOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$('#transferOrderMilestone').modal('hide');
		
	});
	
	var findAllAddress = function(){
		var pickupOrderId = $("#pickupOrderId").val();
		$.post('/yh/pickupOrder/findAllAddress', {pickupOrderId:pickupOrderId}, function(data){
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
		$.post('/yh/pickupOrder/finItemdel/'+id,function(data){
             //保存成功后，刷新列表
             console.log(data);
             paymenttable.fnDraw();
        },'text');
	});
	
	$("#addrow").click(function(){	
		var pickupOrderId =$("#pickupOrderId").val();
		$.post('/yh/pickupOrder/addNewRow/'+pickupOrderId,function(data){
			console.log(data);
			if(data[0] != null){
				paymenttable.fnSettings().sAjaxSource = "/yh/pickupOrder/accountPayable?pickupOrderId="+pickupOrderId;   
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
        "sAjaxSource": "/yh/pickupOrder/pickupOrderPaymentList?pickupOrderId="+pickupOrderId,
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
		$.post('/yh/pickupOrder/updatePickupOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
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
        "sAjaxSource": "/yh/insuranceOrder/accountPayable?insuranceOrderId="+insuranceOrderId,
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
	var insuranceOrderId = $("#insuranceOrderId").val();
	var incomeTab = $('#incomeTab').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		"bFilter": false, //不需要默认的搜索框
		//"sPaginationType": "bootstrap",
		"iDisplayLength": 10,
		"bServerSide": true,
		"sAjaxSource": "/yh/insuranceOrder/incomePayable?insuranceOrderId="+insuranceOrderId,
		"oLanguage": {
			"sUrl": "/eeda/dataTables.ch.txt"
		},
		"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
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
		accountTab.fnSettings().sAjaxSource = "/yh/insuranceOrder/accountPayable?insuranceOrderId="+insuranceOrderId;
		accountTab.fnDraw();
		incomeTab.fnSettings().sAjaxSource = "/yh/insuranceOrder/incomePayable?insuranceOrderId="+insuranceOrderId;
		incomeTab.fnDraw();
	});

 	// 应收
 	$("#incomeTab").on('blur', 'input', function(e){
 		e.preventDefault();
 		var id = $(this).parent().parent().attr("id");
 		var name = $(this).attr("name");
 		var value = $(this).val();
		if(value != null && value != ''){
			var income_rate = $(this).parent().siblings(".income_rate")[0].innerHTML;
			$(this).parent().siblings(".income_insurance_amount")[0].children[0].innerHTML = value * income_rate;
		}
 		var insuranceOrderId = $("#insuranceOrderId").val();
 		$.post('/yh/insuranceOrder/incomeFinItem', {id:id, insuranceOrderId:insuranceOrderId, name:name, value:value}, function(data){
 			if(data.success){
 			}else{
 				alert("修改失败!");
 			}
     	},'json');
 	});
});
