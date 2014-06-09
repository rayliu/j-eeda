
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
    }

	//datatable, 动态处理
    var oTable = $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sPaginationType": "bootstrap",
        "iDisplayLength": 10,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sPaginationType": "full_numbers",
        "bProcessing": false,
        "bServerSide": true,
        "sAjaxSource": "/splist/list",
        "aoColumns": [
            { 
                "mDataProp": null, 
                "sWidth": "15%",
                "fnRender": function(obj) {                    
                    return  "<a class='btn btn-info' href='#' id='"+obj.aData.ID+"'>"+
                                "<i class='icon-edit icon-white'></i>"+
                                "发邮件"+
                            "</a>";
                }
            }, 
            {"mDataProp": "MAIL_SENT_TIME"},
            {"mDataProp": null}, 
            {"mDataProp":"PROVIDER_NAME",
                "fnRender": function(obj) {                    
                    return  "<a href='#' id='"+obj.aData.ID+"'>"+
                                obj.aData.PROVIDER_NAME+                                
                            "</a> "+
                            "<a href='http://www.baidu.com/#wd="+obj.aData.PROVIDER_NAME+"' class='btn btn-info' target='_blank' >查找</a>";
                }
            },
            {"mDataProp":"MAINTENANCE_OFFICE"},
            {"mDataProp":"CONTACT"},        	
        	{"mDataProp":"EMAIL", "sWidth": "25px"},
            {"mDataProp":"PHONE_NO"},
            {"mDataProp":"FAX_NO"},
            {"mDataProp":"ADDRESS1"},
            {"mDataProp":"COUNTRY"},        
        	{"mDataProp":"PROVINCE"},
            {"mDataProp":"CITY", "sWidth": "10%"}            
        ]
    });

    $("#eeda-table").on("click", 'a.btn', function(e){
        e.preventDefault();
        id=$(this).attr('id');
        $.post("/splist/sendMarketingMail/", {id: id}, function(data){

        });
	});
	
    $('.editLink').click(function(e){
        e.preventDefault();
        url=$(this).attr('href');
        url=url+buildQueryParas();
        window.location.href = url; 
    });
    
    var buildQueryParas=function(){
        return '?status='+$("#status").val()
            +'&type='+$("#type").val()
            +'&region='+$("#region").val()
            +'&area_min='+$('#area_min').val()
            +'&area_max='+$('#area_max').val()
            +'&rent_min='+$('#rent_min').val()
            +'&rent_max='+$('#rent_max').val()
            +'&total_min='+$('#total_min').val()
            +'&total_max='+$('#total_max').val()
            +'&fitler_building_no='+$('#fitler_building_no').val()
            +'&fitler_building_unit='+$('#fitler_building_unit').val()
            +'&fitler_room_no='+$('#fitler_room_no').val();
    }
    
    var initSearch=function(){
		
		//var input_box = $('#eeda-table_filter input').first();
        //input_box.val('');
        
        $("#status").val(getQueryStringRegExp("status")).trigger('change');
        $("#type").val(getQueryStringRegExp("type")).trigger('change');
        $("#region").val(getQueryStringRegExp("region")).trigger('change');

        $('#area_min').val(getQueryStringRegExp("area_min")).trigger('change');
		$('#area_max').val(getQueryStringRegExp("area_max")).trigger('change');
		$('#rent_min').val(getQueryStringRegExp("rent_min")).trigger('change');
		$('#rent_max').val(getQueryStringRegExp("rent_max")).trigger('change');
		$('#total_min').val(getQueryStringRegExp("total_min")).trigger('change');
		$('#total_max').val(getQueryStringRegExp("total_max")).trigger('change');
		$('#fitler_building_no').val(getQueryStringRegExp("fitler_building_no")).trigger('change');
		$('#fitler_building_unit').val(getQueryStringRegExp("fitler_building_unit")).trigger('change');
		$('#fitler_room_no').val(getQueryStringRegExp("fitler_room_no")).trigger('change');
        
    };
    
	$("#resetBtn").on("click", function(e){
		e.preventDefault();
		var input_box = $('#eeda-table_filter input').first();
        input_box.val('');

        $("#status").val('').trigger('change');
        $("#type").val('').trigger('change');
        $("#region").val('').trigger('change');

        $('#area_min').val('').trigger('change');
		$('#area_max').val('').trigger('change');
		$('#rent_min').val('').trigger('change');
		$('#rent_max').val('').trigger('change');
		$('#total_min').val('').trigger('change');
		$('#total_max').val('').trigger('change');
		$('#fitler_building_no').val('').trigger('change');
		$('#fitler_building_unit').val('').trigger('change');
		$('#fitler_room_no').val('').trigger('change');
        
        $('#totalFilterDiv').hide();
        $('#rentFilterDiv').hide();
        
        $("#area_min").val(0).trigger('keyup'); 
        $("#rent_min").val(0).trigger('keyup'); 
        localStorage.clear();
        $("#area_min").val("");
        $("#rent_min").val("");
        oTable.fnFilter('', null, false, true);
        
    });

	$("#office").on("change", function(){
        var typeVal = $(this).val();
        oTable.fnFilter(typeVal, 4, false, true);
    });

    $("#type").on("change", function(){
		localStorage.setItem('type', $("#type").val());
        var typeVal = $(this).val();
        if('allDepartment'!=typeVal){
        	oTable.fnFilter(typeVal, 5, false, true);
        }else{
        	oTable.fnFilter('', 5, false, true);
        	oTable.fnDraw(); 
        }        
    });
    
    $("#region").on("change", function(){
		localStorage.setItem('region', $("#region").val());
        var typeVal = $(this).val();
        oTable.fnFilter(typeVal, 6, false, true);
    });

    /* Add event listeners to the two range filtering inputs */
	$('#area_min').on("keyup", function() {
		localStorage.setItem('area_min', $("#area_min").val());
        var typeVal = $(this).val()+"-"+$('#area_max').val();
		oTable.fnFilter(typeVal, 7, false, true);
	});
	$('#area_max').on("keyup", function() {
		localStorage.setItem('area_max', $("#area_max").val());
        var typeVal = $('#area_min').val()+"-"+$(this).val();
		oTable.fnFilter(typeVal, 7, false, true);
	});
	$('#rent_min').on("keyup", function() {
		localStorage.setItem('rent_min', $("#rent_min").val());
		var typeVal = $(this).val()+"-"+$('#rent_max').val();
        oTable.fnFilter(typeVal, 8, false, true);
	});
	$('#rent_max').on("keyup", function() {
		localStorage.setItem('rent_max', $("#rent_max").val());
		var typeVal = $('#rent_min').val()+"-"+$(this).val();
        oTable.fnFilter(typeVal, 8, false, true);
	});
	$('#total_min').on("keyup", function() {
		localStorage.setItem('total_min', $("#total_min").val());
		oTable.fnDraw(); 
	});
	$('#total_max').on("keyup", function() {
		localStorage.setItem('total_max', $("#total_max").val());
		oTable.fnDraw(); 
	});
	$('#fitler_building_no').on("keyup", function() {
		localStorage.setItem('fitler_building_no', $("#fitler_building_no").val());
		oTable.fnFilter($(this).val(), 0, false, true);
	});
	$('#fitler_building_unit').on("keyup", function() {
		localStorage.setItem('fitler_building_unit', $("#fitler_building_unit").val());
		oTable.fnFilter($(this).val(), 1, false, true);
	});
	$('#fitler_room_no').on("keyup", function() {
		localStorage.setItem('fitler_room_no', $("#fitler_room_no").val());
		oTable.fnFilter($(this).val(), 2, false, true);
	});

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