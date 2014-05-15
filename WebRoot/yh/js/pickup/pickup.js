
$(document).ready(function() {

    $('#menu_finance').addClass('active').find('ul').addClass('in');
    
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
        "sAjaxSource": "/yh/pickupOrder/createList",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                    return '<input type="checkbox" name="order_check_box" value="'+obj.aData.ID+'">';
                 }
            },
            { "mDataProp": "ORDER_NO"},
            { "mDataProp": "CARGO_NATURE"},
            { "mDataProp": "ADDRESS"},
            { "mDataProp": "PICKUP_MODE"},
            { "mDataProp": "STATUS"}
                                      
        ]      
    });	
    
    //获取客户的list，选中信息自动填写其他信息
    $('#companyName').on('keyup', function(){
        var inputStr = $('#companyName').val();
        
        $.get("/yh/customerContract/search", {locationName:inputStr}, function(data){
            console.log(data);
            var companyList =$("#companyList");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+"</a></li>");
            }
            companyList.show();
        },'json');

        if(inputStr==''){
            datatable.fnFilter('', 2);
        }
    });

    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#companyName').val($(this).text());
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId);
        //过滤回单列表
        datatable.fnFilter(companyId, 2);
    });

    $('#createBtn').click(function(e){
        e.preventDefault();
        //获取选取回单的ID, 放到数组中
        var chk_value =[];    
        $('input[name="order_check_box"]:checked').each(function(){    
           chk_value.push($(this).val());    
        }); 

        console.log(chk_value.length==0 ?'你还没有选择任何内容！':chk_value);

        $('#ids').val(chk_value);
        $('#createForm').submit();
    });
} );
