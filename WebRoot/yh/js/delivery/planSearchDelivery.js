
$(document).ready(function() {
	$('#menu_deliver').addClass('active').find('ul').addClass('in');
    
    var spName = [];
	
    //可选配送单, 动态处理
    $('#checkedDeliveryTbody').dataTable({
    	"bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": false,
        "bPaginate":false,
        "bProcessing":false,
        "bInfo":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "aoColumns": [   
            {"mDataProp":"ORDER_NO","sWidth": "13%","sClass": "order_no"},
            {"mDataProp":"CUSTOMER","sClass": "customer"},
            {"mDataProp":"C2","sClass": "c2","sClass": "c2"},
            {"mDataProp":"CREATE_STAMP","sWidth": "15%","sClass": "create_stamp"},
            {"mDataProp":"STATUS","sWidth": "8%","sClass": "status"},
            {"mDataProp":"TRANSFER_ORDER_NO","sWidth": "13%","sClass": "transfer_order_no"},
            {"mDataProp":"SERIAL_NO","sWidth": "10%","sClass": "serial_no"}
        ]      
    });	
    
	//可选配送单, 动态处理
    var delieverListTbody =$('#delieverListTbody').dataTable({
    	"bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "bLengthChange":false,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.SP_ID}); 
			return nRow;
		},
        "sAjaxSource": "/deliveryPlanOrder/deliveryList",
        "aoColumns": [   
			{ "mDataProp": null,
				"sWidth": "5%",
				"fnRender": function(obj) {
					return '<input type="checkbox" class="checkedOrUnchecked" name="order_check_box" value="'+obj.aData.ID+'">';
			    }
			},
            {"mDataProp":"ORDER_NO","sWidth": "13%","sClass": "order_no"},
            {"mDataProp":"CUSTOMER","sClass": "customer"},
            {"mDataProp":"C2","sClass": "c2","sClass": "c2"},
            {"mDataProp":"CREATE_STAMP","sWidth": "15%","sClass": "create_stamp"},
            {"mDataProp":"STATUS","sWidth": "8%","sClass": "status"},
            {"mDataProp":"TRANSFER_ORDER_NO","sWidth": "13%","sClass": "transfer_order_no"},
            {"mDataProp":"SERIAL_NO","sWidth": "10%","sClass": "serial_no"}
        ]      
    });	
    
    //条件筛选
	$("#orderNo_filter ,#transfer_filter ,#status_filter,#customer_filter,#sp_filter,#beginTime_filter,#endTime_filter,#warehouse,#serial_no").on('keyup click', function () {    	 	
      	var orderNo_filter = $("#orderNo_filter").val();
      	var transfer_filter = $("#transfer_filter").val();
    	var status_filter = $("#status_filter").val();
      	var customer_filter = $("#customer_filter").val();    	
      	var sp_filter = $("#sp_filter").val();
      	var serial_no = $("#serial_no").val();
      	delieverListTbody.fnSettings().sAjaxSource = "/deliveryPlanOrder/deliveryList?orderNo_filter="+orderNo_filter+"&transfer_filter="+transfer_filter+"&status_filter="+status_filter+"&customer_filter="+customer_filter+"&sp_filter="+sp_filter+"&serial_no="+serial_no;
      	delieverListTbody.fnDraw();
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
        	dataTable.fnFilter('', 2);
      	}
        
	 });
	 

	 //选中某个客户时候
	 $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).text());
        $("#companyList").hide();
        var inputStr = $('#customer_filter').val();
        if(inputStr!=null){
        	var orderNo_filter = $("#orderNo_filter").val();
          	var transfer_filter = $("#transfer_filter").val();
        	var status_filter = $("#status_filter").val();
          	var customer_filter = $("#customer_filter").val();    	
          	var sp_filter = $("#sp_filter").val();
          	var serial_no = $("#serial_no").val();
          	delieverListTbody.fnSettings().sAjaxSource = "/deliveryPlanOrder/deliveryList?orderNo_filter="+orderNo_filter+"&transfer_filter="+transfer_filter+"&status_filter="+status_filter+"&customer_filter="+customer_filter+"&sp_filter="+sp_filter+"&serial_no="+serial_no;
          	delieverListTbody.fnDraw();
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
	    
	    
	//获取供应商的list，选中信息在下方展示其他信息
	$('#sp_filter').on('keyup click', function(){
		
		var inputStr = $('#sp_filter').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/delivery/searchSp', {input:inputStr}, function(data){
			console.log(data);
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
				spList.append("<li><a tabindex='-1' class='fromLocationItem' spid='"+data[i].SP_ID+"' >"+company_name+"</a></li>");
			}
		},'json');

		$("#spList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#spList').show();
		
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
		console.log($('#spList').is(":focus"))
		var message = $(this).text();
		$('#sp_filter').val(message.substring(0, message.indexOf(" ")));
		$('#sp_id').val($(this).attr('partyId'));
		var pageSpName = $("#sp_filter");
		pageSpName.empty();
		var pageSpAddress = $("#pageSpAddress");
		pageSpAddress.empty();
		pageSpAddress.append($(this).attr('address'));
		var contact_person = $(this).attr('contact_person');
		if(contact_person == 'null'){
			contact_person = '';
		}
		pageSpName.append(contact_person+'&nbsp;');
		var phone = $(this).attr('phone');
		if(phone == 'null'){
			phone = '';
		}
		pageSpName.append(phone); 
		pageSpAddress.empty();
		var address = $(this).attr('address');
		if(address == 'null'){
			address = '';
		}
		pageSpAddress.append(address);
	    $('#spList').hide();
	    
	    var orderNo_filter = $("#orderNo_filter").val();
      	var transfer_filter = $("#transfer_filter").val();
    	var status_filter = $("#status_filter").val();
      	var customer_filter = $("#customer_filter").val();    	
      	var sp_filter = $("#sp_filter").val();
      	var serial_no = $("#serial_no").val();
      	delieverListTbody.fnSettings().sAjaxSource = "/deliveryPlanOrder/deliveryList?orderNo_filter="+orderNo_filter+"&transfer_filter="+transfer_filter+"&status_filter="+status_filter+"&customer_filter="+customer_filter+"&sp_filter="+sp_filter+"&serial_no="+serial_no;
      	delieverListTbody.fnDraw();
	});
    
	// 选中或取消事件
	$("#delieverListTbody").on('click', '.checkedOrUnchecked', function(){
		var ckeckedDeliveryList = $("#ckeckedDeliveryList");
		var order_no = $(this).parent().siblings('.order_no')[0].textContent;	
		var customer = $(this).parent().siblings('.customer')[0].textContent;	
		var c2 = $(this).parent().siblings('.c2')[0].textContent;	
		var create_stamp = $(this).parent().siblings('.create_stamp')[0].textContent;	
		var status = $(this).parent().siblings('.status')[0].textContent;	
		var transfer_order_no = $(this).parent().siblings('.transfer_order_no')[0].textContent;	
		var serial_no = $(this).parent().siblings('.serial_no')[0].textContent;	
		if($(this).prop('checked') == true){
			if(spName.length != 0){
				if(spName[0] != $(this).parent().siblings('.c2')[0].innerHTML){
					alert("请选择相同的供应商!");
					return false;
				}else{
					spName.push($(this).parent().siblings('.c2')[0].innerHTML);
					ckeckedDeliveryList.append("<tr value='"+$(this).val()+"'><td>"+order_no+"</td><td>"+customer+"</td><td>"+c2+"</td><td>"+create_stamp+"</td><td>"+status+"</td><td>"+transfer_order_no+"</td><td>"+serial_no+"</td></tr>");
				}
			}else{
				$("#spId").val($(this).parent().parent().attr("id"));
				spName.push($(this).parent().siblings('.c2')[0].innerHTML);
				ckeckedDeliveryList.empty().append("<tr value='"+$(this).val()+"'><td>"+order_no+"</td><td>"+customer+"</td><td>"+c2+"</td><td>"+create_stamp+"</td><td>"+status+"</td><td>"+transfer_order_no+"</td><td>"+serial_no+"</td></tr>");
			}
			$("#addBtn").prop("disabled",false);
		}else{
			var allTrs = ckeckedDeliveryList.children();
			for(var i=0;i<allTrs.length;i++){
				if(allTrs[i].attributes[0].value == $(this).val()){
					allTrs[i].remove();
				}
			}
			if(spName.length != 0){
				spName.splice($(this).parent().siblings('.c2')[0].innerHTML, 1);
			}
			if(spName.length == 0){
				$("#addBtn").prop("disabled",true);
				$("#spId").val("");
			}
		}
	});
	
	$('#addBtn').click(function(e){
        e.preventDefault();
    	var trArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		trArr.push($(this).val());
        	}
        });
        $('#deliveryOrderIds').val(trArr);
        $('#createForm').submit();
    });
});
