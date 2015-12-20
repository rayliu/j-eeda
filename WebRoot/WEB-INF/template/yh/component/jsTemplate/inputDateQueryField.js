<script id="input_date_query_template" type="text/html">
    <div class="col-lg-4">
        <div class="form-group">
            <div id="{{id}}_begin_div" class="input-date">
                <label class="search-label">{{label}}</label>
                <input id="{{id}}_begin_time" class="form-control search-control date" type="text" value="">
                <span class="add-on"> 
                	<i class="fa fa-calendar" data-time-icon="icon-time" data-date-icon="icon-calendar"></i>
                </span>
            </div> ~
            <div id="{{id}}_end_div" class="input-date">
                <input id="{{id}}_end_time" class="form-control search-control date" type="text" value="">
                <span class="add-on"> 
                    <i class="fa fa-calendar" data-time-icon="icon-time" data-date-icon="icon-calendar"></i>
                </span>
            </div>
        </div>
    </div>
    <script>
    $(document).ready(function() {
    	//时间控件
    	$('#{{id}}_begin_div').datetimepicker({  
    	    format: 'yyyy-MM-dd',  
    	    language: 'zh-CN'
    	}).on('changeDate', function(ev){
    	    $(".bootstrap-datetimepicker-widget").hide();   
    	    $('#{{id}}_begin_time').trigger('keyup');
    	});

        //回显
        var dateValue_begin = '{{value}}';
        if(dateValue_begin){
            $('#{{id}}_begin_time').val(dateValue_begin.substr(0,10));
        }

        //时间控件
        $('#{{id}}_end_div').datetimepicker({  
            format: 'yyyy-MM-dd',  
            language: 'zh-CN'
        }).on('changeDate', function(ev){
            $(".bootstrap-datetimepicker-widget").hide();   
            $('#{{id}}_end_time').trigger('keyup');
        });

        //回显
        var dateValue_end = '{{value}}';
        if(dateValue_end){
            $('#{{id}}_end_time').val(dateValue_end.substr(0,10));
        }
    });
    </script>
</script>