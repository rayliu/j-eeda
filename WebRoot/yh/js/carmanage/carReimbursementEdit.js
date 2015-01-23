$(document).ready(function() {
	$('#menu_carmanage').addClass('active').find('ul').addClass('in');

    var num1 = 1;
	//行车单查询，dataTable
    var carSummaryTbody = $('#table_fin').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
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
	          {"mDataProp":"STATUS", "sWidth":"60px",
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
						return "dfa";//DateDiff(obj.aData.RETURN_TIME,obj.aData.TURNOUT_TIME);
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
	    	if(order){
	    		$("#order_no").text(order.ORDER_NO);
	    		$("#status").text(order.STATUS);
	    		//$("#creator").val(order.ORDER_NO);
	    		$("#create_time").text(order.CREATE_STAMP);
	    		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    	}else{
	    		$.scojs_message('数据保存失败', $.scojs_message.TYPE_ERROR);
	    	}
    	});
        
    });
});