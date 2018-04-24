
$(document).ready(function() {
	document.title = '客户订单状态查询 | '+document.title;

    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');
    
    $("input[name=search_type]").change(function(){
        if (this.value == 'order_no') {
            $('#order_no_div').show();
            $('#serial_no_div').hide();
            
            $('#order_no_table_div').show();
            $('#serial_no_table_div').hide();
        }else{
            $('#order_no_div').hide();
            $('#serial_no_div').show();

            $('#order_no_table_div').hide();
            $('#serial_no_table_div').show();
        }
    });
    
	 
    var dataTable = $('#orderStatus_table').DataTable({
        "processing": true,
        "searching": false,
        "serverSide": false,
        "responsive": true,
        "scrollX": true,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "scrollCollapse": false,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        //"ajax": "/customerQuery/orderStatusSearch",
        "columns": [
            { "data": "CUSTOMER_ORDER_NO"},
            { "data": "ROUTE_TO"},
            { "data": "PLANNING_TIME"},
            { "data": "DEPARTURE_TIME"}, 
            { "data": "AMOUNT"}, 
            { "data": "TRANSFER_STATUS"}, 
            { "data": "SIGNIN_TIME"}, 
            { "data": "RETURN_STATUS"}, 
            { "data": "DELIVERY_ADDRESS"}
        ]
    });
    
    
    var itemDetailTable = $('#itemDetail-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/customerQuery/orderSerialNoSearch",
        "aoColumns": [
            {"mDataProp":"ORDER_NO"},
            {"mDataProp":"ITEM_NO"},
            {"mDataProp":"SERIAL_NO"},
            {"mDataProp":"AMOUNT"}        
        ]  
    });	
    
    
    
    $("#eeda-table").on('click', '#edit_detail', function(e){
    	e.preventDefault();	
    	var depart_id = $(this).attr("depart_id");
    	itemDetailTable.fnSettings().sAjaxSource = "/departOrder/getItemDetail?depart_id="+depart_id;
    	itemDetailTable.fnDraw();
    });

    var serial_dataTable = $('#serial_no_table').DataTable({
        "processing": true,
        "searching": false,
        "serverSide": false,
        "scrollX": true,
        "scrollCollapse": true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        //"ajax": "/customerQuery/orderSerialNoSearch",
        "columns": [
            { "data": "SERIAL_NO"},
            { "data": "ITEM_NO"},
            { "data": "AMOUNT"},
            { "data": "ROUTE_TO"}, 
            { "data": "DELIVERY_ADDRESS"}, 
            { "data": "NOTIFY_PARTY_NAME"}, 
            { "data": "DELIVERY_TIME"}, 
            { "data": "DELIVERY_STATUS"}, 
            { "data": "RETURN_STATUS" }, 
            { "data": "RETURN_UNIT" }, 
            { "data": "RECEIVE_ADDRESS" }, 
            { "data": "RECEIVE_ADDRESS"}
        ]
    });
    
    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
    	 
        var search_type = $("input[name=search_type]:checked").val();
        if(search_type=='order_no'){
        	searchData(); 
        }else{
        	searchSerailData(); 
        }
    })

   var searchData=function(){
        var customer_order_no = $.trim($("#customer_order_no").val());
        var customer_id=$.trim($("#customer_id").val());
        var route_to= $.trim($("#route_to").val());
        
        var search_type = $.trim($("#search_type").val());
       
        var beginTime = $.trim($("#plan_time_begin_time").val());
        var endTime = $.trim($("#plan_time_end_time").val());
        
        var flag = false;
        $('#orderForm input,#orderForm select').each(function(){
        	 var textValue = $.trim(this.value);
        	 if(textValue != '' && textValue != null){
        		 if(this.name=="search_type"){
       				return true;
       		 }
        		 flag = true;
        		 return;
        	 } 
        });
        if(!flag){
        	 $.scojs_message('请输入至少一个查询条件', $.scojs_message.TYPE_FALSE);
        	 return false;
        }
        
        var url = "/customerQuery/orderStatusSearch?search_type="+search_type
             +"&customer_order_no="+customer_order_no
             +"&customer_id="+customer_id
             +"&route_to="+route_to
             +"&begin_time="+beginTime
             +"&end_time="+endTime;

        dataTable.ajax.url(url).load();
    };
    
    var searchSerailData=function(){
        var customer_id=$.trim($("#customer_id").val());
        var serial_no=$.trim($("#serial_no").val());
        
        var flag = false;
        $('#orderForm input,#orderForm select').each(function(){
        	 var textValue =$.trim(this.value);
        	 if(textValue != '' && textValue != null){
        		 if(this.name=="search_type"){
          				return true;
          		 }
        		 
        		 flag = true;
        		 return;
        	 } 
        });
        if(!flag){
        	 $.scojs_message('请输入至少一个查询条件', $.scojs_message.TYPE_FALSE);
        	 return false;
        }
        
        var url = "/customerQuery/orderSerialNoSearch?serial_no="+serial_no
             +"&customer_id="+customer_id;

        serial_dataTable.ajax.url(url).load();
    };
    

} );