
$(document).ready(function() {
	document.title = '货损单查询 | '+document.title;

    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));
    
	  //datatable, 动态处理
    var dataTable = $('#eeda-table').DataTable({
        "processing": true,
        "searching": false,
        //"serverSide": true,
        "scrollX": true,
        "scrollY": "300px",
        "scrollCollapse": true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        //"ajax": "/damageOrder/list",
        "columns": [
            { "data": "ORDER_NO", 
                "render": function ( data, type, full, meta ) {
                    return "<a href='/damageOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                }
            },
            { "data": "CUSTOMER_NAME"},
            { "data": "SP_NAME"},
            { "data": "ORDER_TYPE"}, 
            { "data": "BIZ_ORDER_NO"}, 
            { "data": "PROCESS_STATUS"}, 
            { "data": "ACCIDENT_TYPE"}, 
            { "data": "ACCIDENT_DESC"}, 
            { "data": "ACCIDENT_DATE",
                "render": function ( data, type, full, meta ) {
                    if(data)
                        return data.substr(0,10);
                    return '';
                }
            }, 
            { "data": "REMARK"}
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
        var customer_id=$("#customer_id").val();
        var sp_id=$("#sp_id").val();
        
        var order_type = $("#order_type").val();
        var biz_order_no = $('#biz_order_no').val();
        var process_status = $('#process_status').val();

        var accident_type = $('#accident_type').val();

        var beginTime = $("#start_date").val();
        var endTime = $("#end_date").val();
        
        /*  
            查询规则：参数对应DB字段名
            *_no like
            *_id =
            *_status =
            时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/damageOrder/list?order_no="+orderNo
             +"&customer_id="+customer_id
             +"&sp_id="+sp_id
             +"&biz_order_no="+biz_order_no
             +"&process_status="+process_status
             +"&accident_type="+accident_type
             +"&accident_date_begin_time="+beginTime
             +"&accident_date_end_time="+endTime;

        dataTable.ajax.url(url).load();
    };
    

} );