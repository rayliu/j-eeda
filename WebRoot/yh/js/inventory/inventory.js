$(document).ready(function() {
	$('#menu_warehouse').addClass('active').find('ul').addClass('in');
	 $('#example').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/yh/inventory/gateInlist",
			"aoColumns": [
				{ "mDataProp": "USER_NAME" },
				{ "mDataProp": null },
	            { "mDataProp": "PASSWORD_HINT" },
	            { 
	                "mDataProp": null, 
	                "sWidth": "15%",
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-info editbutton' href='/yh/loginUser/edit/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-edit'> </i> "+
	                                "编辑"+
	                            "</a>"+
	                            "<a class='btn btn-danger' href='/yh/loginUser/del/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                                "删除"+
	                            "</a>";
	                }
	            } 
	            ]
	} );
	 $('#customerMessage').on('keyup', function(){
			var inputStr = $('#customerMessage').val();
			$.get('/yh/gateIn/searchCustomer', {input:inputStr}, function(data){
				console.log(data);
				var customerList =$("#customerList");
				customerList.empty();
				for(var i = 0; i < data.length; i++)
				{
					customerList.append("<li><a tabindex='-1' class='fromLocationItem' code='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+"</a></li>");
				}
			},'json');
			$("#customerList").css({ 
	        	left:$(this).position().left+"px", 
	        	top:$(this).position().top+32+"px" 
	        }); 
	        $('#customerList').show();
		});
		// 选中供应商
		$('#customerList').on('click', '.fromLocationItem', function(e){
			$('#spMessage').val($(this).text());
			$('#sp_id').val($(this).attr('spid'));
			$('#cid').val($(this).attr('code'));
	        $('#spList').hide();
	    }); 

});