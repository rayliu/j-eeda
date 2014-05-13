
$(document).ready(function() {
	$('#menu_transfer').addClass('active').find('ul').addClass('in');
    //from表单验证
	var validate = $('#transferOrderUpdateForm').validate({
        rules: {
        	customerMessage: {
            required: true
          },
			spMessage: {
            required: true
          }
        },
        messages : {	             
        	customerMessage : {required:  "请选择一个客户"}, 
        	spMessage : {required:  "请选择一个供应商"}, 
        }
    });
		
     // tooltip demo
     $('.tooltip-demo').tooltip({
       selector: "[data-toggle=tooltip]",
       container: "body"
     });
		
	//获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup', function(){
		var inputStr = $('#customerMessage').val();
		if(inputStr == ""){
			var pageCustomerName = $("#pageCustomerName");
			pageCustomerName.empty();
			var pageCustomerAddress = $("#pageCustomerAddress");
			pageCustomerAddress.empty();
			$('#customer_id').val($(this).attr(''));
		}
		$.get('/yh/transferOrder/searchCustomer', {input:inputStr}, function(data){
			console.log(data);
			var customerList =$("#customerList");
			customerList.empty();
			for(var i = 0; i < data.length; i++)
			{
				customerList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+" "+data[i].CONTACT_PERSON+" "+data[i].PHONE+"</a></li>");
			}
		},'json');
        $('#customerList').show();
	});
	
	// 选中客户
	$('#customerList').on('click', '.fromLocationItem', function(e){
		$('#customerMessage').val($(this).text());
		$('#customer_id').val($(this).attr('partyId'));
		var pageCustomerName = $("#pageCustomerName");
		pageCustomerName.empty();
		pageCustomerName.append($(this).attr('contact_person')+'&nbsp;');
		pageCustomerName.append($(this).attr('phone')); 
		var pageCustomerAddress = $("#pageCustomerAddress");
		pageCustomerAddress.empty();
		pageCustomerAddress.append($(this).attr('address'));

        $('#customerList').hide();
    }); 
	
	//获取供应商的list，选中信息在下方展示其他信息
	$('#spMessage').on('keyup', function(){
		var inputStr = $('#spMessage').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/yh/transferOrder/searchSp', {input:inputStr}, function(data){
			console.log(data);
			var spList =$("#spList");
			spList.empty();
			for(var i = 0; i < data.length; i++)
			{
				spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+" "+data[i].CONTACT_PERSON+" "+data[i].PHONE+"</a></li>");
			}
		},'json');

        $('#spList').show();
	});
	
	// 选中供应商
	$('#spList').on('click', '.fromLocationItem', function(e){
		$('#spMessage').val($(this).text());
		$('#sp_id').val($(this).attr('partyId'));
		var pageSpName = $("#pageSpName");
		pageSpName.empty();
		pageSpName.append($(this).attr('contact_person')+'&nbsp;');
		pageSpName.append($(this).attr('phone')); 
		var pageSpAddress = $("#pageSpAddress");
		pageSpAddress.empty();
		pageSpAddress.append($(this).attr('address'));
        $('#spList').hide();
    }); 
	
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveTransferOrderBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#transferOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
        if($("#order_id").val() == ""){
	    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#departureConfirmationBtn").attr("disabled", false);
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//alert("运输单保存成功!");
				  	$("#style").show();	              
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }else{
        	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#departureConfirmationBtn").attr("disabled", false);
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//alert("运输单保存成功!");
				  	$("#style").show();	            
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }
	});

    //货品明细的table 编辑
    $("table.table tr td").bind("click", dataClick);
    function dataClick(e) {
        console.log(e);
        if (e.currentTarget.innerHTML != "") return;
        if(e.currentTarget.contentEditable != null){
            $(e.currentTarget).attr("contentEditable",true);
        }else{
            $(e.currentTarget).append("<input type='text' value="+e.currentTarget.innerHTML+">");
        }    
    }

    // 单击货品明细时,应列表显示所有的货品
    $("#transferOrderItemList").click(function(e){
        e.preventDefault();
    	// 切换到货品明细时,应先保存运输单
    	// 应先判断order_id是否为空
    	//提交前，校验数据
        if(!$("#transferOrderUpdateForm").valid()){
        	alert("请先保存运输单!");
	       	return false; 
        }

        if($("#order_id").val() == ""){
	    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#departureConfirmationBtn").attr("disabled", false);
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//alert("运输单保存成功!");
				  	$("#style").show();	
				  	
	            	var order_id = $("#order_id").val();
				  	itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;
				  	itemDataTable.fnDraw();                
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }else{
        	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderUpdateForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#departureConfirmationBtn").attr("disabled", false);
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//alert("运输单保存成功!");
				  	$("#style").show();	
				  	
	            	var order_id = $("#order_id").val();
				  	itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;
				  	itemDataTable.fnDraw();                
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }
    	
    	/*$.post('/yh/transferOrderItem/transferOrderItemList',{},function(data){
			var transferOrderItemTbody = $("#transferOrderItemTbody");
			transferOrderItemTbody.empty();
			for(var i = 0; i < data.length; i++)
			{
				var item = data[i].ITEM_NO;
				if(item == null){
					item = "";
					transferOrderItemTbody.append("<tr><td><a class='btn' href='#'>查看序列号</a></td><td>"+item+"</td><td>"+data[i].AMOUNT+"</td><td>"+data[i].UNIT+"</td><td>"+data[i].REMARK+"</td><td><button id='detailEdit("+data[i].ID+"|"+data[i].ORDER_ID+"' type='submit' class='btn btn-default'>单品编辑</button><button id='itemEdit("+data[i].ID+"' type='submit' class='btn btn-default'>编辑</button><button id='deleteItem("+data[i].ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
				}else{
					transferOrderItemTbody.append("<tr><td><a class='btn' href='#'>查看序列号</a></td><td>"+data[i].ITEM_NO+"</td><td>"+data[i].AMOUNT+"</td><td>"+data[i].UNIT+"</td><td>"+data[i].REMARK+"</td><td><button id='detailEdit("+data[i].ID+"|"+data[i].ORDER_ID+"' type='submit' class='btn btn-default'>单品编辑</button><button id='itemEdit("+data[i].ID+"' type='submit' class='btn btn-default'>编辑</button><button id='deleteItem("+data[i].ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
				}
			}
		},'json');*/
    });

	
    
	var order_id = $("#order_id").val();
	//datatable, 动态处理
    var itemDataTable = $('#itemTable').dataTable({
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "aoColumns": [  			            
            {"mDataProp":"ITEM_NO"},
            {"mDataProp":"AMOUNT"},       	
            {"mDataProp":"UNIT"},
            {"mDataProp":"REMARK"},
            {  
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {
                    return	"<a class='btn btn-success dateilEdit' href='?id="+obj.aData.ID+"&order_id="+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "单品编辑"+
                            "</a>"+
                    		"<a class='btn btn-success' href='/yh/transferOrder/edit/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger' href='/yh/transferOrder/delete/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ]      
    });	
    
    $('#itemTable').on( 'dateilEdit2', function () {
        alert( 'dateilEdit2' );
    } );
    
   	// 保存货品
    $("#transferOrderItemFormBtn").click(function(){
    	$.post('/yh/transferOrderItem/saveTransferOrderItem', $("#transferOrderItemForm").serialize(), function(data){
			if(data.ID > 0){
				//保存成功后，刷新列表
                console.log(data);
                if(data.ORDER_ID>0){
                	var order_id = $("#order_id").val();
                	itemDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItem/transferOrderItemList?order_id="+order_id;
                	itemDataTable.fnDraw();
                }else{
                    alert('数据保存失败。');
                }
				/*var transferOrderItemTbody =$("#transferOrderItemTbody");
				var item_no = "";  
				var amount = $("#amount").val();  
				var unit = $("#unit").val();   
				var remark = $("#remark").val(); */
				//transferOrderItemTbody.append("<tr><td><a class='btn' href='#'>查看序列号</a></td><td>"+item_no+"</td><td>"+amount+"</td><td>"+unit+"</td><td>"+remark+"</td><td><button id='detailEdit("+data.ID+"|"+data.ORDER_ID+"' type='submit' class='btn btn-default'>单品编辑</button><button id='itemEdit("+data.ID+"' type='submit' class='btn btn-default'>编辑</button><button id='deleteItem("+data.ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
				// 关闭模态框
				$('#myModal').modal('hide');
			}
		},'json');
    });
    
   	// 更新货品
    $("#transferOrderItemUpdateFormBtn").click(function(){
    	
    	$.post('/yh/transferOrderItem/saveTransferOrderItem', $("#transferOrderItemUpdateForm").serialize(), function(data){
			if(data.ID > 0){ 
				// 关闭模态框
				$('#updateMyModal').modal('hide');
				$.post('/yh/transferOrderItem/transferOrderItemList',{},function(data){
					var transferOrderItemTbody = $("#transferOrderItemTbody");
					transferOrderItemTbody.empty();
					for(var i = 0; i < data.length; i++)
					{
						var item = data[i].ITEM_NO;
						if(item == null){
							item = "";
							transferOrderItemTbody.append("<tr><td><a class='btn' href='#'>查看序列号</a></td><td>"+item+"</td><td>"+data[i].AMOUNT+"</td><td>"+data[i].UNIT+"</td><td>"+data[i].REMARK+"</td><td><button id='detailEdit("+data[i].ID+"|"+data[i].ORDER_ID+"' type='submit' class='btn btn-default'>单品编辑</button><button id='itemEdit("+data[i].ID+"' type='submit' class='btn btn-default'>编辑</button><button id='deleteItem("+data[i].ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
						}else{
							transferOrderItemTbody.append("<tr><td><a class='btn' href='#'>查看序列号</a></td><td>"+data[i].ITEM_NO+"</td><td>"+data[i].AMOUNT+"</td><td>"+data[i].UNIT+"</td><td>"+data[i].REMARK+"</td><td><button id='detailEdit("+data[i].ID+"|"+data[i].ORDER_ID+"' type='submit' class='btn btn-default'>单品编辑</button><button id='itemEdit("+data[i].ID+"' type='submit' class='btn btn-default'>编辑</button><button id='deleteItem("+data[i].ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
						}
					}
				},'json');
			}
		},'json');
    });
    
    // 当arrivalMode1为货品直送时则显示收货人的信息
    $("#arrivalModes").on('click', 'input', function(){
  	  console.log(this);
  	  var inputId  = $(this).attr('id');
	  if(inputId=='arrivalMode1'){
		 $("#contactInformation").show();
		 $("#warehousingConfirmBtn").attr("disabled", true);
	  }else{
		 $("#contactInformation").hide();
	  } 
  	});
    
    // 单品编辑
    $("#transferOrderItemTbody").on('click', 'button', function(){
    	var inputId  = $(this).attr('id');
    	var type = inputId.substring(0,inputId.indexOf('('));
    	if(type == 'detailEdit'){
	  	    var order_id = inputId.substring(inputId.indexOf('|')+1);
	  	    var detail = inputId.substring(0,inputId.indexOf('|'));
	  	    var item_id = detail.substring(inputId.indexOf('(')+1);
	  	    $("#detail_transfer_order_item_id").val(item_id);
	  	    $("#detail_transfer_order_id").val(order_id);
	  	    $("#transferOrderItemDateil").show();
	  	    // 动态展示单品
	  	    $.post('/yh/transferOrderItemDetail/getAllTransferOrderItemDetail', 'transfer_order_item_id='+item_id, function(data){
				var transferOrderItemDetailTbody = $("#transferOrderItemDetailTbody");
				transferOrderItemDetailTbody.empty();
				if(data.transferOrderItemDetails.length > 0 && data.contacts.length > 0){
					for(var i=0,j=0;i<data.transferOrderItemDetails.length,j<data.contacts.length;i++,j++){
			  	    	transferOrderItemDetailTbody.append("<tr><td>"+data.transferOrderItemDetails[i].SERIAL_NO+"</td><td>"+data.transferOrderItemDetails[i].ITEM_NAME+"</td><td>"+data.transferOrderItemDetails[i].VOLUME+"</td><td>"+data.transferOrderItemDetails[i].WEIGHT+"</td><td>"+data.contacts[j].CONTACT_PERSON+"<br/>"+data.contacts[j].PHONE+"<br/>"+data.contacts[j].ADDRESS+"<br/></td>"
			                       + "<td>"+data.transferOrderItemDetails[i].REMARK+"</td><td>"+data.transferOrderItemDetails[i].IS_DAMAGE+"</td><td>"+data.transferOrderItemDetails[i].ESTIMATE_DAMAGE_AMOUNT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REVENUE+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_PAYMENT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REMARK+"</td>"
			                       + "<td><button id='itemDetailEdit("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>编辑</button><button id='itemDetailDelete("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
		  	    	}
				}else{
					for(var i=0;i<data.transferOrderItemDetails.length;i++){
			  	    	transferOrderItemDetailTbody.append("<tr><td>"+data.transferOrderItemDetails[i].SERIAL_NO+"</td><td>"+data.transferOrderItemDetails[i].ITEM_NAME+"</td><td>"+data.transferOrderItemDetails[i].VOLUME+"</td><td>"+data.transferOrderItemDetails[i].WEIGHT+"</td><td></td>"
			                       + "<td>"+data.transferOrderItemDetails[i].REMARK+"</td><td>"+data.transferOrderItemDetails[i].IS_DAMAGE+"</td><td>"+data.transferOrderItemDetails[i].ESTIMATE_DAMAGE_AMOUNT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REVENUE+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_PAYMENT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REMARK+"</td>"
			                       + "<td><button id='itemDetailEdit("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>编辑</button><button id='itemDetailDelete("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
		  	    	}
				}
			},'json');
    	}else if(type == 'itemEdit'){
	  	    var id = inputId.substring(inputId.indexOf('(')+1);
	  	    $("#transfer_order_item_id").val(id);
	  	    $.post('/yh/transferOrderItem/getTransferOrderItem', 'transfer_order_item_id='+id, function(data){
	  	    	// 编辑时回显数据
 	  	    	$("#update_item_name").val(data.ITEM_NAME);
 	  	    	$("#update_amount").val(data.AMOUNT);
 	  	    	$("#update_unit").val(data.UNIT);
 	  	    	$("#update_volume").val(data.VOLUME);
 	  	    	$("#update_weight").val(data.WEIGHT);
 	  	    	$("#update_remark").val(data.REMARK);
			},'json');
	  	    
	  	  	$.post('/yh/transferOrderItem/transferOrderItemList',{},function(data){
				var transferOrderItemTbody = $("#transferOrderItemTbody");
				transferOrderItemTbody.empty();
				for(var i = 0; i < data.length; i++)
				{
					var item = data[i].ITEM_NO;
					if(item == null){
						item = "";
						transferOrderItemTbody.append("<tr><td><a class='btn' href='#'>查看序列号</a></td><td>"+item+"</td><td>"+data[i].AMOUNT+"</td><td>"+data[i].UNIT+"</td><td>"+data[i].REMARK+"</td><td><button id='detailEdit("+data[i].ID+"|"+data[i].ORDER_ID+"' type='submit' class='btn btn-default'>单品编辑</button><button id='itemEdit("+data[i].ID+"' type='submit' class='btn btn-default'>编辑</button><button id='deleteItem("+data[i].ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
					}else{
						transferOrderItemTbody.append("<tr><td><a class='btn' href='#'>查看序列号</a></td><td>"+data[i].ITEM_NO+"</td><td>"+data[i].AMOUNT+"</td><td>"+data[i].UNIT+"</td><td>"+data[i].REMARK+"</td><td><button id='detailEdit("+data[i].ID+"|"+data[i].ORDER_ID+"' type='submit' class='btn btn-default'>单品编辑</button><button id='itemEdit("+data[i].ID+"' type='submit' class='btn btn-default'>编辑</button><button id='deleteItem("+data[i].ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
					}
				}
			},'json');
	  	    
	  		// 模态框:修改货品明细
			$('#updateMyModal').modal('show');	
		}else{
			var id = inputId.substring(inputId.indexOf('(')+1);
			$.post('/yh/transferOrderItem/deleteTransferOrderItem', 'transfer_order_item_id='+id, function(data){
			},'json');
			
			$.post('/yh/transferOrderItem/transferOrderItemList',{},function(data){
				var transferOrderItemTbody = $("#transferOrderItemTbody");
				transferOrderItemTbody.empty();
				for(var i = 0; i < data.length; i++)
				{
					var item = data[i].ITEM_NO;
					if(item == null){
						item = "";
						transferOrderItemTbody.append("<tr><td><a class='btn' href='#'>查看序列号</a></td><td>"+item+"</td><td>"+data[i].AMOUNT+"</td><td>"+data[i].UNIT+"</td><td>"+data[i].REMARK+"</td><td><button id='detailEdit("+data[i].ID+"|"+data[i].ORDER_ID+"' type='submit' class='btn btn-default'>单品编辑</button><button id='itemEdit("+data[i].ID+"' type='submit' class='btn btn-default'>编辑</button><button id='deleteItem("+data[i].ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
					}else{
						transferOrderItemTbody.append("<tr><td><a class='btn' href='#'>查看序列号</a></td><td>"+data[i].ITEM_NO+"</td><td>"+data[i].AMOUNT+"</td><td>"+data[i].UNIT+"</td><td>"+data[i].REMARK+"</td><td><button id='detailEdit("+data[i].ID+"|"+data[i].ORDER_ID+"' type='submit' class='btn btn-default'>单品编辑</button><button id='itemEdit("+data[i].ID+"' type='submit' class='btn btn-default'>编辑</button><button id='deleteItem("+data[i].ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
					}
				}
			},'json');
    	}
    });
						
	// 发车确认
	$("#departureConfirmationBtn").click(function(){
		// 浏览器启动时,停到当前位置
		//debugger;
		if($("#arrivalModeVal").val() == 'delivery'){
			$("#warehousingConfirmBtn").attr("disabled", true);
		}else{
			$("#warehousingConfirmBtn").attr("disabled", false);	
		} 
		$("#receiptBtn").attr("disabled", false); 

		var order_id = $("#order_id").val();
		$.post('/yh/transferOrderMilestone/departureConfirmation',{order_id:order_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
	});
	
	$("input[name='arrivalMode']").each(
		function(){
			if($(this).attr('checked') == 'checked'){
				 $("#contactInformation").show();
				 return false; 
			}else{
				 $("#contactInformation").hide();
			}
		}			
	)
	
	// 保存单品信息
	$("#transferOrderItemDetailFormBtn").click(function(){
		$.post('/yh/transferOrderItemDetail/saveTransferOrderItemDetail', $("#transferOrderItemDetailForm").serialize(), function(transferOrderItemDetail){
			if(transferOrderItemDetail.ID > 0){
				// 动态显示单品信息
				var transferOrderItemDetailTbody = $("#transferOrderItemDetailTbody");
				var serial_no = $("#serial_no").val();                              
				var detail_item_name = $("#detail_item_name").val();                       
				var detail_volume = $("#detail_volume").val();                             
				var detail_weight = $("#detail_weight").val();                             
				var detail_remark = $("#detail_remark").val();                             
				var detail_is_damage = $("#detail_is_damage").val();                       
				var detail_estimate_damage_amount = $("#detail_estimate_damage_amount").val();
				var detail_damage_revenue = $("#detail_damage_revenue").val();             
				var detail_damage_payment = $("#detail_damage_payment").val();             
				var detail_damage_remark = $("#detail_damage_remark").val();               
				var contact_person = $("#detail_contact_person").val();
				var phone = $("#detail_phone").val();               
				var address = $("#detail_address").val();   
				transferOrderItemDetailTbody.append("<tr><td>"+serial_no+"</td><td>"+detail_item_name+"</td><td>"+detail_volume+"</td><td>"+detail_weight+"</td><td>"+contact_person+"<br/>"+phone+"<br/>"+address+"<br/></td>"
	                       + "<td>"+detail_remark+"</td><td>"+detail_is_damage+"</td><td>"+detail_estimate_damage_amount+"<br/>"+detail_damage_revenue+"<br/>"+detail_damage_payment+"<br/>"+detail_damage_remark+"</td>"
	                       + "<td><button id='itemDetailEdit("+transferOrderItemDetail.ID+"|"+transferOrderItemDetail.ITEM_ID+"["+transferOrderItemDetail.ORDER_ID+"-"+transferOrderItemDetail.NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>编辑</button><button id='itemDetailDelete("+transferOrderItemDetail.ID+"|"+transferOrderItemDetail.ITEM_ID+"["+transferOrderItemDetail.ORDER_ID+"-"+transferOrderItemDetail.NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
				$("#detailModal").modal('hide');
			}			
		});
	});
	
	// 点击单品按钮
	$("#transferOrderItemDetailTbody").on('click', 'button', function(){
		var inputId = $(this).attr('id');
		var notify_party_id = inputId.substring(inputId.indexOf('-')+1);
		var contactInfo = inputId.substring(0,inputId.indexOf('-'));
		var order_id = contactInfo.substring(contactInfo.indexOf('[')+1);
		var detailItem = contactInfo.substring(0,contactInfo.indexOf('['));
		var item_id = detailItem.substring(detailItem.indexOf('|')+1);
		var typeDetail = detailItem.substring(0,detailItem.indexOf('|'));
  	    var detail_id = typeDetail.substring(typeDetail.indexOf('(')+1);
		var type = inputId.substring(0,inputId.indexOf('('));
    	if(type == 'itemDetailEdit'){
			//alert(inputId+", order_id=" + order_id +", item_id="+item_id+", detail_id="+detail_id+", contact_id="+notify_party_id);
	  	    
	  	    $("#update_detail_transfer_order_id").val(order_id);
	  	    $("#update_detail_transfer_order_item_id").val(item_id);
	  	    $("#detail_transfer_order_item_detail_id").val(detail_id);
	  	    $("#detail_notify_party_id").val(notify_party_id);
	  	    $.post('/yh/transferOrderItemDetail/getTransferOrderItemDetail', {detail_id:detail_id,notify_party_id:notify_party_id}, function(data){
	  	    	// 编辑时回显数据
	  	    	$("#update_serial_no").val(data.transferOrderItemDetail.SERIAL_NO);
	  	    	$("#update_detail_item_name").val(data.transferOrderItemDetail.ITEM_NAME);
	  	    	$("#update_detail_volume").val(data.transferOrderItemDetail.VOLUME);
	  	    	$("#update_detail_weight").val(data.transferOrderItemDetail.WEIGHT);
	  	    	$("#update_detail_remark").val(data.transferOrderItemDetail.REMARK);
	  	    	$("#update_detail_contact_person").val(data.contact.CONTACT_PERSON);
	  	    	$("#update_detail_phone").val(data.contact.PHONE);
	  	    	$("#update_detail_address").val(data.contact.ADDRESS);
	  	    	$("#update_detail_is_damage").val(data.transferOrderItemDetail.IS_DAMAGE);
	  	    	$("#update_detail_estimate_damage_amount").val(data.transferOrderItemDetail.ESTIMATE_DAMAGE_AMOUNT);
	  	    	$("#update_detail_damage_revenue").val(data.transferOrderItemDetail.DAMAGE_REVENUE);
	  	    	$("#update_detail_damage_payment").val(data.transferOrderItemDetail.DAMAGE_PAYMENT);
	  	    	$("#update_detail_damage_remark").val(data.transferOrderItemDetail.DAMAGE_REMARK);
			},'json');

	  		// 模态框:修改单品明细
			$('#updateDetailModal').modal('show');	
    	}else{
			//alert(inputId+", order_id=" + order_id +", item_id="+item_id+", detail_id="+detail_id+", contact_id="+notify_party_id);
    		$.post('/yh/transferOrderItemDetail/deleteTransferOrderItemDetail', {detail_id:detail_id,notify_party_id:notify_party_id}, function(data){},'json');
    		
    		// 动态展示单品
	  	    $.post('/yh/transferOrderItemDetail/getAllTransferOrderItemDetail', 'transfer_order_item_id='+item_id, function(data){
				var transferOrderItemDetailTbody = $("#transferOrderItemDetailTbody");
				transferOrderItemDetailTbody.empty();
				if(data.transferOrderItemDetails.length > 0 && data.contacts.length > 0){
					for(var i=0,j=0;i<data.transferOrderItemDetails.length,j<data.contacts.length;i++,j++){
			  	    	transferOrderItemDetailTbody.append("<tr><td>"+data.transferOrderItemDetails[i].SERIAL_NO+"</td><td>"+data.transferOrderItemDetails[i].ITEM_NAME+"</td><td>"+data.transferOrderItemDetails[i].VOLUME+"</td><td>"+data.transferOrderItemDetails[i].WEIGHT+"</td><td>"+data.contacts[j].CONTACT_PERSON+"<br/>"+data.contacts[j].PHONE+"<br/>"+data.contacts[j].ADDRESS+"<br/></td>"
			                       + "<td>"+data.transferOrderItemDetails[i].REMARK+"</td><td>"+data.transferOrderItemDetails[i].IS_DAMAGE+"</td><td>"+data.transferOrderItemDetails[i].ESTIMATE_DAMAGE_AMOUNT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REVENUE+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_PAYMENT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REMARK+"</td>"
			                       + "<td><button id='itemDetailEdit("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>编辑</button><button id='itemDetailDelete("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
		  	    	}
				}else{
					for(var i=0;i<data.transferOrderItemDetails.length;i++){
			  	    	transferOrderItemDetailTbody.append("<tr><td>"+data.transferOrderItemDetails[i].SERIAL_NO+"</td><td>"+data.transferOrderItemDetails[i].ITEM_NAME+"</td><td>"+data.transferOrderItemDetails[i].VOLUME+"</td><td>"+data.transferOrderItemDetails[i].WEIGHT+"</td><td></td>"
			                       + "<td>"+data.transferOrderItemDetails[i].REMARK+"</td><td>"+data.transferOrderItemDetails[i].IS_DAMAGE+"</td><td>"+data.transferOrderItemDetails[i].ESTIMATE_DAMAGE_AMOUNT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REVENUE+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_PAYMENT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REMARK+"</td>"
			                       + "<td><button id='itemDetailEdit("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>编辑</button><button id='itemDetailDelete("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
		  	    	}
				}
			},'json'); 
    	}
	});
	  	    
	// 更新单品		  	    	
	$("#transferOrderItemDetailUpdateFormBtn").click(function(){
		$.post('/yh/transferOrderItemDetail/saveTransferOrderItemDetail', $("#transferOrderItemDetailUpdateForm").serialize(), function(data){
			if(data.ID > 0){
				// 模态框:修改单品明细
				$('#updateDetailModal').modal('hide');
				var item_id = $("#update_detail_transfer_order_item_id").val();
		  	    $.post('/yh/transferOrderItemDetail/getAllTransferOrderItemDetail', 'transfer_order_item_id='+item_id, function(data){
					var transferOrderItemDetailTbody = $("#transferOrderItemDetailTbody");
					transferOrderItemDetailTbody.empty();
					if(data.transferOrderItemDetails.length > 0 && data.contacts.length > 0){
						for(var i=0,j=0;i<data.transferOrderItemDetails.length,j<data.contacts.length;i++,j++){
				  	    	transferOrderItemDetailTbody.append("<tr><td>"+data.transferOrderItemDetails[i].SERIAL_NO+"</td><td>"+data.transferOrderItemDetails[i].ITEM_NAME+"</td><td>"+data.transferOrderItemDetails[i].VOLUME+"</td><td>"+data.transferOrderItemDetails[i].WEIGHT+"</td><td>"+data.contacts[j].CONTACT_PERSON+"<br/>"+data.contacts[j].PHONE+"<br/>"+data.contacts[j].ADDRESS+"<br/></td>"
				                       + "<td>"+data.transferOrderItemDetails[i].REMARK+"</td><td>"+data.transferOrderItemDetails[i].IS_DAMAGE+"</td><td>"+data.transferOrderItemDetails[i].ESTIMATE_DAMAGE_AMOUNT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REVENUE+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_PAYMENT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REMARK+"</td>"
				                       + "<td><button id='itemDetailEdit("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>编辑</button><button id='itemDetailDelete("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
			  	    	}
					}else{
						for(var i=0;i<data.transferOrderItemDetails.length;i++){
				  	    	transferOrderItemDetailTbody.append("<tr><td>"+data.transferOrderItemDetails[i].SERIAL_NO+"</td><td>"+data.transferOrderItemDetails[i].ITEM_NAME+"</td><td>"+data.transferOrderItemDetails[i].VOLUME+"</td><td>"+data.transferOrderItemDetails[i].WEIGHT+"</td><td></td>"
				                       + "<td>"+data.transferOrderItemDetails[i].REMARK+"</td><td>"+data.transferOrderItemDetails[i].IS_DAMAGE+"</td><td>"+data.transferOrderItemDetails[i].ESTIMATE_DAMAGE_AMOUNT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REVENUE+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_PAYMENT+"<br/>"+data.transferOrderItemDetails[i].DAMAGE_REMARK+"</td>"
				                       + "<td><button id='itemDetailEdit("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>编辑</button><button id='itemDetailDelete("+data.transferOrderItemDetails[i].ID+"|"+data.transferOrderItemDetails[i].ITEM_ID+"["+data.transferOrderItemDetails[i].ORDER_ID+"-"+data.transferOrderItemDetails[i].NOTIFY_PARTY_ID+"' type='submit' class='btn btn-default'>删除</button></td></tr>");
			  	    	}
					}
				},'json');
			}
		},'json');
	});
				                       
	// 运输里程碑
	$("#transferOrderMilestoneList").click(function(e){
		e.preventDefault();
    	// 切换到货品明细时,应先保存运输单
    	// 应先判断order_id是否为空
    	//提交前，校验数据
        if(!$("#transferOrderForm").valid()){
        	alert("请先保存运输单!");
	       	return false; 
        }
        
        if($("#order_id").val() == ""){
	    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#departureConfirmationBtn").attr("disabled", false);
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//alert("运输单保存成功!");
				  	$("#style").show();	
				  	
				  	var order_id = $("#order_id").val();
					$.post('/yh/transferOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
						var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
						transferOrderMilestoneTbody.empty();
						for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
						{
							transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
						}
					},'json');              
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }else{
        	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#departureConfirmationBtn").attr("disabled", false);
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	//alert("运输单保存成功!");
				  	$("#style").show();	
				  	
				  	var order_id = $("#order_id").val();
					$.post('/yh/transferOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
						var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
						transferOrderMilestoneTbody.empty();
						for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
						{
							transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
						}
					},'json');               
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }
    	
		var order_id = $("#order_id").val();
		$.post('/yh/transferOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.empty();
			for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
			{
				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
			}
		},'json');
	});
	
	// 保存新里程碑
	$("#transferOrderMilestoneFormBtn").click(function(){
		$('#transfer_milestone_order_id').val($('#order_id').val());
		$.post('/yh/transferOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$('#transferOrderMilestone').modal('hide');
		$('#transferOrderMilestoneList').click();
	});
	
	// 回单签收
	$("#receiptBtn").click(function(){
		var order_id = $("#order_id").val();
		$.post('/yh/transferOrderMilestone/receipt',{order_id:order_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
	});
	
	// 入库确认
	$("#warehousingConfirmBtn").click(function(){
		var order_id = $("#order_id").val();
		$.post('/yh/transferOrderMilestone/warehousingConfirm',{order_id:order_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
	});
	
	// 回显货品属性
	$("input[name='cargoNature']").each(function(){
		if($("#cargoNatureRadio").val() == $(this).val()){
			$(this).attr('checked', true);
		}
	});
	
	// 回显提货方式
	$("input[name='pickupMode']").each(function(){
		if($("#pickupModeRadio").val() == $(this).val()){
			$(this).attr('checked', true);
		}
	});
	
	// 回显到达方式
	$("input[name='arrivalMode']").each(function(){
		if($("#arrivalModeRadio").val() == $(this).val()){
			$(this).attr('checked', true);
			if($(this).val() == 'gateIn'){
				$("#contactInformation").hide();
			}
		}
	});
	
	// 
	$("#itemTable").on('click', '.dateilEdit', function(e){
		e.preventDefault();
		alert($(this).attr('href'));
		$("#transferOrderItemDateil").show();
	});
});