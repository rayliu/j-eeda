$(document).ready(function() {
	$('#menu_warehouse').addClass('active').find('ul').addClass('in');
	$('#reset').hide();
	$('#warehouseSelect').val();
	var is_true=false;
	var inventory=$("#inventory").val();
	var source = "";
	var source2 = "";
	if(inventory=='gateIn'){
		$("#btn1").show();
		source = "/gateIn/gateInlist";
		source2 = "/gateIn/gateInEdit";
		is_true = gateIn.isUpdate || gateIn.isComplete;
	}if(inventory=='gateOut'){
		$("#btn2").show();
		source = "/gateOut/gateOutlist";
		source2 = "/gateOut/gateOutEdit";
		is_true = gateOut.isUpdate || gateOut.isComplete;
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
						if(is_true){
							return "<a href='"+source2+"?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
						}else{
							return obj.aData.ORDER_NO;
						}
            			
            		}},
				{ "mDataProp": "COMPANY_NAME" },
				{ "mDataProp": "WAREHOUSE_NAME" },
				{ "mDataProp": "ORDER_TYPE" },
				{ "mDataProp": "STATUS" },
	            { "mDataProp": "QUALIFIER" },
	            { 
	                "mDataProp": null,
	                "bVisible":false,
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-danger' href='/gateIn/gateInDelect/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                            "</a>";
	                }
	            } 
	            ]
	} );
	 var wid = $("#warehouseorderId").val();
	 //入库产品list
	 productDataTable2 = $('#itemTable').dataTable({
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "bServerSide": true,
	        "bRetrieve": true,
	        "sAjaxSource":"/gateIn/gateInProductlist/"+wid,
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
		 $.post('/gateIn/gateInProductEdit/'+id,function(data){
             //保存成功后，刷新列表
             if(data!=null){
            	 //console.log(data.PRODUCT_ID);
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
		 $.post('/gateIn/gateInProductDelect/'+id,function(data){
             //保存成功后，刷新列表
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
			$.get('/gateIn/searchCustomer', {input:inputStr}, function(data){
				
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
	 	$('#customerList').on('blur', function(){
			$('#customerList').hide();
		});

	 	$('#customerList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	 	});
		// 选中客户
		$('#customerList').on('mousedown', '.fromLocationItem', function(e){
			var partyId =$(this).attr('code');
			$('#customerMessage').val($(this).text());
			$('#party_id').val(partyId);
			//productDataTable.fnSettings().sAjaxSource = "/gateIn/gateInProductlist?categoryId="+partyId;
			//productDataTable.fnDraw();
			$('#customerList').hide();
			
			$.post('/gateIn/confirmproduct/'+partyId,function(data){
                
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
			$.post('/gateIn/gateInProductlist2?localArr='+trArr,function(data){
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
			$.get('/gateOut/searchItemName', {input:inputStr,customerId:customerId}, function(data){
				var itemNameList =$("#itemNameList");
				itemNameList.empty();
				for(var i = 0; i < data.length; i++)
				{
					
					if(data[i].ITEM_NO!=null){
					var item_name = data[i].ITEM_NAME;
					if(item_name == null||item_name==''){
						item_name =  data[i].ITEM_NO;
					}
					}
					var item_desc=data[i].ITEM_DESC;
					if(item_desc==null){
						item_desc='';
					}
	                var item_no = data[i].ITEM_NO;
	                if(item_no==null){
	                	item_no='';
					}
	                var expire_date = data[i].EXPIRE_DATE;
	                if(expire_date == null){
	                    expire_date = '';
	                }
	                var lot_no = data[i].LOT_NO;
	                if(lot_no == null){
	                    lot_no = '';
	                }
	                var item_no = data[i].ITEM_NO;
	                if(item_no == null){
	                    item_no = '';
	                }
	                var unit_price = data[i].UNIT_PRICE;
	                if(unit_price == null){
	                    unit_price = '';
	                }
	                var unit_cost = data[i].UNIT_COST;
	                if(unit_cost == null){
	                    unit_cost = '';
	                }
	                var caton_no = data[i].CATON_NO;
	                if(caton_no == null){
	                    caton_no = '';
	                }
	                
	                //console.log(data[i].PRODUCT_ID);
					itemNameList.append("<li><a tabindex='-1' class='fromLocationItem' productId='"+data[i].ID+"' id='"+data[i].ID+"' item_desc='"+item_desc+"' item_no='"+item_no+"' expire_date='"+expire_date+"' lot_no='"+lot_no+"' total_quantity='"+data[i].TOTAL_QUANTITY+"' unit_price='"+unit_price+"' unit_cost='"+unit_cost+"' uom='"+data[i].UOM+"', caton_no='"+caton_no+"', >"+item_name+"</a></li>");
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
		$('#itemNameList').on('blur', function(){
			$('#itemNameList').hide();
		});

	 	$('#itemNameList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	 	});
	// 选中产品名
		$('#itemNameList').on('mousedown', '.fromLocationItem', function(e){
			
			$("#itemNameMessage").val($(this).text());
			if($(this).attr('item_no') == 'null'){
				$("#item_no").val('');
			}else{
				$("#itemNoMessage").val($(this).attr('item_no'));
			}
				if($(this).attr('item_desc') == 'undefined' || $(this).attr('item_desc') == "null"){
					$("#item_desc").val('');
				}else{
					$("#item_desc").val($(this).attr('item_desc'));
				}
				if($(this).attr('expire_date') == "null"){
					$("#expire_date").val('');
				}else{
					$("#expire_date").val($(this).attr('expire_date'));
				}
					
					$("#lot_no").val($(this).attr('lot_no')); 	
					$("#unit_price").val($(this).attr('unit_price'));
					$("#unit_cost").val($(this).attr('unit_cost'));
					$("#uom").val($(this).attr('uom'));
					$("#caton_no").val($(this).attr('caton_no'));
					$("#productId").val($(this).attr('productId'));
					$("#total").val($(this).attr('total_quantity'));
					$("#mylabel").text("(0~"+$(this).attr('total_quantity')+")");
			$('#itemNameList').hide();
		});  	
		
		//获取货品的序列号list，选中信息在下方展示其他信息
		$('#itemNoMessage').on('keyup click', function(){
			var inputStr = $('#itemNoMessage').val();
			var customerId = $('#party_id').val();
			$.get('/gateOut/searchItemNo', {input:inputStr,customerId:customerId}, function(data){
				
				var itemNoList =$("#itemNoList");
				itemNoList.empty();
				for(var i = 0; i < data.length; i++)
				{
					
					var item_no = data[i].ITEM_NO;
					if(item_no == null){
						item_no = '';
					}
	                var item_name = data[i].ITEM_NAME;
	                if(item_name == null){
	                    item_name = '';
	                }
	                //需要对元素尽心是否为空的判断
	                var item_desc = data[i].ITEM_DESC;
	                if(item_desc == null){
	                    item_desc = '';
	                }
	                var expire_date = data[i].EXPIRE_DATE;
	                if(expire_date == null){
	                    expire_date = '';
	                }
	                var lot_no = data[i].LOT_NO;
	                if(lot_no == null){
	                    lot_no = '';
	                }
	                var unit_price = data[i].UNIT_PRICE;
	                if(unit_price == null){
	                    unit_price = '';
	                }
	                var unit_cost = data[i].UNIT_COST;
	                if(unit_cost == null){
	                    unit_cost = '';
	                }
	                var caton_no = data[i].CATON_NO;
	                if(caton_no == null){
	                    caton_no = '';
	                }
	                var total_quantity=data[i].TOTAL_QUANTITY;
	                if(total_quantity==null){
	                	total_quantity='';
	                }
	                var uom=data[i].UOM;
	                if(uom==null){
	                	uom='';
	                }
					itemNoList.append("<li><a tabindex='-1' class='fromLocationItem' productId='"+data[i].ID+" ' id='"+data[i].ID+"' item_desc='"+item_desc+"' item_name='"+item_name+"' expire_date='"+expire_date+"' lot_no='"+lot_no+"' total_quantity='"+total_quantity+"' unit_price='"+unit_price+"' unit_cost='"+unit_cost+"' uom='"+uom+"', caton_no='"+caton_no+"', >"+item_no+"</a></li>");
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
		$('#itemNoList').on('blur', function(){
			$('#itemNoList').hide();
		});

		$.post('/gateOut/alluom',function(data){
			var uom=$('#uom');
			uom.empty();
			uom.append("<option>请选择</option>");
			for(var i=0; i<data.length; i++){
				var name=data[i].NAME;
				uom.append("<option value='"+name+"'>"+name+"</option>");
			}
		},'json');
	 	$('#itemNoList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	 	});
	// 选中序列号
		$('#itemNoList').on('mousedown', '.fromLocationItem', function(e){
			$("#itemNoMessage").val($(this).text());
			if($(this).attr('item_name') == 'null'){
				$("#item_name").val('');
			}else{
				$("#itemNameMessage").val($(this).attr('item_name'));
			}
			if($(this).attr('item_desc') == 'undefined' || $(this).attr('item_desc') == "null"){
				$("#item_desc").val('');
			}else{
				$("#item_desc").val($(this).attr('item_desc'));
			}
			if($(this).attr('expire_date') == "null"){
				$("#expire_date").val('');
			}else{
				$("#expire_date").val($(this).attr('expire_date'));
			}
	 			$("#lot_no").val($(this).attr('lot_no')); 			
	 			//$("#total_quantity").val($(this).attr('total_quantity'));
	 			$("#unit_price").val($(this).attr('unit_price'));
	 			$("#unit_cost").val($(this).attr('unit_cost'));
	 			$("#uom").val($(this).attr('uom'));
	 			$("#caton_no").val($(this).attr('caton_no'));
	 			$("#productId").val($(this).attr('productId'));
	 			$("#total").val($(this).attr('total_quantity'));
	 			$("#mylabel").text("(0~"+$(this).attr('total_quantity')+")");
	    $('#itemNoList').hide();
	 }); 
	 	
	 	
	 	//选择仓库 
		$.get('/gateIn/searchAllwarehouse', function(data){
			
			if(data.length > 0){
			var warehouseSelect = $("#warehouseSelect");
			warehouseSelect.empty();
			
			var warehouseId = $("#warehouseId").val();
			warehouseSelect.append("<option class='form-control'>---请选择仓库---</option>");
			for(var i = 0; i < data.length; i++)
			{
				 if(data[i].ID == warehouseId){
					 warehouseSelect.append("<option class='form-control' value='"+data[i].ID+"' selected='selected' >"+data[i].WAREHOUSE_NAME+"</option>");
				 }else{
					 warehouseSelect.append("<option class='form-control' value='"+data[i].ID+"' >"+data[i].WAREHOUSE_NAME+"</option>");
				 }
			}
			}
		},'json');
		$('#warehouseSelect').on('change', function(e){
			 $("#warehouseId").val($("#warehouseSelect").val());
	    });
		 /*$('#warehouseSelect').on('keyup click', function(){
				var inputStr = $('#warehouseSelect').val();
				$.get('/gateIn/searchAllwarehouse', {input:inputStr}, function(data){
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
				//productDataTable.fnSettings().sAjaxSource = "/gateIn/gateInProductlist?categoryId="+partyId;
				//productDataTable.fnDraw();
				$('#customerList').hide();
		    }); */
			
		


		
		var parentId = "chargeCheckOrderbasic";
		$("#chargeCheckOrderbasic").click(function(e){
			e.preventDefault();
			parentId = e.target.getAttribute("id");
		});
		
		var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
		    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
		    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
		    '</div>';
		$('body').append(alerMsg);
	
		$('#message_trigger_err').on('click', function(e) {
			e.preventDefault();
		});
		
		//运输里程碑
		$("#transferOrderMilestoneList").click(function(e){
			e.preventDefault();
			if("chargeCheckOrderbasic" == parentId){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			}
			parentId = e.target.getAttribute("id");
		});
		$("#transferOrderarap").click(function(e){
			e.preventDefault();
			if("chargeCheckOrderbasic" == parentId){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);	
			}
			parentId = e.target.getAttribute("id");
		});
		 //保存入库单
		 $("#saveInventoryBtn").click(function(e){
	            //阻止a 的默认响应行为，不需要跳转	
			 //var itemId = $("#item_id").val();
	            e.preventDefault();
	            //异步向后台提交数据
	           $.post('/gateIn/gateInSave',$("#inventoryForm").serialize(), function(data){
	                
                 if(data>0){
                	 
                 	$("#warehouseorderId").val(data);
                 	//$("#style").show();
                 	$("#ConfirmBtn").attr("disabled", false);
                 	
                 	//有问题
                 	console.log("55");
                 	contactUrl("gateInEdit?id",data);
                 	
                 	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                 	}else{
                     alert('数据保存失败。');
                 }
	             },'json');
	        });
		 
			// 保存货品
		 $("#addproduct").click(function(){
			 $("#itemNameMessagespan").hide();
			 $("#itemNoMessagespan").hide();
			 $("#total_quantityspan").hide();
			 $("#unit_pricespan").hide();
			 $("#unit_costspan").hide();
			 $('#reset').click();
		 });
		    $("#warehouseOrderItemFormBtn").click(function(e){
		    	var itemNameMessage =$("#itemNameMessage").val();
		    	var itemNoMessage =$("#itemNoMessage").val();
		    	var total_quantity =$("#total_quantity").val();
		    	var unit_price = $("#unit_price").val();
		    	var warehouseorderid = $("#warehouseorderId").val();
		    	var unit_cost =$("#unit_cost").val();
		    	if(itemNameMessage==""||itemNoMessage==""||total_quantity==""||unit_price==""||unit_cost==""){
		    		if(itemNameMessage==""){
		    			$("#itemNameMessagespan").show();
		    		}
		    		else{
		    			$("#itemNameMessagespan").hide();
		    		}
		    		if(itemNoMessage==""){
		    			$("#itemNoMessagespan").show();
		    		}
		    		else{
		    			$("#itemNoMessagespan").hide();
		    		}
		    		if(total_quantity==""){
		    			$("#total_quantityspan").show();
		    		}
		    		else{
		    			$("#total_quantityspan").hide();
		    		}
		    		if(unit_price==""){
		    			$("#unit_pricespan").show();
		    		}
		    		else{
		    			$("#unit_pricespan").hide();
		    		}
		    		if(unit_cost==""){
		    			$("#unit_costspan").show();
		    		}
		    		else{
		    			$("#unit_costspan").hide();
		    		}
		    	}
		    	else{
		    	$.post('/gateIn/savewareOrderItem/'+warehouseorderid,$("#warehouseOrderItemForm").serialize(), function(id){
						//保存成功后，刷新列表
		                if(id>0){
		                	$('#myModal').modal('hide');
		                	var warehouseorderid = $("#warehouseorderId").val();
		                	productDataTable2.fnSettings().sAjaxSource = "/gateIn/gateInProductlist/"+warehouseorderid;
		                	productDataTable2.fnDraw();
		                	$('#reset').click();
		                	e.preventDefault();
		        			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
		                }else{
		                    alert('客户无产品,保存失败！。');
		                }
				},'json');
		    	}
		    });
		    
		    //入仓确认
		    $("#ConfirmBtn").click(function(){
		    	var warehouseorderid = $("#warehouseorderId").val();
		    	$.post('/gateIn/gateInConfirm/'+warehouseorderid,function(data){
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
		   if(wStatus=='已入库'||wStatus==''){
			   $("#ConfirmBtn").attr("disabled", true); 
		   }
		   
		   $("#transferOrderItemList").click(function(e){
			   $("#saveInventoryBtn").click();
			   
/*		    	if($("#warehouseorderId").val() == ''){
			    	e.preventDefault();
			    	
			    	// 切换到货品明细时,应先保存运输单
			    	//提交前，校验数据
			    	alert("请先保存出库单!");
				       	return false; 
			        
		    	}
		    	
*/		  	if("chargeCheckOrderbasic" == parentId){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			}
			parentId = e.target.getAttribute("id");  
		});
});