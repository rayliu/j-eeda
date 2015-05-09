$(document).ready(function() {
	var uploadImgWidth;
	var uploadImgHeight;
	

	$('#file_upload_form').fileupload({
		    // Uncomment the following to send cross-domain cookies:
		    //xhrFields: {withCredentials: true},
		    disableImageResize: false,
		    url: '/returnOrder/saveFile?return_id='+$("#returnId").val()+'&permission='+$("#permission").val(),
		    validation: {allowedExtensions: ['jpeg', 'jpg', 'png' ,'gif']},
		   
		    imageMaxWidth: 1200,
		    imageMaxHeight: 900,
		    imageCrop: false, // 自动高宽比缩放
		    /*progressall: function (e, data) {//设置上传进度事件的回调函数  
		    	console.log("uploadImgWidth:"+uploadImgWidth+",uploadImgHeight:"+uploadImgHeight);
		    }*/ 
		    done: function (e, data) {
		    	if(data.result.result == "true"){
		    		//$("#centerBody").empty().append("<h4>上传成功！</h4>");
		    		$.scojs_message('上传成功,审核通过后将显示图像', $.scojs_message.TYPE_OK);
		    		//console.log("data.result.cause:"+data.result.cause);
		    		var showPictures = $("#showPictures");
		    		var permission = $("#permission").val();
		    		if(permission == "permissionYes"){
		    			showPictures.empty().append('<input type="hidden" id="permission" value="permissionYes">');
		    			$.each(data.result.cause,function(name,value) {
		    				var aText = "待审核";
		    				if(value.AUDIT == 1 || value.AUDIT == true)
		    					aText = "已审核";
		    				showPictures.append('<div style="width:200px;height:210px;float:left;" ><img src="/upload/img/'+value.FILE_PATH+'" alt="" class="imgSign" style="width:180px;height:180px;"><p><a class="picture_audit" picture_id="'+value.ID+'" > ' + aText + ' </a><a class="picture_del" picture_id="'+value.ID+'" > 删除 </a></p></div>');
		                });
		    		}else{
		    			showPictures.empty().append('<input type="hidden" id="permission" value="permissionNo">');
		    			$.each(data.result.cause,function(name,value) {
		    				showPictures.append('<div style="width:200px;height:210px;float:left;" ><img src="/upload/img/'+value.FILE_PATH+'" alt="" class="imgSign" style="width:180px;height:180px;"></div>');
		                });
		    		}
		    	}else{
		    		$.scojs_message(data.result.cause, $.scojs_message.TYPE_ERROR);
		    	}
				$('#myModal').modal('hide');
				$('#cancel').click();
				//$(".modal-backdrop").remove();
				//$("table[role='presentation']").hide();
		    },  
		});

	
	
});//end of ready
