
    var subIndex=0;
   

    $('#addActionBtn').on('click', function(event) {
        var item={
            "ID": '',
            "ACTION_NAME": '',
            "ACTION_TYPE": '',
            "ACTION_DESC": ''
        };
        action_table.row.add(item).draw(false);
    });
    //-------------   子表的动态处理

    var action_tableSetting = {
        paging: false,
        "info": false,
        "processing": true,
        "searching": false,
        "autoWidth": true,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "createdRow": function ( row, data, index ) {
            $(row).attr('id', data.ID);
        },
        //"ajax": "/damageOrder/list",
        "columns": [
            { "width": "30px", "orderable":false, 
                "render": function ( data, type, full, meta ) {
                  return '<a class="remove delete" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i> </a>&nbsp;&nbsp;'+
                    '<a class="remove delete" href="javascript:void(0)" title="编辑"><i class="glyphicon glyphicon-edit"></i> </a>';
                }
            },
            { "data": "ID", visible: false},
            { "data": "ACTION_NAME", width: '150px',
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<input type="text" value="'+data+'" class="product_no form-control"/>';
                }
            },
            { "data": "ACTION_TYPE", width: '150px',
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<select class="form-control">'
                        +'    <option '+(data=='按钮'?'selected':'')+'>按钮</option>'
                        +'    <option '+(data=='打开单据时'?'selected':'')+'>打开单据时</option>'
                        +'    <option '+(data=='数据触发'?'selected':'')+'>数据触发</option>'
                        +'</select>';
                }
            },
            { "data": "ACTION_TRIGGER", width: '150px',
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<select class="form-control">'
                        +'    <option '+(data=='点击'?'selected':'')+'>点击</option>'
                        +'    <option '+(data=='数据条件'?'selected':'')+'>数据条件</option>'
                        +'</select>';
                }
            },
            { "data": "ACTION_DESC",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<textarea class="form-control" rows="3">本单据.单据状态="新建"</textarea>';
                }
            }
        ]
    };

    var action_table = $('#action_table').DataTable(action_tableSetting);

    var $action_table = $("#action_table");

    var deletedActionIds=[];
    //删除表中一行
    $action_table.on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedActionIds.push(tr.attr('id'))

        action_table.row(tr).remove().draw();
    });

    var buildActionArray=function(){
        var table_rows = $action_table.find('tr');
        var items_array=[];
        for(var index=0; index<table_rows.length; index++){
            if(index==0)
                continue;

            var row = table_rows[index];
            var id = $(row).attr('id');
            if(!id){
                id='';
            }

            var col_index= 1;
            var item={
                id: id,
                //field_name: $(row.children[2]).find('input').val(), 
                action_name: $(row.children[col_index]).find('input').val(), 
                action_type: $(row.children[col_index+1]).find('select').val(),
                action_trigger: $(row.children[col_index+2]).find('select').val(),
                action_script: $(row.children[col_index+3]).find('textarea').val(),
                action: $('#module_id').val().length>0?'UPDATE':'CREATE'
            };

            if(item.action_name.length>0){
                items_array.push(item);
            }
        }

        //add deleted items
        for(var index=0; index<deletedActionIds.length; index++){
            var id = deletedActionIds[index];
            var item={
                id: id,
                action: 'DELETE'
            };
            items_array.push(item);
        }
        return items_array;
    };

