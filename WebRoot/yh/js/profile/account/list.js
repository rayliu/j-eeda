$(document).ready(function() {
	$('#menu_profile').addClass('active').find('ul').addClass('in');
	$('#example').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/account/listAccount",
			"aoColumns": [
				{ "mDataProp": "BANK_NAME","sWidth": "12%",
					"fnRender": function(obj) {                    
	                    return "<a href='/account/edit/"+obj.aData.ID+"'>"+
	                                obj.aData.BANK_NAME+
	                            "</a>";
	                }
	            },
	            { "mDataProp": "TYPE","sWidth": "10%",
	            	"fnRender": function(obj) {
	            		var displayName='';
	            	    if(obj.aData.TYPE ==='REC')
	            	    	displayName='收款';
	            	    if(obj.aData.TYPE ==='PAY')
	            	    	displayName='付款';
	            	    if(obj.aData.TYPE ==='ALL')
	            	    	displayName='收款付款';
	                    return displayName;
	                }
	        	},
	            { "mDataProp": "ACCOUNT_NO","sWidth": "9%" },
	            { "mDataProp": "BANK_PERSON","sWidth": "9%" },
	            { "mDataProp": "CURRENCY","sWidth": "9%" },
	            { "mDataProp": "AMOUNT","sWidth": "9%" },
	           	{ "mDataProp": "REMARK" },
	            { 
	                "mDataProp": null, 
	                "sWidth": "15%",
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-info' href='/account/edit/"+obj.aData.ID+"'>"+
	                                
	                                "编辑"+
	                            "</a>"+
	                            "<a class='btn btn-danger' href='/account/del/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                                "删除"+
	                            "</a>";
	                }
	            } 
	            ]
	} );
});