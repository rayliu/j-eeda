
$(document).ready(function() {
	$('#menu_profile').addClass('active').find('ul').addClass('in');
	var datatable=$('#example').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	        "bFilter": false,
            "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "",
			"aoColumns": [
				{ "mDataProp":null},
	            { "mDataProp":null},
	            { "mDataProp":null},
	            { "mDataProp":null},
	            { "mDataProp":null},
	            { "mDataProp":null },
	            { "mDataProp":null },
	            { "mDataProp":null}
	            { 
	                "mDataProp": null, 
	                "sWidth": "11%",
	                "fnRender": function(obj) { 
	                	if(obj.aData.IS_STOP != true){
	                		return "<nobr><% if(shiro.hasPermission('Office.update')){%><a class='btn  btn-primary btn-sm' href='/office/edit/"+obj.aData.ID+"'>"+
		                            "<i class='fa fa-edit'> </i> "+
		                            "编辑"+
			                        "</a> <%}%>"+
			                        "<% if(shiro.hasPermission('Office.delete')){%><a class='btn btn-danger  btn-sm ' href='/office/del/"+obj.aData.ID+"'>"+
			                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                            "停用"+
			                        "</a><%}%></nobr>";
	                	}else{
	                		return "<nobr><% if(shiro.hasPermission('Office.update')){%><a class='btn  btn-primary btn-sm' href='/office/edit/"+obj.aData.ID+"'>"+
		                            "<i class='fa fa-edit'> </i> "+
		                            "编辑"+
			                        "</a> <%}%>"+
			                        "<% if(shiro.hasPermission('Office.delete')){%><a class='btn btn-success  btn-sm' href='/office/del/"+obj.aData.ID+"'>"+
			                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                            "启用"+
			                        "</a><%}%></nobr>";
	                	}
	                    
	                }
	            }
	            ]
	} );
		
});
