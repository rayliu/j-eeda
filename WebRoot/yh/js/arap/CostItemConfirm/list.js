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
        "aLengthMenu": [ [10, 25, 50, 9999999], [10, 25, 50, "All"] ],
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
                    if(obj.aData.CHANGE_AMOUNT!=''&& obj.aData.CHANGE_AMOUNT != null){
                        return "<input type='text' style='width:60px' name='change_amount' id='change' value='"+obj.aData.CHANGE_AMOUNT+"'/>";
                        
                    }
                    else {
                      if(obj.aData.PAY_AMOUNT!=null){
                        return "<input type='text' style='width:60px' name='change_amount' value='"+obj.aData.PAY_AMOUNT+"'/>";
                      }
                      else{
                        return "<input type='text' style='width:60px' name='change_amount' value='0'/>";
                      }
                    }
                }
            },
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
                {"mDataProp":"SERIAL_NO", "sWidth":"100px"},
                {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"100px"},
            	{"mDataProp":"ORDER_NO", "sWidth":"140px", 
                    "fnRender": function(obj) {
                    	return eeda.getUrlByNo(obj.aData.ID, obj.aData.ORDER_NO);
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
    	var idArr=[];
    	var orderNoArr=[];    	
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		idArr.push($(this).attr('id'));
        		orderNoArr.push($(this).attr('order_no'));
        	}
        });     
        console.log(idArr);
        var ids = idArr.join(",");
        var orderNos = orderNoArr.join(",");
        $.post("/costConfirmList/costConfiremReturnOrder", {ids:ids, orderNos:orderNos}, function(data){
        	if(data.success){
        		refreshData();
        	}
        },'json');
    });
    
    /*--------------------------------------------------------------------*/
    //获取所有客户
   /* $('#customer_filter').on('keyup click', function(){
           var inputStr = $('#customer_filter').val();
           
           $.get("/customerContract/search", {locationName:inputStr}, function(data){
               //console.log(data);
               var companyList =$("#companyList");
               companyList.empty();
               for(var i = 0; i < data.length; i++)
               {
                   companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
               }
               if(data.length>0)
                   companyList.show();
               
           },'json');

          
       });*/
    $('#customer_name').on('keyup click', function(){
        var inputStr = $('#customer_name').val();
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
            //console.log(data);
            var companyList =$("#companyList");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' >"+data[i].ABBR+"</a></li>");
            }
            if(data.length>0)
                companyList.show();
            
        },'json');

       
    });



   //选中某个客户时候
      $('#companyList').on('click', '.fromLocationItem', function(e){        
           $('#customer_name').val($(this).text());
           $("#companyList").hide();
           var companyId = $(this).attr('partyId');
           $('#customerId').val(companyId);
          
       });
    // 没选中客户，焦点离开，隐藏列表
       $('#customer_name').on('blur', function(){
           $('#companyList').hide();
       });

       //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
       $('#companyList').on('blur', function(){
           $('#companyList').hide();
       });

       $('#companyList').on('mousedown', function(){
           return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
       });
       
     //获取供应商的list，选中信息在下方展示其他信息
       $('#sp_filter').on('input click', function(){
       		var me= this;
       		var inputStr = $('#sp_filter').val();
       		if(inputStr == ""){
       			var pageSpName = $("#pageSpName");
       			pageSpName.empty();
       			var pageSpAddress = $("#pageSpAddress");
       			pageSpAddress.empty();
       			$('#sp_id').val($(this).attr(''));
       		}
       		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
       			if(inputStr!=$('#sp_filter').val()){//查询条件与当前输入值不相等，返回
					return;
				}
       			var spList =$("#spList");
       			spList.empty();
       			for(var i = 0; i < data.length; i++)
       			{
       				var abbr = data[i].ABBR;
    				if(abbr == null){
    					abbr = '';
    				}
    				var company_name = data[i].COMPANY_NAME;
    				if(company_name == null){
    					company_name = '';
    				}
    				var contact_person = data[i].CONTACT_PERSON;
    				if(contact_person == null){
    					contact_person = '';
    				}
    				var phone = data[i].PHONE;
    				if(phone == null){
    					phone = '';
    				}
    				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
    			}
    		   $("#spList").css({ 
	               	left:$(me).position().left+"px", 
	               	top:$(me).position().top+32+"px" 
               }); 
               $('#spList').show();
       		},'json');

       		
       	});

       	// 没选中供应商，焦点离开，隐藏列表
       	$('#sp_filter').on('blur', function(){
        		$('#spList').hide();
        	});

       	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
       	$('#spList').on('blur', function(){
        		$('#spList').hide();
        	});

       	$('#spList').on('mousedown', function(){
       		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
       	});

       	// 选中供应商
       	$('#spList').on('mousedown', '.fromLocationItem', function(e){
       		var message = $(this).text();
       		$('#sp_filter').val(message.substring(0, message.indexOf(" ")));
       		$('#sp_id').val($(this).attr('partyId'));
       		var pageSpName = $("#pageSpName");
       		pageSpName.empty();
       		var pageSpAddress = $("#pageSpAddress");
       		pageSpAddress.empty();
       		pageSpAddress.append($(this).attr('address'));
       		var contact_person = $(this).attr('contact_person');
       		if(contact_person == 'null'){
       			contact_person = '';
       		}
       		pageSpName.append(contact_person+'&nbsp;');
       		var phone = $(this).attr('phone');
       		if(phone == 'null'){
       			phone = '';
       		}
       		pageSpName.append(phone); 
       		pageSpAddress.empty();
       		var address = $(this).attr('address');
       		if(address == 'null'){
       			address = '';
       		}
       		pageSpAddress.append(address);
               $('#spList').hide();
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
    		var sp = $("#sp_filter").val();
    		var no = $("#operation_number").val();
    		var beginTime = $("#beginTime_filter").val();
    		var endTime = $("#endTime_filter").val();
    		var status = $("#order_status_filter").val();
    		var type = $("#order_type_filter").val();
    		var booking_note_number = $("#booking_note_number").val();
    		var route_from =$("#route_from").val();
    		var route_to = $("#route_to").val();
    		var customer_name = $("#customer_name").val();
    		var plantime = $("#plantime").val();
    		var arrivaltime = $("#arrivaltime").val();
    		var serial_no = $("#serial_no").val();
    		var sign_no = $("#sign_no").val();

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
							                            +"&customer_name="+customer_name
    													+"&plantime="+plantime
    													+"&arrivaltime="+arrivaltime
    													+"&serial_no="+serial_no
    													+"&sign_no="+sign_no;
    	
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
} );
