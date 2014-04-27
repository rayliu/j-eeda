

$(document).ready(function() {
    $('#menu_profile').addClass('active').find('ul').addClass('in');
	//datatable, 动态处理
    $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/product/list",
        "aoColumns": [   
            
            {"mDataProp":"ITEM_NAME"},
            {"mDataProp":"ITEM_NO"},        	
            {"mDataProp":"ITEM_DESC"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success' href='/yh/product/edit/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger' href='/yh/product/delete/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ],      
    });	
} );