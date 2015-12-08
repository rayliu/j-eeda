<script id="${id}" type="text/html">

    <section id="{{id}}_section" class='well structure'>
        <input type="hidden" 
                    class="s_id"
                    value="{{s_id}}">
        <div class="col-lg-4">
            <div class="form-group">
                <label class="search-label">名称</label>
                <input type="text" 
                    id="{{id}}_name" 
                    class="s_name form-control search-control"
                    value="{{s_name}}">
            </div>
        </div>
        <div class="col-lg-4">
            <div class="form-group">
                <label class="search-label">显示类型</label>
                <select class="s_type form-control search-control">
                    <option {{if s_type=='列表'}}selected{{/if}}>列表</option>
                    <option {{if s_type=='字段'}}selected{{/if}}>字段</option>
                </select>
            </div>
        </div>
        <div class="col-lg-4">
            <input type="hidden" 
                    class="s_parent_id"
                    value="{{s_parent_id}}">
            <div class="form-group">
                <label class="search-label">关联上级</label>
                <select class="s_type form-control search-control">
                    {{each structure_names as s_parent_name}}
                        <option>{{s_parent_name}}</option>
                    {{/each}}
                </select>
            </div>
        </div>
        <div class="form-group button-bar" >
            <button section_index="{{id}}" name="addFieldBtn" type="button" class="btn btn-success btn-xs">添加新字段</button>
        </div>
        <table id="{{id}}_table" class="display" cellspacing="0" style="width: 100%;">
            <thead class="eeda">
                <tr>
                    <th></th>
                    <th>id</th>
                    <th>字段名</th>
                    <th>字段显示</th>
                    <th>字段属性</th>
                    <th>必填</th>
                    <th>列表中显示</th>
                    <th>字段模板</th>
                    <th>索引名称</th>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </section>
</script>