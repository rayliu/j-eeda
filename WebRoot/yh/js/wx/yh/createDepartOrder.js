$(document).ready(function() {
	
	var chargeType= $('#chargeType').val();
	if(chargeType=='整车'){
		$('#car_type_div').show();
		var car_type= $('#car_type').val();
		$('#car_type_input').val(car_type);
	}
	
	var limitation= $('#limitation').val();
	$('#arrival_time').val(getNewDay($.format.date(new Date(), "yyyy-MM-dd"),limitation));


	function getNewDay(dateTemp, days) {  
	    var dateTemp = dateTemp.split("-");  
	    var nDate = new Date(dateTemp[1] + '-' + dateTemp[2] + '-' + dateTemp[0]); //转换为MM-DD-YYYY格式    
	    var millSeconds = Math.abs(nDate) + (days * 24 * 60 * 60 * 1000);  
	    var rDate = new Date(millSeconds);  
	    var year = rDate.getFullYear();  
	    var month = rDate.getMonth() + 1;  
	    if (month < 10) month = "0" + month;  
	    var date = rDate.getDate();  
	    if (date < 10) date = "0" + date;  
	    return (year + "-" + month + "-" + date);  
	}  
	
	var checkSp = function(){
		var sp = $('#sp').val();
		if(sp == ''){
			$('#sp').val('没有找到相应的供应商');
			$('#sp').css("color","red");

			var $sp = $('#warn_sp');
			$sp.show();
			
			$('#saveBtn').hide();
			$('#returnBtn').show();
			return;
		}
	}
	checkSp();
	
	
	$('#saveBtn').click(function(){ 
		//非空校验
		if(!check())
			return;
		checkSp();
		
		$('#saveBtn').hide();
		$.post('/wx/departOrder/departOrderCreate',$('#orderForm').serialize(),function(data){
			if(data.ID>0){
				$('#returnBtn').show();
				var $toast = $('#toast');
	             if ($toast.css('display') != 'none') {
	                 return;
	             }
	
	             $toast.show();
	             setTimeout(function () {
	                 $toast.hide();
	             }, 2000);
			}else{
				var $toast = $('#error');
		         if ($toast.css('display') != 'none') {
		             return;
		         }

		         $toast.show();
		         setTimeout(function () {
		             $toast.hide();
		         }, 2000);
			}
		});
	});
	
	
	//非空校验
	$('#weigh,#volume,#pay,#booking_note_number,#arrival_time').on('input',function(){
		var weigh = $('#weigh').val();
		var $weigh = $('#warn_weigh');
		var volume = $('#volume').val();
		var $volume = $('#warn_volume');
		var pay = $('#pay').val();
		var $pay = $('#warn_pay');
		var booking_note_number = $('#booking_note_number').val();
		var $booking_note_number = $('#warn_booking_note_number');
		var arrival_time = $('#arrival_time').val();
		var $arrival_time = $('#warn_arrival_time');
		if(weigh!=''){
			$weigh.hide();
			$volume.hide();
		}
		if(volume!=''){
			$weigh.hide();
			$volume.hide();
		}
		if(pay!=''){
			$pay.hide();
		}
		if(booking_note_number!=''){
			$booking_note_number.hide();
		}
		if(arrival_time!=''){
			$arrival_time.hide();
		}
	})
	
	
	var check = function(){
		var returner = 0.0;
		var weigh = $('#weigh').val();
		var $weigh = $('#warn_weigh');
		var volume = $('#volume').val();
		var $volume = $('#warn_volume');
		var pay = $('#pay').val();
		var $pay = $('#warn_pay');
		var booking_note_number = $('#booking_note_number').val();
		var $booking_note_number = $('#warn_booking_note_number');
		var arrival_time = $('#arrival_time').val();
		var $arrival_time = $('#warn_arrival_time');
		
		if(volume=='' && weigh==''){
			$volume.show();
			$weigh.show();
			returner+=1;
		}
		if(pay==''){
			$pay.show();
			returner+=1;
		}
		if(booking_note_number==''){
			$booking_note_number.show();
			returner+=1;
		}
		if(arrival_time==''){
			$arrival_time.show();
			returner+=1;
		}
		if(returner>0)
			return false;
		else
			return true;
	}
});