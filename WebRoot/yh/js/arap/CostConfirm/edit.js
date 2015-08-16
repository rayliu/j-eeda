$(document).ready(function() {
	document.title = '付款确认单| '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var invoiceApplicationOrderIds = $("#invoiceApplicationOrderIds").val();
    var total = 0.00;
    var datatable=$('#InvorceApplication-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costConfirm/applicationList?invoiceApplicationOrderIds="+invoiceApplicationOrderIds,
        "aoColumns": [   
             {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/costPreInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"PAY_AMOUNT",
    			"fnRender": function(obj) {
    				total = total + parseInt(obj.aData.PAY_AMOUNT) ;
    				$("#total").html(total);
    				$("#nopay_amount").val(total);
    				$("#pay_amount").val(total);
    				$("#total_amount").val(total);
    				return obj.aData.PAY_AMOUNT;
    			}
        	},
            {"mDataProp":"COST_STAMP"},
            {"mDataProp":null,"sWidth": "150px"},
            {"mDataProp":null,"sWidth": "150px"},
            {"mDataProp":null,"sWidth": "150px"}                       
        ]      
    });	
    
    
    
    
  
    
  //供应商查询
    //获取供应商的list，选中信息在下方展示其他信息
    $('#pay_bank').on('input click', function(){
    	var me = this;
		var inputStr = $('#pay_bank').val();
		var bankList =$("#bankList");
		bankList.empty();
		$.get('/costConfirm/searchAllAccount', {input:inputStr}, function(data){
			if(data.length > 0){
				 var accountTypeSelect = $("#accountTypeSelect");
				 accountTypeSelect.empty();
				 var hideAccountId = $("#hideAccountId").val();
				 accountTypeSelect.append("<option ></option>");
				 for(var i=0; i<data.length; i++){
					 //accountTypeSelect.append("<option value='"+data[i].ID+"' selected='selected'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");
					 //bankList.append("<li><a id='"+data[i].ID+"' >"+data[i].BANK_PERSON+" "+data[i].BANK_NAME+" "+data[i].ACCOUNT_NO+"</a></li>");
					bankList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' bank_name='"+data[i].BANK_NAME+"' bank_person='"+data[i].BANK_PERSON+"' account_no='"+data[i].ACCOUNT_NO+"', >"+data[i].BANK_PERSON+" "+data[i].BANK_NAME+" "+data[i].ACCOUNT_NO+"</a></li>");
				 }
			}
			bankList.css({ 
	        	left:$(me).position().left+"px", 
	        	top:$(me).position().top+32+"px" 
	        }); 
			
			bankList.show();
			
		},'json');
    });
    
    // 没选中供应商，焦点离开，隐藏列表
	$('#pay_bank').on('blur', function(){
 		$('#bankList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#bankList').on('blur', function(){
 		$('#bankList').hide();
 	});

	$('#bankList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});

	// 选中供应商
	$('#bankList').on('mousedown', '.fromLocationItem', function(e){
		console.log($('#bankList').is(":focus"));
		var message = $(this).text();
		$('#pay_bank').val(message.substring(0, message.indexOf(" ")));
	    $('#pay_bank').val($(this).attr('bank_name'));
	    $('#pay_account_no').val($(this).attr('account_no'));
        $('#bankList').hide();
        refreshData();
    });
    
	$("#pay_type").on('mouseup click',function(){
		if($("#pay_type").val() == 'cash'){
			//$("#pay_bank").attr("readonly",true);
//			$("#pay_bank").hide();
//			 $('#payee_no').hide();
			
		};
	});
	
	
	$("#saveBtn").on('click',function(){
		$.get('/costConfirm/save',$("#confirmForm").serialize(), function(data){
			if(data.ID>0){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#confirmId").val(data.ID);
				$("#saveBtn").attr("disabled", true);
				$("#savePayConfirmBtn").attr("disabled", false);
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_FALSE);
			}
		},'json');
	});
	
	$("#savePayConfirmBtn").on('click',function(){
		if($("#pay_amount").val()>$("#nopay_amount").val()){
			$.scojs_message('付款金额不可以大于待付金额！！', $.scojs_message.TYPE_FALSE);
			return false;
		}
		
		
		$.get('/costConfirm/saveConfirmLog',$("#confirmForm").serialize(), function(data){
			if(data.arapCostPayConfirmOrderLog.ID>0){
				logTable.fnSettings().sAjaxSource="/costConfirm/logList?confirmId="+$("#confirmId").val();
				logTable.fnDraw();	
				
				$("#pay_amount").val($("#total_amount").val()-data.re.TOTAL);
				$("#nopay_amount").val($("#total_amount").val()-data.re.TOTAL);
				if($("#nopay_amount").val()<=0){
					$("#savePayConfirmBtn").attr("disabled", true);
				}else{
					$("#savePayConfirmBtn").attr("disabled", false);
				}
			}else{
				$.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
			}
		},'json');
		
	});
	
	
	var logTable = $('#log-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costConfirm/logList?confirmId="+$("#confirmId").val(),
        "aoColumns": [  
            {"mDataProp":"ID"},
            {"mDataProp":"PAY_OUT_BANK_NAME"},
            {"mDataProp":"PAY_OUT_ACCOUNT_NO"},
            {"mDataProp":"PAY_TYPE",
            	"fnRender": function(obj) {
    				if(obj.aData.PAY_TYPE=='cash'){
    					return '现金';
    				}else if(obj.aData.PAY_TYPE=='transfers'){
    					return '转账';
    				}else{
    					return '';
    				}
            	}
            },
            {"mDataProp":"AMOUNT"},
            {"mDataProp":"CREATE_DATE"},
            {"mDataProp":"C_NAME"}
        ]      
	});
});