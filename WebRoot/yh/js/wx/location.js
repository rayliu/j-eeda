

wx.ready(function () {
	var getUrlParameter=function(sParam){
	    var sPageURL = window.location.search.substring(1);
	    var sURLVariables = sPageURL.split('&');
	    for (var i = 0; i < sURLVariables.length; i++) 
	    {
	        var sParameterName = sURLVariables[i].split('=');
	        if (sParameterName[0] == sParam) 
	        {
	            return sParameterName[1];
	        }
	    }
	};

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
		$('#locationDesc').text('正在定位,请稍后....');
		wx.getLocation({
			success: function (res) {
				//alert(JSON.stringify(res));
				$('#locationDesc').text('定位成功,正在转接地图....');
				var openLoc=function(res, address){
					wx.openLocation({
						latitude: res.latitude,
						longitude: res.longitude,
						// name: 'TIT 创意园',
						address: address,
						scale: 14,
						infoUrl: 'http://weixin.qq.com'
					});
					$('#locationDesc').text('');
				};
				getLocationInfo(res, openLoc);
			},
			cancel: function (res) {
				alert('用户拒绝授权获取地理位置');
			}
		});
    
	};

	var saveLocation=function(openId){
		wx.getLocation({
			success: function (res) {
				//alert(JSON.stringify(res));
				var saveLoction = function(latlng, address){
					//alert("当前开始ajax保存位置信息");
					$.post("/wx/saveLocationInfo",
					       {longitude: latlng.longitude,latitude: latlng.latitude,address:address, openId:openId},
					    function(data){
						if(data.ID != "" && data.ID != null)
							$('#orderDesc').text("汇报成功");
						else
							$('#orderDesc').text("汇报失败");
					});
					//alert("当前结束ajax保存位置信息");
				};
				getLocationInfo(res, saveLoction);
			},
			cancel: function (res) {
				alert('用户拒绝授权获取地理位置');
			}
		});
	}
	  
	document.querySelector('#getLocation').onclick = function () {
		var code=getUrlParameter('code');
		var openId = $('#openId').val();
		if(openId.length==0){
			$.post("/wx/getWechatUserName",{code:code},function(data){
				if(data.errcode==40029){
					$('#orderDesc').text('当前授权已过期，请返回上级菜单。'+'<br>');
				}else{
					$('#openId').val(data.openid);
					$('#nickName').val(data.nickname);
					saveLocation(data.openid);
				}
				//$('#orderDesc').append(JSON.stringify(data)+'<br>');
				
			});
		}else{
			saveLocation(openId);
		}
	};
  
});

wx.error(function (res) {
  alert(res.errMsg);
});
