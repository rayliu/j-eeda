$(document).ready(function() {
	$('#menu_carmanage').addClass('active').find('ul').addClass('in');

	function DateDiff(d1,d2){ 
	    var day = 24 * 60 * 60 *1000; 
		try{     
		   var dateArr = d1.split("-"); 
		   var checkDate = new Date(); 
		   checkDate.setFullYear(dateArr[0], dateArr[1]-1, dateArr[2]); 
		   var checkTime = checkDate.getTime(); 
		   
		   var dateArr2 = d2.split("-"); 
		   var checkDate2 = new Date(); 
		   checkDate2.setFullYear(dateArr2[0], dateArr2[1]-1, dateArr2[2]); 
		   var checkTime2 = checkDate2.getTime(); 
		     
		   var cha = (checkTime - checkTime2)/day;   
		        return cha; 
	    }catch(e){ 
	    	return false; 
	    }
	};
	
	var unDisposePickuoIds=[];
	//行车单查询，dataTable
    var carSummaryTbody = $('#carSummaryTbody').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/carreimbursement/carSummaryOrderList",
    	"aoColumns": [ 
			    {"mDataProp":null, "sWidth":"10px", 
				  "fnRender": function(obj) {
					  unDisposePickuoIds.push(obj.aData.ID);
					  return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
			    }}, 
	          {"mDataProp":null, "sWidth":"120px",
				    "fnRender": function(obj) {
					  return "<a href='/carsummary/edit?carSummaryId="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
          	  		}
          	  },  
	          {"mDataProp":"PICKUP_NO", "sWidth":"120px"},
	          {"mDataProp":"TRANSFER_ORDER_NO","sWidth":"120px"},
	          {"mDataProp":"STATUS", "sWidth":"60px",
					"fnRender": function(obj) {
						if("new" == obj.aData.STATUS){
			    			return "新建";
			    		}else if("audit" == obj.aData.STATUS){
			    			return "已审核";
			    		}else if("revocation" == obj.aData.STATUS){
			    			return "已撤销";
			    		}else if("reimbursement" == obj.aData.STATUS){
			    			return "已报销";
			    		}else{
			    			return "";
			    		}
					}
	          },
			  {"mDataProp":"CAR_NO", "sWidth":"70px"},
			  {"mDataProp":"MAIN_DRIVER_NAME", "sWidth":"70px"},
			  {"mDataProp":"TURNOUT_TIME", "sWidth":"80px"},
			  {"mDataProp":"RETURN_TIME", "sWidth":"80px"},
			  {"mDataProp":null, "sWidth":"70px",
				  "fnRender": function(obj) {
						return DateDiff(obj.aData.RETURN_TIME,obj.aData.TURNOUT_TIME);
					}
			  },        	
			  {"mDataProp":"VOLUME", "sWidth":"70px"},           
			  {"mDataProp":"WEIGHT", "sWidth":"70px"},        	
			  {"mDataProp":"CARSUMMARYMILEAGE", "sWidth":"80px"},                        
			  {"mDataProp":"MONTH_REFUEL_AMOUNT", "sWidth":"90px"},                        
			  {"mDataProp":"REFUEL_CONSUME", "sWidth":"90px"},                        
			  {"mDataProp":"SUBSIDY", "sWidth":"70px"},                        
			  {"mDataProp":"DRIVER_SALARY", "sWidth":"70px"},                        
			  {"mDataProp":"TOLL_CHARGE", "sWidth":"60px"},                        
			  {"mDataProp":"HANDLING_CHARGES", "sWidth":"60px"},                        
			  {"mDataProp":"FINE", "sWidth":"60px"},                        
			  {"mDataProp":"DELIVERYMAN_SALARY", "sWidth":"80px"},                        
			  {"mDataProp":"PARKING_CHARGE", "sWidth":"60px"},                        
			  {"mDataProp":"QUARTERAGE", "sWidth":"60px"},                        
			  {"mDataProp":"WEIGHING_CHARGE", "sWidth":"60px"},                        
			  {"mDataProp":"OTHER_CHARGES", "sWidth":"70px"},                        
			  {"mDataProp":"TOTAL_COST", "sWidth":"100px"},           
			  {"mDataProp":"DEDUCT_APPORTION_AMOUNT", "sWidth":"100px"},  
			  {"mDataProp":"ACTUAL_PAYMENT_AMOUNT", "sWidth":"100px"}
		]          
    });
    //未报销行车单-时间按钮
    $('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN',
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
		$(".bootstrap-datetimepicker-widget").hide();
	    $('#create_stamp').trigger('keyup');
	});
	
    // 选显卡-未报销行车单
	$("#chargeCheckOrderbasic").click(function(){
		num1 = 1;
		carSummaryTbody.fnSettings().oFeatures.bServerSide = true; 
		carSummaryTbody.fnSettings().sAjaxSource = "/carsummary/carSummaryOrderList";
		carSummaryTbody.fnDraw();
	});
	//未报销行车单各种搜索
    $('#status ,#driver ,#car_no ,#transferOrderNo ,#create_stamp ,#carSummaryOrderNo').on( 'keyup click', function () {
		var status = $("#status").val();
		var driver = $("#driver").val();
		var car_no = $("#car_no").val();
		var transferOrderNo = $("#transferOrderNo").val();
		var create_stamp = $("#create_stamp").val();
		var carSummaryOrderNo = $("#carSummaryOrderNo").val();
		num1 = 1;
		carSummaryTbody.fnSettings().oFeatures.bServerSide = true; 
		carSummaryTbody.fnSettings().sAjaxSource = "/carreimbursement/carSummaryOrderList?status="+status+"&driver="+driver+"&car_no="+car_no+"&transferOrderNo="+transferOrderNo+"&create_stamp="+create_stamp+"&carSummaryOrderNo="+carSummaryOrderNo;
		carSummaryTbody.fnDraw();
	});

    //创建报销单
    $('#createBtn').click(function(e){
        e.preventDefault();
        var trArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){        		
        		trArr.push($(this).val());
        	}
        });
        console.log(trArr);
        if(trArr.length > 0){
        	$("#carSummeryIds").val(trArr);
            $('#createForm').submit();
        }else{
        	alert("请选择要创建的行车单");
        }
    });
	
    //刷新行车报销单
	var carReimbursementTbody = $('#carReimbursementTbody').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [
            { "mDataProp": "ORDER_NO", "sWidth":"120px",
            	"fnRender": function(obj) {
					return "<a href='/carreimbursement/edit?orderId="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
          	  	}
        	},
            { "mDataProp": "STATUS", "sWidth":"50px",
            	"fnRender": function(obj) {
					if("new" == obj.aData.STATUS){
		    			return "新建";
		    		}else if("audit" == obj.aData.STATUS){
		    			return "已审核";
		    		}else if("revocation" == obj.aData.STATUS){
		    			return "已撤销";
		    		}else if("reimbursement" == obj.aData.STATUS){
		    			return "已报销";
		    		}else{
		    			return "";
		    		}
				}},
            { "mDataProp": "CSO_ORDER_NO", "sWidth":"120px"},
            { "mDataProp": "CREATOR"},
            { "mDataProp": "CREATE_STAMP"},
            { "mDataProp": "AUDITOR"},
            { "mDataProp": "AUDIT_STAMP"},
            { "mDataProp": "TOTAL_COST"},
            { "mDataProp": "DEDUCT_COST"},
            { "mDataProp": "ACTUAL_COST"}
        ]
    });
	
	$('#carReimbursementList').click(function(e){
		carReimbursementTbody.fnSettings().oFeatures.bServerSide = true; 
		carReimbursementTbody.fnSettings().sAjaxSource = "/carreimbursement/list?status="+status+"&driver="+driver+"&car_no="+car_no+"&transferOrderNo="+transferOrderNo+"&create_stamp="+create_stamp+"&carSummaryOrderNo="+carSummaryOrderNo;
		carReimbursementTbody.fnDraw();
	});
});