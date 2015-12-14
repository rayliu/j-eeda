$(document).ready(function() {
	document.title = '仓库查询 | '+document.title;
    $('#menu_profile').addClass('active').find('ul').addClass('in');
	//datatable, 动态处理
    var warehouseTab = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "/warehouse/list",
        "aoColumns": [   
            
            {"mDataProp":"WAREHOUSE_NAME",
            	"fnRender":function(obj){
            		if(Warehouser.isUpdate){
            			return "<a  href='/warehouse/edit/"+obj.aData.ID+"' target='_blank'>" + obj.aData.WAREHOUSE_NAME + "</a>";
            		}else{
            			return obj.aData.WAREHOUSE_NAME;
            		}
            		
            	}}, 
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
                	var str="<nobr>";
                	if(Warehouser.isUpdate){
                		str += "<a class='btn  btn-primary btn-sm' href='/warehouse/edit/"+obj.aData.ID+"' target='_blank'>"+
	                        "<i class='fa fa-edit fa-fw'></i>"+
	                        "编辑"+
	                        "</a> ";
                	}
                	if(Warehouser.isDel){
                		if(obj.aData.STATUS != "inactive"){
                    		str += "<a class='btn btn-danger  btn-sm' href='/warehouse/delete/"+obj.aData.ID+"'>"+
    	                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
    	                            "停用"+
    	                        "</a>";
                    	}else{
                    		str += "<a class='btn btn-success  btn-sm' href='/warehouse/delete/"+obj.aData.ID+"'>"+
    	                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
    	                            "启用"+
    	                        "</a>";
    	                }
                	}
                	str+="</nobr>";
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