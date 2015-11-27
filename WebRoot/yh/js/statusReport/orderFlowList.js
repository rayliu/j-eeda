
$(document).ready(function() {
	document.title = '单据流转查询 | '+document.title;

    $('#menu_report').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));
    
	  //datatable, 动态处理
    var dataTable = $('#eeda-table').DataTable({
        "processing": true,
        "searching": false,
        "paging": true, //翻页功能
        "pageLength": 10,
        "aLengthMenu": [ [10 ,25 ,50 ,100 ,9999999], [10 ,25 ,50 ,100, "All"]],
        "scrollX": true,
        "scrollY": "500px",
        "scrollCollapse": true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        //"sAjaxSource": "/statusReport/orderFlowList",
        "columns": [
            { "data": "TRANSFER_ORDER_NO", 
                "render": function ( data, type, full, meta ) {
                    return "<a href='/damageOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                }
            },
            { "data": "CUSTOMER_NAME","width": "10%"},
            { "data": "PICKUP_ORDER_NO", "width": "10%"},
            { "data": "DEPART_ORDER_NO", "width": "10%"}, 
            { "data": "DELIVERY_ORDER_NO", "width": "10%"}, 
            { "data": "RETURN_ORDER_NO", "width": "10%"}, 
            { "data": "CHARGE_ORDER_NO", "width": "10%"}, 
            { "data": "COST_ORDER_NO1", "width": "10%"},
            { "data": "COST_ORDER_NO2", "width": "10%"},
            { "data": "COST_ORDER_NO3", "width": "10%"}
        ]
    });

    
    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    });

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