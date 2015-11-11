
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
        "scrollY": "200px",
        "scrollCollapse": true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        //"ajax": "/damageOrder/list",
        "columns": [
            { "data": "ORDER_NO", 
                "render": function ( data, type, full, meta ) {
                    return "<a href='/bzGateOutOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                }
            },
            { "data": "CUSTOMER_NAME"},
            { "data": "PRODUCT_NO"},
            { "data": "SERIAL_NO"},
            { "data": "CREATE_DATE",
                "render": function ( data, type, full, meta ) {
                    return data.substr(0,10);
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
        var order_no = $("#order_no").val();
        var customer_name=$("#customer_name").val();
        
        var beginTime = $("#start_date").val();
        var endTime = $("#end_date").val();
        
        /*  
            查询规则：参数对应DB字段名
            *_no like
            *_id =
            *_status =
            时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/bzGateOutOrder/list?order_no="+order_no
             +"&customer_name="+customer_name
             +"&create_date_begin_time="+beginTime
             +"&create_date_end_time="+endTime;

        dataTable.ajax.url(url).load();
    };
    

} );