//-------------table add button click
$('#list').on('click', 'button', function(event) {
    if ($(this).attr('name') == 'addRowBtn') {
        var table_id = $(this).attr('table_id');
        var structure_id = $(this).attr('structure_id');

        var btn_type = getTableAddBtnType(structure_id);

        if(btn_type == '添加空行'){
            var dataTable = $('#' + table_id).DataTable();
            var row = window[table_id + '_row'];
            dataTable.row.add(row).draw(false);
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
          return '<input type="checkbox" value="'+full.ID+'">';
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
        
        headerTr.append('<th>'+field.FIELD_DISPLAY_NAME+'</th>')
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