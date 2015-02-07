$(document).ready(function() {
    $('#menu_report').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var statusTable = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/transferOrder/list",
        "aoColumns": [   
            {"mDataProp":"STATUS", "sWidth":"100px"},
            {"mDataProp":"CNAME", "sWidth":"100px"},
            {"mDataProp":"ROUTE_FROM", "sWidth":"100px"},
            {"mDataProp":"ROUTE_TO", "sWidth":"100px"},       	
            {"mDataProp":"AMOUNT", "sWidth":"100px"},
            {"mDataProp":"PIECES", "sWidth":"100px"},
            {"mDataProp":"VOLUME", "sWidth":"100px"},
            {"mDataProp":"WEIGHT", "sWidth":"100px"},
            {"mDataProp":"PLANNING_TIME", "sWidth":"100px"},
            {"mDataProp":"ARRIVAL_TIME", "sWidth":"100px"},
            {"mDataProp":"ADDRESS", "sWidth":"100px",},
            {"mDataProp":"SPNAME", "sWidth":"100px"},
            {"mDataProp":"CUSTOMER_ORDER_NO" ,"sWidth":"100px"},
            {"mDataProp":"ONAME","sWidth":"100px"}
        ]  
    });	
    
});
    