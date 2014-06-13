$(document).ready(function() {
    $('#menu_profile').addClass('active').find('ul').addClass('in');
    
    $('#warehouseForm').validate({
        rules: {
        	warehouse_name: {
            required: true
          },
          	warehouse_address:{
            required: true
          },
          	company_name:{
            required: true
          },
          	contact_person:{
            required: true
          },
          	email:{
          	email: true
          },
          	warehouse_area:{
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
     	$.post('/yh/serviceProvider/province',function(data){
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
			$.get('/yh/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCity");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				toLocationList.show();
			},'json');
		});
     //获取城市的区县
     $('#cmbCity').on('change', function(){
     	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
		var code = $("#location").val(inputStr);
			$.get('/yh/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				toLocationList.show();
			},'json');
		});
     
     $('#cmbArea').on('change', function(){
     	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			var code = $("#location").val(inputStr);
		});         

     // 回显城市
		var hideProvince = $("#hideProvince").val();
     $.get('/yh/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
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
     $.get('/yh/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
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
     $.post('/yh/transferOrder/searchAllOffice',function(data){
		 if(data.length > 0){
			 var officeSelect = $("#officeSelect");
			 officeSelect.empty();
			 var hideOfficeId = $("#hideOfficeId").val();
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideOfficeId){
					 officeSelect.append("<option class='form-control' value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");
				 }else{
					 officeSelect.append("<option class='form-control' value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");					 
				 }
			 }
		 }
	 },'json');

	//获取供应商的list，选中信息在下方展示其他信息
	$('#spMessage').on('keyup', function(){
		var inputStr = $('#spMessage').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/yh/transferOrder/searchSp', {input:inputStr}, function(data){
			console.log(data);
			var spList =$("#spList");
			spList.empty();
			for(var i = 0; i < data.length; i++)
			{
				spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+" "+data[i].CONTACT_PERSON+" "+data[i].PHONE+"</a></li>");
			}
		},'json');
		
        $('#spList').show();
        $("#spList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        
	});
	
	// 选中供应商
	$('#spList').on('click', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#spMessage').val(message.substring(0, message.indexOf(" ")));
		$('#sp_id').val($(this).attr('partyId'));
		var pageSpName = $("#pageSpName");
		pageSpName.empty();
		pageSpName.append($(this).attr('contact_person')+'&nbsp;');
		pageSpName.append($(this).attr('phone')); 
		var pageSpAddress = $("#pageSpAddress");
		pageSpAddress.empty();
		pageSpAddress.append($(this).attr('address'));
        $('#spList').hide();
    }); 
	
	$("input[name='warehouseType']").click(function(){
		if($(this).attr('id') == 'warehouseType1'){
			$("#officeDiv").show();
			$("#spDiv").hide();
		}else{
			$("#spDiv").show();
			$("#officeDiv").hide();
		}
	});
	
	if($("#warehouseType1").attr('checked') == 'checked'){
		$("#officeDiv").show();
	}
	
	// 回显仓库类型
	$("input[name='warehouseType']").each(function(){
		if($("#warehouseTypeHide").val() == $(this).val()){
			$(this).attr('checked', true);
			if($(this).attr('id') == 'warehouseType1'){
				$("#officeDiv").show();
				$("#spDiv").hide();
			}else{
				$("#spDiv").show();
				$("#officeDiv").hide();
			}
		}
	});
} );