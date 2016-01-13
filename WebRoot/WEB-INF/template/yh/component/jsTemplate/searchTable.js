<script id="search_table_template" type="text/html">
    <div class="col-lg-12">
        {{if is_edit_order}}
            <h4>{{label}}</h4>
            <div class="form-group button-bar" >
                <button id="add_row_btn_{{id}}" table_id="table_{{id}}" name="addRowBtn" type="button" class="btn btn-success btn-xs">添加</button>
            </div>
        {{/if}}
        <table id="table_{{id}}" structure_id="{{structure_id}}" class="display" cellspacing="0" style="width: 100%;">
            <thead class="eeda">
                <tr>
                    <th></th>
                    {{each field_list as field}}
                        <th>{{field.FIELD_DISPLAY_NAME}}</th>
                    {{/each}}
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>
    <script>
        var table_{{id}}_setting = {
            paging:true,
            info: true,
            serverSide: true,
            deferLoading: 0, //初次不查数据
            "processing": true,
            "searching": false,
            "autoWidth": true,
            "language": {
                "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
            },
            "createdRow": function ( row, data, index ) {
                var id='';
                if(data.ID){
                    id=data.ID;
                }
                $(row).attr('id', id);
                $(row).append('<input type="hidden" name="id" value="' + id + '"/>');
            },
            "columns": [
                { "width": "10px", "orderable":false, 
                    "render": function ( data, type, full, meta ) {
                      return '<a class="edit"  target="_blank" href="/m/{{module_id}}-'+full.ID+'" title="编辑"><i class="fa fa-edit"></i></a>';
                    }
                },
                {{each field_list as field}}
                    {{if (field.FIELD_TYPE == '下拉列表' 
                        && (
                            field.FIELD_TYPE_EXT_TYPE =='客户列表' || field.FIELD_TYPE_EXT_TYPE =='供应商列表' || field.FIELD_TYPE_EXT_TYPE =='城市列表'
                           )
                        ) }}
                        { "data": "F{{field.ID}}_{{field.FIELD_NAME}}_INPUT",
                            "render": function ( data, type, full, meta ) {
                                if(!data)
                                    data = '';
                                return data;
                           } 
                        },
                    {{else}}
                        { "data": "F{{field.ID}}_{{field.FIELD_NAME}}",
                            "render": function ( data, type, full, meta ) {
                                if(!data)
                                    data = '';
                                return data;
                           } 
                        },
                    {{/if}}
                {{/each}}
            ]
        }

        var table_{{id}}_row = {
            {{each field_list as field}}
                F{{field.ID}}_{{field.FIELD_NAME}}: '',
            {{/each}}
        };
    </script>
</script>