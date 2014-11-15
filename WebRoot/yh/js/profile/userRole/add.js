$(document).ready(function() {
	
    $('#menu_finance').addClass('active').find('ul').addClass('in');
	
	//datatable, 动态处理
	
    var roletable = $('#eeda-table').dataTable({
    	"bFilter" : false,
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/role/list",
        "aoColumns": [
            { "mDataProp": null, "sWidth": "7%"	,
            	 "fnRender": function(obj) {
                     return '<input type="checkbox" name="roleCheck" value="'+obj.aData.ID+'">';
                  }	
            },
           { "mDataProp": "CODE"},
            { "mDataProp": "NAME"}
          
        ] 
    });
 
    $('#user_filter').on('click', function(){
		/*var inputStr = $('#user_filter').val();*/
		$.get('/userRole/userList', function(data){
			var userList =$("#userList");
			userList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var user_name = data[i].USER_NAME;
				if(user_name == null){
					user_name = '';
				}
				
				userList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].ID+"'user_name='"+data[i].USER_NAME+"' >"+user_name+" "+"</a></li>");
			}
		},'json');

		$("#userList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#userList').show();
    });
    
    // 没选中供应商，焦点离开，隐藏列表
	$('#user_filter').on('blur', function(){
 		$('#userList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#userList').on('blur', function(){
 		$('#userList').hide();
 	});

	$('#userList').on('mousedown', function(){
		return false;
	});

	
	$('#userList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#user_filter').val(message.substring(0, message.indexOf(" ")));
		$('#user_id').val($(this).attr('partyId'));
		var userName = $("#user_name");
		userName.empty();
        $('#userList').hide();
    });
    
	var role=[];
    $('#saveBtn').click(function(e){
        e.preventDefault();
        role.splice(0,role.length);
        $("input[name='roleCheck']").each(function(){
        	if($(this).prop('checked') == true){
        		role.push($(this).val());
        	}
        });
        var username = $("#user_filter").val();
        var roles = role.toString();
        if(username != ""&&role.length!=0){
        	$.post('/userRole/saveUserRole?name='+username+'&roles='+roles, function(data){
        		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        		
        		$("#user_filter").val("");
        		$("input[name='roleCheck']").each(function(){
                	$(this).prop('checked',false);	
                });
        		role.splice(0,role.length);	
    		},'json');
        }
       
    });
    
    
    
    
    var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
			    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
			    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
			    '</div>';
    $('body').append(alerMsg);
    

});	