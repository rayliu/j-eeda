
$(document).ready(function() {
		    
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
			var trandferOrderId = $("#tranferid").val();
			$('#eeda-table').dataTable({
		        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/yh/delivery/orderList?trandferOrderId="+trandferOrderId,
		        "aoColumns": [   
		            {"mDataProp":"ITEM_NO"},
		            {"mDataProp":"ITEM_NAME"},
		            {"mDataProp":"AMOUNT"},        	
		            {"mDataProp":"VOLUME"},
		            {"mDataProp":"WEIGHT"},
		        ]      
		    });	
			//添加配送单
			$("#saveBtn").click(function(e){
		            //阻止a 的默认响应行为，不需要跳转	
				
		            e.preventDefault();
		            //异步向后台提交数据
		           $.post('/yh/delivery/deliverySave', $("#deliveryForm").serialize(), function(data){
		                console.log(data);
	                    if(data.success){
	                    	$("#style").show();
	                     }else{
	                        alert('数据保存失败。');
	                    }
		                    
		                },'json');
		        });
			
			$('#eeda-table2').dataTable({
		        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/yh/delivery/SearchTransfer",
		        "aoColumns": [   
		            {"mDataProp":"ORDER_NO"},
		            {"mDataProp":"STATUS"},
		            {"mDataProp":"CARGO_NATURE"},        	
		            {"mDataProp":"PICKUP_MODE"},
		            {"mDataProp":"ARRIVAL_MODE"},
		            { 
		                "mDataProp": null, 
		                "fnRender": function(obj) {                    
		                    return "<select>"+
		                    		"<option>123</option>"+
		                    		"<option>asda</option>"+
		                    		"</select>";
		                }
		            },   
		            { 
		                "mDataProp": null, 
		                "sWidth": "8%",                
		                "fnRender": function(obj) {                    
		                    return "<a class='btn btn-info editbutton' href='/yh/delivery/creat/"+obj.aData.ID+"'>"+
		                    		"<i class='fa fa-list fa-fw'> </i> "+
		                    		"创建"+
		                    		"</a>";
		                }
		            }                         
		        ]      
		    });	
			
			$('#menu_transfer').addClass('active').find('ul').addClass('in');
		    
			//datatable, 动态处理
		    var dataTable =$('#eeda-table3').dataTable({
		        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		        "bServerSide": true,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/yh/delivery/deliveryList",
		        "aoColumns": [   
		            
		            {"mDataProp":"ORDER_NO"},
		            {"mDataProp":"TRANSFER_ORDER_ID"},
		            {"mDataProp":"CUSTOMER_ID"},        	
		            {"mDataProp":"SP_ID"},
		            {"mDataProp":"NOTIFY_PARTY_ID"},
		            {"mDataProp":"STATUS"},
		            { 
		                "mDataProp": null, 
		                "sWidth": "8%",                
		                "fnRender": function(obj) {                    
		                    return "<a class='btn btn-success edit' href='/yh/delivery/edit/"+obj.aData.ID+"'>"+
		                                "<i class='fa fa-edit fa-fw'></i>"+
		                                "查看"+
		                            "</a>"+
		                            "<a class='btn btn-danger cancelbutton' code='"+obj.aData.ID+"'>"+
		                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
		                                "取消"+
		                            "</a>";
		                }
		            }                         
		        ]      
		    });	
		    
			 $("#eeda-table3").on('click', '.cancelbutton', function(e){
				  e.preventDefault();
		         //异步向后台提交数据
				 var id = $(this).attr('code');
				$.post('/yh/delivery/cancel/'+id,function(data){
		                 //保存成功后，刷新列表
		                 console.log(data);
		                 if(data.success){
		                	 dataTable.fnDraw();
		                 }else{
		                     alert('取消失败');
		                 }
		                 
		             },'json');
				  });
			
});
