
$(document).ready(function() {
	document.title = '手工收入单查询 | '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var datatable=$('#chargeMiscOrderList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
        "bSort": false,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeMiscOrder/list",
        "aoColumns": [   
            {"mDataProp":"ORDER_NO","sWidth": "80px",
            	"fnRender": function(obj) {
        			return "<a href='/chargeMiscOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"CHARGE_FROM_TYPE","sWidth": "100px",
                "fnRender": function(obj) {
                    if(obj.aData.CHARGE_FROM_TYPE=='customer'){
                        return '客户';
                    }else if(obj.aData.CHARGE_FROM_TYPE=='sp'){
                        return '供应商';
                    }else{
                        return '其他';
                    }
                }
            },
            {"mDataProp":"TYPE","sWidth": "120px",
            	"fnRender": function(obj) {
                    if(obj.aData.TYPE=='biz'){
                        return '业务收款';
                    }else{
                        return '非业务收款';
                    }
                }
            },
            {"mDataProp":"CUSTOMER_NAME","sWidth": "100px"},
            {"mDataProp":"SP_NAME","sWidth": "130px"},
            {"mDataProp":"OTHERS_NAME","sWidth": "100px"},
            {"mDataProp":"TOTAL_AMOUNT","sWidth": "100px"},
            {"mDataProp":"STATUS","sWidth": "100px",
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },
            {"mDataProp":"CREATE_STAMP","sWidth": "100px",
                "fnRender":function(obj){
                    var create_stamp=obj.aData.CREATE_STAMP;
                    var str=create_stamp.substr(0,10);
                    return str;
                }
            },
            
            {"mDataProp":"REMARK","sWidth": "150px"}                       
        ]      
    });	 


    $("#customer_filter,  #customer_filter, #orderNo_filter, #status_filter, #beginTime_filter, #endTime_filter").on( 'keyup click', function () {
        refreshData();
    });

    var refreshData=function(){
        var customer=$("#customer_filter").val();
        var sp=$("#sp_filter").val();
        var orderNo = $("#orderNo_filter").val();
        var status = $("#status_filter").val();
        var beginTime = $("#beginTime_filter").val();
        var endTime = $("#endTime_filter").val();
        datatable.fnSettings().sAjaxSource = "/chargeMiscOrder/list?orderNo="+orderNo +"&status="+status
                                                +"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime
                                                +"&endTime="+endTime;
        datatable.fnDraw();
    };
} );