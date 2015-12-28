$(document).ready(function() {
	if(warehouse_name){
		document.title = warehouse_name+' | '+document.title;
	}
    $('#menu_profile').addClass('active').find('ul').addClass('in');
    
    $('#warehouseForm').validate({
        rules: {
        	warehouse_name: {
            required: true
          },
          	warehouse_address:{
            required: true
          },
          	email:{
          	email: true
          },
          	warehouse_area:{
          	required: true,
          	number:true
          },
          officeSelect:{
        	  required: true 
          }
        },
        highlight: function(element) {
            $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
        },
        success: function(element) {
            element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
        }
    }); 
    
    //获取全国省份
    $(function(){
     	var province = $("#mbProvince");
     	$.post('/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
				var hideProvince = $("#hideProvince").val();
     		for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideProvince){
     				province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
     				
     				
					}else{
     				province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     		
     	},'json');
    });
     //获取省份的城市
     $('#mbProvince').on('change', function(){
     	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCity");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			},'json');
		});
     //获取城市的区县
     $('#cmbCity').on('change', function(){
     	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
		var code = $("#location").val(inputStr);
			$.get('/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
			},'json');
		});
     
     $('#cmbArea').on('change', function(){
     	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			var code = $("#location").val(inputStr);
		});         

     // 回显城市
	 var hideProvince = $("#hideProvince").val();
     $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
			if(data.length > 0){
				var cmbCity =$("#cmbCity");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				var hideCity = $("#hideCity").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideCity){
						cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
	 },'json');

     // 回显区
     var hideCity = $("#hideCity").val();
     $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
			if(data.length > 0){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				var hideDistrict = $("#hideDistrict").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideDistrict){
						cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
     
	// 获取所有城市
     $.post('/transferOrder/searchAllOffice',function(data){
		 if(data.length > 0){
			 var officeSelect = $("#officeSelect");
			 officeSelect.empty();
			 var hideOfficeId = $("#hideOfficeId").val();
			 officeSelect.append("<option class='form-control'></option>");
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideOfficeId){
					 officeSelect.append("<option class='form-control' value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");
				 }else{
					 if(data[i].IS_STOP != true){
						 officeSelect.append("<option class='form-control' value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");
					 }
					 					 
				 }
			 }
		 }
	 },'json');
	
	$("input[name='warehouseType']").click(function(){
		if($(this).attr('id') == 'warehouseType1'){
			$("#officeDiv").show();
		}else{
			$("#officeDiv").hide();
		}
	});
	
	if($("#warehouseType1").attr('checked') == 'checked'){
		$("#officeDiv").show();
	}
	$('#spMessage').on('keyup click', function(){
		var inputStr = $('#spMessage').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/delivery/searchPartSp', {input:inputStr}, function(data){			
			var spList =$("#spList");
			spList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var abbr = data[i].ABBR;
				if(abbr == null){
					abbr = '';
				}
				var company_name = data[i].COMPANY_NAME;
				if(company_name == null){
					company_name = '';
				}
				var contact_person = data[i].CONTACT_PERSON;
				if(contact_person == null){
					contact_person = '';
				}
				var phone = data[i].PHONE;
				if(phone == null){
					phone = '';
				}
				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');

		$("#spList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#spList').show();
	});

	// 没选中供应商，焦点离开，隐藏列表
	$('#spMessage').on('blur', function(){
 		$('#spList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#spList').on('blur', function(){
 		$('#spList').hide();
 	});

	$('#spList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	// 选中供应商
	$('#spList').on('mousedown', '.fromLocationItem', function(e){
		$('#spMessage').val($(this).text());
		$('#sp_id').val($(this).attr('spid'));
		$('#cid').val($(this).attr('code'));
		$('#a1').html($(this).attr('contact_person'));
		$('#a2').html($(this).attr('company_name'));
		$('#a3').html($(this).attr('address'));
		$('#a4').html($(this).attr('mobile'));
        $('#spList').hide();
    });
	// 回显仓库类型
	$("input[name='warehouseType']").each(function(){
		if($("#warehouseTypeHide").val() == $(this).val()){
			$(this).attr('checked', true);
			if($(this).attr('id') == 'warehouseType1'){
				$("#officeDiv").show();
			}else{
				$("#officeDiv").hide();
			}
		}
	});
	
	// 回显状态
	$("input[name='warehouseStatus']").each(function(){
		if($("#warehouseStatusHide").val() == $(this).val()){
			$(this).attr('checked', true);
		}
	});
	//选中网点回显目的地
	$("#officeSelect").change(function(){
		var officeId = $(this).val();
		$.get('/warehouse/findDocaltion', {"officeId":officeId}, function(data){
			console.log(data);
			if(data != null && data != ""){
				searchAllLocationFrom(data);
			}
		},'json');
	});
	var searchAllLocationFrom = function(locationFrom){
    	$.get('/transferOrder/searchLocationFrom', {locationFrom:locationFrom}, function(data){
    		console.log(data);			
    		var provinceVal = data.PROVINCE;
    		var cityVal = data.CITY;
    		var districtVal = data.DISTRICT;
	        $.get('/serviceProvider/searchAllLocation', {province:provinceVal, city:cityVal}, function(data){	
		        //获取全国省份
	         	var province = $("#mbProvince");
	     		province.empty();
	     		province.append("<option>--请选择省份--</option>");
	     		for(var i = 0; i < data.provinceLocations.length; i++){
					if(data.provinceLocations[i].NAME == provinceVal){
						$("#location").val(data.provinceLocations[i].CODE);
						province.append("<option value= "+data.provinceLocations[i].CODE+" selected='selected'>"+data.provinceLocations[i].NAME+"</option>");
					}else{
						province.append("<option value= "+data.provinceLocations[i].CODE+">"+data.provinceLocations[i].NAME+"</option>");						
					}
				}

				var cmbCity =$("#cmbCity");
	     		cmbCity.empty();
				cmbCity.append("<option  value=''>--请选择城市--</option>");
				for(var i = 0; i < data.cityLocations.length; i++)
				{
					if(data.cityLocations[i].NAME == cityVal){
						$("#location").val(data.cityLocations[i].CODE);
						cmbCity.append("<option value= "+data.cityLocations[i].CODE+" selected='selected'>"+data.cityLocations[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data.cityLocations[i].CODE+">"+data.cityLocations[i].NAME+"</option>");						
					}
				}
				
				if(data.districtLocations.length > 0){
    				var cmbArea =$("#cmbArea");
    				cmbArea.empty();
    				cmbArea.append("<option  value=''>--请选择区(县)--</option>");
    				for(var i = 0; i < data.districtLocations.length; i++)
    				{
    					if(data.districtLocations[i].NAME == districtVal){
    						$("#location").val(data.districtLocations[i].CODE);
    						cmbArea.append("<option value= "+data.districtLocations[i].CODE+" selected='selected'>"+data.districtLocations[i].NAME+"</option>");
    					}else{
    						cmbArea.append("<option value= "+data.districtLocations[i].CODE+">"+data.districtLocations[i].NAME+"</option>");						
    					}
    				}
    			}else{
    				var cmbArea =$("#cmbArea");
    				cmbArea.empty();
    			}
	        },'json');
	        
    	},'json');
    };
} );