$(function() {
    $('#side-menu').metisMenu();
});

//Loads the correct sidebar on window load,
//collapses the sidebar on window resize.
$(function() {
    $(window).bind("load resize", function() {
        if ($(this).width() < 768) {
            $('div.sidebar-collapse').addClass('collapse')
        } else {
            $('div.sidebar-collapse').removeClass('collapse')
        }
    })
});

$(document).ready(function() {
	

    var showSideBar=function(){
    	$('#left_side_bar').show();
		$('#page-wrapper').css('margin-left', '250px');
		$('#hide_menu_btn').css('left', '250px');
		$('#hide_menu_icon').removeClass('fa-angle-double-right');
	    $('#hide_menu_icon').addClass('fa-angle-double-left');
	    if(!!window.localStorage){
            localStorage.setItem("is_show_eeda_side_bar", true);
        }
    };

    var hideSideBar=function(){
    	$('#left_side_bar').hide();
		$('#page-wrapper').css('margin', '0');
		$('#hide_menu_btn').css('left', '0px');
		$('#hide_menu_icon').removeClass('fa-angle-double-left');
	    $('#hide_menu_icon').addClass('fa-angle-double-right');

	    if(!!window.localStorage){
            localStorage.setItem("is_show_eeda_side_bar", false);
        }
    };

    $('#hide_menu_btn').mouseenter(function() {
        $("#hide_menu_btn").css('background-color', 'rgb(231, 231, 231)');
    }).mouseleave(function() {
        $("#hide_menu_btn").css('background-color', 'rgb(241, 241, 241)');
    });

	$('#hide_menu_btn').click(function(){
		if ($('#left_side_bar:visible').length > 0){//the element is visible
			hideSideBar();
		}else{
			showSideBar();
		}
	});

	var loadSideBar=function(){
        if(!!window.localStorage){//查询条件处理
            var is_show_eeda_side_bar = localStorage.getItem('is_show_eeda_side_bar');
            if(is_show_eeda_side_bar=='false'){
				hideSideBar();
            }else{
            	showSideBar();
            }
        }
    };

    loadSideBar();
});