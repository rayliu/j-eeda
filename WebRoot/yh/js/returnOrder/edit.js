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
    
	//datatable, 动态处理
    var detailDataTable = $('#detailTable').dataTable({
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/yh/transferOrderItemDetail/transferOrderDetailList?orderId="+transferOrderId,
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
        if($("#order_id").val() == ""){
	    	$.post('/yh/returnOrder/save', $("#returnOrderForm").serialize(), function(returnOrder){
				if(returnOrder.ID>0){
				  	$("#style").show();	             
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }
	});
});
