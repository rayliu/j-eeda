$(document).ready(function() {
  $('#menu_profile').addClass('active').find('ul').addClass('in');
  	var rolename =$("#role_filter").val();
	var privilege_table = $('#eeda-table').dataTable({
    	"bFilter" : false,
    	"bStateSave":true,
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/privilege/list?rolename="+rolename,
        "aoColumns": [
            { "mDataProp": null, "sWidth": "7%"	,
            	 "fnRender": function(obj) {
            		 if(obj.aData.PERMISSION_CODE==null){
            			 return '<input type="checkbox" name="permissionCheck" class="unChecked" value="'+obj.aData.CODE+'">';
            		 }else{
            			 return '<input type="checkbox" checked="true" class="unChecked" name="permissionCheck" value="'+obj.aData.CODE+'">';
            		 }
                     
                  }	
            },
            { "mDataProp": "MODULE_NAME","sWidth": "20%"},
            { "mDataProp": "NAME"}
        ] 
    });

	var permission=[];
	 $("#eeda-table").on('click','.unChecked',function(){
		permission.splice(0,permission.length);	        
        $("input[name='permissionCheck']").each(function(){
        	if($(this).prop('checked') == true){
        		permission.push($(this).val());
        	}
        });
		console.log(permission);
		
	  });
	
	
	 $('#saveBtn').click(function(e){
	        e.preventDefault();
	        
	        
	        var rolename = $("#role_filter").val();
	        var permissions = permission.toString();
	        
        	$.get('/privilege/update?name='+rolename+'&permissions='+permissions, function(data){
        		//保存成功返回到当前用户的权限页面
        		$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
        		$("#saveBtn").attr("disabled",true);
    		},'json');
        	 
	       
	    });
	    var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
				    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
				    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
				    '</div>';
	    $('body').append(alerMsg); 

	    
});