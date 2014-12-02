
$(document).ready(function() {
    $('#menu_damage').addClass('active').find('ul').addClass('in');
    var names = [];
	//datatable, 动态处理
    var insuranceOrder = $('#eeda-table').dataTable({
    	"bSort": false, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/insuranceOrder/createList",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                    return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
                 }
            },
            { 
            	"mDataProp": "ORDER_NO",
            	"sClass": "order_no"
            },  		    
            { 
            	"mDataProp": "TOTAL_AMOUNT",
            	"sClass": "total_amount"
            },
            { 
            	"mDataProp": "STATUS",
            	"sClass": "status"
            },
            { 
            	"mDataProp": "CNAME",
            	"sClass": "cname"
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
            	"mDataProp": "START_CREATE_STAMP",
            	"sClass": "start_create_stamp"
            }, 
            {"mDataProp":"CARGO_NATURE",
            	"sClass": "cargo_nature",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "damageCargo"){
            			return "损坏货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return "ATM";
            		}else{
            			return "";
            		}}},                                      
    		{ 
            	"mDataProp": "SPNAME",
            	"sClass": "spname"
            },                                       
    		{ 
            	"mDataProp": "CREATE_BY",
            	"sClass": "create_by"
            },                                      
    		{ 
            	"mDataProp": "CREATE_STAMP",
            	"sClass": "create_stamp"
            },                                      
    		{ 
            	"mDataProp": "CUSTOMER_ORDER_NO",
            	"sClass": "customer_order_no"
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
        $('#insuranceOrder_message').val(tableArr);
        $('#createForm').submit();
    });
    function refresh(){
    	var orderNo = $("#orderNo_filter").val();
    	//var status = $("#status_filter").val();
    	//var address = $("#address_filter").val();
    	var customer = $("#customer_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var routeFrom = $("#routeFrom_filter").val();
    	var routeTo = $("#routeTo_filter").val();
    	//var orderType = $("#orderType_filter").val();
    	
    	
    	insuranceOrder.fnSettings().sAjaxSource = "/insuranceOrder/createList?orderNo="+orderNo+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo;
    	insuranceOrder.fnDraw(); 
    };
    $('#orderNo_filter,  #customer_filter, #beginTime_filter, #endTime_filter, #routeTo_filter, #routeFrom_filter').on( 'keyup click', function () {
    	refresh();
    } );
 
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
    
	$("#eeda-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
			$("#saveBtn").attr('disabled', false);
		}else{
			if(names.length == 0){
				$("#saveBtn").attr('disabled', true);				
			}
		}
	});
	
	var sumValue = function(){
		var sumWeightVal = 0;
		var sumVolumnVal = 0;
		$("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		sumWeightVal = sumWeightVal + parseFloat($(this).parent().siblings('.total_weight')[0].textContent == "" ? 0 : $(this).parent().siblings('.total_weight')[0].textContent);
        		sumVolumnVal = sumVolumnVal + parseFloat($(this).parent().siblings('.total_volume')[0].textContent == "" ? 0 : $(this).parent().siblings('.total_volume')[0].textContent);
        	}
        });
		$("#sumWeight").text(sumWeightVal);	
		$("#sumVolume").text(sumVolumnVal);
	};
	
	// 选中或取消事件
	$("#transferOrderList").on('click', '.checkedOrUnchecked', function(){
		var ckeckedTransferOrderList = $("#ckeckedTransferOrderList");
		var order_no = $(this).parent().siblings('.order_no')[0].textContent;		
		var total_amount = $(this).parent().siblings('.total_amount')[0].textContent;		
		var status = $(this).parent().siblings('.status')[0].textContent;		
		var cname = $(this).parent().siblings('.cname')[0].textContent;		
		var route_from = $(this).parent().siblings('.route_from')[0].textContent;		
		var route_to = $(this).parent().siblings('.route_to')[0].textContent;		
		var start_create_stamp = $(this).parent().siblings('.start_create_stamp')[0].textContent;		
		var cargo_nature = $(this).parent().siblings('.cargo_nature')[0].textContent;		
		var spname = $(this).parent().siblings('.spname')[0].textContent;		
		var create_by = $(this).parent().siblings('.create_by')[0].textContent;		
		var create_stamp = $(this).parent().siblings('.create_stamp')[0].textContent;		
		var customer_order_no = $(this).parent().siblings('.customer_order_no')[0].textContent;		
		if($(this).prop('checked') == true){
			if(names.length != 0){
				if(names[0] != $(this).parent().siblings('.cname')[0].innerHTML){
					alert("请选择同一客户的订单!");
					return false;
				}else{
					names.push($(this).parent().siblings('.cname')[0].innerHTML);
				}
			}else{
				if($(this).parent().siblings('.cname')[0].innerHTML != ''){
					names.push($(this).parent().siblings('.cname')[0].innerHTML);
				}
			}
			ckeckedTransferOrderList.append("<tr value='"+$(this).val()+"'><td>"+order_no+"</td><td>"+total_amount+"</td><td>"+status+"</td><td>"+cname+"</td><td>"+route_from+"</td><td>"+route_to+"</td><td>"
					+start_create_stamp+"</td><td>"+cargo_nature+"</td><td>"+spname+"</td><td>"+create_by+"</td><td>"+create_stamp+"</td><td>"+customer_order_no+"</td></tr>");			
		}else{
			var allTrs = ckeckedTransferOrderList.children();
			for(var i=0;i<allTrs.length;i++){
				if(allTrs[i].attributes[0].value == $(this).val()){
					allTrs[i].remove();
				}
			}
			if(names.length != 0){
				names.splice($(this).parent().siblings('.cname')[0].innerHTML, 1);
			}
		}
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
	        var inputStr = $('#customer_filter').val();
	        if(inputStr!=null){
	        	refresh();
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
} );
