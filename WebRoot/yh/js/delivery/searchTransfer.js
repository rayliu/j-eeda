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
    	/*var productIds=[];
    	var transferItemIds=[];
    	var shippingNumbers = [];*/
		var productIds = null;
    	var transferItemIds = null;
    	var shippingNumbers = null;
    	var result = true;
    	$("#eeda-table2 tr:not(:first)").each(function(){
        	$("input:checked",this).each(function(){
        		if($(this).parent().parent().find("td>input[name='amount']").val() == ""){
        			$.scojs_message('配送数量不能为空,请重新输入', $.scojs_message.TYPE_ERROR);
        			$(this).parent().parent().find("td>input[name='amount']").focus();
        			result = false;
        		}else if($(this).parent().parent().find("td>input[name='amount']").val() == 0){
        			$.scojs_message('对不起，配送数量不能为0', $.scojs_message.TYPE_ERROR);
        			$(this).parent().parent().find("td>input[name='amount']").focus();
        			result = false;
        		}
        		/*productIds.push($(this).val()); //货品id
        		shippingNumbers.push($(this).parent().parent().find("td>input[name='amount']").val());
        		transferItemIds.push($(this).parent().parent().attr("id"));*/
        		productIds = $(this).val(); //货品id
        		shippingNumbers = $(this).parent().parent().find("td>input[name='amount']").val();
        		transferItemIds = $(this).parent().parent().attr("id");
        	});
    	}); 
    	if(result){
    		
        	$("#productIds").val(productIds);
        	$("#shippingNumbers").val(shippingNumbers);
        	$("#transferItemIds").val(transferItemIds);
        	/*$("#transferOrderNo").val()*/
        	$("#transferOrderNo1").val();
            $('#createCargoForm').submit();
    	}
	});
	
	var dab2= $('#eeda-table2').dataTable({
		"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"bLengthChange":true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.TID);
			return nRow;
		},
        "sAjaxSource": "/delivery/searchTransfer",
        "aoColumns": [ 
			{ "mDataProp": null,
			    "fnRender": function(obj) {
			    	if(obj.aData.COMPLETE_AMOUNT == obj.aData.AMOUNT){
			    		return "";
			    	}else if((obj.aData.COMPLETE_AMOUNT + obj.aData.QUANTITY) == obj.aData.AMOUNT){
			    		return "";
			    	}else{
			    		return '<input type="checkbox" class="checkedOrUnchecked" inventoryId='+obj.aData.TID+' code3='+obj.aData.PID+' name="check_box" value="'+obj.aData.PRODUCTID+'">';
			    	}
			    }
			},   
			{"mDataProp":"TID","bVisible": false},
            {"mDataProp":"ITEM_NO"},
            {"mDataProp":"ITEM_NAME"},
            {"mDataProp":"ORDER_NO", "sWidth":"100px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"100px"},
            {"mDataProp":"STATUS","bVisible": false},
            {"mDataProp":"CARGO_NATURE","bVisible": false,
            	"sClass": "cargo_nature", "sWidth":"70px",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "damageCargo"){
            			return "损坏货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return deliver_tran.ex_type;
            		}else{
            			return "";
            		}
            }}, 
            {"mDataProp":"WAREHOUSE_NAME","sClass": "warehouse", "sWidth":"100px"},
            {"mDataProp":"WID", "bVisible": false},
            {"mDataProp":"ABBR","sClass": "cname", "sWidth":"120px"},
            {"mDataProp":"PID", "bVisible": false},
            {"mDataProp":"AMOUNT","sWidth":"30px"},
            {"mDataProp":null,"sWidth":"80px",
            	"fnRender": function(obj) {
            		if(obj.aData.COMPLETE_AMOUNT != null && obj.aData.COMPLETE_AMOUNT != "")
            			return obj.aData.COMPLETE_AMOUNT;
            		else
            			return "0";
			    }
            },
            {"mDataProp":null,"sWidth":"90px",
            	"fnRender": function(obj) {
            		if(obj.aData.QUANTITY != null && obj.aData.QUANTITY != "")
            			return obj.aData.QUANTITY;
            		else
            			return "0";
			    }
            },
            {"mDataProp":null,"sWidth":"90px","sClass": "availableAmount",
            	"fnRender": function(obj) {
            		var amount = obj.aData.AMOUNT;
            		var qunantity = obj.aData.QUANTITY;
            		var complete = obj.aData.COMPLETE_AMOUNT;
            		
            		if(qunantity != null && complete != null){
            			return amount - qunantity - complete;
            		}else if(qunantity != null){
            			return amount - qunantity;
            		}else if(complete != null){
            			return amount - complete;
            		}else{
            			return amount;
            		}
			    }
            },
            { 
                "mDataProp": null, "sWidth":"70px",
                "fnRender": function(obj) {                    
                    return "<input type='text' size='7' class='selectAmount' name='amount'  toal="+obj.aData.TOTAL_QUANTITY +" available="+obj.aData.AVAILABLE_QUANTITY+" disabled value='0'>";
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
		"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"bLengthChange":true,
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
            {"mDataProp":null,//序列号
				"sWidth": "8%", 
				"fnRender": function(obj) {
						if(obj.aData.SERIAL_NO==null){
							return "<a class='serId' style='color:#464D51;text-decoration:none;' code='"+obj.aData.TID+"'></a>";
						}else{
							 return  "<a class='serId' style='color:#464D51;text-decoration:none;' code='"+obj.aData.TID+"'>"+obj.aData.SERIAL_NO+ "</a>";
						}
				      
				    }
            	},
            {"mDataProp":"ITEM_NO"},//产品型号
            {"mDataProp":"PIECES"},
            {"mDataProp":"ARRIVAL_TIME"},
            {"mDataProp":"COMPANY","sWidth": "8%"},//地址
            {"mDataProp":"DRIVER"},
            {"mDataProp":"PHONE"},
            {"mDataProp":"OFFICE_NAME"},
            {"mDataProp":"ABBR",//客户
            	"sClass": "cname"},
            {"mDataProp":"CUSTOMER_ORDER_NO"},//客户订单号
            {"mDataProp":null,//运输单号
            		"sWidth": "10%", 
					"fnRender": function(obj) {
					       return  "<a class='transferNo' style='color:#464D51;text-decoration:none;' code2='"+obj.aData.ORDER_NO+"'>"+obj.aData.ORDER_NO+ "</a>";
					    }
            		},
               
            {"mDataProp":"STATUS"},//状态
            {"mDataProp":"CARGO_NATURE","sClass": "cargo_nature",//货品属性
            	"fnRender":function(obj){
            		if(obj.aData.CARGO_NATURE =="ATM"){
            			return deliver_tran.ex_cargo;
            		}else{
            			return obj.aData.CARGO_NATURE;
            		}
            		
            	}},
            {"mDataProp":"PICKUP_MODE",//提货方式
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == "routeSP"){
            			return "干线供应商自提";
            		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
            			return "外包供应商提货";
            		}else{
            			return deliver_tran.ex_type;
            			
            		}}},
            {"mDataProp":"WAREHOUSE_NAME",//仓库
            	"sClass": "warehouse"},
            {"mDataProp":"TID"}
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
			if(cargoNature==="ATM"){
				$("#saveDelivery").attr('disabled', false);
			}
			if(cargoNature==="cargo"){
				$("#saveDeliveryCargo").attr('disabled', false);
				
			}
			
			$inputAmount.attr('disabled', false).val($(objCheckBox).parent().parent().find("td").eq(10).text());// 允许输入配送数量
			
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
			if(cname.length == 0){
				if($("input[name='cargoType']:checked").val() == 'ATM'){
					$("#saveDelivery").attr('disabled', true);
				}else{
					$("#saveDeliveryCargo").attr('disabled', true);
				}
			}
		}
	};
	
	$("#eeda-table4").on('click', function(){
		
	});
	
	$("#eeda-table4").on('click', '.checkedOrUnchecked', function(){
		buildItems(this, "ATM");
	});
	
	$("#eeda-table2").on('click', '.checkedOrUnchecked', function(){
		var val = $(this).prop("checked");
		var tid = $(this).attr("inventoryId");
		$("input[name='check_box']").each(function(){
			var id = $(this).attr("inventoryId");
			if(id != tid){
				if($(this).prop("checked") == true){
		    	   	$(this).prop("checked",false);
		    	   	$(this).parent().parent().find('input[name=amount]').val("0");
		    	   	$(this).parent().parent().find('input[name=amount]').attr("disabled",true); 
		        }
			}
			
		}); 
		//$(this).prop("checked",true);
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
           $.get("/delivery/searchCustomer", {locationName:inputStr}, function(data){
              
               var companyList =$("#companyList");
               companyList.empty();
               for(var i = 0; i < data.length; i++)
               {
                   companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
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
         var singleid = $("#singleid").val();
         var rdc = $('#hiddenRdc').val();
         var customer_order_number =$("#customer_order_number").val();
         //如果客户和仓库都有值，触发查询
         if(warehouseName!=null&&inputStr!=null&&warehouseName!=""&&inputStr!=""&&rdc!=null&&rdc!=""){
        	 dab.fnSettings().oFeatures.bServerSide = true;
             dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?customerName="+inputStr+"&warehouse="+warehouseName+"&code="+code+"&deliveryOrderNo="+deliveryOrderNo+"&customer_order_number="+customer_order_number+"&singleid="+singleid;
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
        $.get("/delivery/searchCustomer", {locationName:inputStr}, function(data){
          
            var companyList =$("#companyList1");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
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
	      	var transferOrderNo = $("#transferOrderNo").val();
	      	/*&&transferOrderNo!=""&&transferOrderNo!=null*/
	      	if(customerName1!=null&&warehouse1!=null&&customerName1!=""&&warehouse1!=""){
	      		dab2.fnSettings().oFeatures.bServerSide = true;
	      		dab2.fnSettings().sAjaxSource = "/delivery/findTransferOrderItems?customerName1="+customerName1+"&warehouse1="+warehouse1+"&transferOrderNo="+transferOrderNo;
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
 		$.get('/delivery/searchPartWarehouse',{warehouseName:warehouse_Name}, function(data){
 			
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
      	var transferOrderNo = $("#transferOrderNo").val();
      	/*&&transferOrderNo!=""&&transferOrderNo!=null*/
      	if(customerName1!=null&&warehouse1!=null&&customerName1!=""&&warehouse1!=""){
      		dab2.fnSettings().oFeatures.bServerSide = true;
      		dab2.fnSettings().sAjaxSource = "/delivery/findTransferOrderItems?customerName1="+customerName1+"&warehouse1="+warehouse1+"&transferOrderNo="+transferOrderNo;
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

	$('#warehouseList,#rdcList,#companyList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	$('#warehouseList,#rdcList,#companyList').on('mousedown', '.fromLocationItem', function(e){
		//var id =$(this).attr('code');
		var inputStr = $('#customerName2').val();
		var warehouseName =$("#warehouse2").val();
		var code= $("#orderStatue2").val();
		var deliveryOrderNo = $("#deliveryOrderNo2").val();
		var customer_order_number = $("#customer_order_number").val();
		var singleid= $("#singleid").val();
		var rdc = $('#hiddenRdc').val();
		var inputStrrdc=$("#rdc").val();
		//如果客户和仓库都有值，触发查询
		if(inputStrrdc!=null&&inputStr!=null&&inputStrrdc!=""&&inputStr!=""&&rdc!=null&&rdc!=""){
			dab.fnSettings().oFeatures.bServerSide = true;
			dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?customerName="+inputStr+"&warehouse="+warehouseName+"&code="+code+"&deliveryOrderNo="+deliveryOrderNo+"&customer_order_number="+customer_order_number+"&singleid="+singleid+"&inputStrrdc="+inputStrrdc;
			dab.fnDraw();
		}
	  
		$('#warehouseList').hide();
	});
	
	//选择RDC
	$('#rdc').on('keyup click', function(){
		var rdc =$("#rdc").val();
		if(rdc == "")
		$("#hiddenRdc").val("");
		$.get('/delivery/searchPartRDC',{rdc:rdc}, function(data){
			
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
		var rdc = $('#hiddenRdc').val();
		var inputStr = $('#customerName2').val();
        var warehouseName =$("#warehouse2").val();
        var customer_order_number = $("#customer_order_number").val();
        var code= $("#orderStatue2").val();
        var deliveryOrderNo = $("#deliveryOrderNo2").val();
        var singleid = $("#singleid").val();
        var inputStrrdc=$("#rdc").val();
       //如果客户和仓库都有值，触发查询
        if(inputStrrdc!=null&&inputStr!=null&&inputStrrdc!=""&&inputStr!=""){
        	dab.fnSettings().oFeatures.bServerSide = true;
        	dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?customerName="+inputStr+"&warehouse="+warehouseName+"&code="+code+"&deliveryOrderNo="+deliveryOrderNo+"&customer_order_number="+customer_order_number+"&singleid="+singleid+"&inputStrrdc="+inputStrrdc+"&rdc="+rdc;
    		dab.fnDraw();
        }else{
        	dab.fnSettings().oFeatures.bServerSide = true;
        	dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?"+"code="+code+"&deliveryOrderNo="+deliveryOrderNo+"&customer_order_number="+customer_order_number+"&customer_order_number="+customer_order_number+"&singleid="+singleid+"&rdc="+rdc;
    		dab.fnDraw();
        }
	});
	
	
	//选择序列号
	$('#orderStatue2').on('keyup click', function(){
		var orderStatue2 = $("#orderStatue2").val();
		var warehouse2 = $("#warehouse2").val();
		var customerName2 = $("#customerName2").val();
		var warehouse_Name =$("#warehouse2").val();
		if((warehouse2 != '' && warehouse2 != null)&&(customerName2!=null && customerName2!='')){
			$.get('/delivery/searchAllOrderStatue',{orderStatue:orderStatue2,warehouse:warehouse2,customerName: customerName2}, function(data){
				console.log(data);
				var orderStatueList =$("#orderStatueList");
				orderStatueList.empty();
				for(var i = 0; i < data.length; i++)
				{
					orderStatueList.append("<li><a tabindex='-1' class='fromLocationItem' style='display: inline;padding-left: 0px;' code='"+data[i].ID+"'>型号："+data[i].ITEM_NO+"，序列号：<span class='seqNo'>"+data[i].SERIAL_NO+"</span>，仓库："+warehouse_Name+"</a></li>");
				}
			},'json');
			$("#orderStatueList").css({ 
		    	left:$(this).position().left+"px", 
		    	top:$(this).position().top+32+"px" 
		    }); 
		    $('#orderStatueList').show();
	    }
	});
	$('#orderStatue2').on('blur', function(){
		$("#orderStatueList").hide();
	});
	$('#orderStatueList').on('blur', function(){
		$('#orderStatueList').hide();
	});

	$('#orderStatueList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	$('#orderStatueList').on('mousedown', '.fromLocationItem', function(e){
		/*var id =$(this).attr('');*/
		var status2 = $('#orderStatue2');
		status2.val($(this).find('.seqNo').text());
		status2.trigger( "click" );
		$('#orderStatueList').hide();
	});
	$('#warehouseList').on('mousedown', '.fromLocationItem', function(e){
        var id =$(this).attr('code');
        $('#warehouse2').val($(this).text());
        $('#hiddenRdc').val(id);
        $('#rdcList').hide();
        var inputStr = $('#customerName2').val();
        var warehouseName =$("#warehouse2").val();
        var customer_order_number = $("#customer_order_number").val();
        var code= $("#orderStatue2").val();
        var deliveryOrderNo = $("#deliveryOrderNo2").val();
        var singleid = $("#singleid").val();
        var inputStrrdc=$("#rdc").val();
       //如果客户和仓库都有值，触发查询
        if(inputStrrdc!=null&&inputStr!=null&&inputStrrdc!=""&&inputStr!=""){
            dab.fnSettings().oFeatures.bServerSide = true;
            dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?customerName="+inputStr+"&warehouse="+warehouseName+"&code="+code+"&deliveryOrderNo="+deliveryOrderNo+"&customer_order_number="+customer_order_number+"&singleid="+singleid+"&inputStrrdc="+inputStrrdc;
            dab.fnDraw();
        }else{
            dab.fnSettings().oFeatures.bServerSide = true;
            dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?"+"code="+code+"&deliveryOrderNo="+deliveryOrderNo+"&customer_order_number="+customer_order_number+"&customer_order_number="+customer_order_number+"&singleid="+singleid;
            dab.fnDraw();
        }
    });
	$('#rdcList,#companyList').on('click', '.fromLocationItem', function(e){
		var rdc = $('#hiddenRdc').val();
		var inputStr = $('#customerName2').val();
        var warehouseName =$("#warehouse2").val();
        var customer_order_number = $("#customer_order_number").val();
        var code= $("#orderStatue2").val();
        var deliveryOrderNo = $("#deliveryOrderNo2").val();
        var singleid = $("#singleid").val();
        var inputStrrdc=$("#rdc").val();
       //如果客户和仓库都有值，触发查询
        if(inputStrrdc!=null&&inputStr!=null&&inputStrrdc!=""&&inputStr!=""){
        	dab.fnSettings().oFeatures.bServerSide = true;
        	dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?customerName="+inputStr+"&warehouse="+warehouseName+"&code="+code+"&deliveryOrderNo="+deliveryOrderNo+"&customer_order_number="+customer_order_number+"&singleid="+singleid+"&inputStrrdc="+inputStrrdc+"&rdc="+rdc;
    		dab.fnDraw();
        }else{
        	dab.fnSettings().oFeatures.bServerSide = true;
        	dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?"+"code="+code+"&deliveryOrderNo="+deliveryOrderNo+"&customer_order_number="+customer_order_number+"&customer_order_number="+customer_order_number+"&singleid="+singleid+"&rdc="+rdc;
    		dab.fnDraw();
        }
    });
	/***red,? 客户和仓库一有值得时候触发事件****/
  	$('#customerName2,#rdc,#warehouse2,#orderStatue2,#deliveryOrderNo2,#customer_order_number,#singleid').on('keyup click', function(){
  		var rdc = $('#hiddenRdc').val();
  		var inputStr = $('#customerName2').val();
        var warehouseName =$("#warehouse2").val();
        var customer_order_number = $("#customer_order_number").val();
        var code= $("#orderStatue2").val();
        var deliveryOrderNo = $("#deliveryOrderNo2").val();
        var singleid = $("#singleid").val();
        var inputStrrdc=$("#rdc").val();
       //如果客户和仓库都有值，触发查询
        if(inputStrrdc!=null&&inputStr!=null&&inputStrrdc!=""&&inputStr!=""){
        	dab.fnSettings().oFeatures.bServerSide = true;
        	dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?customerName="+inputStr+"&warehouse="+warehouseName+"&code="+code+"&deliveryOrderNo="+deliveryOrderNo+"&customer_order_number="+customer_order_number+"&singleid="+singleid+"&inputStrrdc="+inputStrrdc+"&rdc="+rdc;
    		dab.fnDraw();
        }else{
        	dab.fnSettings().oFeatures.bServerSide = true;
        	dab.fnSettings().sAjaxSource ="/delivery/searchTransferByATM?"+"code="+code+"&deliveryOrderNo="+deliveryOrderNo+"&customer_order_number="+customer_order_number+"&customer_order_number="+customer_order_number+"&singleid="+singleid+"&rdc="+rdc;
    		dab.fnDraw();
        }
		           
    });
  	
  	
  	$("#deliveryOrderNo1,#customerName1,#orderStatue1,#warehouse1,#transferOrderNo").on('keyup click', function () {
  		var customerName1 = $("#customerName1").val();
      	var warehouse1 = $("#warehouse1").val();
      	var transferOrderNo = $("#transferOrderNo").val();
      	/*&&transferOrderNo!=""&&transferOrderNo!=null*/
      	if(customerName1!=null&&warehouse1!=null&&customerName1!=""&&warehouse1!=""){
      		dab2.fnSettings().oFeatures.bServerSide = true;
      		dab2.fnSettings().sAjaxSource = "/delivery/findTransferOrderItems?customerName1="+customerName1+"&warehouse1="+warehouse1+"&transferOrderNo="+transferOrderNo;
	      	dab2.fnDraw();
      	}
  	});
  	
  	$("#transferOrderNo").on('keyup click', function () {
  		var customerName1 = $("#customerName1").val();
      	var warehouse1 = $("#warehouse1").val();
      	var transferOrderNo = $("#transferOrderNo").val();
      	/*&&transferOrderNo!=""&&transferOrderNo!=null*/
      	if(customerName1!=null&&warehouse1!=null&&customerName1!=""&&warehouse1!=""){
      		dab2.fnSettings().oFeatures.bServerSide = true;
      		dab2.fnSettings().sAjaxSource = "/delivery/findTransferOrderItems?customerName1="+customerName1+"&warehouse1="+warehouse1+"&transferOrderNo="+transferOrderNo;
	      	dab2.fnDraw();
      	}
  	});
  	
  	// radio选择普通货品和ATM
	$("input[name=aabbcc]").change(function(){
		var cargo =$(this).val();
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
	$("#eeda-table2").on('keyup click', 'input[name="amount"]', function(e){
		var value = $(this).val();
		var available = $(this).parent().parent().find("td").eq(10).text();
		if(isNaN(value)){
			$.scojs_message('只能输入数字,请重新输入', $.scojs_message.TYPE_ERROR);
			$(this).val("");
			$(this).focus();
			return false;
		}else if(value == 0){
			$.scojs_message('对不起，数量不能为0', $.scojs_message.TYPE_ERROR);
			$(this).val(available);
			$(this).focus();
			return false;
		}else{
			if(value * 1 > available * 1){
				$.scojs_message('配送数量不能大于可用库存,请重新输入', $.scojs_message.TYPE_ERROR);
				$(this).val("");
				$(this).focus();
				return false;
			}
		}
	});
    
});