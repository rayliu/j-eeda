

//-------------table tr click
var highLightTr =  function(tr) {
    var trs = tr.parent().parent().parent().find('tr');
    for (var i = 0; i < trs.length; i++) {
        $(trs[i]).css('background-color', '#f9f9f9');
    };
    tr.css('background-color', '#DDD');
};

//-------------table tr click show detail table and filter
$('#list').on('click', 'a[name=show_detail]', function(event) {
    var current_tr = $(this).parent().parent();
    highLightTr(current_tr);

    var table_row_index = current_tr.attr('index');
    var table_row_id = current_tr.attr('id');
    var detail_structure_id = $(this).attr('detail_table_id');

    var dataTable = $(this).closest('table').DataTable();
    var row = dataTable.row(current_tr);
    var icon = $(this).find('i');
    icon.removeClass();

    var table_3rd;
    if (row.child.isShown()) {
        // This row is already open - close it
        if(table_row_id){
            table_3rd = $('div [name=table_'+$(this).attr('detail_table_id')+'_div][parent_table_row_id='+table_row_id+']');
        }else{
            table_3rd = $('div [parent_table_row_index='+table_row_index+']');
        }
        $('#list').append(table_3rd);
        table_3rd.hide();
        row.child.hide();

        current_tr.removeClass('shown');
        icon.addClass('fa fa-chevron-right');
        
    }else {
        // Open this row
        if(table_row_id){
            table_3rd = $('div [name=table_'+$(this).attr('detail_table_id')+'_div][parent_table_row_id='+table_row_id+']');
            table_3rd.show();
            row.child(table_3rd).show();
        }else{
            var structure = getStructure(global_order_structure, detail_structure_id);
            var list_html = template('table_template', {
                customer_id: global_customer_id,
                id: structure.ID,
                structure_id: structure.ID,
                label: structure.NAME,
                field_list: structure.FIELDS_LIST,
                is_edit_order: true,
                parent_table_id: structure.PARENT_ID,  
                parent_table_row_id: table_row_id,//如果table是第三层，它需要知道上层table row的ID
                parent_table_row_index: table_row_index,
                is_3rd_table: 'true'
            });
            row.child(list_html).show();

            table_3rd = $('div [name=table_'+$(this).attr('detail_table_id')+'_div][parent_table_row_id='+table_row_id+']');
            table_3rd.show();
            var table_setting = window['table_' + structure.ID + '_setting'];
            var dataTable = $('div [parent_table_row_index='+table_row_index+'] table').DataTable(table_setting);
        }
        
        table_3rd.closest('td').css('border', 'solid 1px #fafafa');
        current_tr.addClass('shown');
        icon.addClass('fa fa-chevron-down');
    }

    //处理新增按钮
    var addBtn = $('#table_'+detail_structure_id+'_div button[name=addRowBtn]');
    if(table_row_id){
        console.log('filter by id');
        addBtn.attr('parent_row_id', table_row_id);
        addBtn.removeAttr('parent_row_index');
    }else{
        console.log('filter by index');
        addBtn.attr('parent_row_index', table_row_index);
        addBtn.removeAttr('parent_row_id');
    }

});

//-------------table add button click
$('#list').on('click', 'button', function(event) {
    var addBtn = $(this);
    if (addBtn.attr('name') == 'addRowBtn') {
        var table_id = $(this).attr('table_id');
        var structure_id = $(this).attr('structure_id');
        $('#addRowBtn_search_list_modal #target_structure_id').val(structure_id);

        var btn_type = getTableAddBtnType(structure_id);

        if(btn_type == '添加空行'){
            var $table = $('#' + table_id);
            var row = window[table_id + '_row'];
            var dataTable = global_data_table['table_' + structure_id+'_dataTable'];
            if ($(this).attr('is_3rd_table') == 'true') {
                var parent_row_id = addBtn.attr('parent_table_row_id');
                var parent_row_index = addBtn.attr('parent_table_row_index');
                row.PARENT_ROW_ID = parent_row_id;
                row.PARENT_ROW_INDEX = parent_row_index;
                if(parent_row_id){
                    dataTable = global_data_table['table_' + structure_id+'_dataTable_'+parent_row_id];
                }else{
                    $table = $('div [name=table_' + structure_id +'_div][parent_table_row_index='+parent_row_index+'] table');
                    dataTable = $table.DataTable();
                }
            }

            dataTable.row.add(row).draw(false);
            var current_tr = $table.find('tr:last');
            highLightTr(current_tr);
            //current_tr.find('a[name=show_detail]').click();
            bindProductSearch();
        }else{
            $('#addRowBtn_search_list_modal #fields').empty();
            var searchListSetting = getTableAddBtnSetting(structure_id);
            buildSearchModalUI(searchListSetting);
            $('#addRowBtn_search_list_modal').modal('show');
        }
    }
});

//-------------table delete button click
$('#list').on('click', 'a', function(event) {
    if ($(this).attr('class') == 'delete') {
        var table_id = $(this).attr('table_id');
        var dataTable = $('#' + table_id).DataTable();
        var tr = $(this).parent().parent();
        //deletedTableIds.push(tr.attr('id'))

        dataTable.row(tr).remove().draw();
    }
});

var getTableAddBtnType=function(structure_id){
    for (var i = 0; i < global_order_structure.STRUCTURE_LIST.length; i++) {
        var structure = global_order_structure.STRUCTURE_LIST[i];
        if (structure.ID == structure_id ) {
            return structure.ADD_BTN_TYPE;
        }
    }
};

var getTableAddBtnSetting=function(structure_id){
    for (var i = 0; i < global_order_structure.STRUCTURE_LIST.length; i++) {
        var structure = global_order_structure.STRUCTURE_LIST[i];
        if (structure.ID == structure_id ) {
            return structure.ADD_BTN_SETTING;
        }
    }
};

var buildSearchModalUI = function(setting_json){
    var setting_obj = JSON.parse(setting_json);//主表结构

    if(!setting_obj.col_list)
        return;

    var field_html = template('input_hidden_field', 
        {
            id: 'structure_id',
            value: setting_obj.structure_id
        }
    );
    $('#addRowBtn_search_list_modal #fields').append(field_html);

    buildModalQueryFields(setting_obj.col_list);
    buildCondition(setting_obj.condition_list);
    buildModalResultList(setting_obj.col_list);
};

var buildModalQueryFields = function(col_list){
    for (var j = 0; j < col_list.length; j++) {
        var field = JSON.parse(col_list[j].field_name);

        var field_html = '';
        if(field.FIELD_TYPE == '仅显示值'){
            field_html = template('input_field', 
                {
                    id: 'F' + field.ID + '_' +field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    field_type: field.FIELD_TYPE
                }
            );
        }else if(field.FIELD_TYPE == '文本编辑框'){
            field_html = template('input_field', 
                {
                    id: 'F' + field.ID + '_' +field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    type: field.FIELD_TYPE
                }
            );
        }else if(field.FIELD_TYPE == '日期编辑框'){
            field_html = template('input_date_query_template', 
                {
                    id: 'F' + field.ID + '_' +field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    type: field.FIELD_TYPE
                }
            );
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
            } else if (field.FIELD_TYPE_EXT_TYPE == '城市列表') {
                field_html = template('input_location_template', {
                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    value: ''
                });
            }
        } else{
            field_html = template('input_field', 
                {
                    id: 'F' + field.ID + '_' +field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    type: field.FIELD_TYPE
                }
            );
        }

        $('#addRowBtn_search_list_modal #fields').append(field_html);
    }
};

var buildCondition= function(condition_list){
    for (var j = 0; j < condition_list.length; j++) {
        var field = JSON.parse(condition_list[j].field_name);
        var operator = condition_list[j].operator;
        var condition_value = condition_list[j].condition_value;

        var input_fields = $('#addRowBtn_search_list_modal #fields input');
        for (var i = 0; i < input_fields.length; i++) {
            var input = $(input_fields[i]);
            if(input.attr('name') == 'F'+field.ID+'_'+field.FIELD_NAME){
                input.attr('disabled', 'disabled');
                input.val(condition_value);
            }
        };
    }
};

var buildModalResultList = function(col_list){
    var dataTable = $('#modal_search_result_table').DataTable();
    dataTable.destroy();//销毁之前的dataTable setting

    var headerTr = $('#modal_search_result_table thead tr');
    headerTr.empty();
    headerTr.append('<th></th>')//for checkbox col


    var columns = [{
        width: "20px", 
        orderable: false, 
        render: function ( data, type, full, meta ) {
          return '<input type="checkbox" value="'+full.ID+'">'
           +'&nbsp;&nbsp;&nbsp;<a name="show_detail" row_id="'+full.ID+'" href="javascript:void(0)" title="显示明细"><i class="fa fa-chevron-right"></i></a>';
        }
    }];

    for (var i = 0; i < col_list.length; i++) {
        var field = JSON.parse(col_list[i].field_name);
        var col_item = {
            data: 'F' + field.ID + '_' +field.FIELD_NAME
        };
        if (field.FIELD_TYPE == '下拉列表' 
            && (
                (field.FIELD_TYPE_EXT_TYPE =='客户列表' )|| (field.FIELD_TYPE_EXT_TYPE =='供应商列表')
               )
        ){
            col_item = {
                data: 'F' + field.ID + '_' +field.FIELD_NAME + '_INPUT'
            };
        }

        headerTr.append('<th>'+field.FIELD_DISPLAY_NAME+'</th>');
        columns.push(col_item);
    };

    var modal_search_result_table_setting = {
        info: true,
        processing: true,
        searching: false,
        autoWidth: true,
        serverSide: true,
        deferLoading: 0, //初次不查数据
        language: {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        createdRow: function ( row, data, index ) {
            var id='';
            if(data.ID){
                id=data.ID;
            }
            $(row).attr('id', id);
            $(row).append('<input type="hidden" name="id" value="' + id + '"/>');
        },
        columns: columns
    };

    $('#modal_search_result_table').DataTable(modal_search_result_table_setting);

    $('#addRowBtn_search_list_modal #searchBtn').click();
};

var buildStructureSearchUrl=function(){
    var url = '/m_search?';
    var search_inputs = $("#addRowBtn_search_list_modal section#fields input");

    var search_list=[];
    for(var i=0; i<search_inputs.length; i++){
        var input_field = $(search_inputs[i]);
        var name = input_field.attr('name');
        var value = input_field.val();

        url += '&' + name + '=' + value;
    }
    
    return url;
};

var format= function(d){
    // `d` is the original data object for the row
    return '<table cellpadding="5" cellspacing="0" border="0" style="margin-left:10px;">'+
        '<thead>'+
            '<tr   style="background-color: lightblue;" >'+
                '<th></th>'+
                '<th>型号</th>'+
                '<th>数量</th>'+
                '<th>范德萨</th>'+
                '<th>范德萨</th>'+
                '<th>范德萨</th>'+
            '</tr>'+
        '</thead>'+
            '<tbody>'+
            '<tr style="background-color: lightyellow;">'+
                '<td><input type="checkbox" value=""></td></td>'+
                '<td>68NL</td>'+
                '<td>10</td>'+
                '<td>范德萨</td>'+
                '<td>范德萨</td>'+
                '<td>范德萨</td>'+
            '</tr>'+
            '<tr style="background-color: lightyellow;">'+
                '<td><input type="checkbox" value=""></td></td>'+
                '<td>222nl</td>'+
                '<td>5</td>'+
                '<td>范德萨</td>'+
                '<td>范德萨</td>'+
                '<td>范德萨</td>'+
            '</tr>'+
        '</tbody>'+
    '</table>';
};

$('#addRowBtn_search_list_modal table').on('click', 'a[name=show_detail]', function(e){
    //阻止a 的默认响应行为，不需要跳转
    e.preventDefault();
    var tr = $(this).closest('tr');
    var icon = $(this).find('i');
    var dataTable = $('#modal_search_result_table').DataTable();
    var row = dataTable.row(tr);

    icon.removeClass();
    if ( row.child.isShown() ) {
        // This row is already open - close it
        row.child.hide();
        tr.removeClass('shown');
        icon.addClass('fa fa-chevron-right');
    }
    else {
        // Open this row
        row.child( format(row.data()) ).show();
        tr.addClass('shown');
        icon.addClass('fa fa-chevron-down');
    }
});


$('#addRowBtn_search_list_modal #searchBtn').on('click', function(e){
    //阻止a 的默认响应行为，不需要跳转
    e.preventDefault();

    var url = buildStructureSearchUrl();

    console.log('searchBtn.click....');

    var dataTable = $('#modal_search_result_table').DataTable();
    dataTable.ajax.url(url).load();
});

$("#addRowBtn_search_list_modal #resetBtn").click(function(){
    $('#searchForm')[0].reset();
});

$("#addRowBtn_search_list_modal button[name=ok_btn]").click(function(){
    var dataTable = $('#modal_search_result_table').DataTable();
    var data = dataTable.rows().data();

    //回填 table_id
    var target_structure_id = $('#addRowBtn_search_list_modal #target_structure_id').val();
    var target_table = $('#table_' + target_structure_id).DataTable();
    var target_table_setting = window['table_' + target_structure_id + '_setting'];

    // console.log(data);
    var checked_ids = $('#modal_search_result_table input[type=checkbox]:checked');
    for (var i = 0; i < checked_ids.length; i++) {
        var input = $(checked_ids[i]);
        var id = input.val();
        for (var j = 0; j < data.length; j++) {
            var row_obj = dataTable.row(j).data();
            if(id == row_obj.ID){
                var new_row = {};
                for (var k = 0; k < target_table_setting.columns.length; k++) {
                    var column = target_table_setting.columns[k];
                    var field_name = column.data;
                    new_row[field_name] = row_obj[field_name];
                };

                //统一加上REF_T_ID
                new_row['REF_T_ID'] = id;
                target_table.row.add(new_row).draw(false);
            }
        };
    };
    $('#addRowBtn_search_list_modal').modal('hide');
});

