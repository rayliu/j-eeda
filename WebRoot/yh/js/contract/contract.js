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
                    return "<a class='btn btn-success' id='111' href='/yh/customerContract/edit/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger' href='/yh/customerContract/delete/"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ],
       
            
    });
	
        // $('#customerForm').validate({
        //     rules: {
        //       company_name: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
        //         required: true
        //       },
        //       name:{//form 中 name为必填
        //         required: true
        //       }
        //     },
        //     highlight: function(element) {
        //         $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
        //     },
        //     success: function(element) {
        //         element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
        //     }
        // });
        
        //点击button显示添加合同干线div
        $("#btn").click(function(){
        	$("#routeItemFormDiv").show();
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
			if(type=='CUSTOMER'||type2=='CUSTOMER'){
				$.get('/yh/customerContract/search', {locationName:inputStr}, function(data){
					console.log(data);
					var companyList =$("#companyList");
					companyList.empty();
					for(var i = 0; i < data.length; i++)
					{
						companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+"</a></li>");
					}
					companyList.show();
					
				},'json');
			}else{
				$.get('/yh/spContract/search2', {locationName:inputStr}, function(data){
					console.log(data);
					var companyList =$("#companyList");
					companyList.empty();
					for(var i = 0; i < data.length; i++)
					{
						companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+"</a></li>");
					}
					companyList.show();
					
				},'json');
			}
			
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
        });
		
		//添加routeItemForm  出发地点
		 
		//添加routeItemForm 目的地点
		 $('#toName').on('keyup', function(){
			 var inputStr = $('#fromName').val();
			 var inputStr2 = $('#toName').val();
			  if(inputStr==""){
				 alert("请先输入出发点！");
			 }else{
				 $.get('/yh/spContract/searchRoute', {fromName:inputStr,toName:inputStr2}, function(data){
					 for(var i = 0; i < data.length; i++){
						 if(data.length==""){
							alert("没此路线");
						 }else{
							 $('#routeId').val(data[i].RID);
						 }
						
					 }
				 },'json');
			 }
		});
		 
    });