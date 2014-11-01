$(document).ready(function() {
	$('#menu_deliver').addClass('active').find('ul').addClass('in');
	 var dataTable =$('#eeda-table3').dataTable({
		 "bFilter": false, //不需要默认的搜索框
	        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	        "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/delivery/deliveryList",
	        "aoColumns": [   
	            
	            {"mDataProp":"ORDER_NO",
	            	"fnRender": function(obj) {
         			return "<a href='/delivery/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
         		}
	            },
	            {"mDataProp":"CUSTOMER"},
	            {"mDataProp":"C2"},
	            {"mDataProp":"CREATE_STAMP"},
	            {"mDataProp":"STATUS"},
	            {"mDataProp":"TRANSFER_ORDER_NO"},
	            { 
	                "mDataProp": null, 
	                "sWidth": "5%",                
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-danger cancelbutton' title='取消单据' code='"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                            "</a>";
	                }
	            }                         
	        ]      
	    });	
	    
		 $("#eeda-table3").on('click', '.cancelbutton', function(e){
			  e.preventDefault();
	         //异步向后台提交数据
			  var r=confirm("是否取消单据！");   
           if(r==true){
         	  var id = $(this).attr('code');
				$.post('/delivery/cancel/'+id,function(data){
		                 //保存成功后，刷新列表
		                 console.log(data);
		                 if(data.success){
		                	 dataTable.fnDraw();
		                 }else{
		                     alert('取消失败');
		                 }
		             },'json');
			}else{
				return false;   
			}
		});

	$('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $('#beginTime_filter').trigger('keyup');
    });


    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $('#endTime_filter').trigger('keyup');
    });
	
  //条件筛选
	$("#orderNo_filter ,#transfer_filter ,#status_filter,#customer_filter,#sp_filter,#beginTime_filter,#endTime_filter,#warehouse").on('keyup click', function () {    	 	
      	var orderNo_filter = $("#orderNo_filter").val();
      	var transfer_filter = $("#transfer_filter").val();
    	var status_filter = $("#status_filter").val();
      	var customer_filter = $("#customer_filter").val();    	
      	var sp_filter = $("#sp_filter").val();
      	var beginTime_filter = $("#beginTime_filter").val();
      	var endTime_filter = $("#endTime_filter").val();
      	var warehouse = $("#warehouse").val();
      	dataTable.fnSettings().sAjaxSource = "/delivery/deliveryList?orderNo_filter="+orderNo_filter+"&transfer_filter="+transfer_filter+"&status_filter="+status_filter+"&customer_filter="+customer_filter+"&sp_filter="+sp_filter+"&beginTime_filter="+beginTime_filter+"&endTime_filter="+endTime_filter+"&warehouse="+warehouse;
      	dataTable.fnDraw();
      });
	
	/*===========================================================*/
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
	        	dataTable.fnFilter('', 2);
	        }
	        
	    });


	//选中某个客户时候
	   $('#companyList').on('click', '.fromLocationItem', function(e){        
	        $('#customer_filter').val($(this).text());
	        $("#companyList").hide();
	       /* var companyId = $(this).attr('partyId');
	        $('#customerId').val(companyId);*/
	        //过滤回单列表
	        //chargeCheckTable.fnFilter(companyId, 2);
	        var inputStr = $('#customer_filter').val();
	        if(inputStr!=null){
	        	var orderNo_filter = $("#orderNo_filter").val();
	          	var transfer_filter = $("#transfer_filter").val();
	        	var status_filter = $("#status_filter").val();
	          	var customer_filter = $("#customer_filter").val();    	
	          	var sp_filter = $("#sp_filter").val();
	          	var beginTime_filter = $("#beginTime_filter").val();
	          	var endTime_filter = $("#endTime_filter").val();
	          	var warehouse = $("#warehouse").val();
	          	dataTable.fnSettings().sAjaxSource = "/delivery/deliveryList?orderNo_filter="+orderNo_filter+"&transfer_filter="+transfer_filter+"&status_filter="+status_filter+"&customer_filter="+customer_filter+"&sp_filter="+sp_filter+"&beginTime_filter="+beginTime_filter+"&endTime_filter="+endTime_filter+"&warehouse="+warehouse;
	          	dataTable.fnDraw();
	        }
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
	    			console.log(data);
	    			var spList =$("#spList");
	    			spList.empty();
	    			for(var i = 0; i < data.length; i++)
	    			{
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
	    				spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+" "+contact_person+" "+phone+"</a></li>");
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
	    		console.log($('#spList').is(":focus"))
	    		var message = $(this).text();
	    		$('#sp_filter').val(message.substring(0, message.indexOf(" ")));
	    		$('#sp_id').val($(this).attr('partyId'));
	    		var pageSpName = $("#sp_filter");
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
	            
	            var orderNo_filter = $("#orderNo_filter").val();
	          	var transfer_filter = $("#transfer_filter").val();
	        	var status_filter = $("#status_filter").val();
	          	var customer_filter = $("#customer_filter").val();    	
	          	var sp_filter = $("#sp_filter").val();
	          	var beginTime_filter = $("#beginTime_filter").val();
	          	var endTime_filter = $("#endTime_filter").val();
	          	var warehouse = $("#warehouse").val();
	          	dataTable.fnSettings().sAjaxSource = "/delivery/deliveryList?orderNo_filter="+orderNo_filter+"&transfer_filter="+transfer_filter+"&status_filter="+status_filter+"&customer_filter="+customer_filter+"&sp_filter="+sp_filter+"&beginTime_filter="+beginTime_filter+"&endTime_filter="+endTime_filter+"&warehouse="+warehouse;
	          	dataTable.fnDraw();
	        });
});