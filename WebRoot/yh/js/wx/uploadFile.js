$(document).ready(function() {
	
	$('#orderNo').focus();
	
	$("#searchNo").click(function(){
		if($('#orderNo').val()=='')
			return;
		
		$("#customer").hide();
		$("#customerNo").text("");
		$("#transferOrderNo").text("");
		$("#refNo").text("");
		$("#serialNo").text("");
		$("#returnOrderNo").text("");
        refreshData(-1);
    });
	var refreshData=function(customer){  
		var orderNo=$("#orderNo").val();
		$.post('/wx/findReturnOrder',{orderNo:orderNo,customer:customer}, function(data){
			if(data.length ==1){
				var returnId = data[0].ID;
				$("#uploadBtn").attr("class", "weui_btn weui_btn_primary");
				$("#uploadBtn").attr("disabled", false);
				$("#uploadDesc").text("");
				$('#returnId').val(returnId);
				$('#order_status').val(data[0].STATUS);
				$('#depart_id').val(data[0].DEPART_ID);
				$('#dep_status').val(data[0].DEP_STATUS);
				$('#delivery_id').val(data[0].DELIVERY_ID);
				$('#order_status').val(data[0].DEL_STATUS);
				
				$("#customerNo").text(data[0].CUSTOMER_ORDER_NO);
				$("#transferOrderNo").text(data[0].TO_ORDER_NO);
				$("#refNo").text(data[0].REF_NO);
				$("#serialNo").text(data[0].SERIAL_NO);
				$("#returnOrderNo").text(data[0].ORDER_NO);
			}else if(data.length >1) {
				$("#customer").show()
				var selectCustomer =$("#selectCustomer");
				selectCustomer.empty();
				selectCustomer.append("<option value='-1'></option>");
				$.each(data,function(n,value) {
					selectCustomer.append("<option value='"+value.CID+"'>"+value.ABBR+"</option>");
			    });  
				$("#uploadBtn").attr("class", "weui_btn weui_btn_disabled");
				$("#uploadBtn").attr("disabled", true);
			}else{
				$('#orderDesc').text('未找到对应有效单据');
				$('#returnId').val("");
				$("#uploadBtn").attr("class", "weui_btn weui_btn_disabled");
				$("#uploadBtn").attr("disabled", true);
				$("#customer").hide();
			}
			$('#orderDesc').show();
	    },'json');
	};
	$('#selectCustomer').change(function(){ 
		var customer=$("#selectCustomer").val();
		if(customer=="-1"){
			$('#orderDesc').hide();
			$("#uploadBtn").attr("class", "weui_btn weui_btn_disabled");
			$("#uploadBtn").attr("disabled", true);
		}else{
			$("#uploadBtn").attr("class", "weui_btn weui_btn_primary");
			$("#uploadBtn").attr("disabled", false);
			refreshData(customer);
		}
	}); 
	
	//保存图片
    $("#uploadBtn").click(function(e){
    	var order_status = $('#order_status').val();
    	var return_id = $('#returnId').val();
    	var depart_id = $('#depart_id').val();
        var	dep_status = $('#dep_status').val();
    	var delivery_id = $('#delivery_id').val();
    	var del_status = $('#del_status').val();
    	var userId = $('#userId').val();

    	wx.chooseImage({
		    count: 1, // 默认9
		    sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
		    sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
		    success: function (res) {
		        var localIds = res.localIds; // 返回选定照片的本地ID列表，localId可以作为img标签的src属性显示图片
		        if(localIds){
		        	var localId = localIds[0];

		        	if(return_id == ''){
		        		if(order_status == "运输在途"){
		        			$.post('/transferOrderMilestone/receipt',{departOrderId:depart_id,userId:userId,order_type:'wx'},function(data){
		            			if(data > 0){
		            				$('#returnId').val(data);
		            				uploadPhoto(data,localId);
		            			}
		            		})
		        		}else if(order_status == "配送在途"){
		        			$.post('/deliveryOrderMilestone/receipt',{delivery_id:delivery_id,userId:userId,order_type:'wx'},function(data){
		        				if(data>0){
		        					$('#returnId').val(data);
		            				uploadPhoto(data,localId);
		            			}
		            		})
		        		}	
		        	}else{
		        		uploadPhoto(return_id,localId);
		        	}
		        }
		    }
		});
    });
    
    
    //上传图片
    var uploadPhoto = function(return_id,localId){
    	wx.uploadImage({
		    localId: localId, // 需要上传的图片的本地ID，由chooseImage接口获得
		    isShowProgressTips: 1, // 默认为1，显示进度提示
		    success: function (res) {
		        var serverId = res.serverId; // 返回图片的服务器端ID
		        $.post('/wx/saveReturnOrderPic', 
		        	{
		        		serverId:serverId, 
		        		return_order_id: return_id, 
		        		photo_type: $('#photo_type_input').val()
		        	},
		        	function(data){
		        	if(data == "OK"){
		        	   var $toast = $('#toast');
	        	       if ($toast.css('display') != 'none') {
	        	           return;
	        	       }
	        	       $toast.show();
	        	       setTimeout(function () {
	        	           $toast.hide();
	        	           refreshData(-1);
	        	       }, 2000);   
		        	}else{
		        	   var $toast = $('#error');
	        	       if ($toast.css('display') != 'none') {
	        	           return;
	        	       }
	        	       $toast.show();
	        	       setTimeout(function () {
	        	           $toast.hide();
	        	       }, 2000);
		        	}
		        });
		    }
		});
    }
    
	//获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup click', function(){
		var inputStr = $('#customerMessage').val();
		if(inputStr == ""){
			$('#customerId').val("");
		}
		$.get('/wx/findAllCustomer', {input:inputStr}, function(data){
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
				customerList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"'>"+company_name+"</a></li>");
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

	//选中供应商
	$('#customerList').on('mousedown', '.fromLocationItem', function(e){
		$('#customerMessage').val($(this).text());
		$('#customerId').val($(this).attr('partyId'));
        $('#customerList').hide();
    }); 
	
	//更多选项
    $("#helpBlock").click(function(e){
    	if($("#distributionDiv").is(":hidden"))
    		$("#distributionDiv").show();
    	else
    		$("#distributionDiv").hide();
    });
	
	
});

