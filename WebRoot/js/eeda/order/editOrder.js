

    
//$(document).ready(function(template) {
	
    //$('#menu_sys_profile').addClass('active').find('ul').addClass('in');

    $.post('/module/getOrderStructure', {module_id: $("#module_id").val()}, function(json){
        console.log('getOrderStructure....');
        console.log(json);
        $('#module_structure').val(JSON.stringify(json));

        $('#module_name').text(json.MODULE_NAME);
        document.title = json.MODULE_NAME + ' | ' + document.title;

        $('#fields_body').empty();

        //UI 处理
        buildStructureUI(json);
        buildButtonUI(json);
        bindBtnClick();//绑定按钮事件

        //数据处理
        fillOrderData(json);
    }, 'json');

    var fillOrderData = function(structure_json){
        structure_json.id = $("#order_id").val();

        $.post('/m_getOrderData', {params:JSON.stringify(structure_json)}, 
            function(json){
                console.log('getOrderData....');
                console.log(json);

                for (var i = 0; i < json.FIELDS_LIST.length; i++) {
                    var fieldsObj = json.FIELDS_LIST[i];
                    $.each(fieldsObj, function(key, value){
                        $("#"+key).val(value);
                    });
                }

                for (var i = 0; i < json.TABLE_LIST.length; i++) {
                    var tableObj = json.TABLE_LIST[i];
                    var dataTable = $('#table_' + tableObj.STRUCTURE_ID).DataTable();
                    dataTable.clear();
                    var row_list = tableObj.ROW_LIST;
                    var row ={};
                    for (var j = 0; j < row_list.length; j++) {
                        var row_rec = row_list[j];
                        $.each(row_rec, function(key, value){
                            row[key] = value;
                        });
                        dataTable.row.add(row).draw(false);
                    }
                }
            }, 'json');
    };

    var buildButtonUI = function(json){
        for (var i = 0; i < json.ACTION_LIST.length; i++) {
            var buttonObj = json.ACTION_LIST[i];
            var button_html = template('button_template', 
                    {
                        id: buttonObj.ID,
                        label: buttonObj.ACTION_NAME
                    }
                );
            $('#button-bar').append(button_html);
        }
    };

    var buildStructureUI = function(json){
        for (var i = 0; i < json.STRUCTURE_LIST.length; i++) {
            var structure = json.STRUCTURE_LIST[i];

                if(!structure.FIELDS_LIST)
                    continue;
                if(structure.STRUCTURE_TYPE == '字段'){
                    var field_section_html = template('field_section', 
                            {
                                id: structure.ID
                            }
                        );

                    $('#fields').append(field_section_html);
                    var field_section = $('#'+structure.ID+'>.col-lg-12');

                    for (var j = 0; j < structure.FIELDS_LIST.length; j++) {
                        var field = structure.FIELDS_LIST[j];

                        var field_html = '';
                        if(field.FIELD_TYPE == '仅显示值'){
                            field_html = template('input_field', 
                                {
                                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME,
                                    disabled: "disabled"
                                }
                            );
                        }else if(field.FIELD_TYPE == '文本编辑框'){
                            field_html = template('input_field', 
                                {
                                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME
                                }
                            );
                        }else if(field.FIELD_TYPE == '日期编辑框'){
                            field_html = template('input_date_field_template', 
                                {
                                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME
                                }
                            );
                        }else if(field.FIELD_DISPLAY_NAME == '客户'){
                            field_html = template('input_customer_template', 
                                {
                                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME
                                }
                            );
                        }else if(field.FIELD_DISPLAY_NAME == '供应商'){
                            field_html = template('input_sp_template', 
                                {
                                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME,
                                    value: ''
                                }
                            );
                        }else{
                            field_html = template('input_field', 
                                {
                                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME
                                }
                            );
                        }

                        field_section.append(field_html);
                    }
                }else{
                    var list_html = template('table_template', 
                            {
                                id: structure.ID,
                                structure_id: structure.ID,
                                label: structure.NAME,
                                field_list: structure.FIELDS_LIST,
                                is_edit_order: true
                            }
                        );
                    $('#list').append(list_html);

                    //setting 是动态跟随table生成的
                    var table_setting = window['table_' + structure.ID + '_setting'];
                    $('#list table:last').DataTable(table_setting);
                }
        }//end of for
    };

    //-------------table add button click
    $('#list').on('click', 'button', function(event) {
        if($(this).attr('name') == 'addRowBtn'){
            var table_id = $(this).attr('table_id');
            var dataTable = $('#' + table_id).DataTable();
            var row = window[table_id + '_row'];
            dataTable.row.add(row).draw(false);
        }
    });
    //-------------table delete button click
    $('#list').on('click', 'a', function(event) {
        if($(this).attr('class') == 'delete'){
            var table_id = $(this).attr('table_id');
            var dataTable = $('#' + table_id).DataTable();
            var tr = $(this).parent().parent();
            //deletedTableIds.push(tr.attr('id'))

            dataTable.row(tr).remove().draw();
        }
    });


    var deletedTableIds=[];

    var $fields_table = $("#fields-table");

    //删除表中一行
    $fields_table.on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))

        cargoTable.row(tr).remove().draw();
    });

    var buildOrderDto=function(){
        //循环处理字段
        var field_sections = $("#fields section");
        var fields_list = [];
        for(var index=0; index<field_sections.length; index++){
            var field_section = field_sections[index];
            var fields_obj = {
                id : $("#order_id").val(),
                structure_id : $(field_section).attr('id')
            }
            var fields_input = $(field_section).find('input');
            for(var i=0; i<fields_input.length; i++){
                var field = fields_input[i];
                if($(field).attr('name') && $(field).val() !=''){
                    fields_obj[$(field).attr('name')] = $(field).val();
                }
            }
            fields_list.push(fields_obj);
        }

        //循环处理从表
        var table_list = [];
        var tables = $("#list").find('table');
        for(var i=0; i<tables.length; i++){//多个从表
            var table = tables[i];
            var table_rows = $(table).find('tr');
            var row_list = [];

            for (var j = 0; j < table_rows.length; j++) {//遍历当前表的所有行
                table_row = table_rows[j];
                var row_obj = {};
                var fields_input = $(table_row).find('input');
                if(fields_input.length >0){
                    for(var k=0; k<fields_input.length; k++){//遍历当前行的所有input
                        var field = fields_input[k];
                        row_obj[$(field).attr('name')] = $(field).val();
                    }
                    row_list.push(row_obj);
                }
            };

            var table_obj = {
                structure_id: $(table).attr("structure_id"),
                row_list: row_list
            };
            table_list.push(table_obj);
        }

        var order_dto={
            module_id: $('#module_id').val(),
            id: $('#order_id').val(),
            fields_list: fields_list, 
            table_list: table_list,
            action: ''
        };
        
        return order_dto;
    };


    var reDrawTable=function(order){
        deletedTableIds=[];
        cargoTable.clear();
        for (var i = 0; i < order.ITEM_LIST.length; i++) {
            var item = order.ITEM_LIST[i];
            var item={
                "ID": item.ID,
                "PRODUCT_NO": item.PRODUCT_NO,
                "SERIAL_NO": item.SERIAL_NO,
                "REMARK": item.REMARK
            };

            cargoTable.row.add(item).draw(false);
        }
    };

    var bindBtnClick = function(){
        $('button.order_level').on('click', function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();

            var btnClass = $(this).attr('class');
            var btn = $(this);
            btn.attr('disabled', true);

            //提交前，校验数据
            // if(!$("#orderForm").valid()){
            //     return;
            // }

            btn.attr('disabled', false);

            var order_dto = buildOrderDto();
            order_dto.action = btn.text();

            console.log('save OrderData....');
            console.log(order_dto);

            //异步向后台提交数据
            $.post('/m_save', {params:JSON.stringify(order_dto)}, function(data){
                var order = data;
                console.log(order);
                if(order.ID>0){
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);

                    //重新取一次数据渲染页面
                    var structure_json_str = $('#module_structure').val();
                    var structure_json = JSON.parse(structure_json_str);
                    structure_json.id = order.ID;
                    fillOrderData(structure_json);

                    $('#saveBtn').attr('disabled', false);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            });
        });
    };
    

//});