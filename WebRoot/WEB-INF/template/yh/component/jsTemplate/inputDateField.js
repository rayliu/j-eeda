<script id="input_date_field_template" type="text/html">
    <div class="col-lg-4">
        <div class="form-group">
            <div id="{{id}}_div" class="input-append date ">
                <label class="search-label">{{label}}
                {{if is_require=='Y'}} <span style='color:red;display: inherit;'>*</span> {{/if}}
                </label>
                <input id="{{id}}" name="{{id}}" class="form-control search-control  beginTime_filter" type="text" value="">
                <span class="add-on"> 
                	<i class="fa fa-calendar" data-time-icon="icon-time" data-date-icon="icon-calendar"></i>
                </span>
            </div>
        </div>
    </div>
    <script>
    $(document).ready(function() {
    	//时间控件
    	$('#{{id}}_div').datetimepicker({  
    	    format: 'yyyy-MM-dd',  
    	    language: 'zh-CN'
    	}).on('changeDate', function(ev){
    	    $(".bootstrap-datetimepicker-widget").hide();   
    	    $('#{{id}}').trigger('keyup');
    	});

        //回显
        var dateValue = '{{value}}';
        if(dateValue){
            $('#{{id}}').val(dateValue.substr(0,10));
        }
    });
    </script>
</script>