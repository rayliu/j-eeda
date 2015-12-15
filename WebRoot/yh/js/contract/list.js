$(document).ready(function() {
	document.title = '合同信息查询 | '+document.title;
	$('#menu_contract').addClass('active').find('ul').addClass('in');
	var isUpdate = false;
	var isDel = false;
	
  var type = $("#type").val();//注意这里
  var urlSource;
  var urlSource3;
	if(type=='CUSTOMER'){
		$("#btn1").show();
		urlSource="/customerContract/customerList";
		urlSource2="/customerContract/edit/";
		urlSource3="/customerContract/delete/";
		isUpdate = ContractCustomer.isUpdate;
		isDel = ContractCustomer.isDel;
	}if(type=='SERVICE_PROVIDER'){
		$("#btn2").show();
		urlSource="/spContract/spList";
		urlSource2="/spContract/edit/";
		urlSource3="/spContract/delete2/";
		isUpdate = ContractProvider.isUpdate;
		isDel = ContractProvider.isDel;
	}if(type=='DELIVERY_SERVICE_PROVIDER'){
		$("#btn3").show();
		urlSource="/deliverySpContract/deliveryspList";
		urlSource2="/deliverySpContract/edit/";
		urlSource3="/deliverySpContract/delete3/";
		isUpdate = ContranctDelivey.isUpdate;
		isDel = ContranctDelivey.isDel;
	}
    
	//datatable, 动态处理
   var tab2= $('#eeda-table').dataTable({
	   "bFilter": false, 
	   //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": urlSource,
        "aoColumns": [   
            {"mDataProp":"NAME",
              "fnRender": function(obj) { 
            	if(isUpdate){
            		 return "<a title='编辑' href='"+urlSource2+""+obj.aData.CID+"'target='_blank'>"+obj.aData.NAME+
                     "</a>";
            	}else{
            		return obj.aData.NAME;
            	}
               
              }
            },
            {"mDataProp":"COMPANY_NAME",
            	"sWidth": "15%"
            },
            {"mDataProp":"ABBR",
            	"fnRender":function(obj){
            		var str = "";
            		str = obj.aData.ABBR;
            		if(str == null){
            			str = "";
            		}
            		return str;
            	}},
            {"mDataProp":"CONTACT_PERSON"},
            {"mDataProp":"PHONE"},
            {"mDataProp":"PERIOD_FROM"},
            {"mDataProp":"PERIOD_TO"},
            { 
                "mDataProp": null, 
                "sWidth": "11%",  
                "bVisible":(isUpdate || isDel),
                "fnRender": function(obj) {     
                	var str ="<nobr>";
                	
                	if(isUpdate){
                		str +=  "<a class='btn   btn-primary btn-sm' title='编辑' href='"+urlSource2+""+obj.aData.CID+"'>"+
		                            "<i class='fa fa-edit fa-fw'></i>编辑"+
		                        "</a> ";
                	}
                	if(isDel){
                		if(obj.aData.C_IS_STOP != true){
		                    str += "<a class='btn btn-danger  btn-sm' href='"+urlSource3+""+obj.aData.CID+"'>"+
		                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
		                         "停用"+
		                         "</a>";
                		
	               	}else{
	               		str +="<a class='btn btn-success' href='"+urlSource3+""+obj.aData.CID+"'>"+
			                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                         "启用"+
			                     "</a>";
	               	}
            	}
                	return str +="</nobr>";     
                }
            }                         
        ],
     });
   
   //开始时间点击后隐藏
   $('#datetimepicker').datetimepicker({  
       format: 'yyyy-MM-dd',  
       language: 'zh-CN'
   }).on('changeDate', function(ev){
       $(".bootstrap-datetimepicker-widget").hide();
       $('#beginTime_filter').trigger('keyup');
       $("#periodFrom_filter").keyup();
   });
   //结束时间点击后隐藏
   $('#datetimepicker2').datetimepicker({  
       format: 'yyyy-MM-dd',  
       language: 'zh-CN', 
       autoclose: true,
       pickerPosition: "bottom-left"
   }).on('changeDate', function(ev){
       $(".bootstrap-datetimepicker-widget").hide();
       $('#endTime_filter').trigger('keyup');
       $("#periodTo_filter").keyup();
   });
    
    
  //获取供应商的list，选中信息在下方展示其他信息
    $('#companyName_filter').on('keyup click', function(){
		var inputStr = $('#companyName_filter').val();
		$.get('/customerContract/companyNameList', {input:inputStr,type:type}, function(data){
			console.log(data);
			var cpnameList =$("#cpnameList");
			cpnameList.empty();
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
				cpnameList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+"</a></li>");
			}
				/*cpnameList.append("<li><a tabindex='-1' class='fromLocationItem' >"+data[i].COMPANY+"</a></li>");
			}*/
		},'json');

		$("#cpnameList").css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        }); 
        $('#cpnameList').show();
	});

	// 没选中供应商，焦点离开，隐藏列表
	$('#companyName_filter').on('blur', function(){
 		$('#cpnameList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#cpnameList').on('blur', function(){
 		$('#cpnameList').hide();
 	});

	$('#cpnameList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});

	// 选中供应商
	$('#cpnameList').on('mousedown', '.fromLocationItem', function(e){
  		console.log($('#cpnameList').is(":focus"));
  		var message = $(this).text();
  		$('#companyName_filter').val(message);
      $('#cpnameList').hide();
      
  });
	
	//条件搜索>>,
  $("#searchBtn").on('click', function () {    	 	
    	search();
  });

  var search = function(){
    var contractName_filter = $("#contractName_filter").val();
    var contactPerson_filter = $("#contactPerson_filter").val();
    var periodFrom_filter = $("#periodFrom_filter").val();
    var companyName_filter = $("#companyName_filter").val();
    var phone_filter = $("#phone_filter").val();   
    var periodTo_filter = $("#periodTo_filter").val();
    
    if(type=='CUSTOMER'){
        tab2.fnSettings().sAjaxSource = "/customerContract/customerList?contractName_filter="+contractName_filter+"&contactPerson_filter="+contactPerson_filter+"&periodFrom_filter="+periodFrom_filter+"&companyName_filter="+companyName_filter+"&phone_filter="+phone_filter+"&periodTo_filter="+periodTo_filter;
    }if(type=='SERVICE_PROVIDER'){
      tab2.fnSettings().sAjaxSource = "/spContract/spList?contractName_filter="+contractName_filter+"&contactPerson_filter="+contactPerson_filter+"&periodFrom_filter="+periodFrom_filter+"&companyName_filter="+companyName_filter+"&phone_filter="+phone_filter+"&periodTo_filter="+periodTo_filter;
    }if(type=='DELIVERY_SERVICE_PROVIDER'){
      tab2.fnSettings().sAjaxSource = "/deliverySpContract/deliveryspList?contractName_filter="+contractName_filter+"&contactPerson_filter="+contactPerson_filter+"&periodFrom_filter="+periodFrom_filter+"&companyName_filter="+companyName_filter+"&phone_filter="+phone_filter+"&periodTo_filter="+periodTo_filter;
    }
      tab2.fnDraw();
  };
	
} );
