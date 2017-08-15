
$(document).ready(function() {

	var deletedTableIds=[];

     //删除一行
    $("#cost_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        costTable.row(tr).remove().draw();
    }); 
    

    order.buildCostDetail=function(){
    	var cargo_table_rows = $("#cost_table tr");
        var cargo_items_array=[];
        for(var index=0; index<cargo_table_rows.length; index++){
            if(index==0)
                continue;

            var row = cargo_table_rows[index];
            var empty = $(row).find('.dataTables_empty').text();
            if(empty)
            	continue;
            
            var id = $(row).attr('id');
            if(!id){
                id = '';
            }
            
            var item={}
            item.id = id;
           
            for(var i = 1; i < row.childNodes.length; i++){
            	var name = $(row.childNodes[i]).find('input,select').attr('name');
            	var value = $(row.childNodes[i]).find('input,select').val();
            	if(name){
            		item[name] = value;
            	}
            }
            item.action = id.length > 0?'UPDATE':'CREATE';
            cargo_items_array.push(item);
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
        deletedTableIds = [];
        return cargo_items_array;
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
                    return '<button type="button" class="delete btn btn-default btn-xs">删除</button> ';
                }
            },
            { "data": "FIN_ITEM_ID",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                	
                	var str=$("#paymentItemList").html();
                	if(data!=''){
                		return "<input type='hidden' name='fin_item_id' value='"+data+"'/>"+full.FIN_ITEM_NAME;
                	}else{
                		return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
                	}
                }
            },
            { "data": "AMOUNT",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="amount" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "REMARK",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="remark" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "FIN_ITEM_NAME", "visible":false,
            	 "render": function ( data, type, full, meta ) {
            		 return '';
        		 }
        	 }
        ]
    });
    
    order.refleshCostTable = function(order_id){
    	var url = "/pickupGateIn/tableList?order_id="+order_id;
    	costTable.ajax.url(url).load();
    }

    $('#add_cost').on('click', function(){
        var item={ID: ''};
        
        costTable.row.add(item).draw(false);
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
        "createdRow": function ( row, data, index ) {
            $(row).attr('id', data.ID);
        },
        "columns": [
            { "data": "ORDER_NO"},
            { "data": "ABBR"},
            { "data": "PLANNING_TIME"},
            { "data": "ITEM_NO","width":"200px"},
            { "data": "ITEM_NAME"},
            { "data": "AMOUNT"},
            { "data": "REMARK"}
        ]
    });


} );