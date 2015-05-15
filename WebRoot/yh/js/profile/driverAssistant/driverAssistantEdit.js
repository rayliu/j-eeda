$(document).ready(function() {
	if($("#name").val()){
		document.title =$("#name").val() +' | '+document.title;
	}
	$('#menu_carmanage').addClass('active').find('ul').addClass('in');
	
	var academic_qualifications = $("#hid_academic_qualifications").val();
	$("#academic_qualifications").val(academic_qualifications);
	
	//from表单验证
	$('#driverAssistantForm').validate({
        rules: {
        	name: {
        		required: true
            },
            phone: {
            	required: true
          	},
          	identity_number: {
          		required: true
          	},
          	date_of_entry: {
          		required: true
          	},
          	daily_wage:{
          		number:true
          	}
        },
        highlight: function(element) {
            $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
        },
        success: function(element) {
            element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
        }
    });
	
	
	// 获取所有公司
    $.post('/carinfo/searchAllOffice',function(data){
		 if(data.length > 0){
			 var officeSelect = $("#officeSelect");
			 officeSelect.empty();
			 var hideOfficeId = $("#office_id").val();
			 officeSelect.append("<option class='form-control' value='' ></option>");
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
	
    
	$('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN'
	}).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
	    $('#date_of_entry').trigger('keyup');
	});		
		
	$('#datetimepicker2').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN', 
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
	    $('#beging_stamp').trigger('keyup');
	});
	
	$('#datetimepicker3').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN', 
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
	    $('#end_stamp').trigger('keyup');
	});
	
});