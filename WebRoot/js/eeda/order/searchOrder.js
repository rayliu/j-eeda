
//$(document).ready(function(template) {

    //$('#menu_sys_profile').addClass('active').find('ul').addClass('in');
    $('[data-toggle=tooltip]').tooltip();

    $.post('/module/getOrderStructure', {module_id: $("#module_id").val()}, function(json){
        console.log('getOrderStructure....');
        console.log(json);

        $('#module_name').text(json.MODULE_NAME);
        document.title = json.MODULE_NAME + '查询 | ' + document.title;

        $('#fields_body').empty();

        buildStructureUI(json);
    }, 'json');

    var buildStructureUI = function(json){
            var structure = json.STRUCTURE_LIST[0];//主表结构

            if(!structure.FIELDS_LIST)
                return;

            if(structure.STRUCTURE_TYPE == '字段'){
                var field_html = template('input_hidden_field', 
                    {
                        id: 'structure_id',
                        value: structure.ID
                    }
                );
                $('#fields').append(field_html);

                buildQueryFields(structure);
                buildResultList(structure);
            }
    };

    var buildResultList = function(structure){
        var list_html = template('search_table_template', 
                {
                    id: structure.ID,
                    label: structure.NAME,
                    field_list: structure.FIELDS_LIST,
                    module_id: $('#module_id').val()
                }
            );
        $('#list').append(list_html);

        //setting 是动态跟随table生成的
        var table_setting = window['table_' + structure.ID + '_setting'];
        $('#list table:last').DataTable(table_setting);
    };

    var buildQueryFields = function(structure){
        for (var j = 0; j < structure.FIELDS_LIST.length; j++) {
            var field = structure.FIELDS_LIST[j];

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
                        isSearch: true,
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
            
            $('#fields').append(field_html);
        }
    };

    var buildStructureSearchUrl=function(){
        var url = '/m_search?';
        var search_inputs = $("section#fields input");

        var search_list=[];
        for(var i=0; i<search_inputs.length; i++){
            var input_field = $(search_inputs[i]);
            var name = input_field.attr('name');
            var value = input_field.val();

            url += '&' + name + '=' + value;
        }
        
        return url;
    };

    $('#searchBtn').on('click', function(e){
        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();

        var url = buildStructureSearchUrl();

        console.log('searchBtn.click....');

        var dataTable = $('#list table:last').DataTable();
        dataTable.ajax.url(url).load();
    });

    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
    });
//});