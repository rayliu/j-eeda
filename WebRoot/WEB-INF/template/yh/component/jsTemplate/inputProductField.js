<script id="input_product_template" type="text/html">
	<div class="">
		<div class="">
			<input type="text" field_type="product_id" name="{{id}}" value="{{value}}" style="display:none;"/>
			<input type='text' field_type="product_search" name="{{id}}_INPUT" value='{{display_value}}' placeholder='先选择客户并至少输入两个字符查询' class="form-control" >
			<ul name="product_list" class="pull-right dropdown-menu default dropdown-scroll" tabindex="-1" style="width: 30%;top: 35%; left: 2%;"></ul>
		</div>
	</div>
</script>