
$(document).ready(function() {
    $('#menu_assign').addClass('active').find('ul').addClass('in');    
	//datatable, 动态处理
    var datatable = $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/yh/departOrder/createTransferOrderList",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                    return '<input type="checkbox" arrival='+obj.aData.ARRIVAL_MODE+'  name="order_check_box" code='+obj.aData.SPID+' class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
                 }
            },
            { "mDataProp": "ORDER_NO"},
            {"mDataProp":"CARGO_NATURE",
            	"fnRender": function(obj) {
            		if(obj.aData.CARGO_NATURE == "cargo"){
            			return "普通货品";
            		}else if(obj.aData.CARGO_NATURE == "ATM"){
            			return "ATM";
            		}else{
            			return "";
            		}}},
            { "mDataProp": "ADDRESS"},
            { "mDataProp": "ARRIVAL_MODE",
            	"fnRender": function(obj) {
            		if(obj.aData.ARRIVAL_MODE=="delivery"){
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
            			return "源鸿自提";
            		}else{
            			return "";
            		}}},
            { "mDataProp": "SPNAME"}
          
                                      
        ]      
    });	

    $("#saveBtn").click(function(e){
        e.preventDefault();
      var tableArr=[];
      var sp_idArr=[];
      var arrival_idArr=[];
        $("table tr:not(:first)").each(function(){
        	$("input:checked",this).each(function(){
        		var sp_id=$(this).attr("code");
        		var arrival=$(this).attr("arrival");
        		tableArr.push($(this).val());	
        			arrival_idArr.push(arrival);
        		if(sp_id!='null'){
        			sp_idArr.push(sp_id);
        		}
        		
        	});          		
        	}); 
        if(arrival_idArr.length>=2){
        	var j=$.inArray("直送",arrival_idArr);
        	var h=$.inArray("入中转仓",arrival_idArr);
        	if(j>=0&&h>=0){
        		alert("请选择同一到达方式的运输单！");
        		return;
        	}
        	if(j==0&&arrival_idArr.length>=2){
        		alert("每次只能选择一张直送的运输单！");
        		return;
        	}
        	
       
       if(sp_idArr.length>=2){
    	   for(var i=0;i<sp_idArr.length;i++){
    		   
    		   if(sp_idArr[i]!=sp_idArr[i+1]){
    			   alert("请选择同一供应商的运输单！");
    			   return;
    		   }
    		   if(i+2==sp_idArr.length){
				   break;
			   }
    			  
    	   }
       }
        }
        console.log(tableArr);
            $("#departOrder_message").val(tableArr);
            $("#createForm").submit();
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
    	datatable.fnSettings().sAjaxSource = "/yh/departOrder/createTransferOrderList?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&routeFrom="+routeFrom+"&beginTime="+beginTime+"&endTime="+endTime+"&routeTo="+routeTo;
    	datatable.fnDraw();
    	
      });
    
    $("#eeda-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
			$("#saveBtn").attr('disabled', false);
		}else{
			$("#saveBtn").attr('disabled', true);
		}
	});
});
