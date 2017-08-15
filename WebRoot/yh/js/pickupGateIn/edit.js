$(document).ready(function() {

	document.title = order_no + ' | ' + document.title;

    $('#menu_status').addClass('active').find('ul').addClass('in');

    $('[name=pickupMode]').on('change',function(){
    	var pickupMode = this.value;
    	if(pickupMode == 'own'){
    		$('#spDiv').hide();
    		$('#costDiv').hide();
    		$('#cost_table').dataTable().fnClearTable();

    		$('#sp_id').val("");
    		$('#sp_id_input').val("");
    	}else{
    		$('#spDiv').show();
    		$('#costDiv').show();
    	}
    	$('#sp_id').val("");
		$('#sp_id_input').val("");
		
		$('#driver_id').val("");
		$('#driver_name').val("");
		$('#car_no').val("");
		$('#driver_phone').val("");
    });
    

    //------------save
    $('#saveBtn').click(function(e){
        //阻止a 的默认响应行为，不需要跳转
    	var self = this;
        e.preventDefault();
        //提交前，校验数据
        if(!$("#orderForm").valid()){
            return;
        }

        var orderJson = {
            id: $('#order_id').val(), 
            sp_id: $('#sp_id').val(),  
            car_id: $('#car_id').val(),
            pickup_mode: $('[name=pickupMode]:checked').val(),  
            driver_name: $('#driver_name').val(),  
            driver_phone: $('#driver_phone').val(),
            car_no: $('#car_no').val(), 
            remark: $('#remark').val(),  
            trans_to: $('#trans_to').val(),  
            depart_id:$('#depart_id').val(),  
           
            cost_list: order.buildCostDetail()
        };
        $(self).attr('disabled', true);
        //异步向后台提交数据
        $.post('/pickupGateIn/save', {params:JSON.stringify(orderJson)}, function(data){
            if(data.ID>0){
            	contactUrl("edit?id",data.ID);
            	$('#order_id').val(data.ID);
            	$('#order_no').val(data.ORDER_NO);
            	$('#create_stamp').val(data.CREATE_STAMP);
            	$('#creator_name').val(data.CREATE_NAME);
            	$('#status').val(data.STATUS);
            	
            	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
            	$(self).attr('disabled', false);
            	$('#gateInBtn').attr('disabled', false);
            	
            	order.refleshCostTable(data.ID);
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $(self).attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $(self).attr('disabled', false);
          });
    });

    

    
	
	// 列出所有的车辆
	$('#driver_name').on('keyup click', function(){
		var inputStr = '';
		if($('#driver_name').is(":focus")){
			inputStr = $('#driver_name').val();
		}
		
		$.get('/transferOrder/searchAllCarInfo', {input:inputStr,type:"own"}, function(data){
			var driverList = $("#driverList");
			driverList.empty();
			for(var i = 0; i < data.length; i++){ 	
	 			driverList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' carNo='"+data[i].CAR_NO+"' carType='"+data[i].CARTYPE+"' length='"+data[i].LENGTH+"' driver='"+data[i].DRIVER+"' phone='"+data[i].PHONE+"'> "+data[i].DRIVER+"</a></li>");	
			}
		},'json');
		$("#driverList").css({ 
	      	left:$(this).position().left+"px", 
	      	top:$(this).position().top+32+"px" 
		}); 
		$('#driverList').show();
	});
	
	// 选中司机
	 $('#driverList').on('mousedown', '.fromLocationItem', function(e){	
		 $("#car_id").val('');
	  	 $('#driver_name').val('');
	  	 $('#car_no').val('');
	  	 $('#driver_phone').val('');  	 
		 
		 $("#car_id").val($(this).attr('id'));
	  	 $('#driver_name').val($(this).attr('driver'));
	  	 $('#car_no').val($(this).attr('carNo'));
	  	 $('#driver_phone').val($(this).attr('phone'));  	 
	     $('#driverList').hide();   
   });

	// 没选中司机，焦点离开，隐藏列表
	$('#driver_name').on('blur', function(){
		$('#driverList').hide();
	});
	 
	
	 //------------save
    $('#gateInBtn').click(function(e){
        //阻止a 的默认响应行为，不需要跳转
    	var self = this;
        e.preventDefault();

        $(self).attr('disabled', true);
        //异步向后台提交数据
        $.post('/transferOrderMilestone/warehousingConfirm', {departOrderId:$('#depart_id').val()}, function(data){
            if(data.success){
            	$.post('/pickupGateIn/gateIn', {order_id:$('#order_id').val()}, function(data){
            		if(data){
            			$.scojs_message('已入库', $.scojs_message.TYPE_OK);
            			$('#saveBtn').attr('disabled', true);
            		}
            	});
            }else{
                $.scojs_message('入库失败', $.scojs_message.TYPE_ERROR);
                $(self).attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
            $(self).attr('disabled', false);
          });
    });
    
    //按钮控制$('#status').val();
    var order_id =  $('#order_id').val();
    var status = $('#status').val();
    if(order_id == ''){
    	$('#saveBtn').attr('disabled', false);
    }else{
    	if(status == '新建'){
    		$('#saveBtn').attr('disabled', false);
    		$('#gateInBtn').attr('disabled', false);
    	}
    }
    
  
} );