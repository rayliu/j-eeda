
$(document).ready(function() {
	document.title = '付款确认单| '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var invoiceApplicationOrderIds = $("#invoiceApplicationOrderIds").val();
    var total = 0.00;
    var datatable=$('#InvorceApplication-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costConfirm/list?invoiceApplicationOrderIds="+invoiceApplicationOrderIds,
        "aoColumns": [   
             {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/costPreInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"PAY_AMOUNT",
    			"fnRender": function(obj) {
    				total = total + parseInt(obj.aData.PAY_AMOUNT) ;
    				$("#total").html(total);
    				$("#nopay_amount").val(total);
    				return obj.aData.PAY_AMOUNT;
    			}
        	},
            {"mDataProp":"COST_STAMP"},
            {"mDataProp":null,"sWidth": "150px"},
            {"mDataProp":null,"sWidth": "150px"},
            {"mDataProp":null,"sWidth": "150px"}                       
        ]      
    });	 
} );