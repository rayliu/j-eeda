$(document).ready(function() {
	$('#menu_deliver').addClass('active').find('ul').addClass('in');
	
	$("input[name='cargoType']").click(function(){
		var cargoType = $(this).val();
		if(cargoType == "ATM"){
			$("#basic").hide();
			$("#cargos").show();
			dab.fnDraw();
		}else{
			$("#basic").show();
			$("#cargos").hide();
			dab2.fnDraw();
		}
	});
	
	//点击创建 - 普货  
	$("#saveDeliveryCargo").click(function(e){
		e.preventDefault();
    	var productIds=[];
    	var shippingNumbers = [];
    	$("#eeda-table2 tr:not(:first)").each(function(){
        	$("input:checked",this).each(function(){
        		productIds.push($(this).val()); //货品id
        		shippingNumbers.push($(this).parent().parent().find("td>input[name='amount']").val());
        	});
    	}); 
    	console.log("货品id:" + productIds + ",发货数量: " + shippingNumbers);
    	$("#productIds").val(productIds);
    	$("#shippingNumbers").val(shippingNumbers);
        $('#createCargoForm').submit();
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
					       return '<input type="checkbox" class="checkedOrUnchecked" inventoryId='+obj.aData.INVERTORYID+' code3='+obj.aData.CUSTOMER_ID+' name="order_check_box" value="'+obj.aData.ID+'">';
					    }
					},   
  		            {"mDataProp":"ITEM_NO"},
  		            {"mDataProp":"ITEM_NAME"},
  		            {"mDataProp":"WAREHOUSE_NAME","sClass": "warehouse"},
  		            {"mDataProp":"WAREHOUSE_ID", "bVisible": false},
  		            {"mDataProp":"ABBR","sClass": "cname"},
  		            {"mDataProp":"CUSTOMER_ID", "bVisible": false},
  		            {"mDataProp":"TOTAL_QUANTITY"},
  		            {"mDataProp":null,
  		            	"fnRender": function(obj) {
  		            		if(obj.aData.AVAILABLE_QUANTITY != null && obj.aData.AVAILABLE_QUANTITY != "")
  		            			return obj.aData.TOTAL_QUANTITY - obj.aData.AVAILABLE_QUANTITY;
  		            		else
  		            			return "0";
					    }
  		            },
  		            {"mDataProp":null,
  		            	"fnRender": function(obj) {
  		            		if(obj.aData.AVAILABLE_QUANTITY != null && obj.aData.AVAILABLE_QUANTITY != "")
  		            			return obj.aData.AVAILABLE_QUANTITY;
  		            		else
  		            			return obj.aData.TOTAL_QUANTITY;
					    }
  		            },
  		            { 
  		                "mDataProp": null, 
  		                "fnRender": function(obj) {                    
  		                    return "<input type='text' name='amount' toal="+obj.aData.TOTAL_QUANTITY +" available="+obj.aData.AVAILABLE_QUANTITY+" disabled value='0'>";
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

			
			$inputAmount.attr('disabled', false).val('1');// 允许输入配送数量

			
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
           },'json');
           $("#companyList").css({ 
    	    	left:$(this).position().left+"px", 
    	    	top:$(this).position().top+32+"px" 
    	   });
           $("#companyList").show();
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
        },'json');
        $("#companyList1").css({ 
	    	left:$(this).position().left+"px", 
	    	top:$(this).position().top+32+"px" 
	    }); 
	    $('#companyList1').show();
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
	      	}/*else{
	      		dab2.fnSettings().sAjaxSource = "/delivery/searchTransfer";
		      	dab2.fnDraw();
	      	}*/
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
 		var warehouseId = $(this).attr('code');
 		$("#warehouseId").val(warehouseId);
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
    
	//输入普货配送数量
	$("#eeda-table2").on('blur', 'input', function(e){
		var value = $(this).val();
		var toal = $(this).attr("toal");
		var available = $(this).attr("available");
		console.log("value:"+value+",total:"+toal+",available:"+available);
		if(isNaN(value)){
			$.scojs_message('只能输入数字,请重新输入', $.scojs_message.TYPE_OK);
			$(this).val("");
			$(this).focus();
			return false;
		}else{
			if(available == "null" || available == "" || available == null){
				available = toal;
			}
			if(value > available){
				$.scojs_message('配送数量不能大于可用库存,请重新输入', $.scojs_message.TYPE_OK);
				$(this).val("");
				$(this).focus();
				return false;
			}
		}
	});
    
});