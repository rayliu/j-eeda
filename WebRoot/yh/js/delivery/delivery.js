﻿
$(document).ready(function() {
	$('#menu_deliver').addClass('active').find('ul').addClass('in');
	var hang="";
		  //获取供应商的list，选中信息在下方展示其他信息
			$('#spMessage').on('keyup', function(){
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
			var trandferOrderId = $("#tranferid").val();
			var ser =  $("#ser_no").val();
			$('#eeda-table').dataTable({
		        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/yh/delivery/orderList?trandferOrderId="+trandferOrderId+"&ser="+ser,
		        "aoColumns": [
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
				 var itemId = $("#item_id").val();
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
			
			var dataTable2 =  $('#eeda-table2').dataTable({
		        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
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
		          /* { 
		                "mDataProp": null, 
		                "fnRender": function(obj) {
		                	var returnString ="";
		                	console.log(obj.aData.ID);
		                	$.ajax({  	
		                			type : "post",  
		                		  	url : "/yh/delivery/serialNo?id="+obj.aData.ID,  
		                		  	async : false,  
		                		  	success : function(data){  
		                		  		console.log(data);
		                		  		if(data.length!=0){
		                		  		returnString = "<select id='ser'>";
		                		  		for(var i = 0; i < data.length; i++)
		        						{
		                		  			if(data[i].SERIAL_NO==null||data.length==0){
		                		  				returnString ="" ;
		                		  			}else{
		                		  				returnString+="<option value="+data[i].SERIAL_NO+">"+data[i].SERIAL_NO+"</option>"
		                		  			}
		                		  		}
		                		  		returnString+="</select>";
		                		  		}
		                		    }  
		                	 });
		                	return returnString+"</select>";
		                }
		            }, */
		            { 
		                "mDataProp": null, 
		                "fnRender": function(obj) {
		                	var returnString ="";
		                	console.log(obj.aData.ID);
		                	$.ajax({  	
		                			type : "post",  
		                		  	url : "/yh/delivery/serialNo?id="+obj.aData.ID,  
		                		  	async : false,  
		                		  	success : function(data){  
		                		  		console.log(data);
		                		  		returnString+= "<input id='qy2' type='text' placeholder='请选择序列号' class='form-control'>";
	                		  			returnString+= "<ul id ='ulList' >";
		                		  		if(data.length!=0){
		                		  			for(var i = 0; i < data.length; i++)
			        						{
		                		  			
		                		  			returnString+="<li>"+"<a>"+data[i].SERIAL_NO+"</a>"+"</li>";
		                		  			
			        						}
		                		  		}
		                		  		returnString+="</ul>";
		                		    }  
		                	 });
		                	return returnString ;
		                }
		            },
		            { 
		                "mDataProp": null, 
		                "sWidth": "8%",                
		                "fnRender": function(obj) {                    
		                    return "<a class='btn btn-info creat' code='"+obj.aData.ID+"'>"+
		                    		"<i class='fa fa-list fa-fw'> </i> "+
		                    		"创建"+
		                    		"</a>";
		                }
		            }                         
		        ], "fnInitComplete": function(oSettings, json) {
		        	$("#eeda-table2 tr").on('click', '', function(){
		        	 hang = $(this).prevAll().length; 
		       		  	hang = Number(hang)+1;
		        	});
		        }    
		    });	
			$("#eeda-table2").on('click', '#qy2', function(){
				$("#ulList").show();
			});
			//异步创建配送单
			 $("#eeda-table2").on('click', '.creat', function(e){
				 var id = $(this).attr('code');
				 var ser ="";
				 $("#eeda-table2 tr:eq('"+hang+"')").each(function(){
		        	$("#ser",this).each(function(){
		        		ser =$(this).val();        	
		        	});          	
		        }); 
				  e.preventDefault();
		         //异步向后台提交数据
				$.post('/yh/delivery/creat/'+id,{ser:ser},function(id){
		                 //保存成功后，刷新列表
		                 console.log(id);
		                 if(id>0){
		                	// $("#tranferid").val(id);
		                	 //dataTable2.fnSettings().sAjaxSource="/yh/delivery/orderList?trandferOrderId="+id;
		                	 window.location.href="/yh/delivery/creat2?id="+id+ "&ser="+ser;
		                 }else{
		                     alert('取消失败');
		                 }
		             },'json');
				  });
			//datatable, 动态处理
		    var dataTable =$('#eeda-table3').dataTable({
		        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		        "bServerSide": true,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/yh/delivery/deliveryList",
		        "aoColumns": [   
		            
		            {"mDataProp":"ORDER_NO"},
		            {"mDataProp":"TRANSFER_ORDER_NO"},
		            {"mDataProp":"CUSTOMER"},        	
		            {"mDataProp":"C2"},
		            {"mDataProp":"CREATE_STAMP"},
		            {"mDataProp":"STATUS"},
		            { 
		                "mDataProp": null, 
		                "sWidth": "8%",                
		                "fnRender": function(obj) {                    
		                    return "<a class='btn btn-success edit' href='/yh/delivery/edit/"+obj.aData.ID+"'>"+
		                                "<i class='fa fa-edit fa-fw'></i>"+
		                                "查看"+
		                            "</a>"+
		                            "<a class='btn btn-danger cancelbutton' code='"+obj.aData.ID+"'>"+
		                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
		                                "取消"+
		                            "</a>";
		                }
		            }                         
		        ]      
		    });	
		    
			 $("#eeda-table3").on('click', '.cancelbutton', function(e){
				  e.preventDefault();
		         //异步向后台提交数据
				 var id = $(this).attr('code');
				$.post('/yh/delivery/cancel/'+id,function(data){
		                 //保存成功后，刷新列表
		                 console.log(data);
		                 if(data.success){
		                	 dataTable.fnDraw();
		                 }else{
		                     alert('取消失败');
		                 }
		             },'json');
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
				//条件筛选
				$("#order_no ,#tr_order_no ,#de_order_no,#stator,#status,#time_one,#time_two").on( 'keyup click', function () {    	 	
			      	var order_no = $("#order_no").val();
			      	var tr_order_no = $("#tr_order_no").val();
			    	var de_order_no = $("#de_order_no").val();
			      	var stator = $("#stator").val();    	
			      	var status = $("#status").val();
			      	var time_one = $("#time_one").val();
			      	var time_two = $("#time_two").val();
			      	if (status=="新建") {
			      		status ="new";
					}
					if (status=="确认") {
						status = "confirmed";
					}
					if (status=="取消") {
						status ="cancel";
					}
			      	dataTable.fnSettings().sAjaxSource = "/yh/returnorder/list?order_no="+order_no+"&tr_order_no="+tr_order_no+"&de_order_no="+de_order_no+"&stator="+stator+"&status="+status+"&time_one="+time_one+"&time_two="+time_two;
			      	dataTable.fnDraw();
			      });
			 	    	 
			    	
});
