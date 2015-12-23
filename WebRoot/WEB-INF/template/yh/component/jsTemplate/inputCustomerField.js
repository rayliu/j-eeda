<script id="input_customer_template" type="text/html">
	<div class="col-lg-4">
		<input id="{{id}}" type="hidden" name="{{id}}" value="{{value}}"/>
		<div class="form-group">
		    <label class="search-label">{{label}}</label>
		    <input type="text" class="form-control search-control" 
		    id="{{id}}_input" placeholder="请选择客户" value="{{display_value}}">
		    <ul id='{{id}}_list' tabindex="-1" 
			    class="pull-right dropdown-menu default dropdown-scroll" 
			    style="top: 22%; left: 33%;">
		    </ul>
		</div>
	</div>
	<script>

	$(document).ready(function() {
		//获取客户列表，自动填充
		var companyList =$("#{{id}}_list");
		var inputField = $('#{{id}}_input');
		var hiddenField = $('#{{id}}');
		
		inputField.on('keyup click', function(event){
		    var me = this;
		    var inputStr = inputField.val();
		    
		     $.get("/customerContract/search", {customerName:inputStr}, function(data){
		    	if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
					return;
				}
		        companyList.empty();
		        for(var i = 0; i < data.length; i++)
		            companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
		            
		        companyList.css({ 
			    	left:$(me).position().left+"px", 
			    	top:$(me).position().top+28+"px" 
			    });
		        companyList.show();    
		    },'json');
		});
		
		companyList.on('click', '.fromLocationItem', function(e){
			inputField.val($(this).text());
		    companyList.hide();
		    var companyId = $(this).attr('partyId');
		    hiddenField.val(companyId);
		});

		// 1 没选中客户，焦点离开，隐藏列表
		inputField.on('blur', function(){
			if (inputField.val().trim().length ==0) {
				hiddenField.val('');
			};
			companyList.hide();
		});
		
		// 2 当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
		companyList.on('mousedown', function(){
		    return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
		});
		
	});
	</script>
</script>