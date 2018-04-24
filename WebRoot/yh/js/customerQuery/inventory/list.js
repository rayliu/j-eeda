
$(document).ready(function() {
	document.title = '库存查询 | '+document.title;
    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');

    var inventory_dataTable = $('#eeda-table').DataTable({
        "processing": true,
        "searching": false,
        "serverSide": false,
        "scrollX": true,
        //"scrollY": "300px",
        "scrollCollapse": true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        //"ajax": "/customerQuery/inventorySearch",
        "columns": [
            { "data": "WAREHOUSE_NAME"},
            { "data": "ITEM_NO"}, 
            { "data": "AMOUNT"}, 
            { "data": "UNIT"}, 
            { 
        	   "render": function ( data, type, full, meta ) {
        		   return "<a id='edit_detail' warehouse_id="+full.WAREHOUSE_ID+" ITEM_NO="+full.ITEM_NO+" data-target='#itemDetail' data-toggle='modal'>单品明细</a>";
               }
            }
        ]
    });
    
    var itemDetailTable = $('#itemDetail-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/departOrder/getItemDetail",
        "aoColumns": [
            {"mDataProp":"SERIAL_NO"},
            {"mDataProp":"ITEM_NO"},
            {"mDataProp":"DAYS"},
            {"mDataProp":"RECEIVE_UNIT"},
            {"mDataProp":"RECEIVE_ADDRESS"},
            {"mDataProp":"CONTACTS"},
            {"mDataProp":"PHONE"}
        ]  
    });	
    
    
    
    $("#eeda-table").on('click', '#edit_detail', function(e){
    	e.preventDefault();	
    	var warehouse_id = $(this).attr("warehouse_id");
    	var item_no = $(this).attr("item_no");
    	itemDetailTable.fnSettings().sAjaxSource = "/customerQuery/getItemDetail?warehouse_id="+warehouse_id+"&item_no="+item_no;
    	itemDetailTable.fnDraw();
    });


    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    })

   var searchData=function(){
        var warehouse_id=$.trim($("#warehouse_id").val());
        var item_no=$.trim($("#item_no").val());
   	 var flag = false;
     $('#orderForm input,#orderForm select').each(function(){
     	 var textValue = $.trim(this.value);
     	 if(textValue != '' && textValue != null){
     		 flag = true;
     		 return;
     	 } 
     });
     if(!flag){
     	 $.scojs_message('请输入至少一个查询条件', $.scojs_message.TYPE_FALSE);
     	 return false;
     }

        
        var url = "/customerQuery/inventorySearch?warehouse_id="+warehouse_id
             +"&item_no="+item_no;

        inventory_dataTable.ajax.url(url).load();
    };
    

} );