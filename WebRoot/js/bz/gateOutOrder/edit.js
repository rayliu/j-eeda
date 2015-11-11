
$(document).ready(function() {

	document.title = order_no + ' | ' + document.title;

    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));

    //------------save
    $('#saveBtn').click(function(e){
        $(this).attr('disabled', true);

        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数据
        if(!$("#orderForm").valid()){
            return;
        }

        var cargo_items_array = orderController.buildCargoDetail();

        var order = {
            id: $('#order_id').val(),
            order_no: $('#order_no').val(),  
            customer_name: $('#customer_name').val(),
            item_list: cargo_items_array
        };

        console.log(order);

        //异步向后台提交数据
        $.post('/bzGateOutOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            console.log(order);
            if(order.ID>0){
                $("#order_id").val(order.ID);
                $("#order_no").val(order.ORDER_NO);
                $("#creator_name").val(order.CREATOR_NAME);
                $("#create_date").val(order.CREATE_DATE);

                contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);

                $('#saveBtn').attr('disabled', false);

                orderController.reDrawCargoTable(order);

                //加一空行
                $('#add_cargo').click();
                
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
          });
    });


} );