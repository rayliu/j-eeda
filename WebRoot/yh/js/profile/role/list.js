$(document).ready(function() {
	document.title = '岗位查询 | '+document.title;
	$('#menu_sys_profile').addClass('active').find('ul').addClass('in');
	$('#example').dataTable({
			"bFilter" : false,
			"sDom" : "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
			//"sPaginationType": "bootstrap",
			"iDisplayLength" : 10,
			"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
			"oLanguage" : {
				"sUrl" : "/eeda/dataTables.ch.txt"
			},
			"sAjaxSource" : "/role/list",
			"aoColumns" : [
					{
						"mDataProp" : "CODE",
						"fnRender":function(obj){
							var role_update_permission = Role.UpdatePermission;
							if(role_update_permission){
								return "<a href='/role/ClickRole?id="+ obj.aData.ID +"' target='_blank'>" + obj.aData.CODE + "</a>";	
							}else{
								return obj.aData.CODE;
							}
							
						}
					},
					{
						"mDataProp" : "NAME"
					},
					{
						"mDataProp" : "REMARK"
					}
					,{
						"mDataProp" : null,
						"sWidth" : "10%",
						"bVisible":(Role.UpdatePermission || Role.DelPermission),
						"fnRender" : function(obj) {
							if(obj.aData.CODE != "admin" && obj.aData.CODE != "outuser"){
								var role_update_permission = Role.UpdatePermission;
								var role_del_permission = Role.DelPermission;
								
								var str="";
											
								if(role_update_permission){
									str += "<nobr><a class='btn  btn-primary btn-sm' href='/role/ClickRole?id="+obj.aData.ID+"' target='_blank'>"
										+ "<i class='fa fa-edit fa-fw'></i> "
										+ "编辑"
										+ "</a> ";
								}
								if(role_del_permission){
									str += "<a class='btn  btn-sm btn-danger' href='/role/deleteRole/"+obj.aData.ID+"'>"
										+ "<i class='fa fa-trash-o fa-fw'></i> "
										+ "删除"
										+ "</a>";
								}
								return str +="</nobr>";
							}else{
								return "";
							}
							
						}
					}
			]
		});
	$("#createBtn").click(function(){
		document.title=document.title.substring(6, 11);
		$("#roleList").hide();
		$("#addRole").show();
	
	});	
    /*$("#saveBtn").click(function(){
    	$("#roleList").show();
		$("#addRole").hide();
    });	*/
    $('#addRoleForm').validate({
				rules : {
					rolename : {
						required : true,
						remote:{
		                	url: "/role/checkRoleNameExit", //后台处理程序    
                            type: "post",  //数据发送方式  
                            data:  {                     //要传递的数据   
                            	rolename: function() {   
                                    return $("#rolename").val();   
                                  }   
  
                            } 
						}
					},
					rolecode:{
						required:true,
						remote:{
							url: "/role/checkRoleCodeExit", //后台处理程序    
                            type: "post",  //数据发送方式  
                            data:  {                     //要传递的数据   
                            	rolecode: function() {   
                                    return $("#rolecode").val();   
                                  }   
  
                            } 
						}
					}

				},
				 messages:{
	            	 rolename:{
	            		 remote:"岗位已存在"
	            	 },
	            	 rolecode:{
	            		 remote:"岗位编码已存在" 
	            	 }
	             },
				highlight : function(element) {
					$(element).closest('.form-group')
							.removeClass('has-success')
							.addClass('has-error');
				},
				success : function(element) {
					element.addClass('valid').closest(
							'.form-group').removeClass(
							'has-error').addClass(
							'has-success');
				}
			});
});