
$(document).ready(function() {
	
	document.title = '配送排车单查询 | '+document.title;
	$('#menu_deliver').addClass('active').find('ul').addClass('in');
	
	var pickupOrder = $('#dataTables-example').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bAutoWidth":false,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/deliveryPlanOrder/findAllDeliveryPlanOrder",
        "aoColumns": [   
		    {"mDataProp":null, "sWidth":"100px",
            	"fnRender": function(obj) {
            			return "<a href='/deliveryPlanOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		}},
            {"mDataProp":"OFFICE_NAME", "sWidth":"100px"},
		    {"mDataProp":"DELIVER_NO", "sWidth":"100px"},
            {"mDataProp":"CAR_NO", "sWidth":"80px"},	 
		    {"mDataProp":"DRIVER", "sWidth":"70px"},
		    {"mDataProp":"PHONE", "sWidth":"100px"},
		    {"mDataProp":"CREATE_STAMP", "sWidth":"100px",
		    	"fnRender":function(obj){
    				var create_stamp=obj.aData.CREATE_STAMP;
    				var str=create_stamp.substr(0,10);
    				return str;
    			}}, 
		    {"mDataProp":"VOLUME", "sWidth":"70px"},
		    {"mDataProp":"WEIGHT", "sWidth":"70px"},
		    {"mDataProp":"USER_NAME", "sWidth":"70px"},
		    {"mDataProp":"REMARK","sWidth":"150px"}
        ]      
    });	
	
	//获取所有的网点
    $.post('/transferOrder/searchPartOffice',function(data){
   	 if(data.length > 0){
   		 var officeSelect = $("#office_id");
   		 officeSelect.empty();
   		 officeSelect.append("<option value=''></option>");
   		 for(var i=0; i<data.length; i++){
   			 officeSelect.append("<option value='"+data[i].id+"'>"+data[i].OFFICE_NAME+"</option>");					 
   		 }
   	 }
    },'json');
	
    var findLikeAlldeliveryPlans = function(){
    	var order_no = $("#order_no").val();
      	var delivery_no = $("#delivery_no").val();
    	var office_id = $("#office_id").val();
      	var car_no = $("#car_no").val();
      	var turnout_time = $("#turnout_time").val();   
      	var return_time = $("#return_time").val();
      	pickupOrder.fnSettings().sAjaxSource = "/deliveryPlanOrder/findAllDeliveryPlanOrder?order_no="+order_no+"&delivery_no="+delivery_no+"&office_id="+office_id+"&car_no="+car_no+"&turnout_time="+turnout_time+"&return_time="+return_time;
      	pickupOrder.fnDraw();
    };
    
    
	//条件搜索>>,
    $("#order_no,#delivery_no,#car_no,#turnout_time,#return_time").on( 'keyup click', function () { 	
    	findLikeAlldeliveryPlans();
    });
	
    $(" #office_id").on('change',function(){
    	findLikeAlldeliveryPlans();
    });
    
    $('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN'
	}).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
	    $('#beginTime_filter').trigger('keyup');
	    $("#turnout_time").click();
	});		
	
	$('#datetimepicker2').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN', 
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
	    $('#endTime_filter').trigger('keyup');
	    $("#return_time").click();
	});
});