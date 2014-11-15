$(document).ready(function() {
  $('#menu_profile').addClass('active').find('ul').addClass('in');
  
  $('#role_filter').on('keyup click', function(){
		//var inputStr = $('#role_filter').val();
		$.get('/privilege/roleList', function(data){
			var roleList =$("#roleList");
			roleList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var name = data[i].NAME;
				if(name == null){
					name = '';
				}
				
				roleList.append("<li><a tabindex='-1' class='fromLocationItem''name='"+data[i].NAME+"' >"+name+" "+"</a></li>");
			}
		},'json');

		$("#roleList").css({ 
      	left:$(this).position().left+"px", 
      	top:$(this).position().top+32+"px" 
      }); 
      $('#roleList').show();
  });
  

	$('#role_filter').on('blur', function(){
		$('#roleList').hide();
	});


	$('#roleList').on('blur', function(){
		$('#roleList').hide();
	});

	$('#roleList').on('mousedown', function(){
		return false;
	});

	

	
	var privilege_table = $('#eeda-table').dataTable({
    	"bFilter" : false,
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/privilege/list",
        "aoColumns": [
            { "mDataProp": null, "sWidth": "7%"	,
            	 "fnRender": function(obj) {
            		 if(obj.aData.PERMISSION_CODE==null){
            			 return '<input type="checkbox" disabled="true" name="permissionCheck" value="'+obj.aData.CODE+'">';
            		 }else{
            			 return '<input type="checkbox" disabled="true" checked="true" name="permissionCheck" value="'+obj.aData.CODE+'">';
            		 }
                     
                  }	
            },
            { "mDataProp": "MODULE_NAME","sWidth": "20%"},
            { "mDataProp": "NAME"}
        ] 
    });
	
	$('#roleList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#role_filter').val(message.substring(0, message.indexOf(" ")));
		$('#roleList').hide();
		
		var rolename = $('#role_filter').val();
		console.log(rolename);
		
		privilege_table.fnSettings().sAjaxSource = "/privilege/list?rolename="+rolename;
		privilege_table.fnDraw(); 

	});
    $('#roleForm').validate({
        rules: {
        	rolename: {
               required: true
        	}
       	 },  	
		 messages:{
			 rolename:{
				 required:"角色不为空"
			 }
       },
       highlight: function(element) {
      	$(element).parent().removeClass('has-success').addClass('has-error');
      	
      },
      success: function(element) {
      	//element.parent().removeClass('has-error').addClass('has-success');
      	
      }
        
 });	

	
});
   