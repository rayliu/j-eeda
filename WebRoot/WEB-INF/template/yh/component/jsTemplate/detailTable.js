<script id="${id}" type="text/html">
    <div class="col-lg-12">
        <h4>{{label}}</h4>
        <div class="form-group button-bar" >
            <button name="addRowBtn" type="button" class="btn btn-success btn-xs">添加</button>
        </div>
        <table class="display" cellspacing="0" style="width: 100%;">
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
</script>