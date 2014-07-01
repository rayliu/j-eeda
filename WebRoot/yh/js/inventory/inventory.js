$(document).ready(function() {
	$('#menu_warehouse').addClass('active').find('ul').addClass('in');
	
	//入库单list
	 $('#example').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
		 	"iDisplayLength": 10,
	        "bServerSide": true,
	        "bRetrieve": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": null,
			"aoColumns": [
				{ "mDataProp": "USER_NAME" },
				{ "mDataProp": null },
	            { "mDataProp": "PASSWORD_HINT" },
	            { 
	                "mDataProp": null,
	                "sWidth": "15%",
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-info editbutton' href='/yh/loginUser/edit/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-edit'> </i> "+
	                                "编辑"+
	                            "</a>"+
	                            "<a class='btn btn-danger' href='/yh/loginUser/del/"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                                "删除"+
	                            "</a>";
	                }
	            } 
	            ]
	} );
	
	 //入库产品list
	 productDataTable2 = $('#itemTable').dataTable({
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	        "bServerSide": true,
	        "bRetrieve": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource":"/yh/gateIn/gateInProductlist",
	        "aoColumns": [
	            {"mDataProp":"ITEM_NAME"},
	            {"mDataProp":"ITEM_NO"},        	
	            {"mDataProp":"SIZE"},
	            {"mDataProp":"WIDTH"},
	            {"mDataProp":"UNIT"},
	            {"mDataProp":"VOLUME"},
	            {"mDataProp":"WEIGHT"},
	            {"mDataProp":"ITEM_DESC"},
	            { 
	                "mDataProp": null, 
	                "sWidth": "8%",                
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-danger deleteProduct' id='"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                            "</a>";
	                }
	            }      
	        ],      
	    });
	 //出库单list
	 
	 //选择客户 
	 $('#customerMessage').on('keyup click', function(){
			var inputStr = $('#customerMessage').val();
			$.get('/yh/gateIn/searchCustomer', {input:inputStr}, function(data){
				console.log(data);
				var customerList =$("#customerList");
				customerList.empty();
				for(var i = 0; i < data.length; i++)
				{
					customerList.append("<li><a tabindex='-1' class='fromLocationItem'  code='"+data[i].PID+"'>"+data[i].COMPANY_NAME+"</a></li>");
				}
			},'json');
			$("#customerList").css({ 
	        	left:$(this).position().left+"px", 
	        	top:$(this).position().top+32+"px" 
	        }); 
	        $('#customerList').show();
		});
	 	$('#customerMessage').on('blur', function(){
			$("#customerList").delay(120).hide(1);
		});
		// 选中供应商
		$('#customerList').on('click', '.fromLocationItem', function(e){
			var partyId =$(this).attr('code');
			$('#customerMessage').val($(this).text());
			$('#party_id').val(partyId);
			//productDataTable.fnSettings().sAjaxSource = "/yh/gateIn/gateInProductlist?categoryId="+partyId;
			//productDataTable.fnDraw();
			$('#customerList').hide();
	    }); 
		
		/*var trArr=[];
		$("#productId").on('click', function(e){
			 e.preventDefault();
			$("#itemTable2 tr:not(:first)").each(function(){
	        	$("input:checked",this).each(function(){
	        		trArr.push($(this).val()); 
	        	});
	        	}); 
			console.log(trArr);
			$.post('/yh/gateIn/gateInProductlist2?localArr='+trArr,function(data){
	               //保存成功后，刷新列表
	               console.log(data);
	               if(data.success){
	            	   productDataTable2.fnDraw();
	               }else{
	                   alert('取消失败');
	               }
	           },'text');*/
			 	//$('#localArr').val(trArr);
	            //$('#createForm').submit();
		//});
		
		//获取货品的名称list，选中信息在下方展示其他信息
	 	$('#itemNameMessage').on('keyup click', function(){
	 		var inputStr = $('#itemNameMessage').val();
	 		var customerId = $('#party_id').val();
	 		$.get('/yh/transferOrder/searchItemName', {input:inputStr,customerId:customerId}, function(data){
	 			console.log(data);
	 			var itemNameList =$("#itemNameList");
	 			itemNameList.empty();
	 			for(var i = 0; i < data.length; i++)
	 			{
	 				var item_name = data[i].ITEM_NAME;
	 				if(item_name == null){
	 					item_name = '';
	 				}
	 				itemNameList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' cid='"+data[i].CATEGORY_ID+"' item_no='"+data[i].ITEM_NO+"' size='"+data[i].SIZE+"' height='"+data[i].HEIGHT+"' width='"+data[i].WIDTH+"' unit='"+data[i].UNIT+"' volume='"+data[i].VOLUME+"' weight='"+data[i].WEIGHT+"', item_desc='"+data[i].ITEM_DESC+"', >"+data[i].ITEM_NAME+"</a></li>");
	 			}
	 		},'json');		
	 		$("#itemNameList").css({ 
	 			left:$(this).position().left+"px", 
	 			top:$(this).position().top+32+"px" 
	 		}); 
	 		$('#itemNameList').show();        
	 	});
	 	$('#itemNameMessage').on('blur', function(){
			$("#itemNameList").delay(120).hide(1);
		});
	 // 选中产品名
	 	$('#itemNameList').on('click', '.fromLocationItem', function(e){
	 		$("#itemNameMessage").val($(this).text());
	 		if($(this).attr('item_no') == 'null'){
	 			$("#item_no").val('');
	 		}else{
	 			$("#itemNoMessage").val($(this).attr('item_no'));
	 		}
	 		if($(this).attr('size') == 'null'){
	 			$("#size").val('');
	 		}else{
	 			$("#size").val($(this).attr('size'));
	 		}
	 		if($(this).attr('width') == 'null'){
	 			$("#width").val('');
	 		}else{
	 			$("#width").val($(this).attr('width'));
	 		}
	 		if($(this).attr('unit') == 'null'){
	 			$("#unit").val('');
	 		}else{
	 			$("#unit").val($(this).attr('unit')); 			
	 		}
	 		if($(this).attr('volume') == 'null'){
	 			$("volume").val('');
	 		}else{
	 			$("#volume").val($(this).attr('volume'));
	 		}
	 		if($(this).attr('weight') == 'null'){
	 			$("weight").val('');
	 		}else{
	 			$("#weight").val($(this).attr('weight'));
	 		}
	 		if($(this).attr('height') == 'null'){
	 			$("height").val('');
	 		}else{
	 			$("#height").val($(this).attr('height'));
	 		}
	 		if($(this).attr('item_desc') == 'null'){
	 			$("remark").val('');
	 		}else{
	 			$("#remark").val($(this).attr('item_desc'));
	 		}
	 		$("#productId").val($(this).attr('id'));
	 		$('#itemNameList').hide();
	 	});  	
	 	
	 	//获取货品的序列号list，选中信息在下方展示其他信息
	 	$('#itemNoMessage').on('keyup click', function(){
	 		var inputStr = $('#itemNoMessage').val();
	 		var customerId = $('#party_id').val();
	 		$.get('/yh/transferOrder/searchItemNo', {input:inputStr,customerId:customerId}, function(data){
	 			console.log(data);
	 			var itemNoList =$("#itemNoList");
	 			itemNoList.empty();
	 			for(var i = 0; i < data.length; i++)
	 			{
	 				var item_no = data[i].ITEM_NO;
	 				if(item_no == null){
	 					item_no = '';
	 				}
	 				itemNoList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' cid='"+data[i].CATEGORY_ID+"' item_name='"+data[i].ITEM_NAME+"' size='"+data[i].SIZE+"' height='"+data[i].HEIGHT+"' width='"+data[i].WIDTH+"' unit='"+data[i].UNIT+"' volume='"+data[i].VOLUME+"' weight='"+data[i].WEIGHT+"', item_desc='"+data[i].ITEM_DESC+"', >"+data[i].ITEM_NO+"</a></li>");
	 			}
	 		},'json');		
	         $("#itemNoList").css({ 
	         	left:$(this).position().left+"px", 
	         	top:$(this).position().top+32+"px" 
	         }); 
	         $('#itemNoList').show();        
	 	});
	 	$('#itemNoMessage').on('blur', function(){
			$("#itemNoList").delay(120).hide(1);
		});
	 	
	 // 选中序列号
	 	$('#itemNoList').on('click', '.fromLocationItem', function(e){
	 		$("#itemNoMessage").val($(this).text());
	 		if($(this).attr('item_name') == 'null'){
	 			$("#item_name").val('');
	 		}else{
	 			$("#itemNameMessage").val($(this).attr('item_name'));
	 		}
	 		if($(this).attr('size') == 'null'){
	 			$("#size").val('');
	 		}else{
	 			$("#size").val($(this).attr('size'));
	 		}
	 		if($(this).attr('width') == 'null'){
	 			$("#width").val('');
	 		}else{
	 			$("#width").val($(this).attr('width'));
	 		}
	 		if($(this).attr('unit') == 'null'){
	 			$("#unit").val('');
	 		}else{
	 			$("#unit").val($(this).attr('unit')); 			
	 		}
	 		if($(this).attr('volume') == 'null'){
	 			$("volume").val('');
	 		}else{
	 			$("#volume").val($(this).attr('volume'));
	 		}
	 		if($(this).attr('weight') == 'null'){
	 			$("weight").val('');
	 		}else{
	 			$("#weight").val($(this).attr('weight'));
	 		}
	 		if($(this).attr('height') == 'null'){
	 			$("height").val('');
	 		}else{
	 			$("#height").val($(this).attr('height'));
	 		}
	 		if($(this).attr('item_desc') == 'null'){
	 			$("remark").val('');
	 		}else{
	 			$("#remark").val($(this).attr('item_desc'));
	 		}
	 		$("#productId").val($(this).attr('id'));
	        $('#itemNoList').hide();
	     }); 
	 	
	 	 // 获取所有仓库
		 $.post('/yh/gateIn/searchAllwarehouse',function(data){
			 if(data.length > 0){
				 var warehouseSelect = $("#warehouseSelect");
				 warehouseSelect.empty();
				 for(var i=0; i<data.length; i++){
					 warehouseSelect.append("<option class='form-control' value='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</option>");
				 }
			 }
		 },'json');
		 //获取仓库ID
		 $("#warehouseSelect").on('change',function(){
			 $("#warehouseId").val($("#warehouseSelect").val());
		 });
		 
		 //保存入库单
		 $("#saveInventoryBtn").click(function(e){
	            //阻止a 的默认响应行为，不需要跳转	
			 //var itemId = $("#item_id").val();
	            e.preventDefault();
	            //异步向后台提交数据
	           $.post('/yh/gateIn/gateInSave',$("#inventoryForm").serialize(), function(data){
	                console.log(data);
                 if(data>0){
                	 console.log(data);
                 	$("#party_id").val(data);
                 	$("#style").show();
                 	//$("#ConfirmationBtn").attr("disabled", false);
                 }else{
                     alert('数据保存失败。');
                 }
	             },'json');
	        });
});