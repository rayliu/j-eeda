$(document).ready(function() {

    document.title = '出货单查询 | '+document.title;

    $('#menu_transfer').addClass('active').find('ul').addClass('in');

    //datatable 1.10.9, 动态处理
    var gateOutOrder = $('#eeda-table').DataTable({
        "processing": true,
        "serverSide": true,
        "ajax": "/gateOutOrder/list",
        "language": {
            "url": "/eeda/dataTables.ch.txt"
        },
     //    "bFilter": true, //不需要默认的搜索框
     //    "bSort": false, 
     //    "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
     //    "iDisplayLength": 10,
     //    "bServerSide": true,
    	// "oLanguage": {
     //        "sUrl": "/eeda/dataTables.ch.txt"
     //    },
     //    "sAjaxSource": "/gateOutOrder/list",
        "columns": [   
            {"data":"ORDER_NO", "sWidth":"70px",
          //   	"fnRender": function(obj) {
          //           debugger;
        		// 	return "<a href='/gateOutOrder/edit/"+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";        			
        		// }
                "render": function ( data, type, full, meta ) {
                  return '<a href="/gateOutOrder/edit/'+full.ID+'" target="_blank">'+data+'</a>';
                }
            },
            {"data":null,"sWidth":"280px"},
            {"data":"REMARK", "sWidth":"80px"},
            {"data":"CREATE_TIME", "sWidth":"80px",
                "render": function ( data, type, full, meta ) {
                    var str=data.substr(0,10);
                    return str;
                }
    			
            }
            
        ]  
    });	
});