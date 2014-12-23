
$(document).ready(function() {

    $('#menu_finance').addClass('active').find('ul').addClass('in');
   
	//datatable, 动态处理
    var chargeAcceptOrderTab = $('#chargeAccept-table').dataTable({
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
        "sAjaxSource": "/chargeAcceptOrder/list",
        "aoColumns": [   
	        { "mDataProp": null, "sWidth":"20px",
	            "fnRender": function(obj) {
	              return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
	            }
            }, 
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/chargeInvoiceOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"INVOICE_NO"},
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
            /*{"mDataProp":"CHARGE_ORDER_NO"},
            {"mDataProp":"OFFICE_NAME"},
            {"mDataProp":"CNAME"},*/            
            {"mDataProp":null},     
            {"mDataProp":null},     
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
            {"mDataProp":"TOTAL_AMOUNT"},     
            {"mDataProp":"REMARK"},
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
	$("#chargeAccept-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
            var orderNo = $(this).parent().parent().find('a').text();
            var orderObj=$(this).val()+":"+orderNo;
            //var order = ids.pop();
			ids.push(orderObj);

			$("#chargeIds").val(ids);
		}else{
			if(ids.length != 0){
				ids.splice($.inArray($(this).val(),ids),1);
				$("#chargeIds").val(ids);
			}
		}			
	});	
} );