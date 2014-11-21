$(document).ready(function() {
  $('#menu_profile').addClass('active').find('ul').addClass('in');
  	var rolename =$("#role_filter").val();
	var privilege_table = $('#eeda-table').dataTable({
    	"bFilter" : false,	
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
    	"bPaginate": false,
    	"bLengthChange": false,
    	"bInfo": false,
    	"bStateSave":true,
    	//"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/privilege/list?rolename="+rolename,
        "aoColumns": [
			{ "mDataProp": "MODULE_NAME","sWidth":"15%"},
			{ "mDataProp": null,
				"fnRender":function(obj){
					var str = "";
					for(var i=0;i<obj.aData.CHILDRENS.length;i++){
						
					if(obj.aData.CHILDRENS[i].PERMISSION_CODE==null){
						
						str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
		   			 	 
		       		}else{
		       		    str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" checked="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
		       			
		       		}   
				}
				
				return str;
			}}
        ] 
    });

	var permission=[];

	 $('#saveBtn').click(function(e){
	        e.preventDefault();
	        permission.splice(0,permission.length);	        
	        $("input[name='permissionCheck']").each(function(){
	        	if($(this).prop('checked') == true){
	        		permission.push($(this).val());
	        	}
	        });
	        
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