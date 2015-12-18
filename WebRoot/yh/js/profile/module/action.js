
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
                  return '<a class="remove delete" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i> </a>&nbsp;&nbsp;';
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
                        +'    <option '+(data=='不可见按钮'?'selected':'')+'>不可见按钮</option>'
                        +'</select>';
                }
            },
            { "data": "ACTION_TRIGGER", width: '150px',
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<select class="form-control">'
                        +'    <option '+(data=='点击'?'selected':'')+'>点击</option>'
                        +'</select>';
                }
            },
            { "data": "ACTION_SCRIPT",
                "render": function ( data, type, full, meta ) {
                    var html_detail = '';
                    if(!data){
                        html_detail = '';
                    }else{
                        var command_list = JSON.parse(data);
                        for (var i = 0; i < command_list.length; i++) {
                            var command = command_list[i];
                            html_detail = html_detail + '<li style="margin-top: 5px;">'
                            +'    <a class="remove delete_command" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i> </a>&nbsp;'
                            +'        判断条件 '
                            +'       <input type="text" name="condition" value="' + command.condition + '" class=""/> 成立时执行'
                            +'       <select class="">'
                            +'           <option ' + (command.action == '新增'?'selected':'') +'>新增</option>'
                            +'           <option ' + (command.action == '更新'?'selected':'') +'>更新</option>'
                            +'       </select> 动作, 并执行以下脚本'
                            +'       <textarea class="form-control" rows="3">' + command.condition + '</textarea>'
                            +'    </li>';
                        };
                    }
                    var html = '<input class="add_command btn btn-success btn-xs defineAction" type="button" value="增加命令">'
                            +'<ul>'+ html_detail +'</ul>';
                    return html;
                }
            }
        ]
    };

    var action_table = $('#action_table').DataTable(action_tableSetting);

    var $action_table = $("#action_table");

    //增加一个按钮动作
    $action_table.on('click', '.add_command', function(e){
        e.preventDefault();
        //$('#actionModal').modal('show');
        var html = template('module_action_command_template', 
                        {
                            id: 'sub'
                        }
                    );
        $(this).parent().find('ul').append(html);
    });

    //删除一个按钮命令
    $action_table.on('click', '.delete_command', function(e){
        e.preventDefault();
        //$('#actionModal').modal('show');
        $(this).parent().remove();
    });

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
                action_script: buildActionCommandArray($(row.children[col_index+3])),
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

    
    var buildActionCommandArray=function(commandSection){
        var scriptArray = [];
        var command_list = commandSection.find('li');
        for(var index=0; index<command_list.length; index++){
            var $li = $(command_list[index]);
            var command_obj = {
                condition: $li.find('input[type=text]').val(),
                action: $li.find('select').val(),
                script: $li.find('textarea').val(),
            };
            scriptArray.push(command_obj);
        }
        return JSON.stringify(scriptArray);
    }