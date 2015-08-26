
$(document).ready(function() {
	document.title = '复核付款| '+document.title;
    $('#menu_finance').addClass('active').find('ul').addClass('in');
   
    var paymentMethod = "";
    
	//datatable, 动态处理
    var uncostAcceptOrderTab = $('#uncostAccept-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			$(nRow).attr({order_no: aData.ATTRIBUTE});
			return nRow;
		},
        "sAjaxSource": "/costAcceptOrder/unlist",
        "aoColumns": [   
	        { "mDataProp": null, "sWidth":"20px",
	            "fnRender": function(obj) {
	            	if(obj.aData.STATUS == "已付款确认"){
	            		return "";
	            	}else{
	            		return '<input type="checkbox" name="order_check_box"  class="checkedOrUnchecked" value="'+obj.aData.ID+'" order_no="'+obj.aData.ATTRIBUTE+'">';
	            	}
	              
	            }
            }, 
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
            		if(obj.aData.ATTRIBUTE == '对账单')
            			return "<a href='/costAcceptOrder/edit2?id="+obj.aData.ID+"&attribute="+obj.aData.ATTRIBUTE+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		else if(obj.aData.ATTRIBUTE == '报销单')
            			return "<a href='/carreimbursement/edit?orderId="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		else
            			return obj.aData.ORDER_NO;
        		} 
            },
            {"mDataProp":"TOTAL_AMOUNT",
            	"sClass": "pay_amount",
            	 "fnRender": function(obj) {
            		 if(obj.aData.TOTAL_AMOUNT == null || obj.aData.TOTAL_AMOUNT == '' ){
            			 return '<p style="color:red">0<p>';
            		 }else{
            			 return obj.aData.TOTAL_AMOUNT;
            		 }
            	 }
            },  
            {"mDataProp":"CNAME", "sWidth":"150px"},   
            {"mDataProp":"PAYEE_NAME", "sWidth":"150px"},
            {"mDataProp":"INVOICE_NO", "sWidth":"80px"},
            {"mDataProp":"PAYMENT_METHOD", "sWidth":"80px",
                "fnRender": function(obj) {
                    if(obj.aData.PAYMENT_METHOD == 'cash')
                        return '现金';
                    else if(obj.aData.PAYMENT_METHOD == 'transfers')
                        return '转账';
                    else
                    	return '';
                }
            },
            {"mDataProp":"STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='audit'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },
            /*{"mDataProp":"COST_ORDER_NO"},
            {"mDataProp":"OFFICE_NAME"},
            {"mDataProp":"CNAME"},*/            
            {"mDataProp":null},     
            {"mDataProp":null},     
              
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
               
            {"mDataProp":"REMARK"},
            {"mDataProp":null},     
            {"mDataProp":null}                        
        ]      
    });
    
    //未复核列表
	$("#uncostAccept-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
			if($(this).parent().siblings('.pay_amount')[0].textContent == 0){
				$.scojs_message('申请金额不能为0!', $.scojs_message.TYPE_FALSE);
				return false;
			}
		}
	});
	
	
	$('#confirmBtn').click(function(e){
        e.preventDefault();
        $('#confirmForm').submit();
    });
    
    
    var costAcceptOrderTab = $('#costAccept-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			return nRow;
		},
        "sAjaxSource": "/costAcceptOrder/list",
        "aoColumns": [
			{ "mDataProp": null, "sWidth":"20px",
			    "fnRender": function(obj) {
                    if(obj.aData.STATUS=='付款确认中' || obj.aData.STATUS=='已付款确认'){
                        return '';
                    }
			        return '<input type="checkbox" name="order_check_box" id="'+obj.aData.ID+'" class="invoice" order_no="'+obj.aData.ORDER_NO+'">';
			    }
			},
            {"mDataProp":"ORDER_NO","sWidth":"80px",
            	"fnRender": function(obj) {
        			return "<a href='/costPreInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}
            },
            {"mDataProp":"TOTAL_AMOUNT",
            	"sClass":"pay_amount",
           	 	"fnRender": function(obj) {
        		 if(obj.aData.TOTAL_AMOUNT == null || obj.aData.TOTAL_AMOUNT == '' ){
        			 return '<p style="color:red">0<p>';
        		 }else{
        			 return obj.aData.TOTAL_AMOUNT;
        		 }
        	 }
            },  
            {"mDataProp":"CNAME", 
            	"sClass": "cname"
            },  
            {"mDataProp":"PAYEE_NAME", 
            	"sClass": "payee_name"},
            {"mDataProp":"INVOICE_NO", "sWidth":"80px" },
            {"mDataProp":"PAYMENT_METHOD",  "sWidth":"80px",
                "fnRender": function(obj) {
                    if(obj.aData.PAYMENT_METHOD == 'cash')
                        return '现金';
                    else if(obj.aData.PAYMENT_METHOD == 'transfers')
                        return '转账';
                    else
                    	return '';
                }
            },
            {"mDataProp":"STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },
            /*{"mDataProp":"COST_ORDER_NO"},
            {"mDataProp":"OFFICE_NAME"},
            {"mDataProp":"CNAME"},*/            
            {"mDataProp":null},     
            {"mDataProp":null},   
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},    
            {"mDataProp":"REMARK"},
            {"mDataProp":null},     
            {"mDataProp":null}                        
        ]      
    });
    
    //已复核列表
    var ids = [];
	var cnames = [];
	var payee_names = [];
    // 未选中列表
	$("#costAccept-table").on('click', '.invoice', function(e){
		if($(this).prop("checked") == true){
			if($(this).parent().siblings('.pay_amount')[0].textContent == 0){
				$.scojs_message('申请金额不能为0!', $.scojs_message.TYPE_FALSE);
				return false;
			}
			ids.push($(this).attr('id'));
			if(ids.length>0){
				$("#confirmBtn").attr("disabled",false);
				if(ids.length > 1){
					if(cnames[0] != $(this).parent().siblings('.cname')[0].textContent){
						$.scojs_message('请选择相同的供应商!', $.scojs_message.TYPE_FALSE);
						var tmpArr1 = [];
						for(id in ids){
							if(ids[id] != $(this).attr('id')){
								tmpArr1.push(ids[id]);
							}
						}
						ids = tmpArr1;
						return false;
					}else if(payee_names[0] != $(this).parent().siblings('.payee_name')[0].textContent){
						$.scojs_message('请选择相同的收款人!', $.scojs_message.TYPE_FALSE);
						var tmpArr2 = [];
						for(id in ids){
							if(ids[id] != $(this).attr('id')){
								tmpArr2.push(ids[id]);
							}
						}
						ids = tmpArr2;
						return false;
					}
				}
				cnames.push($(this).parent().siblings('.cname')[0].textContent);
				payee_names.push($(this).parent().siblings('.payee_name')[0].textContent);
			}
		}else if($(this).prop("checked") == false){
			var tmpArr = [];
			for(id in ids){
				if(ids[id] != $(this).attr('id')){
					tmpArr.push(ids[id]);
				}
			}
			ids = tmpArr;
		}
		$("#invoiceApplicationOrderIds").val(ids);
		if(ids.length == 0){
			$("#confirmBtn").attr("disabled",true);
			 cnames = [];
			 payee_names = [];
		}
	});
	
	
	$('#confirmBtn').click(function(e){
        e.preventDefault();
        $('#confirmForm').submit();
    });
    
    
    
    
    $.post('/costMiscOrder/searchAllAccount',function(data){
		 if(data.length > 0){
			 var accountTypeSelect = $("#accountTypeSelect");
			 accountTypeSelect.empty();
			 var hideAccountId = $("#hideAccountId").val();
			 accountTypeSelect.append("<option ></option>");
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideAccountId){
					 accountTypeSelect.append("<option value='"+data[i].ID+"' selected='selected'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");
				 }else{
					 accountTypeSelect.append("<option value='"+data[i].ID+"'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");					 
				 }
			}
		}
	},'json');
    
   $("#paymentMethods").on('click', 'input', function(){
   	if($(this).val() == 'cash'){
   		$("#accountTypeDiv").hide();
   	}else{
   		$("#accountTypeDiv").show();    		
   	}
   });    
   
    var ids = [];
    // 未选中列表
	$("#costAccept-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
            var orderNo = $(this).parent().parent().find('a').text();
            var orderObj=$(this).val()+":"+orderNo;
            //var order = ids.pop();
			ids.push(orderObj);

			$("#costIds").val(ids);
		}else{
			if(ids.length != 0){
				ids.splice($.inArray($(this).val(),ids),1);
				$("#costIds").val(ids);
			}
			if(ids.length <= 0){
			}
		}			
	});	
	$("#status_filter").on('change',function(){
		var status = $("#status_filter").val();
		costAcceptOrderTab.fnSettings().sAjaxSource = "/costAcceptOrder/list?status="+status;
		costAcceptOrderTab.fnDraw(); 
	});
	
	/*//点击确认
	$("#auditBtn").click(function(){
		if(paymentMethod == '现金'){
			$("#paymentMethod1").attr("checked","checked");
			$("#cashLabel").show();
			$("#transfersLabel").hide();
			$("#accountTypeDiv").hide();
		}else if(paymentMethod == '转账'){
			$("#paymentMethod2").attr("checked","checked");
			$("#cashLabel").hide();
			$("#transfersLabel").show();
			$("#accountTypeDiv").show();
		}
	});*/
		
	$("#checkBtn").on('click', function(){
		var idArr=[];  	
		var orderArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		idArr.push($(this).attr('value'));
        		orderArr.push($(this).attr('order_no'));
        	}
        });     
        console.log(idArr);
        var ids = idArr.join(",");
        var order= orderArr.join(",");
        $.post("/costAcceptOrder/checkStatus", {ids:ids,order:order}, function(data){
        	costAcceptOrderTab.fnDraw(); 
        	uncostAcceptOrderTab.fnDraw();
        },'json');
	});
	$('#customer_filter').on('keyup click', function(){
		var me = this;
        var inputStr = $('#customer_filter').val();
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
            //console.log(data);
            var companyList =$("#companyList");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
            }
            if(data.length>0)
                companyList.show();
            companyList.css({ 
    	    	left:$(me).position().left+"px", 
    	    	top:$(me).position().top+30+"px" 
    	    }); 
        },'json');
    });
	//当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#companyList').on('blur', function(){
        $('#companyList').hide();
    });

    $('#companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).text());
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId);
       
    });
 // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });
    $('#customer_filter1').on('keyup click', function(){
    	var me = this;
        var inputStr = $('#customer_filter1').val();
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
            //console.log(data);
            var companyList =$("#companyList1");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem1' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
            }
            if(data.length>0){
            	companyList.css({ 
	    	    	left:$(me).position().left+"px", 
	    	    	top:$(me).position().top+30+"px" 
	    	    }); 
                companyList.show();
            }
	            
        },'json');
    });
	//当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#companyList1').on('blur', function(){
        $('#companyList1').hide();
    });

    $('#companyList1').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    $('#companyList1').on('click', '.fromLocationItem1', function(e){        
        $('#customer_filter1').val($(this).text());
        $("#companyList1").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId1').val(companyId);
       
    });
 // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter1').on('blur', function(){
        $('#companyList1').hide();
    });
    
    var costAcceptPayedOrderTab = $('#costAcceptPayed-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			return nRow;
		},
        "sAjaxSource": "/costAcceptOrder/list?status=payed",
        "aoColumns": [
			{ "mDataProp": null, "sWidth":"20px",
			    "fnRender": function(obj) {
                    if(obj.aData.STATUS=='付款确认中' || obj.aData.STATUS=='已付款确认'){
                        return '';
                    }
			        return '<input type="checkbox" name="order_check_box" id="'+obj.aData.ID+'" class="invoice" order_no="'+obj.aData.ORDER_NO+'">';
			    }
			},
            {"mDataProp":"ORDER_NO","sWidth":"80px",
            	"fnRender": function(obj) {
        			return "<a href='/costPreInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}
            },
            {"mDataProp":"TOTAL_AMOUNT",
            	"sClass":"pay_amount",
           	 	"fnRender": function(obj) {
        		 if(obj.aData.TOTAL_AMOUNT == null || obj.aData.TOTAL_AMOUNT == '' ){
        			 return '<p style="color:red">0<p>';
        		 }else{
        			 return obj.aData.TOTAL_AMOUNT;
        		 }
        	 }
            },  
            {"mDataProp":"CNAME", 
            	"sClass": "cname"
            },  
            {"mDataProp":"PAYEE_NAME", 
            	"sClass": "payee_name"},
            {"mDataProp":"INVOICE_NO", "sWidth":"80px" },
            {"mDataProp":"PAYMENT_METHOD",  "sWidth":"80px",
                "fnRender": function(obj) {
                    if(obj.aData.PAYMENT_METHOD == 'cash')
                        return '现金';
                    else if(obj.aData.PAYMENT_METHOD == 'transfers')
                        return '转账';
                    else
                    	return '';
                }
            },
            {"mDataProp":"STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },
            /*{"mDataProp":"COST_ORDER_NO"},
            {"mDataProp":"OFFICE_NAME"},
            {"mDataProp":"CNAME"},*/            
            {"mDataProp":null},     
            {"mDataProp":null},   
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},    
            {"mDataProp":"REMARK"},
            {"mDataProp":null},     
            {"mDataProp":null}                        
        ]      
    });
} );