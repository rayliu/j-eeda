
$(document).ready(function() {
    $('#menu_status').addClass('active').find('ul').addClass('in');
    
    // 在途运输单列表
    $.post('/pickupOrder/pickupOrderMilestoneList', function(data){
		var pickupOrderMilestoneTbody = $("#pickupOrderMilestoneTbody");
		pickupOrderMilestoneTbody.empty();
		for(var i = 0,j = 0; i < data.milestones.length,j < data.usernames.length; i++,j++)
		{
			pickupOrderMilestoneTbody.append("<tr><th>"+data.milestones[i].DEPART_NO+"</th><th>"+data.milestones[i].STATUS+"</th><th>"+data.milestones[i].LOCATION+"</th><th>"+data.usernames[j]+"</th><th>"+data.milestones[i].CREATE_STAMP+"</th></tr>");
		}
	},'json');  
} );
