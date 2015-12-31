

var buildButtonUI = function(module, order_dto) {
    $('#button-bar').empty();
    for (var i = 0; i < module.ACTION_LIST.length; i++) {
        var buttonObj = module.ACTION_LIST[i];
        var is_show = false;
        if(buttonObj.BTN_VISIBLE_CONDITION){
            var btn_v_conditons = JSON.parse(buttonObj.BTN_VISIBLE_CONDITION);
            for (var j = 0; j < btn_v_conditons.length; j++) {
                fields = JSON.parse(btn_v_conditons[j]);
                for (var k = 0; k < fields.length; k++) {
                    var field = fields[k];
                    var field_name = field.key.split(',')[1].split(':')[1];
                    var operator = field.operator;
                    var value = field.value;

                    var order_field_value = getOrderFieldValue(field_name, order_dto);
                    if('=' == operator){
                        if(order_field_value == value){
                            is_show = true;
                            break;
                        }
                    }else if('!=' == operator){
                        if(order_field_value != value){
                            is_show = true;
                            break;
                        }
                    }else if('包含' == operator){
                        var valueArr = value.split(',');
                        for (var l = 0; l < valueArr.length; l++) {
                            var temp_value = valueArr[l];
                            if(order_field_value == temp_value){
                                is_show = true;
                                break;
                            }
                        };
                    }else if('不包含' == operator){

                    }
                };

                
            };
        }

        if(is_show){
            var button_html = template('button_template', {
                id: buttonObj.ID,
                label: buttonObj.ACTION_NAME
            });
            $('#button-bar').append(button_html);
        }
    }
};

var bindBtnClick = function() {
    $('button.order_level').on('click', function(e) {
        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();

        var btnClass = $(this).attr('class');
        var btn = $(this);
        btn.attr('disabled', true);

        //提交前，校验数据
        // if(!$("#orderForm").valid()){
        //     return;
        // }

        btn.attr('disabled', false);

        var order_dto = buildOrderDto();
        order_dto.action = btn.text();

        console.log('save OrderData....');
        console.log(order_dto);

        //异步向后台提交数据
        $.post('/m_save', {
            params: JSON.stringify(order_dto)
        }, function(data) {
            var order = data;
            console.log(order);
            if (order.ID > 0) {
                $('#order_id').val(order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);

                eeda.urlAfterSave($("#module_id").val(), order.ID);
                //重新取一次数据渲染页面
                var structure_json_str = $('#module_structure').val();
                var structure_json = JSON.parse(structure_json_str);
                structure_json.id = order.ID;
                fillOrderData(structure_json);

                $('#saveBtn').attr('disabled', false);
            } else {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        }, 'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
        });
    });
};

var getOrderFieldValue = function (field_name, order_dto){
    var fiedl_value ='null';
    if(order_dto){
        for (var j = 0; j < order_dto.FIELDS_LIST.length; j++) {
            var order = order_dto.FIELDS_LIST[j];
            for(key in order){
                if(key == field_name){
                    fiedl_value = order[key];
                    break;
                }
            }
            if(fiedl_value.length>0)
                break;
        }
    }
    return fiedl_value;
};