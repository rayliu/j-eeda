
$(document).ready(function() {
	var cName = [];
	var returnIds = [];
    var miscOrderIds =[];
    $('#menu_charge').addClass('active').find('ul').addClass('in');
    $('#saveBtn').attr('disabled', true);
	//datatable, 动态处理
    var uncheckedChargeCheckTable = $('#uncheckedChargeCheck-table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
    	"bSort": true, // 不要排序
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10 ,25 ,50 ,100 ,9999999], [10 ,25 ,50 ,100, "All"] ],
        "bServerSide": false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('order_ty', aData.ORDER_TY);
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
		"fnDrawCallback": function( settings ) {
	           $('#searchBtn').attr('disabled',false);
	           $('#searchBtn').text('查询');
	    },
        	//"sAjaxSource": "/chargeCheckOrder/createList?flag=new",
        "aoColumns": [
	          { "mDataProp": null, "sWidth":"20px","bSortable": false,
	            "fnRender": function(obj) {
	            	console.log(obj.aData.tporder+"  "+obj.aData.OFFICE_ID);
	             var strcheck= '<input type="checkbox" name="order_check_box" tporder="'+obj.aData.TPORDER+'"  data-office_id="'+obj.aData.OFFICE_ID+'"  class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
	              //回单
	              for(var i=0;i<returnIds.length;i++){
                         if(returnIds[i]==obj.aData.ID){
                        	 return strcheck= '<input type="checkbox" checked="checked" name="order_check_box" tporder="'+obj.aData.TPORDER+'" data-office_id="'+obj.aData.OFFICE_ID+'" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
                         }
                     }
                  //手工单
                  for(var i=0;i<miscOrderIds.length;i++){
                         if(miscOrderIds[i]==obj.aData.ID){
                        	 return strcheck= '<input type="checkbox" checked="checked" name="order_check_box" tporder="'+obj.aData.TPORDER+'" data-office_id="'+obj.aData.OFFICE_ID+'" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
                         }
                     }
                	 return strcheck;
	            }
	          },
	          {"mDataProp":"ID", "bVisible": false},
	          {"mDataProp":"ORDER_NO","sClass": "order_no",
	        	  "fnRender": function(obj) {
	        		  if(Return.isUpdate || Return.isComplete){
	        			  return "<a href='/returnOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
	        		  }else{
	        			  return obj.aData.ORDER_NO;
	        		  }
	      	  }},
	      		{"mDataProp":null, "sWidth":"120px",
	                "fnRender": function(obj) {
	                    return "未收款";
	          }},
	          {"mDataProp":"CHARGE_TOTAL_AMOUNT", "sWidth":"150px"},
	          {"mDataProp":"OFFICE_NAME", "sWidth":"150px"},
	          {"mDataProp":"CNAME","sClass": "cname", "sWidth":"200px"},
	          {"mDataProp":"SERIAL_NO","sClass": "cname", "sWidth":"100px"},
	          {"mDataProp":"SP","sClass": "sp", "sWidth":"200px"},
	          {"mDataProp":"PLANNING_TIME", "sWidth":"150px"},
	          {"mDataProp":"ADDRESS", "sWidth":"200px"},
	          {"mDataProp":null, "sWidth":"150px"},
	          {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"200px"},
	          {"mDataProp":"DELIVERY_ORDER_NO", "sWidth":"200px"},
	          {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"200px"},        	
	          {"mDataProp":null, "sWidth": "120px", 
	              "fnRender": function(obj) {
	                  if(obj.aData.TRANSACTION_STATUS=='new'){
	                      return '新建';
	                  }else if(obj.aData.TRANSACTION_STATUS=='checking'){
	                      return '已发送对帐';
	                  }else if(obj.aData.TRANSACTION_STATUS=='confirmed'){
	                      return '已审核';
	                  }else if(obj.aData.TRANSACTION_STATUS=='completed'){
	                      return '已结算';
	                  }else if(obj.aData.TRANSACTION_STATUS=='cancel'){
	                      return '取消';
	                  }
	                  return obj.aData.TRANSACTION_STATUS;
	              }
	          },           
	          {"mDataProp":"RECEIPT_DATE", "sWidth":"150px"},          	
	          {"mDataProp":"ROUTE_FROM", "sWidth":"100px"},                        
	          {"mDataProp":"ROUTE_TO", "sWidth":"100px"},                     
	          {"mDataProp":"CONTRACT_AMOUNT", "sWidth":"150px"},                        
	          //{"mDataProp":"PICKUP_AMOUNT", "sWidth":"100px"},                        
	          {"mDataProp":null, "sWidth":"100px"},                        
	          {"mDataProp":"SEND_AMOUNT", "sWidth":"100px"},                        
	          {"mDataProp":"INSURANCE_AMOUNT", "sWidth":"100px"},                        
	          {"mDataProp":"SUPER_MILEAGE_AMOUNT", "sWidth":"100px"},                        
	          {"mDataProp":"STEP_AMOUNT", "sWidth":"100px"},                        
	          {"mDataProp":"INSTALLATION_AMOUNT", "sWidth":"100px"},                        
	          {"mDataProp":null, "sWidth":"150px"},                        
	          {"mDataProp":"WAREHOUSE_AMOUNT", "sWidth":"100px"},                        
	          {"mDataProp":null, "sWidth":"100px"},                        
	          {"mDataProp":null, "sWidth":"100px"},                        
	                                  
	          {"mDataProp":null, "sWidth":"150px"},                        
	          {"mDataProp":null, "sWidth":"150px"},                        
	          {"mDataProp":null, "sWidth":"150px"},                        
	          {"mDataProp":null, "sWidth":"150px"},                        
	          {"mDataProp":null, "sWidth":"200px"}                      
	      ]          
    });
    
    var checkedChargeCheckTable = $('#checkedChargeCheck-table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	//"sAjaxSource": "/chargeCheckOrder/createList2",
    	"aoColumns": [ 
	          {"mDataProp": null},  
	          {"mDataProp":null, "bVisible": false},
	          {"mDataProp":null},
	          {"mDataProp":null, "sWidth":"120px"},
	          {"mDataProp":null, "sWidth":"120px"},
			  {"mDataProp":null, "sWidth":"200px"},
			  {"mDataProp":null, "sWidth":"150px"},
			  {"mDataProp":null, "sWidth":"200px"},
			  {"mDataProp":null, "sWidth":"200px"},
			  {"mDataProp":null, "sWidth":"200px"},
			  {"mDataProp":null, "sWidth":"200px"},        	
			  {"mDataProp":null, "sWidth": "120px"},           
			  {"mDataProp":null, "sWidth":"150px"},        	
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"}, 
			  {"mDataProp":null, "sWidth":"200px"}                      
		]          
    });
	//
    $("input[name='allCheck']").click(function(){
    	var office_ids=[];
    	$("#uncheckedChargeCheckList input[name='order_check_box']").each(function () {
    		var now=$(this);
			office_ids.push(now.data("office_id"));
			var end = false;
			for(var i=0;i<office_ids.length;i++){
				if(i!=0){
					if(office_ids[i]!=office_ids[i-1]){
						alert("请选择相同 的网点的单据!!!");
						end=true;
						$("input:checkbox[name=order_check_box]:checked").each(function (){
							this.checked = false;
						});
						$("input[name=allCheck]").attr("checked",false);
						return false;
					}
				}
			}
			if(end){
				end=false;
				return;
			}
    		var cname = $(this).parent().siblings('.cname')[0].textContent;
    			if(cName.length != 0){
						if(cName[0]!=$(this).parent().siblings('.cname')[0].innerHTML){
							alert("请选择同一客户名称的回单");
							return false;
						}
					}
		    if($("#allCheck").prop("checked")){
		    	this.checked = true;
		    }else{
				this.checked = false;
		    }
			if($(this).prop("checked") == true){
				$(this).parent().parent().clone().appendTo($("#checkedChargeCheckList"));
				cName.push($(this).parent().siblings('.cname')[0].innerHTML);
				if($(this).attr('tporder') == "收入单"){
					miscOrderIds.push($(this).val());
					$("#checkedMiscOrder").val(miscOrderIds);
				}else{
					returnIds.push($(this).val());
					$("#checkedReturnOrder").val(returnIds);
				}
				$('#saveBtn').attr('disabled', false);
			}else{
			cName.splice($.inArray($(this).parent().siblings('.cname')[0].innerHTML, cName), 1);
			var id=$(this)[0].value;
			var rows = $("#checkedChargeCheckList").children();
			for(var i=0; i<rows.length;i++){
				var row = rows[i];
				if(id==$(row).find('input').attr('value')){
					row.remove();
					//$("#checkedCostCheckList").children().splice(i,1);
				}
			}
			if($(this).attr('tporder') == "收入单"){
				if(miscOrderIds.length != 0){
					miscOrderIds.splice($.inArray($(this).val(), miscOrderIds), 1);
					$("#checkedMiscOrder").val(miscOrderIds);
				}
			}else{
				if(returnIds.length != 0){
					returnIds.splice($.inArray($(this).val(), returnIds), 1);
					$("#checkedReturnOrder").val(returnIds);
				}
			}
			if(returnIds.length == 0 && miscOrderIds.length == 0){
				$('#saveBtn').attr('disabled', true);
			}
		}
         });

	 });
    // 未选中列表
    var end=false;
	$("#uncheckedChargeCheck-table").on('click', '.checkedOrUnchecked', function(e){
		//只能选中相同的office_id的单子
		var now=$(this);
		$("input:checkbox[name=order_check_box]:checked").each(function (){
			if($(this).data("office_id")!=now.data("office_id")){
				now.attr("checked",false);
				alert("请选择相同 的网点的单据!!!");
				end=true;
				return false;
			}
		});
		if(end){
			end=false;
			return;
		}
		if($(this).prop("checked") == true){
		
			
			if(cName.length != 0){
				if(cName[0]!=$(this).parent().siblings('.cname')[0].innerHTML){
					alert("请选择同一客户名称的回单");
					return false;
				}
			}
			$(this).parent().parent().clone().appendTo($("#checkedChargeCheckList"));
			if($(this).parent().siblings('.cname')[0].innerHTML != ''){
				cName.push($(this).parent().siblings('.cname')[0].innerHTML);
			}
			if($(this).attr('tporder') == "收入单"){
				miscOrderIds.push($(this).val());
				$("#checkedMiscOrder").val(miscOrderIds);
			}else{
				returnIds.push($(this).val());
				$("#checkedReturnOrder").val(returnIds);
			}
			$('#saveBtn').attr('disabled', false);
		}
		else{
			cName.splice($.inArray($(this).parent().siblings('.cname')[0].innerHTML, cName), 1);
			var id=$(this)[0].value;
			var rows = $("#checkedChargeCheckList").children();
			for(var i=0; i<rows.length;i++){
				var row = rows[i];
				if(id==$(row).find('input').attr('value')){
					row.remove();
					//$("#checkedCostCheckList").children().splice(i,1);
				}
			}
			if($(this).attr('tporder') == "收入单"){
				if(miscOrderIds.length != 0){
					miscOrderIds.splice($.inArray($(this).val(), miscOrderIds), 1);
					$("#checkedMiscOrder").val(miscOrderIds);
				}
			}else{
				if(returnIds.length != 0){
					returnIds.splice($.inArray($(this).val(), returnIds), 1);
					$("#checkedReturnOrder").val(returnIds);
				}
			}
			if(returnIds.length == 0 && miscOrderIds.length == 0){
				$('#saveBtn').attr('disabled', true);
			}
		}
	});
	//刷新列表
	$("#uncheckedChargeCheckOrder").click(function(){
		uncheckedChargeCheckTable.fnDraw();
	});
	// 已选中列表
	$("#checkedChargeCheck-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == false){
			$(this).parent().parent().appendTo($("#uncheckedChargeCheckList"));
			cName.splice($.inArray($(this).parent().siblings('.cname')[0].innerHTML, cName), 1);
			if($(this).attr('tporder') == "收入单"){
				if(miscOrderIds.length != 0){
					miscOrderIds.splice($.inArray($(this).val(), miscOrderIds), 1);
					$("#checkedMiscOrder").val(miscOrderIds);
				}
			}else{
				if(returnIds.length != 0){
					returnIds.splice($.inArray($(this).val(), returnIds), 1);
					$("#checkedReturnOrder").val(returnIds);
				}
			}
			if(returnIds.length == 0 && miscOrderIds.length == 0){
				$('#saveBtn').attr('disabled', true);
			}
		}
	});
	$('#saveBtn').click(function(e){
        e.preventDefault();
        if(returnIds.length>0 || miscOrderIds.length>0){
        	$('#createForm').submit();
        }else{
        	$.scojs_message('对不起，当前你没有选择需要对账的单据', $.scojs_message.TYPE_ERROR);
        }
        
    });
	
	$("#checkedChargeCheckOrder").click(function(){
		$("#checked").show();
	});
	
	$("#create_searchBtn").click(function(){
		refreshCreate();
	});
	
	
	 $("#resetBtn").click(function(){
	        $('#returnOrderSearchForm')[0].reset();
	    });
    var refreshCreate = function(){
    	$("#searchBtn").attr("disabled",true);
    	$("#searchBtn").text("查询中···");
    	var customer = $.trim($('#customer_filter').val());
		var beginTime = $.trim($("#beginTime_filter").val());
		var endTime = $.trim($("#endTime_filter").val());
		var planningBeginTime = $.trim($("#beginTime_filter1").val());
		var planningEndTime = $.trim($("#endTime_filter1").val());
		var orderNo = $.trim($("#orderNo_filter").val());
		var customerNo = $.trim($("#customerNo_filter").val());
		var serialNo2 = $.trim($("#serialNo_filter2").val());
		var address = $.trim($("#address_filter").val());
		var status = $.trim($("#shouru_filter").val());
		$("#allCheck").attr("checked",false);
		
		
//		 var flag = false;
//	        $('#returnOrderSearchForm input,#returnOrderSearchForm select').each(function(){
//	        	 var textValue = $.trim(this.value);
//	        	 if(textValue != '' && textValue != null){
//	        		 flag = true;
//	        		 return;
//	        	 } 
//	        });
//	        if(!flag){
//	        	 $.scojs_message('请输入至少一个查询条件', $.scojs_message.TYPE_FALSE);
//	        	 $("#searchBtn").attr("disabled",false);
//	         	$("#searchBtn").text("查询");
//	        	 return false;
//	        }
		
		uncheckedChargeCheckTable.fnSettings().oFeatures.bServerSide = true;
		uncheckedChargeCheckTable.fnSettings().sAjaxSource = "/chargeCheckOrder/createList?customer="+customer
															+"&beginTime="+beginTime
															+"&endTime="+endTime
															+"&orderNo="+orderNo
															+"&customerNo="+customerNo
															+"&serialNo2="+serialNo2
															+"&address="+address
															+"&planningBeginTime="+planningBeginTime
															+"&planningEndTime="+planningEndTime
															+"&status="+status;
		uncheckedChargeCheckTable.fnDraw();
    };
 
    //获取客户的list，选中信息自动填写其他信息
    $('#customer_filter').on('keyup click', function(){
        var inputStr = $('#customer_filter').val();
        
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
           // console.log(data);
            var companyList =$("#companyList");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
            }
            if(data.length>0)
                companyList.show();
            
        },'json');

        
    });    
    // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#companyList').on('blur', function(){
        $('#companyList').hide();
    });

    $('#companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).text());
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId);
    });
    $('#createBtn').click(function(e){
        e.preventDefault();
        //获取选取回单的ID, 放到数组中
        var chk_value =[];    
        $('input[name="order_check_box"]:checked').each(function(){    
           chk_value.push($(this).val());    
        }); 

        var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
                        '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
                        'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
                    '</div>';
        $('body').append(alerMsg);
        $('#message_trigger_err').on('click', function(e) {
            e.preventDefault();
            
        });
        //$(body).append(chk_value.length==0 ?'你还没有选择任何应付单据！':chk_value);

        $('#ids').val(chk_value);
        if(!$('#returnOrderSearchForm').valid()){
            return false;
        }
        if(chk_value.length==0 ){
            $.scojs_message('你还没有勾选任何应收回单.', $.scojs_message.TYPE_ERROR);
            return false;
        }

        $('#createForm').submit();
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
    $('#datetimepicker3').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter1').trigger('keyup');
    });
    $('#datetimepicker4').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter1').trigger('keyup');
    });
    
    
    //批量撤销功能
    $('#deleteAllBtn').on('click',function(e){
    	var self= this;
    	$(self).prop('disabled',true);
    	var jsonj = {};
    	var jsonArray = [];
    	var $checked = [];
    	$('#uncheckedChargeCheck-table input[name="order_check_box"]').each(function(){
    		var check = $(this).prop('checked');
    		if(check){
    			var id = $(this).parent().parent().attr('id');
    			var order_type = $(this).parent().parent().attr('order_ty');
    			var jsonStr = {};
    			jsonStr.id = id;
    			jsonStr.order_type = order_type;
    			jsonArray.push(jsonStr);
    			$checked.push($(this));
    		}
    	})
    	jsonj.jsonArray= jsonArray;
    	if(jsonArray.length<1){
    		$.scojs_message('请勾选您要撤回的单据', $.scojs_message.TYPE_ERROR);
    		$(self).prop('disabled',false);
    		return false;
    	}
    	
    	$.post('/chargeCheckOrder/deleteConfirm',{jsonStr:JSON.stringify(jsonj)},function(data){
    		if(data.success){
    			cName = [];
    			returnIds = [];
    		    miscOrderIds =[];
    		    $('#saveBtn').attr('disabled',true);
    			$.scojs_message('撤回成功', $.scojs_message.TYPE_OK);
    			refreshCreate();
    			
//    			for(var i = 0;i<$checked.length;i++){
//            		$checked[i].parent().parent().hide();
//            	}
    		}else{
    			$.scojs_message('撤回失败', $.scojs_message.TYPE_ERROR);
    		}
    		$(self).prop('disabled',false);
    	})
    })
   
} );