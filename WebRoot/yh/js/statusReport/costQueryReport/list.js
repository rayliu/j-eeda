$(document).ready(function() {
	document.title = '应付业务单据查询| '+document.title;
    $('#menu_report').addClass('active').find('ul').addClass('in');
   
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
        "aoColumns": [
            {"mDataProp":"BUSINESS_TYPE", "sWidth":"110px"},
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
            {"mDataProp":"REF_NO", "sWidth":"130px"},
            {"mDataProp":"PAY_AMOUNT", "sWidth":"100px"},
            {"mDataProp":"CHANGE_AMOUNT","sWidth":"130px"},
            {"mDataProp":"AUDIT_STATUS", "sWidth":"260px"},
            {"mDataProp":"SERIAL_NO", "sWidth":"100px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"130px"},
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
            {"mDataProp":"OFFICE_NAME", "sWidth":"80px"},
            {"mDataProp":"REMARK", "sWidth":"150px"}                         
        ]      
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
        
       var refreshData = function(){
        	var orderNo = $('#orderNo_filter').val();
    		var sp = $("#sp_id_input").val();
    		var no = $("#operation_number").val();
    		var car_out_time_begin_time = $("#car_out_time_begin_time").val();
    		var car_out_time_end_time = $("#car_out_time_end_time").val();
    		var status = $("#order_status_filter").val();
    		var type = $("#order_type_filter").val();
    		var booking_note_number = $("#booking_note_number").val();
    		var route_from =$("#route_from").val();
    		var route_to = $("#route_to").val();
    		var customer_name = $("#customer_id_input").val();
    		var plantime_begin_time = $("#plantime_begin_time").val();
    		var plantime_end_time = $("#plantime_end_time").val();
    		var serial_no = $("#serial_no").val();
    		var sign_no = $("#sign_no").val();

            costConfiremTable.fnSettings().oFeatures.bServerSide = true;
    		costConfiremTable.fnSettings().sAjaxSource = "/statusReport/costQueryReportSearch?orderNo="+orderNo
                              							+"&sp="+sp
											    		+"&no="+no
											    		+"&beginTime="+car_out_time_begin_time
											    		+"&endTime="+car_out_time_end_time
											    		+"&status="+status
											    		+"&type="+type
											    		+"&booking_note_number="+booking_note_number
							                            +"&route_from="+route_from
							                            +"&route_to="+route_to
							                            +"&customer_name="+customer_name
    													+"&plantime="+plantime_begin_time
    													+"&arrivaltime="+plantime_end_time
    													+"&serial_no="+serial_no
    													+"&sign_no="+sign_no;
    	
    		  costConfiremTable.fnDraw();
       };
       
       /*=====================条件过滤=======================*/
      $('#searchButton').click(function(){
          refreshData();
      });
      
} );
