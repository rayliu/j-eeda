
$(document).ready(function() {
	document.title = '付款确认| '+document.title;
    $('#menu_finance').addClass('active').find('ul').addClass('in');
   
    var paymentMethod = "";
    
	//datatable, 动态处理
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
	            	if(obj.aData.STATUS == "已付款确认"){
	            		return "";
	            	}else{
	            		return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
	            	}
	              
	            }
            }, 
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/costPreInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}
            },
            {"mDataProp":"INVOICE_NO", "sWidth":"80px"},
            {"mDataProp":"PAYMENT_METHOD","sClass": "payment_method",
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
            {"mDataProp":"CNAME", "sWidth":"150px"},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":null},     
            {"mDataProp":"TOTAL_AMOUNT"},     
            {"mDataProp":"REMARK"},
            {"mDataProp":null},     
            {"mDataProp":null}                        
        ]      
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
			$("#auditBtn").attr("disabled",false);
		}else{
			if(ids.length != 0){
				ids.splice($.inArray($(this).val(),ids),1);
				$("#costIds").val(ids);
			}
			if(ids.length <= 0){
				$("#auditBtn").attr("disabled",true);
			}
		}			
	});	
	$("#status_filter").on('change',function(){
		var status = $("#status_filter").val();
		costAcceptOrderTab.fnSettings().sAjaxSource = "/costAcceptOrder/list?status="+status;
		costAcceptOrderTab.fnDraw(); 
	});
	
	//点击确认
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
	});
	
	//选择单据
	$("#costAccept-table").on('click', '.checkedOrUnchecked', function(){
		if($(this).prop('checked') == true){
			if(paymentMethod != ""){
				if(paymentMethod != $(this).parent().siblings('.payment_method')[0].innerHTML){
					alert("请选择相同的付款方式!");
					return false;
				}
			}else
				paymentMethod = $(this).parent().siblings('.payment_method')[0].innerHTML;
		}else{
			var checkedNumber = 0;
			$("#costAccept-table tr").each(function (){
				if($(this).find("td").find("input[type='checkbox']").prop('checked') == true)
					checkedNumber+=1;
			});
			if(checkedNumber == 0)
				paymentMethod = "";
		}
		console.log("付款方式:"+paymentMethod);
	});
	
	
} );