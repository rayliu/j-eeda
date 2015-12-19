
$(document).ready(function() {

	var deletedTableIds=[];

     //删除一行
    $("#cost_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        costTable.row(tr).remove().draw();
        damageOrder.calcTotalCost();
    }); 

    damageOrder.buildCostDetail=function(){
        var table_rows = $("#cost_table tr");
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
                party_name: $(row.children[5]).find('input').val(),
                fin_method: $(row.children[6]).find('select').val(),
                remark: $(row.children[7]).find('input').val(),
                type: 'cost',
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

    damageOrder.reDrawCostTable=function(order){
        deletedTableIds=[];
        costTable.clear();
        for (var i = 0; i < order.COST_LIST.length; i++) {
            var item = order.COST_LIST[i];
            var item={
                "ID": item.ID,
                "STATUS": item.STATUS,
                "FIN_ITEM": item.FIN_ITEM,
                "AMOUNT": item.AMOUNT,
                "PARTY_TYPE": item.PARTY_TYPE,
                "PARTY_NAME": item.PARTY_NAME,
                "REMARK": item.REMARK
            };
    
            costTable.row.add(item).draw(false);
        }       
    };

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
        "createdRow": function ( row, data, index ) {
            $(row).attr('id', data.ID);
        },
        "columns": [
            {  "width": "70px",
                "render": function ( data, type, full, meta ) {
                    if(full.ID){
                        return '<button type="button" class="delete btn btn-default btn-xs">删除</button> '+
                            '<button type="button" class="btn btn-primary btn-xs">确认</button>';
                    }else{
                        return '<button type="button" class="delete btn btn-default btn-xs">删除</button> ';
                    }
                }
            },
            { "data": "ID", "visible": false },
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
            { "data": "AMOUNT", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" value="'+data+'" class="amount form-control"/>';
                }
            },
            { "data": "PARTY_TYPE",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<select class="form-control search-control">'
                        
                        +'<option >客户</option>'
                        +'<option >收货人</option>'
                        +'<option >其他</option>'
                    +'</select>';
                }
            },
            { "data": "PARTY_NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "FIN_METHOD",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    
                    return '<select class="form-control search-control">'
                        +'<option >正常付款</option>'
                        +'<option >抵扣运费</option>'
                        +'<option >责任方已付款</option>'
                    +'</select>';
                }
            },{ "data": "REMARK",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" value="'+data+'" class="form-control"/>';
                }
            }
        ]
    });

    $('#add_cost').on('click', function(){
        var item={ID: ''};
        
        costTable.row.add(item).draw(false);
    });

    $('#cost_table').on('blur', '.amount', function(){
        damageOrder.calcTotalCost();
    });

    damageOrder.calcTotalCost = function(){
        var table_rows = $("#cost_table tr");
        var total_cost=0;
        for(var index=0; index<table_rows.length; index++){
            var row = table_rows[index];
            var amount = $(row.children[3]).find('input').val();
            if($.isNumeric(amount))
                total_cost +=  parseFloat(amount);
        }
        $("#total_cost").text(total_cost);

        var result = parseFloat($("#total_charge").text()) - parseFloat($("#total_cost").text());
        $('#result').text(result);
        if(result<0){
            $('#result').css('color', 'red');
        }else{
            $('#result').css('color', 'black');
        }
    }

} );