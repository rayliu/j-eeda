
$(document).ready(function() {
    $('#menu_status').addClass('active').find('ul').addClass('in');
    
    // 在途运输单列表
    $.post('/yh/departOrder/transferMilestone', function(data){
		var pickupOrderMilestoneTbody = $("#pickupOrderMilestoneTbody");
		pickupOrderMilestoneTbody.empty();
		for(var i = 0; i < data.milestones.length;i++)
		{
			pickupOrderMilestoneTbody.append("<tr><th>"+data.milestones[i].ORDER_NO+"</th><th>"+data.milestones[i].STATUS+"</th><th>"+data.milestones[i].LOCATION+"</th><th>"+data.milestones[i].USERNAMES+"</th><th>"+data.milestones[i].CREATE_STAMP+"</th></tr>");
		}
	},'json');  
} );