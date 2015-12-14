$(document).ready(function() {
    $('#menu_status').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    detailTable = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/departOrder/transferonTriplist",
        "aoColumns": [   
              {"mDataProp":"ORDER_NO",
              	"fnRender": function(obj) {
              			return "<a href='/transferOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
              		}},
             
              {"mDataProp":null,
                  "fnRender": function(obj) {
                  	if(obj.aData.STATUS==null){
                  		obj.aData.STATUS="";
                  	}
                      return obj.aData.STATUS+"<a id='edit_status' order_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'></a>";
                  }},  
             
              {"mDataProp":"CARGO_NATURE",
              	"fnRender": function(obj) {
              		if(obj.aData.CARGO_NATURE == "cargo"){
              			return "普通货品";
              		}else if(obj.aData.CARGO_NATURE == "damageCargo"){
              			return "损坏货品";
              		}else if(obj.aData.CARGO_NATURE == "ATM"){
              			return tranOntrip.ex_cargo;
              		}else{
              			return "";
              		}}},        	
      		{"mDataProp":"OPERATION_TYPE",
      			"fnRender": function(obj) {
      				if(obj.aData.OPERATION_TYPE == "out_source"){
      					return "外包";
      				}else if(obj.aData.OPERATION_TYPE == "own"){
      					return "自营";
      				}else{
      					return "";
      				}}},        	
              {"mDataProp":"PICKUP_MODE",
              	"fnRender": function(obj) {
              		if(obj.aData.PICKUP_MODE == "routeSP"){
              			return "干线供应商自提";
              		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
              			return "外包供应商提货";
              		}else if(obj.aData.PICKUP_MODE == "own"){
              			return tranOntrip.ex_type;
              			
              		}else{
              			return "";
              		}}},
              {"mDataProp":"ARRIVAL_MODE",
              	"fnRender": function(obj) {
              		if(obj.aData.ARRIVAL_MODE == "delivery"){
              			return "货品直送";
              		}else if(obj.aData.ARRIVAL_MODE == "gateIn"){
              			return "入中转仓";
              		}else{
              			return "";
              		}}},
              {"mDataProp":"ADDRESS"},
              {"mDataProp":"CREATE_STAMP"},
              {"mDataProp":"ORDER_TYPE",
              	"fnRender": function(obj) {
              		if(obj.aData.ORDER_TYPE == "salesOrder"){
              			return "销售订单";
              		}else if(obj.aData.ORDER_TYPE == "arrangementOrder"){
              			return "调拨订单";
              		}else if(obj.aData.ORDER_TYPE == "cargoReturnOrder"){
              			return "退货订单";
              		}else if(obj.aData.ORDER_TYPE == "damageReturnOrder"){
              			return "质量退单";
              		}else{
              			return "";
              		}}},
              {"mDataProp":"CNAME"},
              {"mDataProp":"SPNAME"},
              {"mDataProp":"ONAME"},
              {"mDataProp":"REMARK"},
              { 
                  "mDataProp": null, 
                  "fnRender": function(obj) {   
                  	if(obj.aData.STATUS=='已收货'){
                  		return "已收货";
                  	}else{
                  		return "<a class='btn  btn-primary confirmReceipt' code='"+obj.aData.ID+"'>"+
                  		"收货确认"+
                  		"</a>";
                  	}
                  }
              }                                     
          ] 
    });	

    // 收货确认
    $("#eeda-table").on('click', '.confirmReceipt', function(e){
    	var orderId =$(this).attr("code");
    	if(confirm("确定收货吗？")){
    		$.post('/transferOrderMilestone/receipt', {orderId:orderId}, function(data){    
    			if(data.success){
    				detailTable.fnDraw(); 		
                }else{
                    alert('收货出错');
                }
        	});
        } else {
            return;
        }
    });
    
    $("#eeda-table").on('click', '#edit_status', function(e){
    	e.preventDefault();	
    	var order=$(this).attr("order_id");
    	$("#milestoneDepartId").val(order);
    	$.post('/departOrder/transferMilestoneList',{order_id:order},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.empty();
			for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
			{
				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
			}
		},'json');
    	
    });
    
    // 保存新里程碑
	$("#transferOrderMilestoneFormBtn").click(function(){
		$.post('/departOrder/saveTransferMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
			detailTable.fnDraw();  
		},'json');
		//$('#transferOrderMilestone').modal('hide');
	}); 
	
 	$('#orderNo_filter,#status_filter,#address_filter,#beginTime_filter,#endTime_filter').on( 'keyup', function () {
 		resetResult();
    } );
    
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
  //获取【供应商】的list，选中信息在下方展示其他信息
    $('#sp_filter').on('keyup click', function(){
    	sp_filterKeyup();
    	$("#cpnameList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        });
        $('#cpnameList').show();
	});
  //获取【客户】的list，选中信息在下方展示其他信息
    $('#customer_filter').on('keyup click', function(){
    	customer_filterKeyup();
    	$("#customerList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        });
        $('#customerList').show();
	});
   //获取【网点】的list，选中信息在下方展示其他信息
    $('#officeName_filter').on('keyup click', function(){
    	//inputKeyUp();
    	officeName_filterKeyup();
    	$("#officenameList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        });
        $('#officenameList').show();
	});
    

	// 没选中【供应商】，焦点离开，隐藏列表
	$('#sp_filter').on('blur', function(){
 		$('#cpnameList').hide();
 	});
	// 没选中【客户】，焦点离开，隐藏列表
	$('#customer_filter').on('blur', function(){
 		$('#customerList').hide();
 	});
	// 没选中【网点】，焦点离开，隐藏列表
	$('#officeName_filter').on('blur', function(){
 		$('#officenameList').hide();
 	});

	//当用户只点击了滚动条，没选【供应商、客户、网点】，再点击页面别的地方时，隐藏列表
	$('#sp_filter,#customer_filter,#officeName_filter').on('blur', function(){
 		$('#cpnameList').hide();
 	});

	// 选中【供应商】
	$('#cpnameList').on('mousedown', '.fromLocationItem', function(e){
		console.log($('#cpnameList').is(":focus"));
		var message = $(this).text();
		$('#sp_filter').val(message);
        $('#cpnameList').hide();
        resetResult();
    });
	// 选中【客户】
	$('#customerList').on('mousedown', '.fromLocationItem', function(e){
		console.log($('#customerList').is(":focus"));
		var message = $(this).text();
		$('#customer_filter').val(message);
        $('#customerList').hide();
        resetResult();
    });
	// 选中【网点】
	$('#officenameList').on('mousedown', '.fromLocationItem', function(e){
		console.log($('#officenameList').is(":focus"));
		var message = $(this).text();
		$('#officeName_filter').val(message);
        $('#officenameList').hide();
        resetResult();
    });
    
	//供应商、客户、网点
    $('#sp_filter,#customer_filter,#officeName_filter').on( 'keyup click', function () {
    	resetResult();
    } );
		
} );
function sp_filterKeyup(){
	var sp_filter = $('#sp_filter').val();
	$.get('/departOrder/cpnameList',{sp_filter:sp_filter}, function(data){
		console.log(data);
		var cpnameList =$("#cpnameList");
		cpnameList.empty();
		for(var i = 0; i < data.length; i++)
		{
			cpnameList.append("<li><a tabindex='-1' class='fromLocationItem' >"+data[i].COMPANY+"</a></li>");
		}
	},'json');
	
}
function customer_filterKeyup(){
	var customer_filter = $('#customer_filter').val();
	$.get('/departOrder/customerList',{customer_filter:customer_filter}, function(data){
		console.log(data);
		var customerList =$("#customerList");
		customerList.empty();
		for(var i = 0; i < data.length; i++)
		{
			customerList.append("<li><a tabindex='-1' class='fromLocationItem' >"+data[i].CUSTOMER+"</a></li>");
		}
	},'json');
}
function officeName_filterKeyup(){
	var officeName_filter = $('#officeName_filter').val();
	$.get('/departOrder/officenameList',{officeName_filter:officeName_filter}, function(data){
		console.log(data);
		var officenameList =$("#officenameList");
		officenameList.empty();
		for(var i = 0; i < data.length; i++)
		{
			officenameList.append("<li><a tabindex='-1' class='fromLocationItem' >"+data[i].OFFICENAME+"</a></li>");
		}
	},'json');
}
function resetResult(){
	var orderNo = $("#orderNo_filter").val();
	var status = $("#status_filter").val();
	var address = $("#address_filter").val();
	var customer = $("#customer_filter").val();
	var sp = $("#sp_filter").val();
	var beginTime = $("#beginTime_filter").val();
	var endTime = $("#endTime_filter").val();
	var officeName = $("#officeName_filter").val();
	detailTable.fnSettings().sAjaxSource = "/departOrder/transferonTriplist?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
	detailTable.fnDraw();
}
