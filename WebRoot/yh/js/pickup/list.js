 $(document).ready(function() {
		$('#menu_assign').addClass('active').find('ul').addClass('in');
    	
		var pickupOrder = $('#dataTables-example').dataTable({
            "bFilter": false, //不需要默认的搜索框
	        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	        "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/yh/pickupOrder/pickuplist",
	        "aoColumns": [   
			    {"mDataProp":"DEPART_NO",
	            	"fnRender": function(obj) {
	            			return "<a href='/yh/pickupOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.DEPART_NO+"</a>";
	            		}},
			    {"mDataProp":"STATUS"},
			    {"mDataProp":"PICKUP_MODE",
	            	"fnRender": function(obj) {
	            		if(obj.aData.PICKUP_MODE == "routeSP"){
	            			return "干线供应商自提";
	            		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
	            			return "外包供应商提货";
	            		}else if(obj.aData.PICKUP_MODE == "own"){
	            			return "源鸿自提";
	            		}else{
	            			return "";
	            		}}},
			    {"mDataProp":"DRIVER"},
			    {"mDataProp":"PHONE"},
			    {"mDataProp":"CAR_NO"},
			    {"mDataProp":"CARTYPE"},     
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
    	   $.post('/yh/pickupOrder/cancel/'+id,function(data){
               //保存成功后，刷新列表
               console.log(data);
               if(data.success){
            	   pickupOrder.fnDraw();
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
			pickupOrder.fnSettings().sAjaxSource = "/yh/pickupOrder/pickuplist?orderNo="+orderNo+"&departNo="+departNo_filter+"&beginTime="+beginTime+"&endTime="+endTime;
			pickupOrder.fnDraw();
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