
$(document).ready(function() {
	document.title = '出纳日记账查询 | '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));
    
	//datatable, 动态处理
    var accountAuditLogTable = $('#accountAuditLog-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bFilter": false, //不需要默认的搜索框
        "bSort": true, 
        "iDisplayLength": 100,
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/accountAuditLog/list?beginTime="+$("#beginTime_filter").val(),
        "aoColumns": [
            {"mDataProp":null, "sWidth":"20px",
                "fnRender": function(obj) {
                    return '<input type="checkbox"/>' ;
                }
            },
            {"mDataProp":"CREATE_DATE", "sWidth":"180px"},
            {"mDataProp":"SOURCE_ORDER", "sWidth":"150px"},
            {"mDataProp":"ORDER_NO", "sWidth":"150px"},
            {"mDataProp":"BANK_NAME", "sWidth":"150px"},
            {"mDataProp":"CHARGE_AMOUNT", "sWidth":"150px"},
            {"mDataProp":"COST_AMOUNT", "sWidth":"150px" },         
            {"mDataProp":"USER_NAME"}
        ]      
    });

    //accountAuditLogTable.fnSetColumnVis(4, false );

    
    var accountTable = $('#account-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, 
        // "bPaginate": false, //翻页功能
        "bInfo": false,//页脚信息
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			$(nRow).attr('account_id', aData.ACCOUNT_ID);
			return nRow;
		},
    	"sAjaxSource": "/accountAuditLog/accountList?beginTime="+ $("#beginTime_filter").val(),
    	"aoColumns": [   
	        { "mDataProp": null, "sWidth":"30px",
              "fnRender": function(obj) {
                return '<input type="checkbox" name="order_check_box" value="'+obj.aData.ID+'">';
              }
            },
	        {"mDataProp":"BANK_NAME"},
	        {"mDataProp":"MONTH"},
	        {"mDataProp": "INIT_AMOUNT"}, //期初
            {"mDataProp": "TOTAL_CHARGE"}, //本期收入
	        {"mDataProp":"TOTAL_COST"},  //本期支出
            {"mDataProp":"BALANCE_AMOUNT" //本期结余
//                 "fnRender": function(obj) {
//                return obj.aData.INIT_AMOUNT+obj.aData.TOTAL_CHARGE-obj.aData.TOTAL_COST;
//              }
            }, //期末结余
	        {"mDataProp":"REMARK", "bVisible": false}           
	     ]      
    });
    
    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM',  
        viewMode: "months",
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter').trigger('keyup');
        
       	var idArr=[];
   	    $("input[name='order_check_box']").each(function(){
   	    	if($(this).prop('checked') == true){
   	    		idArr.push($(this).parent().parent().attr("account_id"));
   	    	}
   	    });
   	    var ids = idArr.toString();
   		var beginTime = ev.date.getFullYear()+'-'+(ev.date.getMonth()+1);

   		accountTable.fnSettings().sAjaxSource = "/accountAuditLog/accountList?beginTime="+beginTime;
   		accountTable.fnDraw();
   		
   		accountAuditLogTable.fnSettings().sAjaxSource = "/accountAuditLog/list?ids="+ids+"&beginTime="+beginTime;
   		accountAuditLogTable.fnDraw();
    });
    
    $("#account-table").on('click', function(e){
    	var idArr=[];
   	    $("input[name='order_check_box']").each(function(){
   	    	if($(this).prop('checked') == true){
   	    		idArr.push($(this).parent().parent().attr("account_id"));
   	    	}
   	    });
   	    var ids = idArr.toString();
   		var beginTime =$("#beginTime_filter").val();

   		//accountTable.fnSettings().sAjaxSource = "/accountAuditLog/accountList?beginTime="+beginTime;
   		//accountTable.fnDraw();
   		
   		accountAuditLogTable.fnSettings().sAjaxSource = "/accountAuditLog/list?ids="+ids+"&beginTime="+beginTime;
   		accountAuditLogTable.fnDraw();
    });


    $('#datetimepicker3').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime').trigger('keyup');
    });
    
    
    var find = function(){
    	var source_order = $('#source_order').val();
    	var orderNo = $('#orderNo').val();
    	var beginTime = $('#beginTime').val();
    	var endTime = $('#endTime').val();
    	var bankName = $('#bankName').val();
    	var money = $('#money').val();
    	accountAuditLogTable.fnSettings().sAjaxSource = "/accountAuditLog/list?source_order="+source_order
            +"&orderNo="+orderNo
            +"&bankName="+bankName
            +"&money="+money
            +"&begin="+beginTime
            +"&end="+endTime;
       		accountAuditLogTable.fnDraw();
    };
    
    $('#source_order,#orderNo,#beginTime,#endTime,#bankName,#money').on('blur',function(){
    	find();
    });
} );