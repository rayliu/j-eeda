$(document).ready(function() {
    $('#menu_profile').addClass('active').find('ul').addClass('in');
    
    //获取客户的list，选中信息在下方展示其他信息
	$('#customerMessage').on('keyup', function(){
		var inputStr = $('#customerMessage').val();
		if(inputStr == ""){
			var pageCustomerName = $("#pageCustomerName");
			pageCustomerName.empty();
			var pageCustomerAddress = $("#pageCustomerAddress");
			pageCustomerAddress.empty();
			$('#customer_id').val($(this).attr(''));
		}
		$.get('/yh/transferOrder/searchCustomer', {input:inputStr}, function(data){
			console.log(data);
			var customerList =$("#customerList");
			customerList.empty();
			for(var i = 0; i < data.length; i++)
			{
				customerList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+" "+data[i].CONTACT_PERSON+" "+data[i].PHONE+"</a></li>");
			}
		},'json');
		
		$("#customerList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#customerList').show();
	});
	
	// 选中客户
	$('#customerList').on('click', '.fromLocationItem', function(e){
		$("#treeListDiv").show();
		var message = $(this).text();
		$('#customerMessage').val(message.substring(0, message.indexOf(" ")));
		var customerId = $(this).attr('partyId');
		$('#customerId').val(customerId);
		$('#hiddenCustomerId').val($(this).attr('partyId'));
		var pageCustomerName = $("#pageCustomerName");
		pageCustomerName.empty();
		pageCustomerName.append($(this).attr('contact_person')+'&nbsp;');
		pageCustomerName.append($(this).attr('phone')); 
		var pageCustomerAddress = $("#pageCustomerAddress");
		pageCustomerAddress.empty();
		pageCustomerAddress.append($(this).attr('address'));

        $('#customerList').hide();
        
        $("#parentTree").text($("#customerMessage").val());
        
        // 查出所有的类别
        if(customerId > 0){
    	    $.get('/yh/product/searchAllCategory', {customerId:customerId}, function(data){
    			console.log(data);	
    			var productTreeUl = $("#productTreeUl");
    			productTreeUl.empty();
    			if(data.length > 0){
    				for(var i = 0; i < data.length; i++)
    				{
    					productTreeUl.append("<li class='parent_li'><span><i class='fa fa-folder'>" +
    							"</i>  <a href='javascript:void(0)' class='productList' id='"+data[i].ID+"'>"+data[i].NAME+
    							"</a></span>  <div class='edit_icons'><a class='fa fa-plus addCategory' href='javascript:void(0)' title='添加' id='"+data[i].ID+"'></a>  " +
    							"<a class='fa fa-pencil editCategory' href='javascript:void(0)' title='编辑' id='"+data[i].ID+"'></a>  <a class='fa fa-trash-o deleteCategory' href='javascript:void(0)' title='删除' id='"+data[i].ID+"'></a></div></li></ul></li>");
    				}
    			}
    		},'json');
        }
        $("#addProductDiv").show();
        $("#displayDiv").hide();
    }); 
	
    // 保存类别
    $("#categoryFormBtn").click(function(){
    	var parentId = $("#hiddenParentId").val();
    	$.post('/yh/product/saveCategory', $("#categoryForm").serialize(), function(data){
			//保存成功后，刷新列表
            console.log(data);
            if(data.ID>0){
            	// 查出所有的类别
                var customerId = $('#customerId').val();
                if(customerId > 0){
                	$.get('/yh/product/searchAllCategory', {customerId:customerId}, function(data){
            			console.log(data);	
            			var productTreeUl = $("#productTreeUl");
            			productTreeUl.empty();
            			if(data.length > 0){
            				for(var i = 0; i < data.length; i++)
            				{
            					if(parentId == ""){
	            					productTreeUl.append("<li class='parent_li'><span><i class='fa fa-folder'>" +
	            							"</i>  <a href='javascript:void(0)' class='productList' id='"+data[i].ID+"'>"+data[i].NAME+
	            							"</a></span>  <div class='edit_icons'><a class='fa fa-plus addCategory' href='javascript:void(0)' title='添加' id='"+data[i].ID+"'></a>  " +
	            							"<a class='fa fa-pencil editCategory' href='javascript:void(0)' title='编辑' id='"+data[i].ID+"'></a>  <a class='fa fa-trash-o deleteCategory' href='javascript:void(0)' title='删除' id='"+data[i].ID+"'></a></div></li></ul></li>");
            					}else{
            						productTreeUl.append("<li class='parent_li'><span><i class='fa fa-folder'>" +
	            							"</i>  <a href='javascript:void(0)' class='productList' id='"+data[i].ID+"'>"+data[i].NAME+
	            							"</a></span>  <div class='edit_icons'><a class='fa fa-plus addCategory' href='javascript:void(0)' title='添加' id='"+data[i].ID+"'></a>  " +
	            							"<a class='fa fa-pencil editCategory' href='javascript:void(0)' title='编辑' id='"+data[i].ID+"'></a>  <a class='fa fa-trash-o deleteCategory' href='javascript:void(0)' title='删除' id='"+data[i].ID+"'></a></div></li></ul></li>");
            					}
            				}
            			}
            		},'json');
                }
            }else{
                alert('数据保存失败。');
            }
		},'json');
    	$('#editCategory').modal('hide');
    	$("#categoryForm")[0].reset();
    });
    
	// 树状结构点击效果
    $('.tree li:has(ul)').addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
    $('.tree li.parent_li > span').on('click', function (e) {
        var children = $(this).parent('li.parent_li').find(' > ul > li');
        if (children.is(":visible")) {
            children.hide('fast');
            $(this).attr('title', 'Expand this branch').find(' > i').addClass('icon-plus-sign').removeClass('icon-minus-sign');
        } else {
            children.show('fast');
            $(this).attr('title', 'Collapse this branch').find(' > i').addClass('icon-minus-sign').removeClass('icon-plus-sign');
        }
        e.stopPropagation();
    });

	//datatable, 动态处理
    productDataTable = $('#eeda-table').dataTable({
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "bRetrieve": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/product/list",
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
                    return "<a class='btn btn-success editProduct' id='"+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger deleteProduct' id='"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ],      
    });
    
    $("#productTreeLi").on('click', '.productList', function(e){
    	e.preventDefault();
    	var categoryId = $(this).attr('id');
    	$("#hiddenCategoryId").val(categoryId);
    	$("#categoryId").val(categoryId);
    	
    	if($("#displayDiv").attr('style') != ""){
    		$("#displayDiv").show();
    		productDataTable.fnSettings().sAjaxSource = "/yh/product/list?categoryId="+categoryId;
        	productDataTable.fnDraw();
    	}else{
    		productDataTable.fnSettings().sAjaxSource = "/yh/product/list?categoryId="+categoryId;
        	productDataTable.fnDraw();
    	} 
    	
    	// 获取所有类别
    	var customerId = $("#customerId").val();
    	$.get('/yh/product/searchAllCategory', {customerId:customerId}, function(data){
	   		if(data.length > 0){
		   		var categorySelect = $("#categorySelect");
		   		categorySelect.empty();
		   		for(var i=0; i<data.length; i++){
			   		 if(data[i].ID == categoryId){
			   			 categorySelect.append("<option class='form-control' value='"+data[i].ID+"' selected='selected'>"+data[i].NAME+"</option>");
			   		 }else{
			   			 categorySelect.append("<option class='form-control' value='"+data[i].ID+"'>"+data[i].NAME+"</option>");					 
			   		 }
		   		}
	   		}
   	    },'json');
    });
	
    // 保存产品
    $("#productFormBtn").click(function(){
    	$.post('/yh/product/save', $("#productForm").serialize(), function(data){
			//保存成功后，刷新列表
            console.log(data);
            if(data.ID>0){
            	// 查出所有的类别
                var customerId = $('#customerId').val();
                if(customerId > 0){
                	$.get('/yh/product/searchAllCategory', {customerId:customerId}, function(data){
            			console.log(data);	
            			var productTreeUl = $("#productTreeUl");
            			productTreeUl.empty();
            			if(data.length > 0){
            				for(var i = 0; i < data.length; i++)
            				{
            					productTreeUl.append("<li class='parent_li'><span><i class='fa fa-folder'></i>  <a href='javascript:void(0)' class='productList' id='"+data[i].ID+"'>"+data[i].NAME+"</a></li></ul></li>");
            				}
            			}
            		},'json');
                	
                	var categoryId = $("#categoryId").val();
                	productDataTable.fnSettings().sAjaxSource = "/yh/product/list?categoryId="+categoryId;
	            	productDataTable.fnDraw();
                }
            }else{
                alert('数据保存失败。');
            }
			$('#myModal').modal('hide');
	    	//$("#productForm")[0].reset();
		},'json');
    });
    
    // 编辑产品
    $("#eeda-table").on('click', '.editProduct', function(e){
    	var productId = $(this).attr('id');
		$("#productId").val(productId);	
		$.post('/yh/product/getProduct', {productId:productId}, function(data){
  	    	// 编辑时回显数据
			$("#hiddenProductId").val(data.ID);
  	    	$("#item_no").val(data.ITEM_NO);
  	    	$("#item_name").val(data.ITEM_NAME);
  	    	$("#size").val(data.SIZE);	    	
  	    	$("#width").val(data.WIDTH);	    	
  	    	$("#unit").val(data.UNIT);	    	
  	    	$("#volume").val(data.VOLUME);	    	
  	    	$("#weight").val(data.WEIGHT);	    	
  	    	$("#categorySelect").val(data.CATEGORY_ID);	    	
  	    	$("#item_desc").val(data.ITEM_DESC);    	
		},'json');
  		// 模态框:修改货品明细
		$('#myModal').modal('show');	
	});
    
    // 删除产品
    $("#eeda-table").on('click', '.deleteProduct', function(e){
    	var productId = $(this).attr('id');	
    	$.post('/yh/product/delete', {productId:productId}, function(data){ 	
		},'json');

    	var categoryId = $("#categoryId").val();
    	productDataTable.fnSettings().sAjaxSource = "/yh/product/list?categoryId="+categoryId;
    	productDataTable.fnDraw();
    });
    
    // 添加类别
    $("#treeListDiv").on('click', 'a.addCategory', function(){
    	var categoryId = $(this).attr('id');
    	$("#hiddenParentId").val(categoryId);
    	$("#editCategory").modal('show');
    });
    
    // 编辑类别
    $("#treeListDiv").on('click', 'a.editCategory', function(){
    	var categoryId = $(this).attr('id');
    	alert("edit"+$(this).attr('id'));
    });
    
    // 删除类别
    $("#treeListDiv").on('click', 'a.deleteCategory', function(){
    	alert("de"+$(this).attr('id'));
    });
} );