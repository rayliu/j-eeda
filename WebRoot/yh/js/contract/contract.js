$(document).ready(function() {
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
	            {"mDataProp":"LOCATION_FROM"},
	            {"mDataProp":"LOCATION_TO"},
	            {"mDataProp":"AMOUNT"},
	            { 
	                "mDataProp": null, 
	                "sWidth": "8%",                
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-success' id='111' href='/yh/customerContract/roteEdit"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-edit fa-fw'></i>"+
	                                "编辑"+
	                            "</a>"+
	                            "<a class='btn btn-danger' href='/yh/customerContract/roteDelete"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                                "删除"+
	                            "</a>";
	                }
	            }                         
	        ]
	        // "fnDrawCallback": function () {//行编辑模式
	        //     $('#ataTables-example tbody td').editable( '/yh/customerContract/roteEdit', {
	        //         "callback": function( sValue, y ) {
	        //             /* Redraw the table from the new data on the server */
	        //             oTable.fnDraw();
	        //         },
	        //         "height": "14px"
	        //     } );
	        // }
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
        	var contractId = $("#contractId").val();
        	
        	if(contractId != ""){
        		$("#routeItemFormDiv").show();
        	}else{
        		alert("请先添加合同！");
        		
        	}
        });
        $("#cancel").click(function(){
        	$("#routeItemFormDiv").hide();
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
                    	dataTable.fnDraw();
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
        	$('#label').html($(this).attr('contact_person'));
        	$('#label2').html($(this).attr('phone'));
        	$('#label3').html($(this).attr('email'));
        	$('#label4').html($(this).attr('address'));
        	$('#label5').html($(this).attr('post_code'));
        	
        });
		
		//添加routeItemForm  出发地点
		 
		//添加routeItemForm 目的地点
		 $('#toName').on('keyup', function(){
			 var inputStr = $('#fromName').val();
			 var inputStr2 = $('#toName').val();
			 $.get('/yh/spContract/searchRoute', {fromName:inputStr,toName:inputStr2}, function(data){
				 for(var i = 0; i < data.length; i++){
					 	$('#routeId').val(data[i].RID);
				 }
			 },'json');
		});
		 $('#fromName').on('keyup', function(){
			 var inputStr = $('#fromName').val();
			 var inputStr2 = $('#toName').val();
			 $.get('/yh/spContract/searchRoute', {fromName:inputStr,toName:inputStr2}, function(data){
				 for(var i = 0; i < data.length; i++){
					 	$('#routeId').val(data[i].RID);
				 }
			 },'json');
		});
		 
		 //添加合同
		$("#saveContract").click(function(e){
	            //阻止a 的默认响应行为，不需要跳转
			//validator.resetForm();
	            e.preventDefault();
	            //异步向后台提交数据
	            $.post('/yh/customerContract/save', $("#customerForm").serialize(), function(contractId){
	                    
	                    if(contractId>0){
	                        //alert("保存合同成功！");
	                    	$("#style").show();
	                    	//已经有一个重复的contractId 在前面了
	                    	$('#routeContractId').val(contractId);
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
		});
	
		$('#fromLocationList').on('click', '.fromLocationItem', function(e){
			$('#from_id').val($(this).attr('code'));
			$('#fromName').val($(this).text());
        	$("#fromLocationList").hide();
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
		});
	
		$('#toLocationList').on('click', '.fromLocationItem', function(e){
			$('#to_id').val($(this).attr('code'));
			$('#toName').val($(this).text());
        	$("#toLocationList").hide();
    	});
    });
