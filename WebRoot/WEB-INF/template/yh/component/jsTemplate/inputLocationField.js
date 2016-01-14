<script id="input_location_template" type="text/html">
	<div class="col-lg-4">
		<div class="form-group">
			<input id="{{id}}" type="text" name="{{id}}" field_type='list' value="{{value}}" style="display:none;"/>
			<label class="search-label">{{label}}</label>
			<input type="text" class="form-control search-control" 
			id="{{id}}_INPUT" name="{{id}}_INPUT" 
			placeholder="请选择城市"  value="{{display_value}}">
			<div id="{{id}}_list" class="area-list pull-right dropdown-menu default dropdown-scroll" tabindex="-1"  
			style="top: 35%; left: 2%; display: none;">
				<div class="area-list-title">
					<input data-id="0" data-level="0" type="button" value="省份" class="this">
					<input data-id="0" data-level="1" type="button" value="城市">
					<input data-id="0" data-level="2" type="button" value="县区">
					<span class='tips'>如不需选县区，请点击外面空白区域</span>
				</div>
				<div class="area-list-content" style="clear:both;">
					
				</div>
			</div>
				
			<ul id='{{id}}_list——1' class="pull-right dropdown-menu default dropdown-scroll" tabindex="-1" style="top: 35%; left: 2%;">
			</ul>
		</div>
	</div>	
	
	<script>

	$(document).ready(function() {
		//获取sp列表，自动填充
		var spList =$("#{{id}}_list");
		var spListContent =$("#{{id}}_list .area-list-content");
		var inputField = $('#{{id}}_INPUT');
		var hiddenField = $('#{{id}}');
		
		//供应商查询
		var searchLocation = function(level, code){
			var locLevel = "province";
			level = level | 0;
			if(level == 1){
				locLevel = "city";
			}
			if(level == 2){
				locLevel = "area";
			}

			$.get('/serviceProvider/'+locLevel, {id:code}, function(data){
				spListContent.empty();
				for(var i = 0; i < data.length; i++){
					var loc = data[i];
					spListContent.append('<a next-level="'+(level+1)+'" p_code="'+loc.PCODE+'" href="javascript:void(0)" code="'+loc.CODE+'" name="'+loc.NAME+'">'+loc.NAME+'</a>');
				}
				spList.find('input').removeClass('this');
	    		spList.find('input[data-level='+level+']').addClass('this');
				spList.show();
				
			},'json');
		};
	    //获取供应商的list，选中信息在下方展示其他信息
	    inputField.on('input click', function(){
	    	var me = this;
			var inputStr = inputField.val();
			
			searchLocation();
			spList.css({ 
	        	left:$(me).position().left+"px", 
	        	top:$(me).position().top+30+"px" 
	        }); 
	    });

	    spListContent.on('click', 'a', function(){
	    	var dataLevel = $(this).attr('next-level');
	    	var code = $(this).attr('code');
	    	var name = $(this).attr('name');
	    	var oldValue = inputField.val();
	    	if(dataLevel>1){
	    		name = oldValue+'-'+name;
	    	}
	    	inputField.val(name);
	    	hiddenField.val(code);

	    	if(dataLevel == 3){
	    		spList.hide();
	    		return;
	    	}
	    	searchLocation(dataLevel, code);

	    	spList.find('input').removeClass('this');
	    	spList.find('input[data-level='+dataLevel+']').addClass('this');
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

	
	});
	</script>
</script>