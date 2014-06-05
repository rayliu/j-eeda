
$(document).ready(function() {

    $('#menu_finance').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var datatable = $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/pickupOrder/createList",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                    return '<input type="checkbox" name="order_check_box" value="'+obj.aData.ID+'">';
                 }
            },
            { "mDataProp": "ORDER_NO"},
            { "mDataProp": "CARGO_NATURE"},
            { "mDataProp": "ADDRESS"},
            { "mDataProp": "PICKUP_MODE"},
            { "mDataProp": "STATUS"}
                                      
        ]      
    });	
    
    $('#saveBtn').click(function(e){
        e.preventDefault();
    	var trArr=[];
      var tableArr=[];
        $("table tr:not(:first)").each(function(){
        	var row=$(this);
        	$("input:checked",this).each(function(){
        		trArr.push($(this).val());  
        		trArr.push($("td:eq(1)",row).html());        		
        	});          	
        	  	
        	}); 
        tableArr.push(trArr); 
       
        console.log(tableArr);
  
        /*
        $.post("/yh/privilege/editModule_pri",{localArr:Arr},function(data){     

        	   //回调函数 

        	   });
*/
    });
    
} );
