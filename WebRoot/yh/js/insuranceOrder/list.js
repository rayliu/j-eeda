 $(document).ready(function() {
	 
	    document.title = '保险单查询 | '+document.title;
		$('#menu_damage').addClass('active').find('ul').addClass('in');
    	
		var insuranceOrder = $('#dataTables-example').dataTable({
			"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
            "bFilter": false, //不需要默认的搜索框
	        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
	        "bServerSide": true,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/insuranceOrder/list",
	        "aoColumns": [   
			    {"mDataProp":"ORDER_NO",
	            	"fnRender": function(obj) {
	            		if(Insurance.isUpdate){
	            			return "<a href='/insuranceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
	            		}else{
	            			return obj.aData.ORDER_NO;
	            		}
	            			
	            }},
	            {"mDataProp":"COMPANY_NAME"},
	            {"mDataProp":"CUSTOMER"},
			    {"mDataProp":"STATUS"},   
			    {"mDataProp":"INSURANCE_AMOUNT"},  
			    {"mDataProp":"CHANGE_AMOUNT"},  
			    {"mDataProp":"PLANNING_TIME"},
			    {"mDataProp":"CREATE_STAMP"},
			    {"mDataProp":"DEPARTURE_TIME"},
			    {"mDataProp":"TRANSFER_ORDER_NO"},
	            { 
	                "mDataProp": null, 
	                "sWidth": "8%",   
	                "bVisible":false,
	                "fnRender": function(obj) {    
	                	if(false){
	                		return "<a class='btn btn-danger cancelbutton' code='"+obj.aData.ID+"'>"+
		                        "<i class='fa fa-trash-o fa-fw'></i>"+ 
		                        "取消"+
		                        "</a>";
	                	}else{
	                		return "";
	                	}
	                }
	            },
	    		{"mDataProp":"INSURANCE_NO"},
	        ]      
	    });	
        $("#dataTables-example").on('click', '.cancelbutton', function(e){
    		e.preventDefault();
           //异步向后台提交数据
    	   var id = $(this).attr('code');
    	   $.post('/insuranceOrder/cancel/'+id,function(data){
               //保存成功后，刷新列表
               console.log(data);
               if(data.success){
            	   insuranceOrder.fnDraw();
               }else{
                   alert('取消失败');
               }                   
           },'json');
		});
        //获取客户列表，自动填充
        $('#customer_filter').on('keyup click', function(event){
            var me = this;
            var inputStr = $('#customer_filter').val();
            var companyList =$("#companyList");
            $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
                companyList.empty();
                for(var i = 0; i < data.length; i++)
                    companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
                    
                companyList.css({ 
    		    	left:$(me).position().left+"px", 
    		    	top:$(me).position().top+32+"px" 
    		    });
    	        companyList.show();    
            },'json');
            /*if(inputStr=='')
            	transferOrder.fnFilter('', 2);*/
            
        });
        $('#companyList').on('click', '.fromLocationItem', function(e){        
            $('#customer_filter').val($(this).text());
            $("#companyList").hide();
            var companyId = $(this).attr('partyId');
            $('#customerId').val(companyId);
        	refreshData();
        });
        // 没选中客户，焦点离开，隐藏列表
        $('#customer_filter').on('blur', function(){
            $('#companyList').hide();
        });

        //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
        $('#customer_filter').on('blur', function(){
            $('#companyList').hide();
        });

        $('#companyList').on('mousedown', function(){
            return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
        });
        $('#endTime_filter, #beginTime_filter, #orderNo_filter ,#departNo_filter,#planning_end_filter,#planning_begin_filter').on('keyup click', function () {
        	refreshData();
        });
        var refreshData= function () {
			var orderNo = $("#orderNo_filter").val();
			var departNo_filter = $("#departNo_filter").val();
			var beginTime = $("#beginTime_filter").val();
			var customer = $("#customer_filter").val();
			var endTime = $("#endTime_filter").val();
			var planningEndTime = $("#planning_end_filter").val();
			var planningBeginTime = $("#planning_begin_filter").val();
			insuranceOrder.fnSettings().sAjaxSource = "/insuranceOrder/list?orderNo="+orderNo+"&departNo="+departNo_filter+"&beginTime="+beginTime+"&endTime="+endTime+"&customer="+customer+"&planningBeginTime="+planningBeginTime+"&planningEndTime="+planningEndTime;
			insuranceOrder.fnDraw();
		};
		
		$('#datetimepicker').datetimepicker({  
		    format: 'yyyy-MM-dd',  
		    language: 'zh-CN'
		}).on('changeDate', function(ev){
	        $(".bootstrap-datetimepicker-widget").hide();
		    $('#beginTime_filter').trigger('keyup');
		});	
		$('#datetimepicker3').datetimepicker({  
		    format: 'yyyy-MM-dd',  
		    language: 'zh-CN'
		}).on('changeDate', function(ev){
	        $(".bootstrap-datetimepicker-widget").hide();
		    $('#planning_end_filter').trigger('keyup');
		});	
		$('#datetimepicker4').datetimepicker({  
		    format: 'yyyy-MM-dd',  
		    language: 'zh-CN'
		}).on('changeDate', function(ev){
	        $(".bootstrap-datetimepicker-widget").hide();
		    $('#planning_begin_filter').trigger('keyup');
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
		$('#showBtn').on('keyup click', function(e){
            var customer = $('#customer_filter').val();
            var planningEndTime = $("#planning_end_filter").val();
			var planningBeginTime = $("#planning_begin_filter").val();
            if(customer==""){
            	alert("请筛选客户");
            	return;
            }
            $.post("/insuranceOrder/showCustomerAounmt", {customer:customer,planningEndTime:planningEndTime,planningBeginTime:planningBeginTime}, function(data){
            $("#amount").html(data.orders[0].SUM_AMOUNT);
            },'json');
            
        });
		
    });