 $(document).ready(function() {
	 document.title = '回单查询 | '+document.title;
	$('#menu_return').addClass('active').find('ul').addClass('in');
	var clickTabId = "createTab";
	
	//条件查询
    $("#order_no ,#tr_order_no ,#de_order_no,#stator,#status,#time_one,#time_two, #serial_no, #sign_no,#return_type").on('keyup click', function () {    	 	
    	findData();
    });
    
    
    //获取所有客户
    $('#customer_filter').on('keyup click', function(){
       var inputStr = $('#customer_filter').val();
       var companyList =$("#companyList");
       $.get("/customerContract/search", {locationName:inputStr}, function(data){
           companyList.empty();
           for(var i = 0; i < data.length; i++)
               companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
       },'json');
       companyList.show();
    });

	$('#companyList').on('click', '.fromLocationItem', function(e){        
       $('#customer_filter').val($(this).text());
       $("#companyList").hide();
       var companyId = $(this).attr('partyId');
       $('#customerId').val(companyId);
       findData();
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
   
    //开始-时间按钮
    $('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN',
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
		$(".bootstrap-datetimepicker-widget").hide();
	    $('#time_one').trigger('keyup');
	});
    
    //结束-时间按钮
    $('#datetimepicker2').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN',
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
		$(".bootstrap-datetimepicker-widget").hide();
	    $('#time_two').trigger('keyup');
	});
    
	
	var createDataTable =$('#example').dataTable( {
       /* "bProcessing": true, //table载入数据时，是否显示‘loading...’提示  */    	  
		"bSort": false, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "bProcessing": true,
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
		
        //"sAjaxSource": "/returnOrder/list?status=新建",
   			"aoColumns": [
   			{ "mDataProp": "ORDER_NO",
   				"sWidth":"100px",
            	"fnRender": function(obj) {
            		if(Return.isUpdate || Return.isComplete)
            			return "<a href='/returnOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		else
            			return obj.aData.ORDER_NO;
        		}
   			},
   			{ "mDataProp": "RETURN_TYPE","sWidth":"65px" },
   			{ "mDataProp": "PLANNING_TIME","sWidth":"100px" },
   			{ "mDataProp": "SERIAL_NO","sWidth":"60px"},
   			{ "mDataProp": "ITEM_NO","sWidth":"80px"},
   			{ "mDataProp": "RECEIPT_PERSON","sWidth":"60px"},
            { "mDataProp": "RECEIPT_PHONE","sWidth":"80px"},
            { "mDataProp": "RECEIVING_UNIT","sWidth":"120px"},
            { "mDataProp": "RECEIPT_ADDRESS","sWidth":"100px"},
            { "mDataProp": "WAREHOUSE_NAME","sWidth":"80px"},
   		    { "mDataProp": "A_AMOUNT","sWidth":"30px"},
            { "mDataProp": "CNAME","sWidth":"80px"},
            { "mDataProp": "TRANSFER_ORDER_NO","sWidth":"120px"},
            { "mDataProp": "FROM_NAME","sWidth":"80px"},
            { "mDataProp": "TO_NAME","sWidth":"80px"},
            { "mDataProp": "ADDRESS","sWidth":"120px"}, 
            /*{ "mDataProp": "TURNOUT_TIME","sWidth":"120px"},*/
            { "mDataProp": "DELIVERY_ORDER_NO","sWidth":"120px"},
            { "mDataProp": "CREATOR_NAME","sWidth":"100px" },
            { "mDataProp": "CREATE_DATE","sWidth":"100px"},
            { "mDataProp": "TRANSACTION_STATUS","sWidth":"40px",
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
            { "mDataProp": "IMGAUDIT","sWidth":"70px" },
            { "mDataProp": "REMARK"}
         ]
	});
	
	var finishDataTable =$('#example2').dataTable( {
    	"bSort": false, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bProcessing": true,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/returnOrder/list?status='已签收','已确认','对账中','对账已确认'",
   			"aoColumns": [
   			{ "mDataProp": "ORDER_NO",
   				"sWidth":"100px",
            	"fnRender": function(obj) {
            		if(Return.isUpdate || Return.isComplete)
            			return "<a href='/returnOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		else
            			return obj.aData.ORDER_NO;
        		}
   			},
   		    { "mDataProp": "WAREHOUSE_NAME","sWidth":"100px"},
   		    { "mDataProp": "SERIAL_NO","sWidth":"120px"},
          { "mDataProp": "SIGN_NO","sWidth":"120px"},
   		    { "mDataProp": "ITEM_NO","sWidth":"80px"},
   		    { "mDataProp": "A_AMOUNT","sWidth":"30px"},
   		    { "mDataProp": "RECEIVING_UNIT","sWidth":"100px"},
   		    { "mDataProp": "RECEIPT_ADDRESS","sWidth":"120px"},
   		    { "mDataProp": "RECEIPT_PERSON","sWidth":"75px"},
   		    { "mDataProp": "RECEIPT_PHONE","sWidth":"100px"},
            { "mDataProp": "CNAME","sWidth":"120px"},
            { "mDataProp": "TRANSFER_ORDER_NO","sWidth":"120px"},
            { "mDataProp": "FROM_NAME","sWidth":"120px"},
            { "mDataProp": "TO_NAME","sWidth":"120px"},
            { "mDataProp": "ADDRESS","sWidth":"120px"},
            { "mDataProp": "DELIVERY_ORDER_NO","sWidth":"120px"},
            { "mDataProp": "CREATOR_NAME","sWidth":"100px" },
            { "mDataProp": "CREATE_DATE","sWidth":"140px" },
            { "mDataProp": "PLANNING_TIME","sWidth":"140px" },
            { "mDataProp": "RECEIPT_DATE","sWidth":"140px" },
            { "mDataProp": "TRANSACTION_STATUS","sWidth":"60px",
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
            { "mDataProp": "IMGAUDIT","sWidth":"60px" },
            { "mDataProp": "REMARK"}
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
           if(data.success)
        	   createDataTable.fnDraw();
           else
               alert('签收失败');
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
           if(data.success)
        	   createDataTable.fnDraw();
           else
               alert('取消失败');
        },'json');
	 });
	
   	$("#btn").click(function(){      	
   		alert("waerwerwee");      
     	$("#routeItemFormDiv").show();   
    });
   	 
    $("#cancel").click(function(){
    	$("#routeItemFormDiv").hide();
    });

        
    //点击tab选项卡查询
    $("#createTab ,#finishTab").on('click', function (e) { 
    	clickTabId = e.target.getAttribute("id");
    	console.log("当前选项卡："+clickTabId);
    	findData();
    });

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
       findData();
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
   
    //开始-时间按钮
    $('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN',
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
		$(".bootstrap-datetimepicker-widget").hide();
	    $('#time_one').trigger('keyup');
	});
    
    //结束-时间按钮
    $('#datetimepicker2').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN',
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
		$(".bootstrap-datetimepicker-widget").hide();
	    $('#time_two').trigger('keyup');
	});
    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
        saveConditions();
        findData();
    });

  //var conditions_name="query_return_order_list";
  var saveConditions=function(){
      var conditions={
          order_no:$("#order_no").val(),
          tr_order_no :$("#tr_order_no").val(),
          de_order_no:$("#de_order_no").val(),
          time_one:$("#time_one").val(),
          time_two : $("#time_two").val(),
          customer : $("#customer_filter").val(),
          serial_no: $("#serial_no").val(),
          sign_no: $("#sign_no").val(),
          return_type: $("#return_type").val()
      }
      if(!!window.localStorage){//查询条件处理
          localStorage.setItem("query_return_order_list", JSON.stringify(conditions));
      }
  };
  
  var loadConditions=function(){
      if(!!window.localStorage){//查询条件处理
          var query_to = localStorage.getItem("query_return_order_list");
          if(!query_to)
              return;

          var conditions = JSON.parse(localStorage.getItem("query_return_order_list"));
          $("#order_no").val(conditions.order_no);
          $("#tr_order_no").val(conditions.tr_order_no);
          $("#de_order_no").val(conditions.de_order_no);
          $("#time_one").val(conditions.time_one);
          $("#time_two").val(conditions.time_two);
          $("#customer_filter").val(conditions.customer);
          $("#serial_no").val(conditions.serial_no);
          $("#sign_no").val(conditions.sign_no);
          $("#return_type").val(conditions.return_type);
      }
  };

  var findData = function(){
      var order_no = $("#order_no").val();
      var serial_no = $("#serial_no").val();
      var return_type = $("#return_type").val();
      var tr_order_no = $("#tr_order_no").val();
      var de_order_no = $("#de_order_no").val();
      var sign_no = $("#sign_no").val();
      var time_one = $("#time_one").val();
      var time_two = $("#time_two").val();
      var inputStr =$("#customer_filter").val();
      
      if(clickTabId == "createTab"){
        createDataTable.fnSettings().sAjaxSource = "/returnOrder/list?order_no="+order_no+"&serial_no="+serial_no+"&tr_order_no="+tr_order_no+"&de_order_no="+de_order_no+"&status='新建'&time_one="+time_one+"&time_two="+time_two+"&customer="+inputStr+"&return_type="+return_type;
        createDataTable.fnDraw();
      }else{
        finishDataTable.fnSettings().sAjaxSource = "/returnOrder/list?order_no="+order_no+"&sign_no="+sign_no+"&serial_no="+serial_no+"&tr_order_no="+tr_order_no+"&de_order_no="+de_order_no+"&status='已签收','已确认','对账中','对账已确认'&time_one="+time_one+"&time_two="+time_two+"&customer="+inputStr+"&return_type="+return_type;
        finishDataTable.fnDraw();
      }
      saveConditions();
    };

  loadConditions(); 
  findData();
});