$(document).ready(function() {
    document.title = '运营日报表 | '+document.title;
    $('#menu_report').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var statusTable = $('#eeda-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 20,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": false,
    	"bLengthChange":true,
    	"bProcessing": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
        "aoColumns": [   
            {"mDataProp":"ABBR", "sWidth":"60px", "bVisible":true},
            {"mDataProp":"DELIVERYNO", "sWidth":"100px","sClass": "delivery_no"},
            {"mDataProp":"TRANSFERNO", "sWidth":"60px"},
            {"mDataProp":"SERIAL_NO", "sWidth":"40px","sClass": "serial_no"},
            {"mDataProp":"STATUS", "sWidth":"50px"},       	
            {"mDataProp":"PLANNING_TIME", "sWidth":"80px"},
            {"mDataProp":"ORDER_TYPE", "sWidth":"100px"},
            {"mDataProp":"ROUTE_FROM", "sWidth":"60px"},
            {"mDataProp":"ROUTE_TO", "sWidth":"60px"},
            {"mDataProp":null, "sWidth":"20px"},
            {"mDataProp":"PIECES", "sWidth":"20px","sClass": "pieces"},
            {"mDataProp":"WEIGHT", "sWidth":"40px",
        		"fnRender": function(obj) {
        			return "<p align='right'>"+parseFloat(obj.aData.WEIGHT).toFixed(2)+"</p>";
        	}},
            {"mDataProp":"VOLUME", "sWidth":"40px",
        		"fnRender": function(obj) {
        			return "<p align='right'>"+parseFloat(obj.aData.VOLUME).toFixed(2)+"</p>";
        	}},
            {"mDataProp":"YZ_AMOUNT", "sWidth":"40px",
            		"fnRender": function(obj) {
            			return "<p align='right'>"+parseFloat(obj.aData.YZ_AMOUNT).toFixed(2)+"</p>";
            	}
            },
            {"mDataProp":"MAOLILV", "sWidth":"40px",
        		"fnRender": function(obj) {
        			return "<p align='right'>"+parseFloat(obj.aData.MAOLILV*100).toFixed(2)+"%"+"</p>";
           }},
            {"mDataProp":"YF_PICKUP", "sWidth":"40px", "bVisible":false,
            		"fnRender": function(obj) {
            			return "<p align='right'>"+parseFloat(obj.aData.YF_PICKUP).toFixed(2)+"</p>";
               }
            },
            {"mDataProp":"YF_DEPART", "sWidth":"40px", "bVisible":false,
            		"fnRender": function(obj) {
            			return "<p align='right'>"+parseFloat(obj.aData.YF_DEPART).toFixed(2)+"</p>";
            	}
            },
            {"mDataProp":"DELIVERY", "sWidth":"40px", "bVisible":false,
                	"fnRender": function(obj) {
                		return "<p align='right'>"+parseFloat(obj.aData.DELIVERY).toFixed(2)+"</p>";
                }
            },
            {"mDataProp":"YF_INSURANCE", "sWidth":"40px", "bVisible":false,
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.YF_INSURANCE).toFixed(2)+"</p>";
            }},
            {"mDataProp":null, "sWidth":"70px", "bVisible":false},
            {"mDataProp":"YF_SUM", "sWidth":"60px",
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.YF_SUM).toFixed(2)+"</p>";
            }},
            {"mDataProp":"YS_INSURANCE", "sWidth":"40px", "bVisible":false,
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.YS_INSURANCE).toFixed(2)+"</p>";
            }},
            {"mDataProp":"RETURN_AMOUNT", "sWidth":"40px", "bVisible":false,
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.RETURN_AMOUNT).toFixed(2)+"</p>";
            }},
            {"mDataProp":null, "sWidth":"70px", "bVisible":false},
            {"mDataProp":"YS_SUM", "sWidth":"60px",
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.YS_SUM).toFixed(2)+"</p>";
            }}
        ]
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
    		$(this).attr("alt",0).text(">>");
    	}else{
    		statusTable.fnSetColumnVis(15, true);
    		statusTable.fnSetColumnVis(16, true);
    		statusTable.fnSetColumnVis(17, true);
    		statusTable.fnSetColumnVis(18, true);
    		statusTable.fnSetColumnVis(19, true);
    		$(this).attr("alt",1).text("<<");
    	}
    });
    
    //收入隐藏与展开
    $("#incomeHandle").click(function(){
    	var type = $(this).attr("alt");
    	//0代表隐藏
    	if(type == 1){
    		statusTable.fnSetColumnVis(21, false);
    		statusTable.fnSetColumnVis(22, false);
    		statusTable.fnSetColumnVis(23, false);
    		$(this).attr("alt",0).text(">>");
    	}else{
    		statusTable.fnSetColumnVis(21, true);
    		statusTable.fnSetColumnVis(22, true);
    		statusTable.fnSetColumnVis(23, true);
    		$(this).attr("alt",1).text("<<");
    	}
    });
    
    
    $("#queryBtn").on('click', function () {
    	var beginTime = $("#start_date").val();
        var endTime = $("#end_date").val();
    	var order_no = $("#order_no").val();
    	var customer_id = $("#customer_id").val();
    	var routeTo = $("#routeTo").val();
    	var serialNo = $("#serialNo").val();
    	var receive = $("[name=receive]").prop('checked');
    	var noreceive = $("[name=noreceive]").prop('checked');
    	var inventory = $("[name=inventory]").prop('checked');
    	
		statusTable.fnSettings().oFeatures.bServerSide = true;
		statusTable.fnSettings()._iDisplayStart = 0;
    	statusTable.fnSettings().sAjaxSource = "/statusReport/dailyReportStatus?beginTime="+beginTime+"&endTime="+endTime+"&order_no="+order_no
    											+"&customer_id=" +customer_id+"&route_to="+routeTo+"&serial_no="+serialNo
    											+"&receive=" +receive+"&noreceive="+noreceive+"&inventory="+inventory;
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
    });
    
    //获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup click', function(){
		if($('#customerMessage').val() == "")
			$("#customer_id").val("");
		$.get('/statusReport/search', {locationName:$('#customerMessage').val()}, function(data){
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










