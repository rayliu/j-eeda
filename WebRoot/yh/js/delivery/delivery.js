
$(document).ready(function() {
		    $('#eeda-table').dataTable();
		    
		    var customer =$("#customer");
		    customer.empty();
			  $.get('/yh/delivery/selectCustomer',{},function(data){
				  for(var i = 0; i < data.length; i++)
			      {
					  customer.append("<div class='radio'><label><input type='radio' name='customer' id='"+data[i].ID+"' value='"+data[i].ID+"' randonly='1'>"+data[i].COMPANY_NAME+" - "+data[i].CONTACT_PERSON+" - "+data[i].ADDRESS+" - "+data[i].MOBILE+" / "+data[i].PHONE +"</label></div>");        
			      }
			    },'json');
		  // 动态显示客户(单选按钮)
		  var sp =$("#sp");
		  sp.empty();
		  $.get('/yh/delivery/selectSp',{},function(data){
		      for(var i = 0; i < data.length; i++)
		      {
		        sp.append("<div class='radio'><label><input type='radio' name='sp' id='"+data[i].ID+"' value='"+data[i].ID+"' checked=''>"+data[i].ID+" - "+data[i].COMPANY_NAME+" - "+data[i].CONTACT_PERSON+" - "+data[i].ADDRESS+" - "+data[i].MOBILE+" / "+data[i].PHONE +"</label></div>");        
		      }
		      sp.append("<div class='radio'><label><input type='radio' name='sp' id='selectNewCustomer' value='' checked=''>以上选项没有我需要的, 选择新客户:<input id='company_name' name='company_name' type='text'></label></div>");
		      sp.append("<div class='radio'><label><input type='radio' name='sp' id='newCustomer' value='' checked=''>以上选项没有我需要的, 创建新客户</label></div>");
		    },'json');
		  
		  var tab =$("#tab");
		  tab.empty();
		  $.get('/yh/delivery/selectSp',{},function(data){
		      for(var i = 0; i < data.length; i++)
		      {
		        tab.append("<tr><td>"+data[i].ID+"</td><td>"+data[i].COMPANY_NAME+"</td><td>"+data[i].CONTACT_PERSON+"</td><td>"+data[i].ADDRESS+"</td><td>"+data[i].PHONE+"</td></tr>");        
		      }
		    },'json');
		  
		  //单击选中单选按钮,触发事件,控制form的显示与隐藏
		  $("#customers").on('click', 'input', function(){
		    console.log(this);
		    var inputId  = $(this).attr('id');
		    
		    if(inputId=='newCustomer'){
		        $("#customerFormDiv").show();
		    }else{
		        $("#customerFormDiv").hide();
		    }
		    
		  });
		  
		  //点击保存的事件，保存客户信息
		//customerForm 不需要提交
		$("#saveCustomerBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		$.post('/yh/transferOrder/saveCustomer', $("#customerForm").serialize(), function(data){
		//保存成功后，刷新列表
		console.log(data);
		if(data.success){
		  dataTable.fnDraw();
		}else{
		alert('数据保存失败。');
		}
		},'json');
		});
		  // 动态显示联系人(单选按钮)
		  var contact =$("#contacts");
		  contact.empty();
		  $.get('/yh/transferOrder/selectContact',{},function(data){
		      for(var i = 0; i < data.length; i++)
		      {
		        contact.append("<input id='"+data[i].ID+"' type='radio' name='contact' />"+data[i].ID+" - "+data[i].COMPANY_NAME+" - "+data[i].CONTACT_PERSON+" - "+data[i].ADDRESS+" - "+data[i].MOBILE+" / "+data[i].PHONE +"</br>");
		      }
		      contact.append("<input id='newCustomer' type='radio' name='contact' />"+"使用新地址");
		    },'json');
		  
		  // 单击选中单选按钮,触发事件,控制form的显示与隐藏
		  $("#contacts").click(function(){
		    var ins = $("#contacts input[id]");
		    for(var i=0;i<ins.length;i++){
		      if(i == ins.length-1){
		                $("#contactFormDiv").show();
		      }else{
		        $("#contactFormDiv").hide();
		      }
		    }
		  });
		  
		  // 当货品属性位ATM时,增加ATM的序列号
		  $("#cargo_nature_atm").click(function(){
		    $("#label_item_no").show();
		    $("#item_no").show();
		  });
});
