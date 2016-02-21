

    
//$(document).ready(function(template) {
	document.title = '模块定义 | '+document.title;
    $('#menu_sys_profile').addClass('active').find('ul').addClass('in');
    $('[data-toggle=tooltip]').tooltip();
    //-------------   子表的动态处理
    var subIndex=0;
    //添加一个新子表
    $('#addTableBtn').click(function function_name (argument) {

        var structure_names = [];
        var s_names = $('section .s_name');
        for(var i=0; i<s_names.length; i++){
            structure_names.push($(s_names[i]).val());
        }

        var html = template('table_template', {s_name: 'sub'+subIndex, structure_names: structure_names});
        $('#fields_body').append(html);
        $('#fields_body table:last').DataTable(tableSetting);

        subIndex++;
        
        bindFieldTableEvent();
    });

    var deletedTableIds=[];
    //删除一个新子表
    $('#fields_body').on('click', 'a.remove', function(event) {
        var s_id = $(this).parent().find('input.s_id').val();
        $(this).parent().remove();
        deletedTableIds.push(s_id);
    });

    //添加字段
    $('#fields_body').on('click', 'button[name=addFieldBtn]', function(event) {
        var section = $(this).parent().parent();
        var sectionDataTable = $(section.find('table:first')[0]).DataTable();
        var item={
            "ID": '',
            "FIELD_NAME": '',
            "FIELD_TYPE": '',
            "REQUIRED": '',
            "LISTED": '',
            "FIELD_TEMPLATE_PATH": '',
            "INDEX_NAME": ''
        };
        sectionDataTable.row.add(item).draw(false);
    });

    //定义子表“显示类型”：字段，列表
    $('#fields_body').on('change', 'select.s_type', function(event) {
        var select = $(this);
        var add_btn_type_field = select.parent().parent().parent().find('div[name=add_btn_type_field]')
        if('字段' == select.val()){
            add_btn_type_field.hide();
        }else{
            add_btn_type_field.show();
        }
    });
    //定义“新增”按钮类型
    $('#fields_body').on('change', 'select.s_add_btn_type', function(event) {
        var select = $(this);
        var editIcon = select.parent().find('a[name=addBtnSetting]');
        if('添加空行' == select.val()){
            editIcon.hide();
        }else{
            editIcon.show();
        }
    });
    //-------------   子表的动态处理

    var tableSetting = {
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
            $(row).append('<input class="ext_type" type="hidden" value="'+data.FIELD_TYPE_EXT_TYPE+'" />');
            $(row).append('<textarea class="ext_text" style="display:none;" >'+data.FIELD_TYPE_EXT_TEXT+'</textarea>');
        },
        //"ajax": "/damageOrder/list",
        "columns": [
            { "width": "30px", "orderable":false,
                "render": function ( data, type, full, meta ) {
                  return '<a class="remove delete" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i> </a>&nbsp;&nbsp;'+
                    '<a class="edit" href="javascript:void(0)" title="编辑"><i class="glyphicon glyphicon-edit"></i> </a>';
                }
            },
            { "data": "ID", visible: false},
            { "data": "FIELD_DISPLAY_NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<input type="text" value="'+data+'" class="product_no form-control"/>';
                }
            },
            { "data": "FIELD_TYPE",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<select class="form-control">'
                        +'    <option '+(data=='文本编辑框'?'selected':'')+'>文本编辑框</option>'
                        +'    <option '+(data=='仅显示值'?'selected':'')+'>仅显示值</option>'
                        +'    <option '+(data=='隐藏值'?'selected':'')+'>隐藏值</option>'
                        +'    <option '+(data=='日期编辑框'?'selected':'')+'>日期编辑框</option>'
                        +'    <option '+(data=='下拉列表'?'selected':'')+'>下拉列表</option>'
                        +'    <option '+(data=='弹出列表, 从其它数据表选取'?'selected':'')+'>弹出列表, 从其它数据表选取</option>'
                        +'</select>';
                }
            },
            { "data": "FIELD_TYPE_EXT_TYPE", visible: false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<input type="text" value="'+data+'" class="product_no form-control"/>';
                }
            },
            { "data": "FIELD_TYPE_EXT_TEXT", visible: false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<input class="form-control" rows="1">'+data+'</input>';
                }
            },
            { "data": "FIELD_DATA_TYPE", visible: false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<select class="form-control">'
                        +'    <option '+(data=='文本'?'selected':'')+'>文本</option>'
                        +'    <option '+(data=='数值'?'selected':'')+'>数值</option>'
                        +'    <option '+(data=='日期'?'selected':'')+'>日期</option>'
                        +'</select>';
                }
            },
            { "data": "REQUIRED",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<select class="form-control">'
                        +'   <option '+(data=='N'?'selected':'')+'>N</option>'
                        +'   <option '+(data=='Y'?'selected':'')+'>Y</option>'
                        +'</select>';
                }
            }, 
            { "data": "LISTED",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<select class="form-control">'
                        +'   <option '+(data=='Y'?'selected':'')+'>Y</option>'
                        +'    <option '+(data=='N'?'selected':'')+'>N</option>'
                        +'</select>';
                }
            },
            { "data": "FIELD_TEMPLATE_PATH", visible: false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<input type="text" value="'+data+'" class="product_no form-control"/>';
                }
            },
            { "data": "INDEX_NAME", visible: false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                  return '<input type="text" value="'+data+'" class="product_no form-control"/>';
                }
            }
        ]
    };

    var dataTable = $('#fields-table').DataTable(tableSetting);

    //等页面组件装载完成后，再绑定事件
    var bindFieldTableEvent= function(){
        var $fields_table = $("#fields_body table");

        $('.addBtnSetting').click(function(){
            addBtnSettingClick(this);
        });

        //编辑表中一行字段的属性
        $fields_table.on('click', '.edit', function(e){
            e.preventDefault();

            $("#modalForm")[0].reset();
            $("#modal_field_type_ext_div").hide();
            $("#customize_list").hide();

            var tr = $(this).parent().parent()[0];

            $('#modal_row_id').val($(tr).attr('id'));

            $("#modal_field_name").val($(tr.children[1]).find('input').val());
            $("#modal_field_type").val($(tr.children[2]).find('select').val());
            if('下拉列表' == $(tr.children[2]).find('select').val()){
                $("#modal_field_type_ext_type").val($(tr).find('>input.ext_type').val());
                $("#modal_field_type_ext_text").val($(tr).find('>textarea.ext_text').val());
                $("#modal_field_type_ext_div").show();
            }
            if('自定义列表值' == $(tr).find('>input.ext_type').val()){
                $("#customize_list").show();
            }

            $("#editField").modal('show');
        });

        //删除表中一行
        $fields_table.on('click', '.delete', function(e){
            e.preventDefault();
            var tr = $(this).parent().parent();
            var dataTable = $fields_table.DataTable();
            dataTable.row(tr).remove().draw();
        });

        $fields_table.find('tbody').sortable({
          revert: true
        });

    }

    $("#modal_field_type").on('change', function(){
        if('下拉列表' == $(this).val()){
            $("#modal_field_type_ext_div").show();
        }else{
            $("#modal_field_type_ext_div").hide();
        }
        if('弹出列表, 从其它数据表选取' == $(this).val()){
            $("#modal_field_type_pop_div").show();
        }else{
            $("#modal_field_type_pop_div").hide();
        }
    });

    $("#modal_field_type_ext_type").on('change', function(){
        if('自定义列表值' == $(this).val()){
            $("#customize_list").show();
        }else{
            $("#customize_list").hide();
        }
    });
    
    //对话框关闭，填值到列表中
    $('#modalFormOkBtn').click(function(){
        var row_id = $('#modal_row_id').val();
        var tr = $('tr#'+row_id)[0];
        $(tr.children[2]).find('select').val($("#modal_field_type").val());
        $(tr).find('>input.ext_type').val($("#modal_field_type_ext_type").val());
        $(tr).find('>textarea.ext_text').text($("#modal_field_type_ext_text").val());
        $("#editField").modal('hide');
    });

    var deletedItemIds=[];

    var buildStructureFieldsArray=function(structure_table){
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

            var col_index= 1;
            var item={
                id: id,
                //field_name: $(row.children[2]).find('input').val(), 
                field_display_name: $(row.children[col_index]).find('input').val(), 
                field_type: $(row.children[col_index+1]).find('select').val(),
                field_type_ext_type:$(row).find('>input.ext_type').val(),//取自行隐藏字段
                field_type_ext_text:$(row).find('>textarea.ext_text').val(),
                //field_data_type: $(row.children[col_index+2]).find('select').val(),
                required: $(row.children[col_index+2]).find('select').val(),
                listed: $(row.children[col_index+3]).find('select').val(),
                //field_template_path: $(row.children[col_index+5]).find('input').val(),
                index_name:'',
                action: $('#module_id').val().length>0?'UPDATE':'CREATE',
                seq: index
            };

            if(item.field_display_name.length>0){
                items_array.push(item);
            }
        }

        //add deleted items
        for(var index=0; index<deletedItemIds.length; index++){
            var id = deletedItemIds[index];
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
            var fields = buildStructureFieldsArray(structure_table);
            var structure={
                id: $($(structure_section).find('.s_id')[0]).val(),
                name: $($(structure_section).find('.s_name')[0]).val(),
                structure_type: $($(structure_section).find('.s_type')[0]).val(),
                add_btn_type: $($(structure_section).find('.s_add_btn_type')[0]).val(),
                add_btn_setting: $($(structure_section).find('input[name=s_add_btn_setting]')).val(),
                parent_id: $($(structure_section).find('.s_parent_id')[0]).val(),
                field_list: fields
            }
            structure_table_array.push(structure);
        }
        //add deleted tables
        for(var index=0; index<deletedTableIds.length; index++){
            var id = deletedTableIds[index];
            var structure={
                id: id,
                action: 'DELETE'
            };
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

    var saveAction=function(btn, is_start){
        is_start = is_start || false; 
        var structure_list=buildStructureTableArray();
        var action_list=buildActionArray();

        var dto = {
            module_id: $('#module_id').text(),
            structure_list: structure_list,
            action_list: action_list,
            is_start: is_start
        };

        console.log('saveBtn.click....');
        console.log(dto);

        //异步向后台提交数据
        $.post('/module/saveStructure', {params:JSON.stringify(dto)}, function(data){
            var order = data;
            console.log(order);
            if(order.MODULE_ID>0){
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                btn.attr('disabled', false);
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                btn.attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            btn.attr('disabled', false);
        });
    };


    $('#saveBtn').on('click', function(e){
        $(this).attr('disabled', true);

        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数据
        // if(!$("#orderForm").valid()){
        //     return;
        // }

        saveAction($(this));
    });

    //单据预览
    $('#previewBtn').click(function(){
        window.open('/module/preview/'+$("#module_id").text());
    });

    $('#startBtn').click(function(e){
        $(this).attr('disabled', true);

        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();

        saveAction($(this), true);
    });
//});