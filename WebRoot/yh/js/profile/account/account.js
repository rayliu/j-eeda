$(document).ready(function() {
	$('#menu_profile').addClass('active').find('ul').addClass('in');
	var accountId = $("#accountId2").val();
	var dataTable= $('#dataTables-example').dataTable({
	    //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	    //"sPaginationType": "bootstrap",
	    "iDisplayLength": 10,
		"oLanguage": {
	        "sUrl": "/eeda/dataTables.ch.txt"
	    },
	    "bProcessing": true,
	    "bServerSide": true,
	    "sAjaxSource": "/yh/account/accountItem?accountId="+accountId,
	    "aoColumns": [   
	        {"mDataProp":"ORG_NAME"},
	        {"mDataProp":"ACCOUNT_PIN"},
	        {"mDataProp":"CURRENCY"},
	        {"mDataProp":"ORG_PERSON"},
	        { 
	            "mDataProp": null, 
	            "sWidth": "8%",                
	            "fnRender": function(obj) {                    
	         	   return "<a class='btn btn-success eidtAcountItem' code = '"+obj.aData.FID+"'>"+
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
		 $.post('/yh/account/eidtAcountItem/'+id,{accountId:accountId},function(data){
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
		 $.post('/yh/account/delectAcountItem/'+id,{accountId:accountId},function(data){
	     //保存成功后，刷新列表
	     console.log(data);
	     if(data.success){
	    	 dataTable.fnDraw();
	     }else{
	         alert('取消失败');
	     }
	 },'json');
	});

	//添加账户
	$('#save').click(function(){
		 $.post('/yh/account/save', $("#accountFrom").serialize(), function(data){
	         if(data>0){
	             //alert("添加合同成功！");
	         	//$("#style").show();
	         	//已经有一个重复的contractId 在前面了
	         	$('#accountId2').val(data);
	         	dataTable.fnSettings().sAjaxSource="/yh/account/accountItem?accountId="+data; 
	         }else{
	             alert('数据保存失败。');
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
			 $.post('/yh/account/saveAccountItemBtn', $("#accountItemForm").serialize(), function(data){
		         if(data.success){
		        	 $('#myModal').modal('hide');
		        	 dataTable.fnDraw();
		         }else{
		             alert('数据保存失败。');
		         }
		         
		     },'json');
	});



});