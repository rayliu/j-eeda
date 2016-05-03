
$(document).ready(function() {
	if(deliverOrder.orderNo){
		document.title = deliverOrder.orderNo +' | '+document.title;
        $('#title').text('编辑配送单');
	}else{
        $('#title').text('创建配送单');
    }
    var feeTable = $('#itemList-table').dataTable({        
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bPaginate": false, //翻页功能
        "bInfo": false,//页脚信息
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, // 不要排序
        //"bServerSide": true,
        "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+$("#costMiscOrderId").val(),
//        "fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
//            $(nRow).attr('id', aData.ID);
//            return nRow;
//        },        
         "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
             $(nRow).attr({id: aData.ID}); 
             $(nRow).attr({order_type: aData.ORDER_TYPE}); 
             return nRow;
         },

        "aoColumns": [ 
            {"mDataProp":"ID",'bVisible':false },
              {"mDataProp":"ITEM_NO", "sWidth": "20%",
               "sClass":'item_no',
               "fnRender": function(obj) {
                if(obj.aData.ITEM_NO!='' && obj.aData.ITEM_NO != null){
                     return "<input type='text'  name='item_no' value='"+obj.aData.ITEM_NO+"' class='form-control search-control'>";
                }else{
                     return "<input type='text'  name='item_no' class='form-control search-control'>";
                }//field_type='product_search'
             }
            },
            {"mDataProp":"ITEM_NAME","sWidth": "20%",
                 "fnRender": function(obj) {
                     if(obj.aData.ITEM_NAME!='' && obj.aData.ITEM_NAME != null){
                    return "<input type='text' name='item_name' value='"+obj.aData.ITEM_NAME+"' class='form-control search-control'>";
                }else{
                     return "<input type='text' name='item_name' class='form-control search-control'>";
                }
             }
            },
             {"mDataProp":"AMOUNT","sWidth": "10%",
                    "fnRender": function(obj) {
                        if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
                            return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"' class='form-control search-control'>";
                        }else{
                            return "<input type='text' name='amount' class='form-control search-control' value ='0'>";
                        }
                }
             },
             {"mDataProp":"UNIT","sWidth": "10%",
                    "sClass":'unit',
                    "fnRender": function(obj) {
                        if(obj.aData.UNIT!='' && obj.aData.UNIT != null){
                            var str="";
                            $("#unitList").children().each(function(){
                                if(obj.aData.UNIT == $(this).text()){
                                    str+="<option value='"+$(this).text()+"' selected = 'selected'>"+$(this).text()+"</option>";
                                }else{
                                    str+="<option value='"+$(this).text()+"'>"+$(this).text()+"</option>";
                                };
                            });
                            return "<select name='unit_id' class='form-control search-control'>"+str+"</select>";
                        }else{
                            var str="";
                            $("#unitList").children().each(function(){
                                str+="<option value='"+$(this).text()+"'>"+$(this).text()+"</option>";
                            });
                            return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
                        }
                 }
             },
            {"mDataProp":"ITEM_DESC","sWidth": "20%",
                "fnRender": function(obj) {
                    if(obj.aData.ITEM_DESC!='' && obj.aData.ITEM_DESC != null){
                        return "<input type='text' name='item_desc' value='"+obj.aData.ITEM_DESC+"'  class='form-control search-control'>";
                    }else{
                         return "<input type='text'  name='item_desc' class='form-control search-control'>";
                    }
                }
            },
            {"mDataProp": null,"sWidth": "10%",
                "fnRender": function(obj) {
                        return    "<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'><i class='fa fa-trash-o fa-fw'> </i>删除</a>";
                }
            }   
        ]      
    });
    
    
    
    //删除一行
    var deletedIds=[];
    $("#itemList-table").on('click', '.finItemdel', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedIds.push(tr.attr('id'));
        tr.remove();
    });    
    $('#deletedIds').val(deletedIds);
    
    
    $("#addItem").click(function(){
         feeTable.fnAddData({
            ID:'',
             ITEM_NO:'',
             ITEM_NAME:'',
             UNIT:'',
             AMOUNT: '0',
             ITEM_DESC:'',
         });
    });
    
    
    
    //item_no
    $('#itemList-table').on('click', 'input[name=item_no]', function(){
        var inputBox=$(this);
        inputBox.autocomplete({
            source: function( request, response ) {
                if(inputBox.parent().parent()[0].cellIndex >1){//从第3列开始，不需要去后台查数据
                    return;
                }
                $.ajax({
                    url: "/transferOrder/searchItemNo",
                    dataType: "json",
                    data: {
                        customerId: $('#customer_id').val(),
                        input: request.term
                    },
                    success: function( data ){
                        var columnName = inputBox.parent().parent()[0].className;
                        var itemNos =[];
                        $("input[name=item_no]").each(function(){
                               if($(this).val()!=null&&$(this).val()!=""){
                                   itemNos.push($(this).val());
                               }
                           });
                        if(data.length>0){
                            response($.map( data, function( data ) {
                                var complete="";
                                for(var i=0;i<itemNos.length;i++){
                                    if(data.ITEM_NO == itemNos[i]){
                                        complete = data.ITEM_NO;
                                    }
                                }
                                if(complete != data.ITEM_NO){
                                    return {
                                         label: '型号:'+data.ITEM_NO+' 名称:'+data.ITEM_NAME,
                                         value: columnName=='item_name'?data.ITEM_NAME:data.ITEM_NO,
                                         id: data.ID,
                                         item_no: data.ITEM_NO,
                                         item_name: data.ITEM_NAME
                                    };
                                }    
                            }));
                        }else{
                            var d = new Array(1);
                            if($("input[name='orderType']:checked").val() == "arrangementOrder"){
                                d[0] = "当前仓库内客户没有此类产品库存";
                            }else{
                                d[0] = "当前客户没有维护此类产品";
                            }
                            
                            response($.map(d, function( i ) {
                                return {
                                     value:'警告:'+i,
                                     id: '',
                                     item_no: '',
                                     item_name: '',
                                     label: ''
                                };
                            }));
                        }
                    }
                });
            },
            minLength: 2
        });
    });
    


	
	
	
	$('#checkQrC').hover(
			function(){//in
			$('#qrcodeCanv').show();
			},function(){//out
			$('#qrcodeCanv').hide();
		});
			
		$('#qrcodeCanv').qrcode({
			width: 120,
			height: 120,
			text	: 'http://'+window.location.host+'/wx/fileUpload/'+deliverOrder.orderNo //'http://'+window.location.host+'
		});	
	
	$('#menu_deliver').addClass('active').find('ul').addClass('in');

	$('#resetbutton').hide();
	$('#resetbutton2').hide();
	
	var parentId="chargeCheckOrderbasic";
	
	$('#driver_name').on('keyup click', function(){
 		var inputStr = $('#driver_name').val();
		//定义一个TYPE变量，用来作为车辆的条件
		var typeStr = "OWN";
 		$.get('/transferOrder/searchAllCarInfo', {input:inputStr,type:typeStr}, function(data){
 			var driverList = $("#driverList");
 			driverList.empty();
 			for(var i = 0; i < data.length; i++)
 			{
 				driverList.append("<li><a tabindex='-1' class='fromLocationItem' pid='"+data[i].ID+"' car_no='"+data[i].CAR_NO+"' phone='"+data[i].PHONE+"' driver='"+data[i].DRIVER+"' > "+data[i].DRIVER+" "+data[i].PHONE+"</a></li>");
 			}
 		},'json');
 		
 		$("#driverList").css({ 
         	left:$(this).position().left+"px", 
         	top:$(this).position().top+32+"px" 
        }); 
        $('#driverList').show();
	 });
	  	
 	 // 选中司机
 	 $('#driverList').on('mousedown', '.fromLocationItem', function(e){	
	  	 $('#driver_name').val($(this).attr('driver'));
	  	 $('#a5').html($(this).attr('car_no'));  
	  	 $('#a6').html($(this).attr('phone'));
	  	 $('#car_id').val($(this).attr('pid'));
	     $('#driverList').hide();   
     });
 	// 没选中司机，焦点离开，隐藏列表
  	$('#driver_name').on('blur', function(){
   		$('#driverList').hide();
   	});
 	// 没选中司机，焦点离开，隐藏列表
 	$('#driverList').on('blur', function(){
  		$('#driverList').hide();
  	});
	$('#spMessage').on('keyup click', function(){
		var inputStr = $('#spMessage').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/delivery/searchPartSp', {input:inputStr}, function(data){
			var spList =$("#spList");
			spList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var abbr = data[i].ABBR;
				if(abbr == null){
					abbr = '';
				}
				var company_name = data[i].COMPANY_NAME;
				if(company_name == null){
					company_name = '';
				}
				var contact_person = data[i].CONTACT_PERSON;
				if(contact_person == null){
					contact_person = '';
				}
				var phone = data[i].PHONE;
				if(phone == null){
					phone = '';
				}
				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');

		$("#spList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#spList').show();
	});
	// 没选中供应商，焦点离开，隐藏列表
	$('#spMessage').on('blur', function(){
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
		$('#spMessage').val($(this).text());
		$('#sp_id').val($(this).attr('spid'));
		$('#cid').val($(this).attr('code'));
		$('#a1').html($(this).attr('contact_person'));
		$('#a2').html($(this).attr('company_name'));
		$('#a3').html($(this).attr('address'));
		$('#a4').html($(this).attr('mobile'));
        $('#spList').hide();
    });
    $('#spChange').on('keyup click', function(){
		var inputStr = $('#spChange').val();
		$.get('/delivery/searchPartSp', {input:inputStr}, function(data){
			var spChangeList =$("#spChangeList");
			spChangeList.empty();
			for(var i = 0; i < data.length; i++)
			{
				var abbr = data[i].ABBR;
				if(abbr == null){
					abbr = '';
				}
				var company_name = data[i].COMPANY_NAME;
				if(company_name == null){
					company_name = '';
				}
				var contact_person = data[i].CONTACT_PERSON;
				if(contact_person == null){
					contact_person = '';
				}
				var phone = data[i].PHONE;
				if(phone == null){
					phone = '';
				}
				spChangeList.append("<li><a tabindex='-1' class='fromItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
			}
		},'json');
		$("#spChangeList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#spChangeList').show();
	});
// 没选中供应商，焦点离开，隐藏列表
	$('#spChange').on('blur', function(){
 		$('#spChangeList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#spChangeList').on('blur', function(){
 		$('#spChangeList').hide();
 	});

	$('#spChangeList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	// 选中供应商
	$('#spChangeList').on('mousedown', '.fromItem', function(e){
		$('#spChange').val($(this).text());
		$('#spChange_id').val($(this).attr("spid"));
        $('#spChangeList').hide();
    });
	var trandferOrderId = $("#tranferid").val();
	var localArr =$("#localArr").val();
	var localArr2 =$("#localArr2").val();
	var localArr3 =$("#localArr3").val();
	var aa =$("#transferstatus").val();
	var cargoNature =$("#cargoNature").val();
	if(cargoNature == "cargo"){
		$("#cargotable2").hide();
		$("#cargos").show();
		var warehouseId = $("#warehouse_id").val();
		var customerId = $("#customer_id").val();
		var transferItemIds = $("#transferItemIds").val();
		var productIds = $("#productIds").val();
		var shippingNumbers = $("#shippingNumbers").val();
		
		console.log("仓库：" +warehouseId+",客户："+customerId+",产品："+productIds+",数量："+shippingNumbers);
		var numbers = shippingNumbers.split(",");
		var num = 0;
		
		
			// 普货, 动态处理
			$('#cargo-table').dataTable({
				"bFilter": false, 
		        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
		        "iDisplayLength": 10,
		        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
		        "bServerSide": true,
		    	"oLanguage": {
		            "sUrl": "/eeda/dataTables.ch.txt"
		        },
		        "sAjaxSource": "/delivery/orderListCargo?transferItemIds="+transferItemIds,
		        "aoColumns": [
		            {"mDataProp":"ITEM_NO"},  
		            {"mDataProp":"ITEM_NAME"},
		            {"mDataProp":"VOLUME"},
		            {"mDataProp":"WEIGHT"},
		            {"mDataProp":null,
		            	"fnRender": function(obj) {
		            		return numbers[num++];
		            	}
		            },   
		            {"mDataProp":"ABBR"},
		            {"mDataProp":"ORDER_NO"}
		        ]      
		    });	
		} else{
		$('#eeda-table').dataTable({
			"bFilter": false, //不需要默认的搜索框
			"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "iDisplayLength": 10,
	        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
	        "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/delivery/orderList2?localArr="+localArr+"&localArr2="+localArr2,
	        "aoColumns": [
	            {"mDataProp":"ITEM_NO"},  
	            {"mDataProp":"SERIAL_NO",
	            	 "fnRender": function(obj) {
	 			        if(obj.aData.SERIAL_NO!='' && obj.aData.SERIAL_NO != null){
	 			            return "<input type='text' name='serial_no' value='"+obj.aData.SERIAL_NO+"' class='form-control search-control'>";
	 			        }else{
	 			        	 return "<input type='text'  name='serial_no' class='form-control search-control'>";
	 			        }
	 			}},
	            {"mDataProp":"PIECES"},
	            {"mDataProp":"ITEM_NAME"},
	            {"mDataProp":"VOLUME"},
	            {"mDataProp":"WEIGHT"},
	            {"mDataProp":"ORDER_NO", "sWidth": "12%"},
				{"mDataProp":"CUSTOMER"}
	        ]      
	    });	
	}
	// 获取所有网点
	 $.post('/transferOrder/searchAllOffice',function(data){
		 if(data.length > 0){
			 //console.log(data);
			 var deliveryOfficeSelect = $("#deliveryOfficeSelect");
			 deliveryOfficeSelect.empty();
			 var hideDeliveryOfficeSelect = $("#hideDeliveryOfficeSelect").val();
			 deliveryOfficeSelect.append("<option ></option>");	
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideDeliveryOfficeSelect){
					 deliveryOfficeSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].OFFICE_NAME+"</option>");
				 }else{
					 if(data[i].IS_STOP != true){
						 deliveryOfficeSelect.append("<option value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");
					 };
				 };
			 };
		 };
	 },'json');
	 //RDC仓库操作
	 $('#deliveryOfficeSelect').on('change', function(){ 
		 if($(this).val() != ""){
			 // 获取所有仓库
			 findWarehouseForOffice($(this).val());
		 }else{
			 $("#gateInSelect").empty();
		 }
	 });
	 var findWarehouseForOffice = function(officeId){
		 $.post('/transferOrder/searchAllWarehouse', {officeId: officeId},function(data){
			 if(data.length > 0){
				 var gateInSelect = $("#gateInSelect");
				 gateInSelect.empty();
				 var hideWarehouseId = $("#hideWarehouseId").val();
				 for(var i=0; i<data.length; i++){
					 if(data[i].ID == hideWarehouseId){
						 gateInSelect.append("<option value='"+data[i].ID+"' selected='selected'>"+data[i].WAREHOUSE_NAME+"</option>");
						 //$("#gateInSelect").val(data[i].ID);
					 }else{
						 gateInSelect.append("<option value='"+data[i].ID+"'>"+data[i].WAREHOUSE_NAME+"</option>");
					 }
				 }
				 if($("#gateInSelect").val() != "" && $("#gateInSelect").val() != null)
					 $("#gateInSelect").change();
			 }else{
				 $("#gateInSelect").empty();
			 }
		 },'json');	
	 };
	 var deliveryOfficeId = $("#hideDeliveryOfficeSelect").val();
	 if(deliveryOfficeId != null && deliveryOfficeId != ""){
		 findWarehouseForOffice(deliveryOfficeId);
	 };
	$("#eeda-table").on('blur', 'input,select', function(e){
		e.preventDefault();
		var delivery_id = $("#delivery_id").val();
		var detailId = $("#localArr2").val();
		var name = $(this).attr("name");
		var value = $(this).val();
		$.post('/delivery/updateTansterOrderItemDetail', {name:name, value:value,detailId:detailId}, function(data){
			if(data.ID > 0){
				$.scojs_message('更新货品明细成功', $.scojs_message.TYPE_OK);
			}else{
				$.scojs_message('更新货品明细失败', $.scojs_message.TYPE_OK);
			}
    	},'json');
	});
	$("input[name='warehouseNature']").each(function(){
		 if($(this).val()==$("#warehouseNatureRadio").val()){
		 	$(this).attr('checked','checked');
		 	$("#gateInDiv").show();
		 }else{
		 	$("#gateInDiv").hide();
		 }
	});
	$("input[name='warehouseNature']").on('click', function(){
    	console.log(this);
    	var inputId  = $(this).attr('id');
    	if(inputId=='warehouseNature1'){
    		//$("#gateInSelect").hide();
    		$("#gateInDiv").hide();
    	}else{
    		$("#gateInDiv").show();
    	}
    });
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	var saveDelivery = function(){
	////货品明细的构造
		 $('#deletedIds').val(deletedIds);
		 deletedIds = [];
		 var tableRows = $("#itemList-table tr");
        var itemsArray=[];
        for(var index=0; index<tableRows.length; index++){
        	if(index==0)
        		continue;

        	var row = tableRows[index];
        	var id = $(row).attr('id');
        	if(!id){
        		id='';
        	}
        	
        	var item=[
        	    id,
        		$(row.children[0]).find('input').val(), 
			 	$(row.children[1]).find('input').val(),
			 	$(row.children[2]).find('input').val(),
			 	$(row.children[3]).find('select').val(),
			 	$(row.children[4]).find('input').val(),
			 	'空'+'&'
			];
        	itemsArray.push(item);
        }
        $('#JsonDetail').val(itemsArray);

		
		$("#sign_document_no").val($("#sign_no").val());
		var mbProvinceTo = $("#mbProvinceTo").find("option:selected").text();
		var cmbCityTo = $("#cmbCityTo").find("option:selected").text();
		var cmbAreaTo = $("#cmbAreaTo").find("option:selected").text();
		var business_stamp =$('#business_stamp').val();
		var sp_id = $("#sp_id").val();
		var car_id = $("#car_id").val();
		var modeDelvery=$("input[name='modeDelvery']:checked").val();
		if(sp_id == ""&&modeDelvery=="out_source"){
			alert("请选择有效的供应商");
			return false;
		}
		if(car_id == ""&&modeDelvery=="own"){
			alert("请选择有效的司机");
			return false;
		}
		if(mbProvinceTo == "--请选择省份--" || mbProvinceTo == ""){
			alert("请输入目的地省份");
			return false;
		}
		if(cmbCityTo == "--请选择城市--" || cmbCityTo == ""){
			alert("请输入目的地城市");
			return false;
		}
		if(business_stamp == ""){
			alert("请输入业务要求配送时间");
			return false;
		}
		$("#receivingunit").val($("#notify_address").val());
		/*if(cmbAreaTo == "--请选择区(县)--" || cmbAreaTo == ""){
			alert("请输入目的地区（县）");
			return false;
		}*/
		$("#saveBtn").attr("disabled", true);
        // 异步向后台提交数据
        $.post('/delivery/deliverySave',$("#deliveryForm").serialize(), function(data){
            console.log(data);
            if(data.ID>0){
            	$("#delivery_id").val(data.ID);
            	$("#change_delivery_id").val(data.DELIVERY_ID);
            	// $("#style").show();
            	$("#order_no").text(data.ORDER_NO);
            	$("#deliveryOrder_status").text(data.STATUS);
            	if(data.STATUS=='新建'|| data.STATUS=='计划中' ){
        			$("#ConfirmationBtn").attr("disabled", false);
        		}
            	contactUrl("edit?id",data.ID);
            	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
            	
            	if($('#isNullOrder').val()=='Y'){
	            	feeTable.fnSettings().oFeatures.bServerSide = true;
	            	feeTable.fnSettings().sAjaxSource="/delivery/itemsList?delivery_id="+data.ID;
	            	feeTable.fnDraw();
            	}
            	//window.location.reload()

            }else{
                alert('数据保存失败。');
            }
            $("#saveBtn").attr("disabled", false);
         },'json');
            
	 };
	// 添加配送单
	$("#saveBtn").click(function(e){
        // 阻止a 的默认响应行为，不需要跳转
		// var itemId = $("#item_id").val();
		e.preventDefault();
		saveDelivery();
    });
	// 回显配送方式
	$("input[name='modeDelvery']").each(function(){
		if($("#deliveryModeRadio").val() == $(this).val()){
			$(this).attr('checked', true);			
			if($(this).val() == 'own'){
				$("#textDiv1").show();
				$("#textDiv").hide();
				$('#spMessage').val("");
				$('#sp_id').val("");
				$('#cid').val("");
				$('#a1').html("");
				$('#a2').html("");
				$('#a3').html("");
				$('#a4').html("");
			}else{
				$("#textDiv").show();
				$("#textDiv1").hide();			
				$('#car_id').val("");
				$('#driver_name').val("");
				$('#a5').html("");
				$('#a6').html("");
			}
		}
	});
	//改变配送方式
	$("input[name='modeDelvery']").on('click',function(){			
		if($(this).val() == 'own'){
			$("#textDiv1").show();
			$("#textDiv").hide();
			$('#spMessage').val("");
			$('#sp_id').val("");
			$('#cid').val("");
			$('#a1').html("");
			$('#a2').html("");
			$('#a3').html("");
			$('#a4').html("");
		}else{
			$("#textDiv").show();
			$("#textDiv1").hide();			
			$('#car_id').val("");
			$('#driver_name').val("");
			$('#a5').html("");
			$('#a6').html("");
		}
	});
	// 发车确认
	$("#ConfirmationBtn").click(function(){
		// 浏览器启动时,停到当前位置
		// debugger;
		$("#receiptBtn").attr("disabled", false); 
		var code = $("#warehouseCode").val();
		var locationTo = $("#locationTo").val();
		var delivery_id = $("#delivery_id").val();
		var priceType = $("input[name='priceType']:checked").val();
		var warehouseId = $("#warehouse_id").val();
		var customerId = $("#customer_id").val();
		var transferItemIds = $("#transferItemIds").val();
		var productIds = $("#productIds").val();
		var shippingNumbers = $("#shippingNumbers").val();
		var cargoNature =$("#cargoNature").val();
		var business_stamp =$('#business_stamp').val();
		$("#sign_document_no").val($("#sign_no").val());
		var mbProvinceTo = $("#mbProvinceTo").find("option:selected").text();
		var cmbCityTo = $("#cmbCityTo").find("option:selected").text();
		var modeDelvery=$("input[name='modeDelvery']:checked").val();
		var sp_id = $("#sp_id").val();
		var car_id = $("#car_id").val();
		if(sp_id == ""&&modeDelvery=="out_source"){
			alert("请选择有效的供应商");
			return false;
		}
		if(car_id == ""&&modeDelvery=="own"){
			alert("请选择有效的司机");
			return false;
		}
		if(mbProvinceTo == "--请选择省份--" || mbProvinceTo == ""){
			alert("请输入目的地省份");
			return false;
		}
		if(cmbCityTo == "--请选择城市--" || cmbCityTo == ""){
			alert("请输入目的地城市");
			return false;
		}
		if(business_stamp == ""){
			alert("请输入业务要求配送时间");
			return false;
		}
        var depart_date =$('#depart_date').val();

        if(depart_date == ""){
            alert("请输发车时间");
            return false;
        }

		$.post('/deliveryOrderMilestone/departureConfirmation',{delivery_id:delivery_id,code:code,locationTo:locationTo,priceType:priceType,
			warehouseId:warehouseId,customerId:customerId,transferItemIds:transferItemIds,
			productIds:productIds,shippingNumbers:shippingNumbers,cargoNature:cargoNature,
            depart_date: depart_date
        },function(data){
			var MilestoneTbody = $("#transferOrderMilestoneTbody");
			MilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");

			paymenttable.fnSettings().sAjaxSource="/deliveryOrderMilestone/accountPayable/"+delivery_id;
			paymenttable.fnDraw();
			$.scojs_message('发车成功', $.scojs_message.TYPE_OK);
			$('#deliveryOrder_status').html('配送在途');
		},'json');
		$("#ConfirmationBtn").attr("disabled", true);
		//$("#receiptBtn").attr("disabled", false);
	});
	//应付
	$("#arapTab").click(function(e){
		var warehouseNature=$("input[name='warehouseNature']:checked").val()
		if(warehouseNature=='warehouseNatureNo'){
			$("#change_item").hide();
		}
		else{
			$("#change_item").show();
		}
		e.preventDefault();
		parentId = e.target.getAttribute("id");
	});
	// 应收
	$("#arap").click(function(e){
		e.preventDefault();
		parentId = e.target.getAttribute("id");
	});
	// 货品明细
	$("#departOrderItemList").click(function(e){
		e.preventDefault();
		parentId = e.target.getAttribute("id");
	});
	// 基本信息
	$("#chargeCheckOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	// 运输里程碑
	$("#transferOrderMilestoneList").click(function(e){
		e.preventDefault();
    	// 切换到货品明细时,应先保存运输单
    	// 提交前，校验数据
        if($("#delivery_id").val() == ""){
	    	$.post('/transferOrder/saveTransferOrder', $("#transferOrderForm").serialize(), function(transferOrder){
				$("#transfer_order_id").val(transferOrder.ID);
				$("#update_transfer_order_id").val(transferOrder.ID);
				$("#order_id").val(transferOrder.ID);
				$("#transfer_milestone_order_id").val(transferOrder.ID);
				$("#id").val(transferOrder.ID);
				if(transferOrder.ID>0){
					$("#arrivalModeVal").val(transferOrder.ARRIVAL_MODE);
				  	// $("#style").show();
				  	var order_id = $("#order_id").val();
					$.post('/deliveryOrderMilestone/transferOrderMilestoneList',{order_id:order_id},function(data){
						var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
						transferOrderMilestoneTbody.empty();
						for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
						{
							transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
						}
					},'json');
				}else{
					alert('数据保存失败。');
				}
			},'json');
        }else{
		  	var delivery_id = $("#delivery_id").val(); 
		  	$("#transfer_milestone_delivery_id").val(delivery_id); 
			$.post('/deliveryOrderMilestone/transferOrderMilestoneList',{delivery_id:delivery_id},function(data){
				var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
				transferOrderMilestoneTbody.empty();
				for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
				{
					transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
				}
			},'json'); 
			
        }
        parentId = e.target.getAttribute("id");
	});
			
	// 保存新里程碑
	$("#deliveryOrderMilestoneFormBtn").click(function(){
		$.post('/deliveryOrderMilestone/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$('#deliveryOrderMilestone').modal('hide');
	});
	
	// 回单签收
	$("#receiptBtn").click(function(){
		var delivery_id = $("#delivery_id").val();
		$.post('/deliveryOrderMilestone/receipt',{delivery_id:delivery_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
		},'json');
		$("#receiptBtn").attr("disabled", true);
	});
				
	/*$(function(){
		console.log(aa);
		if(aa!=''){
			$("#cargotable").show();
			$("#cargotable2").hide();
			//$("#tranferdiv").show();
			
		}else{
			$("#cargotable").hide();
			$("#cargotable2").show();
			$("#tranferdiv").hide();
		}
	}) ;*/
	$(function(){
		//按钮控制
		//配送对账状态
		//只要对账没确认就可以保存
		var audit_status=$("#audit_status").val();
		if(audit_status == '新建'){
			$("#saveBtn").attr("disabled",false);
		}else{
			$("#saveBtn").attr("disabled", true);
		}
		if($("#deliverystatus").val() =='新建'|| $("#deliverystatus").val()=='计划中' ){
			$("#ConfirmationBtn").attr("disabled", false);
		}else{
			$("#ConfirmationBtn").attr("disabled", true);
		}
	}) ;
	
	// 应付datatable
	var deliveryid =$("#delivery_id").val();
	var paymenttable=$('#table_fin2').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, // 不需要默认的搜索框
        // "sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "bLengthChange":false,
        "sAjaxSource": "/deliveryOrderMilestone/accountPayable/"+deliveryid,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
			{"mDataProp":"FIN_ITEM_NAME",
			    "fnRender": function(obj) {
			        if(obj.aData.FIN_ITEM_NAME!='' && obj.aData.FIN_ITEM_NAME != null){
			        	var str="";
			        	$("#paymentItemList").children().each(function(){
			        		if(obj.aData.FIN_ITEM_NAME == $(this).text()){
			        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
			        		}else{
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			        	if(obj.aData.CREATE_NAME == 'system'){
			        		return obj.aData.FIN_ITEM_NAME;
			        	}else{
			        		return "<select name='fin_item_id'>"+str+"</select>";
			        	}
			        }else{
			        	var str="";
			        	$("#paymentItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	return "<select name='fin_item_id'>"+str+"</select>";
			        }
			 }},
			{"mDataProp":"AMOUNT",
			     "fnRender": function(obj) {
			    	 if(obj.aData.CREATE_NAME == 'system'){
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return obj.aData.AMOUNT;
				         }else{
				         	 return "";
				         }
			    	 }else{
				         if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
				         }else{
				         	 return "<input type='text' name='amount'>";
				         }
			    	 }
			 }},
			/*
			 * {"mDataProp":"FIN_ITEM_NAME","sWidth":
			 * "80px","sClass": "name"},
			 * {"mDataProp":"AMOUNT","sWidth": "80px","sClass":
			 * "amount"},
			 */
			{"mDataProp":"STATUS","sClass": "status"},
			{"mDataProp":"TRANSFERORDERNO","sClass": "amount", "bVisable":false},  
			{"mDataProp":"REMARK",
                "fnRender": function(obj) {
                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                        return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
                    }else{
                    	 return "<input type='text' name='remark'>";
                    }
            }},  
			/*
			 * {"mDataProp":"REMARK","sWidth": "80px","sClass":
			 * "remark"},
			 */
			{  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
              		"<i class='fa fa-trash-o fa-fw'> </i> "+
              		"删除"+
              		"</a>";
                }
            }     
        ]      
    });
	// 应付datatable
	var change_delivery_id =$("#change_delivery_id").val();
	var paymenttable3=$('#table_fin3').dataTable({
		"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": false, // 不需要默认的搜索框
        // "sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "bLengthChange":false,
        "sAjaxSource": "/deliveryOrderMilestone/accountPayable/"+change_delivery_id,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
			{"mDataProp":"FIN_ITEM_NAME",
			    "fnRender": function(obj) {
			        if(obj.aData.FIN_ITEM_NAME!='' && obj.aData.FIN_ITEM_NAME != null){
			        	var str="";
			        	$("#paymentItemList").children().each(function(){
			        		if(obj.aData.FIN_ITEM_NAME == $(this).text()){
			        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
			        		}else{
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			        	if(obj.aData.CREATE_NAME == 'system'){
			        		return obj.aData.FIN_ITEM_NAME;
			        	}else{
			        		return "<select name='fin_item_id'>"+str+"</select>";
			        	}
			        }else{
			        	var str="";
			        	$("#paymentItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	return "<select name='fin_item_id'>"+str+"</select>";
			        }
			 }},
			{"mDataProp":"AMOUNT",
			     "fnRender": function(obj) {
			    	 if(obj.aData.CREATE_NAME == 'system'){
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return obj.aData.AMOUNT;
				         }else{
				         	 return "";
				         }
			    	 }else{
				         if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
				         }else{
				         	 return "<input type='text' name='amount'>";
				         }
			    	 }
			 }},
			/*
			 * {"mDataProp":"FIN_ITEM_NAME","sWidth":
			 * "80px","sClass": "name"},
			 * {"mDataProp":"AMOUNT","sWidth": "80px","sClass":
			 * "amount"},
			 */
			{"mDataProp":"STATUS","sClass": "status"},
			{"mDataProp":"TRANSFERORDERNO","sClass": "amount", "bVisable":false},  
			{"mDataProp":"REMARK",
                "fnRender": function(obj) {
                    if(obj.aData.REMARK!='' && obj.aData.REMARK != null){
                        return "<input type='text' name='remark' value='"+obj.aData.REMARK+"'>";
                    }else{
                    	 return "<input type='text' name='remark'>";
                    }
            }},  
			/*
			 * {"mDataProp":"REMARK","sWidth": "80px","sClass":
			 * "remark"},
			 */
			{  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'>"+
              		"<i class='fa fa-trash-o fa-fw'> </i> "+
              		"删除"+
              		"</a>";
                }
            }     
        ]      
    });
	/*
	 * //应收 $("#item_fin_save").click(function(){ var deliveryid
	 * =$("#delivery_id").val();
	 * $.post('/deliveryOrderMilestone/receiptSave/'+deliveryid,
	 * $("#fin_form").serialize(), function(data){ console.log(data);
	 * if(data.success){ //receipttable.fnDraw();
	 * $('#fin_item').modal('hide'); $('#resetbutton').click(); }else{ }
	 * 
	 * }); });
	 */
	// 应付
	$("#addrow").click(function(){
		var deliveryid =$("#delivery_id").val();
		if(deliveryid != "" && deliveryid != null){
			$.post('/deliveryOrderMilestone/addNewRow/'+deliveryid,function(data){
				console.log(data);
				paymenttable.fnSettings().sAjaxSource = "/deliveryOrderMilestone/accountPayable/"+deliveryid;
				paymenttable.fnDraw();
			});		
		}else{
			$.scojs_message('请先保存配送单', $.scojs_message.TYPE_ERROR);
		}
	});
	$("#addrow1").click(function(){
		var deliveryid =$("#change_delivery_id").val();
		if(deliveryid != "" && deliveryid != null){
			$.post('/deliveryOrderMilestone/addNewRow/'+deliveryid,function(data){
				paymenttable3.fnSettings().sAjaxSource = "/deliveryOrderMilestone/accountPayable/"+deliveryid;
				paymenttable3.fnDraw();
			});		
		}else{
			$.scojs_message('请先保存配送单', $.scojs_message.TYPE_ERROR);
		}
	});
	// 应付修改
	$("#table_fin2").on('blur', 'input,select', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(value != "" && value != null){
			$.post('/deliveryOrderMilestone/updateDeliveryOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
				if(!data.success){
					$.scojs_message('修改失败', $.scojs_message.TYPE_ERROR);
				}
	    	},'json');
		}
	});
	// 应付修改
	$("#table_fin3").on('blur', 'input,select', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(value != "" && value != null){
			$.post('/deliveryOrderMilestone/updateDeliveryOrderFinItem', {paymentId:paymentId, name:name, value:value}, function(data){
				if(!data.success){
					$.scojs_message('修改失败', $.scojs_message.TYPE_ERROR);
				}
	    	},'json');
		}
	});
	//异步删除应付
	 $("#table_fin2").on('click', '.finItemdel', function(e){
		 var id = $(this).attr('code');
		  e.preventDefault();
		  $.post('/deliveryOrderMilestone/finItemdel/'+id,function(data){
               //保存成功后，刷新列表
               console.log(data);
               var deliveryid =$("#delivery_id").val();
               paymenttable.fnSettings().sAjaxSource = "/deliveryOrderMilestone/accountPayable/"+deliveryid;
               paymenttable.fnDraw();
           },'json');
	 });
	 //异步删除应付
	 $("#table_fin3").on('click', '.finItemdel', function(e){
		 var id = $(this).attr('code');
		  e.preventDefault();
		  $.post('/deliveryOrderMilestone/finItemdel/'+id,function(data){
               //保存成功后，刷新列表
               console.log(data);
               var deliveryid =$("#change_delivery_id").val();
               paymenttable3.fnSettings().sAjaxSource = "/deliveryOrderMilestone/accountPayable/"+deliveryid;
               paymenttable3.fnDraw();
           },'json');
	 });

		
    // 获取全国省份
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
    
    // 获取省份的城市
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
    
    // 获取城市的区县
    $('#cmbCityTo').on('change', function(){
		var inputStr = $(this).val();
		$("#locationTo").val(inputStr);
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
		$("#locationTo").val(inputStr);
	});  

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
						//$("#locationTo").val(data[i].CODE);
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
						//$("#locationTo").val(data[i].CODE);
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
    
    $("#mbProvinceTo").on('change',function(){
		$("#locationTo").val($(this).val());
		$("#cmbAreaTo").get(0).selectedIndex=0;
		$("#cmbAreaTo").empty();
	});
	$("#cmbCityTo").on('change',function(){
		$("#locationTo").val($(this).val());
		
		
	});
	$("#cmbAreaTo").on('change',function(){
		$("#locationTo").val($(this).val());
	});


    //计费方式回显
	var departOrderId = $("#delivery_id").val();
	if(departOrderId != '' && departOrderId != null){
		var departOrderChargeType = $("#chargeTypeRadio").val();

		$("input[name='chargeType']").each(function(){
			if(departOrderChargeType == $(this).val()){
				//零担
				if(departOrderChargeType == "perCargo"){
					//隐藏车辆信息
					$("#carInfomation").hide();
					
					$(this).prop('checked', true);
					$("#ltl_price_type").show();
					var hibLtlUnitType = $("#hibLtlUnitType").val();
					$("input[value='"+hibLtlUnitType+"']").prop('checked', true);
				}else if(departOrderChargeType == "perCar"){
                    //显示车辆信息                   
                    $(this).prop('checked', true);
                    $("#car_type_div").show();
                    var departOrderCarType = $("#hiddenDeliveryOrderCarType").val();
                    $("#car_type").val(departOrderCarType);
                }else{
    				if(departOrderChargeType=="perUnit"){
    					$("#carInfomation").hide();
    				}else{
    					$("#carInfomation").show();
    				}
    				$(this).prop('checked', true);
    			}
			}
		});
	}else{
		var transferOrderChargeType = $("#transferOrderChargeType").val();
		$("input[name='chargeType']").each(function(){
			
			if(transferOrderChargeType == $(this).val()){
				//零担
				if(transferOrderChargeType == "perCargo"){
					$("#carInfomation").hide();
					$(this).prop('checked', true);
					$("#ltl_price_type").show();
					$("#optionsRadiosIn1").prop('checked', true);
					//隐藏车辆信息								
    			}else if(transferOrderChargeType == "perCar"){
                    $("#carInfomation").show();
                    //显示车辆信息                   
                    $(this).prop('checked', true);
                    $("#car_type_div").show();
                }else{
    				if(transferOrderChargeType=="perUnit"){
    					$("#carInfomation").hide();
    				}else{
    					$("#carInfomation").show();
    				}
    				
    				$(this).prop('checked', true);
    			}
			}
		});
	}

     $("input[name='chargeType']").click(function(){
    	 //等于零担的时候
        if($('input[name="chargeType"]:checked').val()==='perCargo'){
            $('#ltl_price_type').show();
            $("#carInfomation").hide();
            $("#car_type_div").hide();
        }else if($('input[name="chargeType"]:checked').val()==='perCar'){
            $("#carInfomation").show();
            //显示车辆信息                   
            $(this).prop('checked', true);
            $("#car_type_div").show();
            $('#ltl_price_type').hide();
        }else{
            $('#ltl_price_type').hide();
            $("#car_type_div").hide();
            //计费方式为计件的时候
            if($('input[name="chargeType"]:checked').val()==='perUnit'){
            	$("#carInfomation").hide();
            }else{
            	$("#carInfomation").show();
            }
        }
     });
	
	
	
	$('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#order_delivery_stamp').trigger('keyup');
    });	
	
	$('#datetimepicker1').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#client_order_stamp').trigger('keyup');
    });	
	
	$('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#business_stamp').trigger('keyup');
    });	
		
		
	/*//回显初始地
	var locationFrom = $("#locationForm").val();
	if(locationFrom == "")
		$("#hideLocationFrom").val();
	//var searchAllLocationFrom = function(locationFrom){
	if(locationFrom != ""){
    	$.get('/transferOrder/searchLocationFrom', {locationFrom:locationFrom}, function(data){
    		console.log(data);			
    		var provinceVal = data.PROVINCE;
    		var cityVal = data.CITY;
    		var districtVal = data.DISTRICT;
	        $.get('/serviceProvider/searchAllLocation', {province:provinceVal, city:cityVal}, function(data){	
		        //获取全国省份
	         	var province = $("#mbProvinceFrom");
	     		province.empty();
	     		province.append("<option>--请选择省份--</option>");
	     		for(var i = 0; i < data.provinceLocations.length; i++){
					if(data.provinceLocations[i].NAME == provinceVal){
						$("#locationForm").val(data.provinceLocations[i].CODE);
						province.append("<option value= "+data.provinceLocations[i].CODE+" selected='selected'>"+data.provinceLocations[i].NAME+"</option>");
					}else{
						province.append("<option value= "+data.provinceLocations[i].CODE+">"+data.provinceLocations[i].NAME+"</option>");						
					}
				}

				var cmbCity =$("#cmbCityFrom");
	     		cmbCity.empty();
				cmbCity.append("<option  value=''>--请选择城市--</option>");
				for(var i = 0; i < data.cityLocations.length; i++)
				{
					if(data.cityLocations[i].NAME == cityVal){
						$("#locationForm").val(data.cityLocations[i].CODE);
						cmbCity.append("<option value= "+data.cityLocations[i].CODE+" selected='selected'>"+data.cityLocations[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data.cityLocations[i].CODE+">"+data.cityLocations[i].NAME+"</option>");						
					}
				}
				
				if(data.districtLocations.length > 0){
    				var cmbArea =$("#cmbAreaFrom");
    				cmbArea.empty();
    				cmbArea.append("<option  value=''>--请选择区(县)--</option>");
    				for(var i = 0; i < data.districtLocations.length; i++)
    				{
    					if(data.districtLocations[i].NAME == districtVal){
    						$("#locationForm").val(data.districtLocations[i].CODE);
    						cmbArea.append("<option value= "+data.districtLocations[i].CODE+" selected='selected'>"+data.districtLocations[i].NAME+"</option>");
    					}else{
    						cmbArea.append("<option value= "+data.districtLocations[i].CODE+">"+data.districtLocations[i].NAME+"</option>");						
    					}
    				}
    			}else{
    				var cmbArea =$("#cmbArea");
    				cmbArea.empty();
    			}
	        },'json');
    	},'json');
  	};*/
		
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
						//$("#locationForm").val(data[i].CODE);
					}else{
						province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     	},'json');
    });
    
    //获取省份的城市
    $('#mbProvinceFrom').on('change', function(){
		var inputStr = $(this).val();
		$("#locationForm").val(inputStr);
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
		$("#locationForm").val(inputStr);
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
		$("#locationForm").val(inputStr);
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
					$("#locationForm").val(data[i].CODE);
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
					$("#locationForm").val(data[i].CODE);
				}else{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
			}
		}
	},'json'); 
    
    
    function getChargetype(){
    	//判断修改后相应的计费方式修改
    	var customer_id = $("#customer_id").val();
    	var sp_id = $("#sp_id").val();
    	if(customer_id != null && customer_id !="" && sp_id != null && sp_id !=""){
    		//获取当前供应商客户的计费方式
    		$.post("/serviceProvider/seachChargeType",{sp_id:sp_id,customer_id:customer_id},function(data){
    			if(data.CHARGE_TYPE == null){
    				//这里是当前客户和供应商没有数据维护的情况
    				$("input[name='chargeType']").each(function(){
    					if($(this).val() == 'perUnit'){
    						$(this).prop('checked', true);
    					}
    				});
    			}else{
    				 
    				$("input[name='chargeType']").each(function(){
    					if($(this).val() == data.CHARGE_TYPE){
    						$(this).prop('checked', true);
    					}
    				});
    			}
    		},'json');
    	}
    }


	//撤销订单
	$("#deleteBtn").on('click',function(){
		var status = $('#deliveryOrder_status').text();     //单据状态
		var audit_status = $('#audit_status').val();        //财务状态
		var order_id = $("#delivery_id").val();             //单据ID
		var cargoNature = $('#cargoNature').val();          //货品属性
		if(!confirm("是否确认撤销此订单？"))
			return;
		if(order_id==""){
			$.scojs_message('对不起，当前单据尚未保存，不能撤销', $.scojs_message.TYPE_ERROR);
			return;
		}else if(audit_status!='新建'&& audit_status!='已确认'){
	    	$.scojs_message('对不起，当前单据已做了财务单据，不能撤销', $.scojs_message.TYPE_ERROR);
		}else if(status=='已送达'){
	    	$.scojs_message('对不起，当前单据已有下级单据(申请单。。。)，不能撤销', $.scojs_message.TYPE_ERROR);
		}else{
			$("#deleteBtn").attr('disabled',true);
			$.post('/delivery/deleteOrder', {orderId:order_id,cargoNature:cargoNature}, function(data){ 
	    		if(!data.success){
	    			$("#deleteBtn").attr('disabled',false);
	    			$.scojs_message('撤销失败', $.scojs_message.TYPE_ERROR);
	    		}else{
	    			$.scojs_message('撤销成功!,3秒后自动返回。。。', $.scojs_message.TYPE_OK);
	    			setTimeout(function(){
						location.href="/delivery";
					}, 3000);
	    		}
	    	});
		}
	});
    
});


