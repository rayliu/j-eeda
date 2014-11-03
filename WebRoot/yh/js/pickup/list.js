 $(document).ready(function() {
		$('#menu_assign').addClass('active').find('ul').addClass('in');
    	
		var pickupOrder = $('#dataTables-example').dataTable({
            "bFilter": false, //不需要默认的搜索框
            "bSort": false, 
	        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	        "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/pickupOrder/pickuplist",
	        "aoColumns": [   
			    {"mDataProp":"DEPART_NO", "sWidth":"70px",
	            	"fnRender": function(obj) {
	            			return "<a href='/pickupOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.DEPART_NO+"</a>";
	            		}},
	            {"mDataProp":"OFFICE_NAME", "sWidth":"100px"},
			    {"mDataProp":"STATUS", "sWidth":"80px"},
			    {"mDataProp":"PICKUP_MODE", "sWidth":"120px",
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
	            {"mDataProp":"CAR_NO", "sWidth":"100px"},	 
			    {"mDataProp":"CONTACT_PERSON", "sWidth":"70px"},
			    {"mDataProp":"PHONE", "sWidth":"50px"},
			    {"mDataProp":"CARTYPE", "sWidth":"80px"},     
			    {"mDataProp":"CREATE_STAMP", "sWidth":"100px",
			    	"fnRender":function(obj){
        				var create_stamp=obj.aData.CREATE_STAMP;
        				var str=create_stamp.substr(0,10);
        				return str;
        			}}, 
			    {"mDataProp":"VOLUME", "sWidth":"100px"},
			    {"mDataProp":"WEIGHT", "sWidth":"100px"},
			    {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"70px"},
			    {"mDataProp":"USER_NAME", "sWidth":"70px"},
			    {"mDataProp":"REMARK"}
	        ]      
	    });	

        $("#dataTables-example").on('click', '.cancelbutton', function(e){
    		e.preventDefault();
           //异步向后台提交数据
    	   var id = $(this).attr('code');
    	   $.post('/pickupOrder/cancel/'+id,function(data){
               //保存成功后，刷新列表
               console.log(data);
               if(data.success){
            	   pickupOrder.fnDraw();
               }else{
                   alert('取消失败');
               }                   
           },'json');
		});
        
        $('#endTime_filter, #beginTime_filter, #orderNo_filter ,#departNo_filter,#carNo_filter').on( 'keyup click', function () {
        	var carNo = $("#carNo_filter").val();
        	var take = $("#take_filter").val();
        	var status = $("#status_filter").val();
        	var office =$("#officeSelect").val();
        	
        	var orderNo = $("#orderNo_filter").val();
			var departNo_filter = $("#departNo_filter").val();
			var beginTime = $("#beginTime_filter").val();
			var endTime = $("#endTime_filter").val();
			pickupOrder.fnSettings().sAjaxSource = "/pickupOrder/pickuplist?orderNo="+orderNo
												+"&departNo="+departNo_filter
												+"&beginTime="+beginTime
												+"&endTime="+endTime+"&carNo="+carNo
												+"&take="+take+"&status="+status
												+"&office="+office;
			pickupOrder.fnDraw();
		} );
	  $("#status_filter, #officeSelect, #take_filter").on('change',function(){
		    var carNo = $("#carNo_filter").val();
	      	var take = $("#take_filter").val();
	      	var status = $("#status_filter").val();
	      	var office =$("#officeSelect").val();
	      	
	      	var orderNo = $("#orderNo_filter").val();
			var departNo_filter = $("#departNo_filter").val();
			var beginTime = $("#beginTime_filter").val();
			var endTime = $("#endTime_filter").val();
			pickupOrder.fnSettings().sAjaxSource = "/pickupOrder/pickuplist?orderNo="+orderNo
												+"&departNo="+departNo_filter
												+"&beginTime="+beginTime
												+"&endTime="+endTime+"&carNo="+carNo
												+"&take="+take+"&status="+status
												+"&office="+office;
			pickupOrder.fnDraw();
      });
		//获取所有的网点
        $.post('/transferOrder/searchAllOffice',function(data){
       	 if(data.length > 0){
       		 var officeSelect = $("#officeSelect");
       		 officeSelect.empty();
       		 var hideOfficeId = $("#hideOfficeId").val();
       		 for(var i=0; i<data.length; i++){
       			 if(i == 0){
       				 officeSelect.append("<option ></option>");
       			 }else{
       				 if(data[i].ID == hideOfficeId){
       					 officeSelect.append("<option value='"+data[i].OFFICE_NAME+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");
       				 }else{
       					 officeSelect.append("<option value='"+data[i].OFFICE_NAME+"'>"+data[i].OFFICE_NAME+"</option>");					 
       				 }
       			 }
       		 }
       		
       	 }
        },'json');
        
      
        
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