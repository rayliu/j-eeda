
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
        "paging": false,
        "ordering": false,
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
            { "data": "ACTION_TYPE", width: '90px',
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<select class="form-control">'
                        +'    <option '+(data=='按钮'?'selected':'')+'>按钮</option>'
                        +'    <option '+(data=='不可见按钮'?'selected':'')+'>不可见按钮</option>'
                        +'</select>';
                }
            },
            { "data": "BTN_VISIBLE_CONDITION", width: '200px',
                "render": function ( data, type, full, meta ) {
                  var html_detail = '';
                    if(!data){
                        html_detail = '';
                    }else{
                        var command_list = JSON.parse(data);
                        for (var i = 0; i < command_list.length; i++) {
                            var command = command_list[i];
                            if(!command)
                                continue;

                            var command_setting_str = command;
                            //var obj = JSON.parse(command.command);
                            
                            html_detail = html_detail + '<li style="margin-top: 5px;">'
                            +'    <a class="delete" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i></a>&nbsp;'
                            +'    当<a href="">条件</a>成立时显示'
                            +'    <a name="edit_btn_visible_condition" style="cursor: pointer;"><i class="fa fa-edit"></i></a>'
                            +"    <input name='edit_btn_visible_condition_json' type='hidden' value='"+command_setting_str+"'>"
                            +'    </li>';
                        };
                    }
                    var html = '<input class="add_btn_visible_condition btn btn-success btn-xs defineAction" type="button" value="增加条件">'
                            +'<ol>'+ html_detail +'</ol>';
                    return html;
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
                            if(!command)
                                continue;

                            var command_setting_str = command.command;
                            
                            if(!command_setting_str)
                            	continue;
                            	
                            var obj = JSON.parse(command_setting_str);
                            
                            
                            html_detail = html_detail + '<li style="margin-top: 5px;">'
                            +'    <a class="remove delete_command" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i></a>&nbsp;'
                            +'    当条件 (<strong style="color: green;">' + obj.condition + '</strong>) 成立时执行...'
                            +'    <a name="btnCommandSetting" style="cursor: pointer;"><i class="fa fa-edit"></i></a>'
                            +"    <input name='actionCommandJson' type='text' value='"+command_setting_str+"'>"
                            +'    </li>';
                        };
                    }
                    var html = '<input class="add_command btn btn-success btn-xs defineAction" type="button" value="增加命令">'
                            +'<ol>'+ html_detail +'</ol>';
                    return html;
                }
            }
        ]
    };

    var action_table = $('#action_table').DataTable(action_tableSetting);

    var $action_table = $("#action_table");

    //按钮行中增加一个动作命令
    $action_table.on('click', '.add_command', function(e){
        e.preventDefault();
        //$('#actionModal').modal('show');
        var html = template('module_action_command_template', 
                        {
                            id: 'sub'
                        }
                    );
        $(this).parent().find('ol').append(html);
    });

    //按钮行中一个动作命令的编辑
    $action_table.on('click', 'a[name=btnCommandSetting]', function(e){
        e.preventDefault();
        $('#editBtnActionModal').modal('show');

        var li = $(this).parent();
        var tr = $(this).parent().parent().parent().parent();
        $('#editBtnActionModal input[name=modal_row_id]').val(tr.attr('id'));
        $('#editBtnActionModal input[name=modal_command_li_index]').val(li.index());

        var fieldSetRow = $('#editBtnActionModal #modal_add_field_div .row');
        fieldSetRow.empty();
        $('#editBtnActionModal select[name=table_list]').empty();
        var modal_form = $('#editBtnActionModal #modalForm');
        var command_json = li.find('input[name=actionCommandJson]').val();

        if(command_json){//回显
            commandObj = JSON.parse(command_json);
            modal_form.find('select[name=condition]').val(commandObj.condition);

            if(commandObj.condition == '列表中存在上级单据'){
                for (var i = 0; i < module_obj.STRUCTURE_LIST.length; i++) {
                    var structure = module_obj.STRUCTURE_LIST[i];
                    if(structure.STRUCTURE_TYPE=='列表' && structure.ADD_BTN_TYPE=="弹出列表, 从其它数据表选取"){
                        var ref_structure = structure.ADD_BTN_SETTING_STRUCTURE;
                        $('#editBtnActionModal select[name=table_list]')
                            .append('<option value="'+ref_structure.ID +'">'+ref_structure.NAME+'</option>')
                    }
                };

                $('#editBtnActionModal select[name=table_list]').val(commandObj.structure_id);

                var orderFieldList;
                var table_selected_value = commandObj.structure_id;
                for (var i = 0; i < module_obj.STRUCTURE_LIST.length; i++) {
                    var structure = module_obj.STRUCTURE_LIST[i];
                    if(structure.STRUCTURE_TYPE=='列表' && structure.ADD_BTN_TYPE=="弹出列表, 从其它数据表选取"){
                        var ref_structure = structure.ADD_BTN_SETTING_STRUCTURE;
                        if(table_selected_value == ref_structure.ID){
                            orderFieldList = ref_structure.FIELDS_LIST;
                        }
                    }
                };
                var field_list = commandObj.setValueList;
                for(var i=0; i<field_list.length; i++){
                    var field = field_list[i];
                    for(var key in field){
                        var display_name = key.split(',')[2].split(':')[1];
                        var value = field[key];
                        var html = template('editBtnActionModal_add_field_template', 
                            {
                                field_list: orderFieldList,
                                display_name: display_name,
                                field_value: value
                            }
                        );
                        fieldSetRow.append(html);
                    }
                }
                $('#editBtnActionModal div[name=table_list]').css('display', 'initial');
            }else{
                var orderFieldList = getModuleFields();
                var field_list = commandObj.setValueList;
                for(var i=0; i<field_list.length; i++){
                    var field = field_list[i];
                    for(var key in field){
                        var display_name = key.split(',')[2].split(':')[1];
                        var value = field[key];
                        var html = template('editBtnActionModal_add_field_template', 
                            {
                                field_list: orderFieldList,
                                display_name: display_name,
                                field_value: value
                            }
                        );
                        fieldSetRow.append(html);
                    }
                }
            }

            
        }
    });

    //按钮行中删除一个动作命令
    $action_table.on('click', '.delete_command', function(e){
        e.preventDefault();
        //$('#actionModal').modal('show');
        $(this).parent().remove();
    });

    var deletedActionIds=[];
    //删除表中一个按钮
    $action_table.on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedActionIds.push(tr.attr('id'))

        action_table.row(tr).remove().draw();
    });

    var getModuleFields = function(){
        var orderFieldList;
        for (var i = 0; i < module_obj.STRUCTURE_LIST.length; i++) {
                var structure = module_obj.STRUCTURE_LIST[i];
                if('字段' == structure.STRUCTURE_TYPE && null == structure.PARENT_ID){
                    orderFieldList = structure.FIELDS_LIST;
                    break;
                }
        }
        return orderFieldList;
    };

    //editBtnActionModal 条件变化时
    $('#editBtnActionModal').on('change', 'select[name=condition]', function(){
        var selected_value = $(this).val();
        $('#editBtnActionModal select[name=table_list]').empty();
        if(selected_value == '列表中存在上级单据'){
            for (var i = 0; i < module_obj.STRUCTURE_LIST.length; i++) {
                var structure = module_obj.STRUCTURE_LIST[i];
                if(structure.STRUCTURE_TYPE=='列表' && structure.ADD_BTN_TYPE=="弹出列表, 从其它数据表选取"){
                    var ref_structure = structure.ADD_BTN_SETTING_STRUCTURE;
                    $('#editBtnActionModal select[name=table_list]')
                        .append('<option value="'+ref_structure.ID +'">'+ref_structure.NAME+'</option>')
                }
            };
            $('#editBtnActionModal div[name=table_list]').css('display', 'initial');
        }else{
            $('#editBtnActionModal div[name=table_list]').css('display', 'none');
        }
    });

    //editBtnActionModal 添加字段
    $('#editBtnActionModal').on('click', 'button[name=addField]', function(){
        var selected_value = $('#editBtnActionModal select[name=condition]').val();

        var html;
        if(selected_value == '列表中存在上级单据'){
            var orderFieldList;
            var table_selected_value = $('#editBtnActionModal select[name=table_list]').val();
            for (var i = 0; i < module_obj.STRUCTURE_LIST.length; i++) {
                var structure = module_obj.STRUCTURE_LIST[i];
                if(structure.STRUCTURE_TYPE=='列表' && structure.ADD_BTN_TYPE=="弹出列表, 从其它数据表选取"){
                    var ref_structure = structure.ADD_BTN_SETTING_STRUCTURE;
                    if(table_selected_value == ref_structure.ID){
                        orderFieldList = ref_structure.FIELDS_LIST;
                    }
                }
            };

            html = template('editBtnActionModal_add_field_template', 
                    {
                        field_list: orderFieldList
                    }
                );
        }else{
            var orderFieldList = getModuleFields();

            html = template('editBtnActionModal_add_field_template', 
                    {
                        field_list: orderFieldList
                    }
                );
        }
        
        $(this).parent().parent().find('.row').append(html);
    });

    //editBtnActionModal 添加字段
    $('#editBtnActionModal #modalForm').on('click', 'a.delete', function(){
        $(this).parent().remove();
    });

    //editBtnActionModal 点击确定时，回填JSON到 Btn 行
    $('#editBtnActionModal').on('click', 'button[name=ok_btn]', function(){
        var row_id = $('#editBtnActionModal input[name=modal_row_id]').val();
        var row_command_li_index = $('#editBtnActionModal input[name=modal_command_li_index]').val();

        var tr = $('#action table tr#'+row_id)[0];
        var command_li_condition = $(tr).find('td:eq(4) ol li:eq('
            +row_command_li_index+') strong[name=condition]');
        var command_json_input = $(tr).find('td:eq(4) ol li:eq('
            +row_command_li_index+') input[name=actionCommandJson]');

        var form = $('#editBtnActionModal #modalForm');

        var setValueList = [];
        var fieldSetRow = $('#editBtnActionModal #modal_add_field_div .col-lg-12');
        for(var i=0; i< fieldSetRow.length; i++){
            var row = $(fieldSetRow[i]);
            var key = row.find('select[name=modal_field_name]').val();
            var value = row.find('input[name=field_value]').val();
            var obj ={};
            obj[key] = value;
            setValueList.push(obj);
        }
        var condtion = form.find('select[name=condition]').val();
        var target_structure_id = form.find('select[name=table_list]').val();
        var json_obj = {
            condition: condtion,
            structure_id: target_structure_id,
            setValueList: setValueList
        }

        command_li_condition.text(condtion);
        command_json_input.val(JSON.stringify(json_obj));
        $('#editBtnActionModal').modal('hide');
    });

    var buildActionArray=function(){
        var table_rows = $action_table.find('tr');
        var items_array=[];
        for(var index=0; index<table_rows.length; index++){
            if(index==0)
                continue;


            var row = table_rows[index];

            if($(row).find('td').text() == '表中数据为空')
                continue;

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
                btn_visible_condition: buildBtnVisibleConditionArray($(row.children[col_index+2])),
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

    var buildBtnVisibleConditionArray=function(commandSection){
        var scriptArray = [];
        var command_list = commandSection.find('li');
        for(var index=0; index<command_list.length; index++){
            var $li = $(command_list[index]);
            var visible_condtion = $li.find('input[name=edit_btn_visible_condition_json]').val();
            scriptArray.push(visible_condtion);
        }
        return JSON.stringify(scriptArray);
    }

    var buildActionCommandArray=function(commandSection){
        var scriptArray = [];
        var command_list = commandSection.find('li');
        for(var index=0; index<command_list.length; index++){
            var $li = $(command_list[index]);
            var command_obj = {
                command: $li.find('input[name=actionCommandJson]').val(),
            };
            scriptArray.push(command_obj);
        }
        return JSON.stringify(scriptArray);
    }