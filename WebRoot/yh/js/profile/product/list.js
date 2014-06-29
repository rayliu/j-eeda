$(document).ready(function() {
    $('#menu_profile').addClass('active').find('ul').addClass('in');
    
    // 显示所有的客户
    $.get('/yh/product/searchAllCustomer', function(data){
        console.log(data);
        var zNodes =[];
        for(var i=0;i<data.length && data.length>0;i++){
	        var node={};
	        node.name=data[i].ABBR;
	        node.isParent=true;
	        node.customerId=data[i].PID;
	        node.categoryId=data[i].CAT_ID;
	        node.click=nodePlusClickHandler;
	        zNodes.push(node);	
        }
        $.fn.zTree.init($("#categoryTree"), setting, zNodes);
    },'json');
    
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
        var pageCustomerName = $("#pageCustomerName");


        $.get('/yh/product/searchCustomerCategory', {customerId: customerId}, function(data){
            console.log(data);
            var zNodes =[{name: $("#customerMessage").val(), categoryId:data.ID, customerId: customerId, isParent:true}];
            $.fn.zTree.init($("#categoryTree"), setting, zNodes);
        },'json');

       

		$('#hiddenCustomerId').val($(this).attr('partyId'));
		
		pageCustomerName.empty();
		pageCustomerName.append($(this).attr('contact_person')+'&nbsp;');
		pageCustomerName.append($(this).attr('phone')); 
		var pageCustomerAddress = $("#pageCustomerAddress");
		pageCustomerAddress.empty();
		pageCustomerAddress.append($(this).attr('address'));

        $('#customerList').hide();
        
        $("#parentTree").text($("#customerMessage").val());
        
        // 查出customer的子类别
      //   if(customerId > 0){
    	 //    $.get('/yh/product/searchCustomerCategory', {customerId:customerId}, function(data){
    		// 	console.log(data);	
    		// 	var productTreeUl = $("#productTreeUl");
    		// 	productTreeUl.empty();
    		// 	addNode(data, productTreeUl);
    		// },'json');
      //   }
        $("#addProductDiv").show();
        $("#displayDiv").hide();
    }); 
	
    // 保存类别
    $("#categoryFormBtn").click(function(){
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
            			addNode(data, productTreeUl);
//            			if(data.length > 0){
//            				for(var i = 0; i < data.length; i++)
//            				{
//            					var id = data[i].ID;
//            					if(data[i].PARENT_ID == null){
//        	    					productTreeUl.append("<li class='parent_li'><span><i class='fa fa-folder'>" +
//        	    							"</i>  <a href='javascript:void(0)' class='productList' id='"+data[i].ID+"'>"+data[i].NAME+
//        	    							"</a></span>  <div class='edit_icons'><a class='fa fa-plus addCategory' href='javascript:void(0)' title='添加' id='"+data[i].ID+"'></a>  " +
//        	    							"<a class='fa fa-pencil editCategory' href='javascript:void(0)' title='编辑' id='"+data[i].ID+"'></a>  " +
//        	    							"<a class='fa fa-trash-o deleteCategory' href='javascript:void(0)' title='删除' id='"+data[i].ID+"'></a></div></li></ul></li>");        	    					
//            					}
//            				}
//            			}
            			
            			
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
    
    var nodePlusClickHandler= function(e){
    	e.preventDefault();
    	var customerId = $('#customerId').val();
    	var categoryId = $(this).find('li').attr('categoryId');
    	var nodeElement = $(this);
    	nodeElement.unbind('click');
    	$.get('/yh/product/searchNodeCategory', {categoryId: categoryId, customerId: customerId}, function(data){
    		if(data.length > 0){
	    		console.log(data);
	    		addNode(data, nodeElement);
    		}
    	},'json');
    };
    
    var addNode = function(data, nodeElement){
    	nodeElement.find('ul').empty();
    	for(var i = 0; i < data.length; i++){
    		nodeElement.append(function() {
    			return $("<ul><li class='parent_li productList' categoryId='"+data[i].ID+"'><span ><i class='fa fa-folder'>" +    		
				"</i>  <a href='javascript:void(0)' id='"+data[i].ID+"'>"+data[i].NAME+
				"</a></span>  <div class='edit_icons'><a class='fa fa-plus addCategory' href='javascript:void(0)' title='添加' id='"+data[i].ID+"'></a>  " +
				"<a class='fa fa-pencil editCategory' href='javascript:void(0)' title='编辑' id='"+data[i].ID+"'></a>  " +
				"<a class='fa fa-trash-o deleteCategory' href='javascript:void(0)' title='删除' id='"+data[i].ID+"'></a></div></li></ul>").click(nodePlusClickHandler);
    			}
    		);
    	}
    };
    
    $("#productTreeLi").on('click', '.p roductList', function(e){    	
    	//显示子目录
    	var customerId = $('#customerId').val();
    	var categoryId = $(this).attr('categoryId');
    	var nodeElement = $(this);
    	$.get('/yh/product/searchNodeCategory', {categoryId: categoryId, customerId: customerId}, function(data){
    		if(data.length > 0){
	    		console.log(data);
	    		addNode(data, nodeElement);
    		}
    	},'json');
    	//显示产品列表
    	e.preventDefault();
    	/*var categoryId = $(this).attr('id');
    	$("#hiddenCategoryId").val(categoryId);
    	$("#categoryId").val(categoryId);
    	*/
    	if($("#displayDiv").attr('style') != ""){
    		$("#displayDiv").show();
    		productDataTable.fnSettings().sAjaxSource = "/yh/product/list?categoryId="+categoryId;
        	productDataTable.fnDraw();
    	}else{
    		productDataTable.fnSettings().sAjaxSource = "/yh/product/list?categoryId="+categoryId;
        	productDataTable.fnDraw();
    	} 
    });
	
    // 保存产品
    $("#productFormBtn").click(function(){
    	$.post('/yh/product/save', $("#productForm").serialize(), function(data){    
    		//保存成功后，刷新列表
            console.log(data);
            if(data.ID>0){
            	// 查出所有的类别
            	searchAllCategory();
            }else{
                alert('数据保存失败。');
            }
    		$('#myModal').modal('hide');	
        },'json');
    });
    
    var searchAllCategory=function(){
    	var customerId = $('#customerId').val();
        if(customerId > 0){
        	$.get('/yh/product/searchAllCategory', {customerId:customerId}, function(data){
    			console.log(data);	
    			var productTreeUl = $("#productTreeUl");
    			productTreeUl.empty();
    			if(data.length > 0){
    				for(var i = 0; i < data.length; i++)
    				{
    					var id = data[i].ID;
    					if(data[i].PARENT_ID == null){
	    					productTreeUl.append("<li class='parent_li'><span><i class='fa fa-folder'>" +
	    							"</i>  <a href='javascript:void(0)' class='productList' id='"+data[i].ID+"'>"+data[i].NAME+
	    							"</a></span>  <div class='edit_icons'><a class='fa fa-plus addCategory' href='javascript:void(0)' title='添加' id='"+data[i].ID+"'></a>  " +
	    							"<a class='fa fa-pencil editCategory' href='javascript:void(0)' title='编辑' id='"+data[i].ID+"'></a>  " +
	    							"<a class='fa fa-trash-o deleteCategory' href='javascript:void(0)' title='删除' id='"+data[i].ID+"'></a></div></li></ul></li>");
	    					for(var j = 0; j < data.length; j++)
	        				{
	    						if(data[j].PARENT_ID == id){
		    						productTreeUl.append("<ul><li class='parent_li'><span><i class='fa fa-folder'>" +
			    							"</i>  <a href='javascript:void(0)' class='productList' id='"+data[j].ID+"'>"+data[j].NAME+
			    							"</a></span>  <div class='edit_icons'><a class='fa fa-plus addCategory' href='javascript:void(0)' title='添加' id='"+data[j].ID+"'></a>  " +
			    							"<a class='fa fa-pencil editCategory' href='javascript:void(0)' title='编辑' id='"+data[j].ID+"'></a>  " +
			    							"<a class='fa fa-trash-o deleteCategory' href='javascript:void(0)' title='删除' id='"+data[j].ID+"'></a></div></li></ul></li></ul>");
	    						}
	    					}
    					}
    				}
    			}
    		},'json');
        }
    };
    
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
    	$("#hiddenCustomerId").val($("#customerId").val());
    	$("#editCategory").modal('show');
    });
    
    // 编辑类别
    $("#treeListDiv").on('click', 'a.editCategory', function(){
    	var categoryId = $(this).attr('id');
    	$("#hiddenCustomerId").val($("#customerId").val());
    	$("#hiddenCId").val(categoryId);
    	$("#hiddenParentId").val("");
    	$.post('/yh/product/searchCategory', {categoryId:categoryId}, function(data){ 
    		if(data.ID > 0){
    			$("#name").val(data.NAME);
    		}
		},'json');  
    	$("#editCategory").modal('show');
    });
    
    // 删除类别
    $("#treeListDiv").on('click', 'a.deleteCategory', function(){    	
    	var categoryId = $(this).attr('id');
    	$.post('/yh/product/deleteCategory', {categoryId:categoryId}, function(data){ 	
    		 if(data.success){
            	 searchAllCategory();
             }else{
                 alert('删除失败');
             }
		},'json');    	
    });
    
    var selectCategory = function(){
    	// 获取所有类别
    	var customerId = $("#customerId").val();
    	$.get('/yh/product/findAllCategory', {customerId:customerId}, function(data){
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
    };
    
    //
    $('#editProduct').on('click', function() {    	
    	selectCategory();
    });


    //---------------tree handle
    var setting = {
            view: {
                addHoverDom: addHoverDom,
                removeHoverDom: removeHoverDom,
                selectedMulti: false
            },
            edit: {
                enable: true,
                editNameSelectAll: true,
                showRemoveBtn: showRemoveBtn,
                showRenameBtn: showRenameBtn
            },
            async: {
                enable: true,
                url:"/yh/product/searchNodeCategory",
                autoParam:["categoryId", "customerId", "level=lv"],
                otherParam:{"otherParam":"zTreeAsyncTest"},
                dataFilter: filter
            },
            callback: {
                beforeClick: beforeClick,
                onClick: onNodeClick,
                beforeAsync: beforeAsync,
                onAsyncError: onAsyncError,
                onAsyncSuccess: onAsyncSuccess,

                beforeDrag: beforeDrag,
                beforeEditName: beforeEditName,
                beforeRemove: beforeRemove,
                beforeRename: beforeRename,
                onRemove: onRemove,
                onRename: onRename
            }
        };
        
        //-------------edit--------------
        var log, className = "dark";
        function beforeDrag(treeId, treeNodes) {
            return false;
        }

        function beforeEditName(treeId, treeNode) {
            className = (className === "dark" ? "":"dark");
            showLog("[ "+getTime()+" beforeEditName ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
            var zTree = $.fn.zTree.getZTreeObj("categoryTree");
            zTree.selectNode(treeNode);
            return confirm("进入节点 -- " + treeNode.name + " 的编辑状态吗？");
        }
        function beforeRemove(treeId, treeNode) {
            className = (className === "dark" ? "":"dark");
            showLog("[ "+getTime()+" beforeRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
            var zTree = $.fn.zTree.getZTreeObj("categoryTree");
            zTree.selectNode(treeNode);
            return confirm("确认删除 节点 -- " + treeNode.name + " 吗？");
        }
        function onRemove(e, treeId, treeNode) {
            showLog("[ "+getTime()+" onRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
        }
        function beforeRename(treeId, treeNode, newName, isCancel) {
            className = (className === "dark" ? "":"dark");
            showLog((isCancel ? "<span style='color:red'>":"") + "[ "+getTime()+" beforeRename ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name + (isCancel ? "</span>":""));
            if (newName.length == 0) {
                alert("节点名称不能为空.");
                var zTree = $.fn.zTree.getZTreeObj("categoryTree");
                setTimeout(function(){zTree.editName(treeNode)}, 10);
                return false;
            }
            return true;
        }
        function onRename(e, treeId, treeNode, isCancel) {
            showLog((isCancel ? "<span style='color:red'>":"") + "[ "+getTime()+" onRename ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name + (isCancel ? "</span>":""));
            $.post('/yh/product/saveCategory', {categoryId: treeNode.categoryId, customerId: treeNode.customerId, name:treeNode.name}, function(data){            
                
            },'json');
        }
        function showRemoveBtn(treeId, treeNode) {
            //根节点，不能删除
            if(treeNode.level==0)
                return false;
            //有子节点，不能删除
            var subNodes=[];
            $.ajax({  
                  type : "post",  
                  url : "/yh/product/searchNodeCategory",  
                  data : {categoryId: treeNode.categoryId, customerId: treeNode.customerId},  
                  async : false,  
                  success : function(data){  
                    subNodes=data; 
                  }  
             });

            if(subNodes.length>0)
                return false;
            return true;
            
        }

        function showRenameBtn(treeId, treeNode) {
            //根节点，不能重命名
            if(treeNode.level==0)
                return false;
            
            return true;
        }

        var newCount = 1;
        function addHoverDom(treeId, treeNode) {
            var sObj = $("#" + treeNode.tId + "_span");
            if (treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
            var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
                + "' title='添加类别' onfocus='this.blur();'></span>";
            sObj.after(addStr);
            var btn = $("#addBtn_"+treeNode.tId);
            if (btn) btn.bind("click", function(){
                //异步创建节点
                var zTree = $.fn.zTree.getZTreeObj("categoryTree");
                var nodeName = "新类别" + (newCount++);                

                $.post('/yh/product/saveCategory', {parentId: treeNode.categoryId, customerId: treeNode.customerId, name:nodeName}, function(data){            
                    zTree.addNodes(treeNode, {id:111111, categoryId: data.ID, customerId: treeNode.customerId, isParent:true, name:nodeName});
                },'json');

                
                return false;
            });
        };
        function removeHoverDom(treeId, treeNode) {
            $("#addBtn_"+treeNode.tId).unbind().remove();
        };
        function selectAll() {
            var zTree = $.fn.zTree.getZTreeObj("categoryTree");
            zTree.setting.edit.editNameSelectAll =  $("#selectAll").attr("checked");
        }
        //-------------edit end--------------
        function onNodeClick(event, treeId, treeNode){          
            $("#displayDiv").show();
            productDataTable.fnSettings().sAjaxSource = "/yh/product/list?categoryId="+treeNode.categoryId;
            productDataTable.fnDraw();
        }

        function filter(treeId, parentNode, childNodes) {
            if (!childNodes) return null;
            for (var i=0, l=childNodes.length; i<l; i++) {
                childNodes[i].name = childNodes[i].NAME.replace(/\.n/g, '.');
                childNodes[i].categoryId = childNodes[i].ID;
                childNodes[i].customerId = childNodes[i].CUSTOMER_ID;
                childNodes[i].parentId = childNodes[i].PARENT_ID;
                childNodes[i].isParent=true;
            }
            return childNodes;
        }
        function beforeClick(treeId, treeNode) {
            if (!treeNode.isParent) {
                alert("pls select parent node");
                return false;
            } else {
                return true;
            }
        }
        var log, className = "dark";
        function beforeAsync(treeId, treeNode) {
            className = (className === "dark" ? "":"dark");
            showLog("[ "+getTime()+" beforeAsync ]&nbsp;&nbsp;&nbsp;&nbsp;" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") );
            return true;
        }
        function onAsyncError(event, treeId, treeNode, XMLHttpRequest, textStatus, errorThrown) {
            showLog("[ "+getTime()+" onAsyncError ]&nbsp;&nbsp;&nbsp;&nbsp;" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") );
        }
        function onAsyncSuccess(event, treeId, treeNode, msg) {
            showLog("[ "+getTime()+" onAsyncSuccess ]&nbsp;&nbsp;&nbsp;&nbsp;" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") );
        }
        
        function showLog(str) {
            if (!log) log = $("#log");
            log.append("<li class='"+className+"'>"+str+"</li>");
            if(log.children("li").length > 8) {
                log.get(0).removeChild(log.children("li")[0]);
            }
        }
        function getTime() {
            var now= new Date(),
            h=now.getHours(),
            m=now.getMinutes(),
            s=now.getSeconds(),
            ms=now.getMilliseconds();
            return (h+":"+m+":"+s+ " " +ms);
        }

        function refreshNode(e) {
            var zTree = $.fn.zTree.getZTreeObj("categoryTree"),
            type = e.data.type,
            silent = e.data.silent,
            nodes = zTree.getSelectedNodes();
            if (nodes.length == 0) {
                alert("pls select parent node");
            }
            for (var i=0, l=nodes.length; i<l; i++) {
                zTree.reAsyncChildNodes(nodes[i], type, silent);
                if (!silent) zTree.selectNode(nodes[i]);
            }
        }

        var zNodes =[{}];

        $.fn.zTree.init($("#categoryTree"), setting, zNodes);
} );