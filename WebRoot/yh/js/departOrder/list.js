 $(document).ready(function() {
	$('#menu_assign').addClass('active').find('ul').addClass('in');
var dataTable =$('#dataTables-example').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "bFilter": false, //不需要默认的搜索框
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/departOrder/list",
        "aoColumns": [   
           
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                	return "<a href='/yh/departOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.DEPART_NO+"</a>";
                }
            } ,
            {"mDataProp":"CONTACT_PERSON"},
            {"mDataProp":"PHONE"},
            {"mDataProp":"CAR_NO"},
            {"mDataProp":"CARTYPE"},     
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"DEPART_STATUS"},
            {"mDataProp":"TRANSFER_ORDER_NO"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",               
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-danger cancelbutton' href=' /yh/departOrder/cancel/"+obj.aData.DEPART_ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "取消"+
                            "</a>";
                }
            } 
        ]      
    });
	$('#endTime_filter ,#beginTime_filter ,#sp_filter ,#status_filter ,#orderNo_filter ,#departNo_filter').on( 'keyup click', function () {
		var orderNo = $("#orderNo_filter").val();
		var departNo_filter = $("#departNo_filter").val();
		var status = $("#status_filter").val();
		var sp = $("#sp_filter").val();
		var beginTime = $("#beginTime_filter").val();
		var endTime = $("#endTime_filter").val();
		dataTable.fnSettings().sAjaxSource = "/yh/departOrder/list?orderNo="+orderNo+"&departNo="+departNo_filter+"&status="+status+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime;
		dataTable.fnDraw();
	} );
	
	$('#datetimepicker').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN'
	}).on('changeDate', function(ev){
	    $('#beginTime_filter').trigger('keyup');
	});
	
	
	$('#datetimepicker2').datetimepicker({  
	    format: 'yyyy-MM-dd',  
	    language: 'zh-CN', 
	    autoclose: true,
	    pickerPosition: "bottom-left"
	}).on('changeDate', function(ev){
	    $('#endTime_filter').trigger('keyup');
	});
	
	
	
	//获取供应商的list，选中信息在下方展示其他信息
	$('#sp_filter').on('keyup click', function(){
			var inputStr = $('#sp_filter').val();
			if(inputStr == ""){
				var pageSpName = $("#pageSpName");
				pageSpName.empty();
				var pageSpAddress = $("#pageSpAddress");
				pageSpAddress.empty();
				$('#sp_id').val($(this).attr(''));
			}
			$.get('/yh/transferOrder/searchSp', {input:inputStr}, function(data){
				console.log(data);
				var spList =$("#spList");
				spList.empty();
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
					spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+" "+contact_person+" "+phone+"</a></li>");
				}
			},'json');

			$("#spList").css({ 
	        	left:$(this).position().left+"px", 
	        	top:$(this).position().top+32+"px" 
	        }); 
	        $('#spList').show();
		});

		// 没选中供应商，焦点离开，隐藏列表
		$('#sp_filter').on('blur', function(){
	 		$('#spList').hide();
	 	});

		//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
		$('#spList').on('blur', function(){
	 		$('#spList').hide();
	 	});

		$('#spList').on('mousedown', function(){
			return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
		});

		// 选中供应商
		$('#spList').on('mousedown', '.fromLocationItem', function(e){
			console.log($('#spList').is(":focus"));
			var message = $(this).text();
			$('#sp_filter').val(message.substring(0, message.indexOf(" ")));
			$('#sp_id').val($(this).attr('partyId'));
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			pageSpAddress.append($(this).attr('address'));
			var contact_person = $(this).attr('contact_person');
			if(contact_person == 'null'){
				contact_person = '';
			}
			pageSpName.append(contact_person+'&nbsp;');
			var phone = $(this).attr('phone');
			if(phone == 'null'){
				phone = '';
			}
			pageSpName.append(phone); 
			pageSpAddress.empty();
			var address = $(this).attr('address');
			if(address == 'null'){
				address = '';
			}
			pageSpAddress.append(address);
	        $('#spList').hide();
	        
	        var orderNo = $("#orderNo_filter").val();
			var departNo_filter = $("#departNo_filter").val();
			var status = $("#status_filter").val();
			var sp = $("#sp_filter").val();
			var beginTime = $("#beginTime_filter").val();
			var endTime = $("#endTime_filter").val();
			dataTable.fnSettings().sAjaxSource = "/yh/departOrder/list?orderNo="+orderNo+"&departNo="+departNo_filter+"&status="+status+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime;
			dataTable.fnDraw();
	        
	    });
	

});