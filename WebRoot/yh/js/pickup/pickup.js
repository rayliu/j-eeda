
$(document).ready(function() {
		    
	 $('#eeda-table').dataTable({
	        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/yh/delivery/SearchTransfer",
	        "aoColumns": [   
	            
	            {"mDataProp":"ORDER_NO"},
	            {"mDataProp":"STATUS"},
	            {"mDataProp":"CARGO_NATURE"},        	
	            {"mDataProp":"PICKUP_MODE"},
	            {"mDataProp":"ARRIVAL_MODE"},
	            { 
	                "mDataProp": null, 
	                "fnRender": function(obj) {                    
	                    return "<select>"+
	                    		"<option>123</option>"+
	                    		"<option>asda</option>"+
	                    		"</select>";
	                }
	            },   
	            { 
	                "mDataProp": null, 
	                "sWidth": "8%",                
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-info' href='/yh/delivery/creat/"+obj.aData.ID+"'>"+
	                    		"<i class='fa fa-list fa-fw'> </i> "+
	                    		"创建"+
	                    		"</a>";
	                }
	            }                         
	        ]      
	    });	

});
