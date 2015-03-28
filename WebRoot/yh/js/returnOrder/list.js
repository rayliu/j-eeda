 $(document).ready(function() {
	$('#menu_return').addClass('active').find('ul').addClass('in');
	
	var dataTable =$('#example').dataTable( {
    	"bSort": false, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/returnOrder/list",
   			"aoColumns": [
   			{ "mDataProp": "ORDER_NO",
            	"fnRender": function(obj) {
            		if(Return.isUpdate || Return.isComplete){
            			return "<a href='/returnOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		}else{
            			return obj.aData.ORDER_NO;
            		}
            		
        			
        		}},
            { "mDataProp": "CNAME","sWidth":"120px"},
            { "mDataProp": "TRANSFER_ORDER_NO"},
            { "mDataProp": "DELIVERY_ORDER_NO"},
            { "mDataProp": "CREATOR_NAME","sWidth":"120px" },
            { "mDataProp": "CREATE_DATE","sWidth":"100px" },
            { "mDataProp": "RECEIPT_DATE","sWidth":"120px" },
            { "mDataProp": "TRANSACTION_STATUS","sWidth":"150px",
                "fnRender": function(obj) {
                    if(obj.aData.TRANSACTION_STATUS=='new')
                        return '新建';
                    if(obj.aData.TRANSACTION_STATUS=='confirmed')
                        return '已确认';
                    if(obj.aData.TRANSACTION_STATUS=='cancel')
                        return '取消';
                    
                    return obj.aData.TRANSACTION_STATUS;
                 }
            },
            { "mDataProp": "REMARK","sWidth":"380px"}
         ]
	});

	// 回单签收
	$("#example").on('click', '.returnOrderReceipt', function(e){
		e.preventDefault();
        //异步向后台提交数据
		var id = $(this).attr('code');
		$.post('/returnOrder/returnOrderReceipt/'+id,function(data){
           //保存成功后，刷新列表
           console.log(data);
           if(data.success){
          	   dataTable.fnDraw();
           }else{
               alert('签收失败');
           }
        },'json');
	});
		
	// 取消
	$("#example").on('click', '.cancelbutton', function(e){
		e.preventDefault();
        //异步向后台提交数据
		var id = $(this).attr('code');
		$.post('/returnOrder/cancel/'+id,function(data){
           //保存成功后，刷新列表
           console.log(data);
           if(data.success){
          	   dataTable.fnDraw();
           }else{
               alert('取消失败');
           }
        },'json');
	 });
	
   	$("#btn").click(function(){      	
   	alert("waerwerwee");      
     	$("#routeItemFormDiv").show();   
    });
   	 
    $("#cancel").click(function(){
    	$("#routeItemFormDiv").hide();
    });
    
    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
    	 $(".bootstrap-datetimepicker-widget").hide();
        $('#time_one').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	 $(".bootstrap-datetimepicker-widget").hide();
    	$('#time_two').trigger('keyup');
    });
    
    $("#order_no ,#tr_order_no ,#de_order_no,#stator,#status,#time_one,#time_two,#customer_filter").on( 'keyup click', function () {    	 	
    	var order_no = $("#order_no").val();
    	var tr_order_no = $("#tr_order_no").val();
    	var de_order_no = $("#de_order_no").val();
    	var stator = $("#stator").val();    	
    	var status = $("#status").val();
    	var time_one = $("#time_one").val();
    	var time_two = $("#time_two").val();
    	var inputStr =$("#customer_filter").val();
    	console.log(inputStr);
    	/*if (status=="新建") {
    		status ="new";
    	}
		if (status=="确认") {
			status = "confirmed";
		}
		if (status=="取消") {
			status ="cancel";
		}*/
    	dataTable.fnSettings().sAjaxSource = "/returnOrder/list?order_no="+order_no+"&tr_order_no="+tr_order_no+"&de_order_no="+de_order_no+"&stator="+stator+"&status="+status+"&time_one="+time_one+"&time_two="+time_two+"&customer="+inputStr;
    	dataTable.fnDraw();
    });

    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'en',  
        pickDate: true,  
        pickTime: true,  
        hourStep: 1,  
        minuteStep: 15,  
        secondStep: 30,  
        inputMask: true  
    });
    
    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'en',  
        pickDate: true,  
        pickTime: true,  
        hourStep: 1,  
        minuteStep: 15,  
        secondStep: 30,  
        inputMask: true  
      });
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

	 $('#companyList').on('click', '.fromLocationItem', function(e){        
	       $('#customer_filter').val($(this).text());
	       $("#companyList").hide();
	       var companyId = $(this).attr('partyId');
	       $('#customerId').val(companyId);
	       var inputStr =$("#customer_filter").val();
	       console.log(inputStr);
	       if(inputStr!=null){
	    	   var order_no = $("#order_no").val();
		    	var tr_order_no = $("#tr_order_no").val();
		    	var de_order_no = $("#de_order_no").val();
		    	var stator = $("#stator").val();    	
		    	var status = $("#status").val();
		    	var time_one = $("#time_one").val();
		    	var time_two = $("#time_two").val();
		    	
		    	/*if (status=="新建") {
		    		status ="new";
		    	}
				if (status=="确认") {
					status = "confirmed";
				}
				if (status=="取消") {
					status ="cancel";
				}*/
		    	dataTable.fnSettings().sAjaxSource = "/returnOrder/list?order_no="+order_no+"&tr_order_no="+tr_order_no+"&de_order_no="+de_order_no+"&stator="+stator+"&status="+status+"&time_one="+time_one+"&time_two="+time_two+"&customer="+inputStr;
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
});