
$(document).ready(function() {
	document.title = '提货入库单查询 | '+document.title;

    $('#menu_status').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));
    
	//datatable, 动态处理
    var transferOrder = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示        
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt",//datatable的中文翻译
            "sProcessing":     "Procesando..."
        },
        "sAjaxSource": "/pickupGateIn/list",
        "aoColumns": [   
            {"mDataProp":"ORDER_NO", "sWidth":"150px",
            	"fnRender": function(obj) {
        			return "<a href='/pickupGateIn/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}
            },
            {"mDataProp":"SP_NAME"},
            {"mDataProp":"PICKUP_MODE",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == 'own'){
            			return '自提';
            		}else if(obj.aData.PICKUP_MODE == 'routeSP'){
            			return '1';
            		}else if(obj.aData.PICKUP_MODE == 'pickupSP'){
            			return '2';
            		}
            	}
            },
            {"mDataProp":"DRIVER"},
            {"mDataProp":"STATUS"},
            {"mDataProp":"AUDIT_STATUS"},
            {"mDataProp":"REMARK"}
        ]  
    });	

    
    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    })

   var searchData=function(){
        var orderNo = $("#order_no").val();
        var sp_id=$("#sp_id").val();
        var beginTime = $("#create_stamp_begin_time").val();
        var endTime = $("#create_stamp_end_time").val();
        /*  
            查询规则：参数对应DB字段名
            *_no like
            *_id =
            *_status =
            时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/pickupGateIn/list?order_no="+orderNo
             +"&sp_id="+sp_id
             +"&begin_time="+beginTime
             +"&end_time="+endTime;

        dataTable.ajax.url(url).load();
    };
    

} );