$(document).ready(function() {
	$('#menu_return').addClass('active').find('ul').addClass('in');
		
	var transferOrderId = $("#transferOrderId").val();
	//datatable, 动态处理
    var itemDataTable = $('#itemTable').dataTable({
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/yh/transferOrderItem/transferOrderItemList?order_id="+transferOrderId,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [  			            
            {
            	"mDataProp":"ITEM_NO",            	
            	"sWidth": "80px",
            	"sClass": "item_no"
        	},
            {
            	"mDataProp":"ITEM_NAME",
            	"sWidth": "180px",
            	"sClass": "item_name"
            },
            {
            	"mDataProp":"SIZE",            	
            	"sWidth": "50px",
            	"sClass": "size"
        	},
            {
            	"mDataProp":"WIDTH",
            	"sWidth": "50px",
            	"sClass": "width"
            },
            {
            	"mDataProp":"HEIGHT",            	
            	"sWidth": "50px",
            	"sClass": "height"
        	}, 
            {
            	"mDataProp":"WEIGHT",
            	"sWidth": "50px",
            	"sClass": "weight",
            },
        	{
            	"mDataProp":"AMOUNT",
            	"sWidth": "50px",
            	"sClass": "amount"
            }, 
            {
            	"mDataProp":"UNIT",
            	"sWidth": "50px",
            	"sClass": "unit"
            },
            {
            	"mDataProp":null,
            	"sWidth": "50px",
            	"sClass": "sumWeight",
            	"fnRender": function(obj) {
        			return obj.aData.WEIGHT * obj.aData.AMOUNT;
                }
            },
            {
            	"mDataProp":"VOLUME",
            	"sWidth": "50px",
            	"sClass": "volume",
            	"fnRender": function(obj) {
            		return obj.aData.VOLUME * obj.aData.AMOUNT;
            	}
            },            
            {"mDataProp":"REMARK"},
            {  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-success btn-xs dateilEdit' code='?id="+obj.aData.ID+"' title='查看单品'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                            "</a> ";
                }
            }                         
        ]      
    });
    
    var deliveryOrderId = $("#deliveryOrderId").val();
	//datatable, 动态处理
    var detailDataTable = $('#detailTable').dataTable({
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/yh/returnOrder/transferOrderDetailList?orderId="+transferOrderId+"&deliveryOrderId="+deliveryOrderId,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [  			            
            {
            	"mDataProp":"SERIAL_NO",
        		"sWidth": "80px",
            	"sClass": "serial_no"	
            },
            {
            	"mDataProp":"ITEM_NO",
        		"sWidth": "80px",
            	"sClass": "item_no"            		
            },  
		    {
		    	"mDataProp":"ITEM_NAME",
		    	"sWidth": "80px",
		    	"sClass": "item_name"            		
		    },       	
            {
            	"mDataProp":"VOLUME",
        		"sWidth": "80px",
            	"sClass": "volume"            		
            },
            {
            	"mDataProp":"WEIGHT",
        		"sWidth": "80px",
            	"sClass": "weight"
            },
            {
            	"mDataProp":"CONTACT_PERSON",
        		"sWidth": "80px",
            	"sClass": "contact_person"
            },
            {
            	"mDataProp":"PHONE",
        		"sWidth": "80px",
            	"sClass": "phone"
            },
            {
            	"mDataProp":"ADDRESS",
        		"sWidth": "80px",
            	"sClass": "address"
            },
            {
            	"mDataProp":"REMARK",
        		"sWidth": "80px",
            	"sClass": "remark"
            }                       
        ]      
    });
    
	// 编辑单品
	$("#itemTable").on('click', '.dateilEdit', function(e){
		e.preventDefault();
		var code = $(this).attr('code');
		var itemId = code.substring(code.indexOf('=')+1);
		$("#item_id").val(itemId);
		$("#transferOrderItemDateil").show();

		// 设置单品信息
		$("#detail_transfer_order_id").val($("#order_id").val());
		$("#detail_transfer_order_item_id").val(itemId);
		
		detailDataTable.fnSettings().sAjaxSource = "/yh/transferOrderItemDetail/transferOrderDetailList?item_id="+itemId;
		detailDataTable.fnDraw();  
	});

	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveReturnOrderBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
    	$.post('/yh/returnOrder/save', $("#returnOrderForm").serialize(), function(returnOrder){
			if(returnOrder.ID>0){
			  	$("#style").show();	             
			}else{
				alert('数据保存失败。');
			}
		},'json');
	});
 	
 	if($("#status").val() == 'new'){
 		$("#statusSpan").text('新建');
 	}else if($("#status").val() == 'confirmed'){
 		$("#statusSpan").text('确认'); 		
 	}else{
 		$("#statusSpan").text(''); 		
 	}
 	
 	if($("#cargoNature").val() == 'ATM'){
 		$("#cargoNatureSpan").text('ATM');
 	}else if($("#cargoNature").val() == 'cargo'){
 		$("#cargoNatureSpan").text('普通货品'); 		
 	}else{
 		$("#cargoNatureSpan").text(''); 		
 	}
 	
 	if($("#pickupMode").val() == 'own'){
 		$("#pickupModeSpan").text('源鸿自提');
 	}else if($("#pickupMode").val() == 'routeSP'){
 		$("#pickupModeSpan").text('干线供应商自提'); 		
 	}else if($("#pickupMode").val() == 'pickupSP'){
 		$("#pickupModeSpan").text('外包供应商提货'); 		
 	}else{
 		$("#pickupModeSpan").text(''); 		 		
 	}
 	
 	if($("#arrivalMode").val() == 'gateIn'){
 		$("#arrivalModeSpan").text('入中转仓');
 	}else if($("#arrivalMode").val() == 'delivery'){
 		$("#arrivalModeSpan").text('货品直送'); 		
 	}else{
 		$("#arrivalModeSpan").text(''); 		
 	}
 	
 	//应收datatable
 	 var order_id =$("#returnOrderid").val();
	var receipttable =$('#table_fin').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource":"/yh/returnOrder/accountReceivable/"+order_id,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
			{"mDataProp":"NAME","sWidth": "80px","sClass": "name"},
			{"mDataProp":"AMOUNT","sWidth": "80px","sClass": "amount"}, 
			{"mDataProp":"TRANSFERORDERNO","sWidth": "80px","sClass": "remark"},
			{"mDataProp":"REMARK","sWidth": "80px","sClass": "remark"},
			{"mDataProp":"STATUS","sWidth": "80px","sClass": "status"},
        ]      
    });
	

    //获取全国省份
    $(function(){
     	var province = $("#mbProvinceTo");
     	$.post('/yh/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
				var hideProvince = $("#hideProvinceTo").val();
     		for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideProvince){
     				province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
     				
     				
					}else{
     				province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     		
     	},'json');
    });
    
    //获取省份的城市
    $('#mbProvinceTo').on('change', function(){
			var inputStr = $(this).val();
			$.get('/yh/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCityTo");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				toLocationList.show();
			},'json');
		});
    
    //获取城市的区县
    $('#cmbCityTo').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
			$.get('/yh/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbAreaTo");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				toLocationList.show();
			},'json');
		});
    
    $('#cmbAreaTo').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
		});  
    
    // 回显城市
    var hideProvince = $("#hideProvinceTo").val();
    $.get('/yh/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
		if(data.length > 0){
			var cmbCity =$("#cmbCityTo");
			cmbCity.empty();
			cmbCity.append("<option>--请选择城市--</option>");
			var hideCity = $("#hideCityTo").val();
			for(var i = 0; i < data.length; i++)
			{
				if(data[i].NAME == hideCity){
					cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
				}else{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			}
		}
	},'json');

    // 回显区
    var hideCity = $("#hideCityTo").val();
    $.get('/yh/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
		if(data.length > 0){
			var cmbArea =$("#cmbAreaTo");
			cmbArea.empty();
			cmbArea.append("<option>--请选择区(县)--</option>");
			var hideDistrict = $("#hideDistrictTo").val();
			for(var i = 0; i < data.length; i++)
			{
				if(data[i].NAME == hideDistrict){
					cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
				}else{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			}
		}
	},'json');
});
