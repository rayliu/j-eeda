$(document).ready(function() {
	 $("#del").click(function(){
    	alert("waerwerwee");
    });
	 $("#query").click(function(){
	    	alert("123");
	    });
	 $('#example').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/yh/loginUser/listUser",
			"aoColumns": [
				{ "mDataProp": "USER_NAME" },
	            { "mDataProp": "PASSWORD" },
	            { "mDataProp": "PASSWORD_HINT" },
	            { "mDataProp": null, 
	                "sWidth": "15%",
	                "fnRender": function(obj) {  
	                	
	                    return "<select>"+ 
				                    "<option>"+obj.aData.ID+"</option>" + 
				                    "<option>Saab</option>"+
				                 "</select>";
	                } },
	            { "mDataProp": null, 
	                "sWidth": "15%",
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-info' href='/yh/delivery/creat/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-list fa-fw'> </i> "+
	                                "创建"+
	                            "</a>";
	                }
	            } 
	            ]
	} );
});