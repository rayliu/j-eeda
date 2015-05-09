$(document).ready(function() {
	var uploadImgWidth;
	var uploadImgHeight;
	
	var imgTypeArr = new Array();  
	var imgArr = new Array();  
	var isHand = 0;//1正在处理图片  
	var nowImgType = "image/jpeg";  
	var jic = {  
	    compress: function(source_img_obj,imgType){  
	        //alert("处理图片");  
	        source_img_obj.onload = function() {  
	            var cvs = document.createElement('canvas');  
	            var divWidth = $('#imgContent').width() - 40;
	            //naturalWidth真实图片的宽度  
	            console.log("原图:宽"+this.width+",高"+this.height);  
	            if (this.width > divWidth) {
	            	uploadImgWidth = divWidth;
	            	uploadImgHeight = this.height * (divWidth / this.width);
	          	}else{
	          		uploadImgWidth = this.width;
	                uploadImgHeight = this.height;
	          	}
	            console.log("修改后:宽"+uploadImgWidth+",高"+uploadImgHeight);  
	            initFileuploadComponent(uploadImgWidth, uploadImgHeight);
	        }
	    }  
	};
	
	function handleFileSelect (evt) {
		isHand = 1;  
		imgArr = [];  
		imgTypeArr = [];  
		$("#canvasDiv").html("");  
		var files = evt.files;  
		for (var i = 0, f; f = files[i]; i++) {  
	        // Only process image files.  
	        if (!f.type.match('image.*')) {  
	        	continue;  
	        }  
	        imgTypeArr.push(f.type);  
	        nowImgType = f.type;  
	        var reader = new FileReader();  
	        // Read in the image file as a data URL.  
	        reader.readAsDataURL(f);  
	        // Closure to capture the file information.  
	        reader.onload = (function(theFile) {  
	            return function(e) {  
	                var i = new Image();  
	                i.src = e.target.result;  
	                jic.compress(i,nowImgType);  
	                  
	            };  
	        })(f);  
		}  
	}
	
	$('#file_upload').on('change', function(e){
		handleFileSelect(this);
	});
	
	//初始化fileupload form
	var initFileuploadComponent = function(imageWidth, imageHeight){
		$('#file_upload_form').fileupload({
		    // Uncomment the following to send cross-domain cookies:
		    //xhrFields: {withCredentials: true},
		    disableImageResize: false,
		    url: '/returnOrder/saveFile?return_id='+$("#returnId").val()+'&permission='+$("#permission").val(),
		    validation: {allowedExtensions: ['jpeg', 'jpg', 'png' ,'gif']},
		    imageMaxWidth: imageWidth,
		    imageMaxHeight: imageHeight,
		    imageCrop: true, // Force cropped images
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
	};
	
	
});//end of ready
