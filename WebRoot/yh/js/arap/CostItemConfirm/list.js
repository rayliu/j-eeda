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
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costConfirmList/list",
        "aoColumns": [ 
            { "mDataProp": null, "sWidth":"10px",
                "fnRender": function(obj) {
	              return '<input type="checkbox" name="order_check_box" id="'+obj.aData.ID+'" order_no="'+obj.aData.BUSINESS_TYPE+'">';	
                }
            },
            {"mDataProp":"BUSINESS_TYPE", "sWidth":"75px"},            	
            {"mDataProp":"SPNAME", "sWidth":"160px"},
            {"mDataProp":null, "sWidth": "70px", 
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
            {"mDataProp":"RETURN_ORDER_COLLECTION", "sWidth":"75px"},  
		    {"mDataProp":null, "sWidth":"75px",
                "fnRender": function(obj) {
                    return "未确认";
            }},
            {"mDataProp":"ORDER_NO", "sWidth":"140px", 
                "fnRender": function(obj) {
                	var str = "";
                    if(obj.aData.ORDER_NO.indexOf("PS") > -1){
                        str = "<a href='/delivery/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else if(obj.aData.ORDER_NO.indexOf("PC") > -1){
                        str = "<a href='/pickupOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else if(obj.aData.ORDER_NO.indexOf("FC") > -1){
                        str = "<a href='/departOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else {
                        str = "<a href='/insuranceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }
                    return str;
                }
            },            	
            {"mDataProp":"AMOUNT", "sWidth":"35px"},                        
            {"mDataProp":"VOLUME", "sWidth":"35px"},                        
            {"mDataProp":"WEIGHT", "sWidth":"40px"}, 
            {"mDataProp":null, "sWidth":"50px",
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
            {"mDataProp":"OTHER_COST", "sWidth":"60px"}, 
            {"mDataProp":"PAY_AMOUNT", "sWidth":"50px"},
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"140px"},
            {"mDataProp":"CREATE_STAMP", "sWidth":"100px"}, 
            {"mDataProp":"OFFICE_NAME", "sWidth":"80px"},                       
            {"mDataProp":"REMARK", "sWidth":"150px"}                         
        ]      
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
        		costConfiremTable.fnSettings().sAjaxSource = "/costConfirmList/list";
        		costConfiremTable.fnDraw(); 
        	}
        },'json');
    });
    
    /*--------------------------------------------------------------------*/
    //获取所有客户
    $('#customer_filter').on('keyup click', function(){
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

          
       });



   //选中某个客户时候
      $('#companyList').on('click', '.fromLocationItem', function(e){        
           $('#customer_filter').val($(this).text());
           $("#companyList").hide();
           var companyId = $(this).attr('partyId');
           $('#customerId').val(companyId);
           refreshData();
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
       $('#sp_filter').on('keyup click', function(){
       		var inputStr = $('#sp_filter').val();
       		if(inputStr == ""){
       			var pageSpName = $("#pageSpName");
       			pageSpName.empty();
       			var pageSpAddress = $("#pageSpAddress");
       			pageSpAddress.empty();
       			$('#sp_id').val($(this).attr(''));
       		}
       		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
       			//console.log(data);
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
       		},'json');

       		$("#spList").css({ 
               	left:$(this).position().left+"px", 
               	top:$(this).position().top+32+"px" 
               }); 
               $('#spList').show();
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
               refreshData();
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
    		var no = $("#customer_filter").val();
    		var beginTime = $("#beginTime_filter").val();
    		var endTime = $("#endTime_filter").val();
    		var status = $("#order_status_filter").val();
    		var type = $("#order_type_filter").val();
    		
    		costConfiremTable.fnSettings().sAjaxSource = "/costConfirmList/list?orderNo="+orderNo
											    		+"&sp="+sp
											    		+"&no="+no
											    		+"&beginTime="+beginTime
											    		+"&endTime="+endTime
											    		+"&status="+status
											    		+"&type="+type;
    	
    		costConfiremTable.fnDraw();
       };
       /*=====================条件过滤=======================*/
        //过滤客户
        $('#orderNo_filter,#sp_filter,#customer_filter,#beginTime_filter,#endTime_filter').on( 'keyup', function () {
        	refreshData();
     	} );
        $('#order_type_filter,#order_status_filter').on( 'change', function () {
        	refreshData();
     	} );
} );
