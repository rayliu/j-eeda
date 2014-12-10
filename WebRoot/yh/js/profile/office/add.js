$(document).ready(function() {
  $('#menu_profile').addClass('active').find('ul').addClass('in');	
  /*--------------------------------查询所有的用户----------------------------------------*/
  $('#user_filter').on('click keyup', function(){
		var inputStr = $('#user_filter').val();
		$.get('/userOffice/seachUser', {locationName:inputStr},function(data){			
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
       office=[];
	   $("#officeSelect").val("");
	   $("#eeda_tbody").children().remove();
  });
  /*------------------------------查询所有的网点-------------------------------------*/
	$.post('/userOffice/seachOffice',function(data){
		 if(data.length > 0){
			 var officeSelect = $("#officeSelect");
			 officeSelect.empty();
			 var hideOfficeId = $("#hideOfficeId").val();
			 for(var i=0; i<data.length; i++){
				 if(i == 0){
					 officeSelect.append("<option ></option>");
				 }else{
					 if(data[i].ID == hideOfficeId){
						 officeSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");
					 }else{
						 officeSelect.append("<option value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");					 
					 }
				 }
			 }
			
		 }
	 },'json');
	/*----------------------------填充表格-----------------------------------*/
	var refreshTable= $('#eeda-table').dataTable({           
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",  
        "bPaginate": false,
        "bFilter": false,
    	"bLengthChange": false,
    	"bInfo": false,
    	"bStateSave":true,
        "bServerSide": true, 
    	 "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/userOffice/queryOffice",      
        "aoColumns": [
             { "mDataProp": "OFFICE_NAME"},
            { "mDataProp": null,
            	"fnRender": function(obj){
            		return "<a class='btn btn-danger btn-xs removeOffice' id="+obj.aData.ID+" title='移除'>"+
                    "<i class='fa fa-trash-o fa-fw'></i>"+
                    "</a>";
            	}}
                    
        ]        
    });
	var office=[];
	$("#officeSelect").on('change',function(){
		if($("#officeSelect").val()!=null&&$("#officeSelect").val()!=""){
			var isExit=false;
			for(var i=0;i<office.length;i++){
				if($("#officeSelect").val()==office[i]){
					isExit=true;
				}
			}
			if(isExit==false){
				office.push($("#officeSelect").val());
			}			
		}
		var offices = office.toString();
		refreshTable.fnSettings().sAjaxSource = "/userOffice/queryOffice?offices="+offices;
		refreshTable.fnDraw();
	});
	$("#eeda-table").on('click','.removeOffice',function(e){
		
		var id = $(this).prop("id");
		var j=$.inArray(id, office);
		console.log(j);
		if(j!=-1){
			if(j==0&&office.length==1){
				office=[];
				$("#officeSelect").val("");
				$("#eeda_tbody").children().remove();
			}else{
				office=office.slice(0,j).concat(office.slice(j+1,office.length));
			}
			
		}
		refreshTable.fnSettings().sAjaxSource = "/userOffice/queryOffice?offices="+office.toString();
		refreshTable.fnDraw();
	});
	/*---------------------------保存以及验证------------------------------------*/
	$('#userForm').validate({
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
	
	$("#saveBtn").on('click',function(){
		var name =$("#user_filter").val();
		if(!$("#userForm").valid()){
	       	return false;
        }
		$.post('/userOffice/save?username='+name+'&officeIds='+office.toString(), function(data){
			   if(data==true){
				   office=[];
				   $("#user_filter").val("");
				   $("#officeSelect").val("");
				   $("#eeda_tbody").children().remove();
				   $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				   
			   }else{
				   $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			   }
			   
		},'json');	
	});
	
	$("#user_filter").on("change",function(){
		office=[];
	   $("#officeSelect").val("");
	   $("#eeda_tbody").children().remove();
	});
	$("#user_filter").on("blur",function(){
		$("#userForm").valid();   
	});
	
	/*-----------------------------------提示-------------------------------*/
	 var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	 $('body').append(alerMsg);
	
	
});
   