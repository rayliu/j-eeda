
$(document).ready(function() {
	document.title = '往来票据查询 | '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var datatable=$('#result_table').DataTable({
    	 "bProcessing": true, //table载入数据时，是否显示‘loading...’提示  
        "searching": false,
        //"serverSide": true,
        "scrollX": true,
        "scrollCollapse": true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        //"ajax": "/inOutMiscOrder/list",
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
		},
        "columns":[
           { "width": "30px",
               "render": function ( data, type, full, meta ) {
                 return '<button type="button" class="btn btn-primary delete btn-xs" >'+
                       '<i class="fa fa-trash-o"></i> 删除</button>';
               }
           },
            {"data":"ORDER_NO", "sWidth": "80px",
            	"render": function ( data, type, full, meta ) {
        			return "<a href='/inOutMiscOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
        		}
            },
            {"data":"BIZ_TYPE","sWidth": "50px",
                "render": function ( data, type, full, meta ) {
                    if(data=='personal'){
                        return '借支';
                    }else{
                        return '公司';
                    }
                }
            },
            {"data":"ORDER_TYPE","sWidth": "50px",
            	"render": function ( data, type, full, meta ) {
                    if(data=='1'){
                        return '押金';
                    }else if(data=='2'){
                        return '仓储费';
                    }else if(data=='3'){
                        return '运费';
                    }else if(data=='4'){
                        return '贷款利息';
                    }else if(data=='5'){
                        return '社保费';
                    }else if(data=='6'){
                        return '其它';
                    }
                    return '';
                }
            },
            {"data":"ISSUE_DATE","sWidth": "60px",
                "render": function ( data, type, full, meta ) {
                    if(data)
                        return data.substr(0,10);
                    return data;
                }
            },
            {"data":"REF_NO","sWidth": "80px"},
            {"data":"CHARGE_UNIT","sWidth": "100px"},
            {"data":"CHARGE_PERSON","sWidth": "100px"},
            {"data":"PAY_AMOUNT","sWidth": "100px"},
            {"data":"PAY_STATUS","sWidth": "100px","class":"pay_status"},
            
            {"data":"PAY_UNIT","sWidth": "130px"},
            {"data":"PAY_PERSON","sWidth": "100px"},
            {"data":"CHARGE_AMOUNT","sWidth": "100px"},
            {"data":"CHARGE_STATUS","sWidth": "100px","class":"charge_status"},

            {"data":"CREATE_DATE","sWidth": "100px",
                "render": function ( data, type, full, meta ) {
                    return data.substr(0,10);
                }
            },
            {"data":"REMARK","sWidth": "150px"}                       
        ]      
    });	 
    
    $('#result_table').on('click','.delete',function(){
    	
    	var tr = $(this).parent().parent();
    	var id = tr.attr('id');
    	var pay_status = tr.find('.pay_status').text();
    	var charge_status = tr.find('.charge_status').text();

    	if(!confirm("是否确定删除？")){
    		return false;
    	}
    	
    	if(!(pay_status=='未付'&&charge_status=='未收')){
    		$.scojs_message('单据已存在财务单据，需要先撤销对应财务单据方可删除', $.scojs_message.TYPE_FAIL);
    		return false;
    	}
    	
    	
    	if(id>0){
    		$.post('/inOutMiscOrder/delete', {id:id}, function(data){
    			if(data){
    				$.scojs_message("删除成功", $.scojs_message.TYPE_OK);
    				refreshData();
    			}else{
    				$.scojs_message("删除失败", $.scojs_message.TYPE_FAIL);
    			}
    		}).fail(function() {
    	        $.scojs_message('删除失败', $.scojs_message.TYPE_FAIL);
    	   });
    	}
    	
    });
    

    
    $("#charge_unit_filter,  #pay_unit_filter, #order_no_filter, #beginTime_filter, #endTime_filter").on( 'keyup click', function () {
        //refreshData();
    });

    $("#biz_type_filter").on('change', function () {
       // refreshData();
    });

    $("#searchBtn").click(function(){
    	refreshData();
    });

    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
    });
    
    var refreshData=function(){
        var charge_unit=$("#charge_unit_filter").val();
        var pay_unit=$("#pay_unit_filter").val();
        var order_no = $("#order_no_filter").val();
        var biz_type = $("#biz_type_filter").val();
        var beginTime = $("#beginTime_filter").val();
        var endTime = $("#endTime_filter").val();
        
        var flag = false;
        
        $('#searchForm input,#searchForm select').each(function(){
        	 var textValue = this.value;
        	 if(textValue != '' && textValue != null){
        		 flag = true;
        		 return;
        	 } 
        });
        if(!flag){
        	 $.scojs_message('请输入至少一个查询条件', $.scojs_message.TYPE_FALSE);
        	 return false;
        }
        
        var url="/inOutMiscOrder/list?order_no="+order_no +"&biz_type="+biz_type
                                                +"&charge_unit="+charge_unit
                                                +"&pay_unit="+pay_unit
                                                +"&beginTime="+beginTime
                                                +"&endTime="+endTime;
        datatable.ajax.url(url).load();
    };
} );