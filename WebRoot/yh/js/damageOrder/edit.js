
$(document).ready(function() {

	document.title = order_no + ' | ' + document.title;

    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));

    //------------save
    $('#saveBtn').click(function(e){
        $(this).attr('disabled', true);

        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数据
        if(!$("#orderForm").valid()){
            return;
        }

        var order = {
            id: $('#order_id').val(),
            order_no: $('#order_no').val(),  
            customer_id: $('#customer_id').val(),  
            sp_id: $('#sp_id').val(),

            order_type: $('#order_type').val(),  
            biz_order_no: $('#biz_order_no').val(),  
            process_status: $('#process_status').val(),

            accident_type: $('#accident_type').val(),  
            accident_desc: $('#accident_desc').val(),  
            accident_date: $('#accident_date').val(),

            status: $('#status').val(),
            remark: $('#remark').val()
        };

        console.log(order);

        //异步向后台提交数据
        $.post('/damageOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            console.log(order);
            if(order.ID>0){
                $("#order_no").val(order.ORDER_NO);
                $("#creator_name").val(order.CREATOR_NAME);
                $("#create_date").val(order.CREATE_DATE);
                $("#status").val(order.STATUS);

                contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);

                $('#saveBtn').attr('disabled', false);

                // deletedIds=[];

                // feeTable.fnClearTable();

                // for (var i = 0; i < data.itemList.length; i++) {
                //     var item = data.itemList[i];
                //     feeTable.fnAddData({
                //         ID: item.ID,
                //         CUSTOMER_ORDER_NO: item.CUSTOMER_ORDER_NO,
                //         ITEM_DESC: item.ITEM_DESC,
                //         NAME: item.NAME,
                //         AMOUNT: item.AMOUNT,
                //         CHANGE_AMOUNT: item.CHANGE_AMOUNT,
                //         STATUS: '新建'
                //      });
                // };
                
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
          });
    });

    $('#completeBtn').click(function(){

    });


    //------------事件处理
    var cargoTable = $('#cargo_table').DataTable({
        "processing": true,
        "searching": false,
        "paging": false,
        "info": false,
        "autoWidth": true,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "columns": [
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                  return '<button type="button" class="btn btn-default btn-xs">删除</button> ';
                }
            },
            { "data": "CUSTOMER_NAME", 
                "render": function ( data, type, full, meta ) {
                  return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "ORDER_NO" ,
                "render": function ( data, type, full, meta ) {
                   return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "SP_NAME",
                "render": function ( data, type, full, meta ) {
                   return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "STATUS",
                "render": function ( data, type, full, meta ) {
                   return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            }
        ]
    });

    $('#add_cargo').on('click', function(){
        var item={
            "CUSTOMER_NAME": 1,
            "ORDER_NO": 2,
            "SP_NAME": 3,
            "STATUS": 4
        };
        
        cargoTable.row.add(item).draw(false);
    });

    //--------------------------------------------------------
    var chargeTable = $('#charge_table').DataTable({
        "processing": true,
        "searching": false,
        "paging": false,
        "info": false,
        "autoWidth": true,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "columns": [
            {  "width": "60px",
                "render": function ( data, type, full, meta ) {
                  return '<button type="button" class="btn btn-default btn-xs">删除</button> '+
                            '<button type="button" class="btn btn-primary btn-xs">确认</button>';
                }
            },
            { "data": "CUSTOMER_NAME", 
                "render": function ( data, type, full, meta ) {
                  return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "ORDER_NO" ,
                "render": function ( data, type, full, meta ) {
                   return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "SP_NAME",
                "render": function ( data, type, full, meta ) {
                   return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "STATUS",
                "render": function ( data, type, full, meta ) {
                   return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            }
        ]
    });

    $('#add_charge').on('click', function(){
        var item={
            "CUSTOMER_NAME": 1,
            "ORDER_NO": 2,
            "SP_NAME": 3,
            "STATUS": 4
        };
        
        chargeTable.row.add(item).draw(false);
    });
   //--------------------------------------------------------
    var costTable = $('#cost_table').DataTable({
        "processing": true,
        "searching": false,
        "paging": false,
        "info": false,
        "autoWidth": true,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "columns": [
            {  "width": "60px",
                "render": function ( data, type, full, meta ) {
                  return '<button type="button" class="btn btn-default btn-xs">删除</button> '+
                            '<button type="button" class="btn btn-primary btn-xs">确认</button>';
                }
            },
            { "data": "CUSTOMER_NAME", 
                "render": function ( data, type, full, meta ) {
                  return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "ORDER_NO" ,
                "render": function ( data, type, full, meta ) {
                   return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "SP_NAME",
                "render": function ( data, type, full, meta ) {
                   return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "STATUS",
                "render": function ( data, type, full, meta ) {
                   return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            }
        ]
    });

    $('#add_cost').on('click', function(){
        var item={
            "CUSTOMER_NAME": 1,
            "ORDER_NO": 2,
            "SP_NAME": 3,
            "STATUS": 4
        };
        
        costTable.row.add(item).draw(false);
    });
   // var chargeTable = $('#charge-table').dataTable();

   // var costTable = $('#cost-table').dataTable();
    

} );