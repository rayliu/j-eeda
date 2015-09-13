
$(document).ready(function() {
	document.title = '出纳日记账查询 | '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');
	//datatable, 动态处理
    var accountAuditLogTable = $('#accountAuditLog-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/accountAuditLog/list",
        "aoColumns": [   
            {"mDataProp":"CREATE_DATE", "sWidth":"80px"},
            {"mDataProp":"SOURCE_ORDER"},
            {"mDataProp":"ORDER_NO"},
            {"mDataProp":"BANK_NAME"},
            {"mDataProp":null, "sWidth":"80px", 
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
            {"mDataProp":null, "sWidth":"80px",
                "fnRender": function(obj) {
                    var paymentType='';
                    if(obj.aData.PAYMENT_TYPE == "CHARGE"){
                        paymentType='<span style="color:green;">'+ (Number(obj.aData.AMOUNT))+'</span>';
                    }
                    return paymentType ;
                }
                 
            },
            {"mDataProp":null, "sWidth":"80px",
                "fnRender": function(obj) {
                    var paymentType='';
                    console.log()
                    if(obj.aData.PAYMENT_TYPE == "COST"){
                        paymentType='<span style="color:red;">'+ (Number(obj.aData.AMOUNT))+'</span>';
                    }
                    return paymentType ;
                }
                 
            },
            {"mDataProp":"REMARK"},           
            {"mDataProp":"USER_NAME"}
        ]      
    });
    
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
			return nRow;
		},
    	"sAjaxSource": "/accountAuditLog/accountList",
    	"aoColumns": [   
	        { "mDataProp": null, "sWidth":"30px",
              "fnRender": function(obj) {
                return '<input type="checkbox" name="order_check_box" value="'+obj.aData.ID+'" checked="">';
              }
            },
	        {"mDataProp":"BANK_NAME"},
	        {"mDataProp": "INIT_AMOUNT"}, //期初
            {"mDataProp": "TOTAL_CHARGE"}, //本期收入
	        {"mDataProp":"TOTAL_COST"},  //本期支出
            {"mDataProp":null, //本期结余
                 "fnRender": function(obj) {
                return obj.aData.INIT_AMOUNT+obj.aData.TOTAL_CHARGE-obj.aData.TOTAL_COST;
              }
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
    });


    // $('#datetimepicker2').datetimepicker({  
    //     format: 'yyyy-MM-dd',  
    //     language: 'zh-CN', 
    //     autoclose: true,
    //     pickerPosition: "bottom-left"
    // }).on('changeDate', function(ev){
    //     $(".bootstrap-datetimepicker-widget").hide();
    //     $('#endTime_filter').trigger('keyup');
    // });
    
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