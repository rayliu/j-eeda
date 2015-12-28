<script id="table_add_btn_fillback_template" type="text/html">
	
        <div class="col-lg-12" style = "margin-top: 3px;">
            <a class="delete" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i> </a>
            <select name="modal_from_field_name" class="form-control" style="display: initial;width: initial;">
           		{{each field_list as field}}
                	<option value='structure_id:{{field.STRUCTURE_ID}}, field_name:F{{field.ID}}_{{field.FIELD_NAME}}, display_name:{{field.FIELD_DISPLAY_NAME}}'>{{field.FIELD_DISPLAY_NAME}}</option>
                {{/each}}
                <option>自定义</option>
            </select>
            ->
            <select name="modal_to_field_name" class="form-control" style="display: initial;width: initial;">
                {{each field_list as field}}
                    <option value='structure_id:{{field.STRUCTURE_ID}}, field_name:F{{field.ID}}_{{field.FIELD_NAME}}, display_name:{{field.FIELD_DISPLAY_NAME}}'>{{field.FIELD_DISPLAY_NAME}}</option>
                {{/each}}
            </select>
        </div>
    
</script>