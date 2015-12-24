
$(document).ready(function() {

	document.title = order_no + ' | ' + document.title;

    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');
    
    $('#amount').blur(function(){
        $('#total_amount').text($(this).val());
    });

    //------------save
    $('#saveBtn').click(function(e){
        $(this).attr('disabled', true);

        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数据
        if(!$("#orderForm").valid()){
            return;
        }

        var cargo_items_array=damageOrder.buildCargoDetail();
        var charge_items_array=damageOrder.buildChargeDetail();
        var cost_items_array=damageOrder.buildCostDetail();

        var order = {
            id: $('#order_id').val(),
            order_no: $('#order_no').val(),  
            customer_id: $('#customer_id').val(),  
            sp_id: $('#sp_id').val(),
            insurance_id: $('#insurance_id').val(),

            order_type: $('#order_type').val(),  
            biz_order_no: $('#biz_order_no').val(),  
            process_status: $('#process_status').val(),

            accident_type: $('#accident_type').val(), 
            amount: $('#amount').val(),  
            accident_desc: $('#accident_desc').val(),  
            accident_date: $('#accident_date').val(),

            status: $('#status').val(),
            remark: $('#remark').val(), 
            cargo_list: cargo_items_array,
            charge_list: charge_items_array,
            cost_list: cost_items_array
        };

        console.log(order);

        //异步向后台提交数据
        $.post('/damageOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            console.log(order);
            if(order.ID>0){
                $("#order_no").val(order.ORDER_NO);
                $("#creator_name").val(order.CREATOR_NAME);
                $("#create_date").val(order.CREATE_DATE);
                $("#status").val(order.STATUS);
                $("#order_id").val(order.ID);
                contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);

                $('#saveBtn').attr('disabled', false);
                $('#completeBtn').attr('disabled',false);
                damageOrder.reDrawCargoTable(order);
                damageOrder.reDrawChargeTable(order);
                damageOrder.reDrawCostTable(order);         
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
          });
    });

    
    $('#completeBtn').click(function(){
    	$('#completeBtn').attr('disabled',true);
    	$.post('/damageOrder/check',{id:$("#order_id").val()},function(data){
    		var finish = function(){
    			$.post('/damageOrder/complete',{id:$("#order_id").val()},function(data){
    				if(data.ID>0){
    					$.scojs_message('结案完成', $.scojs_message.TYPE_OK);	
    					$("#status").val(data.STATUS);
    					$('#saveBtn').attr('disabled',true);
    					$('#add_cargo').attr('disabled',true);
    			    	$('#add_cost').attr('disabled',true);
    			    	$('#add_charge').attr('disabled',true);
    				}else{
    					$.scojs_message('后台报错', $.scojs_message.TYPE_OK);	
    				}
    			});
    		};
    		if(data.success){
    			finish();
    		}else{
    			if(confirm('存在明细未确认，是否继续结案？')){
    				finish();
    			}else{
    				$('#completeBtn').attr('disabled',false);
    			}
    		}
    	});
    	
    	
    });
    
    
    //按钮控制
    if($("#status").val()==''){
    	$('#completeBtn').attr('disabled',true);
    }else if($("#status").val()=='已结案'){
    	$('#saveBtn').attr('disabled',true);
    	$('#completeBtn').attr('disabled',true);
    	$('#add_cargo').attr('disabled',true);
    	$('#add_cost').attr('disabled',true);
    	$('#add_charge').attr('disabled',true);
    }
    
    

    damageOrder.calcTotalCharge();
    damageOrder.calcTotalCost();
} );