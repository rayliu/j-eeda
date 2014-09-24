$(document).ready(function() {
    $('#menu_profile').addClass('active').find('ul').addClass('in');
	//datatable, 动态处理
    var warehouseTab = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "/yh/warehouse/list",
        "aoColumns": [   
            
            {"mDataProp":"WAREHOUSE_NAME"}, 
            {"mDataProp":"CONTACT_PERSON"},        	
            {"mDataProp":"PHONE"},
            {"mDataProp":"DNAME"},       	
            {"mDataProp":"WAREHOUSE_ADDRESS"},        	
            {"mDataProp":"WAREHOUSE_DESC"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success' href='/yh/warehouse/edit/"+obj.aData.ID+"'>"+
                            "<i class='fa fa-edit fa-fw'></i>"+
                            "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger' href='/yh/warehouse/delete/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ],      
    });	
    
    $('#warehouseName_filter').on( 'keyup click', function () {
    	var warehouseName = $("#warehouseName_filter").val();
    	var warehouseAddress = $("#warehouseAddress_filter").val();
    	warehouseTab.fnSettings().sAjaxSource = "/yh/warehouse/list?warehouseName="+warehouseName+"&warehouseAddress="+warehouseAddress;
    	warehouseTab.fnDraw(); 
    });
    
    $('#warehouseAddress_filter').on( 'keyup click', function () {
    	var warehouseName = $("#warehouseName_filter").val();
    	var warehouseAddress = $("#warehouseAddress_filter").val();
    	warehouseTab.fnSettings().sAjaxSource = "/yh/warehouse/list?warehouseName="+warehouseName+"&warehouseAddress="+warehouseAddress;
    	warehouseTab.fnDraw(); 
    }); 
} );