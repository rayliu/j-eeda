$(document).ready(function() {

    document.title = '运输单查询 | '+document.title;

    $('#menu_transfer').addClass('active').find('ul').addClass('in');

    //datatable, 动态处理
    var transferOrder = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/scanOrder/list",
        "aoColumns": [   
            {"mDataProp":"ORDER_NO", "sWidth":"70px",
            	"fnRender": function(obj) {
        			if(TransferOrder.isUpdate)
        				return "<a href='/transferOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        			else
        				return obj.aData.ORDER_NO;
        		}
            },
            {"mDataProp":"CUSTOMER_COMPANY_NAME","sWidth":"280px"},
            {"mDataProp":"REMARK", "sWidth":"80px"},
            {"mDataProp":"CREATE_TIME", "sWidth":"80px",
    			"fnRender":function(obj){
    				var create_stamp=obj.aData.CREATE_TIME;
    				var str=create_stamp.substr(0,10);
    				return str;
    			}
            }
            
        ]  
    });	
});