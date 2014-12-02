 $(document).ready(function() {
		$('#menu_damage').addClass('active').find('ul').addClass('in');
    	
		var insuranceOrder = $('#dataTables-example').dataTable({
            "bFilter": false, //不需要默认的搜索框
	        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	        "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/insuranceOrder/list",
	        "aoColumns": [   
			    {"mDataProp":"ORDER_NO",
	            	"fnRender": function(obj) {
	            			return "<a href='/insuranceOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
	            		}},
			    {"mDataProp":"STATUS"},    
			    {"mDataProp":"CREATE_STAMP"},     
			    {"mDataProp":"TRANSFER_ORDER_NO"},
	            { 
	                "mDataProp": null, 
	                "sWidth": "8%",                
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-danger cancelbutton' code='"+obj.aData.ID+"'>"+
			                        "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                        "取消"+
		                        "</a>";
	                }
	            } 
	        ]      
	    });	

        $("#dataTables-example").on('click', '.cancelbutton', function(e){
    		e.preventDefault();
           //异步向后台提交数据
    	   var id = $(this).attr('code');
    	   $.post('/insuranceOrder/cancel/'+id,function(data){
               //保存成功后，刷新列表
               console.log(data);
               if(data.success){
            	   insuranceOrder.fnDraw();
               }else{
                   alert('取消失败');
               }                   
           },'json');
		});
        
        $('#endTime_filter, #beginTime_filter, #orderNo_filter ,#departNo_filter').on( 'keyup click', function () {
			var orderNo = $("#orderNo_filter").val();
			var departNo_filter = $("#departNo_filter").val();
			var beginTime = $("#beginTime_filter").val();
			var endTime = $("#endTime_filter").val();
			insuranceOrder.fnSettings().sAjaxSource = "/insuranceOrder/list?orderNo="+orderNo+"&departNo="+departNo_filter+"&beginTime="+beginTime+"&endTime="+endTime;
			insuranceOrder.fnDraw();
		} );
		
		$('#datetimepicker').datetimepicker({  
		    format: 'yyyy-MM-dd',  
		    language: 'zh-CN'
		}).on('changeDate', function(ev){
	        $(".bootstrap-datetimepicker-widget").hide();
		    $('#beginTime_filter').trigger('keyup');
		});		
		
		$('#datetimepicker2').datetimepicker({  
		    format: 'yyyy-MM-dd',  
		    language: 'zh-CN', 
		    autoclose: true,
		    pickerPosition: "bottom-left"
		}).on('changeDate', function(ev){
	        $(".bootstrap-datetimepicker-widget").hide();
		    $('#endTime_filter').trigger('keyup');
		});
    });