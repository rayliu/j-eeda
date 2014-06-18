 $(document).ready(function() {
	$('#menu_assign').addClass('active').find('ul').addClass('in');
var dataTable =$('#dataTables-example').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/departOrder/list",
        "aoColumns": [   
           
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='' href='/yh/departOrder/add/"+obj.aData.ID+"'>"+
                                obj.aData.DEPART_NO +
                            "</a>";
                }
            } ,
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
                    return "<a class='btn btn-danger cancelbutton' href=' /yh/departOrder/cancel/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "取消"+
                            "</a>";
                }
            } 
        ]      
    });
	
});