

$(document).ready(function() {
	$('#menu_profile').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
   var dataTable= $('#eeda-table').dataTable({
	   "bFilter": false, //不需要默认的搜索框 
	   //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "/customer/list",
        "aoColumns": [   
            {"mDataProp":"COMPANY_NAME"},
            {"mDataProp":"ABBR"},
            {"mDataProp":"CONTACT_PERSON"},        	
            {"mDataProp":"PHONE"},
            {"mDataProp":"ADDRESS","sWidth": "15%"},
            {"mDataProp":"RECEIPT"},
            {"mDataProp":"PAYMENT",
            	"fnRender": function(obj) {
            		if(obj.aData.PAYMENT == "monthlyStatement"){
            			return "月结";
            		}else if(obj.aData.PAYMENT == "freightCollect"){
            			return "到付";
            		}else{
            			return "现付";
            		}}},
            {"mDataProp":null,
            	"fnRender": function(obj) {
	            		if(obj.aData.DNAME == null){
	            			return obj.aData.NAME;
	            		}else{
	            			return obj.aData.DNAME;
	            		}}
            },
            { 
                "mDataProp": null, 
                "sWidth": "11%",  
                "bVisible":(Customer.updatePermission || Customer.delPermission),
                "fnRender": function(obj) {  
                	var str="<nobr>";
                	if(Customer.updatePermission){
            			str +="<a class='btn  btn-primary btn-sm' href='/customer/edit/"+obj.aData.PID+"'>"+
	                            "<i class='fa fa-edit fa-fw'></i>"+
	                            "编辑"+"</a> ";
            		}
                	if(obj.aData.IS_STOP != true){
                		if(Customer.delPermission){
                			str += "<a class='btn btn-danger  btn-sm' href='/customer/delete/"+obj.aData.PID+"'>"+
		                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
		                         "停用"+
		                         "</a>";
                		}
                	}else{
                		if(Customer.delPermission){
                			str +="<a class='btn btn-success' href='/customer/delete/"+obj.aData.PID+"'>"+
	                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                         "启用"+
	                     "</a>";
                		}
                	}
                	str +="</nobr>";
                   return str;
                }
            }                         
        ]    
    });
    
  //条件筛选
	$("#COMPANY_NAME,#CONTACT_PERSON ,#RECEIPT,#ABBR,#ADDRESS,#LOCATION").on('keyup click', function () {    	 	
      	var COMPANY_NAME = $("#COMPANY_NAME").val();
      	var CONTACT_PERSON = $("#CONTACT_PERSON").val();
    	var RECEIPT = $("#RECEIPT").val();
      	var ABBR = $("#ABBR").val();    	
      	var ADDRESS = $("#ADDRESS").val();
      	var LOCATION = $("#LOCATION").val();
      	dataTable.fnSettings().sAjaxSource = "/customer/list?COMPANY_NAME="+COMPANY_NAME+"&CONTACT_PERSON="+CONTACT_PERSON+"&RECEIPT="+RECEIPT+"&ABBR="+ABBR+"&ADDRESS="+ADDRESS+"&LOCATION="+LOCATION;
      	dataTable.fnDraw();
      });
} );