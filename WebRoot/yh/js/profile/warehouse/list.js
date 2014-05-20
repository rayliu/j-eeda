

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
        "sAjaxSource": "/yh/warehouse/list",
        "aoColumns": [   
            
            {"mDataProp":"WAREHOUSE_NAME"}, 
            {"mDataProp":"WAREHOUSE_ADDRESS"},        	
            {"mDataProp":"WAREHOUSE_DESC"},
            {"mDataProp":"CONTACT_PERSON"},        	
            {"mDataProp":"PHONE"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success picture' target='_Blank' href='"+obj.aData.PATH+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "查看图片"+
                            "</a>"+
                            "<a class='btn btn-success' href='/yh/warehouse/edit/"+obj.aData.ID+"'>"+
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
    
    /*$("#eeda-table").on('click', '.picture', function(e){
		  e.preventDefault();
		  //异步向后台提交数据
		  var picture = $(this).attr('picture');
		  //alert(picture);
		  $.post("/yh/warehouse/showPicture?picturePath="+picture,function(data){
             
         },'json');
		});*/
} );