$(document).ready(function() {
	$('#menu_deliver').addClass('active').find('ul').addClass('in');
	 var dataTable =$('#eeda-table3').dataTable({
		 "bFilter": false, //不需要默认的搜索框
	        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	        "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/yh/delivery/deliveryList",
	        "aoColumns": [   
	            
	            {"mDataProp":"ORDER_NO",
	            	"fnRender": function(obj) {
         			return "<a href='/yh/delivery/edit/"+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
         		}
	            },
	            {"mDataProp":"CUSTOMER"},
	            {"mDataProp":"C2"},
	            {"mDataProp":"CREATE_STAMP"},
	            {"mDataProp":"STATUS"},
	            {"mDataProp":"TRANSFER_ORDER_NO"},
	            { 
	                "mDataProp": null, 
	                "sWidth": "5%",                
	                "fnRender": function(obj) {                    
	                    return "<a class='btn btn-danger cancelbutton' title='取消单据' code='"+obj.aData.ID+"'>"+
	                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
	                            "</a>";
	                }
	            }                         
	        ]      
	    });	
	    
		 $("#eeda-table3").on('click', '.cancelbutton', function(e){
			  e.preventDefault();
	         //异步向后台提交数据
			  var r=confirm("是否取消单据！");   
           if(r==true){
         	  var id = $(this).attr('code');
				$.post('/yh/delivery/cancel/'+id,function(data){
		                 //保存成功后，刷新列表
		                 console.log(data);
		                 if(data.success){
		                	 dataTable.fnDraw();
		                 }else{
		                     alert('取消失败');
		                 }
		             },'json');
			}else{
				return false;   
			}
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
	
  //条件筛选
	$("#orderNo_filter ,#transfer_filter ,#status_filter,#customer_filter,#sp_filter,#beginTime_filter,#endTime_filter,#warehouse").on('keyup click', function () {    	 	
      	var orderNo_filter = $("#orderNo_filter").val();
      	var transfer_filter = $("#transfer_filter").val();
    	var status_filter = $("#status_filter").val();
      	var customer_filter = $("#customer_filter").val();    	
      	var sp_filter = $("#sp_filter").val();
      	var beginTime_filter = $("#beginTime_filter").val();
      	var endTime_filter = $("#endTime_filter").val();
      	var warehouse = $("#warehouse").val();
      	dataTable.fnSettings().sAjaxSource = "/yh/delivery/deliveryList?orderNo_filter="+orderNo_filter+"&transfer_filter="+transfer_filter+"&status_filter="+status_filter+"&customer_filter="+customer_filter+"&sp_filter="+sp_filter+"&beginTime_filter="+beginTime_filter+"&endTime_filter="+endTime_filter+"&warehouse="+warehouse;
      	dataTable.fnDraw();
      });
});