$(document).ready(function() {
	$('#menu_warehouse').addClass('active').find('ul').addClass('in');
	 $('#example').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/yh/inventory/gateInlist",
			"aoColumns": [
				{ "mDataProp": "USER_NAME" },
				{ "mDataProp": null },
	            { "mDataProp": "PASSWORD_HINT" },
	            { 
	                "mDataProp": null, 
	                "sWidth": "15%",
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-info editbutton' href='/yh/loginUser/edit/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-edit'> </i> "+
	                                "编辑"+
	                            "</a>"+
	                            "<a class='btn btn-danger' href='/yh/loginUser/del/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                                "删除"+
	                            "</a>";
	                }
	            } 
	            ]
	} );
		

});