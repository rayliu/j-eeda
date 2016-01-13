<script id="select_field_template" type="text/html">
	<div class="col-lg-4">
		<div class="form-group">
		    <label class="search-label">{{label}}</label>
		    <select id="{{id}}" name="{{id}}" 
		    	field_type='list'
		    	class="form-control search-control" {{disabled}}>
		    	{{if isSearch}}
		    		<option></option>
		    	{{/if}}
		    	{{each items as item}}
                    <option>{{item}}</option>
                {{/each}}
		    </select>
		</div>
	</div>
</script>