
$(document).ready(function() {
    $('#menu_status').addClass('active').find('ul').addClass('in');
    var dataTable =$('#eeda_yh').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "bFilter": false, //不需要默认的搜索框
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/departOrder/transferMilestone",
        "aoColumns": [   
          
            {"mDataProp":"ORDER_NO"},
            {"mDataProp":"STATUS"},
            {"mDataProp":"LOCATION"},
            {"mDataProp":"USERNAMES"},     
            {"mDataProp":"CREATE_STAMP"}
          
        ]      
    });
} );