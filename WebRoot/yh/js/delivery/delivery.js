
$(document).ready(function() {
	$('#menu_deliver').addClass('active').find('ul').addClass('in');
	var hang="";
		  //获取供应商的list，选中信息在下方展示其他信息
			$('#spMessage').on('keyup click', function(){
				var inputStr = $('#spMessage').val();
				$.get('/yh/delivery/searchSp', {input:inputStr}, function(data){
					console.log(data);
					var spList =$("#spList");
					spList.empty();
					for(var i = 0; i < data.length; i++)
					{
						spList.append("<li><a tabindex='-1' class='fromLocationItem' code='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+"</a></li>");
					}
				},'json');
				$("#spList").css({ 
		        	left:$(this).position().left+"px", 
		        	top:$(this).position().top+32+"px" 
		        }); 
		        $('#spList').show();
			});
			$('#spMessage').on('blur', function(){
				$("#spList").delay(120).hide(1);
			});
			// 选中供应商
			$('#spList').on('click', '.fromLocationItem', function(e){
				$('#spMessage').val($(this).text());
				$('#sp_id').val($(this).attr('spid'));
				$('#cid').val($(this).attr('code'));
				$('#a1').html($(this).attr('contact_person'));
				$('#a2').html($(this).attr('company_name'));
				$('#a3').html($(this).attr('address'));
				$('#a4').html($(this).attr('phone'));
				
		        $('#spList').hide();
		    }); 
			//datatable, 动态处理
			var sAjaxSource ="";
			var trandferOrderId = $("#tranferid").val();
			var localArr =$("#localArr").val();
			var localArr2 =$("#localArr2").val();
			var localArr3 =$("#localArr3").val();
			if(localArr!=""){
				sAjaxSource ="/yh/delivery/orderList?localArr="+localArr+"&localArr2="+localArr2+"&localArr3="+localArr3;
			}else{
				sAjaxSource ="/yh/delivery/orderList?localArr="+trandferOrderId;
			}
			//var ser =  $("#ser_no").val();
			$('#eeda-table').dataTable({
		        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": sAjaxSource,
		        "aoColumns": [
					{"mDataProp":"ORDER_NO",
						 "sWidth": "12%",
							 },
					{"mDataProp":"CUSTOMER",
							 
								 },
		            {"mDataProp":"ITEM_NO"},  
		            {"mDataProp":"SERIAL_NO"},
		            {"mDataProp":"ITEM_NAME"},
		            {"mDataProp":"AMOUNT"},        	
		            {"mDataProp":"VOLUME"},
		            {"mDataProp":"WEIGHT"},
		        ]      
		    });	
			//添加配送单
			$("#saveBtn").click(function(e){
		            //阻止a 的默认响应行为，不需要跳转	
				 //var itemId = $("#item_id").val();
		            e.preventDefault();
		            //异步向后台提交数据
		           $.post('/yh/delivery/deliverySave',$("#deliveryForm").serialize(), function(data){
		                console.log(data);
	                    if(data.ID>0){
	                    	$("#delivery_id").val(data.ID);
	                    	$("#style").show();
	                    	$("#ConfirmationBtn").attr("disabled", false);
	                    	
	                    }else{
	                        alert('数据保存失败。');
	                    }
		             },'json');
		        });
			var dab2= $('#eeda-table2').dataTable({
		        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		        "bServerSide": true,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/yh/delivery/SearchTransfer",
		        "aoColumns": [   
		  		            {"mDataProp":"ORDER_NO"},
		  		            {"mDataProp":"STATUS"},
		  		            {"mDataProp":"CARGO_NATURE",
		  		            	"fnRender": function(obj) {
		  		            		if(obj.aData.CARGO_NATURE == "ATM"){
		  		            			return "ATM";
		  		            		}else{
		  		            			return "普通货品";
		  		            		}}
		  		            },        	
		  		            {"mDataProp":"PICKUP_MODE",
		  		            	"fnRender": function(obj) {
		  		            		if(obj.aData.PICKUP_MODE == "routeSP"){
		  		            			return "干线供应商自提";
		  		            		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
		  		            			return "外包供应商提货";
		  		            		}else{
		  		            			return "源鸿自提";
		  		            		}}},
		  		            { "mDataProp": "WAREHOUSE_NAME",},
		  		            {"mDataProp":"COMPANY_NAME"},
		  		            { 
		  		                "mDataProp": null, 
		  		                "sWidth": "8%",                
		  		                "fnRender": function(obj) {                    
		  		                    return "<a class='btn btn-info creat' code='"+obj.aData.ID+"' pcode='"+obj.aData.ORDER_NO+"'>"+
		  		                    		"<i class='fa fa-list fa-fw'> </i> "+
		  		                    		"创建"+
		  		                    		"</a>";
		  		                }
		  		            }                         
		  		        ]     
		    });	
			
			//异步创建配送单
			 $("#eeda-table2").on('click', '.creat', function(e){
				 var id = $(this).attr('code');
				  e.preventDefault();
		         //异步向后台提交数据
				var transferNo= ($(this).attr('pcode'));
				$.post('/yh/delivery/creat/'+id,function(id){
		                 //保存成功后，刷新列表
		                 console.log(id);
		                 if(id>0){
		                	 //dataTable2.fnSettings().sAjaxSource="/yh/delivery/orderList?trandferOrderId="+id;
		                	 window.location.href="/yh/delivery/creat2?id="+id+"&localArr="+transferNo;
		                 }else{
		                     alert('取消失败');
		                 }
		             },'text');
				  });
			
			  //deliveryOrderSearchTransfer ATM选择序列号
			var dab= $('#eeda-table4').dataTable({
			        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
			        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
			        //"sPaginationType": "bootstrap",
			        "iDisplayLength": 10,
			        "bServerSide": true,
			    	"oLanguage": {
			            "sUrl": "/eeda/dataTables.ch.txt"
			        },
			        "sAjaxSource": "/yh/delivery/SearchTransfer2",
			        "aoColumns": [
						{ "mDataProp": null,
						    "fnRender": function(obj) {
						       return '<input type="checkbox" class="checkedOrUnchecked" code3='+obj.aData.CUSTOMER_ID+' name="order_check_box" value="'+obj.aData.ID+'">';
						    }
						},          
			            {"mDataProp":null,
							"sWidth": "10%", 
							"fnRender": function(obj) {
							       return  "<a class='serId' style='color:#464D51;text-decoration:none;' code='"+obj.aData.TID+"'>"+obj.aData.SERIAL_NO+ "</a>";
							    }
			            	},
			            {"mDataProp":null,
			            		"sWidth": "10%", 
								"fnRender": function(obj) {
								       return  "<a class='transferNo' style='color:#464D51;text-decoration:none;' code2='"+obj.aData.ORDER_NO+"'>"+obj.aData.ORDER_NO+ "</a>";
								    }
			            		},
			            {"mDataProp":"STATUS"},        	
			            {"mDataProp":"CARGO_NATURE"},
			            {"mDataProp":"PICKUP_MODE",
			            	"fnRender": function(obj) {
			            		if(obj.aData.PICKUP_MODE == "routeSP"){
			            			return "干线供应商自提";
			            		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
			            			return "外包供应商提货";
			            		}else{
			            			return "源鸿自提";
			            		}}},
			            {"mDataProp":"WAREHOUSE_NAME"},
			            {"mDataProp":"COMPANY_NAME"}
			        ]      
			    });	
			$("#eeda-table4").on('click', '.checkedOrUnchecked', function(e){
				if($(this).prop("checked") == true){
					$("#saveDelivery").attr('disabled', false);
				}else{
					$("#saveDelivery").attr('disabled', true);
				}
			});
			//添加运输单序列号
			/* $("#eeda-table2").on('click', '.creat', function(e){
				 var id = $(this).attr('code');*/
				$("#saveDelivery").click(function(e){
					 e.preventDefault();
				    	var trArr=[];
				    	var ser =[];
				    	var transferNo=[];
				    	var customer_idArr=[];
					$("#eeda-table4 tr:not(:first)").each(function(){
						var the=this;
			        	$("input:checked",this).each(function(){
			        		var sp_id=$(this).attr("code3");
			        		trArr.push($(this).val()); 
			        		//ser.push($("td:eq(1)",the).html());
			        		ser.push($(".serId",the).attr('code'));
			        		transferNo.push($(".transferNo",the).attr('code2'));
			        		if(sp_id!=""){
			        			customer_idArr.push(sp_id);
			        		}
			        	});
			        	if(customer_idArr.length>=2){
			         	   for(var i=0;i<customer_idArr.length;i++){
			         		   if(customer_idArr[i]!=customer_idArr[i+1]){
			         			   alert("请选择同客户的运输单！");
			         			   return;
			         		   }
			         			  
			         	   }
			            }
			        	}); 
			        	$('#localArr2').val(ser);
			            $('#localArr').val(trArr);
			            $('#localArr3').val(transferNo);
			            $('#createForm').submit();
				});
			 
			// 发车确认
				$("#ConfirmationBtn").click(function(){
					// 浏览器启动时,停到当前位置
					//debugger;
					$("#receiptBtn").attr("disabled", false); 

					var delivery_id = $("#delivery_id").val();
					$.post('/yh/deliveryOrderMilestone/departureConfirmation',{delivery_id:delivery_id},function(data){
						var MilestoneTbody = $("#transferOrderMilestoneTbody");
						MilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
					},'json');
					$("#ConfirmationBtn").attr("disabled", true);
					$("#receiptBtn").attr("disabled", false);
					
				});
				
				// 运输里程碑
				$("#transferOrderMilestoneList").click(function(e){
					e.preventDefault();
			    	// 切换到货品明细时,应先保存运输单
			    	//提交前，校验数据
			        /*if(!$("#transferOrderForm").valid()){
			        	alert("请先保存运输单!");
				       	return false; 
			        }*/
			        
			        if($("#delivery_id").val() == ""){
				    	$.post('/yh/transferOrder/saveTransferOrder', $("#transferOrderForm").serialize(), function(transferOrder){
							$("#transfer_order_id").val(transferOrder.ID);
							$("#update_transfer_order_id").val(transferOrder.ID);
							$("#order_id").val(transferOrder.ID);
							$("#transfer_milestone_order_id").val(transferOrder.ID);
							$("#id").val(transferOrder.ID);
							if(transferOrder.ID>0){
								if(transferOrder.STATUS == '已发车'){
									$("#departureConfirmationBtn").attr("disabled", true);		
								}else{
									$("#departureConfirmationBtn").attr("disabled", false);
								}
								$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
							  	$("#style").show();	
							  	
							  	var order_id = $("#order_id").val();
								$.post('/yh/deliveryOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
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
					  	var delivery_id = $("#delivery_id").val(); 
					  	$("#transfer_milestone_delivery_id").val(delivery_id); 
						$.post('/yh/deliveryOrderMilestone/transferOrderMilestoneList',{delivery_id:delivery_id},function(data){
							var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
							transferOrderMilestoneTbody.empty();
							for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
							{
								transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
							}
						},'json');     
			        }
				});
			
				// 保存新里程碑
				$("#deliveryOrderMilestoneFormBtn").click(function(){
					$.post('/yh/deliveryOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
						var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
						transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
					},'json');
					$('#deliveryOrderMilestone').modal('hide');
				});
				
				
				// 回单签收
				$("#receiptBtn").click(function(){
					var delivery_id = $("#delivery_id").val();
					$.post('/yh/deliveryOrderMilestone/receipt',{delivery_id:delivery_id},function(data){
						var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
						transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
					},'json');
					$("#receiptBtn").attr("disabled", true);
				});
				
				 /*$(function(){
				 	var deliveryID = $('#delivery_id').val();
			 	    if(deliveryID==''){
			 	    	$("#receiptBtn").attr("disabled", true);
			 	     }else{
			 	    	$("#receiptBtn").attr("disabled", false);
			 	     }
			    }) ;*/
				
				$("#deliveryOrderNo1,#customerName1,#orderStatue1,#warehouse1").on('keyup click', function () {    	 	
			      	var deliveryOrderNo1 = $("#deliveryOrderNo1").val();
			      	var customerName1 = $("#customerName1").val();
			    	var orderStatue1 = $("#orderStatue1").val();
			      	var warehouse1 = $("#warehouse1").val();    	
			      	dab2.fnSettings().sAjaxSource = "/yh/delivery/SearchTransfer?deliveryOrderNo1="+deliveryOrderNo1+"&customerName1="+customerName1+"&orderStatue1="+orderStatue1+"&warehouse1="+warehouse1;
			      	dab2.fnDraw();
			      });
				$("#deliveryOrderNo2,#customerName2,#orderStatue2,#warehouse2").on('keyup click', function () {    	 	
			      	var deliveryOrderNo = $("#deliveryOrderNo2").val();
			      	var customerName = $("#customerName2").val();
			    	var orderStatue = $("#orderStatue2").val();
			      	var warehouse = $("#warehouse2").val();    	
			      	dab.fnSettings().sAjaxSource = "/yh/delivery/SearchTransfer2?deliveryOrderNo="+deliveryOrderNo+"&customerName="+customerName+"&orderStatue="+orderStatue+"&warehouse="+warehouse;
			      	dab.fnDraw();
			      });
				
				
			 	//radio选择普通货品和ATM
				$("input[name=aabbcc]").change(function(){
					var cargo =$(this).val();
					
					console.log(cargo);
					if(cargo=="ATM"){
						$("#cargos").show();
						$("#basic").hide();
					}else{
						$("#basic").show();
						$("#cargos").hide();
					}
					
				});
				
			    	
});
