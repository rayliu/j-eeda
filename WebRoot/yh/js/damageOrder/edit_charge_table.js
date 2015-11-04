
$(document).ready(function() {

    var deletedTableIds=[];

     //删除一行
    $("#charge_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        tr.remove();
    }); 

    damageOrder.buildChargeDetail=function(){
        var table_rows = $("#charge_table tr");
        var items_array=[];
        for(var index=0; index<table_rows.length; index++){
            if(index==0)
                continue;

            var row = table_rows[index];
            var id = $(row).attr('id');
            if(!id){
                id='';
            }

            var item={
                id: id,
                status: $(row.children[1]).text(), 
                fin_item: $(row.children[2]).find('input').val(),
                amount: $(row.children[3]).find('input').val(),
                party_type: $(row.children[4]).find('select').val(),
                name: $(row.children[5]).find('input').val(),
                remark: $(row.children[6]).find('input').val(),
                type: 'charge',
                action: $('#order_id').val().length>0?'UPDATE':'CREATE'
            };
            items_array.push(item);
        }

        //add deleted items
        for(var index=0; index<deletedTableIds.length; index++){
            var id = deletedTableIds[index];
            var item={
                id: id,
                action: 'DELETE'
            };
            items_array.push(item);
        }
        return items_array;
    };

    damageOrder.reDrawChargeTable=function(order){
        deletedTableIds=[];
        chargeTable.clear();
        for (var i = 0; i < order.CHARGE_LIST.length; i++) {
            var item = order.CHARGE_LIST[i];
            var item={
                "ID": item.ID,
                "STATUS": item.STATUS,
                "FIN_ITEM": item.FIN_ITEM,
                "AMOUNT": item.AMOUNT,
                "PARTY_TYPE": item.PARTY_TYPE,
                "NAME": item.NAME,
                "REMARK": item.REMARK
            };
    
            chargeTable.row.add(item).draw(false);
        }       
    };
	
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
        "createdRow": function ( row, data, index ) {
            $(row).attr('id', data.ID);
        },
        "columns": [
            {  "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(full.ID){
                        return '<button type="button" class="btn btn-default btn-xs">删除</button> '+
                            '<button type="button" class="btn btn-primary btn-xs">确认</button>';
                    }else{
                        return '<button type="button" class="btn btn-default btn-xs">删除</button> ';
                    }
                }
            },
            { "data": "STATUS", "width": "50px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='未确认';
                    return data;
                }
            },
            { "data": "FIN_ITEM" ,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "AMOUNT",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "PARTY_TYPE",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    
                    return '<select class="form-control search-control">'
                        +'<option >供应商</option>'
                        +'<option >客户</option>'
                        +'<option >保险公司</option>'
                        +'<option >其他</option>'
                    +'</select>';
                }
            },
            { "data": "CHARGE_TYPE",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    
                    return '<select class="form-control search-control">'
                        +'<option >正常收款</option>'
                        +'<option >抵扣运费</option>'
                    +'</select>';
                }
            },
            { "data": "NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" value="'+data+'" class="form-control"/>';
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

    $('#add_charge').on('click', function(){
        var item={};
        
        chargeTable.row.add(item).draw(false);
    });
   

} );