<script id="${id}" type="text/html">
	<div class="col-lg-4">
		<div class="form-group">
		    <label class="search-label">{{label}}
		    {{if is_require=='Y'}} <span style='color:red;display: inherit;'>*</span> {{/if}}
		    </label>
		    <input type="text" 
		    	id="{{id}}"
		    	name="{{id}}"
		    	field_type="{{field_type}}"
				class="form-control search-control" 
				{{disabled}} 
				placeholder="{{placeholder}}" 
				value="{{value}}"
				>
		</div>
	</div>
</script>