
$(document).ready(function() {

    $('#menu_transfer').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var dataTable =$('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/delivery/deliveryList",
        "aoColumns": [   
            
            {"mDataProp":"ORDER_NO"},
            {"mDataProp":"TRANSFER_ORDER_ID"},
            {"mDataProp":"CUSTOMER_ID"},        	
            {"mDataProp":"SP_ID"},
            {"mDataProp":"NOTIFY_PARTY_ID"},
            {"mDataProp":"STATUS"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success ' href='/yh/transferOrder/edit/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "查看"+
                            "</a>"+
                            "<a class='btn btn-danger cancelbutton' code='"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "取消"+
                            "</a>";
                }
            }                         
        ]      
    });	
    
	 $("#eeda-table").on('click', '.cancelbutton', function(e){
		  e.preventDefault();
         //异步向后台提交数据
		 var id = $(this).attr('code');
		$.post('/yh/delivery/cancel/'+id,function(data){
                 //保存成功后，刷新列表
                 console.log(data);
                 if(data.success){
                	 dataTable.fnDraw();
                 }else{
                     alert('已取消');
                 }
                 
             },'json');
		  });
} );