var queryOffice=function(id){
	var offices=[];
	$.post('/loginUser/searchAllOffice',function(data){
   		 if(data.length > 0){
   			//officeSelect.empty();
   			 var officeSelect = $("select[name='officeSelect']");
   			 
   			$("select[name='officeSelect']").each(function(){
   				if($(this).val()!=null&&$(this).val()!=""){
   					var value=$(this).val();
   					var txt = $(this).find("option:selected").text();
   					offices.push($(this).val());
   					$(this).empty();
   					$(this).append("<option ></option>");
   					$(this).append("<option value='"+value+"' selected='selected'>"+txt+"</option>");
   				}else{
   					$(this).append("<option ></option>");
   				}
	   		});
   			//officeSelect.empty();
   			for(var i=0; i<data.length; i++){
   				var n=0;
   				for(var j=0;j<offices.length;j++){
   					if(data[i].ID==offices[j]){
   						n=offices[j];
   					}
   				}
   				/*if(i == 0){
				 	officeSelect.append("<option ></option>");
			 	}else{*/
			 		if(data[i].ID!=n){
						if(data[i].ID == id){
							officeSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");
						 }else{
							officeSelect.append("<option value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");					 
						 }
					}
   					 
			 	/*}*/
   			}
   			
   		 }
	 },'json');
};
var saveOffice = function(){
	var officeIds =[];
	$("select[name='officeSelect']").each(function(){
		if($(this).val()!=null){
			officeIds.push($(this).val());
		}	
	});
	var id = $("#userId").val();
	if(id!=null&&id!=""){
		$.post('/loginUser/saveOffice',{id:id,officeIds:officeIds.toString()},function(){
			
		},'jason');
	}
	
};
$(document).ready(function(){
	// 获取所有网点
  	/*----------添加---------*/
	//添加网点
  	$("#addOffice").on('click',function(){
		var isNull=false;
		$("select[name='officeSelect']").each(function(){
			if($(this).val()==null||$(this).val()==""){
				isNull=true;
			}
		});
		
		if(isNull==false){
			$("#tobdy").append('<tr><td><select class="form-control sOffice" name="officeSelect"></select>'
                       +' </td><td><input type="radio"  class="is_main" name="isMain_radio" value=""></td>'
                       +' <td><a class="btn removeOffice" title="删除"><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
			queryOffice();
		}
		
		
	});
	
	//添加客户
	$("#addCustomer").on('click',function(){		
		$("#customerTbody").append('<tr><td>'
				+'<input  name="customer" type="text" class="form-control customer"></td>'
                +' <td><a class="btn removeCustomer" title="删除"><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
		
	});
	/*---移除---*/
	//移除网点
	$("#tobdy").on('click','.removeOffice',function(){
		$(this).parent().parent().remove();
	});
	//移除客户
	$("#customerTbody").on('click','.removeCustomer',function(){
		$(this).parent().parent().remove();
	});
	/*下拉框选择、点击*/
	$("#tobdy").on('change','.sOffice',function(){
		$(this).parent().next().find("input[type=radio]").prop("value",$(this).val());
		saveOffice();
		queryOffice();
	});
	
	//查询
	var userId = $("#userId").val();
	$.post('/loginUser/officeList',{userId:userId},function(data){
		var tobdy = $("#tobdy");
		tobdy.empty();
		if(data.userOffice!=null){
			if(data.userOffice.length>0){
				for(var i =0;i<data.userOffice.length;i++){
					if(data.userOffice[i].IS_MAIN==null||data.userOffice[i].IS_MAIN==""){
						tobdy.append('<tr><td><select class="form-control sOffice" name="officeSelect"></select>'
			                    +' </td><td><input type="radio"  class="is_main" name="isMain_radio" value="'+data.userOffice[i].OFFICE_ID+'"></td>'
			                    +' <td><a class="btn removeOffice" title="删除"><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
					}else{
						tobdy.append('<tr><td><select class="form-control sOffice" name="officeSelect"></select>'
			                    +' </td><td><input type="radio" checked  class="is_main" name="isMain_radio" value="'+data.userOffice[i].OFFICE_ID+'"></td>'
			                    +' <td><a class="btn removeOffice" title="删除"><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
					}
					queryOffice(data.userOffice[i].OFFICE_ID);
					
					
				}
			}
		}
		
		
	},'json');
	
	   
});