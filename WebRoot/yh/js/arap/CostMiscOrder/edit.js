$(document).ready(function() {
	if(order_no){
		document.title = order_no +' | '+document.title;
	}
	$('#menu_finance').addClass('active').find('ul').addClass('in');
	
	
	
	
	var saveCostMiscOrder = function(e, callback){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costMiscOrderForm").valid()){
	       	return;
        }
        
        
        var tableRows = $("#feeItemList-table tr");
        var itemsArray=[];
        for(var index=0; index<tableRows.length; index++){
        	if(index==0)
        		continue;

        	var row = tableRows[index];
        	var id = $(row).attr('id');
        	if(!id){
        		id='';
        	}
        	
        	var item={
        		CUSTOMER_ORDER_NO: $(row.children[0]).find('input').val(), 
			 	ITEM_DESC: $(row.children[1]).find('input').val(),
			 	NAME: $(row.children[2]).find('select').val(),
			 	AMOUNT: $(row.children[3]).find('input').val(),
			 	STATUS: '新建',
			 	ID: id,
			 	ACTION: 'create'
        	};
        	itemsArray.push(item);
        }

		var amount = 0.00;
        for(var i=0; i<itemsArray.length; i++){
        	amount+=Number(itemsArray[i].AMOUNT);
        	$('#totalAmountSpan').html(amount);
        }
        
        
      //add deleted items
        for(var index=0; index<deletedIds.length; index++){
        	var id = deletedIds[index];
        	var item={
        		ID: id,
			 	ACTION: 'delete'
        	};
        	itemsArray.push(item);
        }

        var order={
        	costMiscOrderId: $('#costMiscOrderId').val(),
        	biz_type: $('input[name="biz_type"]:checked').val(),
        	cost_to_type: $('input[name="cost_to_type"]:checked').val(),
        	customer_id: $('#customer_id').val(),
        	sp_id: $('#sp_id').val(),
        	route_from: $('#locationForm').val(),
        	route_to: $('#locationTo').val(),
        	others_name: $('#others_name').val(),
        	ref_no: $('#ref_no').val(),
        	remark: $('#remark').val(),
        	amount: amount,
        	items: itemsArray
        };
        //console.log(order);
        
        
		//异步向后台提交数据
		$.post('/costMiscOrder/save',{params:JSON.stringify(order)}, function(data){
			var order = data.order;
			if(order.ID>0){
				$("#ref_order_no").text(order.REF_ORDER_NO);
				$("#arapMiscCostOrderNo").html(order.ORDER_NO);
				$("#create_time").html(order.CREATE_STAMP);
				$("#costMiscOrderId").val(order.ID);
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#saveCostMiscOrderBtn").attr("disabled",false);
				contactUrl("edit?id", order.ID);

				feeTable.fnClearTable();

				for (var i = 0; i < data.itemList.length; i++) {
					var item = data.itemList[i];
					feeTable.fnAddData({
						ID: item.ID,
					 	CUSTOMER_ORDER_NO: item.CUSTOMER_ORDER_NO,
					 	ITEM_DESC: item.ITEM_DESC,
					 	NAME: item.NAME,
					 	AMOUNT: item.AMOUNT,
					 	CHANGE_AMOUNT: item.CHANGE_AMOUNT,
					 	STATUS: '新建'
					 });
				};
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
				$("#saveCostMiscOrderBtn").attr("disabled",false);
			}
		},'json');
	};
   
	/*--------------------------------------------------------------------*/
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	
	
	//获取省份的城市
    $('#mbProvince').on('change', function(){
    	alert("dd");
    	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCity");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				toLocationList.show();
			},'json');
		});
    //获取城市的区县
    $('#cmbCity').on('change', function(){
    	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			var code = $("#notify_location").val(inputStr);
			$.get('/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				toLocationList.show();
			},'json');
		});
    
    $('#cmbArea').on('change', function(){
     	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			var code = $("#notify_location").val(inputStr);
		});         

     // 回显城市
     var hideProvince = $("#hideProvince").val();
     $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
			if(data.length > 0){
				var cmbCity =$("#cmbCity");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				var hideCity = $("#hideCity").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideCity){
						cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');

     // 回显区
     var hideCity = $("#hideCity").val();
     $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
			if(data.length > 0){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				var hideDistrict = $("#hideDistrict").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideDistrict){
						cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
    
    //获取全国省份
    $(function(){
     	var province = $("#mbProvinceFrom");
     	$.post('/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
				var hideProvince = $("#hideProvinceFrom").val();
     		for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideProvince){
     				province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");     				
					}else{
     				province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     		
     	},'json');
    });
    
    //获取省份的城市
    $('#mbProvinceFrom').on('change', function(){
			var inputStr = $(this).val();
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCityFrom");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				
			},'json');
		});
    //获取城市的区县
    $('#cmbCityFrom').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationForm").val(inputStr);
			$.get('/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbAreaFrom");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				
			},'json');
		});
    
    $('#cmbAreaFrom').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationForm").val(inputStr);
		});         
    

    //获取全国省份
    $(function(){
     	var province = $("#mbProvinceTo");
     	$.post('/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
				var hideProvince = $("#hideProvinceTo").val();
     		for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideProvince){
     				province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
     				
     				
					}else{
     				province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     		
     	},'json');
    });
    
    //获取省份的城市
    $('#mbProvinceTo').on('change', function(){
			var inputStr = $(this).val();
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCityTo");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				
			},'json');
		});
    
    //获取城市的区县
    $('#cmbCityTo').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
			$.get('/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbAreaTo");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				
			},'json');
		});
    
    $('#cmbAreaTo').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
		});  
    

    // 回显城市
    var hideProvince = $("#hideProvinceFrom").val();
    $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
			if(data.length > 0){
				var cmbCity =$("#cmbCityFrom");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				var hideCity = $("#hideCityFrom").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideCity){
						cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');

    // 回显区
    var hideCity = $("#hideCityFrom").val();
    $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
			if(data.length > 0){
				var cmbArea =$("#cmbAreaFrom");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				var hideDistrict = $("#hideDistrictFrom").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideDistrict){
						cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
    

    // 回显城市
    var hideProvince = $("#hideProvinceTo").val();
    $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
			if(data.length > 0){
				var cmbCity =$("#cmbCityTo");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				var hideCity = $("#hideCityTo").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideCity){
						cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');

    // 回显区
    var hideCity = $("#hideCityTo").val();
    $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
			if(data.length > 0){
				var cmbArea =$("#cmbAreaTo");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				var hideDistrict = $("#hideDistrictTo").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideDistrict){
						cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
	
	
	
	//设置一个变量值，用来保存当前的ID
	var parentId = "costMiscOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#costMiscOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveCostMiscOrderBtn").click(function(e){
 		$("#saveCostMiscOrderBtn").attr("disabled", true);
 		saveCostMiscOrder(e);
	});
	
    if($("#costMiscOrderStatus").text() == 'new'){
    	$("#costMiscOrderStatus").text('新建');
	}
    
    var feeTable = $('#feeItemList-table').dataTable({    	
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"bPaginate": false, //翻页功能
        "bInfo": false,//页脚信息
        "bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	//"sAjaxSource": "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+$("#costMiscOrderId").val(),
        "fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},		
        "aoColumns": [ 
          	{"mDataProp":"CUSTOMER_ORDER_NO", "sWidth": "100px",
          	 "fnRender": function(obj) {
		        if(obj.aData.CUSTOMER_ORDER_NO!='' && obj.aData.CUSTOMER_ORDER_NO != null){
		            return "<input type='text' name='customer_order_no' value='"+obj.aData.CUSTOMER_ORDER_NO+"' class='form-control search-control'>";
		        }else{
		        	 return "<input type='text' name='customer_order_no' class='form-control search-control'>";
		        }
		     }
            },
            {"mDataProp":"ITEM_DESC","sWidth": "130px",
        	    "fnRender": function(obj) {
			        if(obj.aData.ITEM_DESC!='' && obj.aData.ITEM_DESC != null){
			            return "<input type='text' name='item_desc' value='"+obj.aData.ITEM_DESC+"'  class='form-control search-control'>";
			        }else{
			        	 return "<input type='text'  name='item_desc' class='form-control search-control'>";
			        }
			    }
        	},
			{"mDataProp":"NAME","sWidth": "70px",
			    "fnRender": function(obj) {
			        if(obj.aData.NAME!='' && obj.aData.NAME != null){
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		if(obj.aData.NAME == $(this).text()){
			        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";   
			        			$("#receivableTotal").val(obj.aData.NAME);
			        		}else{
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			            return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
			        }else{
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
			        }
			 }},
			 {"mDataProp":"AMOUNT",
			    "fnRender": function(obj) {
			        if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
			            return "<input type='text' name='amount' value="+obj.aData.AMOUNT+" class='form-control search-control'>";
			        }else{
			        	return "<input type='text' name='amount' class='form-control search-control'>";
			        }
			}},
			{"mDataProp":"CHANGE_AMOUNT"},
			{"mDataProp":"STATUS"},
			{"mDataProp": null,
                "fnRender": function(obj) {
               		 return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'><i class='fa fa-trash-o fa-fw'> </i>删除</a>";
                }
            }   
        ]      
    });
   
    

	//不知道为什么，直接定义dataTable里定义sAjaxSource不起作用，需要在这里重新load
	/*var costMiscOrderId =$("#costMiscOrderId").val();
    if(costMiscOrderId!=""){
		feeTable.fnSettings().sAjaxSource = "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+costMiscOrderId;
		feeTable.fnDraw(); 
	} */
    	
//	//应收
//	$("#addFee").click(function(){	
//		 var insertNewFee = function(costMiscOrderId){
//		 	$.post('/costMiscOrder/addNewFee?costMiscOrderId='+costMiscOrderId,function(data){
//				console.log(data);
//				if(data.ID > 0){
//					feeTable.fnSettings().sAjaxSource = "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+costMiscOrderId;
//					feeTable.fnDraw();  
//				}
//			});
//		 };
//		 var costMiscOrderId =$("#costMiscOrderId").val();
//		 if(costMiscOrderId=="" || costMiscOrderId==null){
//		 	saveCostMiscOrder(event, insertNewFee); //save 主表
//		 }else{
//		 	insertNewFee(costMiscOrderId);
//		 }
//
//	});	
    
  //删除一行
    var deletedIds=[];
	$("#feeItemList-table").on('click', '.finItemdel', function(e){
		e.preventDefault();
		var tr = $(this).parent().parent();
		deletedIds.push(tr.attr('id'));
		tr.remove();
	});	
	
	 
	
	//添加一行
	$("#addFee").click(function(){
		 feeTable.fnAddData({
		 	CUSTOMER_ORDER_NO:'',
		 	ITEM_DESC:'',
		 	NAME:'',
		 	AMOUNT: '0',
		 	CHANGE_AMOUNT: '',
		 	STATUS: '新建'
		 });
		 feeTable.fnDraw(); 
	});	
	
	
	//保存修改费用明细的方法
	/*var savaUpdataMethod = function(evt){
		var inputThis = $(evt);
		var costMiscOrderId = $("#costMiscOrderId").val();
		var paymentId = inputThis.parent().parent().attr("id");
		if(paymentId == "" || paymentId == null)
			paymentId = inputThis.parent().parent().parent().attr("id");
		var name = inputThis.attr("name");
		var value = inputThis.val();
		var costCheckOrderIds = $("#costCheckOrderIds").val();
		if(paymentId != "" && value != "" && costMiscOrderId != "")
		$.post('/costMiscOrder/updateCostMiscOrderItem', {paymentId:paymentId, name:name, value:value, costMiscOrderId: costMiscOrderId, costCheckOrderIds: costCheckOrderIds}, function(data){
			if(data.ID > 0){
				//$("#totalAmountSpan")[0].innerHTML = data.TOTAL_AMOUNT;
			}else{
				alert("修改失败!");
			}
    	},'json');
	};*/
	
	//费用明细列表修改（成本单据类型），当单据改变值时，单据号值为空
	/*$("#feeItemList-table").on('change', 'select[name="order_type"]', function(e){
		savaUpdataMethod(this);
		$(this).parent().parent().find("td").find("input[name='order_no']").val("");
	});*/
	
	//费用明细列表修改(单据号，费用类型，金额，备注，日期)
	/*$("#feeItemList-table").on('blur', 'input, select', function(e){
		savaUpdataMethod(this);
	});*/
	
		
	/*$("#costMiscOrderItem").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costMiscOrderForm").valid()){
	       	return;
        }
        if(parentId == "costMiscOrderbasic"){
        	saveCostMiscOrder(e);
        }
		
		var costMiscOrderId =$("#costMiscOrderId").val();
		if(costMiscOrderId != "" && costMiscOrderId != null){
			feeTable.fnSettings().oFeatures.bServerSide = true;
			feeTable.fnSettings().sAjaxSource = "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+costMiscOrderId;
			feeTable.fnDraw();
		}
		
		parentId = e.target.getAttribute("id");
	});*/
	
	//异步删除应付
	/*$("#feeItemList-table").on('click', '.finItemdel', function(e){
		var id = $(this).attr('code');
		e.preventDefault();
		$.post('/costMiscOrder/finItemdel/'+id,function(data){
             //保存成功后，刷新列表
             console.log(data);
             feeTable.fnSettings().sAjaxSource = "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+$("#costMiscOrderId").val();
     		 feeTable.fnDraw();  
        },'text');
	});	*/
	    
    //获取客户列表，自动填充
    $('#customer_filter').on('keyup click', function(){
        var inputStr = $('#customer_filter').val();
        var companyList =$("#companyList");
        $.get("/transferOrder/searchPartCustomer", {input:inputStr}, function(data){
            companyList.empty();
            for(var i = 0; i < data.length; i++){
                var abbr = data[i].ABBR;
				var company_name = data[i].COMPANY_NAME;
				if(abbr == null) 
					abbr = '';
				if(company_name == null)
					company_name = '';
				companyList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' company_name='"+company_name+"'>"+abbr+" "+company_name+"</a></li>");
            }
        },'json');
        companyList.css({left:$(this).position().left+"px",top:$(this).position().top+32+"px"}).show();
    });
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).attr("company_name"));
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customer_id').val(companyId);
        savePartyInfo(companyId,"CUSTOMER");
    });
    // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    $('#companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });

    //供应商查询
    //获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter').on('keyup click', function(){
		var inputStr = $('#sp_filter').val();
		var spList =$("#spList");
		$.get('/serviceProvider/searchSp', {input:inputStr}, function(data){
			spList.empty();
			for(var i = 0; i < data.length; i++){
				var abbr = data[i].ABBR;
				var company_name = data[i].COMPANY_NAME;
				if(abbr == null) 
					abbr = '';
				if(company_name == null)
					company_name = '';
				spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' company_name='"+company_name+"'>"+abbr+" "+company_name+"</a></li>");
			}
		},'json');
		spList.css({left:$(this).position().left+"px",top:$(this).position().top+32+"px"}).show();
    });
    
    // 没选中供应商，焦点离开，隐藏列表
	$('#sp_filter').on('blur', function(){
 		$('#spList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#spList').on('blur', function(){
 		$('#spList').hide();
 	});

	$('#spList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});

	// 选中供应商
	$('#spList').on('mousedown', '.fromLocationItem', function(e){
		$('#sp_filter').val($(this).attr('company_name'));
		var provider = $(this).attr('partyId');
		$('#sp_id').val(provider);
        $('#spList').hide();
        savePartyInfo(provider,"SERVICE_PROVIDER");
    });
    
});

function datetimepicker(data){
	if(!$("#saveCarSummaryBtn").prop("disabled")){
		$('.input-append').datetimepicker({  
			    format: 'yyyy-MM-dd',  
			    language: 'zh-CN',
			    autoclose: true,
			    pickerPosition: "bottom-left"
			}).on('changeDate', function(ev){
				$(".bootstrap-datetimepicker-widget").hide();
				$(data).parent().prev("input").focus();
		});
	}
}


//按钮控制
var status = $("#status").val();
if(status != '新建' && status != 'new'){
	$('#addFee').hide();  
	$("#saveCostMiscOrderBtn").attr("disabled",true);
}

if(type == 'non_biz' && !is_origin){
	$('#addFee').hide();    	
	$('#saveCostMiscOrderBtn').attr('disabled', true);
}
