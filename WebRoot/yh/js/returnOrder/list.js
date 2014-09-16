 $(document).ready(function() {
	$('#menu_return').addClass('active').find('ul').addClass('in');
	
	var dataTable =$('#example').dataTable( {
	    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",insert into return_order(status_code,create_date,order_type,creator,remark,transfer_order,distribution_order_id,contract_id
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/returnOrder/list",
   			"aoColumns": [
   			{ "mDataProp": "ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/yh/returnOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            { "mDataProp": "CNAME","sWidth":"120px"},
            { "mDataProp": "TRANSFER_ORDER_NO"},
            { "mDataProp": "DELIVERY_ORDER_NO"},
            { "mDataProp": "CREATOR_NAME","sWidth":"120px" },
            { "mDataProp": "CREATE_DATE","sWidth":"100px" },
            { "mDataProp": "RECEIPT_DATE","sWidth":"120px" },
            { "mDataProp": "FROMNAME" },
            { "mDataProp": "TONAME" },
            { "mDataProp": "TRANSACTION_STATUS",
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
            { "mDataProp": "REMARK" },
            { "mDataProp": null,
           		"sWidth": "8%", 
                "fnRender": function(obj) {  
                	if(obj.aData.TRANSACTION_STATUS=='已签收'){
                		return "<a class='btn btn-danger btn-xs cancelbutton' code='"+obj.aData.ID+"' title='取消'>"+
					               "<i class='fa fa-trash-o fa-fw'></i>"+
					           "</a>";
                	}else{
	                	return "<a class='btn btn-success btn-xs returnOrderReceipt' code='"+obj.aData.ID+"' title='回单签收'>"+
				                "回单签收"+
				            "</a> "+
				            "<a class='btn btn-danger btn-xs cancelbutton' code='"+obj.aData.ID+"' title='取消'>"+
				                "<i class='fa fa-trash-o fa-fw'></i>"+
				            "</a>";
                	}
           	    }
 			}
         ]
	});

	// 回单签收
	$("#example").on('click', '.returnOrderReceipt', function(e){
		e.preventDefault();
        //异步向后台提交数据
		var id = $(this).attr('code');
		$.post('/yh/returnOrder/returnOrderReceipt/'+id,function(data){
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
		$.post('/yh/returnOrder/cancel/'+id,function(data){
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
        $('#time_one').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$('#time_two').trigger('keyup');
    });
    
    $("#order_no ,#tr_order_no ,#de_order_no,#stator,#status,#time_one,#time_two").on( 'keyup click', function () {    	 	
    	var order_no = $("#order_no").val();
    	var tr_order_no = $("#tr_order_no").val();
    	var de_order_no = $("#de_order_no").val();
    	var stator = $("#stator").val();    	
    	var status = $("#status").val();
    	var time_one = $("#time_one").val();
    	var time_two = $("#time_two").val();
    	if (status=="新建") {
    		status ="new";
    	}
		if (status=="确认") {
			status = "confirmed";
		}
		if (status=="取消") {
			status ="cancel";
		}
    	dataTable.fnSettings().sAjaxSource = "/yh/returnOrder/list?order_no="+order_no+"&tr_order_no="+tr_order_no+"&de_order_no="+de_order_no+"&stator="+stator+"&status="+status+"&time_one="+time_one+"&time_two="+time_two;
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
});