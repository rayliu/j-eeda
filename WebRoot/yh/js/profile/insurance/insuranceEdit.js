
$(document).ready(function(){
	$('#menu_profile').addClass('active').find('ul').addClass('in');
	
	//from表单验证
	var validate = $('#insuranceForm').validate({
        rules: {
        	insuranceName: {
        		required: true
            },
            company_name: {
            	required: true
          	},
          	abbr: {
          		required: true
          	}
        }
    });
	
	//保存
    $('#saveBtn').click(function(e){
    	//提交前，校验数据
        if(!$("#insuranceForm").valid()){
	       	return false;
        }
        $.post('/insurance/saveInsurance', $("#insuranceForm").serialize(), function(order){
	    	if(order.ID){
	    		$("#insuranceId").val(order.ID);
	    		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    	}else{
	    		$.scojs_message('数据保存失败', $.scojs_message.TYPE_ERROR);
	    	}
    	});
    });
	
	
	
	
	
	
	
});