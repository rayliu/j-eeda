$(document).ready(function() {
 
  $("#searchNo").click(function(){  
    var orderNo = $("#orderNo").val();
    
    $.post('/wx/getRo/'+orderNo,function(data){
           
           if(data.ORDER_NO){
              $('#orderDesc').text('回单号码确认存在，请从相册中选择照片上传。');
              $('#returnId').val(data.ID);
           }else{
              $('#orderDesc').text('回单号码不存在，请重新查询。');
           }
           $('#orderDesc').show();
        },'json');
  });


  //保存图片
    $("#uploadImage1").click(function(e){
      $("#fileupload").click();
   });

   $('#fileupload').fileupload({
        dataType: 'json',
        url: '/wx/saveFile?return_id='+$("#returnId").val(),//上传地址
        done: function (e, data) {
          if(data.result.result = "true"){
            alert("上传成功！");
            console.log("data.result.cause:"+data.result.cause);
            //console.log("data.result.cause:"+data.result.cause+",parseJSON:"+$.parseJSON(data.result.cause));
            //var files = $.parseJSON(data.result.cause);
            var showPictures = $("#showPictures");
            showPictures.empty();
            $.each(data.result.cause,function(name,value) {
              showPictures.append('<div style="width:220px;height:220px;float:left;" ><img src="/upload/fileupload/'+value.FILE_PATH+'" alt="" style="width:200px;height:200px;"><p><a class="picturedel" picture_id="'+value.ID+'" >删除</a></p></div>');
                });
          }else{
            $("#centerBody").empty().append("<h4>"+data.result.cause+"</h4>");
          }
          $("#footer").show();
        },  
        progressall: function (e, data) {//设置上传进度事件的回调函数  
          //$.scojs_message('上传中', $.scojs_message.TYPE_OK);
          //$('#myModal').modal('show');
          //$("#footer").hide();
        } 
     });

});

