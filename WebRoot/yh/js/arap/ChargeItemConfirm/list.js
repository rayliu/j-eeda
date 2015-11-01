$(document).ready(function() {
	document.title = '应收明细确认 | '+document.title;
    $('#menu_charge').addClass('active').find('ul').addClass('in');
   
	  //datatable, 动态处理
    var chargeConfiremTable = $('#chargeConfirem-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "bSort": true, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeConfiremList/list",
        "aoColumns": [ 
            { "mDataProp": null, "sWidth":"20px",
              "fnRender": function(obj) {
                return '<input type="checkbox" name="order_check_box" order_type="'+obj.aData.ORDER_TP+'" value="'+obj.aData.ID+'">';
              }
            },  
            {"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"ORDER_NO", "sWidth":"120px",
            	"fnRender": function(obj) {
            		return eeda.getUrlByNo(obj.aData.ID, obj.aData.ORDER_NO);
        		}},
		        {"mDataProp":null, "sWidth":"120px",
                "fnRender": function(obj) {
                    return "未收款";
            }},
            {"mDataProp":"TOTAL_AMOUNT", "sWidth":"120px"}, 
            {"mDataProp":"CHANGE_AMOUNT", "sWidth":"120px"}, 
            {"mDataProp":"ADDRESS", "sWidth":"150px"}, 
            {"mDataProp":"CNAME", "sWidth":"100px"},
            {"mDataProp":"SP", "sWidth":"150px"},
            {"mDataProp":"PLANNING_TIME", "sWidth":"150px"}, 
            {"mDataProp":"DEPART_TIME", "sWidth":"130px"},
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"120px"},
            {"mDataProp":"DELIVERY_ORDER_NO", "sWidth":"120px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"120px"},        	
            {"mDataProp":null, "sWidth": "120px", 
                "fnRender": function(obj) {
                    if(obj.aData.TRANSACTION_STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.TRANSACTION_STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.TRANSACTION_STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.TRANSACTION_STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.TRANSACTION_STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.TRANSACTION_STATUS;
                }
            },            
            {"mDataProp":"RECEIPT_DATE", "sWidth":"150px"},        	
            {"mDataProp":"ROUTE_FROM", "sWidth":"100px"},                        
            {"mDataProp":"ROUTE_TO", "sWidth":"100px"},                        
            /*{"mDataProp":null, "sWidth":"150px"},                         
            {"mDataProp":null, "sWidth":"100px"},*/                        
            {"mDataProp":"CONTRACT_AMOUNT", "sWidth":"80px"},
            {"mDataProp":"TRANSFER_AMOUNT", "sWidth":"150px"},
            //{"mDataProp":"PICKUP_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":"PICKUP_AMOUNT", "sWidth":"80px"},                        
            {"mDataProp":"SEND_AMOUNT", "sWidth":"80px"},                        
            {"mDataProp":"INSURANCE_AMOUNT", "sWidth":"80px"},                        
            {"mDataProp":"SUPER_MILEAGE_AMOUNT", "sWidth":"80px"},                        
            {"mDataProp":"STEP_AMOUNT", "sWidth":"80px"},                        
            {"mDataProp":"INSTALLATION_AMOUNT", "sWidth":"80px"},                        
            {"mDataProp":"LOAD_AMOUNT", "sWidth":"150px"},                        
            {"mDataProp":"WAREHOUSE_AMOUNT", "sWidth":"80px"},                        
            {"mDataProp":"WAIT_AMOUNT", "sWidth":"80px"},                        
            {"mDataProp":"OTHER_AMOUNT", "sWidth":"80px"},  
            {"mDataProp":null, "sWidth":"80px"},                        
                                   
            {"mDataProp":"REMARK", "sWidth":"200px"}                       
        ]      
    });	
    
    $("#chargeConfiremBtn").click(function(e){
        e.preventDefault();
    	var trArr=[];
    	var orderNoArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		trArr.push($(this).val());
        		orderNoArr.push($(this).attr('order_type'));
        	}
        });     
        console.log(trArr);
        var returnOrderIds = trArr.join(",");
        var orderno=orderNoArr.join(",");
        $.post("/chargeConfiremList/chargeConfiremReturnOrder", {returnOrderIds:returnOrderIds,orderno:orderno}, function(data){
        	if(data.success){
        		chargeConfiremTable.fnSettings().sAjaxSource = "/chargeConfiremList/list";
        		chargeConfiremTable.fnDraw(); 
        	}
        },'json');
    });
    
    //获取所有客户
    $('#customer_filter').on('keyup click', function(){
           var inputStr = $('#customer_filter').val();
           
           $.get("/customerContract/search", {locationName:inputStr}, function(data){
               console.log(data);
               var companyList =$("#companyList");
               companyList.empty();
               for(var i = 0; i < data.length; i++)
               {
                   companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
               }
               if(data.length>0)
                   companyList.show();
               
           },'json');
           refreshCreateList();
       });
    
    var refreshCreateList = function(){
    	  //获取所有的条件
        var customer = $('#customer_filter').val();
		var beginTime = $("#beginTime_filter").val();
		var endTime = $("#endTime_filter").val();
		var orderNofilter = $("#orderNo_filter").val();
		var transferOrderNo = $("#transfer_Order_filter").val();
		var customerNo = $("#customerNo_filter").val();
		var start = $("#start_filter").val();
		var status = $("#shouru_filter").val();
	    chargeConfiremTable.fnSettings().sAjaxSource = "/chargeConfiremList/list?customer="+customer
	   												+"&beginTime="+beginTime
	   												+"&endTime="+endTime
	   												+"&transferOrderNo="+transferOrderNo
	   												+"&customerNo="+customerNo
	   												+"&orderNo="+orderNofilter
	   												+"&start="+start
	   												+"&status="+status;
		   chargeConfiremTable.fnDraw(); 
    };
   //选中某个客户时候
      $('#companyList').on('click', '.fromLocationItem', function(e){        
           $('#customer_filter').val($(this).text());
           $("#companyList").hide();
           var companyId = $(this).attr('partyId');
           $('#customerId').val(companyId);

           refreshCreateList();
           
       });
      // 没选中客户，焦点离开，隐藏列表
       $('#customer_filter').on('blur', function(){
           $('#companyList').hide();
       });

       //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
       $('#companyList').on('blur', function(){
           $('#companyList').hide();
       });

       $('#companyList').on('mousedown', function(){
           return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
       });
       
       //过滤客户
       $('#beginTime_filter,#endTime_filter,#orderNo_filter,#transfer_Order_filter,#customerNo_filter,#start_filter').on( 'keyup ',function(){
    	   refreshCreateList();  
       });
       $("#shouru_filter").on('change',function(){
    	   refreshCreateList();
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
  
} );