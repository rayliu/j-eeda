$(document).ready(function() {
	
	$("#searchNo").click(function(){  
		$.post('/wx/findReturnOrder',$("#returnFrom").serialize(),function(data){
			var returnId = data.ID;
			if(returnId > 0){
				$('#orderDesc').text('回单确认存在，请从相册中选择照片上传');
				$("#uploadDesc").text("");
				$('#returnId').val(returnId);
				//图片上传
				$('#fileupload').fileupload({
			    	autoUpload: true, //自选择后自动上传图片
			    	disableImageResize: /Android(?!.*Chrome)|Opera/.test(window.navigator && navigator.userAgent),
			        dataType: 'json',
			        url: '/wx/saveFile?return_id='+$('#returnId').val(),//上传地址
			        validation: {allowedExtensions: ['jpeg', 'jpg', 'png' ,'gif']},
			        imageMaxWidth: 1200,
			    	imageMaxHeight: 900,
			    	imageCrop: false, // 自动高宽比缩放
			        done: function (e, data) {
			        	console.log("data.result.cause:"+data.result.cause);
			        	if(data.result.result == "true"){
			        		$('#uploadDesc').append("<p>文件名："+data.result.cause+"  上传成功!</p>").show();
			        	}else{
			        		$("#uploadDesc").empty().append("<p>"+data.result.cause+"</p>").show();
			        	}
			        	$("#uploadBtn").text("上传图片");
			    		$("#uploadBtn").prop("disabled",false);
			        },  
			        progressall: function (e, data) {//设置上传进度事件的回调函数
			        	$("#uploadBtn").prop("disabled",true);
			        	var progress = parseInt(data.loaded / data.total * 100, 10);
			            $('#uploadBtn').text("上传中(" + progress + "%)");
			        } 
				});
			}else{
				$('#orderDesc').text('请先查询有效的回单号码');
				$('#returnId').val("");
			}
			$('#orderDesc').show();
	    },'json');
	});

	//保存图片
    $("#uploadBtn").click(function(e){
    	wx.chooseImage({
		    count: 1, // 默认9
		    sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
		    sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
		    success: function (res) {
		        var localIds = res.localIds; // 返回选定照片的本地ID列表，localId可以作为img标签的src属性显示图片
		        $.post('/wx/saveReturnOrderPic',{json:res},function(data){
		        	if(data)
		        		$('#uploadDesc').append("<p>图片上传成功!</p>").show();
		        });
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

