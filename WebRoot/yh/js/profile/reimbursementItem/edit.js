$(document).ready(function() {
    	document.title = '报销费用编辑 | '+document.title;
    	if($("#partyId").val() != null && $("#partyId").val() != "" ){
    		$("#code").attr("disabled",true);
    	}
    	
        $('#reimbursementItemForm').addClass('active').find('ul').addClass('in');
        $('#reimbursementItemForm').validate({
            rules: {
              account_type: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
                    required: true
                  },
            	name: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
                required: true
              },
              type:{//form 中 name为必填
                  required: true
                },

                
            },
            fail: function(element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function(element) {
                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });
        
        
        
      //获取客户列表，自动填充
        $('#account_type').on('keyup click', function(event){
            var me = this;
            var inputStr = $('#account_type').val();
            var typeList =$("#typeList");
            typeList.empty();
            $.get("/reimbursementItem/searchItemType", {input:inputStr}, function(data){
                typeList.empty();
                for(var i = 0; i < data.length; i++)
                	typeList.append("<li><a tabindex='-1' class='fromFinItem' typeId='"+data[i].ID+"' name='"+data[i].NAME+"' >"+data[i].NAME+"</a></li>");
                    typeList.css({ 
	    		    	left:$(me).position().left+"px", 
	    		    	top:$(me).position().top+32+"px" 
    		    	});
                    typeList.show();    
            },'json');
            
        });
        
        $('#typeList').on('click','.fromFinItem', function(e){        
            $('#account_type').val($(this).text());
            $("#typeList").hide();
            var typeId = $(this).attr('typeId');
            $('#typeId').val(typeId);
        });
        
        // 没选中费用类型，焦点离开，隐藏列表
        $('#account_type').on('blur', function(){
            $('#typeList').hide();
        });
        
       
        $('#typeList').on('mousedown', function(){
            return false;//阻止事件回流，不触发 
        });   
        
        
        $("#saveBtn").on('click',function(){
        	$.get('/reimbursementItem/save',$("#reimbursementItemForm").serialize(), function(data){
        		if(data.ID>0){
        			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        			$("#id").val(data.ID);
        			contactUrl("edit?id",data.ID);
        		}else{
        			$.scojs_message('保存失败,请检查下条目是否已存在', $.scojs_message.TYPE_FALSE);
        		}	
        	});
        });
    });