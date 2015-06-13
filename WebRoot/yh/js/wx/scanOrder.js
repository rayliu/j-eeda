
wx.ready(function () {
  
   // 9.1.2 扫描二维码并返回结果
  document.querySelector('#scanQRCode1').onclick = function () {
    wx.scanQRCode({
      needResult: 1,
      desc: 'scanQRCode desc',
      success: function (res) {
        alert(JSON.stringify(res));
        //异步向后台提交数据 
        $.post('/wx/saveScanResult', {serialNo:res.resultStr}, function(data){
          //alert(JSON.stringify(data));
          if(data.status=='ok'){
            $('#orderDesc').text('单号保存成功');
          }else{
            $('#orderDesc').text('单号保存失败');
          }
        });
      }
    });
  };
});

wx.error(function (res) {
  alert(res.errMsg);
});
