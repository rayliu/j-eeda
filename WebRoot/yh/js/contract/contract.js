$(document).ready(function() {
        var oTable = $('#dataTables-example').dataTable();

        $('#customerForm').validate({
            rules: {
              company_name: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
                required: true
              },
              name:{//form 中 name为必填
                required: true
              }
            },
            highlight: function(element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function(element) {
                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });
        
        //点击button显示添加合同干线
        $("#btn").click(function(){
        	$("#contractForm").show();
        });
        $("#cancel").click(function(){
        	$("#contractForm").hide();
        });
        //获取客户的list，选中信息自动填写其他信息
        $('#companyName').on('keyup', function(){
			var inputStr = $('#companyName').val();
			
			$.get('/yh/spContract/search', {locationName:inputStr}, function(data){
				console.log(data);
				var companyList =$("#companyList");
				companyList.empty();
				for(var i = 0; i < data.length; i++)
				{
					companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+"</a></li>");
				}
				companyList.show();
				
			},'json');
		});
	
		$('#companyList').on('click', '.fromLocationItem', function(e){
			//方法已经对了，只是没有取对值
			$('#companyName').val($(this).text());
        	$("#companyList").hide();
        	$('#name').val($(this).attr('contact_person'));
        	$('#address').val($(this).attr('address'));
        	$('#phone').val($(this).attr('phone'));
        	$('#post_code').val($(this).attr('post_code'));
        	$('#email').val($(this).attr('email'));
        });
    });