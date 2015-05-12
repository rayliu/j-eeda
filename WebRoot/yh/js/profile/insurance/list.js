
$(document).ready(function() {
	document.title = '保险公司查询 | '+document.title;
	$('#menu_profile').addClass('active').find('ul').addClass('in');
	var datatable=$('#example').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	        "bFilter": false,
            "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/insurance/list",
			"aoColumns": [
	            { "mDataProp":"COMPANY_NAME",
	            	"fnRender":function(obj){
	            		return "<a target='_blank' href='/insurance/edit/"+ obj.aData.PID +"'>" + obj.aData.COMPANY_NAME + "</a>";
	            	}},
	            { "mDataProp":"CONTACT_PERSON"},
	            { "mDataProp":"CONTACT_PHONE" },
	            { "mDataProp":"ADDRESS" },
	            { 
	                "mDataProp": null, 
	                "sWidth": "11%",
	                "fnRender": function(obj) { 
	                	if(obj.aData.IS_STOP != true){
	                		return "<nobr><a class='btn  btn-primary btn-sm' href='/insurance/edit/"+ obj.aData.PID +"' target='_blank'>"+
		                            "<i class='fa fa-edit'> </i> "+
		                            "编辑"+
			                        "</a> "+
			                        "<a class='btn btn-danger  btn-sm ' href='/insurance/del/"+ obj.aData.PID  +"'>"+
			                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                            "停用"+
			                        "</a></nobr>";
	                	}else{
	                		return "<nobr><a class='btn  btn-primary btn-sm' href='/insurance/edit/"+  obj.aData.PID +"' target='_blank'>"+
		                            "<i class='fa fa-edit'> </i> "+
		                            "编辑"+
			                        "</a> "+
			                        "<a class='btn btn-success  btn-sm' href='/insurance/del/"+  obj.aData.PID +"'>"+
			                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                            "启用"+
			                        "</a>";
	                	}
	                    
	                }
	            }
	            ]
	} );
	$("#name_filter,#person_filter,#address_filter").on('keyup click',function(){
		var customer_name = $("#name_filter").val();
		var person = $("#person_filter").val();
		var address = $("#address_filter").val();
		
		datatable.fnSettings().sAjaxSource = "/insurance/list?customerName=" + customer_name 
												+ "&person=" + person + "&address=" + address;
		datatable.fnDraw(); 
	});
});
