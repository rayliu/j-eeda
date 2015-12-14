$(document).ready(function() {
	if(order_no){
		document.title = order_no +' | '+document.title;
	}
	$('#menu_carmanage').addClass('active').find('ul').addClass('in');

	var DateDiff = function(d1,d2){ 
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

    var num1 = 1;
	//行车单查询，dataTable
    var carSummaryTbody = $('#table_fin').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/carreimbursement/orderDetailList?car_summary_order_ids="+$('#car_summary_order_ids').val(),
    	"aoColumns": [ 
    	    {"mDataProp": null,"sWidth":"10px",
                    "fnRender": function(obj) {
                        return num1++;
            }},
	        {"mDataProp":null, "sWidth":"120px",
				    "fnRender": function(obj) {
					  return "<a href='/carsummary/edit?carSummaryId="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
          	  		}
          	  },  
	          {"mDataProp":"PICKUP_NO", "sWidth":"120px"},
	          {"mDataProp":"TRANSFER_ORDER_NO","sWidth":"120px"},
	          {"mDataProp":"STATUS", "sWidth":"80px",
					"fnRender": function(obj) {
						if("new" == obj.aData.STATUS){
			    			return "新建";
			    		}else if("checked" == obj.aData.STATUS){
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
			  {"mDataProp":"MONTH_REFUEL_AMOUNT", "sWidth":"110px"},                        
			  {"mDataProp":"REFUEL_CONSUME", "sWidth":"90px"},                        
			  {"mDataProp":"SUBSIDY", "sWidth":"70px"},                        
			  {"mDataProp":"DRIVER_SALARY", "sWidth":"90px"},                        
			  {"mDataProp":"TOLL_CHARGE", "sWidth":"90px"},                        
			  {"mDataProp":"HANDLING_CHARGES", "sWidth":"60px"},                        
			  {"mDataProp":"FINE", "sWidth":"60px"},                        
			  {"mDataProp":"DELIVERYMAN_SALARY", "sWidth":"100px"},                        
			  {"mDataProp":"PARKING_CHARGE", "sWidth":"60px"},                        
			  {"mDataProp":"QUARTERAGE", "sWidth":"60px"},                        
			  {"mDataProp":"WEIGHING_CHARGE", "sWidth":"60px"},                        
			  {"mDataProp":"OTHER_CHARGES", "sWidth":"70px"},                        
			  {"mDataProp":"TOTAL_COST", "sWidth":"100px"},           
			  {"mDataProp":"DEDUCT_APPORTION_AMOUNT", "sWidth":"100px"},  
			  {"mDataProp":"ACTUAL_PAYMENT_AMOUNT", "sWidth":"100px"}
		]          
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
   
    	$("#carSummeryIds").val(trArr);
        $.post('/carreimbursement/saveCarReimbursement', $("#orderForm").serialize(), function(order){
	    	if(order.ORDER_NO){
	    		$("#orderId").val(order.ID);
	    		$("#order_no").text(order.ORDER_NO);
	    		$("#status").text(order.STATUS);
	    		//$("#creator").val(order.ORDER_NO);
	    		$("#create_time").text(order.CREATE_STAMP);
	    		contactUrl("edit?id",order.ID);
	    		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    	}else{
	    		$.scojs_message('数据保存失败', $.scojs_message.TYPE_ERROR);
	    	}
    	});
        
    });

    //报销单审核
    $('#accomplishBtn').click(function(e){
        e.preventDefault();
        var id = $("#orderId").val();
        if(id != "" && id != null){
        	$.post('/carreimbursement/audit', {orderId:id}, function(data){
            	console.log(data);
    	    	if(data.audit_name){
    	    		$.scojs_message('审核成功', $.scojs_message.TYPE_OK);
    	    		$("#audit_name").text(data.audit_name);
    	    		$("#audit_stamp").text(data.audit_stamp);
    	    		$('#accomplishBtn').attr('disabled', 'disabled');
    	    		$('#createBtn').attr('disabled', 'disabled');
    	    	}else{
    	    		$.scojs_message('审核失败', $.scojs_message.TYPE_ERROR);
    	    	}
        	});
        }else{
        	$.scojs_message('请先保存单据', $.scojs_message.TYPE_ERROR);
        }
    });
});