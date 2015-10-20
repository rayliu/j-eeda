
$(document).ready(function() {
	document.title = ' | '+document.title;

    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));
    
    var cargoTable = $('#cargo-table').DataTable({
        "processing": true,
        "searching": false,
        "paging": false,
        "info": false,
        "autoWidth": true,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "columns": [
            { 
                "render": function ( data, type, full, meta ) {
                  return '<button type="button" class="btn btn-danger btn-xs">删除</button>';
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
   

   // var chargeTable = $('#charge-table').dataTable();

   // var costTable = $('#cost-table').dataTable();
    

} );