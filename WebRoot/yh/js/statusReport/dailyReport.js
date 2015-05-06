$(document).ready(function() {
    document.title = '运营日报表 | '+document.title;
    $('#menu_report').addClass('active').find('ul').addClass('in');
    $("#queryBtn").prop("disabled",true);
    
	//datatable, 动态处理
    var statusTable = $('#eeda-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"bServerSide": false,
    	"bLengthChange":true,
    	"bProcessing": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [   
            {"mDataProp":"ABBR", "sWidth":"100px", "bVisible":true},
            {"mDataProp":"DELIVERYNO", "sWidth":"100px","sClass": "delivery_no"},
            {"mDataProp":"TRANSFERNO", "sWidth":"100px"},
            {"mDataProp":"SERIAL_NO", "sWidth":"120px","sClass": "serial_no"},
            {"mDataProp":"STATUS", "sWidth":"100px"},       	
            {"mDataProp":"PLANNING_TIME", "sWidth":"150px"},
            {"mDataProp":"ORDER_TYPE", "sWidth":"100px"},
            {"mDataProp":"ROUTE_FROM", "sWidth":"80px"},
            {"mDataProp":"ROUTE_TO", "sWidth":"80px"},
            {"mDataProp":"WAREHOUSENUMBER", "sWidth":"100px","sClass": "warehouse_number"},
            {"mDataProp":"PIECES", "sWidth":"60px","sClass": "pieces"},
            {"mDataProp":"WEIGHT", "sWidth":"60px"},
            {"mDataProp":"VOLUME", "sWidth":"60px"},
            {"mDataProp":"YUNZUOMAOLI", "sWidth":"100px"},
            {"mDataProp":null, "sWidth":"80px",
            	"fnRender": function(obj) {  
            		if(obj.aData.ZONGSHOURU != 0 && obj.aData.ZONGSHOURU != ""){
            			var maolilv = ((obj.aData.YUNZUOMAOLI * 1) / (obj.aData.ZONGSHOURU * 1)).toFixed(2);
            			return (maolilv * 100) + "%";
            		}else{
            			return "";
            		}
                }
            },
            {"mDataProp":"PAYTIHUO", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"PAYGANXIAN", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"PAYPEISONG", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"PAYBAOXIAN", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"PAYANZHUANG", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"PAYTAIJIE", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"PAYDENGDAI", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"PAYZANCUN", "sWidth":"70px", "bVisible":false},
            {"mDataProp":null, "sWidth":"100px", "bVisible":false},
            {"mDataProp":"PAYQITA", "sWidth":"100px", "bVisible":false},
            {"mDataProp":null, "sWidth":"80px",
            	"fnRender": function(obj) {  
        			return (obj.aData.PAYTIHUO + obj.aData.PAYGANXIAN + obj.aData.PAYPEISONG + obj.aData.PAYBAOXIAN + obj.aData.PAYANZHUANG + obj.aData.PAYTAIJIE + obj.aData.PAYDENGDAI + obj.aData.PAYZANCUN + obj.aData.PAYQITA) * 1;
                }
            },
            {"mDataProp":"INCOMETIHUO", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"INCOMEYUNSHU", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"INCOMESONGHUO", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"INCOMEBAOXIAN", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"INCOMETAIJIE", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"INCOMEANZHUANG", "sWidth":"70px", "bVisible":false},
            {"mDataProp":"INCOMEZANCUN", "sWidth":"70px", "bVisible":false},
            {"mDataProp":null, "sWidth":"100px", "bVisible":false},
            {"mDataProp":"INCOMEQITA", "sWidth":"100px", "bVisible":false},
            {"mDataProp":"ZONGSHOURU", "sWidth":"80px"}
        ]
    });	
    
    //单品与普货查询
    $("input[name='cargoType']").change(function(){
    	if($(this).val() == "ATM"){
    		statusTable.fnSetColumnVis(1, true);
    		statusTable.fnSetColumnVis(3, true);
    		statusTable.fnSetColumnVis(9, true);
    		statusTable.fnSetColumnVis(10, true);
    	}else{
    		statusTable.fnSetColumnVis(1, false);
    		statusTable.fnSetColumnVis(3, false);
    		statusTable.fnSetColumnVis(9, false);
    		statusTable.fnSetColumnVis(10, false);
    	}
    });
    
    
    
    //成本隐藏与展开
    $("#payHandle").click(function(){
    	var type = $(this).attr("alt");
    	//0代表隐藏
    	if(type == 1){
    		statusTable.fnSetColumnVis(15, false);
    		statusTable.fnSetColumnVis(16, false);
    		statusTable.fnSetColumnVis(17, false);
    		statusTable.fnSetColumnVis(18, false);
    		statusTable.fnSetColumnVis(19, false);
    		statusTable.fnSetColumnVis(20, false);
    		statusTable.fnSetColumnVis(21, false);
    		statusTable.fnSetColumnVis(22, false);
    		statusTable.fnSetColumnVis(23, false);
    		statusTable.fnSetColumnVis(24, false);
    		$(this).attr("alt",0).text(">>");
    	}else{
    		statusTable.fnSetColumnVis(15, true);
    		statusTable.fnSetColumnVis(16, true);
    		statusTable.fnSetColumnVis(17, true);
    		statusTable.fnSetColumnVis(18, true);
    		statusTable.fnSetColumnVis(19, true);
    		statusTable.fnSetColumnVis(20, true);
    		statusTable.fnSetColumnVis(21, true);
    		statusTable.fnSetColumnVis(22, true);
    		statusTable.fnSetColumnVis(23, true);
    		statusTable.fnSetColumnVis(24, true);
    		$(this).attr("alt",1).text("<<");
    	}
    });
    
    //收入隐藏与展开
    $("#incomeHandle").click(function(){
    	var type = $(this).attr("alt");
    	//0代表隐藏
    	if(type == 1){
    		statusTable.fnSetColumnVis(26, false);
    		statusTable.fnSetColumnVis(27, false);
    		statusTable.fnSetColumnVis(28, false);
    		statusTable.fnSetColumnVis(29, false);
    		statusTable.fnSetColumnVis(30, false);
    		statusTable.fnSetColumnVis(31, false);
    		statusTable.fnSetColumnVis(32, false);
    		statusTable.fnSetColumnVis(33, false)
    		statusTable.fnSetColumnVis(34, false);
    		$(this).attr("alt",0).text(">>");
    	}else{
    		statusTable.fnSetColumnVis(26, true);
    		statusTable.fnSetColumnVis(27, true);
    		statusTable.fnSetColumnVis(28, true);
    		statusTable.fnSetColumnVis(29, true);
    		statusTable.fnSetColumnVis(30, true);
    		statusTable.fnSetColumnVis(31, true);
    		statusTable.fnSetColumnVis(32, true);
    		statusTable.fnSetColumnVis(33, true);
    		statusTable.fnSetColumnVis(34, true);
    		$(this).attr("alt",1).text("<<");
    	}
    });
    
    
    $("#beginTime,#endTime").on('keyup click', function () {
    	var beginTime=$("#beginTime").val();
    	var endTime=$("#endTime").val();
    	if((beginTime != "" && endTime != "")){
    		$("#queryBtn").prop("disabled",false);
    	}else{
    		$("#queryBtn").prop("disabled",true);
    	}
    });
    
    $("#queryBtn").on('click', function () {
    	var beginTime=$("#beginTime").val();
    	var endTime=$("#endTime").val();
    	var serial_no = $("#serial_no").val();
    	var order_no = $("#order_no").val();
    	var customer_id = $("#customer_id").val();
    	var customer_order_no = $("#customer_order_no").val();
    	var item_no = $("#item_no").val();
    	var cargoType = $("input[type='radio'][name='cargoType']:checked").val();
    	if((beginTime != "" && endTime != "")){
    		statusTable.fnSettings().oFeatures.bServerSide = true;
    		statusTable.fnSettings()._iDisplayStart = 0;
	    	statusTable.fnSettings().sAjaxSource = "/statusReport/dailyReportStatus?beginTime="+beginTime+"&endTime="+endTime+"&serial_no="+serial_no
	    		+"&order_no="+order_no+"&customer_id="+customer_id+"&customer_order_no="+customer_order_no+"&item_no="+item_no+"&cargoType="+cargoType;
	    	statusTable.fnDraw(); 
	    	/*$.get("/statusReport/dailyReportStatus?beginTime="+beginTime+"&endTime="+endTime+"&serial_no="+serial_no
		    		+"&order_no="+order_no+"&customer_id="+customer_id+"&customer_order_no="+customer_order_no+"&item_no="+item_no+"&cargoType="+cargoType, null, function(data){
	    		var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
				transferOrderMilestoneTbody.empty();
					
				//transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+location+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
				$.each( data.aaData, function(index, content){ 
					console.log( "数组#" + index + " 数组 value: " + content ); 
					var str = "<tr>";
					$.each( content, function(index, value){ 
						console.log( "item #" + name + " its value is: " + value ); 
						str += "<td>"+value+"</td>";
					});
					transferOrderMilestoneTbody.append(str+"</tr>");
				});  
				//statusTable.fnDraw();
	    	});*/
    	}
    });
    
    //获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup click', function(){
		if($('#customerMessage').val() == "")
			$("#customer_id").val("");
		$.get('/customerContract/search', {locationName:$('#customerMessage').val()}, function(data){
			console.log(data);
			var customerList =$("#customerList");
			customerList.empty();
			for(var i = 0; i < data.length; i++)
			{
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
	    $('#endTime').trigger('keyup');
	});
    
    
});
    
$(document).ready(function() {        
	$('#example').dataTable({        
		"ajax" : 'data.txt',        
		"columns": [        
            { "data": "name", "visible": false},        
            { "data": "position" },       
            { "data": "office" },       
            { "data": "extn" },       
            { "data": "start_date" },      
            { "data": "salary" }    
		                    
        ], 
        "columnDefs": [ 
            // 将Salary列变为红色
            { "targets": [5], 
            	// 目标列位置，下标从0开始 
            	"data": "salary", 
            	// 数据列名 
            	"render": function(data, type, full) { 
            		// 返回自定义内容 
            		return "" + data + ""; 
        		} 
            }]    
	});
});










