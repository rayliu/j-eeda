
$(document).ready(function() {
	document.title = '客户订单状态查询 | '+document.title;

    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');
    
    $("input[name=search_type]").change(function(){
        if (this.value == 'order_no') {
            $('#order_no_div').show();
            $('#serial_no_div').hide();
            
            $('#order_no_table_div').show();
            $('#serial_no_table_div').hide();
        }else{
            $('#order_no_div').hide();
            $('#serial_no_div').show();

            $('#order_no_table_div').hide();
            $('#serial_no_table_div').show();
        }
    });
    
	 
    var dataTable = $('#orderStatus_table').DataTable({
        "processing": true,
        "searching": false,
        //"serverSide": true,
        "scrollX": true,
        //"scrollY": "300px",
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
    
    
    var itemDetailTable = $('#itemDetail-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/departOrder/getItemDetail",
        "aoColumns": [
            {"mDataProp":"ORDER_NO"},
            {"mDataProp":"ITEM_NO"},
            {"mDataProp":"SERIAL_NO"},
            {"mDataProp":"AMOUNT"}        
        ]  
    });	
    
    
    
    $("#eeda-table").on('click', '#edit_detail', function(e){
    	e.preventDefault();	
    	var depart_id = $(this).attr("depart_id");
    	itemDetailTable.fnSettings().sAjaxSource = "/departOrder/getItemDetail?depart_id="+depart_id;
    	itemDetailTable.fnDraw();
    });

    var serial_dataTable = $('#serial_no_table').DataTable({
        "processing": true,
        "searching": false,
        //"serverSide": true,
        "scrollX": true,
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
        var search_type = $("input[name=search_type]:checked").val();
        searchData(search_type); 
    })

   var searchData=function(){
        var orderNo = $("#order_no").val();
        var customer_id=$("#customer_id").val();
        var sp_id=$("#sp_id").val();
        
        var search_type = $("#search_type").val();
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
        var url = "/customerQuery/orderStatusSearch?search_type="+search_type
             +"&order_no="+orderNo
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