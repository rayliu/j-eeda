
$(document).ready(function() {

    $('#menu_finance').addClass('active').find('ul').addClass('in');
	//datatable, 动态处理
    var accountAuditLogTable = $('#accountAuditLog-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/accountAuditLog/list",
        "aoColumns": [   
            {"mDataProp":"CREATE_DATE"},
            {"mDataProp":null,
	            "fnRender": function(obj) {
	            	return '开票记录单';
	            }
            },
            {"mDataProp":"INVOICE_ORDER_NO"},
            {"mDataProp":null,
	            "fnRender": function(obj) {
	            	var str = "";
	            	if(obj.aData.PAYMENT_METHOD == "transfers"){
	            		str = "转账";
	            	}else{
	            		str = "现金";
	            	}
	            	return str;
	            }
            },
            {"mDataProp":"AMOUNT"},
            {"mDataProp":"REMARK"},           
            {"mDataProp":"USER_NAME"},        	
            {"mDataProp":"CREATE_DATE"}
        ]      
    });
    
    var accountTable = $('#account-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, 
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
    	"sAjaxSource": "/accountAuditLog/accountList",
    	"aoColumns": [   
	        { "mDataProp": null, "sWidth":"30px",
              "fnRender": function(obj) {
                return '<input type="checkbox" name="order_check_box" value="'+obj.aData.ID+'" checked="">';
              }
            },
	        {"mDataProp":"BANK_PERSON"},
	        {"mDataProp":null},
	        {"mDataProp":"AMOUNT"},        	
	        {"mDataProp":null},
	        {"mDataProp":"REMARK"}           
	     ]      
    });
    
    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter').trigger('keyup');
    });


    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter').trigger('keyup');
    });
    
    $("#searchBtn").click(function(e){
    	e.preventDefault();
		var idArr=[];
	    $("input[name='order_check_box']").each(function(){
	    	if($(this).prop('checked') == true){
	    		idArr.push($(this).val());
	    	}
	    });
	    var ids = idArr.toString();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	accountAuditLogTable.fnSettings().sAjaxSource = "/accountAuditLog/list?ids="+ids+"&beginTime="+beginTime+"&endTime="+endTime;
    	accountAuditLogTable.fnDraw();
    });
} );