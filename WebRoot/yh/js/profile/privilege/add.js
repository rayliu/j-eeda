$(document).ready(function() {
  $('#menu_profile').addClass('active').find('ul').addClass('in');

  $('#role_filter').on('click', function(){
	    /*var inputStr = $('#role_filter').val();*/
		$.post('/privilege/seachNewRole', function(data){
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

	

	var numberName="";
	var privilege_table = $('#eeda-table').dataTable({
    	"bFilter" : false,
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
    	"bPaginate": false,
    	"bLengthChange": false,
    	"bInfo": false,
    	"bStateSave":true,
    	"iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/privilege/list",
        "aoColumns": [           
            { "mDataProp": "MODULE_NAME","sWidth":"15%"},
			{ "mDataProp": null,
				"fnRender":function(obj){
					var str = "";
					for(var i=0;i<obj.aData.CHILDRENS.length;i++){
						str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;"  name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">'+obj.aData.CHILDRENS[i].NAME+'</div>';
					}
				
				return str;
			}}
        ] 
    });
	
	$('#roleList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#role_filter').val(message.substring(0, message.indexOf(" ")));
		$('#roleList').hide();
	});
	
	/*点击下一页没有保存上页的数据*/
	var permission=[];
    $('#saveBtn').click(function(e){
        e.preventDefault();
        permission.splice(0,permission.length);	
        $("input[name='permissionCheck']").each(function(){
        	if($(this).prop('checked') == true){
        		permission.push($(this).val());
        	}
        });
        var rolename = $("#role_filter").val();
        var permissions = permission.toString();
        if(rolename != ""&&permission!=0){
        	$.get('/privilege/save?name='+rolename+'&permissions='+permissions, function(data){
        		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        		
        		$("#role_filter").val("");
        		$("input[name='permissionCheck']").each(function(){
                	$(this).prop('checked',false);	
                });
        		permission.splice(0,permission.length);
    		},'json');
        }
       
    });

	
});