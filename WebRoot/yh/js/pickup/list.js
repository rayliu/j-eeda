 $(document).ready(function() {
		$('#menu_assign').addClass('active').find('ul').addClass('in');
    	$('#dataTables-example').dataTable({
	        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/yh/pickupOrder/pickuplist",
	        "aoColumns": [   
			    {"mDataProp":"DEPART_NO",
	            	"fnRender": function(obj) {
	            			return "<a href='/yh/pickupOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.DEPART_NO+"</a>";
	            		}},
			    {"mDataProp":"CONTACT_PERSON"},
			    {"mDataProp":"PHONE"},
			    {"mDataProp":"CAR_NO"},
			    {"mDataProp":"CAR_TYPE"},     
			    {"mDataProp":"CREATE_STAMP"},     
			    {"mDataProp":"TRANSFER_ORDER_NO"},
	            { 
	                "mDataProp": null, 
	                "sWidth": "8%",                
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-success edit' href='/yh/pickupOrder/edit/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-edit fa-fw'></i>"+
	                                "查看"+
	                            "</a>"+
	                            "<a class='btn btn-danger cancelbutton' href='/yh/pickupOrder/edit/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                                "取消"+
	                            "</a>";
	                }
	            } 
	        ]      
	    });	
    });