$(document).ready(function() {
	document.title = '单品状态查询 | '+document.title;
    $('#menu_report').addClass('active').find('ul').addClass('in');
	//datatable, 动态处理
    var statusTable = $('#eeda-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"bLengthChange":true,
    	"bProcessing": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [   
            {"mDataProp":"ID", "sWidth":"80px"},          
            {"mDataProp":"SERIAL_NO", "sWidth":"80px"},
            {"mDataProp":"ITEM_NO", "sWidth":"100px"},
            {"mDataProp":"CUSTOMER", "sWidth":"100px"},
            {"mDataProp":"NOTIFY_PARTY_COMPANY", "sWidth":"100px"},
            {"mDataProp":null, "sWidth":"80px",
            	"fnRender": function(obj) {  
            		/*	
            		 	新建运输	
						在货场
						运输在途
						在中转仓
						新建配送
						配送在途
						客户签收（回单在途）
						回单签收
						已对账
						已收款
					*/
            		var status = "新建运输";
            		var TRANSACTION_STATUS = obj.aData.TRANSACTION_STATUS;  //回单
            		var DELIVERY_STATUS = obj.aData.DELIVERY_STATUS;        //配送
            		var DEPART_STATUS = obj.aData.DEPART_STATUS;            //发车
            		var PICK_STATUS = obj.aData.PICK_STATUS;                //提货
            		
            		if(TRANSACTION_STATUS != null){
            			if(TRANSACTION_STATUS == "新建"){
            				status = "回单未签收";
            			}else{
            				status = "回单已签收";
            			}
            		}else if(DELIVERY_STATUS != null){
            			if(DELIVERY_STATUS == "已完成" || DELIVERY_STATUS == "已送达"){
            				status = "回单未签收";
            			}else if(DELIVERY_STATUS == "配送在途"){
            				status = "配送在途";
            			}else if(DELIVERY_STATUS == "新建"){
            				status = "新建配送";
            			}
            		}else if(DEPART_STATUS != null){
            			if(DEPART_STATUS == "新建"){
            				status = "新建发车";
            			}else {
            				status = DEPART_STATUS;
            			}
            		}else if(PICK_STATUS != null){
            			if(PICK_STATUS == "新建"){
            				status = "新建调车";
            			}else{
            				status = "已入货场";
            			}
            		}
            		
            		return "<b style='color:red'>"+status+"</b>";
                }
            },       	
            {"mDataProp":"PLANNING_TIME", "sWidth":"80px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"80px"},
            {"mDataProp":"TRANSFER_NO", "sWidth":"80px"},
            {"mDataProp":"WAREHOUSE_NAME", "sWidth":"80px"},
            {"mDataProp":"WAREHOUSE_STAMP", "sWidth":"120px"},
            {"mDataProp":"DELIVERY_NO", "sWidth":"80px"},
            {"mDataProp":"DELIVERY_STAMP", "sWidth":"80px"},
            {"mDataProp":"RETURN_STAMP", "sWidth":"80px"}
            
        ]  
    });	
    
    
    $("#queryBtn").on('click', function () {
    	var beginTime=$("#beginTime").val();
    	var endTime=$("#endTime").val();
    	var serial_no = $("#serial_no").val();
    	var sign_no = $("#sign_no").val();
    	var order_no = $("#order_no").val();
    	var customer_id = $("#customer_id").val();
    	var customer_order_no = $("#customer_order_no").val();
    	var item_no = $("#item_no").val();
    	
		statusTable.fnSettings().oFeatures.bServerSide = true;
		statusTable.fnSettings()._iDisplayStart = 0;
    	statusTable.fnSettings().sAjaxSource = "/statusReport/productStatus?beginTime="+beginTime+"&endTime="+endTime+"&serial_no="+serial_no
    		+"&order_no="+order_no+"&customer_id="+customer_id+"&customer_order_no="+customer_order_no+"&item_no="+item_no+"&sign_no="+sign_no;
    	statusTable.fnDraw(); 
    	
    });
    
    //获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup click', function(){
		if($('#customerMessage').val() == "")
			$("#customer_id").val("");
		$.get('/statusReport/search', {locationName:$('#customerMessage').val()}, function(data){
			console.log(data);
			var customerList =$("#customerList");
			customerList.empty();
			for(var i = 0; i < data.length; i++){
				var company_name = data[i].COMPANY_NAME;
				if(company_name == null){
					company_name = '';
				}
				var contact_person = data[i].CONTACT_PERSON;
				if(contact_person == null){
					contact_person = '';
				}
				var phone = data[i].PHONE;
				if(phone == null){
					phone = '';
				}
				customerList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' payment='"+data[i].PAYMENT+"' partyId='"+data[i].PID+"' location='"+data[i].LOCATION+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');
		$("#customerList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#customerList').show();
	});

 	// 没选中客户，焦点离开，隐藏列表
	$('#customerMessage').on('blur', function(){
 		$('#customerList').hide();
 	});

	//当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
	$('#customerList').on('blur', function(){
 		$('#customerList').hide();
 	});

	$('#customerList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	
	// 选中客户
	$('#customerList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#customerMessage').val(message.substring(0, message.indexOf(" ")));
		$('#customerMessage').focus();
		$("#customer_id").val($(this).attr('partyId'));
		$('#customerList').hide();
    }); 
    
	$('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN'
	}).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
	    $('#beginTime').trigger('keyup');
	});		
	
	$('#datetimepicker2').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN', 
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime').focus();
	    $('#endTime').trigger('keyup');
	});
    
    
});
    