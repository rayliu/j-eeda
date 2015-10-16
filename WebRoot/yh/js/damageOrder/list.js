
$(document).ready(function() {
	document.title = '货损单查询 | '+document.title;

    $('#menu_returnTransfer').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));
    
	  //datatable, 动态处理
    var dataTable = $('#eeda-table').dataTable({
        "processing": true,
        "searching": false,
        //"serverSide": true,
        "scrollX": true,
        "scrollY": "200px",
        "scrollCollapse": true,
        "autoWidth": false,
        "language": {
            "url": "/eeda/dataTables.ch.txt"
        }
        //"ajax": "/gateOutOrder/list"
    });

   
    

} );