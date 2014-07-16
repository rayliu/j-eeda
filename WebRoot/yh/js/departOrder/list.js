 $(document).ready(function() {
	$('#menu_assign').addClass('active').find('ul').addClass('in');
var dataTable =$('#dataTables-example').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "bFilter": false, //不需要默认的搜索框
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
            {"mDataProp":"DRIVER"},
            {"mDataProp":"PHONE"},
            {"mDataProp":"CAR_NO"},
            {"mDataProp":"CARTYPE"},     
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"STATUS"},
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
			$('#endTime_filter ,#beginTime_filter ,#sp_filter ,#status_filter ,#orderNo_filter ,#departNo_filter').on( 'keyup click', function () {
				var orderNo = $("#orderNo_filter").val();
				var departNo_filter = $("#departNo_filter").val();
				var status = $("#status_filter").val();
				var sp = $("#sp_filter").val();
				var beginTime = $("#beginTime_filter").val();
				var endTime = $("#endTime_filter").val();
				dataTable.fnSettings().sAjaxSource = "/yh/departOrder/list?orderNo="+orderNo+"&departNo="+departNo_filter+"&status="+status+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime;
				dataTable.fnDraw();
			} );
			
			$('#datetimepicker').datetimepicker({  
			    format: 'yyyy-MM-dd',  
			    language: 'zh-CN'
			}).on('changeDate', function(ev){
			    $('#beginTime_filter').trigger('keyup');
			});
			
			
			$('#datetimepicker2').datetimepicker({  
			    format: 'yyyy-MM-dd',  
			    language: 'zh-CN', 
			    autoclose: true,
			    pickerPosition: "bottom-left"
			}).on('changeDate', function(ev){
			    $('#endTime_filter').trigger('keyup');
			});

	
});