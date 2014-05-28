$(document).ready(function() {
	
      /* $('#leadsForm').validate({
           rules: {
           	name: {
               required: true
             },
             type:{//form 中 name为必填
               required: true
             }
           },
           highlight: function(element) {
               $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
           },
           success: function(element) {
               element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
           }
       });*/
       /*
       $('#dataTables-example').dataTable({
           //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
           "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
           //"sPaginationType": "bootstrap",
           "iDisplayLength": 10,
       	"oLanguage": {
               "sUrl": "/eeda/dataTables.ch.txt"
           },
           "bProcessing": true,
           "bServerSide": true,
           "sAjaxSource": "/yh/account/accountItem",
           "aoColumns": [   
               {"mDataProp":"ORG_NAME"},
               {"mDataProp":"ACCOUNT_PIN"},
               {"mDataProp":"CURRENCY"},
               {"mDataProp":"ORG_PERSON"},
               { 
                   "mDataProp": null, 
                   "sWidth": "8%",                
                   "fnRender": function(obj) {                    
                	   return "<a class='btn btn-success ' href='/yh/transferOrder/edit/"+obj.aData.ID+"'>"+
			                       "<i class='fa fa-edit fa-fw'></i>"+
			                       "编辑"+
			                   "</a>"+
			                   "<a class='btn btn-danger cancelbutton' href='/yh/transferOrder/edit/"+obj.aData.ID+"'>"+
			                       "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                       "删除"+
			                   "</a>";
                   }
               }                         
           ],
        });*/
      /* $("#btn").click(function(){
       	var accountId = $("#accountId").val();
       	if(accountId != ""){
       		//$("#routeItemFormDiv").show();
       		$("#accountItemId").val("");
       	}else{
       		alert("请先添加合同！");
       		return false;
       	}
       });
       */
      
       
      /*//添加账户
		$('#save').click(function(){
			 $.post('/yh/account/save', $("#accountFrom").serialize(), function(data){
		         if(data>0){
		             //alert("添加合同成功！");
		         	$("#style").show();
		         	//已经有一个重复的contractId 在前面了
		         	$('#accountId').val(data);
		         	// dataTable.fnSettings().sAjaxSource="/yh/account/accountItem?routId="+contractId; 
		         }else{
		             alert('数据保存失败。');
		         }
		         
		     },'json');
		});*/
});