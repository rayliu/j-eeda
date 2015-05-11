$(document).ready(function() {
	
	var type = $("#type").val();
	if(type == "directSend"){
		$("#titleName").text("创诚易达物流系统-直送签收");
	}else if(type == "distribution"){
		$("#titleName").text("创诚易达物流系统-配送签收");
	}
 
	$("#searchNo").click(function(){  
		$.post('/wx/getRo',$("#returnFrom").serialize(),function(data){
			var returnId = data.ID;
			if(returnId > 0){
				$('#orderDesc').text('回单确认存在，请从相册中选择照片上传');
				$('#returnId').val(returnId);
				//初始化上传控件
				$('#fileupload').fileupload({
			    	autoUpload: true, //自选择后自动上传图片
			    	disableImageResize: /Android(?!.*Chrome)|Opera/.test(window.navigator && navigator.userAgent),
			        dataType: 'json',
			        url: '/wx/saveFile?return_id='+$("#returnId").val(),//上传地址
			        validation: {allowedExtensions: ['jpeg', 'jpg', 'png' ,'gif']},
			        imageMaxWidth: 1200,
			    	imageMaxHeight: 900,
			    	imageCrop: false, // 自动高宽比缩放
			        done: function (e, data) {
			        	if(data.result.result = "true"){
			        		$("#uploadBtn").text("上传图片");
			        		$("#uploadBtn").prop("disabled",false);
			        		$('#uploadDesc').append("<p>文件名："+data.result.cause+"  上传成功！</p>").show();
			        		console.log("data.result.cause:"+data.result.cause);
			        	}else{
			        		$("#centerBody").empty().append("<h4>"+data.result.cause+"</h4>");
			        	}
			        },  
			        progressall: function (e, data) {//设置上传进度事件的回调函数
			        	$("#uploadBtn").prop("disabled",true);
			        	$("#uploadBtn").text("上传中.....");
			        } 
				});
			}else{
				$('#orderDesc').text('回单不存在，请重新查询');
				$('#returnId').val("");
			}
			$('#orderDesc').show();
	    },'json');
	});

	//保存图片
    $("#uploadBtn").click(function(e){
    	if($("#returnId").val().length==0){
    		//alert('回单号码不存在，请重新查询.');
    		$("#uploadDesc").text("请先查询有效的回单号码").show();
    		return;
    	}
    	$("#fileupload").click();
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

	// 选中供应商
	$('#customerList').on('mousedown', '.fromLocationItem', function(e){
		$('#customerMessage').val($(this).text());
		$('#customerId').val($(this).attr('partyId'));
        $('#customerList').hide();
    }); 
	
	
});

