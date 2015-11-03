
$(document).ready(function() {

	document.title = order_no + ' | ' + document.title;

    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');

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

            order_type: $('#order_type').val(),  
            biz_order_no: $('#biz_order_no').val(),  
            process_status: $('#process_status').val(),

            accident_type: $('#accident_type').val(),  
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

                contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);

                $('#saveBtn').attr('disabled', false);

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

    });


} );