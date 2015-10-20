$(document).ready(function() {
  $('#menu_sys_profile').addClass('active').find('ul').addClass('in');	
	var username =$("#user_filter").val();
  $('#eeda-table').dataTable({
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
        "sAjaxSource": "/userRole/permissionList?username="+username,
        "aoColumns": [
             { "mDataProp": "MODULE_NAME","sWidth":"15%"},
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
	

	
});
   