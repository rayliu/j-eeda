//$(document).ready(function(template) {

//$('#menu_sys_profile').addClass('active').find('ul').addClass('in');
'use strict';

var global_order_structure;

$.post('/module/getOrderStructure', {
    module_id: $("#module_id").val()
}, function(json) {
    console.log('getOrderStructure....');
    console.log(json);
    global_order_structure = json;

    $('#module_structure').val(JSON.stringify(json));

    $('#module_name').text(json.MODULE_NAME);
    document.title = json.MODULE_NAME + ' | ' + document.title;

    $('#fields_body').empty();

    //UI 处理
    buildStructureUI(json);

    //数据处理
    if($('#order_id').val()!=''){
        fillOrderData(json);
    }else{
        buildButtonUI(json);
        bindBtnClick(); //绑定按钮事件
    }
}, 'json');

var fillOrderData = function(structure_json) {
    structure_json.id = $("#order_id").val();

    $.post('/m_getOrderData', {
            params: JSON.stringify(structure_json)
        },
        function(json) {
            console.log('getOrderData....');
            console.log(json);

            for (var i = 0; i < json.FIELDS_LIST.length; i++) {
                var fieldsObj = json.FIELDS_LIST[i];
                $.each(fieldsObj, function(key, value) {
                    $("#" + key).val(value);
                });
            }

            for (var i = 0; i < json.TABLE_LIST.length; i++) {
                var tableObj = json.TABLE_LIST[i];
                var dataTable = $('#table_' + tableObj.STRUCTURE_ID).DataTable();
                dataTable.clear();
                var row_list = tableObj.ROW_LIST;
                var row = {};
                for (var j = 0; j < row_list.length; j++) {
                    var row_rec = row_list[j];
                    $.each(row_rec, function(key, value) {
                        row[key] = value;
                    });
                    dataTable.row.add(row).draw(false);
                }
            }

            buildButtonUI(structure_json, json);
            bindBtnClick();
        }, 'json');
};



var buildStructureUI = function(json) {
    for (var i = 0; i < json.STRUCTURE_LIST.length; i++) {
        var structure = json.STRUCTURE_LIST[i];

        if (!structure.FIELDS_LIST)
            continue;
        if (structure.STRUCTURE_TYPE == '字段') {
            generateField(structure);
        } else {

            var list_html = template('table_template', {
                id: structure.ID,
                structure_id: structure.ID,
                label: structure.NAME,
                field_list: structure.FIELDS_LIST,
                is_edit_order: true
            });
            $('#list').append(list_html);

            //setting 是动态跟随table生成的
            var table_setting = window['table_' + structure.ID + '_setting'];

            //从表列头重新处理
            if(structure.ADD_BTN_TYPE == '弹出列表, 从其它数据表选取'){
                var btn_setting_obj = JSON.parse(structure.ADD_BTN_SETTING);
                var headerTr = $('#table_' + structure.ID +' thead tr');

                var col_list = btn_setting_obj.col_list;
                for (var j = 0; j < col_list.length; j++) {
                    var field = JSON.parse(col_list[j].field_name);
                    headerTr.append('<th>'+field.FIELD_DISPLAY_NAME+'</th>');

                    //col setting
                    var col_item = {
                        data: 'F' + field.ID + '_' +field.FIELD_NAME
                    };
                    if (field.FIELD_TYPE == '下拉列表' 
                        && (field.FIELD_TYPE_EXT_TYPE =='客户列表' || field.FIELD_TYPE_EXT_TYPE =='供应商列表')
                    ){
                        col_item = {
                            data: 'F' + field.ID + '_' +field.FIELD_NAME + '_INPUT'
                        };
                    }
                    table_setting.columns.push(col_item);
                };
                //统一加上REF_T_ID
                headerTr.append('<th>REF_T_ID</th>');
                table_setting.columns.push({
                    data: 'REF_T_ID',
                    visible: false,
                    render: function ( data, type, full, meta ) {
                        if(!data)
                            data = '';
                        return '<input type="hidden" name="REF_T_ID" value="' + data + '">';
                    }
                });

                window['table_' + structure.ID + '_setting'] = table_setting;
            }

            $('#table_' + structure.ID).DataTable(table_setting);
        }
    } //end of for
};

var generateField=function(structure){
    var field_section_html = template('field_section', {
        id: structure.ID
    });

    $('#fields').append(field_section_html);
    var field_section = $('#' + structure.ID + '>.col-lg-12');

    for (var j = 0; j < structure.FIELDS_LIST.length; j++) {
        var field = structure.FIELDS_LIST[j];

        var field_html = '';
        if (field.FIELD_TYPE == '仅显示值') {
            field_html = template('input_field', {
                id: 'F' + field.ID + '_' + field.FIELD_NAME,
                label: field.FIELD_DISPLAY_NAME,
                disabled: "disabled"
            });
        } else if (field.FIELD_TYPE == '文本编辑框') {
            field_html = template('input_field', {
                id: 'F' + field.ID + '_' + field.FIELD_NAME,
                label: field.FIELD_DISPLAY_NAME
            });
        } else if (field.FIELD_TYPE == '日期编辑框') {
            field_html = template('input_date_field_template', {
                id: 'F' + field.ID + '_' + field.FIELD_NAME,
                label: field.FIELD_DISPLAY_NAME
            });
        } else if (field.FIELD_TYPE == '下拉列表') {
            if (field.FIELD_TYPE_EXT_TYPE == '自定义列表值') {
                var valueStr = field.FIELD_TYPE_EXT_TEXT;
                var items = valueStr.split('\n');
                field_html = template('select_field_template', {
                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    items: items
                });
            } else if (field.FIELD_TYPE_EXT_TYPE == '客户列表') {
                field_html = template('input_customer_template', {
                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME
                });
            } else if (field.FIELD_TYPE_EXT_TYPE == '供应商列表') {
                field_html = template('input_sp_template', {
                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    value: ''
                });
            }
        } else {
            field_html = template('input_field', {
                id: 'F' + field.ID + '_' + field.FIELD_NAME,
                label: field.FIELD_DISPLAY_NAME
            });
        }

        field_section.append(field_html);
    }
};

var buildOrderDto = function() {
    //循环处理字段
    var field_sections = $("#fields section");
    var fields_list = [];
    for (var index = 0; index < field_sections.length; index++) {
        var field_section = field_sections[index];
        var fields_obj = {
            id: $("#order_id").val(),
            structure_id: $(field_section).attr('id')
        }
        var fields_input = $(field_section).find('input');
        for (var i = 0; i < fields_input.length; i++) {
            var field = fields_input[i];
            if ($(field).attr('name') && $(field).val() != '') {
                fields_obj[$(field).attr('name')] = $(field).val();
            }
        }
        var fields_select = $(field_section).find('select');
        if (fields_select.length > 0) {
            for (var l = 0; l < fields_select.length; l++) { //遍历当前行的所有fields_select
                var field = fields_select[l];
                fields_obj[$(field).attr('name')] = $(field).val();
            }
        }
        fields_list.push(fields_obj);
    }

    //循环处理从表
    var table_list = [];
    var tables = $("#list").find('table');
    for (var i = 0; i < tables.length; i++) { //多个从表
        var table = tables[i];
        var table_rows = $(table).find('tr');
        var row_list = [];

        for (var j = 0; j < table_rows.length; j++) { //遍历当前表的所有行
            if(j == 0)
                continue;
            var table_row = table_rows[j];
            var row_obj = {};
            var fields_input = $(table_row).find('input');
            if (fields_input.length > 0) {
                for (var k = 0; k < fields_input.length; k++) { //遍历当前行的所有input
                    var field = fields_input[k];
                    row_obj[$(field).attr('name')] = $(field).val();
                }
            }
            var fields_select = $(table_row).find('select');
            if (fields_select.length > 0) {
                for (var l = 0; l < fields_select.length; l++) { //遍历当前行的所有fields_select
                    var field = fields_select[l];
                    row_obj[$(field).attr('name')] = $(field).val();
                }
            }
            if(fields_input.length > 0 || fields_select.length > 0){
                row_list.push(row_obj);
            }
        };

        var table_obj = {
            structure_id: $(table).attr("structure_id"),
            row_list: row_list
        };
        table_list.push(table_obj);
    }

    var order_dto = {
        module_id: $('#module_id').val(),
        id: $('#order_id').val(),
        fields_list: fields_list,
        table_list: table_list,
        action: ''
    };

    return order_dto;
};




//});
