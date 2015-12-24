var queryRole = function(){
	document.title = '岗位权限查询 | '+document.title;
	$.get('/privilege/roleList', function(data){
		var roleList =$("#role_filter");
		roleList.empty();
		roleList.append("<option value='' checked>请选择岗位</option>");
		for(var i = 0; i < data.length; i++)
		{
			var name = data[i].NAME;
			if(name == null){
				name = '';
			}
			
			roleList.append("<option value='"+data[i].NAME+"'>"+name+"</option>");
		}
	},'json');
};


$(document).ready(function() {
  $('#menu_sys_profile').addClass('active').find('ul').addClass('in');
  queryRole();
	var privilege_table = $('#eeda-table').dataTable({
    	"bFilter" : false,
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
    	"bPaginate": false,
    	"bLengthChange": false,
    	"bInfo": false,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/privilege/list",
        "aoColumns": [
            { "mDataProp": "MODULE_NAME","sWidth":"15%",
                "fnRender":function(obj){
                  return '<h4>' + obj.aData.MODULE_NAME + '</h4>';
                }
            },
            { "mDataProp": null,
            	"fnRender":function(obj){
            		var str = "";
            		for(var i=0;i<obj.aData.CHILDRENS.length;i++){
            			
            			if(obj.aData.CHILDRENS[i].PERMISSION_CODE==null){
            				str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';               			 	 
	               		}else{
	               			if(obj.aData.IS_AUTHORIZE != null && obj.aData.IS_AUTHORIZE != 0){
	               				str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" checked="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
	               			}else{
	               				str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
	               			}
	               		    
	               			
	               		}   
            		}
            		
            		return str;
            	}}
        ] 
    });
	
	$('#role_filter').on('change',function(e){
		
		var rolename = $('#role_filter').val();
		
		privilege_table.fnSettings().sAjaxSource = "/privilege/list?rolename="+rolename;
		privilege_table.fnDraw(); 

	});
    $('#roleForm').validate({
        rules: {
        	rolename: {
               required: true
        	}
       	 },  	
		 messages:{
			 rolename:{
				 required:"岗位不能为空"
			 }
       },
       highlight: function(element) {
      	$(element).parent().removeClass('has-success').addClass('has-error');
      	
      },
      success: function(element) {
      	//element.parent().removeClass('has-error').addClass('has-success');
      }
        
 });	

	
});
   