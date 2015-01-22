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
        "sAjaxSource": "/warehouse/list",
        "aoColumns": [   
            
            {"mDataProp":"WAREHOUSE_NAME"}, 
            {"mDataProp":"NOTIFY_NAME"},        	
            {"mDataProp":"NOTIFY_MOBILE"},
            {"mDataProp":"DNAME"},       	
            {"mDataProp":"WAREHOUSE_ADDRESS"},        	
            {"mDataProp":"WAREHOUSE_DESC"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",
                "bVisible":(Warehouser.isUpdate || Warehouser.isDel),
                "fnRender": function(obj) {  
                	var str="";
                	if(Warehouser.isUpdate){
                		str += "<a class='btn btn-info' href='/warehouse/edit/"+obj.aData.ID+"'>"+
	                        "<i class='fa fa-edit fa-fw'></i>"+
	                        "编辑"+
	                        "</a>";
                	}
                	if(Warehouser.isDel){
                		if(obj.aData.STATUS != "inactive"){
                    		str += "<a class='btn btn-danger' href='/warehouse/delete/"+obj.aData.ID+"'>"+
    	                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
    	                            "停用"+
    	                        "</a>";
                    	}else{
                    		str += "<a class='btn btn-success' href='/warehouse/delete/"+obj.aData.ID+"'>"+
    	                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
    	                            "启用"+
    	                        "</a>";
    	                }
                	}
                	return str;
	                    
	             }
	          }                         
	      ],      
    });	
    
    $('#warehouseName_filter').on( 'keyup click', function () {
    	var warehouseName = $("#warehouseName_filter").val();
    	var warehouseAddress = $("#warehouseAddress_filter").val();
    	warehouseTab.fnSettings().sAjaxSource = "/warehouse/list?warehouseName="+warehouseName+"&warehouseAddress="+warehouseAddress;
    	warehouseTab.fnDraw(); 
    });
    
    $('#warehouseAddress_filter').on( 'keyup click', function () {
    	var warehouseName = $("#warehouseName_filter").val();
    	var warehouseAddress = $("#warehouseAddress_filter").val();
    	warehouseTab.fnSettings().sAjaxSource = "/warehouse/list?warehouseName="+warehouseName+"&warehouseAddress="+warehouseAddress;
    	warehouseTab.fnDraw(); 
    }); 
} );