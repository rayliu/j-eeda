$(document).ready(function() {
  $('#menu_profile').addClass('active').find('ul').addClass('in');	
	var username =$("#user_filter").val();
  $('#eeda-table').dataTable({
    	"bFilter" : false,
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/userRole/permissionList?username="+username,
        "aoColumns": [
            { "mDataProp": null, "sWidth": "7%"	,
            	 "fnRender": function(obj) {
            		 if(obj.aData.PERMISSION_CODE==null){
            			 return '<input type="checkbox" disabled="true" name="userPermission" value="'+obj.aData.CODE+'">';
            		 }else{
            			 return '<input type="checkbox" disabled="true" checked="checked" name="userPermission" value="'+obj.aData.CODE+'">';
            		 }
                     
                  }	
            },
            { "mDataProp": "MODULE_NAME","sWidth": "20%"},
            { "mDataProp": "NAME"}
        ] 
    });
	

	
});
   