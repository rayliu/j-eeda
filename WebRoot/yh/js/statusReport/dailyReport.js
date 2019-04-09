$(document).ready(function() {
    document.title = '运营日报表 | '+document.title;
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
            {"mDataProp":"ABBR", "sWidth":"60px", "bVisible":true,"sClass":'abbr'},
            {"mDataProp":"TRANSFERNO", "sWidth":"60px","sClass":'transferno'},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"60px","sClass":'customer_order_no'},
            {"mDataProp":"DELIVERYNO", "sWidth":"100px","sClass": "delivery_no"},
            {"mDataProp":"SERIAL_NO", "sWidth":"40px","sClass": "serial_no"},
            {"mDataProp":"STATUS", "sWidth":"50px","sClass":'status'},       	
            {"mDataProp":"PLANNING_TIME", "sWidth":"80px","sClass":'planning_time'},
            {"mDataProp":"ORDER_TYPE", "sWidth":"100px","sClass":'order_type'},
            {"mDataProp":"ROUTE_FROM", "sWidth":"60px","sClass":'route_from'},
            {"mDataProp":"TRANSIT_PLACE", "sWidth":"100px","sClass":'transit_place'},
            {"mDataProp":"ROUTE_TO", "sWidth":"60px","sClass":'route_to'},//10
            {"mDataProp":"FULL_ADDRESS", "sWidth":"180px","sClass":'full_address'},
            {"mDataProp":"ITEM_NO", "sWidth":"60px","sClass":'item_no'},
            {"mDataProp":"PIECES", "sWidth":"20px","sClass": "pieces"},
            {"mDataProp":"WEIGHT", "sWidth":"40px","sClass":"weight",
        		"fnRender": function(obj) {
        			return "<p align='right'>"+parseFloat(obj.aData.WEIGHT).toFixed(2)+"</p>";
        		}
            },
            {"mDataProp":"VOLUME", "sWidth":"40px","sClass":'volume',
        		"fnRender": function(obj) {
        			return "<p align='right'>"+parseFloat(obj.aData.VOLUME).toFixed(2)+"</p>";
        		}
        	},//15
        	{"mDataProp":"F_STATUS", "sWidth":"60px","sClass":'f_status'},
            {"mDataProp":"YZ_AMOUNT", "sWidth":"40px","sClass":'yz_amount',
            		"fnRender": function(obj) {
            			return "<p align='right'>"+parseFloat(obj.aData.YZ_AMOUNT).toFixed(2)+"</p>";
            	}
            },
            {"mDataProp":"MAOLILV", "sWidth":"40px","sClass":'maolilv',
        		"fnRender": function(obj) {
        			return "<p align='right'>"+parseFloat(obj.aData.MAOLILV*100).toFixed(2)+"%"+"</p>";
        		}
            },
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
            },//20
            {"mDataProp":"YF_INSURANCE", "sWidth":"40px", "bVisible":false,
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.YF_INSURANCE).toFixed(2)+"</p>";
            	}
            },
            {"mDataProp":null, "sWidth":"70px", "bVisible":false},
            {"mDataProp":"YF_SUM", "sWidth":"60px","sClass":'yf_sum',
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.YF_SUM).toFixed(2)+"</p>";
            	}
            },
            {"mDataProp":"YS_INSURANCE", "sWidth":"40px", "bVisible":false,
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.YS_INSURANCE).toFixed(2)+"</p>";
            	}
            },
            {"mDataProp":"RETURN_AMOUNT", "sWidth":"40px", "bVisible":false,
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.RETURN_AMOUNT).toFixed(2)+"</p>";
            	}
            },
            {"mDataProp":null, "sWidth":"70px", "bVisible":false},//25
            {"mDataProp":"YS_SUM", "sWidth":"60px","sClass":'ys_sum',
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.YS_SUM).toFixed(2)+"</p>";
            	}
            }
        ]
    });	  
    //成本隐藏与展开
    $("#payHandle").click(function(){
    	var type = $(this).attr("alt");
    	//0代表隐藏
    	if(type == 1){
    		
    		
    		statusTable.fnSetColumnVis(19, false);
    		statusTable.fnSetColumnVis(20, false);
    		statusTable.fnSetColumnVis(21, false);
    		statusTable.fnSetColumnVis(22, false);
    		statusTable.fnSetColumnVis(23, false);
    		$(this).attr("alt",0).text(">>");
    	}else{
    		
    		
    		statusTable.fnSetColumnVis(19, true);
    		statusTable.fnSetColumnVis(20, true);
    		statusTable.fnSetColumnVis(21, true);
    		statusTable.fnSetColumnVis(22, true);
    		statusTable.fnSetColumnVis(23, true);
    		$(this).attr("alt",1).text("<<");
    	}
    });
    
    //收入隐藏与展开
    $("#incomeHandle").click(function(){
    	var type = $(this).attr("alt");
    	//0代表隐藏
    	if(type == 1){
    		
    		
    		statusTable.fnSetColumnVis(24, false);
    		statusTable.fnSetColumnVis(25, false);
    		statusTable.fnSetColumnVis(26, false);
    		statusTable.fnSetColumnVis(27, false);
    		$(this).attr("alt",0).text(">>");
    	}else{
    		
    		
    		statusTable.fnSetColumnVis(24, true);
    		statusTable.fnSetColumnVis(25, true);
    		statusTable.fnSetColumnVis(26, true);
    		statusTable.fnSetColumnVis(27, true);
    		$(this).attr("alt",1).text("<<");
    	}
    });
    
    $("#exportBtn").on('click', function () {
        if($("#eeda-table").find(".dataTables_empty").length>0){
            $.scojs_message('导出内容不能为空', $.scojs_message.TYPE_FALSE);
            return false;
        }
        var self = this;
        self.disabled = true;
        $(self).html("导出中....");
        var data = {}
        data["beginTime"] = $.trim($("#start_date").val());
        data["endTime"] = $.trim($("#end_date").val());
        data["complete_time_begin_time"] = $.trim($("#complete_time_begin_time").val());
        data["complete_time_end_time"] = $.trim($("#complete_time_end_time").val());
        data["order_no"] = $.trim($("#order_no").val());
        data["customer_id"] = $.trim($("#customer_id").val());
        data["routeTo"] = $.trim($("#routeTo").val());
        data["serialNo"] = $.trim($("#serialNo").val());
        data["trans_type"] = $.trim($("#trans_type").val());
        data["receive"] = $.trim($("[name=receive]").prop('checked'));
        data["noreceive"] = $.trim($("[name=noreceive]").prop('checked'));
        data["inventory"] = $.trim($("[name=inventory]").prop('checked'));

        var field_list = [];
        $("#eeda-table th[role=columnheader]").each(function(){
            var value_={};
            var title = $(this).text();
            var value = $(this).attr("class").replace("sorting_disabled ","");
            value_["title"] = title;
            value_["value"] = value;
            field_list.push(value_);

        });
        data["field_list"] = field_list;
        $.post("/statusReport/export",{params:JSON.stringify(data)},function(data){
            self.disabled = false;
            $(self).html("导出");
            if(data){
                window.open(data);
                setTimeout(deleteFile(data),10000);
            }
        }).fail(function(){
            self.disabled = false;
        });
    });

    function deleteFile(file_name){
        $.post("/statusReport/deleteReport",{file_name:file_name},function(data){
            console.log(data);
        });
    }
    
    $("#queryBtn").on('click', function () {
    	var beginTime = $.trim($("#start_date").val());
        var endTime = $.trim($("#end_date").val());
        var complete_time_begin_time = $.trim($("#complete_time_begin_time").val());
        var complete_time_end_time = $.trim($("#complete_time_end_time").val());
    	var order_no = $.trim($("#order_no").val());
    	var customer_id = $.trim($("#customer_id").val());
    	var routeTo = $.trim($("#routeTo").val());
    	var serialNo = $.trim($("#serialNo").val());
    	var trans_type = $.trim($("#trans_type").val());
    	var receive = $.trim($("[name=receive]").prop('checked'));
    	var noreceive = $.trim($("[name=noreceive]").prop('checked'));
    	var inventory = $.trim($("[name=inventory]").prop('checked'));
    	
    	 var flag = false;
	        $('#statusForm input,#statusForm select').each(function(){
	        	 var textValue = $.trim(this.value);
	        	 if(textValue != '' && textValue != null){
	        		 if(this.name=="receive"){
	        			 return true;
	        		 }else if(this.name=="noreceive"){
	        			 return true;
	        		 }else if(this.name == "inventory"){
	        			 return true;
	        		 }
	        		 flag = true;
	        		 return;
	        	 } 
	        });
	        if(!flag){
	        	 $.scojs_message('请输入至少一个查询条件', $.scojs_message.TYPE_FALSE);
	        	 return false;
	        }
    	
    	/*if(customer_id == ''){
    		$.scojs_message('请选择客户', $.scojs_message.TYPE_ERROR);
    		return false;
    	}*/
    	
		statusTable.fnSettings().oFeatures.bServerSide = true;
		statusTable.fnSettings()._iDisplayStart = 0;
    	statusTable.fnSettings().sAjaxSource = "/statusReport/dailyReportStatus?beginTime="+beginTime+"&endTime="+endTime+"&order_no="+order_no
    											+"&customer_id=" +customer_id+"&route_to="+routeTo+"&serial_no="+serialNo
    											+"&receive=" +receive+"&noreceive="+noreceive+"&inventory="+inventory+"&order_type=daily"
    											+"&trans_type="+trans_type+"&complete_time_begin_time="+complete_time_begin_time+"&complete_time_end_time="+complete_time_end_time;
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










