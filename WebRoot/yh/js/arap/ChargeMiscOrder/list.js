
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

    //获取客户列表，自动填充
    $('#customer_filter').on('keyup click', function(event){
        var me = this;
        var inputStr = $('#customer_filter').val();
        var companyList =$("#companyList");
        $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
            companyList.empty();
            for(var i = 0; i < data.length; i++)
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
                
            companyList.css({ 
                left:$(me).position().left+"px", 
                top:$(me).position().top+32+"px" 
            });
            companyList.show();    
        },'json');
        /*if(inputStr=='')
            transferOrder.fnFilter('', 2);*/
        
    });
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).text());
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId);
        refreshData();
    });
    // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    $('#companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
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