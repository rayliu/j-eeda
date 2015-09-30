var eeda={};

var refreshUrl=function(url){
    	var state = window.history.state;
    	if(state){
    		window.history.replaceState(state, "", url);
    	}else{
    		window.history.pushState({}, "", url);
    	}
   };
   
 var contactUrl=function(str,id){
	 refreshUrl(window.location.protocol + "//" + window.location.host+window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/')+1)+str+"="+id);
 };
 
 eeda.getUrlByNo= function(id, orderNo) {
 	var str = "";
     if(orderNo.indexOf("PS") > -1){
         str = "<a href='/delivery/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("PC") > -1){
         str = "<a href='/pickupOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("HD") > -1){
         str = "<a href='/returnOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("DC") > -1){
         str = "<a href='/pickupOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("FC") > -1){
         str = "<a href='/departOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("SGFK") > -1){
         str = "<a href='/costMiscOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("SGSK") > -1){
         str = "<a href='/chargeMiscOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else  if(orderNo.indexOf("BX") > -1){
         str = "<a href='/insuranceOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }
     return str;
 };

 window.onunload=function(){
    //页面刷新时调用，这里需要判断是否当前单据是否有更新，提示用户先保存
	//暂时不处理 
 };
