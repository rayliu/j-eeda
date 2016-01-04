<script id="table_add_btn_field_template" type="text/html">
	
        <div class="col-lg-12" style = "margin-top: 3px;">
            <a class="delete" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i> </a>
            <select name="modal_col_field_name" class="form-control" style="display: initial;width: initial;">
           		{{each field_list as field}}
                	<option value='{{JsonStringify field}}'
                		{{if field.FIELD_DISPLAY_NAME == display_name}}selected{{/if}}
                	 >{{field.FIELD_DISPLAY_NAME}}</option>
                {{/each}}
                <option>自定义</option>
            </select>
            <input class="form-control" type="text" name="name" style="display: none;width: 20%;" placeholder="字段名" value="{{name}}">
            <input class="form-control" type="text" name="sql_name" style="display: none;width: 50%;" placeholder="自定义SQL" value="{{sql_name}}">
        </div>
    
</script>