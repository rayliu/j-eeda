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
	        console.log(node);
        }
        $.fn.zTree.init($("#categoryTree"), setting, zNodes);
    },'json');
    
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
        "bFilter": false, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "sAjaxSource": "/yh/product/list",
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [   
            {
            	"mDataProp":"ITEM_NO",            	
            	"sWidth": "80px",
            	"sClass": "item_no"
            },       	
            {
            	"mDataProp":"SERIAL_NO",
            	"sWidth": "120px",
            	"sClass": "serial_no"
            },        	
            {
            	"mDataProp":"ITEM_NAME",
            	"sWidth": "120px",
            	"sClass": "item_name"
            },
            {
            	"mDataProp":"SIZE",            	
            	"sWidth": "50px",
            	"sClass": "size"
        	},
            {
            	"mDataProp":"WIDTH",
            	"sWidth": "50px",
            	"sClass": "width"
            },
            {
            	"mDataProp":"HEIGHT",            	
            	"sWidth": "50px",
            	"sClass": "height"
        	},
            {
            	"mDataProp":"CATEGORY_NAME",            	
            	"sWidth": "50px",
            	"sClass": "category"
        	},
        	{
            	"mDataProp":"UNIT",
            	"sWidth": "50px",
            	"sClass": "unit"
            },
            {
            	"mDataProp":"VOLUME",
            	"sWidth": "50px",
            	"sClass": "volume"
            }, 
            {
            	"mDataProp":"WEIGHT",
            	"sWidth": "50px",
            	"sClass": "weight",
            },
            {"mDataProp":"ITEM_DESC"},
            { 
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "item_desc",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger btn-xs deleteProduct' id="+obj.aData.ID+" title='删除'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+
                            "</a>";
                }
            }                         
        ],      
    });

    // 计算体积
	var sumVolume = function(currentEle){
		$(currentEle).parent().children('.volume')[0].innerHTML = parseFloat($(currentEle).parent().children('.size')[0].innerHTML)/1000 * parseFloat($(currentEle).parent().children('.width')[0].innerHTML)/1000 * parseFloat($(currentEle).parent().children('.height')[0].innerHTML)/1000;
	};

	// 刷新产品列表
	var refreshProductTable = function(){
		var categoryId = $("#categoryId").val();
		// 刷新产品列表
		productDataTable.fnSettings().sAjaxSource = "/yh/product/list?categoryId="+categoryId;
		productDataTable.fnDraw();
	};
	
    productDataTable.makeEditable({
    	sUpdateURL: '/yh/product/saveProductByField',    	
    	oEditableSettings: {event: 'click'},
    	"aoColumns": [  			            
            {            
            	style: "inherit",
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"item_no",
            	placeholder: "", 
            	callback: function () {
            		refreshProductTable();
            		if(this.textContent == 'repetition'){
            			alert("该产品型号已存在,请更换!");
            		}
            	}
        	},
        	{            
            	style: "inherit",
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"serial_no",
            	placeholder: "", 
            	callback: function () {
            		refreshProductTable();
            	}
        	},
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"item_name",
            	placeholder: "",
            	callback: function () {
            		refreshDetailTable();
            	} 
            },
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"size",
            	placeholder: "",
            	callback: function () {
            		sumVolume(this);
            		refreshProductTable();
            	} 
            },
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"width",
            	placeholder: "",
            	callback: function () {
            		sumVolume(this);
            		refreshProductTable();
            	} 
            },
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"height",
            	placeholder: "",
            	callback: function () {
            		sumVolume(this);
            		refreshProductTable();
            	} 
            },
            null,
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"unit",
            	type: 'select',
            	data: "{'':'', '台':'台','件':'件','套':'套'}",
            	placeholder: "",
            	callback: function () {} 
            },
            null,  
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"weight",
            	placeholder: "",
            	callback: function () {} 
            },
            {
            	indicator: '正在保存...',
            	onblur: 'submit',
            	tooltip: '点击可以编辑',
            	name:"item_desc",
            	type: 'textarea',
            	placeholder: "",
            	callback: function () {} 
            },
            null                        
        ]      
    }).click(function(){
    	var inputBox = $(this).find('input');
        inputBox.autocomplete({
	        source: function( request, response ) {
	        },
        	select: function( event, ui ) {
        		
        		//将选择的产品id先保存到数据库
        		var itemId = $(this).parent().parent().parent()[0].id;
        		var productId = ui.item.id;
        		$.post('/yh/transferOrderItem/saveTransferOrderItem', 
        			{transferOrderItemId:itemId,productId:productId},
        			function(){ itemDataTable.fnDraw();  });        		
                
            },
        	minLength: 2
        });
    });                                                                      
        
    var nodePlusClickHandler= function(e){
    	e.preventDefault();
    	var customerId = $('#customerId').val();
    	var categoryId = $(this).find('li').attr('categoryId');
    	var nodeElement = $(this);
    	nodeElement.unbind('click');
    	$.get('/yh/product/searchNodeCategory', {categoryId: categoryId, customerId: customerId}, function(data){
    		console.log(data);
    		if(data.length > 0){		
	    		addNode(data, nodeElement);
    		}else{
    			console.log(nodeElement);
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
            	productDataTable.fnSettings().sAjaxSource = "/yh/product/list?categoryId="+$("#categoryId").val();
                productDataTable.fnDraw();
            }else{
                alert('数据保存失败。');
            }
    		$('#myModal').modal('hide');	
    		$("#productForm")[0].reset();
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
			$("#categoryId").val(data.CATEGORY_ID);	    	
  	    	$("#item_no").val(data.ITEM_NO);
  	    	$("#item_name").val(data.ITEM_NAME);
  	    	$("#size").val(data.SIZE);	    	
  	    	$("#width").val(data.WIDTH);	    	
  	    	$("#height").val(data.HEIGHT);	    	
  	    	$("#unit").val(data.UNIT);	    	
  	    	$("#volume").val(data.VOLUME);	    	
  	    	$("#weight").val(data.WEIGHT);	    	
  	    	$("#categorySelect").val(data.CNAME);	    	
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
    	refreshProductTable();
    });
    
    var selectCategory = function(){
    	// 获取所有类别
    	/*var treeObj = $.fn.zTree.getZTreeObj("categoryTree");
    	var sNodes = treeObj.getSelectedNodes();
    	if (sNodes.length > 0) {
    		var node = sNodes[0].getParentNode();
    		var categoryId = node.ID;
    		var customerId = sNodes[0].customerId;
    	}*/
    	$.get('/yh/product/findCategory', {categoryId:$("#categoryId").val()}, function(data){
	   		if(data.ID > 0){
		   		$("#categorySelect").val(data.NAME);
	   		}
   	    },'json');
    };
    
    // 新增产品
    $('#editProduct').on('click', function() { 
    	var categoryId = $("#categoryId").val();
 		$.post('/yh/product/addNewRow', {categoryId:categoryId}, function(data){
 			productDataTable.fnDraw(); 
 		});

    });
    
    // 计算产品体积
    $('#height, #width, #size').on('keyup click', function() { 
    	var height = $('#height').val();
    	var width = $('#width').val();
    	var size = $('#size').val();
    	$("#volume").val((height/1000)*(width/1000)*(size/1000));
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
                beforeExpand: beforeExpand,
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
            return true;
            //confirm("进入节点 -- " + treeNode.name + " 的编辑状态吗？");
        }
        function beforeRemove(treeId, treeNode) {
            className = (className === "dark" ? "":"dark");
            showLog("[ "+getTime()+" beforeRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
            var zTree = $.fn.zTree.getZTreeObj("categoryTree");
            zTree.selectNode(treeNode);
            return confirm("确认删除 类别: " + treeNode.name + " 吗？其类别下所有产品也将被删除。");
        }
        function onRemove(e, treeId, treeNode) {
            showLog("[ "+getTime()+" onRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
            var pNode = treeNode.getParentNode();
            pNode.isParent = true;
            var zTree = $.fn.zTree.getZTreeObj("categoryTree");
            $.post('/yh/product/deleteCategory', {categoryId: treeNode.categoryId}, function(data){                
                zTree.reAsyncChildNodes(pNode, "refresh");
                
                zTree.selectNode(pNode,false);
                //设置选中节点后右边编辑内容的载入
                onNodeClick(e, pNode.categoryId, pNode);
            },'json');

            
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
               
                // 7-5 使用异步会导致树节点添加两次， 因为自己手动加了一个，ztree自己异步自动又加了一个
                $.post('/yh/product/addCategory', {categoryId: treeNode.categoryId, customerId: treeNode.customerId, name:nodeName}, function(data){
                    if ((!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) || treeNode.zAsync) 
                        zTree.addNodes(treeNode, {id:data.ID, categoryId: data.ID, customerId: treeNode.customerId, isParent:true, name:nodeName});
                    else
                        zTree.reAsyncChildNodes(treeNode, "refresh");
                    //
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
        	if(treeNode.parentTId != null){
	        	$("#addProductDiv").show();
	            $("#displayDiv").show();
        	}else{
        		$("#addProductDiv").hide();
	            $("#displayDiv").hide();
        	}
            $("#categoryId").val(treeNode.categoryId);
            $("#hiddenCategoryId").val(treeNode.categoryId);
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
            showLog("[ "+getTime()+" beforeAsync ]" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") +", treeNode:"+treeNode);
            return true;
        }
        function onAsyncError(event, treeId, treeNode, XMLHttpRequest, textStatus, errorThrown) {
            showLog("[ "+getTime()+" onAsyncError ]" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") +", treeNode:"+treeNode);
        }
        function onAsyncSuccess(event, treeId, treeNode, msg) {
            showLog("[ "+getTime()+" onAsyncSuccess ]" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") +", treeNode:"+treeNode);
            
        }
        function beforeExpand(treeId, treeNode){
        	showLog("[ "+getTime()+" beforeExpand ]" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") +", treeNode:"+treeNode);
        	
        }
        function showLog(str) {
            console.log(str);
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
            showLog('refreshNode');
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