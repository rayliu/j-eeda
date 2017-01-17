 $(document).ready(function() {
	 document.title = '回单查询 | '+document.title;
	$('#menu_return').addClass('active').find('ul').addClass('in');
	var clickTabId = "createTab";

	
	$("input[name='checkAll']").click(function(){
		var checked = true;
		if(!$(this).prop('checked')){
			checked = false;
			//$('#confirmBtn').attr('disabled',true);
		}else{
			//$('#confirmBtn').attr('disabled',false);
		}
		
    	$("input[name='order_check_box']").each(function () {  
            this.checked = checked;  
         });  
    });
	
	
	$('#confirmBtn').on('click',function(){
		var self = this;
		$(self).attr("disabled",true);
        
    	var trArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		trArr.push($(this).attr("id"));
        		$(this).attr("disabled",true);
        	}
        });     
        
        if(trArr.length==0){
        	$.scojs_message('请勾选要签收的单据', $.scojs_message.TYPE_TYPE_ERROR);
        	return false;
        }
        
        $.post('returnOrder/returnOrderReceipt',{id:trArr.toString()},function(data){
        	if(data){
        		$.scojs_message('签收成功', $.scojs_message.TYPE_OK);
        		$(self).attr("disabled",false);
        	}
        }).fail(function(){
        	$.scojs_message('签收失败', $.scojs_message.TYPE_ERROR);
        	$(self).attr("disabled",false);
        });
	});
	
  
	//条件查询
  $("#return_type, #transfer_type, #order_no ,#tr_order_no ,#de_order_no,#stator,#status,#time_one,#time_two, #serial_no, #sign_no","#officeSelect").on('keyup', function (e) {    	 	
  	
    var code = e.which; // recommended to use e.which, it's normalized across browsers
    if(code==13)e.preventDefault();
    if(code==13){
        findData();
    }
  });

  // $('#return_type, #transfer_type').on('change', function () {
  // 	findData();
  // });
    
   
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
        "bFilter": false, //不需要默认的搜索框
        "bProcessing": true,//table载入数据时，是否显示‘loading...’提示  
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": false,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        //"sAjaxSource": "/returnOrder/list?status=新建",
   			"aoColumns": [
			{ "mDataProp": null, "sWidth":"10px", "bSortable": false,
			    "fnRender": function(obj) {
			      return '<input type="checkbox" name="order_check_box" id="'+obj.aData.ID+'" order_no="'+obj.aData.BUSINESS_TYPE+'">';	
			    }
			},
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
   			{ "mDataProp": "TRANSFER_TYPE","sWidth":"65px" },
   			{ "mDataProp": "PLANNING_TIME","sWidth":"100px" },
   			{ "mDataProp": "SIGN_NO","sWidth":"100px" },
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
        { "mDataProp": "OFFICE_NAME","sWidth":"100px" },
        { "mDataProp": "BUSINESS_STAMP","sWidth":"120px" },
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
    	  "bSort": true, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bProcessing": true,
        "bServerSide": false,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        // "sAjaxSource": "/returnOrder/list?status='已签收','已确认','对账中','对账已确认'",
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
            { "mDataProp": "OFFICE_NAME","sWidth":"100px" },
            { "mDataProp": "BUSINESS_STAMP","sWidth":"120px" },
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

  $("#searchBtn").click(function(){
      saveConditions();
      findData();
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
          customer : $("#customer_id").val(),
          customer_name : $("#customer_id_input").val(),
          serial_no: $("#serial_no").val(),
          sign_no: $("#sign_no").val(),
          return_type: $("#return_type").val(),
          transfer_type: $("#transfer_type").val(),
          warehouse: $("#warehouse").val(),
          to_name: $("#to_name").val(),
          province: $("#province").val()
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
          $("#customer_id").val(conditions.customer);
          $("#customer_id_input").val(conditions.customer_name);
          $("#serial_no").val(conditions.serial_no);
          $("#sign_no").val(conditions.sign_no);
          $("#return_type").val(conditions.return_type);
          $("#transfer_type").val(conditions.transfer_type);
          $("#warehouse").val(conditions.warehouse);
          $("#to_name").val(conditions.to_name);
          $("#province").val(conditions.province);
          
      }
  };
  
  var findData = function(){
      var customer_filter=$('#customer_id_input').val();
       if(!customer_filter){
          $.scojs_message('请选择客户', $.scojs_message.TYPE_ERROR);
          return;
       }
      var order_no = $("#order_no").val();
      var serial_no = $("#serial_no").val();
      var warehouse = $("#warehouse").val();
      var return_type = $("#return_type").val();
      var tr_order_no = $("#tr_order_no").val();
      var de_order_no = $("#de_order_no").val();
      var sign_no = $("#sign_no").val();
      var time_one = $("#time_one").val();
      var time_two = $("#time_two").val();
      var inputStr =$("#customer_id").val();
      var transfer_type =$("#transfer_type").val();
      var to_name =$("#to_name").val();
      var province =$("#province").val();
      var imgaudit =$("#imgaudit").val();
      var photo_type =$("#photo_type").val();
      var q_begin =$("#q_begin_date").val();
      var q_end =$("#q_end_date").val();
      var officeSelect = $("#officeSelect").val();
      var delivery_date_begin_time = $("#delivery_date_begin_time").val();
      var delivery_date_end_time = $("#delivery_date_end_time").val();
     
      if(clickTabId == "createTab"){
        createDataTable.fnSettings().oFeatures.bServerSide = true;
        createDataTable.fnSettings().sAjaxSource = "/returnOrder/list?order_no="+order_no+"&sign_no="+sign_no+"&serial_no="+serial_no+"&officeSelect="+officeSelect+"&tr_order_no="+tr_order_no+"&de_order_no="+de_order_no+"&status='新建'&time_one="+time_one+"&time_two="+time_two+"&customer="+inputStr+"&return_type="+return_type+"&transfer_type="+transfer_type+"&warehouse="+warehouse+"&to_name="+to_name+"&province="+province+"&imgaudit="+imgaudit+"&photo_type="+photo_type+"&delivery_date_begin_time="+delivery_date_begin_time+"&delivery_date_end_time="+delivery_date_end_time;
        createDataTable.fnDraw();
      }else{
        finishDataTable.fnSettings().oFeatures.bServerSide = true;
        finishDataTable.fnSettings().sAjaxSource = "/returnOrder/list?order_no="+order_no+"&sign_no="+sign_no+"&serial_no="+serial_no+"&officeSelect="+officeSelect+"&tr_order_no="+tr_order_no+"&de_order_no="+de_order_no+"&status='已签收','已确认','对账中','对账已确认','已拒收'&time_one="+time_one+"&time_two="+time_two+"&customer="+inputStr+"&return_type="+return_type+"&transfer_type="+transfer_type+"&warehouse="+warehouse+"&to_name="+to_name+"&province="+province+"&imgaudit="+imgaudit+"&q_begin="+q_begin+"&q_end="+q_end+"&photo_type="+photo_type+"&delivery_date_begin_time="+delivery_date_begin_time+"&delivery_date_end_time="+delivery_date_end_time;
        finishDataTable.fnDraw();
      }
      saveConditions();
    };

  $('#downloadBtn').click(function(){
      var customer_filter=$('#customer_id_input').val();
       if(!customer_filter){
          $.scojs_message('请选择客户', $.scojs_message.TYPE_ERROR);
          return;
       }
       // var photo_type =$('#photo_type').val();
       // if(!photo_type){
       //    $.scojs_message('请选择图片类型', $.scojs_message.TYPE_ERROR);
       //    return;
       // }
       var q_begin_date =$('#q_begin_date').val();
       var q_end_date =$('#q_end_date').val();
       if(!q_begin_date || !q_end_date){
          $.scojs_message('请选择签收时间', $.scojs_message.TYPE_ERROR);
          return;
       }

       $('#myModal').modal('show');
  });

  $('#btnOK').click(function(){
      var order_no = $("#order_no").val();
      var serial_no = $("#serial_no").val();
      var warehouse = $("#warehouse").val();
      var return_type = $("#return_type").val();
      var tr_order_no = $("#tr_order_no").val();
      var de_order_no = $("#de_order_no").val();
      var sign_no = $("#sign_no").val();
      var time_one = $("#time_one").val();
      var time_two = $("#time_two").val();
      var inputStr =$("#customer_id").val();
      var transfer_type =$("#transfer_type").val();
      var to_name =$("#to_name").val();
      var province =$("#province").val();
      var imgaudit =$("#imgaudit").val();
      var photo_type =$('#photo_type_radio input[name=sign]:checked').val();
      var q_begin =$("#q_begin_date").val();
      var q_end =$("#q_end_date").val();
      var officeSelect = $("#officeSelect").val();
      var delivery_date_begin_time = $("#delivery_date_begin_time").val();
      var delivery_date_end_time = $("#delivery_date_end_time").val();
        $.post("returnOrder/download?order_no="+order_no+"&sign_no="+sign_no+"&serial_no="+serial_no
          +"&officeSelect="+officeSelect+"&tr_order_no="+tr_order_no+"&de_order_no="
          +de_order_no+"&status='已签收','已确认','对账中','对账已确认','已拒收'&time_one="+time_one
          +"&time_two="+time_two+"&customer="+inputStr+"&return_type="+return_type
          +"&transfer_type="+transfer_type+"&warehouse="+warehouse+"&to_name="+to_name
          +"&province="+province+"&imgaudit="+imgaudit+"&q_begin="+q_begin+"&q_end="+q_end
          +"&photo_type="+photo_type+"&delivery_date_begin_time="+delivery_date_begin_time
          +"&delivery_date_end_time="+delivery_date_end_time,
            function(data){
              $('#myModal').modal('hide');
              //保存成功后，刷新列表
              if(data){
                 if(data == 'noFile'){
                   alert('当前条件无记录, 无法下载文件');
                 }else{
                    window.open(data);
                 }
              }else
                $.scojs_message('下载失败', $.scojs_message.TYPE_ERROR);

        });
  });

  loadConditions(); 
 
});
 
 //获取所有的网点
 $.post('/transferOrder/searchPartOffice',function(data){
	 if(data.length > 0){
		 var officeSelect = $("#officeSelect");
		 officeSelect.empty();
		 officeSelect.append("<option ></option>");
		
		 for(var i=0; i<data.length; i++){
			 officeSelect.append("<option value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");					 
		 }
		
	 }
 },'json');