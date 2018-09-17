$(document).ready(function() {
	 document.title = '报销单查询 | '+document.title;
	 $('#menu_finance').addClass('active').find('ul').addClass('in');
    
    //datatable, 动态处理
	 var userId = $('#userId').val();
    var costExpenseAccountTbody = $('#costExpenseAccountTbody').dataTable({
    	 "bProcessing": true, //table载入数据时，是否显示‘loading...’提示  
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/costReimbursement/reimbursementList",
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
		},
        "aoColumns": [ 
			{ "mDataProp":null,"sWidth": "50px",
				"fnRender": function(obj) {
					if(obj.aData.CREATE_ID == userId){
						return '<button type="button" class="btn btn-primary delete btn-xs" >'+
			            '<i class="fa fa-trash-o"></i> 删除</button>';
					}else{
						return '';
					}
			    }
			},
			{"mDataProp":"ORDER_NO","sWidth":"120px",
				"fnRender": function(obj) {
					return "<a href='/costReimbursement/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
				}
			},
            {"mDataProp":"STATUS", "sWidth":"200px","sClass":"status"},
            {"mDataProp":"ACCOUNT_NAME", "sWidth":"200px"},
            {"mDataProp":"OFFICE_NAME", "sWidth":"200px"},
            {"mDataProp":"ACCOUNT_NO", "sWidth":"200px"},
            {"mDataProp":"ACCOUNT_BANK", "sWidth":"200px"},
            {"mDataProp":"F_NAME", "sWidth":"200px"},
            {"mDataProp":"PAYMENT_TYPE", "sWidth":"200px"},
            {"mDataProp":"CREATENAME", "sWidth":"200px"},
            {"mDataProp":"CREATE_STAMP", "sWidth":"200px"},
            {"mDataProp":"AMOUNT", "sWidth":"150px"},
            {"mDataProp":"REMARK", "sWidth":"150px"}
        ]
    });	
    
    
    $('#costExpenseAccountTbody').on('click','.delete',function(){
    	var tr = $(this).parent().parent();
    	var id = tr.attr('id');
    	var status = tr.find('.status').text();

    	if(!confirm("是否确定删除？")){
    		return false;
    	}
    	
    	if(status!='新建'){
    		$.scojs_message('单据已存在财务单据，需要先撤销对应财务单据方可删除', $.scojs_message.TYPE_FAIL);
    		return false;
    	}
    	
    	if(id>0){
    		$.post('/costReimbursement/delete', {id:id}, function(data){
    			if(data){
    				$.scojs_message("删除成功", $.scojs_message.TYPE_OK);
    				refreshData();
    			}else{
    				$.scojs_message("删除失败", $.scojs_message.TYPE_FAIL);
    			}
    		}).fail(function() {
    	        $.scojs_message('删除失败', $.scojs_message.TYPE_FAIL);
    	   });
    	}
    	
    });
    

    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
    });

    $("#searchBtn").on('click', function () {
    	refreshData();
    });

    var refreshData= function(){
        var orderNo = $.trim($("#orderNo").val());
        var status = $.trim($("#status").val());
        var auditName = $.trim($("#auditName").val());
        var accountName = $.trim($("#account_name").val());
        var office_name = $.trim($("#office_name").val());
        var begin_time = $.trim($("#create_time_begin_time").val());
        var end_time = $.trim($("#create_time_end_time").val());
        var creator_name = $.trim($("#creator_name").val());
        
        var flag = false;
        $('#searchForm input,#searchForm select').each(function(){
        	 var textValue = $.trim(this.value);
        	 if(textValue != '' && textValue != null){
        		 flag = true;
        		 return;
        	 } 
        });
        if(!flag){
        	 $.scojs_message('请输入至少一个查询条件', $.scojs_message.TYPE_FALSE);
        	 return false;
        }
        
        costExpenseAccountTbody.fnSettings().oFeatures.bServerSide = true;
        costExpenseAccountTbody.fnSettings().sAjaxSource = "/costReimbursement/reimbursementList?creator_name="+creator_name+"&orderNo="+orderNo+"&status="+status+"&accountName="+accountName
        +"&office_name="+office_name
        +"&begin_time="+begin_time
        +"&end_time="+end_time;
        costExpenseAccountTbody.fnDraw();
    }
    //获取未审核供应商的list，选中信息在下方展示其他信息
    $('#account_name').on('click input', function(){
        var me = this;
        var inputStr = $('#account_name').val();
        var spList2 =$("#account_list");
        $.get('/transferOrder/searchSp', {input:inputStr}, function(data){
            if(inputStr!=$('#account_name').val()){//查询条件与当前输入值不相等，返回
                return;
            }
            spList2.empty();
            for(var i = 0; i < data.length; i++){
                var abbr = data[i].ABBR;
                var company_name = data[i].COMPANY_NAME;
                var contact_person = data[i].CONTACT_PERSON;
                var phone = data[i].PHONE;
                
                if(abbr == null) 
                    abbr = '';
                if(company_name == null)
                    company_name = '';
                if(contact_person == null)
                    contact_person = '';
                if(phone == null)
                    phone = '';
                
                spList2.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
            }
            spList2.css({ 
                left:$(me).position().left+"px", 
                top:$(me).position().top+28+"px" 
            }); 
            spList2.show();
        },'json');
        
        
    });
    
    // 没选中供应商，焦点离开，隐藏列表
    $('#account_name').on('blur', function(){
        $('#account_list').hide();
    });

    //当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
    $('#account_list').on('blur', function(){
        $('#account_list').hide();
    });

    $('#account_list').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });

    // 选中供应商
    $('#account_list').on('mousedown', '.fromLocationItem', function(e){
        console.log($('#account_list').is(":focus"));
        var message = $(this).text();
        $('#account_name').val($(this).attr("company_name"));
        var pageSpName = $("#pageSpName");
        pageSpName.empty();
        var pageSpAddress = $("#pageSpAddress");
        pageSpAddress.empty();
        pageSpAddress.append($(this).attr('address'));
        var contact_person = $(this).attr('contact_person');
        if(contact_person == 'null')
            contact_person = '';
        pageSpName.append(contact_person+'&nbsp;');
        var phone = $(this).attr('phone');
        if(phone == 'null')
            phone = '';
        pageSpName.append(phone); 
        pageSpAddress.empty();
        var address = $(this).attr('address');
        if(address == 'null')
            address = '';
        pageSpAddress.append(address);
        $('#account_list').hide();
    });
    
});