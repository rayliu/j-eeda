$.fn.dataTableExt.afnFiltering.push(
    function( oSettings, aData, iDataIndex ) {

    	if( isAreaInRange() && isAmountInRange() ){
    		return true;
    	}
    	return false;

    	function isAreaInRange(){
    		var iColumn = 4;
	        var range = $('#areaFilter').val();

	        var areaRange = range.split("-");

			var iMin = parseInt(areaRange[0])*1;
			var iMax = areaRange[1]==""? "" : parseInt(areaRange[1])*1;
	         
	        var iCellValue = aData[iColumn] == "" ? 0 : aData[iColumn]*1;
	        if ( areaRange == "" )
	        {
	            return true;
	        }else if ( iMin == "" && iMax == "" )
	        {
	            return true;
	        }
	        else if ( iMin == "" && iCellValue <= iMax )
	        {
	            return true;
	        }
	        else if ( iMin <= iCellValue && "" == iMax )
	        {
	            return true;
	        }
	        else if ( iMin <= iCellValue && iCellValue <= iMax )
	        {
	            return true;
	        }
	        return false;
    	};

    	function isAmountInRange(){
    		var iColumn = 5;
    		var range, iCellValue;
	        var statusVal = $("#status").val();

	        if(statusVal==''){
	        	return true;
	        }else if(statusVal=='出租' || statusVal=='已租' ){
	        	range = $('#rentFilter').val();	        	
	        }else{
	        	range = $('#totalFilter').val();	        	
	        }

	        if(range==''){
	        	return true;
	        }else if(statusVal=='出租' || statusVal=='已租'){
	        	iCellValue = aData[iColumn].substr(0, aData[iColumn].length-2);
	        }else{
	        	iCellValue = aData[iColumn].substr(0, aData[iColumn].length-1);
	        }

	        var amountRange = range.split("-");

			var iMin = parseInt(amountRange[0])*1;
			var iMax = amountRange[1] == ""? "" : parseInt(amountRange[1])*1;
	        
	        if ( iMin == "" && iMax == "" )
	        {
	            return true;
	        }
	        else if ( iMin == "" && iCellValue < iMax )
	        {
	            return true;
	        }
	        else if ( iMin <= iCellValue && "" == iMax )
	        {
	            return true;
	        }
	        else if ( iMin <= iCellValue && iCellValue <= iMax )
	        {
	            return true;
	        }
	        return false;
    	};
        
    }
);


$(document).ready(function() {
	
	//datatable, 静态处理
	var oTable = $('.datatable').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		"sPaginationType": "bootstrap",
		"oLanguage": {
			"sUrl": "dataTables.ch.txt"
		},
		"aoColumnDefs": [
	      { "sWidth": "15%", "aTargets": [ 0 ] },
	      { "sWidth": "8%", "aTargets": [ 1 ] },
	      { "sWidth": "5%", "aTargets": [ 2 ] },
	      { "sWidth": "5%", "aTargets": [ 3 ] },
	      { "sWidth": "8%", "aTargets": [ 4 ] },
	      { "sWidth": "10%", "aTargets": [ 5 ] },
	      { "sWidth": "15%", "aTargets": [ 6 ] },
	      { "sWidth": "25%", "aTargets": [ 7 ] },
	      { "sWidth": "7%", "aTargets": [ 8 ] },
	      { "sWidth": "10%", "aTargets": [ 9 ] }					      
	    ],
	    "aaSorting": [[ 9, "desc" ]]
	} );
	/* //datatable, 动态处理
    $('#eeda-table').dataTable({
    	"oLanguage": {
            "sUrl": "dataTables.ch.txt"
        },
        "sPaginationType": "full_numbers",
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "listLeads",
        "aoColumns": [   
        	{"mData":"TITLE"},
        	{"mData":"STATUS"},
            {"mData":"TYPE"},
            {"mData":"REGION"},
            {"mData":"INTRO"},
        	{"mData":"REMARK"},
            {"mData":"LOWEST_PRICE"},
            {"mData":"AGENT_FEE"},
            {"mData":"INTRODUCER"},
        	{"mData":"SALES"},
            {"mData":"FOLLOWER"},
            {"mData":"FOLLOWER_PHONE"},
            {"mData":"OWNER"},
            <% if(shiro.hasAnyRole("admin,property_mananger,property_internal_user")){ %>
        	{"mData":"OWNER_PHONE"},
        	<%}%>
        	{"mData":"CREATOR"},
            {"mData":"STATUS"},
            {"mData":"CREATE_DATE"}                        
        ],
        "aoColumnDefs": [ {
	      "aTargets": [ 0 ],
	      "mData": "download_link",
	      "mRender": function ( data, type, full ) {
	        return '<a href="'+data+'">Download</a>';
	      }
	    }]
    });*/

	var getFilterVal=function(){
		return $("#status").val()+' '+$("#type").val()+' '+$("#region").val();
	}
	
	$("#resetBtn").on("click", function(e){
		e.preventDefault();
		var input_box = $('#eeda-table_filter input').first();
        input_box.val('');
        $("#status").val('');
        $("#type").val('');
        $("#region").val('');
        oTable.fnFilter(input_box.val(), null, false, true);
        $('#totalFilterDiv').hide();
        $('#rentFilterDiv').hide();
    });

	$("#status").on("change", function(){
        var typeVal = $(this).val();
        oTable.fnFilter(typeVal, 1, false, true);
        if(typeVal==''){
        	$('#totalFilterDiv').hide();
        	$('#rentFilterDiv').hide();
        }else if(typeVal=='出租' || typeVal=='已租' ){
        	$('#totalFilterDiv').hide();
        	$('#rentFilterDiv').show();
        }else{
        	$('#totalFilterDiv').show();
        	$('#rentFilterDiv').hide();
        }

    });

    $("#type").on("change", function(){
        var typeVal = $(this).val();
        oTable.fnFilter(typeVal, 2, false, true);
    });
    
    $("#region").on("change", function(){
        var typeVal = $(this).val();
        oTable.fnFilter(typeVal, 3, false, true);
    });

    /* Add event listeners to the two range filtering inputs */
	$('#areaFilter').on("change", function() {
		oTable.fnDraw(); 
	} );
	$('#totalFilter').on("change", function() {
		oTable.fnDraw(); 
	} );
	$('#rentFilter').on("change", function() {
		oTable.fnDraw(); 
	} );

	jQuery.fn.limit=function(){ 
	    var self = $("td[limit]"); 
	    self.each(function(){ 
	        var objString = $(this).text(); 
	        var objLength = $(this).text().length; 
	        var num = $(this).attr("limit"); 
	        if(objLength > num){ 
				$(this).attr("title",objString); 
	            objString = $(this).text(objString.substring(0,num) + "..."); 
	        } 
	    }) 
	} 

	$("#eeda-table").limit(); 
	
	
} );