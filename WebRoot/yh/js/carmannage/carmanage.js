$(document).ready(function() {
$('#menu_carmanage').addClass('active').find('ul').addClass('in');
	//发车记录list
	$('#example2').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	       //"sPaginationType": "bootstrap",
		 	"iDisplayLength": 10,
	       "bServerSide": true,
	       "bRetrieve": true,
	   	"oLanguage": {
	           "sUrl": "/eeda/dataTables.ch.txt"
	       },
	       "sAjaxSource":"/yh/carinfo/carmanageList",
			"aoColumns": [
				{"mDataProp":"DRIVER"},
		            {"mDataProp":"CAR_NO"}, 
		            {"mDataProp":"CARTYPE"},
		            {"mDataProp":"LENGTH"},
		            {"mDataProp":"PHONE"},
		            {"mDataProp":"CAR_FOLLOW_NAME"},
		            {"mDataProp":"CAR_FOLLOW_PHONE"},
		            {"mDataProp":"KILOMETRES"},
		            {"mDataProp":"ROAD_BRIDGE"},  
		            {"mDataProp":"DEPART_NO"},  
	           ]
	} );
});