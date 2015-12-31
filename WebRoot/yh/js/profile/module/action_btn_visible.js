
//按钮行中增加一个按钮显示条件
$action_table.on('click', '.add_btn_visible_condition', function(e){
    e.preventDefault();
    //$('#actionModal').modal('show');
    var html = template('module_action_table_btn_visible_template', 
                    {
                        id: 'sub'
                    }
                );
    $(this).parent().find('ol').append(html);
});


//按钮行中一个按钮显示条件的编辑
$action_table.on('click', 'a[name=edit_btn_visible_condition]', function(e){
    e.preventDefault();
    $('#editBtnVisibleConditionModal').modal('show');

    var li = $(this).parent();
    var tr = $(this).parent().parent().parent().parent();
    $('#editBtnVisibleConditionModal input[name=modal_row_id]').val(tr.attr('id'));
    $('#editBtnVisibleConditionModal input[name=modal_command_li_index]').val(li.index());

    var fieldSetRow = $('#editBtnVisibleConditionModal #modal_add_field_div .row');
    fieldSetRow.empty();

    var modal_form = $('#editBtnVisibleConditionModal #modalForm');
    var command_json = li.find('input[name=edit_btn_visible_condition_json]').val();

    if(command_json){//回显
        var orderFieldList = getModuleFields();

        var condition_list = JSON.parse(command_json);
        for(var i=0; i<condition_list.length; i++){
            var field = condition_list[i];
            
            var display_name = field.key.split(',')[2].split(':')[1];
            
            var html = template('editBtnVisibleConditionModal_add_btn_visible_condition_template', 
                {
                    field_list: orderFieldList,
                    display_name: display_name,
                    operator: field.operator,
                    field_value: field.value
                }
            );
            fieldSetRow.append(html);
            
        }
    }
});

//按钮行中删除按钮显示条件
$action_table.on('click', '.delete', function(e){
    e.preventDefault();
    //$('#actionModal').modal('show');
    $(this).parent().remove();
});

// 添加字段
$('#editBtnVisibleConditionModal').on('click', 'button[name=addField]', function(){
    var orderFieldList = getModuleFields();

    var html = template('editBtnVisibleConditionModal_add_btn_visible_condition_template', 
                    {
                        field_list: orderFieldList
                    }
                );
    $(this).parent().parent().find('.row').append(html);
});

//editBtnVisibleConditionModal 
$('#editBtnVisibleConditionModal #modalForm').on('click', 'a.delete', function(){
    $(this).parent().remove();
});

//editBtnActionModal 点击确定时，回填JSON到 Btn 行
$('#editBtnVisibleConditionModal').on('click', 'button[name=ok_btn]', function(){
    var row_id = $('#editBtnVisibleConditionModal input[name=modal_row_id]').val();
    var row_command_li_index = $('#editBtnVisibleConditionModal input[name=modal_command_li_index]').val();

    var tr = $('#action table tr#'+row_id)[0];
    var command_json_input = $(tr).find('ol li:eq('
        +row_command_li_index+') input[name=edit_btn_visible_condition_json]');

    var form = $('#editBtnVisibleConditionModal #modalForm');

    var conditionList = [];
    var fieldSetRow = $('#editBtnVisibleConditionModal #modal_add_field_div .col-lg-12');
    for(var i=0; i< fieldSetRow.length; i++){
        var row = $(fieldSetRow[i]);
        var key = row.find('select[name=modal_field_name]').val();
        var operator = row.find('select[name=operator]').val();
        var value = row.find('input[name=field_value]').val();
        var obj ={
            key: key,
            operator: operator,
            value: value
        };
        conditionList.push(obj);
    }

    command_json_input.val(JSON.stringify(conditionList));
    $('#editBtnVisibleConditionModal').modal('hide');
});