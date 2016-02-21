//$(document).ready(function(template) {


'use strict';

 $('[data-toggle=tooltip]').tooltip();

var global_order_structure;
var global_customer_id;
var global_data_table={};

$.post('/module/getOrderStructure', {
    module_id: $("#module_id").val()
}, function(json) {
    console.log('getOrderStructure....');
    console.log(json);
    global_order_structure = json;

    $('#module_structure').val(JSON.stringify(json));

    $('#module_name').text(json.MODULE_NAME);
    document.title = json.MODULE_NAME + ' | ' + document.title;

    //数据处理
    if($('#order_id').val()!=''){
        fillOrderData(json);
    }else{
         //UI 处理
        buildStructureUI(json);
        buildButtonUI(json);
        bindBtnClick(); //绑定按钮事件
        $('[data-toggle=tooltip]').tooltip();
    }

}, 'json');

var fillOrderData = function(structure_json) {
    $('#fields').empty();
    $('#list').empty();
    structure_json.id = $("#order_id").val();

    $.post('/m_getOrderData', {
            params: JSON.stringify(structure_json)
        },
        function(json) {
            console.log('getOrderData....');
            console.log(json);

            buildFieldsStructureUI(structure_json);
            for (var i = 0; i < json.FIELDS_LIST.length; i++) {
                var fieldsObj = json.FIELDS_LIST[i];
                $.each(fieldsObj, function(key, value) {
                    $("#" + key).val(value);
                });
            }

            buildTableStructureUI(json);

            buildButtonUI(structure_json, json);

            bindProductSearch();
            bindBtnClick();
            $('[data-toggle=tooltip]').tooltip();
        }, 'json');
};

//新增时直接构造UI
var buildStructureUI = function(json) {
    for (var i = 0; i < json.STRUCTURE_LIST.length; i++) {
        var structure = json.STRUCTURE_LIST[i];

        if (!structure.FIELDS_LIST)
            continue;
        if (structure.STRUCTURE_TYPE == '字段') {
            generateField(structure);
        } else {
            generateTable(json, structure);
        }
    } //end of for
};

//编辑时回显构造field UI
var buildFieldsStructureUI = function(order_data_dto) {
    for (var i = 0; i < order_data_dto.STRUCTURE_LIST.length; i++) {
        var structure = order_data_dto.STRUCTURE_LIST[i];

        if (!structure.FIELDS_LIST)
            continue;
        if (structure.STRUCTURE_TYPE == '字段') {
            generateField(structure);
        }
    } //end of for
};

//编辑时回显(递归)构造table UI
var buildTableStructureUI = function(order_data_dto) {
    if(order_data_dto.TABLE_LIST.length>0){
        for (var i = 0; i < order_data_dto.TABLE_LIST.length; i++) {
            var tableObj = order_data_dto.TABLE_LIST[i];
            var tableStructure = getStructure(global_order_structure, tableObj.STRUCTURE_ID);
            var parent_table_row_id = order_data_dto.ID;

            generateTable(global_order_structure, tableStructure, parent_table_row_id);
            console.log('buildTableStructureUI: s_id='+tableObj.STRUCTURE_ID);

            var table_div = $('div [name=table_' + tableObj.STRUCTURE_ID+'_div]:last');
            table_div.attr('parent_table_row_id', order_data_dto.ID);
            var dataTable = table_div.find('table').DataTable();
            dataTable.clear();
            var row_list = tableObj.ROW_LIST;
            var row = {};
            console.log(row_list);
            if(row_list){
                for (var j = 0; j < row_list.length; j++) {
                    var row_rec = row_list[j];
                    $.each(row_rec, function(key, value) {
                        row[key] = value;
                    });
                    dataTable.row.add(row).draw(false);
                    buildTableStructureUI(row_rec);
                }
            }
        }
    }
};

var generateField=function(structure){
    var field_section_html = template('field_section', {
        id: structure.ID
    });

    $('#fields').append(field_section_html);
    var field_section = $('#fields #' + structure.ID + '>.col-lg-12');

    for (var j = 0; j < structure.FIELDS_LIST.length; j++) {
        var field = structure.FIELDS_LIST[j];
        console.log(field.FIELD_DISPLAY_NAME +'is_require:'+field.REQUIRED);
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
                label: field.FIELD_DISPLAY_NAME,
                is_require: field.REQUIRED
            });
        } else if (field.FIELD_TYPE == '日期编辑框') {
            field_html = template('input_date_field_template', {
                id: 'F' + field.ID + '_' + field.FIELD_NAME,
                label: field.FIELD_DISPLAY_NAME,
                is_require: field.REQUIRED
            });
        } else if (field.FIELD_TYPE == '下拉列表') {
            if (field.FIELD_TYPE_EXT_TYPE == '自定义列表值') {
                var valueStr = field.FIELD_TYPE_EXT_TEXT;
                var items = valueStr.split('\n');
                field_html = template('select_field_template', {
                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    items: items,
                    is_require: field.REQUIRED
                });
            } else if (field.FIELD_TYPE_EXT_TYPE == '客户列表') {
                global_customer_id = 'F' + field.ID + '_' + field.FIELD_NAME;
                field_html = template('input_customer_template', {
                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    is_require: field.REQUIRED
                });
            } else if (field.FIELD_TYPE_EXT_TYPE == '供应商列表') {
                field_html = template('input_sp_template', {
                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    value: '',
                    is_require: field.REQUIRED
                });
            }else if (field.FIELD_TYPE_EXT_TYPE == '产品列表') {
                field_html = template('input_product_template', {
                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    value: '',
                    is_require: field.REQUIRED
                });
            }else if (field.FIELD_TYPE_EXT_TYPE == '城市列表') {
                field_html = template('input_location_template', {
                    id: 'F' + field.ID + '_' + field.FIELD_NAME,
                    label: field.FIELD_DISPLAY_NAME,
                    value: '',
                    is_require: field.REQUIRED
                });
            }
        } else {
            field_html = template('input_field', {
                id: 'F' + field.ID + '_' + field.FIELD_NAME,
                label: field.FIELD_DISPLAY_NAME,
                is_require: field.REQUIRED
            });
        }

        field_section.append(field_html);
    }
};

var generateTable=function(order_structure_dto, structure, parent_table_row_id){
    var detail_table_id = checkHaveDetailTable(order_structure_dto, structure);//如果table是第二层，它需要知道下层table的ID
    var is_3rd_table = checkIs3rdTable(order_structure_dto, structure);

    var list_html = template('table_template', {
        customer_id: global_customer_id,
        id: structure.ID,
        structure_id: structure.ID,
        label: structure.NAME,
        field_list: structure.FIELDS_LIST,
        is_edit_order: true,
        parent_table_id: structure.PARENT_ID,  //如果table是第三层，它需要知道上层table的ID
        detail_table_id: detail_table_id,
        parent_table_row_id: parent_table_row_id,
        is_3rd_table: is_3rd_table
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

    if(!is_3rd_table){
        var dataTable = $('#table_' + structure.ID).DataTable(table_setting);
        global_data_table['table_' + structure.ID+'_dataTable'] = dataTable;
    }else{
        var dataTable = $('div [name=table_' + structure.ID+'_div]:last table').DataTable(table_setting);
        global_data_table['table_' + structure.ID+'_dataTable_'+parent_table_row_id] = dataTable;
    }
};

var checkHaveDetailTable = function(order_structure_dto, structure){
    var structure_id = structure.ID;
    for (var i = 0; i < order_structure_dto.STRUCTURE_LIST.length; i++) {
        var temp_structure = order_structure_dto.STRUCTURE_LIST[i];
        if(temp_structure.STRUCTURE_TYPE == '列表' && structure_id == temp_structure.PARENT_ID){
            return temp_structure.ID;
        }
    }
    return null;
}

var checkIs3rdTable = function(order_structure_dto, structure){
    var structure_2rd_id = structure.PARENT_ID;
    for (var i = 0; i < order_structure_dto.STRUCTURE_LIST.length; i++) {
        var temp_structure = order_structure_dto.STRUCTURE_LIST[i];
        if(structure_2rd_id == temp_structure.ID){
            if(temp_structure.PARENT_ID)
                return true;
        }
    }
    return false;
}

var getStructure= function(order_structure_dto, structure_id){
    for (var i = 0; i < order_structure_dto.STRUCTURE_LIST.length; i++) {
        var temp_structure = order_structure_dto.STRUCTURE_LIST[i];
        if(structure_id == temp_structure.ID){
            return temp_structure;
        }
    }
}

//--------------------product search-----------------
var bindProductSearch=function(){
    $('table input[field_type=product_search]').keyup(function(){

            var me = this;
            var inputStr = $(me).val();
            if(inputStr.length<2)
                return;


            $.get('/transferOrder/searchItemNo', {input:inputStr, customerId:$('#'+global_customer_id).val()}, function(data){
                $(me).parent().append('');
                var productList = $(me).parent().find('ul');
                productList.empty();
                if(data.length>0){
                    for(var i = 0; i < data.length; i++){
                        productList.append("<li><a tabindex='-1' class='fromLocationItem' item_id="+data[i].ID+" >"+data[i].ITEM_NO+"</a></li>");
                    }
                }else{
                    productList.append("<li><a tabindex='-1' item_id='' >无记录</a></li>");
                }
                
                productList.css({ 
                    left:$(me).position().left+"px", 
                    top:$(me).position().top+31+"px" 
                }); 
                
                productList.show();
            });
        });

        // 没选中，焦点离开，隐藏列表
        $('table input[field_type=product_search]').on('blur', function(){
            if ($(this).val().trim().length ==0) {
                $(this).parent().find('input[field_type=product_id]').val('');
            };
            $('table ul[name=product_list]').hide();
        });

        //当用户只点击了滚动条，没选中记录，再点击页面别的地方时，隐藏列表
        $('table ul[name=product_list]').on('blur', function(){
            $('table ul[name=product_list]').hide();
        });

        $('table ul[name=product_list]').on('mousedown', function(){
            return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
        });

        // 选中
        $('table ul[name=product_list]').on('mousedown', '.fromLocationItem', function(e){
            $(this).parent().parent().parent().find('input[field_type=product_search]').val($(this).text());
            $(this).parent().parent().parent().find('input[field_type=product_id]').val($(this).attr('item_id'));
            $('table ul[name=product_list]').hide();
        });
};


//--------------------product search-----------------
//});
