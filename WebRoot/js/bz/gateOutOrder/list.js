
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
        //"scrollY": "200px",
        "scrollCollapse": true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "ajax": "/damageOrder/list",
        "serverSide": true,
        "deferLoading": 0, //初次不查数据
        "order": [[ 4, "desc" ]],
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
            { "data": "REMARK"}, 
            { "data": "STATUS", "render": function ( data, type, full, meta ) {
                    if(full.STATUS != "已取消"){
                        return "有效";
                    }
                    return data;
                }},
            { "render": function ( data, type, full, meta ) {
                    if(full.STATUS != "已取消"){
                        return "<button order_id='"+full.ID+"' class='delete btn btn-danger btn-xs'>取消</button>";
                    }
                    return "";
                }
            }
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
        var product_no=$("#product_no").val();
        var serial_no=$("#serial_no").val();
        
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
             +"&product_no="+product_no
             +"&serial_no="+serial_no
             +"&create_date_begin_time="+beginTime
             +"&create_date_end_time="+endTime;

        dataTable.ajax.url(url).load();
    };
    
    $("#eeda-table").on('click', '.delete', function(e){
        e.preventDefault();
        var order_id = $(this).attr('order_id');
        var tr = $(this).parent().parent();

        $.post('/bzGateOutOrder/cancel', {id: order_id}, function(data){
            if(data=='ok'){
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                tr.find('td:nth-child(2)').text('已取消');

            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            }
        }).fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
        });;

        
    }); 

    searchData(); 
} );