$(document).ready(function() {
	$('#menu_warehouse').addClass('active').find('ul').addClass('in');
	$('#reset').hide();
	$('#warehouseSelect').val();
	var inventory=$("#inventory").val();
	var source = "";
	var source2 = "";
	if(inventory=='gateIn'){
		$("#btn1").show();
		source = "/yh/gateIn/gateInlist";
		source2 = "/yh/gateIn/gateInEdit/";
	}if(inventory=='gateOut'){
		$("#btn2").show();
		source = "/yh/gateOut/gateOutlist";
		source2 = "/yh/gateOut/gateOutEdit/";
	}
	//入库单list
	 $('#example').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
		 	"iDisplayLength": 10,
	        "bServerSide": true,
	        "bRetrieve": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource":source,
			"aoColumns": [
				{ "mDataProp": "ORDER_NO",
					"fnRender": function(obj) {
            			return "<a href='"+source2+""+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
            		}},
				{ "mDataProp": "COMPANY_NAME" },
				{ "mDataProp": "WAREHOUSE_NAME" },
				{ "mDataProp": "ORDER_TYPE" },
				{ "mDataProp": "STATUS" },
	            { "mDataProp": "QUALIFIER" },
	            { 
	                "mDataProp": null,
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-danger' href='/yh/gateIn/gateInDelect/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                            "</a>";
	                }
	            } 
	            ]
	} );
	 var wid = $("#warehouseorderId").val();
	 //入库产品list
	 console.log(wid);
	 productDataTable2 = $('#itemTable').dataTable({
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "bServerSide": true,
	        "bRetrieve": true,
	        "sAjaxSource":"/yh/gateIn/gateInProductlist/"+wid,
	        "aoColumns": [
	            {"mDataProp":"ITEM_NAME"},
	            {"mDataProp":"ITEM_NO"},        	
	            {"mDataProp":"EXPIRE_DATE"},
	            {"mDataProp":"LOT_NO"},
	            {"mDataProp":"CATON_NO"},
	            {"mDataProp":"TOTAL_QUANTITY"},
	            {"mDataProp":"UOM"},
	            {"mDataProp":"UNIT_PRICE"},
	            {"mDataProp":"UNIT_COST"},
	            { 
	                "mDataProp": null, 
	                "sWidth": "100px",                
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-success editProduct' code='"+obj.aData.ID+"'>"+
				                    "<i class='fa fa-edit fa-fw'></i>"+
				                "</a>"+
			                    "<a class='btn btn-danger deleteProduct' code='"+obj.aData.ID+"'>"+
			                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                            "</a>";
	                }
	            }      
	        ],      
	    });
	//gateInProduct编辑
	 $("#itemTable").on('click', '.editProduct', function(){
		 var id = $(this).attr('code');
		 $.post('/yh/gateIn/gateInProductEdit/'+id,function(data){
             //保存成功后，刷新列表
             console.log(data);
             if(data!=null){
            	 $("#productId").val(data.PRODUCT_ID);
            	 	$("#warehouseOrderItemId").val(data.ID);
            	 	$("#itemNameMessage").val(data.ITEM_NAME);
         			$("#itemNoMessage").val(data.ITEM_NO);
         			$("#item_desc").val(data.ITEM_DESC);
         			$("#expire_date").val(data.EXPIRE_DATE);
         			$("#lot_no").val(data.LOT_NO); 			
         			$("#total_quantity").val(data.TOTAL_QUANTITY);
         			$("#unit_price").val(data.UNIT_PRICE);
         			$("#unit_cost").val(data.UNIT_COST);
         			$("#uom").val(data.UOM);
         			$("#caton_no").val(data.CATON_NO);
         			$('#myModal').modal('show');
             }else{
                 alert('取消失败');
             }
         },'json');
		});
	//gateInProduct删除
	 $("#itemTable").on('click', '.deleteProduct', function(){
		 var id = $(this).attr('code');
		 $.post('/yh/gateIn/gateInProductDelect/'+id,function(data){
             //保存成功后，刷新列表
             console.log(data);
             if(data.success){
            	 productDataTable2.fnDraw();
             }else{
                 alert('取消失败');
             }
         },'json');
		});
	 //出库单list
	 
	 //选择客户 
	 $('#customerMessage').on('keyup click', function(){
			var inputStr = $('#customerMessage').val();
			$.get('/yh/gateIn/searchCustomer', {input:inputStr}, function(data){
				console.log(data);
				var customerList =$("#customerList");
				customerList.empty();
				for(var i = 0; i < data.length; i++)
				{
					customerList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].PID+"'>"+data[i].COMPANY_NAME+"</a></li>");
				}
			},'json');
			$("#customerList").css({ 
	        	left:$(this).position().left+"px", 
	        	top:$(this).position().top+32+"px" 
	        }); 
	        $('#customerList').show();
		});
	 	$('#customerMessage').on('blur', function(){
			$("#customerList").hide();
		});
		// 选中客户
		$('#customerList').on('mousedown', '.fromLocationItem', function(e){
			var partyId =$(this).attr('code');
			$('#customerMessage').val($(this).text());
			$('#party_id').val(partyId);
			//productDataTable.fnSettings().sAjaxSource = "/yh/gateIn/gateInProductlist?categoryId="+partyId;
			//productDataTable.fnDraw();
			$('#customerList').hide();
			
			$.post('/yh/gateIn/confirmproduct/'+partyId,function(data){
                console.log(data);
             if(data.success){
            	 
             }else{
                 alert('客户无产品！');
             }
             },'json');
	    }); 
		
		/*var trArr=[];
		$("#productId").on('click', function(e){
			 e.preventDefault();
			$("#itemTable2 tr:not(:first)").each(function(){
	        	$("input:checked",this).each(function(){
	        		trArr.push($(this).val()); 
	        	});
	        	}); 
			console.log(trArr);
			$.post('/yh/gateIn/gateInProductlist2?localArr='+trArr,function(data){
	               //保存成功后，刷新列表
	               console.log(data);
	               if(data.success){
	            	   productDataTable2.fnDraw();
	               }else{
	                   alert('取消失败');
	               }
	           },'text');*/
			 	//$('#localArr').val(trArr);
	            //$('#createForm').submit();
		//});
		
		//获取货品的名称list，选中信息在下方展示其他信息
		$('#itemNameMessage').on('keyup click', function(){
			var inputStr = $('#itemNameMessage').val();
			var customerId = $('#party_id').val();
			$.get('/yh/gateOut/searchItemName', {input:inputStr,customerId:customerId}, function(data){
				console.log(data);
				var itemNameList =$("#itemNameList");
				itemNameList.empty();
				for(var i = 0; i < data.length; i++)
				{
					var item_name = data[i].ITEM_NAME;
					if(item_name == null){
						item_name = '';
					}
					itemNameList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' item_desc='"+data[i].ITEM_DESC+"' item_no='"+data[i].ITEM_NO+"' expire_date='"+data[i].EXPIRE_DATE+"' lot_no='"+data[i].LOT_NO+"' total_quantity='"+data[i].TOTAL_QUANTITY+"' unit_price='"+data[i].UNIT_PRICE+"' unit_cost='"+data[i].UNIT_COST+"' uom='"+data[i].UOM+"', caton_no='"+data[i].CATON_NO+"', >"+data[i].ITEM_NAME+"</a></li>");
				}
			},'json');		
			$("#itemNameList").css({ 
				left:$(this).position().left+"px", 
				top:$(this).position().top+32+"px" 
			}); 
			$('#itemNameList').show();        
		});
		$('#itemNameMessage').on('blur', function(){
		$("#itemNameList").hide();
	});
	// 选中产品名
		$('#itemNameList').on('mousedown', '.fromLocationItem', function(e){
			$("#itemNameMessage").val($(this).text());
			if($(this).attr('item_no') == 'null'){
				$("#item_no").val('');
			}else{
				$("#itemNoMessage").val($(this).attr('item_no'));
			}
			
			$("#productId").val($(this).attr('id'));
			$('#itemNameList').hide();
		});  	
		
		//获取货品的序列号list，选中信息在下方展示其他信息
		$('#itemNoMessage').on('keyup click', function(){
			var inputStr = $('#itemNoMessage').val();
			var customerId = $('#party_id').val();
			$.get('/yh/gateOut/searchItemNo', {input:inputStr,customerId:customerId}, function(data){
				console.log(data);
				var itemNoList =$("#itemNoList");
				itemNoList.empty();
				for(var i = 0; i < data.length; i++)
				{
					var item_no = data[i].ITEM_NO;
					if(item_no == null){
						item_no = '';
					}
					itemNoList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' item_desc='"+data[i].ITEM_DESC+"' item_name='"+data[i].ITEM_NAME+"' expire_date='"+data[i].EXPIRE_DATE+"' lot_no='"+data[i].LOT_NO+"' total_quantity='"+data[i].TOTAL_QUANTITY+"' unit_price='"+data[i].UNIT_PRICE+"' unit_cost='"+data[i].UNIT_COST+"' uom='"+data[i].UOM+"', caton_no='"+data[i].CATON_NO+"', >"+data[i].ITEM_NO+"</a></li>");
				}
			},'json');		
	     $("#itemNoList").css({ 
	     	left:$(this).position().left+"px", 
	     	top:$(this).position().top+32+"px" 
	     }); 
	     $('#itemNoList').show();        
		});
		$('#itemNoMessage').on('blur', function(){
		$("#itemNoList").hide();
	});
	// 选中序列号
		$('#itemNoList').on('mousedown', '.fromLocationItem', function(e){
			$("#itemNoMessage").val($(this).text());
			if($(this).attr('item_name') == 'null'){
				$("#item_name").val('');
			}else{
				$("#itemNameMessage").val($(this).attr('item_name'));
			}
	 			
			$("#productId").val($(this).attr('id'));
	    $('#itemNoList').hide();
	 }); 
	 	
	 	
	 	//选择仓库 
		$.get('/yh/gateIn/searchAllwarehouse', function(data){
			console.log(data);
			if(data.length > 0){
			var warehouseSelect = $("#warehouseSelect");
			warehouseSelect.empty();
			
			var warehouseId = $("#warehouseId").val();
			warehouseSelect.append("<option>---请选择仓库---</option>");
			for(var i = 0; i < data.length; i++)
			{
				 if(data[i].ID == warehouseId){
					 warehouseSelect.append("<option class='fromLocationItem' value='"+data[i].ID+"' selected='selected' >"+data[i].WAREHOUSE_NAME+"</option>");
				 }else{
					 warehouseSelect.append("<option class='fromLocationItem' value='"+data[i].ID+"' >"+data[i].WAREHOUSE_NAME+"</option>");
				 }
			}
			}
		},'json');
		$('#warehouseSelect').on('change', function(e){
			 $("#warehouseId").val($("#warehouseSelect").val());
	    });
		 /*$('#warehouseSelect').on('keyup click', function(){
				var inputStr = $('#warehouseSelect').val();
				$.get('/yh/gateIn/searchAllwarehouse', {input:inputStr}, function(data){
					console.log(data);
					var warehouseList =$("#warehouseList");
					warehouseList.empty();
					for(var i = 0; i < data.length; i++)
					{
						warehouseList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</a></li>");
					}
				},'json');
				$("#warehouseList").css({ 
		        	left:$(this).position().left+"px", 
		        	top:$(this).position().top+32+"px" 
		        }); 
		        $('#warehouseList').show();
			});
		 	$('#warehouseSelect').on('blur', function(){
				$("#warehouseList").delay(120).hide(1);
			});*/
		 	
			// 选中仓库
			/*$('#warehouseList').on('click', '.fromLocationItem', function(e){
				var id =$(this).attr('code');
				$('#warehouseSelect').val($(this).text());
				 $("#warehouseId").val(id);
				//productDataTable.fnSettings().sAjaxSource = "/yh/gateIn/gateInProductlist?categoryId="+partyId;
				//productDataTable.fnDraw();
				$('#customerList').hide();
		    }); */
			
		 //保存入库单
		 $("#saveInventoryBtn").click(function(e){
	            //阻止a 的默认响应行为，不需要跳转	
			 //var itemId = $("#item_id").val();
	            e.preventDefault();
	            //异步向后台提交数据
	           $.post('/yh/gateIn/gateInSave',$("#inventoryForm").serialize(), function(data){
	                console.log(data);
                 if(data>0){
                	 console.log(data);
                 	$("#warehouseorderId").val(data);
                 	$("#style").show();
                 	//$("#ConfirmationBtn").attr("disabled", false);
                 }else{
                     alert('数据保存失败。');
                 }
	             },'json');
	        });
		 
			// 保存货品
		 $("#addproduct").click(function(){
			 $('#reset').click();
		 });
		    $("#warehouseOrderItemFormBtn").click(function(){
		    	console.log(warehouseorderid);
		    	var warehouseorderid = $("#warehouseorderId").val();
		    	$.post('/yh/gateIn/savewareOrderItem/'+warehouseorderid,$("#warehouseOrderItemForm").serialize(), function(id){
						//保存成功后，刷新列表
		                console.log(id);
		                if(id>0){
		                	$('#myModal').modal('hide');
		                	var warehouseorderid = $("#warehouseorderId").val();
		                	productDataTable2.fnSettings().sAjaxSource = "/yh/gateIn/gateInProductlist/"+warehouseorderid;
		                	productDataTable2.fnDraw();
		                	$('#reset').click();
		                }else{
		                    alert('客户无产品,保存失败！。');
		                }
				},'json');
		    });
		    //入仓确认
		    $("#ConfirmBtn").click(function(){
		    	var warehouseorderid = $("#warehouseorderId").val();
		    	$.post('/yh/gateIn/gateInConfirm/'+warehouseorderid,function(data){
		    		 if(data.success){
		    			 $("#ConfirmBtn").attr("disabled", true);
		    			 alert("入库成功！");
		                }else{
		                    alert('客户无产品,保存失败！。');
		                }
		    	},'json');
		   });
		   var wStatus = $("#warehouseorderStatus").val();
		   console.log(wStatus);
		   if(wStatus=='已入库'){
			   $("#ConfirmBtn").attr("disabled", true); 
		   }
		   
		   $("#transferOrderItemList").click(function(e){
		    	if($("#warehouseorderId").val() == ''){
			    	e.preventDefault();
			    	// 切换到货品明细时,应先保存运输单
			    	//提交前，校验数据
			        	alert("请先保存入库单!");
				       	return false; 
			        
		    	}
		   });
});