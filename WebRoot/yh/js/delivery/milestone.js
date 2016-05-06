
$(document).ready(function() {
	document.title = '配送在途查询 | '+document.title;
	$('#menu_deliver').addClass('active').find('ul').addClass('in');
	 
	//datatable, 动态处理
    var detailTable = $('#eeda-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "bSort": false,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/delivery/deliveryMilestone",
        "aoColumns": [ 
            { 
                "mDataProp": null, 
                "bVisible":DeliveryOnTrip.isComplete,
                "fnRender": function(obj) {   
                  if(obj.aData.STATUS=="已送达" || obj.aData.STATUS=="已签收"|| obj.aData.STATUS=="已完成"){
                	  return "<a class='btn  btn-danger deleteDelivery' id='deleteBtn' code='"+obj.aData.ID+"'>撤销到达</a>";
                  }else if(obj.aData.STATUS=="已发车" || obj.aData.STATUS=="配送在途"){
                    return "<a class='btn  btn-primary confirmDelivery' id='arriveBtn' code='"+obj.aData.ID+"'>"+
                    "到达确认"+
                    "</a>";
                  }else{
                	  return obj.aData.STATUS;
                  }
                }
            },  
            {"mDataProp":null, "sWidth": "80px",
            	"fnRender": function(obj) {
            		if(Delivery.isUpdate || Delivery.isComplete){
            			return "<a href='/delivery/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		}else{
            			return obj.aData.ORDER_NO;
            		}
                    	
            	}
            },
            {"mDataProp":null,"sWidth":"90px",
                "fnRender": function(obj) {
                	if(obj.aData.LOCATION!=null && obj.aData.LOCATION!=''){
                		if(Delivery.isUpdate){
                			return obj.aData.LOCATION+"<a id='edit_status' del_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                		}else{
                			return obj.aData.LOCATION;
                		}
                		
                	}else{
                		if(obj.aData.STATUS==null){
                    		obj.aData.STATUS="";
                    	}
                    	if(obj.aData.STATUS=='已送达' || obj.aData.STATUS=='已签收'){
                    		//return obj.aData.STATUS;
                    		return "已送达";
                    	}else{
                    		if(Delivery.isUpdate){
                    			return obj.aData.STATUS+"<a id='edit_status' del_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                    		}else{
                    			return obj.aData.STATUS;
                    		}
                    		
                    	
                    	}
                	}
                	
                }
            },
            {"mDataProp":"OFFICE_NAME","sWidth":"70px"},
            {"mDataProp":"CUSTOMER","sWidth":"70px"},
            {"mDataProp":"SERIAL_NO","sWidth":"90px"},
            {"mDataProp":"ITEM_NO","sWidth":"100px"},
            {"mDataProp":"PIECES","sWidth":"70px"},
            {"mDataProp":"C2","sWidth":"70px"},
            {"mDataProp":"CREATE_STAMP","sWidth":"95px",
                "fnRender":function(obj){
                    var create_stamp=obj.aData.CREATE_STAMP;
                    var str=create_stamp.substr(0,10);
                    return str;
                }
            },
            {"mDataProp":"PLANNING_TIME","sWidth":"95px"},
            {"mDataProp":"TRANSFER_ORDER_NO"}          
            
        ]  
    });	
    //签收完成
    $("#eeda-table").on('click', '.confirmDelivery', function(e){
    	var delivery_id =$(this).attr("code");
    	var $text = $(this).parent();
    	var status = $(this).parent().parent().find('td')[2];
    	$text.find('#arriveBtn').attr('disabled',true);
    	if(confirm("到达确认 吗？")){
    		$.post('/deliveryOrderMilestone/receipt',{delivery_id:delivery_id},function(data){
    			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
    			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
    			//detailTable.fnDraw(); 
    			$(status).html('已送达');
    			$text.html("<a class='btn  btn-danger deleteDelivery' id='deleteBtn' code='"+delivery_id+"'>撤销到达</a>");
    			
    		},'json');
        }else{
        	$text.find('#arriveBtn').attr('disabled',false);
        }
    });
    
    //撤销到达
    $("#eeda-table").on('click', '.deleteDelivery', function(e){
    	var delivery_id =$(this).attr("code");
    	var $text = $(this).parent();
    	var status = $(this).parent().parent().find('td')[2];
    	$text.find('#deleteBtn').attr('disabled',true);
    	if(confirm("确定撤销到达吗？")){
    		$.post('/deliveryOrderMilestone/deleteReceipt',{delivery_id:delivery_id},function(data){
    			if(data.success){
    				$.scojs_message('撤销成功', $.scojs_message.TYPE_OK);
        			$(status).html('配送在途');
        			$text.html("<a class='btn  btn-primary confirmDelivery' id='arriveBtn' code='"+delivery_id+"'>到达确认</a>");
    			}else{
    				$.scojs_message('撤销失败，存在下级财务单据(或配送已发车)，不可撤销', $.scojs_message.TYPE_FALSE);
    			}
    		},'json');
        }else{
        	$text.find('#deleteBtn').attr('disabled',false);
        }
    });
    
    $('#milestone_table').dataTable({
    	  "bSort": false, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
        "bProcessing":false,
        "bInfo":false,
        "bPaginate":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "aoColumns": [
            { "mDataProp":null},
            { "mDataProp":null},
            { "mDataProp":null},
            { "mDataProp":null}, 
            { "mDataProp":null},
            { "mDataProp":null},
            { "mDataProp":null},
            { "mDataProp":null},
            { "mDataProp":null},
            { "mDataProp":null}
        ]      
    });	
    $("#eeda-table").on('click', '#edit_status', function(e){
    	e.preventDefault();	
    	var depart_id=$(this).attr("del_id");
    	$("#milestoneDepartId").val(depart_id);
    	$.post('/delivery/transferOrderMilestoneList',{departOrderId:depart_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.empty();
			for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
			{
				var arrival_time=data.transferOrderMilestones[i].ARRIVAL_TIME;
				var performance=data.transferOrderMilestones[i].PERFORMANCE;
				var ufr=data.transferOrderMilestones[i].UNFINISHED_REASON;
				var fs=data.transferOrderMilestones[i].FINISHED_SERAL;
				var ur=data.transferOrderMilestones[i].UNSERAL_REASON;
				var uh=data.transferOrderMilestones[i].UNUSUAL_HANDLE;
				var location = data.transferOrderMilestones[i].LOCATION;
				if(location == null){
					location = "";
				}
				if(arrival_time==null){
					arrival_time="";
				}
				if(performance==null){
					performance="";
				}
				if(ufr==null){
					ufr="";
				}
				if(fs==null){
					fs="";
				}
				if(ur==null){
					ur="";
				}
				if(uh==null){
					uh="";
				}
				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS
													+"</th><th>"+location
													+"</th><th>"+data.usernames[j]
													+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP
													+"</th><th>"+arrival_time
													+"</th><th>"+performance
													+"</th><th>"+ufr
													+"</th><th>"+fs
													+"</th><th>"+ur
													+"</th><th>"+uh
													+"</th></tr>");
			}
			
		},'json');
    	
    	
    });
    $('#transferOrderMilestoneForm').validate({
        rules: {
        	location: {
               required: true
			},
			arrival_filter: { 
           	  	required: true 
           	  } 
            }
       });
    // 保存新里程碑
	$("#transferOrderMilestoneFormBtn").click(function(){
		if(!$("#transferOrderMilestoneForm").valid()){
	       	return false;
        }
		$.post('/delivery/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS
											+"</th><th>"+data.transferOrderMilestone.LOCATION
											+"</th><th>"+data.username
											+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP
											+"</th><th>"+data.transferOrderMilestone.ARRIVAL_TIME
											+"</th><th>"+data.transferOrderMilestone.PERFORMANCE
											+"</th><th>"+data.transferOrderMilestone.UNFINISHED_REASON
											+"</th><th>"+data.transferOrderMilestone.FINISHED_SERAL
											+"</th><th>"+data.transferOrderMilestone.UNSERAL_REASON
											+"</th><th>"+data.transferOrderMilestone.UNUSUAL_HANDLE
											+"</th></tr>");
			detailTable.fnSettings().sAjaxSource = "/delivery/deliveryMilestone";
			detailTable.fnDraw();  
		},'json');
		$("#location").val("");
    	$("#arrival_filter").val("");
    	$("#deliveryException").val("");
    	$("#completeRemark").val("");
    	$("#remark").val("");
    	$("#completeOK").attr("check","checked");
    	$("#sealOK").attr("check","checked");
	
		//$('#transferOrderMilestone').modal('hide');
	}); 

	// 获取所有网点
	$.post('/officeConfig/searchAllOffice',function(data){
		if(data.length > 0){
			 //console.log(data);
			var deliveryOffice = $("#deliveryOffice");
			deliveryOffice.empty();
			deliveryOffice.append("<option ></option>");	
			for(var i=0; i<data.length; i++){
				deliveryOffice.append("<option office_id='"+data[i].ID+"' value='"+data[i].OFFICE_NAME+"'>"+data[i].OFFICE_NAME+"</option>"); 
				};
			};
	},'json');
    $("#searchBtn").click(function(){
        refreshData();
    });

    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
    });
    
    var refreshData=function () {
      var deliveryNo = $("#deliveryNo_filter").val();
      var customer = $("#customer_filter").val();
      var transferorderNo = $("#transferorderNo_filter").val();
      var sp = $("#sp_filter").val();
      var beginTime = $("#beginTime_filter").val();
      var endTime = $("#endTime_filter").val();
      var status  = $("#status").val();
      var serial_no  = $("#serial_no").val();
      var deliveryOffice  = $("#deliveryOffice").val();
      detailTable.fnSettings().oFeatures.bServerSide = true;
      detailTable.fnSettings().sAjaxSource = "/delivery/deliveryMilestone?deliveryNo="+deliveryNo+"&customer="+customer+"&transferorderNo="+transferorderNo+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&status="+status+"&serial_no="+serial_no+"&deliveryOffice="+deliveryOffice;
      detailTable.fnDraw();
    }
	
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
   /* var date = new Date();
    
    $("#arrival_filter").val(date.getFullYear()+"-"+date.getDate()+"-"+date.getDay());*/
    $('#datetimepickerArrival').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#arrival_filter').trigger('keyup');
    });
    $("#sealNO").on("click",function(){    	
    	$("#remark").show();
    });
    $("#sealOK").on("click",function(){    	
    	$("#remark").hide();
    });
    /*----------------------------------------------------------------*/
    
    
    //获取所有客户
    $('#customer_filter').on('keyup click', function(){
           var inputStr = $('#customer_filter').val();
           
           $.get("/customerContract/search", {locationName:inputStr}, function(data){
               console.log(data);
               var companyList =$("#companyList");
               companyList.empty();
               for(var i = 0; i < data.length; i++)
               {
                   companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
               }
               if(data.length>0)
                   companyList.show();
               
           },'json');

           if(inputStr==''){
        	   detailTable.fnFilter('', 2);
           }
           
       });
   //选中某个客户时候
      $('#companyList').on('click', '.fromLocationItem', function(e){        
           $('#customer_filter').val($(this).text());
           $("#companyList").hide();
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
       
       
     //获取供应商的list，选中信息在下方展示其他信息
       $('#sp_filter').on('input click', function(){
       	   var me = this;
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
       		//console.log($('#spList').is(":focus"));
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
       	
	        
} );