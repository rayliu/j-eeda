

$(document).ready(function() {
	

    var setTdTitle=function(nRow){
        $('td:eq(0)', nRow).attr('data-title','小区/大厦名称');
        $('td:eq(1)', nRow).attr('data-title','租售状态');
        $('td:eq(2)', nRow).attr('data-title','房型');
        $('td:eq(3)', nRow).attr('data-title','区域');
        $('td:eq(4)', nRow).attr('data-title','面积(平)');
        $('td:eq(5)', nRow).attr('data-title','金额');
        $('td:eq(6)', nRow).attr('data-title','描述');
        $('td:eq(7)', nRow).attr('data-title','跟进情况');
        $('td:eq(8)', nRow).attr('data-title','创建人');
        $('td:eq(9)', nRow).attr('data-title','创建日期');
    };
    
    var type = $("#type").val();//注意这里
    //var type =  '${contractType}';
    var urlSource;

    if(type=='SP'){
    	 urlSource="/yh/spContract/spList";
    

    }else{

    	urlSource="/yh/customerContract/customerList";

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
        "sAjaxSource": urlSource,//
        "aoColumns": [   
            
        	{"mDataProp":"NAME"},
            {"mDataProp":"PARTY_ID"},        	
        	{"mDataProp":"PERIOD_FROM"},
        	{"mDataProp":"PERIOD_TO"},
            {"mDataProp":"REMARK", "sWidth": "10%"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success' href='customer/edit/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger' href='customer/edit/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ],
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
            setTdTitle(nRow);
        }        
    });

   
	
} );