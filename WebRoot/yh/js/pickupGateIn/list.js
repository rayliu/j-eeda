
$(document).ready(function() {
	document.title = '干线提货单查询 | '+document.title;

    $('#menu_status').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));
    
	//datatable, 动态处理
    var transferOrder = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示        
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt",//datatable的中文翻译
            "sProcessing":     "Procesando..."
        },
        "sAjaxSource": "/pickupGateIn/list?flag=new",
        "aoColumns": [   
            {"mDataProp":"ORDER_NO", "sWidth":"150px",
            	"fnRender": function(obj) {
        			return "<a href='/pickupGateIn/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}
            },
            {"mDataProp":"SP_NAME"},
            {"mDataProp":"PICKUP_MODE",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == 'own'){
            			return '公司自提';
            		}else if(obj.aData.PICKUP_MODE == 'routeSP'){
            			return '干线供应商自提';
            		}else if(obj.aData.PICKUP_MODE == 'pickupSP'){
            			return '外包供应商提货';
            		}
            		return '';
            	}
            },
            {"mDataProp":"DRIVER"},
            {"mDataProp":"STATUS"},
            {"mDataProp":"AUDIT_STATUS"},
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"REMARK"}
        ]  
    });	

    
    $('#resetBtn').click(function(e){
        $("#searchForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    })

   var searchData=function(){
        var orderNo = $.trim($("#order_no").val());
        var sp_id=$.trim($("#sp_id").val());
        var status_filter=$.trim($("#status_filter").val());
        var beginTime =$.trim($("#create_stamp_begin_time").val());
        var endTime = $.trim($("#create_stamp_end_time").val());
        
        var flag = false;
        $('#searchForm input,#searchForm select').each(function(){
        	 var textValue = $.trim(this.value);
        	 if(textValue != '' && textValue != null){
        		 flag = true;
        		 return;
        	 } 
        });
        if(!flag){
        	 $.scojs_message('请输入至少一个查询条件', $.scojs_message.TYPE_FALSE);
        	 return false;
        }
        /*  
            查询规则：参数对应DB字段名
            *_no like
            *_id =
            *_status =
            时间字段需成双定义  *_begin_time *_end_time   between
        */
        transferOrder.fnSettings().sAjaxSource = "/pickupGateIn/list?order_no="+orderNo
             +"&sp_id="+sp_id
             +"&status_filter="+status_filter
             +"&begin_time="+beginTime
             +"&end_time="+endTime;
        transferOrder.fnDraw(); 
        
    };
    

} );