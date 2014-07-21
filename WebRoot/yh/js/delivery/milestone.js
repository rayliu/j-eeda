
$(document).ready(function() {

	$('#menu_deliver').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var detailTable = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/delivery/deliveryMilestone",
        "aoColumns": [   
            {"mDataProp":null,
            	"fnRender": function(obj) {
            		 return "<a href='/yh/delivery/edit/"+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
            		}
            },
            {"mDataProp":null,
                "fnRender": function(obj) {
                	if(obj.aData.STATUS==null){
                		obj.aData.STATUS="";
                	}
                    return obj.aData.STATUS+"<a id='edit_status' del_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                }
            },
            {"mDataProp":null},
            {"mDataProp":"C2"},
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"TRANSFER_ORDER_NO"},
            { 
	                "mDataProp": null, 
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-primary confirmDelivery' code='"+obj.aData.ID+"'>"+
	                    		"签收完成"+
	                    		"</a>";
	                }
	            }    
        ]  
    });	
    $("#eeda-table").on('click', '.confirmDelivery', function(e){
    	var delivery_id =$(this).attr("code");
		$.post('/yh/deliveryOrderMilestone/receipt',{delivery_id:delivery_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$("#receiptBtn").attr("disabled", true);
    });
    $("#eeda-table").on('click', '#edit_status', function(e){
    	e.preventDefault();	
    	var depart_id=$(this).attr("del_id");
    	$("#milestoneDepartId").val(depart_id);
    	$.post('/yh/delivery/transferOrderMilestoneList',{departOrderId:depart_id},function(data){
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
		$.post('/yh/delivery/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
			detailTable.fnSettings().sAjaxSource = "/yh/delivery/deliveryMilestone";
			detailTable.fnDraw();  
		},'json');
		//$('#transferOrderMilestone').modal('hide');
	}); 
	
 
    
    $('#endTime_filter ,#beginTime_filter ,#sp_filter ,#status_filter ,#orderNo_filter ,#departNo_filter').on( 'keyup click', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var departNo_filter = $("#departNo_filter").val();
    	var status = $("#status_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	detailTable.fnSettings().sAjaxSource = "/yh/departOrder/list?orderNo="+orderNo+"&departNo="+departNo_filter+"&status="+status+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime;
    	detailTable.fnDraw();
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

} );