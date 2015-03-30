$(document).ready(function() {
$('#menu_carmanage').addClass('active').find('ul').addClass('in');
	//发车记录list
	$('#example2').dataTable( {
        "bFilter": false, //不需要默认的搜索框
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	       //"sPaginationType": "bootstrap",
		"iDisplayLength": 10,
	    "bServerSide": true,
	    "bRetrieve": true,
	   	"oLanguage": {
	           "sUrl": "/eeda/dataTables.ch.txt"
	       },
       "sAjaxSource":"/carinfo/carmanageList",
		"aoColumns": [
            {"mDataProp":"DEPART_NO",
            	"fnRender": function(obj) {
            			return "<a href='/pickupOrder/carManageEdit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.DEPART_NO+"</a>";
            		}}, 
    		{"mDataProp":"STATUS"},
		    {"mDataProp":"PICKUP_MODE",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == "routeSP"){
            			return "干线供应商自提";
            		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
            			return "外包供应商提货";
            		}else if(obj.aData.PICKUP_MODE == "own"){
            			return carmanage.ex_type;
            		}else{
            			return "";
            		}}},
		    {"mDataProp":"CONTACT_PERSON"},
		    {"mDataProp":"PHONE"},
		    {"mDataProp":"CAR_NO"},
		    {"mDataProp":"CARTYPE"},     
            {"mDataProp":"CAR_FOLLOW_NAME"},
            {"mDataProp":"CAR_FOLLOW_PHONE"},
            {"mDataProp":"KILOMETRES"},
            {"mDataProp":"ROAD_BRIDGE"},  
            {"mDataProp":"CREATE_STAMP"},     
            {"mDataProp":"TRANSFER_ORDER_NO"},
       ]
	} );
});