$(document).ready(function() {
$('#menu_assign').addClass('active').find('ul').addClass('in');
	 // 列出所有的司机
	 $('#driverMessage').on('keyup', function(){
 		var inputStr = $('#driverMessage').val();
 		if(inputStr == ""){
 			$('#driver_phone').val($(this).attr(""));
 		}
 		$.get('/yh/transferOrder/searchAllDriver', {input:inputStr}, function(data){
 			console.log(data);
 			var driverList = $("#driverList");
 			driverList.empty();
 			for(var i = 0; i < data.length; i++)
 			{
 				driverList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' cid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', > "+data[i].CONTACT_PERSON+" "+data[i].PHONE+"</a></li>");
 			}
 		},'json');
 		
 		$("#driverList").css({ 
         	left:$(this).position().left+"px", 
         	top:$(this).position().top+32+"px" 
         }); 
         $('#driverList').show();
	 });
	  	
 	 // 选中司机
 	 $('#driverList').on('click', '.fromLocationItem', function(e){	
 		   $("#driver_id").val($(this).attr('partyId'));
	  	   $('#driverMessage').val($(this).attr('CONTACT_PERSON'));
	  	   $('#driver_phone').val($(this).attr('phone'));
	       $('#driverList').hide();   
    }); 
	
	//显示货品table
	var datatable = $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "bServerSide": true,
        "bDestroy": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/departOrder/getIintDepartOrderItems?localArr="+message+"",
        "aoColumns": [
            { "mDataProp": "CUSTOMER" ,"sWidth": "100%"},
            { "mDataProp": "ORDER_NO" ,"sWidth": "30%"},      
            { "mDataProp": "ITEM_NO"},
            { "mDataProp": "ITEM_NAME"},
            { "mDataProp": "AMOUNT"},
            { "mDataProp": "VOLUME"},
            { "mDataProp": "WEIGHT"},
            { "mDataProp": "REMARK"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {                    
                    return "<a class='btn btn-success dateilEdit' code='?id="+obj.aData.ID+"'>"+
                                "<i class='fa fa-search fa-fw'></i>"+
                                "查看"+
                            "</a>"+					
                            "<a class='btn btn-danger cancelbutton' code='?id="+obj.aData.TR_ORDER_ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                },
            }
                                      
        ]      
    });	
	
	var tr_itemid_list=[];
	// 查看货品
		$("#eeda-table").on('click', '.dateilEdit', function(e){
			e.preventDefault();
			
			$("#transferOrderItemDateil").show();
			//alert("12345");
			 var code = $(this).attr('code');
			var itemId = code.substring(code.indexOf('=')+1);
			tr_itemid_list.push(itemId);
			$("#item_id").val(itemId);
			$("#item_save").attr("disabled", false);
			$("#style").hide();
			detailTable.fnSettings().sAjaxSource = "/yh/departOrder/itemDetailList?item_id="+itemId+"";
			detailTable.fnDraw();   
			
		});
		// 删除货品
		$("#eeda-table").on('click', '.cancelbutton', function(e){
			e.preventDefault();
			
			//$("#transferOrderItemDateil").show();
			//alert("12345");
			 var code = $(this).attr('code');
			var itemId = code.substring(code.indexOf('=')+1);
			var tr_order_id=message;
			tr_order_id=tr_order_id.replace(/,/g, "").replace(itemId, "");
			var	tr_order =[];
			for(var i=0;i<tr_order_id.length;i++){
			tr_order.push(tr_order_id[i]);	
			}
			message=tr_order.join(",");
			$("#message").val(tr_order.join(","));
			$("#item_id").val(itemId);
			datatable.fnSettings().sAjaxSource = "/yh/departOrder/getIintDepartOrderItems?localArr="+tr_order.join(",")+"";
			datatable.fnDraw();   
		});
		
		var item_id = $("#item_id").val();
		var detailTable= $('#detailTable').dataTable({           
            "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",  
            "iDisplayLength": 10,
            "bServerSide": true, 
        	"oLanguage": {
                "sUrl": "/eeda/dataTables.ch.txt"
            },
           "sAjaxSource": "/yh/departOrder/itemDetailList?item_id="+item_id+"",
            "aoColumns": [
                 { "mDataProp": null,
                   "fnRender": function(obj) {
                       return '<input type="checkbox" name="order_check_box" value="'+obj.aData.ID+'">';
                  }
                 },
                { "mDataProp": "ITEM_NAME"},      
                { "mDataProp": "ITEM_NO"},
                { "mDataProp": "SERIAL_NO"},
                { "mDataProp": "VOLUME"},
                { "mDataProp": "WEIGHT"},
                { "mDataProp": "REMARK"},
                                          
            ]  
        });	    
	    //选择单品保存
	    var item_detail_id=[];
	    $("#item_save").click(function(){
	    	
	    	 $("table tr:not(:first)").each(function(){
	    	        
	         	$("input:checked",this).each(function(){
	         		item_detail_id.push($(this).val());
	         	
	         	});          		
	         	}); 
	    	//alert(item_detail_id);
	    	$("#item_detail").val(item_detail_id);
	    	$("#tr_itemid_list").val(tr_itemid_list);
	    	$("#style").show();
	    	$("#item_save").attr("disabled", true);
	    	});

	    // 回显车长
	    var carSizeOption=$("#carsize>option");
	    var carSizeVal=$("#carSizeSelect").val();
	    for(var i=0;i<carSizeOption.length;i++){
	       var svalue=carSizeOption[i].text;
	       if(carSizeVal==svalue){
	    	   $("#carsize option[value='"+svalue+"']").attr("selected","selected");
	       }
	    }
	    
	    // 回显车型
	    var carTypeOption=$("#cartype>option");
	    var carTypeVal=$("#carTypeSelect").val();
	    for(var i=0;i<carTypeOption.length;i++){
	    	var svalue=carTypeOption[i].text;
	    	if(carTypeVal==svalue){
	    		$("#cartype option[value='"+svalue+"']").attr("selected","selected");
	    	}
	    }
} );
