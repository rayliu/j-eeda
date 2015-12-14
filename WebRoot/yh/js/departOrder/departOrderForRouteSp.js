
$(document).ready(function() {
    $('#menu_assign').addClass('active').find('ul').addClass('in'); 
    var spName = [];
    var routeArr = [];
    var spNameUnchecked = [];
    var officeName=[];
    var model=[];
	//datatable, 动态处理
    var datatable = $('#routeSpAndPerCarTable').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	//"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/departOrder/createTransferOrderListForRouteSp",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                	 return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
                 }
            },
            { "mDataProp": "ORDER_NO"},
            { "mDataProp": "CNAME"},
            { "mDataProp": "CUSTOMER_ORDER_NO"},
            { "mDataProp": "PLANNING_TIME"},
            { "mDataProp":"OPERATION_TYPE",
            	"sClass": "operation_type",
    			"fnRender": function(obj) {
    				if(obj.aData.OPERATION_TYPE == "out_source"){
    					return "外包";
    				}else if(obj.aData.OPERATION_TYPE == "own"){
    					return "自营";
    				}else{
    					return "";
    				}}}, 
            { "mDataProp":"CARGO_NATURE",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return departOrderForRoute.ex_cargo;
            		}else{
            			return "";
            		}}},
            { "mDataProp": "DOADDRESS"},
            { "mDataProp": "ARRIVAL_MODE",
            	"sClass": "arrival_model",
            	"fnRender": function(obj) {
            		if(obj.aData.ARRIVAL_MODE == "delivery" || obj.aData.ARRIVAL_MODE == "deliveryToWarehouse" || obj.aData.ARRIVAL_MODE == "deliveryToFactory"
            			|| obj.aData.ARRIVAL_MODE == "deliveryToFachtoryFromWarehouse"){
            			return "直送";
            		}else{
            			return "入中转仓";
            		}
            	},
            },
            {"mDataProp":"PICKUP_MODE",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == "routeSP"){
            			return "干线供应商自提";
            		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
            			return "外包供应商提货";
            		}else if(obj.aData.PICKUP_MODE == "own"){
            			
            			return departOrderForRoute.ex_type;
            		}else{
            			console.log(obj.aData.PICKUP_MODE);
            			return "";
            		}}},
            { 
            	"mDataProp": "SPNAME",
            	"sClass": "spname"
            },
            { 
            	"mDataProp": "ROUTE_FROM",
                "sClass": "route_from"
            },
            { 
            	"mDataProp": "ROUTE_TO",
                "sClass": "route_to"
            },
            { 
            	"mDataProp": "OFFICE_NAME",
                "sClass": "office_name"
            }
        ]      
    });	
    
    $('#saveBtn').click(function(e){
        e.preventDefault();
    	var trArr=[];
        var tableArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		trArr.push($(this).val());
        	}
        });
        tableArr.push(trArr);        
        console.log(tableArr);
        $('#pickupOrder_message').val(tableArr);
        $('#createForm').submit();
    });
    
    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $('#beginTime_filter').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $('#endTime_filter').trigger('keyup');
    });
    
    $("#routeTo_filter ,#endTime_filter ,#beginTime_filter ,#routeFrom_filter ,#customer_filter ,#address_filter ,#status_filter ,#orderNo_filter").on( 'keyup click', function () {    	 	
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var routeFrom = $("#routeFrom_filter").val();
    	var routeTo = $("#routeTo_filter").val();
    	datatable.fnSettings().sAjaxSource = "/departOrder/createTransferOrderListForRouteSp?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo;
    	datatable.fnDraw();    	
    });
  //获取所有客户
    $('#customer_filter').on('keyup click', function(){
           var inputStr = $('#customer_filter').val();
           $.get("/customerContract/search", {locationName:inputStr}, function(data){
               console.log(data);
               var companyList =$("#companyList");
               companyList.empty();
               for(var i = 0; i < data.length; i++)
               {
                   companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
               }
               if(data.length>0)
                   companyList.show();
               
           },'json');
           
       });

   //选中某个客户时候
      $('#companyList').on('click', '.fromLocationItem', function(e){        
           $('#customer_filter').val($(this).text());
           $("#companyList").hide();
           var orderNo = $("#orderNo_filter").val();
	       	var status = $("#status_filter").val();
	       	var address = $("#address_filter").val();
	       	var customer = $("#customer_filter").val();
	       	var beginTime = $("#beginTime_filter").val();
	       	var endTime = $("#endTime_filter").val();
	       	var routeFrom = $("#routeFrom_filter").val();
	       	var routeTo = $("#routeTo_filter").val();
	       	if(customer!=null){
	       		datatable.fnSettings().sAjaxSource = "/departOrder/createTransferOrderListForRouteSp?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo;
		       	datatable.fnDraw(); 
	       	}
	       
       });
    // 没选中客户，焦点离开，隐藏列表
       $('#customer_filter').on('blur', function(){
           $('#companyList').hide();
       });

       //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
       $('#companyList').on('blur', function(){
           $('#companyList').hide();
       });

       $('#companyList').on('mousedown', function(){
           return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
       });
	
	
    $("#routeSpAndPerCarTable").on('click', '.checkedOrUnchecked', function(e){
    	if(spName.length == 0){
    		$("#saveBtn").attr('disabled', true);
    	}
		if($(this).prop("checked") == true){
			$("#saveBtn").attr('disabled', false);
			if(spName.length != 0){
				if(spName[0] != $(this).parent().siblings('.spname')[0].innerHTML && $(this).parent().siblings('.spname')[0].innerHTML != ''){
					alert("请选择同一供应商!");
					return false;
				}else{
					if(routeArr.length != 0){
						if(routeArr[0] != $(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML && $(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML != ''){
							alert("请选择同一线路的运输单!");
							return false;
						}else{
							if(officeName.length != 0){
								if(officeName[0] != $(this).parent().siblings('.office_name')[0].innerHTML){
									alert("请选择同一个网点的运输单");
									return false;
								}else{
									if(model.length !=0){
										if(model[0] != $(this).parent().siblings('.arrival_model')[0].innerHTML){
											alert("请选择同一到达方式的运输单");
											return false;
										}else{
											spName.push($(this).parent().siblings('.spname')[0].innerHTML);
											routeArr.push($(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML);
											officeName.push($(this).parent().siblings('.office_name')[0].innerHTML);
											model.push($(this).parent().siblings('.arrival_model')[0].innerHTML);
											
										}
									}else{
										if($(this).parent().siblings('.arrival_model')[0].innerHTML != ''){
											model.push($(this).parent().siblings('.arrival_model')[0].innerHTML);
										}
									}
									
								}
								
							}else{
								if($(this).parent().siblings('.office_name')[0].innerHTML != ''){
									officeName.push($(this).parent().siblings('.office_name')[0].innerHTML);
								}
							}
						}
					}else{
						if($(this).parent().siblings('.operation_type')[0].innerHTML != ''){
							routeArr.push($(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML);
						}
					}
				}
			}else{
				if($(this).parent().siblings('.spname')[0].innerHTML != ''){
					spName.push($(this).parent().siblings('.spname')[0].innerHTML);
					routeArr.push($(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML);
					officeName.push($(this).parent().siblings('.office_name')[0].innerHTML);
					model.push($(this).parent().siblings('.arrival_model')[0].innerHTML);
				}
			}
		}else{
			if(spName.length != 0){
				spName.splice($(this).parent().siblings('.spname')[0].innerHTML, 1);
			}
			if(routeArr.length != 0){
				routeArr.splice($(this).parent().siblings('.route_from')[0].innerHTML + $(this).parent().siblings('.route_to')[0].innerHTML, 1);
			}
			if(officeName.length != 0){
				officeName.splice($(this).parent().siblings('.office_name')[0].innerHTML,1);
			}
			if(model.length != 0){
				model.splice($(this).parent().siblings('.arrival_model')[0].innerHTML,1);
			}
			
			if(spName.length == 0){
			
				$("#saveBtn").attr('disabled', true);
			}
		}
	});
});
