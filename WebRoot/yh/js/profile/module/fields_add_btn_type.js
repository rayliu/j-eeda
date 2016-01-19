
var modal_module_list;

var current_section;

var addBtnSettingClick = function(btn){
    var $select = $(btn).parent().find('select');
    current_section = $select.parent().parent().parent();
    var s_id = current_section.find('.s_id').val();
    var s_name = current_section.find('.s_name').val();
    var s_add_btn_setting = current_section.find('input[name=s_add_btn_setting]').val();

    $('#modal_module_source').empty();

    getModuleList(function(){
        if('弹出列表, 从其它数据表选取' == $select.val()){
            if(s_add_btn_setting == ''){
                $('#modal_s_id').val(s_id);
                $('#modal_structure_name').val(s_name);
                
                
                $('#editAddBtnType').modal('show');
                

            }else{//回显
                var settingJson = JSON.parse(s_add_btn_setting);
                var col_list_div = $('#modal_add_col_div section .row').empty();
                var condition_list_div =$('#modal_add_condition_div section .row').empty();
                var fillback_list_div =$('#modal_add_fillback_div section .row').empty();
                $('#modal_s_id').val(settingJson.structure_id);
                $('#modal_structure_name').val(s_name);

                //显示字段的回显
                var col_row_list = settingJson.col_list;
                for (var i = 0; i < col_row_list.length; i++) {
                    var col_row = col_row_list[i];//显示字段
                    var col_field = JSON.parse(col_row.field_name);
                    
                    var display_name = col_field.FIELD_DISPLAY_NAME;
                    var module = getModuleStructure(col_field.STRUCTURE_ID);
                    var html = template('table_add_btn_field_template', 
                        {field_list: module.FIELD_LIST, 
                         display_name: display_name,
                         name: col_row.customize_name,
                         sql_name: col_row.customize_sql});
                    col_list_div.append(html);
                };

                var condition_row_list = settingJson.condition_list;
                for (var i = 0; i < condition_row_list.length; i++) {
                    var col_row = condition_row_list[i];
                    var col_field = JSON.parse(col_row.field_name);
                    
                    var display_name = col_field.FIELD_DISPLAY_NAME;
                    var module = getModuleStructure(col_field.STRUCTURE_ID);
                    var html = template('table_add_btn_condtion_template', 
                        {field_list: module.FIELD_LIST, 
                         display_name: display_name,
                         condition: col_row.condition,
                         condition_value: col_row.condition_value});
                    condition_list_div.append(html);
                };

                // var fillback_row_list = settingJson.fillback_list;
                // for (var i = 0; i < fillback_row_list.length; i++) {
                //     var col_row = fillback_row_list[i];
                //     var structure_id = col_row.from_field.split(',')[0].split(':')[1];
                //     var display_name = col_row.to_field.split(',')[2].split(':')[1];
                //     var module = getModuleStructure(structure_id);
                //     var html = template('table_add_btn_field_template', 
                //         {field_list: module.FIELD_LIST, 
                //          display_name: display_name,
                //          name: col_row.customize_name,
                //          sql_name: col_row.customize_sql});
                //     fillback_list_div.append(html);
                // };

                $('#editAddBtnType').modal('show');
            }
        }
    });
    
};

var getModuleList=function(callback){
    $.post("/module/getActiveModules", function(json){
        if(json){
            modal_module_list = json;
            for (var i = 0; i < json.length; i++) {
                var module = json[i];
                $('#modal_module_source').append('<option value="'+module.STRUCTURE_ID+'">'+module.MODULE_NAME+'</option>');
            };
            $('#modal_module_source').append('<option>自定义SQL</option>')
            callback();
        }
    });
}
var getModuleStructure=function(structure_id){
    for (var i = 0; i < modal_module_list.length; i++) {
        var module = modal_module_list[i];
        if(structure_id == module.STRUCTURE_ID){
            return module;
        }
    }
};

$('.addColField').click(function(event) {
    var structrue_id = $('#modal_module_source').val();
    for (var i = 0; i < modal_module_list.length; i++) {
        var module = modal_module_list[i];
        if(structrue_id == module.STRUCTURE_ID){
            var html = template('table_add_btn_field_template', {field_list: module.FIELD_LIST});
            var div = $('.addColField').parent();
            div.find('.row').append(html);
            break;
        }
    }
});

$('#modal_add_col_div').on('change', 'select[name=modal_col_field_name]', function(e){
    var row = $(this).parent();
    if($(this).val() == '自定义'){
        
        row.find('input[name=name]').css('display', 'initial');
        row.find('input[name=sql_name]').css('display', 'initial');
    }else{
        row.find('input[name=name]').css('display', 'none');
        row.find('input[name=sql_name]').css('display', 'none');
    }
});

$('#modal_add_col_div').on('click', '.delete', function(e){
    $(this).parent().remove();
});

$('.addConditionField').click(function(event) {
    var structrue_id = $('#modal_module_source').val();
    for (var i = 0; i < modal_module_list.length; i++) {
        var module = modal_module_list[i];
        if(structrue_id == module.STRUCTURE_ID){
            var html = template('table_add_btn_condtion_template', {field_list: module.FIELD_LIST});
            var div = $('.addConditionField').parent();
            div.find('.row').append(html);
            break;
        }
    }
});

$('#modal_add_condition_div').on('click', '.delete', function(e){
    $(this).parent().remove();
});

$('.addFillbackField').click(function(event) {
    var module_name = $('#modal_module_source').val();
    for (var i = 0; i < modal_module_list.length; i++) {
        var module = modal_module_list[i];
        if(module_name == module.MODULE_NAME){
            var html = template('table_add_btn_fillback_template', {field_list: module.FIELD_LIST});
            var div = $('.addFillbackField').parent();
            div.find('.row').append(html);
            break;
        }
    }
});

$('#modal_add_fillback_div').on('click', '.delete', function(e){
    $(this).parent().remove();
});

$('#modal_table_add_btn_type_ok_btn').click(function(event) {
    var settingObj={
        structure_id: $('#modal_module_source').val(),
        col_list: [],
        condition_list: [],
        fillback_list: []
    };

    var col_list = [];
    var col_row_list = $('#modal_add_col_div section .col-lg-12');
    for (var i = 0; i < col_row_list.length; i++) {
        var col_row = $(col_row_list[i]);
        var col_field = {
            field_name : col_row.find('select').val(),
            customize_name : col_row.find('input[name=name]').val(),
            customize_sql : col_row.find('input[name=sql_name]').val()
        }
        col_list.push(col_field);
    };
    settingObj.col_list = col_list;

    var condition_list = [];
    var condition_rows = $('#modal_add_condition_div section .col-lg-12');
    for (var i = 0; i < condition_rows.length; i++) {
        var row = $(condition_rows[i]);
        var field = {
            field_name : row.find('select').val(),
            condition : row.find('select[name=condition]').val(),
            condition_value : row.find('input[name=condition_value]').val()
        }
        condition_list.push(field);
    };
    settingObj.condition_list = condition_list;

    var fillback_list = [];
    var fillback_rows = $('#modal_add_fillback_div section .col-lg-12');
    for (var i = 0; i < fillback_rows.length; i++) {
        var row = $(fillback_rows[i]);
        var field = {
            from_field : row.find('select[name=modal_from_field_name]').val(),
            to_field : row.find('select[name=modal_to_field_name]').val()
        }
        fillback_list.push(field);
    };
    settingObj.fillback_list = fillback_list;

    current_section.find('input[name=s_add_btn_setting]').val(JSON.stringify(settingObj));
    $('#editAddBtnType').modal('hide');
});