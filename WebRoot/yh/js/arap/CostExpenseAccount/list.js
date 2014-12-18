$(document).ready(function() {
	 $('#menu_finance').addClass('active').find('ul').addClass('in');
    
    //datatable, 动态处理
    var costExpenseAccountTbody = $('#costExpenseAccountTbody').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costReimbursement/reimbursementList",
        "aoColumns": [ 
			{"mDataProp":null,"sWidth":"120px",
				"fnRender": function(obj) {
					return "<a href='/costReimbursement/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
				}
			},
            {"mDataProp":"STATUS", "sWidth":"200px"},
            {"mDataProp":"CREATENAME", "sWidth":"200px"},                 	
            {"mDataProp":"CREATE_STAMP", "sWidth":"200px"},                        
            {"mDataProp":"AMOUNT", "sWidth":"150px"},                        
            {"mDataProp":"AUDITNAME", "sWidth":"100px"},                        
            {"mDataProp":"APPROVALNAME", "sWidth":"100px"},                        
            {"mDataProp":"REMARK", "sWidth":"150px"}                         
        ]      
    });	
    
    $("#orderNo ,#status ,#auditName").on( 'keyup click', function () {    	 	
    	var orderNo = $("#orderNo").val();
    	var status = $("#status").val();
    	var auditName = $("#auditName").val();
    	costExpenseAccountTbody.fnSettings().sAjaxSource = "/costReimbursement/reimbursementList?orderNo="+orderNo+"&status="+status+"&auditName="+auditName;
    	costExpenseAccountTbody.fnDraw();
    });
    
});