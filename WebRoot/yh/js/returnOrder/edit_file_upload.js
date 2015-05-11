$(document).ready(function() {
	var uploadImgWidth;
	var uploadImgHeight;
	
	$('#fileupload').fileupload({
		autoUpload: true, //自选择后自动上传图片
     	disableImageResize: false,
	    url: '/returnOrder/saveFile?return_id='+$("#returnId").val()+'&permission='+$("#permission").val(),
	    validation: {allowedExtensions: ['jpeg', 'jpg', 'png' ,'gif']},
	    dataType: 'json',
	    imageMaxWidth: 1200,
	    imageMaxHeight: 900,
	    imageCrop: false, // 自动高宽比缩放
        //dataType: 'json',
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
		    				showPictures.append('<div style="margin-right: 10px;float:left;" ><img src="/upload/img/'+value.FILE_PATH+'" alt="" class="img-thumbnail" style="height:180px;"><p><a class="picture_audit" picture_id="'+value.ID+'" > ' + aText + ' </a><a class="picture_del" picture_id="'+value.ID+'" > 删除 </a></p></div>');
		                });
		    		}else{
		    			showPictures.empty().append('<input type="hidden" id="permission" value="permissionNo">');
		    			$.each(data.result.cause,function(name,value) {
		    				showPictures.append('<div style="margin-right: 10px;float:left;" ><img src="/upload/img/'+value.FILE_PATH+'" alt="" class="img-thumbnail" style="height:180px;"></div>');
		                });
		    		}
		    	}else{
		    		$.scojs_message(data.result.cause, $.scojs_message.TYPE_ERROR);
		    	}
				$('#myModal').modal('hide');
				$('#cancel').click();
        },
        progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .progress-bar').css(
                'width',
                progress + '%'
            );
        },
        error: function () {
            alert('An error occured while uploading the document.');
        }
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');


	
	
	
});//end of ready
