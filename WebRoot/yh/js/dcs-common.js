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
