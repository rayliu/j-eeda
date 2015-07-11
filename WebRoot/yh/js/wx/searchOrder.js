		

  var clearViewData = function(){
	 $("#order_no").text("");
	 $("#order_status").text("");
	 $("#order_type").text("");
	 $("#customer_name").text("");
	 $("#sp_name").text("");
	 $("#beginCity").text("");
	 $("#endCity").text("");
  };
	
  
  //微信运输单，配送单查询
  $("#searchNo").click(function(e){
  	e.preventDefault();
  	var serialNo = $("#serialNo").val();
  	if(serialNo.trim() != ''){
  		//异步向后台提交数据
		$.post('/wx/findOrderNo', {serialNo:serialNo}, function(data){
			 if(data && (data.ORDER_NO == null || data.ORDER_NO == '')){
				 clearViewData();
				 $("#value").hide();
				 $("#error").text("查无此单号信息，请确认单号是否存在再查询！");
			 }else{
				 $("#error").hide();
				 $("#value").show();
				 $("#order_no").text(data.ORDER_NO);
					if(data.ORDER_TYPE != null && data.ORDER_TYPE != ''){
						if(data.ORDER_TYPE  == "salesOrder")
							$("#order_type").text("销售订单");
			    		else if(data.ORDER_TYPE  == "replenishmentOrder")
			    			$("#order_type").text("补货订单");
			    		else if(data.ORDER_TYPE  == "arrangementOrder")
			    			$("#order_type").text("调拨订单");
			    		else if(data.ORDER_TYPE  == "cargoReturnOrder")
			    			$("#order_type").text("退货订单");
			    		else if(data.ORDER_TYPE  == "damageReturnOrder")
			    			$("#order_type").text("质量退单");
			    		else if(data.ORDER_TYPE  == "gateOutTransferOrder")
			    			$("#order_type").text("出库运输单");
			    		else if(data.ORDER_TYPE  == "movesOrder")
			    			$("#order_type").text("移机单");
			    		else
			    			$("#order_type").text(data.ORDER_TYPE);
					}
					$("#order_status").text(data.STATUS ||'');
					$("#customer_name").text(data.CUSTOMER_NAME ||'');
					$("#sp_name").text(data.SP_NAME ||'');
					$("#beginCity").text(data.BEGINCITY ||'');
					$("#endCity").text(data.ENDCITY ||'');
			 }
		},'json');
  	}else{
  		clearViewData();
  		$("#error").text("请输入单号！");
  		$("#value").hide();
  	}
    
  });
	