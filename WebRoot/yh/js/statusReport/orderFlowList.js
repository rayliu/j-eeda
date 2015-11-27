
$(document).ready(function() {
	document.title = '单据流转查询 | '+document.title;

    $('#menu_report').addClass('active').find('ul').addClass('in');
    
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
            { "data": "TRANSFER_ORDER_NO", 
                "render": function ( data, type, full, meta ) {
                    return "<a href='/damageOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                }
            },
            { "data": null},
            { "data": "PICKUP_ORDER_NO"},
            { "data": "DEPART_ORDER_NO"}, 
            { "data": "DELIVERY_ORDER_NO"}, 
            { "data": "RETURN_ORDER_NO"}, 
            { "data": "CHARGE_ORDER_NO"}, 
            { "data": "COST_ORDER_NO1"},
            { "data": "COST_ORDER_NO2"},
            { "data": "COST_ORDER_NO3"}
        ]
    });

    
    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    })

   var searchData=function(){
        var transfer_order_no = $("#transfer_order_no").val();
        //var customer_id=$("#customer_id").val();
        var pickup_order_no=$("#pickup_order_no").val();
        
        var depart_order_no = $("#depart_order_no").val();
        var delivery_order_no = $('#delivery_order_no').val();
        var return_order_no = $('#return_order_no').val();

        var charge_order_no = $('#charge_order_no').val();

        var cost_order_no = $("#cost_order_no").val();
        
        /*
            查询规则：参数对应DB字段名
            *_no like
            *_id =
            *_status =
            时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/statusReport/orderFlowList?transfer_order_no="+transfer_order_no
             +"&pickup_order_no="+pickup_order_no
             +"&delivery_order_no="+delivery_order_no
             +"&depart_order_no="+depart_order_no
             +"&return_order_no="+return_order_no
             +"&charge_order_no="+charge_order_no
             +"&cost_order_no="+cost_order_no;

        dataTable.ajax.url(url).load();
    };
    

} );