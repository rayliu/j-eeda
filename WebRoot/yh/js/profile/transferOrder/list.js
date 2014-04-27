
$(document).ready(function() {

    $('#menu_transfer').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/transferOrder/list",
        "aoColumns": [   
            
            {"mDataProp":"ORDER_NO"},
            {"mDataProp":"STATUS"},
            {"mDataProp":"CARGO_NATURE"},        	
            {"mDataProp":"PICKUP_MODE"},
            {"mDataProp":"ARRIVAL_MODE"},
            {"mDataProp":"REMARK"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success' href='/yh/transferOrder/edit/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger' href='/yh/transferOrder/delete/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ]      
    });	
} );