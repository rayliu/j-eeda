<script id="${id}" type="text/html">

    <section id="{{id}}_section" class='well structure'>
        <a data-bind="click: deleteTableSection"
           class="remove delete" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i> </a>
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
        <div class="col-lg-4" style="display: none;">
            <div class="form-group">
                <label class="search-label">关联上级</label>
                <select class="s_type form-control search-control">
                    {{each structure_names as s_parent_name}}
                        <option>{{s_parent_name}}</option>
                    {{/each}}
                </select>
            </div>
        </div>
        
        <div class="col-lg-4" name="add_btn_type_field" style= "
            {{if s_type=='字段'}}display:none;{{/if}}
        ">
            <div class="form-group">
                <label class="search-label">点"新增"时</label>
                <select class="s_add_btn_type form-control search-control" style="width:60%;">
                    <option {{if s_add_btn_type=='添加空行'}}selected{{/if}}>添加空行</option>
                    <option {{if s_add_btn_type=='弹出列表, 从其它数据表选取'}}selected{{/if}}>弹出列表, 从其它数据表选取</option>
                </select>
                 
                <a class="addBtnSetting" name="addBtnSetting" style="cursor:pointer;
                    {{if s_add_btn_type=='添加空行'}} display:none;{{/if}}
                ">
                    <i class="fa fa-edit"></i>
                </a>
                
                <input type="hidden" 
                    name="s_add_btn_setting"
                    value="{{s_add_btn_setting}}">
            </div>
        </div>
        
        <div class="col-lg-4">
            <div class="form-group">
                <label class="search-label">ID</label>
                <input type="text" 
                        class="s_id form-control search-control"
                        value="{{s_id}}" disabled>
            </div>
        </div>
        
        <div class="col-lg-4">
            <div class="form-group">
                <label class="search-label">上级ID
                    <i class="fa fa-info-circle" data-toggle="tooltip" data-placement="top" title="主从表结构，此处填上级表ID"></i>
                </label>
                
                <input type="text" 
                        class="s_parent_id form-control search-control"
                        value="{{s_parent_id}}">
            </div>
        </div>
        
        <div class="col-lg-12">
            
            
        </div>
        <div class="col-lg-12 form-group button-bar" >
            <button section_index="{{id}}" name="addFieldBtn" type="button" class="btn btn-success btn-xs">添加新字段</button>
            <label style="margin-top: 5px;">注意: 表中默认有<span style="color:red;" >ID, PARENT_ID, REF_T_ID</span>字段，请勿添加同名字段。</label>
        </div>
        <table id="{{id}}_table" class="display" cellspacing="0" style="width: 100%;">
            <thead class="eeda">
                <tr>
                    <th></th>
                    <th>id</th>
                    <th>字段名</th>
                    <th>字段显示属性</th>
                    <th>字段显示属性ext</th>
                    <th>字段显示属性ext_text</th>
                    <th>字段数据属性</th>
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