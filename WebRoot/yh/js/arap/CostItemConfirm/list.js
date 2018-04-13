$(document).ready(function() {
	document.title = '应付明细确认| '+document.title;
    $('#menu_cost').addClass('active').find('ul').addClass('in');
   
    $("input[name='allCheck']").click(function(){
    	$("input[name='order_check_box']").each(function () {  
    		  
            this.checked = !this.checked;  
  
         });  

    });
    
	//datatable, 动态处理
    var costConfiremTable = $('#costConfirem-table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示  
        "bFilter": false, //不需要默认的搜索框
        "bSort": true, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        //"bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/costConfirmList/list",
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
          $(nRow).attr('id', aData.DID);
          $(nRow).attr('ids', aData.ID);
          $(nRow).attr('order_ty', aData.BUSINESS_TYPE);
          return nRow;
        },
        "fnDrawCallback": function( settings ) {
	           $('#searchButton').attr('disabled',false);
	           $('#searchButton').text('查询');
	    },
        "aoColumns": [ 
            { "mDataProp": null, "sWidth":"10px", "bSortable": false,
                "fnRender": function(obj) {
	              return '<input type="checkbox" name="order_check_box" id="'+obj.aData.ID+'" order_no="'+obj.aData.BUSINESS_TYPE+'">';	
                }
            },
            {"mDataProp":"BUSINESS_TYPE", "sWidth":"100px"},            	
            {"mDataProp":"SPNAME", "sWidth":"160px"},
           
            	
            {"mDataProp":"BOOKING_NOTE_NUMBER", "sWidth":"110px",//托运单号
            	"fnRender":function(obj){
            		var number = obj.aData.BOOKING_NOTE_NUMBER;
            		if(number == ""){
            			return "";
            		}else{
            			return number;
            	}		
            }}, 
            {"mDataProp":"REF_NO", "sWidth":"80px"},
            {"mDataProp":"PAY_AMOUNT", "sWidth":"100px"},
            {"mDataProp":"CHANGE_AMOUNT","sWidth":"100px",
                "fnRender": function(obj) {
              	  if(obj.aData.BUSINESS_TYPE=="成本单"){
              		  return obj.aData.CHANGE_AMOUNT;
              	  }
              	  else{
                      if(obj.aData.CHANGE_AMOUNT!=''&& obj.aData.CHANGE_AMOUNT != null){
                          return "<input type='text' style='width:60px' class='cls' name='change_amount' value='"+obj.aData.CHANGE_AMOUNT+"'/>";
                          
                      }
                      else {
                        if(obj.aData.PAY_AMOUNT!=null){
                          return "<input type='text' style='width:60px' class='cls' name='change_amount' value='"+obj.aData.PAY_AMOUNT+"'/>";
                        }
                        else{
                          return "<input type='text' style='width:60px' class='cls' name='change_amount' value='0'/>";
                        }
                      }
              	  }
                  }
              },
            {"mDataProp":"SERIAL_NO", "sWidth":"100px"},
            {"mDataProp":"REMARK", "sWidth":"150px",
             	 "fnRender": function(obj) {
             		 var remark = obj.aData.REMARK;
             		 if(remark==null)
             			 remark='';
             		 return "<textarea style='' name='remark'/>"+remark+"</textarea>";
             	 }
             },
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"100px"},
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"140px"},
            {"mDataProp":"AMOUNT", "sWidth":"55px"},
            {"mDataProp":"CNAME", "sWidth":"100px"},
            {"mDataProp":"DEPART_TIME", "sWidth":"130px", 
            	"fnRender":function(obj){
            		var timeStamp = obj.aData.DEPART_TIME;
            		if(timeStamp==null){
            			return "";
            		}
            		var subtimeStamp=timeStamp.substring(0,10);
            		if(timeStamp == ""){
            			return "";
            		}else{
            			return subtimeStamp;
            		}
            	}},
            	{"mDataProp":"PLANNING_TIME1", "sWidth":"130px",
                	"fnRender":function(obj){
                		var timeStamp = obj.aData.PLANNING_TIME1;
                		if(timeStamp==null || typeof(timeStamp) =="object"){
                			return "";
                		}
                		var subtimeStamp=timeStamp;
                		if(timeStamp == ""){
                			return "";
                		}else{
                			return subtimeStamp;
                		}
                	}},
                {"mDataProp":"RECEIVINGUNIT", "sWidth":"140px"},
            	  {"mDataProp":"ORDER_NO", "sWidth":"210px", 
                    "fnRender": function(obj) {
                      var order_no = obj.aData.ORDER_NO;
                      var suborder_no=order_no.substring(order_no.length-2,order_no.length);
                      if(suborder_no=='DB'){
                        return order_no;
                      }else{
                          return eeda.getUrlByNo(obj.aData.ID, obj.aData.ORDER_NO);
                      }
                    }
                },
            {"mDataProp":"STATUS", "sWidth": "100px",
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },   
            {"mDataProp":"ROUTE_FROM", "sWidth":"75px"},
            {"mDataProp":"ROUTE_TO", "sWidth":"75px"},                      
            {"mDataProp":"RETURN_ORDER_COLLECTION", "sWidth":"75px"},  
		    {"mDataProp":null, "sWidth":"75px",
                "fnRender": function(obj) {
                    return "未确认";
            }},
            {"mDataProp":"VOLUME", "sWidth":"35px"},
            {"mDataProp":"WEIGHT", "sWidth":"40px"}, 
            {"mDataProp":"TRANSPORT_COST", "sWidth":"50px",
            	"fnRender":function(obj){
            		if(obj.aData.TRANSPORT_COST == null){
            			return "";
            		}else{
            			return obj.aData.TRANSPORT_COST;
            		}
            	}}, 
            {"mDataProp":"CARRY_COST", "sWidth":"50px"},
            {"mDataProp":"CLIMB_COST", "sWidth":"50px"}, 
            {"mDataProp":"INSURANCE_COST", "sWidth":"50px"}, 
            {"mDataProp":"TAKE_COST", "sWidth":"50px"},
            {"mDataProp":"ANZHUANG_COST", "sWidth":"50px"},
            {"mDataProp":"CANGCHU_COST", "sWidth":"50px"}, 
            {"mDataProp":"OTHER_COST", "sWidth":"80px"}, 
            
            {"mDataProp":"CREATE_STAMP", "sWidth":"100px",
            	"fnRender":function(obj){
            		var timeStamp = obj.aData.CREATE_STAMP;
            		if(timeStamp==null || typeof(timeStamp) =="object"){
            			return "";
            		}
            		var subtimeStamp=timeStamp.substring(0,10);
            		if(timeStamp == ""){
            			return "";
            		}else{
            			return subtimeStamp;
            		}
            	}
            }, 
            {"mDataProp":"OFFICE_NAME", "sWidth":"80px"}                       
        ]      
    });	
    
    
    //动态更改备注
    $("#costConfirem-table").on('blur', 'textarea', function(e){
		e.preventDefault();
		var order_type = $(this).parent().parent().attr('order_ty');
		var remark = $(this).val();
		var order_id = $(this).parent().parent().attr('ids');
		$.post('/costCheckOrder/updateOrderRemark', {order_type:order_type,remark:remark,order_id:order_id}, function(data){
			 if(data.success){
	            $.scojs_message('备注给更新成功', $.scojs_message.TYPE_OK);
	         }else{
	            $.scojs_message('备注给更新失败', $.scojs_message.TYPE_ERROR);
	         }
	    },'json');
		uncheckedCostCheckTable.fnDraw();
	});
     
    $('#costConfirem-table').on('blur', 'input:text', function(e){
      e.preventDefault();
      var paymentId = $(this).parent().parent().attr("id");
      var departId = $(this).parent().parent().attr("ids");
      var ty = $(this).parent().parent().attr("order_ty");
      var name = $(this).attr("name");
      var value = $(this).val();
       if(isNaN(value)){      
         alert("调整金额为数字类型");
       }else{
         $.post('/costCheckOrder/updateDepartOrderFinItem', 
            {ty:ty,departId:departId,paymentId:paymentId, name:name, value:value}, 
            function(data){
              $.scojs_message('调整金额成功', $.scojs_message.TYPE_OK);
            },'json');
         
       }
    }); 
    
    $('#plandatetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#plantime').trigger('keyup');
    });

    $('#arrivaldatetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#arrivaltime').trigger('keyup');
    });

    $("#costConfiremBtn").click(function(e){
        e.preventDefault();
        $("#costConfiremBtn").attr("disabled",true);
        
    	var idArr=[];
    	var orderNoArr=[];    
    	var $checked = [];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true && $(this).prop('disabled') == false){
        		idArr.push($(this).attr('id'));
        		orderNoArr.push($(this).attr('order_no'));
        		$(this).prop('disabled',true);
        		$checked.push($(this));
        	}
        });  
        
        if(idArr.length==0){
        	$.scojs_message('请选择单据', $.scojs_message.TYPE_TYPE_ERROR);
        	$("#costConfiremBtn").attr("disabled",false);
        	return false;
        }
        
        if(!confirm('是否确认'+idArr.length+'份单据')){
        	$("#costConfiremBtn").attr("disabled",false);
        	for(var i = 0;i<$checked.length;i++){
        		$checked[i].attr("disabled",false);
        	}
        	return false;
        }
        console.log(idArr);
        var ids = idArr.join(",");
        var orderNos = orderNoArr.join(",");
        $.post("/costConfirmList/costConfiremReturnOrder", {ids:ids, orderNos:orderNos}, function(data){
        	if(data.success){
        		$("#costConfiremBtn").attr("disabled",false);
        		//refreshData();
        		
        		for(var i = 0;i<$checked.length;i++){
            		$checked[i].parent().parent().hide();
            	}
        		$.scojs_message('单据确认成功', $.scojs_message.TYPE_OK);
        	}else{
        		alert('确认失败，请联系管理员进行优化');
        	}
        },'json');
    });
       $('#companyList').on('mousedown', function(){
           return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
       });
       
       	$('#datetimepicker').datetimepicker({  
            format: 'yyyy-MM-dd',  
            language: 'zh-CN'
        }).on('changeDate', function(ev){
            $(".bootstrap-datetimepicker-widget").hide();   
            $('#beginTime_filter').trigger('keyup');
        });


        $('#datetimepicker2').datetimepicker({  
            format: 'yyyy-MM-dd',  
            language: 'zh-CN', 
            autoclose: true,
            pickerPosition: "bottom-left"
        }).on('changeDate', function(ev){
            $(".bootstrap-datetimepicker-widget").hide();
            $('#endTime_filter').trigger('keyup');
        });
        
        $("#resetBtn").click(function(){
            $('#searchFrom')[0].reset();
        });
        
       var refreshData = function(){
    	    $("#searchButton").attr("disabled",true);
       		$("#searchButton").text("查询中···");
        	var orderNo = $('#orderNo_filter').val().trim();
    		var sp = $("#sp_id_input").val().trim();
    		var no = $("#operation_number").val().trim();
    		var beginTime = $("#beginTime_filter").val().trim();
    		var endTime = $("#endTime_filter").val().trim();
    		var status = $("#order_status_filter").val().trim();
    		var type = $("#order_type_filter").val().trim();
    		var booking_note_number = $("#booking_note_number").val().trim();
    		var route_from =$("#route_from").val().trim();
    		var route_to = $("#route_to").val().trim();
    		var customer_id = $("#customer_id").val().trim();
    		var customer_name = $("#customer_id_input").val().trim();
    		var plantime = $("#plantime").val().trim();
    		var arrivaltime = $("#arrivaltime").val().trim();
    		var serial_no = $("#serial_no").val().trim();
    		var sign_no = $("#sign_no").val().trim();
    		
    		var flag = false;
	        $('#searchFrom input,#searchFrom select').each(function(){
	        	 var textValue = this.value;
	        	 if(textValue != '' && textValue != null){
	        		 flag = true;
	        		 return;
	        	 } 
	        });
	        if(!flag){
	        	 $.scojs_message('请输入至少一个查询条件', $.scojs_message.TYPE_FALSE);
	        	 $("#searchButton").attr("disabled",false);
	        		$("#searchButton").text("查询");
	        	 return false;
	        }
    		
    		costConfiremTable.fnSettings().oFeatures.bServerSide = true;
    		costConfiremTable.fnSettings().sAjaxSource = "/costConfirmList/list?orderNo="+orderNo
                              							+"&sp="+sp
											    		+"&no="+no
											    		+"&beginTime="+beginTime
											    		+"&endTime="+endTime
											    		+"&status="+status
											    		+"&type="+type
											    		+"&booking_note_number="+booking_note_number
							                            +"&route_from="+route_from
							                            +"&route_to="+route_to
							                            +"&customer_id="+customer_id
							                            +"&customer_name="+customer_name
    													+"&plantime="+plantime
    													+"&arrivaltime="+arrivaltime
    													+"&serial_no="+serial_no
    													+"&sign_no="+sign_no;
    			saveConditions();
    		  costConfiremTable.fnDraw();
       };
       
       /*=====================条件过滤=======================*/
      $('#searchButton').click(function(){
          refreshData();
      });
      //过滤客户
      // $('#plantime,#arrivaltime,#route_to,#operation_number,#route_from,#customer_name,#orderNo_filter,#operation number,#beginTime_filter,#endTime_filter,#booking_note_number').on('keyup', function () {
      //   	refreshData();
     	// });

      // $('#order_type_filter,#arrivaltime,#order_status_filter').on( 'change', function () {
      //   	refreshData();
     	// } );
      
      $("#costConfirem-table").on('keydown', 'input:text', function(e){//回车换行
   	   var key = e.which;
    	  if (key == 13) {
    		 $(this).parent().parent().next().find('.cls').focus();
    	  }
      });
      
      var saveConditions = function(){//回显查询
    	 var condition = {
    			  orderNo_filter:$('#orderNo_filter').val(),
    			  order_type_filter:$('#order_type_filter').val(),
    			  route_from:$('#route_from').val(),
    			  plantime:$('#plantime').val(),
    			  sp_id:$('#sp_id_input').val(),
    			  operation_number:$('#operation_number').val(),
    			  route_to:$('#route_to').val(),
    			  arrivaltime:$('#arrivaltime').val(),
    			  customer_id_input:$('#customer_id_input').val(),
    			  customer_id:$('#customer_id').val(),
    			  booking_note_number:$('#booking_note_number').val(),
    			  order_status_filter:$('#order_status_filter').val(),
    			  serial_no:$('#serial_no').val(),
    			  beginTime_filter:$('#beginTime_filter').val(),
    			  endTime_filter:$('#endTime_filter').val(),
    			  sign_no: $('#sign_no').val(),
    	  };	
    	  if(!!window.localStorage){
    		  localStorage.setItem("query_costItemConfirm", JSON.stringify(condition)); 
    	  }
      };
      
      var loadCondition = function(){//回显查询
    	 
    	  if(!!window.localStorage){
    		 
    		  var query_json = localStorage.getItem('query_costItemConfirm');
    		  if(!query_json)
    			 return;
    		 
    		  var condition = JSON.parse(query_json);
    		  $('#orderNo_filter').val(condition.orderNo_filter);
    		  $('#order_type_filter').val(condition.order_type_filter);
    		  $('#route_from').val(condition.route_from);
    		  $('#plantime').val(condition.plantime);
    		  $('#sp_id_input').val(condition.sp_id);
    		  $('#operation_number').val(condition.operation_number);
    		  $('#route_to').val(condition.route_to);
    		  $('#arrivaltime').val(condition.arrivaltime);
    		  $('#customer_id_input').val(condition.customer_id_input);
    		  $('#customer_id').val(condition.customer_id);
    		  $('#booking_note_number').val(condition.booking_note_number);
    		  $('#order_status_filter').val(condition.order_status_filter);
    		  $('#serial_no').val(condition.serial_no);
    		  $('#beginTime_filter').val(condition.beginTime_filter);
    		  $('#endTime_filter').val(condition.endTime_filter);
    		  $('#sign_no').val(condition.sign_no);
    	  }
      };
      loadCondition();
} );
