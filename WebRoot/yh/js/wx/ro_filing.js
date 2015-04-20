$(document).ready(function() {
	
	var type = $("#type").val();
	if(type == "directSend"){
		$("#defaultDiv").hide();
		$("#distributionDiv").hide();
		$("#titleName").text("创诚易达物流系统-直送签收");
	}else if(type == "distribution"){
		$("#defaultDiv").hide();
		$("#directSendDiv").hide();
		$("#titleName").text("创诚易达物流系统-配送签收");
	}else{
		$("#directSendDiv").hide();
		$("#distributionDiv").hide();
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
   }
   
    
    
    //供应商列表
    $('#spMessage').on('keyup click', function(){
		var inputStr = $('#spMessage').val();
		if(inputStr == ""){
			$('#sqId').val("");
		}
		$.get('/wx/searchPartSp', {input:inputStr}, function(data){			
			var spList =$("#spList");
			spList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var abbr = data[i].ABBR;
				if(abbr == null){
					abbr = '';
				}
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
				spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' spid='"+data[i].PID+"' >"+company_name+"</a></li>");
			}
		},'json');

		$("#spList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#spList').show();
	});

	// 没选中供应商，焦点离开，隐藏列表
	$('#spMessage').on('blur', function(){
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
		$('#spMessage').val($(this).text());
		$('#sqId').val($(this).attr('spid'));
        $('#spList').hide();
    }); 

});

