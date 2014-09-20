

$(document).ready(function() {
	$('#menu_profile').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var dataTable= $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "/serviceProvider/list",
        "aoColumns": [   
            
            {"mDataProp":"COMPANY_NAME"},
            {"mDataProp":"ABBR"},
            {"mDataProp":"SP_TYPE"}, 
            {"mDataProp":"CONTACT_PERSON"},        	
            {"mDataProp":"MOBILE"},
            {"mDataProp":"ADDRESS"},
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
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success' href='/serviceProvider/edit/"+obj.aData.PID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger' href='/serviceProvider/delete/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ]    
    });

  //条件筛选
	$("#COMPANY_NAME ,#CONTACT_PERSON ,#RECEIPT,#ABBR,#ADDRESS,#LOCATION").on('keyup click', function () {    	 	
      	var COMPANY_NAME = $("#COMPANY_NAME").val();
      	var CONTACT_PERSON = $("#CONTACT_PERSON").val();
    	var RECEIPT = $("#RECEIPT").val();
      	var ABBR = $("#ABBR").val();    	
      	var ADDRESS = $("#ADDRESS").val();
      	var LOCATION = $("#LOCATION").val();
      	dataTable.fnSettings().sAjaxSource = "/serviceProvider/list?COMPANY_NAME="+COMPANY_NAME+"&CONTACT_PERSON="+CONTACT_PERSON+"&RECEIPT="+RECEIPT+"&ABBR="+ABBR+"&ADDRESS="+ADDRESS+"&LOCATION="+LOCATION;

      	dataTable.fnDraw();
      });
	
} );