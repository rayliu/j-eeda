$(document).ready(function() {
	if(bank_name){
		document.title = bank_name +' | '+document.title;
	}
	$('#menu_profile').addClass('active').find('ul').addClass('in');
	var accountId = $("#accountId2").val();
	var dataTable= $('#dataTables-example').dataTable({
	    //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	    //"sPaginationType": "bootstrap",
	    "iDisplayLength": 10,
	    "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
		"oLanguage": {
	        "sUrl": "/eeda/dataTables.ch.txt"
	    },
	    "bProcessing": true,
	    "bServerSide": true,
	    "sAjaxSource": "/account/accountItem?accountId="+accountId,
	    "aoColumns": [   
	        {"mDataProp":null},
	        {"mDataProp":null},
	        {"mDataProp":null},
	        {"mDataProp":null},
	        { 
	            "mDataProp": null, 
	            "sWidth": "8%",                
	            "fnRender": function(obj) {                    
	         	   return "<a class='btn btn-success eidtAcountItem' code = '"+obj.aData.FID+"' target='_blank'>"+
			                       "<i class='fa fa-edit fa-fw'></i>"+
			                       "编辑"+
			                   "</a>"+
			                   "<a class='btn btn-danger delectAcountItem' code2 ='"+obj.aData.FID+"'>"+
			                       "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                       "删除"+
			                   "</a>";
	            }
	        }                          
	    ],
	 });
	$("#dataTables-example").on('click', '.eidtAcountItem', function(){
		  var accountId = $("#accountId2").val();
		 	var id = $(this).attr('code');
		 $.post('/account/eidtAcountItem/'+id,{accountId:accountId},function(data){
	       //保存成功后，刷新列表
	       console.log(data);
	       if(data[0] !=null){
	      	 $('#myModal').modal('show');
	      	 $('#currency').val(data[0].CURRENCY);
	      	 $('#org_name').val(data[0].ORG_NAME);
	      	 $('#account_pin').val(data[0].ACCOUNT_PIN);
	      	 $('#account_person').val(data[0].ORG_PERSON);
	      	 $('#accountItemId').val(data[0].ID);
	       }else{
	           alert('取消失败');
	       }
	   },'json');
	});
	$("#dataTables-example").on('click', '.delectAcountItem', function(){
		  var accountId = $("#accountId2").val();
		 	var id = $(this).attr('code2');
		 $.post('/account/delectAcountItem/'+id,{accountId:accountId},function(data){
	     //保存成功后，刷新列表
	     //console.log(data);
	     if(data.success){
	    	 dataTable.fnDraw();
	     }else{
	         alert('取消失败');
	     }
	 },'json');
	});
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
			    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
			    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
			    '</div>';
	$('body').append(alerMsg);
	
	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	//添加账户
	$('#save').click(function(){
		if($("#accountFrom").valid() == false){
			
			//$.scojs_message('保存失败，请完善信息', $.scojs_message.TYPE_ERROR);
			return false;
		}
		 $.post('/account/save', $("#accountFrom").serialize(), function(data){
	         if(data.ID > 0){
	             //alert("添加合同成功！");
	         	//$("#style").show();
	         	//已经有一个重复的contractId 在前面了
	         	$('#accountId2').val(data);
	         	dataTable.fnSettings().sAjaxSource="/account/accountItem?accountId="+data.ID; 
	         	contactUrl("edit?id",data.ID);
	         	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	         }else{
	        	 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	         }
	         
	     },'json');
	});
	$("#btn").click(function(){
	   	var accountId = $("#accountId2").val();
	   	if(accountId != ""){
	   		//$("#routeItemFormDiv").show();
	   		$("#accountItemId").val("");
	   	}else{
	   		alert("请先保存当前信息！");
	   		return false;
	   	}
	   });
	$('#saveAccountItemBtn').click(function(){
			 $.post('/account/saveAccountItemBtn', $("#accountItemForm").serialize(), function(data){
		         if(data.success){
		        	 $('#myModal').modal('hide');
		        	 dataTable.fnDraw();
		         }else{
		             alert('数据保存失败。');
		         }
		         
		     },'json');
	});
	$('#accountFrom').validate({
        rules: {
       	 bank_name: {
            required: true
          },
          bank_person: {
            required: true
          },
          account_no:{
            required: true
          },
          type:{
        	  required: true
          }
        },
        messages:{
        	bank_name: {
                required: "账户名称不能为空"
              },
              bank_person: {
                required: "开户人姓名不能为空"
              },
              account_no:{
                required: "银行账户号码不能为空"
              },
              type:{
            	  required: "账户类型不能为空"
              }
        },
        highlight: function(element) {
            $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
        },
        success: function(element) {
            element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
        }
    });


});