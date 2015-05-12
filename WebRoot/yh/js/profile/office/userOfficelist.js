$(document).ready(function() {
	document.title = '网点查询 | '+document.title;
  $('#menu_profile').addClass('active').find('ul').addClass('in');
  /*var name =$("#user_filter").val();*/
  var refreshDatable=$('#eeda-table').dataTable({
    	"bFilter" : false,
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
    	"bPaginate": false,
    	"bLengthChange": false,
    	"bInfo": false,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt",
        },
        "sAjaxSource": "/userOffice/list",
        "aoColumns": [
             { "mDataProp":"OFFICE_CODE"},
             { "mDataProp":"OFFICE_NAME"},
             { "mDataProp":"ADDRESS"}
        ] 
    });
	/*-------------------------------用户选择-------------------------------*/
  $('#user_filter').on('click keyup', function(){
		var inputStr = $('#user_filter').val();
		$.get('/userOffice/selectUser', {locationName:inputStr},function(data){
			var userList =$("#userList");
			userList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var user_name = data[i].USER_NAME;
				if(user_name == null){
					user_name = '';
				}
					
					userList.append("<li><a tabindex='-1' class='fromLocationItem' user_name='"+data[i].USER_NAME+"' >"+user_name+" "+"</a></li>");
				}
			},'json');
	
			$("#userList").css({ 
	    	left:$(this).position().left+"px", 
	    	top:$(this).position().top+32+"px" 
	    }); 
			
	    $('#userList').show();
  });

  // 没选中用户，焦点离开，隐藏列表
	$('#user_filter').on('blur', function(){
		$('#userList').hide();
	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#userList').on('blur', function(){
		$('#userList').hide();
	});

	$('#userList').on('mousedown', function(){
		return false;
	});

	
	$('#userList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#user_filter').val(message.substring(0, message.indexOf(" ")));
		$('#user_id').val($(this).attr('partyId'));
		var userName = $("#user_name");
		userName.empty();
		$('#userList').hide();
		
		var username = $("#user_filter").val();
		refreshDatable.fnSettings().sAjaxSource = "/userOffice/queryOffice?username="+username;
		refreshDatable.fnDraw();
		
	});
	/*$("#user_filter").on('change',function(){
		
		var username = $("#user_filter").val();
		refreshDatable.fnSettings().sAjaxSource = "/userOffice/queryOffice?username="+username;
		refreshDatable.fnDraw();
	});*/
	
	/*-------------------------------------校验-----------------------------*/
	/*$('#userForm').validate({
        rules: {
        	user_filter: {
               required: true,
               remote:{
               	url: "/userOffice/checkUserNameExist", //后台处理程序    
                   type: "post",  //数据发送方式  
                   data:  {                     //要传递的数据   
                	   user_filter: function() { 
                   		 return $("#user_filter").val();
                   		
                      },
                   }   
               }
        	}
       	 },  	
		 messages:{
			 user_filter:{
				 required:"用户不为空",
				 remote:"用户不存在"
			 }
       },
       highlight: function(element) {
      	$(element).parent().removeClass('has-success').addClass('has-error');
      	
      },
      success: function(element) {
      	element.parent().removeClass('has-error').addClass('has-success');
      	
      }
	});
	$("#user_filter").on("blur",function(){
		if($("#userForm").valid()){
			var username = $("#user_filter").val();
			refreshDatable.fnSettings().sAjaxSource = "/userOffice/queryOffice?username="+username;
			refreshDatable.fnDraw();
		}   
	});*/
});
   