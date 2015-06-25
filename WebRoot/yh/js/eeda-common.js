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
 

 window.onunload=function(){
    //页面刷新时调用，这里需要判断是否当前单据是否有更新，提示用户先保存
	//暂时不处理 
 };
