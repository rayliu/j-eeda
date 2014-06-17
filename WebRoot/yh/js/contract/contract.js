$(document).ready(function() {
	
	$('#menu_contract').addClass('active').find('ul').addClass('in');
		var contractId=$('#contractId').val();
		var dataTable = $('#dataTables-example').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "bProcessing": true,
	        "bServerSide": true,
	        "sAjaxSource": "/yh/spContract/routeEdit?routId="+contractId,
	        "aoColumns": [  
				{"mDataProp":"PRICETYPE"},
	            {"mDataProp":"LOCATION_FROM"},
	            {"mDataProp":"LOCATION_TO"},
	            {"mDataProp":"AMOUNT"},
	            { 
	                "mDataProp": null, 
	                "sWidth": "8%",                
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-success contractRouteEdit' code='"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-edit fa-fw'></i>"+
	                                "编辑"+
	                            "</a>"+
	                            "<a class='btn btn-danger routeDelete' code2='"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                                "删除"+
	                            "</a>";
	                }
	            }                         
	        ]
	     });
		var dataTable2 = $('#dataTables-example2').dataTable({
	        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "bProcessing": true,
		        "bServerSide": true,
		        "sAjaxSource": "/yh/spContract/routeEdit2?routId="+contractId,
		        "aoColumns": [  
					{"mDataProp":"PRICETYPE"},
					{"mDataProp":"CARTYPE"},
					{"mDataProp":"CARLENGTH"},
		            {"mDataProp":"LOCATION_FROM"},
		            {"mDataProp":"LOCATION_TO"},
		            {"mDataProp":"AMOUNT"},
		            { 
		                "mDataProp": null, 
		                "sWidth": "8%",                
		                "fnRender": function(obj) {                    
		                    return "<a class='btn btn-success contractRouteEdit' code='"+obj.aData.ID+"'>"+
		                                "<i class='fa fa-edit fa-fw'></i>"+
		                                "编辑"+
		                            "</a>"+
		                            "<a class='btn btn-danger routeDelete' code2='"+obj.aData.ID+"'>"+
		                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
		                                "删除"+
		                            "</a>";
		                }
		            }                         
		        ]
		     });
		var dataTable3 = $('#dataTables-example3').dataTable({
	        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        //"sPaginationType": "bootstrap",
		        "iDisplayLength": 10,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "bProcessing": true,
		        "bServerSide": true,
		        "sAjaxSource": "/yh/spContract/routeEdit3?routId="+contractId,
		        "aoColumns": [  
					{"mDataProp":"PRICETYPE"},
					{"mDataProp":"LTLUNITTYPE"},
		            {"mDataProp":"LOCATION_FROM"},
		            {"mDataProp":"LOCATION_TO"},
		            {"mDataProp":"AMOUNT"},
		            { 
		                "mDataProp": null, 
		                "sWidth": "8%",                
		                "fnRender": function(obj) {                    
		                    return "<a class='btn btn-success contractRouteEdit' code='"+obj.aData.ID+"'>"+
		                                "<i class='fa fa-edit fa-fw'></i>"+
		                                "编辑"+
		                            "</a>"+
		                            "<a class='btn btn-danger routeDelete' code2='"+obj.aData.ID+"'>"+
		                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
		                                "删除"+
		                            "</a>";
		                }
		            }                         
		        ]
		     });
		//计件编辑
		 $("#dataTables-example").on('click', '.contractRouteEdit', function(){
			  var contractId = $("#routeContractId").val();
			 	var id = $(this).attr('code');
			 $.post('/yh/customerContract/contractRouteEdit/'+id,{contractId:contractId},function(data){
                 //保存成功后，刷新列表
                 console.log(data);
                 if(data[0] !=null){
                	
                	 $('#myModal').modal('show');
                	 $('#routeId').val(data[0].ID);
                	 $('#from_id').val(data[0].FROM_ID);
                	 $('#fromName').val(data[0].LOCATION_FROM);
                	 $('#to_id').val(data[0].TO_ID);
                	 $('#toName').val(data[0].LOCATION_TO);
                	 $('#price').val(data[0].AMOUNT);
                	 $('#routeItemId').val(data[0].ID);
                	
                	 $('#optionsRadiosInline1').prop('checked', true).trigger('change');
                	 
                 }else{
                     alert('取消失败');
                 }
             },'json');
		  });
			
		//整车编辑
		 $("#dataTables-example2").on('click', '.contractRouteEdit', function(){
			  var contractId = $("#routeContractId").val();
			 	var id = $(this).attr('code');
			 $.post('/yh/customerContract/contractRouteEdit/'+id,{contractId:contractId},function(data){
                 //保存成功后，刷新列表
                 console.log(data);
                 if(data[0] !=null){
                	
                	 $('#myModal').modal('show');
                	 $('#routeId').val(data[0].ID);
                	 $('#from_id').val(data[0].FROM_ID);
                	 $('#fromName').val(data[0].LOCATION_FROM);
                	 $('#to_id').val(data[0].TO_ID);
                	 $('#toName').val(data[0].LOCATION_TO);
                	 $('#price').val(data[0].AMOUNT);
                	 $('#routeItemId').val(data[0].ID);
                	 
                	 $('#optionsRadiosInline2').prop('checked', true).trigger('change');
                 }else{
                     alert('取消失败');
                 }
             },'json');
		  });
		//零担编辑
		 $("#dataTables-example3").on('click', '.contractRouteEdit', function(){
			  var contractId = $("#routeContractId").val();
			 	var id = $(this).attr('code');
			 $.post('/yh/customerContract/contractRouteEdit/'+id,{contractId:contractId},function(data){
                 //保存成功后，刷新列表
                 console.log(data);
                 if(data[0] !=null){
                	
                	 $('#myModal').modal('show');
                	 $('#routeId').val(data[0].ID);
                	 $('#from_id').val(data[0].FROM_ID);
                	 $('#fromName').val(data[0].LOCATION_FROM);
                	 $('#to_id').val(data[0].TO_ID);
                	 $('#toName').val(data[0].LOCATION_TO);
                	 $('#price').val(data[0].AMOUNT);
                	 $('#routeItemId').val(data[0].ID);
                	 $('#optionsRadiosInline3').prop('checked', true).trigger('change');
                 }else{
                     alert('取消失败');
                 }
             },'json');
		  });
		 //计件删除
		 $("#dataTables-example").on('click', '.routeDelete', function(){
			 var id = $(this).attr('code2');
			 $.post('/yh/customerContract/routeDelete/'+id,function(data){
                 //保存成功后，刷新列表
                 console.log(data);
                 if(data.success){
                	 dataTable.fnDraw();
                 }else{
                     alert('取消失败');
                 }
             },'json');
			});
		 //整车删除
		 $("#dataTables-example2").on('click', '.routeDelete', function(){
			 var id = $(this).attr('code2');
			 $.post('/yh/customerContract/routeDelete/'+id,function(data){
                 //保存成功后，刷新列表
                 console.log(data);
                 if(data.success){
                	 dataTable2.fnDraw();
                 }else{
                     alert('取消失败');
                 }
             },'json');
			});
		 //零担删除
		 $("#dataTables-example3").on('click', '.routeDelete', function(){
			 var id = $(this).attr('code2');
			 $.post('/yh/customerContract/routeDelete/'+id,function(data){
                 //保存成功后，刷新列表
                 console.log(data);
                 if(data.success){
                	 dataTable3.fnDraw();
                 }else{
                     alert('取消失败');
                 }
             },'json');
			});
	
		//from表单验证
		var validate = $('#customerForm').validate({
	        rules: {
	          contract_name: {
	            required: true
	          },
	          companyName:{//form 中 name为必填
	            required: true
	          }
	        },
	        messages : {
	             
	        	contract_name : {required:  "不能为空"}, 
	        	companyName: {required :"不能为空"},
	        }
	    });
	
        //点击button显示添加合同干线div
        $("#btn").click(function(){
        	var contractId = $("#routeContractId").val();
        	if(contractId != ""){
        		//$("#routeItemFormDiv").show();
        		$("#routeItemId").val("");
        	}else{
        		alert("请先添加合同！");
        		return false;
        	}
        });
        
        //点击保存的事件，保存干线信息
        //routeItemForm 不需要提交
        $("#saveRouteBtn").click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //异步向后台提交数据
            $.post('/yh/customerContract/routeAdd', $("#routeItemForm").serialize(), function(data){
                    //保存成功后，刷新列表
                    console.log(data);
                    if(data.success){
                    	$('#myModal').modal('hide');
                    	$('#reset').click();
                    	dataTable.fnDraw();
                    	dataTable2.fnDraw();
                    	dataTable3.fnDraw();
                    }else{
                        alert('数据保存失败。');
                    }
                },'json');
        });

        //获取客户的list，选中信息自动填写其他信息
        $('#companyName').on('keyup', function(){
			var inputStr = $('#companyName').val();
			var type = $("#type2").val();
			var type2 = $("#type3").val();
			 var urlSource;
			if(type=='CUSTOMER'||type2=='CUSTOMER'){
				urlSource ="/yh/customerContract/search";
			}else{
				urlSource ="/yh/spContract/search2";
			}
			$.get(urlSource, {locationName:inputStr}, function(data){
				console.log(data);
				var companyList =$("#companyList");
				companyList.empty();
				for(var i = 0; i < data.length; i++)
				{
					companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+"</a></li>");
				}
				companyList.show();
			},'json');
		});
	
		$('#companyList').on('click', '.fromLocationItem', function(e){
			//方法已经对了，只是没有取对值
			$('#companyName').val($(this).text());
        	$("#companyList").hide();
        	$('#name').val($(this).attr('contact_person'));
        	$('#address').val($(this).attr('address'));
        	$('#phone').val($(this).attr('phone'));
        	$('#post_code').val($(this).attr('post_code'));
        	$('#email').val($(this).attr('email'));
        	$('#partyid').val($(this).attr('partyId'));
        	/*$('#label').html($(this).attr('contact_person'));
        	$('#label2').html($(this).attr('phone'));
        	$('#label3').html($(this).attr('email'));
        	$('#label4').html($(this).attr('address'));
        	$('#label5').html($(this).attr('post_code'));*/
        	
        });
		
		
		 //添加合同
		$("#saveContract").click(function(e){
	            //阻止a 的默认响应行为，不需要跳转			    
	            e.preventDefault();
	            //提交前，校验数据
	            if(!$("#customerForm").valid())
	            	return;
	            //异步向后台提交数据
	            $.post('/yh/customerContract/save', $("#customerForm").serialize(), function(contractId){
	                    
	                    if(contractId>0){
	                        //alert("添加合同成功！");
	                    	$("#style").show();
	                    	//已经有一个重复的contractId 在前面了
	                    	$('#routeContractId').val(contractId);
	                    	dataTable.fnSettings().sAjaxSource="/yh/spContract/routeEdit?routId="+contractId;
	                    	dataTable2.fnSettings().sAjaxSource="/yh/spContract/routeEdit2?routId="+contractId;
	                    	dataTable3.fnSettings().sAjaxSource="/yh/spContract/routeEdit3?routId="+contractId;
	                    	
	                    }else{
	                        alert('数据保存失败。');
	                    }
	                    
	                },'json');
	        });
		 
		
		//选择出发地点
		$('#fromName').on('keyup', function(){
			var inputStr = $('#fromName').val();
			$.get('/yh/route/search', {locationName:inputStr}, function(data){
				console.log(data);
				var fromLocationList =$("#fromLocationList");
				fromLocationList.empty();
				for(var i = 0; i < data.length; i++)
				{
					fromLocationList.append("<li><a tabindex='-1' class='fromLocationItem' code='"+data[i].CODE+"'>"+data[i].NAME+"</a></li>");
				}
				fromLocationList.show();
			},'json');
			$("#fromLocationList").css({ 
	        	left:$(this).position().left+"px", 
	        	top:$(this).position().top+30+"px" 
	        }); 
			
		});
		//失去焦点隐藏
		$('#fromName').on('blur', function(){
			$("#fromLocationList").delay(120).hide(1);
		});
		
		$('#fromLocationList').on('click', '.fromLocationItem', function(e){
			$('#from_id').val($(this).attr('code'));
			$('#fromName').val($(this).text());
        	$("#fromLocationList").hide();
        	 /*var inputStr = $('#fromName').val();
			 var inputStr2 = $('#toName').val();
			 if(inputStr!=''&&inputStr2!=''){
				 $.get('/yh/spContract/searchRoute', {fromName:inputStr,toName:inputStr2}, function(data){
					 for(var i = 0; i < data.length; i++){
						 	$('#routeItemId').val(data[i].RID);
					 }
				 },'json');
			 }*/
    	});
		
		//选择目的地点
		$('#toName').on('keyup', function(){
			var inputStr = $('#toName').val();
			$.get('/yh/route/search', {locationName:inputStr}, function(data){
				
				var toLocationList =$("#toLocationList");
				toLocationList.empty();
				for(var i = 0; i < data.length; i++)
				{
					toLocationList.append("<li><a tabindex='-1' class='fromLocationItem' code='"+data[i].CODE+"'>"+data[i].NAME+"</a></li>");
				}
				toLocationList.show();
			},'json');
			$("#toLocationList").css({ 
	        	left:$(this).position().left+"px", 
	        	top:$(this).position().top+30+"px" 
	        }); 
			
		});
		
		//失去焦点隐藏
		$('#toName').on('blur', function(){
			$("#toLocationList").delay(120).hide(1);
		});
		
		$('#toLocationList').on('click', '.fromLocationItem', function(e){
			$('#to_id').val($(this).attr('code'));
			$('#toName').val($(this).text());
        	$("#toLocationList").hide();
        	/* var inputStr = $('#fromName').val();
			 var inputStr2 = $('#toName').val();
			 if(inputStr!=''&&inputStr2!=''){
				 $.get('/yh/spContract/searchRoute', {fromName:inputStr,toName:inputStr2}, function(data){
					 for(var i = 0; i < data.length; i++){
						 	$('#routeId').val(data[i].RID);
					 }
				 },'json');
			 }*/
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
	                    return "<a class='btn btn-success ' href='/yh/transferOrder/edit/"+obj.aData.ID+"'>"+
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

	    $("#changePage").click(function(){
	    	var type= $("#type3").val();
	    	var type2= $("#type2").val();
	    	if(type=='CUSTOMER'||type2=='CUSTOMER'){
	    		window.location.href="/yh/customerContract";
	    	}if(type=='SERVICE_PROVIDER'||type2=='SERVICE_PROVIDER'){
	    		window.location.href="/yh/spContract";
	    	}if(type=='DELIVERY_SERVICE_PROVIDER'||type2=='DELIVERY_SERVICE_PROVIDER'){
	    		window.location.href="/yh/deliverySpContract";
	    	}
	    });

	    $(function(){
	    	 var type= $("#type2").val();
	    	 var type2= $("#type3").val();
	    	 $('#reset').hide();
	    	 if(type!=null||type!=''){
		 	    if(type=='CUSTOMER'){
		 	    	$("#labeltext").html("创建新客户合同");
		 	    }if(type=='SERVICE_PROVIDER'){
		 	    	$("#labeltext").html("创建干线供应商合同");
		 	    }if(type=='DELIVERY_SERVICE_PROVIDER'){
		 	    	$("#labeltext").html("创建配送供应商合同");
		 	    }
	    	 }if(type2!=null||type2!=''){
	    		 if(type2=='CUSTOMER'){
			 	    	$("#labeltext").html("编辑新客户合同");
			 	    }if(type2=='SERVICE_PROVIDER'){
			 	    	$("#labeltext").html("编辑干线供应商合同");
			 	    }if(type2=='DELIVERY_SERVICE_PROVIDER'){
			 	    	$("#labeltext").html("编辑配送供应商合同");
			 	    }
	    	 }
	    }) ;
	   
	    $("input[type='radio'][name='priceType']").change(function(){
	    	hidePriceElements();
	    	var val=$('input[type="radio"][name="priceType"]:checked').val(); // 获取一组radio被选中项的值   
	    	if(val=="计件"){
	    		$("#carType").hide();
	    		$("#carLength").hide();
	    		$("#ltlUnitType").hide();
	    	}else if(val=="整车"){
	    		$("#carType").show();
	    		$("#carLength").show();
	    		$("#ltlUnitType").hide();
	    		//$("#cargoNature").hide();
	    	}else if(val=="零担"){
				$("#carType").hide();
	    		$("#carLength").hide();
	    		$("#ltlUnitType").show();
	    	}

	    });	

	    var hidePriceElements=function(){
	    	$("#carType").hide();
	    	$("#carLength").hide();
	    	$("#ltlUnitType").hide();
	    };

	    
	    hidePriceElements();
	    
});
