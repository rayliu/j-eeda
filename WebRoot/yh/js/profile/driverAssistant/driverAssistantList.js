$(document).ready(function() {
	document.title = '跟车人员信息查询 | '+document.title;
	$('#menu_carmanage').addClass('active').find('ul').addClass('in');
	var dataTable= $('#example').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "bServerSide": true,
	        "bRetrieve": true, 
	        "sAjaxSource": "/driverAssistant/list",
			"aoColumns": [
				{ "mDataProp": "NAME" ,
					"fnRender": function(obj) {					
						return "<a href='/driverAssistant/edit/"+obj.aData.ID+"'target='_blank'>"+obj.aData.NAME+"</a>";     			
				}},
				{ "mDataProp": "PHONE" },
				{ "mDataProp": "DATE_OF_ENTRY" },
				{ "mDataProp": "IDENTITY_NUMBER"},
				{ 
	                "mDataProp": null, 
	                "sWidth": "15%",
	                "fnRender": function(obj) { 
	                	if(obj.aData.IS_STOP != true){
	                		 return "<nobr><a class='btn  btn-primary btn-sm editbutton' href='/driverAssistant/edit/"+obj.aData.ID+"'>"+
			                             "<i class='fa fa-edit'> </i> "+
			                             "编辑"+
				                         "</a>"+
			                         "<a class='btn  btn-sm btn-danger delete' code='"+obj.aData.ID+"'>"+
			                             "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                             "停用"+
			                         "</a></nobr>";
	                	}else{
	                		return "<nobr><a class='btn  btn-primary btn-sm editbutton' href='/driverAssistant/edit/"+obj.aData.ID+"'>"+
		                            "<i class='fa fa-edit'> </i> "+
		                            "编辑"+
			                         "</a>"+
			                         "<a class='btn btn-success btn-sm delete' code='"+obj.aData.ID+"'>"+
			                             "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                             "启用"+
			                         "</a></nobr>";
	                	}
	                   
	                }
	            } 
	            ]
	} );
	//gateInProduct删除
	 $("#example").on('click', '.delete', function(){
		 var text = $(this).text();
		 if(confirm("确定"+text+"吗？")){
			 var id = $(this).attr('code');
			 $.post('/driverAssistant/delect/'+id,function(data){
	             if(data.success){
	            	 dataTable.fnDraw();
	             }else{
	                 alert('停用失败');
	             }
	         },'json');
		 }
	});
	
});