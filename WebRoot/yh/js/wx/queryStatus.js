$(document).ready(function() {
	
	$('#orderNo').focus();
	
	$("#searchNo").click(function(){
		$("#searchNo").attr('disabled',true);
		$('#massage').empty();
		if($('#orderNo').val() == ''){
    		$('#orderDesc').text('请先输入号码');
    		$('#orderDesc').show();
    		return;
    	}
		
		//加载中
		var $loadingToast = $('#loadingToast');
        if ($loadingToast.css('display') != 'none') {
            return;
        }
        $loadingToast.show();
		$.post('/wx/queryStatusJson',$("#returnFrom").serialize(), function(data){
			 $loadingToast.hide();
			$("#searchNo").attr('disabled',false);
			var orderNo=$("#orderNoList")
			orderNo.empty();
			if(data.length>0){	
				data.forEach(function(da){
					var TRANSFER_ORDER_NO = da.TRANSFER_ORDER;
					var PICKUP_ORDER_NO = da.PICKUP_ORDER;
					var DEPART_ORDER_NO = da.DEPART_ORDER;
					var DELIVERY_ORDER_NO = da.DELIVERY_ORDER;
					var RETURN_ORDER_NO = da.RETURN_ORDER;
					if(RETURN_ORDER_NO)
						$('#massage').append('<label class="weui_cell weui_check_label" style="padding: 0px 15px;">'
								
				                +'<div class="weui_cell_bd weui_cell_primary">'
				                +'<p align="left" style="font-size: 10px;">'+RETURN_ORDER_NO.substring(RETURN_ORDER_NO.indexOf('-')+1,RETURN_ORDER_NO.length)+'</p>'
				                +'</div>'
				                +'<div style="background: url(/yh/img/connector_start.png) no-repeat;height: 43px;width: 40px;"></div>'
				                +'<div class="weui_cell_bd weui_cell_primary">'
				                +'<p align="left" style="font-size: 11px;">'+RETURN_ORDER_NO.substring(0, RETURN_ORDER_NO.indexOf('-'))+'</p>'
				                +'</div>'
				                +'</label>');
					if(DELIVERY_ORDER_NO)
			        	$('#massage').append('<label class="weui_cell weui_check_label" style="padding: 0px 15px;">'
			        			
				                +'<div class="weui_cell_bd weui_cell_primary">'
				                +'<p align="left" style="font-size: 10px;">'+DELIVERY_ORDER_NO.substring(DELIVERY_ORDER_NO.indexOf('-')+1,DELIVERY_ORDER_NO.length)+'</p>'
				                +'</div>'
				                +'<div style="background: url(/yh/img/connector.png) no-repeat;height: 43px;width: 40px;"></div>'
				                +'<div class="weui_cell_bd weui_cell_primary">'
				                +'<p align="left" style="font-size: 11px;">'+DELIVERY_ORDER_NO.substring(0, DELIVERY_ORDER_NO.indexOf('-'))+'</p>'
				                +'</div>'
				                +'</label>');
			        if(DEPART_ORDER_NO)
						$('#massage').append('<label class="weui_cell weui_check_label" style="padding: 0px 15px;">'
								
				                +'<div class="weui_cell_bd weui_cell_primary">'
				                +'<p align="left" style="font-size: 10px;">'+DEPART_ORDER_NO.substring(DEPART_ORDER_NO.indexOf('-')+1,DEPART_ORDER_NO.length)+'</p>'
				                +'</div>'
				                +'<div style="background: url(/yh/img/connector.png) no-repeat;height: 43px;width: 40px;"></div>'
				                +'<div class="weui_cell_bd weui_cell_primary">'
				                +'<p align="left" style="font-size: 11px;">'+DEPART_ORDER_NO.substring(0, DEPART_ORDER_NO.indexOf('-'))+'</p>'
				                +'</div>'
				                +'</label>');
					if(PICKUP_ORDER_NO)
						$('#massage').append('<label class="weui_cell weui_check_label" style="padding: 0px 15px;">'
								
				                +'<div class="weui_cell_bd weui_cell_primary">'
				                +'<p align="left" style="font-size: 10px;">'+PICKUP_ORDER_NO.substring(PICKUP_ORDER_NO.indexOf('-')+1,PICKUP_ORDER_NO.length)+'</p>'
				                +'</div>'
				                +'<div style="background: url(/yh/img/connector.png) no-repeat;height: 43px;width: 40px;"></div>'
				                +'<div class="weui_cell_bd weui_cell_primary">'
				                +'<p align="left" style="font-size: 11px;">'+PICKUP_ORDER_NO.substring(0, PICKUP_ORDER_NO.indexOf('-'))+'</p>'
				                +'</div>'
				                +'</label>');


					if(TRANSFER_ORDER_NO)
						$('#massage').append('<label class="weui_cell weui_check_label" style="padding: 0px 15px;">'
				                +'<div class="weui_cell_bd weui_cell_primary">'
				                +'<p align="left" style="font-size: 10px;">'+TRANSFER_ORDER_NO.substring(TRANSFER_ORDER_NO.indexOf('-')+1,TRANSFER_ORDER_NO.length)+'</p>'
				                +'</div>'
				                +'<div style="background: url(/yh/img/connector_end.png) no-repeat;height: 43px;width: 40px;"></div>'
				                +'<div class="weui_cell_bd weui_cell_primary">'
				                +'<p align="left" style="font-size: 11px;">'+TRANSFER_ORDER_NO.substring(0, TRANSFER_ORDER_NO.indexOf('-'))+'</p>'
				                +'</div>'
				                +'</label>');
					
					
			        
					
					

				})	
			}
			else{
				orderNo.append("<h2 class='title'>没有找到此单据信息</h2>");
			}
	    },'json');
	});

	//保存图片
    $("#uploadBtn").click(function(e){
    	if($('#returnId').val() == ''){
    		$('#orderDesc').text('请先查找对应有效的回单号码');
    		$('#orderDesc').show();
    		return;
    	}
    	wx.chooseImage({
		    count: 1, // 默认9
		    sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
		    sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
		    success: function (res) {
		        var localIds = res.localIds; // 返回选定照片的本地ID列表，localId可以作为img标签的src属性显示图片
		        if(localIds){
		        	var localId = localIds[0];

		        	wx.uploadImage({
					    localId: localId, // 需要上传的图片的本地ID，由chooseImage接口获得
					    isShowProgressTips: 1, // 默认为1，显示进度提示
					    success: function (res) {
					        var serverId = res.serverId; // 返回图片的服务器端ID
					        $.post('/wx/saveReturnOrderPic',{serverId:serverId, return_order_id: $('#returnId').val()},function(data){
					        	if(data == "OK"){
					        		$('#uploadDesc').empty().append("<p>图片上传成功!</p>").show();
					        	}else{
					        		$('#uploadDesc').empty().append("<p>图片上传失败!</p>").show();
					        	}
					        });
					    }
					});
		        }
		    }
		});
    });

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

