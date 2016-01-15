<script id="input_sp_template" type="text/html">
	<div class="col-lg-4">
		<div class="form-group">
			<input id="{{id}}" type="text" name="{{id}}" field_type='list' value="{{value}}" style="display:none;"/>
			<label class="search-label">{{label}}
			{{if is_require}} <span style='color:red;display: inherit;'>*</span> {{/if}}
			</label>
			<input type="text" class="form-control search-control" 
			id="{{id}}_INPUT" name="{{id}}_INPUT" 
			placeholder="请选择供应商"  value="{{display_value}}">
			<ul id='{{id}}_list' class="pull-right dropdown-menu default dropdown-scroll" tabindex="-1" style="top: 35%; left: 2%;">
			</ul>
		</div>
	</div>	
	
	<script>

	$(document).ready(function() {
		//获取sp列表，自动填充
		var spList =$("#{{id}}_list");
		var inputField = $('#{{id}}_INPUT');
		var hiddenField = $('#{{id}}');
		
		//供应商查询
	    //获取供应商的list，选中信息在下方展示其他信息
	    inputField.on('input click', function(){
	    	var me = this;
			var inputStr = inputField.val();
			
			$.get('/customerContract/searchSp', {spName:inputStr}, function(data){
				if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
					return;
				}
				spList.empty();
				for(var i = 0; i < data.length; i++){
					var abbr = data[i].ABBR;
					var company_name = data[i].COMPANY_NAME;
					var contact_person = data[i].CONTACT_PERSON;
					var phone = data[i].PHONE;
					
					if(abbr == null) 
						abbr = '';
					if(company_name == null)
						company_name = '';
					if(contact_person == null)
						contact_person = '';
					if(phone == null)
						phone = '';
					
					spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].ID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+"</a></li>");
				}
				spList.css({ 
		        	left:$(me).position().left+"px", 
		        	top:$(me).position().top+28+"px" 
		        }); 
				
				spList.show();
				
			},'json');
	    });
	    
	    // 没选中供应商，焦点离开，隐藏列表
		inputField.on('blur', function(){
			if (inputField.val().trim().length ==0) {
				hiddenField.val('');
			};
			spList.hide();
	 	});

		//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
		spList.on('blur', function(){
			spList.hide();
	 	});

		spList.on('mousedown', function(){
			return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
		});

		// 选中供应商
		spList.on('mousedown', '.fromLocationItem', function(e){
			//console.log($('#spList').is(":focus"));
			inputField.val($(this).text());
			hiddenField.val($(this).attr('partyId'));
			spList.hide();
	    });
	    
		
	});
	</script>
</script>