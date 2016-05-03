$(document).ready(function() {

	$("#orderNo").val('DC'+$.format.date(new Date(), "yyyyMMdd"));

	$("#searchNo").click(function(){
		$('#massage').empty();
		$('#show').css('display','none');
		if($('#orderNo').val().trim() == '' && $('#carNo').val().trim() == ''){
    		return;
    	}
		//加载中
		var $loadingToast = $('#loadingToast');
        if ($loadingToast.css('display') != 'none') {
            return;
        }
        $loadingToast.show();
		$.post('/wx/searchTransferOrder',$("#searchFrom").serialize(), function(data){
			if(data.length!=0){
				$('#massage').append('<label class="weui_cell weui_check_label">'
		                +'<div class="weui_cell_bd weui_cell_primary">'
		                +'<p align="center">运输单号</p>'
		                +'</div>'
		                +'<div class="weui_cell_bd weui_cell_primary">'
		                +'<p align="center">目的地</p>'
		                +'</div>'
		                +'</label>');
				for(var i=0; i<data.length; i++){
					$('#massage').append('<label class="weui_cell weui_check_label" for="'+i+'">'
		                +'<div class="weui_cell_hd" id="box'+i+'">'
		                +'<input type="checkbox" class="weui_check" name="checkbox1" route_to = "'+data[i].ROUTE_TO+'" status="'+data[i].STATUS+'" transfer_id="'+data[i].ID+'" have="'+data[i].DISABLED+'" pickup_id="'+data[i].PICKUP_ID +'" customer_id="'+ data[i].CUSTOMER_ID +'" id="'+i+'">'
		                +'<i class="weui_icon_checked"></i>'
		                +'</div>'
		                +'<div class="weui_cell_bd weui_cell_primary">'
		                +'<p align="center">'+data[i].ORDER_NO+'</p>'
		                +'</div>'
		                +'<div class="weui_cell_bd weui_cell_primary">'
		                +'<p align="center">'+data[i].ROUTE_TO+"("+data[i].AMOUNT+"件)"+'</p>'
		                +'</div>'
		                +'</label>');
					if(data[i].STATUS=='新建')	
						$("#box"+i).html('<i class="weui_icon_cancel"></i>');
					if(data[i].DISABLED>0)	
						$("#box"+i).html('<i class="weui_icon_success_no_circle"></i>');
				}	
			}else{
				$('#show').show();	
			}
			$loadingToast.hide();
		});
	});

	
	//创建按钮控制 
	$('#massage').on('click',function(){
		var ids=0.0;
		$.each($('input[type="checkbox"]'), function(i,val){   
			if($(val).prop("checked")){
				if($(val).attr("have")==0)
					ids+=1;
			}
		});
		if(ids>0){
			$('#createOrder').show();
		}else{
			$('#createOrder').hide();
		}
	});
	
	$('#createOrder').click(function(){
		var trans_pickups = []
		var transferIds = [];
		var customer_ids = '';
		var routeTo = '';
		var check = true;
		$.each($('input[type="checkbox"]'), function(i,val){   
			if($(val).prop("checked")){
				if($(val).attr("have")==0){       //没做发车单的运输单
					trans_pickups.push($(val).attr("transfer_id")+':'+$(val).attr("pickup_id"));
					transferIds.push($(val).attr("transfer_id"));
					
					//客户校验
					if(customer_ids==''){
						customer_ids=$(val).attr('customer_id');
					}else{
						if(customer_ids!=$(val).attr('customer_id')){
							var $toast = $('#error');
					         if ($toast.css('display') != 'none') {
					             return;
					         }
					         $('#text').html('您所勾选的运输单客户不同');
					         
					         $toast.show();
					         setTimeout(function () {
					             $toast.hide();
					         }, 3000);
					         check=false;
						}
					}
					
					//目的地校验
					if(routeTo==''){
						routeTo=$(val).attr('route_to');
					}else{
						if(routeTo!=$(val).attr('route_to')){
							var $toast = $('#error');
					         if ($toast.css('display') != 'none') {
					             return;
					         }
					         $('#text').html('您所勾选的运输单目的地不同')
					         $toast.show();
					         setTimeout(function () {
					             $toast.hide();
					         }, 3000);
					         check=false;
						}
					}
				}
			}
		});
		$('#transferIds').val(transferIds);
		$('#trans_pickups').val(trans_pickups);
		if(check)
		    $('#searchFrom').submit();	
	});
});
