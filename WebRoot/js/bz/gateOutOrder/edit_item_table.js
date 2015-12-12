
$(document).ready(function() {

    var deletedTableIds=[];

    //删除一行
    $("#cargo_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 


    $("#cargo_table").on('keyup', '.product_no', function(e){
        e.preventDefault();

        if( $(this).val().length>0 && e.keyCode == 13){
            var rowsCount = cargoTable.data().length;
            var tr = $(this).parent().parent();
            var row_index = cargoTable.row(tr).index();

            var cargo_table_rows = $("#cargo_table tbody tr");
            var next_row = cargo_table_rows[row_index+1];
            if(next_row){
                $(next_row).find('.product_no').focus();
            }else{
                $('#add_cargo').click();
                cargo_table_rows = $("#cargo_table tbody tr");
                next_row = cargo_table_rows[row_index+1];
                $(next_row).find('.product_no').focus();
            }
            
        }
    }); 

    $("#cargo_table").on('keyup', '.serial_no', function(e){
        e.preventDefault();

        if( $(this).val().length>0 && e.keyCode == 13){
            var rowsCount = cargoTable.data().length;
            var tr = $(this).parent().parent();
            var row_index = cargoTable.row(tr).index();

            var cargo_table_rows = $("#cargo_table tbody tr");
            var next_row = cargo_table_rows[row_index+1];
            if(next_row){
                $(next_row).find('.serial_no').focus();
            }else{
                $('#add_cargo').click();
                cargo_table_rows = $("#cargo_table tbody tr");
                next_row = cargo_table_rows[row_index+1];
                $(next_row).find('.serial_no').focus();
            }
            
        }
    }); 

    orderController.buildCargoDetail=function(){
        var cargo_table_rows = $("#cargo_table tr");
        var cargo_items_array=[];
        for(var index=0; index<cargo_table_rows.length; index++){
            if(index==0)
                continue;

            var row = cargo_table_rows[index];
            var id = $(row).attr('id');
            if(!id){
                id='';
            }

            var item={
                id: id,
                product_no: $(row.children[2]).find('input').val(), 
                serial_no: $(row.children[3]).find('input').val(),
                remark: $(row.children[4]).find('input').val(),
                action: $('#order_id').val().length>0?'UPDATE':'CREATE'
            };

            if(item.product_no.length>0 
                || item.serial_no.length>0
                || item.remark.length>0){
                cargo_items_array.push(item);
            }
        }

        //add deleted items
        for(var index=0; index<deletedTableIds.length; index++){
            var id = deletedTableIds[index];
            var item={
                id: id,
                action: 'DELETE'
            };
            cargo_items_array.push(item);
        }
        return cargo_items_array;
    };

    orderController.reDrawCargoTable=function(order){
        deletedTableIds=[];
        cargoTable.clear();
        for (var i = 0; i < order.ITEM_LIST.length; i++) {
            var item = order.ITEM_LIST[i];
            var item={
                "ID": item.ID,
                "PRODUCT_NO": item.PRODUCT_NO,
                "SERIAL_NO": item.SERIAL_NO,
                "REMARK": item.REMARK
            };
    
            cargoTable.row.add(item).draw(false);
        }       
    };

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
        "createdRow": function ( row, data, index ) {
            $(row).attr('id', data.ID);
            $('td:eq(1)',row).html(index+1);
        },
        "columns": [
            { "width": "30px", "orderable":false, 
                "render": function ( data, type, full, meta ) {
                  return '<button type="button" class="delete btn btn-default btn-xs">删除</button> ';
                }
            },
            { "data": "ID", "visible":false},
            { "data": "PRODUCT_NO", "orderable":false},
            { "data": "PRODUCT_NO",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<input type="text" value="'+data+'" class="product_no form-control"/>';
                }
            },
            { "data": "SERIAL_NO" ,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                   return '<input type="text" value="'+data+'" class="serial_no form-control"/>';
                }
            },
            { "data": "REMARK",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                   return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            }
        ]
    });

    $('#add_cargo').on('click', function(){
        var item={
            "ID": '',
            "PRODUCT_NO": '',
            "SERIAL_NO": '',
            "REMARK": ''
        };
        cargoTable.row.add(item).draw(false);
    });

    $('#add_cargo').click();
} );