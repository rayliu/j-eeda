$(document).ready(function() {
	
	
    var type = $("#type").val();//注意这里
    var urlSource;
    var urlSource3;
	if(type=='CUSTOMER'){
		$("#btn1").show();
		urlSource="/yh/customerContract/customerList";
		urlSource3="/yh/customerContract/delete/";
	}else{
		$("#btn2").show();
		urlSource="/yh/spContract/spList";
		urlSource3="/yh/spContract/delete2/";
	}
    
	//datatable, 动态处理
    $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": urlSource,
        "aoColumns": [   
            {"mDataProp":"NAME"},
            {"mDataProp":"COMPANY_NAME"},
            {"mDataProp":"CONTACT_PERSON"},
            {"mDataProp":"PHONE"},
            {"mDataProp":"PERIOD_FROM"},
            {"mDataProp":"PERIOD_TO"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success' href='/yh/customerContract/edit/"+obj.aData.CID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger' href='"+urlSource3+""+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ],
     });
} );
