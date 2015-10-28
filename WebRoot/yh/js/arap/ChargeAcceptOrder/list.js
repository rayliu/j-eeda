
$(document).ready(function() {
	document.title = '复核收款 | '+document.title;
	
	if($("#page").val()=='return'){
    	$('a[href="#panel-2"]').tab('show');
    }
	
    $('#menu_finance').addClass('active').find('ul').addClass('in');
   
	//datatable, 动态处理
    var chargeNoAcceptOrderTab = $('#chargeNoAccept-table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
        "iDisplayLength": 100,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			return nRow;
		},
        "sAjaxSource": "/chargeAcceptOrder/list?status=unCheck",
        "aoColumns": [   
	        { "mDataProp": null, "sWidth":"20px",
	            "fnRender": function(obj) {
	            	return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
	            }
            }, 
            {"mDataProp":"ORDER_NO","sWidth":"100px",
            	"fnRender": function(obj) {
            		if(obj.aData.ORDER_TYPE == '手工收入单'){
	            		return obj.aData.ORDER_NO;
	            	}else if(obj.aData.ORDER_TYPE == '对账单'){
	            		return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
	            	}else if(obj.aData.ORDER_TYPE == '往来票据单'){
                        return "<a href='/inOutMiscOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else{
	            		return "<a href='/chargeInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
	            	}
        		}},
            {"mDataProp":"ORDER_TYPE","sWidth":"80px",
        			"sClass":"order_type"
        	},   
            {"mDataProp":"INVOICE_NO","sWidth":"80px"},
            {"mDataProp":"STATUS","sWidth":"80px",
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
            /*{"mDataProp":"CHARGE_ORDER_NO"},
            {"mDataProp":"OFFICE_NAME"},
            {"mDataProp":"CNAME"},*/ 
            {"mDataProp":"TOTAL_AMOUNT","sWidth":"60px"},     
            {"mDataProp":null,"sWidth":"60px"},     
            {"mDataProp":null,"sWidth":"60px"},  
            {"mDataProp":"PAYEE","sWidth":"60px"},
            {"mDataProp":"CUSTOMER","sWidth":"120px"},
            {"mDataProp":"CNAME"},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            
            {"mDataProp":"REMARK","sWidth":"180px"},
            {"mDataProp":null},     
            {"mDataProp":null}                        
        ]      
    });
    
    
    
    $.post('/chargeMiscOrder/searchAllAccount',function(data){
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
	$("#chargeNoAccept-table").on('click', '.checkedOrUnchecked', function(e){
		$("#checkBtn").attr('disabled',false);
		if($(this).prop("checked") == true){
            var orderNo = $(this).parent().parent().find('.order_type').text();
            var orderObj=$(this).val()+":"+orderNo;
			ids.push(orderObj);
		}else{
			var array = [];
			for(id in ids){
				if($(this).val()+":"+$(this).parent().parent().find('.order_type').text()!= ids[id]){
					array.push(ids[id]);
				}
			}
			ids = array;
		}	
		$("#chargeIds").val(ids);
		if(ids.length != 0 ){
			$("#checkBtn").attr('disabled',false);
		}else{
			$("#checkBtn").attr('disabled',true);
		}
	});	
	
	
	$("#checkBtn").on('click', function(){
		$("#checkBtn").attr('disabled',true);
		$.post('chargeAcceptOrder/checkOrder?ids='+$("#chargeIds").val(),function(){
			chargeNoAcceptOrderTab.fnDraw();
			chargeAcceptOrderTab.fnDraw();
			ids = [];
		});
	});
	
	
	$("#status_filter").on('change',function(){
		var status = $("#status_filter").val();
		chargeNoAcceptOrderTab.fnSettings().sAjaxSource = "/chargeAcceptOrder/list?status="+status;
		chargeNoAcceptOrderTab.fnDraw(); 
	});
	
	
	//**
	//*****
	//*********
	//*************已复核
	//*********
	//*****
	//**
	
	
	//datatable, 动态处理
    var chargeAcceptOrderTab = $('#chargeAccept-table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
        "iDisplayLength": 100,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			return nRow;
		},
        "sAjaxSource": "/chargeAcceptOrder/list?status=check",
        "aoColumns": [   
	        { "mDataProp": null, "sWidth":"20px",
	            "fnRender": function(obj) {
	            	if(obj.aData.STATUS =="已收款确认"  || obj.aData.STATUS =="收款确认中"){
	            		return "";
	            	}else{
	            		return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
	            	}
	              
	            }
            }, 
            {"mDataProp":"ORDER_NO","sWidth":"100px",
            	"fnRender": function(obj) {
            		if(obj.aData.ORDER_TYPE == '手工收入单'){
	            		return obj.aData.ORDER_NO;
	            	}else if(obj.aData.ORDER_TYPE == '往来票据单'){
                        return "<a href='/inOutMiscOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else{
	            		return "<a href='/chargeInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
	            	}
        		}},
            {"mDataProp":"ORDER_TYPE","sWidth":"80px",
        			"sClass":"order_type"
        	},   
            {"mDataProp":"INVOICE_NO","sWidth":"80px"},
            {"mDataProp":"STATUS","sWidth":"80px",
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
            {"mDataProp":"TOTAL_AMOUNT","sWidth":"60px"}, 
            /*{"mDataProp":"CHARGE_ORDER_NO"},
            {"mDataProp":"OFFICE_NAME"},
            {"mDataProp":"CNAME"},*/            
            {"mDataProp":null,"sWidth":"60px"},     
            {"mDataProp":null,"sWidth":"60px"},
            {"mDataProp":"PAYEE","sWidth":"60px"},
            {"mDataProp":"CUSTOMER","sWidth":"120px"},
            {"mDataProp":"CNAME"},
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},    
            {"mDataProp":"REMARK","sWidth":"180px"},
            {"mDataProp":null},     
            {"mDataProp":null}                        
        ]      
    });
	
	
    var ids2 = [];
    // 未选中列表
	$("#chargeAccept-table").on('click', '.checkedOrUnchecked', function(e){
		$("#confirmBtn").attr('disabled',false);
		if($(this).prop("checked") == true){
            var orderNo = $(this).parent().parent().find('.order_type').text();
            var orderObj=$(this).val()+":"+orderNo;
            //var order = ids.pop();
			ids2.push(orderObj);
		}else{
			var array = [];
			for(id in ids2){
				if($(this).val()+":"+$(this).parent().parent().find('.order_type').text()!= ids2[id]){
					array.push(ids2[id]);
				}
			}
			ids2 = array;
		}	
		$("#chargeIds2").val(ids2);
		if(ids2.length != 0 ){
			$("#confirmBtn").attr('disabled',false);
		}else{
			$("#confirmBtn").attr('disabled',true);
		}
	});	
	
	
	$('#confirmBtn').click(function(e){
		$("#checkBtn").attr('disabled',true);
        e.preventDefault();
        $('#confirmForm').submit();
        ids = [];
    });
	
	
	//未复核数据条件查询
	var refreshData=function(){
		var orderNo_filter =  $("#orderNo_filter").val();
		var customer_filter =  $("#customer_filter").val();
		var beginTime_filter =  $("#beginTime_filter").val();
		var endTime_filter =  $("#endTime_filter").val();

        chargeNoAcceptOrderTab.fnSettings().sAjaxSource="/chargeAcceptOrder/list?status=unCheck&orderNo_filter="+orderNo_filter
        		                                       +"&customer_filter="+customer_filter
        		                                       +"&beginTime_filter="+beginTime_filter
        		                                       +"&endTime_filter="+endTime_filter;
		chargeNoAcceptOrderTab.fnDraw();
    };
    
    
    $("#orderNo_filter,#customer_filter,#beginTime_filter,#endTime_filter").on('keyup',function(){
    	refreshData();
    });
    
    
    
    
  //已复核数据条件查询
	var refreshData2=function(){
		var orderNo_filter =  $("#orderNo_filter2").val();
		var customer_filter =  $("#customer_filter2").val();
		var beginTime_filter =  $("#beginTime_filter2").val();
		var endTime_filter =  $("#endTime_filter2").val();

		chargeAcceptOrderTab.fnSettings().sAjaxSource="/chargeAcceptOrder/list?status=check&orderNo_filter="+orderNo_filter
        		                                       +"&customer_filter="+customer_filter
        		                                       +"&beginTime_filter="+beginTime_filter
        		                                       +"&endTime_filter="+endTime_filter;
       
		chargeAcceptOrderTab.fnDraw();
    };
    
    
    $("#orderNo_filter2,#customer_filter2,#beginTime_filter2,#endTime_filter2").on('keyup blur',function(){
    	refreshData2();
    });
    

	
	//获取客户列表，自动填充
    $('#customer_filter').on('keyup click', function(event){
        var me = this;
        var inputStr = $('#customer_filter').val();
        var companyList =$("#companyList");
        $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
            companyList.empty();
            for(var i = 0; i < data.length; i++)
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
                
            companyList.css({ 
		    	left:$(me).position().left+"px", 
		    	top:$(me).position().top+32+"px" 
		    });
	        companyList.show();    
        },'json');  
    });
    
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).text());
        $("#companyList").hide();
//        var companyId = $(this).attr('partyId');
//        $('#customerId').val(companyId);
    	refreshData();
    });
    // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    $('#companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    
    
    //已复核界面的客户
  //获取客户列表，自动填充
    $('#customer_filter2').on('keyup click', function(event){
        var me = this;
        var inputStr = $('#customer_filter2').val();
        var companyList2 =$("#companyList2");
        $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
            companyList2.empty();
            for(var i = 0; i < data.length; i++)
                companyList2.append("<li><a tabindex='-1' class='fromLocationItem2' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
                
            companyList2.css({ 
		    	left:$(me).position().left+"px", 
		    	top:$(me).position().top+32+"px" 
		    });
	        companyList2.show();    
        },'json');  
    });
    
    $('#companyList2').on('click', '.fromLocationItem2', function(e){        
        $('#customer_filter2').val($(this).text());
        $("#companyList2").hide();
    	refreshData2();
    });
    // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter2').on('blur', function(){
        $('#companyList2').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#customer_filter2').on('blur', function(){
        $('#companyList2').hide();
    });

    $('#companyList2').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    
    


      //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
      $('#customer_filter3').on('blur', function(){
          $('#companyList3').hide();
      });

      $('#companyList3').on('mousedown', function(){
          return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
      });
    
	
} );