
$(document).ready(function() {

	$('#menu_deliver').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var detailTable = $('#eeda-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/delivery/deliveryMilestone",
        "aoColumns": [   
            {"mDataProp":null,
            	"fnRender": function(obj) {
            		 return "<a href='/delivery/edit/"+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
            		}
            },
            {"mDataProp":null,
                "fnRender": function(obj) {
                	if(obj.aData.STATUS==null){
                		obj.aData.STATUS="";
                	}
                	if(obj.aData.STATUS=='已签收'){
                		return obj.aData.STATUS;
                	}else{
                		return obj.aData.STATUS+"<a id='edit_status' del_id="+obj.aData.ID+" data-target='#transferOrderMilestone' data-toggle='modal'><i class='fa fa-pencil fa-fw'></i></a>";
                	}
                }
            },
            {"mDataProp":"CUSTOMER"},
            {"mDataProp":"C2"},
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"TRANSFER_ORDER_NO"},
            { 
                "mDataProp": null, 
                "fnRender": function(obj) {   
                	if(obj.aData.STATUS=='已签收'){
                		return "已送达";
                	}else{
                		return "<a class='btn btn-primary confirmDelivery' code='"+obj.aData.ID+"'>"+
                		"到达确认"+
                		"</a>";
                	}
                }
            }    
        ]  
    });	
    //签收完成
    $("#eeda-table").on('click', '.confirmDelivery', function(e){
    	var delivery_id =$(this).attr("code");
    	if(confirm("确定签收 吗？")){
    		$.post('/deliveryOrderMilestone/receipt',{delivery_id:delivery_id},function(data){
    			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
    			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
    			detailTable.fnDraw(); 
    		},'json');
        }
    });
    
    $("#eeda-table").on('click', '#edit_status', function(e){
    	e.preventDefault();	
    	var depart_id=$(this).attr("del_id");
    	$("#milestoneDepartId").val(depart_id);
    	$.post('/delivery/transferOrderMilestoneList',{departOrderId:depart_id},function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.empty();
			for(var i = 0,j = 0; i < data.transferOrderMilestones.length,j < data.usernames.length; i++,j++)
			{
				transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestones[i].STATUS+"</th><th>"+data.transferOrderMilestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.transferOrderMilestones[i].CREATE_STAMP+"</th></tr>");
			}
		},'json');
    	
    });
    
    // 保存新里程碑
	$("#transferOrderMilestoneFormBtn").click(function(){
		$.post('/delivery/saveTransferOrderMilestone',$("#transferOrderMilestoneForm").serialize(),function(data){
			var transferOrderMilestoneTbody = $("#transferOrderMilestoneTbody");
			transferOrderMilestoneTbody.append("<tr><th>"+data.transferOrderMilestone.STATUS+"</th><th>"+data.transferOrderMilestone.LOCATION+"</th><th>"+data.username+"</th><th>"+data.transferOrderMilestone.CREATE_STAMP+"</th></tr>");
			detailTable.fnSettings().sAjaxSource = "/delivery/deliveryMilestone";
			detailTable.fnDraw();  
		},'json');
		//$('#transferOrderMilestone').modal('hide');
	}); 
	
 
    
    $('#endTime_filter ,#beginTime_filter ,#sp_filter ,#deliveryNo_filter ,#customer_filter ,#transferorderNo_filter').on( 'keyup click', function () {
    	//console.log($("#sp_filter").val());
    	var deliveryNo = $("#deliveryNo_filter").val();
    	var customer = $("#customer_filter").val();
    	var transferorderNo = $("#transferorderNo_filter").val();
    	var sp = $("#sp_filter").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	detailTable.fnSettings().sAjaxSource = "/delivery/deliveryMilestone?deliveryNo="+deliveryNo+"&customer="+customer+"&transferorderNo="+transferorderNo+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime;
    	detailTable.fnDraw();
    } );
	
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
    /*----------------------------------------------------------------*/
    
    
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
        	   detailTable.fnFilter('', 2);
           }
           
       });
   //选中某个客户时候
      $('#companyList').on('click', '.fromLocationItem', function(e){        
           $('#customer_filter').val($(this).text());
           $("#companyList").hide();
         
           var inputStr = $('#customer_filter').val();
           if(inputStr!=null){
        	   var deliveryNo = $("#deliveryNo_filter").val();
	           	var customer = $("#customer_filter").val();
	           	var transferorderNo = $("#transferorderNo_filter").val();
	           	var sp = $("#sp_filter").val();
	           	var beginTime = $("#beginTime_filter").val();
	           	var endTime = $("#endTime_filter").val();
	           	detailTable.fnSettings().sAjaxSource = "/delivery/deliveryMilestone?deliveryNo="+deliveryNo+"&customer="+customer+"&transferorderNo="+transferorderNo+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime;
	           	detailTable.fnDraw();
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
       		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
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
       		//console.log($('#spList').is(":focus"));
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
               
           	var deliveryNo = $("#deliveryNo_filter").val();
	    	var customer = $("#customer_filter").val();
	    	var transferorderNo = $("#transferorderNo_filter").val();
	    	var sp = $("#sp_filter").val();
	    	var beginTime = $("#beginTime_filter").val();
	    	var endTime = $("#endTime_filter").val();
	    	console.log(sp);
    		detailTable.fnSettings().sAjaxSource = "/delivery/deliveryMilestone?deliveryNo="+deliveryNo+"&customer="+customer+"&transferorderNo="+transferorderNo+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime;
	    	detailTable.fnDraw();	
           });
       	
	        
} );