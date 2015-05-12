$(document).ready(function() {
	document.title = user_name+' | '+document.title;
	$('#menu_profile').addClass('active').find('ul').addClass('in');	
	
	//datatable, 动态处理
	var name = $("#user_name").val();
    var datatable = $('#eeda-table').dataTable({
    	"bFilter" : false,
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
    	"bStateSave":true,
    	"iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/userRole/roleList?username="+name,
        "aoColumns": [
            { "mDataProp": null, "sWidth": "7%"	,
            	 "fnRender": function(obj) {
            		 if(obj.aData.ROLE_CODE==null){
            			 return '<input type="checkbox" name="roleCheck" class="unChecked" value="'+obj.aData.ID+'">'; 
            		 }else{
            			 return '<input type="checkbox" checked="true" class="unChecked" name="roleCheck" value="'+obj.aData.ID+'">';
            		 } 
                  }	
            },
            { "mDataProp": "CODE"},
            { "mDataProp": "NAME"},
          
        ]
    });	
    var role=[];
    $("#eeda-table").on('click','.unChecked',function(){
		 role.splice(0,role.length);
		 $("input[name='roleCheck']").each(function(){
	        	if($(this).prop('checked') == true){
	        		role.push($(this).val());
	        	}
	     });
		 console.log(role);
	  });
    $('#saveBtn').click(function(e){
        e.preventDefault();
       
        var username = $("#user_name").val();
        var roles = role.toString();
    	$.post('/userRole/updateRole?name='+username+'&roles='+roles,function(data){
    		$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
    		//$("#saveBtn").attr("disabled",true);
    	},'json');
        
    });
    var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
			    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
			    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
			    '</div>';
    $('body').append(alerMsg); 
    
});	