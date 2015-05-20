wx.ready(function () {
  var geocoder,map, marker = null;
  geocoder = new soso.maps.Geocoder();
  
  var getLocationInfo= function(latlng, callback){
      var res = null;
      var latLng = new soso.maps.LatLng(latlng.latitude, latlng.longitude);
      geocoder.geocode({'location': latLng}, function(results, status) {
            if (status == soso.maps.GeocoderStatus.OK) {
                callback(latlng, results.address);
            } else {
                alert("检索没有结果，原因: " + status);
            }
      });
      return res;
  };
  
  // 7 地理位置接口
  // 7.1 查看地理位置
  document.querySelector('#openLocation').onclick = function () {
     wx.getLocation({
      success: function (res) {
        //alert(JSON.stringify(res));
        var openLoc=function(res, address){
          wx.openLocation({
            latitude: res.latitude,
            longitude: res.longitude,
            // name: 'TIT 创意园',
            address: address,
            scale: 14,
            infoUrl: 'http://weixin.qq.com'
          });
        };
        getLocationInfo(res, openLoc);
      },
      cancel: function (res) {
        alert('用户拒绝授权获取地理位置');
      }
    });
    
  };

  // 7.2 获取当前地理位置
  document.querySelector('#getLocation').onclick = function () {
    wx.getLocation({
      success: function (res) {
        //alert(JSON.stringify(res));
        var saveLoction = function(latlng, address){
        	$.post("/wx/saveLocationInfo",{longitude: latlng.longitude,latitude: latlng.latitude,address:address},function(data){
        		if(data.ID != "" && data.ID != null)
        			alert("汇报成功");
        		else
        			alert("汇报失败");
            });
        };
        getLocationInfo(res, saveLoction);
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
