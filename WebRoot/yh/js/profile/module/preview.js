 

    
//$(document).ready(function(template) {
	document.title = '模块预览 | '+document.title;
    $('#menu_sys_profile').addClass('active').find('ul').addClass('in');

    $.post('/module/getOrderStructure', {module_id: $("#module_id").val()}, function(json){
        console.log('getOrderStructure....');
        console.log(json);
        $('#fields_body').empty();

        buildStructureUI(json);
        buildButtonUI(json);
    }, 'json');

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
                    for (var j = 0; j < structure.FIELDS_LIST.length; j++) {
                        var field = structure.FIELDS_LIST[j];

                        var field_html = '';
                        if(field.FIELD_DATA_TYPE == '文本' && field.FIELD_TYPE == '仅显示值'){
                            field_html = template('input_field', 
                                {
                                    id: field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME,
                                    disabled: "disabled"
                                }
                            );
                        }else if(field.FIELD_DATA_TYPE == '文本' && field.FIELD_TYPE == '文本编辑框'){
                            field_html = template('input_field', 
                                {
                                    id: field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME
                                }
                            );
                        }else if(field.FIELD_TYPE == '日期编辑框'){
                            field_html = template('input_date_field_template', 
                                {
                                    id: field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME
                                }
                            );
                        }else if(field.FIELD_DISPLAY_NAME == '客户'){
                            field_html = template('input_customer_template', 
                                {
                                    id: field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME
                                }
                            );
                        }else if(field.FIELD_DISPLAY_NAME == '供应商'){
                            field_html = template('input_sp_template', 
                                {
                                    id: field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME,
                                    value: ''
                                }
                            );
                        }else{
                            field_html = template('input_field', 
                                {
                                    id: field.FIELD_NAME,
                                    label: field.FIELD_DISPLAY_NAME
                                }
                            );
                        }
                        
                        $('#fields').append(field_html);
                    }
                }else{
                    var list_html = template('table_template', 
                            {
                                id: structure.ID,
                                label: structure.NAME,
                                field_list: structure.FIELDS_LIST
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

    var buildStructureFieldsrray=function(structure_table){
        var table_rows = $(structure_table).find('tr');
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
                field_name: $(row.children[2]).find('input').val(), 
                field_type: $(row.children[3]).find('select').val(),
                field_data_type: $(row.children[4]).find('select').val(),
                required: $(row.children[5]).find('select').val(),
                listed: $(row.children[6]).find('select').val(),
                field_template_path: $(row.children[7]).find('input').val(),
                index_name:'',
                action: $('#module_id').val().length>0?'UPDATE':'CREATE'
            };

            if(item.field_name.length>0){
                items_array.push(item);
            }
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

    var buildStructureTableArray=function(){
        var structure_sections = $("section.structure");
        console.log('structure_sections.length:'+structure_sections.length);

        var structure_table_array=[];
        for(var i=0; i<structure_sections.length; i++){
            var structure_section = structure_sections[i];
            var structure_table = $(structure_section).find('table')[0];
            var fields = buildStructureFieldsrray(structure_table);
            var structure={
                id: $($(structure_section).find('.s_id')[0]).val(),
                name: $($(structure_section).find('.s_name')[0]).val(),
                structure_type: $($(structure_section).find('.s_type')[0]).val(),
                parent_name: $($(structure_section).find('.s_type')[0]).val(),
                parent_id: $($(structure_section).find('.s_parent_id')[0]).val(),
                field_list: fields
            }
            structure_table_array.push(structure);
        }
        
        return structure_table_array;
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


    $('#saveBtn').on('click', function(e){
        $(this).attr('disabled', true);

        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数据
        // if(!$("#orderForm").valid()){
        //     return;
        // }

        var structure_list=buildStructureTableArray();

        var dto = {
            module_id: $('#module_id').text(),
            structure_list: structure_list
        };

        console.log('saveBtn.click....');
        console.log(dto);

        //异步向后台提交数据
        $.post('/module/saveStructure', {params:JSON.stringify(dto)}, function(data){
            var order = data;
            console.log(order);
            if(order.ID>0){
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);

                $('#saveBtn').attr('disabled', false);

                damageOrder.reDrawTable(order);
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
        });
    });

    //单据预览
    $('#previewBtn').click(function(){
        window.open('/module/preview/'+$("#module_id").text());
    });
//});