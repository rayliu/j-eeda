
$(document).ready(function() {

    $('#menu_finance').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var invoiceApplicationOrderIds = $("#invoiceApplicationOrderIds").val();
    var total = 0.00;
    var datatable=$('#costConfirm_table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costConfirm/list",
        "aoColumns": [
            {"mDataProp":"ORDER_NO", "sWidth": "100px", //付款确认单号
            	"fnRender": function(obj) {
        			return "<a href='/costConfirm/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
    				$("#total_amount").val(total);
        		}
            },
            {"mDataProp": null , "sWidth": "100px",//付款申请单号
            	"fnRender": function(obj) {
            		if(obj.aData.FKSQ_NO != null){
            			return obj.aData.FKSQ_NO;
            		}else if(obj.aData.REIMBURSEMENT_NO != null){
            			return obj.aData.REIMBURSEMENT_NO;
            		}else if(obj.aData.CAR_NO != null){
            			return obj.aData.CAR_NO;
            		}else{
            			return obj.aData.MISC_NO;
            		}
            	}
            }, 
            {"mDataProp":"STATUS", "sWidth": "60px"},//状态
            {"mDataProp":"SP_NAME", "sWidth": "180px"},//供应商
            {"mDataProp":"RECEIVE_PERSON","sWidth": "80px"},//收款人
            {"mDataProp":"PAY_AMOUNT","sWidth": "60px"},//付款金额  
            {"mDataProp":"ALREADY_PAY","sWidth": "60px"},//已付金额
            {"mDataProp":null,"sWidth": "60px", //未付金额
                "fnRender": function(obj) {
                    return obj.aData.PAY_AMOUNT - obj.aData.ALREADY_PAY;
                }    
            },
            {"mDataProp":"REMARKS","sWidth": "100px"},//备注
            {"mDataProp":"USER_NAME","sWidth": "60px"},//创建人
            {"mDataProp":"CREATE_DATE","sWidth": "80px",
                "fnRender":function(obj){
                    var create_stamp=obj.aData.CREATE_DATE;
                    var str=create_stamp.substr(0,10);
                    return str;
                }
            },//创建时间           
        ]      
    });	


    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();   
        $('#beginTime_filter').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter').trigger('keyup');
    });

    $('#query_btn').click(function(){
        var order_no = $('#order_no_filter').val();
        var status = $('#status_filter').val();
        var sp_name = $('#sp_filter').val();
        var receiverName = $('#receiver_filter').val();
        var beginTime = $('#beginTime_filter').val();
        var endTime = $('#endTime_filter').val();
        datatable.fnSettings().sAjaxSource = "/costConfirm/list?orderNo="+order_no
            +"&status="+status
            +"&sp_name="+sp_name
            +"&receiverName="+receiverName
            +"&beginTime="+beginTime
            +"&endTime="+endTime;
            
        datatable.fnDraw(); 
    });


} );