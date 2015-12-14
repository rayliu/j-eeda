var queryUser = function(){ 
	$.post('/userRole/userList', function(data){
		var userList =$("#user_filter");
		userList.empty();
		userList.append("<option value='' checked>请选择用户</option>");
		for(var i = 0; i < data.length; i++)
		{
			var user_name = data[i].USER_NAME;
			if(user_name == null){
				user_name = '';
			}
			
			userList.append("<option value='"+user_name+"'>"+user_name+"</option>");
		}
	},'json');
};
$(document).ready(function() {
	
	$('#menu_sys_profile').addClass('active').find('ul').addClass('in');
	
	//datatable, 动态处理
	
    var roletable = $('#eeda-table').dataTable({
    	"bFilter" : false,
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
    	"bStateSave":true,
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/role/listPart",
        "aoColumns": [
            { "mDataProp": null, "sWidth": "7%"	,
            	 "fnRender": function(obj) {
                     return '<input type="checkbox" name="roleCheck" class="unChecked" value="'+obj.aData.ID+'">';
                  }	
            },
           { "mDataProp": "CODE"},
            { "mDataProp": "NAME"}
          
        ] 
    });
    queryUser();
    
	var role=[];
	 $("#eeda-table").on('click','.unChecked',function(){
		 role.splice(0,role.length);
		 $("input[name='roleCheck']").each(function(){
	        	if($(this).prop('checked') == true){
	        		role.push($(this).val());
	        	}
	     });
		 
	  });
    $('#saveBtn').click(function(e){
        e.preventDefault();
        var username = $("#user_filter").val();
        var roles = role.toString();
        
        if(username != ""&&role.length!=0){
        	$.post('/userRole/saveUserRole?name='+username+'&roles='+roles, function(data){
        		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        		$("#user_filter").empty();
        		queryUser();
        		/*$("#user_filter").empty();;*/
        		$("input[name='roleCheck']").each(function(){
                	$(this).prop('checked',false);	
                });
        		
        		role.splice(0,role.length);	
    		},'json');
        }else{
        	$.scojs_message('保存失败，当前没有选择用户或者岗位', $.scojs_message.TYPE_ERROR);
        }
       
    });
    
    var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
			    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
			    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
			    '</div>';
    $('body').append(alerMsg);
   

});	