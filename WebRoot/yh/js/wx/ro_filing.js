$(document).ready(function() {
	
	var type = $("#type").val();
	if(type == "directSend"){
		$("#titleName").text("创诚易达物流系统-直送签收");
	}else if(type == "distribution"){
		$("#titleName").text("创诚易达物流系统-配送签收");
	}
 
	$("#searchNo").click(function(){  
		var orderNo = $("#orderNo").val();
		$.post('/wx/getRo',$("#returnFrom").serialize(),function(data){
	       if(data.ORDER_NO){
	          $('#orderDesc').text('回单确认存在，请从相册中选择照片上传');
	          $('#returnId').val(data.ID);
	          initFileupload();
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

    var initFileupload= function(){
	    $('#fileupload').fileupload({
	        dataType: 'json',
	        url: '/wx/saveFile?return_id='+$("#returnId").val(),//上传地址
	        done: function (e, data) {
	          if(data.result.result = "true"){
	        	  $("#uploadBtn").text("上传图片");
	        	  $('#uploadDesc').text('上传成功！').show();
	        	  $("#uploadBtn").prop("disabled",false);
	        	  //alert("上传成功！");
	        	  console.log("data.result.cause:"+data.result.cause);
	        	  //console.log("data.result.cause:"+data.result.cause+",parseJSON:"+$.parseJSON(data.result.cause));
	        	  var files = $.parseJSON(data.result.cause);
	        	  var showPictures = $("#showPictures");
	        	  showPictures.empty();
	        	  $.each(data.result.cause,function(name,value) {
	        		  showPictures.append('<div style="width:200px;height:210px;float:left;" ><img src="/upload/fileupload/'+value.FILE_PATH+'" alt="" class="imgSign" style="width:180px;height:180px;"><p><a class="picturedel" picture_id="'+value.ID+'" >删除</a></p></div>');
	        	  });
	          }else{
	            $("#centerBody").empty().append("<h4>"+data.result.cause+"</h4>");
	          }
	        },  
	        progressall: function (e, data) {//设置上传进度事件的回调函数
	        	$("#uploadBtn").prop("disabled",true);
	        	$("#uploadBtn").text("上传中.....");
	          //$.scojs_message('上传中', $.scojs_message.TYPE_OK);
	          //$('#myModal').modal('show');
	          //$("#footer").hide();
	        } 
	     });
    };
   
	
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

