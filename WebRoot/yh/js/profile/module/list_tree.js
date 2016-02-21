//$(document).ready(function() {
	
    var module_obj;

    $('#menu_sys_profile').addClass('active').find('ul').addClass('in');

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
            //showRemoveBtn: showRemoveBtn,
            //showRenameBtn: showRenameBtn,
            renameTitle: "编辑",
            removeTitle: "删除",
            drag:{
                isCopy: false,
                isMove: true
            }
        },
        async: {
            enable: true,
            type: 'get',
            url:"/module/searchModule",
            autoParam:["id", "level=lv"],
            dataFilter: dataFilter//处理返回来的JSON 变为 nodes
        },
        callback: {
            beforeRename: beforeRename,
            onRename: onRename,
            beforeDrop: beforeDrop,//判断禁止模块拖拽到模块下
            onDrop: onDrop,
            onClick: onNodeClick
        }
    };

    function dataFilter(treeId, parentNode, childNodes) {
        if (!childNodes) return null;
        for (var i=0, l=childNodes.length; i<l; i++) {
            childNodes[i].name = childNodes[i].MODULE_NAME.replace(/\.n/g, '.');
            childNodes[i].id = childNodes[i].ID;
            childNodes[i].parent_id = childNodes[i].PARENT_ID;
            if(childNodes[i].PARENT_ID>0){
                childNodes[i].isParent=false;
            }
        }
        return childNodes;
    };

    function beforeRename(treeId, treeNode, newName, isCancel) {
        if (newName.length == 0) {
            alert("节点名称不能为空.");
            var zTree = $.fn.zTree.getZTreeObj("moduleTree");
            setTimeout(function(){zTree.editName(treeNode)}, 10);
            return false;
        }
        return true;
    };

    function onRename(e, treeId, treeNode, isCancel) {
        $.post('/module/updateModule', 
            {id: treeNode.id, parent_id: treeNode.parent_id, module_name:treeNode.name}, 
            function(data){

        },'json');
    };

    function beforeDrop(treeId, treeNodes, targetNode, moveType) {
        if(treeNodes[0].level ==0 && targetNode.isParent && moveType =='inner'){
            alert("模块不能拖到模块下级");
            return false;
        }
        if(treeNodes[0].level ==1 && targetNode.level == 0 && moveType !='inner'){
            alert("单据不能拖到模块同级");
            return false;
        }
        if(treeNodes[0].level ==1 && targetNode.level == 1 && moveType =='inner'){
            alert("单据不能拖到单据下级");
            return false;
        }
        return true;
    };

    function onDrop(event, treeId, treeNodes, targetNode, moveType) {
       // console.log("moveType:"+moveType + "," + treeNodes.length + "," + (targetNode ? (targetNode.tId + ", " + targetNode.name) : "isRoot" ));
        console.log("moveType:"+moveType + ", " +treeNodes[0].name+":"+ treeNodes[0].id + ", "+targetNode.name+":"+targetNode.id);
        if(targetNode){
            $.post('/module/updateModuleSeq', 
                {node_id: treeNodes[0].id, target_node_id: targetNode.id, move_type:moveType}, 
                function(data){},
                'json');
        }
    };

    var newCount = 1;
    function addHoverDom(treeId, treeNode) {
        var sObj = $("#" + treeNode.tId + "_span");
        //如果是单据则不能在其下级添加节点
        if (treeNode.level==1 ||treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
        var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
            + "' title='添加' onfocus='this.blur();'></span>";
        sObj.after(addStr);
        var btn = $("#addBtn_"+treeNode.tId);
        if (btn) btn.bind("click", function(){
            //异步创建节点
            var zTree = $.fn.zTree.getZTreeObj("moduleTree");
            var nodeName = "新单据" + (newCount++);
           
            // 7-5 使用异步会导致树节点添加两次， 因为自己手动加了一个，ztree自己异步自动又加了一个
            $.post('/module/addModule', {parent_id: treeNode.id, id: null, name:nodeName}, function(data){
                if ((!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) || treeNode.zAsync){ 
                    zTree.addNodes(treeNode, {id:data.ID, id: data.ID, parent_id: treeNode.PARENT_ID, isParent:true, name:nodeName});
                    zTree.reAsyncChildNodes(treeNode, "refresh");
                } else{
                    zTree.reAsyncChildNodes(treeNode, "refresh");
                }
            },'json');

            return false;
        });
    };
    function removeHoverDom(treeId, treeNode) {
        $("#addBtn_"+treeNode.tId).unbind().remove();
    };

    function onNodeClick(event, treeId, treeNode){
        if (treeNode.level==0 ||treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
        if(treeNode.parentTId != null){
            $("#addProductDiv").show();
            $("#displayDiv").show();
        }else{
            $("#addProductDiv").hide();
            $("#displayDiv").hide();
        }
        $("#module_id").text(treeNode.id);
        $("#order_name").val(treeNode.name);

        $.post('/module/getOrderStructure', {module_id: treeNode.id}, function(json){
            console.log('getOrderStructure....');
            console.log(json);
            module_obj = json;
            $('#fields_body').empty();

            for (var i = 0; i < json.STRUCTURE_LIST.length; i++) {
                var structure = json.STRUCTURE_LIST[i];
                var html = template('table_template', 
                    {
                        id: 'sub'+subIndex,
                        s_id: structure.ID,
                        s_name: structure.NAME,
                        s_type: structure.STRUCTURE_TYPE,
                        s_parent_id: structure.PARENT_ID,
                        s_add_btn_type: structure.ADD_BTN_TYPE,
                        s_add_btn_setting: structure.ADD_BTN_SETTING
                    }
                );

                $('#fields_body').append(html);
                var dataTable = $('body').find('table#'+'sub'+subIndex+'_table').DataTable(tableSetting);
                subIndex++;

                $('[data-toggle=tooltip]').tooltip();
                // Module.dataTable.clear().draw();

                if(!structure.FIELDS_LIST)
                    return;
                

                for (var j = 0; j < structure.FIELDS_LIST.length; j++) {
                    var field = structure.FIELDS_LIST[j];
                    var item={
                        "ID": field.ID,
                        "FIELD_NAME": field.FIELD_NAME,
                        "FIELD_DISPLAY_NAME": field.FIELD_DISPLAY_NAME,
                        "FIELD_TYPE": field.FIELD_TYPE,
                        "FIELD_TYPE_EXT_TYPE": field.FIELD_TYPE_EXT_TYPE,
                        "FIELD_TYPE_EXT_TEXT": field.FIELD_TYPE_EXT_TEXT,
                        "REQUIRED": field.REQUIRED,
                        "LISTED": field.LISTED,
                        "FIELD_TEMPLATE_PATH": field.FIELD_TEMPLATE_PATH,
                        "INDEX_NAME": ''
                    };
                    dataTable.row.add(item).draw(false);
                }
            }

            if(!json.ACTION_LIST)
                    return;

            action_table.clear().draw(false);
            for (var k = 0; k < json.ACTION_LIST.length; k++) {
                var action = json.ACTION_LIST[k];
                var item={
                    "ID": action.ID,
                    "ACTION_NAME": action.ACTION_NAME,
                    "ACTION_TYPE": action.ACTION_TYPE,
                    "BTN_VISIBLE_CONDITION": action.BTN_VISIBLE_CONDITION,
                    "ACTION_SCRIPT": action.ACTION_SCRIPT
                };
                action_table.row.add(item).draw(false);
            }

            bindFieldTableEvent();//click event
        }, 'json');
    };
    //---------------------------  tree handler end -------------

    // 显示当前用户公司的所有模块
    $.get('/module/searchModule', function(data){
        console.log(data);
        var zNodes =[];
        for(var i=0; i<data.length && data.length>0; i++){
	        var node={};
	        node.name=data[i].MODULE_NAME;
	        node.id=data[i].ID;
            node.isParent=true;
	        //node.click=nodePlusClickHandler;
	        zNodes.push(node);
	        //console.log(node);
        }
        $.fn.zTree.init($("#moduleTree"), setting, zNodes);
    },'json');
    
    $('#addModuleBtn').click(function function_name (argument) {
        var zTree = $.fn.zTree.getZTreeObj("moduleTree");
        $.post('/module/addModule', {parent_id: null, id: null, name:"新模块"}, function(data){
            zTree.addNodes(null, {parent_id: null, id: data.ID, name:"新模块", isParent:true});
        },'json');
    });


//});