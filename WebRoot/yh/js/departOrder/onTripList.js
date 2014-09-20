
$(document).ready(function() {

    $('#menu_status').addClass('active').find('ul').addClass('in');
    
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
        "sAjaxSource": "/departOrder/onTripList",
        "aoColumns": [   
            {"mDataProp":null,
            	"fnRender": function(obj) {
            		 return "<a href='/departOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.DEPART_NO+"</a>";
            	}
            },
            {"mDataProp":null,
                "fnRender": function(obj) {
                	if(obj.aData.LOCATION!=null && obj.aData.LOCATION!=''){
                		return obj.aData.LOCATION+"<a id='edit_status' depart_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                	}else{
                    	if(obj.aData.DEPART_STATUS==null){
                    		obj.aData.DEPART_STATUS="";
                    	}
                    	return obj.aData.DEPART_STATUS+"<a id='edit_status' depart_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                	}                	
                }
            },
            {"mDataProp":"CONTACT_PERSON"},
            {"mDataProp":"PHONE"},
            {"mDataProp":"CAR_NO"},
            {"mDataProp":"CARTYPE"},     
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"TRANSFER_ORDER_NO"},
            { 
                "mDataProp": null, 
                "fnRender": function(obj) {  
                	if(obj.aData.ARRIVAL_MODE=='delivery'){
                		return "";
                	}
                	else if(obj.aData.DEPART_STATUS=='已入库'){
                		return "已入库";
                	}else{
                		return "<a class='btn btn-primary confirmInWarehouse' code='"+obj.aData.ID+"'>"+
                		"入库确认"+
                		"</a>";
                	}
                }
            }    
        ]  
    });	
   
    //签收完成
    $("#eeda-table").on('click', '.confirmInWarehouse', function(e){
    	var departOrderId =$(this).attr("code");
    	if(confirm("确定入库吗？")){
    		$.post('/transferOrderMilestone/warehousingConfirm',{departOrderId:departOrderId},function(data){
    			if(data.success){
    				detailTable.fnDraw(); 		
                }else{
                    alert('入库失败');
                }
    		},'json');
        } else {
            return;
        }
    });
    
    $("#eeda-table").on('click', '#edit_status', function(e){
    	e.preventDefault();	
    	var depart_id=$(this).attr("depart_id");
    	$("#milestoneDepartId").val(depart_id);
    	$.post('/departOrder/transferOrderMilestoneList',{departOrderId:depart_id},function(data){
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
		$.post('/departOrder/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
			detailTable.fnDraw();  
		},'json');
		//$('#transferOrderMilestone').modal('hide');
	}); 
	
 
	//供应商暂时去除:#sp_filter ,
    $('#endTime_filter ,#beginTime_filter ,#status_filter ,#orderNo_filter ,#departNo_filter').on( 'keyup click', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var departNo_filter = $("#departNo_filter").val();
    	var status = $("#status_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	detailTable.fnSettings().sAjaxSource = "/departOrder/onTripList?orderNo="+orderNo+"&departNo="+departNo_filter+"&status="+status+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime;
    	detailTable.fnDraw();
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
    
    
  //获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter').on('keyup click', function(){
		var inputStr = $('#sp_filter').val();
		$.get('/departOrder/companyNameList',{input:inputStr}, function(data){
			console.log(data);
			var cpnameList =$("#cpnameList");
			cpnameList.empty();
			for(var i = 0; i < data.length; i++)
			{
				cpnameList.append("<li><a tabindex='-1' class='fromLocationItem' >"+data[i].COMPANY+"</a></li>");
			}
		},'json');

		$("#cpnameList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#cpnameList').show();
	});

	// 没选中供应商，焦点离开，隐藏列表
	$('#sp_filter').on('blur', function(){
 		$('#cpnameList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#cpnameList').on('blur', function(){
 		$('#cpnameList').hide();
 	});

	$('#cpnameList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});

	// 选中供应商
	$('#cpnameList').on('mousedown', '.fromLocationItem', function(e){
		console.log($('#cpnameList').is(":focus"));
		var message = $(this).text();
		$('#sp_filter').val(message);
        $('#cpnameList').hide();
        
    });
    
} );