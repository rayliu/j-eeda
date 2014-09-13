
$(document).ready(function() {
	$('#menu_deliver').addClass('active').find('ul').addClass('in');

	$('#resetbutton').hide();
	$('#resetbutton2').hide();
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
				$("#spList").hide();
			});
			
			$('#spList').on('blur', function(){
		 		$('#spList').hide();
		 	});

			$('#spList').on('mousedown', function(){
				return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
			});

			// 选中供应商
			$('#spList').on('mousedown', '.fromLocationItem', function(e){
				$('#spMessage').val($(this).text());
				$('#sp_id').val($(this).attr('spid'));
				$('#cid').val($(this).attr('code'));
				$('#a1').html($(this).attr('contact_person'));
				$('#a2').html($(this).attr('company_name'));
				$('#a3').html($(this).attr('address'));
				$('#a4').html($(this).attr('mobile'));
				
		        $('#spList').hide();
		    }); 
			//datatable, 动态处理
			var sAjaxSource ="";
			var trandferOrderId = $("#tranferid").val();
			var localArr =$("#localArr").val();
			var localArr2 =$("#localArr2").val();
			var localArr3 =$("#localArr3").val();
			var aa =$("#transferstatus").val();
			
			if(localArr!=""){
				sAjaxSource ="/yh/delivery/orderList?localArr="+localArr+"&localArr2="+localArr2+"&localArr3="+localArr3+"&aa="+aa;
			}else{
				sAjaxSource ="/yh/delivery/orderList?localArr="+trandferOrderId+"&aa="+aa;
			}
			//var ser =  $("#ser_no").val();
			
			//,"bVisable":true
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
			
			
			$('#cargo-table').dataTable({
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "iDisplayLength": 10,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/yh/delivery/orderList2?localArr="+localArr+"&localArr2="+localArr2,
		        "aoColumns": [
					
		            {"mDataProp":"ITEM_NO"},  
		            {"mDataProp":"SERIAL_NO"},
		            {"mDataProp":"ITEM_NAME"},
		            {"mDataProp":"VOLUME"},
		            {"mDataProp":"WEIGHT"},
		            {"mDataProp":"ORDER_NO",
						 "sWidth": "12%",
							 },
					{"mDataProp":"CUSTOMER",
								 },
		        ]      
		    });	
			//添加配送单
			$("#saveBtn").click(function(e){
		        //阻止a 的默认响应行为，不需要跳转	
				//var itemId = $("#item_id").val();
				var spMessage = $("#spMessage").val();
				var mbProvinceTo = $("#mbProvinceTo").find("option:selected").text();
				var cmbCityTo = $("#cmbCityTo").find("option:selected").text();
				var cmbAreaTo = $("#cmbAreaTo").find("option:selected").text();
				if(spMessage == ""){
					alert("请输入供应商名称");
					return false;
				}
				if(mbProvinceTo == "--请选择省份--" || mbProvinceTo == ""){
					alert("请输入目的地省份");
					return false;
				}
				if(cmbCityTo == "--请选择城市--" || cmbCityTo == ""){
					alert("请输入目的地城市");
					return false;
				}
				if(cmbAreaTo == "--请选择区(县)--" || cmbAreaTo == ""){
					alert("请输入目的地区（县）");
					return false;
				}
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
				"bFilter": false, //不需要默认的搜索框
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
		  		          {"mDataProp":null},
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
		    	"bSort": false, // 不要排序
				"bFilter": false, //不需要默认的搜索框
			        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
			        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
			        //"sPaginationType": "bootstrap",
			        "iDisplayLength": 10,
			        "bServerSide": true,
			    	"oLanguage": {
			            "sUrl": "/eeda/dataTables.ch.txt"
			        },
			        "sAjaxSource": "/yh/delivery/searchTransferByATM",
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
			            {"mDataProp":"WAREHOUSE_NAME",
			            	"sClass": "warehouse"},
			            {"mDataProp":"COMPANY_NAME",
			            	"sClass": "cname"},
			            {"mDataProp":"ADDRESS"}
			        ]      
			    });	
			
			var cname = [];
			var warehouseArr = [];
		    $("#eeda-table4").on('click', '.checkedOrUnchecked', function(e){
		    	if(cname.length == 0 && warehouseArr.length == 0){
		    		$("#saveDelivery").attr('disabled', true);
		    	}
				if($(this).prop("checked") == true){
					$("#saveDelivery").attr('disabled', false);
					if(cname.length != 0){
						if(cname[0] != $(this).parent().siblings('.cname')[0].innerHTML && $(this).parent().siblings('.cname')[0].innerHTML != ''){
							alert("请选择同一客户!");
							return false;
						}else{
							if(warehouseArr.length != 0){
								if(warehouseArr[0] != $(this).parent().siblings('.warehouse')[0].innerHTML && $(this).parent().siblings('.warehouse')[0].innerHTML != ''){
									alert("请选择同一仓库!");
									return false;
								}else{
									cname.push($(this).parent().siblings('.cname')[0].innerHTML);
									warehouseArr.push($(this).parent().siblings('.warehouse')[0].innerHTML);
								}
							}else{
								if($(this).parent().siblings('.warehouse')[0].innerHTML != ''){
									warehouseArr.push($(this).parent().siblings('.warehouse')[0].innerHTML);
								}
							}
						}
					}else{
						if($(this).parent().siblings('.cname')[0].innerHTML != ''){
							cname.push($(this).parent().siblings('.cname')[0].innerHTML);
							warehouseArr.push($(this).parent().siblings('.warehouse')[0].innerHTML);
						}
					}
				}else{
					if(cname.length != 0){
						cname.splice($(this).parent().siblings('.cname')[0].innerHTML, 1);
					}
					if(warehouseArr.length != 0){
						warehouseArr.splice($(this).parent().siblings('.warehouse')[0].innerHTML, 1);
					}
				}
			});
			
		    $("#saveDelivery").click(function(e){
				 e.preventDefault();
			    	var trArr=[];
			    	var ser =[];
			    	var transferNo=[];
			    	var customer_idArr=[];
				$("#eeda-table4 tr:not(:first)").each(function(){
					var the=this;
		        	$("input:checked",this).each(function(){
		        		var cus_id=$(this).attr("code3");
		        		trArr.push($(this).val()); 
		        		//ser.push($("td:eq(1)",the).html());
		        		ser.push($(".serId",the).attr('code'));
		        		transferNo.push($(".transferNo",the).attr('code2'));
		        		if(cus_id!=""){
		        			customer_idArr.push(cus_id);
		        		}
		        		  $('#cusId').val(cus_id);
		        	});
		        	}); 
	        	/*if(customer_idArr.length>=2){
	         	   for(var i=0;i<customer_idArr.length;i++){
	         		   if(customer_idArr[i]!=customer_idArr[i+1]){
	         			   alert("请选择同客户的运输单！");
	         			   return;
	         		   }
	         		  if(i+2==customer_idArr.length){
	   				   break;
	         		  }
	         	   }
	            }*/
		        	$('#localArr2').val(ser);
		            $('#localArr').val(trArr);
		            $('#localArr3').val(transferNo);
		          
		            $('#createForm').submit();
			});
			$("#eeda-table3").on('click', '.checkedOrUnchecked', function(e){
				if($(this).prop("checked") == true){
					$("#saveDelivery").attr('disabled', false);
				}else{
					$("#saveDelivery").attr('disabled', true);
				}
			});
			//添加运输单序列号
			/* $("#eeda-table2").on('click', '.creat', function(e){
				 var id = $(this).attr('code');*/
				
		/*----------------------------------------------------------*/
			// 发车确认
				$("#ConfirmationBtn").click(function(){
					// 浏览器启动时,停到当前位置
					//debugger;
					$("#receiptBtn").attr("disabled", false); 
					var code = $("#warehouseCode").val();
					var locationTo = $("#locationTo").val();
					var delivery_id = $("#delivery_id").val();
					var priceType = $("input[name='priceType']:checked").val();
					$.post('/yh/deliveryOrderMilestone/departureConfirmation',{delivery_id:delivery_id,code:code,locationTo:locationTo,priceType:priceType},function(data){
						var MilestoneTbody = $("#transferOrderMilestoneTbody");
						MilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");

						paymenttable.fnSettings().sAjaxSource="/yh/deliveryOrderMilestone/accountPayable/"+delivery_id;
						paymenttable.fnDraw();
					},'json');
					$("#ConfirmationBtn").attr("disabled", true);
					$("#receiptBtn").attr("disabled", false);
					
					paymenttable().fnDraw();
					
				});
		/*-----------------------------------------------------------------*/
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
			      	dab.fnSettings().sAjaxSource = "/yh/delivery/searchTransferByATM?deliveryOrderNo="+deliveryOrderNo+"&customerName="+customerName+"&orderStatue="+orderStatue+"&warehouse="+warehouse;
			      	dab.fnDraw();
			      });
				
				
			 	//radio选择普通货品和ATM
				$("input[name=aabbcc]").change(function(){
					var cargo =$(this).val();
					console.log(cargo);
					if(cargo=="ATM"){
						$("#cargoNature").val("ATM");
						$("#cargos").show();
						$("#basic").hide();
					}else{//普通货品
						$("#cargoNature").val("cargo");
						$("#basic").show();
						$("#cargos").hide();
					}
				});
				
				$(function(){
					console.log(aa);
					if(aa!=''){
						$("#cargotable").show();
						$("#cargotable2").hide();
						$("#tranferdiv").show();
						
					}else{
						$("#cargotable").hide();
						$("#cargotable2").show();
						$("#tranferdiv").hide();
					}
				}) ;
				$(function(){
					if($("#deliverystatus").val()=='新建'){
						$("#ConfirmationBtn").attr("disabled", false);
					}
					if($("#deliverystatus").val()=='已发车'){
						$("#ConfirmationBtn").attr("disabled", true);
						$("#receiptBtn").attr("disabled", false);
					}
					if($("#deliverystatus").val()=='已签收'){
						$("#receiptBtn").attr("disabled", true);
					}
				}) ;
			
				/*var deliveryid =$("#delivery_id").val();
				//应收datatable
				var receipttable =$('#table_fin').dataTable({
					"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
			        "bFilter": false, //不需要默认的搜索框
			        //"sPaginationType": "bootstrap",
			        "iDisplayLength": 10,
			        "bServerSide": true,
			        "sAjaxSource":"/yh/deliveryOrderMilestone/accountReceivable/"+deliveryid,
			    	"oLanguage": {
			            "sUrl": "/eeda/dataTables.ch.txt"
			        },
			        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
						$(nRow).attr('id', aData.ID);
						return nRow;
					},
			        "aoColumns": [
						{"mDataProp":"NAME","sWidth": "80px","sClass": "name"},
						{"mDataProp":"AMOUNT","sWidth": "80px","sClass": "amount"},
						{"mDataProp":"TRANSFERORDERNO","sWidth": "80px","sClass": "amount"},  
						{"mDataProp":"REMARK","sWidth": "80px","sClass": "remark"},
						{"mDataProp":"STATUS","sWidth": "80px","sClass": "status"},
			        ]      
			    });*/
				
				
				//应付datatable
				var deliveryid =$("#delivery_id").val();
				var paymenttable=$('#table_fin2').dataTable({
					"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
			        "bFilter": false, //不需要默认的搜索框
			        //"sPaginationType": "bootstrap",
			        "iDisplayLength": 10,
			        "bServerSide": true,
			        "sAjaxSource": "/yh/deliveryOrderMilestone/accountPayable/"+deliveryid,
			    	"oLanguage": {
			            "sUrl": "/eeda/dataTables.ch.txt"
			        },
			        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
						$(nRow).attr('id', aData.ID);
						return nRow;
					},
			        "aoColumns": [
						{"mDataProp":"FIN_ITEM_NAME",
						    "fnRender": function(obj) {
						        if(obj.aData.FIN_ITEM_NAME!='' && obj.aData.FIN_ITEM_NAME != null){
						        	var str="";
						        	$("#paymentItemList").children().each(function(){
						        		if(obj.aData.FIN_ITEM_NAME == $(this).text()){
						        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
						        		}else{
						        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
						        		}
						        	});
						            return "<select name='fin_item_id'>"+str+"</select>";
						        }else{
						        	var str="";
						        	$("#paymentItemList").children().each(function(){
						        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
						        	});
						        	return "<select name='fin_item_id'>"+str+"</select>";
						        }
						 }},
						{"mDataProp":"AMOUNT",
						     "fnRender": function(obj) {
						         if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
						             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
						         }else{
						         	 return "<input type='text' name='amount'>";
						         }
						 }},  
						/*{"mDataProp":"FIN_ITEM_NAME","sWidth": "80px","sClass": "name"},
						{"mDataProp":"AMOUNT","sWidth": "80px","sClass": "amount"},*/
						{"mDataProp":"STATUS","sClass": "status"},
						{"mDataProp":"TRANSFERORDERNO","sClass": "amount", "bVisable":false},  
						{"mDataProp":"REMARK",
			                "fnRender": function(obj) {
			                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
			                        return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
			                    }else{
			                    	 return "<input type='text' name='remark'>";
			                    }
			            }},  
						/*{"mDataProp":"REMARK","sWidth": "80px","sClass": "remark"},*/
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
		
		/*//应收
		$("#item_fin_save").click(function(){
			var deliveryid =$("#delivery_id").val();
			$.post('/yh/deliveryOrderMilestone/receiptSave/'+deliveryid, $("#fin_form").serialize(), function(data){
				console.log(data);
				if(data.success){
					//receipttable.fnDraw();
					$('#fin_item').modal('hide');
					$('#resetbutton').click();
				}else{
					
				}
				
			});		
		});	*/
		//应付
		$("#addrow").click(function(){	
			var deliveryid =$("#delivery_id").val();
			$.post('/yh/deliveryOrderMilestone/addNewRow/'+deliveryid,function(data){
				console.log(data);
				if(data[0] != null){
					paymenttable.fnSettings().sAjaxSource = "/yh/deliveryOrderMilestone/accountPayable/"+deliveryid;
					paymenttable.fnDraw();
				}else{
					alert("请到基础模块维护应付条目！");
				}
			});		
		});	
		//应付修改
		$("#table_fin2").on('blur', 'input,select', function(e){
			e.preventDefault();
			var paymentId = $(this).parent().parent().attr("id");
			var name = $(this).attr("name");
			var value = $(this).val();
			$.post('/yh/deliveryOrderMilestone/updateDeliveryOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
				if(data.success){
				}else{
					alert("修改失败!");
				}
	    	},'json');
		});
		/*//应收
		$("#addrow2").click(function(){	
			var deliveryid =$("#delivery_id").val();
			$.post('/yh/deliveryOrderMilestone/addNewRow2/'+deliveryid,function(data){
				console.log(data);
				if(data.success){
					paymenttable.fnDraw();
					//$('#fin_item2').modal('hide');
					//$('#resetbutton2').click();
				}else{
					
				}
			});		
		});	*/
		
	    //获取全国省份
	    $(function(){
	     	var province = $("#mbProvinceTo");
	     	$.post('/yh/serviceProvider/province',function(data){
	     		province.append("<option>--请选择省份--</option>");
					var hideProvince = $("#hideProvinceTo").val();
	     		for(var i = 0; i < data.length; i++)
					{
						if(data[i].NAME == hideProvince){
	     				province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
	     				
	     				
						}else{
	     					province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
						}
					}
	     		
	     	},'json');
	    });
	    
	    //获取省份的城市
	    $('#mbProvinceTo').on('change', function(){
				var inputStr = $(this).val();
				$.get('/yh/serviceProvider/city', {id:inputStr}, function(data){
					var cmbCity =$("#cmbCityTo");
					cmbCity.empty();
					cmbCity.append("<option>--请选择城市--</option>");
					for(var i = 0; i < data.length; i++)
					{
						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
					toLocationList.show();
				},'json');
			});
	    
	    //获取城市的区县
	    $('#cmbCityTo').on('change', function(){
				var inputStr = $(this).val();
				var code = $("#locationTo").val(inputStr);
				$.get('/yh/serviceProvider/area', {id:inputStr}, function(data){
					var cmbArea =$("#cmbAreaTo");
					cmbArea.empty();
					cmbArea.append("<option>--请选择区(县)--</option>");
					for(var i = 0; i < data.length; i++)
					{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
					}
					toLocationList.show();
				},'json');
			});
	    
	    $('#cmbAreaTo').on('change', function(){
				var inputStr = $(this).val();
				var code = $("#locationTo").val(inputStr);
			});  

	    // 回显城市
	    var hideProvince = $("#hideProvinceTo").val();
	    $.get('/yh/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
				if(data.length > 0){
					var cmbCity =$("#cmbCityTo");
					cmbCity.empty();
					cmbCity.append("<option>--请选择城市--</option>");
					var hideCity = $("#hideCityTo").val();
					for(var i = 0; i < data.length; i++)
					{
						if(data[i].NAME == hideCity){
							cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
						}else{
							cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
						}
					}
				}
			},'json');

	    // 回显区
	    var hideCity = $("#hideCityTo").val();
	    $.get('/yh/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
				if(data.length > 0){
					var cmbArea =$("#cmbAreaTo");
					cmbArea.empty();
					cmbArea.append("<option>--请选择区(县)--</option>");
					var hideDistrict = $("#hideDistrictTo").val();
					for(var i = 0; i < data.length; i++)
					{
						if(data[i].NAME == hideDistrict){
							cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
						}else{
							cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
						}
					}
				}
			},'json');
	    

	 // 回显付款方式
		$("input[name='chargeType']").each(function(){
			if($("#chargeTypeRadio").val() == $(this).val()){
				$(this).attr('checked', true);		
			}
		});
});
