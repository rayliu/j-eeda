$(document).ready(function() {
    $('#menu_cost').addClass('active').find('ul').addClass('in');
   
	  //datatable, 动态处理
    var chargeConfiremTable = $('#costConfirem-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/costConfiremList/list",
        "aoColumns": [ 
            {"mDataProp":"ORDER_NO"},  
            {"mDataProp":null},
		    {"mDataProp":null, "sWidth":"120px",
                "fnRender": function(obj) {
                    return "未收款";
            }},
            {"mDataProp":"STATUS", "sWidth":"200px"},
            {"mDataProp":"TRANSACTION_STATUS", "sWidth":"150px"},
            {"mDataProp":null, "sWidth":"200px"},
            {"mDataProp":null, "sWidth":"200px"},
            /*{"mDataProp":null, "sWidth":"200px"},*/     	
            {"mDataProp":null, "sWidth": "120px", 
                "fnRender": function(obj) {
                    if(obj.aData.TRANSACTION_STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.TRANSACTION_STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.TRANSACTION_STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.TRANSACTION_STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.TRANSACTION_STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.TRANSACTION_STATUS;
                }
            },           
            {"mDataProp":null, "sWidth":"150px"},        	
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":"AMOUNT", "sWidth":"150px"},                        
            {"mDataProp":"VOLUME", "sWidth":"150px"},                        
            {"mDataProp":"WEIGHT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":null, "sWidth":"150px"},                        
            {"mDataProp":"CREATOR", "sWidth":"150px"},                   
            {"mDataProp":"CREATE_STAMP", "sWidth":"150px"}                     
        ]      
    });	
    
    $("#chargeConfiremBtn").click(function(e){
        e.preventDefault();
    	var trArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		trArr.push($(this).val());
        	}
        });     
        console.log(trArr);
        var returnOrderIds = trArr.join(",");
        $.post("/yh/chargeConfiremList/chargeConfiremReturnOrder", {returnOrderIds:returnOrderIds}, function(data){
        	if(data.success){
        		chargeConfiremTable.fnSettings().sAjaxSource = "/yh/chargeConfiremList/list";
        		chargeConfiremTable.fnDraw(); 
        	}
        },'json');
    });
    
    /*--------------------------------------------------------------------*/
    //获取所有客户
    $('#customer_filter').on('keyup click', function(){
           var inputStr = $('#customer_filter').val();
           
           $.get("/yh/customerContract/search", {locationName:inputStr}, function(data){
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
           var companyId = $(this).attr('partyId');
           $('#customerId').val(companyId);
           //过滤回单列表
           //chargeCheckTable.fnFilter(companyId, 2);
           
           
           
           //获取所有的条件
           var inputStr = $('#customer_filter').val();
           
           
           if(inputStr!=null){
        	   /*
                * 
                * 
                * datatable.fnSettings().sAjaxSource = "/yh/chargeCheckOrder/edit";
              	* datatable.fnDraw(); 
                * */
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
       		$.get('/yh/transferOrder/searchSp', {input:inputStr}, function(data){
       			console.log(data);
       			var spList =$("#spList");
       			spList.empty();
       			for(var i = 0; i < data.length; i++)
       			{
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
       				spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+" "+contact_person+" "+phone+"</a></li>");
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
       		var pageSpName = $("#pageSpName");
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
               //获取到所有的条件，
               /*
                * 
                * 
                * datatable.fnSettings().sAjaxSource = "/yh/chargeCheckOrder/edit";
              	* datatable.fnDraw(); 
                * */
               
               
           });
       
       
       
       /*=========================================================*/
       //过滤客户
       /*   $('#companyName,#beginTime_filter,#endTime_filter,#beginTime,#endTime').on( 'keyup', function () {
         	
         	var companyName = $('#companyName').val();
     		var beginTime = $("#beginTime_filter").val();
     		var endTime = $("#endTime_filter").val();
     		var receiptBegin = $("#beginTime").val();
     		var receiptEnd = $("#endTime").val();
     		console.log("rr"+companyName);
     		chargeCheckTable.fnSettings().sAjaxSource = "/yh/chargeCheckOrder/createList?companyName="+companyName+"&beginTime="+beginTime+"&endTime="+endTime+"&receiptBegin="+receiptBegin+"&receiptEnd="+receiptEnd;
     	
     		chargeCheckTable.fnDraw();
     	} );
  */
} );