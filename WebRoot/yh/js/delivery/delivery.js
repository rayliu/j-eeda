
$(document).ready(function() {
		    $('#eeda-table').dataTable();
		    
		  //获取供应商的list，选中信息在下方展示其他信息
			$('#spMessage').on('keyup', function(){
				var inputStr = $('#spMessage').val();
				$.get('/yh/delivery/searchSp', {input:inputStr}, function(data){
					console.log(data);
					var spList =$("#spList");
					spList.empty();
					for(var i = 0; i < data.length; i++)
					{
						spList.append("<li><a tabindex='-1' class='fromLocationItem' code='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+"</a></li>");
					}
				},'json');

		        $('#spList').show();
			});
			// 选中供应商
			$('#spList').on('click', '.fromLocationItem', function(e){
				$('#spMessage').val($(this).text());
				$('#sp_id').val($(this).attr('spid'));
				$('#cid').val($(this).attr('code'));
				var pageSpName = $("#pageSpName");
				pageSpName.empty();
				
				pageSpName.append($(this).attr('contact_person')+'&nbsp;');
				pageSpName.append($(this).attr('phone')); 
				var pageSpAddress = $("#pageSpAddress");
				pageSpAddress.empty();
				pageSpAddress.append($(this).attr('address'));
		        $('#spList').hide();
		    }); 
			//datatable, 动态处理
			
		    $('#eeda-table').dataTable({
		        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/yh/delivery/transferOderItem",
		        "aoColumns": [   
		            
		            {"mDataProp":"ITEM_NO"},
		            {"mDataProp":"ITEM_NAME"},
		            {"mDataProp":"MOUNT"},        	
		            {"mDataProp":"UNIT"},
		            {"mDataProp":"ITEM_DESC"},
		            {"mDataProp":"REMARK"},
		        ]      
		    });	
			
});
