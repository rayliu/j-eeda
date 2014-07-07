$(document).ready(function() {
	$('#menu_contract').addClass('active').find('ul').addClass('in');
	
    var type = $("#type").val();//注意这里
    var urlSource;
    var urlSource3;
	if(type=='CUSTOMER'){
		$("#btn1").show();
		urlSource="/yh/customerContract/customerList";
		urlSource2="/yh/customerContract/edit/";
		urlSource3="/yh/customerContract/delete/";
	}if(type=='SERVICE_PROVIDER'){
		$("#btn2").show();
		urlSource="/yh/spContract/spList";
		urlSource2="/yh/spContract/edit/";
		urlSource3="/yh/spContract/delete2/";
	}if(type=='DELIVERY_SERVICE_PROVIDER'){
		$("#btn3").show();
		urlSource="/yh/deliverySpContract/deliveryspList";
		urlSource2="/yh/deliverySpContract/edit/";
		urlSource3="/yh/deliverySpContract/delete3/";
	}
    
	//datatable, 动态处理
   var tab2= $('#eeda-table').dataTable({
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
            {"mDataProp":"COMPANY_NAME",
            	"sWidth": "15%"},
            {"mDataProp":"CONTACT_PERSON"},
            {"mDataProp":"MOBILE"},
            {"mDataProp":"PERIOD_FROM"},
            {"mDataProp":"PERIOD_TO"},
            { 
                "mDataProp": null, 
                "sWidth": "11%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success' title='编辑' href='"+urlSource2+""+obj.aData.CID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                            "</a>"+
                            "<a class='btn btn-danger' title='删除' href='"+urlSource3+""+obj.aData.CID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                            "</a>";
                }
            }                         
        ],
     });
   $('#datetimepicker').datetimepicker({  
       format: 'yyyy-MM-dd',  
       language: 'zh-CN'
   }).on('changeDate', function(ev){
       $('#beginTime_filter').trigger('keyup');
   });


   $('#datetimepicker2').datetimepicker({  
       format: 'yyyy-MM-dd',  
       language: 'zh-CN', 
       autoclose: true,
       pickerPosition: "bottom-left"
   }).on('changeDate', function(ev){
       $('#endTime_filter').trigger('keyup');
   });
    //条件搜索
    $("#contractName_filter,#contactPerson_filter,#periodFrom_filter,#companyName_filter,#phone_filter,#periodTo_filter").on('keyup click', function () {    	 	
      	var contractName_filter = $("#contractName_filter").val();
      	var contactPerson_filter = $("#contactPerson_filter").val();
    	var periodFrom_filter = $("#periodFrom_filter").val();
      	var companyName_filter = $("#companyName_filter").val();
      	var phone_filter = $("#phone_filter").val();   
      	var periodTo_filter = $("#periodTo_filter").val();
      	if(type=='CUSTOMER'){
      		tab2.fnSettings().sAjaxSource = "/yh/customerContract/customerList?contractName_filter="+contractName_filter+"&contactPerson_filter="+contactPerson_filter+"&periodFrom_filter="+periodFrom_filter+"&companyName_filter="+companyName_filter+"&phone_filter="+phone_filter+"&periodTo_filter="+periodTo_filter;
    	}if(type=='SERVICE_PROVIDER'){
    		tab2.fnSettings().sAjaxSource = "/yh/spContract/spList?contractName_filter="+contractName_filter+"&contactPerson_filter="+contactPerson_filter+"&periodFrom_filter="+periodFrom_filter+"&companyName_filter="+companyName_filter+"&phone_filter="+phone_filter+"&periodTo_filter="+periodTo_filter;
    	}if(type=='DELIVERY_SERVICE_PROVIDER'){
    		tab2.fnSettings().sAjaxSource = "/yh/deliverySpContract/deliveryspList?contractName_filter="+contractName_filter+"&contactPerson_filter="+contactPerson_filter+"&periodFrom_filter="+periodFrom_filter+"&companyName_filter="+companyName_filter+"&phone_filter="+phone_filter+"&periodTo_filter="+periodTo_filter;
    	}
      	tab2.fnDraw();
      });
    
} );
