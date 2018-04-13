

$(document).ready(function() {
	document.title = '供应商查询 | '+document.title;
	$('#menu_profile').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var dataTable= $('#eeda-table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示  
    	"bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/serviceProvider/list",
        "aoColumns": [ 
            {"mDataProp":"COMPANY_NAME",
            	"fnRender":function(obj){
            		if(Provider.isUpdate){
            			return "<a href='/serviceProvider/edit?id="+obj.aData.PID+"' target='_blank'>" + obj.aData.COMPANY_NAME + "</a>";
            		}else{
            			return obj.aData.COMPANY_NAME;
            		}
            		
            	}},
            {"mDataProp":"ABBR"},
            {"mDataProp":"SP_TYPE",
            	"fnRender": function(obj) {
            		var str = "";
            		if(obj.aData.SP_TYPE == "line"){
            			str = "干线运输供应商";
            		}else if(obj.aData.SP_TYPE == "delivery"){
            			str = "配送供应商";
            		}else if(obj.aData.SP_TYPE == "pickup"){
            			str = "提货供应商";
            		}else if(obj.aData.SP_TYPE == "personal"){
            			str = "个体供应商";
            		}
            		return str;
            	}
            }, 
            {"mDataProp":"CONTACT_PERSON"},        	
            {"mDataProp":"PHONE"},
            {"mDataProp":"ADDRESS","sWidth":"20%"},
            {"mDataProp":"RECEIPT"},
            {"mDataProp":"PAYMENT",
            	"fnRender": function(obj) {
	            		if(obj.aData.PAYMENT == "monthlyStatement"){
	            			return "月结";
	            		}else if(obj.aData.PAYMENT == "freightCollect"){
	            			return "到付";
	            		}else{
	            			return "现付";
	            		}}},
            {"mDataProp":null,
            	"fnRender": function(obj) {
	            		if(obj.aData.DNAME == null){
	            			return obj.aData.NAME;
	            		}else{
	            			return obj.aData.DNAME;
	            		}}
            },
            { 
                "mDataProp": null, 
                "sWidth": "8%",  
                "bVisible":(Provider.isUpdate || Provider.isDel),
                "fnRender": function(obj) {  
                	var str ="<nobr>";
                	// if(Provider.isUpdate){
                	// 	str += "<a class='btn  btn-primary btn-sm' href='/serviceProvider/edit/"+obj.aData.PID+"' target='_blank'>"+
                 //        "<i class='fa fa-edit fa-fw'></i>"+
                 //        "编辑"+"</a> ";
                	// }
                	if(Provider.isDel){
                		if(obj.aData.IS_STOP != true){
			                    str += "<a class='btn btn-danger  btn-sm' href='/serviceProvider/delete/"+obj.aData.PID+"'>"+
			                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                         "停用"+
			                         "</a>";
	                		
		               	}else{
		               		str +="<a class='btn btn-success' href='/serviceProvider/delete/"+obj.aData.PID+"'>"+
				                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
				                         "启用"+
				                     "</a>";
		               	}
                	}
                	
                	return str +="</nobr>";
                   
                }
            }                         
        ]    
    });
    
    $("#searchBtn").click(function(){
        refreshData();
    });

    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
    });
    
    var refreshData=function(){
    	var COMPANY_NAME = $("#COMPANY_NAME").val();
      	var CONTACT_PERSON = $("#CONTACT_PERSON").val();
    	var RECEIPT = $("#RECEIPT").val();
      	var ABBR = $("#ABBR").val();    	
      	var ADDRESS = $("#ADDRESS").val();
      	var LOCATION = $("#LOCATION").val();
      	
      	 var flag = false;
	        $('#searchForm input,#searchForm select').each(function(){
	        	 var textValue = this.value;
	        	 if(textValue != '' && textValue != null){
	        		 flag = true;
	        		 return;
	        	 } 
	        });
	        if(!flag){
	        	 $.scojs_message('请输入至少一个查询条件', $.scojs_message.TYPE_FALSE);
	        	 return false;
	        }
	    dataTable.fnSettings().oFeatures.bServerSide = true;   
      	dataTable.fnSettings().sAjaxSource = "/serviceProvider/list?COMPANY_NAME="+COMPANY_NAME+"&CONTACT_PERSON="+CONTACT_PERSON+"&RECEIPT="+RECEIPT+"&ABBR="+ABBR+"&ADDRESS="+ADDRESS+"&LOCATION="+LOCATION;
      	dataTable.fnDraw();
    };
	
} );