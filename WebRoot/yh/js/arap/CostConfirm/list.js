
$(document).ready(function() {
	document.title = '付款确认单查询| '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var invoiceApplicationOrderIds = $("#invoiceApplicationOrderIds").val();
    var total = 0.00;
    var datatable=$('#costConfirm_table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costConfirm/list",
        "aoColumns": [
            {"mDataProp":"ORDER_NO", "sWidth": "100px", //付款确认单号
            	"fnRender": function(obj) {
        			return "<a href='/costConfirm/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}
            },
            {"mDataProp":"FKSQ_NO", "sWidth": "100px"}, //付款申请单号
            {"mDataProp":"STATUS", "sWidth": "60px"},//状态
            {"mDataProp":"SP_NAME", "sWidth": "100px"},//供应商
            {"mDataProp":"RECEIVE_PERSON","sWidth": "80px"},//收款人
            {"mDataProp":null,"sWidth": "60px"},//付款金额  
            {"mDataProp":null,"sWidth": "60px"},//已付金额
            {"mDataProp":null,"sWidth": "60px"},//未付金额
            {"mDataProp":"REMARKS","sWidth": "100px"},//备注
            {"mDataProp":"USER_NAME","sWidth": "60px"},//创建人
            {"mDataProp":"CREATE_DATE","sWidth": "80px",
                "fnRender":function(obj){
                    var create_stamp=obj.aData.CREATE_DATE;
                    var str=create_stamp.substr(0,10);
                    return str;
                }
            },//创建时间           
        ]      
    });	 
} );