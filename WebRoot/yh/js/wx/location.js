
wx.ready(function () {
  // 1 判断当前版本是否支持指定 JS 接口，支持批量判断
  document.querySelector('#checkJsApi').onclick = function () {
    wx.checkJsApi({
      jsApiList: [
        'getNetworkType',
        'previewImage'
      ],
      success: function (res) {
        alert(JSON.stringify(res));
      }
    });
  };

  
  // 7 地理位置接口
  // 7.1 查看地理位置
  document.querySelector('#openLocation').onclick = function () {
    wx.openLocation({
      latitude: 23.099994,
      longitude: 113.324520,
      name: 'TIT 创意园',
      address: '广州市海珠区新港中路 397 号',
      scale: 14,
      infoUrl: 'http://weixin.qq.com'
    });
  };

  // 7.2 获取当前地理位置
  document.querySelector('#getLocation').onclick = function () {
    wx.getLocation({
      success: function (res) {
        alert(JSON.stringify(res));
      },
      cancel: function (res) {
        alert('用户拒绝授权获取地理位置');
      }
    });
  };
  
});

wx.error(function (res) {
  alert(res.errMsg);
});
