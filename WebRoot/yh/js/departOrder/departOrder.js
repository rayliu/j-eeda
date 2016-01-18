
$(document).ready(function() {
    $('#menu_assign').addClass('active').find('ul').addClass('in'); 
    /*var spName = [];
    var routeArr = [];
    var chargeType2 = [];
    var officeName = [];
    var model=[];
    var pickupIds = [];
    */
    
    //选取方法二
    //供应商、计费方式、网点、到达方式、始发地、目的地
    var customer = "";
    var spName = "";
    var chargeType2 = "";
    var officeName = "";
    var arrivalModel = "";
    var routeFrom = "";
    var routeTo = "";
    var transferOrderIds = [];
    
    //datatable, 动态处理
    $('#eeda-table1').dataTable({
    	"bSort": false, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": false,
        "bProcessing":false,
        "bInfo":false,
        "bPaginate":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "aoColumns": [
            { "mDataProp": "ORDER_NO","sClass": "order_no"},
            { "mDataProp": "PICKUP_NO","sClass": "pickup_no"},
            /*{ "mDataProp": "CUSTOMER_ORDER_NO","sClass": "customer_order_no"},
            { "mDataProp": "PLANNING_TIME","sClass": "planning_time"},*/
            { "mDataProp": "CNAME", "sWidth": "100px","sClass": "cname"},
            { "mDataProp": "OPERATION_TYPE","sClass": "operation_type"}, 
            { "mDataProp": "CARGO_NATURE","sClass": "cargo_nature"},
            { "mDataProp": "TOTAL_AMOUNT", "sClass": "total_amount"},
            { "mDataProp": "DOADDRESS","sClass": "doaddress"},
            { "mDataProp": "ARRIVAL_MODE","sClass": "arrival_model"},
            { "mDataProp": "PICKUP_MODE","sClass": "pickup_mode"},
            { "mDataProp": "SPNAME","sWidth": "100px","sClass": "spname"},
            { "mDataProp": "ROUTE_FROM","sClass": "route_from"},
            { "mDataProp": "ROUTE_TO","sClass": "route_to"},
            { "mDataProp": "CHARGE_TYPE2","sClass": "chargeType2"},
            { "mDataProp": "OFFICE_NAME","sClass": "office_name"}
        ]      
    });	
    
	//datatable, 动态处理
    var datatable = $('#eeda-table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bFilter": false, //不需要默认的搜索框
    	//"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/departOrder/createTransferOrderList",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                	 var result = false;
					 for ( var i = 0; i < transferOrderIds.length; i++) {
						 if(obj.aData.ID == transferOrderIds[i]){
							 result = true;
							 break;
						 }
					 }
					 if(result){
						 return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'" pickupid="'+obj.aData.PICKUP_ID+'" checked="checked">';
					 }else{
						 return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'" pickupid="'+obj.aData.PICKUP_ID+'"">';
					 }
                 }
            },
            { "mDataProp": "ORDER_NO","sClass": "order_no"},
            { "mDataProp": "PICKUP_NO","sClass": "pickup_no"},
            { "mDataProp": "CUSTOMER_ORDER_NO","sClass": "customer_order_no"},
            { "mDataProp": "PLANNING_TIME","sClass": "planning_time"},
            { "mDataProp": "ROUTE_TO","sClass": "route_to"},
            { "mDataProp": "CNAME", "sWidth": "100px","sClass": "cname"},
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
    			"sClass": "cargo_nature",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return allTransfer.ex_cargo;
            		}else{
            			return "";
            		}}},
            { "mDataProp": "TOTAL_AMOUNT", "sClass": "total_amount"},
//            	,
//    			"sClass": "total_amount",
//            	"fnRender": function(obj) {
//            		if(obj.aData.CARGO_NATURE == "ATM"){
//            			return obj.aData.TOTAL_AMOUNT;
//            		}else 
//            			return obj.aData.CARGO_AMOUNT;
//            		}
//            },
 //           { "mDataProp": "TOTAL_AMOUNT2", "sClass": "total_amount2"},		//件数
            { "mDataProp": "DOADDRESS","sClass": "doaddress"},
            { "mDataProp": "ARRIVAL_MODE",
            	"sClass": "arrival_model",
            	"fnRender": function(obj) {
            		if(obj.aData.ARRIVAL_MODE=="delivery"){
            			return "直送";
            		}else{
            			return "入中转仓";
            		}
            	},
            },
            {"mDataProp":"PICKUP_MODE",
            	"sClass": "pickup_mode",
            	"fnRender": function(obj) {
            		if(obj.aData.PICKUP_MODE == "routeSP"){
            			return "干线供应商自提";
            		}else if(obj.aData.PICKUP_MODE == "pickupSP"){
            			return "外包供应商提货";
            		}else if(obj.aData.PICKUP_MODE == "own"){
            				return allTransfer.ex_type;
            		}else{
            			return "";
            		}}},
            {"mDataProp": "SPNAME","sWidth": "100px","sClass": "spname"},
            {"mDataProp": "ROUTE_FROM","sClass": "route_from" },
            {"mDataProp":"CHARGE_TYPE",
            	"sClass": "chargeType2",
            	"fnRender":function(obj){
            		if(obj.aData.CHARGE_TYPE == "perUnit"){
    					return "计件";
    				}else if(obj.aData.CHARGE_TYPE == "perCar"){
    					return "整车";
    				}else if(obj.aData.CHARGE_TYPE == "perCargo"){
    					return "零担";
    				}else{
    					return "";
    				}
            	}
            },
            {"mDataProp": "OFFICE_NAME","sClass": "office_name"}
        ]      
    });	
   
    $('#saveBtn').click(function(e){
        e.preventDefault();
        var tableArr=[];

		$("#ckeckedTransferOrderList tr").each(function (){
			var pickup = $(this).attr("pickupid");
			if (pickup==''|| pickup==null){
				pickup = 'wu';
			}
			
			tableArr.push($(this).attr("value")+':'+ pickup);
		});

        $('#pickupOrder_message').val(tableArr);
        $('#pickupIds').val(pickupIds);
        $('#createForm').submit();
    });
    
    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter').trigger('keyup');
    });
    $("#searchBtn").click(function(){
        saveConditions();
        findData();
    });
    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
        saveConditions();
        findData();
    });
    var saveConditions=function(){
        var conditions={
            order_no:$("#orderNo_filter").val(),
            status:$("#status_filter").val(),
    		address:$("#address_filter").val(),
    		customer:$("#customer_filter").val(),
    		beginTime:$("#beginTime_filter").val(),
    		endTime:$("#endTime_filter").val(),
    		routeFrom:$("#routeFrom_filter").val(),
    		routeTo:$("#routeTo_filter").val()
        }
        if(!!window.localStorage){//查询条件处理
            localStorage.setItem("query_depart_order_list", JSON.stringify(conditions));
        }
    };
    var loadConditions=function(){
        if(!!window.localStorage){//查询条件处理
            var query_to = localStorage.getItem("query_depart_order_list");
            if(!query_to)
                return;

            var conditions = JSON.parse(localStorage.getItem("query_depart_order_list"));
            $("#orderNo_filter").val(conditions.order_no);
            $("#status_filter").val(conditions.status);
            $("#address_filter").val(conditions.address);
            $("#customer_filter").val(conditions.customer);
            $("#beginTime_filter").val(conditions.beginTime);
            $("#endTime_filter").val(conditions.endTime);
            $("#routeFrom_filter").val(conditions.routeFrom);
            $("#routeTo_filter").val(conditions.routeTo);
        }
    };
    var findData = function(){    	 	
    	var orderNo = $("#orderNo_filter").val();
    	var status = $("#status_filter").val();
    	var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var routeFrom = $("#routeFrom_filter").val();
    	var routeTo = $("#routeTo_filter").val();
    	datatable.fnSettings().sAjaxSource = "/departOrder/createTransferOrderList?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo;
    	datatable.fnDraw();
    	saveConditions();
      };
      loadConditions();
    
    //选取运输单
    $("#eeda-table").on('click', '.checkedOrUnchecked', function(e){
    	var value = $(this).val();
		var pickupid = $(this).attr("pickupid");
    	var ckeckedTransferOrderList = $("#ckeckedTransferOrderList");
		var order_no = $(this).parent().siblings('.order_no')[0].textContent;		
		var pickup_no = $(this).parent().siblings('.pickup_no')[0].textContent;	
		var cname = $(this).parent().siblings('.cname')[0].textContent;
		var operation_type = $(this).parent().siblings('.operation_type')[0].textContent;		
		var cargo_nature = $(this).parent().siblings('.cargo_nature')[0].textContent;
		var total_amount = $(this).parent().siblings('.total_amount')[0].textContent;	
		var doaddress = $(this).parent().siblings('.doaddress')[0].textContent;		
		var arrival_model = $(this).parent().siblings('.arrival_model')[0].textContent;		
		var pickup_mode = $(this).parent().siblings('.pickup_mode')[0].textContent;		
		var spname = $(this).parent().siblings('.spname')[0].textContent;		
		var route_from = $(this).parent().siblings('.route_from')[0].textContent;		
		var route_to = $(this).parent().siblings('.route_to')[0].textContent;		
		var charge_type = $(this).parent().siblings('.chargeType2')[0].textContent;		
		var office_name = $(this).parent().siblings('.office_name')[0].textContent;		
    	if($(this).prop("checked") == true){
    		if(total_amount == null || total_amount == ""){
				alert("货品数量不能为空!");
				return false;
			}
    		$("#saveBtn").attr('disabled', false);
    		if(transferOrderIds.length == 0){
    			customer = cname;
    			spName = spname;
    			chargeType2 = charge_type;
    			officeName = office_name;
    			arrivalModel = arrival_model;
    			routeFrom = route_from;
    			routeTo = route_to;
    			transferOrderIds.push($(this).val());
    			ckeckedTransferOrderList.empty();
    		}else{
    			if(spName != spname && spname != ''&&spName!=''){
    				alert("请选择同一供应商!");
    				return false;
    			}else if(chargeType2 != charge_type){
    				alert("请选择同一计费方式!");
    				return false;
    			}else if(routeFrom != route_from && route_from != ''){
					alert("请选择同一线路的运输单!");
					return false;
    			}else if(routeTo != route_to && route_to != ''){
					alert("请选择同一线路的运输单!");
					return false;
				}else if(officeName != office_name){
					alert("请选择同一网点的运输单！");
					return false;
				}else if(arrivalModel != arrival_model){
					alert("请选择同一到达方式的运输单");
					return false;
				}else{
					if(arrivalModel == "直送"){
						if(customer != cname){
		    				alert("请选择同一客户!");
		    				return false;
		    			}else{
		    				transferOrderIds.push($(this).val());
		    			}
					}else{
						transferOrderIds.push($(this).val());
					}
				}
    		}
    		ckeckedTransferOrderList.append("<tr value='"+value+"' pickupid='"+pickupid+"'><td>"+order_no+"</td><td>"+pickup_no+"</td><td>"+cname+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"+operation_type+"</td><td>"+cargo_nature
    				+"</td><td>"+total_amount+"</td><td>"+doaddress+"</td><td>"+arrival_model+"</td><td>"+pickup_mode+"</td><td>"+spname+"</td><td>"+charge_type+"</td><td>"+office_name+"</td></tr>");
    	}else{
    		if(transferOrderIds.length != 0){
    			transferOrderIds.splice(transferOrderIds.indexOf(value), 1); 
    			var allTrs = ckeckedTransferOrderList.children();
    			for(var i=0;i<allTrs.length;i++){
    				if(allTrs[i].attributes[0].value == value){
    					allTrs[i].remove();
    				}
    			}
    		}
    		if(transferOrderIds.length == 0){
    			$("#saveBtn").attr('disabled', true);
    			customer = "";
    			spName = "";
    			chargeType2 = "";
    			officeName = "";
    			arrivalModel = "";
    			routeFrom = "";
    			routeTo = "";
    		}
    	}
    	//console.log("单号："+transferOrderIds);
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

           if(inputStr==''){
        	   datatable.fnFilter('', 2);
           }
           
       });




   //选中某个客户时候
      $('#companyList').on('click', '.fromLocationItem', function(e){        
           $('#customer_filter').val($(this).text());
           $("#companyList").hide();
          /* var companyId = $(this).attr('partyId');
           $('#customerId').val(companyId);*/
           //过滤回单列表
           //chargeCheckTable.fnFilter(companyId, 2);
           var inputStr = $('#customer_filter').val();
           if(inputStr!=null){
        	   var orderNo = $("#orderNo_filter").val();
	           	var status = $("#status_filter").val();
	           	var address = $("#address_filter").val();
	           	var customer = $("#customer_filter").val();
	           	var beginTime = $("#beginTime_filter").val();
	           	var endTime = $("#endTime_filter").val();
	           	var routeFrom = $("#routeFrom_filter").val();
	           	var routeTo = $("#routeTo_filter").val();
	           	datatable.fnSettings().sAjaxSource = "/departOrder/createTransferOrderList?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo;
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
});
