
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
                	if(obj.aData.STATUS=='已签收'){
                		return obj.aData.STATUS;
                	}else{
                		return obj.aData.STATUS+"<a id='edit_status' del_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                	}
                }
            },
            {"mDataProp":"CUSTOMER"},
            {"mDataProp":"C2"},
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"TRANSFER_ORDER_NO"},
            { 
                "mDataProp": null, 
                "fnRender": function(obj) {   
                	if(obj.aData.STATUS=='已签收'){
                		return "已签收";
                	}else{
                		return "<a class='btn btn-primary confirmDelivery' code='"+obj.aData.ID+"'>"+
                		"签收完成"+
                		"</a>";
                	}
                }
            }    
        ]  
    });	
    //签收完成
    $("#eeda-table").on('click', '.confirmDelivery', function(e){
    	var delivery_id =$(this).attr("code");
    	if(confirm("确定签收 吗？")){
    		$.post('/yh/deliveryOrderMilestone/receipt',{delivery_id:delivery_id},function(data){
    			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
    			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
    			detailTable.fnDraw(); 
    		},'json');
        }
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
	
 
    
    $('#endTime_filter ,#beginTime_filter ,#sp_filter ,#deliveryNo_filter ,#customer_filter ,#transferorderNo_filter').on( 'keyup click', function () {
    	var deliveryNo = $("#deliveryNo_filter").val();
    	var customer = $("#customer_filter").val();
    	var transferorderNo = $("#transferorderNo_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	detailTable.fnSettings().sAjaxSource = "/yh/delivery/deliveryMilestone?deliveryNo="+deliveryNo+"&customer="+customer+"&transferorderNo="+transferorderNo+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime;
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