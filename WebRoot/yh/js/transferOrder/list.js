
$(document).ready(function() {

    $('#menu_transfer').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var transferOrder = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/transferOrder/list",
        "aoColumns": [   
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
            			return "<a href='/yh/transferOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
            		}},
            {"mDataProp":"STATUS"},
            {"mDataProp":"CARGO_NATURE",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "damageCargo"){
            			return "损坏货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return "ATM";
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
            			return "源鸿自提";
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
                "sWidth": "8%",                
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger cancelbutton' code='"+obj.aData.ID+"'>"+
	                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                            "取消"+
	                        "</a>";
                }
            }                         
        ]  
    });	
    
    $("#eeda-table").on('click', '.cancelbutton', function(e){
		  e.preventDefault();
       //异步向后台提交数据
		 var id = $(this).attr('code');
		$.post('/yh/transferOrder/cancel/'+id,function(data){
               //保存成功后，刷新列表
               console.log(data);
               if(data.success){
            	   transferOrder.fnDraw();
               }else{
                   alert('取消失败');
               }
               
           },'json');
		});
    
    $('#orderNo_filter').on( 'keyup', function () {
    	var orderNo = $("#orderNo_filter").val();
        /*transferOrder.fnFilter(orderNo, 0, false, true);
        transferOrder.fnDraw();*/
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var officeName = $("#officeName_filter").val();
    	transferOrder.fnSettings().sAjaxSource = "/yh/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
    	transferOrder.fnDraw(); 
    } );
 
    $('input.status_filter').on( 'keyup click', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var officeName = $("#officeName_filter").val();
    	transferOrder.fnSettings().sAjaxSource = "/yh/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
    	transferOrder.fnDraw();
    } );
    
    $('input.address_filter').on( 'keyup click', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var officeName = $("#officeName_filter").val();
    	transferOrder.fnSettings().sAjaxSource = "/yh/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
    	transferOrder.fnDraw();
    } );
    
    $('input.customer_filter').on( 'keyup click', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var officeName = $("#officeName_filter").val();
    	transferOrder.fnSettings().sAjaxSource = "/yh/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
    	transferOrder.fnDraw();
    } );
    
    $('input.sp_filter').on( 'keyup click', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var officeName = $("#officeName_filter").val();
    	transferOrder.fnSettings().sAjaxSource = "/yh/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
    	transferOrder.fnDraw();
    } );
    
    $('input.beginTime_filter').on( 'change input', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var officeName = $("#officeName_filter").val();
    	transferOrder.fnSettings().sAjaxSource = "/yh/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
    	transferOrder.fnDraw();
    } );
    
    $('#beginTime_filter').on('keyup', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var officeName = $("#officeName_filter").val();
    	transferOrder.fnSettings().sAjaxSource = "/yh/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
    	transferOrder.fnDraw();
    } );    
    
    $('#endTime_filter').on( 'keyup click', function () {
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var officeName = $("#officeName_filter").val();
    	transferOrder.fnSettings().sAjaxSource = "/yh/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
    	transferOrder.fnDraw();
    } );
    
    // 导入运输单
    $("#fileUploadBtn").click(function(){
    	$("#toFileUpload").click();
    });
    
    
	$('#toFileUpload').fileupload({
        dataType: 'json',
        done: function (e, data) {
            $.each(data.result.files, function (index, file) {
                $('<p/>').text(file.name).appendTo(document.body);
            });
            //transferOrder.fnDraw();
        }
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

} );