<script id="table_add_btn_condtion_template" type="text/html">
	
        <div class="col-lg-12" style = "margin-top: 3px;">
            <a class="delete" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i> </a>
            <select name="modal_condition_field_name" class="form-control" style="display: initial;width: initial;">
           		{{each field_list as field}}
                	<option value='{{JsonStringify field}}'
                        {{if field.FIELD_DISPLAY_NAME == display_name}}selected{{/if}}
                    >{{field.FIELD_DISPLAY_NAME}}</option>
                {{/each}}
                <option>自定义</option>
            </select>
            <select class="form-control" name="condition" style="display: initial;width: 20%;">
                <option {{if condition == '='}}selected{{/if}}>=</option>
                <option {{if condition == '!='}}selected{{/if}}>!=</option>
                <option {{if condition == '包含'}}selected{{/if}}>包含</option>
                <option {{if condition == '不包含'}}selected{{/if}}>不包含</option>
            </select>
            <input class="form-control" type="text" name="condition_value" style="display: initial;width: 50%;" value="{{condition_value}}">
        </div>
    
</script>