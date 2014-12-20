
$(document).ready(function() {
	$('#menu_deliver').addClass('active').find('ul').addClass('in');

	$('#resetbutton').hide();
	$('#resetbutton2').hide();
	
	var deliveryStatus=$("#deliveryOrder_status").text();
	if(deliveryStatus=="已签收"||deliveryStatus=="已发车"){
		$("#saveBtn").attr("disabled",true);
	}else{
		$("#saveBtn").attr("disabled",false);
	}

	var hang="";
		  /*// 获取供应商的list，选中信息在下方展示其他信息
			$('#spMessage').on('keyup click', function(){
				var inputStr = $('#spMessage').val();
				$.get('/delivery/searchSp', {input:inputStr}, function(data){
					console.log(data);
					var spList =$("#spList");
					spList.empty();
					for(var i = 0; i < data.length; i++)
					{
						var abbr = data[i].ABBR;
						if(abbr == null){
							abbr = '';
						}
						var company_name = data[i].COMPANY_NAME;
						if(company_name == null){
							company_name = '';
						}
						var contact_person = data[i].CONTACT_PERSON;
						if(contact_person == null){
							contact_person = '';
						}
						var phone = data[i].PHONE;
						if(phone == null){
							phone = '';
						}
						spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
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
				return false;// 阻止事件回流，不触发 $('#spMessage').on('blur'
			});*/
	$('#spMessage').on('keyup click', function(){
		var inputStr = $('#spMessage').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/delivery/searchSp', {input:inputStr}, function(data){
			console.log(data);
			var spList =$("#spList");
			spList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var abbr = data[i].ABBR;
				if(abbr == null){
					abbr = '';
				}
				var company_name = data[i].COMPANY_NAME;
				if(company_name == null){
					company_name = '';
				}
				var contact_person = data[i].CONTACT_PERSON;
				if(contact_person == null){
					contact_person = '';
				}
				var phone = data[i].PHONE;
				if(phone == null){
					phone = '';
				}
				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');

		$("#spList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#spList').show();
	});

	// 没选中供应商，焦点离开，隐藏列表
	$('#spMessage').on('blur', function(){
 		$('#spList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
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
			// datatable, 动态处理
			var sAjaxSource ="";
			var trandferOrderId = $("#tranferid").val();
			var localArr =$("#localArr").val();
			var localArr2 =$("#localArr2").val();
			var localArr3 =$("#localArr3").val();
			var aa =$("#transferstatus").val();
			
			if(localArr!=""){
				sAjaxSource ="/delivery/orderList?localArr="+localArr+"&localArr2="+localArr2+"&localArr3="+localArr3+"&aa="+aa;
			}else{
				sAjaxSource ="/delivery/orderList?localArr="+trandferOrderId+"&aa="+aa;
			}
			// var ser = $("#ser_no").val();
			
			// ,"bVisable":true
			$('#eeda-table').dataTable({
		        // "sDom":
				// "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12
				// center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        // "sPaginationType": "bootstrap",
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
				"bFilter": false, //不需要默认的搜索框
				"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "iDisplayLength": 10,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/delivery/orderList2?localArr="+localArr+"&localArr2="+localArr2,
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
			
			var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
			    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
			    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
			    '</div>';
			$('body').append(alerMsg);

			$('#message_trigger_err').on('click', function(e) {
				e.preventDefault();
			});
			
			 var saveDelivery = function(){
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
					/*if(cmbAreaTo == "--请选择区(县)--" || cmbAreaTo == ""){
						alert("请输入目的地区（县）");
						return false;
					}*/
					$("#saveBtn").attr("disabled", true);
		            // 异步向后台提交数据
		            $.post('/delivery/deliverySave',$("#deliveryForm").serialize(), function(data){
		                console.log(data);
	                    if(data.ID>0){
	                    	$("#delivery_id").val(data.ID);
	                    	// $("#style").show();
	                    	$("#ConfirmationBtn").attr("disabled", false);
	                    	$("#order_no").text(data.ORDER_NO);
	                    	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	                    }else{
	                        alert('数据保存失败。');
	                    }
	                    $("#saveBtn").attr("disabled", false);
		             },'json');
		            
			 };
			// 添加配送单
			$("#saveBtn").click(function(e){
		        // 阻止a 的默认响应行为，不需要跳转
				// var itemId = $("#item_id").val();
				e.preventDefault();
				saveDelivery();
	        });
			var dab2= $('#eeda-table2').dataTable({
				"bFilter": false, // 不需要默认的搜索框
		        // "sDom":
				// "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12
				// center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        // "sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		        "bServerSide": true,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/delivery/searchTransfer",
		        "aoColumns": [ 
		        			{ "mDataProp": null,
							    "fnRender": function(obj) {
							       return '<input type="checkbox" class="checkedOrUnchecked" code3='+obj.aData.CUSTOMER_ID+' name="order_check_box" value="'+obj.aData.ID+'">';
							    }
							},   
		  		            {"mDataProp":"ITEM_NO"},
		  		            {"mDataProp":"ITEM_NAME"},
		  		            {"mDataProp":"WAREHOUSE_NAME"},
		  		            {"mDataProp":"WAREHOUSE_ID", "bVisible": false},
		  		            {"mDataProp":"ABBR"},
		  		            {"mDataProp":"CUSTOMER_ID", "bVisible": false},
		  		            {"mDataProp":"TOTAL_QUANTITY"},
		  		            { 
		  		                "mDataProp": null, 
		  		                "sWidth": "8%",                
		  		                "fnRender": function(obj) {                    
		  		                    return "<input type='text' name='amount' disabled value='0'>";
		  		                }
		  		            }
		  		        ]     
		    });	
			
			// 异步创建配送单
			 $("#eeda-table2").on('click', '.creat', function(e){
				 var id = $(this).attr('code');
				  e.preventDefault();
		         // 异步向后台提交数据
				var transferNo= ($(this).attr('pcode'));
				$.post('/delivery/creat/'+id,function(id){
		                 // 保存成功后，刷新列表
		                 console.log(id);
		                 if(id>0){
		                	 // dataTable2.fnSettings().sAjaxSource="/delivery/orderList?trandferOrderId="+id;
		                	 window.location.href="/delivery/creat2?id="+id+"&localArr="+transferNo;
		                 }else{
		                     alert('取消失败');
		                 }
		             },'text');
			 });
			
			  // deliveryOrderSearchTransfer ATM选择序列号
			var dab= $('#eeda-table4').dataTable({
		    	"bSort": false, // 不要排序
				"bFilter": false, // 不需要默认的搜索框
			        // "sDom":
					// "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12
					// center'p>>",
			        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
			        // "sPaginationType": "bootstrap",
			        "iDisplayLength": 10,
			        "bServerSide": true,
			    	"oLanguage": {
			            "sUrl": "/eeda/dataTables.ch.txt"
			        },
			        "sAjaxSource": "/delivery/searchTransferByATM",
			        "aoColumns": [
						{ "mDataProp": null,
						    "fnRender": function(obj) {
						       return '<input type="checkbox" class="checkedOrUnchecked" code3='+obj.aData.CUSTOMER_ID+' name="order_check_box" value="'+obj.aData.ID+'">';
						    }
						},          
			            {"mDataProp":null,
							"sWidth": "10%", 
							"fnRender": function(obj) {
									if(obj.aData.SERIAL_NO==null){
										return "<a class='serId' style='color:#464D51;text-decoration:none;' code='"+obj.aData.TID+"'></a>";
									}else{
										 return  "<a class='serId' style='color:#464D51;text-decoration:none;' code='"+obj.aData.TID+"'>"+obj.aData.SERIAL_NO+ "</a>";
									}
							      
							    }
			            	},
			            {"mDataProp":"ITEM_NO"},
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
			            {"mDataProp":"ABBR",
			            	"sClass": "cname"},
			            {"mDataProp":"COMPANY"}
			        ]      
			    });	
			


			var cname = [];
			var warehouseArr = [];		    
			// 构造已选的行数据
			var buildItems=function(objCheckBox, cargoNature){
				// 判断当前是选中还是去除
				var row=$(objCheckBox).parent().parent();
				var $inputAmount=row.find('input[name=amount]');
				if($(objCheckBox).prop("checked") == true){					
					if(cargoNature==="ATM")
						$("#saveDelivery").attr('disabled', false);
					if(cargoNature==="cargo")
						$("#saveDeliveryCargo").attr('disabled', false);

					
					$inputAmount.attr('disabled', false);// 允许输入配送数量

					
				}else{
					$inputAmount.attr('disabled', true);// 不允许输入配送数量
					$inputAmount.val('0');// 清零
				}

				// TODO: 需要优化，没时间搞。
				if(cname.length == 0 && warehouseArr.length == 0){
		    		$("#saveDelivery").attr('disabled', true);
		    	}

		    	var $checkBox=$(objCheckBox);
				if($checkBox.prop("checked") == true){
					$("#saveDelivery").attr('disabled', false);
					if(cname.length != 0){
						if(cname[0] != $checkBox.parent().siblings('.cname')[0].innerHTML && $checkBox.parent().siblings('.cname')[0].innerHTML != ''){
							alert("请选择同一客户!");
							
							$checkBox.attr("checked",false);
							
							
		
							return false;
						}else{
							if(warehouseArr.length != 0){
								if(warehouseArr[0] != $checkBox.parent().siblings('.warehouse')[0].innerHTML && $checkBox.parent().siblings('.warehouse')[0].innerHTML != ''){
									alert("请选择同一仓库!");
									
									$checkBox.attr("checked",false);
									
									

									return false;
								}else{
									cname.push($checkBox.parent().siblings('.cname')[0].innerHTML);
									warehouseArr.push($checkBox.parent().siblings('.warehouse')[0].innerHTML);
								}
							}else{
								if($checkBox.parent().siblings('.warehouse')[0].innerHTML != ''){
									warehouseArr.push($checkBox.parent().siblings('.warehouse')[0].innerHTML);
								}
							}
						}
					}else{
						if($checkBox.parent().siblings('.cname')[0].innerHTML != ''){
							cname.push($checkBox.parent().siblings('.cname')[0].innerHTML);
							warehouseArr.push($checkBox.parent().siblings('.warehouse')[0].innerHTML);
						}
					}
				}else{
					if(cname.length != 0){
						cname.splice($checkBox.parent().siblings('.cname')[0].innerHTML, 1);
					}
					if(warehouseArr.length != 0){
						warehouseArr.splice($checkBox.parent().siblings('.warehouse')[0].innerHTML, 1);
					}
				}
			};
			
			$("#eeda-table4").on('click', function(){
				console.log("click table");
			});
			
			$("#eeda-table4").on('click', '.checkedOrUnchecked', function(){
				buildItems(this, "ATM");
			});
			
			$("#eeda-table2").on('click', '.checkedOrUnchecked', function(){
				buildItems(this, "cargo");
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
		        		// ser.push($("td:eq(1)",the).html());
		        		ser.push($(".serId",the).attr('code'));
		        		transferNo.push($(".transferNo",the).attr('code2'));
		        		if(cus_id!=""){
		        			customer_idArr.push(cus_id);
		        		}
		        		  $('#cusId').val(cus_id);
		        	});
		        	}); 
	        	/*
				 * if(customer_idArr.length>=2){ for(var i=0;i<customer_idArr.length;i++){
				 * if(customer_idArr[i]!=customer_idArr[i+1]){
				 * alert("请选择同客户的运输单！"); return; }
				 * if(i+2==customer_idArr.length){ break; } } }
				 */
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
			// 添加运输单序列号
			/*
			 * $("#eeda-table2").on('click', '.creat', function(e){ var id =
			 * $(this).attr('code');
			 */
				

			// 发车确认
				$("#ConfirmationBtn").click(function(){
					// 浏览器启动时,停到当前位置
					// debugger;
					$("#receiptBtn").attr("disabled", false); 
					var code = $("#warehouseCode").val();
					var locationTo = $("#locationTo").val();
					var delivery_id = $("#delivery_id").val();
					var priceType = $("input[name='priceType']:checked").val();
					$.post('/deliveryOrderMilestone/departureConfirmation',{delivery_id:delivery_id,code:code,locationTo:locationTo,priceType:priceType},function(data){
						var MilestoneTbody = $("#transferOrderMilestoneTbody");
						MilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");

						paymenttable.fnSettings().sAjaxSource="/deliveryOrderMilestone/accountPayable/"+delivery_id;
						paymenttable.fnDraw();
					},'json');
					$("#ConfirmationBtn").attr("disabled", true);
					//$("#receiptBtn").attr("disabled", false);
					$("#saveBtn").attr("disabled", true);
					paymenttable().fnDraw();
					
				});
				//应付
				$("#arapTab").click(function(e){
					e.preventDefault();
					/*if(!$("#transferOrderForm").valid()){
			        	alert("请先保存运输单!");
				       	return false; 
		        	}*/
					
					parentId = e.target.getAttribute("id");
				});
				
				
				// 增加仓库查询代码和客户查询代码
				$('#customerName2').on('keyup click', function(){
			           var inputStr = $('#customerName2').val();
  
			           $.get("/customerContract/search", {locationName:inputStr}, function(data){
			               console.log(data);
			               var companyList =$("#companyList");
			               companyList.empty();
			               for(var i = 0; i < data.length; i++)
			               {
			                   companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
			               }
			               if(data.length>0)
			                   companyList.show();
			               
			           },'json');
			          
			    });
			   // 选中某个客户时候
			      $('#companyList').on('click', '.fromLocationItem', function(e){        
			           $('#customerName2').val($(this).text());
			           $("#companyList").hide();
			           var companyId = $(this).attr('partyId');
			           $('#customerId').val(companyId);
			           var inputStr = $('#customerName2').val();
			           var warehouseName =$("#warehouse2").val();
			           var code= $("#orderStatue2").val();
			           var deliveryOrderNo = $("#deliveryOrderNo2").val();
			           var rdc = $('#hiddenRdc').val();
			           //如果客户和仓库都有值，触发查询
			           if(warehouseName!=null&&inputStr!=null&&warehouseName!=""&&inputStr!=""&&rdc!=null&&rdc!=""){
				            dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?customerName="+inputStr+"&warehouse="
				        	+warehouseName+"&code="+code+"&deliveryOrderNo="+deliveryOrderNo;
				        	dab.fnDraw();
			           }
			           
			       });
			      // 没选中客户，焦点离开，隐藏列表
			      $('#customerName2').on('blur', function(){
			           $('#companyList').hide();
			       });

			       // 当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
			       $('#companyList').on('blur', function(){
			           $('#companyList').hide();
			       });

			       $('#companyList').on('mousedown', function(){
			           return false;// 阻止事件回流，不触发 $('#spMessage').on('blur'
			       });
			       
			       //获取客户
			       $('#customerName1').on('keyup click', function(){
			           var inputStr = $('#customerName1').val();
  
			           $.get("/customerContract/search", {locationName:inputStr}, function(data){
			               console.log(data);
			               var companyList =$("#companyList1");
			               companyList.empty();
			               for(var i = 0; i < data.length; i++)
			               {
			                   companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
			               }
			               if(data.length>0)
			                   companyList.show();
			               
			           },'json');
			          
			    });
			   // 选中某个客户时候
			      $('#companyList1').on('click', '.fromLocationItem', function(e){        
			           $('#customerName1').val($(this).text());
			           $("#companyList1").hide();
			           var companyId = $(this).attr('partyId');
			           $('#customerId').val(companyId);
			           	var customerName1 = $("#customerName1").val();
				      	var warehouse1 = $("#warehouse1").val();
				      	if(customerName1!=null&&warehouse1!=null&&customerName1!=""&&warehouse1!=""){
				      		dab2.fnSettings().sAjaxSource = "/delivery/searchTransfer?customerName1="+customerName1+"&warehouse1="+warehouse1;
					      	dab2.fnDraw();
				      	}else{
				      		dab2.fnSettings().sAjaxSource = "/delivery/searchTransfer";
					      	dab2.fnDraw();
				      	}
			       });
			      // 没选中客户，焦点离开，隐藏列表
			      $('#customerName1').on('blur', function(){
			           $('#companyList1').hide();
			       });

			       // 当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
			       $('#companyList1').on('blur', function(){
			           $('#companyList1').hide();
			       });

			       $('#companyList1').on('mousedown', function(){
			           return false;// 阻止事件回流，不触发 $('#spMessage').on('blur'
			       });
			     //选择仓库 
				  	 $('#warehouse1').on('keyup click', function(){
				  		var warehouse_Name =$("#warehouse1").val();
				  		$.get('/gateIn/searchAllwarehouse',{warehouseName:warehouse_Name}, function(data){
				  			console.log(data);
				  			var warehouseList =$("#warehouseList1");
				  			warehouseList.empty();
				  			for(var i = 0; i < data.length; i++)
				  			{
				  				warehouseList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</a></li>");
				  			}
				  		},'json');
				  		$("#warehouseList1").css({ 
				  	    	left:$(this).position().left+"px", 
				  	    	top:$(this).position().top+32+"px" 
				  	    }); 
				  	    $('#warehouseList1').show();
				  	    
				  	});
				  	$('#warehouse1').on('blur', function(){
				  		$("#warehouseList1").hide();
				  	});
				  	$('#warehouseList1').on('blur', function(){
				  			$('#warehouseList1').hide();
				  		});

				  	$('#warehouseList1').on('mousedown', function(){
				  		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
				  	});
				  	$('#warehouseList1').on('mousedown', '.fromLocationItem', function(e){
				  		
				  		$('#warehouse1').val($(this).text());
				  		
				  		var customerName1 = $("#customerName1").val();
				      	var warehouse1 = $("#warehouse1").val();
				      	if(customerName1!=null&&warehouse1!=null&&customerName1!=""&&warehouse1!=""){
				      		dab2.fnSettings().sAjaxSource = "/delivery/searchTransfer?customerName1="+customerName1+"&warehouse1="+warehouse1;
					      	dab2.fnDraw();
				      	}else{
				      		dab2.fnSettings().sAjaxSource = "/delivery/searchTransfer";
					      	dab2.fnDraw();
				      	}
				      	$('#warehouseList1').hide();
				  	});
			     //选择仓库 
			  	 $('#warehouse2').on('keyup click', function(){
			  		var warehouse_Name =$("#warehouse2").val();
			  		var rdc = $('#hiddenRdc').val();
			  		if(rdc != null && rdc != ""){
				  		$.get('/delivery/searchAllwarehouse',{warehouseName:warehouse_Name,rdc:rdc}, function(data){
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
			  		}
			  	    
			  	});
			  	$('#warehouse2').on('blur', function(){
			  		$("#warehouseList").hide();
			  	});
			  	$('#warehouseList').on('blur', function(){
			  			$('#warehouseList').hide();
			  		});

			  	$('#warehouseList').on('mousedown', function(){
			  		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
			  	});
			  	$('#warehouseList').on('mousedown', '.fromLocationItem', function(e){
			  		//var id =$(this).attr('code');
			  		$('#warehouse2').val($(this).text());
			  		var inputStr = $('#customerName2').val();
		            var warehouseName =$("#warehouse2").val();
		            var code= $("#orderStatue2").val();
		            var deliveryOrderNo = $("#deliveryOrderNo2").val();
		            var rdc = $('#hiddenRdc').val();
		           //如果客户和仓库都有值，触发查询
		            if(warehouseName!=null&&inputStr!=null&&warehouseName!=""&&inputStr!=""&&rdc!=null&&rdc!=""){
		            	dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?customerName="+inputStr+"&warehouse="
		        		+warehouseName+"&code="+code+"&deliveryOrderNo="+deliveryOrderNo;
		        		dab.fnDraw();
		            }
	        		  
			  		$('#warehouseList').hide();
			  	});
			  	
			  	//选择RDC
			  	$('#rdc').on('keyup click', function(){
			  		var rdc =$("#rdc").val();
			  		if(rdc == "")
						$("#hiddenRdc").val("");
			  		$.get('/delivery/searchAllRDC',{rdc:rdc}, function(data){
			  			console.log(data);
			  			var warehouseList =$("#rdcList");
			  			warehouseList.empty();
			  			for(var i = 0; i < data.length; i++)
			  			{
			  				warehouseList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</a></li>");
			  			}
			  		},'json');
			  		$("#rdcList").css({ 
			  	    	left:$(this).position().left+"px", 
			  	    	top:$(this).position().top+32+"px" 
			  	    }); 
			  	    $('#rdcList').show();
			  	    
			  	});
			  	$('#rdc').on('blur', function(){
			  		$("#rdcList").hide();
			  	});
			  	$('#rdcList').on('blur', function(){
			  			$('#rdcList').hide();
			  		});

			  	$('#rdcList').on('mousedown', function(){
			  		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
			  	});
			  	$('#rdcList').on('mousedown', '.fromLocationItem', function(e){
			  		var id =$(this).attr('code');
			  		$('#rdc').val($(this).text());
			  		$('#hiddenRdc').val(id);
			  		$('#rdcList').hide();
			  	});
			  	
			  	/***red,? 客户和仓库一有值得时候触发事件****/
			  	$('#customerName2,#warehouse2,#orderStatue2,#deliveryOrderNo2').on('keyup click', function(){
			  		var inputStr = $('#customerName2').val();
		            var warehouseName =$("#warehouse2").val();
		            var code= $("#orderStatue2").val();
		            var deliveryOrderNo = $("#deliveryOrderNo2").val();
		           //如果客户和仓库都有值，触发查询
		            if(warehouseName!=null&&inputStr!=null&&warehouseName!=""&&inputStr!=""){
		            	dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?customerName="+inputStr+"&warehouse="
		        		+warehouseName+"&code="+code+"&deliveryOrderNo="+deliveryOrderNo;
		        		dab.fnDraw();
		            }else{
		            	dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?"
		        		+"code="+code+"&deliveryOrderNo="+deliveryOrderNo;
		        		dab.fnDraw();
		            }
	        		           
			    });
			  	
			  	
				// 应收
				$("#arap").click(function(e){
					e.preventDefault();
					/*
					 * if(!$("#transferOrderForm").valid()){ alert("请先保存运输单!");
					 * return false; }
					 */
					
					parentId = e.target.getAttribute("id");
				});
				// 货品明细
				$("#departOrderItemList").click(function(e){
					e.preventDefault();
					/*
					 * if(!$("#transferOrderForm").valid()){ alert("请先保存运输单!");
					 * return false; }
					 */
					parentId = e.target.getAttribute("id");
				});
				var parentId="chargeCheckOrderbasic";
				// 基本信息
				$("#chargeCheckOrderbasic").click(function(e){
					parentId = e.target.getAttribute("id");
				});
				// 运输里程碑
				$("#transferOrderMilestoneList").click(function(e){
					e.preventDefault();
			    	// 切换到货品明细时,应先保存运输单
			    	// 提交前，校验数据
			        /*
					 * if(!$("#transferOrderForm").valid()){ alert("请先保存运输单!");
					 * return false; }
					 */
			        
			        if($("#delivery_id").val() == ""){
				    	$.post('/transferOrder/saveTransferOrder', $("#transferOrderForm").serialize(), function(transferOrder){
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
							  	// $("#style").show();
							  	
							  	var order_id = $("#order_id").val();
								$.post('/deliveryOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
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
						$.post('/deliveryOrderMilestone/transferOrderMilestoneList',{delivery_id:delivery_id},function(data){
							var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
							transferOrderMilestoneTbody.empty();
							for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
							{
								transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
							}
						},'json'); 
						
			        }
			        parentId = e.target.getAttribute("id");
				});
			
				// 保存新里程碑
				$("#deliveryOrderMilestoneFormBtn").click(function(){
					$.post('/deliveryOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
						var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
						transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
					},'json');
					$('#deliveryOrderMilestone').modal('hide');
				});
				
				// 回单签收
				$("#receiptBtn").click(function(){
					var delivery_id = $("#delivery_id").val();
					$.post('/deliveryOrderMilestone/receipt',{delivery_id:delivery_id},function(data){
						var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
						transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
					},'json');
					$("#receiptBtn").attr("disabled", true);
				});
				
				 /*
					 * $(function(){ var deliveryID = $('#delivery_id').val();
					 * if(deliveryID==''){ $("#receiptBtn").attr("disabled",
					 * true); }else{ $("#receiptBtn").attr("disabled", false); } }) ;
					 */
				
				$("#deliveryOrderNo1,#customerName1,#orderStatue1,#warehouse1").on('keyup click', function () {
			      	var customerName1 = $("#customerName1").val();
			      	var warehouse1 = $("#warehouse1").val();
			      	if(customerName1!=null&&warehouse1!=null&&customerName1!=""&&warehouse1!=""){
			      		dab2.fnSettings().sAjaxSource = "/delivery/searchTransfer?customerName1="+customerName1+"&warehouse1="+warehouse1;
				      	dab2.fnDraw();
			      	}else{
			      		dab2.fnSettings().sAjaxSource = "/delivery/searchTransfer";
				      	dab2.fnDraw();
			      	}
			      });
				
			 	// radio选择普通货品和ATM
				$("input[name=aabbcc]").change(function(){
					var cargo =$(this).val();
					console.log(cargo);
					if(cargo=="ATM"){
						$("#cargoNature").val("ATM");
						$("#cargos").show();
						$("#basic").hide();
					}else{// 普通货品
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
			
				/*
				 * var deliveryid =$("#delivery_id").val(); //应收datatable var
				 * receipttable =$('#table_fin').dataTable({ "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12
				 * center'p>>", "bFilter": false, //不需要默认的搜索框
				 * //"sPaginationType": "bootstrap", "iDisplayLength": 10,
				 * "bServerSide": true,
				 * "sAjaxSource":"/deliveryOrderMilestone/accountReceivable/"+deliveryid,
				 * "oLanguage": { "sUrl": "/eeda/dataTables.ch.txt" },
				 * "fnRowCallback": function( nRow, aData, iDisplayIndex,
				 * iDisplayIndexFull ) { $(nRow).attr('id', aData.ID); return
				 * nRow; }, "aoColumns": [ {"mDataProp":"NAME","sWidth":
				 * "80px","sClass": "name"}, {"mDataProp":"AMOUNT","sWidth":
				 * "80px","sClass": "amount"},
				 * {"mDataProp":"TRANSFERORDERNO","sWidth": "80px","sClass":
				 * "amount"}, {"mDataProp":"REMARK","sWidth": "80px","sClass":
				 * "remark"}, {"mDataProp":"STATUS","sWidth": "80px","sClass":
				 * "status"}, ] });
				 */
				
				
				// 应付datatable
				var deliveryid =$("#delivery_id").val();
				var paymenttable=$('#table_fin2').dataTable({
					"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
			        "bFilter": false, // 不需要默认的搜索框
			        // "sPaginationType": "bootstrap",
			        "iDisplayLength": 10,
			        "bServerSide": true,
			        "bLengthChange":false,
			        "sAjaxSource": "/deliveryOrderMilestone/accountPayable/"+deliveryid,
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
						        	if(obj.aData.CREATE_NAME == 'system'){
						        		return obj.aData.FIN_ITEM_NAME;
						        	}else{
						        		return "<select name='fin_item_id'>"+str+"</select>";
						        	}
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
						/*
						 * {"mDataProp":"FIN_ITEM_NAME","sWidth":
						 * "80px","sClass": "name"},
						 * {"mDataProp":"AMOUNT","sWidth": "80px","sClass":
						 * "amount"},
						 */
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
						/*
						 * {"mDataProp":"REMARK","sWidth": "80px","sClass":
						 * "remark"},
						 */
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
		
		/*
		 * //应收 $("#item_fin_save").click(function(){ var deliveryid
		 * =$("#delivery_id").val();
		 * $.post('/deliveryOrderMilestone/receiptSave/'+deliveryid,
		 * $("#fin_form").serialize(), function(data){ console.log(data);
		 * if(data.success){ //receipttable.fnDraw();
		 * $('#fin_item').modal('hide'); $('#resetbutton').click(); }else{ }
		 * 
		 * }); });
		 */
		// 应付
		$("#addrow").click(function(){	
			var deliveryid =$("#delivery_id").val();
			$.post('/deliveryOrderMilestone/addNewRow/'+deliveryid,function(data){
				console.log(data);
				if(data[0] != null){
					paymenttable.fnSettings().sAjaxSource = "/deliveryOrderMilestone/accountPayable/"+deliveryid;
					paymenttable.fnDraw();
				}else{
					alert("请到基础模块维护应付条目！");
				}
			});		
		});	
		// 应付修改
		$("#table_fin2").on('blur', 'input,select', function(e){
			e.preventDefault();
			var paymentId = $(this).parent().parent().attr("id");
			var name = $(this).attr("name");
			var value = $(this).val();
			$.post('/deliveryOrderMilestone/updateDeliveryOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
				if(data.success){
				}else{
					alert("修改失败!");
				}
	    	},'json');
		});
		
		//异步删除应付
		 $("#table_fin2").on('click', '.finItemdel', function(e){
			 var id = $(this).attr('code');
			  e.preventDefault();
			  $.post('/deliveryOrderMilestone/finItemdel/'+id,function(data){
	               //保存成功后，刷新列表
	               console.log(data);
	               paymenttable.fnDraw();
	           },'json');
		 });
		/*
		 * //应收 $("#addrow2").click(function(){ var deliveryid
		 * =$("#delivery_id").val();
		 * $.post('/deliveryOrderMilestone/addNewRow2/'+deliveryid,function(data){
		 * console.log(data); if(data.success){ paymenttable.fnDraw();
		 * //$('#fin_item2').modal('hide'); //$('#resetbutton2').click(); }else{ }
		 * }); });
		 */
		
	    // 获取全国省份
	    $(function(){
	     	var province = $("#mbProvinceTo");
	     	$.post('/serviceProvider/province',function(data){
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
	    
	    // 获取省份的城市
	    $('#mbProvinceTo').on('change', function(){
				var inputStr = $(this).val();
				$.get('/serviceProvider/city', {id:inputStr}, function(data){
					var cmbCity =$("#cmbCityTo");
					cmbCity.empty();
					cmbCity.append("<option>--请选择城市--</option>");
					for(var i = 0; i < data.length; i++)
					{
						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				},'json');
			});
	    
	    // 获取城市的区县
	    $('#cmbCityTo').on('change', function(){
				var inputStr = $(this).val();
				var code = $("#locationTo").val(inputStr);
				$.get('/serviceProvider/area', {id:inputStr}, function(data){
					var cmbArea =$("#cmbAreaTo");
					cmbArea.empty();
					cmbArea.append("<option>--请选择区(县)--</option>");
					for(var i = 0; i < data.length; i++)
					{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
					}
				},'json');
			});
	    
	    $('#cmbAreaTo').on('change', function(){
				var inputStr = $(this).val();
				var code = $("#locationTo").val(inputStr);
			});  

	    // 回显城市
	    var hideProvince = $("#hideProvinceTo").val();
	    $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
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
	    $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
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
	    


	    //计费方式回显
		var departOrderId = $("#delivery_id").val();
		if(departOrderId != '' && departOrderId != null){
			var departOrderChargeType = $("#chargeTypeRadio").val();

			$("input[name='chargeType']").each(function(){
				if(departOrderChargeType == $(this).val()){
					//零担
					if(departOrderChargeType == "perCargo"){
						//隐藏车辆信息
						$("#carInfomation").hide();
						
						$(this).prop('checked', true);
						$("#ltl_price_type").show();
						var hibLtlUnitType = $("#hibLtlUnitType").val();
						$("input[value='"+hibLtlUnitType+"']").prop('checked', true);
					}else if(departOrderChargeType == "perCar"){
	                    //显示车辆信息                   
	                    $(this).prop('checked', true);
	                    $("#car_type_div").show();
	                    var departOrderCarType = $("#hiddenDeliveryOrderCarType").val();
	                    $("#car_type").val(departOrderCarType);
	                }else{
	    				if(departOrderChargeType=="perUnit"){
	    					$("#carInfomation").hide();
	    				}else{
	    					$("#carInfomation").show();
	    				}
	    				$(this).prop('checked', true);
	    			}
				}
			});
		}else{
			var transferOrderChargeType = $("#transferOrderChargeType").val();
			$("input[name='chargeType']").each(function(){
				
				if(transferOrderChargeType == $(this).val()){
					//零担
					if(transferOrderChargeType == "perCargo"){
						$("#carInfomation").hide();
						$(this).prop('checked', true);
						$("#ltl_price_type").show();
						$("#optionsRadiosIn1").prop('checked', true);
						//隐藏车辆信息								
	    			}else if(transferOrderChargeType == "perCar"){
	                    $("#carInfomation").show();
	                    //显示车辆信息                   
	                    $(this).prop('checked', true);
	                    $("#car_type_div").show();
	                }else{
	    				if(transferOrderChargeType=="perUnit"){
	    					$("#carInfomation").hide();
	    				}else{
	    					$("#carInfomation").show();
	    				}
	    				
	    				$(this).prop('checked', true);
	    			}
				}
			});
		}

	     $("input[name='chargeType']").click(function(){
	    	 //等于零担的时候
	        if($('input[name="chargeType"]:checked').val()==='perCargo'){
	            $('#ltl_price_type').show();
	            $("#carInfomation").hide();
	            $("#car_type_div").hide();
	        }else if($('input[name="chargeType"]:checked').val()==='perCar'){
	            $("#carInfomation").show();
	            //显示车辆信息                   
	            $(this).prop('checked', true);
	            $("#car_type_div").show();
	            $('#ltl_price_type').hide();
	        }else{
	            $('#ltl_price_type').hide();
	            $("#car_type_div").hide();
	            //计费方式为计件的时候
	            if($('input[name="chargeType"]:checked').val()==='perUnit'){
	            	$("#carInfomation").hide();
	            }else{
	            	$("#carInfomation").show();
	            }
	        }
	     });
		
		$("#mbProvinceTo").on('change',function(){
			$("#notify_address").val("");
			if($("#mbProvinceTo").find("option:selected").text() =="--请选择省份--"){
				$("#notify_address").val("");
			}else{
				$("#notify_address").val($("#mbProvinceTo").find("option:selected").text());
			}
			$("#cmbAreaTo").get(0).selectedIndex=0;
			$("#cmbAreaTo").empty();
		});
		$("#cmbCityTo").on('change',function(){
			$("#notify_address").val("");
			if($("#cmbCityTo").find("option:selected").text() =="--请选择城市--"){
				$("#notify_address").val($("#mbProvinceTo").find("option:selected").text());
			}else{
				$("#notify_address").val($("#mbProvinceTo").find("option:selected").text()+" "+$("#cmbCityTo").find("option:selected").text());
			}
			
			
		});
		$("#cmbAreaTo").on('change',function(){
			$("#notify_address").val("");
			if($("#cmbCityTo").find("option:selected").text() =="--请选择城市--"){
				$("#notify_address").val($("#mbProvinceTo").find("option:selected").text());
			}else if($("#cmbAreaTo").find("option:selected").text() =="--请选择区(县)--"){
				$("#notify_address").val($("#mbProvinceTo").find("option:selected").text()+" "+$("#cmbCityTo").find("option:selected").text());
			}else{
				$("#notify_address").val($("#mbProvinceTo").find("option:selected").text()+" "+$("#cmbCityTo").find("option:selected").text()+" "+$("#cmbAreaTo").find("option:selected").text());
			}
			
		});
		
		$('#datetimepicker').datetimepicker({  
	        format: 'yyyy-MM-dd',  
	        language: 'zh-CN', 
	        autoclose: true,
	        pickerPosition: "bottom-left"
	    }).on('changeDate', function(ev){
	        $(".bootstrap-datetimepicker-widget").hide();
	        $('#order_delivery_stamp').trigger('keyup');
	    });	
		
		$('#datetimepicker1').datetimepicker({  
	        format: 'yyyy-MM-dd',  
	        language: 'zh-CN', 
	        autoclose: true,
	        pickerPosition: "bottom-left"
	    }).on('changeDate', function(ev){
	    	$(".bootstrap-datetimepicker-widget").hide();
	        $('#client_order_stamp').trigger('keyup');
	    });	
		
		$('#datetimepicker2').datetimepicker({  
	        format: 'yyyy-MM-dd',  
	        language: 'zh-CN', 
	        autoclose: true,
	        pickerPosition: "bottom-left"
	    }).on('changeDate', function(ev){
	    	$(".bootstrap-datetimepicker-widget").hide();
	        $('#business_stamp').trigger('keyup');
	    });	
		
		
		/*//回显初始地
		var locationFrom = $("#locationForm").val();
		if(locationFrom == "")
			$("#hideLocationFrom").val();
		//var searchAllLocationFrom = function(locationFrom){
		if(locationFrom != ""){
	    	$.get('/transferOrder/searchLocationFrom', {locationFrom:locationFrom}, function(data){
	    		console.log(data);			
	    		var provinceVal = data.PROVINCE;
	    		var cityVal = data.CITY;
	    		var districtVal = data.DISTRICT;
		        $.get('/serviceProvider/searchAllLocation', {province:provinceVal, city:cityVal}, function(data){	
			        //获取全国省份
		         	var province = $("#mbProvinceFrom");
		     		province.empty();
		     		province.append("<option>--请选择省份--</option>");
		     		for(var i = 0; i < data.provinceLocations.length; i++){
						if(data.provinceLocations[i].NAME == provinceVal){
							$("#locationForm").val(data.provinceLocations[i].CODE);
							province.append("<option value= "+data.provinceLocations[i].CODE+" selected='selected'>"+data.provinceLocations[i].NAME+"</option>");
						}else{
							province.append("<option value= "+data.provinceLocations[i].CODE+">"+data.provinceLocations[i].NAME+"</option>");						
						}
					}

					var cmbCity =$("#cmbCityFrom");
		     		cmbCity.empty();
					cmbCity.append("<option  value=''>--请选择城市--</option>");
					for(var i = 0; i < data.cityLocations.length; i++)
					{
						if(data.cityLocations[i].NAME == cityVal){
							$("#locationForm").val(data.cityLocations[i].CODE);
							cmbCity.append("<option value= "+data.cityLocations[i].CODE+" selected='selected'>"+data.cityLocations[i].NAME+"</option>");
						}else{
							cmbCity.append("<option value= "+data.cityLocations[i].CODE+">"+data.cityLocations[i].NAME+"</option>");						
						}
					}
					
					if(data.districtLocations.length > 0){
	    				var cmbArea =$("#cmbAreaFrom");
	    				cmbArea.empty();
	    				cmbArea.append("<option  value=''>--请选择区(县)--</option>");
	    				for(var i = 0; i < data.districtLocations.length; i++)
	    				{
	    					if(data.districtLocations[i].NAME == districtVal){
	    						$("#locationForm").val(data.districtLocations[i].CODE);
	    						cmbArea.append("<option value= "+data.districtLocations[i].CODE+" selected='selected'>"+data.districtLocations[i].NAME+"</option>");
	    					}else{
	    						cmbArea.append("<option value= "+data.districtLocations[i].CODE+">"+data.districtLocations[i].NAME+"</option>");						
	    					}
	    				}
	    			}else{
	    				var cmbArea =$("#cmbArea");
	    				cmbArea.empty();
	    			}
		        },'json');
	    	},'json');
	  };*/
	//获取全国省份
    $(function(){
     	var province = $("#mbProvinceFrom");
     	$.post('/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
			var hideProvince = $("#hideProvinceFrom").val();
     		for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideProvince){
						province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
						$("#locationForm").val(data[i].CODE);
					}else{
						province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     	},'json');
    });
    
    //获取省份的城市
    $('#mbProvinceFrom').on('change', function(){
		var inputStr = $(this).val();
		$("#locationForm").val(inputStr);
		$.get('/serviceProvider/city', {id:inputStr}, function(data){
			var cmbCity =$("#cmbCityFrom");
			cmbCity.empty();
			cmbCity.append("<option>--请选择城市--</option>");
			for(var i = 0; i < data.length; i++)
			{
				cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
			}
		},'json');
	});
    //获取城市的区县
    $('#cmbCityFrom').on('change', function(){
		var inputStr = $(this).val();
		$("#locationForm").val(inputStr);
		$.get('/serviceProvider/area', {id:inputStr}, function(data){
			var cmbArea =$("#cmbAreaFrom");
			cmbArea.empty();
			cmbArea.append("<option>--请选择区(县)--</option>");
			for(var i = 0; i < data.length; i++)
			{
				cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
			}
			
		},'json');
	});
    
    $('#cmbAreaFrom').on('change', function(){
		var inputStr = $(this).val();
		$("#locationForm").val(inputStr);
	});   
    // 回显城市
    var hideProvince = $("#hideProvinceFrom").val();
    $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
		if(data.length > 0){
			var cmbCity =$("#cmbCityFrom");
			cmbCity.empty();
			cmbCity.append("<option>--请选择城市--</option>");
			var hideCity = $("#hideCityFrom").val();
			for(var i = 0; i < data.length; i++)
			{
				if(data[i].NAME == hideCity){
					cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					$("#locationForm").val(data[i].CODE);
				}else{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			}
		}
	},'json');

    // 回显区
    var hideCity = $("#hideCityFrom").val();
    $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
		if(data.length > 0){
			var cmbArea =$("#cmbAreaFrom");
			cmbArea.empty();
			cmbArea.append("<option>--请选择区(县)--</option>");
			var hideDistrict = $("#hideDistrictFrom").val();
			for(var i = 0; i < data.length; i++)
			{
				if(data[i].NAME == hideDistrict){
					cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					$("#locationForm").val(data[i].CODE);
				}else{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			}
		}
	},'json');
});

